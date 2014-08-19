// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;

/**
 * if the old tMatchGroup is without separate mode,set "COMPUTE_GRP_QUALITY" to false. so that the output schema is same
 * as before(no contain column "GRP_QUALITY").or else,nothing to do.because default value of "COMPUTE_GRP_QUALITY" is
 * true after TDQ-9284.
 * 
 */
public class UpdateTmatchGroupQualityTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 8, 19, 0, 0, 0);
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
            IComponentFilter filter = new NameComponentFilter("tMatchGroup");
            IComponentConversion checkGIDType = new setGroupQualityTrue();
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(checkGIDType));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#getTypes()
     */
    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        ERepositoryObjectType type = ERepositoryObjectType.getType("PROCESS_MR"); //$NON-NLS-1$
        toReturn.add(ERepositoryObjectType.PROCESS);
        if (type != null) {
            toReturn.add(type);
        }
        return toReturn;
    }

    private class setGroupQualityTrue implements IComponentConversion {

        @Override
        @SuppressWarnings("unchecked")
        public void transform(NodeType node) {
            boolean hasSepratedParm = false;
            EList elementParameter = node.getElementParameter();
            for (Object object : elementParameter) {
                ElementParameterTypeImpl parameter = (ElementParameterTypeImpl) object;
                if (parameter.getName().equals("SEPARATE_OUTPUT")) { //$NON-NLS-1$

                    hasSepratedParm = parameter.getValue().equals("true"); //$NON-NLS-1$
                    break;
                }
            }

            if (ComponentUtilities.getNodeProperty(node, "COMPUTE_GRP_QUALITY") == null) { //$NON-NLS-1$
                ElementParameterType grpQualityType = TalendFileFactory.eINSTANCE.createElementParameterType();
                grpQualityType.setName("COMPUTE_GRP_QUALITY"); //$NON-NLS-1$
                grpQualityType.setField(EParameterFieldType.CHECK.getName());
                grpQualityType.setValue(hasSepratedParm ? "true" : "false"); //$NON-NLS-1$//$NON-NLS-2$
                elementParameter.add(grpQualityType);
            }

        }

    }

}
