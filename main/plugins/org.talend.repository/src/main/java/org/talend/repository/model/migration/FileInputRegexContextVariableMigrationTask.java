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

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * This migration task did nothing but for importing job(s) from TOS 3.0.0. <br/>
 *
 *
 */
public class FileInputRegexContextVariableMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        // if (getProject().getLanguage() != ECodeLanguage.JAVA) {
        // return ExecutionResult.NOTHING_TO_DO;
        // }
        // try {
        // / addQuote(item);
        // return ExecutionResult.SUCCESS_NO_ALERT;
        // } catch (Exception e) {
        // return ExecutionResult.FAILURE;
        //
        // }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private boolean addQuote(Item item) {
		ProcessType processType = getProcessType(item);
		if (processType == null) {
			return false;
		}
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean modified = false;
        EList nodes = processType.getNode();
        for (Object n : nodes) {
            NodeType type = (NodeType) n;
            if (type.getComponentName().equals("tFileInputRegex")) { //$NON-NLS-1$
                EList elementParameterList = type.getElementParameter();
                for (Object elem : elementParameterList) {
                    ElementParameterType elemType = (ElementParameterType) elem;
                    if (elemType.getName().equals("HEADER") || elemType.getName().equals("FOOTER") //$NON-NLS-1$ //$NON-NLS-2$
                            || elemType.getName().equals("LIMIT")) { //$NON-NLS-1$
                        String oldV = elemType.getValue();
                        if (oldV == null || oldV.length() == 0) {
                            if (elemType.getName().equals("LIMIT")) { //$NON-NLS-1$
                                oldV = "-1"; //$NON-NLS-1$
                            } else {
                                oldV = "0"; //$NON-NLS-1$
                            }
                        }
                        if (!oldV.contains("\"")) { //$NON-NLS-1$
                            elemType.setValue("\"" + oldV + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 8, 26, 12, 0, 0);
        return gc.getTime();
    }

}
