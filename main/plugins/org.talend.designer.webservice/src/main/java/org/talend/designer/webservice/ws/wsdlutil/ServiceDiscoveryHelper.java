/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.designer.webservice.ws.wsdlutil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

/**
 * This helper allow easy discovery of services and types
 * 
 * @author rlamarche
 */
public class ServiceDiscoveryHelper {

    private String wsdlUri;

    private WSDLFactory wsdlFactory;

    private Definition definition;

    private List<Definition> definitions;

    private ServiceHelperConfiguration configuration;

    public ServiceDiscoveryHelper(String wsdlUri) throws WSDLException, IOException {
        this(wsdlUri, null);
    }

    public ServiceDiscoveryHelper(String wsdlUri, ServiceHelperConfiguration configuration) throws WSDLException, IOException {
        this.wsdlUri = wsdlUri;
        this.configuration = configuration;
        init();
    }

    /**
     * Read the wsdl and schema
     * 
     * @throws javax.wsdl.WSDLException
     */
    private void init() throws WSDLException, IOException {
        wsdlFactory = WSDLFactory.newInstance();
        // wsdlFactory.
        WSDLReader newWSDLReader = wsdlFactory.newWSDLReader();

        newWSDLReader.setExtensionRegistry(wsdlFactory.newPopulatedExtensionRegistry());
        // newWSDLReader.getExtensionRegistry()
        newWSDLReader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        if (configuration == null) {
            definition = newWSDLReader.readWSDL(wsdlUri);
        } else {
            definition = newWSDLReader.readWSDL(configuration.createWSDLLocator(wsdlUri));
        }
        List<Definition> defs = new LinkedList<Definition>();
        defs.add(definition);
        List<String> importKeys = new LinkedList<String>();
        String parentLoc = absoluteLocation(null, wsdlUri);
        importKeys.add(definition.getTargetNamespace() + " " + parentLoc);
        definitions = findWsdlImport(definition, parentLoc, defs, importKeys);
    }

    private List<Definition> findWsdlImport(Definition definition, String parentLocation,
            List<Definition> definitions, List<String> importKeys) {
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
                            definitions.add(importDef);
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

    /**
     * Return the parsed wsdl, it contains all services
     * 
     * @return
     */

    public List<Definition> getDefinitions() {
        return definitions;
    }

    private static String absoluteLocation(String parentLocation, String wsdlLocation) {
        try {
            return WSDLLocatorImpl.getURL(parentLocation, wsdlLocation).toExternalForm();
        } catch (MalformedURLException e) {
            return "NOLOCATION";
        }
    }
}
