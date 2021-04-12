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
package org.talend.designer.xmlmap.ui.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.data.list.ListenableListEvent.TYPE;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.ui.metadata.dialog.CustomTableManager;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.designer.gefabstractmap.manager.AbstractMapperManager;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.xmlmap.XmlMapComponent;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.InputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.OutputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.parts.directedit.ExpressionCellEditor;
import org.talend.designer.xmlmap.parts.directedit.XmlMapNodeDirectEditManager;
import org.talend.designer.xmlmap.ui.MapperUI;
import org.talend.designer.xmlmap.ui.tabs.table.TreeSchemaTableEditor;
import org.talend.designer.xmlmap.ui.tabs.table.TreeSchemaTableEntry;
import org.talend.designer.xmlmap.ui.tabs.table.XmlTreeSchemaTableView;
import org.talend.designer.xmlmap.ui.tabs.table.utils.SchemaTableUtils;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.designer.xmlmap.util.problem.ProblemsAnalyser;

/**
 * wchen class global comment. Detailled comment
 */
public class MapperManager extends AbstractMapperManager {

    private MapperUI mapperUI;

    private InputXmlTree selectedInputTree;

    private OutputXmlTree selectedOutputTree;

    private InputXmlTree mainInputTree;

    private ProblemsAnalyser problemsAnalyser;

    private XmlMapNodeDirectEditManager currentDirectEditManager;

    private boolean isDieOnError;

    public static final String ERROR_REJECT = "ErrorReject";//$NON-NLS-1$

    public static final String ERROR_REJECT_MESSAGE = "errorMessage";

    public static final String ERROR_REJECT_STACK_TRACE = "errorStackTrace";

    private IFigure selectedFigure;

    public MapperManager(XmlMapComponent mapperComponent, XmlMapData copyOfMapData) {
        super(mapperComponent, copyOfMapData);
        problemsAnalyser = new ProblemsAnalyser(this);
        for (InputXmlTree input : copyOfMapData.getInputTrees()) {
            if (!input.isLookup()) {
                mainInputTree = input;
                break;
            }
        }
        problemsAnalyser.checkProblems();
    }

    @Override
    public XmlMapComponent getMapperComponent() {
        return (XmlMapComponent) super.getMapperComponent();
    }

    @Override
    public XmlMapData getExternalData() {
        return (XmlMapData) super.getExternalData();
    }

    public ExtendedTableModel<TreeSchemaTableEntry> getSelectedInputTreeSchemaModel(InputXmlTree inputXmlTree) {
        if (inputXmlTree == null && !getExternalData().getInputTrees().isEmpty()) {
            inputXmlTree = getExternalData().getInputTrees().get(0);
        }
        List<TreeSchemaTableEntry> treeSchemaEntrys = new ArrayList<TreeSchemaTableEntry>();
        ExtendedTableModel<TreeSchemaTableEntry> tableModel = new ExtendedTableModel<TreeSchemaTableEntry>("Tree Schema",
                treeSchemaEntrys);

        if (inputXmlTree != null) {
            tableModel.setName(inputXmlTree.getName());
            EList<TreeNode> nodes = inputXmlTree.getNodes();
            for (TreeNode node : nodes) {
                if (XmlMapUtil.DOCUMENT.equals(node.getType())) {
                    addTreeSchemaEnties(tableModel, node.getChildren());
                }
            }
        }
        return tableModel;
    }

    public ExtendedTableModel<TreeSchemaTableEntry> getSelectedOutputTreeSchemaModel(OutputXmlTree outputXmlTree) {
        if (outputXmlTree == null && !getExternalData().getOutputTrees().isEmpty()) {
            outputXmlTree = getExternalData().getOutputTrees().get(0);
        }
        List<TreeSchemaTableEntry> treeSchemaEntrys = new ArrayList<TreeSchemaTableEntry>();
        ExtendedTableModel<TreeSchemaTableEntry> tableModel = new ExtendedTableModel<TreeSchemaTableEntry>("Tree Schema",
                treeSchemaEntrys);

        if (outputXmlTree != null) {
            tableModel.setName(outputXmlTree.getName());
            EList<OutputTreeNode> nodes = outputXmlTree.getNodes();
            for (TreeNode node : nodes) {
                if (XmlMapUtil.DOCUMENT.equals(node.getType())) {
                    addTreeSchemaEnties(tableModel, node.getChildren());
                }
            }
        }
        return tableModel;
    }

    private void addTreeSchemaEnties(ExtendedTableModel<TreeSchemaTableEntry> tableModel, EList<TreeNode> nodes) {
        for (TreeNode node : nodes) {
            // avoid to edit choice and subs in schema editor
            if (!node.isChoice() && !node.isSubstitution()) {
                TreeSchemaTableEntry entry = new TreeSchemaTableEntry(node);
                tableModel.add(entry);
            }
            if (!node.getChildren().isEmpty()) {
                addTreeSchemaEnties(tableModel, node.getChildren());
            }
        }

    }

    public void setMapperUI(MapperUI mapperUI) {
        this.mapperUI = mapperUI;
    }

    public MapperUI getMapperUI() {
        return this.mapperUI;
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        if (!event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection) {
            Iterator iterator = ((IStructuredSelection) event.getSelection()).iterator();
            while (iterator.hasNext()) {
                Object firstElement = iterator.next();
                if (firstElement instanceof TableEntityPart) {
                    AbstractNode model = (AbstractNode) ((TableEntityPart) firstElement).getModel();
                    boolean isInputMain = false;
                    if (model instanceof OutputTreeNode) {
                        OutputTreeNode outputTreeNodeRoot = (OutputTreeNode) XmlMapUtil.getTreeNodeRoot((OutputTreeNode) model);
                        if (outputTreeNodeRoot != null && outputTreeNodeRoot.eContainer() instanceof OutputXmlTree) {
                            selectOutputXmlTree((OutputXmlTree) outputTreeNodeRoot.eContainer());

                            onEntitySelection((IStructuredSelection) event.getSelection(), selectedOutputTree);
                        }
                    } else if (model instanceof TreeNode) {
                        TreeNode inputTreeNodeRoot = XmlMapUtil.getTreeNodeRoot((TreeNode) model);
                        if (inputTreeNodeRoot != null && inputTreeNodeRoot.eContainer() instanceof InputXmlTree) {
                            selectInputXmlTree((InputXmlTree) inputTreeNodeRoot.eContainer());
                            isInputMain = !((InputXmlTree) inputTreeNodeRoot.eContainer()).isLookup();
                            onEntitySelection((IStructuredSelection) event.getSelection(), selectedInputTree);
                        }
                    }
                    if (!isInputMain) {
                        refreshStyledTextEditor((TableEntityPart) firstElement);
                    } else {
                        refreshStyledTextEditor(null);
                    }
                    selectedFigure = ((TableEntityPart) firstElement).getFigure();
                } else if (firstElement instanceof InputXmlTreeEditPart) {
                    selectInputXmlTree((InputXmlTree) ((InputXmlTreeEditPart) firstElement).getModel());
                    refreshStyledTextEditor(null);
                } else if (firstElement instanceof OutputXmlTreeEditPart) {
                    selectOutputXmlTree((OutputXmlTree) ((OutputXmlTreeEditPart) firstElement).getModel());
                    refreshStyledTextEditor(null);
                }
            }
        }

        // else {
        // ExtendedTableModel<TreeSchemaTableEntry> oldModel = mapperUI.getTabFolderEditors().getInputTreeSchemaEditor()
        // .getExtendedTableModel();
        // if (oldModel != null && oldModel.getBeanCount() != 0) {
        // List<TreeSchemaTableEntry> treeSchemaEntrys = new ArrayList<TreeSchemaTableEntry>();
        // mapperUI.getTabFolderEditors().getInputTreeSchemaEditor()
        // .setExtendedControlModel(new ExtendedTableModel<TreeSchemaTableEntry>("Tree Schema", treeSchemaEntrys));
        // mapperUI.getTabFolderEditors().getInputTreeSchemaEditor().getTableViewerCreator().refresh();
        // }
        // refreshStyledTextEditor(null);
        // }

    }

    private void onEntitySelection(IStructuredSelection selection, AbstractInOutTree selectedTree) {
        // do selection in metadata schema editor
        EList<? extends TreeNode> nodes = null;
        if (selectedTree instanceof InputXmlTree) {
            nodes = ((InputXmlTree) selectedTree).getNodes();
        } else {
            nodes = ((OutputXmlTree) selectedTree).getNodes();
        }
        List<Integer> selectionIndices = new ArrayList<Integer>();

        List<TreeNode> selectedDocChild = new ArrayList<TreeNode>();
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof TreeNodeEditPart) {
                TreeNode model = (TreeNode) ((TreeNodeEditPart) obj).getModel();
                if (model.eContainer() == selectedTree) {
                    selectionIndices.add(nodes.indexOf(model));
                } else {
                    AbstractInOutTree abstractInOutTree = XmlMapUtil.getAbstractInOutTree(model);
                    if (abstractInOutTree == selectedTree) {
                        selectedDocChild.add(model);
                    }
                }
            }
        }
        int selections[] = new int[selectionIndices.size()];
        for (int i = 0; i < selectionIndices.size(); i++) {
            selections[i] = selectionIndices.get(i);
        }

        MetadataTableEditorView metaEditorView = null;
        XmlTreeSchemaTableView treeEditorView = null;
        if (selectedTree instanceof InputXmlTree) {
            metaEditorView = mapperUI.getTabFolderEditors().getInputMetaEditorView();
            treeEditorView = mapperUI.getTabFolderEditors().getInputTreeSchemaEditor();
        } else {
            metaEditorView = mapperUI.getTabFolderEditors().getOutputMetaEditorView();
            treeEditorView = mapperUI.getTabFolderEditors().getOutputTreeSchemaEditor();
        }
        metaEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(false);
        metaEditorView.getExtendedTableViewer().getTableViewerCreator().getSelectionHelper().setSelection(selections);
        metaEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(true);
        metaEditorView.getExtendedToolbar().updateEnabledStateOfButtons();

        // tree schema editor
        // check if the first selection in selectedTree is displayed in current page ,if yes , select this record in
        // tree schema editor,
        // if no, change page
        if (!selectedDocChild.isEmpty()) {
            treeEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(false);
            TreeNode firstSelection = null;
            if (selectedDocChild.size() > 1) {
                selectionIndices = new ArrayList<Integer>();
                List<TreeNode> allList = new ArrayList<TreeNode>();
                for (TreeNode schemaNodes : nodes) {
                    if (XmlMapUtil.DOCUMENT.equals(schemaNodes.getType())) {
                        allList.addAll(SchemaTableUtils.getTreeSchemaEnties(schemaNodes));
                    }
                }
                int minIndex = allList.size();

                for (TreeNode selected : selectedDocChild) {
                    int indexOf = allList.indexOf(selected);
                    if (indexOf != -1 && indexOf < minIndex) {
                        firstSelection = selected;
                    }
                }
            } else {
                firstSelection = selectedDocChild.get(0);
            }
            treeEditorView.getExtendedTableModel().changePageIfNeeded(firstSelection);

            // update selection
            selectionIndices = new ArrayList<Integer>();
            for (TreeNode selected : selectedDocChild) {
                int indexOf = treeEditorView.getExtendedTableModel().getBeansList().indexOf(selected);
                if (indexOf != -1) {
                    selectionIndices.add(indexOf);
                }
            }
            selections = new int[selectionIndices.size()];
            for (int i = 0; i < selectionIndices.size(); i++) {
                selections[i] = selectionIndices.get(i);
            }

            treeEditorView.getExtendedTableViewer().getTableViewerCreator().getSelectionHelper().setSelection(selections);
            treeEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(true);
            treeEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
        }
    }

    public void selectLinkedInputTableEntries(int[] selectionIndices, boolean isDoc) {
        if (selectedInputTree != null) {
            List nodes = null;
            if (!isDoc) {
                nodes = selectedInputTree.getNodes();
            } else {
                nodes = mapperUI.getTabFolderEditors().getInputTreeSchemaEditor().getExtendedTableModel().getBeansList();
            }

            selectLinkedTableEntries(nodes, selectionIndices);
        }
    }

    public void selectLinkedOutputTableEntries(int[] selectionIndices, boolean isDoc) {
        if (selectedOutputTree != null) {
            List nodes = null;
            if (!isDoc) {
                nodes = selectedOutputTree.getNodes();
            } else {
                nodes = mapperUI.getTabFolderEditors().getOutputTreeSchemaEditor().getExtendedTableModel().getBeansList();
            }
            selectLinkedTableEntries(nodes, selectionIndices);
        }
    }

    private void selectLinkedTableEntries(List nodes, int[] selectionIndices) {
        if (getGraphicalViewer() != null) {
            boolean select = false;
            for (int selectionIndice : selectionIndices) {
                if (selectionIndice < nodes.size()) {
                    Object object = getGraphicalViewer().getEditPartRegistry().get(nodes.get(selectionIndice));
                    if (object instanceof EditPart) {
                        if (!select) {
                            getGraphicalViewer().select((EditPart) object);
                            select = true;
                        } else {
                            getGraphicalViewer().appendSelection((EditPart) object);
                        }
                    }
                }
            }
        }
    }

    public void refreshStyledTextEditor(TableEntityPart nodePart) {
        if (nodePart == null) {
            mapperUI.getTabFolderEditors().getStyledTextHandler().setTextWithoutNotifyListeners("");
            mapperUI.getTabFolderEditors().getStyledTextHandler().getStyledText().setEnabled(false);
            mapperUI.getTabFolderEditors().getStyledTextHandler().getStyledText().setEditable(false);
            return;
        }
        AbstractNode node = (AbstractNode) nodePart.getModel();
        if (node instanceof TreeNode) {
            if (!((TreeNode) node).getChildren().isEmpty()) {
                mapperUI.getTabFolderEditors().getStyledTextHandler().setTextWithoutNotifyListeners("");
                mapperUI.getTabFolderEditors().getStyledTextHandler().getStyledText().setEnabled(false);
                mapperUI.getTabFolderEditors().getStyledTextHandler().getStyledText().setEditable(false);
                return;
            }
        }

        String expression = node.getExpression();
        if (expression == null) {
            expression = "";
        }
        mapperUI.getTabFolderEditors().getStyledTextHandler().setTextWithoutNotifyListeners(expression);
        mapperUI.getTabFolderEditors().getStyledTextHandler().getStyledText().setEnabled(true);
        mapperUI.getTabFolderEditors().getStyledTextHandler().getStyledText().setEditable(true);
        mapperUI.getTabFolderEditors().getStyledTextHandler().setSelectedNodePart(nodePart);
    }

    public void selectInputXmlTree(InputXmlTree inputTree) {
        if (inputTree != selectedInputTree) {
            selectedInputTree = inputTree;
            MetadataTableEditorView inputMetaEditorView = mapperUI.getTabFolderEditors().getInputMetaEditorView();
            List<IODataComponent> inputs = getMapperComponent().getIODataComponents().getInputs();
            IMetadataTable table = null;
            for (int i = 0; i < inputs.size(); i++) {
                IODataComponent ioDataComponent = inputs.get(i);
                if (inputTree.getName() != null && inputTree.getName().equals(ioDataComponent.getConnection().getName())) {
                    table = ioDataComponent.getTable();
                    break;
                }
            }
            if (table != null) {
                MetadataTableEditor editor = new MetadataTableEditor(table, selectedInputTree.getName());
                editor.setModifiedBeanListenable(inputMetaEditorView.getTableViewerCreator());
                IModifiedBeanListener<IMetadataColumn> columnListener = new IModifiedBeanListener<IMetadataColumn>() {

                    @Override
                    public void handleEvent(ModifiedBeanEvent<IMetadataColumn> event) {
                        fireCurrentDirectEditApply();
                        if (AbstractMetadataTableEditorView.ID_COLUMN_NAME.equals(event.column.getId())) {
                            if (event.index < selectedInputTree.getNodes().size()) {
                                TreeNode treeNode = selectedInputTree.getNodes().get(event.index);
                                if (treeNode != null) {
                                    treeNode.setName((String) event.newValue);
                                    processColumnNameChanged(treeNode);
                                }
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_TYPE.equals(event.column.getId())) {
                            if (event.index < selectedInputTree.getNodes().size()) {
                                TreeNode treeNode = selectedInputTree.getNodes().get(event.index);
                                if (treeNode != null) {
                                    if (treeNode.getType() != null && treeNode.getType().equals(XmlMapUtil.DOCUMENT)) {
                                        List<TreeNode> oldLoops = new ArrayList<TreeNode>();
                                        if (selectedInputTree == mainInputTree && mainInputTree.isMultiLoops()) {
                                            oldLoops = XmlMapUtil.getMultiLoopsForXmlTree(selectedInputTree);
                                        }
                                        XmlMapUtil.detachNodeConnections(treeNode, getExternalData(), true);
                                        treeNode.getChildren().clear();
                                        if (selectedInputTree == mainInputTree && mainInputTree.isMultiLoops()) {
                                            selectedInputTree.setMultiLoops(XmlMapUtil.checkMultiLoopsStatus(selectedInputTree));
                                            List<TreeNode> newLoops = XmlMapUtil.getMultiLoopsForXmlTree(selectedInputTree);
                                            oldLoops.removeAll(newLoops);
                                            if (!oldLoops.isEmpty()) {
                                                XmlMapUtil.removeloopInOutputTree(MapperManager.this, oldLoops);
                                            }
                                        }
                                    }
                                    treeNode.setType((String) event.newValue);

                                    if (XmlMapUtil.DOCUMENT.equals(event.newValue)) {
                                        XmlMapUtil.detachNodeConnections(treeNode, getExternalData(), true);
                                        TreeNode createTreeNode = XmlmapFactory.eINSTANCE.createTreeNode();
                                        createTreeNode.setName("root");
                                        createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                                        createTreeNode.setNodeType(NodeType.ELEMENT);
                                        createTreeNode.setXpath(XmlMapUtil.getXPath(treeNode.getXpath(),
                                                createTreeNode.getName(), createTreeNode.getNodeType()));
                                        createTreeNode.setLoop(true);
                                        createTreeNode.setMain(true);
                                        treeNode.getChildren().add(createTreeNode);

                                    }
                                    problemsAnalyser.checkProblems(selectedInputTree);
                                    mapperUI.updateStatusBar();
                                    inputTreeSchemaBeanListModified();
                                }
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_KEY.equals(event.column.getId())) {
                            if (event.index < selectedInputTree.getNodes().size()) {
                                TreeNode treeNode = selectedInputTree.getNodes().get(event.index);
                                treeNode.setKey((Boolean) event.newValue);
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_PATTERN.equals(event.column.getId())) {
                            if (event.index < selectedInputTree.getNodes().size()) {
                                TreeNode treeNode = selectedInputTree.getNodes().get(event.index);
                                treeNode.setPattern((String) event.newValue);
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_NULLABLE.equals(event.column.getId())) {
                            if (event.index < selectedInputTree.getNodes().size()) {
                                TreeNode treeNode = selectedInputTree.getNodes().get(event.index);
                                treeNode.setNullable((Boolean) event.newValue);
                            }
                        }

                    }

                };
                editor.addModifiedBeanListener(columnListener);

                editor.addAfterOperationListListener(new IListenableListListener() {

                    @Override
                    public void handleEvent(ListenableListEvent event) {
                        if (event.type == TYPE.ADDED) {
                            EList<TreeNode> nodes = selectedInputTree.getNodes();
                            List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.addedObjects;
                            if (event.index != null) {
                                int index = event.index;
                                for (IMetadataColumn column : metadataColumns) {
                                    TreeNode createTreeNode = XmlmapFactory.eINSTANCE.createTreeNode();
                                    createTreeNode.setName(column.getLabel());
                                    createTreeNode.setType(column.getTalendType());
                                    createTreeNode.setNullable(column.isNullable());
                                    createTreeNode.setPattern(column.getPattern());
                                    createTreeNode.setXpath(XmlMapUtil.getXPath(selectedInputTree.getName(),
                                            createTreeNode.getName(), createTreeNode.getNodeType()));

                                    selectedInputTree.getNodes().add(index, createTreeNode);
                                    index = index + 1;
                                }
                            }

                        } else if (event.type == TYPE.REMOVED) {
                            List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.removedObjects;
                            List treeNodeToRemove = new ArrayList();
                            for (IMetadataColumn column : metadataColumns) {
                                for (TreeNode node : selectedInputTree.getNodes()) {
                                    if (node.getName() != null && node.getName().equals(column.getLabel())) {
                                        XmlMapUtil.detachNodeConnections(node, getExternalData(), true);
                                        treeNodeToRemove.add(node);
                                    }
                                }
                            }
                            selectedInputTree.getNodes().removeAll(treeNodeToRemove);
                            if (selectedInputTree == mainInputTree && mainInputTree.isMultiLoops()) {
                                selectedInputTree.setMultiLoops(XmlMapUtil.checkMultiLoopsStatus(selectedInputTree));
                                List<TreeNode> oldLoops = new ArrayList<TreeNode>();
                                XmlMapUtil.getChildLoops(oldLoops, treeNodeToRemove);
                                if (!oldLoops.isEmpty()) {
                                    XmlMapUtil.removeloopInOutputTree(MapperManager.this, oldLoops);
                                }
                            }
                            problemsAnalyser.checkProblems(selectedInputTree);
                            mapperUI.updateStatusBar();
                            inputTreeSchemaBeanListModified();
                        } else if (event.type == TYPE.SWAPED) {
                            List<Integer> listIndexTarget = event.indicesTarget;
                            List<Integer> listIndexOrignal = event.indicesOrigin;
                            for (int i = 0; i < listIndexOrignal.size(); i++) {
                                int orignal = listIndexOrignal.get(i);
                                int target = listIndexTarget.get(i);
                                if (orignal < selectedInputTree.getNodes().size()) {
                                    TreeNode tempTreeNode = selectedInputTree.getNodes().get(orignal);
                                    selectedInputTree.getNodes().remove(orignal);
                                    selectedInputTree.getNodes().add(target, tempTreeNode);
                                }
                            }

                        } else if (event.type == TYPE.REPLACED) {
                            // fix for TDI-24071
                            List added = (List) event.addedObjects;
                            List removed = (List) event.removedObjects;

                            boolean needCheckOutput = false;
                            List<TreeNode> oldLoops = new ArrayList<TreeNode>();
                            if (selectedInputTree == mainInputTree && mainInputTree.isMultiLoops()) {
                                needCheckOutput = true;
                            }

                            List<IMetadataColumn> removedColumn = new ArrayList<IMetadataColumn>();
                            List<IMetadataColumn> addedColumn = new ArrayList<IMetadataColumn>();
                            if (!added.isEmpty()) {
                                addedColumn.addAll((List<IMetadataColumn>) added.get(0));
                            }
                            if (!removed.isEmpty()) {
                                removedColumn.addAll((List<IMetadataColumn>) removed.get(0));
                            }
                            Map<IMetadataColumn, TreeNode> nodeMap = new HashMap<IMetadataColumn, TreeNode>();
                            for (int i = 0; i < removedColumn.size(); i++) {
                                IMetadataColumn column = removedColumn.get(i);
                                TreeNode node = selectedInputTree.getNodes().get(i);
                                boolean found = false;
                                for (IMetadataColumn columnAdded : addedColumn) {
                                    if (column.sameMetacolumnAs(columnAdded)) {
                                        nodeMap.put(columnAdded, node);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    XmlMapUtil.detachNodeConnections(node, getExternalData(), true);
                                    // get old loops if doc column need to be delete
                                    if (needCheckOutput) {
                                        XmlMapUtil.getChildLoops(oldLoops, node.getChildren());
                                    }
                                }
                            }

                            // remove all
                            selectedInputTree.getNodes().clear();
                            // add all
                            for (IMetadataColumn column : addedColumn) {
                                TreeNode treeNode = nodeMap.get(column);
                                if (treeNode == null) {
                                    treeNode = XmlmapFactory.eINSTANCE.createTreeNode();
                                    treeNode.setName(column.getLabel());
                                    treeNode.setType(column.getTalendType());
                                    treeNode.setNullable(column.isNullable());
                                    treeNode.setPattern(column.getPattern());
                                    treeNode.setXpath(XmlMapUtil.getXPath(selectedInputTree.getName(), treeNode.getName(),
                                            treeNode.getNodeType()));
                                    if (XmlMapUtil.DOCUMENT.equals(column.getTalendType())) {
                                        TreeNode createTreeNode = XmlmapFactory.eINSTANCE.createTreeNode();
                                        createTreeNode.setName("root");
                                        createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                                        createTreeNode.setNodeType(NodeType.ELEMENT);
                                        createTreeNode.setXpath(XmlMapUtil.getXPath(treeNode.getXpath(),
                                                createTreeNode.getName(), createTreeNode.getNodeType()));
                                        createTreeNode.setLoop(true);
                                        createTreeNode.setMain(true);
                                        treeNode.getChildren().add(createTreeNode);
                                    }
                                }

                                selectedInputTree.getNodes().add(treeNode);
                            }
                            // for multi loops ,incase the doc node with multi loop is deleted
                            selectedInputTree.setMultiLoops(XmlMapUtil.checkMultiLoopsStatus(selectedInputTree));
                            if (!oldLoops.isEmpty()) {
                                XmlMapUtil.removeloopInOutputTree(MapperManager.this, oldLoops);
                            }
                            mapperUI.updateStatusBar();
                        }
                    }

                });

                inputMetaEditorView.setMetadataTableEditor(editor);
            }

            refreshTreeSchemaEditor(inputTree);
        }

    }

    private void processColumnNameChanged(final TreeNode treeNode) {
        XmlMapUtil.updateXPathAndExpression(getExternalData(), getMapperComponent().getExpressionManager(), treeNode,
                treeNode.getName(), XmlMapUtil.getXPathLength(treeNode.getXpath()), true);
        // if (treeNode instanceof OutputTreeNode) {
        // getMapperUI().getTabFolderEditors().getOutputTreeSchemaEditor().refresh();
        // } else {
        // getMapperUI().getTabFolderEditors().getInputTreeSchemaEditor().refresh();
        // }
    }

    public void selectOutputXmlTree(OutputXmlTree outputTree) {
        if (outputTree != selectedOutputTree) {
            selectedOutputTree = outputTree;
            MetadataTableEditorView outputMetaEditorView = mapperUI.getTabFolderEditors().getOutputMetaEditorView();
            List<IMetadataTable> metadataList = getMapperComponent().getMetadataList();
            IMetadataTable table = null;
            for (int i = 0; i < metadataList.size(); i++) {
                if (outputTree.getName() != null && outputTree.getName().equals(metadataList.get(i).getTableName())) {
                    table = metadataList.get(i);
                    break;
                }
            }
            if (outputTree.isErrorReject()) {
                for (IMetadataColumn column : table.getListColumns()) {
                    if (ERROR_REJECT_MESSAGE.equals(column.getLabel()) || ERROR_REJECT_STACK_TRACE.equals(column.getLabel())) {
                        column.setCustom(true);
                    }
                }

                CustomTableManager.addCustomManagementToTable(mapperUI.getTabFolderEditors().getOutputMetaEditorView(), true);
            }

            if (table != null) {
                MetadataTableEditor editor = new MetadataTableEditor(table, table.getTableName());
                outputMetaEditorView.setMetadataTableEditor(editor);
                editor.setModifiedBeanListenable(outputMetaEditorView.getTableViewerCreator());

                IModifiedBeanListener<IMetadataColumn> columnListener = new IModifiedBeanListener<IMetadataColumn>() {

                    @Override
                    public void handleEvent(ModifiedBeanEvent<IMetadataColumn> event) {
                        fireCurrentDirectEditApply();
                        if (AbstractMetadataTableEditorView.ID_COLUMN_NAME.equals(event.column.getId())) {
                            if (event.index < selectedOutputTree.getNodes().size()) {
                                TreeNode treeNode = selectedOutputTree.getNodes().get(event.index);
                                if (treeNode != null) {
                                    treeNode.setName((String) event.newValue);
                                    processColumnNameChanged(treeNode);
                                }
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_TYPE.equals(event.column.getId())) {
                            if (event.index < selectedOutputTree.getNodes().size()) {
                                OutputTreeNode treeNode = selectedOutputTree.getNodes().get(event.index);
                                if (treeNode != null) {
                                    String oldValue = treeNode.getType();

                                    treeNode.setType((String) event.newValue);

                                    if (oldValue != null && oldValue.equals(XmlMapUtil.DOCUMENT)) {
                                        XmlMapUtil.detachNodeConnections(treeNode, getExternalData(), true);
                                        List<TreeNode> removedLoops = new ArrayList<TreeNode>();
                                        if (mainInputTree != null && mainInputTree.isMultiLoops()) {
                                            XmlMapUtil.getChildLoops(removedLoops, treeNode.getChildren());
                                        }
                                        treeNode.getChildren().clear();
                                        selectedOutputTree.setMultiLoops(XmlMapUtil.checkMultiLoopsStatus(selectedOutputTree));
                                        if (mainInputTree != null && mainInputTree.isMultiLoops()) {
                                            if (XmlMapUtil.hasDocument(selectedOutputTree)) {
                                                XmlMapUtil.removeLoopTableForOutput(selectedOutputTree, removedLoops,
                                                        mainInputTree == null ? false : mainInputTree.isMultiLoops());
                                            } else {
                                                selectedOutputTree.getInputLoopNodesTables().clear();
                                                selectedOutputTree.getInputLoopNodesTables().add(
                                                        XmlmapFactory.eINSTANCE.createInputLoopNodesTable());
                                            }
                                        }

                                    }

                                    if (XmlMapUtil.DOCUMENT.equals(event.newValue)) {
                                        XmlMapUtil.detachNodeConnections(treeNode, getExternalData(), true);
                                        OutputTreeNode createTreeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                                        createTreeNode.setName("root");
                                        createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                                        createTreeNode.setNodeType(NodeType.ELEMENT);
                                        createTreeNode.setXpath(XmlMapUtil.getXPath(treeNode.getXpath(),
                                                createTreeNode.getName(), createTreeNode.getNodeType()));
                                        createTreeNode.setLoop(true);
                                        createTreeNode.setMain(true);
                                        treeNode.getChildren().add(createTreeNode);

                                    }
                                    problemsAnalyser.checkProblems(selectedOutputTree);
                                    mapperUI.updateStatusBar();
                                    outputTreeSchemaBeanListModified();
                                }
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_KEY.equals(event.column.getId())) {
                            if (event.index < selectedOutputTree.getNodes().size()) {
                                TreeNode treeNode = selectedOutputTree.getNodes().get(event.index);
                                treeNode.setKey((Boolean) event.newValue);
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_PATTERN.equals(event.column.getId())) {
                            if (event.index < selectedOutputTree.getNodes().size()) {
                                TreeNode treeNode = selectedOutputTree.getNodes().get(event.index);
                                treeNode.setPattern((String) event.newValue);
                            }
                        } else if (AbstractMetadataTableEditorView.ID_COLUMN_NULLABLE.equals(event.column.getId())) {
                            if (event.index < selectedOutputTree.getNodes().size()) {
                                TreeNode treeNode = selectedOutputTree.getNodes().get(event.index);
                                treeNode.setNullable((Boolean) event.newValue);
                            }
                        }

                    }

                };
                editor.addModifiedBeanListener(columnListener);

                editor.addAfterOperationListListener(new IListenableListListener() {

                    @Override
                    public void handleEvent(ListenableListEvent event) {

                        if (event.type == TYPE.ADDED) {
                            EList<OutputTreeNode> nodes = selectedOutputTree.getNodes();
                            List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.addedObjects;
                            if (event.index != null) {
                                int index = event.index;
                                for (IMetadataColumn column : metadataColumns) {
                                    OutputTreeNode createTreeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                                    createTreeNode.setName(column.getLabel());
                                    createTreeNode.setType(column.getTalendType());
                                    createTreeNode.setNullable(column.isNullable());
                                    createTreeNode.setPattern(column.getPattern());
                                    createTreeNode.setXpath(XmlMapUtil.getXPath(selectedOutputTree.getName(),
                                            createTreeNode.getName(), createTreeNode.getNodeType()));

                                    selectedOutputTree.getNodes().add(index, createTreeNode);
                                    index = index + 1;
                                }
                            }

                        } else if (event.type == TYPE.REMOVED) {
                            List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.removedObjects;
                            List treeNodeToRemove = new ArrayList();
                            for (IMetadataColumn column : metadataColumns) {
                                for (TreeNode node : selectedOutputTree.getNodes()) {
                                    if (node.getName() != null && node.getName().equals(column.getLabel())) {
                                        XmlMapUtil.detachNodeConnections(node, getExternalData(), true);
                                        treeNodeToRemove.add(node);
                                    }
                                }
                            }
                            selectedOutputTree.getNodes().removeAll(treeNodeToRemove);
                            problemsAnalyser.checkProblems(selectedOutputTree);
                            mapperUI.updateStatusBar();
                            outputTreeSchemaBeanListModified();
                        } else if (event.type == TYPE.SWAPED) {
                            List<Integer> listIndexTarget = event.indicesTarget;
                            List<Integer> listIndexOrignal = event.indicesOrigin;
                            for (int i = 0; i < listIndexOrignal.size(); i++) {
                                int orignal = listIndexOrignal.get(i);
                                int target = listIndexTarget.get(i);
                                if (orignal < selectedOutputTree.getNodes().size()) {
                                    OutputTreeNode tempTreeNode = selectedOutputTree.getNodes().get(orignal);
                                    selectedOutputTree.getNodes().remove(orignal);
                                    selectedOutputTree.getNodes().add(target, tempTreeNode);
                                }
                            }

                        } else if (event.type == TYPE.REPLACED) {
                            // fix for TDI-24071
                            List added = (List) event.addedObjects;
                            List removed = (List) event.removedObjects;

                            List<IMetadataColumn> removedColumn = new ArrayList<IMetadataColumn>();
                            List<IMetadataColumn> addedColumn = new ArrayList<IMetadataColumn>();
                            if (!added.isEmpty()) {
                                addedColumn.addAll((List<IMetadataColumn>) added.get(0));
                            }
                            if (!removed.isEmpty()) {
                                removedColumn.addAll((List<IMetadataColumn>) removed.get(0));
                            }
                            Map<IMetadataColumn, OutputTreeNode> nodeMap = new HashMap<IMetadataColumn, OutputTreeNode>();
                            for (int i = 0; i < removedColumn.size(); i++) {
                                IMetadataColumn column = removedColumn.get(i);
                                OutputTreeNode node = selectedOutputTree.getNodes().get(i);
                                boolean found = false;
                                for (IMetadataColumn columnAdded : addedColumn) {
                                    if (column.sameMetacolumnAs(columnAdded)) {
                                        nodeMap.put(columnAdded, node);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    XmlMapUtil.detachNodeConnections(node, getExternalData(), true);
                                }
                            }
                            // remove all
                            boolean needCheck = false;
                            if (selectedOutputTree.isMultiLoops()) {
                                needCheck = true;
                            }
                            selectedOutputTree.getNodes().clear();
                            // add all
                            for (IMetadataColumn column : addedColumn) {
                                OutputTreeNode treeNode = nodeMap.get(column);
                                if (treeNode == null) {
                                    treeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                                    treeNode.setName(column.getLabel());
                                    treeNode.setType(column.getTalendType());
                                    treeNode.setNullable(column.isNullable());
                                    treeNode.setPattern(column.getPattern());
                                    treeNode.setXpath(XmlMapUtil.getXPath(selectedInputTree.getName(), treeNode.getName(),
                                            treeNode.getNodeType()));
                                    if (XmlMapUtil.DOCUMENT.equals(column.getTalendType())) {
                                        TreeNode createTreeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                                        createTreeNode.setName("root");
                                        createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                                        createTreeNode.setNodeType(NodeType.ELEMENT);
                                        createTreeNode.setXpath(XmlMapUtil.getXPath(treeNode.getXpath(),
                                                createTreeNode.getName(), createTreeNode.getNodeType()));
                                        createTreeNode.setLoop(true);
                                        createTreeNode.setMain(true);
                                        treeNode.getChildren().add(createTreeNode);
                                    }
                                }
                                selectedOutputTree.getNodes().add(treeNode);
                            }
                            if (needCheck) {
                                selectedOutputTree.setMultiLoops(XmlMapUtil.checkMultiLoopsStatus(selectedOutputTree));
                            }
                            mapperUI.updateStatusBar();
                            mapperUI.updateStatusBar();
                        }

                    }

                });
            }
            refreshTreeSchemaEditor(outputTree);
        }

    }

    public boolean isDieOnError() {
        return isDieOnError;
    }

    public void setDieOnError(boolean isDieOnError) {
        this.isDieOnError = isDieOnError;
    }

    public ProblemsAnalyser getProblemsAnalyser() {
        return this.problemsAnalyser;
    }

    public void setCurrentDirectEditManager(XmlMapNodeDirectEditManager currentDirectEditManager) {
        this.currentDirectEditManager = currentDirectEditManager;
    }

    public void fireCurrentDirectEditApply() {
        if (currentDirectEditManager != null) {
            CellEditor cellEditor = currentDirectEditManager.getCellEditor();
            if (cellEditor instanceof ExpressionCellEditor) {
                ((ExpressionCellEditor) cellEditor).fireApplyEditorValue();
            }
        }
        currentDirectEditManager = null;
    }

    public InputXmlTree getMainInputTree() {
        return mainInputTree;
    }

    public boolean isMainTableMultiLoop() {
        return mainInputTree != null && mainInputTree.isMultiLoops();
    }

    /**
     *
     * DOC refresh column when property of treenode is changed
     *
     * @param treeNode
     */
    public void refreshTreeSchemaColumn(TreeNode treeNode) {
        TableViewerCreator<TreeNode> tableViewerCreator = null;
        if (treeNode instanceof OutputTreeNode) {
            tableViewerCreator = mapperUI.getTabFolderEditors().getOutputTreeSchemaEditor().getTableViewerCreator();

        } else {
            tableViewerCreator = mapperUI.getTabFolderEditors().getInputTreeSchemaEditor().getTableViewerCreator();

        }
        if (tableViewerCreator != null && tableViewerCreator.getTable() != null && !tableViewerCreator.getTable().isDisposed()) {
            tableViewerCreator.getTableViewer().refresh(treeNode);
        }

    }

    private void outputTreeSchemaBeanListModified() {
        ExtendedTableModel<TreeNode> extendedTableModel = mapperUI.getTabFolderEditors().getOutputTreeSchemaEditor()
                .getExtendedTableModel();
        if (extendedTableModel != null) {
            ((TreeSchemaTableEditor) extendedTableModel).beanListModified();
        }
    }

    private void inputTreeSchemaBeanListModified() {
        ExtendedTableModel<TreeNode> extendedTableModel = mapperUI.getTabFolderEditors().getInputTreeSchemaEditor()
                .getExtendedTableModel();
        if (extendedTableModel != null) {
            ((TreeSchemaTableEditor) extendedTableModel).beanListModified();
        }
    }

    /**
     *
     * DOC Administrator Comment method "refreshTreeSchemaEditor".
     *
     * @param outputTree
     */
    public void refreshTreeSchemaEditor(AbstractInOutTree outputTree) {
        XmlTreeSchemaTableView outputTreeSchemaEditor = null;
        if (outputTree instanceof InputXmlTree) {
            outputTreeSchemaEditor = mapperUI.getTabFolderEditors().getInputTreeSchemaEditor();
        } else {
            outputTreeSchemaEditor = mapperUI.getTabFolderEditors().getOutputTreeSchemaEditor();
        }

        TreeSchemaTableEditor editor = new TreeSchemaTableEditor(outputTree);
        outputTreeSchemaEditor.setExtendedTableModel(editor);
        // editor.setModifiedBeanListenable(outputTreeSchemaEditor.getTableViewerCreator());

    }

    public void beanListModified(boolean input) {
        XmlTreeSchemaTableView outputTreeSchemaEditor = null;
        if (input) {
            outputTreeSchemaEditor = mapperUI.getTabFolderEditors().getInputTreeSchemaEditor();
        } else {
            outputTreeSchemaEditor = mapperUI.getTabFolderEditors().getOutputTreeSchemaEditor();
        }

        outputTreeSchemaEditor.getExtendedTableModel().beanListModified();
    }

    public IFigure getSelectedFigure() {
        return this.selectedFigure;
    }

    public void setSelectedFigure(IFigure selectedFigure) {
        this.selectedFigure = selectedFigure;
    }
}
