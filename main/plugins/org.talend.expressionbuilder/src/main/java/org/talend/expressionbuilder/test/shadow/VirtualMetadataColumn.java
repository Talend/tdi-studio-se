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
package org.talend.expressionbuilder.test.shadow;

import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: VirtualMetadataColumn.java 下午03:42:46 2007-7-24 +0000 (2007-7-24) yzhang $
 *
 */
public class VirtualMetadataColumn extends MetadataColumnExt {

    /**
     * yzhang VirtualMetadataColumn constructor comment.
     */
    public VirtualMetadataColumn() {
        this.setComment(""); //$NON-NLS-1$
        this.setDefault(""); //$NON-NLS-1$
        this.setLabel("newColumn"); //$NON-NLS-1$
        this.setTalendType("id_String"); //$NON-NLS-1$
        this.setType(""); //$NON-NLS-1$
        this.setId(String.valueOf(0));

        this.setLength(null);
        this.setPrecision(null);
        this.setDefault(""); //$NON-NLS-1$

        this.setKey(false);

        this.setNullable(false);
        this.setPattern(""); //$NON-NLS-1$
    }
}
