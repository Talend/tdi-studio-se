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

public abstract class BaseModel {

    private final String groupId;

    private final String artifactId;

    private final String version;

    BaseModel(String groupId, String artifactId, String version) {
        if (groupId == null || artifactId == null || version == null) {
            throw new IllegalArgumentException();
        }
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public abstract String getExtension();

    public abstract String getClassifier();

    public String getRepositoryLocation(String overrideExtension) {
        return new StringBuilder().append('/').append(getGroupId().replace('.', '/')).append('/').append(getArtifactId())
                .append('/').append(getVersion()).append('/').append(getArtifactId()).append('-').append(getVersion())
                .append('.').append((null == overrideExtension) ? getExtension() : overrideExtension).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        if (obj instanceof BaseModel) {
            BaseModel model = (BaseModel) obj;
            return groupId.equals(model.getGroupId()) && artifactId.equals(model.getArtifactId());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (groupId + artifactId).hashCode();
    }

}
