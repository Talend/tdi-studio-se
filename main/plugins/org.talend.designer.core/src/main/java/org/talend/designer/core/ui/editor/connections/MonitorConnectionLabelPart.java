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
package org.talend.designer.core.ui.editor.connections;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.process.Element;

/**
 * DOC YeXiaowei class global comment. Detailled comment
 */
public class MonitorConnectionLabelPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((Element) getModel()).addPropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((Element) getModel()).removePropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    public IFigure createFigure() {
        String text = ((MonitorConnectionLabel) getModel()).getLabelText();
        Label label = new Label();
        label.setText(text);
        return label;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        refreshVisuals();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        String text = ((MonitorConnectionLabel) getModel()).getLabelText();
        Label figure = (Label) getFigure();
        // figure.setIcon(null); //TODO add icon to it
        figure.setText(text);
        figure.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));

        ConnectionPart parent = (ConnectionPart) getParent();
        PolylineConnection connFigure = (PolylineConnection) parent.getFigure();
        MonitorConnectionLabelConstraint constraint = new MonitorConnectionLabelConstraint(text, "center", null, connFigure); //$NON-NLS-1$
        parent.setLayoutConstraint(this, getFigure(), constraint);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
     */
    @Override
    public void performRequest(Request request) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#setSelected(int)
     */
    @Override
    public void setSelected(int value) {
        super.setSelected(value);
        if (this.getParent().getSelected() != value) {
            this.getParent().setSelected(value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {

    }

}
