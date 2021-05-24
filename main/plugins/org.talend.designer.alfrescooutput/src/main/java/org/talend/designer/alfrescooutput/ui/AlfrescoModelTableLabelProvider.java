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
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


/**
 * Table label provider for alfrsco models.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: AlfrescoModelTableLabelProvider.java,v 1.1 2008/10/07 21:27:31 mdutoo Exp $
 */
public class AlfrescoModelTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element == null) {
			return ""; //$NON-NLS-1$
		}
	    Element alfrescoModelElement = (Element) element;
	    switch (columnIndex) {
        case 0:
        	return alfrescoModelElement.elementText("title"); //$NON-NLS-1$
        case 1 :
        	return alfrescoModelElement.attributeValue("name"); //$NON-NLS-1$
        default :
        	return ""; //$NON-NLS-1$
        }
	}

	public void dispose() {

	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void addListener(ILabelProviderListener listener) {

	}

	public void removeListener(ILabelProviderListener listener) {

	}

}
