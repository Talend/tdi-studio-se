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
package org.talend.repository.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.i18n.Messages;
import org.talend.core.model.general.Project;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.dialog.PaletteSettingsDialog;

/**
 * DOC qwei class global comment. Detailled comment
 *
 * @deprecated see ProjectSettingDialog
 */
public class ProjectSettingsWizardPage extends WizardPage {

    /** Name text. */
    protected Text nameText;

    /** Comment text. */
    protected Text descriptionText;

    /** Project settings button */
    private Button projectSetBtn;

    /** Version management button */
    private Button versionBtn;

    private Project pro;

    private static boolean setState = false;

    protected ProjectSettingsWizardPage(String pageName, Project pro) {
        super(pageName);
        this.pro = pro;
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
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

        createButtons(container);

        setControl(container);
        updateContent();
        addListeners();
        setPageComplete(true);

    }

    private void createButtons(Composite parent) {
        Composite btnComposite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 2;
        btnComposite.setLayoutData(data);
        RowLayout layout = new RowLayout();
        layout.marginTop = 8;
        layout.spacing = 15;
        btnComposite.setLayout(layout);

        // Project settings
        projectSetBtn = new Button(btnComposite, SWT.PUSH);
        projectSetBtn.setText(org.talend.repository.i18n.Messages.getString("ProjectSettingsWizardPage.PaletteSettings")); //$NON-NLS-1$
        // Version management
        versionBtn = new Button(btnComposite, SWT.PUSH);
        versionBtn.setText(org.talend.repository.i18n.Messages.getString("ProjectSettingsWizardPage.VersionManagement")); //$NON-NLS-1$

    }

    protected void updateContent() {
        nameText.setText(pro.getLabel());
        descriptionText.setText(pro.getEmfProject().getDescription());
    }

    protected void addListeners() {

        projectSetBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PaletteSettingsDialog dialog = new PaletteSettingsDialog(getShell(), pro);
                dialog.open();
            }
        });
        versionBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setState = true;
                // VersionManagementDialog dialog = new VersionManagementDialog(getShell(), pro);
                // dialog.open();
            }
        });
    }

    protected void finish() {
        if (descriptionText.getText().equals(pro.getEmfProject().getDescription())) {
            return;
        }

        pro.getEmfProject().setDescription(descriptionText.getText());
        IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();

        try {
            prf.saveProject(pro);
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        }
    }
}
