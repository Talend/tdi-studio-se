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
package org.talend.designer.codegen.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.utils.io.IOUtils;
import org.talend.core.model.components.ComponentCompilations;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.core.model.components.ComponentBundleToPath;

/**
 * DOC xtan
 * <p>
 * the same as XsdValidationCacheManager.java, used for checking the Skeleton files whether change or not.
 * </p>
 * <p>
 * if there is one Skeleton file have changed, there will generate all jet--->java again.
 * </p>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public final class JetSkeletonManager {

    private Map<String, Long> alreadyCheckedSkeleton;

    private static JetSkeletonManager instance;

    private final boolean forceSkeletonAlreadyChecked = ComponentCompilations.getMarkers();

    private static final String SKELETON_SUFFIX = ".skeleton"; //$NON-NLS-1$

    private static final String INCLUDEFILEINJET_SUFFIX = ".inc.javajet"; //$NON-NLS-1$

    private JetSkeletonManager() {
    }

    private static synchronized JetSkeletonManager getInstance() {
        if (instance == null) {
            instance = new JetSkeletonManager();
        }
        return instance;
    }

    private void load() {
        try {
            deserializeAlreadyChecked();
        } catch (Exception e) {
            IStatus status = new Status(IStatus.WARNING, CodeGeneratorActivator.PLUGIN_ID,
                    Messages.getString("JetSkeletonManager.unableLoad"), e); //$NON-NLS-1$
            CodeGeneratorActivator.getDefault().getLog().log(status);
        }
    }

    private void save() {
        try {
            serializeAlreadyChecked();
        } catch (Exception e) {
            IStatus status = new Status(IStatus.WARNING, CodeGeneratorActivator.PLUGIN_ID,
                    Messages.getString("JetSkeletonManager.unableSave"), e); //$NON-NLS-1$
            CodeGeneratorActivator.getDefault().getLog().log(status);
        }
    }

    private boolean checkAndUpdateCache(java.io.File file) {
        String path = file.getAbsolutePath();
        long currentCRC = 0;
        try {
            currentCRC = IOUtils.computeCRC(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            // ignore here, only print
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        if (forceSkeletonAlreadyChecked) {

            alreadyCheckedSkeleton.put(path, currentCRC);
            return false;
        } else {
            Long lastCheckedCRC = alreadyCheckedSkeleton.get(path);

            boolean isChanged = lastCheckedCRC == null || currentCRC != lastCheckedCRC;
            if (isChanged) {
                alreadyCheckedSkeleton.put(path, currentCRC);
            }

            return isChanged;
        }
    }

    private File getSerializationFilePath() throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(".JETEmitters"); //$NON-NLS-1$
        if (!project.exists()) {
            project.create(new NullProgressMonitor());
        }
        project.open(new NullProgressMonitor());
        IFile file = project.getFile("SkeletonUpdateCache"); //$NON-NLS-1$
        if (!file.exists()) {
            file.create(null, true, new NullProgressMonitor());
        }
        return file.getLocation().toFile();
    }

    private void serializeAlreadyChecked() throws IOException, CoreException {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(getSerializationFilePath()));
            ObjectOutputStream objectOut = new ObjectOutputStream(bufferedOutputStream);
            objectOut.writeObject(alreadyCheckedSkeleton);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (Exception e) {
                // ignore me even if i'm null
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void deserializeAlreadyChecked() throws Exception {
        alreadyCheckedSkeleton = new HashMap<String, Long>();

        File file = getSerializationFilePath();
        if (!file.exists()) {
            return;
        }

        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            ObjectInputStream objectIn = new ObjectInputStream(bufferedInputStream) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    // Enforce the types that we are about to deserialize
                    if (!(desc.getName().startsWith("java.util")
                        || Long.class.getName().equals(desc.getName())
                        || String.class.getName().equals(desc.getName())) {
                        throw new InvalidClassException("Unauthorized deserialization attempt", desc.getName());
                    }
                    return super.resolveClass(desc);
                }
            };
            alreadyCheckedSkeleton = (Map<String, Long>) objectIn.readObject();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                bufferedInputStream.close();
            } catch (Exception e) {
                // ignore me even if i'm null
            }
        }
    }

    /**
     * DOC xtan
     * <p>
     * check the skeleton file whether changed or not, and save the SkeletonUpdateCache file again.
     * </p>
     *
     * @return true if there is one skeleton file changed.
     */
    public static boolean updateSkeletonPersistenceData() {

        boolean doUpdate = false;

        JetSkeletonManager localInstance = JetSkeletonManager.getInstance();
        localInstance.load();

        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

        List<String> skeletons = new ArrayList<String>();
        List<String> systemSkeletons = getSystemSkeletons();
        List<String> componentSkeletons = componentsFactory.getSkeletons();

        if (systemSkeletons != null && !systemSkeletons.isEmpty()) {
            skeletons.addAll(systemSkeletons);
        }
        if (componentSkeletons != null && !componentSkeletons.isEmpty()) {
            skeletons.addAll(componentSkeletons);
        }

        for (String jetSkeleton : skeletons) {
            // System.out.println(jetSkeleton);
            try {

                File file = new File(jetSkeleton);
                if (localInstance.checkAndUpdateCache(file)) {
                    doUpdate = true;
                    // System.out.println("need check:" + jetSkeleton);
                }
            } catch (Exception e) {
                IStatus status = new Status(IStatus.WARNING, CodeGeneratorActivator.PLUGIN_ID,
                        Messages.getString("JetSkeletonManager.updateProblem"), e); //$NON-NLS-1$
                CodeGeneratorActivator.getDefault().getLog().log(status);
                localInstance.save();
                return true;
            }
        }

        localInstance.save();

        return doUpdate;
    }

    /**
     * DOC xtan get the skeleton file: subprocess_header_java.skeleton.
     *
     * @return
     */
    private static List<String> getSystemSkeletons() {

        List<String> skeletonList = new ArrayList<String>();

        // here add the skeleton file in org.talend.designer.codegen\resources
        FileFilter skeletonFilter = new FileFilter() {

            @Override
            public boolean accept(final File file) {
                String fileName = file.getName();
                return file.isFile() && fileName.charAt(0) != '.'
                        && (fileName.endsWith(SKELETON_SUFFIX) || fileName.endsWith(INCLUDEFILEINJET_SUFFIX));
            }

        };

        for (TemplateUtil template : CodeGeneratorInternalTemplatesFactoryProvider.getInstance().getTemplates()) {
            URL resourcesUrl = null;
            try {
                resourcesUrl = FileLocator.toFileURL(ComponentBundleToPath.findComponentsBundleURL(template.getJetPluginRepository(), new Path(template.getTemplateRelativeUri()), null));
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
            if (resourcesUrl != null) {
                File resourcesDir = new File(resourcesUrl.getFile());
                File[] skeletonFiles = resourcesDir.listFiles(skeletonFilter);
                if (skeletonFiles != null) {
                    for (File file : skeletonFiles) {
                        skeletonList.add(file.getAbsolutePath()); // path
                    }
                }
            }
        }

        return skeletonList;
    }

}
