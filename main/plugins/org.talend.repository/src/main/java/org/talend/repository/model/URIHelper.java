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
package org.talend.repository.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class URIHelper {

    public static IFile getFile(URI uri) {
        return org.talend.core.repository.utils.URIHelper.getFile(uri);
    }

    public static IPath convert(URI uri) {
        return org.talend.core.repository.utils.URIHelper.convert(uri);
    }

    public static URI convert(IPath path) {
        return org.talend.core.repository.utils.URIHelper.convert(path);
    }
}
