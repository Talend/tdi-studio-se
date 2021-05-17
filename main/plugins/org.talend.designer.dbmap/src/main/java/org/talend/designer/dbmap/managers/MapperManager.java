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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.designer.abstractmap.managers.AbstractMapperManager;
import org.talend.designer.abstractmap.managers.ILinkManager;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.abstractmap.ui.visualmap.link.LinkState;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.PluginUtils;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.model.table.AbstractDataMapTable;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.table.VarsTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.ui.automap.AutoMapper;
import org.talend.designer.dbmap.ui.dialog.AliasDialog;
import org.talend.designer.dbmap.ui.visualmap.TableEntryProperties;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.table.EntryState;
import org.talend.designer.dbmap.ui.visualmap.table.InputDataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.table.OutputDataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.TablesZoneView;
import org.talend.designer.dbmap.utils.DataMapExpressionParser;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public class MapperManager extends AbstractMapperManager {

    public static final String MAPPER_MODEL_DATA = "MAPPER_MODEL_DATA"; //$NON-NLS-1$

    private TableEntriesManager tableEntriesManager;

    private TableManager tableManager;

    private LinkManager linkManager;

    UIManager uiManager;

    private DbMapComponent mapperComponent;

    private CommandStack commandStack;

    private ProblemsManager problemsManager;

    public MapperManager(DbMapComponent mapperComponent) {
        super(mapperComponent);
        this.mapperComponent = mapperComponent;
        tableEntriesManager = new TableEntriesManager(this);
        tableManager = new TableManager(this);
        linkManager = new LinkManager();
        problemsManager = new ProblemsManager(this);
    }

    public void addTablePair(DataMapTableView view, IDataMapTable data) {
        tableManager.addTable(view, data);
        tableEntriesManager.addAll(data.getColumnEntries());
        if (data instanceof OutputTable) {
            tableEntriesManager.addAll(((OutputTable) data).getWhereFilterEntries());
            tableEntriesManager.addAll(((OutputTable) data).getOtherFilterEntries());
        }
    }

    /**
     *
     * Remove the <code>DataMapTableView</code>-<code>DataMapTable</code> pair.
     *
     * @param view
     */
    public void removeTablePair(DataMapTableView view) {
        IDataMapTable dataTable = tableManager.getData(view);
        removeTablePair(tableManager.getData(view));
    }

    /**
     *
     * Remove the <code>DataMapTable</code>-<code>DataMapTableView</code> pair.
     *
     * @param view
     */
    public void removeTablePair(IDataMapTable dataTable) {
        tableManager.removeTable(dataTable);
    }

    /**
     * DOC amaumont Comment method "getDataMapTable".
     */
    public IDataMapTable retrieveIDataMapTable(DataMapTableView dataMapTableView) {
        return tableManager.getData(dataMapTableView);
    }

    /**
     * DOC amaumont Comment method "getDataMapTableView".
     */
    public DataMapTableView retrieveIDataMapTableView(IDataMapTable abstractDataMapTable) {
        return tableManager.getView(abstractDataMapTable);
    }

    public ITableEntry retrieveTableEntry(TableEntryLocation location) {
        return tableEntriesManager.retrieveTableEntry(location);
    }

    public DataMapTableView retrieveDataMapTableView(ITableEntry dataMapTableEntry) {
        return tableManager.getView(dataMapTableEntry.getParent());
    }

    /**
     * DOC amaumont Comment method "getDataMapTable".
     */
    public DataMapTableView retrieveDataMapTableView(TableEntryLocation names) {
        ITableEntry dataMapTableEntry = retrieveTableEntry(names);
        if (dataMapTableEntry == null) {
            return null;
        }
        return retrieveDataMapTableView(dataMapTableEntry);
    }

    public DataMapTableView retrieveDataMapTableView(Table swtTable) {
        return this.tableManager.getView(swtTable);
    }

    /**
     * DOC amaumont Comment method "getDataMapTable".
     */
    public IDataMapTable retrieveIDataMapTable(TableEntryLocation names) {
        ITableEntry dataMapTableEntry = retrieveTableEntry(names);
        if (dataMapTableEntry == null) {
            return null;
        }
        return dataMapTableEntry.getParent();
    }

    /**
     * DOC amaumont Comment method "clearLinks".
     */
    public void clearLinks() {
        linkManager.clearLinks();

    }

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param link
     */
    public void addLink(IMapperLink link) {
        linkManager.addLink(link);
        changeDependentSourcesAndTargetEntriesState(link.getPointLinkDescriptor2().getTableEntry(), link, false);

        if (link.getPointLinkDescriptor2().getTableEntry() instanceof InputColumnTableEntry
                && linkManager.getCountOfInputLevels() > 4) {
            uiManager.enlargeLeftMarginForInputTables(linkManager.getCountOfInputLevels());
        }

    }

    /**
     * DOC amaumont Comment method "removeLink".
     *
     * @param link
     */
    public void removeLink(IMapperLink link, ITableEntry entryCauseOfRemove) {
        changeDependentSourcesAndTargetEntriesState(entryCauseOfRemove, link, true);
        linkManager.removeLink(link);
    }

    /**
     *
     * DOC amaumont Comment method "changeDependentSourcesAndTargetEntriesState".
     *
     * @param entryCauseOfChange
     * @param currentLink
     * @param removedLink
     */
    private void changeDependentSourcesAndTargetEntriesState(ITableEntry entryCauseOfChange, IMapperLink currentLink,
            boolean removedLink) {

        boolean sourceIsCauseOfChange = false;
        if (currentLink.getPointLinkDescriptor1().getTableEntry() == entryCauseOfChange) {
            sourceIsCauseOfChange = true;
        } else if (currentLink.getPointLinkDescriptor2().getTableEntry() == entryCauseOfChange) {
            sourceIsCauseOfChange = false;
        } else {
            throw new IllegalArgumentException(Messages.getString("MapperManager.exceptionMessage.mustBeSourceOrTarget")); //$NON-NLS-1$
        }

        if (sourceIsCauseOfChange) {
            Set<IMapperLink> dependentLinks = linkManager.getLinksFromSource(entryCauseOfChange);
            for (IMapperLink dependentLink : dependentLinks) {
                changeDependentEntriesState(currentLink, dependentLink.getPointLinkDescriptor2().getTableEntry(), removedLink);
            }
        } else {
            Set<IMapperLink> dependentLinks = linkManager.getLinksFromTarget(entryCauseOfChange);
            for (IMapperLink dependentLink : dependentLinks) {
                changeDependentEntriesState(currentLink, dependentLink.getPointLinkDescriptor1().getTableEntry(), removedLink);
            }
        }
        changeDependentEntriesState(currentLink, entryCauseOfChange, removedLink);
    }

    /**
     *
     * DOC amaumont Comment method "changeDependentEntriesState".
     *
     * @param link
     * @param currentEntry
     * @param removedLink
     */
    private void changeDependentEntriesState(IMapperLink link, ITableEntry currentEntry, boolean removedLink) {
        Set<IMapperLink> dependentLinks = linkManager.getLinksFromSource(currentEntry);
        dependentLinks.addAll(linkManager.getLinksFromTarget(currentEntry));
        boolean hasSelectedLink = false;
        for (IMapperLink dependentLink : dependentLinks) {
            if (dependentLink.getState() == LinkState.SELECTED && dependentLink != link) {
                hasSelectedLink = true;
                break;
            }
        }
        if (!hasSelectedLink && link.getState() == LinkState.UNSELECTED || removedLink) {
            uiManager.setEntryState(this, EntryState.NONE, currentEntry);
        } else {
            uiManager.setEntryState(this, EntryState.HIGHLIGHT, currentEntry);
        }
    }

    /**
     * DOC amaumont Comment method "removeLink".
     *
     * @param link
     */
    public Set<ITableEntry> getSourcesForTarget(ITableEntry dataMapTableEntry) {
        return linkManager.getSourcesForTarget(dataMapTableEntry);
    }

    /**
     * DOC amaumont Comment method "removeLink".
     *
     * @param link
     */
    public Set<IMapperLink> getGraphicalLinksFromSource(ITableEntry dataMapTableEntry) {
        return linkManager.getLinksFromSource(dataMapTableEntry);
    }

    public Set<IMapperLink> getGraphicalLinksFromTarget(ITableEntry dataMapTableEntry) {
        return linkManager.getLinksFromTarget(dataMapTableEntry);
    }

    public List<IMapperLink> getLinks() {
        return linkManager.getLinks();
    }

    /**
     * DOC amaumont Comment method "retrieveTableFromTableEntry".
     *
     * @param dataMapTableEntry
     * @return
     */
    public Table retrieveTable(ITableEntry dataMapTableEntry) {
        return tableEntriesManager.retrieveTable(dataMapTableEntry);
    }

    public TableItem retrieveTableItem(ITableEntry dataMapTableEntry) {
        return tableEntriesManager.retrieveTableItem(dataMapTableEntry);
    }

    public Collection<DataMapTableView> getTablesView() {
        return tableManager.getTablesView();
    }

    /**
     * Return all table data. Order is not assured. DOC amaumont Comment method "getTablesData".
     *
     * @return
     */
    public Collection<IDataMapTable> getTablesData() {
        return tableManager.getTablesData();
    }

    public List<InputTable> getInputTables() {
        return this.tableManager.getInputTables();
    }

    public List<OutputTable> getOutputTables() {
        return this.tableManager.getOutputTables();
    }

    public List<VarsTable> getVarsTables() {
        return this.tableManager.getVarsTables();
    }

    /**
     * Update the column in the tableEntriesManager.
     *
     * @param currentModifiedTableEntry
     * @param newColumnName
     * @param newColumnName2
     */
    public void changeColumnName(ITableEntry dataMapTableEntry, String previousColumnName, String newColumnName) {
        tableEntriesManager.renameEntryName(dataMapTableEntry, previousColumnName, newColumnName);
    }

    /**
     *
     * Remove the entry from the tableEntriesManager.
     *
     * @param dataMapTableEntry
     */
    public void removeTableEntry(ITableEntry dataMapTableEntry) {
        tableEntriesManager.remove(dataMapTableEntry, false);
    }

    /**
     * Remove links from the given entry in the linkManager.
     *
     * @param dataMapTableEntry
     */
    public void removeLinksOf(ITableEntry dataMapTableEntry) {
        Set<IMapperLink> links = linkManager.getLinksFromSource(dataMapTableEntry);
        links.addAll(linkManager.getLinksFromTarget(dataMapTableEntry));
        for (IMapperLink link : links) {
            removeLink(link, dataMapTableEntry);
        }
    }

    @Override
    public UIManager getUiManager() {
        if (this.uiManager == null) {
            uiManager = new UIManager(this, tableManager);
        }
        return this.uiManager;
    }

    protected TableManager getTableManager() {
        return this.tableManager;
    }

    /**
     * DOC amaumont Comment method "getTableEntryProperties".
     *
     * @param dataMapTableEntry
     * @return
     */
    public TableEntryProperties getTableEntryProperties(ITableEntry dataMapTableEntry) {
        return tableEntriesManager.getTableEntryProperties(dataMapTableEntry);
    }

    /**
     * This method is called when "addMetadataTableEditorEntry" is called (event on list of MetadataEditor) , so if you
     * want keep synchronisation between inputs/outputs DataMaps and MetadataEditors don't call this method.
     *
     * @param dataMapTableView
     * @param metadataColumn, can be null if added in VarsTable
     * @param index
     */
    public IColumnEntry addNewColumnEntry(DataMapTableView dataMapTableView, IMetadataColumn metadataColumn, Integer index) {
        IDataMapTable abstractDataMapTable = dataMapTableView.getDataMapTable();
        IColumnEntry dataMapTableEntry = null;
        if (dataMapTableView.getZone() == Zone.INPUTS) {
            dataMapTableEntry = new InputColumnTableEntry(abstractDataMapTable, metadataColumn);
        } else if (dataMapTableView.getZone() == Zone.OUTPUTS) {
            dataMapTableEntry = new OutputColumnTableEntry(abstractDataMapTable, metadataColumn);
        } else {
            throw new IllegalArgumentException(Messages.getString("MapperManager.exceptionMessage.useOtherSignature")); //$NON-NLS-1$
        }
        tableEntriesManager.addTableEntry(dataMapTableEntry, index);
        return dataMapTableEntry;
    }

    /**
     * DOC amaumont Comment method "addTableEntry".
     *
     * @param dataMapTableEntry
     * @param index
     */
    public void addMetadataTableEditorEntry(MetadataTableEditorView metadataTableEditorView, IMetadataColumn metadataColumn,
            Integer index) {
        MetadataTableEditor metadataTableEditor = metadataTableEditorView.getMetadataTableEditor();
        metadataTableEditor.add(metadataColumn, index);
    }

    public FilterTableEntry addNewFilterEntry(DataMapTableView dataMapTableView, String name, Integer index) {
        return addNewFilterEntry(dataMapTableView, name, index, FilterTableEntry.WHERE_FILTER);
    }

    public FilterTableEntry addNewFilterEntry(DataMapTableView dataMapTableView, String name, Integer index, String filterKind) {
        IDataMapTable abstractDataMapTable = dataMapTableView.getDataMapTable();
        FilterTableEntry constraintEntry = new FilterTableEntry(abstractDataMapTable, name, null);
        constraintEntry.setFilterKind(filterKind);
        tableEntriesManager.addTableEntry(constraintEntry, index);
        return constraintEntry;
    }

    /**
     * DOC amaumont Comment method "addOutput".
     */
    public void addOutput() {

        String tableName = uiManager.openNewOutputCreationDialog();
        if (tableName == null) {
            return;
        }

        IProcess process = mapperComponent.getProcess();
        String uniqueName = process.generateUniqueConnectionName("table"); //$NON-NLS-1$
        process.addUniqueConnectionName(uniqueName);

        MetadataTable metadataTable = new MetadataTable();
        metadataTable.setTableName(uniqueName);
        // metadataTable.setId(uniqueName);
        metadataTable.setLabel(tableName);
        Object dbmsValue = getElementParameterValue(EParameterName.MAPPING.getName());
        if (dbmsValue != null && dbmsValue instanceof String) {
            metadataTable.setDbms((String) dbmsValue);
        }

        List<DataMapTableView> outputsTablesView = getUiManager().getOutputsTablesView();
        int sizeOutputsView = outputsTablesView.size();
        Control lastChild = null;
        if (sizeOutputsView - 1 >= 0) {
            lastChild = outputsTablesView.get(sizeOutputsView - 1);
        }

        IDataMapTable abstractDataMapTable = new OutputTable(this, metadataTable, uniqueName, tableName);

        TablesZoneView tablesZoneViewOutputs = uiManager.getTablesZoneViewOutputs();
        DataMapTableView dataMapTableView = uiManager.createNewOutputTableView(lastChild, abstractDataMapTable,
                tablesZoneViewOutputs);
        tablesZoneViewOutputs.setSize(tablesZoneViewOutputs.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        tablesZoneViewOutputs.layout();
        uiManager.moveOutputScrollBarZoneToMax();
        uiManager.refreshBackground(true, false);
        tablesZoneViewOutputs.layout();
        uiManager.selectDataMapTableView(dataMapTableView, true, false);
    }

    public void removeSelectedAliasInputTable() {
        InputDataMapTableView currentSelectedDataMapTableView = uiManager.getCurrentSelectedInputTableView();

        if (currentSelectedDataMapTableView != null) {
            String tableName = currentSelectedDataMapTableView.getDataMapTable().getName();
            if (MessageDialog.openConfirm(currentSelectedDataMapTableView.getShell(),
                    Messages.getString("MapperManager.removeInputTableTitle"), //$NON-NLS-1$
                    Messages.getString("MapperManager.removeInputTableTitleMessage", new Object[] { tableName }))) { //$NON-NLS-1$
                IProcess process = mapperComponent.getProcess();
                uiManager.removeInputTableView(currentSelectedDataMapTableView);
                uiManager.updateToolbarButtonsStates(Zone.INPUTS);
                process.removeUniqueConnectionName(currentSelectedDataMapTableView.getDataMapTable().getName());
                uiManager.refreshSqlExpression();
            }
        }
    }

    public void removeSelectedOutputTable() {
        OutputDataMapTableView currentSelectedDataMapTableView = uiManager.getCurrentSelectedOutputTableView();

        if (currentSelectedDataMapTableView != null) {
            OutputTable outputTable = (OutputTable) currentSelectedDataMapTableView.getDataMapTable();
            String tableTitle = ((AbstractDataMapTable) currentSelectedDataMapTableView.getDataMapTable()).getTitle();
            if (MessageDialog.openConfirm(currentSelectedDataMapTableView.getShell(),
                    Messages.getString("MapperManager.removeOutputTableTitle"), //$NON-NLS-1$
                    Messages.getString("MapperManager.removeOutputTableTitleMessage", new Object[] { tableTitle }))) { //$NON-NLS-1$
                IProcess process = mapperComponent.getProcess();
                uiManager.removeOutputTableView(currentSelectedDataMapTableView);
                uiManager.updateToolbarButtonsStates(Zone.OUTPUTS);
                process.removeUniqueConnectionName(outputTable.getUniqueName());
                uiManager.refreshSqlExpression();
            }
        }
    }

    /**
     * DOC amaumont Comment method "isTableOfInputMetadataEditor".
     *
     * @param table
     * @return
     */
    public boolean isTableOfInputMetadataEditor(Table table) {
        MetadataTableEditorView inputEditorView = uiManager.getInputMetaEditorView();
        Table tableEditorView = inputEditorView.getTable();
        return tableEditorView == table;
    }

    /**
     * DOC amaumont Comment method "isTableOfOutputMetadataEditor".
     *
     * @param table
     * @return
     */
    public boolean isTableOfOutputMetadataEditor(Table table) {
        MetadataTableEditorView outputEditorView = uiManager.getOutputMetaEditorView();
        Table tableEditorView = outputEditorView.getTable();
        return tableEditorView == table;
    }

    public TableEntryLocation findUniqueLocation(final TableEntryLocation proposedLocation, IDataMapTable table) {
        TableEntryLocation tableEntryLocation = new TableEntryLocation(proposedLocation);
        int counter = 1;
        boolean exists = true;
        while (exists) {
            boolean found = false;
            if (table != null) {
                for (IColumnEntry entry : table.getColumnEntries()) {
                    // TDI-26953: drag-and-drop column name should case-sensitive
                    if (entry.getName().equalsIgnoreCase(tableEntryLocation.columnName)) {
                        found = true;
                        break;
                    }
                }
            }
            exists = found;
            if (!exists) {
                break;
            }
            tableEntryLocation.columnName = proposedLocation.columnName + "_" + counter++; //$NON-NLS-1$
        }
        return tableEntryLocation;
    }

    /**
     * DOC amaumont Comment method "orderLinks".
     */
    public void orderLinks() {
        linkManager.orderLinks();
    }

    /**
     * DOC amaumont Comment method "changeEntryExpression".
     *
     * @param currentEntry
     * @param text
     */
    public void changeEntryExpression(ITableEntry currentEntry, String text) {
        currentEntry.setExpression(text);
        getProblemsManager().checkProblemsForTableEntry(currentEntry, true);
        DataMapTableView dataMapTableView = retrieveDataMapTableView(currentEntry);
        TableViewer tableViewer = null;
        if (currentEntry instanceof IColumnEntry) {
            tableViewer = dataMapTableView.getTableViewerCreatorForColumns().getTableViewer();
        } else if (currentEntry instanceof FilterTableEntry) {
            if (FilterTableEntry.OTHER_FILTER.equals(((FilterTableEntry) currentEntry).getFilterKind())) {
                tableViewer = dataMapTableView.getTableViewerCreatorForOtherFilters().getTableViewer();
            } else {
                tableViewer = dataMapTableView.getTableViewerCreatorForWhereFilters().getTableViewer();
            }
        }
        if (currentEntry.getProblems() != null) {
            tableViewer.getTable().deselectAll();
        }
        tableViewer.refresh(currentEntry);

        uiManager.parseNewExpression(text, currentEntry, false);
        uiManager.refreshSqlExpression();
    }

    public DbMapComponent getComponent() {
        return this.mapperComponent;
    }

    /**
     * DOC amaumont Comment method "getPreviewPath".
     *
     * @return
     */
    public String getPreviewFilePath() {
        IRepositoryService service = PluginUtils.getRepositoryService();
        return service.getPathFileName(RepositoryConstants.IMG_DIRECTORY, getPreviewFileName()).toString();
    }

    /**
     * DOC amaumont Comment method "getPreviewFileName".
     *
     * @return
     */
    private String getPreviewFileName() {
        return mapperComponent.getProcess().getId()
                + "-" + mapperComponent.getUniqueName() + "-" + EParameterName.PREVIEW.getName() //$NON-NLS-1$ //$NON-NLS-2$
                + ".bmp"; //$NON-NLS-1$
    }

    public void updateEmfParameters(String... parametersToUpdate) {

        HashSet<String> hParametersToUpdate = new HashSet<String>();
        for (String element : parametersToUpdate) {
            hParametersToUpdate.add(element);
        }

        List<? extends IElementParameter> elementParameters = mapperComponent.getElementParameters();
        for (IElementParameter parameter : elementParameters) {
            if (hParametersToUpdate.contains(parameter.getName())) {
                // set preview path to PREVIEW parameter
                if (EParameterName.PREVIEW.getName().equals(parameter.getName())) {
                    String previewFileName = getPreviewFileName();
                    parameter.setValue(previewFileName == null ? "" : previewFileName); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    public Object getElementParameterValue(String parameterName) {

        List<? extends IElementParameter> elementParameters = mapperComponent.getElementParameters();
        for (IElementParameter parameter : elementParameters) {
            if (parameter.getName().equals(parameterName)) {
                return parameter.getValue();
            }
        }
        return null;
    }

    /**
     * DOC amaumont Comment method "replacePreviousLocationInAllExpressions".
     */
    public void replacePreviousLocationInAllExpressions(final TableEntryLocation previousLocation,
            final TableEntryLocation newLocation, boolean renamed) {

        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(getCurrentLanguage());
        Collection<IDataMapTable> tablesData = getTablesData();
        for (IDataMapTable table : tablesData) {
            List<IColumnEntry> columnEntries = table.getColumnEntries();
            for (IColumnEntry entry : columnEntries) {
                replaceLocation(previousLocation, newLocation, dataMapExpressionParser, table, entry, renamed);
            }
            if (table instanceof OutputTable) {
                List<FilterTableEntry> whereConstraintEntries = ((OutputTable) table).getWhereFilterEntries();
                for (FilterTableEntry entry : whereConstraintEntries) {
                    replaceLocation(previousLocation, newLocation, dataMapExpressionParser, table, entry, renamed);
                }
                List<FilterTableEntry> otherConstraintEntries = ((OutputTable) table).getOtherFilterEntries();
                for (FilterTableEntry entry : otherConstraintEntries) {
                    replaceLocation(previousLocation, newLocation, dataMapExpressionParser, table, entry, renamed);
                }
            }
        }
    }

    /**
     * DOC amaumont Comment method "getCurrentLanguage".
     *
     * @return
     */
    public IDbLanguage getCurrentLanguage() {
        return getComponent().getGenerationManager().getLanguage();
    }

    /**
     *
     * DOC amaumont Comment method "replaceLocation".
     *
     * @param previousLocation
     * @param newLocation
     * @param dataMapExpressionParser
     * @param table
     * @param entry
     * @return true if expression of entry has changed
     */
    private boolean replaceLocation(final TableEntryLocation previousLocation, final TableEntryLocation newLocation,
            DataMapExpressionParser dataMapExpressionParser, IDataMapTable table, ITableEntry entry, boolean renamed) {
        boolean expressionHasChanged = false;
        String currentExpression = entry.getExpression();
        TableEntryLocation[] tableEntryLocations = dataMapExpressionParser.parseTableEntryLocations(currentExpression);
        // loop on all locations of current expression
        for (TableEntryLocation currentLocation : tableEntryLocations) {
            if (renamed && previousLocation.tableName.equals(currentLocation.tableName)) {
                previousLocation.columnName = currentLocation.columnName;
                newLocation.columnName = currentLocation.columnName;
            }
            if (currentLocation.equals(previousLocation)) {
                currentExpression = dataMapExpressionParser.replaceLocation(currentExpression, previousLocation, newLocation);
                expressionHasChanged = true;
            }
        }
        if (expressionHasChanged) {
            entry.setExpression(currentExpression);
            DataMapTableView dataMapTableView = retrieveIDataMapTableView(table);
            TableViewerCreator tableViewerCreator = null;
            if (entry instanceof IColumnEntry) {
                tableViewerCreator = dataMapTableView.getTableViewerCreatorForColumns();
            } else if (entry instanceof FilterTableEntry) {
                if (FilterTableEntry.OTHER_FILTER.equals(((FilterTableEntry) entry).getFilterKind())) {
                    tableViewerCreator = dataMapTableView.getTableViewerCreatorForOtherFilters();
                } else {
                    tableViewerCreator = dataMapTableView.getTableViewerCreatorForWhereFilters();
                }
            }
            tableViewerCreator.getTableViewer().refresh(entry);
            uiManager.parseExpression(currentExpression, entry, false, true, false, renamed);
            return true;
        }
        return false;
    }

    /**
     * @return
     * @see org.talend.designer.abstractmap.managers.LinkManager#getCurrentNumberLinks()
     */
    public int getCurrentNumberLinks() {
        return this.linkManager.getCurrentNumberLinks();
    }

    /**
     * @return
     * @see org.talend.designer.dbmap.ui.MapperUI#getCommandStack()
     */
    public CommandStack getCommandStack() {
        return this.commandStack;
    }

    /**
     * Sets the commandStack.
     *
     * @param commandStack the commandStack to set
     */
    public void setCommandStack(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public void executeCommand(Command command) {
        if (this.commandStack != null) {
            this.commandStack.execute(command);
        } else {
            command.execute();
        }
    }

    public boolean checkEntryHasEmptyExpression(ITableEntry entry) {
        return entry.getExpression() == null || entry.getExpression().trim().length() == 0;
    }

    /**
     * DOC amaumont Comment method "mapAutomaticallly".
     */
    public void mapAutomaticallly() {
        AutoMapper autoMapper = new AutoMapper(this);
        autoMapper.map();
        uiManager.refreshSqlExpression();
    }

    public void useDelimitedIdentifiers(boolean useDelimitedIdentifiers) {
        getComponent().getGenerationManager().setUseDelimitedIdentifiers(useDelimitedIdentifiers);
        uiManager.refreshSqlExpression();
    }

    public void useAliasInOutputTable(boolean useAliasInOutputTable) {
        getComponent().getGenerationManager().setUseAliasInOutputTable(useAliasInOutputTable);
        uiManager.refreshSqlExpression();
    }

    /**
     * Getter for problemsManager.
     *
     * @return the problemsManager
     */
    public ProblemsManager getProblemsManager() {
        return this.problemsManager;
    }

    public boolean componentIsReadOnly() {
        if (getAbstractMapComponent().getOriginalNode().getJobletNode() != null) {
            return getAbstractMapComponent().isReadOnly() || getAbstractMapComponent().getOriginalNode().isReadOnly();
        }

        return getAbstractMapComponent().isReadOnly() || getAbstractMapComponent().getProcess().isReadOnly()
                || getAbstractMapComponent().getOriginalNode().isReadOnly();
    }

    /**
     * DOC amaumont Comment method "addAlias".
     */
    public void addInputAliasTable() {

        AliasDialog aliasDialog = new AliasDialog(this, tableManager.getPhysicalInputTableNames(), tableManager.getAliases(),
                tableManager.getVisibleTables(), false);
        if (!aliasDialog.open()) {
            return;
        }

        List<IOConnection> incomingConnections = getComponent().getMapperMain().getIoInputConnections();
        IOConnection connectionFound = null;
        for (IOConnection connection : incomingConnections) {
            if (connection.getName().equalsIgnoreCase(aliasDialog.getTableName())) {
                connectionFound = connection;
                break;
            }
        }

        List<DataMapTableView> inputsTablesView = getUiManager().getInputsTablesView();
        int sizeOutputsView = inputsTablesView.size();
        Control lastChild = null;
        if (sizeOutputsView - 1 >= 0) {
            lastChild = inputsTablesView.get(sizeOutputsView - 1);
        }

        String alias = aliasDialog.getAlias();
        boolean isPhysicalTable = alias.equals("") || alias.equalsIgnoreCase(aliasDialog.getTableName()); //$NON-NLS-1$
        String aliasOrTableName = isPhysicalTable ? aliasDialog.getTableName() : alias;

        boolean isInvisiblePhysicalTable = aliasDialog.isSameAsPhysicalTable(aliasOrTableName)
                && !aliasDialog.isSameAsVisibleTableName(aliasOrTableName);

        InputTable inputTable = new InputTable(this, connectionFound, aliasOrTableName);
        if (isInvisiblePhysicalTable) {
            inputTable.setAlias(null);
        } else {
            inputTable.setAlias(aliasOrTableName);
        }
        inputTable.setTableName(aliasDialog.getTableName());
        inputTable.initFromExternalData(null);

        TablesZoneView tablesZoneViewInputs = uiManager.getTablesZoneViewInputs();
        DataMapTableView dataMapTableView = uiManager.createNewInputTableView(lastChild, inputTable, tablesZoneViewInputs);
        tablesZoneViewInputs.setSize(tablesZoneViewInputs.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        tablesZoneViewInputs.layout(true, true);
        uiManager.moveInputScrollBarZoneToMax();
        uiManager.refreshBackground(true, false);
        tablesZoneViewInputs.layout();
        uiManager.selectDataMapTableView(dataMapTableView, true, false);
        uiManager.updateDropDownJoinTypeForInputs();
        uiManager.parseAllExpressionsForAllTables();
        uiManager.refreshSqlExpression();
        getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);

    }

    public void renameInputAliasTable() {
        AliasDialog aliasDialog = new AliasDialog(this, tableManager.getPhysicalInputTableNames(), tableManager.getAliases(),
                tableManager.getVisibleTables(), true);
        if (!aliasDialog.open()) {
            return;
        }

        String alias = aliasDialog.getAlias();
        boolean isPhysicalTable = alias.equals("") || alias.equalsIgnoreCase(aliasDialog.getTableName()); //$NON-NLS-1$
        String aliasOrTableName = isPhysicalTable ? aliasDialog.getTableName() : alias;

        boolean isInvisiblePhysicalTable = aliasDialog.isSameAsPhysicalTable(aliasOrTableName)
                && !aliasDialog.isSameAsVisibleTableName(aliasOrTableName);

        InputDataMapTableView currentSelectedDataMapTableView = uiManager.getCurrentSelectedInputTableView();
        if (currentSelectedDataMapTableView != null) {
            IDataMapTable dataMapTable = currentSelectedDataMapTableView.getDataMapTable();
            if (dataMapTable != null && dataMapTable instanceof InputTable) {
                InputTable inputTable = ((InputTable) dataMapTable);
                String oldName = inputTable.getName();
                if (isInvisiblePhysicalTable || isPhysicalTable) {
                    inputTable.setAlias(null);
                } else {
                    inputTable.setAlias(aliasOrTableName);
                }
                // Update expressions
                TableEntryLocation oldLocation = new TableEntryLocation(oldName, null);
                TableEntryLocation newLocation = new TableEntryLocation(inputTable.getName(), null);
                updateTableEntries(inputTable, oldName, inputTable.getName());
                // mapperComponent.replaceLocationsInAllExpressions(oldLocation, newLocation, true);
                replacePreviousLocationInAllExpressions(oldLocation, newLocation, true);

                uiManager.refreshBackground(false, false);
                uiManager.updateToolbarButtonsStates(Zone.INPUTS);
                currentSelectedDataMapTableView.updateSelectedDataMapTableViewTitle();
                // Update tab
                MetadataTableEditorView inputEditorView = uiManager.getInputMetaEditorView();
                if (inputEditorView != null) {
                    inputEditorView.setTitle(inputTable.getTitle());
                }
                uiManager.refreshSqlExpression();
                getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
            }
        }
    }

    public void updateTableEntries(InputTable inputTable, String oldName, String newName) {
        List<IColumnEntry> columnEntries = inputTable.getColumnEntries();
        for (IColumnEntry entry : columnEntries) {
            tableEntriesManager.updateTableEntryLocation(entry, oldName, newName);
        }
    }

    /**
     * DOC amaumont Comment method "isUnmatchingEntry".
     *
     * @param inputEntry
     * @return
     */
    public boolean isUnmatchingEntry(InputColumnTableEntry inputEntry) {
        return tableEntriesManager.isUnmatchingEntryWithDbColumn(inputEntry);
    }

    /**
     * DOC amaumont Comment method "initInternalData".
     */
    public void initInternalData() {
        List<IOConnection> ioInputConnections = getComponent().getMapperMain().getIoInputConnections();
        tableEntriesManager.loadDbTableNameColumnNameToMetadaColumns(ioInputConnections);
    }

    protected TableEntriesManager getTableEntriesManager() {
        return this.tableEntriesManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.managers.AbstractMapperManager#getLinkManager()
     */
    @Override
    public ILinkManager getLinkManager() {
        return linkManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.managers.AbstractMapperManager#isMapperChanged()
     */
    @Override
    public boolean isDataChanged() {
        return false;
    }
}