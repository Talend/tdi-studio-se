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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Collections;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
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

public class PostgresqlDefaultDBVersion extends AbstractJobMigrationTask{

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 9, 24, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String [] componentsNameToAffect = new String [] {
                "tCreateTable",
                "tPostgresqlCDC",
                "tPostgresPlusBulkExec",
                "tPostgresPlusConnection",
                "tPostgresPlusInput",
                "tPostgresPlusOutput",
                "tPostgresPlusOutputBulkExec",
                "tPostgresPlusRow",
                "tPostgresPlusSCD",
                "tPostgresPlusSCDELT",
                "tPostgresqlBulkExec",
                "tPostgresqlConnection",
                "tPostgresqlInput",
                "tPostgresqlOutput",
                "tPostgresqlOutputBulkExec",
                "tPostgresqlRow",
                "tPostgresqlSCD",
                "tPostgresqlSCDELT"
        };

        IComponentConversion defaultDBVersion = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                boolean useExistConnection = "true".equals(ComponentUtilities.getNodePropertyValue(node, "USE_EXISTING_CONNECTION"));
                if (useExistConnection) return;

                String componentName = node.getComponentName();
                String dbVersion = "";

                if ("tCreateTable".equals(componentName)) {
                    String dbType = ComponentUtilities.getNodePropertyValue(node, "DBTYPE");
                    if (!"POSTGRE".equals(dbType) || !"POSTGREPLUS".equals(dbType)) return;
                    dbVersion = ComponentUtilities.getNodePropertyValue(node, "DB_POSTGRE_VERSION");
                } else {
                    dbVersion = ComponentUtilities.getNodePropertyValue(node, "DB_VERSION");
                }

                if (dbVersion==null) {
                    ComponentUtilities.addNodeProperty(node, "DB_VERSION", "CLOSED_LIST");
                    ComponentUtilities.setNodeValue(node, "DB_VERSION", "PRIOR_TO_V9");
                }
            }
        };

        for (String componentName : componentsNameToAffect) {
            IComponentFilter componentFilter = new NameComponentFilter(componentName);
            try {
                ModifyComponentsAction.searchAndModify(item, processType, componentFilter, Collections.singletonList(defaultDBVersion));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }
}
