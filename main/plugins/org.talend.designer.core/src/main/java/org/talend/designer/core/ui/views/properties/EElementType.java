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
package org.talend.designer.core.ui.views.properties;

import org.talend.core.model.process.EComponentCategory;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public enum EElementType {

    NODE(EComponentCategory.BASIC, EComponentCategory.ADVANCED, EComponentCategory.VIEW, EComponentCategory.DOC),
    NODE_LABEL(EComponentCategory.BASIC, EComponentCategory.ADVANCED, EComponentCategory.VIEW, EComponentCategory.DOC),
    CONNECTION(EComponentCategory.BASIC, EComponentCategory.ADVANCED),
    NOTE(EComponentCategory.BASIC, EComponentCategory.ADVANCED),
    SUBJOB(EComponentCategory.BASIC, EComponentCategory.ADVANCED),
    ADVANCED_NODE(
                  EComponentCategory.BASIC,
                  EComponentCategory.ADVANCED,
                  EComponentCategory.DYNAMICS_SETTINGS,
                  EComponentCategory.VIEW,
                  EComponentCategory.DOC),
    ELT_NODE(
             EComponentCategory.BASIC,
             EComponentCategory.ADVANCED,
             EComponentCategory.DYNAMICS_SETTINGS,
             EComponentCategory.SQL_PATTERN,
             EComponentCategory.VIEW,
             EComponentCategory.DOC),
    RUN_PROCESS(
                EComponentCategory.BASICRUN,
                EComponentCategory.DEBUGRUN,
                EComponentCategory.ADVANCESETTING,
                EComponentCategory.TARGET,
                EComponentCategory.MEMORYRUN),
    SCHEDULE_4_HADOOP_NON_WINDOWS(
                                  EComponentCategory.SCHEDULE_4_HADOOP_EXECUTE_JOB,
                                  EComponentCategory.SCHEDULE_4_HADOOP_MONITORING),
    SCHEDULE_4_HADOOP_WINDOWS(EComponentCategory.SCHEDULE_4_HADOOP_EXECUTE_JOB);

    private EComponentCategory[] categories;

    private EElementType(EComponentCategory... categories) {
        this.categories = categories;
    }

    public EComponentCategory[] getCategories() {
        return categories;
    }

}
