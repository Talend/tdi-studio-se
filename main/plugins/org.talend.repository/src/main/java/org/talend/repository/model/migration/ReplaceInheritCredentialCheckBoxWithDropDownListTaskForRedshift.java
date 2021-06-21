package org.talend.repository.model.migration;

import java.util.Collections;
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
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

/**
 * https://jira.talendforge.org/browse/TDI-46065
 */
public class ReplaceInheritCredentialCheckBoxWithDropDownListTaskForRedshift extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String[] componentsName = new String[] {"tRedshiftBulkExec", "tRedshiftOutputBulk", "tRedshiftOutputBulkExec"};

        try {
            for (int i = 0; i < componentsName.length; i++) {
                IComponentFilter filter = new NameComponentFilter(componentsName[i]);
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Collections.singletonList(new IComponentConversion() {

                            public void transform(NodeType node) {
                                if (ComponentUtilities.getNodeProperty(node, "CREDENTIAL_PROVIDER") == null) {
                                    ComponentUtilities.addNodeProperty(node, "CREDENTIAL_PROVIDER", "CLOSED_LIST");
                                    ElementParameterType prevCredProperty = ComponentUtilities.getNodeProperty(node, "INHERIT_CREDENTIALS");
                                    if (prevCredProperty != null && Boolean.TRUE.toString().equalsIgnoreCase(prevCredProperty.getValue())) {
                                        ComponentUtilities.getNodeProperty(node, "CREDENTIAL_PROVIDER").setValue("INHERIT_CREDENTIALS");
                                    } else {
                                        ComponentUtilities.getNodeProperty(node, "CREDENTIAL_PROVIDER").setValue("STATIC_CREDENTIALS");
                                    }
                                }
                            }
                        }));
                if(componentsName[i].equals("tRedshiftBulkExec")) {
                    ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Collections.singletonList(new IComponentConversion() {

                            public void transform(NodeType node) {
                                    ComponentUtilities.getNodeProperty(node, "CREDENTIAL_PROVIDER").setShow(false);
                            }
                    }));
                }
                ModifyComponentsAction.searchAndModify(item, processType, filter, 
                    Collections.singletonList(new RemovePropertyComponentConversion("INHERIT_CREDENTIALS")));
            }

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, 05, 14, 12, 0, 0);
        return gc.getTime();
    }
}
