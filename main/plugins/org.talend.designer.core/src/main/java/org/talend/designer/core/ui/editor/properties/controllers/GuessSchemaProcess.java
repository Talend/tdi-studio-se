// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.ProcessStreamTrashReaderUtil;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.EDatabaseComponentName;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.utils.sql.ConnectionUtils;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class GuessSchemaProcess {

    protected static final int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore()
            .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT);

    private static final String DEFAULT_JOB_NAME = "Mock_job_for_Guess_schema"; //$NON-NLS-1$

    private String memoSQL;

    private Process process;

    private Property property;

    private INode node;

    private IComponent outputComponent;

    private static final String CSV_EXT = "csv"; //$NON-NLS-1$

    private IPath outpath;

    private IPath temppath;

    private String currentProcessEncoding = "GBK"; //$NON-NLS-1$

    private IContext selectContext;

    private Connection conn;

    private DbInfo info;

    private IProcess originalProcess;

    private static String LIB_NODE = "tLibraryLoad"; //$NON-NLS-1$

    private static String TEMPFILE_APPEND_NAME = "GuessSchemaDelimitedFile"; //$NON-NLS-1$

    public GuessSchemaProcess(Property property, INode node, IContext selectContext, String memoSQL, DbInfo info) {
        this.property = property;
        this.node = node;
        this.selectContext = selectContext;
        this.memoSQL = memoSQL.replace("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        this.info = info;
        this.conn = info.getConn();
        initOutpath();
    }

    public GuessSchemaProcess(Property property, INode node, IContext selectContext, String memoSQL, DbInfo info,
            IProcess originalProcess) {
        this.property = property;
        this.node = node;
        this.selectContext = selectContext;
        this.memoSQL = memoSQL.replace("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        this.info = info;
        this.conn = info.getConn();
        this.originalProcess = originalProcess;
        initOutpath();
    }

    private void initOutpath() {
        outpath = new Path(System.getProperty("user.dir")).append("Temp"); //$NON-NLS-1$ //$NON-NLS-2$ 
    }

    private void buildProcess() {
        process = new Process(property);
        process.getContextManager().getListContext().addAll(node.getProcess().getContextManager().getListContext());
        process.getContextManager().setDefaultContext(this.selectContext);
        outputComponent = ComponentsFactoryProvider.getInstance().get(
                EDatabaseComponentName.FILEDELIMITED.getOutPutComponentName(), ComponentCategory.CATEGORY_4_DI.getName());

        // create the tLibraryLoad for the input node

        if (node.getModulesNeeded().size() > 0 && !node.getComponent().getName().equals("tRedshiftInput")) {//$NON-NLS-1$
            for (ModuleNeeded module : node.getModulesNeeded()) {
                if (module.isRequired(node.getElementParameters())) {
                    Node libNode1 = new Node(ComponentsFactoryProvider.getInstance().get(LIB_NODE,
                            ComponentCategory.CATEGORY_4_DI.getName()), process);
                    libNode1.setPropertyValue("LIBRARY", "\"" + module.getModuleName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    NodeContainer nc = process.loadNodeContainer(libNode1, false);
                    process.addNodeContainer(nc);
                }
            }
        } else { // hywang add for 9594
            if (node.getComponent().getName().equals("tJDBCInput") || node.getComponent().getName().equals("tRedshiftInput")) {
                List<String> drivers = EDatabaseVersion4Drivers.getDrivers(info.getTrueDBTypeForJDBC(), info.getDbVersion());
                String moduleNeedName = "";
                Node libNode1 = new Node(ComponentsFactoryProvider.getInstance().get(LIB_NODE,
                        ComponentCategory.CATEGORY_4_DI.getName()), process);
                if (drivers.size() > 0) {
                    // use the first driver as defalult.
                    // added for bug 13592
                    moduleNeedName = drivers.get(0).toString();
                    libNode1.setPropertyValue("LIBRARY", "\"" + moduleNeedName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                process.addNodeContainer(new NodeContainer(libNode1));
            }
        }

        INode connectionNode = null;

        IElementParameter existConnection = node.getElementParameter("USE_EXISTING_CONNECTION");
        boolean useExistConnection = (existConnection == null ? false : (Boolean) existConnection.getValue());
        if (useExistConnection) {
            IElementParameter connector = node.getElementParameter("CONNECTION");
            if (connector != null) {
                String connectorValue = connector.getValue().toString();
                for (INode generatingNode : originalProcess.getGraphicalNodes()) {
                    if (generatingNode.getUniqueName().equals(connectorValue)) {
                        connectionNode = generatingNode;
                        break;
                    }
                }
                List<? extends INode> generatingNodes = originalProcess.getGeneratingNodes();
                if (connectionNode == null) {
                    // for connection node in joblet
                    for (INode generatingNode : generatingNodes) {
                        if (generatingNode.getUniqueName().equals(connectorValue)) {
                            connectionNode = generatingNode;
                            break;
                        }
                    }
                    // for visual connection component in joblet
                    if (connectionNode == null) {
                        List<IMultipleComponentManager> multipleComponentManagers = node.getComponent()
                                .getMultipleComponentManagers();
                        for (IMultipleComponentManager manager : multipleComponentManagers) {
                            String inName = manager.getInput().getName();
                            String componentValue = connectorValue + "_" + inName;
                            for (INode gnode : generatingNodes) {
                                if (gnode.getUniqueName().equals(componentValue) && (gnode instanceof INode)) {
                                    connectionNode = gnode;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        List<ModuleNeeded> neededLibraries = new ArrayList<ModuleNeeded>();
        JavaProcessUtil.addNodeRelatedModules(process, neededLibraries, node);
        for (ModuleNeeded module : neededLibraries) {
            Node libNode1 = new Node(ComponentsFactoryProvider.getInstance().get(LIB_NODE), process);
            libNode1.setPropertyValue("LIBRARY", "\"" + module.getModuleName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            process.addNodeContainer(new NodeContainer(libNode1));
        }

        if (connectionNode != null) {
            neededLibraries = new ArrayList<ModuleNeeded>();
            JavaProcessUtil.addNodeRelatedModules(process, neededLibraries, connectionNode);
            for (ModuleNeeded module : neededLibraries) {
                Node libNode1 = new Node(ComponentsFactoryProvider.getInstance().get(LIB_NODE), process);
                libNode1.setPropertyValue("LIBRARY", "\"" + module.getModuleName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                process.addNodeContainer(new NodeContainer(libNode1));
            }
        }

        // create the tLibraryLoad for the output component which is "tFileOutputDelimited"
        for (ModuleNeeded module : outputComponent.getModulesNeeded()) {
            Node libNode2 = new Node(ComponentsFactoryProvider.getInstance().get(LIB_NODE), process);
            libNode2.setPropertyValue("LIBRARY", "\"" + module.getModuleName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            process.addNodeContainer(new NodeContainer(libNode2));
        }
        int fetchSize = maximumRowsToPreview; // for sql statement, feature 6622.
        if (maximumRowsToPreview > 1000) {
            fetchSize = 1000;
        }
        String codeStart, codeMain, codeEnd;
        temppath = buildTempCSVFilename();
        // Should also replace "/r". NMa.
        memoSQL = memoSQL.replace("\r", " ");// ISO-8859-15

        // fix for TDI-26285
        String createStatament = "conn.createStatement(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,java.sql.ResultSet.CONCUR_READ_ONLY)";//$NON-NLS-1$
        String systemProperty = "";//$NON-NLS-1$ 
        if (info.isHive()) {
            createStatament = "conn.createStatement()";//$NON-NLS-1$ 
            systemProperty = "System.setProperty(\"mapred.job.tracker\",\"" + info.getJobTracker() + "\");\r\n"//$NON-NLS-1$ //$NON-NLS-2$ 
                    + "System.setProperty(\"fs.default.name\", \"" + info.getNameNode() + "\");\r\n";//$NON-NLS-1$ //$NON-NLS-2$ 
            // only embeded hive need the following params
            if (info.getThrifturi() != null) {
                systemProperty = systemProperty + " System.setProperty(\"hive.metastore.local\", \"false\");\r\n"//$NON-NLS-1$ 
                        + " System.setProperty(\"hive.metastore.uris\", \"" + info.getThrifturi() + "\");\r\n"//$NON-NLS-1$ //$NON-NLS-2$ 
                        + "System.setProperty(\"hive.metastore.execute.setugi\", \"true\");\r\n";//$NON-NLS-1$ 
            }
        }
        // add for bug TDI-27137 by fwang on 27 August, 2013.
        EDatabaseTypeName dbType = EDatabaseTypeName.JAVADB_EMBEDED.getTypeFromDbType(info.getDbType());
        if (EDatabaseTypeName.JAVADB_EMBEDED.equals(dbType)) {
            IElementParameter dbPathElement = node.getElementParameter("DBPATH");
            if (dbPathElement != null) {
                String derbyPath = dbPathElement.getValue().toString().replace("\"", "").trim();
                systemProperty = systemProperty + "System.setProperty(\"derby.system.home\",\"" + derbyPath + "\");\r\n";
            }
        }
        // the Sqlite
        if (EDatabaseTypeName.SQLITE.getXmlName().equals(info.getDbType())) {
            createStatament = "conn.createStatement()";
        }
        // the VERTICA
        if (ConnectionUtils.isVertica(info.getUrl())) {
            createStatament = "conn.createStatement()";
        }
        if (EDatabaseTypeName.GENERAL_JDBC.getXmlName().equals(info.getDbType())
                && "com.sap.db.jdbc.Driver".equals(info.getDriverClassName())
                || EDatabaseTypeName.SAPHana.getXmlName().equals(info.getDbType())) {
            createStatament = "conn.createStatement()";
        }
        codeStart = systemProperty + getCodeStart(connectionNode, createStatament, fetchSize);

        codeMain = "String[] dataOneRow = new String[numbOfColumn];\r\n" + "for (int i = 1; i <= numbOfColumn; i++) {\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "    \r\n" + " try{\r\n" + "    String tempStr = rs.getString(i);\r\n" + "    dataOneRow[i-1] = tempStr;\r\n" + "      } catch (java.sql.SQLException e) {\r\n" + "}\r\n" + "}\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "csvWriter.writeNext(dataOneRow);"; //$NON-NLS-1$
        
        
        if(EDatabaseTypeName.REDSHIFT.getXmlName().equals(info.getDbType())){
            codeEnd = "nbRows++;\r\n" + "    if (nbRows > " + maximumRowsToPreview + ") break;\r\n" + "}\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + "conn.close();\r\n" + "csvWriter.close();\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }else{
            codeEnd = "nbRows++;\r\n" + "    if (nbRows > " + maximumRowsToPreview + ") break;\r\n" + "}\r\n"  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + "conn.close();\r\n" + "csvWriter.close();\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        IComponent component = null;
        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            component = ComponentsFactoryProvider.getInstance().get("tJavaFlex"); //$NON-NLS-1$
            break;
        case PERL:
        default:
            component = ComponentsFactoryProvider.getInstance().get("tPerlFlex"); //$NON-NLS-1$
            break;

        }

        Node flexNode = new Node(component, process);
        flexNode.setPropertyValue("CODE_START", codeStart); //$NON-NLS-1$
        flexNode.setPropertyValue("CODE_MAIN", codeMain); //$NON-NLS-1$
        flexNode.setPropertyValue("CODE_END", codeEnd); //$NON-NLS-1$

        process.addNodeContainer(new NodeContainer(flexNode));
    }
    
    private String getCodeStart(INode connectionNode, String createStatament, int fetchSize){
        String codeStart = null;
        if(EDatabaseTypeName.REDSHIFT.getXmlName().equals(info.getDbType())){
            String tableName = (String) node.getElementParameter("TABLE").getValue();
            String dbName = null;
            String schema = null;
            if(connectionNode!=null){
                if(connectionNode.getElementParameter("DBNAME")!=null){
                    dbName = (String) connectionNode.getElementParameter("DBNAME").getValue();
                }
                if(connectionNode.getElementParameter("SCHEMA_DB")!=null){
                    schema = (String) connectionNode.getElementParameter("SCHEMA_DB").getValue();
                }
            }else{
                if(node.getElementParameter("DBNAME")!=null){
                    dbName = (String) node.getElementParameter("DBNAME").getValue();
                }
                if(node.getElementParameter("SCHEMA_DB")!=null){
                    schema = (String) node.getElementParameter("SCHEMA_DB").getValue();
                }
            }
            codeStart = "java.lang.Class.forName(\"" + info.getDriverClassName() + "\");\r\n" + "String url = \"" + info.getUrl() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "\";\r\n" + "java.sql.Connection conn = java.sql.DriverManager.getConnection(url, \"" + info.getUsername() //$NON-NLS-1$ //$NON-NLS-2$
                    + "\", \"" + info.getPwd() + "\");\r\n" + "java.sql.DatabaseMetaData metaData = conn.getMetaData();\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "java.sql.ResultSet rs = metaData.getColumns(" + dbName + "," + schema + "," + tableName + ",null);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                   
                    + "\r\n" + "String fileName = (new java.io.File(\r\n" + "                    \"" + temppath //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "\")).getAbsolutePath().replace(\r\n" + "                    \"\\\\\", \"/\");\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "com.talend.csv.CSVWriter csvWriter = new com.talend.csv.CSVWriter(\r\n" //$NON-NLS-1$
                    + "                    new java.io.BufferedWriter(new java.io.OutputStreamWriter(\r\n" //$NON-NLS-1$
                    + "                            new java.io.FileOutputStream(\r\n" //$NON-NLS-1$
                    + "                                    fileName, false),\r\n" //$NON-NLS-1$
                    + "                            \"GBK\")));\r\n" + "                            \r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "csvWriter.setSeparator(';');\r\n" + "csvWriter.setQuoteStatus(com.talend.csv.CSVWriter.QuoteStatus.FORCE);\r\n" + "int nbRows = 0;\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    
                    
                    
                    + "java.util.List<String> columnNameList= new java.util.ArrayList<String>();\r\n" + "java.util.List<String> nullableList= new java.util.ArrayList<String>();\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "java.util.List<String> lengthList= new java.util.ArrayList<String>();\r\n" + "java.util.List<String> precisionList= new java.util.ArrayList<String>();\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "java.util.List<String> dbtypeList= new java.util.ArrayList<String>();\r\n" //$NON-NLS-1$
                    
                    +"while (rs.next()) {\r\n" //$NON-NLS-1$
                    +"columnNameList.add(rs.getString(\"COLUMN_NAME\"));\r\n" //$NON-NLS-1$
                    +"nullableList.add(rs.getBoolean(\"NULLABLE\") ? \"true\" : \"false\");\r\n" //$NON-NLS-1$
                    +"lengthList.add(Integer.toString(rs.getInt(\"COLUMN_SIZE\")));\r\n" //$NON-NLS-1$
                    +"precisionList.add(Integer.toString(rs.getInt(\"NUM_PREC_RADIX\")));\r\n" //$NON-NLS-1$
                    +"dbtypeList.add(rs.getString(\"TYPE_NAME\"));\r\n" //$NON-NLS-1$
                    +"}\r\n" //$NON-NLS-1$
                    
                    +"int numbOfColumn = columnNameList.size();" //$NON-NLS-1$
                    + "String[] columnNames = new String[numbOfColumn];\r\n" + "String[] nullables = new String[numbOfColumn];\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "String[] lengths = new String[numbOfColumn];\r\n" + "String[] precisions = new String[numbOfColumn];\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "String[] dbtypes = new String[numbOfColumn];\r\n" //$NON-NLS-1$
                    
                    
                    + "for(int i = 0;i<=numbOfColumn-1;i++){\r\n" + "columnNames[i] = columnNameList.get(i);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "nullables[i] = nullableList.get(i);\r\n" //$NON-NLS-1$
                    + "lengths[i] = lengthList.get(i);\r\n" //$NON-NLS-1$
                    + "precisions[i] = precisionList.get(i);" //$NON-NLS-1$
                    + "dbtypes[i] = dbtypeList.get(i);\r\n" + "}\r\n" //$NON-NLS-1$ //$NON-NLS-2$

                    + "csvWriter.writeNext(columnNames);\r\n" + "csvWriter.writeNext(nullables);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "csvWriter.writeNext(lengths);\r\n" + "csvWriter.writeNext(precisions);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "csvWriter.writeNext(dbtypes);\r\n" + "while (rs.next()) {"; //$NON-NLS-1$ //$NON-NLS-2$ 
        }else{
            codeStart = "java.lang.Class.forName(\"" + info.getDriverClassName() + "\");\r\n" + "String url = \"" + info.getUrl() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "\";\r\n" + "java.sql.Connection conn = java.sql.DriverManager.getConnection(url, \"" + info.getUsername() //$NON-NLS-1$ //$NON-NLS-2$
                    + "\", \"" + info.getPwd() + "\");\r\n" + "java.sql.Statement stm = " + createStatament + ";\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "try {\r\nstm.setFetchSize(" + fetchSize + ");\r\n} catch (Exception e) {\r\n// Exception is thrown if db don't support, no need to catch exception here\r\n} \r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "java.sql.ResultSet rs = stm.executeQuery(" + memoSQL + ");\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "java.sql.ResultSetMetaData rsmd = rs.getMetaData();\r\n" + "int numbOfColumn = rsmd.getColumnCount();\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "\r\n" + "String fileName = (new java.io.File(\r\n" + "                    \"" + temppath //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "\")).getAbsolutePath().replace(\r\n" + "                    \"\\\\\", \"/\");\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "com.talend.csv.CSVWriter csvWriter = new com.talend.csv.CSVWriter(\r\n" //$NON-NLS-1$
                    + "                    new java.io.BufferedWriter(new java.io.OutputStreamWriter(\r\n" //$NON-NLS-1$
                    + "                            new java.io.FileOutputStream(\r\n" //$NON-NLS-1$
                    + "                                    fileName, false),\r\n" //$NON-NLS-1$
                    + "                            \"GBK\")));\r\n" + "                            \r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "csvWriter.setSeparator(';');\r\n" + "csvWriter.setQuoteStatus(com.talend.csv.CSVWriter.QuoteStatus.FORCE);\r\n" + "int nbRows = 0;\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "String[] columnNames = new String[numbOfColumn];\r\n" + "String[] nullables = new String[numbOfColumn];\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "String[] lengths = new String[numbOfColumn];\r\n" + "String[] precisions = new String[numbOfColumn];\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "String[] dbtypes = new String[numbOfColumn];\r\n" //$NON-NLS-1$
                    + "for(int i = 1;i<=numbOfColumn;i++){\r\n" + "columnNames[i-1] = rsmd.getColumnName(i);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "nullables[i-1] = rsmd.isNullable(i) == 0? \"false\" : \"true\";\r\n" //$NON-NLS-1$
                    + "lengths[i-1] = Integer.toString(rsmd.getScale(i));\r\n" //$NON-NLS-1$
                    + "precisions[i-1] = Integer.toString(rsmd.getPrecision(i));" //$NON-NLS-1$
                    + "dbtypes[i-1] = rsmd.getColumnTypeName(i);\r\n" + "}\r\n" //$NON-NLS-1$ //$NON-NLS-2$

                    + "csvWriter.writeNext(columnNames);\r\n" + "csvWriter.writeNext(nullables);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "csvWriter.writeNext(lengths);\r\n" + "csvWriter.writeNext(precisions);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + "csvWriter.writeNext(dbtypes);\r\n" + "while (rs.next()) {"; //$NON-NLS-1$ //$NON-NLS-2$ 
        }
        return codeStart;
    }

    // write content to a temp .csv file
    private IPath buildTempCSVFilename() {
        String filename = outpath.lastSegment();

        if (outpath.getFileExtension() != null) {
            filename = filename.substring(0, filename.length() - outpath.getFileExtension().length());
        } else // Check if file has no suffix.
        {
            int length = filename.length();
            filename = filename.substring(0, length) + TEMPFILE_APPEND_NAME + "."; //$NON-NLS-1$
        }

        filename += CSV_EXT;
        IPath tempPath;
        tempPath = Path.fromOSString(CorePlugin.getDefault().getPreferenceStore()
                .getString(ITalendCorePrefConstants.FILE_PATH_TEMP));
        tempPath = tempPath.append(filename);

        return tempPath;
    }

    public CsvArray run() throws ProcessorException {

        CsvArray array = new CsvArray();
        buildProcess();
        IProcessor processor = ProcessorUtilities.getProcessor(process, null);
        processor.setContext(selectContext);
        File previousFile = temppath.toFile();
        if (previousFile.exists()) {
            previousFile.delete();
        }
        java.lang.Process executeprocess = processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
        StringBuffer buffer = new StringBuffer();
        ProcessStreamTrashReaderUtil.readAndForget(executeprocess, buffer);
        final String errorMessage = buffer.toString();
        boolean checkError = !info.isHive() | !previousFile.exists();
        if (checkError && !"".equals(buffer.toString())) {
            throw new ProcessorException(errorMessage) {

                private static final long serialVersionUID = 1L;

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Throwable#initCause(java.lang.Throwable)
                 */
                @Override
                public synchronized Throwable initCause(Throwable cause) {
                    // TODO Auto-generated method stub
                    return super.initCause(cause);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Throwable#getMessage()
                 */
                @Override
                public String getMessage() {
                    StringTokenizer tokenizer = new StringTokenizer(errorMessage, "\n");
                    StringBuilder sqlError = new StringBuilder();
                    if (tokenizer.countTokens() > 2) {
                        tokenizer.nextToken();
                        sqlError.append(tokenizer.nextToken()).append("\n");
                    }
                    return sqlError.toString();
                }

            };
        } else {
            try {
                array = array.createFrom(previousFile, currentProcessEncoding);
            } catch (IOException ioe) {
                throw new ProcessorException(ioe) {

                    /*
                     * (non-Javadoc)
                     * 
                     * @see java.lang.Throwable#getMessage()
                     */
                    @Override
                    public String getMessage() {
                        return Messages.getString("GuessSchemaController.0", System.getProperty("line.separator")); //$NON-NLS-1$ //$NON-NLS-2$                        
                    }
                };
            }
        }
        return array;
    }

    public static Property getNewmockProperty() {

        Property mockProperty = PropertiesFactory.eINSTANCE.createProperty();
        mockProperty.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getUser());
        mockProperty.setVersion(VersionUtils.DEFAULT_VERSION);
        mockProperty.setStatusCode(""); //$NON-NLS-1$
        mockProperty.setId("ID"); //$NON-NLS-1$
        mockProperty.setLabel(DEFAULT_JOB_NAME);

        return mockProperty;
    }

}
