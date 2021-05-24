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
package org.talend.repository.generic.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * created by ycbai on 2016年10月8日 Detailled comment
 *
 */
public class RepTypeMappingManagerTest {

    @Test
    public void testGetNewRepType() {
        ERepositoryObjectType newSFRepType = RepTypeMappingManager.getInstance().getNewRepType("METADATA_SALESFORCE_SCHEMA"); //$NON-NLS-1$
        if (newSFRepType != null) {
            assertEquals("salesforce", newSFRepType.getType()); //$NON-NLS-1$
        }
        ERepositoryObjectType newFDRepType = RepTypeMappingManager.getInstance().getNewRepType("METADATA_FILE_DELIMITED"); //$NON-NLS-1$
        if (newFDRepType != null) {
            assertEquals("fileDelimited", newFDRepType.getType()); //$NON-NLS-1$
        }
    }

}
