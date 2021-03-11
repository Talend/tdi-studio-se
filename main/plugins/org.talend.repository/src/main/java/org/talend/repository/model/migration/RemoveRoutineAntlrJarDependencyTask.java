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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;

/**
 * ADD sizhaoliu 2015-7-17 for TUP-3271
 */
public class RemoveRoutineAntlrJarDependencyTask extends AbstractItemMigrationTask {

    @Override
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
                return ExecutionResult.NOTHING_TO_DO;
            }
            try {
                EList<IMPORTType> imports = routineItem.getImports();
                List<IMPORTType> toRemove = new ArrayList<IMPORTType>();

                for (IMPORTType importElement : imports) {
                    if ("antlr-3.3.jar".equals(importElement.getMODULE())) { //$NON-NLS-1$
                        toRemove.add(importElement);
                    }
                }
                if (toRemove.size() > 0) {
                    imports.removeAll(toRemove);
                    ProxyRepositoryFactory.getInstance().save(routineItem);
                }
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 7, 17, 12, 0, 0);
        return gc.getTime();
    }
}
