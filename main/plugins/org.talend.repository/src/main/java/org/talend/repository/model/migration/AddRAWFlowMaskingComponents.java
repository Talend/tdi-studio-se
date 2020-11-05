// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ConnectionTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.NodeTypeImpl;

/**
 * The class for AddRAWFlowTPatternMasking
 */
public class AddRAWFlowMaskingComponents extends AbstractJobMigrationTask {

    private ProcessType processType = null;

    private final String OLDCONNECTORNAME = "FLOW"; //$NON-NLS-1$

    private final String NEWCONNECTORNAME = "FLOW_OUTPUT"; //$NON-NLS-1$

    private final String RAWCONNECTORNAME = "RAW"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 8, 1, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = super.getTypes();
        // PROCESS_MR stands for Map/Reduce and Spark.
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        return toReturn;
    }



    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        processType = getProcessType(item);

        try {
            IComponentFilter filter = new NameComponentFilter("tPatternMasking"); //$NON-NLS-1$
            IComponentConversion unCheckRaw = new UnCheckRAWFlow();
            IComponentConversion changeMetadataName = new ChangeMetadataName();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter,
                            Arrays.<IComponentConversion> asList(unCheckRaw, changeMetadataName));

            filter = new NameComponentFilter("tPatternUnmasking"); //$NON-NLS-1$
            unCheckRaw = new UnCheckRAWFlow();
            changeMetadataName = new ChangeMetadataName();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter,
                            Arrays.<IComponentConversion> asList(unCheckRaw, changeMetadataName));

            filter = new NameComponentFilter("tDataMasking"); //$NON-NLS-1$
            unCheckRaw = new UnCheckRAWFlow();
            changeMetadataName = new ChangeMetadataName();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter,
                            Arrays.<IComponentConversion> asList(unCheckRaw, changeMetadataName));

            filter = new NameComponentFilter("tDataUnmasking"); //$NON-NLS-1$
            unCheckRaw = new UnCheckRAWFlow();
            changeMetadataName = new ChangeMetadataName();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter,
                            Arrays.<IComponentConversion> asList(unCheckRaw, changeMetadataName));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

    private class UnCheckRAWFlow implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            ElementParameterType seperateProperty = ComponentUtilities.getNodeProperty(node, "SEPERATE_OUTPUT"); //$NON-NLS-1$
            if (seperateProperty == null) {
                seperateProperty = TalendFileFactory.eINSTANCE.createElementParameterType();
                seperateProperty.setName("SEPERATE_OUTPUT"); //$NON-NLS-1$
                seperateProperty.setField(EParameterFieldType.CHECK.getName());
                seperateProperty.setValue("false"); //$NON-NLS-1$
                node.getElementParameter().add(seperateProperty);
            }
        }

    }

    private class ChangeMetadataName implements IComponentConversion {

        private MetadataType newMT = null;

        @Override
        public void transform(NodeType node) {
            newMT = null;
            String metadataName = StringUtils.EMPTY;
            EList<?> metadatas = node.getMetadata();
            for (Object o : metadatas) {
                MetadataType mt = (MetadataType) o;
                String connector = mt.getConnector();
                if (OLDCONNECTORNAME.equals(connector) && metadatas.size() == 1) {
                    metadataName = mt.getName();
                    createNewMetadata(node, mt);
                    removeCustomColumn(mt);
                    addRawFlow(node, mt);
                    changeConnection(metadataName);
                    break;
                }
            }
            if (newMT != null) {
                node.getMetadata().add(newMT);
            }
        }

        private void addRawFlow(NodeType node, MetadataType mt) {
            MetadataType rawMT = EcoreUtil.copy(mt);
            rawMT.setConnector(RAWCONNECTORNAME);
            rawMT.setName(RAWCONNECTORNAME);
            ColumnType errorMessage = TalendFileFactory.eINSTANCE.createColumnType();
            errorMessage.setNullable(true);
            errorMessage.setDefaultValue(StringUtils.EMPTY);
            errorMessage.setLength(0);
            errorMessage.setName("ERROR_MESSAGE"); //$NON-NLS-1$
            errorMessage.setType("id_String"); //$NON-NLS-1$
            errorMessage.setOriginalLength(-1);
            errorMessage.setUsefulColumn(true);
            errorMessage.setSourceType(StringUtils.EMPTY);
            errorMessage.setPrecision(0);
            rawMT.getColumn().add(errorMessage);
            node.getMetadata().add(rawMT);

        }

        private void removeCustomColumn(MetadataType mt) {
            if (checkTheColumnExist(mt)) {
                return;
            }
            EList<?> columns = mt.getColumn();
            for (Object theColumnObject : columns) {
                ColumnType theColumn = (ColumnType) theColumnObject;
                if ("ORIGINAL_MARK".equals(theColumn.getName())) { //$NON-NLS-1$
                    columns.remove(theColumnObject);
                    break;
                }
            }

        }

        /**
         * change connnection name
         *
         * @param metadataName
         */
        private void changeConnection(String metadataName) {

            for (Object o : processType.getConnection()) {
                ConnectionType ct = (ConnectionType) o;
                if (metadataName.equals(ct.getMetaname()) && OLDCONNECTORNAME.equals(ct.getConnectorName())) {
                    ct.setConnectorName(NEWCONNECTORNAME);
                    ct.setMetaname(NEWCONNECTORNAME);
                    break;
                }
            }

        }

        /**
         * Create new metadata
         */
        private void createNewMetadata(NodeType node, MetadataType mt) {

            newMT = EcoreUtil.copy(mt);
            newMT.setConnector(NEWCONNECTORNAME);
            newMT.setName(NEWCONNECTORNAME);
            node.getMetadata().add(newMT);
        }

        private boolean checkTheColumnExist(MetadataType mt) {
            EList<ConnectionTypeImpl> connections = processType.getConnection();
            EList<NodeTypeImpl> nodes = processType.getNode();
            String metadataName = mt.getName();
            for (ConnectionTypeImpl theConnection : connections) {
                if (metadataName.equals(theConnection.getTarget())) {
                    for (NodeTypeImpl node : nodes) {

                        EList<MetadataType> previousNodeMetadataTypes = node.getMetadata();
                        for (MetadataType outputMetadataType : previousNodeMetadataTypes) {
                            if (outputMetadataType.getName().equals(theConnection.getMetaname())) {
                                EList<ColumnType> columns = outputMetadataType.getColumn();
                                for (ColumnType theColumn : columns) {
                                    if ("ORIGINAL_MARK".equals(theColumn.getName())) { //$NON-NLS-1$
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }
                    }
                    return false;
                }
            }
            return false;
        }
    }
}
