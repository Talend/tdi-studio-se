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

public class FeatureModel extends BaseModel {

    public FeatureModel(String artifactId, String version) {
        super("", artifactId, version);
    }

    public FeatureModel(String artifactId) {
        this(artifactId, "");
    }

    @Override
    public String getExtension() {
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
