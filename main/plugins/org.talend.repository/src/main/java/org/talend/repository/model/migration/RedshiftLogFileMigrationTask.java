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
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Redshift set use log file to true when import from 6.5.1 and 7.0.1
 */
public class RedshiftLogFileMigrationTask extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(final Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] comps = { "tRedshiftConnection", "tRedshiftInput" };

        for (String component : comps) {
            IComponentFilter filter = new NameComponentFilter(component); // $NON-NLS-1$
            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion>asList(new IComponentConversion() {

                            @Override
                            public void transform(NodeType node) {
                                if (ComponentUtilities.getNodeProperty(node, "USE_LOG_FILE") == null 
                                		&& ComponentUtilities.getNodeProperty(node, "LOG_FILE") != null) {//$NON-NLS-1$
                                    ComponentUtilities.addNodeProperty(node, "USE_LOG_FILE", "CHECK");//$NON-NLS-1$ //$NON-NLS-2$
                                    ComponentUtilities.setNodeValue(node, "USE_LOG_FILE", "true");//$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }

                        }));

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 01, 20, 10, 0, 0);
        return gc.getTime();
    }
}
