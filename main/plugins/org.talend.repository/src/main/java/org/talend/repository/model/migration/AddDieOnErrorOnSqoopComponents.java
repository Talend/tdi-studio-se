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
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * This migration task add the die on error option on the sqoop components. It reverses the default value (from false to
 * true) in case of the java API is used.
 *
 * DOC rdubois class global comment. Detailled comment
 */

public class AddDieOnErrorOnSqoopComponents extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            String[] arrSqoopComponentsName = new String[] {
                    "tSqoopImport", "tSqoopExport", "tSqoopMerge", "tSqoopImportAllTables" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            for (String sqoopComponentsName : arrSqoopComponentsName) {
                IComponentFilter filter = new NameComponentFilter(sqoopComponentsName);
                IComponentConversion addOption = new AddDieOnErrorOptionConversion();
                ModifyComponentsAction
                        .searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 8, 11, 17, 00, 00);
        return gc.getTime();
    }

    /**
     * DOC rdubois AddDieOnErrorOptionConversion class global comment. Detailled comment
     */
    private class AddDieOnErrorOptionConversion implements IComponentConversion {

        private String field = "CHECK"; //$NON-NLS-1$

        private String name = "DIE_ON_ERROR"; //$NON-NLS-1$

        private String mode = "USE_COMMANDLINE"; //$NON-NLS-1$

        public AddDieOnErrorOptionConversion() {
            super();
        }

        @Override
        public void transform(NodeType node) {
            if (ComponentUtilities.getNodeProperty(node, name) == null) {
                ComponentUtilities.addNodeProperty(node, name, field);
                ComponentUtilities.setNodeValue(node, name, "true"); //$NON-NLS-1$
                ElementParameterType modeType = ComponentUtilities.getNodeProperty(node, mode);
                if (modeType != null && "true".equals(modeType.getValue())) { //$NON-NLS-1$
                    ComponentUtilities.setNodeValue(node, name, "false"); //$NON-NLS-1$
                }
            }
        }
    }
}
