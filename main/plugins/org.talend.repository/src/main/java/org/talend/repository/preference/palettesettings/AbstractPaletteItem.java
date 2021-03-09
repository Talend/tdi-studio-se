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
package org.talend.repository.preference.palettesettings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.ui.component.preference.provider.IPaletteItem;

/**
 * created by nrousseau on Aug 11, 2014 Detailled comment
 *
 */
public abstract class AbstractPaletteItem implements IPaletteItem {

    private ImageDescriptor imageDesc;

    private String label;

    private List<IPaletteItem> children = new ArrayList<IPaletteItem>();

    private ComponentCategory category;

    private IPaletteItem parent;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.palettesettings.IPaletteItem#getImageDesc()
     */
    @Override
    public ImageDescriptor getImageDesc() {
        return imageDesc;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.palettesettings.IPaletteItem#getLabel()
     */
    @Override
    public String getLabel() {
        return label;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.palettesettings.IPaletteItem#getChildren()
     */
    @Override
    public List<IPaletteItem> getChildren() {
        return children;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.palettesettings.IPaletteItem#getPaletteType()
     */
    @Override
    public ComponentCategory getPaletteType() {
        return category;
    }

    /**
     * Getter for category.
     *
     * @return the category
     */
    public ComponentCategory getCategory() {
        return this.category;
    }

    /**
     * Sets the category.
     *
     * @param category the category to set
     */
    public void setCategory(ComponentCategory category) {
        this.category = category;
    }

    /**
     * Sets the imageDesc.
     *
     * @param imageDesc the imageDesc to set
     */
    public void setImageDesc(ImageDescriptor imageDesc) {
        this.imageDesc = imageDesc;
    }

    /**
     * Sets the label.
     *
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.preference.provider.IPaletteItem#getFamily()
     */
    @Override
    public String getFamily() {
        return null;
    }

    /**
     * Getter for parent.
     *
     * @return the parent
     */
    @Override
    public IPaletteItem getParent() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.components.preference.provider.IPaletteItem#setParent(org.talend.designer.components.preference
     * .provider.IPaletteItem)
     */
    @Override
    public void setParent(IPaletteItem paletteItem) {
        this.parent = paletteItem;
    }

}
