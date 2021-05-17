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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * Migration task to add blocking into BLOCKING_DEFINITION.
 */
public class AddBlockingFortRecordMatching extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            IComponentFilter filter = new NameComponentFilter("tRecordMatching");
            IComponentConversion addBlockingTable = new AddBlockingDefinition();
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                    .<IComponentConversion> asList(addBlockingTable));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 7, 05, 17, 41, 10);
        return gc.getTime();
    }

    /**
     * DOC ytao AddBlockingFortRecordMatching class global comment.
     */
    private class AddBlockingDefinition implements IComponentConversion {

        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, "BLOCKING_DEFINITION") == null) {

                java.util.List<ElementValueType> blockings = new ArrayList<ElementValueType>();
                TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;

                ElementParameterType p = ComponentUtilities.getNodeProperty(node, "JOIN_KEY");

                if (p != null) {
                    EList<ElementValueType> es = p.getElementValue();

                    List<String> inputCols = new ArrayList<String>();
                    List<String> lookupCols = new ArrayList<String>();
                    List<String> matchTypes = new ArrayList<String>();

                    for (ElementValueType e : es) {

                        if (e.getElementRef().equals("INPUT_COLUMN")) {
                            inputCols.add(e.getValue());
                        } else if (e.getElementRef().equals("LOOKUP_COLUMN")) {
                            lookupCols.add(e.getValue());
                        } else if (e.getElementRef().equals("MATCHING_TYPE")) {
                            matchTypes.add(e.getValue());
                        }
                    }

                    Map<String, String> blockMap = new java.util.HashMap<String, String>();
                    for (String matchType : matchTypes) {

                        if ("Exact".equals(matchType)) {
                            blockMap.put(inputCols.get(matchTypes.indexOf(matchType)), lookupCols.get(matchTypes
                                    .indexOf(matchType)));
                        }
                    }

                    for (Map.Entry<String, String> entry : blockMap.entrySet()) {
                        ElementValueType elementValue = fileFact.createElementValueType();
                        elementValue.setElementRef("INPUT_COLUMN");
                        elementValue.setValue(entry.getKey());
                        blockings.add(elementValue);

                        ElementValueType elementValue2 = fileFact.createElementValueType();
                        elementValue2.setElementRef("LOOKUP_COLUMN");
                        elementValue2.setValue(entry.getValue());
                        blockings.add(elementValue2);
                    }
                }
                ComponentUtilities.addNodeProperty(node, "BLOCKING_DEFINITION", "TABLE");
                ComponentUtilities.setNodeProperty(node, "BLOCKING_DEFINITION", blockings);
            }
        }
    }

}
