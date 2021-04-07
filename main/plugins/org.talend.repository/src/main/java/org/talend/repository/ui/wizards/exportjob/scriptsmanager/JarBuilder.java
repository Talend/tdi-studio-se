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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.ResourceModelUtils;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.repository.ProjectManager;
import org.talend.repository.utils.ZipFileUtils;
import org.talend.utils.files.FileUtils;
import org.talend.utils.io.FilesUtils;

/**
 * This is a jar file builder. <br/>
 *
 * $Id: MakeJarRunnable.java Mar 30, 200712:49:05 PM bqian $
 *
 */
public class JarBuilder {

    final File dir;

    final File jarFile;

    Collection<String> includeDirs = null;

    Collection<String> excludeDirs = null;

    Collection<File> excludeFiles = null;

    Collection<File> includeRoutines = null;

    //    private static final String SYSTEM = "system"; //$NON-NLS-1$

    private static final String CONTEXT = "context"; //$NON-NLS-1$

    private static final String TEMP = "temp"; //$NON-NLS-1$

    private List<File> libPath;

    private boolean isFatJar = false;

    /**
     * Constructure.
     *
     * @param root
     * @param jarFile
     * @param jarName
     * @param includeDirs
     */
    public JarBuilder(File root, File jarFile) {
        this.dir = root;
        this.jarFile = jarFile;
    }

    public void setIncludeDir(Collection<String> includeDirs) {
        this.includeDirs = includeDirs;
    }

    public void setExcludeDir(Collection<String> excludeDirs) {
        this.excludeDirs = excludeDirs;
    }

    public void setExcludeFiles(Collection<File> excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    public void setIncludeRoutines(Collection<File> includeRoutines) {
        this.includeRoutines = includeRoutines;
    }

    /**
     * Gets the files to zip in jar.
     *
     * @return
     */
    private Collection<File> getExportedFiles() {

        if (includeDirs == null) {
            includeDirs = Collections.singleton(""); //$NON-NLS-1$
        }
        Collection<File> includeFiles = getAllFiles(includeDirs);

        if (excludeDirs != null) {
            includeFiles.removeAll(getAllFiles(excludeDirs));
        }
        if (excludeFiles != null) {
            includeFiles.removeAll(excludeFiles);
        }
        if (includeRoutines != null) {
            for (File f : includeRoutines) {
                if (!includeFiles.contains(f)) {
                    includeFiles.add(f);
                }
            }
        }

        return includeFiles;
    }

    private Collection<File> getAllFiles(Collection<String> subDirs) {
        final Collection<File> list = new ArrayList<File>();

        for (String subDir : subDirs) {

            File subFile = new File(dir, subDir);
            subFile.listFiles(new java.io.FilenameFilter() {

                @Override
                public boolean accept(java.io.File dir, String name) {
                    File file = new java.io.File(dir, name);
                    if (file.isFile()) {
                        list.add(file);
                        return true;
                    } else {
                        file.listFiles(this);
                    }
                    return false;
                }
            });
        }
        return list;
    }

    private Manifest getManifest() {
        Manifest manifest = new Manifest();
        Attributes a = new Attributes();
        a.put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0"); //$NON-NLS-1$
        a.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Talend Open Studio"); //$NON-NLS-1$
        manifest.getEntries().put(jarFile.getName(), a);
        return manifest;
    }

    /**
     * Builds the jar file.
     *
     * @throws Exception
     */
    public void buildJar() throws IOException {
        exportJar(dir, getExportedFiles(), getManifest());
    }

    /**
     * Create temp folder for zip files to jar file. Add by nma, order 12346
     *
     * @throws Exception
     */
    private void createTempSubFolder(String tempFolderPath, File srcFile) {
        if (srcFile.isDirectory() && !srcFile.getName().equals(CONTEXT)) {
            File projectFolder = new File(tempFolderPath + File.separatorChar + srcFile.getName());
            if (!projectFolder.exists()) {
                projectFolder.mkdir();
                File[] folderFiles = srcFile.listFiles();
                for (File f : folderFiles) {
                    createTempSubFolder(projectFolder.getAbsolutePath(), f);
                }
            }
        }
    }

    /**
     * exports the jar to specific location.
     *
     * @param root
     * @param list
     * @param manifest
     */
    private void exportJar(File root, Collection<File> list, Manifest manifest) throws IOException {
        if (jarFile.exists()) {
            StringBuffer tempFolderBuffer = new StringBuffer(jarFile.getParent());
            String tempFolderPath = tempFolderBuffer.append(File.separatorChar).toString();
            // TDI-29008:should create the temp folder use timestamp when export several jobs at the same time,the
            // the jar contains classes will not affected by other jobs
            File tempFolder = getExportJarTempFolder();
            tempFolderPath = tempFolderPath + tempFolder.getName();
            ZipFileUtils.unZip(jarFile, tempFolderPath);
            for (File subf : list) {
                String desFileName = subf.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                File srcFile = subf;
                while (!srcFile.getParentFile().getAbsolutePath().equals(root.getAbsolutePath())) {
                    srcFile = srcFile.getParentFile();
                }
                createTempSubFolder(tempFolderPath, srcFile);
                FileChannel srcChannel = null;
                FileChannel dstChannel = null;
                try {
                    srcChannel = new FileInputStream(subf.getAbsoluteFile()).getChannel();
                    // Create intermediate folders (needed for contexts folder)
                    new File(tempFolderPath + File.separatorChar + desFileName).getParentFile().mkdirs();
                    dstChannel = new FileOutputStream(tempFolderPath + File.separatorChar + desFileName).getChannel();
                    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
                } finally {
                    if (null != srcChannel) {
                        srcChannel.close();
                    }
                    if (null != dstChannel) {
                        dstChannel.close();
                    }
                }
            }
            if (libPath != null && libPath.size() > 0) {
                File tempLib = new File(tempFolderPath + File.separatorChar + JavaUtils.JAVA_LIB_DIRECTORY);
                for (File file : libPath) {
                    if (isFatJar) {
                        // unzip the jar file
                        unZipFile(file.getAbsolutePath(), new File(tempFolderPath).getAbsolutePath());
                    } else {
                        if (!tempLib.exists()) {
                            tempLib.mkdir();
                        }
                        FilesUtils.copyFile(new FileInputStream(file), new File(tempLib.getAbsolutePath() + File.separatorChar
                                + file.getName()));
                    }
                }
            }
            // First we remove the signature files from the META-INF folder (see TBD-5163 for example)
            FileUtils.deleteFiles(new File(tempFolderPath + "/META-INF"), name -> name.endsWith(".RSA") || name.endsWith(".SF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            // Then we archive
            ZipFileUtils.zip(tempFolderPath, jarFile.getPath(), false);
        } else {
            JarOutputStream jarOut = null;
            try {
                jarOut = new JarOutputStream(new FileOutputStream(jarFile), manifest);

                for (File subf : list) {
                    String filename = subf.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                    JarEntry entry = new JarEntry(filename.replace('\\', '/'));
                    jarOut.putNextEntry(entry);

                    FileInputStream fin = new FileInputStream(subf);
                    byte[] buf = new byte[4096];
                    int read;
                    while ((read = fin.read(buf)) != -1) {
                        jarOut.write(buf, 0, read);
                    }
                    fin.close();

                    jarOut.closeEntry();
                    jarOut.flush();
                }
            } finally {
                if (jarOut != null) {
                    jarOut.close();
                }
            }
        }
    }

    private File getExportJarTempFolder() {
        File tempFolder = null;
        try {
            tempFolder = File.createTempFile(TEMP, null);
            if (tempFolder.exists() && tempFolder.isFile()) {
                tempFolder.delete();
            }
            tempFolder.mkdirs(); // use the same tmp file to make the tmp folder.

        } catch (IOException e) {
            ExceptionHandler.process(e);
            tempFolder = new File(getTmpFolder());
        }

        return tempFolder;
    }

    private String getTmpFolder() {
        String tmpFold = getTmpFolderPath();
        File f = new File(tmpFold);
        if (!f.exists()) {
            f.mkdir();
        }
        return tmpFold;
    }

    private String getTmpFolderPath() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        String tmpFolder;
        try {
            IProject physProject = ResourceModelUtils.getProject(project);
            tmpFolder = physProject.getFolder(TEMP).getLocation().toPortableString();
        } catch (Exception e) {
            tmpFolder = SharedStudioUtils.getTempFolderPath().toPortableString();
        }
        tmpFolder = tmpFolder + "/talendExporter"; //$NON-NLS-1$
        return tmpFolder;
    }

    /**
     * Getter for libPath.
     *
     * @return the libPath
     */
    public List<File> getLibPath() {
        return this.libPath;
    }

    /**
     * Sets the libPath.
     *
     * @param libPath the libPath to set
     */
    public void setLibPath(List<File> libPath) {
        this.libPath = libPath;
    }

    private void unZipFile(String jarPath, String outFileName) throws IOException {
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> jarEntrys = jarFile.entries();
        while (jarEntrys.hasMoreElements()) {
            JarEntry jarEntry = jarEntrys.nextElement();
            File f = new File(outFileName + File.separator + jarEntry.getName());
            FilesUtils.validateDestPath(outFileName, f.getPath());
            makeSupDir(f.getAbsolutePath());
            if (jarEntry.isDirectory() || jarEntry.getName().contains("MANIFEST.MF")) { //$NON-NLS-1$
                continue;
            }
            writeFile(jarFile.getInputStream(jarEntry), f);
        }
    }

    private static void makeSupDir(String outFileName) {
        Pattern p = Pattern.compile("[/\\" + File.separator + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        Matcher m = p.matcher(outFileName);
        while (m.find()) {
            int index = m.start();
            String subDir = outFileName.substring(0, index);
            File subDirFile = new File(subDir);
            if (!subDirFile.exists()) {
                subDirFile.mkdir();
            }
        }
    }

    private static void writeFile(File inputFile, File outputFile) throws IOException {
        writeFile(new FileInputStream(inputFile), outputFile);
    }

    private static void writeFile(InputStream ips, File outputFile) throws IOException {
        OutputStream ops = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            byte[] buffer = new byte[1024];
            int nBytes = 0;
            while ((nBytes = ips.read(buffer)) > 0) {
                ops.write(buffer, 0, nBytes);
            }
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            try {
                if (null != ops) {
                    ops.flush();
                    ops.close();
                }
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                if (null != ips) {
                    ips.close();
                }
            }
        }
    }

    /**
     * Getter for isStorm.
     *
     * @return the isStorm
     */
    public boolean isFatJar() {
        return this.isFatJar;
    }

    /**
     * Sets the isStorm.
     *
     * @param isStorm the isStorm to set
     */
    public void setFatJar(boolean isFatJar) {
        this.isFatJar = isFatJar;
    }
}
