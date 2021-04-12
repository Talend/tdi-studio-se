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
package org.talend.designer.webservice.ui.tree;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WebServiceTreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ParameterInfo) {
            return ((ParameterInfo) inputElement).getParameterInfos().toArray();
        }
        return null;
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */

    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ParameterInfo) {
            return ((ParameterInfo) parentElement).getParameterInfos().toArray();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */

    public Object getParent(Object element) {
        if (element != null && element instanceof ParameterInfo) {
            return ((ParameterInfo) element).getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */

    public boolean hasChildren(Object element) {
        if (element != null) {
            if (element instanceof ParameterInfo) {
                return ((ParameterInfo) element).getParameterInfos().size() > 0;
            }
        }
        return false;
    }

}
