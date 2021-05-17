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

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.CheckBox;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Column;
import org.talend.sqlbuilder.erdiagram.ui.parts.TablePart.CustomTableFigure;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ColumnPart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {

    private CustomColumnFigure primativeFigure;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        primativeFigure = new CustomColumnFigure();
        return primativeFigure;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ColumnGraphicalEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new ColumnEditPolicy());
    }

    @Override
    public List getModelSourceConnections() {
        return ((Column) getModel()).getInputs();
    }

    @Override
    public List getModelTargetConnections() {
        return ((Column) getModel()).getOutputs();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Column.PROP_INPUTS)) {
            refreshSourceConnections();
        } else if (evt.getPropertyName().endsWith(Column.PROP_OUTPUTS)) {
            refreshTargetConnections();
        }
        ((Column) getModel()).getTable().getErDiagram().setDirty(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return new ColumnRelationAnchor(getFigure());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return new ColumnRelationAnchor(getFigure());
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
        ((Column) getModel()).addPropertyChangeListener(this);
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
        ((Column) getModel()).removePropertyChangeListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return new ColumnRelationAnchor(getFigure());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return new ColumnRelationAnchor(getFigure());
    }

    /**
     * DOC admin ColumnPart class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     *
     */
    public class ColumnRelationAnchor extends ChopboxAnchor {

        /**
         * DOC admin ColumnPart.ColumnRelationAnchor constructor comment.
         */
        public ColumnRelationAnchor() {
            super();
        }

        public ColumnRelationAnchor(IFigure owner) {
            super(owner);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.draw2d.ChopboxAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
         */
        @Override
        public Point getLocation(Point reference) {
            Point point;
            point = super.getLocation(reference);
            Rectangle r = new Rectangle();
            r.setBounds(getBox());
            r.translate(-1, -1);
            r.resize(1, 1);
            getOwner().translateToAbsolute(r);

            TablePart tablePart = (TablePart) getParent();
            CustomTableFigure figure = tablePart.getPrimaryShape();
            Rectangle r2 = new Rectangle();
            r2.setBounds(figure.getBounds());
            // r2.translate(-1, -1);
            // r2.resize(1, 1);
            // figure.translateToAbsolute(r2);

            int cX2 = (reference.x > (r2.x + r2.width)) ? (r2.x + r2.width) : (r2.x);

            float centerY = r.y + 0.5f * r.height;
            int cX = (reference.x > (r.x + r.width)) ? (r.x + r.width) : (r.x);
            int cY = Math.round(centerY);

            cY = (cY > r2.y + r2.height) ? (r2.y + r2.height) : cY;
            cY = (cY < r2.y) ? r2.y : cY;

            point = new Point(cX, cY);
            point = new Point(cX2 > cX ? cX2 : cX, cY);

            return point;
        }
    }

    /**
     * DOC admin ColumnPart class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     *
     */
    public class CustomColumnFigure extends org.talend.sqlbuilder.erdiagram.ui.figures.CustomColumnFigure {

        /**
         * @generated
         */
        public CustomColumnFigure() {
            org.eclipse.draw2d.FlowLayout myGenLayoutManager = new org.eclipse.draw2d.FlowLayout();
            myGenLayoutManager.setStretchMinorAxis(false);
            myGenLayoutManager.setMinorAlignment(org.eclipse.draw2d.FlowLayout.ALIGN_CENTER);
            myGenLayoutManager.setMajorAlignment(org.eclipse.draw2d.FlowLayout.ALIGN_LEFTTOP);
            myGenLayoutManager.setMajorSpacing(5);
            myGenLayoutManager.setMinorSpacing(5);
            myGenLayoutManager.setHorizontal(true);

            this.setLayoutManager(myGenLayoutManager);
            this.setBorder(new ColumnFigureBorder());
            createContents();
        }

        CheckBox isSelected;

        Label columnName;

        /**
         * @generated
         */
        private void createContents() {

            final Column column = (Column) getModel();
            if (!column.getElementName().equals("*")) { //$NON-NLS-1$
                isSelected = new CheckBox();
                isSelected.setSelected(column.isSelected());
                setFigureCustomColumnIsSelectedFigure(isSelected);
                this.add(isSelected);
                isSelected.addChangeListener(new ChangeListener() {

                    public void handleStateChanged(ChangeEvent event) {
                        if (event.getPropertyName().equals(ButtonModel.SELECTED_PROPERTY)) {
                            column.setSelected(isSelected.isSelected());
                        }
                    }

                });
            }

            columnName = new Label();

            columnName.setText(column.getElementName());
            Font erFont = new Font(null, "Arial", 7, SWT.BOLD); //$NON-NLS-1$
            columnName.setFont(erFont);
            columnName.setForegroundColor(ColorConstants.black);
            setFigureCustomColumnNameFigure(columnName);

            this.add(columnName);
        }

        /**
         * @generated
         */
        private CheckBox fCustomColumnIsSelectedFigure;

        /**
         * @generated
         */
        public CheckBox getFigureCustomColumnIsSelectedFigure() {
            return fCustomColumnIsSelectedFigure;
        }

        /**
         * @generated
         */
        private void setFigureCustomColumnIsSelectedFigure(CheckBox fig) {
            fCustomColumnIsSelectedFigure = fig;
        }

        /**
         * @generated
         */
        private Label fCustomColumnNameFigure;

        /**
         * @generated
         */
        public Label getFigureCustomColumnNameFigure() {
            return fCustomColumnNameFigure;
        }

        /**
         * @generated
         */
        private void setFigureCustomColumnNameFigure(Label fig) {
            fCustomColumnNameFigure = fig;
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

        /**
         * DOC admin ColumnPaneFigure class global comment. Detailled comment <br/>
         *
         * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
         *
         */
        class ColumnFigureBorder extends AbstractBorder {

            public Insets getInsets(IFigure figure) {
                return new Insets(5, 3, 5, 3);
            }

            public void paint(IFigure figure, Graphics graphics, Insets insets) {
                graphics.setForegroundColor(ColorConstants.black);
                graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), tempRect.getTopRight());
            }
        }
    }

    public CustomColumnFigure getPrimativeFigure() {
        return primativeFigure;
    }
}
