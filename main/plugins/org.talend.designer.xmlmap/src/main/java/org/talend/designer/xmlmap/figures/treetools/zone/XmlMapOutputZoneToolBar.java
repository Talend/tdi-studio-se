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
package org.talend.designer.xmlmap.figures.treetools.zone;

import java.util.List;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;
import org.talend.designer.gefabstractmap.figures.treetools.zone.OutputZoneToolBar;
import org.talend.designer.xmlmap.XmlMapComponent;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.OutputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.XmlMapDataEditPart;
import org.talend.designer.xmlmap.parts.XmlMapDataManager;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapOutputZoneToolBar extends OutputZoneToolBar {

    private XmlMapData externalData;

    private XmlMapDataEditPart externalPart;

    private XmlMapComponent mapperComponent;

    private MapperManager mapperManger;

    private XmlMapGraphicViewer graphicViewer;

    /**
     * DOC Administrator XmlMapOutputZoneToolBar constructor comment.
     *
     * @param rootModelManager
     */
    public XmlMapOutputZoneToolBar(XmlMapDataManager rootModelManager) {
        super(rootModelManager);
        externalData = rootModelManager.getModel();
        externalPart = rootModelManager.getEditPart();
        graphicViewer = (XmlMapGraphicViewer) rootModelManager.getGraphicalViewer();
        mapperManger = (MapperManager) rootModelManager.getMapperManger();
        mapperComponent = mapperManger.getMapperComponent();
        createZoneContent();
    }

    private void removeMetadataTableByName(String name) {
        if (name == null) {
            return;
        }
        IMetadataTable found = null;
        if (mapperComponent != null && mapperComponent.getMetadataList() != null) {
            for (IMetadataTable table : mapperComponent.getMetadataList()) {
                if (name.equals(table.getTableName())) {
                    found = table;
                    break;
                }
            }
            if (found != null) {
                mapperComponent.getMetadataList().remove(found);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.treetools.zone.OutputZoneToolBar#addTable()
     */
    @Override
    protected void addTable() {
        final OutputXmlTree createOutputXmlTree = XmlmapFactory.eINSTANCE.createOutputXmlTree();
        CommandStack commandStack = externalPart.getViewer().getEditDomain().getCommandStack();
        commandStack.execute(new Command() {

            @Override
            public void execute() {
                if (externalPart.getViewer() instanceof XmlMapGraphicViewer) {
                    XmlMapGraphicViewer viewer = (XmlMapGraphicViewer) externalPart.getViewer();
                    MapperManager manager = viewer.getMapperManager();
                    String outputName = manager.getMapperUI().openNewOutputCreationDialog();
                    if (outputName == null) {
                        return;
                    }
                    createOutputXmlTree.setName(outputName);
                    externalData.getOutputTrees().add(createOutputXmlTree);
                    MetadataTable metadataTable = new MetadataTable();
                    metadataTable.setLabel(outputName);
                    metadataTable.setTableName(outputName);
                    mapperComponent.getMetadataList().add(metadataTable);
                    mapperComponent.getProcess().addUniqueConnectionName(outputName);

                    int indexOf = externalPart.getModelChildren().indexOf(createOutputXmlTree);
                    if (indexOf != -1) {
                        externalPart.getViewer().select((EditPart) externalPart.getChildren().get(indexOf));
                    }

                    // need set focus back to canvas

                    if (!min_size.isEnabled()) {
                        min_size.setEnabled(true);
                    }
                    // check problem for created output incase input main is multiloop
                    mapperManger.getProblemsAnalyser().checkProblems(createOutputXmlTree);
                    mapperManger.getMapperUI().updateStatusBar();
                }

            }
        });

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.treetools.zone.OutputZoneToolBar#removeTable()
     */
    @Override
    protected void removeTable() {

        CommandStack commandStack = graphicViewer.getEditDomain().getCommandStack();
        commandStack.execute(new Command() {

            @Override
            public void execute() {
                OutputXmlTreeEditPart currentSelectedOutputXmlTree = graphicViewer.getFiguresManager()
                        .getCurrentSelectedOutputTable();
                if (currentSelectedOutputXmlTree != null) {
                    OutputXmlTree outputTree = (OutputXmlTree) currentSelectedOutputXmlTree.getModel();
                    int indexOf = externalPart.getModelChildren().indexOf(outputTree);

                    mapperComponent.getProcess().removeUniqueConnectionName(outputTree.getName());
                    removeMetadataTableByName(outputTree.getName());
                    for (OutputTreeNode treeNode : outputTree.getNodes()) {
                        XmlMapUtil.detachConnectionsSouce(treeNode, externalData);
                    }
                    externalData.getOutputTrees().remove(outputTree);
                    XmlMapUtil.detachFilterSource(outputTree, externalData);

                    indexOf = indexOf - 1;
                    if (indexOf > -1 && (EditPart) externalPart.getChildren().get(indexOf) instanceof OutputXmlTreeEditPart) {
                        graphicViewer.select((EditPart) externalPart.getChildren().get(indexOf));
                    } else if (indexOf > -1 && indexOf + 1 < externalPart.getChildren().size()
                            && (EditPart) externalPart.getChildren().get(indexOf + 1) instanceof OutputXmlTreeEditPart) {
                        graphicViewer.select((EditPart) externalPart.getChildren().get(indexOf + 1));
                    } else {
                        mapperManger.getMapperUI().getTabFolderEditors().getOutputMetaEditorView().setMetadataTableEditor(null);
                    }
                    mapperManger.getProblemsAnalyser().clearProblemsForTree(outputTree);
                    mapperManger.getMapperUI().updateStatusBar();
                }
                if (externalData.getOutputTrees().isEmpty() && min_size.isEnabled()) {
                    min_size.setEnabled(false);
                }
            }
        });

    }

    class AutoMapButton extends ToolBarButtonImageFigure {

        public AutoMapButton() {
            super(null);
            setText("Auto Map");
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            AutoMapper autoMap = new AutoMapper(externalData);
            autoMap.map();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.treetools.zone.ZoneToolBar#moveUp()
     */
    @Override
    public void moveUp() {
        List<OutputXmlTree> outputTrees = externalData.getOutputTrees();
        OutputXmlTreeEditPart currentSelectedOutputXmlTree = graphicViewer.getFiguresManager().getCurrentSelectedOutputTable();
        if (currentSelectedOutputXmlTree != null) {
            OutputXmlTree selectedTree = (OutputXmlTree) currentSelectedOutputXmlTree.getModel();

            int index = outputTrees.indexOf(selectedTree);
            if (index != -1 && index - 1 >= 0) {
                outputTrees.remove(selectedTree);
                outputTrees.add(index - 1, selectedTree);

                // index of modelchildren is different from index of tree
                int indexOf = externalPart.getModelChildren().indexOf(selectedTree);
                if (indexOf != -1) {
                    graphicViewer.appendSelection((EditPart) externalPart.getChildren().get(indexOf));
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.treetools.zone.ZoneToolBar#moveDown()
     */
    @Override
    public void moveDown() {
        List<OutputXmlTree> outputTrees = externalData.getOutputTrees();
        OutputXmlTreeEditPart currentSelectedOutputXmlTree = graphicViewer.getFiguresManager().getCurrentSelectedOutputTable();
        if (currentSelectedOutputXmlTree != null) {
            OutputXmlTree selectedTree = (OutputXmlTree) currentSelectedOutputXmlTree.getModel();

            int index = outputTrees.indexOf(selectedTree);
            if (index != -1 && index + 1 < outputTrees.size()) {
                outputTrees.remove(selectedTree);
                outputTrees.add(index + 1, selectedTree);

                // index of modelchildren is different from index of tree
                int indexOf = externalPart.getModelChildren().indexOf(selectedTree);
                if (indexOf != -1) {
                    graphicViewer.appendSelection((EditPart) externalPart.getChildren().get(indexOf));
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.treetools.zone.ZoneToolBar#minSize()
     */
    @Override
    public void minSize() {
        minimized = !minimized;
        EList<OutputXmlTree> outputTrees = externalData.getOutputTrees();
        for (OutputXmlTree outputTree : outputTrees) {
            if (minimized != outputTree.isMinimized()) {
                outputTree.setMinimized(minimized);
            }
        }
    }

    @Override
    protected boolean isMinSizeEnable() {
        return externalData.getOutputTrees().isEmpty();
    }

    @Override
    public boolean getMinSizeStatus() {
        boolean min = true;
        for (OutputXmlTree tree : externalData.getOutputTrees()) {
            if (!tree.isMinimized()) {
                min = false;
            }
        }
        return min;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.treetools.zone.OutputZoneToolBar#autoMap()
     */
    @Override
    public void autoMap() {
        AutoMapper mapper = new AutoMapper(externalData);
        mapper.map();
    }
}
