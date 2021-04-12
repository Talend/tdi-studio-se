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
package org.talend.designer.core.ui.editor;

import java.util.Iterator;
import java.util.List;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsHandler;

/**
 * DOC marvin class global comment. Detailled comment
 */
public class ProcessComponentsHandler implements IComponentsHandler {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.TComponentsHandler#filterComponents(java.util.List)
     */
    @Override
    public List<IComponent> filterComponents(List<IComponent> allComponents) {
        if (allComponents != null && allComponents.size() > 0) {
            Iterator<IComponent> componentsIterator = allComponents.iterator();
            while (componentsIterator.hasNext()) {
                IComponent component = componentsIterator.next();
                String compType = component.getPaletteType();
                if (compType != null && !ComponentCategory.CATEGORY_4_DI.getName().equals(compType)) {
                    componentsIterator.remove();
                }
            }
        }
        return allComponents;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.TComponentsHandler#sortComponents(java.util.List)
     */
    @Override
    public List<IComponent> sortComponents(List<IComponent> filteredComponents) {
        return filteredComponents;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsHandler#extractComponentsCategory()
     */
    @Override
    public ComponentCategory extractComponentsCategory() {
        return ComponentCategory.CATEGORY_4_DI;
    }

}
