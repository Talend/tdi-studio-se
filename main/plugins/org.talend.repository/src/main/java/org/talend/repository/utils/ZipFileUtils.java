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
package org.talend.repository.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.utils.io.FilesUtils;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ZipFileUtils {

    private static int bufSize = 2048; // size of bytes

    public static String zip(String zipDirectory) {
        File zipDir = new File(zipDirectory);
        return zip(zipDirectory, zipDir.getPath(), false);
    }

    public static String zip(String zipDirectory, String zipFileName, boolean includeSelfDir) {
        File zipDir = new File(zipDirectory);
        File[] willZipFileArr;
        if (includeSelfDir || zipDir.isFile()) {
            willZipFileArr = new File[] { zipDir };
        } else {
            willZipFileArr = zipDir.listFiles();
        }
        return zip(willZipFileArr, zipFileName);
    }

    public static String zip(File[] files, String zipFileName) {

        JarOutputStream jarOutput = null;
        try {
            jarOutput = new JarOutputStream(new FileOutputStream(zipFileName));

            for (File file : files) {
                zipFiles(file, jarOutput, "");
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            if (jarOutput != null) {
                try {
                    jarOutput.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return null;
    }

    private static void zipFiles(File file, JarOutputStream jos, String pathName) throws Exception {
        String fileName = pathName + file.getName();
        if (file.isDirectory()) {
            fileName = fileName + "/";
            jos.putNextEntry(new JarEntry(fileName));
            String fileNames[] = file.list();
            if (fileNames != null) {
                for (int i = 0; i < fileNames.length; i++) {
                    zipFiles(new File(file, fileNames[i]), jos, fileName);
                }
                jos.closeEntry();
            }
        } else {
            JarEntry jarEntry = new JarEntry(fileName);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            jos.putNextEntry(jarEntry);

            byte[] buf = new byte[bufSize];
            int len;
            while ((len = in.read(buf)) >= 0) {
                jos.write(buf, 0, len);
            }
            in.close();
            jos.closeEntry();
        }
    }

    public static String unZip(File unZipFile) {
        return unZip(unZipFile.getPath(), null);
    }

    public static String unZip(File unZipFile, String destFileName) {
        if (destFileName == null || destFileName.trim().length() == 0) {
            destFileName = unZipFile.getParent();
        }

        File destFile;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(unZipFile);
            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                destFile = new File(destFileName, entry.getName());
                FilesUtils.validateDestPath(destFileName, destFile.getPath());
                unZipFile(destFile, zipFile, entry);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return e.getMessage();
        } finally {
            try {
                assert zipFile != null;
                zipFile.close();
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    public static String unZip(String unZipFileName) {
        return unZip(unZipFileName, null);
    }

    public static String unZip(String unZipFileName, String destFileName) {
        return unZip(new File(unZipFileName), destFileName);

    }

    private static void unZipFile(File destFile, ZipFile zipFile, ZipEntry entry) throws IOException {
        InputStream inputStream;
        FileOutputStream fileOut;
        if (entry.isDirectory()) {
            destFile.mkdirs();
        } else {
            File parent = destFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            inputStream = zipFile.getInputStream(entry);

            fileOut = new FileOutputStream(destFile);
            byte[] buf = new byte[bufSize];
            int readedBytes;
            while ((readedBytes = inputStream.read(buf)) > 0) {
                fileOut.write(buf, 0, readedBytes);
            }
            fileOut.close();

            inputStream.close();
        }
    }

    public void setBufSize(int bufSize) {
        ZipFileUtils.bufSize = bufSize;
    }
}
