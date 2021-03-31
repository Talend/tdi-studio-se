// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.routine;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarItem;

/**
 * ggu class global comment. Detailled comment
 */
public class ShowRoutineItemsViewerFilter extends ViewerFilter {

    private Map<Project, List<Property>> allItems;

    public ShowRoutineItemsViewerFilter(Map<Project, List<Property>> allItems) {
        super();
        this.allItems = allItems;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof Property && ((Property) element).getItem() instanceof RoutineItem
                && ((RoutineItem) ((Property) element).getItem()).isBuiltIn() == false) {
            return true;
        }
        if (element instanceof Property && ((Property) element).getItem() instanceof RoutinesJarItem) {
            return true;
        }
        if (element instanceof Project) {
            List<Property> list = allItems.get(element);
            boolean has = false;
            if (list != null) {
                for (Property p : list) {
                    if (select(viewer, parentElement, p)) {
                        has = true;
                        break;
                    }
                }
            }
            return has;
        }
        return false;
    }

}
