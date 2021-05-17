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
package org.talend.designer.alfrescooutput.ui;

import org.dom4j.Element;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.talend.designer.alfrescooutput.data.AlfrescoModelElements;


/**
 * Content provider for Alfresco models.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: AlfrescoModelContentProvider.java,v 1.1 2008/10/07 21:27:31 mdutoo Exp $
 *
 */
public class AlfrescoModelContentProvider
		implements IStructuredContentProvider, AlfrescoModelElementsChangeListener {

	private AlfrescoModelElements alfrescoModelElements = null;

	public AlfrescoModelContentProvider() {

	}


	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (alfrescoModelElements != null) {
			alfrescoModelElements.removeListener(this);
		}
		if (newInput instanceof AlfrescoModelElements) {
			this.alfrescoModelElements = (AlfrescoModelElements) newInput;
			this.alfrescoModelElements.addListener(this);
		} else {
			alfrescoModelElements = null;
		}
	}
	public void dispose() {
		if (this.alfrescoModelElements != null) {
			this.alfrescoModelElements.removeListener(this);
		}
	}

	public Object[] getElements(Object inputElement) {
		if (alfrescoModelElements == null) {
			return null;
		}
		return alfrescoModelElements.getOrderedAlfrescoModelElements().toArray();
	}

	/**
	 * To override
	 */
	public void alfrescoModelElementAdded(Element alfrescoModelElement) {

	}

	/**
	 * To override
	 */
	public void alfrescoModelElementRemoved(Element alfrescoModelElement) {

	}

}
