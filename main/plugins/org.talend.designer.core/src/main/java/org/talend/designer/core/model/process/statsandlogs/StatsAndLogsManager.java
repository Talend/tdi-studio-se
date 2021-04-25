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
package org.talend.designer.core.model.process.statsandlogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.DatabaseConnStrUtil;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolAvroHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.DataConnection;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class StatsAndLogsManager {

    public static final String TPREJOB = "tPrejob"; //$NON-NLS-1$

    public static final String PRE_STA_LOG_CON = "preStaLogCon"; //$NON-NLS-1$

    private static final String LOG_UNIQUE_NAME = "talendLogs"; //$NON-NLS-1$

    private static final String STAT_UNIQUE_NAME = "talendStats"; //$NON-NLS-1$

    private static final String METER_UNIQUE_NAME = "talendMeter"; //$NON-NLS-1$

    public static final String ENCODING_TYPE_UTF_8 = "UTF-8"; //$NON-NLS-1$

    public static final String ENCODING_TYPE_ISO_8859_15 = "ISO-8859-15"; //$NON-NLS-1$

    public static final String ENCODING_TYPE_CUSTOM = "CUSTOM"; //$NON-NLS-1$

    public static final String CONNECTION_UID = "connectionStatsLogs";//$NON-NLS-1$

    private static List<String> moduleNameList;

    private static List<String> moduleValueList;

    public static boolean isStatsAndLogsActivated(IProcess process) {
        String dbOutput = null;
        boolean dbFlag = ((Boolean) process.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName()).isShow(process.getElementParameters());
        if (!dbFlag) {
            dbOutput = null;
        } else {
            dbOutput = (String) process.getElementParameter(EParameterName.DB_TYPE.getName()).getValue();
            if (dbOutput == null || dbOutput.equals("")) { //$NON-NLS-1$
                dbOutput = null;
                dbFlag = false;
            }
        }
        boolean file = ((Boolean) process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).isShow(process.getElementParameters());

        boolean console = ((Boolean) process.getElementParameter(EParameterName.ON_CONSOLE_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_CONSOLE_FLAG.getName()).isShow(process.getElementParameters());

        if (!file && !dbFlag && !console) {
            return false;
        }
        return true;
    }

    public static List<DataNode> getStatsAndLogsNodes(IProcess process) {
        List<DataNode> nodeList = new ArrayList<DataNode>();

        String dbOutput = null;

        boolean dbFlag = ((Boolean) process.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName()).isShow(process.getElementParameters());
        if (!dbFlag) {
            dbOutput = null;
        } else {
            dbOutput = (String) process.getElementParameter(EParameterName.DB_TYPE.getName()).getValue();
            dbOutput = OracleComponentHelper.filterOracleComponentName(dbOutput);
            if (dbOutput == null || dbOutput.equals("")) { //$NON-NLS-1$
                dbOutput = null;
                dbFlag = false;
            }
        }
        boolean file = ((Boolean) process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).isShow(process.getElementParameters());

        boolean console = ((Boolean) process.getElementParameter(EParameterName.ON_CONSOLE_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_CONSOLE_FLAG.getName()).isShow(process.getElementParameters());

        if (!file && !dbFlag && !console) {
            return nodeList;
        }

        boolean useStats = ((Boolean) process.getElementParameter(EParameterName.ON_STATCATCHER_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_STATCATCHER_FLAG.getName())
                        .isShow(process.getElementParameters());

        boolean useLogs = ((Boolean) process.getElementParameter(EParameterName.ON_LOGCATCHER_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_LOGCATCHER_FLAG.getName())
                        .isShow(process.getElementParameters());

        boolean useMetter = ((Boolean) process.getElementParameter(EParameterName.ON_METERCATCHER_FLAG.getName()).getValue())
                && process.getElementParameter(EParameterName.ON_METERCATCHER_FLAG.getName())
                        .isShow(process.getElementParameters());

        String basePath = (String) process.getElementParameter(EParameterName.FILE_PATH.getName()).getValue();
        basePath = basePath.replace("\\", "/") + "+ \"/\" +"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        DataNode connectionNode = null;
        // String connectionUID = "connectionStatsLogs";//$NON-NLS-1$

        DataNode commitNode = null;
        String connectionUID2;
        IComponent commitComponent = null;

        String subString = null;
        /*
         * maybe, need create every of committing node for log/stat/metter.
         */

        // for bug 10453
        boolean isNotInformixDB = true;
        if (dbFlag) {
            String[] javaDbComponents = StatsAndLogsConstants.DB_OUTPUT_COMPONENTS;
            for (String dbComponent : javaDbComponents) {
                String commitComponentName = null;
                if (OracleComponentHelper
                        .filterOracleConnectionType(
                                (String) process.getElementParameter(EParameterName.DB_TYPE.getName()).getValue())
                        .equals(dbComponent)) {
                    if (dbComponent.endsWith("Output")) {//$NON-NLS-1$
                        subString = dbComponent.substring(0, dbComponent.lastIndexOf("Output"));//$NON-NLS-1$
                        commitComponentName = subString + "Commit";//$NON-NLS-1$
                    } else {
                        commitComponentName = "tOracleCommit";//$NON-NLS-1$
                    }
                    commitComponent = ComponentsFactoryProvider.getInstance().get(commitComponentName,
                            ComponentCategory.CATEGORY_4_DI.getName());
                    if (commitComponentName.indexOf("Informix") != -1) {
                        isNotInformixDB = false;
                    }
                    if (commitComponent != null) {
                        connectionUID2 = CONNECTION_UID + "_Commit";//$NON-NLS-1$
                        commitNode = new DataNode(commitComponent, connectionUID2);
                        commitNode.setSubProcessStart(true);
                        commitNode.setActivate(true);
                        boolean isGeneric = commitNode.getComponent().getComponentType() == EComponentType.GENERIC;
                        IElementParameter param = commitNode.getElementParameter(EParameterName.CONNECTION.getName());
                        if (param == null && isGeneric) {
                            param = commitNode.getElementParameter("referencedComponent"); //$NON-NLS-1$
                        }
                        if (param != null) {
                            param.setValue(CONNECTION_UID);
                        }
                        IElementParameter elementParameter = commitNode.getElementParameter("CLOSE");//$NON-NLS-1$
                        if (elementParameter == null && isGeneric) {
                            elementParameter = commitNode.getElementParameter("closeConnection");//$NON-NLS-1$
                        }
                        if (elementParameter != null) {
                            elementParameter.setValue(Boolean.FALSE);
                        }

                        commitNode.setProcess(process);
                        nodeList.add(commitNode);
                    }
                }
            }
        }

        if (useStats) {
            IComponent dbOutputComponent = dbOutput != null
                    ? ComponentsFactoryProvider.getInstance().get(dbOutput, process.getComponentsType())
                    : null;
            boolean tcomp_jdbcoutput = (dbOutputComponent != null)
                    && (dbOutputComponent.getComponentType() == EComponentType.GENERIC);
            if (tcomp_jdbcoutput) {
                connectionNode = addGenericDataNodes(process, nodeList, connectionNode, commitNode, dbOutputComponent,
                        "tStatCatcher", basePath, file, console);
            } else {
                DataNode statsNode = createStatsNode(file, console, dbOutput);
                if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
                    statsNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                            "File::Spec->catfile(" + basePath + "," //$NON-NLS-1$ //$NON-NLS-2$
                                    + process.getElementParameter(EParameterName.FILENAME_STATS.getName()).getValue() + ")"); //$NON-NLS-1$
                } else {
                    statsNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                            basePath + process.getElementParameter(EParameterName.FILENAME_STATS.getName()).getValue());
                }
                if (dbFlag) {
                    if (commitNode != null && isNotInformixDB) {
                        connectionNode = addConnection(connectionNode, process, CONNECTION_UID, statsNode, nodeList, commitNode);
                    } else {
                        useNoConnectionComponentDB(statsNode, process, CONNECTION_UID);
                    }
                    statsNode.getElementParameter("TABLE").setValue(//$NON-NLS-1$
                            process.getElementParameter(EParameterName.TABLE_STATS.getName()).getValue());
                }

                if (file) {
                    IElementParameter encodingParameter = process.getElementParameter(EParameterName.ENCODING.getName());
                    if (encodingParameter != null) {
                        Object value = encodingParameter.getValue();
                        if (value != null && !"".equals(value)) {
                            IElementParameter elementParameter = statsNode.getElementParameter(EParameterName.ENCODING.getName());
                            if (elementParameter != null) {
                                String encoding = value.toString();
                                if (!value.toString().startsWith(TalendTextUtils.getQuoteChar())) {
                                    encoding = TalendTextUtils.addQuotes(encoding);
                                }
                                elementParameter.setValue(encoding);
                            }
                        }
                    }
                }
                statsNode.setProcess(process);
                nodeList.add(statsNode);
            }
        }

        if (useLogs) {
            IComponent dbOutputComponent = dbOutput != null
                    ? ComponentsFactoryProvider.getInstance().get(dbOutput, process.getComponentsType())
                    : null;
            boolean tcomp_jdbcoutput = (dbOutputComponent != null)
                    && (dbOutputComponent.getComponentType() == EComponentType.GENERIC);
            if (tcomp_jdbcoutput) {
                connectionNode = addGenericDataNodes(process, nodeList, connectionNode, commitNode, dbOutputComponent,
                        "tLogCatcher", basePath, file, console);
            } else {
                DataNode logsNode = createLogsNode(file, console, dbOutput);
                if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
                    logsNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                            "File::Spec->catfile(" + basePath + "," //$NON-NLS-1$ //$NON-NLS-2$
                                    + process.getElementParameter(EParameterName.FILENAME_LOGS.getName()).getValue() + ")"); //$NON-NLS-1$
                } else {
                    logsNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                            basePath + process.getElementParameter(EParameterName.FILENAME_LOGS.getName()).getValue());
                }
                if (dbFlag) {
                    if (commitNode != null && isNotInformixDB) {
                        connectionNode = addConnection(connectionNode, process, CONNECTION_UID, logsNode, nodeList, commitNode);
                    } else {
                        useNoConnectionComponentDB(logsNode, process, CONNECTION_UID);
                    }
                    logsNode.getElementParameter("TABLE").setValue(//$NON-NLS-1$
                            process.getElementParameter(EParameterName.TABLE_LOGS.getName()).getValue());
                }
                if (file) {
                    IElementParameter encodingParameter = process.getElementParameter(EParameterName.ENCODING.getName());
                    if (encodingParameter != null) {
                        Object value = encodingParameter.getValue();
                        if (value != null && !"".equals(value)) {
                            IElementParameter elementParameter = logsNode.getElementParameter(EParameterName.ENCODING.getName());
                            if (elementParameter != null) {
                                String encoding = value.toString();
                                if (!value.toString().startsWith(TalendTextUtils.getQuoteChar())) {
                                    encoding = TalendTextUtils.addQuotes(encoding);
                                }
                                elementParameter.setValue(encoding);
                            }
                        }
                    }
                }

                if (logsNode.getElementParameter(EParameterName.CATCH_RUNTIME_ERRORS.getName()) != null) {
                    logsNode.getElementParameter(EParameterName.CATCH_RUNTIME_ERRORS.getName())
                            .setValue(process.getElementParameter(EParameterName.CATCH_RUNTIME_ERRORS.getName()).getValue());
                }
                if (logsNode.getElementParameter(EParameterName.CATCH_USER_ERRORS.getName()) != null) {
                    logsNode.getElementParameter(EParameterName.CATCH_USER_ERRORS.getName())
                            .setValue(process.getElementParameter(EParameterName.CATCH_USER_ERRORS.getName()).getValue());
                }
                if (logsNode.getElementParameter(EParameterName.CATCH_USER_WARNING.getName()) != null) {
                    logsNode.getElementParameter(EParameterName.CATCH_USER_WARNING.getName())
                            .setValue(process.getElementParameter(EParameterName.CATCH_USER_WARNING.getName()).getValue());
                }

                logsNode.setProcess(process);
                nodeList.add(logsNode);
            }
        }

        if (useMetter) {
            IComponent dbOutputComponent = dbOutput != null
                    ? ComponentsFactoryProvider.getInstance().get(dbOutput, process.getComponentsType())
                    : null;
            boolean tcomp_jdbcoutput = (dbOutputComponent != null)
                    && (dbOutputComponent.getComponentType() == EComponentType.GENERIC);
            if (tcomp_jdbcoutput) {
                connectionNode = addGenericDataNodes(process, nodeList, connectionNode, commitNode, dbOutputComponent,
                        "tFlowMeterCatcher", basePath, file, console);
            } else {
                DataNode meterNode = createMetterNode(file, console, dbOutput);
                if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
                    meterNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                            "File::Spec->catfile(" + basePath + "," //$NON-NLS-1$ //$NON-NLS-2$
                                    + process.getElementParameter(EParameterName.FILENAME_METTER.getName()).getValue() + ")"); //$NON-NLS-1$
                } else {
                    meterNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                            basePath + process.getElementParameter(EParameterName.FILENAME_METTER.getName()).getValue());
                }
                if (dbFlag) {
                    if (commitNode != null && isNotInformixDB) {
                        connectionNode = addConnection(connectionNode, process, CONNECTION_UID, meterNode, nodeList, commitNode);
                    } else {
                        useNoConnectionComponentDB(meterNode, process, CONNECTION_UID);
                    }
                    meterNode.getElementParameter("TABLE").setValue(//$NON-NLS-1$
                            process.getElementParameter(EParameterName.TABLE_METER.getName()).getValue());
                }

                if (file) {
                    IElementParameter encodingParameter = process.getElementParameter(EParameterName.ENCODING.getName());
                    if (encodingParameter != null) {
                        Object value = encodingParameter.getValue();
                        if (value != null && !"".equals(value)) {
                            IElementParameter elementParameter = meterNode.getElementParameter(EParameterName.ENCODING.getName());
                            if (elementParameter != null) {
                                String encoding = value.toString();
                                if (!value.toString().startsWith(TalendTextUtils.getQuoteChar())) {
                                    encoding = TalendTextUtils.addQuotes(encoding);
                                }
                                elementParameter.setValue(encoding);
                            }
                        }
                    }
                }

                meterNode.setProcess(process);
                nodeList.add(meterNode);
            }
        }

        return nodeList;
    }

    private static DataNode addGenericDataNodes(IProcess process, List<DataNode> nodeList, DataNode connectionNode,
            DataNode commitNode, IComponent dbOutputComponent, String inputComponentName, String basePath, boolean file,
            boolean console) {
        String prefixName = null;
        String inputComponentId = null;
        String dbTableParameterName = null;
        boolean catchStats = false;
        boolean catchLogs = false;
        boolean catchMetters = false;
        if ("tStatCatcher".equals(inputComponentName)) {
            prefixName = STAT_UNIQUE_NAME;
            inputComponentId = "STATS";
            dbTableParameterName = EParameterName.TABLE_STATS.getName();
            catchStats = true;
        } else if ("tLogCatcher".equals(inputComponentName)) {
            prefixName = LOG_UNIQUE_NAME;
            inputComponentId = "LOGS";
            dbTableParameterName = EParameterName.TABLE_LOGS.getName();
            catchLogs = true;
        } else if ("tFlowMeterCatcher".equals(inputComponentName)) {
            prefixName = METER_UNIQUE_NAME;
            inputComponentId = "METTER";
            dbTableParameterName = EParameterName.TABLE_METER.getName();
            catchMetters = true;
        }

        DataNode catcherNode = createGenericInputNode(inputComponentName, prefixName, inputComponentId, process);
        catcherNode.getMetadataList().clear();
        IMetadataTable sourceMetadataTable = null;
        for (int k = 0; k < catcherNode.getElementParameters().size(); k++) {
            IElementParameter currentParam = catcherNode.getElementParameters().get(k);
            if (currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                Object value = currentParam.getValue();
                if (value instanceof IMetadataTable) {
                    sourceMetadataTable = ((IMetadataTable) value).clone();
                    sourceMetadataTable.setTableName(catcherNode.getUniqueName());
                    sourceMetadataTable.setAttachedConnector(currentParam.getContext());
                    catcherNode.getMetadataList().add(sourceMetadataTable);
                    break;
                }
            }
        }
        nodeList.add(catcherNode);
        DataNode inputNode = catcherNode;
        if (catchLogs) {
            // please see JobLogsComponent class
            if (inputNode.getElementParameter("CATCH_JAVA_EXCEPTION") != null) {
                inputNode.getElementParameter("CATCH_JAVA_EXCEPTION")
                        .setValue(process.getElementParameter(EParameterName.CATCH_RUNTIME_ERRORS.getName()).getValue());
            }
            if (inputNode.getElementParameter("CATCH_TDIE") != null) {
                inputNode.getElementParameter("CATCH_TDIE")
                        .setValue(process.getElementParameter(EParameterName.CATCH_USER_ERRORS.getName()).getValue());
            }
            if (inputNode.getElementParameter("CATCH_TWARN") != null) {
                inputNode.getElementParameter("CATCH_TWARN")
                        .setValue(process.getElementParameter(EParameterName.CATCH_USER_WARNING.getName()).getValue());
            }
        }

        if (file) {
            DataNode fileNode = createGenericFileNode(inputNode, sourceMetadataTable, prefixName, process);
            if (catchStats) {
                fileNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                        basePath + process.getElementParameter(EParameterName.FILENAME_STATS.getName()).getValue());
            } else if (catchLogs) {
                fileNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                        basePath + process.getElementParameter(EParameterName.FILENAME_LOGS.getName()).getValue());
            } else if (catchMetters) {
                fileNode.getElementParameter("FILENAME").setValue(//$NON-NLS-1$
                        basePath + process.getElementParameter(EParameterName.FILENAME_METTER.getName()).getValue());
            }
            nodeList.add(fileNode);
            inputNode = fileNode;
        }

        DataConnection dataConnec = new DataConnection();
        dataConnec.setMetadataTable(sourceMetadataTable);

        DataNode dbOutputNode = new DataNode(dbOutputComponent, prefixName + "_" + "DB");
        dbOutputNode.setStart(false);
        dbOutputNode.setSubProcessStart(false);
        dbOutputNode.setActivate(true);
        dbOutputNode.setProcess(process);
        nodeList.add(dbOutputNode);

        ComponentProperties tcomp_properties = dbOutputNode.getComponentProperties();
        dbOutputNode.getElementParameter("tableSelection.tablename")
                .setValue(process.getElementParameter(dbTableParameterName).getValue());

        NamedThing referencedComponent = tcomp_properties.getProperty("referencedComponent");
        if (referencedComponent instanceof ComponentReferenceProperties) {
            ComponentReferenceProperties refProps = (ComponentReferenceProperties) referencedComponent;
            refProps.referenceType.setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
            refProps.componentInstanceId.setStoredValue(CONNECTION_UID);
            refProps.componentInstanceId.setTaggedValue("ADD_QUOTES", true);
        }
        sourceMetadataTable = sourceMetadataTable.clone();
        sourceMetadataTable.setTableName(dbOutputNode.getUniqueName());
        sourceMetadataTable.setAttachedConnector("MAIN");
        List<IMetadataTable> tables = new ArrayList<>();
        tables.add(sourceMetadataTable);
        dbOutputNode.setMetadataList(tables);

        Schema schema = MetadataToolAvroHelper.convertToAvro(ConvertionHelper.convert(sourceMetadataTable));
        tcomp_properties.setValue("main.schema", schema);
        tcomp_properties.setValue("schemaFlow.schema", schema);

        dataConnec.setActivate(true);
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setConnectorName("FLOW");
        dataConnec.setName("row_" + inputNode.getUniqueName());
        dataConnec.setSource(inputNode);
        dataConnec.setTarget(dbOutputNode);
        ((List<IConnection>) inputNode.getOutgoingConnections()).add(dataConnec);
        ((List<IConnection>) dbOutputNode.getIncomingConnections()).add(dataConnec);
        inputNode = dbOutputNode;

        resetShowIf(dbOutputNode);

        connectionNode = addConnection(connectionNode, process, CONNECTION_UID, catcherNode, nodeList, commitNode, true);
        IElementParameter refPara = commitNode.getElementParameter("referencedComponent");
        if (refPara != null) {
            refPara.setValue(connectionNode.getUniqueName());
            IGenericDBService dbService = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
                dbService = GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
            }
            if (dbService != null) {
                dbService.initReferencedComponent(refPara, connectionNode.getUniqueName());
            }
        }

        if (console) {
            DataNode consoleNode = createGenericConsoleNode(inputNode, sourceMetadataTable, prefixName, process);
            nodeList.add(consoleNode);
            inputNode = consoleNode;
        }

        return connectionNode;
    }

    private static DataNode createGenericInputNode(final String inputComponentName, String prefixName, String inputComponentId,
            IProcess process) {
        IComponent inputComponent = ComponentsFactoryProvider.getInstance().get(inputComponentName,
                ComponentCategory.CATEGORY_4_DI.getName());
        String inputComponentUniqueName = prefixName + "_" + inputComponentId;
        DataNode inputNode = new DataNode(inputComponent, inputComponentUniqueName);
        inputNode.setStart(true);
        inputNode.setSubProcessStart(true);
        inputNode.setActivate(true);
        inputNode.setProcess(process);
        return inputNode;
    }

    private static DataNode createGenericConsoleNode(DataNode inputNode, IMetadataTable sourceMetadataTable, String prefixName,
            IProcess process) {
        DataConnection dataConnec = new DataConnection();
        dataConnec.setMetadataTable(sourceMetadataTable);
        dataConnec.setConnectorName(sourceMetadataTable.getAttachedConnector());

        IComponent inputComponent = ComponentsFactoryProvider.getInstance().get("tLogRow",
                ComponentCategory.CATEGORY_4_DI.getName());
        DataNode consoleNode = new DataNode(inputComponent, prefixName + "_" + "CONSOLE");
        consoleNode.setStart(false);
        consoleNode.setSubProcessStart(false);
        consoleNode.setActivate(true);
        consoleNode.setProcess(process);
        sourceMetadataTable = sourceMetadataTable.clone();
        sourceMetadataTable.setTableName(consoleNode.getUniqueName());
        sourceMetadataTable.setAttachedConnector("FLOW");
        consoleNode.getMetadataList().clear();
        consoleNode.getMetadataList().add(sourceMetadataTable);

        dataConnec.setActivate(true);
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setName("row_" + inputNode.getUniqueName());
        dataConnec.setSource(inputNode);
        dataConnec.setTarget(consoleNode);
        ((List<IConnection>) inputNode.getOutgoingConnections()).add(dataConnec);
        ((List<IConnection>) consoleNode.getIncomingConnections()).add(dataConnec);
        return consoleNode;
    }

    private static DataNode createGenericFileNode(DataNode inputNode, IMetadataTable sourceMetadataTable, String prefixName,
            IProcess process) {
        DataConnection dataConnec = new DataConnection();
        dataConnec.setMetadataTable(sourceMetadataTable);

        IComponent inputComponent = ComponentsFactoryProvider.getInstance().get("tFileOutputDelimited",
                ComponentCategory.CATEGORY_4_DI.getName());
        DataNode fileNode = new DataNode(inputComponent, prefixName + "_" + "FILE");
        fileNode.setStart(false);
        fileNode.setSubProcessStart(false);
        fileNode.setActivate(true);
        fileNode.setProcess(process);
        sourceMetadataTable = sourceMetadataTable.clone();
        sourceMetadataTable.setTableName(fileNode.getUniqueName());
        sourceMetadataTable.setAttachedConnector("FLOW");
        fileNode.getMetadataList().clear();
        fileNode.getMetadataList().add(sourceMetadataTable);
        IElementParameter rowSeparatorParam = fileNode.getElementParameter("ROWSEPARATOR");
        if (rowSeparatorParam != null) {
            rowSeparatorParam.setValue(TalendTextUtils.addQuotes("\\n", TalendTextUtils.QUOTATION_MARK));
        }
        IElementParameter fieldSeparatorParam = fileNode.getElementParameter("FIELDSEPARATOR");
        if (fieldSeparatorParam != null) {
            fieldSeparatorParam.setValue(TalendTextUtils.addQuotes(";"));
        }
        IElementParameter appendParam = fileNode.getElementParameter("APPEND");
        if (appendParam != null) {
            appendParam.setValue(Boolean.TRUE);
        }
        IElementParameter headerParam = fileNode.getElementParameter("INCLUDEHEADER");
        if (headerParam != null) {
            headerParam.setValue(Boolean.FALSE);
        }

        IElementParameter encodingParameter = process.getElementParameter(EParameterName.ENCODING.getName());
        if (encodingParameter != null) {
            Object value = encodingParameter.getValue();
            if (value != null && !"".equals(value)) {
                IElementParameter elementParameter = fileNode.getElementParameter(EParameterName.ENCODING.getName());
                if (elementParameter != null) {
                    String encoding = value.toString();
                    if (!value.toString().startsWith(TalendTextUtils.getQuoteChar())) {
                        encoding = TalendTextUtils.addQuotes(encoding);
                    }
                    elementParameter.setValue(encoding);
                }
            }
        }

        dataConnec.setActivate(true);
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setConnectorName("FLOW");
        dataConnec.setName("row_" + inputNode.getUniqueName());
        dataConnec.setSource(inputNode);
        dataConnec.setTarget(fileNode);
        ((List<IConnection>) inputNode.getOutgoingConnections()).add(dataConnec);
        ((List<IConnection>) fileNode.getIncomingConnections()).add(dataConnec);
        return fileNode;
    }

    private static void useNoConnectionComponentDB(DataNode dataNode, IProcess process, String connectionUID) {

        dataNode.getElementParameter(EParameterName.HOST.getName())
                .setValue(process.getElementParameter(EParameterName.HOST.getName()).getValue());
        dataNode.getElementParameter(EParameterName.PORT.getName())
                .setValue(process.getElementParameter(EParameterName.PORT.getName()).getValue());
        dataNode.getElementParameter(EParameterName.SCHEMA_DB.getName())
                .setValue(process.getElementParameter(EParameterName.SCHEMA_DB.getName()).getValue());
        dataNode.getElementParameter(EParameterName.DBNAME.getName())
                .setValue(process.getElementParameter(EParameterName.DBNAME.getName()).getValue());
        dataNode.getElementParameter(EParameterName.DB_VERSION.getName())
                .setValue(process.getElementParameter(EParameterName.DB_VERSION.getName()).getValue());
        dataNode.getElementParameter(EParameterName.PROPERTIES.getName())
                .setValue(process.getElementParameter(EParameterName.PROPERTIES.getName()).getValue());
        dataNode.getElementParameter(EParameterName.USER.getName())
                .setValue(process.getElementParameter(EParameterName.USER.getName()).getValue());
        dataNode.getElementParameter(EParameterName.PASS.getName())
                .setValue(process.getElementParameter(EParameterName.PASS.getName()).getValue());

        Object dbType = process.getElementParameter(EParameterName.DB_TYPE.getName()).getValue();
        if (dbType != null) {
            String dbStr = dbType.toString();
            dataNode.getElementParameter(EParameterName.CONNECTION_TYPE.getName())
                    .setValue(OracleComponentHelper.filterOracleConnectionType(dbStr));
            if (dbStr.indexOf("Access") != -1) {//$NON-NLS-1$
                dataNode.getElementParameter(EParameterName.DBNAME.getName())
                        .setValue(process.getElementParameter(EParameterName.DBFILE.getName()).getValue());
            } else if (dbStr.indexOf("Informix") != -1) {//$NON-NLS-1$
                dataNode.getElementParameter(EParameterName.USE_TRANSACTION.getName()).setValue(Boolean.FALSE);
            }
        }

        IElementParameter param = dataNode.getElementParameter(EParameterName.USE_EXISTING_CONNECTION.getName());
        if (param != null) {
            param.setValue(Boolean.FALSE);
        }
        dataNode.getMetadataFromConnector(connectionUID);

    }

    private static DataNode addConnection(DataNode connectionNode, IProcess process, String connectionUID, DataNode dataNode,
            List<DataNode> nodeList, DataNode commitNode) {
        return addConnection(connectionNode, process, connectionUID, dataNode, nodeList, commitNode, false);
    }

    private static DataNode addConnection(DataNode connectionNode, IProcess process, String connectionUID, DataNode dataNode,
            List<DataNode> nodeList, DataNode commitNode, boolean tcomp) {
        if (!tcomp) {
            IElementParameter param = dataNode.getElementParameter(EParameterName.USE_EXISTING_CONNECTION.getName());
            if (param != null) {
                param.setValue(Boolean.TRUE);
            }
            param = dataNode.getElementParameter(EParameterName.CONNECTION.getName());
            if (param != null) {
                param.setValue(connectionUID);
            }
        }
        if (connectionNode == null) {
            IComponent component = null;
            String[] javaDbComponents = StatsAndLogsConstants.DB_OUTPUT_COMPONENTS;
            for (String dbComponent : javaDbComponents) {
                String connectionComponentName = null;
                if (OracleComponentHelper
                        .filterOracleConnectionType(
                                (String) process.getElementParameter(EParameterName.DB_TYPE.getName()).getValue())
                        .equals(dbComponent)) {
                    if (dbComponent.endsWith("Output")) { //$NON-NLS-1$
                        String substring = dbComponent.substring(0, dbComponent.lastIndexOf("Output")); //$NON-NLS-1$
                        connectionComponentName = substring + "Connection"; //$NON-NLS-1$
                    } else {
                        connectionComponentName = "tOracleConnection"; //$NON-NLS-1$
                    }
                    component = ComponentsFactoryProvider.getInstance().get(connectionComponentName, process.getComponentsType());

                    if (component != null) {
                        connectionNode = new DataNode(component, connectionUID);
                        connectionNode.setSubProcessStart(true);
                        connectionNode.setActivate(true);
                        boolean isGeneric = connectionNode.getComponent().getComponentType() == EComponentType.GENERIC;
                        // check if shared parameter exist, if yes, use it ONLY when use the project settings.
                        // name for shared connection can be always the same, as we use only when project settings is
                        // activated.
                        IElementParameter elementParameter = connectionNode
                                .getElementParameter(EParameterName.USE_SHARED_CONNECTION.getName());
                        if (elementParameter == null && isGeneric) {
                            elementParameter = connectionNode.getElementParameter("shareConnection"); //$NON-NLS-1$
                        }
                        if (elementParameter != null && elementParameter.getName() != null) {
                            elementParameter.setValue(Boolean.TRUE);
                            final String sharedConnName = "StatsAndLog_Shared_Connection"; //$NON-NLS-1$
                            IElementParameter sharedElementParameter = connectionNode
                                    .getElementParameter(EParameterName.SHARED_CONNECTION_NAME.getName());
                            if (sharedElementParameter == null && isGeneric) {
                                sharedElementParameter = connectionNode.getElementParameter("sharedConnectionName"); //$NON-NLS-1$
                            }
                            if ((Boolean) process.getElementParameter(EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName())
                                    .getValue()) {
                                sharedElementParameter.setValue(TalendTextUtils.addQuotes(sharedConnName));
                            } else {
                                String url = getUrl(process);
                                if (url == null || url.equals("")) { // fix bug of stats/logs found for sybase
                                    sharedElementParameter.setValue(TalendTextUtils.addQuotes(sharedConnName));
                                } else {
                                    sharedElementParameter.setValue(url + "+" + TalendTextUtils.addQuotes("_" + sharedConnName)); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                        }
                        setConnectionParameter(connectionNode, process, connectionUID, dataNode, nodeList);

                        if (isGeneric) {// reset the show if
                            resetShowIf(connectionNode);
                            if (checkUrlContainsAutoCommit(connectionNode)) {
                                IElementParameter autoCommitParam = connectionNode.getElementParameter("autocommit");//$NON-NLS-1$
                                if (autoCommitParam != null) {
                                    autoCommitParam.setValue(Boolean.TRUE);
                                }
                            }
                        }

                        if (connectionComponentName.contains("Oracle")) {//$NON-NLS-1$
                            if (connectionNode.getElementParameter(EParameterName.CONNECTION_TYPE.getName()) != null) {
                                connectionNode.getElementParameter(EParameterName.CONNECTION_TYPE.getName())
                                        .setValue(dbComponent);
                            }
                        }
                        connectionNode.setProcess(process);
                        nodeList.add(connectionNode);

                        IComponent prejobComponent = ComponentsFactoryProvider.getInstance().get(TPREJOB,
                                ComponentCategory.CATEGORY_4_DI.getName());
                        DataNode preNode = new DataNode(prejobComponent, PRE_STA_LOG_CON);
                        preNode.setStart(true);
                        preNode.setSubProcessStart(true);
                        preNode.setActivate(true);
                        preNode.setProcess(process);
                        nodeList.add(preNode);
                        DataConnection dataConnec = createDataConnectionForComponentOK(preNode, connectionNode);
                        ((List<IConnection>) preNode.getOutgoingConnections()).add(dataConnec);
                        ((List<IConnection>) connectionNode.getIncomingConnections()).add(dataConnec);

                    }
                }
            }
        }
        boolean noCommitNode = false;
        if (checkUrlContainsAutoCommit(connectionNode)) {
            IElementParameter autoCommitParam = connectionNode.getElementParameter("autocommit");//$NON-NLS-1$
            if (autoCommitParam != null && autoCommitParam.getValue() != null) {
                noCommitNode = Boolean.parseBoolean(autoCommitParam.getValue().toString());
                if (noCommitNode && nodeList.contains(commitNode)) {
                    nodeList.remove(commitNode);
                }
            }
        }
        if (!noCommitNode) {
            DataConnection dataConnec = createDataConnectionForSubJobOK(dataNode, commitNode);
            ((List<IConnection>) dataNode.getOutgoingConnections()).add(dataConnec);
            ((List<IConnection>) commitNode.getIncomingConnections()).add(dataConnec);
        }
        return connectionNode;
    }

    private static boolean checkUrlContainsAutoCommit(DataNode connectionNode) {
        if (connectionNode != null) {
            boolean isGeneric = connectionNode.getComponent().getComponentType() == EComponentType.GENERIC;
            if (isGeneric) {
                IElementParameter urlParam = connectionNode
                        .getElementParameter(EConnectionParameterName.GENERIC_URL.getDisplayName());
                if (urlParam != null) {
                    Object obj = urlParam.getValue();
                    if (obj != null && obj instanceof String) {
                        String url = (String) obj;
                        if (url != null && url.toLowerCase().contains("autocommit=true")) {//$NON-NLS-1$
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void resetShowIf(DataNode connectionNode) {
        ComponentProperties tcomp_properties = connectionNode.getComponentProperties();
        if (tcomp_properties != null) {
            List<Form> forms = tcomp_properties.getForms();
            if (forms != null) {
                for (Form form : forms) {
                    tcomp_properties.refreshLayout(form);
                }
            }
        }
        // we set it for avoiding to depend on the getComponentProperties implement
        connectionNode.setComponentProperties(tcomp_properties);
    }

    /**
     * DOC zli Comment method "getUrl".
     *
     * @param process
     */
    private static String getUrl(IProcess process) {
        String processDBType = (String) process.getElementParameter(EParameterName.DB_TYPE.getName()).getValue();

        if (StatsAndLogsConstants.JDBC_OUTPUT.equals(processDBType)) {
            IElementParameter urlParam = process.getElementParameter(EParameterName.URL.getName());
            if (urlParam != null && urlParam.getValue() != null) {
                return urlParam.getValue().toString();
            }
        }

        int indexOfItemFromList = process.getElementParameter(EParameterName.DB_TYPE.getName())
                .getIndexOfItemFromList(processDBType);
        String[] listItemsDisplayName = process.getElementParameter(EParameterName.DB_TYPE.getName()).getListItemsDisplayName();
        processDBType = listItemsDisplayName[indexOfItemFromList];

        IElementParameter param = process.getElementParameter(EParameterName.DBFILE.getName());
        final String empty = ""; //$NON-NLS-1$
        String processDBFileName = empty;
        if (param != null) {
            processDBFileName = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.DATASOURCE.getName());
        String processDBDatasouce = empty;
        if (param != null) {
            processDBDatasouce = (String) param.getValue();
        }

        param = process.getElementParameter(EParameterName.DBNAME.getName());
        String processDBName = empty;
        if (param != null) {
            processDBName = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.HOST.getName());
        String processHost = empty;
        if (param != null) {
            processHost = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.PORT.getName());
        String processDBPort = empty;
        if (param != null) {
            processDBPort = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.PASS.getName());
        String processDBPass = empty;
        if (param != null) {
            processDBPass = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.SCHEMA_DB.getName());
        String processDBSID = empty;
        if (param != null) {
            processDBSID = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.USER.getName());
        String processDBUser = empty;
        if (param != null) {
            processDBUser = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.PROPERTIES.getName());
        String processDBAdditionParameters = empty;
        if (param != null) {
            processDBAdditionParameters = (String) param.getValue();
        }
        param = process.getElementParameter(EParameterName.DB_VERSION.getName());
        String processDBVersion = empty;
        if (param != null) {
            processDBVersion = (String) param.getValue();
        }
        if (processDBType.equals("Oracle OCI")) {
            processDBSID = processDBName;
        }
        String dbURL = DatabaseConnStrUtil.getURLString(true, processDBType, processDBVersion, processHost, processDBUser,
                processDBPass, processDBPort, processDBSID, processDBFileName, processDBDatasouce, processDBFileName,
                processDBAdditionParameters);

        return dbURL;
    }

    private static DataConnection createDataConnectionForComponentOK(DataNode dataNode, DataNode commitNode) {
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(true);
        dataConnec.setLineStyle(EConnectionType.ON_COMPONENT_OK);
        dataConnec.setTraceConnection(false);
        dataConnec.setName("after_" + dataNode.getUniqueName() + "_" + commitNode.getUniqueName()); //$NON-NLS-1$ //$NON-NLS-2$
        // dataConnec.setName(refSource.getUniqueName() + "_to_hash_" + connection.getName());
        dataConnec.setSource(dataNode);
        dataConnec.setTarget(commitNode);
        dataConnec.setConnectorName(EConnectionType.ON_COMPONENT_OK.getName());
        return dataConnec;
    }

    private static DataConnection createDataConnectionForSubJobOK(DataNode dataNode, DataNode commitNode) {
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(true);
        dataConnec.setLineStyle(EConnectionType.ON_SUBJOB_OK);
        dataConnec.setTraceConnection(false);
        dataConnec.setName("sub_ok_" + dataNode.getUniqueName() + "_" + commitNode.getUniqueName()); //$NON-NLS-1$ //$NON-NLS-2$
        // dataConnec.setName(refSource.getUniqueName() + "_to_hash_" + connection.getName());
        dataConnec.setSource(dataNode);
        dataConnec.setTarget(commitNode);
        dataConnec.setConnectorName(EConnectionType.ON_SUBJOB_OK.getName());
        return dataConnec;
    }

    private static void setConnectionParameter(DataNode connectionNode, IProcess process, String connectionUID, DataNode dataNode,
            List<DataNode> nodeList) {
        // db type
        String dbType = null;
        IElementParameter dbTypeParameter = connectionNode.getElementParameter("TYPE");//$NON-NLS-1$
        if (dbTypeParameter != null) {
            Object dbTypeObj = dbTypeParameter.getValue();
            if (dbTypeObj != null) {
                dbType = dbTypeObj.toString();
            }
        }
        if (connectionNode.getElementParameter(EParameterName.HOST.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.HOST.getName())
                    .setValue(process.getElementParameter(EParameterName.HOST.getName()).getValue());
        }
        if (connectionNode.getElementParameter(EParameterName.PORT.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.PORT.getName())
                    .setValue(process.getElementParameter(EParameterName.PORT.getName()).getValue());
        }
        // schema for common database component
        if (connectionNode.getElementParameter(EParameterName.SCHEMA_DB.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.SCHEMA_DB.getName())
                    .setValue(process.getElementParameter(EParameterName.SCHEMA_DB.getName()).getValue());
        }
        // schema for Oracle,PostGre,Vertica,Greenplum
        if (connectionNode.getElementParameter(EParameterName.SCHEMA_DB_ORACLE.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.SCHEMA_DB_ORACLE.getName())
                    .setValue(process.getElementParameter(EParameterName.SCHEMA_JOB.getName()).getValue());
        }
        // schema for DB2
        if (connectionNode.getElementParameter(EParameterName.SCHEMA_DB_DB2.getDisplayName()) != null) {
            connectionNode.getElementParameter(EParameterName.SCHEMA_DB_DB2.getDisplayName())
                    .setValue(process.getElementParameter(EParameterName.SCHEMA_DB.getName()).getValue());
        }
        // schema for MSSQL
        if (connectionNode.getElementParameter(EParameterName.SCHEMA_DB_MSSQL.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.SCHEMA_DB_MSSQL.getName())
                    .setValue(process.getElementParameter(EParameterName.SCHEMA_JOB.getName()).getValue());
        }
        if (connectionNode.getElementParameter(EParameterName.DBNAME.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.DBNAME.getName())
                    .setValue(process.getElementParameter(EParameterName.DBNAME.getName()).getValue());
        }
        if (connectionNode.getElementParameter(EParameterName.LOCAL_SERVICE_NAME.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.LOCAL_SERVICE_NAME.getName())
                    .setValue(process.getElementParameter(EParameterName.LOCAL_SERVICE_NAME.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EParameterName.URL.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.URL.getName())
                    .setValue(process.getElementParameter(EParameterName.URL.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EConnectionParameterName.GENERIC_URL.getDisplayName()) != null) {
            connectionNode.getElementParameter(EConnectionParameterName.GENERIC_URL.getDisplayName())
                    .setValue(process.getElementParameter(EParameterName.URL.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EParameterName.DRIVER_JAR.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.DRIVER_JAR.getName())
                    .setValue(process.getElementParameter(EParameterName.DRIVER_JAR.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName()) != null) {
            IElementParameter elePara = connectionNode
                    .getElementParameter(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName());
            Object value = process.getElementParameter(EParameterName.DRIVER_JAR.getName()).getValue();
            getMVNDriverJar(elePara, value);
            elePara.setValue(value);
        }

        if (connectionNode.getElementParameter(EParameterName.DRIVER_CLASS.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.DRIVER_CLASS.getName())
                    .setValue(process.getElementParameter(EParameterName.DRIVER_CLASS.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName()) != null) {
            connectionNode.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName())
                    .setValue(process.getElementParameter(EParameterName.DRIVER_CLASS.getName()).getValue());
        }

        // 7253
        IElementParameter elementParameter = connectionNode.getElementParameter("PROPERTY");
        if (elementParameter != null) {
            String repositoryValue = elementParameter.getRepositoryValue();
            if ("DATABASE:SQLITE".equals(repositoryValue)) {
                if (connectionNode.getElementParameter(EParameterName.DBNAME.getName()) != null) {
                    connectionNode.getElementParameter(EParameterName.DBNAME.getName())
                            .setValue(process.getElementParameter(EParameterName.DBFILE.getName()).getValue());
                }
            }
        }

        if (connectionNode.getElementParameter(EParameterName.DB_VERSION.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.DB_VERSION.getName())
                    .setValue(process.getElementParameter(EParameterName.DB_VERSION.getName()).getValue());
        }
        if (StringUtils.isNotEmpty(dbType) && EDatabaseTypeName.MSSQL.getXmlName().equalsIgnoreCase(dbType)) {
            if (connectionNode.getElementParameter("DRIVER") != null) {//$NON-NLS-1$
                connectionNode.getElementParameter("DRIVER") //$NON-NLS-1$
                        .setValue(process.getElementParameter(EParameterName.DB_VERSION.getName()).getValue());
            }
        }
        if (connectionNode.getElementParameter(EParameterName.PROPERTIES.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.PROPERTIES.getName())
                    .setValue(process.getElementParameter(EParameterName.PROPERTIES.getName()).getValue());
        }
        if (connectionNode.getElementParameter(EParameterName.USER.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.USER.getName())
                    .setValue(process.getElementParameter(EParameterName.USER.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EConnectionParameterName.GENERIC_USERNAME.getDisplayName()) != null) {
            connectionNode.getElementParameter(EConnectionParameterName.GENERIC_USERNAME.getDisplayName())
                    .setValue(process.getElementParameter(EParameterName.USER.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EParameterName.PASS.getName()) != null) {
            connectionNode.getElementParameter(EParameterName.PASS.getName())
                    .setValue(process.getElementParameter(EParameterName.PASS.getName()).getValue());
        }

        if (connectionNode.getElementParameter(EConnectionParameterName.GENERIC_PASSWORD.getDisplayName()) != null) {
            connectionNode.getElementParameter(EConnectionParameterName.GENERIC_PASSWORD.getDisplayName())
                    .setValue(process.getElementParameter(EParameterName.PASS.getName()).getValue());
        }

    }

    private static void getMVNDriverJar(IElementParameter elePara, Object value) {
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        if (dbService == null) {
            return;
        }

        if (value instanceof List) {
            List objs = (List) value;
            for (Object obj : objs) {
                if (obj instanceof Map) {
                    Map map = (Map) obj;
                    String driver = (String) map.get("drivers");
                    if (elePara.getName().equals(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName())) {
                        driver = dbService.getMVNPath(driver);
                    }
                    map.put("drivers", driver);
                }
            }
        }
    }

    private static DataNode createLogsNode(boolean useFile, boolean console, String dbOutput) {
        JobLogsComponent logsComponent = new JobLogsComponent(useFile, console, dbOutput);
        DataNode logsNode = new DataNode(logsComponent, LOG_UNIQUE_NAME);
        logsNode.setStart(true);
        logsNode.setSubProcessStart(true);
        logsNode.setActivate(true);
        logsNode.getMetadataList().clear();

        // load the tLogCatcher to get the schema.
        IComponent tmpComponent = ComponentsFactoryProvider.getInstance().get("tLogCatcher", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        DataNode tmpNode = new DataNode(tmpComponent, "tmp"); //$NON-NLS-1$
        boolean found = false;
        for (int k = 0; k < tmpNode.getElementParameters().size() && !found; k++) {
            IElementParameter currentParam = tmpNode.getElementParameters().get(k);
            if (currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                Object value = currentParam.getValue();
                if (value instanceof IMetadataTable) {
                    IMetadataTable table = ((IMetadataTable) value).clone();
                    table.setTableName(LOG_UNIQUE_NAME);
                    table.setAttachedConnector(currentParam.getContext());
                    logsNode.getMetadataList().add(table);
                }
                found = true;
            }
        }
        return logsNode;
    }

    private static DataNode createStatsNode(boolean useFile, boolean console, String dbOutput) {
        JobStatsComponent statsComponent = new JobStatsComponent(useFile, console, dbOutput);
        DataNode statsNode = new DataNode(statsComponent, STAT_UNIQUE_NAME);
        statsNode.setStart(true);
        statsNode.setSubProcessStart(true);
        statsNode.setActivate(true);

        statsNode.getMetadataList().clear();

        // load the tStatCatcher to get the schema.
        IComponent tmpComponent = ComponentsFactoryProvider.getInstance().get("tStatCatcher", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        DataNode tmpNode = new DataNode(tmpComponent, "tmp"); //$NON-NLS-1$
        boolean found = false;
        for (int k = 0; k < tmpNode.getElementParameters().size() && !found; k++) {
            IElementParameter currentParam = tmpNode.getElementParameters().get(k);
            if (currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                Object value = currentParam.getValue();
                if (value instanceof IMetadataTable) {
                    IMetadataTable table = ((IMetadataTable) value).clone();
                    table.setTableName(STAT_UNIQUE_NAME);
                    table.setAttachedConnector(currentParam.getContext());
                    statsNode.getMetadataList().add(table);
                }
                found = true;
            }
        }
        return statsNode;
    }

    private static DataNode createMetterNode(boolean useFile, boolean console, String dbOutput) {
        JobMetterComponent statsComponent = new JobMetterComponent(useFile, console, dbOutput);
        DataNode statsNode = new DataNode(statsComponent, METER_UNIQUE_NAME);
        statsNode.setStart(true);
        statsNode.setSubProcessStart(true);
        statsNode.setActivate(true);

        statsNode.getMetadataList().clear();

        // load the tFlowMeterCatcher to get the schema.
        IComponent tmpComponent = ComponentsFactoryProvider.getInstance().get("tFlowMeterCatcher", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        DataNode tmpNode = new DataNode(tmpComponent, "tmp"); //$NON-NLS-1$
        boolean found = false;
        for (int k = 0; k < tmpNode.getElementParameters().size() && !found; k++) {
            IElementParameter currentParam = tmpNode.getElementParameters().get(k);
            if (currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || currentParam.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                Object value = currentParam.getValue();
                if (value instanceof IMetadataTable) {
                    IMetadataTable table = ((IMetadataTable) value).clone();
                    table.setTableName(METER_UNIQUE_NAME);
                    table.setAttachedConnector(currentParam.getContext());
                    statsNode.getMetadataList().add(table);
                }
                found = true;
            }
        }
        return statsNode;
    }

    public static List<IElementParameter> getStatsAndLogsParameters(IProcess process) {
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();
        paramList.addAll(statsAndLogsParametersTitlePart(process));
        paramList.addAll(statsAndLogsParametersFilePart(process));
        paramList.addAll(statsAndLogsParametersDBPart(process));
        paramList.addAll(statsAndLogsParametersFinalPart(process));

        for (IElementParameter param : paramList) {
            if (param.getRepositoryValue() != null) {
                // if any of the parameter of stat&logs is using repository, then force to link it to the name of the
                // property for stat&logs
                param.setRepositoryProperty(EParameterName.PROPERTY_TYPE.getName());
            }
        }
        return paramList;
    }

    private static List<IElementParameter> statsAndLogsParametersTitlePart(IProcess process) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

        param = new ElementParameter(process);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.ON_STATCATCHER_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_STATCATCHER_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_STATCATCHER_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.ON_LOGCATCHER_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_LOGCATCHER_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_LOGCATCHER_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.ON_METERCATCHER_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_METERCATCHER_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_METERCATCHER_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(1);
        paramList.add(param);

        // on console
        param = new ElementParameter(process);
        param.setName(EParameterName.ON_CONSOLE_FLAG.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.ON_CONSOLE_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(2);
        param.setShowIf(
                "((ON_CONSOLE_FLAG == 'true' or ON_CONSOLE_FLAG == 'false') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true'))"); //$NON-NLS-1$
        paramList.add(param);

        return paramList;
    }

    private static List<IElementParameter> statsAndLogsParametersFilePart(IProcess process) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$
        // on files
        param = new ElementParameter(process);
        param.setName(EParameterName.ON_FILES_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_FILES_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_FILES_FLAG.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(10);
        param.setShowIf("(ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // file path
        param = new ElementParameter(process);
        param.setName(EParameterName.FILE_PATH.getName());
        param.setValue(addQuotes(replaceSlash(preferenceStore.getString(languagePrefix + EParameterName.FILE_PATH.getName()))));
        param.setDisplayName(EParameterName.FILE_PATH.getDisplayName());
        param.setFieldType(EParameterFieldType.DIRECTORY);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf(
                "(ON_FILES_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setNumRow(11);
        paramList.add(param);

        // stats file name
        param = new ElementParameter(process);
        param.setName(EParameterName.FILENAME_STATS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.FILENAME_STATS.getName())));
        param.setDisplayName(EParameterName.FILENAME_STATS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf("(ON_FILES_FLAG == 'true' and ON_STATCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setRequired(true);
        param.setNumRow(12);

        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FILENAME_LOGS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.FILENAME_LOGS.getName())));
        param.setDisplayName(EParameterName.FILENAME_LOGS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf("(ON_FILES_FLAG == 'true' and ON_LOGCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setNumRow(13);
        param.setRequired(true);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FILENAME_METTER.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.FILENAME_METTER.getName())));
        param.setDisplayName(EParameterName.FILENAME_METTER.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setShowIf("(ON_FILES_FLAG == 'true' and ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        param.setRequired(true);
        param.setNumRow(14);
        paramList.add(param);

        // stats log encoding
        ElementParameter encodingParam = new ElementParameter(process);
        encodingParam.setName(EParameterName.ENCODING.getName());
        encodingParam.setDisplayName(EParameterName.ENCODING.getDisplayName());
        encodingParam.setCategory(EComponentCategory.STATSANDLOGS);
        encodingParam.setFieldType(EParameterFieldType.ENCODING_TYPE);
        encodingParam.setShowIf(
                "(ON_FILES_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        encodingParam.setValue(ENCODING_TYPE_ISO_8859_15);
        encodingParam.setNumRow(15);
        paramList.add(encodingParam);

        ElementParameter childPram = new ElementParameter(process);
        childPram.setName(EParameterName.ENCODING_TYPE.getName());
        childPram.setDisplayName(EParameterName.ENCODING_TYPE.getDisplayName());
        childPram.setFieldType(EParameterFieldType.TECHNICAL);
        childPram.setCategory(EComponentCategory.STATSANDLOGS);
        childPram.setListItemsDisplayName(new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childPram.setListItemsDisplayCodeName(
                new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childPram.setListItemsValue(new String[] { ENCODING_TYPE_ISO_8859_15, ENCODING_TYPE_UTF_8, ENCODING_TYPE_CUSTOM });
        childPram.setValue(ENCODING_TYPE_ISO_8859_15);
        childPram.setNumRow(15);
        childPram.setShowIf(
                "(ON_FILES_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        childPram.setParentParameter(encodingParam);

        return paramList;
    }

    private static List<IElementParameter> statsAndLogsParametersDBPart(IProcess process) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

        // on database
        param = new ElementParameter(process);
        param.setName(EParameterName.ON_DATABASE_FLAG.getName());
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.ON_DATABASE_FLAG.getName()));
        param.setDisplayName(EParameterName.ON_DATABASE_FLAG.getDisplayName()); // On Database
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(50);
        param.setShowIf("(ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        ElementParameter parentPropertyType = new ElementParameter(process);
        parentPropertyType.setName(EParameterName.PROPERTY_TYPE.getName());
        parentPropertyType.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        parentPropertyType.setValue(""); //$NON-NLS-1$
        parentPropertyType.setCategory(EComponentCategory.STATSANDLOGS);
        parentPropertyType.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        parentPropertyType.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        parentPropertyType.setNumRow(51);
        parentPropertyType.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(parentPropertyType);

        param = new ElementParameter(process);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setName(EParameterName.PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        param.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setValue(preferenceStore.getString(languagePrefix + EParameterName.PROPERTY_TYPE.getName()));
        param.setNumRow(51);
        param.setFieldType(EParameterFieldType.TECHNICAL);
        param.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$

        param.setParentParameter(parentPropertyType);
        // paramList.add(param);

        param = new ElementParameter(process);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] {});
        param.setListItemsValue(new String[] {});
        param.setNumRow(51);
        param.setFieldType(EParameterFieldType.TECHNICAL);
        param.setValue(preferenceStore.getString(languagePrefix + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()));
        param.setShow(false);
        param.setRequired(true);
        // paramList.add(param);
        param.setParentParameter(parentPropertyType);

        // dbType
        param = new ElementParameter(process);
        param.setName(EParameterName.DB_TYPE.getName());
        String type = preferenceStore.getString(languagePrefix + EParameterName.DB_TYPE.getName());
        if (type == null || "".equals(type.trim())) { //$NON-NLS-1$
            type = StatsAndLogsConstants.DB_COMPONENTS[1][0];
        }
        param.setValue(type);
        param.setDisplayName(EParameterName.DB_TYPE.getDisplayName());
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setListItemsDisplayName(StatsAndLogsConstants.DISPLAY_DBNAMES[1]);
        param.setListItemsValue(StatsAndLogsConstants.DB_COMPONENTS[1]);
        param.setListRepositoryItems(StatsAndLogsConstants.REPOSITORY_ITEMS[1]);
        param.setListItemsDisplayCodeName(StatsAndLogsConstants.CODE_LIST[1]);
        param.setNumRow(52);
        param.setRepositoryValue("TYPE"); //$NON-NLS-1$
        param.setRequired(true);
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // dbVersion
        if (process.getElementParameter(EParameterName.DB_VERSION.getName()) == null) {
            param = new ElementParameter(process);
            param.setName(EParameterName.DB_VERSION.getName());
            param.setDisplayName(EParameterName.DB_VERSION.getDisplayName());
            param.setFieldType(EParameterFieldType.CLOSED_LIST);
            param.setCategory(EComponentCategory.STATSANDLOGS);
            param.setValue(StatsAndLogsConstants.DB_VERSION_DRIVER[1]);
            param.setListItemsDisplayName(StatsAndLogsConstants.DB_VERSION_DISPLAY);
            param.setListItemsValue(StatsAndLogsConstants.DB_VERSION_DRIVER);
            param.setListItemsDisplayCodeName(StatsAndLogsConstants.DB_VERSION_CODE);
            param.setNumRow(52);
            param.setRepositoryValue("DB_VERSION"); //$NON-NLS-1$
            param.setRequired(true);
            param.setShowIf(
                    "(ON_DATABASE_FLAG == 'true') and (DB_TYPE == 'POSTGRESQL' or DB_TYPE == 'SYBASE' or DB_TYPE == 'OCLE' or DB_TYPE == 'ACCESS' or DB_TYPE == 'OCLE_OCI' or DB_TYPE == 'MSSQL' or  DB_TYPE == 'MYSQL') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
            paramList.add(param);
        }

        // jdbc url
        param = new ElementParameter(process);
        param.setName(EParameterName.URL.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.URL.getName())));
        param.setDisplayName(EParameterName.URL.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(53);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_URL.getDisplayName()); // $NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE=='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // jdbc child param
        if (moduleNameList == null) {
            Set<ModuleNeeded> moduleNeededList = ModulesNeededProvider.getModulesNeeded();
            moduleNameList = new ArrayList<String>();
            moduleValueList = new ArrayList<String>();
            for (ModuleNeeded module : moduleNeededList) {
                String moduleName = module.getModuleName();
                if (moduleName != null) {
                    if (!moduleNameList.contains(moduleName)) {
                        moduleNameList.add(moduleName);
                    }
                    String moduleValue = TalendTextUtils.addQuotes(module.getMavenUri());
                    if (!moduleValueList.contains(moduleValue)) {
                        moduleValueList.add(moduleValue);
                    }
                }
            }
            Comparator<String> comprarator = new IgnoreCaseComparator();
            Collections.sort(moduleNameList, comprarator);
            Collections.sort(moduleValueList, comprarator);
        }
        String[] moduleNameArray = moduleNameList.toArray(new String[0]);
        String[] moduleValueArray = moduleValueList.toArray(new String[0]);
        ElementParameter childParam = new ElementParameter(process);
        childParam.setName("drivers");
        childParam.setDisplayName("drivers");
        childParam.setFieldType(EParameterFieldType.MODULE_LIST);
        childParam.setListItemsDisplayName(moduleNameArray);
        childParam.setListItemsValue(moduleValueArray);
        // driver jar for jdbc
        param = new ElementParameter(process);
        param.setName(EParameterName.DRIVER_JAR.getName());
        param.setDisplayName(EParameterName.DRIVER_JAR.getDisplayName());
        param.setFieldType(EParameterFieldType.TABLE);
        param.setListItemsDisplayCodeName(new String[] { "drivers" });
        param.setListItemsDisplayName(new String[] { "drivers" });
        param.setListItemsValue(new ElementParameter[] { childParam });
        param.setValue(new ArrayList<Map<String, Object>>());
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName()); // $NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE=='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // class name for jdbc
        param = new ElementParameter(process);
        param.setName(EParameterName.DRIVER_CLASS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.DRIVER_CLASS.getName())));
        param.setDisplayName(EParameterName.DRIVER_CLASS.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(57);
        param.setRepositoryValue(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName()); // $NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE=='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // host
        param = new ElementParameter(process);
        param.setName(EParameterName.HOST.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.HOST.getName())));
        param.setDisplayName(EParameterName.HOST.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(53);
        param.setRepositoryValue("SERVER_NAME"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE' and DB_TYPE!='ACCESS' and DB_TYPE!='OCLE_OCI' and DB_TYPE!='JDBC') "); //$NON-NLS-1$
        paramList.add(param);

        // port
        param = new ElementParameter(process);
        param.setName(EParameterName.PORT.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.PORT.getName())));
        param.setDisplayName(EParameterName.PORT.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(53);
        param.setRepositoryValue("PORT"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE' and DB_TYPE!='ACCESS' and DB_TYPE!='FIREBIRD' and DB_TYPE!='OCLE_OCI'  and DB_TYPE!='JDBC' and DB_TYPE!='TERADATA') "); //$NON-NLS-1$
        paramList.add(param);

        // databaseSource
        // gcui:see bug 7456.
        param = new ElementParameter(process);
        param.setName(EParameterName.DATASOURCE.getName());
        param.setDisplayName(EParameterName.DATASOURCE.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.DATASOURCE.getName())));
        param.setNumRow(53);
        param.setRepositoryValue("DATASOURCE"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')and (DB_TYPE =='INFORMIX' or DB_TYPE =='GODBC' or DB_TYPE =='MSODBC') "); //$NON-NLS-1$
        paramList.add(param);

        // dbName
        param = new ElementParameter(process);
        param.setName(EParameterName.DBNAME.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.DBNAME.getName())));
        param.setDisplayName(EParameterName.DBNAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE' and DB_TYPE!='ACCESS' and DB_TYPE!='FIREBIRD' and DB_TYPE != 'OCLE_OCI'  and DB_TYPE!='JDBC')"); //$NON-NLS-1$
        paramList.add(param);

        // local service name
        param = new ElementParameter(process);
        param.setName(EParameterName.LOCAL_SERVICE_NAME.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.LOCAL_SERVICE_NAME.getName())));
        param.setDisplayName(EParameterName.LOCAL_SERVICE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and DB_TYPE =='OCLE_OCI'"); //$NON-NLS-1$ //and
                                                                                                                                                                                // (DB_TYPE
                                                                                                                                                                                // ==
                                                                                                                                                                                // 'OCLE_OCI'
        paramList.add(param);

        // additional parameters
        param = new ElementParameter(process);
        param.setName(EParameterName.PROPERTIES.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.PROPERTIES.getName())));
        param.setDisplayName(EParameterName.PROPERTIES.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("PROPERTIES_STRING"); //$NON-NLS-1$
        param.setShowIf(
                "(DB_TYPE=='MSSQL' or DB_TYPE=='MYSQL' or DB_TYPE=='INFORMIX' or DB_TYPE=='OCLE' or DB_TYPE=='OCLE_OCI' or DB_TYPE=='SYBASE') and (ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // schema
        param = new ElementParameter(process);
        param.setName(EParameterName.SCHEMA_DB.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.SCHEMA_DB.getName())));
        param.setDisplayName(EParameterName.SCHEMA_DB.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(54);
        param.setRepositoryValue("SCHEMA"); //$NON-NLS-1$
        param.setShowIf(
                "(DB_TYPE=='OCLE' or DB_TYPE=='POSTGRESQL' or DB_TYPE=='POSTGRESPLUS' or DB_TYPE=='OCLE_OCI' or DB_TYPE=='MSSQL' or DB_TYPE=='INFORMIX' or DB_TYPE=='IBM_DB2' or DB_TYPE=='SYBASE' ) and (ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // username
        param = new ElementParameter(process);
        param.setName(EParameterName.USER.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.USER.getName())));
        param.setDisplayName(EParameterName.USER.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(55);
        param.setRequired(true);
        param.setRepositoryValue("USERNAME"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')and (DB_TYPE!='SQLITE')"); //$NON-NLS-1$
        paramList.add(param);

        // password
        param = new ElementParameter(process);
        param.setName(EParameterName.PASS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.PASS.getName())));
        param.setDisplayName(EParameterName.PASS.getDisplayName());
        param.setFieldType(EParameterFieldType.PASSWORD);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(55);
        param.setRequired(true);
        param.setRepositoryValue("PASSWORD"); //$NON-NLS-1$
        param.setShowIf(
                "(ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true') and (DB_TYPE!='SQLITE')"); //$NON-NLS-1$
        paramList.add(param);
        // databse file path
        param = new ElementParameter(process);
        param.setName(EParameterName.DBFILE.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.DBFILE.getName())));
        param.setDisplayName(EParameterName.DBFILE.getDisplayName());
        param.setFieldType(EParameterFieldType.FILE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(56);
        param.setRepositoryValue("FILE"); //$NON-NLS-1$
        param.setShowIf(
                "(DB_TYPE=='SQLITE' or DB_TYPE=='ACCESS' or DB_TYPE=='FIREBIRD') and (ON_DATABASE_FLAG == 'true') and (ON_STATCATCHER_FLAG == 'true' or ON_LOGCATCHER_FLAG == 'true' or ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);
        // Stats table
        param = new ElementParameter(process);
        param.setName(EParameterName.TABLE_STATS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.TABLE_STATS.getName())));
        param.setDisplayName(EParameterName.TABLE_STATS.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(57);
        param.setShowIf("(ON_DATABASE_FLAG == 'true' and ON_STATCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // Log table
        param = new ElementParameter(process);
        param.setName(EParameterName.TABLE_LOGS.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.TABLE_LOGS.getName())));
        param.setDisplayName(EParameterName.TABLE_LOGS.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(58);
        param.setShowIf("(ON_DATABASE_FLAG == 'true' and ON_LOGCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        // Metter table
        param = new ElementParameter(process);
        param.setName(EParameterName.TABLE_METER.getName());
        param.setValue(addQuotes(preferenceStore.getString(languagePrefix + EParameterName.TABLE_METER.getName())));
        param.setDisplayName(EParameterName.TABLE_METER.getDisplayName());
        param.setFieldType(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(59);
        param.setShowIf("(ON_DATABASE_FLAG == 'true' and ON_METERCATCHER_FLAG == 'true')"); //$NON-NLS-1$
        paramList.add(param);

        return paramList;
    }

    /**
     * DOC yzhang class global comment. Detailled comment
     */
    private final static class IgnoreCaseComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    private static List<IElementParameter> statsAndLogsParametersFinalPart(IProcess process) {
        ElementParameter param;
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        List<IElementParameter> paramList = new ArrayList<IElementParameter>();

        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$

        // Catch runtime errors
        param = new ElementParameter(process);
        param.setName("CATCH_RUNTIME_ERRORS"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_RUNTIME_ERRORS.getName()));
        param.setDisplayName(EParameterName.CATCH_RUNTIME_ERRORS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(90);
        param.setShowIf(
                "((CATCH_RUNTIME_ERRORS == 'true' or CATCH_RUNTIME_ERRORS == 'false') and (ON_LOGCATCHER_FLAG == 'true'))"); //$NON-NLS-1$
        paramList.add(param);

        // Catch user errors
        param = new ElementParameter(process);
        param.setName("CATCH_USER_ERRORS"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_USER_ERRORS.getName()));
        param.setDisplayName(EParameterName.CATCH_USER_ERRORS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(90);
        param.setShowIf("((CATCH_USER_ERRORS == 'true' or CATCH_USER_ERRORS == 'false') and (ON_LOGCATCHER_FLAG == 'true'))"); //$NON-NLS-1$
        paramList.add(param);

        // Catch user warning
        param = new ElementParameter(process);
        param.setName("CATCH_USER_WARNING"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_USER_WARNING.getName()));
        param.setDisplayName(EParameterName.CATCH_USER_WARNING.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(90);
        param.setShowIf("((CATCH_USER_WARNING == 'true' or CATCH_USER_WARNING == 'false') and (ON_LOGCATCHER_FLAG == 'true'))"); //$NON-NLS-1$
        paramList.add(param);

        // Catch realtime statistics
        param = new ElementParameter(process);
        param.setName("CATCH_REALTIME_STATS"); //$NON-NLS-1$
        param.setValue(preferenceStore.getBoolean(languagePrefix + EParameterName.CATCH_REALTIME_STATS.getName()));
        param.setDisplayName(EParameterName.CATCH_REALTIME_STATS.getDisplayName() + " (" //$NON-NLS-1$
                + EParameterName.TSTATCATCHER_STATS.getDisplayName() + ")"); //$NON-NLS-1$
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(91);
        param.setShowIf(
                "((CATCH_REALTIME_STATS == 'true' or CATCH_REALTIME_STATS == 'false') and (ON_STATCATCHER_FLAG == 'true'))"); //$NON-NLS-1$
        paramList.add(param);

        return paramList;
    }

    /**
     * This function will add quotes only if necessary for the stats & logs.
     *
     * @param str
     * @return
     */
    public static String addQuotes(String str) {
        // function?
        if (str.contains("(") && str.contains(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            return str;
        }

        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            // if the user already added quotes (anywhere) then don't add.
            if (str.contains("\"")) { //$NON-NLS-1$
                return str;
            }
            break;
        default: // PERL
            // if the user already added quotes (anywhere) then don't add.
            if (str.contains("'")) { //$NON-NLS-1$
                return str;
            }
        }
        if (ContextParameterUtils.containContextVariables(str)) {
            return str;
        }
        return TalendTextUtils.addQuotes(str);
    }

    private static String replaceSlash(String str) {
        String tempStr = str.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        return tempStr;
    }
}
