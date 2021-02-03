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
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractAllJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 *
 * Change the dynamic field length in file input components
 *
 */
public class ChangeFileInputDynamicFieldLength extends AbstractAllJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		final ProcessType processType = getProcessType(item);
		String[] compNames = { "tFileInputDelimited", //$NON-NLS-1$
				"tFileInputExcel" }; //$NON-NLS-1$

		IComponentConversion conversion = new IComponentConversion() {

			@Override
			public void transform(NodeType node) {
				for (Object om : node.getMetadata()) {
					MetadataType metadata = (MetadataType) om;
					for (Object oc : metadata.getColumn()) {
						ColumnType column = (ColumnType) oc;
						String type = column.getType();
						if (!"id_Dynamic".equals(type)) {
							continue;
						} else {
							int columLength = column.getLength();
							// Only the column length is not empty would be used after TDI-45400
							if (columLength >= 0) {
								column.setLength(100);
							}
						}
					}
				}
			}

		};

		for (String name : compNames) {
			IComponentFilter filter = new NameComponentFilter(name);

			try {
				ModifyComponentsAction.searchAndModify(item, processType, filter,
						Arrays.<IComponentConversion>asList(conversion));
			} catch (PersistenceException e) {
				return ExecutionResult.FAILURE;
			}
		}
		return ExecutionResult.SUCCESS_NO_ALERT;
	}

	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2021, 0, 25, 12, 0, 0);
		return gc.getTime();
	}

}
