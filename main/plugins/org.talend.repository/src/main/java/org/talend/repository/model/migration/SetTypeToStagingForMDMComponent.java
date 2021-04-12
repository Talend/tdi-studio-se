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
public class SetTypeToStagingForMDMComponent extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        try {
            addType(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 7, 16, 12, 0, 0);
        return gc.getTime();
    }

    private void addType(Item item) throws Exception {
        ProcessType processType = getProcessType(item);
        java.util.List<IComponentFilter> filters = new java.util.ArrayList<IComponentFilter>();
        filters.add(new NameComponentFilter("tMDMBulkLoad"));
        filters.add(new NameComponentFilter("tMDMInput"));
        filters.add(new NameComponentFilter("tMDMOutput"));
        filters.add(new NameComponentFilter("tMDMDelete"));
        filters.add(new NameComponentFilter("tMDMRouteRecord"));
        filters.add(new NameComponentFilter("tMDMSP"));
        filters.add(new NameComponentFilter("tMDMViewSearch"));
        filters.add(new NameComponentFilter("tMDMReadConf"));
        filters.add(new NameComponentFilter("tMDMWriteConf"));
        IComponentConversion addOption = new AddType();
        java.util.Iterator<IComponentFilter> iter = filters.iterator();
        while (iter.hasNext()) {
            IComponentFilter filter = iter.next();
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
        }
    }

    /**
     *
     * rdubois AddConnectionMode class global comment. Detailled comment
     */
    private class AddType implements IComponentConversion {

        private String field = "CLOSED_LIST"; //$NON-NLS-1$

        private String name = "CONTAINER_TYPE"; //$NON-NLS-1$

        public AddType() {
            super();
        }

        @Override
        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, name) == null) {
                ComponentUtilities.addNodeProperty(node, name, field);
                ComponentUtilities.setNodeValue(node, name, "MASTER");
            }

            for (Object o : node.getElementParameter()) {
                ElementParameterType para = (ElementParameterType) o;
                if ("DATACLUSTER".equals(para.getName())) { //$NON-NLS-1$
                    String value = para.getValue();
                    if (value != null && value.endsWith("#STAGING\"")) {
                        para.setValue(value.substring(0, value.lastIndexOf("#STAGING")) + "\"");
                        ComponentUtilities.setNodeValue(node, name, "STAGING");
                    }
                }
            }

        }
    }
}
