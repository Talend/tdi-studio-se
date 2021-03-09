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
package org.talend.designer.core.utils;

import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JobSettingVersionUtil {

    public static void setDbVersion(IElementParameter elementParameter, String value, boolean withInitialValue) {
        setDbVersion(elementParameter, value, value, withInitialValue);
    }

    public static void setDbVersion(IElementParameter elementParameter, String dbType, String dbVersion) {
        setDbVersion(elementParameter, dbType, dbVersion, false);
    }

    public static void setDbVersion(IElementParameter elementParameter, String dbType, String dbVersion, boolean withInitialValue) {
        if (elementParameter == null || dbType == null) {
            return;
        }
        if (dbType.indexOf("Access") != -1) {//$NON-NLS-1$
            if (withInitialValue) {
                elementParameter.setValue(StatsAndLogsConstants.ACCESS_VERSION_CODE[0]);
            } else {
                elementParameter.setValue(dbVersion);
            }
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.ACCESS_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.ACCESS_VERSION_CODE);
        } else if (dbType.toUpperCase().indexOf("ORACLE") != -1) {//$NON-NLS-1$
            if (withInitialValue) {
                elementParameter.setValue(StatsAndLogsConstants.ORACLE_VERSION_CODE[0]);
            } else {
                elementParameter.setValue(dbVersion);
            }
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.ORACLE_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.ORACLE_VERSION_CODE);
        } else if (dbType.toUpperCase().indexOf("AS400") != -1) {//$NON-NLS-1$
            if (withInitialValue) {
                elementParameter.setValue(StatsAndLogsConstants.AS400_VERSION_CODE[0]);
            } else {
                elementParameter.setValue(dbVersion);
            }
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.AS400_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.AS400_VERSION_CODE);
        } else if (dbType.toUpperCase().indexOf("MYSQL") != -1) {//$NON-NLS-1$
            if (withInitialValue) {
                elementParameter.setValue(StatsAndLogsConstants.MYSQL_VERSION_CODE[0]);
            } else {
                elementParameter.setValue(dbVersion);
            }
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.MYSQL_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.MYSQL_VERSION_CODE);
        } else if (dbType.toUpperCase().indexOf("MSSQL") != -1) {//$NON-NLS-1$
            if (withInitialValue) {
                elementParameter.setValue(StatsAndLogsConstants.MSSQL_VERSION_CODE[0]);
            } else {
                elementParameter.setValue(dbVersion);
            }
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.MSSQL_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.MSSQL_VERSION_CODE);
        } else if (dbType.toUpperCase().indexOf("POSTGRESQL") != -1) {//$NON-NLS-1$
            if (withInitialValue) {
                elementParameter.setValue(StatsAndLogsConstants.PSQL_VERSION_CODE[0]);
            } else {
                elementParameter.setValue(dbVersion);
            }
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.PSQL_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.PSQL_VERSION_CODE);
        }
        else if (dbType.toUpperCase().indexOf("SYBASE") != -1) {//$NON-NLS-1$
            if (withInitialValue) {
                elementParameter.setValue(StatsAndLogsConstants.SYBASE_VERSION_CODE[0]);
            } else {
                elementParameter.setValue(dbVersion);
            }
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.SYBASE_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.SYBASE_VERSION_CODE);
        }
    }

}
