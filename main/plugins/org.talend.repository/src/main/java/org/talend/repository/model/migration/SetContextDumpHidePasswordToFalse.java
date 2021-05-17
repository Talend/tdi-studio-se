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
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * jjzhou class global comment. Detailled comment
 */
public class SetContextDumpHidePasswordToFalse extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        try {
            setHiddenPasswordtoFalse(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }


    private void setHiddenPasswordtoFalse(Item item) throws Exception {
        ProcessType processType = getProcessType(item);
        IComponentConversion SetHiddenPasswordFalse = new SetHiddenPasswordFalse();
        IComponentFilter filter = new NameComponentFilter("tContextDump");//$NON-NLS-1$
        ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(SetHiddenPasswordFalse));
    }

    private class SetHiddenPasswordFalse implements IComponentConversion {

        private String field = "CHECK"; //$NON-NLS-1$

        private String name = "HIDE_PASSWORD"; //$NON-NLS-1$

        public SetHiddenPasswordFalse() {
            super();
        }

        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, name) == null) {
                ComponentUtilities.addNodeProperty(node, name, field);
                ComponentUtilities.setNodeValue(node, name, "false");//$NON-NLS-1$
            }

        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 02, 14, 19, 0, 0);
        return gc.getTime();
    }
}
