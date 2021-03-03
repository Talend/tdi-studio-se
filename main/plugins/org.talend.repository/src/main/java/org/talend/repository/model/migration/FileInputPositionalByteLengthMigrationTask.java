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

public class FileInputPositionalByteLengthMigrationTask extends AbstractJobMigrationTask {

    private static final String COMPONENT_NAME = "tFileInputPositional";

    private static final String USE_BYTE_LENGTH_PARAMETER_NAME = "USE_BYTE";
    private static final String USE_BYTE_LENGTH_PARAMETER_TYPE = "CHECK";

    private static final String PATTERN_UNITS_PARAMETER_NAME = "PATTERN_UNITS";
    private static final String PATTERN_UNITS_PARAMETER_TYPE = "CLOSED_LIST";
    private static final String PATTERN_UNITS_BYTES_OPTION = "BYTES";
    private static final String PATTERN_UNITS_SYMBOLS_OPTION = "SYMBOLS";

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2018, 03, 19, 18, 0, 0).getTime();
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
                            ElementParameterType patternUnits = ComponentUtilities.getNodeProperty(node, PATTERN_UNITS_PARAMETER_NAME);

                            if (patternUnits == null) {
                                ComponentUtilities.addNodeProperty(node, PATTERN_UNITS_PARAMETER_NAME, PATTERN_UNITS_PARAMETER_TYPE);

                                ElementParameterType useByte = ComponentUtilities.getNodeProperty(node, USE_BYTE_LENGTH_PARAMETER_NAME);
                                String option = useByte != null && useByte.getValue() != null && "true".equals(useByte.getValue())
                                        ? PATTERN_UNITS_BYTES_OPTION : PATTERN_UNITS_SYMBOLS_OPTION;
                                ComponentUtilities.setNodeValue(node, PATTERN_UNITS_PARAMETER_NAME, option);
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
