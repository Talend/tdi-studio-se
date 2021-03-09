// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.alfrescooutput.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.talend.designer.alfrescooutput.AlfrescoOutputComponent;
import org.talend.designer.alfrescooutput.i18n.Messages;
import org.talend.designer.alfrescooutput.util.AlfrescoOutputException;

/**
 * Manages the model, including load and save.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: AlfrescoOutputModelManager.java,v 1.2 2008/10/10 10:05:54 mdutoo Exp $
 */
public class AlfrescoOutputModelManager {

    // alfresco typing
    public static final String PARAM_AVAILABLE_MODELS = "AVAILABLE_ALFRESCO_MODELS"; //$NON-NLS-1$

    public static final String PARAM_AVAILABLE_NAMESPACES = "AVAILABLE_ALFRESCO_NAMESPACES"; //$NON-NLS-1$

    public static final String PARAM_ITEM_FILEPATH = "FILEPATH"; //$NON-NLS-1$

    public static final String PARAM_ITEM_PREFIX = "PREFIX"; //$NON-NLS-1$

    public static final String PARAM_ITEM_URI = "URI"; //$NON-NLS-1$

    public static final String PARAM_TYPE_NAME = "ALFRESCO_TYPE_NAME"; //$NON-NLS-1$

    public static final String PARAM_ASPECT_NAMES = "ALFRESCO_ASPECT_NAMES"; //$NON-NLS-1$

    public static final String PARAM_ITEM_NAME = "NAME"; //$NON-NLS-1$

    // property mapping and additional items
    public static final String PARAM_PROPERTY_MAPPING = "PROPERTY_MAPPING"; //$NON-NLS-1$

    public static final String PARAM_ITEM_TITLE = "TITLE"; //$NON-NLS-1$

    public static final String PARAM_ITEM_TYPE = "TYPE"; //$NON-NLS-1$

    public static final String PARAM_ITEM_MANDATORY = "MANDATORY"; //$NON-NLS-1$

    public static final String PARAM_ITEM_VALUE = "VALUE"; //$NON-NLS-1$

    public static final String PARAM_ITEM_COLUMN = "COLUMN"; //$NON-NLS-1$

    public static final String PARAM_ITEM_DEFAULT = "DEFAULT"; //$NON-NLS-1$

    // association mapping and additional items
    public static final String PARAM_ASSOCIATION_MAPPING = "ASSOCIATION_MAPPING"; //$NON-NLS-1$

    public static final String PARAM_ITEM_CHILD = "CHILD"; //$NON-NLS-1$

    public static final String PARAM_ITEM_MANY = "MANY"; //$NON-NLS-1$

    // association mapping and additional items
    private AlfrescoOutputComponent alfrescoOutputComponent;

    // model :
    private Element type = null;

    private AlfrescoModelElements availableTypes = new AlfrescoModelElements();

    private final AlfrescoModelElements aspects = new AlfrescoModelElements();

    private final AlfrescoModelElements availableAspects = new AlfrescoModelElements();

    private final ArrayList<String> availableModels = new ArrayList<String>();

    private final HashMap<String, String> availablePrefixToNamespaceMap = new HashMap<String, String>(3);

    // computed alfresco metadatas :
    private MetadataManager metadataManager = new MetadataManager(this);

    public AlfrescoOutputModelManager(AlfrescoOutputComponent alfrescoOutputComponent) {
        this.alfrescoOutputComponent = alfrescoOutputComponent;
    }

    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    public Element getType() {
        return type;
    }

    public HashMap<String, String> getAvailablePrefixToNamespaceMap() {
        return availablePrefixToNamespaceMap;
    }

    public ArrayList<String> getAvailableModels() {
        return availableModels;
    }

    public AlfrescoModelElements getAvailableTypes() {
        return availableTypes;
    }

    public AlfrescoModelElements getAspects() {
        return aspects;
    }

    public AlfrescoModelElements getAvailableAspects() {
        return availableAspects;
    }

    public void setType(Element type) {
        this.type = type;
    }

    /**
     * Adds an aspect definition to the model
     *
     * @param alfrescoModelElement
     */
    public void addAspect(Element alfrescoModelElement) {
        this.aspects.addAlfrescoModelElement(alfrescoModelElement);
        this.availableAspects.removeAlfrescoModelElement(alfrescoModelElement);
    }

    /**
     * Removes an aspect definition from the model
     *
     * @param alfrescoModelElement
     */
    public void removeAspect(Element alfrescoModelElement) {
        this.availableAspects.addAlfrescoModelElement(alfrescoModelElement);
        this.aspects.removeAlfrescoModelElement(alfrescoModelElement);
    }

    /**
     * Adds an Alfresco model definition file.
     *
     * @param newModelFilePath
     * @throws AlfrescoOutputException if already added or error reading it
     */
    public void addModel(String newModelFilePath) throws AlfrescoOutputException {
        if (this.availableModels.contains(newModelFilePath)) {
            throw new AlfrescoOutputException(Messages.getString("AlfrescoOutputModelManager.alreadyAdded")); //$NON-NLS-1$
        }

        this.availableModels.add(newModelFilePath);

        // parsing the model
        org.dom4j.Document modelDoc = null;
        try {
            modelDoc = new SAXReader().read(new File(newModelFilePath));
        } catch (DocumentException dex) {
            throw new AlfrescoOutputException(Messages.getString("AlfrescoOutputModelManager.errorReadingModel") + " " //$NON-NLS-1$ //$NON-NLS-2$
                    + newModelFilePath, dex);
        }

        Element modelElt = modelDoc.getRootElement();

        Element namespacesElt = modelElt.element("namespaces"); //$NON-NLS-1$
        if (namespacesElt != null) {
            List<Element> namespaces = (List<Element>) namespacesElt.elements("namespace"); //$NON-NLS-1$
            HashMap<String, String> availablePrefixToNamespaceMapTmp = new HashMap<String, String>(3);
            for (Element namespace : namespaces) {
                String namespacePrefix = namespace.attributeValue("prefix"); //$NON-NLS-1$
                if (this.availablePrefixToNamespaceMap.containsKey(namespacePrefix)) {
                    throw new AlfrescoOutputException(Messages.getString("AlfrescoOutputModelManager.prefixConflict") + " " //$NON-NLS-1$ //$NON-NLS-2$
                            + namespacePrefix + " " + this.availablePrefixToNamespaceMap.get(namespacePrefix)); //$NON-NLS-1$
                }
                String namespaceUri = namespace.attributeValue("uri"); //$NON-NLS-1$
                availablePrefixToNamespaceMapTmp.put(namespacePrefix, namespaceUri);
            }
            this.availablePrefixToNamespaceMap.putAll(availablePrefixToNamespaceMapTmp);
        }

        Element typesElt = modelElt.element("types"); //$NON-NLS-1$
        if (typesElt != null) {
            List<Element> types = (List<Element>) typesElt.elements("type"); //$NON-NLS-1$
            this.availableTypes.addAllAlfrescoModelElement(types);
        }

        Element aspectsElt = modelElt.element("aspects"); //$NON-NLS-1$
        if (aspectsElt != null) {
            List<Element> aspects = (List<Element>) aspectsElt.elements("aspect"); //$NON-NLS-1$
            this.availableAspects.addAllAlfrescoModelElement(aspects);
        }
    }

    /**
     * Removes an Alfresco model definition file.
     *
     * @param oldModelFilePath
     * @throws AlfrescoOutputException
     */
    public void removeModel(String oldModelFilePath) throws AlfrescoOutputException {
        if (!this.availableModels.remove(oldModelFilePath)) {
            throw new AlfrescoOutputException(Messages.getString("AlfrescoOutputModelManager.notYetAdded")); //$NON-NLS-1$
        }

        // parsing the model
        org.dom4j.Document modelDoc = null;
        try {
            modelDoc = new SAXReader().read(new File(oldModelFilePath));
        } catch (DocumentException dex) {
            throw new AlfrescoOutputException(Messages.getString("AlfrescoOutputModelManager.errorReadingModel") + " " //$NON-NLS-1$ //$NON-NLS-2$
                    + oldModelFilePath, dex);
        }

        Element modelElt = modelDoc.getRootElement();

        Element namespacesElt = modelElt.element("namespaces"); //$NON-NLS-1$
        if (namespacesElt != null) {
            List<Element> namespaces = (List<Element>) namespacesElt.elements("namespace"); //$NON-NLS-1$
            for (Element namespace : namespaces) {
                String namespacePrefix = namespace.attributeValue("prefix"); //$NON-NLS-1$
                this.availablePrefixToNamespaceMap.remove(namespacePrefix);
            }
        }

        Element typesElt = modelElt.element("types"); //$NON-NLS-1$
        if (typesElt != null) {
            List<Element> types = (List<Element>) typesElt.elements("type"); //$NON-NLS-1$
            this.availableTypes.removeAllAlfrescoModelElement(types);
        }

        Element aspectsElt = modelElt.element("aspects"); //$NON-NLS-1$
        if (aspectsElt != null) {
            List<Element> aspects = (List<Element>) aspectsElt.elements("aspect"); //$NON-NLS-1$
            this.availableAspects.removeAllAlfrescoModelElement(aspects);
        }
    }

    /**
     * Clears the model
     */
    public void clear() {
        type = null; // ?
        availableTypes.clear();
        aspects.clear();
        availableAspects.clear();
        availableModels.clear();
    }

    /**
     * Loads the model from the component
     *
     * @throws AlfrescoOutputException if error loading specified models
     */
    public void load() throws AlfrescoOutputException {
        this.clear();

        // loading available models :
        List<Map<String, String>> availableModelsTable = (List<Map<String, String>>) alfrescoOutputComponent.getElementParameter(
                PARAM_AVAILABLE_MODELS).getValue(); // TABLE
        if (availableModelsTable != null) {
            for (Map<String, String> availableModelRow : availableModelsTable) {
                String availableModel = availableModelRow.get(PARAM_ITEM_FILEPATH);
                if (availableModel != null) {
                    this.addModel(availableModel); // enriches availableTypes and availableAspects
                }
            }
        }

        // loading available namespaces :
        List<Map<String, String>> availableNamespacesTable = (List<Map<String, String>>) alfrescoOutputComponent
                .getElementParameter(PARAM_AVAILABLE_NAMESPACES).getValue(); // TABLE
        if (availableNamespacesTable != null) {
            for (Map<String, String> availableNamespaceRow : availableNamespacesTable) {
                String namespacePrefix = availableNamespaceRow.get(PARAM_ITEM_PREFIX);
                String namespaceUri = availableNamespaceRow.get(PARAM_ITEM_URI);
                if (namespacePrefix != null) {
                    this.availablePrefixToNamespaceMap.put(namespacePrefix, namespaceUri);
                }
            }
        }

        // loading chosen type :
        String typeName = (String) alfrescoOutputComponent.getElementParameter(PARAM_TYPE_NAME).getValue(); // TEXT
        if (typeName != null && typeName.length() == 0) {
            typeName = null;
        }
        this.setType(this.availableTypes.getAlfrescoModelElementMap().get(typeName));

        // loading chosen aspects :
        List<Map<String, String>> aspectNamesTable = (List<Map<String, String>>) alfrescoOutputComponent.getElementParameter(
                PARAM_ASPECT_NAMES).getValue();
        if (aspectNamesTable != null) {
            for (Map<String, String> aspectNameRow : aspectNamesTable) {
                String aspectName = aspectNameRow.get(PARAM_ITEM_NAME);
                Element aspect = this.availableAspects.getAlfrescoModelElementMap().get(aspectName);
                if (aspect != null) {
                    this.addAspect(aspect);
                }
            }
        }

        // update (compute) metadatas
        this.metadataManager.updateMetadata();
    }

    /**
     * Saves the model to the component
     */
    public void save() {
        // available models :
        List<Map<String, String>> availableModelsTable = (List<Map<String, String>>) alfrescoOutputComponent.getElementParameter(
                PARAM_AVAILABLE_MODELS).getValue(); // TABLE
        availableModelsTable.clear();
        for (String availableModel : availableModels) {
            HashMap<String, String> availableModelRow = new HashMap<String, String>(1);
            availableModelRow.put(PARAM_ITEM_FILEPATH, availableModel);
            availableModelsTable.add(availableModelRow);
        }
        alfrescoOutputComponent.getElementParameter(PARAM_AVAILABLE_MODELS).setValue(availableModelsTable);

        // available namespaces :
        List<Map<String, String>> availableNamespacesTable = (List<Map<String, String>>) alfrescoOutputComponent
                .getElementParameter(PARAM_AVAILABLE_NAMESPACES).getValue(); // TABLE
        availableNamespacesTable.clear();
        for (String namespacePrefix : this.availablePrefixToNamespaceMap.keySet()) {
            String namespaceUri = this.availablePrefixToNamespaceMap.get(namespacePrefix);
            HashMap<String, String> availableNamespaceRow = new HashMap<String, String>(2);
            availableNamespaceRow.put(PARAM_ITEM_PREFIX, namespacePrefix);
            availableNamespaceRow.put(PARAM_ITEM_URI, namespaceUri);
            availableNamespacesTable.add(availableNamespaceRow);
        }
        alfrescoOutputComponent.getElementParameter(PARAM_AVAILABLE_NAMESPACES).setValue(availableNamespacesTable);

        // chosen type :
        String typeName = (type == null) ? "" : type.attributeValue("name"); //$NON-NLS-1$ //$NON-NLS-2$
        alfrescoOutputComponent.getElementParameter(PARAM_TYPE_NAME).setValue(typeName); // TEXT

        // chosen aspects :
        List<Map<String, String>> aspectNamesTable = (List<Map<String, String>>) alfrescoOutputComponent.getElementParameter(
                PARAM_ASPECT_NAMES).getValue(); // TABLE
        aspectNamesTable.clear();
        for (Element aspect : aspects.getOrderedAlfrescoModelElements()) {
            HashMap<String, String> aspectRow = new HashMap<String, String>(1);
            String aspectName = aspect.attributeValue("name"); //$NON-NLS-1$
            aspectRow.put(PARAM_ITEM_NAME, aspectName);
            aspectNamesTable.add(aspectRow);
        }
        alfrescoOutputComponent.getElementParameter(PARAM_ASPECT_NAMES).setValue(aspectNamesTable);

        // let's save metadatas;
        this.saveMetadatas(PARAM_PROPERTY_MAPPING, this.metadataManager.getPropertyMap());
        this.saveMetadatas(PARAM_ASSOCIATION_MAPPING, this.metadataManager.getAssociationMap());

        alfrescoOutputComponent.getElementParameter("UPDATE_COMPONENTS").setValue(Boolean.TRUE); //$NON-NLS-1$
    }

    /**
     * Saves the given metadata to the given map
     *
     * @param metadataMappingParamName allows to switch between saving properties or elements
     */
    protected void saveMetadatas(String metadataMappingParamName, Map<String, Element> metadataMap) {
        List<Map<String, String>> metadataMappingTable = (List<Map<String, String>>) alfrescoOutputComponent.getElementParameter(
                metadataMappingParamName).getValue();
        Set<String> remainingMetadataNames = new HashSet<String>(metadataMap.keySet());

        // handling previously know metadata ; using a copy of their list so we're able do removes in it
        for (Map<String, String> metadataMappingRow : new ArrayList<Map<String, String>>(metadataMappingTable)) {
            String name = metadataMappingRow.get(PARAM_ITEM_NAME);

            // trying to find the corresponding existing metadata...
            Element metadata = metadataMap.get(name);
            if (metadata == null) {
                // has been removed ; let's remove it from the parameter
                metadataMappingTable.remove(metadataMappingRow);
                continue;
            }

            fillMetadataMappingRow(metadataMappingRow, metadata, metadataMappingParamName);
            remainingMetadataNames.remove(name);
        }

        // let's add the remaining metadata as new rows
        for (String name : remainingMetadataNames) {
            Map<String, String> metadataMappingRow = new HashMap<String, String>();
            Element metadata = metadataMap.get(name); // it exists
            fillMetadataMappingRow(metadataMappingRow, metadata, metadataMappingParamName);
            metadataMappingTable.add(metadataMappingRow);
        }
    }

    /**
     * Fills a (parameter) metadata row with metadata info
     */
    private void fillMetadataMappingRow(Map<String, String> metadataMappingRow, Element metadata, String metadataMappingParamName) {
        if (PARAM_PROPERTY_MAPPING.equals(metadataMappingParamName)) {
            // case properties :
            fillPropertyMappingRow(metadataMappingRow, metadata);

        } else if (PARAM_ASSOCIATION_MAPPING.equals(metadataMappingParamName)) {
            // case associations :
            fillAssociationMappingRow(metadataMappingRow, metadata);
        }
    }

    /**
     * Fills a (parameter) property metadata row with property info
     */
    protected void fillPropertyMappingRow(Map<String, String> metadataMappingRow, Element metadata) {
        // NB. reputting PARAM_ITEM_NAME is not required if the metadata with
        // such a name already existed, but not a problem either
        metadataMappingRow.put(PARAM_ITEM_NAME, metadata.attributeValue("name")); //$NON-NLS-1$
        metadataMappingRow.put(PARAM_ITEM_TITLE, metadata.elementText("title")); //$NON-NLS-1$
        metadataMappingRow.put(PARAM_ITEM_TYPE, metadata.elementText("type")); //$NON-NLS-1$
        metadataMappingRow.put(PARAM_ITEM_MANDATORY, metadata.elementText("mandatory")); //$NON-NLS-1$
        metadataMappingRow.put(PARAM_ITEM_DEFAULT, metadata.elementText("default")); //$NON-NLS-1$
        // NB. no default for VALUE
    }

    /**
     * Fills a (parameter) association metadata row with association info
     */
    protected void fillAssociationMappingRow(Map<String, String> metadataMappingRow, Element metadata) {
        // NB. reputting PARAM_ITEM_NAME is not required if the metadata with
        // such a name already existed, but not a problem either
        metadataMappingRow.put(PARAM_ITEM_NAME, metadata.attributeValue("name")); //$NON-NLS-1$
        metadataMappingRow.put(PARAM_ITEM_CHILD, String.valueOf("child-association".equals(metadata.getName()))); //$NON-NLS-1$
        metadataMappingRow.put(PARAM_ITEM_TITLE, metadata.elementText("title")); //$NON-NLS-1$
        Element associationTarget = metadata.element("target"); //$NON-NLS-1$
        if (associationTarget != null) {
            metadataMappingRow.put(PARAM_ITEM_TYPE, associationTarget.elementText("class")); //$NON-NLS-1$
            metadataMappingRow.put(PARAM_ITEM_MANDATORY, associationTarget.elementText("mandatory")); //$NON-NLS-1$
            metadataMappingRow.put(PARAM_ITEM_MANY, associationTarget.elementText("many")); //$NON-NLS-1$
        }
    }

}
