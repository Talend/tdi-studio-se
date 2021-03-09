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
 * Add missing API Mode properties for Jobs before 6.1
 *
 */
public class AddApiModeToMarketoComponentsMigrationTask extends AbstractJobMigrationTask {

    public static final String SOAP = "USE_SOAP_API";

    public static final String REST = "USE_REST_API";

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 3, 17, 17, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] mktoComponents = new String[] { "tMarketoInput", "tMarketoOutput", "tMarketoListOperation" };
        try {
            for (String mktoComp : mktoComponents) {
                IComponentFilter filter = new NameComponentFilter(mktoComp);
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(new IComponentConversion() {

                            @Override
                            public void transform(NodeType node) {
                                ElementParameterType isApiMode = ComponentUtilities.getNodeProperty(node, SOAP);
                                // Job pre 6.1 : REST wasn't managed
                                if (isApiMode == null) {
                                    ComponentUtilities.addNodeProperty(node, SOAP, "RADIO");
                                    ComponentUtilities.getNodeProperty(node, SOAP).setValue("true");
                                    ComponentUtilities.addNodeProperty(node, REST, "RADIO");
                                    ComponentUtilities.getNodeProperty(node, REST).setValue("false");
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
}
