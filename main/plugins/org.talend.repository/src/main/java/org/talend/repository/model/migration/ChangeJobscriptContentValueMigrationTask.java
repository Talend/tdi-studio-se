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

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobScriptItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC zwzhao class global comment. Detailled comment <br/>
 *
 */
public class ChangeJobscriptContentValueMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 3, 6, 16, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (item instanceof JobScriptItem) {
            byte[] bytes = ((JobScriptItem) item).getContent().getInnerContent();
            String message = new String(bytes);
            message = message.replaceAll("PATTREN", "PATTERN");
            try {
                ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
                byteArray.setInnerContent(message.getBytes());
                ((JobScriptItem) item).setContent(byteArray);
                factory.save(item, true);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        }
        return ExecutionResult.NOTHING_TO_DO;

    }

}
