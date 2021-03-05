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
package org.talend.repository.imports;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.talend.utils.io.FilesUtils;

/**
 * @deprecated have moved to /org.talend.repository.items.importexport, but still need for MDM, so keep it temp.
 */
@Deprecated
public class FilesManager extends ResourcesManager {

    @Override
    public InputStream getStream(IPath path) throws IOException {
        if (path2Object.get(path) != null) {
            return new BufferedInputStream(new FileInputStream((File) path2Object.get(path)));
        }
        return null;
    }

    @Override
    public boolean collectPath2Object(Object root) {
        return doCollectItemFiles((File) root);
    }

    private boolean doCollectItemFiles(File directory) {
        File[] contents = directory.listFiles();

        if (contents != null) {
            for (File content : contents) {
                File file = content;

                if (file.isFile()) {
                    add(file.getAbsolutePath(), file);
                }
                if (file.isDirectory()) {
                    if (!file.getName().equals(WizardProjectsImportPage.METADATA_FOLDER) && (!FilesUtils.isSVNFolder(file))) {
                        collectPath2Object(content);
                    }
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.localprovider.imports.ResourcesManager#getProvider()
     */
    @Override
    public IImportStructureProvider getProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.localprovider.imports.ResourcesManager#closeResource()
     */
    @Override
    public void closeResource() {
        // TODO Auto-generated method stub

    }
}
