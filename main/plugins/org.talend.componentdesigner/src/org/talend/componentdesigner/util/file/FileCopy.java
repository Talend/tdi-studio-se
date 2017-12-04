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
package org.talend.componentdesigner.util.file;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.talend.componentdesigner.i18n.internal.Messages;

/**
 * DOC rli class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class FileCopy {

    public void copyFile(String srcFileName, String desDirectory) {
        try {
            java.io.File srcFile = new java.io.File(srcFileName);
            // here need check first, before mkdirs().
            if (!srcFile.exists() || !srcFile.isFile()) {
                throw new Exception(Messages.getString("FileCopy.ExceptionMSG", srcFileName)); //$NON-NLS-1$
            }
            String desFileName = srcFile.getName();
            if (desFileName != null && desFileName.trim().equals("")) { //$NON-NLS-1$
                desFileName = "NewName.temp"; //$NON-NLS-1$
            }

            java.io.File desFile = new java.io.File(desDirectory, desFileName);

            if (!srcFile.getPath().equals(desFile.getPath())) {
                java.io.File desFileParent = desFile.getParentFile();
                if (desFileParent != null && !desFileParent.exists()) {
                    desFileParent.mkdirs();
                }
                copy(srcFile.getPath(), desFile.getPath(), false);
            }

        } catch (Exception e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        }
    }

    public static void main(String[] args) {
        FileCopy tttClass = new FileCopy();
        tttClass.copyFile("E:/temp/launch_conf.png", "E:/two"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void copy(String srcFileName, String desFileName, boolean delSrc) throws IOException {
        FileInputStream srcInputStream = new FileInputStream(srcFileName);

        File source = new File(srcFileName);
        File dest = new File(desFileName);

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = srcInputStream;
            out = new FileOutputStream(dest);

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
            if (delSrc) {
                source.delete();
            }
        } catch (Exception e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        } finally {

            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * DOC slanglois Comment method "copyComponentFolder".
     * 
     * @param sourceComponentFolder
     * @param targetComponentFolder
     */
    public static void copyComponentFolder(String sourceComponentFolder, String targetComponentFolder, boolean modifySkeletonValue) {
        try {
            File targetFolder = new File(targetComponentFolder);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File sourceFolder = new File(sourceComponentFolder);
            String[] file = sourceFolder.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (sourceComponentFolder.endsWith(File.separator)) {
                    temp = new File(sourceComponentFolder + file[i]);
                } else {
                    temp = new File(sourceComponentFolder + File.separator + file[i]);
                }

                if (temp.isFile() && !file[i].startsWith(".")) { //$NON-NLS-1$
                    DataInputStream input = new DataInputStream(new FileInputStream(temp));
                    FileOutputStream output = new FileOutputStream(targetComponentFolder + File.separator
                            + (temp.getName()).toString());

                    // check and fix the skeleton value.
                    if (modifySkeletonValue && (file[i].endsWith(".javajet") || file[i].endsWith(".perljet"))) { //$NON-NLS-1$ //$NON-NLS-2$
                        StringBuilder sb = new StringBuilder();
                        while (sb.indexOf("%>") < 0 && sb.indexOf("/templates/") < 0) { //$NON-NLS-1$ //$NON-NLS-2$
                            char c = (char) input.readByte();
                            sb.append(c);
                        }
                        int index = sb.indexOf("/templates/"); //$NON-NLS-1$
                        if (index > 0) {
                            int end = index;
                            int start = index;
                            while (sb.charAt(start) != '"') {
                                start--;
                            }
                            sb.replace(start + 1, end, "../../.."); //$NON-NLS-1$
                        }
                        output.write(sb.toString().getBytes());

                    }

                    byte[] b = new byte[1024];
                    int len;

                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) { // copy subfolder
                    // TDI-31436:Avoid case of copy infinite subfolder if target folder is under source folder
                    if (!temp.getAbsolutePath().equals(targetComponentFolder)) {
                        copyComponentFolder(sourceComponentFolder + File.separator + file[i], targetComponentFolder
                                + File.separator + file[i], modifySkeletonValue);
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);

        }
    }
}
