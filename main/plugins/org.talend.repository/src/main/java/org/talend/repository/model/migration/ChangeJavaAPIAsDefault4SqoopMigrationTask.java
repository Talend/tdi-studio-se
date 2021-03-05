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
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * jjzhou class global comment. Detailled comment
 */
public class ChangeJavaAPIAsDefault4SqoopMigrationTask extends
		AbstractJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		try {
			AddExecuteMode(item);
			return ExecutionResult.SUCCESS_NO_ALERT;
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2013, 9, 30, 12, 0, 0);
		return gc.getTime();
	}

	private void AddExecuteMode(Item item) throws Exception {
		ProcessType processType = getProcessType(item);
		java.util.List<IComponentFilter> filters = new java.util.ArrayList<IComponentFilter>();
		filters.add(new NameComponentFilter("tSqoopImport"));//$NON-NLS-1$
		filters.add(new NameComponentFilter("tSqoopExport"));//$NON-NLS-1$
		filters.add(new NameComponentFilter("tSqoopImportAllTables"));//$NON-NLS-1$
		IComponentConversion addOption = new AddExecuteMode();
		java.util.Iterator<IComponentFilter> iter = filters.iterator();
		while (iter.hasNext()) {
			IComponentFilter filter = (IComponentFilter) iter.next();
			ModifyComponentsAction.searchAndModify(item, processType, filter,
					Arrays.<IComponentConversion> asList(addOption));
		}
	}

	/**
	 *
	 * rdubois AddConnectionMode class global comment. Detailled comment
	 */
	private class AddExecuteMode implements IComponentConversion {

		private String field = "RADIO"; //$NON-NLS-1$

		private String commandline_name = "USE_COMMANDLINE"; //$NON-NLS-1$

		private String name_api = "USE_JAVAAPI"; //$NON-NLS-1$

		public AddExecuteMode() {
			super();
		}

		public void transform(NodeType node) {
			if (ComponentUtilities.getNodeProperty(node, commandline_name) == null) {
				ComponentUtilities.addNodeProperty(node, commandline_name, field);
				ComponentUtilities.setNodeValue(node, commandline_name, "true");//$NON-NLS-1$
				if (ComponentUtilities.getNodeProperty(node, name_api) == null) {
					ComponentUtilities.addNodeProperty(node, name_api, field);
				}
				ComponentUtilities.setNodeValue(node, name_api, "false");//$NON-NLS-1$
			}
		}
	}
}
