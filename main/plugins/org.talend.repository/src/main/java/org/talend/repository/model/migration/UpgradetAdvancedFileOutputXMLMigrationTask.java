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
import org.talend.core.model.components.conversions.UpdateMappingFortAdvancedXMLConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC s class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2007-10-25 17:06:40 +0000 xzhang $
 */
public class UpgradetAdvancedFileOutputXMLMigrationTask extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null ) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            IComponentFilter filter1 = new NameComponentFilter("tAdvancedFileOutputXML"); //$NON-NLS-1$

            IComponentConversion removeQuotes1 = new UpdateMappingFortAdvancedXMLConversion(); //$NON-NLS-1$
            ModifyComponentsAction.searchAndModify(item,processType, filter1, Arrays.<IComponentConversion> asList(removeQuotes1));

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
