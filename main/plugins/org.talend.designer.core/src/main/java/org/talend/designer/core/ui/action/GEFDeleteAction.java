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
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.PartFactory;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionLabel;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerFigure;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeEditPolicy;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * Modification of the delete action to enhance the speed of the action from GEF. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class GEFDeleteAction extends DeleteAction {

    List objectsToDelete;

    public GEFDeleteAction(IWorkbenchPart part) {
        super(part);
    }

    @Override
    protected boolean calculateEnabled() {
        objectsToDelete = new ArrayList();
        List objects = getSelectedObjects();
        objectsToDelete.addAll(objects);
        if (objects.isEmpty() || (objects.size() == 1 && objects.get(0) instanceof ProcessPart)) {
            return false;
        }

        if (!(objects.get(0) instanceof EditPart)) {
            return false;
        }
        AbstractTalendEditor editor = (AbstractTalendEditor) this.getWorkbenchPart();
        AbstractProcessProvider pProvider = AbstractProcessProvider.findProcessProviderFromPID(IComponent.JOBLET_PID);
        if (pProvider != null) {
            Map<JobletContainerPart, List<NodePart>> jobletMap = new HashMap<JobletContainerPart, List<NodePart>>();
            boolean nodeInJoblet = false;
            boolean allJunitnode = true;
            boolean hasNode = false;
            int i = 0;
            for (Object o : objects) {
                if (o instanceof NodePart) {
                    hasNode = true;
                    NodePart nodePart = (NodePart) o;
                    Node no = (Node) ((NodePart) o).getModel();
                    if (no.getProcess().isReadOnly()) {
                        return false;
                    }
                    if (no.isReadOnly()) {
                        i++;
                    }
                    if (no.getJunitNode() == null) {
                        allJunitnode = false;
                    }
                    if (!pProvider.canDeleteNode(no)) {
                        return false;
                    }

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
                } else if (o instanceof ConnectionPart) {
                    Connection conn = (Connection) ((ConnectionPart) o).getModel();
                    if (conn.getSource().getProcess().isReadOnly()) {
                        return false;
                    }
                    if (conn.isReadOnly()) {
                        i++;
                    }

                } else if (o instanceof ConnLabelEditPart) {
                    ConnectionLabel connLabel = (ConnectionLabel) ((ConnLabelEditPart) o).getModel();
                    if (connLabel.getConnection().getSource().getProcess().isReadOnly()) {
                        return false;
                    }
                    if (connLabel.getConnection().isReadOnly()) {
                        i++;
                    }
                } else if (o instanceof NoteEditPart) {
                    allJunitnode = false;
                    Note note = (Note) ((NoteEditPart) o).getModel();
                    if (note.isReadOnly()) {
                        i++;
                    }
                } else if (o instanceof SubjobContainerPart) {
                    SubjobContainer subjob = (SubjobContainer) ((SubjobContainerPart) o).getModel();
                    if (subjob.getProcess().isReadOnly()) {
                        return false;
                    }
                    if (subjob.isReadOnly()) {
                        i++;
                        continue;
                    }
                    boolean isAllReadonly = true;
                    boolean subjobAllJunit = true;
                    for (NodeContainer nc : subjob.getNodeContainers()) {
                        Node node = nc.getNode();
                        if (!node.isReadOnly()) {
                            isAllReadonly = false;
                        }
                        if (node.getJunitNode() == null) {
                            subjobAllJunit = false;
                        }
                    }
                    if (isAllReadonly || subjobAllJunit) {
                        i++;
                    }
                }
            }

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
                    objectsToDelete.removeAll(list);
                    PartFactory factory = new PartFactory();
                    NodePart createEditPart = (NodePart) factory.createEditPart(jobletContainer,
                            ((NodeContainer) jobletContainer.getModel()).getNode());
                    createEditPart.setParent(jobletContainer);
                    createEditPart.installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeEditPolicy());
                    objectsToDelete.add(createEditPart);
                } else {
                    nodeInJoblet = true;
                }
            }

            if (((nodeInJoblet || allJunitnode) && hasNode) || i == objects.size()) {
                return false;
            }
        }
        return true;
    }

    public static List filterSameObject(List list) {
        List newList = new ArrayList();
        for (Object object : list) {
            if (!newList.contains(object)) {
                newList.add(object);
            }
        }

        return newList;

    }

    @Override
    public Command createDeleteCommand(List objects) {
        objects = filterSameObject(objectsToDelete);
        if (objects.isEmpty()) {
            return null;
        }
        if (!(objects.get(0) instanceof EditPart)) {
            return null;
        }

        EditPart object = (EditPart) objects.get(0);

        // for TUP-1015
        boolean isConnAttachedJLTriggerComp = false;
        ConnectionPart connectionPart = null;
        if (object instanceof ConnectionPart) {
            connectionPart = (ConnectionPart) object;
        } else if (object instanceof ConnLabelEditPart) {
            connectionPart = (ConnectionPart) object.getParent();
        }
        if (connectionPart != null) {
            Node srcNode = null;
            Object srcModel = connectionPart.getSource().getModel();
            if (srcModel instanceof Node) {
                srcNode = (Node) srcModel;
            }
            Node tarNode = null;
            Object tarModel = connectionPart.getTarget().getModel();
            if (tarModel instanceof Node) {
                tarNode = (Node) tarModel;
            }
            if (srcNode == null || tarNode == null) {
                return null;
            }
            IProcess process = srcNode.getProcess();
            if (AbstractProcessProvider.isExtensionProcessForJoblet(process)) {
                IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                        IJobletProviderService.class);
                if (service != null && (service.isTriggerNode(srcNode) || service.isTriggerNode(tarNode))) {
                    isConnAttachedJLTriggerComp = true;
                }
            }
        }
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        String preKey = TalendDesignerPrefConstants.NOT_SHOW_WARNING_WHEN_DELETE_LINK_WITH_JOBLETTRIGGERLINKCOMPONENT;
        if (isConnAttachedJLTriggerComp && !preferenceStore.getBoolean(preKey)) {
            MessageDialogWithToggle jlTriggerConfirmDialog = new MessageDialogWithToggle(null,
                    Messages.getString("GEFDeleteAction.deleteConnectionDialog.title"), //$NON-NLS-1$
                    null, // accept the default window icon
                    Messages.getString("GEFDeleteAction.deleteConnectionDialog.msg"), MessageDialog.WARNING, new String[] { //$NON-NLS-1$
                    IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0,
                    Messages.getString("GEFDeleteAction.deleteConnectionDialog.toggleMsg"), //$NON-NLS-1$
                    preferenceStore.getDefaultBoolean(preKey));
            jlTriggerConfirmDialog.setPrefStore(preferenceStore);
            jlTriggerConfirmDialog.setPrefKey(preKey);
            if (jlTriggerConfirmDialog.open() != IDialogConstants.YES_ID) {
                return null;
            }
            preferenceStore.setValue(preKey, jlTriggerConfirmDialog.getToggleState());
        }

        List nodeParts = new ArrayList();
        List noteParts = new ArrayList();
        List others = new ArrayList(objects);

        for (Object o : objects) {
            if (o instanceof NodePart) {
                others.remove(o);
                Node model = (Node) ((NodePart) o).getModel();
                if (model.getJobletNode() != null) {
                    continue;
                }
                if (model.getJunitNode() != null) {
                    continue;
                }

                nodeParts.add(o);
            } else if (o instanceof NoteEditPart) {
                noteParts.add(o);
                others.remove(o);
            } else if (o instanceof SubjobContainerPart) {
                others.remove(o);
                SubjobContainerPart subjob = (SubjobContainerPart) o;

                for (Iterator iterator = subjob.getChildren().iterator(); iterator.hasNext();) {
                    NodeContainerPart nodeContainerPart = (NodeContainerPart) iterator.next();
                    if (nodeContainerPart instanceof JobletContainerPart) {
                        JobletContainer jobletCon = (JobletContainer) ((JobletContainerPart) nodeContainerPart).getModel();
                        JobletContainerFigure jobletFigure = (JobletContainerFigure) ((JobletContainerPart) nodeContainerPart)
                                .getFigure();
                        if (!jobletCon.isCollapsed()) {
                            jobletFigure.doCollapse();
                        }
                    }
                    NodePart nodePart = nodeContainerPart.getNodePart();
                    if (nodePart != null) {
                        Node model = (Node) nodePart.getModel();
                        if (model.getJunitNode() != null) {
                            continue;
                        }
                        nodeParts.add(nodePart);
                    }
                }
            }
        }

        if (others.size() == 0) { // so notes & nodes only
            CompoundCommand cpdCmd = new CompoundCommand();
            cpdCmd.setLabel(Messages.getString("GEFDeleteAction.DeleteItems")); //$NON-NLS-1$
            if (nodeParts.size() != 0) {
                GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
                deleteReq.setEditParts(nodeParts);

                cpdCmd.add(((NodePart) nodeParts.get(0)).getCommand(deleteReq));
            }
            if (noteParts.size() != 0) {
                GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
                deleteReq.setEditParts(noteParts);
                cpdCmd.add(((NoteEditPart) noteParts.get(0)).getCommand(deleteReq));
            }

            return cpdCmd;
        } else {
            GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
            deleteReq.setEditParts(objects);

            Command cmd = object.getCommand(deleteReq);
            return cmd;
        }
    }

}
