// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.Project;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.utils.GitProviderUtil;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class GeneralProjectSettingPage extends ProjectSettingPage {

    private Text nameText;

    private Text descriptionText;
    
    private Button utf8Button;

    private Button disableScreenshotBtn;

    private static ProjectPreferenceManager prefManager = CoreRuntimePlugin.getInstance().getProjectPreferenceManager();

    public GeneralProjectSettingPage() {
        super();
        this.noDefaultAndApplyButton();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        GridData data;

        // Name
        Label nameLab = new Label(container, SWT.NONE);
        nameLab.setText(Messages.getString("PropertiesWizardPage.Name")); //$NON-NLS-1$

        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.setEditable(false);
        // Description
        Label descriptionLab = new Label(container, SWT.NONE);
        descriptionLab.setText(Messages.getString("PropertiesWizardPage.Description")); //$NON-NLS-1$
        descriptionLab.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

        descriptionText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 120;
        descriptionText.setLayoutData(data);      
        
        utf8Button = new Button(container, SWT.CHECK);
        utf8Button.setText("Allow specific characters (UTF8,...) for columns of schemas");
        GridData utf8ButtonData = new GridData(GridData.FILL_HORIZONTAL);
        utf8ButtonData.horizontalSpan = 2;
        utf8Button.setLayoutData(utf8ButtonData);

        if (GitProviderUtil.isProjectInGitMode()) {
            disableScreenshotBtn = new Button(container, SWT.CHECK);
            disableScreenshotBtn.setText(Messages.getString("GeneralProjectSettingPage.disableScreenshot"));
            GridData disableScreenshotBtnData = new GridData(GridData.FILL_HORIZONTAL);
            disableScreenshotBtnData.horizontalSpan = 2;
            disableScreenshotBtn.setLayoutData(disableScreenshotBtnData);
            disableScreenshotBtn.setSelection(prefManager.isDisableScreenshot());
        }

        updateContent();
        return container;
    }

    protected void updateContent() {
        nameText.setText(pro.getLabel());
        String description = "";
        Project emfProject = pro.getEmfProject();
        if (emfProject != null && emfProject.getDescription() != null) {
            description = emfProject.getDescription();
        }
        descriptionText.setText(description);       
        utf8Button.setSelection(prefManager.isAllowSpecificCharacters());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        super.performApply();
        apply();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        apply();
        return super.performOk();
    }

    protected void apply() {
        boolean optionChanged = false;
        if (utf8Button != null && utf8Button.getSelection() != prefManager.isAllowSpecificCharacters()) {
            prefManager.setAllowSpecificCharacters(utf8Button.getSelection());
            optionChanged = true;
        }
        if (disableScreenshotBtn != null && disableScreenshotBtn.getSelection() != prefManager.isDisableScreenshot()) {
            prefManager.setDisableScreenshot(disableScreenshotBtn.getSelection());
            optionChanged = true;
        }
        if (optionChanged) {
            prefManager.save();
        }

        if (descriptionText != null) {
            if (descriptionText.getText().equals(pro.getEmfProject().getDescription())) {
                return;
            }
            pro.getEmfProject().setDescription(descriptionText.getText());
        }
        IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();

        try {
            prf.saveProject(pro);
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

}
