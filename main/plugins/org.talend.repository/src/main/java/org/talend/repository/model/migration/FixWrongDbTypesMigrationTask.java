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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MappingType;
import org.talend.core.model.metadata.MappingTypeRetriever;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * // * This migration only fixes some problem of case for the dbtypes<br>
 * If the DB Types is really empty, it won't set anything by default to try to keep the same as before.
 *
 */
public class FixWrongDbTypesMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 9, 16, 0, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        boolean modified = false;
        for (Object nodeTypeObject : processType.getNode()) {
            NodeType nodeType = (NodeType) nodeTypeObject;
            IComponent component = ComponentsFactoryProvider.getInstance().get(nodeType.getComponentName(),
                    ComponentCategory.CATEGORY_4_DI.getName());
            if (component == null) {
                continue; // in case original component doesn't exist here
            }
            FakeNode fNode = new FakeNode(component);
            IElementParameter mappingParameter = fNode.getElementParameterFromField(EParameterFieldType.MAPPING_TYPE);
            if (mappingParameter != null && mappingParameter.getValue() != null) {
                String mappingParameterValue = (String) mappingParameter.getValue();
                MappingTypeRetriever mtr = MetadataTalendType.getMappingTypeRetriever(mappingParameterValue);
                if (mtr == null) {
                    continue;
                }
                for (Object metadataObject : nodeType.getMetadata()) {
                    MetadataType metadataType = (MetadataType) metadataObject;
                    for (Object columnObject : metadataType.getColumn()) {
                        ColumnType columnType = (ColumnType) columnObject;
                        if (columnType.getSourceType() != null && !"".equals(columnType.getSourceType())) {
                            if (mtr.isAdvicedTalendToDbType(columnType.getType(), columnType.getSourceType())) {
                                // correct type already, no need to do anything
                                continue;
                            }
                            List<MappingType> advicedTalendToDbTypes = mtr.getAdvicedTalendToDbTypes(columnType.getType());
                            if (advicedTalendToDbTypes == null) {
                                continue;
                            }
                            String dbType = columnType.getSourceType();
                            for (MappingType type : advicedTalendToDbTypes) {
                                if (type.getDbType().equalsIgnoreCase(dbType)) {
                                    columnType.setSourceType(type.getDbType());
                                    modified = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (modified) {
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

    private class FakeNode extends AbstractNode {

        /**
         * DOC nrousseau FakeNode constructor comment.
         */
        public FakeNode(IComponent component) {
            super();
            setComponentName(component.getName());
            List<IMetadataTable> metaList = new ArrayList<IMetadataTable>();
            IMetadataTable metaTable = new MetadataTable();
            metaTable.setTableName("test");
            metaList.add(metaTable);
            setMetadataList(metaList);
            setComponent(component);
            setElementParameters(component.createElementParameters(this));
            setListConnector(component.createConnectors(this));
            setUniqueName("test");
            setHasConditionalOutputs(component.hasConditionalOutputs());
            setIsMultiplyingOutputs(component.isMultiplyingOutputs());
        }

    }
}
