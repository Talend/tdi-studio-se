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
package org.talend.repository.ui.login;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
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
}
