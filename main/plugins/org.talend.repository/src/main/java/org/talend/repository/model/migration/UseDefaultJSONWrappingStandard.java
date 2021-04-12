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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;

public class UseDefaultJSONWrappingStandard extends AbstractJobMigrationTask {
    private static final String QUOTE_NULL_VALUES_PROPERTY_NAME = "QUOTE_NULL_VALUES";
    private static final String QUOTE_ALL_VALUES_PROPERTY_NAME = "QUOTE_ALL_VALUES";
    private static final String QUOTE_NULL_VALUES_PROPERTY_TYPE = "CHECK";
    private static final String QUOTE_NULL_VALUES_MIGRATION_VALUE = "false";

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, Calendar.OCTOBER, 24, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        org.talend.designer.core.model.utils.emf.talendfile.ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        IComponentConversion setOldStandardConversion = node -> {
            if (ComponentUtilities.getNodeProperty(node, QUOTE_NULL_VALUES_PROPERTY_NAME) == null) {
                ComponentUtilities.addNodeProperty(node, QUOTE_NULL_VALUES_PROPERTY_NAME, QUOTE_NULL_VALUES_PROPERTY_TYPE);
            }
            
            String quoteAllValuesCheckValue = ComponentUtilities.getNodeProperty(node, QUOTE_ALL_VALUES_PROPERTY_NAME).getValue();
            if ("true".equals(quoteAllValuesCheckValue)) {
                ComponentUtilities.setNodeValue(node, QUOTE_NULL_VALUES_PROPERTY_NAME, QUOTE_NULL_VALUES_MIGRATION_VALUE);
            }
            
        };
        IComponentFilter filter = new NameComponentFilter("tWriteJSONField");
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter, Collections.singletonList(setOldStandardConversion));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (PersistenceException e) {
            return ExecutionResult.FAILURE;
        }
    }
}
