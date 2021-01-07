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
package org.talend.designer.mapper.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.runtime.thread.AsynchronousThreading;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.utils.image.ImageCapture;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.data.list.ListenableListEvent.TYPE;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.metadata.dialog.CustomTableManager;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.designer.abstractmap.AbstractMapComponent;
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
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ExternalNodeChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperUiProperties;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.model.MapperModel;
import org.talend.designer.mapper.model.table.AbstractDataMapTable;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.mapper.model.tableentry.ExpressionFilterEntry;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.InputColumnTableEntry;
import org.talend.designer.mapper.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.model.tableentry.VarTableEntry;
import org.talend.designer.mapper.ui.MapperUI;
import org.talend.designer.mapper.ui.commands.DataMapTableViewSelectedCommand;
import org.talend.designer.mapper.ui.dialog.AutoMappingDialog;
import org.talend.designer.mapper.ui.dialog.MapReducePropertySetDialog;
import org.talend.designer.mapper.ui.dialog.OutputAddDialog;
import org.talend.designer.mapper.ui.dialog.PropertySetDialog;
import org.talend.designer.mapper.ui.footer.StatusBar;
import org.talend.designer.mapper.ui.footer.StatusBar.STATUS;
import org.talend.designer.mapper.ui.tabs.TabFolderEditors;
import org.talend.designer.mapper.ui.visualmap.TableEntryProperties;
import org.talend.designer.mapper.ui.visualmap.link.Link;
import org.talend.designer.mapper.ui.visualmap.link.StyleLinkFactory;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.table.EntryState;
import org.talend.designer.mapper.ui.visualmap.table.InputDataMapTableView;
import org.talend.designer.mapper.ui.visualmap.table.OutputDataMapTableView;
import org.talend.designer.mapper.ui.visualmap.table.VarsDataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.InputsZone;
import org.talend.designer.mapper.ui.visualmap.zone.OutputsZone;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;
import org.talend.designer.mapper.ui.visualmap.zone.scrollable.TablesZoneView;
import org.talend.designer.mapper.ui.visualmap.zone.toolbar.ToolbarOutputZone;
import org.talend.designer.mapper.ui.visualmap.zone.toolbar.ToolbarZone;
import org.talend.designer.mapper.utils.DataMapExpressionParser;
import org.talend.designer.mapper.utils.ParseExpressionResult;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class UIManager extends AbstractUIManager {

    private final MapperManager mapperManager;

    private MapperUI mapperUI;

    private Composite refComposite;

    private int mapperResponse = SWT.NONE;

    private final TableManager tableManager;

    private ILineSelectionListener inputsSelectionChangedListener;

    private ILineSelectionListener outputsSelectionChangedListener;

    private final List<IColumnEntry> lastCreatedInOutColumnEntries = new ArrayList<IColumnEntry>();

    private ExternalMapperUiProperties uiProperties;

    private OutputDataMapTableView currentSelectedOutputTableView;

    private InputDataMapTableView currentSelectedInputTableView;

    private VarsDataMapTableView currentSelectedVarsTableView;

    private Zone previousSelectedZone;

    private boolean previousSelectedTableIsConstraint;

    private boolean previousSelectedTableIsGlobalMap;

    private DisposeListener commonMetadataDisposeListener;

    private StyleLinkFactory drawableLinkFactory;

    private IModifiedBeanListener<IMetadataColumn> inputModifiedBeanListener;

    private IModifiedBeanListener<IMetadataColumn> outputModifiedBeanListener;

    private Display display;

    private boolean closeWithoutPrompt;

    private Map<IConnection, Map<String, String>> changedNameColumns = new HashMap<IConnection, Map<String, String>>();

    public static final String NAME_SEPARATOR = "--";

    private static DataMapTableView oldSelectedView;

    private final Map<String, String> oldMappingMap = new HashMap<String, String>();

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
        // reject table readonly
        IDataMapTable dataMapTable = dataMapTableView.getDataMapTable();
        if (dataMapTable instanceof OutputTable && ((OutputTable) dataMapTable).isErrorRejectTable()) {
            OutputTable table = (OutputTable) dataMapTable;
            if (table.getMetadataTable() != null) {
                for (IMetadataColumn column : table.getMetadataTable().getListColumns()) {
                    if (mapperManager.ERROR_REJECT_MESSAGE.equals(column.getLabel())
                            || mapperManager.ERROR_REJECT_STACK_TRACE.equals(column.getLabel())) {
                        column.setCustom(true);
                    }
                }
            }
            CustomTableManager.addCustomManagementToTable(getOutputMetaEditorView(), true);
        }

        TabFolderEditors tabFolderEditors = mapperUI.getTabFolderEditors();
        // tabFolderEditors.setSelection(TabFolderEditors.INDEX_TAB_METADATA_EDITOR);
        MetadataTableEditorView metadataTableEditorView = null;
        MetadataTableEditorView otherMetadataTableEditorView = null;
        final Zone currentZone = dataMapTableView.getZone();

        List<? extends AbstractDataMapTable> tables = null;

        DataMapTableView previousSelectedTableView = null;

        if (currentZone == Zone.INPUTS) {
            metadataTableEditorView = tabFolderEditors.getInputMetaEditorView();
            otherMetadataTableEditorView = tabFolderEditors.getOutputMetaEditorView();
            tables = mapperManager.getInputTables();
            previousSelectedTableView = this.currentSelectedInputTableView;
            this.currentSelectedInputTableView = (InputDataMapTableView) dataMapTableView;
            metadataTableEditorView.setReadOnly(this.currentSelectedInputTableView.getInputTable().hasReadOnlyMetadataColumns()
                    || mapperManager.componentIsReadOnly() || this.currentSelectedInputTableView.getInputTable().isReadOnly());
        } else if (currentZone == Zone.OUTPUTS) {
            metadataTableEditorView = tabFolderEditors.getOutputMetaEditorView();
            otherMetadataTableEditorView = tabFolderEditors.getInputMetaEditorView();
            tables = mapperManager.getOutputTables();
            previousSelectedTableView = this.currentSelectedOutputTableView;
            this.currentSelectedOutputTableView = (OutputDataMapTableView) dataMapTableView;
            metadataTableEditorView.setReadOnly(this.currentSelectedOutputTableView.getOutputTable().hasReadOnlyMetadataColumns()
                    || mapperManager.componentIsReadOnly());
        } else {
            this.currentSelectedVarsTableView = (VarsDataMapTableView) dataMapTableView;
        }

        if (currentZone != Zone.VARS) {

            updateToolbarButtonsStates(currentZone);

            final AbstractInOutTable abstractDataMapTable = (AbstractInOutTable) mapperManager
                    .retrieveAbstractDataMapTable(dataMapTableView);
            MetadataTableEditor currentMetadataTableEditor = metadataTableEditorView.getMetadataTableEditor();
            final TableViewerCreator dataMapTVCreator = dataMapTableView.getTableViewerCreatorForColumns();
            final TableViewer dataMapTableViewer = dataMapTableView.getTableViewerCreatorForColumns().getTableViewer();
            if (currentMetadataTableEditor == null || currentMetadataTableEditor != null && oldSelectedView != dataMapTableView) {
                oldSelectedView = dataMapTableView;
                if (useNewCommand) {
                    DataMapTableViewSelectedCommand command = new DataMapTableViewSelectedCommand(this,
                            previousSelectedTableView, dataMapTableView);
                    mapperManager.executeCommand(command);
                }

                currentMetadataTableEditor = new MetadataTableEditor(abstractDataMapTable.getMetadataTable(),
                        abstractDataMapTable.getName());

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
                    public void handleEvent(ListenableListEvent event) {
                        DataMapTableView view = mapperManager.retrieveAbstractDataMapTableView(abstractDataMapTable);
                        List<DataMapTableView> relatedOutputsTableView = getRelatedOutputsTableView(dataMapTableView);

                        if (event.type == TYPE.ADDED) {
                            // metadataEditorTableViewer.refresh();
                            List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.addedObjects;

                            lastCreatedInOutColumnEntries.clear();
                            if (event.index != null) {
                                int index = event.index;
                                for (IMetadataColumn metadataColumn : metadataColumns) {
                                    lastCreatedInOutColumnEntries.add(mapperManager.addNewColumnEntry(dataMapTableView,
                                            metadataColumn, index));
                                    // handle related table view
                                    for (DataMapTableView tableView : relatedOutputsTableView) {
                                        // changed for bug TDI-26551 in July 2,2013 by fwang, should use original
                                        // expression for related table.
                                        IMetadataColumn relatedMetadata = metadataColumn;
                                        if (!oldMappingMap.isEmpty()) {
                                            IDataMapTable retrieveAbstractDataMapTable = mapperManager
                                                    .retrieveAbstractDataMapTable(tableView);
                                            relatedMetadata = metadataColumn.clone();
                                            String label = relatedMetadata.getLabel();
                                            String expression = oldMappingMap.get(retrieveAbstractDataMapTable.getName() + "_"
                                                    + label);
                                            relatedMetadata.setExpression(expression == null ? "" : expression);
                                        }

                                        mapperManager.addNewColumnEntry(tableView, relatedMetadata, index);
                                    }
                                    index = index + 1;

                                }
                            } else if (event.indicesTarget != null) {
                                List<Integer> indicesTarget = event.indicesTarget;
                                int lstSize = indicesTarget.size();
                                for (int i = 0; i < lstSize; i++) {
                                    Integer indice = indicesTarget.get(i);
                                    IMetadataColumn metadataColumn = metadataColumns.get(i);
                                    lastCreatedInOutColumnEntries.add(mapperManager.addNewColumnEntry(dataMapTableView,
                                            metadataColumn, indice));

                                    // handle related table view
                                    for (DataMapTableView tableView : relatedOutputsTableView) {
                                        mapperManager.addNewColumnEntry(tableView, metadataColumn, indice);
                                    }
                                }

                            } else {
                                throw new IllegalStateException(Messages.getString("UIManager.1")); //$NON-NLS-1$
                            }
                            refreshBackground(false, false);
                            if (event.index != null) {
                                dataMapTableViewer.refresh();
                                dataMapTVCreator.getSelectionHelper().setSelection(event.index);
                                if (dataMapTableView.canBeResizedAtPreferedSize()) {
                                    dataMapTableView.changeSize(view.getPreferredSize(true, false, false), true, true);
                                }
                                // resize ralated table
                                for (DataMapTableView tableView : relatedOutputsTableView) {
                                    if (tableView.canBeResizedAtPreferedSize()) {
                                        tableView.changeSize(tableView.getPreferredSize(true, false, false), true, true);
                                    }
                                }

                            } else if (event.indicesTarget != null) {
                                dataMapTableViewer.refresh();
                                dataMapTableView.changeSize(view.getPreferredSize(false, true, false), true, true);
                                int[] selection = ArrayUtils.toPrimitive((Integer[]) event.indicesTarget.toArray(new Integer[0]));
                                dataMapTVCreator.getSelectionHelper().setSelection(selection);

                                for (DataMapTableView tableView : relatedOutputsTableView) {
                                    tableView.changeSize(tableView.getPreferredSize(false, true, false), true, true);
                                }
                            }
                        }

                        if (event.type == TYPE.REMOVED) {
                            // metadataEditorTableViewer.refresh();
                            List<IMetadataColumn> metadataColumns = (List<IMetadataColumn>) event.removedObjects;
                            for (IMetadataColumn metadataColumn : metadataColumns) {
                                ITableEntry metadataTableEntry = mapperManager.retrieveTableEntry(new TableEntryLocation(
                                        abstractDataMapTable.getName(), metadataColumn.getLabel()));
                                mapperManager.removeTableEntry(metadataTableEntry);

                                for (DataMapTableView tableView : relatedOutputsTableView) {
                                    IDataMapTable retrieveAbstractDataMapTable = mapperManager
                                            .retrieveAbstractDataMapTable(tableView);
                                    metadataTableEntry = mapperManager.retrieveTableEntry(new TableEntryLocation(
                                            retrieveAbstractDataMapTable.getName(), metadataColumn.getLabel()));
                                    mapperManager.removeTableEntry(metadataTableEntry);
                                    if (tableView.canBeResizedAtPreferedSize()) {
                                        tableView.resizeAtExpandedSize();
                                    }
                                }
                            }

                            dataMapTableViewer.refresh();
                            if (dataMapTableView.canBeResizedAtPreferedSize()) {
                                dataMapTableView.resizeAtExpandedSize();
                            }
                            resizeTablesZoneViewAtComputedSize(dataMapTableView.getZone());
                            moveScrollBarZoneAtSelectedTable(dataMapTableView.getZone());
                            refreshBackground(true, false);
                        }

                        if (event.type == TYPE.SWAPED) {
                            List<Integer> listIndexTarget = event.indicesTarget;
                            abstractDataMapTable.swapColumnElements(event.indicesOrigin, listIndexTarget);
                            // handle related table view
                            for (DataMapTableView tableView : relatedOutputsTableView) {
                                AbstractDataMapTable relatedTable = (AbstractDataMapTable) tableView.getDataMapTable();
                                relatedTable.swapColumnElements(event.indicesOrigin, listIndexTarget);
                            }

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
                    DataMapTableView otherDataMapTableView = mapperManager.retrieveAbstractDataMapTableView(table);
                    otherDataMapTableView.setBackground(dataMapTableView.getDisplay().getSystemColor(
                            SWT.COLOR_WIDGET_LIGHT_SHADOW));
                }
                dataMapTableView.setBackground(dataMapTableView.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
            }

            if (selectAllEntries) {
                dataMapTVCreator.getTable().selectAll();
                selectAllLinks(dataMapTableView);
                if (currentZone != Zone.VARS) {
                    metadataTableEditorView.getTableViewerCreator().getTable().selectAll();
                    metadataTableEditorView.getToolBar().updateEnabledStateOfButtons();
                }
                if (currentZone == Zone.OUTPUTS) {
                    dataMapTableView.getTableViewerCreatorForFilters().getTable().selectAll();
                }
            }

            boolean isMainSchemaRepository = false;
            if (abstractDataMapTable instanceof OutputTable) {
                OutputTable outputTable = (OutputTable) abstractDataMapTable;
                if (outputTable.getIsJoinTableOf() != null || !"".equals(outputTable.getIsJoinTableOf())) {
                    final OutputTable outputTableByName = mapperManager.getOutputTableByName(outputTable.getIsJoinTableOf());
                    if (outputTableByName != null && outputTableByName.getId() != null) {
                        isMainSchemaRepository = true;
                    }
                }
            }

            dataMapTableView.enableDiaplayViewer(abstractDataMapTable.isRepository() || isMainSchemaRepository);
        }

        if (selectAllEntries && currentZone == Zone.VARS) {
            selectAllLinks(dataMapTableView);
            mapperManager.getUiManager().getVarsTablesView().get(0).getTableViewerCreatorForColumns().getTable().selectAll();
        }

        if (otherMetadataTableEditorView != null) {
            otherMetadataTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
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
            toolbar.setEnabledMinimizeTablesButton(mapperManager.getUiManager().getInputsTablesView().size() > 0);
        } else if (currentZone == Zone.OUTPUTS) {
            toolbar = getOutputsZone().getToolbar();
            ((ToolbarOutputZone) toolbar).setEnabledRemoveTableButton(currentSelectedOutputTableView != null
                    && currentSelectedOutputTableView.getDataMapTable() instanceof OutputTable
                    && !((OutputTable) currentSelectedOutputTableView.getDataMapTable()).isErrorRejectTable());
            toolbar.setEnabledMinimizeTablesButton(mapperManager.getUiManager().getOutputsTablesView().size() > 0);
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

            public void handleEvent(ModifiedBeanEvent<IMetadataColumn> event) {
                if (AbstractMetadataTableEditorView.ID_COLUMN_NAME.equals(event.column.getId())
                        && !event.previousValue.equals(event.newValue)) {
                    IMetadataColumn modifiedObject = event.bean;
                    if (modifiedObject != null) {
                        TableEntryLocation tableEntryLocation = new TableEntryLocation(dataMapTableView.getDataMapTable()
                                .getName(), (String) event.previousValue);
                        final ITableEntry dataMapTableEntry = mapperManager.retrieveTableEntry(tableEntryLocation);
                        // see bug 7471,record the modification about the column value
                        String curConnName = null;
                        if (dataMapTableEntry != null) {
                            curConnName = dataMapTableEntry.getParentName();
                        }
                        List<? extends IConnection> curInConns = mapperManager.getAbstractMapComponent().getIncomingConnections();
                        List<? extends IConnection> curOutConns = mapperManager.getAbstractMapComponent()
                                .getOutgoingConnections();
                        for (IConnection curInConn : curInConns) {
                            if (curConnName.equals(curInConn.getUniqueName())) {
                                Map<String, String> changedNameInColumns = new HashMap<String, String>();
                                changedNameInColumns.put((String) event.newValue, (String) event.previousValue);
                                changedNameColumns.put(curInConn, changedNameInColumns);
                            }
                        }
                        for (IConnection curOutConn : curOutConns) {
                            if (curConnName.equals(curOutConn.getUniqueName())) {
                                Map<String, String> changedNameOutColumns = new HashMap<String, String>();
                                changedNameOutColumns.put((String) event.newValue, (String) event.previousValue);
                                changedNameColumns.put(curOutConn, changedNameOutColumns);
                            }
                        }

                        processColumnNameChanged((String) event.previousValue, (String) event.newValue, dataMapTableView,
                                dataMapTableEntry);

                        // related tables
                        List<DataMapTableView> relatedOutputsTableView = getRelatedOutputsTableView(dataMapTableView);
                        for (DataMapTableView relatedView : relatedOutputsTableView) {
                            TableEntryLocation relatedEntryLocation = new TableEntryLocation(relatedView.getDataMapTable()
                                    .getName(), (String) event.previousValue);
                            ITableEntry relatedTableEntry = mapperManager.retrieveTableEntry(relatedEntryLocation);
                            processColumnNameChanged((String) event.previousValue, (String) event.newValue, relatedView,
                                    relatedTableEntry);
                        }

                    }
                    // dataMapTableViewer.refresh(event.bean, true);
                    tableViewer.refresh(true);
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

        case VARS:
            return -mapperUI.getScollbarSelectionZoneVars();

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
        case VARS:
            return mapperUI.getScollbarZoneVars();
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

        boolean save = true;

        for (DataMapTableView dataMapTableView : getInputsTablesView()) {
            dataMapTableView.notifyFocusLost();
        }
        for (DataMapTableView dataMapTableView : getOutputsTablesView()) {
            dataMapTableView.notifyFocusLost();
        }
        for (DataMapTableView dataMapTableView : getVarsTablesView()) {
            dataMapTableView.notifyFocusLost();
        }
        if ((response == SWT.OK || response == SWT.APPLICATION_MODAL)
                && mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(false)) {

            save = MessageDialog.openConfirm(getMapperContainer().getShell(),
                    Messages.getString("UIManager.SaveDespiteErrors.Title"), //$NON-NLS-1$
                    Messages.getString("UIManager.SaveDespiteErrors.Message")); //$NON-NLS-1$
        }
        if (save) {
            Composite parent = mapperUI.getMapperUIParent();

            prepareClosing(response);

            if (parent instanceof Shell) {
                if (response == SWT.OK || response == SWT.CANCEL) {
                    ((Shell) parent).close();
                } else {
                    IExternalNode externalNode = mapperManager.getAbstractMapComponent().getExternalNode();
                    IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

                    if (externalNode != null && (part instanceof AbstractMultiPageTalendEditor)) {
                        INode node = externalNode.getOriginalNode();
                        if (node != null && node instanceof Node) {
                            Command cmd = new ExternalNodeChangeCommand((Node) node, externalNode);
                            CommandStack cmdStack = (CommandStack) part.getAdapter(CommandStack.class);
                            cmdStack.execute(cmd);
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC amaumont Comment method "saveDataBeforeClose".
     *
     * @param response
     */
    public void prepareClosing(int response) {
        // TDI-22179
        if (response == SWT.OK || response == SWT.APPLICATION_MODAL) {
            setMapperResponse(SWT.OK);
        }

        if (response == SWT.CANCEL) {
            removeUnsavedOutputsFromProcess();
        } else {
            mapperManager.getAbstractMapComponent().restoreMapperModelFromInternalData();
        }

        saveCurrentUIProperties();

        if (response == SWT.OK || response == SWT.APPLICATION_MODAL) {
            createVisualMapImage();
            closeWithoutPrompt = true;
            // see bug 7471
            Map<IConnection, Set<String>> preColumnSets = mapperUI.getPreColumnSet();
            List<InputTable> inputTables = mapperManager.getInputTables();
            List<OutputTable> outputTables = mapperManager.getOutputTables();
            setTraceFilterParaMapper(preColumnSets, inputTables, changedNameColumns);
            setTraceFilterParaMapper(preColumnSets, outputTables, changedNameColumns);
            refreshVisualMapImage();
        }

        mapperManager.updateEmfParameters(EParameterName.PREVIEW.getName());
    }

    /**
     * DOC wzhang Comment method "setTraceFilterParaMapper".
     */
    public void setTraceFilterParaMapper(Map<IConnection, Set<String>> preColumnSet,
            List<? extends AbstractInOutTable> curTables, Map<IConnection, Map<String, String>> changedColumnMap) {
        for (IConnection curConn : preColumnSet.keySet()) {
            Set<String> addedColumns = new HashSet<String>();
            Set<String> preColSet = preColumnSet.get(curConn);
            Map<String, String> changedColumns = changedColumnMap.get(curConn);
            for (AbstractInOutTable table : curTables) {
                String curTableName = table.getName();
                if (curTableName.equals(curConn.getUniqueName())) {
                    if (changedColumns != null) {
                        for (String newName : changedColumns.keySet()) {
                            String oldName = changedColumns.get(newName);
                            if (preColSet.contains(oldName)) {
                                preColSet.remove(oldName);
                                preColSet.add(newName);
                            }
                        }
                    }
                    List<IMetadataColumn> curTableColumn = table.getMetadataTable().getListColumns();
                    for (IMetadataColumn curColumn : curTableColumn) {
                        if (!(preColSet.contains(curColumn.getLabel()))) {
                            addedColumns.add(curColumn.getLabel());
                        }
                    }
                }
            }
            CorePlugin.getDefault().getDesignerCoreService().updateTraceColumnValues(curConn, changedColumns, addedColumns);
        }
    }

    private void removeUnsavedOutputsFromProcess() {

        AbstractMapComponent abstractMapComponent = getAbstractMapperManager().getAbstractMapComponent();
        IProcess process = abstractMapComponent.getProcess();

        List<OutputTable> currentOutputTablesList = mapperManager.getOutputTables();
        HashSet<String> currentTables = new HashSet<String>(currentOutputTablesList.size());
        for (OutputTable outputTable : currentOutputTablesList) {
            currentTables.add(outputTable.getName());
        }

        ExternalMapperData originalExternalData = (ExternalMapperData) mapperManager.getOriginalExternalData();
        HashSet<String> originalTableNames = new HashSet<String>();
        if (originalExternalData != null) {
            List<ExternalMapperTable> originalOutputTables = originalExternalData.getOutputTables();
            for (ExternalMapperTable outputTable : originalOutputTables) {
                originalTableNames.add(outputTable.getName());
            }

        }

        for (String currentTable : currentTables) {
            if (!originalTableNames.contains(currentTable)) {
                process.removeUniqueConnectionName(currentTable);
            }
        }

    }

    /**
     * DOC amaumont Comment method "createVisualMapImage".
     */
    private void createVisualMapImage() {
        // String previewFilePath = mapperManager.getPreviewFilePath();
        // if (previewFilePath != null) {
        Image image = ImageCapture.capture(mapperUI.getDatasFlowViewSashForm());
        // avoid to scale to make the pic more clear
        // image = ImageUtils.scale(image, 50);
        // add by hshen 7410
        ImageDescriptor imagedes = ImageDescriptor.createFromImage(image);
        mapperManager.getAbstractMapComponent().setScreenshot(imagedes);
        // byte[] mapdata = ImageUtils.saveImageToData(imagedes);
        // String previewFileName = getPreviewFileName();
        // List<? extends IElementParameter> elementParameters = null;
        //  ((IElementParameter) elementParameters).setValue(mapdata); //$NON-NLS-1$

        // ImageUtils.save(image, previewFilePath, SWT.IMAGE_BMP);
        // }
    }

    private void refreshVisualMapImage() {
        AbstractMapComponent mapCom = mapperManager.getAbstractMapComponent();
        if (mapCom == null) {
            return;
        }
        INode component = mapCom.getOriginalNode();
        if (component != null && component.getExternalNode() != null && component.getExternalNode().getScreenshot() != null) {
            byte[] saveImageToData = ImageUtils.saveImageToData(component.getExternalNode().getScreenshot());
            IProcess process = component.getProcess();
            if (process instanceof IProcess2) {
                IProcess2 processtmp = (IProcess2) process;
                Item item = processtmp.getProperty().getItem();
                if (item instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) item;
                    processItem.getProcess().getScreenshots().put(component.getUniqueName(), saveImageToData);
                } else if (item instanceof JobletProcessItem) {
                    JobletProcessItem jobletItem = (JobletProcessItem) item;
                    jobletItem.getJobletProcess().getScreenshots().put(component.getUniqueName(), saveImageToData);
                }

            }

        }
    }

    /**
     * DOC amaumont Comment method "saveUiProperties".
     */
    private void saveCurrentUIProperties() {
        uiProperties = new ExternalMapperUiProperties();
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

        TableViewerCreator tableViewerCreatorForColumns = view.getTableViewerCreatorForColumns();
        if (tableViewerCreatorForColumns != null) {
            final TableViewer tableViewerForEntries = tableViewerCreatorForColumns.getTableViewer();
            if (tableViewerForEntries.getSelection() != null) {
                List<ITableEntry> extractSelectedTableEntries = extractSelectedTableEntries(tableViewerForEntries.getSelection());
                for (ITableEntry entry : extractSelectedTableEntries) {
                    if (entry instanceof OutputColumnTableEntry) {
                        if (((OutputColumnTableEntry) entry).getMetadataColumn().isCustom() && view.getZone() == Zone.OUTPUTS) {
                            metadataTableEditorView.getExtendedToolbar().getRemoveButton().getButton().setEnabled(false);
                            break;
                        }
                    }

                }
            }
        }
    }

    public void selectAllLinks(DataMapTableView dataMapTableView) {
        selectLinks(dataMapTableView, null, false, true);
    }

    /**
     * Highlight links and linked cells which have are referenced by the selected items.
     *
     * @param dataMapTableView
     * @param forceResetHighlightLinksForOtherTables TODO
     * @param selectAllTableLinks TODO
     * @param selectedMetadataTableEntries, source or targets entries which must be highlighted, can be null to select
     * all links of a same DataMapTableView
     */
    @SuppressWarnings("unchecked")
    public void selectLinks(DataMapTableView dataMapTableView, List<ITableEntry> selectedTableEntries,
            boolean forceResetHighlightLinksForOtherTables, boolean selectAllTableLinks) {

        if (!selectAllTableLinks && selectedTableEntries == null || selectedTableEntries != null
                && selectedTableEntries.size() == 0) {
            return;
        }

        UIManager uiManager = mapperManager.getUiManager();
        TableViewerCreator<ITableEntry> currentTableViewer = null;

        boolean isFilterTableSelected = false;
        boolean isGlobalMapTableSelected = false;

        if (!selectAllTableLinks) {
            ITableEntry firstEntry = selectedTableEntries.get(0);
            if (firstEntry.isTableEntry()) {
                if (firstEntry instanceof FilterTableEntry) {
                    isFilterTableSelected = true;
                    currentTableViewer = dataMapTableView.getTableViewerCreatorForFilters();
                } else if (firstEntry instanceof GlobalMapEntry) {
                    isGlobalMapTableSelected = true;
                    currentTableViewer = dataMapTableView.getTableViewerCreatorForGlobalMap();
                } else {
                    currentTableViewer = dataMapTableView.getTableViewerCreatorForColumns();
                }
            }
        }

        // Color selectedColor = dataMapTableView.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
        Color unselectedColor = dataMapTableView.getDisplay().getSystemColor(SWT.COLOR_WHITE);

        Zone currentZone = dataMapTableView.getZone();

        Set<ITableEntry> hashSelectedMetadataTableEntries = new HashSet<ITableEntry>();
        if (selectAllTableLinks) {
            if (currentZone == Zone.INPUTS || currentZone == Zone.OUTPUTS) {
                hashSelectedMetadataTableEntries.add(((AbstractInOutTable) dataMapTableView.getDataMapTable())
                        .getExpressionFilter());
            }
            hashSelectedMetadataTableEntries.addAll(dataMapTableView.getTableViewerCreatorForColumns().getInputList());
            if (currentZone == Zone.INPUTS) {
                hashSelectedMetadataTableEntries.addAll(dataMapTableView.getTableViewerCreatorForGlobalMap().getInputList());
            }
            if (currentZone == Zone.OUTPUTS) {
                hashSelectedMetadataTableEntries.addAll(dataMapTableView.getTableViewerCreatorForFilters().getInputList());
            }
        } else {
            hashSelectedMetadataTableEntries.addAll(selectedTableEntries);
        }

        // ////////////////////////////////////////////////////////////////////////
        // Unselect all links and tableEntries if Ctrl or Shift keys are not pressed or if zone different of last
        // selection
        boolean zoneHasChanged = (previousSelectedZone == Zone.INPUTS || previousSelectedZone == Zone.VARS)
                && currentZone == Zone.OUTPUTS || (currentZone == Zone.INPUTS || currentZone == Zone.VARS)
                && previousSelectedZone == Zone.OUTPUTS;
        boolean tableTypeHasChanged = previousSelectedTableIsConstraint != isFilterTableSelected && currentZone == Zone.OUTPUTS
                || previousSelectedTableIsGlobalMap != isGlobalMapTableSelected && currentZone == Zone.INPUTS;
        boolean resetHighlightObjectsForOtherTables = !uiManager.isDragging()
                && (!uiManager.isCtrlPressed() && !uiManager.isShiftPressed() || zoneHasChanged);
        if (resetHighlightObjectsForOtherTables || forceResetHighlightLinksForOtherTables) {
            for (IMapperLink link : mapperManager.getLinks()) {
                if (!hashSelectedMetadataTableEntries.contains(link.getPointLinkDescriptor1().getTableEntry())
                        && !hashSelectedMetadataTableEntries.contains(link.getPointLinkDescriptor2().getTableEntry())) {
                    link.setState(LinkState.UNSELECTED);
                    ITableEntry sourceITableEntry = link.getPointLinkDescriptor1().getTableEntry();
                    TableItem tableItem = mapperManager.retrieveTableItem(sourceITableEntry);
                    if(tableItem != null) {
                        tableItem.setBackground(unselectedColor);
                    }
                    ITableEntry targetITableEntry = link.getPointLinkDescriptor2().getTableEntry();
                    if(tableItem != null) {
                        tableItem = mapperManager.retrieveTableItem(targetITableEntry);
                    }
                    if (tableItem != null) {
                        tableItem.setBackground(unselectedColor);
                    }
                }
            }

            if (currentZone == Zone.INPUTS || currentZone == Zone.VARS) {
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
                        viewToDeselectEntries.unselectAllGlobalMapEntries();
                    } else if (isGlobalMapTableSelected) {
                        viewToDeselectEntries.unselectAllColumnEntries();
                        viewToDeselectEntries.unselectAllConstraintEntries();
                    } else {
                        viewToDeselectEntries.unselectAllGlobalMapEntries();
                        viewToDeselectEntries.unselectAllConstraintEntries();
                    }
                }
            }
        }
        // ////////////////////////////////////////////////////////////////////////

        // ////////////////////////////////////////////////////////////////////////
        // Select or unselect links and tableEntries
        List<ITableEntry> allEntriesOfCurrentTableView = new ArrayList<ITableEntry>();
        if (currentZone == Zone.INPUTS || currentZone == Zone.OUTPUTS) {
            allEntriesOfCurrentTableView.add(((AbstractInOutTable) dataMapTableView.getDataMapTable()).getExpressionFilter());
        }
        if (selectAllTableLinks || currentTableViewer != null) {
            allEntriesOfCurrentTableView.addAll(dataMapTableView.getTableViewerCreatorForColumns().getInputList());
            if (selectAllTableLinks && currentZone == Zone.OUTPUTS) {
                allEntriesOfCurrentTableView.addAll(dataMapTableView.getTableViewerCreatorForFilters().getInputList());
            }
            // if (selectAllTableLinks && currentZone == Zone.INPUTS) {
            if (currentZone == Zone.INPUTS) {
                allEntriesOfCurrentTableView.addAll(dataMapTableView.getTableViewerCreatorForGlobalMap().getInputList());
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
        previousSelectedTableIsGlobalMap = isGlobalMapTableSelected;
    }

    public void unselectAllInputMetaDataEntries() {
        getInputMetaEditorView().getTable().deselectAll();
    }

    public void unselectAllOutputMetaDataEntries() {
        getOutputMetaEditorView().getTable().deselectAll();
    }

    public void setEntryState(MapperManager pMapperManager, EntryState entryState, ITableEntry entry) {
        if (!(entry instanceof ExpressionFilterEntry)) {
            TableItem tableItem = pMapperManager.retrieveTableItem(entry);
            if(tableItem != null) {
                tableItem.setBackground(entryState.getColor());
            }
        }
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
        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(tableEntry);

        int entriesSize = 0;
        int minHeight = dataMapTableView.getTableViewerCreatorForColumns().getTable().getHeaderHeight() + dataMapTableView.getTableViewerCreatorForColumns().getTable().getItemHeight();
        TableItem[] tableItems = new TableItem[0];
        if (tableEntry instanceof InputColumnTableEntry || tableEntry instanceof OutputColumnTableEntry) {
            tableItems = dataMapTableView.getTableViewerCreatorForColumns().getTable().getItems();

            AbstractInOutTable abstractInOutTable = (AbstractInOutTable) dataMapTableView.getDataMapTable();
            if (dataMapTableView.getZone() == Zone.OUTPUTS) {
                OutputTable outputTable = (OutputTable) abstractInOutTable;
                List<IColumnEntry> oldOuputEntries = outputTable.getDataMapTableEntries();
                entriesSize = oldOuputEntries.size();
            }
            if (dataMapTableView.getZone() == Zone.INPUTS) {
                InputTable inputTable = (InputTable) abstractInOutTable;
                List<IColumnEntry> oldOuputEntries = inputTable.getDataMapTableEntries();
                entriesSize = oldOuputEntries.size();
            }
        }
        Rectangle tableViewBounds = dataMapTableView.getBounds();
        Point pointFromTableViewOrigin = null;
        Display display = dataMapTableView.getDisplay();
        Point returnedPoint = new Point(0, 0);
        TableEntryProperties tableEntryProperties = null;

        int itemIndex = 0;
        if (tableEntry instanceof IColumnEntry || tableEntry instanceof FilterTableEntry || tableEntry instanceof GlobalMapEntry) {
            tableEntryProperties = mapperManager.getTableEntryProperties(tableEntry);
            returnedPoint = tableEntryProperties.position;
            if (forceRecalculate || returnedPoint == null) {
                int y;
                TableItem tableItem = mapperManager.retrieveTableItem(tableEntry);
                boolean isOutputEntry = tableEntry instanceof OutputColumnTableEntry;
                boolean isIntputEntry = tableEntry instanceof InputColumnTableEntry;
                boolean checked = false;
                for (int i = 0; i < tableItems.length; i++) {
                    if (tableItems[i].getData() == tableEntry) {
                        itemIndex= i;
                        break;
                    }
                }
                boolean allIsNull = false;
                if(tableItem == null && (isIntputEntry || isOutputEntry)){
                    if(tableItems.length > 0){
                        tableItem = tableItems[0];
                        checked = true;
                    }else {
                        allIsNull = true;
                    }
                }

                if(!allIsNull){
                    Table table = tableItem.getParent();
                    Rectangle boundsTableItem = tableItem.getBounds(1);// FIX for issue 1225 ("1" parameter added)
                    y = boundsTableItem.y + table.getItemHeight() / 2  + dataMapTableView.getBorderWidth();

                    if(isOutputEntry || isIntputEntry) {
                        if( entriesSize != tableItems.length) {
                            y = boundsTableItem.y + table.getItemHeight() / 2  + dataMapTableView.getBorderWidth();
                        }
                    }
                    if(checked) {
                        y = boundsTableItem.y  + dataMapTableView.getBorderWidth();
                        checked = false;
                    }
                    int x = 0;
                    if (y < 0) {
                        y = 0;
                    }

                    Point point = new Point(x, y);

                    pointFromTableViewOrigin = display.map(tableItem.getParent(), dataMapTableView, point);
                } else {
                    Text columnFilterText = dataMapTableView.getColumnNameFilterText();
                    Point point = new Point(-dataMapTableView.getBorderWidth() - 19, minHeight);
                    pointFromTableViewOrigin = display.map(columnFilterText, dataMapTableView, point);
                }
            }
        } else if (tableEntry instanceof ExpressionFilterEntry) {
            StyledText expressionFilterText = dataMapTableView.getExpressionFilterText();
//            dataMapTableView.getex
            Point point = new Point(-dataMapTableView.getBorderWidth() - 19, 16);
            pointFromTableViewOrigin = display.map(expressionFilterText, dataMapTableView, point);
        } else {
            throw new IllegalStateException("Case not found"); //$NON-NLS-1$
        }

        if (pointFromTableViewOrigin.y > tableViewBounds.height - TableEntriesManager.HEIGHT_REACTION) {
            pointFromTableViewOrigin.y = tableViewBounds.height - TableEntriesManager.HEIGHT_REACTION;
        }

        returnedPoint = convertPointToReferenceOrigin(getReferenceComposite(), pointFromTableViewOrigin, dataMapTableView);
        if (tableEntryProperties != null) {
            tableEntryProperties.position = returnedPoint;
        }
        return returnedPoint;
    }

    public Point convertPointToReferenceOrigin(final Composite referenceComposite, Point point, Composite child) {
        Point returnedPoint = new Point(point.x, point.y);
        if (WindowSystem.isBigSurOrLater()) {
            int headerHeight = (child instanceof DataMapTableView) ? ((DataMapTableView) child).getHeaderHeight() : 0;
            if (returnedPoint.y < headerHeight) {
                returnedPoint.y = headerHeight;
            }
        }
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
        return mapperUI.getTabFolderEditors().getInputMetaEditorView();
    }

    public MetadataTableEditorView getOutputMetaEditorView() {
        return mapperUI.getTabFolderEditors().getOutputMetaEditorView();
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

    public void parseNewFilterColumn(String expression, ITableEntry currentModifiedITableEntry, boolean appliedOrCanceled) {
        ParseExpressionResult result = parseFilterColumn(expression, currentModifiedITableEntry, true, true, appliedOrCanceled);
//        if (result.isAtLeastOneLinkHasBeenAddedOrRemoved()) {
//            mapperManager.getUiManager().refreshBackground(false, false);
//        }
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

        if (mapperManager.isAdvancedMap()
                && (dataMapTableView.getZone() == Zone.INPUTS || dataMapTableView.getZone() == Zone.OUTPUTS)) {
            AbstractInOutTable table = (AbstractInOutTable) dataMapTableView.getDataMapTable();
            if (dataMapTableView.getZone() == Zone.INPUTS) {
                InputTable inputTable = (InputTable) table;
                List<GlobalMapEntry> globalMapEntries = inputTable.getGlobalMapEntries();
                if (globalMapEntries != null && !inputTable.isMainConnection()) {
                    parseAllExpressions(globalMapEntries, newLinksMustHaveSelectedState);
                }
            }
            if (table.isActivateExpressionFilter()) {
                ExpressionFilterEntry expressionFilter = table.getExpressionFilter();
                parseExpression(expressionFilter.getExpression(), expressionFilter, newLinksMustHaveSelectedState, false, false);
            }
        } else if (dataMapTableView.getZone() == Zone.OUTPUTS) {
            List<ITableEntry> constraintEntriesList = dataMapTableView.getTableViewerCreatorForFilters().getInputList();
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

        if (currentModifiedITableEntry instanceof InputColumnTableEntry) {
            InputColumnTableEntry entry = (InputColumnTableEntry) currentModifiedITableEntry;
            if (StringUtils.trimToNull(expression) == null) {
                entry.setOperator(null);
            }
        }

        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(currentModifiedITableEntry);
        boolean linkHasBeenAdded = false;
        boolean linkHasBeenRemoved = false;

        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(LanguageProvider.getCurrentLanguage());
        TableEntryLocation[] tableEntriesLocationsSources = dataMapExpressionParser.parseTableEntryLocations(expression);
        Set<TableEntryLocation> alreadyProcessed = new HashSet<TableEntryLocation>();
        Set<ITableEntry> sourcesForTarget = mapperManager.getSourcesForTarget(currentModifiedITableEntry);
        Set<ITableEntry> sourcesForTargetToDelete = new HashSet<ITableEntry>(sourcesForTarget);

        boolean isInputEntry = currentModifiedITableEntry instanceof InputColumnTableEntry;

        ECodeLanguage codeLanguage = LanguageProvider.getCurrentLanguage().getCodeLanguage();

        for (TableEntryLocation tableEntriesLocationsSource : tableEntriesLocationsSources) {
            TableEntryLocation location = tableEntriesLocationsSource;

            // tests to know if link must be removed if key is unchecked
            boolean dontRemoveLink = (!isInputEntry || isInputEntry
                    && (inputExpressionAppliedOrCanceled || !inputExpressionAppliedOrCanceled
                            && !mapperManager
                                    .checkEntryHasInvalidUncheckedKey((InputColumnTableEntry) currentModifiedITableEntry)));

            if (!alreadyProcessed.contains(location) && checkSourceLocationIsValid(location, currentModifiedITableEntry)
                    && (mapperManager.isAdvancedMap() || !mapperManager.isAdvancedMap() && dontRemoveLink)) {
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

        Set<IMapperLink> targets = mapperManager.getGraphicalLinksFromTarget(currentModifiedITableEntry);
        Set<IMapperLink> linksFromTarget = new HashSet<IMapperLink>(targets);
        for (IMapperLink link : linksFromTarget) {
            if (sourcesForTargetToDelete.contains(link.getPointLinkDescriptor1().getTableEntry())) {
                mapperManager.removeLink(link, link.getPointLinkDescriptor2().getTableEntry());
                linkHasBeenRemoved = true;
            }
        }
        mapperManager.orderLinks();

        if (!mapperManager.isAdvancedMap()) {
            if (dataMapTableView.getZone() == Zone.INPUTS) {
                if (linkHasBeenAdded || linkHasBeenRemoved) {
                    checkTargetInputKey(currentModifiedITableEntry, checkInputKeyAutomatically, inputExpressionAppliedOrCanceled);
                }
                if (inputExpressionAppliedOrCanceled) {
                    openChangeKeysDialog((InputDataMapTableView) dataMapTableView);
                }
            }
        }

        return new ParseExpressionResult(linkHasBeenAdded, linkHasBeenRemoved);
    }



    public ParseExpressionResult parseFilterColumn(String expression, ITableEntry currentModifiedITableEntry,
            boolean linkMustHaveSelectedState, boolean checkInputKeyAutomatically, boolean inputExpressionAppliedOrCanceled) {

        if (currentModifiedITableEntry instanceof InputColumnTableEntry) {
            InputColumnTableEntry entry = (InputColumnTableEntry) currentModifiedITableEntry;
            if (StringUtils.trimToNull(expression) == null) {
                entry.setOperator(null);
            }
        }

        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(currentModifiedITableEntry);
        boolean linkHasBeenAdded = false;
        boolean linkHasBeenRemoved = false;

        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(LanguageProvider.getCurrentLanguage());
        TableEntryLocation[] tableEntriesLocationsSources = dataMapExpressionParser.parseTableEntryLocations(expression);
        Set<TableEntryLocation> alreadyProcessed = new HashSet<TableEntryLocation>();
        Set<ITableEntry> sourcesForTarget = mapperManager.getSourcesForTarget(currentModifiedITableEntry);
        Set<ITableEntry> sourcesForTargetToDelete = new HashSet<ITableEntry>(sourcesForTarget);

        boolean isInputEntry = currentModifiedITableEntry instanceof InputColumnTableEntry;

        ECodeLanguage codeLanguage = LanguageProvider.getCurrentLanguage().getCodeLanguage();

        for (TableEntryLocation tableEntriesLocationsSource : tableEntriesLocationsSources) {
            TableEntryLocation location = tableEntriesLocationsSource;

            // tests to know if link must be removed if key is unchecked
            boolean dontRemoveLink = (!isInputEntry || isInputEntry
                    && (inputExpressionAppliedOrCanceled || !inputExpressionAppliedOrCanceled
                            && !mapperManager
                                    .checkEntryHasInvalidUncheckedKey((InputColumnTableEntry) currentModifiedITableEntry)));

            if (!alreadyProcessed.contains(location) && checkSourceLocationIsValid(location, currentModifiedITableEntry)
                    && (mapperManager.isAdvancedMap() || !mapperManager.isAdvancedMap() && dontRemoveLink)) {
                ITableEntry sourceTableEntry = mapperManager.retrieveTableEntry(location);
                    if (sourceTableEntry != null && sourcesForTarget.contains(sourceTableEntry)){
                        Set<IMapperLink> targets = mapperManager.getGraphicalLinksFromTarget(currentModifiedITableEntry);
                        Set<IMapperLink> linksFromTarget = new HashSet<IMapperLink>(targets);
                        for (IMapperLink link : linksFromTarget) {
                                link.calculate();
                        }
                }
            }
        }

        mapperManager.orderLinks();

        if (!mapperManager.isAdvancedMap()) {
            if (dataMapTableView.getZone() == Zone.INPUTS) {
                if (linkHasBeenAdded || linkHasBeenRemoved) {
                    checkTargetInputKey(currentModifiedITableEntry, checkInputKeyAutomatically, inputExpressionAppliedOrCanceled);
                }
                if (inputExpressionAppliedOrCanceled) {
                    openChangeKeysDialog((InputDataMapTableView) dataMapTableView);
                }
            }
        }

        return new ParseExpressionResult(linkHasBeenAdded, linkHasBeenRemoved);
    }


    /**
     * DOC amaumont Comment method "removeInvalidKeys".
     *
     * @param newSelectedDataMapTableView
     */
    private void removeInvalidInputKeys(InputDataMapTableView inputDataMapTableView) {
        List<IColumnEntry> targetTableEntries = inputDataMapTableView.getDataMapTable().getColumnEntries();
        for (IColumnEntry entry : targetTableEntries) {
            InputColumnTableEntry inputEntry = (InputColumnTableEntry) entry;
            if (mapperManager.checkEntryHasInvalidCheckedKey(inputEntry)) {
                inputEntry.getMetadataColumn().setKey(false);
            }
        }
    }

    /**
     *
     * DOC amaumont Comment method "hasInvalidInputKeys".
     *
     * @param newSelectedDataMapTableView
     * @return
     */
    private boolean hasInvalidInputExpressionKeys(InputDataMapTableView inputDataMapTableView) {

        if (inputDataMapTableView.getTableViewerCreatorForColumns() == null) {
            return false;
        }

        List<IColumnEntry> targetTableEntries = inputDataMapTableView.getDataMapTable().getColumnEntries();
        for (IColumnEntry entry : targetTableEntries) {
            InputColumnTableEntry inputEntry = (InputColumnTableEntry) entry;
            if (mapperManager.checkEntryHasInvalidCheckedKey(inputEntry)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * DOC amaumont Comment method "checkTargetInputKey".
     *
     * @param currentModifiedTableEntry
     * @param inputDataMapTableView
     * @param checkInputKeyAutomatically
     * @param appliedOrCanceled TODO
     */
    private void checkTargetInputKey(ITableEntry currentModifiedTableEntry, boolean checkInputKeyAutomatically,
            boolean appliedOrCanceled) {
        // check key
        if (checkInputKeyAutomatically && currentModifiedTableEntry instanceof InputColumnTableEntry) {

            IMetadataColumn metadataColumn = ((InputColumnTableEntry) currentModifiedTableEntry).getMetadataColumn();
            if (!metadataColumn.isKey()) {
                metadataColumn.setKey(true);
                refreshInOutTableAndMetaTable((AbstractInOutTableEntry) currentModifiedTableEntry);
            }
        }
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
     * @param text
     * @param entry
     */
    public void processColumnNameChanged(final String previousColumnName, final String newColumnName,
            final DataMapTableView dataMapTableView, final ITableEntry currentModifiedITableEntry) {

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
            // for the input/output table expression filter
            if (dataMapTable instanceof AbstractInOutTable) {
                ExpressionFilterEntry expressionFilterEntry = ((AbstractInOutTable) dataMapTable).getExpressionFilter();
                if (expressionFilterEntry != null && expressionFilterEntry.getExpression() != null) {
                    if (parseExpression(expressionFilterEntry.getExpression(), expressionFilterEntry, true, true, false)
                            .isAtLeastOneLinkRemoved()) {
                        atLeastOneLinkHasBeenRemoved = true;
                    }
                }
            }
        }
        mapperManager.getUiManager().refreshBackground(false, false);
        dataMapTableView.getTableViewerCreatorForColumns().getTableViewer().refresh(currentModifiedITableEntry);

        if (atLeastOneLinkHasBeenRemoved) {
            new AsynchronousThreading(20, false, dataMapTableView.getDisplay(), new Runnable() {

                public void run() {

                    TableViewerCreator tableViewerCreatorForColumns = dataMapTableView.getTableViewerCreatorForColumns();
                    boolean propagate = MessageDialog.openQuestion(tableViewerCreatorForColumns.getTable().getShell(),
                            Messages.getString("UIManager.propagateTitle"), //$NON-NLS-1$
                            Messages.getString("UIManager.propagateMessage")); //$NON-NLS-1$
                    if (propagate) {
                        TableEntryLocation previousLocation = new TableEntryLocation(currentModifiedITableEntry.getParentName(),
                                previousColumnName);
                        TableEntryLocation newLocation = new TableEntryLocation(currentModifiedITableEntry.getParentName(),
                                newColumnName);
                        mapperManager.replacePreviousLocationInAllExpressions(previousLocation, newLocation);
                    }
                }
            }).start();
        }
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
        dataMapTableView.loaded();
        this.mapperUI.getOutputMouseSrolledListener().addMouseWheelListener(dataMapTableView);
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

    public ScrolledComposite getScrolledCompositeViewVars() {
        return this.mapperUI.getScrolledCompositeViewVars();
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

    public ExternalMapperUiProperties getUiProperties() {
        if (this.uiProperties == null) {
            this.uiProperties = new ExternalMapperUiProperties();
        }
        return this.uiProperties;
    }

    public void setUiProperties(ExternalMapperUiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    public OutputDataMapTableView getCurrentSelectedOutputTableView() {
        return this.currentSelectedOutputTableView;
    }

    public InputDataMapTableView getCurrentSelectedInputTableView() {
        return this.currentSelectedInputTableView;
    }

    public VarsDataMapTableView getCurrentSelectedVarsTableView() {
        return this.currentSelectedVarsTableView;
    }

    public TabFolderEditors getTabFolderEditors() {
        return this.mapperUI.getTabFolderEditors();
    }

    /**
     * DOC amaumont Comment method "openAddNewOutputDialog".
     *
     * return tableName--joinTableName if creating join table
     */
    public String openNewOutputCreationDialog() {
        final IProcess process = mapperManager.getAbstractMapComponent().getProcess();
        String outputName = process.generateUniqueConnectionName("out"); //$NON-NLS-1$
        //        InputDialog id = new InputDialog(getMapperContainer().getShell(), Messages.getString("UIManager.addNewOutputTable"), //$NON-NLS-1$
        //                Messages.getString("UIManager.typeTableName"), outputName, new IInputValidator() { //$NON-NLS-1$
        //
        // public String isValid(String newText) {
        // if (!process.checkValidConnectionName(newText)) {
        //                            return Messages.getString("UIManager.tableNameIsNotValid"); //$NON-NLS-1$
        // }
        // return null;
        // }
        //
        // });
        // int response = id.open();
        // if (response == InputDialog.OK) {
        // return id.getValue();
        // }

        OutputAddDialog dialog = new OutputAddDialog(getShell(), mapperManager, outputName);
        if (Window.OK == dialog.open()) {
            String tableName = "";
            tableName = dialog.getTableName();
            if (dialog.isCreatingJoinTable()) {
                tableName = tableName + NAME_SEPARATOR + dialog.getJoinTableName();
            }
            return tableName;
        }

        return null;
    }

    /**
     * DOC amaumont Comment method "openAddNewOutputDialog".
     *
     * @param inputDataMapTableView
     */
    private void openChangeKeysDialog(final InputDataMapTableView inputDataMapTableView) {

        new AsynchronousThreading(50, false, inputDataMapTableView.getDisplay(), new Runnable() {

            public void run() {

                if (hasInvalidInputExpressionKeys(inputDataMapTableView)) {
                    if (MessageDialog.openConfirm(inputDataMapTableView.getShell(),
                            Messages.getString("UIManager.removeInvalidKeys"), //$NON-NLS-1$
                            Messages.getString("UIManager.comfirmToRemoveTableKeys") + inputDataMapTableView.getDataMapTable().getName() //$NON-NLS-1$
                                    + "'")) { //$NON-NLS-1$
                        removeInvalidInputKeys(inputDataMapTableView);
                    }
                    refreshInOutTableAndMetaTable(inputDataMapTableView);
                }

            }
        }).start();

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
        } else if (zone == Zone.VARS) {
            return getTablesZoneViewVars();
        } else {
            throw new IllegalArgumentException("Case not found"); //$NON-NLS-1$
        }
    }

    /**
     * DOC amaumont Comment method "getTablesZoneView".
     *
     * @param dataMapTableViewTarget
     */
    public ScrolledComposite getScrolledZoneView(DataMapTableView dataMapTableViewTarget) {
        Zone zone = dataMapTableViewTarget.getZone();
        if (zone == Zone.OUTPUTS) {
            return getScrolledCompositeViewOutputs();
        } else if (zone == Zone.INPUTS) {
            return getScrolledCompositeViewInputs();
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

    public void setPositionOfVerticalScrollBarZone(ScrolledComposite scrollComposite, int scrollBarSelection) {
        ScrollBar verticalBar = scrollComposite.getVerticalBar();
        verticalBar.setSelection(scrollBarSelection);
        scrollComposite.setOrigin(0, scrollBarSelection);
    }

    /**
     * DOC amaumont Comment method "getDisplay".
     */
    public Display getDisplay() {
        return display;
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
            List<DataMapTableView> inputsTablesView = mapperManager.getUiManager().getInputsTablesView();
            if (moveUp) {
                moveSelectTableUp(currentSelectedInputTableView, inputsTablesView, 2);
            } else {
                moveSelectedTableDown(currentSelectedInputTableView, inputsTablesView, 1);
            }
            moveInputScrollBarZoneAtSelectedTable();
        } else if (zone == Zone.OUTPUTS) {
            List<DataMapTableView> outputsTablesView = mapperManager.getUiManager().getOutputsTablesView();
            if (moveUp) {
                moveSelectTableUp(currentSelectedOutputTableView, outputsTablesView, 1);
            } else {
                moveSelectedTableDown(currentSelectedOutputTableView, outputsTablesView, 0);
            }
            moveOutputScrollBarZoneAtSelectedTable();
        }
        updateToolbarButtonsStates(zone);
        refreshBackground(true, false);
    }

    @Override
    public void openPropertySetDialog() {
        if (mapperManager.isBigDataProcess()) {
            new MapReducePropertySetDialog(getShell(), mapperManager).open();
        } else {
            new PropertySetDialog(getShell(), mapperManager).open();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.managers.AbstractUIManager#openAutoMapDialog()
     */
    @Override
    public void openAutoMappingDialog() {
        new AutoMappingDialog(getShell(), mapperManager).open();
    }

    public boolean isTableViewMoveable(Zone zone, boolean moveUp) {
        if (zone == Zone.INPUTS) {
            if (currentSelectedInputTableView == null) {
                return false;
            }
            List<DataMapTableView> tablesView = mapperManager.getUiManager().getInputsTablesView();
            int indexCurrentTable = tablesView.indexOf(currentSelectedInputTableView);
            if (moveUp) {
                if (indexCurrentTable > 1) {
                    return true;
                }
                return false;
            } else {
                if (indexCurrentTable < tablesView.size() - 1 && indexCurrentTable > 0) {
                    return true;
                }
                return false;
            }
        } else if (zone == Zone.OUTPUTS) {
            if (currentSelectedOutputTableView == null) {
                return false;
            }
            List<DataMapTableView> tablesView = mapperManager.getUiManager().getOutputsTablesView();
            int indexCurrentTable = tablesView.indexOf(currentSelectedOutputTableView);
            if (moveUp) {
                if (indexCurrentTable > 0) {
                    // TDI-28187:join table should always under its host table
                    OutputDataMapTableView previousOutTable = (OutputDataMapTableView) tablesView.get(indexCurrentTable - 1);
                    String joinTableName = currentSelectedOutputTableView.getOutputTable().getIsJoinTableOf();
                    if (joinTableName != null && joinTableName.equals(previousOutTable.getOutputTable().getName())) {
                        return false;
                    }
                    return true;
                }
                return false;
            } else {
                if (indexCurrentTable < tablesView.size() - 1) {
                    OutputDataMapTableView nextOutTable = (OutputDataMapTableView) tablesView.get(indexCurrentTable + 1);
                    String joinTableName = nextOutTable.getOutputTable().getIsJoinTableOf();
                    if (joinTableName != null && joinTableName.equals(currentSelectedOutputTableView.getOutputTable().getName())) {
                        return false;
                    }
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

        checkProblemsForMovedTables(currentSelectedTableView, previousTableView);
    }

    private void moveSelectedTableDown(final DataMapTableView currentSelectedTableView, List<DataMapTableView> tablesView,
            int indexStartMovedAuthorized) {
        int indexCurrentTable = tablesView.indexOf(currentSelectedTableView);

        if (indexCurrentTable < indexStartMovedAuthorized || indexCurrentTable == tablesView.size() - 1) {
            return;
        }
        final DataMapTableView nextTableView = tablesView.get(indexCurrentTable + 1);

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

        checkProblemsForMovedTables(currentSelectedTableView, nextTableView);
    }

    /**
     * DOC amaumont Comment method "checkProblemsForMovedTables".
     *
     * @param currentSelectedTableView
     * @param nextTableView
     */
    private void checkProblemsForMovedTables(final DataMapTableView currentSelectedTableView, final DataMapTableView nextTableView) {
        new Thread() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                display.asyncExec(new Runnable() {

                    public void run() {
                        mapperManager.getProblemsManager().checkProblemsForAllEntries(currentSelectedTableView, true);
                        mapperManager.getProblemsManager().checkProblemsForAllEntries(nextTableView, false);
                    }

                });
            }

        }.start();
    }

    public void minimizeAllTables(Zone zone, boolean minimize, ToolItem minimizeButton) {

        List<DataMapTableView> tablesView = null;
        TablesZoneView tablesZoneView = null;
        if (zone == Zone.INPUTS) {
            tablesView = mapperManager.getUiManager().getInputsTablesView();
            tablesZoneView = getTablesZoneViewInputs();
        } else if (zone == Zone.OUTPUTS) {
            tablesZoneView = getTablesZoneViewOutputs();
            tablesView = mapperManager.getUiManager().getOutputsTablesView();
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
        } else if (zone == Zone.VARS) {
            tablesZoneView = getTablesZoneViewVars();
        }
        tablesZoneView.setSize(tablesZoneView.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Call mapperManager.removeSelectedOutput() to remove a table view.
     *
     * @param dataMapTableViewToRemove
     */
    public void removeOutputTableView(DataMapTableView dataMapTableViewToRemove) {
        List<DataMapTableView> outputsTablesView = mapperManager.getUiManager().getOutputsTablesView();
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
        if (outputMetaEditorView.getMetadataTableEditor() != null
                && outputMetaEditorView.getMetadataTableEditor().getMetadataTable() == ((OutputTable) dataMapTableViewToRemove
                        .getDataMapTable()).getMetadataTable()) {
            getOutputMetaEditorView().setMetadataTableEditor(null);
        }
        dataMapTableViewToRemove.dispose();
        dataMapTableViewToRemove = null;
        getTablesZoneViewOutputs().layout();
        refreshBackground(true, false);
        this.currentSelectedOutputTableView = null;
    }

    public void registerCustomPaint() {
        List<DataMapTableView> tablesView = mapperManager.getUiManager().getOutputsTablesView();
        tablesView.addAll(mapperManager.getUiManager().getInputsTablesView());
        for (DataMapTableView view : tablesView) {
            view.getTableViewerCreatorForColumns().setUseCustomItemColoring(false);
        }

    }

    public void unregisterCustomPaint() {
        List<DataMapTableView> tablesView = mapperManager.getUiManager().getOutputsTablesView();
        tablesView.addAll(mapperManager.getUiManager().getInputsTablesView());
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
        List<DataMapTableView> tablesView = mapperManager.getUiManager().getOutputsTablesView();
        tablesView.addAll(mapperManager.getUiManager().getInputsTablesView());
        tablesView.addAll(mapperManager.getUiManager().getVarsTablesView());
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

    public List<DataMapTableView> getVarsTablesView() {
        return tableManager.getVarsTablesView();
    }

    public List<DataMapTableView> getOutputsTablesView() {
        return tableManager.getOutputsTablesView();
    }

    public List<DataMapTableView> getInputsTablesView() {
        return tableManager.getInputsTablesView();
    }

    public List<DataMapTableView> getRelatedOutputsTableView(DataMapTableView view) {
        List<DataMapTableView> joinTableViews = new ArrayList<DataMapTableView>();
        if (view.getDataMapTable() instanceof OutputTable) {
            OutputTable outputTable = (OutputTable) view.getDataMapTable();
            String originalTalbeName = "";
            if (outputTable.getIsJoinTableOf() == null) {
                originalTalbeName = outputTable.getName();
            } else {
                originalTalbeName = outputTable.getIsJoinTableOf();
            }
            for (DataMapTableView outputView : getOutputsTablesView()) {
                if (outputView.getDataMapTable() instanceof OutputTable) {
                    OutputTable table = (OutputTable) outputView.getDataMapTable();
                    if ((table.getName().equals(originalTalbeName) || originalTalbeName.equals(table.getIsJoinTableOf()))
                            && outputView != view) {
                        joinTableViews.add(outputView);
                    }
                }
            }
        }
        return joinTableViews;
    }

    /**
     * DOC amaumont Comment method "createUI".
     *
     * @param parent
     * @param mapperModel
     */
    public void createUI(Composite parent, MapperModel mapperModel) {
        this.display = parent.getDisplay();
        mapperUI = new MapperUI(parent, mapperManager);
        mapperUI.createUI(mapperModel);
    }

    /**
     * DOC amaumont Comment method "createUI".
     *
     * @param display
     * @param mapperModel
     * @return
     */
    public Shell createUI(Display display, MapperModel mapperModel) {
        this.display = display;
        mapperUI = new MapperUI(display, mapperManager);
        mapperUI.createUI(mapperModel);
        return mapperUI.getShell();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.managers.AbstractUIManager#getAbstractMapperManager()
     */
    @Override
    public AbstractMapperManager getAbstractMapperManager() {
        return mapperManager;
    }

    /**
     * @return
     * @see org.talend.designer.mapper.ui.MapperUI#getDatasFlowViewSashForm()
     */
    public SashForm getDatasFlowViewSashForm() {
        return this.mapperUI.getDatasFlowViewSashForm();
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

    public boolean checkSourceLocationIsValid(ITableEntry entrySource, ITableEntry entryTarget) {

        if (entrySource instanceof VarTableEntry && entrySource.getParent() == entryTarget.getParent()) {
            List<IColumnEntry> columnEntries = entrySource.getParent().getColumnEntries();
            if (columnEntries.indexOf(entrySource) < columnEntries.indexOf(entryTarget)) {
                return true;
            }
        } else if (entrySource instanceof InputColumnTableEntry
                && ((entryTarget instanceof InputColumnTableEntry && entrySource.getParent() != entryTarget.getParent()) || entryTarget
                        .getParent() instanceof InputTable
                        && (entryTarget instanceof ExpressionFilterEntry || entryTarget instanceof GlobalMapEntry))

        ) {
            List<InputTable> inputTables = mapperManager.getInputTables();
            int indexTableSource = inputTables.indexOf(entrySource.getParent());
            int indexTableTarget = inputTables.indexOf(entryTarget.getParent());
            if (indexTableSource < indexTableTarget || entryTarget instanceof ExpressionFilterEntry
                    && indexTableSource <= indexTableTarget) {
                return true;
            }
        } else if ((entryTarget instanceof VarTableEntry || entryTarget instanceof OutputColumnTableEntry
                || entryTarget instanceof FilterTableEntry || entryTarget instanceof ExpressionFilterEntry

        && entryTarget.getParent() instanceof OutputTable)) {
            if (entrySource instanceof InputColumnTableEntry || entrySource instanceof VarTableEntry) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for closeWithoutPrompt.
     *
     * @return the closeWithoutPrompt
     */
    public boolean isCloseWithoutPrompt() {
        return this.closeWithoutPrompt;
    }

    public void applyActivatedCellEditors(TableViewerCreator tableViewerCreator) {
        if (tableViewerCreator != null) {
            tableViewerCreator.applyActivatedCellEditor();
        }

    }

    /**
     *
     * "applyActivatedCellEditorsForAllTables".
     *
     * @param exceptThisTableViewerCreator
     */
    public void applyActivatedCellEditorsForAllTables(TableViewerCreator exceptThisTableViewerCreator) {

        Collection<DataMapTableView> tablesView = mapperManager.getTablesView();

        for (DataMapTableView dataMapTableView : tablesView) {

            TableViewerCreator tableViewerCreatorForColumns = dataMapTableView.getTableViewerCreatorForColumns();
            if (tableViewerCreatorForColumns != exceptThisTableViewerCreator) {
                applyActivatedCellEditors(tableViewerCreatorForColumns);
            }

            TableViewerCreator tableViewerCreatorForGlobalMap = dataMapTableView.getTableViewerCreatorForGlobalMap();
            if (tableViewerCreatorForGlobalMap != null && tableViewerCreatorForGlobalMap != exceptThisTableViewerCreator) {
                applyActivatedCellEditors(tableViewerCreatorForGlobalMap);
            }

        }

    }

    public void setStatusBarValues(STATUS status, String text) {
        StatusBar statusBar = mapperUI.getStatusBar();
        if (statusBar != null) {
            statusBar.setValues(status, text);
        }
    }

    public Shell getShell() {
        return mapperUI.getShell();
    }

    public Map<String, String> getOldMappingMap() {
        return oldMappingMap;
    }

}
