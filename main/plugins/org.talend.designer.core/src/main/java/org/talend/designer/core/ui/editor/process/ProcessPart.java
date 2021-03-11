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
package org.talend.designer.core.ui.editor.process;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorInput;
import org.talend.core.model.process.INode;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.TalendScalableFreeformRootEditPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerFigure;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.utils.DesignerColorUtils;
import org.talend.designer.core.utils.ResourceDisposeUtil;
import org.talend.repository.model.RepositoryNode;

/**
 * EditPart of the Diagram that will set the main layer, and the differents kinds of rulers. <br/>
 *
 * $Id$
 *
 */
public class ProcessPart extends AbstractGraphicalEditPart implements PropertyChangeListener, IAdaptable {

    private FreeformLayer fig2;

    private RepositoryNode node;

    // private static final Color READ_WRITE_COLOR = new Color(null, DesignerColorUtils.getPreferenceDesignerEditorRGB(
    // DesignerColorUtils.JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEAULT_EDITOR_COLOR));
    //
    // private static final Color READ_ONLY_COLOR = new Color(null, DesignerColorUtils.getPreferenceReadonlyRGB(
    // DesignerColorUtils.READONLY_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEAULT_READONLY_COLOR));

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    public List getModelChildren() {
        return ((Process) this.getModel()).getElements();
    }

    @Override
    protected void unregisterVisuals() {
        super.unregisterVisuals();
        if (fig2 != null) {
            ResourceDisposeUtil.disposeColor(fig2.getBackgroundColor());
            ResourceDisposeUtil.disposeColor(fig2.getForegroundColor());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        super.activate();
        ((Process) getModel()).addPropertyChangeListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (!isActive()) {
            return;
        }
        super.deactivate();
        ((Process) getModel()).removePropertyChangeListener(this);
        fig2.getBackgroundColor().dispose();
        fig2.getForegroundColor().dispose();
        fig2 = null;
        node = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        Figure figure = new FreeformLayer();
        figure.setLayoutManager(new FreeformLayout());

        fig2 = new FreeformLayer();
        getLayer(TalendScalableFreeformRootEditPart.PROCESS_BACKGROUND_LAYER).add(fig2);
        ajustReadOnly();
        // ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        // connLayer.setConnectionRouter(new NullRouter());
        return figure;
    }

    /**
     * DOC smallet Comment method "ajustReadOnly".
     */
    public void ajustReadOnly() {
        if (((Process) getModel()).isReadOnly()) {

            fig2.setBackgroundColor(new Color(null, DesignerColorUtils.getPreferenceReadonlyRGB(
                    DesignerColorUtils.READONLY_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEFAULT_READONLY_COLOR)));
            fig2.setOpaque(true);
        } else {
            ResourceDisposeUtil.disposeColor(fig2.getBackgroundColor());
            fig2.setBackgroundColor(new Color(null, DesignerColorUtils.getPreferenceDesignerEditorRGB(
                    DesignerColorUtils.JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEFAULT_EDITOR_COLOR)));
            fig2.setOpaque(true);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(final Class adapter) {
        if (adapter.equals(RepositoryNode.class)) {
            if (node == null) {
                RootEditPart rootEditPart = getRoot();
                if (rootEditPart instanceof TalendScalableFreeformRootEditPart) {
                    TalendScalableFreeformRootEditPart rootEditPart2 = (TalendScalableFreeformRootEditPart) rootEditPart;
                    IEditorInput editorInput = rootEditPart2.getEditorInput();
                    if (editorInput instanceof ProcessEditorInput) {
                        ProcessEditorInput processEditorInput = (ProcessEditorInput) editorInput;
                        node = processEditorInput.getRepositoryNode();
                    }
                }
            }
            return node;
        }

        if (adapter == SnapToHelper.class) {
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

        return super.getAdapter(adapter);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        super.refreshVisuals();
        if (DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_ANTIALIASING)) {
            ConnectionLayer cLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
            cLayer.setAntialias(SWT.ON);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessLayoutEditPolicy());
        installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (Process.NEED_UPDATE_JOB.equals(prop)) {
            refresh();
        } else if (SubjobContainer.UPDATE_SUBJOB_DATA.equals(prop)) {
            for (Object part : getChildren()) {
                if (part != null && part instanceof SubjobContainerPart) {
                    IFigure fig = ((SubjobContainerPart) part).getFigure();
                    if (fig != null && fig instanceof SubjobContainerFigure) {
                        ((SubjobContainerFigure) fig).updateData();
                        ((SubjobContainerPart) part).refreshVisuals();
                        refresh();
                    }
                }
            }
        }
    }

    @Override
    public List getChildren() {
        return super.getChildren();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#refresh()
     */
    @Override
    public void refresh() {
        sortNode();
        super.refresh();
    }

    /**
     *
     * DOC ggu Comment method "sortNode". <br/>
     *
     * Sort components Alphabeticaly in the Outline viewer. <br/>
     *
     * Rule: sorted by unique name
     */

    private void sortNode() {
        Process process = (Process) this.getModel();

        List<INode> eleList = (List<INode>) process.getGraphicalNodes();
        if (eleList.size() > 1) {
            Node node, tmpNode;
            for (int i = 1; i < eleList.size(); i++) {
                tmpNode = (Node) eleList.get(i);
                for (int k = i; k > 0; k--) {
                    node = (Node) eleList.get(k - 1);
                    // ascending order
                    if (node.getUniqueName().compareTo(tmpNode.getUniqueName()) > 0) {
                        // Swap the node
                        eleList.remove(node);
                        eleList.add(k, node);
                    }
                }
            }
        }

    }

}
