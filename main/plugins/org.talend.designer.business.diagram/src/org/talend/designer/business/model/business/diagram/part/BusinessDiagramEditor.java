package org.talend.designer.business.model.business.diagram.part;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.actions.ActionIds;
import org.eclipse.gmf.runtime.diagram.ui.editparts.CompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.NoteEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.editparts.NoteAttachmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.document.StorageDiagramDocumentProvider;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.editor.FileDiagramEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.core.ui.ILastVersionChecker;
import org.talend.designer.business.diagram.custom.actions.DiagramResourceManager;
import org.talend.designer.business.diagram.custom.actions.SaveAsBusinessModelAction;
import org.talend.designer.business.diagram.custom.dnd.BusinessDiagramDropTargetListener;
import org.talend.designer.business.diagram.custom.edit.parts.BaseBusinessItemRelationShipEditPart;
import org.talend.designer.business.diagram.custom.edit.parts.BusinessItemShapeEditPart;
import org.talend.designer.business.diagram.custom.figures.BusinessItemShapeFigure;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.designer.business.model.business.diagram.edit.parts.BusinessEditPartFactory;
import org.talend.designer.business.model.business.diagram.edit.parts.BusinessProcessEditPart;
import org.talend.designer.business.model.business.diagram.providers.BusinessDiagramActionProvider;
import org.talend.designer.core.ui.ActiveProcessTracker;
import org.talend.designer.core.ui.views.jobsettings.JobSettingsView;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;

/**
 * @generated
 */
public class BusinessDiagramEditor extends FileDiagramEditor implements IGotoMarker, ILastVersionChecker {

    /**
     * @generated
     */
    public static final String ID = "org.talend.designer.business.model.business.diagram.part.BusinessDiagramEditorID"; //$NON-NLS-1$

    protected boolean keepPropertyLocked; // used only if the user try to open more than one editor at a time.

    private Boolean readOnly;

    public static final String BUSINESS_DIAGRAM_EDITOR_IMAGE_PATH = "/icons/obj16/BusinessDiagramFile_White.png"; //$NON-NLS-1$

    /**
     * @generated
     */
    public BusinessDiagramEditor() {
        super(true);
        ActiveProcessTracker.initialize();
    }

    /**
     * @generated
     */
    @Override
    protected String getEditingDomainID() {
        return "org.talend.designer.business.diagram.EditingDomain"; //$NON-NLS-1$
    }

    /**
     * @generated
     */
    @Override
    protected TransactionalEditingDomain createEditingDomain() {
        TransactionalEditingDomain domain = super.createEditingDomain();
        domain.setID(getEditingDomainID());
        return domain;
    }

    /**
     * @generated
     */
    @Override
    protected void setDocumentProvider(IEditorInput input) {
        if (input.getAdapter(IFile.class) != null) {
            setDocumentProvider(new BusinessDocumentProvider());
        } else {
            setDocumentProvider(new StorageDiagramDocumentProvider());
        }
    }

    /**
     * @generated
     */
    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();

        IDiagramGraphicalViewer viewer = getDiagramGraphicalViewer();
        // customize popup menu
        ContextMenuProvider provider = new BusinessDiagramActionProvider(this, viewer);
        viewer.setContextMenu(provider);
        getSite().registerContextMenu(ActionIds.DIAGRAM_EDITOR_CONTEXT_MENU, provider, viewer);

        DiagramRootEditPart root = (DiagramRootEditPart) getDiagramGraphicalViewer().getRootEditPart();
        LayeredPane printableLayers = (LayeredPane) root.getLayer(LayerConstants.PRINTABLE_LAYERS);
        FreeformLayer extLabelsLayer = new FreeformLayer();
        extLabelsLayer.setLayoutManager(new DelegatingLayout());
        printableLayers.addLayerAfter(extLabelsLayer, BusinessEditPartFactory.EXTERNAL_NODE_LABELS_LAYER,
                LayerConstants.PRIMARY_LAYER);
        LayeredPane scalableLayers = (LayeredPane) root.getLayer(LayerConstants.SCALABLE_LAYERS);
        FreeformLayer scaledFeedbackLayer = new FreeformLayer();
        scaledFeedbackLayer.setEnabled(false);
        scalableLayers.addLayerAfter(scaledFeedbackLayer, LayerConstants.SCALED_FEEDBACK_LAYER,
                DiagramRootEditPart.DECORATION_UNPRINTABLE_LAYER);
    }

    /**
     * @generated
     */
    @Override
    protected PreferencesHint getPreferencesHint() {
        return BusinessDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT;
    }

    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        getDiagramGraphicalViewer().addDropTargetListener(new BusinessDiagramDropTargetListener(getDiagramGraphicalViewer()));
    }

    RepositoryEditorInput repositoryEditorInput;

    @Override
    public void setInput(IEditorInput input) {
        super.setInput(input);
        Object obj = input.getAdapter(RepositoryEditorInput.class);
        if (obj instanceof RepositoryEditorInput) {
            repositoryEditorInput = (RepositoryEditorInput) obj;
        }
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        if (repositoryEditorInput != null) {
            final BusinessProcessItem businessProcessItem = (BusinessProcessItem) repositoryEditorInput.getItem();
            IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
            IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
            try {
                repFactory.updateLockStatus();
                // For TDI-23825, if not lock by user try to lock again.
                boolean locked = repFactory.getStatus(businessProcessItem) == ERepositoryStatus.LOCK_BY_USER;
                if (!locked) {
                    repFactory.lock(businessProcessItem);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            ERepositoryStatus status = repFactory.getStatus(businessProcessItem);
            if (!status.equals(ERepositoryStatus.LOCK_BY_USER) && !repFactory.getRepositoryContext().isEditableAsReadOnly()) {
                MessageDialog.openWarning(getEditorSite().getShell(),
                        Messages.getString("BusinessDiagramEditor.canNotSaveTitle"), //$NON-NLS-1$
                        Messages.getString("BusinessDiagramEditor.canNotSaveMessage")); //$NON-NLS-1$
                return;
            }
            super.doSave(progressMonitor);
            DiagramResourceManager diagramResourceManager = new DiagramResourceManager();
            diagramResourceManager.updateFromResource(businessProcessItem, repositoryEditorInput.getFile());
            final IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            RepositoryWorkUnit rwu = new RepositoryWorkUnit("save Business") {

                @Override
                protected void run() throws LoginException, PersistenceException {
                    ProxyRepositoryFactory.getInstance().save(businessProcessItem);
                }
            };
            rwu.setAvoidUnloadResources(true);
            rwu.setAvoidSvnUpdate(true);
            factory.executeRepositoryWorkUnit(rwu);
            propertyIsDirty = false;
            firePropertyChange(IEditorPart.PROP_DIRTY);

        }
    }

    // private void saveSVG(BusinessProcessItem businessProcessItem) {
    // if (PluginChecker.isTIS()) {
    // EList businessItems = businessProcessItem.getSemantic().getBusinessItems();
    // // OutputStream outputStream = new FileOutputStream("d:\\businessModelDemo.svg");
    // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    // SVGImageExporter.export(this.getDiagramGraphicalViewer(), outputStream, businessItems);
    // byte[] svgbyteArray = outputStream.toByteArray();
    // ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
    // byteArray.setInnerContent(svgbyteArray);
    //
    // SVGBusinessProcessItem svgBusinessProcessItem = businessProcessItem.getSvgBusinessProcessItem();
    // boolean serialized = true;
    // if (svgBusinessProcessItem == null) {
    // serialized = false;
    // svgBusinessProcessItem = PropertiesFactory.eINSTANCE.createSVGBusinessProcessItem();
    // }
    // svgBusinessProcessItem.eResource();
    // if (svgBusinessProcessItem.getProperty() == null) {
    // Property property = PropertiesFactory.eINSTANCE.createProperty();
    // property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
    // .getUser());
    // property.setVersion(VersionUtils.DEFAULT_VERSION);
    //                property.setStatusCode(""); //$NON-NLS-1$
    // property.setLabel(businessProcessItem.getProperty().getLabel() + "_svg");
    // property.setId("svg_" + businessProcessItem.getProperty().getId());
    // svgBusinessProcessItem.setProperty(property);
    // }
    // svgBusinessProcessItem.setBusinessProcessItem(businessProcessItem);
    // svgBusinessProcessItem.setName(businessProcessItem.getProperty().getLabel() + "_svg");
    // svgBusinessProcessItem.setContent(byteArray);
    //
    // businessProcessItem.setSvgBusinessProcessItem(svgBusinessProcessItem);
    //
    // try {
    // if (!serialized || svgBusinessProcessItem.eResource() == null) {
    // ProxyRepositoryFactory.getInstance().create(svgBusinessProcessItem,
    // new Path(businessProcessItem.getState().getPath()));
    // } else {
    // ProxyRepositoryFactory.getInstance().save(svgBusinessProcessItem);
    // }
    // } catch (PersistenceException e) {
    // ExceptionHandler.process(e);
    // }
    // }
    // }

    @Override
    public boolean isEditable() {
        if (repositoryEditorInput != null) {
            if (repositoryEditorInput.isReadOnly()) {
                return false;
            }
            return !checkReadOnly();
        }
        return super.isEditable();
    }

    // fix for bug 22597
    private boolean checkReadOnly() {
        if (readOnly == null) {
            Item item = repositoryEditorInput.getItem();
            IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
            readOnly = !isLastVersion(item) || !repositoryFactory.isEditableAndLockIfPossible(item);
        }
        return readOnly;
    }

    @Override
    public boolean isDirty() {
        return propertyIsDirty || super.isDirty();
    }

    @Override
    public void dispose() {
        getSite().setSelectionProvider(null);
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);

        super.dispose();

        if (isKeepPropertyLocked()) {
            return;
        }
        // Unlock the process :
        IProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
        try {
            // Property property = repFactory.reload(repositoryEditorInput.getItem().getProperty());
            Property property = repFactory.getUptodateProperty(repositoryEditorInput.getItem().getProperty());
            repositoryEditorInput.setItem(property.getItem());
            repFactory.unlock(repositoryEditorInput.getItem());
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (LoginException e) {
            ExceptionHandler.process(e);
        }
        RepositoryNode repositoryNode = repositoryEditorInput.getRepositoryNode();
        if (repositoryNode == null) {
            repositoryEditorInput.setRepositoryNode(null); // retrieve the node.
            repositoryNode = repositoryEditorInput.getRepositoryNode();
        }
        // if (repositoryNode != null) {
        // if (repFactory.getStatus(repositoryEditorInput.getItem()) == ERepositoryStatus.DELETED) {
        // RepositoryManager.refreshDeletedNode(null);
        // } else {
        // RepositoryManager.refreshSavedNode(repositoryNode);
        // }
        // }
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        if (editorInput instanceof RepositoryEditorInput) {
            RepositoryEditorInput processEditorInput = (RepositoryEditorInput) editorInput;
            processEditorInput.getItem().getProperty().eAdapters().add(dirtyListener);
        }
    }

    private boolean propertyIsDirty;

    private AdapterImpl dirtyListener = new AdapterImpl() {

        @Override
        public void notifyChanged(Notification notification) {
            if (notification.getEventType() != Notification.REMOVING_ADAPTER
                    && notification.getEventType() != Notification.RESOLVE) {
                propertyIsDirty = true;
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        }
    };

    private Boolean lastVersion;

    public void refresh() {
        DiagramEditPart diagramEditPart = this.getDiagramEditPart();
        List list = diagramEditPart.getChildren();
        for (Object o : list) {
            if (o instanceof BusinessItemShapeEditPart) {
                BusinessItemShapeEditPart element = (BusinessItemShapeEditPart) o;
                element.refresh();

            }
        }
    }

    public RepositoryEditorInput getDiagramEditorInput() {
        return this.repositoryEditorInput;

    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        super.selectionChanged(part, selection);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        JobSettingsView view = (JobSettingsView) page.findView(JobSettingsView.ID);

        getDiagramEditorInput().getItem().getProperty().eAdapters().remove(dirtyListener);

        if (view == null) {
            return;
        }
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        Object firstElement = ((IStructuredSelection) selection).getFirstElement();
        if (!(selection instanceof IStructuredSelection)) {
            return;
        } else if (firstElement instanceof RepositoryNode) {
            return;
        }

        // for Find Assignment
        if (((IStructuredSelection) selection).size() > 0) {
            DiagramEditPart diagramEditPart = getDiagramEditPart();
            if (diagramEditPart instanceof BusinessProcessEditPart) {
                BusinessProcessEditPart processPart = (BusinessProcessEditPart) diagramEditPart;
                for (Object object : processPart.getChildren()) {
                    if (object instanceof BusinessItemShapeEditPart) {
                        BusinessItemShapeEditPart shapEditPart = (BusinessItemShapeEditPart) object;
                        IFigure figure = shapEditPart.getFigure();
                        for (Object child : figure.getChildren()) {
                            if (child instanceof BusinessItemShapeFigure) {
                                BusinessItemShapeFigure shapFigure = (BusinessItemShapeFigure) child;
                                Border border = shapFigure.getBorder();
                                if (border != null) {
                                    shapFigure.setDrawFrame(false);
                                    shapFigure.revalidate();
                                    shapFigure.repaint();
                                }

                            }
                        }

                    }
                }
            }
        }

        // to refresh the jobsettings view
        if (((IStructuredSelection) selection).size() > 1) {
            view.cleanDisplay();
        } else {

            if (firstElement instanceof BusinessItemShapeEditPart || firstElement instanceof BaseBusinessItemRelationShipEditPart
                    || firstElement instanceof NoteEditPart || firstElement instanceof NoteAttachmentEditPart) {

                view.refresh(false, firstElement);
            } else if (firstElement instanceof BusinessProcessEditPart || firstElement instanceof CompartmentEditPart) {

                view.refresh(true, this);
            }

        }
        getDiagramEditorInput().getItem().getProperty().eAdapters().add(dirtyListener);

    }

    public boolean isAlreadyOpened() {
        return foundExistEditor(this.getEditorInput());
    }

    private boolean foundExistEditor(final IEditorInput editorInput) {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {

            IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
            if (page != null) {
                RepositoryEditorInput repEditorInput = (RepositoryEditorInput) editorInput
                        .getAdapter(RepositoryEditorInput.class);
                if (repEditorInput != null) {
                    int num = 0;
                    try {
                        for (IEditorReference ref : page.getEditorReferences()) {
                            Object tmpInput = ref.getEditorInput().getAdapter(RepositoryEditorInput.class);
                            if (tmpInput != null) {
                                if (repEditorInput.equals(tmpInput)) {
                                    num++;
                                }
                            }
                        }
                    } catch (PartInitException e) {
                        ExceptionHandler.process(e);
                    }

                    return num > 1;
                }
            }

        }
        return false;
    }

    public ISelection getSelection() {
        return this.getDiagramGraphicalViewer().getSelection();
    }

    public String getEditorID() {
        return this.ID;
    }

    public boolean isKeepPropertyLocked() {
        return this.keepPropertyLocked;
    }

    public void setKeepPropertyLocked(boolean keepPropertyLocked) {
        this.keepPropertyLocked = keepPropertyLocked;
    }

    @Override
    protected void disposeDocumentProvider() {
        if (!keepPropertyLocked) {
            super.disposeDocumentProvider();
        }
    }

    @Override
    public void setFocus() {
        getSelection();

        super.setFocus();
    }

    @Override
    public boolean isLastVersion(Item item) {
        if (lastVersion != null) {
            return lastVersion;
        }
        if (item.getProperty() != null) {
            try {
                List<IRepositoryViewObject> allVersion = null;
                ItemState state = item.getState();
                ERepositoryObjectType type = ERepositoryObjectType.BUSINESS_PROCESS;

                if (type != null && state != null && state.getPath() != null) {
                    allVersion = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                            .getAllVersion(item.getProperty().getId(), state.getPath(), type);
                } else {
                    allVersion = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                            .getAllVersion(item.getProperty().getId());
                }
                if (allVersion == null || allVersion.isEmpty()) {
                    return false;
                }
                String lastVersion = VersionUtils.DEFAULT_VERSION;

                for (IRepositoryViewObject object : allVersion) {
                    if (VersionUtils.compareTo(object.getVersion(), lastVersion) > 0) {
                        lastVersion = object.getVersion();
                    }
                }
                if (VersionUtils.compareTo(item.getProperty().getVersion(), lastVersion) == 0) {
                    return true;
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.ILastVersionChecker#setLastVersion(java.lang.Boolean)
     */
    @Override
    public void setLastVersion(Boolean lastVersion) {
        this.lastVersion = lastVersion;
    }

    @Override
    public void doSaveAs() {
        SaveAsBusinessModelAction saveAsAction = new SaveAsBusinessModelAction(this);
        saveAsAction.run();
    }

    @Override
    public boolean isSaveAsAllowed() {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            return false;
        }
        return true;
    }

    @Override
    public Image getTitleImage() {
        Image image = BusinessDiagramEditorPlugin.getInstance().getBundledImage(BUSINESS_DIAGRAM_EDITOR_IMAGE_PATH);
        if (image == null) {
            image = super.getTitleImage();
        }
        return image;
    }


    @Override
    public String getPartName() {
        String title = null;
        if (repositoryEditorInput != null) {
            title = super.getPartName() + " " + repositoryEditorInput.getItem().getProperty().getVersion();
        }
        return title;
    }
}
