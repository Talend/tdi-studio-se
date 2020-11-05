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
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * TBD-2564: Change the SparkConf design to split local and cluster mode behavior to ease SparkJobServer integration
 */
public class ChangeLocalModeForSparkConfiguration extends AbstractJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        // PROCESS_MR stands for Map/Reduce and Spark.
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
                        if ("SPARK_MODE".equals(param.getName())) { //$NON-NLS-1$
                            modified = true;
                            ElementParameterType property = TalendFileFactory.eINSTANCE.createElementParameterType();
                            property.setName("SPARK_LOCAL_MODE"); //$NON-NLS-1$
                            property.setField("CHECK"); //$NON-NLS-1$
                            property.setValue("false"); //$NON-NLS-1$
                            if ("LOCAL".equalsIgnoreCase(param.getValue())) { //$NON-NLS-1$
                                param.setValue("CLUSTER"); //$NON-NLS-1$
                                property.setValue("true"); //$NON-NLS-1$
                            }
                            elementParameters.add(property);
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

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 9, 9, 12, 0, 0);
        return gc.getTime();
    }

}
