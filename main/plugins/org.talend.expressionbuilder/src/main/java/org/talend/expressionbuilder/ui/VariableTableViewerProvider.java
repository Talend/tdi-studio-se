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
package org.talend.expressionbuilder.ui;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.runtime.model.expressionbuilder.Variable;

/**
 * Content and label provider for table viewer on expression builder dialog.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: VariableTableViewerProvider.java 下午03:49:15 2007-6-25 +0000 (2007-6-25) yzhang $
 *
 */
public class VariableTableViewerProvider implements IStructuredContentProvider, ITableLabelProvider {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        List<Variable> list = (List<Variable>) inputElement;
        return list.size() > 0 ? list.toArray() : new Object[] {};
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }

    /* ########## methods implemented from label provider interface ####### */

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        Variable var = (Variable) element;
        if (columnIndex == 0) {
            return var.getName();
        } else if (columnIndex == 1) {
            return var.getValue();
        }
        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

}
