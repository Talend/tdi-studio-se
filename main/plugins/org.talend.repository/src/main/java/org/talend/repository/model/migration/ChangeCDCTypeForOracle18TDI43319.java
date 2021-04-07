package org.talend.repository.model.migration;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
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
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class ChangeCDCTypeForOracle18TDI43319 extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        IComponentFilter filter = new NameComponentFilter("tOracleCDC");
        IComponentConversion conversion = new IComponentConversion() {

            public void transform(NodeType node) {
                String dbVersion = null;
                ElementParameterType useExistingConn =
                        ComponentUtilities.getNodeProperty(node, "USE_EXISTING_CONNECTION"); //$NON-NLS-1$ //$NON-NLS-2$
                if (useExistingConn != null && "true".equalsIgnoreCase(useExistingConn.getValue())) {
                    ElementParameterType connNamePara = ComponentUtilities.getNodeProperty(node, "CONNECTION"); //$NON-NLS-1$
                    if (connNamePara != null) {
                        NodeType tConnNode = searchNodeTypeByUniqName(processType, connNamePara.getValue());
                        if (tConnNode != null) {
                            dbVersion = ComponentUtilities.getNodePropertyValue(tConnNode, "DB_VERSION");
                        }
                    }
                } else {
                    dbVersion = ComponentUtilities.getNodePropertyValue(node, "DB_VERSION");
                }
                if ("ORACLE_18".equals(dbVersion)) {
                    String cdcMode = ComponentUtilities.getNodePropertyValue(node, "CDC_MODE");
                    if ("LOG".equals(cdcMode)) {
                        ComponentUtilities.setNodeValue(node, "CDC_MODE", "LOG_UNSUPPORTED");
                    }
                }
            }
        };
        try {
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter, Collections.singletonList(conversion));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    // Search for connection node
    private NodeType searchNodeTypeByUniqName(ProcessType processType, String uniqName) {
        if (processType == null || (uniqName == null || "".equals(uniqName.trim()))) {
            return null;
        }
        NodeType searchNode = null;
        EList<NodeType> nodes = processType.getNode();
        for (NodeType node : nodes) {
            if (node.getComponentName().equals("tOracleConnection")) { //$NON-NLS-1$
                ElementParameterType uniqNameParam = ComponentUtilities.getNodeProperty(node, "UNIQUE_NAME");
                if (uniqNameParam != null && uniqNameParam.getValue().equals(uniqName)) {
                    searchNode = node;
                    break;
                }
            }
        }

        return searchNode;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 11, 9, 1, 0, 0);
        return gc.getTime();
    }
}
