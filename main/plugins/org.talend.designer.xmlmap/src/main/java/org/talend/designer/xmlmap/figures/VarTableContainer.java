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
package org.talend.designer.xmlmap.figures;

import org.eclipse.draw2d.Figure;
import org.talend.designer.gefabstractmap.figures.table.AbstractTable;
import org.talend.designer.gefabstractmap.figures.table.AbstractTableContainer;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarContainer;
import org.talend.designer.xmlmap.figures.table.VarTable;
import org.talend.designer.xmlmap.figures.table.VarTableManager;
import org.talend.designer.xmlmap.figures.treetools.VarToolBarFigure;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class VarTableContainer extends AbstractTableContainer {

    private VarToolBarFigure toolBarConainer;

    public VarTableContainer(VarTableManager tableManager) {
        super(tableManager, false);
        createContents();
    }

    @Override
    public VarTableManager getTableManager() {
        return (VarTableManager) super.getTableManager();
    }

    @Override
    protected ToolBarContainer createToolBarContainer() {
        if (toolBarConainer == null) {
            toolBarConainer = new VarToolBarFigure(getTableManager());
        }
        return toolBarConainer;
    }

    @Override
    protected AbstractTable createTable() {
        if (table == null) {
            table = new VarTable(getTableManager());
        }
        return table;
    }

    @Override
    protected String getTreeDisplayName() {
        return "Var";
    }

    @Override
    protected void createTreeSettings(Figure parent) {

    }

    public Figure getHeader() {
        return this.header;
    }

    public Figure getTableItemContainer() {
        return table.getTableItemContainer();
    }

    public VarToolBarFigure getToolBarFigure() {
        return this.toolBarConainer;
    }

}
