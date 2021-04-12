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
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.talend.sqlbuilder.erdiagram.ui.editor.ErDiagramRootEditPart;
import org.talend.sqlbuilder.erdiagram.ui.nodes.ErDiagram;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ErDiagramPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    private ErDiagramLayoutEditPolicy diagramLayoutEditPolicy;

    private FreeformLayer fig2;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        return ((ErDiagram) this.getModel()).getTables();
    }

    public void caculateTablesLocation() {
        List tables = getModelChildren();
        for (Iterator it = tables.iterator(); it.hasNext();) {
            Table table = (Table) it.next();
            Dimension tableSize = table.getSize();
            table.setLocation(tableSize.width + 20, table.getLocation().y);
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
        ((ErDiagram) this.getModel()).addPropertyChangeListener(this);
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
        ((ErDiagram) this.getModel()).removePropertyChangeListener(this);
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
        getLayer(ErDiagramRootEditPart.PROCESS_BACKGROUND_LAYER).add(fig2);
        return figure;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        diagramLayoutEditPolicy = new ErDiagramLayoutEditPolicy();
        installEditPolicy(EditPolicy.LAYOUT_ROLE, diagramLayoutEditPolicy);
        installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        refreshChildren();
        ErDiagram model2 = (ErDiagram) getModel();
        if (evt.getPropertyName().equals(ErDiagram.PROP_ISDIRTY)) {
            model2.updateSqlText();
        } else {
            (model2).setDirty(true);
        }
    }

}
