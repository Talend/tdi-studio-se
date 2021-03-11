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

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.window.Window;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.preference.metadata.MetadataTypeLengthConstants;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.gefabstractmap.figures.treetools.zone.InputZoneToolBar;
import org.talend.designer.xmlmap.XmlMapComponent;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.InputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.XmlMapDataEditPart;
import org.talend.designer.xmlmap.parts.XmlMapDataManager;
import org.talend.designer.xmlmap.ui.dialog.PropertySetDialog;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapInputZoneToolBar extends InputZoneToolBar {

    private XmlMapData externalData;

    private XmlMapDataEditPart externalPart;

    private XmlMapComponent mapperComponent;

    private MapperManager mapperManger;

    private XmlMapGraphicViewer graphicViewer;

    /**
     * DOC Administrator XmlMapInputZoneToolBar constructor comment.
     *
     * @param rootModelManager
     */
    public XmlMapInputZoneToolBar(XmlMapDataManager rootModelManager) {
        super(rootModelManager);
        externalData = rootModelManager.getModel();
        externalPart = rootModelManager.getEditPart();
        graphicViewer = (XmlMapGraphicViewer) rootModelManager.getGraphicalViewer();
        mapperManger = (MapperManager) rootModelManager.getMapperManger();
        mapperComponent = mapperManger.getMapperComponent();
        createZoneContent();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.treetools.zone.ZoneToolBar#moveUp()
     */
    @Override
    public void moveUp() {
        List<InputXmlTree> inputTrees = externalData.getInputTrees();
        InputXmlTreeEditPart currentSelectedInputXmlTree = graphicViewer.getFiguresManager().getCurrentSelectedInputTable();
        if (currentSelectedInputXmlTree != null) {
            InputXmlTree selectedTree = (InputXmlTree) currentSelectedInputXmlTree.getModel();

            int index = inputTrees.indexOf(selectedTree);
            if (index != -1 && index - 1 >= 0) {
                inputTrees.remove(selectedTree);
                inputTrees.add(index - 1, selectedTree);

                // index of modelchildren is different from index of tree
                int indexOf = externalPart.getModelChildren().indexOf(selectedTree);
                if (indexOf != -1) {
                    externalPart.getViewer().appendSelection((EditPart) externalPart.getChildren().get(indexOf));
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
        List<InputXmlTree> inputTrees = externalData.getInputTrees();
        InputXmlTreeEditPart currentSelectedInputXmlTree = graphicViewer.getFiguresManager().getCurrentSelectedInputTable();
        if (currentSelectedInputXmlTree != null) {
            InputXmlTree selectedTree = (InputXmlTree) currentSelectedInputXmlTree.getModel();

            int index = inputTrees.indexOf(selectedTree);
            if (index != -1 && index + 1 < inputTrees.size()) {
                inputTrees.remove(selectedTree);
                inputTrees.add(index + 1, selectedTree);

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
        EList<InputXmlTree> inputTrees = externalData.getInputTrees();
        for (InputXmlTree inputTree : inputTrees) {
            if (minimized != inputTree.isMinimized()) {
                inputTree.setMinimized(minimized);
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.treetools.zone.InputZoneTooBar#changProperty()
     */
    @Override
    public void changProperty() {

        boolean dieOnError = mapperManger.isDieOnError();

        PropertySetDialog propertySet = new PropertySetDialog(null, dieOnError);
        if (propertySet.open() == Window.OK) {
            if (graphicViewer != null && propertySet.isValueChanged()) {
                final boolean newValue = !dieOnError;

                CommandStack commandStack = graphicViewer.getEditDomain().getCommandStack();
                commandStack.execute(new Command() {

                    @Override
                    public void execute() {
                        mapperManger.setDieOnError(newValue);
                        if (newValue) {
                            if (!externalData.getOutputTrees().isEmpty()) {
                                OutputXmlTree outputTree = externalData.getOutputTrees().get(0);
                                if (outputTree.isErrorReject()) {
                                    // mapperComponent.getMetadataList().add(metadataTable);
                                    externalData.getOutputTrees().remove(0);
                                    mapperComponent.getProcess().removeUniqueConnectionName(outputTree.getName());
                                    removeMetadataTableByName(outputTree.getName());
                                    if (!externalData.getOutputTrees().isEmpty()) {
                                        int indexOf = externalPart.getModelChildren().indexOf(
                                                externalData.getOutputTrees().get(0));
                                        if (indexOf != -1) {
                                            graphicViewer.select((EditPart) externalPart.getChildren().get(indexOf));
                                        }
                                    }
                                }
                            }
                        } else {
                            boolean hasRejectTable = false;
                            if (!externalData.getOutputTrees().isEmpty()) {
                                OutputXmlTree outputTree = externalData.getOutputTrees().get(0);
                                if (outputTree.isErrorReject()) {
                                    hasRejectTable = true;
                                }
                            }
                            if (!hasRejectTable) {
                                String baseName = MapperManager.ERROR_REJECT;

                                IProcess process = mapperComponent.getProcess();
                                String tableName = baseName;
                                if (!process.checkValidConnectionName(baseName) && process instanceof IProcess2) {
                                    final String uniqueName = ((IProcess2) process).generateUniqueConnectionName("row", baseName);
                                    tableName = uniqueName;
                                    ((IProcess2) process).addUniqueConnectionName(uniqueName);
                                } else if (process instanceof IProcess2) {
                                    tableName = baseName;
                                    ((IProcess2) process).addUniqueConnectionName(baseName);
                                }
                                OutputXmlTree outputXmlTree = XmlmapFactory.eINSTANCE.createOutputXmlTree();
                                outputXmlTree.setErrorReject(true);
                                outputXmlTree.setName(tableName);
                                externalData.getOutputTrees().add(0, outputXmlTree);

                                MetadataTable metadataTable = new MetadataTable();
                                metadataTable.setLabel(tableName);
                                metadataTable.setTableName(tableName);

                                MetadataColumn errorMessageCol = new MetadataColumn();
                                errorMessageCol.setLabel(MapperManager.ERROR_REJECT_MESSAGE);
                                errorMessageCol.setTalendType(DesignerPlugin.getDefault().getPreferenceStore()
                                        .getString(MetadataTypeLengthConstants.FIELD_DEFAULT_TYPE));
                                errorMessageCol.setNullable(true);
                                errorMessageCol.setOriginalDbColumnName(MapperManager.ERROR_REJECT_MESSAGE);
                                errorMessageCol.setReadOnly(true);
                                errorMessageCol.setCustom(true);
                                metadataTable.getListColumns().add(errorMessageCol);

                                MetadataColumn errorStackTrace = new MetadataColumn();
                                errorStackTrace.setLabel(MapperManager.ERROR_REJECT_STACK_TRACE);
                                errorStackTrace.setTalendType(DesignerPlugin.getDefault().getPreferenceStore()
                                        .getString(MetadataTypeLengthConstants.FIELD_DEFAULT_TYPE));
                                errorStackTrace.setNullable(true);
                                errorStackTrace.setOriginalDbColumnName(MapperManager.ERROR_REJECT_STACK_TRACE);
                                errorStackTrace.setReadOnly(true);
                                errorStackTrace.setCustom(true);
                                metadataTable.getListColumns().add(errorStackTrace);
                                mapperComponent.getMetadataList().add(metadataTable);

                                OutputTreeNode errorMessageNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                                errorMessageNode.setName(MapperManager.ERROR_REJECT_MESSAGE);
                                errorMessageNode.setType(errorMessageCol.getTalendType());
                                errorMessageNode.setNullable(true);
                                outputXmlTree.getNodes().add(errorMessageNode);

                                OutputTreeNode errorStackTraceNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                                errorStackTraceNode.setName(MapperManager.ERROR_REJECT_STACK_TRACE);
                                errorStackTraceNode.setType(errorStackTrace.getTalendType());
                                errorStackTraceNode.setNullable(true);
                                outputXmlTree.getNodes().add(errorStackTraceNode);

                                int indexOf = externalPart.getModelChildren().indexOf(outputXmlTree);
                                if (indexOf != -1) {
                                    graphicViewer.select((EditPart) externalPart.getChildren().get(indexOf));
                                }

                            }
                        }
                    }
                });
            }
        }

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
     * @see org.talend.designer.newabstractmap.figures.treetools.zone.ZoneToolBar#isMinSizeEnable()
     */
    @Override
    protected boolean isMinSizeEnable() {
        return externalData.getInputTrees().isEmpty();
    }

    @Override
    public boolean getMinSizeStatus() {
        boolean min = true;
        for (InputXmlTree tree : externalData.getInputTrees()) {
            if (!tree.isMinimized()) {
                min = false;
            }
        }
        return min;
    }
}
