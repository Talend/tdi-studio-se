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
import org.talend.core.model.migration.AbstractAllJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by bchen on Jul 20, 2015 Detailled comment
 *
 */
public class UnifyParameterOfDatastaxAndHectorForCassandra extends AbstractAllJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 6, 28, 20, 0, 0);
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
        String[] compNames = { "tCassandraInput", "tCassandraOutput", "tCassandraRow", "tCassandraLookupInput" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        IComponentConversion conversion = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                if (node == null) {
                    return;
                }

                ElementParameterType cf = ComponentUtilities.getNodeProperty(node, "TABLE"); //$NON-NLS-1$
                if (cf != null) {
                    if (ComponentUtilities.getNodeProperty(node, "COLUMN_FAMILY") == null) { //$NON-NLS-1$
                        ComponentUtilities.addNodeProperty(node, "COLUMN_FAMILY", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    ComponentUtilities.getNodeProperty(node, "COLUMN_FAMILY").setValue(cf.getValue()); //$NON-NLS-1$
                }

                ElementParameterType actionForCF = ComponentUtilities.getNodeProperty(node, "TABLE_ACTION"); //$NON-NLS-1$
                if (actionForCF != null) {
                    if (ComponentUtilities.getNodeProperty(node, "ACTION_ON_COLUMN_FAMILY") == null) { //$NON-NLS-1$
                        ComponentUtilities.addNodeProperty(node, "ACTION_ON_COLUMN_FAMILY", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    ComponentUtilities.getNodeProperty(node, "ACTION_ON_COLUMN_FAMILY").setValue(actionForCF.getValue()); //$NON-NLS-1$
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
