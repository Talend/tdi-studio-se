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

import org.apache.log4j.Logger;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.swt.widgets.Composite;
import org.talend.themes.core.elements.stylesettings.TalendPaletteCSSStyleSetting;

/**
 *
 */
public class TalendPaletteViewerProvider extends PaletteViewerProvider {

    private static Logger log = Logger.getLogger(TalendPaletteViewerProvider.class);

    protected TalendPaletteViewer talendPaletteViewer;

    protected TalendPaletteCSSStyleSetting cssStyleSetting;

    public TalendPaletteViewerProvider(EditDomain graphicalViewerDomain) {
        super(graphicalViewerDomain);
        cssStyleSetting = new TalendPaletteCSSStyleSetting();
    }

    @Override
    public PaletteViewer createPaletteViewer(Composite parent) {
        // removed by 10304
        // if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC) {
        // // PTDO need check it later and fix the bug on MacOS.
        // return super.createPaletteViewer(parent);
        // }
        talendPaletteViewer = new TalendPaletteViewer(this.getEditDomain(), cssStyleSetting);

        FigureCanvas canvas = new TalendFigureCanvas(parent, talendPaletteViewer.getLightweightSys(), talendPaletteViewer);
        talendPaletteViewer.setFigureCanvas(canvas);

        configurePaletteViewer(talendPaletteViewer);
        hookPaletteViewer(talendPaletteViewer);
        return talendPaletteViewer;
    }

    /**
     * log the exception and throw a Runtime exception cause this is serious.
     *
     * @param iae
     */
    private static void handleReflectionFailure(Exception e) {
        // our hook is not working so say it
        log.error("Draw2D Canvas hook failed", e);
        throw new RuntimeException(e);

    }

    @Override
    protected void configurePaletteViewer(PaletteViewer pViewer) {
        pViewer.setContextMenu(new TalendPaletteContextMenuProvider(pViewer));
        pViewer.addDragSourceListener(new TalendPaletteDragSourceListener(pViewer));
    }

    public TalendPaletteCSSStyleSetting getCSSStyleSetting() {
        return cssStyleSetting;
    }

    public TalendPaletteViewer getTalendPaletteViewer() {
        return this.talendPaletteViewer;
    }
}
