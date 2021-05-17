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

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC ldong class global comment. Detailled comment <br/>
 */
public class AddContextGroupNameMigrationTask extends AbstractItemMigrationTask {

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 11, 24, 17, 50, 15);
        return gc.getTime();
    }

    public ExecutionResult execute(Item item) {
        try {
            boolean modified = addContextGroupName(item);
            if (modified) {
                return ExecutionResult.SUCCESS_NO_ALERT;
            } else {
                return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private boolean addContextGroupName(Item item) throws PersistenceException {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean modified = false;
        if (item instanceof ConnectionItem) {
            Connection con = ((ConnectionItem) item).getConnection();
            ContextItem originalItem = ContextUtils.getContextItemById2(con.getContextId());
            if (originalItem != null) {
                if (con.getContextName() == null || "".equals(con.getContextName())) {
                    con.setContextName(originalItem.getDefaultContext());
                    factory.save(item);
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }

}
