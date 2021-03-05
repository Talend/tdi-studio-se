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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: AbstractSQLFolderNode.java,v 1.4 2006/11/01 05:40:59 peiqin.hou Exp $
 *
 */
public abstract class AbstractSQLFolderNode extends AbstractFolderNode {

    /**
     * Get childType.
     *
     * @return Child type.
     */
    public abstract String getChildType();

    /**
     * Get name.
     *
     * @return name.
     */
    public abstract String getName();

    /**
     * @return SQL.
     */
    public abstract String getSQL();

    /**
     * @return SQLParameters.
     */
    public abstract Object[] getSQLParameters();

    /**
     * Load Children.
     */
    public final void loadChildren() {

        SQLConnection connection = getSession().getInteractiveConnection();
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

            while (rs.next()) {

                String name = rs.getString(1);
                if (!isExcludedByFilter(name)) {
                    ObjectNode node = new ObjectNode(name, getChildType(), this, pimage);
                    addChildNode(node);
                }
            }

            rs.close();

        } catch (Exception e) {

            SqlBuilderPlugin.log(Messages.getString("AbstractSQLFolderNode.logMessage1") + getName(), e); //$NON-NLS-1$

        } finally {

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    SqlBuilderPlugin.log(Messages.getString("AbstractSQLFolderNode.logMessage2"), e); //$NON-NLS-1$
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (Exception e) {
                    SqlBuilderPlugin.log(Messages.getString("AbstractSQLFolderNode.logMessage2"), e); //$NON-NLS-1$
                }
            }
        }

    }

}
