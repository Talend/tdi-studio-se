// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.dnd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.designer.abstractmap.managers.AbstractUIManager;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.dnd.DraggedData;
import org.talend.designer.abstractmap.ui.dnd.DraggingInfosPopup;
import org.talend.designer.abstractmap.ui.dnd.TableEntriesTransfer;
import org.talend.designer.abstractmap.ui.dnd.TransferableEntry;
import org.talend.designer.abstractmap.ui.listener.DefaultDropTargetListener;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.model.tableentry.VarTableEntry;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.TablesZoneView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class CompleteDropTargetListener extends DefaultDropTargetListener {

    private Table draggableTable;

    private boolean isIntersectAtPreviousDragOver;

    private Point lastDragPosition;

    /**
     * DOC amaumont CompleteDropTargetListener constructor comment.
     *
     * @param mapperManager
     */
    public CompleteDropTargetListener(MapperManager mapperManager, Table draggableTable) {
        super(mapperManager);
        this.draggableTable = draggableTable;
    }

    @Override
    protected MapperManager getMapperManager() {
        return (MapperManager) super.getMapperManager();
    }

    @Override
    public UIManager getUiManager() {
        return (UIManager) super.getUiManager();
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        super.dragEnter(event);
        draggableTable.setFocus();
    }

    @Override
    public void dragOver(DropTargetEvent event) {

        super.dragOver(event);
        UIManager uiManager = getUiManager();
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();

        // System.out.println("\n>>dragOver");

        DraggedData draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        DropContextAnalyzer analyzer = analyzeDropTarget(event, draggedData);

        fillEvent(event, analyzer);
        InsertionIndicator insertionIndicator = retrieveInsertionIndicator();
        if (analyzer.isDropValid()) {

            draggingInfosPopup.setOutputToOutputMode(analyzer.isOutputToOutput());

            if (analyzer.isInputToInput()) {
                draggingInfosPopup.setExpressionContext(true);
                draggingInfosPopup.setInsertionEntryContext(false);
            } else if (analyzer.isCursorOverExpressionCell()) {
                draggingInfosPopup.setExpressionContext(true);
            } else if (analyzer.targetTableIsFiltersTable()) {
                draggingInfosPopup.setExpressionContext(true);
            } else {
                draggingInfosPopup.setExpressionContext(false);
            }

            draggingInfosPopup.setMapOneToOneMode(analyzer.isMapOneToOneMode(), analyzer.isMapOneToOneAuthorized());
            Point cursorPosition = new Point(event.x, event.y);
            Integer itemIndexWhereInsertFromPosition = getItemIndexFromPosition(cursorPosition);
            if (analyzer.isMapOneToOneMode() && analyzer.isMapOneToOneAuthorized()) {
                int size = draggedData.getTransferableEntryList().size();
                if (itemIndexWhereInsertFromPosition != null) {
                    draggableTable.setSelection(itemIndexWhereInsertFromPosition, itemIndexWhereInsertFromPosition + size - 1);
                    if (!analyzer.targetTableIsFiltersTable()
                            && itemIndexWhereInsertFromPosition + size - 1 >= draggableTable.getItemCount()) {
                        insertionIndicator.updatePosition(draggableTable, draggableTable.getItemCount());
                        insertionIndicator.setVisible(true);
                        draggingInfosPopup.setInsertionEntryContext(true);
                    } else {
                        insertionIndicator.setVisible(false);
                        draggingInfosPopup.setInsertionEntryContext(false);
                    }
                } else {
                    draggableTable.deselectAll();
                    insertionIndicator.setVisible(false);
                    draggingInfosPopup.setInsertionEntryContext(false);
                }
            } else {
                if (!analyzer.isTableSourceAndTargetAreSame()) {
                    draggableTable.deselectAll();
                }
                if (itemIndexWhereInsertFromPosition != null && !analyzer.isInsertionEntryMode()) {
                    draggableTable.setSelection(itemIndexWhereInsertFromPosition);
                }
                if (!analyzer.targetTableIsFiltersTable()) {
                    updateInsertionIndicator(event);
                    insertionIndicator.setVisible(analyzer.isInsertionEntryMode());
                }
                draggingInfosPopup.setInsertionEntryContext(analyzer.isInsertionEntryMode());
            }
            draggingInfosPopup.setDropInvalid(false, false);
        } else {
            draggingInfosPopup.setMapOneToOneMode(false, false);
            draggingInfosPopup.setExpressionContext(false);
            draggingInfosPopup.setInsertionEntryContext(false);
            draggingInfosPopup.setDropInvalid(true, analyzer.isInvalidKeyPressed());
        }
    }

    private void configurePopupInfos(DropContextAnalyzer analyzer) {
        AbstractUIManager uiManager = getUiManager();
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();

        if (analyzer.isDropValid()) {

            draggingInfosPopup.setOutputToOutputMode(analyzer.isOutputToOutput());

            if (uiManager.isCtrlPressed()) {
                draggingInfosPopup.setOverwriteMode(true);
            } else {
                draggingInfosPopup.setOverwriteMode(false);
            }

            if (uiManager.isShiftPressed() && analyzer.isMapOneToOneAuthorized()) {
                draggingInfosPopup.setMapOneToOneMode(true, true);
            } else {
                draggingInfosPopup.setMapOneToOneMode(false, analyzer.isMapOneToOneAuthorized());
            }

            if (analyzer.isInputToInput()) {
                draggingInfosPopup.setExpressionContext(true);
                draggingInfosPopup.setInsertionEntryContext(false);
            } else if (analyzer.isCursorOverExpressionCell()) {
                draggingInfosPopup.setExpressionContext(true);
                draggingInfosPopup.setInsertionEntryContext(analyzer.isInsertionEntryMode());
            } else if (analyzer.targetTableIsFiltersTable()) {
                draggingInfosPopup.setExpressionContext(true);
                draggingInfosPopup.setInsertionEntryContext(false);
            } else {
                draggingInfosPopup.setExpressionContext(false);
                draggingInfosPopup.setInsertionEntryContext(true);
            }
            draggingInfosPopup.setDropInvalid(false, false);
        } else {
            draggingInfosPopup.setExpressionContext(false);
            draggingInfosPopup.setMapOneToOneMode(false, false);
            draggingInfosPopup.setInsertionEntryContext(false);
            draggingInfosPopup.setDropInvalid(true, analyzer.isInvalidKeyPressed());
        }
    }

    private DropContextAnalyzer analyzeDropTarget(DropTargetEvent event, DraggedData draggedData) {
        DropContextAnalyzer analyzer = new DropContextAnalyzer(draggedData, event, getMapperManager());
        return analyzer;
    }

    private void fillEvent(DropTargetEvent event, DropContextAnalyzer analyzer) {
        event.feedback = analyzer.getFeedback();
        event.detail = analyzer.getDetail();
    }

    /**
     *
     * DOC amaumont Comment method "updateInsertionIndicator".
     *
     * @param event
     * @return true if droppable
     */
    private void updateInsertionIndicator(DropTargetEvent event) {
        UIManager uiManager = getUiManager();
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();

        // ////////////////////////////////
        // to resolve graphical bug between popupInfos and InsertionIndicator
        InsertionIndicator insertionIndicator = retrieveInsertionIndicator();
        Rectangle popupBounds = draggingInfosPopup.getBounds();
        Point positionPopupFromMapperShellOrigin = draggingInfosPopup.getPositionFromMapperShellOrigin();
        Rectangle boundsPopupFromMapperShellOrigin = new Rectangle(positionPopupFromMapperShellOrigin.x,
                positionPopupFromMapperShellOrigin.y, popupBounds.width, popupBounds.height);
        boolean intersect = insertionIndicator.isLeftArrowMustBeRefreshed(boundsPopupFromMapperShellOrigin);

        Point eventPosition = new Point(event.x, event.y);
        // int itemIndexTarget = getItemIndexWhereInsertFromPosition(eventPosition);
        int itemIndexTarget = TableUtils.getItemIndexWhereInsertFromPosition(draggableTable, eventPosition);
        insertionIndicator.updatePosition(draggableTable, itemIndexTarget);

        if (WindowSystem.isGTK()) {
            if (insertionIndicator.isRightArrowVisible() && !eventPosition.equals(lastDragPosition)) {
                insertionIndicator.setRightArrowVisible(false);
                insertionIndicator.setLefArrowVisible(false);
            } else {
                insertionIndicator.setRightArrowVisible(insertionIndicator.isVisible());
                insertionIndicator.setLefArrowVisible(insertionIndicator.isVisible());
            }

        } else {
            if (isIntersectAtPreviousDragOver || intersect) {
                insertionIndicator.setLefArrowVisible(false);
                isIntersectAtPreviousDragOver = false;
            } else {
                insertionIndicator.setLefArrowVisible(insertionIndicator.isVisible());
            }
        }

        isIntersectAtPreviousDragOver = intersect;
        lastDragPosition = eventPosition;
    }

    private InsertionIndicator retrieveInsertionIndicator() {
        DataMapTableView dataMapTableViewTarget = getMapperManager().retrieveDataMapTableView(draggableTable);
        TablesZoneView targetTablesZoneView = getUiManager().getTablesZoneView(dataMapTableViewTarget);
        InsertionIndicator insertionIndicator = targetTablesZoneView.getInsertionIndicator();
        return insertionIndicator;
    }

    @Override
    public void dragLeave(DropTargetEvent event) {
        // System.out.println("\n>>dragLeave");
        // System.out.println(event);
        super.dragLeave(event);

        DraggedData draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        DropContextAnalyzer analyzer = analyzeDropTarget(event, draggedData);

        UIManager uiManager = getUiManager();
        if (!analyzer.isTableSourceAndTargetAreSame()) {
            draggableTable.deselectAll();
        }
        retrieveInsertionIndicator().setVisible(false);
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();
        draggingInfosPopup.setExpressionContext(false);
        draggingInfosPopup.setInsertionEntryContext(false);
        uiManager.setDragging(false);
    }

    @Override
    public void dragOperationChanged(DropTargetEvent event) {
        // System.out.println("\n>>dragOperationChanged");
        // showInfos(event);
        // super.dragOperationChanged(event);
        DraggedData draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        detectPressedKeys(event);
        DropContextAnalyzer analyzer = analyzeDropTarget(event, draggedData);

        configurePopupInfos(analyzer);

        UIManager uiManager = getUiManager();
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();
        draggingInfosPopup.updateVisibleLabels();

        fillEvent(event, analyzer);
        getUiManager().setCurrentDragDetail(event.detail);
    }

    // private void showInfos(DropTargetEvent event) {
    // System.out.println(event);
    // System.out.println("event.feedback=" + event.feedback);
    // System.out.println("event.detail=" + event.detail);
    // System.out.println("event.operations=" + event.operations);
    //
    // System.out.println("DND.DROP_DEFAULT=" + DND.DROP_DEFAULT);
    // System.out.println("DND.DROP_COPY=" + DND.DROP_COPY);
    // System.out.println("DND.DROP_MOVE=" + DND.DROP_MOVE);
    // System.out.println("DND.DROP_LINK=" + DND.DROP_LINK);
    // System.out.println("DND.DROP_TARGET_MOVE=" + DND.DROP_TARGET_MOVE);
    // }
    //
    @Override
    public void drop(DropTargetEvent event) {

        super.drop(event);

        retrieveInsertionIndicator().setVisible(false);

        UIManager uiManager = getUiManager();

        DraggedData draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        DropContextAnalyzer analyzer = analyzeDropTarget(event, draggedData);

        // System.out.println("\n>>drop");
        // System.out.println(event);
        Point cursorPosition = new Point(event.x, event.y);
        // int startInsertAtThisIndex = getItemIndexWhereInsertFromPosition(cursorPosition);
        int startInsertAtThisIndex = TableUtils.getItemIndexWhereInsertFromPosition(draggableTable, cursorPosition);
        IDbLanguage currentLanguage = getMapperManager().getCurrentLanguage();
        DataMapTableView dataMapTableViewTarget = getMapperManager().retrieveDataMapTableView(draggableTable);
        Zone zoneTarget = dataMapTableViewTarget.getZone();

        uiManager.selectDataMapTableView(dataMapTableViewTarget, true, false);
        MetadataTableEditorView metadataEditorView = getUiManager().getMetadataEditorView(dataMapTableViewTarget.getZone());
        List<TransferableEntry> transferableEntryList = draggedData.getTransferableEntryList();
        int currentIndex = startInsertAtThisIndex;
        uiManager.clearLastCreatedInOutColumnEntries();

        draggableTable.deselectAll();

        ITableEntry currentEntryTarget = getEntryFromPosition(cursorPosition);

        ArrayList<String> columnsBeingAdded = new ArrayList<String>();
        ArrayList<Integer> columnIndicesToSelect = new ArrayList<Integer>();
        ArrayList<ITableEntry> sourceEntriesOfEntriesBeingAdded = new ArrayList<ITableEntry>();
        ArrayList<IMetadataColumn> metadataColumnsBeingAdded = new ArrayList<IMetadataColumn>();

        boolean targetTableIsFiltersTable = analyzer.targetTableIsFiltersTable();
        boolean atLeastOneEntryInserted = false;

        boolean insertionEntryMode = analyzer.isInsertionEntryMode();
        boolean mapEachSourceToNextTargets = analyzer.isMapOneToOneMode();

        TableViewerCreator tableViewerCreatorTarget = null;
        if (targetTableIsFiltersTable) {
            if (analyzer.targetTableIsWhereFiltersTable()) {
                tableViewerCreatorTarget = dataMapTableViewTarget.getTableViewerCreatorForWhereFilters();
            } else {
                tableViewerCreatorTarget = dataMapTableViewTarget.getTableViewerCreatorForOtherFilters();
            }
        } else {
            tableViewerCreatorTarget = dataMapTableViewTarget.getTableViewerCreatorForColumns();
        }

        // MapperDropCommand dropCommand = new MapperDropCommand();

        // MetadataEditorEvent metadataEditorEvent = new MetadataEditorEvent(MetadataEditorEvent.TYPE.ADD);
        ITableEntry lastEntryTarget = null;
        for (TransferableEntry transferableEntry : transferableEntryList) {
            ITableEntry tableEntrySource = transferableEntry.getTableEntrySource();
            IMetadataColumn metadataColumnDragged = transferableEntry.getMetadataColumn();
            Zone zoneSourceEntry = (Zone) transferableEntry.getZoneSourceEntry();

            TableEntryLocation tableEntryLocationTarget = new TableEntryLocation(dataMapTableViewTarget.getDataMapTable()
                    .getName(), tableEntrySource.getName());

            if (zoneSourceEntry == Zone.INPUTS && zoneTarget == Zone.INPUTS
                    && tableEntrySource.getParentName().equals(tableEntryLocationTarget.tableName)) {

                continue;

            } else if (currentEntryTarget != null && !insertionEntryMode) {

                modifyExistingEntry(uiManager, analyzer, currentLanguage, currentEntryTarget, columnIndicesToSelect,
                        tableViewerCreatorTarget, lastEntryTarget, tableEntrySource, zoneSourceEntry);

            } else {
                String columnName = transferableEntry.getTableEntrySource().getName();
                tableEntryLocationTarget = getMapperManager().findUniqueLocation(tableEntryLocationTarget,
                        dataMapTableViewTarget.getDataMapTable());
                columnName = tableEntryLocationTarget.columnName;
                if (currentEntryTarget == null && analyzer.isMapOneToOneMode()) {
                    currentIndex = tableViewerCreatorTarget.getInputList().size();
                    columnIndicesToSelect.add(currentIndex);
                }
                if (zoneSourceEntry == Zone.INPUTS && zoneTarget == Zone.OUTPUTS) {
                    insertNewInOutEntryFromInputEntry(sourceEntriesOfEntriesBeingAdded, metadataColumnsBeingAdded,
                    // metadataEditorEvent,
                            tableEntrySource, metadataColumnDragged, columnName);
                    atLeastOneEntryInserted = true;

                } else if (zoneSourceEntry == Zone.OUTPUTS && zoneTarget == Zone.OUTPUTS) {

                    insertOutpuEntryCopyToOtherOutput(sourceEntriesOfEntriesBeingAdded, metadataColumnsBeingAdded,
                    // metadataEditorEvent,
                            tableEntrySource, metadataColumnDragged, columnName);
                    atLeastOneEntryInserted = true;

                } else {
                    // throw new IllegalStateException("Drop case not found !");
                }
                columnsBeingAdded.add(columnName);

            }

            lastEntryTarget = currentEntryTarget;
            if (mapEachSourceToNextTargets && currentEntryTarget != null) {
                currentEntryTarget = getNextEntryTarget(currentEntryTarget, tableViewerCreatorTarget);
            }

        } // for TransferableEntry transferableEntry : transferableEntryList

        if (!atLeastOneEntryInserted) {
            tableViewerCreatorTarget.getTableViewer().refresh();
        } else {
            updateExpressionsOfInsertedEntries(currentLanguage, metadataEditorView, currentIndex,
                    sourceEntriesOfEntriesBeingAdded, targetTableIsFiltersTable, tableViewerCreatorTarget
                    // , metadataEditorEvent
                    , metadataColumnsBeingAdded);

            for (int i = currentIndex; i < currentIndex + sourceEntriesOfEntriesBeingAdded.size(); i++) {
                columnIndicesToSelect.add(i);
            }

        }
        dataMapTableViewTarget.resizeAtExpandedSize();

        uiManager.unselectAllEntriesOfAllTables();

        uiManager.refreshBackground(true, false);
        if (metadataEditorView != null && !targetTableIsFiltersTable) {
            metadataEditorView.getTableViewerCreator().getTableViewer().refresh();
        }

        int[] selection = ArrayUtils.toPrimitive(columnIndicesToSelect.toArray(new Integer[0]));
        tableViewerCreatorTarget.getSelectionHelper().setSelection(selection);
        ISelection iselection = tableViewerCreatorTarget.getTableViewer().getSelection();
        List<ITableEntry> selectedEntries = uiManager.extractSelectedTableEntries(iselection);

        uiManager.unselectAllOutputMetaDataEntries();
        uiManager.unselectAllInputMetaDataEntries();

        uiManager.parseAllExpressionsForAllTables();
        getMapperManager().getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);

        getUiManager().refreshSqlExpression();

        uiManager.selectLinks(dataMapTableViewTarget, selectedEntries, targetTableIsFiltersTable, true);
        // refresh for cell data disappear on MAC
        tableViewerCreatorTarget.getTableViewer().refresh();
        tableViewerCreatorTarget.getTable().setFocus();

        uiManager.setDragging(false);
    }

    /**
     * DOC amaumont Comment method "modifyExistingEntry".
     *
     * @param uiManager
     * @param analyzer
     * @param currentLanguage
     * @param currentEntryTarget
     * @param columnIndicesToSelect
     * @param tableViewerCreatorTarget
     * @param lastEntryTarget
     * @param tableEntrySource
     * @param zoneSourceEntry
     */
    private void modifyExistingEntry(UIManager uiManager, DropContextAnalyzer analyzer, IDbLanguage currentLanguage,
            ITableEntry currentEntryTarget, ArrayList<Integer> columnIndicesToSelect,
            TableViewerCreator tableViewerCreatorTarget, ITableEntry lastEntryTarget, ITableEntry tableEntrySource,
            Zone zoneSourceEntry) {
        boolean overwrite = (lastEntryTarget != currentEntryTarget && analyzer.isOverwriteExpression());
        modifyExistingExpression(currentLanguage, currentEntryTarget, tableEntrySource, overwrite, zoneSourceEntry);
        uiManager.parseExpression(currentEntryTarget.getExpression(), currentEntryTarget, false, true, true);

        if (currentEntryTarget instanceof InputColumnTableEntry) {
            setDefaultOperator((InputColumnTableEntry) currentEntryTarget);
        }

        int indexOfEntry = tableViewerCreatorTarget.getInputList().indexOf(currentEntryTarget);
        columnIndicesToSelect.add(indexOfEntry);
    }

    /**
     * DOC amaumont Comment method "updateExpressionsOfInsertedEntries".
     *
     * @param uiManager
     * @param currentLanguage
     * @param metadataEditorView
     * @param currentIndex
     * @param sourceEntriesOfEntriesBeingAdded
     * @param targetTableIsConstraintsTable
     * @param tableViewerCreatorTarget
     * @param metadataColumnsBeingAdded
     * @param metadataEditorEvent
     */
    @SuppressWarnings("unchecked")
    private void updateExpressionsOfInsertedEntries(IDbLanguage currentLanguage, MetadataTableEditorView metadataEditorView,
            int currentIndex, ArrayList<ITableEntry> sourceEntriesOfEntriesBeingAdded, boolean targetTableIsConstraintsTable,
            TableViewerCreator tableViewerCreatorTarget, ArrayList<IMetadataColumn> metadataColumnsBeingAdded
    // , MetadataEditorEvent metadataEditorEvent
    ) {
        UIManager uiManager = getUiManager();

        if (metadataEditorView != null && !targetTableIsConstraintsTable) {
            metadataEditorView.getMetadataTableEditor().addAll(currentIndex, metadataColumnsBeingAdded);

            // metadataEditorEvent.indexStartInsert = currentIndex;
            // IAction action = MetadataEditorActionFactory.getInstance().getAction(metadataEditorView,
            // metadataEditorEvent);
            // action.run(metadataEditorEvent);

            Zone zoneTarget = getMapperManager().retrieveDataMapTableView(tableViewerCreatorTarget.getTable()).getZone();

            List<IColumnEntry> lastCreatedTableEntries = uiManager.getLastCreatedInOutColumnEntries();
            for (int i = 0; i < lastCreatedTableEntries.size(); i++) {
                ITableEntry tableEntrySource = sourceEntriesOfEntriesBeingAdded.get(i);
                ITableEntry dataMapTableEntry = lastCreatedTableEntries.get(i);
                if (zoneTarget == Zone.INPUTS) {
                    setDefaultOperator((InputColumnTableEntry) dataMapTableEntry);
                }
                DataMapTableView dataMapTableView = getMapperManager().retrieveIDataMapTableView(tableEntrySource.getParent());
                Zone zoneSource = dataMapTableView.getZone();
                String location = null;
                if (zoneSource == Zone.OUTPUTS) {
                    location = tableEntrySource.getExpression();
                } else {
                    location = currentLanguage.getLocation(tableEntrySource.getParentName(), tableEntrySource.getName());
                }

                if (location != null) {
                    dataMapTableEntry.setExpression(location + " "); //$NON-NLS-1$
                }
            }
        }

        tableViewerCreatorTarget.getTableViewer().refresh();

        List<ITableEntry> refreshedTableEntriesList = tableViewerCreatorTarget.getInputList();
        for (ITableEntry tableEntry : refreshedTableEntriesList) {
            uiManager.parseExpression(tableEntry.getExpression(), tableEntry, false, true, false);
        }

    }

    /**
     * DOC amaumont Comment method "setDefaultOperator".
     *
     * @param dataMapTableEntry
     */
    private void setDefaultOperator(InputColumnTableEntry inputEntry) {
        if (inputEntry.getOperator() == null || inputEntry.getOperator().equals("")) { //$NON-NLS-1$
            inputEntry.setOperator(getMapperManager().getCurrentLanguage().getOperatorsManager().getDefaultEqualOperator()
                    .getOperator());
        }
    }

    @SuppressWarnings("unchecked")
    private ITableEntry getNextEntryTarget(ITableEntry currentEntryTarget, TableViewerCreator tableViewerCreatorTarget) {
        // mapperManager.get
        // currentEntryTarget.getParent()
        if (currentEntryTarget == null) {
            throw new IllegalArgumentException("currentEntryTarget should'nt be null"); //$NON-NLS-1$
        }
        List<ITableEntry> tableEntries = tableViewerCreatorTarget.getInputList();
        int indexCurrentEntryTarget = tableEntries.indexOf(currentEntryTarget);
        if (indexCurrentEntryTarget + 1 >= tableEntries.size()) {
            return null;
        } else {
            return tableEntries.get(indexCurrentEntryTarget + 1);
        }
    }

    private void insertNewInOutEntryFromInputEntry(ArrayList<ITableEntry> sources,
            ArrayList<IMetadataColumn> metadataColumnsBeingAdded,
            // MetadataEditorEvent metadataEditorEvent,
            ITableEntry tableEntrySource, IMetadataColumn metadataColumnDragged, String columnName) {
        MetadataColumn metadataColumn = new MetadataColumn(metadataColumnDragged);
        metadataColumn.setLabel(columnName);
        metadataColumnsBeingAdded.add(metadataColumn);
        // metadataEditorEvent.entries.add(metadataColumn);
        sources.add(tableEntrySource);
    }

    private void insertOutpuEntryCopyToOtherOutput(ArrayList<ITableEntry> sources,
            ArrayList<IMetadataColumn> metadataColumnsBeingAdded,
            // MetadataEditorEvent metadataEditorEvent,
            ITableEntry tableEntrySource, IMetadataColumn metadataColumnDragged, String columnName) {
        MetadataColumn metadataColumn = new MetadataColumn(metadataColumnDragged);
        metadataColumn.setLabel(columnName);
        metadataColumnsBeingAdded.add(metadataColumn);
        // metadataEditorEvent.entries.add(metadataColumn);
        sources.add(tableEntrySource);
    }

    private void insertNewOutputEntryFromVarEntry(ArrayList<ITableEntry> sources,
            ArrayList<IMetadataColumn> metadataColumnsBeingAdded, ITableEntry tableEntrySource, String columnName) {
        MetadataColumn metadataColumn = new MetadataColumn();
        metadataColumn.setLabel(columnName);
        metadataColumn.setTalendType(((VarTableEntry) tableEntrySource).getType());
        metadataColumn.setNullable(((VarTableEntry) tableEntrySource).isNullable());
        metadataColumnsBeingAdded.add(metadataColumn);
        sources.add(tableEntrySource);
    }

    private void modifyExistingExpression(IDbLanguage currentLanguage, ITableEntry entryTarget, ITableEntry tableEntrySource,
            boolean overwriteExpression, Zone zoneSourceEntry) {
        String expression = null;
        if (zoneSourceEntry == Zone.OUTPUTS) {
            expression = tableEntrySource.getExpression();
        } else {
            expression = currentLanguage.getLocation(tableEntrySource.getParentName(), tableEntrySource.getName());
        }
        if (expression == null) {
            return;
        }
        String expressionToWrite = null;
        if (overwriteExpression) {
            expressionToWrite = expression + "  "; //$NON-NLS-1$
        } else {
            String currentTargetExpression = entryTarget.getExpression();
            if (currentTargetExpression == null) {
                currentTargetExpression = ""; //$NON-NLS-1$
            }
            String space = ""; //$NON-NLS-1$
            boolean isEmpty = "".equals(currentTargetExpression.trim()); //$NON-NLS-1$
            if (currentTargetExpression.endsWith("  ")) { //$NON-NLS-1$
                space = ""; //$NON-NLS-1$
            } else if (!isEmpty && currentTargetExpression.endsWith(" ")) { //$NON-NLS-1$
                space = " "; //$NON-NLS-1$
            } else if (!isEmpty) {
                space = "  "; //$NON-NLS-1$
            }
            expressionToWrite = currentTargetExpression + space + expression + " "; //$NON-NLS-1$
        }
        entryTarget.setExpression(expressionToWrite);

    }

    @Override
    public void dropAccept(DropTargetEvent event) {
        // System.out.println("\n>>dropAccept");
        // System.out.println(event);
        super.dropAccept(event);

    }

    /**
     *
     * DOC amaumont Comment method "getItemIndexFromPosition".
     *
     * @param cursorPosition
     * @return
     */
    private Integer getItemIndexFromPosition(Point cursorPosition) {
        TableItem[] tableItems = draggableTable.getItems();
        Point point = new Point(cursorPosition.x, cursorPosition.y);
        TableItem tableItemBehindCursor = getTableItemFromPosition(point);
        if (tableItemBehindCursor != null) {
            for (int i = 0; i < tableItems.length; i++) {
                if (tableItems[i] == tableItemBehindCursor) {
                    return i;
                }
            }
        }
        return null;
    }

    /**
     *
     * DOC amaumont Comment method "getTableItemFromPosition".
     *
     * @param cursorPosition
     * @return
     */
    private TableItem getTableItemFromPosition(Point cursorPosition) {
        Point point = new Point(cursorPosition.x, cursorPosition.y);
        Point pointCursor = draggableTable.toControl(point.x, point.y);
        return draggableTable.getItem(pointCursor);
    }

    /**
     *
     * DOC amaumont Comment method "getEntryFromPosition".
     *
     * @param cursorPosition
     * @return
     */
    private ITableEntry getEntryFromPosition(Point cursorPosition) {
        TableItem tableItemBehindCursor = getTableItemFromPosition(cursorPosition);
        if (tableItemBehindCursor != null) {
            return (ITableEntry) tableItemBehindCursor.getData();
        } else {
            return null;
        }
    }

}
