// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.composite.provider;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.talend.componentdesigner.ImageLib;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.model.ILibEntry;

/**
 * Content provider that maintains a list of classpath entries which are shown in a tree viewer.
 */
public class LibListProvider extends LabelProvider implements IStructuredContentProvider {

    public LibListProvider() {
        // fTab = tab;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        Image image = null;
        if (element instanceof ILibEntry) {
            switch (((ILibEntry) element).getType()) {
            case ILibEntry.JAR:
                image = ImageLib.getImage(ImageLib.JAR_OBJ);
                break;
            case ILibEntry.PM:
                image = ImageLib.getImage(ImageLib.PM_OBJ);
                break;
            default:
            }
        }
        return image;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        String text = null;
        if (element instanceof ILibEntry) {
            text = ((ILibEntry) element).getNameAndPath(PluginConstant.JOINT_MARK);
        }
        return text;
    }

    /**
     * @see IStructuredContentProvider#getElements(Object)
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public Object[] getElements(Object inputElement) {
        if (inputElement != null && inputElement instanceof List) {
            List entryList = (List) inputElement;
            return entryList.toArray();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {

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
}
