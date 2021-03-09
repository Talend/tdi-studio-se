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
package org.talend.repository.preference;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.ExceptionMessageDialog;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.core.service.ICommandLineService;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.preference.audit.AuditManager;
import org.talend.repository.preference.audit.SupportDBUrlStore;
import org.talend.repository.preference.audit.SupportDBUrlType;
import org.talend.repository.preference.audit.SupportDBVersions;
import org.talend.utils.security.StudioEncryption;
import org.talend.utils.sugars.TypedReturnCode;

/**
 * created by hcyi on May 9, 2018
 * Detailled comment
 *
 */
public class AuditProjectSettingPage extends ProjectSettingPage {

    private Button generateButton;

    private Button savedInDBButton;

    private Group dbConfigGroup;

    private LabelledCombo dbTypeCombo;

    private LabelledCombo dbVersionCombo;

    private LabelledText driverText;

    private LabelledText urlText;

    private LabelledText usernameText;

    private LabelledText passwordText;

    private Button checkButton;

    private String generatePath;

    private ProjectPreferenceManager prefManager;

    private LabelledCombo historyCombo;

    private Button refreshButton;

    private Button historyGenerateButton;

    private Integer selectedAuditId;

    private Map<Integer, String> currentParameters = new HashMap<Integer, String>();

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        generateButton = new Button(composite, SWT.NONE);
        generateButton.setText(Messages.getString("AuditProjectSettingPage.generateButtonText")); //$NON-NLS-1$

        savedInDBButton = new Button(composite, SWT.CHECK);
        savedInDBButton.setText(Messages.getString("AuditProjectSettingPage.savedInDBButtonText")); //$NON-NLS-1$
        //
        createDbConfigGroup(composite);

        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }
        prefManager = new ProjectPreferenceManager(AuditManager.AUDIT_RESOURCES, true);
        addListeners();
        init();
        return composite;
    }

    protected Composite createDbConfigGroup(Composite parent) {
        GridLayout layout2 = (GridLayout) parent.getLayout();
        dbConfigGroup = new Group(parent, SWT.NONE);
        dbConfigGroup.setText(Messages.getString("AuditProjectSettingPage.DBConfig.title")); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(layout2.numColumns, 1).align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                .applyTo(dbConfigGroup);
        dbConfigGroup.setLayout(new GridLayout(1, false));

        Composite dbConfigComposite = new Composite(dbConfigGroup, SWT.NULL);
        GridLayout dbConfigCompLayout = new GridLayout(3, false);
        dbConfigComposite.setLayout(dbConfigCompLayout);
        dbConfigComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dbTypeCombo = new LabelledCombo(dbConfigComposite, Messages.getString("AuditProjectSettingPage.DBConfig.dbType"), //$NON-NLS-1$
                Messages.getString("AuditProjectSettingPage.DBConfig.dbTypeTip"), //$NON-NLS-1$
                SupportDBUrlStore.getInstance().getDBDisplayNames(), 2, true);
        dbVersionCombo = new LabelledCombo(dbConfigComposite, Messages.getString("AuditProjectSettingPage.DBConfig.dbVersion"), //$NON-NLS-1$
                Messages.getString("AuditProjectSettingPage.DBConfig.dbVersionTip"), //$NON-NLS-1$
                new String[0], 2, true);
        driverText = new LabelledText(dbConfigComposite, Messages.getString("AuditProjectSettingPage.DBConfig.Driver"), 2); //$NON-NLS-1$
        driverText.setReadOnly(true);
        urlText = new LabelledText(dbConfigComposite, Messages.getString("AuditProjectSettingPage.DBConfig.Url"), 2); //$NON-NLS-1$
        usernameText = new LabelledText(dbConfigComposite, Messages.getString("AuditProjectSettingPage.DBConfig.Username"), 2); //$NON-NLS-1$
        passwordText = new LabelledText(dbConfigComposite, Messages.getString("AuditProjectSettingPage.DBConfig.Password"), 2, //$NON-NLS-1$
                SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);

        Composite checkComposite = new Composite(dbConfigGroup, SWT.NULL);
        GridLayout checkCompLayout = new GridLayout(1, false);
        checkCompLayout.marginHeight = 0;
        checkComposite.setLayout(checkCompLayout);
        checkComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));
        checkButton = new Button(checkComposite, SWT.RIGHT_TO_LEFT);
        checkButton.setText(Messages.getString("AuditProjectSettingPage.DBConfig.CheckButtonText")); //$NON-NLS-1$

        Composite historyComposite = new Composite(dbConfigGroup, SWT.NULL);
        GridLayout historyCompLayout = new GridLayout(5, false);
        historyCompLayout.marginHeight = 0;
        historyComposite.setLayout(historyCompLayout);
        historyComposite.setLayoutData(new GridData(SWT.LEFT, SWT.RIGHT, true, true));
        historyCombo = new LabelledCombo(historyComposite, Messages.getString("AuditProjectSettingPage.history.label"), //$NON-NLS-1$
                Messages.getString("AuditProjectSettingPage.history.labelTip"), new String[0], 2, true); //$NON-NLS-1$
        ((GridData) historyCombo.getCombo().getLayoutData()).widthHint = 150;
        refreshButton = new Button(historyComposite, SWT.NULL);
        refreshButton.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        refreshButton.setToolTipText(Messages.getString("AuditProjectSettingPage.history.refreshButton")); //$NON-NLS-1$
        historyGenerateButton = new Button(historyComposite, SWT.NULL);
        historyGenerateButton.setText(Messages.getString("AuditProjectSettingPage.history.historyGenerateButton")); //$NON-NLS-1$
        return dbConfigGroup;
    }

    private void addListeners() {
        generateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                final Map<String, String> results = new HashMap<String, String>();
                final TypedReturnCode<java.sql.Connection> dbResults = new TypedReturnCode<java.sql.Connection>(Boolean.FALSE);
                boolean dbChecked = savedInDBButton.getSelection();
                if (dbChecked) {
                    TypedReturnCode<java.sql.Connection> rc = getConnectionReturnCode();
                    dbResults.setOk(rc.isOk());
                    dbResults.setObject(rc.getObject());
                    dbResults.setMessage(rc.getMessage());
                }
                String url = urlText.getText();
                String driver = driverText.getText();
                String username = usernameText.getText();
                String password = passwordText.getText();
                // select a foder as the generate path
                if (selectGeneratePath()) {
                    final Thread t[] = new Thread[1];
                    ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(
                            PlatformUI.getWorkbench().getDisplay().getActiveShell()) {

                        @Override
                        protected void cancelPressed() {
                            if (t[0] != null) {
                                t[0].interrupt();
                            }
                        }
                    };
                    IRunnableWithProgress runnable = new IRunnableWithProgress() {

                        @Override
                        public void run(IProgressMonitor monitor) throws InterruptedException {
                            t[0] = Thread.currentThread();
                            monitor.beginTask(Messages.getString("AuditProjectSettingPage.generateAuditReportProgressBar"), //$NON-NLS-1$
                                    IProgressMonitor.UNKNOWN);
                            ICommandLineService service = getCommandLineService();
                            if (service != null) {
                                if (dbChecked) {
                                    if (dbResults.isOk()) {
                                        try {
                                            service.populateAudit(url, driver, username, password);
                                            Map<String, String> returnResult = service.generateAuditReport(generatePath);
                                            results.putAll(returnResult);
                                        } catch (Exception e) {
                                            results.put(AuditManager.AUDIT_GENERATE_REPORT_EXCEPTION,
                                                    ExceptionUtils.getFullStackTrace(e));
                                        }
                                    }
                                } else {
                                    String path = "";//$NON-NLS-1$
                                    File tempFolder = null;
                                    try {
                                        File createTempFile = File.createTempFile("AuditReport", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                        path = createTempFile.getPath();
                                        createTempFile.delete();
                                        tempFolder = new File(path);
                                        tempFolder.mkdir();
                                        path = path.replace("\\", "/");//$NON-NLS-1$//$NON-NLS-2$

                                        // Just use the h2 as default if no check
                                        service.populateAudit(
                                                "jdbc:h2:" + path + "/database/audit;AUTO_SERVER=TRUE;lock_timeout=15000", //$NON-NLS-1$ //$NON-NLS-2$
                                                "org.h2.Driver", "tisadmin", "tisadmin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        Map<String, String> returnResult = service.generateAuditReport(generatePath);
                                        results.putAll(returnResult);
                                    } catch (IOException e) {
                                        ExceptionHandler.process(e);
                                    } catch (Exception e) {
                                        results.put(AuditManager.AUDIT_GENERATE_REPORT_EXCEPTION,
                                                ExceptionUtils.getFullStackTrace(e));
                                    } finally {
                                        FilesUtils.deleteFile(tempFolder, true);
                                    }
                                }
                            }
                            monitor.done();
                        }
                    };
                    try {
                        progressDialog.run(true, true, runnable);
                    } catch (InvocationTargetException e1) {
                        ExceptionHandler.process(e1);
                    } catch (InterruptedException e1) {
                        ExceptionHandler.process(e1);
                    }
                }
                // Show information
                showCheckConnectionInformation(false, dbResults);
                showGenerationInformation(results);
            }
        });

        savedInDBButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                reLoad();
            }

        });

        dbTypeCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                String selectedItem = ((Combo) e.getSource()).getText();
                String dbType = SupportDBUrlStore.getInstance().getDBType(selectedItem);
                dbVersionCombo.getCombo().setItems(SupportDBVersions.getDisplayedVersions(dbType));
                String savedDbType = prefManager.getValue(AuditManager.AUDIT_DBTYPE);
                if (savedDbType != null && savedDbType.equals(dbType)) {
                    reLoad();
                } else {
                    if (dbVersionCombo.getCombo().getItemCount() > 0) {
                        dbVersionCombo.getCombo().select(0);
                    }
                    urlText.setText(SupportDBUrlStore.getInstance().getDefaultDBUrl(dbType));
                    String driverClassName = SupportDBUrlStore.getInstance().getDBUrlType(dbType).getDbDriver();
                    if (EDatabaseTypeName.MYSQL.getDisplayName().equalsIgnoreCase(dbType)) {
                        if (EDatabaseVersion4Drivers.MYSQL_8.getVersionValue().equals(getCurrentDBVersion())) {
                            driverClassName = EDatabase4DriverClassName.MYSQL8.getDriverClass();
                        }
                    }
                    driverText.setText(driverClassName);
                    //
                    usernameText.setText("");//$NON-NLS-1$
                    passwordText.setText("");//$NON-NLS-1$
                }
            }
        });

        dbVersionCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                String selectedItem = dbTypeCombo.getCombo().getText();
                String dbType = SupportDBUrlStore.getInstance().getDBType(selectedItem);
                String driverClassName = SupportDBUrlStore.getInstance().getDBUrlType(dbType).getDbDriver();
                if (EDatabaseTypeName.MYSQL.getDisplayName().equalsIgnoreCase(dbType)) {
                    if (EDatabaseVersion4Drivers.MYSQL_8.getVersionValue().equals(getCurrentDBVersion())) {
                        driverClassName = EDatabase4DriverClassName.MYSQL8.getDriverClass();
                    }
                }
                driverText.setText(driverClassName);
            }
        });

        checkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TypedReturnCode<java.sql.Connection> dbResults = getConnectionReturnCode();
                showCheckConnectionInformation(true, dbResults);
            }

        });

        historyCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                String selectedItem = ((Combo) e.getSource()).getText();
                if (StringUtils.isNotEmpty(selectedItem)) {
                    selectedAuditId = getKey(selectedItem);
                    if (selectedAuditId != -1) {
                        historyGenerateButton.setEnabled(true);
                    }
                }
            }
        });

        refreshButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ICommandLineService service = getCommandLineService();
                if (service != null && savedInDBButton.getSelection()) {
                    try {
                        TypedReturnCode<java.sql.Connection> dbResults = getConnectionReturnCode();
                        showCheckConnectionInformation(false, dbResults);
                        if (dbResults.isOk()) {
                            currentParameters = service.listAllHistoryAudits(urlText.getText(), driverText.getText(),
                                    usernameText.getText(), passwordText.getText());
                            String[] items = initHistoryDisplayNames();
                            historyCombo.getCombo().setItems(items);
                            if (items.length > 0) {
                                historyCombo.getCombo().select(0);
                            } else {
                                historyGenerateButton.setEnabled(false);
                            }
                        }
                    } catch (Exception e1) {
                        ExceptionMessageDialog.openWarning(getShell(), "Error", e1);
                    }
                }
            }

        });

        historyGenerateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                final Map<String, String> results = new HashMap<String, String>();
                final TypedReturnCode<java.sql.Connection> dbResults = new TypedReturnCode<java.sql.Connection>(Boolean.FALSE);
                boolean dbChecked = savedInDBButton.getSelection();
                if (dbChecked) {
                    TypedReturnCode<java.sql.Connection> rc = getConnectionReturnCode();
                    dbResults.setOk(rc.isOk());
                    dbResults.setObject(rc.getObject());
                    dbResults.setMessage(rc.getMessage());
                }
                String url = urlText.getText();
                String driver = driverText.getText();
                String username = usernameText.getText();
                String password = passwordText.getText();
                // select a foder as the generate path
                if (selectGeneratePath()) {
                    final Thread t[] = new Thread[1];
                    ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(
                            PlatformUI.getWorkbench().getDisplay().getActiveShell()) {

                        @Override
                        protected void cancelPressed() {
                            if (t[0] != null) {
                                t[0].interrupt();
                            }
                        }
                    };
                    IRunnableWithProgress runnable = new IRunnableWithProgress() {

                        @Override
                        public void run(IProgressMonitor monitor) throws InterruptedException {
                            t[0] = Thread.currentThread();
                            monitor.beginTask(Messages.getString("AuditProjectSettingPage.generateAuditReportProgressBar"), //$NON-NLS-1$
                                    IProgressMonitor.UNKNOWN);
                            ICommandLineService service = getCommandLineService();
                            if (service != null && dbChecked) {
                                if (dbResults.isOk()) {
                                    try {
                                        service.populateHistoryAudit(selectedAuditId, url, driver, username, password);
                                        Map<String, String> returnResult = service.generateAuditReport(generatePath);
                                        results.putAll(returnResult);
                                    } catch (Exception e) {
                                        results.put(AuditManager.AUDIT_GENERATE_REPORT_EXCEPTION,
                                                ExceptionUtils.getFullStackTrace(e));
                                    }
                                }
                            }
                            monitor.done();
                        }
                    };
                    try {
                        progressDialog.run(true, true, runnable);
                    } catch (InvocationTargetException e1) {
                        ExceptionHandler.process(e1);
                    } catch (InterruptedException e1) {
                        ExceptionHandler.process(e1);
                    }
                }
                // Show information
                showCheckConnectionInformation(false, dbResults);
                showGenerationInformation(results);
            }

        });
    }

    private void init() {
        savedInDBButton.setSelection(prefManager.getBoolean(AuditManager.AUDIT_SAVEDINDB));
        reLoad();
    }

    private void reLoad() {
        if (savedInDBButton.getSelection()) {
            dbTypeCombo.setText(prefManager.getValue(AuditManager.AUDIT_DBTYPE));
            dbVersionCombo.setText(prefManager.getValue(AuditManager.AUDIT_DBVERSION));
            driverText.setText(prefManager.getValue(AuditManager.AUDIT_DRIVER));
            urlText.setText(prefManager.getValue(AuditManager.AUDIT_URL));
            usernameText.setText(prefManager.getValue(AuditManager.AUDIT_USERNAME));
            passwordText.setText(StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                    .decrypt(prefManager.getValue(AuditManager.AUDIT_PASSWORD)));
        }
        hideControl(!savedInDBButton.getSelection());
    }

    private void hideControl(boolean hide) {
        dbTypeCombo.setReadOnly(hide);
        dbVersionCombo.setReadOnly(hide);
        urlText.setReadOnly(hide);
        usernameText.setReadOnly(hide);
        passwordText.setReadOnly(hide);
        checkButton.setEnabled(!hide);

        historyCombo.setEnabled(!hide);
        refreshButton.setEnabled(!hide);
        historyGenerateButton.setEnabled(!hide && historyCombo.getSelectionIndex() > -1);
    }

    private void save() {
        if (prefManager != null) {
            prefManager.setValue(AuditManager.AUDIT_DBTYPE, dbTypeCombo.getText());
            prefManager.setValue(AuditManager.AUDIT_DBVERSION, dbVersionCombo.getText());
            prefManager.setValue(AuditManager.AUDIT_DRIVER, driverText.getText());
            prefManager.setValue(AuditManager.AUDIT_URL, urlText.getText());
            prefManager.setValue(AuditManager.AUDIT_USERNAME, usernameText.getText());
            prefManager.setValue(AuditManager.AUDIT_PASSWORD, StudioEncryption
                    .getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM).encrypt(passwordText.getText()));
            prefManager.setValue(AuditManager.AUDIT_SAVEDINDB, savedInDBButton.getSelection());
            prefManager.save();
        }
    }

    private void performDefaultStatus() {
        savedInDBButton.setSelection(false);
        dbTypeCombo.deselectAll();
        dbVersionCombo.deselectAll();
        driverText.setText(""); //$NON-NLS-1$
        urlText.setText("");//$NON-NLS-1$
        usernameText.setText("");//$NON-NLS-1$
        passwordText.setText("");//$NON-NLS-1$
        hideControl(true);
    }

    private boolean selectGeneratePath() {
        DirectoryDialog dial = new DirectoryDialog(getShell(), SWT.NONE);
        String directory = dial.open();
        if (StringUtils.isNotEmpty(directory)) {
            generatePath = Path.fromOSString(directory).toPortableString();
            if (!generatePath.endsWith("/")) { //$NON-NLS-1$
                generatePath += "/"; //$NON-NLS-1$
            }
            return true;
        } else {
            MessageDialog.openError(getShell(), "Error", //$NON-NLS-1$
                    Messages.getString("AuditProjectSettingPage.selectAuditReportFolder")); //$NON-NLS-1$
            return false;
        }
    }

    private String[] initHistoryDisplayNames() {
        String[] items = new String[currentParameters.size()];
        currentParameters.values().toArray(items);
        return items;
    }

    private Integer getKey(String selectedItem) {
        for (Integer key : currentParameters.keySet()) {
            String currentValue = currentParameters.get(key);
            if (selectedItem.equals(currentValue)) {
                return key;
            }
        }
        return -1;
    }

    private TypedReturnCode<java.sql.Connection> getConnectionReturnCode() {
        ICommandLineService service = getCommandLineService();
        if (service != null) {
            return service.checkConnection(getCurrentDBVersion(), urlText.getText(), driverText.getText(), usernameText.getText(),
                    passwordText.getText());
        }
        return new TypedReturnCode<java.sql.Connection>();
    }

    private ICommandLineService getCommandLineService() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICommandLineService.class)) {
            return (ICommandLineService) GlobalServiceRegister.getDefault().getService(ICommandLineService.class);
        }
        return null;
    }

    private String getCurrentDBVersion() {
        String dbType = SupportDBUrlStore.getInstance().getDBType(dbTypeCombo.getText());
        SupportDBUrlType urlType = SupportDBUrlStore.getInstance().getDBUrlType(dbType);
        if (urlType != null) {
            return SupportDBVersions.getVersionValue(urlType, dbVersionCombo.getText());
        }
        return null;
    }

    private void showGenerationInformation(Map<String, String> result) {
        boolean status = Boolean.parseBoolean(result.get(AuditManager.AUDIT_GENERATE_REPORT_STATUS));
        if (status) {
            MessageDialog.openInformation(getShell(), Messages.getString("AuditProjectSettingPage.generate.title"), //$NON-NLS-1$
                    Messages.getString("AuditProjectSettingPage.generate.successful", //$NON-NLS-1$
                            result.get(AuditManager.AUDIT_GENERATE_REPORT_PATH)));
        } else {
            String mainMsg = Messages.getString("AuditProjectSettingPage.generate.failed.message"); //$NON-NLS-1$
            new ErrorDialogWidthDetailArea(getShell(), RepositoryPlugin.PLUGIN_ID, mainMsg,
                    result.get(AuditManager.AUDIT_GENERATE_REPORT_EXCEPTION));
        }
    }

    private void showCheckConnectionInformation(boolean show, TypedReturnCode<java.sql.Connection> result) {
        if (!result.isOk()) {
            String mainMsg = Messages.getString("AuditProjectSettingPage.DBConfig.CheckConnection.failed"); //$NON-NLS-1$
            new ErrorDialogWidthDetailArea(getShell(), RepositoryPlugin.PLUGIN_ID, mainMsg, result.getMessage());
        } else if (result.isOk() && show) {
            MessageDialog.openInformation(getShell(), Messages.getString("AuditProjectSettingPage.DBConfig.CheckButtonText"),result.getMessage()); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        save();
        super.performApply();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        save();
        return super.performOk();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {
    }

    @Override
    protected void performDefaults() {
        performDefaultStatus();
        super.performDefaults();
    }
}