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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * @deprecated have moved to /org.talend.repository.items.importexport, but still need for MDM, so keep it temp.
 */
@Deprecated
public class ProviderManager extends ResourcesManager {

    private IImportStructureProvider provider;

    public ProviderManager(Object provider) {
        this.provider = (IImportStructureProvider) provider;
    }

    @Override
    public InputStream getStream(IPath path) {
        if (path2Object.get(path) != null) {
            return provider.getContents(path2Object.get(path));
        }
        return null;
    }

    @Override
    public boolean collectPath2Object(Object root) {
        return doCollectItemFiles(root, 0);
    }

    private boolean doCollectItemFiles(Object entry, int level) {
        List children = provider.getChildren(entry);
        if (children == null) {
            children = new ArrayList(1);
        }
        Iterator childrenEnum = children.iterator();
        while (childrenEnum.hasNext()) {
            Object child = childrenEnum.next();
            if (provider.isFolder(child)) {
                doCollectItemFiles(child, level + 1);
            } else {
                add(provider.getFullPath(child), child);
            }
        }
        return true;
    }

    @Override
    public void closeResource() {
        if (provider instanceof ZipLeveledStructureProvider) {
            ((ZipLeveledStructureProvider) provider).closeArchive();
        }

        if (provider instanceof TarLeveledStructureProvider) {
            ((TarLeveledStructureProvider) provider).closeArchive();
        }
    }

    /**
     * Getter for provider.
     *
     * @return the provider
     */
    @Override
    public IImportStructureProvider getProvider() {
        return this.provider;
    }
}
