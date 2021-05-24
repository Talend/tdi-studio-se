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
package org.talend.designer.core.ui.editor.properties.controllers.uidialog.tns;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 *
 * rshi class global comment. Detailled comment
 */
public class TnsContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object parentElement) {
        return ((TnsNodeModel) parentElement).getChildren().toArray();
    }

    public Object getParent(Object element) {
        return null;
    }

    public boolean hasChildren(Object element) {
        return ((TnsNodeModel) element).hasChildren();
    }

    public Object[] getElements(Object inputElement) {
        return ((TnsNodeModel) inputElement).getChildren().toArray();
    }

    public void dispose() {

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

}
