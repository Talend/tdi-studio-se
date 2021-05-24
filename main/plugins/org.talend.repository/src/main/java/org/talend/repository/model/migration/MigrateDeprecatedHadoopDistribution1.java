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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractAllJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * This migration task migrates distributions which don't exist anymore to their upper version. It's suffixed with a 1
 * because similar migration task could exist in the future.
 */
public class MigrateDeprecatedHadoopDistribution1 extends AbstractAllJobMigrationTask {

    Map<String, String> values = new HashMap<String, String>();

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType != null) {
            String[] dbVersionComponents = {
                    "tHDFSConnection", "tHDFSCompare", "tHDFSDelete", "tHDFSCopy", "tHDFSExist", "tHDFSGet", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                    "tHDFSInput", "tHDFSOutput", "tHDFSList", "tHDFSOutputRaw", "tHDFSProperties", "tHDFSPut", "tHDFSRename", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                    "tHDFSRowCount", "tSqoopImport", "tSqoopExport", "tSqoopMerge", "tSqoopImportAllTables" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

            String[] hbaseVersionComponents = { "tPigStoreResult", "tHBaseConnection", "tHBaseInput", "tHBaseOutput" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            String[] hcatVersionComponents = { "tHCatalogOperation", "tHCatalogLoad", "tHCatalogInput", "tHCatalogOutput" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            String[] hiveVersionComponents = {
                    "tHiveConnection", "tHiveRow", "tHiveInput", "tHiveLoad", "tHiveCreateTable", "tELTHiveMap" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

            values.put("HDP_1_0", "HDP_1_2"); //$NON-NLS-1$ //$NON-NLS-2$
            values.put("Cloudera_CDH3", "Cloudera_CDH4"); //$NON-NLS-1$ //$NON-NLS-2$
            values.put("MAPR1", "MAPR2"); //$NON-NLS-1$ //$NON-NLS-2$
            values.put("MapR_EMR", "APACHE_1_0_3_EMR"); //$NON-NLS-1$ //$NON-NLS-2$

            class ComponentConversion implements IComponentConversion {

                private String parameter;

                public ComponentConversion(String parameterName) {
                    this.parameter = parameterName;
                }

                @Override
                public void transform(NodeType node) {
                    ElementParameterType dbVersion = ComponentUtilities.getNodeProperty(node, parameter);
                    // dbVersion can be null for tHDFSInput and tHDFSOutput in M/R. In this case, ignore the task.
                    if (dbVersion != null && dbVersion.getValue() != null) {
                        String dbVersionValue = dbVersion.getValue();
                        replaceValue(node, dbVersionValue, parameter);
                    }
                }

            }

            IComponentConversion changeDbVersion = new ComponentConversion("DB_VERSION"); //$NON-NLS-1$
            IComponentConversion changeHbaseVersion = new ComponentConversion("HBASE_VERSION"); //$NON-NLS-1$
            IComponentConversion changeHcatVersion = new ComponentConversion("HCAT_VERSION"); //$NON-NLS-1$
            IComponentConversion changePigVersion = new ComponentConversion("PIG_VERSION"); //$NON-NLS-1$
            IComponentConversion changeHiveVersion = new ComponentConversion("HIVE_VERSION"); //$NON-NLS-1$
            IComponentConversion changeMRVersion = new ComponentConversion("MR_VERSION"); //$NON-NLS-1$

            try {
                searchAndModify(dbVersionComponents, item, processType, changeDbVersion);
                searchAndModify(hbaseVersionComponents, item, processType, changeHbaseVersion);
                searchAndModify(hcatVersionComponents, item, processType, changeHcatVersion);
                searchAndModify(new String[] { "tPigLoad" }, item, processType, changePigVersion); //$NON-NLS-1$
                searchAndModify(hiveVersionComponents, item, processType, changeHiveVersion);
                changeMRConfiguration(processType, item);
                searchAndModify(new String[] { "tMRConfiguration" }, item, processType, changeMRVersion); //$NON-NLS-1$
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }

            return ExecutionResult.SUCCESS_NO_ALERT;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private void changeMRConfiguration(ProcessType processType, Item item) throws PersistenceException {
        boolean modified = false;
        final ParametersType parameters = processType.getParameters();
        if (parameters != null) {
            EList elementParameters = parameters.getElementParameter();
            for (int i = 0; i < elementParameters.size(); i++) {
                ElementParameterType param = (ElementParameterType) elementParameters.get(i);
                if ("MR_VERSION".equals(param.getName())) { //$NON-NLS-1$
                    if (values.containsKey(param.getValue())) {
                        param.setValue(values.get(param.getValue()));
                        modified = true;
                    }
                    break;
                }
            }
        }
        if (modified) {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            factory.save(item, true);
        }
    }

    private void searchAndModify(String[] components, Item item, ProcessType processType, IComponentConversion conversion)
            throws PersistenceException {
        for (String name : components) {
            IComponentFilter filter = new NameComponentFilter(name);
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(conversion));
        }
    }

    private void replaceValue(NodeType node, String dbVersionValue, String param) {
        if (values.containsKey(dbVersionValue)) {
            ComponentUtilities.setNodeValue(node, param, values.get(dbVersionValue));
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 07, 1, 16, 0, 0);
        return gc.getTime();
    }
}
