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

import org.talend.commons.exception.PersistenceException;
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
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangetMongoDBInputSortTextToTable extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        IComponentFilter filter = new NameComponentFilter("tMongoDBInput");
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                    .<IComponentConversion> asList(new IComponentConversion() {

						public void transform(NodeType node) {
							ElementParameterType sort = ComponentUtilities.getNodeProperty(node, "SORT");
							if (sort == null) {
								return;
							}
                            String fieldType = sort.getField();
                            if ("TEXT".equalsIgnoreCase(fieldType)) {
                            	String sortValue = sort.getValue();
                            	ComponentUtilities.removeNodeProperty(node, "SORT");
                            	ComponentUtilities.addNodeProperty(node,  "SORT", "TABLE");
                            	if(sortValue!=null && !"".equals(sortValue)){
	                            	sort = ComponentUtilities.getNodeProperty(node , "SORT");
	                            	sortValue = sortValue.substring(sortValue.indexOf("{") + 1, sortValue.lastIndexOf("}"));
	                            	String [] sortTable = sortValue.split(",");
	                            	ElementValueType columnNameElement = null;
	                            	ElementValueType sortType = null;
	                            	for (String sortStr : sortTable) {
	                            		columnNameElement = TalendFileFactory.eINSTANCE.createElementValueType();
	                            		columnNameElement.setElementRef("COLNAME");
	                            		String columnValue = sortStr.split(":")[0].trim();
	                            		if (columnValue.startsWith("\\\"") && columnValue.endsWith("\\\"")) {
	                            			columnValue = "\"" + columnValue.substring(2, columnValue.length() - 2) + "\"";
	                            		}
	                            		columnNameElement.setValue(columnValue);

		                            	sort.getElementValue().add(columnNameElement);
		                            	sortType = TalendFileFactory.eINSTANCE.createElementValueType();
		                            	sortType.setElementRef("ORDER");
		                            	sortType.setValue(("1".equals(sortStr.split(":")[1].trim())) ? "asc" : "desc");
		                            	sort.getElementValue().add(sortType);
	                            	}
                            	}

                            }
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 7, 24, 12, 0, 0);
        return gc.getTime();
    }
}
