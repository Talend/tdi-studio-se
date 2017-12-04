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

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.model.enumtype.JetFileStamp;
import org.talend.componentdesigner.model.enumtype.ResourceLanguageType;
import org.talend.componentdesigner.ui.composite.LibSelectionComposite;

/**
 * @author rli
 * 
 */
public class WizardJetFilesChoosePage extends AbstractComponentPage {

    private Button useBeginButton;

    private Button useMainButton;

    private Button useEndButton;

    private Label imageDirectoryLabel;

    private Text imageDirectoryText;

    private Button viewImageButton;

    private Button browserImageButton;

    private Button defaultImageButton;

    private Button useDefaultResourceButton;

    private Button useZHResourceButton;

    private Button useFRResourceButton;

    private LibSelectionComposite libComposite;

    protected WizardJetFilesChoosePage(String pageName, ComponentPref componentPref) {
        super(pageName, componentPref);
    }

    @Override
    protected void initialize() {
        if (this.componentPref.getName() == null) {
            useBeginButton.setSelection(true);
            componentPref.setJetFileStamps(JetFileStamp.findFileStamps(true, false, false));
            useDefaultResourceButton.setSelection(true);
            final List<ResourceLanguageType> resourceTypes = new ArrayList<ResourceLanguageType>();
            resourceTypes.add(ResourceLanguageType.DEFAULTRESOURCETYPE);
            componentPref.setResourceLanguageTypes(resourceTypes);
            defaultImageButton.setSelection(true);
        } else {
            // initialize jet file stamp
            for (JetFileStamp fileStamp : componentPref.getJetFileStamps()) {
                switch (fileStamp) {
                case JETBEGINSTAMP:
                    this.useBeginButton.setSelection(true);
                    break;
                case JETMAINSTAMP:
                    this.useMainButton.setSelection(true);
                    break;
                case JETENDSTAMP:
                    this.useEndButton.setSelection(true);
                    break;
                default:
                }
            }

            // initialize resource file language
            for (ResourceLanguageType resourceLangType : componentPref.getResourceLanguageTypes()) {
                switch (resourceLangType) {
                case DEFAULTRESOURCETYPE:
                    this.useDefaultResourceButton.setSelection(true);
                    break;
                case ZHRESOURCETYPE:
                    this.useZHResourceButton.setSelection(true);
                    break;
                case FRRESOURCETYPE:
                    this.useFRResourceButton.setSelection(true);
                    break;
                default:
                }
            }

            // initialize image selection
            if (this.componentPref.getImageURL() == null) {
                defaultImageButton.setSelection(true);
            } else {
                browserImageButton.setSelection(true);
                imageDirectoryText.setText(componentPref.getImageURL());
            }

            // initialize library list selection
            libComposite.setLibEntries(componentPref.getLibEntries());

        }
        setPageComplete(validatePage());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageContent(Composite parent) {
        Composite groupsComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        groupsComposite.setLayout(layout);
        groupsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        creatFilesSelectionGrp(groupsComposite);
        creatResourceLangGrp(groupsComposite);
        creatImageSelectionGrp(groupsComposite);
        creatLibSelectionGrp(groupsComposite);

        this.setControl(groupsComposite);

    }

    private void creatFilesSelectionGrp(Composite groupsComposite) {
        Group filesSelectionGrp = new Group(groupsComposite, SWT.NONE);
        filesSelectionGrp.setText("Select required Jet files"); //$NON-NLS-1$
        // GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(
        // filesSelectionGrp);
        filesSelectionGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout groupLayout = new GridLayout(1, false);
        filesSelectionGrp.setLayout(groupLayout);

        // create the language selection check button
        useBeginButton = new Button(filesSelectionGrp, SWT.CHECK | SWT.RIGHT);
        useBeginButton.setText(Messages.getString("WizardJetFilesChoosePage.BeginFile")); //$NON-NLS-1$
        useMainButton = new Button(filesSelectionGrp, SWT.CHECK | SWT.RIGHT);
        useMainButton.setText(Messages.getString("WizardJetFilesChoosePage.MainFile")); //$NON-NLS-1$
        useEndButton = new Button(filesSelectionGrp, SWT.CHECK | SWT.RIGHT);
        useEndButton.setText(Messages.getString("WizardJetFilesChoosePage.EndFile")); //$NON-NLS-1$

        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPageComplete(validatePage());
                List<JetFileStamp> jetFileTypes = JetFileStamp.findFileStamps(useBeginButton.getSelection(), useMainButton
                        .getSelection(), useEndButton.getSelection());
                componentPref.setJetFileStamps(jetFileTypes);
            }
        };
        useBeginButton.addSelectionListener(listener);
        useMainButton.addSelectionListener(listener);
        useEndButton.addSelectionListener(listener);
    }

    private void creatResourceLangGrp(Composite groupsComposite) {
        Group resourceLangGrp = new Group(groupsComposite, SWT.NONE);
        resourceLangGrp.setText("Select language dependent file"); //$NON-NLS-1$
        // GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(
        // filesSelectionGrp);
        resourceLangGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout groupLayout = new GridLayout(3, false);
        resourceLangGrp.setLayout(groupLayout);

        useDefaultResourceButton = new Button(resourceLangGrp, SWT.CHECK | SWT.RIGHT);
        useDefaultResourceButton.setText(PluginConstant.DEFAULTLANG_RESOURCE);
        useZHResourceButton = new Button(resourceLangGrp, SWT.CHECK | SWT.RIGHT);
        useZHResourceButton.setText(PluginConstant.ZHLANG_RESOURCE);
        useFRResourceButton = new Button(resourceLangGrp, SWT.CHECK | SWT.RIGHT);
        useFRResourceButton.setText(PluginConstant.FRLANG_RESOURCE);

        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setPageComplete(validatePage());
                Button button = ((Button) e.getSource());
                if (button.getSelection()) {
                    componentPref.getResourceLanguageTypes().add(ResourceLanguageType.find(button.getText()));
                } else {
                    componentPref.getResourceLanguageTypes().remove(ResourceLanguageType.find(button.getText()));
                }
                // propertyChangeBean.firePropertyChange(PluginConstant.RESOURCETYPE_PROPERTY, null, resourceTypes);
            }
        };
        useDefaultResourceButton.addSelectionListener(listener);
        useZHResourceButton.addSelectionListener(listener);
        useFRResourceButton.addSelectionListener(listener);
    }

    private void creatImageSelectionGrp(Composite groupsComposite) {
        Group imageSelectionGrp = new Group(groupsComposite, SWT.NONE);
        imageSelectionGrp.setText("Select Icon"); //$NON-NLS-1$
        // GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(
        // filesSelectionGrp);
        imageSelectionGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout groupLayout = new GridLayout(9, false);
        imageSelectionGrp.setLayout(groupLayout);

        defaultImageButton = new Button(imageSelectionGrp, SWT.RADIO | SWT.RIGHT);
        GridData imageGridData = new GridData();
        imageGridData.horizontalSpan = 9;
        defaultImageButton.setLayoutData(imageGridData);
        defaultImageButton.setText(Messages.getString("WizardJetFilesChoosePage.UseDefaultIcon")); //$NON-NLS-1$
        browserImageButton = new Button(imageSelectionGrp, SWT.RADIO | SWT.RIGHT);
        browserImageButton.setText(Messages.getString("WizardJetFilesChoosePage.UseIconFrom")); //$NON-NLS-1$
        imageGridData = new GridData();
        imageGridData.horizontalSpan = 9;
        browserImageButton.setLayoutData(imageGridData);

        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setImageDirectoryEnable(browserImageButton.getSelection());
                setPageComplete(validatePage());
            }
        };
        defaultImageButton.addSelectionListener(listener);
        browserImageButton.addSelectionListener(listener);

        imageDirectoryLabel = new Label(imageSelectionGrp, SWT.NONE);
        GridData labelGridData = new GridData();
        labelGridData.horizontalSpan = 2;
        imageDirectoryLabel.setLayoutData(labelGridData);
        imageDirectoryLabel.setText(Messages.getString("WizardJetFilesChoosePage.Directory")); //$NON-NLS-1$

        imageDirectoryText = new Text(imageSelectionGrp, SWT.BORDER);
        GridData textGridData = new GridData(GridData.FILL_HORIZONTAL);
        textGridData.horizontalSpan = 6;
        imageDirectoryText.setLayoutData(textGridData);

        imageDirectoryText.addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            public void modifyText(ModifyEvent e) {
                setPageComplete(validatePage());
                componentPref.setImageURL(imageDirectoryText.getText());
                // propertyChangeBean.firePropertyChange(PluginConstant.IMAGE_PROPERTY, null,
                // imageDirectoryText.getText());
            }

        });

        viewImageButton = new Button(imageSelectionGrp, SWT.UP);
        GridData buttonGridData = new GridData();
        buttonGridData.horizontalSpan = 1;
        viewImageButton.setLayoutData(buttonGridData);
        viewImageButton.setText(PluginConstant.BROWSER_LABEL);
        viewImageButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                imageDirectoryText.setText(getPathFromDialog(new String[] { "*.png" })); //$NON-NLS-1$
                setPageComplete(validatePage());
            }
        });

        setImageDirectoryEnable(browserImageButton.getSelection());
    }

    private void creatLibSelectionGrp(Composite groupsComposite) {
        Group libSelectionGrp = new Group(groupsComposite, SWT.NONE);
        libSelectionGrp.setText("Add library"); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_BOTH);
        libSelectionGrp.setLayoutData(gd);
        GridLayout groupLayout = new GridLayout();
        libSelectionGrp.setLayout(groupLayout);
        libComposite = new LibSelectionComposite(libSelectionGrp, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);

        libComposite.setLayoutData(gd);
        libComposite.setComponentPrefBean(this.componentPref);
    }

    private String getPathFromDialog(String[] extensions) {
        FileDialog fileDialog = new FileDialog(this.getShell(), SWT.OPEN);
        fileDialog.setFilterPath(null);
        fileDialog.setFilterExtensions(extensions); //$NON-NLS-1$ //$NON-NLS-2$
        String filePath = fileDialog.open();
        if (filePath == null) {
            return ""; //$NON-NLS-1$
        }
        return filePath;
    }

    private void setImageDirectoryEnable(boolean enabled) {
        viewImageButton.setEnabled(enabled);
        imageDirectoryText.setEnabled(enabled);
        imageDirectoryLabel.setEnabled(enabled);
    }

    @Override
    protected boolean validatePage() {
        if (browserImageButton.getSelection() && imageDirectoryText.getText().equals(PluginConstant.EMPTY_STRING)) {
            this.setErrorMessage(Messages.getString("WizardJetFilesChoosePage.ChoseAIcon")); //$NON-NLS-1$
            return false;
        }
        this.setErrorMessage(null);
        return true;
    }
}
