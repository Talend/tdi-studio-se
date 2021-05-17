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
 * Change activemq-all-5.1.0.jar to activemq-all-5.7.0.jar in jobs where
 * tMOM components are used.
 */
public class ChangeActiveMqJarName4MOMComponents extends AbstractJobMigrationTask {
	private static final String PROPERTY_TO_REMOVE = "MQ_DERVIERS";

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        String[] tmomCompNames = {"tMomInput","tMomInputLoop","tMomOutput"}; //$NON-NLS-1$

    	IComponentConversion changeActiveMqDriverJarType = new IComponentConversion() {
	        public void transform(NodeType node) {
	        	ElementParameterType mq_drivers = ComponentUtilities.getNodeProperty(node, PROPERTY_TO_REMOVE); //$NON-NLS-2$
	        	if (mq_drivers != null) {
	        		ComponentUtilities.removeNodeProperty(node, PROPERTY_TO_REMOVE);
	        	}
	        }
    	};

    	for (String name : tmomCompNames) {
            IComponentFilter filter = new NameComponentFilter(name); //$NON-NLS-4$
            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                		Arrays.<IComponentConversion> asList(changeActiveMqDriverJarType));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 10, 15, 10, 0, 0);
        return gc.getTime();
    }
}
