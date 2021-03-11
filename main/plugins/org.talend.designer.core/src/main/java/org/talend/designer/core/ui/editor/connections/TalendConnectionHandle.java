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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PolygonShape;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.handles.SquareHandle;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.ui.action.TalendCreateConnectionTool;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class TalendConnectionHandle extends SquareHandle implements PropertyChangeListener {

    private NodePart nodePart;

    private INodeConnector mainConnector;

    public TalendConnectionHandle(NodePart nodePart) {
        this.nodePart = nodePart;
        this.setOwner(nodePart);
        setLayoutManager(new StackLayout());
    }

    public void setMainNodeConnector(INodeConnector nodeConnector) {
        this.mainConnector = nodeConnector;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.AbstractHandle#createDragTracker()
     */
    @Override
    protected DragTracker createDragTracker() {
        if (this.mainConnector == null) {
            this.mainConnector = new NodeConnectorTool(nodePart).getConnector();
        }
        if (this.mainConnector == null) {
            return null;
        }

        final List<Object> listArgs = new ArrayList<Object>();
        listArgs.add(null);
        listArgs.add(null);
        listArgs.add(null);
        TalendCreateConnectionTool myConnectTool = new TalendCreateConnectionTool(new CreationFactory() {

            @Override
            public Object getNewObject() {
                return listArgs;
            }

            @Override
            public Object getObjectType() {
                // should get the connector at realtime
                mainConnector = new NodeConnectorTool(nodePart).getConnector();
                if (mainConnector == null) {
                    // if no connector is available, maybe can return "on componnet ok" connector to avoid NPE
                    Node node = (Node) nodePart.getModel();
                    mainConnector = node.getConnectorFromType(EConnectionType.ON_COMPONENT_OK);
                    // specify the connection name
                    listArgs.set(1, mainConnector.getLinkName());
                } else {
                    // no need to specify the connection name
                    listArgs.set(1, null);
                }
                return mainConnector.getName();
            }
        }, nodePart);

        return myConnectTool;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.SquareHandle#init()
     */
    @Override
    protected void init() {
        // TODO Auto-generated method stub
        super.init();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.SquareHandle#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paintFigure(Graphics g) {
        // TODO Auto-generated method stub
        super.paintFigure(g);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.AbstractHandle#ancestorAdded(org.eclipse.draw2d.IFigure)
     */
    @Override
    public void ancestorAdded(IFigure ancestor) {
        // TODO Auto-generated method stub
        super.ancestorAdded(ancestor);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.AbstractHandle#validate()
     */
    @Override
    public void validate() {
        if (isValid()) {
            return;
        }
        removeAll();

        if (this.mainConnector == null) {
            this.mainConnector = new NodeConnectorTool(nodePart).getConnector();
        }
        if (this.mainConnector == null) {
            return;
        }
        Rectangle rec = new Rectangle(0, 0, 12, 12);
        PolygonShape sourceShape = new PolygonShape();
        sourceShape.addPoint(new Point(0, 0));
        sourceShape.addPoint(new Point(9, 0));
        sourceShape.addPoint(new Point(11, 4.5));
        sourceShape.addPoint(new Point(11, 6.5));
        sourceShape.addPoint(new Point(9, 11));
        sourceShape.addPoint(new Point(0, 11));

        sourceShape.setFill(true);
        sourceShape.setPreferredSize(rec.getSize());
        if (mainConnector != null) {
            RGB shapeColor = this.mainConnector.getConnectionProperty(this.mainConnector.getDefaultConnectionType()).getRGB();
            sourceShape.setForegroundColor(ColorUtils.getCacheColor(shapeColor));
            sourceShape.setBackgroundColor(ColorUtils.getCacheColor(shapeColor));
        }

        sourceShape.setBounds(rec);
        sourceShape.setLayoutManager(new ToolbarLayout());

        add(sourceShape);
        setSize(sourceShape.getPreferredSize());

        super.validate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.AbstractHandle#setLocator(org.eclipse.draw2d.Locator)
     */
    @Override
    public void setLocator(Locator locator) {
        super.setLocator(locator);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.ConnectionHandle#addNotify()
     */
    @Override
    public void addNotify() {
        super.addNotify();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#findFigureAt(int, int, org.eclipse.draw2d.TreeSearch)
     */
    @Override
    public IFigure findFigureAt(int x, int y, TreeSearch search) {
        // return the ConnectionHandle and not the children figures
        if (containsPoint(x, y)) {
            return this;
        }
        return super.findFigureAt(x, y, search);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.SquareHandle#getFillColor()
     */
    @Override
    protected Color getFillColor() {
        return (isPrimary()) ? ColorUtils.getCacheColor(new RGB(191, 218, 115)) : ColorConstants.white;
    }

}
