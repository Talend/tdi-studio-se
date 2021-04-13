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
package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.ui.login.LoginHelper;
import org.talend.repository.ui.login.connections.network.NetworkConfiguration;

public class PerformancePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private CheckBoxFieldEditor dbConnTimeoutActive;

    private IntegerFieldEditor dbConnTimeout;

    private IntegerFieldEditor hbaseOrMaprDBScanLimit;

    public PerformancePreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault()
                .getService(IBrandingService.class);
        if (breaningService.isPoweredOnlyCamel()) {
            addField(new BooleanFieldEditor(ITalendCorePrefConstants.DEACTIVE_REPOSITORY_UPDATE,
                    Messages.getString("PerformancePreferencePage.display.deactiveRepositoryUpdate"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK,
                    Messages.getString("PerformancePreferencePage.propertyCodeCheck"), getFieldEditorParent())); //$NON-NLS-1$

            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.GENERATE_CODE_WHEN_OPEN_JOB,
                    Messages.getString("PerformancePreferencePage.generateCode"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.CHECK_ONLY_LAST_VERSION,
                    Messages.getString("PerformancePreferencePage.checkVersion"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPAGATE_CONTEXT,
                    Messages.getString("PerformancePreferencePage.propagateContext"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPAGATE_CONTEXT_VARIABLE,
                    Messages.getString("PerformancePreferencePage.addOrDeleteVariable"), //$NON-NLS-1$
                    getFieldEditorParent()));

            dbConnTimeoutActive = new CheckBoxFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED,
                    Messages.getString("PerformancePreferencePage.ActivedTimeoutSetting"), getFieldEditorParent()); //$NON-NLS-1$
            dbConnTimeoutActive.getButton().addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    checkDBTimeout();
                }
            });
            dbConnTimeout = new IntegerFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT,
                    Messages.getString("PerformancePreferencePage.ConnectionTimeout"), //$NON-NLS-1$
                    getFieldEditorParent());
            Text textControl = dbConnTimeout.getTextControl(getFieldEditorParent());
            textControl.setToolTipText(Messages.getString("PerformancePreferencePage.ConnectionTimeoutTip")); //$NON-NLS-1$
            dbConnTimeout.setValidRange(0, Short.MAX_VALUE);
            textControl.setEnabled(getPreferenceStore().getBoolean(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED));
            addField(dbConnTimeoutActive);
            addField(dbConnTimeout);
            addRemoteInforAutoCheckFiled();
        } else {
            addField(new BooleanFieldEditor(ITalendCorePrefConstants.DEACTIVE_REPOSITORY_UPDATE,
                    Messages.getString("PerformancePreferencePage.display.deactiveRepositoryUpdate"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK,
                    Messages.getString("PerformancePreferencePage.propertyCodeCheck"), getFieldEditorParent())); //$NON-NLS-1$

            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.GENERATE_CODE_WHEN_OPEN_JOB,
                    Messages.getString("PerformancePreferencePage.generateCode"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.CHECK_ONLY_LAST_VERSION,
                    Messages.getString("PerformancePreferencePage.checkVersion"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPAGATE_CONTEXT,
                    Messages.getString("PerformancePreferencePage.propagateContext"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPAGATE_CONTEXT_VARIABLE,
                    Messages.getString("PerformancePreferencePage.addOrDeleteVariable"), //$NON-NLS-1$
                    getFieldEditorParent()));

            dbConnTimeoutActive = new CheckBoxFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED,
                    Messages.getString("PerformancePreferencePage.ActivedTimeoutSetting"), getFieldEditorParent()); //$NON-NLS-1$
            dbConnTimeoutActive.getButton().addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    checkDBTimeout();
                }
            });
            dbConnTimeout = new IntegerFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT,
                    Messages.getString("PerformancePreferencePage.ConnectionTimeout"), //$NON-NLS-1$
                    getFieldEditorParent());
            Text textControl = dbConnTimeout.getTextControl(getFieldEditorParent());
            textControl.setToolTipText(Messages.getString("PerformancePreferencePage.ConnectionTimeoutTip")); //$NON-NLS-1$
            dbConnTimeout.setValidRange(0, Short.MAX_VALUE);
            textControl.setEnabled(getPreferenceStore().getBoolean(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED));
            addField(dbConnTimeoutActive);
            addField(dbConnTimeout);

            addField(new BooleanFieldEditor(ITalendCorePrefConstants.ADD_USER_ROUTINES,
                    Messages.getString("PerformancePreferencePage.addAllUserRoutines"), //$NON-NLS-1$
                    getFieldEditorParent()));
            // TDI-8323:remove this one,we do not need this since we always add all system routines for new job
            // addField(new BooleanFieldEditor(ITalendCorePrefConstants.ADD_SYSTEM_ROUTINES, Messages
            // .getString("PerformancePreferencePage.addAllSystemRoutines"),//$NON-NLS-1$
            // getFieldEditorParent()));
            addRemoteInforAutoCheckFiled();
        }
        IntegerFieldEditor codeFormatTimeout = new IntegerFieldEditor(
                ITalendCorePrefConstants.PERFORMANCE_JAVA_PROCESS_CODE_FORMATE_TIMEOUT,
                Messages.getString("PerformancePreferencePage.CodeFormatTimeout"), //$NON-NLS-1$
                getFieldEditorParent());
        codeFormatTimeout.setValidRange(1, Short.MAX_VALUE);
        addField(codeFormatTimeout);

        hbaseOrMaprDBScanLimit = new IntegerFieldEditor(ITalendCorePrefConstants.HBASE_OR_MAPRDB_SCAN_LIMIT,
                Messages.getString("PerformancePreferencePage.HBaseOrMaprDBScanLimit"), //$NON-NLS-1$
                getFieldEditorParent());
        Text limitTextControl = hbaseOrMaprDBScanLimit.getTextControl(getFieldEditorParent());
        limitTextControl.setToolTipText(Messages.getString("PerformancePreferencePage.HBaseOrMaprDBScanLimitTip")); //$NON-NLS-1$
        hbaseOrMaprDBScanLimit.setValidRange(0, Short.MAX_VALUE);
        addField(hbaseOrMaprDBScanLimit);
    }

    private void addRemoteInforAutoCheckFiled() {
        if (PluginChecker.isRemoteProviderPluginLoaded()) {
            if (PluginChecker.isSVNProviderPluginLoaded()) {
                final CheckBoxFieldEditor autoCheckField = new CheckBoxFieldEditor(
                        ITalendCorePrefConstants.SVN_UPDATE_INFO_AUTO_CHECK,
                        Messages.getString("PerformancePreferencePage.autoCheckField"), getFieldEditorParent()); //$NON-NLS-1$
                addField(autoCheckField);
                final IntegerFieldEditor autoCheckTime = new IntegerFieldEditor(
                        ITalendCorePrefConstants.SVN_UPDATE_INFO_AUTO_CHECK_TIME_INTERVAL,
                        Messages.getString("PerformancePreferencePage.autoCheckTime"), //$NON-NLS-1$
                        getFieldEditorParent());
                autoCheckTime.setValidRange(1, 30);
                autoCheckTime.setEnabled(getPreferenceStore().getBoolean(ITalendCorePrefConstants.SVN_UPDATE_INFO_AUTO_CHECK),
                        getFieldEditorParent());
                addField(autoCheckTime);
                autoCheckField.getButton().addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (autoCheckField.getButton().getSelection()) {
                            autoCheckTime.setEnabled(true, getFieldEditorParent());
                        } else {
                            autoCheckTime.setEnabled(false, getFieldEditorParent());
                        }
                    }
                });
            }

            final CheckBoxFieldEditor autoRefreshLocksField = new CheckBoxFieldEditor(ITalendCorePrefConstants.AUTO_REFRESH_LOCKS,
                    Messages.getString("PerformancePreferencePage.autoRefreshLocksField"), getFieldEditorParent()); //$NON-NLS-1$
            addField(autoRefreshLocksField);
            //
            String timeout = Messages.getString("PerformancePreferencePage.tacTimeout");//$NON-NLS-1$
            String readTimeout = Messages.getString("PerformancePreferencePage.tacTimeout.read");//$NON-NLS-1$
            if (!LoginHelper.isRemotesConnection()) {
                timeout = Messages.getString("PerformancePreferencePage.defaultTimeout");//$NON-NLS-1$
                readTimeout = Messages.getString("PerformancePreferencePage.defaultTimeout.read");//$NON-NLS-1$
            }
            final IntegerFieldEditor tacConnectionTimeout = new IntegerFieldEditor(
                    ITalendCorePrefConstants.PERFORMANCE_TAC_CONNECTION_TIMEOUT, timeout, getFieldEditorParent());
            tacConnectionTimeout.setValidRange(NetworkConfiguration.CONNECTION_TIMEOUT_MIN,
                    NetworkConfiguration.CONNECTION_TIMEOUT_MAX);
            addField(tacConnectionTimeout);

            final IntegerFieldEditor tacReadTimeout = new IntegerFieldEditor(
                    ITalendCorePrefConstants.PERFORMANCE_TAC_READ_TIMEOUT, readTimeout, getFieldEditorParent());
            tacReadTimeout.setValidRange(NetworkConfiguration.READ_TIMEOUT_MIN, NetworkConfiguration.READ_TIMEOUT_MAX);
            addField(tacReadTimeout);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void dispose() {
        super.dispose();
        // TDI-21143 : Studio repository view : remove all refresh call to repo view
        // IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
        // if (view != null) {
        // view.refresh();
        // }
    }

    private void checkDBTimeout() {
        if (dbConnTimeout != null) {
            Text textControl = dbConnTimeout.getTextControl(getFieldEditorParent());
            if (textControl != null && dbConnTimeoutActive != null) {
                textControl.setEnabled(dbConnTimeoutActive.getBooleanValue());
            }
        }
    }

    @Override
    protected void performApply() {
        super.performApply();
    }

    @Override
    public boolean performOk() {
        final boolean toReturn = super.performOk();
        return toReturn;
    }

}
