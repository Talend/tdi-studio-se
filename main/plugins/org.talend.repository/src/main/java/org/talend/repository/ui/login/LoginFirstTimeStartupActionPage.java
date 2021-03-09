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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.core.service.IUpdateService;
import org.talend.core.utils.ProjectUtils;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.actions.importproject.ImportDemoProjectAction;
import org.talend.repository.ui.actions.importproject.ImportProjectAsAction;
import org.talend.repository.ui.login.connections.ConnectionsDialog;
import org.talend.utils.json.JSONException;

/**
 * created by cmeng on May 22, 2015 Detailled comment
 *
 */
public class LoginFirstTimeStartupActionPage extends AbstractLoginActionPage {
    
    protected static Logger log = Logger.getLogger(LoginFirstTimeStartupActionPage.class);

    protected static final String FINISH_BUTTON_ACTION_CREATE_NEW_PROJECT = "CREATE_NEW_PROJECT"; //$NON-NLS-1$

    protected static final String FINISH_BUTTON_ACTION_IMPORT_DEMO_PROJECT = "IMPORT_DEMO_PROJECT"; //$NON-NLS-1$

    protected static final String FINISH_BUTTON_ACTION_IMPORT_EXISTING_PROJECT = "IMPORT_EXISTING_PROJECT"; //$NON-NLS-1$

    protected static final String NEW_PROJECT_NAME = "Local_Project"; //$NON-NLS-1$

    protected Label title;

    protected Button createNewProject;

    protected Text newProjectName;

    protected Button importDemoProject;

    protected Button importExistingProject;

    protected Button manageConnections;

    protected ConnectionBean defaultConnectionBean;

    protected String finishButtonAction;

    protected LoginHelper loginHelper;

    public LoginFirstTimeStartupActionPage(Composite parent, LoginDialogV2 dialog, int style) {
        super(parent, dialog, style);
    }

    @Override
    public void preCreateControl() {
        // nothing need to do
    }

    @Override
    public void init() throws Throwable {
        super.init();
        loginHelper = LoginHelper.getInstance();
        List<ConnectionBean> storedConnections = LoginHelper.getInstance().getStoredConnections();
        if (storedConnections == null) {
            storedConnections = new ArrayList<ConnectionBean>();
        } else {
            storedConnections = loginHelper.filterUsableConnections(storedConnections);
        }
        if (storedConnections.size() == 0) {
            defaultConnectionBean = LoginHelper.createDefaultLocalConnection();
            storedConnections.add(defaultConnectionBean);
            LoginHelper.getInstance().setStoredConnections(storedConnections);
            LoginHelper.getInstance().saveConnections(storedConnections);
        } else {
            defaultConnectionBean = storedConnections.get(0);
        }
        setRepositoryContextInContext();
    }

    protected void setRepositoryContextInContext() {
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider.getRepositoriyById(defaultConnectionBean
                .getRepositoryId()));
        LoginHelper.setRepositoryContextInContext(defaultConnectionBean, LoginHelper.getUser(defaultConnectionBean), null, null);
    }

    @Override
    public void afterCreateControl() {
        alwaysAsk.setSelection(LoginHelper.isAlwaysAskAtStartup());
        previousButton.setVisible(false);
        createNewProject.setSelection(true);
        finishButtonAction = FINISH_BUTTON_ACTION_CREATE_NEW_PROJECT;
        newProjectName.setText(NEW_PROJECT_NAME);
        newProjectName.setToolTipText(NEW_PROJECT_NAME);

        selectNewProjectName();
        validateNewProjectName();
    }

    @Override
    public void instantiateControl(Composite container) {

        /**
         * 1. Create all the control first
         */
        title = new Label(container, SWT.NONE);
        title.setFont(LoginDialogV2.fixedFont);
        title.setText(Messages.getString("LoginFirstTimeStartupActionPage.title")); //$NON-NLS-1$
        createNewProject = new Button(container, SWT.RADIO);
        createNewProject.setFont(LoginDialogV2.fixedFont);
        createNewProject.setBackground(backgroundRadioColor);
        createNewProject.setText(Messages.getString("LoginFirstTimeStartupActionPage.createNewProject")); //$NON-NLS-1$
        newProjectName = new Text(container, SWT.BORDER);
        newProjectName.setFont(LoginDialogV2.fixedFont);
        importDemoProject = new Button(container, SWT.RADIO);
        importDemoProject.setFont(LoginDialogV2.fixedFont);
        importDemoProject.setBackground(backgroundRadioColor);
        importDemoProject.setText(Messages.getString("LoginFirstTimeStartupActionPage.importDemoProject")); //$NON-NLS-1$
        importExistingProject = new Button(container, SWT.RADIO);
        importExistingProject.setFont(LoginDialogV2.fixedFont);
        importExistingProject.setBackground(backgroundRadioColor);
        importExistingProject.setText(Messages.getString("LoginFirstTimeStartupActionPage.importExsitingProject")); //$NON-NLS-1$
        manageConnections = new Button(container, SWT.NONE);
        manageConnections.setFont(LoginDialogV2.fixedFont);
        manageConnections.setBackground(backgroundBtnColor);
        manageConnections.setText(Messages.getString("LoginFirstTimeStartupActionPage.manageConnections")); //$NON-NLS-1$

        super.instantiateControl(container);
    }

    @Override
    protected void layoutControl() {
        super.layoutControl();

        FormData formData = null;

        formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.top = new FormAttachment(0, 0);
        title.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(title, TAB_VERTICAL_PADDING_LEVEL_1, SWT.BOTTOM);
        formData.left = new FormAttachment(title, TAB_HORIZONTAL_PADDING_LEVEL_1, SWT.LEFT);
        createNewProject.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(createNewProject, TAB_HORIZONTAL_PADDING_LEVEL_1, SWT.RIGHT);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(createNewProject, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(createNewProject, 0, SWT.CENTER);
        newProjectName.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(createNewProject, 0, SWT.LEFT);
        formData.top = new FormAttachment(createNewProject, TAB_VERTICAL_PADDING_LEVEL_1, SWT.BOTTOM);
        importDemoProject.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(importDemoProject, 0, SWT.LEFT);
        formData.top = new FormAttachment(importDemoProject, TAB_VERTICAL_PADDING_LEVEL_1, SWT.BOTTOM);
        importExistingProject.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(title, 0, SWT.LEFT);
        formData.width = LoginDialogV2.getNewButtonSize(manageConnections).x;
        formData.bottom = new FormAttachment(navigateArea, -1 * TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);
        manageConnections.setLayoutData(formData);
        manageConnections.setBackground(backgroundBtnColor);
    }

    @Override
    public void refreshUIData() {
        // nothing need to do
    }

    @Override
    public void check() {
        try {
            if (SharedStudioUtils.isSharedStudioMode()) {
                validatePatchForSharedMode();
            }
        } catch (JSONException e) {
            CommonExceptionHandler.process(e);
            log.error(e);
        }
    }
    
    protected void validatePatchForSharedMode() throws JSONException {
        String missingPatchVersion = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IUpdateService.class)) {
            IUpdateService updateService = GlobalServiceRegister.getDefault().getService(IUpdateService.class);
            missingPatchVersion = updateService.getSharedStudioMissingPatchVersion();
        }
        if (missingPatchVersion != null) {
            errorManager.setWarnMessage(Messages.getString("LoginProjectPage.sharedModeMissingPatchFile", missingPatchVersion));
        }
    }


    @Override
    protected void addListeners() {
        super.addListeners();
        finishButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (FINISH_BUTTON_ACTION_CREATE_NEW_PROJECT.equals(finishButtonAction)) {
                    createNewProject();
                } else if (FINISH_BUTTON_ACTION_IMPORT_DEMO_PROJECT.equals(finishButtonAction)) {
                    importDemoProject();
                } else if (FINISH_BUTTON_ACTION_IMPORT_EXISTING_PROJECT.equals(finishButtonAction)) {
                    importExistingProject();
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
                    selectNewProjectName();
                    validateNewProjectName();
                    finishButtonAction = FINISH_BUTTON_ACTION_CREATE_NEW_PROJECT;
                } else {
                    createNewProject.setText(Messages.getString("LoginFirstTimeStartupActionPage.createNewProject")); //$NON-NLS-1$
                    getErrorManager().clearCreateNewProjectError();
                    newProjectName.setVisible(false);
                    newProjectName.setBackground(null);
                    finishButton.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        newProjectName.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validateNewProjectName();
            }
        });

        importDemoProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                finishButtonAction = FINISH_BUTTON_ACTION_IMPORT_DEMO_PROJECT;
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        importExistingProject.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                finishButtonAction = FINISH_BUTTON_ACTION_IMPORT_EXISTING_PROJECT;
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });

        manageConnections.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ConnectionsDialog connectionsDialog = new ConnectionsDialog(getShell());
                int open = connectionsDialog.open();
                if (open == Window.OK) {
                    LoginFirstTimeStartupActionPageErrorManager iErrorManager = getErrorManager();
                    iErrorManager.clearManageConnectionsError();
                    iErrorManager.clearManageConnectionsWarn();
                    refreshOperationAreaEnable(true);
                    List<ConnectionBean> storedConnections = connectionsDialog.getConnections();
                    if (storedConnections == null || storedConnections.isEmpty()) {
                        defaultConnectionBean = LoginHelper.createDefaultLocalConnection();
                        storedConnections = new ArrayList<ConnectionBean>();
                        storedConnections.add(defaultConnectionBean);
                        LoginHelper.getInstance().setStoredConnections(storedConnections);
                        LoginHelper.getInstance().saveConnections(storedConnections);
                        setRepositoryContextInContext();
                    } else if (storedConnections.size() == 1) {
                        defaultConnectionBean = storedConnections.get(0);
                        if (defaultConnectionBean.isComplete()) {
                            LoginHelper.getInstance().setStoredConnections(storedConnections);
                            LoginHelper.getInstance().saveConnections(storedConnections);

                            boolean shouldGotoNextPage = false;
                            if (LoginHelper.isRemotesConnection(defaultConnectionBean)) {
                                shouldGotoNextPage = true;
                            } else if (!LoginHelper.isWorkspaceSame(defaultConnectionBean)) {
                                shouldGotoNextPage = true;
                            } else {
                                shouldGotoNextPage = false;
                            }

                            if (shouldGotoNextPage) {
                                try {
                                    gotoNextPage();
                                    return;
                                } catch (Throwable e1) {
                                    CommonExceptionHandler.process(e1);
                                }
                            } else {
                                setRepositoryContextInContext();
                            }
                        } else {
                            iErrorManager.setManageConnectionsError(Messages
                                    .getString("LoginFirstTimeStartupActionPage.manageConnection.incomplete")); //$NON-NLS-1$
                            refreshOperationAreaEnable(false);
                        }
                    } else {
                        try {
                            LoginHelper.getInstance().setStoredConnections(storedConnections);
                            LoginHelper.getInstance().saveConnections(storedConnections);
                            gotoNextPage();
                            return;
                        } catch (Throwable e1) {
                            CommonExceptionHandler.process(e1);
                        }
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });
    }

    @Override
    public Object getCheckedErrors() {
        return null;
    }

    @Override
    public AbstractActionPage getNextPage() {
        return new LoginProjectPage(this.getParent(), loginDialog, SWT.NONE);
    }

    protected void selectNewProjectName() {
        createNewProject.setText(Messages.getString("LoginFirstTimeStartupActionPage.createNewProject.selected")); //$NON-NLS-1$
        createNewProject.pack();
        FormData formData = (FormData) newProjectName.getLayoutData();
        formData.left = new FormAttachment(createNewProject, createNewProject.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                + TAB_HORIZONTAL_PADDING_LEVEL_1, SWT.LEFT);
        newProjectName.setVisible(true);
        // newProjectName.setText(""); //$NON-NLS-1$
        newProjectName.forceFocus();
        newProjectName.selectAll();
    }

    protected void validateNewProjectName() {

        if (!createNewProject.getSelection()) {
            return;
        }

        LoginFirstTimeStartupActionPageErrorManager iErrorManager = getErrorManager();
        iErrorManager.clearCreateNewProjectError();
        String projectNameErrorMessage = null;
        String strProjectName = newProjectName.getText();
        if (strProjectName.length() == 0) {
            projectNameErrorMessage = Messages.getString("NewProjectWizardPage.nameEmpty"); //$NON-NLS-1$
        } else {
            if (ProjectUtils.isNotValidProjectName(strProjectName)) {
                projectNameErrorMessage = Messages.getString("NewProjectWizardPage.illegalCharacter"); //$NON-NLS-1$
            } else {
                if (isProjectNameAlreadyUsed(strProjectName)) {
                    projectNameErrorMessage = Messages.getString("NewProjectWizardPage.projectNameAlredyExists"); //$NON-NLS-1$
                }
            }
        }
        if (projectNameErrorMessage != null) {
            iErrorManager.setCreateNewProjectError(projectNameErrorMessage);
            newProjectName.setBackground(LoginDialogV2.RED_COLOR);
            newProjectName.setToolTipText(projectNameErrorMessage);
            finishButton.setEnabled(false);
        } else {
            newProjectName.setToolTipText(newProjectName.getText());
            newProjectName.setBackground(null);
            finishButton.setEnabled(true);
            newProjectName.getShell().setDefaultButton(finishButton);
        }
    }

    protected void refreshOperationAreaEnable(boolean isEnable) {
        createNewProject.setEnabled(isEnable);
        newProjectName.setEnabled(isEnable);
        importDemoProject.setEnabled(isEnable);
        importExistingProject.setEnabled(isEnable);
        refreshFinishButtonEnable(isEnable);
    }

    protected void refreshFinishButtonEnable(boolean isEnable) {
        if (!isEnable) {
            finishButton.setEnabled(false);
        } else {
            finishButton.setEnabled(!errorManager.hasError());
        }
    }

    protected boolean isProjectNameAlreadyUsed(String projectName) {
        Project[] projects = null;
        try {
            projects = ProxyRepositoryFactory.getInstance().readProject();
        } catch (Exception e) {
            return true;
        }
        for (Project project : projects) {
            if (Project.createTechnicalName(projectName).compareTo(project.getTechnicalLabel()) == 0) {
                return true;
            }
        }
        return false;
    }

    protected void createNewProject() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Context ctx = CorePlugin.getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        Project project = null;
        try {
            Project projectInfor = ProjectHelper.createProject(newProjectName.getText().trim().replace(' ', '_'), "", //$NON-NLS-1$
                    ECodeLanguage.JAVA.getName(), repositoryContext.getUser());
            project = repositoryFactory.createProject(projectInfor);
        } catch (PersistenceException e) {
            MessageDialog.openError(getShell(), Messages.getString("NewProjectWizard.failureTitle"), Messages //$NON-NLS-1$
                    .getString("NewProjectWizard.failureText")); //$NON-NLS-1$
            MessageBoxExceptionHandler.process(e);
        }
        if (project == null) {
            return;
        }
        openProject(project);
    }

    protected void importDemoProject() {
        ImportDemoProjectAction.getInstance().setShell(getShell());
        ImportDemoProjectAction.getInstance().run();
        String importedDemoProjectName = ImportDemoProjectAction.getInstance().getProjectName();
        if (importedDemoProjectName == null || importedDemoProjectName.isEmpty()) {
            return;
        }
        Project selectedProject = loginHelper.getProjectByName(loginHelper.getProjects(defaultConnectionBean),
                importedDemoProjectName);
        openProject(selectedProject);
    }

    protected void importExistingProject() {
        ImportProjectAsAction.getInstance().run();
        if (ImportProjectAsAction.getInstance().isImportedSeveralProjects()) {
            try {
                gotoNextPage();
            } catch (Throwable e) {
                CommonExceptionHandler.process(e);
            }
            return;
        }
        String importedExistingProjectName = ImportProjectAsAction.getInstance().getProjectName();
        if (importedExistingProjectName == null || importedExistingProjectName.isEmpty()) {
            return;
        }
        Project selectedProject = loginHelper.getProjectByName(loginHelper.getProjects(defaultConnectionBean),
                importedExistingProjectName);
        openProject(selectedProject);
    }

    protected void openProject(Project project) {
        if (project == null) {
            return;
        }
        LoginHelper.setRepositoryContextInContext(defaultConnectionBean, LoginHelper.getUser(defaultConnectionBean), project,
                SVNConstant.NAME_TRUNK);
        if (loginHelper.logIn(defaultConnectionBean, project)) {
            loginHelper.saveLastConnectionBean(defaultConnectionBean);
            loginHelper.getPrefManipulator().setLastProject(project.getLabel());
            LoginHelper.setAlwaysAskAtStartup(alwaysAsk.getSelection());
            loginDialog.okPressed();
        }
    }

    protected LoginFirstTimeStartupActionPageErrorManager getErrorManager() {
        return (LoginFirstTimeStartupActionPageErrorManager) errorManager;
    }

    @Override
    protected ErrorManager createErrorManager() {
        return new LoginFirstTimeStartupActionPageErrorManager();
    }

    protected class LoginFirstTimeStartupActionPageErrorManager extends ErrorManager {

        protected String createNewProjectErrorMsg;

        protected String manageConnectionsError;

        protected String manageConnectionsWarn;

        @Override
        public void clearAllMessages() {
            createNewProjectErrorMsg = null;
            manageConnectionsError = null;
            manageConnectionsWarn = null;
            super.clearAllMessages();
        }

        public void setCreateNewProjectError(String errMsg) {
            createNewProjectErrorMsg = errMsg;
            loginDialog.setErrorMessage(errMsg, null);
        }

        public void clearCreateNewProjectError() {
            createNewProjectErrorMsg = null;
            loginDialog.clearErrorMessage();
            showErrorMessage();
        }

        public void setManageConnectionsWarn(String manageConnectionsWarn) {
            this.manageConnectionsWarn = manageConnectionsWarn;
            loginDialog.setWarnMessage(manageConnectionsWarn, null);
        }

        public void clearManageConnectionsWarn() {
            manageConnectionsWarn = null;
            loginDialog.clearErrorMessage();
            showErrorMessage();
        }

        public void setManageConnectionsError(String manageConnectionsError) {
            this.manageConnectionsError = manageConnectionsError;
            loginDialog.setErrorMessage(manageConnectionsError, null);
        }

        public void clearManageConnectionsError() {
            manageConnectionsError = null;
            loginDialog.clearErrorMessage();
            showErrorMessage();
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

            if (manageConnectionsError != null && !manageConnectionsError.isEmpty()) {
                loginDialog.setErrorMessage(manageConnectionsError, null);
                return true;
            }

            if (manageConnectionsWarn != null && !manageConnectionsWarn.isEmpty()) {
                loginDialog.setWarnMessage(manageConnectionsWarn, null);
                return true;
            }

            return false;
        }

        @Override
        public boolean hasError() {
            boolean hasError = false;

            if (manageConnectionsError != null && !manageConnectionsError.isEmpty()) {
                hasError = true;
            }

            if (createNewProjectErrorMsg != null && !createNewProjectErrorMsg.isEmpty()) {
                hasError = true;
            }

            return hasError || super.hasError();
        }

    }
}
