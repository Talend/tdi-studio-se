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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
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
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.EDatabaseComponentName;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.utils.sql.ConnectionUtils;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class GuessSchemaProcess extends AbstractGuessSchemaProcess {

    private String memoSQL;

    private IComponent outputComponent;

    private Connection conn;

    private DbInfo info;

    private IProcess originalProcess;

    private static String LIB_NODE = "tLibraryLoad"; //$NON-NLS-1$

    public GuessSchemaProcess(Property property, INode node, IContext selectContext, String memoSQL, DbInfo info) {
        super(property, node, selectContext);
        this.memoSQL = memoSQL.replace("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        this.info = info;
        this.conn = info.getConn();
        this.originalProcess = null;
    }

    public GuessSchemaProcess(Property property, INode node, IContext selectContext, String memoSQL, DbInfo info,
            IProcess originalProcess) {
        super(property, node, selectContext);
        this.memoSQL = memoSQL.replace("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        this.info = info;
        this.conn = info.getConn();
        this.originalProcess = originalProcess;
    }

    @Override
    protected void buildProcess() {
        Property property = getProperty();
        Process process = null;
        process = new Process(property);
        setProcess(process);
        INode node = getNode();
        configContext(process, node);
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
        // Should also replace "/r". NMa.
        memoSQL = memoSQL.replace("\r", " ");// ISO-8859-15

        // fix for TDI-26285
        String createStatament = "conn.createStatement(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,java.sql.ResultSet.CONCUR_READ_ONLY)";//$NON-NLS-1$
        // fix for TUP-20711
        if(EDatabaseTypeName.MSSQL.getDisplayName().equals(info.getDbType())) {
        	createStatament = "conn.createStatement()";
        }
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

        if (EDatabaseTypeName.REDSHIFT.getXmlName().equals(info.getDbType())
                || EDatabaseTypeName.REDSHIFT_SSO.getXmlName().equals(info.getDbType())) {
            createStatament = "conn.createStatement()";
        }

        codeStart = systemProperty + getCodeStart(connectionNode, createStatament, fetchSize);

        codeMain = "String[] dataOneRow = new String[numbOfColumn];\r\n" + "for (int i = 1; i <= numbOfColumn; i++) {\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "    \r\n" + " try{\r\n" + "    String tempStr = rs.getString(i);\r\n" + "    dataOneRow[i-1] = tempStr;\r\n" + "      } catch (java.sql.SQLException e) {\r\n" + "}\r\n" + "}\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "csvWriter.writeNext(dataOneRow);"; //$NON-NLS-1$
        
        
        codeEnd = "nbRows++;\r\n" + "    if (nbRows > " + maximumRowsToPreview + ") break;\r\n" + "}\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "conn.close();\r\n" + "csvWriter.close();\r\n"; //$NON-NLS-1$ //$NON-NLS-2$

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
        IPath temppath = getTemppath();
        String codeStart = "java.lang.Class.forName(\"" + info.getDriverClassName() + "\");\r\n" + "String url = \"" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + info.getUrl() + "\";\r\n" + "java.sql.Connection conn = java.sql.DriverManager.getConnection(url, \"" //$NON-NLS-1$ //$NON-NLS-2$
                + info.getUsername() + "\", \"" + info.getPwd() + "\");\r\n" + "java.sql.Statement stm = " + createStatament + ";\r\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "try {\r\nstm.setFetchSize(" + fetchSize //$NON-NLS-1$
                + ");\r\n} catch (Exception e) {\r\n// Exception is thrown if db don't support, no need to catch exception here\r\n} \r\n" //$NON-NLS-1$
                + "java.sql.ResultSet rs = stm.executeQuery(" + memoSQL + ");\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "java.sql.ResultSetMetaData rsmd = rs.getMetaData();\r\n" + "int numbOfColumn = rsmd.getColumnCount();\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "\r\n" + "String fileName = (new java.io.File(\r\n" + "                    \"" + temppath //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "\")).getAbsolutePath().replace(\r\n" + "                    \"\\\\\", \"/\");\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "com.talend.csv.CSVWriter csvWriter = new com.talend.csv.CSVWriter(\r\n" //$NON-NLS-1$
                + "                    new java.io.BufferedWriter(new java.io.OutputStreamWriter(\r\n" //$NON-NLS-1$
                + "                            new java.io.FileOutputStream(\r\n" //$NON-NLS-1$
                + "                                    fileName, false),\r\n" //$NON-NLS-1$
                + "                            \"GBK\")));\r\n" + "                            \r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "csvWriter.setSeparator(';');\r\n" + "csvWriter.setQuoteStatus(com.talend.csv.CSVWriter.QuoteStatus.FORCE);\r\n" //$NON-NLS-1$ //$NON-NLS-2$
                + "int nbRows = 0;\r\n" //$NON-NLS-1$
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
        return codeStart;
    }

    @Override
    protected boolean isCheckError() {
        File previousFile = getTemppath().toFile();
        return !info.isHive() | !previousFile.exists();
    }

}
