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
public class ChangeSpecifyKeysAndColumnsFortCassandraInput extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        IComponentFilter filter = new NameComponentFilter("tCassandraInput");
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            ElementParameterType specifyKeys = ComponentUtilities.getNodeProperty(node, "SPECIFY_KEYS");
                            ElementParameterType specifyColumns = ComponentUtilities.getNodeProperty(node, "SPECIFY_COLUMNS");
                            ElementParameterType columns = ComponentUtilities.getNodeProperty(node, "SUPER_COLUMN_KEYS");
                            ElementParameterType columnStart = ComponentUtilities.getNodeProperty(node, "COLUMN_START");
                            ElementParameterType columnEnd = ComponentUtilities.getNodeProperty(node, "COLUMN_END");
                            ElementParameterType columnLimit = ComponentUtilities.getNodeProperty(node, "COLUMN_LIMIT");
                            ElementParameterType columnFamilyType = ComponentUtilities
                                    .getNodeProperty(node, "COLUMN_FAMILY_TYPE");

                            ElementParameterType keyStart = ComponentUtilities.getNodeProperty(node, "KEY_START");
                            ElementParameterType keyEnd = ComponentUtilities.getNodeProperty(node, "KEY_END");
                            ElementParameterType count = ComponentUtilities.getNodeProperty(node, "COUNT");
                            if (specifyKeys == null && specifyColumns == null && columnStart == null && columnEnd == null
                                    && columns != null && columnLimit == null && keyStart != null && keyEnd != null
                                    && count != null) {
                                ComponentUtilities.addNodeProperty(node, "SPECIFY_KEYS", "CHECK");
                                ComponentUtilities.getNodeProperty(node, "SPECIFY_KEYS").setValue("true");

                                ComponentUtilities.addNodeProperty(node, "SPECIFY_COLUMNS", "CHECK");
                                if ("SUPER".equals(columnFamilyType.getValue()) && (!"".equals(columns.getValue()))
                                        && (!"\"\"".equals(columns.getValue()))) {
                                    ComponentUtilities.getNodeProperty(node, "SPECIFY_COLUMNS").setValue("true");
                                }

                                ComponentUtilities.addNodeProperty(node, "COLUMNS", "TEXT");
                                ComponentUtilities.removeNodeProperty(node, "SUPER_COLUMN_KEYS");
                                ComponentUtilities.getNodeProperty(node, "COLUMNS").setValue(columns.getValue());

                                ComponentUtilities.addNodeProperty(node, "COLUMN_START", "TEXT");
                                ComponentUtilities.getNodeProperty(node, "COLUMN_START").setValue(keyStart.getValue());
                                ComponentUtilities.getNodeProperty(node, "KEY_START").setValue("\"\"");

                                ComponentUtilities.addNodeProperty(node, "COLUMN_END", "TEXT");
                                ComponentUtilities.getNodeProperty(node, "COLUMN_END").setValue(keyEnd.getValue());
                                ComponentUtilities.getNodeProperty(node, "KEY_END").setValue("\"\"");

                                ComponentUtilities.addNodeProperty(node, "COLUMN_LIMIT", "TEXT");
                                ComponentUtilities.getNodeProperty(node, "COLUMN_LIMIT").setValue(count.getValue());
                            }
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 9, 7, 12, 0, 0);
        return gc.getTime();
    }
}
