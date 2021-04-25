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
package org.talend.designer.core.model.components;

import org.talend.commons.ui.runtime.image.IImage;
import org.talend.designer.core.DesignerPlugin;

/**
 *
 * DOC smallet ImageProvider class global comment. Detailled comment <br/>
 *
 * $Id: ImageProvider.java 418 2006-11-13 16:01:26 +0000 (lun., 13 nov. 2006) cantoine $
 *
 */
public enum EComponentsImage implements IImage {

    DEFAULT_COMPONENT_ICON("/icons/default-32.png"), //$NON-NLS-1$

    ;

    private String path;

    EComponentsImage() {
        this.path = "/icons/unknown.gif"; //$NON-NLS-1$
    }

    EComponentsImage(String path) {
        this.path = path;
    }

    /**
     * Getter for path.
     *
     * @return the path
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Getter for clazz.
     *
     * @return the clazz
     */
    @Override
    public Class getLocation() {
        return DesignerPlugin.class;
    }

}
