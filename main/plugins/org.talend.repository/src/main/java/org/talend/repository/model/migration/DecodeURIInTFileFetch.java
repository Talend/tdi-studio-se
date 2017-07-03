package org.talend.repository.model.migration;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

public class DecodeURIInTFileFetch extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 7, 3, 14, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            String componentsName = new String("tFileFetch"); //$NON-NLS-1$

            IComponentFilter filter = new NameComponentFilter(componentsName);
            IComponentConversion addOption = new IComponentConversion() {

                @Override
                public void transform(NodeType node) {

                    String uriPropertyType = "TEXT"; //$NON-NLS-1$
                    String uriPropertyName = "URI"; //$NON-NLS-1$
                    String protocolPropertyName = "PROTO"; //$NON-NLS-1$

                    ElementParameterType uriProperty = ComponentUtilities.getNodeProperty(node, uriPropertyName);
                    ElementParameterType protocolName = ComponentUtilities.getNodeProperty(node, protocolPropertyName);

                    // decode URI using URLDecoder
                    if ((protocolName != null)
                            && (("http".equals(protocolName.getValue())) || ("https".equals(protocolName.getValue())))) { //$NON-NLS-1$

                        if (uriProperty == null) {
                            ComponentUtilities.addNodeProperty(node, uriPropertyName, uriPropertyType);
                            return; // no need to decode URI
                        }

                        String URI = uriProperty.getValue();
                        try {
                            URI = URLDecoder.decode(URI, "UTF-8");
                            ComponentUtilities.setNodeValue(node, uriPropertyName, URI); // $NON-NLS-1$
                        } catch (UnsupportedEncodingException e) {
                            ExceptionHandler.process(e);
                        }
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
