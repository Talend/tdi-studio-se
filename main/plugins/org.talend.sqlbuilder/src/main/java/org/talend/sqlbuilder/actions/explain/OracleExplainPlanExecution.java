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
package org.talend.sqlbuilder.actions.explain;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;

import net.sourceforge.sqlexplorer.sqlpanel.SQLResult;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;
import org.talend.sqlbuilder.sqlcontrol.AbstractSQLExecution;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class OracleExplainPlanExecution extends AbstractSQLExecution {

    /**
     * DOC dev ExplainPlanExecution class global comment. Detailled comment <br/>
     *
     * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
     *
     */
    static class TreeLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {

            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {

            ExplainNode en = (ExplainNode) element;
            if (columnIndex == 0) {
                return en.toString();
            }
            if (columnIndex == 1) {
                int cost = en.getCost();
                if (cost != -1) {
                    return "" + cost; //$NON-NLS-1$
                } else {
                    return ""; //$NON-NLS-1$
                }
            } else if (columnIndex == 2) {
                int card = en.getCardinality();
                if (card != -1) {
                    return "" + card; //$NON-NLS-1$
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    private PreparedStatement prepStmt;

    protected SQLResult sqlResult;

    protected Statement stmt;

    /**
     * DOC dev ExplainPlanExecution constructor comment.
     */
    public OracleExplainPlanExecution(String sqlString, SessionTreeNode sessionTreeNode) {
        this.sqlStatement = sqlString;
        session = sessionTreeNode;
        sqlResult = new SQLResult();
        sqlResult.setSqlStatement(sqlStatement);

        // set initial message
        setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait")); //$NON-NLS-1$
    }

    private void displayResults(final ExplainNode node) {

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            @SuppressWarnings("deprecation")
            public void run() {

                clearCanvas();

                GridLayout gLayout = new GridLayout();
                gLayout.numColumns = 2;
                gLayout.marginLeft = 0;
                gLayout.horizontalSpacing = 0;
                gLayout.verticalSpacing = 0;
                gLayout.marginWidth = 0;
                gLayout.marginHeight = 0;
                composite.setLayout(gLayout);

                try {
                    composite.setData("parenttab", parentTab); //$NON-NLS-1$

                    Composite pp = new Composite(composite, SWT.NULL);
                    pp.setLayout(new FillLayout());
                    pp.setLayoutData(new GridData(GridData.FILL_BOTH));
                    TreeViewer tv = new TreeViewer(pp, SWT.BORDER | SWT.FULL_SELECTION);
                    Tree tree = tv.getTree();
                    tree.setLinesVisible(true);
                    tree.setHeaderVisible(true);
                    TreeColumn tc = new TreeColumn(tree, SWT.NULL);
                    tc.setText(""); //$NON-NLS-1$
                    tc = new TreeColumn(tree, SWT.NULL);
                    tc.setText(Messages.getString("OracleExplainPlanExecution.tableColumnText1")); //$NON-NLS-1$
                    tc = new TreeColumn(tree, SWT.NULL);
                    tc.setText(Messages.getString("OracleExplainPlanExecution.tableColumnText2")); //$NON-NLS-1$
                    TableLayout tableLayout = new TableLayout();
                    tableLayout.addColumnData(new ColumnWeightData(6, 150, true));
                    tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
                    tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
                    tree.setLayout(tableLayout);

                    tv.setContentProvider(new ITreeContentProvider() {

                        @Override
                        public void dispose() {

                        }

                        @Override
                        public Object[] getChildren(Object parentElement) {

                            return ((ExplainNode) parentElement).getChildren();
                        }

                        @Override
                        public Object[] getElements(Object inputElement) {

                            ExplainNode nd = ((ExplainNode) inputElement);

                            return nd.getChildren();
                        }

                        @Override
                        public Object getParent(Object element) {

                            return ((ExplainNode) element).getParent();
                        }

                        @Override
                        public boolean hasChildren(Object element) {

                            if (((ExplainNode) element).getChildren().length > 0) {
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

                        }
                    });
                    tv.setLabelProvider(new TreeLabelProvider() {
                    });
                    tv.setInput(node);
                    tv.refresh();
                    tv.expandAll();

                    // make columns full size
                    for (int i = 0; i < tree.getColumnCount(); i++) {
                        tree.getColumn(i).pack();
                    }

                    final Composite parent = composite;
                    tree.addKeyListener(new KeyAdapter() {

                        @Override
                        public void keyReleased(KeyEvent e) {

                            switch (e.keyCode) {

                            case SWT.F5:

                                // refresh SQL Results
                                try {
                                    Object o = parent.getData("parenttab"); //$NON-NLS-1$
                                    if (o != null) {
                                        AbstractSQLExecution sqlExec = (AbstractSQLExecution) ((TabItem) o).getData();
                                        if (sqlExec != null) {
                                            sqlExec.startExecution();
                                        }
                                    }
                                } catch (Exception e1) {
                                    SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage1"), e1); //$NON-NLS-1$
                                }

                                break;
                            default:
                                return;

                            }

                        }

                    });

                } catch (Exception e) {

                    // add message
                    String message = e.getMessage();
                    Label errorLabel = new Label(composite, SWT.FILL);
                    errorLabel.setText(message);
                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

                    SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage2"), e); //$NON-NLS-1$
                }

                composite.layout();
                composite.redraw();

            };
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doExecution() throws Exception {

        try {

            if (isCancelled) {
                return;
            }

            stmt = connection.createStatement();
            String idp = Integer.toHexString(new Random().nextInt()).toUpperCase();
            stmt.execute("delete plan_table where statement_id='" + idp + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            stmt.close();
            stmt = null;

            if (isCancelled) {
                return;
            }

            stmt = connection.createStatement();
            stmt.execute("EXPLAIN PLAN SET statement_id = '" + idp + "' FOR " + sqlStatement); //$NON-NLS-1$ //$NON-NLS-2$
            stmt.close();
            stmt = null;

            if (isCancelled) {
                return;
            }

            prepStmt = connection.prepareStatement("select " //$NON-NLS-1$
                    + "object_type,operation,options,object_owner,object_name,optimizer,cardinality ,cost,id,parent_id " //$NON-NLS-1$
                    + " from " + " plan_table " + " start with id = 0 and statement_id=? " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + " connect by prior id=parent_id and statement_id=?"); //$NON-NLS-1$
            prepStmt.setString(1, idp);
            prepStmt.setString(2, idp);
            ResultSet rs = prepStmt.executeQuery();

            if (isCancelled) {
                return;
            }

            HashMap mp = new HashMap();
            while (rs.next()) {
                String objectType = rs.getString("object_type"); //$NON-NLS-1$
                String operation = rs.getString("operation"); //$NON-NLS-1$
                String options = rs.getString("options"); //$NON-NLS-1$
                String objectOwner = rs.getString("object_owner"); //$NON-NLS-1$
                String objectName = rs.getString("object_name"); //$NON-NLS-1$
                String optimizer = rs.getString("optimizer"); //$NON-NLS-1$
                int cardinality = rs.getInt("cardinality"); //$NON-NLS-1$
                if (rs.wasNull()) {
                    cardinality = -1;
                }

                int cost = rs.getInt("cost"); //$NON-NLS-1$
                if (rs.wasNull()) {
                    cost = -1;
                }
                int parentID = rs.getInt("parent_id"); //$NON-NLS-1$
                int id = rs.getInt("id"); //$NON-NLS-1$
                ExplainNode nd = null;
                if (id == 0) {
                    ExplainNode dummy = new ExplainNode(null);
                    mp.put(new Integer(-1), dummy);
                    dummy.setId(-1);
                    nd = new ExplainNode(dummy);
                    dummy.add(nd);
                    nd.setId(0);
                    mp.put(new Integer(0), nd);
                } else {
                    ExplainNode ndParent = (ExplainNode) mp.get(new Integer(parentID));

                    nd = new ExplainNode(ndParent);
                    ndParent.add(nd);
                    mp.put(new Integer(id), nd);
                }
                nd.setCardinality(cardinality);
                nd.setCost(cost);
                nd.setObjectName(objectName);
                nd.setObjectOwner(objectOwner);
                nd.setObjectType(objectType);
                nd.setOperation(operation);
                nd.setOptimizer(optimizer);
                nd.setOptions(options);
            }
            rs.close();
            prepStmt.close();
            prepStmt = null;
            ExplainNode ndParent = (ExplainNode) mp.get(new Integer(-1));

            if (isCancelled) {
                return;
            }
            displayResults(ndParent);

        } catch (Exception e) {

            if (stmt != null) {

                try {
                    stmt.close();
                    stmt = null;
                } catch (Exception e1) {
                    SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage3"), e); //$NON-NLS-1$
                }
            }

            if (prepStmt != null) {
                try {
                    prepStmt.close();
                    prepStmt = null;
                } catch (Exception e1) {
                    SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage3"), e); //$NON-NLS-1$
                }
            }
            throw e;
        }

    }

    @Override
    protected void doStop() throws Exception {

        Exception t = null;

        if (stmt != null) {

            try {
                stmt.cancel();
            } catch (Exception e) {
                t = e;
                SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage4"), e); //$NON-NLS-1$
            }
            try {
                stmt.close();
                stmt = null;
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage3"), e); //$NON-NLS-1$
            }
        }

        if (prepStmt != null) {

            try {
                prepStmt.cancel();
            } catch (Exception e) {
                t = e;
                SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage4"), e); //$NON-NLS-1$
            }
            try {
                prepStmt.close();
                prepStmt = null;
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanExecution.logMessage3"), e); //$NON-NLS-1$
            }
        }

        if (t != null) {
            throw t;
        }
    }
}
