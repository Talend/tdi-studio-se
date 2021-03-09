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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.conversions.RenameComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC shong class global comment. Detailled comment <br/>
 *
 * Rename tParseXMLRow to tExtractXMLField
 *
 */
public class RenametParseXMLRowTotExtractXMLFieldMigrationTask extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        if (getProject().getLanguage() == ECodeLanguage.JAVA && processType != null) {
            try {
                IComponentFilter filter1 = new NameComponentFilter("tParseXMLRow"); //$NON-NLS-1$
                IComponentConversion changeNodeNameConversion = new IComponentConversion() {

                    public void transform(NodeType node) {

                        ProcessType item = (ProcessType) node.eContainer();
                        for (Object o : item.getNode()) {
                            NodeType nt = (NodeType) o;
                            for (Object o1 : nt.getElementParameter()) {
                                ElementParameterType t = (ElementParameterType) o1;
                                String value = t.getValue();
                                if (value != null) {
                                    if (value.contains("tParseXMLRow")) { //$NON-NLS-1$
                                        String replaceAll = value.replaceAll("tParseXMLRow", "tExtractXMLField"); //$NON-NLS-1$ //$NON-NLS-2$
                                        t.setValue(replaceAll);
                                    }
                                }
                            }
                        }
                    }
                };
                // bug 22125
                ModifyComponentsAction.searchAndModify(item, processType, filter1,
                        Arrays.<IComponentConversion> asList(new RenameComponentConversion("tExtractXMLField")));
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 10, 20, 12, 0, 0);
        return gc.getTime();
    }
}
