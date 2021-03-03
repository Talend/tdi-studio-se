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

import org.talend.commons.exception.PersistenceException;
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
 * DOC jzhao class global comment. Detailled comment
 */
public class AddCDCTypeForOracleCDCMigrationTask extends
		AbstractJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		IComponentFilter filter = new NameComponentFilter("tOracleCDC");
		try {
			ModifyComponentsAction
					.searchAndModify(
							item,
							processType,
							filter,
							Arrays.<IComponentConversion> asList(new IComponentConversion() {

								public void transform(NodeType node) {
									ElementParameterType logMode = ComponentUtilities
											.getNodeProperty(node,
													"CDC_TYPE_MODE");
									if (logMode == null) {
										return;
									} else {
										ComponentUtilities
												.addNodeProperty(node,
														"CDC_MODE",
														"CLOSED_LIST");
										if ("true".equalsIgnoreCase(logMode
												.getValue())) {
											ComponentUtilities.setNodeValue(
													node, "CDC_MODE", "LOG");
										} else {
											ComponentUtilities
													.setNodeValue(node,
															"CDC_MODE",
															"TRIGGER");
										}

									}
								}
							}));
		} catch (PersistenceException e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}

		return ExecutionResult.SUCCESS_NO_ALERT;

	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2014, 7, 27, 12, 0, 0);
		return gc.getTime();
	}
}
