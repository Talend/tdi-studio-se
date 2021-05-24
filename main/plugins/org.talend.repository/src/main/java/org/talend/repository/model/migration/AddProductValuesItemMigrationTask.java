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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.repository.item.ItemProductValuesHelper;

/**
 * DOC ggu class global comment. Detailled comment
 *
 * must be in beforeLogon, because will update the product values when logOnProject
 */
public class AddProductValuesItemMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 11, 13, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        try {
            // don't migrate for system item
            boolean invalid = false;
            if (item instanceof RoutineItem) {
                invalid = ((RoutineItem) item).isBuiltIn();
            } else if (item instanceof SQLPatternItem) {
                invalid = ((SQLPatternItem) item).isSystem();
            }

            Property property = item.getProperty();
            if (!invalid && ItemProductValuesHelper.setValuesWhenMigrate(property)) {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
