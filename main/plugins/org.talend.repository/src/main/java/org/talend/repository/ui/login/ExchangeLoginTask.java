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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ExchangeUser;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.service.IExchangeService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.login.AbstractLoginTask;
import org.talend.registration.wizards.register.TalendForgeDialog;
import org.talend.repository.ProjectManager;


/**
 * created by nrousseau on Jul 15, 2015
 * Detailled comment
 *
 */
public class ExchangeLoginTask extends AbstractLoginTask  implements IRunnableWithProgress {


    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        if (!PluginChecker.isExchangeSystemLoaded() || TalendPropertiesUtil.isHideExchange()) {
            return;
        }
        Job job = new Job("Check Exchange") { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {        // check for Talendforge
                    IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
                    boolean checkTisVersion = prefStore.getBoolean(ITalendCorePrefConstants.EXCHANGE_CHECK_TIS_VERSION);
                    IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                            IBrandingService.class);
                    if (!checkTisVersion && brandingService.isPoweredbyTalend()) {
                        int count = prefStore.getInt(TalendForgeDialog.LOGINCOUNT);
                        if (count < 0) {
                            count = 1;
                        }
                        ExchangeUser exchangeUser = ProjectManager.getInstance().getCurrentProject().getExchangeUser();
                        boolean isExchangeLogon = exchangeUser.getLogin() != null && !exchangeUser.getLogin().equals(""); //$NON-NLS-1$
                        boolean isUserPassRight = true;
                        if (isExchangeLogon) {
                            IExchangeService service = (IExchangeService) GlobalServiceRegister.getDefault().getService(
                                    IExchangeService.class);
                            if (service.checkUserAndPass(exchangeUser.getUsername(), exchangeUser.getPassword()) != null) {
                                isUserPassRight = false;
                            }
                        }

                        if (!isExchangeLogon || !isUserPassRight) {
                            if ((count + 1) % 4 == 0) {
                                // if (Platform.getOS().equals(Platform.OS_LINUX)) {
                                // TalendForgeDialog tfDialog = new TalendForgeDialog(this.getShell(), project);
                                // tfDialog.open();
                                // } else {
                                Display.getDefault().asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        Project project = ProjectManager.getInstance().getCurrentProject();
                                        String userEmail = null;
                                        if (project.getAuthor() != null) {
                                            userEmail = project.getAuthor().getLogin();
                                        }
                                        TalendForgeDialog tfDialog = new TalendForgeDialog(DisplayUtils.getDefaultShell(), userEmail);
                                        tfDialog.setBlockOnOpen(true);
                                        tfDialog.open();
                                    }

                                });
                            }

                            prefStore.setValue(TalendForgeDialog.LOGINCOUNT, count + 1);
                        }
                    }
                    return org.eclipse.core.runtime.Status.OK_STATUS;
                }
        };
        job.setSystem(true);
        job.setUser(false);
        job.setPriority(Job.INTERACTIVE);
        job.schedule(); // start as soon as possible

    }

}
