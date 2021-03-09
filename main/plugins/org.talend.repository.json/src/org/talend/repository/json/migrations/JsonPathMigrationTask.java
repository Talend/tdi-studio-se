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
package org.talend.repository.json.migrations;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.JSONFileConnectionItem;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;

/**
 * created by cmeng on Jul 10, 2015 Detailled comment
 *
 */
public class JsonPathMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 7, 10, 18, 27, 30);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        boolean modified = false;

        if (item instanceof JSONFileConnectionItem) {
            Connection conn = ((JSONFileConnectionItem) item).getConnection();
            if (conn instanceof JSONFileConnection) {
                ((JSONFileConnection) conn).setReadbyMode(EJsonReadbyMode.XPATH.getValue());
                modified = true;
            }
        }

        try {
            if (modified) {
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                factory.save(item, true);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

}
