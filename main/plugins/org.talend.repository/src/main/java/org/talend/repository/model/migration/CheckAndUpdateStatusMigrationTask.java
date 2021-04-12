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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.Status;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class CheckAndUpdateStatusMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 3, 15, 17, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (ERepositoryObjectType.getItemType(item).isDQItemType() && !ERepositoryObjectType.getItemType(item).isDIItemType()) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            boolean modified = updateStatus(item);
            if (modified) {
                return ExecutionResult.SUCCESS_NO_ALERT;
            } else {
                return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private boolean updateStatus(Item item) throws PersistenceException {
        boolean modified = false;
        Property property = item.getProperty();
        if (property == null || StringUtils.trimToNull(property.getStatusCode()) == null) {
            return modified;
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        List<Status> technicalStatus = factory.getTechnicalStatus();
        List<Status> documentationStatus = factory.getDocumentationStatus();
        Map<String, Status> codeToStatusMap = new HashMap<String, Status>();
        Map<String, Status> labelToStatusMap = new HashMap<String, Status>();
        for (Status status : technicalStatus) {
            codeToStatusMap.put(status.getCode(), status);
            labelToStatusMap.put(status.getLabel(), status);
        }
        for (Status status : documentationStatus) {
            codeToStatusMap.put(status.getCode(), status);
            labelToStatusMap.put(status.getLabel(), status);
        }
        String statusCode = property.getStatusCode();
        if (codeToStatusMap.get(statusCode) == null) {
            Status invalidStatus = labelToStatusMap.get(statusCode);
            if (invalidStatus == null) {
                property.setStatusCode(""); //$NON-NLS-1$
            } else {
                property.setStatusCode(invalidStatus.getCode());
            }
            factory.save(item);
            modified = true;
        }

        return modified;
    }

}
