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
package org.talend.designer.fileoutputxml.ui.edit;

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
import org.talend.designer.fileoutputxml.i18n.Messages;
import org.talend.designer.fileoutputxml.managers.FOXManager;
import org.talend.metadata.managment.ui.wizard.metadata.xml.dialog.DragAndDrogDialog;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;

/**
 * amaumont class global comment. Detailled comment <br/>
 *
 * $Id: Schema2XMLDragAndDropHandler.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class Schema2XMLDragAndDropHandler {

    private Schema2XMLLinker linker;

    private DragSource dragSource;

    protected int dropDefaultOperation = DND.DROP_LINK;

    private DropTarget loopDropTarget;

    /**
     * amaumont TreeToTableDragAndDropHandler constructor comment.
     *
     * @param linker
     */
    public Schema2XMLDragAndDropHandler(Schema2XMLLinker linker) {
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
     * DOC wzhang Comment method "getManager".
     *
     * @return
     */
    private FOXManager getManager() {
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
                for (TableItem treeItem : items) {
                    draggedData.add(treeItem.getData());
                }
                IConnection connection = getManager().getUiManager().getFoxUI().getConnection();
                if (connection != null) {
                    draggedData.setTable(connection.getMetadataTable());
                }
                LocalDataTransfer.getInstance().setLocalDraggedData(draggedData);
            }
        }

        @Override
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

        @Override
        public void dragEnter(DropTargetEvent event) {
        }

        /**
         *
         * DOC wzhang Comment method "getRootElement".
         *
         * @param curElement
         * @return
         */
        private FOXTreeNode getRootElement(FOXTreeNode curElement) {
            if (curElement != null) {
                FOXTreeNode parent = curElement.getParent();
                if (parent != null) {
                    return getRootElement(parent);
                }
                return curElement;
            }
            return null;
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

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.util.TransferDropTargetListener#getTransfer()
         */
        @Override
        public Transfer getTransfer() {
            return LocalDataTransfer.getInstance();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.util.TransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
         */
        @Override
        public boolean isEnabled(DropTargetEvent event) {
            FOXManager manager = getManager();
            if (manager != null) {
                String currentSchema = manager.getCurrentSchema();
                if (currentSchema != null) {
                    Item targetItem = (Item) event.item;
                    if (targetItem != null) {
                        Object data = targetItem.getData();
                        if (data instanceof FOXTreeNode) {
                            FOXTreeNode rootElement = getRootElement((FOXTreeNode) data);
                            if (rootElement != null) {
                                return currentSchema.equals(rootElement.getRow());
                            }
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public void dragOver(DropTargetEvent event) {
            // System.out.println("\n>>drop");
            DropTarget dropTarget = (DropTarget) event.getSource();
            Item targetItem = (Item) event.item;
            if (targetItem == null || !isEnabled(event)) {
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

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
         */
        @Override
        public void drop(DropTargetEvent event) {
            // System.out.println("\n>>drop");
            DropTarget dropTarget = (DropTarget) event.getSource();
            Item targetItem = (Item) event.item;
            if (targetItem == null) {
                return;
            }

            Control control = dropTarget.getControl();
            // add by wzhang. get current row.
            String row = (String) control.getData("row"); //$NON-NLS-1$
            if (getManager().getFoxComponent().istFileOutputMSXML()) {
                row = getManager().getCurrentSchema();
            }
            LocalDraggedData draggedData = LocalDataTransfer.getInstance().getDraggedData();

            List<Object> dragdedData = draggedData.getTransferableEntryList();
            IMetadataTable table = null;
            if (draggedData.getTable() instanceof IMetadataTable) {
                table = (IMetadataTable) draggedData.getTable();
            }

            FOXTreeNode targetNode = (FOXTreeNode) (targetItem.getData());

            if (dragdedData.size() == 1 && isDropRelatedColumn(event)) {
                // Changed by Marvin Wang on Jun. 8, 2012 for bug TDI-21396.
                // Drag all nodes to related column directly with no checking. Do not worry about the root element, caz
                // it has shilded the operation.
                // if (!targetNode.hasChildren()) {
                IMetadataColumn metaColumn = (IMetadataColumn) dragdedData.get(0);
                targetNode.setDefaultValue(null);
                targetNode.setColumn(metaColumn);
                targetNode.setTable(table);
                targetNode.setRow(row);
                linker.getXMLViewer().refresh(targetNode);

                Display display = linker.getSource().getDisplay();
                Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                linker.getSource().getShell().setCursor(cursor);

                linker.valuedChanged(targetItem);

                linker.getSource().getShell().setCursor(null);
                // }
            } else if (dragdedData.size() > 0) {
                DragAndDrogDialog dialog = null;
                if (getManager().getFoxComponent().istWriteJSONField()) {
                    dialog = new DragAndDrogDialog(null, true);
                } else {
                    dialog = new DragAndDrogDialog(null);
                }
                dialog.open();
                if (dialog.getReturnCode() == IDialogConstants.CANCEL_ID) {
                    return;
                }
                // add by xzhang
                if (dialog.getSelectValue().equals(DragAndDrogDialog.CREATE_AS_TEXT)) {
                    if (hasElementChildren(targetNode)) {
                        // add for bug 11723.
                        List<FOXTreeNode> children = targetNode.getChildren();
                        for (FOXTreeNode foxTreeNode : children) {
                            if (!(foxTreeNode instanceof Attribute)) {
                                MessageDialog.openConfirm(control.getShell(), Messages.getString("CreateElementAction.0"), //$NON-NLS-1$
                                        "\"" + targetNode.getLabel() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
                                                + Messages.getString("Schema2XMLDragAndDropHandler.HasChildrenWarning")); //$NON-NLS-1$
                                return;
                            }
                        }

                    } else if (targetNode.getParent() == null) {
                        MessageDialog.openConfirm(control.getShell(), Messages.getString("CreateElementAction.0"), //$NON-NLS-1$
                                "\"" + targetNode.getLabel() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
                                        + Messages.getString("Schema2XMLDragAndDropHandler.IsRootWarning")); //$NON-NLS-1$
                        return;
                    }
                    IMetadataColumn metaColumn = (IMetadataColumn) dragdedData.get(0);
                    targetNode.setColumn(metaColumn);
                    targetNode.setDefaultValue(null);
                } else if (dialog.getSelectValue().equals(DragAndDrogDialog.CREATE_AS_SUBELEMENT)) {
                    if (!(targetNode instanceof Element)) {
                        MessageDialog.openConfirm(control.getShell(), Messages.getString("CreateElementAction.0"), //$NON-NLS-1$
                                "\"" + targetNode.getLabel() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
                                        + Messages.getString("Schema2XMLDragAndDropHandler.IsNotElementWarning")); //$NON-NLS-1$
                        return;
                    }
                    if (targetNode.getColumn() != null) {
                        if (!MessageDialog.openConfirm(control.getShell(), Messages.getString("CreateElementAction.0"), //$NON-NLS-1$
                                Messages.getString("CreateElementAction.1") //$NON-NLS-1$
                                        + targetNode.getLabel() + "\"?")) { //$NON-NLS-1$
                            return;
                        }
                        targetNode.setColumn(null);
                    }
                    for (Object obj : dragdedData) {
                        IMetadataColumn metaColumn = (IMetadataColumn) obj;
                        boolean isContain = false;
                        for (FOXTreeNode node : ((Element) targetNode).getElementChildren()) {
                            if (node.getLabel().equals(metaColumn.getLabel())) {
                                node.setColumn(metaColumn);
                                node.setRow(row);
                                node.setTable(table);
                                setDefaultFixValue(node);
                                isContain = true;
                            }
                        }
                        if (!isContain) {
                            FOXTreeNode child = new Element(metaColumn.getLabel());
                            child.setColumn(metaColumn);
                            child.setTable(table);
                            child.setRow(row);
                            targetNode.addChild(child);
                        }
                    }
                    setDefaultFixValue(targetNode);
                } else if (dialog.getSelectValue().equals(DragAndDrogDialog.CREATE_AS_ATTRIBUTE)) {
                    if (!(targetNode instanceof Element)) {
                        MessageDialog.openConfirm(control.getShell(), Messages.getString("CreateElementAction.0"), //$NON-NLS-1$
                                "\"" + targetNode.getLabel() + "\" " //$NON-NLS-1$ //$NON-NLS-2$
                                        + Messages.getString("Schema2XMLDragAndDropHandler.IsNotElementWarning")); //$NON-NLS-1$
                        return;
                    }
                    for (Object obj : dragdedData) {
                        IMetadataColumn metaColumn = (IMetadataColumn) obj;
                        boolean isContain = false;
                        for (FOXTreeNode node : ((Element) targetNode).getAttributeChildren()) {
                            if (node.getLabel().equals(metaColumn.getLabel())) {
                                node.setColumn(metaColumn);
                                node.setTable(table);
                                node.setRow(row);
                                setDefaultFixValue(node);
                                isContain = true;
                            }
                        }
                        if (!isContain) {
                            FOXTreeNode child = new Attribute(metaColumn.getLabel());
                            child.setColumn(metaColumn);
                            child.setTable(table);
                            child.setRow(row);
                            targetNode.addChild(child);
                        }
                    }
                    // setDefaultFixValue(targetNode);
                }

                if (row != null) {
                    targetNode.setRow(row);
                    if (!getManager().getFoxComponent().istFileOutputMSXML()) {
                        FOXTreeNode root = (FOXTreeNode) linker.getXMLViewer().getTree().getItem(0).getData();
                        setTreeNodeRow(root, row);
                    }
                }
                linker.getXMLViewer().refresh();

                Display display = linker.getSource().getDisplay();
                Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                linker.getSource().getShell().setCursor(cursor);

                linker.valuedChanged(targetItem);

                linker.getSource().getShell().setCursor(null);
            }
            if (row != null) {
                targetNode.setRow(row);
                if (!getManager().getFoxComponent().istFileOutputMSXML()) {
                    FOXTreeNode root = (FOXTreeNode) linker.getXMLViewer().getTree().getItem(0).getData();
                    setTreeNodeRow(root, row);
                }
            }
            linker.getXMLViewer().refresh();
            linker.getXMLViewer().expandAll();

            linker.updateLinksStyleAndControlsSelection(control, true);
        }

        /**
         * DOC wzhang Comment method "setDefaultFixValue".
         *
         * @param treeNode
         */
        private void setDefaultFixValue(FOXTreeNode treeNode) {
            String fixValue = treeNode.getDefaultValue();
            if (fixValue == null) {
                return;
            }
            treeNode.setDefaultValue(null);
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

        // reset all the treenode add row to relative column
        // private void resetTree(FOXTreeNode root, String row) {
        // root.getChildren();
        // for (TreeItem item : items) {
        // setTreeNodeRow(item, row);
        // }
        // }

        // reset all the treeNode add row to relative column
        private void setTreeNodeRow(FOXTreeNode root, String row) {
            if (root == null) {
                return;
            }
            root.setRow(row);
            if (root instanceof Element) {
                Element element = (Element) root;
                List<FOXTreeNode> children = element.getElementChildren();
                for (FOXTreeNode child : children) {
                    setTreeNodeRow(child, row);
                }
                final List<? extends FOXTreeNode> nameSpaceChildren = element.getNameSpaceChildren();
                for (FOXTreeNode child : nameSpaceChildren) {
                    setTreeNodeRow(child, row);
                }
            }
            List<FOXTreeNode> children = root.getChildren();
            for (FOXTreeNode child : children) {
                setTreeNodeRow(child, row);
            }

        }
    }
}
