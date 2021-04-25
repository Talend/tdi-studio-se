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
package org.talend.designer.runprocess;

import java.beans.PropertyChangeListener;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.commons.utils.workbench.extensions.ExtensionImplementationProvider;
import org.talend.commons.utils.workbench.extensions.ExtensionPointLimiterImpl;
import org.talend.commons.utils.workbench.extensions.IExtensionPointLimiter;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.runprocess.maven.listener.ProcessChangeListener;
import org.talend.repository.model.IRepositoryService;

/**
 * The activator class controls the plug-in life cycle.
 */
public class RunProcessPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.designer.runprocess"; //$NON-NLS-1$

    // The shared instance
    private static RunProcessPlugin plugin;

    /** The running process contexts manager. */
    private RunProcessContextManager runProcessContextManager;

    private ProjectPreferenceManager projectPreferenceManager;

    private PropertyChangeListener processChangeListener;

    /**
     * Constructs a new Activator.
     */
    public RunProcessPlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        IExtensionPointLimiter extensionPointLimiter = new ExtensionPointLimiterImpl(
                "org.talend.designer.runprocess.runprocess_manager", "runprocess_manager"); //$NON-NLS-1$ //$NON-NLS-2$

        runProcessContextManager = ExtensionImplementationProvider.getSingleInstance(extensionPointLimiter, null);

        if (runProcessContextManager == null) {
            runProcessContextManager = new RunProcessContextManager();
        }
        processChangeListener = new ProcessChangeListener();
        ProxyRepositoryFactory.getInstance().addPropertyChangeListener(processChangeListener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        ProxyRepositoryFactory.getInstance().removePropertyChangeListener(processChangeListener);
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static RunProcessPlugin getDefault() {
        return plugin;
    }

    /**
     * Getter for runProcessContextManager.
     *
     * @return the runProcessContextManager
     */
    public RunProcessContextManager getRunProcessContextManager() {
        return this.runProcessContextManager;
    }

    public ProjectPreferenceManager getProjectPreferenceManager() {
        if (projectPreferenceManager == null) {
            projectPreferenceManager = new ProjectPreferenceManager(PLUGIN_ID);
        }
        return projectPreferenceManager;
    }

    public IRepositoryService getRepositoryService() {
        IService service = GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        return (IRepositoryService) service;
    }

    /**
     * DOC get a implement of CodeGeneratorService.
     *
     * @return a implement of CodeGeneratorService
     */
    public ICodeGeneratorService getCodeGeneratorService() {
        IService service = GlobalServiceRegister.getDefault().getService(ICodeGeneratorService.class);
        return (ICodeGeneratorService) service;
    }

}
