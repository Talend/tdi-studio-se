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
public class AddHashKeyFromInputConnector4tHashOutput extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = { "tHashOutput" }; //$NON-NLS-1$

        IComponentConversion action = new IComponentConversion() {

            public void transform(NodeType node) {
                if (node == null) {
                    return;
                }

                ElementParameterType property = ComponentUtilities.getNodeProperty(node, "HASH_KEY_FROM_INPUT_CONNECTOR");//$NON-NLS-1$
                if (property == null) {
                    ComponentUtilities.addNodeProperty(node, "HASH_KEY_FROM_INPUT_CONNECTOR", "CHECK");//$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.setNodeValue(node, "HASH_KEY_FROM_INPUT_CONNECTOR", "true");//$NON-NLS-1$ //$NON-NLS-2$
                }
            }

        };

        for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(action));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 2, 3, 12, 0, 0);
        return gc.getTime();
    }
}
