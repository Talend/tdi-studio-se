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
package org.talend.designer.runprocess.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.runprocess.IEclipseProcessor;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ProcessDebugDialog extends Dialog {

    /** The process to be run in debug mode. */
    private IProcess2 process;

    private ProcessContextComposite contextComposite;

    /**
     * Constructs a new ProcessDebugDialog.
     *
     * @param parentShell Parent shell.
     * @param process The process to be run in debug mode.
     */
    public ProcessDebugDialog(Shell parentShell, IProcess2 process) {
        super(parentShell);

        this.process = process;
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);

        newShell.setText(Messages.getString("ProcessDebugDialog.title", process.getLabel())); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages.getString("ProcessDebugDialog.debugBtn"), true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        contextComposite = new ProcessContextComposite(composite, SWT.NONE);
        contextComposite.setProcess(process);
        contextComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        return composite;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (contextComposite.promptConfirmLauch()) {

            final IContext context = contextComposite.getSelectedContext();
            super.okPressed();

            IRunnableWithProgress worker = new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) {
                    IProcessor processor = ProcessorUtilities.getProcessor(process, process.getProperty(), context);
                    monitor.beginTask("Launching debugger", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    try {
                        // use this function to generate childrens also.
                        ProcessorUtilities.generateCode(process, context, false, false, true, monitor);
                        ILaunchConfiguration config = ((IEclipseProcessor) processor).debug();
                        if (config != null) {
                            // PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new
                            // DebugInNewWindowListener());
                            DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
                        } else {
                            MessageDialog.openInformation(getShell(), Messages.getString("ProcessDebugDialog.debugBtn"), Messages //$NON-NLS-1$
                                    .getString("ProcessDebugDialog.errortext")); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } catch (ProcessorException e) {
                        IStatus status = new Status(IStatus.ERROR, RunProcessPlugin.PLUGIN_ID, IStatus.OK,
                                "Debug launch failed.", e); //$NON-NLS-1$
                        RunProcessPlugin.getDefault().getLog().log(status);
                        MessageDialog.openError(getShell(), Messages.getString("ProcessDebugDialog.debugBtn"), //$NON-NLS-1$
                                ""); //$NON-NLS-1$
                    } finally {
                        monitor.done();
                    }
                }
            };

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            try {
                progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), worker, ResourcesPlugin.getWorkspace()
                        .getRoot());
            } catch (InvocationTargetException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }

        }
    }

    // /**
    // * Listener opening the DEBUG perspective in a new window. <br/>
    // *
    // * $Id$
    // *
    // */
    // private static class DebugInNewWindowListener implements IPerspectiveListener {
    //
    // private static final String DEBUG_PERSP_ID = "org.eclipse.debug.ui.DebugPerspective";
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage,
    // * org.eclipse.ui.IPerspectiveDescriptor)
    // */
    // public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    // if (DEBUG_PERSP_ID.equals(perspective.getId())) {
    // try {
    // PlatformUI.getWorkbench().openWorkbenchWindow(DEBUG_PERSP_ID, page.getInput());
    // page.closePerspective(perspective, false, false);
    // } catch (WorkbenchException e) {
    // // Do nothing.
    // } finally {
    // page.getWorkbenchWindow().removePerspectiveListener(this);
    // }
    // }
    //
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
    // * org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
    // */
    // public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
    // // Do nothing
    // }
    // }

}
