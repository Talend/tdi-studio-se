// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
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
public class ChangeTEZ_JARS_NAME4HiveComponentsTDI44481 extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = { "tHiveConnection", "tHiveRow", "tHiveInput", "tHiveLoad", "tELTHiveMap"};

        IComponentConversion conversion = new IComponentConversion() {

            public void transform(NodeType node) {
                if (node == null) {
                    return;
                }

                ElementParameterType parameter = ComponentUtilities.getNodeProperty(node, "TEZ_JARS_NAME");

                if (parameter != null) {
                    String value = parameter.getValue();
                    if (value == null) {
                        return;
                    }

                    String oldVersion = "commons-beanutils-1.7.0.jar,commons-beanutils-core-1.8.0.jar";
                    String newVersion = "commons-beanutils-1.9.4.jar";
                    if (value.contains(oldVersion)) {
                        String newDBVersion = value.replace(oldVersion, newVersion);
                        ComponentUtilities.setNodeValue(node, "TEZ_JARS_NAME", newDBVersion);
                    }
                }
            }

        };

        for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(conversion));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 6, 9, 12, 0, 0);
        return gc.getTime();
    }
}
