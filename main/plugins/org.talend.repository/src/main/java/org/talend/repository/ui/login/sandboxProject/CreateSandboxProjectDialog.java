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
package org.talend.repository.ui.login.sandboxProject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.User;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.utils.ProjectUtils;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;

/**
 * ggu class global comment. Detailled comment
 */
public class CreateSandboxProjectDialog extends TitleAreaDialog {

    /**
     * must be same as tac pattern.
     */
    // RepositoryConstants.MAIL_PATTERN
    private static final String MAIL_PATTERN = "(?:[a-zA-ZÀ-ÿ0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-ZÀ-ÿ0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-zA-ZÀ-ÿ0-9](?:[a-zA-ZÀ-ÿ0-9-]*[a-zA-ZÀ-ÿ0-9])?\\.)+[a-zA-ZÀ-ÿ0-9](?:[a-zA-ZÀ-ÿ0-9-]*[a-zA-ZÀ-ÿ0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-ZÀ-ÿ0-9-]*[a-zA-ZÀ-ÿ0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"; //$NON-NLS-1$

    private static final String FIRSTNAME_PATTERN = "^[a-zA-Zà-ÿ0-9 _\\-]*$"; //$NON-NLS-1$

    private static final String LASTNAME_PATTERN = "^[a-zA-Z0-9 _-]+$"; //$NON-NLS-1$

    /**
     *
     */
    private Group projectGroup;

    private LabelledText urlText;

    private Button checkBtn;

    private LabelledCombo languageCombo;

    private LabelledText projectLabelText;

    private Group userGroup;

    private LabelledText userLoginText, userPassText, userFirstNameText, userLastNameText;

    private Project[] projects = null;

    private ConnectionBean bean;

    private boolean enableSandboxProject;

    private RepositoryContext originRepCtx;

    /**
     * @deprecated should be no used after bug 15815.
     */
    @Deprecated
    private final ConnectionBean currentConnBean;

    public CreateSandboxProjectDialog(Shell parentShell, ConnectionBean currentConnBean) {
        super(parentShell);
        this.currentConnBean = currentConnBean;
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        ImageDescriptor imgDesc = brandingService.getLoginHImage();
        if (imgDesc != null) {
            setTitleImage(ImageProvider.getImage(imgDesc));
        }

        originRepCtx = ProxyRepositoryFactory.getInstance().getRepositoryContext();
        initProjectList();
    }

    // public boolean existedBeforeConn() {
    // return this.currentConnBean != null // there is no any connection.
    // && RepositoryConstants.REPOSITORY_REMOTE_ID.equals(this.currentConnBean.getRepositoryId()); // if not
    // // local
    // }

    // private String getExistedBeforeConnURL() {
    // if (existedBeforeConn()) {
    // return this.currentConnBean.getDynamicFields().get(RepositoryConstants.REPOSITORY_URL);
    // }
    // return null;
    // }

    private void initProjectList() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();

        try {
            projects = repositoryFactory.readProject();
        } catch (Exception e) {
            //
        }
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        String fullProductName = brandingService.getFullProductName();
        newShell.setText(Messages.getString("CreateSandboxProjectDialog.Title", fullProductName)); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        createProjectInfors(composite);
        addProjectListeners();
        addUserListeners();
        return composite;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        checkFields();
        // if (userLoginText != null) {
        // userLoginText.getTextControl().forceFocus(); // focus this first
        // }
        return contents;
    }

    private void createProjectInfors(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // url
        Composite urlComp = new Composite(composite, SWT.NONE);
        urlComp.setLayout(new GridLayout(3, false));
        urlComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        urlText = new LabelledText(urlComp, Messages.getString("CreateSandboxProjectDialog.UrlTitle")); //$NON-NLS-1$
        // if (existedBeforeConn()) {
        // urlText.setText(getExistedBeforeConnURL());
        // }
        checkBtn = new Button(urlComp, SWT.PUSH);
        checkBtn.setText(Messages.getString("CreateSandboxProjectDialog.CheckTitle")); //$NON-NLS-1$

        projectGroup = new Group(composite, SWT.NONE);
        projectGroup.setText(Messages.getString("CreateSandboxProjectDialog.Settings")); //$NON-NLS-1$
        projectGroup.setLayout(new GridLayout(2, false));
        projectGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        projectLabelText = new LabelledText(projectGroup,
                Messages.getString("CreateSandboxProjectDialog.ProjectLabel"), 1, SWT.SINGLE); //$NON-NLS-1$
        GridData layoutData = new GridData();
        layoutData.widthHint = 180;
        layoutData.minimumWidth = 180;
        projectLabelText.setLayoutData(layoutData);
        projectLabelText.getTextControl().setEditable(false);

        languageCombo = new LabelledCombo(projectGroup, Messages.getString("NewProjectWizardPage.language"), null, new String[] { //$NON-NLS-1$
                ECodeLanguage.JAVA.getName() });
        layoutData = new GridData();
        layoutData.widthHint = 100;
        layoutData.minimumWidth = 100;
        languageCombo.getCombo().setLayoutData(layoutData);
        languageCombo.select(0); // default for java

        // user
        createUserInfors(projectGroup);

    }

    private void addProjectListeners() {

        ModifyListener listener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (e.widget == urlText.getTextControl()) {
                    enableSandboxProject = false;
                }
                checkFields();
            }
        };
        urlText.addModifyListener(listener);
        projectLabelText.addModifyListener(listener);
        checkBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (urlText.getText().trim().length() > 0) {
                    Context ctx = CorePlugin.getContext();
                    RepositoryContext oldContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
                    try {
                        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                        // always remote
                        // if (factory.getRepositoryFactoryFromProvider() == null) { //
                        factory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider
                                .getRepositoriyById(RepositoryConstants.REPOSITORY_REMOTE_ID));
                        // }
                        if (factory.isLocalConnectionProvider()) {
                            MessageDialog.openError(getShell(), Messages.getString("CreateSandboxProjectDialog.ErrorTitle"), //$NON-NLS-1$
                                    Messages.getString("CreateSandboxProjectDialog.ErrorLocalSuppportMessages")); //$NON-NLS-1$
                        } else {
                            // set url for
                            RepositoryContext repositoryContext = new RepositoryContext();
                            repositoryContext.setFields(new HashMap<String, String>());
                            repositoryContext.setClearPassword(originRepCtx.getClearPassword());
                            repositoryContext.setToken(bean != null ? bean.isToken() : false);
                            repositoryContext.setUser(originRepCtx.getUser());
                            repositoryContext.getFields().put(RepositoryConstants.REPOSITORY_URL, urlText.getText());
                            ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, repositoryContext);
                            //
                            enableSandboxProject = factory.enableSandboxProject();
                            if (!enableSandboxProject) {
                                MessageDialog.openError(getShell(), Messages.getString("CreateSandboxProjectDialog.ErrorTitle"), //$NON-NLS-1$
                                        Messages.getString("CreateSandboxProjectDialog.ErrorSuppportMessages")); //$NON-NLS-1$
                            }
                        }
                    } catch (PersistenceException e1) {
                        ExceptionHandler.process(e1);
                        MessageDialog.openError(getShell(), Messages.getString("CreateSandboxProjectDialog.ErrorTitle"), //$NON-NLS-1$
                                e1.getMessage());
                    } finally {
                        // revert
                        if (oldContext != null) {
                            ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, oldContext);
                        }
                    }
                }
                checkFields();
            }

        });
    }

    private void createUserInfors(Composite parent) {
        userGroup = new Group(parent, SWT.NULL);
        userGroup.setLayout(new GridLayout(4, false));
        GridData userLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        userLayoutData.horizontalSpan = 2;
        userGroup.setLayoutData(userLayoutData);

        userLoginText = new LabelledText(userGroup, Messages.getString("CreateSandboxProjectDialog.Login")); //$NON-NLS-1$
        userPassText = new LabelledText(userGroup, Messages.getString("CreateSandboxProjectDialog.Password")); //$NON-NLS-1$
        userPassText.getTextControl().setEchoChar('*');

        userFirstNameText = new LabelledText(userGroup, Messages.getString("CreateSandboxProjectDialog.userFirstname")); //$NON-NLS-1$
        userLastNameText = new LabelledText(userGroup, Messages.getString("CreateSandboxProjectDialog.userLastname")); //$NON-NLS-1$

    }

    private void addUserListeners() {
        ModifyListener listener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (e.widget == userLoginText.getTextControl()) {
                    String generateProjectName = ProjectHelper.generateSandbocProjectName(userLoginText.getText());
                    projectLabelText.setText(generateProjectName);
                }
                checkFields();
            }
        };

        userLoginText.addModifyListener(listener);
        userPassText.addModifyListener(listener);
        userFirstNameText.addModifyListener(listener);
        userLastNameText.addModifyListener(listener);
    }

    private void checkFields() {

        // check field
        String projectLabel = projectLabelText.getText();

        checkEnable();

        boolean enableProjectLabel = false;
        if (urlText.getText().trim().length() == 0) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.URLEmptyMessage")); //$NON-NLS-1$
        } else if (!this.enableSandboxProject) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.needCheckMessages")); //$NON-NLS-1$
        } else if ((userLoginText.getText().length() == 0 || !Pattern.matches(MAIL_PATTERN, userLoginText.getText()))) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.userLoginValidMessage")); //$NON-NLS-1$
        } else if (ProjectUtils.isNotValidProjectName(projectLabel)) {
            setErrorMessage(Messages.getString("NewProjectWizardPage.illegalCharacter")); //$NON-NLS-1$
            enableProjectLabel = true;
        } else if (isProjectNameAlreadyUsed(projectLabel)) {
            setErrorMessage(Messages.getString("NewProjectWizardPage.projectNameAlredyExists")); //$NON-NLS-1$
            enableProjectLabel = true;
        } else if (userPassText.getText().length() == 0) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.userPasswordEmptyMessage")); //$NON-NLS-1$
        } else if (userFirstNameText.getText().trim().length() == 0) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.FirstNameEmptyMessages")); //$NON-NLS-1$
        } else if (!Pattern.matches(FIRSTNAME_PATTERN, userFirstNameText.getText())) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.FirstNameInvalidMessages")); //$NON-NLS-1$
        } else if (userLastNameText.getText().trim().length() == 0) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.LastNameEmptyMessages")); //$NON-NLS-1$
        } else if (!Pattern.matches(LASTNAME_PATTERN, userLastNameText.getText())) {
            setErrorMessage(Messages.getString("CreateSandboxProjectDialog.LastNameInvalidMessages")); //$NON-NLS-1$
        } else {
            setErrorMessage(null);
        }
        checkProjectLabel(enableProjectLabel);
    }

    @Override
    public void setErrorMessage(String newErrorMessage) {
        super.setErrorMessage(newErrorMessage);
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed()) {
            button.setEnabled(newErrorMessage == null);
        }

    }

    private void checkProjectLabel(boolean editable) {
        if (editable) {
            projectLabelText.getTextControl().setEditable(true);
        } else {
            projectLabelText.getTextControl().setEditable(false);
        }
    }

    private void checkEnable() {
        projectLabelText.setEnabled(this.enableSandboxProject);
        languageCombo.setEnabled(this.enableSandboxProject);
        userLoginText.setEnabled(this.enableSandboxProject);
        userPassText.setEnabled(this.enableSandboxProject);
        userFirstNameText.setEnabled(this.enableSandboxProject);
        userLastNameText.setEnabled(this.enableSandboxProject);
    }

    private boolean isProjectNameAlreadyUsed(String newProjectName) {

        if (projects == null) { // need check later.
            return false;
        }
        for (Project project : projects) {
            if (Project.createTechnicalName(newProjectName).compareTo(project.getTechnicalLabel()) == 0) {
                return true;
            }
        }
        return false;
    }

    private String generateConnectionName(String login) {

        if (Pattern.matches(RepositoryConstants.MAIL_PATTERN, login)) {
            int at = login.indexOf('@');
            if (at > -1) {
                String mailName = login.substring(0, at);
                if (mailName.length() > 0) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(mailName);
                    sb.append("-Connection"); //$NON-NLS-1$
                    return sb.toString();
                }
            }
        }
        return null;
    }

    @Override
    protected void okPressed() {

        //        boolean confirm = MessageDialog.openConfirm(getShell(), Messages.getString("CreateSandboxProjectDialog.ConfirmTitle"), //$NON-NLS-1$
        //                Messages.getString("CreateSandboxProjectDialog.ConfirmMessages")); //$NON-NLS-1$
        // if (!confirm) {
        // super.okPressed();
        // return;
        // }
        //
        final String url = urlText.getText();
        final String projectName = projectLabelText.getText();
        final String projectLanguage = languageCombo.getText();
        final String projectAuthor = userLoginText.getText();
        final String projectAuthorPass = userPassText.getText();
        final String projectAuthorFirstname = userFirstNameText.getText();
        final String projectAuthorLastname = userLastNameText.getText();

        // final boolean needCreateNewConn = !existedBeforeConn() || !url.trim().equals(getExistedBeforeConnURL());

        bean = new ConnectionBean();
        bean.setRepositoryId(RepositoryConstants.REPOSITORY_REMOTE_ID);
        bean.setUser(projectAuthor);
        bean.setPassword(projectAuthorPass);
        bean.setName(generateConnectionName(projectAuthor));
        bean.setDescription(bean.getName());
        bean.setWorkSpace(new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath());
        bean.getDynamicFields().put(RepositoryConstants.REPOSITORY_URL, url);
        bean.setComplete(true);

        // set context for url and in order to create project later.
        RepositoryContext repositoryContext = new RepositoryContext();
        // if (existedBeforeConn()) {
        // Context ctx = CorePlugin.getContext();
        // RepositoryContext oldContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        // repositoryContext.setUser(oldContext.getUser());
        // repositoryContext.setClearPassword(oldContext.getClearPassword());
        // } else {
        User user = ProjectHelper.createUser(projectAuthor, projectAuthorPass, projectAuthorFirstname, projectAuthorLastname,
                false);
        repositoryContext.setUser(user);
        repositoryContext.setClearPassword(projectAuthorPass);
        // }
        repositoryContext.setFields(bean.getDynamicFields());
        Context ctx = CorePlugin.getContext();
        ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, repositoryContext);

        // set provider
        // if (!existedBeforeConn()) { // no connection
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider.getRepositoriyById(bean.getRepositoryId()));
        // }
        //
        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Display disp = Display.getCurrent();
                if (disp == null) {
                    disp = Display.getDefault();
                }
                disp.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        monitor.beginTask("Creating...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                        Project projectInfor = ProjectHelper.createProject(projectName, projectName, projectLanguage,
                                projectAuthor, projectAuthorPass, projectAuthorFirstname, projectAuthorLastname, false);
                        projectInfor.setSandboxProject(true);

                        boolean ok = false;
                        try {
                            User authUser = originRepCtx.getUser();
                            String authPassword = originRepCtx.getClearPassword();
                            Project createProject = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                                    .createProject(authUser, authPassword, projectInfor);
                            ok = (createProject != null);
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                            MessageDialog.openError(getShell(), Messages.getString("CreateSandboxProjectDialog.Failure"), //$NON-NLS-1$
                                    Messages.getString("CreateSandboxProjectDialog.failureMessage") //$NON-NLS-1$
                                            + "\n\n" + e.getMessage()); //$NON-NLS-1$
                        }
                        if (ok) { // if not created, will don't close the dialog
                            String messages = Messages.getString("CreateSandboxProjectDialog.successMessage"); //$NON-NLS-1$
                            // if (needCreateNewConn) {
                            messages += "\n\n" //$NON-NLS-1$
                                    + Messages.getString("CreateSandboxProjectDialog.creatingConnectionMessages", bean.getName()); //$NON-NLS-1$
                            // }
                            MessageDialog.openInformation(getShell(),
                                    Messages.getString("CreateSandboxProjectDialog.successTitile"), messages); //$NON-NLS-1$

                            // if (needCreateNewConn) {
                            // save connection
                            ConnectionUserPerReader instance = ConnectionUserPerReader.getInstance();
                            List<ConnectionBean> connections = instance.forceReadConnections();
                            connections.add(bean);
                            instance.saveConnections(connections);
                            // }
                            CreateSandboxProjectDialog.super.okPressed();
                        }
                        monitor.done();
                    }
                });

            }
        };
        try {
            // final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(getShell());
            final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
            dialog.run(true, false, runnable);

        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }
    }

    public ConnectionBean getConnectionBean() {
        return this.bean;
    }

}
