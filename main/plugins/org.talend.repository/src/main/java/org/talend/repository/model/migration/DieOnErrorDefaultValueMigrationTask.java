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
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.AddPropertyDieOnErrorOptionConversion;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * 1. For new jobs : Die on Error should be unchecked by default for all components having this option (tDBOutput, and
 * tFileInput) 2. For old jobs, keep the ckeckbox checked with a migration task on the same components
 */
public class DieOnErrorDefaultValueMigrationTask extends AbstractJobMigrationTask {

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
        String[] componentsName = new String[] { "tAccessOutput", "tAccessRow", "tDB2Output", "tDB2Row", "tDBOutput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "tDBSQLRow", "tFileInputDelimited", "tFileInputExcel", "tFileInputLDIF", "tFileInputPositional", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "tFileInputRegex", "tFirebirdOutput", "tFirebirdRow", "tHSQLDbOutput", "tHSQLDbRow", "tInformixOutput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                "tInformixRow", "tIngresOutput", "tIngresRow", "tInterbaseOutput", "tInterbaseRow", "tJDBCOutput", "tJDBCRow", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                "tJavaDBOutput", "tJavaDBRow", "tMSSqlOutput", "tMSSqlRow", "tMysqlOutput", "tMysqlRow", "tOracleOutput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                "tOracleRow", "tPostgresqlOutput", "tPostgresqlRow", "tSQLiteOutput", "tSQLiteRow", "tSybaseOutput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                "tSybaseRow", "tTeradataFastLoad", "tTeradataOutput", "tFileInputCSV" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        try {

            for (int i = 0; i < componentsName.length; i++) {
                IComponentFilter filter = new NameComponentFilter(componentsName[i]);
                IComponentConversion addPropertyDieOnError = new AddPropertyDieOnErrorOptionConversion();
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addPropertyDieOnError));
            }

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
