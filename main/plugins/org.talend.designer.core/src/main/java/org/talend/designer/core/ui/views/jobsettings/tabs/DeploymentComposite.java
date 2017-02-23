// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.views.jobsettings.tabs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.MavenDeploymentValueChangeCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.utils.MavenVersionUtils;

public class DeploymentComposite extends AbstractTabComposite {

    private Text groupIdText;

    private Button versionCheckbox;

    private Text versionText;

    private Label versionWarningLabel;

    private CCombo exportTypeCombo;

    private String defaultVersion;

    private Process process;

    public DeploymentComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            IRepositoryViewObject iRepositoryViewObject) {
        super(parent, style, widgetFactory, iRepositoryViewObject);
        if (iRepositoryViewObject instanceof Process) {
            process = (Process) iRepositoryViewObject;
            defaultVersion = process.getVersion();
        }
        createControl();
        initialize();
        addListeners();
    }

    private void createControl() {
        setLayout(new GridLayout());
        setBackground(getParent().getBackground());
        
        Composite composite = new Composite(this, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 10;
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayout(layout);
        composite.setLayoutData(gridData);

        new Label(composite, SWT.NONE);
        
        Label groupIdLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.gourpIdLabel")); //$NON-NLS-1$
        GridData groupIdLabelData = new GridData(GridData.FILL_HORIZONTAL);
        groupIdLabel.setLayoutData(groupIdLabelData);

        groupIdText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        GridData groupIdTextData = new GridData(GridData.FILL_HORIZONTAL);
        groupIdTextData.widthHint = 200;
        groupIdText.setLayoutData(groupIdTextData);

        new Label(composite, SWT.NONE);

        versionCheckbox = widgetFactory.createButton(composite, "", SWT.CHECK); //$NON-NLS-1$
        GridData versionCheckBoxData = new GridData(GridData.FILL_HORIZONTAL);
        versionCheckbox.setLayoutData(versionCheckBoxData);
        Label versionLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.versionLabel")); //$NON-NLS-1$
        GridData versionLabelData = new GridData(GridData.FILL_HORIZONTAL);
        versionLabel.setLayoutData(versionLabelData);

        versionText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        GridData versionTextData = new GridData(GridData.FILL_HORIZONTAL);
        versionTextData.widthHint = 200;
        versionText.setLayoutData(versionTextData);

        versionWarningLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.versionWarning")); //$NON-NLS-1$
        versionWarningLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));

        new Label(composite, SWT.NONE);
        
        Label exportTypeLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.exportTypeLabel")); //$NON-NLS-1$
        GridData exportTypeLabelData = new GridData(GridData.FILL_HORIZONTAL);
        exportTypeLabel.setLayoutData(exportTypeLabelData);

        exportTypeCombo = widgetFactory.createCCombo(composite);
        GridData exportTypeComboData = new GridData(GridData.FILL_HORIZONTAL);
        exportTypeComboData.widthHint = 200;
        exportTypeCombo.setLayoutData(exportTypeComboData);
    }

    private void initialize() {
        EMap<?, ?> properties = repositoryObject.getProperty().getAdditionalProperties();
        if (properties != null) {
            String groupId = (String) properties.get(MavenConstants.NAME_GROUP_ID);
            if (groupId != null) {
                groupIdText.setText(groupId);
            } else {
                // TODO get from PublishPlugin.getDefault().getPreferenceStore();
                String defaultGroupId = "org.example"; // $NON-NLS-1$
                groupIdText.setText(defaultGroupId);
            }
            String userVersion = (String) properties.get(MavenConstants.NAME_USER_VERSION);
            if (userVersion != null) {
                boolean isDefaultVersion = userVersion.equals(defaultVersion);
                versionCheckbox.setSelection(!isDefaultVersion);
                versionText.setEnabled(!isDefaultVersion);
                versionText.setText(userVersion);
                versionWarningLabel.setVisible(!MavenVersionUtils.isValidMavenVersion(userVersion));
            } else {
                versionCheckbox.setSelection(false);
                versionText.setEnabled(false);
                versionText.setText(defaultVersion);
                versionWarningLabel.setVisible(false);
            }
            // TODO setup exportType.
            exportTypeCombo.setItems(new String[] { "a", "b", "c" });
            exportTypeCombo.setEnabled(false);
            String exportType = (String) properties.get(MavenConstants.NAME_EXPORT_TYPE);
            if (exportType != null) {
                exportTypeCombo.select(exportTypeCombo.indexOf(exportType));
            } else {
                exportTypeCombo.select(0);
            }
        }
    }

    private void addListeners() {
        groupIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (!StringUtils.isEmpty(groupIdText.getText())) {
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_GROUP_ID,
                            groupIdText.getText());
                    getCommandStack().execute(cmd);
                }
            }
        });

        versionCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (versionCheckbox.getSelection()) {
                    versionText.setEnabled(true);
                    versionText.setText(""); //$NON-NLS-1$
                } else {
                    versionText.setEnabled(false);
                    versionText.setText(defaultVersion);
                }
            }

        });

        versionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String version = versionText.getText();
                if (!StringUtils.isEmpty(version)) {
                    versionWarningLabel.setVisible(!MavenVersionUtils.isValidMavenVersion(version));
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_USER_VERSION, version);
                    getCommandStack().execute(cmd);
                }
            }

        });

        exportTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_EXPORT_TYPE,
                        exportTypeCombo.getText());
                getCommandStack().execute(cmd);
            }

        });
    }

    private CommandStack getCommandStack() {
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (part instanceof AbstractMultiPageTalendEditor) {
            CommandStack cmdStack = (CommandStack) part.getAdapter(CommandStack.class);
            return cmdStack;
        }
        return null;
    }

}
