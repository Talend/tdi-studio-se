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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.system.EclipseCommandLine;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.workspace.ChooseWorkspaceData;
import org.talend.metadata.managment.ui.model.TableViewerContentProvider;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.ERepositoryImages;
import org.talend.repository.ui.actions.importproject.ImportDemoProjectAction;
import org.talend.repository.ui.actions.importproject.ImportProjectAsAction;
import org.talend.repository.ui.actions.importproject.SelectDeleteProjectDialog;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;
import org.talend.repository.ui.wizards.newproject.NewProjectWizard;

/**
 * DOC Administrator class global comment. Detailled comment
 *
 * @deprecated use LoginProjectPage instead
 */
@Deprecated
public class TOSLoginComposite extends Composite {

    private FormToolkit toolkit;

    private LoginComposite loginComposite;

    private Composite formBody;

    private Composite repositoryComposite;

    private Composite separatorComposite;

    private Composite tosActionComposite;

    private ListViewer projectListViewer;

    private Button openButton;

    private Button deleteButton;

    private Button createButton;

    private Button importButton;

    private Button demoProjectButton;

    private Composite tosWorkspaceComposite;

    private Button changeButton;

    private Hyperlink repositoryHyperlink;

    private Composite tosWelcomeComposite;

    private Composite colorComposite;

    private Label iconLabel;

    private Label onIconLabel;

    private Label statusLabel;

    private Map projectsMap = new HashMap();

    private Button restartBut;

    private LoginDialog dialog;

    private Text workspaceText;

    private ConnectionUserPerReader perReader = null;

    private String oldPath;

    private Map<String, String> convertorMapper = new HashMap<String, String>();

    private static final Image OPEN_IMAGE = ImageProvider.getImage(ERepositoryImages.OPEN_ICON);

    public static final Color WHITE_COLOR = new Color(null, 255, 255, 255);

    public static final Color GREY_COLOR = new Color(null, 215, 215, 215);

    public static final Color YELLOW_GREEN_COLOR = new Color(null, 159, 181, 38);// 143, 163, 35

    public static final Color YELLOW_COLOR = new Color(null, 255, 173, 37);// 254, 182, 84

    private static final Color RED_COLOR = new Color(null, new RGB(240, 0, 0));// 255

    private static final Image LOGIN_CRITICAL_IMAGE = ImageProvider.getImage(ERepositoryImages.LOGIN_CRITICAL_ICON);

    private static final Image LOGIN_CORRECT_IMAGE = ImageProvider.getImage(ERepositoryImages.LOGIN_CORRECT_ICON);

    IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class);

    Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.NONE);// Arial courier

    /**
     * DOC Administrator TOSLoginComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public TOSLoginComposite(Composite parent, int style, LoginComposite loginComposite, LoginDialog dialog) {
        super(parent, style);
        this.loginComposite = loginComposite;
        this.dialog = dialog;

        perReader = ConnectionUserPerReader.getInstance();

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        toolkit = new FormToolkit(this.getDisplay());
        toolkit = new FormToolkit(this.getDisplay());
        Form form = toolkit.createForm(this);
        formBody = form.getBody();
        formBody.setBackgroundMode(SWT.INHERIT_DEFAULT);
        GridData formBodyGd = new GridData(GridData.FILL_BOTH);
        form.setLayoutData(formBodyGd);
        GridLayout createLayout = createLayout(1);
        createLayout.verticalSpacing = 0;
        createLayout.horizontalSpacing = 0;
        createLayout.marginHeight = 0;
        createLayout.marginWidth = 0;
        formBody.setLayout(createLayout);
        createTosRepositoryArea(formBody);
        // createSeparator(formBody);
        createTosActionArea(formBody);
        createTosWorkspaceArea(formBody);
        createStatusArea(formBody);
        addListener();

        try {
            setStatusArea();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        formBody.layout();

    }

    private void createStatusArea(Composite parent) {
        tosWelcomeComposite = toolkit.createComposite(parent);
        tosWelcomeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tosWelcomeComposite.setLayout(new FormLayout());
        tosWelcomeComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);

        FormData formData2 = null;

        colorComposite = toolkit.createComposite(tosWelcomeComposite);

        // display the icon on the colorComposite
        // iconLabel and onIiconLabel both for the display of icon.
        onIconLabel = toolkit.createLabel(colorComposite, null);
        formData2 = new FormData();
        formData2.height = 46;
        formData2.top = new FormAttachment(0, -11);
        formData2.left = new FormAttachment(0, 8);
        formData2.right = new FormAttachment(0, 54);
        onIconLabel.setLayoutData(formData2);

        colorComposite.setLayout(new FormLayout());
        formData2 = new FormData();
        formData2.top = new FormAttachment(0, 11);
        formData2.height = 24;
        formData2.left = new FormAttachment(0);
        formData2.right = new FormAttachment(100);
        colorComposite.setLayoutData(formData2);

        // display the icon under the colorComposite
        iconLabel = toolkit.createLabel(tosWelcomeComposite, null);
        formData2 = new FormData();
        formData2.height = 46;
        formData2.top = new FormAttachment(0, 0);
        formData2.left = new FormAttachment(0, 8);
        formData2.right = new FormAttachment(0, 54);
        iconLabel.setLayoutData(formData2);

        statusLabel = toolkit.createLabel(colorComposite, null);
        formData2 = new FormData();
        formData2.top = new FormAttachment(colorComposite, 4, SWT.CENTER);
        formData2.height = 24;
        formData2.left = new FormAttachment(0, 60);
        formData2.right = new FormAttachment(100, -5);
        statusLabel.setLayoutData(formData2);

        restartBut = toolkit.createButton(tosWelcomeComposite, Messages.getString("LoginComposite.RESTART"), SWT.PUSH); //$NON-NLS-1$
        restartBut.setVisible(false);
        FormData formData = new FormData();
        formData.top = new FormAttachment(colorComposite, 0);// 5, 315
        // formData.left = new FormAttachment(0, 310);
        formData.right = new FormAttachment(100, -5);
        formData.bottom = new FormAttachment(100, 0);
        restartBut.setLayoutData(formData);// new GridData(GridData.FILL_HORIZONTAL)
    }

    private GridLayout createLayout(int numColumns) {
        GridLayout layout = new GridLayout(numColumns, false);
        if (!EnvironmentUtils.isWindowsSystem()) {
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
        }
        return layout;
    }

    private void createTosRepositoryArea(Composite parent) {
        repositoryComposite = toolkit.createComposite(parent);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 40;
        repositoryComposite.setLayoutData(gd);
        repositoryComposite.setLayout(new FormLayout());
        repositoryComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);

        String productName = brandingService.getProductName();
        Label welcomeLabel = toolkit.createLabel(repositoryComposite,
                Messages.getString("TOSLoginComposite.welcomeTitle", productName)); //$NON-NLS-1$
        welcomeLabel.setBackground(repositoryComposite.getBackground());
        welcomeLabel.setFont(font);
        FormData welcomeLabelFormData = new FormData();
        welcomeLabelFormData.top = new FormAttachment(0, 7);
        welcomeLabelFormData.left = new FormAttachment(0, 10);
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            welcomeLabelFormData.right = new FormAttachment(0, 380);
            welcomeLabelFormData.right = new FormAttachment(0, 420);
        } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
            welcomeLabelFormData.right = new FormAttachment(0, 420);
        } else {
            welcomeLabelFormData.right = new FormAttachment(0, 420);
        }
        welcomeLabelFormData.bottom = new FormAttachment(100, 0);
        welcomeLabel.setLayoutData(welcomeLabelFormData);
    }

    private void createSeparator(Composite parent) {
        separatorComposite = toolkit.createComposite(parent);
        separatorComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        separatorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.marginTop = 10;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginBottom = 0;
        separatorComposite.setLayout(layout);
        Label separatorLabel = toolkit.createSeparator(separatorComposite, SWT.HORIZONTAL | SWT.COLOR_DARK_GRAY);
        separatorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // separatorLabel.setBackground(GREY_COLOR);
    }

    private void createTosActionArea(Composite parent) {
        tosActionComposite = toolkit.createComposite(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 140;
        tosActionComposite.setLayoutData(gd);
        tosActionComposite.setLayout(new FormLayout());
        tosActionComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        // tosActionComposite.setBackground(GREY_COLOR);

        FormData data;

        Label projectLabel = toolkit.createLabel(tosActionComposite, Messages.getString("TOSLoginComposite.projectLabel"));
        projectLabel.setFont(font);
        GC gc = new GC(projectLabel);
        Point labelSize = gc.stringExtent(Messages.getString("TOSLoginComposite.projectLabel"));
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(0, 10);
        data.left = new FormAttachment(0, 50);
        // data.right = new FormAttachment(0, 20 + labelSize.x);
        data.bottom = new FormAttachment(0, 30);
        projectLabel.setBackground(tosActionComposite.getBackground());
        projectLabel.setLayoutData(data);

        openButton = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(projectLabel, 0, SWT.TOP);

        gc = new GC(openButton);
        Point labelSizeOpenButton = gc.stringExtent(Messages.getString("TOSLoginComposite.openButton"));
        gc.dispose();
        int width = OPEN_IMAGE.getBounds().width;

        gc = new GC(openButton);
        Point labelSizeDeleteButton = gc.stringExtent(Messages.getString("TOSLoginComposite.deleteButton"));
        gc.dispose();

        if (labelSizeOpenButton.x + width > labelSizeDeleteButton.x) {
            data.left = new FormAttachment(100, -20 - labelSizeOpenButton.x - width - 10);
        } else {
            data.left = new FormAttachment(100, -20 - labelSizeDeleteButton.x - 10);
        }
        data.right = new FormAttachment(100, -10);
        // if (Platform.getOS().equals(Platform.OS_WIN32)) {
        // data.bottom = new FormAttachment(projectLabel, 0, SWT.BOTTOM);
        // } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
        // data.bottom = new FormAttachment(projectLabel, 5, SWT.BOTTOM);
        // } else {
        // data.bottom = new FormAttachment(projectLabel, 5, SWT.BOTTOM);
        // }
        openButton.setText(Messages.getString("TOSLoginComposite.openButton"));
        openButton.setFont(font);
        openButton.setLayoutData(data);
        openButton.setImage(OPEN_IMAGE);

        Composite projectListViewerContainer = new Composite(tosActionComposite, SWT.NONE);
        projectListViewerContainer.setLayout(new FillLayout());
        this.projectListViewer = new ListViewer(projectListViewerContainer);
        this.projectListViewer.setContentProvider(new TableViewerContentProvider());
        this.projectListViewer.setLabelProvider(new ILabelProvider() {

            @Override
            public void removeListener(ILabelProviderListener listener) {
            }

            @Override
            public boolean isLabelProperty(Object element, String property) {

                return false;

            }

            @Override
            public void dispose() {
            }

            @Override
            public void addListener(ILabelProviderListener listener) {
            }

            @Override
            public String getText(Object element) {

                if (element != null) {

                    return element.toString();
                }

                return null;
            }

            @Override
            public Image getImage(Object element) {

                return null;

            }
        });
        this.projectListViewer.setSorter(new ViewerSorter());

        data = new FormData();
        data.top = new FormAttachment(projectLabel, 0, SWT.TOP);
        data.left = new FormAttachment(projectLabel, 10, SWT.RIGHT);
        data.right = new FormAttachment(openButton, -10, SWT.LEFT);
        data.bottom = new FormAttachment(0, 100);
        projectListViewerContainer.setLayoutData(data);

        deleteButton = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(openButton, 5, SWT.BOTTOM);
        data.left = new FormAttachment(openButton, 0, SWT.LEFT);
        data.right = new FormAttachment(openButton, 0, SWT.RIGHT);

        deleteButton.setText(Messages.getString("TOSLoginComposite.deleteButton"));
        deleteButton.setFont(font);
        deleteButton.setLayoutData(data);

        createButton = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(projectListViewerContainer, 10, SWT.BOTTOM);
        data.left = new FormAttachment(projectListViewerContainer, 0, SWT.LEFT);
        data.right = new FormAttachment(projectListViewerContainer, 65, SWT.LEFT);

        createButton.setText(Messages.getString("TOSLoginComposite.createButton"));
        createButton.setFont(font);
        createButton.setLayoutData(data);

        importButton = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(createButton, 0, SWT.TOP);
        data.left = new FormAttachment(createButton, 10, SWT.RIGHT);

        importButton.setText(Messages.getString("TOSLoginComposite.importButton"));
        importButton.setFont(font);
        importButton.setLayoutData(data);

        demoProjectButton = toolkit.createButton(tosActionComposite, null, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(importButton, 0, SWT.TOP);
        data.left = new FormAttachment(importButton, 10, SWT.RIGHT);
        data.right = new FormAttachment(100, -10);

        demoProjectButton.setText(Messages.getString("TOSLoginComposite.demoProjectButton"));
        demoProjectButton.setFont(font);
        demoProjectButton.setLayoutData(data);

        enableOpenAndDelete(false);
    }

    private void createTosWorkspaceArea(Composite parent) {
        tosWorkspaceComposite = toolkit.createComposite(parent);
        tosWorkspaceComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tosWorkspaceComposite.setLayout(new FormLayout());
        tosWorkspaceComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);

        FormData data;

        Label workSpaceLabel = toolkit.createLabel(tosWorkspaceComposite, Messages.getString("TOSLoginComposite.workspaceLabel"));
        workSpaceLabel.setFont(font);
        GC gc = new GC(workSpaceLabel);
        Point labelSize = gc.stringExtent(Messages.getString("TOSLoginComposite.workspaceLabel"));
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(0, 10);
        data.left = new FormAttachment(0, 10);
        data.right = new FormAttachment(0, 10 + labelSize.x);
        data.bottom = new FormAttachment(0, 30);
        workSpaceLabel.setLayoutData(data);

        changeButton = toolkit.createButton(tosWorkspaceComposite, null, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(workSpaceLabel, 0, SWT.TOP);
        data.left = new FormAttachment(100, -75);
        data.right = new FormAttachment(100, -10);
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            data.bottom = new FormAttachment(workSpaceLabel, 0, SWT.BOTTOM);
        } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
            data.bottom = new FormAttachment(workSpaceLabel, 5, SWT.BOTTOM);
        } else {
            data.bottom = new FormAttachment(workSpaceLabel, 5, SWT.BOTTOM);
        }
        changeButton.setText(Messages.getString("TOSLoginComposite.changeButton"));
        changeButton.setFont(font);
        changeButton.setLayoutData(data);

        workspaceText = toolkit.createText(tosWorkspaceComposite, null, SWT.READ_ONLY | SWT.BORDER);
        workspaceText.setFont(font);
        workspaceText.setBackground(GREY_COLOR);
        workspaceText.setText(loginComposite.getConnection().getWorkSpace());
        oldPath = loginComposite.getConnection().getWorkSpace();
        data = new FormData();
        data.width = 200;
        data.top = new FormAttachment(workSpaceLabel, 0, SWT.TOP);
        data.left = new FormAttachment(workSpaceLabel, 10, SWT.RIGHT);
        data.right = new FormAttachment(changeButton, -10, SWT.LEFT);
        data.bottom = new FormAttachment(changeButton, 0, SWT.BOTTOM);
        workspaceText.setLayoutData(data);

    }

    public Project[] readProject() {
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

    private void addListener() {
        createButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Project project = null;
                ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
                NewProjectWizard newPrjWiz = new NewProjectWizard(null);
                WizardDialog newProjectDialog = new WizardDialog(getShell(), newPrjWiz);
                newProjectDialog.setTitle(Messages.getString("LoginDialog.newProjectTitle")); //$NON-NLS-1$
                if (newProjectDialog.open() == Window.OK) {
                    project = newPrjWiz.getProject();
                    refresh();
                    ArrayList<String> allProjects = (ArrayList<String>) projectListViewer.getInput();
                    int index = 0;
                    Collections.sort(allProjects);
                    for (int i = 0; i < allProjects.size(); i++) {
                        String projectName = allProjects.get(i);
                        if (project.getLabel().equals(projectName)) {
                            index = i;
                            break;
                        }
                    }
                    projectListViewer.getList().select(index);
                    projectListViewer.refresh();
                }
            }
        });

        deleteButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell activeShell = Display.getCurrent().getActiveShell();
                SelectDeleteProjectDialog dialog = new SelectDeleteProjectDialog(activeShell, true);
                if (dialog.open() == Dialog.OK) {
                    CorePlugin.getDefault().getRepositoryLocalProviderService().resetXmiResourceSet();
                    java.util.List<Object> delList = dialog.getDelList();
                    if (delList.size() != 0) {
                        for (Object obj : delList) {
                            if (obj instanceof IProject) {
                                IProject p = (IProject) obj;
                                if (projectsMap.containsKey(p.getName())) {
                                    projectsMap.remove(p.getName());
                                    String name = convertorMapper.get(p.getName());
                                    if (name != null) {
                                        convertorMapper.remove(p.getName());
                                        TOSLoginComposite.this.projectListViewer.getList().remove(name);
                                    }
                                    if (TOSLoginComposite.this.projectListViewer.getList().getItemCount() == 0) {
                                        enableOpenAndDelete(false);
                                    } else if (TOSLoginComposite.this.projectListViewer.getSelection().isEmpty()) {
                                        TOSLoginComposite.this.projectListViewer.getList().select(0);
                                    }
                                    try {
                                        setStatusArea();
                                    } catch (PersistenceException e1) {
                                        ExceptionHandler.process(e1);
                                    }
                                }
                            }
                        }
                    }
                }
                refresh();
            }
        });

        importButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ImportDemoProjectAction.getInstance().setShell(getShell());
                ImportProjectAsAction.getInstance().run();
                String newProject = ImportProjectAsAction.getInstance().getProjectName();
                if (newProject != null) {
                    ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
                    Project[] projects = null;
                    try {
                        projects = repositoryFactory.readProject();
                    } catch (PersistenceException e1) {
                        e1.printStackTrace();
                    } catch (BusinessException e1) {
                        e1.printStackTrace();
                    }
                    if (!projectsMap.containsKey(newProject.toUpperCase())) {

                        for (Project project : projects) {
                            if (project.getLabel().toUpperCase().equals(newProject.toUpperCase())) {
                                projectsMap.put(newProject.toUpperCase(), project);
                                convertorMapper.put(newProject.toUpperCase(), newProject);

                                enableOpenAndDelete(true);
                                try {
                                    setStatusArea();
                                } catch (PersistenceException e1) {
                                    ExceptionHandler.process(e1);
                                }
                            }
                        }

                        TOSLoginComposite.this.projectListViewer.setInput(new ArrayList(convertorMapper.values()));
                    }
                }
                try {
                    IRunnableWithProgress op = new IRunnableWithProgress() {

                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            try {
                                ProxyRepositoryFactory.getInstance().initialize();
                            } catch (PersistenceException e) {
                                throw new InvocationTargetException(e);
                            }
                        }
                    };
                    new ProgressMonitorDialog(getShell()).run(true, false, op);
                    refresh();
                } catch (InvocationTargetException e1) {
                    e1.getTargetException();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
        changeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dirDialog = new DirectoryDialog(dialog.getShell());
                String path = dirDialog.open();
                if (path == null || "".equals(path)) { //$NON-NLS-1$
                    workspaceText.setText(getRecentWorkSpace());
                    loginComposite.getConnection().setWorkSpace(getRecentWorkSpace());
                } else {
                    workspaceText.setText(path);
                    loginComposite.getConnection().setWorkSpace(path);
                    if (!path.equals(oldPath)) {
                        oldPath = path;
                        restartBut.setVisible(true);
                        openButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                        createButton.setEnabled(false);
                        importButton.setEnabled(false);
                        demoProjectButton.setEnabled(false);
                        changeButton.setEnabled(false);
                    }
                }
                java.util.List<ConnectionBean> list = new ArrayList<ConnectionBean>();
                list.add(loginComposite.getConnection());
                loginComposite.storedConnections = list;
                perReader.saveConnections(loginComposite.storedConnections);

                if (!loginComposite.isWorkSpaceSame()) {
                    try {
                        setStatusArea();
                    } catch (PersistenceException e1) {
                        ExceptionHandler.process(e1);
                    }
                }

            }
        });
        restartBut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                LoginComposite.isRestart = true;
                ConnectionBean connection = loginComposite.getConnection();
                perReader.saveLastConnectionBean(connection);
                // update the restart command line to specify the workspace to launch
                // if relaunch, should delete the "disableLoginDialog" argument in eclipse data for bug TDI-19214
                EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand("-data", connection.getWorkSpace(), false); //$NON-NLS-1$
                // store the workspace in the eclipse history so that it is rememebered on next studio launch
                ChooseWorkspaceData workspaceData = new ChooseWorkspaceData(""); //$NON-NLS-1$
                workspaceData.workspaceSelected(connection.getWorkSpace());
                workspaceData.writePersistedData();
                dialog.okPressed();
            }
        });
        openButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                loginComposite.setRepositoryContextInContext();
                if (!TOSLoginComposite.this.projectListViewer.getSelection().isEmpty()) {
                    String selection = TOSLoginComposite.this.projectListViewer.getList().getSelection()[0];
                    if (selection != null && !selection.equals("")) {
                        Project project = (Project) projectsMap.get(selection.toUpperCase());
                        boolean flag = dialog.logIn(project);
                        if (flag) {
                            dialog.okPressed();
                        }
                    }
                }
            }
        });
        demoProjectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ImportDemoProjectAction action = ImportDemoProjectAction.getInstance();
                action.setShell(getShell());
                action.run();
                refresh();
            }
        });
    }

    private void refresh() {
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Project[] projects = null;
        try {
            projects = repositoryFactory.readProject();
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        } catch (BusinessException e1) {
            ExceptionHandler.process(e1);
        }

        this.projectListViewer.getList().removeAll();

        projectsMap.clear();
        convertorMapper.clear();
        if (projects != null) {
            for (Project pro : projects) {
                convertorMapper.put(pro.getTechnicalLabel(), pro.getLabel());
                projectsMap.put(pro.getTechnicalLabel(), pro);
                enableOpenAndDelete(true);
            }

            this.projectListViewer.setInput(new ArrayList(convertorMapper.values()));
        }

        try {
            setStatusArea();
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
    }

    private String getRecentWorkSpace() {
        String filePath = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
        return filePath;
    }

    public ListViewer getProjectListViewer() {

        return this.projectListViewer;

    }

    public Map getProjectMap() {
        return projectsMap;
    }

    public Map<String, String> getConvertorMapper() {

        return this.convertorMapper;

    }

    public void setStatusArea() throws PersistenceException {
        String productName = brandingService.getFullProductName();
        if (productName != null) {
            String[] split = productName.split(" "); //$NON-NLS-1$
            if (split != null && split.length > 3) {
                productName = brandingService.getShortProductName();
            }
        }

        if (loginComposite.getConnection() != null) {
            if (!loginComposite.isWorkSpaceSame()) {
                iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                colorComposite.setBackground(RED_COLOR);
                onIconLabel.setBackground(colorComposite.getBackground());
                statusLabel.setText(Messages.getString("LoginComposite.DIFFERENT_WORKSPACES")); //$NON-NLS-1$
                statusLabel.setBackground(RED_COLOR);
                statusLabel.setForeground(WHITE_COLOR);
                Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                statusLabel.setFont(font);
            } else if (this.projectListViewer.getList().getItemCount() > 0) {
                iconLabel.setImage(LOGIN_CORRECT_IMAGE);
                onIconLabel.setImage(LOGIN_CORRECT_IMAGE);
                colorComposite.setBackground(YELLOW_GREEN_COLOR);
                onIconLabel.setBackground(colorComposite.getBackground());
                statusLabel.setText(Messages.getString("LoginComposite.TisWorkspace_welcome", productName)); //$NON-NLS-1$
                // int size = loginComposite.calStatusLabelFont(11, statusLabel.getText());
                statusLabel.setBackground(YELLOW_GREEN_COLOR);
                statusLabel.setForeground(WHITE_COLOR);
                Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                statusLabel.setFont(font);
            } else {
                iconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                onIconLabel.setImage(LOGIN_CRITICAL_IMAGE);
                colorComposite.setBackground(RED_COLOR);
                onIconLabel.setBackground(colorComposite.getBackground());
                statusLabel.setText(Messages.getString("LoginComposite.PROJECT_NEED")); //$NON-NLS-1$
                statusLabel.setBackground(RED_COLOR);
                statusLabel.setForeground(WHITE_COLOR);
                Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.BOLD);// Arial courier
                statusLabel.setFont(font);
            }
        }
    }

    public void enableOpenAndDelete(boolean enable) {
        openButton.setEnabled(enable);
        deleteButton.setEnabled(enable);
    }
}
