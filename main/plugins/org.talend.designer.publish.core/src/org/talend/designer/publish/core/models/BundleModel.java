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
package org.talend.designer.publish.core.models;

import java.io.File;

public class BundleModel extends BaseModel {

    private final File file;

    public BundleModel(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null);
    }

    public BundleModel(String groupId, String artifactId, String version, File file) {
        super(groupId, artifactId, version);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getExtension() {
        if (null != file) {
            String filename = file.getName();
            return filename.substring(filename.lastIndexOf('.') + 1);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.publish.core.models.BaseModel#getClassifier()
     */
    @Override
    public String getClassifier() {
        return null;
    }

}
