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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;


/**
 * Handles computed metadata in the model.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: MetadataManager.java,v 1.1 2008/10/07 21:27:31 mdutoo Exp $
 */
public class MetadataManager {

	private AlfrescoOutputModelManager modelManager;

	// computed alfresco metadatas :
	private Map<String,Element> propertyMap = new HashMap<String,Element>();
	private Map<String,Element> associationMap = new HashMap<String,Element>();
	private List<String> missingTypeNames = new ArrayList<String>();
	private List<String> missingAspectNames = new ArrayList<String>();

	public MetadataManager(AlfrescoOutputModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public Map<String,Element> getPropertyMap() {
		return propertyMap;
	}
	public Map<String,Element> getAssociationMap() {
		return associationMap;
	}

	public List<String> getMissingTypeNames() {
		return missingTypeNames;
	}
	public List<String> getMissingAspectNames() {
		return missingAspectNames;
	}

	/**
	 * Updates computed metadata according to the current model.
	 */
    public void updateMetadata() {
    	// TODO replace availableXxx by map of elements of discovered models ? otherwise have to add explicitly auditable, titled...
    	this.propertyMap.clear();
    	this.associationMap.clear();
    	this.missingTypeNames.clear();
    	this.missingAspectNames.clear();

    	// handling supertype metadatas :
    	if (modelManager.getType() != null) {
    		this.fillMetadatas(modelManager.getType(), missingTypeNames, missingAspectNames);
    	}
    	for (Element aspect : modelManager.getAspects().getOrderedAlfrescoModelElements()) {
        	this.fillMetadatas(aspect, missingTypeNames, missingAspectNames);
    	}
    }

    /**
     * Fills the given maps with the typing metadatas found in the given alfresco metamodel element.
     * NB. avoids system metadata : base and referenceable,
     * noderef pseudo prop (equivalent for sys:referenceable props) must be handled elsewhere.
     * @param alfrescoModelElement
     * @param metadataMap
     * @param missingTypeNames
     * @param missingAspectNames
     */
    protected void fillMetadatas(Element alfrescoModelElement,
    		List<String> missingTypeNames, List<String> missingAspectNames) {
    	String parentName = alfrescoModelElement.elementText("parent"); //$NON-NLS-1$
    	if (parentName != null
    			&& !parentName.equals("sys:base")) { // avoiding system metadata : base and referenceable //$NON-NLS-1$
    		boolean isType = "type".equals(alfrescoModelElement.getName()); //$NON-NLS-1$
    		Element parent = ((isType) ? modelManager.getAvailableTypes() : modelManager.getAvailableAspects())
    			.getAlfrescoModelElementMap().get(parentName);
    		if (parent != null) {
            	this.fillMetadatas(parent, missingTypeNames, missingAspectNames);
    		} else { // TODO not if sys:base ?
    			((isType) ? missingTypeNames : missingAspectNames).add(parentName);
    		}
    	}

    	Element mandatoryAspectsElt = alfrescoModelElement.element("mandatory-aspects"); //$NON-NLS-1$
    	if (mandatoryAspectsElt != null) {
	    	for (Element aspectElt : (List<Element>) mandatoryAspectsElt.elements("aspect")) { //$NON-NLS-1$
	    		String aspectName = aspectElt.getText();
	    		Element aspect = modelManager.getAvailableAspects().getAlfrescoModelElementMap().get(aspectName);
	    		if (aspect != null) {
	            	this.fillMetadatas(aspect, missingTypeNames, missingAspectNames);
	    		} else {
	    			missingAspectNames.add(aspectName);
	    		}
	    	}
    	}

		// properties :
    	Element propertiesElt = alfrescoModelElement.element("properties"); //$NON-NLS-1$
    	if (propertiesElt != null) {
    		for (Element property : (List<Element>) propertiesElt.elements()) {
    			if (!"true".equals(property.elementText("protected"))) { //$NON-NLS-1$ //$NON-NLS-2$
    				propertyMap.put(property.attributeValue("name"), property); //$NON-NLS-1$
    			} // else filtering out protected
    		}
    	}

    	// associations and child-associations :
    	Element associationsElt = alfrescoModelElement.element("associations"); //$NON-NLS-1$
    	if (associationsElt != null) {
    		for (Element association : (List<Element>) associationsElt.elements()) {
    			associationMap.put(association.attributeValue("name"), association); //$NON-NLS-1$
    		}
    	}

    	// NB. child-associations can only be set to target an existing element.
    	// to create a new element with a target association, targetLocation must be used.
    }

}
