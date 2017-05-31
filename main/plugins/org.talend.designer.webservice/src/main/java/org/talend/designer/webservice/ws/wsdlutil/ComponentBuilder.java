package org.talend.designer.webservice.ws.wsdlutil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroupBase;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.talend.designer.webservice.ws.wsdlinfo.OperationInfo;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;
import org.talend.designer.webservice.ws.wsdlinfo.PortNames;
import org.talend.designer.webservice.ws.wsdlinfo.ServiceInfo;

/**
 * DOC gcui class global comment. Detailled comment
 */
public class ComponentBuilder {

    WSDLFactory wsdlFactory = null;

    private Vector<XmlSchema> wsdlTypes = new Vector<XmlSchema>();

    private int inOrOut;

    private List<String> parametersName = new ArrayList<String>();

    private List<String> schemaNames = new ArrayList<String>();

    private List<String> documentBaseList = new ArrayList<String>();

    private List<XmlSchemaElement> allXmlSchemaElement = new ArrayList<XmlSchemaElement>();

    private List<XmlSchemaType> allXmlSchemaType = new ArrayList<XmlSchemaType>();

    private List<String> alldExtendtion = new ArrayList<String>();

    private XmlSchemaCollection schemaCollection;

    public final static String DEFAULT_SOAP_ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";

    // SimpleTypesFactory simpleTypesFactory = null;

    public ComponentBuilder() throws WSDLException {
        wsdlFactory = WSDLFactory.newInstance();
        // simpleTypesFactory = new SimpleTypesFactory();
    }

    /**
     * Getter for allXmlSchemaType.
     * 
     * @return the allXmlSchemaType
     */
    public List<XmlSchemaType> getAllXmlSchemaType() {
        return this.allXmlSchemaType;
    }

    /**
     * Getter for allXmlSchemaElement.
     * 
     * @return the allXmlSchemaElement
     */
    public List<XmlSchemaElement> getAllXmlSchemaElement() {
        return this.allXmlSchemaElement;
    }

    public ServiceInfo buildserviceinformation(ServiceInfo serviceinfo) throws Exception {
        // WSDLReader reader = wsdlFactory.newWSDLReader();
        // Definition def = reader.readWSDL(null, serviceinfo.getWsdlUri());
        ServiceDiscoveryHelper sdh;
        if (serviceinfo.getAuthConfig() != null && serviceinfo.getWsdlUri().indexOf("http") == 0) {
            sdh = new ServiceDiscoveryHelper(serviceinfo.getWsdlUri(), serviceinfo.getAuthConfig());

        } else {
            sdh = new ServiceDiscoveryHelper(serviceinfo.getWsdlUri());
        }
        List<Definition> defs = sdh.getDefinitions();

        wsdlTypes = createSchemaFromTypes(defs);

        collectAllXmlSchemaElement();

        collectAllXmlSchemaType();

        Map services = defs.get(0).getServices();
        if (services != null) {
            Iterator svcIter = services.values().iterator();
            while (svcIter.hasNext()) {
                populateComponent(serviceinfo, (Service) svcIter.next());
            }
        }
        return serviceinfo;
    }

    /**
     * DOC gcui Comment method "collectAllElement".
     * 
     * @return
     */
    private void collectAllXmlSchemaElement() {
        for (int i = 0; i < wsdlTypes.size(); i++) {
            XmlSchema xmlSchema = (wsdlTypes.elementAt(i));
            if (xmlSchema == null) {
                continue;
            }
            XmlSchemaObjectTable elements = xmlSchema.getElements();
            Iterator elementsItr = elements.getValues();
            while (elementsItr.hasNext()) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) elementsItr.next();

                allXmlSchemaElement.add(xmlSchemaElement);
            }
        }
    }

    /**
     * DOC gcui Comment method "collectAllXmlSchemaType".
     */
    private void collectAllXmlSchemaType() {
        for (int i = 0; i < wsdlTypes.size(); i++) {
            XmlSchema xmlSchema = (wsdlTypes.elementAt(i));
            if (xmlSchema == null) {
                continue;
            }
            XmlSchemaObjectTable xmlSchemaObjectTable = xmlSchema.getSchemaTypes();
            Iterator typesItr = xmlSchemaObjectTable.getValues();
            while (typesItr.hasNext()) {
                XmlSchemaType xmlSchemaType = (XmlSchemaType) typesItr.next();
                allXmlSchemaType.add(xmlSchemaType);
            }
        }

    }

    protected Vector<XmlSchema> createSchemaFromTypes(List<Definition> wsdlDefinitions) throws WSDLException {
        Vector<XmlSchema> schemas = new Vector<XmlSchema>();
        Set<String> imports = new HashSet<String>();
        org.w3c.dom.Element schemaElementt = null;
        Map importElement = null;
        List includeElement = null;
        for (Definition def : wsdlDefinitions) {
            if (def.getTypes() != null) {
                Vector schemaExtElem = findExtensibilityElement(def.getTypes().getExtensibilityElements(), "schema");
                for (int i = 0; i < schemaExtElem.size(); i++) {
                    ExtensibilityElement schemaElement = (ExtensibilityElement) schemaExtElem.elementAt(i);
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
                            // while (keyIterator.hasNext()) {
                            // String key = keyIterator.next().toString();
                            // Vector importEle = (Vector) importElement.get(key);
                            // for (int j = 0; j < importEle.size(); j++) {
                            // com.ibm.wsdl.extensions.schema.SchemaImportImpl importValidate =
                            // (com.ibm.wsdl.extensions.schema.SchemaImportImpl) importEle
                            // .elementAt(j);
                            // if (importValidate.getSchemaLocationURI() == null) {
                            // importElement.remove(key);
                            // }
                            // }
                            // }
                            if (importElement.size() > 0) {
                                isHaveImport = true;
                            }
                            // validateImportUrlPath(importElement);
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

    /**
     * DOC gcui Comment method "findIncludesSchema".
     * 
     * @param wsdlDefinition
     * @param schemas
     * @param includeElement
     * @throws WSDLException
     */
    private void findIncludesSchema(Definition wsdlDefinition, Vector schemas, List includeElement) throws WSDLException {
        org.w3c.dom.Element schemaElementt;
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
        org.w3c.dom.Element schemaElementt;
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

    private Vector findExtensibilityElement(List extensibilityElements, String elementType) {

        Vector elements = new Vector();
        if (extensibilityElements != null) {
            Iterator iter = extensibilityElements.iterator();
            while (iter.hasNext()) {
                ExtensibilityElement elment = (ExtensibilityElement) iter.next();
                if (elment.getElementType().getLocalPart().equalsIgnoreCase(elementType)) {
                    elements.add(elment);
                }
            }
        }
        return elements;
    }

    private XmlSchema createschemafromtype(org.w3c.dom.Element schemaElement, Definition wsdlDefinition, String documentBase)
            throws WSDLException {
        if (schemaElement == null) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "Unable to find schema extensibility element in WSDL");
        }

        XmlSchema xmlSchema = null;
        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        xmlSchemaCollection.setBaseUri(fixDocumentBase(documentBase));

        xmlSchema = xmlSchemaCollection.read(schemaElement);

        // XmlSchemaObjectTable xmlSchemaObjectTable = xmlSchema.getSchemaTypes();

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

    private ServiceInfo populateComponent(ServiceInfo component, Service service) {
        QName qName = service.getQName();
        String namespace = qName.getNamespaceURI();
        String name = qName.getLocalPart();
        component.setServerName(name);
        component.setServerNameSpace(namespace);
        Map ports = service.getPorts();
        Iterator portIter = ports.values().iterator();
        while (portIter.hasNext()) {
            Port port = (Port) portIter.next();
            Binding binding = port.getBinding();
            if (port.getName() != null && component.getPortNames() == null) {
                List<PortNames> portNames = new ArrayList();
                PortNames portName = new PortNames();
                portName.setPortName(port.getName());
                portNames.add(portName);
                component.setPortNames(portNames);
            } else if (port.getName() != null && component.getPortNames() != null) {
                PortNames portName = new PortNames();
                portName.setPortName(port.getName());
                component.getPortNames().add(portName);
            }
            List operations = buildOperations(binding);
            Iterator operIter = operations.iterator();
            while (operIter.hasNext()) {
                OperationInfo operation = (OperationInfo) operIter.next();
                Vector addrElems = findExtensibilityElement(port.getExtensibilityElements(), "address");
                ExtensibilityElement element = (ExtensibilityElement) addrElems.elementAt(0);
                if (element != null && element instanceof SOAPAddress) {
                    SOAPAddress soapAddr = (SOAPAddress) element;
                    operation.setTargetURL(soapAddr.getLocationURI());
                } else if (element != null && element instanceof SOAP12Address) {
                    SOAP12Address soapAddr = (SOAP12Address) element;
                    operation.setTargetURL(soapAddr.getLocationURI());
                }
                component.addOperation(operation);
            }
        }
        return component;
    }

    private List buildOperations(Binding binding) {
        List operationInfos = new ArrayList();

        List operations = binding.getBindingOperations();

        if (operations != null && !operations.isEmpty()) {

            Vector soapBindingElems = findExtensibilityElement(binding.getExtensibilityElements(), "binding");
            String style = "document"; // default

            ExtensibilityElement soapBindingElem = (ExtensibilityElement) soapBindingElems.elementAt(0);
            if (soapBindingElem != null && soapBindingElem instanceof SOAPBinding) {
                SOAPBinding soapBinding = (SOAPBinding) soapBindingElem;
                style = soapBinding.getStyle();
            } else if (soapBindingElem != null && soapBindingElem instanceof SOAP12Binding) {
                SOAP12Binding soapBinding = (SOAP12Binding) soapBindingElem;
                style = soapBinding.getStyle();
            }

            Iterator opIter = operations.iterator();

            while (opIter.hasNext()) {
                alldExtendtion.clear();
                BindingOperation oper = (BindingOperation) opIter.next();
                Vector operElems = findExtensibilityElement(oper.getExtensibilityElements(), "operation");
                ExtensibilityElement operElem = (ExtensibilityElement) operElems.elementAt(0);
                if (operElem != null && operElem instanceof SOAPOperation) {

                    OperationInfo operationInfo = new OperationInfo(style);
                    buildOperation(operationInfo, oper);
                    operationInfos.add(operationInfo);
                } else if (operElem != null && operElem instanceof SOAP12Operation) {
                    OperationInfo operationInfo = new OperationInfo(style);
                    buildOperation(operationInfo, oper);
                    operationInfos.add(operationInfo);
                }
            }
        }

        return operationInfos;
    }

    private OperationInfo buildOperation(OperationInfo operationInfo, BindingOperation bindingOper) {
        Operation oper = bindingOper.getOperation();
        operationInfo.setTargetMethodName(oper.getName());
        Vector operElems = findExtensibilityElement(bindingOper.getExtensibilityElements(), "operation");
        ExtensibilityElement operElem = (ExtensibilityElement) operElems.elementAt(0);
        if (operElem != null && operElem instanceof SOAPOperation) {
            SOAPOperation soapOperation = (SOAPOperation) operElem;
            operationInfo.setSoapActionURI(soapOperation.getSoapActionURI());
        } else if (operElem != null && operElem instanceof SOAP12Operation) {
            SOAP12Operation soapOperation = (SOAP12Operation) operElem;
            operationInfo.setSoapActionURI(soapOperation.getSoapActionURI());
        }
        BindingInput bindingInput = bindingOper.getBindingInput();
        BindingOutput bindingOutput = bindingOper.getBindingOutput();
        Vector bodyElems = findExtensibilityElement(bindingInput.getExtensibilityElements(), "body");
        ExtensibilityElement bodyElem = (ExtensibilityElement) bodyElems.elementAt(0);

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
            operationInfo.setEncodingStyle(encodingStyle.toString());
            operationInfo.setTargetObjectURI(soapBody.getNamespaceURI());
        } else if (bodyElem != null && bodyElem instanceof SOAP12Body) {
            SOAP12Body soapBody = (SOAP12Body) bodyElem;
            String encodingStyle = null;
            if (soapBody.getEncodingStyle() != null) {
                encodingStyle = soapBody.getEncodingStyle().toString();
            }
            if (encodingStyle == null) {
                encodingStyle = DEFAULT_SOAP_ENCODING_STYLE;
            }
            operationInfo.setEncodingStyle(encodingStyle.toString());
            operationInfo.setTargetObjectURI(soapBody.getNamespaceURI());
        }

        Input inDef = oper.getInput();
        if (inDef != null) {
            Message inMsg = inDef.getMessage();
            if (inMsg != null) {
                operationInfo.setInputMessageName(inMsg.getQName().getLocalPart());
                getParameterFromMessage(operationInfo, inMsg, 1);
                operationInfo.setInmessage(inMsg);
            }
        }

        Output outDef = oper.getOutput();
        if (outDef != null) {
            Message outMsg = outDef.getMessage();
            if (outMsg != null) {
                operationInfo.setOutputMessageName(outMsg.getQName().getLocalPart());
                getParameterFromMessage(operationInfo, outMsg, 2);
                operationInfo.setOutmessage(outMsg);
            }
        }

        return operationInfo;
    }

    private void getParameterFromMessage(OperationInfo operationInfo, Message msg, int manner) {

        // inOrOut = manner;
        List msgParts = msg.getOrderedParts(null);
        // msg.getQName();
        // Schema wsdlType = null;
        // XmlSchema xmlSchema = null;
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
            // add first parameter from message.
            ParameterInfo parameterRoot = new ParameterInfo();
            parameterRoot.setName(partName);
            parameterRoot.setNameSpace(namespace);
            if (manner == 1) {
                operationInfo.addInparameter(parameterRoot);
            } else {
                operationInfo.addOutparameter(parameterRoot);
            }
            // String targetnamespace = "";
            // ComplexType complexType = null;

            // for (int i = 0; i < wsdlTypes.size(); i++) {
            // xmlSchema = (XmlSchema) (wsdlTypes.elementAt(i));
            // if (xmlSchema == null) {
            // continue;
            // }
            // if (xmlSchema != null && xmlSchema.getTargetNamespace() != null) {
            // targetnamespace = xmlSchema.getTargetNamespace();
            // operationInfo.setNamespaceURI(targetnamespace);
            // }
            if (allXmlSchemaElement.size() > 0) {

                buildParameterFromElements(partElement, parameterRoot, manner);

            } else if (allXmlSchemaType.size() > 0) {

                buileParameterFromTypes(namespace, partElement, parameterRoot, manner);
            }
            operationInfo.setWsdltype(wsdlTypes);
        }
    }

    private void buildParameterFromElements(String partElement, ParameterInfo parameterRoot, int ioOrRecursion) {
        // XmlSchemaObjectTable elements = xmlSchema.getElements();
        if (ioOrRecursion < 3) {
            parametersName.clear();
            parametersName.add(parameterRoot.getName());
        } else if (ioOrRecursion == 3) {
            parametersName.add(parameterRoot.getName());
        }
        Iterator elementsItr = allXmlSchemaElement.iterator();
        if (partElement != null) {
            while (elementsItr.hasNext()) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) elementsItr.next();
                if (partElement.equals(xmlSchemaElement.getName())) {
                    // ParameterInfo parameter = new ParameterInfo();
                    // parameter.setName(partName);
                    if (xmlSchemaElement.getSchemaType() != null) {
                        if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                            XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                            XmlSchemaParticle xmlSchemaParticle = xmlElementComplexType.getParticle();
                            if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                                XmlSchemaObjectCollection xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                                if (xmlSchemaObjectCollection != null) {
                                    buildParameterFromCollection(xmlSchemaObjectCollection, parameterRoot, ioOrRecursion);
                                }
                            } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                                String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                                String paraTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                                if (paraTypeName != null) {
                                    parameterRoot.setType(paraTypeName);
                                    buileParameterFromTypes(paraTypeNamespace, paraTypeName, parameterRoot, ioOrRecursion);
                                }
                            }
                        } else if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaSimpleType) {
                            XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaElement.getSchemaType();
                            String typeName = xmlSchemaSimpleType.getName();
                            if (typeName != null && typeName.equals("anyType")) {
                                ParameterInfo parameterSon = new ParameterInfo();
                                parameterSon.setName("anyType");
                                parameterSon.setParent(parameterRoot);
                                parameterRoot.getParameterInfos().add(parameterSon);
                            }
                            parameterRoot.setType(typeName);
                        }

                    } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                        String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                        String paraTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                        if (paraTypeName != null) {
                            parameterRoot.setType(paraTypeName);
                            buileParameterFromTypes(paraTypeNamespace, paraTypeName, parameterRoot, ioOrRecursion);
                        }
                    }
                }
            }
        }

    }

    /**
     * DOC gcui Comment method "buileParameterFromTypes".
     * 
     * @param paraType
     * @param parameter
     * @param operationInfo
     * @param i
     */
    private void buileParameterFromTypes(String paraNamespace, String paraType, ParameterInfo parameter, int ioOrRecursion) {
        if (ioOrRecursion < 3) {
            parametersName.clear();
            parametersName.add(parameter.getName());
        } else if (ioOrRecursion == 3) {
            parametersName.add(parameter.getName());
        }
        for (int i = 0; i < allXmlSchemaType.size(); i++) {
            XmlSchemaType type = allXmlSchemaType.get(i);
            String typeName = type.getName();
            if (paraType.equals(typeName) && paraNamespace.equals(type.getQName().getNamespaceURI())) {
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType xmlSchemaComplexType = (XmlSchemaComplexType) type;
                    XmlSchemaParticle xmlSchemaParticle = xmlSchemaComplexType.getParticle();
                    XmlSchemaObjectCollection xmlSchemaObjectCollection = null;
                    if (xmlSchemaParticle == null && xmlSchemaComplexType.getContentModel() != null) {
                        Object obj = xmlSchemaComplexType.getContentModel().getContent();
                        if (obj instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension xscce = (XmlSchemaComplexContentExtension) obj;
                            if (xscce.getBaseTypeName() != null) {
                                buileParameterFromTypes(xscce.getBaseTypeName().getNamespaceURI(), xscce.getBaseTypeName()
                                        .getLocalPart(), parameter, ioOrRecursion);
                            }
                            if (xscce != null) {
                                xmlSchemaParticle = xscce.getParticle();
                            }
                            if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                                xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                            }

                        } else if (obj instanceof XmlSchemaComplexContentRestriction) {
                            XmlSchemaComplexContentRestriction xsccr = (XmlSchemaComplexContentRestriction) obj;
                            xmlSchemaObjectCollection = xsccr.getAttributes();
                        }

                    } else if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                        XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                        xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                    }
                    if (xmlSchemaObjectCollection != null && xmlSchemaObjectCollection.getCount() > 0) {
                        buildParameterFromCollection(xmlSchemaObjectCollection, parameter, 3);
                    } else if (xmlSchemaObjectCollection != null && xmlSchemaObjectCollection.getCount() == 0
                            && xmlSchemaComplexType.isAbstract()) {
                        findExtendtion(xmlSchemaComplexType, parameter, 3);
                    }
                } else if (type instanceof XmlSchemaSimpleType) {
                    // Will TO DO if need.
                    // System.out.println("XmlSchemaSimpleType");

                }
                break;
            }

        }
    }

    private void buileParameterFromTypes2(String paraType, ParameterInfo parameter, int ioOrRecursion) {
        if (ioOrRecursion < 3) {
            parametersName.clear();
            parametersName.add(parameter.getName());
        } else if (ioOrRecursion == 3) {
            parametersName.add(parameter.getName());
        }
        for (int i = 0; i < allXmlSchemaType.size(); i++) {
            XmlSchemaType type = allXmlSchemaType.get(i);
            String typeName = type.getName();
            if (paraType.equals(typeName)) {
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType xmlSchemaComplexType = (XmlSchemaComplexType) type;
                    XmlSchemaParticle xmlSchemaParticle = xmlSchemaComplexType.getParticle();
                    XmlSchemaObjectCollection xmlSchemaObjectCollection = null;
                    if (xmlSchemaParticle == null && xmlSchemaComplexType.getContentModel() != null) {
                        Object obj = xmlSchemaComplexType.getContentModel().getContent();
                        if (obj instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension xscce = (XmlSchemaComplexContentExtension) obj;
                            if (xscce != null) {
                                xmlSchemaParticle = xscce.getParticle();
                            }
                            if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                                xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                            }

                        } else if (obj instanceof XmlSchemaComplexContentRestriction) {
                            XmlSchemaComplexContentRestriction xsccr = (XmlSchemaComplexContentRestriction) obj;
                            xmlSchemaObjectCollection = xsccr.getAttributes();
                        }

                    } else if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                        XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                        xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                    }
                    if (xmlSchemaObjectCollection != null) {
                        buildParameterFromCollection2(xmlSchemaObjectCollection, parameter, 3);
                    }
                } else if (type instanceof XmlSchemaSimpleType) {
                    // Will TO DO if need.
                    // System.out.println("XmlSchemaSimpleType");

                }
                break;
            }

        }
    }

    private void buildParameterFromCollection(XmlSchemaObjectCollection xmlSchemaObjectCollection, ParameterInfo parameter,
            int ioOrRecursion) {

        // XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) xmlSchemaParticle;
        // XmlSchemaObjectCollection xmlSchemaObjectCollection = xmlSchemaSequence.getItems();
        int count = xmlSchemaObjectCollection.getCount();
        for (int j = 0; j < count; j++) {
            XmlSchemaObject xmlSchemaObject = xmlSchemaObjectCollection.getItem(j);
            if (xmlSchemaObject instanceof XmlSchemaGroupBase) {
                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaObject;
                XmlSchemaObjectCollection items = xmlSchemaGroupBase.getItems();
                if (items != null) {
                    buildParameterFromCollection(items, parameter, ioOrRecursion);
                }
            } else if (xmlSchemaObject instanceof XmlSchemaAny) {
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName("_content_");
                parameterSon.setParent(parameter);
                parameter.getParameterInfos().add(parameterSon);

            } else if (xmlSchemaObject instanceof XmlSchemaElement) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) xmlSchemaObject;
                String elementName = xmlSchemaElement.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName(elementName);
                parameterSon.setParent(parameter);
                if (((XmlSchemaElement) xmlSchemaObject).getQName() != null) {
                    parameterSon.setNameSpace(((XmlSchemaElement) xmlSchemaObject).getQName().getNamespaceURI());
                }
                Long min = xmlSchemaElement.getMinOccurs();
                Long max = xmlSchemaElement.getMaxOccurs();
                if (max - min > 1) {
                    parameterSon.setArraySize(-1);
                    parameterSon.setIndex("*");
                }
                parameter.getParameterInfos().add(parameterSon);

                Boolean isHave = false;
                if (!parametersName.isEmpty() && parameterSon.getName() != null) {
                    for (int p = 0; p < parametersName.size(); p++) {
                        if (parameterSon.getName().equals(parametersName.get(p))) {
                            isHave = true;
                        }
                    }
                }
                // if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                // XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                // if (xmlElementComplexType.isAbstract()) {
                // findExtendtion(xmlSchemaElement.getSchemaType(), parameterSon, ioOrRecursion);
                // }
                // }
                if (xmlSchemaElement.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                    String elementTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                    if (elementTypeName != null && elementTypeName.equals("anyType")) {
                        parameterSon.setName(xmlSchemaElement.getName() + ":anyType");
                    }
                    parameterSon.setType(elementTypeName);
                    parameterSon.setNameSpace(xmlSchemaElement.getQName().getNamespaceURI());
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes(elementTypeNamespace, elementTypeName, parameterSon, ioOrRecursion);
                    }
                } else if (xmlSchemaElement.getSchemaType() != null) {
                    if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                        XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                        XmlSchemaParticle xmlSchemaParticle = xmlElementComplexType.getParticle();
                        if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                            XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                            XmlSchemaObjectCollection childCollection = xmlSchemaGroupBase.getItems();
                            if (childCollection != null && !isHave) {
                                buildParameterFromCollection(childCollection, parameterSon, ioOrRecursion);
                            }
                        } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                            String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                            String paraTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                            if (paraTypeName != null && !isHave) {
                                parameter.setType(paraTypeName);
                                buileParameterFromTypes(paraTypeNamespace, paraTypeName, parameterSon, ioOrRecursion);
                            }
                        }
                    } else if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaSimpleType) {
                        XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaElement.getSchemaType();
                        String typeName = xmlSchemaSimpleType.getName();
                        if (typeName != null && typeName.equals("anyType")) {
                            ParameterInfo pSon = new ParameterInfo();
                            pSon.setName("anyType");
                            pSon.setParent(parameter);
                            parameter.getParameterInfos().add(pSon);
                        }
                        parameter.setType(typeName);

                    }

                } else if (xmlSchemaElement.getRefName() != null) {
                    String elementTypeName = xmlSchemaElement.getRefName().getLocalPart();
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buildParameterFromElements(elementTypeName, parameterSon, ioOrRecursion);
                    }
                }

            } else if (xmlSchemaObject instanceof XmlSchemaAttribute) {
                XmlSchemaAttribute xmlSchemaAttribute = (XmlSchemaAttribute) xmlSchemaObject;
                String elementName = xmlSchemaAttribute.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName(elementName);
                parameterSon.setParent(parameter);

                parameter.getParameterInfos().add(parameterSon);
                Boolean isHave = false;
                if (!parametersName.isEmpty() && parameterSon.getName() != null) {
                    for (int p = 0; p < parametersName.size(); p++) {
                        if (parameterSon.getName().equals(parametersName.get(p))) {
                            isHave = true;
                        }
                    }

                }
                if (xmlSchemaAttribute.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaAttribute.getSchemaTypeName().getLocalPart();
                    String elementTypeNamespace = xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI();
                    parameterSon.setType(elementTypeName);
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes(elementTypeNamespace, elementTypeName, parameterSon, ioOrRecursion);
                    }
                } else if (xmlSchemaAttribute.getRefName() != null) {
                    String refName = xmlSchemaAttribute.getRefName().getLocalPart();
                    parameterSon.setType(refName);
                    if (!isHave) {
                        buildParameterFromElements(refName, parameterSon, ioOrRecursion);

                    }
                }
            }
        }

    }

    private void buildParameterFromCollection2(XmlSchemaObjectCollection xmlSchemaObjectCollection, ParameterInfo parameter,
            int ioOrRecursion) {

        // XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) xmlSchemaParticle;
        // XmlSchemaObjectCollection xmlSchemaObjectCollection = xmlSchemaSequence.getItems();
        int count = xmlSchemaObjectCollection.getCount();
        for (int j = 0; j < count; j++) {
            XmlSchemaObject xmlSchemaObject = xmlSchemaObjectCollection.getItem(j);
            if (xmlSchemaObject instanceof XmlSchemaGroupBase) {
                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaObject;
                XmlSchemaObjectCollection items = xmlSchemaGroupBase.getItems();
                if (items != null) {
                    buildParameterFromCollection(items, parameter, ioOrRecursion);
                }
            } else if (xmlSchemaObject instanceof XmlSchemaAny) {
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName("_content_");
                parameterSon.setParent(parameter);
                parameter.getParameterInfos().add(parameterSon);

            } else if (xmlSchemaObject instanceof XmlSchemaElement) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) xmlSchemaObject;
                String elementName = xmlSchemaElement.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName(elementName);
                parameterSon.setParent(parameter);
                Long min = xmlSchemaElement.getMinOccurs();
                Long max = xmlSchemaElement.getMaxOccurs();
                if (max - min > 1) {
                    parameterSon.setArraySize(-1);
                    parameterSon.setIndex("*");
                }
                parameter.getParameterInfos().add(parameterSon);

                Boolean isHave = false;
                if (!parametersName.isEmpty() && parameterSon.getName() != null) {
                    for (int p = 0; p < parametersName.size(); p++) {
                        if (parameterSon.getName().equals(parametersName.get(p))) {
                            isHave = true;
                        }
                    }
                }
                // if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                // XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                // if (xmlElementComplexType.isAbstract()) {
                // findExtendtion(xmlSchemaElement.getSchemaType(), parameterSon, ioOrRecursion);
                // }
                // }
                if (xmlSchemaElement.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                    parameterSon.setType(elementTypeName);
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes2(elementTypeName, parameterSon, ioOrRecursion);
                    }

                } else if (xmlSchemaElement.getSchemaType() != null) {
                    if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                        XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                        XmlSchemaParticle xmlSchemaParticle = xmlElementComplexType.getParticle();
                        if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                            XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                            XmlSchemaObjectCollection childCollection = xmlSchemaGroupBase.getItems();
                            if (childCollection != null && !isHave) {
                                buildParameterFromCollection(childCollection, parameterSon, ioOrRecursion);
                            }
                        } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                            String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                            String paraTypeNamespace = xmlSchemaElement.getSchemaTypeName().getNamespaceURI();
                            if (paraTypeName != null && !isHave) {
                                parameter.setType(paraTypeName);
                                buileParameterFromTypes(paraTypeNamespace, paraTypeName, parameterSon, ioOrRecursion);
                            }
                        }
                    } else if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaSimpleType) {
                        XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaElement.getSchemaType();
                        String typeName = xmlSchemaSimpleType.getName();
                        parameter.setType(typeName);

                    }

                } else if (xmlSchemaElement.getRefName() != null) {
                    String elementTypeName = xmlSchemaElement.getRefName().getLocalPart();
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buildParameterFromElements(elementTypeName, parameterSon, ioOrRecursion);
                    }
                }

            } else if (xmlSchemaObject instanceof XmlSchemaAttribute) {
                XmlSchemaAttribute xmlSchemaAttribute = (XmlSchemaAttribute) xmlSchemaObject;
                String elementName = xmlSchemaAttribute.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName(elementName);
                parameterSon.setParent(parameter);

                parameter.getParameterInfos().add(parameterSon);
                Boolean isHave = false;
                if (!parametersName.isEmpty() && parameterSon.getName() != null) {
                    for (int p = 0; p < parametersName.size(); p++) {
                        if (parameterSon.getName().equals(parametersName.get(p))) {
                            isHave = true;
                        }
                    }

                }
                if (xmlSchemaAttribute.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaAttribute.getSchemaTypeName().getLocalPart();
                    String elementTypeNamespace = xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI();
                    parameterSon.setType(elementTypeName);
                    if (!isHave && !WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes(elementTypeNamespace, elementTypeName, parameterSon, ioOrRecursion);
                    }
                } else if (xmlSchemaAttribute.getRefName() != null) {
                    String refName = xmlSchemaAttribute.getRefName().getLocalPart();
                    parameterSon.setType(refName);
                    if (!isHave) {
                        buildParameterFromElements(refName, parameterSon, ioOrRecursion);

                    }
                }
            }
        }

    }

    private void findExtendtion(XmlSchemaType xmlSchemaType, ParameterInfo parameterSon, int ioOrRecursion) {
        for (int i = 0; i < allXmlSchemaType.size(); i++) {
            XmlSchemaType type = allXmlSchemaType.get(i);
            if (type != null) {
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) type;
                    if (xmlElementComplexType.getContentModel() != null) {
                        Object obj = xmlElementComplexType.getContentModel().getContent();
                        if (obj instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension xscce = (XmlSchemaComplexContentExtension) obj;
                            if (xscce.getBaseTypeName().getLocalPart() != null
                                    && xmlSchemaType.getName().equals(xscce.getBaseTypeName().getLocalPart())) {
                                if (xmlElementComplexType.isAbstract()) {
                                    findExtendtion(type, parameterSon, ioOrRecursion);
                                    break;
                                } else {
                                    if (!alldExtendtion.contains(type.getName())) {
                                        alldExtendtion.add(type.getName());
                                        ParameterInfo parameterType = new ParameterInfo();
                                        parameterType.setName(parameterSon.getName() + ".@type");
                                        boolean flag = false;
                                        for (int x = 0; x < parameterSon.getParameterInfos().size(); x++) {
                                            ParameterInfo info = parameterSon.getParameterInfos().get(x);
                                            if (info.getName().equals(parameterType.getName())) {
                                                flag = true;
                                            }
                                        }
                                        if (!flag) {
                                            parameterType.setParent(parameterSon);
                                            parameterSon.getParameterInfos().add(parameterType);
                                        }

                                        ParameterInfo parameterSubSon = new ParameterInfo();
                                        parameterSubSon.setName(type.getName());
                                        parameterSubSon.setParent(parameterSon);
                                        parameterSon.getParameterInfos().add(parameterSubSon);
                                        buileParameterFromTypes2(type.getName(), parameterSubSon, ioOrRecursion);
                                        parametersName.clear();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
