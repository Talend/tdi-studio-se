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

import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;


/**
 * DOC Administrator  class global comment. Detailled comment
 * <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (???????o?, 29 ?1???? 2006) nrousseau $
 *
 */
public class HandleOracleSchemaMigrationTask extends AbstractJobMigrationTask {

    @Override
	public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);

        if (getProject().getLanguage() == ECodeLanguage.JAVA && processType != null) {

            NodeType tOracleConnection = null;
            for (Object nodeType : processType.getNode()) {
                NodeType tmpNodeType = (NodeType) nodeType;
                if (tmpNodeType.getComponentName().equals("tOracleConnection")) { //$NON-NLS-1$
                    tOracleConnection = tmpNodeType;
                    break;
                }
            }
            if (tOracleConnection != null) {
                if (ComponentUtilities.getNodeProperty(tOracleConnection, "SCHEMA_DB") == null) { //$NON-NLS-1$
                    ComponentUtilities.addNodeProperty(tOracleConnection, "SCHEMA_DB", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.setNodeValue(tOracleConnection, "SCHEMA_DB", "\"\""); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    ElementParameterType elementParameter = ComponentUtilities.getNodeProperty(tOracleConnection, "SCHEMA_DB"); //$NON-NLS-1$
                    if (elementParameter.getValue().equals("ROOT")) { //$NON-NLS-1$
                        elementParameter.setValue("\"\""); //$NON-NLS-1$
                    }
                }
            }
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 4, 7, 12, 0, 0);
        return gc.getTime();
    }
}
