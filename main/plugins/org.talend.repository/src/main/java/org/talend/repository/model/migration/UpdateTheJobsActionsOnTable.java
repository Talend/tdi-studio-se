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
import java.util.List;

import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC zwang class global comment. Detailled comment
 */
public class UpdateTheJobsActionsOnTable extends AbstractJobMigrationTask {

    private static final String T_ORACLE_OUTPUT = "tOracleOutput"; //$NON-NLS-1$

    private static final String CLEAR_TABLE = "CLEAR_TABLE"; //$NON-NLS-1$

    public static boolean isClear;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
		ProcessType process = getProcessType(item);
		if (process == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}

        List<NodeType> nodeList = process.getNode();
        for (NodeType node : nodeList) {
            if (node.getComponentName().indexOf(UpdateTheJobsActionsOnTable.T_ORACLE_OUTPUT) != -1) {
                for (ElementParameterType elementParameterType : (List<ElementParameterType>) node.getElementParameter()) {
                    if (UpdateTheJobsActionsOnTable.CLEAR_TABLE.equals(elementParameterType.getName())) {
                        if ("true".equals(elementParameterType.getValue())) { //$NON-NLS-1$
                            UpdateTheJobsActionsOnTable.isClear = true;
                            return ExecutionResult.SUCCESS_NO_ALERT;
                        }
                    }
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        // TODO Auto-generated method stub
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }

}
