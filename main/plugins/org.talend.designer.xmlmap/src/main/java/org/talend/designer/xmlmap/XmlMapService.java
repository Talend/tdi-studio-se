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
package org.talend.designer.xmlmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.service.IXmlMapService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.IConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.INodeConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.ui.expressionutil.TableEntryLocation;
import org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager;
import org.talend.designer.xmlmap.util.XmlMapConnectionBuilder;

/**
 * DOC talend class global comment. Detailled comment
 */
public class XmlMapService implements IXmlMapService {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.service.IXmlMapService#isXmlMapComponent(org.talend.core.model.process.IExternalNode)
     */
    @Override
    public boolean isXmlMapComponent(IExternalNode node) {
        if (node instanceof XmlMapComponent) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.service.IXmlMapService#checkXMLMapDifferents(org.talend.core.model.process.INode,
     * org.talend.core.model.process.INode)
     */
    @Override
    public boolean checkXMLMapDifferents(INode testNode, INode originalNode) {
        AbstractExternalData oriExternalData = originalNode.getExternalNode().getExternalEmfData();
        AbstractExternalData testExternalData = testNode.getExternalNode().getExternalEmfData();

        if (oriExternalData == null && testExternalData == null) {
            return false;
        }
        if (oriExternalData == null || testExternalData == null) {
            return true;
        }
        if (!(oriExternalData instanceof XmlMapData) || !(testExternalData instanceof XmlMapData)) {
            return false;
        }
        Map<String, String> inputConnNameMap = getInputConnectionNameMap(testNode);
        XmlMapData oriXmlData = (XmlMapData) oriExternalData;
        XmlMapData testXmlData = (XmlMapData) testExternalData;

        EList<InputXmlTree> oriInputs = oriXmlData.getInputTrees();
        EList<OutputXmlTree> oriOutputs = oriXmlData.getOutputTrees();
        EList<VarTable> oriVars = oriXmlData.getVarTables();

        EList<InputXmlTree> testInputs = testXmlData.getInputTrees();
        EList<OutputXmlTree> testOutputs = testXmlData.getOutputTrees();
        EList<VarTable> testVars = testXmlData.getVarTables();

        if (oriInputs.size() != testInputs.size()) {
            return true;
        }
        if (oriOutputs.size() != testOutputs.size()) {
            return true;
        }
        if (oriVars.size() != testVars.size()) {
            return true;
        }

        for (InputXmlTree oriInput : oriInputs) {
            String oriName = oriInput.getName();
            InputXmlTree testInput = null;
            for (InputXmlTree input : testInputs) {
                if (input.getName().equals(oriName)) {
                    testInput = input;
                    break;
                }
            }
            if (testInput == null) {
                testInput = getInputXmlTree(testNode, testInputs, oriName);
            }
            if (testInput == null) {
                return true;
            }
            if (oriInput.isActivateExpressionFilter() != testInput.isActivateExpressionFilter()) {
                return true;
            }
            if (!StringUtils.equals(oriInput.getExpressionFilter(), testInput.getExpressionFilter())) {
                return true;
            }
            if (oriInput.isMinimized() != testInput.isMinimized()) {
                return true;
            }
            if (oriInput.isActivateCondensedTool() != testInput.isActivateCondensedTool()) {
                return true;
            }
            EList<TreeNode> oriEntrys = oriInput.getNodes();
            EList<TreeNode> testEntrys = testInput.getNodes();
            if (oriEntrys.size() != testEntrys.size()) {
                return true;
            }
            for (TreeNode oriEntry : oriEntrys) {
                String oriEntryName = oriEntry.getName();
                boolean found = false;
                for (TreeNode testEntry : testEntrys) {
                    if (oriEntryName.equals(testEntry.getName())) {
                        found = true;
                        if (checkExpression(oriEntry.getExpression(), testEntry.getExpression(), inputConnNameMap)) {
                            return true;
                        }
                        break;
                    }
                }
                if (!found) {
                    return true;
                }
            }
        }

        for (OutputXmlTree oriOutput : oriOutputs) {
            String oriName = oriOutput.getName();
            OutputXmlTree testOutput = null;
            for (OutputXmlTree output : testOutputs) {
                if (output.getName().equals(oriName)) {
                    testOutput = output;
                    break;
                }
            }
            if (testOutput == null) {
                testOutput = getOutputXmlTree(testNode, testOutputs, oriName);
            }
            if (testOutput == null) {
                return true;
            }
            if (oriOutput.isActivateExpressionFilter() != testOutput.isActivateExpressionFilter()) {
                return true;
            }
            if (!StringUtils.equals(oriOutput.getExpressionFilter(), testOutput.getExpressionFilter())) {
                return true;
            }
            if (oriOutput.isMinimized() != testOutput.isMinimized()) {
                return true;
            }
            if (oriOutput.isActivateCondensedTool() != testOutput.isActivateCondensedTool()) {
                return true;
            }

            EList<OutputTreeNode> oriEntrys = oriOutput.getNodes();
            EList<OutputTreeNode> testEntrys = testOutput.getNodes();
            if (oriEntrys.size() != testEntrys.size()) {
                return true;
            }
            for (OutputTreeNode oriEntry : oriEntrys) {
                String oriEntryName = oriEntry.getName();
                boolean found = false;
                for (OutputTreeNode testEntry : testEntrys) {
                    if (oriEntryName.equals(testEntry.getName())) {
                        found = true;
                        if (found) {
                            if (checkChildOutputTreeNode(oriEntry, testEntry, inputConnNameMap)) {
                                return true;
                            }
                        }
                        break;
                    }
                }
                if (!found) {
                    return true;
                }
            }
        }

        for (VarTable oriVar : oriVars) {
            String oriName = oriVar.getName();
            VarTable testVar = null;
            for (VarTable var : testVars) {
                if (var.getName().equals(oriName)) {
                    testVar = var;
                    break;
                }
            }
            if (testVar == null) {
                return true;
            }
            if (oriVar.isMinimized() != testVar.isMinimized()) {
                return true;
            }
            EList<VarNode> oriEntrys = oriVar.getNodes();
            EList<VarNode> testEntrys = testVar.getNodes();
            if (oriEntrys.size() != testEntrys.size()) {
                return true;
            }
            for (VarNode oriEntry : oriEntrys) {
                String oriEntryName = oriEntry.getName();
                boolean found = false;
                for (VarNode testEntry : testEntrys) {
                    if (oriEntryName.equals(testEntry.getName())) {
                        found = true;
                        if (checkExpression(oriEntry.getExpression(), testEntry.getExpression(), inputConnNameMap)) {
                            return true;
                        }
                    }
                }
                if (!found) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.service.IXmlMapService#externalEmfDataClone(org.talend.designer.core.model.utils.emf.talendfile
     * .AbstractExternalData)
     */
    @Override
    public AbstractExternalData externalEmfDataClone(AbstractExternalData externalEmfData) {
        if (!(externalEmfData instanceof XmlMapData)) {
            return externalEmfData;
        }
        Map<EObject, EObject> nodeMaps = new HashMap<EObject, EObject>();
        XmlMapData newXmlMapData = XmlmapFactory.eINSTANCE.createXmlMapData();
        XmlMapData xmlMapData = (XmlMapData) externalEmfData;
        EList<InputXmlTree> oriInputs = xmlMapData.getInputTrees();
        EList<OutputXmlTree> oriOutputs = xmlMapData.getOutputTrees();
        EList<VarTable> oriVars = xmlMapData.getVarTables();
        EList<IConnection> oriConns = xmlMapData.getConnections();

        for (IConnection oriConn : oriConns) {
            if (oriConn instanceof INodeConnection) {
                AbstractNode sourceNode = ((INodeConnection) oriConn).getSource();
                AbstractNode targetNode = ((INodeConnection) oriConn).getTarget();
                EObject source = null;
                if (nodeMaps.get(sourceNode) != null) {
                    source = nodeMaps.get(sourceNode);
                } else {
                    source = cloneTreeNode(sourceNode);
                    nodeMaps.put(sourceNode, source);
                }

                EObject target = null;
                if (nodeMaps.get(targetNode) != null) {
                    target = nodeMaps.get(targetNode);
                } else {
                    target = cloneTreeNode(targetNode);
                    nodeMaps.put(targetNode, target);
                }

                if (oriConn instanceof Connection) {
                    new XmlMapConnectionBuilder().createConnection((AbstractNode) source, (AbstractNode) target, newXmlMapData);
                } else if (oriConn instanceof LookupConnection) {
                    new XmlMapConnectionBuilder().createLookupConnection((TreeNode) source, (TreeNode) target, newXmlMapData);
                }
            } else if (oriConn instanceof FilterConnection) {
                AbstractNode sourceNode = ((FilterConnection) oriConn).getSource();
                AbstractInOutTree targetNode = ((FilterConnection) oriConn).getTarget();

                EObject source = null;
                if (nodeMaps.get(sourceNode) != null) {
                    source = nodeMaps.get(sourceNode);
                } else {
                    source = cloneTreeNode(sourceNode);
                    nodeMaps.put(sourceNode, source);
                }
                EObject target = null;
                if (nodeMaps.get(targetNode) != null) {
                    target = nodeMaps.get(targetNode);
                } else {
                    target = cloneTreeNode(targetNode);
                    nodeMaps.put(targetNode, target);
                }

                new XmlMapConnectionBuilder().createFilterConnection((AbstractNode) source, (AbstractInOutTree) target,
                        newXmlMapData);
            }
        }

        for (InputXmlTree inputXml : oriInputs) {
            InputXmlTree newInputXml = null;
            if (nodeMaps.get(inputXml) == null) {
                newInputXml = (InputXmlTree) cloneTreeNode(inputXml);
            } else {
                newInputXml = (InputXmlTree) nodeMaps.get(inputXml);
            }

            if (inputXml.getNodes() != null) {
                for (TreeNode treeNode : inputXml.getNodes()) {
                    EObject obj = nodeMaps.get(treeNode);
                    if ((obj != null) && !newInputXml.getNodes().contains(obj)) {
                        newInputXml.getNodes().add((TreeNode) obj);
                    }
                }
            }
            if (!newXmlMapData.getInputTrees().contains(newInputXml)) {
                newXmlMapData.getInputTrees().add(newInputXml);
            }
            nodeMaps.put(inputXml, newInputXml);
        }

        for (OutputXmlTree outputXml : oriOutputs) {
            OutputXmlTree newOutputXml = null;
            if (nodeMaps.get(outputXml) == null) {
                newOutputXml = (OutputXmlTree) cloneTreeNode(outputXml);
            } else {
                newOutputXml = (OutputXmlTree) nodeMaps.get(outputXml);
            }
            if (outputXml.getNodes() != null) {
                for (OutputTreeNode treeNode : outputXml.getNodes()) {
                    EObject obj = nodeMaps.get(treeNode);
                    if ((obj != null) && !newOutputXml.getNodes().contains(obj)) {
                        newOutputXml.getNodes().add((OutputTreeNode) obj);
                    }
                }
            }
            // if ((outputXml.getFilterIncomingConnections() != null) &&
            // !outputXml.getFilterIncomingConnections().isEmpty()) {
            // newOutputXml.getFilterIncomingConnections().addAll(outputXml.getFilterIncomingConnections());
            // }
            // if ((outputXml.getInputLoopNodesTables() != null) && !outputXml.getInputLoopNodesTables().isEmpty())
            // {
            // newOutputXml.getInputLoopNodesTables().addAll(outputXml.getInputLoopNodesTables());
            // }
            if (!newXmlMapData.getOutputTrees().contains(newOutputXml)) {
                newXmlMapData.getOutputTrees().add(newOutputXml);
            }
            nodeMaps.put(outputXml, newOutputXml);
        }

        for (VarTable varXml : oriVars) {
            VarTable newVarXml = null;
            if (nodeMaps.get(varXml) == null) {
                newVarXml = XmlmapFactory.eINSTANCE.createVarTable();
                newVarXml.setMinimized(varXml.isMinimized());
                newVarXml.setName(varXml.getName());

                if (varXml.getNodes() != null) {
                    for (VarNode treeNode : varXml.getNodes()) {
                        EObject obj = nodeMaps.get(treeNode);
                        if (obj != null) {
                            newVarXml.getNodes().add((VarNode) obj);
                        }
                    }
                }
                if (!newXmlMapData.getVarTables().contains(newVarXml)) {
                    newXmlMapData.getVarTables().add(newVarXml);
                }
                nodeMaps.put(varXml, newVarXml);
            }
        }

        return newXmlMapData;
    }

    private EObject cloneTreeNode(EObject node) {
        if (node instanceof AbstractInOutTree) {
            AbstractInOutTree source = null;
            AbstractInOutTree oriSource = null;
            if (node instanceof InputXmlTree) {
                oriSource = (InputXmlTree) node;
                source = XmlmapFactory.eINSTANCE.createInputXmlTree();
                ((InputXmlTree) source).setActivateGlobalMap(((InputXmlTree) oriSource).isActivateGlobalMap());
                ((InputXmlTree) source).setInnerJoin(((InputXmlTree) oriSource).isInnerJoin());
                ((InputXmlTree) source).setLookup(((InputXmlTree) oriSource).isLookup());
                ((InputXmlTree) source).setLookupMode(((InputXmlTree) oriSource).getLookupMode());
                ((InputXmlTree) source).setMatchingMode(((InputXmlTree) oriSource).getMatchingMode());
                ((InputXmlTree) source).setPersistent(((InputXmlTree) oriSource).isPersistent());
            } else if (node instanceof OutputXmlTree) {
                oriSource = (OutputXmlTree) node;
                source = XmlmapFactory.eINSTANCE.createOutputXmlTree();
                ((OutputXmlTree) source).setAllInOne(((OutputXmlTree) oriSource).isAllInOne());
                ((OutputXmlTree) source).setEnableEmptyElement(((OutputXmlTree) oriSource).isEnableEmptyElement());
                ((OutputXmlTree) source).setErrorReject(((OutputXmlTree) oriSource).isErrorReject());
                ((OutputXmlTree) source).setReject(((OutputXmlTree) oriSource).isReject());
                ((OutputXmlTree) source).setRejectInnerJoin(((OutputXmlTree) oriSource).isRejectInnerJoin());
            }
            source.setActivateCondensedTool(oriSource.isActivateCondensedTool());
            source.setActivateExpressionFilter(oriSource.isActivateExpressionFilter());
            source.setExpressionFilter(oriSource.getExpressionFilter());
            source.setMinimized(oriSource.isMinimized());
            source.setMultiLoops(oriSource.isMultiLoops());
            source.setName(oriSource.getName());
            return source;
        } else if (node instanceof VarNode) {
            VarNode oriSource = (VarNode) node;
            VarNode source = XmlmapFactory.eINSTANCE.createVarNode();
            source.setExpression(oriSource.getExpression());
            source.setName(oriSource.getName());
            source.setNullable(oriSource.isNullable());
            source.setType(oriSource.getType());
            return source;
        } else if (node instanceof TreeNode) {
            TreeNode source = null;
            TreeNode oriSource = null;
            if (node instanceof OutputTreeNode) {
                oriSource = (OutputTreeNode) node;
                source = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                ((OutputTreeNode) source).setAggregate(((OutputTreeNode) oriSource).isAggregate());
                ((OutputTreeNode) source).setInputLoopNodesTable(((OutputTreeNode) oriSource).getInputLoopNodesTable());
            } else if (node instanceof GlobalMapNode) {
                oriSource = (GlobalMapNode) node;
                source = XmlmapFactory.eINSTANCE.createGlobalMapNode();
                return source;
            } else {
                oriSource = (TreeNode) node;
                source = XmlmapFactory.eINSTANCE.createTreeNode();
            }
            source.setChoice(oriSource.isChoice());
            source.setDefaultValue(oriSource.getDefaultValue());
            source.setExpression(oriSource.getExpression());
            source.setGroup(oriSource.isGroup());
            source.setKey(oriSource.isKey());
            source.setLoop(oriSource.isLoop());
            source.setMain(oriSource.isMain());
            source.setName(oriSource.getName());
            source.setNodeType(oriSource.getNodeType());
            source.setNullable(oriSource.isNullable());
            source.setOptional(oriSource.isOptional());
            source.setPattern(oriSource.getPattern());
            source.setSubstitution(oriSource.isSubstitution());
            source.setType(oriSource.getType());
            source.setXpath(oriSource.getXpath());
            return source;
        }

        return null;
    }

    private InputXmlTree getInputXmlTree(INode testNode, EList<InputXmlTree> testInputs, String oriName) {
        ITestContainerProviderService testContainerService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            testContainerService = (ITestContainerProviderService) GlobalServiceRegister.getDefault().getService(
                    ITestContainerProviderService.class);
        }
        for (org.talend.core.model.process.IConnection conn : testNode.getIncomingConnections()) {
            INode source = conn.getSource();
            if (testContainerService != null && testContainerService.isTestCaseComponent(source.getComponent())) {
                if (!source.getIncomingConnections().isEmpty() && conn.getUniqueName().equals(oriName)) {
                    for (InputXmlTree input : testInputs) {
                        if (input.getName().equals(source.getIncomingConnections().get(0).getUniqueName())) {
                            return input;
                        }
                    }
                }
            }
        }
        return null;
    }

    private OutputXmlTree getOutputXmlTree(INode testNode, EList<OutputXmlTree> testOutputs, String oriName) {
        ITestContainerProviderService testContainerService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            testContainerService = (ITestContainerProviderService) GlobalServiceRegister.getDefault().getService(
                    ITestContainerProviderService.class);
        }
        for (org.talend.core.model.process.IConnection conn : testNode.getOutgoingConnections()) {
            INode target = conn.getTarget();
            if (testContainerService != null && testContainerService.isTestCaseComponent(target.getComponent())) {
                if (!target.getOutgoingConnections().isEmpty() && conn.getUniqueName().equals(oriName)) {
                    for (OutputXmlTree output : testOutputs) {
                        if (output.getName().equals(target.getOutgoingConnections().get(0).getUniqueName())) {
                            return output;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean checkExpression(String oriExpression, String testExpression, Map<String, String> inputConnNameMap) {
        if (oriExpression == null && testExpression == null) {
            return false;
        }
        if (oriExpression == null || testExpression == null) {
            return true;
        }
        if (oriExpression.equals(testExpression)) {
            return false;
        }
        XmlMapExpressionManager expressionManager = new XmlMapExpressionManager();
        List<TableEntryLocation> oriMatchedLocations = expressionManager.parseTableEntryLocation(oriExpression);
        List<TableEntryLocation> testMatchedLocations = expressionManager.parseTableEntryLocation(testExpression);
        if (oriMatchedLocations.size() != testMatchedLocations.size()) {
            return true;
        }
        for (int i = 0; i < oriMatchedLocations.size(); i++) {
            TableEntryLocation currentLocation = oriMatchedLocations.get(i);
            TableEntryLocation testLocation = testMatchedLocations.get(i);
            if (!currentLocation.getColumnValue().equals(testLocation.getColumnValue())) {
                return true;
            } else if (inputConnNameMap.get(currentLocation.getTableName()) == null) {
                return true;
            } else if (!inputConnNameMap.get(currentLocation.getTableName()).equals(testLocation.getTableName())) {
                return true;
            }
        }
        if (oriMatchedLocations.isEmpty() && testMatchedLocations.isEmpty()) {
            if (!oriExpression.equals(testExpression)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkChildOutputTreeNode(TreeNode oriEntry, TreeNode testEntry, Map<String, String> inputConnNameMap) {
        if (checkExpression(oriEntry.getExpression(), testEntry.getExpression(), inputConnNameMap)) {
            return true;
        }
        EList<TreeNode> oriList = oriEntry.getChildren();
        EList<TreeNode> testList = testEntry.getChildren();
        if (oriList == null && testList == null) {
            return false;
        }
        if (oriList == null || testList == null) {
            return true;
        }
        if (oriList.size() != testList.size()) {
            return true;
        }
        for (TreeNode oriNode : oriList) {
            boolean found = false;
            for (TreeNode testNode : testList) {
                if (oriNode.getName().equals(testNode.getName())) {
                    found = true;
                    if (found) {
                        if (checkChildOutputTreeNode(oriNode, testNode, inputConnNameMap)) {
                            return true;
                        }
                    }
                    break;
                }
            }
            if (!found) {
                return true;
            }

        }
        return false;
    }

    private Map<String, String> getInputConnectionNameMap(INode testNode) {
        Map<String, String> inputNameMap = new HashMap<String, String>();
        ITestContainerProviderService testContainerService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            testContainerService = (ITestContainerProviderService) GlobalServiceRegister.getDefault().getService(
                    ITestContainerProviderService.class);
        }
        for (org.talend.core.model.process.IConnection conn : testNode.getIncomingConnections()) {
            if (testContainerService != null && testContainerService.isTestCaseComponent(conn.getSource().getComponent())) {
                if (!conn.getSource().getIncomingConnections().isEmpty()) {
                    inputNameMap.put(conn.getUniqueName(), conn.getSource().getIncomingConnections().get(0).getUniqueName());
                }
            }
        }
        return inputNameMap;
    }
}
