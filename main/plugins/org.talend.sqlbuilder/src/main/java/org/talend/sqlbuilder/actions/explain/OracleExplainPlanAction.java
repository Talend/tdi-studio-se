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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.actions.AbstractEditorAction;
import org.talend.sqlbuilder.actions.IResultDisplayer;
import org.talend.sqlbuilder.dbstructure.SessionTreeNodeManager;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;
import org.talend.sqlbuilder.ui.editor.ISQLEditor;
import org.talend.sqlbuilder.util.ImageUtil;
import org.talend.sqlbuilder.util.QueryTokenizer;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class OracleExplainPlanAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.Explain"); //$NON-NLS-1$

    static String createPlanTableScript = "CREATE TABLE PLAN_TABLE (" //$NON-NLS-1$
            + "  STATEMENT_ID                    VARCHAR2(30)," + " TIMESTAMP                       DATE," //$NON-NLS-1$ //$NON-NLS-2$
            + "  REMARKS                         VARCHAR2(80)," + "  OPERATION                       VARCHAR2(30)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  OPTIONS                         VARCHAR2(30)," + "  OBJECT_NODE                     VARCHAR2(128)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  OBJECT_OWNER                    VARCHAR2(30)," + "  OBJECT_NAME                     VARCHAR2(30)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  OBJECT_INSTANCE                 NUMBER(38)," + "  OBJECT_TYPE                     VARCHAR2(30)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  OPTIMIZER                       VARCHAR2(255)," + "  SEARCH_COLUMNS                  NUMBER," //$NON-NLS-1$ //$NON-NLS-2$
            + "  ID                              NUMBER(38)," + "  PARENT_ID                       NUMBER(38)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  POSITION                        NUMBER(38)," + "  COST                            NUMBER(38)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  CARDINALITY                     NUMBER(38)," + "  BYTES                           NUMBER(38)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  OTHER_TAG                       VARCHAR2(255)," + "  PARTITION_START                 VARCHAR2(255)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  PARTITION_STOP                  VARCHAR2(255)," + "  PARTITION_ID                    NUMBER(38)," //$NON-NLS-1$ //$NON-NLS-2$
            + "  OTHER                           LONG," + "  DISTRIBUTION                    VARCHAR2(30)" + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private ISQLEditor editor;

    private IResultDisplayer resultDisplayer;

    /**
     * DOC dev ExplainPlanAction constructor comment.
     */
    public OracleExplainPlanAction(IResultDisplayer resultDisplayer, ISQLEditor editor) {
        super();
        this.editor = editor;
        this.resultDisplayer = resultDisplayer;
        this.setImageDescriptor(img);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getText()
     */
    @Override
    public String getText() {
        return Messages.getString("oracle.editor.actions.explain"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public void run() {

        RepositoryNode node = editor.getRepositoryNode();
        SessionTreeNodeManager nodeManager = new SessionTreeNodeManager();
        SessionTreeNode runNode = null;

        try {
            runNode = nodeManager.getSessionTreeNode(node, editor.getDialog().getSelectedContext());
        } catch (Exception e) {
            MessageDialog.openError(null, Messages.getString("AbstractSQLExecution.Executing.Error"), e.getMessage()); //$NON-NLS-1$
            SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanAction.logMessage1"), e); //$NON-NLS-1$
            return;
        }
        Preferences prefs = SqlBuilderPlugin.getDefault().getPluginPreferences();

        String queryDelimiter = prefs.getString(IConstants.QUERY_DELIMITER);
        String alternateDelimiter = prefs.getString(IConstants.ALTERNATE_DELIMITER);
        String commentDelimiter = prefs.getString(IConstants.COMMENT_DELIMITER);

        QueryTokenizer qt = new QueryTokenizer(getSQLToBeExecuted(), queryDelimiter, alternateDelimiter, commentDelimiter);
        List queryStrings = new ArrayList();
        while (qt.hasQuery()) {
            final String querySql = qt.nextQuery();
            // ignore commented lines.
            if (!querySql.startsWith("--")) { //$NON-NLS-1$
                queryStrings.add(querySql);
            }
        }

        // check if we can run explain plans
        try {
            Statement st = runNode.getInteractiveConnection().createStatement();
            boolean createPlanTable = false;
            boolean notFoundTable = true;
            try {
                ResultSet rs = st.executeQuery("select statement_id from plan_table"); //$NON-NLS-1$
                notFoundTable = false;
                rs.close();
            } catch (Throwable e) {
                createPlanTable = MessageDialog.openQuestion(null,
                        Messages.getString("oracle.editor.actions.explain.notFound.Title"), //$NON-NLS-1$
                        Messages.getString("oracle.editor.actions.explain.notFound")); //$NON-NLS-1$
            } finally {
                try {
                    st.close();
                } catch (Throwable e) {
                    SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanAction.logMessage2"), e); //$NON-NLS-1$
                }
            }
            if (notFoundTable && !createPlanTable) {
                return;
            }

            if (notFoundTable && createPlanTable) {

                SQLConnection conn = runNode.getInteractiveConnection();
                st = conn.createStatement();

                try {
                    st.execute(createPlanTableScript);

                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }

                } catch (Throwable e) {
                    SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanAction.logMessage2"), e); //$NON-NLS-1$
                    MessageDialog.openError(null, Messages.getString("oracle.editor.actions.explain.createError.Title"), //$NON-NLS-1$
                            Messages.getString("oracle.editor.actions.explain.createError")); //$NON-NLS-1$
                    try {
                        st.close();
                    } catch (Throwable e1) {
                        SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanAction.logMessage3"), e1); //$NON-NLS-1$
                    }
                    return;
                }
                try {
                    st.close();
                } catch (Throwable e) {
                    SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanAction.logMessage3"), e); //$NON-NLS-1$
                }
            }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanAction.logMessage4"), e); //$NON-NLS-1$
        }

        // execute explain plan for all statements

        try {
            while (!queryStrings.isEmpty()) {

                String querySql = (String) queryStrings.remove(0);

                if (querySql != null) {
                    resultDisplayer.addSQLExecution(new OracleExplainPlanExecution(querySql, runNode));
                }
            }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("OracleExplainPlanAction.logMessage5"), e); //$NON-NLS-1$
        }
    }

    /**
     * Gets sql for executing.
     *
     * @return string
     */
    public String getSQLToBeExecuted() {
        String sql = editor.getSQLToBeExecuted();
        return sql;
    }
}
