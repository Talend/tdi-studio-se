package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

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

public class KeepChangeRemoteDirFalseForFTPFileExist extends AbstractJobMigrationTask {
    private final static String PROPERTY_TYPE = "CHECK"; //$NON-NLS-1$
    private final static String PROPERTY_NAME = "MOVE_TO_THE_CURRENT_DIRECTORY"; //$NON-NLS-1$
    private final static String COMPONENT_NAME = "tFTPFileExist"; //$NON-NLS-1$
    
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 4, 30, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        
        String componentsName = new String(COMPONENT_NAME);

        IComponentFilter filter = new NameComponentFilter(componentsName);
        IComponentConversion addOption = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {

                if (ComponentUtilities.getNodeProperty(node, PROPERTY_NAME) == null) {
                    ComponentUtilities.addNodeProperty(node, PROPERTY_NAME, PROPERTY_TYPE);
                }
                //set false to the value after migration
                ComponentUtilities.setNodeValue(node, PROPERTY_NAME, "false"); //$NON-NLS-1$
            }

        };
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

}
