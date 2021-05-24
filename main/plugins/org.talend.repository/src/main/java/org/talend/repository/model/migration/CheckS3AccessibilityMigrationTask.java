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

import org.talend.commons.exception.PersistenceException;
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
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class CheckS3AccessibilityMigrationTask extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        IComponentFilter filter = new NameComponentFilter("tS3Connection");
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                    .<IComponentConversion> asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            if (ComponentUtilities.getNodeProperty(node, "CHECK_ACCESSIBILITY") == null) {//$NON-NLS-1$
                            	ComponentUtilities.addNodeProperty(node, "CHECK_ACCESSIBILITY", "CHECK");//$NON-NLS-1$ //$NON-NLS-2$
                            	if(ComponentUtilities.getNodeProperty(node, "ASSUME_ROLE") == null){
                            		//indicate that the job from studio before 6.3.0
                                    ComponentUtilities.setNodeValue(node, "CHECK_ACCESSIBILITY", "false");//$NON-NLS-1$ //$NON-NLS-2$
                            	}else{
                            		ComponentUtilities.setNodeValue(node, "CHECK_ACCESSIBILITY", "true");//$NON-NLS-1$ //$NON-NLS-2$
                                    if (ComponentUtilities.getNodeProperty(node, "CHECK_METHOD") == null) {//$NON-NLS-1$
                                        ComponentUtilities.addNodeProperty(node, "CHECK_METHOD", "CLOSED_LIST");//$NON-NLS-1$ //$NON-NLS-2$
                                        ComponentUtilities.setNodeValue(node, "CHECK_METHOD", "BUCKET_CONFIGURATION");//$NON-NLS-1$ //$NON-NLS-2$
                                    }
                            	}
                            }
                        }
                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 11, 3, 16, 0, 0);
        return gc.getTime();
    }
}
