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

/**
 * With the improvements from TBD-2639:
 *
 * The consumer timeout is no longer directly set from component UI, but calculated depending on whether the user is
 * timing out from the first message or from the last received message (or using both stop conditions).
 *
 * The old default behaviour when reading "From beginning" was to delete any existing offsets. This now needs to be
 * explicitly set using the RESET_OFFSET parameter to true.
 */
public class UpdateKakfaInputConsumerTimeoutAndAutoOffsetTask extends AbstractJobMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 10, 9, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            IComponentFilter filter = new NameComponentFilter("tKafkaInput"); //$NON-NLS-1$
            IComponentConversion addOption = new UpdateKakfaInputConversion();
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addOption));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class UpdateKakfaInputConversion implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            // If the consumer timeout is not -1, then use it as a MAX_WAIT stopping condition.
            ElementParameterType consumerTimeout = ComponentUtilities.getNodeProperty(node, "KAFKA_CONSUMER_TIMEOUT"); //$NON-NLS-1$
            if (consumerTimeout != null && consumerTimeout.getValue() != null) {
                if (!"\"-1\"".equals(consumerTimeout.getValue()) && !"-1".equals(consumerTimeout.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.addNodeProperty(node, "USE_BATCH_MESSAGE_TIMEOUT", "CHECK"); //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.setNodeValue(node, "USE_BATCH_MESSAGE_TIMEOUT", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.addNodeProperty(node, "BATCH_MESSAGE_TIMEOUT", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$
                    ComponentUtilities.setNodeValue(node, "BATCH_MESSAGE_TIMEOUT", consumerTimeout.getValue()); //$NON-NLS-1$
                }
            }

            // Set the new RESET_OFFSET to true if and only if the old AUTO_OFFSET_RESET property was "smallest".
            ElementParameterType newConsumerOffsetStartingPoint = ComponentUtilities.getNodeProperty(node, "AUTO_OFFSET_RESET"); //$NON-NLS-1$
            if (newConsumerOffsetStartingPoint != null && newConsumerOffsetStartingPoint.getValue() != null) {
                ComponentUtilities.addNodeProperty(node, "RESET_OFFSET", "CHECK");//$NON-NLS-1$ //$NON-NLS-2$
                ComponentUtilities.setNodeValue(node, "RESET_OFFSET", //$NON-NLS-1$
                        String.valueOf("SMALLEST".equals(newConsumerOffsetStartingPoint.getValue()))); //$NON-NLS-1$
            }
        }
    }
}