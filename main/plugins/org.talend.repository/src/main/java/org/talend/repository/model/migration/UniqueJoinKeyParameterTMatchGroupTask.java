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
import java.util.HashMap;
import java.util.Map;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
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
import org.talend.designer.core.model.utils.emf.talendfile.impl.TalendFileFactoryImpl;

public class UniqueJoinKeyParameterTMatchGroupTask extends AbstractJobMigrationTask {

    private final String PROPERTYKEY = "JOIN_KEY"; //$NON-NLS-1$

    private final String COMPONENTNAME1 = "tMatchGroup"; //$NON-NLS-1$

    private final String COMPONENTNAME2 = "tRecordMatching"; //$NON-NLS-1$

    private final String COMPONENTNAME3 = "tMatchGroupHadoop"; //$NON-NLS-1$

    private final String NEWCOLUMNNAME = "HANDLE_NULL"; //$NON-NLS-1$

    private final String NEWCOLUMNDEFAULEVALUE = "nullMatchNull"; //$NON-NLS-1$

    private Map<String, Integer> COMPONENTSIZE = new HashMap<String, Integer>();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 2, 28, 13, 59, 10);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        initComponentMap();
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            for (String Mapkey : COMPONENTSIZE.keySet()) {
                IComponentFilter filter = new NameComponentFilter(Mapkey);
                IComponentConversion addHandleNullColumn = new AddHandleNullColumn();
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(addHandleNullColumn));
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     * DOC zshen Comment method "initComponentMap".
     */
    private void initComponentMap() {
        COMPONENTSIZE.put(COMPONENTNAME1, 4);
        COMPONENTSIZE.put(COMPONENTNAME2, 5);
        COMPONENTSIZE.put(COMPONENTNAME3, 4);

    }

    /**
     *
     *
     * the column of HANDLE_NULL is a new column for tmatchGroup
     *
     */
    private class AddHandleNullColumn implements IComponentConversion {

        /*
         * (non-Javadoc)
         *
         * @see
         * org.talend.core.model.components.conversions.IComponentConversion#transform(org.talend.designer.core.model
         * .utils.emf.talendfile.NodeType)
         */
        public void transform(NodeType node) {
            if (ComponentUtilities.getNodeProperty(node, PROPERTYKEY) != null) {
                ElementParameterType p = ComponentUtilities.getNodeProperty(node, PROPERTYKEY);
                java.util.List<ElementValueType> joinKeys = new ArrayList<ElementValueType>();
                int insertIndex = COMPONENTSIZE.get(node.getComponentName());
                for (int i = 0; i < p.getElementValue().size(); i++) {
                    joinKeys.add((ElementValueType) p.getElementValue().get(i));
                    if (i + 1 == insertIndex) {
                        insertIndex = insertIndex + COMPONENTSIZE.get(node.getComponentName());
                        ElementValueType handleNullElemenVauleType = TalendFileFactoryImpl.eINSTANCE.createElementValueType();
                        handleNullElemenVauleType.setElementRef(NEWCOLUMNNAME);
                        handleNullElemenVauleType.setValue(NEWCOLUMNDEFAULEVALUE);
                        joinKeys.add(handleNullElemenVauleType);
                    }
                }
                p.getElementValue().clear();
                p.getElementValue().addAll(joinKeys);
            }
        }

    }

}
