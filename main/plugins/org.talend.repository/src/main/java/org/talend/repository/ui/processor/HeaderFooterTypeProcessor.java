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
package org.talend.repository.ui.processor;

import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC zli class global comment. Detailled comment
 */
public class HeaderFooterTypeProcessor extends SingleTypeProcessor {

    /**
     * DOC zli HeaderFooterTypeProcessor constructor comment.
     *
     * @param repositoryType
     */
    public HeaderFooterTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.METADATA_HEADER_FOOTER;
    }

}
