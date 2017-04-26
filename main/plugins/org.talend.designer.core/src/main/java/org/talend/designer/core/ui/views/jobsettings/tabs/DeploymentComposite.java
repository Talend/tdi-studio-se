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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.PluginChecker;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.BuildType;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.MavenDeploymentValueChangeCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.utils.MavenVersionUtils;

public class DeploymentComposite extends AbstractTabComposite {

    Color COLOR_RED = getDisplay().getSystemColor(SWT.COLOR_RED);

    private Button groupIdCheckbox;

    private Text groupIdText;

    private Button versionCheckbox;

    private Text versionText;

    private Label buildTypeLabel;

    private ComboViewer buildTypeCombo;

    private String defaultGroupId;

    private String groupId;

    private String defaultVersion;

    private String version;

    private Process process;

    private CommandStack commandStack;

    public DeploymentComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            IRepositoryViewObject repositoryViewObject) {
        super(parent, style, widgetFactory, repositoryViewObject);

        assert (repositoryViewObject instanceof Process);
        process = (Process) repositoryViewObject;
        commandStack = process.getCommandStack();
        defaultVersion = MavenVersionUtils.getDefaultVersion(process.getVersion());

        createControl();
        initialize();
        addListeners();
        checkReadOnly();
    }

    private void checkReadOnly() {
        try {
            String currentVersion = process.getVersion();
            IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getLastVersion(process.getId());
            String latestVersion = obj.getVersion();
            if (!currentVersion.equals(latestVersion)) {
                groupIdCheckbox.setEnabled(false);
                groupIdText.setEnabled(false);
                versionCheckbox.setEnabled(false);
                versionText.setEnabled(false);
                if (buildTypeCombo != null) {
                    buildTypeCombo.getCCombo().setEnabled(false);
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        
    }

    private void createControl() {
        setLayout(new GridLayout());
        setBackground(getParent().getBackground());

        Composite composite = new Composite(this, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        groupIdCheckbox = widgetFactory.createButton(composite, Messages.getString("DeploymentComposite.gourpIdLabel"), //$NON-NLS-1$
                SWT.CHECK);
        groupIdCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        groupIdText = new Text(composite, SWT.BORDER);
        GridData groupIdTextData = new GridData(GridData.FILL_HORIZONTAL);
        groupIdTextData.widthHint = 200;
        groupIdText.setLayoutData(groupIdTextData);

        versionCheckbox = widgetFactory.createButton(composite, Messages.getString("DeploymentComposite.versionLabel"), //$NON-NLS-1$
                SWT.CHECK);
        versionCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        versionText = new Text(composite, SWT.BORDER);
        GridData versionTextData = new GridData(GridData.FILL_HORIZONTAL);
        versionTextData.widthHint = 200;
        versionText.setLayoutData(versionTextData);

        buildTypeLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.buildTypeLabel")); //$NON-NLS-1$
        buildTypeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        buildTypeCombo = new ComboViewer(widgetFactory.createCCombo(composite, SWT.READ_ONLY | SWT.BORDER));
        final Control buildTypeControl = buildTypeCombo.getControl();
        GridData buildTypeComboData = new GridData(GridData.FILL_HORIZONTAL);
        buildTypeComboData.widthHint = 200;
        buildTypeControl.setLayoutData(buildTypeComboData);
        buildTypeCombo.setContentProvider(ArrayContentProvider.getInstance());
        buildTypeCombo.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof BuildType) {
                    return ((BuildType) element).getLabel();
                }
                return super.getText(element);
            }

        });

    }

    private void initialize() {
        Map<Object, Object> processAdditionalProperties = this.process.getAdditionalProperties();
        if (processAdditionalProperties != null) {
            // TODO get from PublishPlugin.getDefault().getPreferenceStore();
            defaultGroupId = "org.example"; // $NON-NLS-1$
            if (groupId == null) {
                groupId = (String) processAdditionalProperties.get(MavenConstants.NAME_GROUP_ID);
                if (groupId == null) {
                    groupId = defaultGroupId;
                }
            }
            if (groupId != null) {
                boolean isDefaultGroupId = groupId.equals(defaultGroupId);
                groupIdCheckbox.setSelection(!isDefaultGroupId);
                groupIdText.setEnabled(!isDefaultGroupId);
                groupIdText.setText(groupId);
            } else {
                groupIdText.setText(defaultGroupId);
                groupIdCheckbox.setSelection(false);
                groupIdText.setEnabled(false);
            }
            if (version == null) {
                version = (String) processAdditionalProperties.get(MavenConstants.NAME_USER_VERSION);
                if (version == null) {
                    version = defaultVersion;
                }
            }
            if (version != null) {
                boolean isDefaultVersion = version.equals(defaultVersion);
                versionCheckbox.setSelection(!isDefaultVersion);
                versionText.setEnabled(!isDefaultVersion);
                versionText.setText(version);
                versionText.setToolTipText(""); //$NON-NLS-1$
            } else {
                versionCheckbox.setSelection(false);
                versionText.setEnabled(false);
                versionText.setText(defaultVersion);
                versionText.setToolTipText(Messages.getString("DeploymentComposite.valueWarning")); //$NON-NLS-1$ ;
            }

            final boolean showBuildType = isShowBuildType();
            final Control buildTypeControl = buildTypeCombo.getControl();
            buildTypeControl.setVisible(showBuildType);
            buildTypeLabel.setVisible(showBuildType);

            if (showBuildType) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(IBuildParametes.PROCESS, this.process);
                final BuildType[] validBuildTypes = BuildExportManager.getInstance().getValidBuildTypes(parameters);
                buildTypeCombo.setInput(validBuildTypes);
                buildTypeControl.setEnabled(true);
                String buildType = (String) processAdditionalProperties.get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
                BuildType foundType = null;
                if (buildType != null) {
                    for (BuildType t : validBuildTypes) {
                        if (t.getName().equals(buildType)) {
                            foundType = t;
                            break;
                        }
                    }
                }
                if (foundType == null) {// set the first one by default
                    foundType = validBuildTypes[0];
                }
                buildTypeCombo.setSelection(new StructuredSelection(foundType));
            }
        }
    }

    private boolean isShowBuildType() {
        if (!PluginChecker.isTIS()) {
            return false;
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.PROCESS, this.process);
        final BuildType[] validBuildTypes = BuildExportManager.getInstance().getValidBuildTypes(parameters);
        if (validBuildTypes != null && validBuildTypes.length > 1) {// TUP-17276
            return true;
        }
        return false;
    }

    private void addListeners() {
        groupIdCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (groupIdCheckbox.getSelection()) {
                    groupIdText.setEnabled(true);
                    groupIdText.setText(groupId);
                } else {
                    groupIdText.setEnabled(false);
                    groupIdText.setText(defaultGroupId);
                    // remove key, so will be default groupId
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_GROUP_ID, null);
                    getCommandStack().execute(cmd);
                }
            }

        });

        groupIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String currentGroupId =  groupIdText.getText();
                if (currentGroupId != null && !currentGroupId.trim().equals("")) { //$NON-NLS-1$
                    groupIdText.setBackground(getBackground());
                    groupIdText.setToolTipText(""); //$NON-NLS-1$
                    if (!defaultGroupId.equals(currentGroupId)) {
                        groupId = currentGroupId;
                    } else {
                        currentGroupId = null;
                    }
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_GROUP_ID, currentGroupId);
                    getCommandStack().execute(cmd);
                } else {
                    groupIdText.setBackground(COLOR_RED);
                    groupIdText.setToolTipText(Messages.getString("DeploymentComposite.valueWarning")); //$NON-NLS-1$
                }
            }
        });

        versionCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (versionCheckbox.getSelection()) {
                    versionText.setEnabled(true);
                    versionText.setText(version);
                } else {
                    versionText.setEnabled(false);
                    versionText.setText(defaultVersion);
                    // remove key, so will be default version
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_USER_VERSION, null);
                    getCommandStack().execute(cmd);
                }
            }

        });

        versionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String currentVersion = versionText.getText();
                if (currentVersion != null && !currentVersion.trim().equals("") //$NON-NLS-1$
                        && !MavenVersionUtils.isValidMavenVersion(currentVersion)) {
                    versionText.setToolTipText(Messages.getString("DeploymentComposite.valueWarning")); //$NON-NLS-1$
                    versionText.setBackground(COLOR_RED);
                } else {
                    versionText.setToolTipText(""); //$NON-NLS-1$
                    versionText.setBackground(getBackground());
                    if (!defaultVersion.equals(currentVersion)) {
                        version = currentVersion;
                    } else {
                        currentVersion = null;
                    }
                    // if empty, remove it from job, else will set the new value
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_USER_VERSION, currentVersion);
                    getCommandStack().execute(cmd);
                }
            }

        });

        buildTypeCombo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                final ISelection selection = event.getSelection();
                if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
                    final Object elem = ((IStructuredSelection) selection).getFirstElement();
                    if (elem instanceof BuildType) {
                        Command cmd = new MavenDeploymentValueChangeCommand(process,
                                TalendProcessArgumentConstant.ARG_BUILD_TYPE, ((BuildType) elem).getName());
                        getCommandStack().execute(cmd);
                    }
                }
            }

        });
    }

    private CommandStack getCommandStack() {
        return commandStack;
    }

}
