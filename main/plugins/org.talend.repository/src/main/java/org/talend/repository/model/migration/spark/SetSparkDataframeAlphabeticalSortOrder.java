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
package org.talend.repository.model.migration.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 *
 * Set the Spark Dataframe Alphabatical sort order in tHiveOutput & tFileParquet. If the component doesn't have the
 * checkbox then we want to maintain the legacy (alphabatical) order.
 *
 */
@SuppressWarnings("deprecation")
public class SetSparkDataframeAlphabeticalSortOrder extends AbstractJobMigrationTask {

    final String propertyName = "SORT_COLUMNS_ALPHABETICALLY"; //$NON-NLS-1$

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        String[] componentsName = new String[] { "tHiveOutput", "tFileOutputParquet" }; //$NON-NLS-1$ //$NON-NLS-2$

        try {

            for (String element : componentsName) {
                IComponentFilter filter = new NameComponentFilter(element);
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(new IComponentConversion() {

                            @Override
                            public void transform(NodeType node) {
                                if (ComponentUtilities.getNodeProperty(node, propertyName) == null) {
                                    ComponentUtilities.addNodeProperty(node, propertyName, "CHECK"); //$NON-NLS-1$
                                    ComponentUtilities.getNodeProperty(node, propertyName).setValue("true"); //$NON-NLS-1$
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
        GregorianCalendar gc = new GregorianCalendar(2017, 05, 02, 12, 10, 15);
        return gc.getTime();
    }
}
