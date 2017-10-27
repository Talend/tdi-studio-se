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
package org.talend.designer.runprocess.maven;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.CommonsPlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.java.JavaProcessorUtilities;

public class MavenDeployUtil {

    public static void deployToLocalMaven(IProgressMonitor monitor, String[] jarsOrMvnURls) throws Exception {
        if (jarsOrMvnURls != null) {
            // unify
            Map<String, String> jarsForMvnUrlMap = new HashMap<String, String>();
            Set<String> unifiedMvnUrls = new LinkedHashSet<String>();
            for (String name : jarsOrMvnURls) {
                String mvnUrl = name;
                if (!MavenUrlHelper.isMvnUrl(name)) {
                    mvnUrl = MavenUrlHelper.generateMvnUrlForJarName(name);
                    jarsForMvnUrlMap.put(name, mvnUrl);
                }
                unifiedMvnUrls.add(mvnUrl);
            }

            // find the available
            Set<String> availableMvnUrls = PomUtil.availableArtifacts(monitor, unifiedMvnUrls.toArray(new String[0]));
            // left the unavailable
            Set<String> unavailableMvnUrls = new LinkedHashSet<String>(unifiedMvnUrls);
            if (availableMvnUrls != null) {
                unavailableMvnUrls.removeAll(availableMvnUrls);
            }

            // check the only jar name way for old system.
            for (String jarName : jarsForMvnUrlMap.keySet()) {
                String mvnUrl = jarsForMvnUrlMap.get(jarName);
                if (mvnUrl != null && !unavailableMvnUrls.contains(mvnUrl)) { // existed.
                    // remove the existed in local maven repository, left the non-existed jars.
                    jarsForMvnUrlMap.remove(jarName);
                }
            }

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibraryManagerService.class)) {
                ILibraryManagerService libService = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                        ILibraryManagerService.class);

                // 1, only check the old jar name way, if mvn url, no need check.
                // if still have some jars are not installed
                for (String jarName : jarsForMvnUrlMap.keySet()) {
                    String jarPath = libService.getJarPath(jarName);
                    // if jar existed in old "talend.library.path" folder or in bundle, then install it
                    if (jarPath != null) {
                        // assume if the jar is in "talend.library.path" folder, user have agree the jar license, so
                        // install directly.
                        installJarFile(jarPath);
                    }
                }

                // 2, still not existed some.
                if (!unavailableMvnUrls.isEmpty() && !CommonsPlugin.isHeadless()) {
                    // for studio, popup the install modules dialog to install
                    File libDir = JavaProcessorUtilities.getJavaProjectLibFolder();
                    if (libDir != null) {
                        libService.retrieve(unavailableMvnUrls, libDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static void installJarFile(String jarPath) {
        // one fake method first.
    }
}
