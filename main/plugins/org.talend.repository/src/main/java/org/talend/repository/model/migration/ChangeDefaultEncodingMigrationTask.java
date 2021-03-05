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
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * jjzhou class global comment. Detailled comment
 */
public class ChangeDefaultEncodingMigrationTask extends
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
		GregorianCalendar gc = new GregorianCalendar(2013, 10, 26, 12, 0, 0);
		return gc.getTime();
	}

	@SuppressWarnings("cast")
	private void AddExecuteMode(Item item) throws Exception {
		ProcessType processType = getProcessType(item);
		java.util.List<IComponentFilter> filters = new java.util.ArrayList<IComponentFilter>();
		filters.add(new NameComponentFilter("tSybaseBulkExec"));//$NON-NLS-1$
		filters.add(new NameComponentFilter("tSybaseOutputBulkExec"));//$NON-NLS-1$
		filters.add(new NameComponentFilter("tOracleBulkExec"));//$NON-NLS-1$
		filters.add(new NameComponentFilter("tOracleOutputBulkExec"));//$NON-NLS-1$
		IComponentConversion addOption = new AddExecuteMode(); //$NON-NLS-1$
		java.util.Iterator<IComponentFilter> iter = filters.iterator();
		while (iter.hasNext()) {
			IComponentFilter filter = (IComponentFilter) iter.next();
			ModifyComponentsAction.searchAndModify(item, processType, filter,
					Arrays.<IComponentConversion> asList(addOption));
		}
	}

	/**
	 *
	 * jjzhou changeDefaulte value for encoding
	 */
	private class AddExecuteMode implements IComponentConversion {

		public AddExecuteMode() {
			super();
		}

		public void transform(NodeType node) {
			ElementParameterType encoding = ComponentUtilities.getNodeProperty(node, "ENCODING"); //$NON-NLS-1$

			if (encoding != null) {
				String value = encoding.getValue();  //$NON-NLS-1$
				String componeName = node.getComponentName();  //$NON-NLS-1$

				if ("tSybaseOutputBulkExec".equals(componeName)) {
					ElementParameterType sybaseEncoding = ComponentUtilities.getNodeProperty(node, "SYBASE_ENCODING"); //$NON-NLS-1$
					if (sybaseEncoding == null) {
						ComponentUtilities.addNodeProperty(node,"SYBASE_ENCODING", "OPEN_LIST"); //$NON-NLS-1$ //$NON-NLS-2$
						sybaseEncoding = ComponentUtilities.getNodeProperty(node, "SYBASE_ENCODING");
						changeEncodingValue(value, sybaseEncoding);
					}
				} else if ("tSybaseBulkExec".equals(componeName)) {
					changeEncodingValue(value, encoding);
				} else if ("tOracleBulkExec".equals(componeName)) {
					ElementParameterType enableEditOracleEncoding = ComponentUtilities.getNodeProperty(node, "ENABLE_EDIT_ORACLE_ENCODING"); //$NON-NLS-1$
					if (enableEditOracleEncoding != null) {
						String enableEditOracleEncodingValue = enableEditOracleEncoding.getValue(); //$NON-NLS-1$
						ElementParameterType oracleEnconding = ComponentUtilities.getNodeProperty(node, "ORACLE_ENCODING"); //$NON-NLS-1$
						if (oracleEnconding != null) {
							if("true".equals(enableEditOracleEncodingValue)){
								String oracleEncodingValue = oracleEnconding.getValue(); //$NON-NLS-1$
								encoding.setValue(oracleEncodingValue);
								ComponentUtilities.removeNodeProperty(node,"ENABLE_EDIT_ORACLE_ENCODING");
								ComponentUtilities.removeNodeProperty(node,"ORACLE_ENCODING");
							}else{
								ComponentUtilities.removeNodeProperty(node,"ENABLE_EDIT_ORACLE_ENCODING");
								ComponentUtilities.removeNodeProperty(node,"ORACLE_ENCODING");
								changeEncodingValue(value, encoding);
							}
						}
					}else{
						changeEncodingValue(value, encoding);
					}
				} else if ("tOracleOutputBulkExec".equals(componeName)) {
					ElementParameterType enableEditOracleEncoding = ComponentUtilities.getNodeProperty(node, "ENABLE_EDIT_ORACLE_ENCODING"); //$NON-NLS-1$
					if (enableEditOracleEncoding != null) {
						ElementParameterType oracleEnconding = ComponentUtilities.getNodeProperty(node, "ORACLE_ENCODING"); //$NON-NLS-1$
						if (oracleEnconding != null) {
							String enableEditOracleEncodingValue = enableEditOracleEncoding.getValue(); //$NON-NLS-1$
							if ("true".equals(enableEditOracleEncodingValue)) {
								ComponentUtilities.removeNodeProperty(node,"ENABLE_EDIT_ORACLE_ENCODING");
								oracleEnconding.setField("OPEN_LIST");
							} else {
								ComponentUtilities.removeNodeProperty(node,"ENABLE_EDIT_ORACLE_ENCODING");
								oracleEnconding.setField("OPEN_LIST");
								changeEncodingValue(value, oracleEnconding);
							}
						}
					}else{
						ComponentUtilities.addNodeProperty(node,"ORACLE_ENCODING", "OPEN_LIST"); //$NON-NLS-1$ //$NON-NLS-2$
						enableEditOracleEncoding = ComponentUtilities.getNodeProperty(node, "ORACLE_ENCODING");
						changeEncodingValue(value, enableEditOracleEncoding);
					}
				}
			}
		}
	}

	public void changeEncodingValue(String sourceVale, ElementParameterType param) {
		if ("\"UTF-8\"".equalsIgnoreCase(sourceVale)) { //$NON-NLS-1$
			param.setValue("\"UTF8\""); //$NON-NLS-1$
		} else if ("\"ISO-8859-15\"".equalsIgnoreCase(sourceVale)) { //$NON-NLS-1$
			param.setValue("\"WE8ISO8859P15\""); //$NON-NLS-1$
		} else if ("\"GBK\"".equalsIgnoreCase(sourceVale)) { //$NON-NLS-1$
			param.setValue("\"ZHS16GBK\""); //$NON-NLS-1$
		} else if ("\"GB2312\"".equalsIgnoreCase(sourceVale)) { //$NON-NLS-1$
			param.setValue("\"ZHS16CGB231280\""); //$NON-NLS-1$
		} else if ("\"BIG5\"".equalsIgnoreCase(sourceVale)) { //$NON-NLS-1$
			param.setValue("\"ZHT16BIG5\""); //$NON-NLS-1$
		} else {
			param.setValue("\"AL16UTF16\""); //$NON-NLS-1$
		}
	}
}
