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
package org.talend.designer.core.ui.editor.jobletcontainer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public abstract class AbstractJobletContainer extends NodeContainer{

    public static final String UPDATE_JOBLET_CONTENT = "UPDATE_JOBLET_CONTENT"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_DATA = "UPDATE_JOBLET_DATA"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_CONNECTIONS = "UPDATE_JOBLET_CONNECTIONS"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_TITLE_COLOR = "UPDATE_JOBLET_TITLE_COLOR"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_DISPLAY = "UPDATE_JOBLET_DISPLAY"; //$NON-NLS-1$

    protected List<NodeContainer> nodeContainers = new ArrayList<NodeContainer>();

    protected List<Node> nodes = new ArrayList<Node>();

    protected boolean update = false;

    protected boolean needchangeLock = true;

    /**
     * DOC hwang AbstractJobletContaienr constructor comment.
     * @param node
     */
    public AbstractJobletContainer(Node node) {
        super(node);
    }

    public List<NodeContainer> getNodeContainers() {
        return this.nodeContainers;
    }

    public abstract Rectangle getJobletContainerRectangle();

    public abstract boolean isUpdate();

    public abstract void setCollapsed(boolean collapsed);

    public abstract void setNeedchangeLock(boolean needchangeLock);

    public abstract void transferLocation(Point oldPos);

    public abstract void updateJobletNodes(boolean update);

}
