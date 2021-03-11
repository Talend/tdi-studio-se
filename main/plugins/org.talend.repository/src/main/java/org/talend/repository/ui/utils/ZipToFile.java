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
package org.talend.repository.ui.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.repository.constants.FileConstants;

/**
 * DOC aiming class global comment. Detailled comment
 */
public class ZipToFile {

    public static final int BUFFER = 1024;// buf size

    public static void deleteDirectory(File dir) {
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                deleteDirectory(entries[i]);
            } else {
                entries[i].delete();
            }
        }
        dir.delete();
    }

    /**
     *
     * DOC wzhang Comment method "copyFile".
     *
     * @param src
     * @param dest
     */
    public static void copyFile(String src, String dest) {
        File srcFile = new File(src);
        if (srcFile.exists()) {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int i = 0;
                while ((i = in.read(buffer)) != -1) {
                    out.write(buffer, 0, i);
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     *
     * DOC aiming Comment method "zipFile".
     *
     * @param baseDir
     * @param zipFile
     * @throws Exception
     */
    public static void zipFile(String baseDir, String zipFile) throws Exception {
        List<File> fileList = getSubFiles(new File(baseDir));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry ze = null;
        byte[] buf = new byte[BUFFER];
        int readLen = 0;
        for (int i = 0; i < fileList.size(); i++) {
            File f = fileList.get(i);
            ze = new ZipEntry(getAbsFileName(baseDir, f));
            if (f.isDirectory()) {
                zos.putNextEntry(ze);
                continue;
            }
            ze.setSize(f.length());
            ze.setTime(f.lastModified());
            zos.putNextEntry(ze);
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            while ((readLen = is.read(buf, 0, BUFFER)) != -1) {
                zos.write(buf, 0, readLen);
            }
            is.close();
        }
        zos.close();
    }

    /**
     *
     * DOC aiming Comment method "getAbsFileName".
     *
     * @param baseDir
     * @param realFileName
     * @return
     */
    private static String getAbsFileName(String baseDir, File realFileName) {
        File real = realFileName;
        File base = new File(baseDir);
        String ret = real.getName();
        if (real.equals(base)) {
            return "/";
        }
        while (true) {
            real = real.getParentFile();
            if (real == null) {
                break;
            }
            if (real.equals(base)) {
                break;
            } else {
                ret = real.getName() + "/" + ret; //$NON-NLS-1$
            }
        }
        if (realFileName.isDirectory()) {
            ret += "/";
        }
        return ret;
    }

    /**
     *
     * DOC aiming Comment method "getSubFiles".
     *
     * @param baseDir
     * @return
     */
    private static List<File> getSubFiles(File baseDir) {
        List<File> ret = new ArrayList<File>();

        File[] tmp = baseDir.listFiles();
        for (File item : tmp) {
            if (item.isFile()) {
                if (FileConstants.MANIFEST_MF_FILE_NAME.equalsIgnoreCase(item.getName())) {
                    // "META-INF/MANIFEST.MF" must be a first entry in JAR
                    // package
                    ret.add(0, item);
                } else {
                    ret.add(item);
                }
            } else if (item.isDirectory()) {

                // TESB-3436 fix issue with creating not needed folder.
                // LiXiaoPeng
                ret.add(item);
                if (FileConstants.META_INF_FOLDER_NAME.equalsIgnoreCase(item.getName())) {

                    // "META-INF/MANIFEST.MF" must be a first entry in JAR
                    // package
                    ret.addAll(0, getSubFiles(item));
                } else {
                    ret.addAll(getSubFiles(item));
                }
            }
        }
        return ret;
    }

    /**
     *
     * DOC aiming Comment method "unZipFile".
     *
     * @param zipfile
     * @param unzipdir
     * @throws Exception
     */
    public static void unZipFile(String zipfile, String unzipdir) throws Exception {
        FilesUtils.unzip(zipfile, unzipdir);
    }

    public static void main(String[] args) {
        try {
            zipFile("C:\\zipfile\\", "C:\\new.jar"); //$NON-NLS-1$ //$NON-NLS-2$
            unZipFile("C:\\new.jar", "c:/unzipf/"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }
}
