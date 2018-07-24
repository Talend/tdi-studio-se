package org.talend.repository.resource;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RouteResourceActivator extends AbstractUIPlugin {

	// The plug-in ID
    public static final String PLUGIN_ID = "org.talend.repository.resource"; //$NON-NLS-1$

	// The shared instance
	private static RouteResourceActivator plugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RouteResourceActivator getDefault() {
		return plugin;
	}

	/**
	 * The constructor
	 */
	public RouteResourceActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static ImageDescriptor createImageDesc(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
