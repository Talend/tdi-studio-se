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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteEditPartFactory;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory;
import org.talend.themes.core.elements.stylesettings.TalendPaletteCSSStyleSetting;

/**
 *
 */
public class TalendPaletteEditPartFactory extends PaletteEditPartFactory {

    protected TalendPaletteCSSStyleSetting cssStyleSetting;

    public TalendPaletteEditPartFactory(TalendPaletteCSSStyleSetting cssStyleSetting) {
        this.cssStyleSetting = cssStyleSetting;
    }

    @Override
    protected EditPart createMainPaletteEditPart(EditPart parentEditPart, Object model) {
        return new TalendSliderPaletteEditPart((PaletteRoot) model, cssStyleSetting);
    }

    @Override
    protected EditPart createDrawerEditPart(EditPart parentEditPart, Object model) {
        PaletteDrawer paletteDrawer = (PaletteDrawer) model;
        TalendDrawerEditPart drawerEditPart = new TalendDrawerEditPart(paletteDrawer, cssStyleSetting);
        if (parentEditPart instanceof TalendSliderPaletteEditPart) {
            String label = paletteDrawer.getLabel();
            if (TalendEditorPaletteFactory.FAVORITES.equals(label)) {
                ((TalendPaletteViewer) parentEditPart.getViewer()).setFavoritesEditPart(drawerEditPart);
            } else if (TalendEditorPaletteFactory.RECENTLY_USED.equals(label)) {
                ((TalendPaletteViewer) parentEditPart.getViewer()).setRecentlyUsedEditPart(drawerEditPart);
            }
        }
        return drawerEditPart;
    }

    @Override
    public EditPart createEntryEditPart(EditPart parentEditPart, Object model) {
        return new TalendEntryEditPart((PaletteEntry) model, cssStyleSetting);
    }

    @Override
    protected EditPart createGroupEditPart(EditPart parentEditPart, Object model) {
        return new TalendGroupEditPart((PaletteContainer) model);
    }
}
