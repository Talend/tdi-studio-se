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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeBagName4tPigCode extends
		AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = {"tPigCode"}; //$NON-NLS-1$

    	IComponentConversion changeBagName4tPigCode = new IComponentConversion() {

	        public void transform(NodeType node) {
	            if(node == null) {
	                return;
	            }

	        	ElementParameterType pigScript = ComponentUtilities.getNodeProperty(node, "SCRIPT_CODE"); //$NON-NLS-1$

	        	if (pigScript != null) {
	        		String content = pigScript.getValue();
	        		if(content == null) {
	        		    return;
	        		}

	        		Pattern pattern = Pattern.compile("tPig[a-zA-Z]+_\\d+_RESULT");//$NON-NLS-1$
	        		String end = "_RESULT";//$NON-NLS-1$
	        		Matcher matcher = pattern.matcher(content);
	        		boolean matches = matcher.find();

	        		if (matches) {
	                    //replace the pigscript
	        		    StringBuffer sb = new StringBuffer();
	                    do {
	                        String group = matcher.group();

	                        String uniqueNameOfComponent = group.substring(0,group.lastIndexOf(end));

	                        String outputConnectionName = null;

	                        if(processType!=null) {

	                            List connections = processType.getConnection();
	                            if(connections!=null) {
	                                for(Object connection : connections) {
	                                    ConnectionType currentConnection = (ConnectionType) connection;
	                                    String source = currentConnection.getSource();

	                                    if(uniqueNameOfComponent!=null && uniqueNameOfComponent.equals(source)) {//find the unique output connection
	                                        for (Object paramObject : currentConnection.getElementParameter()) {
	                                            ElementParameterType paramType = (ElementParameterType) paramObject;
	                                            if ("UNIQUE_NAME".equals(paramType.getName())) {//$NON-NLS-1$
	                                                outputConnectionName = paramType.getValue();
	                                            }
	                                        }
	                                        break;
	                                    }
	                                }
	                            }
	                        }

	                        if(outputConnectionName == null) {//not find
	                            matcher.appendReplacement(sb,group);
	                        } else {
	                            matcher.appendReplacement(sb,uniqueNameOfComponent+"_"+outputConnectionName+end);//$NON-NLS-1$
	                        }

	                        matches = matcher.find();
	                    } while (matches);
	                    matcher.appendTail(sb);

	        		    pigScript.setValue(sb.toString());
	        		}
	        	}
	        }

    	};

    	for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(changeBagName4tPigCode));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 2, 6, 10, 0, 0);
        return gc.getTime();
    }
}
