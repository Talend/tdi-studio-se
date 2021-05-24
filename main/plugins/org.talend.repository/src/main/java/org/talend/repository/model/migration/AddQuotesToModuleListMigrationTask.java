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
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * ftang class global comment. Detailled comment <br/>
 *
 *
 */
public class AddQuotesToModuleListMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @Override
	public ExecutionResult execute(Item item) {
        try {
            addQuote(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     * Add quote marks to the separators.
     *
     * yzhang Comment method "addQuote".
     *
     * @throws PersistenceException
     */
    private void addQuote(Item item) throws PersistenceException {
		ProcessType processType = getProcessType(item);
		if (processType == null) {
			return;
		}
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        boolean modified = false;

        EList node = processType.getNode();
        for (Object object2 : node) {
            NodeType type = (NodeType) object2;
            EList elementParameterList = type.getElementParameter();
            for (Object object3 : elementParameterList) {
                ElementParameterType elemType = (ElementParameterType) object3;
                if (elemType.getField().equals("MODULE_LIST")) { //$NON-NLS-1$
                    String value = elemType.getValue();
                    if (!isWithinQuote(value)) {
                        elemType.setValue(TalendTextUtils.addQuotes(value));
                        modified = true;
                        break;
                    }

                }
            }

        }

        // for (Object object : elementParameters) {
        // ElementParameterType elem = (ElementParameterType) object;
        // if (elem.getName().equals("LIBRARY")) {
        // String value = (String) elem.getValue();
        // if (isWithinQuote(value)) {
        // value = TalendTextUtils.addQuotes(value);
        // elem.setValue(value);
        // break;
        // }
        // }
        //
        // }
        // modified = true;

        if (modified) {
            factory.save(item);
        }
    }

    /**
     * To see whehter the string is surrounded with double quote mark.
     *
     *
     * @param string
     * @return
     */
    private boolean isWithinQuote(String string) {
        if (string == null) {
            return true;
        }
        boolean isWithin = false;
        if (string.startsWith("\"") && string.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            isWithin = true;
        } else if (string.startsWith("\'") && string.endsWith("\'")) { //$NON-NLS-1$ //$NON-NLS-2$
            isWithin = true;
        }
        return isWithin;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 4, 12, 12, 0, 0);
        return gc.getTime();
    }

}
