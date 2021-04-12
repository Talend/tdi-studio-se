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
package org.talend.designer.core.generic.palette;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory;

/**
 *
 * created by hcyi on Sep 14, 2015 Detailled comment
 *
 */
public final class ComponentsPaletteFactory {

    private static PaletteRoot paletteRoot;

    public static List<PaletteEntry> createPalette() {
        List<PaletteEntry> list = null;
        paletteRoot = ComponentPaletteUtilities.getPaletteRoot();
        Object defaultEntry = paletteRoot.getChildren().get(0);
        if (defaultEntry instanceof PaletteGroup) {
            list = createLinkNodes((PaletteGroup) defaultEntry);
        }
        return list;
    }

    public static PaletteRoot createPalette(IComponentsFactory compFac, PaletteRoot root) {
        ComponentsUtils.loadComponents(ComponentsUtils.getComponentService());
        paletteRoot = TalendEditorPaletteFactory.createPalette(compFac);
        Object defaultEntry = paletteRoot.getChildren().get(0);
        if (defaultEntry instanceof PaletteGroup) {
            createLinkNodes((PaletteGroup) defaultEntry);
        }
        return paletteRoot;
    }

    private static List<PaletteEntry> createLinkNodes(PaletteGroup toolGroup) {
        ArrayList<PaletteEntry> arrayList = new ArrayList<PaletteEntry>();
        // to do
        return arrayList;
    }
}
