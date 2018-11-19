package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.metadata.types.TypesManager;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class AddDbMappingTTeradataFastLoadMigrationTask extends AbstractJobMigrationTask {

	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2018, 6, 26, 0, 0, 0);
		return gc.getTime();
	}

	@Override
	public ExecutionResult execute(Item item) {
		final String mappingType = "teradata_id";
		ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {

        	IComponentFilter filter = new NameComponentFilter("tTeradataFastLoad");
        	ModifyComponentsAction.searchAndModify(item, processType, filter,
        			Arrays.<IComponentConversion> asList(new IComponentConversion() {

        				@Override
        				public void transform(NodeType node) {
        					for (Object metadataObject : node.getMetadata()) {
        						MetadataType metadataType = (MetadataType) metadataObject;
        						for (Object columnObject : metadataType.getColumn()) {
        							ColumnType columnType = (ColumnType) columnObject;
        							if (columnType.getSourceType() == null || "".equals(columnType.getSourceType())) {
        								String type = null;
        								//Previously all date fields were sent as DATE to db. 
        								//We need to leave this as it was, not to break previous tasks.
        								if(columnType.getType().equals("id_Date")) {
        									type = "DATE";
        								} else {
        									type = TypesManager.getDBTypeFromTalendType(mappingType, columnType.getType());
        								}
        								columnType.setSourceType(type);
        							}
        						}
        					}
        				}
        			}));

        	return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
	}

}
