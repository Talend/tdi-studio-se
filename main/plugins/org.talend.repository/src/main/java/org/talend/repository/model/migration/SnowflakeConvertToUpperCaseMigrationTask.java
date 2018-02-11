package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class SnowflakeConvertToUpperCaseMigrationTask extends AbstractJobMigrationTask {

    private static final String OUTPUT_COMPONENT_NAME = "tSnowflakeOutput";
    private static final String CONVERT_COLUMNS_TO_UPPERCASE_PROPERTY_NAME = "convertColumnsToUppercase";
    private static final String CONVERT_COLUMNS_TO_UPPERCASE_PROPERTY_TYPE = "CHECK";

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2018, 02, 05, 18, 0, 0).getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        IComponentFilter outputComponentFilter = new NameComponentFilter(OUTPUT_COMPONENT_NAME);

        try {
            ModifyComponentsAction.searchAndModify(item, processType, outputComponentFilter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            ElementParameterType convertColumnsToUppercase = ComponentUtilities.getNodeProperty(node, CONVERT_COLUMNS_TO_UPPERCASE_PROPERTY_NAME);

                            if (convertColumnsToUppercase == null) {
                                ComponentUtilities.addNodeProperty(node, CONVERT_COLUMNS_TO_UPPERCASE_PROPERTY_NAME, CONVERT_COLUMNS_TO_UPPERCASE_PROPERTY_TYPE);
                                ComponentUtilities.getNodeProperty(node, CONVERT_COLUMNS_TO_UPPERCASE_PROPERTY_NAME).setValue("false");
                            }
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;

    }
}
