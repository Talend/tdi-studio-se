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
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractAllJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementValueTypeImpl;

/**
 * created by bchen on Jul 20, 2015 Detailled comment
 *
 */
public class AddPortForTCassandraConfigurationSpark extends AbstractAllJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 6, 27, 12, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = { "tCassandraConfiguration" }; //$NON-NLS-1$

        IComponentConversion conversion = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                if (node == null) {
                    return;
                }

                ElementParameterType parameter = ComponentUtilities.getNodeProperty(node, "PORT"); //$NON-NLS-1$
                List<Object> needRemovedList = new ArrayList<Object>();
                if (parameter == null) {
                    // get the value of native port if the user defined it on the configuration table
                    String portValue = null;
                    ElementParameterType configTable = ComponentUtilities.getNodeProperty(node, "CASSANDRA_CONFIGURATION"); //$NON-NLS-1$
                    boolean findNative = false;
                    boolean findRpc = false;
                    for (Object e : configTable.getElementValue()) {
                        ElementValueTypeImpl el = (ElementValueTypeImpl) e;
                        if (findNative && "VALUE".equals(el.getElementRef())) { //$NON-NLS-1$
                            portValue = el.getValue();
                            needRemovedList.add(e);
                            findNative = false;
                        }
                        if (findRpc && "VALUE".equals(el.getElementRef())) { //$NON-NLS-1$
                            needRemovedList.add(e);
                            findRpc = false;
                        }
                        if ("KEY".equals(el.getElementRef())) { //$NON-NLS-1$
                            if ("connection_native_port".equals(el.getValue())) { //$NON-NLS-1$
                                findNative = true;
                                needRemovedList.add(e);
                            }
                            if ("connection_rpc_port".equals(el.getValue())) { //$NON-NLS-1$
                                findRpc = true;
                                needRemovedList.add(e);
                            }
                        }
                    }
                    configTable.getElementValue().removeAll(needRemovedList);
                    ComponentUtilities.addNodeProperty(node, "PORT", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.setNodeValue(node, "PORT", portValue == null ? "\"9042\"" : portValue); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        };

        for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(conversion));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }
}
