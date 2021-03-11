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
package org.talend.repository.ui.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.prefs.GeneralParametersProvider;
import org.talend.core.prefs.GeneralParametersProvider.GeneralParameters;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.utils.ProjectUtils;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class NewImportProjectWizardPage extends WizardPage {

    /** Name field. */
    private Text nameText;

    /** Technical Name. */
    private Text technicalNameText;

    /** Description field. */
    private Text descriptionText;

    private IStatus nameStatus;

    private IStatus descriptionStatus;

    private IStatus languageStatus;

    private Button languagePerlRadio;

    private Button languageJavaRadio;

    private static List<String> keywords = new ArrayList<String>();

    /**
     * DOC Administrator NewImportProjectWizardPage constructor comment.
     *
     * @param pageName
     */
    protected NewImportProjectWizardPage() {
        super("WizardPage"); //$NON-NLS-1$

        setTitle(Messages.getString("NewProjectWizardPage.title2")); //$NON-NLS-1$
        setDescription(Messages.getString("NewProjectWizardPage.description"));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        // Name
        Label nameLab = new Label(container, SWT.NONE);
        nameLab.setText(Messages.getString("NewProjectWizardPage.name")); //$NON-NLS-1$

        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // TechnicalName (for information only)
        Label technicalNameLab = new Label(container, SWT.NONE);
        technicalNameLab.setText(Messages.getString("NewProjectWizardPage.technicalName")); //$NON-NLS-1$

        technicalNameText = new Text(container, SWT.BORDER);
        technicalNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        technicalNameText.setEnabled(false);

        // Description
        Label descriptionLab = new Label(container, SWT.NONE);
        descriptionLab.setText(Messages.getString("NewProjectWizardPage.comment")); //$NON-NLS-1$
        descriptionLab.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

        descriptionText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 60;
        descriptionText.setLayoutData(data);

        // Language
        Label languageLab = new Label(container, SWT.NONE);
        languageLab.setText(Messages.getString("NewProjectWizardPage.language")); //$NON-NLS-1$
        languageLab.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

        Composite radioContainer = new Composite(container, SWT.NONE);
        radioContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL + GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        radioContainer.setLayout(gridLayout);

        languageJavaRadio = new Button(radioContainer, SWT.RADIO);
        languageJavaRadio.setText(ECodeLanguage.JAVA.getName());
        languageJavaRadio.setSelection(true);

        languagePerlRadio = new Button(radioContainer, SWT.RADIO);
        languagePerlRadio.setText(ECodeLanguage.PERL.getName() + " (deprecated)");

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        String[] availableLanguages = brandingService.getBrandingConfiguration().getAvailableLanguages();
        if (availableLanguages.length != 2) {
            if (ArrayUtils.contains(availableLanguages, ECodeLanguage.JAVA.getName())) {
                languagePerlRadio.setVisible(false);
                languageJavaRadio.setVisible(false);
                languageJavaRadio.setSelection(true);
                languageLab.setVisible(false);
            }
            if (ArrayUtils.contains(availableLanguages, ECodeLanguage.PERL.getName())) {
                languagePerlRadio.setSelection(true);
                languageJavaRadio.setVisible(false);
                languageJavaRadio.setVisible(false);
                languageLab.setVisible(false);
            }
        }

        // languageCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        // languageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // languageCombo.setItems(new String[] { ECodeLanguage.PERL.getName(), ECodeLanguage.JAVA.getName() });
        // languageCombo.select(0);

        setControl(container);
        addListeners();
        setPageComplete(false);
        init();
    }

    protected void init() {
        nameText.setText("Local_Project"); //$NON-NLS-1$
        nameText.selectAll();
        checkFieldsValue();
    }

    private void addListeners() {
        nameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                checkFieldsValue();
            }
        });

        descriptionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                checkFieldsValue();
            }
        });

        languagePerlRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkFieldsValue();
            }
        });

        languageJavaRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkFieldsValue();
            }
        });
    }

    /**
     * DOC ocarbone Comment method "checkField".
     */
    protected void checkFieldsValue() {
        // Field Name
        if (nameText.getText().length() == 0) {
            nameStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                    Messages.getString("NewProjectWizardPage.nameEmpty"), null); //$NON-NLS-1$
        } else {
            // for bug 11214
            if (!nameText.getText().endsWith(" ")) {//$NON-NLS-1$
                technicalNameText.setText(Project.createTechnicalName(nameText.getText()));
            }
            if (ProjectUtils.isNotValidProjectName(nameText.getText())) {
                nameStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                        Messages.getString("NewProjectWizardPage.illegalCharacter"), null); //$NON-NLS-1$
            } else {

                if (isProjectNameAlreadyUsed(nameText.getText())) {
                    nameStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                            Messages.getString("NewProjectWizardPage.projectNameAlredyExists"), null); //$NON-NLS-1$
                } else {
                    nameStatus = createOkStatus();

                    // Field description
                    descriptionStatus = createOkStatus();

                    // Combo language
                    if (!languageJavaRadio.getSelection() && !languagePerlRadio.getSelection()) {
                        languageStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                                Messages.getString("NewProjectWizardPage.languageEmpty"), //$NON-NLS-1$
                                null);
                    } else if (!languageEnable(getLanguage())) {
                        languageStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.WARNING,
                                Messages.getString("NewProjectWizard.error.languageNotSupported", getLanguage()), //$NON-NLS-1$
                                null);
                    } else {
                        languageStatus = createOkStatus();
                    }
                }
            }
        }
        updatePageStatus();
    }

    private void updatePageStatus() {
        IStatus findMostSevere = findMostSevere();
        setMessage(findMostSevere);
        setPageComplete(findMostSevere.getSeverity() != IStatus.ERROR);
    }

    private IStatus findMostSevere() {
        IStatus status;
        if (nameStatus.getSeverity() == IStatus.ERROR) {
            status = nameStatus;
        } else if (descriptionStatus.getSeverity() == IStatus.ERROR) {
            status = descriptionStatus;
        } else if (languageStatus.getSeverity() == IStatus.ERROR) {
            status = languageStatus;
        } else {
            status = nameStatus.getSeverity() > descriptionStatus.getSeverity() ? nameStatus : descriptionStatus;
            status = status.getSeverity() > languageStatus.getSeverity() ? status : languageStatus;
        }
        return status;
    }

    private void setMessage(IStatus status) {
        if (IStatus.ERROR == status.getSeverity()) {
            setErrorMessage(status.getMessage());
            setMessage(""); //$NON-NLS-1$
        } else {
            setMessage(status.getMessage());
            setErrorMessage(null);
        }
    }

    Project[] projects;

    public void setProjects(Project[] projects) {
        this.projects = projects;
    }

    private boolean isProjectNameAlreadyUsed(String newProjectName) {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        if (projects == null) {
            try {
                projects = repositoryFactory.readProject();
            } catch (Exception e) {
                return true;
            }
        }
        for (Project project : projects) {
            if (Project.createTechnicalName(newProjectName).compareTo(project.getTechnicalLabel()) == 0) {
                return true;
            }
        }
        return false;
    }

    private static IStatus createOkStatus() {
        return new Status(IStatus.OK, RepositoryPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
    }

    public String getLanguage() {
        if (languageJavaRadio.getSelection()) {
            return ECodeLanguage.JAVA.getName();
        }
        if (languagePerlRadio.getSelection()) {
            return ECodeLanguage.PERL.getName();
        }
        return null;
    }

    private boolean languageEnable(String language) {
        String[] authorizedLanguage = GeneralParametersProvider.getStrings(GeneralParameters.AUTHORIZED_LANGUAGE);
        return Arrays.binarySearch(authorizedLanguage, language) >= 0;
    }

    public String getProjectName() {
        return nameText.getText();
    }

    public String getTechnicalName() {
        return technicalNameText.getText();
    }

}
