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
import org.talend.migration.IMigrationTask.ExecutionResult;

public class changeDBVersionForMapROJAIOutputMigrationTask extends AbstractJobMigrationTask {

	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2019, 8, 12, 12, 0, 0);
		return gc.getTime();
	}
    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = {"tMapROjaiOutput"};

    	IComponentConversion changeDBNVersion = new IComponentConversion() {

	        public void transform(NodeType node) {
	            if(node == null) {
	                return;
	            }

	        	ElementParameterType database = ComponentUtilities.getNodeProperty(node, "DB_VERSION"); //$NON-NLS-1$

	        	if (database == null) {
	        	    return;
	        	}

        	    String dbname = database.getValue();

        	    if("MAPROJAI_5_1".equals(dbname)) {
        	    	database.setValue("MAPR510");
        	    } else if ("MAPROJAI_5_2".equals(dbname)) {
        	    	database.setValue("MAPR520");
        	    }
	        }

    	};

    	for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(changeDBNVersion));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }
}
