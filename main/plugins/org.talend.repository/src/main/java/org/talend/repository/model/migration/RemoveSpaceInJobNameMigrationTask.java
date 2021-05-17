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

/**
 * Task replace run before and after with then run connection.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ChangeRunBeforeAfterToThenRunMigrationTask.java 下午04:41:56 2007-5-17 +0000 (2007-5-17) yzhang $
 *
 */
public class RemoveSpaceInJobNameMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    public ExecutionResult execute(Item item) {

        try {
            renameJobs(item);
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

    /**
     * Rename jobs if needed, remove spaces and replace them by "_". Note that this migration is only for 1.0 or 1.1 to
     * more recent version, after the 1.1 it was not possible to add some spaces.
     *
     * @throws PersistenceException
     */
    private void renameJobs(Item item) throws PersistenceException {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        if (item.getProperty().getLabel().contains(" ")) { // if the job contain some spaces //$NON-NLS-1$
            item.getProperty().setLabel(item.getProperty().getLabel().replaceAll(" ", "_")); //$NON-NLS-1$ //$NON-NLS-2$
            factory.save(item,true);
        }
    }
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
