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
import java.util.List;

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
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Kevin.cui class global comment. substitute removed operators in MDM component
 */
public class SubstituteRemovedMDMOperatorMigrationTask extends AbstractJobMigrationTask {

	@Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 4, 27, 17, 0, 0);
        return gc.getTime();
    }

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);

		if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}

        String[] componentsName = new String[] {
                "tMDMInput", "tMDMDelete", "tMDMViewSearch" };
        IComponentConversion substituteRemovedOperatorsFromJobItem = new IComponentConversion() {
			public void transform(NodeType node) {
				ElementParameterType operations = ComponentUtilities.getNodeProperty(node, "OPERATIONS");
				if(operations != null && operations.getElementValue().size() > 0){
					for(ElementValueType elementValue : (List<ElementValueType>) operations.getElementValue()) {
                        if ("STRICTCONTAINS".equals(elementValue.getValue()) && "FUNCTION".equals(elementValue.getElementRef())) {
                            elementValue.setValue("CONTAINS");
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
								Arrays.<IComponentConversion> asList(substituteRemovedOperatorsFromJobItem));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
				return ExecutionResult.FAILURE;
			}
		}
		return ExecutionResult.SUCCESS_WITH_ALERT;
	}

}

