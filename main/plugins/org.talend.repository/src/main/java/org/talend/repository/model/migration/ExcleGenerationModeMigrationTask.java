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
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ExcelFileConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC wf class global comment. Detailled comment
 */
public class ExcleGenerationModeMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 7, 23, 17, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        FileExcelConnection fileExcelConn = null;
        if (item instanceof ExcelFileConnectionItem) {
            ExcelFileConnectionItem exelConnItem = (ExcelFileConnectionItem) item;
            if (exelConnItem.getConnection() instanceof FileExcelConnection) {
                fileExcelConn = (FileExcelConnection) exelConnItem.getConnection();
            }
        }
        if (fileExcelConn == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            boolean isXsls = false;
            String filePath = fileExcelConn.getFilePath();
            if (filePath.endsWith(".xlsx")) {
                isXsls = true;
            }
            String genMode = fileExcelConn.getGenerationMode();
            if (isXsls && genMode == null) {
                fileExcelConn.setGenerationMode("USER_MODE");
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } else {
                return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }
}
