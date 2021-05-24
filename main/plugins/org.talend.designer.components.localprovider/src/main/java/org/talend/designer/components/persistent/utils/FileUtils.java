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
package org.talend.designer.components.persistent.utils;

import java.io.File;
import java.io.IOException;

/**
 *
 * FileUtils.
 */
public final class FileUtils {

    private FileUtils() {
        super();
    }

    public static synchronized void createParentFolderIfNotExists(String filePath) throws IOException {

        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (!parentFile.isDirectory()) {
            boolean createFolder = parentFile.mkdirs();
            if (!createFolder) {
                throw new RuntimeException("The following directory can't be created : '" //$NON-NLS-1$
                        + parentFile.getAbsolutePath() + "'"); //$NON-NLS-1$
            }
        }

    }

    public static void main(String[] args) throws IOException {
        createParentFolderIfNotExists("/home/amaumont/temp/folder/test"); //$NON-NLS-1$
    }

}
