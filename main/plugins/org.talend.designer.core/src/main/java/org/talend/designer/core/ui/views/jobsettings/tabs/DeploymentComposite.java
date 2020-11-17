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
package org.talend.designer.core.ui.views.jobsettings.tabs;

import static org.talend.repository.utils.MavenVersionUtils.containsKey;
import static org.talend.repository.utils.MavenVersionUtils.get;
import static org.talend.repository.utils.MavenVersionUtils.getDefaultVersion;
import static org.talend.repository.utils.MavenVersionUtils.isAdditionalPropertiesNull;
import static org.talend.repository.utils.MavenVersionUtils.isValidMavenVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.BuildType;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.cmd.MavenDeploymentValueChangeCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.maven.utils.PomIdsHelper;

public class DeploymentComposite extends AbstractTabComposite {

    Color COLOR_RED = getDisplay().getSystemColor(SWT.COLOR_RED);

    private Button groupIdCheckbox;

    private Text groupIdText;

    private Button versionCheckbox;

    private Text versionText;

    private Button snapshotCheckbox;

    private Label buildTypeLabel;

    private ComboViewer buildTypeCombo;

    private String defaultGroupId;

    private String groupId;

    private String defaultVersion;

    private String version;

    private Process process;

    private Item serviceItem;

    private CommandStack commandStack;

    private IESBService esbService;

    private boolean isService;

    private boolean isProcessItem;

    private boolean isServiceItem;

    private boolean isDataServiceJob; // Is ESB SOAP Service Job
    
    private boolean isChildJob; 

    public DeploymentComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            IRepositoryViewObject repositoryViewObject) {
        super(parent, style, widgetFactory, repositoryViewObject);
        if (repositoryViewObject instanceof Process) {
            process = (Process) repositoryViewObject;
        } else if (repositoryViewObject.getRepositoryObjectType().equals(ERepositoryObjectType.PROCESS)
                || repositoryViewObject.getRepositoryObjectType().equals(ERepositoryObjectType.PROCESS_ROUTE)
                || repositoryViewObject.getRepositoryObjectType().equals(ERepositoryObjectType.PROCESS_ROUTE_MICROSERVICE)) {
            isProcessItem = true;
            ProcessItem i = (ProcessItem) repositoryViewObject.getProperty().getItem();
            try {
                process = (new ProcessEditorInput(i, true, null, false).getLoadedProcess());
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
            esbService = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
        }
        if (repositoryViewObject instanceof Process || isProcessItem) {
            commandStack = process.getCommandStack();
            defaultVersion = getDefaultVersion(process.getVersion());

            isDataServiceJob = false;
            isChildJob = false;
            // Disable widgests in case of the job is for ESB data service
            if (!process.getComponentsType().equals(ComponentCategory.CATEGORY_4_CAMEL.getName())) {
                List<INode> nodes = (List<INode>) process.getGraphicalNodes();
                for (INode node : nodes) {
                    if ("tESBProviderRequest".equals(node.getComponent().getName())) {
                        isDataServiceJob = true;
                        defaultVersion = "";
                        break;
                    }
                }
                
                for (INode node : nodes) {
                    if ("tRouteInput".equals(node.getComponent().getName())) {
                        isChildJob = true;
                        break;
                    }
                }
            }
        } else {
            IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (esbService != null) {
                if (esbService.isWSDLEditor(editor)) {
                    serviceItem = esbService.getWSDLEditorItem(editor);
                    commandStack = (CommandStack) editor.getAdapter(CommandStack.class);
                } else if (repositoryViewObject.getRepositoryObjectType().getType().equals("SERVICES")
                        || repositoryViewObject.getRepositoryObjectType().equals(ERepositoryObjectType.SERVICESPORT)
                        || repositoryViewObject.getRepositoryObjectType().equals(ERepositoryObjectType.SERVICESOPERATION)) {
                    serviceItem = repositoryViewObject.getProperty().getItem();
                    isServiceItem = true;
                }
                defaultVersion = getDefaultVersion(serviceItem.getProperty().getVersion());
                isService = true;
            }
        }
        createControl();
        initialize();
        addListeners();
        checkReadOnly();
    }

    private void checkReadOnly() {
        try {
            String currentVersion = isService ? serviceItem.getProperty().getVersion() : process.getVersion();
            IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance()
                    .getLastVersion(isService ? serviceItem.getProperty().getId() : process.getId());
            String latestVersion = obj.getVersion();

            if (!currentVersion.equals(latestVersion) || isDataServiceJob || isProcessItem || isServiceItem) {
                groupIdCheckbox.setEnabled(false);
                groupIdText.setEnabled(false);
                versionCheckbox.setEnabled(false);
                versionText.setEnabled(false);
                snapshotCheckbox.setEnabled(false);
                if (buildTypeCombo != null) {
                    buildTypeCombo.getCCombo().setEnabled(false);
                }
                if (buildTypeLabel != null) {
                    buildTypeLabel.setEnabled(false);
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

    }

    private void createControl() {
        setLayout(new GridLayout());
        setBackground(getParent().getBackground());
        if (isDataServiceJob) {
            Composite messageComposite = new Composite(this, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.horizontalSpacing = 10;
            layout.verticalSpacing = 10;
            messageComposite.setLayout(layout);
            messageComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            widgetFactory.createLabel(messageComposite,
                    "SOAP data service cannot be published, deployment setting is \naccording to the defined service.");
        }
        
        if (isChildJob) {
            Composite messageComposite = new Composite(this, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.horizontalSpacing = 10;
            layout.verticalSpacing = 10;
            messageComposite.setLayout(layout);
            messageComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            widgetFactory.createLabel(messageComposite,
                    "Deployment parameters will be inherited from parent route during publishing from Studio and Command Line");
        }
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
        groupIdTextData.widthHint = 600;
        groupIdText.setLayoutData(groupIdTextData);

        versionCheckbox = widgetFactory.createButton(composite, Messages.getString("DeploymentComposite.versionLabel"), //$NON-NLS-1$
                SWT.CHECK);
        versionCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        versionText = new Text(composite, SWT.BORDER);
        GridData versionTextData = new GridData(GridData.FILL_HORIZONTAL);
        versionTextData.widthHint = 200;
        versionText.setLayoutData(versionTextData);

        snapshotCheckbox = widgetFactory.createButton(composite, Messages.getString("DeploymentComposite.snapshotLabel"), //$NON-NLS-1$
                SWT.CHECK);
        GridData snapshotCheckboxData = new GridData(GridData.FILL_HORIZONTAL);
        snapshotCheckboxData.horizontalSpan = 2;
        snapshotCheckbox.setLayoutData(snapshotCheckboxData);

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
        if (!isAdditionalPropertiesNull(getObject())) {
            Property property = isService ? serviceItem.getProperty() : process.getProperty();
            PomIdsHelper.getJobGroupId(property);
            String defaultProjectGroupId = PomIdsHelper.getJobGroupId(property);
            defaultGroupId = isDataServiceJob ? "" : defaultProjectGroupId; //$NON-NLS-1$
            groupId = (String) get(getObject(), MavenConstants.NAME_GROUP_ID);
            boolean isCustomGroupId = groupId != null;
            if (!isCustomGroupId) {
                groupId = defaultGroupId;
            }
            groupIdText.setText(groupId);
            groupIdCheckbox.setSelection(isCustomGroupId);
            groupIdText.setEnabled(isCustomGroupId);

            version = (String) get(getObject(), MavenConstants.NAME_USER_VERSION);
            boolean isCustomVersion = version != null;
            if (!isCustomVersion) {
                version = defaultVersion;
            }
            versionText.setText(version);
            versionCheckbox.setSelection(isCustomVersion);
            versionText.setEnabled(isCustomVersion);
            versionText.setToolTipText(""); //$NON-NLS-1$

            boolean useSnapshot = containsKey(getObject(), MavenConstants.NAME_PUBLISH_AS_SNAPSHOT);
            snapshotCheckbox.setSelection(useSnapshot);

            final boolean showBuildType = isShowBuildType();
            final Control buildTypeControl = buildTypeCombo.getControl();
            buildTypeControl.setVisible(showBuildType);
            buildTypeLabel.setVisible(showBuildType);
            
            if (showBuildType) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(getObjectType(), getObject());
                final BuildType[] validBuildTypes = BuildExportManager.getInstance().getValidBuildTypes(parameters);
                buildTypeCombo.setInput(validBuildTypes);
                buildTypeControl.setEnabled(true);
                String buildType = (String) get(getObject(), TalendProcessArgumentConstant.ARG_BUILD_TYPE);
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
        // add support for ESB Service.
        if (!PluginChecker.isTIS()) {
            return false;
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(getObjectType(), getObject());
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
                }
            }

        });

        groupIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String currentGroupId = groupIdText.getText();
                if (PomIdsHelper.isValidGroupId(currentGroupId)) {
                    groupIdText.setBackground(getBackground());
                    groupIdText.setToolTipText(""); //$NON-NLS-1$
                    if (!defaultGroupId.equals(currentGroupId)) {
                        groupId = currentGroupId;
                    } else {
                        if (!groupIdCheckbox.getSelection()) {
                            currentGroupId = null;
                        }
                    }
                    // if empty, remove it from job, else will set the new value
                    Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_GROUP_ID,
                            currentGroupId);
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
                }
            }

        });

        versionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String currentVersion = versionText.getText();
                if (StringUtils.isBlank(currentVersion)
                        || !isValidMavenVersion(currentVersion, snapshotCheckbox.getSelection())) {
                    versionText.setToolTipText(Messages.getString("DeploymentComposite.valueWarning")); //$NON-NLS-1$
                    versionText.setBackground(COLOR_RED);
                } else {
                    versionText.setToolTipText(""); //$NON-NLS-1$
                    versionText.setBackground(getBackground());
                    if (!defaultVersion.equals(currentVersion)) {
                        version = currentVersion;
                    } else {
                        if (!versionCheckbox.getSelection()) {
                            currentVersion = null;
                        }
                    }
                    // if empty, remove it from job, else will set the new value
                    Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_USER_VERSION,
                            currentVersion);
                    getCommandStack().execute(cmd);
                }
            }

        });

        snapshotCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // if unchecked then remove key.
                String useSnapshot = snapshotCheckbox.getSelection() ? String.valueOf(true) : null;
                Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_PUBLISH_AS_SNAPSHOT,
                        useSnapshot);
                getCommandStack().execute(cmd);
            }

        });

        buildTypeCombo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                final ISelection selection = event.getSelection();
                if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
                    final Object elem = ((IStructuredSelection) selection).getFirstElement();
                    if (elem instanceof BuildType) {
                        Command cmd = new MavenDeploymentValueChangeCommand(getObject(),
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

    private Object getObject() {
        return isService ? serviceItem.getProperty() : process;
    }

    private String getObjectType() {
        return isService ? IBuildParametes.SERVICE : IBuildParametes.PROCESS;
    }

}
