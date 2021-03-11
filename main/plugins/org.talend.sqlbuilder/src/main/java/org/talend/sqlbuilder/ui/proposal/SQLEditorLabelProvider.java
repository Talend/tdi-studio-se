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
package org.talend.sqlbuilder.ui.proposal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: SQLEditorLabelProvider.java,v 1.3 2006/11/06 08:28:00 qiang.zhang Exp $
 *
 */
public class SQLEditorLabelProvider implements ILabelProvider {

    private ImageDescriptor sqlEditor = ImageUtil.getDescriptor("Images.SqlEditorIcon"); //$NON-NLS-1$

    /**
     * DOC dev SQLEditorLabelProvider constructor comment.
     */
    public SQLEditorLabelProvider() {
        super();
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        Image i = null;
        if (element instanceof SQLEditorAllProposal) {
            i = ((SQLEditorAllProposal) element).getImage();
            if (i != null) {
                return i;
            }
        }
        return ImageProvider.getImage(sqlEditor);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof SQLEditorAllProposal) {
            return ((SQLEditorAllProposal) element).getLabel();
        }
        return ""; //$NON-NLS-1$
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
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
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
