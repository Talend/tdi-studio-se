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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
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
 * rdubois class global comment. Detailled comment
 */
public class RenameVersionParametersForPigMigrationTask extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        try {
            renameParameters(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 3, 15, 12, 0, 0);
        return gc.getTime();
    }

    private void renameParameters(Item item) throws Exception {
        ProcessType processType = getProcessType(item);
        IComponentFilter filter = new NameComponentFilter("tPigLoad");
        IComponentConversion addOption = new AddErgonomicOptionConversion();
        ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
    }

    /**
     *
     * AddErgonomicOptionConversion class global comment. Detailled comment
     */
    private class AddErgonomicOptionConversion implements IComponentConversion {

        private String field1 = "RADIO"; //$NON-NLS-1$

        private String name1 = "LOCAL"; //$NON-NLS-1$

        private String field2 = "RADIO"; //$NON-NLS-1$

        private String name2 = "MAPREDUCE"; //$NON-NLS-1$

        private String field3 = "CLOSED_LIST"; //$NON-NLS-1$

        private String name3 = "PIG_VERSION"; //$NON-NLS-1$

        public AddErgonomicOptionConversion() {
            super();
        }

        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, name1) == null) {
                ComponentUtilities.addNodeProperty(node, name1, field1);
            }
            if (ComponentUtilities.getNodeProperty(node, name2) == null) {
                ComponentUtilities.addNodeProperty(node, name2, field2);
            }
            if (ComponentUtilities.getNodeProperty(node, name3) == null) {
                ComponentUtilities.addNodeProperty(node, name3, field3);
            }

            for (Object o : node.getElementParameter()) {
                ElementParameterType para = (ElementParameterType) o;
                if ("DB_VERSION".equals(para.getName())) { //$NON-NLS-1$
                    if ("pig.jar".equals(para.getValue())) {
                        ComponentUtilities.setNodeValue(node, name1, "true"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name2, "false"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name3, "APACHE_0_20_2");
                    } else if ("pig-withouthadoop.jar;hadoop-conf.jar;hadoop-0.20.2-core.jar".equals(para.getValue())) {
                        ComponentUtilities.setNodeValue(node, name1, "false"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name2, "true"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name3, "APACHE_0_20_2"); //$NON-NLS-1$
                    } else if ("pig-withouthadoop.jar;hadoop-conf.jar;hadoop-0.20.2-cdh3u1-core.jar".equals(para.getValue())) {
                        ComponentUtilities.setNodeValue(node, name1, "false"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name2, "true"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name3, "CLOUDERA_0.20_CDH3U1"); //$NON-NLS-1$
                    } else if ("automaton.jar;pig-0.9.0-core.jar;pigperf.jar;sdsuLibJKD12.jar;maprfs-0.1.jar;zookeeper-3.3.2.jar;hadoop-0.20.2-dev-core.jar;hadoop-conf.jar"
                            .equals(para.getValue())) {
                        ComponentUtilities.setNodeValue(node, name1, "false"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name2, "true"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name3, "MAPR"); //$NON-NLS-1$
                    } else if ("pig-0.9.2-withouthadoop.jar;hadoop-core-1.0.0.jar;hadoop-conf.jar;log4j-1.2.15.jar;commons-configuration-1.6.jar;commons-lang-2.4.jar;commons-logging-1.1.1.jar"
                            .equals(para.getValue())) {
                        ComponentUtilities.setNodeValue(node, name1, "false"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name2, "true"); //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, name3, "APACHE_1_0_0"); //$NON-NLS-1$
                    }
                    ComponentUtilities.removeNodeProperty(node, "DB_VERSION");
                    break;
                }

            }
        }
    }
}
