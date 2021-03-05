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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.service.IDesignerMapperService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionFigure;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * wzhang class global comment. Detailled comment
 */
public class CreateComponentOnLinkHelper {

    public static void setupTMap(Node node) {
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerMapperService.class)) {
            return;
        }
        IConnection inputConnection = node.getIncomingConnections().get(0);
        IConnection outputConnection = node.getOutgoingConnections().get(0);
        if (outputConnection.getMetadataTable() != null && inputConnection.getMetadataTable() != null) {
            outputConnection.getMetadataTable().setListColumns(inputConnection.getMetadataTable().clone(false).getListColumns());
            ((Process) node.getProcess()).checkProcess();
        }
        if ("tMap".equals(node.getComponent().getName())) {
            IDesignerMapperService service = (IDesignerMapperService) GlobalServiceRegister.getDefault().getService(
                    IDesignerMapperService.class);
            service.createAutoMappedNode(node, inputConnection, outputConnection);
        }

    }

    public static void updateTMap(INode node, IConnection oldConnection, IConnection newConnection) {
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerMapperService.class)) {
            return;
        }
        IDesignerMapperService service = (IDesignerMapperService) GlobalServiceRegister.getDefault().getService(
                IDesignerMapperService.class);
        service.updateLink(node, oldConnection, newConnection);
    }

    public static void selectConnection(Connection connection, SubjobContainerPart containerPart) {
        List children = containerPart.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Object object = children.get(i);
            if (object instanceof NodeContainerPart) {
                NodeContainerPart nodeContainerPart = (NodeContainerPart) object;
                Object nodePart = nodeContainerPart.getChildren().get(0);
                if (nodePart instanceof NodePart) {
                    List sourceConnections = ((NodePart) nodePart).getSourceConnections();
                    for (int j = 0; j < sourceConnections.size(); j++) {
                        Object connectionPart = sourceConnections.get(j);
                        if (connectionPart instanceof ConnectionPart) {
                            ConnectionPart connPart = (ConnectionPart) connectionPart;
                            org.talend.designer.core.ui.editor.connections.Connection conn = (org.talend.designer.core.ui.editor.connections.Connection) connPart
                                    .getModel();
                            if (conn.equals(connection)) {
                                ConnectionFigure fig = (ConnectionFigure) connPart.getFigure();
                                if (fig.getLineWidth() != 2) {
                                    fig.setLineWidth(2);
                                    connPart.refresh();
                                }
                            } else {
                                ConnectionFigure fig = (ConnectionFigure) connPart.getFigure();
                                if (fig.getLineWidth() != 1) {
                                    fig.setLineWidth(1);
                                    connPart.refresh();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void unselectAllConnections(SubjobContainerPart containerPart) {
        List children = containerPart.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Object object = children.get(i);
            if (object instanceof NodeContainerPart) {
                NodeContainerPart nodeContainerPart = (NodeContainerPart) object;
                Object nodePart = nodeContainerPart.getChildren().get(0);
                if (nodePart instanceof NodePart) {
                    List sourceConnections = ((NodePart) nodePart).getSourceConnections();
                    for (int j = 0; j < sourceConnections.size(); j++) {
                        Object connectionPart = sourceConnections.get(j);
                        if (connectionPart instanceof ConnectionPart) {
                            ConnectionPart connPart = (ConnectionPart) connectionPart;
                            org.talend.designer.core.ui.editor.connections.Connection conn = (org.talend.designer.core.ui.editor.connections.Connection) connPart
                                    .getModel();
                            ConnectionFigure fig = (ConnectionFigure) connPart.getFigure();
                            if (fig.getLineWidth() != 1) {
                                fig.setLineWidth(1);
                                connPart.refresh();
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<Connection> getConnection(SubjobContainerPart containerPart, Point point, Node node) {
        List<Connection> connectionList = new ArrayList<Connection>();
        List<Connection> connList = new ArrayList<Connection>();
        List children = containerPart.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Object object = children.get(i);
            if (object instanceof NodeContainerPart) {
                NodeContainerPart nodePart = (NodeContainerPart) object;
                Object o = nodePart.getChildren().get(0);
                if (o instanceof NodePart) {
                    List connections = ((NodePart) o).getSourceConnections();
                    for (int j = 0; j < connections.size(); j++) {
                        Object connection = connections.get(j);
                        if (connection instanceof ConnectionPart) {
                            ConnectionPart connPart = (ConnectionPart) connection;
                            Connection conn = (Connection) connPart.getModel();
                            if (canCreateNodeOnLink(conn, node)) {
                                Rectangle bounds = ((ConnectionPart) connection).getFigure().getBounds();
                                // System.out.println("location:" + point);
                                if (bounds.contains(point)) {
                                    // System.out.println("bounds:" + bounds);
                                    connectionList.add(conn);
                                }
                                if (containsPoint(bounds, point)) {
                                    // System.out.println("contains");
                                    connList.add(conn);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!connectionList.isEmpty()) {
            return connectionList;
        } else {
            return connList;
        }
    }

    /**
     *
     * comment method "containsPoint". this function is different with Rectangle api to avoid problem that can't get
     * link even it's clearly the target in talend studio.
     *
     * @return
     */
    public static boolean containsPoint(Rectangle rec, Point p) {
        if (rec != null && p != null) {
            return p.y >= rec.y && p.y < rec.y + rec.height * 2 && p.x >= rec.x && p.x < rec.x + rec.width * 2;
        }
        return false;
    }

    public static boolean isFlowConnection(Connection connection) {
        if (connection != null) {
            INode source = connection.getSource();
            INodeConnector connctor = connection.getSourceNodeConnector();
            EConnectionType connectionType = source.getConnectorFromName(connctor.getName()).getDefaultConnectionType();
            return connectionType.hasConnectionCategory(EConnectionType.FLOW);
        }
        return false;
    }

    public static boolean canCreateNodeOnLink(org.talend.designer.core.ui.editor.connections.Connection connection, Node node) {
        if (connection != null && node != null) {

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                        .getService(ICamelDesignerCoreService.class);
                if (camelService.isRouteBuilderNode(node)) {
                    return camelService.canCreateNodeOnLink(connection, node);
                }
            }

            INode source = connection.getSource();
            INodeConnector sourceConnector = connection.getSourceNodeConnector();

            INode target = connection.getTarget();
            INodeConnector targetConnector = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
            for (INodeConnector connector : node.getConnectorsFromType(EConnectionType.FLOW_MAIN)) {
                if (connector.getMaxLinkOutput() != 0) {
                    targetConnector = connector;
                    break;
                }
            }
            EConnectionType outputConnectionType = node.getConnectorFromName(targetConnector.getName())
                    .getDefaultConnectionType();

            // only consider Flow connection
            EConnectionType connectionType = source.getConnectorFromName(sourceConnector.getName()).getDefaultConnectionType();
            if (connectionType.hasConnectionCategory(EConnectionType.FLOW)) {
                boolean b1 = ConnectionManager.canConnectToTarget(source, null, node, connectionType, sourceConnector.getName(),
                        null);
                boolean b2 = ConnectionManager.canConnectToSource(source, node, target, outputConnectionType,
                        targetConnector.getName(), null, true);
                return b1 && b2;
            }
        }
        return false;
    }

    public static List<Object> getTargetArgs(Connection connection, Node node) {
        List<Object> connArgs = new ArrayList<Object>();
        IMetadataTable meta = null;
        String connectName = null;
        INodeConnector connector = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
        for (INodeConnector tconnector : node.getConnectorsFromType(EConnectionType.FLOW_MAIN)) {
            if (connector.getMaxLinkOutput() != 0) {
                connector = tconnector;
                break;
            }
        }

        EConnectionType connectType = node.getConnectorFromName(connector.getName()).getDefaultConnectionType();
        if (connectType.hasConnectionCategory(EConnectionType.FLOW)) {
            // String name = "*New Output*" + " (" + connector.getMenuName() + ")";
            if (node.getConnectorFromName(connector.getName()).isMultiSchema()) {
                String tableName = connection.getMetaName();
                meta = connection.getSource().getMetadataTable(tableName);
                if (connector.isMultiSchema()) {
                    if (connection.getTarget().getConnectorFromName(node.getConnectorFromName(connector.getName()).getName())
                            .isMultiSchema()) {
                        // Connection conn = (Connection) connection.getTarget().getIncomingConnections().get(0);
                        // node.getMetadataList().add(conn.getMetadataTable());
                    } else {
                        MetadataTable table = (MetadataTable) connection.getTarget().getMetadataList().get(0).clone();
                        table.setLabel(tableName);
                        node.getMetadataList().add(table);
                    }
                }
            } else {
                if (node.getMetadataList().size() == 0) {
                    meta = null;
                } else {
                    meta = node.getMetadataFromConnector(connector.getName());
                }
                connectName = node.getProcess().generateUniqueConnectionName(Process.DEFAULT_ROW_CONNECTION_NAME);
            }

            if (connectType.equals(EConnectionType.FLOW_MAIN) || connectType.equals(EConnectionType.FLOW_REF)) {
                if (meta == null) {
                    connArgs.add(null);
                } else {
                    connArgs.add(meta.getTableName());
                }
            } else {
                connArgs.add(node.getUniqueName());
            }
            String baseName = node.getConnectionName();
            String fromConnectionName = null;
            if (node.getProcess().checkValidConnectionName(baseName)) {
                fromConnectionName = node.getProcess().generateUniqueConnectionName(baseName);
            }
            if (fromConnectionName != null && connectType.hasConnectionCategory(IConnectionCategory.FLOW)
                    && node.getProcess().checkValidConnectionName(fromConnectionName, false) && !connector.isMultiSchema()) {
                connArgs.add(fromConnectionName);
            } else {
                connArgs.add(connectName);
            }
            connArgs.add(null);
        }

        return connArgs;
    }

    public static List<ConnectionPart> getConnectionParts(ProcessPart processPart, Point point, Node node) {
        List<ConnectionPart> connectionParts = new ArrayList<ConnectionPart>();
        for (Object child : processPart.getChildren()) {
            if (child instanceof SubjobContainerPart) {
                SubjobContainerPart subJobPart = (SubjobContainerPart) child;
                for (Object sChilde : subJobPart.getChildren()) {
                    if (sChilde instanceof NodeContainerPart) {
                        NodeContainerPart nodeCPart = (NodeContainerPart) sChilde;
                        for (Object nChild : nodeCPart.getChildren()) {
                            if (nChild instanceof NodePart) {
                                for (Object conn : ((NodePart) nChild).getTargetConnections()) {
                                    if (conn instanceof ConnectionPart
                                            && canCreateNodeOnLink((Connection) ((ConnectionPart) conn).getModel(), node)) {
                                        ConnectionPart connPart = (ConnectionPart) conn;
                                        // if (connPart.getFigure() != null &&
                                        // connPart.getFigure().getBounds().contains(point)) {
                                        connectionParts.add((ConnectionPart) conn);
                                        // }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        return connectionParts;
    }

    public static void unselectAllConnections(ProcessPart processPart) {
        for (Object child : processPart.getChildren()) {
            if (child instanceof SubjobContainerPart) {
                SubjobContainerPart subJobPart = (SubjobContainerPart) child;
                for (Object sChilde : subJobPart.getChildren()) {
                    if (sChilde instanceof NodeContainerPart) {
                        NodeContainerPart nodeCPart = (NodeContainerPart) sChilde;
                        for (Object nChild : nodeCPart.getChildren()) {
                            if (nChild instanceof NodePart) {
                                for (Object conn : ((NodePart) nChild).getTargetConnections()) {
                                    if (conn instanceof ConnectionPart) {
                                        unselectConnection((ConnectionPart) conn);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    public static void selectConnection(ConnectionPart connPart) {
        ConnectionFigure fig = (ConnectionFigure) connPart.getFigure();
        if (fig.getLineWidth() != 2) {
            fig.setLineWidth(2);
            connPart.refresh();
        }
    }

    public static void unselectConnection(ConnectionPart connPart) {
        ConnectionFigure fig = (ConnectionFigure) connPart.getFigure();
        if (fig.getLineWidth() != 1) {
            fig.setLineWidth(1);
            connPart.refresh();
        }
    }

    public static double getDistanceOrthogonal(double x, double y, PolylineConnection connection, double zoom) {
        double distance = 1000000000;
        double a = 0, b = 0, c = 0;
        PointList points = connection.getPoints();
        for (int i = 0; i < points.size() - 1; i++) {
            Point point1 = points.getPoint(i);
            Point point2 = points.getPoint(i + 1);
            double dis = CreateComponentOnLinkHelper.getDistanceOrthogonal(x, y, point1, point2, zoom);
            if (dis < distance) {
                distance = dis;
            }
        }
        return distance;
    }

    public static double getDistanceOrthogonal(double x, double y, Point point1, Point point2, double zoom) {
        double dis = 0;
        double a, b, c;
        a = lineDis(point1.x * zoom, point1.y * zoom, point2.x * zoom, point2.y * zoom);// distance of point1 to point2
        b = lineDis(point1.x * zoom, point1.y * zoom, x, y); // distance of point1 to x,y
        c = lineDis(point2.x * zoom, point2.y * zoom, x, y);// distance of point2 to x,y
        if (c + b == a) {// on the line of point1-point2
            dis = 0;
            return dis;
        }
        if (c * c >= a * a + b * b) {
            return 1000000000;
        }
        if (b * b >= a * a + c * c) {
            return 1000000000;
        }

        double p = (a + b + c) / 2;
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));
        dis = 2 * s / a;
        return dis;
    }

    private static double lineDis(double x1, double y1, double x2, double y2) {
        double lineLength = 0;
        lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return lineLength;
    }

}
