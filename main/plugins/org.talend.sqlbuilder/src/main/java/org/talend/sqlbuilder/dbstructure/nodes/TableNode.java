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
package org.talend.sqlbuilder.dbstructure.nodes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;
import net.sourceforge.squirrel_sql.fw.sql.TableColumnInfo;

import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.impl.MetadataColumnImpl;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataFromDataBase;
import org.talend.cwm.relational.TdColumn;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.MetadataTableRepositoryObject;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: TableNode.java,v 1.20 2006/11/09 01:21:53 qiang.zhang Exp $
 *
 */
public class TableNode extends AbstractNode {

    private static final int PRIMARY_KEY_NAME_INDEX = 4;

    private static final int FOREIGN_KEY_NAME_INDEX = 4;

    private static final int COLUMN_NAME_INDEX = 4;

    private List pcolumnNames;

    private List pforeignKeyNames;

    private List pprimaryKeyNames;

    private ITableInfo ptableInfo;

    private List pfolderNames = new ArrayList();

    private RepositoryNode currentRepositoryNode;

    private boolean isFromRepository;

    private static DatabaseConnection connection;

    private String sourceName;

    /**
     * Create new database table node.
     *
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public TableNode(INode parent, String name, SessionTreeNode sessionNode, ITableInfo tableInfo) {

        ptableInfo = tableInfo;
        psessionNode = sessionNode;
        pparent = parent;
        pname = name;
        pimageKey = "Images.TableNodeIcon"; //$NON-NLS-1$
    }

    /**
     * @return the source name (table name in db)
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * @param sourceName sourceName.
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * @return isFromRepository.
     */
    public boolean isFromRepository() {
        return isFromRepository;
    }

    /**
     * Set isFromRepository.
     *
     * @param isFromRepository2 isFromRepository.
     */
    public void setFromRepository(boolean isFromRepository2) {
        this.isFromRepository = isFromRepository2;
    }

    /**
     * @return RepositoryNode.
     */
    public RepositoryNode getCurrentRepositoryNode() {
        return currentRepositoryNode;
    }

    /**
     * @param currentRepositoryNode CurrentRepositoryNode.
     */
    public void setCurrentRepositoryNode(RepositoryNode currentRepositoryNode) {
        this.currentRepositoryNode = currentRepositoryNode;
    }

    /**
     * @return List of column names for this table.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public List getColumnNames() {

        if (pcolumnNames == null) {

            pcolumnNames = new ArrayList();
            try {
                ResultSet resultSet = psessionNode.getMetaData().getColumns(ptableInfo);
                while (resultSet.next()) {
                    pcolumnNames.add(resultSet.getString(COLUMN_NAME_INDEX));
                }

            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("TableNode.logMessage1"), e); //$NON-NLS-1$
            }

        }

        return pcolumnNames;
    }

    /**
     * @return List of column names for this table.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public List getForeignKeyNames() {

        if (pforeignKeyNames == null) {

            pforeignKeyNames = new ArrayList();
            try {
                ResultSet resultSet = psessionNode.getMetaData().getImportedKeys(ptableInfo);
                while (resultSet.next()) {
                    pforeignKeyNames.add(resultSet.getString(FOREIGN_KEY_NAME_INDEX));
                }

            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("TableNode.logMessage2"), e); //$NON-NLS-1$
            }

        }

        return pforeignKeyNames;
    }

    /**
     * @return List of column names for this table.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public List getPrimaryKeyNames() {

        if (pprimaryKeyNames == null) {

            pprimaryKeyNames = new ArrayList();
            if (psessionNode.getInteractiveConnection() == null || ptableInfo == null) {
                return pprimaryKeyNames;
            }

            try {
                ResultSet resultSet = psessionNode.getMetaData().getPrimaryKeys(ptableInfo);
                while (resultSet.next()) {
                    pprimaryKeyNames.add(resultSet.getString(PRIMARY_KEY_NAME_INDEX));
                }

            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("TableNode.logMessage3"), e); //$NON-NLS-1$
            }

        }

        return pprimaryKeyNames;
    }

    /**
     * @return Qualified table name
     */
    public String getQualifiedName() {

        if (ptableInfo == null) {
            return null;
        }

        return ptableInfo.getQualifiedName();
    }

    /**
     * @return // TODO fix this for sql completion?
     */
    public String getTableDesc() {

        return getTableInfo().getQualifiedName();
    }

    /**
     * @return TableInfo for this node
     */
    public ITableInfo getTableInfo() {

        return ptableInfo;
    }

    /**
     * Returns the table info type as the type for this node.
     *
     * @return Type.
     * @see org.talend.sqlbuilder.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return ptableInfo.getType();
    }

    /**
     * @return UniqueIdentifier.
     */
    public String getUniqueIdentifier() {

        return getQualifiedName();
    }

    /**
     * @return isEndNode.
     */
    public boolean isEndNode() {

        return false;
    }

    /**
     * @return true if this node is a synonym
     */
    public boolean isSynonym() {

        return ptableInfo.getType().equalsIgnoreCase("SYNONYM"); //$NON-NLS-1$
    }

    /**
     * @return true if this node is a table
     */
    public boolean isTable() {

        return ptableInfo.getType().equalsIgnoreCase("TABLE"); //$NON-NLS-1$
    }

    /**
     * @return true if this node is a view
     */
    public boolean isView() {

        return ptableInfo.getType().equalsIgnoreCase("VIEW"); //$NON-NLS-1$
    }

    /**
     *
     *
     * @see org.talend.sqlbuilder.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        if (ptableInfo == null) {
            return;
        }

        try {
            // add column and index nodes if they don't exist yet.

            ColumnFolderNode colNode = new ColumnFolderNode(this, ptableInfo);
            if (!pfolderNames.contains(colNode.getName())) {
                addChildNode(colNode);
            }

            IndexFolderNode indexNode = new IndexFolderNode(this, ptableInfo);
            if (!pfolderNames.contains(indexNode.getName())) {
                addChildNode(indexNode);
            }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("TableNode.logMessage4") + getName(), e); //$NON-NLS-1$
        }

    }

    /**
     * @return Label text.
     */
    @Override
    public String getLabelText() {
        String labelText = super.getLabelText();
        if (labelText == null) {
            return null;
        }
        return labelText;
    }

    @Override
    public String getLabelAtColumn(int columnIndex) {
        if (columnIndex == 0) {
            // if (ptableInfo.getSchemaName() != null) {
            // return ptableInfo.getSchemaName() + "." + getLabelText();
            // } else {
            // return getLabelText();
            // }
            return getLabelText();
        } else if (columnIndex == 1) {
            return getRepositoryName();
        }
        return null;
    }

    /**
     * @return ChildNodes.
     */
    @Override
    public INode[] getChildNodes() {
        INode[] tempNodes = super.getChildNodes();
        INode[] nodesInDB = null;
        for (INode node : tempNodes) {
            if (node instanceof ColumnFolderNode) {
                nodesInDB = node.getChildNodes();
            }
        }

        if (currentRepositoryNode == null) {
            return nodesInDB;
        }

        Map<String, INode> allNodes = new HashMap<String, INode>();

        // add db nodes.
        if (nodesInDB != null) {
            for (INode node : nodesInDB) {
                allNodes.put(node.getLabelText(), node);
            }
        }

        EList columns = TableNode.getColumns(currentRepositoryNode);

        for (int i = 0, size = columns.size(); i < size; i++) {
            MetadataColumnImpl column = (MetadataColumnImpl) columns.get(i);
            if (!allNodes.keySet().contains(column.getLabel()) && !allNodes.keySet().contains(column.getOriginalField())) {
                allNodes.put(column.getLabel(), convert2ColumnNode(this, column));
            } else {
                ColumnNode cNode = (ColumnNode) allNodes.get(column.getOriginalField());
                cNode.setRepositoryName(column.getLabel());
                cNode.setSameToColumn(isNodeSameToColumn(cNode, column));
                cNode.setSourceName(column.getOriginalField());
            }
        }

        return allNodes.values().toArray(new INode[] {});
    }

    /**
     * Check if ColumnNode and Column are the same.
     *
     * @param node ColumnNode
     * @param column MetadataColumnImpl
     * @return isNodeSameToColumn
     * @exception
     */
    private boolean isNodeSameToColumn(ColumnNode node, MetadataColumnImpl column) {
        SessionTreeNode sessionTreeNode = node.getSession();

        TableColumnInfo[] tableColumnInfo = null;
        try {
            tableColumnInfo = sessionTreeNode.getMetaData().getColumnInfo(ptableInfo);
        } catch (SQLException e) {
            SqlBuilderPlugin.log(e.getMessage(), e);
            return false;
        }
        // Retrive metadataColumns from Database
        IMetadataConnection iMetadataConnection = ConvertionHelper.convert(connection);
        List<TdColumn> metadataColumns = new ArrayList<TdColumn>();
        metadataColumns = ExtractMetaDataFromDataBase.returnMetadataColumnsFormTable(iMetadataConnection, getLabelText());
        Iterator iterate = metadataColumns.iterator();

        while (iterate.hasNext()) {
            MetadataColumn metadataColumn = (MetadataColumn) iterate.next();
            for (TableColumnInfo info : tableColumnInfo) {
                if (metadataColumn.getLabel().equals(node.getLabelText()) && info.getColumnName().equals(node.getLabelText())) {
                    boolean divergency = isEquivalent(metadataColumn, column);
                    metadataColumn.setDivergency(divergency);
                    return divergency;
                }
            }
        }

        return true;
    }

    /**
     * Check if TableColumnInfo and MetadataColumnImpl are the same..
     *
     * @param info MetadataColumn
     * @param column MetadataColumnImpl
     * @return isEquivalent.
     * @exception
     */
    private boolean isEquivalent(MetadataColumn info, MetadataColumnImpl column) {
        // if (!info.getLabel().equals(column.getLabel())) {
        // return false;
        // }
        if (info.getLength() != column.getLength()) {
            return false;
        }
        if (info.getDefaultValue() != null && !info.getDefaultValue().equals(column.getDefaultValue())) {
            return false;
        }
        if (column.getDefaultValue() != null && column.getDefaultValue().length() != 0
                && !column.getDefaultValue().equals(info.getDefaultValue())) {
            return false;
        }
        if (info.isNullable() != column.isNullable()) {
            return false;
        }
        if (info.isKey() != column.isKey()) {
            return false;
        }
        if (info.getPrecision() != column.getPrecision()) {
            return false;
        }
        if (info.getSourceType() != null && !info.getSourceType().equals(column.getSourceType())) {
            return false;
        }
        if (info.getComment() != null && info.getComment().length() != 0 && !info.getComment().equals(column.getComment())) {
            return false;
        }
        if (column.getComment() != null && column.getComment().length() != 0 && !column.getComment().equals(info.getComment())) {
            return false;
        }

        if (info.getTalendType() != null && !info.getTalendType().equals(column.getTalendType())) {
            return false;
        }
        if (column.getTalendType() != null && !column.getTalendType().equals(info.getTalendType())) {
            return false;
        }

        return true;
    }

    /**
     * @param tableNode TableNode.
     * @param column MetadataColumnImpl
     * @return INode.
     * @exception
     */
    public static INode convert2ColumnNode(TableNode tableNode, MetadataColumnImpl column) {
        ColumnNode columnNode = null;
        try {
            columnNode = new ColumnNode(tableNode, "", tableNode.psessionNode, tableNode, true); //$NON-NLS-1$
            columnNode.setFromRepository(true);
        } catch (Exception e) {
            SqlBuilderPlugin.log(e.getMessage(), e);
        }
        columnNode.setRepositoryName(column.getLabel());

        return columnNode;
    }

    /**
     * Get repository columns from table repositoryNode.
     *
     * @param repositoryNode -- table repositoryNode
     * @return columns
     * @exception
     */
    @SuppressWarnings("deprecation")//$NON-NLS-1$
    public static EList getColumns(RepositoryNode repositoryNode) {
        return getMetadataTable(repositoryNode).getColumns();
    }

    /**
     * Get repository table from table repositoryNode.
     *
     * @param repositoryNode -- table repositoryNode
     * @return columns
     * @exception
     */
    @SuppressWarnings("deprecation")//$NON-NLS-1$
    public static MetadataTable getMetadataTable(RepositoryNode repositoryNode) {
        RepositoryNodeType type = SQLBuilderRepositoryNodeManager.getRepositoryType(repositoryNode);
        if (type != RepositoryNodeType.TABLE) {
            throw new RuntimeException(Messages.getString("TableNode.exceptionMessage")); //$NON-NLS-1$
        }
        MetadataTableRepositoryObject tableObject = (MetadataTableRepositoryObject) repositoryNode.getObject();
        return tableObject.getTable();
    }

    /**
     * @return Background.
     */
    @Override
    public Color getBackground() {
        if (getLabelText().equals(getSourceName())) {
            return super.getBackground();
        } else if (getLabelAtColumn(1) == null || getLabelAtColumn(1).trim().equals("")) { //$NON-NLS-1$
            return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
        } else {
            return Display.getDefault().getSystemColor(SWT.COLOR_RED);
        }
    }

    /**
     * @return Foreground.
     */
    @Override
    public Color getForeground() {
        if (getLabelText().equals(getSourceName())) {
            return super.getBackground();
        } else {
            return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
        }
    }

}
