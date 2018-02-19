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

public class AddDefaultFTPSPort extends AbstractJobMigrationTask {

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

                @Override
                public void transform(NodeType node) {

                    String propertyType = "TEXT"; //$NON-NLS-1$
                    String ftpsPortPropertyName = "FTPS_PORT"; //$NON-NLS-1$
                    String ftpsPropertyName = "FTPS"; //$NON-NLS-1$
                    String oldPortPropertyName = "PORT"; //$NON-NLS-1$

                    if (ComponentUtilities.getNodeProperty(node, ftpsPortPropertyName) == null) {
                        ComponentUtilities.addNodeProperty(node, ftpsPortPropertyName, propertyType);
                    }
                    ElementParameterType ftpsProperty = ComponentUtilities.getNodeProperty(node, ftpsPropertyName);
                    if ((ftpsProperty != null) && ("true".equals(ftpsProperty.getValue()))) { //$NON-NLS-1$
                        ElementParameterType oldPortProperty = ComponentUtilities.getNodeProperty(node, oldPortPropertyName);
                        if (oldPortProperty != null) {
                            String oldPortValue = oldPortProperty.getValue();
                            ComponentUtilities.setNodeValue(node, ftpsPortPropertyName, oldPortValue); // $NON-NLS-1$
                        }
                        return;
                    }

                    ComponentUtilities.setNodeValue(node, ftpsPortPropertyName, DEFAULT_FTPS_PORT); // $NON-NLS-1$

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
            return null;
        }
    }

}
