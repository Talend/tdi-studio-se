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
package org.talend.designer.rowgenerator.shadow;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.designer.rowgenerator.PluginUtils;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class RowGenProcessMain {

    public static final String PREVIEW = "Preview"; //$NON-NLS-1$

    private boolean running;

    private boolean killing;

    private final RowGeneratorComponent component;

    private final List<List<String>> results;

    private Process process;

    // private ECodeLanguage language;

    private RowGenProcess proc;

    private String number;

    private IContextManager jobContextManager;

    /**
     * qzhang RowGenPreivewCodeMain constructor comment.
     */
    public RowGenProcessMain(RowGeneratorComponent component) {
        this.component = component;
        results = new ArrayList<List<String>>();
        // initPerlArray();
    }

    private void getProcess() {
        initPerlArray();
        this.component.setNumber(number);
        if (component.getProcess() instanceof IProcess2) { // get current job context manager.
            jobContextManager = component.getProcess().getContextManager();
        }
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel(PREVIEW + "_RowGenerator2"); //$NON-NLS-1$
        property.setId(PREVIEW + "_RowGenerator2"); //$NON-NLS-1$
        proc = new RowGenProcess(property, component);
    }

    /**
     * qzhang Comment method "initPerlArray".
     */
    private void initPerlArray() {
        List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
        MetadataTable table = (MetadataTable) this.component.getMetadataList().get(0);
        for (IMetadataColumn col : table.getListColumns()) {
            MetadataColumnExt ext = (MetadataColumnExt) col;
            Map<String, Object> value = new HashMap<String, Object>();
            value.put(RowGeneratorComponent.COLUMN_NAME, ext.getLabel());
            value.put(RowGeneratorComponent.ARRAY, FunctionManagerExt.getOneColData(ext));
            map.add(value);
        }
        this.component.setTableElementParameter(map);
    }

    /**
     * qzhang Comment method "runPreviewCode".
     */
    public Process runPreviewCode() {
        getProcess();
        if (jobContextManager == null) {
            // proc.getContextManager().setListContext(component.getProcess().getContextManager().getListContext());
            proc.getContextManager().setDefaultContext(component.getProcess().getContextManager().getDefaultContext());
        } else {
            // proc.getContextManager().setListContext(jobContextManager.getListContext());
            proc.getContextManager().setDefaultContext(jobContextManager.getDefaultContext());
        }
        // IContext context2 = new org.talend.core.model.context.JobContext(PREVIEW);
        // if (UIManager.isJavaProject()) {
        // List<IContextParameter> params = new ArrayList<IContextParameter>();
        // JobContextParameter contextParameter = new JobContextParameter();
        // contextParameter.setContext(context2);
        // contextParameter.setName(PREVIEW);
        // contextParameter.setValue(PREVIEW);
        // contextParameter.setType("String");
        // params.add(contextParameter);
        // context2.setContextParameterList(params);
        // }

        // generate context files.
        IProcessor contextProcessor = ProcessorUtilities.getProcessor(proc, null);
        contextProcessor.setContext(proc.getContextManager().getDefaultContext());
        try {
            contextProcessor.cleanBeforeGenerate(TalendProcessOptionConstants.CLEAN_CONTEXTS);
            contextProcessor.generateContextCode();
        } catch (ProcessorException pe) {
            ExceptionHandler.process(pe);
        }

        IProcessor processor = ProcessorUtilities.getProcessor(proc, null, proc.getContextManager().getDefaultContext());
        try {
            return processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return null;
        }
    }

    /**
     * yzhang Comment method "run".
     *
     * @param refresh
     * @param number
     * @return
     */
    public List<List<String>> run(final Button refresh, String number) {
        this.number = number;
        results.clear();
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        try {
            progressService.runInUI(PlatformUI.getWorkbench().getProgressService(), new IRunnableWithProgress() {

                public void run(final IProgressMonitor monitor) {
                    monitor.beginTask(Messages.getString("RowGenPreivewCodeMain.Process.Generate"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    try {
                        try {
                            process = runPreviewCode();
                            if (process == null) {
                                return;
                            }

                            StringBuffer out = new StringBuffer();
                            StringBuffer err = new StringBuffer();

                            createResultThread(process.getErrorStream(), err).start();
                            createResultThread(process.getInputStream(), out).start();

                            process.waitFor();

                            if (out.length() > 0) {
                                convert(out.toString());
                            }
                            if (err.length() > 0) {
                                String mainMsg = Messages.getString("RowGenPreivewCodeMain.PerlRun.Error"); //$NON-NLS-1$
                                new ErrorDialogWidthDetailArea(Display.getCurrent().getActiveShell(), PluginUtils.PLUGIN_ID,
                                        mainMsg, Messages.getString("RowGenProcessMain.checkParameter", err.toString())); //$NON-NLS-1$
                            }
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                            kill();
                        }
                    } finally {
                        monitor.done();
                        refresh.setText(Messages.getString("RowGenPreivewCodeMain.PreviewBtn.Text")); //$NON-NLS-1$
                    }
                }
            }, null);

        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        } finally {
            proc.reconnection();
        }
        return results;
    }

    /**
     * qzhang Comment method "convert".
     *
     * @param string
     * @return
     */
    protected void convert(String string) {
        // if (UIManager.isJavaProject()) {
        // string = string.substring(string.indexOf("\n") + 1);
        // }
        string = string.replaceAll("\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
        String[] rows = string.split("\n"); //$NON-NLS-1$
        for (int i = 0; i < rows.length; i++) {
            char[] cs = rows[i].toCharArray();
            List<String> cols = new ArrayList<String>();
            cols.add("" + (i + 1)); //$NON-NLS-1$
            StringBuffer col = new StringBuffer();
            for (int j = 0; j < cs.length; j++) {
                if (cs[j] == '|') {
                    cols.add(col.toString());
                    col = new StringBuffer();
                } else {
                    col.append(cs[j]);
                }
            }
            cols.add(col.toString());
            results.add(cols);
        }
    }

    public int kill() {
        int exitCode;
        if (!killing && isRunning()) {
            killing = true;
        }
        if (!killing && isRunning()) {
            killing = true;
            try {
                exitCode = killProcess();
            } finally {
                killing = false;
            }
        } else {
            exitCode = 0;
        }

        setRunning(false);
        return exitCode;
    }

    /**
     * qzhang Comment method "killProcess".
     *
     * @return
     */
    private int killProcess() {
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

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        if (this.running != running) {
            this.running = running;
        }
    }

    private Thread createResultThread(final InputStream input, final StringBuffer result) {
        final int bufferSize = 1024;
        Thread thread = new Thread() {

            public void run() {
                try {
                    BufferedInputStream outStreamProcess = new BufferedInputStream(input);
                    byte[] buffer = new byte[bufferSize];
                    int count = 0;
                    while ((count = outStreamProcess.read(buffer, 0, buffer.length)) != -1) {
                        result.append(new String(buffer, 0, count));
                    }
                    outStreamProcess.close();
                } catch (IOException ioe) {
                    ExceptionHandler.process(ioe);
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        };
        return thread;
    }
}
