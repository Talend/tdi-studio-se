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
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.model.ILibEntry;

/**
 * DOC rli class global comment. Detailled comment
 */
public abstract class AbstractLibEntry implements ILibEntry {

    protected IResource resource;

    protected IPath path;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.model.ILibEntry#getResource()
     */
    public IResource getResource() {
        return resource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.model.ILibEntry#getLocation()
     */
    public String getLocation() {
        if (resource != null) {
            return resource.getProjectRelativePath().toString();
        } else if (path != null) {
            return path.toOSString();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.model.ILibEntry#getNameAndPath(java.lang.String)
     */
    public String getNameAndPath(String jointMark) {
        StringBuffer resultString = new StringBuffer();
        if (resource != null) {
            resultString.append(resource.getName());
            resultString.append(jointMark);
            resultString.append(resource.getProjectRelativePath());
            return resultString.toString();
        } else if (path != null) {
            resultString.append(path.lastSegment());
            resultString.append(jointMark);
            resultString.append(path.toOSString());
            return resultString.toString();
        } else {
            return resultString.toString();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.model.ILibEntry#getType()
     */
    public int getType() {
        return 0;
    }

    public boolean isExternal() {
        return this.resource == null;
    }

    public String getName() {
        if (resource != null) {
            return resource.getName();
        } else if (path != null) {
            return path.lastSegment();
        } else {
            return PluginConstant.EMPTY_STRING;
        }
    }

}
