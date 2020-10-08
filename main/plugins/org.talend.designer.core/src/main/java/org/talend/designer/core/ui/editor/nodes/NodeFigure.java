// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.AnchorListener;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.connections.ConnectionFigure;
import org.talend.designer.core.ui.editor.connections.DummyConnectionFigure;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.utils.ResourceDisposeUtil;

/**
 * This class create a figure with the given image. <br/>
 * eh
 *
 * $Id$
 *
 */
public class NodeFigure extends Figure {

    private final ImageFigure fig;

    private final SimpleHtmlFigure hint;

    private int alpha = -1;

    private final NodeBorder lineBorder = new NodeBorder();

    private Map<ConnectionFigure, DummyConnectionFigure> sourceDummyMap;

    private Map<ConnectionFigure, DummyConnectionFigure> targetDummyMap;

    private boolean dummy;

    public static final Color START_COLOR = new Color(null, new RGB(153, 218, 39));

    private ConnectionFigure targetConnection;

    private DummyConnectionFigure targetDummy;

    private List<ConnectionFigure> newSourceConnections = new ArrayList<ConnectionFigure>();

    private Node node;

    private AnchorListener targetListener = new AnchorListener() {

        @Override
        public void anchorMoved(ConnectionAnchor anchor) {
            updateTarget();
        }

    };

    private Map<ConnectionFigure, AnchorListener> sourceListeners = new HashMap<ConnectionFigure, AnchorListener>();

    public NodeFigure(Node node) {
        this.node = node;
        fig = new ImageFigure();
        fig.setImage(CoreImageProvider.getComponentIcon(node.getComponent(), ICON_SIZE.ICON_32));
        fig.setSize(new Dimension(Node.DEFAULT_SIZE, Node.DEFAULT_SIZE));
        add(fig);

        INodeConnector mainNodeConnector = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
        if (mainNodeConnector != null) {
            sourceDummyMap = new HashMap<ConnectionFigure, DummyConnectionFigure>();
            targetDummyMap = new HashMap<ConnectionFigure, DummyConnectionFigure>();
        }
        this.setSize(node.getSize());
        this.setOpaque(false);
        hint = new SimpleHtmlFigure();
        this.setBorder(lineBorder);

    }

    public void setHint(String hintText) {
        if (hintText.equals("")) { //$NON-NLS-1$
            setToolTip(null);
        } else {
            hint.setText(hintText);
            setToolTip(hint);
        }
    }

    private ConnectionFigure updateSource(ConnectionFigure curConn) {
        DummyConnectionFigure connection = sourceDummyMap.get(curConn);
        if (curConn.getTargetAnchor() == null || curConn.getSourceAnchor() == null) {
            connection.setVisible(false);
            return curConn;
        }
        Point figCenter = new Rectangle(this.node.getLocation(), this.node.getSize()).getCenter();
        curConn.getConnectionRouter().route(curConn);
        Point endPoint = curConn.getStart();
        if (!figCenter.equals(connection.getStart())) {
            connection.setStart(figCenter);
        }
        if (!endPoint.equals(connection.getEnd())) {
            connection.setEnd(endPoint);
        }

        return null;
    }

    private void updateTarget() {
        if (targetConnection == null) {
            return;
        }
        Point figCenter = new Rectangle(this.node.getLocation(), this.node.getSize()).getCenter();
        if (targetConnection.getTargetAnchor() == null) {
            targetDummy.setVisible(false);
        } else {
            targetConnection.getConnectionRouter().route(targetConnection);
            Point startPoint = targetConnection.getEnd();
            if (!startPoint.equals(targetDummy.getStart())) {
                targetDummy.setStart(startPoint);
            }
            if (!figCenter.equals(targetDummy.getEnd())) {
                targetDummy.setEnd(figCenter);
            }
        }
    }

    @Override
    public void setBounds(final Rectangle rect) {
        super.setBounds(rect);

        Point location = (new Point(rect.getCenter()))
                .translate(new Point(-fig.getSize().width / 2, -(fig.getSize().height / 2)));
        Rectangle figBounds = new Rectangle(location, fig.getSize());
        this.fig.setBounds(figBounds);
        if (dummy) {
            if (sourceDummyMap != null) {
                if (targetConnection != null && sourceDummyMap.keySet().size() != 0) {
                    for (final ConnectionFigure curConn : newSourceConnections) {
                        AnchorListener sourceListener = new AnchorListener() {

                            @Override
                            public void anchorMoved(ConnectionAnchor anchor) {
                                if (curConn != null) {
                                    updateSource(curConn);
                                }
                            }
                        };
                        if (curConn.getTargetAnchor() != null && curConn.getTargetAnchor().getOwner() != null) {
                            sourceListeners.put(curConn, sourceListener);
                            curConn.getTargetAnchor().addAnchorListener(sourceListener);
                        }
                    }

                    newSourceConnections.clear();

                    updateTarget();

                    List<ConnectionFigure> toRemove = new ArrayList<ConnectionFigure>();
                    for (ConnectionFigure curConn : sourceDummyMap.keySet()) {
                        ConnectionFigure connToRemove = updateSource(curConn);
                        if (connToRemove != null) {
                            toRemove.add(connToRemove);
                        }
                    }
                    sourceDummyMap.keySet().removeAll(toRemove);

                } else {
                    INodeConnector mainNodeConnector = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
                    ConnectionFigure connection = new ConnectionFigure(
                            mainNodeConnector.getConnectionProperty(EConnectionType.FLOW_MAIN), node);
                    connection.setTargetDecoration(null);
                    connection.setStart(new Point(figBounds.x, figBounds.y + figBounds.height / 2));
                    connection.setEnd(new Point(figBounds.x + figBounds.width, figBounds.y + figBounds.height / 2));
                }
            }
        }
        if (!rect.getSize().equals(fig.getSize())) {
            lineBorder.setUseRectangle(true);
        } else {
            lineBorder.setUseRectangle(false);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (alpha != -1) {
            graphics.setAlpha(alpha);
        } else {
            graphics.setAlpha(255);
        }
        if (DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_ANTIALIASING)) {
            graphics.setInterpolation(SWT.HIGH);
        }
        if (fig.getImage() == null || fig.getImage().isDisposed()) {
            fig.setImage(CoreImageProvider.getComponentIcon(node.getComponent(), ICON_SIZE.ICON_32));
        }
//        fig.setImage(CoreImageProvider.getComponentIcon(node.getComponent(), ICON_SIZE.ICON_32));
//        fig.setBorder(new LineBorder(new Color(Display.getDefault(), new RGB(204, 153, 255))));
        super.paint(graphics);
    }

    public void setDummy(boolean value) {
        dummy = value;
        if (sourceDummyMap != null) {
            if (dummy) {
                for (DummyConnectionFigure connection : sourceDummyMap.values()) {
                    connection.setAlpha(255);
                    connection.setVisible(true);
                }
            } else {
                for (DummyConnectionFigure connection : sourceDummyMap.values()) {
                    connection.setVisible(false);
                }
            }
        }
        if (targetDummy != null) {
            if (dummy) {
                targetDummy.setAlpha(255);
                targetDummy.setVisible(true);
            } else {
                targetDummy.setVisible(false);
            }
        }
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setStart(boolean start) {
        if (start) {
            setBackgroundColor(START_COLOR);
            setOpaque(true);
        } else {
            setOpaque(false);
        }
    }

    /**
     * DOC nrousseau NodeFigure class global comment. Detailled comment <br/>
     *
     */
    class NodeBorder extends AbstractBorder {

        private boolean useRectangle;

        @Override
        public Insets getInsets(IFigure figure) {
            return null;
        }

        @Override
        public void paint(IFigure figure, Graphics g, Insets theInsets) {
            Rectangle r = getPaintRectangle(figure, theInsets);

            if (useRectangle) {
                g.setLineWidth(2);
                g.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
                g.drawRectangle(r);
                g.drawLine(r.x, 1, r.right(), 1);
                g.drawLine(1, r.y, 1, r.bottom());

                g.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
                g.drawLine(r.x, r.bottom() - 1, r.right(), r.bottom() - 1);
                g.drawLine(r.right() - 1, r.y, r.right() - 1, r.bottom());
            }

        }

        public void setUseRectangle(boolean useRectangle) {
            this.useRectangle = useRectangle;
        }

    }

    /**
     * Sets the startConnection.
     *
     * @param startConnection the startConnection to set
     */
    DummyConnectionFigure connection = null;

    public void addSourceConnection(ConnectionFigure sourceConnection) {
        if (!sourceDummyMap.keySet().contains(sourceConnection)) {
            if (connection != null) {
                connection.disposeColors();
            }
            connection = new DummyConnectionFigure(sourceConnection.getConnection(), sourceConnection.getConnectionProperty(),
                    node);
            connection.setTargetDecoration(null);
            add(connection);
            if (dummy) {
                connection.setAlpha(255);
                connection.setVisible(true);
            } else {
                connection.setVisible(false);
            }
            this.sourceDummyMap.put(sourceConnection, connection);
            newSourceConnections.add(sourceConnection);
        }
    }

    /**
     * Sets the endConnection.
     *
     * @param endConnection the endConnection to set
     */
    public void setTargetConnection(ConnectionFigure targetConnection) {
        if (!targetDummyMap.keySet().contains(targetConnection)) {
            if (targetConnection != null && targetConnection.getSourceAnchor() != null) {
                targetConnection.getSourceAnchor().removeAnchorListener(targetListener);
            }
            if (this.targetConnection == null) {
                this.targetConnection = targetConnection;
            } else if (targetConnection.getConnection().getLineStyle() == EConnectionType.FLOW_MAIN) {
                this.targetConnection = targetConnection;
            } else {
                return;
            }

            if (connection != null) {
                connection.disposeColors();
            }
            connection = new DummyConnectionFigure(targetConnection.getConnection(), targetConnection.getConnectionProperty(),
                    node);
            connection.setTargetDecoration(null);
            add(connection);
            if (dummy) {
                connection.setAlpha(255);
                connection.setVisible(true);
            } else {
                connection.setVisible(false);
            }
            targetDummy = connection;

            if (targetConnection.getSourceAnchor() != null && targetConnection.getSourceAnchor().getOwner() != null) {
                targetConnection.getSourceAnchor().addAnchorListener(targetListener);
            }
            targetDummyMap.put(targetConnection, targetDummy);
        }
    }

    public void removeSourceConnection(ConnectionFigure connectionFigure) {
        for (ConnectionFigure curConn : sourceListeners.keySet()) {
            if (connectionFigure.equals(curConn) && curConn.getTargetAnchor() != null) {
                curConn.getTargetAnchor().removeAnchorListener(sourceListeners.get(curConn));

            }
        }
    }

    public void removeTargetConnection(ConnectionFigure connectionFigure) {
        // there can be only one "target" connection
        if (targetConnection != null && targetConnection.getSourceAnchor() != null
                && targetConnection.getSourceAnchor().getOwner() != null) {
            targetConnection.getSourceAnchor().removeAnchorListener(targetListener);
            if (targetConnection != null
                    && connectionFigure.getConnection().getUniqueName().equals(targetConnection.getConnection().getUniqueName())) {
                targetConnection = null;
                targetDummyMap.remove(targetConnection);
            }
        }
    }

    public void disposeColors() {
        if (connection != null) {
            ResourceDisposeUtil.disposeColor(connection.getBackgroundColor());
            ResourceDisposeUtil.disposeColor(connection.getForegroundColor());
        }
        // connection.disposeColors();
        if (targetConnection != null) {
            targetConnection.disposeColors();
        }
        if (targetDummy != null) {
            targetDummy.disposeColors();
        }
    }

    public ImageFigure getImageFigure() {
        return fig;
    }
}
