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
package org.talend.designer.hl7.edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.designer.hl7.dnd.MessageToSchemaDraggedData;
import org.talend.designer.hl7.dnd.SegmentTransfer;
import org.talend.designer.hl7.dnd.TransferableSegmentEntry;
import org.talend.designer.hl7.managers.HL7Manager;
import org.talend.designer.hl7.model.PrimitiveModel;
import org.talend.designer.hl7.model.SegmentModel;
import org.talend.designer.hl7.ui.view.HL7MetadataEmfTableEditorView;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class HL7Message2SchemaDragAndDropHandler {

    private HL7Tree2SchemaLinker linker;

    private DragSource dragSource;

    private DropTarget loopDropTarget;

    protected int dropDefaultOperation = DND.DROP_LINK;

    private String defaultLabel = "newColumn"; //$NON-NLS-1$

    private Table targetTable;

    public HL7Message2SchemaDragAndDropHandler(HL7Tree2SchemaLinker linker) {
        this.linker = linker;
        this.targetTable = linker.getTarget();
        init();
    }

    private void init() {
        createDragSource();
        createDropTarget();
    }

    public HL7Manager getManager() {
        return linker.getManager();
    }

    private void createDragSource() {
        if (dragSource != null) {
            dragSource.dispose();
        }
        dragSource = new DragSource(linker.getTree(), DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dragSource.setTransfer(new Transfer[] { SegmentTransfer.getInstance() });
        DragSourceListener sourceListener = new TreeDragSourceListener();
        dragSource.addDragListener(sourceListener);
    }

    private void createDropTarget() {

        if (loopDropTarget != null) {
            loopDropTarget.dispose();
        }
        loopDropTarget = new DropTarget(linker.getTarget(), DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        loopDropTarget.setTransfer(new Transfer[] { SegmentTransfer.getInstance() });
        DropTargetListener targetListener = new TableDropTargetListener();
        loopDropTarget.addDropListener(targetListener);

    }

    class TreeDragSourceListener implements TransferDragSourceListener {

        public void dragFinished(DragSourceEvent event) {
            event.getSource();
        }

        public void dragSetData(DragSourceEvent event) {
            event.getSource();
        }

        public void dragStart(DragSourceEvent event) {
            TreeItem[] items = linker.getTree().getSelection();
            if (items.length == 0) {
                event.doit = false;
            } else {
                MessageToSchemaDraggedData draggedData = new MessageToSchemaDraggedData();
                List<TreeItem> itemList = new ArrayList<TreeItem>();
                for (TreeItem treeItem : items) {
                    if (treeItem.getData() instanceof PrimitiveModel) {
                        PrimitiveModel pm = (PrimitiveModel) treeItem.getData();
                        draggedData.add(new TransferableSegmentEntry(pm));
                        itemList.add(treeItem);
                    }
                }
                SegmentTransfer.getInstance().setDragedItem(items[0]);
                SegmentTransfer.getInstance().setDraggedData(draggedData);
                SegmentTransfer.getInstance().setDragedItemList(itemList);
            }
        }

        public Transfer getTransfer() {
            return SegmentTransfer.getInstance();
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
            dragEnterExecute(event);
        }

        /**
         * DOC amaumont Comment method "dragEnterExecute".
         * 
         * @param event
         */
        private void dragEnterExecute(DropTargetEvent event) {
            targetTable.setFocus();
        }

        public void dragOver(DropTargetEvent event) {
            if (!(((SegmentTransfer) getTransfer()).getDragedItem().getData() instanceof PrimitiveModel)) {
                event.detail = DND.DROP_NONE;
            } else {
                PrimitiveModel pm = (PrimitiveModel) (((SegmentTransfer) getTransfer()).getDragedItem().getData());
                String segName = pm.getDisplayName().split("-")[0];
                //                String segName = pm.getDisplayName().substring(0, pm.getDisplayName().indexOf("_")); //$NON-NLS-N$
                Object obj = ((IStructuredSelection) linker.getMainui().getMetaTableViewer().getSelection()).getFirstElement();
                if (obj != null && obj instanceof SegmentModel) {
                    if (!((SegmentModel) obj).getSeg().getName().equals(segName)) {
                        event.detail = DND.DROP_NONE;
                    }
                }
            }
        }

        public void dragLeave(DropTargetEvent event) {
        }

        public void dragOperationChanged(DropTargetEvent event) {
            MessageToSchemaDraggedData draggedData = SegmentTransfer.getInstance().getDraggedData();

        }

        public void dropAccept(DropTargetEvent event) {
            MessageToSchemaDraggedData draggedData = SegmentTransfer.getInstance().getDraggedData();

        }

        public Transfer getTransfer() {
            return SegmentTransfer.getInstance();
        }

        public boolean isEnabled(DropTargetEvent event) {
            return false;
        }

        public void drop(DropTargetEvent event) {
            // System.out.println("\n>>drop");
            if (!linker.isRepository()) {
                DropTarget dropTarget = (DropTarget) event.getSource();
                Control control = dropTarget.getControl();

                MessageToSchemaDraggedData draggedData = SegmentTransfer.getInstance().getDraggedData();

                List<TransferableSegmentEntry> transferableEntryList = draggedData.getTransferableEntryList();

                HL7MetadataEmfTableEditorView hl7TableEditorView = linker.getHl7SchemaTableEditorView();

                // ExtendedTableModel<MetadataColumn> extendedTableModel = hl7TableEditorView.getExtendedTableModel();
                if (transferableEntryList.size() > 0) {
                    for (int i = 0; i < transferableEntryList.size(); i++) {
                        TransferableSegmentEntry entry = transferableEntryList.get(i);
                        String segName = entry.getPm().getDisplayName();
                        Display display = linker.getTree().getDisplay();
                        Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                        linker.getTree().getShell().setCursor(cursor);
                        MetadataColumn metacolumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                        String label = hl7TableEditorView.getMetadataEditor().getNextGeneratedColumnName(defaultLabel);
                        metacolumn.setLabel(label);
                        metacolumn.setOriginalField(segName);
                        metacolumn.setLength(226);
                        metacolumn.setPrecision(0);
                        hl7TableEditorView.getMetadataEditor().add(metacolumn);
                        TreeItem item = SegmentTransfer.getInstance().getDragedItemList().get(i);
                        TableItem targetitem = targetTable.getSelection()[0];
                        createLinks(item, targetitem);
                        final Combo combo = linker.getMainui().getMetaTableViewer().getCombo();
                        String key = combo.getItem(combo.getSelectionIndex()).toString();
                        MetadataColumn copied = copyColumn(metacolumn);
                        linker.getManager().updateRelationMapping(key, copied, true);
                    }

                }
                linker.updateLinksStyleAndControlsSelection(control, true); // solve drag pb on 13749
            }
            // linker.getBackgroundRefresher().refreshBackground();
        }
    }

    public void createLinks(TreeItem itemSource, TableItem itemTarget) {

        linker.addLinks(itemSource, itemSource.getData(), itemTarget.getParent(), itemTarget.getData());
    }

    protected MetadataColumn copyColumn(MetadataColumn column) {
        MetadataColumn newColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
        newColumn.setComment(column.getComment());
        newColumn.setDefaultValue(column.getDefaultValue());
        newColumn.setKey(column.isKey());
        newColumn.setLabel(column.getLabel());
        newColumn.setPattern(column.getPattern());
        if (column.getLength() < 0) {
            newColumn.setLength(0);
        } else {
            newColumn.setLength(column.getLength());
        }
        newColumn.setNullable(column.isNullable());
        if (column.getPrecision() < 0) {
            newColumn.setPrecision(0);
        } else {
            newColumn.setPrecision(column.getPrecision());
        }
        newColumn.setTalendType(column.getTalendType());
        newColumn.setSourceType(column.getSourceType());
        if (column.getOriginalField() == null || column.getOriginalField().equals("")) { //$NON-NLS-1$
            newColumn.setLabel(column.getLabel());
        } else {
            newColumn.setOriginalField(column.getOriginalField());
        }
        return newColumn;
    }

}
