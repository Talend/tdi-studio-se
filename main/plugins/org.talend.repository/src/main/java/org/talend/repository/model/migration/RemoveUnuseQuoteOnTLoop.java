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

// import org.talend.commons.ui.runtime.exception.ExceptionHandler;
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
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class RemoveUnuseQuoteOnTLoop extends AbstractJobMigrationTask {

    public RemoveUnuseQuoteOnTLoop() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String[] componentsName = new String[] { "tLoop" }; //$NON-NLS-1$
        IComponentConversion removeQuote = new IComponentConversion() {

            private void removeQuote(ElementParameterType element) {
                if (element != null && element.getValue() != null) {
                    String dValue = element.getValue();
                    if (dValue.startsWith("\"") && dValue.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                        dValue = dValue.substring(1, dValue.length() - 1);
                        element.setValue(dValue);
                    }
                }
            }

            @Override
            public void transform(NodeType node) {
                List<String> parameters = Arrays.asList(new String[] { "DECLARATION", "CONDITION", "ITERATION" }); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                for (String parameter : parameters) {
                    removeQuote(ComponentUtilities.getNodeProperty(node, parameter));
                }
            }
        };

        for (String name : componentsName) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(removeQuote));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 9, 24, 12, 0, 0);
        return gc.getTime();
    }

}
