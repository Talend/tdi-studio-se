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

import java.util.ArrayList;
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
import org.talend.designer.components.exchange.ExchangePlugin;
import org.talend.designer.components.exchange.i18n.Messages;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.util.ExchangeUtils;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class RefreshJob extends Job {

    public RefreshJob() {
        super(Messages.getString("RefreshJob.FindExtensions.Title")); //$NON-NLS-1$
    }

    private List<ComponentExtension> fAvailableExtensions;

    @Override
    public IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("RefreshJob.FindExtensions.Message"), IProgressMonitor.UNKNOWN);

        // run in another thread, make it possible to stop the remote procedure call when user press cancel
        // button
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<ComponentExtension>> task = executor.submit(new Callable<List<ComponentExtension>>() {

            @Override
            public List<ComponentExtension> call() throws Exception {
                List<ComponentExtension> extensions = new ArrayList<ComponentExtension>();
                extensions = ComponentSearcher.getAvailableComponentExtensions(ExchangeUtils.TYPEEXTENSION,
                        ExchangePlugin.getStudioVersion(), ExchangeUtils.getCurrentLanguage(), "");//$NON-NLS-1$
                if (extensions == null || extensions.isEmpty() || extensions.size() == 0) {
                    extensions = ComponentSearcher.getAvailableComponentExtensions(ExchangeUtils.TYPEEXTENSION, "5.6.1",//$NON-NLS-1$
                            ExchangeUtils.getCurrentLanguage(), "");//$NON-NLS-1$
                }
                return extensions;
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
                    fAvailableExtensions = task.get();
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

    public List<ComponentExtension> getAvailableExtensions() {
        return fAvailableExtensions;
    }

}
