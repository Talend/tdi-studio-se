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
package org.talend.designer.codegen.config;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.CommonUIPlugin;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class CodeGeneratorProgressMonitor extends ProgressMonitorWrapper implements IProgressMonitorWithBlocking {

    /**
     * Threshold for how often the event loop is spun, in ms.
     */
    private static int tTHRESH = 100;

    /**
     * Maximum amount of time to spend processing events, in ms.
     */
    private static int tMAX = 50;

    /**
     * Last time the event loop was spun.
     */
    private long lastTime = System.currentTimeMillis();

    /**
     * The task name is the name of the current task in the event loop.
     */
    private String taskName;

    /**
     * Constructs a new instance of the receiver and forwards to monitor.
     *
     * @param monitor
     */
    public CodeGeneratorProgressMonitor(IProgressMonitor monitor) {
        super(monitor);
    }

    /**
     * @see IProgressMonitor#beginTask
     */
    @Override
    public void beginTask(String name, int totalWork) {
        super.beginTask(name, totalWork);
        taskName = name;
        runEventLoop();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
     */
    @Override
    public void clearBlocked() {
        // Run the event loop.
        if (CommonUIPlugin.isFullyHeadless()) {
            return;
        }
        final Display disp = DisplayUtils.getDisplay();

        disp.syncExec(new Runnable() {

            @Override
            public void run() {
                Dialog.getBlockedHandler().clearBlocked();
            }

        });
    }

    /**
     * @see IProgressMonitor#done
     */
    @Override
    public void done() {
        super.done();
        taskName = null;
        runEventLoop();
    }

    /**
     * @see IProgressMonitor#internalWorked
     */
    @Override
    public void internalWorked(double work) {
        super.internalWorked(work);
        runEventLoop();
    }

    /**
     * @see IProgressMonitor#isCanceled
     */
    @Override
    public boolean isCanceled() {
        runEventLoop();
        return super.isCanceled();
    }

    /**
     * Runs an event loop.
     */
    private void runEventLoop() {
        // Only run the event loop so often, as it is expensive on some platforms
        // (namely Motif).
        final long t = System.currentTimeMillis();
        if (t - lastTime < tTHRESH) {
            return;
        }
        lastTime = t;
        // Run the event loop.
        if (CommonUIPlugin.isFullyHeadless()) {
            return;
        }
        final Display disp = DisplayUtils.getDisplay();
        if (disp == null) {
            return;
        }
        disp.syncExec(new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    try {
                        if (!disp.readAndDispatch()) {
                            break;
                        }
                    } catch (SWTException se) {
                        // do nothing;
                    }

                    // Only run the event loop for so long.
                    // Otherwise, this would never return if some other thread was
                    // constantly generating events.
                    if (System.currentTimeMillis() - t > tMAX) {
                        break;
                    }
                }
            }

        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
     */
    @Override
    public void setBlocked(final IStatus reason) {
        // Run the event loop.
        if (CommonUIPlugin.isFullyHeadless()) {
            ExceptionHandler.process(reason.getException());
            return;
        }
        final Display disp = DisplayUtils.getDisplay();

        disp.syncExec(new Runnable() {

            @Override
            public void run() {
                Dialog.getBlockedHandler().showBlocked(CodeGeneratorProgressMonitor.this, reason, taskName);
            }

        });
    }

    /**
     * @see IProgressMonitor#setCanceled
     */
    @Override
    public void setCanceled(boolean b) {
        super.setCanceled(b);
        taskName = null;
        runEventLoop();
    }

    /**
     * @see IProgressMonitor#setTaskName
     */
    @Override
    public void setTaskName(String name) {
        super.setTaskName(name);
        taskName = name;
        runEventLoop();
    }

    /**
     * @see IProgressMonitor#subTask
     */
    @Override
    public void subTask(String name) {
        // Be prepared in case the first task was null
        if (taskName == null) {
            taskName = name;
        }
        super.subTask(name);
        runEventLoop();
    }

    /**
     * @see IProgressMonitor#worked
     */
    @Override
    public void worked(int work) {
        super.worked(work);
        runEventLoop();
    }

    /**
     * Return the name of the current task.
     *
     * @return Returns the taskName.
     */
    protected String getTaskName() {
        return taskName;
    }
}
