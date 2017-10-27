// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui.edit;

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
import org.talend.core.model.process.IConnection;
import org.talend.designer.hl7.managers.HL7Manager;
import org.talend.designer.hl7.managers.HL7OutputManager;
import org.talend.designer.hl7.ui.data.Attribute;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.designer.hl7.ui.dialog.HL7DragAndDrogDialog;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class SchemaXMLDragAndDropHandler {

    private SchemaXMLLinker linker;

    private DragSource dragSource;

    protected int dropDefaultOperation = DND.DROP_LINK;

    private DropTarget loopDropTarget;

    /**
     * amaumont TreeToTableDragAndDropHandler constructor comment.
     * 
     * @param linker
     */
    public SchemaXMLDragAndDropHandler(SchemaXMLLinker linker) {
        this.linker = linker;
        init();
    }

    /**
     * amaumont Comment method "init".
     */
    private void init() {
        createDragSource();
        createDropTarget();
    }

    /**
     * 
     * amaumont Comment method "createDragSource".
     * 
     * @param sourceListener
     */
    private void createDragSource() {
        if (dragSource != null) {
            dragSource.dispose();
        }

        dragSource = new DragSource(linker.getSource(), DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dragSource.setTransfer(new Transfer[] { LocalDataTransfer.getInstance() });

        DragSourceListener sourceListener = new TreeDragSourceListener();
        dragSource.addDragListener(sourceListener);
    }

    /**
     * 
     * create DropTarget.
     */
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
     * DOC hwang Comment method "getManager".
     * 
     * @return
     */
    private HL7Manager getManager() {
        return linker.getManager();
    }

    /**
     * 
     * amaumont XmlToSchemaDragAndDropHandler class global comment. Detailled comment <br/>
     * 
     * $Id: Schema2XMLDragAndDropHandler.java,v 1.1 2007/06/12 07:20:38 gke Exp $
     * 
     */
    class TreeDragSourceListener implements TransferDragSourceListener {

        public void dragFinished(DragSourceEvent event) {
            event.getSource();
        }

        public void dragSetData(DragSourceEvent event) {
            event.getSource();
        }

        public void dragStart(DragSourceEvent event) {
            TableItem[] items = linker.getSource().getSelection();
            if (items.length == 0) {
                event.doit = false;
            } else {
                LocalDraggedData draggedData = new LocalDraggedData();
                for (TableItem treeItem : items) {
                    draggedData.add(treeItem.getData());
                }
                IConnection connection = getManager().getUiManager().getHl7UI().getConnection();
                if (connection != null) {
                    draggedData.setTable(connection.getMetadataTable());
                }
                LocalDataTransfer.getInstance().setLocalDraggedData(draggedData);
            }
        }

        public Transfer getTransfer() {
            return LocalDataTransfer.getInstance();
        }
    };

    /**
     * 
     * amaumont XmlToSchemaDragAndDropHandler class global comment. Detailled comment <br/>
     * 
     * $Id: Schema2XMLDragAndDropHandler.java,v 1.1 2007/06/12 07:20:38 gke Exp $
     * 
     */
    public class TableDropTargetListener implements TransferDropTargetListener {

        public void dragEnter(DropTargetEvent event) {
        }

        private HL7TreeNode getRootElement(HL7TreeNode curElement) {
            if (curElement != null) {
                HL7TreeNode parent = curElement.getParent();
                if (parent != null) {
                    return getRootElement(parent);
                }
                return curElement;
            }
            return null;
        }

        public void dragLeave(DropTargetEvent event) {
        }

        public void dragOperationChanged(DropTargetEvent event) {
        }

        public void dropAccept(DropTargetEvent event) {

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.util.TransferDropTargetListener#getTransfer()
         */
        public Transfer getTransfer() {
            return LocalDataTransfer.getInstance();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.util.TransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
         */
        public boolean isEnabled(DropTargetEvent event) {
            HL7Manager manager = getManager();
            if (manager != null && manager instanceof HL7OutputManager) {
                String currentSchema = ((HL7OutputManager) manager).getCurrentSchema(true);
                if (currentSchema != null) {
                    Item targetItem = (Item) event.item;
                    if (targetItem != null) {
                        Object data = targetItem.getData();
                        if (data != null && data instanceof HL7TreeNode) {
                            HL7TreeNode treeNode = ((HL7TreeNode) data);
                            if (treeNode.getParent() == null) {
                                MessageDialog.openConfirm(event.display.getActiveShell(), "Warning", "\"" + treeNode.getLabel()
                                        + "\" " + "is root, can not have linker,you should create sub-elements or attributes.");
                                return false;
                            }
                            String columnLabel = treeNode.getRow();
                            return columnLabel != null ? columnLabel.startsWith(currentSchema) : false;
                        }
                    }
                }
            }
            return true;
        }

        public void dragOver(DropTargetEvent event) {
            // System.out.println("\n>>drop");
            DropTarget dropTarget = (DropTarget) event.getSource();
            Item targetItem = (Item) event.item;
            if (targetItem == null) {
                event.detail = DND.DROP_NONE;
                return;
            }

            HL7TreeNode targetNode = (HL7TreeNode) (targetItem.getData());
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
                    HL7TreeNode parent = targetNode.getParent();
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

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
         */
        public void drop(DropTargetEvent event) {
            // System.out.println("\n>>drop");
            DropTarget dropTarget = (DropTarget) event.getSource();
            Item targetItem = (Item) event.item;
            if (targetItem == null || !isEnabled(event)) {
                return;
            }

            Control control = dropTarget.getControl();

            String row = (String) control.getData("row"); //$NON-NLS-1$
            if (getManager().getHl7Component().isHL7Output()) {
                if (getManager() instanceof HL7OutputManager) {
                    row = ((HL7OutputManager) getManager()).getCurrentSchema(true);
                }

            }
            LocalDraggedData draggedData = LocalDataTransfer.getInstance().getDraggedData();

            List<Object> dragdedData = draggedData.getTransferableEntryList();
            IMetadataTable table = null;
            if (draggedData.getTable() instanceof IMetadataTable) {
                table = (IMetadataTable) draggedData.getTable();
            }

            HL7TreeNode targetNode = (HL7TreeNode) (targetItem.getData());

            if (dragdedData.size() == 1 && isDropRelatedColumn(event)) {
                if (!targetNode.hasChildren()) {
                    IMetadataColumn metaColumn = (IMetadataColumn) dragdedData.get(0);
                    targetNode.setDefaultValue(null);
                    targetNode.setColumn(metaColumn);
                    targetNode.setTable(table);
                    targetNode.setRow(row);
                    linker.getXMLViewer().refresh(targetNode);
                    linker.getXMLViewer().expandAll();

                    Display display = linker.getSource().getDisplay();
                    Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                    linker.getSource().getShell().setCursor(cursor);

                    linker.valuedChanged(targetItem);

                    linker.getSource().getShell().setCursor(null);
                }
            } else if (dragdedData.size() > 0) {
                boolean hide = !canAddAttribute(targetNode);
                HL7DragAndDrogDialog dialog = new HL7DragAndDrogDialog(null, hide);
                dialog.open();
                if (dialog.getReturnCode() == IDialogConstants.CANCEL_ID) {
                    return;
                }

                if (dialog.getSelectValue().equals(HL7DragAndDrogDialog.CREATE_AS_TEXT)) {
                    if (targetNode.hasChildren()) {
                        List<HL7TreeNode> children = targetNode.getChildren();
                        for (HL7TreeNode hl7TreeNode : children) {
                            if (!(hl7TreeNode instanceof Attribute)) {
                                MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel() + "\" "
                                        + "has element children, can not have linker.");
                                return;
                            }
                        }

                    } else if (targetNode.getParent() == null) {
                        MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel() + "\" "
                                + "is root, can not have linker.");
                        return;
                    }
                    IMetadataColumn metaColumn = (IMetadataColumn) dragdedData.get(0);
                    targetNode.setColumn(metaColumn);
                    setDefaultFixValue(targetNode);
                } else if (dialog.getSelectValue().equals(HL7DragAndDrogDialog.CREATE_AS_SUBELEMENT)) {
                    if (!(targetNode instanceof Element)) {
                        MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
                                + "isn't a Element, can not create sub-elements or attributes.");
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
                        IMetadataColumn metaColumn = (IMetadataColumn) obj;
                        boolean isContain = false;
                        for (HL7TreeNode node : ((Element) targetNode).getElementChildren()) {
                            if (node.getLabel().equals(metaColumn.getLabel())) {
                                node.setColumn(metaColumn);
                                node.setRow(row);
                                node.setTable(table);
                                setDefaultFixValue(node);
                                isContain = true;
                            }
                        }
                        if (!isContain) {
                            HL7TreeNode child = new Element(metaColumn.getLabel());
                            child.setColumn(metaColumn);
                            child.setTable(table);
                            child.setRow(row);
                            targetNode.addChild(child);
                        }
                    }
                    setDefaultFixValue(targetNode);
                } else if (dialog.getSelectValue().equals(HL7DragAndDrogDialog.CREATE_AS_ATTRIBUTE)) {
                    if (!(targetNode instanceof Element)) {
                        MessageDialog.openConfirm(control.getShell(), "Warning", "\"" + targetNode.getLabel() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
                                + "isn't a Element, can not create sub-elements or attributes.");
                        return;
                    }
                    for (Object obj : dragdedData) {
                        IMetadataColumn metaColumn = (IMetadataColumn) obj;
                        boolean isContain = false;
                        for (HL7TreeNode node : ((Element) targetNode).getAttributeChildren()) {
                            if (node.getLabel().equals(metaColumn.getLabel())) {
                                node.setColumn(metaColumn);
                                node.setTable(table);
                                node.setRow(row);
                                setDefaultFixValue(node);
                                isContain = true;
                            }
                        }
                        if (!isContain) {
                            HL7TreeNode child = new Attribute(metaColumn.getLabel());
                            child.setColumn(metaColumn);
                            child.setTable(table);
                            child.setRow(row);
                            targetNode.addChild(child);
                        }
                    }
                    setDefaultFixValue(targetNode);
                }

                if (row != null) {
                    targetNode.setRow(row);
                    if (!getManager().getHl7Component().isHL7Output()) {
                        HL7TreeNode root = (HL7TreeNode) linker.getXMLViewer().getTree().getItem(0).getData();
                        setTreeNodeRow(root, row);
                    }
                }
                linker.getXMLViewer().refresh();
                linker.getXMLViewer().expandAll();

                Display display = linker.getSource().getDisplay();
                Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                linker.getSource().getShell().setCursor(cursor);

                linker.valuedChanged(targetItem);

                linker.getSource().getShell().setCursor(null);
            }
            if (row != null) {
                targetNode.setRow(row);
                if (!getManager().getHl7Component().isHL7Output()) {
                    HL7TreeNode root = (HL7TreeNode) linker.getXMLViewer().getTree().getItem(0).getData();
                    setTreeNodeRow(root, row);
                }
            }
            linker.getXMLViewer().refresh();
            linker.getXMLViewer().expandAll();

            linker.updateLinksStyleAndControlsSelection(control, true);
        }
    }

    /**
     * DOC wzhang Comment method "setDefaultFixValue".
     * 
     * @param treeNode
     */
    private void setDefaultFixValue(HL7TreeNode treeNode) {
        String fixValue = treeNode.getDefaultValue();
        if (fixValue == null) {
            return;
        }
        treeNode.setDefaultValue(null);
    }

    // reset all the treenode add row to relative column
    // private void resetTree(HL7TreeNode root, String row) {
    // root.getChildren();
    // for (TreeItem item : items) {
    // setTreeNodeRow(item, row);
    // }
    // }

    // reset all the treeNode add row to relative column
    private void setTreeNodeRow(HL7TreeNode root, String row) {
        if (root == null)
            return;
        root.setRow(row);
        if (root instanceof Element) {
            Element element = (Element) root;
            List<HL7TreeNode> children = element.getElementChildren();
            for (HL7TreeNode child : children) {
                setTreeNodeRow(child, row);
            }
            children = element.getNameSpaceChildren();
            for (HL7TreeNode child : children) {
                setTreeNodeRow(child, row);
            }
        }
        List<HL7TreeNode> children = root.getChildren();
        for (HL7TreeNode child : children) {
            setTreeNodeRow(child, row);
        }

    }

    private boolean canAddAttribute(HL7TreeNode node) {

        if (node == null) {
            return false;
        }
        if (node.getParent() == null) {
            return false;
        }
        if (node instanceof Element) {
            if (((Element) node).getAttributeChildren().size() > 0) {
                return false;
            }

            String nodeName = node.getLabel() + "-";
            List<HL7TreeNode> childs = ((Element) node).getElementChildren();
            if (childs.size() > 0) {
                for (HL7TreeNode child : childs) {
                    if (!child.getLabel().startsWith(nodeName)) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }

        if (node.getClass() != Element.class) {
            return false;
        }
        return true;
    }
}
