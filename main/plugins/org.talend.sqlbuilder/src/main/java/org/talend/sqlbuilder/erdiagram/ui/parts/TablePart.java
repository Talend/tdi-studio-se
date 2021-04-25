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
package org.talend.sqlbuilder.erdiagram.ui.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.LayoutEditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.talend.sqlbuilder.erdiagram.ui.figures.ColumnPaneFigure;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;

/**
 *  qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class TablePart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    protected IFigure primaryShape;

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        return ((Table) getModel()).getColumns();
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        CustomTableFigure tablefigure = new CustomTableFigure();
        this.primaryShape = tablefigure;
        return tablefigure;
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#addChildVisual(org.eclipse.gef.EditPart, int)
     */
    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        if (childEditPart instanceof ColumnPart) {
            super.addChildVisual(childEditPart, -1);
        }
    }

    public CustomTableFigure getPrimaryShape() {
        return (CustomTableFigure) primaryShape;
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new TableEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
    }

    protected LayoutEditPolicy createLayoutEditPolicy() {
        LayoutEditPolicy lep = new LayoutEditPolicy() {

            protected EditPolicy createChildEditPolicy(EditPart child) {
                EditPolicy result = child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
                if (result == null) {
                    result = new NonResizableEditPolicy();
                }
                return result;
            }

            protected Command getMoveChildrenCommand(Request request) {
                return null;
            }

            protected Command getCreateCommand(CreateRequest request) {
                return null;
            }
        };
        return lep;
    }

    /*
     * (non-Java)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Table.PROP_LOCATION)) {
            refreshVisuals();
        }
        if (evt.getPropertyName().equals(Table.PROP_SIZE)) {
            refreshVisuals();
        }

    }

    /**
     *  admin TablePart class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     *
     */
    public class CustomTableFigure extends org.talend.sqlbuilder.erdiagram.ui.figures.CustomTableFigure {

        public static final int BUTTON_HEIGHT = 12;

        private ColumnPaneFigure columnPaneFigure;

        Label tableName;

        /**
         * @generated
         */
        public CustomTableFigure() {

            this.setBorder(new org.eclipse.draw2d.MarginBorder(2, 3, 7, 7));
            ToolbarLayout toolbarLayout = new ToolbarLayout();
            toolbarLayout.setVertical(true);
            setLayoutManager(toolbarLayout);
            setBorder(new LineBorder(ColorConstants.black, 1));
            setOpaque(true);
            createContents2();
        }

        protected void createContents() {
            tableName = new Label();
            Font erFont = new Font(null, "Arial", 9, SWT.BOLD); //$NON-NLS-1$
            tableName.setFont(erFont);
            tableName.setForegroundColor(ColorConstants.white);
            tableName.setText(((Table) getModel()).getElementName());
            tableName.setLayoutManager(new ToolbarLayout());
            setFigureCustomTableNameFigure(tableName);
            this.add(tableName);
        }

        private void createContents2() {
            ScrollPane columnsPane = new ScrollPane();
            columnPaneFigure = new ColumnPaneFigure();
            columnsPane.getViewport().setContents(columnPaneFigure);

            this.tableName = new Label();
            tableName.setText(((Table) getModel()).getElementName());
            Font erFont = new Font(null, "Arial", 9, SWT.BOLD); //$NON-NLS-1$
            tableName.setFont(erFont);
            tableName.setForegroundColor(ColorConstants.white);
            add(tableName, 0);
            add(columnsPane);
        }

        /**
         * @generated
         */
        private Label fCustomTableNameFigure;

        /**
         * @generated
         */
        public Label getFigureCustomTableNameFigure() {
            return fCustomTableNameFigure;
        }

        /**
         * @generated
         */
        private void setFigureCustomTableNameFigure(Label fig) {
            fCustomTableNameFigure = fig;
        }

        /**
         * @generated
         */
        private boolean myUseLocalCoordinates = false;

        /**
         * @generated
         */
        protected boolean useLocalCoordinates() {
            return myUseLocalCoordinates;
        }

        /**
         * @generated
         */
        protected void setUseLocalCoordinates(boolean useLocalCoordinates) {
            myUseLocalCoordinates = useLocalCoordinates;
        }

        public ColumnPaneFigure getColumnPaneFigure() {
            return this.columnPaneFigure;
        }

        public void setColumnPaneFigure(ColumnPaneFigure columnPaneFigure) {
            this.columnPaneFigure = columnPaneFigure;
        }

    }

    @Override
    protected void refreshVisuals() {
        Table table = (Table) getModel();
        Point loc = table.getLocation();
        Dimension size = table.getSize();
        Rectangle rectangle = new Rectangle(loc, size);
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);

    }

    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        super.activate();
        ((Table) getModel()).addPropertyChangeListener(this);

    }

    @Override
    public void deactivate() {
        if (!isActive()) {
            return;
        }
        super.deactivate();
        ((Table) getModel()).removePropertyChangeListener(this);

    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getContentPane()
     */
    @Override
    public IFigure getContentPane() {
        ColumnPaneFigure panel = ((CustomTableFigure) getFigure()).getColumnPaneFigure();
        return panel;
    }
}
