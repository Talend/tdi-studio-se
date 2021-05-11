package org.talend.repository.model.migration;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SetForceCopyFortFileCopy extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(final Item item) {
        final ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] compNames = {"tFileCopy"}; //$NON-NLS-1$

        IComponentConversion action = new IComponentConversion() {

            public void transform(NodeType node) {
                if(node == null) {
                    return;
                }

                if (ComponentUtilities.getNodeProperty(node, "FORCE_COPY_DELETE") == null) {//$NON-NLS-1$
                    ComponentUtilities.addNodeProperty(node, "FORCE_COPY_DELETE", "CHECK");//$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.getNodeProperty(node, "FORCE_COPY_DELETE").setValue("true");//$NON-NLS-1$ //$NON-NLS-2$
                }

            }

        };

        for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(action));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, Calendar.APRIL, 9, 0, 0, 0);
        return gc.getTime();
    }
}
