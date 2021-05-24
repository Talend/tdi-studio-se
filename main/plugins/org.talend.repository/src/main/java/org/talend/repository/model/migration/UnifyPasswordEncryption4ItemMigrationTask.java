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

import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ConnectionHelper;

/**
 * created by ggu on Aug 21, 2014 Detailled comment
 *
 */
public abstract class UnifyPasswordEncryption4ItemMigrationTask extends AbstractItemMigrationTask {

    protected ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 9, 1, 12, 0, 0);
        return gc.getTime();
    }

    protected String cleanPassword(String password) {
        return ConnectionHelper.getCleanPassword(password);
    }

}
