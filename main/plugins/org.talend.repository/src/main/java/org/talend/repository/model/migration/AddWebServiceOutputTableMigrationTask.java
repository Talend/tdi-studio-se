// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC gldu class global comment. Detailled comment
 */
public class AddWebServiceOutputTableMigrationTask extends AbstractJobMigrationTask {

    private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        // TODO Auto-generated method stub
        try {
            addNewColumn(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private void addNewColumn(Item item) {
        // TODO Auto-generated method stub
        boolean isUpdate = false;
        MetadataType metadata;
        ProcessType processType = getProcessType(item);
        TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
        String webServiceComponentName = "";
        NodeType webServiceNode = null;
        for (Object o : processType.getNode()) {
            if (o instanceof NodeType) {
                NodeType currentNode = (NodeType) o;
                if ("tWebService".equals(currentNode.getComponentName())) {
                    webServiceNode = currentNode;
                    if (currentNode.getMetadata().size() == 1) {
                        isUpdate = true;
                        for (Object e : currentNode.getElementParameter()) {
                            ElementParameterType p = (ElementParameterType) e;
                            if ("UNIQUE_NAME".equals(p.getName())) {
                                webServiceComponentName = p.getValue();
                                MetadataType newMetadata = (MetadataType) currentNode.getMetadata().get(0);
                                newMetadata.getColumn().clear();
                            }
                        }
                    }
                }
            }
        }
        if (isUpdate) {
            String name = "";
            String webSourceName = "";
            String webserviceOutNaame = "";
            for (Object o : processType.getConnection()) {
                if (o instanceof ConnectionType) {
                    ConnectionType currentConnection = (ConnectionType) o;
                    if ("FLOW".equals(currentConnection.getConnectorName())
                            && currentConnection.getTarget().equals(webServiceComponentName)) {
                        webSourceName = currentConnection.getSource();
                    } else if ("FLOW".equals(currentConnection.getConnectorName())
                            && currentConnection.getSource().equals(webServiceComponentName)) {
                        webserviceOutNaame = currentConnection.getTarget();
                        currentConnection.setConnectorName("OUTPUT");
                        currentConnection.setMetaname("OUTPUT");
                    }
                }
            }
            for (Object o : processType.getNode()) {
                if (o instanceof NodeType) {
                    NodeType currentNode = (NodeType) o;
                    for (Object e : currentNode.getElementParameter()) {
                        ElementParameterType p = (ElementParameterType) e;
                        if ("UNIQUE_NAME".equals(p.getName()) && webSourceName.equals(p.getValue())) {
                            MetadataType newMetadata = (MetadataType) currentNode.getMetadata().get(0);
                            metadata = ((MetadataType) webServiceNode.getMetadata().get(0));
                            EList<ColumnType> list = newMetadata.getColumn();
                            for (int i = 0; i < list.size(); i++) {
                                ColumnType in = (ColumnType) list.get(i);
                                ColumnType out = fileFact.createColumnType();
                                out.setComment(in.getComment());
                                out.setKey(in.isKey());
                                out.setNullable(in.isNullable());
                                if (String.valueOf(in.getLength()).equals("0")) {
                                    out.unsetLength();
                                } else {
                                    out.setLength(in.getLength());
                                }
                                out.setName(in.getName());
                                if (String.valueOf(in.getPrecision()).equals("0")) {
                                    out.unsetPrecision();
                                } else {
                                    out.setPrecision(in.getPrecision());
                                }
                                if (!in.getName().equals(in.getOriginalDbColumnName())) {
                                    out.setOriginalDbColumnName(in.getOriginalDbColumnName());
                                }
                                out.setType(in.getType());
                                out.setSourceType(in.getSourceType());
                                out.setPattern(in.getPattern());
                                out.setDefaultValue(in.getDefaultValue());
                                metadata.getColumn().add(out);
                            }
                        } else if ("UNIQUE_NAME".equals(p.getName()) && webserviceOutNaame.equals(p.getValue())) {
                            MetadataType newMetadata = fileFact.createMetadataType();
                            metadata = ((MetadataType) currentNode.getMetadata().get(0));
                            name = metadata.getName();
                            EList<ColumnType> list = metadata.getColumn();
                            newMetadata.setName("OUTPUT");
                            newMetadata.setConnector("OUTPUT");
                            for (int i = 0; i < list.size(); i++) {
                                ColumnType in = (ColumnType) list.get(i);
                                ColumnType out = fileFact.createColumnType();
                                out.setComment(in.getComment());
                                out.setKey(in.isKey());
                                out.setNullable(in.isNullable());
                                if (String.valueOf(in.getLength()).equals("0")) {
                                    out.unsetLength();
                                } else {
                                    out.setLength(in.getLength());
                                }
                                out.setName(in.getName());
                                if (String.valueOf(in.getPrecision()).equals("0")) {
                                    out.unsetPrecision();
                                } else {
                                    out.setPrecision(in.getPrecision());
                                }
                                if (!in.getName().equals(in.getOriginalDbColumnName())) {
                                    out.setOriginalDbColumnName(in.getOriginalDbColumnName());
                                }
                                out.setType(in.getType());
                                out.setSourceType(in.getSourceType());
                                out.setPattern(in.getPattern());
                                out.setDefaultValue(in.getDefaultValue());
                                newMetadata.getColumn().add(out);
                                webServiceNode.getMetadata().add(newMetadata);
                            }
                        }
                    }
                }
            }
        }

        if (isUpdate) {
            try {
                FACTORY.save(item, true);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 7, 05, 17, 41, 10);
        return gc.getTime();
    }

}
