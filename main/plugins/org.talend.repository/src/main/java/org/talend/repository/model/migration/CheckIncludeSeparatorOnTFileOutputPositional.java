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

@SuppressWarnings("nls")
public class CheckIncludeSeparatorOnTFileOutputPositional extends AbstractJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS_SPARK);
        toReturn.add(ERepositoryObjectType.PROCESS_SPARKSTREAMING);
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        toReturn.add(ERepositoryObjectType.PROCESS_STORM);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            IComponentFilter filter = new NameComponentFilter("tFileOutputPositional");
            IComponentConversion addOption = new CheckIncludeSeparatorOnTFileOutputPositionalConversion();
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class CheckIncludeSeparatorOnTFileOutputPositionalConversion implements IComponentConversion {

        public CheckIncludeSeparatorOnTFileOutputPositionalConversion() {
            super();
        }

        @Override
        public void transform(NodeType node) {
            ElementParameterType includeHeaderProperty = ComponentUtilities.getNodeProperty(node, "INCLUDEHEADER");
            if (includeHeaderProperty != null && "true".equals(includeHeaderProperty.getValue())) {
                ComponentUtilities.addNodeProperty(node, "INCLUDESEPARATOR", "CHECK");
                ComponentUtilities.setNodeValue(node, "INCLUDESEPARATOR", "true");
            }
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 8, 7, 15, 0, 0);
        return gc.getTime();
    }
}
