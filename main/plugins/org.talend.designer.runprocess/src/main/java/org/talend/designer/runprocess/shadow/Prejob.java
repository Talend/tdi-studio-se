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
package org.talend.designer.runprocess.shadow;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class Prejob extends ShadowNode {

    /**
     * DOC Administrator Prejob constructor comment.
     *
     * @param nodeType
     */
    public Prejob(String nodeType) {
        super("tPrejob");
    }

    @Override
    public boolean isStart() {
        return true;
    }

}
