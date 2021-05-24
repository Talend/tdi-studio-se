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
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.SalesforceSchemaConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.PackageHelper;
import orgomg.cwm.resource.record.RecordFile;

/**
 * 0018470: Change salesforce system to be able to retrieve several schema in one connectio
 */
public class SalesforceMultiSchemaMigrationTask extends AbstractItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org .talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof SalesforceSchemaConnectionItem) {
            boolean modify = false;
            SalesforceSchemaConnectionItem salesforceItem = (SalesforceSchemaConnectionItem) item;
            if (salesforceItem.getConnection() instanceof SalesforceSchemaConnection) {
                SalesforceSchemaConnection sfConnection = (SalesforceSchemaConnection) salesforceItem.getConnection();
                SalesforceModuleUnit module = ConnectionFactory.eINSTANCE.createSalesforceModuleUnit();
                module.setId(ProxyRepositoryFactory.getInstance().getNextId());
                String moduleName = sfConnection.getModuleName();
                if (moduleName == null || "".equals(moduleName)) {
                    moduleName = "Default";
                }
                module.setLabel(moduleName);
                module.setModuleName(moduleName);
                EList<orgomg.cwm.objectmodel.core.Package> originalPackages = sfConnection.getDataPackage();
                for (int i = 0; i < originalPackages.size(); i++) {
                    MetadataTable table = (MetadataTable) originalPackages.get(i).getOwnedElement().get(0);
                    if (originalPackages.get(i) instanceof RecordFile) {
                        table.setLabel(moduleName);
                    }
                    module.getTables().add(table);
                }
                sfConnection.getModules().add(module);
                modify = true;
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
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 3, 27, 18, 0, 0);
        return gc.getTime();
    }
}
