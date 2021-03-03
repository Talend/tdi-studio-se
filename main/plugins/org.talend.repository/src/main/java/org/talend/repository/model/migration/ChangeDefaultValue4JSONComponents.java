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
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeDefaultValue4JSONComponents extends AbstractAllJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = {
                "tFileInputJSON", "tExtractJSONFields", "tExtractAbstractJSONFields", "tFileInputJSON", "tInputFormatJSON" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        IComponentConversion conversion = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                if (node == null) {
                    return;
                }

                ElementParameterType parameter = ComponentUtilities.getNodeProperty(node, "READ_BY"); //$NON-NLS-1$

                if (parameter == null) {
                    ComponentUtilities.addNodeProperty(node, "READ_BY", "CLOSED_LIST"); //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.setNodeValue(node, "READ_BY", "XPATH"); //$NON-NLS-1$ //$NON-NLS-2$
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

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 5, 24, 11, 0, 0);
        return gc.getTime();
    }
}
