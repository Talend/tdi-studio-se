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
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

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
 * ADD sizhaoliu for TDQ-7635
 */
public class AddNewFieldsForTDQHadoopComponentsTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentConversion changeNamenodeURIValue = new IComponentConversion() {

            public void transform(NodeType node) {
                if (node == null) {
                    return;
                }

                ElementParameterType namenodeURI = ComponentUtilities.getNodeProperty(node, "FS_DEFAULT_NAME"); //$NON-NLS-1$
                if (namenodeURI == null) {
                    ElementParameterType host = ComponentUtilities.getNodeProperty(node, "HOST"); //$NON-NLS-1$
                    ElementParameterType port = ComponentUtilities.getNodeProperty(node, "PORT"); //$NON-NLS-1$
                    ComponentUtilities.addNodeProperty(node, "FS_DEFAULT_NAME", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.setNodeValue(node,
                            "FS_DEFAULT_NAME", "\"hdfs://\" + " + host.getValue() + " + \":\" + " + port.getValue() + " + \"/\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                ElementParameterType distribution = ComponentUtilities.getNodeProperty(node, "DISTRIBUTION"); //$NON-NLS-1$
                if (distribution == null) {
                    ElementParameterType dbVersion = ComponentUtilities.getNodeProperty(node, "DB_VERSION"); //$NON-NLS-1$
                    ComponentUtilities.addNodeProperty(node, "DISTRIBUTION", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                    String dbVersionValue = dbVersion.getValue();
                    if (dbVersionValue.endsWith("_EMR")) { //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, "DISTRIBUTION", "AMAZON_EMR"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (dbVersionValue.startsWith("HDP_")) { //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, "DISTRIBUTION", "HORTONWORKS"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (dbVersionValue.startsWith("Cloudera_")) { //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, "DISTRIBUTION", "CLOUDERA"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (dbVersionValue.startsWith("MAPR")) { //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, "DISTRIBUTION", "MAPR"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (dbVersionValue.startsWith("APACHE_")) { //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, "DISTRIBUTION", "APACHE"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }

        };

        try {
            List<IComponentFilter> filters = new ArrayList<IComponentFilter>();
            filters.add(new NameComponentFilter("tGenKeyHadoop"));
            filters.add(new NameComponentFilter("tMatchGroupHadoop"));
            Iterator<IComponentFilter> iter = filters.iterator();
            while (iter.hasNext()) {
                IComponentFilter filter = (IComponentFilter) iter.next();
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(changeNamenodeURIValue));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 9, 10, 0, 0, 0);
        return gc.getTime();
    }
}
