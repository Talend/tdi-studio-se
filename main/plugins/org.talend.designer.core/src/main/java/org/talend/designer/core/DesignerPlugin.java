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
package org.talend.designer.core;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * The designer plugin class to be used in the desktop. <br/>
 *
 * $Id$
 *
 */
/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class DesignerPlugin extends AbstractUIPlugin {

    public static final String ID = "org.talend.designer.core"; //$NON-NLS-1$

    private static DesignerPlugin plugin;

    public DesignerPlugin() {
        plugin = this;
        /*
         * // plugins list IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry(); String[] namespaces =
         * extensionRegistry.getNamespaces(); System.out.println("Liste des plugins talend:"); for (int i = 0; i <
         * namespaces.length; i++) { if (namespaces[i].startsWith("org.talend")) { String message = namespaces[i];
         * Bundle bundle = Platform.getBundle(namespaces[i]);
         *
         * if (bundle == null) { message += " pb"; } else { String version = (String)
         * bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION); String[] subVersions =
         * version.split("\\."); version = subVersions[0] + "." + subVersions[1] + "." + subVersions[2]; message += "
         * version:" + version; } System.out.println(message); } }
         */
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static DesignerPlugin getDefault() {
        return plugin;
    }

    /**
     * DOC get a implement of RunProcessService.
     *
     * @return a implement of RunProcessService
     */
    public IRunProcessService getRunProcessService() {
        IService service = GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        return (IRunProcessService) service;
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

    /**
     * DOC get a implement of IRepositoryService.
     *
     * @return a implement of IRepositoryService
     */
    public IRepositoryService getRepositoryService() {
        IService service = GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        return (IRepositoryService) service;
    }

    public IProxyRepositoryFactory getProxyRepositoryFactory() {
        IRepositoryService service = getRepositoryService();
        return service.getProxyRepositoryFactory();
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(final String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.talend.designer.core", path); //$NON-NLS-1$
    }

}
