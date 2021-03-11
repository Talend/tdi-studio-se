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
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;


public class DisableUseBatchWhenRejectLineExists extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        return new GregorianCalendar(2015, 9, 28, 12, 0, 0).getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        List<String> filterList = Arrays.asList("tFirebirdOutput", "tGreenplumOutput", "tIngresOutput", "tInterbaseOutput", "tParAccelOutput", "tPostgresPlusOutput", "tSQLiteOutput", "tRedshiftOutput");
        List<String> filterListMore = Arrays.asList("tTeradataOutput", "tOracleOutput");

        IComponentConversion disableUseBatchWhenRejectLineExists = new IComponentConversion() {
            public void transform(NodeType node) {
                ElementParameterType useBatch = ComponentUtilities.getNodeProperty(node, "USE_BATCH");
                List<ConnectionType> list = ComponentUtilities.getNodeOutputConnections(node);
                for(ConnectionType connType : list){
                    EConnectionType eConnType = EConnectionType.getTypeFromId(
                                                  connType.getLineStyle()
                                                 );
                    if(eConnType == EConnectionType.FLOW_MAIN && connType.getConnectorName().equals("REJECT")){
                        if(useBatch ==  null){
                             ComponentUtilities.addNodeProperty(node, "USE_BATCH", "CHECK");
                        }
                             ComponentUtilities.getNodeProperty(node, "USE_BATCH").setValue("false");
                        break;
                    }
                }
            }
        };

        IComponentConversion disableUseBatchWhenRejectLineExistsMore = new IComponentConversion() {
            public void transform(NodeType node) {
                ElementParameterType useBatch = ComponentUtilities.getNodeProperty(node, "USE_BATCH_SIZE");
                ElementParameterType useBatchAndUseExistingConn = ComponentUtilities.getNodeProperty(node, "USE_BATCH_AND_USE_CONN");
                List<ConnectionType> list = ComponentUtilities.getNodeOutputConnections(node);
                for(ConnectionType connType : list){
                    EConnectionType eConnType = EConnectionType.getTypeFromId(
                                                  connType.getLineStyle()
                                                 );
                    if(eConnType == EConnectionType.FLOW_MAIN && connType.getConnectorName().equals("REJECT")){
                        if(useBatch ==  null){
                             ComponentUtilities.addNodeProperty(node, "USE_BATCH_SIZE", "CHECK");
                        }
                        if(useBatchAndUseExistingConn ==  null){
                             ComponentUtilities.addNodeProperty(node, "USE_BATCH_AND_USE_CONN", "CHECK");
                        }
                             ComponentUtilities.getNodeProperty(node, "USE_BATCH_SIZE").setValue("false");
                             ComponentUtilities.getNodeProperty(node, "USE_BATCH_AND_USE_CONN").setValue("false");
                        break;
                    }
                }
            }
        };

        for (String componentName : filterList) {
            IComponentFilter filter = new NameComponentFilter(componentName);
            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(disableUseBatchWhenRejectLineExists));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        for (String componentName : filterListMore) {
            IComponentFilter filter = new NameComponentFilter(componentName);
            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(disableUseBatchWhenRejectLineExistsMore));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }
}
