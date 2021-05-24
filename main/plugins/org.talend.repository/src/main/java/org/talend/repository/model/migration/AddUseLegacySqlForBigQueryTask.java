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
import java.util.List;

import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
/**
 *
 * @author kcoepeau
 *
 * Migrate tBigQuery DI components to keep Legacy SQL usage for old jobs.
 *
 */
public class AddUseLegacySqlForBigQueryTask extends AbstractJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.PROCESS_SPARK);
        toReturn.add(ERepositoryObjectType.PROCESS_SPARKSTREAMING);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if(processType == null){
            return ExecutionResult.NOTHING_TO_DO;
        }

        List<String> componentNames = Arrays.asList("tBigQueryInput");

        IComponentConversion addUseLegacySql = new AddLegacySqlCheckbox();
        try {
            for(String componentName : componentNames){
                ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter(componentName),
                        Arrays.<IComponentConversion> asList(addUseLegacySql));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            return ExecutionResult.FAILURE;
        }



    }

    private class AddLegacySqlCheckbox implements IComponentConversion {

        private String field = "CHECK"; //$NON-NLS-1$

        private String name = "USE_LEGACY_SQL"; //$NON-NLS-1$

        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, name) == null) {
                ComponentUtilities.addNodeProperty(node, name, field);
                ComponentUtilities.setNodeValue(node, name, "true");
            }

        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 1, 18, 18, 0, 0);
        return gc.getTime();
    }

}
