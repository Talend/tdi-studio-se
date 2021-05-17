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
package org.talend.repository.preference;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.composite.ThreeCompositesSashForm;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.component.preference.provider.IPaletteItem;
import org.talend.core.ui.component.preference.provider.TalendPaletteLabelProvider;
import org.talend.core.ui.component.preference.provider.TalendPaletteTreeProvider;
import org.talend.core.ui.component.settings.ComponentsSettingsHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.preference.palettesettings.ComponentPaletteItem;
import org.talend.repository.preference.palettesettings.FolderPaletteItem;
import org.talend.repository.preference.palettesettings.PaletteItemHelper;
import org.talend.repository.preference.palettesettings.RootPaletteItem;
import org.talend.repository.ui.actions.ShowStandardAction;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class PaletteSettingPage extends ProjectSettingPage {

    private static final String FAMILY_SPEARATOR = "--FAMILY--"; //$NON-NLS-1$

    // private static final String DIALOG_TITLE = "Palette Settings";

    private static final boolean RESTORE = true;

    private TreeViewer hiddenViewer, displayViewer;

    private Project project = pro;

    private Button hideCompnentsButton;

    private Button displayComponentsButton;

    private ThreeCompositesSashForm compositesSachForm;

    private boolean needCodeGen;

    // <name:visiblility>
    private List<ComponentSetting> statusBackup = new ArrayList<ComponentSetting>();

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        init();

        Composite composite = new Composite(parent, 0);
        compositesSachForm = new ThreeCompositesSashForm(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);
        addTreeViewer(compositesSachForm);
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            compositesSachForm.setEnabled(false);
        }

        return composite;
    }

    private void init() {
        // this.project = pro;
        List<ComponentSetting> c = getComponentsFromProject(project);
        for (ComponentSetting componentSetting : c) {
            statusBackup.add(componentSetting);
        }
    }

    private List<IPaletteItem> getViewerInput() {
        return PaletteItemHelper.buildFullPaletteItemList();
    }

    /**
     * qwei Comment method "addViewer".
     *
     * @param parent
     */
    private void addTreeViewer(ThreeCompositesSashForm parent) {
        ViewerSorter sorter = new ViewerSorter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
             * java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof ComponentPaletteItem && e2 instanceof ComponentPaletteItem) {
                    return super.compare(viewer, ((IPaletteItem) e1).getLabel(), ((IPaletteItem) e2).getLabel());
                } else if (e1 instanceof FolderPaletteItem && e2 instanceof FolderPaletteItem) {
                    return super.compare(viewer, ((IPaletteItem) e1).getLabel(), ((IPaletteItem) e2).getLabel());
                } else if (e1 instanceof ComponentPaletteItem && e2 instanceof FolderPaletteItem) {
                    return 1;
                } else if (e1 instanceof FolderPaletteItem && e2 instanceof ComponentPaletteItem) {
                    return -1;
                } else if (e1 instanceof RootPaletteItem && e2 instanceof RootPaletteItem) {
                    if (((IPaletteItem) e1).getPaletteType() == ComponentCategory.CATEGORY_4_DI) {
                        return -1; // up
                    } else if (((IPaletteItem) e2).getPaletteType() == ComponentCategory.CATEGORY_4_DI) {
                        return 1; // down
                    } else if (((IPaletteItem) e1).getPaletteType() == ComponentCategory.CATEGORY_4_CAMEL
                            && ((IPaletteItem) e2).getPaletteType() == ComponentCategory.CATEGORY_4_MAPREDUCE) {
                        return -1; // up
                    } else if (((IPaletteItem) e1).getPaletteType() == ComponentCategory.CATEGORY_4_MAPREDUCE
                            && ((IPaletteItem) e2).getPaletteType() == ComponentCategory.CATEGORY_4_CAMEL) {
                        return 1; // down
                    } else if (((IPaletteItem) e1).getPaletteType() == ComponentCategory.CATEGORY_4_MAPREDUCE
                            && ((IPaletteItem) e2).getPaletteType() == ComponentCategory.CATEGORY_4_STORM) {
                        return -1; // up
                    } else if (((IPaletteItem) e1).getPaletteType() == ComponentCategory.CATEGORY_4_STORM
                            && ((IPaletteItem) e2).getPaletteType() == ComponentCategory.CATEGORY_4_MAPREDUCE) {
                        return 1; // down
                    } else if (((IPaletteItem) e1).getPaletteType() == ComponentCategory.CATEGORY_4_CAMEL
                            && ((IPaletteItem) e2).getPaletteType() == ComponentCategory.CATEGORY_4_STORM) {
                        return -1; // up
                    } else if (((IPaletteItem) e1).getPaletteType() == ComponentCategory.CATEGORY_4_STORM
                            && ((IPaletteItem) e2).getPaletteType() == ComponentCategory.CATEGORY_4_CAMEL) {
                        return 1; // down
                    }

                }
                return super.compare(viewer, e1, e2);
            }

        };

        List<IPaletteItem> input = getViewerInput();
        Composite leftComposite = parent.getLeftComposite();
        Label label = new Label(leftComposite, SWT.NONE);
        label.setText("Hide");
        hiddenViewer = new TreeViewer(leftComposite);
        hiddenViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        hiddenViewer.setContentProvider(new TalendPaletteTreeProvider());
        hiddenViewer.setLabelProvider(new TalendPaletteLabelProvider());
        hiddenViewer.addFilter(getFilterForComponent(false));
        hiddenViewer.setSorter(sorter);

        hiddenViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                displayComponentsButton.setEnabled(!event.getSelection().isEmpty());
            }
        });
        createButtons(parent.getMidComposite());
        Composite rightComposite = parent.getRightComposite();
        label = new Label(rightComposite, SWT.NONE);
        label.setText("Show");
        displayViewer = new TreeViewer(rightComposite);
        displayViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        displayViewer.setContentProvider(new TalendPaletteTreeProvider());
        displayViewer.setLabelProvider(new TalendPaletteLabelProvider());
        displayViewer.addFilter(getFilterForComponent(true));

        displayViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                hideCompnentsButton.setEnabled(!event.getSelection().isEmpty());
            }
        });
        displayViewer.setSorter(sorter);

        getComponentsFromProject(project);
        hiddenViewer.setInput(input);
        displayViewer.setInput(input);
        if (!input.isEmpty()) {
            displayViewer.expandToLevel(input.get(0), 1);
        }
        if (!input.isEmpty()) {
            hiddenViewer.expandToLevel(input.get(0), 1);
        }
    }

    /**
     * DOC qwei Comment method "getFilterForHiddenComponent". isVisible false for left viewer; true for right viewer
     *
     * @return
     */
    private ViewerFilter getFilterForComponent(final boolean isVisible) {
        ViewerFilter filter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                IPaletteItem entry = (IPaletteItem) element;

                if (!(entry instanceof ComponentPaletteItem)) {
                    return isFolderVisible(entry, isVisible);
                }

                if (isVisible) {
                    return isComponentVisible(entry);
                } else {
                    return !isComponentVisible(entry);
                }
            }
        };
        return filter;
    }

    /**
     * Check if this folder needs to be showed. *
     *
     * @param entry
     * @param isVisible
     * @return
     */
    protected boolean isFolderVisible(IPaletteItem container, boolean isVisible) {
        for (IPaletteItem entry : container.getChildren()) {
            if (!(entry instanceof ComponentPaletteItem)) {
                boolean display = isFolderVisible(entry, isVisible);
                if (display) {
                    return true;
                } else {
                    continue;
                }
            } else {
                if (isVisible) {
                    if (isComponentVisible(entry)) {
                        return true;
                    }
                } else {
                    if (!isComponentVisible(entry)) {
                        return true;
                    }
                }
                continue;
            }
        }

        return false;
    }

    private void refreshViewer() {
        hiddenViewer.refresh();
        displayViewer.refresh();
    }

    private void createButtons(Composite parent) {
        Label label1 = new Label(compositesSachForm.getMidComposite(), SWT.NONE);
        GridDataFactory.swtDefaults().hint(42, 18).applyTo(label1);
        Composite buttonComposite = new Composite(compositesSachForm.getMidComposite(), SWT.BORDER);
        Label label2 = new Label(compositesSachForm.getMidComposite(), SWT.NONE);
        GridDataFactory.swtDefaults().hint(42, 0).applyTo(label2);

        GridLayout gridLayout = new GridLayout(1, true);
        buttonComposite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        // gridData.verticalAlignment = GridData.CENTER;
        buttonComposite.setLayoutData(gridData);

        Composite buttonComposite2 = new Composite(buttonComposite, SWT.NONE);

        gridLayout = new GridLayout(1, true);
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        buttonComposite2.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.verticalAlignment = GridData.CENTER;
        buttonComposite2.setLayoutData(gridData);
        displayComponentsButton = new Button(buttonComposite2, SWT.NONE);
        displayComponentsButton.setImage(ImageProvider.getImage(EImage.RIGHT_ICON));
        displayComponentsButton.setToolTipText(""); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(displayComponentsButton);
        displayComponentsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setComponentVisible(hiddenViewer.getSelection(), true);
                setValid(true);
                needCodeGen = true;
            }
        });
        hideCompnentsButton = new Button(buttonComposite2, SWT.NONE);
        hideCompnentsButton.setImage(ImageProvider.getImage(EImage.LEFT_ICON));
        hideCompnentsButton.setToolTipText(""); //$NON-NLS-1$
        gridData = new GridData();
        gridData.verticalAlignment = GridData.CENTER;
        hideCompnentsButton.setLayoutData(gridData);
        hideCompnentsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setComponentVisible(displayViewer.getSelection(), false);
                setValid(true);
            }
        });

        displayComponentsButton.setEnabled(false);
        hideCompnentsButton.setEnabled(false);

    }

    /**
     * set the selected component as visible or not
     *
     * @param selection
     * @param b
     */
    protected void setComponentVisible(ISelection selection, boolean visible) {
        IStructuredSelection sel = (IStructuredSelection) selection;
        Set<IPaletteItem> items = new HashSet<IPaletteItem>();

        for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
            IPaletteItem entry = (IPaletteItem) iterator.next();
            retrieveAllEntry(items, entry);
        }

        Set<IComponent> usedComponents = getComponentsUsedInProject(ProjectManager.getInstance().getCurrentProject());

        boolean isUsed = false;
        for (IPaletteItem item : items) {
            if (!visible && item instanceof ComponentPaletteItem) {
                if (usedComponents.contains(((ComponentPaletteItem) item).getComponent())) {
                    isUsed = true;
                    continue;
                }

            }
            setComponentVisible(item, visible);

        }
        if (isUsed) {
            MessageDialog messageDialog = new MessageDialog(getShell(),
                    Messages.getString("PaletteSettingPage.paletteSettings"), null, //$NON-NLS-1$
                    Messages.getString("PaletteSettingPage.selection1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                            + Messages.getString("PaletteSettingPage.selection2"), MessageDialog.INFORMATION, //$NON-NLS-1$
                    new String[] { "OK" }, 0); //$NON-NLS-1$
            messageDialog.open();
        }

        refreshViewer();
    }

    private void retrieveAllEntry(Set<IPaletteItem> list, IPaletteItem entry) {
        if (!(entry instanceof ComponentPaletteItem)) {
            IPaletteItem container = entry;
            for (Object element : container.getChildren()) {
                IPaletteItem en = (IPaletteItem) element;
                retrieveAllEntry(list, en);
            }
        } else {
            list.add(entry);
        }

    }

    @SuppressWarnings("unchecked")
    public List<ComponentSetting> getComponentsFromProject(Project project) {
        List<ComponentSetting> components = project.getEmfProject().getComponentsSettings();
        return components;
    }

    public boolean isComponentVisible(IPaletteItem entry) {
        return ComponentsSettingsHelper.isComponentVisible(entry.getPaletteType().getName(), entry.getLabel(), entry.getFamily());
    }

    private void setComponentVisible(IPaletteItem item, boolean visible) {
        try {
            ComponentPaletteItem componentPaletteItem = (ComponentPaletteItem) item;
            List<ComponentSetting> components = getComponentsFromProject(project);
            List<ComponentSetting> toRemoveFromSettings = new ArrayList<ComponentSetting>();
            for (ComponentSetting componentSetting : components) {
                if (componentSetting.getFamily() != null
                        && componentSetting.getFamily().equals(componentPaletteItem.getFamily())
                        && componentSetting.getName().equals(
                                item.getPaletteType().getName() + "|" + componentPaletteItem.getLabel())) {
                    if (visible == componentPaletteItem.getComponent().isVisibleInComponentDefinition()) {
                        toRemoveFromSettings.add(componentSetting);
                    }
                    componentSetting.setHidden(!visible);
                }
            }
            components.removeAll(toRemoveFromSettings);
            if (visible != componentPaletteItem.getComponent().isVisibleInComponentDefinition()) {
                ComponentSetting cs = PropertiesFactory.eINSTANCE.createComponentSetting();
                cs.setName(item.getPaletteType().getName() + "|" + item.getLabel());
                cs.setHidden(!visible);
                cs.setFamily(item.getFamily());
                components.add(cs);
            }
            ComponentsSettingsHelper.resetHiddenComponents();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * qwei Comment method "getViewer".
     *
     * @return
     */
    public TreeViewer getViewer() {
        return this.hiddenViewer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        // TODO Auto-generated method stub
        // super.performApply();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        // TODO Auto-generated method stub
        boolean performOk = super.performOk();
        okPressed();
        return performOk;
    }

    protected void okPressed() {
        ProgressMonitorDialog pmd = new ProgressMonitorDialog(DisplayUtils.getDefaultShell());
        IRunnableWithProgress rwp = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();
                try {
                    prf.saveProject(project);
                    ShowStandardAction.getInstance().doRun();
                    if (needCodeGen) {
                        Job refreshTemplates = CorePlugin.getDefault().getCodeGeneratorService().refreshTemplates();
                        refreshTemplates.addJobChangeListener(new JobChangeAdapter() {

                            @Override
                            public void done(IJobChangeEvent event) {
                                CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
                            }
                        });

                    }

                    // ComponentUtilities.updatePalette();
                } catch (Exception ex) {
                    ExceptionHandler.process(ex);
                }
            }
        };
        try {
            pmd.run(true, false, rwp);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        retoreDefaultSettings();
    }

    /**
     * bqian Comment method "retoreDefaultSettings".
     */
    private void retoreDefaultSettings() {
        ComponentsFactoryProvider.restoreComponentVisibilityStatus();
        refreshViewer();
        setValid(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performCancel()
     */
    @Override
    public boolean performCancel() {
        cancelPressed();
        return super.performCancel();
    }

    protected void cancelPressed() {
        List<ComponentSetting> components = getComponentsFromProject(project);
        components.clear();
        components.addAll(statusBackup);
        ComponentsSettingsHelper.resetHiddenComponents();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {

    }

    Set<IComponent> componentsUsed;

    public Set<IComponent> getComponentsUsedInProject(Project project) {
        if (componentsUsed != null) {
            return componentsUsed;
        }
        componentsUsed = new HashSet<IComponent>();
        IProxyRepositoryFactory repositoryFactory = CoreUIPlugin.getDefault().getProxyRepositoryFactory();

        try {
            ERepositoryObjectType jobType = ERepositoryObjectType.PROCESS;
            if (jobType != null) {
                List<IRepositoryViewObject> allProcess = repositoryFactory.getAll(project, jobType, true);
                addUsedComponents(componentsUsed, allProcess, ComponentCategory.CATEGORY_4_DI);
            }
            ERepositoryObjectType jobletType = ERepositoryObjectType.JOBLET;
            if (jobletType != null) {
                List<IRepositoryViewObject> allJoblet = repositoryFactory.getAll(project, jobletType, true);
                addUsedComponents(componentsUsed, allJoblet, ComponentCategory.CATEGORY_4_DI);
            }
            ERepositoryObjectType routeType = ERepositoryObjectType.PROCESS_ROUTE;
            if (routeType != null) {
                List<IRepositoryViewObject> allRoutes = repositoryFactory.getAll(project, routeType, true);
                addUsedComponents(componentsUsed, allRoutes, ComponentCategory.CATEGORY_4_CAMEL);
            }
            ERepositoryObjectType mrType = ERepositoryObjectType.valueOf("PROCESS_MR"); //$NON-NLS-1$
            if (mrType != null) {
                List<IRepositoryViewObject> allMr = repositoryFactory.getAll(project, mrType, true);
                addUsedComponents(componentsUsed, allMr, ComponentCategory.CATEGORY_4_MAPREDUCE);
                addUsedComponents(componentsUsed, allMr, ComponentCategory.CATEGORY_4_SPARK);
            }
            ERepositoryObjectType stormType = ERepositoryObjectType.valueOf("PROCESS_STORM"); //$NON-NLS-1$
            if (stormType != null) {
                List<IRepositoryViewObject> allStorm = repositoryFactory.getAll(project, stormType, true);
                addUsedComponents(componentsUsed, allStorm, ComponentCategory.CATEGORY_4_STORM);
                addUsedComponents(componentsUsed, allStorm, ComponentCategory.CATEGORY_4_SPARKSTREAMING);
            }

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                        .getDefault().getService(ITestContainerProviderService.class);
                if (testContainerService != null) {
                    ERepositoryObjectType testCaseType = testContainerService.getTestCaseObjectType();
                    if (testCaseType != null) {
                        List<IRepositoryViewObject> allTestCase = repositoryFactory.getAll(project, testCaseType, true);
                        addUsedComponents(componentsUsed, allTestCase, ComponentCategory.CATEGORY_4_SPARK);
                        addUsedComponents(componentsUsed, allTestCase, ComponentCategory.CATEGORY_4_DI);
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return componentsUsed;
    }

    private void addUsedComponents(Set<IComponent> components, List<IRepositoryViewObject> allProcess, ComponentCategory category) {
        for (IRepositoryViewObject object : allProcess) {
            Item item = object.getProperty().getItem();

            List parameters = null;
            ProcessType processType = null;
            if (item instanceof ProcessItem) {
                processType = ((ProcessItem) item).getProcess();
            } else if (item instanceof JobletProcessItem) {
                processType = ((JobletProcessItem) item).getJobletProcess();
            }
            if (processType != null) {
                for (Object oNode : processType.getNode()) {
                    NodeType node = (NodeType) oNode;
                    IComponent component = ComponentsFactoryProvider.getInstance().get(node.getComponentName(),
                            category.getName());
                    if (component != null && component.getComponentType() == EComponentType.EMF) {
                        components.add(component);
                    }
                }
                if (processType.getParameters() != null) { // occurs actually only in joblets
                    parameters = processType.getParameters().getElementParameter();
                }
            }

            if (parameters != null) {
                // used in stats&log and implicite
                Set<IComponent> inStatsLogsAndImplicit = getComponentsInStatsLogsAndImplicit(parameters);
                if (inStatsLogsAndImplicit != null) {
                    components.addAll(inStatsLogsAndImplicit);
                }
            }
        }
    }

    private Set<IComponent> getComponentsInStatsLogsAndImplicit(List parameters) {
        Set<IComponent> components = new HashSet<IComponent>();
        for (Object obj : parameters) {
            String paramName = null;
            Object value = null;
            if (obj instanceof ElementParameterType) {
                ElementParameterType param = (ElementParameterType) obj;
                paramName = param.getName();
                value = param.getValue();
            }
            if (value == null) {
                continue;
            }
            if ("ON_STATCATCHER_FLAG".equals(paramName)) { //$NON-NLS-1$
                addComponent(components, "tStatCatcher", value); //$NON-NLS-1$
            } else if ("ON_METERCATCHER_FLAG".equals(paramName)) { //$NON-NLS-1$
                addComponent(components, "tFlowMeterCatcher", value); //$NON-NLS-1$
            } else if ("ON_LOGCATCHER_FLAG".equals(paramName)) { //$NON-NLS-1$
                addComponent(components, "tLogCatcher", value); //$NON-NLS-1$
            } else if ("ON_CONSOLE_FLAG".equals(paramName)) { //$NON-NLS-1$
                addComponent(components, "tLogRow", value); //$NON-NLS-1$
            } else if ("ON_FILES_FLAG".equals(paramName)) { //$NON-NLS-1$
                addComponent(components, "tFileOutputDelimited", value); //$NON-NLS-1$
            } else if ("ON_DATABASE_FLAG".equals(paramName)) { //$NON-NLS-1$
                String usedDatabase = getUsedDatabase(parameters, "DB_TYPE"); //$NON-NLS-1$
                if (usedDatabase != null) {
                    addComponent(components, usedDatabase, value);
                }
            } else if ("IMPLICIT_TCONTEXTLOAD".equals(paramName)) { //$NON-NLS-1$
                addComponent(components, "tContextLoad", value); //$NON-NLS-1$
            } else if ("FROM_FILE_FLAG_IMPLICIT_CONTEXT".equals(paramName)) { //$NON-NLS-1$
                addComponent(components, "tFileInputDelimited", value); //$NON-NLS-1$
            } else if ("FROM_DATABASE_FLAG_IMPLICIT_CONTEXT".equals(paramName)) { //$NON-NLS-1$
                String usedDatabase = getUsedDatabase(parameters, "DB_TYPE_IMPLICIT_CONTEXT"); //$NON-NLS-1$
                if (usedDatabase != null) {
                    addComponent(components, usedDatabase, value);
                }

            }
        }
        return components;
    }

    private String getUsedDatabase(List parameters, String typeName) {
        for (Object obj : parameters) {
            String paramName = null;
            Object value = null;
            String field = null;
            if (obj instanceof IElementParameter) {
                IElementParameter param = (IElementParameter) obj;
                paramName = param.getName();
                value = param.getValue();
                field = param.getFieldType().getName();
            } else if (obj instanceof ElementParameterType) {
                ElementParameterType param = (ElementParameterType) obj;
                paramName = param.getName();
                value = param.getValue();
                field = param.getField();
            }
            if (EParameterFieldType.CLOSED_LIST.getName().equals(field) && typeName.equals(paramName)) {
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }

    private void addComponent(Set<IComponent> components, String name, Object value) {
        if (Boolean.TRUE.equals(Boolean.valueOf(value.toString()))) {
            IComponent component = ComponentsFactoryProvider.getInstance().get(name, ComponentCategory.CATEGORY_4_DI.getName());
            if (component != null && component.getComponentType() == EComponentType.EMF) {
                components.add(component);
            }
        }
    }
}
