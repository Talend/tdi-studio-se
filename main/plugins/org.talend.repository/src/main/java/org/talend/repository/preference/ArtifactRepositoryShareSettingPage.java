// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.INexusService;
import org.talend.core.nexus.ArtifactRepositoryBean;
import org.talend.core.nexus.IRepositoryArtifactHandler;
import org.talend.core.nexus.RepositoryArtifactHandlerManager;
import org.talend.core.runtime.projectsetting.AbstractProjectSettingPage;
import org.talend.repository.i18n.Messages;

public class ArtifactRepositoryShareSettingPage extends AbstractProjectSettingPage {

    public static final String PREFERENCE_NAME = "org.talend.updates.runtime"; // $NON-NLS-N$

    public static final String PREF_KEY_SHARE_ENABLE = "repository.share.enable"; // $NON-NLS-N$

    public static final String PREF_KEY_SHARE_REPOSITORY_ID = "repository.share.repository.id"; // $NON-NLS-N$

    public static final String DEFAULT_REPOSITORY_ID = "component-updates"; // $NON-NLS-N$

    public static final String PREF_KEY_LAST_CHECK_UPDATE_TIME = "lastCheckUpdateTime"; //$NON-NLS-1$

    public static final String PREF_KEY_CHECK_UPDATE_PER_DAYS = "checkUpdatePerDays"; //$NON-NLS-1$

    public static final String PREF_KEY_AUTO_CHECK_UPDATE = "autoCheckUpdate"; //$NON-NLS-1$

    public static final String PREF_KEY_SHOW_WARN_DIALOG_WHEN_INSTALLING_FEATURES = "showWarnDialogWhenInstallingFeatures"; //$NON-NLS-1$

    /**
     * If 0, then means will check update for each startup
     */
    private static final int CHECK_UPDATE_PER_DAYS_MIN = 0;

    private static final int CHECK_UPDATE_PER_DAYS_MAX = 365000;

    private final Image IMG_UNKNOWN = ImageProvider.getImage(EImage.UNKNOWN_ICON);

    private final Image IMG_OK = ImageProvider.getImage(EImage.OK);

    private final Image IMG_ERROR = ImageProvider.getImage(EImage.ERROR_ICON);

    private IRepositoryArtifactHandler repositoryHandler;

    private Button enableShareCheckbox, checkButton;

    private Button autoCheckUpdateBtn;

    private Button showWarnDialogWhenInstallingFeaturesBtn;

    private Text repositoryIdText;

    private Text checkUpdatePerDaysText;

    private Label statusLabel;

    private Label checkUpdatePerDaysTextLabel;

    public ArtifactRepositoryShareSettingPage() {
        super();
        noDefaultAndApplyButton();
    }

    @Override
    protected String getPreferenceName() {
        return PREFERENCE_NAME;
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        parent.setLayout(new GridLayout(4, false));
        enableShareCheckbox = new Button(parent, SWT.CHECK);
        enableShareCheckbox.setText(Messages.getString("ArtifactRepositoryShareSettingPage.enableShareLabel")); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(4, 1).applyTo(enableShareCheckbox);
        Label repositoryIdLabel = new Label(parent, SWT.NONE);
        repositoryIdLabel.setText(Messages.getString("ArtifactRepositoryShareSettingPage.repositoryIdLabel")); //$NON-NLS-1$
        repositoryIdText = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(repositoryIdText);
        checkButton = new Button(parent, SWT.PUSH);
        checkButton.setText(Messages.getString("ArtifactRepositoryShareSettingPage.checkLabel")); //$NON-NLS-1$
        statusLabel = new Label(parent, SWT.NONE);
        statusLabel.setImage(IMG_UNKNOWN);

        showWarnDialogWhenInstallingFeaturesBtn = new Button(parent, SWT.CHECK);
        showWarnDialogWhenInstallingFeaturesBtn
                .setText(Messages.getString("ArtifactRepositoryShareSettingPage.showWarnDialogWhenInstallingFeatures")); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(4, 1).applyTo(showWarnDialogWhenInstallingFeaturesBtn);

        autoCheckUpdateBtn = new Button(parent, SWT.CHECK);
        autoCheckUpdateBtn.setText(Messages.getString("ArtifactRepositoryShareSettingPage.autoCheckUpdate")); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(4, 1).applyTo(autoCheckUpdateBtn);

        checkUpdatePerDaysTextLabel = new Label(parent, SWT.NONE);
        checkUpdatePerDaysTextLabel.setText(Messages.getString("ArtifactRepositoryShareSettingPage.checkUpdatePerDays")); //$NON-NLS-1$
        checkUpdatePerDaysText = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().span(3, 1).applyTo(checkUpdatePerDaysText);

        initFields();
        addListeners();
        validate();
    }

    private void initFields() {
        boolean enableShare = getPreferenceStore().getBoolean(PREF_KEY_SHARE_ENABLE);
        enableShareCheckbox.setSelection(enableShare);
        repositoryIdText.setText(getPreferenceStore().getString(PREF_KEY_SHARE_REPOSITORY_ID));
        showWarnDialogWhenInstallingFeaturesBtn
                .setSelection(getPreferenceStore().getBoolean(PREF_KEY_SHOW_WARN_DIALOG_WHEN_INSTALLING_FEATURES));
        autoCheckUpdateBtn.setSelection(getPreferenceStore().getBoolean(PREF_KEY_AUTO_CHECK_UPDATE));
        checkUpdatePerDaysText.setText(getPreferenceStore().getString(PREF_KEY_CHECK_UPDATE_PER_DAYS));

        updateFields(enableShare);
        setValid(false);
    }

    private void addListeners() {
        enableShareCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean select = enableShareCheckbox.getSelection();
                updateFields(select);
                setValid(!select);
            }
            
        });

        repositoryIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (statusLabel.getImage() != IMG_UNKNOWN) {
                    statusLabel.setImage(IMG_UNKNOWN);
                }
                setValid(false);
            }
        });

        checkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean success = checkRepositoryConnection();
                statusLabel.setImage(success ? IMG_OK : IMG_ERROR);
                setValid(success);
            }

        });

        autoCheckUpdateBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onCheckUpdateBtnSelected();
            }
        });

        checkUpdatePerDaysText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        checkUpdatePerDaysText.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                onCheckUpdatePerDaysTextVerify(e);
            }
        });
    }

    private void onCheckUpdatePerDaysTextVerify(VerifyEvent e) {
        if (!checkInteger(e)) {
            return;
        }
        String text = checkUpdatePerDaysText.getText();
        String newText = text.substring(0, e.start) + e.text;
        if (e.end < text.length()) {
            newText = newText + text.substring(e.end);
        }

        try {
            int i = Integer.valueOf(newText);
            if (i < CHECK_UPDATE_PER_DAYS_MIN || CHECK_UPDATE_PER_DAYS_MAX < i) {
                e.doit = false;
            }
        } catch (Exception ex) {
            // nothing to do
        }
    }

    private boolean checkInteger(VerifyEvent e) {
        try {
            if (StringUtils.isNotBlank(e.text)) {
                Integer i = Integer.valueOf(e.text);
                if (i <= 0) {
                    e.doit = false;
                }
            }
        } catch (Exception ex) {
            e.doit = false;
        } finally {
            if (!e.doit) {
                return e.doit;
            }
        }
        if (e.character != 0 && e.keyCode != SWT.BS && e.keyCode != SWT.DEL && !Character.isDigit(e.character)) {
            e.doit = false;
        } else {
            if (e.character == '0' && e.start == 0) {
                e.doit = false;
            } else {
                e.doit = true;
            }
        }
        return e.doit;
    }

    private void onCheckUpdateBtnSelected() {
        validate();
    }

    private void checkPerDays() {
        boolean isChecked = autoCheckUpdateBtn.getSelection();
        checkUpdatePerDaysTextLabel.setEnabled(isChecked);
        checkUpdatePerDaysText.setEnabled(isChecked);
    }

    private void validate() {
        setErrorMessage(null);
        setMessage(null);
        checkPerDays();
        boolean validate = true;
        if (!validateCheckPerDays()) {
            validate = false;
        }
        setValid(validate);
    }

    private boolean validateCheckPerDays() {
        boolean result = true;
        boolean isAutoCheckUpdateSelected = autoCheckUpdateBtn.getSelection();

        if (isAutoCheckUpdateSelected) {
            String days = checkUpdatePerDaysText.getText();
            if (StringUtils.isBlank(days)) {
                result = false;
            } else {
                try {
                    Integer.valueOf(days);
                } catch (Exception e) {
                    result = false;
                }
            }
        }
        if (!result) {
            setErrorMessage(Messages.getString("ArtifactRepositoryShareSettingPage.checkUpdatePerDays.error", //$NON-NLS-1$
                    CHECK_UPDATE_PER_DAYS_MIN, CHECK_UPDATE_PER_DAYS_MAX,
                    Messages.getString("ArtifactRepositoryShareSettingPage.checkUpdatePerDays"))); //$NON-NLS-1$
        }
        return result;
    }

    private boolean checkRepositoryConnection() {
        INexusService nexusService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(INexusService.class)) {
            nexusService = (INexusService) GlobalServiceRegister.getDefault().getService(INexusService.class);
        }
        if (nexusService == null) {
            return false;
        }
        ArtifactRepositoryBean artifactRepository = nexusService.getArtifactRepositoryFromServer();
        if (artifactRepository == null) {
            return false;
        }
        String repositoryId = repositoryIdText.getText();
        if (StringUtils.isBlank(repositoryId)) {
            return false;
        }
        artifactRepository.setRepositoryId(repositoryId);
        if (repositoryHandler == null) {
            repositoryHandler = RepositoryArtifactHandlerManager.getRepositoryHandler(artifactRepository);
        }
        return repositoryHandler.checkConnection();
    }

    @Override
    public boolean performOk() {
        if (!isValid()) {
            return false;
        }
        if (enableShareCheckbox != null && !enableShareCheckbox.isDisposed()) {
            getPreferenceStore().setValue(PREF_KEY_SHARE_ENABLE, enableShareCheckbox.getSelection());
            if (enableShareCheckbox.getSelection()) {
                getPreferenceStore().setValue(PREF_KEY_SHARE_REPOSITORY_ID, repositoryIdText.getText());
            }
            getPreferenceStore().setValue(PREF_KEY_AUTO_CHECK_UPDATE, autoCheckUpdateBtn.getSelection());
            getPreferenceStore().setValue(PREF_KEY_SHOW_WARN_DIALOG_WHEN_INSTALLING_FEATURES,
                    showWarnDialogWhenInstallingFeaturesBtn.getSelection());
            getPreferenceStore().setValue(PREF_KEY_CHECK_UPDATE_PER_DAYS, checkUpdatePerDaysText.getText());
        }
        return super.performOk();
    }

    private void updateFields(boolean select) {
        repositoryIdText.setEnabled(select);
        checkButton.setEnabled(select);
        statusLabel.setImage(IMG_UNKNOWN);
    }

    @Override
    public boolean isValid() {
        validate();
        return super.isValid();
    }

}
