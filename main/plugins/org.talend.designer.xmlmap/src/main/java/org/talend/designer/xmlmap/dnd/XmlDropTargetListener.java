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
package org.talend.designer.xmlmap.dnd;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Shell;
import org.talend.designer.gefabstractmap.dnd.MapperDropTargetListener;
import org.talend.designer.gefabstractmap.dnd.TransferedObject;
import org.talend.designer.gefabstractmap.figures.sash.ISash;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.parts.VarNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlDropTargetListener extends MapperDropTargetListener {

    private IFigure targetFigure;

    public XmlDropTargetListener(EditPartViewer viewer) {
        super(viewer);
    }

    @Override
    protected Request createTargetRequest() {
        CreateNodeConnectionRequest request = new CreateNodeConnectionRequest(getTargetEditPart());
        return request;
    }

    @Override
    protected void updateTargetEditPart() {
        super.updateTargetEditPart();
        Point dropLocation = getDropLocation();
        if (getTargetEditPart() != null) {
            targetFigure = ((AbstractGraphicalEditPart) getTargetEditPart()).getFigure().findFigureAt(dropLocation.x,
                    dropLocation.y);
        }
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

    @Override
    public void handleDragOver() {
        getCurrentEvent().detail = DND.DROP_NONE;
        getCurrentEvent().feedback = DND.FEEDBACK_NONE;
        updateTargetRequest();
        updateTargetEditPart();

        DropTargetEvent event = getCurrentEvent();

        Object object = TemplateTransfer.getInstance().getObject();
        if (object == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        // dnd the sash
        if (object instanceof ISash) {
            handleSashDrag(event, (ISash) object);
            return;
        }

        // dnd the tree node
        if (!(object instanceof TransferedObject)) {
            event.detail = DND.DROP_NONE;
            return;
        }

        DropContextAnalyzer analyzer = new DropContextAnalyzer((TransferedObject) object,
                (AbstractGraphicalEditPart) getTargetEditPart(), getDropLocation());
        if (analyzer.isDropValid()) {
            event.detail = analyzer.getDetail();
            NewNodeCreationFactory factory = new NewNodeCreationFactory(analyzer.getDropType(), null);
            getCreateRequest().setFactory(factory);
        }

        showTargetFeedback();
    }

    @Override
    public void drop(DropTargetEvent event) {
        setCurrentEvent(event);
        eraseTargetFeedback();
        handleDrop();
        unload();
    }

    @Override
    protected void handleDrop() {
        final Object object = TemplateTransfer.getInstance().getObject();
        if (object == null || !(object instanceof TransferedObject)) {
            return;
        }
        updateTargetRequest();
        updateTargetEditPart();

        DropContextAnalyzer analyzer = new DropContextAnalyzer((TransferedObject) object,
                (AbstractGraphicalEditPart) getTargetEditPart(), getDropLocation());
        if (analyzer.isDropValid()) {
            NodeType selectedNodeType = NodeType.ELEMENT;
            DropType dropType = analyzer.getDropType();
            if (dropType == DropType.DROP_OUTPUT_DOC_CHILD && getTargetEditPart() instanceof OutputTreeNodeEditPart) {
                OutputTreeNode targetOutputNode = (OutputTreeNode) ((OutputTreeNodeEditPart) getTargetEditPart()).getModel();
                Shell shell = getViewer().getControl().getShell();

                // if allNamespace , create output as namespace , if allsubTree , create output subtree , no need prompt
                boolean needPrompt = false;
                boolean hasSubTree = false;
                for (Object o : ((TransferedObject) object).getToTransfer()) {
                    if (o instanceof VarNodeEditPart) {
                        needPrompt = true;
                    } else if (o instanceof TreeNodeEditPart) {
                        TreeNode treeNode = (TreeNode) ((TreeNodeEditPart) o).getModel();
                        if (NodeType.ATTRIBUT.equals(treeNode.getNodeType())) {
                            needPrompt = true;
                        }
                        if (NodeType.ELEMENT.equals(treeNode.getNodeType())) {
                            if (treeNode.getChildren().isEmpty()) {
                                needPrompt = true;
                            } else {
                                hasSubTree = true;
                            }
                        }
                    }
                }

                if (needPrompt) {
                    DragAndDrogDialog selectDialog = new DragAndDrogDialog(shell, !targetOutputNode.getChildren().isEmpty());
                    int open = selectDialog.open();
                    if (open == Window.OK) {
                        if (DragAndDrogDialog.CREATE_AS_SUBELEMENT.equals(selectDialog.getSelectValue())) {
                            selectedNodeType = NodeType.ELEMENT;
                        } else if (DragAndDrogDialog.CREATE_AS_ATTRIBUTE.equals(selectDialog.getSelectValue())) {
                            selectedNodeType = NodeType.ATTRIBUT;
                        } else if (DragAndDrogDialog.CREATE_AS_SUBELEMENT.equals(selectDialog.getSelectValue())) {
                            selectedNodeType = NodeType.NAME_SPACE;
                        } else if (DragAndDrogDialog.CREATE_AS_TEXT.equals(selectDialog.getSelectValue())) {
                            dropType = DropType.DROP_EXPRESSION;
                        }
                    } else {
                        return;
                    }
                }

                if (dropType != DropType.DROP_EXPRESSION) {
                    if (!targetOutputNode.getIncomingConnections().isEmpty()
                            && ((selectedNodeType != NodeType.ATTRIBUT && selectedNodeType != NodeType.NAME_SPACE) || hasSubTree)) {
                        boolean canContinue = MessageDialog
                                .openConfirm(null, "Warning",
                                        "Do you want to disconnect the existing linker and then add an sub element for the selected element ?");
                        if (canContinue) {
                            MapperManager mapperManager = ((XmlMapGraphicViewer) getViewer()).getMapperManager();
                            if (mapperManager != null && mapperManager.getExternalData() != null) {
                                XmlMapUtil.detachNodeConnections(targetOutputNode, mapperManager.getExternalData(), false);
                            }
                        } else {
                            return;
                        }
                    }
                }
            }

            NewNodeCreationFactory factory = new NewNodeCreationFactory(dropType, selectedNodeType);
            getCreateRequest().setFactory(factory);
        }

        if (getTargetEditPart() != null) {
            Command command = getCommand();
            if (command != null && command.canExecute()) {
                getViewer().getEditDomain().getCommandStack().execute(command);
            } else {
                getCurrentEvent().detail = DND.DROP_NONE;
            }
        } else {
            getCurrentEvent().detail = DND.DROP_NONE;
        }

        selectAddedObject();
    }

    private void selectAddedObject() {
        boolean added = false;
        List newObjects = getCreateRequest().getNewObjects();
        for (int i = 0; i < newObjects.size(); i++) {
            Object model = newObjects.get(i);
            EditPartViewer viewer = getViewer();
            viewer.getControl().forceFocus();
            Object editpart = viewer.getEditPartRegistry().get(model);
            if (editpart instanceof EditPart) {
                if (!added) {
                    // Force a layout first.
                    getViewer().flush();
                    viewer.select((EditPart) editpart);
                    added = true;
                } else {
                    viewer.appendSelection((EditPart) editpart);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.dnd.AbstractTransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
     */
    @Override
    public boolean isEnabled(DropTargetEvent event) {
        final Object object = TemplateTransfer.getInstance().getObject();
        if (object instanceof ISash) {
            return true;
        }
        return super.isEnabled(event);
    }

    @Override
    protected void updateTargetRequest() {
        CreateNodeConnectionRequest request = getCreateRequest();
        request.setLocation(getDropLocation());
    }

    protected CreateNodeConnectionRequest getCreateRequest() {
        return ((CreateNodeConnectionRequest) getTargetRequest());
    }

    @Override
    protected void handleExitingEditPart() {
        eraseTargetFeedback();
    }

}
