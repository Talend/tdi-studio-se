// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.views.jobsettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.business.BusinessType;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.EmptyRepositoryObject;
import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.services.IGITProviderService;
import org.talend.core.services.ISVNProviderService;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.IHeaderFooterProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.properties.tab.HorizontalTabFactory;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.properties.tab.TalendPropertyTabDescriptor;
import org.talend.core.ui.services.IGitUIProviderService;
import org.talend.core.ui.services.ISVNUiProviderService;
import org.talend.designer.business.diagram.custom.IDiagramModelService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.jobsettings.tabs.DeploymentComposite;
import org.talend.designer.core.ui.views.jobsettings.tabs.HeaderFooterComposite;
import org.talend.designer.core.ui.views.jobsettings.tabs.MainComposite;
import org.talend.designer.core.ui.views.jobsettings.tabs.ProcessVersionComposite;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;
import org.talend.designer.core.ui.views.statsandlogs.StatsAndLogsComposite;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IJobSettingsView;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobSettingsView extends ViewPart implements IJobSettingsView, ISelectionChangedListener {

    /**
     *
     */
    private static final String SEPARATOR = "->"; //$NON-NLS-1$

    public static final String VIEW_NAME_JOBLET = "Joblet"; //$NON-NLS-1$

    public static final String VIEW_NAME_STREAMING = "Streaming"; //$NON-NLS-1$

    public static final String VIEW_NAME_BATCH = "Batch"; //$NON-NLS-1$

    public static final String MODEL = "Model"; //$NON-NLS-1$

    private HorizontalTabFactory tabFactory = null;

    private TalendPropertyTabDescriptor currentSelectedTab;

    private Element element;

    private boolean cleaned;

    private boolean selectedPrimary;

    private boolean allowVerchange = true;

    private Process process;

    private Composite parent;

    private ISelection selectedModel;

    private ISVNProviderService svnService;

    private ISVNUiProviderService svnUIService;

    private IGITProviderService gitService;

    private IGitUIProviderService gitUIService;

    public JobSettingsView() {
        tabFactory = new HorizontalTabFactory();
        CorePlugin.getDefault().getRepositoryService().addRepositoryTreeViewListener(this);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
                .getService(IBrandingService.class);
        allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();
        initProviderServices();
    }

    private void initProviderServices() {

        if (PluginChecker.isSVNProviderPluginLoaded()) {
            svnService = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
            svnUIService = (ISVNUiProviderService) GlobalServiceRegister.getDefault().getService(ISVNUiProviderService.class);
        }
        if (PluginChecker.isGITProviderPluginLoaded()) {
            gitService = (IGITProviderService) GlobalServiceRegister.getDefault().getService(IGITProviderService.class);
            gitUIService = (IGitUIProviderService) GlobalServiceRegister.getDefault().getService(IGitUIProviderService.class);
        }
    }

    public static String getViewNameLable() {
        return Messages.getString("JobSettingsView.JobSettings"); //$NON-NLS-1$
    }

    private IProcess getProcess(IRepositoryViewObject viewObject) {
        boolean isOpen = RepositoryManager.isOpenedItemInEditor(viewObject);
        if (isOpen) {
            final IEditorPart activeEditor = getSite().getPage().getActiveEditor();
            if (activeEditor != null && activeEditor instanceof AbstractMultiPageTalendEditor) {
                AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
                IProcess process = talendEditor.getProcess();
                return process;
            }
        }
        return null;
    }

    @Override
    public void createPartControl(Composite parent) {
        // tabFactory = new HorizontalTabFactory();
        this.parent = parent;
        tabFactory.initComposite(parent, false);
        tabFactory.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                TalendPropertyTabDescriptor descriptor = (TalendPropertyTabDescriptor) selection.getFirstElement();

                if (descriptor == null) {
                    return;
                }

                if (currentSelectedTab != null) {
                    if ((!currentSelectedTab.getData().equals(descriptor.getData())
                            || currentSelectedTab.getData() != descriptor.getData()
                            || currentSelectedTab.getCategory() != descriptor.getCategory())) {
                        for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                            curControl.dispose();
                        }
                    }
                }

                if (element == null || !element.equals(descriptor.getData()) || currentSelectedTab == null
                        || currentSelectedTab.getCategory() != descriptor.getCategory() || selectedPrimary) {
                    Object data = descriptor.getData();
                    if (data instanceof Element) {
                        element = (Element) data;
                        currentSelectedTab = descriptor;
                        IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), element,
                                descriptor.getCategory());

                    } else if (data instanceof IRepositoryViewObject) {
                        IRepositoryViewObject viewObject = (IRepositoryViewObject) data;
                        IProcess process = getProcess(viewObject);
                        if (process != null && process instanceof Element && process.getId().equals(viewObject.getId())
                                && process.getVersion().equals(viewObject.getVersion())) {
                            data = process;
                        }
                        currentSelectedTab = descriptor;
                        IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), data,
                                descriptor.getCategory());

                    } else if (data instanceof IEditorPart) {
                        currentSelectedTab = descriptor;
                        IRepositoryViewObject repObj = retrieveBusiness((IEditorPart) data);
                        if (repObj != null) {
                            IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), repObj,
                                    descriptor.getCategory());
                        }

                    } else {
                        currentSelectedTab = descriptor;
                        IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), null,
                                descriptor.getCategory());
                    }
                    selectedPrimary = false;
                }

            }
        });

    }

    private IRepositoryViewObject retrieveBusiness(IEditorPart businessPart) {
        if (CorePlugin.getDefault().getDiagramModelService().isBusinessDiagramEditor(businessPart)) {
            IRepositoryViewObject lastVersion = null;
            selectedModel = CorePlugin.getDefault().getDiagramModelService().getBusinessEditorSelection(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());

            try {

                IRepositoryEditorInput input = CorePlugin.getDefault().getDiagramModelService()
                        .getBusinessDiagramEditorInput(businessPart);

                if (input != null) {
                    RepositoryNode node = input.getRepositoryNode();
                    if (node != null) {
                        lastVersion = node.getObject();
                    } else {
                        lastVersion = CorePlugin.getDefault().getProxyRepositoryFactory()
                                .getLastVersion(input.getItem().getProperty().getId());
                    }
                }
                return lastVersion;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    private IDynamicProperty createTabComposite(Composite parent, Object data, EComponentCategory category) {
        final int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS;
        IDynamicProperty dynamicComposite = null;

        if (EComponentCategory.EXTRA.equals(category)) {
            // achen modify to fix bug 0006241
            Process process = getElement();
            boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
            if (isJoblet) {
                dynamicComposite = new MultipleThreadDynamicComposite(parent, style, category, (Element) data, true);
            } else {
                dynamicComposite = new ExtraComposite(parent, style, category, (Element) data, true);
                CoreUIPlugin.setCSSId(dynamicComposite.getComposite(),
                        "org-talend-designer-core-ui-views-jobsettings-JobSettingsView-ExtraComposite");//$NON-NLS-1$
            }

        } else if (EComponentCategory.STATSANDLOGS.equals(category)) {
            dynamicComposite = new StatsAndLogsComposite(parent, style, category, (Element) data);
            CoreUIPlugin.setCSSId(dynamicComposite.getComposite(),
                    "org-talend-designer-core-ui-views-jobsettings-JobSettingsView-StatsAndLogsComposite");//$NON-NLS-1$
        } else if (EComponentCategory.CONTEXT.equals(category)) {
            // TODO
            // dynamicComposite = new ContextDynamicComposite(parent, style, category, element);

        } else if (EComponentCategory.MAIN.equals(category)) {
            dynamicComposite = new MainComposite(parent, SWT.NONE, tabFactory, (IRepositoryViewObject) data);
        } else if (EComponentCategory.VERSIONS.equals(category)) {
            if (allowVerchange) {
                dynamicComposite = new ProcessVersionComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(),
                        (IRepositoryViewObject) data);
            }
        } else if (EComponentCategory.HEADERFOOTER.equals(category)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IHeaderFooterProviderService.class)) {
                IHeaderFooterProviderService headerFooterService = (IHeaderFooterProviderService) GlobalServiceRegister
                        .getDefault().getService(IHeaderFooterProviderService.class);
                if (headerFooterService.isVisible()) {
                    dynamicComposite = new HeaderFooterComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(),
                            (IRepositoryViewObject) data);
                }
            }
        } else if (EComponentCategory.SVNHISTORY.equals(category) && svnUIService != null) {
            dynamicComposite = svnUIService.createProcessSVNHistoryComposite(parent, tabFactory.getWidgetFactory(),
                    (IRepositoryViewObject) data);
        } else if (EComponentCategory.GITHISTORY.equals(category) && gitUIService != null) {
            dynamicComposite = gitUIService.createProcessGitHistoryComposite(parent, this, tabFactory.getWidgetFactory(),
                    (IRepositoryViewObject) data);
        } else if (EComponentCategory.APPEARANCE.equals(category)) {
            dynamicComposite = (IDynamicProperty) CorePlugin.getDefault().getDiagramModelService()
                    .getBusinessAppearanceComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(), selectedModel);
        } else if (EComponentCategory.RULERS_AND_GRID.equals(category)) {
            dynamicComposite = (IDynamicProperty) CorePlugin.getDefault().getDiagramModelService()
                    .getBusinessRulersAndGridComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(), null);
        } else if (EComponentCategory.ASSIGNMENT.equals(category)) {
            dynamicComposite = (IDynamicProperty) CorePlugin.getDefault().getDiagramModelService()
                    .getBusinessAssignmentComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(), selectedModel);
        } else if (EComponentCategory.DEPLOYMENT.equals(category)) {
            dynamicComposite = new DeploymentComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(),
                    (IRepositoryViewObject) data);
        }

        if (dynamicComposite != null) {
            dynamicComposite.refresh();
        }
        currentSelectedTab.setPropertyComposite(dynamicComposite);
        return dynamicComposite;
    }

    /**
     *
     * DOC ggu Comment method "setElement".
     *
     * @param obj
     */

    private void setElement(Object obj, final String title, Image image) {
        EComponentCategory[] categories = null;

        if (obj != null && obj instanceof Process) {
            process = (Process) obj;
            if (currentSelectedTab != null && currentSelectedTab.getData().equals(process) && !cleaned) {
                return;
            }

            categories = getCategories(process);
        } else if (obj != null && obj instanceof IRepositoryViewObject) {
            categories = getCategories(obj);
            IRepositoryViewObject viewObject = (IRepositoryViewObject) obj;
            IProcess process = getProcess(viewObject);
            if (process != null && process instanceof Element && process.getId().equals(viewObject.getId())
                    && process.getVersion().equals(viewObject.getVersion())) {
                categories = getCategories(process);
            }
        } else if (obj instanceof IEditorPart) {
            if (CorePlugin.getDefault().getDiagramModelService().isBusinessDiagramEditor((IEditorPart) obj)) {
                categories = getCategories(obj);
            }

        } else {
            BusinessType type = CorePlugin.getDefault().getDiagramModelService().getBusinessModelType(obj);
            if (BusinessType.NOTE.equals(type) || BusinessType.SHAP.equals(type) || BusinessType.CONNECTION.equals(type)) {
                categories = getCategories(obj);
            } else {
                cleanDisplay();
                return;
            }
        }

        final List<TalendPropertyTabDescriptor> descriptors = new ArrayList<TalendPropertyTabDescriptor>();
        for (EComponentCategory category : categories) {
            TalendPropertyTabDescriptor d = new TalendPropertyTabDescriptor(category);
            d.setData(obj);
            descriptors.add(d);
        }

        tabFactory.setInput(descriptors);
        setPartName(title, image);
        cleaned = false;
        tabFactory.setSelection(new IStructuredSelection() {

            @Override
            public Object getFirstElement() {
                return null;
            }

            @Override
            public Iterator iterator() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public Object[] toArray() {
                return null;
            }

            @Override
            public List toList() {
                List<TalendPropertyTabDescriptor> d = new ArrayList<TalendPropertyTabDescriptor>();

                if (descriptors.size() > 0) {
                    if (currentSelectedTab != null) {
                        for (TalendPropertyTabDescriptor ds : descriptors) {
                            if (ds.getCategory() == currentSelectedTab.getCategory()) {
                                d.add(ds);
                                return d;
                            }
                        }
                    }
                    d.add(descriptors.get(0));
                }
                return d;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

        });

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.ViewPart#setPartName(java.lang.String)
     */
    @Override
    protected void setPartName(String partName) {
        setPartName(partName, null);
    }

    /**
     *
     * DOC ggu Comment method "setPartName".
     *
     * set title
     */
    public void setPartName(String typeTitle, Image icon) {
        String title = null;
        String type = null;

        if (typeTitle != null && typeTitle.contains(SEPARATOR)) {
            String[] tt = typeTitle.split(SEPARATOR);
            type = tt[0];
            title = tt[1];
        } else {
            title = typeTitle;
        }

        String viewName = getViewNameLable();
        if (element instanceof IProcess && AbstractProcessProvider.isExtensionProcessForJoblet((IProcess) element)) {
            viewName = VIEW_NAME_JOBLET;
        }

        if (type != null) {
            viewName = type;
        }

        if (title == null) {
            title = ""; //$NON-NLS-1$
        }
        if (!title.equals("")) { //$NON-NLS-1$
            viewName = viewName + "(" + title + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            super.setTitleToolTip(title);
        }
        if (tabFactory != null && icon == null) {
            icon = ImageProvider.getImage(ECoreImage.PROCESS_ICON);
            if (this.element != null && this.element instanceof IProcess) {
                if (((IProcess2) this.element).disableRunJobView()) { // ?? joblet
                    icon = ImageProvider.getImage(ECoreImage.JOBLET_ICON);
                } else if (ProcessUtils.isTestContainer(process)) {
                    icon = ImageProvider.getImage(ECoreImage.TEST_CONTAINER);
                } else if (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(process.getComponentsType())) {
                    icon = ImageProvider.getImage(EJobSettingImage.PROCESS_MR_ICON_X16);
                } else if (ComponentCategory.CATEGORY_4_SPARK.getName().equals(process.getComponentsType())) {
                    icon = ImageProvider.getImage(EJobSettingImage.PROCESS_SPARK_ICON_X16);
                } else if (ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName().equals(process.getComponentsType())) {
                    icon = ImageProvider.getImage(EJobSettingImage.PROCESS_SPARK_STREAMING_ICON_X16);
                } else if (ComponentCategory.CATEGORY_4_STORM.getName().equals(process.getComponentsType())) {
                    icon = ImageProvider.getImage(EJobSettingImage.PROCESS_STORM_ICON_X16);
                }
            }
        }
        tabFactory.setTitle(title, icon);
        super.setTitleImage(icon);
        if (gitService!=null && gitService.isProjectInGitMode()) {
            return;
        }

        // This invocation below will bring in refresh issue for git.
        super.setPartName(viewName);
    }

    /**
     * set the category.
     */
    private EComponentCategory[] getCategories(Object obj) {
        List<EComponentCategory> category = new ArrayList<EComponentCategory>();

        boolean isOfflineMode = CorePlugin.getDefault().getProxyRepositoryFactory().getRepositoryContext().isOffline();

        if (obj instanceof Process) {
            Process process = (Process) obj;
            category.add(EComponentCategory.MAIN);
            boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
            if (process.getComponentsType().equals(ComponentCategory.CATEGORY_4_DI.getName())) {
                category.add(EComponentCategory.EXTRA);
            }
            if (!isJoblet && process.getComponentsType().equals(ComponentCategory.CATEGORY_4_DI.getName())) {
                category.add(EComponentCategory.STATSANDLOGS);
            }
            if (allowVerchange) {
                category.add(EComponentCategory.VERSIONS);
            }
            if (!isJoblet) {
                category.add(EComponentCategory.DEPLOYMENT);
            }
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IHeaderFooterProviderService.class)) {
                IHeaderFooterProviderService headerFooterService = (IHeaderFooterProviderService) GlobalServiceRegister
                        .getDefault().getService(IHeaderFooterProviderService.class);
                if (headerFooterService.isVisible()) {
                    category.add(EComponentCategory.HEADERFOOTER);
                }
            }

            // if svn remote connection, added by nma
            if (svnService != null && svnService.isProjectInSvnMode() && !isOfflineMode) {
                category.add(EComponentCategory.SVNHISTORY);
            }

            if (gitService != null && gitService.isProjectInGitMode() && !isOfflineMode) {
                category.add(EComponentCategory.GITHISTORY);
            }

        } else if (obj instanceof IRepositoryViewObject) {
            category.add(EComponentCategory.MAIN);
            if (allowVerchange) {
                category.add(EComponentCategory.VERSIONS);
            }

            if (svnService != null && svnService.isProjectInSvnMode() && !isOfflineMode
                    && (((IRepositoryViewObject) obj).getRepositoryObjectType() == ERepositoryObjectType.JOBLET
                            || ERepositoryObjectType.getAllTypesOfProcess()
                                    .contains(((IRepositoryViewObject) obj).getRepositoryObjectType()))) {
                category.add(EComponentCategory.SVNHISTORY);
            } else if (gitService != null && gitService.isProjectInGitMode() && !isOfflineMode
                    && (((IRepositoryViewObject) obj).getRepositoryObjectType() == ERepositoryObjectType.JOBLET
                            || ERepositoryObjectType.getAllTypesOfProcess()
                                    .contains(((IRepositoryViewObject) obj).getRepositoryObjectType()))) {
                category.add(EComponentCategory.GITHISTORY);
            }
        } else if (obj instanceof IEditorPart) {
            if (CorePlugin.getDefault().getDiagramModelService().isBusinessDiagramEditor((IEditorPart) obj)) {
                category.add(EComponentCategory.MAIN);
                category.add(EComponentCategory.APPEARANCE);
                category.add(EComponentCategory.RULERS_AND_GRID);
                if (allowVerchange) {
                    category.add(EComponentCategory.VERSIONS);
                }
            }
        } else {
            BusinessType type = CorePlugin.getDefault().getDiagramModelService().getBusinessModelType(obj);
            if (BusinessType.SHAP.equals(type) || BusinessType.CONNECTION.equals(type)) {
                category.add(EComponentCategory.APPEARANCE);
                category.add(EComponentCategory.ASSIGNMENT);
            } else if (BusinessType.NOTE.equals(type)) {
                category.add(EComponentCategory.APPEARANCE);
            }
        }

        return category.toArray(new EComponentCategory[0]);
    }

    public Process getElement() {
        return (Process) element;
    }

    @Override
    public boolean isCleaned() {
        return this.cleaned;
    }

    @Override
    public void cleanDisplay() {
        setPartName(null);
        tabFactory.setInput(null);
        tabFactory.setTitle(null, null);
        if (tabFactory.getTabComposite() != null) {
            for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                curControl.setVisible(false);
                curControl.dispose();
            }
        }
        this.currentSelectedTab = null;
        this.element = null;
        this.cleaned = true;
        this.selectedPrimary = true;
        process = null;
    }

    @Override
    public void refresh() {
        refresh(false, null);
    }

    public void refresh(boolean force, Object obj) {
        if (force) {
            cleanDisplay();
        }
        final IEditorPart activeEditor = getSite().getPage().getActiveEditor();

        if (obj == null) {
            if (activeEditor != null && activeEditor instanceof AbstractMultiPageTalendEditor) {
                AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
                IProcess process = talendEditor.getProcess();
                if (process != null && process instanceof Element) {
                    this.selectedPrimary = true;
                    this.cleaned = force;
                    this.element = (Element) process;

                    // remove "Job" or "Joblet" from title
                    String title = activeEditor.getTitle();
                    if (title.startsWith(VIEW_NAME_JOBLET)) {
                        title = title.substring(VIEW_NAME_JOBLET.length() + 1);
                    } else if (title.startsWith(getViewNameLable())) {
                        title = title.substring(getViewNameLable().length() + 1);

                    }

                    setElement(element, title, null);
                    return;
                }
            } else {
                IDiagramModelService diagramModelService = CorePlugin.getDefault().getDiagramModelService();
                if (diagramModelService != null && diagramModelService.isBusinessDiagramEditor(activeEditor)) {
                    this.selectedPrimary = true;
                    this.cleaned = force;
                    IRepositoryViewObject object = retrieveBusiness(activeEditor);
                    if (object != null) {
                        String title = object.getLabel() + " " + object.getVersion(); //$NON-NLS-1$
                        Object type = object.getRepositoryObjectType();
                        setElement(activeEditor, type + SEPARATOR + title, null);
                    }
                    return;
                }
            }

        } else {

            this.selectedPrimary = true;
            this.cleaned = force;
            IRepositoryViewObject object = retrieveBusiness(activeEditor);
            if (object != null) {
                String title = object.getLabel() + " " + object.getVersion(); //$NON-NLS-1$
                Object type = object.getRepositoryObjectType();
                setElement(obj, type + SEPARATOR + title, null);
            }
            return;

        }

        cleanDisplay();

    }

    @Override
    public void setFocus() {
        this.parent.setFocus();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                CorePlugin.getDefault().getRepositoryService().removeRepositoryTreeViewListener(JobSettingsView.this);
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.
     * SelectionChangedEvent )
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof StructuredSelection) {
            Object input = ((IStructuredSelection) selection).getFirstElement();

            if (!(input instanceof RepositoryNode)) {
                if (input instanceof IAdaptable) {
                    // see ProcessPart.getAdapter()
                    IAdaptable adaptable = (IAdaptable) input;
                    input = adaptable.getAdapter(RepositoryNode.class);
                }
            }

            if (input instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) input;
                Object obj = repositoryNode.getProperties(EProperties.CONTENT_TYPE);

                String type = null;
                if (obj != null) {
                    type = obj.toString();
                    if (obj instanceof ERepositoryObjectType) {
                        ERepositoryObjectType objType = (ERepositoryObjectType) obj;
                        if (objType == ERepositoryObjectType.PROCESS) {
                            type = getViewNameLable();
                        } else if (objType == ERepositoryObjectType.JOBLET) {
                            type = VIEW_NAME_JOBLET;
                        }
                    }

                } else {
                    return;
                }

                IRepositoryViewObject repositoryObject = repositoryNode.getObject();
                if (repositoryObject == null) {
                    repositoryObject = new EmptyRepositoryObject();
                    return;
                }
                String title = repositoryObject.getLabel();
                if (allowVerchange) {
                    if (repositoryObject.getVersion() != null) {
                        title = repositoryObject.getLabel() + " " + repositoryObject.getVersion(); //$NON-NLS-1$
                    }
                }
                Image jobSettingImage = null;
                ERepositoryObjectType repositoryObjectType = repositoryNode.getObjectType();
                if (repositoryObjectType == ERepositoryObjectType.PROCESS_MR
                        || repositoryObjectType == ERepositoryObjectType.PROCESS_STORM) {
                    jobSettingImage = getImage(repositoryObject);
                }
                if (jobSettingImage == null) {
                    jobSettingImage = getImageFromFramework(repositoryObjectType);
                }
                if (jobSettingImage == null) {
                    jobSettingImage = ImageProvider.getImage(repositoryNode.getIcon());
                }

                setElement(repositoryObject, type + SEPARATOR + title, jobSettingImage);
            }
        }

    }

    private Image getImageFromFramework(ERepositoryObjectType itemType) {
        IGenericWizardService wizardService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
        if (wizardService != null && wizardService.isGenericType(itemType)) {
            return wizardService.getNodeImage(itemType.getType());
        }
        return null;
    }

    private Image getImage(IRepositoryViewObject repositoryObject) {
        if (repositoryObject == null) {
            return null;
        }
        Property property = repositoryObject.getProperty();
        if (property != null) {
            IImage image = CoreImageProvider.getIcon(property.getItem());
            if (image != null) {
                return ImageProvider.getImage(image);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.views.properties.IJobSettingsView#getSelection()
     */
    @Override
    public ISelection getSelection() {
        ISVNUiProviderService service = null;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            service = (ISVNUiProviderService) GlobalServiceRegister.getDefault().getService(ISVNUiProviderService.class);
        }
        if (currentSelectedTab == null) {
            return null;
        }
        IDynamicProperty dc = currentSelectedTab.getPropertyComposite();
        if (dc instanceof ProcessVersionComposite) {
            return ((ProcessVersionComposite) dc).getSelection();

        } else if (service != null && service.isSVNHistoryComposite(dc)) {
            return service.getSVNHistorySelection(dc);
        } else if (CorePlugin.getDefault().getDiagramModelService().isInstanceOfBusinessAssignmentComposite(dc)) {
            IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
            if (repositoryView != null) {
                return repositoryView.getViewer().getSelection();
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.views.properties.IJobSettingsView#refreshCurrentViewTab()
     */
    @Override
    public void refreshCurrentViewTab() {
        if (currentSelectedTab == null) {
            return;
        }
        IDynamicProperty dc = currentSelectedTab.getPropertyComposite();
        if (dc != null) {
            dc.refresh();
        }
    }

    public void setISelection(ISelection selection) {
        this.selectedModel = selection;
    }

    public TalendPropertyTabDescriptor getCurrentSelectedTab() {
        return this.currentSelectedTab;
    }

    @Override
    public void onPropertiesChanged(Map<String, Object> maps) {
        if (maps == null || maps.isEmpty()) {
            return;
        }
        Object obj = maps.get(IJobSettingsView.JOBTYPE_CHANGED);
        if (obj instanceof IRepositoryViewObject) {
            String type = null;
            IRepositoryViewObject repositoryObject = (IRepositoryViewObject) obj;
            // opened job needn't to change the title
            if (!(obj instanceof IProcess2)) {
                Property property = repositoryObject.getProperty();
                if (property != null) {
                    Item item = property.getItem();
                    if (item != null) {
                        ERepositoryObjectType repositoryObjectType = ERepositoryObjectType.getItemType(item);
                        if (repositoryObjectType == ERepositoryObjectType.PROCESS) {
                            type = getViewNameLable();
                        } else if (repositoryObjectType != null) {
                            type = repositoryObjectType.getLabel();
                        }
                    }
                }
            }
            Image image = getImage(repositoryObject);
            if (image != null && image.isDisposed()) {
                image = null;
            }
            if (type == null) {
                super.setTitleImage(image);
                tabFactory.setTitleImage(image);
            } else {
                String title = repositoryObject.getLabel();
                if (allowVerchange) {
                    if (repositoryObject.getVersion() != null) {
                        title = repositoryObject.getLabel() + " " + repositoryObject.getVersion(); //$NON-NLS-1$
                    }
                }
                setPartName(type + SEPARATOR + title, image);
            }
        }
    }

}
