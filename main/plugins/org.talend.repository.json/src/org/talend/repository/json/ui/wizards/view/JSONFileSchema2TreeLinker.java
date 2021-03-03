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
package org.talend.repository.json.ui.wizards.view;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.talend.commons.ui.swt.dialogs.EventLoopProgressMonitor;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.ui.swt.drawing.link.ExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.ItemExtremityDescriptor;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.drawing.link.LinksManager;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.linking.TableToTreeLinker;
import org.talend.commons.ui.utils.TreeUtils;
import org.talend.metadata.managment.ui.wizard.metadata.xml.XmlExtractorBgRefresher;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * wzhang class global comment. Detailled comment
 */
public class JSONFileSchema2TreeLinker extends TableToTreeLinker<Object, Object> {

    private TreeViewer jsonViewer;

    private Color selectedLoopLinkColor;

    private Color selectedRelativeLinkColor;

    private AbstractJSONStepForm form;

    private StyleLink selectedLoopStyleLink;

    private Comparator<LinkDescriptor<Item, Object, Tree, Object>> drawingLinksComparator;

    public JSONFileSchema2TreeLinker(Composite commonParent) {
        super(commonParent);
    }

    public void init(Table schemaTable, TreeViewer jsonViewer) {
        init(schemaTable, jsonViewer.getTree(), new XmlExtractorBgRefresher(this));
        this.jsonViewer = jsonViewer;
        init();
    }

    public void init(JSONFileOutputMetadataEmfTableEditorView xmlEmfTableEditorView, TreeViewer jsonViewer) {
        init(xmlEmfTableEditorView.getTable(), jsonViewer.getTree(), new XmlExtractorBgRefresher(this));
        this.jsonViewer = jsonViewer;
        init();
    }

    private void init() {
        Display display = getBgDrawableComposite().getDisplay();
        initColors(display);

        setUnselectedStyleLink(createStandardLink(display.getSystemColor(SWT.COLOR_BLUE)));

        getSelectedRelativeStyleLink();

        initListeners();
    }

    public AbstractJSONStepForm getForm() {
        return this.form;
    }

    public void setForm(AbstractJSONStepForm form) {
        this.form = form;
    }

    public TreeViewer getJSONViewer() {
        return this.jsonViewer;
    }

    private void initColors(Display display) {
        // selectedLoopLinkColor = new Color(display, 255, 131, 255);
        selectedLoopLinkColor = new Color(display, 110, 168, 255);// light blue
        selectedRelativeLinkColor = new Color(display, 110, 168, 0);
        getSource().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                selectedLoopLinkColor.dispose();
                selectedRelativeLinkColor.dispose();
                getSource().removeDisposeListener(this);
            }
        });

    }

    private void getSelectedRelativeStyleLink() {
        setSelectedStyleLink(createStandardLink(selectedLoopLinkColor));
    }

    private void initListeners() {
        new JSONFileDragAndDropHandler(this);
    }

    public void valuedChanged(Widget widget) {
        onXPathValueChanged(widget);
    }

    public void onXPathValueChanged(Widget widget) {
        if (widget != null) {
            linksManager.removeLinksFromDataItem2(widget.getData());
        }
        createLinks();
    }

    public LinksManager<Item, Object, Tree, Object> getLinkManager() {
        return getLinksManager();
    }

    public void createLinks() {

        removeAllLinks();
        getBackgroundRefresher().refreshBackground();

        ProgressDialog progressDialog = new ProgressDialog(getSource().getShell(), 1000) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) {

                TreeItem root = jsonViewer.getTree().getItem(0);
                // if (getManager().getFoxComponent().istFileOutputMSXML()) {
                // List<FOXTreeNode> treeData = getManager().getTreeData(getManager().getCurrentSchema());
                // if (treeData != null && treeData.size() > 0) {
                // FOXTreeNode rootTreeData = treeData.get(0);
                // for (TreeItem item : xmlViewer.getTree().getItems()) {
                // if (rootTreeData == item.getData()) {
                // root = item;
                // break;
                // }
                // }
                // }
                // }
                List<TreeItem> allItems = TreeUtils.collectAllItems(root);
                monitorWrap = new EventLoopProgressMonitor(monitor);

                String taskName = "Loop links creation ...";
                int totalWork = allItems.size();

                monitorWrap.beginTask(taskName, totalWork); //$NON-NLS-1$

                for (int i = 0; i < totalWork; i++) {

                    if (monitorWrap.isCanceled()) {
                        return;
                    }

                    TreeItem treeItem = allItems.get(i);
                    FOXTreeNode node = (FOXTreeNode) treeItem.getData();
                    if (node.getColumn() == null) { //$NON-NLS-1$
                        continue;
                    }
                    createLoopLinks(node.getColumn().getLabel(), treeItem, monitorWrap, i == totalWork - 1);

                    monitorWrap.worked(1);
                }

                monitorWrap.done();
            }
        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }

    }

    public void removeAllLinks() {
        getLinksManager().clearLinks();
    }

    public boolean isNoLinker() {
        List<LinkDescriptor<Item, Object, Tree, Object>> links = getLinksManager().getLinks();
        if (links == null || links.size() == 0) {
            return true;
        }
        return false;
    }

    public int linkSize() {
        return this.getLinksManager().getLinks().size();
    }

    private void createLoopLinks(String xPathExpression, TreeItem tableItemTarget, IProgressMonitor monitorWrap, boolean lastOne) {

        if (monitorWrap != null && monitorWrap.isCanceled()) {
            return;
        }

        TableItem treeItemFromAbsoluteXPath = getItem(xPathExpression);
        if (treeItemFromAbsoluteXPath != null) {
            addLoopLink(treeItemFromAbsoluteXPath, (Object) treeItemFromAbsoluteXPath.getData(), tableItemTarget.getParent(),
                    (FOXTreeNode) tableItemTarget.getData(), lastOne);
        }
    }

    private TableItem getItem(String path) {
        this.getSource().getItems();
        for (int i = 0; i < this.getSource().getItems().length; i++) {
            TableItem item = getSource().getItems()[i];
            if (path.equals(item.getText())) {
                return item;
            }

        }
        return this.getSource().getItems()[0];
    }

    @Override
    public void drawBackground(GC gc) {
        super.drawBackground(gc);
    }

    public LinkDescriptor<Item, Object, Tree, Object> addLoopLink(Item tableItem, Object dataItem1, Tree tree,
            FOXTreeNode dataItem2, boolean lastOne) {
        return addLink(tableItem, dataItem1, tree, dataItem2, lastOne);
    }

    private LinkDescriptor<Item, Object, Tree, Object> addLink(Item tableItem, Object dataItem1, Tree tree, Object dataItem2,
            boolean lastOne) {
        LinkDescriptor<Item, Object, Tree, Object> link = new LinkDescriptor<Item, Object, Tree, Object>(
                new ItemExtremityDescriptor(tableItem, dataItem1), new ExtremityLink<Tree, Object>(tree, dataItem2));

        link.setStyleLink(getUnselectedStyleLink());
        getLinksManager().addLink(link);
        updateLinksStyleAndControlsSelection(tree, lastOne);
        return link;
    }

    public void updateLinksStyleAndControlsSelection(Control currentControl, boolean lastOne) {
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
            for (int i = 0; i < selection.length; i++) {
                TreeItem tableItem = selection[i];
                selectedItems.add(tableItem.getData());
            }
        } else {
            TableItem[] selection = getSource().getSelection();
            for (int i = 0; i < selection.length; i++) {
                TableItem treeItem = selection[i];
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

            if (extremity.getGraphicalObject() == jsonViewer.getTree()
                    || otherExtremity.getGraphicalObject() == jsonViewer.getTree()) {
                styleLink = getSelectedLoopStyleLink();
            } else {

                if (currentItemIsSelected) {
                    styleLink = getSelectedStyleLink();
                    if (isTarget) {

                        itemsToSelect.put((TableItem) otherExtremity.getGraphicalObject(), null);

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
                    TableItem[] tableItems = (TableItem[]) tableItemsToSelect.toArray(new TableItem[0]);
                    table.setSelection(tableItems);
                }
            } else {
                jsonViewer.getTree().deselectAll();
            }
        }
        getLinksManager().sortLinks(getDrawingLinksComparator());
        // for bug 9279
        if (lastOne) {
            getBackgroundRefresher().refreshBackground();
        }
    }

    public StyleLink getSelectedLoopStyleLink() {
        if (this.selectedLoopStyleLink == null) {
            this.selectedLoopStyleLink = createStandardLink(selectedLoopLinkColor);
        }
        return this.selectedLoopStyleLink;
    }

    @Override
    protected Comparator<LinkDescriptor<Item, Object, Tree, Object>> getDrawingLinksComparator() {
        if (this.drawingLinksComparator == null) {
            this.drawingLinksComparator = new Comparator<LinkDescriptor<Item, Object, Tree, Object>>() {

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
}
