// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractAllJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.MetadataTypeImpl;

/**
 * created by hcyi on Nov 12, 2020
 * Detailled comment
 *
 */
public class SpecialUpdateELTDefaultNameMigrationTask extends AbstractAllJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null || !Boolean
                .valueOf(System.getProperty("talend.import.specialUpdateELTDefaultNameParameter", Boolean.FALSE.toString()))) { //$NON-NLS-1$
            return ExecutionResult.NOTHING_TO_DO;
        }
        IComponentFilter filter = new NameComponentFilter("tELTOracleInput"); //$NON-NLS-1$
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            boolean update = false;
                            String connectionName = null;
                            String orginalTableValue = ComponentUtilities.getNodePropertyValue(node, "ELT_TABLE_NAME"); //$NON-NLS-1$
                            String orginalSchemaValue = ComponentUtilities.getNodePropertyValue(node, "ELT_SCHEMA_NAME"); //$NON-NLS-1$
                            String tableValue = TalendQuoteUtils.removeQuotes(orginalTableValue);
                            String schemaValue = TalendQuoteUtils.removeQuotes(orginalSchemaValue);
                            if (StringUtils.isBlank(schemaValue)) {
                                connectionName = tableValue;
                            } else {
                                connectionName = schemaValue + "." + tableValue; //$NON-NLS-1$
                            }

                            for (ConnectionType connection : (List<ConnectionType>) processType.getConnection()) {
                                String label = connection.getLabel();
                                // if user custom connection name, keep everything currently they had , will update the
                                // default table name value .
                                if (!connectionName.equals(label)) {
                                    String sourceNodeName = connection.getSource();
                                    MetadataTypeImpl table = (MetadataTypeImpl) node.getMetadata().get(0);
                                    if (table.getName().equals(sourceNodeName)) {
                                        connectionName = label;
                                        update = true;
                                        break;
                                    }
                                }
                            }
                            // get from connection label
                            if (update) {
                                if (StringUtils.isNotBlank(connectionName)) {
                                    String connectionNameTemp = connectionName;
                                    int connNameLength = connectionNameTemp.length();
                                    String schemaNewValue = null;
                                    String tableNewValue = null;
                                    // Connection name cases:context.a.context.b /context.a.b /a.context.b /a.b /b
                                    if (ContextParameterUtils.isContainContextParam(connectionNameTemp)) {
                                        if (connectionNameTemp.startsWith(ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX)) {
                                            int index = connectionNameTemp.indexOf(".", //$NON-NLS-1$
                                                    ContextParameterUtils.JAVA_NEW_CONTEXT_PREFIX.length());
                                            schemaNewValue = connectionNameTemp.substring(0, index);
                                            tableNewValue = connectionNameTemp.substring(index + 1, connNameLength);
                                        } else {
                                            int index = connectionNameTemp.indexOf(".");//$NON-NLS-1$
                                            schemaNewValue = connectionNameTemp.substring(0, index);
                                            tableNewValue = connectionNameTemp.substring(index + 1, connNameLength);
                                        }
                                    } else {
                                        String[] names = connectionNameTemp.split("\\.");//$NON-NLS-1$
                                        if (names.length == 2) {
                                            schemaNewValue = names[0];
                                            tableNewValue = names[1];
                                        }
                                    }
                                    // If global map in link
                                    schemaNewValue = TalendTextUtils.removeQuotesIfExist(schemaNewValue);
                                    if (schemaNewValue.startsWith("+") && schemaNewValue.endsWith("+")) { //$NON-NLS-1$ //$NON-NLS-2$
                                        schemaNewValue = schemaNewValue.substring(1, schemaNewValue.length() - 1);
                                    }
                                    tableNewValue = TalendTextUtils.removeQuotesIfExist(tableNewValue);
                                    if (tableNewValue.startsWith("+") && tableNewValue.endsWith("+")) { //$NON-NLS-1$ //$NON-NLS-2$
                                        tableNewValue = tableNewValue.substring(1, tableNewValue.length() - 1);
                                    }

                                    // Update the value
                                    if (!schemaNewValue.equals(schemaValue)) {
                                        ComponentUtilities.setNodeValue(node, "ELT_SCHEMA_NAME", schemaNewValue); //$NON-NLS-1$
                                    }
                                    if (!tableNewValue.equals(tableValue)) {
                                        ComponentUtilities.setNodeValue(node, "ELT_TABLE_NAME", tableNewValue); //$NON-NLS-1$
                                    }
                                }
                            }
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 11, 13, 12, 0, 0);
        return gc.getTime();
    }
}