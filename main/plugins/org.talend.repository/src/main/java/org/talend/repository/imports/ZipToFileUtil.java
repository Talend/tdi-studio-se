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
package org.talend.repository.imports;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.talend.core.repository.constants.FileConstants;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ZipToFileUtil {

    private ZipInputStream zipIn;

    private ZipOutputStream zipOut;

    private ZipEntry zipEntry;

    private static int bufSize;

    private byte[] buf;

    private int readedBytes;

    public ZipToFileUtil() {
        this(512);
    }

    public ZipToFileUtil(int bufSize) {
        this.bufSize = bufSize;
        this.buf = new byte[this.bufSize];
    }

    public void doZip(String zipDirectory) {
        File file;
        File zipDir;
        zipDir = new File(zipDirectory);
        String zipFileName = zipDir.getName() + FileConstants.ZIP_FILE_SUFFIX;
        try {
            this.zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
            handleDir(zipDir, this.zipOut);
            this.zipOut.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void handleDir(File dir, ZipOutputStream zipOut) throws IOException {
        FileInputStream fileIn;
        File[] files;
        files = dir.listFiles();

        if (files.length == 0) {

            this.zipOut.putNextEntry(new ZipEntry(dir.toString() + "/"));
            this.zipOut.closeEntry();
        } else {
            for (File fileName : files) {
                // System.out.println(fileName);
                if (fileName.isDirectory()) {
                    handleDir(fileName, this.zipOut);
                } else {
                    fileIn = new FileInputStream(fileName);
                    this.zipOut.putNextEntry(new ZipEntry(fileName.toString()));
                    while ((this.readedBytes = fileIn.read(this.buf)) > 0) {
                        this.zipOut.write(this.buf, 0, this.readedBytes);
                    }
                    this.zipOut.closeEntry();
                }
            }
        }
    }

    public void unZip(String unZipfileName) {
        FileOutputStream fileOut;
        File file;
        String pathPrefix = "/";
        try {
            this.zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(unZipfileName)));
            // if (unZipfileName.indexOf("/") == 0) {
            // pathPrefix = unZipfileName.substring(0, unZipfileName.lastIndexOf("/"));
            // } else {
            pathPrefix = unZipfileName.substring(0, unZipfileName.lastIndexOf("/"));
            pathPrefix = pathPrefix + "/";
            // }
            while ((this.zipEntry = this.zipIn.getNextEntry()) != null) {
                file = new File(pathPrefix + this.zipEntry.getName());
                if (this.zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // if the file do not exist, create it
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    fileOut = new FileOutputStream(file);
                    while ((this.readedBytes = this.zipIn.read(this.buf)) > 0) {
                        fileOut.write(this.buf, 0, this.readedBytes);
                    }
                    fileOut.close();
                }
                this.zipIn.closeEntry();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void setBufSize(int bufSize) {
        this.bufSize = bufSize;
    }

    // delete the File
    public static boolean deleteDirectory(String folder) {
        String dir = folder;
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);

        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        // delete all the file
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // delete the file
            if (file.isFile()) {
                flag = deleteFile(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            }

            else {
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }

        if (!flag) {
            return false;
        }

        // delete the directory
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            return true;
        } else {
            return false;
        }
    }

}
