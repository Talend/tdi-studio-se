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
package org.talend.designer.maven.job.setting.repository;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.maven.ui.setting.repository.tester.AbstractRepositoryNodeRepositorySettingTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class MavenJobRepositorySettingTester extends AbstractRepositoryNodeRepositorySettingTester {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.maven.ui.setting.repository.tester.AbstractRepositoryNodeRepositorySettingTester#getType()
     */
    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.PROCESS;
    }
}
