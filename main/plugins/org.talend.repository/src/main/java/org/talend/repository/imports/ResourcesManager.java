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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.talend.core.repository.utils.InputStreamProvider;

/**
 * @deprecated have moved to /org.talend.repository.items.importexport, but still need for MDM, so keep it temp.
 */
@Deprecated
public abstract class ResourcesManager implements InputStreamProvider{

    protected Map<IPath, Object> path2Object = new HashMap<IPath, Object>();

    public void add(String path, Object object) {
        path2Object.put(new Path(path), object);
    }

    public Set<IPath> getPaths() {
        return path2Object.keySet();
    }

    public abstract InputStream getStream(IPath path) throws IOException;

    public abstract boolean collectPath2Object(Object root);

    public abstract IImportStructureProvider getProvider();

    public abstract void closeResource();
}
