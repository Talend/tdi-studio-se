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
package org.talend.repository.json.ui.wizards.dnd;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTableAddCommand;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.list.UniqueStringGenerator;
import org.talend.datatools.xml.utils.XPathPopulationUtil;
import org.talend.repository.json.ui.wizards.extraction.ExtractionFieldsWithJSONXPathEditorView;
import org.talend.repository.json.ui.wizards.extraction.ExtractionLoopWithJSONXPathEditorView;
import org.talend.repository.json.ui.wizards.extraction.JSONToXPathLinker;
import org.talend.repository.model.json.JSONXPathLoopDescriptor;
import org.talend.repository.model.json.SchemaTarget;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;

/**
 * DOC amaumont class global comment. Detailed comment <br/>
 *
 * $Id$
 *
 */
public class JSONToSchemaDragAndDropHandler {

    private JSONToXPathLinker linker;

    private DragSource dragSource;

    private DropTarget fieldsDropTarget;

    protected int dropDefaultOperation = DND.DROP_LINK;

    private Tree tree;

    private Table fieldsTable;

    private Table loopTable;

    private DropTarget loopDropTarget;

    private String readbyMode;

    /**
     * DOC amaumont TreeToTableDragAndDropHandler constructor comment.
     *
     * @param linker
     */
    public JSONToSchemaDragAndDropHandler(JSONToXPathLinker linker) {
        this.readbyMode = EJsonReadbyMode.XPATH.getValue();
        this.linker = linker;
        tree = linker.getTree();
        loopTable = linker.getLoopTableEditorView().getTableViewerCreator().getTable();
        fieldsTable = linker.getFieldsTableEditorView().getTableViewerCreator().getTable();
        init();
    }

    /**
     * DOC amaumont Comment method "init".
     */
    private void init() {
        createDragSource();
        createDropTarget();
    }

    public void dispose() {
        if (dragSource != null) {
            dragSource.dispose();
        }
        if (loopDropTarget != null) {
            loopDropTarget.dispose();
        }
        if (fieldsDropTarget != null) {
            fieldsDropTarget.dispose();
        }
    }

    public void setReadbyMode(String readbyMode) {
        this.readbyMode = readbyMode;
    }
    
    public String getReadbyMode() {
        return this.readbyMode;
    }

    /**
     *
     * DOC amaumont Comment method "createDragSource".
     *
     * @param sourceListener
     */
    private void createDragSource() {
        if (dragSource != null) {
            dragSource.dispose();
        }
        dragSource = new DragSource(tree, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dragSource.setTransfer(new Transfer[] { XPathTransfer.getInstance() });
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
        loopDropTarget = new DropTarget(loopTable, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        loopDropTarget.setTransfer(new Transfer[] { XPathTransfer.getInstance() });
        DropTargetListener targetListener = new TableDropTargetListener();
        loopDropTarget.addDropListener(targetListener);

        if (fieldsDropTarget != null) {
            fieldsDropTarget.dispose();
        }
        fieldsDropTarget = new DropTarget(fieldsTable, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        fieldsDropTarget.setTransfer(new Transfer[] { XPathTransfer.getInstance() });
        targetListener = new TableDropTargetListener();
        fieldsDropTarget.addDropListener(targetListener);
    }

    /**
     *
     * DOC amaumont JSONToSchemaDragAndDropHandler class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    class TreeDragSourceListener implements TransferDragSourceListener {

        @Override
        public void dragFinished(DragSourceEvent event) {
        }

        @Override
        public void dragSetData(DragSourceEvent event) {
            // System.out.println("\n>>dragSetData");
            // System.out.println(event);
            // if (TableEntriesTransfer.getInstance().isSupportedType(event.dataType)) {
            // }
        }

        @Override
        public void dragStart(DragSourceEvent event) {
            // System.out.println("\n>>dragStart");
            // System.out.println(event);
            TreeItem[] items = tree.getSelection();
            if (items.length == 0) {
                event.doit = false;
            } else {
                JSONToSchemaDraggedData draggedData = new JSONToSchemaDraggedData();
                for (TreeItem treeItem : items) {
                    String absoluteXPath = linker.getAbsoluteXPath(treeItem);
                    draggedData.add(new TransferableJSONXPathEntry(absoluteXPath));
                }
                XPathTransfer.getInstance().setDraggedData(draggedData);

            }
        }

        @Override
        public Transfer getTransfer() {
            return XPathTransfer.getInstance();
        }
    };

    /**
     *
     * DOC amaumont JSONToSchemaDragAndDropHandler class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    public class TableDropTargetListener implements TransferDropTargetListener {

        @Override
        public void dragEnter(DropTargetEvent event) {
            dragEnterExecute(event);
        }

        /**
         * DOC amaumont Comment method "dragEnterExecute".
         *
         * @param event
         */
        private void dragEnterExecute(DropTargetEvent event) {
            fieldsTable.setFocus();
        }

        @Override
        public void dragOver(DropTargetEvent event) {
            // System.out.println("\n>>dragOver");

        }

        @Override
        public void dragLeave(DropTargetEvent event) {
            // System.out.println("\n>>dragLeave");
            // System.out.println(event);
        }

        @Override
        public void dragOperationChanged(DropTargetEvent event) {
            // System.out.println("\n>>dragOperationChanged");
            // showInfos(event);
            JSONToSchemaDraggedData draggedData = XPathTransfer.getInstance().getDraggedData();

        }

        @Override
        public void dropAccept(DropTargetEvent event) {
            // System.out.println("\n>>dropAccept");
            // System.out.println(event);
            JSONToSchemaDraggedData draggedData = XPathTransfer.getInstance().getDraggedData();

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.util.TransferDropTargetListener#getTransfer()
         */
        @Override
        public Transfer getTransfer() {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.util.TransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
         */
        @Override
        public boolean isEnabled(DropTargetEvent event) {
            // TODO Auto-generated method stub
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
            Control control = dropTarget.getControl();

            JSONToSchemaDraggedData draggedData = XPathTransfer.getInstance().getDraggedData();

            List<TransferableJSONXPathEntry> transferableEntryList = draggedData.getTransferableEntryList();

            ExtractionLoopWithJSONXPathEditorView loopTableEditorView = linker.getLoopTableEditorView();
            if (loopTableEditorView.isReadOnly()) {
                return;
            }
            ExtendedTableModel<JSONXPathLoopDescriptor> extendedTableModel = loopTableEditorView.getExtendedTableModel();
            JSONXPathLoopDescriptor pathLoopDescriptor = extendedTableModel.getBeansList().get(0);

            if (linker.isLoopTable((Table) control)) {

                if (transferableEntryList.size() > 0) {

                    String absoluteXPath = transferableEntryList.get(0).getAbsoluteXPath();

                    TableViewerCreatorColumn xpathColumn = linker.getLoopTableEditorView().getXPathColumn();
                    Display display = linker.getTree().getDisplay();
                    Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
                    linker.getTree().getShell().setCursor(cursor);
                    loopTableEditorView.getTableViewerCreator()
                            .setBeanValue(xpathColumn, pathLoopDescriptor, absoluteXPath, true);
                    linker.getTree().getShell().setCursor(null);
                }

            } else {

                ExtractionFieldsWithJSONXPathEditorView tableEditorView = linker.getFieldsTableEditorView();
                Integer startInsertAtThisIndex = TableUtils.getItemIndexWhereInsertFromPosition(fieldsTable, new Point(event.x,
                        event.y));
                // full list of columns to calculate the rename of target columns automatically
                List<SchemaTarget> fullSchemaTargetList = new ArrayList<SchemaTarget>(tableEditorView.getModel().getBeansList());

                boolean isUsingJsonPath = EJsonReadbyMode.JSONPATH.getValue().equals(readbyMode);

                // list of columns just added with drag&drop
                List<SchemaTarget> list = new ArrayList<SchemaTarget>(transferableEntryList.size());
                for (TransferableJSONXPathEntry entry : transferableEntryList) {

                    ArrayList<String> loopXpathNodes = linker.getLoopXpathNodes();
                    if (loopXpathNodes.size() > 0) {
                        String loopPath = loopXpathNodes.get(0);
                        String relativeXPath = null;
                        if (isUsingJsonPath) {
                            String absoluteXPath = entry.getAbsoluteXPath();
                            String loopJsonPath = loopPath + linker.getFieldSeperator();
                            if (absoluteXPath.equals(loopPath)) {
                                relativeXPath = "@"; //$NON-NLS-1$
                            } else if (absoluteXPath.startsWith(loopJsonPath)) {
                                relativeXPath = absoluteXPath.substring(loopJsonPath.length());
                            } else {
                                relativeXPath = absoluteXPath;
                            }
                        } else {
                            relativeXPath = XPathPopulationUtil.populateColumnPath(loopPath, entry.getAbsoluteXPath());

                            if (relativeXPath.startsWith("/")) { //$NON-NLS-1$
                                relativeXPath = relativeXPath.substring(1);
                            }
                            if (relativeXPath.endsWith("../")) { //$NON-NLS-1$
                                relativeXPath = relativeXPath.substring(0, relativeXPath.length() - 1);
                            }
                            if (relativeXPath.trim().equals("")) { //$NON-NLS-1$
                                relativeXPath = "."; //$NON-NLS-1$
                            }
                        }

                        SchemaTarget newTargetEntry = linker.getNewSchemaTargetEntry(relativeXPath);
                        String name = extractColumnName(extractTagName(relativeXPath, readbyMode), fullSchemaTargetList);
                        // if (!name.equals(relativeXPath)) {
                        newTargetEntry.setTagName(name);
                        // }
                        list.add(newTargetEntry);
                        fullSchemaTargetList.add(newTargetEntry);
                    }
                }

                tableEditorView.getTableViewerCreator().getTableViewer().refresh();

                loopTable.deselectAll();
                fieldsTable.deselectAll();

                linker.getTree().deselectAll();

                if (list.size() > 0) {
                    ExtendedTableAddCommand addCommand = new ExtendedTableAddCommand(tableEditorView.getModel(), list,
                            startInsertAtThisIndex);

                    tableEditorView.getExtendedTableViewer().executeCommand(addCommand);
                }

            }
            linker.updateLinksStyleAndControlsSelection(control, true);
            linker.getFieldsTableEditorView().getTableViewerCreator().getTableViewer().refresh();
        }

    }

    /**
     * Extract last word of an expression, the last character must be a letter or a number.
     *
     * @param currentExpr
     * @return
     */
    public static String extractLastWord(String currentExpr) {
        int size = currentExpr.length();
        for (int i = size - 1; i >= 0; i--) {
            if (!("" + currentExpr.charAt(i)).matches("\\w")) { //$NON-NLS-1$ //$NON-NLS-2$
                return currentExpr.substring(i + 1, currentExpr.length());
            }
        }
        return currentExpr;
    }

    public String extractTagName(String currentExpr, String readbyMode) {
        if (EJsonReadbyMode.JSONPATH.getValue().equals(readbyMode)) {
            return extractTagName4JsonPath(currentExpr);
        } else {
            return extractTagName(currentExpr);
        }
    }

    private String extractTagName4JsonPath(String currentExpr) {
        String tagName = currentExpr;
        String[] exprs = currentExpr.split("\\."); //$NON-NLS-1$
        if (0 < exprs.length) {
            tagName = exprs[exprs.length - 1];
        }
        return tagName;
    }

    /**
     * Extract last word of an expression, the last character must be a letter or a number.
     *
     * @param currentExpr
     * @return
     */
    public static String extractTagName(String currentExpr) {
        String[] exprs = currentExpr.split("/");
        if (exprs.length > 0) {
            currentExpr = exprs[exprs.length - 1];
        }
        if (currentExpr.contains(":")) { //$NON-NLS-1$
            currentExpr = currentExpr.split(":")[1]; //$NON-NLS-1$
        }

        return currentExpr;
    }

    /**
     * Extract last word of an expression, the last character must be a letter or a number.
     *
     * @param currentExpr
     * @param fullSchemaTargetList
     * @return
     */
    public static String extractColumnName(String currentExpr, List<SchemaTarget> fullSchemaTargetList) {

        String columnName = currentExpr.startsWith("@") ? currentExpr.substring(1) : currentExpr;
        columnName = columnName.replaceAll("[^a-zA-Z0-9]", "_");
        if (columnName.isEmpty()) {
            columnName = "column";
        }

        UniqueStringGenerator<SchemaTarget> uniqueStringGenerator = new UniqueStringGenerator<SchemaTarget>(columnName,
                fullSchemaTargetList) {

            /*
             * (non-Javadoc)
             *
             * @see org.talend.commons.utils.data.list.UniqueStringGenerator#getBeanString(java.lang.Object)
             */
            @Override
            protected String getBeanString(SchemaTarget bean) {
                return bean.getTagName();
            }

        };
        columnName = uniqueStringGenerator.getUniqueString();
        return columnName;
    }

    public static void main(String[] args) {
        String relativePath = XPathPopulationUtil.populateColumnPath("/doc/members/member/returns", "/doc/members"); //$NON-NLS-1$ //$NON-NLS-2$
        // System.out.println(relativePath);
        relativePath = XPathPopulationUtil.populateColumnPath(
                "/doc/members/member/returns/see/@cref", "/doc/members/member/summary/@name"); //$NON-NLS-1$ //$NON-NLS-2$
        // System.out.println(relativePath);
    }
}
