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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by JYHU on Dec 31, 2012 Detailled comment
 *
 */
public class AddCaseSensitiveToTCreateTableWithHSQLDB extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 12, 31, 18, 50, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            IComponentFilter filter = new NameComponentFilter("tCreateTable"); //$NON-NLS-1$
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        public void transform(NodeType node) {
                            ElementParameterType ept = ComponentUtilities.getNodeProperty(node, "DBTYPE");
                            if (ept != null && ("HSQLDB").equals(ept.getValue())) {
                                if (ComponentUtilities.getNodeProperty(node, "CASESENSITIVE") == null) { //$NON-NLS-1$
                                    ComponentUtilities.addNodeProperty(node, "CASESENSITIVE", "CHECK"); //$NON-NLS-1$//$NON-NLS-2$
                                    ComponentUtilities.getNodeProperty(node, "CASESENSITIVE").setValue("true");//$NON-NLS-1$//$NON-NLS-2$
                                }
                            }
                        }

                    }));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }
}
