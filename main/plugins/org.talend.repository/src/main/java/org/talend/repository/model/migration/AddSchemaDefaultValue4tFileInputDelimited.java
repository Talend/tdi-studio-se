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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * set default value for tFileInputDelimited schema .
 *
 */
public class AddSchemaDefaultValue4tFileInputDelimited extends
		AbstractJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		IComponentFilter filter = new NameComponentFilter("tFileInputDelimited");
		try {
			ModifyComponentsAction.searchAndModify(item, processType, filter,
					Arrays.<IComponentConversion> asList(new IComponentConversion() {

						public void transform(NodeType node) {
							EList<EObject> list = node.eContents();
							for (EObject object : list) {
								if (object instanceof MetadataType) {
									MetadataType flow = (MetadataType) object;
									if ("FLOW".equalsIgnoreCase(flow.getConnector())) {
										Iterator<?> columns = flow.getColumn().iterator();
										while (columns.hasNext()) {
											Object outColumn = columns.next();
											if (outColumn instanceof ColumnType) {
												ColumnType column = ((ColumnType) outColumn);
												JavaType javaType = JavaTypesManager.getJavaTypeFromId(column.getType());
												boolean isJavaPrimitiveType = JavaTypesManager.isJavaPrimitiveType(javaType,column.isNullable());
												if(isJavaPrimitiveType && (column.getDefaultValue()==null || column.getDefaultValue().isEmpty())){
													String defaultValue = JavaTypesManager.getDefaultValueFromJavaType(javaType.getPrimitiveClass().getSimpleName(), null);
													if(defaultValue != null){
														column.setDefaultValue(defaultValue);
													}
												}
											}
										}
									}
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
		GregorianCalendar gc = new GregorianCalendar(2015, 2, 18, 12, 0, 0);
		return gc.getTime();
	}
}
