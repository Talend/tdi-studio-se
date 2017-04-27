package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
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
import org.talend.migration.IMigrationTask.ExecutionResult;

public class AddFailOnErrorOnTFileCopyTDI38441 extends AbstractJobMigrationTask {

	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2017, 4, 27, 14, 0, 0);
		return gc.getTime();
	}

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            String componentsName = new String ("tFileCopy"); //$NON-NLS-1$

            IComponentFilter filter = new NameComponentFilter(componentsName);
            IComponentConversion addOption = new AddFailOnErrorOptionConversion();
            ModifyComponentsAction
            	.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    
        
	}
    
	

	
	private class AddFailOnErrorOptionConversion implements IComponentConversion {

        private String field = "CHECK"; //$NON-NLS-1$

        private String name = "FAILON"; //$NON-NLS-1$

        private String isCopyDirectory = "ENABLE_COPY_DIRECTORY"; //$NON-NLS-1$

        public AddFailOnErrorOptionConversion() {
            super();
        }

        @Override
        public void transform(NodeType node) {
            if (ComponentUtilities.getNodeProperty(node, name) == null) {
                ComponentUtilities.addNodeProperty(node, name, field);
            }
            ElementParameterType copyingDirectory = ComponentUtilities.getNodeProperty(node, isCopyDirectory);
            if (copyingDirectory != null) {
            	if ("true".equals(copyingDirectory.getValue())) { //$NON-NLS-1$
            		ComponentUtilities.setNodeValue(node, name, "false"); //$NON-NLS-1$
            		return;
            	}
            	ComponentUtilities.setNodeValue(node, name, "true"); //$NON-NLS-1$
            }
        }
	}
	
}
