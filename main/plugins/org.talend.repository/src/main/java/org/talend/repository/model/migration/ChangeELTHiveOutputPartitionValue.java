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
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Migration task
 */
public class ChangeELTHiveOutputPartitionValue extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        try {
            wrapQuot(item, processType);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (PersistenceException e) {
            // TODO Auto-generated catch block
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private boolean wrapQuot(Item item, ProcessType processType) throws PersistenceException {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean modified = false;
        EList node = processType.getNode();
        for (Object n : node) {
            NodeType type = (NodeType) n;
            if (type.getComponentName().equals("tELTHiveOutput")) { //$NON-NLS-1$
                EList elementParameterList = type.getElementParameter();
                for (Object elem : elementParameterList) {
                    ElementParameterType elemType = (ElementParameterType) elem;
                    if (elemType.getName().equals("FIELD_PARTITION")) { //$NON-NLS-1$
                        EList elemValue = elemType.getElementValue();
                        for (Object eVal : elemValue) {
                            ElementValueType elemVal = (ElementValueType) eVal;
                            String originV = elemVal.getValue();
                            elemVal.setValue("\"" + originV + "\""); //$NON-NLS-1$ //$NON-NLS-2$
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
        GregorianCalendar gc = new GregorianCalendar(2012, 11, 11, 17, 41, 10);
        return gc.getTime();
    }

}
