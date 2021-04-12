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
package org.talend.designer.filemultischemas.data;

import org.talend.core.model.metadata.MetadataColumn;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemasMetadataColumn extends MetadataColumn {

    private MultiMetadataColumn parent; // only for schema details tree provider

    private String data;

    // private String card;

    public Integer getTagLevel() {
        if (parent != null) {
            return parent.getContainerTagLevel();
        }
        return 0;
    }

    // public String getCard() {
    // return this.card;
    // }
    //
    // public void setCard(String card) {
    // this.card = card;
    // }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public MultiMetadataColumn getParent() {
        return this.parent;
    }

    public void setParent(MultiMetadataColumn parent) {
        this.parent = parent;
    }

}
