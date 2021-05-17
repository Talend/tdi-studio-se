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
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.IProxyRepositoryFactory;


/**
 * ggu class global comment. Detailled comment.
 *
 * such as: replace old script code ("(String)context.getProperty("var1")") to new one ("context.var1")
 */
public class ReplaceOldContextScriptCodeMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);

        if (getProject().getLanguage() == ECodeLanguage.PERL ||  processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        boolean changed = UpdateContextVariablesHelper.updateProcessForReplacedOldScriptCode(processType);

        if (changed) {
            try {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
