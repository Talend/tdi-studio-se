package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.commons.exception.PersistenceException;
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

public class CheckAzureBlobStorageKeepRemoteDirJavajetToTCOMPv0 extends AbstractJobMigrationTask {
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentFilter filter = new NameComponentFilter("tAzureStorageGet"); //$NON-NLS-1$
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion>asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            ElementParameterType exceptionOnEileExist = ComponentUtilities.getNodeProperty(node,
                                    "keepRemoteDirStructure"); //$NON-NLS-1$
                            if(exceptionOnEileExist==null){
                                ComponentUtilities.addNodeProperty(node, "keepRemoteDirStructure", "CHECK");
                                exceptionOnEileExist = ComponentUtilities.getNodeProperty(node, "keepRemoteDirStructure");
                                exceptionOnEileExist.setValue("true");
                            }
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_WITH_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, Calendar.AUGUST, 20, 12, 0, 0);
        return gc.getTime();
    }
}
