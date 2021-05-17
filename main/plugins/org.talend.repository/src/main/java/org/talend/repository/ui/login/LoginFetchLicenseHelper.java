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
package org.talend.repository.ui.login;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionMessageDialog;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.service.IRemoteService;
import org.talend.repository.i18n.Messages;
import org.talend.utils.json.JSONObject;

/**
 * created by hcyi on Jan 16, 2019
 * Detailled comment
 *
 */
public class LoginFetchLicenseHelper {

    private static LoginFetchLicenseHelper instance;

    private LoginHelper loginHelper;

    private Map<Project, Job> fetchLicenseJobMap;

    private IRemoteService remoteService;

    public static LoginFetchLicenseHelper getInstance() {
        if (instance == null) {
            instance = new LoginFetchLicenseHelper();
        }
        return instance;
    }

    private LoginFetchLicenseHelper() {
        loginHelper = LoginHelper.getInstance();
        fetchLicenseJobMap = new HashMap<Project, Job>();
    }

    public IRemoteService getRemoteService() {
        if (remoteService == null) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IRemoteService.class)) {
                remoteService = (IRemoteService) GlobalServiceRegister.getDefault().getService(IRemoteService.class);
            }
        }
        return remoteService;
    }

    public String getAdminURL() {
        return LoginHelper.getAdminURL(loginHelper.getCurrentSelectedConnBean());
    }

    public void fetchLicenseIfNeeded(Project proj) {
        if (LoginHelper.isRemotesConnection(loginHelper.getCurrentSelectedConnBean())) {
            fetchLicense(proj);
        }
    }

    public Job fetchLicense(Project proj) {
        String url = getAdminURL();
        String userId = loginHelper.getCurrentSelectedConnBean().getUser();
        String key = loginHelper.getLicenseMapKey(url, proj.getLabel(), userId);
        String license = null;
        try {
            license = loginHelper.getLicense(key);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        Job fetchJob = null;
        if (license == null || license.isEmpty()) {
            fetchJob = fetchLicenseJobMap.get(proj);
            boolean createJob = true;
            if (fetchJob != null) {
                if (fetchJob.getResult() == null) {
                    // just wait finish, click refresh may clear all running jobs
                    createJob = false;
                } else {
                    createJob = true;
                }
            }
            if (createJob) {
                fetchJob = createFetchLicenseJob(proj);
                fetchJob.setUser(false);
                fetchJob.schedule();
            }
        }
        return fetchJob;
    }

    private Job createFetchLicenseJob(Project proj) {
        final String projLabel = proj.getLabel();
        Job fetchJob = new Job(Messages.getString("LoginProjectPage.fetchLicense.job", proj.getLabel())) { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                ConnectionBean cBean = loginHelper.getCurrentSelectedConnBean();
                try {
                    String userId = cBean.getUser();
                    String url = getAdminURL();
                    JSONObject jsonObj = getRemoteService().getLicenseKey(userId, cBean.getPassword(), url, projLabel);
                    String fetchedLicense = jsonObj.getString("customerName") + "_" + jsonObj.getString("licenseKey"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    String key = loginHelper.getLicenseMapKey(url, projLabel, userId);
                    loginHelper.putLicense(key, fetchedLicense);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                return Status.OK_STATUS;
            }

            @Override
            protected void canceling() {
                Thread thread = this.getThread();
                try {
                    // to interrupt the slow network connection
                    thread.interrupt();
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        fetchLicenseJobMap.put(proj, fetchJob);
        return fetchJob;
    }

    public void cancelAndClearFetchJobs() {
        for (Job job : fetchLicenseJobMap.values()) {
            job.cancel();
        }
        fetchLicenseJobMap.clear();
    }

    public Map<Project, Job> getFetchLicenseJobMap() {
        return this.fetchLicenseJobMap;
    }

    /**
     * 
     * @return if false: user cancel login
     */
    public boolean refreshLicenseIfNeeded(Project proj) {
        if (CommonsPlugin.isHeadless()) {
            return true;
        }
        ConnectionBean conn = loginHelper.getCurrentSelectedConnBean();
        if (LoginHelper.isRemotesConnection(conn)) {
            String url = getAdminURL();
            String projLabel = proj.getLabel();
            String userId = conn.getUser();
            try {
                String key = loginHelper.getLicenseMapKey(url, projLabel, userId);
                String license = loginHelper.getLicense(key);
                if (license == null || license.isEmpty()) {
                    Job fetchJob = getFetchLicenseJobMap().get(proj);
                    if (fetchJob == null || fetchJob.getResult() != null) {
                        // if result is not null, means fetchJob has already finished but no license fetched
                        fetchJob = fetchLicense(proj);
                    }
                    final Job fJob = fetchJob;
                    if (fJob != null) {
                        final AtomicBoolean isInterupted = new AtomicBoolean(false);
                        final Shell shell = DisplayUtils.getDefaultShell(false);
                        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                        dialog.run(true, true, new IRunnableWithProgress() {

                            @Override
                            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                monitor.setTaskName(fJob.getName());
                                while (true) {
                                    if (monitor.isCanceled()) {
                                        /**
                                         * If network is slow, maybe just wait the fetch job finish, but still can click
                                         * the Refresh button to cancel all fetch jobs
                                         */
                                        // fJob.cancel();

                                        isInterupted.set(true);
                                        break;
                                    }
                                    IStatus result = fJob.getResult();
                                    if (result != null) {
                                        break;
                                    }
                                    try {
                                        Thread.sleep(250);
                                    } catch (Exception e) {
                                        // nothing to do
                                    }
                                }
                            }
                        });
                        if (isInterupted.get()) {
                            return false;
                        }
                    }
                    license = loginHelper.getLicense(key);
                }
                if (license == null || license.isEmpty()) {
                    throw new Exception(Messages.getString("LoginProjectPage.fetchLicense.error.failed")); //$NON-NLS-1$
                }
                // will do save in CoreTisService if needed
                // ICoreTisService tisService = (ICoreTisService) GlobalServiceRegister.getDefault()
                // .getService(ICoreTisService.class);
                // File remoteLicense = tisService.getRemoteLicenseFile();
                // tisService.storeLicenseFile(remoteLicense, license);
            } catch (Exception e) {
                final Shell shell = DisplayUtils.getDefaultShell(false);
                ExceptionMessageDialog.openError(shell, Messages.getString("LoginProjectPage.fetchLicense.error.title"), //$NON-NLS-1$
                        Messages.getString("LoginProjectPage.fetchLicense.error.msg"), e); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }
}
