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
package org.talend.repository.view.di.viewer.handlers.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ILibrariesService;
import org.talend.repository.items.importexport.manager.ResourcesManager;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class ImportHandlerUtil {

    /**
     * 
     * DOC jding Comment method "deployJarToDestForArchive".
     * 
     * @param manager
     * @param extRoutines K:moduleName V:mvnUrl
     * @param jarsToDeploy K:mvnUrl V:file URL
     */
    public static void deployJarToDestForArchive(final ResourcesManager manager, Map<String, String> extRoutines,
            Map<String, URL> jarsToDeploy) {
        if (extRoutines.isEmpty()) {
            return;
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {
            IPath tmpDir = new Path(System.getProperty("user.dir") + File.separatorChar + "tmpJar"); //$NON-NLS-1$ //$NON-NLS-2$

            File dirFile = tmpDir.toFile();
            Set<String> moduleSet = extRoutines.keySet();
            for (IPath path : manager.getPaths()) {
                String fileName = path.lastSegment();
                if (moduleSet.contains(fileName)) {
                    try {
                        InputStream is = manager.getStream(path);
                        if (!dirFile.exists()) {
                            dirFile.mkdirs();
                        }
                        File temFile = tmpDir.append(fileName).toFile();
                        if (temFile.exists()) {
                            temFile.delete();
                        }
                        byte[] b = new byte[1024];
                        int length = 0;
                        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(temFile, true));
                        while ((length = is.read(b)) != -1) {
                            fos.write(b, 0, length);
                        }
                        fos.close();

                        jarsToDeploy.put(extRoutines.get(fileName), temFile.toURI().toURL());
                    } catch (MalformedURLException e) {
                        ExceptionHandler.process(e);
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
    }

    /**
     * 
     * DOC jding Comment method "deployJarToDest".
     * 
     * @param manager
     * @param extRoutines K:moduleName V:mvnUrl
     * @param jarsToDeploy K:mvnUrl V:file URL
     */
    public static void deployJarToDest(final ResourcesManager manager, Map<String,String> extRoutines, Map<String,URL> jarsToDeploy) {
        File file = null;
        if (extRoutines.isEmpty()) {
            return;
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {
            Set<String> moduleSet = extRoutines.keySet();
            for (Object element : manager.getPaths()) {
                String value = element.toString();
                file = new File(value);
                if (moduleSet.contains(file.getName())) {
                    try {
                        jarsToDeploy.put(extRoutines.get(file.getName()), file.toURL());
                    } catch (MalformedURLException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
    }

}
