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
package org.talend.designer.xmlmap.figures.treetools.zone;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class AutoMapper {

    private XmlMapData xmlMapData;

    /**
     * Map all empty output expression cells if possible.
     *
     * @param manager
     */
    public AutoMapper(XmlMapData xmlMapData) {
        this.xmlMapData = xmlMapData;
    }

    /**
     * DOC amaumont Comment method "map".
     */
    public void map() {
        EList<InputXmlTree> inputTrees = xmlMapData.getInputTrees();
        EList<OutputXmlTree> outputTrees = xmlMapData.getOutputTrees();

        /*
         * non-document node , if name is the same ,automap document node , check xpath first , if can't find ,check the
         * name
         */
        for (OutputXmlTree outputTree : outputTrees) {
            List<TreeNode> outputEntries = getAllEntities(outputTree);

            for (TreeNode outputEntry : outputEntries) {
                if ((outputEntry.getExpression() == null || "".equals(outputEntry.getExpression()))
                        && XmlMapUtil.isExpressionEditable(outputEntry)) {
                    String xpath = outputEntry.getXpath();
                    String outputNodePath = xpath.substring(outputTree.getName().length() + 1, xpath.length());

                    TreeNode inputSameXpath = null;
                    TreeNode inputSameName = null;
                    out: for (InputXmlTree inputTable : inputTrees) {
                        List<TreeNode> inputColumnEntries = getAllEntities(inputTable);
                        in: for (TreeNode inputEntry : inputColumnEntries) {
                            // check if input tree node can be mapped
                            if (XmlMapUtil.isExpressionEditable(inputEntry)) {
                                String inputXpath = inputEntry.getXpath();
                                String inputNodePath = inputXpath.substring(inputTable.getName().length() + 1,
                                        inputXpath.length());
                                if (outputNodePath.equals(inputNodePath)) {
                                    inputSameXpath = inputEntry;
                                    break out;
                                }
                                // if the same name , find the first matched node , don't overwrite
                                if (inputSameName == null && outputEntry.getName() != null
                                        && outputEntry.getName().equals(inputEntry.getName())) {
                                    inputSameName = inputEntry;
                                }

                            }

                        }
                    }

                    TreeNode inputEntryToMap = null;
                    if (inputSameXpath != null) {
                        inputEntryToMap = inputSameXpath;
                    } else if (inputSameName != null) {
                        inputEntryToMap = inputSameName;
                    }
                    if (inputEntryToMap != null) {
                        String expression = outputEntry.getExpression();
                        String convertToExpression = XmlMapUtil.convertToExpression(inputEntryToMap.getXpath());
                        if (expression != null && expression.indexOf(convertToExpression) != -1) {
                            continue;
                        } else {
                            if (expression == null) {
                                expression = "";
                            }
                            expression = expression + convertToExpression;
                        }
                        outputEntry.setExpression(expression);
                        Connection conn = XmlmapFactory.eINSTANCE.createConnection();
                        conn.setSource(inputEntryToMap);
                        conn.setTarget(outputEntry);
                        outputEntry.getIncomingConnections().add(conn);
                        inputEntryToMap.getOutgoingConnections().add(conn);
                        xmlMapData.getConnections().add(conn);
                    }

                }
            }

        }

    }

    private List<TreeNode> getAllEntities(AbstractInOutTree abstractTree) {
        List<TreeNode> allEntities = new ArrayList<TreeNode>();
        if (abstractTree instanceof InputXmlTree) {
            EList<TreeNode> nodes = ((InputXmlTree) abstractTree).getNodes();
            if (!nodes.isEmpty()) {
                allEntities.addAll(nodes);
                addChildEntities(allEntities, nodes);
            }
        } else if (abstractTree instanceof OutputXmlTree) {
            EList<OutputTreeNode> nodes = ((OutputXmlTree) abstractTree).getNodes();
            if (!nodes.isEmpty()) {
                allEntities.addAll(nodes);
                addChildEntities(allEntities, nodes);
            }
        }

        return allEntities;
    }

    private void addChildEntities(List<TreeNode> allEntities, List<? extends TreeNode> parentNodes) {
        for (TreeNode parent : parentNodes) {
            EList<TreeNode> children = parent.getChildren();
            if (!children.isEmpty()) {
                allEntities.addAll(children);
                addChildEntities(allEntities, children);
            }
        }

    }

}
