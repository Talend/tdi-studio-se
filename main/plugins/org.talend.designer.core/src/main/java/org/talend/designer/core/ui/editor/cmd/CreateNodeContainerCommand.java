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
package org.talend.designer.core.ui.editor.cmd;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.UpdateManagerHelper;
import org.talend.core.model.update.extension.UpdateManagerProviderDetector;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.StitchPseudoComponent;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.palette.TalendPaletteViewer;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.problems.Problems;

/**
 * Command that create a new node. <br/>
 *
 * $Id$
 *
 */
public class CreateNodeContainerCommand extends CreateCommand {

    private final NodeContainer nodeContainer;

    /**
     * Create the node on the given diagram.
     *
     * @param diagram
     * @param node
     * @param location
     */
    public CreateNodeContainerCommand(Process process, NodeContainer nodeContainer, Point location) {
        super(Messages.getString("CreateNodeCommand.Label"), process, location); //$NON-NLS-1$
        this.nodeContainer = nodeContainer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canExecute() {
        if (nodeContainer.getNode().getComponent() instanceof StitchPseudoComponent) {
            return false;
        }

        for (IGraphicalNode currentNode : (List<IGraphicalNode>) process.getGraphicalNodes()) {
            if ((currentNode.getLocation().x == location.x) && (currentNode.getLocation().y == location.y)) {
                return false;
            }
            // check if the component is sigleton
            // see bug 3903
            if (currentNode.getComponent() == nodeContainer.getNode().getComponent()
                    && nodeContainer.getNode().getComponent().isSingleton()) {
                return false;
            }
        }

        AbstractProcessProvider provider = AbstractProcessProvider.findProcessProviderFromPID(nodeContainer.getNode()
                .getComponent().getPluginExtension());
        if (provider != null) {
            if (!provider.canCreateNode(nodeContainer.getNode())) {
                return false;
            }

        }

        return true;
    }

    @Override
    public void execute() {
        if (this.location != null) {
            this.nodeContainer.getNode().setLocation(this.location);
        }

        updatePaletteRecentlyUsedList();

        AbstractProcessProvider provider = AbstractProcessProvider.findProcessProviderFromPID(nodeContainer.getNode()
                .getComponent().getPluginExtension());
        IElementParameter ep = nodeContainer.getNode().getElementParameter(EParameterName.UNIQUE_NAME.getName());
        process.addUniqueNodeName(ep.getValue().toString());
        if (provider == null || (provider != null && provider.containNodeInMemoryNotProcess())) {
            this.process.addNodeContainer(this.nodeContainer);
            process.checkStartNodes();

            nodeContainer.getNode().checkAndRefreshNode();
            refreshRelatedNodes();
            // update joblet context.
            // AbstractProcessProvider provider =
            // AbstractProcessProvider.findProcessProviderFromPID(nodeContainer.getNode()
            // .getComponent().getPluginFullName());
            // if (provider != null) {
            // provider.updateJobletContext(nodeContainer.getNode());
            // }
            if (nodeContainer.getNode().getComponent().getComponentType() == EComponentType.JOBLET) {
                IUpdateItemType jobletContextType = UpdateManagerProviderDetector.INSTANCE
                        .getUpdateItemType(UpdateManagerHelper.TYPE_JOBLET_CONTEXT);
                if (jobletContextType != null) {
                    process.getUpdateManager().update(jobletContextType);
                }
            }
        } else {
            String name = provider.getComponentProcess().getName() + " " + provider.getComponentProcess().getVersion();
            MessageDialog warningMessageDialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell(), "Can't create node", null, "Joblet process " + name + " is not saved. Please save it first",
                    MessageDialog.OK, new String[] { "OK" }, 0);
            warningMessageDialog.open();

        }
    }

    private void refreshRelatedNodes() {
        Node node = nodeContainer.getNode();
        for (INode inode : this.process.getGraphicalNodes()) {
            if (inode.getUniqueName().equals(node.getUniqueName())) {
                continue;
            }
            if (inode.getElementParameter("CONNECTION") != null) {
                IElementParameter para = inode.getElementParameter("CONNECTION");
                if (para.getFilter() != null && para.getFilter().equals(node.getComponent().getName())) {
                    ((Node) inode).checkAndRefreshNode();
                }
            }
        }
    }

    /**
     * DOC cmeng Comment method "updatePaletteRecentlyUsedList".
     */
    protected void updatePaletteRecentlyUsedList() {
        if (this.process == null) {
            return;
        }
        Object editor = this.process.getEditor();
        if (editor instanceof AbstractMultiPageTalendEditor) {
            AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) editor).getTalendEditor();
            if (talendEditor != null) {
                TalendPaletteViewer talendPaletteViewer = talendEditor.getTalendPaletteViewer();
                if (talendPaletteViewer != null) {
                    talendPaletteViewer.addRecentlyUsedComponent(this.nodeContainer.getNode().getComponent());
                }
            }
        }
    }

    @Override
    public void undo() {
        this.process.removeNodeContainer(this.nodeContainer);
        // process.checkProcess();
        process.checkStartNodes();
        Problems.clearAll(nodeContainer.getNode());
        Problems.refreshProblemTreeView();
    }

    @Override
    public void redo() {
        this.execute();
    }
}
