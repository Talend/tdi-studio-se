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
package org.talend.designer.core.ui.editor.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.sqlbuilder.NotReallyNeedSchemaDBS;
import org.talend.metadata.managment.repository.ManagerConnection;

/**
 * cLi class global comment. Detailled comment
 */
public class TracesConnectionUtils {

    private static final String EMPTY = ""; //$NON-NLS-1$

    public static List<String> getEnabledTraceColumns(final IConnection conn) {
        List<String> enabledColumns = new ArrayList<String>();

        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        IMetadataTable table = conn.getMetadataTable();

        if (values != null && conn != null && !values.isEmpty()) {

            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (isTraceColumnEnabled(conn, line, column)) {
                    enabledColumns.add((String) column);
                }
            }
        } else if (table != null) {
            for (IMetadataColumn column : table.getListColumns()) {
                enabledColumns.add(column.getLabel());
            }
        }
        return enabledColumns;
    }

    private static boolean isTraceColumnEnabled(final IConnection conn, final Map<String, Object> line, final Object columnName) {
        if (conn == null || columnName == null || line == null || line.isEmpty()) {
            return false;
        }
        IMetadataTable table = conn.getMetadataTable();

        if (columnName instanceof String && isContainColumn(table, (String) columnName)) {
            Object checked = line.get(IConnection.TRACE_SCHEMA_COLUMN_CHECKED);
            if (checked != null) {
                if (checked instanceof Boolean && (Boolean) checked) {
                    return true;
                } else if (checked instanceof String && Boolean.TRUE.toString().equals(checked)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTracesColumnEnabled(final IConnection conn, final String columnName) {
        if (conn == null || columnName == null) {
            return false;
        }
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (values != null) {
            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (columnName.equals(column)) {
                    return isTraceColumnEnabled(conn, line, columnName);
                }
            }
        }
        return false;
    }

    public static String getTracesConditionSet(final IConnection conn) {
        IMetadataTable table = conn.getMetadataTable();
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (table != null && values != null) {
            StringBuffer conditions = new StringBuffer();

            boolean first = true;
            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (!isTraceColumnEnabled(conn, line, column)) {
                    continue;
                }
                String str = getTracesColumnCondition(line, conn, column);
                if (!EMPTY.equals(str)) {
                    if (first) {
                        conditions.append(str);
                        first = false;
                    } else {
                        conditions.append(" && " + str); //$NON-NLS-1$
                    }
                }
            }
            if (!first) { // contain one at least.
                String string = conditions.toString();
                if (!string.trim().equals(EMPTY)) {
                    return string;
                }
            }
        }
        return null;
    }

    private static String getTracesColumnCondition(Map<String, Object> line, IConnection conn, Object columnName) {
        if (line == null || line.isEmpty() || conn == null || columnName == null) {
            return EMPTY;
        }
        IMetadataTable table = conn.getMetadataTable();

        if (columnName instanceof String && isContainColumn(table, (String) columnName)) {
            Object condition = line.get(IConnection.TRACE_SCHEMA_COLUMN_CONDITION);
            if (condition != null && condition instanceof String) {
                String str = (String) condition;
                str = str.trim();
                if (!EMPTY.equals(str)) {
                    return str;
                }
            }
        }
        return EMPTY;
    }

    public static String getTracesColumnCondition(final IConnection conn, final String columnName) {
        if (conn == null || columnName == null) {
            return EMPTY;
        }
        IMetadataTable table = conn.getMetadataTable();
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (table != null && values != null) {

            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (columnName.equals(column)) {
                    return getTracesColumnCondition(line, conn, columnName);
                }
            }
        }
        return EMPTY;
    }

    public static void setTraceColumnValues(final IConnection conn, final String columnName, String condition, boolean enable) {
        if (conn == null || columnName == null || EMPTY.equals(columnName)) {
            return;
        }
        if (condition == null) {
            condition = EMPTY;
        }
        IMetadataTable table = conn.getMetadataTable();
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (table != null && values != null) {
            Map<String, Object> foundLine = null;
            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (columnName.equals(column)) {// found
                    foundLine = line;
                    break;
                }
            }
            if (foundLine == null) { // not found
                foundLine = new HashMap<String, Object>();
                foundLine.put(IConnection.TRACE_SCHEMA_COLUMN, columnName);
                values.add(foundLine);

            }
            foundLine.put(IConnection.TRACE_SCHEMA_COLUMN_CHECKED, new Boolean(enable));
            foundLine.put(IConnection.TRACE_SCHEMA_COLUMN_CONDITION, condition);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getTraceConnectionFilterValues(IConnection conn) {
        IElementParameter elementParameter = conn.getElementParameter(EParameterName.TRACES_CONNECTION_FILTER.getName());
        if (elementParameter != null) {
            Object value = elementParameter.getValue();
            if (value != null) {
                List<Map<String, Object>> values = (List<Map<String, Object>>) value;
                return values;
            }
        }
        return null;
    }

    private static boolean isContainColumn(IMetadataTable table, String column) {
        if (table != null && column != null) {
            for (IMetadataColumn col : table.getListColumns()) {
                if (col.getLabel().equals(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOC wzhang Comment method "setTraceFilterParameters".
     */
    public static void setTraceFilterParameters(INode node, IMetadataTable table, Set<String> preColumnSet,
            Map<String, String> changedNameColumns) {
        if (node != null && table != null && preColumnSet != null) {
            IConnection curConnection = getConnection(node.getOutgoingConnections(), table);
            if (curConnection != null) {
                Set<String> addedColumns = new HashSet<String>();
                for (IMetadataColumn curColumn : table.getListColumns()) {
                    if (!(preColumnSet.contains(curColumn.getLabel()))) {
                        addedColumns.add(curColumn.getLabel());
                    }
                }
                CorePlugin.getDefault().getDesignerCoreService()
                        .updateTraceColumnValues(curConnection, changedNameColumns, addedColumns);
            }
        }
    }

    /**
     * DOC wzhang Comment method "getConnection".
     */
    public static IConnection getConnection(List<? extends IConnection> connections, IMetadataTable table) {
        if (table != null && connections != null) {
            for (IConnection conn : connections) {
                IMetadataTable metaTable = conn.getMetadataTable();
                if (metaTable != null) {
                    String tabName = metaTable.getTableName();
                    if (tabName.equals(table.getTableName())) {
                        return conn;
                    }
                }
            }
        }
        return null;
    }

    /**
     * DOC hwang Comment method "createConnection".
     */
    public static DatabaseConnection createConnection(ConnectionParameters parameters) {
        ExtractMetaDataUtils extractMeta = ExtractMetaDataUtils.getInstance();
        String dbType = parameters.getDbType();
        boolean isNeedSchema = EDatabaseTypeName.getTypeFromDbType(dbType).isNeedSchema();
        String productName = EDatabaseTypeName.getTypeFromDisplayName(dbType).getProduct();
        // boolean isOralceWithSid = productName.equals(EDatabaseTypeName.ORACLEFORSID.getProduct());

        String schema = parameters.getSchema();
        EDatabaseTypeName type = EDatabaseTypeName.getTypeFromDbType(dbType);
        if (ManagerConnection.isSchemaFromSidOrDatabase(type)) {
            schema = parameters.getDbName();
        }

        if ("".equals(schema) && EDatabaseTypeName.INFORMIX.getProduct().equals(productName)) { //$NON-NLS-1$
            schema = parameters.getUserName();
        }

        if (EDatabaseTypeName.EXASOL.getProduct().equals(productName)) {
            schema = parameters.getDbName();
        }

        boolean isSchemaInValid = (schema == null) || (schema.equals("\'\'")) || (schema.equals("\"\"")) //$NON-NLS-1$ //$NON-NLS-2$
                || (schema.trim().equals("")); //$NON-NLS-1$
        // from 616 till line 622 modified by hyWang
        NotReallyNeedSchemaDBS dbs = new NotReallyNeedSchemaDBS();
        dbs.init();
        List<String> names = dbs.getNeedSchemaDBNames();
        boolean ifNeedSchemaDB = names.contains(productName);
        if (isNeedSchema && isSchemaInValid && !ifNeedSchemaDB) {
            parameters.setConnectionComment(Messages.getString("TracesConnectionUtils.connectionComment")); //$NON-NLS-1$
            return null;
        }
        DatabaseConnection connection = ConnectionFactory.eINSTANCE.createDatabaseConnection();
        connection.setFileFieldName(parameters.getFilename());
        connection.setDatabaseType(dbType);
        connection.setUsername(parameters.getUserName());
        connection.setPort(parameters.getPort());
        connection.setRawPassword(parameters.getPassword());
        if (dbType != null && dbType.equals(EDatabaseTypeName.ORACLE_OCI.getDisplayName())
                && parameters.getLocalServiceName() != null && !"".equals(parameters.getLocalServiceName())) {
            connection.setSID(parameters.getLocalServiceName());
        } else {
            connection.setSID(parameters.getDbName());
        }
        connection.setLabel(parameters.getDbName());
        connection.setDatasourceName(parameters.getDatasource());
        if (parameters.getDbType().equals(EDatabaseTypeName.GODBC.getDisplayName())
                && StringUtils.isEmpty(parameters.getDatasource())) {
            connection.setDatasourceName(parameters.getDbName());
        }
        if ("".equals(connection.getLabel())) { //$NON-NLS-1$
            connection.setLabel(parameters.getDatasource());
        }
        String driverClassByDbType = null;
        if (parameters.getDriverClass() != null) {
            driverClassByDbType = parameters.getDriverClass();
        } else {
            driverClassByDbType = extractMeta.getDriverClassByDbType(dbType);
        }
        String driverJar = parameters.getDriverJar();
        connection.setDriverClass(driverClassByDbType);
        connection.setDriverJarPath(driverJar);
        String databaseType = connection.getDatabaseType();
        if (driverClassByDbType != null && !"".equals(driverClassByDbType)
                && EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(parameters.getDbType())) {
            if (driverClassByDbType.startsWith("\"") && driverClassByDbType.endsWith("\"")) {
                driverClassByDbType = TalendTextUtils.removeQuotes(driverClassByDbType);
            }
            String dbTypeByClassName = "";
            if (driverJar != null && !"".equals(driverJar)) {
                dbTypeByClassName = extractMeta.getDbTypeByClassNameAndDriverJar(driverClassByDbType, driverJar);
            } else {
                dbTypeByClassName = extractMeta.getDbTypeByClassName(driverClassByDbType);
            }

            if (dbTypeByClassName != null) {
                databaseType = dbTypeByClassName;
            }
        }

        final String product = EDatabaseTypeName.getTypeFromDisplayName(databaseType).getProduct();
        ;
        connection.setProductId(product);
        if (MetadataTalendType.getDefaultDbmsFromProduct(product) != null) {
            final String mapping = MetadataTalendType.getDefaultDbmsFromProduct(product).getId();
            connection.setDbmsId(mapping);
        }

        if (!isSchemaInValid && isNeedSchema) {
            schema = schema.replaceAll("\'", ""); //$NON-NLS-1$ //$NON-NLS-2$
            schema = schema.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
            connection.setUiSchema(schema);
        }
        connection.setServerName(parameters.getHost());
        connection.setAdditionalParams(parameters.getJdbcProperties());

        connection.setURL(parameters.getCombineURL());

        connection.setDBRootPath(parameters.getDirectory());
        connection.setDbVersionString(parameters.getDbVersion());

        // Added by Marvin Wang to add other parameters.
        Map<String, String> params = parameters.getParameters();
        if (params != null && params.size() > 0) {
            Set<Entry<String, String>> collection = params.entrySet();
            for (Entry<String, String> para : collection) {
                connection.getParameters().put(para.getKey(), para.getValue());
            }
        }
        
        EDatabaseTypeName typeName = EDatabaseTypeName.getTypeFromDbType(dbType);
        if (ManagerConnection.isSchemaFromSidOrDatabase(typeName)) {
        	connection.setUiSchema(schema);
        }

        return connection;
    }

    public static boolean isJobletInnerConnection(IConnection conn) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
            IJobletProviderService jobletServer = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (jobletServer != null && conn != null) {
                INode jobletNode = conn.getSource().getJobletNode();
                // connection's target and source are in same joblet node.
                if (jobletNode != null && (jobletNode == conn.getTarget().getJobletNode())
                        && jobletServer.isJobletComponent(jobletNode)) {
                    return true; // joblet's inner connection
                }
            }
        }
        return false;
    }

}
