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

/**
 * ADD sizhaoliu TDQ-8431 2014-3-27
 * <p>
 * The tRecordMatching component used "Custom" as value in the Matching Function field, it should be changed to
 * lowercased "custom" as what we use in tMatchGroup and Match Rules, in order to avoid rule sharing problems.
 */
public class ChangeRecordMatchingCustomFunctionValueTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentConversion changeCustomFunctionValue = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                ElementParameterType eleParam = ComponentUtilities.getNodeProperty(node, "JOIN_KEY"); //$NON-NLS-1$
                if (eleParam != null) {
                    EList<ElementValueType> currentEleValues = eleParam.getElementValue();
                    Iterator<ElementValueType> iterator = currentEleValues.iterator();
                    while (iterator.hasNext()) {
                        ElementValueType param = iterator.next();
                        if ("MATCHING_TYPE".equals(param.getElementRef()) && "Custom".equals(param.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
                            param.setValue("custom"); //$NON-NLS-1$
                        }
                    }
                }
            }
        };

        try {
            List<IComponentFilter> filters = new ArrayList<IComponentFilter>();
            filters.add(new NameComponentFilter("tRecordMatching")); //$NON-NLS-1$
            Iterator<IComponentFilter> iter = filters.iterator();
            while (iter.hasNext()) {
                IComponentFilter filter = iter.next();
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(changeCustomFunctionValue));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 3, 27, 0, 0, 0);
        return gc.getTime();
    }
}
