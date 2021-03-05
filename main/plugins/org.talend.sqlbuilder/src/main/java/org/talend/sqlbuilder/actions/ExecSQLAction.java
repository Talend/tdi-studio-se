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
package org.talend.sqlbuilder.actions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.SessionTreeNodeManager;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;
import org.talend.sqlbuilder.sqlcontrol.SQLExecution;
import org.talend.sqlbuilder.ui.editor.ISQLEditor;
import org.talend.sqlbuilder.util.ImageUtil;
import org.talend.sqlbuilder.util.QueryTokenizer;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * This class is used for executing sql query. <br/>
 *
 * $Id: ExecSQLAction.java,v 1.21 2006/11/03 03:19:05 qiang.zhang Exp $
 *
 */
public class ExecSQLAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.ExecSQLIcon"); //$NON-NLS-1$

    private IResultDisplayer resultViewer = null;

    private Preferences prefs = SqlBuilderPlugin.getDefault().getPluginPreferences();

    private int max = Integer.parseInt(prefs.getString(IConstants.WARN_RESEULTS));

    private String queryDelimiter = prefs.getString(IConstants.QUERY_DELIMITER);

    private String alternateDelimiter = prefs.getString(IConstants.ALTERNATE_DELIMITER);

    private String commentDelimiter = prefs.getString(IConstants.COMMENT_DELIMITER);

    /**
     * ExecSQLAction constructor.
     *
     * @param resultViewer
     */
    public ExecSQLAction(IResultDisplayer resultViewer, ISQLEditor editor) {
        this.resultViewer = resultViewer;
        this.editor = editor;
        setActionDefinitionId(ICommandIds.EXEC_SQL);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#getImageDescriptor()
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return img;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getText()
     */
    @Override
    public String getText() {
        return Messages.getString("SQLEditor.Actions.Execute"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getToolTipText()
     */
    @Override
    public String getToolTipText() {
        return Messages.getString("SQLEditor.Actions.Execute.ToolTip"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    @Override
    public void run() {
        int maxresults = max;
        try {
            boolean isLimit = editor.getIfLimit();
            if (isLimit) {
                String tmp = editor.getMaxResult();
                if (tmp != null && tmp.trim().length() != 0) {
                    maxresults = Integer.parseInt(tmp);
                }
                if (maxresults < 0) {
                    new Exception(Messages.getString("SQLEditor.LimitRows.Error")); //$NON-NLS-1$
                }
            }
        } catch (final Exception e) {
            UIUtils.openErrorDialog(Messages.getString("SQLEditor.Error.InvalidRowLimit.Title"), e); //$NON-NLS-1$
            return;
        }
        try {
            if (maxresults > max) {
                final int largeResults = maxresults;
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        boolean okToExecute = MessageDialog.openConfirm(Display.getDefault().getShells()[0],
                                Messages.getString("SQLEditor.LimitRows.ConfirmNoLimit.Title"), Messages //$NON-NLS-1$
                                        .getString("SQLEditor.LimitRows.ConfirmNoLimit.Message")); //$NON-NLS-1$
                        if (okToExecute) {
                            ExecSQLAction.this.run(largeResults);
                        }
                    }
                });
            } else {
                run(maxresults);
            }
        } catch (final Exception e) {
            UIUtils.openErrorDialog(Messages.getString("SQLResultsView.Error.Title"), e); //$NON-NLS-1$
        }
    }

    /**
     * DOC qianbing Comment method "run". Processes the database operation.
     *
     * @param maxRows
     */
    protected void run(int maxRows) {
        RepositoryNode node = editor.getRepositoryNode();
        SessionTreeNodeManager nodeManager = new SessionTreeNodeManager();
        SessionTreeNode runNode = null;

        try {
            ConnectionContextHelper.setSqlBuilderDialogShell(editor.getDialog().getShell());
            runNode = nodeManager.getSessionTreeNode(node, editor.getDialog().getSelectedContext());
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
            MessageDialog.openError(null, Messages.getString("AbstractSQLExecution.Executing.Error"), e.getMessage()); //$NON-NLS-1$
            SqlBuilderPlugin.log(Messages.getString("ExecSQLAction.logMessageGetSessionTreeNodeFail"), e); //$NON-NLS-1$
            return;
        }

        QueryTokenizer qt = new QueryTokenizer(editor.getSQLToBeExecuted(), queryDelimiter, alternateDelimiter, commentDelimiter);

        List<String> queryStrings = new ArrayList<String>();
        while (qt.hasQuery()) {
            String querySql = qt.nextQuery();
            // ignore commented lines.
            if (!querySql.startsWith("--")) { //$NON-NLS-1$
                queryStrings.add(querySql);
            }
        }
        boolean executed = false;
        try {
            // Diaplay data in sqlResult Composites
            while (!queryStrings.isEmpty()) {
                String querySql = queryStrings.remove(0);
                if (querySql != null) {
                    executed = true;
                    SQLExecution sqlExe = new SQLExecution(querySql, maxRows, runNode);
                    resultViewer.addSQLExecution(sqlExe);
                    // editor.setSQLRunTime(sqlExe.getSQLResult().getExecutionTimeMillis());
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
            SqlBuilderPlugin.log(Messages.getString("ExecSQLAction.logMessageErrorCreatingSqlTab"), e); //$NON-NLS-1$
        } finally {
            if (runNode != null && !executed) {
                DriverShim wapperDriver = runNode.getWapperDriver();
                String dbType = runNode.getDatabaseConnection().getDatabaseType();
                String driverClassName = runNode.getDatabaseConnection().getDriverClass();
                if (wapperDriver != null && MetadataConnectionUtils.isDerbyRelatedDb(driverClassName, dbType)) {
                    try {
                        wapperDriver.connect("jdbc:derby:;shutdown=true", null); //$NON-NLS-1$
                    } catch (SQLException e) {
                        // exception of shutdown success. no need to catch.
                    }
                }
            }
        }
    }
}
