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
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.conversions.ResetComponentNameConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Migration task to rename component from tSalesforceWaveXXX to tSalesforceEinsteinXXX.
 *
 */

public class RenameSQLDWHMigrationTask extends AbstractJobMigrationTask {

    final String[] source = { "tSQLDWHBulkExec", "tSQLDWHClose", "tSQLDWHCommit", "tSQLDWHConnection", "tSQLDWHInput", "tSQLDWHOutput", "tSQLDWHRollback", "tSQLDWHRow" };

    final String[] target = { "tAzureSynapseBulkExec", "tAzureSynapseClose", "tAzureSynapseCommit", "tAzureSynapseConnection", "tAzureSynapseInput", "tAzureSynapseOutput", "tAzureSynapseRollback", "tAzureSynapseRow" };

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            for (int i = 0; i < source.length; i++) {
                final int j = i;
                IComponentFilter filter = new NameComponentFilter(source[i]);
                ResetComponentNameConversion renameConversion = 
                		new ResetComponentNameConversion(target[i], source[j]);
                IComponentConversion changeNodeNameConversion = new IComponentConversion() {

                    public void transform(NodeType node) {

                        ProcessType item = (ProcessType) node.eContainer();
                        for (Object o : item.getConnection()) {
                            ConnectionType connection = (ConnectionType) o;
                            if ("RUN_IF".equals(connection.getConnectorName())) {
                                for (Object obj : connection.getElementParameter()) {
                                    ElementParameterType type = (ElementParameterType) obj;
                                    if ("CONDITION".equals(type.getName())) {
                                        if (type.getValue() != null && type.getValue().contains(source[j])) {
                                            String replaceAll = type.getValue().replaceAll(source[j], target[j]);
                                            type.setValue(replaceAll);
                                        }
                                        break;
                                    }
                                }
                            }
                            if ("TABLE".equals(connection.getConnectorName())) {
                                for (Object obj : connection.getElementParameter()) {
                                    ElementParameterType type = (ElementParameterType) obj;
                                    if ("UNIQUE_NAME".equals(type.getName())) {
                                        if (type.getValue() != null && type.getValue().contains(source[j])) {
                                            String replaceAll = type.getValue().replaceAll(source[j], target[j]);
                                            type.setValue(replaceAll);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        for (Object o : item.getNode()) {
                            NodeType nt = (NodeType) o;
                            for (Object o1 : nt.getElementParameter()) {
                                ElementParameterType t = (ElementParameterType) o1;
                                String value = t.getValue();
                                if (!"UNIQUE_NAME".equals(t.getName()) && value != null) {
                                    if (value.contains(source[j])) {
                                        String replaceAll = value.replaceAll(source[j], target[j]);
                                        t.setValue(replaceAll);
                                    }
                                }
                                if ("TABLE".equals(t.getField())) {
                                    if (t.getName() != null && t.getName().contains(source[j])) {
                                        String replaceAll = t.getName().replaceAll(source[j], target[j]);
                                        t.setName(replaceAll);
                                    }
                                    for (ElementValueType type : (List<ElementValueType>) t.getElementValue()) {
                                        if (type.getValue() != null && type.getValue().contains(source[j])) {
                                            String replaceAll = type.getValue().replaceAll(source[j], target[j]);
                                            type.setValue(replaceAll);
                                        }
                                    }
                                }
                            }
                        }

                    }
                };
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(renameConversion, changeNodeNameConversion));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 1, 27, 12, 0, 0);
        return gc.getTime();
    }
}
