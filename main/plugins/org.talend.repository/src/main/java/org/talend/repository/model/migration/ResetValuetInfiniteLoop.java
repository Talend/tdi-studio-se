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
 *
 * MOD ytao for feature 17713
 *
 */
public class ResetValuetInfiniteLoop extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] componentsName = new String[] { "tInfiniteLoop" };

        try {

            for (int i = 0; i < componentsName.length; i++) {
                IComponentFilter filter = new NameComponentFilter(componentsName[i]);
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(new IComponentConversion() {

                            public void transform(NodeType node) {
                                if (ComponentUtilities.getNodeProperty(node, "WAIT") != null) {
                                    int time = Integer.valueOf(ComponentUtilities.getNodeProperty(node, "WAIT").getValue());
                                    ComponentUtilities.addNodeProperty(node, "WAIT_MS", "TEXT");
                                    ComponentUtilities.getNodeProperty(node, "WAIT_MS").setValue(String.valueOf(time * 1000));
                                    ComponentUtilities.removeNodeProperty(node, "WAIT");
                                }
                            }

                        }));
            }

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 12, 21, 19, 14, 15);
        return gc.getTime();
    }
}
