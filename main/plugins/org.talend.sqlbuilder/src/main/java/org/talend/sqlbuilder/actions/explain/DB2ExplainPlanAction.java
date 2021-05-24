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
public class DB2ExplainPlanAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.Explain"); //$NON-NLS-1$

    private ISQLEditor editor;

    private IResultDisplayer resultDisplayer;

    /**
     * DOC dev ExplainPlanAction constructor comment.
     */
    public DB2ExplainPlanAction(IResultDisplayer resultDisplayer, ISQLEditor editor) {
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
        return Messages.getString("db2.editor.actions.explain"); //$NON-NLS-1$
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
            SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanAction.logMessage1"), e); //$NON-NLS-1$
            return;
        }

        Preferences prefs = SqlBuilderPlugin.getDefault().getPluginPreferences();

        String queryDelimiter = prefs.getString(IConstants.QUERY_DELIMITER);
        String alternateDelimiter = prefs.getString(IConstants.ALTERNATE_DELIMITER);
        String commentDelimiter = prefs.getString(IConstants.COMMENT_DELIMITER);

        QueryTokenizer qt = new QueryTokenizer(getSQLToBeExecuted(), queryDelimiter, alternateDelimiter, commentDelimiter);
        final List queryStrings = new ArrayList();
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
                ResultSet rs = st.executeQuery("select queryno from SYSTOOLS.EXPLAIN_STATEMENT"); //$NON-NLS-1$
                notFoundTable = false;
                rs.close();
            } catch (Throwable e) {
                createPlanTable = MessageDialog.openQuestion(null,
                        Messages.getString("db2.editor.actions.explain.notFound.Title"), //$NON-NLS-1$
                        Messages.getString("db2.editor.actions.explain.notFound")); //$NON-NLS-1$
            } finally {
                try {
                    st.close();
                } catch (Throwable e) {
                    SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanAction.logMessage2"), e); //$NON-NLS-1$
                }
            }
            if (notFoundTable && !createPlanTable) {
                return;
            }

            if (notFoundTable && createPlanTable) {
                SQLConnection conn = runNode.getInteractiveConnection();
                st = conn.createStatement();
                try {
                    st.execute(createPlanScript1);
                    st.execute(createPlanScript2);
                    st.execute(createPlanScript3);
                    st.execute(createPlanScript4);
                    st.execute(createPlanScript5);
                    st.execute(createPlanScript6);
                    st.execute(createPlanScript7);

                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }

                } catch (Throwable e) {
                    SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanAction.logMessage3"), e); //$NON-NLS-1$
                    MessageDialog.openError(null, Messages.getString("db2.editor.actions.explain.createError.Title"), //$NON-NLS-1$
                            Messages.getString("db2.editor.actions.explain.createError")); //$NON-NLS-1$
                    try {
                        st.close();
                    } catch (Throwable e1) {
                        SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanAction.logMessage2"), e1); //$NON-NLS-1$
                    }
                    return;
                }
                try {
                    st.close();
                } catch (Throwable e) {
                    SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanAction.logMessage2"), e); //$NON-NLS-1$
                }
            }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanAction.logMessage4"), e); //$NON-NLS-1$
        }

        // execute explain plan for all statements

        try {

            while (!queryStrings.isEmpty()) {

                String querySql = (String) queryStrings.remove(0);

                if (querySql != null) {
                    resultDisplayer.addSQLExecution(new DB2ExplainPlanExecution(querySql, runNode));
                }
            }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("DB2ExplainPlanAction.logMessage5"), e); //$NON-NLS-1$
        }
    }

    static String createPlanScript1 = "CREATE TABLE SYSTOOLS.EXPLAIN_INSTANCE ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "EXPLAIN_TIME      TIMESTAMP    NOT NULL, " //$NON-NLS-1$
            + "SOURCE_NAME       VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "SOURCE_SCHEMA     VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "SOURCE_VERSION    VARCHAR(64)  NOT NULL, " //$NON-NLS-1$
            + "EXPLAIN_OPTION    CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "SNAPSHOT_TAKEN    CHAR(1)   NOT NULL, " //$NON-NLS-1$
            + "DB2_VERSION       CHAR(7)   NOT NULL, " //$NON-NLS-1$
            + "SQL_TYPE          CHAR(1)   NOT NULL, " //$NON-NLS-1$
            + "QUERYOPT          INTEGER   NOT NULL, " //$NON-NLS-1$
            + "BLOCK             CHAR(1)   NOT NULL, " //$NON-NLS-1$
            + "ISOLATION         CHAR(2)   NOT NULL, " //$NON-NLS-1$
            + "BUFFPAGE          INTEGER   NOT NULL, " //$NON-NLS-1$
            + "AVG_APPLS         INTEGER   NOT NULL, " //$NON-NLS-1$
            + "SORTHEAP          INTEGER   NOT NULL, " //$NON-NLS-1$
            + "LOCKLIST          INTEGER   NOT NULL, " //$NON-NLS-1$
            + "MAXLOCKS          SMALLINT  NOT NULL, " //$NON-NLS-1$
            + "LOCKS_AVAIL       INTEGER   NOT NULL, " //$NON-NLS-1$
            + "CPU_SPEED         DOUBLE    NOT NULL, " //$NON-NLS-1$
            + "REMARKS           VARCHAR(254), " //$NON-NLS-1$
            + "DBHEAP            INTEGER   NOT NULL, " //$NON-NLS-1$
            + "COMM_SPEED        DOUBLE    NOT NULL, " //$NON-NLS-1$
            + "PARALLELISM       CHAR(2)   NOT NULL, " //$NON-NLS-1$
            + "DATAJOINER        CHAR(1)   NOT NULL, " //$NON-NLS-1$
            + "PRIMARY KEY (EXPLAIN_REQUESTER, EXPLAIN_TIME, SOURCE_NAME,  SOURCE_SCHEMA, SOURCE_VERSION));"; //$NON-NLS-1$

    static String createPlanScript2 = "CREATE TABLE SYSTOOLS.EXPLAIN_STATEMENT ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "EXPLAIN_TIME      TIMESTAMP    NOT NULL, " //$NON-NLS-1$
            + "SOURCE_NAME       VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "SOURCE_SCHEMA     VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + " SOURCE_VERSION    VARCHAR(64)  NOT NULL, " //$NON-NLS-1$
            + " EXPLAIN_LEVEL     CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "  STMTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "  SECTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "  QUERYNO           INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                      QUERYTAG          CHAR(20)     NOT NULL, " //$NON-NLS-1$
            + "                      STATEMENT_TYPE    CHAR(2)      NOT NULL, " //$NON-NLS-1$
            + "                      UPDATABLE         CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                      DELETABLE         CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                      TOTAL_COST        DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                      STATEMENT_TEXT    CLOB(2M)     NOT NULL NOT LOGGED, " //$NON-NLS-1$
            + "                      SNAPSHOT          BLOB(10M)    NOT LOGGED, " //$NON-NLS-1$
            + "                      QUERY_DEGREE      INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                         PRIMARY KEY (EXPLAIN_REQUESTER, " //$NON-NLS-1$
            + "                                      EXPLAIN_TIME, " //$NON-NLS-1$
            + "                                      SOURCE_NAME, " //$NON-NLS-1$
            + "                                      SOURCE_SCHEMA, " //$NON-NLS-1$
            + "                                   SOURCE_VERSION, " //$NON-NLS-1$
            + "                                      EXPLAIN_LEVEL, " //$NON-NLS-1$
            + "                                      STMTNO, " //$NON-NLS-1$
            + "                                      SECTNO), " //$NON-NLS-1$
            + "                          FOREIGN KEY (EXPLAIN_REQUESTER, " //$NON-NLS-1$
            + "                                       EXPLAIN_TIME, " //$NON-NLS-1$
            + "                                       SOURCE_NAME, " //$NON-NLS-1$
            + "                                       SOURCE_SCHEMA, " //$NON-NLS-1$
            + "                                       SOURCE_VERSION) " //$NON-NLS-1$
            + "                          REFERENCES SYSTOOLS.EXPLAIN_INSTANCE " //$NON-NLS-1$
            + "                          ON DELETE CASCADE);"; //$NON-NLS-1$

    static String createPlanScript3 = "CREATE TABLE SYSTOOLS.EXPLAIN_ARGUMENT ( EXPLAIN_REQUESTER   VARCHAR(128)  NOT NULL, " //$NON-NLS-1$
            + "       EXPLAIN_TIME        TIMESTAMP     NOT NULL, " //$NON-NLS-1$
            + "                     SOURCE_NAME         VARCHAR(128)  NOT NULL, " //$NON-NLS-1$
            + "                     SOURCE_SCHEMA       VARCHAR(128)  NOT NULL, " //$NON-NLS-1$
            + "                     SOURCE_VERSION      VARCHAR(64)   NOT NULL, " //$NON-NLS-1$
            + "                     EXPLAIN_LEVEL       CHAR(1)       NOT NULL, " //$NON-NLS-1$
            + "                     STMTNO              INTEGER       NOT NULL, " //$NON-NLS-1$
            + "                     SECTNO              INTEGER       NOT NULL, " //$NON-NLS-1$
            + "                     OPERATOR_ID         INTEGER       NOT NULL, " //$NON-NLS-1$
            + "                     ARGUMENT_TYPE       CHAR(8)       NOT NULL, " //$NON-NLS-1$
            + "                     ARGUMENT_VALUE      VARCHAR(1024), " //$NON-NLS-1$
            + "                     LONG_ARGUMENT_VALUE CLOB(2M)      NOT LOGGED, " //$NON-NLS-1$
            + "                        FOREIGN KEY (EXPLAIN_REQUESTER, " //$NON-NLS-1$
            + "                                     EXPLAIN_TIME, " //$NON-NLS-1$
            + "                                     SOURCE_NAME, " //$NON-NLS-1$
            + "                                     SOURCE_SCHEMA, " //$NON-NLS-1$
            + "                                     SOURCE_VERSION, " //$NON-NLS-1$
            + "                                     EXPLAIN_LEVEL, " //$NON-NLS-1$
            + "                                     STMTNO, " //$NON-NLS-1$
            + "                                     SECTNO) " //$NON-NLS-1$
            + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT " //$NON-NLS-1$
            + "                        ON DELETE CASCADE);"; //$NON-NLS-1$

    static String createPlanScript4 = "CREATE TABLE SYSTOOLS.EXPLAIN_OBJECT ( EXPLAIN_REQUESTER    VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                    EXPLAIN_TIME         TIMESTAMP    NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_NAME          VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_SCHEMA        VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_VERSION       VARCHAR(64)  NOT NULL, " //$NON-NLS-1$
            + "                   EXPLAIN_LEVEL        CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                   STMTNO               INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   SECTNO               INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   OBJECT_SCHEMA        VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                   OBJECT_NAME          VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                   OBJECT_TYPE          CHAR(2)      NOT NULL, " //$NON-NLS-1$
            + "                   CREATE_TIME          TIMESTAMP, " //$NON-NLS-1$
            + "                   STATISTICS_TIME      TIMESTAMP, " //$NON-NLS-1$
            + "                   COLUMN_COUNT         SMALLINT     NOT NULL, " //$NON-NLS-1$
            + "                   ROW_COUNT            BIGINT       NOT NULL, " //$NON-NLS-1$
            + "                   WIDTH                INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   PAGES                INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   DISTINCT             CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                   TABLESPACE_NAME      VARCHAR(128), " //$NON-NLS-1$
            + "                   OVERHEAD             DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                   TRANSFER_RATE        DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                   PREFETCHSIZE         INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   EXTENTSIZE           INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   CLUSTER              DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                   NLEAF                INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   NLEVELS              INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   FULLKEYCARD          BIGINT       NOT NULL, " //$NON-NLS-1$
            + "                   OVERFLOW             INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   FIRSTKEYCARD         BIGINT       NOT NULL, " //$NON-NLS-1$
            + "                   FIRST2KEYCARD        BIGINT       NOT NULL, " //$NON-NLS-1$
            + "                   FIRST3KEYCARD        BIGINT       NOT NULL, " //$NON-NLS-1$
            + "                   FIRST4KEYCARD        BIGINT       NOT NULL, " //$NON-NLS-1$
            + "                   SEQUENTIAL_PAGES     INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   DENSITY              INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   STATS_SRC            CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                   AVERAGE_SEQUENCE_GAP          DOUBLE  NOT NULL, " //$NON-NLS-1$
            + "                   AVERAGE_SEQUENCE_FETCH_GAP    DOUBLE  NOT NULL, " //$NON-NLS-1$
            + "                   AVERAGE_SEQUENCE_PAGES        DOUBLE  NOT NULL, " //$NON-NLS-1$
            + "                   AVERAGE_SEQUENCE_FETCH_PAGES  DOUBLE  NOT NULL, " //$NON-NLS-1$
            + "                   AVERAGE_RANDOM_PAGES          DOUBLE  NOT NULL, " //$NON-NLS-1$
            + "                   AVERAGE_RANDOM_FETCH_PAGES    DOUBLE  NOT NULL, " //$NON-NLS-1$
            + "                   NUMRIDS                       BIGINT  NOT NULL, " //$NON-NLS-1$
            + "                   NUMRIDS_DELETED               BIGINT  NOT NULL, " //$NON-NLS-1$
            + "                   NUM_EMPTY_LEAFS               BIGINT  NOT NULL, " //$NON-NLS-1$
            + "                   ACTIVE_BLOCKS                 BIGINT  NOT NULL, " //$NON-NLS-1$
            + "                        FOREIGN KEY (EXPLAIN_REQUESTER, " //$NON-NLS-1$
            + "                                     EXPLAIN_TIME, " //$NON-NLS-1$
            + "                                     SOURCE_NAME, " //$NON-NLS-1$
            + "                                     SOURCE_SCHEMA, " //$NON-NLS-1$
            + "                                     SOURCE_VERSION, " //$NON-NLS-1$
            + "                                     EXPLAIN_LEVEL, " //$NON-NLS-1$
            + "                                     STMTNO, " //$NON-NLS-1$
            + "                                     SECTNO) " //$NON-NLS-1$
            + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT " //$NON-NLS-1$
            + "                        ON DELETE CASCADE);"; //$NON-NLS-1$

    static String createPlanScript5 = "CREATE TABLE SYSTOOLS.EXPLAIN_OPERATOR ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                     EXPLAIN_TIME      TIMESTAMP    NOT NULL, " //$NON-NLS-1$
            + "                     SOURCE_NAME       VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                     SOURCE_SCHEMA     VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                     SOURCE_VERSION    VARCHAR(64)  NOT NULL, " //$NON-NLS-1$
            + "                     EXPLAIN_LEVEL     CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                     STMTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                     SECTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                     OPERATOR_ID       INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                     OPERATOR_TYPE     CHAR(6)      NOT NULL, " //$NON-NLS-1$
            + "                     TOTAL_COST        DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     IO_COST           DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     CPU_COST          DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     FIRST_ROW_COST    DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     RE_TOTAL_COST     DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     RE_IO_COST        DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     RE_CPU_COST       DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     COMM_COST         DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     FIRST_COMM_COST   DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     BUFFERS           DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     REMOTE_TOTAL_COST DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                     REMOTE_COMM_COST  DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                        FOREIGN KEY (EXPLAIN_REQUESTER, " //$NON-NLS-1$
            + "                                     EXPLAIN_TIME, " //$NON-NLS-1$
            + "                                     SOURCE_NAME, " //$NON-NLS-1$
            + "                                     SOURCE_SCHEMA, " //$NON-NLS-1$
            + "                                     SOURCE_VERSION, " //$NON-NLS-1$
            + "                                     EXPLAIN_LEVEL, " //$NON-NLS-1$
            + "                                     STMTNO, " //$NON-NLS-1$
            + "                                     SECTNO) " //$NON-NLS-1$
            + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT " //$NON-NLS-1$
            + "                        ON DELETE CASCADE);"; //$NON-NLS-1$

    static String createPlanScript6 = "CREATE TABLE SYSTOOLS.EXPLAIN_PREDICATE ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                      EXPLAIN_TIME      TIMESTAMP    NOT NULL, " //$NON-NLS-1$
            + "                      SOURCE_NAME       VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                      SOURCE_SCHEMA     VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                      SOURCE_VERSION    VARCHAR(64)  NOT NULL, " //$NON-NLS-1$
            + "                      EXPLAIN_LEVEL     CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                      STMTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                      SECTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                      OPERATOR_ID       INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                      PREDICATE_ID      INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                      HOW_APPLIED       CHAR(5)      NOT NULL, " //$NON-NLS-1$
            + "                      WHEN_EVALUATED    CHAR(3)      NOT NULL, " //$NON-NLS-1$
            + "                      RELOP_TYPE        CHAR(2)      NOT NULL, " //$NON-NLS-1$
            + "                      SUBQUERY          CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                      FILTER_FACTOR     DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                      PREDICATE_TEXT    CLOB(2M)     NOT LOGGED, " //$NON-NLS-1$
            + "                        FOREIGN KEY (EXPLAIN_REQUESTER, " //$NON-NLS-1$
            + "                                     EXPLAIN_TIME, " //$NON-NLS-1$
            + "                                     SOURCE_NAME, " //$NON-NLS-1$
            + "                                     SOURCE_SCHEMA, " //$NON-NLS-1$
            + "                                     SOURCE_VERSION, " //$NON-NLS-1$
            + "                                     EXPLAIN_LEVEL, " //$NON-NLS-1$
            + "                                     STMTNO, " //$NON-NLS-1$
            + "                                     SECTNO) " //$NON-NLS-1$
            + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT " //$NON-NLS-1$
            + "                        ON DELETE CASCADE);"; //$NON-NLS-1$

    static String createPlanScript7 = "CREATE TABLE SYSTOOLS.EXPLAIN_STREAM ( EXPLAIN_REQUESTER VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                     EXPLAIN_TIME      TIMESTAMP    NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_NAME       VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_SCHEMA     VARCHAR(128) NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_VERSION    VARCHAR(64)  NOT NULL, " //$NON-NLS-1$
            + "                   EXPLAIN_LEVEL     CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                   STMTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   SECTNO            INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   STREAM_ID         INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_TYPE       CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                   SOURCE_ID         INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   TARGET_TYPE       CHAR(1)      NOT NULL, " //$NON-NLS-1$
            + "                   TARGET_ID         INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   OBJECT_SCHEMA     VARCHAR(128), " //$NON-NLS-1$
            + "                   OBJECT_NAME       VARCHAR(128), " //$NON-NLS-1$
            + "                   STREAM_COUNT      DOUBLE       NOT NULL, " //$NON-NLS-1$
            + "                   COLUMN_COUNT      SMALLINT     NOT NULL, " //$NON-NLS-1$
            + "                   PREDICATE_ID      INTEGER      NOT NULL, " //$NON-NLS-1$
            + "                   COLUMN_NAMES      CLOB(2M)     NOT LOGGED, " //$NON-NLS-1$
            + "                    PMID              SMALLINT     NOT NULL, " //$NON-NLS-1$
            + "               SINGLE_NODE       CHAR(5), " //$NON-NLS-1$
            + "                   PARTITION_COLUMNS CLOB(2M)     NOT LOGGED, " //$NON-NLS-1$
            + "                        FOREIGN KEY (EXPLAIN_REQUESTER, " //$NON-NLS-1$
            + "                                     EXPLAIN_TIME, " //$NON-NLS-1$
            + "                                    SOURCE_NAME, " //$NON-NLS-1$
            + "                                 SOURCE_SCHEMA, " //$NON-NLS-1$
            + "                                     SOURCE_VERSION, " //$NON-NLS-1$
            + "                                     EXPLAIN_LEVEL, " //$NON-NLS-1$
            + "                                     STMTNO, " //$NON-NLS-1$
            + "                                     SECTNO) " //$NON-NLS-1$
            + "                        REFERENCES SYSTOOLS.EXPLAIN_STATEMENT " //$NON-NLS-1$
            + "                        ON DELETE CASCADE);"; //$NON-NLS-1$

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
