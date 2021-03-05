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
package org.talend.repository.json.util;

import org.talend.commons.ui.runtime.image.IImage;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public enum JSONImage implements IImage {
    JSON_ICON("/icons/JsonFile_icon16.png"),

    JSON_ICON32("/icons/JsonFile_icon32.png");

    private String path;

    JSONImage() {
        this.path = "/icons/JsonFile_icon32.png"; //$NON-NLS-1$
    }

    JSONImage(String path) {
        this.path = path;
    }

    @Override
    public Class getLocation() {
        return JSONImage.class;
    }

    @Override
    public String getPath() {
        return this.path;
    }

}
