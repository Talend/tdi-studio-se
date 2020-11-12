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
package org.talend.designer.dbmap.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.utils.image.ImageCapture;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.data.list.ListenableListEvent.TYPE;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.abstractmap.managers.AbstractMapperManager;
import org.talend.designer.abstractmap.managers.AbstractUIManager;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.dnd.DraggingInfosPopup;
import org.talend.designer.abstractmap.ui.listener.DropTargetOperationListener;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.abstractmap.ui.visualmap.link.LinkState;
import org.talend.designer.abstractmap.ui.visualmap.link.PointLinkDescriptor;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.external.data.ExternalDbMapUiProperties;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.model.table.AbstractInOutTable;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.ui.MapperUI;
import org.talend.designer.dbmap.ui.commands.DataMapTableViewSelectedCommand;
import org.talend.designer.dbmap.ui.tabs.TabFolderEditors;
import org.talend.designer.dbmap.ui.visualmap.TableEntryProperties;
import org.talend.designer.dbmap.ui.visualmap.link.Link;
import org.talend.designer.dbmap.ui.visualmap.link.StyleLinkFactory;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.table.EntryState;
import org.talend.designer.dbmap.ui.visualmap.table.InputDataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.table.OutputDataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.InputsZone;
import org.talend.designer.dbmap.ui.visualmap.zone.OutputsZone;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.TablesZoneView;
import org.talend.designer.dbmap.ui.visualmap.zone.toolbar.ToolbarInputZone;
import org.talend.designer.dbmap.ui.visualmap.zone.toolbar.ToolbarOutputZone;
import org.talend.designer.dbmap.ui.visualmap.zone.toolbar.ToolbarZone;
import org.talend.designer.dbmap.utils.DataMapExpressionParser;
import org.talend.designer.dbmap.utils.ParseExpressionResult;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: UIManager.java 2077 2007-02-15 11:16:46Z amaumont $
 *
 */
public class UIManager extends AbstractUIManager {

    private MapperManager mapperManager;

    private MapperUI mapperUI;

    private Composite refComposite;

    private int mapperResponse = SWT.NONE;

    private TableManager tableManager;

    private ILineSelectionListener inputsSelectionChangedListener;

    private ILineSelectionListener outputsSelectionChangedListener;

    private List<IColumnEntry> lastCreatedInOutColumnEntries = new ArrayList<IColumnEntry>();

    private ExternalDbMapUiProperties uiProperties;

    private OutputDataMapTableView currentSelectedOutputTableView;

    private InputDataMapTableView currentSelectedInputTableView;

    private Zone previousSelectedZone;

    private boolean previousSelectedTableIsConstraint;

    private DisposeListener commonMetadataDisposeListener;

    private StyleLinkFactory drawableLinkFactory;

    private IModifiedBeanListener<IMetadataColumn> inputModifiedBeanListener;

    private IModifiedBeanListener<IMetadataColumn> outputModifiedBeanListener;

    /**
     * DOC amaumont UIManager constructor comment.
     *
     * @param tableManager
     * @param manager
     */
    public UIManager(MapperManager mapperManager, TableManager tableManager) {
        super();
        this.mapperManager = mapperManager;
        this.tableManager = tableManager;
    }

    /**
     * Select a table view.
     *
     * @param dataMapTableView
     * @param useNewCommand
     * @param selectAllEntries TODO
     */
    public void selectDataMapTableView(final DataMapTableView dataMapTableView, boolean useNewCommand, boolean selectAllEntries) {

        TabFolderEditors tabFolderEditors = mapperUI.getTabFolderEditors();
        // tabFolderEditors.setSelection(TabFolderEditors.INDEX_TAB_METADATA_EDITOR);
        MetadataTableEditorView metadataTableEditorView = null;
        MetadataTableEditorView otherMetadataTableEditorView = null;
        final Zone currentZone = dataMapTableView.getZone();

        List<? extends IDataMapTable> tables = null;

        DataMapTableView previousSelectedTableView = null;
        DataMapTableView newSelectedTableView = null;

        if (currentZone == Zone.INPUTS) {
            metadataTableEditorView = tabFolderEditors.getInputMetaEditor();
            otherMetadataTableEditorView = tabFolderEditors.getOutputMetaEditor();
            tables = mapperManager.getInputTables();
            previousSelectedTableView = this.currentSelectedInputTableView;
            setCurrentSelectedInputTableView((InputDataMapTableView) dataMapTableView);
            newSelectedTableView = dataMapTableView;
        } else if (currentZone == Zone.OUTPUTS) {
            metadataTableEditorView = tabFolderEditors.getOutputMetaEditor();
            otherMetadataTableEditorView = tabFolderEditors.getInputMetaEditor();
            tables = mapperManager.getOutputTables();
            previousSelectedTableView = this.currentSelectedOutputTableView;
            newSelectedTableView = dataMapTableView;
            setCurrentSelectedOutputTableView((OutputDataMapTableView) dataMapTableView);
        }

        updateToolbarButtonsStates(currentZone);

        final AbstractInOutTable abstractDataMapTable = (AbstractInOutTable) mapperManager
                .retrieveIDataMapTable(dataMapTableView);
        MetadataTableEditor currentMetadataTableEditor = metadataTableEditorView.getMetadataTableEditor();
        final TableViewerCreator dataMapTVCreator = dataMapTableView.getTableViewerCreatorForColumns();
        final TableViewer dataMapTableViewer = dataMapTableView.getTableViewerCreatorForColumns().getTableViewer();
        if (currentMetadataTableEditor == null || currentMetadataTableEditor != null
                && newSelectedTableView != previousSelectedTableView) {

            if (useNewCommand) {
                DataMapTableViewSelectedCommand command = new DataMapTableViewSelectedCommand(this, previousSelectedTableView,
                        dataMapTableView);
                mapperManager.executeCommand(command);
            }

            currentMetadataTableEditor = new MetadataTableEditor(abstractDataMapTable.getMetadataTable(),
                    abstractDataMapTable.getTitle());

            currentMetadataTableEditor.setModifiedBeanListenable(metadataTableEditorView.getTableViewerCreator());

            final MetadataTableEditorView metadataTableEditorViewFinal = metadataTableEditorView;
            final TableViewerCreator metadataTVCreator = metadataTableEditorViewFinal.getTableViewerCreator();
            final MetadataTableEditor metadataTableEditor = currentMetadataTableEditor;

            modifySelectionChangedListener(currentZone, metadataTableEditorViewFinal, metadataTVCreator, metadataTableEditor,
                    dataMapTableView, previousSelectedTableView);

            // init actions listeners for list which contains metadata
            metadataTableEditor.addAfterOperationListListener(new IListenableListListener() {

                /**
                 * DOC acer Comment method "handleEvent".
                 *
                 * @param event
                 */
                @Override
                public void handleEvent(ListenableListEvent event) {

                    DataMapTableView view = mapperManager.retrieveIDataMapTableView(abstractDataMapTable);
                    if (event.type == TYPE.ADDED) {
                        // metadataEditorTableViewer.refresh();
                        List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.addedObjects;

                        lastCreatedInOutColumnEntries.clear();
                        if (event.index != null) {
                            int index = event.index;
                            for (IMetadataColumn metadataColumn : metadataColumns) {
                                addColumn(metadataColumn, dataMapTableView, index);
                            }
                        } else if (event.indicesTarget != null) {
                            List<Integer> indicesTarget = event.indicesTarget;
                            int lstSize = indicesTarget.size();
                            for (int i = 0; i < lstSize; i++) {
                                Integer indice = indicesTarget.get(i);
                                IMetadataColumn metadataColumn = metadataColumns.get(i);
                                addColumn(metadataColumn, dataMapTableView, indice);
                            }

                        } else {
                            throw new IllegalStateException(Messages.getString("UIManager.1")); //$NON-NLS-1$
                        }
                        refreshBackground(false, false);
                        if (event.index != null) {
                            dataMapTableView.changeSize(view.getPreferredSize(true, false, false), true, true);
                            dataMapTableViewer.refresh();
                            dataMapTVCreator.getSelectionHelper().setSelection(event.index);
                        } else if (event.indicesTarget != null) {
                            dataMapTableViewer.refresh();
                            dataMapTableView.changeSize(view.getPreferredSize(false, true, false), true, true);
                            int[] selection = ArrayUtils.toPrimitive((Integer[]) event.indicesTarget.toArray(new Integer[0]));
                            dataMapTVCreator.getSelectionHelper().setSelection(selection);
                        }
                        mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
                    }

                    if (event.type == TYPE.REMOVED) {
                        // metadataEditorTableViewer.refresh();
                        List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.removedObjects;
                        for (IMetadataColumn metadataColumn : metadataColumns) {
                            removeColumn(metadataColumn, abstractDataMapTable);
                        }
                        dataMapTableViewer.refresh();
                        dataMapTableView.resizeAtExpandedSize();
                        resizeTablesZoneViewAtComputedSize(dataMapTableView.getZone());
                        moveScrollBarZoneAtSelectedTable(dataMapTableView.getZone());
                        refreshBackground(true, false);
                        mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
                    }

                    if (event.type == TYPE.SWAPED) {
                        List<Integer> listIndexTarget = event.indicesTarget;
                        abstractDataMapTable.swapColumnElements(event.indicesOrigin, listIndexTarget);
                        // dataMapTableViewer.refresh();
                        refreshBackground(true, false);
                    }

                }

            });
            metadataTableEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(false);
            metadataTableEditorView.setMetadataTableEditor(metadataTableEditor);
            metadataTableEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(true);
            metadataTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();

            dataMapTVCreator.getSelectionHelper().setActiveFireSelectionChanged(false);
            metadataTableEditorView.getTableViewerCreator().getSelectionHelper()
                    .setSelection(dataMapTableViewer.getTable().getSelectionIndices());
            dataMapTVCreator.getSelectionHelper().setActiveFireSelectionChanged(true);

            // disable highlight for other DataMapTableView and highlight selected DataMapTableView
            for (IDataMapTable table : tables) {
                DataMapTableView otherDataMapTableView = mapperManager.retrieveIDataMapTableView(table);
                otherDataMapTableView.setBackground(dataMapTableView.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
            }
            dataMapTableView.setBackground(dataMapTableView.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
        }

        if (selectAllEntries) {
            dataMapTVCreator.getTable().selectAll();
            selectAllLinks(dataMapTableView);
            metadataTableEditorView.getTableViewerCreator().getTable().selectAll();
            metadataTableEditorView.getToolBar().updateEnabledStateOfButtons();
            if (currentZone == Zone.OUTPUTS) {
                dataMapTableView.getTableViewerCreatorForWhereFilters().getTable().selectAll();
                dataMapTableView.getTableViewerCreatorForOtherFilters().getTable().selectAll();
            }
        }

        if (otherMetadataTableEditorView != null) {
            otherMetadataTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
        }

    }
    
    private void removeColumn(IMetadataColumn metadataColumn, AbstractInOutTable abstractDataMapTable) {
        if(abstractDataMapTable instanceof InputTable) {
            for(InputTable inTable : mapperManager.getInputTables()) {
                if(inTable.getTableName().equals(((InputTable)abstractDataMapTable).getTableName())) {
                    ITableEntry metadataTableEntry = mapperManager.retrieveTableEntry(new TableEntryLocation(
                            inTable.getName(), metadataColumn.getLabel()));
                    mapperManager.removeTableEntry(metadataTableEntry);
                }
            }
        }else {
            ITableEntry metadataTableEntry = mapperManager.retrieveTableEntry(new TableEntryLocation(
                    abstractDataMapTable.getName(), metadataColumn.getLabel()));
            mapperManager.removeTableEntry(metadataTableEntry);
        }
        
    }
    
    private void addColumn(IMetadataColumn metadataColumn, DataMapTableView dataMapTableView, int index) {
        IDataMapTable abstractDataMapTable = dataMapTableView.getDataMapTable();
        IColumnEntry dataMapTableEntry = null;
        if (dataMapTableView.getZone() == Zone.INPUTS && abstractDataMapTable instanceof InputTable) {
            for(InputTable inTable : mapperManager.getInputTables()) {
                if(inTable.getTableName().equals(((InputTable)abstractDataMapTable).getTableName())) {
                    dataMapTableEntry = new InputColumnTableEntry(inTable, metadataColumn);
                    mapperManager.getTableEntriesManager().addTableEntry(dataMapTableEntry, index);
                    lastCreatedInOutColumnEntries.add(dataMapTableEntry);
                }
            }
        }else {
            lastCreatedInOutColumnEntries.add(mapperManager.addNewColumnEntry(dataMapTableView,
                    metadataColumn, index++));
        }
    }

    /**
     * DOC amaumont Comment method "updateToolbarButtonsStates".
     *
     * @param currentZone
     */
    public void updateToolbarButtonsStates(Zone currentZone) {
        ToolbarZone toolbar = null;
        if (currentZone == Zone.INPUTS) {
            toolbar = getInputsZone().getToolbar();
            ((ToolbarInputZone) toolbar).setEnabledRenameAliasButton(currentSelectedInputTableView != null);
            ((ToolbarInputZone) toolbar).setEnabledRemoveAliasButton(currentSelectedInputTableView != null);
            toolbar.setEnabledMinimizeTablesButton(getInputsTablesView().size() > 0);
        } else if (currentZone == Zone.OUTPUTS) {
            toolbar = getOutputsZone().getToolbar();
            ((ToolbarOutputZone) toolbar).setEnabledRemoveTableButton(currentSelectedOutputTableView != null);
            toolbar.setEnabledMinimizeTablesButton(getOutputsTablesView().size() > 0);
        }
        toolbar.setEnabledMoveTableButton(true, isTableViewMoveable(currentZone, true));
        toolbar.setEnabledMoveTableButton(false, isTableViewMoveable(currentZone, false));
    }

    private void modifySelectionChangedListener(final Zone currentZone,
            final MetadataTableEditorView metadataTableEditorViewFinal, final TableViewerCreator metadataTVCreator,
            final MetadataTableEditor metadataTableEditor, final DataMapTableView dataMapTableView,
            DataMapTableView previousSelectedTableView) {

        final TableViewer tableViewer = dataMapTableView.getTableViewerCreatorForColumns().getTableViewer();

        IModifiedBeanListener<IMetadataColumn> modifiedBeanListener = new IModifiedBeanListener<IMetadataColumn>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<IMetadataColumn> event) {
                if (AbstractMetadataTableEditorView.ID_COLUMN_NAME.equals(event.column.getId())
                        && !event.previousValue.equals(event.newValue)) {
                    IMetadataColumn modifiedObject = event.bean;
                    if (modifiedObject != null) {
                        TableEntryLocation tableEntryLocation = new TableEntryLocation(dataMapTableView.getDataMapTable()
                                .getName(), (String) event.previousValue);
                        final ITableEntry dataMapTableEntry = mapperManager.retrieveTableEntry(tableEntryLocation);
                        processColumnNameChanged((String) event.previousValue, (String) event.newValue, dataMapTableView,
                                dataMapTableEntry, false);
                    }
                    // dataMapTableViewer.refresh(event.bean, true);
                    tableViewer.refresh(true);
                    mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
                } else if (AbstractMetadataTableEditorView.ID_COLUMN_KEY.equals(event.column.getId())) {
                    tableViewer.refresh(true);
                    IColumnEntry entry = dataMapTableView.getDataMapTable().getColumnEntries().get(event.index);
                    parseExpression(entry.getExpression(), entry, false, false, false);
                } else if (AbstractMetadataTableEditorView.ID_COLUMN_TYPE.equals(event.column.getId())
                        || AbstractMetadataTableEditorView.ID_COLUMN_NULLABLE.equals(event.column.getId())) {
                    mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
                }
            }

        };

        ILineSelectionListener metadataEditorViewerSelectionChangedListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                // System.out.println("LineSelectionEvent");

                if (metadataTableEditorViewFinal.getTableViewerCreator() == e.source) {
                    if (metadataTableEditorViewFinal.getExtendedTableViewer().isExecuteSelectionEvent()) {
                        mapperManager.getUiManager().selectLinkedTableEntries(metadataTableEditor.getMetadataTable(),
                                metadataTVCreator.getTable().getSelectionIndices());
                    }
                } else {
                    // if (dataMapTableView.getExtendedTableViewerForColumns().isExecuteSelectionEvent()) {
                    // int[] selectionIndices =
                    // dataMapTableView.getTableViewerCreatorForColumns().getTable().getSelectionIndices();
                    // mapperManager.getUiManager().selectLinkedMetadataEditorEntries(dataMapTableView,
                    // selectionIndices);
                    // }

                }
            }

        };

        // ISelectionChangedListener previousSelectionChangedListener = null;
        ILineSelectionListener previousSelectionChangedListener = null;
        IModifiedBeanListener<IMetadataColumn> previousModifiedBeanListener = null;
        if (currentZone == Zone.INPUTS) {
            previousSelectionChangedListener = inputsSelectionChangedListener;
            previousModifiedBeanListener = inputModifiedBeanListener;
        } else if (currentZone == Zone.OUTPUTS) {
            previousSelectionChangedListener = outputsSelectionChangedListener;
            previousModifiedBeanListener = outputModifiedBeanListener;
        }
        if (previousSelectionChangedListener != null) {
            // metadataTVCreator.removeSelectionChangedListener(previousSelectionChangedListener);
            metadataTVCreator.getSelectionHelper().removeAfterSelectionListener(previousSelectionChangedListener);
            if (previousSelectedTableView != null) {
                previousSelectedTableView.getTableViewerCreatorForColumns().getSelectionHelper()
                        .removeAfterSelectionListener(previousSelectionChangedListener);
            }
        }

        if (previousModifiedBeanListener != null) {
            metadataTableEditor.removeModifiedBeanListener(previousModifiedBeanListener);
        }

        if (currentZone == Zone.INPUTS) {
            inputsSelectionChangedListener = metadataEditorViewerSelectionChangedListener;
            inputModifiedBeanListener = modifiedBeanListener;
        } else if (currentZone == Zone.OUTPUTS) {
            outputsSelectionChangedListener = metadataEditorViewerSelectionChangedListener;
            outputModifiedBeanListener = modifiedBeanListener;
        }
        // metadataTVCreator.addSelectionChangedListener(metadataEditorViewerSelectionChangedListener);
        metadataTVCreator.getSelectionHelper().addAfterSelectionListener(metadataEditorViewerSelectionChangedListener);
        dataMapTableView.getTableViewerCreatorForColumns().getSelectionHelper()
                .addAfterSelectionListener(metadataEditorViewerSelectionChangedListener);
        metadataTableEditor.addModifiedBeanListener(modifiedBeanListener);

        if (this.commonMetadataDisposeListener == null) {
            this.commonMetadataDisposeListener = new DisposeListener() {

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    if (inputsSelectionChangedListener != null) {
                        getMetadataEditorView(Zone.INPUTS).getTableViewerCreator().getSelectionHelper()
                                .removeAfterSelectionListener(inputsSelectionChangedListener);
                    }
                    if (outputsSelectionChangedListener != null) {
                        getMetadataEditorView(Zone.OUTPUTS).getTableViewerCreator().getSelectionHelper()
                                .removeAfterSelectionListener(outputsSelectionChangedListener);
                    }
                    if (inputModifiedBeanListener != null) {
                        MetadataTableEditor metadataTableEditor = getMetadataEditorView(Zone.INPUTS).getMetadataTableEditor();
                        if (metadataTableEditor != null) {
                            metadataTableEditor.removeModifiedBeanListener(inputModifiedBeanListener);
                        }
                    }
                    if (outputModifiedBeanListener != null) {
                        MetadataTableEditor metadataTableEditor = getMetadataEditorView(Zone.OUTPUTS).getMetadataTableEditor();
                        if (metadataTableEditor != null) {
                            metadataTableEditor.removeModifiedBeanListener(outputModifiedBeanListener);
                        }
                    }
                }
            };
            metadataTVCreator.getTable().addDisposeListener(this.commonMetadataDisposeListener);
        }

    }

    public void setMapperUI(MapperUI mapperUI) {
        this.mapperUI = mapperUI;
    }

    /**
     * DOC amaumont Comment method "refreshBackground".
     *
     * @param firstExecutionAfterTime
     */
    @Override
    public void refreshBackground(boolean forceRecalculate, boolean firstExecutionAfterTime) {
        if (forceRecalculate) {
            mapperUI.getBackgroundRefreshLimiterForceRecalculate().startIfExecutable(firstExecutionAfterTime, null);
        } else {
            mapperUI.getBackgroundRefreshLimiter().startIfExecutable(firstExecutionAfterTime, null);
        }
    }

    /**
     * DOC amaumont Comment method "refreshBackground".
     */
    public void refreshBackground(boolean forceRecalculate, int timeBeforeNewRefresh, boolean executeAfterTime) {
        if (forceRecalculate) {
            mapperUI.getBackgroundRefreshLimiterForceRecalculate().setTimeBeforeNewExecution(timeBeforeNewRefresh);
        } else {
            mapperUI.getBackgroundRefreshLimiter().setTimeBeforeNewExecution(timeBeforeNewRefresh);
        }
        refreshBackground(forceRecalculate, executeAfterTime);
    }

    public Composite getReferenceComposite() {
        if (this.refComposite == null) {
            this.refComposite = mapperUI.getVisualMapReferenceComposite();
        }
        return this.refComposite;
    }

    public int getVerticalScrolledOffsetBar(Zone zone) {
        switch (zone) {
        case INPUTS:
            return -mapperUI.getScollbarSelectionZoneInputs();

        case OUTPUTS:
            return -mapperUI.getScollbarSelectionZoneOutputs();

        default:
            throw new RuntimeException(Messages.getString("UIManager.2") + zone + Messages.getString("UIManager.3")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public ScrollBar getVerticalScrollBar(Zone zone) {
        switch (zone) {
        case INPUTS:
            return mapperUI.getScollbarZoneInputs();
        case OUTPUTS:
            return mapperUI.getScollbarZoneOutputs();

        default:
            throw new RuntimeException("The zone " + zone + " does'nt exist !"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * DOC amaumont Comment method "setDialogResponse".
     *
     * @param ok
     */
    public void setMapperResponse(int mapperResponse) {
        this.mapperResponse = mapperResponse;
    }

    public int getMapperResponse() {
        return this.mapperResponse;
    }

    /**
     * DOC amaumont Comment method "closeMapperDialog".
     *
     * @param ok
     */
    public void closeMapper(int response) {
        setMapperResponse(response);
        Composite parent = mapperUI.getMapperUIParent();
        saveCurrentUIProperties();

        if (response == SWT.OK) {
            createVisualMapImage();
        }

        mapperManager.updateEmfParameters(EParameterName.PREVIEW.getName());

        if (parent instanceof Shell) {
            ((Shell) parent).close();
        }
    }

    /**
     * DOC amaumont Comment method "createVisualMapImage".
     */
    private void createVisualMapImage() {
        String previewFilePath = mapperManager.getPreviewFilePath();
        if (previewFilePath != null) {
            Image image = ImageCapture.capture(mapperUI.getDatasFlowViewSashForm());
            image = ImageUtils.scale(image, 50);
            ImageUtils.save(image, previewFilePath, SWT.IMAGE_BMP);
        }
    }

    /**
     * DOC amaumont Comment method "saveUiProperties".
     */
    private void saveCurrentUIProperties() {
        uiProperties = new ExternalDbMapUiProperties();
        uiProperties.setWeightsMainSashForm(mapperUI.getMainSashForm().getWeights());
        uiProperties.setWeightsDatasFlowViewSashForm(mapperUI.getDatasFlowViewSashForm().getWeights());
        uiProperties.setShellMaximized(getMapperContainer().getShell().getMaximized());
        if (!uiProperties.isShellMaximized()) {
            uiProperties.setBoundsMapper(getMapperContainer().getBounds());
        }
    }

    /**
     * DOC amaumont Comment method "selectTableEntries".
     *
     * @param metadataTable
     * @param selectedTableEntries
     * @param selectionIndices
     */
    public void selectLinkedTableEntries(IMetadataTable metadataTable, int[] selectionIndices) {
        DataMapTableView dataMapTableView = tableManager.getView(metadataTable);

        if (dataMapTableView == null) {
            return;
        }

        dataMapTableView.setTableSelection(selectionIndices);

        List<ITableEntry> list = extractSelectedTableEntries(dataMapTableView.getTableViewerCreatorForColumns().getTableViewer()
                .getSelection());
        selectLinks(dataMapTableView, list, false, false);
    }

    /**
     * DOC amaumont Comment method "selectTableEntries".
     *
     * @param view
     * @param selectedTableEntries
     * @param selectionIndices
     */
    public void selectLinkedMetadataEditorEntries(DataMapTableView view, int[] selectionIndices) {
        MetadataTableEditorView metadataTableEditorView = null;
        MetadataTableEditorView otherMetadataTableEditorView = null;
        if (view.getZone() == Zone.INPUTS) {
            metadataTableEditorView = getInputMetaEditorView();
            otherMetadataTableEditorView = getOutputMetaEditorView();
        } else if (view.getZone() == Zone.OUTPUTS) {
            metadataTableEditorView = getOutputMetaEditorView();
            otherMetadataTableEditorView = getInputMetaEditorView();
        }
        if (metadataTableEditorView != null) {
            metadataTableEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(false);
            metadataTableEditorView.getExtendedTableViewer().getTableViewerCreator().getSelectionHelper()
                    .setSelection(selectionIndices);
            metadataTableEditorView.getTableViewerCreator().getSelectionHelper().setActiveFireSelectionChanged(true);
            metadataTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();

        }
        if (otherMetadataTableEditorView != null) {
            otherMetadataTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
        }
    }

    public void selectAllLinks(DataMapTableView dataMapTableView) {
        selectLinks(dataMapTableView, null, false, false);
    }

    /**
     * Highlight links and linked cells which have are referenced by the selected items.
     *
     * @param dataMapTableView
     * @param selectedMetadataTableEntries, source or targets entries which must be highlighted, can be null to select
     * all links of a same DataMapTableView
     * @param isFilterTableSelected TODO
     * @param forceResetHighlightLinksForOtherTables TODO
     */
    @SuppressWarnings("unchecked")
    public void selectLinks(DataMapTableView dataMapTableView, List<ITableEntry> selectedMetadataTableEntries,
            boolean isFilterTableSelected, boolean forceResetHighlightLinksForOtherTables) {

        boolean selectColumnAndFiltersLinks = (selectedMetadataTableEntries == null);

        UIManager uiManager = mapperManager.getUiManager();
        TableViewerCreator<ITableEntry> currentTableViewer = null;
        TableViewerCreator<ITableEntry> whereFilterCreator = null;
        TableViewerCreator<ITableEntry> otherFilterCreator = null;

        if (isFilterTableSelected) {
            currentTableViewer = dataMapTableView.getTableViewerCreatorForWhereFilters();
        } else {
            currentTableViewer = dataMapTableView.getTableViewerCreatorForColumns();
        }

        // Color selectedColor = dataMapTableView.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
        Color unselectedColor = dataMapTableView.getDisplay().getSystemColor(SWT.COLOR_WHITE);

        Zone currentZone = dataMapTableView.getZone();

        Set<ITableEntry> hashSelectedMetadataTableEntries = new HashSet<ITableEntry>();
        if (selectColumnAndFiltersLinks) {
            hashSelectedMetadataTableEntries.addAll(dataMapTableView.getTableViewerCreatorForColumns().getInputList());
            if (currentZone == Zone.OUTPUTS) {
                hashSelectedMetadataTableEntries.addAll(dataMapTableView.getTableViewerCreatorForWhereFilters().getInputList());
                hashSelectedMetadataTableEntries.addAll(dataMapTableView.getTableViewerCreatorForOtherFilters().getInputList());
            }
        } else {
            hashSelectedMetadataTableEntries.addAll(selectedMetadataTableEntries);
        }

        // ////////////////////////////////////////////////////////////////////////
        // Unselect all links and tableEntries if Ctrl or Shift keys are not pressed or if zone different of last
        // selection
        boolean zoneHasChanged = previousSelectedZone == Zone.INPUTS && currentZone == Zone.OUTPUTS || currentZone == Zone.INPUTS
                && previousSelectedZone == Zone.OUTPUTS;
        boolean tableTypeHasChanged = previousSelectedTableIsConstraint != isFilterTableSelected && currentZone == Zone.OUTPUTS;
        boolean resetHighlightObjectsForOtherTables = !uiManager.isDragging()
                && (!uiManager.isCtrlPressed() && !uiManager.isShiftPressed() || zoneHasChanged);
        if (resetHighlightObjectsForOtherTables || forceResetHighlightLinksForOtherTables) {
            for (IMapperLink link : mapperManager.getLinks()) {
                if (!hashSelectedMetadataTableEntries.contains(link.getPointLinkDescriptor1().getTableEntry())
                        && !hashSelectedMetadataTableEntries.contains(link.getPointLinkDescriptor2().getTableEntry())) {
                    link.setState(LinkState.UNSELECTED);
                    ITableEntry sourceITableEntry = link.getPointLinkDescriptor1().getTableEntry();
                    TableItem tableItem = mapperManager.retrieveTableItem(sourceITableEntry);
                    tableItem.setBackground(unselectedColor);
                    ITableEntry targetITableEntry = link.getPointLinkDescriptor2().getTableEntry();
                    tableItem = mapperManager.retrieveTableItem(targetITableEntry);
                    tableItem.setBackground(unselectedColor);
                }
            }

            if (currentZone == Zone.INPUTS) {
                unselectAllOutputMetaDataEntries();
            } else if (currentZone == Zone.OUTPUTS) {
                unselectAllInputMetaDataEntries();
            }

            Collection<DataMapTableView> tablesToDeselectEntries = mapperManager.getTablesView();
            for (DataMapTableView viewToDeselectEntries : tablesToDeselectEntries) {
                if (viewToDeselectEntries != dataMapTableView) {
                    viewToDeselectEntries.unselectAllEntries();
                } else if (viewToDeselectEntries == dataMapTableView && tableTypeHasChanged) {
                    if (isFilterTableSelected) {
                        viewToDeselectEntries.unselectAllColumnEntries();
                    } else {
                        viewToDeselectEntries.unselectAllConstraintEntries();
                    }
                }
            }
        }
        // ////////////////////////////////////////////////////////////////////////

        // ////////////////////////////////////////////////////////////////////////
        // Select or unselect links and tableEntries
        List<ITableEntry> allEntriesOfCurrentTableView = new ArrayList<ITableEntry>();
        if (currentTableViewer != null) {
            allEntriesOfCurrentTableView.addAll(currentTableViewer.getInputList());
            if (selectColumnAndFiltersLinks && currentZone == Zone.OUTPUTS) {
                allEntriesOfCurrentTableView.addAll(dataMapTableView.getTableViewerCreatorForWhereFilters().getInputList());
                allEntriesOfCurrentTableView.addAll(dataMapTableView.getTableViewerCreatorForOtherFilters().getInputList());
            }
        }
        int lstSize = allEntriesOfCurrentTableView.size();
        Set<IMapperLink> linksAlreadySelected = new HashSet<IMapperLink>();
        for (int i = 0; i < lstSize; i++) {
            ITableEntry entry = allEntriesOfCurrentTableView.get(i);
            Set<IMapperLink> linksFromSource = mapperManager.getGraphicalLinksFromSource(entry);
            Set<IMapperLink> linksFromTarget = mapperManager.getGraphicalLinksFromTarget(entry);
            LinkState linkState = null;
            if (hashSelectedMetadataTableEntries.contains(entry)) {
                linkState = LinkState.SELECTED;
            } else {
                linkState = LinkState.UNSELECTED;
            }
            for (IMapperLink link : linksFromSource) {
                ITableEntry targetITableEntry = link.getPointLinkDescriptor2().getTableEntry();
                if (linkState == LinkState.SELECTED || !linksAlreadySelected.contains(link) && linkState == LinkState.UNSELECTED) {
                    link.setState(linkState);
                    if (linkState == LinkState.SELECTED) {
                        linksAlreadySelected.add(link);
                    }
                }
                EntryState entryState = (link.getState() == LinkState.SELECTED ? EntryState.HIGHLIGHT : EntryState.NONE);
                setEntryState(mapperManager, entryState, targetITableEntry);
            }
            for (IMapperLink link : linksFromTarget) {
                ITableEntry sourceITableEntry = link.getPointLinkDescriptor1().getTableEntry();
                if (linkState == LinkState.SELECTED || !linksAlreadySelected.contains(link) && linkState == LinkState.UNSELECTED) {
                    link.setState(linkState);
                    if (linkState == LinkState.SELECTED) {
                        linksAlreadySelected.add(link);
                    }
                }
                EntryState entryState = (link.getState() == LinkState.SELECTED ? EntryState.HIGHLIGHT : EntryState.NONE);
                setEntryState(mapperManager, entryState, sourceITableEntry);
            }
        }
        // ////////////////////////////////////////////////////////////////////////

        // order links to place selected links at last position (last drawn)
        mapperManager.orderLinks();

        uiManager.refreshBackground(false, false);

        previousSelectedZone = dataMapTableView.getZone();
        previousSelectedTableIsConstraint = isFilterTableSelected;
    }

    public void unselectAllInputMetaDataEntries() {
        getInputMetaEditorView().getTable().deselectAll();
    }

    public void unselectAllOutputMetaDataEntries() {
        getOutputMetaEditorView().getTable().deselectAll();
    }

    public void setEntryState(MapperManager pMapperManager, EntryState entryState, ITableEntry entry) {
        TableItem tableItem = pMapperManager.retrieveTableItem(entry);
        tableItem.setBackground(entryState.getColor());
    }

    /**
     * DOC amaumont Comment method "extractTableEntries".
     *
     * @param selection
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ITableEntry> extractSelectedTableEntries(ISelection selection) {
        StructuredSelection currentSelection = (StructuredSelection) selection;
        return currentSelection.toList();
    }

    /**
     * DOC amaumont Comment method "getTableEntryPosition".
     *
     * @param manager TODO
     * @param tableEntry
     * @param forceRecalculate TODO
     * @return
     */
    public Point getTableEntryPosition(ITableEntry tableEntry, boolean forceRecalculate) {
        TableEntryProperties tableEntryProperties = mapperManager.getTableEntryProperties(tableEntry);
        Point returnedPoint = tableEntryProperties.position;
        if (forceRecalculate || returnedPoint == null) {
            TableItem tableItem = mapperManager.retrieveTableItem(tableEntry);
            DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(tableEntry);
            Rectangle tableViewBounds = dataMapTableView.getBounds();
            Table table = tableItem.getParent();
            Rectangle boundsTableItem = tableItem.getBounds(1);// FIX for issue 1225 ("1" parameter added)

            int x = 0;
            int y = boundsTableItem.y + table.getItemHeight() / 2 + dataMapTableView.getBorderWidth();
            if (y < 0) {
                y = 0;
            }

            Point point = new Point(x, y);

            Display display = dataMapTableView.getDisplay();
            Point pointFromTableViewOrigin = display.map(tableItem.getParent(), dataMapTableView, point);

            if (pointFromTableViewOrigin.y > tableViewBounds.height - TableEntriesManager.HEIGHT_REACTION) {
                pointFromTableViewOrigin.y = tableViewBounds.height - TableEntriesManager.HEIGHT_REACTION;
            }

            returnedPoint = convertPointToReferenceOrigin(getReferenceComposite(), pointFromTableViewOrigin, dataMapTableView);
            tableEntryProperties.position = returnedPoint;
        }
        return returnedPoint;
    }

    public Point convertPointToReferenceOrigin(final Composite referenceComposite, Point point, Composite child) {
        Point returnedPoint = new Point(point.x, point.y);
        while (child != referenceComposite) {
            Rectangle bounds = child.getBounds();
            child = child.getParent();
            ScrollBar vScrollBar = child.getVerticalBar();
            if (vScrollBar != null) {
                returnedPoint.y += vScrollBar.getSelection();
            }
            returnedPoint.x += bounds.x;
            returnedPoint.y += bounds.y;
        }
        return returnedPoint;
    }

    public MetadataTableEditorView getInputMetaEditorView() {
        return mapperUI.getTabFolderEditors().getInputMetaEditor();
    }

    public MetadataTableEditorView getOutputMetaEditorView() {
        return mapperUI.getTabFolderEditors().getOutputMetaEditor();
    }

    /**
     * DOC amaumont Comment method "processNewExpression".
     *
     * @param appliedOrCanceled TODO
     * @param text
     * @param newSelectedDataMapTableView
     * @param currentModifiedObject
     */
    public void parseNewExpression(String expression, ITableEntry currentModifiedITableEntry, boolean appliedOrCanceled) {
        ParseExpressionResult result = parseExpression(expression, currentModifiedITableEntry, true, true, appliedOrCanceled);
        if (result.isAtLeastOneLinkHasBeenAddedOrRemoved()) {
            mapperManager.getUiManager().refreshBackground(false, false);
        }

    }

    /**
     * DOC amaumont Comment method "processAllExpressions".
     */
    @SuppressWarnings("unchecked")
    public void parseAllExpressionsForAllTables() {
        List<DataMapTableView> tablesView = tableManager.getInputsTablesView();
        tablesView.addAll(tableManager.getVarsTablesView());
        tablesView.addAll(tableManager.getOutputsTablesView());
        for (DataMapTableView view : tablesView) {
            parseAllExpressions(view, false);
        }
    }

    /**
     * DOC amaumont Comment method "processAllExpressions".
     *
     * @param newLinksMustHaveSelectedState TODO
     */
    @SuppressWarnings("unchecked")
    public void parseAllExpressions(DataMapTableView dataMapTableView, boolean newLinksMustHaveSelectedState) {
        List<IColumnEntry> columnsEntriesList = dataMapTableView.getDataMapTable().getColumnEntries();
        parseAllExpressions(columnsEntriesList, newLinksMustHaveSelectedState);
        if (dataMapTableView.getZone() == Zone.OUTPUTS) {
            List<ITableEntry> constraintEntriesList = dataMapTableView.getTableViewerCreatorForWhereFilters().getInputList();
            parseAllExpressions(constraintEntriesList, newLinksMustHaveSelectedState);
            constraintEntriesList = dataMapTableView.getTableViewerCreatorForOtherFilters().getInputList();
            parseAllExpressions(constraintEntriesList, newLinksMustHaveSelectedState);
        }
    }

    private void parseAllExpressions(List<? extends ITableEntry> entriesList, boolean newLinksMustHaveSelectedState) {
        for (ITableEntry entry : entriesList) {
            parseExpression(entry.getExpression(), entry, newLinksMustHaveSelectedState, false, false);
        }
    }

    /**
     *
     *
     * @param expression
     * @param currentModifiedITableEntry
     * @param linkMustHaveSelectedState
     * @param checkInputKeyAutomatically TODO
     * @param inputExpressionAppliedOrCanceled TODO
     * @param newSelectedDataMapTableView
     * @return true if a link has been added or removed, false else
     */
    public ParseExpressionResult parseExpression(String expression, ITableEntry currentModifiedITableEntry,
            boolean linkMustHaveSelectedState, boolean checkInputKeyAutomatically, boolean inputExpressionAppliedOrCanceled) {
        return parseExpression(expression, currentModifiedITableEntry, linkMustHaveSelectedState, checkInputKeyAutomatically,
                inputExpressionAppliedOrCanceled, false);
    }

    public ParseExpressionResult parseExpression(String expression, ITableEntry currentModifiedITableEntry,
            boolean linkMustHaveSelectedState, boolean checkInputKeyAutomatically, boolean inputExpressionAppliedOrCanceled,
            boolean renamed) {

        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(currentModifiedITableEntry);
        boolean linkHasBeenAdded = false;
        boolean linkHasBeenRemoved = false;

        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(mapperManager.getCurrentLanguage());
        TableEntryLocation[] tableEntriesLocationsSources = dataMapExpressionParser.parseTableEntryLocations(expression);
        Set<TableEntryLocation> alreadyProcessed = new HashSet<TableEntryLocation>();
        Set<ITableEntry> sourcesForTarget = mapperManager.getSourcesForTarget(currentModifiedITableEntry);
        Set<ITableEntry> sourcesForTargetToDelete = new HashSet<ITableEntry>(sourcesForTarget);

        boolean isInputEntry = currentModifiedITableEntry instanceof InputColumnTableEntry;

        for (TableEntryLocation tableEntriesLocationsSource : tableEntriesLocationsSources) {
            TableEntryLocation location = tableEntriesLocationsSource;

            if (!alreadyProcessed.contains(location) && checkSourceLocationIsValid(location, currentModifiedITableEntry)) {
                ITableEntry sourceTableEntry = mapperManager.retrieveTableEntry(location);
                sourcesForTargetToDelete.remove(sourceTableEntry);
                if (sourceTableEntry != null && !sourcesForTarget.contains(sourceTableEntry)

                ) {
                    DataMapTableView sourceDataMapTableView = mapperManager.retrieveDataMapTableView(sourceTableEntry);
                    IMapperLink link = new Link(new PointLinkDescriptor(sourceTableEntry, sourceDataMapTableView.getZone()),
                            new PointLinkDescriptor(currentModifiedITableEntry, dataMapTableView.getZone()), mapperManager);
                    link.setState(linkMustHaveSelectedState ? LinkState.SELECTED : LinkState.UNSELECTED);
                    mapperManager.addLink(link);
                    linkHasBeenAdded = true;
                }
                alreadyProcessed.add(location);
            }
        }
        if (!renamed) {
            Set<IMapperLink> targets = mapperManager.getGraphicalLinksFromTarget(currentModifiedITableEntry);
            Set<IMapperLink> linksFromTarget = new HashSet<IMapperLink>(targets);
            for (IMapperLink link : linksFromTarget) {
                if (sourcesForTargetToDelete.contains(link.getPointLinkDescriptor1().getTableEntry())) {
                    mapperManager.removeLink(link, link.getPointLinkDescriptor2().getTableEntry());
                    linkHasBeenRemoved = true;
                }
            }
        }
        mapperManager.orderLinks();

        return new ParseExpressionResult(linkHasBeenAdded, linkHasBeenRemoved);
    }

    /**
     * DOC amaumont Comment method "refreshInOutTableAndMetaTable".
     *
     * @param currentModifiedTableEntry can be null
     */
    private void refreshInOutTableAndMetaTable(AbstractInOutTableEntry currentModifiedTableEntry) {
        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(currentModifiedTableEntry);
        IMetadataTable metadataTableTarget = ((AbstractInOutTable) dataMapTableView.getDataMapTable()).getMetadataTable();
        dataMapTableView.getTableViewerCreatorForColumns().getTableViewer().refresh(currentModifiedTableEntry);
        MetadataTableEditorView metadataEditorView = getMetadataEditorView(dataMapTableView.getZone());
        if (metadataEditorView != null && metadataEditorView.getMetadataTableEditor() != null
                && metadataEditorView.getMetadataTableEditor().getMetadataTable() == metadataTableTarget) {
            metadataEditorView.getTableViewerCreator().getTableViewer().refresh(currentModifiedTableEntry.getMetadataColumn());
            metadataEditorView.getTableViewerCreator().refreshTableEditorControls();
        }
    }

    /**
     *
     * DOC amaumont Comment method "refreshInOutTableAndMetaTable".
     *
     * @param dataMapTableView
     */
    private void refreshInOutTableAndMetaTable(DataMapTableView dataMapTableView) {
        IMetadataTable metadataTableTarget = ((AbstractInOutTable) dataMapTableView.getDataMapTable()).getMetadataTable();
        dataMapTableView.getTableViewerCreatorForColumns().getTableViewer().refresh();
        MetadataTableEditorView metadataEditorView = getMetadataEditorView(dataMapTableView.getZone());
        if (metadataEditorView != null && metadataEditorView.getMetadataTableEditor() != null
                && metadataEditorView.getMetadataTableEditor().getMetadataTable() == metadataTableTarget) {
            metadataEditorView.getTableViewerCreator().getTableViewer().refresh();
            metadataEditorView.getTableViewerCreator().refreshTableEditorControls();
        }
    }

    /**
     * DOC amaumont Comment method "processNewProcessColumnName".
     *
     * @param previousColumnName TODO
     * @param dataMapTableView
     * @param renamingDependentEntries TODO
     * @param text
     * @param entry
     */
    public void processColumnNameChanged(final String previousColumnName, final String newColumnName,
            final DataMapTableView dataMapTableView, final ITableEntry currentModifiedITableEntry,
            boolean renamingDependentEntries) {

        mapperManager.changeColumnName(currentModifiedITableEntry, previousColumnName, newColumnName);
        Collection<DataMapTableView> tableViews = mapperManager.getTablesView();
        boolean atLeastOneLinkHasBeenRemoved = false;
        for (DataMapTableView view : tableViews) {
            IDataMapTable dataMapTable = view.getDataMapTable();
            List<IColumnEntry> metadataTableEntries = dataMapTable.getColumnEntries();
            for (IColumnEntry entry : metadataTableEntries) {
                if (parseExpression(entry.getExpression(), entry, true, true, false).isAtLeastOneLinkRemoved()) {
                    atLeastOneLinkHasBeenRemoved = true;
                }
            }
            if (dataMapTable instanceof OutputTable) {
                List<FilterTableEntry> constraintWhereEntries = ((OutputTable) dataMapTable).getWhereFilterEntries();
                for (FilterTableEntry entry : constraintWhereEntries) {
                    if (parseExpression(entry.getExpression(), entry, true, true, false).isAtLeastOneLinkRemoved()) {
                        atLeastOneLinkHasBeenRemoved = true;
                    }
                }
                List<FilterTableEntry> constraintOtherEntries = ((OutputTable) dataMapTable).getOtherFilterEntries();
                for (FilterTableEntry entry : constraintOtherEntries) {
                    if (parseExpression(entry.getExpression(), entry, true, true, false).isAtLeastOneLinkRemoved()) {
                        atLeastOneLinkHasBeenRemoved = true;
                    }
                }
            }
        }
        mapperManager.getUiManager().refreshBackground(false, false);
        dataMapTableView.getTableViewerCreatorForColumns().getTableViewer().refresh(currentModifiedITableEntry);

        TableEntryLocation previousLocation = new TableEntryLocation(currentModifiedITableEntry.getParentName(),
                previousColumnName);
        TableEntryLocation newLocation = new TableEntryLocation(currentModifiedITableEntry.getParentName(), newColumnName);
        mapperManager.replacePreviousLocationInAllExpressions(previousLocation, newLocation, false);
        mapperManager.getUiManager().refreshBackground(false, false);
        refreshSqlExpression();

        if (!renamingDependentEntries) {

            AbstractInOutTable currentTable = (AbstractInOutTable) currentModifiedITableEntry.getParent();
            if (currentTable instanceof InputTable) {

                InputTable currentInputTable = (InputTable) currentTable;
                String physicalTableName = currentInputTable.getTableName();

                String alias = currentInputTable.getAlias();

                InputTable physicalInputTable = null;
                List<InputTable> inputTables = mapperManager.getInputTables();

                if (alias != null) {

                    for (InputTable table : inputTables) {
                        if (table.equals(physicalTableName)) {
                            physicalInputTable = table;
                        }
                    }

                } else {
                    physicalInputTable = currentInputTable;
                }

                if (physicalInputTable == null) {

                    List<IOConnection> incomingConnections = mapperManager.getComponent().getMapperMain().getIoInputConnections();
                    IOConnection connectionFound = null;
                    for (IOConnection connection : incomingConnections) {
                        if (connection.getName().equals(physicalTableName)) {
                            connectionFound = connection;
                        }
                    }
                    IMetadataColumn metadataColumn = connectionFound.getTable().getColumn(previousColumnName);
                    if (metadataColumn != null) {
                        metadataColumn.setLabel(newColumnName);
                    }
                }

                for (InputTable table : inputTables) {
                    if ((
                    // Physical table parent
                    alias != null && table.getAlias() == null && table.getName().equals(physicalTableName) ||
                    // Alias table
                    alias == null && table.getAlias() != null && table.getTableName().equals(physicalTableName))) {

                        TableEntryLocation location = new TableEntryLocation(table.getName(), previousColumnName);
                        DataMapTableView aliasTableView = mapperManager.retrieveDataMapTableView(location);
                        ITableEntry aliasTableEntry = mapperManager.retrieveTableEntry(location);
                        if (aliasTableEntry != null) {
                            processColumnNameChanged(previousColumnName, newColumnName, aliasTableView, aliasTableEntry, true);
                        }

                    }
                }

            }

        }

        // if (atLeastOneLinkHasBeenRemoved) {
        // new AsynchronousThreading(20, false, dataMapTableView.getDisplay(), new Runnable() {
        //
        // public void run() {
        // TableViewerCreator tableViewerCreatorForColumns = dataMapTableView
        // .getTableViewerCreatorForColumns();
        // boolean propagate = MessageDialog.openQuestion(tableViewerCreatorForColumns.getTable().getShell(),
        // Messages.getString("UIManager.propagateTitle"), //$NON-NLS-1$
        // Messages.getString("UIManager.propagateMessage")); //$NON-NLS-1$
        // if (propagate) {
        // TableEntryLocation previousLocation = new TableEntryLocation(currentModifiedITableEntry
        // .getParentName(), previousColumnName);
        // TableEntryLocation newLocation = new TableEntryLocation(currentModifiedITableEntry
        // .getParentName(), newColumnName);
        // mapperManager.replacePreviousLocationInAllExpressions(previousLocation, newLocation);
        // refreshSqlExpression();
        // }
        // }
        // }).start();
        // }
    }

    public OutputDataMapTableView createNewOutputTableView(Control previousControl, IDataMapTable abstractDataMapTable,
            Composite parent) {
        OutputDataMapTableView dataMapTableView = new OutputDataMapTableView(parent, SWT.BORDER, abstractDataMapTable,
                mapperManager);
        FormData formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(previousControl);
        dataMapTableView.setLayoutData(formData);
        dataMapTableView.minimizeTable(abstractDataMapTable.isMinimized());
        dataMapTableView.registerStyledExpressionEditor(getTabFolderEditors().getStyledTextHandler());
        this.mapperUI.getOutputMouseSrolledListener().addMouseWheelListener(dataMapTableView);
        return dataMapTableView;
    }

    public InputDataMapTableView createNewInputTableView(Control previousControl, InputTable inputTable, Composite parent) {

        InputDataMapTableView dataMapTableView = new InputDataMapTableView(parent, SWT.BORDER, inputTable, mapperManager);
        FormData formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(previousControl);
        dataMapTableView.setLayoutData(formData);
        dataMapTableView.minimizeTable(inputTable.isMinimized());
        dataMapTableView.registerStyledExpressionEditor(getTabFolderEditors().getStyledTextHandler());
        this.mapperUI.getInputMouseSrolledListener().addMouseWheelListener(dataMapTableView);
        return dataMapTableView;
    }

    public TablesZoneView getTablesZoneViewOutputs() {
        return this.mapperUI.getOutputTablesZoneView();
    }

    public TablesZoneView getTablesZoneViewInputs() {
        return this.mapperUI.getInputTablesZoneView();
    }

    public TablesZoneView getTablesZoneViewVars() {
        return this.mapperUI.getVarsTableZoneView();
    }

    public ScrolledComposite getScrolledCompositeViewOutputs() {
        return this.mapperUI.getScrolledCompositeViewOutputs();
    }

    public ScrolledComposite getScrolledCompositeViewInputs() {
        return this.mapperUI.getScrolledCompositeViewInputs();
    }

    public InputsZone getInputsZone() {
        return this.mapperUI.getInputsZone();
    }

    public OutputsZone getOutputsZone() {
        return this.mapperUI.getOutputsZone();
    }

    /**
     * DOC amaumont Comment method "getMetadataEditorView".
     *
     * @param newSelectedDataMapTableView
     */
    public MetadataTableEditorView getMetadataEditorView(Zone zone) {
        if (zone == Zone.INPUTS) {
            return getInputMetaEditorView();
        }
        if (zone == Zone.OUTPUTS) {
            return getOutputMetaEditorView();
        }
        return null;
    }

    public List<IColumnEntry> getLastCreatedInOutColumnEntries() {
        return this.lastCreatedInOutColumnEntries;
    }

    public void clearLastCreatedInOutColumnEntries() {
        this.lastCreatedInOutColumnEntries.clear();
    }

    public Composite getMapperContainer() {
        return mapperUI.getMapperUIParent();
    }

    public ExternalDbMapUiProperties getUiProperties() {
        if (this.uiProperties == null) {
            this.uiProperties = new ExternalDbMapUiProperties();
        }
        return this.uiProperties;
    }

    public void setUiProperties(ExternalDbMapUiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    public OutputDataMapTableView getCurrentSelectedOutputTableView() {
        return this.currentSelectedOutputTableView;
    }

    public InputDataMapTableView getCurrentSelectedInputTableView() {
        return this.currentSelectedInputTableView;
    }

    public TabFolderEditors getTabFolderEditors() {
        return this.mapperUI.getTabFolderEditors();
    }

    /**
     * DOC amaumont Comment method "openAddNewOutputDialog".
     */
    public String openNewOutputCreationDialog() {
        final IProcess process = mapperManager.getComponent().getProcess();
        String outputName = process.generateUniqueConnectionName("newOutput"); //$NON-NLS-1$
        InputDialog id = new InputDialog(getMapperContainer().getShell(), Messages.getString("UIManager.addNewOutputTable"), //$NON-NLS-1$
                Messages.getString("UIManager.typeTableName"), outputName, new IInputValidator() { //$NON-NLS-1$

                    @Override
                    public String isValid(String newText) {
                        if (KeywordsValidator.isKeyword(newText) || KeywordsValidator.isSqlKeyword(newText)) {
                            return Messages.getString("UIManager.tableNameIsNotValid"); //$NON-NLS-1$
                        }
                        if (newText != null && !process.checkValidConnectionName(newText, true)) {
                            return Messages.getString("UIManager.nameExistOrNull"); //$NON-NLS-1$
                        }
                        return null;
                    }

                });
        int response = id.open();
        if (response == InputDialog.OK) {
            return id.getValue();
        }
        return null;
    }

    /**
     * DOC amaumont Comment method "getTablesZoneView".
     *
     * @param dataMapTableViewTarget
     */
    public TablesZoneView getTablesZoneView(DataMapTableView dataMapTableViewTarget) {
        Zone zone = dataMapTableViewTarget.getZone();
        if (zone == Zone.OUTPUTS) {
            return getTablesZoneViewOutputs();
        } else if (zone == Zone.INPUTS) {
            return getTablesZoneViewInputs();
        } else {
            throw new IllegalArgumentException("Case not found"); //$NON-NLS-1$
        }
    }

    public DropTargetOperationListener getDropTargetOperationListener() {
        return this.mapperUI.getDropTargetOperationListener();
    }

    @Override
    public DraggingInfosPopup getDraggingInfosPopup() {
        return this.mapperUI.getDraggingInfosPopup();
    }

    public void moveOutputScrollBarZoneToMax() {
        ScrolledComposite scrolledCompositeViewOutputs = getScrolledCompositeViewOutputs();
        setPositionOfVerticalScrollBarZone(scrolledCompositeViewOutputs, scrolledCompositeViewOutputs.getVerticalBar()
                .getMaximum());
    }

    public void moveInputScrollBarZoneToMax() {
        ScrolledComposite scrolledCompositeViewInputs = getScrolledCompositeViewInputs();
        setPositionOfVerticalScrollBarZone(scrolledCompositeViewInputs, scrolledCompositeViewInputs.getVerticalBar().getMaximum());
    }

    public void moveScrollBarZoneAtSelectedTable(Zone zone) {
        if (zone == Zone.INPUTS) {
            moveInputScrollBarZoneAtSelectedTable();
        } else if (zone == Zone.OUTPUTS) {
            moveOutputScrollBarZoneAtSelectedTable();
        }
    }

    private void moveOutputScrollBarZoneAtSelectedTable() {
        Rectangle bounds = currentSelectedOutputTableView.getBounds();
        int selection = bounds.y - 30;
        ScrolledComposite scrolledCompositeViewOutputs = getScrolledCompositeViewOutputs();
        setPositionOfVerticalScrollBarZone(scrolledCompositeViewOutputs, selection);
    }

    private void moveInputScrollBarZoneAtSelectedTable() {
        Rectangle bounds = currentSelectedInputTableView.getBounds();
        int selection = bounds.y - 30;
        ScrolledComposite scrolledCompositeViewInputs = getScrolledCompositeViewInputs();
        setPositionOfVerticalScrollBarZone(scrolledCompositeViewInputs, selection);
    }

    private void setPositionOfVerticalScrollBarZone(ScrolledComposite scrollComposite, int scrollBarSelection) {
        ScrollBar verticalBar = scrollComposite.getVerticalBar();
        verticalBar.setSelection(scrollBarSelection);
        scrollComposite.setOrigin(0, scrollBarSelection);
    }

    /**
     * DOC amaumont Comment method "getDisplay".
     */
    public Display getDisplay() {
        return getMapperContainer().getDisplay();
    }

    /**
     *
     * DOC amaumont Comment method "moveSelectedTable".
     *
     * @param zone
     * @param moveUp true to move up, else false to move down
     */
    public void moveSelectedTable(Zone zone, boolean moveUp) {
        if (zone == Zone.INPUTS) {
            List<DataMapTableView> inputsTablesView = getInputsTablesView();
            if (moveUp) {
                moveSelectTableUp(currentSelectedInputTableView, inputsTablesView, 1);
            } else {
                moveSelectedTableDown(currentSelectedInputTableView, inputsTablesView, 0);
            }
            updateDropDownJoinTypeForInputs();
            moveInputScrollBarZoneAtSelectedTable();
        } else if (zone == Zone.OUTPUTS) {
            List<DataMapTableView> outputsTablesView = getOutputsTablesView();
            if (moveUp) {
                moveSelectTableUp(currentSelectedOutputTableView, outputsTablesView, 1);
            } else {
                moveSelectedTableDown(currentSelectedOutputTableView, outputsTablesView, 0);
            }
            moveOutputScrollBarZoneAtSelectedTable();
        }
        updateToolbarButtonsStates(zone);
        refreshBackground(true, false);
        refreshSqlExpression();
    }

    /**
     * DOC amaumont Comment method "updateDropDownJoinTypeForInputs".
     */
    public void updateDropDownJoinTypeForInputs() {
        List<DataMapTableView> inputsTablesView;
        inputsTablesView = getInputsTablesView();
        if (inputsTablesView.size() >= 1) {
            InputDataMapTableView inputView0 = (InputDataMapTableView) inputsTablesView.get(0);
            inputView0.setEnableJoinTypeDropDown(false);
        }
        if (inputsTablesView.size() >= 2) {
            InputDataMapTableView inputView1 = (InputDataMapTableView) inputsTablesView.get(1);
            inputView1.setEnableJoinTypeDropDown(true);
        }
    }

    public boolean isTableViewMoveable(Zone zone, boolean moveUp) {
        if (zone == Zone.INPUTS) {
            if (currentSelectedInputTableView == null) {
                return false;
            }
            List<DataMapTableView> tablesView = getInputsTablesView();
            int indexCurrentTable = tablesView.indexOf(currentSelectedInputTableView);
            if (moveUp) {
                if (indexCurrentTable > 0) {
                    return true;
                }
                return false;
            } else {
                if (indexCurrentTable < tablesView.size() - 1 && indexCurrentTable >= 0) {
                    return true;
                }
                return false;
            }
        } else if (zone == Zone.OUTPUTS) {
            if (currentSelectedOutputTableView == null) {
                return false;
            }
            List<DataMapTableView> tablesView = getOutputsTablesView();
            int indexCurrentTable = tablesView.indexOf(currentSelectedOutputTableView);
            if (moveUp) {
                if (indexCurrentTable > 0) {
                    return true;
                }
                return false;
            } else {
                if (indexCurrentTable < tablesView.size() - 1) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     *
     * DOC amaumont Comment method "moveSelectTableUp".
     *
     * @param currentSelectedTableView
     * @param tablesView
     * @param indexStartMovedAuthorized
     */
    private void moveSelectTableUp(DataMapTableView currentSelectedTableView, List<DataMapTableView> tablesView,
            int indexStartMovedAuthorized) {
        int indexCurrentTable = tablesView.indexOf(currentSelectedTableView);

        if (indexCurrentTable < indexStartMovedAuthorized) {
            return;
        }

        FormData formDataCurrent = (FormData) currentSelectedTableView.getLayoutData();
        DataMapTableView beforePreviousTableView = null;
        if (indexCurrentTable - 2 >= 0) {
            beforePreviousTableView = tablesView.get(indexCurrentTable - 2);
            formDataCurrent.top.control = beforePreviousTableView;
        } else {
            formDataCurrent.top.control = null;
        }

        DataMapTableView previousTableView = null;
        if (indexCurrentTable - 1 >= 0) {
            previousTableView = tablesView.get(indexCurrentTable - 1);
            FormData formDataPrevious = (FormData) previousTableView.getLayoutData();
            formDataPrevious.top.control = currentSelectedTableView;
        }

        if (indexCurrentTable + 1 <= tablesView.size() - 1) {
            DataMapTableView nextTableView = tablesView.get(indexCurrentTable + 1);
            FormData formDataNext = (FormData) nextTableView.getLayoutData();
            formDataNext.top.control = previousTableView;
        }

        tableManager.swapWithPreviousTable(currentSelectedTableView.getDataMapTable());
        currentSelectedTableView.getParent().layout();
        parseAllExpressions(currentSelectedTableView, false);
        parseAllExpressions(previousTableView, false);

        mapperManager.getProblemsManager().checkProblemsForAllEntries(currentSelectedTableView, true);
        mapperManager.getProblemsManager().checkProblemsForAllEntries(previousTableView, true);
    }

    private void moveSelectedTableDown(DataMapTableView currentSelectedTableView, List<DataMapTableView> tablesView,
            int indexStartMovedAuthorized) {
        int indexCurrentTable = tablesView.indexOf(currentSelectedTableView);

        if (indexCurrentTable < indexStartMovedAuthorized || indexCurrentTable == tablesView.size() - 1) {
            return;
        }
        DataMapTableView nextTableView = tablesView.get(indexCurrentTable + 1);

        FormData formDataCurrent = (FormData) currentSelectedTableView.getLayoutData();
        formDataCurrent.top.control = nextTableView;

        if (indexCurrentTable + 2 <= tablesView.size() - 1) {
            DataMapTableView afterNextTableView = tablesView.get(indexCurrentTable + 2);
            FormData formDataAfterNext = (FormData) afterNextTableView.getLayoutData();
            formDataAfterNext.top.control = currentSelectedTableView;
        }

        FormData formDataNext = (FormData) nextTableView.getLayoutData();
        if (indexCurrentTable - 1 >= 0) {
            formDataNext.top.control = tablesView.get(indexCurrentTable - 1);
        } else {
            formDataNext.top.control = null;
        }

        tableManager.swapWithNextTable(currentSelectedTableView.getDataMapTable());

        currentSelectedTableView.getParent().layout();
        parseAllExpressions(currentSelectedTableView, false);
        parseAllExpressions(nextTableView, false);

        mapperManager.getProblemsManager().checkProblemsForAllEntries(currentSelectedTableView, true);
        mapperManager.getProblemsManager().checkProblemsForAllEntries(nextTableView, true);
    }

    public void minimizeAllTables(Zone zone, boolean minimize, ToolItem minimizeButton) {

        List<DataMapTableView> tablesView = null;
        TablesZoneView tablesZoneView = null;
        if (zone == Zone.INPUTS) {
            tablesView = getInputsTablesView();
            tablesZoneView = getTablesZoneViewInputs();
        } else if (zone == Zone.OUTPUTS) {
            tablesZoneView = getTablesZoneViewOutputs();
            tablesView = getOutputsTablesView();
        } else {
            throw new RuntimeException("Case not found:" + zone); //$NON-NLS-1$
        }

        Layout layout = tablesZoneView.getLayout();

        try {
            tablesZoneView.setLayout(null);
            for (DataMapTableView view : tablesView) {
                view.minimizeTable(minimize);
            }
        } catch (RuntimeException e) {
            ExceptionHandler.process(e);
        } finally {
            tablesZoneView.setLayout(layout);
        }

        tablesZoneView.layout();
        for (DataMapTableView view : tablesView) {
            view.layout();
        }
        resizeTablesZoneViewAtComputedSize(zone);

        moveScrollBarZoneAtSelectedTable(zone);
    }

    /**
     * recalculate parent size don't hide DataMapTableView when resized.
     */
    public void resizeTablesZoneViewAtComputedSize(Zone zone) {
        TablesZoneView tablesZoneView = null;
        if (zone == Zone.INPUTS) {
            tablesZoneView = getTablesZoneViewInputs();
        } else if (zone == Zone.OUTPUTS) {
            tablesZoneView = getTablesZoneViewOutputs();
        }
        tablesZoneView.setSize(tablesZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Call mapperManager.removeSelectedOutput() to remove a table view.
     *
     * @param dataMapTableViewToRemove
     */
    public void removeOutputTableView(OutputDataMapTableView dataMapTableViewToRemove) {
        List<DataMapTableView> outputsTablesView = getOutputsTablesView();
        int sizeList = outputsTablesView.size();
        for (int i = 0; i < sizeList; i++) {
            Control control = outputsTablesView.get(i);
            if (control == dataMapTableViewToRemove && i < sizeList - 1 && i > 0) {
                FormData formData = (FormData) outputsTablesView.get(i + 1).getLayoutData();
                formData.top = new FormAttachment(outputsTablesView.get(i - 1));
                break;
            }
        }
        mapperManager.removeTablePair(dataMapTableViewToRemove);
        MetadataTableEditorView outputMetaEditorView = getOutputMetaEditorView();
        OutputTable outputTable = (OutputTable) dataMapTableViewToRemove.getDataMapTable();

        if (outputMetaEditorView.getMetadataTableEditor().getMetadataTable() == outputTable.getMetadataTable()) {
            getOutputMetaEditorView().setMetadataTableEditor(null);
        }
        dataMapTableViewToRemove.dispose();
        dataMapTableViewToRemove = null;

        getTablesZoneViewOutputs().layout();
        refreshBackground(true, false);
        setCurrentSelectedOutputTableView(null);
    }

    /**
     * Call mapperManager.removeSelectedOutput() to remove a table view.
     *
     * @param dataMapTableViewToRemove
     */
    public void removeInputTableView(InputDataMapTableView dataMapTableViewToRemove) {
        List<DataMapTableView> outputsTablesView = getInputsTablesView();
        int sizeList = outputsTablesView.size();
        for (int i = 0; i < sizeList; i++) {
            Control control = outputsTablesView.get(i);
            if (control == dataMapTableViewToRemove && i < sizeList - 1 && i > 0) {
                FormData formData = (FormData) outputsTablesView.get(i + 1).getLayoutData();
                formData.top = new FormAttachment(outputsTablesView.get(i - 1));
                break;
            }
        }
        mapperManager.removeTablePair(dataMapTableViewToRemove);
        MetadataTableEditorView outputMetaEditorView = getInputMetaEditorView();
        if (outputMetaEditorView.getMetadataTableEditor().getMetadataTable() == ((InputTable) dataMapTableViewToRemove
                .getDataMapTable()).getMetadataTable()) {
            getInputMetaEditorView().setMetadataTableEditor(null);
        }
        dataMapTableViewToRemove.dispose();
        dataMapTableViewToRemove = null;
        getTablesZoneViewInputs().layout();
        refreshBackground(true, false);
        setCurrentSelectedInputTableView(null);
    }

    public void registerCustomPaint() {
        List<DataMapTableView> tablesView = getOutputsTablesView();
        tablesView.addAll(getInputsTablesView());
        for (DataMapTableView view : tablesView) {
            view.getTableViewerCreatorForColumns().setUseCustomItemColoring(false);
        }

    }

    public void unregisterCustomPaint() {
        List<DataMapTableView> tablesView = getOutputsTablesView();
        tablesView.addAll(getInputsTablesView());
        for (DataMapTableView view : tablesView) {
            view.getTableViewerCreatorForColumns().setUseCustomItemColoring(false);
        }
    }

    /**
     * Getter for drawableLinkFactory.
     *
     * @return the drawableLinkFactory
     */
    public StyleLinkFactory getStyleLinkFactory() {
        if (this.drawableLinkFactory == null) {
            this.drawableLinkFactory = new StyleLinkFactory(getLinkStyle());
        }
        return this.drawableLinkFactory;
    }

    public void unselectAllEntriesOfAllTables() {
        List<DataMapTableView> tablesView = getOutputsTablesView();
        tablesView.addAll(getInputsTablesView());
        tablesView.addAll(getVarsTablesView());
        for (DataMapTableView view : tablesView) {
            view.unselectAllEntries();
        }

    }

    /**
     * DOC amaumont Comment method "enlargeLeftMarginForInputTables".
     */
    public void enlargeLeftMarginForInputTables(int countOfLevels) {
        int marginLeft = mapperUI.getInputTablesZoneView().getMarginLeft();
        int calculatedMarginLeft = calculateInputsLeftMarginFromLevelsCount(countOfLevels);
        if (calculatedMarginLeft >= marginLeft) {
            mapperUI.getInputTablesZoneView().setMarginLeft(calculatedMarginLeft + 5);
        }

    }

    private int calculateInputsLeftMarginFromLevelsCount(int countOfLevels) {
        return 5 + countOfLevels * 5;
    }

    public List<DataMapTableView> getInputsTablesView() {
        return this.tableManager.getInputsTablesView();
    }

    public List<DataMapTableView> getOutputsTablesView() {
        return this.tableManager.getOutputsTablesView();
    }

    public List<DataMapTableView> getVarsTablesView() {
        return this.tableManager.getVarsTablesView();
    }

    /**
     * Refresh Sql select query in the bottom tab and its title.
     */
    public void refreshSqlExpression() {
        if (getTabFolderEditors().getSelectionIndex() == 2) {
            mapperManager.getComponent().restoreMapperModelFromInternalData();
            OutputDataMapTableView selectedOutputTableView = getCurrentSelectedOutputTableView();
            if (selectedOutputTableView == null) {

            } else {
                IDataMapTable dataMapTable = selectedOutputTableView.getDataMapTable();
                String tableName = dataMapTable.getName();
                DbGenerationManager generationMnager = mapperManager.getComponent().getGenerationManager();
                String sql = generationMnager.buildSqlSelect(mapperManager.getComponent(), tableName);
                getTabFolderEditors().getItem(2).setText(
                        Messages.getString("TabFolderEditors.SqlSelectQuery", new Object[] { tableName })); //$NON-NLS-1$
                getTabFolderEditors().getStyledSqlText().setText(sql);
            }
        }
    }

    /**
     * Sets the currentSelectedOutputTableView.
     *
     * @param currentSelectedOutputTableView the currentSelectedOutputTableView to set
     */
    protected void setCurrentSelectedOutputTableView(OutputDataMapTableView currentSelectedOutputTableView) {
        this.currentSelectedOutputTableView = currentSelectedOutputTableView;
        refreshSqlExpression();
    }

    /**
     * Sets the currentSelectedInputTableView.
     *
     * @param currentSelectedInputTableView the currentSelectedInputTableView to set
     */
    protected void setCurrentSelectedInputTableView(InputDataMapTableView currentSelectedInputTableView) {
        this.currentSelectedInputTableView = currentSelectedInputTableView;
    }

    public Shell getShell() {
        return mapperUI.getShell();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.managers.AbstractUIManager#getAbstractMapperManager()
     */
    @Override
    public AbstractMapperManager getAbstractMapperManager() {
        return mapperManager;
    }

    public boolean checkSourceLocationIsValid(ITableEntry entrySource, ITableEntry entryTarget) {

        if (entrySource instanceof InputColumnTableEntry && entryTarget instanceof InputColumnTableEntry
        // && entrySource.getParent() != entryTarget.getParent()
        ) {
            // List<InputTable> inputTables = getInputTables();
            // int indexTableSource = inputTables.indexOf(entrySource.getParent());
            // int indexTableTarget = inputTables.indexOf(entryTarget.getParent());
            // if (indexTableSource < indexTableTarget) {
            return true;
            // }
        } else if (entryTarget instanceof OutputColumnTableEntry || entryTarget instanceof FilterTableEntry) {
            if (entrySource instanceof InputColumnTableEntry || entrySource instanceof OutputColumnTableEntry
                    && entrySource.getParent() == entryTarget.getParent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOC amaumont Comment method "checkLocationIsValid".
     *
     * @param couple
     * @param currentModifiedITableEntry
     * @param manager TODO
     * @param locationSource TODO
     * @param entryTarget TODO
     * @return
     */
    public boolean checkSourceLocationIsValid(TableEntryLocation locationSource, ITableEntry entryTarget) {
        return checkSourceLocationIsValid(mapperManager.retrieveTableEntry(locationSource), entryTarget);
    }

}
