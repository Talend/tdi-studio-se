// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.maven;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.login.AbstractLoginTask;

/**
 * 
 * DOC ggu class global comment. Detailled comment
 */
public class CleanMavenFilesLoginTask extends AbstractLoginTask implements IRunnableWithProgress {

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
//
//            @Override
//            public void run(IProgressMonitor monitor) {
//                try {
//                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
//                        IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
//                                IRunProcessService.class);
//                        ITalendProcessJavaProject talendProcessJavaProject = service.getTalendProcessJavaProject();
//                        if (talendProcessJavaProject != null) {
//                            talendProcessJavaProject.cleanMavenFiles(monitor);
//                        }
//                    }
//                } catch (Exception e) {
//                    ExceptionHandler.process(e);
//                }
//            }
//        };
//
//        try {
//            IWorkspace workspace = ResourcesPlugin.getWorkspace();
//            workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
//        } catch (CoreException e) {
//            throw new InvocationTargetException(e);
//        }

    }

}
