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
package org.talend.repository.model.migration.spark;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Migration task that renames the wrong versions name.
 */
public class RenameSparkVersions extends AbstractJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        try {
            ProcessType processType = getProcessType(item);
            if (processType != null) {
                final ParametersType parameters = processType.getParameters();
                if (parameters != null) {
                    boolean modified = false;
                    EList<ElementParameterType> elementParameters = parameters.getElementParameter();
                    for (int i = 0; i < elementParameters.size(); i++) {
                        ElementParameterType param = elementParameters.get(i);
                        if ("SPARK_VERSION".equals(param.getName())) { //$NON-NLS-1$
                            if ("Cloudera_CDH51".equals(param.getValue())) { //$NON-NLS-1$
                                param.setValue("Cloudera_CDH5_1"); //$NON-NLS-1$
                                modified = true;
                            }
                            if ("MAPR_410".equals(param.getValue())) { //$NON-NLS-1$
                                param.setValue("MAPR410"); //$NON-NLS-1$
                                modified = true;
                            }

                            break;
                        }
                    }
                    if (modified) {
                        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                        factory.save(item, true);
                        return ExecutionResult.SUCCESS_NO_ALERT;
                    }

                }
            }
        } catch (PersistenceException e) {
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 10, 20, 13, 0, 0);
        return gc.getTime();
    }
}
