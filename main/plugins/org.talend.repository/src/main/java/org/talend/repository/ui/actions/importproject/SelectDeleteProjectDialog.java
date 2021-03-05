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
package org.talend.repository.ui.actions.importproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.prefs.GeneralParametersProvider;
import org.talend.core.prefs.GeneralParametersProvider.GeneralParameters;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.ERepositoryImages;

/**
 * DOC qwei class global comment. Detailled comment
 */
public class SelectDeleteProjectDialog extends SelectionDialog {

    private Button bSelectAll;

    private Button bDeselectAll;

    private Button chkButton;

    private ContainerCheckedTreeViewer treeViewer;

    private List<Object> projectItemList = new ArrayList<Object>();

    private List<Object> delItemList = new ArrayList<Object>();

    private Project pro;

    private static final String DEFAULTMESAGE = Messages.getString("SelectDeleteProjectDialog.Label"); //$NON-NLS-1$

    private static final String TITILE = Messages.getString("SelectDeleteProjectDialog.Title"); //$NON-NLS-1$

    private List<Project> projects = new ArrayList<Project>();

    protected boolean login = false;

    /**
     * DOC qwei SelectDeleteProjectDialog constructor comment.
     *
     * @param parentShell
     */
    protected SelectDeleteProjectDialog(Shell parentShell) {
        super(parentShell);
        super.setShellStyle((SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.RESIZE | SWT.MIN | SWT.MAX));
        // TODO Auto-generated constructor stub
        setTitle(TITILE);
        setMessage(DEFAULTMESAGE);
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        this.pro = repositoryContext.getProject();

    }

    public SelectDeleteProjectDialog(Shell parentShell, boolean login) {
        this(parentShell);
        getProjectItem(login);
        this.login = login;
    }

    private void getProjectItem(boolean login) {
        List<String> notExportProjects = Arrays.asList(GeneralParametersProvider
                .getStrings(GeneralParameters.PROJECTS_EXCLUDED_FROM_EXPORT));
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        if (projects == null) {
            CommonExceptionHandler.process(new Exception(Messages.getString("SelectDeleteProjectDialog.retrieveProjects.npe"))); //$NON-NLS-1$
            return;
        }
        for (int i = 0; i < projects.length; i++) {
            try {
                if (projects[i] != null && !projects[i].getResourceAttributes().isReadOnly() && projects[i].isOpen()
                        && !notExportProjects.contains(projects[i].getName())) {
                    if (!login && pro.getLabel().toLowerCase().equals(projects[i].getName().toLowerCase())) {
                        // nothing need to do
                    } else {
                        projectItemList.add(projects[i]);
                    }
                }
            } catch (Throwable e) {
                CommonExceptionHandler.process(e);
            }
        }

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        createMessageArea(composite);
        Group group = Form.createGroup(composite, 20, null, 250);
        Composite inner = new Composite(group, SWT.NONE);
        inner.setFont(composite.getFont());
        inner.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 10;
        layout.makeColumnsEqualWidth = false;
        inner.setLayout(layout);
        createTreeViewer(inner);
        createOptionsArea(inner);
        return composite;
    }

    private void createOptionsArea(Composite parent) {
        Composite optionsGroup = new Composite(parent, SWT.NONE);
        optionsGroup.setLayout(new GridLayout());
        optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        chkButton = new Button(optionsGroup, SWT.CHECK);
        chkButton.setText(Messages.getString("SelectDeleteProjectDialog.deleteWarning")); //$NON-NLS-1$
        chkButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        chkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                chkButton.setSelection(chkButton.getSelection());
            }
        });
    }

    private void createTreeViewer(Composite parent) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.makeColumnsEqualWidth = false;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
        GridData listData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        treeViewer = new ContainerCheckedTreeViewer(parent);
        treeViewer.getControl().setLayoutData(listData);
        // treeViewer.setContentProvider(getResourceProvider(IResource.FOLDER | IResource.PROJECT));
        treeViewer.setContentProvider(getResourceProvider(IResource.PROJECT));
        // treeViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
        treeViewer.setLabelProvider(new LabelProvider() {

            @Override
            public final String getText(Object element) {
                // query the element for its label

                String label = ""; //$NON-NLS-1$
                if (element instanceof IProject) {
                    IProject project = (IProject) element;
                    label = getLabel(project.getName());
                } else if (element instanceof IFolder) {
                    IFolder folder = (IFolder) element;
                    label = getLabel(folder.getName());
                }
                return label;
            }

            private String getLabel(String name) {
                List<Project> proItem = getProjectItem();
                for (int i = 0; i < proItem.size(); i++) {
                    Project p = proItem.get(i);
                    String proName = p.getTechnicalLabel()/* .toUpperCase() */;
                    if (proName.equals(name)) {
                        return ProjectManager.getProjectDisplayLabel(p.getEmfProject());
                    }
                }
                return name;
            }

            @Override
            public final Image getImage(Object element) {
                // obtain the base image by querying the element

                if (element instanceof IProject) {
                    return ImageProvider.getImage(ERepositoryImages.PROJECT_ICON);
                } else if (element instanceof IFolder) {
                    return ImageProvider.getImage(ERepositoryImages.FOLDER_ICON);
                }
                return null;
            }
        });
        treeViewer.setInput(projectItemList);
        treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        addTreeListener();
        createButtons(parent);

    }

    private List<Project> getProjectItem() {
        if (login) {
            List<Project> projectList = new ArrayList<Project>();
            if (projectItemList != null && !projectItemList.isEmpty()) {
                Iterator<Object> iter = projectItemList.iterator();
                while (iter.hasNext()) {
                    Object project = iter.next();
                    if (project instanceof Project) {
                        projectList.add((Project) project);
                    }
                }
            }
            return projectList;
        }

        if (!projects.isEmpty()) {
            return projects;
        }
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Project[] projects = null;
        try {
            projects = repositoryFactory.readProject();
        } catch (PersistenceException e) {
            CommonExceptionHandler.process(e);
        } catch (BusinessException e) {
            CommonExceptionHandler.process(e);
        }
        for (Project p : projects) {
            this.projects.add(p);
        }
        if (projects != null) {
            return this.projects;
        }
        return Collections.emptyList();
    }

    private void createButtons(Composite parent) {

        Composite buttons = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttons.setLayout(layout);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        bSelectAll = new Button(buttons, SWT.PUSH);
        bSelectAll.setText(Messages.getString("SelectDeleteProjectDialog.SelectAll")); //$NON-NLS-1$
        bSelectAll.setFont(parent.getFont());
        setButtonLayoutData(bSelectAll);
        bSelectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                treeViewer.setAllChecked(true);
                delItemList = projectItemList;
            }
        });

        bDeselectAll = new Button(buttons, SWT.PUSH);
        bDeselectAll.setText(Messages.getString("SelectDeleteProjectDialog.DeselectAll")); //$NON-NLS-1$
        bDeselectAll.setFont(parent.getFont());
        setButtonLayoutData(bDeselectAll);
        bDeselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                treeViewer.setAllChecked(false);
                if (!delItemList.isEmpty()) {
                    delItemList.removeAll(projectItemList);
                }
            }
        });
    }

    private ITreeContentProvider getResourceProvider(final int resourceType) {
        return new WorkbenchContentProvider() {

            @Override
            public Object[] getChildren(Object o) {
                if (o instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) o).members();
                    } catch (CoreException e) {
                        // just return an empty set of children
                        return new Object[0];
                    }

                    // filter out the desired resource types
                    ArrayList results = new ArrayList();
                    for (IResource member : members) {
                        // And the test bits with the resource types to see if they are what we want
                        if ((member.getType() & resourceType) > 0) {
                            results.add(member);
                        }
                    }
                    return results.toArray();
                }
                // input element case
                if (o instanceof ArrayList) {
                    return ((ArrayList) o).toArray();
                }
                return new Object[0];
            }
        };
    }

    private void addTreeListener() {
        treeViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object obj = event.getElement();
                if (event.getChecked()) {
                    delItemList.add(obj);
                } else {
                    if (delItemList.contains(obj)) {
                        delItemList.remove(obj);
                    }

                }

            }
        });
    }

    private void delProjectItem() {
        try {

            if (delItemList.size() != 0) {
                for (Object obj : delItemList) {
                    if (obj instanceof IProject) {
                        IProject project = (IProject) obj;
                        // project.delete(true, null);
                        project.delete(!chkButton.getSelection(), true, null);
                        // IResourceChangeDescriptionFactory factory =
                        // ResourceChangeValidator.getValidator().createDeltaFactory();
                        // factory.delete(project);
                    } else if (obj instanceof IFolder) {
                        IFolder folder = (IFolder) obj;
                        folder.delete(false, null);
                    }

                }
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    protected void okPressed() {
        delProjectItem();
        projects = null;
        super.okPressed();
    }

    public List<Object> getDelList() {
        return delItemList;
    }
}
