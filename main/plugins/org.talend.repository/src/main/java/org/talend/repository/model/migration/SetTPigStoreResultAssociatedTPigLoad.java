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
 * Add the property "ASSOCIATED_PIG_LOAD" to all tPigStoreResult components.
 *
 */
public class SetTPigStoreResultAssociatedTPigLoad extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        try {

            IComponentFilter filter = new NameComponentFilter("tPigStoreResult");
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        @Override
                        public void transform(NodeType node) {
                            ComponentUtilities.addNodeProperty(node, "ASSOCIATED_PIG_LOAD", "COMPONENT_LIST");
                            ComponentUtilities.getNodeProperty(node, "ASSOCIATED_PIG_LOAD").setValue("UNRELEVANT");
                        }
                    }));

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 11, 19, 12, 0, 0);
        return gc.getTime();
    }
}
