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

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;
import org.talend.commons.ui.runtime.utils.ControlUtils;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.dnd.DraggedData;
import org.talend.designer.abstractmap.ui.dnd.DraggingInfosPopup;
import org.talend.designer.abstractmap.ui.dnd.TableEntriesTransfer;
import org.talend.designer.abstractmap.ui.dnd.TransferableEntry;
import org.talend.designer.abstractmap.ui.listener.DefaultDropTargetListener;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class CompleteDropTargetStyledTextListener extends DefaultDropTargetListener {

    private StyledText draggableTargetControl;

    private boolean isIntersectAtPreviousDragOver;

    private Point lastDragPosition;

    /**
     * DOC amaumont CompleteDropTargetStyledTextListener constructor comment.
     *
     * @param mapperManager
     * @param text
     */
    public CompleteDropTargetStyledTextListener(MapperManager mapperManager, StyledText text) {
        super(mapperManager);
        this.draggableTargetControl = text;
    }

    @Override
    protected MapperManager getMapperManager() {
        return (MapperManager) super.getMapperManager();
    }

    @Override
    protected UIManager getUiManager() {
        return (UIManager) super.getUiManager();
    }

    public void dragEnter(DropTargetEvent event) {
        super.dragEnter(event);
        draggableTargetControl.setFocus();
    }

    public void dragOver(DropTargetEvent event) {

        super.dragOver(event);
        UIManager uiManager = getUiManager();
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();

        // System.out.println("\n>>dragOver");

        DraggedData draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        DropContextAnalyzer analyzer = analyzeDropTarget(event, draggedData);

        fillEvent(event, analyzer);
        if (analyzer.isDropValid()) {

            draggingInfosPopup.setExpressionContext(true);

        } else {
            draggingInfosPopup.setMapOneToOneMode(false, false);
            draggingInfosPopup.setExpressionContext(false);
            draggingInfosPopup.setInsertionEntryContext(false);
            draggingInfosPopup.setDropInvalid(true, analyzer.isInvalidKeyPressed());
        }
    }

    private void configurePopupInfos(DropContextAnalyzer analyzer) {
        UIManager uiManager = getUiManager();
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
            } else if (analyzer.targetIsExpressionFilterText() || analyzer.targetTableIsFiltersTable()) {
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

    public void dragLeave(DropTargetEvent event) {
        // System.out.println("\n>>dragLeave");
        // System.out.println(event);
        super.dragLeave(event);

        UIManager uiManager = getUiManager();
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();
        draggingInfosPopup.setExpressionContext(false);
        draggingInfosPopup.setInsertionEntryContext(false);
        uiManager.setDragging(false);
    }

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
    public void drop(DropTargetEvent event) {

        super.drop(event);

        UIManager uiManager = getUiManager();

        DraggedData draggedData = TableEntriesTransfer.getInstance().getDraggedData();
        DropContextAnalyzer analyzer = analyzeDropTarget(event, draggedData);

        // System.out.println("\n>>drop");
        // System.out.println(event);
        ILanguage currentLanguage = LanguageProvider.getCurrentLanguage();
        DataMapTableView dataMapTableViewTarget = getMapperManager().retrieveDataMapTableView(draggableTargetControl);

        uiManager.selectDataMapTableView(dataMapTableViewTarget, true, false);
        List<TransferableEntry> transferableEntryList = draggedData.getTransferableEntryList();

        ITableEntry currentEntryTarget = ((AbstractInOutTable) dataMapTableViewTarget.getDataMapTable()).getExpressionFilter();

        boolean overwrite = false;
        if (analyzer.isOverwriteExpression()) {
            currentEntryTarget.setExpression(""); //$NON-NLS-1$
        }
        for (TransferableEntry transferableEntry : transferableEntryList) {
            ITableEntry tableEntrySource = transferableEntry.getTableEntrySource();
            Zone zoneSourceEntry = (Zone) transferableEntry.getZoneSourceEntry();

            modifyExistingExpression(currentLanguage, currentEntryTarget, tableEntrySource, overwrite, zoneSourceEntry);

        } // for TransferableEntry transferableEntry : transferableEntryList
        uiManager.parseExpression(currentEntryTarget.getExpression(), currentEntryTarget, false, true, true);

        ControlUtils.setText(draggableTargetControl, currentEntryTarget.getExpression());

        uiManager.refreshBackground(true, false);

        uiManager.unselectAllInputMetaDataEntries();

        // uiManager.parseAllExpressionsForAllTables();
        getMapperManager().getProblemsManager().checkProblemsForTableEntry(currentEntryTarget, true);

        uiManager.selectLinks(dataMapTableViewTarget, Arrays.<ITableEntry> asList(currentEntryTarget), true, false);

        uiManager.setDragging(false);
    }

    private void modifyExistingExpression(ILanguage currentLanguage, ITableEntry entryTarget, ITableEntry tableEntrySource,
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

    public void dropAccept(DropTargetEvent event) {
        // System.out.println("\n>>dropAccept");
        // System.out.println(event);
        super.dropAccept(event);

    }

}
