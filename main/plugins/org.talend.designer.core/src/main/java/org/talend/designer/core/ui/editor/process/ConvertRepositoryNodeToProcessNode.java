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
package org.talend.designer.core.ui.editor.process;

import java.util.regex.Pattern;

import org.eclipse.gef.commands.CompoundCommand;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.ConnParameterKeys;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.connection.hive.HiveModeInfo;
import org.talend.core.model.metadata.connection.hive.HiveServerVersionInfo;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.DragAndDropManager;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.model.utils.IDragAndDropServiceHandler;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.utils.CsvArray;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.QueryGuessCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.DbInfo;
import org.talend.designer.core.ui.editor.properties.controllers.GuessSchemaProcess;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.UpdateRepositoryUtils;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class ConvertRepositoryNodeToProcessNode {

    private INode node;

    private String memoSQL;

    private DbInfo info;

    private IContext selectedContext;

    DatabaseConnection databaseConnection;

    public ConvertRepositoryNodeToProcessNode(ConnectionItem connectionItem, IMetadataConnection convertedConnection,
            String tableName) {
        databaseConnection = (DatabaseConnection) connectionItem.getConnection();
        IMetadataConnection iMetadataConnection = convertedConnection;
        String dbType = iMetadataConnection.getDbType();
        String username = iMetadataConnection.getUsername();
        String pwd = iMetadataConnection.getPassword();
        String dbVersion = iMetadataConnection.getDbVersionString();
        String url = iMetadataConnection.getUrl();
        String additionalParams = iMetadataConnection.getAdditionalParams();

        if (EDatabaseTypeName.GENERAL_JDBC.getProduct().equals(dbType)) { // hywang for 9594
            info = new DbInfo(dbType, username, pwd, dbVersion, url, iMetadataConnection.getDriverClass(),
                    iMetadataConnection.getDriverJarPath(), additionalParams);
        } else if (EDatabaseTypeName.HIVE.getDisplayName().equals(iMetadataConnection.getDbType())) {
            String jobTracker = TalendTextUtils.removeQuotes(String.valueOf(iMetadataConnection
                    .getParameter(ConnParameterKeys.CONN_PARA_KEY_JOB_TRACKER_URL)));
            String nameNode = TalendTextUtils.removeQuotes(String.valueOf(iMetadataConnection
                    .getParameter(ConnParameterKeys.CONN_PARA_KEY_NAME_NODE_URL)));
            String thrifturi = null;
            if (HiveModeInfo.get(iMetadataConnection.getDbVersionString()) == HiveModeInfo.EMBEDDED) {
                thrifturi = "thrift://" + iMetadataConnection.getServerName() + ":" + iMetadataConnection.getPort();
            }
            info = new DbInfo(iMetadataConnection.getDbType(), iMetadataConnection.getUsername(),
                    iMetadataConnection.getPassword(), iMetadataConnection.getDbVersionString(), iMetadataConnection.getUrl(),
                    jobTracker, nameNode, thrifturi, iMetadataConnection.getDriverJarPath());
            String hiveServerVersion = String.valueOf(iMetadataConnection.getParameter(ConnParameterKeys.HIVE_SERVER_VERSION));
            String driverClass = ""; //$NON-NLS-1$
            if (HiveServerVersionInfo.HIVE_SERVER_2.getKey().equals(hiveServerVersion)) {
                driverClass = EDatabase4DriverClassName.HIVE2.getDriverClass();
            } else {
                driverClass = EDatabase4DriverClassName.HIVE.getDriverClass();
            }
            info.setDriverClassName(driverClass);
        } else {
            info = new DbInfo(dbType, username, pwd, dbVersion, url, additionalParams);
        }
        try {
            convertToProcessNode(connectionItem, tableName);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
    }

    public void convertToProcessNode(ConnectionItem connectionItem, String tableName) throws ProcessorException {

        EDatabaseComponentName name = EDatabaseComponentName.getCorrespondingComponentName(connectionItem,
                ERepositoryObjectType.METADATA_CONNECTIONS);
        String componentName = null;
        if(name != null){
            componentName = name.getDefaultComponentName();
        }
        if(componentName == null ){
            IComponentName rcSetting = null;
            for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
                rcSetting = handler.getCorrespondingComponentName(connectionItem, ERepositoryObjectType.METADATA_CONNECTIONS);
                if (rcSetting != null) {
                    break;
                }
            }
            if(rcSetting != null){
                componentName = rcSetting.getDefaultComponentName();
            }
        }

        IComponent dbInputComponent = ComponentsFactoryProvider.getInstance().get(componentName,
                ComponentCategory.CATEGORY_4_DI.getName());
        Process process = new Process(GuessSchemaProcess.getNewmockProperty());
        node = new Node(dbInputComponent, process);
        selectedContext = node.getProcess().getContextManager().getDefaultContext();

        // TDI-20011
        IMetadataTable table = UpdateRepositoryUtils.getTableByName(connectionItem, tableName);
        if (table == null) {
            table = new MetadataTable();
            table.setTableName(tableName);
            table.setLabel(tableName);
        }
        IElementParameter propertyParam = ((Node) node).getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        IElementParameter schemaParam = ((Node) node).getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
        if (schemaParam == null) {
            schemaParam = ((Node) node).getElementParameterFromField(EParameterFieldType.SCHEMA_REFERENCE);
        }
        String propertyId = connectionItem.getProperty().getId();
        String schema = databaseConnection.getUiSchema();
        String dbType = databaseConnection.getDatabaseType();
        String value = connectionItem.getProperty().getId() + " - " + table.getLabel(); //$NON-NLS-1$

        CompoundCommand cc = new CompoundCommand();
        // inital parameters command
        ChangeValuesFromRepository changeValueCommand = new ChangeValuesFromRepository(node, databaseConnection,
                propertyParam.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), propertyId); //$NON-NLS-1$
        changeValueCommand.ignoreContextMode(true);
        cc.add(changeValueCommand);

        // change metadata command
        RepositoryChangeMetadataCommand changeMetadataCommand = new RepositoryChangeMetadataCommand((Node) node,
                schemaParam.getName() + ":" //$NON-NLS-1$
                        + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), value, table, null, null);
        cc.add(changeMetadataCommand);

        // guess query command
        QueryGuessCommand queryGuessCommand = new QueryGuessCommand(node, node.getMetadataList().get(0), schema, dbType,
                databaseConnection);
        cc.add(queryGuessCommand);

        // execute the commands
        cc.execute();

        IElementParameter query = node.getElementParameterFromField(EParameterFieldType.MEMO_SQL);

        memoSQL = query.getValue().toString();
        String memoSQLTemp = TalendTextUtils.removeQuotesIfExist(memoSQL);
        if ((memoSQLTemp == null || memoSQLTemp.equals("")) && tableName != null && !tableName.equals("")) {
            boolean check = !Pattern.matches("^\\w+$", tableName);//$NON-NLS-1$
            boolean isJDBCForMysql = databaseConnection.getURL() == null ? false : databaseConnection.getURL().startsWith(
                    "jdbc:mysql");
            if ((dbType.equals(EDatabaseTypeName.MYSQL.getDisplayName()) || isJDBCForMysql) && check) {
                tableName = TalendQuoteUtils.addQuotes(tableName, TalendQuoteUtils.ANTI_QUOTE);
            }
            memoSQL = "select * from " + tableName;
            memoSQL = TalendTextUtils.addSQLQuotes(memoSQL);
        }
    }

    public CsvArray runMockProcess() throws ProcessorException {
        GuessSchemaProcess gsp = new GuessSchemaProcess(GuessSchemaProcess.getNewmockProperty(), node, selectedContext, memoSQL,
                info);
        return gsp.run();
    }
}
