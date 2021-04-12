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
package org.talend.designer.xmlmap.figures.treesettings;

import org.eclipse.draw2d.ToolbarLayout;
import org.talend.designer.gefabstractmap.figures.treesettings.AbstractTreeSettingContainer;
import org.talend.designer.xmlmap.figures.table.OutputTreeSettingTable;
import org.talend.designer.xmlmap.figures.table.XmlMapTableManager;

/**
 * wchen class global comment. Detailled comment
 */
public class OutputTreeSettingContainer extends AbstractTreeSettingContainer {

    private XmlMapTableManager tableModelManager;

    private OutputTreeSettingTable tableFigure;

    public OutputTreeSettingContainer(XmlMapTableManager tableModelManager) {
        this.tableModelManager = tableModelManager;
        createContent();
    }

    public void createContent() {
        setLayoutManager(new ToolbarLayout());
        // table
        tableFigure = new OutputTreeSettingTable(tableModelManager);
        this.add(tableFigure);
    }

    @Override
    public void update(int type) {
        tableFigure.update(type);
    }

    @Override
    public void deselectTreeSettingRows() {
        tableFigure.deselectTreeSettingRows();
    }
}
