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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.metadata.managment.connection.manager.DatabaseConnConstants;
import org.talend.utils.sql.ConnectionUtils;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class DbInfo {

    private String username = null;

    private String pwd = null;

    private String url = null;

    private String driverClassName = null;

    private String driverJarPath = null;

    private String dbType = null;

    private String dbVersion = null;

    private String trueDBTypeForJDBC = null;

    private String additionalParams = null;

    private Connection conn = null;

    private boolean isHive;

    private String jobTracker;

    private String nameNode;

    private String thrifturi;

    public DbInfo(String dbType, String username, String pwd, String dbVersion, String url, String additionalParams) {
        this.dbType = dbType;
        this.username = username;
        this.pwd = pwd;
        this.dbVersion = dbVersion;
        this.url = url;
        this.additionalParams = additionalParams;
        generateDriverName();
        genarateDriverJarPath();
        getConnFromNode();
        this.trueDBTypeForJDBC = getTrueDBType(driverClassName, driverJarPath);
    }

    // for hive
    public DbInfo(String dbType, String username, String pwd, String dbVersion, String url, String jobTracker, String nameNode,
            String thrifturi, String additionalParams) {
        this.dbType = dbType;
        this.username = username;
        this.pwd = pwd;
        this.dbVersion = dbVersion;
        this.url = url;
        this.additionalParams = additionalParams;
        this.jobTracker = jobTracker;
        this.nameNode = nameNode;
        this.thrifturi = thrifturi;
        this.isHive = true;
        generateDriverName();
        if (!isHive) {
            genarateDriverJarPath();
            getConnFromNode();
        }
        this.trueDBTypeForJDBC = getTrueDBType(driverClassName, driverJarPath);
    }

    public DbInfo(String dbType, String username, String pwd, String dbVersion, String url, String driverJarPath,
            String additionalParams) {
        this.dbType = dbType;
        this.username = username;
        this.pwd = pwd;
        this.dbVersion = dbVersion;
        this.url = url;
        this.driverJarPath = driverJarPath;
        this.additionalParams = additionalParams;
        generateDriverName();
        getConnFromNode();
        genarateDriverJarPath();
        this.trueDBTypeForJDBC = getTrueDBType(driverClassName, driverJarPath);

    }

    public String getTrueDBTypeForJDBC() {
        return this.trueDBTypeForJDBC;
    }

    // hywang add constructor for bug 9594
    public DbInfo(String dbType, String username, String pwd, String dbVersion, String url, String driverClassName,
            String driverJarPath, String additionalParams) {
        this.dbType = dbType;
        this.username = username;
        this.pwd = pwd;
        this.dbVersion = dbVersion;
        this.url = url;
        this.driverClassName = driverClassName;
        this.driverJarPath = driverJarPath;
        this.trueDBTypeForJDBC = dbType;
        this.additionalParams = additionalParams;
        getConnFromNode();
    }

    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public String getDriverJarPath() {
        return this.driverJarPath;
    }

    public String getDbType() {
        return this.dbType;
    }

    public String getDbVersion() {
        return this.dbVersion;
    }

    public Connection getConn() {
        return this.conn;
    }

    public String getPwd() {
        return this.pwd;
    }
    public String getadditionalParams() {
    	return this.additionalParams;
    }
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setDriverJarPath(String driverJarPath) {
        this.driverJarPath = driverJarPath;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    private void getConnFromNode() {
        DriverShim wapperDriver = null;
        ExtractMetaDataUtils extractMeta = ExtractMetaDataUtils.getInstance();
        try {
            List list = null;
            if (dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getProduct())
                    || dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getXmlName())) {
                list = extractMeta.connect(trueDBTypeForJDBC, url, username, pwd, driverClassName, driverJarPath, dbVersion,
                        additionalParams);
            } else {
                // driverJarPath set to null,to reget driverJarPath
                driverJarPath = "";
                list = extractMeta.connect(dbType, url, username, pwd, driverClassName, driverJarPath, dbVersion,
                        additionalParams);
            }
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof Connection) {
                        conn = (Connection) list.get(i);
                    }
                    if (list.get(i) instanceof DriverShim) {
                        wapperDriver = (DriverShim) list.get(i);
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        } finally {
            // bug 9162
            try {
                // if HSQLDB_IN_PROGRESS connection is not closed,HSQLDB_IN_PROGRESS can't open
                if (conn != null) {
                    ConnectionUtils.closeConnection(conn);
                }
                if (wapperDriver != null && isJavaDB()) {
                    wapperDriver.connect("jdbc:derby:;shutdown=true", null); //$NON-NLS-1$
                }
            } catch (SQLException e) {
                // exception of shutdown success. no need to catch.
            }
        }
    }

    private boolean isJavaDB() {
        if (dbType == null) {
            return false;
        }
        return dbType.equals(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName())
                || dbType.equals(EDatabaseTypeName.JAVADB_DERBYCLIENT.getDisplayName())
                || dbType.equals(EDatabaseTypeName.JAVADB_JCCJDBC.getDisplayName());
    }

    private void generateDriverName() {
        if (EDatabaseTypeName.HIVE.getDisplayName().equals(dbType)) {
            if (url.startsWith(DatabaseConnConstants.HIVE_2_URL_FORMAT)) {
                driverClassName = EDatabase4DriverClassName.HIVE2.getDriverClass();
            } else {
                driverClassName = EDatabase4DriverClassName.HIVE.getDriverClass();
            }
        } else if (EDatabaseTypeName.MYSQL.getDisplayName().equals(dbType)) {
            if (EDatabaseVersion4Drivers.MYSQL_8.getVersionValue().equals(dbVersion)) {
                driverClassName = EDatabase4DriverClassName.MYSQL8.getDriverClass();
            } else if (EDatabaseVersion4Drivers.MARIADB.getVersionValue().equals(dbVersion)) {
                driverClassName = EDatabase4DriverClassName.MARIADB.getDriverClass();
            }
            if (StringUtils.isEmpty(driverClassName)) {
                driverClassName = EDatabase4DriverClassName.MYSQL.getDriverClass();
            }
        } else if (dbType.equals(EDatabaseTypeName.VERTICA.getXmlName())) {
            if (EDatabaseVersion4Drivers.VERTICA_6.getVersionValue().equals(dbVersion)
                    || EDatabaseVersion4Drivers.VERTICA_5_1.getVersionValue().equals(dbVersion)
                    || EDatabaseVersion4Drivers.VERTICA_6_1_X.getVersionValue().equals(dbVersion)
                    || EDatabaseVersion4Drivers.VERTICA_7.getVersionValue().equals(dbVersion)
                    || EDatabaseVersion4Drivers.VERTICA_7_1_X.getVersionValue().equals(dbVersion)
                    || EDatabaseVersion4Drivers.VERTICA_9.getVersionValue().equals(dbVersion)) {
                driverClassName = EDatabase4DriverClassName.VERTICA2.getDriverClass();
            }
        } else if (EDatabaseTypeName.SYBASEASE.getDisplayName().equals(dbType)) {
            if (EDatabaseVersion4Drivers.SYBASEIQ_16.getVersionValue().equals(dbVersion)) {
                driverClassName = EDatabase4DriverClassName.SYBASEIQ_16.getDriverClass();
            } else if (EDatabaseVersion4Drivers.SYBASEASE.getVersionValue().equals(dbVersion)) {
                driverClassName = EDatabase4DriverClassName.SYBASEIQ.getDriverClass();
            }else if(EDatabaseVersion4Drivers.SYBASEIQ_16_SA.getVersionValue().equals(dbVersion)) {
            	driverClassName = EDatabase4DriverClassName.SYBASEIQ_16_SA.getDriverClass();
            }
        } else if (EDatabaseTypeName.MSSQL.getDisplayName().equals(dbType)
                && EDatabaseVersion4Drivers.MSSQL_PROP.getVersionValue().equals(dbVersion)) {
            driverClassName = EDatabase4DriverClassName.MSSQL2.getDriverClass();
        } else {
            driverClassName = ExtractMetaDataUtils.getInstance().getDriverClassByDbType(dbType);
        }
    }

    private void genarateDriverJarPath() {
        // modified by wzhang. when driver has more than one jar.
        List<String> drivers = EDatabaseVersion4Drivers.getDrivers(dbType, dbVersion);
        if (drivers.size() == 1) {
            String driverName = EDatabaseVersion4Drivers.getDriversStr(dbType, dbVersion);
            if (driverName != null) {
                driverJarPath = ExtractMetaDataUtils.getInstance().getJavaLibPath() + driverName;
            }
        } else {
            driverJarPath = null;
        }
    }

    private String getTrueDBType(String className, String driverJar) {
        return ExtractMetaDataUtils.getInstance().getDbTypeByClassNameAndDriverJar(className, driverJar);
    }

    private String getTrueDBType(String className) {
        return ExtractMetaDataUtils.getInstance().getDbTypeByClassName(className);
    }

    /**
     * Getter for isHive.
     *
     * @return the isHive
     */
    public boolean isHive() {
        return this.isHive;
    }

    /**
     * Getter for jobTracker.
     *
     * @return the jobTracker
     */
    public String getJobTracker() {
        return this.jobTracker;
    }

    /**
     * Getter for nameNode.
     *
     * @return the nameNode
     */
    public String getNameNode() {
        return this.nameNode;
    }

    /**
     * Getter for thrifturi.
     *
     * @return the thrifturi
     */
    public String getThrifturi() {
        return this.thrifturi;
    }

}
