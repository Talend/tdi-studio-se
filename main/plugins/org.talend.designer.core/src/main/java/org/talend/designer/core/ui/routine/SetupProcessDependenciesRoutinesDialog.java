// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.routine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 */
public class SetupProcessDependenciesRoutinesDialog extends Dialog {

    private boolean readonly;

    private final List<RoutineItemRecord> userRoutines = new ArrayList<RoutineItemRecord>();

    private final List<RoutineItemRecord> systemRoutines = new ArrayList<RoutineItemRecord>();

    private CTabFolder folder;

    private CTabItem userTabItem, systemTabItem;

    private Button addBtn, delBtn, upBtn, downBtn;

    private ListViewer userViewer, systemViewer;

    private final Map<Project, List<Property>> allRoutineItems = new HashMap<Project, List<Property>>();

    public SetupProcessDependenciesRoutinesDialog(Shell parentShell, ProcessType process, boolean readonly) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
        this.readonly = readonly;
        init(process);
    }

    @SuppressWarnings("unchecked")
    private void init(ProcessType process) {
        allRoutineItems.clear();

        ProjectManager projectManager = ProjectManager.getInstance();
        Project currentProject = projectManager.getCurrentProject();

        initModels(currentProject);
        Set<Project> referenceProjects = new HashSet<Project>();
        this.getAllReferenceProjects(currentProject, referenceProjects);
        initRefProjects(referenceProjects);
        List<RoutinesParameterType> routinesDependencies = process.getParameters().getRoutinesParameter();
        List<String> typeNames = new ArrayList<String>();
        for (RoutinesParameterType type : routinesDependencies) {
            RoutineItemRecord record = new RoutineItemRecord();

            record.setName(type.getName());

            Property property = findObject(type.getId(), type.getName());
            if (property != null) {
                record.setId(property.getId()); // if system, id is not used
                record.setLabel(property.getLabel());
            } else {
                record.setHasProblem(true);
                record.setLabel(type.getName()); // use the record
            }
            if (!record.hasProblem()) { // if lost, willn't display
                if (((RoutineItem) property.getItem()).isBuiltIn()) {
                    systemRoutines.add(record);
                } else {
                    if (typeNames.contains(type.getName())) {
                        break;
                    } else {
                        typeNames.add(type.getName());
                        userRoutines.add(record);
                    }
                }
            }
        }
    }

    private void getAllReferenceProjects(Project currentProject, Set<Project> referenceProjects) {
        for (Project p : ProjectManager.getInstance().getReferencedProjects(currentProject)) {
            referenceProjects.add(p);
            getAllReferenceProjects(p, referenceProjects);
        }
    }

    private void initRefProjects(Set<Project> referenceProjects) {
        for (Project p : referenceProjects) {
            initModels(p);
        }
    }

    private void initModels(Project project) {
        try {

            IProxyRepositoryFactory proxyRepositoryFactory = CorePlugin.getDefault().getRepositoryService()
                    .getProxyRepositoryFactory();
            List<IRepositoryViewObject> allRoutineItemObjects = proxyRepositoryFactory.getAll(project,
                    ERepositoryObjectType.ROUTINES, RoutinesUtil.allowDeletedRoutine());
            allRoutineItemObjects.addAll(proxyRepositoryFactory.getAll(project, ERepositoryObjectType.PIG_UDF,
                    RoutinesUtil.allowDeletedRoutine()));
            for (IRepositoryViewObject obj : allRoutineItemObjects) {
                Property property = obj.getProperty();
                if (project.equals(ProjectManager.getInstance().getCurrentProject())) {
                    addItems(project, property);
                } else {
                    // don't add system routines in ref-project
                    if (property.getItem() instanceof RoutineItem && !((RoutineItem) property.getItem()).isBuiltIn()) {
                        addItems(project, property);
                    }
                }
            }

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    private void addItems(Project project, Property property) {
        List<Property> list = allRoutineItems.get(project);
        if (list == null) {
            list = new ArrayList<Property>();
            allRoutineItems.put(project, list);
        }
        list.add(property);
    }

    private Property findObject(String idOrName, String name) {
        for (Project p : allRoutineItems.keySet()) {
            List<Property> list = allRoutineItems.get(p);
            if (list != null) {
                for (Property property : list) {
                    String objIdOrName = property.getId();
                    String objName = property.getLabel();
                    // objIdOrName = property.getLabel();
                    if (objIdOrName != null && objIdOrName.equals(idOrName) && property.getItem() instanceof RoutineItem) {
                        return property;
                    } else if (objName != null && objName.equals(name) && property.getItem() instanceof RoutineItem) {
                        return property;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("SetupProcessDependenciesRoutinesAction.title")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        updateButtons();
        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.heightHint = 200;
        layoutData.widthHint = 350;

        composite.setLayoutData(layoutData);
        applyDialogFont(composite);

        createTabFolderField(composite);
        createButtonField(composite);

        return composite;
    }

    private void createTabFolderField(Composite parent) {
        // tab
        folder = new CTabFolder(parent, SWT.NONE);
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));
        folder.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons();
            }

        });
        userTabItem = new CTabItem(folder, SWT.NONE);
        userTabItem.setText(Messages.getString("SetupProcessDependenciesRoutinesDialog.userRoutineLabel")); //$NON-NLS-1$

        systemTabItem = new CTabItem(folder, SWT.NONE);
        systemTabItem.setText(Messages.getString("SetupProcessDependenciesRoutinesDialog.systemRoutineLabel")); //$NON-NLS-1$

        folder.setSelection(userTabItem);
        folder.setSimple(false);

        ISelectionChangedListener listListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtons();
            }
        };
        // user
        Composite userComposite = new Composite(folder, SWT.NONE);
        userComposite.setLayout(new GridLayout());
        userComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        userViewer = new ListViewer(userComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        userViewer.setLabelProvider(new RoutineRecordLabelProvider());
        userViewer.setContentProvider(ArrayContentProvider.getInstance());
        userViewer.setInput(userRoutines);
        userViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
        userViewer.addSelectionChangedListener(listListener);

        userTabItem.setControl(userComposite);

        // system
        Composite systemComposite = new Composite(folder, SWT.NONE);
        systemComposite.setLayout(new GridLayout());
        systemComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        systemViewer = new ListViewer(systemComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        systemViewer.setLabelProvider(new RoutineRecordLabelProvider());
        systemViewer.setContentProvider(ArrayContentProvider.getInstance());
        systemViewer.setInput(systemRoutines);
        systemViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
        systemViewer.addSelectionChangedListener(listListener);

        systemTabItem.setControl(systemComposite);
    }

    private void createButtonField(Composite parent) {
        Composite btnComposite = new Composite(parent, SWT.NONE);
        btnComposite.setLayout(new GridLayout());
        GridData layoutData = new GridData(GridData.FILL_VERTICAL);
        layoutData.verticalAlignment = SWT.CENTER;
        btnComposite.setLayoutData(layoutData);

        addBtn = new Button(btnComposite, SWT.PUSH);
        addBtn.setImage(ImageProvider.getImage(EImage.ADD_ICON));
        addBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ListViewer currentViewer = getCurrentViewer();
                if (currentViewer == null) {
                    return;
                }
                boolean system = (currentViewer == systemViewer);
                List<RoutineItemRecord> currentRecords = getCurrentRecords();
                ShowRoutineItemsDialog dialog = new ShowRoutineItemsDialog(getShell(), allRoutineItems, currentRecords, system);
                if (dialog.open() == Window.OK) {
                    Property[] selectedItems = dialog.getSelectedItems();
                    List<Property> needAddedItems = new ArrayList<Property>();
                    // filter
                    for (Property p : selectedItems) {
                        boolean found = false;
                        for (RoutineItemRecord record : currentRecords) {
                            if (system) {
                                found = p.getLabel().equals(record.getLabel());
                            } else {
                                found = p.getId().equals(record.getId());
                            }
                        }
                        if (!found) {
                            needAddedItems.add(p);
                        }
                    }
                    // create
                    for (Property p : needAddedItems) {
                        RoutineItemRecord newOne = new RoutineItemRecord();
                        newOne.setId(p.getId());
                        newOne.setLabel(p.getLabel());
                        newOne.setName(p.getLabel());
                        newOne.setVersion(p.getVersion());
                        currentRecords.add(newOne);
                    }
                    //
                    currentViewer.setInput(currentRecords);
                    currentViewer.refresh();
                    updateButtons();
                }
            }

        });

        delBtn = new Button(btnComposite, SWT.PUSH);
        delBtn.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
        delBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ListViewer currentViewer = getCurrentViewer();
                List<RoutineItemRecord> currentRecords = getCurrentRecords();
                if (currentViewer != null && !((IStructuredSelection) currentViewer.getSelection()).isEmpty()) {
                    Iterator iterator = ((IStructuredSelection) currentViewer.getSelection()).iterator();
                    int index = 0;
                    while (iterator.hasNext()) {
                        Object selectedRecord = iterator.next();
                        if (currentRecords != null && selectedRecord != null) {
                            index = currentRecords.indexOf(selectedRecord);
                            currentRecords.remove(selectedRecord);
                            if (index > currentRecords.size() - 1) {
                                index = currentRecords.size() - 1;
                            } else if (index < 0) {
                                index = 0;
                            }
                        }
                    }
                    currentViewer.setInput(currentRecords);
                    currentViewer.getList().select(index);
                    currentViewer.refresh();

                }

                updateButtons();
            }

        });
        upBtn = new Button(btnComposite, SWT.PUSH);
        upBtn.setImage(ImageProvider.getImage(EImage.UP_ICON));
        upBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveDatas(true);
            }

        });
        downBtn = new Button(btnComposite, SWT.PUSH);
        downBtn.setImage(ImageProvider.getImage(EImage.DOWN_ICON));
        downBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveDatas(false);
            }

        });
    }

    private void moveDatas(boolean up) {
        ListViewer viewer = getCurrentViewer();
        List<RoutineItemRecord> records = getCurrentRecords();
        if (viewer != null && records != null && records.size() > 1) {
            int selectionIndex = viewer.getList().getSelectionIndex();
            final int size = records.size();
            if (selectionIndex > -1 && selectionIndex < size) {
                int newIndex = selectionIndex;
                RoutineItemRecord movedRecord = records.remove(selectionIndex);
                if (movedRecord != null) {
                    if (up) {
                        if (selectionIndex == 0) {
                            newIndex = size - 1;
                        } else {
                            newIndex = selectionIndex - 1;
                        }

                    } else {
                        if (selectionIndex == size - 1) {
                            newIndex = 0;
                        } else {
                            newIndex = selectionIndex + 1;
                        }
                    }
                    records.add(newIndex, movedRecord);

                    viewer.setInput(records);
                    viewer.refresh();

                    viewer.getList().setSelection(newIndex);
                }
            }
        }

        updateButtons();
    }

    private void updateButtons() {
        updateButtons(getCurrentViewer());
    }

    private ListViewer getCurrentViewer() {
        if (folder.getSelection() == userTabItem) {
            return userViewer;
        } else if (folder.getSelection() == systemTabItem) {
            return systemViewer;
        }
        return null;
    }

    private List<RoutineItemRecord> getCurrentRecords() {
        if (folder.getSelection() == userTabItem) {
            return userRoutines;
        } else if (folder.getSelection() == systemTabItem) {
            return systemRoutines;
        }
        return null;
    }

    private RoutineItemRecord getCurrentSelectedRecord() {
        ListViewer currentViewer = getCurrentViewer();
        if (currentViewer != null) {
            Object firstElement = ((IStructuredSelection) currentViewer.getSelection()).getFirstElement();
            if (firstElement != null && firstElement instanceof RoutineItemRecord) {
                return (RoutineItemRecord) firstElement;
            }

        }
        return null;
    }

    private void updateButtons(ListViewer viewer) {
        addBtn.setEnabled(false);
        delBtn.setEnabled(false);
        upBtn.setEnabled(false);
        downBtn.setEnabled(false);

        if (viewer != null && !readonly) {
            addBtn.setEnabled(true);
            if (!((IStructuredSelection) viewer.getSelection()).isEmpty()) {
                delBtn.setEnabled(true);
                // more than one in list, and only one select
                if (viewer.getList().getItemCount() > 1 && ((IStructuredSelection) viewer.getSelection()).size() == 1) {
                    upBtn.setEnabled(true);
                    downBtn.setEnabled(true);
                }
            }
        }
        if (readonly) {
            Button button = getButton(IDialogConstants.OK_ID);
            if (button != null && !button.isDisposed()) {
                button.setEnabled(false);
            }
        }
    }

    public List<RoutineItemRecord> getUserRoutines() {
        return this.userRoutines;
    }

    public List<RoutineItemRecord> getSystemRoutines() {
        return this.systemRoutines;
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

}
