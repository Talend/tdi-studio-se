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
package org.talend.designer.fileoutputxml.ui.edit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.ui.swt.drawing.link.ExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.ItemExtremityDescriptor;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.linking.TableToTreeLinker;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.designer.fileoutputxml.managers.FOXManager;
import org.talend.metadata.managment.ui.wizard.metadata.xml.XmlExtractorBgRefresher;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.repository.i18n.Messages;

/**
 * bqian This is the linker for connecting schema instructure to xml's. <br/>
 *
 * $Id: Schema2XMLLinker.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class Schema2XMLLinker extends TableToTreeLinker<Object, Object> {

    private TreeViewer xmlViewer;

    private StyleLink selectedLoopStyleLink;

    private Comparator<LinkDescriptor<Item, Object, Tree, Object>> drawingLinksComparator;

    private Color selectedLoopLinkColor;

    private Color selectedRelativeLinkColor;

    private Color transparentBgColor;

    private Color originalBgColor;

    private FOXManager manager;

    /**
     * amaumont XmlToMetadataTableLinker constructor comment.
     *
     * @param commonParent common main parent of tree and table, it and its children should have backgoundMode
     * configured with SWT.INHERIT_FORCE, same configuration for parents of tree and table.
     * @param tree
     * @param xmlViewer
     * @param table
     */
    public Schema2XMLLinker(Composite commonParent) {
        super(commonParent);
    }

    public void init(Table schemaTable, TreeViewer xmlViewer) {
        init(schemaTable, xmlViewer.getTree(), new XmlExtractorBgRefresher(this));
        this.xmlViewer = xmlViewer;
        init();
    }

    public FOXManager getManager() {
        return this.manager;
    }

    public void setManager(FOXManager manager) {
        this.manager = manager;
    }

    /**
     * Comment method "init".
     *
     * @param tree
     */
    private void init() {

        Display display = getBgDrawableComposite().getDisplay();

        initColors(display);

        if (WindowSystem.isBigSurOrLater()) {
            getBackgroundRefresher().setBackgroundColor(originalBgColor);
            getBgDrawableComposite().setBackground(transparentBgColor);
        }

        setUnselectedStyleLink(createStandardLink(display.getSystemColor(SWT.COLOR_BLUE)));

        getSelectedRelativeStyleLink();

        initListeners();
        // createLinks();

    }

    /**
     * amaumont Comment method "initColors".
     *
     * @param display
     */
    private void initColors(Display display) {
        // selectedLoopLinkColor = new Color(display, 255, 131, 255);
        selectedLoopLinkColor = new Color(display, 110, 168, 255);// light blue
        selectedRelativeLinkColor = new Color(display, 110, 168, 0);
        transparentBgColor = new Color(display, 0, 0, 0, 0);
        originalBgColor = getBgDrawableComposite().getBackground();

        getSource().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                selectedLoopLinkColor.dispose();
                selectedRelativeLinkColor.dispose();
                transparentBgColor.dispose();
                originalBgColor.dispose();
                getSource().removeDisposeListener(this);
            }
        });

    }

    /**
     * amaumont Comment method "initListeners".
     */
    private void initListeners() {
        new Schema2XMLDragAndDropHandler(this);
    }

    /**
     * amaumont Comment method "createLinks".
     */
    public void createLinks() {

        removeAllLinks();
        getBackgroundRefresher().refreshBackground();

        ProgressDialog progressDialog = new ProgressDialog(getSource().getShell(), 1000) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) {
                List<FOXTreeNode> treeData = getManager().getTreeData(getManager().getCurrentSchema());
                createLoopLinks(treeData);
                getBackgroundRefresher().refreshBackground();
            }
        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            // Nothing to do
            ExceptionHandler.process(e);
        }

    }

    public void valuedChanged(Widget widget) {
        onXPathValueChanged(widget);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.linking.TreeToTableLinker#drawBackground(org.eclipse.swt.graphics.GC)
     */
    @Override
    public void drawBackground(GC gc) {
        super.drawBackground(gc);
    }

    /**
     * amaumont Comment method "addLink".
     *
     * @param tableItem
     * @param dataItem1
     * @param tree
     * @param dataItem2
     */
    public void addLoopLink(Item tableItem, Object dataItem1, Tree tree, FOXTreeNode dataItem2) {
        LinkDescriptor<Item, Object, Tree, Object> link = addLink(tableItem, dataItem1, tree, dataItem2);
    }

    /**
     * amaumont Comment method "addLink".
     *
     * @param tableItem
     * @param dataItem1
     * @param tree
     * @param dataItem2
     */
    private LinkDescriptor<Item, Object, Tree, Object> addLink(Item tableItem, Object dataItem1, Tree tree, Object dataItem2) {
        LinkDescriptor<Item, Object, Tree, Object> link = new LinkDescriptor<Item, Object, Tree, Object>(
                new ItemExtremityDescriptor(tableItem, dataItem1), new ExtremityLink<Tree, Object>(tree, dataItem2));

        link.setStyleLink(getUnselectedStyleLink());
        getLinksManager().addLink(link);
        updateLinksStyleAndControlsSelection(tree);
        return link;
    }

    /**
     * DOC amaumont Comment method "removeAllLinks".
     */
    public void removeAllLinks() {
        getLinksManager().clearLinks();
    }

    public boolean removeLinksFromTableEntry(SchemaTarget schemaTarget) {
        return getLinksManager().removeLinksFromDataItem2(schemaTarget);
    }

    public boolean removeLinksFromTreeNode(FOXTreeNode node) {
        return getLinksManager().removeLinksFromDataItem2(node);
    }

    /**
     * DOC amaumont Comment method "onXPathValueChanged".
     *
     * @param table
     * @param widget
     */
    public void onXPathValueChanged(Widget widget) {
        linksManager.removeLinksFromDataItem2(widget.getData());
        createLinks();
    }

    /**
     * DOC ke Comment method "linkSize".
     *
     * @return
     */
    public int linkSize() {
        return this.getLinksManager().getLinks().size();
    }

    private void createLoopLinks(List<FOXTreeNode> treeData) {
        for (FOXTreeNode treeNode : treeData) {
            if (treeNode.getColumn() != null) {
                TableItem tableItem = null;
                for (TableItem curTableItem : getSource().getItems()) {
                    String columnLabel = treeNode.getColumn().getLabel();
                    if (curTableItem.getText().equals(columnLabel)) {
                        tableItem = curTableItem;
                        break;
                    }
                }
                if (tableItem == null) {
                    continue;
                }
                String path = tableItem.getText();
                if (path != null) {
                    TreeItem treeItem = getTreeItem(treeNode);
                    if (treeItem != null) {
                        addLoopLink(tableItem, tableItem.getData(), xmlViewer.getTree(), (FOXTreeNode) treeItem.getData());
                    }
                }
            }
            createLoopLinks(treeNode.getChildren());
        }
    }

    public String getAbsoluteXPath(FOXTreeNode foxTreeNode) {
        FOXTreeNode item = foxTreeNode;
        String path = "";
        while (item != null) {
            path = "/" + item.getLabelForViewer() + path;
            item = item.getParent();
        }
        return path;
    }

    public TreeItem getTreeItem(TreeItem[] items, FOXTreeNode treeNode, boolean expandedOnly) {
        TreeItem item = null;
        for (TreeItem curItem : items) {
            // call getText method since we are in a lazy tree.
            // it will force to load this item in the GUI (since it should be displayed in all cases)
            // without this, the data will be null, and we can't retrieve anything
            curItem.getText();
            if (curItem.getData() != null) {
                if (curItem.getData().equals(treeNode)) {
                    return curItem;
                }
                // will check if one of the parent of the treeNode is this one.
                if (containsTreeNode((FOXTreeNode) curItem.getData(), treeNode)) {
                    if (!expandedOnly || curItem.getExpanded()) {
                        return getTreeItem(curItem.getItems(), treeNode, expandedOnly);
                    } else {
                        return curItem;
                    }
                }
            }
        }
        return item;
    }

    /**
     * DOC nrousseau Comment method "containsTreeNode".
     *
     * @param data
     * @param treeNode
     * @return
     */
    private boolean containsTreeNode(FOXTreeNode currentNode, FOXTreeNode leafNode) {
        FOXTreeNode parentOfLeafNode = leafNode.getParent();
        if (parentOfLeafNode != null) {
            if (parentOfLeafNode.equals(currentNode)) {
                return true;
            }
            return containsTreeNode(currentNode, parentOfLeafNode);
        }
        return false;
    }

    public TreeItem getTreeItem(FOXTreeNode foxTreeNode) {
        TreeItem[] items = xmlViewer.getTree().getItems();
        return getTreeItem(items, foxTreeNode, false);
    }

    public TreeItem getFirstVisibleTreeItemOfPath(FOXTreeNode foxTreeNode) {
        TreeItem[] items = xmlViewer.getTree().getItems();
        return getTreeItem(items, foxTreeNode, true);
    }

    /**
     * Getter for loopTableEditorView.
     *
     * @return the loopTableEditorView
     */
    public TreeViewer getXMLViewer() {
        return this.xmlViewer;
    }

    @SuppressWarnings("unchecked")
    public void updateLinksStyleAndControlsSelection(Control currentControl) {
        // super.updateLinksStyleAndControlsSelection(currentControl);
        boolean isTarget = false;
        if (getSource() != currentControl) {
            isTarget = true;
        } else {
            isTarget = false;
        }

        HashSet selectedItems = new HashSet();
        Map itemsToSelect = new HashMap();

        if (isTarget) {
            getTarget().deselectAll();

            TreeItem[] selection = getTarget().getSelection();
            for (TreeItem tableItem : selection) {
                selectedItems.add(tableItem.getData());
            }
        } else {
            TableItem[] selection = getSource().getSelection();
            for (TableItem treeItem : selection) {
                selectedItems.add(treeItem.getData());
            }
        }

        List<LinkDescriptor<Item, Object, Tree, Object>> links = linksManager.getLinks();
        for (LinkDescriptor<Item, Object, Tree, Object> link : links) {
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

            if (extremity.getGraphicalObject() == xmlViewer.getTree()
                    || otherExtremity.getGraphicalObject() == xmlViewer.getTree()) {
                styleLink = getSelectedLoopStyleLink();
            } else {

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
            }
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
                    table.setSelection(tableItems);
                }
            } else {
                xmlViewer.getTree().deselectAll();
            }
        }
        getLinksManager().sortLinks(getDrawingLinksComparator());
    }

    public StyleLink getSelectedLoopStyleLink() {
        if (this.selectedLoopStyleLink == null) {
            this.selectedLoopStyleLink = createStandardLink(selectedLoopLinkColor);
        }
        return this.selectedLoopStyleLink;
    }

    /**
     * DOC amaumont Comment method "getSelectedRelativeStyleLink".
     *
     * @param selectedLoopLinkColor
     */
    private void getSelectedRelativeStyleLink() {
        setSelectedStyleLink(createStandardLink(selectedLoopLinkColor));
    }

    /**
     * DOC amaumont Comment method "validateXPathExpression".
     *
     * @param newValue
     * @return null if expression is valid, else return the error message.
     */
    public String validateXPathExpression(String xpathExpression) {
        if (xpathExpression == null || xpathExpression.trim().length() == 0) {
            return null;
        }
        XPathFactory xpf = XPathFactory.newInstance();
        try {
            xpf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (XPathFactoryConfigurationException ex) {
            ExceptionHandler.process(ex);
        }
        XPath xpath = xpf.newXPath();
        try {
            xpath.compile(xpathExpression);
        } catch (Exception e) {
            return Messages.getString("XmlToXPathLinker.exceptionReturn.xPathInvalid"); //$NON-NLS-1$
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.linking.TreeToTablesLinker#getDrawingLinksComparator()
     */
    @Override
    protected Comparator<LinkDescriptor<Item, Object, Tree, Object>> getDrawingLinksComparator() {
        if (this.drawingLinksComparator == null) {
            this.drawingLinksComparator = new Comparator<LinkDescriptor<Item, Object, Tree, Object>>() {

                @Override
                public int compare(LinkDescriptor<Item, Object, Tree, Object> link1,
                        LinkDescriptor<Item, Object, Tree, Object> link2) {
                    IStyleLink link1StyleLink = link1.getStyleLink();
                    IStyleLink link2StyleLink = link2.getStyleLink();
                    if (link1StyleLink == link2StyleLink) {
                        return 0;
                    }
                    if (link1StyleLink == getSelectedLoopStyleLink()) {
                        return 1;
                    }
                    if (link2StyleLink == getSelectedLoopStyleLink()) {
                        return -1;
                    }
                    if (link1StyleLink == getUnselectedStyleLink()) {
                        return -1;
                    }
                    return 1;
                }

            };
        }
        return this.drawingLinksComparator;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.linking.TableToTreeLinker#getFirstVisibleTreeItemOfPath(java.lang.Object)
     */
    @Override
    protected TreeItem getFirstVisibleTreeItemOfPath(Object dataItem) {
        return getFirstVisibleTreeItemOfPath((FOXTreeNode) dataItem);
    }
}
