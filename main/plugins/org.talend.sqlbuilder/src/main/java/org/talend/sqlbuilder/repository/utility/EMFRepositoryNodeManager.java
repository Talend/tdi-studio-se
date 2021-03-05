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
package org.talend.sqlbuilder.repository.utility;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.MetadataColumnRepositoryObject;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.MetadataTableRepositoryObject;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.erdiagram.ui.ErDiagramComposite;
import org.talend.sqlbuilder.util.QueryTokenizer;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public final class EMFRepositoryNodeManager {

    private Preferences prefs = SqlBuilderPlugin.getDefault().getPluginPreferences();

    private String queryDelimiter = prefs.getString(IConstants.QUERY_DELIMITER);

    private String alternateDelimiter = prefs.getString(IConstants.ALTERNATE_DELIMITER);

    private String commentDelimiter = prefs.getString(IConstants.COMMENT_DELIMITER);

    public static final String TABLE_ALIAS_PREFIX = "TableAlias:"; //$NON-NLS-1$

    public static final String COLUMN_ALIAS_PREFIX = "ColumnAlias:"; //$NON-NLS-1$

    private static EMFRepositoryNodeManager instance = new EMFRepositoryNodeManager();

    private DatabaseMetaData dbMetaData;

    private SQLBuilderRepositoryNodeManager rnmanager = new SQLBuilderRepositoryNodeManager();

    private String leftDbQuote;

    private String rightDbQuote;

    /**
     * qzhang EMFRepositoryNodeManager constructor comment.
     */
    private EMFRepositoryNodeManager() {
    }

    /**
     * dev Comment method "getQueryByLabel".
     *
     * @param node all repository node Type
     * @param label search query label
     * @return
     */
    @SuppressWarnings("unchecked")
    public Query getQueryByLabel(RepositoryNode node, String label) {
        root = null;
        if (node.getObjectType().equals(ERepositoryObjectType.METADATA_CON_QUERY)) {
            root = node.getParent().getParent();
        } else {
            root = SQLBuilderRepositoryNodeManager.getRoot(node);
        }
        if (root == null) {
            return null;
        }
        DatabaseConnectionItem item = SQLBuilderRepositoryNodeManager.getItem(root);
        DatabaseConnection connection = (DatabaseConnection) item.getConnection();
        QueriesConnection queriesConnection = connection.getQueries();
        if (queriesConnection != null) {
            List<Query> queries = queriesConnection.getQuery();
            for (Query query : queries) {
                if (query.getLabel().equals(label)) {
                    return query;
                }
            }
        }
        return null;
    }

    private RepositoryNode root;

    @SuppressWarnings("unchecked")
    public List<MetadataTable> getTables(List<IRepositoryNode> nodes, List<MetadataColumn> selectedColumns) {
        List<MetadataTable> tables = new ArrayList<MetadataTable>();
        for (IRepositoryNode node : nodes) {
            RepositoryNodeType type = SQLBuilderRepositoryNodeManager.getRepositoryType((RepositoryNode) node);
            if (type == RepositoryNodeType.DATABASE) {
                root = (RepositoryNode) node;
                DatabaseConnection connection = (DatabaseConnection) SQLBuilderRepositoryNodeManager
                        .getItem((RepositoryNode) node).getConnection();
                for (MetadataTable table : ConnectionHelper.getTables(connection)) {
                    if (!tables.contains(table)) {
                        tables.add(table);
                        selectedColumns.addAll(table.getColumns());
                    }
                }
                // if database is selected , It does not need to check others.
                break;
            } else if (type == RepositoryNodeType.TABLE) {
                MetadataTable table = ((MetadataTableRepositoryObject) node.getObject()).getTable();
                if (!tables.contains(table)) {
                    tables.add(table);
                    selectedColumns.addAll(table.getColumns());
                }
                root = SQLBuilderRepositoryNodeManager.getRoot((RepositoryNode) node);

            } else if (type == RepositoryNodeType.COLUMN) {

                MetadataColumn column = ((MetadataColumnRepositoryObject) node.getObject()).getColumn();
                if (!selectedColumns.contains(column)) {
                    selectedColumns.add(column);
                }

                MetadataTable table = column.getTable();
                if (!tables.contains(table)) {
                    tables.add(table);
                }
                root = SQLBuilderRepositoryNodeManager.getRoot((RepositoryNode) node);
            }
            setRoot(null);
        }
        return tables;
    }

    /**
     * qzhang Comment method "setRoot".
     */
    public void setRoot(RepositoryNode rootNode) {
        // IMetadataConnection iMetadataConnection;
        if (rootNode != null) {
            root = rootNode;
        }
    }

    public List<String[]> getPKFromTables(List<MetadataTable> tables, String selectedContext) {
        List<String[]> fks = new ArrayList<String[]>();
        String fk = ""; //$NON-NLS-1$
        String pk = ""; //$NON-NLS-1$
        IMetadataConnection iMetadataConnection = null;
        if (root != null) {
            try {
                DatabaseConnection databaseConnection = (DatabaseConnection) SQLBuilderRepositoryNodeManager.getItem(root)
                        .getConnection();
                iMetadataConnection = ConvertionHelper.convert(databaseConnection, false, selectedContext);
                iMetadataConnection.setAdditionalParams(ConvertionHelper.convertAdditionalParameters(databaseConnection));
                dbMetaData = rnmanager.getDatabaseMetaData(iMetadataConnection);
            } catch (final Exception e) {
                final String mainMsg = Messages.getString("EMFRepositoryNodeManager.DBConnection.Text"); //$NON-NLS-1$
                Display.getDefault().syncExec(new Runnable() {

                    /*
                     * (non-Javadoc)
                     *
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        new ErrorDialogWidthDetailArea(DisplayUtils.getDefaultShell(false), SqlBuilderPlugin.PLUGIN_ID, mainMsg,
                                e.getMessage());
                    }
                });

            }
        }
        for (MetadataTable table : tables) {
            try {
                if (dbMetaData != null && table.getSourceName() != null) {
                    ResultSet resultSet;
                    if (dbMetaData.supportsSchemasInDataManipulation() && !"".equals(iMetadataConnection.getSchema())) { //$NON-NLS-1$
                        // bug 0006949 added
                        if (dbMetaData.getCatalogs() != null) {
                            resultSet = dbMetaData.getExportedKeys(null, iMetadataConnection.getSchema(), table.getSourceName());
                        } else {
                            resultSet = dbMetaData.getExportedKeys("", iMetadataConnection.getSchema(), table.getSourceName()); //$NON-NLS-1$
                        }
                    } else {
                        if (dbMetaData.getCatalogs() != null) {
                            resultSet = dbMetaData.getExportedKeys(null, iMetadataConnection.getSchema(), table.getSourceName());
                        } else {
                            resultSet = dbMetaData.getExportedKeys("", iMetadataConnection.getSchema(), table.getSourceName()); //$NON-NLS-1$
                        }
                    }
                    if (resultSet != null) {
                        ResultSetMetaData metadata = resultSet.getMetaData();
                        if (metadata != null) {
                            int[] relevantIndeces = new int[metadata.getColumnCount()];
                            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                                relevantIndeces[i - 1] = i;
                            }

                            while (resultSet.next()) {
                                for (int relevantIndece : relevantIndeces) {
                                    String key = metadata.getColumnName(relevantIndece);
                                    if (key.toUpperCase().equals("FKCOLUMN_NAME")) { //$NON-NLS-1$
                                        fk += resultSet.getString(relevantIndece);
                                    } else if (key.toUpperCase().equals("FKTABLE_NAME")) { //$NON-NLS-1$
                                        fk = resultSet.getString(relevantIndece) + "."; //$NON-NLS-1$
                                    } else if (key.toUpperCase().equals("PKCOLUMN_NAME")) { //$NON-NLS-1$
                                        pk = table.getSourceName() + "." + resultSet.getString(relevantIndece); //$NON-NLS-1$
                                    }
                                }
                                if (!"".equals(fk) && !"".equals(pk)) { //$NON-NLS-1$ //$NON-NLS-2$
                                    String[] strs = new String[2];
                                    strs[0] = pk;
                                    strs[1] = fk;
                                    fks.add(strs);
                                    fk = ""; //$NON-NLS-1$
                                    pk = ""; //$NON-NLS-1$
                                }
                            }
                        }
                        resultSet.close();
                    }
                }
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("EMFRepositoryNodeManager.logMessage"), e); //$NON-NLS-1$
            } finally {
                MetadataConnectionUtils.closeDerbyDriver();
            }
        }
        if (!relations.isEmpty()) {
            fks.addAll(relations);
            relations.clear();
        }
        return fks;
    }

    private List<String[]> relations = new ArrayList<String[]>();

    private boolean isPrompt;

    public String initSqlStatement(String currentSql) {
        QueryTokenizer qt = new QueryTokenizer(currentSql, queryDelimiter, alternateDelimiter, commentDelimiter);
        List<String> queryStrings = new ArrayList<String>();
        while (qt.hasQuery()) {
            String querySql = qt.nextQuery();
            // ignore commented lines.
            if (!querySql.startsWith("--")) { //$NON-NLS-1$
                queryStrings.add(querySql);
            }
        }
        if (queryStrings.size() < 1) {
            // MessageDialog.openError(DisplayUtils.getDefaultShell(false),
            // Messages.getString("EMFRepositoryNodeManager.Notice.Title"),
            // Messages
            // .getString("EMFRepositoryNodeManager.Notice.info"));
            return null;
        }
        Boolean isForce = null;
        if (queryStrings.size() > 1 && isPrompt) {
            isForce = MessageDialog.openQuestion(DisplayUtils.getDefaultShell(false),
                    Messages.getString("EMFRepositoryNodeManager.Notice.Title2"), //$NON-NLS-1$
                    Messages.getString("EMFRepositoryNodeManager.Notice.info2")); //$NON-NLS-1$
        }
        String string = queryStrings.get(0).toLowerCase().replaceAll("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        string = string.replaceAll("\t", " "); //$NON-NLS-1$ //$NON-NLS-2$
        string = string.replaceAll("\r", " "); //$NON-NLS-1$ //$NON-NLS-2$
        // string = string.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
        string = string.replaceAll("\'", ""); //$NON-NLS-1$ //$NON-NLS-2$
        if (!string.startsWith("select ")) { //$NON-NLS-1$
            if (isPrompt) {
                MessageDialog.openWarning(DisplayUtils.getDefaultShell(false),
                        Messages.getString("EMFRepositoryNodeManager.Notice.title3"), Messages //$NON-NLS-1$
                        .getString("EMFRepositoryNodeManager.Notice.info3")); //$NON-NLS-1$
            }
        }
        if (isForce != null && !isForce.booleanValue()) {
            return null;
        }
        // MessageDialog
        // .openInformation(DisplayUtils.getDefaultShell(false), "Notice",
        // "GUI Sql Editor maybe not show all features of your Sql Statement!\n And your full sql Statement will show in
        // buttom of the GUI.");
        return string;
    }

    /**
     * qzhang Comment method "updateErDiagram".
     *
     * @param toSql
     * @throws Exception
     */
    public void updateErDiagram(boolean isModified, ErDiagramComposite editor, String toSql, RepositoryNode rootNode)
            throws Exception {
        if (toSql != null && !"".equals(toSql) && isModified) { //$NON-NLS-1$
            // String info = Messages.getString("MultiPageSqlBuilderEditor.Notice.InformationNotFull");
            // MessageDialog.openInformation(DisplayUtils.getDefaultShell(false),
            // Messages.getString("MultiPageSqlBuilderEditor.NoticeTitle.Text"), info);

            List<IRepositoryNode> nodeSel = parseSqlStatement(toSql, rootNode);
            if (nodeSel == null || nodeSel.isEmpty()) {
                editor.setSqlText(toSql);
            } else {
                editor.updateNodes(nodeSel, toSql);
            }
        }
        editor.setModified(false);
    }

    @SuppressWarnings("unchecked")
    public List<IRepositoryNode> parseSqlStatement(String sql, RepositoryNode currRoot) throws Exception {
        // inital the quote depence on the dbtype
        IRepositoryViewObject rObject = currRoot.getObject();
        DatabaseConnectionItem item = (DatabaseConnectionItem) rObject.getProperty().getItem();
        DatabaseConnection dbConnection = (DatabaseConnection) item.getConnection();
        String dbType = dbConnection.getDatabaseType();
        leftDbQuote = TalendTextUtils.getQuoteByDBType(dbType, true);
        rightDbQuote = TalendTextUtils.getQuoteByDBType(dbType, false);

        sql = initSqlStatement(sql);
        if (sql == null || "".equals(sql) || !sql.startsWith("select ")) { //$NON-NLS-1$ //$NON-NLS-2$
            return Collections.EMPTY_LIST;
        }
        List<String> tableNames = new ArrayList<String>();
        List<String> columnsNames = new ArrayList<String>();

        String[] cols = parseSqlToNameList(sql, tableNames, columnsNames);
        List<IRepositoryNode> nodes = new ArrayList<IRepositoryNode>();
        for (IRepositoryNode tableNode : currRoot.getChildren()) {
            for (int i = 0; i < tableNames.size(); i++) {
                String tableLabel = ""; //$NON-NLS-1$
                if (tableNode.getObject() instanceof MetadataTableRepositoryObject) {
                    MetadataTableRepositoryObject object = (MetadataTableRepositoryObject) tableNode.getObject();
                    tableLabel = object.getSourceName();
                } else {
                    tableLabel = tableNode.getObject().getLabel();
                }

                boolean isNeed = false;
                if (cols.length == 1 && cols[0].equals("*")) { //$NON-NLS-1$
                    for (String string : tableNames) {
                        if (string.equals(tableLabel.toLowerCase())) {
                            nodes.add(tableNode);
                            isNeed = true;
                        }
                    }
                }
                if (tableLabel != null) {
                    for (String string : tableNames) {
                        if (string.equals(tableLabel.toLowerCase())) {
                            isNeed = true;
                        }
                    }
                }
                if (isNeed) {
                    for (IRepositoryNode colNode : tableNode.getChildren()) {
                        String collabel = ""; //$NON-NLS-1$
                        if (colNode.getObject() instanceof MetadataColumnRepositoryObject) {
                            MetadataColumnRepositoryObject object2 = (MetadataColumnRepositoryObject) colNode.getObject();
                            collabel = object2.getSourceName();
                        } else {
                            collabel = colNode.getObject().getLabel();
                        }
                        if (collabel != null) {
                            for (String string : columnsNames) {
                                if ((string.replaceAll("\\" + leftDbQuote, "").replaceAll("\\" + rightDbQuote, "")) //$NON-NLS-1$ //$NON-NLS-2$
                                        .equals(collabel.toLowerCase())) {
                                    nodes.add(colNode);
                                }

                                // need get dbtype quotes,if the dbtype is Access,need to delete both left bracket and
                                // right bracket
                                if ((string.replaceAll("\\" + leftDbQuote, "").replaceAll("\\" + rightDbQuote, "")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        .equals(tableLabel.toLowerCase() + "." + collabel.toLowerCase())) {
                                    if (!nodes.contains(colNode)) {
                                        nodes.add(colNode);
                                    }
                                }
                                for (int j = 0; j < relations.size(); j++) {
                                    String[] pks = relations.get(j);
                                    String pk = pks[0];
                                    String fk = pks[1];
                                    boolean isSet = false;
                                    if (pk.equals(collabel.toLowerCase())) {
                                        isSet = true;
                                        pk = tableLabel.toLowerCase() + "." + collabel.toLowerCase(); //$NON-NLS-1$
                                        if (!nodes.contains(colNode)) {
                                            nodes.add(colNode);
                                        }
                                    }
                                    if (fk.equals(collabel.toLowerCase())) {
                                        isSet = true;
                                        fk = tableLabel.toLowerCase() + "." + collabel.toLowerCase(); //$NON-NLS-1$
                                        if (!nodes.contains(colNode)) {
                                            nodes.add(colNode);
                                        }
                                    }
                                    if (pk.equals(tableLabel.toLowerCase() + "." + collabel.toLowerCase())) { //$NON-NLS-1$
                                        if (!nodes.contains(colNode)) {
                                            nodes.add(colNode);
                                        }
                                    }
                                    if (fk.equals(tableLabel.toLowerCase() + "." + collabel.toLowerCase())) { //$NON-NLS-1$
                                        if (!nodes.contains(colNode)) {
                                            nodes.add(colNode);
                                        }
                                    }
                                    if (isSet) {
                                        relations.set(j, new String[] { pk, fk });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return nodes;
    }

    public String addComment(String fullSql) {
        String commentSql = ""; //$NON-NLS-1$
        String[] cs = fullSql.split("\n"); //$NON-NLS-1$
        for (String string : cs) {
            if (string.startsWith("--")) { //$NON-NLS-1$
                commentSql += string;
            } else {
                commentSql += "--" + string; //$NON-NLS-1$
            }
        }
        if (!commentSql.endsWith(queryDelimiter)) {
            commentSql += queryDelimiter;
        }
        return commentSql;
    }

    /**
     * qzhang Comment method "parseSqlToNameList".
     *
     * @param sql
     * @param tableNames
     * @param columnsNames
     */
    private String[] parseSqlToNameList(String sql, List<String> tableNames, List<String> columnsNames) throws Exception {
        String lcSql = sql.toLowerCase();
        String select = lcSql.split("select ")[1]; //$NON-NLS-1$
        String[] s = select.split(" from "); //$NON-NLS-1$
        String[] columns = s[0].split(","); //$NON-NLS-1$

        if (s.length > 1) {
            String fromStr = s[1];
            String whereStr = ""; //$NON-NLS-1$
            int indexWhere = s[1].indexOf(" where "); //$NON-NLS-1$
            if (indexWhere != -1) {
                fromStr = s[1].split(" where ")[0]; //$NON-NLS-1$
                whereStr = s[1].split(" where ")[1]; //$NON-NLS-1$
            }
            String[] tables = fromStr.split(","); //$NON-NLS-1$
            String[] rel = whereStr.split(" and "); //$NON-NLS-1$

            for (String string : columns) {
                int dotIndex = string.indexOf("."); //$NON-NLS-1$
                if (dotIndex != -1) {
                    tableNames.add(string.substring(0, dotIndex).trim().replaceAll("\\" + leftDbQuote, "") //$NON-NLS-1$ //$NON-NLS-2$
                            .replaceAll("\\" + rightDbQuote, ""));
                }
                columnsNames.add(string.trim());
            }
            for (String string : tables) {
                String tableName = string;
                if (string.contains(".")) { //$NON-NLS-1$
                    tableName = string.substring(string.indexOf(".") + 1).replaceAll("\\" + leftDbQuote, "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            .replaceAll("\\" + rightDbQuote, "");
                }
                if (!tableNames.contains(tableName.trim())) {
                    tableNames.add(tableName);
                }
            }
            for (String element : rel) {
                String[] strs = element.split("="); //$NON-NLS-1$
                if (strs.length == 2) {
                    strs[0] = strs[0].trim();
                    strs[1] = strs[1].trim();
                    relations.add(strs);
                }
            }

            // add column's name of relations to column names.
            for (String[] string : relations) {
                if (!columnsNames.contains(string[0])) {
                    columnsNames.add(string[0]);
                }
                if (!columnsNames.contains(string[1])) {
                    columnsNames.add(string[1]);
                }
            }
            // fixed table Names when contains alias.
            fixedNamesContainAlias(tableNames, TABLE_ALIAS_PREFIX);

            // fixed column Names when contains alias.
            fixedNamesContainAlias(columnsNames, COLUMN_ALIAS_PREFIX);

            // replace table's alias with table real name in columnsNames.
            for (int i = 0; i < columnsNames.size(); i++) {
                String string = columnsNames.get(i);
                if (string.contains(".")) { //$NON-NLS-1$
                    columnsNames.set(i, string.substring(string.indexOf(".") + 1)); //$NON-NLS-1$

                }
            }
            // fixed relations ,when contans alias.
            for (int i = 0; i < relations.size(); i++) {
                String[] strs = relations.get(i);
                String s0 = strs[0];
                String s1 = strs[1];
                for (String string : tableNames) {
                    int di = s0.indexOf("."); //$NON-NLS-1$
                    if (di > -1 && string.startsWith(TABLE_ALIAS_PREFIX) && string.endsWith(s0.substring(0, di))) {
                        s0 = string.substring(TABLE_ALIAS_PREFIX.length(), string.indexOf("=")) + s0.substring(di); //$NON-NLS-1$
                    }
                    di = s1.indexOf("."); //$NON-NLS-1$
                    if (di > -1 && string.startsWith(TABLE_ALIAS_PREFIX) && string.endsWith(s1.substring(0, di))) {
                        s1 = string.substring(TABLE_ALIAS_PREFIX.length(), string.indexOf("=")) + s1.substring(di); //$NON-NLS-1$
                    }
                }
                relations.set(i, new String[] { s0, s1 });
            }
        }
        return columns;
    }

    /**
     * qzhang Comment method "fixedNamesContainAlias".
     *
     * @param tableNames
     */
    private void fixedNamesContainAlias(List<String> tableNames, String prefix) throws Exception {
        int originalSize = tableNames.size(); // hywang add for 0009970 to limit the list's size
        for (int i = 0; i < originalSize; i++) {
            String name = tableNames.get(i);
            String[] aliasNames = regExMatch(name);
            List<String> tableContainAlias = new ArrayList<String>();
            String aliasName = ""; //$NON-NLS-1$
            String realName = ""; //$NON-NLS-1$
            for (String aliasName2 : aliasNames) {
                String string = aliasName2;
                if (!string.equals("")) { //$NON-NLS-1$
                    tableContainAlias.add(string);
                }
            }

            if (tableContainAlias.size() == 3) { // such as "column as Alias"
                realName = tableContainAlias.get(0);
                aliasName = tableContainAlias.get(2);
                if (!tableNames.contains(aliasName)) {
                    tableNames.add(prefix + realName + "=" + aliasName); //$NON-NLS-1$
                } else {
                    tableNames.set(tableNames.indexOf(aliasName), prefix + realName + "=" + aliasName); //$NON-NLS-1$
                }
                tableNames.set(i, realName);
            }
            // else if (tableContainAlias.size() == 2) { // such as "column Alias"
            // realName = tableContainAlias.get(0);
            // aliasName = tableContainAlias.get(1);
            // if (!tableNames.contains(aliasName)) {
            // tableNames.add(prefix + realName + "=" + aliasName); //$NON-NLS-1$
            // } else {
            // tableNames.set(tableNames.indexOf(aliasName), prefix + realName + "=" + aliasName); //$NON-NLS-1$
            // }
            // tableNames.set(i, realName);
            // }

            else if (tableContainAlias.size() == 1) {
                realName = tableContainAlias.get(0);
                tableNames.set(i, name.trim());
            }
        }
    }

    /**
     * Getter for root.
     *
     * @return the root
     */
    public RepositoryNode getRoot() {
        return this.root;
    }

    public static EMFRepositoryNodeManager getInstance() {
        return instance;
    }

    public boolean isPrompt() {
        return this.isPrompt;
    }

    public void setPrompt(boolean isPrompt) {
        this.isPrompt = isPrompt;
    }

    // method regExMatch added by hyWang
    private String[] regExMatch(String input) {
        // try to split the column by a regEx
        final String regEx = ".+"; //$NON-NLS-1$
        Pattern p = Pattern.compile(checkRegexp(leftDbQuote + regEx + rightDbQuote));
        Matcher matcher = p.matcher(input);
        String tempStr = null;
        List<String> splits = new ArrayList<String>();
        if (matcher.find()) {
            String group = matcher.group();
            int endIndex = input.indexOf(group) + group.length();
            splits.add(input.substring(0, endIndex));
            // PTODO
            String end = input.substring(endIndex); //
            end = end.trim();
            tempStr = end;
        } else {
            tempStr = input;
        }
        String[] split = tempStr.split(" "); //$NON-NLS-1$
        for (String s : split) {
            if (!"".equals(s)) { //$NON-NLS-1$
                splits.add(s);
            }
        }
        return splits.toArray(new String[0]);
    }

    private String checkRegexp(String regexp) {
        regexp = regexp.replace("[", "\\["); //$NON-NLS-1$ //$NON-NLS-2$
        regexp = regexp.replace("]", "\\]"); //$NON-NLS-1$ //$NON-NLS-2$
        regexp = regexp.replace("(", "\\("); //$NON-NLS-1$ //$NON-NLS-2$
        regexp = regexp.replace(")", "\\)"); //$NON-NLS-1$ //$NON-NLS-2$
        return regexp;
    }
}
