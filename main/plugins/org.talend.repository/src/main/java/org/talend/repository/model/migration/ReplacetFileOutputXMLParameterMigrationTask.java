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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
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

/**
 * Migration task use to change the tFileOutputXML parameter value for the context,add quote to value.
 */
public class ReplacetFileOutputXMLParameterMigrationTask extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            IComponentFilter filter = new NameComponentFilter("tFileOutputXML");
            IComponentConversion changeNodeValueConversion = new IComponentConversion() {

                public void transform(NodeType node) {
                    ProcessType item = (ProcessType) node.eContainer();
                    for (Object o : item.getNode()) {
                        NodeType nt = (NodeType) o;
                        for (Object o1 : nt.getElementParameter()) {
                            ElementParameterType t = (ElementParameterType) o1;
                            if ("TABLE".equals(t.getField()) && ("MAPPING".equals(t.getName()) || "GROUP_BY".equals(t.getName()))) {
                                for (ElementValueType type : (List<ElementValueType>) t.getElementValue()) {
                                    if ("LABEL".equals(type.getElementRef())) {
                                        String value = type.getValue();
                                        if (value != null && (!value.trim().startsWith("\""))) {
                                            type.setValue("\"" + value.trim() + "\"");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(changeNodeValueConversion));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 3, 18, 12, 0, 0);
        return gc.getTime();
    }
}
