package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import org.talend.migration.IMigrationTask.ExecutionResult;


public class PostgresqlDefaultDBVersion extends AbstractJobMigrationTask{
	
	private static final String DB_VERSION_PROPERTY = "DB_VERSION";
	private static final String DB_VERSION_VALUE = "PRIOR_TO_V9";

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
        List<String> componentsNameToAffect = new ArrayList<>();
        componentsNameToAffect.add("tCreateTable");
        componentsNameToAffect.add("tPostgresqlCDC");
        
        componentsNameToAffect.add("tPostgresPlusBulkExec");
        componentsNameToAffect.add("tPostgresPlusConnection");
        componentsNameToAffect.add("tPostgresPlusInput");
        componentsNameToAffect.add("tPostgresPlusOutput");
        componentsNameToAffect.add("tPostgresPlusOutputBulkExec");
        componentsNameToAffect.add("tPostgresPlusRow");
        componentsNameToAffect.add("tPostgresPlusSCD");
        componentsNameToAffect.add("tPostgresPlusSCDELT");
        
        componentsNameToAffect.add("tPostgresqlBulkExec");
        componentsNameToAffect.add("tPostgresqlConnection");
        componentsNameToAffect.add("tPostgresqlInput");
        componentsNameToAffect.add("tPostgresqlOutput");
        componentsNameToAffect.add("tPostgresqlOutputBulkExec");
        componentsNameToAffect.add("tPostgresqlRow");
        componentsNameToAffect.add("tPostgresqlSCD");
        componentsNameToAffect.add("tPostgresqlSCDELT");


        IComponentConversion setDefaultDBVersion = new IComponentConversion() {

			@Override
			public void transform(NodeType node) {
				if ("tCreateTable".equals(node.getComponentName()) && !isCreateTableMatchMigrationTask(node)) {
					return;
				}
				String dbVersion = ComponentUtilities.getNodePropertyValue(node, DB_VERSION_PROPERTY);
				if (dbVersion == null || dbVersion.isEmpty()) {
					ComponentUtilities.setNodeValue(node, DB_VERSION_PROPERTY, DB_VERSION_VALUE);
				}
			}
			
			private boolean isCreateTableMatchMigrationTask(NodeType node) {
				String dbType = ComponentUtilities.getNodePropertyValue(node, "DBTYPE");
				boolean useExistingConnection = "true".equals(ComponentUtilities.getNodePropertyValue(node, "USE_EXISTING_CONNECTION"));
				return "POSTGRE".equals(dbType) && !useExistingConnection;
			}
        };
        
        for (String componentName : componentsNameToAffect) {
            IComponentFilter componentFilter = new NameComponentFilter(componentName);
            try {
            	ModifyComponentsAction.searchAndModify(item, processType, componentFilter,
                    Arrays.<IComponentConversion> asList(setDefaultDBVersion));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
		return ExecutionResult.SUCCESS_NO_ALERT;
	}
}
