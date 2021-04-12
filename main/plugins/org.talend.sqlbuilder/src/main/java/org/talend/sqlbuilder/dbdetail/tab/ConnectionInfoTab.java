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
package org.talend.sqlbuilder.dbdetail.tab;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dataset.dataset.DataSet;
import org.talend.sqlbuilder.dbstructure.nodes.INode;

/**
 * @author Davy Vanherbergen
 *
 */
public class ConnectionInfoTab extends AbstractDataSetTab {

    @Override
    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.ConnectionInfo"); //$NON-NLS-1$
    }

    @Override
    public String getStatusMessage() {
        return Messages.getString(
                "DatabaseDetailView.Tab.ConnectionInfo.status", new String[] { getNode().getSession().toString() }); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.dbdetail.tab.AbstractDataSetTab#getDataSet()
     */
    @Override
    public DataSet getDataSet() throws Exception {

        INode node = getNode();

        if (node == null) {
            return null;
        }
        if (node.getSession() == null) {
            return null;
        }
        SQLDatabaseMetaData sqlMetaData = node.getSession().getMetaData();
        if (sqlMetaData == null) {
            return null;
        }
        DatabaseMetaData jdbcMetaData = sqlMetaData.getJDBCMetaData();

        String[] header = new String[2];
        header[0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.Property"); //$NON-NLS-1$
        header[1] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.Value"); //$NON-NLS-1$

        String[][] data = new String[124][2];

        setDataTo20(node, sqlMetaData, jdbcMetaData, data);
        setDataTo40(jdbcMetaData, data);
        setDataTo60(jdbcMetaData, data);
        setDataTo80(jdbcMetaData, data);
        setDataTo100(jdbcMetaData, data);
        setDataTo120(jdbcMetaData, data);
        int[] types = new int[2];
        types[0] = DataSet.TYPE_STRING;
        types[1] = DataSet.TYPE_STRING;
        DataSet dataSet = new DataSet(header, data, types);
        return dataSet;
    }

    /**
     * DOC set the data(100-120) value .
     *
     * @param jdbcMetaData
     * @param data
     */
    private void setDataTo120(DatabaseMetaData jdbcMetaData, String[][] data) {
        data[101][0] = Messages.getString("ConnectionInfoTab.property1"); //$NON-NLS-1$
        try {
            data[101][1] = "" + jdbcMetaData.getMaxStatementLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage1"), e); //$NON-NLS-1$
        }
        data[102][0] = Messages.getString("ConnectionInfoTab.property2"); //$NON-NLS-1$
        try {
            data[102][1] = "" + jdbcMetaData.getMaxStatements(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage2"), e); //$NON-NLS-1$
        }
        data[103][0] = Messages.getString("ConnectionInfoTab.property3"); //$NON-NLS-1$
        try {
            data[103][1] = "" + jdbcMetaData.getMaxTableNameLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage3"), e); //$NON-NLS-1$
        }
        data[104][0] = Messages.getString("ConnectionInfoTab.property4"); //$NON-NLS-1$
        try {
            data[104][1] = "" + jdbcMetaData.getMaxTablesInSelect(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage4"), e); //$NON-NLS-1$
        }
        data[105][0] = Messages.getString("ConnectionInfoTab.property5"); //$NON-NLS-1$
        try {
            data[105][1] = "" + jdbcMetaData.getMaxUserNameLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage5"), e); //$NON-NLS-1$
        }

        data[106][0] = Messages.getString("ConnectionInfoTab.property6"); //$NON-NLS-1$
        setData106(jdbcMetaData, data);

        data[107][0] = Messages.getString("ConnectionInfoTab.property7"); //$NON-NLS-1$
        try {
            data[107][1] = "" + jdbcMetaData.supportsTransactions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage6"), e); //$NON-NLS-1$
        }
        data[108][0] = Messages.getString("ConnectionInfoTab.property8"); //$NON-NLS-1$
        try {
            data[108][1] = "" + jdbcMetaData.supportsDataDefinitionAndDataManipulationTransactions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage7"), e); //$NON-NLS-1$
        }
        data[109][0] = Messages.getString("ConnectionInfoTab.property9"); //$NON-NLS-1$
        try {
            data[109][1] = "" + jdbcMetaData.supportsDataManipulationTransactionsOnly(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage8"), e); //$NON-NLS-1$
        }
        data[110][0] = Messages.getString("ConnectionInfoTab.property10"); //$NON-NLS-1$
        try {
            data[110][1] = "" + jdbcMetaData.dataDefinitionCausesTransactionCommit(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage9"), e); //$NON-NLS-1$
        }
        data[111][0] = Messages.getString("ConnectionInfoTab.property11"); //$NON-NLS-1$
        try {
            data[111][1] = "" + jdbcMetaData.dataDefinitionIgnoredInTransactions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage10"), e); //$NON-NLS-1$
        }
        data[112][0] = Messages.getString("ConnectionInfoTab.property12"); //$NON-NLS-1$
        try {
            data[112][1] = "" + jdbcMetaData.supportsBatchUpdates(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage11"), e); //$NON-NLS-1$
        }
        data[113][0] = Messages.getString("ConnectionInfoTab.property13"); //$NON-NLS-1$
        try {
            data[113][1] = "" + jdbcMetaData.supportsSavepoints(); //$NON-NLS-1$
        } catch (SQLException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage12"), e); //$NON-NLS-1$
        }
        data[114][0] = Messages.getString("ConnectionInfoTab.property14"); //$NON-NLS-1$
        try {
            data[114][1] = "" + jdbcMetaData.supportsNamedParameters(); //$NON-NLS-1$
        } catch (SQLException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage13"), e); //$NON-NLS-1$
        }
        data[115][0] = Messages.getString("ConnectionInfoTab.property15"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[115][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[115][1] = "" + jdbcMetaData.supportsGetGeneratedKeys(); //$NON-NLS-1$
            }
        } catch (SQLException unimplementedOperationException) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage14"), e); //$NON-NLS-1$
        }
        data[116][0] = Messages.getString("ConnectionInfoTab.property16"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[116][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[116][1] = "" + jdbcMetaData.getDatabaseMajorVersion(); //$NON-NLS-1$
            }
        } catch (UnsupportedOperationException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage15"), e); //$NON-NLS-1$
        }
        data[117][0] = Messages.getString("ConnectionInfoTab.property17"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[117][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[117][1] = "" + jdbcMetaData.getDatabaseMinorVersion(); //$NON-NLS-1$
            }
        } catch (UnsupportedOperationException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage16"), e); //$NON-NLS-1$
        }
        data[118][0] = Messages.getString("ConnectionInfoTab.property18"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[118][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[118][1] = "" + jdbcMetaData.getJDBCMinorVersion(); //$NON-NLS-1$
            }
        } catch (SQLException operationException) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage17"), e); //$NON-NLS-1$
        }
        data[119][0] = Messages.getString("ConnectionInfoTab.property19"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[119][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[119][1] = "" + jdbcMetaData.getJDBCMajorVersion(); //$NON-NLS-1$
            }
        } catch (SQLException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage18"), e); //$NON-NLS-1$
        }
        data[120][0] = Messages.getString("ConnectionInfoTab.property20"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[120][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[120][1] = "" + jdbcMetaData.getSQLStateType(); //$NON-NLS-1$
            }
        } catch (SQLException unimplementedOperationException) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage19"), e); //$NON-NLS-1$
        }
        data[121][0] = Messages.getString("ConnectionInfoTab.property21"); //$NON-NLS-1$
        try {
            data[121][1] = "" + jdbcMetaData.locatorsUpdateCopy(); //$NON-NLS-1$
        } catch (UnsupportedOperationException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage20"), e); //$NON-NLS-1$
        }
        data[122][0] = Messages.getString("ConnectionInfoTab.property22"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[122][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[122][1] = "" + jdbcMetaData.supportsStatementPooling(); //$NON-NLS-1$
            }
        } catch (UnsupportedOperationException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage21"), e); //$NON-NLS-1$
        }
        data[123][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.ReadOnly"); //$NON-NLS-1$
        try {
            data[123][1] = "" + jdbcMetaData.isReadOnly(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage22"), e); //$NON-NLS-1$
        }
    }

    /**
     * DOC set the data(106) value ..
     *
     * @param jdbcMetaData
     * @param data
     */
    private void setData106(DatabaseMetaData jdbcMetaData, String[][] data) {
        try {
            int isol = jdbcMetaData.getDefaultTransactionIsolation();
            String is = null;
            switch (isol) {
            case java.sql.Connection.TRANSACTION_NONE:
                is = "TRANSACTION_NONE"; //$NON-NLS-1$
                break;

            case java.sql.Connection.TRANSACTION_READ_COMMITTED:
                is = "TRANSACTION_READ_COMMITTED"; //$NON-NLS-1$
                break;

            case java.sql.Connection.TRANSACTION_READ_UNCOMMITTED:
                is = "TRANSACTION_READ_UNCOMMITTED"; //$NON-NLS-1$
                break;

            case java.sql.Connection.TRANSACTION_REPEATABLE_READ:
                is = "TRANSACTION_REPEATABLE_READ"; //$NON-NLS-1$
                break;

            case java.sql.Connection.TRANSACTION_SERIALIZABLE:
                is = "TRANSACTION_SERIALIZABLE"; //$NON-NLS-1$
                break;

            default:
                is = ""; //$NON-NLS-1$
                break;
            }

            data[106][1] = is;
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.78"), e); //$NON-NLS-1$
        }
    }

    /**
     * DOC set the data(80-100) value .
     *
     * @param jdbcMetaData
     * @param data
     */
    private void setDataTo100(DatabaseMetaData jdbcMetaData, String[][] data) {
        data[81][0] = Messages.getString("ConnectionInfoTab.property23"); //$NON-NLS-1$
        try {
            data[81][1] = "" + jdbcMetaData.supportsOpenCursorsAcrossCommit(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage23"), e); //$NON-NLS-1$
        }
        data[82][0] = Messages.getString("ConnectionInfoTab.property24"); //$NON-NLS-1$
        try {
            data[82][1] = "" + jdbcMetaData.supportsOpenCursorsAcrossRollback(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage24"), e); //$NON-NLS-1$
        }
        data[83][0] = Messages.getString("ConnectionInfoTab.property25"); //$NON-NLS-1$
        try {
            data[83][1] = "" + jdbcMetaData.supportsOpenStatementsAcrossCommit(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.LogMessage25"), e); //$NON-NLS-1$
        }
        data[84][0] = Messages.getString("ConnectionInfoTab.property26"); //$NON-NLS-1$
        try {
            data[84][1] = "" + jdbcMetaData.supportsOpenStatementsAcrossRollback(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage26"), e); //$NON-NLS-1$
        }
        data[85][0] = Messages.getString("ConnectionInfoTab.property27"); //$NON-NLS-1$
        try {
            data[85][1] = "" + jdbcMetaData.getMaxBinaryLiteralLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage27"), e); //$NON-NLS-1$
        }
        data[86][0] = Messages.getString("ConnectionInfoTab.property28"); //$NON-NLS-1$
        try {
            data[86][1] = "" + jdbcMetaData.getMaxCharLiteralLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage28"), e); //$NON-NLS-1$
        }
        data[87][0] = Messages.getString("ConnectionInfoTab.property29"); //$NON-NLS-1$
        try {
            data[87][1] = "" + jdbcMetaData.getMaxColumnNameLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage29"), e); //$NON-NLS-1$
        }
        data[88][0] = Messages.getString("ConnectionInfoTab.property30"); //$NON-NLS-1$
        try {
            data[88][1] = "" + jdbcMetaData.getMaxColumnsInGroupBy(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage30"), e); //$NON-NLS-1$
        }
        data[89][0] = Messages.getString("ConnectionInfoTab.property31"); //$NON-NLS-1$
        try {
            data[89][1] = "" + jdbcMetaData.getMaxColumnsInIndex(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage31"), e); //$NON-NLS-1$
        }
        data[90][0] = Messages.getString("ConnectionInfoTab.property32"); //$NON-NLS-1$
        try {
            data[90][1] = "" + jdbcMetaData.getMaxColumnsInOrderBy(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage32"), e); //$NON-NLS-1$
        }
        data[91][0] = Messages.getString("ConnectionInfoTab.property33"); //$NON-NLS-1$
        try {
            data[91][1] = "" + jdbcMetaData.getMaxColumnsInSelect(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage33"), e); //$NON-NLS-1$
        }
        data[92][0] = Messages.getString("ConnectionInfoTab.property34"); //$NON-NLS-1$
        try {
            data[92][1] = "" + jdbcMetaData.getMaxColumnsInTable(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage34"), e); //$NON-NLS-1$
        }
        data[93][0] = Messages.getString("ConnectionInfoTab.property35"); //$NON-NLS-1$
        try {
            data[93][1] = "" + jdbcMetaData.getMaxConnections(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage35"), e); //$NON-NLS-1$
        }
        data[94][0] = Messages.getString("ConnectionInfoTab.property36"); //$NON-NLS-1$
        try {
            data[94][1] = "" + jdbcMetaData.getMaxCursorNameLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage36"), e); //$NON-NLS-1$
        }
        data[95][0] = Messages.getString("ConnectionInfoTab.property37"); //$NON-NLS-1$
        try {
            data[95][1] = "" + jdbcMetaData.getMaxIndexLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage37"), e); //$NON-NLS-1$
        }
        data[96][0] = Messages.getString("ConnectionInfoTab.property38"); //$NON-NLS-1$
        try {
            data[96][1] = "" + jdbcMetaData.getMaxSchemaNameLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage38"), e); //$NON-NLS-1$
        }
        data[97][0] = Messages.getString("ConnectionInfoTab.property39"); //$NON-NLS-1$
        try {
            data[97][1] = "" + jdbcMetaData.getMaxProcedureNameLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage39"), e); //$NON-NLS-1$
        }
        data[98][0] = Messages.getString("ConnectionInfoTab.property40"); //$NON-NLS-1$
        try {
            data[98][1] = "" + jdbcMetaData.getMaxCatalogNameLength(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage40"), e); //$NON-NLS-1$
        }
        data[99][0] = Messages.getString("ConnectionInfoTab.property41"); //$NON-NLS-1$
        try {
            data[99][1] = "" + jdbcMetaData.getMaxRowSize(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage41"), e); //$NON-NLS-1$
        }
        data[100][0] = Messages.getString("ConnectionInfoTab.property42"); //$NON-NLS-1$
        try {
            data[100][1] = "" + jdbcMetaData.doesMaxRowSizeIncludeBlobs(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage42"), e); //$NON-NLS-1$
        }
    }

    /**
     * DOC set the data(60-80) value ..
     *
     * @param jdbcMetaData
     * @param data
     */
    private void setDataTo80(DatabaseMetaData jdbcMetaData, String[][] data) {
        data[61][0] = Messages.getString("ConnectionInfoTab.property43"); //$NON-NLS-1$
        try {
            data[61][1] = "" + jdbcMetaData.supportsSchemasInDataManipulation(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage43"), e); //$NON-NLS-1$
        }
        data[62][0] = Messages.getString("ConnectionInfoTab.property44"); //$NON-NLS-1$
        try {
            data[62][1] = "" + jdbcMetaData.supportsSchemasInProcedureCalls(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage44"), e); //$NON-NLS-1$
        }
        data[63][0] = Messages.getString("ConnectionInfoTab.property45"); //$NON-NLS-1$
        try {
            data[63][1] = "" + jdbcMetaData.supportsSchemasInTableDefinitions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage45"), e); //$NON-NLS-1$
        }
        data[64][0] = Messages.getString("ConnectionInfoTab.property46"); //$NON-NLS-1$
        try {
            data[64][1] = "" + jdbcMetaData.supportsSchemasInIndexDefinitions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage46"), e); //$NON-NLS-1$
        }
        data[65][0] = Messages.getString("ConnectionInfoTab.property47"); //$NON-NLS-1$
        try {
            data[65][1] = "" + jdbcMetaData.supportsSchemasInPrivilegeDefinitions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage47"), e); //$NON-NLS-1$
        }
        data[66][0] = Messages.getString("ConnectionInfoTab.property48"); //$NON-NLS-1$
        try {
            data[66][1] = "" + jdbcMetaData.supportsCatalogsInDataManipulation(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage48"), e); //$NON-NLS-1$
        }
        data[67][0] = Messages.getString("ConnectionInfoTab.property49"); //$NON-NLS-1$
        try {
            data[67][1] = "" + jdbcMetaData.supportsCatalogsInProcedureCalls(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage49"), e); //$NON-NLS-1$
        }
        data[68][0] = Messages.getString("ConnectionInfoTab.property50"); //$NON-NLS-1$
        try {
            data[68][1] = "" + jdbcMetaData.supportsCatalogsInTableDefinitions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage50"), e); //$NON-NLS-1$
        }
        data[69][0] = Messages.getString("ConnectionInfoTab.property51"); //$NON-NLS-1$
        try {
            data[69][1] = "" + jdbcMetaData.supportsCatalogsInIndexDefinitions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage51"), e); //$NON-NLS-1$
        }
        data[70][0] = Messages.getString("ConnectionInfoTab.property52"); //$NON-NLS-1$
        try {
            data[70][1] = "" + jdbcMetaData.supportsCatalogsInPrivilegeDefinitions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage52"), e); //$NON-NLS-1$
        }
        data[71][0] = Messages.getString("ConnectionInfoTab.property53"); //$NON-NLS-1$
        try {
            data[71][1] = "" + jdbcMetaData.supportsPositionedDelete(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage53"), e); //$NON-NLS-1$
        }
        data[72][0] = Messages.getString("ConnectionInfoTab.property54"); //$NON-NLS-1$
        try {
            data[72][1] = "" + jdbcMetaData.supportsPositionedUpdate(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage54"), e); //$NON-NLS-1$
        }
        data[73][0] = Messages.getString("ConnectionInfoTab.property55"); //$NON-NLS-1$
        try {
            data[73][1] = "" + jdbcMetaData.supportsStoredProcedures(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage55"), e); //$NON-NLS-1$
        }
        data[74][0] = Messages.getString("ConnectionInfoTab.property56"); //$NON-NLS-1$
        try {
            data[74][1] = "" + jdbcMetaData.supportsSubqueriesInComparisons(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage56"), e); //$NON-NLS-1$
        }
        data[75][0] = Messages.getString("ConnectionInfoTab.property57"); //$NON-NLS-1$
        try {
            data[75][1] = "" + jdbcMetaData.supportsSubqueriesInExists(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage57"), e); //$NON-NLS-1$
        }
        data[76][0] = Messages.getString("ConnectionInfoTab.property58"); //$NON-NLS-1$
        try {
            data[76][1] = "" + jdbcMetaData.supportsSubqueriesInIns(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage58"), e); //$NON-NLS-1$
        }
        data[77][0] = Messages.getString("ConnectionInfoTab.property59"); //$NON-NLS-1$
        try {
            data[77][1] = "" + jdbcMetaData.supportsSubqueriesInQuantifieds(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage59"), e); //$NON-NLS-1$
        }
        data[78][0] = Messages.getString("ConnectionInfoTab.property60"); //$NON-NLS-1$
        try {
            data[78][1] = "" + jdbcMetaData.supportsCorrelatedSubqueries(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage60"), e); //$NON-NLS-1$
        }
        data[79][0] = Messages.getString("ConnectionInfoTab.property61"); //$NON-NLS-1$
        try {
            data[79][1] = "" + jdbcMetaData.supportsUnion(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage61"), e); //$NON-NLS-1$
        }
        data[80][0] = Messages.getString("ConnectionInfoTab.property62"); //$NON-NLS-1$
        try {
            data[80][1] = "" + jdbcMetaData.supportsUnionAll(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage62"), e); //$NON-NLS-1$
        }
    }

    /**
     * DOC set the data(40-60) value ..
     *
     * @param jdbcMetaData
     * @param data
     */
    private void setDataTo60(DatabaseMetaData jdbcMetaData, String[][] data) {
        data[41][0] = Messages.getString("ConnectionInfoTab.property63"); //$NON-NLS-1$
        try {
            data[41][1] = "" + jdbcMetaData.supportsLikeEscapeClause(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage63"), e); //$NON-NLS-1$
        }
        data[42][0] = Messages.getString("ConnectionInfoTab.property64"); //$NON-NLS-1$
        try {
            data[42][1] = "" + jdbcMetaData.supportsMultipleResultSets(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage64"), e); //$NON-NLS-1$
        }
        data[43][0] = Messages.getString("ConnectionInfoTab.property65"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[43][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[43][1] = "" + jdbcMetaData.supportsMultipleOpenResults(); //$NON-NLS-1$
            }
        } catch (SQLException operationException) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage65"), e); //$NON-NLS-1$
        }
        data[44][0] = Messages.getString("ConnectionInfoTab.property66"); //$NON-NLS-1$
        try {
            data[44][1] = "" + jdbcMetaData.supportsMultipleTransactions(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage66"), e); //$NON-NLS-1$
        }
        data[45][0] = Messages.getString("ConnectionInfoTab.property67"); //$NON-NLS-1$
        try {
            data[45][1] = "" + jdbcMetaData.supportsNonNullableColumns(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage67"), e); //$NON-NLS-1$
        }
        data[46][0] = Messages.getString("ConnectionInfoTab.property68"); //$NON-NLS-1$
        try {
            data[46][1] = "" + jdbcMetaData.supportsMinimumSQLGrammar(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage68"), e); //$NON-NLS-1$
        }
        data[47][0] = Messages.getString("ConnectionInfoTab.property69"); //$NON-NLS-1$
        try {
            data[47][1] = "" + jdbcMetaData.supportsCoreSQLGrammar(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage69"), e); //$NON-NLS-1$
        }
        data[48][0] = Messages.getString("ConnectionInfoTab.property70"); //$NON-NLS-1$
        try {
            data[48][1] = "" + jdbcMetaData.supportsExtendedSQLGrammar(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage70"), e); //$NON-NLS-1$
        }
        data[49][0] = Messages.getString("ConnectionInfoTab.property71"); //$NON-NLS-1$
        try {
            data[49][1] = "" + jdbcMetaData.supportsANSI92EntryLevelSQL(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage71"), e); //$NON-NLS-1$
        }
        data[50][0] = Messages.getString("ConnectionInfoTab.property72"); //$NON-NLS-1$
        try {
            data[50][1] = "" + jdbcMetaData.supportsANSI92IntermediateSQL(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage72"), e); //$NON-NLS-1$
        }
        data[51][0] = Messages.getString("ConnectionInfoTab.property73"); //$NON-NLS-1$
        try {
            data[51][1] = "" + jdbcMetaData.supportsANSI92FullSQL(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage73"), e); //$NON-NLS-1$
        }
        data[52][0] = Messages.getString("ConnectionInfoTab.property74"); //$NON-NLS-1$
        try {
            data[52][1] = "" + jdbcMetaData.supportsIntegrityEnhancementFacility(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage74"), e); //$NON-NLS-1$
        }
        data[53][0] = Messages.getString("ConnectionInfoTab.property75"); //$NON-NLS-1$
        try {
            data[53][1] = "" + jdbcMetaData.supportsOuterJoins(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage75"), e); //$NON-NLS-1$
        }
        data[54][0] = Messages.getString("ConnectionInfoTab.property76"); //$NON-NLS-1$
        try {
            data[54][1] = "" + jdbcMetaData.supportsFullOuterJoins(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage76"), e); //$NON-NLS-1$
        }
        data[55][0] = Messages.getString("ConnectionInfoTab.property77"); //$NON-NLS-1$
        try {
            data[55][1] = "" + jdbcMetaData.supportsLimitedOuterJoins(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage77"), e); //$NON-NLS-1$
        }
        data[56][0] = Messages.getString("ConnectionInfoTab.property78"); //$NON-NLS-1$
        try {
            data[56][1] = "" + jdbcMetaData.getSchemaTerm(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage78"), e); //$NON-NLS-1$
        }
        data[57][0] = Messages.getString("ConnectionInfoTab.property79"); //$NON-NLS-1$
        try {
            data[57][1] = "" + jdbcMetaData.getProcedureTerm(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage79"), e); //$NON-NLS-1$
        }
        data[58][0] = Messages.getString("ConnectionInfoTab.property80"); //$NON-NLS-1$
        try {
            data[58][1] = "" + jdbcMetaData.getCatalogTerm(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage80"), e); //$NON-NLS-1$
        }
        data[59][0] = Messages.getString("ConnectionInfoTab.property81"); //$NON-NLS-1$
        try {
            data[59][1] = "" + jdbcMetaData.isCatalogAtStart(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage81"), e); //$NON-NLS-1$
        }
        data[60][0] = Messages.getString("ConnectionInfoTab.property82"); //$NON-NLS-1$
        try {
            data[60][1] = "" + jdbcMetaData.getCatalogSeparator(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage82"), e); //$NON-NLS-1$
        }
    }

    /**
     * DOC set the data(20-40) value ..
     *
     * @param jdbcMetaData
     * @param data
     */
    private void setDataTo40(DatabaseMetaData jdbcMetaData, String[][] data) {
        data[21][0] = Messages.getString("ConnectionInfoTab.property83"); //$NON-NLS-1$
        try {
            data[21][1] = "" + jdbcMetaData.storesMixedCaseIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage83"), e); //$NON-NLS-1$
        }
        data[22][0] = Messages.getString("ConnectionInfoTab.property84"); //$NON-NLS-1$
        try {
            data[22][1] = "" + jdbcMetaData.supportsMixedCaseQuotedIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage84"), e); //$NON-NLS-1$
        }
        data[23][0] = Messages.getString("ConnectionInfoTab.property85"); //$NON-NLS-1$
        try {
            data[23][1] = "" + jdbcMetaData.storesUpperCaseQuotedIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage85"), e); //$NON-NLS-1$
        }
        data[24][0] = Messages.getString("ConnectionInfoTab.property86"); //$NON-NLS-1$
        try {
            data[24][1] = "" + jdbcMetaData.storesLowerCaseQuotedIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage86"), e); //$NON-NLS-1$
        }
        data[25][0] = Messages.getString("ConnectionInfoTab.property87"); //$NON-NLS-1$
        try {
            data[25][1] = "" + jdbcMetaData.storesMixedCaseQuotedIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage87"), e); //$NON-NLS-1$
        }
        data[26][0] = Messages.getString("ConnectionInfoTab.property88"); //$NON-NLS-1$
        try {
            data[26][1] = "" + jdbcMetaData.getIdentifierQuoteString(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage88"), e); //$NON-NLS-1$
        }
        data[27][0] = Messages.getString("ConnectionInfoTab.property89"); //$NON-NLS-1$
        try {
            data[27][1] = "" + jdbcMetaData.getSearchStringEscape(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage89"), e); //$NON-NLS-1$
        }
        data[28][0] = Messages.getString("ConnectionInfoTab.property90"); //$NON-NLS-1$
        try {
            data[28][1] = "" + jdbcMetaData.getExtraNameCharacters(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage90"), e); //$NON-NLS-1$
        }
        data[29][0] = Messages.getString("ConnectionInfoTab.property91"); //$NON-NLS-1$
        try {
            data[29][1] = "" + jdbcMetaData.supportsAlterTableWithAddColumn(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage91"), e); //$NON-NLS-1$
        }
        data[30][0] = Messages.getString("ConnectionInfoTab.property92"); //$NON-NLS-1$
        try {
            data[30][1] = "" + jdbcMetaData.supportsAlterTableWithDropColumn(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage92"), e); //$NON-NLS-1$
        }
        data[31][0] = Messages.getString("ConnectionInfoTab.property93"); //$NON-NLS-1$
        try {
            data[31][1] = "" + jdbcMetaData.supportsColumnAliasing(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage93"), e); //$NON-NLS-1$
        }
        data[32][0] = Messages.getString("ConnectionInfoTab.property94"); //$NON-NLS-1$
        try {
            data[32][1] = "" + jdbcMetaData.nullPlusNonNullIsNull(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage94"), e); //$NON-NLS-1$
        }
        data[33][0] = Messages.getString("ConnectionInfoTab.property95"); //$NON-NLS-1$
        try {
            data[33][1] = "" + jdbcMetaData.supportsConvert(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage95"), e); //$NON-NLS-1$
        }
        data[34][0] = Messages.getString("ConnectionInfoTab.property96"); //$NON-NLS-1$
        try {
            data[34][1] = "" + jdbcMetaData.supportsTableCorrelationNames(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage96"), e); //$NON-NLS-1$
        }
        data[35][0] = Messages.getString("ConnectionInfoTab.property97"); //$NON-NLS-1$
        try {
            data[35][1] = "" + jdbcMetaData.supportsDifferentTableCorrelationNames(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage97"), e); //$NON-NLS-1$
        }
        data[36][0] = Messages.getString("ConnectionInfoTab.property98"); //$NON-NLS-1$
        try {
            data[36][1] = "" + jdbcMetaData.supportsExpressionsInOrderBy(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage98"), e); //$NON-NLS-1$
        }
        data[37][0] = Messages.getString("ConnectionInfoTab.property99"); //$NON-NLS-1$
        try {
            data[37][1] = "" + jdbcMetaData.supportsOrderByUnrelated(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage99"), e); //$NON-NLS-1$
        }
        data[38][0] = Messages.getString("ConnectionInfoTab.property100"); //$NON-NLS-1$
        try {
            data[38][1] = "" + jdbcMetaData.supportsGroupBy(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage100"), e); //$NON-NLS-1$
        }
        data[39][0] = Messages.getString("ConnectionInfoTab.property101"); //$NON-NLS-1$
        try {
            data[39][1] = "" + jdbcMetaData.supportsGroupByUnrelated(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage101"), e); //$NON-NLS-1$
        }
        data[40][0] = Messages.getString("ConnectionInfoTab.property102"); //$NON-NLS-1$
        try {
            data[40][1] = "" + jdbcMetaData.supportsGroupByBeyondSelect(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage102"), e); //$NON-NLS-1$
        }
    }

    /**
     * DOC set the data(0-20) value ..
     *
     * @param node
     * @param sqlMetaData
     * @param jdbcMetaData
     * @param data
     */
    private void setDataTo20(INode node, SQLDatabaseMetaData sqlMetaData, DatabaseMetaData jdbcMetaData, String[][] data) {
        data[0][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DatabaseProductName"); //$NON-NLS-1$
        try {
            data[0][1] = sqlMetaData.getDatabaseProductName();
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage103"), e); //$NON-NLS-1$
        }
        data[1][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverMajor"); //$NON-NLS-1$
        try {
            data[1][1] = "" + jdbcMetaData.getDriverMajorVersion(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage104"), e); //$NON-NLS-1$
        }
        data[2][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverMinor"); //$NON-NLS-1$
        try {
            data[2][1] = "" + jdbcMetaData.getDriverMinorVersion(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage105"), e); //$NON-NLS-1$
        }
        data[3][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverName"); //$NON-NLS-1$
        try {
            data[3][1] = "" + sqlMetaData.getDriverName(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage106"), e); //$NON-NLS-1$
        }
        data[4][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.DriverVersion"); //$NON-NLS-1$
        try {
            data[4][1] = "" + jdbcMetaData.getDriverVersion(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage107"), e); //$NON-NLS-1$
        }
        data[5][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.UserName"); //$NON-NLS-1$
        try {
            data[5][1] = "" + sqlMetaData.getUserName(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage108"), e); //$NON-NLS-1$
        }
        data[6][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.URL"); //$NON-NLS-1$
        try {
            data[6][1] = "" + jdbcMetaData.getURL(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage109"), e); //$NON-NLS-1$
        }
        data[7][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.AutocommitMode"); //$NON-NLS-1$
        try {
            data[7][1] = "" + jdbcMetaData.getConnection().getAutoCommit(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage110"), e); //$NON-NLS-1$
        }
        data[8][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.CommitOnClose"); //$NON-NLS-1$
        try {
            data[8][1] = "" + node.getSession().getInteractiveConnection().getCommitOnClose(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage111"), e); //$NON-NLS-1$
        }
        data[9][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.ProceduresCallable"); //$NON-NLS-1$
        try {
            data[9][1] = "" + jdbcMetaData.allProceduresAreCallable(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage112"), e); //$NON-NLS-1$
        }
        data[10][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.TablesSelectable"); //$NON-NLS-1$
        try {
            data[10][1] = "" + jdbcMetaData.allTablesAreSelectable(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage113"), e); //$NON-NLS-1$
        }
        data[11][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedHigh"); //$NON-NLS-1$
        try {
            data[11][1] = "" + jdbcMetaData.nullsAreSortedHigh(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage114"), e); //$NON-NLS-1$
        }
        data[12][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedLow"); //$NON-NLS-1$
        try {
            data[12][1] = "" + jdbcMetaData.nullsAreSortedLow(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage115"), e); //$NON-NLS-1$
        }
        data[13][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedStart"); //$NON-NLS-1$
        try {
            data[13][1] = "" + jdbcMetaData.nullsAreSortedAtStart(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage116"), e); //$NON-NLS-1$
        }
        data[14][0] = Messages.getString("DatabaseDetailView.Tab.ConnectionInfo.NullsSortedEnd"); //$NON-NLS-1$
        try {
            data[14][1] = "" + jdbcMetaData.nullsAreSortedAtEnd(); //$NON-NLS-1$
        } catch (UnsupportedOperationException exception) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage117"), e); //$NON-NLS-1$
        }
        data[15][0] = Messages.getString("ConnectionInfoTab.property103"); //$NON-NLS-1$
        try {
            if (jdbcMetaData.getURL().startsWith("jdbc:sybase")) {//$NON-NLS-1$
                data[15][1] = "" + 0; //$NON-NLS-1$
            } else {
                data[15][1] = "" + jdbcMetaData.getResultSetHoldability(); //$NON-NLS-1$
            }

        } catch (UnsupportedOperationException operationException) {
            // do nothing.
        } catch (SQLException sql) {
            // do nothing.
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage118"), e); //$NON-NLS-1$
        }
        data[16][0] = Messages.getString("ConnectionInfoTab.property104"); //$NON-NLS-1$
        try {
            data[16][1] = "" + jdbcMetaData.usesLocalFiles(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage119"), e); //$NON-NLS-1$
        }
        data[17][0] = Messages.getString("ConnectionInfoTab.property105"); //$NON-NLS-1$
        try {
            data[17][1] = "" + jdbcMetaData.usesLocalFilePerTable(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage120"), e); //$NON-NLS-1$
        }
        data[18][0] = Messages.getString("ConnectionInfoTab.property106"); //$NON-NLS-1$
        try {
            data[18][1] = "" + jdbcMetaData.supportsMixedCaseIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage121"), e); //$NON-NLS-1$
        }
        data[19][0] = Messages.getString("ConnectionInfoTab.property107"); //$NON-NLS-1$
        try {
            data[19][1] = "" + jdbcMetaData.storesUpperCaseIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage122"), e); //$NON-NLS-1$
        }
        data[20][0] = Messages.getString("ConnectionInfoTab.property108"); //$NON-NLS-1$
        try {
            data[20][1] = "" + jdbcMetaData.storesLowerCaseIdentifiers(); //$NON-NLS-1$
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ConnectionInfoTab.logMessage123"), e); //$NON-NLS-1$
        }
    }
}
