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
package org.talend.designer.core.model.components;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.runtime.util.SharedStudioUtils;

/**
 * created by nrousseau on Apr 3, 2013 Detailled comment
 *
 */
public class ComponentBundleToPath {
	public static final String SHARED_STUDIO_CUSTOM_COMPONENT_BUNDLE = "SharedStudioCustomComponent";
	private static Map<String, String> bundleIdToPath = new HashMap<String, String>();

	public static String getPathFromBundle(String bundle) {
		String applicationPath = bundleIdToPath.get(bundle);
		if (applicationPath == null) {
			if (SHARED_STUDIO_CUSTOM_COMPONENT_BUNDLE.equals(bundle)) {
				String absPath = SharedStudioUtils.getSharedStudioComponentsParentFolder().getAbsolutePath();
				applicationPath = (new Path(absPath)).toPortableString();
			} else {
				try {
					applicationPath = FileLocator.getBundleFile(Platform.getBundle(bundle)).getPath();
					applicationPath = (new Path(applicationPath)).toPortableString();
				} catch (IOException e2) {
					ExceptionHandler.process(e2);
					return null;
				}
			}
			bundleIdToPath.put(bundle, applicationPath);
		}
		return applicationPath;
	}

	public static URL findComponentsBundleURL(String bundleId, IPath path) {
		return ComponentBundleToPath.findComponentsBundleURL(bundleId, path, null);
	}

	public static URL findComponentsBundleURL(String bundleId, IPath path, Map<String, String> override) {
		if (SHARED_STUDIO_CUSTOM_COMPONENT_BUNDLE.equals(bundleId)) {
			URL url = Platform.getConfigurationLocation().getURL();
			if (path != null) {
				IPath basePath = new Path(url.getFile());
				try {
					url = basePath.append(path).toFile().toURI().toURL();
				} catch (MalformedURLException ex) {
					ExceptionHandler.process(ex);
				}
			}
			return url;
		} else {
			Bundle bundle = Platform.getBundle(bundleId);
			return FileLocator.find(bundle, path, override);
		}
	}
}
