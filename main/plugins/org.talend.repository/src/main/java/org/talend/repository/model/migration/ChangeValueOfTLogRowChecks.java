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

import org.talend.commons.ui.runtime.exception.RuntimeExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.PropertyComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class ChangeValueOfTLogRowChecks extends AbstractJobMigrationTask {

    /**
     *
     */
    private static final String VERTICAL = "VERTICAL"; //$NON-NLS-1$

    private static final String TABLE_PRINT = "TABLE_PRINT"; //$NON-NLS-1$

    IComponentFilter filterTablePrint = new PropertyComponentFilter("tLogRow", TABLE_PRINT, "true"); //$NON-NLS-1$ //$NON-NLS-2$

    IComponentFilter filterVertical = new PropertyComponentFilter("tLogRow", VERTICAL, "true"); //$NON-NLS-1$ //$NON-NLS-2$

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
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filterTablePrint, Arrays.<IComponentConversion> asList(new Conversion(
                    TABLE_PRINT)));
            ModifyComponentsAction.searchAndModify(item, processType, filterVertical, Arrays.<IComponentConversion> asList(new Conversion(
                    VERTICAL)));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception err) {
            RuntimeExceptionHandler.process(err);
            return ExecutionResult.FAILURE;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        java.util.GregorianCalendar gc = new GregorianCalendar(2008, 4, 8, 13, 0, 0);
        return gc.getTime();
    }

    /**
     * yzhang ChangeValueOfTLogRowChecks class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
     *
     */
    private class Conversion implements IComponentConversion {

        private String propertyName;

        /**
         * yzhang ChangeValueOfTLogRowChecks.Conversion constructor comment.
         */
        public Conversion(String propertyName) {
            this.propertyName = propertyName;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.components.conversions.IComponentConversion#transform(org.talend.designer.core.model.utils.emf.talendfile.NodeType)
         */
        public void transform(NodeType node) {
            ComponentUtilities.setNodeValue(node, propertyName, "false"); //$NON-NLS-1$
        }
    }
}
