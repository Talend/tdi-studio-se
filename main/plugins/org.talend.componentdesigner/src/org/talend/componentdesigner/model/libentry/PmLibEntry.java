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
package org.talend.componentdesigner.model.libentry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * DOC rli class global comment. Detailled comment
 */
public class PmLibEntry extends AbstractLibEntry {

    public PmLibEntry(Object obj) {
        if (obj instanceof IPath) {
            this.path = (IPath) obj;
        } else if (obj instanceof IResource) {
            this.resource = (IResource) obj;
        }
    }

    public PmLibEntry(IResource name) {
        this.resource = name;
    }

    public PmLibEntry(IPath path) {
        this.path = path;
    }

    @Override
    public int getType() {
        return PM;
    }

    public String getNamePrefix() {
        int index = this.getName().indexOf(".pm"); //$NON-NLS-1$
        return this.getName().substring(0, index);
    }

}
