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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.ParameterTypeFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class ChangeSqlPatternValueMigrationTask extends AbstractJobMigrationTask {

    private static final String SQLPATTERN_VALUE = "SQLPATTERN_VALUE"; //$NON-NLS-1$

    private IComponentFilter filter = new ParameterTypeFilter(SQLPATTERN_VALUE);

    private boolean modified;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar clendar = new GregorianCalendar(2008, 5, 28, 16, 0, 0);
        return clendar.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}
        List<IComponentConversion> list = new ArrayList<IComponentConversion>();
        list.add(new PropertyConversion());
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
					list);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        if (modified) {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                factory.save(item, new boolean[] { true });
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.NOTHING_TO_DO;
    }

    /**
     * yzhang ChangeSqlPatternValueMigrationTask class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
     *
     */
    private class PropertyConversion implements IComponentConversion {

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.components.conversions.IComponentConversion#transform(org.talend.designer.core.model.utils.emf.talendfile.NodeType)
         */
        public void transform(NodeType node) {

            EList values = ComponentUtilities.getNodeProperty(node, SQLPATTERN_VALUE).getElementValue();

            for (Object objElementValue : values) {
                ElementValueType value = (ElementValueType) objElementValue;
                String oldValue = value.getValue();
                if (!oldValue.contains("--")) { //$NON-NLS-1$
                    String newValue = "tempid--" + oldValue; //$NON-NLS-1$
                    value.setValue(newValue);
                    modified = true;
                }
            }

        }

    }

}
