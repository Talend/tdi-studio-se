package org.talend.webservice.helper;

import org.apache.ws.commons.schema.*;
import org.w3c.dom.Element;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;
import java.util.*;

public class WSDLMetadataUtils {

    WSDLFactory wsdlFactory = null;

    private Vector<XmlSchema> wsdlTypes = new Vector<XmlSchema>();

    private List<String> parametersName = new ArrayList<String>();

    private List<String> schemaNames = new ArrayList<String>();

    private List<String> documentBaseList = new ArrayList<String>();

    private List<XmlSchemaElement> allXmlSchemaElement = new ArrayList<XmlSchemaElement>();

    private List<XmlSchemaType> allXmlSchemaType = new ArrayList<XmlSchemaType>();

    public final static String DEFAULT_SOAP_ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";

    public WSDLMetadataUtils() throws WSDLException {
        wsdlFactory = WSDLFactory.newInstance();
    }

    public static class OperationInfo {
        QName port;
        QName service;

        String operationName;

        List<ParameterInfo> inputParameters = new ArrayList<ParameterInfo>();
        List<ParameterInfo> outputParameter = new ArrayList<ParameterInfo>();
    }

    public class ParameterInfo {
        String name;

        /* list of parameters, only filled if complex type */
        List<ParameterInfo> childParameters = new ArrayList<ParameterInfo>();
    }

    //not thread safe
    private List<OperationInfo> operations;

    private String targetOperationName;
    private String currentPort;
    private QName currentService;

    public OperationInfo parseOperationInfo(ServiceDiscoveryHelper sdh, String port, String operationName) throws Exception {
        this.targetOperationName = operationName;

        Collection<Definition> defs = sdh.getDefinitions();

        wsdlTypes = createSchemaFromTypes(defs);

        collectAllXmlSchemaElement();

        collectAllXmlSchemaType();

        //only fetch services from main wsdl/definition, others for elements and type definition
        Map services = defs.iterator().next().getServices();
        if (services != null) {
            Iterator iter = services.values().iterator();
            while (iter.hasNext()) {
                List<OperationInfo> operations = getOperations((Service) iter.next());
                for(OperationInfo info : operations) {
                    if(port==null) {
                        return info;
                    }
                    if(port.equals(info.port.getLocalPart())) {
                        return info;
                    }
                }
            }
        }

        throw new RuntimeException("can't find the operation : " + operationName + " with port : " + port);
    }

    private void collectAllXmlSchemaElement() {
        for (int i = 0; i < wsdlTypes.size(); i++) {
            XmlSchema xmlSchema = (wsdlTypes.elementAt(i));
            if (xmlSchema == null) {
                continue;
            }
            Map<QName, XmlSchemaElement> elements = xmlSchema.getElements();
            Iterator elementsItr = elements.values().iterator();
            while (elementsItr.hasNext()) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) elementsItr.next();

                allXmlSchemaElement.add(xmlSchemaElement);
            }
        }
    }

    private void collectAllXmlSchemaType() {
        for (int i = 0; i < wsdlTypes.size(); i++) {
            XmlSchema xmlSchema = (wsdlTypes.elementAt(i));
            if (xmlSchema == null) {
                continue;
            }
            Map<QName, XmlSchemaType> xmlSchemaObjectTable = xmlSchema.getSchemaTypes();
            Iterator typesItr = xmlSchemaObjectTable.values().iterator();
            while (typesItr.hasNext()) {
                XmlSchemaType xmlSchemaType = (XmlSchemaType) typesItr.next();
                allXmlSchemaType.add(xmlSchemaType);
            }
        }

    }

    protected Vector<XmlSchema> createSchemaFromTypes(Collection<Definition> wsdlDefinitions) throws WSDLException {
        Vector<XmlSchema> schemas = new Vector<XmlSchema>();
        Set<String> imports = new HashSet<String>();
        Element schemaElementt = null;
        Map importElement = null;
        List includeElement = null;
        for (Definition def : wsdlDefinitions) {
            if (def.getTypes() != null) {
                List schemaExtElem = findExtensibilityElement(def.getTypes().getExtensibilityElements(), "schema");
                for (int i = 0; i < schemaExtElem.size(); i++) {
                    ExtensibilityElement schemaElement = (ExtensibilityElement) schemaExtElem.get(i);
                    if (schemaElement != null && schemaElement instanceof UnknownExtensibilityElement) {
                        schemaElementt = ((UnknownExtensibilityElement) schemaElement).getElement();

                        String documentBase = ((javax.wsdl.extensions.schema.Schema) schemaElement).getDocumentBaseURI();
                        XmlSchema schema = createschemafromtype(schemaElementt, def, documentBase);
                        if (schema != null) {
                            schemas.add(schema);
                            if (schema.getTargetNamespace() != null) {
                                schemaNames.add(schema.getTargetNamespace());
                            }
                        }
                        importElement = ((javax.wsdl.extensions.schema.Schema) schemaElement).getImports();
                        if (importElement != null && importElement.size() > 0) {
                            findImportSchema(def, schemas, importElement, imports);
                        }
                    }

                    if (schemaElement != null && schemaElement instanceof javax.wsdl.extensions.schema.Schema) {
                        schemaElementt = ((javax.wsdl.extensions.schema.Schema) schemaElement).getElement();
                        String documentBase = ((javax.wsdl.extensions.schema.Schema) schemaElement).getDocumentBaseURI();
                        Boolean isHaveImport = false;
                        importElement = ((javax.wsdl.extensions.schema.Schema) schemaElement).getImports();
                        if (importElement != null && importElement.size() > 0) {
                            Iterator keyIterator = importElement.keySet().iterator();
                            if (importElement.size() > 0) {
                                isHaveImport = true;
                            }
                        }

                        XmlSchema schema = createschemafromtype(schemaElementt, def, documentBase);
                        if (schema != null) {
                            schemas.add(schema);
                            if (schema.getTargetNamespace() != null) {
                                schemaNames.add(schema.getTargetNamespace());
                            }
                        }

                        if (isHaveImport) {
                            findImportSchema(def, schemas, importElement, imports);
                        }
                    }
                }

            }
        }
        return schemas;
    }

    private void findIncludesSchema(Definition wsdlDefinition, Vector schemas, List includeElement) throws WSDLException {
        Element schemaElementt;
        for (int i = 0; i < includeElement.size(); i++) {

            schemaElementt = ((com.ibm.wsdl.extensions.schema.SchemaReferenceImpl) includeElement.get(i)).getReferencedSchema()
                    .getElement();
            String documentBase = ((com.ibm.wsdl.extensions.schema.SchemaReferenceImpl) includeElement.get(i))
                    .getReferencedSchema().getDocumentBaseURI();
            XmlSchema schemaInclude = createschemafromtype(schemaElementt, wsdlDefinition, documentBase);
            if (schemaInclude != null) {
                schemas.add(schemaInclude);
                if (schemaInclude.getTargetNamespace() != null) {
                    schemaNames.add(schemaInclude.getTargetNamespace());
                }
            }
        }
    }

    private void findImportSchema(Definition wsdlDefinition, Vector schemas, Map importElement, Set<String> imports)
            throws WSDLException {
        Element schemaElementt;
        List includeElement = null;
        Iterator keyIterator = importElement.keySet().iterator();
        Boolean isHaveImport = false;
        while (keyIterator.hasNext()) {
            Object object = keyIterator.next();
            if (object != null) {
                String key = object.toString();
                Vector importEle = (Vector) importElement.get(key);

                for (int i = 0; i < importEle.size(); i++) {
                    Map importChildElement = null;
                    com.ibm.wsdl.extensions.schema.SchemaImportImpl importImpl = (com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle
                            .elementAt(i);
                    // to avoid import cycle
                    String importLocation = importImpl.getSchemaLocationURI() + ":" + importImpl.getNamespaceURI();
                    if (imports.contains(importLocation)) {
                        continue;
                    } else {
                        imports.add(importLocation);
                    }
                    if (importImpl.getReferencedSchema() != null) {

                        schemaElementt = importImpl.getReferencedSchema().getElement();
                        String documentBase = importImpl.getReferencedSchema().getDocumentBaseURI();

                        if ((com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle.elementAt(i) != null) {
                            if (((com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle.elementAt(i)).getReferencedSchema() != null) {
                                importChildElement = ((com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle.elementAt(i))
                                        .getReferencedSchema().getImports();
                                if (importChildElement != null && importChildElement.size() > 0 && !isIncludeSchema(documentBase)) {
                                    isHaveImport = true;
                                    documentBaseList.add(documentBase);
                                    // validateImportUrlPath(importElement);
                                }
                            }
                        }

                        XmlSchema schemaImport = createschemafromtype(schemaElementt, wsdlDefinition, documentBase);
                        if (schemaImport != null) {
                            schemas.add(schemaImport);
                            if (schemaImport.getTargetNamespace() != null) {
                                schemaNames.add(schemaImport.getTargetNamespace());
                            }
                        }
                    }

                    if (isHaveImport) {
                        findImportSchema(wsdlDefinition, schemas, importChildElement, imports);
                    }

                    if ((com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle.elementAt(i) != null) {
                        if (((com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle.elementAt(i)).getReferencedSchema() != null) {
                            includeElement = ((com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle.elementAt(i))
                                    .getReferencedSchema().getIncludes();
                            if (includeElement != null && includeElement.size() > 0) {

                                findIncludesSchema(wsdlDefinition, schemas, includeElement);
                            }
                        }
                    }

                }
            }
        }
    }

    private List findExtensibilityElement(List extensibilityElements, String elementType) {
        List elements = new ArrayList();
        if (extensibilityElements != null) {
            Iterator iter = extensibilityElements.iterator();
            while (iter.hasNext()) {
                ExtensibilityElement element = (ExtensibilityElement) iter.next();
                if (element.getElementType().getLocalPart().equalsIgnoreCase(elementType)) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }

    private XmlSchema createschemafromtype(Element schemaElement, Definition wsdlDefinition, String documentBase)
            throws WSDLException {
        if (schemaElement == null) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "Unable to find schema extensibility element in WSDL");
        }

        XmlSchema xmlSchema = null;
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        xmlSchemaCollection.setBaseUri(fixDocumentBase(documentBase));

        xmlSchema = xmlSchemaCollection.read(schemaElement);

        return xmlSchema;
    }

    private String fixDocumentBase(String documentBase) {
        String fixedPath = documentBase;
        try {
            URL url = new URL(documentBase);
            File file = new File(url.getFile());
            fixedPath = file.toURI().toString();
        } catch (Exception e) {
            fixedPath = documentBase;
        }
        return fixedPath;
    }

    private Boolean isIncludeSchema(String documentBase) {
        Boolean isHaveSchema = false;
        for (int i = 0; i < documentBaseList.size(); i++) {
            String documentBaseTem = documentBaseList.get(i);
            if (documentBaseTem.equals(documentBase)) {
                isHaveSchema = true;
            }
        }
        return isHaveSchema;
    }

    private List<OperationInfo> getOperations(Service service) {
        currentService = service.getQName();
        List<OperationInfo> result = new ArrayList<>();

        Map ports = service.getPorts();
        Iterator portIter = ports.values().iterator();
        while (portIter.hasNext()) {
            Port port = (Port) portIter.next();
            Binding binding = port.getBinding();

            currentPort = port.getName();

            result.addAll(buildOperations(binding));
        }

        return result;
    }

    private List<OperationInfo> buildOperations(Binding binding) {
        List<OperationInfo> result = new ArrayList<>();

        List operations = binding.getBindingOperations();

        if (operations != null && !operations.isEmpty()) {
            List soapBindingElems = findExtensibilityElement(binding.getExtensibilityElements(), "binding");
            String style = "document"; // default

            ExtensibilityElement soapBindingElem = (ExtensibilityElement) soapBindingElems.get(0);
            if (soapBindingElem != null && soapBindingElem instanceof SOAPBinding) {
                SOAPBinding soapBinding = (SOAPBinding) soapBindingElem;
                style = soapBinding.getStyle();
            } else if (soapBindingElem != null && soapBindingElem instanceof SOAP12Binding) {
                SOAP12Binding soapBinding = (SOAP12Binding) soapBindingElem;
                style = soapBinding.getStyle();
            }

            Iterator opIter = operations.iterator();

            while (opIter.hasNext()) {
                BindingOperation oper = (BindingOperation) opIter.next();
                List operElems = findExtensibilityElement(oper.getExtensibilityElements(), "operation");
                ExtensibilityElement operElem = (ExtensibilityElement) operElems.get(0);

                if(!targetOperationName.equals(oper.getName())) {
                    continue;
                }

                OperationInfo operationInfo = new OperationInfo();
                operationInfo.service = currentService;
                operationInfo.port = new QName(currentService.getNamespaceURI(), currentPort);
                operationInfo.operationName = oper.getName();

                //TODO do different?
                if (operElem != null && operElem instanceof SOAPOperation) {
                    buildOperation(operationInfo, oper);
                } else if (operElem != null && operElem instanceof SOAP12Operation) {
                    buildOperation(operationInfo, oper);
                }

                result.add(operationInfo);
            }
        }

        return result;
    }

    private void buildOperation(OperationInfo operationInfo, BindingOperation bindingOper) {
        Operation oper = bindingOper.getOperation();

        List operElems = findExtensibilityElement(bindingOper.getExtensibilityElements(), "operation");
        ExtensibilityElement operElem = (ExtensibilityElement) operElems.get(0);
        if (operElem != null && operElem instanceof SOAPOperation) {//TODO do different?
            SOAPOperation soapOperation = (SOAPOperation) operElem;
        } else if (operElem != null && operElem instanceof SOAP12Operation) {
            SOAP12Operation soapOperation = (SOAP12Operation) operElem;
        }
        BindingInput bindingInput = bindingOper.getBindingInput();
        BindingOutput bindingOutput = bindingOper.getBindingOutput();
        List bodyElems = findExtensibilityElement(bindingInput.getExtensibilityElements(), "body");
        ExtensibilityElement bodyElem = (ExtensibilityElement) bodyElems.get(0);

        if (bodyElem != null && bodyElem instanceof SOAPBody) {
            SOAPBody soapBody = (SOAPBody) bodyElem;
            List styles = soapBody.getEncodingStyles();
            String encodingStyle = null;
            if (styles != null) {
                encodingStyle = styles.get(0).toString();
            }
            if (encodingStyle == null) {
                encodingStyle = DEFAULT_SOAP_ENCODING_STYLE;
            }
            //TODO get namespace uri here?
            //soapBody.getNamespaceURI();
        } else if (bodyElem != null && bodyElem instanceof SOAP12Body) {
            SOAP12Body soapBody = (SOAP12Body) bodyElem;
            String encodingStyle = null;
            if (soapBody.getEncodingStyle() != null) {
                encodingStyle = soapBody.getEncodingStyle().toString();
            }
            if (encodingStyle == null) {
                encodingStyle = DEFAULT_SOAP_ENCODING_STYLE;
            }
            //TODO get namespace uri here?
            //soapBody.getNamespaceURI();
        }

        Input inDef = oper.getInput();
        if (inDef != null) {
            Message inMsg = inDef.getMessage();
            if (inMsg != null) {
                getParameterFromMessage(operationInfo, inMsg, 1);
            }
        }

        //don't need output parameter struct now
        /*
        Output outDef = oper.getOutput();
        if (outDef != null) {
            Message outMsg = outDef.getMessage();
            if (outMsg != null) {
                getParameterFromMessage(operationInfo, outMsg, 2);
            }
        }
        */
    }

    private void getParameterFromMessage(OperationInfo operationInfo, Message msg, int manner) {
        List msgParts = msg.getOrderedParts(null);
        Iterator iter = msgParts.iterator();
        while (iter.hasNext()) {
            Part part = (Part) iter.next();
            String partName = part.getName();
            String partElement = null;
            String namespace = null;
            if (part.getElementName() != null) {
                partElement = part.getElementName().getLocalPart();
                namespace = part.getElementName().getNamespaceURI();
            } else if (part.getTypeName() != null) {
                partElement = part.getTypeName().getLocalPart();
                namespace = part.getTypeName().getNamespaceURI();
            }
            // add root parameter from message.
            ParameterInfo parameterRoot = new ParameterInfo();
            parameterRoot.name = partName;
            if (manner == 1) {
                operationInfo.inputParameters.add(parameterRoot);
            } else {
                operationInfo.outputParameter.add(parameterRoot);
            }
            if (allXmlSchemaElement.size() > 0) {
                buildParameterFromElements(partElement, parameterRoot, manner);
            } else if (allXmlSchemaType.size() > 0) {
                buileParameterFromTypes(namespace, partElement, parameterRoot, manner);
            }
        }
    }

    private void buildParameterFromElements(String partElement, ParameterInfo parameterRoot, int ioOrRecursion) {
        if (ioOrRecursion < 3) {
            parametersName.clear();
            parametersName.add(parameterRoot.name);
        } else if (ioOrRecursion == 3) {
            parametersName.add(parameterRoot.name);
        }
        Iterator elementsItr = allXmlSchemaElement.iterator();
        if (partElement != null) {
            while (elementsItr.hasNext()) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) elementsItr.next();
                if (partElement.equals(xmlSchemaElement.getName())) {
                    if (xmlSchemaElement.getSchemaType() != null) {
                        if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                            XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                            XmlSchemaParticle xmlSchemaParticle = xmlElementComplexType.getParticle();
                            if (xmlSchemaParticle instanceof XmlSchemaGroupParticle) {
                                XmlSchemaGroupParticle xmlSchemaGroupBase = (XmlSchemaGroupParticle) xmlSchemaParticle;
                                if (xmlSchemaGroupBase != null) {
                                    buildParameterFromCollection(xmlSchemaGroupBase, parameterRoot, ioOrRecursion);
                                }
                            } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                                String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                                String paraTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                                if (paraTypeName != null) {
                                    buileParameterFromTypes(paraTypeNamespace, paraTypeName, parameterRoot, ioOrRecursion);
                                }
                            }
                        } else if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaSimpleType) {
                            XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaElement.getSchemaType();
                            String typeName = xmlSchemaSimpleType.getName();
                            if (typeName != null && typeName.equals("anyType")) {
                                ParameterInfo parameterSon = new ParameterInfo();
                                parameterSon.name = "anyType";
                                parameterRoot.childParameters.add(parameterSon);
                            }
                        }
                    } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                        String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                        String paraTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                        if (paraTypeName != null) {
                            buileParameterFromTypes(paraTypeNamespace, paraTypeName, parameterRoot, ioOrRecursion);
                        }
                    }
                }
            }
        }
    }

    private void buileParameterFromTypes(String paraNamespace, String paraType, ParameterInfo parameter, int ioOrRecursion) {
        if (ioOrRecursion < 3) {
            parametersName.clear();
            parametersName.add(parameter.name);
        } else if (ioOrRecursion == 3) {
            parametersName.add(parameter.name);
        }

        //twebserviceinput need auto metadata fetch for input parameters, but it only support very easy, not support custom defined type usage like this:
        //<element type="tns="s:anyCustomDefinedSimpleOrComplexType"">
        //so nothing to do here, TODO make sure it
    }

    private void buildParameterFromCollection(XmlSchemaGroupParticle xmlSchemaGroupParticle, ParameterInfo parameter,
            int ioOrRecursion) {
        if (!(xmlSchemaGroupParticle instanceof XmlSchemaSequence)) {
            throw new RuntimeException("don't support that complex parameter type, only support xsd:sequence");
        }

        XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) xmlSchemaGroupParticle;
        List<XmlSchemaSequenceMember> sequences = xmlSchemaSequence.getItems();

        for (XmlSchemaSequenceMember sequence : sequences) {
            if (sequence instanceof XmlSchemaAny) {//TODO remove it as not support too
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.name = "_content_";
                parameter.childParameters.add(parameterSon);
            } else if (sequence instanceof XmlSchemaElement) {//this is the major part we support
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) sequence;
                String elementName = xmlSchemaElement.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.name= elementName;

                parameter.childParameters.add(parameterSon);

                Boolean isHave = false;
                if (!parametersName.isEmpty() && parameterSon.name != null) {
                    for (int p = 0; p < parametersName.size(); p++) {
                        if (parameterSon.name.equals(parametersName.get(p))) {
                            isHave = true;
                        }
                    }
                }

                if (xmlSchemaElement.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                    String elementTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                    if (elementTypeName != null && elementTypeName.equals("anyType")) {//TODO remove it
                        parameterSon.name = xmlSchemaElement.getName() + ":anyType";
                    }
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes(elementTypeNamespace, elementTypeName, parameterSon, ioOrRecursion);
                    }
                } else if (xmlSchemaElement.getSchemaType() != null) {
                    if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                        throw new RuntimeException("don't support nested or ref complex type in xsd:sequence");
                    } else if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaSimpleType) {
                        XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaElement.getSchemaType();
                        String typeName = xmlSchemaSimpleType.getName();
                        if (typeName != null && typeName.equals("anyType")) {
                            ParameterInfo pSon = new ParameterInfo();
                            pSon.name = "anyType";
                            parameter.childParameters.add(pSon);
                        }
                    }
                } else if (xmlSchemaElement.getRef() != null) {//TODO twebserviceinput support it before?
                    String elementTypeName = xmlSchemaElement.getRef().getTargetQName().getLocalPart();
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buildParameterFromElements(elementTypeName, parameterSon, ioOrRecursion);
                    }
                }
            } else if (sequence instanceof XmlSchemaAttribute) {
                XmlSchemaAttribute xmlSchemaAttribute = (XmlSchemaAttribute) sequence;
                String elementName = xmlSchemaAttribute.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.name = elementName;

                parameter.childParameters.add(parameterSon);
                Boolean isHave = false;
                if (!parametersName.isEmpty() && parameterSon.name != null) {
                    for (int p = 0; p < parametersName.size(); p++) {
                        if (parameterSon.name.equals(parametersName.get(p))) {
                            isHave = true;
                        }
                    }

                }
                if (xmlSchemaAttribute.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaAttribute.getSchemaTypeName().getLocalPart();
                    String elementTypeNamespace = xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI();
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes(elementTypeNamespace, elementTypeName, parameterSon, ioOrRecursion);
                    }
                } else if (xmlSchemaAttribute.getRef() != null) {//TODO twebserviceinput support it before?
                    String refName = xmlSchemaAttribute.getRef().getTargetQName().getLocalPart();
                    if (!isHave) {
                        buildParameterFromElements(refName, parameterSon, ioOrRecursion);
                    }
                }
            } else {
                throw new RuntimeException("don't support the nest type in xsd:sequence");
            }
        }

    }

}
