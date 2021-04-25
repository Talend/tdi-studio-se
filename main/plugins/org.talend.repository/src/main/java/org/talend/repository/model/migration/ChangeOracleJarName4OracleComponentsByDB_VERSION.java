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
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeOracleJarName4OracleComponentsByDB_VERSION extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        String[] oracleCompNames = { "tOracleBulkExec", "tOracleClose", "tOracleCommit", "tOracleConnection", "tOracleInput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "tOracleOutput", "tOracleOutputBulk", "tOracleOutputBulkExec", "tOracleRollback", "tOracleRow", "tOracleSCD", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                "tOracleSCDELT", "tOracleSP", "tOracleTableList", "tAmazonOracleClose", "tAmazonOracleCommit", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "tAmazonOracleConnection", "tAmazonOracleInput", "tAmazonOracleOutput", "tAmazonOracleRollback", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                "tAmazonOracleRow", "tMondrianInput", "tCreateTable", "tOracleInvalidRows", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                "tOracleValidRows", "tELTOracleMap", "tOracleCDC", "tOracleSCDELT" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        IComponentConversion changeOracleDriverJarType = new IComponentConversion() {

            public void transform(NodeType node) {
                ElementParameterType dbVersion = ComponentUtilities.getNodeProperty(node, "DB_VERSION"); //$NON-NLS-1$
                if (dbVersion != null) {
                    String jarValue = dbVersion.getValue();
                    if ("ojdbc6.jar".equalsIgnoreCase(jarValue)) { //$NON-NLS-1$
                        dbVersion.setValue("ORACLE_11-6"); //$NON-NLS-1$
                    } else if ("ojdbc5.jar".equalsIgnoreCase(jarValue)) { //$NON-NLS-1$
                        dbVersion.setValue("ORACLE_11"); //$NON-NLS-1$
                    } else if ("ojdbc14.jar".equalsIgnoreCase(jarValue)) { //$NON-NLS-1$
                        dbVersion.setValue("ORACLE_10"); //$NON-NLS-1$
                    } else if ("ojdbc14-9i.jar".equalsIgnoreCase(jarValue)) { //$NON-NLS-1$
                        dbVersion.setValue("ORACLE_9"); //$NON-NLS-1$
                    } else if ("ojdbc12.jar".equalsIgnoreCase(jarValue)) { //$NON-NLS-1$
                        dbVersion.setValue("ORACLE_8"); //$NON-NLS-1$
                    }
                }
            }

        };

        for (String name : oracleCompNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(changeOracleDriverJarType));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 10, 19, 10, 0, 0);
        return gc.getTime();
    }
}
