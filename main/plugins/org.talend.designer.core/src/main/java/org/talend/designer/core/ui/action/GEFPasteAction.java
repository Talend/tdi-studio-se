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
package org.talend.designer.core.ui.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.PartFactory;
import org.talend.designer.core.ui.editor.cmd.MultiplePasteCommand;
import org.talend.designer.core.ui.editor.cmd.NodesPasteCommand;
import org.talend.designer.core.ui.editor.cmd.NotesPasteCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class GEFPasteAction extends SelectionAction {

    Point cursorLocation = null;

    /**
     * Getter for cursorLocation.
     *
     * @return the cursorLocation
     */
    public org.eclipse.draw2d.geometry.Point getCursorLocation() {
        if (cursorLocation != null) {
            org.eclipse.draw2d.geometry.Point gefPoint = new org.eclipse.draw2d.geometry.Point(cursorLocation);
            return gefPoint;
        }
        return null;
    }

    /**
     * Sets the cursorLocation.
     *
     * @param cursorLocation the cursorLocation to set
     */
    public void setCursorLocation(Point cursorLocation) {
        this.cursorLocation = cursorLocation;
    }

    /**
     * DOC nrousseau NodesPasteAction constructor comment.
     *
     * @param part
     */
    public GEFPasteAction(IWorkbenchPart part) {
        super(part);
        setId(ActionFactory.PASTE.getId());
        setText(Messages.getString("NodesPasteAction.paste")); //$NON-NLS-1$
        ISharedImages sharedImages = part.getSite().getWorkbenchWindow().getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        Object o;
        try {
            o = Clipboard.getDefault().getContents();
        } catch (RuntimeException e) {
            return false;
        }

        org.eclipse.swt.dnd.Clipboard systemClipboard = new org.eclipse.swt.dnd.Clipboard(Display.getCurrent());
        Object systemObject = null;
        if (systemClipboard != null) {
            systemObject = systemClipboard.getContents(TextTransfer.getInstance());
        }

        List obj = getSelectedObjects();
        if (o == null
                && systemObject != null
                && systemObject instanceof String
                && obj != null
                && obj.size() == 1
                && (obj.get(0) instanceof NodeLabelEditPart || obj.get(0) instanceof ConnLabelEditPart || obj.get(0) instanceof NoteEditPart)) {
            return true;
        }

        if (o instanceof String) {
            return true;
        }

        if (!(o instanceof List)) {
            return false;
        }
        List objects = (List) o;
        if (objects.isEmpty()) {
            return false;
        }
        for (Object currentObject : objects) {
            if (!(currentObject instanceof NodePart) && !(currentObject instanceof NoteEditPart)
                    && !(currentObject instanceof SubjobContainerPart)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        Object clipBoardContent;
        try {
            clipBoardContent = Clipboard.getDefault().getContents();
        } catch (RuntimeException e) {
            return;
        }
        AbstractTalendEditor editor = (AbstractTalendEditor) this.getWorkbenchPart();
        org.eclipse.swt.dnd.Clipboard systemClipboard = new org.eclipse.swt.dnd.Clipboard(Display.getCurrent());
        Object systemObject = systemClipboard.getContents(TextTransfer.getInstance());

        if (clipBoardContent instanceof List) {
            List<EditPart> partsList = (List<EditPart>) clipBoardContent;
            if (partsList == null || partsList.isEmpty()) {
                return;
            }

            List<NodePart> nodeParts = new ArrayList<NodePart>();
            List<NoteEditPart> noteParts = new ArrayList<NoteEditPart>();
            List<SubjobContainerPart> subjobParts = new ArrayList<SubjobContainerPart>();

            for (Object o : partsList) {
                if (o instanceof NodePart) {
                    if (!nodeParts.contains(o)) {
                        nodeParts.add((NodePart) o);
                    }
                } else if (o instanceof NoteEditPart) {
                    noteParts.add((NoteEditPart) o);
                } else if (o instanceof SubjobContainerPart) {
                    SubjobContainerPart subjob = (SubjobContainerPart) o;

                    for (Iterator iterator = subjob.getChildren().iterator(); iterator.hasNext();) {
                        NodeContainerPart nodeContainerPart = (NodeContainerPart) iterator.next();
                        // add for bug TDI-20206
                        if (nodeContainerPart instanceof JobletContainerPart) {
                            for (Object obj : nodeContainerPart.getChildren()) {
                                if (obj instanceof NodePart && !nodeParts.contains(obj)) {
                                    nodeParts.add((NodePart) obj);
                                }
                            }
                        }
                        NodePart nodePart = nodeContainerPart.getNodePart();
                        if (nodePart != null) {
                            if (!nodeParts.contains(nodePart)) {
                                nodeParts.add(nodePart);
                            }
                            subjobParts.add(subjob);
                        }
                    }
                }
            }

            Map<JobletContainerPart, List<NodePart>> jobletMap = new HashMap<JobletContainerPart, List<NodePart>>();
            for (NodePart nodePart : nodeParts) {
                boolean isCollapsedNode = false;
                if (editor.getProcess().getGraphicalNodes().contains(nodePart.getModel())) {
                    isCollapsedNode = true;
                }
                if (!isCollapsedNode && nodePart.getParent() instanceof JobletContainerPart) {
                    JobletContainerPart jobletContainer = (JobletContainerPart) nodePart.getParent();
                    List<NodePart> jobletNodeParts = jobletMap.get(jobletContainer);
                    if (jobletNodeParts == null) {
                        jobletNodeParts = new ArrayList<NodePart>();
                        jobletMap.put(jobletContainer, jobletNodeParts);
                    }
                    jobletNodeParts.add(nodePart);
                }
            }
            List<NodePart> expandedJobletNodes = new ArrayList<NodePart>();
            for (JobletContainerPart jobletContainer : jobletMap.keySet()) {
                boolean copyJobletNode = true;
                List<NodePart> list = jobletMap.get(jobletContainer);
                for (Object obj : jobletContainer.getChildren()) {
                    if (obj instanceof NodePart) {
                        if (!list.contains(obj)) {
                            copyJobletNode = false;
                            break;
                        }

                    }
                }
                if (copyJobletNode) {
                    nodeParts.removeAll(list);
                    PartFactory factory = new PartFactory();
                    NodePart createEditPart = (NodePart) factory.createEditPart(jobletContainer,
                            ((NodeContainer) jobletContainer.getModel()).getNode());
                    createEditPart.setParent(jobletContainer);
                    nodeParts.add(createEditPart);
                    expandedJobletNodes.add(createEditPart);
                }
            }

            org.eclipse.draw2d.geometry.Point gefPoint = getCursorLocation();

            // qli comment
            // if the components instanceof JobletInputOutputComponent and current process instanceof not
            // JobletGEFProcess,just create a messageBox and return.
            AbstractProcessProvider findProcessProviderFromPID = AbstractProcessProvider
                    .findProcessProviderFromPID(IComponent.JOBLET_PID);
            if (findProcessProviderFromPID != null) {
                for (NodePart copiedNodePart : nodeParts) {
                    Node copiedNode = (Node) copiedNodePart.getModel();
                    // add for bug TDI-20207.if copy joblet/job to itself,then return.
                    EComponentType componentType = null;
                    String copideNodeId = null;
                    String editorProcessId = null;
                    if (copiedNode.getComponent() != null) {
                        componentType = copiedNode.getComponent().getComponentType();
                        if (copiedNode.getComponent().getProcess() != null) {
                            copideNodeId = copiedNode.getComponent().getProcess().getId();
                        }
                    }
                    if (editor.getProcess() != null) {
                        editorProcessId = editor.getProcess().getId();
                    }
                    for (IElementParameter element : copiedNode.getElementParametersFromField(EParameterFieldType.PROCESS_TYPE)) {
                        for (Map.Entry<String, IElementParameter> entry : element.getChildParameters().entrySet()) {
                            if (("PROCESS_TYPE_PROCESS").equals(entry.getKey())) {
                                if (editorProcessId != null && ProcessUtils.isSameProperty(editorProcessId, String.valueOf(entry.getValue().getValue()), true)) {
                                    return;
                                }
                            }
                        }
                    }
                    if ((EComponentType.JOBLET).equals(componentType)) {
                        if (editorProcessId != null && editorProcessId.equals(copideNodeId)) {
                            return;
                        }
                    }

                    if (findProcessProviderFromPID.isJobletInputOrOutputComponent(copiedNode)) {
                        if (!findProcessProviderFromPID.isExtensionProcess(editor.getProcess())) {
                            MessageBox messagebox = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                                    SWT.ICON_WARNING);
                            messagebox.setText(Messages.getString("GEFPasteAction.textWarning")); //$NON-NLS-1$
                            messagebox.setMessage(Messages.getString("GEFPasteAction.warningMessages")); //$NON-NLS-1$
                            messagebox.open();
                            return;
                        }
                    }

                    if (copiedNode.getJobletNode() != null) {
                        boolean canP = canPasteJobletNode(editor.getProcess(), copiedNode);
                        if (!canP) {
                            return;
                        }
                    }
                }
            }

            if (nodeParts.size() != 0 && noteParts.size() != 0) {
                MultiplePasteCommand mpc = new MultiplePasteCommand(nodeParts, noteParts,
                        (org.talend.designer.core.ui.editor.process.Process) editor.getProcess(), gefPoint);
                mpc.setSelectedSubjobs(subjobParts);
                mpc.setSelectedExpandedJoblet(expandedJobletNodes);
                mpc.setCheckNodeExist(false);
                execute(mpc);
            } else if (nodeParts.size() != 0) {
                NodesPasteCommand cmd = new NodesPasteCommand(nodeParts, editor.getProcess(), gefPoint);
                cmd.setCheckNodeExist(false);
                cmd.setSelectedSubjobs(subjobParts);
                cmd.setSelectedExpandedJoblet(expandedJobletNodes);
                execute(cmd);
            } else if (noteParts.size() != 0) {
                NotesPasteCommand cmd = new NotesPasteCommand(noteParts,
                        (org.talend.designer.core.ui.editor.process.Process) editor.getProcess(), gefPoint, false, null);
                execute(cmd);
            }
            setCursorLocation(null);
        } else if (clipBoardContent instanceof String) {
            List objects = getSelectedObjects();

            if (objects.size() == 1) {
                String content = (String) clipBoardContent;
                if (objects.get(0) instanceof NoteEditPart && ((NoteEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((NoteEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof ConnLabelEditPart
                        && ((ConnLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((ConnLabelEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof NodeLabelEditPart
                        && ((NodeLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    {
                        Text text = (Text) ((NodeLabelEditPart) objects.get(0)).getDirectEditManager().getCellEditor()
                                .getControl();
                        if (text != null) {
                            text.insert(content);
                        }
                    }

                }
            }
        } else if (systemObject != null && systemObject instanceof String) {
            List objects = getSelectedObjects();

            if (objects.size() == 1) {
                String content = (String) systemObject;
                if (objects.get(0) instanceof NoteEditPart && ((NoteEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((NoteEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof ConnLabelEditPart
                        && ((ConnLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((ConnLabelEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof NodeLabelEditPart
                        && ((NodeLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    {
                        Text text = (Text) ((NodeLabelEditPart) objects.get(0)).getDirectEditManager().getCellEditor()
                                .getControl();
                        if (text != null) {
                            text.insert(content);
                        }
                    }

                }
            }
        }
    }

    private boolean canPasteJobletNode(IProcess2 curNodeProcess, Node copiedNode) {
        Item temIten = curNodeProcess.getProperty().getItem();
        if (!(temIten instanceof JobletProcessItem)) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                if (service.isJobletInOutComponent(copiedNode)) {
                    MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Paste Error",
                            "Can not paste joblet component in job!");
                    return false;
                }
            }
        }
        Node jobletNode = (Node) copiedNode.getJobletNode();
        AbstractJobletContainer jnc = (AbstractJobletContainer) jobletNode.getNodeContainer();
        List<NodeContainer> ncs = jnc.getNodeContainers();
        if (ncs != null) {
            for (NodeContainer nc : ncs) {
                if (nc.getNode() == copiedNode) {
                    return true;
                }
            }
        }
        return false;
    }
}
