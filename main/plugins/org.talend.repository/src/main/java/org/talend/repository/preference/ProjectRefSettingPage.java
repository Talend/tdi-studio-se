package org.talend.repository.preference;

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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.projectsetting.IProjectSettingPreferenceConstants;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.core.services.IGITProviderService;
import org.talend.core.services.ISVNProviderService;
import org.talend.repository.ProjectManager;
import org.talend.repository.ReferenceProjectProblemManager;
import org.talend.repository.ReferenceProjectProvider;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.ui.dialog.AProgressMonitorDialogWithCancel;
import org.talend.repository.ui.dialog.OverTimePopupDialogTask;
import org.talend.repository.ui.dialog.ProjectSettingsPreferenceDialog;

public class ProjectRefSettingPage extends ProjectSettingPage {

    private static Logger log = Logger.getLogger(ProjectRefSettingPage.class);

    public static final String ID = "org.talend.repository.preference.ProjectRefSettingPage"; //$NON-NLS-1$

    public static final String START_BROWSER_ID = "org.talend.rcp.intro.starting.StartingBrowser"; //$NON-NLS-1$

    private static final int REPOSITORY_LOCAL = 0;

    private static final int REPOSITORY_GIT = 1;

    private static final int REPOSITORY_SVN = 2;

    private ListViewer viewer;

    private Combo projectCombo;

    private Combo branchCombo;

    private Button removeButton;

    private Button addButton;
    
    private Button strictButton;

    private Project[] allProjects;

    private Project lastSelectedProject;

    private ISVNProviderService svnProviderService;

    private IGITProviderService gitProviderService;

    private List<ProjectReferenceBean> originInput = new ArrayList<ProjectReferenceBean>();

    private static List<ProjectReferenceBean> viewerInput = new ArrayList<ProjectReferenceBean>();

    private boolean isReadOnly = false;

    private Exception errorException;

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
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
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        if (this.getRepositoryContext().isOffline() || ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()
                || currentProject.isLocal()) {
            isReadOnly = true;
        }
        if (!isReadOnly) {
            String branchSelection = ProjectManager.getInstance()
                    .getMainProjectBranch(ProjectManager.getInstance().getCurrentProject());
            if (branchSelection.startsWith(ProjectManager.NAME_TAGS + ProjectManager.SEP_CHAR)) {
                isReadOnly = true;
            }
        }
        Composite composite = new Composite(parent, SWT.None);
        composite.setLayout(new GridLayout(2, false));
        GridData data = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(data);

        SashForm form = new SashForm(composite, SWT.HORIZONTAL);
        form.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group listGroup = new Group(form, SWT.None);
        listGroup.setLayout(new GridLayout(2, false));
        listGroup.setText(Messages.getString("ReferenceProjectSetupPage.ListGroup"));//$NON-NLS-1$

        viewer = new ListViewer(listGroup, SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setLabelProvider(new ReferenceProjectLabelProvider());
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        GridData viewerData = new GridData(GridData.FILL_BOTH);
        viewerData.horizontalSpan = 2;
        viewer.getControl().setLayoutData(viewerData);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setErrorMessage(null);
                ISelection selection = viewer.getSelection();
                removeButton.setEnabled(!selection.isEmpty() && !isReadOnly);
            }
        });

        removeButton = new Button(listGroup, SWT.None);
        removeButton.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
        removeButton.setToolTipText(Messages.getString("ReferenceProjectSetupPage.ButtonTooltipDelete")); //$NON-NLS-1$
        GridData removeButtonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        removeButtonData.horizontalSpan = 2;
        removeButton.setLayoutData(removeButtonData);
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                removeProjectReference();
            }
        });
        
        Group addGroup = new Group(form, SWT.None);
        addGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        addGroup.setLayout(new GridLayout(1, false));
        addGroup.setText(Messages.getString("ReferenceProjectSetupPage.AddGroup"));//$NON-NLS-1$
        
        Composite addCom = new Composite(addGroup, SWT.NULL);
        addCom.setLayout(new GridLayout(3, false));
        addCom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label projectLabel = new Label(addCom, SWT.None);
        projectLabel.setLayoutData(new GridData());
        projectLabel.setText(Messages.getString("ReferenceProjectSetupPage.LabelProject"));//$NON-NLS-1$

        projectCombo = new Combo(addCom, SWT.BORDER);
        projectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectCombo.setEnabled(false);
        projectCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                Project project = getCurrentSelectedProject();
                if (lastSelectedProject == null
                        || (project != null && !project.getTechnicalLabel().equals(lastSelectedProject.getTechnicalLabel()))) {
                    lastSelectedProject = project;
                    initBranchData();
                }
                updateAddButtonStatus();
            }
        });
        Button refreshButton = new Button(addCom, SWT.None);
        refreshButton.setLayoutData(new GridData());
        refreshButton.setEnabled(!isReadOnly);
        refreshButton.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        refreshButton.setToolTipText(Messages.getString("ReferenceProjectSetupPage.ButtonTooltipRefresh"));//$NON-NLS-1$
        refreshButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                initProjectData();
                lastSelectedProject = null;
            }
        });

        Label branchLabel = new Label(addCom, SWT.None);
        branchLabel.setLayoutData(new GridData());
        branchLabel.setText(Messages.getString("ReferenceProjectSetupPage.LabelBranch"));//$NON-NLS-1$

        branchCombo = new Combo(addCom, SWT.BORDER);
        branchCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        branchCombo.setEnabled(false);
        branchCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                updateAddButtonStatus();
            }
        });

        addButton = new Button(addCom, SWT.None);
        addButton.setLayoutData(new GridData());
        addButton.setImage(ImageProvider.getImage(EImage.ADD_ICON));
        addButton.setToolTipText(Messages.getString("ReferenceProjectSetupPage.ButtonTooltipAdd")); //$NON-NLS-1$
        addButton.setEnabled(false);
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addProjectReference();
            }
        });
        
        Composite strCom = new Composite(addGroup, SWT.NULL);
        GridLayout layout = new GridLayout(1, false);
        strCom.setLayout(layout);
        
        strictButton = new Button(strCom, SWT.CHECK);
        strictButton.setLayoutData(new GridData());
        strictButton.setText(Messages.getString("ReferenceProjectSetupPage.ButtonTooltipStrict")); //$NON-NLS-1$
        boolean strict = false;
        ProjectPreferenceManager projectPreferenceManager = CoreRuntimePlugin.getInstance()
                .getProjectPreferenceManager();
        if(projectPreferenceManager != null) {
            strict = projectPreferenceManager.getBoolean(
                    IProjectSettingPreferenceConstants.USE_STRICT_REFERENCE_JOBLET);
        }
        strictButton.setSelection(strict);

        form.setWeights(new int[] { 1, 1 });

        applyDialogFont(composite);
        initViewerData();
        if (isReadOnly) {
            this.setTitle(Messages.getString("ReferenceProjectSetupPage.TitleReadOnly")); //$NON-NLS-1$
        } else {
            this.setTitle(Messages.getString("ReferenceProjectSetupPage.Title")); //$NON-NLS-1$
        }

        this.checkInvalidProject();
        return null;
    }

    @Override
    public void refresh() {
    }

    private void initViewerData() {
        if (!ProjectSettingsPreferenceDialog.isInReopen()) {
            Project project = ProjectManager.getInstance().getCurrentProject();
            originInput.addAll(getReferenceProjectData(project));
            viewerInput.clear();
            viewerInput.addAll(originInput);
        }
        viewer.setInput(viewerInput);
        viewer.refresh();
    }

    private List<ProjectReferenceBean> getReferenceProjectData(Project project) {
        List<ProjectReferenceBean> result = new ArrayList<ProjectReferenceBean>();
        List<ProjectReference> list = project.getProjectReferenceList();
        for (ProjectReference pr : list) {
            ProjectReferenceBean prb = new ProjectReferenceBean();
            prb.setReferenceProjectLabel(pr.getReferencedProject().getTechnicalLabel());
            prb.setReferenceProject(pr.getReferencedProject());
            prb.setReferenceBranch(pr.getReferencedBranch());
            result.add(prb);
        }
        List<String> invalidProjectLabelList = ReferenceProjectProblemManager.getInstance()
                .getInvalidProjectReferenceList(ProjectManager.getInstance().getCurrentProject().getTechnicalLabel());
        if (invalidProjectLabelList.size() > 0) {
            for (String invalidLabel : invalidProjectLabelList) {
                ProjectReferenceBean prb = new ProjectReferenceBean();
                prb.setReferenceProjectLabel(invalidLabel);
                prb.setReferenceBranch("");
                prb.setValid(false);
                result.add(prb);
            }
        }
        return result;
    }

    private void initProjectData() {
        this.setErrorMessage(null);
        projectCombo.setEnabled(false);
        projectCombo.setItems(new String[0]);
        String errorMessage = null;
        OverTimePopupDialogTask<Project[]> overTimePopupDialogTask = new OverTimePopupDialogTask<Project[]>() {

            @Override
            public Project[] run() throws Throwable {
                return ProxyRepositoryFactory.getInstance().readProject();
            }
        };
        overTimePopupDialogTask.setNeedWaitingProgressJob(false);
        try {
            allProjects = overTimePopupDialogTask.runTask();
        } catch (Throwable e) {
            errorMessage = e.getLocalizedMessage();
        }
        if (allProjects != null && allProjects.length > 0) {
            Project currentProject = ProjectManager.getInstance().getCurrentProject();
            int projectRepositoryType = getProjectRepositoryType(currentProject);
            List<String> itemList = new ArrayList<String>();
            for (int i = 0; i < allProjects.length; i++) {
                if (currentProject.getTechnicalLabel().equals(allProjects[i].getTechnicalLabel())) {
                    continue;
                }
                if (projectRepositoryType == getProjectRepositoryType(allProjects[i])) {
                    itemList.add(allProjects[i].getTechnicalLabel());
                }
            }
            projectCombo.setItems(itemList.toArray(new String[0]));
        }
        if (projectCombo.getItemCount() == 0) {
            projectCombo.setItems(new String[] { Messages.getString("ReferenceProjectSetupPage.lblNoAvailableProject") });
            projectCombo.select(0);
            projectCombo.setEnabled(false);
        } else {
            projectCombo.setEnabled(true);
        }
        this.setErrorMessage(errorMessage);
    }

    /**
     * Get project repository type
     *
     * @param project
     */
    private int getProjectRepositoryType(Project project) {
        try {
            if (!project.isLocal()) {
                if (gitProviderService != null && gitProviderService.isGITProject(project)) {
                    return REPOSITORY_GIT;
                }
            }
            if (svnProviderService != null && svnProviderService.isSVNProject(project)) {
                return REPOSITORY_SVN;
            }
        } catch (PersistenceException ex) {
            ExceptionHandler.process(ex);
        }

        return REPOSITORY_LOCAL;
    }

    private void updateAddButtonStatus() {
        boolean isEnable = false;
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        int projectRepositoryType = getProjectRepositoryType(currentProject);
        if (REPOSITORY_LOCAL == projectRepositoryType) {
            if (StringUtils.isNotEmpty(projectCombo.getText())) {
                isEnable = true;
            }
        } else {
            if (StringUtils.isNotEmpty(projectCombo.getText()) && StringUtils.isNotEmpty(branchCombo.getText())) {
                isEnable = true;
            }
        }
        Project p = getCurrentSelectedProject();
        if (p != null) {
            for (ProjectReferenceBean bean : viewerInput) {
                if (bean.getReferenceProjectLabel().equals(p.getTechnicalLabel())) {
                    isEnable = false;
                    break;
                }
            }
        } else {
            isEnable = false;
        }

        addButton.setEnabled(isEnable && !isReadOnly);
    }

    private void initBranchData() {
        this.setErrorMessage(null);
        branchCombo.setEnabled(false);
        branchCombo.setItems(new String[0]);
        String errorMessage = null;
        List<String> allBranch;

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        int projectRepositoryType = getProjectRepositoryType(currentProject);
        if (REPOSITORY_LOCAL == projectRepositoryType) {
            return;
        }
        if (projectRepositoryType == REPOSITORY_SVN && this.getRepositoryContext().isOffline()) {
            this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorCanNotGetSVNBranchData")); //$NON-NLS-1$
            return;
        }
        OverTimePopupDialogTask<List<String>> overTimePopupDialogTask = new OverTimePopupDialogTask<List<String>>() {

            @Override
            public List<String> run() throws Throwable {
                IRepositoryService repositoryService = (IRepositoryService) GlobalServiceRegister.getDefault()
                        .getService(IRepositoryService.class);
                if (repositoryService != null) {
                    return repositoryService.getProjectBranch(lastSelectedProject, false);
                }
                return null;
            }
        };
        overTimePopupDialogTask.setNeedWaitingProgressJob(false);
        try {
            allBranch = overTimePopupDialogTask.runTask();
            if (allBranch != null) {
                branchCombo.setItems(allBranch.toArray(new String[0]));
            }
            if (projectRepositoryType == REPOSITORY_SVN) {
                if (!allBranch.contains(SVNConstant.NAME_TRUNK)) {
                    allBranch.add(SVNConstant.NAME_TRUNK);
                }
                branchCombo.setItems(allBranch.toArray(new String[0]));
                branchCombo.setText(SVNConstant.NAME_TRUNK);
            } else if (projectRepositoryType == REPOSITORY_GIT) {
                branchCombo.setItems(allBranch.toArray(new String[0]));
                String branch = null;
                if (allBranch.contains(SVNConstant.NAME_MAIN)) {
                    branch = SVNConstant.NAME_MAIN;
                } else if (allBranch.contains(SVNConstant.NAME_MASTER)) {
                    branch = SVNConstant.NAME_MASTER;
                } else if (0 < allBranch.size()) {
                    branch = allBranch.get(0);
                }
                if (StringUtils.isNotBlank(branch)) {
                    branchCombo.setText(branch);
                }
            }
        } catch (Throwable e) {
            errorMessage = e.getLocalizedMessage();
        }
        branchCombo.setEnabled(true);
        this.setErrorMessage(errorMessage);
    }

    private RepositoryContext getRepositoryContext() {
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext()
                .getProperty(Context.REPOSITORY_CONTEXT_KEY);
        return repositoryContext;
    }

    protected Project getCurrentSelectedProject() {
        String label = projectCombo.getText();
        if (allProjects != null) {
            for (Project project : allProjects) {
                if (label.equals(project.getTechnicalLabel())) {
                    return project;
                }
            }
        }
        return null;
    }

    private void addProjectReference() {
        this.setErrorMessage(null);
        Project p = getCurrentSelectedProject();
        if (p != null) {
            int projectRepositoryType = this.getProjectRepositoryType(p);
            String branch = ""; //$NON-NLS-1$
            if (REPOSITORY_LOCAL != projectRepositoryType) {
                branch = branchCombo.getText();
                if (branch.length() == 0) {
                    this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorBranchEmpty"));//$NON-NLS-1$
                    return;
                }
            }
            for (ProjectReferenceBean bean : viewerInput) {
                if (bean.getReferenceProjectLabel().equals(p.getTechnicalLabel())) {
                    this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorContainedProject"));//$NON-NLS-1$
                    return;
                }
            }

            ProjectReferenceBean referenceBean = new ProjectReferenceBean();
            referenceBean.setReferenceProjectLabel(p.getEmfProject().getTechnicalLabel());
            referenceBean.setReferenceProject(p.getEmfProject());
            referenceBean.setReferenceBranch(branch);
            viewerInput.add(referenceBean);
            viewer.refresh();
        }
    }

    /**
     *
     * @param projectRefMap
     * @return @return false- cycle reference exist otherwise true
     * @throws PersistenceException
     * @throws InvalidProjectException
     */
    private boolean checkCycleReference(Map<String, List<ProjectReference>> projectRefMap)
            throws PersistenceException, BusinessException {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        List<ProjectReference> referenceList = new ArrayList<ProjectReference>();
        for (ProjectReferenceBean bean : viewerInput) {
            referenceList.add(getProjectReferenceInstance(bean));
        }
        if (referenceList.size() == 0) {
            return true;
        }
        projectRefMap.put(currentProject.getTechnicalLabel(), referenceList);
        for (ProjectReference projetReference : referenceList) {
            List<ProjectReference> childReferenceList = ReferenceProjectProblemManager.getAllReferenceProject(projetReference,
                    projectRefMap, new HashSet<String>(), true);
            projectRefMap.put(projetReference.getReferencedProject().getTechnicalLabel(), childReferenceList);
        }
        return ReferenceProjectProblemManager.checkCycleReference(projectRefMap);
    }

    private ProjectReference getProjectReferenceInstance(ProjectReferenceBean bean) throws PersistenceException {
        ProjectReference pr = PropertiesFactory.eINSTANCE.createProjectReference();
        pr.setReferencedBranch(bean.getReferenceBranch());
        pr.setReferencedProject(bean.getReferenceProject());
        return pr;
    }

    private void removeProjectReference() {
        ISelection selection = viewer.getSelection();
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            viewerInput.removeAll(((IStructuredSelection) selection).toList());
            viewer.refresh();
            updateAddButtonStatus();
        }
    }

    protected static String getProjectDecription(String projectName, String branch) {
        StringBuffer sb = new StringBuffer();
        sb.append(projectName);
        if (branch != null && branch.trim().length() > 0) {
            sb.append("/").append(branch); //$NON-NLS-1$
        }
        return sb.toString();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean performOk() {
        if (!this.isControlCreated()) {
            return true;
        }
        if (!checkInvalidProject()) {
            return false;
        }
        if (isModified()) {
            if (!validate()) {
                return false;
            }
            
            if (checkOtherEditorsOpened()) {
                return false;
            }

            MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                    Messages.getString("RepoReferenceProjectSetupAction.TitleReferenceChanged"), //$NON-NLS-1$
                    Messages.getString("RepoReferenceProjectSetupAction.MsgReferenceChanged")); //$NON-NLS-1$
            // Apply new setting
            errorException = null;
            RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit(
                    Messages.getString("ReferenceProjectSetupPage.TaskApplyReferenceSetting")) { //$NON-NLS-1$

                public void run() throws PersistenceException {
                    IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {

                        final String mainProjectLabel = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();

                        @Override
                        public void run(IProgressMonitor monitor) throws CoreException {
                            try {
                                relogin(mainProjectLabel, false, monitor);
                                saveData();
                            } catch (Exception ex) {
                                errorException = ex;
                                synSetErrorMessage(errorException);
                                // If failed, try to roll back
                                try {
                                    relogin(mainProjectLabel, true, monitor);
                                } catch (Exception e) {
                                    ExceptionHandler.process(e);
                                    log.error("Roll back reference project settings failed:" + e); //$NON-NLS-1$
                                }
                            }
                        }
                    };

                    IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            try {
                                final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                                ISchedulingRule schedulingRule = workspace.getRoot();
                                workspace.run(workspaceRunnable, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                            } catch (CoreException e) {
                                throw new InvocationTargetException(e);
                            }
                        }
                    };
                    ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
                    try {
                        progressDialog.run(true, false, iRunnableWithProgress);
                    } catch (InvocationTargetException | InterruptedException ex) {
                        errorException = ex;
                    }
                }
            };
            repositoryWorkUnit.setAvoidUnloadResources(true);
            repositoryWorkUnit.setUnloadResourcesAfterRun(true);
            repositoryWorkUnit.setForceTransaction(true);
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
        }
        
        ProjectPreferenceManager projectPreferenceManager = CoreRuntimePlugin.getInstance()
                .getProjectPreferenceManager();
        if(projectPreferenceManager != null && strictButton != null) {
            projectPreferenceManager.setValue(IProjectSettingPreferenceConstants.USE_STRICT_REFERENCE_JOBLET, 
                    strictButton.getSelection());
            projectPreferenceManager.save();
        }
        
        if (errorException != null) {
            return false;
        }

        return super.performOk();
    }

    private boolean checkOtherEditorsOpened() {
        IWorkbenchWindow workBench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workBench != null) {
            IWorkbenchPage page = workBench.getActivePage();
            IEditorReference[] editors = page.getEditorReferences();
            boolean isStartBrowser = false;
            if (editors.length > 0) {
                if (editors.length == 1 && START_BROWSER_ID.equals(editors[0].getId())) {
                    isStartBrowser = true;
                    return false;
                }
                if (!isStartBrowser) {
                    for (IEditorReference editor : editors) {
                        if (editor.isDirty()) {
                            setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorEditorsNotSaved"));
                            return true;
                        }
                    }
                    page.closeAllEditors(true);
                }
            }
        }
        return false;
    }

    private boolean checkInvalidProject() {
        this.setErrorMessage(null);
        StringBuffer sb = new StringBuffer();
        for (ProjectReferenceBean bean : viewerInput) {
            if (!bean.isValid()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(bean.getReferenceProjectLabel());
            }
        }
        if (sb.length() > 0) {
            setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorMissingReferencedProject", sb.toString()));
            return false;
        }
        return true;
    }

    private boolean validate() {
        this.setErrorMessage(null);
        Map<String, List<ProjectReference>> projectRefMap = new HashMap<String, List<ProjectReference>>();
        AProgressMonitorDialogWithCancel<Boolean> dialog = new AProgressMonitorDialogWithCancel<Boolean>(
                Display.getCurrent().getActiveShell()) {

            protected Boolean runWithCancel(IProgressMonitor monitor) throws Throwable {
                return checkCycleReference(projectRefMap);
            }
        };
        try {
            dialog.run(Messages.getString("ReferenceProjectSetupPage.TaskCheckCycleReference"), null, true, //$NON-NLS-1$
                    AProgressMonitorDialogWithCancel.ENDLESS_WAIT_TIME);
        } catch (Exception e) {
            setErrorMessage(e.getMessage());
            return false;
        }
        if (dialog.getExecuteException() != null) {
            setErrorMessage(dialog.getExecuteException().getMessage());
            return false;
        }
        if (!(Boolean) dialog.getExecuteResult()) {
            this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorCycleReference"));//$NON-NLS-1$
            return false;
        }

        try {
            ReferenceProjectProblemManager.checkMoreThanOneBranch(projectRefMap);
        } catch (BusinessException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean isModified() {
        if (originInput.size() != viewerInput.size()) {
            return true;
        }
        for (ProjectReferenceBean originValue : originInput) {
            boolean isFind = false;
            for (ProjectReferenceBean value : viewerInput) {
                if (originValue.getReferenceProjectLabel().equals(value.getReferenceProjectLabel())
                        && originValue.getReferenceBranch().equals(value.getReferenceBranch())) {
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                return true;
            }
        }
        return false;
    }

    private List<ProjectReference> convertToProjectReference(List<ProjectReferenceBean> input) throws PersistenceException {
        List<ProjectReference> output = new ArrayList<ProjectReference>();
        for (ProjectReferenceBean bean : input) {
            ProjectReference pr = PropertiesFactory.eINSTANCE.createProjectReference();
            pr.setReferencedBranch(bean.getReferenceBranch());
            pr.setReferencedProject(bean.getReferenceProject());
            output.add(pr);
        }
        return output;
    }

    private void saveData() {
        try {
            ProjectManager.getInstance().getCurrentProject().saveProjectReferenceList(convertToProjectReference(viewerInput));
            ReferenceProjectProvider.removeAllTempReferenceList();
        } catch (Exception e) {
            errorException = e;
            String errorMessage = Messages.getString("ReferenceProjectSetupPage.ErrorSaveReferenceSettingFailed", //$NON-NLS-1$
                    e.getLocalizedMessage());
            synSetErrorMessage(errorMessage);
            log.error("Save reference project settings failed:" + e);//$NON-NLS-1$
        }
    }

    private void relogin(String mainProjectLabel, boolean isRollback, IProgressMonitor monitor)
            throws PersistenceException, BusinessException {
        monitor.beginTask(Messages.getString("RepoReferenceProjectSetupAction.TaskRelogin"), 10); //$NON-NLS-1$
        monitor.subTask(Messages.getString("RepoReferenceProjectSetupAction.TaskLogoff")); //$NON-NLS-1$
        ProxyRepositoryFactory.getInstance().logOffProject();
        monitor.worked(2);
        Project[] projects;
        Project switchProject = null;
        projects = ProxyRepositoryFactory.getInstance().readProject();
        for (Project p : projects) {
            if (p.getTechnicalLabel().equals(mainProjectLabel)) {
                switchProject = p;
                break;
            }
        }
        if (isRollback) {
            ReferenceProjectProvider.removeAllTempReferenceList();
        } else {
            List<ProjectReference> newReferenceSetting = convertToProjectReference(viewerInput);
            ReferenceProjectProvider.setTempReferenceList(ProjectManager.getInstance().getCurrentProject().getTechnicalLabel(),
                    newReferenceSetting);
        }
        monitor.subTask(Messages.getString("RepoReferenceProjectSetupAction.TaskLogon", switchProject.getLabel())); //$NON-NLS-1$
        ProxyRepositoryFactory.getInstance().logOnProject(switchProject, monitor);
        monitor.worked(7);
        refreshNavigatorView();
        monitor.worked(1);
        monitor.done();
    }

    private void synSetErrorMessage(String message) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                setErrorMessage(message == null ? "" : message);
            }
        });
    }

    private void synSetErrorMessage(Exception ex) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                String message = null;
                if (ex instanceof PersistenceException) {
                    PersistenceException pEx = (PersistenceException) ex;
                    if (pEx.getCause() instanceof BusinessException) {
                        BusinessException bEx = (BusinessException) pEx.getCause();
                        message = bEx.getKey();
                    }
                }
                if (message == null || message.length() == 0) {
                    message = ex.getMessage();
                }
                synSetErrorMessage(message);
            }
        });
    }

    private void refreshNavigatorView() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                ProjectRepositoryNode.getInstance().cleanup();
                IViewPart repositoryView = RepositoryManagerHelper.findRepositoryView();
                if (repositoryView instanceof CommonNavigator) {
                    CommonViewer commonViewer = ((CommonNavigator) repositoryView).getCommonViewer();
                    Object input = commonViewer.getInput();
                    // make sure to init the repository view rightly.
                    commonViewer.setInput(input);
                }
                // TDQ-15627: when the current is DQ Perspective, need to refresh DQ Repository View.
                IViewPart DqrepositoryView = RepositoryManagerHelper.getDQRepositoryView();
                if (DqrepositoryView != null) {
                    ((CommonNavigator) DqrepositoryView).getCommonViewer().refresh();
                }
                // TDQ-15627~
            }
        });
    }
}

class ReferenceProjectLabelProvider implements ILabelProvider {

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public Image getImage(Object element) {
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ProjectReferenceBean) {
            ProjectReferenceBean pr = (ProjectReferenceBean) element;
            StringBuffer sb = new StringBuffer();
            sb.append(ProjectRefSettingPage.getProjectDecription(pr.getReferenceProjectLabel(), pr.getReferenceBranch()));
            return sb.toString();
        }
        return null;
    }
}

class ProjectReferenceBean {

    private boolean isValid = true;

    private String referenceBranch;

    private org.talend.core.model.properties.Project referenceProject;

    private String referenceProjectLabel;

    public String getReferenceBranch() {
        return referenceBranch;
    }

    public void setReferenceBranch(String referenceBranch) {
        this.referenceBranch = referenceBranch;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getReferenceProjectLabel() {
        return referenceProjectLabel;
    }

    public void setReferenceProjectLabel(String referenceProjectLabel) {
        this.referenceProjectLabel = referenceProjectLabel;
    }

    public org.talend.core.model.properties.Project getReferenceProject() {
        return referenceProject;
    }

    public void setReferenceProject(org.talend.core.model.properties.Project referenceProject) {
        this.referenceProject = referenceProject;
    }

}
