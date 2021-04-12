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

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.swt.dnd.LocalDataTransfer;
import org.talend.commons.ui.swt.dnd.LocalDraggedData;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.dnd.XPathTransfer;

/**
 *
 * DOC ycbai class global comment. Detailled comment
 *
 */
public class JSONTree2SchemaDragAndDropHandler {

    private JSONTree2SchemaLinker linker;

    private DragSource dragSource;

    protected int dropDefaultOperation = DND.DROP_LINK;

    private TreeViewer treeViewer;

    private Tree tree;

    private TableViewer tableViewer;

    private Table table;

    private DropTarget loopDropTarget;

    private JSONFileConnection connection;

    public JSONTree2SchemaDragAndDropHandler(JSONTree2SchemaLinker linker) {
        this.linker = linker;
        treeViewer = this.linker.getTreeViewer();
        tableViewer = this.linker.getTableViewer();
        tree = treeViewer.getTree();
        table = tableViewer.getTable();
        connection = this.linker.getConnection();
        init();
    }

    private void init() {
        createDragSource();
        createDropTarget();
    }

    private void createDragSource() {
        if (dragSource != null) {
            dragSource.dispose();
        }
        dragSource = new DragSource(tree, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dragSource.setTransfer(new Transfer[] { XPathTransfer.getInstance() });
        DragSourceListener sourceListener = new TreeDragSourceListener();
        dragSource.addDragListener(sourceListener);
    }

    private void createDropTarget() {
        if (loopDropTarget != null) {
            loopDropTarget.dispose();
        }
        loopDropTarget = new DropTarget(table, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        loopDropTarget.setTransfer(new Transfer[] { XPathTransfer.getInstance() });
        DropTargetListener targetListener = new TableDropTargetListener();
        loopDropTarget.addDropListener(targetListener);
    }

    class TreeDragSourceListener implements TransferDragSourceListener {

        public void dragFinished(DragSourceEvent event) {
        }

        public void dragSetData(DragSourceEvent event) {
        }

        public void dragStart(DragSourceEvent event) {
            TreeItem[] items = tree.getSelection();
            if (items.length == 0) {
                event.doit = false;
            } else {
                boolean isHasChildren = false;
                for (TreeItem treeItem : items) {
                    if (treeItem.getItemCount() > 0) {
                        isHasChildren = true;
                        break;
                    }
                }
                if (isHasChildren) {
                    event.doit = false;
                } else {
                    LocalDraggedData draggedData = new LocalDraggedData();
                    for (TreeItem treeItem : items) {
                        draggedData.add(treeItem.getData());
                    }
                    LocalDataTransfer.getInstance().setLocalDraggedData(draggedData);
                }
            }
        }

        public Transfer getTransfer() {
            return LocalDataTransfer.getInstance();
        }
    };

    public class TableDropTargetListener implements TransferDropTargetListener {

        public void dragEnter(DropTargetEvent event) {
        }

        public void dragOver(DropTargetEvent event) {
        }

        public void dragLeave(DropTargetEvent event) {
        }

        public void dragOperationChanged(DropTargetEvent event) {
        }

        public void dropAccept(DropTargetEvent event) {
        }

        public Transfer getTransfer() {
            return XPathTransfer.getInstance();
        }

        public boolean isEnabled(DropTargetEvent event) {
            return true;
        }

        public void drop(DropTargetEvent event) {
            DropTarget dropTarget = (DropTarget) event.getSource();
            Item targetItem = (Item) event.item;

            MetadataColumn targetColumn = null;
            if (targetItem != null) {
                targetColumn = (MetadataColumn) targetItem.getData();
            }
            if (targetColumn != null) {

            }

            EList columns = ConnectionHelper.getTables(connection).toArray(new MetadataTable[0])[0].getColumns();
            int maxColumnsNumber = CoreUIPlugin.getDefault().getPreferenceStore()
                    .getInt(ITalendCorePrefConstants.MAXIMUM_AMOUNT_OF_COLUMNS_FOR_XML);
            if (columns.size() >= maxColumnsNumber) {
                MessageDialog
                        .openWarning(
                                treeViewer.getTree().getShell(),
                                "Columns Overflow",
                                "The amount of schema columns has reached the max value. Please increase the max value on Preference Page(/Talend/Metadata) if you want to add new columns.");
                return;
            }

            Control control = dropTarget.getControl();
            LocalDraggedData draggedData = LocalDataTransfer.getInstance().getDraggedData();
            List<Object> transferableObjs = draggedData.getTransferableEntryList();
            for (Object obj : transferableObjs) {
                if (obj instanceof FOXTreeNode) {
                    FOXTreeNode treeNode = (FOXTreeNode) obj;
                    calcuAddedColumns(treeNode, targetColumn, columns);
                }
            }

            tableViewer.setInput(columns);
            tableViewer.refresh();
            treeViewer.refresh();

            Display display = tree.getDisplay();
            Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
            tree.getShell().setCursor(cursor);

            linker.valueChanged(targetItem);

            tree.getShell().setCursor(null);

            linker.updateLinksStyleAndControlsSelection(control, true);
            linker.updateConnection();
            linker.updateFormStatus();
        }
    }

    protected void calcuAddedColumns(FOXTreeNode node, MetadataColumn targetColumn, EList columnList) {
        if (targetColumn == null) {
            addColumn(node.getLabel(), node, columnList);
        } else {
            node.setColumn(ConvertionHelper.convertToIMetaDataColumn(targetColumn));
        }
    }

    private void addColumn(String label, FOXTreeNode node, EList columnList) {
        if (StringUtils.isEmpty(label)) {
            return;
        }
        MetadataEmfTableEditor editor = new MetadataEmfTableEditor();
        String columnName = label;
        if (columnName.contains(":")) { //$NON-NLS-1$
            columnName = columnName.split(":")[1]; //$NON-NLS-1$
        }
        columnName = columnName.replaceAll("[^a-zA-Z0-9]", "_"); //$NON-NLS-1$
        String dataType = node.getDataType();
        MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
        metadataColumn.setLabel(editor.getNextGeneratedColumnName(columnName, columnList));
        metadataColumn.setOriginalField(label);
        metadataColumn.setTalendType(dataType);
        columnList.add(metadataColumn);
        node.setColumn(ConvertionHelper.convertToIMetaDataColumn(metadataColumn));
    }

}
