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
public class ChangeDeleteEmptyFileValueIfAppendTrueFeatureTDI26625 extends
		AbstractJobMigrationTask {

	public ChangeDeleteEmptyFileValueIfAppendTrueFeatureTDI26625() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (getProject().getLanguage() != ECodeLanguage.JAVA
				|| processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}
		String[] componentsName = new String[] {
				"tFileOutputARFF", "tFileOutputExcel", "tFileOutputLDIF", "tFileOutputPositional"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IComponentConversion changeDeletEmptyFileValue = new IComponentConversion() {
			public void transform(NodeType node) {
				ElementParameterType append = ComponentUtilities.getNodeProperty(node, "APPEND");
				if(append==null){
					append = ComponentUtilities.getNodeProperty(node, "APPEND_FILE");
				}
				if (append != null && "true".equals(append.getValue())) {
					ElementParameterType deletEmptyFile = ComponentUtilities
							.getNodeProperty(node, "DELETE_EMPTYFILE");
					if (deletEmptyFile != null && "true".equals(deletEmptyFile.getValue())) {
						deletEmptyFile.setValue("false");
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
								Arrays.<IComponentConversion> asList(changeDeletEmptyFileValue));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
				return ExecutionResult.FAILURE;
			}
		}
		return ExecutionResult.SUCCESS_WITH_ALERT;
	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2013, 06, 11, 12, 0, 0);
		return gc.getTime();
	}

}
