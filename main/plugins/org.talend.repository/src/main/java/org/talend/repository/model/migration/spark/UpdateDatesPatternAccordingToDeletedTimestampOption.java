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

import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 *
 * @author ametivier
 *
 * Since we removed the timestamp option from certain components, we need to update the pattern in the schema
 * according to the value of the checkbox
 *
 */
public class UpdateDatesPatternAccordingToDeletedTimestampOption extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 01, 19, 10, 0, 0);
        return gc.getTime();
    }
    
    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS_SPARK);
        toReturn.add(ERepositoryObjectType.PROCESS_SPARKSTREAMING);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        List<String> impactedComponents =
                Arrays.asList("tFileOutputParquet", "tHiveOutput", "tRedshiftOutput", "tSqlRow", "tMatchPairing",
                        "tMatchPredict", "tMatchModel", "tDataShuffling");

        IComponentConversion AdaptSchemaForDateType = new AdaptSchemaForDateType();

        try {
            for (String componentName : impactedComponents) {
                ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentName),
                        Arrays.<IComponentConversion> asList(AdaptSchemaForDateType));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            return ExecutionResult.FAILURE;
        }
    }

    private class AdaptSchemaForDateType implements IComponentConversion {

        private String name = "DATE_TO_TIMESTAMP_DF_TYPE_SUBSTITUTION"; //$NON-NLS-1$

        public void transform(NodeType node) {
            
            for(Object om : node.getMetadata()){
                MetadataType metadata = (MetadataType) om;
                for(Object oc : metadata.getColumn()){
                    ColumnType column = (ColumnType) oc;
                    if(column.getType().equals("id_Date")) {
                        if(ComponentUtilities.getNodePropertyValue(node, name) == "true"){
                            column.setPattern("\"yyyy-MM-dd\"");
                        } else {
                                
                        }
                    }
                }
            }
        }
    }
}
