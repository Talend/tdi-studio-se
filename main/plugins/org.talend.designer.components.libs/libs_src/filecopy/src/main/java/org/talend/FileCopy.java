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
package org.talend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class FileCopy {

    static Logger  logger = LoggerFactory.getLogger(Object.class);

    /** Private constructor, only static methods */
    private FileCopy() {
    }

    /**
     * Copy files.
     * 
     * @param srcFileName : file name for source file.
     * @param desFileName : file name for destination file.
     * @param delSrc : true if delete source.
     * @throws IOException : if IO pb.
     */
    public static void copyFile(String srcFileName, String desFileName, boolean delSrc, boolean keepModified)
            throws IOException {
        final Path source = Paths.get(srcFileName);
        final Path destination =  Paths.get(desFileName);
        FileTime lastModifiedTime = null;
        try {
            lastModifiedTime = Files.getLastModifiedTime(source);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
        if (delSrc) {
            // move : more efficient if in same FS and mustr delete existing file.
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        if(keepModified){
            try {
                Files.setLastModifiedTime(destination,lastModifiedTime);
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        }
    }

    public static void copyFile(String srcFileName, String desFileName, boolean delSrc ) throws IOException {
        copyFile(srcFileName,desFileName,delSrc,true);
    }

    /**
     * Force Copy and Delete files.
     *
     * @param srcFileName : file name for source file.
     * @param desFileName : file name for destination file.
     * @throws IOException : if IO pb.
     */
    public static void forceCopyAndDelete(String srcFileName, String desFileName, boolean keepModified) throws IOException {
        final Path source = Paths.get(srcFileName);
        final Path destination =  Paths.get(desFileName);
        final long lastModifiedTime = new File(srcFileName).lastModified();

        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        Files.delete(source);
        if(keepModified){
            destination.toFile().setLastModified(lastModifiedTime);
        }

    }

    public static void forceCopyAndDelete(String srcFileName, String desFileName) throws IOException {
        forceCopyAndDelete(srcFileName,desFileName,true);
    }

}
