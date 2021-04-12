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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.i18n.Messages;
import org.talend.registration.wizards.register.TalendForgeDialog;
import org.talend.repository.ProjectManager;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class ExchangePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private Button logonButton;

    Link userAccountLink;

    private String userAccount;

    IPreferenceStore prefStore = PlatformUI.getPreferenceStore();

    public ExchangePreferencePage() {
        super(GRID);
        initData();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
        composite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setLayout(new GridLayout());

        Group eGroup = new Group(composite, SWT.NONE);
        eGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        eGroup.setLayout(new GridLayout(3, false));

        Label userLabelTitle = new Label(eGroup, SWT.NONE);
        userLabelTitle.setText(Messages.getString("ExchangePreferencePage.currentUserTitle")); //$NON-NLS-1$
        GridData data = new GridData(GridData.FILL, GridData.CENTER, true, false);
        userLabelTitle.setLayoutData(data);

        userAccountLink = new Link(eGroup, SWT.NONE);
        GridData dataLink = new GridData(GridData.FILL, GridData.CENTER, true, false);
        dataLink.widthHint = convertVerticalDLUsToPixels(120);
        userAccountLink.setLayoutData(dataLink);
        userAccountLink.setText(userAccount != null ? "<a> " + userAccount + " </a>" : "<a> " + "< None >" + " </a>");

        logonButton = new Button(eGroup, SWT.PUSH);
        logonButton.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        logonButton.setText(userAccount != null ? Messages.getString("ExchangePreferencePage.signOutExchangeTitle") : Messages
                .getString("ExchangePreferencePage.signInExchangeTitle"));

        if (PluginChecker.isRemoteProviderPluginLoaded()) {
            BooleanFieldEditor checkTisLogonExchangeDialog = new BooleanFieldEditor(
                    ITalendCorePrefConstants.EXCHANGE_CHECK_TIS_VERSION,
                    Messages.getString("ExchangePreferencePage.checkTisLogonExchangeTitle"), getFieldEditorParent()); //$NON-NLS-1$
            addField(checkTisLogonExchangeDialog);
        }

        BooleanFieldEditor downloadedCheckUpdates = new BooleanFieldEditor(
                ITalendCorePrefConstants.EXCHANGE_DOWNLOADED_CHECK_UPDATES,
                Messages.getString("ExchangePreferencePage.exchangeCheckUpdatesTitle"), getFieldEditorParent()); //$NON-NLS-1$
        addField(downloadedCheckUpdates);

        addListeners();
    }

    private void addListeners() {

        logonButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Project project = ProjectManager.getInstance().getCurrentProject();
                if (project != null) {
                    if (userAccount == null) {
                        String userEmail = null;
                        if (project.getAuthor() != null) {
                            userEmail = project.getAuthor().getLogin();
                        }
                        TalendForgeDialog tfDialog = new TalendForgeDialog(logonButton.getShell(), userEmail);
                        if (tfDialog.open() == Window.OK) {
                            initData();
                            userAccountLink.setText(userAccount != null ? "<a> " + userAccount + " </a>" : "<a> " + "< None >"
                                    + " </a>");
                            logonButton.setText(userAccount != null ? Messages
                                    .getString("ExchangePreferencePage.signOutExchangeTitle") : Messages
                                    .getString("ExchangePreferencePage.signInExchangeTitle"));
                            logonButton.getParent().pack();
                        }
                    } else {
                        logonButton.setText(Messages.getString("ExchangePreferencePage.signInExchangeTitle"));
                        userAccountLink.setText("<a> " + "< None >" + " </a>");
                        userAccount = null;
                        if (project.getAuthor() != null) {
                            String connectionEmail = project.getAuthor().getLogin();
                            prefStore.setValue(connectionEmail, "");
                        }
                    }
                }
            }
        });
    }

    public void initData() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        if (project.getAuthor() != null) {
            String connectionEmail = project.getAuthor().getLogin();

            String string = prefStore.getString(connectionEmail);
            if (string != null) {
                String[] split = string.split(":");
                if (split.length == 3) {
                    userAccount = split[1];
                }
            }
        }
        setPreferenceStore(prefStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        return super.performOk();
    }

}
