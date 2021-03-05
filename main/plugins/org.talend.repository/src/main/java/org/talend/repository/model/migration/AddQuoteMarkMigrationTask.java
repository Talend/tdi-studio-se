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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.FileConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * Add quote marks to separator after feature 1192 added to trunk.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: AddQutoMigrationTask.java 下午01:23:38 2007-7-4 +0000 (2007-7-4) yzhang $
 *
 */
public class AddQuoteMarkMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    public ExecutionResult execute(Item item) {

        try {
            addQuote((ConnectionItem) item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA);
        return toReturn;
    }

    /**
     * Add quote marks to the separators.
     *
     * yzhang Comment method "addQuote".
     *
     * @throws PersistenceException
     */
    private void addQuote(ConnectionItem connItem) throws PersistenceException {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        boolean modified = false;

        Connection conn = connItem.getConnection();
        if (conn instanceof FileConnection) {
            FileConnection fileConnection = (FileConnection) conn;
            String old = null;

            old = fileConnection.getFieldSeparatorValue();
            if (!isWithinQuote(old)) {
                fileConnection.setFieldSeparatorValue(surroundDQuote(old));
                modified = true;
            }

            old = fileConnection.getRowSeparatorValue();
            if (!isWithinQuote(old)) {
                fileConnection.setRowSeparatorValue(surroundDQuote(old));
                modified = true;
            }

            old = fileConnection.getEscapeChar();
            if (!isWithinQuote(old)) {
                fileConnection.setEscapeChar(TalendTextUtils.addQuotes(old));
                modified = true;
            }

            old = fileConnection.getTextEnclosure();
            if (!isWithinQuote(old)) {
                fileConnection.setTextEnclosure(TalendTextUtils.addQuotes(old));
                modified = true;
            }

        }

        if (modified) {
            factory.save(connItem);
        }
    }

    /**
     * To see whehter the string is surrounded with double quote mark.
     *
     * yzhang Comment method "isWithinQuot".
     *
     * @param string
     * @return
     */
    private boolean isWithinQuote(String string) {
        if (string == null) {
            return true;
        }
        boolean isWithin = false;
        if (string.startsWith("\"") && string.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            isWithin = true;
        } else if (string.startsWith("\'") && string.endsWith("\'")) { //$NON-NLS-1$ //$NON-NLS-2$
            isWithin = true;
        }
        return isWithin;
    }

    /**
     * Surround the old string with double quote.
     *
     * yzhang Comment method "surroundQuote".
     *
     * @param old
     * @return
     */
    private String surroundDQuote(String old) {
        return "\"" + old + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    }
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
