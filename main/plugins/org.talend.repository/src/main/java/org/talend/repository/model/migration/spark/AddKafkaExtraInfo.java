package org.talend.repository.model.migration.spark;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.exception.ExceptionHandler;
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
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class AddKafkaExtraInfo extends AbstractJobMigrationTask {

    private static final List<String> IMPACTED_COMPONENTS = Arrays.asList("tKafkaInput");

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 12, 23, 13, 0, 0);
        return gc.getTime();
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        // Spark batch
        List<ERepositoryObjectType> types = Arrays
                .asList(ERepositoryObjectType.TEST_CONTAINER, ERepositoryObjectType.PROCESS_STORM,
                        ERepositoryObjectType.SPARK_STREAMING_JOBLET);

        return types;
    }

    @Override
    public ExecutionResult execute(Item item) {

        boolean fullMigrationSucceded = true;
        ProcessType processType = getProcessType(item);

        if (processType == null) {

            return ExecutionResult.NOTHING_TO_DO;
        }

        for (String componentName : IMPACTED_COMPONENTS) {

            IComponentConversion addKafkaInfo = new AddKafkaInfo();

            try {
                ModifyComponentsAction
                        .searchAndModify(item, processType, new NameComponentFilter(componentName),
                                java.util.Collections.singletonList(addKafkaInfo));
            } catch (Exception e) {
                ExceptionHandler.process(e);
                fullMigrationSucceded = false;
            }

        }

        return fullMigrationSucceded ? ExecutionResult.SUCCESS_NO_ALERT : ExecutionResult.FAILURE;
    }

    private class AddKafkaInfo implements IComponentConversion {

        @Override
        public void transform(NodeType node) {

            TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
            MetadataType newMetadata = fileFact.createMetadataType();

            for (Object metadataObject : node.getMetadata()) {
                MetadataType metadata = (MetadataType) metadataObject;

                Map<String, String> newColumns = new HashMap<String, String>();

                newColumns.put("topic", "id_String");
                newColumns.put("partition", "id_Integer");
                newColumns.put("key", "id_byte[]");

                List<ColumnType> columnList = metadata.getColumn();

                for (Map.Entry<String, String> entry : newColumns.entrySet()) {
                    ColumnType metadataColumn = fileFact.createColumnType();
                    metadataColumn.setName(entry.getKey());
                    metadataColumn.setType(entry.getValue());
                    columnList.add(metadataColumn);
                }
            }
        }
    }

}
