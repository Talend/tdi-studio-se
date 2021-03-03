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
package org.talend.repository;

import org.talend.core.IMigrateDIMetadataItemService;
import org.talend.core.model.properties.Item;
import org.talend.migration.IMigrationTask.ExecutionResult;
import org.talend.repository.model.migration.MergeTosMetadataMigrationTask;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public class MigrationDIMetadataItemService implements IMigrateDIMetadataItemService {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.IMigrateDIMetadataItemService#migrateDIItems(org.talend.core.model.properties.Item)
     */
    public ExecutionResult migrateDIItems(Item item) {

        return new MergeTosMetadataMigrationTask().execute(item);
    }

}
