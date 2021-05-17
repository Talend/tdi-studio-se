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
package org.talend.designer.core.ui.editor.palette;

import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.talend.designer.core.IPaletteFilter;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory;

/**
 *
 */
public class TalendPaletteDrawer extends PaletteDrawer implements IPaletteFilter {

    private boolean filtered;

    private String originalName;

    /**
     * Getter for filtered.
     *
     * @return the filtered
     */
    @Override
    public boolean isFiltered() {
        String label = this.getLabel();
        if (TalendEditorPaletteFactory.FAVORITES.equals(label) || TalendEditorPaletteFactory.RECENTLY_USED.equals(label)) {
            return false;
        }
        return this.filtered;
    }

    /**
     * Sets the filtered.
     *
     * @param filtered the filtered to set
     */
    @Override
    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
        PaletteContainer parentContainer = getParent();
        if (parentContainer instanceof IPaletteFilter) {
            ((IPaletteFilter) parentContainer).setFiltered(filtered);
        }
    }

    public TalendPaletteDrawer(String label) {
        super(label);
        filtered = true;
    }

    @Override
    public boolean acceptsType(Object type) {
        return true;
    }

    @Override
    public String getOriginalName() {
        return this.originalName;
    }

    @Override
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

}
