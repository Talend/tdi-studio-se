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
package org.talend.sqlbuilder.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.metadata.managment.repository.ManagerConnection;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.ui.SQLBuilderDialog;

/**
 * DOC qianbing class global comment. Detailled comment <br/>
 *
 * $Id: UIUtils.java,v 1.3 2006/11/07 07:37:22 qianbing Exp $
 *
 */
public class UIUtils {

    /**
     * DOC dev UIUtils constructor comment.
     */
    public UIUtils() {
    }

    /**
     * Display a error message if the db connection is failed DOC Comment method "checkConnection".
     *
     * @param parentShell
     * @param imetadataConnection
     */
    public static void checkConnection(final Shell parentShell, IMetadataConnection imetadataConnection) {
        // display a error message if the db connection is failed.
        final ManagerConnection managerConnection = new ManagerConnection();
        managerConnection.check(imetadataConnection, true);
        if (!managerConnection.getIsValide()) {
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    new ErrorDialogWidthDetailArea(parentShell, SqlBuilderPlugin.PLUGIN_ID, Messages
                            .getString("UIUtils.DBConnectionFailure"), //$NON-NLS-1$
                            managerConnection.getMessageException());
                }
            });
        }
    }

    /**
     * Open a error dialog.
     *
     * @param msg String
     * @param e Exception
     */
    public static void openErrorDialog(final String msg, final Exception e) {

        SqlBuilderPlugin.log(msg, e);
        final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                MessageDialog.openError(shell, msg, e.getMessage());
            }
        });
    }

    /**
     * This implementation of IRunnableContext#run(boolean, boolean, IRunnableWithProgress) runs the given
     * <code>IRunnableWithProgress</code> using the progress monitor for this progress dialog and blocks until the
     * runnable has been run, regardless of the value of <code>fork</code>. The dialog is opened before the runnable is
     * run, and closed after it completes. It is recommended that <code>fork</code> is set to true in most cases. If
     * <code>fork</code> is set to <code>false</code>, the runnable will run in the UI thread and it is the runnable's
     * responsibility to call <code>Display.readAndDispatch()</code> to ensure UI responsiveness.
     *
     *
     * <pre>
     * final IRunnableWithProgress r = new IRunnableWithProgress() {
     *
     *     public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
     *         monitor.beginTask(&quot;test task&quot;, 100);
     *         for (int i = 0; i &lt; 100; i++) {
     *             monitor.worked(1);
     *             try {
     *                 Thread.sleep(50);
     *             } catch (Exception e) {
     *             }
     *         }
     *         monitor.done();
     *     }
     * };
     * runWithProgress(r);
     * </pre>
     */
    /**
     * DOC dev Comment method "runWithProgress".
     *
     * @param operation
     * @param fork
     * @param monitor
     * @param shell
     */
    public static void runWithProgress(final IRunnableWithProgress operation, final boolean fork, final IProgressMonitor monitor,
            final Shell shell) {
        final IRunnableWithProgress progress = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                // monitor.beginTask("test task", scale * total);
                // SubProgressMonitor sm = new SubProgressMonitor(monitor, 1 * scale);
                Thread t = new Thread() {

                    @Override
                    public void run() {
                        try {
                            operation.run(monitor);
                        } catch (Exception e) {
                            SqlBuilderPlugin.log(Messages.getString("UIUtils.logMessage1"), e); //$NON-NLS-1$
                        }
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        };

        Runnable run = new Runnable() {

            @Override
            public void run() {
                try {
                    ModalContext.run(progress, fork, monitor, shell.getDisplay());
                } catch (Exception e) {
                    SqlBuilderPlugin.log(Messages.getString("UIUtils.logMessage2"), e); //$NON-NLS-1$
                }
            }
        };

        BusyIndicator.showWhile(Display.getDefault(), run);
    }

    private static Map<String, List<SQLBuilderDialog>> sqlBuilders = new HashMap<String, List<SQLBuilderDialog>>();

    public static void addSqlBuilderDialog(String jobName, SQLBuilderDialog dialog) {
        List<SQLBuilderDialog> list = sqlBuilders.get(jobName);
        if (list == null) {
            list = new ArrayList<SQLBuilderDialog>();
            list.add(dialog);
            sqlBuilders.put(jobName, list);
        } else {
            list.add(dialog);
        }
    }

    public static void closeSqlBuilderDialogs(String jobName) {
        List<SQLBuilderDialog> list = sqlBuilders.get(jobName);
        if (list != null) {
            for (SQLBuilderDialog dialog : list) {
                if (dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed()) {
                    dialog.close();
                }
            }
        }
    }

    public static void updateSqlBuilderDialogTitle(String newLabel, String jobName, String uniqueName) {
        List<SQLBuilderDialog> list = sqlBuilders.get(jobName);
        if (list != null) {
            for (SQLBuilderDialog dialog : list) {
                if (dialog != null && dialog.getShell() != null && !dialog.getShell().isDisposed()) {
                    if (dialog.getShell().getText().contains(uniqueName)) {
                        String title = TextUtil.SQL_BUILDER_TITLE_COMP_MODPREFIX + jobName;
                        title += TextUtil.SQL_BUILDER_TITLE_COMP_NAME + newLabel + "(" + uniqueName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                        dialog.getShell().setText(title);
                    }
                }
            }
        }
    }
}
