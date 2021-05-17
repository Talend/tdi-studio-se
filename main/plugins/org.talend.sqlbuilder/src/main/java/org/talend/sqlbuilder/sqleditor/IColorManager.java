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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Manages SWT color objects for given color keys and given <code>RGB</code> objects. Until the <code>dispose</code>
 * method is called, the same color object is returned for equal keys and equal <code>RGB</code> values.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IJavaColorConstants
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 * @author qiang.zhang
 */
public interface IColorManager {

    /**
     * Returns a color object for the given key. The color objects are remembered internally; the same color object is
     * returned for equal keys.
     *
     * @param key the color key
     * @return the color object for the given key
     */
    Color getColor(String key);

    /**
     * Returns the color object for the value represented by the given <code>RGB</code> object.
     *
     * @param rgb the rgb color specification
     * @return the color object for the given rgb value
     */
    Color getColor(RGB rgb);

    /**
     * Disposes all color objects remembered by this color manager.
     */
    void dispose();
}
