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
package org.talend.repository.preference.audit;

/**
 *
 * created by hcyi on May 29, 2018 Detailled comment
 *
 */
public enum SupportDBUrlType {

    H2LOCALDEFAULTURL("H2_Local", //$NON-NLS-1$
                      "H2 Local", //$NON-NLS-1$
                      "localhost", //$NON-NLS-1$
                      SupportDBUrlStore.EMPTY_STRING,
                      "dbname", //$NON-NLS-1$
                      SupportDBUrlStore.EMPTY_STRING,
                      "org.h2.Driver", //$NON-NLS-1$
                      null,
                      SupportDBUrlStore.EMPTY_STRING),

    H2REMOTEDEFAULTURL("H2_Remote", //$NON-NLS-1$
                       "H2 Remote", //$NON-NLS-1$
                       "localhost", //$NON-NLS-1$
                       "8080", //$NON-NLS-1$
                       "dbname", //$NON-NLS-1$
                       SupportDBUrlStore.EMPTY_STRING,
                       "org.h2.Driver", //$NON-NLS-1$
                       null,
                       SupportDBUrlStore.EMPTY_STRING),

    MYSQLDEFAULTURL("Mysql", //$NON-NLS-1$
                    "Mysql", //$NON-NLS-1$
                    "localhost", //$NON-NLS-1$
                    "3306", //$NON-NLS-1$
                    "dbname", //$NON-NLS-1$
                    "?", //$NON-NLS-1$
                    "org.gjt.mm.mysql.Driver", //$NON-NLS-1$
                    null,
                    SupportDBUrlStore.EMPTY_STRING),
    ORACLEDEFAULTURL("Oracle", //$NON-NLS-1$
                     "Oracle", //$NON-NLS-1$
                     "localhost", //$NON-NLS-1$
                     "1521", //$NON-NLS-1$
                     "dbname", //$NON-NLS-1$
                     SupportDBUrlStore.EMPTY_STRING,
                     "oracle.jdbc.driver.OracleDriver", //$NON-NLS-1$
                     null,
                     SupportDBUrlStore.EMPTY_STRING),
    MSSQLDEFAULTURL("MSSQL", //$NON-NLS-1$
                    "MSSQL", //$NON-NLS-1$
                    "localhost", //$NON-NLS-1$
                    "1433", //$NON-NLS-1$
                    "dbname", //$NON-NLS-1$
                    SupportDBUrlStore.EMPTY_STRING,
                    "net.sourceforge.jtds.jdbc.Driver", //$NON-NLS-1$
                    null,
                    SupportDBUrlStore.EMPTY_STRING),
    MARIADBDEFAULTURL("MariaDB", //$NON-NLS-1$
                      "MariaDB", //$NON-NLS-1$
                      "localhost", //$NON-NLS-1$
                      "3306", //$NON-NLS-1$
                      "dbname", //$NON-NLS-1$
                      SupportDBUrlStore.EMPTY_STRING,
                      "org.mariadb.jdbc.Driver", //$NON-NLS-1$
                      null,
                      SupportDBUrlStore.EMPTY_STRING),
    POSTGRESQLEFAULTURL("PostgreSQL", //$NON-NLS-1$
                        "PostgreSQL", //$NON-NLS-1$
                        "localhost", //$NON-NLS-1$
                        "5432", //$NON-NLS-1$
                        "dbname", //$NON-NLS-1$
                        SupportDBUrlStore.EMPTY_STRING,
                        "org.postgresql.Driver", //$NON-NLS-1$
                        null,
                        SupportDBUrlStore.EMPTY_STRING);

    private final String dbKey;

    private final String displayName;

    private final String hostName;

    private final String port;

    private final String dbName;

    private final String paramSeprator;

    private final String dbDriver;

    private final String dialect;

    private final String dataSource;

    SupportDBUrlType(String dbKey, String displayName, String hostName, String port, String dbName, String paramSeprator,
            String dbDriver,
            String dialect, String datasource) {
        this.dbKey = dbKey;
        this.displayName = displayName;
        this.hostName = hostName;
        this.port = port;
        this.dbName = dbName;
        this.paramSeprator = paramSeprator;
        this.dbDriver = dbDriver;
        this.dialect = dialect;
        this.dataSource = datasource;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPort() {
        return port;
    }

    public String getDBName() {
        return dbName;
    }

    public String getDBKey() {
        return dbKey;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public String getDialect() {
        return dialect;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getParamSeprator() {
        return paramSeprator;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
