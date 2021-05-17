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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC slanglois class global comment. Detailled comment Remove
 */
public class UpgradetDenormalizeMigrationTask extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            IComponentFilter filter1 = new NameComponentFilter("tDenormalize"); //$NON-NLS-1$

            IComponentConversion conversion = new IComponentConversion() {

                public void transform(NodeType node) {
                    List<ElementValueType> values = new ArrayList<ElementValueType>();
                    TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;

                    MetadataType object = (MetadataType) node.getMetadata().get(0);

                    String delimiterValue = ComponentUtilities.getNodePropertyValue(node, "DELIMITER"); //$NON-NLS-1$
                    String mergeValue = ComponentUtilities.getNodePropertyValue(node, "MERGE"); //$NON-NLS-1$

                    ElementParameterType p = ComponentUtilities.getNodeProperty(node, "GROUPS"); //$NON-NLS-1$
                    if (p != null) {
                        EList<ElementValueType> es = p.getElementValue();
                        List<String> groups = new ArrayList<String>();
                        for (ElementValueType e : es) {
                            if (e.getElementRef().equals("INPUT_COLUMN")) { //$NON-NLS-1$
                                groups.add(e.getValue());
                            }
                        }

                        for (Object o : object.getColumn()) {
                            ColumnType tagada = (ColumnType) o;
                            if (groups.contains(tagada.getName())) {
                                continue;
                            }
                            ElementValueType elementValue = fileFact.createElementValueType();
                            elementValue.setElementRef("INPUT_COLUMN"); //$NON-NLS-1$
                            elementValue.setValue(tagada.getName());
                            values.add(elementValue);

                            ElementValueType elementValue2 = fileFact.createElementValueType();
                            elementValue2.setElementRef("DELIMITER"); //$NON-NLS-1$
                            elementValue2.setValue(delimiterValue);
                            values.add(elementValue2);

                            ElementValueType elementValue3 = fileFact.createElementValueType();
                            elementValue3.setElementRef("MERGE"); //$NON-NLS-1$
                            elementValue3.setValue(mergeValue);
                            values.add(elementValue3);
                        }

                        ComponentUtilities.addNodeProperty(node, "DENORMALIZE_COLUMNS", "TABLE"); //$NON-NLS-1$ //$NON-NLS-2$
                        ComponentUtilities.setNodeProperty(node, "DENORMALIZE_COLUMNS", values); //$NON-NLS-1$
                        ComponentUtilities.removeNodeProperty(node, "DELIMITER"); //$NON-NLS-1$
                        ComponentUtilities.removeNodeProperty(node, "MERGE"); //$NON-NLS-1$
                        ComponentUtilities.removeNodeProperty(node, "GROUPS"); //$NON-NLS-1$
                    }

                }
            };

            ModifyComponentsAction.searchAndModify(item, processType, filter1, Arrays.<IComponentConversion> asList(conversion));

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
