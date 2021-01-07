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
package org.talend.designer.core.ui.routine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.repository.ProjectManager;

/**
 * ggu class global comment. Detailled comment
 */
public class SetupProcessDependenciesRoutinesDialog extends Dialog {

    private boolean readonly;

    private boolean isRouteProcess;

    private final List<RoutineItemRecord> globalRoutines = new ArrayList<>();

    private final List<RoutineItemRecord> systemRoutines = new ArrayList<>();
    
    private final List<RoutineItemRecord> routinesJars = new ArrayList<>();

    private final List<RoutineItemRecord> beansJars = new ArrayList<>();

    private CTabFolder folder;

    private CTabItem globalRoutinesTabItem, routinesJarTabItem, beansJarTabItem;

    private Button addBtn, delBtn, upBtn, downBtn;

    private ListViewer globalRoutinesViewer, routinesJarViewer, beansJarViewer;

    private final Map<Project, List<Property>> allRoutineItems = new HashMap<Project, List<Property>>();

    private final Map<Project, List<Property>> allRoutinesJarItems = new HashMap<Project, List<Property>>();

    private final Map<Project, List<Property>> allBeansJarItems = new HashMap<Project, List<Property>>();

    public SetupProcessDependenciesRoutinesDialog(Shell parentShell, Item item, boolean readonly) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
        this.readonly = readonly;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = GlobalServiceRegister.getDefault()
                    .getService(ICamelDesignerCoreService.class);
            isRouteProcess = camelService.isInstanceofCamelRoutes(item);
        }
        init(item);
    }

    @SuppressWarnings("unchecked")
    private void init(Item item) {
        ProcessType process = null;
        if (item instanceof ProcessItem) {
            process = ((ProcessItem) item).getProcess();
        } else if (item instanceof JobletProcessItem) {
            process = ((JobletProcessItem) item).getJobletProcess();
        }
        allRoutineItems.clear();
        allRoutinesJarItems.clear();
        allBeansJarItems.clear();

        ProjectManager projectManager = ProjectManager.getInstance();
        Project currentProject = projectManager.getCurrentProject();
        initModelsForRoutines(currentProject);

        Set<Project> referenceProjects = new HashSet<Project>();
        getAllReferenceProjects(currentProject, referenceProjects);
        referenceProjects.forEach(p -> initModelsForRoutines(p));
        
        initModelsForCodesJars();

        Set<RoutineItemRecord> systemRoutinesSet = new LinkedHashSet<>();
        Set<RoutineItemRecord> globalRoutinesSet = new LinkedHashSet<>();
        Set<RoutineItemRecord> routinesJarsSet = new LinkedHashSet<>();
        Set<RoutineItemRecord> beansJarsSet = new LinkedHashSet<>();
        List<RoutinesParameterType> routinesDependencies = process.getParameters().getRoutinesParameter();
        for (RoutinesParameterType routinesParameter : routinesDependencies) {
            Property property = findObject(routinesParameter.getId(), routinesParameter.getName(), routinesParameter.getType());
            if (property == null) {
                // if lost, won't display
                continue;
            }
            RoutineItemRecord record = new RoutineItemRecord();
            record.setName(routinesParameter.getName());
            record.setId(property.getId()); // if system, id is not used
            record.setLabel(property.getLabel());
            if (routinesParameter.getType() != null) {
                record.setType(routinesParameter.getType());
            }
            if (property.getItem() instanceof RoutineItem && ((RoutineItem) property.getItem()).isBuiltIn()) {
                systemRoutinesSet.add(record);
            } else {
                if (routinesParameter.getType() == null) {
                    globalRoutinesSet.add(record);
                } else if (ERepositoryObjectType.getItemType(property.getItem()) == ERepositoryObjectType.ROUTINESJAR) {
                    routinesJarsSet.add(record);
                } else if (isRouteProcess
                        && ERepositoryObjectType.getItemType(property.getItem()) == ERepositoryObjectType.BEANSJAR) {
                    beansJarsSet.add(record);
                }
            }
        }
        systemRoutines.addAll(systemRoutinesSet);
        globalRoutines.addAll(globalRoutinesSet);
        routinesJars.addAll(routinesJarsSet);
        beansJars.addAll(beansJarsSet);
    }

    private void getAllReferenceProjects(Project currentProject, Set<Project> referenceProjects) {
        for (Project p : ProjectManager.getInstance().getReferencedProjects(currentProject)) {
            referenceProjects.add(p);
            getAllReferenceProjects(p, referenceProjects);
        }
    }

    private void initModelsForRoutines(Project project) {
        try {
            Project currentProject = ProjectManager.getInstance().getCurrentProject();
            ProxyRepositoryFactory.getInstance()
                    .getAll(project, ERepositoryObjectType.ROUTINES, RoutinesUtil.allowDeletedRoutine()).stream()
                    .map(o -> o.getProperty()).forEach(p -> {
                        // don't add system routines in ref-project
                        if (!project.equals(currentProject) && p.getItem() instanceof RoutineItem
                                && ((RoutineItem) p.getItem()).isBuiltIn()) {
                            return;
                        }
                        addItems(project, allRoutineItems, p);
                    });
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    private void initModelsForCodesJars() {
        for (Property property : CodesJarResourceCache.getAllCodesJars()) {
            Project project = ProjectManager.getInstance()
                    .getProjectFromProjectTechLabel(ProjectManager.getInstance().getProject(property).getTechnicalLabel());
            ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
            if (type == ERepositoryObjectType.ROUTINESJAR) {
                addItems(project, allRoutinesJarItems, property);
            } else if (type == ERepositoryObjectType.BEANSJAR && isRouteProcess) {
                addItems(project, allBeansJarItems, property);
            }
        }
    }

    private void addItems(Project project, Map<Project, List<Property>> all, Property property) {
        List<Property> list = all.get(project);
        if (list == null) {
            list = new ArrayList<Property>();
            all.put(project, list);
        }
        list.add(property);
    }

    private Property findObject(String id, String name, String type) {
        if (type == null) {
            for (Project p : allRoutineItems.keySet()) {
                for (Property property : allRoutineItems.get(p)) {
                    if (StringUtils.equals(property.getId(), id) || StringUtils.equals(property.getLabel(), name)) {
                        return property;
                    }
                }
            }
            return null;
        }
        for (Project p : allRoutinesJarItems.keySet()) {
            for (Property property : allRoutinesJarItems.get(p)) {
                String objType = ERepositoryObjectType.getItemType(property.getItem()).name();
                if ((StringUtils.equals(property.getId(), id) || StringUtils.equals(property.getLabel(), name))
                        && StringUtils.equals(objType, type)) {
                    return property;
                }
            }
        }
        if (isRouteProcess) {
            for (Project p : allBeansJarItems.keySet()) {
                for (Property property : allBeansJarItems.get(p)) {
                    String objType = ERepositoryObjectType.getItemType(property.getItem()).name();
                    if ((StringUtils.equals(property.getId(), id) || StringUtils.equals(property.getLabel(), name))
                            && StringUtils.equals(objType, type)) {
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
        layoutData.heightHint = 250;
        layoutData.widthHint = 350;
        if (isRouteProcess) {
            layoutData.widthHint = 480;
        }

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

        if (isRouteProcess) {
            beansJarTabItem = new CTabItem(folder, SWT.NONE);
            beansJarTabItem.setText(Messages.getString("SetupProcessDependenciesRoutinesDialog.beansJarLabel")); //$NON-NLS-1$
        }

        routinesJarTabItem = new CTabItem(folder, SWT.NONE);
        routinesJarTabItem.setText(Messages.getString("SetupProcessDependenciesRoutinesDialog.routinesJarLabel")); //$NON-NLS-1$

        globalRoutinesTabItem = new CTabItem(folder, SWT.NONE);
        globalRoutinesTabItem.setText(Messages.getString("SetupProcessDependenciesRoutinesDialog.userRoutineLabel")); //$NON-NLS-1$

        if (isRouteProcess) {
            folder.setSelection(beansJarTabItem);
        } else {
            folder.setSelection(routinesJarTabItem);
        }
        folder.setSimple(false);

        ISelectionChangedListener listListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtons();
            }
        };
        // global routines
        globalRoutinesViewer = createViewer(globalRoutinesTabItem, globalRoutines, listListener);
        // routines jars
        routinesJarViewer = createViewer(routinesJarTabItem, routinesJars, listListener);
        // beans jars
        if (isRouteProcess) {
            beansJarViewer = createViewer(beansJarTabItem, beansJars, listListener);
        }
    }

    private ListViewer createViewer(CTabItem tabItem, List<RoutineItemRecord> all, ISelectionChangedListener listListener) {
        Composite composite = new Composite(folder, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        ListViewer viewer = new ListViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.setLabelProvider(new RoutineRecordLabelProvider());
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.setInput(all);
        viewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.addSelectionChangedListener(listListener);

        tabItem.setControl(composite);
        return viewer;
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
                List<RoutineItemRecord> currentRecords = getCurrentRecords();
                ERepositoryObjectType type = null;
                Map<Project, List<Property>> allItems = null;
                if (currentViewer == globalRoutinesViewer) {
                    type = ERepositoryObjectType.ROUTINES;
                    allItems = allRoutineItems;
                } else if (currentViewer == routinesJarViewer) {
                    type = ERepositoryObjectType.ROUTINESJAR;
                    allItems = allRoutinesJarItems;
                } else if (currentViewer == beansJarViewer) {
                    type = ERepositoryObjectType.BEANSJAR;
                    allItems = allBeansJarItems;
                }
                ShowRoutineItemsDialog dialog = new ShowRoutineItemsDialog(getShell(), allItems, currentRecords, type);
                if (dialog.open() == Window.OK) {
                    Property[] selectedItems = dialog.getSelectedItems();
                    List<Property> needAddedItems = new ArrayList<Property>();
                    // filter
                    for (Property p : selectedItems) {
                        boolean found = false;
                        for (RoutineItemRecord record : currentRecords) {
                            found = p.getId().equals(record.getId());
                            if (found) {
                                break;
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
                        if (type != ERepositoryObjectType.ROUTINES) {
                            // won't store type for global routines to keep compatible
                            newOne.setType(type.name());
                        }
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
        CTabItem selection = folder.getSelection();
        if (selection == globalRoutinesTabItem) {
            return globalRoutinesViewer;
        }
        if (selection == routinesJarTabItem) {
            return routinesJarViewer;
        }
        if (isRouteProcess && selection == beansJarTabItem) {
            return beansJarViewer;
        }
        return null;
    }

    private List<RoutineItemRecord> getCurrentRecords() {
        CTabItem selection = folder.getSelection();
        if (selection == globalRoutinesTabItem) {
            return globalRoutines;
        }
        if (selection == routinesJarTabItem) {
            return routinesJars;
        }
        if (isRouteProcess && selection == beansJarTabItem) {
            return beansJars;
        }
        return Collections.emptyList();
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

    public List<RoutineItemRecord> getGlobalRoutines() {
        return globalRoutines;
    }

    public List<RoutineItemRecord> getRoutinesJars() {
        return routinesJars;
    }

    public List<RoutineItemRecord> getBeansJars() {
        if (isRouteProcess) {
            return beansJars;
        }
        return Collections.emptyList();
    }

    public List<RoutineItemRecord> getSystemRoutines() {
        return systemRoutines;
    }

}
