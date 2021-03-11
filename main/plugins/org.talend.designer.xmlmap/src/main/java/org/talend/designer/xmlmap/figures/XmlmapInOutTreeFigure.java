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
import org.talend.designer.gefabstractmap.figures.treesettings.FilterContainer;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarContainer;
import org.talend.designer.xmlmap.figures.table.XmlMapTableManager;
import org.talend.designer.xmlmap.figures.table.XmlMapTableTree;
import org.talend.designer.xmlmap.figures.treetools.TreeToolBarContainer;

/**
 * DOC talend class global comment. Detailled comment
 */
public abstract class XmlmapInOutTreeFigure extends AbstractTableContainer {

    public XmlmapInOutTreeFigure(XmlMapTableManager tableModelManager) {
        super(tableModelManager);
    }

    @Override
    public FilterContainer getFilterContainer() {
        return this.filterFigure;
    }

    @Override
    protected AbstractTable createTable() {
        if (table == null) {
            table = new XmlMapTableTree(getTableManager());
        }
        return table;
    }

    public XmlMapTableManager getTableManager() {
        return (XmlMapTableManager) super.getTableManager();
    }

    @Override
    protected abstract String getTreeDisplayName();

    @Override
    protected abstract void createTreeSettings(Figure parent);

    @Override
    public AbstractTable getTableTree() {
        return this.table;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.table.AbstractTableContainer#createToolBarContainer()
     */
    @Override
    protected ToolBarContainer createToolBarContainer() {
        return new TreeToolBarContainer(getTableManager());
    }
}
