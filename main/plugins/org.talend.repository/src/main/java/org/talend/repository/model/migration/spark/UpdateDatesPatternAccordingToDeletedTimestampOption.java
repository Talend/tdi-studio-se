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

    private static final List<String> IMPACTED_COMPONENTS =
            Arrays.asList("tFileOutputParquet", "tHiveOutput", "tRedshiftOutput", "tSqlRow", "tMatchPairing",
                    "tMatchPredict", "tMatchModel", "tDataShuffling");
    
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 01, 19, 10, 0, 0);
        return gc.getTime();
    }
    
    @Override
    public List<ERepositoryObjectType> getTypes() {
        return Arrays.asList(ERepositoryObjectType.PROCESS_MR, ERepositoryObjectType.PROCESS_STORM);
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentConversion adaptSchemaForDateType = new AdaptSchemaForDateType();

        try {
            for (String componentName : IMPACTED_COMPONENTS) {
                ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentName),
                        java.util.Collections.singletonList(adaptSchemaForDateType));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class AdaptSchemaForDateType implements IComponentConversion {

        private String CHECK_BOX_NAME = "DATE_TO_TIMESTAMP_DF_TYPE_SUBSTITUTION"; //$NON-NLS-1$

        public void transform(NodeType node) {
            
            for(Object om : node.getMetadata()){
                MetadataType metadata = (MetadataType) om;
                for(Object oc : metadata.getColumn()){
                    ColumnType column = (ColumnType) oc;
                    if(column.getType().equals("id_Date") && isDateOrTimestamp(column)) {
                        if(isBasicDatePattern(column) && "true".equals(ComponentUtilities.getNodePropertyValue(node, CHECK_BOX_NAME))){
                            column.setPattern("\"yyyy-MM-dd HH:mm:ss\"");
                        } else if (!isBasicDatePattern(column) && "false".equals(ComponentUtilities.getNodePropertyValue(node, CHECK_BOX_NAME))) {
                            column.setPattern("\"yyyy-MM-dd\"");
                        }
                    }
                }
            }
        }
        
        private boolean isBasicDatePattern(ColumnType column) {
            List<String> basicPatterns = new ArrayList<String>(
                    java.util.Arrays.asList("dd-MM-yyyy", "MM-dd-yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy"));
            return basicPatterns.contains(column.getPattern().replaceAll("\"",""));
        }
        private boolean isDateOrTimestamp(ColumnType column) {
            List<String> basicPatterns = new ArrayList<String>(
                    java.util.Arrays.asList("dd-MM-yyyy", "MM-dd-yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy",
                            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'000Z'", 
                            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd HH:mm:ss zzz", "yyyy-MM-dd HH:mm:ss.SSSXXX"));
            return basicPatterns.contains(column.getPattern().replaceAll("\"",""));
        }
    }
}
