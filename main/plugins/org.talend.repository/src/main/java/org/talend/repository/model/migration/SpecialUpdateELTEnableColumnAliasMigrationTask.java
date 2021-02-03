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

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractAllJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by hcyi on Jan 7, 2021
 * Detailled comment
 *
 */
public class SpecialUpdateELTEnableColumnAliasMigrationTask extends AbstractAllJobMigrationTask {

    private static final String ELT_ORACLE_DISABLE_COLUMN_ALIAS = "elt.oracle.disableColumnAlias"; //$NON-NLS-1$

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        boolean disableOracleAlias = Boolean.valueOf(System.getProperty(ELT_ORACLE_DISABLE_COLUMN_ALIAS, Boolean.FALSE.toString()));
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null || disableOracleAlias) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        IComponentFilter filter = new NameComponentFilter("tELTOracleMap"); //$NON-NLS-1$
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            ComponentUtilities.setNodeValue(node, "USE_ALIAS_IN_OUTPUT_TABLE", String.valueOf(!disableOracleAlias)); //$NON-NLS-1$
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, 01, 07, 12, 0, 0);
        return gc.getTime();
    }
}
