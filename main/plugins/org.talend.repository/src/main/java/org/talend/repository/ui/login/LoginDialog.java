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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.OperationCancelException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ExchangeUser;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.service.IExchangeService;
import org.talend.core.services.ICoreTisService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.registration.wizards.register.TalendForgeDialog;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;

/**
 * Login dialog. <br/>
 *
 * $Id$
 *
 * @deprecated use LoginDialogV2 instead
 */
@Deprecated
public class LoginDialog extends TrayDialog {

    /** The login composite. */
    private LoginComposite loginComposite;

    private ConnectionUserPerReader perReader;

    private Composite base;

    private StackLayout stackLayout;

    private TOSLoginComposite tosLoginComposite;

    /**
     * Construct a new LoginDialog.
     *
     * @param parentShell Parent shell.
     */
    public LoginDialog(Shell parentShell) {
        super(parentShell);
        perReader = ConnectionUserPerReader.getInstance();
        setHelpAvailable(false);
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        newShell.setText(Messages.getString("LoginDialog.title", brandingService.getFullProductName())); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, 0);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);
        // initialize the dialog units
        initializeDialogUnits(composite);
        // create the dialog area and button bar
        dialogArea = createDialogArea(composite);
        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        container.setLayout(layout);
        // container.setBackground(new Color(null, 215, 215, 215));
        container.setBackground(new Color(null, 255, 255, 255));
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        new ImageCanvas(container, brandingService.getLoginVImage());

        if (!perReader.isHaveUserPer()) {
            perReader.createPropertyFile();
        }
        base = new Composite(container, SWT.NONE);
        base.setLayoutData(new GridData(GridData.FILL_BOTH));
        stackLayout = new StackLayout();
        base.setLayout(stackLayout);
        if (!PluginChecker.isRemoteProviderPluginLoaded()) {// tos
            loginComposite = new LoginComposite(base, SWT.NONE, this, tosLoginComposite, stackLayout);
            loginComposite.populateProjectList();
            tosLoginComposite = new TOSLoginComposite(base, SWT.NONE, loginComposite, this);
        } else {
            loginComposite = new LoginComposite(base, SWT.NONE, this, tosLoginComposite, stackLayout);
        }
        GridData data = new GridData(GridData.FILL_BOTH);
        // data.widthHint = INNER_LOGIN_COMPOSITE_WIDTH;
        // data.heightHint = DIALOG_HEIGHT;
        loginComposite.setLayoutData(data);
        stackLayout.topControl = loginComposite;
        base.layout();
        if (!PluginChecker.isRemoteProviderPluginLoaded()) {
            Project[] projectList = readProject();
            if (projectList.length > 0) {
                advanced();
            }
        }
        return container;
    }

    private Project[] readProject() {
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Project[] projects = null;
        try {
            projects = repositoryFactory.readProject();
        } catch (PersistenceException e1) {
            e1.printStackTrace();
        } catch (BusinessException e1) {
            e1.printStackTrace();
        }
        return projects;
    }

    public void advanced() {
        stackLayout.topControl = tosLoginComposite;
        base.layout();
        Project[] projectCollection = tosLoginComposite.readProject();

        Map<String, String> convertorMapper = tosLoginComposite.getConvertorMapper();

        for (Project element : projectCollection) {

            tosLoginComposite.getProjectMap().put(element.getLabel().toUpperCase(), element);
            convertorMapper.put(element.getLabel().toUpperCase(), element.getLabel());

        }

        ListViewer projectListViewer = tosLoginComposite.getProjectListViewer();
        projectListViewer.setInput(new ArrayList(convertorMapper.values()));

        try {
            tosLoginComposite.setStatusArea();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        if (projectListViewer.getList().getItemCount() > 0) {

            projectListViewer.getList().select(0);
            tosLoginComposite.enableOpenAndDelete(true);

        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (LoginComposite.isRestart) {
            super.okPressed();
        } else {
            if (PluginChecker.isRemoteProviderPluginLoaded()) {
                boolean isLogInOk = logIn(loginComposite.getProject());
                if (isLogInOk) {
                    super.okPressed();
                }// else login failed so ignor the ok button.
            } else {
                super.okPressed();
            }
        }
    }

    public void saveLastConnBean(ConnectionBean connBean) {
        perReader.saveLastConnectionBean(connBean);
    }

    /**
     * DOC smallet Comment method "logIn".
     *
     * @param project
     * @throws Exception
     */
    protected boolean logIn(final Project project) {
        final ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        ConnectionBean connBean = loginComposite.getConnection();
        final boolean needRestartForLocal = loginComposite.needRestartForLocal();
        if (connBean == null || project == null || project.getLabel() == null) {
            return false;
        }
        try {
            if (!project.getEmfProject().isLocal() && factory.isLocalConnectionProvider()) {
                List<IRepositoryFactory> rfList = RepositoryFactoryProvider.getAvailableRepositories();
                IRepositoryFactory remoteFactory = null;
                for (IRepositoryFactory rf : rfList) {
                    if (!rf.isLocalConnectionProvider()) {
                        remoteFactory = rf;
                        break;
                    }
                }
                if (remoteFactory != null) {
                    factory.setRepositoryFactoryFromProvider(remoteFactory);
                    factory.getRepositoryContext().setOffline(true);
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        // Save last used parameters
        PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        prefManipulator.setLastProject(project.getTechnicalLabel());
        saveLastConnBean(connBean);
        // check for Talendforge
        if (PluginChecker.isExchangeSystemLoaded() && !TalendPropertiesUtil.isHideExchange()) {
            IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
            boolean checkTisVersion = prefStore.getBoolean(ITalendCorePrefConstants.EXCHANGE_CHECK_TIS_VERSION);
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            if (!checkTisVersion && brandingService.isPoweredbyTalend()) {
                int count = prefStore.getInt(TalendForgeDialog.LOGINCOUNT);
                if (count < 0) {
                    count = 1;
                }
                ExchangeUser exchangeUser = project.getExchangeUser();
                boolean isExchangeLogon = exchangeUser.getLogin() != null && !exchangeUser.getLogin().equals("");
                boolean isUserPassRight = true;
                if (isExchangeLogon) {
                    IExchangeService service = (IExchangeService) GlobalServiceRegister.getDefault().getService(
                            IExchangeService.class);
                    if (service.checkUserAndPass(exchangeUser.getUsername(), exchangeUser.getPassword()) != null) {
                        isUserPassRight = false;
                    }
                }

                if (!isExchangeLogon || !isUserPassRight) {
                    if ((count + 1) % 4 == 0) {
                        // if (Platform.getOS().equals(Platform.OS_LINUX)) {
                        // TalendForgeDialog tfDialog = new TalendForgeDialog(this.getShell(), project);
                        // tfDialog.open();
                        // } else {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                String userEmail = null;
                                if (project.getAuthor() != null) {
                                    userEmail = project.getAuthor().getLogin();
                                }
                                TalendForgeDialog tfDialog = new TalendForgeDialog(getShell(), userEmail);
                                tfDialog.setBlockOnOpen(true);
                                tfDialog.open();
                            }

                        });
                    }

                    prefStore.setValue(TalendForgeDialog.LOGINCOUNT, count + 1);
                }
            }
        }

        try {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreTisService.class)) {
                final ICoreTisService service = (ICoreTisService) GlobalServiceRegister.getDefault().getService(
                        ICoreTisService.class);
                if (service != null) {// if in TIS then update the bundle status according to the project type
                    if (!service.validProject(project, needRestartForLocal)) {
                        LoginComposite.isRestart = true;
                        return true;
                    }
                }// else not in TIS so ignor caus we may not have a licence so we do not know which bundles belong to
                 // DI, DQ or MDM
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
            loginComposite.populateProjectList();
            MessageDialog.openError(getShell(), getShell().getText(), e.getMessage());
            return false;
        }

        final Shell shell = this.getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                // monitorWrap = new EventLoopProgressMonitor(monitor);
                try {

                    factory.logOnProject(project, monitor);
                } catch (LoginException e) {
                    throw new InvocationTargetException(e);
                } catch (PersistenceException e) {
                    throw new InvocationTargetException(e);
                } catch (OperationCanceledException e) {
                    throw new InterruptedException(e.getLocalizedMessage());
                }

                monitor.done();
            }
        };

        try {

            dialog.run(true, true, runnable);

        } catch (final InvocationTargetException e) {
            if (PluginChecker.isRemoteProviderPluginLoaded()) {
                loginComposite.populateProjectList();
                if (e.getTargetException() instanceof OperationCancelException) {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openError(Display.getDefault().getActiveShell(),
                                    Messages.getString("LoginDialog.logonCanceled"), e.getTargetException().getLocalizedMessage());
                        }

                    });
                } else {
                    MessageBoxExceptionHandler.process(e.getTargetException(), getShell());
                }
            } else {
                loginComposite.populateTOSProjectList();
                MessageBoxExceptionHandler.process(e.getTargetException(), getShell());
            }
            return false;
        } catch (InterruptedException e) {
            if (PluginChecker.isRemoteProviderPluginLoaded()) {
                loginComposite.populateProjectList();
            } else {
                loginComposite.populateTOSProjectList();
            }
            return false;
        }

        return true;
    }

    public void updateButtons() {
        // loginComposite.fillProjectsBtn.setEnabled(loginComposite.canFinish());
        // getButton(IDialogConstants.OK_ID).setEnabled(loginComposite.canFinish());
    }

    /**
     * Canvas displaying an image.<br/>
     *
     * $Id$
     *
     */
    private class ImageCanvas extends Canvas {

        private Image img;

        public ImageCanvas(Composite parent, ImageDescriptor imgDesc) {
            super(parent, SWT.NONE);

            if (imgDesc != null) {
                img = imgDesc.createImage();
                addPaintListener(new PaintListener() {

                    @Override
                    public void paintControl(PaintEvent e) {
                        e.gc.drawImage(img, 0, 0);
                    }
                });
            }
        }

        /*
         * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
         */
        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            Point size;
            if (img != null) {
                size = new Point(img.getBounds().width, img.getBounds().height);
            } else {
                size = super.computeSize(wHint, hHint, changed);
            }
            return size;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.widgets.Widget#dispose()
         */
        @Override
        public void dispose() {
            if (img != null) {
                img.dispose();
                img = null;
            }

            super.dispose();
        }

    }

}
