// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.wizard.creatcomponent;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.model.enumtype.LanguageType;

/**
 * @author rli
 * 
 */
public class WizardComponentFolderPage extends AbstractComponentPage {

    private Text componentLongName;

    private Button useJavaLangButton;

    private Text componentFolderText;

    /**
     * @param pageName
     * @param componentPref
     */
    public WizardComponentFolderPage(String pageName, ComponentPref componentPref) {
        super(pageName, componentPref);
    }

    @Override
    protected void initialize() {
        if (this.componentPref.getName() == null) {
            useJavaLangButton.setSelection(true);
            componentPref.setLanguageType(LanguageType.JAVALANGUAGETYPE);
            componentFolderText.addModifyListener(new ModifyListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
                 */
                @Override
                public void modifyText(ModifyEvent e) {
                    setPageComplete(validatePage());
                    componentPref.setName(componentFolderText.getText());
                }

            });
            componentLongName.addModifyListener(new ModifyListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
                 */
                @Override
                public void modifyText(ModifyEvent e) {
                    setPageComplete(validatePage());
                    componentPref.setLongName(componentLongName.getText());
                }

            });
            this.setPageComplete(validatePage());
        } else {
            this.componentFolderText.setText(componentPref.getName());
            this.componentFolderText.setEnabled(false);
            switch (componentPref.getLanguageType()) {
            case JAVALANGUAGETYPE:
                this.useJavaLangButton.setSelection(true);
                break;
            case BOTHLANGUAGETYPE:
                this.useJavaLangButton.setSelection(true);
                break;
            default:
            }
            this.setPageComplete(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageContent(Composite parent) {

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        this.createComponentGroup(composite);
        this.setControl(composite);
    }

    private void createComponentGroup(Composite parent) {
        // component specification group
        Group optionsGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, true);
        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

        Label nameLabel = new Label(optionsGroup, SWT.NONE);
        nameLabel.setText(Messages.getString("WizardComponentFolderPage.Name")); //$NON-NLS-1$
        componentFolderText = new Text(optionsGroup, SWT.BORDER | SWT.LEFT);
        componentFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label longNameLabel = new Label(optionsGroup, SWT.NONE);
        longNameLabel.setText(Messages.getString("WizardComponentFolderPage.LongName")); //$NON-NLS-1$
        componentLongName = new Text(optionsGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.LEFT);
        componentLongName.setLayoutData(new GridData(225, 60));

        // create the language selection check button
        Label availability = new Label(optionsGroup, SWT.LEFT);
        availability.setText(Messages.getString("WizardComponentFolderPage.Available")); //$NON-NLS-1$

        Composite languageGroup = new Composite(optionsGroup, SWT.NONE);
        languageGroup.setLayout(layout);
        // languageGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

        useJavaLangButton = new Button(languageGroup, SWT.CHECK | SWT.RIGHT);
        useJavaLangButton.setText(Messages.getString("WizardComponentFolderPage.Java")); //$NON-NLS-1$
        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPageComplete(validatePage());
                componentPref.setLanguageType(LanguageType.JAVALANGUAGETYPE);
                // propertyChangeBean.firePropertyChange(PluginConstant.LANGUAGE_PROPERTY, null, type);
            }
        };
        useJavaLangButton.addSelectionListener(listener);
    }

    @Override
    protected boolean validatePage() {
        if (useJavaLangButton != null) {
            if (!(useJavaLangButton.getSelection())) {
                setErrorMessage(Messages.getString("WizardComponentFolderPage.ErrMSG1")); //$NON-NLS-1$
                return false;
            }
        }
        if (this.componentFolderText.isEnabled()) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PluginConstant.COMPONENT_PROJECT);
            String componentName = componentFolderText.getText();
            if (componentName.equals(PluginConstant.EMPTY_STRING)) {
                setErrorMessage(Messages.getString("WizardComponentFolderPage.ErrMSG2")); //$NON-NLS-1$
                return false;
            }
            IFolder componentFolder = project.getFolder(componentName);
            if (componentFolder.exists()) {
                setErrorMessage(Messages.getString("WizardComponentFolderPage.ErrMSG3")); //$NON-NLS-1$
                return false;
            }
        }

        if (this.componentLongName.isEnabled()) {
            String longName = componentLongName.getText();
            if (longName.equals(PluginConstant.EMPTY_STRING)) {
                setErrorMessage(Messages.getString("WizardComponentFolderPage.ErrMSG4")); //$NON-NLS-1$
                return false;
            }
        }

        setErrorMessage(null);
        return true;
    }

    /**
     * Getter for componentName.
     * 
     * @return the componentName
     */
    public String getComponentFolderName() {
        return componentFolderText.getText();
    }

    /**
     * Getter for componentLongName.
     * 
     * @return the componentLongName
     */
    public Text getComponentLongName() {
        return componentLongName;
    }
}
