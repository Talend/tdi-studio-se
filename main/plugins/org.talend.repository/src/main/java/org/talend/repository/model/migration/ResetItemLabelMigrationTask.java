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
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.utils.WorkspaceUtils;
import org.talend.migration.IProjectMigrationTask;
import org.talend.repository.items.importexport.handlers.imports.ImportBasicHandler;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class ResetItemLabelMigrationTask extends AbstractItemMigrationTask implements IProjectMigrationTask{

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 3, 13, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        IProxyRepositoryFactory repositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        ImportBasicHandler handler = new ImportBasicHandler();
        Property property = item.getProperty();
        if(property == null){
            return ExecutionResult.NOTHING_TO_DO;
        }
        String label = property.getLabel();
        if(label == null){
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            boolean isAvailable = WorkspaceUtils.checkNameIsOK(label);
            if(!isAvailable){
                property.setLabel(handler.getPropertyLabel(StringUtils.trimToNull(label)));
                property.setDisplayName(StringUtils.trimToNull(label));
                repositoryFactory.save(item, true);
            }
        } catch (PersistenceException e) {
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_WITH_ALERT;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.CONTEXT);
        return toReturn;
    }

}
