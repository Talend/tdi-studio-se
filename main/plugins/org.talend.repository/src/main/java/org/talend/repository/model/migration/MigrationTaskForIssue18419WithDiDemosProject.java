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

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class MigrationTaskForIssue18419WithDiDemosProject extends MigrationTaskForIssue18419 {

    private static final String DI_DEMOS_PROJECT_NAME = "DI_DEMOS"; //$NON-NLS-1$

    @Override
    protected String getDemoProjectName() {
        return DI_DEMOS_PROJECT_NAME;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 7, 31, 0, 0, 0);
        return gc.getTime();
    }
}
