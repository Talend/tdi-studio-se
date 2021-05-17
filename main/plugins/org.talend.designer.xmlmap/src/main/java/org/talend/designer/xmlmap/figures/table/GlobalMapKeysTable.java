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

import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PositionConstants;
import org.talend.designer.gefabstractmap.figures.manager.TableManager;
import org.talend.designer.gefabstractmap.figures.table.AbstractTable;
import org.talend.designer.gefabstractmap.figures.table.ColumnKeyConstant;
import org.talend.designer.gefabstractmap.figures.table.ColumnSash;
import org.talend.designer.gefabstractmap.figures.table.TableColumn;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;
import org.talend.designer.xmlmap.i18n.Messages;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class GlobalMapKeysTable extends AbstractTable {

    private InputXmlTree inputxmlTree;

    private TableColumn expressionColumn;

    private TableColumn globalMapKeyColumn;

    private TableColumn addColumn;

    private ColumnSash columnSash;

    /**
     * DOC hcyi InputTreeGlobalMapKeysTable constructor comment.
     *
     * @param tableModelManager
     */
    public GlobalMapKeysTable(TableManager tableModelManager) {
        super(tableModelManager);
        inputxmlTree = (InputXmlTree) tableModelManager.getModel();
        createColumns();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.table.AbstractTable#createColumns()
     */
    @Override
    protected void createColumns() {
        layoutManager.setAjustToTableWidth(false);
        if (inputxmlTree != null && inputxmlTree.isLookup()) {
            expressionColumn = new TableColumn(ColumnKeyConstant.KEY_EXPRESSION);
            expressionColumn.setText(Messages.getString("InputTreeGlobalMapKeysTable.expressionTitle"));
            addColumn(expressionColumn);

            columnSash = new ColumnSash(this);
            columnSash.setLeftColumn(expressionColumn);
            addSeparator(columnSash);

            globalMapKeyColumn = new TableColumn(ColumnKeyConstant.KEY_NAME);
            globalMapKeyColumn.setText(Messages.getString("InputTreeGlobalMapKeysTable.globalMapKeyTitle"));
            columnSash.setRightColumn(globalMapKeyColumn);
            addColumn(globalMapKeyColumn);

            columnSash = new ColumnSash(this);
            columnSash.setLeftColumn(globalMapKeyColumn);
            addSeparator(columnSash);

            addColumn = new TableColumn(ColumnKeyConstant.KEY_VARIABLE);
            addColumn.setIcon(ImageProviderMapper.getImage(ImageInfo.ADD));
            addColumn.setLabelAlignment(PositionConstants.BOTTOM);
            columnSash.setRightColumn(addColumn);
            addColumn.setBorder(new MarginBorder(2));
            addColumn(addColumn);

            // add Listener
            addColumn.addMouseListener(new MouseListener() {

                @Override
                public void mousePressed(MouseEvent me) {
                    GlobalMapNode defaultGlobalMapNode = XmlmapFactory.eINSTANCE.createGlobalMapNode();
                    defaultGlobalMapNode.setExpression("");
                    defaultGlobalMapNode.setName("\"myKey\"");
                    inputxmlTree.getGlobalMapKeysValues().add(defaultGlobalMapNode);
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                }

                @Override
                public void mouseDoubleClicked(MouseEvent me) {
                }

            });

        }
    }

    public InputXmlTree getInputxmlTree() {
        return this.inputxmlTree;
    }
}
