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
package org.talend.repository.ui.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;

/**
 * @author ocarbone
 * @deprecated by cli
 */
@Deprecated
public class DataStringConnection {

    private final DataConnection[] dataConnection;

    private final String[] defaultTable;

    public static String mySQlDefaultValue = "noDatetimeStringSync=true"; //$NON-NLS-1$

    public static String as400DefaultValue = "prompt=false"; //$NON-NLS-1$

    public final static int DBTYPE_GENERIC_ODBC = 4;

    public final static int DBTYPE_MICORSOFT_SQL_SERVER_ODBC_DRIVER = 5;

    public final static int DBTYPE_SQLITE = 8;

    public final static int DBTYPE_INTERBASE = 10;

    public final static int DBTYPE_FIREBIRD = 12;

    public final static int DBTYPE_INFORMIX = 13;

    public final static int DBTYPE_ACCESS = 14;

    public final static int DBTYPE_TERDATA = 15;

    public final static int DBTYPE_AS400 = 16;

    public final static int DBTYPE_JAVADB_EMBEDED = 17;

    public final static int DBTYPE_HSQLDB_IN_PROCESS = 22;

    public final static String GENERAL_JDBC = "General JDBC"; //$NON-NLS-1$

    public final static String EXASOL = "Exasol"; //$NON-NLS-1$

    private String dbVersion = null;

    // private Combo combo;

    public String getDbVersion() {
        return this.dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    private int selectionIndex;

    private String urlConnectionStr;

    public DataStringConnection() {
        String host = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.|[\\w\\.\\-_]{0,})"; //$NON-NLS-1$
        String port = "(\\d{0,5})"; //$NON-NLS-1$
        String word = "([\\w\\.\\-_]{0,})"; //$NON-NLS-1$
        String sid = "([\\w\\.\\-_]{0,})"; //$NON-NLS-1$
        String fileMdb = "([\\w\\.\\-_]{0,}).mdb"; //$NON-NLS-1$
        String file = "([\\w\\.\\-_]{0,})"; //$NON-NLS-1$
        String addParam = "([\\w\\.\\-_=]{0,})"; //$NON-NLS-1$
        String schema = sid;

        dataConnection = new DataConnection[31];

        defaultTable = new String[31];

        dataConnection[0] = new DataConnection(
                "MySQL", "jdbc:mysql://<host>:<port>/<sid>?<property>", "jdbc:mysql://" + host + ":" + port //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        + "/" + sid + "\\?" + addParam, "3306"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[1] = new DataConnection("PostgreSQL", "jdbc:postgresql://<host>:<port>/<sid>", "jdbc:postgresql://" + host //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ":" + port + "/" + sid, "5432"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[2] = new DataConnection("Oracle with SID", "jdbc:oracle:thin:@<host>:<port>:<sid>", "jdbc:oracle:thin:@" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + host + ":" + port + ":" + sid, "1521"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[3] = new DataConnection(
                "Oracle with service name", //$NON-NLS-1$
                "jdbc:oracle:thin:@(description=(address=(protocol=tcp)(host=<host>)(port=<port>))(connect_data=(service_name=<service_name>)))", //$NON-NLS-1$
                "jdbc:oracle:thin:@\\(description=\\(address=\\(protocol=tcp\\)\\(host=" + host + "\\)\\(port=" + port //$NON-NLS-1$ //$NON-NLS-2$
                        + "\\)\\)\\(connect_data=\\(service_name=" + sid + "\\)\\)\\)", "1521"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[DBTYPE_GENERIC_ODBC] = new DataConnection("Generic ODBC", "jdbc:odbc:<datasource>", "jdbc:odbc:" + word); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[DBTYPE_MICORSOFT_SQL_SERVER_ODBC_DRIVER] = new DataConnection(
                "Microsoft SQL Server (Odbc driver)", "jdbc:odbc:<datasource>", "jdbc:odbc:" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        + word);

        dataConnection[6] = new DataConnection(
                "Sybase ASE", "jdbc:sybase:Tds:<host>:<port>/<sid>", "jdbc:sybase:Tds:" + host + ":" + port //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        + "/" + sid, "5001"); //$NON-NLS-1$ //$NON-NLS-2$

        dataConnection[7] = new DataConnection("IBM DB2", "jdbc:db2://<host>:<port>/<sid>", "jdbc:db2://" + host + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ":" + port + "/" + sid, "50000"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[DBTYPE_SQLITE] = new DataConnection("SQLite", "jdbc:sqlite:/<filename>", "jdbc:sqlite:/" + file); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[9] = new DataConnection("Ingres", "jdbc:ingres://<host>:<port>/<sid>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:ingres://" + host + ":" + port + "/" + sid, "II7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[DBTYPE_INTERBASE] = new DataConnection("Interbase", "jdbc:interbase://<host>/<sid>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:interbase://" + host + "/" + sid); //$NON-NLS-1$ //$NON-NLS-2$

        // dataConnection[11] = new DataConnection("Microsoft SQL Server", "jdbc:jtds:sqlserver://<host>:<port>/<sid>",
        // "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + sid, "1433"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        dataConnection[11] = new DataConnection("Microsoft SQL Server", "jdbc:jtds:sqlserver://<host>:<port>/<sid>;<property>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + sid + ";" + addParam, "1433"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        dataConnection[DBTYPE_FIREBIRD] = new DataConnection("FireBird", "jdbc:firebirdsql:<host>:<filename>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:firebirdsql:" + host + ":" + file); //$NON-NLS-1$ //$NON-NLS-2$

        dataConnection[DBTYPE_INFORMIX] = new DataConnection("Informix", //$NON-NLS-1$
                "jdbc:informix-sqli://<host>:<port>/<sid>:informixserver=<datasource>;<property>", //$NON-NLS-1$
                "jdbc:informix-sqli://" + host + ":" + port + "/" + sid + ":informixserver=" + word + ";" + addParam); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        dataConnection[DBTYPE_ACCESS] = new DataConnection("Access", //$NON-NLS-1$
                "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=<filename>", //$NON-NLS-1$
                "jdbc:odbc:Driver={Microsoft Access Driver \\(\\*\\.mdb, \\*\\.accdb\\)};DBQ=" + file); //$NON-NLS-1$

        dataConnection[DBTYPE_TERDATA] = new DataConnection(
                "Teradata", "jdbc:teradata://<host>/<sid>", "jdbc:teradata://" + host + "/" + sid); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        // dataConnection[16] = new DataConnection("AS400", "jdbc:as400://<host>/<sid>;prompt=false;libraries=<sid>",
        // "jdbc:as400://" + host + "/" + sid + ";prompt=false;libraries=" + sid);
        dataConnection[DBTYPE_AS400] = new DataConnection("AS400", "jdbc:as400://<host>/<sid>;libraries=<sid>;<property>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:as400://" + host + "/" + sid + ";libraries=" + sid + ";" + addParam); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[DBTYPE_JAVADB_EMBEDED] = new DataConnection("JavaDB Embeded", "jdbc:derby:<dbRootPath>", "jdbc:derby:" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + word);
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[18] = new DataConnection("JavaDB JCCJDBC", "jdbc:derby:net://<host>:<port>/<sid>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:derby:net://" + host + ":" + port + "/" + sid, "1527"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[19] = new DataConnection("JavaDB DerbyClient", "jdbc:derby://<host>:<port>/<sid>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:derby://" + host + ":" + port + "/" + sid, "1527"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[20] = new DataConnection("HSQLDB Server", "jdbc:hsqldb:hsql://<host>:<port>/<sid>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:hsqldb:hsql://" + host + ":" + port + "/" + sid, "9001"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[21] = new DataConnection("HSQLDB WebServer", "jdbc:hsqldb:http://<host>:<port>/<sid>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:hsqldb:http://" + host + ":" + port + "/" + sid, "9001"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[DBTYPE_HSQLDB_IN_PROCESS] = new DataConnection("HSQLDB In-Process", //$NON-NLS-1$
                "jdbc:hsqldb:file:<dbRootPath>/<sid>;ifexists=true", "jdbc:hsqldb:file:" + file + "/" + sid + ";ifexists=true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[23] = new DataConnection("MaxDB", "jdbc:sapdb://<host>:<port>/<sid>", //$NON-NLS-1$ //$NON-NLS-2$
                "jdbc:sapdb://" + host + ":" + port + "/" + sid, "7210"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        dataConnection[24] = new DataConnection(
                "PostgresPlus", "jdbc:postgresql://<host>:<port>/<sid>", "jdbc:postgresql://" + host //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        + ":" + port + "/" + sid, "5432"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[25] = new DataConnection("IBM DB2 ZOS", "jdbc:db2://<host>:<port>/<sid>", "jdbc:db2://" + host + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ":" + port + "/" + sid, "557"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[26] = new DataConnection("SAS", "jdbc:sasiom://<host>:<port>", "jdbc:sasiom://" + host //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ":" + port, "7070"); //$NON-NLS-1$ //$NON-NLS-2$

        dataConnection[27] = new DataConnection("Greenplum", "jdbc:postgresql://<host>:<port>/<sid>", "jdbc:postgresql://" + host //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ":" + port + "/" + sid, "5432"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        dataConnection[28] = new DataConnection("ParAccel", "jdbc:paraccel://<host>:<port>/<sid>", "jdbc:paraccel://" + host //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ":" + port + "/" + sid, "5439"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        dataConnection[29] = new DataConnection("Netezza", "jdbc:netezza://<host>:<port>/<sid>", "jdbc:netezza://" + host //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ":" + port + "/" + sid, "5480"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // General jdbc
        dataConnection[30] = new DataConnection(GENERAL_JDBC, "jdbc:xxx://<xxx>:<xxx>", "jdbc:xxx://" + host //$NON-NLS-1$ //$NON-NLS-2$
                + ":" + port, "xxxx"); //$NON-NLS-1$ //$NON-NLS-2$
        // // hshen
        //        dataConnection[31] = new DataConnection(EXASOL, "jdbc:exa:<host>:<port>:schema=<sid>", "jdbc:exa:" + host //$NON-NLS-1$ //$NON-NLS-2$
        //                + ":" + port + ":schema=" + sid, "8563"); //$NON-NLS-1$ //$NON-NLS-2$

        // dataConnection[8] = new DataConnection("Sybase IQ", "jdbc:sybase:Tds:<host>:<port>/<sid>", "jdbc:sybase:Tds:"
        // + host + ":" + port //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        // + "/" + sid, "2638"); //$NON-NLS-1$ //$NON-NLS-2$

        // TODO CAN : reactivate this Connections when PerlModule can connect with this Databases.
        // dataConnection[3] = new DataConnection("Oracle Thin", "jdbc:oracle:thin:@<host>:<port>:<sid>",
        // "jdbc:oracle:thin:@" + host + ":"
        // + port + ":" + sid, "1521");

        // dataConnection[4] = new DataConnection("Oracle Oci", "jdbc:oracle:oci:@<host>:<port>:<sid>",
        // "jdbc:oracle:oci:@" + host + ":"
        // + port + ":" + sid, "1521");

        // dataConnection[6] = new DataConnection("Sybase", "jdbc:sybase:Tds:<host>:<port>/<sid>", "jdbc:sybase:Tds:" +
        // host + ":" + port
        // + "/" + sid, "2048");

        // dataConnection[8] = new DataConnection("Microsoft Access",
        // "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=<filename>;UID=%<login>%;PWD=%<password>%",
        // "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + fileMdb + ";UID=%" + word + ";PWD=%" + word);
    }

    public String[] getItem() {
        String[] s = defaultTable;
        for (int i = 0; i < dataConnection.length; i++) {
            s[i] = new String(dataConnection[i].getLabel());
        }
        return s;
    }

    /**
     * DOC wzhang Comment method "getDBTypes". Extract method form addDBSelectCombo()
     */
    public List<String> getDBTypes() {
        List<String> databaseType = new ArrayList<String>(Arrays.asList(getItem()));
        if (LanguageManager.getCurrentLanguage() == ECodeLanguage.PERL) {
            databaseType.remove("Microsoft SQL Server"); //$NON-NLS-1$
            databaseType.remove("Ingres"); //$NON-NLS-1$
            databaseType.remove("Interbase"); //$NON-NLS-1$
            databaseType.remove("FireBird"); //$NON-NLS-1$
            databaseType.remove("Informix"); //$NON-NLS-1$
            databaseType.remove("Access"); //$NON-NLS-1$
            databaseType.remove("Teradata"); //$NON-NLS-1$
            databaseType.remove("AS400"); //$NON-NLS-1$

            databaseType.remove("JavaDB Embeded"); //$NON-NLS-1$
            databaseType.remove("JavaDB JCCJDBC"); //$NON-NLS-1$
            databaseType.remove("JavaDB DerbyClient"); //$NON-NLS-1$

            databaseType.remove("HSQLDB Server"); //$NON-NLS-1$
            databaseType.remove("HSQLDB WebServer"); //$NON-NLS-1$
            databaseType.remove("HSQLDB In-Process"); //$NON-NLS-1$
        }
        return databaseType;
    }

    /**
     * SetVisible fields : Serveur, Login, Password.
     *
     * @param int dbTypeItemIndex
     * @param String host
     * @param String login
     * @param String password
     * @param String port
     * @param String sid
     * @param String filename
     * @param String datasource
     *
     * @return string
     */
    public String getString(final int dbTypeItemIndex, final String host, final String login, final String password,
            final String port, final String sid, final String filename, final String datasource) {
        this.selectionIndex = dbTypeItemIndex;
        String s = getStringConnectionTemplate();
        if (s == null) {
            return ""; //$NON-NLS-1$
        }
        s = getStringReplace(s, "<login>", login); //$NON-NLS-1$
        s = getStringReplace(s, "<password>", password); //$NON-NLS-1$
        s = getStringReplace(s, "<host>", host); //$NON-NLS-1$
        s = getStringReplace(s, "<login>", login); //$NON-NLS-1$
        s = getStringReplace(s, "<password>", password); //$NON-NLS-1$
        s = getStringReplace(s, "<port>", port); //$NON-NLS-1$
        s = getStringReplace(s, "<sid>", sid); //$NON-NLS-1$
        s = getStringReplace(s, "<service_name>", sid); //$NON-NLS-1$
        s = getStringReplace(s, "<datasource>", datasource); //$NON-NLS-1$
        // PTODO OCA : if needed, adapt the file separator to all OS (not only backslashes)
        s = getStringReplace(s, "<filename>", filename); //$NON-NLS-1$
        this.urlConnectionStr = s;
        return s;
    }

    public String getStringConnectionTemplate() {
        if (selectionIndex < 0) {
            return null;
        } else {
            DataConnection tmp = dataConnection[selectionIndex];
            if (dbVersion != null) { // PTODO need refactor later.
                if (DBTYPE_ACCESS == selectionIndex) {
                    if (dbVersion.equals("Access 2003")) { //$NON-NLS-1$
                        String str = tmp.getString();
                        return str.replaceFirst(",\\s\\*\\.accdb", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            return tmp.getString();
        }
    }

    public String getRegex() {
        if (selectionIndex < 0) {
            return null;
        } else {
            DataConnection tmp = dataConnection[selectionIndex];
            if (dbVersion != null) { // PTODO need refactor later.
                if (DBTYPE_ACCESS == selectionIndex) {
                    if (dbVersion.equals("Access 2003")) { //$NON-NLS-1$
                        String str = tmp.getRegex();
                        return str.replaceFirst(",\\s\\*\\.accdb", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            return tmp.getRegex();
        }
    }

    public String getDefaultPort() {
        if (selectionIndex < 0) {
            return null;
        } else {
            return dataConnection[selectionIndex].getDefaultPort();
        }
    }

    public String getStringReplace(final String init, final String before, final String after) {
        String s = init;

        if (after != null) {
            // s = init.replaceFirst(before, after);
            s = init.replace(before, after);
        }
        return s;
    }

    /**
     * Method getAnalyse extact serveur, port, sid of stringConnection and check the dbType.
     *
     * @param stringConnection
     * @return string[] { selectionIndex, serveur, port, sid }
     */
    public String[] getAnalyse(final String stringConnection) {
        Integer selectionIndex = getSelectionIndex();
        String[] s = { selectionIndex.toString(), "", "", "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        String regex = getRegex();
        if (stringConnection == "") { //$NON-NLS-1$
            return s;
        }

        Perl5Compiler compiler = new Perl5Compiler();
        Perl5Matcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        try {
            pattern = compiler.compile(regex);
            if (matcher.contains(stringConnection, pattern)) {
                matcher.matches(stringConnection, pattern);
                MatchResult matchResult = matcher.getMatch();
                s[0] = selectionIndex.toString();
                if (matchResult != null) {
                    for (int i = 1; i < matchResult.groups(); i++) {
                        s[i] = matchResult.group(i);
                    }
                }
            } else {
                // search if another regex corresponding at the string of connection
                int i = searchGoodRegex(stringConnection);
                if (i != getSelectionIndex()) {
                    setSelectionIndex(i);
                    s = getAnalyse(stringConnection);
                }
            }

        } catch (MalformedPatternException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return s;
    }

    /**
     * method searchGoodRegex search the regex corresponding at the string of connection.
     *
     * @param stringConnection
     * @return selectionIndex
     */
    private int searchGoodRegex(String stringConnection) {
        String startStringConnection;
        String startTemplateString;
        for (int i = 0; i < dataConnection.length; i++) {
            startTemplateString = dataConnection[i].getString().substring(0, dataConnection[i].getString().indexOf("<")); //$NON-NLS-1$
            if (startTemplateString.length() <= stringConnection.length()) {
                startStringConnection = stringConnection.substring(0, startTemplateString.length());
                if (stringConnection.contains("(description=(address=(protocol=tcp)")) { //$NON-NLS-1$
                    return 3;
                } else if (startTemplateString.equals(startStringConnection)) {
                    return i;
                }
            }
        }
        return getSelectionIndex();
    }

    /**
     * method getIndexOfLabel return the index of a Connection String label.
     *
     * @param string label
     */
    public int getIndexOfLabel(String label) {
        // for (int i = 0; i < dataConnection.length; i++) {
        // if (dataConnection[i].getLabel().equals(label)) {
        // return i;
        // }
        // }
        // return -1;
        return getDbIndex(label);
    }

    private int getDbIndex(String dbtype) {
        List<String> types = getDBTypes();
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            if (type.equals(dbtype)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * set selectionIndex (the index of the StringConnection in the Array DataConnection).
     *
     * @param selectionIndex
     */
    public void setSelectionIndex(int selectionIndex) {
        this.selectionIndex = selectionIndex;
    }

    /**
     * set gelectionIndex (the index of the StringConnection in the Array DataConnection).
     *
     * @return selectionIndex
     */
    public int getSelectionIndex() {
        return selectionIndex;
    }

    /**
     * Return true when the String Connection contains "jdbc:oracle:".
     *
     * @return
     */
    public boolean isSchemaNeeded() {
        if (selectionIndex < 0) {
            return false;

        }
        return getStringConnectionTemplate().substring(0, 12).equals("jdbc:oracle:") //$NON-NLS-1$
                || getStringConnectionTemplate().substring(0, 15).equals("jdbc:postgresql") //$NON-NLS-1$
                || getStringConnectionTemplate().startsWith("jdbc:paraccel") //$NON-NLS-1$
                || getStringConnectionTemplate().substring(0, 8).equals("jdbc:db2") //$NON-NLS-1$
                || getStringConnectionTemplate().substring(0, 11).equals("jdbc:sybase");//$NON-NLS-1$
    }

    /**
     * Return true when the String Connection contains "jdbc:jtds:sqlserver".
     *
     * @return
     */
    public boolean isAddtionParamsNeeded() {
        if (selectionIndex < 0) {
            return false;
        }
        return getStringConnectionTemplate().substring(0, 19).equals("jdbc:jtds:sqlserver") //$NON-NLS-1$
                || getStringConnectionTemplate().substring(0, 18).equals("jdbc:informix-sqli") //$NON-NLS-1$
                || getStringConnectionTemplate().substring(0, 10).equals("jdbc:mysql") //$NON-NLS-1$
                || getStringConnectionTemplate().substring(0, 10).equals("jdbc:as400"); //$NON-NLS-1$

    }

    private static String[] dataBaseNeededList = { "jdbc:mysql", "jdbc:sybase", "jdbc:db2", "jdbc:ingres", "jdbc:interbase", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "jdbc:jtds:sqlserver", "jdbc:informix-sqli", "jdbc:teradata", "jdbc:as400", "jdbc:derby", "jdbc:derby:net", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            "jdbc:hsqldb:hsql", "jdbc:hsqldb:http", "jdbc:hsqldb:file", "jdbc:sapdb", "jdbc:postgresql", "jdbc:db2", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            "jdbc:netezza" }; //$NON-NLS-1$

    public boolean isDatabaseNeeded() {
        if (selectionIndex < 0) {
            return false;
        }
        String connectionTemplate = getStringConnectionTemplate();
        int subIndex = 0;
        for (String connection : dataBaseNeededList) {
            subIndex = connection.length();
            boolean needed = connectionTemplate.substring(0, subIndex).equals(connection);
            if (needed) {
                return needed;
            }
        }
        return false;

    }

    /**
     * DOC qiang.zhang Comment method "getString".
     *
     * @param selectionIndex2
     * @param text
     * @param text2
     * @param text3
     * @param text4
     * @param text5
     * @param lowerCase
     * @param text6
     * @param text7
     * @return
     */
    public String getString(final int dbTypeItemIndex, final String host, final String login, final String password,
            final String port, final String sid, final String filename, final String datasource, String dbrootPath) {
        String string = getString(dbTypeItemIndex, host, login, password, port, sid, filename, datasource);
        if (string.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        if (dbTypeItemIndex == 22) {
            string = getStringReplace(string, "<dbRootPath>", dbrootPath); //$NON-NLS-1$
        } else {
            string = getStringReplace(string, "<dbRootPath>", sid); //$NON-NLS-1$
        }
        this.urlConnectionStr = string;
        return string;
    }

    public String getString(final int dbTypeItemIndex, final String host, final String login, final String password,
            final String port, final String sid, final String filename, final String datasource, String dbrootPath,
            String addParams, String... dbVersion) {
        String string = getString(dbTypeItemIndex, host, login, password, port, sid, filename, datasource);
        if (string.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        if (dbTypeItemIndex == 22) {
            string = getStringReplace(string, "<dbRootPath>", dbrootPath); //$NON-NLS-1$
        } else {
            string = getStringReplace(string, "<dbRootPath>", sid); //$NON-NLS-1$
        }
        if (dbTypeItemIndex == 11 || dbTypeItemIndex == 13 || dbTypeItemIndex == 0 || dbTypeItemIndex == 16) {
            string = getStringReplace(string, "<property>", addParams); //$NON-NLS-1$
        }

        // if (dbTypeItemIndex == 31 && schema != null && schema.length > 0) {
        //            string = getStringReplace(string, "<schemaname>", schema[0]); //$NON-NLS-1$
        // }
        this.urlConnectionStr = string;
        return string;
    }

    public String getUrlConnectionStr() {
        return this.urlConnectionStr;
    }

    /**
     *
     * DOC wzhang Comment method "getUrlConnectionStr".
     *
     * @param conn
     * @return
     */
    public String getUrlConnectionStr(DatabaseConnection conn) {
        if (conn == null || conn.getDatabaseType() == null) {
            return null;
        }
        DataStringConnection dataStrConn = new DataStringConnection();
        dataStrConn.getString(getDBTypes().indexOf(conn.getDatabaseType()), conn.getServerName(), conn.getUsername(),
                conn.getRawPassword(), conn.getPort(), conn.getSID(), conn.getFileFieldName(), conn.getDatasourceName(),
                conn.getDBRootPath(), conn.getAdditionalParams());
        return dataStrConn.getUrlConnectionStr();
    }

}
