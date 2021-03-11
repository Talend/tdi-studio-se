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
package org.talend.designer.core.ui.editor.update;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.talend.core.model.update.UpdatesConstants;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateViewerSorter extends ViewerSorter {

    public UpdateViewerSorter() {
        //
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        String name1 = null;
        String name2 = null;
        if (e1 instanceof Job && e2 instanceof Job) {
            name1 = ((Job) e1).getName();
            name2 = ((Job) e2).getName();
        } else if (e1 instanceof Category && e2 instanceof Category) {
            name1 = ((Category) e1).getName();
            name2 = ((Category) e2).getName();
        } else if (e1 instanceof Item && e2 instanceof Item) {
            name1 = ((Item) e1).getProperty();
            name2 = ((Item) e2).getProperty();
        }
        if (name1 == null) {
            name1 = UpdatesConstants.EMPTY;
        }
        if (name2 == null) {
            name2 = UpdatesConstants.EMPTY;
        }
        return getComparator().compare(name1, name2);
    }

}
