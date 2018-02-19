package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
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
import org.talend.migration.IMigrationTask.ExecutionResult;

public class AddDefaultFTPSPortMigrationTask extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 2, 19, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            String connectionComponentsName = new String("tFTPConnection"); //$NON-NLS-1$
            String getComponentsName = new String("tFTPGet"); //$NON-NLS-1$
            String putComponentsName = new String("tFTPPut"); //$NON-NLS-1$

            IComponentFilter connectionFilter = new NameComponentFilter(connectionComponentsName);
            IComponentFilter getFilter = new NameComponentFilter(getComponentsName);
            IComponentFilter putFilter = new NameComponentFilter(putComponentsName);

            IComponentConversion addOptionConversion = new IComponentConversion() {

                private static final String DEFAULT_FTPS_PORT = "990"; //$NON-NLS-1$

                private static final String PROPERTY_TYPE = "TEXT"; //$NON-NLS-1$

                private static final String FTPS_PORT_PROPERTY_NAME = "FTPS_PORT"; //$NON-NLS-1$

                private static final String FTPS_PROPERTY_NAME = "FTPS"; //$NON-NLS-1$

                private static final String OLD_PORT_PROPERTY_NAME = "PORT"; //$NON-NLS-1$

                @Override
                public void transform(NodeType node) {
                    if (ComponentUtilities.getNodeProperty(node, FTPS_PORT_PROPERTY_NAME) == null) {
                        ComponentUtilities.addNodeProperty(node, FTPS_PORT_PROPERTY_NAME, PROPERTY_TYPE);
                    }
                    ElementParameterType ftpsProperty = ComponentUtilities.getNodeProperty(node, FTPS_PROPERTY_NAME);
                    ElementParameterType oldPortProperty = ComponentUtilities.getNodeProperty(node, OLD_PORT_PROPERTY_NAME);
                    if ((ftpsProperty != null) && (oldPortProperty != null) && ("true".equals(ftpsProperty.getValue()))) { //$NON-NLS-1$
                        String oldPortValue = oldPortProperty.getValue();
                        ComponentUtilities.setNodeValue(node, FTPS_PORT_PROPERTY_NAME, oldPortValue);
                    } else {
                        ComponentUtilities.setNodeValue(node, FTPS_PORT_PROPERTY_NAME, DEFAULT_FTPS_PORT);
                    }

                }

            };

            ModifyComponentsAction.searchAndModify(item, processType, connectionFilter,
                    Arrays.<IComponentConversion> asList(addOptionConversion));
            ModifyComponentsAction.searchAndModify(item, processType, getFilter,
                    Arrays.<IComponentConversion> asList(addOptionConversion));
            ModifyComponentsAction.searchAndModify(item, processType, putFilter,
                    Arrays.<IComponentConversion> asList(addOptionConversion));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

}
