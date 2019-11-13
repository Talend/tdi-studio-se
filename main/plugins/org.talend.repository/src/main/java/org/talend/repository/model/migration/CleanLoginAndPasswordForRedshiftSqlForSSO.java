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
import org.talend.utils.security.StudioEncryption;

public class CleanLoginAndPasswordForRedshiftSqlForSSO extends AbstractJobMigrationTask {
	
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 10, 7, 10, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String[] componentsNameToAffect = new String[]{
                "tRedshiftBulkExec",
                "tRedshiftConnection",
                "tRedshiftInput",
                "tRedshiftOutput",
                "tRedshiftOutputBulkExec",
                "tRedshiftRow",
                "tRedshiftUnload"
        };

        IComponentConversion defaultDBVersion = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                boolean useSSO = "SSO".equals(ComponentUtilities.getNodePropertyValue(node, "JDBC_URL"));
                                
                if (useSSO) {
                	ComponentUtilities.setNodeValue(node, "USER", "\"\"");             	
                	ComponentUtilities.setNodeValue(node, "PASS", StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM).encrypt("\"\""));
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
