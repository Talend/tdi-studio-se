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
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by nrousseau on Mar 3, 2014 Detailled comment
 *
 */
public class EncryptPasswordInComponentsMigrationTask extends AbstractItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.JOBLET);
        toReturn.add(ERepositoryObjectType.PROCESS);
        if (ERepositoryObjectType.PROCESS_MR != null) {
            toReturn.add(ERepositoryObjectType.PROCESS_MR);
        }
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 1, 4, 15, 0, 0);
        return gc.getTime();
    }

    public ProcessType getProcessType(Item item) {
        ProcessType processType = null;
        if (item instanceof ProcessItem) {
            processType = ((ProcessItem) item).getProcess();
        }
        if (item instanceof JobletProcessItem) {
            processType = ((JobletProcessItem) item).getJobletProcess();
        }
        if (processType != null) {
            EmfHelper.visitChilds(processType);
        }
        return processType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        try {
            boolean modified = false;
            ComponentCategory category = ComponentCategory.getComponentCategoryFromItem(item);
            for (Object nodeObjectType : processType.getNode()) {
                NodeType nodeType = (NodeType) nodeObjectType;
                IComponent component = ComponentsFactoryProvider.getInstance().get(nodeType.getComponentName(),
                        category.getName());
                if (component == null) {
                    continue;
                }
                FakeNode fNode = new FakeNode(component);
                for (Object paramObjectType : nodeType.getElementParameter()) {
                    ElementParameterType param = (ElementParameterType) paramObjectType;
                    IElementParameter paramFromEmf = fNode.getElementParameter(param.getName());
                    if (paramFromEmf == null) {
                        // means some parameters existing in the job for this component, but not on emf component
                        // might be some deprecated / removed parameter.
                        continue;
                    }
                    EParameterFieldType paramFromComponent = paramFromEmf.getFieldType();
                    if (EParameterFieldType.PASSWORD == paramFromComponent && param.getValue() != null) {
                        boolean encrypted = true;
                        try {
                            int ind = param.getValue().lastIndexOf(PasswordEncryptUtil.ENCRYPT_KEY);
                            if (ind == -1) {
                                encrypted = false;
                            } else {
                                String value = new StringBuilder(param.getValue()).replace(ind,
                                        ind + PasswordEncryptUtil.ENCRYPT_KEY.length(), "").toString();
                                PasswordEncryptUtil.decryptPassword(value);
                            }
                        } catch (Exception e) {
                            encrypted = false;
                        }

                        if (!encrypted) {
                            param.setValue(
                                    PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(param.getValue()))
                                    + PasswordEncryptUtil.ENCRYPT_KEY);
                            modified = true;
                        }
                    }
                }
            }
            if (modified) {
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                factory.save(item, true);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    public static class FakeNode extends AbstractNode {

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
