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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.service.IDQComponentService;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Make sure the attribute of tMatchGroup component have a good style
 */
public class CleanCustomerMatcherField extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 5, 10, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            IComponentFilter filter = new NameComponentFilter("tMatchGroup"); //$NON-NLS-1$
            IComponentConversion checkQuotesForTheField = new CheckQuotesForTheField();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter,
                            Arrays.<IComponentConversion> asList(checkQuotesForTheField));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class CheckQuotesForTheField implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDQComponentService.class)) {
                final IDQComponentService service =
                        GlobalServiceRegister.getDefault().getService(IDQComponentService.class);
                service.correctlyCustomMatcherParameter(node);
            }
        }
    }

}
