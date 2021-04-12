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
package org.talend.repository.json.ui.wizards.view;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.swt.dnd.LocalDataTransfer;
import org.talend.commons.ui.swt.dnd.LocalDraggedData;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.metadata.managment.ui.wizard.metadata.xml.dialog.DragAndDrogDialog;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * wzhang class global comment. Detailled comment
 */
public class JSONFileDragAndDropHandler {

    private JSONFileSchema2TreeLinker linker;

    private DragSource dragSource;

    private DropTarget loopDropTarget;

    public JSONFileDragAndDropHandler(JSONFileSchema2TreeLinker linker) {
        this.linker = linker;
        init();
    }

    private void init() {
        createDragSource();
        createDropTarget();
    }

    private AbstractJSONStepForm getMainForm() {
        return linker.getForm();
    }

    private void createDragSource() {
        if (dragSource != null) {
            dragSource.dispose();
        }

        dragSource = new DragSource(linker.getSource(), DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dragSource.setTransfer(new Transfer[] { LocalDataTransfer.getInstance() });

        DragSourceListener sourceListener = new TreeDragSourceListener();
        dragSource.addDragListener(sourceListener);
    }

    private void createDropTarget() {

        if (loopDropTarget != null) {
            loopDropTarget.dispose();
        }
        loopDropTarget = new DropTarget(linker.getTarget(), DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        loopDropTarget.setTransfer(new Transfer[] { LocalDataTransfer.getInstance() });
        DropTargetListener targetListener = new TableDropTargetListener();
        loopDropTarget.addDropListener(targetListener);

    }

    /**
     *
     * DOC wzhang XmlFileDragAndDropHandler class global comment. Detailled comment
     */

    class TreeDragSourceListener implements TransferDragSourceListener {

        @Override
        public void dragFinished(DragSourceEvent event) {
            event.getSource();
        }

        @Override
        public void dragSetData(DragSourceEvent event) {
            event.getSource();
        }

        @Override
        public void dragStart(DragSourceEvent event) {
            TableItem[] items = linker.getSource().getSelection();
            if (items.length == 0) {
                event.doit = false;
            } else {
                LocalDraggedData draggedData = new LocalDraggedData();
                for (TableItem tableItem : items) {
                    draggedData.add(tableItem.getData());
                }
                draggedData.setTable(getMainForm().getMetadataTable());
                LocalDataTransfer.getInstance().setLocalDraggedData(draggedData);
            }

        }

        @Override
        public Transfer getTransfer() {
            return LocalDataTransfer.getInstance();
        }
    }

    /**
     *
     * DOC wzhang XmlFileDragAndDropHandler class global comment. Detailled comment
     */
    class TableDropTargetListener implements TransferDropTargetListener {

        @Override
        public void dragEnter(DropTargetEvent event) {

        }

        @Override
        public void dragLeave(DropTargetEvent event) {

        }

        @Override
        public void dragOperationChanged(DropTargetEvent event) {
        }

        @Override
        public void dropAccept(DropTargetEvent event) {

        }

        @Override
        public Transfer getTransfer() {
            return LocalDataTransfer.getInstance();
        }

        @Override
        public boolean isEnabled(DropTargetEvent event) {
            return true;
        }

        @Override
        public void dragOver(DropTargetEvent event) {
            Item targetItem = (Item) event.item;
            if (targetItem == null) {
                event.detail = DND.DROP_NONE;
                return;
            }
            FOXTreeNode targetNode = (FOXTreeNode) (targetItem.getData());
            LocalDraggedData draggedData = LocalDataTransfer.getInstance().getDraggedData();
            List<Object> dragdedData = draggedData.getTransferableEntryList();
            if (dragdedData.size() == 1 && isDropRelatedColumn(event)) {
                if (targetNode instanceof Element) {
                    Element element = (Element) targetNode;
                    if (!element.getElementChildren().isEmpty() || element.getParent() == null) {
                        event.detail = DND.DROP_NONE;
                        return;
                    }
                } else {
                    FOXTreeNode parent = targetNode.getParent();
                    if (parent == null) {
                        event.detail = DND.DROP_NONE;
                        return;
                    }
                }
            }
            event.detail = DND.DROP_LINK;
        }

        private boolean isDropRelatedColumn(DropTargetEvent event) {
            DropTarget dropTarget = (DropTarget) event.getSource();
            Display display = event.display;
            Control control = dropTarget.getControl();
            TreeItem item = (TreeItem) event.item;
            Rectangle rec = display.map(control, null, item.getBounds(1));
            if ((event.x >= rec.x) && (event.y >= rec.y) && ((event.x - rec.x) <= rec.width) && ((event.y - rec.y) <= rec.height)) {
                return true;
            }
            return false;
        }

        private void setDefaultFixValue(FOXTreeNode treeNode) {
            String fixValue = treeNode.getDefaultValue();
            if (fixValue == null) {
                return;
            }
            treeNode.setDefaultValue(null);
        }

        @Override
        public void drop(DropTargetEvent event) {
            DropTarget dropTarget = (DropTarget) event.getSource();
            Item targetItem = (Item) event.item;
            if (targetItem == null) {
                return;
            }
            Control control = dropTarget.getControl();
            LocalDraggedData draggedData = LocalDataTransfer.getInstance().getDraggedData();
            List<Object> dragdedData = draggedData.getTransferableEntryList();
            IMetadataTable table = null;
            if (draggedData.getTable() instanceof MetadataTable) {
                table = ConvertionHelper.convert((MetadataTable) draggedData.getTable());
            }

            FOXTreeNode targetNode = (FOXTreeNode) (targetItem.getData());

            if (dragdedData.size() == 1 && isDropRelatedColumn(event)) {
                // if (!targetNode.hasChildren()) {
                // IMetadataColumn metaColumn = (IMetadataColumn) dragdedData.get(0);
                IMetadataColumn metaColumn = ConvertionHelper.convertToIMetaDataColumn((MetadataColumn) dragdedData.get(0));
                targetNode.setDefaultValue(null);
                targetNode.setColumn(metaColumn);
                targetNode.setTable(table);
                targetNode.setDataType(metaColumn.getTalendType());
                // targetNode.setRow(row);

                linker.getJSONViewer().refresh(targetNode);
                linker.getJSONViewer().expandAll();

                Display display = linker.getSource().getDisplay();
                Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                linker.getSource().getShell().setCursor(cursor);

                linker.valuedChanged(targetItem);

                linker.getSource().getShell().setCursor(null);
                // }
            } else if (dragdedData.size() > 0) {
                DragAndDrogDialog dialog = new DragAndDrogDialog(null);
                dialog.open();
                if (dialog.getReturnCode() == IDialogConstants.CANCEL_ID) {
                    return;
                }

                if (dialog.getSelectValue().equals(DragAndDrogDialog.CREATE_AS_TEXT)) {
                    if (hasElementChildren(targetNode)) {
                        List<FOXTreeNode> children = targetNode.getChildren();
                        for (FOXTreeNode foxTreeNode : children) {
                            if (!(foxTreeNode instanceof Attribute) && !(foxTreeNode instanceof NameSpaceNode)) {
                                MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel()
                                        + "\" has element children, can not have linker.");
                                return;
                            }
                        }
                    }
                    // else if (targetNode.getParent() == null) {
                    // MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel()
                    // + "\" is root, can not have linker.");
                    // return;
                    // }
                    // IMetadataColumn metaColumn = (IMetadataColumn) dragdedData.get(0);
                    IMetadataColumn metaColumn = ConvertionHelper.convertToIMetaDataColumn((MetadataColumn) dragdedData.get(0));

                    targetNode.setColumn(metaColumn);
                    targetNode.setDataType(metaColumn.getTalendType());
                    targetNode.setDefaultValue(null);

                } else if (dialog.getSelectValue().equals(DragAndDrogDialog.CREATE_AS_SUBELEMENT)) {
                    if (!(targetNode instanceof Element)) {
                        MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel()
                                + "\" isn't a Element, can not create sub-elements or attributes.");
                        return;
                    }
                    if (targetNode.getColumn() != null) {
                        if (!MessageDialog.openConfirm(control.getShell(), "Warning",
                                "Do you want to disconnect the existing linker and then add an sub element for the selected element \""
                                        + targetNode.getLabel() + "\"?")) {
                            return;
                        }
                        targetNode.setColumn(null);
                    }
                    for (Object obj : dragdedData) {
                        // IMetadataColumn metaColumn = (IMetadataColumn) obj;
                        IMetadataColumn metaColumn = ConvertionHelper.convertToIMetaDataColumn((MetadataColumn) obj);

                        boolean isContain = false;
                        for (FOXTreeNode node : ((Element) targetNode).getElementChildren()) {
                            if (node.getLabel().equals(metaColumn.getLabel())) {
                                node.setColumn(metaColumn);
                                node.setDataType(metaColumn.getTalendType());
                                // node.setRow(row);
                                node.setTable(table);
                                setDefaultFixValue(node);
                                isContain = true;
                            }
                        }
                        if (!isContain) {
                            FOXTreeNode child = new Element(metaColumn.getLabel());
                            child.setColumn(metaColumn);
                            child.setDataType(metaColumn.getTalendType());
                            child.setTable(table);
                            // child.setRow(row);
                            targetNode.addChild(child);
                        }
                    }
                    if (targetNode instanceof Element && targetNode.getParent() == null && targetNode.isLoop()) {
                        targetNode.setLoop(false);
                    }
                    setDefaultFixValue(targetNode);
                } else if (dialog.getSelectValue().equals(DragAndDrogDialog.CREATE_AS_ATTRIBUTE)) {
                    if (!(targetNode instanceof Element)) {
                        MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel()
                                + "\" isn't a Element, can not create sub-elements or attributes.");
                        return;
                    }
                    for (Object obj : dragdedData) {
                        // IMetadataColumn metaColumn = (IMetadataColumn) obj;
                        IMetadataColumn metaColumn = ConvertionHelper.convertToIMetaDataColumn((MetadataColumn) obj);

                        boolean isContain = false;
                        for (FOXTreeNode node : ((Element) targetNode).getAttributeChildren()) {
                            if (node.getLabel().equals(metaColumn.getLabel())) {
                                node.setColumn(metaColumn);
                                node.setTable(table);
                                node.setDataType(metaColumn.getTalendType());
                                // node.setRow(row);
                                setDefaultFixValue(node);
                                isContain = true;
                            }
                        }
                        if (!isContain) {
                            FOXTreeNode child = new Attribute(metaColumn.getLabel());
                            child.setColumn(metaColumn);
                            child.setDataType(metaColumn.getTalendType());
                            child.setTable(table);
                            // child.setRow(row);
                            targetNode.addChild(child);
                        }
                    }
                    // setDefaultFixValue(targetNode);
                }
                linker.getJSONViewer().refresh();
                linker.getJSONViewer().expandAll();

                Display display = linker.getSource().getDisplay();
                Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                linker.getSource().getShell().setCursor(cursor);

                linker.valuedChanged(targetItem);

                linker.getSource().getShell().setCursor(null);
            }
            linker.getJSONViewer().refresh();
            linker.getJSONViewer().expandAll();
            linker.updateLinksStyleAndControlsSelection(control, true);
            linker.getForm().updateConnection();
            linker.getForm().updateStatus();
        }
    }

    private boolean hasElementChildren(FOXTreeNode node) {
        boolean haveElementChild = false;
        for (FOXTreeNode child : node.getChildren()) {
            if (child instanceof Element) {
                haveElementChild = true;
                break;
            }
        }
        return haveElementChild;
    }

}
