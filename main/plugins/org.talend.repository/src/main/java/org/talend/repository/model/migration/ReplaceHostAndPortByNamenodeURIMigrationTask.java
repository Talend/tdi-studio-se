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
 * rdubois class global comment. Detailled comment
 */
public class ReplaceHostAndPortByNamenodeURIMigrationTask extends
		AbstractJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		try {
			replaceHostAndPort(item);
			return ExecutionResult.SUCCESS_NO_ALERT;
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2012, 7, 27, 16, 45, 0);
		return gc.getTime();
	}

	private void replaceHostAndPort(Item item) throws Exception {
		ProcessType processType = getProcessType(item);
		java.util.List<IComponentFilter> filters = new java.util.ArrayList<IComponentFilter>();
		filters.add(new NameComponentFilter("tHDFSPut"));
		filters.add(new NameComponentFilter("tHDFSGet"));
		filters.add(new NameComponentFilter("tHDFSRename"));
		IComponentConversion addNNUri = new AddNNUri();
		java.util.Iterator<IComponentFilter> iter = filters.iterator();
		while (iter.hasNext()) {
			IComponentFilter filter = (IComponentFilter) iter.next();
			ModifyComponentsAction.searchAndModify(item, processType, filter,
					Arrays.<IComponentConversion> asList(addNNUri));
		}
	}

	/**
	 *
	 * rdubois AddConnectionMode class global comment. Detailled comment
	 */
	private class AddNNUri implements IComponentConversion {

		private String field = "TEXT"; //$NON-NLS-1$

		private String name = "FS_DEFAULT_NAME"; //$NON-NLS-1$

		public AddNNUri() {
			super();
		}

		public void transform(NodeType node) {

			if (ComponentUtilities.getNodeProperty(node, name) == null) {
				ComponentUtilities.addNodeProperty(node, name, field);
			}

			String host = "", port = "";
			boolean hostDone = false, portDone = false;

			for (Object o : node.getElementParameter()) {
				ElementParameterType para = (ElementParameterType) o;
				if ("HOST".equals(para.getName())) {
					host = para.getValue();
					hostDone = true;
				}
				if ("PORT".equals(para.getName())) {
					port = para.getValue();
					portDone = true;
				}
				if (hostDone && portDone)
					break;
			}

			for (Object o : node.getElementParameter()) {
				ElementParameterType para = (ElementParameterType) o;
				if ("DB_VERSION".equals(para.getName())) { //$NON-NLS-1$
					if (para.getValue()
							.startsWith("hadoop-0.20.2-dev-core.jar")) {
						ComponentUtilities.setNodeValue(node, name, "\"maprfs://\"+"
								+ host + "+\":\" + " + port + " + \"/\"");
					} else {
						ComponentUtilities.setNodeValue(node, name, "\"hdfs://\"+"
								+ host + "+\":\" + " + port);
					}
					break;
				}
			}

		}
	}
}
