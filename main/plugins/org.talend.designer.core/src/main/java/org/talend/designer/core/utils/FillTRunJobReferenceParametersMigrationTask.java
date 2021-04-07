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
package org.talend.designer.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * Created by bhe on Nov 22, 2019
 */
public class FillTRunJobReferenceParametersMigrationTask extends AbstractItemMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess2());
        return toReturn;
    }

    @Override
    public final ExecutionResult execute(Item item) {
        ProcessType pt = null;
        if (item instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) item;
            pt = processItem.getProcess();
        } else if (item instanceof JobletProcessItem) {
            JobletProcessItem jobletItem = (JobletProcessItem) item;
            pt = jobletItem.getJobletProcess();
        }

        if (pt == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentFilter filter = new NameComponentFilter("tRunJob"); //$NON-NLS-1$
        try {
            ModifyComponentsAction.searchAndModify(item, pt, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        @Override
                        public void transform(NodeType node) {
                            ElementParameterType jobId = ComponentUtilities.getNodeProperty(node,
                                    EParameterName.PROCESS.getName() + ":" + EParameterName.PROCESS_TYPE_PROCESS.getName()); //$NON-NLS-1$
                            if (jobId.getValue() == null || jobId.getValue().isEmpty()) {
                                ElementParameterType jobLabel = ComponentUtilities.getNodeProperty(node,
                                        EParameterName.PROCESS.getName()); // $NON-NLS-1$
                                String id = getIdFormLabel(jobLabel.getValue());
                                if (id != null && !id.isEmpty()) {
                                    jobId.setValue(id);
                                }
                            }
                        }

                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    private static String getIdFormLabel(final String label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        final IProxyRepositoryFactory proxyRepositoryFactory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        try {
            List<IRepositoryViewObject> allRepositoryObject = proxyRepositoryFactory.getAll(ERepositoryObjectType.PROCESS, true);
            for (IRepositoryViewObject repObject : allRepositoryObject) {
                Item item = repObject.getProperty().getItem();
                if (item != null && label.equals(item.getProperty().getLabel())) {
                    return item.getProperty().getId();
                }
            }
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 11, 22, 12, 0, 0);
        return gc.getTime();
    }
}
