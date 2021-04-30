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
package org.talend.designer.core.generic.model;

import java.util.List;

import org.eclipse.gef.palette.PaletteEntry;
import org.talend.core.model.process.IGenericProvider;
import org.talend.designer.core.generic.palette.ComponentsPaletteFactory;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.repository.ProjectManager;

/**
 * created by hcyi on Sep 11, 2015 Detailled comment
 *
 */
// TUP-4152
public class ComponentsProvider implements IGenericProvider {

    @Override
    public void loadComponentsFromExtensionPoint() {
        if (ProjectManager.getInstance().getCurrentProject() != null) {
            ComponentsUtils.initComponents();
        }
    }

    @Override
    public List<PaletteEntry> addPaletteEntry() {
        return ComponentsPaletteFactory.createPalette();
    }
}
