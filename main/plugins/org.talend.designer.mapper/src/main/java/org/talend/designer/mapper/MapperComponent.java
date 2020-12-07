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
package org.talend.designer.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.cursor.CursorHelper;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.genhtml.HTMLDocUtils;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.BlockCode;
import org.talend.core.model.process.HashConfiguration;
import org.talend.core.model.process.HashableColumn;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IHashConfiguration;
import org.talend.core.model.process.IHashableColumn;
import org.talend.core.model.process.IHashableInputConnections;
import org.talend.core.model.process.ILookupMode;
import org.talend.core.model.process.IMatchingMode;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.node.IExternalMapEntry;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.service.IDesignerMapperService;
import org.talend.designer.abstractmap.AbstractMapComponent;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.external.data.ExternalMapperUiProperties;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.language.generation.GenerationManager;
import org.talend.designer.mapper.language.generation.GenerationManagerFactory;
import org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable;
import org.talend.designer.mapper.model.emf.mapper.InputTable;
import org.talend.designer.mapper.model.emf.mapper.MapperData;
import org.talend.designer.mapper.model.emf.mapper.MapperFactory;
import org.talend.designer.mapper.model.emf.mapper.MapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.OutputTable;
import org.talend.designer.mapper.model.emf.mapper.UiProperties;
import org.talend.designer.mapper.model.emf.mapper.VarTable;
import org.talend.designer.mapper.model.table.LOOKUP_MODE;
import org.talend.designer.mapper.model.table.TMAP_LOOKUP_MODE;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.utils.DataMapExpressionParser;
import org.talend.designer.mapper.utils.MapperHelper;
import org.talend.designer.mapper.utils.problems.ProblemsAnalyser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MapperComponent extends AbstractMapComponent implements IHashableInputConnections {

    private MapperMain mapperMain;

    private List<IMetadataTable> metadataListOut;

    private ExternalMapperData externalData;

    private GenerationManager generationManager;
    
    private boolean shouldGenerateDatasetCode;

    /**
     * DOC amaumont MapperComponent constructor comment.
     */
    public MapperComponent() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#initialize()
     */
    @Override
    public void initialize() {
        super.initialize();
        initMapperMain(false);
        mapperMain.loadInitialParamters();
    }

    @Override
    public boolean isGeneratedAsVirtualComponent() {
        return MapperHelper.isGeneratedAsVirtualComponent(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#getPersistentData()
     */
    @Override
    public IExternalData getExternalData() {
        if (this.externalData == null) {
            this.externalData = new ExternalMapperData();
        }
        return this.externalData;
    }

    private void initMapperMain(boolean forceNew) {
        if (forceNew || !forceNew && mapperMain == null) {
            mapperMain = new MapperMain(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#open(org.eclipse.swt.widgets.Display)
     */
    public int open(final Display display) {
        // TimeMeasure.start("Total open");
        // TimeMeasure.display = false;

        Shell parentShell = display.getActiveShell();

        CursorHelper.changeCursor(parentShell, SWT.CURSOR_WAIT);

        Shell shell = null;
        try {
            initMapperMain(true);
            mapperMain.createModelFromExternalData(getIODataComponents(), getMetadataList(), externalData, true);
            shell = mapperMain.createUI(display);
            // TimeMeasure.display = true;
            // TimeMeasure.end("Total open");
            try {
                restoreMapperModelFromInternalData();
                mapperMain.getMapperManager().setOriginalExternalData(externalData.clone());
            } catch (CloneNotSupportedException e1) {
                ExceptionHandler.process(e1);
            }
        } finally {
            parentShell.setCursor(null);
        }
        while (shell != null && !shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Throwable e) {
                if (MapperMain.isStandAloneMode()) {
                    e.printStackTrace();
                } else {
                    ExceptionHandler.process(e);
                }
            }
        }
        if (MapperMain.isStandAloneMode()) {
            display.dispose();
        }
        return mapperMain.getMapperDialogResponse();
    }

    public IExternalData getTMapExternalData() {
        initMapperMain(true);
        mapperMain.createModelFromExternalData(getIODataComponents(), getMetadataList(), externalData, true);
        IExternalData data = restoreMapModelFromInternalData();
        try {
            mapperMain.getMapperManager().setOriginalExternalData(externalData.clone());
        } catch (CloneNotSupportedException e) {
            if (MapperMain.isStandAloneMode()) {
                e.printStackTrace();
            } else {
                ExceptionHandler.process(e);
            }
        }
        return data;
    }

    private IExternalData restoreMapModelFromInternalData() {
        super.restoreMapperModelFromInternalData();
        externalData = mapperMain.buildTMAPExternalData();
        IExternalData data = null;
        try {
            data = externalData.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        sortOutputsConnectionsLikeVisualOrder();
        return data;
    }

    /**
     * Restore mapper model from internal stored data.
     */
    @Override
    public void restoreMapperModelFromInternalData() {
        super.restoreMapperModelFromInternalData();
        mapperMain.loadModelFromInternalData();
        metadataListOut = mapperMain.getMetadataListOut();
        externalData = mapperMain.buildExternalData();
        sortOutputsConnectionsLikeVisualOrder();
    }

    /**
     * Sort outgoingConnections for code generation as visible output zone of tMap.
     */
    @SuppressWarnings("unchecked")
    private void sortOutputsConnectionsLikeVisualOrder() {
        List<IConnection> outgoingConnections = (List<IConnection>) getOutgoingConnections();
        Map<String, IConnection> connectionNameToOutgoingConnection = new HashMap<String, IConnection>();
        for (IConnection connection : outgoingConnections) {
            connectionNameToOutgoingConnection.put(connection.getName(), connection);
        }

        List<ExternalMapperTable> outputTables = externalData.getOutputTables();
        List<IConnection> tmpList = new ArrayList<IConnection>(outgoingConnections);
        outgoingConnections.clear();

        int lstSize = outputTables.size();
        for (int i = 0; i < lstSize; i++) {
            ExternalMapperTable table = outputTables.get(i);
            String tableName = table.getName();

            IConnection connection = connectionNameToOutgoingConnection.get(tableName);
            if (connection != null) {
                outgoingConnections.add(connection);
            }
        }
        // add connections without metadata
        for (IConnection connection : tmpList) {
            if (!outgoingConnections.contains(connection)) {
                outgoingConnections.add(connection);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#open()
     */
    public int open(final Composite parent) {
        // initMapperMain(true);
        // mapperMain.createModelFromExternalData(getIODataComponents(), getMetadataList(), externalData, true);
        // mapperMain.createUI(parent);
        // return mapperMain.getMapperDialogResponse();
        return open(parent.getDisplay());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#setPersistentData(java.lang.Object)
     */
    public void setExternalData(IExternalData externalData) {
        this.externalData = (ExternalMapperData) externalData;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getGeneratedCode()
     */
    public String getGeneratedCode() {
        try {
            ICodeGeneratorService service = PluginUtils.getCodeGeneratorService();

            return service.createCodeGenerator().generateComponentCode(this, ECodePart.MAIN);
        } catch (SystemException e) {
            ExceptionHandler.process(e);
        }
        return ""; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getMetadataList()
     */
    @Override
    public List<IMetadataTable> getMetadataList() {
        return this.metadataListOut;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#setMetadataList(java.util.List)
     */
    @Override
    public void setMetadataList(List<IMetadataTable> metadataTablesOut) {
        this.metadataListOut = metadataTablesOut;
    }

    @Override
    public void buildExternalData(AbstractExternalData abstractData) {
        externalData = new ExternalMapperData();
        if (abstractData instanceof MapperData) {
            MapperData mapperData = (MapperData) abstractData;
            List<ExternalMapperTable> externalTables = new ArrayList<ExternalMapperTable>();
            // input
            for (InputTable pTable : mapperData.getInputTables()) {
                ExternalMapperTable externalTable = new ExternalMapperTable();
                setExternalTable(externalTable, pTable);
                externalTable.setLookupMode(pTable.getLookupMode());
                externalTable.setMatchingMode(pTable.getMatchingMode());
                externalTable.setInnerJoin(pTable.isInnerJoin());
                externalTable.setPersistent(pTable.isPersistent());
                externalTable.setGlobalMapKeysValues(getExternalEntities(pTable.getGlobalMapKeysValues()));
                externalTables.add(externalTable);
            }
            externalData.setInputTables(externalTables);
            // output
            externalTables = new ArrayList<ExternalMapperTable>();
            for (OutputTable pTable : mapperData.getOutputTables()) {
                ExternalMapperTable externalTable = new ExternalMapperTable();
                setExternalTable(externalTable, pTable);
                externalTable.setReject(pTable.isReject());
                externalTable.setRejectInnerJoin(pTable.isRejectInnerJoin());
                externalTable.setErrorRejectTable(pTable.isIsErrorRejectTable());
                externalTable.setIsJoinTableOf(pTable.getIsJoinTableOf());
                externalTables.add(externalTable);
            }
            externalData.setOutputTables(externalTables);
            // var tables
            externalTables = new ArrayList<ExternalMapperTable>();
            for (VarTable varTable : mapperData.getVarTables()) {
                ExternalMapperTable externalTable = new ExternalMapperTable();
                externalTable.setSizeState(varTable.getSizeState().getLiteral());
                externalTable.setMinimized(varTable.isMinimized());
                externalTable.setName(varTable.getName());
                externalTable.setMetadataTableEntries(getExternalEntities(varTable.getMapperTableEntries()));
                externalTables.add(externalTable);
            }
            externalData.setVarsTables(externalTables);
            ExternalMapperUiProperties uiProperties = new ExternalMapperUiProperties();
            UiProperties uiPro = mapperData.getUiProperties();
            if (uiPro != null) {
                uiProperties.setShellMaximized(uiPro.isShellMaximized());
            }
        }
        this.setExternalData(externalData);
    }

    // set common attribute for input output external table
    private void setExternalTable(ExternalMapperTable externalTable, AbstractInOutTable pTable) {
        externalTable.setActivateCondensedTool(pTable.isActivateCondensedTool());
        externalTable.setActivateExpressionFilter(pTable.isActivateExpressionFilter());
        externalTable.setActivateColumnNameFilter(pTable.isActivateColumnNameFilter());
        externalTable.setExpressionFilter(pTable.getExpressionFilter());
        externalTable.setColumnNameFilter(pTable.getColumnNameFilter());
        externalTable.setSizeState(pTable.getSizeState().getLiteral());
        externalTable.setMinimized(pTable.isMinimized());
        externalTable.setName(pTable.getName());
        externalTable.setMetadataTableEntries(getExternalEntities(pTable.getMapperTableEntries()));
        externalTable.setId(pTable.getId());
    }

    private List<ExternalMapperTableEntry> getExternalEntities(List<MapperTableEntry> pEntities) {
        List<ExternalMapperTableEntry> entityList = new ArrayList<ExternalMapperTableEntry>();
        if (pEntities != null && !pEntities.isEmpty()) {
            for (MapperTableEntry pEntity : pEntities) {
                ExternalMapperTableEntry externalEntity = new ExternalMapperTableEntry();
                externalEntity.setExpression(pEntity.getExpression());
                externalEntity.setName(pEntity.getName());
                externalEntity.setNullable(pEntity.isNullable());
                externalEntity.setType(pEntity.getType());
                externalEntity.setOperator(pEntity.getOperator());
                entityList.add(externalEntity);
            }
            return entityList;
        } else {
            // fix for tuj tMap_01_basics_04 , need to return null if no entity
            return null;
        }
    }

    @Override
    public AbstractExternalData getExternalEmfData() {
        final MapperData emfMapperData = MapperFactory.eINSTANCE.createMapperData();
        initMapperMain(false);
        mapperMain.createModelFromExternalData(getIncomingConnections(), getOutgoingConnections(), externalData,
                getMetadataList(), false);
        ExternalMapperData data = mapperMain.buildExternalData();
        if (mapperMain != null && data != null) {
            if (externalData != null) {
                if(!isConnectionEmpty()) {
                    this.externalData = data;// fwang fixed bug TDI-8027
                }
                MapperHelper.saveDataToEmf(data, emfMapperData);
            }
        }
        return emfMapperData;
    }

    private boolean isConnectionEmpty() {
        return getIncomingConnections().isEmpty() && getOutgoingConnections().isEmpty();
    }

    public void renameInputConnection(String oldConnectionName, String newConnectionName) {
        if (oldConnectionName == null || newConnectionName == null) {
            throw new NullPointerException();
        }
        if (externalData != null) {
            List<ExternalMapperTable> inputTables = externalData.getInputTables();
            for (ExternalMapperTable table : inputTables) {
                if (table.getName().equals(oldConnectionName)) {
                    table.setName(newConnectionName);
                    TableEntryLocation oldLocation = new TableEntryLocation(oldConnectionName, null);
                    TableEntryLocation newLocation = new TableEntryLocation(newConnectionName, null);
                    replaceLocationsInAllExpressions(oldLocation, newLocation, true);
                    break;
                }
            }
        }
    }

    public void renameOutputConnection(String oldName, String newName) {
        if (oldName == null || newName == null) {
            throw new NullPointerException();
        }
        if (externalData != null) {
            List<ExternalMapperTable> outputTables = externalData.getOutputTables();
            for (ExternalMapperTable table : outputTables) {
                if (table.getName().equals(oldName)) {
                    table.setName(newName);
                } else if (table.getIsJoinTableOf() != null && table.getIsJoinTableOf().equals(oldName)) {
                    table.setIsJoinTableOf(newName);
                }
            }
        }
    }

    @Override
    protected void renameMetadataColumnName(String conectionName, String oldColumnName, String newColumnName) {
        if (conectionName == null || oldColumnName == null || newColumnName == null) {
            throw new NullPointerException();
        }
        if (externalData != null) {
            // rename metadata column name
            List<ExternalMapperTable> tables = new ArrayList<ExternalMapperTable>(externalData.getInputTables());
            tables.addAll(externalData.getOutputTables());
            ExternalMapperTable tableFound = null;
            for (ExternalMapperTable table : tables) {
                if (table.getName().equals(conectionName)) {
                    List<ExternalMapperTableEntry> metadataTableEntries = table.getMetadataTableEntries();
                    for (ExternalMapperTableEntry entry : metadataTableEntries) {
                        if (entry.getName().equals(oldColumnName)) {
                            entry.setName(newColumnName);
                            tableFound = table;
                            break;
                        }
                    }
                    break;
                }
            }

            // it is necessary to update expressions only if renamed column come from input table
            if (tableFound != null && externalData.getInputTables().indexOf(tableFound) != -1) {
                TableEntryLocation oldLocation = new TableEntryLocation(conectionName, oldColumnName);
                TableEntryLocation newLocation = new TableEntryLocation(conectionName, newColumnName);
                replaceLocationsInAllExpressions(oldLocation, newLocation, false);
            }

        }
    }

    /**
     * DOC amaumont Comment method "replaceLocations".
     *
     * @param oldLocation
     * @param newLocation
     * @param tableRenamed TODO
     * @param newTableName
     * @param newColumnName
     */
    private void replaceLocationsInAllExpressions(TableEntryLocation oldLocation, TableEntryLocation newLocation,
            boolean tableRenamed) {
        // replace old location by new location for all expressions in mapper
        List<ExternalMapperTable> tables = new ArrayList<ExternalMapperTable>(externalData.getInputTables());
        tables.addAll(new ArrayList<ExternalMapperTable>(externalData.getVarsTables()));
        tables.addAll(new ArrayList<ExternalMapperTable>(externalData.getOutputTables()));
        DataMapExpressionParser dataMapExpressionParser = new DataMapExpressionParser(LanguageProvider.getCurrentLanguage());
        // loop on all tables
        for (ExternalMapperTable table : tables) {
            List<ExternalMapperTableEntry> metadataTableEntries = table.getMetadataTableEntries();

            String expressionFilter = replaceLocation(oldLocation, newLocation, table.getExpressionFilter(),
                    dataMapExpressionParser, tableRenamed);
            if (expressionFilter != null) {
                table.setExpressionFilter(expressionFilter);
            }

            if (metadataTableEntries != null) {
                // loop on all entries of current table
                for (ExternalMapperTableEntry entry : metadataTableEntries) {
                    String expression = replaceLocation(oldLocation, newLocation, entry.getExpression(), dataMapExpressionParser,
                            tableRenamed);
                    if (expression != null) {
                        entry.setExpression(expression);
                    }
                } // for (ExternalMapperTableEntry entry : metadataTableEntries) {
            }
            if (table.getConstraintTableEntries() != null) {
                for (ExternalMapperTableEntry entry : table.getConstraintTableEntries()) {
                    String expression = replaceLocation(oldLocation, newLocation, entry.getExpression(), dataMapExpressionParser,
                            tableRenamed);
                    if (expression != null) {
                        entry.setExpression(expression);
                    }
                }
            }
            if (table.getGlobalMapKeysValues() != null) {
                for (ExternalMapperTableEntry entry : table.getGlobalMapKeysValues()) {
                    String expression = replaceLocation(oldLocation, newLocation, entry.getExpression(), dataMapExpressionParser,
                            tableRenamed);
                    if (expression != null) {
                        entry.setExpression(expression);
                    }
                }
            }
        } // for (ExternalMapperTable table : tables) {
    }

    public String replaceLocation(TableEntryLocation oldLocation, TableEntryLocation newLocation, String currentExpression,
            DataMapExpressionParser dataMapExpressionParser, boolean tableRenamed) {
        if (currentExpression == null || currentExpression.trim().length() == 0) {
            return null;
        }
        TableEntryLocation[] tableEntryLocations = dataMapExpressionParser.parseTableEntryLocations(currentExpression);
        // loop on all locations of current expression
        for (TableEntryLocation currentLocation : tableEntryLocations) {
            if (tableRenamed && oldLocation.tableName.equals(currentLocation.tableName)) {
                oldLocation.columnName = currentLocation.columnName;
                newLocation.columnName = currentLocation.columnName;
            }
            if (currentLocation.equals(oldLocation)) {
                currentExpression = dataMapExpressionParser.replaceLocation(currentExpression, currentLocation, newLocation);
            }
        } // for (int i = 0; i < tableEntryLocations.length; i++) {
        return currentExpression;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#getProblems()
     */
    @Override
    public List<Problem> getProblems() {
        initMapperMain(false);
        ProblemsAnalyser problemsAnalyser = new ProblemsAnalyser(mapperMain.getMapperManager());
        return problemsAnalyser.checkProblems(externalData);
    }

    /**
     * Getter for mapperMain.
     *
     * @return the mapperMain
     */
    public MapperMain getMapperMain() {
        return this.mapperMain;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#getComponentDocumentation(java.lang.String, java.lang.String)
     */
    public IComponentDocumentation getComponentDocumentation(String componentLabel, String tempFolderPath) {
        MapperComponentDocumentation componentDocumentation = new MapperComponentDocumentation();
        componentDocumentation.setComponentName(componentLabel);
        componentDocumentation.setTempFolderPath(tempFolderPath);
        componentDocumentation.setExternalData(this.externalData);
        componentDocumentation.setPreviewPicPath(HTMLDocUtils.getPreviewPicPath(this));
        componentDocumentation.setExternalNode(getExternalNode());

        return componentDocumentation;
    }

    public GenerationManager initGenerationManager() {
        this.generationManager = GenerationManagerFactory.getInstance().getGenerationManager(
                LanguageProvider.getCurrentLanguage());
        return this.generationManager;
    }

    public GenerationManager getGenerationManager() {
        return this.generationManager;
    }

    @Override
    public List<BlockCode> getBlocksCodeToClose() {
        if (generationManager == null) {
            throw new IllegalStateException(Messages.getString("MapperComponent.generationNotInitial")); //$NON-NLS-1$
        }
        return this.generationManager.getBlocksCodeToClose();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IHashableInputConnections#getHashConfiguration(java.lang.String)
     */
    public IHashConfiguration getHashConfiguration(String connectionName) {

        IHashConfiguration hashConfigurationForMapper = null;
        ExternalMapperData externalData = (ExternalMapperData) getExternalData();
        List<ExternalMapperTable> inputTables = externalData.getInputTables();
        List<IHashableColumn> hashableColumns = new ArrayList<IHashableColumn>();
        for (ExternalMapperTable inputTable : inputTables) {
            if (inputTable.getName().equals(connectionName)) {
                List<ExternalMapperTableEntry> metadataTableEntries = inputTable.getMetadataTableEntries();
                if (metadataTableEntries != null) {
                    int metadataTableEntriesListSize = metadataTableEntries.size();
                    for (int i = 0; i < metadataTableEntriesListSize; i++) {
                        ExternalMapperTableEntry entry = metadataTableEntries.get(i);
                        if (entry.getExpression() != null && !entry.getExpression().trim().equals("")) { //$NON-NLS-1$
                            hashableColumns.add(new HashableColumn(entry.getName(), i));
                        }
                    }
                }

                IMatchingMode matchingMode = MATCHING_MODE.parse(inputTable.getMatchingMode());
                if (matchingMode == null) {
                    matchingMode = MATCHING_MODE.UNIQUE_MATCH;
                }

                ILookupMode lookupMode = LOOKUP_MODE.parse(inputTable.getLookupMode());
                if (lookupMode == null) {
                    lookupMode = LOOKUP_MODE.LOAD_ONCE;
                }

                IElementParameter tempFolderElem = getElementParameter("TEMPORARY_DATA_DIRECTORY"); //$NON-NLS-1$
                // modified by wzhang to fix 7824
                String tempFolder = null;
                if (tempFolderElem != null) {
                    tempFolder = (String) tempFolderElem.getValue();
                }
                if (("").equals(tempFolder)) {
                    tempFolder = (String) this.getProcess().getElementParameter("COMP_DEFAULT_FILE_DIR").getValue() + "/temp"; //$NON-NLS-1$ //$NON-NLS-2$
                    tempFolder = TalendTextUtils.addQuotes(tempFolder);
                }

                IElementParameter rowsBufferSizeElem = getElementParameter("ROWS_BUFFER_SIZE"); //$NON-NLS-1$
                String rowsBufferSize = null;
                if (rowsBufferSizeElem != null) {
                    rowsBufferSize = (String) rowsBufferSizeElem.getValue();
                }
                hashConfigurationForMapper = new HashConfiguration(hashableColumns, matchingMode, inputTable.isPersistent(),
                        tempFolder, rowsBufferSize);
                break;
            }
        }

        return hashConfigurationForMapper;
    }

    /**
     *
     * DOC amaumont Comment method "hasOrRenameData".
     *
     * @param oldName
     * @param newName can be null if <code>renameAction</code> is false
     * @param renameAction true to rename in all expressions, false to get boolean if present in one of the expressions
     * @return
     */
    @Override
    protected boolean hasOrRenameData(String oldName, String newName, boolean renameAction) {
        if (oldName == null || newName == null && renameAction) {
            throw new NullPointerException();
        }

        if (externalData != null) {
            List<ExternalMapperTable> tables = new ArrayList<ExternalMapperTable>(externalData.getInputTables());
            tables.addAll(externalData.getOutputTables());
            if (externalData.getVarsTables() != null) {
                tables.addAll(externalData.getVarsTables());
            }

            for (ExternalMapperTable table : tables) {

                if (table.getExpressionFilter() != null) {
                    Pattern pattern = getRenamePattern(oldName);
                    if (pattern != null) {
                        PatternMatcher matcher = new Perl5Matcher();
                        ((Perl5Matcher) matcher).setMultiline(true);

                        if (renameAction) {
                            Perl5Substitution substitution = getRenameSubstitution(newName);
                            String expression = renameDataIntoExpression(pattern, matcher, substitution,
                                    table.getExpressionFilter());
                            table.setExpressionFilter(expression);
                        } else {
                            if (hasDataIntoExpression(pattern, matcher, table.getExpressionFilter())) {
                                return true;
                            }
                        }
                    }
                }

                List<ExternalMapperTableEntry> metadataTableEntries = table.getMetadataTableEntries();
                if (metadataTableEntries != null) {
                    // loop on all entries of current table
                    for (ExternalMapperTableEntry entry : metadataTableEntries) {
                        if (hasOrRenameEntry(entry, oldName, newName, renameAction)) {
                            return true; // existed
                        }
                    } // for (ExternalMapperTableEntry entry : metadataTableEntries) {
                }
                if (table.getConstraintTableEntries() != null) {
                    for (ExternalMapperTableEntry entry : table.getConstraintTableEntries()) {
                        if (hasOrRenameEntry(entry, oldName, newName, renameAction)) {
                            return true; // existed
                        }
                    }
                }

            }

        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#isRunRefSubProcessAtStart(java.lang.String)
     */
    @Override
    public boolean isRunRefSubProcessAtStart(String connectionName) {
        if (externalData != null) {
            List<ExternalMapperTable> tables = new ArrayList<ExternalMapperTable>(externalData.getInputTables());
            for (ExternalMapperTable table : tables) {
                if (table.getName().equals(connectionName)) {
                    String lookupMode = table.getLookupMode();
                    if (TMAP_LOOKUP_MODE.RELOAD.name().equals(lookupMode)
                            || TMAP_LOOKUP_MODE.CACHE_OR_RELOAD.name().equals(lookupMode)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isUseLoopOnConditionalOutput(java.lang.String)
     */
    @Override
    public boolean isUseLoopOnConditionalOutput(String outputName) {
        for (ExternalMapperTable table : externalData.getOutputTables()) {
            if (table.getIsJoinTableOf() != null && table.getIsJoinTableOf().equals(outputName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> checkNeededRoutines(List<String> possibleRoutines, String additionalString) {
        List<String> routinesToAdd = new ArrayList<String>();
        for (String routine : possibleRoutines) {
            List<ExternalMapperTable> listOutput = (List<ExternalMapperTable>) this.getExternalData().getOutputTables();
            for (ExternalMapperTable outTable : listOutput) {
                List<IExternalMapEntry> listOutEntry = (List<IExternalMapEntry>) outTable.returnTableEntries();
                if (listOutEntry != null && !listOutEntry.isEmpty()) {
                    for (IExternalMapEntry outEntry : listOutEntry) {
                        String expression = outEntry.getExpression();
                        if (expression != null && !routinesToAdd.contains(routine)
                                && expression.contains(routine + additionalString)) {
                            routinesToAdd.add(routine);
                        }
                    }
                }
            }
            List<ExternalMapperTable> listInput = (List<ExternalMapperTable>) this.getExternalData().getInputTables();
            for (ExternalMapperTable inputTable : listInput) {
                List<IExternalMapEntry> listInEntry = (List<IExternalMapEntry>) inputTable.returnTableEntries();
                if (listInEntry != null && !listInEntry.isEmpty()) {
                    for (IExternalMapEntry inEntry : listInEntry) {
                        String expression = inEntry.getExpression();
                        if (expression != null && !routinesToAdd.contains(routine)
                                && expression.contains(routine + additionalString)) {
                            routinesToAdd.add(routine);
                        }
                    }
                }
            }
            List<ExternalMapperTable> listVar = (List<ExternalMapperTable>) this.getExternalData().getVarsTables();
            for (IExternalMapTable varTable : listVar) {
                List<IExternalMapEntry> listVarEntry = (List<IExternalMapEntry>) varTable.returnTableEntries();
                if (listVarEntry != null && !listVarEntry.isEmpty()) {
                    for (IExternalMapEntry varEntry : listVarEntry) {
                        String expression = varEntry.getExpression();
                        if (expression != null && !routinesToAdd.contains(routine)
                                && expression.contains(routine + additionalString)) {
                            routinesToAdd.add(routine);
                        }
                    }
                }
            }
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerMapperService.class)) {
                IDesignerMapperService service = (IDesignerMapperService) GlobalServiceRegister.getDefault().getService(
                        IDesignerMapperService.class);
                List<String> experssionFilters = service.getExpressionFilter(this.getExternalData());
                if (!experssionFilters.isEmpty()) {
                    for (String experssion : experssionFilters) {
                        if (experssion != null && !routinesToAdd.contains(routine)
                                && experssion.contains(routine + additionalString)) {
                            routinesToAdd.add(routine);
                        }
                    }
                }
            }
        }
        return routinesToAdd;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#getIODataComponents()
     */
    @Override
    public IODataComponentContainer getIODataComponents() {
        // if metadata exist in IO + metadatalist, just update the instance of the one in IO.
        // check if there is some table added / deleted as well. (correct one is in metadatalist)
        List<IODataComponent> listOutput = super.getIODataComponents().getOuputs();
        List<IMetadataTable> metadataTableList = getMetadataList();
        for (IODataComponent ioComponent : listOutput) {
            String tableName = ioComponent.getNewMetadataTable().getTableName();
            for (IMetadataTable table : metadataTableList) {
                if (tableName != null && tableName.equals(table.getTableName())) {
                    ioComponent.setNewMetadataTable(table);
                    break;
                }
            }
        }
        return super.getIODataComponents();
    }

    public void loadDatasetConditions(boolean isJobValidForDataset) {
        this.shouldGenerateDatasetCode = isDatasetCompatible(isJobValidForDataset);
    }

    public boolean isDatasetCompatible(boolean isJobValidForDataset) {
        //spark 2.0 and batch
        if (!isJobValidForDataset) {
            return false;
        }

        //only two input connections
        if (this.externalData.getInputTables().size() != 2) {
            return false;
        } // one connection must be all matches
        else if (!isAtLeastOneInputTableAllMatch(this.externalData)) {
            return false;
        }

        if (externalData != null) {
            // Output should not have filter until its implemented in spark dataset lib
            boolean res = externalData.getOutputTables().stream()
                    .anyMatch(t -> t.getExpressionFilter() != null);
            if (res) {
                return false;
            }
        }

        //only one output, can be equal to 0 when graphically adding component so we avoid NPE
        if (this.externalData.getOutputTables().size() != 1) {
            return false;
        } //no rejects
        else if (this.externalData.getOutputTables().get(0).isRejectInnerJoin()
                || this.externalData.getOutputTables().get(0).isReject()) {
            return false;
        }
        return true;
    }


    private boolean isAtLeastOneInputTableAllMatch(ExternalMapperData data) {
        return "ALL_MATCHES".equals(data.getInputTables().get(0).getMatchingMode()) || "ALL_MATCHES".equals(data.getInputTables().get(1).getMatchingMode());
    }

    @Override
    public boolean getShouldGenerateDataset() {
        return this.shouldGenerateDatasetCode;
    }
}
