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
 * Migration task for switching on 'Quote all values' option in tWriteJSONField component.
 * New default value is to skip quotation marks for Numbers and Booleans.
 */
public class WriteJSONQuoteAllValuesMigrationTask extends AbstractJobMigrationTask {

    private static final String COMPONENT_NAME = "tWriteJSONField";
    private static final String QUOTE_ALL_VALUES_PARAMETER_NAME = "QUOTE_ALL_VALUES";
    private static final String QUOTE_ALL_VALUES_PARAMETER_TYPE = "CHECK";

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2018, 02, 26, 18, 0, 0).getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentFilter componentFilter = new NameComponentFilter(COMPONENT_NAME);

        try {
            ModifyComponentsAction.searchAndModify(item, processType, componentFilter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            ElementParameterType quoteAllValues = ComponentUtilities.getNodeProperty(node, QUOTE_ALL_VALUES_PARAMETER_NAME);

                            if (quoteAllValues == null) {
                                ComponentUtilities.addNodeProperty(node, QUOTE_ALL_VALUES_PARAMETER_NAME, QUOTE_ALL_VALUES_PARAMETER_TYPE);
                                ComponentUtilities.setNodeValue(node, QUOTE_ALL_VALUES_PARAMETER_NAME, "true");
                            }
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;

    }
}
