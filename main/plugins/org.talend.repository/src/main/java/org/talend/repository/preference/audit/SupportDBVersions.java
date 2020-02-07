// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * created by hcyi on Jun 20, 2018 Detailled comment
 *
 */
public enum SupportDBVersions {
    H2_LOCAL(SupportDBUrlType.H2LOCALDEFAULTURL, "H2 Local", "H2_Local"), //$NON-NLS-1$ //$NON-NLS-2$
    H2_REMOTE(SupportDBUrlType.H2REMOTEDEFAULTURL, "H2 Remote", "H2_Remote"), //$NON-NLS-1$ //$NON-NLS-2$
    JTDS(SupportDBUrlType.MSSQLDEFAULTURL, "Open source JTDS", "JTDS"), //$NON-NLS-1$ //$NON-NLS-2$
    MSSQL_PROP(SupportDBUrlType.MSSQLDEFAULTURL, "Microsoft", "MSSQL_PROP"), //$NON-NLS-1$ //$NON-NLS-2$
    MARIADB(SupportDBUrlType.MARIADBDEFAULTURL, "MariaDB", "MARIADB"), //$NON-NLS-1$ //$NON-NLS-2$
    MYSQL_8(SupportDBUrlType.MYSQLDEFAULTURL, "MySQL 8", "MYSQL_8"), //$NON-NLS-1$ //$NON-NLS-2$
    MYSQL_5(SupportDBUrlType.MYSQLDEFAULTURL, "MySQL 5", "MYSQL_5"), //$NON-NLS-1$ //$NON-NLS-2$
    ORACLE_18(SupportDBUrlType.ORACLEDEFAULTURL, "Oracle 18 and above", "ORACLE_18"),
    ORACLE_12(SupportDBUrlType.ORACLEDEFAULTURL, "Oracle 12", "ORACLE_12"), //$NON-NLS-1$ //$NON-NLS-2$
    ORACLE_11(SupportDBUrlType.ORACLEDEFAULTURL, "Oracle 11", "ORACLE_11"), //$NON-NLS-1$ //$NON-NLS-2$
    ORACLE_10(SupportDBUrlType.ORACLEDEFAULTURL, "Oracle 10", "ORACLE_10"), //$NON-NLS-1$ //$NON-NLS-2$
    ORACLE_9(SupportDBUrlType.ORACLEDEFAULTURL, "Oracle 9", "ORACLE_9"), //$NON-NLS-1$ //$NON-NLS-2$
    ORACLE_8(SupportDBUrlType.ORACLEDEFAULTURL, "Oracle 8", "ORACLE_8"), //$NON-NLS-1$ //$NON-NLS-2$
    PSQL_V9_X(SupportDBUrlType.POSTGRESQLEFAULTURL, "v9 and later", "V9_X"),
    PSQL_PRIOR_TO_V9(SupportDBUrlType.POSTGRESQLEFAULTURL, "Prior to v9", "PRIOR_TO_V9"); //$NON-NLS-1$ //$NON-NLS-2$
    //$NON-NLS-1$ //$NON-NLS-2$


    private String versionDisplayName;

    private String versionValue;

    private SupportDBUrlType type;

    SupportDBVersions(SupportDBUrlType type, String versionDisplayName, String versionValue) {
        this.setType(type);
        this.setVersionDisplayName(versionDisplayName);
        this.setVersionValue(versionValue);
    }

    /**
     * Getter for versionDisplayName.
     *
     * @return the versionDisplayName
     */
    public String getVersionDisplayName() {
        return versionDisplayName;
    }

    /**
     * Sets the versionDisplayName.
     *
     * @param versionDisplayName the versionDisplayName to set
     */
    public void setVersionDisplayName(String versionDisplayName) {
        this.versionDisplayName = versionDisplayName;
    }

    /**
     * Getter for versionValue.
     *
     * @return the versionValue
     */
    public String getVersionValue() {
        return versionValue;
    }

    /**
     * Sets the versionValue.
     *
     * @param versionValue the versionValue to set
     */
    public void setVersionValue(String versionValue) {
        this.versionValue = versionValue;
    }

    /**
     * Getter for type.
     *
     * @return the type
     */
    public SupportDBUrlType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type to set
     */
    public void setType(SupportDBUrlType type) {
        this.type = type;
    }

    public static String getDisplayedVersion(SupportDBUrlType type, String versionValue) {
        for (SupportDBVersions version : values()) {
            if (version.getType().equals(type)) {
                if (versionValue == null || "".equals(versionValue) //$NON-NLS-1$
                        || versionValue.equals(version.getVersionValue())) {
                    return version.getVersionDisplayName();
                }
            }
        }
        return null;
    }

    public static String getVersionValue(SupportDBUrlType type, String versionDisplayName) {
        for (SupportDBVersions version : values()) {
            if (version.getType().equals(type)) {
                if (versionDisplayName == null || "".equals(versionDisplayName) //$NON-NLS-1$
                        || versionDisplayName.equals(version.getVersionDisplayName())) {
                    return version.getVersionValue();
                }
            }
        }
        return null;
    }

    public static String[] getDisplayedVersions(SupportDBUrlType type) {
        List<String> versions = new ArrayList<String>();
        for (SupportDBVersions version : values()) {
            if (version.getType().equals(type)) {
                versions.add(version.getVersionDisplayName());
            }
        }
        return versions.toArray(new String[0]);
    }

    public static String[] getDisplayedVersions(String dbType) {
        SupportDBUrlType urlType = SupportDBUrlStore.getInstance().getDBUrlType(dbType);
        return getDisplayedVersions(urlType);
    }
}
