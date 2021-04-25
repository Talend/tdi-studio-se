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
public class ChangeValue4BonitaComponents extends
		AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        String[] compNames = {"tBonitaDeploy" ,"tBonitaInstantiateProcess"};

    	IComponentConversion conversion = new IComponentConversion() {

	        public void transform(NodeType node) {
	            if(node == null) {
	                return;
	            }

	        	ElementParameterType parameter = ComponentUtilities.getNodeProperty(node, "DB_VERSION");

	        	if (parameter != null) {
	        		String value = parameter.getValue();
	        		if(value == null) {
	        		    return;
	        		}

	        		String oldValue523Prefix = "bonita-client-5.2.3.jar";
	        		String oldValue531Prefix = "bonita-client-5.3.jar";
	        		if(value.startsWith(oldValue523Prefix)) {
	        			String newValue = "antlr-2.7.6.jar;asm-1.5.3.jar;asm-3.1.jar;asm-attrs-1.5.3.jar;cglib-nodep-2.1_3.jar;commons-collections-2.1.1.jar;commons-logging-1.0.4.jar;dom4j-1.6.1.jar;ehcache-1.2.3.jar;groovy-all-1.7.0.jar;h2-1.2.132.jar;hibernate-3.2.6.ga.jar;jline-0.9.94.jar;jta-1.1.jar;novaBpmIdentity-1.0.jar;novaBpmPerf-1.0.jar;novaBpmUtil-1.0.jar";
	        			ComponentUtilities.setNodeValue(node, "DB_VERSION", newValue);
	        		} else if(value.startsWith(oldValue531Prefix)){
	        			String newValue = "novaBpmIdentity-1.0.jar;novaBpmPerf-1.0.jar;novaBpmUtil-1.0.jar;hibernate-core-3.5.6-Final.jar;commons-collections-3.1.jar;ehcache-core-2.2.0.jar;hibernate-commons-annotations-3.2.0.Final.jar;hibernate-search-3.2.1.Final.jar;javassist-3.8.0.GA.jar;lucene-core-2.9.3.jar;slf4j-api-1.6.1.jar;dom4j-1.6.1.jar;h2-1.2.132.jar;jta-1.1.jar;antlr-2.7.6.jar;commons-logging-1.1.1.jar";
	        			ComponentUtilities.setNodeValue(node, "DB_VERSION", newValue);
	        		}
	        	}
	        }

    	};

    	for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(conversion));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 5, 30, 10, 0, 0);
        return gc.getTime();
    }
}
