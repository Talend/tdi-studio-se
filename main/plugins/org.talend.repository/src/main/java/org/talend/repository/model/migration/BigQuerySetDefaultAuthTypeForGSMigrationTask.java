//============================================================================
//
//Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
//This source code is available under agreement available at
//%InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//You should have received a copy of the agreement
//along with this program; if not, write to Talend SA
//9 rue Pages 92150 Suresnes, France
//
//============================================================================
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

public class BigQuerySetDefaultAuthTypeForGSMigrationTask extends AbstractJobMigrationTask{

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 6, 13, 10, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String [] componentsNameToAffect = new String [] {
                "tBigQueryOutput",
                "tBigQueryBulkExec"
        };

        IComponentConversion defaultAuthType = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                String authType = ComponentUtilities.getNodePropertyValue(node, "AUTH_TYPE");
                if (authType == null) {
                    ComponentUtilities.addNodeProperty(node, "AUTH_TYPE", "CLOSED_LIST");
                    ComponentUtilities.setNodeValue(node, "AUTH_TYPE", "USER_ACCOUNT_HMAC");
                }

            }
        };

        for (String componentName : componentsNameToAffect) {
            IComponentFilter componentFilter = new NameComponentFilter(componentName);
            try {
                ModifyComponentsAction.searchAndModify(item, processType, componentFilter, Collections.singletonList(defaultAuthType));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }
}
