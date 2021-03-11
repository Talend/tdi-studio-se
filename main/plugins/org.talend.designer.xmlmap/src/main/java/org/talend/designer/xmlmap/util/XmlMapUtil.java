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
package org.talend.designer.xmlmap.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.designer.xmlmap.i18n.Messages;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.IConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.INodeConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.ui.expressionutil.TableEntryLocation;
import org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapUtil {

    public static final String DOCUMENT = "id_Document";

    public static final String XPATH_SEPARATOR = "/";

    public static final String EXPRESSION_SEPARATOR = ".";

    public static final String DOUBLE_ESCAPE = "\\"; //$NON-NLS-1$

    public static final String EXPRESSION_SEPARATOR_SPLIT = DOUBLE_ESCAPE + EXPRESSION_SEPARATOR;

    public static final String DEFAULT_DATA_TYPE = "id_String";

    public static final String CHILDREN_SEPARATOR = ":/";

    public static final String EXPRESSION_LEFT = "[";

    public static final String EXPRESSION_RIGHT = "]";

    public static final String XPATH_ATTRIBUTE = "@";

    public static final String XPATH_NAMESPACE = "xmlns:";

    public static final int DEFAULT_OFFSET = 5;

    public static final int DEFAULT_OFFSET_FILTER = 7;

    public static final String DEFAULT_NAME_SPACE_PREFIX = "(default)";

    public static final String VAR_TABLE_NAME = "Var";

    /**
     *
     * DOC talend Comment method "getXPathLength".
     *
     * @param xPath
     * @return if return >2 , TreeNode is a child of document node.
     */
    public static int getXPathLength(String xPath) {
        if (xPath == null) {
            return 0;
        }

        if (xPath.indexOf(CHILDREN_SEPARATOR) != -1) {

            String childPath = xPath.substring(xPath.indexOf(CHILDREN_SEPARATOR) + 2, xPath.length());
            return 2 + childPath.split(XPATH_SEPARATOR).length;

        } else {
            return xPath.split(XPATH_SEPARATOR).length;
        }

    }

    public static String getXPath(String parentPath, String label, NodeType nodeType) {
        if (parentPath == null || label == null) {
            throw new IllegalArgumentException("Invalid xpath");
        }
        String newXPath = "";
        String type = "";
        if (NodeType.ATTRIBUT.equals(nodeType)) {
            type = XPATH_ATTRIBUTE;
        } else if (NodeType.NAME_SPACE.equals(nodeType)) {
            type = XPATH_NAMESPACE;
        }
        // parentPath is tree xpath
        if (parentPath.indexOf(CHILDREN_SEPARATOR) != -1) {
            String[] split = parentPath.split(CHILDREN_SEPARATOR);
            if (split.length != 2) {
                throw new IllegalArgumentException("Invalid xpath");
            }

            newXPath = parentPath + XPATH_SEPARATOR + type + label;

        }
        // parentPath is normal column xpath
        else {
            if (parentPath.indexOf(XPATH_SEPARATOR) == -1) {
                newXPath = parentPath + XPATH_SEPARATOR + label;
            } else if (parentPath.split(XPATH_SEPARATOR).length == 2) {
                newXPath = parentPath.replace(XPATH_SEPARATOR, EXPRESSION_SEPARATOR) + CHILDREN_SEPARATOR + label;
            }
        }
        return newXPath;
    }

    public static String convertToExpression(String xPath) {
        if (xPath == null) {
            return xPath;
        }

        if (xPath.indexOf(CHILDREN_SEPARATOR) != -1) {
            return EXPRESSION_LEFT + xPath + EXPRESSION_RIGHT;
        } else {
            return xPath.replaceAll(XPATH_SEPARATOR, EXPRESSION_SEPARATOR);
        }

    }

    public static void updateXPathAndExpression(XmlMapData mapData, XmlMapExpressionManager expressionManager,
            List<? extends TreeNode> treeNodes, String newName, int xpathReplaceLocation) {
        for (TreeNode treeNode : treeNodes) {
            updateXPathAndExpression(mapData, expressionManager, treeNode, newName, xpathReplaceLocation, true);
        }

    }

    public static void updateXPathAndExpression(XmlMapData mapperData, XmlMapExpressionManager expressionManager,
            TreeNode treeNode, String newName, int xpathReplaceLocation, boolean updateTargetExpression) {
        String xpath = treeNode.getXpath();
        int xPathLength = getXPathLength(xpath);
        String newXPath = "";
        // tree child xpath eg : row1.newColum:/class/student/name
        if (xpath.split(CHILDREN_SEPARATOR).length == 2) {
            String[] split = xpath.split(CHILDREN_SEPARATOR);
            // change the root node part eg : row1.newColum
            if (xpathReplaceLocation <= 2) {
                String[] subSplit = split[0].split(EXPRESSION_SEPARATOR_SPLIT);
                if (subSplit.length == 2 && xpathReplaceLocation - 1 >= 0) {
                    subSplit[xpathReplaceLocation - 1] = newName;
                    newXPath = subSplit[0] + EXPRESSION_SEPARATOR + subSplit[1] + CHILDREN_SEPARATOR + split[1];
                }
            } else {
                // change the child part eg : class/student/name
                String[] subSplit = split[1].split(XPATH_SEPARATOR);
                if (xpathReplaceLocation == xPathLength) {
                    String typeString = "";
                    if (NodeType.ATTRIBUT.equals(treeNode.getNodeType())) {
                        typeString = XPATH_ATTRIBUTE;
                    } else if (NodeType.NAME_SPACE.equals(treeNode.getNodeType())) {
                        typeString = XPATH_NAMESPACE;
                    }
                    subSplit[xpathReplaceLocation - 2 - 1] = typeString + newName;
                } else {
                    subSplit[xpathReplaceLocation - 2 - 1] = newName;
                }

                newXPath = split[0] + CHILDREN_SEPARATOR;
                for (String string : subSplit) {
                    newXPath = newXPath + string + XPATH_SEPARATOR;
                }
                newXPath = newXPath.substring(0, newXPath.length() - 1);
            }

        } else if (xpath.split(XPATH_SEPARATOR).length == 2) {
            // normal column
            String[] split = xpath.split(XPATH_SEPARATOR);
            if (xpathReplaceLocation <= xPathLength && xpathReplaceLocation - 1 >= 0) {
                split[xpathReplaceLocation - 1] = newName;
            }
            newXPath = split[0] + XPATH_SEPARATOR + split[1];

        } else {
            throw new IllegalArgumentException("Invalid xpath");
        }

        treeNode.setXpath(newXPath);
        if (updateTargetExpression) {
            updateTargetExpression(treeNode, xpath, expressionManager);
        } else {
            if (mapperData == null) {
                return;
            }
            XmlMapUtil.detachNodeConnections(treeNode, mapperData, true);
        }
        if (!treeNode.getChildren().isEmpty()) {
            for (TreeNode child : treeNode.getChildren()) {
                updateXPathAndExpression(mapperData, expressionManager, child, newName, xpathReplaceLocation,
                        updateTargetExpression);
            }
        }
    }

    private static void updateTargetExpression(TreeNode treeNode, String oldXpath, XmlMapExpressionManager expressionManager) {
        String oldExpression = convertToExpression(oldXpath);
        String newExpression = XmlMapUtil.convertToExpression(treeNode.getXpath());
        updateTargetExpression(treeNode, oldExpression, newExpression, expressionManager);
    }

    public static void updateTargetExpression(AbstractNode renamedNode, String oldExpression, String newExpression,
            XmlMapExpressionManager expressionManager) {
        TableEntryLocation previousLocation = expressionManager.parseTableEntryLocation(oldExpression).get(0);
        TableEntryLocation newLocation = expressionManager.parseTableEntryLocation(newExpression).get(0);

        List<INodeConnection> connections = new ArrayList<INodeConnection>();
        connections.addAll(renamedNode.getOutgoingConnections());
        if (renamedNode instanceof TreeNode) {
            connections.addAll(((TreeNode) renamedNode).getLookupOutgoingConnections());
        }

        for (INodeConnection connection : connections) {
            AbstractNode target = connection.getTarget();
            List<TableEntryLocation> targetLocaitons = expressionManager.parseTableEntryLocation(target.getExpression());
            for (TableEntryLocation current : targetLocaitons) {
                if (current.equals(previousLocation)) {
                    String replaced = expressionManager.replaceExpression(target.getExpression(), current, newLocation);
                    target.setExpression(replaced);
                }
            }

        }
        for (FilterConnection connection : renamedNode.getFilterOutGoingConnections()) {
            AbstractInOutTree target = connection.getTarget();
            List<TableEntryLocation> targetLocaitons = expressionManager.parseTableEntryLocation(target.getExpressionFilter());
            for (TableEntryLocation current : targetLocaitons) {
                if (current.equals(previousLocation)) {
                    String replaced = expressionManager.replaceExpression(target.getExpressionFilter(), current, newLocation);
                    target.setExpressionFilter(replaced);
                }
            }
        }
    }

    /*
     * convert from output expression to xpath
     */
    public static String convertToXpath(String expression) {
        if (expression == null) {
            return expression;
        }

        if (expression.startsWith(EXPRESSION_LEFT) && expression.endsWith(EXPRESSION_RIGHT)) {
            return expression.substring(1, expression.length() - 1);
        } else {
            return expression.replace(EXPRESSION_SEPARATOR, XPATH_SEPARATOR);
        }

    }

    public static TreeNode getTreeNodeRoot(TreeNode model) {
        if (model.eContainer() instanceof AbstractInOutTree) {
            return model;
        } else if (model.eContainer() instanceof TreeNode) {
            return getTreeNodeRoot((TreeNode) model.eContainer());
        }
        return null;
    }

    public static AbstractInOutTree getAbstractInOutTree(TreeNode model) {
        if (model.eContainer() instanceof AbstractInOutTree) {
            return (AbstractInOutTree) model.eContainer();
        } else if (model.eContainer() instanceof TreeNode) {
            return getAbstractInOutTree((TreeNode) model.eContainer());
        }
        return null;

    }

    public static XmlMapData getXmlMapData(AbstractNode treeNode) {
        AbstractNode rootNode = null;
        if (treeNode instanceof TreeNode) {
            rootNode = XmlMapUtil.getTreeNodeRoot((TreeNode) treeNode);
        } else if (treeNode instanceof VarNode) {
            return (XmlMapData) treeNode.eContainer().eContainer();
        }
        if (rootNode != null && rootNode.eContainer() != null && rootNode.eContainer().eContainer() instanceof XmlMapData) {
            return (XmlMapData) rootNode.eContainer().eContainer();
        }
        return null;
    }

    public static void cleanSubGroup(TreeNode node, List<TreeNode> newLoopUpGroups) {
        for (TreeNode treeNode : node.getChildren()) {
            TreeNode child = treeNode;
            if (child.isGroup()) {
                if (newLoopUpGroups == null || newLoopUpGroups.isEmpty()) {
                    child.setGroup(false);
                } else {
                    boolean found = false;
                    for (TreeNode group : newLoopUpGroups) {
                        if (child == group) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        child.setGroup(false);
                    }
                }
            }
            cleanSubGroup(child, newLoopUpGroups);
        }
    }

    public static void cleanSubGroup(TreeNode node) {
        cleanSubGroup(node, null);

    }

    public static void detachConnectionsTarget(AbstractNode treeNode, XmlMapData mapData) {
        detachConnectionsTarget(treeNode, mapData, true);
    }

    public static void detachConnectionsTarget(AbstractNode treeNode, XmlMapData mapData, boolean detachChildren) {
        for (Connection connection : treeNode.getOutgoingConnections()) {
            AbstractNode target = connection.getTarget();
            if (target.getIncomingConnections().contains(connection)) {
                target.getIncomingConnections().remove(connection);
                mapData.getConnections().remove(connection);
            }
        }
        treeNode.getOutgoingConnections().clear();

        // TreeNode detach children's connections
        if (treeNode instanceof TreeNode) {
            TreeNode inputTreeNode = (TreeNode) treeNode;
            if (detachChildren && !inputTreeNode.getChildren().isEmpty()) {
                for (int i = 0; i < inputTreeNode.getChildren().size(); i++) {
                    TreeNode child = inputTreeNode.getChildren().get(i);
                    detachConnectionsTarget(child, mapData);
                }
            }
        }
    }

    public static void detachConnectionsSouce(AbstractNode treeNode, XmlMapData mapData) {
        detachConnectionsSouce(treeNode, mapData, true);
    }

    public static void detachConnectionsSouce(AbstractNode treeNode, XmlMapData mapData, boolean detachChildren) {
        for (Connection connection : treeNode.getIncomingConnections()) {
            AbstractNode source = connection.getSource();
            if (source.getOutgoingConnections().contains(connection)) {
                source.getOutgoingConnections().remove(connection);
                mapData.getConnections().remove(connection);
            }
        }
        treeNode.getIncomingConnections().clear();

        if (treeNode instanceof OutputTreeNode) {
            OutputTreeNode outputTreeNode = (OutputTreeNode) treeNode;
            if (!XmlMapUtil.isExpressionEditable(outputTreeNode) && outputTreeNode.isAggregate()) {
                outputTreeNode.setAggregate(false);
            }
            if (detachChildren && !outputTreeNode.getChildren().isEmpty()) {
                for (int i = 0; i < outputTreeNode.getChildren().size(); i++) {
                    TreeNode child = outputTreeNode.getChildren().get(i);
                    detachConnectionsSouce(child, mapData);
                }
            }
        }
        treeNode.setExpression("");
    }

    public static void detachLookupTarget(TreeNode treeNode, XmlMapData mapData) {
        detachLookupTarget(treeNode, mapData, true);
    }

    public static void detachLookupTarget(TreeNode treeNode, XmlMapData mapData, boolean detachChildren) {
        for (LookupConnection connection : treeNode.getLookupOutgoingConnections()) {
            if (connection.getTarget() instanceof TreeNode) {
                TreeNode target = (TreeNode) connection.getTarget();
                if (target.getLookupIncomingConnections().contains(connection)) {
                    target.getLookupIncomingConnections().remove(connection);
                    mapData.getConnections().remove(connection);
                }
            }
        }
        treeNode.getLookupOutgoingConnections().clear();

        if (detachChildren) {
            if (!treeNode.getChildren().isEmpty()) {
                for (TreeNode child : treeNode.getChildren()) {
                    detachLookupTarget(child, mapData, detachChildren);
                }
            }
        }

    }

    public static void detachLookupSource(TreeNode treeNode, XmlMapData mapData) {
        detachLookupSource(treeNode, mapData, true);
    }

    public static void detachLookupSource(TreeNode treeNode, XmlMapData mapData, boolean detachChildren) {
        for (LookupConnection connection : treeNode.getLookupIncomingConnections()) {
            TreeNode source = (TreeNode) connection.getSource();
            if (source.getLookupOutgoingConnections().contains(connection)) {
                source.getLookupOutgoingConnections().remove(connection);
                mapData.getConnections().remove(connection);
            }
        }
        treeNode.getLookupIncomingConnections().clear();

        if (detachChildren) {
            if (!treeNode.getChildren().isEmpty()) {
                for (TreeNode child : treeNode.getChildren()) {
                    detachLookupSource(child, mapData, detachChildren);
                }
            }
        }
    }

    public static void detachFilterSource(AbstractInOutTree tree, XmlMapData mapData) {
        for (FilterConnection connection : tree.getFilterIncomingConnections()) {
            if (connection.getSource() != null) {
                if (connection.getSource().getFilterOutGoingConnections().contains(connection)) {
                    connection.getSource().getFilterOutGoingConnections().remove(connection);
                    mapData.getConnections().remove(connection);
                }
            }
        }
        tree.getFilterIncomingConnections().clear();
    }

    public static void detachFilterTarget(AbstractNode abstractNode, XmlMapData mapData, boolean detachChildren) {
        for (FilterConnection connection : abstractNode.getFilterOutGoingConnections()) {
            AbstractInOutTree target = connection.getTarget();
            if (target.getFilterIncomingConnections().contains(connection)) {
                target.getFilterIncomingConnections().remove(connection);
                mapData.getConnections().remove(connection);
            }
        }
        abstractNode.getFilterOutGoingConnections().clear();

        if (detachChildren && abstractNode instanceof TreeNode) {
            TreeNode treeNode = (TreeNode) abstractNode;
            if (!treeNode.getChildren().isEmpty()) {
                for (TreeNode child : treeNode.getChildren()) {
                    detachFilterTarget(child, mapData, detachChildren);
                }
            }
        }

    }

    public static void detachFilterTarget(AbstractNode abstractNode, XmlMapData mapData) {
        detachFilterTarget(abstractNode, mapData, true);
    }

    public static void detachNodeConnections(AbstractNode abstractNode, XmlMapData mapData, boolean detachChildren) {
        detachConnectionsSouce(abstractNode, mapData, detachChildren);
        detachConnectionsTarget(abstractNode, mapData, detachChildren);
        detachFilterTarget(abstractNode, mapData, detachChildren);
        if (abstractNode instanceof TreeNode) {
            detachLookupSource((TreeNode) abstractNode, mapData, detachChildren);
            detachLookupTarget((TreeNode) abstractNode, mapData, detachChildren);
        }

    }

    public static List<IConnection> getAllNodeLookConnections(AbstractInOutTree abstractTree) {
        List<IConnection> connections = new ArrayList<IConnection>();
        if (abstractTree instanceof InputXmlTree) {
            getChildLookupConnections(connections, ((InputXmlTree) abstractTree).getNodes());
        }
        return connections;
    }

    private static void getChildLookupConnections(List<IConnection> connections, List<? extends TreeNode> nodesList) {
        for (TreeNode node : nodesList) {
            EList<LookupConnection> outgoingConnections = node.getLookupOutgoingConnections();
            connections.addAll(outgoingConnections);
            if (!node.getChildren().isEmpty()) {
                getChildLookupConnections(connections, node.getChildren());
            }
        }
    }

    /**
     *
     * get filter connections with a InputXmlTree target
     *
     * @param abstractTree
     * @return
     */
    public static List<IConnection> getInputTreeFilterConnections(AbstractInOutTree abstractTree) {
        List<IConnection> connections = new ArrayList<IConnection>();
        if (abstractTree instanceof InputXmlTree) {
            getChildFilterConnections(connections, ((InputXmlTree) abstractTree).getNodes());
        }
        return connections;
    }

    private static void getChildFilterConnections(List<IConnection> connections, List<? extends TreeNode> nodesList) {
        for (TreeNode node : nodesList) {
            EList<FilterConnection> outgoingConnections = node.getFilterOutGoingConnections();
            for (FilterConnection conn : outgoingConnections) {
                if (conn.getTarget() instanceof InputXmlTree) {
                    connections.add(conn);
                }
            }
            if (!node.getChildren().isEmpty()) {
                getChildFilterConnections(connections, node.getChildren());
            }
        }
    }

    public static String findUniqueVarColumnName(String baseName, VarTable parentTable) {
        if (baseName == null) {
            throw new IllegalArgumentException("Base name can't null");
        }
        String uniqueName = baseName + 1;

        int counter = 1;
        boolean exists = true;
        while (exists) {
            exists = !checkValidColumnName(uniqueName, parentTable);
            if (!exists) {
                break;
            }
            uniqueName = baseName + counter++;
        }
        return uniqueName;
    }

    private static boolean checkValidColumnName(String newName, VarTable parentTable) {
        for (VarNode entry : parentTable.getNodes()) {
            if (entry.getName().equals(newName)) {
                return false;
            }
        }
        Pattern regex = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$", Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE);//$NON-NLS-1$
        Matcher regexMatcher = regex.matcher(newName);
        return regexMatcher.matches();
    }

    public static boolean hasAtLeastOneHashKey(InputXmlTree inputTree) {
        if (inputTree == null) {
            return false;
        }
        boolean hasHashKey;
        for (TreeNode node : inputTree.getNodes()) {
            hasHashKey = hasHashKey(node);
            if (hasHashKey) {
                return hasHashKey;
            }
        }
        return false;

    }

    private static boolean hasHashKey(TreeNode node) {
        if (node.getExpression() != null && !node.getExpression().trim().equals("")) {
            return true;
        } else {
            boolean childHasKey = false;
            if (!node.getChildren().isEmpty()) {
                for (TreeNode child : node.getChildren()) {
                    childHasKey = hasHashKey(child);
                    if (childHasKey) {
                        return childHasKey;
                    }
                }
            }
            return false;
        }

    }

    public static boolean isExpressionEditable(TreeNode treeNode) {
        List children = treeNode.getChildren();
        if (treeNode.isChoice() || treeNode.isSubstitution()) {
            return false;
        }
        boolean haschild = false;
        boolean isNameSpace = false;
        if (children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                TreeNode child = (TreeNode) children.get(i);
                // attribute and namespace are not treat as subnode , so the expression of treeNode should be editable.
                if (NodeType.ATTRIBUT != child.getNodeType() && NodeType.NAME_SPACE != child.getNodeType()) {
                    haschild = true;
                    break;
                }
            }
        } else if (NodeType.NAME_SPACE.equals(treeNode.getNodeType())) {
            isNameSpace = true;
        }
        return !haschild && !isNameSpace;
    }

    public static boolean hasDocument(AbstractInOutTree abstractTree) {
        if (abstractTree == null) {
            return false;
        }
        List<TreeNode> children = new ArrayList<TreeNode>();
        if (abstractTree instanceof OutputXmlTree) {
            children.addAll(((OutputXmlTree) abstractTree).getNodes());
        } else if (abstractTree instanceof InputXmlTree) {
            children.addAll(((InputXmlTree) abstractTree).getNodes());
        }
        for (int i = 0; i < children.size(); i++) {
            TreeNode treeNode = children.get(i);
            if (XmlMapUtil.DOCUMENT.equals(treeNode.getType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSubElementOfDocument(TreeNode treeNode) {
        if (treeNode == null) {
            return false;
        }
        return getXPathLength(treeNode.getXpath()) > 2;
    }

    public static void upsetMainNode(TreeNode loop) {
        if (loop == null) {
            return;
        }
        if (NodeType.ELEMENT.equals(loop.getNodeType())) {
            TreeNode parent = loop;
            while (parent != null) {
                parent.setMain(true);
                if (parent.eContainer() instanceof TreeNode
                        && !XmlMapUtil.DOCUMENT.equals(((TreeNode) parent.eContainer()).getType())) {
                    parent = (TreeNode) parent.eContainer();
                } else {
                    parent = null;
                }
            }

        }
    }

    public static void clearMainNode(TreeNode root) {
        if (root == null) {
            return;
        }
        if (NodeType.ELEMENT.equals(root.getNodeType())) {
            TreeNode e = root;
            if (e.isMain()) {
                e.setMain(false);
            }
            for (TreeNode child : root.getChildren()) {
                clearMainNode(child);
            }
        }
    }

    public static TreeNode findUpGroupNode(OutputTreeNode node) {
        if (node.eContainer() instanceof OutputTreeNode) {
            OutputTreeNode parent = (OutputTreeNode) node.eContainer();
            if (parent.isGroup()) {
                return parent;
            } else {
                return findUpGroupNode(parent);
            }
        }
        return null;
    }

    public static TreeNode getRealParentNode(TreeNode node) {
        if (node.isSubstitution() || node.isChoice()) {
            if (node.eContainer() instanceof TreeNode) {
                TreeNode parent = (TreeNode) node.eContainer();
                if (!parent.isChoice() && !parent.isSubstitution()) {
                    return parent;
                } else {
                    return getRealParentNode(parent);
                }
            }
        }
        return node;
    }

    public static TreeNode getLoopParentNode(TreeNode treeNode) {
        if (treeNode != null && treeNode instanceof TreeNode) {
            if (treeNode.isLoop()) {
                return treeNode;
            } else {
                if (treeNode.eContainer() != null && treeNode.eContainer() instanceof TreeNode) {
                    return getLoopParentNode((TreeNode) treeNode.eContainer());
                }
            }
        }
        return null;
    }

    public static OutputTreeNodeEditPart getParentLoopNodeEditPart(OutputTreeNodeEditPart nodePart) {
        if (nodePart != null && nodePart instanceof OutputTreeNodeEditPart) {
            OutputTreeNodeEditPart nodePartTemp = nodePart;
            TreeNode model = (TreeNode) nodePartTemp.getModel();
            if (model.isLoop()) {
                return nodePartTemp;
            } else {
                if (nodePartTemp.getParent() != null && nodePartTemp.getParent() instanceof OutputTreeNodeEditPart) {
                    return getParentLoopNodeEditPart((OutputTreeNodeEditPart) nodePartTemp.getParent());
                }
            }
        }
        return null;
    }

    public static List<TreeNode> getMultiLoopsForXmlTree(AbstractInOutTree tree) {
        List<TreeNode> loopNodes = new ArrayList<TreeNode>();
        List<? extends TreeNode> children = new ArrayList<TreeNode>();
        if (tree instanceof InputXmlTree) {
            children = ((InputXmlTree) tree).getNodes();
        } else if (tree instanceof OutputXmlTree) {
            children = ((OutputXmlTree) tree).getNodes();
        }
        getChildLoops(loopNodes, children);
        return loopNodes;
    }

    public static boolean checkMultiLoopsStatus(AbstractInOutTree tree) {
        // set multiloops to true only if one doc schema have more then one loops , if a table have two doc schema but
        // each one only have one loop,multiloops should be false
        List<? extends TreeNode> children = new ArrayList<TreeNode>();
        if (tree instanceof InputXmlTree) {
            children = ((InputXmlTree) tree).getNodes();
        } else if (tree instanceof OutputXmlTree) {
            children = ((OutputXmlTree) tree).getNodes();
        }
        List<TreeNode> docChildren = new ArrayList<TreeNode>();
        for (TreeNode child : children) {
            if (DOCUMENT.equals(child.getType())) {
                docChildren.add(child);
            }
        }
        List<TreeNode> loopNodes = new ArrayList<TreeNode>();
        for (TreeNode doc : docChildren) {
            getChildLoops(loopNodes, doc.getChildren());
            if (loopNodes.size() > 1) {
                return true;
            }
        }
        return false;
    }

    public static void getChildLoops(List<TreeNode> loopNodes, List<? extends TreeNode> nodesToCheck) {
        // if (onlyGetFirstLoop) {
        // if (!loopNodes.isEmpty()) {
        // return;
        // }
        // }
        for (TreeNode node : nodesToCheck) {
            if (node.isLoop()) {
                loopNodes.add(node);
            } else if (!node.getChildren().isEmpty()) {
                getChildLoops(loopNodes, node.getChildren());
            }

        }

    }

    public static void getChildLoops(List<TreeNode> loopNodes, List<? extends TreeNode> nodesToCheck, boolean addRoot,
            boolean onlyGetFirstLoop) {
        if (onlyGetFirstLoop) {
            if (!loopNodes.isEmpty()) {
                return;
            }
        }
        for (TreeNode node : nodesToCheck) {
            if (node.isLoop()) {
                loopNodes.add(node);
            } else {
                if (addRoot) {
                    if (node.eContainer() != null && node.eContainer().eContainer() instanceof AbstractInOutTree) {
                        loopNodes.add(node);
                    }
                }
                if (!node.getChildren().isEmpty()) {
                    getChildLoops(loopNodes, node.getChildren());
                }
            }

        }

    }

    // only leaf nodes or element without sub elements can be drag
    public static boolean isDragable(TreeNode treeNode) {
        if (treeNode.isChoice() || treeNode.isSubstitution()) {
            return false;
        }
        if (treeNode.getChildren().isEmpty()) {
            return true;
        } else {
            for (TreeNode child : treeNode.getChildren()) {
                if (!NodeType.ATTRIBUT.equals(child.getNodeType()) && !NodeType.NAME_SPACE.equals(child.getNodeType())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String isValidColumnName(final List<? extends AbstractNode> validationList, String newName) {
        if (!StringUtil.validateLabelForXML(newName) || !MetadataToolHelper.isValidColumnName(newName)) {
            return Messages.getString("InsertNewColumn_invalid", newName);
        } else {
            for (AbstractNode existed : validationList) {
                if (existed.getName().equals(newName)) {
                    return Messages.getString("InsertNewColumn_existed");
                }
            }
        }
        return null;

    }

    public static void removeloopInOutputTree(MapperManager mapperManager, List<TreeNode> oldLoopsFromInput) {
        removeloopInOutputTree(mapperManager.getExternalData(), mapperManager.getMainInputTree(), oldLoopsFromInput, true);
    }

    /**
     *
     * DOC WCHEN Remove source loops refrenced from input main
     *
     * @param mapData
     * @param mainInput
     * @param oldLoopsFromInput
     * @param checkProblem
     */
    public static void removeloopInOutputTree(XmlMapData mapData, InputXmlTree mainInput, List<TreeNode> oldLoopsFromInput,
            boolean checkProblem) {
        boolean isMainInputMultiLoop = mainInput == null ? false : mainInput.isMultiLoops();
        EList<OutputXmlTree> outputTrees = mapData.getOutputTrees();
        for (OutputXmlTree outputTree : outputTrees) {
            if (isMainInputMultiLoop) {
                for (TreeNode oldLoop : oldLoopsFromInput) {
                    EList<InputLoopNodesTable> inputLoopNodesTables = outputTree.getInputLoopNodesTables();
                    for (InputLoopNodesTable inputLoopTable : inputLoopNodesTables) {
                        inputLoopTable.getInputloopnodes().remove(oldLoop);
                    }
                }
            } else {
                List<TreeNode> multiLoopsForXmlTree = getMultiLoopsForXmlTree(outputTree);
                for (TreeNode loop : multiLoopsForXmlTree) {
                    ((OutputTreeNode) loop).setInputLoopNodesTable(null);
                }
                outputTree.getInputLoopNodesTables().clear();
            }
        }

    }

    public static void removeloopInOutputTree(XmlMapData mapData, InputXmlTree mainInput, List<TreeNode> oldLoopsFromInput) {
        removeloopInOutputTree(mapData, mainInput, oldLoopsFromInput, false);
    }

    public static void removeLoopTableForOutput(OutputXmlTree outputTable2Check, List<TreeNode> reomvedOutputLoops,
            boolean isMainMultiloops) {
        for (TreeNode reomved : reomvedOutputLoops) {
            OutputTreeNode outputNode = (OutputTreeNode) reomved;
            if (outputNode.getInputLoopNodesTable() != null) {
                outputTable2Check.getInputLoopNodesTables().remove(outputNode.getInputLoopNodesTable());
                outputNode.setInputLoopNodesTable(null);
            }
        }
        // incase there are no used InputLoopNodesTables from previous version
        if (!isMainMultiloops) {
            outputTable2Check.getInputLoopNodesTables().clear();
        }

    }

    public static List<TreeNode> getFlatChildrenList(TreeNode treeNode) {
        List<TreeNode> list = new ArrayList<TreeNode>();
        List<TreeNode> children = treeNode.getChildren();
        for (int i = 0; i < children.size(); i++) {
            TreeNode child = children.get(i);
            list.add(child);
            if (!child.getChildren().isEmpty()) {
                list.addAll(getFlatChildrenList(child));
            }
        }
        return list;

    }

    public static boolean cleanGroup(List<? extends TreeNode> nodes) {
        boolean changed = false;
        for (TreeNode obj : nodes) {
            OutputTreeNode outputNode = (OutputTreeNode) obj;
            if (outputNode.isGroup()) {
                outputNode.setGroup(false);
                changed = true;
            }
            if (!outputNode.getChildren().isEmpty()) {
                changed = cleanGroup(outputNode.getChildren()) || changed;
            }
        }

        return changed;
    }

    public static boolean cleanAggregate(List<? extends TreeNode> nodes) {
        boolean changed = false;
        for (TreeNode obj : nodes) {
            OutputTreeNode outputNode = (OutputTreeNode) obj;
            if (outputNode.isAggregate()) {
                outputNode.setAggregate(false);
                changed = true;
            }
            if (!outputNode.getChildren().isEmpty()) {
                changed = cleanAggregate(outputNode.getChildren()) || changed;
            }
        }

        return changed;
    }

    public static String getColumnPatternFromMetadataTable(XMLFileNode node, MetadataTable metaTable) {
        if (node == null || metaTable == null) {
            return null;
        }
        String pattern = null;
        EList<MetadataColumn> columns = metaTable.getColumns();
        for (MetadataColumn column : columns) {
            if (column.getLabel() != null && column.getLabel().equals(node.getRelatedColumn())) {
                String colPattern = column.getPattern();
                if (colPattern != null && !colPattern.isEmpty()) {
                    pattern = colPattern;
                    break;
                }
            }
        }
        return pattern;
    }

}
