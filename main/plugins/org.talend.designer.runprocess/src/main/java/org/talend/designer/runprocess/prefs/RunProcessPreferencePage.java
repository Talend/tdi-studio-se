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
package org.talend.designer.runprocess.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.ui.ProcessManager;

/**
 *
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public class RunProcessPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /**
     * This class exists to provide visibility to the <code>refreshValidState</code> method and to perform more
     * intelligent clearing of the error message.
     */
    protected class ConsoleIntegerFieldEditor extends IntegerFieldEditor {

        public ConsoleIntegerFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        /**
         * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
         */
        @Override
        protected void refreshValidState() {
            super.refreshValidState();
        }

        /**
         * Clears the error message from the message line if the error message is the error message from this field
         * editor.
         */
        @Override
        protected void clearErrorMessage() {
            if (canClearErrorMessage()) {
                super.clearErrorMessage();
            }
        }
    }

    // private BooleanFieldEditor2 fWrapEditor = null;
    private final IntegerFieldEditor fClientComPortEditor = null;

    // private BooleanFieldEditor2 fUseBufferSize = null;
    private final ConsoleIntegerFieldEditor fBufferSizeEditor = null;

    private final ConsoleIntegerFieldEditor fTabSizeEditor = null;

    private Group clientGroup;

    private StringFieldEditor fClientHostEditor;

    private IntegerFieldEditor fClientStatPortEditor;

    private IntegerFieldEditor fClientTraceEditor;

    private BooleanFieldEditor onSavebeforeField;

    private Group remoteServersGroup;

    private BooleanFieldEditor onClearbeforeField;

    private BooleanFieldEditor onExecTimeField;

    private BooleanFieldEditor onStatisticsField;

    private BooleanFieldEditor onTracesField;

    private IntegerFieldEditor tracesTime;

    private Group vmGroup;

    private Composite parent;

    private ProcessManager manager;

    // private FontFieldEditor consoleFontField = null;

    /**
     * Create the console page.
     */
    public RunProcessPreferencePage() {
        super(GRID);
        setDescription(Messages.getString("prefs.configuration.title")); //$NON-NLS-1$
        setPreferenceStore(RunProcessPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {

        return RunProcessPlugin.getDefault().getPreferenceStore();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

    }

    /**
     * DOC chuang Comment method "addConsoleFont".
     */
    // private void addConsoleFont() {
    // Group group = new Group(parent, SWT.NONE);
    // group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    // group.setLayout(new GridLayout(1, false));
    //        group.setText(Messages.getString("RunProcessPreferencePage.outputConsole")); //$NON-NLS-1$
    //
    // Composite composite = new Composite(group, SWT.NONE);
    // composite.setLayout(new GridLayout(3, false));
    // GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    // gridData.grabExcessHorizontalSpace = true;
    // gridData.horizontalSpan = 3;
    // composite.setLayoutData(gridData);
    // consoleFontField = new FontFieldEditor(RunProcessPrefsConstants.CONSOLE_FONT, Messages
    //                .getString("RunProcessPreferencePage.consoleFont"), composite); //$NON-NLS-1$
    //
    // }
    /**
     * Create all field editors for this page.
     */
    @Override
    public void createFieldEditors() {

        parent = new Composite(getFieldEditorParent(), SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginBottom = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginHeight = 0;
        parent.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        parent.setLayoutData(gridData);

        clientGroup = new Group(parent, SWT.NONE);
        clientGroup.setText(Messages.getString("prefs.clientConfiguration")); //$NON-NLS-1$
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        clientGroup.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        clientGroup.setLayoutData(gd);

        // /////////////////////////////////////////////////////////////////////////////
        // CLIENT_STATS_PORTS

        Composite compositeStatsPorts = new Composite(clientGroup, SWT.NONE);
        compositeStatsPorts.setLayoutData(new GridData(GridData.FILL_BOTH));
        gridLayout = new GridLayout(2, true);
        compositeStatsPorts.setLayout(gridLayout);

        Composite compositeStatsPort1 = new Composite(compositeStatsPorts, SWT.NONE);
        compositeStatsPort1.setLayoutData(new GridData(GridData.FILL_BOTH));
        gridLayout = new GridLayout(2, true);
        compositeStatsPort1.setLayout(gridLayout);
        fClientStatPortEditor = new IntegerFieldEditor(RunProcessPrefsConstants.CLIENT_STATS_PORT_BOUND1,
                Messages.getString("prefs.clientStatsPortBound1"), compositeStatsPort1); //$NON-NLS-1$
        addField(fClientStatPortEditor);
        fClientStatPortEditor.setValidRange(1024, 65535);
        fClientStatPortEditor.setErrorMessage(Messages.getString("runProcessRemote.clientStatInvalidRange")); //$NON-NLS-1$

        Composite compositeStatsPort2 = new Composite(compositeStatsPorts, SWT.NONE);
        compositeStatsPort2.setLayoutData(new GridData(GridData.FILL_BOTH));
        gridLayout = new GridLayout(2, true);
        compositeStatsPort2.setLayout(gridLayout);
        fClientStatPortEditor = new IntegerFieldEditor(RunProcessPrefsConstants.CLIENT_STATS_PORT_BOUND2,
                Messages.getString("prefs.clientStatsPortBound2"), compositeStatsPort2); //$NON-NLS-1$
        addField(fClientStatPortEditor);
        fClientStatPortEditor.setValidRange(1024, 65535);
        fClientStatPortEditor.setErrorMessage(Messages.getString("prefs.clientStatInvalidRange")); //$NON-NLS-1$
        // /////////////////////////////////////////////////////////////////////////////

        // /////////////////////////////////////////////////////////////////////////////
        // CLIENT_TRACE_PORTS

        Composite compositeTracePorts = new Composite(clientGroup, SWT.NONE);
        compositeTracePorts.setLayoutData(new GridData(GridData.FILL_BOTH));
        gridLayout = new GridLayout(2, true);
        compositeTracePorts.setLayout(gridLayout);

        Composite compositeTracePorts1 = new Composite(compositeTracePorts, SWT.NONE);
        compositeTracePorts1.setLayoutData(new GridData(GridData.FILL_BOTH));
        gridLayout = new GridLayout(2, true);
        compositeTracePorts1.setLayout(gridLayout);
        fClientStatPortEditor = new IntegerFieldEditor(RunProcessPrefsConstants.CLIENT_TRACE_PORT_BOUND1,
                Messages.getString("prefs.clientTracePortBound1"), compositeTracePorts1); //$NON-NLS-1$
        addField(fClientStatPortEditor);
        fClientStatPortEditor.setValidRange(1024, 65535);
        fClientStatPortEditor.setErrorMessage(Messages.getString("prefs.clientStatInvalidRange")); //$NON-NLS-1$

        Composite compositeTracePort2 = new Composite(compositeTracePorts, SWT.NONE);
        compositeTracePort2.setLayoutData(new GridData(GridData.FILL_BOTH));
        gridLayout = new GridLayout(2, true);
        compositeTracePort2.setLayout(gridLayout);
        fClientStatPortEditor = new IntegerFieldEditor(RunProcessPrefsConstants.CLIENT_TRACE_PORT_BOUND2,
                Messages.getString("prefs.clientTracePortBound2"), compositeTracePort2); //$NON-NLS-1$
        addField(fClientStatPortEditor);
        fClientStatPortEditor.setValidRange(1024, 65535);
        fClientStatPortEditor.setErrorMessage(Messages.getString("prefs.clientStatInvalidRange")); //$NON-NLS-1$
        // /////////////////////////////////////////////////////////////////////////////

        Composite compositeStateTraceRun = new Composite(clientGroup, SWT.NONE);
        compositeStateTraceRun.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutStateTraceRun = new GridLayout(3, true);
        compositeStateTraceRun.setLayout(gridLayoutStateTraceRun);

        Composite compositeSaveBeforeRun = new Composite(compositeStateTraceRun, SWT.NONE);
        compositeSaveBeforeRun.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutSaveBeforeRun = new GridLayout(1, true);
        compositeSaveBeforeRun.setLayout(gridLayoutSaveBeforeRun);
        onSavebeforeField = new BooleanFieldEditor(RunProcessPrefsConstants.ISSAVEBEFORERUN,
                Messages.getString("RunProcessPreferencePage.save"), SWT.NONE, //$NON-NLS-1$
                compositeSaveBeforeRun);
        addField(onSavebeforeField);

        Composite compositeClearBeforeRun = new Composite(compositeStateTraceRun, SWT.NONE);
        compositeClearBeforeRun.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutClearBeforeRun = new GridLayout(1, true);
        compositeClearBeforeRun.setLayout(gridLayoutClearBeforeRun);
        onClearbeforeField = new BooleanFieldEditor(RunProcessPrefsConstants.ISCLEARBEFORERUN,
                Messages.getString("RunProcessPreferencePage.clear"), SWT.NONE, //$NON-NLS-1$
                compositeClearBeforeRun);

        addField(onClearbeforeField);
        Composite compositeExecTimeRun = new Composite(compositeStateTraceRun, SWT.NONE);
        compositeExecTimeRun.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutExecTimeRun = new GridLayout(1, true);
        compositeExecTimeRun.setLayout(gridLayoutExecTimeRun);
        onExecTimeField = new BooleanFieldEditor(RunProcessPrefsConstants.ISEXECTIMERUN,
                Messages.getString("RunProcessPreferencePage.execTime"), SWT.NONE, //$NON-NLS-1$
                compositeExecTimeRun);
        addField(onExecTimeField);

        Composite compositeStatisticsRun = new Composite(compositeStateTraceRun, SWT.NONE);
        compositeStatisticsRun.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutStatisticsRun = new GridLayout(1, true);
        compositeStatisticsRun.setLayout(gridLayoutStatisticsRun);
        onStatisticsField = new BooleanFieldEditor(RunProcessPrefsConstants.ISSTATISTICSRUN,
                Messages.getString("RunProcessPreferencePage.statistics"), SWT.NONE, //$NON-NLS-1$
                compositeStatisticsRun);
        addField(onStatisticsField);

        Composite compositeTracesRun = new Composite(compositeStateTraceRun, SWT.NONE);
        compositeTracesRun.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutTracesRun = new GridLayout(1, false);
        compositeTracesRun.setLayout(gridLayoutTracesRun);
        onTracesField = new BooleanFieldEditor(RunProcessPrefsConstants.ISTRACESRUN,
                Messages.getString("RunProcessPreferencePage.traces"), SWT.NONE, compositeTracesRun); //$NON-NLS-1$
        addField(onTracesField);

        Composite compositeTracesTime = new Composite(compositeStateTraceRun, SWT.NONE);
        compositeTracesTime.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutTracesTime = new GridLayout(1, false);
        compositeTracesTime.setLayout(gridLayoutTracesTime);
        tracesTime = new IntegerFieldEditor(RunProcessPrefsConstants.STRACESTIME,
                Messages.getString("RunProcessPreferencePage.pauseTime"), compositeTracesTime); //$NON-NLS-1$
        addField(tracesTime);

        // 0004895: Font size of the output console are very small
        // addConsoleFont();

        Composite argumentsComposite = new Composite(parent, SWT.NONE);
        argumentsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayoutArguments = new GridLayout(1, false);
        argumentsComposite.setLayout(gridLayoutArguments);
        String text = "RunProcessPreferencePage.vmArgument";
        IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (breaningService.isPoweredOnlyCamel()) {
            text = "RunProcessPreferencePage.RouteVmArgument";
        }
        VMArgumentsViewer argumentsViewer = new VMArgumentsViewer(RunProcessPrefsConstants.VMARGUMENTS, Messages.getString(text), //$NON-NLS-1$
                argumentsComposite);
        addField(argumentsViewer);
        manager = ProcessManager.getInstance();
    }

    protected void createSpacer(Composite composite, int columnSpan) {
        Label label = new Label(composite, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = columnSpan;
        label.setLayoutData(gd);
    }

    /**
     * @see IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        // consoleFontField.store();

        // update high water mark to be (about) 100 lines (100 * 80 chars) greater than low water mark
        // IPreferenceStore store = RunRemoteProcessPlugin.getDefault().getPreferenceStore();
        // int low = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
        // int high = low + 8000;
        // store.setValue(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK, high);
        RunProcessPlugin.getDefault().savePluginPreferences();
        IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        service.refreshView();
        manager.setClearBeforeExec(onClearbeforeField.getBooleanValue());
        manager.setExecTime(onExecTimeField.getBooleanValue());
        manager.setStat(onStatisticsField.getBooleanValue());
        manager.setSaveJobBeforeRun(onSavebeforeField.getBooleanValue());
        return ok;
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
     */
    @Override
    protected void initialize() {
        super.initialize();
        // if (consoleFontField != null) {
        // consoleFontField.setPage(this);
        // consoleFontField.setPropertyChangeListener(this);
        // consoleFontField.setPreferenceStore(getPreferenceStore());
        // consoleFontField.load();
        // }
    }

    @Override
    public void dispose() {
        super.dispose();
        // if (consoleFontField != null) {
        // consoleFontField.setPage(null);
        // consoleFontField.setPropertyChangeListener(null);
        // consoleFontField.setPreferenceStore(null);
        // }
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        RunProcessPlugin.getDefault().getPreferenceStore().setDefault(RunProcessPrefsConstants.ISCLEARBEFORERUN, true);
        manager.setClearBeforeExec(true);
        super.performDefaults();
        // consoleFontField.loadDefault();
    }

    protected boolean canClearErrorMessage() {
        if (fClientComPortEditor.isValid() && fBufferSizeEditor.isValid()) {
            return true;
        }
        return false;
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getProperty().equals(FieldEditor.IS_VALID)) {
            boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
            // If the new value is true then we must check all field editors.
            // If it is false, then the page is invalid in any case.
            if (newValue) {
                if (fClientComPortEditor != null && event.getSource() != fClientComPortEditor) {
                    // fClientComPortEditor.refreshValidState();
                }
                checkState();
            } else {
                super.propertyChange(event);
            }

        } else {
            super.propertyChange(event);
        }
    }
}
