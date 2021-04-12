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
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * jjzhou class global comment. Detailled comment
 */
public class SetReadByJSONPathToFalse extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        try {
            setReadByXPathToFalse(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }


    private void setReadByXPathToFalse(Item item) throws Exception {
        ProcessType processType = getProcessType(item);
        IComponentConversion addReadByXPath = new AddReadByXPath();
        IComponentFilter filter = new NameComponentFilter("tFileInputJSON");
        ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(addReadByXPath));
    }

    /**
     *
     * AddTrimOption class global comment. Detailled comment
     */
    private class AddReadByXPath implements IComponentConversion {

        private String field = "CHECK"; //$NON-NLS-1$

        private String name = "READBYXPATH"; //$NON-NLS-1$

        public AddReadByXPath() {
            super();
        }

        public void transform(NodeType node) {

            if (ComponentUtilities.getNodeProperty(node, name) == null) {
                ComponentUtilities.addNodeProperty(node, name, field);
                ComponentUtilities.setNodeValue(node, name, "false");
            }

        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 00, 30, 19, 0, 0);
        return gc.getTime();
    }
}
