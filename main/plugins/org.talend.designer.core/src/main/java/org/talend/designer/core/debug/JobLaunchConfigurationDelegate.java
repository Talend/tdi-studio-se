// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;

/**
 * bqian A launch delegate for launching local job. <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class JobLaunchConfigurationDelegate extends org.eclipse.debug.core.model.LaunchConfigurationDelegate {

    /**
     * Convenience method to get the launch manager.
     * 
     * @return the launch manager
     */
    protected ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        return null;
    }

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        String jobId = configuration.getAttribute(TalendDebugUIConstants.JOB_ID, (String) null);
        String jobName = configuration.getAttribute(TalendDebugUIConstants.JOB_NAME, (String) null);
        String jobVersion = configuration.getAttribute(TalendDebugUIConstants.JOB_VERSION, (String) null);
        // find process from open editor.
        IProcess2 process = DesignerUtilities.findProcessFromEditors(jobId, jobVersion);
        // find process from repository
        if (process == null) {
            process = findProcessFromRepository(jobId, jobVersion);
        }

        if (process == null) {
            ExceptionHandler.log(Messages.getString("JobLaunchConfigurationDelegate.jobNotFound", jobName)); //$NON-NLS-1$
            return;
        }

        final IProcess2 p = process;
        // Run job
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
                service.setActiveProcess(p);
                service.refreshView();
                service.getRunProcessAction().run();
            }
        });

    }

    /**
     * DOC bqian Comment method "findProcessFromRepository".
     * 
     * @param jobName
     * @return
     */
    private IProcess2 findProcessFromRepository(String jobId, String version) {
        try {
            ItemCacheManager.clearCache();
            ProcessItem processItem = ItemCacheManager.getProcessItem(jobId, version);
            if (processItem != null) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
                    IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                            IDesignerCoreService.class);
                    IProcess process = service.getProcessFromItem(processItem);
                    if (process instanceof IProcess2) {
                        return (IProcess2) process;
                    }
                }
                // keep old code for now, but it should never be called.
                ProcessEditorInput fileEditorInput = new ProcessEditorInput(processItem, true, true, true);
                IProcess2 process = fileEditorInput.getLoadedProcess();
                return process;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }
}
