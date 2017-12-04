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
package org.talend.designer.core.model.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;

/**
 * created by nrousseau on Apr 3, 2013 Detailled comment
 * 
 */
public class ComponentBundleToPath {

    private static Map<String, String> bundleIdToPath = new HashMap<String, String>();

    public static String getPathFromBundle(String bundle) {
        String applicationPath = bundleIdToPath.get(bundle);
        if (applicationPath == null) {
            try {
                applicationPath = FileLocator.getBundleFile(Platform.getBundle(bundle)).getPath();
                applicationPath = (new Path(applicationPath)).toPortableString();
            } catch (IOException e2) {
                ExceptionHandler.process(e2);
                return null;
            }
            bundleIdToPath.put(bundle, applicationPath);
        }
        return applicationPath;
    }
}
