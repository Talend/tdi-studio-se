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
public class DB2ExplainPlanExecution extends AbstractSQLExecution {

    /**
     * DOC dev DB2ExplainPlanExecution class global comment. Detailled comment <br/>
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

    private SQLResult sqlResult;

    private Statement stmt;

    /**
     * DOC dev DB2ExplainPlanExecution constructor comment.
     */
    public DB2ExplainPlanExecution(String sqlString, SessionTreeNode sessionTreeNode) {
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
                    tc.setText(Messages.getString("DB2ExplainPlanExecution.tableColumnText1")); //$NON-NLS-1$
                    tc = new TreeColumn(tree, SWT.NULL);
                    tc.setText(Messages.getString("DB2ExplainPlanExecution.tableColumnText2")); //$NON-NLS-1$
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
                                    SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError1"), e1); //$NON-NLS-1$
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

                    SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError2"), e); //$NON-NLS-1$
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

            setProgressMessage(Messages.getString("SQLResultsView.Executing")); //$NON-NLS-1$

            int idp = new Random().nextInt(1000);

            prepStmt = connection.prepareStatement("delete from SYSTOOLS.explain_statement where queryno = ? "); //$NON-NLS-1$
            prepStmt.setInt(1, idp);
            prepStmt.executeUpdate();
            prepStmt.close();
            prepStmt = null;

            if (isCancelled) {
                return;
            }

            stmt = connection.createStatement();
            stmt.execute("EXPLAIN PLAN SET queryno = " + idp + " FOR " + sqlStatement); //$NON-NLS-1$ //$NON-NLS-2$
            stmt.close();
            stmt = null;

            if (isCancelled) {
                return;
            }

            prepStmt = connection.prepareStatement("SELECT O.Operator_ID as id, S2.Target_ID as parent_id, O.Operator_Type, " //$NON-NLS-1$
                    + "S.OBJECT_SCHEMA, EOB.OBJECT_TYPE, S.Object_Name, O.CPU_COST,  " //$NON-NLS-1$
                    + "CAST(O.Total_Cost AS INTEGER) Cost FROM SYSTOOLS.EXPLAIN_OPERATOR O " //$NON-NLS-1$
                    + "LEFT OUTER JOIN SYSTOOLS.EXPLAIN_STREAM S2 ON O.Operator_ID=S2.Source_ID " //$NON-NLS-1$
                    + "LEFT OUTER JOIN SYSTOOLS.EXPLAIN_STREAM S  ON O.Operator_ID = S.Target_ID " //$NON-NLS-1$
                    + "AND O.Explain_Time = S.Explain_Time AND S.Object_Name IS NOT NULL " //$NON-NLS-1$
                    + "LEFT OUTER JOIN SYSTOOLS.explain_object EOB ON O.Explain_Time = EOB.Explain_Time     " //$NON-NLS-1$
                    + "AND S.OBJECT_NAME = EOB.OBJECT_NAME " //$NON-NLS-1$
                    + "where o.explain_time =  (select max(explain_time) from SYSTOOLS.explain_statement where queryno = ?) " //$NON-NLS-1$
                    + "ORDER BY O.Operator_ID ASC, S2.TARGET_ID ASC "); //$NON-NLS-1$
            prepStmt.setInt(1, idp);
            ResultSet rs = prepStmt.executeQuery();

            if (isCancelled) {
                return;
            }

            HashMap mp = new HashMap();
            ExplainNode baseNode = new ExplainNode(null);
            mp.put(new Integer(-1), baseNode);
            int lastId = -1;

            while (rs.next()) {
                String objectType = rs.getString("object_type"); //$NON-NLS-1$
                String operation = rs.getString("operator_type"); //$NON-NLS-1$
                String options = null;
                String objectOwner = rs.getString("object_schema"); //$NON-NLS-1$
                String objectName = rs.getString("object_name"); //$NON-NLS-1$
                String optimizer = null;
                int cardinality = rs.getInt("cpu_cost"); //$NON-NLS-1$
                if (rs.wasNull()) {
                    cardinality = -1;
                }

                int cost = rs.getInt("cost"); //$NON-NLS-1$
                if (rs.wasNull()) {
                    cost = -1;

                }
                int parentID = rs.getInt("parent_id"); //$NON-NLS-1$
                if (rs.wasNull()) {
                    parentID = -1;
                }
                int id = rs.getInt("id"); //$NON-NLS-1$

                if (id != lastId) {

                    lastId = id;
                    ExplainNode ndParent = (ExplainNode) mp.get(new Integer(parentID));
                    ExplainNode nd = new ExplainNode(ndParent);
                    mp.put(new Integer(id), nd);

                    ndParent.add(nd);
                    nd.setId(id);
                    nd.setCardinality(cardinality);
                    nd.setCost(cost);
                    nd.setObjectName(objectName);
                    nd.setObjectOwner(objectOwner);
                    nd.setObjectType(objectType);
                    nd.setOperation(operation);
                    nd.setOptimizer(optimizer);
                    nd.setOptions(options);
                }
            }
            rs.close();
            prepStmt.close();
            prepStmt = null;

            if (isCancelled) {
                return;
            }

            displayResults(baseNode);

        } catch (Exception e) {

            if (stmt != null) {

                try {
                    stmt.close();
                    stmt = null;
                } catch (Exception e1) {
                    SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError3"), e); //$NON-NLS-1$
                }
            }

            if (prepStmt != null) {
                try {
                    prepStmt.close();
                    prepStmt = null;
                } catch (Exception e1) {
                    SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError3"), e); //$NON-NLS-1$
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
                SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError4"), e); //$NON-NLS-1$
            }
            try {
                stmt.close();
                stmt = null;
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError3"), e); //$NON-NLS-1$
            }
        }

        if (prepStmt != null) {

            try {
                prepStmt.cancel();
            } catch (Exception e) {
                t = e;
                SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError4"), e); //$NON-NLS-1$
            }
            try {
                prepStmt.close();
                prepStmt = null;
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanExecution.logMessageError3"), e); //$NON-NLS-1$
            }
        }

        if (t != null) {
            throw t;
        }
    }

}
