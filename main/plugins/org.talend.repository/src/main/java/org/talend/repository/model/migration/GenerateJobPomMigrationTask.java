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
package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.core.CorePlugin;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class GenerateJobPomMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 1, 23, 23, 00, 00);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        // only execute the migration task during logon, disable it for the import item (check of log finished)
        if (!ProxyRepositoryFactory.getInstance().isFullLogonFinished()) {
            CorePlugin.getDefault().getRunProcessService().generatePom(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        return ERepositoryObjectType.getAllTypesOfProcess2();
    }

}
