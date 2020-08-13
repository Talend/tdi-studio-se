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

import java.util.ArrayList;
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
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;

/**
 *
 * @author ametivier
 *
 * There was two behaviour made to differentiate SQL input and dataset input, 
 * but that was a mistake and we can refacto both fields because they should have same behaviour
 * so its basically forcing fields from one to the other
 *
 */
public class RefactoDeltaMergeOption extends AbstractJobMigrationTask {

	private static final List<String> IMPACTED_COMPONENTS =
            Arrays.asList("tDeltaLakeOutput");
    
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 8, 12, 10, 0, 0);
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

        IComponentConversion mapDeltaValue = new MapDeltaValue();

        try {
            for (String componentName : IMPACTED_COMPONENTS) {
                ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentName),
                        java.util.Collections.singletonList(mapDeltaValue));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class MapDeltaValue implements IComponentConversion {

    	private String MERGE_SOURCE = "MERGE_SOURCE"; //$NON-NLS-1$
    	
    	private String CB_MATCH_1 = "WHEN_MATCHED_1_CB"; //$NON-NLS-1$
        private String COND_MATCH_1 = "WHEN_MATCHED_1_COND"; //$NON-NLS-1$
        private String ACTION_MATCH_1 = "WHEN_MATCHED_1_ACTION"; //$NON-NLS-1$
        private String MAPPING_MATCH_1 = "WHEN_MATCHED_1_COLS"; //$NON-NLS-1$
        
        private String CB_MATCH_2 = "WHEN_MATCHED_2_CB"; //$NON-NLS-1$
        private String COND_MATCH_2 = "WHEN_MATCHED_2_COND"; //$NON-NLS-1$
        private String ACTION_MATCH_2 = "WHEN_MATCHED_2_ACTION"; //$NON-NLS-1$
        private String MAPPING_MATCH_2 = "WHEN_MATCHED_2_COLS"; //$NON-NLS-1$
        
        private String CB_NOT_MATCH = "WHEN_NOT_MATCHED_CB"; //$NON-NLS-1$
        private String COND_NOT_MATCH = "WHEN_NOT_MATCHED_COND"; //$NON-NLS-1$
        private String ACTION_NOT_MATCH = "WHEN_NOT_MATCHED_ACTION"; //$NON-NLS-1$
        private String MAPPING_NOT_MATCH = "WHEN_NOT_MATCHED_COLS"; //$NON-NLS-1$
        
        private String CB_MATCH_1_SQL = "WHEN_MATCHED_1_CB_SQL"; //$NON-NLS-1$
        private String COND_MATCH_1_SQL = "WHEN_MATCHED_1_COND_SQL"; //$NON-NLS-1$
        private String ACTION_MATCH_1_SQL = "WHEN_MATCHED_1_ACTION_SQL"; //$NON-NLS-1$
        private String MAPPING_MATCH_1_SQL = "WHEN_MATCHED_1_COLS_SQL"; //$NON-NLS-1$
        
        private String CB_MATCH_2_SQL = "WHEN_MATCHED_2_CB_SQL"; //$NON-NLS-1$
        private String COND_MATCH_2_SQL = "WHEN_MATCHED_2_COND_SQL"; //$NON-NLS-1$
        private String ACTION_MATCH_2_SQL = "WHEN_MATCHED_2_ACTION_SQL"; //$NON-NLS-1$
        private String MAPPING_MATCH_2_SQL = "WHEN_MATCHED_2_COLS_SQL"; //$NON-NLS-1$
        
        private String CB_NOT_MATCH_SQL = "WHEN_NOT_MATCHED_CB_SQL"; //$NON-NLS-1$
        private String COND_NOT_MATCH_SQL = "WHEN_NOT_MATCHED_COND_SQL"; //$NON-NLS-1$
        private String ACTION_NOT_MATCH_SQL = "WHEN_NOT_MATCHED_ACTION_SQL"; //$NON-NLS-1$
        private String MAPPING_NOT_MATCH_SQL = "WHEN_NOT_MATCHED_COLS_SQL"; //$NON-NLS-1$
        
        public void transform(NodeType node) {
        	if ("SQL".equals(ComponentUtilities.getNodePropertyValue(node, MERGE_SOURCE))) {
        		ComponentUtilities.setNodeValue(node, CB_MATCH_1, ComponentUtilities.getNodePropertyValue(node, CB_MATCH_1_SQL));
        		ComponentUtilities.setNodeValue(node, COND_MATCH_1, ComponentUtilities.getNodePropertyValue(node, COND_MATCH_1_SQL));
        		ComponentUtilities.setNodeValue(node, ACTION_MATCH_1, ComponentUtilities.getNodePropertyValue(node, ACTION_MATCH_1_SQL));
        		overrideTable(node, MAPPING_MATCH_1, MAPPING_MATCH_1_SQL);
        		
        		ComponentUtilities.setNodeValue(node, CB_MATCH_2, ComponentUtilities.getNodePropertyValue(node, CB_MATCH_2_SQL));
        		ComponentUtilities.setNodeValue(node, COND_MATCH_2, ComponentUtilities.getNodePropertyValue(node, COND_MATCH_2_SQL));
        		ComponentUtilities.setNodeValue(node, ACTION_MATCH_2, ComponentUtilities.getNodePropertyValue(node, ACTION_MATCH_2_SQL));
        		overrideTable(node, MAPPING_MATCH_2, MAPPING_MATCH_2_SQL);
        		
        		ComponentUtilities.setNodeValue(node, CB_NOT_MATCH, ComponentUtilities.getNodePropertyValue(node, CB_NOT_MATCH_SQL));
        		ComponentUtilities.setNodeValue(node, COND_NOT_MATCH, ComponentUtilities.getNodePropertyValue(node, COND_NOT_MATCH_SQL));
        		ComponentUtilities.setNodeValue(node, ACTION_NOT_MATCH, ComponentUtilities.getNodePropertyValue(node, ACTION_NOT_MATCH_SQL));
        		overrideTable(node, MAPPING_NOT_MATCH, MAPPING_NOT_MATCH_SQL);
        	}
        	
        }
        
        private void overrideTable(NodeType node, String table, String originalTable) {
        	java.util.List<ElementValueType> values = new ArrayList<ElementValueType>();
        	TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
        	for (Object o : ComponentUtilities.getNodeProperty(node, originalTable).getElementValue()) {
        		ElementValueType e = (ElementValueType) o;
        		ElementValueType elementValue = fileFact.createElementValueType();
                elementValue.setElementRef(e.getElementRef());
                elementValue.setValue(e.getValue());
                values.add(elementValue);
        	}
        	ElementParameterType property = ComponentUtilities.getNodeProperty(node, table);
        	property.getElementValue().clear();
        	for (ElementValueType value : values) {
                property.getElementValue().add(value);
            }
        }
    }
}
