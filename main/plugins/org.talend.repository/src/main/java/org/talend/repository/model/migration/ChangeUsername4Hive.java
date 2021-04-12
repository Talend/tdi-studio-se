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
public class ChangeUsername4Hive extends
		AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = {"tHiveConnection","tHiveRow","tHiveCreateTable",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "tHiveLoad","tHiveInput","tELTHiveMap"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	IComponentConversion changeBagName4tPigCode = new IComponentConversion() {

	        public void transform(NodeType node) {
	            if(node == null) {
	                return;
	            }

	        	ElementParameterType usename = ComponentUtilities.getNodeProperty(node, "USER"); //$NON-NLS-1$
	        	ElementParameterType connectionMode = ComponentUtilities.getNodeProperty(node, "CONNECTION_MODE"); //$NON-NLS-1$

	        	if (usename == null || connectionMode == null) {
	        	    return;
	        	}

	        	String name = usename.getValue();
                String mode = connectionMode.getValue();

                if(name == null || mode == null || "STANDALONE".equals(mode)) {//$NON-NLS-1$
                    return;
                }

                usename.setValue("\"\"");//$NON-NLS-1$
	        }

    	};

    	for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(changeBagName4tPigCode));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 9, 22, 10, 0, 0);
        return gc.getTime();
    }
}
