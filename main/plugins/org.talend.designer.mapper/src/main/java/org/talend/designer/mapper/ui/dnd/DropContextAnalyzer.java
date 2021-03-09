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
package org.talend.designer.mapper.ui.dnd;

import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.ui.dnd.DraggedData;
import org.talend.designer.abstractmap.ui.dnd.TableEntriesTransfer;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
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

    private boolean isCursorOverHeader;

    private boolean overwriteExpression;

    private boolean mapOneToOneAuthorized;

    private boolean isInputToInput;

    private boolean isOutputToOutput;

    private boolean invalidKeyPressed;

    private StyledText currentStyledTextTarget;

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
        Control controlTarget = dropTarget.getControl();
        dataMapTableViewTarget = mapperManager.retrieveDataMapTableView(controlTarget);
        if (controlTarget instanceof Table) {
            currentTableTarget = (Table) dropTarget.getControl();
        } else if (controlTarget instanceof StyledText) {
            currentStyledTextTarget = (StyledText) dropTarget.getControl();
        } else {
            throw new IllegalArgumentException(Messages.getString("DropContextAnalyzer.0", controlTarget.toString())); //$NON-NLS-1$
        }
        zoneTarget = dataMapTableViewTarget.getZone();
        draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        dataMapTableViewSource = (DataMapTableView) draggedData.getDataMapTableViewSource();
        tableItemSource = draggedData.getTableItemSource();
        zoneSource = dataMapTableViewSource.getZone();

        if (currentTableTarget != null) {
            analyzeCursorOverExpressionCell();
        }

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

        if (targetIsExpressionFilterText()
                || (targetTableIsFiltersTable() || targetTableIsGlobalMapTable() || draggedData.getTransferableEntryList().size() <= 1)) {
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
            int indexTableSource = inputTables.indexOf(dataMapTableViewSource.getDataMapTable());
            int indexTableTarget = inputTables.indexOf(dataMapTableViewTarget.getDataMapTable());
            if (currentTableTarget != null) {
                if (indexTableSource >= indexTableTarget) {
                    /*
                     * INPUT => INPUT && index of table source >= index of table target
                     */
                    return false;
                } else {
                    return true;
                }
            } else if (currentStyledTextTarget != null) {
                if (indexTableSource > indexTableTarget) {
                    /*
                     * INPUT => INPUT && index of table source > index of table target
                     */
                    return false;
                } else {
                    return true;
                }
            } else {
                throw new IllegalStateException(Messages.getString("DropContextAnalyzer.notFound")); //$NON-NLS-1$
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
                /*
                 * INPUT => INPUT && index of table source >= index of table target
                 */
                return false;
            } else {
                // OUTPUT => OUTPUT
                return checkZoonTarget();
            }

        }

        if (currentTableTarget != null) {
            TableItem tableItemTarget = getTableItemFromPosition(new Point(event.x, event.y));
            if (zoneSource == Zone.VARS && zoneTarget == Zone.VARS && tableItemTarget != null) {
                if (tableItemSource == tableItemTarget || !dropVarsEntryIsValid(tableItemTarget)) {
                    /*
                     * VAR => VAR && (item source == item target || item target is invalid)
                     */
                    return false;
                }
            }
        }

        if (zoneSource == Zone.VARS && zoneTarget == Zone.INPUTS || zoneSource == Zone.OUTPUTS && zoneTarget == Zone.INPUTS
                || zoneSource == Zone.OUTPUTS && zoneTarget == Zone.VARS) {
            /*
             * VAR => INPUT OUTPUT => OUTPUT OUTPUT => INPUT OUTPUT => VAR
             */
            return false;
        }

        return checkZoonTarget();
    }

    private boolean checkZoonTarget() {
        IDataMapTable dataMapTable = dataMapTableViewTarget.getDataMapTable();
        if (zoneTarget == Zone.OUTPUTS) {
            OutputTable outputTable = (OutputTable) dataMapTable;
            if (outputTable.isErrorRejectTable()) {
                if (currentTableTarget != null && event != null) {
                    if (event.item instanceof TableItem) {
                        Object data = event.item.getData();
                        if (data instanceof OutputColumnTableEntry) {
                            String label = ((OutputColumnTableEntry) data).getName();
                            if (mapperManager.ERROR_REJECT_MESSAGE.equals(label)
                                    || mapperManager.ERROR_REJECT_STACK_TRACE.equals(label)) {
                                return false;
                            }
                        }
                    }
                }
            }
            boolean useRepositoryMeta = (outputTable.isRepository())
                    || (outputTable.getId() != null && !"".equals(outputTable.getId()));
            // forbiden dnd if use reposiotry metadat
            // Unable to drag a column from the main or lookup tables to the expression filter of an output table that
            // has a repository schema
            // for join table if main table use repository metadata
            if (!useRepositoryMeta && outputTable.getIsJoinTableOf() != null && !"".equals(outputTable.getIsJoinTableOf())) {
                OutputTable mainTable = mapperManager.getOutputTableByName(outputTable.getIsJoinTableOf());
                if (mainTable != null) {
                    useRepositoryMeta = mainTable.getId() != null && !"".equals(mainTable.getId());
                }
            }

            if (useRepositoryMeta && currentStyledTextTarget != null) {
                return true;
            }
            if (useRepositoryMeta && isCursorOverHeader || useRepositoryMeta && !isCursorOverExpressionCell) {
                return false;
            }
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
        boolean targetTableIsGlobalMapTable = targetTableIsGlobalMapTable(dataMapTableViewTarget);

        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(currentTableTarget);

        TableViewerCreator tableViewerCreator = null;
        if (targetTableIsGlobalMapTable) {
            tableViewerCreator = dataMapTableView.getTableViewerCreatorForGlobalMap();
        } else {
            tableViewerCreator = dataMapTableView.getTableViewerCreatorForColumns();
        }

        Point pointCursor = currentTableTarget.toControl(event.x, event.y);
        if (tableViewerCreator.getTable() != currentTableTarget) {
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

        if (pointCursor.y < currentTableTarget.getHeaderHeight()) {
            isCursorOverHeader = true;
        } else {
            isCursorOverHeader = false;
        }

        List<TableViewerCreatorColumnNotModifiable> viewerColumns = tableViewerCreator.getColumns();
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
        boolean targetTableIsGlobalMapTable = targetTableIsGlobalMapTable(dataMapTableViewTarget);

        if (isDropValid) {

            if (mapperManager.getUiManager().isCtrlPressed()) {
                overwriteExpression = true;
            } else {
                overwriteExpression = false;
            }

            if (zoneSource == Zone.INPUTS && zoneTarget == Zone.INPUTS && !targetTableIsGlobalMapTable) {
                mapOneToOneMode = false;
                insertionEntryMode = false;
                dropFeedback |= DND.FEEDBACK_SELECT;
            } else if (targetTableIsConstraintsTable) {
                mapOneToOneMode = false;
                insertionEntryMode = false;
                dropFeedback |= DND.FEEDBACK_SELECT;
            } else if (zoneSource == Zone.INPUTS && zoneTarget == Zone.INPUTS && targetTableIsGlobalMapTable
                    || zoneSource == Zone.INPUTS && zoneTarget == Zone.VARS || zoneSource == Zone.INPUTS
                    && zoneTarget == Zone.OUTPUTS || zoneSource == Zone.VARS && zoneTarget == Zone.VARS
                    || zoneSource == Zone.VARS && zoneTarget == Zone.OUTPUTS || zoneSource == Zone.OUTPUTS
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

        // System.out.println("insertionEntryMode="+insertionEntryMode);

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
        return currentTableTarget == target.getTableViewerCreatorForFilters().getTable();
    }

    public boolean targetTableIsFiltersTable() {
        return targetTableIsFiltersTable(dataMapTableViewTarget);
    }

    private boolean targetTableIsGlobalMapTable(DataMapTableView target) {
        if (target.getZone() != Zone.INPUTS) {
            return false;
        }
        return currentTableTarget == target.getTableViewerCreatorForGlobalMap().getTable();
    }

    public boolean targetTableIsGlobalMapTable() {
        return targetTableIsGlobalMapTable(dataMapTableViewTarget);
    }

    public boolean targetIsExpressionFilterText() {
        if (currentStyledTextTarget != null && dataMapTableViewTarget.getZone() == Zone.INPUTS
                && dataMapTableViewTarget.getZone() == Zone.OUTPUTS
                && currentStyledTextTarget == dataMapTableViewTarget.getExpressionFilterText()) {
            return true;
        } else {
            return false;
        }
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
