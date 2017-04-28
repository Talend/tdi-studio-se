package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

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
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class AddFailOnErrorOnTFileCopyTDI38441 extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 4, 27, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            String componentsName = new String("tFileCopy"); //$NON-NLS-1$

            IComponentFilter filter = new NameComponentFilter(componentsName);
            IComponentConversion addOption = new IComponentConversion() {

                @Override
                public void transform(NodeType node) {

                    String propertyType = "CHECK"; //$NON-NLS-1$
                    String propertyName = "FAILON"; //$NON-NLS-1$
                    String isCopyDirectoryPropertyName = "ENABLE_COPY_DIRECTORY"; //$NON-NLS-1$

                    if (ComponentUtilities.getNodeProperty(node, propertyName) == null) {
                        ComponentUtilities.addNodeProperty(node, propertyName, propertyType);
                    }
                    ElementParameterType copyingDirectory = ComponentUtilities.getNodeProperty(node, isCopyDirectoryPropertyName);
                    if (copyingDirectory != null) {
                        if ("true".equals(copyingDirectory.getValue())) { //$NON-NLS-1$
                            ComponentUtilities.setNodeValue(node, propertyName, "false"); //$NON-NLS-1$
                            return;
                        }
                        ComponentUtilities.setNodeValue(node, propertyName, "true"); //$NON-NLS-1$
                    }

                }

            };
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

}
