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
package org.talend.designer.dbmap.ui.dnd;

import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.designer.abstractmap.ui.dnd.DraggedData;
import org.talend.designer.abstractmap.ui.dnd.TableEntriesTransfer;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DropContextAnalyzer.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class DropContextAnalyzer {

    private DropTargetEvent event;

    private DraggedData draggedData;

    private MapperManager mapperManager;

    private Zone zoneTarget;

    private Zone zoneSource;

    private DataMapTableView dataMapTableViewTarget;

    private DataMapTableView dataMapTableViewSource;

    private boolean isDropValid;

    private TableItem tableItemSource;

    private Table currentTableTarget;

    private int feedback;

    private int detail;

    private boolean insertionEntryMode;

    private boolean mapOneToOneMode;

    private boolean isCursorOverExpressionCell;

    private boolean overwriteExpression;

    private boolean mapOneToOneAuthorized;

    private boolean isInputToInput;

    private boolean isOutputToOutput;

    private boolean invalidKeyPressed;

    public DropContextAnalyzer(DraggedData draggedData, DropTargetEvent event, MapperManager mapperManager) {
        super();
        this.draggedData = draggedData;
        this.event = event;
        this.mapperManager = mapperManager;
        init();
    }

    /**
     * DOC amaumont Comment method "init".
     */
    private void init() {

        DropTarget dropTarget = (DropTarget) event.widget;
        currentTableTarget = (Table) dropTarget.getControl();
        dataMapTableViewTarget = mapperManager.retrieveDataMapTableView(currentTableTarget);
        zoneTarget = dataMapTableViewTarget.getZone();
        draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        dataMapTableViewSource = (DataMapTableView) draggedData.getDataMapTableViewSource();
        tableItemSource = draggedData.getTableItemSource();
        zoneSource = dataMapTableViewSource.getZone();

        analyzeCursorOverExpressionCell();

        invalidKeyPressed = hasInvalidKeyPressed();

        isDropValid = checkDropIsValid();

        analyzeForFeedback();
        analyzeForDetail();
        // System.out.println("isCursorOverExpressionCell="+isCursorOverExpressionCell);
    }

    /**
     * DOC amaumont Comment method "checkDropHasValidTarget".
     */
    private boolean checkDropIsValid() {

        isInputToInput = false;
        mapOneToOneAuthorized = true;

        if (targetTableIsFiltersTable() || draggedData.getTransferableEntryList().size() <= 1) {
            mapOneToOneAuthorized = false;
        }

        if (invalidKeyPressed) {
            return false;
        }

        /*
         * INPUT => INPUT
         */
        if (zoneSource == Zone.INPUTS && zoneTarget == Zone.INPUTS) {

            isInputToInput = true;
            mapOneToOneAuthorized = false;
            List<InputTable> inputTables = mapperManager.getInputTables();
            int indexSourceInputTable = inputTables.indexOf(dataMapTableViewSource.getDataMapTable());
            int indexTargetInputTable = inputTables.indexOf(dataMapTableViewTarget.getDataMapTable());
            if (indexSourceInputTable == indexTargetInputTable) {
                return false;
            } else {
                return true;
            }

        }

        /*
         * OUTPUT => OUTPUT
         */
        if (zoneSource == Zone.OUTPUTS && zoneTarget == Zone.OUTPUTS) {

            isOutputToOutput = true;
            mapOneToOneAuthorized = true;
            List<OutputTable> outputTables = mapperManager.getOutputTables();
            int indexSourceOutputTable = outputTables.indexOf(dataMapTableViewSource.getDataMapTable());
            int indexTargetOutputTable = outputTables.indexOf(dataMapTableViewTarget.getDataMapTable());
            if (indexSourceOutputTable == indexTargetOutputTable) {
                return false;
            } else {
                return true;
            }

        }

        TableItem tableItemTarget = getTableItemFromPosition(new Point(event.x, event.y));

        if (zoneSource == Zone.OUTPUTS && zoneTarget == Zone.INPUTS) {
            /*
             * OUTPUT => OUTPUT OUTPUT => INPUT
             */
            return false;
        }

        return true;
    }

    private boolean hasInvalidKeyPressed() {
        UIManager uiManager = mapperManager.getUiManager();
        if (WindowSystem.isGTK() && uiManager.isCtrlPressed() ^ uiManager.isShiftPressed()) {
            return true;
        }
        return false;
    }

    public int getDetail() {
        return this.detail;
    }

    public int getFeedback() {
        return this.feedback;
    }

    /**
     * .
     */
    @SuppressWarnings("unchecked")
    private void analyzeCursorOverExpressionCell() {
        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(currentTableTarget);
        TableViewerCreator tableViewerCreatorForColumns = dataMapTableView.getTableViewerCreatorForColumns();
        Point pointCursor = currentTableTarget.toControl(event.x, event.y);
        if (tableViewerCreatorForColumns.getTable() != currentTableTarget) {
            isCursorOverExpressionCell = false;
            return;
        }

        int maxY = (WindowSystem.isWIN32() && currentTableTarget.getHeaderVisible() ? currentTableTarget.getHeaderHeight() : 0)
                + (currentTableTarget.getItemCount() * (currentTableTarget.getItemHeight() + currentTableTarget.getBorderWidth() + (WindowSystem
                        .isGTK() ? 2 : 0)));

        if (pointCursor.y < 0 || pointCursor.y >= maxY) {
            isCursorOverExpressionCell = false;
            return;
        }

        // searching current column index
        int currentColumnIndex = 0;
        TableColumn[] columns = currentTableTarget.getColumns();
        for (int i = 0, width = 0; i < columns.length; i++) {
            TableColumn column = columns[i];
            int widthColumn = column.getWidth();
            if (pointCursor.x >= width && pointCursor.x <= width + widthColumn) {
                currentColumnIndex = i;
                break;
            }
            width += widthColumn;
        }

        List<TableViewerCreatorColumnNotModifiable> viewerColumns = tableViewerCreatorForColumns.getColumns();
        TableViewerCreatorColumnNotModifiable viewerColumn = viewerColumns.get(currentColumnIndex);
        if (DataMapTableView.ID_EXPRESSION_COLUMN.equals(viewerColumn.getId())) {
            isCursorOverExpressionCell = true;
        } else {
            isCursorOverExpressionCell = false;
        }

    }

    private void analyzeForFeedback() {
        int dropFeedback = DND.FEEDBACK_SCROLL;
        boolean targetTableIsConstraintsTable = targetTableIsFiltersTable(dataMapTableViewTarget);

        if (isDropValid) {

            if (mapperManager.getUiManager().isCtrlPressed()) {
                overwriteExpression = true;
            } else {
                overwriteExpression = false;
            }

            if (zoneSource == Zone.INPUTS && zoneTarget == Zone.INPUTS) {
                dropFeedback |= DND.FEEDBACK_SELECT;
                mapOneToOneMode = false;
                insertionEntryMode = false;
            } else if (targetTableIsConstraintsTable) {
                mapOneToOneMode = false;
                insertionEntryMode = false;
                dropFeedback |= DND.FEEDBACK_SELECT;
            } else if (zoneSource == Zone.INPUTS && zoneTarget == Zone.OUTPUTS || zoneSource == Zone.OUTPUTS
                    && zoneTarget == Zone.OUTPUTS) {

                if (isCursorOverExpressionCell) {
                    insertionEntryMode = false;
                    if (mapperManager.getUiManager().isShiftPressed() && draggedData.getTransferableEntryList().size() > 1) {
                        mapOneToOneMode = true;
                    } else {
                        dropFeedback |= DND.FEEDBACK_SELECT;
                        mapOneToOneMode = false;
                    }
                } else {
                    mapOneToOneMode = false;
                    insertionEntryMode = true;
                }

            } else {
                mapOneToOneMode = false;
                insertionEntryMode = true;
            }
        }
        feedback = dropFeedback;
    }

    private void analyzeForDetail() {

        int dropOperation = DND.DROP_NONE;
        if (isDropValid) {
            if (overwriteExpression && !insertionEntryMode) {
                dropOperation = DND.DROP_LINK;
            } else {
                dropOperation = DND.DROP_COPY;
            }
        }
        detail = dropOperation;
    }

    private boolean targetTableIsFiltersTable(DataMapTableView target) {
        if (target.getZone() != Zone.OUTPUTS) {
            return false;
        }
        return (currentTableTarget == target.getTableViewerCreatorForWhereFilters().getTable() || currentTableTarget == target
                .getTableViewerCreatorForOtherFilters().getTable());
    }

    public boolean targetTableIsFiltersTable() {
        return targetTableIsFiltersTable(dataMapTableViewTarget);
    }

    public boolean targetTableIsWhereFiltersTable() {
        if (dataMapTableViewTarget.getZone() != Zone.OUTPUTS) {
            return false;
        }
        return currentTableTarget == dataMapTableViewTarget.getTableViewerCreatorForWhereFilters().getTable();
    }

    public boolean targetTableIsOtherFiltersTable() {
        if (dataMapTableViewTarget.getZone() != Zone.OUTPUTS) {
            return false;
        }
        return currentTableTarget == dataMapTableViewTarget.getTableViewerCreatorForOtherFilters().getTable();
    }

    private TableItem getTableItemFromPosition(Point cursorPosition) {
        Point pointCursor = currentTableTarget.toControl(cursorPosition.x, cursorPosition.y);
        return currentTableTarget.getItem(pointCursor);
    }

    private boolean dropVarsEntryIsValid(TableItem tableItemTarget) {
        Table table = tableItemTarget.getParent();
        TableItem[] tableItems = table.getItems();
        for (TableItem tableItem : tableItems) {
            if (tableItemTarget == tableItem) {
                break;
            }
            if (tableItemSource == tableItem) {
                return true;
            }
        }
        return false;
    }

    public boolean isDropValid() {
        return this.isDropValid;
    }

    public boolean isInsertionEntryMode() {
        return this.insertionEntryMode;
    }

    public boolean isMapOneToOneMode() {
        return this.mapOneToOneMode;
    }

    public boolean isOverwriteExpression() {
        return this.overwriteExpression;
    }

    public boolean isCursorOverExpressionCell() {
        return isCursorOverExpressionCell;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "\nDropContextAnalyzer instance:" + "\n isTargetEntryValid=" + isDropValid + "\n insertionIndicatorVisible=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + insertionEntryMode + "\n mapOneToOne=" + mapOneToOneMode + "\n overwriteExpression=" + overwriteExpression //$NON-NLS-1$ //$NON-NLS-2$
                + "\n isCursorOverExpressionColumn=" + isCursorOverExpressionCell; //$NON-NLS-1$
    }

    public boolean isMapOneToOneAuthorized() {
        return this.mapOneToOneAuthorized;
    }

    /**
     * DOC amaumont Comment method "isTableSourceAndTargetAreSame".
     */
    public boolean isTableSourceAndTargetAreSame() {
        if (tableItemSource == null) {
            return false;
        }
        if (currentTableTarget == null) {
            return false;
        }
        return tableItemSource.getParent() == currentTableTarget;
    }

    public boolean isInputToInput() {
        return this.isInputToInput;
    }

    public boolean isOutputToOutput() {
        return this.isOutputToOutput;
    }

    public void setOutputTuOutput(boolean isOutputTuOutput) {
        this.isOutputToOutput = isOutputTuOutput;
    }

    public boolean isInvalidKeyPressed() {
        return invalidKeyPressed;
    }

}
