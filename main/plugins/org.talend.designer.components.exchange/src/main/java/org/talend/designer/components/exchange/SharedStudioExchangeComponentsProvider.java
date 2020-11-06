package org.talend.designer.components.exchange;
//============================================================================
//
//Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
//This source code is available under agreement available at
//%InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//You should have received a copy of the agreement
//along with this program; if not, write to Talend SA
//9 rue Pages 92150 Suresnes, France
//
//============================================================================
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.runtime.util.ComponentsLocationProvider;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.designer.core.model.components.ComponentBundleToPath;

public class SharedStudioExchangeComponentsProvider extends ExchangeComponentsProvider implements ComponentsLocationProvider{
    @Override
    public File getInstallationFolder() throws IOException {
		File componentFolder = SharedStudioUtils.getSharedStudioComponentsParentFolder();
		IPath path = new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER);
        path = path.append(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER).append(ComponentUtilities.getExtFolder(getFolderName()));
        File installationFolder = new File (componentFolder, path.toOSString());
		return installationFolder;
    }
    

    public String getComponentsBundle() {
        return ComponentBundleToPath.SHARED_STUDIO_CUSTOM_COMPONENT_BUNDLE;
    }
    
    public boolean isSupportCurrentMode() {
    	if (SharedStudioUtils.isSharedStudioMode()) {
    		return true;
    	}
    	return false;
    }
     
    @Override
    public ResourceBundle getResourceBundle(String label) {
    	URL configFolderUrl = Platform.getConfigurationLocation().getURL();
    	URLClassLoader urlLoader = new URLClassLoader(new java.net.URL[]{configFolderUrl});
    	java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle( label , 
    	                java.util.Locale.getDefault(), urlLoader );
    	return bundle;
    }
}
