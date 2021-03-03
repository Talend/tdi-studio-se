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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * jyhu class global comment. Detailled comment
 */
public class SetFilterTiemFromTotoFromPOPComponentsMigrationTask extends AbstractJobMigrationTask {
	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
	 * ProcessItem)
	 */
	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}

		try {

			IComponentFilter filter = new NameComponentFilter("tPOP");
			ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
					.<IComponentConversion> asList(new IComponentConversion() {

						public void transform(NodeType node) {
							ElementParameterType p = ComponentUtilities.getNodeProperty(node, "ADVANCED_FILTER");
							EList<ElementValueType> es = p.getElementValue();
							for(int j = 0;j<es.size();j++){
								if(es.get(j)!=null && "To".equals(es.get(j).getValue())){
									es.get(j).setValue("From");
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

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2012, 6, 25, 12, 0, 0);
		return gc.getTime();
	}
}
