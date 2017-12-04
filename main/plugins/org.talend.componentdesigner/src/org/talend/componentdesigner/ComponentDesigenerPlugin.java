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
package org.talend.componentdesigner;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.componentdesigner.manager.ComponentProjectManager;

/**
 * The activator class controls the plug-in life cycle.
 */
public class ComponentDesigenerPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.componentdesigner"; //$NON-NLS-1$

    // The shared instance
    private static ComponentDesigenerPlugin plugin;

    /**
     * The constructor.
     */
    public ComponentDesigenerPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static ComponentDesigenerPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public void setUsed(boolean isUsed) {
        this.getPreferenceStore().setValue(PluginConstant.PROJECTCREATED_FLAG, isUsed);

    }

    public boolean isUsed() {
        return this.getPreferenceStore().getBoolean(PluginConstant.PROJECTCREATED_FLAG);
    }

    public void checkProject() {
        if (!ComponentDesigenerPlugin.getDefault().isUsed()) {
            this.creatComponentProj();
        }
    }

    public void creatComponentProj() {
        ComponentProjectManager manager = ComponentProjectManager.getInstance();
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        manager.createNewProject(this.getPreferenceStore().getString(PluginConstant.PROJECT_URL),
                PluginConstant.COMPONENT_PROJECT, shell);
        ComponentDesigenerPlugin.getDefault().setUsed(true);
    }

    public void creatComponentProj(String directory) {
        ComponentProjectManager manager = ComponentProjectManager.getInstance();
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        IProject project = manager.createNewProject(directory, PluginConstant.COMPONENT_PROJECT, shell);
        manager.configProject(project, shell);
        ComponentDesigenerPlugin.getDefault().setUsed(true);
    }
}
