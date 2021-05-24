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
package org.talend.sqlbuilder.sqleditor;

import org.eclipse.swt.graphics.RGB;

/**
 * For internal use only. Not API.
 * <p>
 * A color manager extension is for extending <code>IColorManager</code> instances with new functionality.
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 * @author qiang.zhang
 */
public interface IColorManagerExtension {

    /**
     * Remembers the given color specification under the given key.
     *
     * @param key the color key
     * @param rgb the color specification
     * @exception UnsupportedOperationException if there is already a color specification remembered under the given key
     */
    void bindColor(String key, RGB rgb);

    /**
     * Forgets the color specification remembered under the given key.
     *
     * @param key the color key
     */
    void unbindColor(String key);
}
