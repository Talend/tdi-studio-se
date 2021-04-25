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
import org.talend.core.model.components.conversions.RemovePropertyComponentConversion;
import org.talend.core.model.components.conversions.RenameComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.PropertyComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Use to rename tDB(Input|Output|SQLRow) into tOracle(Input|Output|Row).
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class RenametFTPToFTPGetMigrationTask extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);

        try {

            if (getProject().getLanguage().equals(ECodeLanguage.JAVA) && processType != null ) {
                IComponentConversion removePropertyComponentConversion = new RemovePropertyComponentConversion("TYPE"); //$NON-NLS-1$

                RenameComponentConversion renameComponentConversion = new RenameComponentConversion("tFTPGet"); //$NON-NLS-1$
                IComponentFilter filter1 = new PropertyComponentFilter("tFTP", "ACTION", "get"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ModifyComponentsAction.searchAndModify(item, processType, filter1, Arrays.<IComponentConversion> asList(
                        renameComponentConversion, removePropertyComponentConversion));

                renameComponentConversion.setNewName("tFTPPut"); //$NON-NLS-1$
                IComponentFilter filter2 = new PropertyComponentFilter("tFTP", "ACTION", "put"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ModifyComponentsAction.searchAndModify(item, processType,filter2, Arrays.<IComponentConversion> asList(
                        renameComponentConversion, removePropertyComponentConversion));

                renameComponentConversion.setNewName("tFTPDelete"); //$NON-NLS-1$
                IComponentFilter filter3 = new PropertyComponentFilter("tFTP", "ACTION", "delete"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ModifyComponentsAction.searchAndModify(item, processType, filter3, Arrays.<IComponentConversion> asList(
                        renameComponentConversion, removePropertyComponentConversion));

                renameComponentConversion.setNewName("tFTPRename"); //$NON-NLS-1$
                IComponentFilter filter4 = new PropertyComponentFilter("tFTP", "ACTION", "rename"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ModifyComponentsAction.searchAndModify(item, processType, filter4, Arrays.<IComponentConversion> asList(
                        renameComponentConversion, removePropertyComponentConversion));
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } else {
                // do nothing
                return ExecutionResult.NOTHING_TO_DO;
            }
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
