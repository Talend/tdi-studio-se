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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.talend.designer.alfrescooutput.ui.AlfrescoModelElementsChangeListener;

/**
 * Model of XML Elements representing Alfresco typing concepts
 * for the UI's various providers. Handles an ordered map of
 * alfresco model XML elements (properties or associations).
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: AlfrescoModelElements.java,v 1.1 2008/10/07 21:27:31 mdutoo Exp $
 */
public class AlfrescoModelElements {

	private Map<String,Element> alfrescoModelElementMap = new HashMap<String,Element>();
	private List<Element> orderedAlfrescoModelElements = new ArrayList<Element>();

	private Set<AlfrescoModelElementsChangeListener> listeners = new HashSet<AlfrescoModelElementsChangeListener>();

	public AlfrescoModelElements() {

	}


	/**
	 * Returns the map of all alfresco model elements.
	 * @return
	 */
	public Map<String,Element> getAlfrescoModelElementMap() {
		return alfrescoModelElementMap;
	}
	/**
	 * Provides the order by returning a list of all alfresco model elements.
	 * @return
	 */
	public List<Element> getOrderedAlfrescoModelElements() {
		return orderedAlfrescoModelElements;
	}

	public void addAlfrescoModelElement(Element alfrescoModelElement) {
		alfrescoModelElementMap.put(alfrescoModelElement.attributeValue("name"), alfrescoModelElement); //$NON-NLS-1$
		orderedAlfrescoModelElements.add(alfrescoModelElement);
		for (AlfrescoModelElementsChangeListener listener : listeners) {
			listener.alfrescoModelElementAdded(alfrescoModelElement);
		}
	}

	public void removeAlfrescoModelElement(Element alfrescoModelElement) {
		Element existing = alfrescoModelElementMap.remove(alfrescoModelElement.attributeValue("name")); //$NON-NLS-1$
		if (existing != null) {
			orderedAlfrescoModelElements.remove(existing);
		}
		for (AlfrescoModelElementsChangeListener listener : listeners) {
			listener.alfrescoModelElementRemoved(alfrescoModelElement);
		}
	}

	public void addAllAlfrescoModelElement(Collection<Element> alfrescoModelElements) {
		for (Element alfrescoModelElement : alfrescoModelElements) {
			this.addAlfrescoModelElement(alfrescoModelElement);
		}
	}

	public void removeAllAlfrescoModelElement(Collection<Element> alfrescoModelElements) {
		for (Element alfrescoModelElement : alfrescoModelElements) {
			this.removeAlfrescoModelElement(alfrescoModelElement);
		}
	}

	public void clear() {
		this.removeAllAlfrescoModelElement(new ArrayList<Element>(this.orderedAlfrescoModelElements));
	}


	public void addListener(AlfrescoModelElementsChangeListener listener) {
		listeners.add(listener);
	}

	public void removeListener(AlfrescoModelElementsChangeListener listener) {
		listeners.remove(listener);
	}

}
