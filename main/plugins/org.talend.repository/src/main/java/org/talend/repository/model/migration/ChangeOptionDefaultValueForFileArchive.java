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
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeOptionDefaultValueForFileArchive extends
		AbstractJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (getProject().getLanguage() != ECodeLanguage.JAVA
				|| processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}

		String[] componentsName = new String[] {  "tFileArchive", "tFileUnarchive" };
		IComponentConversion changeJDBCDriverJarType = new IComponentConversion() {

			public void transform(NodeType node) {
				if("tFileArchive".equals(node.getComponentName())){
					ElementParameterType encryptMethod = ComponentUtilities.getNodeProperty(node, "ENCRYPT_METHOD");
					if (encryptMethod != null) {
						if ("DEFAULT".equals(encryptMethod.getValue())) {
							ComponentUtilities.setNodeValue(node,"ENCRYPT_METHOD","JAVA_ENCRYPT");
						}
						if ("AES256".equals(encryptMethod.getValue())) {
							ComponentUtilities.setNodeValue(node,"ENCRYPT_METHOD","ENC_METHOD_AES");
						}
					}
				}
				if("tFileUnarchive".equals(node.getComponentName())){
					ElementParameterType encryptMethod = ComponentUtilities.getNodeProperty(node, "DECRYPT_METHOD");
					if (encryptMethod != null) {
						if ("DEFAULT".equals(encryptMethod.getValue())) {
							ComponentUtilities.setNodeValue(node,"DECRYPT_METHOD","JAVA_DECRYPT");
						}
						if ("AES256".equals(encryptMethod.getValue())) {
							ComponentUtilities.setNodeValue(node,"DECRYPT_METHOD","ZIP4J_DECRYPT");
						}
					}
				}

			}
		};

		for (String name : componentsName) {
			IComponentFilter filter = new NameComponentFilter(name); //$NON-NLS-1$

			try {
				ModifyComponentsAction
						.searchAndModify(
								item,
								processType,
								filter,
								Arrays.<IComponentConversion> asList(changeJDBCDriverJarType));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
				return ExecutionResult.FAILURE;
			}
		}

		return ExecutionResult.SUCCESS_WITH_ALERT;

	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2015, 9, 13, 16, 0, 0);
		return gc.getTime();
	}

}
