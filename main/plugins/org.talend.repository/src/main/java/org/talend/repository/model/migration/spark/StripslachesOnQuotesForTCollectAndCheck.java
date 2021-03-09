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
package org.talend.repository.model.migration.spark;

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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * TBD-2642: Remove slash before qutoes on tCollectAndCheck
 */
public class StripslachesOnQuotesForTCollectAndCheck extends AbstractJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS_SPARK);
        // MR is here because Process_SPARK does not works
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            IComponentFilter filter = new NameComponentFilter("tCollectAndCheck");
            IComponentConversion addOption = new StripslachesOnQuotesForTCollectAndCheckConversion();
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class StripslachesOnQuotesForTCollectAndCheckConversion implements IComponentConversion {

        public StripslachesOnQuotesForTCollectAndCheckConversion() {
            super();
        }

        @Override
        public void transform(NodeType node) {
            ElementParameterType referenceProperty = ComponentUtilities.getNodeProperty(node, "REFERENCE");
            if (referenceProperty != null && referenceProperty.getValue() != null) {
                ComponentUtilities.setNodeValue(node, "REFERENCE", referenceProperty.getValue().replace("\\\"", "\""));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 10, 7, 12, 0, 0);
        return gc.getTime();
    }

}
