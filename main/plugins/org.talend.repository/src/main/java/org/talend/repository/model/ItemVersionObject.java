// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model;

import org.eclipse.jdt.internal.core.util.Util;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class ItemVersionObject {

    private Property property;

    private RepositoryNode repositoryNode;

    private String oldVersion;

    private String newVersion;

    public ItemVersionObject(Property property, RepositoryNode repositoryNode, String oldVersion) {
        super();
        this.repositoryNode = repositoryNode;
        this.property = property;
        this.oldVersion = oldVersion;
        this.newVersion = oldVersion; // init
    }

    public String getNewVersion() {
        return this.newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public Item getItem() {
        return this.property.getItem();
    }

    public RepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }

    public String getOldVersion() {
        return this.oldVersion;
    }

    @SuppressWarnings("restriction")
    @Override
    public int hashCode() {
        return Util.combineHashCodes(getRepositoryNode().getId().hashCode(), getOldVersion().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ItemVersionObject) {
            ItemVersionObject tObj = (ItemVersionObject) obj;
            if (tObj.getRepositoryNode().getId().equals(getRepositoryNode().getId())
                    && tObj.getItem().getProperty().getVersion().equals(property.getVersion())) {
                return true;
            }
        }
        return false;
    }

}
