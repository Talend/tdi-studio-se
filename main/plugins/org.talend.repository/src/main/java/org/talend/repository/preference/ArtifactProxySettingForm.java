package org.talend.repository.preference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.core.nexus.ArtifactRepositoryBean;
import org.talend.core.nexus.ArtifactRepositoryBean.NexusType;
import org.talend.core.nexus.TalendLibsServerManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.repository.i18n.Messages;

public class ArtifactProxySettingForm extends AbstractArtifactProxySettingForm {

    private Combo artifactType;

    private Text urlText;

    private Text usernameText;

    private Text talendLibPasswordText;

    private Text repositoryIdText;

    private Button enableProxySettingBtn;

    private ProjectPreferenceManager prefManager;

    public ArtifactProxySettingForm(Composite parent, int style) {
        super(parent, style);
        createControl();
        addListeners();
        loadData();
    }

    private void createControl() {
        Composite parent = this;

        Composite container = createFormContainer(parent);
        int ALIGN_HORIZON = getAlignHorizon();
        int ALIGN_VERTICAL_INNER = getAlignVerticalInner();
        int ALIGN_VERTICALs_INNER2 = ALIGN_VERTICAL_INNER - ALIGN_VERTICAL_INNER / 2;
        int ALIGN_VERTICAL = getAlignVertical();
        int MARGIN_GROUP = 5;

        // talend lib group begin ========================
        Group talendLibgroup = new Group(container, SWT.NONE);
        talendLibgroup.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.groupNameTalendLib")); //$NON-NLS-1$
        FormData formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.top = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        talendLibgroup.setLayoutData(formData);
        FormLayout formLayout = new FormLayout();
        formLayout.marginTop = MARGIN_GROUP;
        formLayout.marginBottom = MARGIN_GROUP;
        formLayout.marginLeft = MARGIN_GROUP;
        formLayout.marginRight = MARGIN_GROUP;
        talendLibgroup.setLayout(formLayout);
        // enable checkbox
        enableProxySettingBtn = new Button(talendLibgroup, SWT.CHECK);
        enableProxySettingBtn
                .setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.enableProxySetting"));
        formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        enableProxySettingBtn.setLayoutData(formData);
        // artifact type
        Label artifactTypeLabel = new Label(talendLibgroup, SWT.NONE);
        artifactTypeLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.artifactType"));
        formData = new FormData();
        formData.left = new FormAttachment(talendLibgroup, 0, SWT.LEFT);
        formData.top = new FormAttachment(enableProxySettingBtn, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        artifactTypeLabel.setLayoutData(formData);
        artifactType = new Combo(talendLibgroup, SWT.PUSH);
        for (NexusType type : ArtifactRepositoryBean.NexusType.values()) {
            artifactType.add(type.name());
        }
        formData = new FormData();
        formData.left = new FormAttachment(artifactTypeLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.top = new FormAttachment(artifactTypeLabel, 0, SWT.CENTER);
        artifactType.setLayoutData(formData);
        artifactType.setText(ArtifactRepositoryBean.NexusType.NEXUS_3.name());
        // URL
        Label urlLabel = new Label(talendLibgroup, SWT.NONE);
        urlLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.url"));
        formData = new FormData();
        formData.top = new FormAttachment(artifactType, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        urlLabel.setLayoutData(formData);
        urlText = new Text(talendLibgroup, SWT.BORDER);
        formData = new FormData();
        formData.top = new FormAttachment(urlLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(urlLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        urlText.setLayoutData(formData);
        // username
        Label usernameLabel = new Label(talendLibgroup, SWT.NONE);
        usernameLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.username"));
        formData = new FormData();
        formData.top = new FormAttachment(urlText, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        usernameLabel.setLayoutData(formData);
        usernameText = new Text(talendLibgroup, SWT.BORDER);
        formData = new FormData();
        formData.top = new FormAttachment(usernameLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(usernameLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        usernameText.setLayoutData(formData);
        // password
        Label talendLibpasswordLabel = new Label(talendLibgroup, SWT.NONE);
        talendLibpasswordLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.password"));
        formData = new FormData();
        formData.top = new FormAttachment(usernameText, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        talendLibpasswordLabel.setLayoutData(formData);
        talendLibPasswordText = new Text(talendLibgroup, SWT.BORDER | SWT.PASSWORD);
        formData = new FormData();
        formData.top = new FormAttachment(talendLibpasswordLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(talendLibpasswordLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        talendLibPasswordText.setLayoutData(formData);
        // repository id
        Label repositoryIdLabel = new Label(talendLibgroup, SWT.NONE);
        repositoryIdLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.repositoryId"));
        formData = new FormData();
        formData.top = new FormAttachment(talendLibPasswordText, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        repositoryIdLabel.setLayoutData(formData);
        repositoryIdText = new Text(talendLibgroup, SWT.BORDER);
        formData = new FormData();
        formData.top = new FormAttachment(repositoryIdLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(repositoryIdLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        repositoryIdText.setLayoutData(formData);
        // talend lib group end =============================
    }

    private void addListeners() {
        enableProxySettingBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (enableProxySettingBtn.getSelection()) {
                    disableAllText(true);
                } else {
                    disableAllText(false);
                }
            }
        });
    }

    private void disableAllText(boolean checked) {
        urlText.setEnabled(checked);
        usernameText.setEnabled(checked);
        talendLibPasswordText.setEnabled(checked);
        repositoryIdText.setEnabled(checked);
    }

    @Override
    public boolean isComplete() {
        showMessage(null, WizardPage.NONE);
        boolean checkUsernamePassword = true;
        return checkUsernamePassword;
    }

    @Override
    public boolean canFlipToNextPage() {
        return isComplete();
    }

    @Override
    public boolean canFinish() {
        if (isComplete()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean performOk() {
        save();
        boolean isReadonly = isReadonly();
        if (isReadonly) {
            return super.performOk();
        }
        boolean isOk = performApply();
        if (!isOk) {
            return false;
        }
        return super.performOk();
    }

    @Override
    public boolean performApply() {
        save();
        boolean isReadonly = isReadonly();
        if (isReadonly) {
            return super.performApply();
        }
        /*
         * try { IDynamicDistributionsGroup selectedSetupDynamicDistriGroup = getSelectedSetupDynamicDistriGroup(); if
         * (selectedSetupDynamicDistriGroup != null) { IDynamicDistributionPreference dynamicDistributionPreference =
         * selectedSetupDynamicDistriGroup
         * .getDynamicDistributionPreference(ProjectManager.getInstance().getCurrentProject()); if
         * (dynamicDistributionPreference != null) { boolean changed = false;
         * 
         * boolean isAnonymous = anonymousBtn.getSelection(); changed = changed ||
         * dynamicDistributionPreference.isAnonymous() != isAnonymous;
         * dynamicDistributionPreference.setAnonymous(isAnonymous);
         * 
         * boolean overrideDefaultSetup = overrideDefaultSetupBtn.getSelection(); changed = changed ||
         * dynamicDistributionPreference.overrideDefaultSetup() != overrideDefaultSetup;
         * dynamicDistributionPreference.setOverrideDefaultSetup(overrideDefaultSetup);
         * 
         * String password = passwordText.getText(); changed = changed ||
         * !StringUtils.equals(dynamicDistributionPreference.getPassword(), password);
         * dynamicDistributionPreference.setPassword(password);
         * 
         * String repository = repositoryText.getText(); changed = changed ||
         * !StringUtils.equals(dynamicDistributionPreference.getRepository(), repository);
         * dynamicDistributionPreference.setRepository(repository);
         * 
         * String username = userText.getText(); changed = changed ||
         * !StringUtils.equals(dynamicDistributionPreference.getUsername(), username);
         * dynamicDistributionPreference.setUsername(username);
         * 
         * dynamicDistributionPreference.save(); isComplete();
         * 
         * if (changed) { reloadDynamicDistributions(); } } } } catch (Throwable e) { ExceptionHandler.process(e);
         * String message = e.getMessage(); if (StringUtils.isEmpty(message)) { message =
         * Messages.getString("ExceptionDialog.message.empty"); //$NON-NLS-1$ }
         * ExceptionMessageDialog.openError(getShell(), Messages.getString("ExceptionDialog.title"), message, e);
         * //$NON-NLS-1$ }
         */
        return super.performApply();
    }

    @Override
    public void performDefaults() {
        /*
         * boolean isReadonly = isReadonly(); if (isReadonly) { super.performDefaults(); return; } boolean agree =
         * MessageDialog.openConfirm(getShell(),
         * Messages.getString("DynamicDistributionPreferenceForm.performDefaults.confirm.title"), //$NON-NLS-1$
         * Messages.getString("DynamicDistributionPreferenceForm.performDefaults.confirm.message")); //$NON-NLS-1$ if
         * (!agree) { return; } try { IDynamicDistributionsGroup selectedSetupDynamicDistriGroup =
         * getSelectedSetupDynamicDistriGroup(); if (selectedSetupDynamicDistriGroup != null) {
         * IDynamicDistributionPreference dynamicDistributionPreference = selectedSetupDynamicDistriGroup
         * .getDynamicDistributionPreference(ProjectManager.getInstance().getCurrentProject()); if
         * (dynamicDistributionPreference != null) { boolean changed = false;
         * 
         * boolean isAnonymous = dynamicDistributionPreference.getDefaultIsAnonymous(); changed = changed ||
         * dynamicDistributionPreference.isAnonymous() != isAnonymous;
         * dynamicDistributionPreference.setAnonymous(isAnonymous);
         * 
         * boolean overrideDefaultSetup = dynamicDistributionPreference.getDefaultOverrideDefaultSetup(); changed =
         * changed || dynamicDistributionPreference.overrideDefaultSetup() != overrideDefaultSetup;
         * dynamicDistributionPreference.setOverrideDefaultSetup(overrideDefaultSetup);
         * 
         * String password = dynamicDistributionPreference.getDefaultPassword(); changed = changed ||
         * !StringUtils.equals(dynamicDistributionPreference.getPassword(), password);
         * dynamicDistributionPreference.setPassword(password);
         * 
         * String repository = dynamicDistributionPreference.getDefaultRepository(); changed = changed ||
         * !StringUtils.equals(dynamicDistributionPreference.getRepository(), repository);
         * dynamicDistributionPreference.setRepository(repository);
         * 
         * String username = dynamicDistributionPreference.getDefaultUsername(); changed = changed ||
         * !StringUtils.equals(dynamicDistributionPreference.getUsername(), username);
         * dynamicDistributionPreference.setUsername(username);
         * 
         * dynamicDistributionPreference.save(); loadRepositorySetupGroup(); isComplete();
         * 
         * if (changed) { reloadDynamicDistributions(); } } } } catch (Throwable e) { ExceptionHandler.process(e);
         * String message = e.getMessage(); if (StringUtils.isEmpty(message)) { message =
         * Messages.getString("ExceptionDialog.message.empty"); //$NON-NLS-1$ }
         * ExceptionMessageDialog.openError(getShell(), Messages.getString("ExceptionDialog.title"), message, e);
         * //$NON-NLS-1$ }
         */
        super.performDefaults();
    }

    /*
     * private void reloadDynamicDistributions() throws Throwable { final Throwable throwable[] = new Throwable[1];
     * ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell()); progressDialog.run(true, false, new
     * IRunnableWithProgress() {
     * 
     * @Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
     * monitor.beginTask(Messages.getString("DynamicDistributionPreferenceForm.progress.reload"), //$NON-NLS-1$
     * IProgressMonitor.UNKNOWN); try { BigDataBasicUtil.reloadAllDynamicDistributions(monitor); } catch (Exception e) {
     * throwable[0] = e; } } }); if (throwable[0] != null) { throw throwable[0]; } }
     */

    private boolean isReadonly() {
        return ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject();
    }

    private void loadData() {
        //the configuration in ini file override project setting
        String proxyUrl = System.getProperty(TalendLibsServerManager.NEXUS_PROXY_URL);
        String username = System.getProperty(TalendLibsServerManager.NEXUS_PROXY_USERNAME);
        String password = System.getProperty(TalendLibsServerManager.NEXUS_PROXY_PASSWORD);
        String repositoryId = System.getProperty(TalendLibsServerManager.NEXUS_PROXY_REPOSITORY_ID);
        String type = System.getProperty(TalendLibsServerManager.NEXUS_PROXY_TYPE);
        boolean enableFlag = StringUtils.isNotEmpty(proxyUrl)||StringUtils.isNotEmpty(type)||StringUtils.isNotEmpty(repositoryId);;
        prefManager = new ProjectPreferenceManager("org.talend.proxy.nexus", true);
        if(enableFlag) {
            prefManager.setValue(TalendLibsServerManager.ENABLE_PROXY_SETTING, enableFlag);
            prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_URL, proxyUrl);
            prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_USERNAME, username);
            prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_PASSWORD, password);
            prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_REPOSITORY_ID, repositoryId);
            prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_TYPE, type);
            prefManager.save();
        }
        boolean enableProxy = prefManager.getBoolean(TalendLibsServerManager.ENABLE_PROXY_SETTING);
        if (!enableProxy) {
            urlText.setEnabled(false);
            usernameText.setEnabled(false);
            talendLibPasswordText.setEnabled(false);
            repositoryIdText.setEnabled(false);
            return;
        }
        enableProxySettingBtn.setSelection(enableProxy);
        urlText.setText(prefManager.getValue(TalendLibsServerManager.NEXUS_PROXY_URL));
        usernameText.setText(prefManager.getValue(TalendLibsServerManager.NEXUS_PROXY_USERNAME));
        talendLibPasswordText.setText(prefManager.getValue(TalendLibsServerManager.NEXUS_PROXY_PASSWORD));
        repositoryIdText.setText(prefManager.getValue(TalendLibsServerManager.NEXUS_PROXY_REPOSITORY_ID));
        artifactType.setText(prefManager.getValue(TalendLibsServerManager.NEXUS_PROXY_TYPE));
    }

    private void save() {
        boolean enableFlag = enableProxySettingBtn.getSelection();
        String url = urlText.getText();
        String username = urlText.getText();
        String password = talendLibPasswordText.getText();
        String repositoryId = repositoryIdText.getText();
        String type = artifactType.getText();
        prefManager.setValue(TalendLibsServerManager.ENABLE_PROXY_SETTING, enableFlag);
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_URL, url);
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_USERNAME, username);
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_PASSWORD, password);
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_REPOSITORY_ID, repositoryId);
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_TYPE, type);
        prefManager.save();
        /*
         * if (enableFlag) { System.setProperty(TalendLibsServerManager.NEXUS_PROXY_URL, url);
         * System.setProperty(TalendLibsServerManager.NEXUS_PROXY_USERNAME, username);
         * System.setProperty(TalendLibsServerManager.NEXUS_PROXY_PASSWORD, password);
         * System.setProperty(TalendLibsServerManager.NEXUS_PROXY_REPOSITORY_ID, repositoryId);
         * System.setProperty(TalendLibsServerManager.NEXUS_PROXY_TYPE, type); } else {
         * System.clearProperty(TalendLibsServerManager.NEXUS_PROXY_URL);
         * System.clearProperty(TalendLibsServerManager.NEXUS_PROXY_USERNAME);
         * System.clearProperty(TalendLibsServerManager.NEXUS_PROXY_PASSWORD);
         * System.clearProperty(TalendLibsServerManager.NEXUS_PROXY_REPOSITORY_ID);
         * System.clearProperty(TalendLibsServerManager.NEXUS_PROXY_TYPE); }
         */
    }

}
