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
package org.talend.designer.core.ui.editor.cmd;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Priority;
import org.eclipse.gef.commands.Command;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRuleService;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.utils.DatabaseConnectionParameterUtil;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.utils.sql.metadata.constants.GetTable;

import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * This class is used for generating new query when "Guess Query" button is selected. <br/>
 *
 */
public class QueryGuessCommand extends Command {

    private final IElement node;

    private String propName;

    private Object oldValue;

    private final IMetadataTable newOutputMetadataTable;

    private Map<String, String> dbNameAndDbTypeMap;

    private Map<String, String> dbNameAndSchemaMap;

    private String realTableId;

    private String realTableName;

    private String realDBType;

    private String schema;

    private String dbType;

    private Connection conn; // hywang add for 9594

    private IProcess process;

    // Added TDQ-5616 yyin 20121206
    private String whereClause = null;

    /**
     * The property is defined in an element, which can be either a node or a connection.
     *
     * @param node
     * @param propName
     * @param newOutputMetadataTable
     */
    public QueryGuessCommand(IElement node, IMetadataTable newOutputMetadataTable) {
        this.node = node;
        this.newOutputMetadataTable = newOutputMetadataTable;
    }

    public QueryGuessCommand(INode node2, IMetadataTable metadataTable, Connection conn) {// 9594
        this(node2, metadataTable);
        this.conn = conn;
    }

    /**
     * DOC qzhang QueryGuessCommand constructor comment.
     *
     * @param node2
     * @param metadataTable
     * @param schema
     * @param dbType
     */
    public QueryGuessCommand(INode node2, IMetadataTable metadataTable, String schema, String dbType) {
        this(node2, metadataTable);
        this.schema = schema;
        this.dbType = dbType;
    }

    public QueryGuessCommand(INode node2, IMetadataTable metadataTable, String schema, String dbType, Connection conn) {// 9594
        this(node2, metadataTable);
        if (!"".equals(schema)) {
            this.schema = schema;
        }
        this.dbType = dbType;
        this.conn = conn;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        IElementParameter dqRuler = node.getElementParameter("DQRULES_LIST");
        String newQuery;
        if (dqRuler == null || "".equals(dqRuler.getValue())) {
            newQuery = generateNewQuery();
        } else {
            newQuery = generateNewQueryFromDQRuler(dqRuler);
        }

        for (IElementParameter param : (List<IElementParameter>) node.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                oldValue = node.getPropertyValue(param.getName());
                this.propName = param.getName();
                String sql = null;
                try {
                    // sql = new SQLFormatUtil().formatSQL(newQuery);
                    if (QueryUtil.needFormatSQL(dbType)) {
                        sql = fomatQuery(newQuery);
                    } else {
                        sql = newQuery;
                    }
                    node.setPropertyValue(param.getName(), sql);
                } catch (Exception e) {
                    ExceptionHandler.process(e, Priority.WARN);
                    node.setPropertyValue(param.getName(), newQuery);
                }

                param.setRepositoryValueUsed(false);
            }
        }

        node.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);

        if (this.node instanceof Node) {
            ((Node) this.node).checkAndRefreshNode();
        }

        // Ends
    }

    private String generateNewQueryFromDQRuler(IElementParameter dqRulerParam) {
        ITDQRuleService rulerService = null;
        try {
            rulerService = GlobalServiceRegister.getDefault().getService(ITDQRuleService.class);
        } catch (RuntimeException e) {
            // nothing to do
        }
        if (rulerService != null) {
            IElementParameter existConnection = node.getElementParameter("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
            boolean useExistConnection = (existConnection == null ? false : (Boolean) existConnection.getValue());
            INode connectionNode = null;
            if (node != null && node instanceof INode) {
                process = ((INode) node).getProcess();
            }
            if (useExistConnection && process != null) {
                IElementParameter connector = node.getElementParameter("CONNECTION");
                if (connector != null) {
                    String connectorValue = connector.getValue().toString();
                    List<? extends INode> graphicalNodes = process.getGeneratingNodes();
                    for (INode node : graphicalNodes) {
                        if (node.getUniqueName().equals(connectorValue)) {
                            connectionNode = node;
                            break;
                        }
                    }
                }
            }

            IElementParameter typeParam = node.getElementParameter("TYPE"); //$NON-NLS-1$
            IElementParameter dbParam = node.getElementParameter(EParameterName.DBNAME.getName());
            IContext lastRunContext = ((IProcess2) process).getLastRunContext();
            String dbName = JavaProcessUtil
                    .getRealParamValue(process,
                            dbParam == null ? org.apache.commons.lang.StringUtils.EMPTY : dbParam.getValue().toString(),
                            lastRunContext);
            IElementParameter schemaParam = node.getElementParameter(EParameterName.SCHEMA_DB.getName());
            String schemaName = JavaProcessUtil.getRealParamValue(process,
                    schemaParam == null ? org.apache.commons.lang.StringUtils.EMPTY : schemaParam.getValue().toString(),
                    lastRunContext);
            IElementParameter tableParam = node.getElementParameterFromField(EParameterFieldType.DBTABLE);
            String tableName = JavaProcessUtil.getRealParamValue(process, tableParam.getValue().toString(), lastRunContext);
            IElementParameter whereClause = node.getElementParameter("WHERE_CLAUSE"); //$NON-NLS-1$
            String whereStr = org.apache.commons.lang.StringUtils.EMPTY;
            if (whereClause.getValue() != null) {
                whereStr = JavaProcessUtil.getRealParamValue(process, whereClause.getValue().toString(), lastRunContext);
            }

            if (connectionNode != null) {
                typeParam = connectionNode.getElementParameter("TYPE"); //$NON-NLS-1$
                dbParam = connectionNode.getElementParameter(EParameterName.DBNAME.getName());
                dbName = JavaProcessUtil
                        .getRealParamValue(process, dbParam == null ? org.apache.commons.lang.StringUtils.EMPTY
                                : dbParam.getValue().toString(), lastRunContext);
                schemaParam = connectionNode.getElementParameter(EParameterName.SCHEMA_DB.getName());
                schemaName = JavaProcessUtil.getRealParamValue(process,
                        schemaParam == null ? org.apache.commons.lang.StringUtils.EMPTY : schemaParam.getValue().toString(),
                        lastRunContext);

            }

            List<IMetadataTable> metadataList = null;
            IMetadataTable metadataTable = null;
            if (node instanceof Node) {
                metadataList = ((Node) node).getMetadataList();
                if (metadataList != null && !metadataList.isEmpty()) {
                    metadataTable = metadataList.get(0);
                }
            }
            return rulerService.getQueryByRule(dqRulerParam, typeParam, dbName, schemaName, tableName, metadataTable, node
                    .getElementName().contains("Invalid"), whereStr); //$NON-NLS-1$
        }
        return org.apache.commons.lang.StringUtils.EMPTY;
    }

    private String generateNewQuery() {
        // used for generating new Query.
        ExtractMetaDataUtils extractMeta = ExtractMetaDataUtils.getInstance();
        if (realDBType != null) {
            dbType = realDBType;
        }
        //
        if (node != null && node instanceof INode) {
            process = ((INode) node).getProcess();
        }
        if (this.realTableId != null && this.dbNameAndDbTypeMap.containsKey(this.realTableId)) {
            dbType = this.dbNameAndDbTypeMap.get(this.realTableId);
        }
        if (dbType == null || dbType.equals("")) { //$NON-NLS-1$
            IElementParameter ptParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (ptParam != null && ptParam.getRepositoryValue() != null) {
                if (ptParam.getRepositoryValue().endsWith(EDatabaseTypeName.GENERAL_JDBC.getProduct())) {
                    dbType = EDatabaseTypeName.GENERAL_JDBC.getDisplayName();
                }
            }
        }
        boolean isJdbc = false;
        INode connectionNode = null;

        IElementParameter existConnection = node.getElementParameter("USE_EXISTING_CONNECTION");
        boolean useExistConnection = (existConnection == null ? false : (Boolean) existConnection.getValue());

        if (useExistConnection) {
            IElementParameter connector = node.getElementParameter("CONNECTION");
            if (connector != null) {
                String connectorValue = connector.getValue().toString();
                for (INode generatingNode : process.getGraphicalNodes()) {
                    if (generatingNode.getUniqueName().equals(connectorValue)) {
                        connectionNode = generatingNode;
                        break;
                    }
                }
                if (connectionNode == null) {
                    List<? extends INode> graphicalNodes = process.getGeneratingNodes();
                    for (INode node : graphicalNodes) {
                        if (node.getUniqueName().equals(connectorValue)) {
                            connectionNode = node;
                            break;
                        }
                    }
                    // for visual connection component in joblet
                    if (connectionNode == null && node instanceof INode) {
                        List<IMultipleComponentManager> multipleComponentManagers = ((INode) node).getComponent()
                                .getMultipleComponentManagers();
                        for (IMultipleComponentManager manager : multipleComponentManagers) {
                            String inName = manager.getInput().getName();
                            String componentValue = connectorValue + "_" + inName;
                            for (INode gnode : process.getGeneratingNodes()) {
                                if (gnode.getUniqueName().equals(componentValue) && (gnode instanceof INode)) {
                                    connectionNode = gnode;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        // hywang add for bug 7575
        String jdbcDriverClassName = null;
        String jdbcDriverJarName = null;
        if (dbType != null && dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getProduct())) {
            isJdbc = true;
            boolean isGeneralJDBC = dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName());

            String driverClassName = null;
            if (isGeneralJDBC) {
                driverClassName = node.getElementParameter("DRIVER_CLASS").getValue().toString();
                if (connectionNode != null) {
                    driverClassName = connectionNode.getElementParameter("DRIVER_CLASS").getValue().toString();
                }
            } else {
                IElementParameter elementParameter = node.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_CLASS
                        .getDisplayName());
                if (elementParameter == null) {
                    // for mr process , it still use old JDBC component but can use new connection from repository
                    elementParameter = node.getElementParameter("DRIVER_CLASS");
                }
                driverClassName = elementParameter.getValue().toString();
                if (connectionNode != null) {
                    driverClassName = connectionNode
                            .getElementParameter(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName()).getValue()
                            .toString();
                }
            }

            driverClassName = TalendTextUtils.removeQuotes(driverClassName);
            //
            if (driverClassName != null && !"".equals(driverClassName)) {
                boolean isContextModeDriverClass = ContextParameterUtils.containContextVariables(driverClassName);
                if (isContextModeDriverClass) {
                    driverClassName = JavaProcessUtil.getContextOriginalValue(process, driverClassName);
                }
            }
            // specil handle Sybase Database's driverClassName
            if (driverClassName != null && !"".equals(driverClassName)) {
                if (driverClassName.equals("com.sybase.jdbc3.jdbc.SybDataSource")) {
                    driverClassName = EDatabase4DriverClassName.SYBASEASE.getDriverClass();
                }
            }

            // DRIVER_JAR:
            String driverJarName = null;
            if (isGeneralJDBC) {
                driverJarName = node.getElementParameter("DRIVER_JAR").getValue().toString();
                if (connectionNode != null) {
                    driverJarName = connectionNode.getElementParameter("DRIVER_JAR").getValue().toString();
                }
            } else {
                IElementParameter elementParameter = node.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_JAR
                        .getDisplayName());
                if (elementParameter == null) {
                    // for mr process , it still use old JDBC component but can use new connection from repository
                    elementParameter = node.getElementParameter("DRIVER_JAR");
                }
                driverJarName = elementParameter.getValue().toString();

                if (connectionNode != null) {
                    driverJarName = connectionNode
                            .getElementParameter(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName()).getValue()
                            .toString();
                }
            }
            if (driverJarName != null && driverJarName.startsWith("[") && driverJarName.endsWith("]")) {
                driverJarName = driverJarName.substring(1, driverJarName.length() - 1);
                if (driverJarName != null && driverJarName.startsWith("{") && driverJarName.endsWith("}")) {
                    driverJarName = driverJarName.substring(1, driverJarName.length() - 1);
                }
            }
            if (driverJarName != null && !"".equals(driverJarName)) {
                boolean isContextMode = ContextParameterUtils.containContextVariables(driverJarName);
                if (isContextMode) {
                    driverJarName = JavaProcessUtil.getContextOriginalValue(process, driverJarName);
                }
                dbType = extractMeta.getDbTypeByClassNameAndDriverJar(driverClassName, driverJarName);
            } else {
                dbType = extractMeta.getDbTypeByClassName(driverClassName);
            }

            DatabaseConnection dbConn = null;
            if (dbType == null) { // handle context mode
                if (conn != null) {
                    if (conn instanceof DatabaseConnection) {
                        dbConn = (DatabaseConnection) conn;
                    }
                    driverClassName = DatabaseConnectionParameterUtil.getTrueParamValue(dbConn, driverClassName);
                    dbType = extractMeta.getDbTypeByClassName(driverClassName);
                }
            }

            jdbcDriverJarName = driverJarName;
            jdbcDriverClassName = driverClassName;

            if (dbType == null) {
                // TDQ-15039: for the unknown JDBC type connection, make sure we can generate the correct query.
                // at least for BigQuery, the QUOTATION_MARK is the same with mysql.
                if (driverJarName.contains("BigQuery") || driverClassName.contains("BigQuery")) {
                    dbType = EDatabaseTypeName.MYSQL.getDisplayName();
                } else {
                    // if we can not get the DB Type from the existing driver list, just set back the type to ORACLE
                    // since it's one DB unknown from Talend.
                    // it might not work directly for all DB, but it will generate a standard query.
                    dbType = EDatabaseTypeName.ORACLE_OCI.getDisplayName();
                }
            }
            // data view锛� conn=null
            // need add code here for dbtype(oracle)
        }

        if (dbNameAndSchemaMap != null) {
            schema = this.dbNameAndSchemaMap.get(this.realTableId);
        }
        String propertyType = (String) node.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
        boolean isTeradata = false;
        if (dbType != null) {
            isTeradata = dbType.equalsIgnoreCase(EDatabaseTypeName.TERADATA.getDisplayName());
        }
        if (propertyType != null && !propertyType.equals(EmfComponent.REPOSITORY)) {
            List<? extends IElementParameter> elementParameters = this.node.getElementParameters();
            if (useExistConnection) {
                elementParameters = connectionNode.getElementParameters();
            }
            for (IElementParameter param : elementParameters) {
                if (param.getRepositoryValue() != null) {
                    if ((!isTeradata && param.getRepositoryValue().equals("SCHEMA")) //$NON-NLS-1$
                            || (isTeradata && param.getRepositoryValue().equals("SID"))) {// check if dbtype is //$NON-NLS-1$
                        // Teradata, always keep the
                        // query style like
                        // "dbname.tablename.columnname" on build-in mode
                        schema = (String) param.getValue();
                        schema = schema.replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        schema = schema.replace("\'", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        break;
                    }
                }
            }
        } else if (schema == null) {
            IElementParameter param = node.getElementParameter(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
            if (param != null) {
                try {
                    IRepositoryViewObject object = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                            .getLastVersion((String) param.getValue());
                    if (object != null) {
                        Item item = object.getProperty().getItem();
                        if (item != null && item instanceof DatabaseConnectionItem) {

                            if (isTeradata) {
                                schema = (String) RepositoryToComponentProperty.getValue(
                                        ((DatabaseConnectionItem) item).getConnection(), "SID", null); //$NON-NLS-1$
                            } else {
                                schema = (String) RepositoryToComponentProperty.getValue(
                                        ((DatabaseConnectionItem) item).getConnection(), "SCHEMA", null); //$NON-NLS-1$
                            }
                            schema = TalendTextUtils.removeQuotes(schema);
                        }
                    }
                } catch (PersistenceException e) {
                    //
                }
            }

        }

        // if (conn instanceof DatabaseConnection && conn.isContextMode()) {
        // schema = DatabaseConnectionParameterUtil.getContextTrueValue((DatabaseConnection) conn, schema);
        // }

        String newQuery = null;
        // Need update schema if table type as calculation view for SAP Hana Database
        updateSchema(dbType, newOutputMetadataTable);
        realTableName = QueryUtil.getTableName(node, newOutputMetadataTable, schema, dbType, realTableName);

        if (realTableName.startsWith(TalendTextUtils.QUOTATION_MARK) && realTableName.endsWith(TalendTextUtils.QUOTATION_MARK)
                && realTableName.length() > 2) {
            realTableName = realTableName.substring(1, realTableName.length() - 1);
        }
        if (conn != null
                && (isJdbc || dbType.equals(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName()) || (StringUtils.isEmpty(schema) && (EDatabaseTypeName.ORACLE_CUSTOM
                        .equals(EDatabaseTypeName.getTypeFromDbType(dbType))
                        || EDatabaseTypeName.ORACLEFORSID.equals(EDatabaseTypeName.getTypeFromDbType(dbType))
                        || EDatabaseTypeName.ORACLESN.equals(EDatabaseTypeName.getTypeFromDbType(dbType)) || EDatabaseTypeName.ORACLE_OCI
                            .equals(EDatabaseTypeName.getTypeFromDbType(dbType)))))) {
            schema = getDefaultSchema(realTableName);
        }

        newQuery = QueryUtil.generateNewQuery(node, newOutputMetadataTable, isJdbc, dbType, schema, realTableName);

        // Added yyin TDQ-5616: if there are where clause, append it to the query
        if (whereClause != null) {// the where clause is inputted by the user, so no need to modify it.
            newQuery = newQuery.substring(0, newQuery.length() - 1) + whereClause + "\"";//$NON-NLS-1$
        }// ~

        // TDQ-15039: support bigQuery can generate correct query
        if ((jdbcDriverJarName != null && jdbcDriverJarName.contains("BigQuery"))
                || (jdbcDriverClassName != null && jdbcDriverClassName.contains("BigQuery"))) {
            // remove the first schema. from the select
            newQuery = "\"SELECT " + newQuery
                    .substring(newQuery
                            .indexOf(TalendTextUtils.ANTI_QUOTE + realTableName + TalendTextUtils.ANTI_QUOTE
                                    + TalendTextUtils.JAVA_END_STRING));
        }
        // TDQ-15039~

        return TalendTextUtils.addSQLQuotes(newQuery);
    }

    // Added TDQ-5616 yyin 20121206
    public void setWhereClause(String clause) {
        this.whereClause = clause;
    }

    // get DatabaseConnection
    protected DatabaseConnection getConnection(ConnectionItem connectionItem) {
        return (DatabaseConnection) connectionItem.getConnection();
    }

    @Override
    public void undo() {
        node.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
        if (propName != null && oldValue != null) {
            node.setPropertyValue(propName, oldValue);
        }
    }

    @Override
    public void redo() {
        this.execute();
    }

    /**
     * Sets a set of maps what used for generating new Query.
     *
     * @param dbNameAndDbTypeMap
     * @param dbNameAndSchemaMap
     * @param getRepositoryTableMap
     */
    public void setMaps(Map<String, String> dbNameAndDbTypeMap, Map<String, String> dbNameAndSchemaMap,
            Map<String, IMetadataTable> repositoryTableMap) {
        this.dbNameAndDbTypeMap = dbNameAndDbTypeMap;
        this.dbNameAndSchemaMap = dbNameAndSchemaMap;
    }

    /**
     * Sets the real table id and table name.
     *
     * @param realTableId
     * @param realTableName
     * @param type
     */
    public void setParameters(String realTableId, String realTableName, String type) {
        this.realTableId = realTableId;
        this.realTableName = realTableName;
        realDBType = type;
    }

    /*
     *
     * hyWang Method formatQuery
     */
    private String fomatQuery(String query) {
        String lastPartA, lastPartB;
        StringBuffer buffer = new StringBuffer();
        String[] s = query.split(","); //$NON-NLS-1$
        buffer.append(s[0]);
        if (s.length > 1) {
            for (int i = 1; i < s.length - 1; i++) {
                s[i] = s[i].trim();
                buffer.append("," + "\n" + "\t" + "\t" + s[i]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            // for bug 13265
            String[] last = s[s.length - 1].split(" FROM "); //$NON-NLS-1$
            lastPartA = last[0].trim() + "\n"; //$NON-NLS-1$
            lastPartB = "FROM" + "\t" + last[1].trim(); //$NON-NLS-1$ //$NON-NLS-2$
            s[s.length - 1] = lastPartA + lastPartB;
            buffer.append("," + "\n" + "\t" + "\t" + s[s.length - 1]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } else {
            String[] temp = s[0].split("FROM"); //$NON-NLS-1$
            if (temp.length > 1) {
                lastPartA = temp[0] + "\n"; //$NON-NLS-1$
                lastPartB = "FROM" + "\t" + temp[1]; //$NON-NLS-1$ //$NON-NLS-2$
                buffer = new StringBuffer();
                buffer.append(lastPartA);
                buffer.append(lastPartB);
            }
        }
        return buffer.toString();
    }

    private void updateSchema(String dbType, IMetadataTable metadataTable) {
        if (dbType != null && EDatabaseTypeName.SAPHana.getDisplayName().equals(dbType)) {
            if (metadataTable != null) {
                Map<String, String> properties = metadataTable.getAdditionalProperties();
                if (properties.containsKey(GetTable.TABLE_SCHEM.name())) {
                    schema = properties.get(GetTable.TABLE_SCHEM.name());
                }
            }
        }
    }

    private String getDefaultSchema(String realTableName) {
        String schema = "";
        List<Schema> schemas = ConnectionHelper.getSchema(this.conn);
        Set<Catalog> catalog = ConnectionHelper.getAllCatalogs(this.conn);
        for (Schema deScha : schemas) {
            for (ModelElement ele : deScha.getOwnedElement()) {
                String childeleName = TalendTextUtils.addQuotesWithSpaceFieldForSQLStringForce(
                        TalendTextUtils.declareString(ele.getName()), dbType, true);
                if (childeleName.startsWith(TalendTextUtils.QUOTATION_MARK)
                        && childeleName.endsWith(TalendTextUtils.QUOTATION_MARK) && childeleName.length() > 2) {
                    childeleName = childeleName.substring(1, childeleName.length() - 1);
                }
                if (childeleName.equals(realTableName)) {
                    return deScha.getName();
                }
            }
        }

        for (Catalog cata : catalog) {
            for (ModelElement ele : cata.getOwnedElement()) {
                if (ele instanceof Schema) {
                    for (ModelElement child : ((Schema) ele).getOwnedElement()) {
                        String childeleName = TalendTextUtils.addQuotesWithSpaceFieldForSQLStringForce(
                                TalendTextUtils.declareString(child.getName()), dbType, true);
                        if (childeleName.startsWith(TalendTextUtils.QUOTATION_MARK)
                                && childeleName.endsWith(TalendTextUtils.QUOTATION_MARK) && childeleName.length() > 2) {
                            childeleName = childeleName.substring(1, childeleName.length() - 1);
                        }
                        if (childeleName.equals(realTableName)) {
                            return ele.getName();
                        }
                    }
                } else {
                    String eleName = TalendTextUtils.addQuotesWithSpaceFieldForSQLStringForce(
                            TalendTextUtils.declareString(ele.getName()), dbType, true);
                    if (eleName.startsWith(TalendTextUtils.QUOTATION_MARK) && eleName.endsWith(TalendTextUtils.QUOTATION_MARK)
                            && eleName.length() > 2) {
                        eleName = eleName.substring(1, eleName.length() - 1);
                    }
                    if (eleName.equals(realTableName)) {
                        return cata.getName();
                    }
                }
            }
        }
        return schema;
    }
}
