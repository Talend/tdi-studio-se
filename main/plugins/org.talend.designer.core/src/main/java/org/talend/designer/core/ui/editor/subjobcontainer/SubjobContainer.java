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
package org.talend.designer.core.ui.editor.subjobcontainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ISubjobContainer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ITestContainerGEFService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletUtil;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.utils.DesignerColorUtils;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class SubjobContainer extends Element implements ISubjobContainer {

    public static final String UPDATE_SUBJOB_CONTENT = "UPDATE_SUBJOB_CONTENT"; //$NON-NLS-1$

    public static final String UPDATE_SUBJOB_DATA = "UPDATE_SUBJOB_DATA"; //$NON-NLS-1$

    public static final String UPDATE_SUBJOB_CONNECTIONS = "UPDATE_SUBJOB_CONNECTIONS"; //$NON-NLS-1$

    public static final String UPDATE_SUBJOB_TITLE_COLOR = "UPDATE_SUBJOB_TITLE_COLOR"; //$NON-NLS-1$

    public static final String UPDATE_SUBJOB_DISPLAY = "UPDATE_SUBJOB_DISPLAY"; //$NON-NLS-1$

    protected List<NodeContainer> nodeContainers = new ArrayList<NodeContainer>();

    private IProcess2 process;

    private List<Connection> outputs = new ArrayList<Connection>();

    private Map<String, Point> pointMap = new HashMap<String, Point>();

    public SubjobContainer(IProcess2 process) {
        // if the subjob is in collapse State.
        this.process = process;

        ElementParameter param = new ElementParameter(this);
        param.setName(EParameterName.COLLAPSED.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.COLLAPSED.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(false);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.SHOW_SUBJOB_TITLE.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.SHOW_SUBJOB_TITLE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        // Unique name of the the start linked with this subjob.
        param = new ElementParameter(this);
        param.setName(EParameterName.UNIQUE_NAME.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.UNIQUE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(false);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        // Name of the subjob (title)
        param = new ElementParameter(this);
        param.setName(EParameterName.SUBJOB_TITLE.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.SUBJOB_TITLE.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(3);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShowIf(EParameterName.SHOW_SUBJOB_TITLE.getName() + " == 'true'"); //$NON-NLS-1$
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.SUBJOB_TITLE_COLOR.getName());
        param.setValue(null); // default subjob color
        param.setDisplayName(EParameterName.SUBJOB_TITLE_COLOR.getDisplayName());
        param.setFieldType(EParameterFieldType.COLOR);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShowIf(EParameterName.SHOW_SUBJOB_TITLE.getName() + " == 'true'"); //$NON-NLS-1$
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.SUBJOB_COLOR.getName());
        param.setValue(null); // default subjob color
        param.setDisplayName(EParameterName.SUBJOB_COLOR.getDisplayName());
        param.setFieldType(EParameterFieldType.COLOR);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(4);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.SUBJOB_DISPLAYED.getName());
        param.setValue(Boolean.TRUE);
        param.setDisplayName(EParameterName.SUBJOB_DISPLAYED.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(5);
        param.setRequired(false);
        param.setShow(false);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);
    }

    public void addNodeContainer(NodeContainer nodeContainer) {
        nodeContainers.add(nodeContainer);
        nodeContainer.setSubjobContainer(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return (String) getPropertyValue(EParameterName.SUBJOB_TITLE.getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return process.isReadOnly();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        // TODO Auto-generated method stub

    }

    /**
     * Getter for nodeContainers.
     *
     * @return the nodeContainers
     */
    public List<NodeContainer> getNodeContainers() {
        return this.nodeContainers;
    }

    public boolean deleteNodeContainer(NodeContainer nodeContainer) {
        if (getNodeContainers().contains(nodeContainer)) {
            getNodeContainers().remove(nodeContainer);
            updateSubjobContainer();
            return true;
        }
        return false;
    }

    /**
     * DOC ycbai Comment method "deleteNodeContainer".
     *
     * @param nodeUniqueName
     * @return
     */
    public boolean deleteNodeContainer(String nodeUniqueName) {
        boolean deleted = false;
        if (nodeUniqueName == null) {
            return deleted;
        }
        List<NodeContainer> nContainers = getNodeContainers();
        Iterator<NodeContainer> nodeContainerIter = nContainers.iterator();
        while (nodeContainerIter.hasNext()) {
            NodeContainer nc = nodeContainerIter.next();
            if (nodeUniqueName.equals(nc.getNode().getUniqueName())) {
                nodeContainerIter.remove();
                deleted = true;
            }
        }
        if (deleted) {
            updateSubjobContainer();
        }
        return deleted;
    }

    /**
     * DOC nrousseau Comment method "getSubjobContainerRectangle".
     *
     * @return
     */
    public Rectangle getSubjobContainerRectangle() {
        Rectangle totalRectangle = null;
        boolean collapsed = isCollapsed();
        boolean isTestContainer = false;
        ITestContainerGEFService testContainerService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerGEFService.class)) {
            testContainerService = (ITestContainerGEFService) GlobalServiceRegister.getDefault().getService(
                    ITestContainerGEFService.class);
            if (testContainerService != null) {
                isTestContainer = testContainerService.isTestContainer(this.process);
            }
        }
        // boolean hasJoblet = false;
        for (NodeContainer container : nodeContainers) {
            Rectangle curRect = null;
            if (container instanceof AbstractJobletContainer) {
                curRect = ((AbstractJobletContainer) container).getJobletContainerRectangle();
            } else if (isTestContainer && testContainerService != null) {
                curRect = testContainerService.getJunitContainerRectangle(container);
            } else {
                curRect = container.getNodeContainerRectangle();
            }

            if ((curRect.x + curRect.width) == (container.getNode().getPosX() + container.getNode().getSize().width)) {
                curRect.setSize(curRect.getSize().width + TalendEditor.GRID_SIZE, curRect.getSize().height);
            }
            if ((curRect.y + curRect.height) == (container.getNode().getPosY() + container.getNode().getSize().height)) {
                curRect.setSize(curRect.getSize().width, curRect.getSize().height + TalendEditor.GRID_SIZE);
            }
            if (curRect.y == container.getNode().getPosY()) {
                // means have totally no other status or such on the top of the node in the NodeContainerFigure, then we
                // add one more space on the top of the subjob

                // this could be done only once, but to simplify the calculation, just do for every node
                curRect.setLocation(curRect.getLocation().x, curRect.getLocation().y - TalendEditor.GRID_SIZE);
                curRect.setSize(curRect.getSize().width, curRect.getSize().height + TalendEditor.GRID_SIZE);
            }
            if (curRect.x == container.getNode().getPosX()) {
                // means if the node container figure is just at the border of the size of the component
                // then we add one more space on the left of the component, just to avoid have one subjob background
                // "too small" (too close to component)

                // this could be done only once, but to simplify the calculation, just do for every node
                curRect.setLocation(curRect.getLocation().x - TalendEditor.GRID_SIZE, curRect.getLocation().y);
                curRect.setSize(curRect.getSize().width + TalendEditor.GRID_SIZE, curRect.getSize().height);
            }

            if (collapsed && totalRectangle == null) {
                totalRectangle = curRect.getCopy();
            } else if (!collapsed) {
                if (totalRectangle == null) {
                    totalRectangle = curRect.getCopy();
                } else {
                    totalRectangle = totalRectangle.getUnion(curRect);
                }
            } else if (collapsed && testContainerService != null && testContainerService.isJunitContainer(container)) {
                INode startNode = this.getSubjobStartNode().getJunitNode();
                INode containerNode = container.getNode().getJunitNode();
                if (startNode != null && containerNode != null && startNode == containerNode) {
                    totalRectangle = testContainerService.getJunitContainerRectangle(container).getCopy();
                }
            }

            if (isTestContainer && !collapsed) {
                if (curRect.x == totalRectangle.x) {
                    totalRectangle.setLocation(totalRectangle.getLocation().x - TalendEditor.GRID_SIZE,
                            totalRectangle.getLocation().y);
                    totalRectangle.setSize(totalRectangle.getSize().width + TalendEditor.GRID_SIZE,
                            totalRectangle.getSize().height);
                }
                if (curRect.y == totalRectangle.y) {
                    totalRectangle.setLocation(totalRectangle.getLocation().x, totalRectangle.getLocation().y
                            - TalendEditor.GRID_SIZE);
                    totalRectangle.setSize(totalRectangle.getSize().width, totalRectangle.getSize().height
                            + TalendEditor.GRID_SIZE);
                }
            }

        }

        if (totalRectangle == null) {
            return null;
        }

        Point location = totalRectangle.getLocation();
        Point newLocation = new Point();
        newLocation.x = (location.x / TalendEditor.GRID_SIZE) * TalendEditor.GRID_SIZE;
        newLocation.y = (location.y / TalendEditor.GRID_SIZE) * TalendEditor.GRID_SIZE;
        // bug 5158
        if (newLocation.y <= 0 && location.y < 0) {
            newLocation.y = newLocation.y - TalendEditor.GRID_SIZE;
        }
        if (newLocation.x <= 0 && location.x < 0) {
            newLocation.x = newLocation.x - TalendEditor.GRID_SIZE;
        }
        totalRectangle.setLocation(newLocation);
        Dimension diff = location.getDifference(newLocation);
        Dimension size = totalRectangle.getSize().expand(diff);
        if ((size.height % TalendEditor.GRID_SIZE) == 0) {
            size.height = (size.height / TalendEditor.GRID_SIZE) * TalendEditor.GRID_SIZE;
        } else {
            size.height = ((size.height / TalendEditor.GRID_SIZE) + 1) * TalendEditor.GRID_SIZE;
        }
        if ((size.width % TalendEditor.GRID_SIZE) == 0) {
            size.width = (size.width / TalendEditor.GRID_SIZE) * TalendEditor.GRID_SIZE;
        } else {
            size.width = ((size.width / TalendEditor.GRID_SIZE) + 1) * TalendEditor.GRID_SIZE;
        }
        // if (hasJoblet) {
        // size.width = size.width + TalendEditor.GRID_SIZE;
        // }
        totalRectangle.setSize(size);
        return totalRectangle;
    }

    public void refreshNodesLocation(boolean jobletCollapsed, AbstractJobletContainer nc, int rightChangewidth, int downChangeheight,
            int leftChangewidth, int upChangeheight) {
        JobletUtil util = new JobletUtil();
        Node node = nc.getNode();
        Rectangle jobletRec = new Rectangle(node.getLocation(), node.getSize());
        boolean isJobletSubjob = nc.getSubjobContainer() == this;
        for (NodeContainer container : nodeContainers) {
            if (container.getNode().getUniqueName().equals(nc.getNode().getUniqueName())) {
                continue;
            }

            Point nodePoint = container.getNode().getLocation().getCopy();

            Rectangle jRec = util.getExpandRectangle(jobletRec);
            // Rectangle jNodeConRec = util.getExpandRectangle(nodeConRec);
            // Rectangle pointrec = util.getExpandRectangle(container.getNodeContainerRectangle());
            jRec.width = jRec.width + rightChangewidth + leftChangewidth;
            jRec.height = jRec.height + downChangeheight + upChangeheight;
            // jNodeConRec.width = jNodeConRec.width + changewidth;
            // jNodeConRec.height = jNodeConRec.height + changeheight;
            if (!jobletCollapsed) {

                if (!nc.isUpdate()) {
                    Point origPoint = new Point(nodePoint.x, nodePoint.y);
                    pointMap.put(container.getNode().getUniqueName(), origPoint);
                    if (isJobletSubjob) {
                        if (nodePoint.x > jobletRec.x) {
                            nodePoint.x = nodePoint.x + rightChangewidth;
                        }

                        if (nodePoint.y > jobletRec.y) {
                            nodePoint.y = nodePoint.y + downChangeheight;
                        }

                        if (nodePoint.x < jobletRec.x) {
                            nodePoint.x = nodePoint.x - leftChangewidth;
                        }

                        if (nodePoint.y < jobletRec.y) {
                            nodePoint.y = nodePoint.y - upChangeheight;
                        }
                    } else {
                        Rectangle jobletSubRec = nc.getSubjobContainer().getSubjobContainerRectangle();
                        Rectangle currentRec = this.getSubjobContainerRectangle();

                        if (nodePoint.x > jobletRec.x
                                && !(currentRec.y < jobletSubRec.y
                                        && currentRec.y + currentRec.height <= jobletSubRec.y + upChangeheight
                                                + node.getSize().height || jobletSubRec.y + jobletSubRec.height <= currentRec.y
                                        + downChangeheight + node.getSize().height
                                        && currentRec.y > jobletSubRec.y)) {
                            nodePoint.x = nodePoint.x + rightChangewidth;
                        }

                        if (nodePoint.y > jobletRec.y
                                && !(currentRec.x < jobletSubRec.x
                                        && currentRec.x + currentRec.width <= jobletSubRec.x + leftChangewidth
                                                + node.getSize().width || jobletSubRec.x + jobletSubRec.width <= currentRec.x
                                        + rightChangewidth + node.getSize().width
                                        && currentRec.x > jobletSubRec.x)) {
                            nodePoint.y = nodePoint.y + downChangeheight;
                        }

                        if (nodePoint.x < jobletRec.x
                                && !(currentRec.y < jobletSubRec.y
                                        && currentRec.y + currentRec.height <= jobletSubRec.y + upChangeheight
                                                + node.getSize().height || jobletSubRec.y + jobletSubRec.height <= currentRec.y
                                        + downChangeheight + node.getSize().height
                                        && currentRec.y > jobletSubRec.y)) {
                            nodePoint.x = nodePoint.x - leftChangewidth;
                        }

                        if (nodePoint.y < jobletRec.y
                                && !(currentRec.x < jobletSubRec.x
                                        && currentRec.x + currentRec.width <= jobletSubRec.x + leftChangewidth
                                                + node.getSize().width || jobletSubRec.x + jobletSubRec.width <= currentRec.x
                                        + rightChangewidth + node.getSize().width
                                        && currentRec.x > jobletSubRec.x)) {
                            nodePoint.y = nodePoint.y - upChangeheight;
                        }

                    }
                }

            } else {
                nodePoint = pointMap.get(container.getNode().getUniqueName());
                // if (nodePoint.x > nodeRec.x && nodePoint.x > rightChangewidth) {
                // nodePoint.x = nodePoint.x - rightChangewidth;
                // }
                // if (nodePoint.x < nodeRec.x) {
                // nodePoint.x = nodePoint.x + leftChangewidth;
                // }
                // if (nodePoint.y > nodeRec.y && nodePoint.y > downChangeheight) {
                // if ((nodePoint.y - downChangeheight) > (nodeRec.y + 64)) {
                // nodePoint.y = nodePoint.y - downChangeheight;
                // } else {
                // nodePoint.y = nodeRec.y + 64;
                // }
                //
                // }

            }
            container.getNode().setLocation(nodePoint);
        }
    }

    /**
     * Getter for collapsed.
     *
     * @return the collapsed
     */
    public boolean isCollapsed() {
        return (Boolean) getPropertyValue(EParameterName.COLLAPSED.getName());
    }

    /**
     * Sets the collapsed.
     *
     * @param collapsed the collapsed to set
     */
    public void setCollapsed(boolean collapsed) {
        setPropertyValue(EParameterName.COLLAPSED.getName(), new Boolean(collapsed));
    }

    public Node getSubjobStartNode() {
        String subjobStartUniqueName = (String) getPropertyValue(EParameterName.UNIQUE_NAME.getName());
        if (process != null && (List<Node>) process.getGraphicalNodes() != null) {
            for (Node node : (List<Node>) process.getGraphicalNodes()) {
                if (node.getUniqueName() != null && node.getUniqueName().equals(subjobStartUniqueName)) {
                    return node;
                }
            }
        }
        return null;
    }

    public void setSubjobStartNode(Node node) {
        setPropertyValue(EParameterName.UNIQUE_NAME.getName(), node.getUniqueName());

        if (node.getComponent().getName().equals("tPrejob") || node.getComponent().getName().equals("tPostjob")) { //$NON-NLS-1$ //$NON-NLS-2$
            setPropertyValue(EParameterName.SHOW_SUBJOB_TITLE.getName(), Boolean.TRUE);
            getElementParameter(EParameterName.SHOW_SUBJOB_TITLE.getName()).setShow(false);
        } else {
            getElementParameter(EParameterName.SHOW_SUBJOB_TITLE.getName()).setShow(true);
        }
        setSubjobPropertyColor(EParameterName.SUBJOB_COLOR.getName(), node,
                DesignerColorUtils.getPreferenceSubjobRGB(DesignerColorUtils.SUBJOB_COLOR_NAME, DesignerColorUtils.SUBJOB_COLOR));
        setSubjobPropertyColor(EParameterName.SUBJOB_TITLE_COLOR.getName(), node, DesignerColorUtils.getPreferenceSubjobRGB(
                DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME, DesignerColorUtils.SUBJOB_TITLE_COLOR));

    }

    private void setSubjobPropertyColor(String propertyName, Node node, RGB defaultColor) {
        RGB rgbValue = ColorUtils.parseStringToRGB((String) node.getPropertyValue(propertyName), defaultColor);
        setPropertyValue(propertyName, ColorUtils.getRGBValue(rgbValue));
    }

    @Override
    public void updateSubjobContainer() {
        fireStructureChange(UPDATE_SUBJOB_CONTENT, this);
        refreshOutputConnections();
    }

    /**
     * Getter for process.
     *
     * @return the process
     */
    public IProcess2 getProcess() {
        return this.process;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.Element#setPropertyValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void setPropertyValue(String id, Object value) {
        super.setPropertyValue(id, value);
        if (id.equals(EParameterName.COLLAPSED.getName())) {
            // refreshOutputConnections();
            updateSubjobContainer();
            refreshMRProgressBar();
        } else if (id.equals(EParameterName.SUBJOB_COLOR.getName()) || id.equals(EParameterName.SHOW_SUBJOB_TITLE.getName())
                || id.equals(EParameterName.SUBJOB_TITLE.getName())) {
            fireStructureChange(UPDATE_SUBJOB_DATA, this);
        } else if (id.equals(EParameterName.SUBJOB_TITLE_COLOR.getName())) {
            fireStructureChange(UPDATE_SUBJOB_TITLE_COLOR, this);
        } else if (id.equals(EParameterName.SUBJOB_DISPLAYED.getName())) {
            updateSubjobDisplay();
        }
    }

    /**
     * DOC nrousseau Comment method "refreshOutputConnections".
     */
    private void refreshOutputConnections() {
        boolean collapsed = isCollapsed();
        // reinitialize all output connections.
        Node subjobStartNode = this.getSubjobStartNode();

        List<Connection> connectionsToUpdate = new ArrayList<Connection>(outputs);
        outputs = new ArrayList<Connection>();

        if (!collapsed) {
            fireStructureChange(UPDATE_SUBJOB_CONNECTIONS, this);

            for (NodeContainer nodeContainer : this.nodeContainers) {
                Node currentNode = nodeContainer.getNode();
                // force connections draw update
                currentNode.forceConnectionsUpdate();

                for (Connection connection : outputs) {
                    if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
                        connection.setSubjobConnection(false);
                    }
                }
            }
            return;
        }
        for (NodeContainer nodeContainer : this.nodeContainers) {
            Node currentNode = nodeContainer.getNode();
            if (currentNode.equals(subjobStartNode)) {
                // avoid subjobStartNode as it's not needed
                continue;
            }
            for (Connection connection : (List<Connection>) currentNode.getOutgoingConnections()) {
                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DEPENDENCY)
                        && !subjobStartNode.equals(connection.getTarget().getDesignSubjobStartNode())) {
                    connection.setSubjobConnection(true);
                    outputs.add(connection);
                }
            }
            fireStructureChange(UPDATE_SUBJOB_CONNECTIONS, this);
        }
    }

    private void refreshMRProgressBar() {
        for (NodeContainer nc : this.getNodeContainers()) {
            if (nc instanceof JobletContainer) {
                if (nc.getNode().isMapReduceStart()) {
                    ((JobletContainer) nc).updateState("UPDATE_MR_STATUS", "CLEAR", new Double(0), new Double(0)); //$NON-NLS-1$
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String nodes = ""; //$NON-NLS-1$
        for (NodeContainer nodeContainer : nodeContainers) {
            nodes += nodeContainer.getNode().toString();
            if (nodeContainers.indexOf(nodeContainer) != (nodeContainers.size() - 1)) {
                nodes += ", "; //$NON-NLS-1$
            }
        }
        return "SubjobContainer [" + nodes + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public List<? extends IConnection> getOutgoingConnections() {
        return this.outputs;
    }

    /**
     * DOC nrousseau Comment method "dispose".
     */
    public void dispose() {
        this.nodeContainers.clear();
        nodeContainers = null;
        process = null;
        outputs.clear();
        outputs = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.ISubjobContainer#isDisplayed()
     */
    @Override
    public boolean isDisplayed() {
        if (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.DISPLAY_SUBJOBS)) {
            return false;
        }
        if (!process.isSubjobEnabled()) {
            return false;
        }
        return (Boolean) getPropertyValue(EParameterName.SUBJOB_DISPLAYED.getName());
    }

    public void setDisplayed(Boolean displayed) {
        setPropertyValue(EParameterName.SUBJOB_DISPLAYED.getName(), displayed);
    }

    @Override
    public void updateSubjobDisplay() {
        if (!isDisplayed() && isCollapsed()) {
            // if the subjob hidden and collapsed, remove the collapse status first.
            setCollapsed(false);
        }
        fireStructureChange(UPDATE_SUBJOB_DISPLAY, this);
    }

    public void savePoint(Node node, Point point) {
        pointMap.put(node.getUniqueName(), point);
    }
}
