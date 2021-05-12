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
package org.talend.designer.core.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.rulers.IColumnSupport;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.metadata.builder.connection.Properties;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.IRepositoryWorkUnitListener;
import org.talend.core.model.repository.job.JobResourceManager;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.model.utils.AccessingEmfJob;
import org.talend.core.repository.constants.Constant;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.core.runtime.repository.item.ItemProductKeys;
import org.talend.core.runtime.util.ItemDateParser;
import org.talend.core.services.ICreateXtextProcessService;
import org.talend.core.services.ISVNProviderService;
import org.talend.core.services.IUIRefresher;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.ILastVersionChecker;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.core.ui.process.UpdateRunJobComponentContextHelper;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.properties.tab.TalendPropertyTabDescriptor;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.core.views.IComponentSettingsView;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.CodeEditorFactory;
import org.talend.designer.core.ui.editor.TalendJavaEditor;
import org.talend.designer.core.ui.editor.dependencies.JobDependenciesEditor;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletUtil;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.outline.NodeTransferDragSourceListener;
import org.talend.designer.core.ui.editor.outline.NodeTreeEditPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.views.contexts.ContextsView;
import org.talend.designer.core.ui.views.jobsettings.JobSettingsView;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.maven.utils.MavenProjectUtils;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.ui.views.IJobSettingsView;
import org.talend.themes.core.elements.interfaces.ITalendThemeChangeListener;
import org.talend.themes.core.elements.utils.TalendThemeUtils;

/**
 * DOC qzhang class global comment. Detailled comment
 */
public abstract class AbstractMultiPageTalendEditor extends MultiPageEditorPart implements IResourceChangeListener,
        ISelectionListener, IUIRefresher, IMultiPageTalendEditor, ITalendThemeChangeListener {

    public static final String DISPLAY_CODE_VIEW = "DISPLAY_CODE_VIEW"; //$NON-NLS-1$

    public static final String CSS_CLASS_ID = "org-talend-rcp-abstractMultiPageEditor-footer"; //$NON-NLS-1$

    private boolean isCheckout = false;

    protected AdapterImpl dirtyListener = new AdapterImpl() {

        @Override
        public void notifyChanged(Notification notification) {
            if (notification.getEventType() != Notification.REMOVING_ADAPTER
                    && notification.getEventType() != Notification.RESOLVE) {
                int featureID = notification.getFeatureID(Properties.class);
                if (featureID == PropertiesPackage.PROPERTY__MODIFICATION_DATE
                        || featureID == PropertiesPackage.PROPERTY__CREATION_DATE) {
                    if (notification.getNewValue() == null || notification.getOldValue() == null
                            || notification.getOldValue() != null
                            && notification.getOldValue().equals(notification.getNewValue())) {
                        // no date or same date
                        return;
                    }
                } else if (featureID == PropertiesPackage.PROPERTY__INFORMATIONS) {
                    // ignore
                    return;
                } else if (featureID == PropertiesPackage.PROPERTY__MAX_INFORMATION_LEVEL) {
                    if (notification.getOldValue() != null && notification.getOldValue().equals(notification.getNewValue())) {
                        return; // nothing to do
                    }
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {

                            updateTitleImage();
                        }
                    });
                    return;
                }

                if (Display.getCurrent() != null) {
                    propertyIsDirty = true;
                    firePropertyChange(IEditorPart.PROP_DIRTY);
                }
            }
        }
    };

    protected IRepositoryWorkUnitListener repositoryWorkListener = new IRepositoryWorkUnitListener() {

        @Override
        public void workUnitFinished() {
            revisionChanged = true;
            Display display = DisplayUtils.getDisplay();
            if (display != null) {
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        setName();
                    }
                });
            }
        }
    };

    protected boolean propertyIsDirty = false;

    protected AbstractDecoratedTextEditor codeEditor;

    protected IProcessor processor;

    protected String oldJobName;

    protected boolean keepPropertyLocked; // used only if the user try to open more than one editor at a time.

    protected JobEditorInput processEditorInput;

    protected AbstractTalendEditor designerEditor;

    private AbstractDecoratedTextEditor jobletEditor;

    private JobDependenciesEditor dependenciesEditor;

    protected List propertyInformation;

    protected boolean useCodeView = true;

    public boolean revisionChanged = false;

    public String revisionNumStr = null;

    protected static ISVNProviderService svnProviderService = null;

    static {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ISVNProviderService.class)) {
            svnProviderService = GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
        }
    }

    @Override
    public void changePaletteComponentHandler() {
        ComponentsFactoryProvider.getInstance().setComponentsHandler(designerEditor.getComponenentsHandler());
    }

    private final IPartListener partListener = new IPartListener() {

        @Override
        public void partOpened(IWorkbenchPart part) {
            if (part == AbstractMultiPageTalendEditor.this) {
                IProcess2 process = getProcess();
                if (process.getEditor() == null) {
                    ((Process) process).setEditor(AbstractMultiPageTalendEditor.this);
                }
                refreshTestContaierView();
            }
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
            if (part == AbstractMultiPageTalendEditor.this) {
                savePropertyIfNeededForErrorStatus();
                IProject currentProject;
                try {
                    currentProject = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
                    String jobScriptVersion = "";
                    if (getEditorInput() != null && getEditorInput() instanceof RepositoryEditorInput) {
                        Item item = ((RepositoryEditorInput) getEditorInput()).getItem();
                        if (item != null) {
                            Property property = item.getProperty();
                            if (property != null) {
                                jobScriptVersion = "_" + property.getVersion();
                            }
                        }
                    }
                    IFile file = currentProject.getFolder("temp").getFile(
                            getEditorInput().getName() + jobScriptVersion + "_job" + ".jobscript");
                    if (file.exists()) {
                        file.delete(true, null);
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
                changeContextsViewStatus(true);
                refreshTestContaierView();
            }
        }

        @Override
        public void partActivated(IWorkbenchPart part) {
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
        }

    };

    private ServiceRegistration lockService;

    private IPropertyListener propertyListener = null;

    // only convert process and jobscript when select between designer and jboscript page.
    private int oldPageIndex = 0;

    /**
     * DOC hcw Comment method "restorePropertyInformation".
     */
    protected void savePropertyIfNeededForErrorStatus() {
        if (designerEditor.isReadOnly()) {
            return;
        }
        Property property = processEditorInput.getItem().getProperty();
        if (propertyInformation != null && !CollectionUtils.isEqualCollection(propertyInformation, property.getInformations())) {
            Problems.computePropertyMaxInformationLevel(property, true);
        }
    }

    protected String getContextPerspectiveID() {
        return "org.talend.rcp.perspective";
    }

    public AbstractMultiPageTalendEditor() {
        super();

        ActiveProcessTracker.initialize();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        IBrandingService brandingService = GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        Map<String, Object> settings = brandingService.getBrandingConfiguration().getJobEditorSettings();
        if (settings.containsKey(DISPLAY_CODE_VIEW)) {
            useCodeView = (Boolean) settings.get(DISPLAY_CODE_VIEW);
        }
    }

    @Override
    public boolean isDirty() {
        return propertyIsDirty || super.isDirty();
    }

    public void setReadOnly(boolean readonly) {
        designerEditor.setReadOnly(readonly);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(final IEditorSite site, IEditorInput editorInput) throws PartInitException {
        setSite(site);
        setInput(editorInput);
        if (!(editorInput instanceof JobEditorInput)) {
            return;
        }
        site.setSelectionProvider(new MultiPageTalendSelectionProvider(this));
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);

        // Lock the process :
        IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
        final IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        processEditorInput = (JobEditorInput) editorInput;
        final IProcess2 currentProcess = processEditorInput.getLoadedProcess();
        if (!currentProcess.isReadOnly()) {
            try {
                Property property = processEditorInput.getItem().getProperty();
                propertyInformation = new ArrayList(property.getInformations());
                property.eAdapters().add(dirtyListener);
                repFactory.lock(currentProcess);
                boolean locked = repFactory.getStatus(currentProcess) == ERepositoryStatus.LOCK_BY_USER;
                if (!locked) {
                    setReadOnly(true);
                }
                revisionChanged = true;
            } catch (PersistenceException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            } catch (BusinessException e) {
                // Nothing to do
                ExceptionHandler.process(e);
            }
        } else {
            setReadOnly(true);
            Bundle bundle = FrameworkUtil.getBundle(AbstractMultiPageTalendEditor.class);
            final Display display = getSite().getShell().getDisplay();
            this.lockService = bundle.getBundleContext().registerService(
                    EventHandler.class.getName(),
                    new EventHandler() {

                        @Override
                        public void handleEvent(Event event) {
                            String lockTopic = Constant.REPOSITORY_ITEM_EVENT_PREFIX + Constant.ITEM_LOCK_EVENT_SUFFIX;
                            if (lockTopic.equals(event.getTopic())) {
                                Object o = event.getProperty(Constant.ITEM_EVENT_PROPERTY_KEY);
                                if (o != null && o instanceof Item) {
                                    Item item = (Item) o;
                                    String itemId = item.getProperty().getId();
                                    if (itemId.equals(currentProcess.getId())) {
                                        if (currentProcess.isReadOnly()) {
                                            boolean readOnly = currentProcess.checkReadOnly();
                                            boolean orginalReadOnlyStatus = designerEditor.isReadOnly();
                                            setReadOnly(readOnly);
                                            if (!readOnly) {
                                                display.asyncExec(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        setFocus();
                                                    }
                                                });

                                                if (orginalReadOnlyStatus == true) {
                                                    // refresh to the given item version, nomally it is the latest
                                                    // version,
                                                    // means the editor/process will be refreshed to the latest version
                                                    refreshProcess(item, false);
                                                    ProxyRepositoryFactory.getInstance().fireRepositoryPropertyChange(
                                                            "view_refresh", null, item); //$NON-NLS-1$
                                                }

                                                Property property = processEditorInput.getItem().getProperty();
                                                propertyInformation = new ArrayList(property.getInformations());
                                                property.eAdapters().add(dirtyListener);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    new Hashtable<String, String>(Collections.singletonMap(EventConstants.EVENT_TOPIC,
                            Constant.REPOSITORY_ITEM_EVENT_PREFIX + "*"))); //$NON-NLS-1$
            revisionChanged = true;
        }
        // setTitleImage(ImageProvider.getImage(getEditorTitleImage()));
        updateTitleImage(processEditorInput.getItem().getProperty());
        getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);

    }

    /**
     * Refresh the editor/process to the given refreshedItem version
     *
     * @param refreshedItem
     * @param force
     */
    private void refreshProcess(final Item refreshedItem, boolean force) {
        Item currentItem = processEditorInput.getItem();
        if (isStorageVersionChanged(refreshedItem, currentItem) || force) {
            processEditorInput.setItem(refreshedItem);
            final IProcess2 process = processEditorInput.getLoadedProcess();
            getSite().getShell().getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    process.setProperty(refreshedItem.getProperty());
                    process.updateProperties();
                    ProcessType processType = null;
                    if (refreshedItem instanceof ProcessItem) {
                        processType = ((ProcessItem) refreshedItem).getProcess();
                    } else if (refreshedItem instanceof JobletProcessItem) {
                        processType = ((JobletProcessItem) refreshedItem).getJobletProcess();
                    } else {
                        ExceptionHandler.process(new Exception("Mismatched case")); //$NON-NLS-1$
                    }
                    if (processType != null) {
                        ((Process) process).updateProcess(processType);
                    }
                    process.refreshProcess();
                    revisionChanged = true;
                    setName();
                }
            });
        }
    }

    private boolean isStorageVersionChanged(Item refreshedItem, Item currentItem) {
        boolean isChanged = false;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            if (svnProviderService != null && svnProviderService.isProjectInSvnMode()) {
                Process refreshedProcess = new Process(refreshedItem.getProperty());
                String refreshedNumStr = svnProviderService.getCurrentSVNRevision(refreshedProcess);
                refreshedProcess.dispose();
                if (refreshedNumStr != null) {
                    refreshedNumStr = getFormattedRevisionNumStr(refreshedNumStr);
                    if (!refreshedNumStr.equals(revisionNumStr)) {
                        isChanged = true;
                    }
                }
            }
        } else {
            Date currentModifiedDate = ItemDateParser.parseAdditionalDate(currentItem.getProperty(),
                    ItemProductKeys.DATE.getModifiedKey());
            Date refreshedModifiedDate = ItemDateParser.parseAdditionalDate(refreshedItem.getProperty(),
                    ItemProductKeys.DATE.getModifiedKey());
            isChanged = currentModifiedDate != null && !currentModifiedDate.equals(refreshedModifiedDate);
        }
        return isChanged;
    }

    protected String getFormattedRevisionNumStr(String revisionNum) {
        return ".r" + revisionNum; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.editor.INameRefresher#refreshName()
     */
    @Override
    public void refreshName() {
        try {
            JobResourceManager jobResourceManager = JobResourceManager.getInstance();
            jobResourceManager.removeProtection(designerEditor);
            for (String id : designerEditor.getProtectedIds()) {
                if (designerEditor.getJobResource(id).getJobInfo().getJobName().equalsIgnoreCase(oldJobName)) {
                    // delete only the job renamed
                    jobResourceManager.deleteResource(designerEditor.getJobResource(id));
                }
            }
            designerEditor.resetJobResources();

            setName();
            JobInfo jobInfo = designerEditor.getCurrentJobResource().getJobInfo();
            if (jobInfo != null) {
                jobInfo.setJobName(getEditorInput().getName());
            }
            jobResourceManager.addProtection(designerEditor);

            processor.initPath();
            processor.setProcessorStates(IProcessor.STATES_EDIT);

            // modified by wzhang to fix bug 8180 in thales branding.
            if (useCodeView) {
                updateCodeEditorContent();
            }

            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (activeWorkbenchWindow != null) {
                if (activeWorkbenchWindow.getActivePage().isPartVisible(this)) {
                    new ActiveProcessTracker().partBroughtToTop(this);
                    DesignerPlugin.getDefault().getRunProcessService().refreshView();
                }
            }

        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    private void updateCodeEditorContent() {
        if (!(processor.getProperty().getItem() instanceof JobletProcessItem)) {
            FileEditorInput input = createFileEditorInput();
            codeEditor.setInput(input);
        }
    }

    /**
     * DOC bqian Comment method "selectNode".
     *
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void selectNode(Node node) {
        GraphicalViewer viewer = designerEditor.getViewer();
        Object object = viewer.getRootEditPart().getChildren().get(0);
        if (object instanceof ProcessPart) {
            // the structure in memory is:
            // ProcessPart < SubjobContainerPart < NodeContainerPart < NodePart
            for (EditPart editPart : (List<EditPart>) ((ProcessPart) object).getChildren()) {
                if (editPart instanceof SubjobContainerPart) {
                    SubjobContainerPart subjobPart = (SubjobContainerPart) editPart;
                    for (EditPart part : (List<EditPart>) subjobPart.getChildren()) {
                        if (part instanceof NodeContainerPart) {
                            EditPart nodePart = (EditPart) part.getChildren().get(0);
                            if (nodePart instanceof NodePart) {
                                if (((NodePart) nodePart).getModel().equals(node)) {
                                    viewer.select(nodePart);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC bqian Comment method "selectNode".
     *
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void selectNode(String nodeName) {
        GraphicalViewer viewer = designerEditor.getViewer();
        Object object = viewer.getRootEditPart().getChildren().get(0);
        if (object instanceof ProcessPart) {
            // the structure in memory is:
            // ProcessPart < SubjobContainerPart < NodeContainerPart < NodePart
            for (EditPart editPart : (List<EditPart>) ((ProcessPart) object).getChildren()) {
                if (editPart instanceof SubjobContainerPart) {
                    SubjobContainerPart subjobPart = (SubjobContainerPart) editPart;
                    for (EditPart part : (List<EditPart>) subjobPart.getChildren()) {
                        if (part instanceof NodeContainerPart) {
                            EditPart nodePart = (EditPart) part.getChildren().get(0);
                            if (nodePart instanceof NodePart) {
                                if (((Node) ((NodePart) nodePart).getModel()).getLabel().equals(nodeName)) {
                                    viewer.select(nodePart);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public EditPart getSelectedNodePart(String nodeName) {
        GraphicalViewer viewer = designerEditor.getViewer();
        Object object = viewer.getRootEditPart().getChildren().get(0);
        if (object instanceof ProcessPart) {
            // the structure in memory is:
            // ProcessPart < SubjobContainerPart < NodeContainerPart < NodePart
            for (EditPart editPart : (List<EditPart>) ((ProcessPart) object).getChildren()) {
                if (editPart instanceof SubjobContainerPart) {
                    SubjobContainerPart subjobPart = (SubjobContainerPart) editPart;
                    for (EditPart part : (List<EditPart>) subjobPart.getChildren()) {
                        if (part instanceof NodeContainerPart) {
                            EditPart nodePart = (EditPart) part.getChildren().get(0);
                            if (nodePart instanceof NodePart) {
                                if (((Node) ((NodePart) nodePart).getModel()).getLabel().equals(nodeName)) {
                                    return nodePart;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void changeContextsViewStatus(boolean flag) {
        IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (workbenchPage != null) {
            IViewPart view = workbenchPage.findView(ContextsView.CTX_ID_DESIGNER);
            if (view != null) {
                ContextsView contextsView = (ContextsView) view;
                contextsView.getContextViewComposite().setTabEnable(flag);
                contextsView.getContextViewComposite().getContextTableComposite().refresh();
            }
        }
    }

    /**
     * Calculates the contents of page 2 when the it is activated.
     */
    @Override
    protected void pageChange(final int newPageIndex) {
        super.pageChange(newPageIndex);
        setName();
        if (newPageIndex == 1) {
            turnToCodePage(newPageIndex);
        } else if (newPageIndex == 0 && (jobletEditor == getEditor(oldPageIndex))) {
            covertJobscriptOnPageChange();
            changeContextsViewStatus(true);
        } else if (jobletEditor == getEditor(newPageIndex)) {
            turnToJobScriptPage(newPageIndex);
        }
        oldPageIndex = getActivePage();
    }

    protected void turnToJobScriptPage(int newPageIndex) {
        if (jobletEditor != getEditor(newPageIndex)) {
            return;
        }

        ICreateXtextProcessService convertJobtoScriptService = CorePlugin.getDefault().getCreateXtextProcessService();

        try {
            final String scriptValue = convertJobtoScriptService.convertJobtoScript(getProcess().saveXmlFile());
            IFile file = (IFile) jobletEditor.getEditorInput().getAdapter(IResource.class);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(scriptValue.getBytes());
            if (file.exists()) {
                jobletEditor.getDocumentProvider().getDocument(jobletEditor.getEditorInput()).set(scriptValue);
                boolean isProcessReadOnly = ((JobEditorInput) getEditor(0).getEditorInput()).isReadOnly();

                IProxyRepositoryFactory rFactory = ProxyRepositoryFactory.getInstance();
                if (isProcessReadOnly || rFactory.isUserReadOnlyOnCurrentProject()) {
                    IDocumentProvider provider = jobletEditor.getDocumentProvider();
                    Class p = provider.getClass();
                    Class[] type = new Class[1];
                    type[0] = Boolean.TYPE;
                    Object[] para = new Object[1];
                    para[0] = Boolean.TRUE;
                    Method method = p.getMethod("setReadOnly", type);
                    method.invoke(provider, para);
                }

                IAction action = jobletEditor.getAction("FoldingRestore"); //$NON-NLS-1$
                action.run();
                jobletEditor.doSave(null);
            } else {
                file.create(byteArrayInputStream, true, null);
            }
            if (propertyListener == null) {
                propertyListener = new IPropertyListener() {

                    @Override
                    public void propertyChanged(Object source, int propId) {
                        if (source instanceof IEditorPart && ((IEditorPart) source).isDirty()) {
                            getProcess().setProcessModified(true);
                            getProcess().setNeedRegenerateCode(true);
                        }
                    }

                };
                jobletEditor.addPropertyListener(propertyListener);
            }

        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } catch (SecurityException e) {
            ExceptionHandler.process(e);
        } catch (NoSuchMethodException e) {
            ExceptionHandler.process(e);
        } catch (IllegalArgumentException e) {
            ExceptionHandler.process(e);
        } catch (IllegalAccessException e) {
            ExceptionHandler.process(e);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        }
        changeContextsViewStatus(false);

    }

    protected void turnToCodePage(int newPageIndex) {
        // TDI-25866:In case select a component and switch to the code page,need clean its componentSetting view
        IComponentSettingsView viewer = (IComponentSettingsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().findView(IComponentSettingsView.ID);

        if (viewer != null) {
            viewer.cleanDisplay();
        }
        if (codeEditor instanceof ISyntaxCheckableEditor) {
            moveCursorToSelectedComponent();

            /*
             * Belowing method had been called at line 331 within the generateCode method, as soon as code generated.
             */
            // ((ISyntaxCheckableEditor) codeEditor).validateSyntax();
        }

        codeSync();
        // updateCodeEditorContent();
        changeContextsViewStatus(true);
    }

    private void covertJobscriptOnPageChange() {
        try {
            boolean isDirty = jobletEditor.isDirty();
            jobletEditor.doSave(null);
            IProcess2 oldProcess = getProcess();

            ICreateXtextProcessService n = CorePlugin.getDefault().getCreateXtextProcessService();
            Item item = oldProcess.getProperty().getItem();
            ProcessType processType = null;
            if (item instanceof ProcessItem) {
                processType = n.convertDesignerEditorInput(((IFile) jobletEditor.getEditorInput().getAdapter(IResource.class))
                        .getLocation().toOSString(), oldProcess.getProperty());
            } else if (item instanceof JobletProcessItem) {
                processType = n.convertJobletDesignerEditorInput(
                        ((IFile) jobletEditor.getEditorInput().getAdapter(IResource.class)).getLocation().toOSString(),
                        oldProcess.getProperty());
            }
            if (item instanceof ProcessItem) {
                ((Process) oldProcess).updateProcess(processType);
                if (processType.getParameters() != null && processType.getParameters().getRoutinesParameter() != null) {
                    List<RoutinesParameterType> routineTypes = processType.getParameters().getRoutinesParameter();
                    if (routineTypes.stream().anyMatch(r -> r.getType() != null)) {
                        try {
                            MavenProjectUtils.updateMavenProject(new NullProgressMonitor(),
                                    IRunProcessService.get().getTalendJobJavaProject(item.getProperty()).getProject());
                        } catch (CoreException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            } else if (item instanceof JobletProcessItem) {
                ((Process) oldProcess).updateProcess(processType);
            }
            oldProcess.getUpdateManager().updateAll();
            designerEditor.setDirty(isDirty);
            List<Node> nodes = (List<Node>) oldProcess.getGraphicalNodes();
            List<Node> newNodes = new ArrayList<Node>();
            newNodes.addAll(nodes);
            Set<IProcess> processSet = new HashSet<IProcess>();
            for (Node node : newNodes) {
                if (processSet.add(node.getProcess())) {
                    node.getProcess().checkStartNodes();
                }
                node.checkAndRefreshNode();
                IElementParameter ep = node.getElementParameter("ACTIVATE");
                if (ep != null && ep.getValue().equals(Boolean.FALSE)) {
                    node.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
                    node.setPropertyValue(EParameterName.ACTIVATE.getName(), false);
                } else if (ep != null && ep.getValue().equals(Boolean.TRUE)) {
                    node.setPropertyValue(EParameterName.ACTIVATE.getName(), false);
                    node.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
                }
                for (IElementParameter param : node.getElementParameters()) {
                    if (!param.getChildParameters().isEmpty()) {
                        if (param.getValue() != null && param.getValue() instanceof String
                                && ((String) param.getValue()).contains(":")) {
                            String splited[] = ((String) param.getValue()).split(":");
                            String childNameNeeded = splited[0].trim();
                            String valueChild = TalendQuoteUtils.removeQuotes(splited[1].trim());
                            if (param.getChildParameters().containsKey(childNameNeeded)) {
                                param.getChildParameters().get(childNameNeeded).setValue(valueChild);
                            }
                        }
                    }
                }
                if (node.getNodeContainer() instanceof JobletContainer) {
                    JobletContainer jc = (JobletContainer) node.getNodeContainer();
                    if (node.isMapReduceStart()) {
                        jc.updateState("UPDATE_STATUS", "", jc.getPercentMap(), jc.getPercentReduce()); //$NON-NLS-1$
                    }
                }
            }
        } catch (PersistenceException e) {
        }
    }

    /**
     * Move Cursor to Selected Node.
     *
     * @param processor
     */
    private void moveCursorToSelectedComponent() {
        String nodeName = getSelectedNodeName();
        if (nodeName != null) {
            if (codeEditor instanceof TalendJavaEditor) {
                ((TalendJavaEditor) codeEditor).placeCursorTo(nodeName);
            }
        }
    }

    public void setName() {
        if (getEditorInput() == null) {
            return;
        }
        String label = getEditorInput().getName();
        oldJobName = label;
    }

    public void setName(String revisionNum) {
        setName();
        String label = getEditorInput().getName();
        String jobVersion = this.getProcess().getVersion();
        setPartName(Messages.getString("MultiPageTalendEditor.Job", label, jobVersion) + revisionNum); //$NON-NLS-1$
        revisionNumStr = revisionNum;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
     */
    @Override
    protected void createPages() {
        createPage0();
        createPage1();
        createPage2();

        if (getPageCount() == 1) {
            Composite container = getContainer();
            if (container instanceof CTabFolder) {
                ((CTabFolder) container).setTabHeight(0);
            }
        }

    }

    protected void setCSSStylingClass() {
        Composite container = getContainer();
        if (container instanceof CTabFolder) {
            CoreUIPlugin.setCSSClass(container, CSS_CLASS_ID);
        }
    }

    @Override
    public void onThemeChanging(String newThemeId, String oldThemeId) {
        if (newThemeId == null) {
            return;
        }
        if (TalendThemeUtils.isThemeFromTalend(newThemeId)) {
            return;
        }
        Composite container = getContainer();
        if (container instanceof CTabFolder) {
            ((CTabFolder) container).setBackground(null, null, true);
            ((CTabFolder) container).setSelectionBackground(null, null, true);
        }
    }

    protected void createPage0() {
        try {
            int index = addPage(designerEditor, getEditorInput());
            setPageText(index, "Designer"); //$NON-NLS-1$
            designerEditor.setParent(this);
        } catch (PartInitException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        setCSSStylingClass();
    }

    /**
     * Creates page 1 of the multi-page editor, which allows you to change the font used in page 2.
     */
    protected void createPage1() {
        IProcess2 process = getProcess();
        codeEditor = CodeEditorFactory.getInstance().getCodeEditor(getCurrentLang(), process);
        processor = ProcessorUtilities.getProcessor(process, process.getProperty(), process.getContextManager()
                .getDefaultContext());
        if (!process.isReadOnly()) {
            MavenProjectUtils.enableMavenNature(new NullProgressMonitor(), processor.getCodeProject());
        }
        if (processor instanceof IJavaBreakpointListener) {
            JDIDebugModel.addJavaBreakpointListener((IJavaBreakpointListener) processor);
        }

        processor.setProcessorStates(IProcessor.STATES_EDIT);
        if (codeEditor instanceof ISyntaxCheckableEditor) {
            processor.setSyntaxCheckableEditor((ISyntaxCheckableEditor) codeEditor);
        }

        if (useCodeView) {
            try {

                IEditorInput editorInput = createFileEditorInput();
                if (!(process.getProperty().getItem() instanceof ProcessItem)) { // shouldn't work for joblet
                    editorInput = new JobletCodeEditInput();
                }
                int index = addPage(codeEditor, editorInput);
                // init Syntax Validation.
                setPageText(index, "Code"); //$NON-NLS-1$

            } catch (PartInitException pie) {
                // pie.printStackTrace();
                ExceptionHandler.process(pie);
            }
        }

        if (DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.GENERATE_CODE_WHEN_OPEN_JOB)) {
            generateCode();
        }
    }

    // create jobscript editor
    protected void createPage2() {
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(ICreateXtextProcessService.class)) {
            return;
        }
        String scriptValue = "";
        try {
            IProject currentProject = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            String jobScriptVersion = "";
            if (getEditorInput() != null && getEditorInput() instanceof RepositoryEditorInput) {
                Item item = ((RepositoryEditorInput) getEditorInput()).getItem();
                if (item != null) {
                    Property property = item.getProperty();
                    if (property != null) {
                        jobScriptVersion = "_" + property.getVersion();
                    }
                }
            }
            IFile file = currentProject.getFolder("temp").getFile(
                    getEditorInput().getName() + jobScriptVersion + "_job" + ".jobscript");

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(scriptValue.getBytes());
            if (file.exists()) {
                file.delete(true, null);
                file.create(byteArrayInputStream, true, null);
                file.setContents(byteArrayInputStream, 0, null);
            } else {
                file.create(byteArrayInputStream, true, null);
            }

            String pointId = "org.talend.metalanguage.jobscript.JobScriptForMultipage";
            // the way to get the xtextEditor programmly
            IEditorInput editorInput = new FileEditorInput(file);

            IExtensionPoint ep = RegistryFactory.getRegistry().getExtensionPoint("org.eclipse.ui.editors");
            IExtension[] extensions = ep.getExtensions();
            IExtension ex;
            IConfigurationElement confElem = null;
            for (IExtension extension : extensions) {
                ex = extension;
                if (ex.getContributor().getName().equals("org.talend.metalanguage.jobscript.ui")) {
                    for (IConfigurationElement c : ex.getConfigurationElements()) {

                        if (c.getName().equals("editor") && c.getAttribute("id").equals(pointId)) {
                            confElem = c;
                            break;
                        }
                    }
                }
            }

            if (confElem != null) {
                jobletEditor = (AbstractDecoratedTextEditor) confElem.createExecutableExtension("class");

                if (jobletEditor != null) {
                    int index = addPage(jobletEditor, editorInput);
                    setPageText(index, "Jobscript");
                }
            }
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC bqian Comment method "generateCode".
     */
    protected void generateCode() {
        final IProcess2 process = getProcess();
        if (!(process.getProperty().getItem() instanceof ProcessItem)) { // shouldn't work for joblet
            return;
        }
        if (process.getGeneratingNodes().size() != 0) {
            Job job = new AccessingEmfJob("Generating code") { //$NON-NLS-1$

                @Override
                protected IStatus doRun(IProgressMonitor monitor) {
                    try {
                        ProcessorUtilities.generateCode(process, process.getContextManager().getDefaultContext(), false, false,
                                true, ProcessorUtilities.GENERATE_WITH_FIRST_CHILD);
                    } catch (ProcessorException e) {
                        ExceptionHandler.process(e);
                    }

                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.setPriority(Job.BUILD);
            job.schedule(); // start as soon as possible
        }
    }

    /**
     * Creates page 1 of the multi-page editor, which allows you to change the font used in page 2.
     */

    /**
     * get the current project generating code language.
     *
     * @return the current generating code language
     */
    private ECodeLanguage getCurrentLang() {
        return ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getProject()
                .getLanguage();
    }

    /**
     * Saves the multi-page editor's document.
     */
    @Override
    public void doSave(final IProgressMonitor monitor) {
        Item curItem = getProcess().getProperty().getItem();
        IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        try {
            // For TDI-23825, if not lock by user try to lock again.
            if (!getProcess().isReadOnly()) {
                repFactory.lock(curItem);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        if (getProcess().isReadOnly() || repFactory.isUserReadOnlyOnCurrentProject()) {
            MessageDialog.openWarning(getEditor(0).getEditorSite().getShell(),
                    Messages.getString("AbstractMultiPageTalendEditor.readonly"),
                    Messages.getString("AbstractMultiPageTalendEditor.readonlyMessage"));
            return;
        }
        ERepositoryStatus status = repFactory.getStatus(curItem);
        if (!status.equals(ERepositoryStatus.LOCK_BY_USER) && !repFactory.getRepositoryContext().isEditableAsReadOnly()) {
            MessageDialog.openWarning(getEditor(0).getEditorSite().getShell(),
                    Messages.getString("AbstractMultiPageTalendEditor.canNotSaveTitle"),
                    Messages.getString("AbstractMultiPageTalendEditor.canNotSaveMessage"));
            return;
        }
        if (!isDirty()) {
            return;
        }

        Map<String, Boolean> jobletMap = new HashMap<String, Boolean>();
        changeCollapsedState(true, jobletMap);
        updateRunJobContext();
        designerEditor.getProcess().getProperty().eAdapters().remove(dirtyListener);
        repFactory.addRepositoryWorkUnitListener(repositoryWorkListener);

        if (jobletEditor == getActiveEditor()) {
            boolean isDirty = jobletEditor.isDirty();
            refreshPropertyDirtyStatus();
            jobletEditor.doSave(monitor);
            try {
                IProcess2 oldProcess = getProcess();

                ICreateXtextProcessService n = CorePlugin.getDefault().getCreateXtextProcessService();
                ProcessType processType = n.convertDesignerEditorInput(
                        ((IFile) jobletEditor.getEditorInput().getAdapter(IResource.class)).getLocation().toOSString(),
                        oldProcess.getProperty());

                IProcess2 newProcess = null;
                Item item = getProcess().getProperty().getItem();

                if (item instanceof ProcessItem) {
                    ((Process) designerEditor.getProcess()).updateProcess(processType);
                    if (isDirty) {
                        getProcess().setProcessModified(true);
                        getProcess().setNeedRegenerateCode(true);
                    }
                } else if (item instanceof JobletProcessItem) {
                    AbstractProcessProvider processProvider = AbstractProcessProvider
                            .findProcessProviderFromPID(IComponent.JOBLET_PID);
                    if (processProvider != null) {
                        newProcess = processProvider.buildNewGraphicProcess(item);
                    }
                    designerEditor.setProcess(newProcess);

                    Boolean lastVersion = null;
                    if (oldProcess instanceof ILastVersionChecker) {
                        lastVersion = ((ILastVersionChecker) oldProcess).isLastVersion(item);
                    }

                    if (designerEditor.getEditorInput() instanceof JobEditorInput) {
                        ((JobEditorInput) designerEditor.getEditorInput()).checkInit(lastVersion, null, true);
                    }
                }
                getEditor(0).doSave(monitor);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        } else {
            refreshPropertyDirtyStatus();
            getEditor(0).doSave(monitor);
        }
        /*
         * refresh should be executed before add the listener,or it will has eProxy on the property,it will cause a
         * editor dirty problem. hywang commet bug 17357
         */
        if (processEditorInput != null) {
            propertyInformation = new ArrayList(processEditorInput.getItem().getProperty().getInformations());
            propertyIsDirty = false;
        }
        if (designerEditor != null && dirtyListener != null) {
            designerEditor.getProcess().getProperty().eAdapters().add(dirtyListener);
        }

        refreshJobSettingsView();
        changeCollapsedState(false, jobletMap);

        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null) {
                testContainerService.updateDetect(getProcess(), false);

            }
        }

        if (isCheckout) {
            CommandStack stack = (CommandStack) getAdapter(CommandStack.class);
            stack.flush();
            isCheckout = false;
        }
    }

    public boolean haveDirtyJoblet() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
            IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            for (INode node : getProcess().getGraphicalNodes()) {
                if ((node instanceof Node) && ((Node) node).isJoblet()) {
                    if (service != null) {
                        if (service.jobletIsDirty(node)) {
                            MessageDialog.openWarning(this.getContainer().getShell(),
                                    Messages.getString("MultiPageTalendEditor.DIRTY"), node.getComponent().getName() //$NON-NLS-1$
                                            + Messages.getString("MultiPageTalendEditor.DIRTYMESSAGE")); //$NON-NLS-1$
                            return true;
                        }

                    }
                }
            }
        }
        return false;
    }

    private void changeCollapsedState(boolean state, Map<String, Boolean> map) {
        List<? extends INode> nodeList = getProcess().getGraphicalNodes();
        for (INode node : nodeList) {
            if (node instanceof Node) {
                NodeContainer nc = ((Node) node).getNodeContainer();
                if ((nc instanceof AbstractJobletContainer) && nc.getNode().isJoblet()) {
                    if (((AbstractJobletContainer) nc).isCollapsed() && !state) {
                        if (map.get(nc.getNode().getUniqueName()) != null && !map.get(nc.getNode().getUniqueName())) {
                            ((AbstractJobletContainer) nc).setCollapsed(state);
                        }

                    } else if (!((AbstractJobletContainer) nc).isCollapsed() && state) {
                        map.put(nc.getNode().getUniqueName(), false);
                        ((AbstractJobletContainer) nc).setCollapsed(state);
                    }
                }
            }
        }
    }

    /**
     * Added by Marvin Wang on July 30, 2012 for refreshing the tab of Job view.
     */
    protected void refreshJobSettingsView() {
        IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IJobSettingsView.ID);
        if (viewPart != null && viewPart instanceof JobSettingsView) {
            JobSettingsView jobView = (JobSettingsView) viewPart;
            TalendPropertyTabDescriptor currentSelectedTab = jobView.getCurrentSelectedTab();
            if (currentSelectedTab != null) {
                IDynamicProperty dc = currentSelectedTab.getPropertyComposite();
                if (dc != null) {
                    dc.refresh();
                }
            }
        }
    }

    /**
     * Added by hwang for refreshing the tab of testContaier view.
     */
    protected void refreshTestContaierView() {
    }

    private void refreshPropertyDirtyStatus() {
        /*
         * refresh should be executed before add the listener,or it will has eProxy on the property,it will cause a
         * editor dirty problem. hywang commet bug 17357
         */
        if (processEditorInput != null) {
            propertyInformation = new ArrayList(processEditorInput.getItem().getProperty().getInformations());
            propertyIsDirty = false;
        }
    }

    protected void updateRunJobContext() {
        final JobContextManager manager = (JobContextManager) getProcess().getContextManager();
        if (manager.isModified()) {
            final Map<String, String> nameMap = manager.getNameMap();

            // gcui:add a progressDialog.
            Shell shell = null;
            Display display = PlatformUI.getWorkbench().getDisplay();
            if (display != null) {
                shell = display.getActiveShell();
            }
            if (shell == null) {
                display = Display.getCurrent();
                if (display == null) {
                    display = Display.getDefault();
                }
                if (display != null) {
                    shell = display.getActiveShell();
                }
            }
            ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
            IRunnableWithProgress runnable = new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) {
                    monitor.beginTask(Messages.getString("AbstractMultiPageTalendEditor_pleaseWait"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
                            factory.executeRepositoryWorkUnit(new RepositoryWorkUnit<Object>("..", this) { //$NON-NLS-1$

                                @Override
                                protected void run() throws LoginException, PersistenceException {
                                    try {
                                        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();

                                        Set<String> curContextVars = getCurrentContextVariables(manager);
                                        IProcess2 process2 = getProcess();
                                        String jobId = process2.getProperty().getId();
                                        IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                                .getActivePage().getEditorReferences();
                                        List<IProcess2> processes = CorePlugin.getDefault().getDesignerCoreService()
                                                .getOpenedProcess(reference);

                                        // gcui:if nameMap is empty it do nothing.
                                        if (!nameMap.isEmpty()) {
                                            UpdateRunJobComponentContextHelper.updateItemRunJobComponentReference(factory,
                                                    nameMap, jobId, curContextVars);
                                            UpdateRunJobComponentContextHelper.updateOpenedJobRunJobComponentReference(processes,
                                                    nameMap, jobId, curContextVars);
                                        }
                                        // add for bug 9564
                                        List<IRepositoryViewObject> all = factory.getAll(ERepositoryObjectType.PROCESS, true);
                                        List<ProcessItem> allProcess = new ArrayList<ProcessItem>();
                                        for (IRepositoryViewObject repositoryObject : all) {
                                            Item item = repositoryObject.getProperty().getItem();
                                            if (item instanceof ProcessItem) {
                                                ProcessItem processItem = (ProcessItem) item;
                                                allProcess.add(processItem);
                                            }
                                        }
                                        UpdateRunJobComponentContextHelper.updateRefJobRunJobComponentContext(factory,
                                                allProcess, process2);

                                    } catch (PersistenceException e) {
                                        // e.printStackTrace();
                                        ExceptionHandler.process(e);
                                    }
                                    manager.setModified(false);
                                }
                            });

                        }

                    });
                    monitor.done();
                    if (monitor.isCanceled()) {
                        try {
                            throw new InterruptedException("Save Fail"); //$NON-NLS-1$
                        } catch (InterruptedException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            };
            try {
                progressDialog.run(true, true, runnable);
            } catch (InvocationTargetException e1) {
                ExceptionHandler.process(e1);
            } catch (InterruptedException e1) {
                ExceptionHandler.process(e1);
            }
        }
    }

    private Set<String> getCurrentContextVariables(IContextManager manager) {
        Set<String> varNameSet = new HashSet<String>();
        if (manager != null) {
            for (IContextParameter param : manager.getDefaultContext().getContextParameterList()) {
                varNameSet.add(param.getName());
            }
        }
        return varNameSet;
    }

    private void codeSync() {
        IProcess2 process = getProcess();
        if (!(process.getProperty().getItem() instanceof ProcessItem)) { // shouldn't work for joblet
            return;
        }

        if (jobletEditor == getEditor(oldPageIndex)) {
            // added for routines code generated switch editor 0 to 3.
            ProcessItem processItem = (ProcessItem) process.getProperty().getItem();
            covertJobscriptOnPageChange();
            ParametersType parameters = processItem.getProcess().getParameters();
            if (parameters != null && parameters.getRoutinesParameter() != null && parameters.getRoutinesParameter().size() == 0) {
                try {
                    List<RoutinesParameterType> dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();
                    parameters.getRoutinesParameter().addAll(dependenciesInPreference);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        // if some code has been generated already, for the editor we should need only the main job, not the childs.
        try {
            boolean lastGeneratedWithStats = ProcessorUtilities.getLastGeneratedWithStats(process.getId());
            boolean lastGeneratedWithTrace = ProcessorUtilities.getLastGeneratedWithTrace(process.getId());

            if (processor.isCodeGenerated()) {
                ProcessorUtilities.generateCode(process, process.getContextManager().getDefaultContext(), lastGeneratedWithStats,
                        lastGeneratedWithTrace, true, ProcessorUtilities.GENERATE_MAIN_ONLY);
            } else {
                ProcessorUtilities.generateCode(process, process.getContextManager().getDefaultContext(), lastGeneratedWithStats,
                        lastGeneratedWithTrace, true, ProcessorUtilities.GENERATE_WITH_FIRST_CHILD);
            }
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Saves the multi-page editor's document as another file. Also updates the text for page 0's tab, and updates this
     * multi-page editor's input to correspond to the nested editor's.
     */
    @Override
    public void doSaveAs() {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        // setPageText(0, editor.getTitle());
        // setInput(editor.getEditorInput());
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart
     */
    public void gotoMarker(final IMarker marker) {
        setActivePage(0);
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart.
     */
    @Override
    public boolean isSaveAsAllowed() {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            return false;
        }
        return true;
    }

    public void showDesignerPage() {
        setActivePage(0);
    }

    public void showCodePage() {
        if (useCodeView) {
            setActivePage(1);
        }
    }

    /**
     * Move Cursor to Selected Node.
     *
     * @param processor
     */

    /**
     * Get the selected Node if any.
     *
     * @return the component selected name or null if component is not found or is not activated
     */
    public String getSelectedNodeName() {
        String nodeName = null;
        Node node = getSelectedGraphicNode();
        if (node != null) {
            if (node.isActivate() || node.isDummy()) {
                nodeName = node.getUniqueName();
            } else {
                nodeName = null;
            }

            if (isVirtualNode(node)) {
                // add for feature 13701
                for (IMultipleComponentManager mcm : node.getComponent().getMultipleComponentManagers()) {
                    if (!mcm.isLookupMode()) {
                        nodeName += "_" + mcm.getInput().getName(); //$NON-NLS-1$
                    }
                }
                // for feature 13701
                // nodeName += "_" + node.getComponent().getMultipleComponentManagers().get(0).getInput().getName();
                // //$NON-NLS-1$
            }
            if (node.isFileScaleComponent()) {
                nodeName += "_fsNode"; //$NON-NLS-1$
            }
        }
        return nodeName;
    }

    /**
     *
     * DOC YeXiaowei Comment method "isVirtualNode".
     *
     * @param node
     * @return
     */
    private boolean isVirtualNode(final INode node) {
        boolean isVirtualNode = false;

        IElementParameter param = node.getElementParameter("IS_VIRTUAL_COMPONENT"); //$NON-NLS-1$
        if (param != null) { // now only available for tUniqRow.
            return (Boolean) param.getValue() && param.isRequired(node.getElementParameters());
        }

        final String componentName = node.getComponent().getName();
        if (componentName.equals("tMap")) { //$NON-NLS-1$
            isVirtualNode = CorePlugin.getDefault().getMapperService().isVirtualComponent(node);
        } else if (componentName.equals("tXMLMap")) { //$NON-NLS-1$
            isVirtualNode = CorePlugin.getDefault().getXMLMapperService().isVirtualComponent(node);
        } else if (componentName.equals("tAvroMap")) { //$NON-NLS-1$
            isVirtualNode = CorePlugin.getDefault().getSparkMapperService().isVirtualComponent(node);
        } else {
            List<IMultipleComponentManager> multipleComponentManagers = node.getComponent().getMultipleComponentManagers();
            for (IMultipleComponentManager mcm : multipleComponentManagers) {
                if (!mcm.isLookupMode()) {
                    return true;
                }
            }
        }
        if (!isVirtualNode) {
            if (node.getExternalNode() != null) {
                return node.getExternalNode().isGeneratedAsVirtualComponent();
            }
        }

        return isVirtualNode;
    }

    /**
     * DOC amaumont Comment method "getSelectedNode".
     *
     * @return
     */
    public Node getSelectedGraphicNode() {
        Node node = null;
        List selections = designerEditor.getViewer().getSelectedEditParts();
        if (selections.size() == 1) {
            Object selection = selections.get(0);

            if (selection instanceof NodeTreeEditPart) {
                NodeTreeEditPart nTreePart = (NodeTreeEditPart) selection;
                node = (Node) nTreePart.getModel();
            } else {
                if (selection instanceof NodePart) {
                    NodePart editPart = (NodePart) selection;
                    node = (Node) editPart.getModel();
                } else if (selection instanceof NodeLabelEditPart) {
                    NodeLabelEditPart editPart = (NodeLabelEditPart) selection;
                    node = ((NodeLabel) editPart.getModel()).getNode();
                }
            }
        }
        return node;
    }

    private void updateTitleImage(Property property) {
        Image image = null;
        InformationLevel level = property.getMaxInformationLevel();
        image = ImageProvider.getImage(getEditorTitleImage());
        if (level.equals(InformationLevel.ERROR_LITERAL)) {
            image = OverlayImageProvider.getImageWithError(image).createImage();
        }
        setTitleImage(image);
    }

    protected void updateTitleImage() {
        if (getProcess() == null) {
            return;
        }
        Property property = getProcess().getProperty();
        updateTitleImage(property);
    }

    protected IImage getEditorTitleImage() {
        return ECoreImage.PROCESS_ICON_EDITOR;
    }

    /**
     * Closes all project files on project close.
     */

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                    for (IWorkbenchPage page : pages) {
                        if (((FileEditorInput) designerEditor.getEditorInput()).getFile().getProject()
                                .equals(event.getResource())) {
                            IEditorPart editorPart = page.findEditor(designerEditor.getEditorInput());
                            page.closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

    @Override
    public Object getAdapter(final Class adapter) {
        if (designerEditor != null && designerEditor.equals(getActiveEditor())) {
            return this.getActiveEditor().getAdapter(adapter);
        }
        return super.getAdapter(adapter);
    }

    /**
     * Will allow to propagate the Delete evenement in the designer.
     */
    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (this.equals(getSite().getPage().getActiveEditor())) {
            if (selection instanceof StructuredSelection) {
                StructuredSelection structSel = (StructuredSelection) selection;
                if (structSel.getFirstElement() instanceof EditPart) {
                    if (designerEditor.equals(getActiveEditor())) {
                        designerEditor.selectionChanged(getActiveEditor(), selection);

                    }
                }
            }
        }
    }

    protected FileEditorInput createFileEditorInput() {

        IPath codePath = processor.getCodePath();

        if (codePath == null || codePath.isEmpty()) {
            // reinitialize the processor if there was any problem during the initialization.
            // (should not happen)
            try {
                processor.initPath();
            } catch (ProcessorException e) {
                MessageBoxExceptionHandler.process(e);
            }
            codePath = processor.getCodePath();
        }

        IFile codeFile = processor.getCodeProject().getFile(codePath);
        if (!codeFile.exists()) {
            // Create empty one
            try {
                codeFile.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
            } catch (CoreException e) {
                // Do nothing.
            }
        }
        return new FileEditorInput(codeFile);
    }

    /**
     * Getter for process.
     *
     * @return the process
     */
    @Override
    public IProcess2 getProcess() {
        if (designerEditor == null) {
            return null;
        }
        return designerEditor.getProcess();
    }

    public void updateChildrens() {
        // just call the method add protection will update new childrens and
        // keep old ones (keep to delete automatically
        // when closing job)
        JobResourceManager.getInstance().addProtection(designerEditor);
    }

    /**
     * DOC bqian Comment method "selectNode".
     *
     * @param node
     */
    public void selectNode(INode node) {
        GraphicalViewer viewer = designerEditor.getViewer();
        Object object = viewer.getRootEditPart().getChildren().get(0);
        if (object instanceof ProcessPart) {
            for (EditPart editPart : (List<EditPart>) ((ProcessPart) object).getChildren()) {
                if (editPart instanceof NodePart) {
                    if (((NodePart) editPart).getModel().equals(node)) {
                        viewer.select(editPart);
                    }
                }
            }
        }
    }

    public boolean isJobAlreadyOpened() {
        return foundExistEditor(this.getEditorInput());
    }

    private boolean foundExistEditor(final IEditorInput editorInput) {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {

            // WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
            IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
            if (page != null) {
                if (editorInput instanceof RepositoryEditorInput) {
                    RepositoryEditorInput curEditorInput = (RepositoryEditorInput) editorInput;

                    IEditorReference[] ref = page.findEditors(curEditorInput, getEditorId(), IWorkbenchPage.MATCH_INPUT);
                    return ref.length > 1;
                }
            }

        }
        return false;
    }

    /**
     * DOC qzhang Comment method "getEditorId".
     *
     * @return
     */
    public abstract String getEditorId();

    /**
     * Getter for keepPropertyLocked.
     *
     * @return the keepPropertyLocked
     */
    public boolean isKeepPropertyLocked() {
        return this.keepPropertyLocked;
    }

    /**
     * Sets the keepPropertyLocked.
     *
     * @param keepPropertyLocked the keepPropertyLocked to set
     */
    public void setKeepPropertyLocked(boolean keepPropertyLocked) {
        this.keepPropertyLocked = keepPropertyLocked;
    }

    /**
     * Getter for codeEditor.
     *
     * @return the codeEditor
     */
    public TalendJavaEditor getCodeEditor() {
        return (TalendJavaEditor) this.codeEditor;
    }

    @Override
    public AbstractTalendEditor getTalendEditor() {
        return designerEditor;
    }

    public JobDependenciesEditor getDependenciesEditor() {
        return dependenciesEditor;
    }

    public void beforeDispose() {
        if (null != jobletEditor) {
            IColumnSupport cs = jobletEditor.getAdapter(IColumnSupport.class);
            cs.dispose();
        }
    }

    /**
     * The <code>MultiPageEditorPart</code> implementation of this <code>IWorkbenchPart</code> method disposes all
     * nested editors. Subclasses may extend.
     */
    @Override
    public void dispose() {
        boolean isDirty = isDirty();
        getSite().setSelectionProvider(null);
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        /* after the release of eclipse3.6,this parameter can't be null */
        // setInput(null);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
        if (this.lockService != null) {
            this.lockService.unregister();
        }
        super.dispose();
        if (getProcess() != null && !getProcess().isReadOnly()) {
            if (isKeepPropertyLocked()) {
                return;
            }

            // Unlock the process :
            IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
            IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
            try {
                getProcess().getProperty().eAdapters().remove(dirtyListener);
                Property property = getProcess().getProperty();
                if (property.eResource() == null || property.getItem().eResource() == null) {
                    property = repFactory.getUptodateProperty(property);
                }
                // fix for bug 12524 for db repository
                // property = repFactory.reload(property);

                JobletUtil jUtil = new JobletUtil();
                jUtil.makeSureUnlockJoblet(getProcess());
                if (isDirty) {
                    // fix for TUP-17936
                    jUtil.reloadJobletInCurrentProcess(getProcess());
                }

                Item item = getProcess().getProperty().getItem();
                boolean keep = jUtil.keepLockJoblet(item);
                if (keep) {
                    repFactory.unlock(property.getItem());
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            } catch (LoginException e) {
                ExceptionHandler.process(e);
            }
        }

        if (processEditorInput != null) {
            processEditorInput.dispose();
            processEditorInput = null;
        }
        designerEditor = null;
        codeEditor = null;
        if (processor instanceof IJavaBreakpointListener) {
            JDIDebugModel.removeJavaBreakpointListener((IJavaBreakpointListener) processor);
        }
        processor = null;
        dirtyListener = null;
        NodeTransferDragSourceListener.getInstance().setEditPart(null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#initializePageSwitching()
     */
    @Override
    protected void initializePageSwitching() {

    }

    public abstract AbstractTalendEditor getDesignerEditor();

    class JobletCodeEditInput extends PlatformObject implements IEditorInput {

        @Override
        public Object getAdapter(Class adapter) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public IPersistableElement getPersistable() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getToolTipText() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public boolean isCheckout() {
        return isCheckout;
    }

    public void setCheckout(boolean isCheckout) {
        this.isCheckout = isCheckout;
    }

}
