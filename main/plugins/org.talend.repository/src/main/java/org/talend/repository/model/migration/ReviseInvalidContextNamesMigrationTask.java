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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

/**
 *
 * created by ycbai on 2016年7月8日 Detailled comment
 *
 */
public class ReviseInvalidContextNamesMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 7, 8, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        boolean modified = false;
        if (item instanceof ContextItem) {
            ContextItem contextItem = (ContextItem) item;
            List<ContextType> contextTypes = contextItem.getContext();
            for (ContextType contextType : contextTypes) {
                List<ContextParameterType> contextParameterTypes = contextType.getContextParameter();
                for (ContextParameterType contextParameterType : contextParameterTypes) {
                    String name = contextParameterType.getName();
                    if (!ContextParameterUtils.isValidParameterName(name)) {
                        String newName = ContextParameterUtils.getValidParameterName(name);
                        contextParameterType.setName(newName);
                        modified = true;
                    }
                }
            }
        }
        if (modified) {
            try {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
