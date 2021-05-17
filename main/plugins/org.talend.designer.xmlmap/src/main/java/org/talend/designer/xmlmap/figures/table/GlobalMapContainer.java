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
package org.talend.designer.xmlmap.figures.table;

import org.eclipse.draw2d.ToolbarLayout;
import org.talend.designer.gefabstractmap.figures.table.AbstractGlobalMapContainer;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class GlobalMapContainer extends AbstractGlobalMapContainer {

    private XmlMapTableManager tableModelManager;

    private GlobalMapKeysTable tableFigure;

    public GlobalMapContainer(XmlMapTableManager tableModelManager) {
        this.tableModelManager = tableModelManager;
        createContent();
    }

    public void createContent() {
        setLayoutManager(new ToolbarLayout());
        tableFigure = new GlobalMapKeysTable(tableModelManager);
        this.add(tableFigure);
    }

    public GlobalMapKeysTable getTableFigure() {
        return this.tableFigure;
    }
}
