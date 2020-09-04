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
package org.talend.designer.core.ui.editor.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.gmf.draw2d.AnimatableZoomManager;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.hadoop.HadoopConstants;
import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.hadoop.IOozieService;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IHL7Constant;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.ISAPConstant;
import org.talend.core.model.metadata.MetadataSchemaType;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.CDCType;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.HL7FileNode;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MdmConceptType;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPTable;
import org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.core.model.metadata.builder.connection.impl.BRMSConnectionImpl;
import org.talend.core.model.metadata.builder.connection.impl.HL7ConnectionImpl;
import org.talend.core.model.metadata.designerproperties.PropertyConstants.CDCTypeMode;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.process.node.MapperExternalNode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.properties.ValidationRulesConnectionItem;
import org.talend.core.model.repository.DragAndDropManager;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.model.utils.IDragAndDropServiceHandler;
import org.talend.core.model.utils.SQLPatternUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.RepositoryComponentManager;
import org.talend.core.repository.RepositoryComponentSetting;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.model.repositoryObject.QueryRepositoryObject;
import org.talend.core.repository.model.repositoryObject.SAPFunctionRepositoryObject;
import org.talend.core.repository.model.repositoryObject.SAPIDocRepositoryObject;
import org.talend.core.repository.model.repositoryObject.SalesforceModuleRepositoryObject;
import org.talend.core.service.ISAPProviderService;
import org.talend.core.ui.ICDCProviderService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForEBCDICCommand;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForHL7Command;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForSAPBapi;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForSAPCommand;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ExtendedNodeManager;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IExtendedNodeHandler;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.dialog.mergeorder.ChooseJobletDialog;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.TalendScalableFreeformRootEditPart;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand;
import org.talend.designer.core.ui.editor.cmd.ConnectionReconnectCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.QueryGuessCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeQueryCommand;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * Performs a native Drop for the talendEditor. see feature
 *
 * $Id: TalendEditorDropTargetListener.java 1 2006-09-29 17:06:40 +0000 (Ð“Â¦Ð’ï¿½Ð’ÑŸÐ“Â¦Ð’ÑšÐ’ÑŸÐ“Â¤Ð’Ñ”Ð’â€�, 29
 * Ð“Â¤Ð’â„–Ð’ÑœÐ“Â¦Ð’ÑšÐ’â‚¬ 2006) nrousseau $
 *
 */
public class TalendEditorDropTargetListener extends TemplateTransferDropTargetListener {

    private static final String MR_PROPERTY_PREFIX = "MR_PROPERTY:"; //$NON-NLS-1$

    private AbstractTalendEditor editor;

    private boolean fromPalette; // only for palette dnd, feature 6457

    private ConnectionPart selectedConnectionPart = null;

    private boolean sqlChange = false;
    /**
     * TalendEditorDropTargetListener constructor comment.
     *
     * @param editor
     */
    public TalendEditorDropTargetListener(AbstractTalendEditor editor) {
        super(editor.getViewer());
        this.editor = editor;
        setTransfer(LocalSelectionTransfer.getTransfer());
    }

    @Override
    public boolean isEnabled(DropTargetEvent e) {
        Object obj = getSelection().getFirstElement();
        if (obj instanceof RepositoryNode) {
            RepositoryNode sourceNode = (RepositoryNode) obj;
            if (PluginChecker.isCDCPluginLoaded()) {
                ICDCProviderService service = GlobalServiceRegister.getDefault().getService(
                        ICDCProviderService.class);

                if (service != null && (service.isSubscriberTableNode(sourceNode) || service.isSystemSubscriberTable(sourceNode))) {
                    return false;
                }
            }
            IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
            if (hadoopClusterService != null && hadoopClusterService.isHadoopClusterNode(sourceNode)) {
                return false;
            }
            IOozieService oozieService = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IOozieService.class)) {
                oozieService = GlobalServiceRegister.getDefault().getService(IOozieService.class);
            }
            if (oozieService != null && oozieService.isOozieNode(sourceNode)) {
                return false;
            }

            ISAPProviderService sapService = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ISAPProviderService.class)) {
                sapService = GlobalServiceRegister.getDefault().getService(ISAPProviderService.class);
            }
            if (sapService != null && sapService.isSAPNode(sourceNode)) {
                return false;
            }
        }

        return !this.editor.getProcess().isReadOnly();
    }

    @Override
    public void dragEnter(DropTargetEvent event) {

    }

    @Override
    public void dragLeave(DropTargetEvent event) {

    }

    @Override
    public void dragOperationChanged(DropTargetEvent event) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.dnd.TemplateTransferDropTargetListener#handleDragOver()
     */
    @Override
    protected void handleDragOver() {
        super.handleDragOver();
        // when the job that selected is the same one in the current editor, the drag event should be disabled.
        IStructuredSelection selection = getSelection();
        if (selection.size() != 1) {
            getCurrentEvent().detail = DND.DROP_NONE;
            return;
        }

        if (selection.getFirstElement() instanceof RepositoryNode) {
            RepositoryNode sourceNode = (RepositoryNode) selection.getFirstElement();
            if (equalsJobInCurrentEditor(sourceNode)) {
                getCurrentEvent().detail = DND.DROP_NONE;
            }
        }

    }

    @Override
    public void dragOver(DropTargetEvent event) {
        // multi-drag for job,context, sqlPattern.
        IStructuredSelection selection = getSelection();
        if (selection.size() > 1) {
            boolean allowed = true;
            Item temItem = null;
            Iterator iter = selection.iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (next instanceof RepositoryNode) {
                    RepositoryNode sourceNode = (RepositoryNode) next;
                    IRepositoryViewObject object = sourceNode.getObject();
                    if (object != null) {
                        Item selectItem = object.getProperty().getItem();
                        if (temItem == null) {
                            temItem = selectItem;
                            continue;
                        }
                        if (selectItem instanceof ProcessItem && !(temItem instanceof ProcessItem)) {
                            allowed = false;
                        } else if (selectItem instanceof ContextItem && !(temItem instanceof ContextItem)) {
                            allowed = false;
                        } else if (selectItem instanceof SQLPatternItem && temItem instanceof SQLPatternItem) {
                            allowed = false;
                        } else if (selectItem instanceof ResourceItem && temItem instanceof ResourceItem) {
                            allowed = false;
                        } else if (selectItem instanceof RoutineItem && temItem instanceof RoutineItem) {
                            allowed = false;
                        } else if (selectItem instanceof FolderItem && temItem instanceof FolderItem) {
                            allowed = false;
                        }
                    }
                }
            }
            if (!allowed) {
                event.detail = DND.DROP_NONE;
            }
        } else {
            if (selection.getFirstElement() instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) selection.getFirstElement();
                IRepositoryViewObject object = repositoryNode.getObject();

                if (object != null) {
                    Item selectItem = object.getProperty().getItem();

                    if (selectItem instanceof SQLPatternItem) {
                        event.detail = DND.DROP_NONE;
                    } else if (selectItem instanceof ResourceItem) {
                        event.detail = DND.DROP_NONE;
                    } else if (selectItem instanceof RoutineItem) {
                        event.detail = DND.DROP_NONE;
                    } else if (selectItem instanceof FolderItem) {
                        event.detail = DND.DROP_NONE;
                    }
                }
            }

            CreateRequest req = ((CreateRequest) getTargetRequest());
            Object o = null;
            try {
                o = req.getNewObject();
            } catch (Exception e) {
                return;
            }
            if (!(o instanceof Node)) {
                return;
            }

            RootEditPart rep = editor.getViewer().getRootEditPart().getRoot();

            Point viewOriginalPosition = new Point();
            if (rep instanceof ScalableFreeformRootEditPart) {
                ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                Viewport viewport = (Viewport) root.getFigure();
                viewOriginalPosition = viewport.getViewLocation();
            }

            org.eclipse.swt.graphics.Point swtLocation = new org.eclipse.swt.graphics.Point(event.x + viewOriginalPosition.x,
                    event.y + viewOriginalPosition.y);
            Canvas canvas = (Canvas) editor.getViewer().getControl();
            swtLocation = canvas.toControl(swtLocation);
            // System.out.println("topLeft:" + topLeftpoint + " / event:" + swtLocation);
            org.eclipse.draw2d.geometry.Point draw2dPosition = new org.eclipse.draw2d.geometry.Point(swtLocation.x, swtLocation.y);

            double zoom = 1.0;
            if (editor.getViewer().getRootEditPart() instanceof TalendScalableFreeformRootEditPart) {
                ZoomManager zoomManager = ((TalendScalableFreeformRootEditPart) editor.getViewer().getRootEditPart())
                        .getZoomManager();
                zoom = zoomManager.getZoom();
            }
            List<ConnectionPart> connectionParts = CreateComponentOnLinkHelper.getConnectionParts(editor.getProcessPart(),
                    draw2dPosition, (Node) o);

            double minDistance = 1000000000;
            for (ConnectionPart part : connectionParts) {
                if (part.getFigure() instanceof PolylineConnection) {
                    PolylineConnection connection = (PolylineConnection) part.getFigure();
                    double distance = CreateComponentOnLinkHelper.getDistanceOrthogonal(draw2dPosition.x, draw2dPosition.y,
                            connection, zoom);
                    if (distance < minDistance) {
                        selectedConnectionPart = part;
                        minDistance = Math.min(distance, minDistance);
                    }
                }
            }

            if (selectedConnectionPart != null && minDistance < 15) {
                for (Object child : editor.getProcessPart().getChildren()) {
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

    @Override
    protected Request createTargetRequest() {
        fromPalette = false;
        CreateRequest request = new CreateRequest();
        CreationFactory factory = getFactory(LocalSelectionTransfer.getTransfer().getSelection());
        if (factory != null) {
            fromPalette = true;
            request.setFactory(factory);
            return request;
        }
        return super.createTargetRequest();
    }

    @Override
    protected CreationFactory getFactory(Object template) {
        CreationFactory factory = super.getFactory(template);
        if (factory == null) { // for palette dnd, feature 6457
            if (template != null && template instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) template).getFirstElement();
                if (element != null && element instanceof PaletteEditPart) {
                    Object model = ((PaletteEditPart) element).getModel();
                    if (model != null && model instanceof ToolEntry) {
                        return (CreationFactory) ((ToolEntry) model).getToolProperty(CreationTool.PROPERTY_CREATION_FACTORY);
                    }

                } else if (!(getTargetEditPart() instanceof NodeContainerPart) && editor.getParent() != null) {
                    for (IExtendedNodeHandler hander : ExtendedNodeManager.getExtendedNodeHandler()) {
                        factory = hander.getCreationFactory(element, editor.getParent().getEditorId());
                        if (factory != null) {
                            return factory;
                        }
                    }
                }
            }
        }

        return factory;
    }

    // private void checkRequiredModules() {
    // Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
    // if (newObject instanceof Node) {
    // IComponent component = ((Node) newObject).getComponent();
    // Shell shell = Display.getCurrent().getActiveShell();
    // ModulesInstallerUtil.installModules(new Shell(shell), component);
    // }
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.dnd.TemplateTransferDropTargetListener#handleDrop()
     */
    @Override
    protected void handleDrop() {
        updateTargetRequest();
        updateTargetEditPart();
        // getCurrentEvent()
        // if drop a node on the job, create new component,
        // else just update the schema or something of the target component.

        // if (getTargetEditPart() instanceof NodeContainerPart) {

        // IEditorPart iep = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        // IEditorInput iei = iep.getEditorInput();
        // iei
        // EditPart ep = getTargetEditPart();
        // checkRequiredModules();
        if (fromPalette && getTargetRequest() instanceof CreateRequest) {
            if (selectedConnectionPart != null) {
                CreateRequest req = ((CreateRequest) getTargetRequest());
                Object o = req.getNewObject();
                Point location = req.getLocation();
                if (o instanceof Node) {
                    createComponentOnLink((Node) o, location);
                }
                // checkRequiredModules();
                return;
            } else if (getTargetEditPart() instanceof ProcessPart) {
                // for palette dnd, feature 6457
                Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                if (newObject != null) {
                    Command command = getCommand();
                    if (command != null) {
                        execCommandStack(command);
                    }
                }
                // checkRequiredModules();
                return;
            } else if (getTargetEditPart() instanceof SubjobContainerPart) {
                // TDI-24985:for palette dnd.
                Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                if (newObject != null) {
                    Command command = getCommand();
                    if (command != null) {
                        execCommandStack(command);
                    }
                }
                return;
            } else if (getTargetEditPart() instanceof JobletContainerPart) {
                JobletContainerPart jobletPart = (JobletContainerPart) getTargetEditPart();
                if (isLock(jobletPart)) {
                    ChooseJobletDialog dialog = new ChooseJobletDialog(DisplayUtils.getDefaultShell(false), getDropLocation());
                    if (dialog.open() == dialog.OK) {
                        EditPart part = getTargetEditPart();
                        if (dialog.addToJoblet()) {

                            AbstractMultiPageTalendEditor openEditor = getJobletPart((JobletContainerPart) part);
                            part = openEditor.getDesignerEditor().getProcessPart();
                            // editor = openEditor.getTalendEditor();
                            setTargetEditPart(part);
                            Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                            if (newObject != null) {
                                Command command = getCommand();
                                if (command != null) {
                                    CommandStack commandStack = (CommandStack) openEditor.getAdapter(CommandStack.class);
                                    if (commandStack != null) {
                                        commandStack.execute(command);
                                    } else {
                                        command.execute();
                                    }
                                }
                            }
                            // checkRequiredModules();
                            return;
                        } else {
                            part = getParentPart(part);
                            setTargetEditPart(part);
                            Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                            if (newObject != null) {
                                Command command = getCommand();
                                if (command != null) {
                                    execCommandStack(command);
                                }
                            }
                            // checkRequiredModules();
                            return;
                        }

                    }
                }
            }

        }

        List<Object> sources = getSelectSource();
        if (containsContextSource(sources)) {
            createContext(sources);
        } else {
            if (!(getTargetEditPart() instanceof NodeContainerPart)) {

                try {
                    createNewComponent(getCurrentEvent());
                } catch (OperationCanceledException e) {
                    return;
                }

            } else {
                if (containsSQLPatternSource(sources)) {
                    createSQLPattern(sources);
                } else {
                    Object obj = getSelection().getFirstElement();
                    createSchema(obj, getTargetEditPart());
                    createQuery(obj, getTargetEditPart());
                    createProperty(obj, getTargetEditPart());
                    createChildJob(obj, getTargetEditPart());
                    createValidationRule(obj, getTargetEditPart());
                    if (obj instanceof IRepositoryNode) {
                        propaHadoopCfgChanges((IRepositoryNode) obj);
                    }
                    for (IExtendedNodeHandler hander : ExtendedNodeManager.getExtendedNodeHandler()) {
                        boolean updated = hander.updateComponent(obj, getTargetEditPart(), editor.getCommandStack());
                        if (updated) {
                            break;
                        }
                    }
                }
            }

        }
        // in case after drag/drop the editor is dirty but can not get focus
        if (editor.isDirty()) {
            editor.setFocus();
        }
        this.eraseTargetFeedback();
    }

    /**
     * DOC ycbai Comment method "propaHadoopCfgChanges".
     *
     * <P>
     * Propagate the changes from hadoop cluster to M/R process when drag&drop hadoop subnode from repository view to
     * M/R process.
     * </P>
     *
     * @param repositoryNode
     */
    private void propaHadoopCfgChanges(IRepositoryNode repositoryNode) {
        if (repositoryNode == null || repositoryNode.getObject() == null) {
            return;
        }

        IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
        if (hadoopClusterService == null || !hadoopClusterService.isHadoopSubnode(repositoryNode)) {
            return;
        }

        IProcess process = editor.getProcess();
        if (!ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(process.getComponentsType())
                && !ComponentCategory.CATEGORY_4_SPARK.getName().equals(process.getComponentsType())
                && !ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName().equals(process.getComponentsType())) {
            return;
        }
        if ((process instanceof IProcess2) && (((IProcess2) process).getProperty().getItem() instanceof JobletProcessItem)) {
            return;
        }

        Item subItem = repositoryNode.getObject().getProperty().getItem();
        String propertyParamName = MR_PROPERTY_PREFIX + EParameterName.PROPERTY_TYPE.getName();
        String propertyRepTypeParamName = MR_PROPERTY_PREFIX + EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
        IElementParameter propertyParam = process.getElementParameter(propertyParamName);
        if (propertyParam == null) {
            return;
        }
        String repositoryValue = propertyParam.getRepositoryValue();
        if (repositoryValue == null) {
            return;
        }
        String[] supportedRepositoryTypes = repositoryValue.split("\\|"); //$NON-NLS-1$
        String repositoryType = hadoopClusterService.getRepositoryTypeOfHadoopSubItem(subItem);
        if (!ArrayUtils.contains(supportedRepositoryTypes, repositoryType)) {
            return;
        }

        Item hadoopClusterItem = hadoopClusterService.getHadoopClusterBySubitemId(new Project(ProjectManager.getInstance()
                .getProject(subItem)), subItem.getProperty().getId());
        String hadoopClusterId = hadoopClusterItem.getProperty().getId();
        if (EmfComponent.REPOSITORY.equals(propertyParam.getValue())) {
            // do nothing when select the same hadoop cluster.
            String propertyId = (String) process.getElementParameter(propertyRepTypeParamName).getValue();
            if (hadoopClusterId.equals(propertyId)) {
                return;
            }
        }

        Connection connection = ((ConnectionItem) subItem).getConnection();
        if (hadoopClusterService.hasDiffsFromClusterToProcess(subItem, process)) {
            boolean confirmUpdate = MessageDialog.openConfirm(editor.getSite().getShell(),
                    Messages.getString("TalendEditorDropTargetListener.updateHadoopCfgDialog.title"), //$NON-NLS-1$
                    Messages.getString("TalendEditorDropTargetListener.updateHadoopCfgDialog.msg")); //$NON-NLS-1$
            if (confirmUpdate) {
                // Update spark mode to YARN_CLIENT if repository
                if (ComponentCategory.CATEGORY_4_SPARK.getName().equals(process.getComponentsType())
                        || ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName().equals(process.getComponentsType())) {
                    IElementParameter sparkLocalParam = process.getElementParameter(HadoopConstants.SPARK_LOCAL_MODE);
                    IElementParameter sparkParam = process.getElementParameter(HadoopConstants.SPARK_MODE);
                    if (sparkLocalParam != null && (Boolean) (sparkLocalParam.getValue())) {
                        sparkLocalParam.setValue(false);
                    }

                    if (sparkParam != null && !HadoopConstants.SPARK_MODE_YARN_CLIENT.equals(sparkParam.getValue())) {
                        sparkParam.setValue(HadoopConstants.SPARK_MODE_YARN_CLIENT);
                    }

                }
                propertyParam.setValue(EmfComponent.REPOSITORY);
                ChangeValuesFromRepository command = new ChangeValuesFromRepository(process, connection,
                        propertyRepTypeParamName, subItem.getProperty().getId());
                execCommandStack(command);
            }
        }
    }

    private void createSQLPattern(List<Object> sourceList) {
        if (sourceList.size() == 0) {
            return;
        }
        NodeContainerPart nodePart = (NodeContainerPart) getTargetEditPart();
        Object model = nodePart.getModel();
        if (model instanceof NodeContainer) {
            Node node = ((NodeContainer) model).getNode();
            IElementParameter sqlPatternValue = node.getElementParameter(EParameterName.SQLPATTERN_VALUE.getName());
            if (sqlPatternValue != null) {
                boolean created = false;
                for (Object source : sourceList) {
                    if (source instanceof RepositoryNode) {
                        RepositoryNode sourceNode = (RepositoryNode) source;
                        Item item = sourceNode.getObject().getProperty().getItem();
                        if (item instanceof SQLPatternItem) {
                            SQLPatternItem pattern = (SQLPatternItem) item;
                            Property property = pattern.getProperty();
                            String propertyId = property.getId();
                            String propertyLabel = property.getLabel();
                            List<Map> values = (List<Map>) sqlPatternValue.getValue();
                            Map<String, String> patternMap = new HashMap<String, String>();
                            boolean contains = false;
                            for (Map map : values) {
                                String compoundId = (String) map.get(SQLPatternUtils.SQLPATTERNLIST);
                                String id = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[0];
                                String name = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[1];
                                if (id.equals(propertyId) && name.equals(propertyLabel)) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) {
                                patternMap.put(SQLPatternUtils.SQLPATTERNLIST, propertyId + SQLPatternUtils.ID_SEPARATOR
                                        + propertyLabel);
                                values.add(patternMap);
                                sqlPatternValue.setValue(values);
                                created = true;
                            }
                        }
                    }
                }
                if (created) {
                    RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurComponentSettingsView();
                }
            }
        }
    }

    private void createContext(List<Object> sourceList) {
        if (sourceList.size() == 0) {
            return;
        }
        boolean created = false;
        for (Object source : sourceList) {
            if (source instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) source;
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof ContextItem) {
                    ContextItem contextItem = (ContextItem) item;
                    EList context = contextItem.getContext();
                    Set<String> contextSet = new HashSet<String>();
                    Iterator iterator = context.iterator();
                    while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof ContextTypeImpl) {
                            EList contextParameters = ((ContextTypeImpl) obj).getContextParameter();
                            Iterator contextParas = contextParameters.iterator();
                            while (contextParas.hasNext()) {
                                ContextParameterTypeImpl contextParameterType = (ContextParameterTypeImpl) contextParas.next();
                                String name = contextParameterType.getName();
                                contextSet.add(name);
                            }
                        }
                    }
                    IEditorInput editorInput = editor.getEditorInput();
                    if (editorInput instanceof JobEditorInput) {
                        JobEditorInput jobInput = (JobEditorInput) editorInput;
                        IProcess2 process = jobInput.getLoadedProcess();
                        IContextManager contextManager = process.getContextManager();
                        List<IContext> listContext = contextManager.getListContext();
                        // context group will reflect absolutely if no context variable in contextViewer
                        // if (!ConnectionContextHelper.containsVariable(contextManager)) {
                        // for bug 15608
                        // ConnectionContextHelper.addContextVarForJob(process, contextItem, contextManager);
                        // ConnectionContextHelper.checkAndAddContextsVarDND(contextItem, contextManager);
                        Map<String, String> renamedMap = ContextUtils.getContextParamterRenamedMap(process.getProperty().getItem());
                        Set<String> addedVars = ConnectionContextHelper.checkAndAddContextVariables(contextItem, contextSet,
                                process.getContextManager(), false, renamedMap);
                        if (addedVars != null && !addedVars.isEmpty()
                                && !ConnectionContextHelper.isAddContextVar(contextItem, contextManager, contextSet)) {
                            // show
                            Map<String, Set<String>> addedVarsMap = new HashMap<String, Set<String>>();
                            addedVarsMap.put(item.getProperty().getLabel(), contextSet);
                            if (ConnectionContextHelper.showContextdialog(process, contextItem, process.getContextManager(),
                                    addedVarsMap, contextSet)) {
                                created = true;
                            }
                        }
                        // } else {
                        // Set<String> addedContext = ConnectionContextHelper.checkAndAddContextVariables(contextItem,
                        // contextSet, contextManager, false);
                        // if (addedContext != null && addedContext.size() > 0) {
                        // ConnectionContextHelper.addContextVarForJob(process, contextItem, contextSet);
                        // created = true;
                        // }
                        // }
                    }
                }
            }
        }
        if (created) {
            RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
        }
    }

    private List<Object> getSelectSource() {
        List<Object> sourceList = new ArrayList<Object>();
        Iterator iterator = getSelection().iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            sourceList.add(obj);
        }
        return sourceList;
    }

    private boolean containsContextSource(List<Object> source) {
        if (source.size() == 0) {
            return false;
        }
        for (Object object : source) {
            if (object instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) object;
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof ContextItem) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsSQLPatternSource(List<Object> source) {
        if (source.size() == 0) {
            return false;
        }
        for (Object object : source) {
            if (object instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) object;
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof SQLPatternItem) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOC ycbai Comment method "createValidationRule".
     *
     * @param dragModel
     * @param targetEditPart
     */
    private void createValidationRule(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        if (dragNode.getObject().getProperty().getItem() instanceof ValidationRulesConnectionItem) {
            Node node = (Node) nodePart.getNodePart().getModel();
            List<IRepositoryViewObject> valRuleObjs = ValidationRulesUtil.getRelatedValidationRuleObjs(node);
            IRepositoryViewObject valRuleObj = dragNode.getObject();
            String schemaType = (String) node.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
            if (EmfComponent.BUILTIN.equals(schemaType)
                    || !ValidationRulesUtil.isCurrentValRuleObjInList(valRuleObjs, valRuleObj)) {
                MessageDialog.openWarning(editor.getSite().getShell(),
                        Messages.getString("SchemaTypeController.validationrule.title.warn"), //$NON-NLS-1$
                        Messages.getString("SchemaTypeController.validationrule.cannotApplyValMsg")); //$NON-NLS-1$
                return;
            }
            CompoundCommand cc = new CompoundCommand();
            cc.add(new PropertyChangeCommand(node, EParameterName.VALIDATION_RULES.getName(), true));
            cc.add(new ChangeValuesFromRepository(node, null, "VALIDATION_RULE_TYPE:VALIDATION_RULE_TYPE", //$NON-NLS-1$
                    EmfComponent.REPOSITORY));
            cc.add(new PropertyChangeCommand(node, EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName(), valRuleObj
                    .getProperty().getId()));
            execCommandStack(cc);
        }
    }

    /**
     * DOC qwei Comment method "createChildJob".
     */
    private void createChildJob(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;

        if (dragNode.getObject().getProperty().getItem() instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) dragNode.getObject().getProperty().getItem();
            Command command = getChangeChildProcessCommand((Node) nodePart.getNodePart().getModel(), processItem);
            if (command != null) {
                execCommandStack(command);
            }
        }
    }

    /**
     * DOC bqian Comment method "createSchema".
     *
     * @param firstElement
     * @param targetEditPart
     */
    private void createSchema(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        Node node = (Node) nodePart.getNodePart().getModel();
        IRepositoryViewObject object = dragNode.getObject();

        if (dragNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            CompoundCommand cc = new CompoundCommand();
            boolean isValRulesLost = false;
            if (dragNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE) {
                IRepositoryViewObject currentValRuleObj = ValidationRulesUtil.getCurrentValidationRuleObjs(node);
                if (currentValRuleObj != null) {
                    String schema = object.getProperty().getId() + " - " + object.getLabel(); //$NON-NLS-1$
                    List<IRepositoryViewObject> valRuleObjs = ValidationRulesUtil.getRelatedValidationRuleObjs(schema);
                    if (!ValidationRulesUtil.isCurrentValRuleObjInList(valRuleObjs, currentValRuleObj)) {
                        if (!MessageDialog.openConfirm(editor.getSite().getShell(),
                                Messages.getString("SchemaTypeController.validationrule.title.confirm"), //$NON-NLS-1$
                                Messages.getString("SchemaTypeController.validationrule.selectMetadataMsg"))) { //$NON-NLS-1$
                            isValRulesLost = false;
                            return;
                        } else {
                            isValRulesLost = true;
                        }
                    }
                }
            }
            ConnectionItem connectionItem = (ConnectionItem) dragNode.getObject().getProperty().getItem();
            getChangeMetadataCommand(cc, dragNode, node, connectionItem);

            if (isValRulesLost) {
                ValidationRulesUtil.appendRemoveValidationRuleCommands(cc, node);
            }
            if (cc.getCommands().size() > 0) {
                execCommandStack(cc);
            }
        }
    }

    private void createQuery(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        if (dragNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) dragNode.getObject().getProperty().getItem();
            Command command = getChangeQueryCommand(dragNode, (Node) nodePart.getNodePart().getModel(), connectionItem);
            if (command != null) {
                execCommandStack(command);
            }
        }
    }

    private void createProperty(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        if (dragNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) dragNode.getObject().getProperty().getItem();
            Command command = getChangePropertyCommand(dragNode, (Node) nodePart.getNodePart().getModel(), connectionItem);
            if (command != null) {
                execCommandStack(command);
            }
        }
    }

    private boolean equalsJobInCurrentEditor(RepositoryNode sourceNode) {
        Item item = sourceNode.getObject().getProperty().getItem();
        if (item instanceof ProcessItem) {
            return editor.getProcess().getProperty().getItem().equals(item);
        }
        return false;
    }

    private IStructuredSelection getSelection() {
        LocalSelectionTransfer transfer = (LocalSelectionTransfer) getTransfer();
        IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
        return selection;
    }

    /**
     * Used to store data temporarily. <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (Ð“Â¦Ð’ï¿½Ð’ÑŸÐ“Â¦Ð’ÑšÐ’ÑŸÐ“Â¤Ð’Ñ”Ð’â€�, 29
     * Ð“Â¤Ð’â„–Ð’ÑœÐ“Â¦Ð’ÑšÐ’â‚¬ 2006) nrousseau $
     *
     */
    class TempStore {

        // This is the element that user select in the repositoryView.
        RepositoryNode seletetedNode = null;

        IComponentName componentName = null;

        IComponent component;

    }

    public void createNewComponent(DropTargetEvent event1) {
        boolean quickCreateInput = event1.detail == DND.DROP_LINK;
        boolean quickCreateOutput = event1.detail == DND.DROP_COPY;
        Iterator iterator = getSelection().iterator();
        List<TempStore> list = new ArrayList<TempStore>();
        List<IComponent> components = new ArrayList<IComponent>();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) obj;
                if (equalsJobInCurrentEditor(sourceNode)) {
                    continue;
                }

                Item item = sourceNode.getObject().getProperty().getItem();

                if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                    ITestContainerProviderService testContainerService = GlobalServiceRegister
                            .getDefault().getService(ITestContainerProviderService.class);
                    if (testContainerService != null && testContainerService.isTestContainerItem(item)) {
                        continue;
                    }
                }

                ERepositoryObjectType type = sourceNode.getObjectType();
                if (!(item instanceof ConnectionItem) && !(item instanceof ProcessItem) && !(item instanceof JobletProcessItem)
                        && !(item instanceof RulesItem) && !(item instanceof LinkRulesItem)) { // hywang modified for
                    // feature 6484,for
                    // RulesItem
                    return;
                }

                {
                    /**
                     * Remove this limitation for feature: TDI-32362, I think it should be ok, don't know why adding
                     * this limitation to forbid to d&d salseforce connection, I keep those code in case this behavior
                     * will cause some bugs when d&d salseforce connection, then maybe need to review
                     */
                    // if (item instanceof SalesforceSchemaConnectionItem && type ==
                    // ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
                    // return;
                    // }
                }

                TempStore store = new TempStore();

                store.seletetedNode = sourceNode;
                getAppropriateComponent(item, quickCreateInput, quickCreateOutput, store, type);
                if (store.component != null) {
                    list.add(store);
                } else {
                    MessageDialog.openInformation(editor.getEditorSite().getShell(),
                            Messages.getString("TalendEditorDropTargetListener.dngsupportdialog.title"), //$NON-NLS-1$
                            Messages.getString("TalendEditorDropTargetListener.dngsupportdialog.content")); //$NON-NLS-1$
                }
            }

            org.eclipse.swt.graphics.Point swtLocation = new org.eclipse.swt.graphics.Point(event1.x, event1.y);
            Canvas canvas = (Canvas) editor.getViewer().getControl();

            /*
             * translate to Canvas coordinate
             */
            swtLocation = canvas.toControl(swtLocation);
            org.eclipse.swt.graphics.Point size = canvas.getSize();
            /*
             * translate to Viewport coordinate with zoom
             */
            org.eclipse.draw2d.geometry.Point draw2dPosition = new org.eclipse.draw2d.geometry.Point(swtLocation.x, swtLocation.y);

            /*
             * calcule the view port position. Take into acounte the scroll position
             */
            ProcessPart part = (ProcessPart) editor.getViewer().getRootEditPart().getRoot().getChildren().get(0);

            IFigure targetFigure = part.getFigure();
            translateAbsolateToRelative(targetFigure, draw2dPosition);
            String lastUniqname = ""; //$NON-NLS-1$

            // creates every node
            for (TempStore store : list) {
                RepositoryNode selectedNode = store.seletetedNode;
                IComponent element = store.component;
                if (!components.contains(element)) {
                    components.add(element);
                }
                Node node = new Node(element);
                // for bug4564(metadata label format)
                // IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
                // if (preferenceStore.getBoolean(TalendDesignerPrefConstants.USE_REPOSITORY_NAME)) {
                // node.setPropertyValue(EParameterName.LABEL.getName(), selectedNode.getObject().getLabel());
                // }
                IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
                if (preferenceStore.getBoolean(TalendDesignerPrefConstants.USE_REPOSITORY_NAME)) {
                    String LabelValue = null;
                    RepositoryNode repositoryNode = null;
                    repositoryNode = (RepositoryNode) getSelection().getFirstElement();
                    // dnd a table
                    IElementParameter dbTableParam = node.getElementParameterFromField(EParameterFieldType.DBTABLE);
                    boolean hasDbTableField = dbTableParam != null;

                    if (repositoryNode.getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE
                            && repositoryNode.getObject() != null
                            && repositoryNode.getObject().getProperty().getItem() instanceof DatabaseConnectionItem
                            && hasDbTableField) {
                        LabelValue = DesignerUtilities.getParameterVar(dbTableParam.getName());
                    } else if (repositoryNode.getObjectType() == ERepositoryObjectType.PROCESS
                            || repositoryNode.getObjectType() == ERepositoryObjectType.PROCESS_MR
                            || repositoryNode.getObjectType() == ERepositoryObjectType.PROCESS_STORM) { // dnd a job
                        LabelValue = DesignerUtilities.getParameterVar(EParameterName.PROCESS);
                    } else if (repositoryNode.getObjectType() == ERepositoryObjectType.JOBLET
                            || repositoryNode.getObjectType() == ERepositoryObjectType.SPARK_JOBLET
                            || repositoryNode.getObjectType() == ERepositoryObjectType.SPARK_STREAMING_JOBLET) {
                        LabelValue = element.getName();
                    } else if (CorePlugin.getDefault().getDesignerCoreService()
                            .getPreferenceStore(TalendDesignerPrefConstants.DEFAULT_LABEL)
                            .equals(node.getPropertyValue(EParameterName.LABEL.getName()))) {// dnd a default
                        LabelValue = selectedNode.getObject().getLabel();
                    }
                    if (LabelValue != null) {
                        node.setPropertyValue(EParameterName.LABEL.getName(), LabelValue);
                    }
                }
                processSpecificDBTypeIfSameProduct(store.componentName, node);
                NodeContainer nc = ((Process) node.getProcess()).loadNodeContainer(node, false);
                ;

                // create component on link
                boolean executed = false;
                if (getSelection().size() == 1 && getTargetEditPart() instanceof SubjobContainerPart) {
                    executed = createComponentOnLink(node, draw2dPosition);
                }

                if (!executed) {
                    // create the node on the design sheet
                    execCommandStack(new CreateNodeContainerCommand((Process) editor.getProcess(), nc, draw2dPosition));
                }
                // initialize the propertiesView
                CompoundCommand cc = new CompoundCommand();
                createRefreshingPropertiesCommand(cc, selectedNode, node);
                execCommandStack(cc);
                // for (Command command : commands) {
                // }

                propaHadoopCfgChanges(selectedNode);

                draw2dPosition = draw2dPosition.getCopy();
                draw2dPosition.x += TalendEditor.GRID_SIZE;
                draw2dPosition.y += TalendEditor.GRID_SIZE;

                node.checkNode();
                lastUniqname = node.getUniqueName();
            }
            // setselecte(part, lastUniqname);
        }
        // Shell shell = Display.getCurrent().getActiveShell();
        // ModulesInstallerUtil.installModules(new Shell(shell), components);

    }

    private void setselecte(AbstractEditPart part, String lastUniqname) {
        List<NodePart> parts = getAllNodePart(part);
        for (NodePart nodePart : parts) {
            if (nodePart.getModel() instanceof Node) {
                Node node = (Node) nodePart.getModel();
                // for (String uniqname : uniquteNames) {
                if (node.getUniqueName().equals(lastUniqname)) {
                    nodePart.setDrop(true);
                    nodePart.setSelected(EditPart.SELECTED);
                    nodePart.setDrop(false);
                } else {
                    nodePart.setSelected(EditPart.SELECTED_NONE);
                }

                // }
            }
        }
    }

    private List<NodePart> getAllNodePart(AbstractEditPart part) {
        List<NodePart> partList = new ArrayList<NodePart>();
        if (part.getChildren() != null && part.getChildren().size() > 0) {
            for (int i = 0; i < part.getChildren().size(); i++) {
                if (part.getChildren().get(i) instanceof AbstractEditPart) {
                    AbstractEditPart child = (AbstractEditPart) part.getChildren().get(i);
                    if (child.getChildren() != null && child.getChildren().size() > 0) {
                        partList.addAll(getAllNodePart(child));
                    } else {
                        if (child instanceof NodePart) {
                            partList.add((NodePart) child);
                        }
                    }
                }
            }
            // partList.add
        } else {
            if (part instanceof NodePart) {
                partList.add((NodePart) part);
            }
        }
        return partList;
    }

    /**
     * DOC bqian Comment method "createRefreshingPropertiesCommand".
     *
     * @param selectedNode
     * @param node
     */
    private List<Command> createRefreshingPropertiesCommand(CompoundCommand cc, RepositoryNode selectedNode, Node node) {
        // List<Command> list = new ArrayList<Command>();
        if (selectedNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            String propertyId = selectedNode.getObject().getProperty().getId();
            ConnectionItem originalConnectionItem = (ConnectionItem) selectedNode.getObject().getProperty().getItem();
            ConnectionItem connectionItem = originalConnectionItem;
            Connection originalConnection = connectionItem.getConnection();
            Connection connection = connectionItem.getConnection();
            // if component is CDC, replace by the CDC connection.
            if (node.getComponent().getName().contains("CDC")) { // to replace by a flag CDC in component? //$NON-NLS-1$
                if (selectedNode.getObject().getProperty().getItem() instanceof DatabaseConnectionItem) {
                    final DatabaseConnection databaseConnection = (DatabaseConnection) connection;
                    CDCConnection cdcConn = databaseConnection.getCdcConns();
                    if (cdcConn != null) {
                        EList cdcTypes = cdcConn.getCdcTypes();
                        if (cdcTypes != null && !cdcTypes.isEmpty()) {
                            CDCType cdcType = (CDCType) cdcTypes.get(0);
                            // replace property by CDC property.
                            propertyId = cdcType.getLinkDB();
                            try {
                                IRepositoryViewObject object = ProxyRepositoryFactory.getInstance().getLastVersion(propertyId);
                                if (object != null) {
                                    if (object.getProperty().getItem() instanceof DatabaseConnectionItem) {
                                        DatabaseConnectionItem dbConnItem = (DatabaseConnectionItem) object.getProperty()
                                                .getItem();
                                        // replace connection by CDC connection
                                        connectionItem = dbConnItem;
                                        connection = dbConnItem.getConnection();
                                        String originalCdcTypeMode = ((DatabaseConnection) originalConnection).getCdcTypeMode();
                                        if (connection instanceof DatabaseConnection) {
                                            ((DatabaseConnection) connection).setCdcTypeMode(originalCdcTypeMode);
                                        }
                                    }
                                }
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                            // set cdc type mode.
                            IElementParameter logModeParam = node.getElementParameter(EParameterName.CDC_TYPE_MODE.getName());
                            if (logModeParam != null) {
                                String cdcTypeMode = ((DatabaseConnection) originalConnection).getCdcTypeMode();
                                Command logModeCmd = new PropertyChangeCommand(node, EParameterName.CDC_TYPE_MODE.getName(),
                                        CDCTypeMode.LOG_MODE.getName().equals(cdcTypeMode));
                                cc.add(logModeCmd);
                            }
                            // set lib for as400 so far.
                            final String name = "SOURCE_LIB"; //$NON-NLS-1$
                            IElementParameter libParam = node.getElementParameter(name);
                            if (libParam != null) {
                                Object propValue;
                                if (connection.isContextMode()
                                        && ContextParameterUtils.isContainContextParam(databaseConnection.getSID())) {
                                    propValue = databaseConnection.getSID();
                                } else {
                                    propValue = TalendTextUtils.addQuotes(databaseConnection.getSID());
                                }
                                Command libSettingCmd = new PropertyChangeCommand(node, name, propValue);
                                cc.add(libSettingCmd);
                            }
                        }
                    }
                }

            }

            // fore EBCDIC, by cli
            if ((connectionItem instanceof EbcdicConnectionItem) && PluginChecker.isEBCDICPluginLoaded()) {
                // TDI-20505:integration the drag/drop for EBCDIC connection and EBCDIC metadataTable
                IRepositoryViewObject object = selectedNode.getObject();
                if (selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_EBCDIC) {
                    for (MetadataTable table : ConnectionHelper.getTables(originalConnection)) {
                        Command ebcdicCmd = new RepositoryChangeMetadataForEBCDICCommand(node, IEbcdicConstant.TABLE_SCHEMAS,
                                table.getLabel(), ConvertionHelper.convert(table));
                        cc.add(ebcdicCmd);
                    }
                }
                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE) {
                    MetadataTable table = null;
                    if (object instanceof MetadataTableRepositoryObject) {
                        table = ((MetadataTableRepositoryObject) object).getTable();
                    }

                    Command ebcdicCmd = new RepositoryChangeMetadataForEBCDICCommand(node, IEbcdicConstant.TABLE_SCHEMAS,
                            table.getLabel(), ConvertionHelper.convert(table));
                    cc.add(ebcdicCmd);
                }
            }
            // fore HL7, by gcui
            if ((selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_HL7 && PluginChecker.isHL7PluginLoaded())
                    || (selectedNode.getParent() != null
                            && selectedNode.getParent().getObjectType() == ERepositoryObjectType.METADATA_FILE_HL7 && PluginChecker
                                .isHL7PluginLoaded())) {
                if (originalConnection instanceof HL7ConnectionImpl) {
                    if (((HL7ConnectionImpl) originalConnection).getRoot() != null) {
                        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
                        for (Object obj : ((HL7ConnectionImpl) originalConnection).getRoot()) {
                            if (obj instanceof HL7FileNode) {
                                Map<String, String> newMap = new HashMap<String, String>();
                                newMap.put(IHL7Constant.ATTRIBUTE, ((HL7FileNode) obj).getAttribute());
                                newMap.put(IHL7Constant.PATH, ((HL7FileNode) obj).getFilePath());
                                newMap.put(IHL7Constant.COLUMN, ((HL7FileNode) obj).getRelatedColumn());
                                newMap.put(IHL7Constant.ORDER, String.valueOf(((HL7FileNode) obj).getOrder()));
                                newMap.put(IHL7Constant.VALUE, ((HL7FileNode) obj).getDefaultValue());
                                newMap.put(IHL7Constant.REPEATABLE, String.valueOf(((HL7FileNode) obj).isRepeatable()));
                                mapList.add(newMap);
                            }
                        }
                        IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);
                        if (externalNode != null && externalNode.getElementParameter("ROOT") != null) { //$NON-NLS-1$
                            externalNode.getElementParameter("ROOT").setValue(mapList); //$NON-NLS-1$
                        }

                        String fileName = ((HL7ConnectionImpl) originalConnection).getOutputFilePath();
                        if (externalNode != null && externalNode.getElementParameter("FILENAME") != null && fileName != null) { //$NON-NLS-1$
                            externalNode.getElementParameter("FILENAME").setValue(TalendTextUtils.addQuotes(fileName)); //$NON-NLS-1$
                        }

                    }

                }

                // fore HL7, by gcui
                if (selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_HL7 && PluginChecker.isHL7PluginLoaded()) {
                    for (MetadataTable table : ConnectionHelper.getTablesWithOrders(originalConnection)) {
                        Command hl7Cmd = new RepositoryChangeMetadataForHL7Command(node, IHL7Constant.TABLE_SCHEMAS,
                                table.getLabel(), ConvertionHelper.convert(table));
                        cc.add(hl7Cmd);
                    }
                }
            }

            // for brms
            if ((selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_BRMS && PluginChecker.isBRMSPluginLoaded())
                    || (selectedNode.getParent() != null
                            && selectedNode.getParent().getObjectType() == ERepositoryObjectType.METADATA_FILE_BRMS && PluginChecker
                                .isBRMSPluginLoaded())) {
                if (originalConnection instanceof BRMSConnectionImpl) {
                    if (((BRMSConnectionImpl) originalConnection).getRoot() != null) {
                        List<Map<String, String>> rootList = new ArrayList<Map<String, String>>();
                        List<Map<String, String>> loopList = new ArrayList<Map<String, String>>();
                        List<Map<String, String>> groupList = new ArrayList<Map<String, String>>();
                        for (Object obj : ((BRMSConnectionImpl) originalConnection).getRoot()) {
                            if (obj instanceof XMLFileNode) {
                                Map<String, String> rootMap = new HashMap<String, String>();
                                rootMap.put("ATTRIBUTE", ((XMLFileNode) obj).getAttribute()); //$NON-NLS-1$
                                rootMap.put("PATH", ((XMLFileNode) obj).getXMLPath()); //$NON-NLS-1$
                                rootMap.put("COLUMN", ((XMLFileNode) obj).getRelatedColumn()); //$NON-NLS-1$
                                rootMap.put("ORDER", String.valueOf(((XMLFileNode) obj).getOrder())); //$NON-NLS-1$
                                rootMap.put("VALUE", ((XMLFileNode) obj).getDefaultValue()); //$NON-NLS-1$
                                rootList.add(rootMap);

                            }
                        }
                        for (Object obj : ((BRMSConnectionImpl) originalConnection).getLoop()) {
                            if (obj instanceof XMLFileNode) {
                                Map<String, String> loopMap = new HashMap<String, String>();
                                loopMap.put("ATTRIBUTE", ((XMLFileNode) obj).getAttribute()); //$NON-NLS-1$
                                loopMap.put("PATH", ((XMLFileNode) obj).getXMLPath()); //$NON-NLS-1$
                                loopMap.put("COLUMN", ((XMLFileNode) obj).getRelatedColumn()); //$NON-NLS-1$
                                loopMap.put("ORDER", String.valueOf(((XMLFileNode) obj).getOrder())); //$NON-NLS-1$
                                loopMap.put("VALUE", ((XMLFileNode) obj).getDefaultValue()); //$NON-NLS-1$
                                loopList.add(loopMap);
                            }
                        }
                        for (Object obj : ((BRMSConnectionImpl) originalConnection).getGroup()) {
                            if (obj instanceof XMLFileNode) {
                                Map<String, String> groupMap = new HashMap<String, String>();
                                groupMap.put("ATTRIBUTE", ((XMLFileNode) obj).getAttribute()); //$NON-NLS-1$
                                groupMap.put("PATH", ((XMLFileNode) obj).getXMLPath()); //$NON-NLS-1$
                                groupMap.put("COLUMN", ((XMLFileNode) obj).getRelatedColumn()); //$NON-NLS-1$
                                groupMap.put("ORDER", String.valueOf(((XMLFileNode) obj).getOrder())); //$NON-NLS-1$
                                groupMap.put("VALUE", ((XMLFileNode) obj).getDefaultValue()); //$NON-NLS-1$
                                groupList.add(groupMap);
                            }
                        }
                        IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);
                        if (externalNode != null && externalNode.getElementParameter("ROOT") != null) { //$NON-NLS-1$
                            externalNode.getElementParameter("ROOT").setValue(rootList); //$NON-NLS-1$
                        }
                        if (externalNode != null && externalNode.getElementParameter("LOOP") != null) { //$NON-NLS-1$
                            externalNode.getElementParameter("LOOP").setValue(loopList); //$NON-NLS-1$
                        }
                        if (externalNode != null && externalNode.getElementParameter("GROUP") != null) { //$NON-NLS-1$
                            externalNode.getElementParameter("GROUP").setValue(groupList); //$NON-NLS-1$
                        }

                    }

                }
            }

            IElementParameter propertyParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (propertyParam != null) {
                propertyParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).setValue(propertyId);
            }
            IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();

            Map<String, IMetadataTable> repositoryTableMap = new HashMap<String, IMetadataTable>();

            if (!originalConnection.isReadOnly()) {
                for (Object tableObj : ConnectionHelper.getTables(originalConnection)) {
                    org.talend.core.model.metadata.builder.connection.MetadataTable table;

                    table = (org.talend.core.model.metadata.builder.connection.MetadataTable) tableObj;

                    if (factory.getStatus(originalConnectionItem) != ERepositoryStatus.DELETED) {
                        if (!factory.isDeleted(table)) {
                            String value = table.getId();
                            IMetadataTable newTable = ConvertionHelper.convert(table);
                            repositoryTableMap.put(value, newTable);
                        }
                    }
                }
            }
            // DesignerPlugin.getDefault().getProxyRepositoryFactory().getLastVersion("")
            if (propertyParam != null) {
                // command used to set property type
                IMetadataTable metadataTable = null;
                // if (selectedNode.getContentType() == ERepositoryObjectType.METADATA_MDMCONNECTION
                // && selectedNode.getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
                if (selectedNode.getObject() instanceof IMetadataTable) {
                    metadataTable = (IMetadataTable) selectedNode.getObject();
                    if (metadataTable != null && repositoryTableMap.get(metadataTable.getId()) != null) {
                        metadataTable = repositoryTableMap.get(metadataTable.getId());
                    }
                }
                // }
                ChangeValuesFromRepository command1 = new ChangeValuesFromRepository(node, connection, metadataTable,
                        propertyParam.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), propertyId, true); //$NON-NLS-1$
                command1.setMaps(repositoryTableMap);
                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) != ERepositoryObjectType.METADATA_CON_QUERY) {
                    command1.setGuessQuery(true);
                }

                setSAPSpecailValueForCommand(selectedNode, command1);

                // for salesForce module
                SalesforceModuleRepositoryObject sfObject = null;
                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SALESFORCE_MODULE) {
                    sfObject = (SalesforceModuleRepositoryObject) selectedNode.getObject();
                } else if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE) {
                    IRepositoryViewObject object = selectedNode.getParent().getObject();
                    if (object instanceof SalesforceModuleRepositoryObject) {
                        sfObject = (SalesforceModuleRepositoryObject) object;
                    }
                }
                if (sfObject != null) {
                    ModelElement modelElement = sfObject.getModelElement();
                    if (modelElement instanceof SalesforceModuleUnit) {
                        command1.setSalesForceModuleUnit((SalesforceModuleUnit) modelElement);
                    }
                }
                cc.add(command1);
            }

            // command used to set metadata
            getChangeMetadataCommand(cc, selectedNode, node, originalConnectionItem);

            // command used to set query
            if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_QUERY) {
                IElementParameter queryParam = node.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE);

                QueryRepositoryObject object = (QueryRepositoryObject) selectedNode.getObject();
                Query query = object.getQuery();
                if (!sqlChange) {
                    String sql = query.getValue();
                    query.setValue(TalendTextUtils.addStrInQuery(sql));
                    sqlChange = true;
                }
                String value = originalConnectionItem.getProperty().getId() + " - " + query.getLabel(); //$NON-NLS-1$
                if (queryParam != null) {
                    RepositoryChangeQueryCommand command3 = new RepositoryChangeQueryCommand(node, query, queryParam.getName()
                            + ":" + EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(), value); //$NON-NLS-1$
                    cc.add(command3);
                }
            } else {
                if (connection instanceof DatabaseConnection && hasQuery(node)) {
                    DatabaseConnection connection2 = (DatabaseConnection) connection;
                    String schema = connection2.getUiSchema();
                    String dbType = connection2.getDatabaseType();
                    QueryGuessCommand queryGuessCommand = null;
                    if (node.getMetadataList().size() == 0) {
                        queryGuessCommand = new QueryGuessCommand(node, null, schema, dbType);
                    } else {
                        // modified by hyWang for bug 7190
                        queryGuessCommand = new QueryGuessCommand(node, node.getMetadataList().get(0), schema, dbType, connection);
                    }
                    if (queryGuessCommand != null) {
                        cc.add(queryGuessCommand);
                    }
                }
            }
            // context, moved to ChangeValuesFromRepository(bug 5198)
            // ConnectionContextHelper.addContextForNodeParameter(node, connectionItem);
        } else if (selectedNode.getObject().getProperty().getItem() instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) selectedNode.getObject().getProperty().getItem();
            // command used to set job
            String value = null;
            if (ProcessorUtilities.isNeedProjectProcessId(node.getComponent().getName())) {
                org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(processItem.getProperty());
                value = ProcessUtils.getProjectProcessId(project.getTechnicalLabel(), processItem.getProperty().getId());
            } else {
                value = processItem.getProperty().getId();
            }
            PropertyChangeCommand command4 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_PROCESS.getName(), value);
            cc.add(command4);
            PropertyChangeCommand command5 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_CONTEXT.getName(),
                    processItem.getProcess().getDefaultContext());
            cc.add(command5);
        } else if (selectedNode.getObject().getProperty().getItem() instanceof FileItem) { // hywang add for 6484
            if (selectedNode.getObject().getProperty().getItem() instanceof RulesItem) {
                RulesItem rulesItem = (RulesItem) selectedNode.getObject().getProperty().getItem();
                //                String displayName = "Rules:" + rulesItem.getProperty().getLabel(); //$NON-NLS-N$
                IElementParameter propertyParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
                if (propertyParam != null) {
                    propertyParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName())
                            .setValue(EmfComponent.REPOSITORY);
                    // propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())
                    // .setListItemsDisplayName(new String[] { displayName });
                    final String showId = rulesItem.getProperty().getId();
                    PropertyChangeCommand command6 = new PropertyChangeCommand(node,
                            EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), showId);
                    cc.add(command6);
                }
            }
        } else if (selectedNode.getObject().getProperty().getItem() instanceof LinkRulesItem) {
            LinkRulesItem linkItem = (LinkRulesItem) selectedNode.getObject().getProperty().getItem();
            IElementParameter propertyParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (propertyParam != null) {
                propertyParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                // propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())
                // .setListItemsDisplayName(new String[] { displayName });
                final String showId = linkItem.getProperty().getId();
                PropertyChangeCommand command7 = new PropertyChangeCommand(node,
                        EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), showId);
                cc.add(command7);
            }
        }
        return null;
    }

    private void setSAPSpecailValueForCommand(RepositoryNode selectedNode, ChangeValuesFromRepository command) {
        // Noted by Marvin Wang on Jun. 29, 2012. The piece of code is used to judge if the selected node is SAP
        // Table node, if so, set up the table name to command.
        IRepositoryViewObject object = selectedNode.getObject();
        if (object instanceof MetadataColumnRepositoryObject) {
            object = selectedNode.getParent().getParent().getObject();
        }
        SAPFunctionUnit unit = null;
        if (object != null) {
            if (object instanceof MetadataTableRepositoryObject) {
                MetadataTableRepositoryObject tableObject = (MetadataTableRepositoryObject) object;
                MetadataTable abstractMetadataObject = (MetadataTable) tableObject.getAbstractMetadataObject();
                if (abstractMetadataObject != null && abstractMetadataObject.eContainer() instanceof SAPFunctionUnit) {
                    unit = (SAPFunctionUnit) abstractMetadataObject.eContainer();
                    command.setSapFunctionLabel(unit.getLabel());
                    // set table name to functionName/type/tablename;
                    String currentTableName = unit.getLabel() + "/" + abstractMetadataObject.getTableType() + "/" //$NON-NLS-1$ //$NON-NLS-2$
                            + abstractMetadataObject.getLabel();
                    command.setCurrentTableName(currentTableName);
                } else if (abstractMetadataObject instanceof SAPTable) {
                    command.setCurrentTableName(abstractMetadataObject.getLabel());
                }
            } else if (object instanceof SAPFunctionRepositoryObject) {
                SAPFunctionRepositoryObject sapObj = (SAPFunctionRepositoryObject) object;
                unit = sapObj.getModelElement();
                command.setSapFunctionLabel(unit.getLabel());
            } else if (object instanceof SAPIDocRepositoryObject) {
                SAPIDocRepositoryObject sapObj = (SAPIDocRepositoryObject) object;
                command.setSapIDocLabel(sapObj.getLabel());

            }
        }

    }

    public boolean hasQuery(Node node) {
        IElementParameter elementParameter = node.getElementParameterFromField(EParameterFieldType.MEMO_SQL);
        if (elementParameter == null) {
            return false;
        }
        return true;
    }

    /**
     * DOC bqian Comment method "getChangeMetadataCommand".
     *
     * @param selectedNode
     * @param node
     * @param list
     * @param connectionItem
     */
    private void getChangeMetadataCommand(CompoundCommand cc, RepositoryNode selectedNode, Node node,
            ConnectionItem connectionItem) {
        if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_COLUMN) {
            RepositoryNode columnParentNode = selectedNode.getParent().getParent();
            if (columnParentNode != null) {
                selectedNode = columnParentNode;
            }
        }
        if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE
                || selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_FUNCTION
                || selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SALESFORCE_MODULE) {
            String etlSchema = null;
            if (connectionItem.getConnection() instanceof DatabaseConnection) {
                DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
                if (connection instanceof DatabaseConnection) {
                    etlSchema = connection.getUiSchema();
                }
                if (!"".equals(etlSchema)) { //$NON-NLS-1$
                    IElementParameter e = node.getElementParameter("ELT_SCHEMA_NAME"); //$NON-NLS-1$
                    if (e != null) {
                        if (connection.isContextMode() && ContextParameterUtils.isContainContextParam(etlSchema)) {
                            e.setValue(etlSchema);
                        } else {
                            e.setValue(TalendTextUtils.addQuotes(etlSchema));
                        }
                    }
                    // node.getElementParameter("ELT_SCHEMA_NAME").setValue("\"" + etlSchema + "\"");
                }
            }

            IRepositoryViewObject object = selectedNode.getObject();
            MetadataTable table = null;
            if (object instanceof MetadataTableRepositoryObject) {
                table = ((MetadataTableRepositoryObject) object).getTable();
            } else if (object instanceof SalesforceModuleRepositoryObject) {
                table = ((SalesforceModuleRepositoryObject) object).getDefaultTable();
                IRepositoryViewObject obj = null;
                if (selectedNode.getChildren() != null) {
                    for (IRepositoryNode repositoryNode : selectedNode.getChildren()) {
                        obj = repositoryNode.getObject();
                        if (obj instanceof MetadataTableRepositoryObject) {
                            table = ((MetadataTableRepositoryObject) obj).getTable();
                            break;
                        }
                    }
                }
            }
            // for SAP
            if (PluginChecker.isSAPWizardPluginLoaded() && connectionItem instanceof SAPConnectionItem) {
                getSAPCommand(cc, object, connectionItem, table, node);
                return;

            }

            if (table != null) {
                String value = connectionItem.getProperty().getId() + " - " + table.getLabel(); //$NON-NLS-1$
                IElementParameter schemaParam = node.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                if (schemaParam == null) {
                    schemaParam = node.getElementParameterFromField(EParameterFieldType.SCHEMA_REFERENCE);
                }

                IElementParameter queryParam = node.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE);
                if (queryParam != null) {
                    queryParam = queryParam.getChildParameters().get(EParameterName.QUERYSTORE_TYPE.getName());
                    if (queryParam != null) {
                        queryParam.setValue(EmfComponent.BUILTIN);
                    }
                }

                // for EBCDIC (bug 5860)
                // if (PluginChecker.isEBCDICPluginLoaded() && connectionItem instanceof EbcdicConnectionItem) {
                // Command ebcdicCmd = new RepositoryChangeMetadataForEBCDICCommand(node, IEbcdicConstant.TABLE_SCHEMAS,
                // table.getLabel(), ConvertionHelper.convert(table));
                // return ebcdicCmd;
                // }
                if (PluginChecker.isHL7PluginLoaded() && connectionItem instanceof HL7ConnectionItem) {
                    Command hl7Cmd = new RepositoryChangeMetadataForHL7Command(node, IEbcdicConstant.TABLE_SCHEMAS,
                            table.getLabel(), ConvertionHelper.convert(table));
                    cc.add(hl7Cmd);
                    return;
                }
                if (schemaParam == null) {
                    return;
                }
                if (node.isELTComponent()) {
                    node.setPropertyValue(EParameterName.LABEL.getName(), "__ELT_TABLE_NAME__"); //$NON-NLS-1$
                }
                schemaParam.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                RepositoryChangeMetadataCommand command2 = new RepositoryChangeMetadataCommand(node, schemaParam.getName() + ":" //$NON-NLS-1$
                        + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), value, ConvertionHelper.convert(table), null,
                        connectionItem.getConnection());
                cc.add(command2);
                return;
            }
        }
    }

    /**
     * Added by Marvin Wang on July 12, 2012 for getting the command of SAP.
     *
     * @param object
     * @param connectionItem
     * @param table
     * @param node
     * @return
     */
    private void getSAPCommand(CompoundCommand cc, IRepositoryViewObject object, ConnectionItem connectionItem,
            MetadataTable table, Node node) {
        SAPFunctionUnit functionUnit = null;
        if (object instanceof MetadataTableRepositoryObject) {
            IElementParameter schemaParam = null;
            List<IElementParameter> schemaTypeList = node.getElementParametersFromField(EParameterFieldType.SCHEMA_TYPE);
            for (IElementParameter param : schemaTypeList) {
                if (param.getName().equals("SCHEMA")) { //$NON-NLS-1$
                    schemaParam = param;
                    break;
                }
            }
            if (table.eContainer() instanceof SAPFunctionUnit) {
                // function parameter table
                functionUnit = (SAPFunctionUnit) table.eContainer();
                // To judge what the current node is, tSAPOutput or tSAPInput component.
                String sapComponentName = node.getComponent().getName();
                if ("tSAPBapi".equals(sapComponentName)) { //$NON-NLS-1$
                    RepositoryChangeMetadataForSAPBapi command = new RepositoryChangeMetadataForSAPBapi(node,
                            (SAPFunctionUnit) table.eContainer(), ConvertionHelper.convert(table), null);
                    cc.add(command);
                } else {
                    if (schemaParam != null) {
                        // repository id should be connectionid - sapfunctionName/type/tableName
                        String type = table.getTableType() == null ? MetadataSchemaType.OUTPUT.name() : table.getTableType();
                        String value = connectionItem.getProperty().getId()
                                + " - " + functionUnit.getLabel() + "/" + type + "/" + table.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        RepositoryChangeMetadataCommand changeValueCmd = new RepositoryChangeMetadataCommand(node,
                                schemaParam.getName() + ":" //$NON-NLS-1$
                                        + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), value,
                                ConvertionHelper.convert(table), null, connectionItem.getConnection());
                        cc.add(changeValueCmd);
                    }
                    IElementParameter schemasParam = node.getElementParameter(ISAPConstant.TABLE_SCHEMAS);
                    if (schemasParam != null) {
                        Command sapCmd = new RepositoryChangeMetadataForSAPCommand(node, ISAPConstant.TABLE_SCHEMAS,
                                table.getLabel(), ConvertionHelper.convert(table), functionUnit);
                        cc.add(sapCmd);
                    }
                }
            } else {
                // Sap Table : keep repository id as before
                if (schemaParam != null) {
                    String value = connectionItem.getProperty().getId() + " - " + table.getLabel(); //$NON-NLS-1$
                    RepositoryChangeMetadataCommand changeValueCmd = new RepositoryChangeMetadataCommand(node,
                            schemaParam.getName() + ":" //$NON-NLS-1$
                                    + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), value, ConvertionHelper.convert(table),
                            null, connectionItem.getConnection(), table);
                    cc.add(changeValueCmd);
                }

            }
        } else if (object instanceof SAPFunctionRepositoryObject) {
            functionUnit = (SAPFunctionUnit) ((SAPFunctionRepositoryObject) object).getAbstractMetadataObject();
            if (node.getComponent() != null && node.getComponent().getName().equals("tSAPBapi")) { //$NON-NLS-1$
                RepositoryChangeMetadataForSAPBapi command = new RepositoryChangeMetadataForSAPBapi(node, functionUnit, null,
                        null);
                cc.add(command);
            } else {
                for (MetadataTable metadataTable : functionUnit.getTables()) {
                    Command sapCmd = new RepositoryChangeMetadataForSAPCommand(node, ISAPConstant.TABLE_SCHEMAS,
                            metadataTable.getLabel(), ConvertionHelper.convert(metadataTable), functionUnit);
                    cc.add(sapCmd);
                }
            }
        }

    }

    private boolean isMdmOutput(RepositoryNode selectedNode, ConnectionItem connectionItem) {
        boolean isMdmOutput = false;
        if (connectionItem instanceof MDMConnectionItem) {
            MDMConnectionItem mdmItem = (MDMConnectionItem) connectionItem;
            final MDMConnection connection = (MDMConnection) mdmItem.getConnection();
            final EList<Concept> schemas = connection.getSchemas();
            final Object properties = selectedNode.getProperties(EProperties.LABEL);
            Concept concept = null;
            for (int i = 0; i < schemas.size(); i++) {
                final String label = schemas.get(i).getLabel();
                if (label != null && label.equals(properties)) {
                    concept = schemas.get(i);
                    break;
                }
            }
            if (concept != null && MdmConceptType.OUTPUT.equals(concept.getConceptType())) {
                isMdmOutput = true;
            }
        }
        return isMdmOutput;
    }

    private Command getChangeQueryCommand(RepositoryNode selectedNode, Node node, ConnectionItem connectionItem) {
        if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_QUERY) {
            QueryRepositoryObject object = (QueryRepositoryObject) selectedNode.getObject();
            Query query = object.getQuery();
            String value = connectionItem.getProperty().getId() + " - " + query.getLabel(); //$NON-NLS-1$
            IElementParameter queryParam = node.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE);
            if (queryParam != null) {
                queryParam.getChildParameters().get(EParameterName.QUERYSTORE_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                RepositoryChangeQueryCommand command2 = new RepositoryChangeQueryCommand(node, query, queryParam.getName() + ":" //$NON-NLS-1$
                        + EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(), value);
                return command2;
            }

        }
        return null;
    }

    private Command getChangePropertyCommand(RepositoryNode selectedNode, Node node, ConnectionItem connectionItem) {
        ERepositoryObjectType selectedNodeType = selectedNode.getObjectType();

        IComponentName rcSetting = RepositoryComponentManager.getSetting(connectionItem, selectedNodeType);
        if (rcSetting == null) {
            for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
                rcSetting = handler.getCorrespondingComponentName(connectionItem, selectedNodeType);
                if (rcSetting != null) {
                    break;
                }
            }
            if (rcSetting == null) {
                return null;
            }
        }

        List<IComponent> neededComponents = RepositoryComponentManager.filterNeededComponents(connectionItem, selectedNode,
                selectedNodeType);
        for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
            List<IComponent> comList = handler.filterNeededComponents(connectionItem, selectedNode, selectedNodeType);
            if (comList != null) {
                for (IComponent handlerComp : comList) {
                    if (!neededComponents.contains(handlerComp)) {
                        neededComponents.add(handlerComp);
                    }
                }
            }
        }

        List<String> componentNameList = new ArrayList<String>();
        for (IComponent component : neededComponents) {
            componentNameList.add(component.getName());
        }
        String nodeComponentName = node.getComponent().getName();
        if (componentNameList.contains(nodeComponentName)) {
            IElementParameter param = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (param != null) {
                return getPropertyPublicPart(selectedNode, param, node, connectionItem);
            }
        }

        return null;
    }

    private Command getPropertyPublicPart(RepositoryNode selectedNode, IElementParameter param, Node node,
            ConnectionItem connectionItem) {
        param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
        ChangeValuesFromRepository command2 = new ChangeValuesFromRepository(
                node,
                connectionItem.getConnection(),
                param.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), selectedNode.getObject().getProperty().getId()); //$NON-NLS-1$
        if (selectedNode.getObject() instanceof IMetadataTable) {
            IMetadataTable metadataTable = (IMetadataTable) selectedNode.getObject();
            command2.setTable(metadataTable);
        }
        if (selectedNode.getProperties(EProperties.CONTENT_TYPE) != ERepositoryObjectType.METADATA_CON_QUERY) {
            command2.setGuessQuery(true);
        }
        return command2;

    }

    /**
     * DOC qwei Comment method "getChangeChildProcessCommand".
     */
    private Command getChangeChildProcessCommand(Node node, ProcessItem processItem) {
        // command used to set job
        String value = processItem.getProperty().getId();
        if (ProcessorUtilities.isNeedProjectProcessId(node.getComponent().getName())) {
            org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(processItem.getProperty());
            value = ProcessUtils.getProjectProcessId(project.getTechnicalLabel(), processItem.getProperty().getId());
        }
        IElementParameter processParam = node.getElementParameterFromField(EParameterFieldType.PROCESS_TYPE);
        if (processParam != null) {
            PropertyChangeCommand command2 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_PROCESS.getName(), value);
            return command2;
        }
        return null;
    }

    @Override
    public void dropAccept(DropTargetEvent event) {
    }

    private void getAppropriateComponent(Item item, boolean quickCreateInput, boolean quickCreateOutput, TempStore store,
            ERepositoryObjectType type) {
        IComponentName rcSetting = RepositoryComponentManager.getSetting(item, type);

        // IComponentName name = EDatabaseComponentName.getCorrespondingComponentName(item, type);
        // For handler, need check for esb
        if (rcSetting == null) {
            for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
                rcSetting = handler.getCorrespondingComponentName(item, type);
                if (rcSetting != null) {
                    break;
                }
            }
            if (rcSetting == null) {
                return;
            }
        }
        boolean isCurrentProject = true;
        String projectName = null;
        if (store.seletetedNode.getObject() != null) {
            projectName = store.seletetedNode.getObject().getProjectLabel();
            isCurrentProject = projectName.equals(ProjectManager.getInstance().getCurrentProject().getLabel());
        }

        List<IComponent> neededComponents = RepositoryComponentManager.filterNeededComponents(item, store.seletetedNode, type,
                isCurrentProject, projectName);

        for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
            List<IComponent> comList = handler.filterNeededComponents(item, store.seletetedNode, type);
            if (comList != null) {
                for (IComponent handlerComp : comList) {
                    if (!neededComponents.contains(handlerComp) && !handlerComp.isTechnical()) {
                        neededComponents.add(handlerComp);
                    }
                }
            }
        }

        // special handle hbase to support tpigLoad
        String hbaseName = EDatabaseTypeName.HBASE.getDisplayName().toUpperCase();
        if (rcSetting != null && (hbaseName).equals(rcSetting.toString())) {
            IComponentsService service = GlobalServiceRegister.getDefault().getService(
                    IComponentsService.class);
            String componentProductname = null;
            Collection<IComponent> components = service.getComponentsFactory().readComponents();
            for (IComponent component : components) {
                componentProductname = component.getRepositoryType();
                if (componentProductname != null && componentProductname.contains(hbaseName)
                        && !neededComponents.contains(component) && !component.isTechnical()) {
                    neededComponents.add(component);
                }
            }
        }
        // special handle to add tCreateTable component for db , except hive/impala/JDBC
        boolean isHive = EDatabaseTypeName.HIVE.getDisplayName().toUpperCase().equals(rcSetting.toString());
        boolean isImpala = EDatabaseTypeName.IMPALA.getDisplayName().toUpperCase().equals(rcSetting.toString());
        boolean isJDBC = EDatabaseTypeName.GENERAL_JDBC.name().toUpperCase().equals(rcSetting.toString());
        if (item != null && item instanceof DatabaseConnectionItem && !isHive && !isImpala && !isJDBC) {
            IComponent createTableComponent = ComponentsFactoryProvider.getInstance().get("tCreateTable", //$NON-NLS-1$
                    ComponentCategory.CATEGORY_4_DI.getName());
            neededComponents.add(createTableComponent);
        }

        neededComponents = (List<IComponent>) ComponentUtilities.filterVisibleComponents(neededComponents);

        RepositoryComponentSetting settingCopy = new RepositoryComponentSetting();
        settingCopy.setInputComponent(rcSetting.getInputComponentName());
        settingCopy.setOutputComponent(rcSetting.getOutPutComponentName());
        settingCopy.setDefaultComponent(rcSetting.getDefaultComponentName());


        String typeName = null;
        if (item instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) item;
            typeName = connectionItem.getTypeName();
            Connection connection = connectionItem.getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbconn = (DatabaseConnection) connection;
                if (UnifiedComponentUtil.isAdditionalJDBC(dbconn.getProductId())) {
                    typeName = dbconn.getProductId();
                }
            }
        }

        neededComponents = UnifiedComponentUtil.filterUnifiedComponent(settingCopy, neededComponents, typeName);
        // Check if the components in the list neededComponents have the same category that is required by Process.
        IComponent component = chooseOneComponent(extractComponents(neededComponents), settingCopy, quickCreateInput,
                quickCreateOutput, typeName);
        store.component = UnifiedComponentUtil.getEmfComponent(rcSetting, component);
        store.componentName = rcSetting;
    }

    /**
     * Extracts the components which have the same palette type as process. Added by Marvin Wang on Feb 27, 2013.
     *
     * @param neededComponents
     * @return
     */
    protected List<IComponent> extractComponents(List<IComponent> neededComponents) {
        if (neededComponents != null && neededComponents.size() > 0) {
            Iterator<IComponent> componentsIterator = neededComponents.iterator();
            while (componentsIterator.hasNext()) {
                IComponent component = componentsIterator.next();
                String compType = component.getPaletteType();
                if (compType != null && !compType.equals(editor.getProcess().getComponentsType())) {
                    componentsIterator.remove();
                }
            }
        }
        return neededComponents;
    }

    /**
     * Let the user choose which component he would like to create.
     *
     * @param neededComponents
     * @param name
     * @param quickCreateInput
     * @param quickCreateOutput
     */
    private IComponent chooseOneComponent(List<IComponent> neededComponents, IComponentName name, boolean quickCreateInput,
            boolean quickCreateOutput, String typeName) {
        if (neededComponents.isEmpty()) {
            return null;
        }
        if (neededComponents.size() == 1) {
            return neededComponents.get(0);
        }

        IComponent inputComponent = getComponentByName(name.getInputComponentName(), quickCreateInput, neededComponents);
        if (inputComponent != null) {
            return inputComponent;
        }
        IComponent outputComponent = getComponentByName(name.getOutPutComponentName(), quickCreateOutput, neededComponents);
        if (outputComponent != null) {
            return outputComponent;
        }

        ComponentChooseDialog dialog = new ComponentChooseDialog(editor.getSite().getShell(), neededComponents, typeName);
        IComponent defaultComponent = getComponentByName(name.getDefaultComponentName(), true, neededComponents);
        if (defaultComponent != null) {
            dialog.setInitialSelections(new Object[] { defaultComponent });
        } else {
            dialog.setInitialSelections(new Object[] { neededComponents.get(0) });
        }
        if (dialog.open() == IDialogConstants.OK_ID) {
            return dialog.getResultComponent();
        }

        throw new OperationCanceledException(Messages.getString("TalendEditorDropTargetListener.cancelOperation")); //$NON-NLS-1$
    }

    private IComponent getComponentByName(String name, boolean loop, List<IComponent> neededComponents) {
        if (loop) {
            for (IComponent component : neededComponents) {
                if (component.getName().equals(name)) {
                    return component;
                }
            }
        }
        return null;
    }

    private void execCommandStack(Command command) {
        CommandStack cs = editor.getCommandStack();
        if (cs != null) {
            cs.execute(command);
        } else {
            command.execute();
        }
    }

    private boolean createComponentOnLink(Node node, Point originalPoint) {
        boolean executed = false;

        RootEditPart rep = editor.getViewer().getRootEditPart().getRoot();

        Point viewOriginalPosition = new Point();
        if (rep instanceof ScalableFreeformRootEditPart) {
            ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
            Viewport viewport = (Viewport) root.getFigure();
            viewOriginalPosition = viewport.getViewLocation();
        }
        Point point = new Point(originalPoint.x + viewOriginalPosition.x, originalPoint.y + viewOriginalPosition.y);
        point.x = (int) (point.x / AnimatableZoomManager.currentZoom);
        point.y = (int) (point.y / AnimatableZoomManager.currentZoom);
        org.talend.designer.core.ui.editor.connections.Connection targetConnection = null;
        if (selectedConnectionPart != null) {
            targetConnection = (org.talend.designer.core.ui.editor.connections.Connection) selectedConnectionPart.getModel();
        }

        if (targetConnection != null) {
            IProcess2 p = editor.getProcess();
            NodeContainer nodeContainer = ((Process) node.getProcess()).loadNodeContainer(node, false);
            // TDI-21099
            if (p instanceof Process) {
                CreateNodeContainerCommand createCmd = new CreateNodeContainerCommand((Process) p, nodeContainer, point);
                execCommandStack(createCmd);
                // reconnect the node
                Node originalTarget = (Node) targetConnection.getTarget();

                EConnectionType connectionType = EConnectionType.FLOW_MAIN;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                    ICamelDesignerCoreService camelService = GlobalServiceRegister.getDefault()
                            .getService(ICamelDesignerCoreService.class);
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
                // FIXME perhaps, this is not good fix, need check it later
                // bug 21411
                if (PluginChecker.isJobLetPluginLoaded()) {
                    IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null && service.isJobletComponent(targetConnection.getTarget())) {
                        if (targetConnection.getTarget() instanceof Node) {
                            NodeContainer jobletContainer = ((Node) targetConnection.getTarget()).getNodeContainer();
                            // remove the old connection in the container
                            jobletContainer.getInputs().remove(targetConnection);
                        }
                    }
                }
                ConnectionReconnectCommand cmd2 = new ConnectionReconnectCommand(targetConnection);
                cmd2.setNewTarget(node);
                execCommandStack(cmd2);

                List<Object> nodeArgs = CreateComponentOnLinkHelper.getTargetArgs(targetConnection, node);
                ConnectionCreateCommand nodeCmd = new ConnectionCreateCommand(node, targetConnector.getName(), nodeArgs, false);
                nodeCmd.setTarget(originalTarget);
                execCommandStack(nodeCmd);

                // TDI-22977:need judge the current Drag/Drop node's outputs for update the target
                // Setting,such as the target is TMap
                if (node.getOutgoingConnections().size() > 0) {
                    if (node.getExternalNode() instanceof MapperExternalNode) {
                        CreateComponentOnLinkHelper.setupTMap(node);
                    }
                    if (originalTarget.getExternalNode() instanceof MapperExternalNode) {
                        CreateComponentOnLinkHelper.updateTMap(originalTarget, targetConnection, node.getOutgoingConnections()
                                .get(0));
                    }
                    originalTarget.renameData(targetConnection.getName(), node.getOutgoingConnections().get(0).getName());
                }
                if (!ConnectionCreateCommand.isCreatingConnection()) {
                    return true;
                }
                executed = true;
            }
        }

        return executed;
    }

    private void updateConnectionCommand(org.talend.designer.core.ui.editor.connections.Connection connection, Node node,
            CompoundCommand command) {
        if (connection != null || node != null) {
            Node originalTarget = (Node) connection.getTarget();
            INodeConnector targetConnector = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
            for (INodeConnector connector : node.getConnectorsFromType(EConnectionType.FLOW_MAIN)) {
                if (connector.getMaxLinkOutput() != 0) {
                    targetConnector = connector;
                    break;
                }
            }
            ConnectionCreateCommand.setCreatingConnection(true);
            EConnectionType reconnectNewInputStyle = connection.getLineStyle();
            if (ConnectionManager.canConnectToTarget(connection.getSource(), originalTarget, node, connection.getLineStyle(),
                    connection.getName(), targetConnector.getName())) {
                reconnectNewInputStyle = ConnectionManager.getNewConnectionType();
            }
            if (reconnectNewInputStyle.equals(EConnectionType.FLOW_MAIN)) {
                connection.reconnect(connection.getSource(), node, EConnectionType.FLOW_MAIN);
            } else if (reconnectNewInputStyle.equals(EConnectionType.FLOW_MERGE)) {
                connection.reconnect(connection.getSource(), node, EConnectionType.FLOW_MERGE);
            } else if (reconnectNewInputStyle.equals(EConnectionType.FLOW_REF)) {
                connection.reconnect(connection.getSource(), node, EConnectionType.FLOW_REF);
            }
            INodeConnector nodeConnector = node.getConnectorFromName(targetConnector.getName());
            nodeConnector.setCurLinkNbInput(nodeConnector.getCurLinkNbInput() + 1);
            List<Object> nodeArgs = CreateComponentOnLinkHelper.getTargetArgs(connection, node);
            ConnectionCreateCommand nodeCmd = new ConnectionCreateCommand(node, targetConnector.getName(), nodeArgs, false);
            nodeCmd.setTarget(originalTarget);
            INodeConnector originalNodeConnector = originalTarget.getConnectorFromName(connection.getTargetNodeConnector()
                    .getName());
            originalNodeConnector.setCurLinkNbInput(originalNodeConnector.getCurLinkNbInput() - 1);
            command.add(nodeCmd);
        }
    }

    /**
     * see issue 0002439.<br>
     * There are two types of Oracle.
     *
     * @param name
     * @param node
     */
    private void processSpecificDBTypeIfSameProduct(IComponentName name, Node node) {
        // process "Oracle with service name"
        if (name == EDatabaseComponentName.DBORACLESN) {
            IElementParameter p = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
            // set value to "ORACLE_SERVICE_NAME"
            if (p != null) {
                // p.setValue(p.getListItemsValue()[1]);
                node.setPropertyValue("CONNECTION_TYPE", p.getListItemsValue()[1]); //$NON-NLS-1$
            } else {
                node.setPropertyValue("CONNECTION_TYPE", "ORACLE_SERVICE_NAME"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else if (name == EDatabaseComponentName.DBORACLEOCI) {
            IElementParameter p = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
            if (p != null) {
                Object[] obj = p.getListItemsValue();
                if (obj.length >= 2) {
                    p.setValue(p.getListItemsValue()[1]);
                }
            }
        }
    }

    public void translateAbsolateToRelative(IFigure owner, Translatable t) {
        owner.translateToRelative(t);

        Rectangle bounds = owner.getBounds();
        t.performTranslate(-bounds.x, -bounds.y);

    }

    /**
     * Sets the editor.
     *
     * @param editor the editor to set
     */
    public void setEditor(AbstractTalendEditor editor) {
        this.editor = editor;
    }

    public EditPart getParentPart(EditPart part) {
        EditPart parent = part.getParent();
        if (!(parent instanceof ProcessPart)) {
            parent = getParentPart(parent);
        }
        return parent;
    }

    private boolean isLock(JobletContainerPart part) {
        INode jobletNode = ((JobletContainer) part.getModel()).getNode();
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                return service.isLock(jobletNode);
            }
        }

        return false;
    }

    public AbstractMultiPageTalendEditor getJobletPart(JobletContainerPart part) {
        AbstractMultiPageTalendEditor openEditor = null;
        Node jobletNode = ((JobletContainer) part.getModel()).getNode();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                openEditor = (AbstractMultiPageTalendEditor) service.openJobletEditor(jobletNode, page);
            }
        }
        return openEditor;
    }
}

/**
 * A dialog used to choose one component.
 */
class ComponentChooseDialog extends ListDialog {

    /**
     * bqian ComponentChooseDialog constructor comment.
     *
     * @param parentShell
     */
    public ComponentChooseDialog(Shell parentShell, List<IComponent> input, String typeName) {
        super(parentShell);
        setTitle(Messages.getString("TalendEditorDropTargetListener.title")); //$NON-NLS-1$
        setMessage(Messages.getString("TalendEditorDropTargetListener.chooseComponent")); //$NON-NLS-1$
        setInput(input);
        setContentProvider(ArrayContentProvider.getInstance());
        setLabelProvider(new LabelProvider() {

            @Override
            public Image getImage(Object element) {
                IComponent component = (IComponent) element;
                return CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_16);
            }

            @Override
            public String getText(Object element) {
                IComponent component = (IComponent) element;
                if (UnifiedComponentUtil.isDelegateComponent(component) && typeName != null) {
                    return component.getName() + "(" + typeName + ")";
                }
                return component.getDisplayName();
            }

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
             */
            @Override
            public void dispose() {
                super.dispose();
            }
        });

    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        //
        getTableViewer().getTable().showSelection();
        return control;
    }

    public IComponent getResultComponent() {
        return (IComponent) getResult()[0];
    }

}
