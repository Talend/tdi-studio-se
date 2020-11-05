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
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.NodeTypeImpl;

/**
 *
 * @author ametivier
 *
 * to keep retrocompatibility in dataset component that used timestamp by default (studio 7.0) or if 
 * they specified to use timestamp or date via an option (7.1 & 7.2), we need to add a global boolean value if all components
 * used timestamp, else we need to update the schema's pattern according to the value of the checkbox if it exists, 
 * or timestamp since it was the previous default value
 * 
 * note that in case the pattern is updated and the schema was repo, then the schema must be changed to built in when
 * opening the job to keep updated value (this case only happens if theres a conflict in Date / Timestamp in different nodes
 * of a job, which should be rare because it lead to a GUI error)
 *
 */
public class SetGlobalTimestampOptionForPreviousVersions extends AbstractJobMigrationTask {

	private static final List<String> IMPACTED_COMPONENTS =
            Arrays.asList("tFileOutputParquet", "tHiveOutput", "tRedshiftOutput", "tSqlRow", "tMatchPairing",
                    "tMatchPredict", "tMatchModel", "tDataShuffling", "tFileInputParquet", "tHiveInput",
                    "tDeltaLakeInput", "tDeltaLakeOutput");
    
    private String TIMESTAMP_OPTION = "USE_TIMESTAMP_FOR_DATASET"; //$NON-NLS-1$
    
    private String CHECKBOX_TIMESTAMP_NODE = "DATE_TO_TIMESTAMP_DF_TYPE_SUBSTITUTION"; //$NON-NLS-1$
    
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 01, 19, 10, 0, 0);
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
        IComponentConversion adaptSchemaForDateType = new AdaptSchemaForDateType();

        try {
        	if (isAComponentUsingDate(processType)) {
        		for (String componentName : IMPACTED_COMPONENTS) {
                    ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentName),
                            java.util.Collections.singletonList(adaptSchemaForDateType));
                }
        	} else {
        		setGlobalOption(processType, item);
        	}
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }
    
    private boolean isAComponentUsingDate(ProcessType processType) {
    	for (Object o : processType.getNode()) {
        	NodeTypeImpl node = (NodeTypeImpl) o;
        	for (Object p : node.getElementParameter()) {
        		ElementParameterTypeImpl elem = (ElementParameterTypeImpl) p;
        		if (CHECKBOX_TIMESTAMP_NODE.equals(elem.getName()) && "false".equals(elem.getValue())) {
        			return true;
        		}
        	}
        }
    	return false;
    }
    
    private void setGlobalOption(ProcessType processType, Item item) throws PersistenceException {
    	ElementParameterType property = TalendFileFactory.eINSTANCE.createElementParameterType();
        property.setName(TIMESTAMP_OPTION); //$NON-NLS-1$
        property.setField("CHECK"); //$NON-NLS-1$
        property.setValue("true"); //$NON-NLS-1$
        processType.getParameters().getElementParameter().add(property);
    	ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        factory.save(item, true);
    }

    private class AdaptSchemaForDateType implements IComponentConversion {

        public void transform(NodeType node) {
            for(Object om : node.getMetadata()){
                MetadataType metadata = (MetadataType) om;
                for(Object oc : metadata.getColumn()){
                    ColumnType column = (ColumnType) oc;
                    if(column.getType().equals("id_Date")) {
                    	if ("false".equals(ComponentUtilities.getNodePropertyValue(node, CHECKBOX_TIMESTAMP_NODE))) {
                            column.setPattern("\"dd-MM-yyyy\"");
                        } else {
                        	column.setPattern("\"yyyy-MM-dd HH:mm:ss\"");
                        }
                    }
                }
            }
        }
    }
}
