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
package org.talend.designer.core.ui.editor;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.SimpleRaisedBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.Disposable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.Request;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.tools.TargetingTool;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.e4.compatibility.ActionBars;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.draw2d.AnimatableZoomManager;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.utils.workbench.preferences.GlobalConstant;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IComponentsHandler;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ISubjobContainer;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.node.MapperExternalNode;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.job.IJobResourceProtection;
import org.talend.core.model.repository.job.JobResource;
import org.talend.core.model.repository.job.JobResourceManager;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.core.service.IMRProcessService;
import org.talend.core.service.IStormProcessService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.core.utils.ReflectionUtils;
import org.talend.core.views.IComponentSettingsView;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.ITalendEditor;
import org.talend.designer.core.assist.TalendEditorComponentCreationUtil;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.StitchPseudoComponent;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.NodePartKeyHander;
import org.talend.designer.core.ui.action.ConnectionSetAsMainRef;
import org.talend.designer.core.ui.action.GEFCopyAction;
import org.talend.designer.core.ui.action.GEFDeleteAction;
import org.talend.designer.core.ui.action.GEFPasteAction;
import org.talend.designer.core.ui.action.GEFRedoAction;
import org.talend.designer.core.ui.action.GEFUndoAction;
import org.talend.designer.core.ui.action.ModifyConnectionOrderAction;
import org.talend.designer.core.ui.action.ModifyMergeOrderAction;
import org.talend.designer.core.ui.action.ModifyOutputOrderAction;
import org.talend.designer.core.ui.action.TalendConnectionCreationTool;
import org.talend.designer.core.ui.action.ToggleSubjobsAction;
import org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand;
import org.talend.designer.core.ui.editor.cmd.ConnectionReconnectCommand;
import org.talend.designer.core.ui.editor.cmd.CreateCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNoteCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNodeCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.connections.NodeConnectorTool;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.outline.NodeTreeEditPart;
import org.talend.designer.core.ui.editor.outline.ProcessTreePartFactory;
import org.talend.designer.core.ui.editor.palette.TalendCombinedTemplateCreationEntry;
import org.talend.designer.core.ui.editor.palette.TalendDrawerEditPart;
import org.talend.designer.core.ui.editor.palette.TalendEntryEditPart;
import org.talend.designer.core.ui.editor.palette.TalendFlyoutPaletteComposite;
import org.talend.designer.core.ui.editor.palette.TalendPaletteDrawer;
import org.talend.designer.core.ui.editor.palette.TalendPaletteHelper;
import org.talend.designer.core.ui.editor.palette.TalendPaletteViewer;
import org.talend.designer.core.ui.editor.palette.TalendPaletteViewerProvider;
import org.talend.designer.core.ui.editor.process.CreateComponentOnLinkHelper;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.process.TalendEditorDropTargetListener;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.views.CodeView;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;
import org.talend.designer.core.utils.ComponentsHelpUtil;
import org.talend.designer.core.utils.ConnectionUtil;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC qzhang class global comment. Detailled comment
 */
public abstract class AbstractTalendEditor extends GraphicalEditorWithFlyoutPalette implements
        ITabbedPropertySheetPageContributor, IJobResourceProtection, ITalendEditor {

    private static Logger log = Logger.getLogger(AbstractTalendEditor.class);

    private ConnectionPart selectedConnectionPart = null;

    private boolean isSynchronizer = false;

    // reflection field to access a private field
    private static Field splitterField, pageField;
    // reflection is used to work our way around some limitation in the GraphicalEditorWithFlyoutPalette upper class
    // retreive method and field in a static way to avoid doing it everytime they are called and improve perf
    static {
        try {
            IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
            if (helpSystem instanceof WorkbenchHelpSystem) {
                WorkbenchHelpSystem workbenchHelpSystem = (WorkbenchHelpSystem) helpSystem;
                workbenchHelpSystem.setDesiredHelpSystemId("org.talend.designer.core.TalendHelpUI"); //$NON-NLS-1$
            }
            splitterField = GraphicalEditorWithFlyoutPalette.class.getDeclaredField("splitter"); //$NON-NLS-1$
            splitterField.setAccessible(true);

            pageField = GraphicalEditorWithFlyoutPalette.class.getDeclaredField("page"); //$NON-NLS-1$
            pageField.setAccessible(true);
        } catch (SecurityException e) {
            handleReflectionFailure(e);
        } catch (NoSuchFieldException e) {
            handleReflectionFailure(e);
        }
    }

    // @Override
    @Override
    public void createPartControl(Composite parent) {
        /**
         * FIXME ggu
         *
         * Because the field splitter and page are private, and need override this method, so do it by reflection.
         */

        init();

        TalendFlyoutPaletteComposite talendSplitter = new TalendFlyoutPaletteComposite(parent, SWT.NONE, getSite().getPage(),
                (TalendPaletteViewerProvider) getPaletteViewerProvider(), getPalettePreferences());
        try {
            // set the field splitter using reflection
            splitterField.set(this, talendSplitter);
        } catch (IllegalArgumentException e) {
            handleReflectionFailure(e);
        } catch (IllegalAccessException e) {
            handleReflectionFailure(e);
        }
        createGraphicalViewer(talendSplitter);
        talendSplitter.setGraphicalControl(getGraphicalControl());
        TalendEditorComponentCreationUtil.updateAssistListener(this);
        Object hookPage;
        try {
            // get the field page using reflection
            hookPage = pageField.get(this);
            if (hookPage != null && hookPage instanceof CustomPalettePage) {
                talendSplitter.setExternalViewer(((CustomPalettePage) hookPage).getPaletteViewer());
                // set the field page using reflection
                pageField.set(this, null);
            }
        } catch (IllegalArgumentException e) {
            handleReflectionFailure(e);
        } catch (IllegalAccessException e) {
            handleReflectionFailure(e);
        }
        TalendPaletteHelper.checkAndInitToolBar();
    }

    /**
     * log the exception and throw a Runtime exception cause this is serious.
     *
     * @param e
     */
    private static void handleReflectionFailure(Exception e) {
        // our hook is not working so say it
        log.error("Talend Editor hook failed", e);
        throw new RuntimeException(e);

    }

    private OutlinePage outlinePage;

    protected FigureCanvas getEditor() {
        return (FigureCanvas) getGraphicalViewer().getControl();
    }

    public void selectPaletteEntry(String componentName) {
        PaletteViewer paletteViewer = getPaletteViewerProvider().getEditDomain().getPaletteViewer();
        if (paletteViewer == null) {
            return;
        }
        PaletteRoot root = getPaletteRoot();
        RootEditPart part = paletteViewer.getRootEditPart();
        if (part != null) {
            collapsePalette(part.getChildren());
        }
        if (root != null) {
            selectPaletteEntry(componentName, paletteViewer, root.getChildren());
        }
    }

    public PaletteViewer getPaletteViewer2() {
        return getPaletteViewerProvider().getEditDomain().getPaletteViewer();
    }

    /**
     * DOC hcw Comment method "collapsePalette".
     *
     * @param children
     */
    private void collapsePalette(List children) {
        for (Object object : children) {
            if (object instanceof TalendDrawerEditPart) {
                TalendDrawerEditPart part = (TalendDrawerEditPart) object;
                part.setExpanded(false);
                collapsePalette(part.getChildren());
            } else if (object instanceof EditPart) {
                collapsePalette(((EditPart) object).getChildren());
            }
        }
    }

    private boolean selectPaletteEntry(String componentName, PaletteViewer paletteViewer, List entries) {
        for (Object entry : entries) {

            if (entry instanceof PaletteContainer) {
                PaletteContainer container = (PaletteContainer) entry;
                if (selectPaletteEntry(componentName, paletteViewer, container.getChildren())) {
                    return true;
                }

            } else if (entry instanceof TalendPaletteDrawer) {
                TalendPaletteDrawer drawer = (TalendPaletteDrawer) entry;
                if (selectPaletteEntry(componentName, paletteViewer, drawer.getChildren())) {
                    return true;
                }
            } else if (entry instanceof ToolEntry) {
                ToolEntry paletteEntry = (ToolEntry) entry;
                if (paletteEntry.getLabel().equals(componentName)) {
                    EditPart part = getToolEntryEditPart(paletteViewer, paletteEntry);
                    expandPaletteDrawer(paletteViewer, paletteEntry);
                    // paletteViewer.setSelection(new StructuredSelection(part));
                    // paletteViewer.setFocus(part);
                    paletteViewer.select(part);
                    paletteViewer.reveal(part);
                    paletteViewer.setActiveTool(paletteEntry);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOC hcw Comment method "expandPaletteDrawer".
     *
     * @param paletteViewer
     * @param paletteEntry
     */
    private void expandPaletteDrawer(PaletteViewer paletteViewer, ToolEntry paletteEntry) {
        List<TalendDrawerEditPart> parts = new ArrayList<TalendDrawerEditPart>();
        PaletteContainer parent = paletteEntry.getParent();
        while (parent != null) {
            if (parent instanceof TalendPaletteDrawer) {
                Object editpart = paletteViewer.getEditPartRegistry().get(parent);
                if (editpart instanceof TalendDrawerEditPart) {
                    parts.add((TalendDrawerEditPart) editpart);
                }
            }
            parent = parent.getParent();
        }
        for (int i = parts.size() - 1; i >= 0; i--) {
            parts.get(i).setExpanded(true);
        }
    }

    private EditPart getToolEntryEditPart(PaletteViewer paletteViewer, ToolEntry entry) {
        return (EditPart) paletteViewer.getEditPartRegistry().get(entry);
    }

    public void savePaletteState() {
        PaletteViewer paletteViewer = getPaletteViewerProvider().getEditDomain().getPaletteViewer();
        if (paletteViewer != null) {
            TalendEditorPaletteFactory.saveFamilyState(paletteViewer);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot()
     */
    @Override
    protected PaletteRoot getPaletteRoot() {
        return ComponentPaletteUtilities.getPaletteRoot();
    }

    @Override
    public Object getAdapter(final Class type) {

        // this will select the TabbedPropertyPage instead of the classic property view
        // available in Eclipse 3.2
        if (type == IPropertySheetPage.class) {
            IPropertySheetPage properties = new TalendTabbedPropertySheetPage(this);
            return properties;
        }

        if (type == IContentOutlinePage.class) {
            outlinePage = new OutlinePage(new TreeViewer());
            return outlinePage;
        }
        if (type == ZoomManager.class) {
            return getGraphicalViewer().getProperty(ZoomManager.class.toString());
        }
        
        if(type == ScrollingGraphicalViewer.class) {
            return saveOutlinePicture((ScrollingGraphicalViewer) getGraphicalViewer());
        }

        return super.getAdapter(type);
    }

    protected AbstractMultiPageTalendEditor parent;

    protected boolean savePreviouslyNeeded;

    // protected IProcess2 process;

    private RulerComposite rulerComp;

    protected IComponentsFactory components;

    protected boolean readOnly;

    public static final int GRID_SIZE = 32;

    private boolean dirtyState = false;

    protected JobResource currentJobResource;

    protected String projectName;

    protected final Map<String, JobResource> protectedJobs;

    private IComponentsHandler componenentsHandler;

    /** The verify key listener for activation code triggering. */
    public ActivationCodeTrigger fActivationCodeTrigger = new ActivationCodeTrigger();

    public AbstractTalendEditor() {
        this(false);
        init();
    }

    public AbstractTalendEditor(boolean readOnly) {

        ProcessorUtilities.addOpenEditor(this);

        // an EditDomain is a "session" of editing which contains things
        // like the CommandStack
        setEditDomain(new TalendEditDomain(this));
        this.readOnly = readOnly;

        // projectName = ((RepositoryContext)
        // CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getProject()
        // .getLabel();

        currentJobResource = new JobResource();
        protectedJobs = new HashMap<String, JobResource>();
        initializeKeyBindingScopes();

        init();
    }

    protected void init() {
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        this.componenentsHandler = initComponentsHandler();
        componentsFactory.setComponentsHandler(componenentsHandler);
    }

    /**
     * Added by Marvin Wang on Jan. 11, 2012 for creating a new component handler for editor with palette.
     *
     * @return
     */
    protected abstract IComponentsHandler initComponentsHandler();

    /**
     * Getter for componenentsHandler.
     *
     * @return the componenentsHandler
     */
    public IComponentsHandler getComponenentsHandler() {
        return this.componenentsHandler;
    }

    /**
     * Sets the componenentsHandler.
     *
     * @param componenentsHandler the componenentsHandler to set
     */
    public void setComponenentsHandler(IComponentsHandler componenentsHandler) {
        this.componenentsHandler = componenentsHandler;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        monitor.beginTask("begin save job...", 100); //$NON-NLS-1$
        monitor.worked(10);
        savePreviewPictures();

        // generate the MR infor parameter.
        try {
            boolean isStormServiceRegistered = GlobalServiceRegister.getDefault().isServiceRegistered(IStormProcessService.class);
            boolean isMRServiceRegistered = GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class);
            if (isStormServiceRegistered || isMRServiceRegistered) {
                IProcess2 process = getProcess();
                if (process != null) {
                    IRepositoryViewObject repoObjectView = DesignerPlugin.getDefault().getRepositoryService()
                            .getProxyRepositoryFactory().getLastVersion(process.getId());
                    if (repoObjectView != null && repoObjectView.getProperty() != null) {
                        Item currentItem = repoObjectView.getProperty().getItem();
                        if (isMRServiceRegistered) {
                            IMRProcessService mrService = GlobalServiceRegister.getDefault().getService(
                                    IMRProcessService.class);

                            if (mrService.isMapReduceItem(currentItem)) {
                                // We make sure that the current item is a Batch item before generating the M/R
                                // information parameters.
                                mrService.generateMRInfosParameter(process);
                            }
                        }
                        if (isStormServiceRegistered) {
                            IStormProcessService stormService = GlobalServiceRegister.getDefault()
                                    .getService(IStormProcessService.class);

                            if (stormService.isStormItem(currentItem)) {
                                // We make sure that the current item is a Streaming item before generating the Spark
                                // Streaming information parameters.
                                stormService.generateSparkStreamingInfosParameter(process);
                            }
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }

        try {
            if (getEditorInput() instanceof JobEditorInput) {
                boolean saved = ((JobEditorInput) getEditorInput()).saveProcess(new SubProgressMonitor(monitor, 80), null);
                if (!saved) {
                    monitor.setCanceled(true);
                    throw new InterruptedException();
                }
            }

            setDirty(false);
            getCommandStack().markSaveLocation();

            boolean isneedReload = false;
            for (int i = 0; i < getProcess().getGraphicalNodes().size(); i++) {
                Node node = (Node) getProcess().getGraphicalNodes().get(i);
                if (node.isNeedloadLib()) {
                    isneedReload = true;
                    node.setNeedLoadLib(false);
                }
            }

            if (isneedReload) {
                // / See bug 4821
                GlobalServiceRegister.getDefault().getService(ILibrariesService.class)
                        .updateModulesNeededForCurrentJob(getProcess());
            }

            monitor.worked(10);

        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
            monitor.setCanceled(true);
        } finally {
            monitor.done();
        }

    }

    @Override
    protected FlyoutPreferences getPalettePreferences() {
        return TalendEditorPaletteFactory.createPalettePreferences();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#setFocus()
     */
    @Override
    public void setFocus() {
        IComponentSettingsView viewer = (IComponentSettingsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().findView(IComponentSettingsView.ID);
        if (viewer != null) {
            IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();

            if (selection.size() == 1
                    && (selection.getFirstElement() instanceof NodePart || selection.getFirstElement() instanceof ConnectionPart
                            || selection.getFirstElement() instanceof SubjobContainerPart || selection.getFirstElement() instanceof ConnLabelEditPart)) {

                viewer.setElement((Element) ((AbstractEditPart) selection.getFirstElement()).getModel());

            } else {
                viewer.cleanDisplay();
            }
        }

        JobSettings.switchToCurJobSettingsView();

        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null) {
                testContainerService.switchToCurTestContainerView();
            }
        }

        super.setFocus();

        if (!readOnly) {
            // When gain focus, check read-only to disable read-only mode if process has been restore while opened :
            // 1. Enabled/disabled components :

            // process.checkReadOnly();

            // 2. Set backgroung color :
            List children = getViewer().getRootEditPart().getChildren();
            if (!children.isEmpty()) {
                ProcessPart rep = (ProcessPart) children.get(0);
                rep.ajustReadOnly();
            }
        }
    }

    protected String[] fKeyBindingScopes;

    /**
     * Sets the key binding scopes for this editor.
     *
     * @param scopes a non-empty array of key binding scope identifiers
     * @since 2.1
     */
    protected void setKeyBindingScopes(String[] scopes) {
        Assert.isTrue(scopes != null && scopes.length > 0);
        fKeyBindingScopes = scopes;
    }

    // ------------------------------------------------------------------------
    // Overridden from EditorPart

    @Override
    protected void setInput(final IEditorInput input) {
        super.setInput(input);

        try {
            if (input instanceof JobEditorInput) {
                // process = ((JobEditorInput) input).getLoadedProcess();
                projectName = CorePlugin.getDefault().getProxyRepositoryFactory()
                        .getProject(((RepositoryEditorInput) input).getItem()).getLabel();
            }
        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
            return;
        }

        IProcess2 process = getProcess();
        if (process != null && process.getProperty().getItem() instanceof ProcessItem) {
            currentJobResource.setJobInfo(new JobInfo((ProcessItem) process.getProperty().getItem(), process.getProperty(),
                    process.getContextManager().getDefaultContext().getName()));
            currentJobResource.setProjectName(projectName);

            JobResourceManager.getInstance().addProtection(this);
        }
    }

    /*
     * @see ITextEditor#setAction(String, IAction)
     */
    public void setAction(String actionID, IAction action) {
        Assert.isNotNull(actionID);
        if (action == null) {
            action = getActionRegistry().getAction(actionID);
            if (action != null) {
                fActivationCodeTrigger.unregisterActionFromKeyActivation(action);
            }
        } else {
            // getActionRegistry().registerAction(action);
            fActivationCodeTrigger.registerActionForKeyActivation(action);
        }
    }

    /**
     * Initializes the key binding scopes of this editor.
     */
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope" }); //$NON-NLS-1$
    }

    /**
     * Internal key verify listener for triggering action activation codes.
     */
    protected class ActivationCodeTrigger implements VerifyKeyListener {

        /** Indicates whether this trigger has been installed. */
        private boolean fIsInstalled = false;

        /**
         * The key binding service to use.
         *
         * @since 2.0
         */
        private IKeyBindingService fKeyBindingService;

        /*
         * @see VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
         */
        @Override
        public void verifyKey(VerifyEvent event) {

            // ActionActivationCode code = null;
            // int size = fActivationCodes.size();
            // for (int i = 0; i < size; i++) {
            // code = (ActionActivationCode) fActivationCodes.get(i);
            // if (code.matches(event)) {
            // IAction action = getAction(code.fActionId);
            // if (action != null) {
            //
            // if (action instanceof IUpdate)
            // ((IUpdate) action).update();
            //
            // if (!action.isEnabled() && action instanceof IReadOnlyDependent) {
            // IReadOnlyDependent dependent = (IReadOnlyDependent) action;
            // boolean writable = dependent.isEnabled(true);
            // if (writable) {
            // event.doit = false;
            // return;
            // }
            // } else if (action.isEnabled()) {
            // event.doit = false;
            // action.run();
            // return;
            // }
            // }
            // }
            // }
        }

        /**
         * Installs this trigger on the editor's text widget.
         *
         * @since 2.0
         */
        public void install() {
            if (!fIsInstalled) {

                // if (fSourceViewer instanceof ITextViewerExtension) {
                // ITextViewerExtension e = (ITextViewerExtension) fSourceViewer;
                // e.prependVerifyKeyListener(this);
                // } else {
                // StyledText text = fSourceViewer.getTextWidget();
                // text.addVerifyKeyListener(this);
                // }

                fKeyBindingService = getEditorSite().getKeyBindingService();
                fIsInstalled = true;
            }
        }

        /**
         * Uninstalls this trigger from the editor's text widget.
         *
         * @since 2.0
         */
        public void uninstall() {
            if (fIsInstalled) {

                // if (fSourceViewer instanceof ITextViewerExtension) {
                // ITextViewerExtension e = (ITextViewerExtension) fSourceViewer;
                // e.removeVerifyKeyListener(this);
                // } else if (fSourceViewer != null) {
                // StyledText text = fSourceViewer.getTextWidget();
                // if (text != null && !text.isDisposed())
                // text.removeVerifyKeyListener(fActivationCodeTrigger);
                // }

                fIsInstalled = false;
                fKeyBindingService = null;
            }
        }

        /**
         * Registers the given action for key activation.
         *
         * @param action the action to be registered
         * @since 2.0
         */
        public void registerActionForKeyActivation(IAction action) {
            if (action.getActionDefinitionId() != null) {
                fKeyBindingService.registerAction(action);
            }
        }

        /**
         * The given action is no longer available for key activation
         *
         * @param action the action to be unregistered
         * @since 2.0
         */
        public void unregisterActionFromKeyActivation(IAction action) {
            if (action.getActionDefinitionId() != null) {
                fKeyBindingService.unregisterAction(action);
            }
        }

        /**
         * Sets the key binding scopes for this editor.
         *
         * @param keyBindingScopes the key binding scopes
         * @since 2.1
         */
        public void setScopes(String[] keyBindingScopes) {
            if (keyBindingScopes != null && keyBindingScopes.length > 0) {
                fKeyBindingService.setScopes(keyBindingScopes);
            }
        }
    }

    /**
     * Representation of action activation codes.
     */
    protected static class ActionActivationCode {

        /** The action id. */
        public String fActionId;

        /** The character. */
        public char fCharacter;

        /** The key code. */
        public int fKeyCode = -1;

        /** The state mask. */
        public int fStateMask = SWT.DEFAULT;

        /**
         * Creates a new action activation code for the given action id.
         *
         * @param actionId the action id
         */
        public ActionActivationCode(String actionId) {
            fActionId = actionId;
        }

        /**
         * Returns <code>true</code> if this activation code matches the given verify event.
         *
         * @param event the event to test for matching
         * @return whether this activation code matches <code>event</code>
         */
        public boolean matches(VerifyEvent event) {
            return (event.character == fCharacter && (fKeyCode == -1 || event.keyCode == fKeyCode) && (fStateMask == SWT.DEFAULT || event.stateMask == fStateMask));
        }
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        /** * Manage the view in the Outline ** */
        ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();
        viewer.setSelectionManager(new TalendSelectionManager());

        TalendScalableFreeformRootEditPart root = new TalendScalableFreeformRootEditPart(getEditorInput());

        List<String> zoomLevels = new ArrayList<String>();
        zoomLevels.add(ZoomManager.FIT_ALL);
        zoomLevels.add(ZoomManager.FIT_WIDTH);
        zoomLevels.add(ZoomManager.FIT_HEIGHT);
        root.getZoomManager().setZoomLevelContributions(zoomLevels);
        // root.getZoomManager().setZoomAnimationStyle(ZoomManager.ANIMATE_NEVER);

        IAction zoomIn = new ZoomInAction(root.getZoomManager());
        IAction zoomOut = new ZoomOutAction(root.getZoomManager());
        getActionRegistry().registerAction(zoomIn);
        getActionRegistry().registerAction(zoomOut);
        IHandlerService service = getEditorSite().getService(IHandlerService.class);
        service.activateHandler(zoomIn.getActionDefinitionId(), new ActionHandler(zoomIn));

        service.activateHandler(zoomOut.getActionDefinitionId(), new ActionHandler(zoomOut));

        IAction directEditAction = new DirectEditAction(this);
        getActionRegistry().registerAction(directEditAction);
        getSelectionActions().add(directEditAction.getId());

        IAction copyAction = new GEFCopyAction(this);
        getActionRegistry().registerAction(copyAction);
        getSelectionActions().add(copyAction.getId());
        // setAction(copyAction.getId(), copyAction);

        IAction pasteAction = new GEFPasteAction(this);
        getActionRegistry().registerAction(pasteAction);
        getSelectionActions().add(pasteAction.getId());
        // setAction(pasteAction.getId(), pasteAction);

        IAction deleteAction = new GEFDeleteAction(this);
        getActionRegistry().registerAction(deleteAction);
        getSelectionActions().add(deleteAction.getId());
        // setAction(deleteAction.getId(), deleteAction);

        IAction undoAction = new GEFUndoAction(this);
        getActionRegistry().registerAction(undoAction);
        getSelectionActions().add(undoAction.getId());

        IAction redoAction = new GEFRedoAction(this);
        getActionRegistry().registerAction(redoAction);
        getSelectionActions().add(redoAction.getId());

        IAction setRefAction = new ConnectionSetAsMainRef(this);
        getActionRegistry().registerAction(setRefAction);
        getSelectionActions().add(setRefAction.getId());

        IAction modifyMergeAction = new ModifyMergeOrderAction(this);
        getActionRegistry().registerAction(modifyMergeAction);
        getSelectionActions().add(modifyMergeAction.getId());

        IAction modifyOutputOrderAction = new ModifyOutputOrderAction(this);
        getActionRegistry().registerAction(modifyOutputOrderAction);
        getSelectionActions().add(modifyOutputOrderAction.getId());

        List<IAction> calcOrderActions = ModifyConnectionOrderAction.getOrderActions(this);
        for (IAction orderAction : calcOrderActions) {
            getActionRegistry().registerAction(orderAction);
            getSelectionActions().add(orderAction.getId());
        }

        viewer.setRootEditPart(root);

        PartFactory partFactory = new PartFactory();
        // set the factory to use for creating EditParts for elements in the model
        getGraphicalViewer().setEditPartFactory(partFactory);
        getGraphicalViewer().setKeyHandler(new NodePartKeyHander(getGraphicalViewer()).setParent(getCommonKeyHandler()));
        // GraphicalViewer graViewer = getGraphicalViewer();
        // graViewer.setKeyHandler(new GraphicalViewerKeyHandler(graViewer));
        initializeActivationCodeTrigger();

        /** * Management of the context menu ** */

        ContextMenuProvider cmProvider = new TalendEditorContextMenuProvider(this, viewer, getActionRegistry());
        viewer.setContextMenu(cmProvider);

        /** * Management of the Zoom ** */
        /*
         * ZoomManager manager = (ZoomManager) getGraphicalViewer().getProperty(ZoomManager.class.toString()); if
         * (manager != null) { manager.setZoom(getProcess().getZoom()); }
         */
        // Scroll-wheel Zoom
        getGraphicalViewer().setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1), MouseWheelZoomHandler.SINGLETON);

        /** * Snap To Grid ** */
        // Grid properties
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_SPACING,
                new Dimension(AbstractTalendEditor.GRID_SIZE, AbstractTalendEditor.GRID_SIZE));
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, new Boolean(true/*
                                                                                            * getProcess().isGridEnabled(
                                                                                            * )
                                                                                            */));
        // We keep grid visibility and enablement in sync
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, new Boolean(true/*
                                                                                            * getProcess().isGridEnabled(
                                                                                            * )
                                                                                            */));
        IAction showGrid = new ToggleGridAction(getGraphicalViewer());
        getActionRegistry().registerAction(showGrid);

        /** * Snap To Geometry ** */
        getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, new Boolean(false/*
                                                                                                 * getProcess().
                                                                                                 * isSnapToGeometryEnabled
                                                                                                 * ()
                                                                                                 */));
        IAction snapAction = new ToggleSnapToGeometryAction(getGraphicalViewer());
        getActionRegistry().registerAction(snapAction);

        configurationSubJob(viewer);
    }

    protected void configurationSubJob(GraphicalViewer viewer) {
        if (getProcess() != null) {
            if (getProcess().isSubjobEnabled()) {
                // toggle subjobs action
                IAction toggleSubjobsAction = ToggleSubjobsAction.getDefault();
                getActionRegistry().registerAction(toggleSubjobsAction);

                for (Iterator iterator = getSelectionActions().iterator(); iterator.hasNext();) {
                    String actionID = (String) iterator.next();
                    IAction action = getActionRegistry().getAction(actionID);
                    setAction(actionID, action);
                }
            }
            int minx = getMinX();
            int miny = getMinY();
            if (viewer != null && viewer.getControl() instanceof FigureCanvas) {
                if (minx < 0) {
                    ((FigureCanvas) viewer.getControl()).getViewport().getHorizontalRangeModel().setMinimum(minx);
                    ((FigureCanvas) viewer.getControl()).scrollToX(minx);
                }
                if (miny < 0) {
                    ((FigureCanvas) viewer.getControl()).getViewport().getVerticalRangeModel().setMinimum(miny);
                    ((FigureCanvas) viewer.getControl()).scrollToY(miny);
                }

            }
        }
    }

    private int getMinX() {
        int x = 0;
        for (ISubjobContainer node : this.getProcess().getSubjobContainers()) {
            int ncX = ((SubjobContainer) node).getSubjobContainerRectangle().x;
            if (x > ncX) {
                x = ncX;
            }
        }
        return x;
    }

    private int getMinY() {
        int y = 0;
        for (ISubjobContainer node : this.getProcess().getSubjobContainers()) {
            int ncY = ((SubjobContainer) node).getSubjobContainerRectangle().y;
            if (y > ncY) {
                y = ncY;
            }
        }
        return y;
    }

    /**
     * Initializes the activation code trigger.
     *
     * @since 2.1
     */
    public void initializeActivationCodeTrigger() {
        fActivationCodeTrigger.install();
        fActivationCodeTrigger.setScopes(fKeyBindingScopes);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public IProcess2 getProcess() {
        if (this.getEditorInput() instanceof JobEditorInput) {
            return ((JobEditorInput) this.getEditorInput()).getLoadedProcess();
        }
        return null;
    }

    public void setProcess(IProcess2 process) {
        if (this.getEditorInput() instanceof JobEditorInput) {
            ((JobEditorInput) this.getEditorInput()).setLoadedProcess(process);

        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getActions() {
        return getSelectionActions();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#getActionRegistry()
     */
    @Override
    public ActionRegistry getActionRegistry() {
        return super.getActionRegistry();
    }

    /**
     * Save the outline picture for this editor.
     *
     * @param viewer
     */
    protected Map<String, Object> saveOutlinePicture(ScrollingGraphicalViewer viewer) {
        Map<String,Object> results = new HashMap<String, Object>();
        GlobalConstant.generatingScreenShoot = true;
        LayerManager layerManager = (LayerManager) viewer.getEditPartRegistry().get(LayerManager.ID);
        // save image using swt
        // get root figure
        IFigure backgroundLayer = layerManager.getLayer(LayerConstants.GRID_LAYER);
        IFigure contentLayer = layerManager.getLayer(LayerConstants.PRINTABLE_LAYERS);
        // create image from root figure
        FreeformViewport viewport = (FreeformViewport) ((TalendScalableFreeformRootEditPart) layerManager).getFigure();
        viewport.getUpdateManager().performUpdate();
        int width = contentLayer.getBounds().width;
        int height = contentLayer.getBounds().height;
        Image img = new Image(null, width, height);
        GC gc = new GC(img);
        Graphics graphics = new SWTGraphics(gc);
        Point point = contentLayer.getBounds().getTopLeft();
        graphics.translate(-point.x, -point.y);
        results.put(IProcess.SCREEN_OFFSET_X, String.valueOf(-point.x));
        results.put(IProcess.SCREEN_OFFSET_Y, String.valueOf(-point.y));
        backgroundLayer.paint(graphics);
        contentLayer.paint(graphics);
        graphics.dispose();
        gc.dispose();
        results.put("process", ImageUtils.saveImageToData(img));
        img.dispose();

        // service.getProxyRepositoryFactory().refreshJobPictureFolder(outlinePicturePath);
        GlobalConstant.generatingScreenShoot = false;
        
        return results;
    }

    /**
     * DOC qzhang Comment method "getOutlinePicturePath".
     *
     * @return
     */
    protected String getOutlinePicturePath() {
        return RepositoryConstants.IMG_DIRECTORY_OF_JOB_OUTLINE;
    }

    @Override
    protected Control getGraphicalControl() {
        return rulerComp;
    }

    @Override
    protected void createGraphicalViewer(final Composite parent) {
        rulerComp = new RulerComposite(parent, SWT.NONE);
        super.createGraphicalViewer(rulerComp);
        rulerComp.setGraphicalViewer((ScrollingGraphicalViewer) getGraphicalViewer());
    }

    @Override
    public void commandStackChanged(final EventObject event) {
        if (isDirty()) {
            if (!this.savePreviouslyNeeded) {
                // remove all error status at any change of the job .
                removeErrorStatusIfDirty();
                this.savePreviouslyNeeded = true;
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
            checkSaveAsEnabled();
        } else {
            savePreviouslyNeeded = false;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        super.commandStackChanged(event);
    }

    private void checkSaveAsEnabled() {
        if (!isSaveAsAllowed()) {
            return;
        }
        IAction action = getAction(ActionFactory.SAVE_AS.getId());
        if (action == null) {
            action = ActionFactory.SAVE_AS.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            getActionRegistry().registerAction(action);
        }
        IAction saveAsAction = getAction(ActionFactory.SAVE_AS.getId());
        if (saveAsAction != null && !getAction(ActionFactory.SAVE_AS.getId()).isEnabled()) {
            getAction(ActionFactory.SAVE_AS.getId()).setEnabled(true);
        }
    }

    protected void removeErrorStatusIfDirty() {
        // remove all error status at any change of the job .
        IProcess2 process = getProcess();
        if (process != null) {
            Property property = process.getProperty();
            if (property != null && !property.getInformations().isEmpty()) {
                property.getInformations().clear();
                Problems.computePropertyMaxInformationLevel(property, false);
                // remove error for all the nodes
                for (INode psNode : getProcess().getGraphicalNodes()) {
                    if (psNode instanceof Node) {
                        Node node = (Node) psNode;
                        node.setErrorFlag(false);
                        node.setCompareFlag(false);
                        node.setErrorInfo(null);
                        node.getNodeError().updateState("UPDATE_STATUS", false); //$NON-NLS-1$
                        node.setErrorInfoChange("ERRORINFO", false); //$NON-NLS-1$
                    }
                }
                Problems.refreshProblemTreeView();
            }
        }
    }

    /**
     * This function is used only for the TabbedPropertySheetPage.
     *
     * @return contributorId String
     */
    @Override
    public String getContributorId() {
        return IRepositoryView.VIEW_ID;
    }

    /**
     * ftang Comment method "savePreviewPictures".
     */
    protected void savePreviewPictures() {
        // getGraphicalViewer().getControl().getDisplay().asyncExec(new Runnable() {
        //
        // public void run() {
        Map<String, Object> results = saveOutlinePicture((ScrollingGraphicalViewer) getGraphicalViewer());
        IProcess2 process = getProcess();
        process.setPropertyValue(IProcess.SCREEN_OFFSET_X, (String) results.get(IProcess.SCREEN_OFFSET_X));
        process.setPropertyValue(IProcess.SCREEN_OFFSET_Y, (String) results.get(IProcess.SCREEN_OFFSET_Y));
        process.getScreenshots().put("process", (byte[]) results.get("process"));
        // }
        //
        // });
    }

    protected RepositoryEditorInput getRepositoryEditorInput() {
        return (RepositoryEditorInput) getEditorInput();
    }

    @Override
    public void doSaveAs() {
        SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow().getShell());
        dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
        dialog.open();
        IPath path = dialog.getResult();

        if (path == null) {
            return;
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IFile file = workspace.getRoot().getFile(path);

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

            @Override
            public void execute(final IProgressMonitor monitor) throws CoreException {
                try {
                    savePreviewPictures();
                    getProcess().saveXmlFile();
                    // file.refreshLocal(IResource.DEPTH_ONE, monitor);
                } catch (Exception e) {
                    // e.printStackTrace();
                    ExceptionHandler.process(e);
                }
            }
        };

        try {
            new ProgressMonitorDialog(getSite().getWorkbenchWindow().getShell()).run(false, true, op);
            // setInput(new FileEditorInput((IFile) file));
            getCommandStack().markSaveLocation();
            setDirty(false);

        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    @Override
    public void dispose() {
        ProcessorUtilities.editorClosed(this);

        talendPaletteViewerProvider = null;

        // achen modify to fix bug 0006107
        // if (!getParent().isKeepPropertyLocked()) {
        // JobResourceManager manager = JobResourceManager.getInstance();
        // manager.removeProtection(this);
        // for (JobResource r : protectedJobs.values()) {
        // manager.deleteResource(r);
        // }
        // }
        ComponentSettingsView viewer = (ComponentSettingsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().findView(ComponentSettingsView.ID);
        if (viewer != null) {
            viewer.cleanDisplay();
        }

        for (Iterator iterator = getSelectionActions().iterator(); iterator.hasNext();) {
            String actionID = (String) iterator.next();
            IAction action = getActionRegistry().getAction(actionID);
            if (action != null) {
                fActivationCodeTrigger.unregisterActionFromKeyActivation(action);
                getActionRegistry().removeAction(action);
                if (action instanceof Disposable) {
                    ((Disposable) action).dispose();
                }
            }
        }
        fActivationCodeTrigger.uninstall();
        fActivationCodeTrigger = null;
        getSelectionActions().clear();

        getGraphicalViewer().removeDropTargetListener(talendEditorDropTargetListener);

        if (getGraphicalViewer().getContents() != null) {
            getGraphicalViewer().getContents().deactivate();
            getGraphicalViewer().getContents().removeNotify();
            getGraphicalViewer().getRootEditPart().deactivate();
            getGraphicalViewer().getRootEditPart().removeNotify();
        }
        getGraphicalViewer().setEditPartFactory(null);
        getGraphicalViewer().setContents(null);
        MenuManager contextMenu = getGraphicalViewer().getContextMenu();
        if (contextMenu != null) {
            contextMenu.dispose();
        }
        // can't set null dirrectly since it will throw NPE
        // getGraphicalViewer().setContextMenu(null);

        // rulerComp.dispose();

        if (sharedKeyHandler != null) {
            sharedKeyHandler.remove(KeyStroke.getPressed(SWT.F1, 0));
            sharedKeyHandler.remove(KeyStroke.getPressed(SWT.DEL, 0));
        }

        // super.setInput(null);

        // getGraphicalViewer().setContents(null);
        // if (getGraphicalViewer().getControl() != null && !getGraphicalViewer().getControl().isDisposed()) {
        // getGraphicalViewer().getControl().dispose();
        // }

        talendEditorDropTargetListener.setEditor(null);
        talendEditorDropTargetListener = null;
        // TalendScalableFreeformRootEditPart rootEditPart = (TalendScalableFreeformRootEditPart) getGraphicalViewer()
        // .getRootEditPart();
        // rootEditPart.setEditorInput(null);
        // rootEditPart.deactivate();

        super.dispose();
        AbstractMultiPageTalendEditor parentEditor = getParent();
        if (parentEditor != null && !parentEditor.isKeepPropertyLocked() && getProcess() != null) {
            ((Process) getProcess()).dispose();
        }
        // process = null;
        parent = null;

        getEditDomain().getCommandStack().dispose();
        getEditDomain().setActiveTool(null);
        getEditDomain().setPaletteRoot(null);
        getEditDomain().setPaletteViewer(null);
        // don't clear edit domain, since git merge conflict editor reused it
        // getEditDomain().setCommandStack(null);
        getEditDomain().setDefaultTool(null);
        getSelectionSynchronizer().removeViewer(getGraphicalViewer());
        getSite().setSelectionProvider(null);
        CodeView.refreshCodeView(null);
    }

    public void gotoMarker(final IMarker marker) {
    }

    @Override
    public boolean isDirty() {
        // rely on the command stack to determine dirty flag
        return dirtyState || getCommandStack().isDirty();
    }

    @Override
    public void setDirty(boolean dirty) {
        dirtyState = dirty;
        if (dirtyState) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        // allow Save As
        return true;
    }

    TalendPaletteViewerProvider talendPaletteViewerProvider;

    @Override
    protected PaletteViewerProvider createPaletteViewerProvider() {
        if (talendPaletteViewerProvider == null) {
            talendPaletteViewerProvider = new TalendPaletteViewerProvider(getEditDomain());
        }
        return talendPaletteViewerProvider;
    }

    public TalendPaletteViewer getTalendPaletteViewer() {
        TalendPaletteViewer talendPaletteViewer = null;

        if (talendPaletteViewerProvider != null) {
            talendPaletteViewer = talendPaletteViewerProvider.getTalendPaletteViewer();
        }

        return talendPaletteViewer;
    }

    public IComponent getComponent(String name) {
        if (components == null) {
            components = ComponentsFactoryProvider.getInstance();
        }
        return components.get(name, getProcess().getComponentsType());
    }

    public ProcessPart getProcessPart() {
        RootEditPart rootEditPart = ((ScrollingGraphicalViewer) getGraphicalViewer()).getRootEditPart();
        return (ProcessPart) rootEditPart.getChildren().get(0);
    }

    /**
     * Outline view page. <br/>
     *
     * $Id: TalendEditor.java 7516 2007-12-11 05:23:18Z ftang $
     *
     */

    /**
     * This function will allow the use of the Delete in the Multipage Editor.
     */
    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        super.selectionChanged(part, selection);
        if (this.equals(part)) { // Propagated from MyMultiPageEditor.
            updateActions(getSelectionActions());
        }
    }

    /**
     * Get the viewer in the editor.
     *
     * @return
     */
    public GraphicalViewer getViewer() {
        return getGraphicalViewer();
    }

    public AbstractMultiPageTalendEditor getParent() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.job.deletion.IResourceProtection#getProtectedIds()
     */
    @Override
    public String[] calculateProtectedIds() {
        IProcess2 process = getProcess();
        if (!(process.getProperty().getItem() instanceof ProcessItem)) {
            return new String[] {};
        }
        // achen modify to fix bug 6462
        // Set<JobInfo> subJobs = ProcessorUtilities.getChildrenJobInfo((ProcessItem) process.getProperty().getItem());

        // for (JobInfo jobInfo : subJobs) {
        //            String protectedJob = "subjob_of_" + process.getLabel() + "_" + //$NON-NLS-1$ //$NON-NLS-2$
        //                    projectName + "_" + jobInfo.getJobName() + "_" + jobInfo.getJobVersion(); //$NON-NLS-1$ //$NON-NLS-2$
        // protectedJobs.put(protectedJob, new JobResource(projectName, jobInfo));
        // }
        String currentJobId = "talend_editor_" + projectName + "_" + process.getLabel() //$NON-NLS-1$ //$NON-NLS-2$
                + "_" + process.getVersion(); //$NON-NLS-1$
        protectedJobs.put(currentJobId, currentJobResource);

        Set<String> set = protectedJobs.keySet();

        return set.toArray(new String[set.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.job.deletion.IResourceProtection#getProjectedIds()
     */
    @Override
    public String[] getProtectedIds() {
        Set<String> set = protectedJobs.keySet();
        return set.toArray(new String[set.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.job.deletion.IResourceProtection#getJobResource()
     */
    @Override
    public JobResource getJobResource(String id) {
        return protectedJobs.get(id);
    }

    /**
     * Remove all the protected resources.
     *
     * yzhang Comment method "resetJobResources".
     */
    public void resetJobResources() {
        String[] ids = getProtectedIds();
        for (String id : ids) {
            protectedJobs.remove(id);
        }
    }

    /**
     * Getter for currentJobResource.
     *
     * @return the currentJobResource
     */
    public JobResource getCurrentJobResource() {
        return this.currentJobResource;
    }

    /**
     * bqian class global comment. Detailled comment <br/>
     *
     * $Id: TalendEditor.java 7860 2008-01-02 06:56:40Z qzhang $
     *
     */
    class TalendEditDomain extends DefaultEditDomain {

        /**
         * DOC bqian TalendEditor.TalendEditDomain class global comment. Detailled comment <br/>
         *
         * $Id: TalendEditor.java 7860 2008-01-02 06:56:40Z qzhang $
         *
         */
        class DragProcessor {

            int x;

            int y;

        }

        private DragProcessor processor = null;

        private boolean createConnection = false;

        private static final int DEFAULT_MOVE_OFFSET = GRID_SIZE;

        private Point startPoint = null;

        private int moveOffset = DEFAULT_MOVE_OFFSET;

        private IComponent previousComponent = null;

        /**
         * bqian TalendEditDomain constructor comment.
         *
         * @param editorPart
         */
        public TalendEditDomain(IEditorPart editorPart) {
            super(editorPart);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.gef.EditDomain#mouseDown(org.eclipse.swt.events.MouseEvent, org.eclipse.gef.EditPartViewer)
         */
        @Override
        public void mouseDown(org.eclipse.swt.events.MouseEvent mouseEvent, EditPartViewer viewer) {
            TalendEditorContextMenuProvider.setEnableContextMenu(true);
            createConnection = false;
            boolean flag = false;
            if (viewer.getSelectionManager() instanceof TalendSelectionManager) {
                Point point = new Point(mouseEvent.x, mouseEvent.y);
                ((TalendSelectionManager) viewer.getSelectionManager()).setSelectPoint(point);
                flag = true;
            }
            if (mouseEvent.button == 2) {
                getEditor().setCursor(Cursors.HAND);
                processor = new DragProcessor();
                processor.x = mouseEvent.x;
                processor.y = mouseEvent.y;
            } else if (mouseEvent.button == 3) {
                startPoint = new Point(mouseEvent.x, mouseEvent.y);

                RootEditPart rep = getGraphicalViewer().getRootEditPart();
                if (rep instanceof ScalableFreeformRootEditPart) {
                    ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                    Viewport viewport = (Viewport) root.getFigure();
                    Point viewOriginalPosition = viewport.getViewLocation();
                    startPoint.translate(viewOriginalPosition);
                }
                createConnection = true;
                super.mouseDown(mouseEvent, viewer);
            } else {
                super.mouseDown(mouseEvent, viewer);
                if (flag && getActiveTool() instanceof CreationTool) {
                    CreationTool tool = (CreationTool) getActiveTool();
                    updateNodeOnLink(tool);
                }

                final StructuredSelection newSelection = (StructuredSelection) viewer.getSelection();
                if (!newSelection.isEmpty() && newSelection.getFirstElement() instanceof TalendEntryEditPart) {
                    TalendEntryEditPart editPart = (TalendEntryEditPart) newSelection.getFirstElement();
                    if(editPart.getModel() instanceof TalendCombinedTemplateCreationEntry) {
                        TalendCombinedTemplateCreationEntry entry =
                                (TalendCombinedTemplateCreationEntry) editPart.getModel();
                        IComponent newComponent = entry.getComponent();
                        if (newComponent != null && newComponent instanceof StitchPseudoComponent) {
                            if (newComponent.equals(previousComponent)) { // check if we are clicking on it for a 2nd time
                                StitchPseudoComponent stitchPseudoComponent = (StitchPseudoComponent) newComponent;
                                try {
                                    final URL compURL = new URL(stitchPseudoComponent.getConnectorURL()
                                            + StitchDataLoaderConstants.getUTMParamSuffix());
                                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(compURL);
                                } catch (PartInitException | MalformedURLException e) {
                                    ExceptionHandler.process(e);
                                }
                                previousComponent = null; // remove the registered selection
                            } else { // if it's the first time selecting a stitch connector
                                previousComponent = newComponent; // register the first click
                            }
                            super.mouseUp(mouseEvent, viewer); // simulate the release of button to avoid dropping on canvas
                        } else { // user click at another component after a stitch component
                            previousComponent = null; // remove the registered selection
                        }
                    }
                } else { // user clicks at somewhere outside the palette: the canvas for instance
                    previousComponent = null; // remove the registered selection
                }
            }
        }

        private void execCommandStack(Command command) {
            CommandStack cs = getCommandStack();
            if (cs != null) {
                cs.execute(command);
            } else {
                command.execute();
            }
        }

        private void updateNodeOnLink(CreationTool tool) {
            try {
                Class toolClass = TargetingTool.class;
                Field targetRequestField = toolClass.getDeclaredField("targetRequest"); //$NON-NLS-1$
                Field targetEditpartField = toolClass.getDeclaredField("targetEditPart"); //$NON-NLS-1$
                targetRequestField.setAccessible(true);
                targetEditpartField.setAccessible(true);
                Request request = (Request) targetRequestField.get(tool);
                EditPart editPart = (EditPart) targetEditpartField.get(tool);

                if ((request instanceof CreateRequest) && readOnly) {
                    return;
                }

                if (request instanceof CreateRequest && selectedConnectionPart != null) {
                    Object object = ((CreateRequest) request).getNewObject();
                    if (object instanceof Node) {
                        Node node = (Node) object;
                        Point originalPoint = ((CreateRequest) request).getLocation();

                        RootEditPart rep = getViewer().getRootEditPart().getRoot();

                        Point viewOriginalPosition = new Point();
                        if (rep instanceof ScalableFreeformRootEditPart) {
                            ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                            Viewport viewport = (Viewport) root.getFigure();
                            viewOriginalPosition = viewport.getViewLocation();
                        }
                        Point point = new Point(originalPoint.x + viewOriginalPosition.x, originalPoint.y
                                + viewOriginalPosition.y);
                        point.x = (int) (point.x/AnimatableZoomManager.currentZoom);
                        point.y = (int) (point.y/AnimatableZoomManager.currentZoom);
                        // Connection targetConnection = CreateComponentOnLinkHelper.getSelectedConnection();
                        // for (Object child : getProcessPart().getChildren()) {
                        // if (child instanceof SubjobContainerPart) {
                        // CreateComponentOnLinkHelper.unselectAllConnections((SubjobContainerPart) child);
                        // }
                        // }
                        Connection targetConnection = (Connection) selectedConnectionPart.getModel();

                        if (targetConnection != null) {
                            NodeContainer nodeContainer = ((Process)node.getProcess()).loadNodeContainer(node, false);
                            if (getProcess() instanceof Process) {
                                CreateNodeContainerCommand createCmd = new CreateNodeContainerCommand((Process) getProcess(),
                                        nodeContainer, point);
                                execCommandStack(createCmd);

                                // reconnect the node
                                Node originalTarget = (Node) targetConnection.getTarget();

                                EConnectionType connectionType = EConnectionType.FLOW_MAIN;
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                                    ICamelDesignerCoreService camelService = GlobalServiceRegister
                                            .getDefault().getService(ICamelDesignerCoreService.class);
                                    if (camelService.isRouteBuilderNode(node)) {
                                        connectionType = camelService.getTargetConnectionType(node);
                                    }
                                }
                                INodeConnector targetConnector = node.getConnectorFromType(connectionType);
                                for (INodeConnector connector : node.getConnectorsFromType(connectionType)) {
                                    if (connector.getMaxLinkOutput() != 0) {
                                        targetConnector = connector;
                                        break;
                                    }
                                }
                                ConnectionCreateCommand.setCreatingConnection(true);
                                // System.out.println("old: " + targetConnection.getSource().getUniqueName() + "-----"
                                // + targetConnection.getUniqueName() + "----->"
                                // + targetConnection.getTarget().getUniqueName());

                                // FIXME perhaps, this is not good fix, need check it later
                                // bug 21411
                                if (PluginChecker.isJobLetPluginLoaded()) {
                                    IJobletProviderService service = GlobalServiceRegister.getDefault()
                                            .getService(IJobletProviderService.class);
                                    if (service != null && service.isJobletComponent(targetConnection.getTarget())) {
                                        if (targetConnection.getTarget() instanceof Node) {
                                            NodeContainer jobletContainer = ((Node) targetConnection.getTarget())
                                                    .getNodeContainer();
                                            // remove the old connection in the container
                                            jobletContainer.getInputs().remove(targetConnection);
                                        }
                                    }
                                }

                                ConnectionReconnectCommand cmd2 = new ConnectionReconnectCommand(targetConnection);
                                cmd2.setNewTarget(node);
                                execCommandStack(cmd2);

                                // System.out.print("new: " + targetConnection.getSource().getUniqueName() + "-----"
                                // + targetConnection.getUniqueName() + "----->"
                                // + targetConnection.getTarget().getUniqueName() + "(new)");

                                // INodeConnector nodeConnector = node.getConnectorFromName(targetConnector.getName());
                                // nodeConnector.setCurLinkNbInput(nodeConnector.getCurLinkNbInput() + 1);
                                List<Object> nodeArgs = null;
                                if (connectionType == EConnectionType.ROUTE || connectionType == EConnectionType.ROUTE_ENDBLOCK) {
                                    nodeArgs = new ArrayList<Object>();
                                    nodeArgs.add(null);
                                    nodeArgs.add(ConnectionUtil.generateUniqueConnectionName(connectionType,
                                            originalTarget.getProcess(), targetConnector));
                                    nodeArgs.add(null);
                                } else {
                                    nodeArgs = CreateComponentOnLinkHelper.getTargetArgs(targetConnection, node);
                                }

                                ConnectionCreateCommand nodeCmd = new ConnectionCreateCommand(node, targetConnector.getName(),
                                        nodeArgs, false);
                                nodeCmd.setTarget(originalTarget);
                                // INodeConnector originalNodeConnector =
                                // originalTarget.getConnectorFromName(targetConnection
                                // .getTargetNodeConnector().getName());
                                // originalNodeConnector.setCurLinkNbInput(originalNodeConnector.getCurLinkNbInput() -
                                // 1);
                                execCommandStack(nodeCmd);

                                // System.out.println("-----" + nodeCmd.getConnection().getUniqueName() + "(new)----->"
                                // + originalTarget.getUniqueName());

                                // TDI-22977:need judge the current Drag/Drop node's outputs for update the target
                                // Setting,such as the target is TMap
                                if (node.getOutgoingConnections().size() > 0) {
                                    if (node.getExternalNode() instanceof MapperExternalNode) {
                                        CreateComponentOnLinkHelper.setupTMap(node);
                                    }
                                    if (originalTarget.getExternalNode() instanceof MapperExternalNode) {
                                        CreateComponentOnLinkHelper.updateTMap(originalTarget, targetConnection, node
                                                .getOutgoingConnections().get(0));
                                    }
                                    originalTarget.renameData(targetConnection.getName(), node.getOutgoingConnections().get(0)
                                            .getName());
                                }
                                if (!ConnectionCreateCommand.isCreatingConnection()) {
                                    return;
                                }
                            }
                        }
                    }
                }

                if (request instanceof CreateRequest) {
                    CreateRequest cRequest = (CreateRequest) request;
                    Object newObject = cRequest.getNewObject();
                    boolean isNodeInstance = (newObject instanceof Node);
                    boolean isNoteInstance = (newObject instanceof Note);
                    if (isNodeInstance || isNoteInstance) {
                        // step 1: get the orginal position in editor
                        Point originalPoint = ((CreateRequest) request).getLocation();
                        RootEditPart rep = getViewer().getRootEditPart().getRoot();
                        Point viewOriginalPosition = new Point();
                        if (rep instanceof ScalableFreeformRootEditPart) {
                            ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                            Viewport viewport = (Viewport) root.getFigure();
                            viewOriginalPosition = viewport.getViewLocation();
                        }
                        Point point = new Point(originalPoint.x + viewOriginalPosition.x, originalPoint.y
                                + viewOriginalPosition.y);
                        point.x = (int) (point.x/AnimatableZoomManager.currentZoom);
                        point.y = (int) (point.y/AnimatableZoomManager.currentZoom);
                        // step 2: create node/note
                        CreateCommand createCmd = null;
                        if (isNodeInstance) {
                            Node node = (Node) newObject;
                            // TDI-23304 this bug is caused by TDI-23058
                            if (!node.getComponent().getComponentType().equals(EComponentType.JOBLET)) {
                                NodeContainer nodeContainer = ((Process)node.getProcess()).loadNodeContainer(node, false);
                                createCmd = new CreateNodeContainerCommand((Process) getProcess(), nodeContainer, point);
                            }
                        } else if (isNoteInstance) {
                            createCmd = new CreateNoteCommand((Process) getProcess(), (Note) newObject, point);
                        }
                        if (createCmd != null) {
                            execCommandStack(createCmd);
                        }
                        // IComponent component = ((Node) cRequest.getNewObject()).getComponent();
                        // ModulesInstallerUtil.installModules(getSite().getShell(), component);
                    }
                }

            } catch (SecurityException e) {
                ExceptionHandler.process(e);
            } catch (NoSuchFieldException e) {
                ExceptionHandler.process(e);
            } catch (IllegalArgumentException e) {
                ExceptionHandler.process(e);
            } catch (IllegalAccessException e) {
                ExceptionHandler.process(e);
            }
        }

        public void updateViewport(int offX, int offY) {
            RootEditPart rep = getGraphicalViewer().getRootEditPart();

            if (rep instanceof ScalableFreeformRootEditPart) {
                ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                Viewport viewport = (Viewport) root.getFigure();
                Point viewOriginalPosition = viewport.getViewLocation();
                viewOriginalPosition.x -= offX;
                viewOriginalPosition.y -= offY;

                viewport.setViewLocation(viewOriginalPosition.x, viewOriginalPosition.y);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.gef.EditDomain#mouseDrag(org.eclipse.swt.events.MouseEvent, org.eclipse.gef.EditPartViewer)
         */
        @Override
        public void mouseDrag(org.eclipse.swt.events.MouseEvent mouseEvent, EditPartViewer viewer) {
            super.mouseDrag(mouseEvent, viewer);
            if (processor != null) {
                int offX = mouseEvent.x - processor.x;
                int offY = mouseEvent.y - processor.y;

                updateViewport(offX, offY);
                processor.x = mouseEvent.x;
                processor.y = mouseEvent.y;
            } else if (createConnection) {
                handleCreateDefaultConnection(new Point(mouseEvent.x, mouseEvent.y));
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.gef.EditDomain#mouseUp(org.eclipse.swt.events.MouseEvent, org.eclipse.gef.EditPartViewer)
         */
        @Override
        public void mouseUp(org.eclipse.swt.events.MouseEvent mouseEvent, EditPartViewer viewer) {
            createConnection = false;
            selectedConnectionPart = null;
            CreateComponentOnLinkHelper.unselectAllConnections(getProcessPart());
            if (mouseEvent.button != 2) {
                super.mouseUp(mouseEvent, viewer);
            } else {
                getEditor().setCursor(null);
                processor = null;
            }
        }

        @Override
        public void keyDown(org.eclipse.swt.events.KeyEvent keyEvent, EditPartViewer viewer) {
            int keyCode = keyEvent.keyCode;//
            if (selectedConnectionPart != null) {
                if (selectedConnectionPart != null) {
                    CreateComponentOnLinkHelper.unselectConnection(selectedConnectionPart);
                }
                selectedConnectionPart = null;
            } else if (keyEvent.stateMask == SWT.CTRL
                    && (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_DOWN || keyCode == SWT.ARROW_LEFT || keyCode == SWT.ARROW_RIGHT)) {
                List<EditPart> parts = viewer.getSelectedEditParts();
                if (parts == null) {
                    return;
                }
                for (EditPart part : parts) {
                    if (part instanceof NodePart) {
                        Node node = (Node) part.getModel();
                        if (node != null) {
                            moveShape(keyCode, node, moveOffset);
                        }
                    }
                }
                moveOffset++;
            } else {
                super.keyDown(keyEvent, viewer);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.gef.EditDomain#keyUp(org.eclipse.swt.events.KeyEvent, org.eclipse.gef.EditPartViewer)
         */
        @Override
        public void keyUp(KeyEvent keyEvent, EditPartViewer viewer) {
            super.keyUp(keyEvent, viewer);
            if (moveOffset != DEFAULT_MOVE_OFFSET) {
                moveOffset = DEFAULT_MOVE_OFFSET;
            }
        }

        /**
         * DOC bqian Comment method "moveShape".
         *
         * @param keyCode
         * @param shape
         */
        private void moveShape(int keyCode, Node node, int offset) {

            Point location = node.getLocation().getCopy();
            if (location == null) {
                return;
            }
            switch (keyCode) {
            case SWT.ARROW_UP:
                location.y = location.y - offset;
                break;
            case SWT.ARROW_DOWN:
                location.y = location.y + offset;
                break;
            case SWT.ARROW_LEFT:
                location.x = location.x - offset;
                break;
            case SWT.ARROW_RIGHT:
                location.x = location.x + offset;
                break;
            default:
                // do nothing
            }
            MoveNodeCommand locationCommand = new MoveNodeCommand(node, location);
            getCommandStack().execute(locationCommand);
        }

        public void handleCreateDefaultConnection(Point currentLocation) {
            if (getActiveTool() instanceof TalendConnectionCreationTool) {
                // if a connection is already in creation, no need to create it again
                return;
            }

            if (getViewer().getSelectedEditParts().size() != 1) {
                return;
            }

            EditPart part = (EditPart) getViewer().getSelectedEditParts().get(0);
            if (!(part instanceof NodePart)) {
                return;
            }

            IFigure figure = ((NodePart) part).getFigure();

            ZoomManager zoomManager = (ZoomManager) AbstractTalendEditor.this.getAdapter(ZoomManager.class);
            double zoom = zoomManager.getZoom();
            if (zoom > 1e-7) {
                startPoint.scale(1 / zoom);
            }

            if (!figure.getBounds().contains(startPoint)) {
                // if the start location of the drag is not in the component, ignore it
                return;
            }

            if (startPoint.getDistance2(currentLocation) < 30) {
                // need to move a minimum the mouse for the drag, if not enough, display the context menu
                return;
            }

            final INodeConnector mainConnector = new NodeConnectorTool((NodePart) part).getConnector();
            if (mainConnector == null) {
                return;
            }

            figure.translateToAbsolute(startPoint);

            Canvas canvas = (Canvas) getViewer().getControl();
            Event event = new Event();
            event.button = 3;
            event.count = 0;
            event.detail = 0;
            event.end = 0;
            event.height = 0;
            event.keyCode = 0;
            event.start = 0;
            event.stateMask = 0;
            event.time = 9516624; // any old time... doesn't matter
            event.type = 3;
            event.widget = canvas;
            event.width = 0;
            event.x = startPoint.x + 3;
            event.y = startPoint.y + 3;
            /**
             * Set the connection tool to be the current tool
             */

            final List<Object> listArgs = new ArrayList<Object>();

            listArgs.add(null);
            listArgs.add(null);
            listArgs.add(null);

            TalendConnectionCreationTool myConnectTool = new TalendConnectionCreationTool(new CreationFactory() {

                @Override
                public Object getNewObject() {
                    return listArgs;
                }

                @Override
                public Object getObjectType() {
                    return mainConnector.getName();
                }
            }, false);
            myConnectTool.performConnectionStartWith(part);
            getViewer().getEditDomain().setActiveTool(myConnectTool);
            canvas.notifyListeners(3, event);
            TalendEditorContextMenuProvider.setEnableContextMenu(false);
        }

        @Override
        public void mouseMove(MouseEvent mouseEvent, EditPartViewer viewer) {
            boolean flag = false;
            if (viewer.getSelectionManager() instanceof TalendSelectionManager) {
                flag = true;
            }
            if (flag && getActiveTool() instanceof CreationTool) {
                CreationTool tool = (CreationTool) getActiveTool();
                Class toolClass = TargetingTool.class;
                Field targetRequestField;
                Request request = null;
                try {
                    targetRequestField = toolClass.getDeclaredField("targetRequest");
                    targetRequestField.setAccessible(true);
                    request = (Request) targetRequestField.get(tool);
                } catch (SecurityException e) {
                    ExceptionHandler.process(e);
                } catch (NoSuchFieldException e) {
                    ExceptionHandler.process(e);
                } catch (IllegalArgumentException e) {
                    ExceptionHandler.process(e);
                } catch (IllegalAccessException e) {
                    ExceptionHandler.process(e);
                }
                if (request instanceof CreateRequest && ((CreateRequest) request).getNewObject() instanceof Node) {
                    Node node = (Node) ((CreateRequest) request).getNewObject();
                    RootEditPart rep = getViewer().getRootEditPart().getRoot();

                    Point viewOriginalPosition = new Point();
                    if (rep instanceof ScalableFreeformRootEditPart) {
                        ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                        Viewport viewport = (Viewport) root.getFigure();
                        viewOriginalPosition = viewport.getViewLocation();
                    }

                    org.eclipse.draw2d.geometry.Point draw2dPosition = new org.eclipse.draw2d.geometry.Point(mouseEvent.x
                            + viewOriginalPosition.x, mouseEvent.y + viewOriginalPosition.y);
                    double zoom = 1.0;
                    if (viewer.getRootEditPart() instanceof TalendScalableFreeformRootEditPart) {
                        ZoomManager zoomManager = ((TalendScalableFreeformRootEditPart) viewer.getRootEditPart())
                                .getZoomManager();
                        zoom = zoomManager.getZoom();
                    }

                    List<ConnectionPart> connectionParts = CreateComponentOnLinkHelper.getConnectionParts(getProcessPart(),
                            draw2dPosition, node);

                    double minDistance = 1000000000;
                    for (ConnectionPart part : connectionParts) {
                        if (part.getFigure() instanceof PolylineConnection) {
                            PolylineConnection connection = (PolylineConnection) part.getFigure();
                            double distance = CreateComponentOnLinkHelper.getDistanceOrthogonal(draw2dPosition.x,
                                    draw2dPosition.y, connection, zoom);
                            if (distance < minDistance) {
                                selectedConnectionPart = part;
                                minDistance = Math.min(distance, minDistance);
                            }
                        }
                    }

                    if (selectedConnectionPart != null && minDistance < 15) {
                        for (Object child : getProcessPart().getChildren()) {
                            if (child instanceof SubjobContainerPart) {
                                CreateComponentOnLinkHelper.unselectAllConnections((SubjobContainerPart) child);
                            }
                        }
                        CreateComponentOnLinkHelper.selectConnection(selectedConnectionPart);
                    } else {
                        if (selectedConnectionPart != null) {
                            CreateComponentOnLinkHelper.unselectConnection(selectedConnectionPart);
                        }
                        selectedConnectionPart = null;
                    }

                }
            }
            super.mouseMove(mouseEvent, viewer);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#getCommandStack()
     */
    @Override
    public CommandStack getCommandStack() {
        return super.getCommandStack();
    }

    /**
     * Comment method "getAction".
     *
     * @param actionID
     * @return
     */
    public IAction getAction(String actionID) {
        return getActionRegistry().getAction(actionID);
    }

    private KeyHandler sharedKeyHandler;

    /**
     * Sets the parent.
     *
     * @param parent the parent to set
     */
    public void setParent(AbstractMultiPageTalendEditor parent) {
        this.parent = parent;
    }

    /**
     * DOC qzhang Comment method "getCommonKeyHandler".
     *
     * @return
     */
    public KeyHandler getCommonKeyHandler() {
        if (sharedKeyHandler == null) {
            sharedKeyHandler = new TalendEditorKeyHandler();
            sharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 0), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
            // deactivate the F2 shortcut as it's not used anymore
            // sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
            // getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
        }
        return sharedKeyHandler;
    }

    protected void displayHelp() {

        ISelection selection = getGraphicalViewer().getSelection();
        if (selection != null) {
            if (selection instanceof IStructuredSelection) {

                Object input = ((IStructuredSelection) selection).getFirstElement();
                Node node = null;
                if (input instanceof NodeTreeEditPart) {
                    NodeTreeEditPart nTreePart = (NodeTreeEditPart) input;
                    node = (Node) nTreePart.getModel();
                } else {
                    if (input instanceof NodePart) {
                        EditPart editPart = (EditPart) input;
                        node = (Node) editPart.getModel();
                    }
                }
                if (node != null) {
                    if (ComponentsHelpUtil.isUseOnLineHelp()) {
                        if (!node.isJoblet() && node.getComponent() != null && node.getComponent().isLoaded()
                                && node.getComponent().isMadeByTalend()) {
                            ComponentsHelpUtil.openLineHelp(node.getComponent().getDisplayName());
                        }
                    } else {
                        String helpLink = (String) node.getPropertyValue(EParameterName.HELP.getName());
                        String requiredHelpLink = ((Process) node.getProcess()).getBaseHelpLink() + node.getComponent().getName();
                        if (helpLink == null || "".equals(helpLink) || !requiredHelpLink.equals(helpLink)) {
                            helpLink = ((Process) node.getProcess()).getBaseHelpLink() + node.getComponent().getName();
                        }
                        PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpLink);
                    }
                }
            }
        }
    }

    protected TalendEditorDropTargetListener talendEditorDropTargetListener = null;

    // ------------------------------------------------------------------------
    // Abstract methods from GraphicalEditor

    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        // this uses the PartFactory set in configureGraphicalViewer
        // to create an EditPart for the diagram and sets it as the
        // content for the viewer
        IProcess2 process = getProcess();
        getGraphicalViewer().setContents(process);

        // containers are not correctly updated by default, so update them after all nodes have been added
        if (process != null) {
            for (ISubjobContainer subjobContainer : process.getSubjobContainers()) {
                subjobContainer.updateSubjobContainer();
            }
        }
        getGraphicalViewer().getControl().addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {
                updateActions(getSelectionActions());
            }
        });
        talendEditorDropTargetListener = new TalendEditorDropTargetListener(this);
        getGraphicalViewer().addDropTargetListener(talendEditorDropTargetListener);
    }

    // ------------------------------------------------------------------------
    // Abstract methods from EditorPart

    /**
     * Outline view page. <br/>
     *
     * $Id: TalendEditor.java 7860 2008-01-02 06:56:40Z qzhang $
     *
     */
    class OutlinePage extends ContentOutlinePage implements IAdaptable {

        private PageBook pageBook;

        private Control outline;

        private Canvas overview;

        private IAction showOutlineAction, showOverviewAction, linkWithEditorAction;

        static final int ID_OUTLINE = 0;

        static final int ID_OVERVIEW = 1;

        static final int BORDER_SIZE = 3;

        private Thumbnail thumbnail;

        private DisposeListener disposeListener;

        public OutlinePage(EditPartViewer viewer) {
            super(viewer);
        }

        @Override
        public void init(final IPageSite pageSite) {
            super.init(pageSite);
            ActionRegistry registry = getActionRegistry();
            IActionBars bars = pageSite.getActionBars();
            String id = ActionFactory.UNDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.REDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.DELETE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            bars.updateActionBars();
        }

        protected void configureOutlineViewer() {
            getViewer().setEditDomain(getEditDomain());
            getViewer().setEditPartFactory(new ProcessTreePartFactory());
            getViewer().setKeyHandler(getCommonKeyHandler());
            IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
            showOutlineAction = new Action() {

                @Override
                public String getId() {
                    return "ID_OUTLINE"; //$NON-NLS-1$
                }

                @Override
                public void run() {
                    showPage(ID_OUTLINE);
                }
            };
            showOutlineAction.setImageDescriptor(ImageDescriptor.createFromFile(DesignerPlugin.class, "/icons/outline.png")); //$NON-NLS-1$
            tbm.add(showOutlineAction);
            showOverviewAction = new Action() {

                @Override
                public String getId() {
                    return "ID_OVERVIEW"; //$NON-NLS-1$
                }

                @Override
                public void run() {
                    showPage(ID_OVERVIEW);
                }
            };
            showOverviewAction.setImageDescriptor(ImageDescriptor.createFromFile(DesignerPlugin.class, "/icons/overview.gif")); //$NON-NLS-1$
            tbm.add(showOverviewAction);
            linkWithEditorAction = new Action() {

                @Override
                public void run() {
                    ISelection selection = getViewer().getSelection();
                    if (selection != null) {
                        if (selection instanceof IStructuredSelection) {
                            Object input = ((IStructuredSelection) selection).getFirstElement();
                            if (input instanceof NodeTreeEditPart) {
                                isSynchronizer = true;
                                getViewer().setSelection(selection);
                                isSynchronizer = false;
                            }
                        }
                    }
                }
            };
            linkWithEditorAction.setImageDescriptor(ImageDescriptor.createFromFile(DesignerPlugin.class, "/icons/synced.png")); //$NON-NLS-1$
            tbm.add(linkWithEditorAction);
            getSite().getActionBars().updateActionBars();

            showPage(ID_OUTLINE);
        }

        @Override
        public void createControl(final Composite parent) {

            pageBook = new PageBook(parent, SWT.NONE);
            outline = getViewer().createControl(pageBook);
            overview = new Canvas(pageBook, SWT.NONE);
            pageBook.showPage(outline);
            configureOutlineViewer();
            hookOutlineViewer();
            initializeOutlineViewer();
        }

        @Override
        public void dispose() {
            unhookOutlineViewer();
            if (thumbnail != null) {
                thumbnail.deactivate();
                thumbnail = null;
            }
            super.dispose();
            AbstractTalendEditor.this.outlinePage = null;
            outlinePage = null;
        }

        @Override
        public Object getAdapter(final Class type) {
            if (type == ZoomManager.class) {
                return getGraphicalViewer().getProperty(ZoomManager.class.toString());
            }
            return null;
        }

        @Override
        public Control getControl() {
            return pageBook;
        }

        protected void hookOutlineViewer() {
            getSelectionSynchronizer().addViewer(getViewer());
        }

        protected void initializeOutlineViewer() {
            setContents(getProcess());
        }

        protected void initializeOverview() {
            LightweightSystem lws = new LightweightSystem(overview);
            RootEditPart rep = getGraphicalViewer().getRootEditPart();
            if (rep instanceof ScalableFreeformRootEditPart) {
                ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                thumbnail = new ScrollableThumbnail((Viewport) root.getFigure());
                thumbnail.setBorder(new SimpleRaisedBorder());
                thumbnail.setSource(root.getLayer(LayerConstants.PRINTABLE_LAYERS));
                lws.setContents(thumbnail);
                disposeListener = new DisposeListener() {

                    @Override
                    public void widgetDisposed(final DisposeEvent e) {
                        if (thumbnail != null) {
                            thumbnail.deactivate();
                            thumbnail = null;
                        }
                    }
                };
                getEditor().addDisposeListener(disposeListener);
            }
        }

        public void setContents(final Object contents) {
            getViewer().setContents(contents);
        }

        protected void showPage(final int id) {
            if (id == ID_OUTLINE) {
                showOutlineAction.setChecked(true);
                showOverviewAction.setChecked(false);
                pageBook.showPage(outline);
                if (thumbnail != null) {
                    thumbnail.setVisible(false);
                }
            } else if (id == ID_OVERVIEW) {
                if (thumbnail == null) {
                    initializeOverview();
                }
                showOutlineAction.setChecked(false);
                showOverviewAction.setChecked(true);
                pageBook.showPage(overview);
                thumbnail.setVisible(true);
            }
        }

        protected void unhookOutlineViewer() {
            IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
            IContributionItem outlineItem = tbm.remove(showOutlineAction.getId());
            IContributionItem overviewItem = tbm.remove(showOverviewAction.getId());
            MToolBarElement outlineElement = null;
            MToolBarElement overviewElement = null;
            try {
                // cf TDI-34745
                // try to release items that are not released from the e4 model
                ActionBars actionBars = null;
                if (getSite().getActionBars() instanceof SubActionBars) {
                    actionBars = (ActionBars) ReflectionUtils.getPrivateField(getSite().getActionBars(), "parent"); //$NON-NLS-1$
                }
                if (actionBars != null && outlineItem != null && overviewItem != null) {
                    Object object = ReflectionUtils.getPrivateField(actionBars, "part"); //$NON-NLS-1$
                    if (object != null) {
                        MPart part = (MPart) object;
                        MToolBar toolbar = part.getToolbar();
                        if (toolbar != null) {
                            Object renderer = toolbar.getRenderer();
                            if (renderer instanceof ToolBarManagerRenderer) {
                                // update the mapping of opaque items
                                ToolBarManagerRenderer tbRenderer = ((ToolBarManagerRenderer) renderer);
                                outlineElement = tbRenderer.getToolElement(outlineItem);
                                overviewElement = tbRenderer.getToolElement(overviewItem);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // do nothing, since this part is only to tryto help to release memory (because opaqueelement are never
                // released)
            }
            if (outlineItem != null) {
                outlineItem.dispose();
            }
            if (overviewItem != null) {
                overviewItem.dispose();
            }
            getSite().getActionBars().updateActionBars();

            if (outlineElement != null && OpaqueElementUtil.isOpaqueToolItem(outlineElement)) {
                OpaqueElementUtil.clearOpaqueItem(outlineElement);
            }
            if (overviewElement != null && OpaqueElementUtil.isOpaqueToolItem(overviewElement)) {
                OpaqueElementUtil.clearOpaqueItem(overviewElement);
            }
            showOutlineAction = null;
            showOverviewAction = null;
            linkWithEditorAction = null;
            getSelectionSynchronizer().removeViewer(getViewer());
            if (disposeListener != null && getEditor() != null && !getEditor().isDisposed()) {
                getEditor().removeDisposeListener(disposeListener);
            }
        }
    }

    private SelectionSynchronizer synchronizer;

    @Override
    protected SelectionSynchronizer getSelectionSynchronizer() {
        if (synchronizer == null) {
            synchronizer = new SelectionSynchronizer() {

                @Override
                protected EditPart convert(EditPartViewer viewer, EditPart part) {
                    EditPart editPart = super.convert(viewer, part);
                    if (editPart == null) {
                        // maybe, not good, should be only for outline.
                        editPart = super.convert(viewer, part.getParent());
                    }
                    return editPart;
                }

                @Override
                protected void applySelection(EditPartViewer viewer, ISelection selection) {
                    if (!isSynchronizer) {
                        return;
                    }
                    super.applySelection(viewer, selection);
                }
            };
        }

        return synchronizer;

    }

    /**
     * Getter for readOnly.
     *
     * @return the readOnly
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    class TalendEditorKeyHandler extends KeyHandler {

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event != null && SWT.F1 == event.keyCode && 0 == event.stateMask) {
                displayHelp();
                event.doit = false;
                return true;
            } else {
                return super.keyPressed(event);
            }
        }
    }
}

