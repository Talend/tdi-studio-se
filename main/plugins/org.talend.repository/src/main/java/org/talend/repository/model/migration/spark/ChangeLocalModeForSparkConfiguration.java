// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * TBD-2564: Change the SparkConf design to split local and cluster mode behavior to ease SparkJobServer integration
 */
public class ChangeLocalModeForSparkConfiguration extends
		AbstractJobMigrationTask {

	@Override
	public List<ERepositoryObjectType> getTypes() {
		List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
		ERepositoryObjectType type = ERepositoryObjectType
				.getType("PROCESS_SPARK"); //$NON-NLS-1$
		if (type != null) {
			toReturn.add(type);
		}
		type = ERepositoryObjectType
				.getType("PROCESS_SPARKSTREAMING"); //$NON-NLS-1$
		if (type != null) {
			toReturn.add(type);
		}
		return toReturn;
	}

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (processType != null) {
			try {
	            IComponentFilter filter = new NameComponentFilter("tSparkConfiguration");
	            ModifyComponentsAction.searchAndModify(item, processType, filter,
	                Arrays.<IComponentConversion> asList(new IComponentConversion() {
	                    @Override
	                    public void transform(NodeType node) {
	                    	ElementParameterType ept = ComponentUtilities.getNodeProperty(node, "SPARK_MODE");
                    		ComponentUtilities.addNodeProperty(node, "SPARK_LOCAL_MODE", "CHECK");
	                    	if("LOCAL".equalsIgnoreCase(ept.getValue())) {
	                            ComponentUtilities.getNodeProperty(node, "SPARK_LOCAL_MODE").setValue("true");	
	                            ComponentUtilities.getNodeProperty(node, "SPARK_MODE").setValue("CLUSTER"); // set back to default	                         
	                    	} else {
	                            ComponentUtilities.getNodeProperty(node, "SPARK_LOCAL_MODE").setValue("false");                        		
	                    	}             
	                    }
	                }));
	            return ExecutionResult.SUCCESS_NO_ALERT;
	        } catch (Exception e) {
	            ExceptionHandler.process(e);
	            return ExecutionResult.FAILURE;
	        }
		}
		return ExecutionResult.NOTHING_TO_DO;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
	 */
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2015, 9, 9, 12, 0, 0);
		return gc.getTime();
	}

}
