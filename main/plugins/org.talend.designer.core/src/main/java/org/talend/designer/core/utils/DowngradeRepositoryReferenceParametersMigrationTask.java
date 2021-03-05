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
package org.talend.designer.core.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * Temporary migration linked with the feature 3310. <br>
 * This migration could be deleted later.
 */
public class DowngradeRepositoryReferenceParametersMigrationTask extends AbstractItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.JOBLET);
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ExecutionResult execute(Item item) {
        final IProxyRepositoryFactory factory = RepositoryPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        if (DowngradeParameterHelper.downgradeItem(item)) {
            try {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.NOTHING_TO_DO;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 3, 21, 12, 0, 0);
        return gc.getTime();
    }
}
