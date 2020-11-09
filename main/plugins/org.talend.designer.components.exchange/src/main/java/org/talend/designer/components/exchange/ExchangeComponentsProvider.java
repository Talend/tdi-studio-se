// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.components.exchange;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.runtime.util.SharedStudioInfoProvider;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.components.exchange.util.ExchangeUtils;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class ExchangeComponentsProvider extends AbstractComponentsProvider implements SharedStudioInfoProvider{

    /**
     * ExchangeComponentsProvider constructor.
     */
    public ExchangeComponentsProvider() {
    }

    @Override
    public void preComponentsLoad() throws IOException {
        File installFolder = getInstallationFolder();

        // clean folder
        if (installFolder.exists()) {
            FilesUtils.removeFolder(installFolder, true);
        }
        FilesUtils.createFoldersIfNotExists(installFolder.getAbsolutePath(), false);

        File externalComponentsLocation = getExternalComponentsLocation();
        if (externalComponentsLocation != null) {
            if (externalComponentsLocation.exists()) {
                try {
                    copyFolder(externalComponentsLocation, installFolder);
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    /**
     *
     * DOC hcyi Comment method "copyFolder".
     *
     * @param externalComponentsLocation
     * @param installFolder
     * @throws IOException
     */
    private void copyFolder(File externalComponentsLocation, File installFolder) throws IOException {
        FileFilter folderFilter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        FileFilter ff = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (FilesUtils.isSVNFolder(pathname)) {
                    return false;
                }
                return true;
            }

        };

        for (File folder : externalComponentsLocation.listFiles(folderFilter)) {
            /*
             * Some components have: <componentName>/<files> . some other have: <folder>/<componentName>/<files> .
             */
            File componentFolder = searchComponentFolder(folder);
            // if we cannot find the component folder, just ignore it
            if (componentFolder != null) {
                File target = new File(installFolder, componentFolder.getName());
                // FileUtils.copyDirectory(componentFolder, target);
                FilesUtils.copyFolder(componentFolder, target, true, ff, null, true, false);
            }
        }
    }

    /**
     *
     * DOC hcyi Comment method "searchComponentFolder".
     *
     * @param folder
     * @return
     */
    public static File searchComponentFolder(File folder) {
        File componentFolder = null;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    componentFolder = searchComponentFolder(file);
                    if (componentFolder != null) {
                        break;
                    }
                } else {
                    // assumes that if the folder contains properties file, it is the root folder of component
                    if (file.getName().endsWith(".properties")) { //$NON-NLS-1$
                        componentFolder = folder;
                        break;
                    }
                }
            }
        }
        return componentFolder;
    }

    @Override
    protected File getExternalComponentsLocation() {
        return ExchangeUtils.getComponentFolder();
    }

    @Override
    public String getComponentsLocation() {
        return new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER).append(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER)
                .append(ComponentUtilities.getExtFolder(getFolderName())).toString();
    }

    @Override
    public File getInstallationFolder() throws IOException {
        String componentsPath = IComponentsFactory.COMPONENTS_LOCATION;
        IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (breaningService.isPoweredOnlyCamel()) {
            componentsPath = IComponentsFactory.CAMEL_COMPONENTS_LOCATION;
        }
        Bundle b = Platform.getBundle(componentsPath);

        File installationFolder = null;
        IPath nullPath = new Path(""); //$NON-NLS-1$
        URL url = FileLocator.find(b, nullPath, null);
        URL fileUrl = FileLocator.toFileURL(url);
        File bundleFolder = new File(fileUrl.getPath());

        IPath path = new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER)
                .append(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER);
        path = path.append(ComponentUtilities.getExtFolder(getFolderName()));

        installationFolder = new File(bundleFolder, path.toOSString());

        return installationFolder;
    }

    @Override
    public boolean isUseLocalProvider() {
        return true;
    }

    public boolean isCustom() {
        return true;
    }

    public String getComponentsBundle() {
        return IComponentsFactory.COMPONENTS_LOCATION;
    }

    public boolean isSupportCurrentMode() {
    	if (SharedStudioUtils.isSharedStudioMode()) {
    		return false;
    	}
    	return true;
    }
}
