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
package org.talend.designer.core.ui.editor.nodecontainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeError;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.nodes.NodeProgressBar;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;

/**
 * This element will contain all elements that will be linked to a node.
 *
 * $Id$
 *
 */
public class NodeContainer extends Element {

    private Node node;

    private NodeLabel nodeLabel;

    private NodeError nodeError;

    private NodeProgressBar nodeProgressBar;

    protected List<IElement> elements = new ArrayList<IElement>();

    private SubjobContainer subjobContainer;

    private Point breakpointLocation = new Point();

    private Point collapseLocation = new Point();

    private Point warningLocation = new Point();

    private Point infoLocation = new Point();

    private Point errorLocation = new Point();

    private Point parallelLocation = new Point();

    private Point windowLocation = new Point();

    private Point markLocation = new Point();

    private Point errorMarkLocation = new Point();

    private Point validationRuleLocation = new Point();

    private Dimension breakpointSize;

    private Dimension errorSize;

    private Dimension warningSize;

    private Dimension infoSize;

    private Dimension parallelSize;

    private Dimension windowSize;

    private Dimension errorMarkSize;

    private Dimension validationRuleSize;

    private Rectangle errorMarkRectangle;

    private Rectangle errorRectangle;

    protected Set<IConnection> outputs = new HashSet<IConnection>();

    protected Set<IConnection> inputs = new HashSet<IConnection>();

    public NodeContainer(Node node) {
        this.node = node;
        node.setNodeContainer(this);
        this.nodeLabel = node.getNodeLabel();
        this.nodeError = node.getNodeError();
        this.nodeProgressBar = node.getNodeProgressBar();
        elements.add(node);
        elements.add(nodeLabel);
        elements.add(nodeError);
        if (node.isFileScaleComponent()) {
            elements.add(nodeProgressBar);
        }

        if (!CommonsPlugin.isHeadless()) {
            Image image = ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(NodeContainerFigure.BREAKPOINT_IMAGE));
            breakpointSize = new Dimension(image.getImageData().width, image.getImageData().height);
            image = ImageProvider.getImage(EImage.ERROR_SMALL);
            errorSize = new Dimension(image.getImageData().width, image.getImageData().height);
            image = ImageProvider.getImage(EImage.WARNING_SMALL);
            warningSize = new Dimension(image.getImageData().width, image.getImageData().height);
            image = ImageProvider.getImage(EImage.INFORMATION_SMALL);
            infoSize = new Dimension(image.getImageData().width, image.getImageData().height);
            image = ImageProvider.getImage(EImage.PARALLEL_EXECUTION);
            parallelSize = new Dimension(image.getImageData().width, image.getImageData().height);
            image = ImageProvider.getImage(EImage.WINDOW);
            windowSize = new Dimension(image.getImageData().width, image.getImageData().height);
            image = ImageProvider.getImage(EImage.Error_Mark);
            errorMarkSize = new Dimension(image.getImageData().width, image.getImageData().height);
            image = ImageProvider.getImage(EImage.LOCK_ICON);
            validationRuleSize = new Dimension(image.getImageData().width, image.getImageData().height);
        }

        ElementParameter param = new ElementParameter(this);
        param.setName(EParameterName.COLLAPSED.getName());
        param.setValue(Boolean.TRUE);
        param.setDisplayName(EParameterName.COLLAPSED.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(1);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(false);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.SHOW_SUBJOB_TITLE.getName());
        param.setValue(Boolean.TRUE);
        param.setDisplayName(EParameterName.SHOW_SUBJOB_TITLE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(2);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.SUBJOB_TITLE.getName());
        param.setValue(node.getLabel());
        param.setDisplayName(EParameterName.SUBJOB_TITLE.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(3);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShowIf(EParameterName.SHOW_SUBJOB_TITLE.getName() + " == 'true'"); //$NON-NLS-1$
        addElementParameter(param);
    }

    private Rectangle prepareStatus(Point nodeLocation, Dimension nodeSize) {
        Rectangle statusRectangle = null;
        Rectangle breakpointRectangle, warningRectangle, infoRectangle, parallelLocationRectangle, windowLocationRectangle, validationRuleRectangle, collapseRectangle;

        int status = node.getStatus();

        if ((status & Process.BREAKPOINT_STATUS) != 0) {
            breakpointLocation.x = nodeLocation.x - breakpointSize.width;
            breakpointLocation.y = nodeLocation.y - breakpointSize.height;
            breakpointRectangle = new Rectangle(breakpointLocation, breakpointSize);
            statusRectangle = breakpointRectangle;
        }

        if ((status & Process.ERROR_STATUS) != 0) {
            errorLocation.x = nodeLocation.x + nodeSize.width;
            errorLocation.y = nodeLocation.y - errorSize.height;
            errorRectangle = new Rectangle(errorLocation, errorSize);
            if (statusRectangle == null) {
                statusRectangle = errorRectangle;
            } else {
                statusRectangle.union(errorRectangle);
            }
        }

        if ((status & Process.WARNING_STATUS) != 0) {
            warningLocation.x = nodeLocation.x + nodeSize.width;
            warningLocation.y = nodeLocation.y - warningSize.height;
            warningRectangle = new Rectangle(warningLocation, warningSize);
            if (statusRectangle == null) {
                statusRectangle = warningRectangle;
            } else {
                statusRectangle.union(warningRectangle);
            }
        }

        if (this instanceof AbstractJobletContainer) {
            Dimension collapseSize = new Dimension(20, 20);
            collapseLocation.x = nodeLocation.x - 20;
            collapseLocation.y = nodeLocation.y - collapseSize.height;
            collapseRectangle = new Rectangle(collapseLocation, collapseSize);
            if (statusRectangle == null) {
                Point extendPoint = new Point();
                extendPoint.x = nodeLocation.x + nodeSize.width;
                extendPoint.y = nodeLocation.y - warningSize.height;
                Rectangle extendRectangle = new Rectangle(extendPoint, warningSize);
                statusRectangle = extendRectangle;
            }
            statusRectangle.union(collapseRectangle);
        }

        if ((status & Process.INFO_STATUS) != 0) {
            infoLocation.x = nodeLocation.x + nodeSize.width;
            infoLocation.y = nodeLocation.y - infoSize.height;
            infoRectangle = new Rectangle(infoLocation, infoSize);
            if (statusRectangle == null) {
                statusRectangle = infoRectangle;
            } else {
                statusRectangle.union(infoRectangle);
            }
        }

        if ((status & Process.VALIDATION_RULE_STATUS) != 0) {
            validationRuleLocation.x = nodeLocation.x + nodeSize.width / 2 + nodeSize.width / 4;
            validationRuleLocation.y = nodeLocation.y - validationRuleSize.height / 2;
            validationRuleRectangle = new Rectangle(validationRuleLocation, validationRuleSize);
            if (statusRectangle == null) {
                statusRectangle = validationRuleRectangle;
            } else {
                statusRectangle.union(validationRuleRectangle);
            }
        }

        if (node.isErrorFlag()) {
            markLocation.x = nodeLocation.x - nodeSize.width / 2;
            markLocation.y = nodeLocation.y;

            errorMarkLocation.x = nodeLocation.x - (errorMarkSize.width - nodeSize.width) / 2;
            errorMarkLocation.y = markLocation.y - errorMarkSize.height;
            errorMarkRectangle = new Rectangle(errorMarkLocation, errorMarkSize);

            if (statusRectangle == null) {
                statusRectangle = errorMarkRectangle;
            } else {
                statusRectangle.union(errorMarkRectangle);
            }
        }

        boolean parallelize = false;
        IElementParameter enableParallelizeParameter = node.getElementParameter(EParameterName.PARALLELIZE.getName());
        if (enableParallelizeParameter != null) {
            parallelize = (Boolean) enableParallelizeParameter.getValue();
        }

        if (parallelize) {
            parallelLocation.x = nodeLocation.x - parallelSize.width;
            parallelLocation.y = nodeLocation.y - parallelSize.height;
            parallelLocationRectangle = new Rectangle(parallelLocation, parallelSize);

            if (statusRectangle == null) {
                statusRectangle = parallelLocationRectangle;
            } else {
                statusRectangle.union(parallelLocationRectangle);
            }
        }

        IElementParameter window = node.getElementParameter(EParameterName.WINDOW_DURATION.getName());
        String windowDuration = null;
        if (window != null) {
            windowDuration = (String) window.getValue();
        }

        if (windowDuration != null) {
            windowLocation.x = nodeLocation.x
                    - ((nodeLocation.x + (windowSize.width / 2)) - (nodeLocation.x + (nodeSize.width / 2)));
            windowLocation.y = nodeLocation.y - windowSize.height;
            windowLocationRectangle = new Rectangle(windowLocation, windowSize);

            if (statusRectangle == null) {
                statusRectangle = windowLocationRectangle;
            } else {
                statusRectangle.union(windowLocationRectangle);
            }
        }

        return statusRectangle;
    }

    private Rectangle prepareCleanStatus(Point nodeLocation, Dimension nodeSize) {
        Rectangle statusRectangle = null;
        Rectangle breakpointRectangle, warningRectangle, infoRectangle, validationRuleRectangle, collapseRectangle;

        int status = node.getStatus();

        if ((status & Process.BREAKPOINT_STATUS) != 0) {
            breakpointLocation.x = nodeLocation.x - breakpointSize.width;
            breakpointLocation.y = nodeLocation.y - breakpointSize.height;
            breakpointRectangle = new Rectangle(breakpointLocation, breakpointSize);
            statusRectangle = breakpointRectangle;
        }

        if ((status & Process.ERROR_STATUS) != 0) {
            errorLocation.x = nodeLocation.x + nodeSize.width;
            errorLocation.y = nodeLocation.y - errorSize.height;
            errorRectangle = new Rectangle(errorLocation, errorSize);
            if (statusRectangle == null) {
                statusRectangle = errorRectangle;
            } else {
                statusRectangle.union(errorRectangle);
            }
        }

        if ((status & Process.WARNING_STATUS) != 0) {
            warningLocation.x = nodeLocation.x + nodeSize.width;
            warningLocation.y = nodeLocation.y - warningSize.height;
            warningRectangle = new Rectangle(warningLocation, warningSize);
            if (statusRectangle == null) {
                statusRectangle = warningRectangle;
            } else {
                statusRectangle.union(warningRectangle);
            }
        }

        if (this instanceof AbstractJobletContainer) {
            Dimension collapseSize = new Dimension(20, 20);
            collapseLocation.x = nodeLocation.x - 20;
            collapseLocation.y = nodeLocation.y - collapseSize.height;
            collapseRectangle = new Rectangle(collapseLocation, collapseSize);
            if (statusRectangle == null) {
                Point extendPoint = new Point();
                extendPoint.x = nodeLocation.x + nodeSize.width;
                extendPoint.y = nodeLocation.y - warningSize.height;
                Rectangle extendRectangle = new Rectangle(extendPoint, warningSize);
                statusRectangle = extendRectangle;
            }
            statusRectangle.union(collapseRectangle);
        }

        if ((status & Process.INFO_STATUS) != 0) {
            infoLocation.x = nodeLocation.x + nodeSize.width;
            infoLocation.y = nodeLocation.y - infoSize.height;
            infoRectangle = new Rectangle(infoLocation, infoSize);
            if (statusRectangle == null) {
                statusRectangle = infoRectangle;
            } else {
                statusRectangle.union(infoRectangle);
            }
        }

        if ((status & Process.VALIDATION_RULE_STATUS) != 0) {
            validationRuleLocation.x = nodeLocation.x + nodeSize.width / 2 + nodeSize.width / 4;
            validationRuleLocation.y = nodeLocation.y - validationRuleSize.height / 2;
            validationRuleRectangle = new Rectangle(validationRuleLocation, validationRuleSize);
            if (statusRectangle == null) {
                statusRectangle = validationRuleRectangle;
            } else {
                statusRectangle.union(validationRuleRectangle);
            }
        }

        if (node.isErrorFlag()) {
            markLocation.x = nodeLocation.x - nodeSize.width / 2;
            markLocation.y = nodeLocation.y;
            errorMarkLocation.x = nodeLocation.x - (errorMarkSize.width - nodeSize.width) / 2;
            errorMarkLocation.y = markLocation.y - errorMarkSize.height;
            errorMarkRectangle = new Rectangle(errorMarkLocation, errorMarkSize);

            if (statusRectangle == null) {
                statusRectangle = errorMarkRectangle;
            } else {
                statusRectangle.union(errorMarkRectangle);
            }
        }
        return statusRectangle;
    }

    public Rectangle getNodeContainerRectangle() {
        Point nodeLocation;
        nodeLocation = node.getLocation();

        Dimension nodeSize;
        Dimension labelSize;
        Dimension errorNodeSize;
        nodeSize = node.getSize();
        Rectangle nodeRectangle = new Rectangle(nodeLocation, nodeSize);

        Rectangle statusRectangle = prepareStatus(nodeLocation, nodeSize);

        Point labelLocation = nodeLabel.getLocation().getCopy();
        Point offset = nodeLabel.getOffset();
        Point textOffset = new Point();
        labelSize = nodeLabel.getLabelSize();
        textOffset.y = nodeSize.height;
        textOffset.x = (nodeSize.width - labelSize.width) / 2;
        nodeLabel.setTextOffset(textOffset);
        labelLocation.translate(offset);
        labelLocation.translate(textOffset);
        Rectangle labelRectangle = new Rectangle(labelLocation, labelSize);

        errorNodeSize = nodeError.getErrorSize();

        Point progressLocation = nodeProgressBar.getLocation().getCopy();
        Rectangle progressNodeRectangle = new Rectangle(progressLocation, new Dimension(80, 80));

        Rectangle finalRect;
        finalRect = nodeRectangle.getUnion(labelRectangle).getUnion(statusRectangle);

        if (node.isErrorFlag()) {
            finalRect.height += errorNodeSize.height;
            finalRect.height += errorMarkSize.height;
        }
        if (node.isFileScaleComponent()) {
            finalRect = finalRect.getUnion(progressNodeRectangle);// finalRect.height += progressNodeSize.height;//
        }
        return finalRect;
    }

    public Rectangle getNodeMarkRectangle() {
        Point nodeLocation;
        nodeLocation = node.getLocation();

        Dimension nodeSize;
        Dimension labelSize;
        Dimension errorNodeSize;
        Dimension progressNodeSize;
        nodeSize = node.getSize();
        Rectangle nodeRectangle = new Rectangle(nodeLocation, nodeSize);

        Rectangle statusRectangle = prepareCleanStatus(nodeLocation, nodeSize);

        labelSize = nodeLabel.getLabelSize();

        errorNodeSize = nodeError.getErrorSize();

        progressNodeSize = nodeProgressBar.getProgressSize();

        Rectangle finalRect;
        finalRect = nodeRectangle.getUnion(statusRectangle);
        finalRect.height += labelSize.height / 2;
        if (node.isErrorFlag()) {
            finalRect.height += errorNodeSize.height;
        }
        if (node.isFileScaleComponent()) {
            finalRect.height += progressNodeSize.height;
        }
        return finalRect;
    }

    public Rectangle getErrorMarkRectangle() {
        return errorMarkRectangle;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return node.getUniqueName();
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public NodeLabel getNodeLabel() {
        return this.nodeLabel;
    }

    public void setNodeLabel(NodeLabel nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public NodeError getNodeError() {
        return this.nodeError;
    }

    public void setNodeError(NodeError nodeError) {
        this.nodeError = nodeError;
    }

    public NodeProgressBar getNodeProgressBar() {
        return this.nodeProgressBar;
    }

    public void setNodeProgressBar(NodeProgressBar nodeProgressBar) {
        this.nodeProgressBar = nodeProgressBar;
    }

    public List getElements() {
        return elements;
    }

    @Override
    public boolean isReadOnly() {
        if (node.getJobletNode() != null) {
            return node.isReadOnly();
        }
        return node.getProcess().isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
    }

    /**
     * Getter for breakpointLocation.
     *
     * @return the breakpointLocation
     */
    public Point getBreakpointLocation() {
        return this.breakpointLocation;
    }

    /**
     * Getter for warningLocation.
     *
     * @return the warningLocation
     */
    public Point getWarningLocation() {
        return this.warningLocation;
    }

    /**
     * Getter for collapseLocation.
     *
     * @return the collapseLocation
     */
    public Point getCollapseLocation() {
        return this.collapseLocation;
    }

    /**
     * Getter for warningLocation.
     *
     * @return the warningLocation
     */
    public Point getInfoLocation() {
        return this.infoLocation;
    }

    /**
     * Getter for errorMarkLocation.
     *
     * @return the errorMarkLocation
     */
    public Point getErrorMarkLocation() {
        return this.errorMarkLocation;
    }

    /**
     * Getter for parallelLocation.
     *
     * @return the parallelLocation
     */
    public Point getParallelLocation() {
        return this.parallelLocation;
    }

    /**
     * Getter for windowLocation.
     *
     * @return the windowLocation
     */
    public Point getWindowLocation() {
        return this.windowLocation;
    }

    /**
     * Getter for errorLocation.
     *
     * @return the errorLocation
     */
    public Point getErrorLocation() {
        return this.errorLocation;
    }

    /**
     * Getter for markLocation.
     *
     * @return the markLocation
     */
    public Point getMarkLocation() {
        return this.markLocation;
    }

    /**
     * Getter for markLocation validationRuleLocation.
     *
     * @return
     */
    public Point getValidationRuleLocation() {
        return this.validationRuleLocation;
    }

    /**
     * Getter for subjobContainer.
     *
     * @return the subjobContainer
     */
    public SubjobContainer getSubjobContainer() {
        return this.subjobContainer;
    }

    /**
     * Sets the subjobContainer.
     *
     * @param subjobContainer the subjobContainer to set
     */
    public void setSubjobContainer(SubjobContainer subjobContainer) {
        this.subjobContainer = subjobContainer;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NodeContainer{" + node.toString() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Getter for collapsed.
     *
     * @return the collapsed
     */
    public boolean isCollapsed() {
        IElementParameter param = this.getElementParameter(EParameterName.COLLAPSED.getName());
        if (param == null) {
            return false;
        }
        return (Boolean) getPropertyValue(EParameterName.COLLAPSED.getName());
    }

    public Set<IConnection> getOutputs() {
        return outputs;
    }

    public Set<IConnection> getInputs() {
        return inputs;
    }

    public void setInputs(Set<IConnection> inputs) {
        this.inputs = inputs;
    }

    public void setOutputs(Set<IConnection> outputs) {
        this.outputs = outputs;
    }

    public void refreshInConnections(IConnection conn, INode target) {
        for (IConnection iconn : inputs) {
            if (!iconn.getTarget().getUniqueName().equals(target.getUniqueName())) {
                inputs.remove(iconn);
            }
        }
        inputs.add(conn);
    }

    public void refreshOutConnections(IConnection conn, INode source) {
        for (IConnection iconn : outputs) {
            if (!iconn.getSource().getUniqueName().equals(source.getUniqueName())) {
                outputs.remove(iconn);
            }
        }
        outputs.add(conn);
    }

    public Rectangle getErrorRectangle() {
        return this.errorRectangle;
    }
}
