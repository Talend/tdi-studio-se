// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.webservice.ui.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.drawing.link.BezierHorizontalLink;
import org.talend.commons.ui.swt.drawing.link.ExtremityEastArrow;
import org.talend.commons.ui.swt.drawing.link.ExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IDrawableLink;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.drawing.link.TableExtremityDescriptor;
import org.talend.commons.ui.swt.linking.TableToTablesLinker;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.webservice.data.InputMappingData;
import org.talend.designer.webservice.data.OutPutMappingData;
import org.talend.designer.webservice.ui.WebServiceExpressionParser;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC Administrator class global comment. Detailled comment
 */

public class WebServiceTableLiner extends TableToTablesLinker<Object, Object> {

    Set<String> sourceInList = new HashSet<String>();

    Set<String> sourceOutList = new HashSet<String>();

    /**
     * DOC Administrator WebServiceTableLiner constructor comment.
     *
     * @param commonParent
     */
    public WebServiceTableLiner(Composite commonParent) {
        super(commonParent);
        // initField();
        initStyleLink();

    }

    private void initStyleLink() {
        IStyleLink sStyleLink = this.getSelectedStyleLink();
        if (sStyleLink == null) {
            sStyleLink = createStandardLink(new Color(getBgDrawableComposite().getDisplay(), 255, 102, 102));
            this.setSelectedStyleLink(sStyleLink);
        }
        IStyleLink uStyleLink = this.getUnselectedStyleLink();
        if (uStyleLink == null) {
            sStyleLink = createStandardLink(getBgDrawableComposite().getDisplay().getSystemColor(SWT.COLOR_GRAY));
            this.setUnselectedStyleLink(sStyleLink);
        }
    }

    public void addLinks(TableItem itemSource, Object data1, Table tableTarget, Object data2, String mark) {
        LinkDescriptor<TableItem, Object, Table, Object> link = new LinkDescriptor<TableItem, Object, Table, Object>(
                new TableExtremityDescriptor(itemSource, data1), new ExtremityLink<Table, Object>(tableTarget, data2));
        link.setStyleLink(createStandardLink(new Color(getBgDrawableComposite().getDisplay(), 255, 102, 102)));
        getLinksManager().addLink(link);

        if (mark.equals("INPUTMAPPING")) {
            sourceInList.add(((IMetadataColumn) itemSource.getData()).getLabel());
        } else if (mark.equals("OUTPUTMAPPING")) {
            if (((OutPutMappingData) itemSource.getData()).getParameterName() != null) {
                String sourseName = ((OutPutMappingData) itemSource.getData()).getParameterName();
                // int m = sourseName.lastIndexOf(".");
                // sourseName = sourseName.substring(m + 1);
                sourceOutList.add(sourseName);
            } else {
                sourceOutList.add(((OutPutMappingData) itemSource.getData()).getParameter().getName());
            }
        }
    }

    public void clearLinks() {
        getLinksManager().clearLinks();
        getBackgroundRefresher().refreshBackground();
    }

    public Set<String> getInputSource() {

        return sourceInList;
    }

    public Set<String> getOutputSource() {

        return sourceOutList;

    }

    protected StyleLink createStandardLink(Color color) {
        StyleLink styleLink = new StyleLink();
        BezierHorizontalLink link = new BezierHorizontalLink(styleLink);
        // LineLinkWithHorizontalConnectors link = new LineLinkWithHorizontalConnectors(styleLink);
        // link.setConnectorWidth(40);
        styleLink.setDrawableLink(link);
        styleLink.setForegroundColor(color);
        styleLink.setLineWidth(2);
        styleLink.setExtremity2(new ExtremityEastArrow(styleLink, 0, 0));
        return styleLink;
    }

    @Override
    public void updateLinksStyleAndControlsSelection(Control currentControl, Boolean flag) {
        boolean isTarget = false;
        if (this.getTargets().contains(currentControl)) {
            isTarget = true;
        } else if (currentControl == this.getSource()) {
            isTarget = false;
        } else {
            throw new IllegalArgumentException("This type of currentControl is unsupported"); //$NON-NLS-1$
        }

        HashSet selectedItems = new HashSet();
        Map itemsToSelect = new HashMap();

        if (isTarget) {
            for (Table table : targets) {
                if (table != currentControl) {
                    table.deselectAll();
                }
            }

            TableItem[] selection = ((Table) currentControl).getSelection();
            for (int i = 0; i < selection.length; i++) {
                TableItem tableItem = selection[i];
                selectedItems.add(tableItem.getData());
            }

        } else {
            // source.deselectAll();

            TableItem[] selection = source.getSelection();
            for (int i = 0; i < selection.length; i++) {
                TableItem treeItem = selection[i];
                selectedItems.add(treeItem.getData());
            }

        }

        List<LinkDescriptor<TableItem, Object, Table, Object>> links = linksManager.getLinks();
        for (LinkDescriptor<TableItem, Object, Table, Object> link : links) {
            IStyleLink styleLink = null;
            IExtremityLink extremity = null;
            IExtremityLink otherExtremity = null;
            if (isTarget) {
                extremity = link.getExtremity2();
                otherExtremity = link.getExtremity1();
            } else {
                extremity = link.getExtremity1();
                otherExtremity = link.getExtremity2();
            }
            boolean currentItemIsSelected = selectedItems.contains(extremity.getDataItem());

            // if (extremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()
            // || otherExtremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()) {
            // styleLink = getSelectedLoopStyleLink();
            // } else {

            if (currentItemIsSelected) {
                styleLink = getSelectedStyleLink();
                if (isTarget) {

                    itemsToSelect.put(otherExtremity.getGraphicalObject(), null);

                } else {

                    Table currentTable = (Table) otherExtremity.getGraphicalObject();
                    List<TableItem> tableItemsToSelect = (List<TableItem>) itemsToSelect.get(currentTable);

                    if (tableItemsToSelect == null) {
                        tableItemsToSelect = new ArrayList<TableItem>();
                        itemsToSelect.put(currentTable, tableItemsToSelect);
                    }
                    TableItem tableItem = TableUtils.getTableItem(currentTable, otherExtremity.getDataItem());
                    tableItemsToSelect.add(tableItem);
                }
            } else {
                styleLink = getUnselectedStyleLink();
            }
            // }
            if (styleLink == null) {
                styleLink = getDefaultStyleLink();
            }
            link.setStyleLink(styleLink);

        }
        if (isTarget) {
            (getSource()).setSelection((TableItem[]) itemsToSelect.keySet().toArray(new TableItem[0]));
        } else {
            Set<Table> set = itemsToSelect.keySet();
            if (set.size() > 0) {
                for (Table table : set) {
                    ArrayList<TableItem> tableItemsToSelect = (ArrayList<TableItem>) itemsToSelect.get(table);
                    table.deselectAll();
                    TableItem[] tableItems = tableItemsToSelect.toArray(new TableItem[0]);
                    if (tableItems[0] != null) {
                        table.setSelection(tableItems);
                    }
                }
            } else {
                // loopTableEditorView.getTable().deselectAll();
                // fieldsTableEditorView.getTable().deselectAll();
            }
            // fieldsTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
        }
        getLinksManager().sortLinks(getDrawingLinksComparator());
        getBackgroundRefresher().refreshBackground();
    }

    public void drawBackground(GC gc) {

        Rectangle clipBounds = source.getBounds();

        List<Table> tables = targets;

        if (tables == null || tables.size() == 0) {
            throw new IllegalStateException();
        }
        Table targetOne = tables.get(0);

        Rectangle targetOneTableBounds = targetOne.getDisplay().map(targetOne, getBgDrawableComposite(), targetOne.getBounds());
        // System.out.println(tableBounds);

        // System.out.println(getBgDrawableComposite());

        int soffset = WindowSystem.isGTK() ? 0 : 20;

        clipBounds.width = targetOneTableBounds.x;
        clipBounds.height += soffset - 4;
        clipBounds.x = 0;
        clipBounds.y = soffset;

        gc.setClipping(clipBounds);

        // -------------------------------------------
        List<LinkDescriptor<TableItem, Object, Table, Object>> links = linksManager.getLinks();
        int lstSize = links.size();

        int xStartBezierLink = findXRightStartBezierLink(source.getItems(), 0);

        Point treeToCommonPoint = display.map(source, getBgDrawableComposite(), new Point(0, 0));
        // System.out.println("treeToCommonPoint=" + treeToCommonPoint);

        int treeItemHeight = source.getItemHeight();

        Rectangle treeBounds = source.getBounds();

        if (WindowSystem.isGTK()) {
            gc.setAdvanced(false);
        } else {
            gc.fillRectangle(treeToCommonPoint.x, treeToCommonPoint.y, treeBounds.width - source.getBorderWidth(),
                    treeBounds.height - source.getBorderWidth());
            // System.out.println(isAntialiasAllowed());
            if (backgroundRefresher.isAntialiasAllowed()) {
                gc.setAntialias(SWT.ON);
            } else {
                gc.setAntialias(SWT.OFF);
            }
        }

        for (int i = 0; i < lstSize; i++) {
            LinkDescriptor<TableItem, Object, Table, Object> link = links.get(i);

            Table table = link.getExtremity2().getGraphicalObject();
            Point tableToCommonPoint = display.map(table, getBgDrawableComposite(), new Point(0, 0));

            IDrawableLink drawableLink = link.getStyleLink().getDrawableLink();
            if (drawableLink == null) {
                drawableLink = getDefaultStyleLink().getDrawableLink();
            }
            drawableLink.getStyle().apply(gc);

            IExtremityLink<TableItem, Object> extremity1 = link.getExtremity1();
            IExtremityLink<Table, Object> extremity2 = link.getExtremity2();

            TableItem treeItem = TableUtils.getTableItem(source, (Object) extremity1.getDataItem());
            if (treeItem == null) {
                continue;
            }
            TableItem firstExpandedAscTreeItem = treeItem;

            Rectangle treeItemBounds = firstExpandedAscTreeItem.getBounds();

            int yStraight = treeToCommonPoint.y + treeItemHeight / 2 + treeItemBounds.y;
            Point pointStartStraight = new Point(treeToCommonPoint.x + treeItemBounds.x + treeItemBounds.width, yStraight);
            Point pointEndStraight = new Point(treeToCommonPoint.x + xStartBezierLink, yStraight);

            if (TableUtils.getTableItem(table, (Object) extremity2.getDataItem()) == null) {
                continue;
            }
            Rectangle tableItemBounds = TableUtils.getTableItem(table, (Object) extremity2.getDataItem()).getBounds();
            Rectangle tableBounds = table.getBounds();

            Point pointEndCentralCurve = backgroundRefresher.convertPointToCommonParentOrigin(new Point(tableItemBounds.x - 2,
                    tableItemBounds.y + table.getItemHeight() / 2 + table.getBorderWidth()), table);

            boolean lineStyleDot = false;

            Point point = display.map(source, getBgDrawableComposite(), new Point(0, 0));

            if (yStraight < point.y || yStraight > point.y + treeBounds.height) {
                lineStyleDot = true;
            }

            if (pointEndCentralCurve.y < tableToCommonPoint.y) {
                pointEndCentralCurve.y = tableToCommonPoint.y;
                lineStyleDot = true;
            }

            if (pointEndCentralCurve.y > tableToCommonPoint.y + tableBounds.height) {
                pointEndCentralCurve.y = tableToCommonPoint.y + tableBounds.height - 2 * table.getBorderWidth();
                lineStyleDot = true;
            }

            if (firstExpandedAscTreeItem == treeItem && !lineStyleDot) {
                gc.setLineStyle(SWT.LINE_SOLID);
            } else {
                gc.setLineStyle(SWT.LINE_DOT);
            }

            Point offset = getOffset();

            if (WindowSystem.isGTK()) {
                pointStartStraight.x += -15;
            }

            gc.drawLine(pointStartStraight.x + offset.x, pointStartStraight.y + offset.y, pointEndStraight.x + offset.x,
                    pointEndStraight.y + offset.y);

            pointEndStraight.x = pointEndStraight.x + offset.x + treeBounds.width - 6;
            pointEndStraight.y += offset.y;

            pointEndCentralCurve.x += offset.x - 6;
            pointEndCentralCurve.y += offset.y;

            if (WindowSystem.isBigSurOrLater()) {
                pointEndCentralCurve.y -= table.getItemHeight();
            }

            drawableLink.setPoint1(pointEndStraight);
            drawableLink.setPoint2(pointEndCentralCurve);

            drawableLink.draw(gc);
        }

    }

    public void updateLinksWhenDelete() {

    }

    public void onXPathValueChanged(Table sourceTable, Table targetTable, String oldValue, String newValue, String inComeName,
            int itemIndex) {
        TableItem tabletraItem = targetTable.getItem(itemIndex);
        boolean needAddLink = true;
        TableItem items[] = sourceTable.getItems();
        List<TableItem> itemList = new ArrayList<TableItem>();
        for (int i = 0; i < items.length; i++) {
            String columnLabel = null;
            IMetadataColumn column = null;
            ParameterInfo parame = null;
            if (items[i].getData() instanceof IMetadataColumn) {
                column = (IMetadataColumn) items[i].getData();
                columnLabel = column.getLabel();
            }
            // else if (items[i].getData() instanceof ParameterInfo) {
            // parame = (ParameterInfo) items[i].getData();
            // columnLabel = parame.getName();
            // }
            else if (items[i].getData() instanceof OutPutMappingData) {
                parame = ((OutPutMappingData) items[i].getData()).getParameter();
                if (!parame.getParameterInfos().isEmpty()) {
                    continue;
                }
                // columnLabel = parame.getName();
                columnLabel = ((OutPutMappingData) items[i].getData()).getParameterName();
                int m = columnLabel.lastIndexOf(".");
                columnLabel = columnLabel.substring(m + 1);
            }

            if (newValue.contains(columnLabel)) {
                // if (!pattern(columnLabel, newValue)) {
                if (inComeName != null) {
                    WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*");
                    Map<String, String> itemNamemap = webParser.parseInTableEntryLocations(newValue);
                    Set<Entry<String, String>> set = itemNamemap.entrySet();
                    Iterator<Entry<String, String>> ite = set.iterator();
                    while (ite.hasNext()) {
                        Entry<String, String> entry = ite.next();
                        String columnValue = entry.getKey();
                        String rowValue = entry.getValue();

                        if (!isPatternWord(inComeName + "." + columnLabel, rowValue + "." + columnValue)) {
                            linksManager.removeLinksFromDataItem2(tabletraItem.getData());
                            if (tabletraItem.getData() instanceof InputMappingData) {
                                InputMappingData data = (InputMappingData) tabletraItem.getData();
                                List<IMetadataColumn> list = data.getMetadataColumnList();
                                if (column != null) {
                                    list.remove(column);
                                }
                            }
                            needAddLink = false;
                            for (TableItem item : itemList) {
                                addLinks(item, item.getData(), targetTable, tabletraItem.getData(), "INPUTMAPPING");
                            }
                            getBackgroundRefresher().refreshBackground();
                            // return;
                        }

                    }

                } else {
                    WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*\\w+(\\[\\d+?\\])?\\s*");
                    Set<String> set = webParser.parseOutTableEntryLocations(newValue);
                    Iterator<String> ite = set.iterator();
                    while (ite.hasNext()) {
                        String columnValue = ite.next();
                        // if (columnValue.equals(columnLabel)) {
                        if (!isPatternWord(columnLabel, columnValue)) {
                            linksManager.removeLinksFromDataItem2(tabletraItem.getData());
                            OutPutMappingData data = (OutPutMappingData) tabletraItem.getData();
                            if (parame != null) {
                                data.getParameterList().remove(parame);
                            }

                            needAddLink = false;
                            for (TableItem item : itemList) {
                                addLinks(item, item.getData(), targetTable, tabletraItem.getData(), "OUTPUTMAPPING");
                            }
                            getBackgroundRefresher().refreshBackground();
                            // return;
                            // }
                        }

                    }
                }

                if (true) {
                    // boolean mark = linksManager.haveTheLink(tabletraItem.getData());
                    // if (!mark) {
                    if (inComeName != null) {
                        WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*");
                        Map<String, String> itemNamemap = webParser.parseInTableEntryLocations(newValue);
                        Set<Entry<String, String>> set = itemNamemap.entrySet();
                        Iterator<Entry<String, String>> ite = set.iterator();
                        while (ite.hasNext()) {
                            Entry<String, String> entry = ite.next();
                            String columnValue = entry.getKey();
                            String rowValue = entry.getValue();
                            if (isPatternWord(inComeName + "." + columnLabel, rowValue + "." + columnValue)) {
                                itemList.add(items[i]);
                                addLinks(items[i], items[i].getData(), targetTable, tabletraItem.getData(), "INPUTMAPPING");
                                getBackgroundRefresher().refreshBackground();
                                if (tabletraItem.getData() instanceof InputMappingData) {
                                    InputMappingData data = (InputMappingData) tabletraItem.getData();
                                    List<IMetadataColumn> list = data.getMetadataColumnList();
                                    if (column != null && !list.contains(column)) {
                                        list.add(column);
                                    }
                                }
                            }
                        }
                    } else {
                        WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*\\w+(\\[\\d+?\\])?\\s*");
                        Set<String> set = webParser.parseOutTableEntryLocations(newValue);
                        Iterator<String> ite = set.iterator();
                        while (ite.hasNext()) {
                            String columnValue = ite.next();
                            if (isPatternWord(columnLabel, columnValue)) {
                                itemList.add(items[i]);
                                addLinks(items[i], items[i].getData(), targetTable, tabletraItem.getData(), "OUTPUTMAPPING");
                                getBackgroundRefresher().refreshBackground();
                            }
                        }
                    }

                }
            } else if (oldValue.contains(columnLabel)) {
                linksManager.removeLinksFromDataItem2(tabletraItem.getData());
                if (tabletraItem.getData() instanceof InputMappingData) {
                    InputMappingData data = (InputMappingData) tabletraItem.getData();
                    List<IMetadataColumn> list = data.getMetadataColumnList();
                    if (column != null) {
                        list.remove(column);
                    }
                } else if (tabletraItem.getData() instanceof OutPutMappingData) {
                    OutPutMappingData data = (OutPutMappingData) tabletraItem.getData();
                    if (parame != null) {
                        data.getParameterList().remove(parame);
                    }
                }
                for (TableItem item : itemList) {
                    if (inComeName != null) {
                        addLinks(item, item.getData(), targetTable, tabletraItem.getData(), "INPUTMAPPING");
                    } else {
                        addLinks(item, item.getData(), targetTable, tabletraItem.getData(), "OUTPUTMAPPING");
                    }
                }
                getBackgroundRefresher().refreshBackground();
            }
        }
        // createFieldLinks(newValue, tableItem, null);

    }

    public boolean isPatternWord(String oldValue, String newValue) {
        if (newValue == null || newValue.length() == 0) {
            return false;
        }
        if (newValue.length() < oldValue.length()) {
            return false;
        }
        if (newValue.equals(oldValue)) {
            return true;
        }
        if (!newValue.contains(oldValue)) {
            return false;
        }
        int pos = newValue.indexOf(oldValue);
        boolean before = false, after = false;
        if (pos > 0) {
            before = isWordandNum(String.valueOf(newValue.charAt(pos - 1)));
        } else {
            if (newValue.endsWith(String.valueOf(oldValue.charAt(oldValue.length() - 1)))) {
                before = true;
            } else {
                before = false;
            }
        }
        if ((pos + oldValue.length()) <= newValue.length() - 1) {
            after = isWordandNum(String.valueOf(newValue.charAt(pos + oldValue.length())));
        } else {
            if (newValue.endsWith(String.valueOf(oldValue.charAt(oldValue.length() - 1)))) {
                after = true;
            } else {
                after = false;
            }
        }
        return before && after;
    }

    /**
     *
     * @param one
     * @return
     */
    private boolean isWordandNum(String one) {
        String reg = "[^\\w]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(one);
        return matcher.matches();
    }

    public boolean pattern(String oldValue, String newValue) {
        String reg = "[^a-zA-Z0-9]" + oldValue + "[^a-zA-Z0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(newValue);
        return matcher.matches();
    }

    private void createFieldLinks(final String relativeXpathPrm, final TableItem tableItemTarget, IProgressMonitor progressMonitor) {

        if (relativeXpathPrm == null || relativeXpathPrm.trim().length() == 0) {
            return;
        }

        boolean breakAll = false;

        boolean expressionIsAbsolute = false;
        if (relativeXpathPrm.trim().startsWith("/")) { //$NON-NLS-1$
            expressionIsAbsolute = true;
        }

        String relativeXpath = relativeXpathPrm;
        boolean limitLoopExceeded = false;
        boolean limitFieldExceeded = false;

        Set<String> alreadyProcessedXPath = new HashSet<String>();

    }
}
