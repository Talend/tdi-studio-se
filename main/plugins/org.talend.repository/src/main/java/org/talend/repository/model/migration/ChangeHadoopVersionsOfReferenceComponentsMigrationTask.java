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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by JYHU on April 9, 2013 Detailled comment
 *
 */
public class ChangeHadoopVersionsOfReferenceComponentsMigrationTask extends AbstractJobMigrationTask {

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
        String[] componentsName = new String[] { "tPigLoad", //$NON-NLS-1$
                "tHDFSCompare", //$NON-NLS-1$
                "tHDFSConnection",//$NON-NLS-1$
                "tHDFSCopy",//$NON-NLS-1$
                "tHDFSDelete",//$NON-NLS-1$
                "tHDFSExist",//$NON-NLS-1$
                "tHDFSGet",//$NON-NLS-1$
                "tHDFSInput",//$NON-NLS-1$
                "tHDFSList",//$NON-NLS-1$
                "tHDFSOutput",//$NON-NLS-1$
                "tHDFSProperties",//$NON-NLS-1$
                "tHDFSPut",//$NON-NLS-1$
                "tHDFSRename",//$NON-NLS-1$
                "tHDFSRowCount",//$NON-NLS-1$
                "tPigStoreResult",//$NON-NLS-1$
                "tSqoopExport",//$NON-NLS-1$
                "tSqoopImport",//$NON-NLS-1$
                "tSqoopImportAllTables", //$NON-NLS-1$
                //                "tMRConfiguration", //$NON-NLS-1$
                "tGenKeyHadoop", //$NON-NLS-1$
                "tMatchGroupHadoop" }; //$NON-NLS-1$

        try {

            for (String element : componentsName) {
                IComponentFilter filter = new NameComponentFilter(element);
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(new IComponentConversion() {

                            @Override
                            public void transform(NodeType node) {
                                String[] params = { "PIG_VERSION", "DB_VERSION",/* "MR_VERSION", */"HBASE_VERSION" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                ElementParameterType ept = null;
                                for (String param : params) {
                                    ept = ComponentUtilities.getNodeProperty(node, param);
                                    if (ept != null) {
                                        setValue(node, ept, param);
                                        break;
                                    }

                                }
                            }

                        }));
            }

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (java.lang.Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public void setValue(NodeType node, ElementParameterType ept, String param) {
        if ("CLOUDERA_0.20_CDH3U1".equals(ept.getValue()) || "Cloudera_0_20_CDH3U1".equals(ept.getValue())) {//$NON-NLS-1$ //$NON-NLS-2$
            ComponentUtilities.setNodeValue(node, param, "Cloudera_CDH3"); //$NON-NLS-1$
        } else if ("CLOUDERA_CDH4".equals(ept.getValue())) {//$NON-NLS-1$
            ComponentUtilities.setNodeValue(node, param, "Cloudera_CDH4");//$NON-NLS-1$
        } else if ("MapR".equalsIgnoreCase(ept.getValue())) {//$NON-NLS-1$
            ComponentUtilities.setNodeValue(node, param, "MAPR1");//$NON-NLS-1$
        } else if ("MapR2".equals(ept.getValue())) {//$NON-NLS-1$
            ComponentUtilities.setNodeValue(node, param, "MAPR2");//$NON-NLS-1$
        } else if ("MapR212".equals(ept.getValue())) {//$NON-NLS-1$
            ComponentUtilities.setNodeValue(node, param, "MAPR212");//$NON-NLS-1$
        } else if ("HADOOP_1_0_0".equals(ept.getValue())) {//$NON-NLS-1$
            ComponentUtilities.setNodeValue(node, param, "APACHE_1_0_0");//$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 4, 9, 19, 0, 0);
        return gc.getTime();
    }

}
