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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * @author k709335
 *
 */
public abstract class AbstractSQLSourceTab extends AbstractSourceTab {

    public abstract String getSQL();

    public abstract Object[] getSQLParameters();

    public String getSource() {

        String source = null;

        SQLConnection connection = getNode().getSession().getInteractiveConnection();
        ResultSet rs = null;
        Statement stmt = null;
        PreparedStatement pStmt = null;

        int timeOut = SqlBuilderPlugin.getDefault().getPluginPreferences().getInt(IConstants.INTERACTIVE_QUERY_TIMEOUT);

        try {

            Object[] params = getSQLParameters();
            if (params == null || params.length == 0) {

                // use normal statement
                stmt = connection.createStatement();
                stmt.setQueryTimeout(timeOut);
                rs = stmt.executeQuery(getSQL());

            } else {

                // use prepared statement
                pStmt = connection.prepareStatement(getSQL());
                pStmt.setQueryTimeout(timeOut);

                for (int i = 0; i < params.length; i++) {

                    if (params[i] instanceof String) {
                        pStmt.setString(i + 1, (String) params[i]);
                    } else if (params[i] instanceof Integer) {
                        pStmt.setInt(i + 1, ((Integer) params[i]).intValue());
                    } else if (params[i] instanceof String) {
                        pStmt.setLong(i + 1, ((Long) params[i]).longValue());
                    }
                }

                rs = pStmt.executeQuery();
            }

            source = ""; //$NON-NLS-1$
            while (rs.next()) {

                source = source + rs.getString(1);
            }

            rs.close();

        } catch (Exception e) {

            SqlBuilderPlugin.log(Messages.getString("AbstractSQLSourceTab.logMessage1") + getNode().getName(), e); //$NON-NLS-1$

        } finally {

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    SqlBuilderPlugin.log(Messages.getString("AbstractSQLSourceTab.logMessage2"), e); //$NON-NLS-1$
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (Exception e) {
                    SqlBuilderPlugin.log(Messages.getString("AbstractSQLSourceTab.logMessage2"), e); //$NON-NLS-1$
                }
            }
        }
        return source;
    }

}
