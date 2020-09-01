// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.items.importexport.handlers.imports.ImportCacheHelper;

public class CreateContextLinkMigrationTask extends AbstractItemMigrationTask {

    protected ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public List<ERepositoryObjectType> getTypes() {
        return ContextUtils.getAllSupportContextLinkTypes();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
	public ExecutionResult execute(Item item) {
		if (factory.isFullLogonFinished()) { // For import into opening project, we will create context link later. Please refer to ImportBasicHandler-->afterImportingItems
			return ExecutionResult.NOTHING_TO_DO;
		}
		return ContextUtils.createContextLinkForItem(item, ImportCacheHelper.getInstance().getCachedContextIdToItemMap());
	}

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 04, 02, 12, 0, 0);
        return gc.getTime();
    }
}
