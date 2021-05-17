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

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * <p>
 * This Task check is to rename tGenKey's customer column T_WINDOW_KEY into T_GEN_KEY, see feature:15392 @ytao
 * </p>
 */
public class RenameCustomerColumnOftGenKey extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    @Override
    public ExecutionResult execute(Item item) {
        try {
            changeCustomerColumn(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private void changeCustomerColumn(Item item) throws PersistenceException {
        ProcessType processType = getProcessType(item);

        if (processType == null) {
            return;
        }
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean modified = false;

        for (Object o : processType.getNode()) {
            NodeType node = (NodeType) o;

            for (Object o2 : node.getMetadata()) {
                MetadataType metadata = (MetadataType) o2;

                for (Object o3 : metadata.getColumn()) {
                    ColumnType column = (ColumnType) o3;

                    if ("T_WINDOW_KEY".equals(column.getName())) {
                        column.setName("T_GEN_KEY");
                        modified = true;
                    }
                }

            }
        }

        if (modified) {
            factory.save(item, true);
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 9, 14, 12, 0, 0);
        return gc.getTime();
    }
}
