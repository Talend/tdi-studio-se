// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
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
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class SAPBAPIParameterTypeMigrationTask extends AbstractJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}
		String[] componentsName = new String[] { "tSAPBapi" };

		try {

			for (String element : componentsName) {
				IComponentFilter filter = new NameComponentFilter(element);
				ModifyComponentsAction.searchAndModify(item, processType, filter,
						Arrays.<IComponentConversion>asList(new IComponentConversion() {

							@SuppressWarnings("unchecked")
							@Override
							public void transform(NodeType node) {
								ElementParameterType mapping_input = ComponentUtilities.getNodeProperty(node,
										"MAPPING_INPUT");
								TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
								if (mapping_input != null) {
									List<ElementValueType> newElementValues = new ArrayList<ElementValueType>();
									List<ElementValueType> elementValues = mapping_input.getElementValue();
									boolean tableType = false;
									for (ElementValueType elementValue : elementValues) {
										if ("TYPE".equals(elementValue.getElementRef())
												&& "TABLE".equals(elementValue.getValue())) {
											tableType = true;
										}

										if ("CHANGING".equals(elementValue.getElementRef())) {
											ElementValueType value = fileFact.createElementValueType();
											value.setElementRef("PARAMETER_TYPE");
											boolean is_changing_parameter = "true".equalsIgnoreCase(elementValue.getValue());
											if (tableType) {
												value.setValue(is_changing_parameter ? "CHANGING" : "TABLES");
											} else {
												value.setValue(is_changing_parameter ? "CHANGING" : "IMPORT");
											}

											newElementValues.add(value);

											tableType = false;

											continue;
										}

										newElementValues.add(elementValue);
									}

									elementValues.clear();
									elementValues.addAll(newElementValues);
								}
							}

						}));
			}

			return ExecutionResult.SUCCESS_NO_ALERT;
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
	}

	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2019, 9, 23, 18, 0, 0);
		return gc.getTime();
	}

}
