// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.migration.IProjectMigrationTask;
import org.talend.repository.model.RepositoryConstants;

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
        Property property = item.getProperty();
        if(property == null){
            return ExecutionResult.NOTHING_TO_DO;
        }
        String label = property.getLabel();
        if(label == null){
            return ExecutionResult.NOTHING_TO_DO;
        }
        property.setLabel(getPropertyLabel(getPropertyLabel(StringUtils.trimToNull(label))));
        property.setDisplayName(StringUtils.trimToNull(label));
        return ExecutionResult.SUCCESS_WITH_ALERT;
    }
    
    private String getPropertyLabel(String name) {
        String label = name;
        for (String toReplace : RepositoryConstants.ITEM_FORBIDDEN_IN_LABEL) {
            label = label.replace(toReplace, "_"); //$NON-NLS-1$
        }
        return label;
    }

}
