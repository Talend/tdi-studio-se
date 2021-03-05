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
package org.talend.designer.runprocess.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.utils.workbench.extensions.ExtensionImplementationProvider;
import org.talend.commons.utils.workbench.extensions.ExtensionPointLimiterImpl;
import org.talend.commons.utils.workbench.extensions.IExtensionPointLimiter;
import org.talend.core.model.process.IContext;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.prefs.RunProcessPrefsConstants;
import org.talend.designer.runprocess.ui.views.DefaultProcessViewHelper;
import org.talend.designer.runprocess.ui.views.IProcessViewHelper;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ProcessManager {

    private static IProcessViewHelper processViewHelper;

    public static ProcessManager manager;

    private JobJvmComposite targetComposite;

    private Shell shell;

    private IContext selectedContext;

    private static RunProcessContext processContext;

    private Boolean saveJobBeforeRun = true;

    private Boolean execTime = RunProcessPlugin.getDefault().getPreferenceStore()
            .getBoolean(RunProcessPrefsConstants.ISEXECTIMERUN);

    private Boolean stat = RunProcessPlugin.getDefault().getPreferenceStore()
            .getBoolean(RunProcessPrefsConstants.ISSTATISTICSRUN);

    private Boolean clearBeforeExec = RunProcessPlugin.getDefault().getPreferenceStore()
            .getBoolean(RunProcessPrefsConstants.ISCLEARBEFORERUN);

    private Boolean customLog4j = RunProcessPlugin.getDefault().getPreferenceStore()
            .getBoolean(RunProcessPrefsConstants.CUSTOMLOG4J);

    private String log4jLevel = RunProcessPlugin.getDefault().getPreferenceStore().getString(RunProcessPrefsConstants.LOG4JLEVEL);

    private boolean trace = true;

    public static ProcessManager getInstance() {
        if (manager == null) {
            manager = new ProcessManager();
        }
        IExtensionPointLimiter extensionPointLimiter = new ExtensionPointLimiterImpl(
                "org.talend.designer.runprocess.runprocess_view_helper", "runprocess_view_helper");
        IProcessViewHelper processViewHelperPrm = ExtensionImplementationProvider.getSingleInstance(extensionPointLimiter, null);
        if (processViewHelperPrm == null) {
            processViewHelperPrm = new DefaultProcessViewHelper();
        }
        setProcessViewHelper(processViewHelperPrm);
        return manager;

    }

    public static void setProcessViewHelper(IProcessViewHelper processView) {
        processViewHelper = processView;
    }

    public void setProcessContext(RunProcessContext processContext) {
        this.processContext = processContext;
    }

    protected static RunProcessContext getProcessContext() {
        return processContext;
    }

    public void setJobVMArgumentsComposite(JobVMArgumentsComposite argumentsViewer) {

    }

    /**
     * DOC Administrator Comment method "setProcessShell".
     *
     * @param shell
     */
    public void setProcessShell(Shell shell) {
        // TODO Auto-generated method stub
        this.shell = shell;
    }

    public Shell getProcessShell() {
        return shell;
    }

    /**
     * DOC Administrator Comment method "setSelectContext".
     *
     * @param selectedContext
     */
    public void setSelectContext(IContext selectedContext) {
        // TODO Auto-generated method stub
        this.selectedContext = selectedContext;
    }

    public IContext getSelectContext() {
        return selectedContext;
    }

    /**
     * DOC Administrator Comment method "setSaveJobBeforeRun".
     *
     * @param b
     */
    public void setSaveJobBeforeRun(boolean b) {
        // TODO Auto-generated method stub
        this.saveJobBeforeRun = b;
    }

    public Boolean getSaveJobBeforeRun() {
        return this.saveJobBeforeRun;
    }

    /**
     * DOC Administrator Comment method "setExecTime".
     *
     * @param b
     */
    public void setExecTime(boolean b) {
        // TODO Auto-generated method stub
        this.execTime = b;
    }

    public Boolean getStat() {
        return this.stat;
    }

    public void setStat(Boolean stat) {
        this.stat = stat;
    }

    public Boolean getExecTime() {
        return this.execTime;
    }

    /**
     * DOC Administrator Comment method "setClearBeforeExec".
     *
     * @param selection
     */
    public void setClearBeforeExec(boolean selection) {
        // TODO Auto-generated method stub
        this.clearBeforeExec = selection;
    }

    public Boolean getClearBeforeExec() {
        return this.clearBeforeExec;
    }

    public JobJvmComposite createJobJvmComposite(Composite parent) {
        targetComposite = this.processViewHelper.getProcessComposite(parent);
        return targetComposite;
    }

    public JobJvmComposite getJobJvmComposite() {
        return targetComposite;
    }

    /**
     * DOC Administrator Comment method "setBooleanTrace".
     *
     * @param b
     */
    public void setBooleanTrace(boolean b) {
        // TODO Auto-generated method stub
        this.trace = b;
    }

    public boolean isTrace() {
        return this.trace;
    }

    public String getLog4jLevel() {
        return log4jLevel;
    }

    public void setLog4jLevel(String log4jLevel) {
        this.log4jLevel = log4jLevel;
    }

    public Boolean getCustomLog4j() {
        return customLog4j;
    }

    public void setCustomLog4j(Boolean customLog4j) {
        this.customLog4j = customLog4j;
    }

}
