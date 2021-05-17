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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.internal.ui.palette.editparts.SliderPaletteEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.editparts.PaletteAnimator;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.gef.ui.palette.editparts.PaletteToolbarLayout;
import org.talend.themes.core.elements.stylesettings.TalendPaletteCSSStyleSetting;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TalendSliderPaletteEditPart extends SliderPaletteEditPart {

    private PaletteAnimator controller;

    protected TalendPaletteCSSStyleSetting cssStyleSetting;

    public TalendSliderPaletteEditPart(PaletteRoot paletteRoot, TalendPaletteCSSStyleSetting cssStyleSetting) {
        super(paletteRoot);
        this.cssStyleSetting = cssStyleSetting;
    }

    @Override
    public IFigure createFigure() {
        Figure figure = new Figure();
        figure.setOpaque(true);
        // figure.setForegroundColor(ColorConstants.listForeground);
        // figure.setBackgroundColor(ColorConstants.listBackground);
        figure.setForegroundColor(cssStyleSetting.getSliderPaletteForgroundColor());
        figure.setBackgroundColor(cssStyleSetting.getSliderPaletteBackgroundColor());
        return figure;
    }

    /**
     * This method overrides super's functionality to do nothing.
     *
     * @see PaletteEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
    }

    /**
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#registerVisuals()
     */
    @Override
    protected void registerVisuals() {
        super.registerVisuals();
        controller = new PaletteAnimator(((PaletteViewer) getViewer()).getPaletteViewerPreferences());
        getViewer().getEditPartRegistry().put(PaletteAnimator.class, controller);
        ToolbarLayout layout = new PaletteToolbarLayout();
        getFigure().setLayoutManager(layout);
        getFigure().addLayoutListener(controller);
    }

}
