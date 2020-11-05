// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;

/**
 *
 * @author ametivier
 *
 * Since we removed the timestamp option from certain components, we need to update the pattern in the schema
 * according to the value of the checkbox
 *
 */
public class UpdateSeparatorAndEscapeForDatasetAPI extends AbstractJobMigrationTask {

	private static final List<String> IMPACTED_COMPONENTS =
            Arrays.asList("tFileInputDelimited", "tExtractDelimitedFields", "tFileOutputDelimited");
    
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 8, 1, 10, 0, 0);
        return gc.getTime();
    }
    
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
        boolean isSparkLocal = false;
        String sparkLocalVersion = "";
        String sparkVersion = "";
        for (Object o:processType.getParameters().getElementParameter()) {
        	ElementParameterTypeImpl p = (ElementParameterTypeImpl) o;
        	if ("SPARK_LOCAL_MODE".equals(p.getName())) {
        		isSparkLocal = "true".equals(p.getValue());
        	} else if ("SPARK_LOCAL_VERSION".equals(p.getName())) {
        		sparkLocalVersion = p.getValue();
        	} else if ("SUPPORTED_SPARK_VERSION".equals(p.getName())) {
        		sparkVersion = p.getValue();
        	}
        }
        if ((isSparkLocal && sparkLocalVersion.compareTo("SPARK_2_0_0") < 0) || (!isSparkLocal && sparkVersion.compareTo("SPARK_2_0_0") < 0)) {
        	//we only update if we use dataset (meaning spark 2.0+)
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentConversion adaptSeparatorAndEscape = new AdaptSeparatorAndEscape();

        try {
            for (String componentName : IMPACTED_COMPONENTS) {
                ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentName),
                        java.util.Collections.singletonList(adaptSeparatorAndEscape));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class AdaptSeparatorAndEscape implements IComponentConversion {

    	private String ISCSV = "CSV_OPTION"; //$NON-NLS-1$
        private String ESCAPE = "ESCAPE_CHAR"; //$NON-NLS-1$
        private String TEXTENCLOSURE = "TEXT_ENCLOSURE"; //$NON-NLS-1$
        
        public void transform(NodeType node) {
        	boolean isCSV = "true".equals(ComponentUtilities.getNodePropertyValue(node, ISCSV));
        	String escape = ComponentUtilities.getNodePropertyValue(node, ESCAPE);
        	String separator = ComponentUtilities.getNodePropertyValue(node, TEXTENCLOSURE);
        	if (isCSV) {
        		if ("\"\"\"".equals(escape)) {
        			ComponentUtilities.setNodeValue(node, ESCAPE, "\"\\\"\"");//"\"\\\"\"" is printed "\"" in UI
        		}
        		if ("\"\"\"".equals(separator)) {
        			ComponentUtilities.setNodeValue(node, TEXTENCLOSURE, "\"\\\"\"");
        		}
        	}
        }
    }
}
