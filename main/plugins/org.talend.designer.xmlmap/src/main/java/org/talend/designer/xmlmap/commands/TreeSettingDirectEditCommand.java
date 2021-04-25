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
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;
import org.talend.designer.xmlmap.figures.treesettings.TreeSettingsConstant;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.model.tree.IUILookupMode;
import org.talend.designer.xmlmap.model.tree.IUIMatchingMode;
import org.talend.designer.xmlmap.model.tree.XML_MAP_LOOKUP_MODE;
import org.talend.designer.xmlmap.model.tree.XML_MAP_MATCHING_MODE;
import org.talend.designer.xmlmap.ui.expressionutil.TableEntryLocation;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TreeSettingDirectEditCommand extends DirectEditCommand {

    private Object model;

    private Object newValue;

    private DirectEditType type;

    public TreeSettingDirectEditCommand(Object model, DirectEditType type, Object newValue) {
        this.model = model;
        this.newValue = newValue;
        this.type = type;
    }

    @Override
    public void execute() {
        try {
            if (model instanceof InputXmlTree) {
                InputXmlTree inputTree = (InputXmlTree) model;
                if (type != null) {
                    switch (type) {
                    case LOOKUP_MODEL:
                        inputTree.setLookupMode(getLookupModelByLabel((String) newValue));
                        break;
                    case MATCH_MODEL:
                        inputTree.setMatchingMode(getMatchModelByLabel((String) newValue));
                        break;
                    case JOIN_MODEL:
                        if (TreeSettingsConstant.INNER_JOIN.equals(newValue)) {
                            inputTree.setInnerJoin(true);
                        } else {
                            inputTree.setInnerJoin(false);
                        }
                        break;
                    case PERSISTENT_MODEL:
                        inputTree.setPersistent(Boolean.valueOf((String) newValue));
                        break;
                    case EXPRESSION_FILTER:
                        calculateFilterConnections(inputTree, (String) newValue);
                        inputTree.setExpressionFilter((String) newValue);
                        break;
                    default:
                        break;
                    }
                }
            } else if (model instanceof OutputXmlTree) {
                OutputXmlTree outputTree = (OutputXmlTree) model;
                if (type != null) {
                    switch (type) {
                    case OUTPUT_REJECT:
                        outputTree.setReject(Boolean.valueOf((String) newValue));
                        break;
                    case LOOK_UP_INNER_JOIN_REJECT:
                        outputTree.setRejectInnerJoin(Boolean.valueOf((String) newValue));
                        break;
                    case EXPRESSION_FILTER:
                        calculateFilterConnections(outputTree, (String) newValue);
                        outputTree.setExpressionFilter((String) newValue);
                        break;
                    case ALL_IN_ONE:
                        outputTree.setAllInOne(Boolean.valueOf((String) newValue));
                        break;
                    case ENABLE_EMPTY_ELEMENT:
                        outputTree.setEnableEmptyElement(Boolean.valueOf((String) newValue));
                    }
                }
            }

        } catch (PatternSyntaxException ex) {
            // Syntax error in the regular expression
        }
    }

    private void calculateFilterConnections(AbstractInOutTree abstractTree, String newValue) {
        XmlMapData mapperData = (XmlMapData) abstractTree.eContainer();

        List<TableEntryLocation> matchedLocations = expressionManager.parseTableEntryLocation((String) newValue);
        EList<FilterConnection> connections = abstractTree.getFilterIncomingConnections();

        List usefullConnections = new ArrayList();

        if (!matchedLocations.isEmpty()) {
            for (int i = 0; i < matchedLocations.size(); i++) {
                TableEntryLocation currentLocation = matchedLocations.get(i);
                boolean found = false;
                for (FilterConnection conn : connections) {
                    TableEntryLocation sourceLocation = null;
                    if (conn.getSource() instanceof TreeNode) {
                        sourceLocation = expressionManager.parseTableEntryLocation(
                                XmlMapUtil.convertToExpression(((TreeNode) conn.getSource()).getXpath())).get(0);

                    } else if (conn.getSource() instanceof VarNode) {
                        VarNode varNode = (VarNode) conn.getSource();
                        sourceLocation = new TableEntryLocation(((VarTable) varNode.eContainer()).getName(), varNode.getName());
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
                        if (abstractTree instanceof OutputXmlTree) {
                            findFromVar = true;
                        }
                        AbstractNode sourceNode = findConnectionSource(mapperData, currentLocation,
                                XmlMapUtil.getXPathLength(convertToXpath), findFromVar);
                        if (sourceNode != null) {
                            FilterConnection connection = null;
                            connection = XmlmapFactory.eINSTANCE.createFilterConnection();
                            sourceNode.getFilterOutGoingConnections().add(connection);
                            abstractTree.getFilterIncomingConnections().add(connection);

                            connection.setSource(sourceNode);
                            connection.setTarget(abstractTree);
                            mapperData.getConnections().add(connection);
                            usefullConnections.add(connection);
                        }
                    }
                }
            }

            List<FilterConnection> copyOfConnections = new ArrayList<FilterConnection>(connections);
            copyOfConnections.removeAll(usefullConnections);
            for (FilterConnection connection : copyOfConnections) {
                if (connection.getSource() != null) {
                    if (connection.getSource().getFilterOutGoingConnections().contains(connection)) {
                        connection.getSource().getFilterOutGoingConnections().remove(connection);
                        mapperData.getConnections().remove(connection);
                    }
                }
            }
            abstractTree.getFilterIncomingConnections().removeAll(copyOfConnections);

        } else if (!connections.isEmpty()) {

            for (FilterConnection connection : connections) {
                if (connection.getSource() != null) {
                    if (connection.getSource().getFilterOutGoingConnections().contains(connection)) {
                        connection.getSource().getFilterOutGoingConnections().remove(connection);
                        mapperData.getConnections().remove(connection);
                    }
                }
            }

            abstractTree.getFilterIncomingConnections().removeAll(connections);
        }
    }

    private String getLookupModelByLabel(String label) {
        IUILookupMode[] availableLookups = { XML_MAP_LOOKUP_MODE.LOAD_ONCE, XML_MAP_LOOKUP_MODE.RELOAD,
                XML_MAP_LOOKUP_MODE.CACHE_OR_RELOAD };
        for (IUILookupMode mode : availableLookups) {
            if (mode.getLabel().equals(label.trim())) {
                return mode.toString();
            }
        }
        return "";
    }

    private String getMatchModelByLabel(String label) {
        IUIMatchingMode[] allMatchingModel = XML_MAP_MATCHING_MODE.values();
        for (IUIMatchingMode mode : allMatchingModel) {
            if (mode.getLabel().equals(label)) {
                return mode.toString();
            }
        }
        return "";
    }

}
