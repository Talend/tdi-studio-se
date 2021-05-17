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
public class ChangeFileInputJSONUrlUnCheckWhenXpathCheck extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        String[] tFileInputJSON = { "tFileInputJSON" }; //$NON-NLS-1$

        IComponentConversion changeUrlUnCheck = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                ElementParameterType readByXpath = ComponentUtilities.getNodeProperty(node, "READBYXPATH"); //$NON-NLS-1$
                ElementParameterType useUrl = ComponentUtilities.getNodeProperty(node, "USEURL"); //$NON-NLS-1$
                if (readByXpath != null && "true".equals(readByXpath.getValue())) { //$NON-NLS-1$
                    if (useUrl != null && "true".equals(useUrl.getValue())) { //$NON-NLS-1$
                        ComponentUtilities.setNodeValue(node, "USEURL", "false"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }

        };

        for (String name : tFileInputJSON) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(changeUrlUnCheck));
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
        GregorianCalendar gc = new GregorianCalendar(2013, 05, 9, 10, 0, 0);
        return gc.getTime();
    }
}
