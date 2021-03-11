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
package org.talend.designer.xmlmap.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.INodeConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.ui.expressionutil.TableEntryLocation;
import org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class DirectEditCommand extends Command {

    private AbstractNode model;

    private Object newValue;

    private DirectEditType type;

    private EditPart targetEditPart;

    private XmlMapData mapperData;

    protected XmlMapExpressionManager expressionManager = new XmlMapExpressionManager();

    public DirectEditCommand() {

    }

    public DirectEditCommand(EditPart targetEditPart, AbstractNode model, DirectEditType type, Object newValue) {
        this.targetEditPart = targetEditPart;
        this.model = model;
        this.newValue = newValue;
        this.type = type;
    }

    @Override
    public void execute() {
        try {
            if (model != null) {
                if (DirectEditType.EXPRESSION.equals(type)) {

                    List<TableEntryLocation> matchedLocations = expressionManager.parseTableEntryLocation((String) newValue);

                    EList<? extends INodeConnection> connections = null;
                    if (model instanceof OutputTreeNode || model instanceof VarNode) {
                        connections = model.getIncomingConnections();
                    } else if (model instanceof TreeNode) {
                        connections = ((TreeNode) model).getLookupIncomingConnections();
                    }

                    List usefullConnections = new ArrayList();

                    mapperData = XmlMapUtil.getXmlMapData(model);
                    if (!matchedLocations.isEmpty()) {
                        for (int i = 0; i < matchedLocations.size(); i++) {
                            TableEntryLocation currentLocation = matchedLocations.get(i);
                            boolean found = false;
                            for (INodeConnection conn : connections) {
                                TableEntryLocation sourceLocation = null;
                                if (conn.getSource() instanceof TreeNode) {
                                    sourceLocation = expressionManager.parseTableEntryLocation(
                                            XmlMapUtil.convertToExpression(((TreeNode) conn.getSource()).getXpath())).get(0);
                                } else if (conn.getSource() instanceof VarNode) {
                                    VarNode varNode = (VarNode) conn.getSource();
                                    sourceLocation = new TableEntryLocation(((VarTable) varNode.eContainer()).getName(),
                                            varNode.getName());
                                }
                                if (currentLocation.equals(sourceLocation)) {
                                    found = true;
                                    usefullConnections.add(conn);
                                    break;
                                }
                            }
                            if (!found) {
                                if (mapperData != null) {
                                    String convertToXpath = XmlMapUtil.convertToXpath(currentLocation.toString());
                                    boolean findFromVar = false;
                                    if (model instanceof OutputTreeNode) {
                                        findFromVar = true;
                                    }

                                    AbstractNode sourceNode = findConnectionSource(mapperData, currentLocation,
                                            XmlMapUtil.getXPathLength(convertToXpath), findFromVar);
                                    if (sourceNode != null) {
                                        INodeConnection connection = null;
                                        if (model instanceof OutputTreeNode || model instanceof VarNode) {
                                            connection = XmlmapFactory.eINSTANCE.createConnection();
                                            sourceNode.getOutgoingConnections().add((Connection) connection);
                                            model.getIncomingConnections().add((Connection) connection);

                                        } else if (model instanceof TreeNode && sourceNode instanceof TreeNode) {
                                            TreeNode source = (TreeNode) sourceNode;
                                            connection = XmlmapFactory.eINSTANCE.createLookupConnection();
                                            source.getLookupOutgoingConnections().add((LookupConnection) connection);
                                            ((TreeNode) model).getLookupIncomingConnections().add((LookupConnection) connection);
                                        }

                                        //
                                        // if (model instanceof OutputTreeNode && sourceNode instanceof TreeNode) {
                                        // createInputLoopTable((TreeNode) sourceNode, (OutputTreeNode) model);
                                        // }

                                        connection.setSource(sourceNode);
                                        connection.setTarget(model);
                                        mapperData.getConnections().add(connection);
                                        usefullConnections.add(connection);
                                    }
                                }
                            }
                        }
                    } else {
                        if (!connections.isEmpty()) {
                            if (model instanceof OutputTreeNode || model instanceof VarNode) {
                                XmlMapUtil.detachConnectionsSouce(model, mapperData, false);
                                model.getIncomingConnections().clear();
                            } else if (model instanceof TreeNode) {
                                XmlMapUtil.detachLookupSource((TreeNode) model, mapperData);
                                ((TreeNode) model).getLookupIncomingConnections().clear();
                            }
                        }
                    }
                    List<INodeConnection> copyOfConnections = new ArrayList<INodeConnection>(connections);
                    copyOfConnections.removeAll(usefullConnections);
                    if (model instanceof OutputTreeNode || model instanceof VarNode) {
                        for (INodeConnection connection : copyOfConnections) {
                            if (connection.getSource() != null) {
                                if (connection.getSource().getOutgoingConnections().contains(connection)) {
                                    connection.getSource().getOutgoingConnections().remove(connection);
                                    mapperData.getConnections().remove(connection);
                                }
                            }
                        }
                        model.getIncomingConnections().removeAll(copyOfConnections);

                    } else if (model instanceof TreeNode) {
                        for (INodeConnection connection : copyOfConnections) {
                            if (connection.getSource() != null) {
                                if (((TreeNode) connection.getSource()).getLookupOutgoingConnections().contains(connection)) {
                                    ((TreeNode) connection.getSource()).getLookupOutgoingConnections().remove(connection);
                                    mapperData.getConnections().remove(connection);
                                }
                            }
                        }
                        ((TreeNode) model).getLookupIncomingConnections().removeAll(copyOfConnections);

                    }

                    if (model instanceof OutputTreeNode) {
                        OutputTreeNode outModel = (OutputTreeNode) model;
                        if (NodeType.NAME_SPACE.equals(outModel.getNodeType()) && outModel.getExpression() != null
                                && !"".equals(model.getExpression())) {
                            outModel.setDefaultValue("");
                        }
                    }
                    model.setExpression((String) newValue);
                } else if (DirectEditType.VAR_NODE_TYPE.equals(type)) {
                    VarNode varModel = (VarNode) model;
                    JavaType javaTypeFromLabel = JavaTypesManager.getJavaTypeFromLabel((String) newValue);
                    if (javaTypeFromLabel == null) {
                        javaTypeFromLabel = JavaTypesManager.getDefaultJavaType();
                    }
                    varModel.setType(javaTypeFromLabel.getId());
                } else if (DirectEditType.NODE_NAME.equals(type)) {
                    if (model instanceof VarNode) {
                        List<VarNode> children = new ArrayList<VarNode>();
                        children.addAll(((VarTable) model.eContainer()).getNodes());
                        children.remove(model);
                        String message = XmlMapUtil.isValidColumnName(children, (String) newValue);
                        if (message != null) {
                            MessageDialog.openError(null, "Error", message);
                            return;
                        }
                        String oldName = model.getName();
                        String oldExpression = XmlMapUtil.VAR_TABLE_NAME + XmlMapUtil.EXPRESSION_SEPARATOR + oldName;
                        model.setName((String) newValue);
                        String newExpression = XmlMapUtil.VAR_TABLE_NAME + XmlMapUtil.EXPRESSION_SEPARATOR + model.getName();
                        XmlMapUtil.updateTargetExpression(model, oldExpression, newExpression, expressionManager);
                    } else if (model instanceof GlobalMapNode) {
                        model.setName((String) newValue);
                    }
                }
            }

        } catch (PatternSyntaxException ex) {
            // Syntax error in the regular expression
        }
    }

    // private void createInputLoopTable(TreeNode sourceNode, OutputTreeNode targetOutputNode) {
    // EditPartViewer viewer = targetEditPart.getViewer();
    // if (viewer instanceof XmlMapGraphicViewer) {
    // InputLoopTableUtil.addSourceLoopToInputLoopTable(sourceNode, targetOutputNode, ((XmlMapGraphicViewer) viewer)
    // .getMapperManager().getMainInputTree());
    // }
    // }

    protected AbstractNode findConnectionSource(XmlMapData mapperData, TableEntryLocation matchedLocation, int xpathLength,
            boolean findFromVar) {
        if (mapperData == null) {
            return null;
        }
        AbstractNode node = findConnectionSource(mapperData.getInputTrees(), matchedLocation, xpathLength);

        if (node == null && findFromVar) {
            if (xpathLength == 2) {
                VarTable varTable = mapperData.getVarTables().get(0);
                if (varTable.getName() != null && varTable.getName().equals(matchedLocation.getTableName())) {
                    EList<VarNode> nodes = varTable.getNodes();
                    for (VarNode varNode : nodes) {
                        if (varNode.getName() != null && varNode.getName().equals(matchedLocation.getColumnValue())) {
                            node = varNode;
                        }
                    }
                }
            }
        }
        if (node instanceof TreeNode) {
            if (XmlMapUtil.isDragable((TreeNode) node)) {
                return node;
            } else {
                return null;
            }
        }
        return node;
    }

    private TreeNode findConnectionSource(List<InputXmlTree> inputTrees, TableEntryLocation matchedLocation, int xpathLength) {
        for (InputXmlTree inputTree : inputTrees) {
            for (TreeNode node : inputTree.getNodes()) {
                TreeNode source = findConnectionSource(node, matchedLocation, xpathLength);
                if (source != null) {
                    return source;
                }
            }
        }

        return null;
    }

    private TreeNode findConnectionSource(TreeNode treeNode, TableEntryLocation matchedLocation, int xpathLength) {
        int sourceXpathLength = XmlMapUtil.getXPathLength(treeNode.getXpath());
        if (xpathLength == sourceXpathLength) {
            TableEntryLocation sourceLocation = expressionManager.parseTableEntryLocation(
                    XmlMapUtil.convertToExpression(treeNode.getXpath())).get(0);
            if (matchedLocation.equals(sourceLocation)) {
                return treeNode;
            }
        } else if (xpathLength > sourceXpathLength) {
            for (TreeNode child : treeNode.getChildren()) {
                TreeNode source = findConnectionSource(child, matchedLocation, xpathLength);
                if (source != null) {
                    return source;
                }
            }
        }
        return null;
    }

}
