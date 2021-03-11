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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementValueTypeImpl;

/**
 *
 *
 */
public class ChangeValueBug14780 extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] componentsName = new String[] { "tSchemaComplianceCheck" };

        try {

            for (int i = 0; i < componentsName.length; i++) {
                IComponentFilter filter = new NameComponentFilter(componentsName[i]);
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(new IComponentConversion() {

                            public void transform(NodeType node) {
                                if (ComponentUtilities.getNodeProperty(node, "EMPTY_IS_NULL") != null) {
                                    if (ComponentUtilities.getNodeProperty(node, "EMPTY_IS_NULL").getValue().equals("true")) {
                                        ComponentUtilities.addNodeProperty(node, "ALL_EMPTY_ARE_NULL", "CHECK");
                                        ComponentUtilities.getNodeProperty(node, "ALL_EMPTY_ARE_NULL").setValue("false");
                                        ComponentUtilities.getNodeProperty(node, "EMPTY_IS_NULL").setValue("false");
                                    } else {
                                        for (Object obj : ComponentUtilities.getNodeProperty(node, "EMPTY_NULL_TABLE")
                                                .getElementValue()) {
                                            ElementValueTypeImpl evt = (ElementValueTypeImpl) obj;
                                            if ("EMPTY_NULL".equals(evt.getElementRef())) {
                                                evt.setValue("false");
                                            }
                                        }
                                        ComponentUtilities.addNodeProperty(node, "ALL_EMPTY_ARE_NULL", "CHECK");
                                        ComponentUtilities.getNodeProperty(node, "ALL_EMPTY_ARE_NULL").setValue("false");
                                    }
                                }
                            }

                        }));
            }

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
