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
import java.util.regex.Pattern;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class ReplaceVarcharArrayWithVarcharIssueTDI35719 extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 5, 19, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        final List<String> componentNames = Arrays.asList("tTeradataTPTUtility", "tTeradataTPTExec");
        final String tableToCheck = "TPT_COOA_FOR_UPDATE";
        final List<String> fieldNames = Arrays.asList("ERRORTABLE1", "ERRORTABLE2", "WORKTABLE");

        IComponentFilter filter = new IComponentFilter() {

            @Override
            public boolean accept(NodeType node) {
                return componentNames.contains(node.getComponentName());
            }
        };

        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        @Override
                        public void transform(NodeType node) {
                            String openBrStr = "['";
                            String closeBrStr = "']";
                            String openBrPattern = Pattern.quote(openBrStr);
                            String closeBrPattern = Pattern.quote(closeBrStr);
                            ProcessType item = (ProcessType) node.eContainer();
                            for (Object o : item.getNode()) {
                                NodeType nt = (NodeType) o;
                                for (Object o1 : nt.getElementParameter()) {
                                    ElementParameterType t = (ElementParameterType) o1;
                                    if ("TABLE".equals(t.getField()) && tableToCheck.equals(t.getName())) {
                                        List<ElementValueType> elementValues = (List<ElementValueType>) t.getElementValue();
                                        for (int i = 0; i < elementValues.size() - 1; i++) {
                                            ElementValueType nameCol = elementValues.get(i);
                                            String nameValue = nameCol.getValue();
                                            if (nameCol.getElementRef().equals("OPTIONAL_ATTRIBUTES_NAME") && nameValue != null
                                                    && fieldNames.contains(nameValue)) {
                                                ElementValueType valueCol = elementValues.get(++i);
                                                String value = valueCol != null ? valueCol.getValue().trim() : null;
                                                if (value != null && value.contains(openBrStr) || value.contains(closeBrStr)) {
                                                    String newValue = value.replaceFirst(openBrPattern, "")
                                                            .replaceFirst(closeBrPattern, "");
                                                    if (!value.equals(newValue)) {
                                                        valueCol.setValue(newValue);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

}
