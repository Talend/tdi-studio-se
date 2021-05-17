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

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * set "PARALLELIZE_KEEP_EMPTY" default value to "true" in db components on paralleize mode, see issue tdi bug 21708.
 *
 */
public class ChangeDefaultValueTDIBug21708 extends AbstractJobMigrationTask {

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

        try {
        	boolean modified=false;
            for (Object o : processType.getNode()) {
                NodeType node = (NodeType) o;
                if (ComponentUtilities.getNodeProperty(node, "PARALLELIZE") != null) {
                    if (ComponentUtilities.getNodeProperty(node, "PARALLELIZE_KEEP_EMPTY") == null) {
                        ComponentUtilities.addNodeProperty(node, "PARALLELIZE_KEEP_EMPTY", "CHECK");
                        ComponentUtilities.getNodeProperty(node, "PARALLELIZE_KEEP_EMPTY").setValue("true");
                        modified=true;
                    }
                }
            }
            if(modified){
            	 ProxyRepositoryFactory.getInstance().save(item, true);
            	 return ExecutionResult.SUCCESS_NO_ALERT;
            }else{
            	 return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 7, 12, 12, 0, 0);
        return gc.getTime();
    }
}
