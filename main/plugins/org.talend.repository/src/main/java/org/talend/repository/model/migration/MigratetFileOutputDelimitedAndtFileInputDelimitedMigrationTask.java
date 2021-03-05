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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * for bug TDI-13497
 *
 */
public class MigratetFileOutputDelimitedAndtFileInputDelimitedMigrationTask extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
    	if(processType == null){
    		return ExecutionResult.NOTHING_TO_DO;
    	}

    	String[] componentsNames = new String[] { "tFileInputDelimited" , "tFileOutputDelimited" };

    	try {
        	for(String componentsName : componentsNames) {
        		ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentsName), Arrays
                        .<IComponentConversion> asList(new IComponentConversion() {

							public void transform(NodeType node) {
								ElementParameterType par = ComponentUtilities.getNodeProperty(node, "CSV_OPTION");
								if(par!=null && "true".equals(par.getValue())) {
									ElementParameterType para = ComponentUtilities.getNodeProperty(node, "ROWSEPARATOR");
									if(para!=null) {
										String rowSeparator = para.getValue();
										ElementParameterType parameter = ComponentUtilities.getNodeProperty(node, "CSVROWSEPARATOR");
										if(parameter == null) {
											ComponentUtilities.addNodeProperty(node, "CSVROWSEPARATOR", "OPENED_LIST");
											ComponentUtilities.setNodeValue(node, "CSVROWSEPARATOR", rowSeparator);
										}
									}
								}
							}

                        }));
        	}
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 9, 1, 12, 0, 0);
        return gc.getTime();
    }
}
