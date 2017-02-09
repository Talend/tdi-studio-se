package org.talend.designer.core.ui.editor.jobletcontainer;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.rulers.RulerProvider;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ISubjobContainer;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.TalendScalableFreeformRootEditPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.NodeSnapToGeometry;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

public class JobletContainerPart extends NodeContainerPart {

    @Override
    protected IFigure createFigure() {
        IFigure layoutFigure = getLayer(TalendScalableFreeformRootEditPart.MAP_REDUCE_LAYER);

        JobletContainerFigure JobletContainerFigure = new JobletContainerFigure((JobletContainer) this.getModel(), layoutFigure);
        Node node = ((NodeContainer) getModel()).getNode();
        JobletContainerFigure.updateStatus(node.getStatus());

        return JobletContainerFigure;
    }

    @Override
    protected void unregisterVisuals() {
        super.unregisterVisuals();
        if (getFigure() instanceof JobletContainerFigure) {
            ((JobletContainerFigure) getFigure()).dispose();
        }
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void activate() {
        super.activate();
        ((JobletContainer) getModel()).addPropertyChangeListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((JobletContainer) getModel()).removePropertyChangeListener(this);
    }

    @Override
    public void setSelected(int value) {
        super.setSelected(SELECTED_NONE);
    }

    @Override
    public NodePart getNodePart() {
        Object o = this.getChildren().get(0);
        if (o instanceof NodePart) {
            return (NodePart) o;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new JobletContainerLayoutEditPolicy());
    }

    @Override
    protected void refreshVisuals() {
        if (getParent() == null) {// || !isDisplayJoblet
            return;
        }

        Rectangle rectangle = ((JobletContainer) this.getModel()).getJobletContainerRectangle();
        if (rectangle == null) {
            return;
        }
        // Rectangle cleanRectangle = ((NodeContainer) this.getModel()).getNodeMarkRectangle();
        if (this.getParent() != null) {
            if (this.getParent().getModel() instanceof SubjobContainer) {
                // Rectangle subjobRec = ((SubjobContainer) this.getParent().getModel()).getSubjobContainerRectangle();
                ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
            }
        }

        // ((JobletContainerFigure) getFigure()).initializejobletContainer(rectangle);

    }

    @Override
    protected List getModelChildren() {
        return ((JobletContainer) this.getModel()).getElements();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent changeEvent) {
        String prop = changeEvent.getPropertyName();
        boolean needUpdateSubjob = false;
        if (prop.equals(EParameterName.HINT.getName())) {
            Node node = ((NodeContainer) getModel()).getNode();
            // ((JobletContainerFigure) figure).setInfoHint(node.getShowHintText());
        } else if (JobletContainer.UPDATE_JOBLET_CONTENT.equals(prop)) {
            refresh();
            List<AbstractGraphicalEditPart> childrens = getChildren();
            for (AbstractGraphicalEditPart part : childrens) {
                part.refresh();
            }
            needUpdateSubjob = true;
        } else if (JobletContainer.UPDATE_JOBLET_CONNECTIONS.equals(prop)) {
            refreshSourceConnections();
        } else if (JobletContainer.UPDATE_JOBLET_TITLE_COLOR.equals(prop)) {
            if (getFigure() instanceof JobletContainerFigure) {
                ((JobletContainerFigure) getFigure()).updateJobletContainerColor();
                refreshVisuals();
            }
        } else if (JobletContainer.UPDATE_JOBLET_DISPLAY.equals(prop)) {
            if (getFigure() instanceof JobletContainerFigure) {
                ((JobletContainerFigure) getFigure()).updateJobletContainerColor();
                refreshVisuals();
            }
            needUpdateSubjob = true;
        } else if (changeEvent.getPropertyName().equals("UPDATE_STATUS")) { //$NON-NLS-1$
            // ((JobletContainer) getModel()).updateJobletNodes(true);
            if (getFigure() instanceof JobletContainerFigure) {
                String newValue = (String) changeEvent.getNewValue();
                if (newValue != null && newValue.equals("CLEAR")) {
                    ((JobletContainerFigure) getFigure()).refreshNodes(true);
                } else {
                    ((JobletContainerFigure) getFigure()).refreshNodes(false);
                }
                if (((JobletContainer) getModel()).getNode().isMapReduce()) {
                    JobletContainer jCon = (JobletContainer) getModel();
                    if (jCon.getNode().isMapReduceStart()) {
                        jCon.updateJobletNodes(true);
                        ((JobletContainerFigure) getFigure()).updateData();
                    } else if (jCon.getMrStartContainer() != null) {
                        jCon.getMrStartContainer().updateJobletNodes(true);
                    }

                }
            }
            refreshVisuals();
        } else if (changeEvent.getPropertyName().equals("UPDATE_MR_STATUS")) {
            if (getFigure() instanceof JobletContainerFigure) {
                ((JobletContainerFigure) getFigure()).refreshMRstatus();
            }
            refreshVisuals();
        } else { // can only be UPDATE_SUBJOB_DATA, need to modify if some others are added
            if (getFigure() instanceof JobletContainerFigure) {
                if (((JobletContainer) getModel()).getNode().isMapReduce()) {
                    JobletContainer jCon = (JobletContainer) getModel();
                    if (jCon.getNode().isMapReduceStart()) {
                        jCon.updateJobletNodes(true);
                    } else if (jCon.getMrStartContainer() != null) {
                        jCon.getMrStartContainer().updateJobletNodes(true);
                    }

                }
                ((JobletContainerFigure) getFigure()).updateData();
                refreshVisuals();
            }
        }

        if (changeEvent.getPropertyName().equals(EParameterName.ACTIVATE.getName())) {
            Node node = ((NodeContainer) getModel()).getNode();
            if (node.isActivate()) {
                // ((JobletContainerFigure) figure).setAlpha(-1);
                ((JobletContainerFigure) figure).repaint();
                refreshVisuals();
            } else {
                // ((JobletContainerFigure) figure).setAlpha(Node.ALPHA_VALUE);
                ((JobletContainerFigure) figure).repaint();
                refreshVisuals();
            }
        }
        if (changeEvent.getPropertyName().equals(Node.PERFORMANCE_DATA)) {
            refreshVisuals();
        }

        if (needUpdateSubjob) {
            EditPart editPart = getParent();
            if (editPart != null) {
                while ((!(editPart instanceof ProcessPart)) && (!(editPart instanceof SubjobContainerPart))) {
                    editPart = editPart.getParent();
                }
                Node node = ((NodeContainer) getModel()).getNode();

                List<ISubjobContainer> proSubList = new ArrayList<ISubjobContainer>(
                        ((IProcess2) node.getProcess()).getSubjobContainers());

                if (editPart instanceof SubjobContainerPart) {
                    // Node node = ((NodeContainer) getModel()).getNode();
                    JobletContainer nc = (JobletContainer) this.getModel();
                    // Rectangle rec = new Rectangle(node.getLocation(), node.getSize());
                    boolean isCollapse = nc.isCollapsed() && !nc.getNode().isMapReduceStart();
                    int rightChangewidth = nc.getRightChangeWidth();
                    int downChangeheight = nc.getDownChangeHeight();
                    int leftChangewidth = nc.getLeftChangeWidth();
                    int upChangeheight = nc.getUpChangeHeight();
                    if (nc.getNode().isJoblet()) {
                        if (editPart instanceof SubjobContainerPart) {
                            ((SubjobContainer) ((SubjobContainerPart) editPart).getModel()).refreshNodesLocation(isCollapse, nc,
                                    rightChangewidth, downChangeheight, leftChangewidth, upChangeheight);
                        }
                        // for (ISubjobContainer sb : proSubList) {
                        // ((SubjobContainer) sb).refreshNodesLocation(isCollapse, nc, rightChangewidth,
                        // downChangeheight,
                        // leftChangewidth, upChangeheight);
                        // }
                    }

                    editPart.refresh();
                }

            }
        }
        if (changeEvent.getPropertyName().equals(Node.UPDATE_STATUS)) {
            Node node = ((NodeContainer) getModel()).getNode();
            if (node != null && node.isMapReduce()) {
                ((JobletContainerFigure) this.getFigure()).updateErrorFlag(node.isErrorFlag());
                ((JobletContainerFigure) this.getFigure()).setShowCompareMark(node.isCompareFlag() && !node.isErrorFlag());
                IElementParameter param = node.getElementParameter(EParameterName.INFORMATION.getName());
                if (param != null) {
                    boolean showInfoFlag = Boolean.TRUE.equals(param.getValue());
                    if (changeEvent.getNewValue() instanceof Integer) {
                        Integer status = (Integer) changeEvent.getNewValue();
                        if (status != null) {
                            ((JobletContainerFigure) this.getFigure()).updateStatus(status, showInfoFlag);
                        }
                        ((JobletContainerFigure) this.getFigure()).setInfoHint(node.getShowHintText());
                    }
                    refreshVisuals();
                }
            } else {
                ((JobletContainerFigure) getFigure()).updateStatus(node.getStatus());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class key) {
        if (key == SnapToHelper.class) {
            List<Object> snapStrategies = new ArrayList<Object>();
            Boolean val = (Boolean) getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);

            val = (Boolean) getViewer().getProperty(NodeSnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new NodeSnapToGeometry(this));
            }

            val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToGrid(this));
            }

            if (snapStrategies.size() == 0) {
                return null;
            }
            if (snapStrategies.size() == 1) {
                return snapStrategies.get(0);
            }

            SnapToHelper[] ss = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++) {
                ss[i] = (SnapToHelper) snapStrategies.get(i);
            }
            return new CompoundSnapToHelper(ss);
        }

        return super.getAdapter(key);
    }
}
