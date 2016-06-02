package org.talend.repository.model.migration;

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
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class SetDefaultValueForNewerEmailFirstOfTDI35976 extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
       return new GregorianCalendar(2016, 4, 12, 16, 0, 0).getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        
        List<String> filterList = Arrays.asList(
        		"tPOP"
        		);
        
        IComponentConversion correctBatchModeForDBComponents = new IComponentConversion() {
			public void transform(NodeType node) {
				ElementParameterType isNewEmailFirst = ComponentUtilities.getNodeProperty(node, "NEWER_EMAILS");
				if(isNewEmailFirst == null){
					ComponentUtilities.addNodeProperty(node, "NEWER_EMAILS", "CHECK");
	                ComponentUtilities.getNodeProperty(node, "NEWER_EMAILS").setValue("false");
				}
			}
		};
		
		for(String componentName: filterList){
			IComponentFilter filter = new NameComponentFilter(componentName);
			try {
				ModifyComponentsAction
						.searchAndModify(
								item,
								processType,
								filter,
								Arrays.<IComponentConversion> asList(correctBatchModeForDBComponents));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
				return ExecutionResult.FAILURE;
			}
		}
		
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

}
