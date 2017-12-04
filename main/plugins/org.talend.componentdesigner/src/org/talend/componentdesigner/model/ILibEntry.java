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
package org.talend.componentdesigner.model;

import org.eclipse.core.resources.IResource;

/**
 * The library entry model.
 */
public interface ILibEntry {

    /**
     * Type identifier for jar entries.
     */
    public static final int JAR = 1;

    /**
     * Type identifier for pm entries.
     */
    public static final int PM = 2;

    public IResource getResource();

    /**
     * Returns an absolute path in the local file system for this entry, or <code>null</code> if none, or if this
     * entry is of type <code>CONTAINER</code>.
     * 
     * @return an absolute path in the local file system for this entry, or <code>null</code> if none
     */
    public String getLocation();

    public String getNameAndPath(String jointMark);

    public int getType();

    public boolean isExternal();

    public String getName();

    public String getNamePrefix();

}
