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
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ReplaceSpaceCharForItemNameMigrationTask extends AbstractItemMigrationTask {

    private static final IProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.CONTEXT);

        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_DELIMITED);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_POSITIONAL);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_REGEXP);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_XML);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_EXCEL);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_LDIF);
        toReturn.add(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        toReturn.add(ERepositoryObjectType.METADATA_LDAP_SCHEMA);
        toReturn.add(ERepositoryObjectType.METADATA_WSDL_SCHEMA);
        toReturn.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);

        toReturn.add(ERepositoryObjectType.DOCUMENTATION);
        toReturn.remove(null); // remove all null
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        final String space = " "; //$NON-NLS-1$

        try {
            Property property = item.getProperty();

            String name = property.getLabel();
            if (name != null && name.contains(space)) {
                name = name.replaceAll(space, "_"); //$NON-NLS-1$
                property.setLabel(name);

                fakeFunction(item);
                FACTORY.save(item);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            }

            return ExecutionResult.NOTHING_TO_DO;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     *
     * ggu Comment method "fakeFunction".
     *
     * nothing to do, but if there is nothing to do, the item will be empty.
     */
    private void fakeFunction(Item item) {
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
        if (itemType == ERepositoryObjectType.CONTEXT) {
            EList context = ((ContextItem) item).getContext();
            if (context != null) {
                context.get(0);
            }
        } else if (itemType == ERepositoryObjectType.METADATA_CONNECTIONS
                || itemType == ERepositoryObjectType.METADATA_FILE_DELIMITED
                || itemType == ERepositoryObjectType.METADATA_FILE_POSITIONAL
                || itemType == ERepositoryObjectType.METADATA_FILE_REGEXP || itemType == ERepositoryObjectType.METADATA_FILE_XML
                || itemType == ERepositoryObjectType.METADATA_FILE_EXCEL || itemType == ERepositoryObjectType.METADATA_FILE_LDIF
                || itemType == ERepositoryObjectType.METADATA_GENERIC_SCHEMA
                || itemType == ERepositoryObjectType.METADATA_LDAP_SCHEMA
                || itemType == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA
                || itemType == ERepositoryObjectType.METADATA_WSDL_SCHEMA) {
            ((ConnectionItem) item).getConnection();
        } else if (itemType == ERepositoryObjectType.DOCUMENTATION) {
            ((FileItem) item).getContent();
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
