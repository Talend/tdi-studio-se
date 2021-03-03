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
import org.talend.designer.components.exchange.util.ExchangeWebService;
import org.talend.designer.components.exchange.util.WebserviceStatus;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class UploadRevisionJob extends Job {

    private ComponentExtension fe;

    private WebserviceStatus ws = null;

    public UploadRevisionJob(ComponentExtension extension) {
        super(Messages.getString("UploadRevisionJob.Title")); //$NON-NLS-1$
        this.fe = extension;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("UploadRevisionJob.Message"), IProgressMonitor.UNKNOWN);

        // run in another thread, make it possible to stop the remote procedure call when user press cancel
        // button
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<WebserviceStatus> task = executor.submit(new Callable<WebserviceStatus>() {

            public WebserviceStatus call() throws Exception {
                WebserviceStatus webserviceStatus;
                if (ExchangeUtils.checkUserAndPassword()) {
                    webserviceStatus = ExchangeWebService.insertionRevisionService(fe.getIdExtension(),
                            ExchangeUtils.TYPEEXTENSION, ExchangeUtils.getUserName(), ExchangeUtils.getPasswordHash(),
                            fe.getLastVersionAvailable(), fe.getListVersionCompatibles(), fe.getFilename(),
                            "application/multipart-formdata", fe.getDescription(), "true");
                } else {
                    webserviceStatus = new WebserviceStatus();
                    webserviceStatus.setMessageException(Messages.getString("Exchange.logon.error"));
                }
                return webserviceStatus;
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
                    ws = task.get();
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
     * Getter for ws.
     *
     * @return the ws
     */
    public WebserviceStatus getWs() {
        return this.ws;
    }

}
