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
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC xqliu class global comment. Detailled comment
 */
public class UpdatetPersonatorOutputSchemaTask extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 4, 9, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            // tPersonator
            IComponentFilter filter_tAddressRowCloud = new NameComponentFilter("tPersonator");
            IComponentConversion update_tPersonator = new Update_tPersonator();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter_tAddressRowCloud,
                            Arrays.<IComponentConversion> asList(update_tPersonator));

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class Update_tPersonator implements IComponentConversion {

        public void transform(NodeType node) {
            // update OUTPUT_SCHEMA_TYPE_1
            ElementParameterType nodeProperty1 = ComponentUtilities.getNodeProperty(node, "OUTPUT_SCHEMA_TYPE_1");
            boolean flag_nodeProperty1 = nodeProperty1 == null;
            if (flag_nodeProperty1) {
                nodeProperty1 = TalendFileFactory.eINSTANCE.createElementParameterType();
            }
            nodeProperty1.setField("RADIO"); //$NON-NLS-1$
            nodeProperty1.setName("OUTPUT_SCHEMA_TYPE_1"); //$NON-NLS-1$
            nodeProperty1.setValue("true"); //$NON-NLS-1$
            nodeProperty1.setShow(false);
            if (flag_nodeProperty1) {
                node.getElementParameter().add(nodeProperty1);
            }

            // update OUTPUT_SCHEMA_TYPE_2
            ElementParameterType nodeProperty2 = ComponentUtilities.getNodeProperty(node, "OUTPUT_SCHEMA_TYPE_2");
            boolean flag_nodeProperty2 = nodeProperty2 == null;
            if (flag_nodeProperty2) {
                nodeProperty2 = TalendFileFactory.eINSTANCE.createElementParameterType();
            }
            nodeProperty2.setField("RADIO"); //$NON-NLS-1$
            nodeProperty2.setName("OUTPUT_SCHEMA_TYPE_2"); //$NON-NLS-1$
            nodeProperty2.setValue("false"); //$NON-NLS-1$
            nodeProperty2.setShow(false);
            if (flag_nodeProperty2) {
                node.getElementParameter().add(nodeProperty2);
            }
        }
    }
}
