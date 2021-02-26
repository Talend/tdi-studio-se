// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sqlbuilder.ui;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWithDetailAreaAndContinueButton;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.ConfigureConnParamDialog;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.progress.OpenSQLBuilderDialogProgress;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * Open SqlBuilderDialog Job.
 *
 * @author qzhang
 *
 */
public class OpenSQLBuilderDialogJob extends Job {

    private SQLBuilderRepositoryNodeManager manager = new SQLBuilderRepositoryNodeManager();

    private ConnectionParameters connectionParameters;

    private static String id = SqlBuilderPlugin.PLUGIN_ID;

    private OpenSQLBuilderDialogProgress loginProgress;

    private Composite composite;

    private IElement elem;

    private String propertyName;

    private CommandStack commandStack;

    private AbstractElementPropertySectionController controller;

    private static final Logger log = Logger.getLogger(OpenSQLBuilderDialogJob.class);

    /**
     * DOC dev OpenDialogJob constructor comment.
     *
     * @param connectionParameters
     * @param composite
     * @param elem
     * @param propertyName
     * @param commandStack
     * @param controller
     */
    public OpenSQLBuilderDialogJob(ConnectionParameters connectionParameters, Composite composite, IElement elem,
            String propertyName, CommandStack commandStack, AbstractElementPropertySectionController controller) {
        super(Messages.getString("OpenSQLBuilderDialogJob.openSqlbuilderDialog")); //$NON-NLS-1$
        this.connectionParameters = connectionParameters;

        this.composite = composite;
        this.commandStack = commandStack;
        this.elem = elem;
        this.propertyName = propertyName;
        this.controller = controller;
    }

    /**
     * DOC dev OpenDialogJob constructor comment.
     */
    public OpenSQLBuilderDialogJob(ConnectionParameters connectionParameters) {
        super(Messages.getString("OpenSQLBuilderDialogJob.openSqlbuilderDialog")); //$NON-NLS-1$
        this.connectionParameters = connectionParameters;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        log.info("start to open dialog...");
        loginProgress = new OpenSQLBuilderDialogProgress(connectionParameters, manager, composite);
        Object obj = controller.getDynamicProperty().getPart();
        Process p = null;
        if (obj instanceof IMultiPageTalendEditor) {
            p = (Process) ((IMultiPageTalendEditor) obj).getProcess();
        } else {
            throw new RuntimeException(Messages.getString("OpenSQLBuilderDialogJob.typeRequried")); //$NON-NLS-1$
        }
        final Process process = p;
        try {
            loginProgress.run(monitor);
            log.info("loginProgress done");
            if (EDatabaseTypeName.ACCESS.getDisplayName().equals(connectionParameters.getDbType())
                    || connectionParameters.isStatus()) {
                Display.getDefault().asyncExec(new Runnable() {

                    public void run() {
                        try {
                            Shell parentShell = DisplayUtils.getDefaultShell(false);
                            if (elem instanceof Node) {
                                TextUtil.setDialogTitle(process.getName(), (String) ((Node) elem).getElementParameter("LABEL") //$NON-NLS-1$
                                        .getValue(), elem.getElementName());
                            } else {
                                TextUtil.setDialogTitle(process.getName(), null, null);
                            }
                            log.info("SQLBuilderDialog creating");
                            SQLBuilderDialog dial = new SQLBuilderDialog(parentShell);
                            dial.setBlockOnOpen(true);
                            UIUtils.addSqlBuilderDialog(process.getName(), dial);

                            dial.setConnParameters(connectionParameters);
                            if (Window.OK == dial.open()) {
                                log.info("SQLBuilderDialog opened");
                                if (!composite.isDisposed() && !connectionParameters.isNodeReadOnly()) {
                                    if (EParameterFieldType.DBTABLE.equals(connectionParameters.getFieldType())) {
                                        // final String selectDBTable = connectionParameters.getSelectDBTable();
                                        // if (selectDBTable != null) {
                                        // Command cmd = new PropertyChangeCommand(elem, propertyName, TalendTextUtils
                                        // .addSQLQuotes(selectDBTable));
                                        // commandStack.execute(cmd);
                                        // }
                                    } else {
                                        String sql = connectionParameters.getQuery();
                                        sql = QueryUtil.checkAndAddQuotes(sql);
                                        PropertyChangeCommand cmd = new PropertyChangeCommand(elem, propertyName, sql);
                                        cmd.setUpdate(true);
                                        commandStack.execute(cmd);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error(e);
                            ExceptionHandler.process(e);
                        }
                    }

                });
            } else {
                Display.getDefault().asyncExec(new Runnable() {

                    public void run() {
                        String pid = SqlBuilderPlugin.PLUGIN_ID;
                        String mainMsg = Messages.getString("ConnectionError.Message"); //$NON-NLS-1$
                        if (new ErrorDialogWithDetailAreaAndContinueButton(composite.getShell(), pid, mainMsg,
                                connectionParameters.getConnectionComment()).getCodeOfButton() == Dialog.OK) {
                            ConfigureConnParamDialog paramDialog = new ConfigureConnParamDialog(composite.getShell(),
                                    connectionParameters, process.getContextManager(), elem);
                            if (paramDialog.open() == Window.OK) {
                                controller.openSqlBuilderBuildIn(connectionParameters, propertyName);
                            }
                        }
                    }
                });

            }
        } catch (InterruptedException ie) {
            loginProgress.cleanUp();
            return new Status(IStatus.CANCEL, id, IStatus.CANCEL, "Progress.OpenSqlBuilderDialog.Cancelled", null); //$NON-NLS-1$
        } catch (Exception e) {
            loginProgress.cleanUp();
            return new Status(IStatus.ERROR, id, IStatus.CANCEL, "Progress.OpenSqlBuilderDialog.Error", e); //$NON-NLS-1$
        }
        return new Status(IStatus.OK, id, IStatus.OK, "tested ok ", null); //$NON-NLS-1$
    }
}
