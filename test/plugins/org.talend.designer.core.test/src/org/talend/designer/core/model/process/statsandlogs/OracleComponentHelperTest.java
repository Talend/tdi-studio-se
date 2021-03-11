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
package org.talend.designer.core.model.process.statsandlogs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class OracleComponentHelperTest {

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.statsandlogs.OracleComponentHelper#filterOracleConnectionType(java.lang.String)}
     * .
     */
    @Test
    public void testFilterOracleConnectionType() {
        assertEquals(StatsAndLogsConstants.ORACLE_WITH_SID_CONN_TYPE,
                OracleComponentHelper.filterOracleConnectionType(JobSettingsConstants.ORACLE_INPUT_SID_ALIAS));
        assertEquals(StatsAndLogsConstants.ORACLE_OCI,
                OracleComponentHelper.filterOracleConnectionType(JobSettingsConstants.ORACLE_OUTPUT_OCI_ALIAS));
        assertEquals(StatsAndLogsConstants.ORACLE_WITH_SERVICE_CONN_TYPE,
                OracleComponentHelper.filterOracleConnectionType(JobSettingsConstants.ORACLE_OUTPUT_SN_ALIAS));
        assertEquals(StatsAndLogsConstants.ORACLE_WITH_SID_CONN_TYPE,
                OracleComponentHelper.filterOracleConnectionType(JobSettingsConstants.ORACLE_OUTPUT_SID_ALIAS));
        assertEquals(StatsAndLogsConstants.ORACLE_WITH_SERVICE_CONN_TYPE,
                OracleComponentHelper.filterOracleConnectionType(JobSettingsConstants.ORACLE_INOUT_SN_ALIAS));

        assertEquals(StatsAndLogsConstants.JDBC_OUTPUT,
                OracleComponentHelper.filterOracleConnectionType(StatsAndLogsConstants.JDBC_OUTPUT));
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.statsandlogs.OracleComponentHelper#filterOracleComponentName(java.lang.String)}
     * .
     */
    @Test
    public void testFilterOracleComponentName() {
        assertEquals(JobSettingsConstants.ORACLE_OUTPUT,
                OracleComponentHelper.filterOracleComponentName(JobSettingsConstants.ORACLE_OUTPUT_OCI_ALIAS));
        assertEquals(JobSettingsConstants.ORACLE_OUTPUT,
                OracleComponentHelper.filterOracleComponentName(JobSettingsConstants.ORACLE_OUTPUT_SN_ALIAS));
        assertEquals(JobSettingsConstants.ORACLE_OUTPUT,
                OracleComponentHelper.filterOracleComponentName(JobSettingsConstants.ORACLE_OUTPUT_SID_ALIAS));

        assertEquals(JobSettingsConstants.ORACLE_INPUT,
                OracleComponentHelper.filterOracleComponentName(JobSettingsConstants.ORACLE_INOUT_SN_ALIAS));
        assertEquals(JobSettingsConstants.ORACLE_INPUT,
                OracleComponentHelper.filterOracleComponentName(JobSettingsConstants.ORACLE_INPUT_SID_ALIAS));
        assertEquals(JobSettingsConstants.ORACLE_INPUT,
                OracleComponentHelper.filterOracleComponentName(JobSettingsConstants.ORACLE_INOUT_OCI_ALIAS));
    }

}
