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
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC xtan. tFileInputRegx when bug:4765, it add quote in jet, and now when bug:5432, it should remove the quote.<br/>
 */
public class FileInputRegexContextVariableMigrationTaskForSave extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (getProject().getLanguage() != ECodeLanguage.JAVA
				|| processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            removeQuote(item, processType);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            return ExecutionResult.FAILURE;

        }
    }

    private boolean removeQuote(Item item, ProcessType processType)
			throws PersistenceException {

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
                        if (oldV != null && oldV.startsWith("\"") && oldV.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                            elemType.setValue(oldV.substring(1, oldV.length() - 1));
                            modified = true;
                        }

                    }
                }
            }
        }

        if (modified) {
            factory.save(item, true);
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
