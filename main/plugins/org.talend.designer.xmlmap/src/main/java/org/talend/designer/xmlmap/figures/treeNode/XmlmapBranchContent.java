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
package org.talend.designer.xmlmap.figures.treeNode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.window.Window;
import org.talend.datatools.xml.utils.XSDPopulationUtil2;
import org.talend.designer.gefabstractmap.figures.table.entity.TreeBranchContent;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.ui.dialog.SetLoopFunctionDialog;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class XmlmapBranchContent extends TreeBranchContent {

    protected Label statusFigure;

    protected Label defaultValue;

    private ToolBarButtonImageFigure loopButtonFigure;

    private TreeNodeEntityManager entityManager;

    private TreeNode treeNode;

    private MapperManager manager;

    public XmlmapBranchContent(TreeNodeEntityManager entityManager) {
        this.entityManager = entityManager;
        treeNode = entityManager.getModel();
        this.manager = (MapperManager) entityManager.getGraphicalViewer().getMapperManager();
        createContent();
    }

    @Override
    protected void createContent() {
        super.createContent();
        statusFigure = new Label();
        statusFigure.setForegroundColor(ColorConstants.red);
        statusFigure.setText(getStatus());

        defaultValue = new Label();
        defaultValue.setForegroundColor(ColorConstants.blue);
        defaultValue.setText(getDefaultValue());
        ImageInfo infor = ImageInfo.SETLOOPFUNCTION_BUTTON;

        if (treeNode instanceof OutputTreeNode) {
            InputLoopNodesTable inputLoopNodesTable = ((OutputTreeNode) treeNode).getInputLoopNodesTable();
            infor = (inputLoopNodesTable == null || inputLoopNodesTable.getInputloopnodes().isEmpty()) ? ImageInfo.SETLOOPFUNCTION_BUTTON_ERROR
                    : ImageInfo.SETLOOPFUNCTION_BUTTON;
        }
        loopButtonFigure = new ToolBarButtonImageFigure(ImageProviderMapper.getImage(infor));
        loopButtonFigure.addMouseListener(new MouseListener.Stub() {

            @Override
            public void mousePressed(MouseEvent me) {
                OutputTreeNode outputTreeNode = (OutputTreeNode) treeNode;
                List<TreeNode> loopNodes = new ArrayList<TreeNode>();
                if (manager.isMainTableMultiLoop()) {
                    loopNodes.addAll(XmlMapUtil.getMultiLoopsForXmlTree(manager.getMainInputTree()));
                }
                InputLoopNodesTable inputLoopNodesTable = null;
                if (outputTreeNode.getInputLoopNodesTable() != null) {
                    inputLoopNodesTable = outputTreeNode.getInputLoopNodesTable();
                } else {
                    inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                    outputTreeNode.setInputLoopNodesTable(inputLoopNodesTable);
                    AbstractInOutTree abstractInOutTree = XmlMapUtil.getAbstractInOutTree(outputTreeNode);
                    if (abstractInOutTree != null) {
                        ((OutputXmlTree) abstractInOutTree).getInputLoopNodesTables().add(inputLoopNodesTable);
                    }
                }
                SetLoopFunctionDialog nsDialog = new SetLoopFunctionDialog(null, outputTreeNode.getInputLoopNodesTable(),
                        loopNodes);
                if (nsDialog.open() == Window.OK) {
                    manager.getProblemsAnalyser().checkProblems(XmlMapUtil.getAbstractInOutTree(outputTreeNode));
                    manager.getMapperUI().updateStatusBar();
                }
            }
        });

        if (treeNode != null && treeNode instanceof OutputTreeNode) {
            // display loop setup button only when input main is multiloop
            if (treeNode.isLoop() && manager.isMainTableMultiLoop()) {
                this.add(loopButtonFigure);
            }
        }
        this.add(statusFigure);
        this.add(defaultValue);
    }

    public void updateLoopButtonFigure() {
        if (treeNode instanceof OutputTreeNode) {
            if (treeNode.isLoop() && manager.isMainTableMultiLoop()) {
                if (!this.getChildren().contains(loopButtonFigure)) {
                    this.add(loopButtonFigure, 1);
                }
                InputLoopNodesTable loopNodesTable = ((OutputTreeNode) treeNode).getInputLoopNodesTable();
                if (loopNodesTable == null || loopNodesTable.getInputloopnodes().isEmpty()) {
                    loopButtonFigure.setImage(ImageProviderMapper.getImage(ImageInfo.SETLOOPFUNCTION_BUTTON_ERROR));
                } else {
                    loopButtonFigure.setImage(ImageProviderMapper.getImage(ImageInfo.SETLOOPFUNCTION_BUTTON));
                }

            } else {
                if (this.getChildren().contains(loopButtonFigure)) {
                    this.remove(loopButtonFigure);
                }
            }
        }

    }

    @Override
    protected String getNameValue() {
        String name = "";
        if (NodeType.ATTRIBUT.equals(treeNode.getNodeType())) {
            name = XmlMapUtil.XPATH_ATTRIBUTE + treeNode.getName();
        } else if (NodeType.NAME_SPACE.equals(treeNode.getNodeType())) {
            name = XmlMapUtil.XPATH_NAMESPACE + treeNode.getName();
        } else {
            if (treeNode.isChoice()) {
                name = XSDPopulationUtil2.CHOICE;
            } else {
                name = treeNode.getName();
            }
        }
        return name;
    }

    private String getDefaultValue() {
        if (NodeType.NAME_SPACE.equals(treeNode.getNodeType())) {
            String defaultValue2 = treeNode.getDefaultValue();
            if (defaultValue2 == null || "".equals(defaultValue2)) {
                return "";
            }
            return "(Default Value :" + defaultValue2 + ")";
        } else {
            return "";
        }
    }

    public String getStatus() {
        String status = "";
        if (treeNode.isLoop()) {
            status = "(loop" + (treeNode.isOptional() ? " :optional" : "");
        }
        if (treeNode instanceof OutputTreeNode) {
            OutputTreeNode outputNode = (OutputTreeNode) treeNode;
            if (outputNode.isGroup()) {
                if ("".equals(status)) {
                    status = "(group";
                } else {
                    status = status + " , group";
                }

            }

            if (outputNode.isAggregate()) {
                if ("".equals(status)) {
                    status = "(aggregate";
                } else {
                    status = status + " , aggregate";
                }

            }
        }

        if (!"".equals(status)) {
            status = status + ")";
        }

        return status;
    }

    public void updateStatus() {
        statusFigure.setText(getStatus());
    }

    public void updateDefaultValueFigure() {
        defaultValue.setText(getDefaultValue());
    }

    @Override
    public void updataNameFigure() {
        nameFigure.setText(getNameValue());
    }

}
