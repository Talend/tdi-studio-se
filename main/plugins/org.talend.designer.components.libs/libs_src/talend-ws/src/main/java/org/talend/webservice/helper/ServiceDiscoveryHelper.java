/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.talend.webservice.helper.conf.ServiceHelperConfiguration;
import org.talend.webservice.helper.conf.WSDLLocatorImpl;
import org.w3c.dom.Element;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.extensions.schema.SchemaConstants;
import com.ibm.wsdl.util.xml.DOMUtils;
import com.ibm.wsdl.util.xml.QNameUtils;

/**
 * This helper allow easy discovery of services and types
 * 
 * @author rlamarche
 */
public class ServiceDiscoveryHelper {

    private String wsdlUri;

    private WSDLFactory wsdlFactory;

    private ServiceHelperConfiguration configuration;

    private File wsdlTmpDir;

    private Map<String, Definition> definitions;

    private XmlSchemaCollection schemaCollection;

    private Set<String> namespaces;

    private final String LOCAL_WSDL_NAME = "mainWSDL.wsdl";

    public ServiceDiscoveryHelper(String wsdlUri) throws WSDLException, IOException, TransformerException, URISyntaxException {
        this(wsdlUri, null, null);
    }

    public ServiceDiscoveryHelper(String wsdlUri, String tempPath) throws WSDLException, IOException, TransformerException,
            URISyntaxException {
        this(wsdlUri, null, tempPath);
    }

    public ServiceDiscoveryHelper(String wsdlUri, ServiceHelperConfiguration configuration) throws WSDLException, IOException,
            TransformerException, URISyntaxException {
        this(wsdlUri, configuration, null);
    }

    public ServiceDiscoveryHelper(String wsdlUri, ServiceHelperConfiguration configuration, String tempPath)
            throws WSDLException, IOException, TransformerException, URISyntaxException {
        this.wsdlUri = wsdlUri;
        this.configuration = configuration;
        this.wsdlTmpDir = createTempWsdlDir(tempPath);
        init();
    }

    private File createTempWsdlDir(String tempPath) {
        File tmpWsdlDir;
        if (tempPath != null && !"".equals(tempPath)) {
            tmpWsdlDir = new File(tempPath, "wsdl" + String.valueOf(new Date().getTime()) + Thread.currentThread().getId());
        } else {
            tmpWsdlDir = new File(System.getProperty("java.io.tmpdir"), "wsdl" + String.valueOf(new Date().getTime()) + Thread.currentThread().getId());
        }
        if (!tmpWsdlDir.mkdir()) {
            throw new SecurityException("Unable to create temporary directory," + tmpWsdlDir.getAbsolutePath());
        }
        tmpWsdlDir.deleteOnExit();
        return tmpWsdlDir;
    }

    /**
     * Read the wsdl and schema
     * 
     * @throws javax.wsdl.WSDLException
     * @throws TransformerException
     * @throws URISyntaxException
     */
    private void init() throws WSDLException, IOException, TransformerException, URISyntaxException {
        wsdlFactory = WSDLFactory.newInstance();
        WSDLReader newWSDLReader = wsdlFactory.newWSDLReader();
        newWSDLReader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        Definition definition;

        URI wsdlURI = new URI(wsdlUri);

        if (configuration != null && wsdlURI.getScheme().startsWith("http")) {
            definition = newWSDLReader.readWSDL(configuration.createWSDLLocator(wsdlUri));
        } else {
            definition = newWSDLReader.readWSDL(wsdlUri);
        }

        Map<String, Definition> defs = new HashMap<String, Definition>();
        defs.put(LOCAL_WSDL_NAME, definition);
        List<String> importKeys = new LinkedList<String>();
        String parentLoc = absoluteLocation(null, wsdlUri);
        importKeys.add(definition.getTargetNamespace() + " " + parentLoc);
        definitions = findWsdlImport(definition, parentLoc, defs, importKeys);

        schemaCollection = getSchemaCollection(definitions);

        namespaces = collectNamespaces();

        generateTempWsdlFile();

    }

    private void generateTempWsdlFile() throws FileNotFoundException, WSDLException, TransformerException, URISyntaxException {
        generateImportAndIncludeXsd(definitions);

        for (Object key : definitions.keySet()) {
            File importFile = new File(wsdlTmpDir, key.toString());
            importFile.deleteOnExit();
            wsdlFactory.newWSDLWriter().writeWSDL(definitions.get(key), new FileOutputStream(importFile));
        }

    }

    @SuppressWarnings("unchecked")
    private void generateImportAndIncludeXsd(Map<String, Definition> definitions) throws FileNotFoundException,
            TransformerException, URISyntaxException {
        Map<String, Types> typesList = getTypesFromWsdl(definitions);
        Map<String, String> schemaIDs = null;

        for (Object key : typesList.keySet()) {
            Types types = typesList.get(key);
            if (types != null) {
                List<ExtensibilityElement> extensibilityElements = types.getExtensibilityElements();
                for (ExtensibilityElement el : extensibilityElements) {
                    if (el instanceof Schema) {
                        Schema schema = (Schema) el;
                        if (schemaIDs == null) {
                            schemaIDs = new HashMap<String, String>();
                            schemaIDs.put(schema.getDocumentBaseURI(), key.toString());
                        }
                        createTempImportSchemaFile(schema, schemaIDs);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createTempImportSchemaFile(Schema schema, Map<String, String> schemaIDs) throws FileNotFoundException,
            TransformerException, URISyntaxException {

        if (schema.getImports() != null) {
            Iterator<?> importSchemaIte = schema.getImports().values().iterator();
            while (importSchemaIte.hasNext()) {

                List<?> importSchemaList = (List<?>) importSchemaIte.next();
                for (Object importSchemaObj : importSchemaList) {

                    if (importSchemaObj instanceof SchemaImport) {

                        SchemaImport importSchema = (SchemaImport) importSchemaObj;
                        String schId = importSchema.getId();
                        String schNS = importSchema.getNamespaceURI();
                        String schLocUri = importSchema.getSchemaLocationURI();
                        Schema refSchema = importSchema.getReferencedSchema();

                        createTempImportSchemaFile(schema, schLocUri, schNS, schId, refSchema, schemaIDs);
                    }
                }
            }
        }

        if (schema.getIncludes() != null || schema.getRedefines() != null) {
            List<SchemaReference> includes = new ArrayList<SchemaReference>();
            if (schema.getIncludes() != null && !schema.getIncludes().isEmpty()) {
                includes.addAll(schema.getIncludes());
            }
            if (schema.getRedefines() != null && !schema.getRedefines().isEmpty()) {
                includes.addAll(schema.getRedefines());
            }

            for (SchemaReference include : includes) {

                Schema refSchema = include.getReferencedSchema();
                String schId = include.getId();
                String schLocUri = include.getSchemaLocationURI();

                createTempImportSchemaFile(schema, schLocUri, null, schId, refSchema, schemaIDs);

            }
        }

    }

    private void createTempImportSchemaFile(Schema schema, String schLocUri, String schNS, String schId, Schema refSchema,
            Map<String, String> schemaIDs) throws FileNotFoundException, TransformerException, URISyntaxException {
        if (schLocUri != null && refSchema != null) {
            if (!schemaIDs.containsKey(refSchema.getDocumentBaseURI())) {
                String xsdFileName = "schemaXSD" + schemaIDs.size() + ".xsd";
                schemaIDs.put(refSchema.getDocumentBaseURI(), xsdFileName);

                // lookup child import wsdl
                createTempImportSchemaFile(refSchema, schemaIDs);

                // create current wsdl
                createTempXSDFile(refSchema, xsdFileName);

                // modified the parent wsdl
                changeImportXsdLocation(schema, schId, schNS, schLocUri, xsdFileName);
            } else {
                changeImportXsdLocation(schema, schId, schNS, schLocUri, schemaIDs.get(refSchema.getDocumentBaseURI()));

                // only remove the duplicate XSD:IMPORT, for bug[http://jira.talendforge.org/browse/TDI-18573]
                removeImportXsdLocation(schema, schId, schNS, schemaIDs.get(refSchema.getDocumentBaseURI()));
            }
        }
    }

    private void createTempXSDFile(Schema sourceSchema, String xsdFileName) throws FileNotFoundException, TransformerException {
        Element e = sourceSchema.getElement();
        DOMSource domSource = new DOMSource(e);

        File xsdFile = new File(this.wsdlTmpDir, xsdFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(xsdFile);
        StreamResult streamResult = new StreamResult(fileOutputStream);

        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, streamResult);

    }

    private void removeImportXsdLocation(Schema schema, String id, String ns, String location) {
        Element schemaElem = schema.getElement();

        Element tempEl = DOMUtils.getFirstChildElement(schemaElem);
        for (; tempEl != null; tempEl = DOMUtils.getNextSiblingElement(tempEl)) {
            QName tempElType = QNameUtils.newQName(tempEl);
            if (SchemaConstants.XSD_IMPORT_QNAME_LIST.contains(tempElType)) {
                if ((DOMUtils.getAttribute(tempEl, SchemaConstants.ATTR_ID) == null ? id == null : DOMUtils.getAttribute(tempEl,
                        SchemaConstants.ATTR_ID).equals(id))
                        && (DOMUtils.getAttribute(tempEl, Constants.ATTR_NAMESPACE) == null ? ns == null : DOMUtils.getAttribute(
                                tempEl, Constants.ATTR_NAMESPACE).equals(ns))
                        && (DOMUtils.getAttribute(tempEl, SchemaConstants.ATTR_SCHEMA_LOCATION) == null ? location == null
                                : DOMUtils.getAttribute(tempEl, SchemaConstants.ATTR_SCHEMA_LOCATION).equals(location))) {
                    schemaElem.removeChild(tempEl);
                }
            }
        }
    }

    private void changeImportXsdLocation(Schema schema, String id, String ns, String location, String newXSDFileName) {
        Element schemaElem = schema.getElement();

        Element tempEl = DOMUtils.getFirstChildElement(schemaElem);

        for (; tempEl != null; tempEl = DOMUtils.getNextSiblingElement(tempEl)) {
            QName tempElType = QNameUtils.newQName(tempEl);
            if (SchemaConstants.XSD_IMPORT_QNAME_LIST.contains(tempElType)
                    || SchemaConstants.XSD_INCLUDE_QNAME_LIST.contains(tempElType)
                    || SchemaConstants.XSD_REDEFINE_QNAME_LIST.contains(tempElType)) {
                if ((DOMUtils.getAttribute(tempEl, SchemaConstants.ATTR_ID) == null ? id == null : DOMUtils.getAttribute(tempEl,
                        SchemaConstants.ATTR_ID).equals(id))
                        && (DOMUtils.getAttribute(tempEl, Constants.ATTR_NAMESPACE) == null ? ns == null : DOMUtils.getAttribute(
                                tempEl, Constants.ATTR_NAMESPACE).equals(ns))
                        && (DOMUtils.getAttribute(tempEl, SchemaConstants.ATTR_SCHEMA_LOCATION) == null ? location == null
                                : DOMUtils.getAttribute(tempEl, SchemaConstants.ATTR_SCHEMA_LOCATION).equals(location))) {
                    tempEl.getAttributeNode(SchemaConstants.ATTR_SCHEMA_LOCATION).setValue(newXSDFileName);
                }
            }
        }
    }

    private Map<String, Definition> findWsdlImport(Definition definition, String parentLocation,
    		Map<String, Definition> definitions, List<String> importKeys) {

    	if (definition.getImports() != null && !definition.getImports().isEmpty()) {

            Map<?, ?> imports = definition.getImports();
            List<Import> importsToRemove = new LinkedList<Import>();
            for (Map.Entry<?, ?> entry : imports.entrySet()) {

                String namespace = (String) entry.getKey();
                Vector<?> importsVector = (Vector<?>) entry.getValue();
                for (Object importObj : importsVector) {
                    Import importDecl = (Import) importObj;
                    String importLoc = absoluteLocation(parentLocation, importDecl.getLocationURI());
                    String importKey = namespace + " " + importLoc;
                    if (importKeys.contains(importKey)) {
                        importsToRemove.add(importDecl);
                    } else {
                        importKeys.add(importKey);

                        String importWsdlFileName = "importWsdl" + definitions.size() + ".wsdl";
                        Definition importDef = importDecl.getDefinition();

                        if (importDef != null) {
                            definitions.put(importWsdlFileName, importDef);
                            findWsdlImport(importDef, importLoc, definitions, importKeys);
                        }
                        importDecl.setLocationURI(importWsdlFileName);
                    }
                }
            }
            if (!importsToRemove.isEmpty()) {
                for (Import importToRemove : importsToRemove) {
                    definition.removeImport(importToRemove);
                }
            }
        }

        return definitions;
    }

    @SuppressWarnings("unchecked")
    private XmlSchemaCollection getSchemaCollection(Map<String, Definition> definitions) throws FileNotFoundException,
            TransformerException, URISyntaxException {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        Map<String, Types> typesList = getTypesFromWsdl(definitions);

        int tmpCount = 0;
        String tmpTNName = "";

        for (Object key : typesList.keySet()) {
            Types types = typesList.get(key);
            if (types != null) {
                List<ExtensibilityElement> extensibilityElements = types.getExtensibilityElements();
                for (ExtensibilityElement el : extensibilityElements) {
                    if (el instanceof Schema) {
                        Schema schema = (Schema) el;
                        // for bug 8674
                        // set base uri for relative path in schemaLocation.
                        schemaCollection.setBaseUri(schema.getDocumentBaseURI());

                        // synthetic URI for the schemas without targetNamespace,avoid conflict error.

                        if (schema.getElement().getAttributeNode("targetNamespace") == null) {
                            tmpTNName = schema.getDocumentBaseURI() + "#type" + tmpCount;
                            schemaCollection.read(schema.getElement(), tmpTNName);
                            tmpCount++;
                        } else {
                            schemaCollection.read(schema.getElement());
                        }
                    }
                }
            }
        }
        return schemaCollection;

    }

    private Map<String, Types> getTypesFromWsdl(Map<String, Definition> importDefinitions) {
        Map<String, Types> typesList = new HashMap<String, Types>();

        for (Object key : importDefinitions.keySet()) {
            Definition importDef = importDefinitions.get(key);
            if (importDef != null) {
                if (importDef.getTypes() != null) {
                    typesList.put(key.toString(), importDef.getTypes());
                }
            }
        }
        return typesList;
    }

    @SuppressWarnings("unchecked")
    private Set<String> collectNamespaces() {
        Set<String> namespaces = new HashSet<String>();

        for (Object key : definitions.keySet()) {
            Set<Map.Entry<String, String>> entrySet = definitions.get(key).getNamespaces().entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                if (namespaces.contains(entry.getValue())) {
                    continue;
                }
                namespaces.add(entry.getValue());
            }
        }

        // some targetnamespace in schema missing in definitions,code for bug 9900 (for bare webservice)
        if (schemaCollection != null) {
            org.apache.ws.commons.schema.XmlSchema[] xs = schemaCollection.getXmlSchemas();
            if (xs != null) {
                for (org.apache.ws.commons.schema.XmlSchema x : xs) {
                    if (namespaces.contains(x.getTargetNamespace()) || x.getTargetNamespace() == null) {
                        continue;
                    }
                    namespaces.add(x.getTargetNamespace());
                }
            }
        }

        return namespaces;
    }

    /**
     * Return the parsed wsdl, it contains all services
     * 
     * @return
     */
    public Definition getDefinition() {
        return definitions.get(this.LOCAL_WSDL_NAME);
    }

    /**
     * Return the xml schema collection
     * 
     * @return
     */
    public XmlSchemaCollection getSchema() {
        return schemaCollection;
    }

    public String getWsdlUri() {
        return wsdlUri;
    }

    public String getLocalWsdlUri() {
        return new File(wsdlTmpDir, this.LOCAL_WSDL_NAME).toURI().toString();
    }

    public Set<String> getNamespaces() {
        return namespaces;
    }

    private static String absoluteLocation(String parentLocation, String wsdlLocation) {
        try {
            return WSDLLocatorImpl.getURL(parentLocation, wsdlLocation).toExternalForm();
        } catch (MalformedURLException e) {
            return "NOLOCATION";
        }
    }
}
