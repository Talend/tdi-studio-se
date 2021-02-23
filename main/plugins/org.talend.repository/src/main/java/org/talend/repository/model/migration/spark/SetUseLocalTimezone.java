package org.talend.repository.model.migration.spark;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.impl.AdditionalInfoMapImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
*
* @author hliashchuk
*
* This migration task is created to keep the same behavior for jobs created in Studio older than 7.3.1
* Since 7.3.1 default timezone is set to UTC and in order not to affect jobs created with 7.3.1+, "Use local timezone" checkbox is set to false 
* and migration task is applied for older jobs to set "Use local timezone" to true
*
*/
public class SetUseLocalTimezone extends AbstractJobMigrationTask {

	private String CHECKBOX_USE_LOCAL_TIMEZONE = "USE_LOCAL_TIMEZONE";
			
	@Override
    public List<ERepositoryObjectType> getTypes() {
        return Arrays.asList(ERepositoryObjectType.PROCESS_MR);
    }

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        List<AdditionalInfoMapImpl> properties = item.getProperty().getAdditionalProperties();
        String talendVersionUsedToCreateJob = null;
        for (AdditionalInfoMapImpl property : properties) {
        	if ("created_product_version".equals(property.getKey().toString())) {
        		talendVersionUsedToCreateJob = property.getValue().toString();
        	}
        } 
        if (talendVersionUsedToCreateJob.compareTo("7.3.1") < 0){
        	ElementParameterType property = TalendFileFactory.eINSTANCE.createElementParameterType();
        	property.setName(CHECKBOX_USE_LOCAL_TIMEZONE);
            property.setField("CHECK");
            property.setValue("true");
        	try {
            	processType.getParameters().getElementParameter().add(property);
            	ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                factory.save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        } else {
        	return ExecutionResult.NOTHING_TO_DO;
        }
        
	}

	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2021, 02, 01, 10, 0, 0);
        return gc.getTime();
	}

}
