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

import org.talend.commons.exception.PersistenceException;
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
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeHadoopVersionValue4TDQComponents extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        String[] tdqHadoopCompNames = { "tGenKeyHadoop", "tMatchGroupHadoop"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


        IComponentConversion changeHDFSVersionValue = new IComponentConversion() {

            public void transform(NodeType node) {
                ElementParameterType version = ComponentUtilities.getNodeProperty(node, "DB_VERSION"); //$NON-NLS-1$
                if (version != null) {
                    String value = version.getValue();
                    if(value == null) {
                        return;
                    }
                    if (value.equals("hadoop-core-0.20.204.0.jar;commons-logging-1.1.1.jar")) { //$NON-NLS-1$
                        version.setValue("APACHE_0_20_204"); //$NON-NLS-1$
                    }else if (value.startsWith("hadoop-0.20.2-cdh3u1-core.jar;commons-logging-1.0.4.jar")) { //$NON-NLS-1$
                        version.setValue("Cloudera_0_20_CDH3U1"); //$NON-NLS-1$
                    }else if (value.startsWith("hadoop-core-1.0.0.jar;commons-logging-1.1.1.jar")) { //$NON-NLS-1$
                        version.setValue("HADOOP_1_0_0"); //$NON-NLS-1$
                    }else if (value.startsWith("hadoop-core-1.0.0.jar;commons-logging-1.1.1.jar;hdp-dummy.jar")) { //$NON-NLS-1$
                        version.setValue("HDP_1_0"); //$NON-NLS-1$
                    }else if (value.startsWith("hadoop-auth-2.0.0-cdh4.0.1.jar;hadoop-common-2.0.0-cdh4.0.1.jar;hadoop-hdfs-2.0.0-cdh4.0.1.jar;hadoop-core-2.0.0-mr1-cdh4.0.1.jar;protobuf-java-2.4.0a.jar;slf4j-api-1.6.1.jar;slf4j-log4j12-1.6.1.jar;guava-11.0.2.jar;commons-logging-1.1.1.jar")) { //$NON-NLS-1$
                        version.setValue("Cloudera_CDH4"); //$NON-NLS-1$
                    }
                }
            }

        };

        for (String name : tdqHadoopCompNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(changeHDFSVersionValue));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 0, 25, 14, 0, 0);
        return gc.getTime();
    }
}
