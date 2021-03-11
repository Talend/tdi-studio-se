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
package org.talend.designer.gefabstractmap.editor;

import java.util.EventObject;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.talend.designer.gefabstractmap.dnd.MapperDragSourceListener;
import org.talend.designer.gefabstractmap.dnd.MapperDropTargetListener;
import org.talend.designer.gefabstractmap.manager.AbstractMapperManager;
import org.talend.designer.gefabstractmap.part.InputTablePart;
import org.talend.designer.gefabstractmap.part.MapperRootEditPart;
import org.talend.designer.gefabstractmap.part.OutputTablePart;

/**
 * created by Administrator on 2013-1-16 Detailled comment
 *
 */
public abstract class MapperGraphicalEditor extends GraphicalEditor {

    private KeyHandler sharedKeyHandler;

    private AbstractMapperManager mapperManager;

    public MapperGraphicalEditor(AbstractMapperManager mapperManager) {
        DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
        defaultEditDomain.getCommandStack().addCommandStackListener(this);
        setEditDomain(defaultEditDomain);
        this.mapperManager = mapperManager;
    }

    @Override
    protected void createGraphicalViewer(final Composite parent) {
        // rulerComp = new RulerComposite(parent, SWT.BORDER);
        MapperGraphicalViewer viewer = createMapperGraphicalViewer();
        viewer.setMapperManager(mapperManager);
        viewer.createControl(parent);
        setGraphicalViewer(viewer);
        configureGraphicalViewer();
        hookGraphicalViewer();
        initializeGraphicalViewer();
        // super.createGraphicalViewer(rulerComp);
        // rulerComp.setGraphicalViewer( getGraphicalViewer());
    }

    /**
     * @see org.eclipse.gef.commands.CommandStackListener#commandStackChanged(java.util.EventObject)
     */
    @Override
    public void commandStackChanged(EventObject event) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#createActions()
     */
    @Override
    protected void createActions() {
    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
     */
    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        getGraphicalViewer().setRootEditPart(new MapperScalableRootEditPart());
        getGraphicalViewer().setKeyHandler(new GraphicalViewerKeyHandler(getGraphicalViewer()).setParent(getCommonKeyHandler()));

    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
     */
    @Override
    protected void initializeGraphicalViewer() {

        // getGraphicalViewer().setContents(getContents());

        getGraphicalViewer().addDragSourceListener(createDragSourceListener());

        getGraphicalViewer().addDropTargetListener(createDropTargetListener());

        getGraphicalViewer().setContextMenu(new MenueProvider(getGraphicalViewer()));
        initializeActionRegistry();
    }

    public AbstractMapperManager getMapperManager() {
        return this.mapperManager;
    }

    public void setContent(Object content) {
        getGraphicalViewer().setContents(content);
    }

    public void makeDefaultSelection() {
        EditPart contents = getGraphicalViewer().getContents();
        if (contents instanceof MapperRootEditPart) {
            MapperRootEditPart mapdataPart = (MapperRootEditPart) contents;
            List children = mapdataPart.getChildren();
            InputTablePart firstInputPart = null;
            OutputTablePart firstOutputPart = null;
            if (children != null) {
                for (int i = 0; i < children.size(); i++) {
                    Object object = children.get(i);
                    if (object instanceof InputTablePart && firstInputPart == null) {
                        firstInputPart = (InputTablePart) object;
                    }
                    if (object instanceof OutputTablePart && firstOutputPart == null) {
                        firstOutputPart = (OutputTablePart) object;
                    }
                    if (firstInputPart != null && firstOutputPart != null) {
                        break;
                    }
                }

                if (firstInputPart != null) {
                    getGraphicalViewer().appendSelection(firstInputPart);
                }
                if (firstOutputPart != null) {
                    getGraphicalViewer().appendSelection(firstOutputPart);
                }
            }
        }
    }

    protected KeyHandler getCommonKeyHandler() {
        if (sharedKeyHandler == null) {
            // sharedKeyHandler = new KeyHandler();
            // sharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
            // getActionRegistry().getAction(ActionFactory.DELETE.getId()));
            // sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
            // getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
        }
        return sharedKeyHandler;
    }

    /**
     * Get the viewer in the editor.
     *
     * @return
     */
    public GraphicalViewer getViewer() {
        return getGraphicalViewer();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void hookGraphicalViewer() {
        getSelectionSynchronizer().addViewer(getGraphicalViewer());
    }

    /**
     * wchen class global comment. Detailled comment
     */
    class MenueProvider extends ContextMenuProvider {

        public MenueProvider(EditPartViewer viewer) {
            super(viewer);
        }

        @Override
        public void buildContextMenu(IMenuManager menu) {
            MapperGraphicalEditor.this.buildContextMenu(menu);
        }
    }

    protected abstract MapperGraphicalViewer createMapperGraphicalViewer();

    protected abstract MapperDragSourceListener createDragSourceListener();

    protected abstract MapperDropTargetListener createDropTargetListener();

    public void buildContextMenu(IMenuManager menu) {
    }

}
