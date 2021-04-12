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
package org.talend.designer.runprocess.shadow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.repository.ResourceModelUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.preview.ExcelSchemaBean;
import org.talend.core.repository.model.preview.IProcessDescription;
import org.talend.core.repository.model.preview.SalesforceSchemaBean;
import org.talend.core.utils.CsvArray;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.JobErrorsChecker;
import org.talend.designer.runprocess.ProcessStreamTrashReader;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.repository.ProjectManager;

/**
 * Launch a Process in shadow mode. <br/>
 *
 * $Id$
 *
 * @param <T>
 *
 */
public class ShadowProcess<T extends IProcessDescription> {

    /**
     *
     */
    private static final String TEMP_LDAP_SCHEMA_FILE_NAME = "TempLDAPSchema"; //$NON-NLS-1$

    private static final String TEMP_WSDL_SCHEMA_FILE_NAME = "TempWSDLSchema"; //$NON-NLS-1$

    private static final String TEMP_SALEFORCE_SCHEMA_FILE_NAME = "TempSALESFORCESchema"; //$NON-NLS-1$

    private String currentProcessEncoding = "ISO-8859-15"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(ShadowProcess.class);

    /**
     * Available Shadow Process Types.
     *
     * $Id$
     *
     */
    public static enum EShadowProcessType {
        FILE_DELIMITED,
        FILE_POSITIONAL,
        FILE_CSV,
        FILE_REGEXP,
        FILE_XML,
        FILE_EXCEL,
        FILE_LDIF,
        WSDL_SCHEMA,
        LDAP_SCHEMA,
        SALESFORCE_SCHEMA;

        private EShadowProcessType() {

        }
    }

    // private static final String XML_EXT = "xml"; //$NON-NLS-1$

    private static final String CSV_EXT = "csv"; //$NON-NLS-1$

    private T description;

    private String inPath;

    private IPath outPath;

    private EShadowProcessType type;

    private Process process;

    private String[] proxyParameters;

    /**
     * Constructs a new ShadowProcess.
     */
    public ShadowProcess(T description, EShadowProcessType type) {
        super();

        this.description = description;
        String filePath = description.getFilepath();

        IPath tempPath = getTmpFolderPath();
        if (filePath != null) {
            this.inPath = filePath;
            this.outPath = buildTempCSVFilename(new Path(filePath));
        } else if (type.name().equals("LDAP_SCHEMA")) { //$NON-NLS-1$
            this.outPath = tempPath.append(TEMP_LDAP_SCHEMA_FILE_NAME + "." + CSV_EXT); //$NON-NLS-1$
        } else if (type.name().equals("WSDL_SCHEMA")) { //$NON-NLS-1$
            this.outPath = tempPath.append(TEMP_WSDL_SCHEMA_FILE_NAME + "." + CSV_EXT); //$NON-NLS-1$
        } else if (type.name().equals("SALESFORCE_SCHEMA")) { //$NON-NLS-1$
            this.outPath = tempPath.append(TEMP_SALEFORCE_SCHEMA_FILE_NAME + "." + CSV_EXT); //$NON-NLS-1$
        }
        this.type = type;

        SalesforceSchemaBean salesforceSchemaBean = description.getSalesforceSchemaBean();
        if (salesforceSchemaBean != null) {
            boolean uesHttp = salesforceSchemaBean.isUesHttp();
            boolean useProxy = salesforceSchemaBean.isUseProxy();

            if (uesHttp || useProxy) {
                this.proxyParameters = new String[] {};
                String proxyHost = salesforceSchemaBean.getProxyHost();
                String proxyPort = salesforceSchemaBean.getProxyPort();
                String userName = salesforceSchemaBean.getProxyUsername();
                String password = salesforceSchemaBean.getProxyPassword();
                if (uesHttp) {
                    if (proxyHost != null && userName == null && password == null) {
                        this.proxyParameters = new String[] { "-Dhttp.proxyHost=" + proxyHost, "-Dhttp.proxyPort=" + proxyPort };
                    }
                    if (proxyHost != null && userName != null && password != null) {
                        this.proxyParameters = new String[] { "-Dhttp.proxyHost=" + proxyHost, "-Dhttp.proxyPort=" + proxyPort,
                                "-Dhttp.proxyUsername=" + userName, "-Dhttp.proxyPassword=" + password };
                    }
                } else {
                    if (proxyHost != null && userName == null && password == null) {
                        this.proxyParameters = new String[] { "-DsocksProxyHost=" + proxyHost, "-DsocksProxyPort=" + proxyPort };
                    }
                    if (proxyHost != null && userName != null && password != null) {
                        this.proxyParameters = new String[] { "-DsocksProxyHost=" + proxyHost, "-Dhttp.proxyPort=" + proxyPort,
                                "-Djava.net.socks.username=" + userName, "-Djava.net.socks.password=" + password };
                    }
                }
            }
        }
    }

    private IProcess buildProcess() {
        IProcess ps = null;

        if (description.getEncoding() != null) {
            currentProcessEncoding = TalendTextUtils.removeQuotes(description.getEncoding());
        }

        FileOutputDelimitedNode outNode = new FileOutputDelimitedNode(
                TalendTextUtils.addQuotes("" + PathUtils.getPortablePath(outPath.toOSString())), description.getEncoding()); //$NON-NLS-1$
        switch (type) {

        case FILE_DELIMITED:

        case FILE_CSV:
            FileInputDelimitedNode inDelimitedNode = new FileInputDelimitedNode(inPath, description.getRowSeparator(),
                    description.getFieldSeparator(), description.getLimitRows(), description.getHeaderRow(),
                    description.getFooterRow(), description.getEscapeCharacter(), description.getTextEnclosure(),
                    description.getRemoveEmptyRowsToSkip(), description.isSplitRecord(), description.getEncoding(), type);
            ps = new FileinToDelimitedProcess<FileInputDelimitedNode>(inDelimitedNode, outNode);
            break;

        case FILE_POSITIONAL:
            FileInputPositionalNode inPositionalNode = new FileInputPositionalNode(inPath, description.getRowSeparator(),
                    description.getPattern(), description.getHeaderRow(), description.getFooterRow(), description.getLimitRows(),
                    description.getRemoveEmptyRowsToSkip(), description.getEncoding());
            outNode.setColumnNumber(inPositionalNode.getColumnNumber());
            ps = new FileinToDelimitedProcess<FileInputPositionalNode>(inPositionalNode, outNode);
            break;

        case FILE_REGEXP:
            FileInputRegExpNode inRegExpNode = new FileInputRegExpNode(inPath, description.getRowSeparator(),
                    description.getPattern(), description.getLimitRows(), description.getHeaderRow(), description.getFooterRow(),
                    description.getRemoveEmptyRowsToSkip(), description.getEncoding());
            ps = new FileinToDelimitedProcess<FileInputRegExpNode>(inRegExpNode, outNode);
            break;
        case FILE_XML:
            List<Map<String, String>> newmappings = new ArrayList<Map<String, String>>();
            List<Map<String, String>> oldmappings = description.getMapping();
            for (int i = 0; i < oldmappings.size(); i++) {
                Map<String, String> map = oldmappings.get(i);
                map.put("SCHEMA_COLUMN", "row" + i); //$NON-NLS-1$ //$NON-NLS-2$
                newmappings.add(map);
            }
            FileInputXmlNode inXmlNode = new FileInputXmlNode(inPath, description.getLoopQuery(), description.getMapping(),
                    description.getLoopLimit(), description.getEncoding());
            ps = new FileinToDelimitedProcess<FileInputXmlNode>(inXmlNode, outNode);
            break;
        case FILE_EXCEL:
            // hywang add for excel 2007
            String versionCheck = "false";
            String afterRemoveQuotesPath = TalendTextUtils.removeQuotes(inPath);
            if (afterRemoveQuotesPath.toLowerCase().endsWith(".xlsx")) {
                versionCheck = "true";
            }

            FileInputExcelNode excelNode = null;
            ExcelSchemaBean excelBean = description.getExcelSchemaBean();

            excelNode = new FileInputExcelNode(inPath,
                    description.getSchema(),
                    description.getEncoding() == null ? TalendTextUtils.addQuotes("ISO-8859-1") : description.getEncoding(), //$NON-NLS-1$
                    Integer.toString(description.getLimitRows()), Integer.toString(description.getHeaderRow()),
                    Integer.toString(description.getFooterRow()), Boolean.toString(description.getRemoveEmptyRowsToSkip()),
                    excelBean, versionCheck);

            outNode.setMetadataList(excelNode.getMetadataList());

            ps = new FileinToDelimitedProcess<FileInputExcelNode>(excelNode, outNode);
            break;
        case FILE_LDIF:
            outNode = new FileOutputDelimitedForLDIF(TalendTextUtils.addQuotes("" //$NON-NLS-1$
                    + PathUtils.getPortablePath(outPath.toOSString())), description.getEncoding());

            FileInputLdifNode inLdifNode = new FileInputLdifNode(inPath, description.getSchema(), description.getEncoding());
            outNode.setMetadataList(inLdifNode.getMetadataList());
            ps = new FileinToDelimitedProcess<FileInputLdifNode>(inLdifNode, outNode);
            break;
        case LDAP_SCHEMA:
            // outNode = new FileOutputDelimitedForLDIF(TalendTextUtils.addQuotes(""
            // + PathUtils.getPortablePath(outPath.toOSString())), description.getEncoding());

            LDAPSchemaInputNode inLDAPSchemaNode = new LDAPSchemaInputNode(TalendTextUtils.addQuotes("" //$NON-NLS-1$
                    + PathUtils.getPortablePath(outPath.toOSString())), description.getSchema(), description.getEncoding(),
                    description.getLdapSchemaBean());
            List<IMetadataTable> metadataList = inLDAPSchemaNode.getMetadataList();
            for (IMetadataTable metadataTable : metadataList) {
                // for bug15450
                List<IMetadataColumn> listColumns = metadataTable.getListColumns();
                for (IMetadataColumn metadataColumn : listColumns) {
                    String label = metadataColumn.getLabel();
                    boolean validColumnName = MetadataToolHelper.isValidColumnName(label);
                    if (!validColumnName) {
                        metadataColumn.setLabel(label.replaceAll("[^a-zA-Z0-9_]", "_"));
                    }
                }
            }
            outNode.setMetadataList(metadataList);
            ps = new FileinToDelimitedProcess<LDAPSchemaInputNode>(inLDAPSchemaNode, outNode);
            break;
        case WSDL_SCHEMA:
            // outNode = new FileOutputDelimitedForLDIF(TalendTextUtils.addQuotes(""
            // + PathUtils.getPortablePath(outPath.toOSString())), description.getEncoding());

            WSDLSchemaInputNode inWSDLSchemaNode = new WSDLSchemaInputNode(TalendTextUtils.addQuotes("" //$NON-NLS-1$
                    + PathUtils.getPortablePath(outPath.toOSString())), description.getEncoding(), description.getSchema(),
                    description.getWsdlSchemaBean(), description.getWsdlSchemaBean().getParameters());
            outNode.setMetadataList(inWSDLSchemaNode.getMetadataList());
            ps = new FileinToDelimitedProcess<WSDLSchemaInputNode>(inWSDLSchemaNode, outNode);

            break;

        case SALESFORCE_SCHEMA:
            SalesforceSchemaInputNode inSalesforceNode = new SalesforceSchemaInputNode(description.getSchema(),
                    description.getSalesforceSchemaBean());
            outNode.setMetadataList(inSalesforceNode.getMetadataList());
            SalesforceSchemaBean salesforceSchemaBean = description.getSalesforceSchemaBean();
            boolean uesHttp = salesforceSchemaBean.isUesHttp();
            salesforceSchemaBean.isUseProxy();
            if (uesHttp) {
                String proxyHost = salesforceSchemaBean.getProxyHost();
                String proxyPort = salesforceSchemaBean.getProxyPort();
                String userName = salesforceSchemaBean.getProxyUsername();
                String password = salesforceSchemaBean.getProxyPassword();
                final Prejob prejob = new Prejob("");
                final SetProxy setProxy = new SetProxy("HTTP_PROXY", proxyHost, proxyPort, userName, password, proxyHost + "|"
                        + proxyPort);
                ps = new FileinToDelimitedProcess<SalesforceSchemaInputNode>(inSalesforceNode, outNode, prejob, setProxy);
            } else {
                ps = new FileinToDelimitedProcess<SalesforceSchemaInputNode>(inSalesforceNode, outNode);
            }
            break;
        default:
            break;
        }
        return ps;
    }

    private IPath buildTempCSVFilename(IPath inPath) {
        String filename = inPath.lastSegment();
        if (inPath.getFileExtension() != null) {
            filename = filename.substring(0, filename.length() - inPath.getFileExtension().length());
        } else // Check if file has no suffix.
        {
            int length = filename.length();
            filename = filename.substring(0, length - 1) + "."; //$NON-NLS-1$
        }

        filename += CSV_EXT;
        IPath tempPath;
        tempPath = getTmpFolderPath();
        tempPath = tempPath.append(filename);

        return tempPath;
    }

    public CsvArray run() throws ProcessorException {
        IProcess talendProcess = buildProcess();
        IProcessor processor = ProcessorUtilities.getProcessor(talendProcess, null);
        processor.setProxyParameters(getProxyParameters());
        // try {
        // Delete previous Perl generated file
        File previousFile = outPath.toFile();
        if (previousFile.exists()) {
            previousFile.delete();
        }

        // Process ps = processor.run(process.getContextManager().getDefaultContext(), Processor.NO_STATISTICS,
        // Processor.NO_TRACES,Processor.WATCH_ALLOWED);//Old

        IContext context = talendProcess.getContextManager().getDefaultContext();
        processor.setContext(context);
        process = processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
        ProcessStreamTrashReader.readAndForget(process);

        if (!outPath.toFile().exists()) {
            throw new ProcessorException(Messages.getString("ShadowProcess.notGeneratedOutputException")); //$NON-NLS-1$
        }

        try {
            CsvArray array = new CsvArray();
            array = array.createFrom(outPath.toFile(), currentProcessEncoding);
            return array;
        } catch (IOException ioe) {
            throw new ProcessorException(ioe);
        }
    }

    /**
     *
     * DOC xye Comment method "runWithErrorOutputAsException".
     *
     * @param outputErrorAsException
     * @return
     * @throws ProcessorException
     */
    public CsvArray runWithErrorOutputAsException(final boolean outputErrorAsException) throws ProcessorException {

        IProcess talendProcess = buildProcess();

        IProcessor processor = ProcessorUtilities.getProcessor(talendProcess, null);
        processor.setProxyParameters(getProxyParameters());

        File previousFile = outPath.toFile();
        if (previousFile.exists()) {
            previousFile.delete();
        }

        IContext context = talendProcess.getContextManager().getDefaultContext();
        processor.setContext(context);
        process = processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
        IPath srcCodePath = processor.getSrcCodePath();
        IFile shadowFileInputToDelimitedOutputFile = processor.getTalendJavaProject().getProject().getFile(srcCodePath);
        if (shadowFileInputToDelimitedOutputFile != null && shadowFileInputToDelimitedOutputFile.exists()) {
            JobErrorsChecker.checkShadowFileError(shadowFileInputToDelimitedOutputFile);
        }
        String error = ProcessStreamTrashReader.readErrorStream(process);
        if (error != null) {
            log.warn(error, new ProcessorException(error));
        }

        if (!outPath.toFile().exists()) {
            if (outputErrorAsException && error != null) {
                throw new ProcessorException(error);
            } else {
                throw new ProcessorException(Messages.getString("ShadowProcess.notGeneratedOutputException")); //$NON-NLS-1$
            }
        }

        try {
            CsvArray array = new CsvArray();
            array = array.createFrom(outPath.toFile(), currentProcessEncoding);
            return array;
        } catch (IOException ioe) {
            throw new ProcessorException(ioe);
        }
    }

    /**
     * Destroy the current process if exists.
     *
     * @return error code of {@link java.lang.Process#exitValue()}
     */
    public int destroy() {
        int exitCode = 0;
        if (process != null) {
            process.destroy();
            try {
                exitCode = process.exitValue();
            } catch (IllegalThreadStateException itse) {
                // Can be throw on some UNIX system :(
                // but the process is really killed.
            }
            process = null;
        }
        return exitCode;
    }

    public String[] getProxyParameters() {
        return this.proxyParameters;
    }

    public void setProxyParameters(String[] proxyParameters) {
        if (proxyParameters != null) {
            this.proxyParameters = proxyParameters;
        }

    }

    private IPath getTmpFolderPath() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject physProject;
        String tmpFolder = System.getProperty("user.dir"); //$NON-NLS-1$
        try {
            physProject = ResourceModelUtils.getProject(project);
            tmpFolder = physProject.getFolder("temp").getLocation().toPortableString(); //$NON-NLS-1$
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        tmpFolder = tmpFolder + "/preview"; //$NON-NLS-1$
        return new Path(tmpFolder);
    }

}
