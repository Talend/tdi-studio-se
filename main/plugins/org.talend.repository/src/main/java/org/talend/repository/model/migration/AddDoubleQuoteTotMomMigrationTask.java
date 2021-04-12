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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.AddQuotesInPropertyComponentConversion;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC s class global comment. Detailled comment
 */
public class AddDoubleQuoteTotMomMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
		if (getProject().getLanguage() != ECodeLanguage.JAVA
				|| processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {

            IComponentFilter filter1 = new NameComponentFilter("tMomInput"); //$NON-NLS-1$
            IComponentFilter filter2 = new NameComponentFilter("tMomOutput"); //$NON-NLS-1$

            IComponentConversion addQuotesFrom = new AddQuotesInPropertyComponentConversion("FROM"); //$NON-NLS-1$
            IComponentConversion addQuotesTo = new AddQuotesInPropertyComponentConversion("TO"); //$NON-NLS-1$

            IComponentConversion addQuotes1 = new AddQuotesInPropertyComponentConversion("SERVERADDRESS"); //$NON-NLS-1$
            IComponentConversion addQuotes2 = new AddQuotesInPropertyComponentConversion("SERVERPORT"); //$NON-NLS-1$
            IComponentConversion addQuotes3 = new AddQuotesInPropertyComponentConversion("CHANNEL"); //$NON-NLS-1$
            IComponentConversion addQuotes4 = new AddQuotesInPropertyComponentConversion("QM"); //$NON-NLS-1$
            IComponentConversion addQuotes5 = new AddQuotesInPropertyComponentConversion("QUEUE"); //$NON-NLS-1$

            ModifyComponentsAction.searchAndModify(item, processType, filter1,
					Arrays.<IComponentConversion> asList(addQuotes1,
							addQuotes2,
                    addQuotes3, addQuotes4, addQuotes5, addQuotesFrom));

            ModifyComponentsAction.searchAndModify(item, processType, filter2,
					Arrays.<IComponentConversion> asList(addQuotes1,
							addQuotes2,
                    addQuotes3, addQuotes4, addQuotes5, addQuotesTo));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 3, 28, 12, 0, 0);
        return gc.getTime();
    }

}
