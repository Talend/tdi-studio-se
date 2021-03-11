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
package org.talend.sqlbuilder.dbstructure;

import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.RepositoryObject;

/**
 * SqlBuilderRepositoryObject.
 *
 */
public class SqlBuilderRepositoryObject extends RepositoryObject {

    private String repositoryName;

    private String sourceName;

    private String image;

    private String color;

    private boolean isBuildIn;

    private String diffColor;

    private String diffImage;

    private boolean isDiffDevergency;

    private boolean isDiffSynchronised;

    public SqlBuilderRepositoryObject(Property property) {
        super(property);
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public boolean isBuildIn() {
        return isBuildIn;
    }

    public void setBuildIn(boolean buildInValue) {
        this.isBuildIn = buildInValue;
    }

    @Override
    public String getLabel() {
        return repositoryName;
    }

    public String getDiffColor() {
        return diffColor;
    }

    public void setDiffColor(String diffColor) {
        this.diffColor = diffColor;
    }

    public String getDiffImage() {
        return diffImage;
    }

    public void setDiffImage(String diffImage) {
        this.diffImage = diffImage;
    }

    public boolean isDiffDevergency() {
        return isDiffDevergency;
    }

    public void setDiffDevergency(boolean judgeDiffDevergency) {
        this.isDiffDevergency = judgeDiffDevergency;
    }

    public boolean isDiffSynchronised() {
        return isDiffSynchronised;
    }

    public void setDiffSynchronised(boolean judgeDiffSynchronised) {
        this.isDiffSynchronised = judgeDiffSynchronised;
    }

}
