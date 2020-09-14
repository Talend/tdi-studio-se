//============================================================================
//
//Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
//This source code is available under agreement available at
//%InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//You should have received a copy of the agreement
//along with this program; if not, write to Talend SA
//9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.repository.model.migration;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TeradataTPTAddMainDatabaseToOptionalTablesMigrationTask extends AbstractJobMigrationTask {

    public static final String QUOTATION_MARK = "\"";

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 8, 14, 10, 0, 0);// check dates to start migration task
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String[] componentsNameToAffect = new String[]{
                "tTeradataTPTExec",
                "tTeradataTPTUtility"
        };

        IComponentConversion componentConversion = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                ElementParameterType enforceMainDatabase = ComponentUtilities.getNodeProperty(node, "ENFORCE_MAIN_DATABASE");
                if (enforceMainDatabase == null) {
                    String databaseName = ComponentUtilities.getNodePropertyValue(node, "DBNAME");
                    boolean specifyLogTable = "true".equals(ComponentUtilities.getNodePropertyValue(node, "SPECIFY_LOG_TABLE"));
                    if (specifyLogTable) {
                        String logTableName = ComponentUtilities.getNodePropertyValue(node, "LOG_TABLE_TABLE");
                        ComponentUtilities.setNodeValue(node, "LOG_TABLE_TABLE", concatenateDatabaseAndTableName(databaseName, logTableName));
                    }
                    boolean applyOptionalAttributes = "true".equals(ComponentUtilities.getNodePropertyValue(node, "APPLY_OPTIONAL_ATTRIBUTES"));// APPLY_OPTIONAL_ATTRIBUTES
                    if (applyOptionalAttributes) {
                        String consumerOperator = ComponentUtilities.getNodePropertyValue(node, "ACTION");
                        if ("Load".equals(consumerOperator)) {
                            appendDatabaseNameToOptionalTable(node, "TPT_CONSUMER_OPERATOR_OPTIONAL_ATTRIBUTES", databaseName, "ERRORTABLE1");
                            appendDatabaseNameToOptionalTable(node, "TPT_CONSUMER_OPERATOR_OPTIONAL_ATTRIBUTES", databaseName, "ERRORTABLE2");
                        }
                        if ("Stream".equals(consumerOperator)) {
                            appendDatabaseNameToOptionalTable(node, "TPT_COOA_FOR_STREAM", databaseName, "ERRORTABLE");
                        }
                    }
                }

            }

            private String concatenateDatabaseAndTableName(String databaseName, String tableName) {
                String extendedTableName = "";
                if (databaseName.startsWith(QUOTATION_MARK)) {
                    extendedTableName += databaseName.substring(0, databaseName.length() - 1);
                } else {
                    extendedTableName += databaseName + " + \"";
                }
                if (tableName.endsWith(QUOTATION_MARK)) {
                    extendedTableName += "." + tableName.substring(1);
                } else {
                    extendedTableName += ".\" + " + tableName;
                }
                return extendedTableName;
            }

            private ElementValueType extractItemFromProperty(NodeType node, String propertyName, String itemName) {
                ElementParameterType additionalList = ComponentUtilities.getNodeProperty(node, propertyName);
                List<ElementValueType> itemList = (List<ElementValueType>) additionalList.getElementValue();
                ElementValueType item = null;
                for (int i = 0; i < itemList.size() - 1; i++) {
                    ElementValueType elementValueType = itemList.get(i);
                    String elementName = elementValueType.getValue();
                    if (itemName.equals(elementName)) {
                        item = itemList.get(++i);
                    }
                }
                return item;
            }

            private void appendDatabaseNameToOptionalTable(NodeType node, String consumerOperatorAtributeTable, String databaseName, String tableName) {
                ElementValueType optionalTable = extractItemFromProperty(node, consumerOperatorAtributeTable, tableName);
                if (optionalTable != null) {
                    String optionalTableName = optionalTable.getValue();
                    optionalTable.setValue(concatenateDatabaseAndTableName(databaseName, optionalTableName));
                }
            }
        };

        for (String componentName : componentsNameToAffect) {
            IComponentFilter filter = new NameComponentFilter(componentName);
            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Collections.singletonList(componentConversion));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }
}
