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
package org.talend.repository.ui.login;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionMessageDialog;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.network.TalendProxySelector;
import org.talend.commons.utils.system.EclipseCommandLine;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.model.provider.LoginConnectionManager;
import org.talend.core.repository.services.ILoginConnectionService;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.services.ICoreTisService;
import org.talend.core.services.IGITProviderService;
import org.talend.core.ui.TalendBrowserLaunchHelper;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.workspace.ChooseWorkspaceData;
import org.talend.core.utils.ProjectUtils;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.actions.importproject.ImportDemoProjectAction;
import org.talend.repository.ui.actions.importproject.ImportProjectAsAction;
import org.talend.repository.ui.dialog.OverTimePopupDialogTask;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;
import org.talend.repository.ui.login.connections.ConnectionsDialog;
import org.talend.repository.ui.login.sandboxProject.CreateSandboxProjectDialog;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * created by cmeng on May 13, 2015 Detailled comment
 *
 */
public class LoginProjectPage extends AbstractLoginActionPage {

    protected static final String FINISH_ACTION_OPEN_PROJECT = "OPEN_PROJECT"; //$NON-NLS-1$

    protected static final String FINISH_ACTION_RESTART = "RESTART"; //$NON-NLS-1$

    protected static final String FINISH_ACTION_UPDATE = "UPDATE"; //$NON-NLS-1$

    protected static final String FINISH_ACTION_UPDATE_DETAILS = "DETAILS"; //$NON-NLS-1$

    protected static final String FINISH_ACTION_CREATE_SANDBOX = "CREATE_SANDBOX"; //$NON-NLS-1$

    protected static final String FINISH_ACTION_IMPORT_DEMO_PROJECT = "IMPORT_DEMO_PROJECT"; //$NON-NLS-1$

    protected static final String FINISH_ACTION_IMPORT_LOCAL_PROJECT = "IMPORT_LOCAL_PROJECT"; //$NON-NLS-1$

    protected static final String FINISH_ACTION_CREATE_NEW_PROJECT = "CREATE_NEW_PROJECT"; //$NON-NLS-1$

    protected static ConnectionBean bean = null;

    protected static Logger log = Logger.getLogger(LoginProjectPage.class);

    protected Label title;

    protected Composite connectionManageArea;

    protected ComboViewer connectionsViewer;

    protected Button manageButton;

    protected Composite projectOperationArea;

    protected Button selectExistingProject;

    protected Composite projectListArea;

    protected ListViewer projectViewer;

    protected Composite branchArea;

    protected Label branchLabel;

    protected ComboViewer branchesViewer;

    protected Button refreshProjectButton;

    protected Button createNewProject;

    protected Text newProjectName;

    protected Button executeCreateNewProject;

    protected Button importDemoProject;

    protected Button executeImportDemoProject;

    protected Button importLocalProject;

    protected Button executeImportLocalProject;

    protected Button createSandBoxProject;

    protected Button executeCreateSandBoxProject;

    protected Project currentProjectSettings;

    protected Job backgroundGUIUpdate;

    private String selectedProjectBeforeRefresh;

    // protected ConnectionBean beforeConnBean;

    protected String finishButtonAction;

    protected boolean afterUpdate = false;

    protected IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
            IBrandingService.class);

    protected LoginHelper loginHelper;

    protected LoginFetchLicenseHelper loginFetchLicenseHelper;

    public LoginProjectPage(Composite parent, LoginDialogV2 dialog, int style) {
        super(parent, dialog, style);
    }

    @Override
    public void preCreateControl() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init() throws Throwable {
        super.init();
        finishButtonAction = FINISH_ACTION_OPEN_PROJECT;
        loginHelper = LoginHelper.getInstance();
        loginFetchLicenseHelper = LoginFetchLicenseHelper.getInstance();
    }

    @Override
    public void instantiateControl(Composite container) {

        /**
         * 1. Create all the control first
         */

        String execute = Messages.getString("LoginProjectPage.execute"); //$NON-NLS-1$
        // Image openImage = ImageProvider.getImage(ERepositoryImages.OPEN_PROJECT_ICON);
        // Connection Area
        title = new Label(container, SWT.NONE);
        title.setFont(LoginDialogV2.fixedFont);
        title.setText(Messages.getString("LoginProjectPage.title")); //$NON-NLS-1$
        connectionManageArea = new Composite(container, SWT.NONE);
        connectionsViewer = new ComboViewer(connectionManageArea, SWT.READ_ONLY);
        connectionsViewer.getControl().setFont(LoginDialogV2.fixedFont);
        manageButton = new Button(connectionManageArea, SWT.NONE);
        manageButton.setFont(LoginDialogV2.fixedFont);
        manageButton.setBackground(backgroundBtnColor);
        manageButton.setText(Messages.getString("LoginProjectPage.manage")); //$NON-NLS-1$
        // Project Operation Area
        projectOperationArea = new Composite(container, SWT.NONE);
        // Existing Project Area
        selectExistingProject = new Button(projectOperationArea, SWT.RADIO);
        selectExistingProject.setFont(LoginDialogV2.fixedFont);
        selectExistingProject.setBackground(backgroundRadioColor);
        selectExistingProject.setText(Messages.getString("LoginProjectPage.selectProject")); //$NON-NLS-1$
        projectListArea = new Composite(projectOperationArea, SWT.NONE);
        projectViewer = new ListViewer(projectListArea, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        projectViewer.getControl().setFont(LoginDialogV2.fixedFont);
        projectViewer.setContentProvider(ArrayContentProvider.getInstance());
        projectViewer.setLabelProvider(new ProjectLabelProvider());
        projectViewer.setSorter(new ViewerSorter());
        // Branch Area
        branchArea = new Composite(projectListArea, SWT.NONE);
        branchLabel = new Label(branchArea, SWT.NONE);
        branchLabel.setFont(LoginDialogV2.fixedFont);
        branchLabel.setText(Messages.getString("LoginProjectPage.Branch")); //$NON-NLS-1$
        branchesViewer = new ComboViewer(branchArea, SWT.READ_ONLY);
        branchesViewer.getControl().setFont(LoginDialogV2.fixedFont);
        branchesViewer.setContentProvider(ArrayContentProvider.getInstance());
        branchesViewer.setLabelProvider(new LabelProvider());
        refreshProjectButton = new Button(branchArea, SWT.NONE);
        refreshProjectButton.setFont(LoginDialogV2.fixedFont);
        refreshProjectButton.setBackground(backgroundBtnColor);
        refreshProjectButton.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        // Create New Project
        createNewProject = new Button(projectOperationArea, SWT.RADIO);
        createNewProject.setFont(LoginDialogV2.fixedFont);
        createNewProject.setBackground(backgroundRadioColor);
        createNewProject.setText(Messages.getString("LoginProjectPage.createNewProject")); //$NON-NLS-1$
        newProjectName = new Text(projectOperationArea, SWT.BORDER);
        newProjectName.setFont(LoginDialogV2.fixedFont);
        newProjectName.setBackground(backgroundBtnColor);
        executeCreateNewProject = new Button(projectOperationArea, SWT.NONE);
        executeCreateNewProject.setFont(LoginDialogV2.fixedFont);
        executeCreateNewProject.setText(Messages.getString("LoginProjectPage.create")); //$NON-NLS-1$
        executeCreateNewProject.setBackground(backgroundBtnColor);
        // Import Demo Project
        importDemoProject = new Button(projectOperationArea, SWT.RADIO);
        importDemoProject.setFont(LoginDialogV2.fixedFont);
        importDemoProject.setBackground(backgroundRadioColor);
        importDemoProject.setText(Messages.getString("LoginProjectPage.importDemoProject")); //$NON-NLS-1$
        executeImportDemoProject = new Button(projectOperationArea, SWT.NONE);
        executeImportDemoProject.setFont(LoginDialogV2.fixedFont);
        // executeImportDemoProject.setImage(openImage);
        executeImportDemoProject.setText(execute);
        executeImportDemoProject.setBackground(backgroundBtnColor);
        // Import Local Project
        importLocalProject = new Button(projectOperationArea, SWT.RADIO);
        importLocalProject.setFont(LoginDialogV2.fixedFont);
        importLocalProject.setBackground(backgroundRadioColor);
        importLocalProject.setText(Messages.getString("LoginProjectPage.importLocalProject")); //$NON-NLS-1$
        executeImportLocalProject = new Button(projectOperationArea, SWT.NONE);
        executeImportLocalProject.setFont(LoginDialogV2.fixedFont);
        // executeImportLocalProject.setImage(openImage);
        executeImportLocalProject.setText(execute);
        executeImportLocalProject.setBackground(backgroundBtnColor);
        // Create SandBox Project
        createSandBoxProject = new Button(projectOperationArea, SWT.RADIO);
        createSandBoxProject.setFont(LoginDialogV2.fixedFont);
        createSandBoxProject.setBackground(backgroundRadioColor);
        createSandBoxProject.setText(Messages.getString("LoginProjectPage.createSandBoxProject.title")); //$NON-NLS-1$
        executeCreateSandBoxProject = new Button(projectOperationArea, SWT.NONE);
        executeCreateSandBoxProject.setFont(LoginDialogV2.fixedFont);
        // executeCreateSandBoxProject.setImage(openImage);
        executeCreateSandBoxProject.setText(execute);
        executeCreateSandBoxProject.setBackground(backgroundBtnColor);

        super.instantiateControl(container);
    }

    @Override
    protected void layoutControl() {
        /**
         * 2. Layout all the control
         */
        super.layoutControl();

        FormData formData = null;

        /**
         * 2.1 Layout Bottom Navigation Area
         */
        layoutNavigatorArea();

        /**
         * 2.2 Layout Connection Area
         */
        formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, 0);
        title.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(title, TAB_VERTICAL_PADDING_LEVEL_2, SWT.BOTTOM);
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        connectionManageArea.setLayoutData(formData);
        GridLayout gridLayout = new GridLayout(2, true);
        gridLayout.horizontalSpacing = TAB_HORIZONTAL_PADDING_LEVEL_2;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        connectionManageArea.setLayout(gridLayout);

        // formData = new FormData();
        // formData.top = new FormAttachment(title, TAB_VERTICAL_PADDING_LEVEL_2, SWT.BOTTOM);
        // formData.right = new FormAttachment(100, 0);
        // formData.left = new FormAttachment(100, -1 * LoginDialogV2.getNewButtonSize(manageButton).x);
        // manageButton.setLayoutData(formData);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        manageButton.setLayoutData(gridData);

        // formData = new FormData();
        // formData.top = new FormAttachment(manageButton, 0, SWT.CENTER);
        // formData.bottom = new FormAttachment(manageButton, 0, SWT.CENTER);
        // formData.left = new FormAttachment(title, 0, SWT.LEFT);
        // formData.right = new FormAttachment(manageButton, -1 * TAB_HORIZONTAL_PADDING_LEVEL_2, SWT.LEFT);
        // connectionsViewer.getControl().setLayoutData(formData);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        connectionsViewer.getControl().setLayoutData(gridData);
        connectionsViewer.setContentProvider(ArrayContentProvider.getInstance());
        connectionsViewer.setLabelProvider(new ConnectionLabelProvider());

        /**
         * 2.3 Layout Project Operation Area
         */
        int operationButtonPadding = 6;

        formData = new FormData();
        // formData.top = new FormAttachment(manageButton, 0, SWT.BOTTOM);
        formData.top = new FormAttachment(connectionManageArea, 0, SWT.BOTTOM);
        formData.bottom = new FormAttachment(navigateArea, 0, SWT.TOP);
        // formData.left = new FormAttachment(connectionsViewer.getControl(), 0, SWT.LEFT);
        formData.left = new FormAttachment(connectionManageArea, 0, SWT.LEFT);
        formData.right = new FormAttachment(100, 0);
        projectOperationArea.setLayout(new FormLayout());
        projectOperationArea.setLayoutData(formData);

        formData = new FormData();
        formData.bottom = new FormAttachment(100, -1 * TAB_VERTICAL_PADDING_LEVEL_1);
        formData.left = new FormAttachment(0, 0);
        createSandBoxProject.setLayoutData(formData);
        formData = new FormData();
        formData.top = new FormAttachment(createSandBoxProject, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(createSandBoxProject, 0, SWT.CENTER);
        formData.right = new FormAttachment(100, -1 * TAB_HORIZONTAL_PADDING_LEVEL_1);
        formData.width = LoginDialogV2.getNewButtonSize(executeCreateSandBoxProject, operationButtonPadding).x;
        executeCreateSandBoxProject.setLayoutData(formData);

        formData = new FormData();
        formData.bottom = new FormAttachment(createSandBoxProject, -1 * TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);
        formData.left = new FormAttachment(0, 0);
        importLocalProject.setLayoutData(formData);
        formData = new FormData();
        formData.top = new FormAttachment(importLocalProject, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(importLocalProject, 0, SWT.CENTER);
        formData.right = new FormAttachment(100, -1 * TAB_HORIZONTAL_PADDING_LEVEL_1);
        formData.width = LoginDialogV2.getNewButtonSize(executeImportLocalProject, operationButtonPadding).x;
        executeImportLocalProject.setLayoutData(formData);

        formData = new FormData();
        formData.bottom = new FormAttachment(importLocalProject, -1 * TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);
        formData.left = new FormAttachment(0, 0);
        importDemoProject.setLayoutData(formData);
        formData = new FormData();
        formData.top = new FormAttachment(importDemoProject, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(importDemoProject, 0, SWT.CENTER);
        formData.right = new FormAttachment(100, -1 * TAB_HORIZONTAL_PADDING_LEVEL_1);
        formData.width = LoginDialogV2.getNewButtonSize(executeImportDemoProject, operationButtonPadding).x;
        executeImportDemoProject.setLayoutData(formData);

        formData = new FormData();
        formData.bottom = new FormAttachment(importDemoProject, -1 * TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);
        formData.left = new FormAttachment(0, 0);
        createNewProject.setLayoutData(formData);
        formData = new FormData();
        formData.top = new FormAttachment(createNewProject, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(createNewProject, 0, SWT.CENTER);
        formData.right = new FormAttachment(100, -1 * TAB_HORIZONTAL_PADDING_LEVEL_1);
        formData.width = LoginDialogV2.getNewButtonSize(executeCreateNewProject, operationButtonPadding).x;
        executeCreateNewProject.setLayoutData(formData);
        formData = new FormData();
        formData.top = new FormAttachment(createNewProject, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(createNewProject, 0, SWT.CENTER);
        formData.right = new FormAttachment(executeCreateNewProject, -1 * TAB_HORIZONTAL_PADDING_LEVEL_2, SWT.LEFT);
        formData.left = new FormAttachment(createNewProject, TAB_HORIZONTAL_PADDING_LEVEL_2, SWT.RIGHT);
        newProjectName.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(0, TAB_VERTICAL_PADDING_LEVEL_1);
        formData.left = new FormAttachment(0, 0);
        selectExistingProject.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(0, TAB_HORIZONTAL_PADDING_LEVEL_1);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(selectExistingProject, TAB_VERTICAL_PADDING_LEVEL_2, SWT.BOTTOM);
        formData.bottom = new FormAttachment(createNewProject, -1 * TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);
        projectListArea.setLayoutData(formData);

        layoutProjectListArea();
    }

    protected void layoutProjectListArea() {
        projectListArea.setLayout(new FormLayout());
        FormData formData = null;

        /**
         * BranchArea layout
         */
        formData = new FormData();
        formData.left = new FormAttachment(0, TAB_HORIZONTAL_PADDING_LEVEL_3);
        formData.right = new FormAttachment(100, 0);
        formData.bottom = new FormAttachment(100, 0);
        branchArea.setLayoutData(formData);
        branchArea.setLayout(new FormLayout());

        int svnBranchLabelWidth = branchLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        int refreshButtonWidth = refreshProjectButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

        formData = new FormData();
        formData.bottom = new FormAttachment(100, 0);
        formData.right = new FormAttachment(100, -1 * refreshButtonWidth - TAB_HORIZONTAL_PADDING_LEVEL_2);
        formData.left = new FormAttachment(0, svnBranchLabelWidth + TAB_HORIZONTAL_PADDING_LEVEL_2);
        Control svnBranchComboControl = branchesViewer.getControl();
        svnBranchComboControl.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(svnBranchComboControl, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(svnBranchComboControl, 0, SWT.CENTER);
        formData.left = new FormAttachment(0, 0);
        branchLabel.setLayoutData(formData);

        formData = new FormData();
        formData.right = new FormAttachment(100, 0);
        if (EnvironmentUtils.isWindowsSystem()) {
            formData.top = new FormAttachment(svnBranchComboControl, -1, SWT.TOP);
            formData.bottom = new FormAttachment(svnBranchComboControl, 1, SWT.BOTTOM);
        } else {
            formData.top = new FormAttachment(svnBranchComboControl, 0, SWT.CENTER);
            formData.bottom = new FormAttachment(svnBranchComboControl, 0, SWT.CENTER);
        }
        refreshProjectButton.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.bottom = new FormAttachment(branchArea, -1 * TAB_VERTICAL_PADDING_LEVEL_2, SWT.TOP);
        projectViewer.getControl().setLayoutData(formData);

    }

    private boolean isNeedSandboxProject;

    protected boolean isNeedSandboxProject() {
        isNeedSandboxProject = LoginHelper.isRemotesConnection(getConnection());
        if (isNeedSandboxProject) {
            BusyIndicator.showWhile(getDisplay(), new Runnable() {

                @Override
                public void run() {
                    try {
                        isNeedSandboxProject = ProxyRepositoryFactory.getInstance().enableSandboxProject();
                    } catch (Exception e) {
                        CommonExceptionHandler.process(e);
                    }
                }
            });
        } else {
            return false;
        }
        return isNeedSandboxProject;
    }

    @Override
    public void afterCreateControl() {
        resetProjectOperationSelectionWithBusyCursor(false);
        alwaysAsk.setSelection(LoginHelper.isAlwaysAskAtStartup());
        previousButton.setVisible(false);
    }

    protected void fillUIContentsWithBusyCursor() {
        BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
                fillUIContents();
            }
        });
    }

    protected void fillUIContents() {
        boolean isOnlyRemoteConnection = brandingService.getBrandingConfiguration().isOnlyRemoteConnection();
        List<ConnectionBean> storedConnections = loginHelper.getStoredConnections();
        storedConnections = loginHelper.filterUsableConnections(storedConnections);
        for (ConnectionBean connectionBean : storedConnections) {
            String user2 = connectionBean.getUser();
            String repositoryId2 = connectionBean.getRepositoryId();
            String workSpace = connectionBean.getWorkSpace();
            String name = connectionBean.getName();
            if (user2 != null && !"".equals(user2) && repositoryId2 != null && !"".equals(repositoryId2) && workSpace != null //$NON-NLS-1$ //$NON-NLS-2$
                    && !"".equals(workSpace) && name != null && !"".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
                boolean valid = false;
                if (LoginHelper.isRemotesConnection(connectionBean)) {
                    String url = connectionBean.getDynamicFields().get(RepositoryConstants.REPOSITORY_URL);
                    valid = url != null && !"".equals(url); //$NON-NLS-1$
                } else {
                    valid = Pattern.matches(RepositoryConstants.MAIL_PATTERN, user2);
                }
                connectionBean.setComplete(valid);
            }
        }
        if (!isOnlyRemoteConnection) {
            connectionsViewer.setInput(storedConnections);
        } else {
            // feature 8,hide error remote connection for Uniserv after their login validate
            List<ILoginConnectionService> loginConnectionServices = LoginConnectionManager.getRemoteConnectionService();
            List<ConnectionBean> lastRemoteConnections = new ArrayList<ConnectionBean>();
            if (loginConnectionServices.size() > 0) {
                for (ILoginConnectionService loginConncetion : loginConnectionServices) {
                    for (ConnectionBean iBean : storedConnections) {
                        String errorMsg = loginConncetion.checkConnectionValidation(iBean.getName(), iBean.getDescription(),
                                iBean.getUser(), iBean.getPassword(), iBean.getWorkSpace(),
                                iBean.getDynamicFields().get(RepositoryConstants.REPOSITORY_URL));
                        if (StringUtils.isEmpty(errorMsg) && iBean.isComplete()) {
                            lastRemoteConnections.add(iBean);
                        }
                    }
                }
            }
            if (lastRemoteConnections.size() > 0) {
                storedConnections = lastRemoteConnections;
            }
            connectionsViewer.setInput(storedConnections);
        }

        // Check number of connection available.
        if (storedConnections.size() == 0) {
            //
        } else if (storedConnections.size() == 1) {
            connectionsViewer.setSelection(new StructuredSelection(new Object[] { storedConnections.get(0) }));
        } else {
            // select last connection used
            boolean selected = false;
            // for (ConnectionBean curent : storedConnections) {
            // String stringValue = ((LabelProvider) connectionsViewer.getLabelProvider()).getText(curent);
            // if (curent.getName().equals( lastConnection)) {
            // selectLast(stringValue, connectionsViewer.getCombo());
            // selected = true;
            // }
            // }
            ConnectionBean selectedConnBean = loginHelper.getCurrentSelectedConnBean();
            if (selectedConnBean != null) {
                connectionsViewer.setSelection(new StructuredSelection(new Object[] { selectedConnBean }));
                IStructuredSelection sel = (IStructuredSelection) connectionsViewer.getSelection();
                if (selectedConnBean.equals(sel.getFirstElement())) {
                    selected = true;
                }
            }
            if (!selected) {
                connectionsViewer.setSelection(new StructuredSelection(new Object[] { storedConnections.get(0) }));
            }
        }
        ConnectionBean selectedConnBean = getConnection();
        if (selectedConnBean != null) {
            loginHelper.setCurrentSelectedConnBean(selectedConnBean);
        }

        if (getConnection() != null || !validateFields()) {
            setRepositoryContextInContext();
        }

        fillUIProjectListWithBusyCursor();

        refreshNecessaryVisible();
    }

    @Override
    public void refreshUIData() {
        fillUIContentsWithBusyCursor();
    }

    @Override
    public void check() {
        try {
            checkErrors();
            log.info("validate updatesite..."); //$NON-NLS-1$
            validateUpdate();
        } catch (PersistenceException e) {
            CommonExceptionHandler.process(e);
            log.error(e);
        } catch (JSONException e) {
            CommonExceptionHandler.process(e);
            log.error(e);
        }
    }

    @Override
    protected void addListeners() {
        super.addListeners();

        connectionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                try {
                    resetProjectOperationSelectionWithBusyCursor(false);
                    final ConnectionBean connection = getConnection();
                    if (connection == null) {
                        checkErrors();
                        return;
                    }
                    // if (beforeConnBean != null && connection.equals(beforeConnBean)) {
                    // return;
                    // }
                    if (connection.equals(loginHelper.getCurrentSelectedConnBean())) {
                        // in case they are equal but different object id
                        loginHelper.setCurrentSelectedConnBean(connection);
                        return;
                    } else {
                        loginHelper.setCurrentSelectedConnBean(connection);
                    }
                    loginFetchLicenseHelper.cancelAndClearFetchJobs();
                    errorManager.clearAllMessages();
                    // beforeConnBean = connection;
                    updateServerFields();

                    // Validate data
                    if (validateFields()) {
                        fillUIProjectListWithBusyCursor();
                        validateProject();
                    }
                    checkErrors();
                    validateUpdate();
                    if (errorManager.isHasAuthException()) {
                        handleOpenConnectionsDialog(true);
                    }
                } catch (PersistenceException e) {
                    CommonExceptionHandler.process(e);
                } catch (JSONException e) {
                    CommonExceptionHandler.process(e);
                } finally {
                    TalendProxySelector.checkProxy();
                }
            }

        });

        selectExistingProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selectExistingProject.getSelection()) {
                    // refreshProjectListAreaEnable(true);
                    finishButtonAction = FINISH_ACTION_OPEN_PROJECT;
                    // changeFinishButtonAction(finishButtonAction);
                    try {
                        checkErrors();
                    } catch (PersistenceException e1) {
                        CommonExceptionHandler.process(e1);
                    }
                } else {
                    refreshProjectListAreaEnable(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        createSandBoxProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (createSandBoxProject.getSelection()) {
                    finishButtonAction = FINISH_ACTION_CREATE_SANDBOX;
                    // changeFinishButtonAction(finishButtonAction);
                    refreshUIFinishButtonEnable(false);
                    executeCreateSandBoxProject.setVisible(true);
                    executeCreateSandBoxProject.forceFocus();
                } else {
                    executeCreateSandBoxProject.setVisible(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        importDemoProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (importDemoProject.getSelection()) {
                    finishButtonAction = FINISH_ACTION_IMPORT_DEMO_PROJECT;
                    // changeFinishButtonAction(finishButtonAction);
                    refreshUIFinishButtonEnable(false);
                    executeImportDemoProject.setVisible(true);
                    executeImportDemoProject.forceFocus();
                } else {
                    executeImportDemoProject.setVisible(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        importLocalProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (importLocalProject.getSelection()) {
                    finishButtonAction = FINISH_ACTION_IMPORT_LOCAL_PROJECT;
                    // changeFinishButtonAction(finishButtonAction);
                    refreshUIFinishButtonEnable(false);
                    executeImportLocalProject.setVisible(true);
                    executeImportLocalProject.forceFocus();
                } else {
                    executeImportLocalProject.setVisible(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        createNewProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (createNewProject.getSelection()) {
                    createNewProject.setText(Messages.getString("LoginProjectPage.createNewProject.beginCreate")); //$NON-NLS-1$
                    createNewProject.pack();
                    FormData formData = (FormData) newProjectName.getLayoutData();
                    formData.left = new FormAttachment(createNewProject, createNewProject.getSize().x
                            + TAB_HORIZONTAL_PADDING_LEVEL_2, SWT.LEFT);
                    newProjectName.setVisible(true);
                    newProjectName.forceFocus();
                    executeCreateNewProject.setVisible(true);
                    finishButtonAction = FINISH_ACTION_CREATE_NEW_PROJECT;
                    // changeFinishButtonAction(finishButtonAction);
                    refreshUIFinishButtonEnable(false);
                    validateNewProjectName();
                } else {
                    createNewProject.setText(Messages.getString("LoginProjectPage.createNewProject")); //$NON-NLS-1$
                    newProjectName.setVisible(false);
                    executeCreateNewProject.setVisible(false);
                    getErrorManager().clearCreateNewProjectError();
                    // refreshUIFinishButtonEnable();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        projectViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                finishButton.setEnabled(false);
                Project project = getProject();
                if (project != null) {
                    selectedProjectBeforeRefresh = project.getLabel();

                    // last used project will be saved when click finish
                    // loginHelper.getPrefManipulator().setLastProject(project.getLabel());
                    loginFetchLicenseHelper.fetchLicenseIfNeeded(project);
                    try {
                        fillUIBranches(project, false);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        ExceptionHandler.process(e);
                    }
                    setRepositoryContextInContext();
                }
                finishButton.setEnabled(true);
            }
        });

        branchesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {

                // last used branch of project will be saved when click finish
                // String branch = getBranch();
                // if (branch == null) {
                // branch = SVNConstant.EMPTY;
                // }
                // Project project = getProject();
                // try {
                // loginHelper.getPrefManipulator().setLastSVNBranch(
                // new JSONObject(project.getEmfProject().getUrl()).getString("location"), project.getTechnicalLabel(),
                // branch);
                // } catch (JSONException e) {
                // // TODO Auto-generated catch block
                // ExceptionHandler.process(e);
                // }
            }
        });

        manageButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOpenConnectionsDialog(false);
            }
        });

        finishButton.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                finishButton.removePaintListener(this);
                // for start, page showed complete
                if (errorManager.isHasAuthException()) {
                    handleOpenConnectionsDialog(true);
                }
            }
        });

        refreshProjectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // reset flag to connect again
                errorManager.setAuthException(null);
                errorManager.setHasAuthException(false);

                loginFetchLicenseHelper.cancelAndClearFetchJobs();
                LoginProjectPage.this.selectedProjectBeforeRefresh = getProject() == null ? null : getProject().getLabel();
                // Validate data
                if (validateFields()) {
                    fillUIProjectListWithBusyCursor();
                    validateProject();
                }
                try {
                    checkErrors();
                    validateUpdate();
                } catch (PersistenceException e1) {
                    CommonExceptionHandler.process(e1);
                } catch (JSONException e1) {
                    CommonExceptionHandler.process(e1);
                }
                setRepositoryContextInContext();
                LoginProjectPage.this.selectedProjectBeforeRefresh = null;
                if (errorManager.isHasAuthException()) {
                    handleOpenConnectionsDialog(true);
                }
            }
        });

        newProjectName.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validateNewProjectName();
            }
        });

        executeCreateNewProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewProjectWithBusyCursor();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        executeCreateSandBoxProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                createSandboxProject();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        executeImportDemoProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                importDemoProject();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        executeImportLocalProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                importProjects();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        finishButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (FINISH_ACTION_OPEN_PROJECT.equals(finishButtonAction)) {
                    setRepositoryContextInContext();
                    finishPressed();
                } else if (FINISH_ACTION_RESTART.equals(finishButtonAction)) {
                    restartStudio();
                } else if (FINISH_ACTION_UPDATE.equals(finishButtonAction)
                        || FINISH_ACTION_UPDATE_DETAILS.equals(finishButtonAction)) {
                    updateStudio();
                } else if (FINISH_ACTION_CREATE_SANDBOX.equals(finishButtonAction)) {
                    createSandboxProject();
                } else if (FINISH_ACTION_IMPORT_DEMO_PROJECT.equals(finishButtonAction)) {
                    importDemoProject();
                } else if (FINISH_ACTION_IMPORT_LOCAL_PROJECT.equals(finishButtonAction)) {
                    importProjects();
                } else if (FINISH_ACTION_CREATE_NEW_PROJECT.equals(finishButtonAction)) {
                    createNewProjectWithBusyCursor();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

    }

    public void finishPressed() {
        if (LoginHelper.isRestart) {
            loginDialog.okPressed();
        } else {
            // should save before login, since svn related codes will read them
            saveLastUsedProjectAndBranch();
            boolean isLogInOk = loginHelper.logIn(getConnection(), getProject(), errorManager);
            if (isLogInOk) {
                LoginHelper.setAlwaysAskAtStartup(alwaysAsk.getSelection());
                loginDialog.okPressed();
            } else {
                fillUIProjectListWithBusyCursor();
                revertUpdateStatus();
                if (errorManager.isHasAuthException()) {
                    handleOpenConnectionsDialog(true);
                }
            }
        }
    }

    public void handleOpenConnectionsDialog(boolean showError) {
        try {
            if (showError && errorManager.isHasAuthException()) {
                String[] dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL };
                MessageDialog dialog = new ExceptionMessageDialog(getShell(),
                        Messages.getString("LoginProjectPage.errorMessageTitle"), null, //$NON-NLS-1$
                        Messages.getString("LoginProjectPage.authorizationErrorMessage"), MessageDialog.ERROR, //$NON-NLS-1$
                        dialogButtonLabels, 0, errorManager.getAuthException());
                if (dialog.open() == Window.CANCEL) {
                    return;
                }
            }
            ConnectionsDialog connectionsDialog = new ConnectionsDialog(getShell(), getConnection(),
                    errorManager.isHasAuthException());
            int open = connectionsDialog.open();
            if (open == Window.OK) {
                List<ConnectionBean> storedConnections = connectionsDialog.getConnections();
                loginHelper.setStoredConnections(storedConnections);
                loginHelper.saveConnections();
                // reset flag to connect again
                errorManager.setAuthException(null);
                errorManager.setHasAuthException(false);
                fillUIContentsWithBusyCursor();
                final ConnectionBean connection = getConnection();
                if (connection == null) {
                    checkErrors();
                    return;
                }
                // beforeConnBean = connection;

                updateServerFields();

                // Validate data
                if (validateFields()) {
                    fillUIProjectListWithBusyCursor();
                    validateProject();
                }
                checkErrors();
                validateUpdate();
                if (errorManager.isHasAuthException()) {
                    handleOpenConnectionsDialog(true);
                }
            } else if (!LoginHelper.isRemotesConnection(getConnection())) {
                fillUIProjectListWithBusyCursor();
                validateProject();
                checkErrors();
            }
            // setStatusArea();
        } catch (PersistenceException e1) {
            CommonExceptionHandler.process(e1);
        } catch (JSONException e2) {
            CommonExceptionHandler.process(e2);
        }
    }

    private void revertUpdateStatus() {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        repositoryContext.setNoUpdateWhenLogon(false);
    }

    private void saveLastUsedProjectAndBranch() {

        Project project = getProject();
        loginHelper.getPrefManipulator().setLastProject(project.getLabel());

        if (LoginHelper.isRemotesConnection(getConnection())) {
            String branch = getBranch();
            if (branch == null) {
                branch = SVNConstant.EMPTY;
            }
            try {
                loginHelper.getPrefManipulator().setLastSVNBranch(
                        new JSONObject(project.getEmfProject().getUrl()).getString("location"), project.getTechnicalLabel(), //$NON-NLS-1$
                        branch);
            } catch (JSONException e) {
                ExceptionHandler.process(e);
            }
        } else {
            try {
                String jsonStr = project.getEmfProject().getUrl();
                if (jsonStr != null && !jsonStr.isEmpty()) {
                    String lastLogonBranch = loginHelper.getPrefManipulator()
                            .getLastSVNBranch(new JSONObject(jsonStr).getString("location"), project.getTechnicalLabel()); //$NON-NLS-1$
                    ProjectManager.getInstance().setMainProjectBranch(project, lastLogonBranch);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

        }

    }

    @Override
    public Object getCheckedErrors() {
        // TODO Auto-generated method stub
        return null;
    }

    protected void updateStudio() {
        // install and update all patches;
        try {
            if (FINISH_ACTION_UPDATE.equals(finishButtonAction)) {
                ICoreTisService tisService = (ICoreTisService) GlobalServiceRegister.getDefault().getService(
                        ICoreTisService.class);
                afterUpdate = false;
                if (tisService != null) {
                    if (tisService.isDefaultLicenseAndProjectType()) {
                        tisService.downLoadAndInstallUpdates(getConnection().getUser(), getConnection().getPassword(),
                                loginFetchLicenseHelper.getAdminURL());
                    } else {
                        EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand(EclipseCommandLine.LOGIN_ONLINE_UPDATE, null,
                                false, true);
                        EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand(EclipseCommandLine.CLEAN, null, false, true);
                        EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand(EclipseCommandLine.TALEND_RELOAD_COMMAND,
                                Boolean.TRUE.toString(), false);
                        EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand(
                                EclipseCommandLine.TALEND_PROJECT_TYPE_COMMAND, "", true);
                        EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand(EclipseCommandLine.ARG_TALEND_LICENCE_PATH,
                                "", true);
                    }
                    afterUpdate = true;
                    tisService.setNeedResartAfterUpdate(afterUpdate);
                }
                // need to relauch the studio automaticlly after updating
                LoginHelper.isRestart = true;
                loginHelper.saveLastConnectionBean(getConnection());
                finishPressed();
            } else {
                String[] buttons = new String[] { IDialogConstants.OK_LABEL };
                String message = Messages.getString("LoginComposite.archivaJarLost"); //$NON-NLS-1$
                ArchivaErrorDialog archivaDialog = new ArchivaErrorDialog(getShell(), "Warning", null, message,
                        MessageDialog.WARNING, buttons, 0);
                archivaDialog.open();
            }
        } catch (Throwable e1) {
            CommonExceptionHandler.process(e1);
            ExceptionMessageDialog.openError(getShell(), Messages.getString("LoginProjectPage.update.error.title"), //$NON-NLS-1$
                    Messages.getString("LoginProjectPage.update.error.message"), e1); //$NON-NLS-1$
        }
    }

    protected void restartStudio() {
        LoginHelper.isRestart = true;
        ConnectionBean iBean = getConnection();
        loginHelper.saveLastConnectionBean(iBean);
        // update the restart command line to specify the workspace to launch
        // if relaunch, should delete the "disableLoginDialog" argument in eclipse data for bug TDI-19214
        String workspace = iBean.getWorkSpace();
        if (workspace != null) {
            URI uri = new File(workspace).toURI();
            workspace = uri.toString();
            workspace = workspace.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$//$NON-NLS-2$
        }
        EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand("-data", workspace, false); //$NON-NLS-1$
        // store the workspace in the eclipse history so that it is rememebered on next studio launch
        ChooseWorkspaceData workspaceData = new ChooseWorkspaceData(""); //$NON-NLS-1$
        workspaceData.workspaceSelected(iBean.getWorkSpace());
        workspaceData.writePersistedData();
        finishPressed();
    }

    protected void resetProjectOperationSelectionWithBusyCursor() {
        resetProjectOperationSelection(true);
    }

    protected void resetProjectOperationSelectionWithBusyCursor(final boolean needCheckError) {
        BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
                resetProjectOperationSelection(needCheckError);
            }
        });
    }

    protected void resetProjectOperationSelection(final boolean needCheckError) {
        selectExistingProject.setSelection(true);
        finishButtonAction = FINISH_ACTION_OPEN_PROJECT;
        refreshProjectListAreaEnable(selectExistingProject.isEnabled());
        importDemoProject.setSelection(false);
        executeImportDemoProject.setVisible(false);
        importLocalProject.setSelection(false);
        executeImportLocalProject.setVisible(false);
        createSandBoxProject.setSelection(false);
        executeCreateSandBoxProject.setVisible(false);
        createNewProject.setSelection(false);
        createNewProject.setText(Messages.getString("LoginProjectPage.createNewProject")); //$NON-NLS-1$
        newProjectName.setVisible(false);
        executeCreateNewProject.setVisible(false);
        if (needCheckError) {
            try {
                checkErrors();
            } catch (PersistenceException e) {
                CommonExceptionHandler.process(e);
            }
        } else {
            changeFinishButtonAction();
            finishButton.getShell().setDefaultButton(finishButton);
        }
    }

    // protected void initConnection() {
    // if (storedConnections == null || storedConnections.size() == 0) {
    // getConnection();
    // }
    // }

    protected void refreshBranchAreaVisible() {
        boolean tisRemote = isSVNProviderPluginLoadedRemote();
        refreshBranchAreaVisible(tisRemote);
    }

    protected void refreshProjectOperationAreaEnable(boolean isEnable) {
        refreshProjectListAreaEnable(isEnable && selectExistingProject.getSelection());
        projectOperationArea.setEnabled(isEnable);
        selectExistingProject.setEnabled(isEnable);
        importDemoProject.setEnabled(isEnable);
        executeImportDemoProject.setEnabled(isEnable);
        importLocalProject.setEnabled(isEnable);
        executeImportLocalProject.setEnabled(isEnable);
        createSandBoxProject.setEnabled(isEnable);
        executeCreateSandBoxProject.setEnabled(isEnable);
        createNewProject.setEnabled(isEnable);
        executeCreateNewProject.setEnabled(isEnable);
        alwaysAsk.setEnabled(isEnable);
    }

    protected void refreshProjectListAreaEnable() {
        refreshProjectListAreaEnable(selectExistingProject.getSelection());
    }

    protected void refreshProjectListAreaEnable(boolean isEnable) {
        refreshNecessaryVisible();
        projectListArea.setEnabled(isEnable);
        projectViewer.getControl().setEnabled(isEnable);
        branchArea.setEnabled(isEnable);
        branchLabel.setEnabled(isEnable);
        branchesViewer.getControl().setEnabled(isEnable);
        refreshProjectButton.setEnabled(isEnable);
    }

    protected void refreshNecessaryVisible() {
        refreshNecessaryVisible(LoginHelper.isRemotesConnection(getConnection()));
    }

    protected void refreshNecessaryVisible(boolean isRemote) {
        boolean isSVNPluginLoaded = PluginChecker.isSVNProviderPluginLoaded();

        boolean needSandboxProject = isRemote;
        if (!errorManager.isHasAuthException()) {
            // connect administrator exist error, avoid check isNeedSandboxProject from remote
            needSandboxProject = isNeedSandboxProject();
        }
        refreshCreateSandboxProjectVisible(needSandboxProject);
        refreshImportLocalProjectVisible(!isRemote);
        refreshImportDemoProjectVisible(!isRemote);
        refreshCreateNewProjectVisible(!isRemote);
        refreshBranchAreaVisible(isSVNPluginLoaded && isRemote);

        Control projectListAreaBottomControl = null;
        if (isSVNPluginLoaded) {
            if (isRemote) {
                if (needSandboxProject) {
                    projectListAreaBottomControl = createSandBoxProject;
                } else {
                    projectListAreaBottomControl = navigateArea;
                }
            }
        }
        if (projectListAreaBottomControl == null) {
            projectListAreaBottomControl = createNewProject;
        }
        FormData formData = (FormData) projectListArea.getLayoutData();
        if (projectListAreaBottomControl == navigateArea) {
            formData.bottom = new FormAttachment(100, -1 * TAB_VERTICAL_PADDING_LEVEL_1);
        } else {
            formData.bottom = new FormAttachment(projectListAreaBottomControl, -1 * TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);
        }

        projectOperationArea.layout();

        projectOperationArea.update();
    }

    protected void refreshCreateSandboxProjectVisible(boolean visible) {
        createSandBoxProject.setVisible(visible);
        FormData formData = (FormData) createSandBoxProject.getLayoutData();
        if (visible) {
            formData.height = -1;
            formData.bottom.offset = -1 * TAB_VERTICAL_PADDING_LEVEL_1;
        } else {
            formData.height = 0;
            formData.bottom.offset = 0;
        }
    }

    protected void refreshCreateNewProjectVisible(boolean visible) {
        createNewProject.setVisible(visible);
        FormData formData = (FormData) createNewProject.getLayoutData();
        if (visible) {
            formData.height = -1;
            formData.bottom.offset = -1 * TAB_VERTICAL_PADDING_LEVEL_1;
        } else {
            formData.height = 0;
            formData.bottom.offset = 0;
        }
    }

    protected void refreshImportDemoProjectVisible(boolean visible) {
        importDemoProject.setVisible(visible);
        FormData formData = (FormData) importDemoProject.getLayoutData();
        if (visible) {
            formData.height = -1;
            formData.bottom.offset = -1 * TAB_VERTICAL_PADDING_LEVEL_1;
        } else {
            formData.height = 0;
            formData.bottom.offset = 0;
        }
    }

    protected void refreshImportLocalProjectVisible(boolean visible) {
        importLocalProject.setVisible(visible);
        FormData formData = (FormData) importLocalProject.getLayoutData();
        if (visible) {
            formData.height = -1;
            formData.bottom.offset = -1 * TAB_VERTICAL_PADDING_LEVEL_1;
        } else {
            formData.height = 0;
            formData.bottom.offset = 0;
        }
    }

    protected void refreshBranchAreaVisible(boolean isRemote) {
        branchArea.setVisible(isRemote);
        FormData formData = (FormData) branchArea.getLayoutData();
        if (isRemote) {
            formData.height = -1;
        } else {
            formData.height = 0;
        }
        formData = (FormData) projectViewer.getControl().getLayoutData();
        if (isRemote) {
            formData.bottom.offset = -1 * TAB_VERTICAL_PADDING_LEVEL_2;
        } else {
            formData.bottom.offset = 0;
        }
        branchArea.setEnabled(isRemote);
        refreshProjectButton.setEnabled(isRemote);
    }

    protected void selectLast(String lastObjectSelected, Combo comboToSelect) {
        if (lastObjectSelected != null) {
            int userIndex = -1;
            String[] items = comboToSelect.getItems();
            for (int i = 0; userIndex == -1 && i < items.length; i++) {
                if (lastObjectSelected.equals(items[i])) {
                    userIndex = i;
                }
            }
            if (userIndex != -1) {
                comboToSelect.select(userIndex);
            } else {
                comboToSelect.select(0);
            }
        }
    }

    private void updateFinishButtonStatus() {
        List<ILoginConnectionService> loginConnectionServices = LoginConnectionManager.getRemoteConnectionService();
        boolean localConn = false;
        String errorMsg = null;
        if (getConnection() != null) {
            localConn = getConnection().getRepositoryId() == null
                    || getConnection().getRepositoryId().equals(RepositoryConstants.REPOSITORY_LOCAL_ID);
            if (loginConnectionServices.size() > 0 && getConnection().isComplete()) {
                for (ILoginConnectionService loginConncetion : loginConnectionServices) {
                    errorMsg = loginConncetion.checkConnectionValidation(getConnection().getName(), getConnection()
                            .getDescription(), getConnection().getUser(), getConnection().getPassword(), getConnection()
                            .getWorkSpace(), getConnection().getDynamicFields().get(RepositoryConstants.REPOSITORY_URL));
                    if (StringUtils.isEmpty(errorMsg)) {
                        break;
                    }
                }
            }
        }

        ConnectionBean connection = getConnection();
        if (connection == null) {
            // refreshProjectListAreaEnable(false);
            refreshProjectOperationAreaEnable(false);
            finishButton.setEnabled(false);
        } else if (errorMsg != null) {
            // refreshProjectListAreaEnable(false);
            refreshProjectOperationAreaEnable(false);
            finishButton.setEnabled(false);
            errorManager.setErrMessage(errorMsg);
        } else if ((projectViewer != null && projectViewer.getInput() == null) || !connection.isComplete()) {
            // refreshProjectListAreaEnable(false);
            refreshProjectOperationAreaEnable(false);
            finishButton.setEnabled(false);
            errorManager.setErrMessage(Messages.getString("LoginComposite.ConnectionIncompletedMessage")); //$NON-NLS-1$
        } else if (!isWorkSpaceSame()) {
            // connectionsViewer.getControl().setEnabled(true);
            // refreshProjectListAreaEnable(false);
            refreshProjectOperationAreaEnable(false);

            changeFinishButtonAction(FINISH_ACTION_RESTART);

        } else {
            // refreshProjectListAreaEnable(true);
            refreshProjectOperationAreaEnable(true);

            changeFinishButtonAction();

            finishButton.getShell().setDefaultButton(finishButton);
        }
        // refreshBranchAreaVisible(!localConn);
    }

    /**
     * @see org.talend.repository.ui.login.LoginComposite#setStatusArea()
     * @throws PersistenceException
     */
    public void checkErrors() throws PersistenceException {
        try {
            // errorManager.clearAllMessages();
            if (getConnection() != null) {
                final boolean localConn = getConnection().getRepositoryId() == null
                        || getConnection().getRepositoryId().equals(RepositoryConstants.REPOSITORY_LOCAL_ID);
                boolean visible = PluginChecker.isSVNProviderPluginLoaded() && !localConn;

                if (!isWorkSpaceSame()) {
                    errorManager.setErrMessage(Messages.getString("LoginProjectPage.DIFFERENT_WORKSPACE")); //$NON-NLS-1$
                } else if (projectViewer == null || projectViewer.getList().getItemCount() > 0) {
                    // for normal case, no need to show message anymore
                    // String productName = brandingService.getFullProductName();
                    // if (productName != null) {
                    //                        String[] split = productName.split(" "); //$NON-NLS-1$
                    // if (split != null && split.length > 3) {
                    // productName = brandingService.getShortProductName();
                    // }
                    // }
                    // errorManager.setInfoMessage(Messages.getString("LoginComposite.TisWorkspace_welcome", productName)); //$NON-NLS-1$
                    errorManager.clearAllMessages();
                } else {
                    if (LoginHelper.isRemoteConnection(getConnection())) {
                        errorManager.setErrMessage(Messages.getString("LoginProjectPage.project_need.remote.v1")); //$NON-NLS-1$
                    } else if (LoginHelper.isCloudConnection(getConnection())) {
                        errorManager.setErrMessage(Messages.getString("LoginProjectPage.project_need.remote.cloud")); //$NON-NLS-1$
                    } else {
                        errorManager.setErrMessage(Messages.getString("LoginComposite.PROJECT_NEED")); //$NON-NLS-1$
                    }
                }
            } else {
                errorManager.setErrMessage(Messages.getString("LoginComposite.connectionEmpty")); //$NON-NLS-1$
            }
        } finally {
            updateFinishButtonStatus();
        }
    }

    protected void validateUpdate() throws JSONException {
        if (errorManager.isHasAuthException()) {
            // can't connect to remote
            return;
        }
        final ConnectionBean currentBean = getConnection();
        String repositoryId = null;
        // at 1st time open the studio there are no bean at all,so need avoid NPE
        if (currentBean != null) {
            repositoryId = currentBean.getRepositoryId();
        }

        try {
            if (currentBean != null && isSVNProviderPluginLoadedRemote() && isWorkSpaceSame()) {
                if (afterUpdate) {
                    refreshProjectOperationAreaEnable(false);
                    errorManager.setErrMessage(Messages.getString("LoginProjectPage.archivaFinish")); //$NON-NLS-1$
                    changeFinishButtonAction(FINISH_ACTION_RESTART);
                } else {
                    OverTimePopupDialogTask<Boolean> overTimePopupDialogTask = new OverTimePopupDialogTask<Boolean>() {

                        @Override
                        public Boolean run() throws Throwable {
                            return LoginHelper.isStudioNeedUpdate(currentBean);
                        }
                    };
                    overTimePopupDialogTask.setNeedWaitingProgressJob(false);
                    boolean needUpdate = overTimePopupDialogTask.runTask();

                    if (needUpdate && isWorkSpaceSame()) {
                        refreshProjectOperationAreaEnable(false);
                        errorManager.setErrMessage(Messages.getString("LoginProjectPage.updateArchiva")); //$NON-NLS-1$
                        changeFinishButtonAction(FINISH_ACTION_UPDATE);
                    }
                }
            }
        } catch (PersistenceException e) {
            CommonExceptionHandler.process(e);
        } catch (SystemException e) {
            updateArchivaErrorButton();
        } catch (Throwable e) {
            CommonExceptionHandler.process(e);
        }
    }

    private void updateArchivaErrorButton() {
        refreshProjectOperationAreaEnable(false);
        errorManager.setWarnMessage(Messages.getString("LoginComposite.archivaFailed")); //$NON-NLS-1$
        changeFinishButtonAction(FINISH_ACTION_UPDATE_DETAILS);
    }

    protected void changeFinishButtonAction() {
        if (selectExistingProject.getSelection()) {
            finishButtonAction = FINISH_ACTION_OPEN_PROJECT;
        } else if (createSandBoxProject.getSelection()) {
            finishButtonAction = FINISH_ACTION_CREATE_SANDBOX;
        } else if (importDemoProject.getSelection()) {
            finishButtonAction = FINISH_ACTION_IMPORT_DEMO_PROJECT;
        } else if (importLocalProject.getSelection()) {
            finishButtonAction = FINISH_ACTION_IMPORT_LOCAL_PROJECT;
        } else if (createNewProject.getSelection()) {
            finishButtonAction = FINISH_ACTION_CREATE_NEW_PROJECT;
        }
        changeFinishButtonAction(finishButtonAction);
    }

    protected void changeFinishButtonAction(String newAction) {
        finishButtonAction = newAction;
        if (FINISH_ACTION_OPEN_PROJECT.equals(newAction)) {
            finishButton.setText(Messages.getString("LoginProjectPage.finish")); //$NON-NLS-1$
        } else if (FINISH_ACTION_UPDATE.equals(newAction)) {
            finishButton.setText(Messages.getString("LoginProjectPage.update")); //$NON-NLS-1$
        } else if (FINISH_ACTION_UPDATE_DETAILS.equals(newAction)) {
            finishButton.setText(Messages.getString("LoginProjectPage.details")); //$NON-NLS-1$
        } else if (FINISH_ACTION_CREATE_SANDBOX.equals(newAction)) {
            //            finishButton.setText(Messages.getString("LoginProjectPage.execute")); //$NON-NLS-1$
        } else if (FINISH_ACTION_IMPORT_DEMO_PROJECT.equals(newAction)) {
            //            finishButton.setText(Messages.getString("LoginProjectPage.execute")); //$NON-NLS-1$
        } else if (FINISH_ACTION_IMPORT_LOCAL_PROJECT.equals(newAction)) {
            //            finishButton.setText(Messages.getString("LoginProjectPage.execute")); //$NON-NLS-1$
        } else if (FINISH_ACTION_CREATE_NEW_PROJECT.equals(newAction)) {
            //            finishButton.setText(Messages.getString("LoginProjectPage.create")); //$NON-NLS-1$
        } else {
            finishButton.setText(Messages.getString("LoginProjectPage.restart")); //$NON-NLS-1$
            finishButtonAction = FINISH_ACTION_RESTART;
        }
        refreshUIFinishButtonEnable();
        updateButtonLayoutSize(finishButton);
        navigateArea.layout();
    }

    protected static void updateButtonLayoutSize(Button btn) {
        Object layoutData = btn.getLayoutData();
        if (layoutData instanceof FormData) {
            ((FormData) layoutData).width = LoginDialogV2.getNewButtonSize(btn).x;
        }
    }

    private void updateServerFields() {
        setRepositoryContextInContext();
        validateFields();

        if (isAuthenticationNeeded()) {
            populateRemoteLoginElements();
        } else {
            unpopulateRemoteLoginElements();
        }
    }

    private void clearAndDisableProjectList() {
        if (projectListArea != null) {
            projectListArea.setEnabled(false);
        }
        if (projectViewer != null) {
            projectViewer.setInput(null);
        }
    }

    /**
     * fill login values with default elements.
     */
    private void populateRemoteLoginElements() {
        clearAndDisableProjectList();
    }

    /**
     * clear login values.
     */
    private void unpopulateRemoteLoginElements() {
        fillUIProjectListWithBusyCursor();
    }

    private boolean validateFields() {
        boolean valid = true;

        ConnectionBean connection = getConnection();
        boolean isRemote = LoginHelper.isRemotesConnection(connection);
        boolean serverIsLocal = !isAuthenticationNeeded();
        if (valid && getConnection() == null) {
            valid = false;
        } else if (valid && !getConnection().isComplete()) {
            valid = false;
        } else if (valid && !serverIsLocal && connection.getUser().length() == 0) {
            valid = false;
        } else if (valid && !isRemote && !Pattern.matches(RepositoryConstants.MAIL_PATTERN, getUser().getLogin())) {
            valid = false;
        }
        if (valid && !serverIsLocal && connection.getPassword().length() == 0) {
            valid = false;
        }

        return valid;
    }

    private boolean validateProject() {
        boolean valid = true;

        if (projectViewer != null && projectViewer.getList().getItemCount() == 0) {
            valid = false;
            //            errorMsg = Messages.getString("LoginComposite.projectEmpty"); //$NON-NLS-1$
        }

        return valid;
    }

    /**
     * @see org.talend.repository.ui.wizards.newproject.NewProjectWizardPage#checkFieldsValue
     */
    protected void validateNewProjectName() {

        if (!createNewProject.getSelection()) {
            return;
        }

        LoginProjectPageErrorManager loginProjectPageErrorManager = getErrorManager();
        // loginProjectPageErrorManager.clearCreateNewProjectError();
        String projectNameErrorMessage = null;
        String strNewProjectName = newProjectName.getText();
        // Field Name
        if (strNewProjectName.length() == 0) {
            // for bug TDI-6993
            projectNameErrorMessage = Messages.getString("NewProjectWizardPage.nameEmpty"); //$NON-NLS-1$
        } else {
            // // for bug 11214
            //            if (!newProjectName.getText().endsWith(" ")) {//$NON-NLS-1$
            // technicalNameText.setText(Project.createTechnicalName(nameText.getText()));
            // }
            if (ProjectUtils.isNotValidProjectName(strNewProjectName)) {
                projectNameErrorMessage = Messages.getString("NewProjectWizardPage.illegalCharacter"); //$NON-NLS-1$
            } else {
                if (isProjectNameAlreadyUsed(strNewProjectName)) {
                    projectNameErrorMessage = Messages.getString("NewProjectWizardPage.projectNameAlredyExists"); //$NON-NLS-1$
                }
            }
        }
        if (projectNameErrorMessage != null) {
            loginProjectPageErrorManager.setCreateNewProjectError(projectNameErrorMessage);
            newProjectName.setToolTipText(projectNameErrorMessage);
            newProjectName.setBackground(LoginDialogV2.RED_COLOR);
            executeCreateNewProject.setEnabled(false);
            // refreshUIFinishButtonEnable(false);
        } else {
            String createNewProjectError = loginProjectPageErrorManager.getCreateNewProjectError();
            if (createNewProjectError != null && !createNewProjectError.isEmpty()) {
                loginProjectPageErrorManager.clearCreateNewProjectError();
            }
            newProjectName.setToolTipText(strNewProjectName);
            newProjectName.setBackground(null);
            executeCreateNewProject.setEnabled(true);
            // refreshUIFinishButtonEnable(true);
            executeCreateNewProject.getShell().setDefaultButton(executeCreateNewProject);

        }
    }

    protected boolean isProjectNameAlreadyUsed(String projectName) {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Project[] projects = (Project[]) projectViewer.getInput();
        if (projects == null) {
            try {
                projects = repositoryFactory.readProject();
            } catch (Exception e) {
                return true;
            }
        }
        for (Project project : projects) {
            if (Project.createTechnicalName(projectName).compareTo(project.getTechnicalLabel()) == 0) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAuthenticationNeeded() {
        if (getConnection() == null || !getConnection().isComplete()) {
            return false;
        }
        IRepositoryFactory repository = RepositoryFactoryProvider.getRepositoriyById(getConnection().getRepositoryId());
        if (repository == null) {
            return false;
        }
        return repository.isAuthenticationNeeded();
    }

    public ConnectionBean getConnection() {
        IStructuredSelection sel = (IStructuredSelection) connectionsViewer.getSelection();
        ConnectionBean firstElement = (ConnectionBean) sel.getFirstElement();
        // if (!PluginChecker.isSVNProviderPluginLoaded()) {
        // if (bean == null) {
        // bean = ConnectionBean.getDefaultConnectionBean();
        //                bean.setUser("test@talend.com"); //$NON-NLS-1$
        // bean.setWorkSpace(getRecentWorkSpace());
        // bean.setComplete(true);
        // }
        // return bean;
        // }
        return firstElement;
    }

    public boolean isWorkSpaceSame() {
        return LoginHelper.isWorkspaceSame(getConnection());
    }

    protected String getRecentWorkSpace() {
        String filePath = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
        return filePath;
    }

    protected void fillUIProjectListWithBusyCursor() {
        BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
                fillUIProjectList();
            }
        });
    }

    protected void refreshUIFinishButtonEnable() {
        boolean enabled = true;
        if (FINISH_ACTION_OPEN_PROJECT.equals(finishButtonAction)) {
            Object input = projectViewer.getInput();
            enabled = input != null && ((input instanceof Project[]) && ((Project[]) input).length > 0);
            if (getErrorManager().hasError()) {
                enabled = false;
            }
        } else if (FINISH_ACTION_UPDATE.equals(finishButtonAction)) {
            enabled = true;
        } else if (FINISH_ACTION_UPDATE_DETAILS.equals(finishButtonAction)) {
            enabled = true;
        } else if (FINISH_ACTION_RESTART.equals(finishButtonAction)) {
            enabled = true;
        } else {
            enabled = false;
        }
        refreshUIFinishButtonEnable(enabled);
    }

    protected void refreshUIFinishButtonEnable(boolean isEnable) {
        finishButton.setEnabled(isEnable);
    }

    /**
     * @see org.talend.repository.ui.login.LoginComposite#populateProjectList()
     */
    protected void fillUIProjectList() {

        Project[] projects = null;
        if (!errorManager.isHasAuthException()) {
            projects = loginHelper.getProjects(getConnection(), errorManager);
        }
        if (projects == null) {
            projects = new Project[0];
        }

        if (projectViewer != null) {
            projectViewer.setInput(projects);
        }

        // importDemoProjectAction.setExistingProjects(projects);
        if (projects.length > 0) {
            refreshProjectListAreaEnable();
            // Try to select the last recently used project
            selectLastUsedProject();
        } else {
            refreshProjectListAreaEnable(false);
            branchesViewer.setInput(null);
        }

    }

    /**
     * svn provider plugin loaded && a remote connection
     *
     * @return
     */
    protected boolean isSVNProviderPluginLoadedRemote() {
        boolean isRemote = false;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            StructuredSelection selection = (StructuredSelection) connectionsViewer.getSelection();
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof ConnectionBean) {
                isRemote = LoginHelper.isRemotesConnection((ConnectionBean) firstElement);
            }
        }
        return isRemote;
    }

    protected void selectLastUsedProject() {
        if (projectViewer != null) {
            Project[] projects = (Project[]) projectViewer.getInput();
            Project project = null;
            if (selectedProjectBeforeRefresh != null) {
                for (Project p : projects) {
                    if (selectedProjectBeforeRefresh.equals(p.getLabel())) {
                        project = p;
                        break;
                    }
                }
            }
            if (project == null) {
                project = loginHelper.getLastUsedProject(projects);
            }
            if (project == null && projects.length > 0) {
                project = projects[0];
            }
            if (project != null) {
                try {
                    selectProject(project);
                } catch (JSONException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private void selectProject(Project goodProject) throws JSONException {
        projectViewer.setSelection(new StructuredSelection(new Object[] { goodProject }), true);
        selectedProjectBeforeRefresh = goodProject.getLabel();
        loginFetchLicenseHelper.fetchLicenseIfNeeded(goodProject);
        fillUIBranches(goodProject, true);
        // if (PluginChecker.isTIS()) {
        // }
        setRepositoryContextInContext();
    }

    private void selectProject(String projectName) throws JSONException {
        Project[] projects = (Project[]) projectViewer.getInput();
        for (Project current : projects) {
            if (current.getLabel().equals(projectName)) {
                selectProject(current);
                return;
            }
        }
    }

    private String getStorage(Project project) throws JSONException {
        if (project == null) {
            return "";
        }
        String storage = "";
        if (project != null) {
            String url = project.getEmfProject().getUrl();
            if (url == null) {
                return "";
            }
            JSONObject jsonObj = new JSONObject(url);
            storage = jsonObj.getString("storage");
        }
        return storage;
    }

    private void fillUIBranches(final Project project, boolean lastUsedBranch) throws JSONException {
        final String storage = getStorage(project);
        if (LoginHelper.isRemotesConnection(getConnection())) {
            currentProjectSettings = project;
            final List<String> projectBranches = new ArrayList<String>();
            if ("svn".equals(storage)) {
                projectBranches.add("trunk"); //$NON-NLS-1$
                branchesViewer.setInput(projectBranches);
                branchesViewer.setSelection(new StructuredSelection(new Object[] { "trunk" })); //$NON-NLS-1$
            } else if ("git".equals(storage)) { //$NON-NLS-1$
                String master = "master"; //$NON-NLS-1$
                projectBranches.add(master);
                branchesViewer.setInput(projectBranches);
                if (projectBranches.size() != 0) {
                    branchesViewer.setSelection(new StructuredSelection(
                            new Object[] { projectBranches.contains(master) ? master : projectBranches.get(0) }));
                }

            }
            branchesViewer.getCombo().setEnabled(false);
            if (backgroundGUIUpdate == null/* || (backgroundGUIUpdate.getState() == Job.NONE) */) {
                backgroundGUIUpdate = new Job("List Branches") { //$NON-NLS-1$

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        projectBranches.clear();
                        try {
                            projectBranches.addAll(loginHelper.getProjectBranches(currentProjectSettings));
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            ExceptionHandler.process(e);
                        }
                        return org.eclipse.core.runtime.Status.OK_STATUS;
                    }

                };
                Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {

                    @Override
                    public void done(IJobChangeEvent event) {
                        if (event.getJob().equals(backgroundGUIUpdate)) {
                            if (branchesViewer != null && !branchesViewer.getCombo().isDisposed()) {
                                branchesViewer.getCombo().getDisplay().syncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (branchesViewer == null || branchesViewer.getControl() == null
                                                || branchesViewer.getControl().isDisposed()) {
                                            return;
                                        }
                                        branchesViewer.setInput(projectBranches);
                                        //branchesViewer.setSelection(new StructuredSelection(new Object[] { projectBranches.get(0) })); //$NON-NLS-1$
                                        if ("svn".equals(storage) && projectBranches.size() != 0) {
                                            branchesViewer.setSelection(new StructuredSelection(new Object[] { projectBranches
                                                    .contains("trunk") ? "trunk" : projectBranches.get(0) }));
                                        } else if ("git".equals(storage) && projectBranches.size() != 0) {
                                            branchesViewer.setSelection(new StructuredSelection(new Object[] { projectBranches
                                                    .contains("master") ? "master" : projectBranches.get(0) }));
                                        }
                                        // svnBranchCombo.getCombo().setFont(originalFont);
                                        branchesViewer.getCombo().setEnabled(projectViewer.getControl().isEnabled());
                                    }
                                });
                            }
                        }
                    }
                });
                branchesViewer.getCombo().addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        backgroundGUIUpdate = null;
                    }
                });
            }
            backgroundGUIUpdate.schedule();
        }
    }

    private List<String> getBranches(Project project) {
        IGITProviderService gitProviderService = (IGITProviderService) GlobalServiceRegister.getDefault().getService(
                IGITProviderService.class);
        String[] branchArr = gitProviderService.getBranchList(project);
        List<String> branches = Arrays.asList(branchArr);

        return branches;
    }

    /**
     * Store the current selected project&branch etc into context
     */
    public void setRepositoryContextInContext() {
        LoginHelper.setRepositoryContextInContext(getConnection(), getUser(), getProject(), getBranch());
    }

    public User getUser() {
        return LoginHelper.getUser(loginHelper.getCurrentSelectedConnBean());
    }

    public Project getProject() {
        Project project = null;
        if (projectViewer != null && !projectViewer.getSelection().isEmpty()) {
            IStructuredSelection sel = (IStructuredSelection) projectViewer.getSelection();
            project = (Project) sel.getFirstElement();
        }
        return project;
    }

    public String getBranch() {
        Project project = getProject();
        boolean isRemoteConnection = LoginHelper.isRemotesConnection(getConnection());
        if (branchesViewer != null && isRemoteConnection && !branchesViewer.getSelection().isEmpty() && project != null) {
            IStructuredSelection ss = (IStructuredSelection) branchesViewer.getSelection();
            String branch = (String) ss.getFirstElement();
            /*
             * verify branches
             */
            // List<String> branchList = getProjectBranches(project);
            // if (branchList != null && branchList.contains(branch)) {
            return branch;
            // }
        } else if (!isRemoteConnection && project != null) {
            List<ProjectReference> referenceProjects = project.getProjectReferenceList();
            String currentBranch = null;
            if (referenceProjects != null && !referenceProjects.isEmpty()) {
                boolean allBranchesAreSame = true;
                for (ProjectReference referenceProject : referenceProjects) {
                    String branchFromReference = referenceProject.getBranch();
                    if (currentBranch == null) {
                        if (branchFromReference == null) {
                            branchFromReference = ""; //$NON-NLS-1$
                        }
                        currentBranch = branchFromReference;
                        continue;
                    }
                    if (!currentBranch.equals(branchFromReference)) {
                        allBranchesAreSame = false;
                        break;
                    }
                }
                if (!allBranchesAreSame) {
                    currentBranch = ""; //$NON-NLS-1$
                }
            } else {
                currentBranch = ""; //$NON-NLS-1$
            }
            return currentBranch;
        }
        return null;
    }

    public void importProjects() {
        ImportProjectAsAction.getInstance().run();

        fillUIProjectListWithBusyCursor();

        String newProject = ImportProjectAsAction.getInstance().getProjectName();
        if (newProject != null) {
            resetProjectOperationSelectionWithBusyCursor();
            try {
                selectProject(newProject);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
            }
        } else if (ImportProjectAsAction.getInstance().isImportedSeveralProjects()) {
            resetProjectOperationSelectionWithBusyCursor();
        }
        validateProject();
    }

    public void importDemoProject() {
        // dialog.setMessage("Importing demo project ...");
        ImportDemoProjectAction.getInstance().setShell(getShell());
        ImportDemoProjectAction.getInstance().run();
        fillUIProjectListWithBusyCursor();
        String newProject = ImportDemoProjectAction.getInstance().getProjectName();
        if (newProject != null) {
            resetProjectOperationSelectionWithBusyCursor();
            try {
                selectProject(newProject);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
            }
        }
        validateProject();
    }

    private void createSandboxProject() {
        if (getConnection() != null) {
            setRepositoryContextInContext(); // must set the current connection, if existed
        }
        CreateSandboxProjectDialog sandboxDialog = new CreateSandboxProjectDialog(getShell(), getConnection());

        if (sandboxDialog.open() == Window.OK) {
            ConnectionUserPerReader instance = ConnectionUserPerReader.getInstance();
            loginHelper.setStoredConnections(instance.forceReadConnections());
            loginHelper.setLastConnection(sandboxDialog.getConnectionBean().getName());

            loginHelper.saveConnections();

            fillUIContentsWithBusyCursor();
        }
        try {
            checkErrors();
        } catch (PersistenceException e1) {
            CommonExceptionHandler.process(e1);
        }
        if (getConnection() != null) { // reset the context, because there are some changes in create sandbox dialog
            setRepositoryContextInContext();
        }
    }

    protected void createNewProjectWithBusyCursor() {
        BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
                createNewProject();
            }
        });
    }

    protected void createNewProject() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Context ctx = CorePlugin.getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        try {
            Project projectInfor = ProjectHelper.createProject(newProjectName.getText().trim().replace(' ', '_'), "", //$NON-NLS-1$
                    ECodeLanguage.JAVA.getName(), repositoryContext.getUser());
            Project project = repositoryFactory.createProject(projectInfor);
            resetProjectOperationSelectionWithBusyCursor(false);
            fillUIProjectListWithBusyCursor();
            selectProject(project.getLabel());
            checkErrors();
            newProjectName.setText(""); //$NON-NLS-1$
        } catch (PersistenceException e) {
            MessageDialog.openError(getShell(), Messages.getString("NewProjectWizard.failureTitle"), Messages //$NON-NLS-1$
                    .getString("NewProjectWizard.failureText")); //$NON-NLS-1$
            MessageBoxExceptionHandler.process(e);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            ExceptionHandler.process(e);
        }
    }

    protected LoginProjectPageErrorManager getErrorManager() {
        return (LoginProjectPageErrorManager) errorManager;
    }

    @Override
    protected ErrorManager createErrorManager() {
        return new LoginProjectPageErrorManager();
    }

    protected class ConnectionLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            ConnectionBean prj = (ConnectionBean) element;
            String connectionName = prj.getName();
            if (LoginHelper.isRemoteConnection(prj)) {
                String remoteLabel = null;
                if (brandingService.isPoweredbyTalend()) {
                    remoteLabel = Messages.getString("LoginProjectPage.remote.talend"); //$NON-NLS-1$
                } else {
                    remoteLabel = Messages.getString("LoginProjectPage.remote"); //$NON-NLS-1$
                }
                connectionName = connectionName + " (" + remoteLabel; //$NON-NLS-1$
            } else if (LoginHelper.isCloudUSConnection(prj)) {
                connectionName = connectionName + " (" + Messages.getString("LoginProjectPage.cloud.aws_us"); //$NON-NLS-1$//$NON-NLS-2$
            } else if (LoginHelper.isCloudEUConnection(prj)) {
                connectionName = connectionName + " (" + Messages.getString("LoginProjectPage.cloud.aws_eu"); //$NON-NLS-1$//$NON-NLS-2$
            } else if (LoginHelper.isCloudAPACConnection(prj)) {
                connectionName = connectionName + " (" + Messages.getString("LoginProjectPage.cloud.aws_apac"); //$NON-NLS-1$//$NON-NLS-2$
            } else if (LoginHelper.isCloudUSWestConnection(prj)) {
                connectionName = connectionName + " (" + Messages.getString("LoginProjectPage.cloud.usa_west"); //$NON-NLS-1$//$NON-NLS-2$
            } else if (LoginHelper.isCloudCustomConnection(prj)) {
                connectionName = connectionName + " (" + Messages.getString("LoginProjectPage.cloud.cloud_custom"); //$NON-NLS-1$//$NON-NLS-2$
            } else {
                connectionName = connectionName + " (" + Messages.getString("LoginProjectPage.local"); //$NON-NLS-1$//$NON-NLS-2$
            }
            if (!prj.isComplete()) {
                connectionName = connectionName + ", " + Messages.getString("connections.form.field.imcomplete"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            connectionName = connectionName + ")"; //$NON-NLS-1$
            return connectionName;
        }
    }

    protected class ProjectLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            Project prj = (Project) element;
            String projectDisplayLabel = prj.getLabel();
            if (ProjectManager.enableSpecialTechnicalProjectName()) {
                projectDisplayLabel = ProjectManager.getProjectDisplayLabel(prj.getEmfProject());
            }
            String toReturn = projectDisplayLabel + " - " + prj.getLanguage().getName(); //$NON-NLS-1$
            if (!prj.isLocal() && !isAuthenticationNeeded()) {
                toReturn += " (remote project in offline mode)"; //$NON-NLS-1$
            }
            return toReturn;
        }

    }

    /**
     * created by sgandon on 18 nov. 2014 Detailled comment
     *
     */
    private static class ArchivaErrorDialog extends MessageDialog {

        public ArchivaErrorDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
                int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
            super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
        }

        @Override
        protected Composite createCustomArea(Composite parent) {
            Composite helpComposite = new Composite(parent, SWT.NONE);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumHeight = 45;
            helpComposite.setLayoutData(gridData);
            GC gc = new GC(helpComposite);
            String linkLabel = Messages.getString("LoginProjectPage.ArchivaErrorDialog.howToInstallAJar"); //$NON-NLS-1$
            Point linkSize = gc.stringExtent(linkLabel);
            Font font = new Font(null, LoginDialogV2.FONT_ARIAL, 9, SWT.NORMAL);
            Hyperlink link = new Hyperlink(helpComposite, SWT.NONE);
            link.setText(linkLabel);
            link.setSize(linkSize.x + 15, 30);
            link.setBackground(helpComposite.getBackground());
            link.setUnderlined(true);
            link.setFont(font);

            GridData gridData1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData1.widthHint = this.getMinimumMessageWidth();
            link.setLayoutData(gridData1);
            gc.dispose();
            link.addHyperlinkListener(new HyperlinkAdapter() {

                @Override
                public void linkActivated(HyperlinkEvent e) {
                    String url = "https://help.talend.com/pages/viewpage.action?pageId=14230347";
                    TalendBrowserLaunchHelper.openURL(url);
                }
            });
            return helpComposite;
        }
    }

    protected class LoginProjectPageErrorManager extends ErrorManager {

        protected String createNewProjectErrorMsg;

        @Override
        public void clearAllMessages() {
            createNewProjectErrorMsg = null;
            super.clearAllMessages();
        }

        public void setCreateNewProjectError(String errMsg) {
            createNewProjectErrorMsg = errMsg;
            loginDialog.setErrorMessage(errMsg, null);
        }

        public String getCreateNewProjectError() {
            return createNewProjectErrorMsg;
        }

        public void clearCreateNewProjectError() {
            createNewProjectErrorMsg = null;
            if (!showErrorMessage()) {
                loginDialog.clearErrorMessage();
            }
        }

        @Override
        public boolean showErrorMessage() {
            boolean hasMsg = super.showErrorMessage();
            if (hasMsg) {
                return true;
            }
            if (createNewProjectErrorMsg != null && !createNewProjectErrorMsg.isEmpty()) {
                loginDialog.setErrorMessage(createNewProjectErrorMsg, null);
                return true;
            }
            return false;
        }

        @Override
        public boolean hasError() {
            boolean hasError = false;
            if (createNewProjectErrorMsg != null && !createNewProjectErrorMsg.isEmpty()) {
                hasError = true;
            }
            return hasError || super.hasError();
        }
    }
}
