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
package org.talend.repository.json.ui.shadow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.repository.ResourceModelUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.preview.IProcessDescription;
import org.talend.core.repository.model.preview.SalesforceSchemaBean;
import org.talend.core.utils.CsvArray;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessStreamTrashReader;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.shadow.FileInputXmlNode;
import org.talend.designer.runprocess.shadow.FileOutputDelimitedNode;
import org.talend.designer.runprocess.shadow.FileinToDelimitedProcess;
import org.talend.repository.ProjectManager;

/**
 * Launch a Process in shadow mode. <br/>
 *
 * $Id: ShadowProcess.java 96654 2013-01-10 12:40:34Z mhirt $
 *
 * @param <T>
 *
 */
public class JSONShadowProcess<T extends IProcessDescription> {

    /**
     *
     */
    private static final String TEMP_LDAP_SCHEMA_FILE_NAME = "TempLDAPSchema"; //$NON-NLS-1$

    private static final String TEMP_WSDL_SCHEMA_FILE_NAME = "TempWSDLSchema"; //$NON-NLS-1$

    private static final String TEMP_SALEFORCE_SCHEMA_FILE_NAME = "TempSALESFORCESchema"; //$NON-NLS-1$

    private String currentProcessEncoding = "ISO-8859-15"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(JSONShadowProcess.class);

    /**
     * Available Shadow Process Types.
     *
     * $Id: ShadowProcess.java 96654 2013-01-10 12:40:34Z mhirt $
     *
     */
    public static enum EJSONShadowProcessType {
        FILE_JSON,
        FILE_XML;

        private EJSONShadowProcessType() {

        }
    }

    // private static final String XML_EXT = "xml"; //$NON-NLS-1$

    private static final String CSV_EXT = "csv"; //$NON-NLS-1$

    private T description;

    private String inPath;

    private IPath outPath;

    private EJSONShadowProcessType type;

    private Process process;

    private String[] proxyParameters;

    /**
     * Constructs a new ShadowProcess.
     */
    public JSONShadowProcess(T description, EJSONShadowProcessType type) {
        super();

        this.description = description;
        String filePath = description.getFilepath();

        IPath tempPath = getTmpFolderPath();
        if (filePath != null) {
            this.inPath = filePath;
            this.outPath = buildTempCSVFilename(new Path(filePath));
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
        case FILE_JSON:
            List<Map<String, String>> newmappings4Jsonpath = new ArrayList<Map<String, String>>();
            List<Map<String, String>> oldmappings4Jsonpath = description.getMapping();
            for (int i = 0; i < oldmappings4Jsonpath.size(); i++) {
                Map<String, String> map = oldmappings4Jsonpath.get(i);
                map.put("SCHEMA_COLUMN", "row" + i); //$NON-NLS-1$ //$NON-NLS-2$
                newmappings4Jsonpath.add(map);
            }
            FileInputJsonNode4Input inJsonNode = new FileInputJsonNode4Input(inPath, description.getLoopQuery(),
                    description.getMapping(), description.getLoopLimit(), description.getEncoding(), description.getReadbyMode());
            ps = new FileinToDelimitedProcess<FileInputJsonNode4Input>(inJsonNode, outNode);
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

        String error = ProcessStreamTrashReader.readErrorStream(process);
        if (outputErrorAsException) {
            if (error != null) {
                throw new ProcessorException(error);
            }
        } else {
            if (error != null) {
                log.warn(error, new ProcessorException(error));
            }
        }

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
