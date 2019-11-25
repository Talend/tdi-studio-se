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
import java.nio.file.StandardCopyOption;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class FileCopy {

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
    public static void copyFile(String srcFileName, String desFileName, boolean delSrc) throws IOException {
        final File source = new File(srcFileName);
        final File destination = new File(desFileName);

        if (delSrc) {
            // move : more efficient if in same FS and mustr delete existing file.
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
