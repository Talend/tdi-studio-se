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

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.ProcessStreamTrashReaderUtil;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public abstract class AbstractGuessSchemaProcess {

    protected static final String CSV_EXT = "csv"; //$NON-NLS-1$

    protected static final String DEFAULT_JOB_NAME = "Mock_job_for_Guess_schema"; //$NON-NLS-1$

    protected static String TEMPFILE_APPEND_NAME = "GuessSchemaDelimitedFile"; //$NON-NLS-1$

    protected static final int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore()
            .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT);

    private Process process;

    private Property property;

    private INode node;

    private IPath outpath;

    private IPath temppath;

    private IContext selectContext;

    private String currentProcessEncoding = "UTF-8"; //$NON-NLS-1$

    public AbstractGuessSchemaProcess(final Property property, final INode node, final IContext selectContext) {
        this.property = property;
        this.node = node;
        this.selectContext = selectContext;
        initOutpath();
    }

    abstract protected void buildProcess();

    abstract protected boolean isCheckError();

    protected void initOutpath() {
        outpath = SharedStudioUtils.getTempFolderPath();
    }

    protected void configContext(Process inProcess, INode inNode) {
        IContext selectContext = getSelectContext();
        inProcess.getContextManager().getListContext().addAll(inNode.getProcess().getContextManager().getListContext());
        inProcess.getContextManager().setDefaultContext(selectContext);
    }

    public CsvArray run() throws ProcessorException {
        IPath temppath = buildTempCSVFilename();
        setTemppath(temppath);

        CsvArray array = new CsvArray();
        buildProcess();
        IProcessor processor = ProcessorUtilities.getProcessor(getProcess(), null);
        processor.setContext(selectContext);
        File previousFile = temppath.toFile();
        if (previousFile.exists()) {
            previousFile.delete();
        }
        java.lang.Process executeprocess = processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
        StringBuffer buffer = new StringBuffer();
        ProcessStreamTrashReaderUtil.readAndForget(executeprocess, buffer);
        final String errorMessage = buffer.toString();

        if (isCheckError() && !"".equals(buffer.toString())) {
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
                        sqlError.append(tokenizer.nextToken()).append("\n"); //$NON-NLS-1$
                    } else if (tokenizer.countTokens() == 1) {
                        sqlError.append(tokenizer.nextToken()).append("\n"); //$NON-NLS-1$
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
                        return getErrorMessage();
                    }
                };
            }
        }
        return array;
    }

    protected String getErrorMessage() {
        return Messages.getString("GuessSchemaController.0", System.getProperty("line.separator")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static Property getNewmockProperty() {

        Property mockProperty = PropertiesFactory.eINSTANCE.createProperty();
        mockProperty
                .setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        mockProperty.setVersion(VersionUtils.DEFAULT_VERSION);
        mockProperty.setStatusCode(""); //$NON-NLS-1$
        mockProperty.setId("ID"); //$NON-NLS-1$
        mockProperty.setLabel(DEFAULT_JOB_NAME);

        return mockProperty;
    }

    // write content to a temp .csv file
    protected IPath buildTempCSVFilename() {
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
        tempPath = Path
                .fromOSString(CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.FILE_PATH_TEMP));
        tempPath = tempPath.append(filename);

        return tempPath;
    }

    public Process getProcess() {
        return this.process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public Property getProperty() {
        return this.property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public INode getNode() {
        return this.node;
    }

    public void setNode(INode node) {
        this.node = node;
    }

    public IPath getOutpath() {
        return this.outpath;
    }

    public void setOutpath(IPath outpath) {
        this.outpath = outpath;
    }

    public IPath getTemppath() {
        return this.temppath;
    }

    public void setTemppath(IPath temppath) {
        this.temppath = temppath;
    }

    public IContext getSelectContext() {
        return this.selectContext;
    }

    public void setSelectContext(IContext selectContext) {
        this.selectContext = selectContext;
    }

    public String getCurrentProcessEncoding() {
        return this.currentProcessEncoding;
    }

    public void setCurrentProcessEncoding(String currentProcessEncoding) {
        this.currentProcessEncoding = currentProcessEncoding;
    }

}
