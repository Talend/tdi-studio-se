// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.views.jobsettings;

import org.talend.commons.ui.runtime.image.IImage;

/**
 * DOC marvin class global comment. Detailled comment
 */
public enum EJobSettingImage implements IImage {

    PROCESS_MR_ICON_X16("/icons/process_mr.png"), //$NON-NLS-1$
    PROCESS_SPARK_ICON_X16("/icons/process_spark_icon.png"), //$NON-NLS-1$
    PROCESS_SPARK_STREAMING_ICON_X16("/icons/process_spark_streaming.png"), //$NON-NLS-1$
    PROCESS_ICON_X16("/icons/process_icon.png"), //$NON-NLS-1$
    ;

    private String path;

    EJobSettingImage(String path) {
        this.path = path;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.runtime.image.IImage#getLocation()
     */
    @Override
    public Class getLocation() {
        return EJobSettingImage.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.runtime.image.IImage#getPath()
     */
    @Override
    public String getPath() {
        return path;
    }

}
