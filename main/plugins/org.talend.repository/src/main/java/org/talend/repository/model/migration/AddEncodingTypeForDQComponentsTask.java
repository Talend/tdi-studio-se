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
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * Migration for added the combo list of encoding type
 */
public class AddEncodingTypeForDQComponentsTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 7, 9, 0, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        try {
            IComponentFilter filter = new IComponentFilter() {

                final transient List<String> names = new ArrayList<String>() {

                    private static final long serialVersionUID = 1L;

                    {
                        add("tDataMasking"); //$NON-NLS-1$
                        add("tPatternMasking"); //$NON-NLS-1$
                        add("tPatternUnmasking"); //$NON-NLS-1$
                    }
                };

                @Override
                public boolean accept(NodeType node) {
                    return names.contains(node.getComponentName());
                }

            };
            IComponentConversion checkGIDType = new CheckGIDType();
            ModifyComponentsAction
                    .searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(checkGIDType));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

    private class CheckGIDType implements IComponentConversion {

        @Override
        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, "ENCODING") == null) { //$NON-NLS-1$
                ElementParameterType encodingContent = TalendFileFactory.eINSTANCE.createElementParameterType();
                encodingContent.setName("ENCODING"); //$NON-NLS-1$
                encodingContent.setField(EParameterFieldType.ENCODING_TYPE.getName());
                encodingContent.setValue("\"\""); //$NON-NLS-1$
                node.getElementParameter().add(encodingContent);
            }
            if (ComponentUtilities.getNodeProperty(node, "ENCODING:ENCODING_TYPE") == null) { //$NON-NLS-1$
                ElementParameterType encodingType = TalendFileFactory.eINSTANCE.createElementParameterType();
                encodingType.setName("ENCODING:ENCODING_TYPE"); //$NON-NLS-1$
                encodingType.setField(EParameterFieldType.TECHNICAL.getName());
                encodingType.setValue("CUSTOM"); //$NON-NLS-1$
                node.getElementParameter().add(encodingType);
            }
        }

    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = super.getTypes();
        // PROCESS_MR stands for Map/Reduce and Spark.
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        // PROCESS_STORM stands for Storm and Spark Streaming.
        toReturn.add(ERepositoryObjectType.PROCESS_STORM);
        return toReturn;
    }
}
