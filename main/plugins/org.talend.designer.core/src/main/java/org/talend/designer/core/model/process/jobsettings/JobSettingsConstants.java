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
package org.talend.designer.core.model.process.jobsettings;

/**
 * DOC ggu class global comment. Detailled comment
 */
public final class JobSettingsConstants {

    public static final String ORACLE_INPUT_SID_ALIAS = "tOracleInput_sid"; //$NON-NLS-1$

    public static final String ORACLE_INOUT_SN_ALIAS = "tOracleInput_servername"; //$NON-NLS-1$

    public static final String ORACLE_INOUT_OCI_ALIAS = "tOracleInput_oci"; //$NON-NLS-1$

    public static final String ORACLE_OUTPUT_SID_ALIAS = "tOracleOutput_sid"; //$NON-NLS-1$

    public static final String ORACLE_OUTPUT_SN_ALIAS = "tOracleOutput_servername"; //$NON-NLS-1$

    public static final String ORACLE_OUTPUT_OCI_ALIAS = "tOracleOutput_oci"; //$NON-NLS-1$

    public static final String ORACLE_OUTPUT = "tOracleOutput"; //$NON-NLS-1$

    public static final String ORACLE_INPUT = "tOracleInput"; //$NON-NLS-1$

    public static final String IMPLICIT_TCONTEXTLOAD_REGEX = "REGEX"; //$NON-NLS-1$

    /**
     *
     */
    public enum ContextLoadInfo {
        ERROR("Error"), //$NON-NLS-1$
        WARNING("Warning"), //$NON-NLS-1$
        INFO("Info"); //$NON-NLS-1$

        private String display;

        private ContextLoadInfo(String display) {
            this.display = display;
        }

        public String getDisplayName() {
            return this.display;
        }

        public String getName() {
            return this.toString();
        }

    }

    private static final String[] PERL_DB_INPUT_COMPONENTS = new String[] {
            "tDBInput", "tMysqlInput", "tDBInput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            ORACLE_INPUT_SID_ALIAS, ORACLE_INOUT_SN_ALIAS, ORACLE_INOUT_OCI_ALIAS,
            "tPostgresPlusInput", "tPostgresqlInput", "tDB2Input", "tSybaseInput", "tIngresInput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
            "tInterbaseInput", "tSQLiteInput", "tFirebirdInput", "tInformixInput", "tAccessInput", "tTeradataInput" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    private static final String[] JAVA_DB_INPUT_COMPONENTS = new String[] {
            "tJDBCInput", "tMysqlInput", "tMSSqlInput", ORACLE_INPUT_SID_ALIAS, ORACLE_INOUT_SN_ALIAS, ORACLE_INOUT_OCI_ALIAS, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "tPostgresPlusInput", "tPostgresqlInput", "tDB2Input", "tSybaseInput", "tIngresInput", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
            "tInterbaseInput", "tSQLiteInput", "tFirebirdInput", "tInformixInput", "tAccessInput", "tTeradataInput" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    public static final String[] JAVA_DB_OUTPUT_COMPONENTS = new String[] {
            "tJDBCOutput", "tMysqlOutput", "tMSSqlOutput", ORACLE_OUTPUT_SID_ALIAS, ORACLE_OUTPUT_SN_ALIAS, ORACLE_OUTPUT_OCI_ALIAS,//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "tPostgresPlusOutput", "tPostgresqlOutput", "tDB2Output", "tSybaseOutput", "tIngresOutput", "tInterbaseOutput", "tSQLiteOutput", "tFirebirdOutput", "tInformixOutput", "tAccessOutput", "tTeradataOutput" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

    private static final String[] PERL_DB_OUTPUT_COMPONENTS = new String[] {
            "tMysqlOutput", ORACLE_OUTPUT_SID_ALIAS, ORACLE_OUTPUT_SN_ALIAS, ORACLE_OUTPUT_OCI_ALIAS, //$NON-NLS-1$
            "tPostgresPlusOutput", "tPostgresqlOutput", "tDB2Output", "tSybaseOutput", "tSQLiteOutput", "tFirebirdOutput" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    public static final String[][] DB_OUTPUT_COMPONENTS = new String[][] { PERL_DB_OUTPUT_COMPONENTS, JAVA_DB_OUTPUT_COMPONENTS };

    public static final String[][] DB_INPUT_COMPONENTS = new String[][] { PERL_DB_INPUT_COMPONENTS, JAVA_DB_INPUT_COMPONENTS };

    private static final String EXTRA = "_IMPLICIT_CONTEXT"; //$NON-NLS-1$

    public static final String QUERY = "QUERY"; //$NON-NLS-1$

    public static String getExtraParameterName(final String name) {
        if (name == null) {
            return ""; //$NON-NLS-1$
        }
        return name + EXTRA;
        // return name;
    }

    public static boolean isExtraParameter(final String name) {
        if (name == null) {
            return false;
        }
        return name.endsWith(EXTRA);
    }

    public static String addBrackets(final String condition) {
        if (condition == null || "".equals(condition.trim())) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        return "(" + condition + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
