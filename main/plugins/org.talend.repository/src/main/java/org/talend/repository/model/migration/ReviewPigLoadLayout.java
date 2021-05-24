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

import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Migration task that reviews the tPigLoad layout.
 */
public class ReviewPigLoadLayout extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType != null) {
            try {
                IComponentFilter filter = new NameComponentFilter("tPigLoad"); //$NON-NLS-1$
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(new IComponentConversion() {

                            @Override
                            public void transform(NodeType node) {
                                ElementParameterType mr = ComponentUtilities.getNodeProperty(node, "MAPREDUCE"); //$NON-NLS-1$
                                ElementParameterType tez = ComponentUtilities.getNodeProperty(node, "TEZ"); //$NON-NLS-1$
                                ComponentUtilities.addNodeProperty(node, "ENGINE", "CLOSED_LIST"); //$NON-NLS-1$ //$NON-NLS-2$
                                if ("true".equalsIgnoreCase(mr.getValue())) { //$NON-NLS-1$
                                    ComponentUtilities.getNodeProperty(node, "ENGINE").setValue("MAPREDUCE"); //$NON-NLS-1$ //$NON-NLS-2$
                                } else if ("true".equalsIgnoreCase(tez.getValue())) { //$NON-NLS-1$
                                    ComponentUtilities.getNodeProperty(node, "ENGINE").setValue("TEZ"); //$NON-NLS-1$ //$NON-NLS-2$
                                }

                                ComponentUtilities.removeNodeProperty(node, "MAPREDUCE"); //$NON-NLS-1$
                                ComponentUtilities.removeNodeProperty(node, "TEZ"); //$NON-NLS-1$
                            }
                        }));
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception e) {
                CommonExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 10, 16, 14, 0, 0);
        return gc.getTime();
    }
}
