package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.conversions.RemovePropertyComponentConversion;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * https://jira.talendforge.org/browse/COMP-310
 * https://jira.talendforge.org/browse/TDI-45323
 */
public class ReplaceInheritCredentialCheckBoxWithDropDownListTask extends AbstractJobMigrationTask {

    private static final String CREDENTIAL_PROVIDER_PROPERTY_NAME = "CREDENTIAL_PROVIDER";
    private static final String INHERIT_CREDENTIALS_PROPERTY_NAME = "INHERIT_CREDENTIALS";
    private static final String INHERIT_CREDENTIALS_LIST_VALUE = "INHERIT_CREDENTIALS";
    private static final String STATIC_CREDENTIALS_LIST_VALUE = "STATIC_CREDENTIALS";

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        // the list with components that should be migrated
        // TODO: What about spark tS3Configuration?
        final List<NameComponentFilter> componentNames = Stream.of(
                "tS3BucketCreate",
                "tS3BucketDelete",
                "tS3BucketExist",
                "tS3BucketList",
                "tS3Connection",
                "tS3Copy",
                "tS3Delete",
                "tS3Get",
                "tS3List",
                "tS3Put"
        ).map(NameComponentFilter::new)
                .collect(Collectors.toList());

        // add property that presents drop down list
        IComponentConversion addNewProperty = node -> {
            ComponentUtilities.addNodeProperty(node, CREDENTIAL_PROVIDER_PROPERTY_NAME, "CLOSED_LIST");

            ElementParameterType prevCredProperty = ComponentUtilities.getNodeProperty(node, INHERIT_CREDENTIALS_PROPERTY_NAME);
            if (prevCredProperty != null && Boolean.TRUE.toString().equalsIgnoreCase(prevCredProperty.getValue())) {
                ComponentUtilities.getNodeProperty(node, CREDENTIAL_PROVIDER_PROPERTY_NAME)
                        .setValue(INHERIT_CREDENTIALS_LIST_VALUE);
            } else {
                ComponentUtilities.getNodeProperty(node, CREDENTIAL_PROVIDER_PROPERTY_NAME)
                        .setValue(STATIC_CREDENTIALS_LIST_VALUE);
            }
        };

        // conversions
        List<IComponentConversion> conversions = Arrays.asList(
                addNewProperty,
                new RemovePropertyComponentConversion(INHERIT_CREDENTIALS_PROPERTY_NAME)
        );

        try {
            for (NameComponentFilter filter : componentNames) {
                ModifyComponentsAction.searchAndModify(item, processType, filter, conversions);
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, Calendar.MARCH, 29, 14, 34, 0);
        return gc.getTime();
    }
}
