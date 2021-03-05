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
package org.talend.designer.mapper.managers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.metadata.dialog.CustomTableManager;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.core.ui.preference.metadata.MetadataTypeLengthConstants;
import org.talend.designer.abstractmap.managers.AbstractMapperManager;
import org.talend.designer.abstractmap.managers.ILinkManager;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.abstractmap.ui.visualmap.link.LinkState;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.mapper.MapperComponent;
import org.talend.designer.mapper.PluginUtils;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.language.generation.JavaGenerationManager.PROBLEM_KEY_FIELD;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.table.TMAP_LOOKUP_MODE;
import org.talend.designer.mapper.model.table.TMAP_MATCHING_MODE;
import org.talend.designer.mapper.model.table.VarsTable;
import org.talend.designer.mapper.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.mapper.model.tableentry.ExpressionFilterEntry;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.InputColumnTableEntry;
import org.talend.designer.mapper.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.model.tableentry.VarTableEntry;
import org.talend.designer.mapper.ui.automap.AutoMapper;
import org.talend.designer.mapper.ui.commands.AddGlobalMapEntryCommand;
import org.talend.designer.mapper.ui.commands.AddVarEntryCommand;
import org.talend.designer.mapper.ui.visualmap.TableEntryProperties;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.table.EntryState;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;
import org.talend.designer.mapper.ui.visualmap.zone.scrollable.TablesZoneView;
import org.talend.designer.mapper.utils.DataMapExpressionParser;
import org.talend.designer.mapper.utils.MapperHelper;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MapperManager extends AbstractMapperManager {

    public static final String MAPPER_MODEL_DATA = "MAPPER_MODEL_DATA"; //$NON-NLS-1$

    private TableEntriesManager tableEntriesManager;

    TableManager tableManager;

    private ILinkManager linkManager;

    private UIManager uiManager;

    private CommandStack commandStack;

    private ProblemsManager problemsManager;

    private Map<String, Object> defaultSettingMap = new HashMap<String, Object>();

    /**
     * True if the process is big data, which has a constraint that all join keys must be consistent between lookup
     * tables, and a different properties dialog.
     */
    private boolean isBigDataProcess;

    /** True if the process is MapReduce, which has different defaults for match mode. */
    private boolean isMRProcess;

    private boolean isSearchOption = false;

    public MapperManager(MapperComponent mapperComponent) {
        super(mapperComponent);
        tableEntriesManager = new TableEntriesManager(this);
        tableManager = new TableManager();
        linkManager = new LinkManager();
        problemsManager = new ProblemsManager(this);
        IProcess process = getAbstractMapComponent().getProcess();
        isMRProcess = ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(process.getComponentsType());
        isBigDataProcess = MapperHelper.isMapperOnBigDataProcess(process.getComponentsType());
        getDefaultSetting();
    }

    public void addTablePair(DataMapTableView view, IDataMapTable data) {
        tableManager.addTable(view, data);
        tableEntriesManager.addAll(data.getColumnEntries());
        if (data instanceof OutputTable) {
            tableEntriesManager.addAll(((OutputTable) data).getFilterEntries());
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
        List<IColumnEntry> dataMapTableEntries = dataTable.getColumnEntries();
        if (isAdvancedMap() && dataTable instanceof AbstractInOutTable) {
            tableEntriesManager.removeAll(Arrays.asList(((AbstractInOutTable) dataTable).getExpressionFilter()));
        }
        tableEntriesManager.removeAll(dataMapTableEntries);
        if (dataTable instanceof OutputTable) {
            List<FilterTableEntry> constraintEntries = ((OutputTable) dataTable).getFilterEntries();
            tableEntriesManager.removeAll(constraintEntries);
        }
        tableManager.removeTable(view);
    }

    /**
     *
     * Remove the <code>DataMapTable</code>-<code>DataMapTableView</code> pair.
     *
     * @param view
     */
    public void removeTablePair(IDataMapTable dataTable) {
        List<IColumnEntry> dataMapTableEntries = dataTable.getColumnEntries();
        tableEntriesManager.removeAll(dataMapTableEntries);
        if (dataTable instanceof OutputTable) {
            List<FilterTableEntry> constraintEntries = ((OutputTable) dataTable).getFilterEntries();
            tableEntriesManager.removeAll(constraintEntries);
        }
        tableManager.removeTable(dataTable);
    }

    /**
     * DOC amaumont Comment method "getDataMapTable".
     */
    public IDataMapTable retrieveAbstractDataMapTable(DataMapTableView dataMapTableView) {
        return tableManager.getData(dataMapTableView);
    }

    /**
     * DOC amaumont Comment method "getDataMapTableView".
     */
    public DataMapTableView retrieveAbstractDataMapTableView(IDataMapTable abstractDataMapTable) {
        return tableManager.getView(abstractDataMapTable);
    }

    public ITableEntry retrieveTableEntry(TableEntryLocation location) {
        return tableEntriesManager.retrieveTableEntry(location);
    }

    public DataMapTableView retrieveDataMapTableView(ITableEntry dataMapTableEntry) {
        return tableManager.getView(dataMapTableEntry.getParent());
    }

    public TableViewerCreator retrieveTableViewerCreator(ITableEntry dataMapTableEntry) {
        DataMapTableView view = retrieveDataMapTableView(dataMapTableEntry);
        TableViewerCreator tableViewerCreator = null;
        if (view != null) {
            if (dataMapTableEntry instanceof AbstractInOutTableEntry || dataMapTableEntry instanceof VarTableEntry) {
                tableViewerCreator = view.getTableViewerCreatorForColumns();
            } else if (dataMapTableEntry instanceof FilterTableEntry) {
                tableViewerCreator = view.getTableViewerCreatorForFilters();
            }
        }
        return tableViewerCreator;
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

    public DataMapTableView retrieveDataMapTableView(Control swtControl) {
        return this.tableManager.getView(swtControl);
    }

    /**
     * DOC amaumont Comment method "getDataMapTable".
     */
    public IDataMapTable retrieveAbstractDataMapTable(TableEntryLocation names) {
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
     * @return An IMetadataTable appropriate for use with the mapper component in its current context.
     */
    private IMetadataTable getNewMetadataTable() {
        // All component types use the same MetadataTable implementation.
        return new MetadataTable();
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

    public OutputTable getOutputTableByName(String name) {
        if (name == null) {
            return null;
        }
        for (OutputTable table : getOutputTables()) {
            if (name.equals(table.getName())) {
                return table;
            }
        }

        return null;
    }

    /**
     * DOC amaumont Comment method "renameProcessColumnName".
     *
     * @param currentModifiedTableEntry
     * @param newColumnName
     * @param newColumnName2
     */
    public void changeColumnName(ITableEntry dataMapTableEntry, String previousColumnName, String newColumnName) {
        tableEntriesManager.renameEntryName(dataMapTableEntry, previousColumnName, newColumnName);
    }

    public void removeTableEntry(ITableEntry dataMapTableEntry) {
        tableEntriesManager.remove(dataMapTableEntry);
    }

    /**
     * DOC amaumont Comment method "removeLinksFrom".
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
            String expression = metadataColumn.getExpression();
            if (expression != null && !"".equals(expression)) {
                dataMapTableEntry = new OutputColumnTableEntry(abstractDataMapTable, metadataColumn, expression);
            } else {
                dataMapTableEntry = new OutputColumnTableEntry(abstractDataMapTable, metadataColumn);
            }
        } else {
            throw new IllegalArgumentException(Messages.getString("MapperManager.exceptionMessage.useOtherSignature")); //$NON-NLS-1$
        }
        tableEntriesManager.addTableEntry(dataMapTableEntry, index);
        return dataMapTableEntry;
    }

    /**
     * This method is called when "addMetadataTableEditorEntry" is called (event on list of MetadataEditor) , so if you
     * want keep synchronisation between inputs/outputs DataMaps and MetadataEditors don't call this method.
     *
     * For other uses such as add an entry to VarsTable or add entries to inputs or outputs DataMaps when
     * MetadataEditors are not active, call it.
     *
     * @param dataMapTableView
     * @param index
     * @param type TODO
     * @param metadataColumn, can be null if added in VarsTable
     */
    public VarTableEntry addNewVarEntry(DataMapTableView dataMapTableView, String name, Integer index, String type) {
        IDataMapTable abstractDataMapTable = dataMapTableView.getDataMapTable();
        VarTableEntry dataMapTableEntry = null;
        if (dataMapTableView.getZone() == Zone.VARS) {
            dataMapTableEntry = new VarTableEntry(abstractDataMapTable, name, null, type);
        } else {
            throw new IllegalArgumentException(Messages.getString("MapperManager.exceptionMessage.useOtherSignature")); //$NON-NLS-1$
        }

        AddVarEntryCommand varEntryCommand = new AddVarEntryCommand(tableEntriesManager, dataMapTableEntry, index);
        executeCommand(varEntryCommand);

        return dataMapTableEntry;
    }

    /**
     *
     * @param dataMapTableView
     * @param index
     * @param type TODO
     * @param metadataColumn, can be null if added in VarsTable
     */
    public GlobalMapEntry addNewGlobalMapEntry(DataMapTableView dataMapTableView, ITableEntry tableEntrySource, Integer index) {
        IDataMapTable abstractDataMapTable = dataMapTableView.getDataMapTable();
        GlobalMapEntry dataMapTableEntry = null;
        if (dataMapTableView.getZone() == Zone.INPUTS) {
            dataMapTableEntry = new GlobalMapEntry(abstractDataMapTable,
                    "\"" + dataMapTableView.findUniqueName("myKey") + "\"", null); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        } else {
            throw new IllegalArgumentException(Messages.getString("MapperManager.exceptionMessage.useOtherSignature")); //$NON-NLS-1$
        }

        AddGlobalMapEntryCommand command = new AddGlobalMapEntryCommand(tableEntriesManager, dataMapTableEntry, index);
        executeCommand(command);

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
        IDataMapTable abstractDataMapTable = dataMapTableView.getDataMapTable();
        FilterTableEntry constraintEntry = new FilterTableEntry(abstractDataMapTable, name, null);
        tableEntriesManager.addTableEntry(constraintEntry, index);
        return constraintEntry;
    }

    /**
     * DOC amaumont Comment method "addOutput".
     */
    public void addOutput() {
        String joinTableName = null;
        OutputTable abstractDataMapTable = null;

        String name = uiManager.openNewOutputCreationDialog();
        if (name == null) {
            return;
        }

        String[] split = name.split(uiManager.NAME_SEPARATOR);
        String tableName = split[0];
        boolean isCreatingJoinTable = split.length == 2;
        if (isCreatingJoinTable) {
            joinTableName = split[1];
        }

        IProcess process = getAbstractMapComponent().getProcess();
        OutputTable orignalOutputTable = null;
        if (isCreatingJoinTable) {
            orignalOutputTable = getOutputTableByName(tableName);
            if (orignalOutputTable != null) {
                IMetadataTable metadataTable = orignalOutputTable.getMetadataTable();
                if (metadataTable != null) {
                    process.addUniqueConnectionName(joinTableName);
                    abstractDataMapTable = new OutputTable(this, metadataTable, joinTableName);
                    abstractDataMapTable.setIsJoinTableOf(tableName);
                }
            }

        } else {
            process.addUniqueConnectionName(tableName);
            IMetadataTable metadataTable = getNewMetadataTable();
            metadataTable.setTableName(tableName);
            abstractDataMapTable = new OutputTable(this, metadataTable, tableName);
        }

        if (abstractDataMapTable == null) {
            return;
        }
        abstractDataMapTable.initFromExternalData(null);

        List<DataMapTableView> outputsTablesView = uiManager.getOutputsTablesView();
        int sizeOutputsView = outputsTablesView.size();
        Control lastChild = null;
        if (sizeOutputsView - 1 >= 0) {
            lastChild = outputsTablesView.get(sizeOutputsView - 1);
        }

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

    public void addRejectOutput() {
        String baseName = ERROR_REJECT;

        IProcess process = getAbstractMapComponent().getProcess();
        String tableName = baseName;
        if (!process.checkValidConnectionName(baseName) && process instanceof IProcess2) {
            final String uniqueName = ((IProcess2) process).generateUniqueConnectionName("row", baseName);
            tableName = uniqueName;
            ((IProcess2) process).addUniqueConnectionName(uniqueName);
        } else if (process instanceof IProcess2) {
            tableName = baseName;
            ((IProcess2) process).addUniqueConnectionName(baseName);
        }
        IMetadataTable metadataTable = getNewMetadataTable();
        metadataTable.setTableName(tableName);
        MetadataColumn errorMessageCol = new MetadataColumn();
        errorMessageCol.setLabel(ERROR_REJECT_MESSAGE);
        errorMessageCol.setTalendType(DesignerPlugin.getDefault().getPreferenceStore()
                .getString(MetadataTypeLengthConstants.FIELD_DEFAULT_TYPE));
        errorMessageCol.setNullable(true);
        errorMessageCol.setOriginalDbColumnName(ERROR_REJECT_MESSAGE);
        errorMessageCol.setReadOnly(true);
        errorMessageCol.setCustom(true);
        errorMessageCol.setCustomId(0);
        metadataTable.getListColumns().add(errorMessageCol);

        MetadataColumn errorStackTrace = new MetadataColumn();
        errorStackTrace.setLabel(ERROR_REJECT_STACK_TRACE);
        errorStackTrace.setTalendType(DesignerPlugin.getDefault().getPreferenceStore()
                .getString(MetadataTypeLengthConstants.FIELD_DEFAULT_TYPE));
        errorStackTrace.setNullable(true);
        errorStackTrace.setOriginalDbColumnName(ERROR_REJECT_STACK_TRACE);
        errorStackTrace.setReadOnly(true);
        errorStackTrace.setCustom(true);
        errorStackTrace.setCustomId(1);
        metadataTable.getListColumns().add(errorStackTrace);

        OutputTable abstractDataMapTable = new OutputTable(this, metadataTable, tableName);
        abstractDataMapTable.setErrorRejectTable(true);
        abstractDataMapTable.initFromExternalData(null);
        TablesZoneView tablesZoneViewOutputs = uiManager.getTablesZoneViewOutputs();
        DataMapTableView rejectDataMapTableView = uiManager.createNewOutputTableView(null, abstractDataMapTable,
                tablesZoneViewOutputs);
        tablesZoneViewOutputs.setSize(tablesZoneViewOutputs.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        List<DataMapTableView> outputsTablesView = uiManager.getOutputsTablesView();
        int sizeList = outputsTablesView.size();
        for (int i = 0; i < sizeList; i++) {
            if (i + 1 < sizeList) {
                FormData formData = (FormData) outputsTablesView.get(i + 1).getLayoutData();
                formData.top = new FormAttachment(outputsTablesView.get(i));
            }

        }
        CustomTableManager.addCustomManagementToTable(uiManager.getOutputMetaEditorView(), true);
        tablesZoneViewOutputs.setSize(tablesZoneViewOutputs.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        tablesZoneViewOutputs.layout();
        uiManager.moveOutputScrollBarZoneToMax();
        uiManager.refreshBackground(true, false);
        uiManager.selectDataMapTableView(rejectDataMapTableView, true, false);
    }

    public boolean hasRejectOutput(List<OutputTable> tables) {
        if (tables == null || tables.size() == 0) {
            return false;
        }
        for (OutputTable outputTable : tables) {
            if (outputTable.isErrorRejectTable()) {
                return true;
            }
        }
        return false;

    }

    public void removeRejectOutput() {
        List<DataMapTableView> outputsTablesView = uiManager.getOutputsTablesView();
        Iterator<DataMapTableView> iterator = outputsTablesView.iterator();
        DataMapTableView outputTable = null;
        DataMapTableView toRemove = null;
        while (iterator.hasNext()) {
            outputTable = iterator.next();
            if (outputTable.getDataMapTable() instanceof OutputTable
                    && ((OutputTable) outputTable.getDataMapTable()).isErrorRejectTable()) {
                toRemove = outputTable;
                iterator.remove();
                break;
            }
        }
        if (toRemove != null) {
            uiManager.removeOutputTableView(toRemove);
            uiManager.updateToolbarButtonsStates(Zone.OUTPUTS);
            IProcess process = getAbstractMapComponent().getProcess();
            process.removeUniqueConnectionName(toRemove.getDataMapTable().getName());
        }
    }

    public void removeSelectedOutput() {
        DataMapTableView currentSelectedDataMapTableView = uiManager.getCurrentSelectedOutputTableView();
        String append = "";
        OutputTable outputTable = ((OutputTable) currentSelectedDataMapTableView.getDataMapTable());
        List<DataMapTableView> relatedOutputsTableView = null;
        if (outputTable.getIsJoinTableOf() == null) {
            relatedOutputsTableView = uiManager.getRelatedOutputsTableView(currentSelectedDataMapTableView);
            if (relatedOutputsTableView != null && !relatedOutputsTableView.isEmpty()) {
                append = " and it's join table ";
                for (DataMapTableView tableView : relatedOutputsTableView) {
                    IDataMapTable retrieveAbstractDataMapTable = this.retrieveAbstractDataMapTable(tableView);
                    if (retrieveAbstractDataMapTable != null) {
                        append = append + "'" + retrieveAbstractDataMapTable.getName() + " ' ,";
                    }
                }
                append = append.substring(0, append.length() - 1);
            }
        }

        if (currentSelectedDataMapTableView != null) {
            String tableName = currentSelectedDataMapTableView.getDataMapTable().getName();
            if (MessageDialog.openConfirm(currentSelectedDataMapTableView.getShell(),
                    Messages.getString("MapperManager.removeOutputTableTitle"), //$NON-NLS-1$
                    Messages.getString("MapperManager.removeOutputTableTitleMessage") + tableName + " '" + append + "?")) { //$NON-NLS-1$ //$NON-NLS-2$
                IProcess process = getAbstractMapComponent().getProcess();
                uiManager.removeOutputTableView(currentSelectedDataMapTableView);

                // remove join table
                if (outputTable.getIsJoinTableOf() == null && relatedOutputsTableView != null) {
                    for (DataMapTableView view : relatedOutputsTableView) {
                        uiManager.removeOutputTableView(view);
                        process.removeUniqueConnectionName(view.getDataMapTable().getName());
                    }
                }
                uiManager.updateToolbarButtonsStates(Zone.OUTPUTS);
                process.removeUniqueConnectionName(currentSelectedDataMapTableView.getDataMapTable().getName());
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
    public void changeEntryExpression(final ITableEntry currentEntry, String text) {
        currentEntry.setExpression(text);

        DataMapTableView dataMapTableView = retrieveDataMapTableView(currentEntry);
        TableViewer tableViewer = null;
        if (currentEntry instanceof IColumnEntry || currentEntry instanceof FilterTableEntry) {

            getProblemsManager().checkProblemsForTableEntryWithDelayLimiter(currentEntry);

            if (currentEntry instanceof IColumnEntry) {
                tableViewer = dataMapTableView.getTableViewerCreatorForColumns().getTableViewer();
            } else if (currentEntry instanceof FilterTableEntry) {
                tableViewer = dataMapTableView.getTableViewerCreatorForFilters().getTableViewer();
            }
            if (currentEntry.getProblems() != null) {
                tableViewer.getTable().deselectAll();
            }
            tableViewer.refresh(currentEntry);
        } else if (currentEntry instanceof ExpressionFilterEntry) {
            dataMapTableView.getExpressionFilterText().setTextWithoutNotifyListeners(text);
            if (!dataMapTableView.getExpressionFilterText().isFocusControl()) {
                dataMapTableView.checkProblemsForExpressionFilterWithDelay();
            }
        }

        uiManager.parseNewExpression(text, currentEntry, false);
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
        return getAbstractMapComponent().getProcess().getId()
                + "-" + getAbstractMapComponent().getUniqueName() + "-" + EParameterName.PREVIEW.getName() //$NON-NLS-1$ //$NON-NLS-2$
                + ".bmp"; //$NON-NLS-1$
    }

    public void updateEmfParameters(String... parametersToUpdate) {

        HashSet<String> hParametersToUpdate = new HashSet<String>();
        for (String element : parametersToUpdate) {
            hParametersToUpdate.add(element);
        }

        List<? extends IElementParameter> elementParameters = getAbstractMapComponent().getElementParameters();
        boolean needUpdateComponent = false;
        for (IElementParameter parameter : elementParameters) {

            if (hParametersToUpdate.contains(parameter.getName())) {
                // set preview path to PREVIEW parameter
                if (EParameterName.PREVIEW.getName().equals(parameter.getName())) {
                    String previewFileName = getPreviewFileName();
                    parameter.setValue(previewFileName == null ? "" : previewFileName); //$NON-NLS-1$
                    needUpdateComponent = true;
                }
            }
        }

        // Updates the preview picture.
        for (IElementParameter elementParameter : elementParameters) {
            if (needUpdateComponent && EParameterName.UPDATE_COMPONENTS.getName().equals(elementParameter.getName())) {
                elementParameter.setValue(new Boolean(true));
            }
        }
    }

    /**
     * DOC amaumont Comment method "replacePreviousLocationInAllExpressions".
     */
    public void replacePreviousLocationInAllExpressions(final TableEntryLocation previousLocation,
            final TableEntryLocation newLocation) {

        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(LanguageProvider.getCurrentLanguage());
        Collection<IDataMapTable> tablesData = getTablesData();
        for (IDataMapTable table : tablesData) {
            List<IColumnEntry> columnEntries = table.getColumnEntries();
            if (table instanceof AbstractInOutTable) {
                replaceLocation(previousLocation, newLocation, dataMapExpressionParser, table,
                        ((AbstractInOutTable) table).getExpressionFilter());
            }
            for (IColumnEntry entry : columnEntries) {
                replaceLocation(previousLocation, newLocation, dataMapExpressionParser, table, entry);
            }
            if (table instanceof OutputTable) {
                List<FilterTableEntry> constraintEntries = ((OutputTable) table).getFilterEntries();
                for (FilterTableEntry entry : constraintEntries) {
                    replaceLocation(previousLocation, newLocation, dataMapExpressionParser, table, entry);
                }
            }

        }
        uiManager.refreshBackground(false, false);
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
            DataMapExpressionParser dataMapExpressionParser, IDataMapTable table, ITableEntry entry) {
        if (entry.getExpression() == null || entry.getExpression().trim().length() == 0) {
            return false;
        }
        boolean expressionHasChanged = false;
        String currentExpression = entry.getExpression();
        TableEntryLocation[] tableEntryLocations = dataMapExpressionParser.parseTableEntryLocations(currentExpression);
        // loop on all locations of current expression
        for (TableEntryLocation currentLocation : tableEntryLocations) {
            if (currentLocation.equals(previousLocation)) {
                currentExpression = dataMapExpressionParser.replaceLocation(currentExpression, previousLocation, newLocation);
                expressionHasChanged = true;
            }
        }
        if (expressionHasChanged) {
            entry.setExpression(currentExpression);
            DataMapTableView dataMapTableView = retrieveAbstractDataMapTableView(table);
            TableViewerCreator tableViewerCreator = null;
            if (entry instanceof IColumnEntry || entry instanceof FilterTableEntry) {
                if (entry instanceof IColumnEntry) {
                    tableViewerCreator = dataMapTableView.getTableViewerCreatorForColumns();
                } else if (entry instanceof FilterTableEntry) {
                    tableViewerCreator = dataMapTableView.getTableViewerCreatorForFilters();
                }
                tableViewerCreator.getTableViewer().refresh(entry);
            } else if (entry instanceof ExpressionFilterEntry) {
                dataMapTableView.getExpressionFilterText().setText(currentExpression);
            }
            uiManager.parseExpression(currentExpression, entry, false, true, false);
            return true;
        }
        return false;
    }

    /**
     * @return
     * @see org.talend.designer.mapper.managers.LinkManager#getCurrentNumberLinks()
     */
    public int getCurrentNumberLinks() {
        return this.linkManager.getCurrentNumberLinks();
    }

    /**
     * @return
     * @see org.talend.designer.mapper.ui.MapperUI#getCommandStack()
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

    /**
     * DOC amaumont Comment method "checkEntryHasValidKey".
     *
     * @param inputEntry
     */
    public boolean checkEntryHasInvalidCheckedKey(InputColumnTableEntry inputEntry) {
        return inputEntry.getMetadataColumn().isKey() && checkEntryHasEmptyExpression(inputEntry);
    }

    /**
     * DOC amaumont Comment method "checkEntryHasValidKey".
     *
     * @param inputEntry
     */
    public boolean checkEntryHasInvalidUncheckedKey(InputColumnTableEntry inputEntry) {
        return !inputEntry.getMetadataColumn().isKey() && inputEntry.getExpression() != null
                && inputEntry.getExpression().trim().length() > 0;
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
    }

    /**
     * DOC amaumont Comment method "buildKey".
     *
     * @param tableName
     * @param entryName
     * @return
     */
    public String buildProblemKey(PROBLEM_KEY_FIELD problemKeyField, String tableName, String entryName) {
        return problemsManager.buildProblemKey(getAbstractMapComponent().getUniqueName(), problemKeyField, tableName, entryName);
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
     * DOC amaumont Comment method "isAdvancedMap".
     *
     * @return
     */
    public boolean isAdvancedMap() {
        return true;

    }

    /**
     * DOC amaumont Comment method "isTableHasAtLeastOneHashKey".
     *
     * @param inputTable
     */
    public boolean isTableHasAtLeastOneHashKey(InputTable inputTable) {
        boolean atLeastOneHashKey = false;
        List<IColumnEntry> columnEntries = inputTable.getColumnEntries();
        for (IColumnEntry entry : columnEntries) {
            if (entry.getExpression() != null && !entry.getExpression().trim().equals("")) { //$NON-NLS-1$
                atLeastOneHashKey = true;
                break;
            }
        }
        return atLeastOneHashKey;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.managers.AbstractMapperManager#getLinkManager()
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
        getAbstractMapComponent().restoreMapperModelFromInternalData();
        IExternalData originalExternalData = getOriginalExternalData();
        IExternalData currentExternalData = getAbstractMapComponent().getExternalData();
        if (originalExternalData == null && currentExternalData == null) {
            return false;
        }
        if (originalExternalData == null && currentExternalData != null) {
            return true;
        }

        return !originalExternalData.equals(currentExternalData)
                || MapperSettingsManager.getInstance(this).isMapperSettingChanged();
    }

    public Map<String, Object> getDefaultSetting() {
        if (defaultSettingMap.isEmpty()) {
            defaultSettingMap.put(DataMapTableView.LOOKUP_MODEL_SETTING, TMAP_LOOKUP_MODE.LOAD_ONCE);
            defaultSettingMap.put(DataMapTableView.MATCH_MODEL_SETTING, TMAP_MATCHING_MODE.ALL_ROWS);
            defaultSettingMap.put(DataMapTableView.JOIN_MODEL_SETTING, false);
            defaultSettingMap.put(DataMapTableView.PERSISTENCE_MODEL_SETTING, false);
            defaultSettingMap.put(DataMapTableView.OUTPUT_REJECT, false);
            defaultSettingMap.put(DataMapTableView.LOOK_UP_INNER_JOIN_REJECT, false);
            defaultSettingMap.put(DataMapTableView.SCHEMA_TYPE, false);
            defaultSettingMap.put(DataMapTableView.SCHEMA_ID, null);

            defaultSettingMap.put(MapperSettingsManager.REPLICATED_JOIN, false);
            defaultSettingMap.put(MapperSettingsManager.DIE_ON_ERROR, true);
            defaultSettingMap.put(MapperSettingsManager.LEVENSHTEIN, 0);
            defaultSettingMap.put(MapperSettingsManager.JACCARD, 0);

            // boolean parallel = false;
            // IElementParameter paraEle = getAbstractMapComponent().getElementParameter("LKUP_PARALLELIZE");
            // if (paraEle != null) {
            // parallel = (Boolean) paraEle.getValue();
            // }
            defaultSettingMap.put(MapperSettingsManager.LOOKUP_IN_PARALLEL, false);
            defaultSettingMap.put(MapperSettingsManager.ENABLE_AUTO_CONVERT_TYPE, false);
            defaultSettingMap.put(MapperSettingsManager.TEMPORARY_DATA_DIRECTORY, "");
            defaultSettingMap.put(MapperSettingsManager.ROWS_BUFFER_SIZE, 2000000);
        }
        return defaultSettingMap;
    }

    public boolean isPersistentMap() {
        return LanguageProvider.getCurrentLanguage().getCodeLanguage() == ECodeLanguage.JAVA;
    }

    public boolean isCheckSyntaxEnabled() {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        return preferenceStore.getBoolean(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK);
    }

    public boolean isTracesActive() {
        return DesignerPlugin.getDefault().getRunProcessService().enableTraceForActiveRunProcess();
    }

    public boolean isMRProcess() {
        return this.isMRProcess;
    }

    public boolean isBigDataProcess() {
        return this.isBigDataProcess;
    }

    public boolean isSearchOption() {
        return this.isSearchOption;
    }

    public void setSearchOption(boolean isSearchOption) {
        this.isSearchOption = isSearchOption;
    }
}
