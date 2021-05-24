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
package org.talend.repository.resource.ui.util;

import org.talend.commons.ui.runtime.image.IImage;

/**
 *
 * DOC jding class global comment. Detailled comment
 */
public enum EResourcesImage implements IImage {
    RESOURCE_ICON("/icons/resource.png");//$NON-NLS-1$

    private String path;

    EResourcesImage(String path) {
        this.path = path;
    }

    @Override
    public Class<?> getLocation() {
        return EResourcesImage.class;
    }

    @Override
    public String getPath() {
        return this.path;
    }

}
