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
 * set Share identity insert in multi table" default value to "true" in tMSSqlConnection advanced setting, see issue 9519.
 *
 */
public class ChangeShareIdentityDefaultValueMigrationTaskIssue9519 extends AbstractJobMigrationTask {

    public ChangeShareIdentityDefaultValueMigrationTaskIssue9519() {
        // TODO Auto-generated constructor stub
    }

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String[] componentsName = new String[] { "tMSSqlConnection" }; //$NON-NLS-1$

        for (String name : componentsName) {
            IComponentFilter filter = new NameComponentFilter(name); //$NON-NLS-1$

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(new IComponentConversion() {

							public void transform(NodeType node) {
								// TODO Auto-generated method stub
								 ElementParameterType propertyShareIdentitySetting = ComponentUtilities.getNodeProperty(node, "SHARE_IDENTITY_SETTING"); //$NON-NLS-1$
								  if (propertyShareIdentitySetting == null ){ //$NON-NLS-1$
									  	ComponentUtilities.addNodeProperty(node, "SHARE_IDENTITY_SETTING", "CHECK"); //$NON-NLS-1$ //$NON-NLS-2$
									  	 ComponentUtilities.setNodeValue(node, "SHARE_IDENTITY_SETTING", "true"); //$NON-NLS-1$ //$NON-NLS-2$
							        }
							}

                        }));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 11, 10, 10, 0, 0);
        return gc.getTime();
    }

}
