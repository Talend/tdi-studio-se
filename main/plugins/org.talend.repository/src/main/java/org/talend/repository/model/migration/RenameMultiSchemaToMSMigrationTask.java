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

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC wliu class global comment. Detailled comment <br/> Rename tFileInputXMLMultiSchema to tFileInputMSXML Rename<br/>
 * tFileOutputXMLMultiSchema to tFileOutputMSXML <br/> $Id: talend.epf 1 2009-02-31 17:06:40Z wliu $
 *
 */
public class RenameMultiSchemaToMSMigrationTask extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType != null && getProject().getLanguage() == ECodeLanguage.JAVA) {
            try {
                ModifyComponentsAction.searchAndRename(item, processType, "tFileInputXMLMultiSchema", "tFileInputMSXML"); //$NON-NLS-1$ //$NON-NLS-2$
                ModifyComponentsAction.searchAndRename(item, processType, "tFileOutputXMLMultiSchema", "tFileOutputMSXML"); //$NON-NLS-1$ //$NON-NLS-2$
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
        GregorianCalendar gc = new GregorianCalendar(2009, 3, 4, 10, 39, 12);
        return gc.getTime();
    }
}
