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
package org.talend.sqlbuilder.sqlcontrol;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dataset.dataset.DataSet;
import org.talend.sqlbuilder.dataset.dataset.DataSetTable;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: SQLExecution.java,v 1.14 2006/10/27 00:35:20 qiang.zhang Exp $
 *
 */
public class SQLExecution extends AbstractSQLExecution {

    protected int maxRows;

    protected SQLResult sqlResult;

    protected Statement stmt;

    public SQLExecution(String sqlString, int maxRows, SessionTreeNode sessionTreeNode) {

        sqlStatement = sqlString;
        this.maxRows = maxRows;
        session = sessionTreeNode;
        sqlResult = new SQLResult();
        sqlResult.setSqlStatement(sqlStatement);

        // set initial message
        setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait")); //$NON-NLS-1$

    }

    /**
     * Display SQL Results in result pane.
     */
    protected void displayResults() {

        Display.getDefault().asyncExec(new Runnable() {

            public void run() {

                clearCanvas();

                composite.setLayout(new GridLayout());

                try {
                    int resultCount = sqlResult.getDataSet().getRows().length;
                    String statusMessage = Messages.getString("SQLResultsView.Time.Prefix") + " " //$NON-NLS-1$ //$NON-NLS-2$
                            + sqlResult.getExecutionTimeMillis() + " " //$NON-NLS-1$
                            + Messages.getString("SQLResultsView.Time.Postfix"); //$NON-NLS-1$

                    if (resultCount > 0) {
                        statusMessage = statusMessage + "  " //$NON-NLS-1$
                                + Messages.getString("SQLResultsView.Count.Prefix") + " " + resultCount; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    new DataSetTable(composite, sqlResult.getDataSet(), statusMessage, true);

                    composite.setData("parenttab", parentTab); //$NON-NLS-1$

                } catch (Exception e) {

                    // add message
                    String message = e.getMessage();
                    Label errorLabel = new Label(composite, SWT.FILL);
                    errorLabel.setText(message);
                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

                    SqlBuilderPlugin.log(Messages.getString("SQLExecution.logMessage1"), e); //$NON-NLS-1$
                }

                composite.layout();
                composite.redraw();

                // reset to start message in case F5 will be used
                setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait")); //$NON-NLS-1$
            };
        });
    }

    private void closeStatement() {

        if (stmt == null) {
            return;
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("SQLExecution.logMessage2"), e); //$NON-NLS-1$
            } finally {
                // bug 17980
                DriverShim wapperDriver = session.getWapperDriver();
                if (wapperDriver != null) {
                    try {
                        wapperDriver.connect("jdbc:derby:;shutdown=true", null); //$NON-NLS-1$
                    } catch (SQLException e) {
                        // exception of shutdown success. no need to catch.
                    }
                }
            }
        }
        stmt = null;

    }

    protected void doExecution() throws Exception {

        final long startTime = System.currentTimeMillis();

        try {

            stmt = connection.createStatement();

            setProgressMessage(Messages.getString("SQLResultsView.Executing")); //$NON-NLS-1$

            stmt.setMaxRows(maxRows);

            if (isCancelled) {
                return;
            }

            boolean b = stmt.execute(sqlStatement);

            if (isCancelled) {
                closeStatement();
                return;
            }

            if (b) {

                final ResultSet rs = stmt.getResultSet();
                if (rs != null) {

                    if (isCancelled) {
                        closeStatement();
                        return;
                    }

                    // create new dataset from results
                    DataSet dataSet = new DataSet(null, rs, null);
                    final long endTime = System.currentTimeMillis();

                    // update sql result
                    sqlResult.setDataSet(dataSet);
                    sqlResult.setExecutionTimeMillis(endTime - startTime);

                    closeStatement();

                    if (isCancelled) {
                        return;
                    }

                    // show results..
                    displayResults();

                    // update text on editor
                    composite.getDisplay().asyncExec(new Runnable() {

                        public void run() {
                        }
                    });
                }

            } else {

                // update text on editor
                composite.getDisplay().asyncExec(new Runnable() {

                    public void run() {

                        // close tab
                        parentTab.dispose();
                    }
                });

                closeStatement();

                if (isCancelled) {
                    return;
                }
            }

            stmt = null;

        } catch (Exception e) {

            closeStatement();
            throw e;
        } finally {
            // bug 17980
            DriverShim wapperDriver = session.getWapperDriver();
            if (wapperDriver != null) {
                try {
                    wapperDriver.connect("jdbc:derby:;shutdown=true", null); //$NON-NLS-1$
                } catch (SQLException e) {
                    // exception of shutdown success. no need to catch.
                }
            }
        }

    }

    /**
     * Cancel sql execution and close execution tab.
     */
    public void doStop() {
        // wapperdriver is not null such as derby
        DriverShim wapperDriver = session.getWapperDriver();
        if (stmt != null && wapperDriver == null) {

            try {
                stmt.cancel();
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("SQLExecution.logMessage3"), e); //$NON-NLS-1$
            }
            try {
                closeStatement();
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("SQLExecution.logMessage4"), e); //$NON-NLS-1$
            }
        }

    }

    public SQLResult getSQLResult() {
        return this.sqlResult;
    }
}
