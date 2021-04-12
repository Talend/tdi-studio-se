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
package org.talend.repository.view.di.viewer.content;

import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobDesignsContentProviderTest extends AbstractRootNodeContentProviderTest {

    @Override
    protected boolean isRoot() {
        return true;
    }

    @Override
    protected ERepositoryObjectType getTestType() {
        return ERepositoryObjectType.PROCESS;
    }
}
