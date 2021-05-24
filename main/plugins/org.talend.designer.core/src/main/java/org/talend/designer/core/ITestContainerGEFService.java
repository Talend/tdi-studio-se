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
package org.talend.designer.core;

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.talend.core.IService;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;

/**
 * created by hwang on Jan 12, 2015 Detailled comment
 *
 */
public interface ITestContainerGEFService extends IService {

    public EditPart createEditorPart(Object model);

    public Element getJunitContainer(IProcess2 process);

    public NodeContainer createJunitContainer(Node node);

    public void setTestNodes(Map<IGraphicalNode, IGraphicalNode> nodeMap, Map<SubjobContainer, List<Node>> junitGroup,
            List<NodeContainer> nodeCons);

    public Rectangle getJunitContainerRectangle(NodeContainer nodeContainer);

    public boolean isTestContainer(IProcess2 process);

    public Map<SubjobContainer, List<Node>> caculateJunitGroup(List<NodePart> nodeParts);

    public IProcess2 getTestContainerProcess(Item item);

    public boolean isJunitContainer(NodeContainer nodeContainer);

}
