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
package org.talend.repository;

import java.beans.PropertyChangeEvent;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.InformException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.OperationCancelException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.PasswordHelper;
import org.talend.commons.utils.system.EclipseCommandLine;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IRepositoryContextService;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.action.DisableLanguageActions;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.FileConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.migration.IMigrationToolService;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.impl.PropertiesFactoryImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryElementDelta;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.model.utils.CloneConnectionUtils;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.core.repository.CoreRepositoryPlugin;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.model.repositoryObject.SalesforceModuleRepositoryObject;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.core.repository.utils.RepositoryPathProvider;
import org.talend.core.services.IGITProviderService;
import org.talend.core.services.ISVNProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.services.IRulesProviderService;
import org.talend.cwm.helper.ModelElementHelper;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.metadata.managment.ui.model.ProjectNodeHelper;
import org.talend.metadata.managment.ui.utils.DBConnectionContextUtils;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.navigator.RepoViewCommonNavigator;
import org.talend.repository.plugin.integration.BindingActions;
import org.talend.repository.plugin.integration.SwitchProjectAction;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.actions.routines.CreateRoutineAction;
import org.talend.repository.ui.actions.sqlpattern.CreateSqlpatternAction;
import org.talend.repository.ui.actions.sqlpattern.EditSqlpatternAction;
import org.talend.repository.ui.dialog.ContextRepositoryReviewDialog;
import org.talend.repository.ui.dialog.ProjectSettingDialog;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;
import org.talend.repository.ui.login.LoginDialogV2;
import org.talend.repository.ui.login.LoginHelper;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;
import org.talend.repository.ui.login.connections.network.NetworkErrorRetryDialog;
import org.talend.repository.ui.utils.ColumnNameValidator;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * DOC qian class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 *
 */

public class RepositoryService implements IRepositoryService, IRepositoryContextService {

    private static Logger log = Logger.getLogger(RepositoryService.class);

    private ISVNProviderService svnProviderService;
    private IGITProviderService gitProviderService;
    private boolean isInitedProviderService = false;
    private final Semaphore askUserForNetworkIssueSemaphore = new Semaphore(1, true);

    private volatile boolean askUserForNetworkIssueRetryCache = false;

    private volatile boolean donnotRetryByCancel = false;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#getComponentsFactory()
     */
    @Override
    public IComponentsFactory getComponentsFactory() {
        return ComponentsFactoryProvider.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#getPathFileName(java.lang.String, java.lang.String)
     */
    @Override
    public IPath getPathFileName(String folderName, String fileName) {
        return RepositoryPathProvider.getPathFileName(folderName, fileName);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#getProxyRepositoryFactory()
     */
    @Override
    public IProxyRepositoryFactory getProxyRepositoryFactory() {
        return ProxyRepositoryFactory.getInstance();
    }

    @Override
    public IPath getRepositoryPath(IRepositoryNode node) {
        return RepositoryNodeUtilities.getPath(node);
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.repository.model.IRepositoryService#registerRepositoryChangedListener(org.talend.repository.
     * IRepositoryChangedListener)
     */
    @Override
    public void registerRepositoryChangedListener(IRepositoryChangedListener listener) {
        CoreRepositoryPlugin.getDefault().registerRepositoryChangedListener(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.model.IRepositoryService#registerRepositoryChangedListenerAsFirst(org.talend.repository
     * .IRepositoryChangedListener)
     */
    @Override
    public void registerRepositoryChangedListenerAsFirst(IRepositoryChangedListener listener) {
        CoreRepositoryPlugin.getDefault().registerRepositoryChangedListenerAsFirst(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.repository.model.IRepositoryService#removeRepositoryChangedListener(org.talend.repository.
     * IRepositoryChangedListener)
     */
    @Override
    public void removeRepositoryChangedListener(IRepositoryChangedListener listener) {
        CoreRepositoryPlugin.getDefault().removeRepositoryChangedListener(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.model.IRepositoryService#repositoryChanged(org.talend.repository.RepositoryElementDelta)
     */
    @Override
    public void repositoryChanged(IRepositoryElementDelta delta) {
        CoreRepositoryPlugin.getDefault().repositoryChanged(delta);
    }

    // This method is used for the Action in RepositoryView to synchronize the sqlBuilder.
    // see DataBaseWizard, DatabaseTableWizard, AContextualAction
    @Override
    public void notifySQLBuilder(List<IRepositoryViewObject> list) {
        IRepositoryChangedListener listener = (IRepositoryChangedListener) RepositoryManagerHelper.getDIRepositoryView(false);
        if (listener != null) {
            removeRepositoryChangedListener(listener);
        }
        for (IRepositoryViewObject element : list) {
            repositoryChanged(new RepositoryElementDelta(element));
        }
        if (listener != null) {
            registerRepositoryChangedListenerAsFirst(listener);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#validateColumnName(java.lang.String, int)
     */
    @Override
    public String validateColumnName(String columnName, int index) {
        return ColumnNameValidator.validateColumnNameFormat(columnName, index);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#openLoginDialog()
     */
    @Override
    public void openLoginDialog() {
        if (isloginDialogDisabled()) {
            return;
        }

        if (CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY) != null) {
            return;
        }

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        boolean logged = false;
        LoginDialogV2 loginDialog = new LoginDialogV2(shell);
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true);
        logged = loginDialog.open() == LoginDialogV2.OK;
        if (logged) {

            // addCommand();
            new DisableLanguageActions().earlyStartup();

            new BindingActions().bind();

            IMigrationToolService toolService = CorePlugin.getDefault().getMigrationToolService();
            toolService.executeMigration(SwitchProjectAction.PLUGIN_MODEL);

            IRunProcessService runService = CorePlugin.getDefault().getRunProcessService();
            runService.deleteAllJobs(SwitchProjectAction.PLUGIN_MODEL);

            CorePlugin.getDefault().getCodeGeneratorService().initializeTemplates();
            CorePlugin.getDefault().getDesignerCoreService()
                    .synchronizeDesignerUI(new PropertyChangeEvent(this, IComponentConstants.NORMAL, null, null));
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#openLoginDialog(org.eclipse.swt.widgets.Shell, boolean)
     */
    @Override
    public boolean openLoginDialog(Shell shell) {
        if (isloginDialogDisabled()) {
            return true;
        }
        boolean logged = false;
        if (!LoginHelper.isAlwaysAskAtStartup() && !LoginHelper.isNeedForceShowLogonDialog()) {
            logged = LoginHelper.getInstance().loginAuto();
        }
        if (!logged) {
            LoginDialogV2 loginDialog = new LoginDialogV2(shell);
            logged = (loginDialog.open() == LoginDialogV2.OK);
        }
        return logged;

    }

    private boolean isloginDialogDisabled() {
        boolean reload = Boolean.parseBoolean(System.getProperty("talend.project.reload")); //$NON-NLS-1$
        reload = reload || Boolean.getBoolean("talend.project.reload4Proj");
        PreferenceManipulator preferenceManipulator = new PreferenceManipulator();
        ConnectionBean lastBean = null;
        if (reload) {
            final ConnectionUserPerReader instance = ConnectionUserPerReader.getInstance();
            instance.forceReadConnections();
            final String lastConncetion = ConnectionUserPerReader.getInstance().readLastConncetion();
            for (ConnectionBean bean : instance.readConnections()) {
                if (bean.getName().equals(lastConncetion)) {
                    lastBean = bean;
                    break;
                }
            }
        }

        if (ArrayUtils.contains(Platform.getApplicationArgs(), EclipseCommandLine.TALEND_DISABLE_LOGINDIALOG_COMMAND)) {
            boolean deleteProjectIfExist = ArrayUtils.contains(Platform.getApplicationArgs(), "--deleteProjectIfExist"); //$NON-NLS-1$
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            brandingService.getBrandingConfiguration().setUseProductRegistration(false);
            ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();

            String projectName = getAppArgValue("-project", "AUTO_LOGIN_PROJECT"); //$NON-NLS-1$ //$NON-NLS-2$
            String language = getAppArgValue("-language", ECodeLanguage.JAVA.getName()); //$NON-NLS-1$
            String login = getAppArgValue("-login", "auto@login.com"); //$NON-NLS-1$ //$NON-NLS-2$
            String password = getAppArgValue("-loginPass", ""); //$NON-NLS-1$ //$NON-NLS-2$
            String tacURL = getAppArgValue("-tacURL", null); //$NON-NLS-1$
            // if tacURL is null, the branch will be no useful.
            String branch = getAppArgValue("-branch", null); //$NON-NLS-1$
            // if tacURL is not null, will be remote
            final boolean isRemote = tacURL != null;

            if (reload && lastBean != null) {
                final String lastProject = preferenceManipulator.getLastProject();
                if (lastProject != null) {
                    projectName = lastProject;
                }
                final String lastUser = lastBean.getUser();
                if (lastUser != null) {
                    login = lastUser;
                }
                final String lastPass = lastBean.getPassword();
                if (lastPass != null) {
                    password = lastPass;
                }

            }

            User userInfo = PropertiesFactoryImpl.eINSTANCE.createUser();
            userInfo.setLogin(login);
            try {
                userInfo.setPassword(PasswordHelper.encryptPasswd(password));
            } catch (NoSuchAlgorithmException e) {
                ExceptionHandler.process(e);
            }

            LoginHelper.isAutoLogonFailed = false;
            try {
                ConnectionBean bean = null;

                if (reload && lastBean != null) {// reload
                    bean = lastBean;
                } else {
                    if (tacURL != null && isRemote) { // remote
                        bean = ConnectionBean.getDefaultRemoteConnectionBean();
                        bean.setUser(login);
                        bean.setPassword(password);
                        bean.getDynamicFields().put(RepositoryConstants.REPOSITORY_URL, tacURL);
                    } else {
                        bean = ConnectionBean.getDefaultConnectionBean();
                    }
                }
                LoginHelper.getInstance().setCurrentSelectedConnBean(bean);

                Context ctx = CorePlugin.getContext();
                RepositoryContext repositoryContext = new RepositoryContext();
                ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, repositoryContext);

                repositoryContext.setUser(userInfo);
                repositoryContext.setClearPassword(password);
                repositoryContext.setFields(bean.getDynamicFields());
                repositoryContext.setToken(bean.isToken());

                repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider.getRepositoriyById(bean
                        .getRepositoryId()));
                Project project = null;
                for (Project p : repositoryFactory.readProject()) {
                    if (p.getLabel().equals(projectName) || p.getTechnicalLabel().equals(projectName)) {
                        project = p;
                        break;
                    }
                }
                if (!reload) {
                    if (deleteProjectIfExist && project != null) {
                        IWorkspace workspace = ResourcesPlugin.getWorkspace();
                        IProject eclipseProject = workspace.getRoot().getProject(project.getTechnicalLabel());
                        if (eclipseProject.exists()) {
                            eclipseProject.delete(true, new NullProgressMonitor());
                        }
                    }
                    if (!isRemote && (project == null || deleteProjectIfExist)) {
                        Project projectInfor = ProjectHelper.createProject(projectName, "", //$NON-NLS-1$
                                language, userInfo);
                        project = repositoryFactory.createProject(projectInfor);
                    }
                } else {
                    if (project != null && !project.getEmfProject().isLocal() && repositoryFactory.isLocalConnectionProvider()) {
                        List<IRepositoryFactory> rfList = RepositoryFactoryProvider.getAvailableRepositories();
                        IRepositoryFactory remoteFactory = null;
                        for (IRepositoryFactory rf : rfList) {
                            if (!rf.isLocalConnectionProvider()) {
                                remoteFactory = rf;
                                break;
                            }
                        }
                        if (remoteFactory != null) {
                            repositoryFactory.setRepositoryFactoryFromProvider(remoteFactory);
                            repositoryFactory.getRepositoryContext().setOffline(true);
                        }
                    }

                }
                if (!repositoryFactory.isLocalConnectionProvider()) {
                    ProjectManager.getInstance().setMainProjectBranch(
                            project,
                            preferenceManipulator.getLastSVNBranch(
                                    new JSONObject(project.getEmfProject().getUrl()).getString("location"),
                                    project.getTechnicalLabel()));
                }
                if (project != null && reload && lastBean != null && repositoryFactory.getRepositoryContext().isOffline()) {
                    if (PluginChecker.isSVNProviderPluginLoaded()) {
                        ISVNProviderService svnProviderService = (ISVNProviderService) GlobalServiceRegister.getDefault()
                                .getService(ISVNProviderService.class);
                        if (svnProviderService.isSVNProject(project)) {
                            String projectUrl = svnProviderService.getProjectUrl(project);
                            String lastBranch = preferenceManipulator.getLastSVNBranch(projectUrl, project.getTechnicalLabel());
                            ProjectManager.getInstance().setMainProjectBranch(project, lastBranch);
                        }
                    }
                }
                if (project == null) {
                    throw new LoginException(Messages.getString("RepositoryService.projectNotFound", projectName)); //$NON-NLS-1$
                }
                repositoryContext.setProject(project);

                repositoryFactory.logOnProject(project, new NullProgressMonitor());
            } catch (final PersistenceException e) {
                if (e instanceof OperationCancelException) {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openError(Display.getDefault().getActiveShell(),
                                    Messages.getString("LoginDialog.logonCanceled"), e.getLocalizedMessage());
                        }

                    });
                } else if (e instanceof InformException) {
                    Display.getDefault().syncExec(() -> MessageDialog.openInformation(Display.getDefault().getActiveShell(),
                            Messages.getString("LoginDialog.logonDenyTitle"), e.getLocalizedMessage()));
                } else {
                    MessageBoxExceptionHandler.process(e, DisplayUtils.getDefaultShell(false));
                }
                repositoryFactory.logOffProject();
                LoginHelper.isAutoLogonFailed = true;
            } catch (LoginException e) {
                MessageBoxExceptionHandler.process(e, DisplayUtils.getDefaultShell(false));
                repositoryFactory.logOffProject();
                LoginHelper.isAutoLogonFailed = true;
            } catch (BusinessException e) {
                MessageBoxExceptionHandler.process(e, DisplayUtils.getDefaultShell(false));
                repositoryFactory.logOffProject();
                LoginHelper.isAutoLogonFailed = true;
            } catch (CoreException e) {
                MessageBoxExceptionHandler.process(e, DisplayUtils.getDefaultShell(false));
                repositoryFactory.logOffProject();
                LoginHelper.isAutoLogonFailed = true;
            } catch (JSONException e) {
                ExceptionHandler.process(e);
                LoginHelper.isAutoLogonFailed = true;
            }

            if (LoginHelper.isAutoLogonFailed) {
                LoginHelper.isRestart=true;
            }

            return true;
        }
        return false;
    }

    private String getAppArgValue(String arg, String defaultValue) {
        String value = defaultValue;
        int index = ArrayUtils.indexOf(Platform.getApplicationArgs(), arg);
        if (index > 0) {
            if (index + 1 < Platform.getApplicationArgs().length) {
                value = Platform.getApplicationArgs()[index + 1];
            }
        }
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#initializeForTalendStartupJob()
     */
    @Override
    public void initializeForTalendStartupJob() {
        // do nothing now.

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#initializeTalend()
     */
    @Override
    public void initializePluginMode() {

        if (CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY) != null) {
            return;
        }
        openLoginDialog();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#isRCPMode()
     */
    @Override
    public boolean isRCPMode() {
        return CoreRepositoryPlugin.getDefault().isRCPMode();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#setRCPMode()
     */
    @Override
    public void setRCPMode() {
        CoreRepositoryPlugin.getDefault().setRCPMode();
    }

    @Override
    public DatabaseConnection cloneOriginalValueConnection(DatabaseConnection dbConn) {
        return DBConnectionContextUtils.cloneOriginalValueConnection(dbConn);
    }

    @Override
    public DatabaseConnection cloneOriginalValueConnection(DatabaseConnection dbConn, boolean defaultContext) {
        return DBConnectionContextUtils.cloneOriginalValueConnection(dbConn, defaultContext, null);
    }

    @Override
    public void setMetadataConnectionParameter(DatabaseConnection dbConn, IMetadataConnection metaConn) {
        DBConnectionContextUtils.setMetadataConnectionParameter(dbConn, metaConn);
    }

    @Override
    public DatabaseConnection cloneOriginalValueConnection(DatabaseConnection dbConn, boolean defaultContext,
            String selectedContext) {
        return DBConnectionContextUtils.cloneOriginalValueConnection(dbConn, defaultContext, selectedContext);
    }

    @Override
    public FileConnection cloneOriginalValueConnection(FileConnection fileConn) {
        return CloneConnectionUtils.cloneOriginalValueConnection(fileConn, false);
    }

    @Override
    public IEditorPart openSQLPatternEditor(SQLPatternItem item, boolean readOnly) {
        IEditorPart openSQLPatternEditor = null;
        try {
            openSQLPatternEditor = new EditSqlpatternAction().openSQLPatternEditor(item, readOnly);
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        } catch (SystemException e) {
            ExceptionHandler.process(e);
        }
        return openSQLPatternEditor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#createSqlpattern()
     */
    @Override
    public void createSqlpattern(String path, boolean isFromSqlPatternComposite) {
        new CreateSqlpatternAction(path, isFromSqlPatternComposite).run();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.IRepositoryService#addRepositoryViewListener(org.eclipse.ui.ISelectionListener)
     */
    @Override
    public void addRepositoryTreeViewListener(ISelectionChangedListener listener) {
        IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
        if (repositoryView != null) {
            StructuredViewer treeViewer = repositoryView.getViewer();
            if (treeViewer != null) {
                treeViewer.addSelectionChangedListener(listener);
            } else {
                // RepositoryView.addPreparedListeners(listener);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.repository.model.IRepositoryService#removeRepositoryTreeViewListener(org.eclipse.jface.viewers.
     * ISelectionChangedListener)
     */
    @Override
    public void removeRepositoryTreeViewListener(ISelectionChangedListener listener) {
        IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
        if (repositoryView != null) {
            StructuredViewer treeViewer = repositoryView.getViewer();
            if (treeViewer != null) {
                treeViewer.removeSelectionChangedListener(listener);
            }
        }
    }

    @Override
    public IPreferenceStore getRepositoryPreferenceStore() {
        return RepositoryPlugin.getDefault().getPreferenceStore();
    }

    @Override
    public RepositoryNode getRepositoryNode(String id, boolean expanded) {
        return RepositoryNodeUtilities.getRepositoryNode(id, expanded);
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.repository.model.IRepositoryService#openRepositoryReviewDialog(org.talend.core.model.repository.
     * ERepositoryObjectType, java.lang.String)
     */
    @Override
    public ContextItem openRepositoryReviewDialog(ERepositoryObjectType type, String repositoryType,
            List<IContextParameter> params, IContextManager contextManager) {
        ContextRepositoryReviewDialog dialog = new ContextRepositoryReviewDialog(DisplayUtils.getDefaultShell(false), type,
                params, contextManager);
        dialog.setFilterReferenceNode(true);
        if (dialog.open() == Window.OK) {
            return dialog.getItem();
        }
        return null;
    }

    /**
     * wzhang Comment method "getRootRepositoryNode".
     *
     * @param type
     * @return
     */
    @Override
    public RepositoryNode getRootRepositoryNode(ERepositoryObjectType type) {
        IRepositoryView view = RepositoryManagerHelper.getRepositoryView();
        if (view != null) {
            ProjectRepositoryNode root = (ProjectRepositoryNode) view.getRoot();
            return root.getRootRepositoryNode(type);
        }
        return null;
    }

    @Override
    public void setInternalNodeHTMLMap(INode node, Map<String, Object> internalNodeHTMLMap) {
        IElementParameter propertyParam = null;
        IElementParameter functionParam = null;
        for (IElementParameter param : node.getElementParameters()) {

            if ("PROPERTY".equals(param.getName())) { //$NON-NLS-1$
                propertyParam = param.getChildParameters().get("REPOSITORY_PROPERTY_TYPE"); //$NON-NLS-1$
            }
            if ("SAP_FUNCTION".equals(param.getName())) { //$NON-NLS-1$
                functionParam = param;
            }
        }
        if (propertyParam != null && functionParam != null) {
            try {
                IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(
                        (String) propertyParam.getValue());
                if (lastVersion != null) {

                    Item item = lastVersion.getProperty().getItem();
                    if (item instanceof SAPConnectionItem) {
                        SAPConnectionItem sapItem = (SAPConnectionItem) item;
                        SAPConnection connection = (SAPConnection) sapItem.getConnection();
                        connection.getFuntions();
                        for (Object obj : connection.getFuntions()) {
                            if (obj instanceof SAPFunctionUnit) {
                                SAPFunctionUnit function = (SAPFunctionUnit) obj;
                                String functionName = (String) functionParam.getValue();
                                if (function.getName().equals(functionName.substring(1, functionName.length() - 1))) {
                                    if (!function.getDocument().isEmpty()) {
                                        String document = ModelElementHelper.getFirstDocument(function).getReference();
                                        if (document != null && !"".equals(document)) { //$NON-NLS-1$

                                            internalNodeHTMLMap.put(node.getUniqueName(),
                                                    document.substring(document.indexOf("<font"), document.indexOf("</body>"))); //$NON-NLS-1$ //$NON-NLS-2$
                                        }
                                    }
                                }

                            }
                        }

                    }
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

    }

    @Override
    public IDialogSettings getDialogSettings() {
        return RepositoryPlugin.getDefault().getDialogSettings();
    }

    @Override
    public Set<MetadataTable> getTablesFromSpecifiedDataPackage(DatabaseConnection dbconn) {
        return ProjectNodeHelper.getTablesFromSpecifiedDataPackage(dbconn);
    }

    @Override
    public Class getClassForSalesforceModule() {
        return SalesforceModuleRepositoryObject.class;
    }

    @Override
    public AContextualAction getCreateRoutineAction(IRepositoryView repositoryView) {
        CreateRoutineAction createRoutineAction = new CreateRoutineAction(true);
        createRoutineAction.setWorkbenchPart(repositoryView);
        return createRoutineAction;
    }

    @Override
    public String getRulesProviderPath(RulesItem currentRepositoryItem) {
        IRulesProviderService rulesService = null;
        if (PluginChecker.isRulesPluginLoaded()) {
            rulesService = (IRulesProviderService) GlobalServiceRegister.getDefault().getService(IRulesProviderService.class);
            try {
                rulesService.syncRule(currentRepositoryItem);
                IFile ruleFile = rulesService.getRuleFile(currentRepositoryItem, FileConstants.XLS_FILE_SUFFIX);
                if (ruleFile == null) {
                    return null;
                }
                String path = ruleFile.getLocation().toOSString();
                return path;
            } catch (SystemException e) {
                // added by SeB, log it at least butthe devlopper should have a look at this
                log.error("failed to get the Rules provider path", e); //$NON-NLS-1$
            }
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public boolean openReadOnlyDialog(Shell shell) {
        String branchSelection = ProjectManager.getInstance().getMainProjectBranch(
                ProjectManager.getInstance().getCurrentProject());
        if (branchSelection != null) {
            if (branchSelection.startsWith(SVNConstant.NAME_TAGS)) {
                MessageDialog.openInformation(shell, Messages.getString("RepositoryService.projectReadonlyTitle"), //$NON-NLS-1$
                        Messages.getString("RepositoryService.projectReadonly")); //$NON-NLS-1$
            }
        }
        return true;
    }

    @Override
    public RepositoryNode getRepNodeFromRepReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType) {
        RepositoryReviewDialog dialog = new RepositoryReviewDialog(parentShell, type, repositoryType);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }

        return null;
    }

    @Override
    public void openProjectSettingDialog(final String pageId) {
        new ProjectSettingDialog().open(pageId);
    }

    @Override
    public List<String> getProjectBranch(Project project) {
        List<String> branchesList = new ArrayList<String>();
        if (!isInitedProviderService) {
            initProviderService();
        }
        if (project != null) {
            try {
                if (!project.isLocal() && svnProviderService != null && svnProviderService.isSVNProject(project)) {
                    branchesList.add(SVNConstant.NAME_TRUNK);
                    String[] branchList = svnProviderService.getBranchList(project);
                    if (branchList != null) {
                        branchesList.addAll(Arrays.asList(branchList));
                    }
                }
                if (!project.isLocal() && gitProviderService != null && gitProviderService.isGITProject(project)) {
                    branchesList.addAll(Arrays.asList(gitProviderService.getBranchList(project)));
                }
            } catch (PersistenceException e) {
                CommonExceptionHandler.process(e);
            }
        }
        return branchesList;
    }

    private void initProviderService() {
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            try {
                svnProviderService = (ISVNProviderService) GlobalServiceRegister.getDefault()
                        .getService(ISVNProviderService.class);
                gitProviderService = (IGITProviderService) GlobalServiceRegister.getDefault()
                        .getService(IGITProviderService.class);
            } catch (RuntimeException e) {
                // nothing to do
            }
        }
        isInitedProviderService = true;
    }

    @Override
    public boolean askRetryForNetworkIssue(Throwable ex) {
        /**
         * Don't popup dialog for junit since it will block the junit
         */
        if (CommonsPlugin.isJUnitTest()) {
            return false;
        }
        if (donnotRetryByCancel) {
            return false;
        }
        final AtomicBoolean retry = new AtomicBoolean(false);
        try {

            /**
             * Only popup one dialog for one time.
             */
            if (askUserForNetworkIssueSemaphore.availablePermits() <= 0) {
                askUserForNetworkIssueSemaphore.acquire();
                askUserForNetworkIssueSemaphore.release();
                return askUserForNetworkIssueRetryCache;
            }
            askUserForNetworkIssueSemaphore.acquire();

            if (Display.getCurrent() == null) {
                try {
                    Display defaultDisplay = Display.getDefault();

                    /**
                     * Check whether UI thread is busy
                     */
                    if (defaultDisplay.getThread().getState() == Thread.State.RUNNABLE) {
                        defaultDisplay.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                retry.set(askRetryForNetworkIssueInDialog(DisplayUtils.getDefaultShell(), ex));
                            }
                        });
                    } else {
                        /**
                         * If UI thread is busy, to avoid dead lock, we need to create a new UI thread and run dialog in
                         * this new UI thread
                         */
                        DisplayUtils.syncExecInNewUIThread(new Runnable() {

                            @Override
                            public void run() {
                                retry.set(askRetryForNetworkIssueInDialog(null, ex));
                            }
                        });
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            } else {
                retry.set(askRetryForNetworkIssueInDialog(DisplayUtils.getDefaultShell(), ex));
            }

        } catch (Throwable t) {
            ExceptionHandler.process(t);
        } finally {
            askUserForNetworkIssueRetryCache = retry.get();
            if (askUserForNetworkIssueSemaphore.availablePermits() <= 0) {
                askUserForNetworkIssueSemaphore.release();
            }
        }
        return askUserForNetworkIssueRetryCache;
    }

    private boolean askRetryForNetworkIssueInDialog(Shell shell, Throwable ex) {
        NetworkErrorRetryDialog dialog = new NetworkErrorRetryDialog(shell, ex);
        int result = dialog.open();
        donnotRetryByCancel = dialog.isDonnotRetryByCancel();
        return NetworkErrorRetryDialog.BUTTON_RETRY_INDEX == result;
    }

    @Override
    public boolean isSVN() {
        if (svnProviderService == null && PluginChecker.isSVNProviderPluginLoaded()) {
            svnProviderService = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
        }
        if (svnProviderService != null) {
            return svnProviderService.isProjectInSvnMode();
        }
        return false;
    }

    @Override
    public boolean isGIT() {
        if (gitProviderService == null && PluginChecker.isGITProviderPluginLoaded()) {
            gitProviderService = (IGITProviderService) GlobalServiceRegister.getDefault().getService(IGITProviderService.class);
        }
        if (gitProviderService != null) {
            return gitProviderService.isProjectInGitMode();
        }
        return false;
    }

    @Override
    public void setShouldCheckRepoViewCommonNavigatorDirty(IRepositoryView repView, boolean shouldFlag) {
        if (repView instanceof RepoViewCommonNavigator) {
            // RepoViewCommonNavigator.shouldCheckRepositoryDirty default is true
            // If set to false, after execute, should set back to true
            ((RepoViewCommonNavigator) repView).setShouldCheckRepositoryDirty(shouldFlag);
        }
    }
}
