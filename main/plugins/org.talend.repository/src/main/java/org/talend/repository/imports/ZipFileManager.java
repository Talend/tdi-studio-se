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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * @deprecated have moved to /org.talend.repository.items.importexport, but still need for MDM, so keep it temp.
 */
@Deprecated
public class ZipFileManager extends ResourcesManager {

    private ZipFile zipFile;

    public ZipFileManager(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public InputStream getStream(IPath path) throws IOException {
        if (path2Object.get(path) != null) {
            return zipFile.getInputStream((ZipEntry) path2Object.get(path));
        }
        return null;
    }

    @Override
    public boolean collectPath2Object(Object root) {
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            add(entry.getName(), entry);
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
