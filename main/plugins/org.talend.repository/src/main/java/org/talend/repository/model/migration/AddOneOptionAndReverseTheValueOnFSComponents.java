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
 * there will be a new option on FS components since 3.2, see revision r30399, and the default status(unchecked) of the
 * option will due to the behavior of jobs changed which create by 3.1 or the version before. so add this task to help
 * the user upgrade their jobs. <br/>
 *
 * add option on FS components and set the option checked.<br/>
 *
 * DOC ytao class global comment. Detailled comment
 */

public class AddOneOptionAndReverseTheValueOnFSComponents extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {

            //            String[] arrFSComponentsName = new String[] { "tFSAggregate", "tFSCheck", "tFSCode", "tFSFilterColumns", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            //                    "tFSFilterRows", "tFSTransform", "tFSUnique" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String[] arrFSComponentsName = new String[] { "tFSAggregate", "tFSCheck", "tFSCode", "tFSFilterColumns", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    "tFSFilterRows", "tFSTransform", "tFSUnique", "tFSJoin" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            for (String fsComponentsName : arrFSComponentsName) {
                IComponentFilter filter = new NameComponentFilter(fsComponentsName);
                IComponentConversion addOption = new AddSortResultsOptionConversion();
                ModifyComponentsAction
                        .searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 9, 30, 15, 41, 10);
        return gc.getTime();
    }

    /**
     * DOC ytao AddOneOptionAndReverseTheValueOnFSComponents class global comment. Detailled comment
     */
    private class AddSortResultsOptionConversion implements IComponentConversion {

        private String field = "CHECK"; //$NON-NLS-1$

        private String name = "SORT_RESULTS"; //$NON-NLS-1$

        public AddSortResultsOptionConversion() {
            super();
        }

        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, name) == null) {

                ComponentUtilities.addNodeProperty(node, name, field);
                ComponentUtilities.setNodeValue(node, name, "true"); //$NON-NLS-1$

            }
        }
    }

}
