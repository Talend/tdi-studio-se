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

package org.talend.designer.core.ui.editor.process;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.BRMSConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.DelimitedFileConnectionItem;
import org.talend.core.model.properties.EDIFACTConnectionItem;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.properties.ExcelFileConnectionItem;
import org.talend.core.model.properties.FTPConnectionItem;
import org.talend.core.model.properties.GenericSchemaConnectionItem;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LDAPSchemaConnectionItem;
import org.talend.core.model.properties.LdifFileConnectionItem;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.PositionalFileConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RegExFileConnectionItem;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.properties.SalesforceSchemaConnectionItem;
import org.talend.core.model.properties.WSDLSchemaConnectionItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.properties.impl.ConnectionItemImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.IComponentName;

/**
 * DOC bqian TalendEditor class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 * @deprecated use extension point in class RepositoryComponentManager
 */
public enum EDatabaseComponentName implements IComponentName {

    // DATABASES
    DBIBMDB2ZOS(DatabaseConnectionItem.class, EDatabaseTypeName.IBMDB2ZOS, "tDB2Input", "tDB2Output", true), //$NON-NLS-1$ //$NON-NLS-2$
    DBJAVADBEMBEDED(DatabaseConnectionItem.class, EDatabaseTypeName.JAVADB_EMBEDED, "tJavaDBInput", "tJavaDBOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    // SAS(DatabaseConnectionItem.class, EDatabaseTypeName.SAS, "tSASInput", "tSASOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    HSQLDB_IN_PROGRESS(DatabaseConnectionItem.class, EDatabaseTypeName.HSQLDB_IN_PROGRESS, "tHSQLDbInput", "tHSQLDbOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    HSQLDB_WEBSERVER(DatabaseConnectionItem.class, EDatabaseTypeName.HSQLDB_WEBSERVER, "tHSQLDbInput", "tHSQLDbOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    HSQLDB_SERVER(DatabaseConnectionItem.class, EDatabaseTypeName.HSQLDB_SERVER, "tHSQLDbInput", "tHSQLDbOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    JAVADB_DERBYCLIENT(DatabaseConnectionItem.class, EDatabaseTypeName.JAVADB_DERBYCLIENT, "tJavaDBInput", "tJavaDBOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    JAVADB_JCCJDBC(DatabaseConnectionItem.class, EDatabaseTypeName.JAVADB_JCCJDBC, "tJavaDBInput", "tJavaDBOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    DBMYSQL(DatabaseConnectionItem.class, EDatabaseTypeName.MYSQL, "tMysqlInput", "tMysqlOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    AMAZON_AURORA(
                  DatabaseConnectionItem.class,
                  EDatabaseTypeName.AMAZON_AURORA,
                  "tAmazonAuroraInput", "tAmazonAuroraOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    DBPSQL(DatabaseConnectionItem.class, EDatabaseTypeName.PSQL, "tPostgresqlInput", "tPostgresqlOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    PARACCEL(DatabaseConnectionItem.class, EDatabaseTypeName.PARACCEL, "tParAccelInput", "tParAccelOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    GREENPLUM(DatabaseConnectionItem.class, EDatabaseTypeName.GREENPLUM, "tGreenplumInput", "tGreenplumOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    REDSHIFT(DatabaseConnectionItem.class, EDatabaseTypeName.REDSHIFT, "tRedshiftInput", "tRedshiftOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    REDSHIFT_SSO(DatabaseConnectionItem.class, EDatabaseTypeName.REDSHIFT_SSO, "tRedshiftInput", "tRedshiftOutput", true),
    NETEZZA(DatabaseConnectionItem.class, EDatabaseTypeName.NETEZZA, "tNetezzaInput", "tNetezzaOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    DBPLUSPSQL(DatabaseConnectionItem.class, EDatabaseTypeName.PLUSPSQL, "tPostgresPlusInput", "tPostgresPlusOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    DBORACLEFORSID(DatabaseConnectionItem.class, EDatabaseTypeName.ORACLEFORSID, "tOracleInput", "tOracleOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    DBORACLESN(DatabaseConnectionItem.class, EDatabaseTypeName.ORACLESN, "tOracleInput", "tOracleOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    DBORACLEOCI(DatabaseConnectionItem.class, EDatabaseTypeName.ORACLE_OCI, "tOracleInput", "tOracleOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    DBORACLERAC(DatabaseConnectionItem.class, EDatabaseTypeName.ORACLE_CUSTOM, "tOracleInput", "tOracleOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    DBGODBC(DatabaseConnectionItem.class, EDatabaseTypeName.GODBC, "tDBInput", "tDBOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    MSODBC(DatabaseConnectionItem.class, EDatabaseTypeName.MSODBC, "tDBInput", "tDBOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    IBMDB2(DatabaseConnectionItem.class, EDatabaseTypeName.IBMDB2, "tDB2Input", "tDB2Output", true), //$NON-NLS-1$ //$NON-NLS-2$
    SYBASEASE(DatabaseConnectionItem.class, EDatabaseTypeName.SYBASEASE, "tSybaseInput", "tSybaseOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    AS400(DatabaseConnectionItem.class, EDatabaseTypeName.AS400, "tAS400Input", "tAS400Output", true), //$NON-NLS-1$ //$NON-NLS-2$

    // General JDBC
    GENERAL_JDBC(DatabaseConnectionItem.class, EDatabaseTypeName.GENERAL_JDBC, "tJDBCInput", "tJDBCOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    // this Sybase IQ not used.
    // SYBASEIQ(DatabaseConnectionItem.class,"SYBASE", "Sybase IQ", new Boolean(false), "SYBASE"),
    MSSQLODBC(DatabaseConnectionItem.class, EDatabaseTypeName.MSSQL, "tMSSqlInput", "tMSSqlOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    // this don't use in Branch 2.0
    HSQL(DatabaseConnectionItem.class, EDatabaseTypeName.HSQLDB, "tHSQLDbInput", "tHSQLDbOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    JAVADB(DatabaseConnectionItem.class, EDatabaseTypeName.JAVADB, "tJavaDBInput", "tJavaDBOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    INGRES(DatabaseConnectionItem.class, EDatabaseTypeName.INGRES, "tIngresInput", "tIngresOutput", true), // "INGRES"), //$NON-NLS-1$ //$NON-NLS-2$
    INTERBASE(DatabaseConnectionItem.class, EDatabaseTypeName.INTERBASE, "tInterbaseInput", "tInterbaseOutput", true), // "INTERBASE" //$NON-NLS-1$ //$NON-NLS-2$
    EXASOL(DatabaseConnectionItem.class, EDatabaseTypeName.EXASOL, "tExasolInput", "tExasolOutput", true), // "INGRES"), //$NON-NLS-1$ //$NON-NLS-2$
    VECTORWISE(DatabaseConnectionItem.class, EDatabaseTypeName.VECTORWISE, "tVectorWiseInput", "tVectorWiseOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    // )
    // ,
    SQLITE(DatabaseConnectionItem.class, EDatabaseTypeName.SQLITE, "tSQLiteInput", "tSQLiteOutput", true), // "SQLITE"), //$NON-NLS-1$ //$NON-NLS-2$
    FIREBIRD(DatabaseConnectionItem.class, EDatabaseTypeName.FIREBIRD, "tFirebirdInput", "tFirebirdOutput", true), // "FIREBIRD" //$NON-NLS-1$ //$NON-NLS-2$
    // ),
    INFORMIX(DatabaseConnectionItem.class, EDatabaseTypeName.INFORMIX, "tInformixInput", "tInformixOutput", true), // "INFORMIX" //$NON-NLS-1$ //$NON-NLS-2$
    // );
    ACCESS(DatabaseConnectionItem.class, EDatabaseTypeName.ACCESS, "tAccessInput", "tAccessOutput", true), // "ACCESS"); //$NON-NLS-1$ //$NON-NLS-2$

    TERADATA(DatabaseConnectionItem.class, EDatabaseTypeName.TERADATA, "tTeradataInput", "tTeradataOutput", true), // "TERADATA" //$NON-NLS-1$ //$NON-NLS-2$
    // );
    TERADATA_TABLE(DatabaseConnectionItem.class, EDatabaseTypeName.TERADATA, "tELTTeradataInput", "tELTTeradataOutput", true), // "TERADATA" //$NON-NLS-1$ //$NON-NLS-2$
    // )
    // ;
    VERTICA(DatabaseConnectionItem.class, EDatabaseTypeName.VERTICA, "tVerticaInput", "tVerticaOutput", true), //"VERTICA" //$NON-NLS-1$ //$NON-NLS-2$
    MAXDB(DatabaseConnectionItem.class, EDatabaseTypeName.MAXDB, "tMaxDBInput", "tMaxDBOutput", true), // "MAXDB"); //$NON-NLS-1$ //$NON-NLS-2$

    SAS(DatabaseConnectionItem.class, EDatabaseTypeName.SAS, "tSasInput", "tSasOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    SAPHana(DatabaseConnectionItem.class, EDatabaseTypeName.SAPHana, "tSAPHanaInput", "tSAPHanaOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    // Changed by Marvin Wang on Sep. 3, 2012 for bug TDI-22566.
    HIVE(DatabaseConnectionItem.class, EDatabaseTypeName.HIVE, "tHiveInput", "tHiveOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    HBASE(DatabaseConnectionItem.class, EDatabaseTypeName.HBASE, "tHBaseInput", "tHBaseOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    MAPRDB(DatabaseConnectionItem.class, EDatabaseTypeName.MAPRDB, "tMapRDBInput", "tMapRDBOutput", true), //$NON-NLS-1$ //$NON-NLS-2$
    IMPALA(DatabaseConnectionItem.class, EDatabaseTypeName.IMPALA, "tImpalaInput", "tImpalaOutput", true), //$NON-NLS-1$ //$NON-NLS-2$

    // FILES
    FILEARFF(FakeFileConnectionItem.class, "tFileInputARFF", "tFileOutputARFF", "DELIMITED"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    FILEDELIMITED(DelimitedFileConnectionItem.class, "tFileInputDelimited", "tFileOutputDelimited", "DELIMITED"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    FILEEXCEL(ExcelFileConnectionItem.class, "tFileInputExcel", "tFileOutputExcel", "EXCEL"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    FILELDIF(LdifFileConnectionItem.class, "tFileInputLDIF", "tFileOutputLDIF", "LDIF"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    FILEPOSITIONAL(PositionalFileConnectionItem.class, "tFileInputPositional", "tFileOutputPositional", "POSITIONAL"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    FILEREGEX(RegExFileConnectionItem.class, "tFileInputRegex", null, "REGEX"), //$NON-NLS-1$ //$NON-NLS-2$
    FILEXML(XmlFileConnectionItem.class, "tFileInputXML", "tFileOutputXML", "XML"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    FILEXMLOUTPUT(XmlFileConnectionItem.class, "tFileInputXML", "tAdvancedFileOutputXML", "XMLOUTPUT"), //$NON-NLS-1$ //$NON-NLS-2$
    SAPFFUNCTION(SAPConnectionItem.class, "tSAPInput", "tSAPOutput", "SAP"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    SAPIDOC(SAPConnectionItem.class, "tSAPIDocInput", "tSAPIDocOutput", "SAPIDOC"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GENERICSCHEMA(GenericSchemaConnectionItem.class, (String) null, null, "GENERICSCHEMA"), //$NON-NLS-1$

    FILEEBCDIC(EbcdicConnectionItem.class, "tFileInputEBCDIC", "tFileOutputEBCDIC", "EBCDIC"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    FILEHL7(HL7ConnectionItem.class, "tHL7Input", null, "HL7"), //$NON-NLS-1$ //$NON-NLS-2$
    EDIFACT(EDIFACTConnectionItem.class, "tUN/EDIFACT", null, "UN/EDIFACT"),
    FILEMDM(MDMConnectionItem.class, "tMDMInput", "tMDMOutput", "MDM"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    FTP(FTPConnectionItem.class, "", null, "FTP"), //$NON-NLS-1$ //$NON-NLS-2$
    BRMS(BRMSConnectionItem.class, "tBRMS", "tBRMS", "BRMS"),
    LDAP(LDAPSchemaConnectionItem.class, "tLDAPInput", "tLDAPOutput", "LDAP"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    WSDL(WSDLSchemaConnectionItem.class, "tWebServiceInput", null, "WSDL"), //$NON-NLS-1$ //$NON-NLS-2$
    WEBSERVICE(WSDLSchemaConnectionItem.class, (String) null, "tWebService", "WEBSERVICE"), //$NON-NLS-1$ //$NON-NLS-2$
    SALESFORCE(SalesforceSchemaConnectionItem.class, "tSalesforceInput", "tSalesforceOutput", "SALESFORCE"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // hywang add for Rule
    Rules(RulesItem.class, "tRules", "tRules", "RULE"), //$NON-NLS-1$ //$NON-NLS-2$
    LinkRules(LinkRulesItem.class, "tRules", "tRules", "RULE"),
    // RunJob
    RunJob(ProcessItem.class, "tRunJob", "tRunJob"); //$NON-NLS-1$ //$NON-NLS-2$

    // DBORACLESN("ORACLE", "Oracle with service name", new Boolean(true), "ORACLE"),
    // DBGODBC(DatabaseConnectionItem.class,"MSODBC", "Generic ODBC", new Boolean(false), "MSODBC"),
    // DBMSODBC("Microsoft SQL (Odbc driver)", "Microsoft SQL Server (Odbc driver)", new Boolean(false), "MSODBC"),
    // DBIBMDB2("IBM DB2", "IBM DB2", new Boolean(false), "IBMDB2"),
    // DBSYBASEASE("SybaseASE", "Sybase ASE", new Boolean(false), "SYBASE"),
    //
    // // this Sybase IQ not used.
    // DBSYBASEIQ("Sybase IQ", "Sybase IQ", new Boolean(false), "SYBASE"),
    // DBMSSQL("MSSQL", "Microsoft SQL Server", new Boolean(false), "MSODBC"),
    // // this don't use in Branch 2.0
    // DBHSQL("HSQL","HSQL",new Boolean(false),"HSQLDB"),
    // DBJAVADB("JavaDB", "JavaDB", new Boolean(false), "JAVADB"),
    // DBINGRES("Ingres", "Ingres", new Boolean(false), "INGRES"), // "INGRES"),
    // DBINTERBASE("Interbase", "Interbase", new Boolean(false), "Interbase"), // "INTERBASE"),
    // DBSQLITE("SQLite", "SQLite", new Boolean(false), "SQLITE"), // "SQLITE"),
    // DBFIREBIRD("FireBird", "FireBird", new Boolean(false), "FIREBIRD"), // "FIREBIRD"),
    // DBINFORMIX("Informix", "Informix", new Boolean(true), "INFORMIX"), // "INFORMIX");
    //
    // DBACCESS("Access", "Access", new Boolean(false), "ACCESS"); // "ACCESS");
    // JAVADB_EMBEDED("JavaDB Embeded", "JavaDB Embeded", new Boolean(false), "JAVADB"),
    // JAVADB_JCCJDBC("JavaDB JCCJDBC", "JavaDB JCCJDBC", new Boolean(false), "JAVADB"),
    // JAVADB_DERBYCLIENT("JavaDB DerbyClient", "JavaDB DerbyClient", new Boolean(false), "JAVADB");

    Class clazz;

    String inputComponentName;

    String outPutComponentName;

    EDatabaseTypeName dbTypeName;

    boolean forTableItem;

    private String productName;

    public boolean isForTableItem() {
        return this.forTableItem;
    }

    /**
     * Getter for clazz.
     *
     * @return the clazz
     */
    public Class getMappingKey() {
        return this.clazz;
    }

    public String getDBType() {
        if (dbTypeName != null) {
            return dbTypeName.getDisplayName();
        }
        return null;
    }

    public String getProductName() {
        if (dbTypeName != null) {
            return "DATABASE:" + dbTypeName.getProduct(); //$NON-NLS-1$
        }

        return this.productName;
    }

    @Override
    public String getDefaultComponentName() {
        return getInputComponentName();
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Getter for inputComponentName.
     *
     * @return the inputComponentName
     */
    @Override
    public String getInputComponentName() {
        return this.inputComponentName;
    }

    /**
     * Getter for outPutComponentName.
     *
     * @return the outPutComponentName
     */
    @Override
    public String getOutPutComponentName() {
        return this.outPutComponentName;
    }

    EDatabaseComponentName(Class clazz, String inputComponentName, String outPutComponentName, String productName) {
        this.clazz = clazz;
        this.inputComponentName = inputComponentName;
        this.outPutComponentName = outPutComponentName;
        this.productName = productName;

    }

    // TODO need to be removed after implementing this feature
    //
    EDatabaseComponentName(Class clazz, String inputComponentName, String outPutComponentName) {
        this.clazz = clazz;
        this.inputComponentName = inputComponentName;
        this.outPutComponentName = outPutComponentName;
    }

    /**
     * Contructor for files.
     *
     * @param clazz
     * @param dbTypeName
     * @param inputComponentName
     * @param outPutComponentName
     */
    EDatabaseComponentName(Class clazz, EDatabaseTypeName dbTypeName, String inputComponentName, String outPutComponentName) {
        this(clazz, dbTypeName, inputComponentName, outPutComponentName, false);
    }

    /**
     * Contructor for databases.
     *
     * @param clazz
     * @param type
     * @param inputComponentName
     * @param outPutComponentName
     */
    EDatabaseComponentName(Class clazz, EDatabaseTypeName dbTypeName, String inputComponentName, String outPutComponentName,
            boolean tableItem) {
        this(clazz, inputComponentName, outPutComponentName);
        this.dbTypeName = dbTypeName;
        this.forTableItem = tableItem;
    }

    public static EDatabaseComponentName getCorrespondingComponentName(Item item, ERepositoryObjectType type) {
        if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
            for (EDatabaseComponentName typeName : EDatabaseComponentName.values()) {
                if (typeName.getMappingKey().isAssignableFrom(item.getClass())) {

                    if (typeName.getMappingKey() == DatabaseConnectionItem.class) {
                        DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                        DatabaseConnection dbConnection = (DatabaseConnection) dbItem.getConnection();
                        if (typeName.getDBType().equals(dbConnection.getDatabaseType()) && typeName.isForTableItem()) {
                            return typeName;
                        }
                    } else {
                        return typeName;
                    }
                }
            }
            return null;
        } else if (type == ERepositoryObjectType.METADATA_SAP_IDOC) {
            return EDatabaseComponentName.SAPIDOC;

        } else {
            return getCorrespondingComponentName(item, true);
        }
    }

    private static EDatabaseComponentName getCorrespondingComponentName(Item item, boolean flag) {

        for (EDatabaseComponentName typeName : EDatabaseComponentName.values()) {
            if (typeName.getMappingKey().isAssignableFrom(item.getClass())) {

                if (typeName.getMappingKey() == DatabaseConnectionItem.class) {
                    DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                    DatabaseConnection dbConnection = (DatabaseConnection) dbItem.getConnection();
                    if (typeName.getDBType().equals(dbConnection.getDatabaseType()) && flag) {
                        return typeName;
                    }
                } else {
                    return typeName;
                }
            }
        }
        return null;
    }

    public static String getProductName(Item item) {
        for (EDatabaseComponentName typeName : EDatabaseComponentName.values()) {
            if (typeName.getMappingKey().isAssignableFrom(item.getClass())) {
                return typeName.getProductName();
                // if (typeName.getMappingKey() == DatabaseConnectionItem.class) {
                // DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                // DatabaseConnection dbConnection = (DatabaseConnection) dbItem.getConnection();
                // if (typeName.getDBType().equals(dbConnection.getDatabaseType()) && flag) {
                // return typeName;
                // }
                // } else {
                // return typeName;
                // }
            }
        }
        return null;
    }

    /*******************************************************************************************************************
     *
     * DOC xye EDatabaseComponentName class global comment. Detailled comment
     */
    class FakeFileConnectionItem extends ConnectionItemImpl {

    }
}
