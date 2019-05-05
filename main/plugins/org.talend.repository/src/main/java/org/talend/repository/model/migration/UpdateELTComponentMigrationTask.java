package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IDbMapService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.MetadataTypeImpl;

public class UpdateELTComponentMigrationTask extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 7, 19, 12, 00, 00);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        boolean isModified = false;
        ProcessType processType = getProcessType(item);
        if(processType == null){
            return ExecutionResult.NOTHING_TO_DO; 
        }
        List nodes = processType.getNode();
        List<ConnectionType> connections = processType.getConnection();
        for (ConnectionType connection : connections) {
            String sourceNodeName = connection.getSource();
            String targetNodeName = connection.getTarget();
            if (sourceNodeName.matches("tELT.+Input.+") && targetNodeName.matches("tELT.+Map.+")) { //$NON-NLS-1$ //$NON-NLS-2$
                NodeType eltInputNode = getNodeTypeByUniqueName(nodes, sourceNodeName);
                String orginalTableName = getPropertyValue(eltInputNode, "ELT_TABLE_NAME"); //$NON-NLS-1$
                String orginalSchemaName = getPropertyValue(eltInputNode, "ELT_SCHEMA_NAME"); //$NON-NLS-1$
                if (orginalSchemaName == null) {
                    continue;
                }
                String tableName = TalendQuoteUtils.removeQuotes(orginalTableName);
                String schemaName = TalendQuoteUtils.removeQuotes(orginalSchemaName);
                String connectionName;
                if (schemaName.trim().equals("")) { //$NON-NLS-1$
                    connectionName = tableName;
                } else {
                    connectionName = schemaName + "." + tableName; //$NON-NLS-1$
                }
                if (connection.getLabel().equals(tableName)) {
                    connection.setLabel(connectionName);
                    isModified = true;
                } else {
                    // if user custom connection name, keep everything currently they had.
                    break;
                }
                MetadataTypeImpl table = (MetadataTypeImpl) eltInputNode.getMetadata().get(0);
                if (table.getLabel().equals(tableName)) {
                    table.setLabel(connectionName);
                }

                NodeType eltMapNode = getNodeTypeByUniqueName(nodes, targetNodeName);
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDbMapService.class)) {
                    IDbMapService service = (IDbMapService) GlobalServiceRegister.getDefault().getService(IDbMapService.class);
                    service.updateEMFDBMapData(eltMapNode, tableName, connectionName);
                }
            }
        }
        if (isModified) {
            try {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private NodeType getNodeTypeByUniqueName(List nodes, String uniqueName) {
        for (Object node : nodes) {
            NodeType nodeType = (NodeType) node;
            String name = getPropertyValue((NodeType) node, "UNIQUE_NAME"); //$NON-NLS-1$
            if (name.equals(uniqueName)) {
                return nodeType;
            }
        }
        return null;
    }

    private String getPropertyValue(NodeType node, String name) {
        for (Object param : node.getElementParameter()) {
            ElementParameterType paramType = (ElementParameterType) param;
            if (name.equals(paramType.getName())) {
                return paramType.getValue();
            }
        }
        return null;
    }

}
