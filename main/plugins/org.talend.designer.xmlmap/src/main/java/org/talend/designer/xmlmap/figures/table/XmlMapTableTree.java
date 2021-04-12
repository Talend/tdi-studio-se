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

import org.talend.designer.gefabstractmap.figures.table.AbstractTable;
import org.talend.designer.gefabstractmap.figures.table.ColumnKeyConstant;
import org.talend.designer.gefabstractmap.figures.table.ColumnSash;
import org.talend.designer.gefabstractmap.figures.table.TableColumn;
import org.talend.designer.gefabstractmap.model.abstractmap.MapperTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class XmlMapTableTree extends AbstractTable {

    private TableColumn expressionColumn;

    private TableColumn nameColumn;

    private ColumnSash columnSash;

    public XmlMapTableTree(XmlMapTableManager tableModelManager) {
        super(tableModelManager);
        createColumns();
    }

    @Override
    protected void createColumns() {
        layoutManager.setAjustToTableWidth(false);
        // table column title
        MapperTable xmlTree = tableModelManager.getModel();
        boolean hasExpression = false;
        if (xmlTree instanceof InputXmlTree) {
            InputXmlTree inputTree = (InputXmlTree) xmlTree;
            if (inputTree.isLookup()) {
                expressionColumn = new TableColumn(ColumnKeyConstant.KEY_EXPRESSION);
                expressionColumn.setText("Exp.key");
                this.addColumn(expressionColumn);
                columnSash = new ColumnSash(this);
                columnSash.setLeftColumn(expressionColumn);
                this.addSeparator(columnSash);
                hasExpression = true;
            }
        } else {
            expressionColumn = new TableColumn(ColumnKeyConstant.KEY_EXPRESSION);
            expressionColumn.setText("Expression");
            this.addColumn(expressionColumn);
            columnSash = new ColumnSash(this);
            columnSash.setLeftColumn(expressionColumn);
            this.addSeparator(columnSash);
            hasExpression = true;
        }

        nameColumn = new TableColumn(ColumnKeyConstant.KEY_NAME);
        nameColumn.setText("Column");
        if (columnSash != null) {
            columnSash.setRightColumn(nameColumn);
        }
        this.addColumn(nameColumn);
        if (hasExpression) {
            layoutManager.setWeight(0, 0.5);
        }
    }
}
