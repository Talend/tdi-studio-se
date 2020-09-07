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
package org.talend.repository.preference.palettesettings;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.ui.component.preference.provider.IPaletteItem;

/**
 * created by nrousseau on Aug 11, 2014 Detailled comment
 *
 */
public class ComponentPaletteItem extends AbstractPaletteItem implements IPaletteItem {

    IComponent component;

    String family;

    public ComponentPaletteItem(IComponent component, String origFamily) {
        this.component = component;
        setLabel(component.getDisplayName());
        setImageDesc(component.getIcon16());
        setCategory(ComponentCategory.getComponentCategoryFromName(component.getPaletteType()));
        this.family = origFamily;
    }

    /**
     * Getter for component.
     *
     * @return the component
     */
    public IComponent getComponent() {
        return this.component;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.palettesettings.AbstractPaletteItem#getFamily()
     */
    @Override
    public String getFamily() {
        return this.family;
    }
}
