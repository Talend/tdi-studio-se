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
import java.util.Set;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ConnectionHelper;

/**
 * DOC zli class global comment. Detailled comment <br/>
 *
 */
public class ConvertLabelForConnectionItemMigrationTask extends AbstractItemMigrationTask {

    private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

    @Override
    public ExecutionResult execute(Item item) {
        boolean changed = false;
        if (item instanceof ConnectionItem) {
            ConnectionItem conItem = (ConnectionItem) item;
            Connection connection = conItem.getConnection();
            Set tables = ConnectionHelper.getTables(connection);
            for (Object tableObj : tables) {
                MetadataTable table = (MetadataTable) tableObj;
                String label = table.getLabel();
                if (label != null) {
                    String validateValue = MetadataToolHelper.validateValue(label);
                    if (validateValue != null && !label.equals(validateValue)) {
                        table.setLabel(validateValue);
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            try {
                FACTORY.save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;

    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 11, 17, 12, 0, 0);
        return gc.getTime();
    }
}
