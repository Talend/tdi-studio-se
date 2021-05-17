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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PositionConstants;
import org.talend.designer.gefabstractmap.figures.ComboCellLabel;
import org.talend.designer.gefabstractmap.figures.borders.ColumnBorder;
import org.talend.designer.gefabstractmap.figures.borders.RowBorder;
import org.talend.designer.gefabstractmap.figures.layout.RowLayout;
import org.talend.designer.gefabstractmap.figures.table.AbstractTable;
import org.talend.designer.gefabstractmap.figures.table.ColumnKeyConstant;
import org.talend.designer.gefabstractmap.figures.table.ColumnSash;
import org.talend.designer.gefabstractmap.figures.table.TableColumn;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.xmlmap.figures.treesettings.TreeSettingsConstant;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;
import org.talend.designer.xmlmap.model.tree.IUILookupMode;
import org.talend.designer.xmlmap.model.tree.IUIMatchingMode;
import org.talend.designer.xmlmap.model.tree.LOOKUP_MODE;
import org.talend.designer.xmlmap.model.tree.XML_MAP_LOOKUP_MODE;
import org.talend.designer.xmlmap.model.tree.XML_MAP_MATCHING_MODE;

/**
 * created by wchen on 2013-1-21 Detailled comment
 *
 */
public class InputTreeSettingTable extends AbstractTable {

    private InputXmlTree inputxmlTree;

    private Figure lookupModelRow;

    private ComboCellLabel lookupModel;

    private Figure matchModelRow;

    private ComboCellLabel matchModel;

    private Figure joinModelRow;

    private ComboCellLabel joinModel;

    private Figure persistentModelRow;

    private ComboCellLabel persistentModel;

    /**
     * DOC Administrator InputTreeSettingTable constructor comment.
     *
     * @param tableModelManager
     */
    public InputTreeSettingTable(XmlMapTableManager tableModelManager) {
        super(tableModelManager);
        inputxmlTree = (InputXmlTree) tableModelManager.getModel();
        createColumns();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.AbstractTable#createColumns()
     */
    @Override
    protected void createColumns() {
        TableColumn column = new TableColumn(ColumnKeyConstant.TREE_SETTING_PROPERTY);
        column.setText("Property");
        addColumn(column);

        ColumnSash sash = new ColumnSash(this);
        sash.setLeftColumn(column);
        addSeparator(sash);

        column = new TableColumn(ColumnKeyConstant.TREE_SETTING_VALUE);
        column.setText("Value");
        sash.setRightColumn(column);
        addColumn(column);

        Figure container = getTableItemContainer();

        lookupModelRow = new Figure();
        lookupModelRow.setLayoutManager(new RowLayout());
        Label label = new Label();
        label.setText("Lookup Model");
        label.setLabelAlignment(PositionConstants.LEFT);
        CompoundBorder compoundBorder = new CompoundBorder(new ColumnBorder(), new RowBorder(2, 5, 2, -1));
        label.setBorder(compoundBorder);
        lookupModelRow.add(label);
        lookupModel = new ComboCellLabel();
        lookupModel.setDirectEditType(DirectEditType.LOOKUP_MODEL);
        lookupModel.setText(getLookupDisplayName(inputxmlTree.getLookupMode()));
        lookupModel.setLabelAlignment(PositionConstants.LEFT);
        lookupModel.setBorder(new RowBorder(2, 5, 2, -1));
        lookupModelRow.add(lookupModel);
        container.add(lookupModelRow);

        matchModelRow = new Figure();
        matchModelRow.setLayoutManager(new RowLayout());
        label = new Label();
        label.setText("Match Model");
        label.setLabelAlignment(PositionConstants.LEFT);
        compoundBorder = new CompoundBorder(new ColumnBorder(), new RowBorder(2, 5, 2, -1));
        label.setBorder(compoundBorder);
        matchModelRow.add(label);
        matchModel = new ComboCellLabel();
        matchModel.setDirectEditType(DirectEditType.MATCH_MODEL);
        matchModel.setText(getMatchModelDisplayName(inputxmlTree.getMatchingMode()));
        matchModel.setLabelAlignment(PositionConstants.LEFT);
        matchModel.setBorder(new RowBorder(2, 5, 2, -1));
        matchModelRow.add(matchModel);
        container.add(matchModelRow);

        joinModelRow = new Figure();
        joinModelRow.setLayoutManager(new RowLayout());
        label = new Label();
        label.setText("Join Model");
        label.setLabelAlignment(PositionConstants.LEFT);
        compoundBorder = new CompoundBorder(new ColumnBorder(), new RowBorder(2, 5, 2, -1));
        label.setBorder(compoundBorder);
        joinModelRow.add(label);
        joinModel = new ComboCellLabel();
        joinModel.setDirectEditType(DirectEditType.JOIN_MODEL);

        String join = "";
        if (inputxmlTree.isInnerJoin()) {
            join = TreeSettingsConstant.INNER_JOIN;
        } else {
            join = TreeSettingsConstant.LEFT_OUTER_JOIN;
        }

        joinModel.setText(join);
        joinModel.setLabelAlignment(PositionConstants.LEFT);
        joinModel.setBorder(new RowBorder(2, 5, 2, -1));
        joinModelRow.add(joinModel);
        container.add(joinModelRow);

        // TDI-17714:remove the true option in the Store temp data lookup property.
        // persistentModelRow = new Figure();
        // persistentModelRow.setLayoutManager(new TableItemLayout());
        // label = new Label();
        // label.setText("Store Temp Data");
        // label.setLabelAlignment(PositionConstants.LEFT);
        // compoundBorder = new CompoundBorder(new RowBorder(), new ColumnBorder());
        // label.setBorder(compoundBorder);
        // persistentModelRow.add(label);
        // persistentModel = new ComboCellLabel();
        // persistentModel.setDirectEditType(DirectEditType.PERSISTENT_MODEL);
        // persistentModel.setText(String.valueOf(inputxmlTree.isPersistent()));
        // persistentModel.setLabelAlignment(PositionConstants.LEFT);
        // persistentModel.setBorder(new RowBorder(2, 5, 2, -1));
        // persistentModelRow.add(persistentModel);
        // container.add(persistentModelRow);
        container.setOpaque(true);
        container.setBackgroundColor(ColorConstants.white);

        container.addMouseListener(new MouseListener() {

            Figure selectedFigure = null;

            @Override
            public void mousePressed(MouseEvent me) {
                boolean lookup = lookupModelRow.containsPoint(me.x, me.y);
                if (lookup) {
                    if (selectedFigure != lookupModelRow) {
                        lookupModelRow.setOpaque(true);
                        lookupModelRow.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_COLUMN_TREE_SETTING));
                        matchModelRow.setOpaque(false);
                        joinModelRow.setOpaque(false);
                        // persistentModelRow.setOpaque(false);
                    }
                    return;
                }
                boolean matchModel = matchModelRow.containsPoint(me.x, me.y);
                if (matchModel) {
                    if (selectedFigure != matchModelRow) {
                        matchModelRow.setOpaque(true);
                        matchModelRow.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_COLUMN_TREE_SETTING));
                        lookupModelRow.setOpaque(false);
                        joinModelRow.setOpaque(false);
                        // persistentModelRow.setOpaque(false);
                    }
                    return;
                }
                boolean joinModel = joinModelRow.containsPoint(me.x, me.y);
                if (joinModel) {
                    if (selectedFigure != joinModelRow) {
                        joinModelRow.setOpaque(true);
                        joinModelRow.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_COLUMN_TREE_SETTING));
                        lookupModelRow.setOpaque(false);
                        matchModelRow.setOpaque(false);
                        // persistentModelRow.setOpaque(false);
                    }
                    return;
                }
                // boolean persistentModel = persistentModelRow.containsPoint(me.x, me.y);
                // if (persistentModel) {
                // if (selectedFigure != persistentModelRow) {
                // persistentModelRow.setOpaque(true);
                // persistentModelRow.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTION));
                // lookupModelRow.setOpaque(false);
                // matchModelRow.setOpaque(false);
                // joinModelRow.setOpaque(false);
                // }
                // }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseDoubleClicked(MouseEvent me) {
            }

        });
    }

    private String getLookupDisplayName(String lookupModel) {
        IUILookupMode[] availableJoins = { XML_MAP_LOOKUP_MODE.LOAD_ONCE, XML_MAP_LOOKUP_MODE.RELOAD,
                XML_MAP_LOOKUP_MODE.CACHE_OR_RELOAD };
        for (IUILookupMode model : availableJoins) {
            if (model.toString().equals(lookupModel)) {
                return model.getLabel();
            }
        }
        return lookupModel;
    }

    private String getMatchModelDisplayName(String matcheModel) {
        IUIMatchingMode[] allMatchingModel = XML_MAP_MATCHING_MODE.values();
        for (IUIMatchingMode model : allMatchingModel) {
            if (model.toString().equals(matcheModel)) {
                return model.getLabel();
            }
        }
        return matcheModel;
    }

    public void update(int type) {
        switch (type) {
        case XmlmapPackage.INPUT_XML_TREE__LOOKUP_MODE:
            lookupModel.setText(getLookupDisplayName(inputxmlTree.getLookupMode()));
            if (inputxmlTree.isLookup() && inputxmlTree.getLookupMode().equals(LOOKUP_MODE.LOAD_ONCE.toString())) {
                inputxmlTree.setActivateGlobalMap(false);
            } else {
                inputxmlTree.setActivateGlobalMap(true);
            }
            break;
        case XmlmapPackage.INPUT_XML_TREE__MATCHING_MODE:
            matchModel.setText(getMatchModelDisplayName(inputxmlTree.getMatchingMode()));
            break;
        case XmlmapPackage.INPUT_XML_TREE__INNER_JOIN:
            if (inputxmlTree.isInnerJoin()) {
                joinModel.setText(TreeSettingsConstant.INNER_JOIN);
            } else {
                joinModel.setText(TreeSettingsConstant.LEFT_OUTER_JOIN);
            }
            break;
        case XmlmapPackage.INPUT_XML_TREE__PERSISTENT:
            persistentModel.setText(String.valueOf(inputxmlTree.isPersistent()));
        default:
            break;
        }
    }

    public void deselectTreeSettingRows() {
        if (lookupModelRow.isOpaque()) {
            lookupModelRow.setOpaque(false);
        }
        if (matchModelRow.isOpaque()) {
            matchModelRow.setOpaque(false);
        }
        if (joinModelRow.isOpaque()) {
            joinModelRow.setOpaque(false);
        }
        // if (persistentModelRow.isOpaque()) {
        // persistentModelRow.setOpaque(false);
        // }
    }

}
