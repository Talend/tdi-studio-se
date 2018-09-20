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

    private final Image IMG_UNKNOWN = ImageProvider.getImage(EImage.UNKNOWN_ICON);

    private final Image IMG_OK = ImageProvider.getImage(EImage.OK);

    private final Image IMG_ERROR = ImageProvider.getImage(EImage.ERROR_ICON);

    private IRepositoryArtifactHandler repositoryHandler;

    private Button enableShareCheckbox, checkButton;

    private Text repositoryIdText;

    private Label statusLabel;

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
        initFields();
        addListeners();
    }

    private void initFields() {
        enableShareCheckbox.setSelection(getPreferenceStore().getBoolean(PREF_KEY_SHARE_ENABLE));
        repositoryIdText.setText(getPreferenceStore().getString(PREF_KEY_SHARE_REPOSITORY_ID));
    }

    private void addListeners() {
        enableShareCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean select = enableShareCheckbox.getSelection();
                repositoryIdText.setEnabled(select);
                checkButton.setEnabled(select);
                setValid(!select);
                statusLabel.setImage(IMG_UNKNOWN);
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
        if (enableShareCheckbox != null && !enableShareCheckbox.isDisposed()) {
            getPreferenceStore().setValue(PREF_KEY_SHARE_ENABLE, enableShareCheckbox.getSelection());
            if (enableShareCheckbox.getSelection()) {
                getPreferenceStore().setValue(PREF_KEY_SHARE_REPOSITORY_ID, repositoryIdText.getText());
            }
        }
        return super.performOk();
    }

}
