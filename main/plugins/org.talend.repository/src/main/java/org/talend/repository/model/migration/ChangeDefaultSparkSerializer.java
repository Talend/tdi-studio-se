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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ComponentsAction;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class ChangeDefaultSparkSerializer extends AbstractJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS_SPARK);
        toReturn.add(ERepositoryObjectType.PROCESS_SPARKSTREAMING);
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        toReturn.add(ERepositoryObjectType.PROCESS_STORM);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType != null) {
            try {
                if (needChange(processType)) {
                    changeSparkConfiguration(processType, item);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        }
        return ExecutionResult.NOTHING_TO_DO;

    }

    private boolean needChange(ProcessType processType) {
        NodeType tCache = ComponentsAction.search(processType, new NameComponentFilter("tCacheOut")); //$NON-NLS-1$
        NodeType tReplicate = ComponentsAction.search(processType, new NameComponentFilter("tReplicate")); //$NON-NLS-1$

        if (tCache != null) {
            if (useKryo(tCache)) {
                return false;
            }
        }

        if (tReplicate != null) {
            ElementParameterType cacheOut = ComponentUtilities.getNodeProperty(tReplicate, "CACHEOUTPUT"); //$NON-NLS-1$
            if (cacheOut != null && "true".equals(cacheOut.getValue())) { //$NON-NLS-1$
                if (useKryo(tReplicate)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void changeSparkConfiguration(ProcessType processType, Item item) throws PersistenceException {
        boolean modified = false;
        final ParametersType parameters = processType.getParameters();
        if (parameters != null) {
            EList elementParameters = parameters.getElementParameter();
            ElementParameterType property = TalendFileFactory.eINSTANCE.createElementParameterType();
            property.setName("CUSTOMIZE_SPARK_SERIALIZER"); //$NON-NLS-1$
            property.setField("CHECK"); //$NON-NLS-1$
            property.setValue("true"); //$NON-NLS-1$
            elementParameters.add(property);
            property = TalendFileFactory.eINSTANCE.createElementParameterType();
            property.setName("SPARK_SERIALIZER"); //$NON-NLS-1$
            property.setField("TEXT"); //$NON-NLS-1$
            property.setValue("\"org.apache.spark.serializer.JavaSerializer\""); //$NON-NLS-1$
            elementParameters.add(property);
            for (int i = 0; i < elementParameters.size(); i++) {
                ElementParameterType param = (ElementParameterType) elementParameters.get(i);
                if ("ADVANCED_SETTINGS_CHECK".equals(param.getName())) { //$NON-NLS-1$
                    param.setValue("true"); //$NON-NLS-1$
                    modified = true;
                    break;
                }

            }
        }
        if (modified) {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            factory.save(item, true);
        }
    }

    private boolean useKryo(NodeType node) {
        ElementParameterType storageLevel = ComponentUtilities.getNodeProperty(node, "STORAGELEVEL"); //$NON-NLS-1$
        if (storageLevel != null) {
            String storageLevelValue = storageLevel.getValue();
            if ("MEMORY_ONLY_SER".equals(storageLevelValue) || "MEMORY_AND_DISK_SER".equals(storageLevelValue) //$NON-NLS-1$ //$NON-NLS-2$
                    || "MEMORY_ONLY_SER_2".equals(storageLevelValue) || "MEMORY_AND_DISK_SER_2".equals(storageLevelValue)) { //$NON-NLS-1$ //$NON-NLS-2$
                ElementParameterType useKryo = ComponentUtilities.getNodeProperty(node, "KRYOSER"); //$NON-NLS-1$
                if (useKryo != null) {
                    if ("true".equals(useKryo.getValue())) { //$NON-NLS-1$
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 7, 9, 15, 0, 0);
        return gc.getTime();
    }
}
