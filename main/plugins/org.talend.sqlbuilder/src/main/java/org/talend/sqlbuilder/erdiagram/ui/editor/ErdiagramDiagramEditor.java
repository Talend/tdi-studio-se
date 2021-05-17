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
package org.talend.sqlbuilder.erdiagram.ui.editor;

import java.util.EventObject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.tools.PanningSelectionTool;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.talend.sqlbuilder.erdiagram.ui.ErDiagramComposite;
import org.talend.sqlbuilder.erdiagram.ui.actions.ErDiagramItemDeleteAction;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) qzhang $
 *
 */
public class ErdiagramDiagramEditor extends GraphicalEditor {

    private KeyHandler sharedKeyHandler;

    private boolean savePreviouslyNeeded = false;

    public static final int GRID_SIZE = 32;

    private RulerComposite rulerComp;

    public static final String ID = "org.talend.sqlbuider.erdiagram.diagram.part.SqlBuilderErdiagramDiagramEditor"; //$NON-NLS-1$

    /**
     * admin ErdiagramDiagramEditor constructor comment.
     */
    public ErdiagramDiagramEditor() {
        DefaultEditDomain editDomain = new DefaultEditDomain(this);
        editDomain.setDefaultTool(new PanningSelectionTool());
        setEditDomain(editDomain);

    }

    protected KeyHandler getCommonKeyHandler() {
        if (sharedKeyHandler == null) {
            sharedKeyHandler = new KeyHandler();
            sharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 0), getActionRegistry().getAction(
                    ActionFactory.DELETE.getId()));
        }
        return sharedKeyHandler;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();
        ErDiagramRootEditPart root = new ErDiagramRootEditPart();

        IAction deleteAction = new ErDiagramItemDeleteAction(this);
        getActionRegistry().registerAction(deleteAction);
        getSelectionActions().add(deleteAction.getId());

        viewer.setRootEditPart(root);

        ErDiagramPartFactory partFactory = new ErDiagramPartFactory();
        // set the factory to use for creating EditParts for elements in the
        // model
        getGraphicalViewer().setEditPartFactory(partFactory);
        getGraphicalViewer().setKeyHandler(
                new GraphicalViewerKeyHandler(getGraphicalViewer()).setParent(getCommonKeyHandler()));
        /** * Management of the context menu ** */
        ContextMenuProvider cmProvider = new ErDiagramMenuProvider(this, viewer, getActionRegistry());
        viewer.setContextMenu(cmProvider);

        /** * Snap To Grid ** */
        // Grid properties
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_SPACING, new Dimension(GRID_SIZE, GRID_SIZE));
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED,
                new Boolean(true/* getProcess().isGridEnabled() */));
        // We keep grid visibility and enablement in sync
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE,
                new Boolean(true/* getProcess().isGridEnabled() */));
        IAction showGrid = new ToggleGridAction(getGraphicalViewer());
        getActionRegistry().registerAction(showGrid);

        /** * Snap To Geometry ** */
        getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED,
                new Boolean(false/* getProcess().isSnapToGeometryEnabled() */));
        IAction snapAction = new ToggleSnapToGeometryAction(getGraphicalViewer());
        getActionRegistry().registerAction(snapAction);
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#hookGraphicalViewer()
     */
    @Override
    protected void hookGraphicalViewer() {
        getSelectionSynchronizer().addViewer(getGraphicalViewer());
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
    }

    public Control getGraphicalControl() {
        rulerComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        return rulerComp;
    }

    public void updateSqlText() {
        if (rulerComp.getParent() instanceof ErDiagramComposite) {
            ErDiagramComposite erComposite = (ErDiagramComposite) rulerComp.getParent();
            erComposite.updateSql();
        }
    }

    protected void createGraphicalViewer(final Composite parent) {
        rulerComp = new RulerComposite(parent, SWT.BORDER);
        super.createGraphicalViewer(rulerComp);
        rulerComp.setGraphicalViewer((ScrollingGraphicalViewer) getGraphicalViewer());
    }

    public void commandStackChanged(final EventObject event) {
        if (isDirty()) {
            if (!this.savePreviouslyNeeded) {
                this.savePreviouslyNeeded = true;
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        } else {
            savePreviouslyNeeded = false;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        super.commandStackChanged(event);
    }

    protected void initializeGraphicalViewer() {

    }

    public boolean isDirty() {
        // rely on the command stack to determine dirty flag
        return getCommandStack().isDirty();
    }

    public FigureCanvas getEditor() {
        return (FigureCanvas) getGraphicalViewer().getControl();
    }

    /**
     * Get the viewer in the editor.
     *
     * @return
     */
    public GraphicalViewer getViewer() {
        return getGraphicalViewer();
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {

    }

}
