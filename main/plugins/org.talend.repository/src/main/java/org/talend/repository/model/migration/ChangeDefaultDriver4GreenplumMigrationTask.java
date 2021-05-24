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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractAllJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.migration.IMigrationTask.ExecutionResult;

/**
 * Change default driver type for Greenplum components
 *
 */
public class ChangeDefaultDriver4GreenplumMigrationTask extends AbstractAllJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		final ProcessType processType = getProcessType(item);
		if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}
		String[] compNames = { "tELTGreenplumMap", "tGreenplumBulkExec", "tGreenplumConnection", "tGreenplumGPLoad", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"tGreenplumInput", "tGreenplumOutputBulkExec", "tGreenplumOutput", "tGreenplumRow", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"tGreenplumSCD" }; //$NON-NLS-1$

		for (String compName : compNames) {
			IComponentFilter filter = new NameComponentFilter(compName); // $NON-NLS-1$
			try {
				ModifyComponentsAction.searchAndModify(item, processType, filter,
						Arrays.<IComponentConversion>asList(new IComponentConversion() {

							public void transform(NodeType node) {
								ElementParameterType dbDriver = ComponentUtilities.getNodeProperty(node, "DB_DRIVER"); //$NON-NLS-1$
								if (dbDriver == null) {
									ComponentUtilities.addNodeProperty(node, "DB_DRIVER", "CLOSED_LIST");
									ComponentUtilities.getNodeProperty(node, "DB_DRIVER").setValue("POSTGRESQL");
								}
							}
						}));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
				return ExecutionResult.FAILURE;
			}
		}

		return ExecutionResult.SUCCESS_WITH_ALERT;
	}
	
	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2021, 0, 6, 12, 0, 0);
		return gc.getTime();
	}

}
