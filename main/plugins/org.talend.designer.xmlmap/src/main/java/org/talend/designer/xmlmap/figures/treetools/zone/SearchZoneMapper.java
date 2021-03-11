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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.ui.dialogs.SearchPattern;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityFigure;
import org.talend.designer.gefabstractmap.figures.var.VarEntityFigure;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.resource.EntryState;
import org.talend.designer.xmlmap.figures.InputXmlTreeFigure;
import org.talend.designer.xmlmap.figures.OutputXmlTreeFigure;
import org.talend.designer.xmlmap.figures.treeNode.XmlmapTreeNodeFigure;
import org.talend.designer.xmlmap.figures.treesettings.XmlMapFilterContainer;
import org.talend.designer.xmlmap.figures.varnode.VarNodeFigure;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.parts.InputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.parts.OutputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.parts.VarNodeEditPart;
import org.talend.designer.xmlmap.parts.XmlMapDataEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class SearchZoneMapper {

    private MapperManager mapperManager;

    private boolean isHightlightAll = false;

    private SearchPattern matcher = null;

    public SearchZoneMapper(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
        matcher = new SearchPattern();
    }

    public void search(Map<Integer, Map<Integer, Figure>> searchMaps, String searchValue) {
        if (searchValue.equals("") || searchValue == null) {
            return;
        }

        List<InputXmlTree> inputTrees = mapperManager.getExternalData().getInputTrees();
        List<VarTable> varTables = mapperManager.getExternalData().getVarTables();
        List<OutputXmlTree> outputTrees = mapperManager.getExternalData().getOutputTrees();
        matcher.setPattern("*" + searchValue.trim() + "*");
        int index = -1;

        // for the InputTables
        for (InputXmlTree inputXmlTree : inputTrees) {
            // ExpressionFilter
            if (inputXmlTree.getExpressionFilter() != null && matcher.matches(inputXmlTree.getExpressionFilter())) {
                EList<Adapter> adapter = inputXmlTree.eAdapters();
                if (adapter.size() > 0) {
                    if (adapter.get(0) instanceof InputXmlTreeEditPart) {
                        InputXmlTreeEditPart inputXmlTreeEditPart = (InputXmlTreeEditPart) adapter.get(0);
                        if (inputXmlTreeEditPart != null && inputXmlTreeEditPart.getFigure() != null
                                && inputXmlTreeEditPart.getFigure() instanceof InputXmlTreeFigure) {
                            InputXmlTreeFigure inputXmlTreeFigure = (InputXmlTreeFigure) inputXmlTreeEditPart.getFigure();
                            Map<Integer, Figure> map = new HashMap<Integer, Figure>();
                            map.put(0, inputXmlTreeFigure.getFilterContainer());
                            index++;
                            searchMaps.put(index, map);
                        }
                    }
                }
            }
            // TreeNode
            for (TreeNode node : inputXmlTree.getNodes()) {
                // id_Document type
                if (XmlMapUtil.DOCUMENT.equals(node.getType())) {
                    for (TreeNode nodeTemp : XmlMapUtil.getFlatChildrenList(node)) {
                        if (getMatcherNodeFigure(nodeTemp).size() > 0) {
                            index++;
                            searchMaps.put(index, getMatcherNodeFigure(nodeTemp));
                        }
                    }
                }
                if (getMatcherNodeFigure(node).size() > 0) {
                    index++;
                    searchMaps.put(index, getMatcherNodeFigure(node));
                }
            }
        }

        // for the VarsTables
        for (VarTable varTable : varTables) {
            for (VarNode node : varTable.getNodes()) {
                if (getMatcherNodeFigure(node).size() > 0) {
                    index++;
                    searchMaps.put(index, getMatcherNodeFigure(node));
                }
            }
        }

        // for the OutputTables
        for (OutputXmlTree outputXmlTree : outputTrees) {
            // ExpressionFilter
            if (outputXmlTree.getExpressionFilter() != null && matcher.matches(outputXmlTree.getExpressionFilter())) {
                EList<Adapter> adapter = outputXmlTree.eAdapters();
                if (adapter.size() > 0) {
                    if (adapter.get(0) instanceof OutputXmlTreeEditPart) {
                        OutputXmlTreeEditPart outputXmlTreeEditPart = (OutputXmlTreeEditPart) adapter.get(0);
                        if (outputXmlTreeEditPart != null && outputXmlTreeEditPart.getFigure() != null
                                && outputXmlTreeEditPart.getFigure() instanceof OutputXmlTreeFigure) {
                            OutputXmlTreeFigure outputXmlTreeFigure = (OutputXmlTreeFigure) outputXmlTreeEditPart.getFigure();
                            Map<Integer, Figure> map = new HashMap<Integer, Figure>();
                            map.put(0, outputXmlTreeFigure.getFilterContainer());
                            index++;
                            searchMaps.put(index, map);
                        }
                    }
                }
            }
            // OutputTreeNode
            for (OutputTreeNode node : outputXmlTree.getNodes()) {
                // id_Document type
                if (XmlMapUtil.DOCUMENT.equals(node.getType())) {
                    for (TreeNode nodeTemp : XmlMapUtil.getFlatChildrenList(node)) {
                        if (getMatcherNodeFigure(nodeTemp).size() > 0) {
                            index++;
                            searchMaps.put(index, getMatcherNodeFigure(nodeTemp));
                        }
                    }
                } else {
                    if (getMatcherNodeFigure(node).size() > 0) {
                        index++;
                        searchMaps.put(index, getMatcherNodeFigure(node));
                    }
                }
            }
        }
    }

    public Integer selectHightlight(Map<Integer, Map<Integer, Figure>> searchMaps, Integer selectKey, String option) {
        if (searchMaps.containsKey(selectKey)) {
            if (option.equals("next") && selectKey + 1 < searchMaps.size()) {
                Map<Integer, Figure> map = searchMaps.get(selectKey);
                Map<Integer, Figure> mapNext = searchMaps.get(selectKey + 1);
                if (isHightlightAll) {
                    setEntryState(mapperManager, EntryState.HIGHLIGHTALL, map.get(0));
                    setEntryState(mapperManager, EntryState.SEARCH_HIGHLIGHT, mapNext.get(0));
                } else {
                    setEntryState(mapperManager, EntryState.NONE, map.get(0));
                    setEntryState(mapperManager, EntryState.SEARCH_HIGHLIGHT, mapNext.get(0));
                }
                // move scrollBarZone at selected TableItem
                moveScrollBarZoneAtSelectedTableItem(mapNext.get(0));
                return selectKey + 1;
            } else if (option.equals("previous") && selectKey > 0) {
                Map<Integer, Figure> map = searchMaps.get(selectKey);
                Map<Integer, Figure> mapNext = searchMaps.get(selectKey - 1);
                if (isHightlightAll) {
                    setEntryState(mapperManager, EntryState.HIGHLIGHTALL, map.get(0));
                    setEntryState(mapperManager, EntryState.SEARCH_HIGHLIGHT, mapNext.get(0));
                } else {
                    setEntryState(mapperManager, EntryState.NONE, map.get(0));
                    setEntryState(mapperManager, EntryState.SEARCH_HIGHLIGHT, mapNext.get(0));
                }
                // move scrollBarZone at selected TableItem
                moveScrollBarZoneAtSelectedTableItem(mapNext.get(0));
                return selectKey - 1;
            } else if (option.equals("first")) {
                setEntryState(mapperManager, EntryState.SEARCH_HIGHLIGHT, searchMaps.get(0).get(0));
                // move scrollBarZone at selected TableItem
                moveScrollBarZoneAtSelectedTableItem(searchMaps.get(0).get(0));
                return 0;
            }
        } else {
            searchMaps.clear();
            return 0;
        }
        return selectKey;
    }

    public void hightlightAll(Map<Integer, Map<Integer, Figure>> searchMaps, boolean isHightlight) {
        Iterator iter = searchMaps.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Map<Integer, Figure> map = (Map<Integer, Figure>) entry.getValue();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                if (isHightlight) {
                    setEntryState(mapperManager, EntryState.HIGHLIGHTALL, (Figure) e.getValue());
                } else {
                    setEntryState(mapperManager, EntryState.NONE, (Figure) e.getValue());
                }
            }
        }
    }

    public void setEntryState(MapperManager mapperManager, EntryState entryState, Figure entry) {
        if (entry != null) {
            if (entry instanceof XmlmapTreeNodeFigure) {
                XmlmapTreeNodeFigure xmlMapTreeNodeFigure = (XmlmapTreeNodeFigure) entry;
                if (xmlMapTreeNodeFigure != null) {
                    if (xmlMapTreeNodeFigure.getExpressionFigure() != null
                            && matcher.matches(xmlMapTreeNodeFigure.getExpressionFigure().getText())) {
                        xmlMapTreeNodeFigure.getExpressionFigure().setOpaque(true);
                        xmlMapTreeNodeFigure.getExpressionFigure().setBackgroundColor(entryState.getColor());
                    }
                    if (xmlMapTreeNodeFigure.getTreeNode() != null
                            && matcher.matches(xmlMapTreeNodeFigure.getTreeNode().getName())) {
                        if (XmlMapUtil.isSubElementOfDocument(xmlMapTreeNodeFigure.getTreeNode())) {
                            if (!xmlMapTreeNodeFigure.getTreeNode().getName().endsWith("(choice)")) {
                                xmlMapTreeNodeFigure.getBranchContent().setOpaque(true);
                                xmlMapTreeNodeFigure.getBranchContent().setBackgroundColor(entryState.getColor());
                            }
                        } else {
                            xmlMapTreeNodeFigure.setOpaque(true);
                            xmlMapTreeNodeFigure.setBackgroundColor(entryState.getColor());
                            xmlMapTreeNodeFigure.setOpaque(false);
                            if (xmlMapTreeNodeFigure.getExpressionFigure() != null
                                    && !matcher.matches(xmlMapTreeNodeFigure.getExpressionFigure().getText())) {
                                // xmlMapTreeNodeFigure.getExpressionFigure().setOpaque(true);
                                // xmlMapTreeNodeFigure.getExpressionFigure().setBackgroundColor(EntryState.NONE.getColor());
                            }
                        }
                    }
                }
            } else if (entry instanceof VarEntityFigure) {
                VarEntityFigure varEntityFigure = (VarEntityFigure) entry;
                if (varEntityFigure != null) {
                    if (varEntityFigure.getExpression() != null && matcher.matches(varEntityFigure.getExpression().getText())) {
                        varEntityFigure.getExpression().setOpaque(true);
                        varEntityFigure.getExpression().setBackgroundColor(entryState.getColor());
                    }
                    if (varEntityFigure.getVarName() != null && matcher.matches(varEntityFigure.getVarName())) {
                        varEntityFigure.setOpaque(true);
                        varEntityFigure.setBackgroundColor(entryState.getColor());
                        if (varEntityFigure.getExpression() == null
                                || !matcher.matches(varEntityFigure.getExpression().getText())) {
                            varEntityFigure.getExpression().setOpaque(true);
                            varEntityFigure.getExpression().setBackgroundColor(EntryState.NONE.getColor());
                        }
                        varEntityFigure.getTypeFigure().setOpaque(true);
                        varEntityFigure.getTypeFigure().setBackgroundColor(EntryState.NONE.getColor());
                    }
                }
            } else if (entry instanceof XmlMapFilterContainer) {
                XmlMapFilterContainer filterText = (XmlMapFilterContainer) entry;
                if (filterText != null && filterText.getTextArea() != null) {
                    filterText.getTextArea().setOpaque(true);
                    filterText.getTextArea().setBackgroundColor(entryState.getColor());
                }
            }
        }
    }

    public void moveScrollBarZoneAtSelectedTableItem(Figure entry) {
        if (entry != null) {
            Rectangle bounds = entry.getBounds();
            int selection = bounds.y - 100;
            if (entry instanceof XmlmapTreeNodeFigure) {
                XmlmapTreeNodeFigure xmlMapTreeNodeFigure = (XmlmapTreeNodeFigure) entry;
                TreeNode treeNode = xmlMapTreeNodeFigure.getTreeNode();
                if (treeNode != null) {
                    for (Adapter adapter : treeNode.eAdapters()) {
                        TreeNodeEditPart part = (TreeNodeEditPart) adapter;
                        XmlMapDataEditPart xmlMapDataEditPart = part.getMapDataEditPart();
                        if (adapter instanceof OutputTreeNodeEditPart) {
                            Viewport viewport = xmlMapDataEditPart.getOutputScroll().getViewport();
                            viewport.setViewLocation(viewport.getViewLocation().translate(bounds.x, selection));
                        } else if (adapter instanceof TreeNodeEditPart) {
                            Viewport viewport = xmlMapDataEditPart.getInputScroll().getViewport();
                            viewport.setViewLocation(viewport.getViewLocation().translate(bounds.x, selection));
                        }
                    }
                }
            } else if (entry instanceof VarNodeFigure) {
                VarNodeFigure varNodeFigure = (VarNodeFigure) entry;
                VarNode varNode = varNodeFigure.getVarNode();
                if (varNode != null) {
                    for (Adapter adapter : varNode.eAdapters()) {
                        VarNodeEditPart part = (VarNodeEditPart) adapter;
                        XmlMapDataEditPart xmlMapDataEditPart = part.getMapDataEditPart();
                        Viewport viewport = xmlMapDataEditPart.getVarScroll().getViewport();
                        viewport.setViewLocation(viewport.getViewLocation().translate(bounds.x, selection));
                    }
                }
            }
        }
    }

    public Integer getSelectedKeyAtSelectedTableItem(Map<Integer, Map<Integer, Figure>> searchMaps) {
        Integer selectKey = 0;
        IFigure figure = mapperManager.getSelectedFigure();
        if (figure != null) {
            Iterator iter = searchMaps.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Map<Integer, Figure> map = (Map<Integer, Figure>) entry.getValue();
                if (map.containsValue(figure)) {
                    Iterator it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry e = (Map.Entry) it.next();
                        if (e.getValue() != null && e.getValue() instanceof IFigure) {
                            IFigure figureTemp = (IFigure) e.getValue();
                            if (figure.equals(figureTemp)) {
                                mapperManager.setSelectedFigure(null);
                                return (Integer) entry.getKey();
                            }
                        }
                    }
                }
            }
        }
        return selectKey;
    }

    private Map<Integer, Figure> getMatcherNodeFigure(AbstractNode node) {
        Map<Integer, Figure> map = new HashMap<Integer, Figure>();
        int i = -1;
        TableEntityFigure figure = null;
        if (node != null) {
            EList<Adapter> adapter = node.eAdapters();
            if (adapter.size() > 0) {
                if (adapter.get(0) instanceof TableEntityPart) {
                    TableEntityPart tableEntityPart = (TableEntityPart) adapter.get(0);
                    if (tableEntityPart != null && tableEntityPart.getFigure() != null
                            && tableEntityPart.getFigure() instanceof TableEntityFigure) {
                        figure = (TableEntityFigure) tableEntityPart.getFigure();
                    }
                }
            }
            if (node.getExpression() != null && matcher.matches(node.getExpression())) {
                i++;
                map.put(i, figure);
            } else if (node.getName() != null && matcher.matches(node.getName())) {
                i++;
                map.put(i, figure);
            }
        }
        return map;
    }

    public boolean isHightlightAll() {
        return this.isHightlightAll;
    }

    public void setHightlightAll(boolean isHightlightAll) {
        this.isHightlightAll = isHightlightAll;
    }
}
