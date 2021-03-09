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
package org.talend.designer.components.exchange.jobs;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.designer.components.exchange.i18n.Messages;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.util.ExchangeUtils;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class ShowContributedExtensionsJob extends Job {

    private List<ComponentExtension> fContributedExtensions;

    public ShowContributedExtensionsJob() {
        super(Messages.getString("ShowContributedExtensionsJob.ShowContributedExtensions.Title")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("ShowContributedExtensionsJob.ShowContributedExtensions.Message"),
                IProgressMonitor.UNKNOWN);

        // run in another thread, make it possible to stop the remote procedure call when user press cancel
        // button
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<ComponentExtension>> task = executor.submit(new Callable<List<ComponentExtension>>() {

            public List<ComponentExtension> call() throws Exception {
                if (ExchangeUtils.checkUserAndPassword()) {
                    return ComponentSearcher.getContributedExtensions("4.2", ExchangeUtils.getCurrentLanguage(),
                            ExchangeUtils.getUserName(), ExchangeUtils.getPasswordHash());
                }
                return null;
            }
        });

        while (true) {
            try {
                if (monitor.isCanceled()) {
                    // stop the web service call
                    task.cancel(true);
                    return Status.CANCEL_STATUS;
                } else if (task.isDone()) {
                    // web service call complete
                    fContributedExtensions = task.get();
                    break;
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return Status.CANCEL_STATUS;
            } finally {
                executor.shutdown();
            }
        }
        monitor.done();
        return Status.OK_STATUS;
    }

    /**
     * Getter for fContributedExtensions.
     *
     * @return the fContributedExtensions
     */
    public List<ComponentExtension> getfContributedExtensions() {
        return this.fContributedExtensions;
    }
}
