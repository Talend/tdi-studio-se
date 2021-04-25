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
 * rdubois class global comment. Detailled comment
 */
public class SetTrimToTrueAppendModeAdvancedFileOutputXMLComponentsMigrationTask extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        try {
            setTrimToTrue(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 4, 10, 12, 0, 0);
        return gc.getTime();
    }

    private void setTrimToTrue(Item item) throws Exception {
        ProcessType processType = getProcessType(item);
        IComponentConversion addTrim = new AddTrimOption();
        IComponentFilter filter = new NameComponentFilter("tAdvancedFileOutputXML");
        ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addTrim));
    }

    /**
     *
     * AddTrimOption class global comment. Detailled comment
     */
    private class AddTrimOption implements IComponentConversion {

        private String field = "CHECK"; //$NON-NLS-1$

        private String name = "TRIM"; //$NON-NLS-1$

        public AddTrimOption() {
            super();
        }

        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, name) == null) {
                ComponentUtilities.addNodeProperty(node, name, field);
            }

            boolean isMerge = false;
            boolean isCompact = false;

            boolean mergeIsPassed = false;
            boolean compactIsPassed = false;

            for (Object o : node.getElementParameter()) {
                ElementParameterType para = (ElementParameterType) o;
                if ("MERGE".equals(para.getName())) { //$NON-NLS-1$
                    mergeIsPassed = true;
                    if ("true".equals(para.getValue())) {
                        isMerge = true;
                    }
                }

                if ("PRETTY_COMPACT".equals(para.getName())) { //$NON-NLS-1$
                    compactIsPassed = true;
                    if ("true".equals(para.getValue())) {
                        isCompact = true;
                    }
                }

                if (mergeIsPassed && compactIsPassed) {
                    break;
                }

            }

            if (isMerge && !isCompact) {
                ComponentUtilities.setNodeValue(node, name, "true");
            } else {
                ComponentUtilities.setNodeValue(node, name, "false");
            }

        }
    }
}
