// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.model;

import ca.uhn.hl7v2.model.Structure;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public abstract class AbstractStructureModel implements IModel {

    protected String displayName;

    protected Structure parent;

    public String getDisplayName() {
        if (this.displayName == null) {
            generateDisplayName();
        }
        return this.displayName;
    }

    public Structure getParent() {
        return this.parent;
    }

    protected void generateDisplayName() {
    };

}
