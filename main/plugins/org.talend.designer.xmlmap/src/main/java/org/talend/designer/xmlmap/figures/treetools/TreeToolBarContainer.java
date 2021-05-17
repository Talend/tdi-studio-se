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
package org.talend.designer.xmlmap.figures.treetools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarContainer;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;
import org.talend.designer.xmlmap.figures.table.XmlMapTableManager;
import org.talend.designer.xmlmap.figures.treesettings.TreeSettingsConstant;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.ui.dialog.SetLoopFunctionDialog;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class TreeToolBarContainer extends ToolBarContainer {

    private ToolBarButtonImageFigure setLoopFunctionButton;

    private Map<String, Object> defaultSettingMap = new HashMap<String, Object>();

    private InputXmlTree inputMainTable;

    private AbstractInOutTree abstractTree;

    protected ToolBarButtonImageFigure condensedButton;

    protected ToolBarButtonImageFigure expressionFilterButton;

    public TreeToolBarContainer(XmlMapTableManager tableManager) {
        super(tableManager);
        createToolbar();
    }

    @Override
    public XmlMapTableManager getTableManager() {
        return (XmlMapTableManager) super.getTableManager();
    }

    @Override
    protected void createToolbar() {
        super.createToolbar();
        condensedButton = new CondensedButton(ImageProviderMapper.getImage(ImageInfo.CONDENSED_TOOL_ICON));
        condensedButton.setSelected(getTableManager().isActivateCondensedTool());
        setTooltips(condensedButton, "tXmlMap settings");
        expressionFilterButton = new ExpressionFilterButton(ImageProviderMapper.getImage(ImageInfo.ACTIVATE_FILTER_ICON));
        expressionFilterButton.setSelected(getTableManager().isActivateExpressionFilter());
        setTooltips(expressionFilterButton, "Enable/disable expression filter");

        inputMainTable = ((MapperManager) getTableManager().getGraphicalViewer().getMapperManager()).getMainInputTree();
        abstractTree = getTableManager().getModel();
        if (abstractTree instanceof OutputXmlTree) {
            ImageInfo info = ImageInfo.SETLOOPFUNCTION_BUTTON;
            EList<InputLoopNodesTable> inputLoopNodesTables = ((OutputXmlTree) abstractTree).getInputLoopNodesTables();
            if (!inputLoopNodesTables.isEmpty() && inputLoopNodesTables.get(0) != null) {
                if (inputLoopNodesTables.get(0).getInputloopnodes().isEmpty()) {
                    info = ImageInfo.SETLOOPFUNCTION_BUTTON_ERROR;
                }
            }
            setLoopFunctionButton = new SetLoopFunctionButton(ImageProviderMapper.getImage(info));
            setTooltips(setLoopFunctionButton, "set Loop Function");
            this.add(setLoopFunctionButton);
            if (inputMainTable == null || !inputMainTable.isMultiLoops()) {
                setLoopFunctionButton.setVisible(false);
            }
        }

        // TDI-22087
        if (abstractTree instanceof OutputXmlTree
                || (abstractTree instanceof InputXmlTree && ((InputXmlTree) abstractTree).isLookup())) {

            this.add(condensedButton);

            this.add(expressionFilterButton);
        }

        boolean isErrorReject = false;
        if (abstractTree instanceof OutputXmlTree) {
            isErrorReject = ((OutputXmlTree) abstractTree).isErrorReject();
        }
        if (isErrorReject) {
            condensedButton.setEnabled(false);
            expressionFilterButton.setEnabled(false);
        }

        this.add(min_size);
    }

    public void updateLoopFunctionButton() {
        if (setLoopFunctionButton != null && abstractTree instanceof OutputXmlTree) {
            if (inputMainTable != null && inputMainTable.isMultiLoops()) {
                if (!XmlMapUtil.hasDocument(abstractTree)) {
                    if (abstractTree instanceof OutputXmlTree) {
                        EList<InputLoopNodesTable> inputLoopNodesTables = ((OutputXmlTree) abstractTree)
                                .getInputLoopNodesTables();
                        if (inputLoopNodesTables.isEmpty() || inputLoopNodesTables.get(0).getInputloopnodes().isEmpty()) {
                            setLoopFunctionButton.setImage(ImageProviderMapper.getImage(ImageInfo.SETLOOPFUNCTION_BUTTON_ERROR));
                        } else {
                            setLoopFunctionButton.setImage(ImageProviderMapper.getImage(ImageInfo.SETLOOPFUNCTION_BUTTON));
                        }
                    }
                    setLoopFunctionButton.setVisible(true);
                } else {
                    setLoopFunctionButton.setVisible(false);
                }
            } else {
                setLoopFunctionButton.setVisible(false);
            }
        }
    }

    @Override
    public void updateButtonsColor(Color color) {
        super.updateButtonsColor(color);
        if (condensedButton != null) {
            if (!condensedButton.isSelected()) {
                condensedButton.setBackgroundColor(color);
            }
        }
        if (expressionFilterButton != null) {
            if (!expressionFilterButton.isSelected()) {
                expressionFilterButton.setBackgroundColor(color);
            }
        }
        if (setLoopFunctionButton != null) {
            updateLoopFunctionButton();
            if (!setLoopFunctionButton.isSelected()) {
                setLoopFunctionButton.setBackgroundColor(color);
            }
        }

    }

    public Map<String, Object> getDefaultSetting() {
        if (defaultSettingMap.isEmpty()) {
            defaultSettingMap.put(TreeSettingsConstant.OUTPUT_REJECT, false);
            defaultSettingMap.put(TreeSettingsConstant.LOOK_UP_INNER_JOIN_REJECT, false);
        }
        return defaultSettingMap;
    }

    class CondensedButton extends ToolBarButtonImageFigure {

        public CondensedButton(Image image) {
            super(image);
            setStyle(Clickable.STYLE_TOGGLE);
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            getTableManager().setActivateCondensedTool(this.isSelected());
            revalidate();
        }
    }

    class ExpressionFilterButton extends ToolBarButtonImageFigure {

        public ExpressionFilterButton(Image image) {
            super(image);
            setStyle(Clickable.STYLE_TOGGLE);
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            getTableManager().setActivateExpressionFilter(this.isSelected());
            revalidate();
        }
    }

    class SetLoopFunctionButton extends ToolBarButtonImageFigure {

        public SetLoopFunctionButton(Image image) {
            super(image);
            setStyle(Clickable.STYLE_TOGGLE);
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            CommandStack commandStack = getTableManager().getGraphicalViewer().getEditDomain().getCommandStack();
            commandStack.execute(new Command() {

                @Override
                public void execute() {
                    if (abstractTree instanceof OutputXmlTree) {

                        OutputXmlTree outputXmlTree = (OutputXmlTree) abstractTree;
                        InputLoopNodesTable inputLoopNodesTable = null;
                        if (!outputXmlTree.getInputLoopNodesTables().isEmpty()) {
                            inputLoopNodesTable = outputXmlTree.getInputLoopNodesTables().get(0);
                        } else {
                            inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                            outputXmlTree.getInputLoopNodesTables().add(inputLoopNodesTable);
                        }
                        List<TreeNode> loopNodes = new ArrayList<TreeNode>();
                        if (inputMainTable != null && inputMainTable.isMultiLoops()) {
                            loopNodes.addAll(XmlMapUtil.getMultiLoopsForXmlTree(inputMainTable));
                        }
                        SetLoopFunctionDialog nsDialog = new SetLoopFunctionDialog(null, inputLoopNodesTable, loopNodes);
                        setLoopFunctionButton.setSelected(false);
                        if (nsDialog.open() == Window.OK) {
                            ((MapperManager) getTableManager().getGraphicalViewer().getMapperManager()).getProblemsAnalyser()
                                    .checkProblems(abstractTree);
                            ((MapperManager) getTableManager().getGraphicalViewer().getMapperManager()).getMapperUI()
                                    .updateStatusBar();
                        }
                    }
                }
            });
            revalidate();
        }
    }

}
