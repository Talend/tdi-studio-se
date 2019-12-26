package org.talend.repository.preference;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.nexus.ArtifactRepositoryBean;
import org.talend.core.nexus.ArtifactRepositoryBean.NexusType;
import org.talend.core.nexus.IRepositoryArtifactHandler;
import org.talend.core.nexus.RepositoryArtifactHandlerManager;
import org.talend.core.nexus.TalendLibsServerManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.utils.sugars.TypedReturnCode;

public class ArtifactProxySettingForm extends AbstractArtifactProxySettingForm {

    private Combo artifactType;

    private Text urlText;

    private Text usernameText;

    private Text talendLibPasswordText;

    private Text repositoryIdText;

    private Button enableProxySettingBtn;

    private Button checkConnectionBtn;

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

        // compute size
        Label artifactTypeLabel = new Label(talendLibgroup, SWT.NONE);
        artifactTypeLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.artifactType"));
        Point artifactTypeLabelSize = artifactTypeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        Label urlLabel = new Label(talendLibgroup, SWT.NONE);
        urlLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.url"));
        Point urlLabelSize = urlLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        Label usernameLabel = new Label(talendLibgroup, SWT.NONE);
        usernameLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.username"));
        Point usernameLabelSize = usernameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        Label talendLibpasswordLabel = new Label(talendLibgroup, SWT.NONE);
        talendLibpasswordLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.password"));
        Point talendLibpasswordLabelSize = talendLibpasswordLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        Label repositoryIdLabel = new Label(talendLibgroup, SWT.NONE);
        repositoryIdLabel.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.repositoryId"));
        Point repositoryIdLabelSize = repositoryIdLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        int maxLabelWidth = getMaxLabelWidth(artifactTypeLabelSize.x, urlLabelSize.x, usernameLabelSize.x,
                talendLibpasswordLabelSize.x, repositoryIdLabelSize.x);

        // enable checkbox
        enableProxySettingBtn = new Button(talendLibgroup, SWT.CHECK);
        enableProxySettingBtn
                .setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.enableProxySetting"));
        formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        enableProxySettingBtn.setLayoutData(formData);
        // artifact type
        formData = new FormData();
        formData.left = new FormAttachment(talendLibgroup, 0, SWT.LEFT);
        formData.top = new FormAttachment(enableProxySettingBtn, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.width = maxLabelWidth;
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
        formData = new FormData();
        formData.top = new FormAttachment(artifactType, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        formData.width = maxLabelWidth;
        urlLabel.setLayoutData(formData);
        urlText = new Text(talendLibgroup, SWT.BORDER);
        formData = new FormData();
        formData.top = new FormAttachment(urlLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(urlLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        urlText.setLayoutData(formData);
        // username
        formData = new FormData();
        formData.top = new FormAttachment(urlText, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        usernameLabel.setLayoutData(formData);
        formData.width = maxLabelWidth;
        usernameText = new Text(talendLibgroup, SWT.BORDER);
        formData = new FormData();
        formData.top = new FormAttachment(usernameLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(usernameLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        usernameText.setLayoutData(formData);
        // password
        formData = new FormData();
        formData.top = new FormAttachment(usernameText, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        formData.width = maxLabelWidth;
        talendLibpasswordLabel.setLayoutData(formData);
        talendLibPasswordText = new Text(talendLibgroup, SWT.BORDER | SWT.PASSWORD);
        formData = new FormData();
        formData.top = new FormAttachment(talendLibpasswordLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(talendLibpasswordLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        talendLibPasswordText.setLayoutData(formData);
        // repository id
        formData = new FormData();
        formData.top = new FormAttachment(talendLibPasswordText, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.left = new FormAttachment(artifactTypeLabel, 0, SWT.LEFT);
        formData.width = maxLabelWidth;
        repositoryIdLabel.setLayoutData(formData);
        repositoryIdText = new Text(talendLibgroup, SWT.BORDER);
        formData = new FormData();
        formData.top = new FormAttachment(repositoryIdLabel, 0, SWT.CENTER);
        formData.left = new FormAttachment(repositoryIdLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        repositoryIdText.setLayoutData(formData);
        // check connection buttion
        checkConnectionBtn = new Button(talendLibgroup, SWT.NONE);
        checkConnectionBtn.setText(Messages.getString("ProjectSettingPage.ArtifactProxySetting.checkConnection"));
        formData = new FormData();
        formData.top = new FormAttachment(repositoryIdText, ALIGN_VERTICAL_INNER, SWT.BOTTOM);
        formData.right = new FormAttachment(100);
        checkConnectionBtn.setLayoutData(formData);
        // talend lib group end
    }

    private int getMaxLabelWidth(int... pointXs) {
        Arrays.sort(pointXs);
        return pointXs[pointXs.length - 1];
    }
    private void addListeners() {
        enableProxySettingBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (enableProxySettingBtn.getSelection()) {
                    disableAllText(true);
                    updateButtons();
                } else {
                    disableAllText(false);
                    updateButtons();
                }
            }
        });
        checkConnectionBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ArtifactRepositoryBean serverBean = getServerBean();
                IRepositoryArtifactHandler repHander = RepositoryArtifactHandlerManager.getRepositoryHandler(serverBean);
                TypedReturnCode<HttpResponse> connectionResultAndCode = repHander.getConnectionResultAndCode();
                showCheckConnectionInformation(true, connectionResultAndCode);
            }
        });
    }

    private void disableAllText(boolean checked) {
        urlText.setEnabled(checked);
        usernameText.setEnabled(checked);
        talendLibPasswordText.setEnabled(checked);
        repositoryIdText.setEnabled(checked);
        checkConnectionBtn.setEnabled(checked);
    }

    @Override
    public boolean isComplete() {
        // nothing to check
        return true;
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
        return super.performApply();
    }

    @Override
    public void performDefaults() {
        super.performDefaults();
        talendLibRestore();
    }

    private void talendLibRestore() {
        urlText.setText("");
        urlText.setEnabled(false);
        usernameText.setText("");
        usernameText.setEnabled(false);
        talendLibPasswordText.setText("");
        talendLibPasswordText.setEnabled(false);
        repositoryIdText.setText("");
        repositoryIdText.setEnabled(false);
        artifactType.setText(ArtifactRepositoryBean.NexusType.NEXUS_3.name());
        enableProxySettingBtn.setSelection(false);
        prefManager.setValue(TalendLibsServerManager.ENABLE_PROXY_SETTING, false);
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_URL, "");
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_USERNAME, "");
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_PASSWORD, "");
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_REPOSITORY_ID, "");
        prefManager.setValue(TalendLibsServerManager.NEXUS_PROXY_TYPE, "");
        prefManager.save();
    }
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
            checkConnectionBtn.setEnabled(false);
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
        String username = usernameText.getText();
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
    }

    private ArtifactRepositoryBean getServerBean() {
        ArtifactRepositoryBean serverBean = new ArtifactRepositoryBean();
        serverBean.setServer(urlText.getText());
        serverBean.setRepositoryId(repositoryIdText.getText());
        serverBean.setUserName(usernameText.getText());
        serverBean.setPassword(talendLibPasswordText.getText());
        serverBean.setType(artifactType.getText());
        return serverBean;
    }

    private void showCheckConnectionInformation(boolean show, TypedReturnCode<HttpResponse> result) {
        if (!result.isOk()) {
            String mainMsg = Messages.getString("ProjectSettingPage.ArtifactProxySetting.connectionFailureMsg"); //$NON-NLS-1$
            new ErrorDialogWidthDetailArea(getShell(), RepositoryPlugin.PLUGIN_ID, mainMsg, result.getMessage());
        } else if (result.isOk() && show) {
            MessageDialog.openInformation(getShell(),
                    Messages.getString("ProjectSettingPage.ArtifactProxySetting.checkConnection"), //$NON-NLS-1$
                    result.getMessage());
        }
    }
}
