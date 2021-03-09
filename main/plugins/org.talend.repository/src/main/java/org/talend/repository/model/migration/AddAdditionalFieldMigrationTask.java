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

import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;

/**
 * Migration task added only to avoid people to import jobs with additionalField in a version who doesn't contain the
 * additionalField.<br>
 * If any job is imported in an old version, there will be an exception because additionalField is not existing in the
 * old emf model.
 */
public class AddAdditionalFieldMigrationTask extends AbstractItemMigrationTask {

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 6, 25, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        return ExecutionResult.NOTHING_TO_DO;
    }

}
