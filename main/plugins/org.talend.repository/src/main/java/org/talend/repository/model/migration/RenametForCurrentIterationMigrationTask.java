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
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * migration task rename tFor_ variable to tLoop variable.
 *
 */

public class RenametForCurrentIterationMigrationTask extends AbstractJobMigrationTask {

    private static final String TFOR = "tFor_"; //$NON-NLS-1$

    private static final String TLOOP = "tLoop_"; //$NON-NLS-1$

    private static final String CURRENT_ITER = "CURRENT_ITERATION"; //$NON-NLS-1$

    private static final String CURRENT_VALUE = "CURRENT_VALUE"; //$NON-NLS-1$

    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
		if (processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}
        try {
            convertItem(item, processType);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private boolean convertItem(Item item, ProcessType processType) throws PersistenceException {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean modified = false;
        ECodeLanguage language = getProject().getLanguage();
        EList node = processType.getNode();
        for (Object n : node) {
            NodeType type = (NodeType) n;

            EList elementParameterList = type.getElementParameter();
            for (Object elem : elementParameterList) {
                ElementParameterType elemType = (ElementParameterType) elem;
                if (!elemType.getField().equals("CHECK")) { //$NON-NLS-1$
                    if (language.equals(ECodeLanguage.JAVA)) {
                        if (elemType.getValue() != null && elemType.getValue().contains(TFOR)
                                && elemType.getValue().contains(CURRENT_ITER)) {
                            elemType.setValue(elemType.getValue().replaceAll(CURRENT_ITER, CURRENT_VALUE));
                        }
                    }

                    if (elemType.getValue() != null && elemType.getValue().contains(TFOR)) {

                        elemType.setValue(elemType.getValue().replaceAll(TFOR, TLOOP));
                        modified = true;

                    }

                    // for table

                    EList elemValue = elemType.getElementValue();
                    for (Object elemV : elemValue) {
                        ElementValueType elemVal = (ElementValueType) elemV;
                        if (language.equals(ECodeLanguage.JAVA)) {
                            if (elemVal.getValue() != null && elemVal.getValue().contains(TFOR)
                                    && elemVal.getValue().contains(CURRENT_ITER)) {
                                elemVal.setValue(elemVal.getValue().replaceAll(CURRENT_ITER, CURRENT_VALUE));
                            }
                        }
                        if (elemVal.getValue() != null && elemVal.getValue().contains(TFOR)) {
                            elemVal.setValue(elemVal.getValue().replaceAll(TFOR, TLOOP));
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

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 5, 30, 12, 0, 0);
        return gc.getTime();
    }
}
