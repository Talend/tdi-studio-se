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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC zwang class global comment. Detailled comment
 */
public class RenameRoutinesClassName extends AbstractItemMigrationTask {

    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.ROUTINES);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (getProject().getLanguage() == ECodeLanguage.JAVA) {
            RoutineItem routineItem = (RoutineItem) item;
            if (routineItem.isBuiltIn()) {
                // if it's a system routine, no migration needed.
                return ExecutionResult.NOTHING_TO_DO;
            }
            try {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                ByteArray content = routineItem.getContent();
                String str = new String(content.getInnerContent());
                String string = item.getProperty().getLabel();
                str = str.replaceAll("__CLASS_NAME__", string); //$NON-NLS-1$
                content.setInnerContent(str.getBytes());
                routineItem.setContent(content);
                factory.save(routineItem);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
