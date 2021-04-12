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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MdmConceptType;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC talend class global comment. Detailled comment
 */
public class MdmConceptMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof MDMConnectionItem) {
            boolean modify = false;
            MDMConnectionItem mdmConnItem = (MDMConnectionItem) item;
            if (mdmConnItem.getConnection() instanceof MDMConnection) {
                MDMConnection mdmConnection = (MDMConnection) mdmConnItem.getConnection();
                final EList<Concept> schemas = mdmConnection.getSchemas();
                for (Concept concept : schemas) {
                    if (!concept.isInputModel()) {
                        concept.setConceptType(MdmConceptType.OUTPUT);
                        modify = true;
                    }
                }

            }
            if (modify) {
                try {
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    factory.save(item, true);
                    return ExecutionResult.SUCCESS_WITH_ALERT;
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 9, 20, 12, 0, 0);
        return gc.getTime();
    }

}
