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
package org.talend.repository.json.ui.wizards.extraction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.runtime.xml.XmlNodeRetriever;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.SelectionHelper;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.dialogs.EventLoopProgressMonitor;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.ui.swt.drawing.link.BezierHorizontalLink;
import org.talend.commons.ui.swt.drawing.link.ExtremityEastArrow;
import org.talend.commons.ui.swt.drawing.link.ExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.drawing.link.TreeExtremityDescriptor;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedControlViewer.EVENT_TYPE;
import org.talend.commons.ui.swt.extended.table.ExtendedControlEvent;
import org.talend.commons.ui.swt.extended.table.IExtendedControlListener;
import org.talend.commons.ui.swt.linking.TreeToTablesLinker;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.data.list.ListenableListEvent.TYPE;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.wizard.metadata.xml.XmlExtractorBgRefresher;
import org.talend.repository.json.ui.wizards.dnd.JSONToSchemaDragAndDropHandler;
import org.talend.repository.model.json.JSONXPathLoopDescriptor;
import org.talend.repository.model.json.SchemaTarget;
import org.talend.repository.ui.wizards.metadata.connection.files.json.AbstractTreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;
import org.w3c.dom.Node;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class JSONToXPathLinker extends TreeToTablesLinker<Object, Object> {

    protected AbstractTreePopulator treePopulator;

    private ExtractionFieldsWithJSONXPathEditorView fieldsTableEditorView;

    private XmlNodeRetriever nodeRetriever;

    private ExtractionLoopWithJSONXPathEditorView loopTableEditorView;

    private StyleLink selectedLoopStyleLink;

    protected ArrayList<String> loopXpathNodes = new ArrayList<String>(0);

    private Comparator<LinkDescriptor<TreeItem, Object, Table, Object>> drawingLinksComparator;

    private ArrayList<Node> uniqueLoopNodes = new ArrayList<Node>();

    private ArrayList<Node> allLoopNodes = new ArrayList<Node>();

    private Color selectedLoopLinkColor;

    private Color selectedRelativeLinkColor;

    protected JSONToSchemaDragAndDropHandler jsonDndHandler;

    private XmlExtractorBgRefresher xmlExtractorBgRefresher;

    /**
     * DOC amaumont JSONToMetadataTableLinker constructor comment.
     *
     * @param commonParent common main parent of tree and table, it and its children should have backgoundMode
     * configured with SWT.INHERIT_FORCE, same configuration for parents of tree and table.
     * @param tree
     * @param loopTableEditorView
     * @param table
     */
    public JSONToXPathLinker(Composite commonParent) {
        super(commonParent);
    }

    public void init(Tree tree, ExtractionLoopWithJSONXPathEditorView loopTableEditorView,
            ExtractionFieldsWithJSONXPathEditorView fieldsTableEditorView, AbstractTreePopulator treePopulator) {
        xmlExtractorBgRefresher = new XmlExtractorBgRefresher(this);
        init(tree, new Table[] { loopTableEditorView.getExtendedTableViewer().getTableViewerCreator().getTable(),
                fieldsTableEditorView.getExtendedTableViewer().getTableViewerCreator().getTable(), }, xmlExtractorBgRefresher);
        this.treePopulator = treePopulator;
        this.loopTableEditorView = loopTableEditorView;
        this.fieldsTableEditorView = fieldsTableEditorView;
        this.nodeRetriever = getXmlNodeRetriever(treePopulator.getFilePath(), getCurrentLoopXPath());

        TextCellEditorWithProposal xPathCellEditor = loopTableEditorView.getXPathCellEditor();
        xPathCellEditor.setContentProposalProvider(getJSONXPathProposalProvider(this, false));
        xPathCellEditor = fieldsTableEditorView.getXPathCellEditor();
        xPathCellEditor.setContentProposalProvider(getJSONXPathProposalProvider(this, true));
        init();
    }

    public void init(AbstractTreePopulator treePopulator) {
        this.treePopulator = treePopulator;
        this.nodeRetriever = getXmlNodeRetriever(treePopulator.getFilePath(), getCurrentLoopXPath());
    }

    public String getAbsoluteXPath(TreeItem treeItem) {
        return treePopulator.getAbsoluteXPath(treeItem);
    }

    protected JSONXPathProposalProvider getJSONXPathProposalProvider(JSONToXPathLinker linker, boolean isRelative) {
        JSONXPathProposalProvider provider = new JSONXPathProposalProvider(linker, isRelative);
        provider.setReadbyMode(EJsonReadbyMode.XPATH.getValue());
        return provider;
    }

    protected XmlNodeRetriever getXmlNodeRetriever(String filePath, String loopXPath) {
        return new XmlNodeRetriever(filePath, loopXPath);
    }

    /**
     * DOC amaumont Comment method "init".
     *
     * @param tree
     */
    private void init() {

        Display display = getBgDrawableComposite().getDisplay();

        initColors(display);

        StyleLink unselectedStyleLink = new StyleLink();
        unselectedStyleLink.setDrawableLink(new BezierHorizontalLink(unselectedStyleLink));
        unselectedStyleLink.setForegroundColor(display.getSystemColor(SWT.COLOR_GRAY));
        unselectedStyleLink.setLineWidth(2);

        int xOffset = WindowSystem.isGTK() ? 2 : -2;
        int yOffset = WindowSystem.isGTK() ? -1 : 0;
        unselectedStyleLink.setExtremity2(new ExtremityEastArrow(unselectedStyleLink, -ExtremityEastArrow.WIDTH_ARROW + xOffset,
                yOffset));
        setUnselectedStyleLink(unselectedStyleLink);

        getSelectedRelativeStyleLink();

        initListeners();
        createLinks();
        if (WindowSystem.isBigSurOrLater()) {
            getBackgroundRefresher().setBackgroundColor(getBgDrawableComposite().getBackground());
            getBgDrawableComposite().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
            this.getTree().setLinesVisible(true);
        }
    }

    /**
     * DOC amaumont Comment method "initColors".
     *
     * @param display
     */
    private void initColors(Display display) {
        RGB selectedLoopLinkRGB = new RGB(255, 131, 0);
        RGB selectedRelativeLinkRGB = new RGB(110, 168, 255);
        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        if (colorRegistry.get(selectedLoopLinkRGB.toString()) == null) {
            colorRegistry.put(selectedLoopLinkRGB.toString(), selectedLoopLinkRGB);
        }
        if (colorRegistry.get(selectedRelativeLinkRGB.toString()) == null) {
            colorRegistry.put(selectedRelativeLinkRGB.toString(), selectedRelativeLinkRGB);
        }
        selectedLoopLinkColor = colorRegistry.get(selectedLoopLinkRGB.toString());
        selectedRelativeLinkColor = colorRegistry.get(selectedRelativeLinkRGB.toString());

    }

    /**
     * DOC amaumont Comment method "initListeners".
     */
    protected void initListeners() {
        initLoopListeners();
        initFieldsListeners();
        jsonDndHandler = new JSONToSchemaDragAndDropHandler(this);
    }

    private void removeListeners() {
        removeLoopListeners();
        removeFieldsListeners();
        jsonDndHandler.dispose();
    }

    /**
     * DOC amaumont Comment method "createLinks".
     */
    public void createLinks() {

        removeAllLinks();
        getBackgroundRefresher().refreshBackground();

        ProgressDialog progressDialog = new ProgressDialog(getTree().getShell(), 1000) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) {

                TableItem[] loopTableItems = loopTableEditorView.getTable().getItems();
                TableItem[] fieldsTableItems = fieldsTableEditorView.getTable().getItems();

                monitorWrap = new EventLoopProgressMonitor(monitor);

                String taskName = "Loop links creation ...";
                int totalWork = loopTableItems.length + fieldsTableItems.length;

                monitorWrap.beginTask(taskName, totalWork);

                List<JSONXPathLoopDescriptor> xpathLoopDescriptorList = loopTableEditorView.getModel().getBeansList();
                for (int i = 0; i < loopTableItems.length; i++) {

                    if (monitorWrap.isCanceled()) {
                        return;
                    }

                    TableItem tableItem = loopTableItems[i];
                    JSONXPathLoopDescriptor xpathLoopDescriptor = xpathLoopDescriptorList.get(i);
                    String originalValue = xpathLoopDescriptor.getAbsoluteXPathQuery();
                    if (xpathLoopDescriptor.getConnection() != null) {
                        ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                                xpathLoopDescriptor.getConnection(), xpathLoopDescriptor.getConnection().getContextName());
                        if (contextType != null) {
                            originalValue = ConnectionContextHelper.getOriginalValue(contextType,
                                    xpathLoopDescriptor.getAbsoluteXPathQuery());
                            originalValue = TalendQuoteUtils.removeQuotes(originalValue);
                        }
                    }

                    if (originalValue != null) {
                        loadItemDataForLazyLoad(loopTableEditorView);
                        createLoopLinks(originalValue, tableItem, monitorWrap);
                    }

                    monitorWrap.worked(1);
                }

                List<SchemaTarget> schemaTargetList = fieldsTableEditorView.getModel().getBeansList();

                createFieldsLinkWithProgressMonitor(monitorWrap, schemaTargetList.size() + loopTableItems.length,
                        schemaTargetList, 0, fieldsTableItems.length);

                monitorWrap.done();

            }

        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            // Nothing to do
        }

    }

    /**
     * DOC amaumont Comment method "createFieldsLinkWithProgressDialog".
     *
     * @param monitorWrap
     * @param fieldsTableItems
     * @param totalWork
     * @param schemaTargetList
     */
    private void createFieldsLinkWithProgressMonitor(IProgressMonitor monitorWrap, int totalWork,
            List<SchemaTarget> schemaTargetList, int startTableItem, int tableItemLength) {
        monitorWrap.beginTask("Fields links creation ...", totalWork);

        loadItemDataForLazyLoad(fieldsTableEditorView);
        TableItem[] fieldsTableItems = fieldsTableEditorView.getTable().getItems();
        for (int i = startTableItem, indexShemaTarget = 0; i < startTableItem + tableItemLength; i++, indexShemaTarget++) {

            if (monitorWrap.isCanceled()) {
                for (int j = startTableItem + tableItemLength - 2; j >= 0; j--) {
                    // fieldsTableEditorView.getTable().remove(j);
                    fieldsTableEditorView.getTable().redraw();
                    JSONExtractorFieldModel schemaModel = this.fieldsTableEditorView.getModel();
                    schemaModel.remove(j);
                }
                return;
            }

            TableItem tableItem = fieldsTableItems[i];
            SchemaTarget schemaTarget = schemaTargetList.get(indexShemaTarget);
            String relativeXpathQuery = schemaTarget.getRelativeXPathQuery();
            if(fieldToExtract(jsonDndHandler.extractTagName(relativeXpathQuery, jsonDndHandler.getReadbyMode()), treePopulator.getAllNodes())) {
            	createFieldLinks(relativeXpathQuery, tableItem, monitorWrap, schemaTarget);
            }
            monitorWrap.worked(1);
        }
        fieldsTableEditorView.getTableViewerCreator().getTableViewer().refresh();
        getLinksManager().sortLinks(getDrawingLinksComparator());
        getBackgroundRefresher().refreshBackground();
    }

    private IModifiedBeanListener loopModelModifiedBeanListener;

    private IExtendedControlListener loopTableExtendedControlListener;

    private ILineSelectionListener afterLineSelectionListener;
    
    public boolean fieldToExtract(String relativeXpathQuery, List<ATreeNode> nodes) {
    	for(ATreeNode node : nodes) {
    		if(relativeXpathQuery.equals(node.getLabel())) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * DOC amaumont Comment method "initListeners".
     */
    private void initLoopListeners() {

        JSONExtractorLoopModel loopModel = this.loopTableEditorView.getModel();

        final Table loopTable = this.loopTableEditorView.getTableViewerCreator().getTable();
        loopModelModifiedBeanListener = new IModifiedBeanListener<JSONXPathLoopDescriptor>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<JSONXPathLoopDescriptor> event) {
                handleModifiedBeanEvent(event);
            }

            private void handleModifiedBeanEvent(ModifiedBeanEvent<JSONXPathLoopDescriptor> event) {
                if (event.column == loopTableEditorView.getXPathColumn()) {
                    onXPathValueChanged(loopTable, (String) event.newValue, event.index);
                }

            }

        };
        loopModel.addModifiedBeanListener(loopModelModifiedBeanListener);

        loopTableExtendedControlListener = new IExtendedControlListener() {

            @Override
            public void handleEvent(ExtendedControlEvent event) {
                if (event.getType() == EVENT_TYPE.MODEL_CHANGED) {
                    nodeRetriever.setCurrentLoopXPath(getCurrentLoopXPath());
                }
            }

        };
        this.loopTableEditorView.getExtendedTableViewer().addListener(loopTableExtendedControlListener);

        SelectionHelper selectionHelper = this.loopTableEditorView.getTableViewerCreator().getSelectionHelper();
        afterLineSelectionListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                updateLinksStyleAndControlsSelection(e.source.getTable(), true);
            }
        };
        selectionHelper.addAfterSelectionListener(afterLineSelectionListener);
    }

    private void removeLoopListeners() {
        JSONExtractorLoopModel loopModel = this.loopTableEditorView.getModel();
        loopModel.removeModifiedBeanListener(loopModelModifiedBeanListener);
        this.loopTableEditorView.getExtendedTableViewer().removeListener(loopTableExtendedControlListener);
        SelectionHelper selectionHelper = this.loopTableEditorView.getTableViewerCreator().getSelectionHelper();
        selectionHelper.removeAfterSelectionListener(afterLineSelectionListener);
    }

    private IModifiedBeanListener schemaModelModifiedBeanListener;

    private IListenableListListener schemaModelBeforeListenableListListener;

    private IListenableListListener schemaModelAfterListenableListListener;

    private ILineSelectionListener fieldsTableLineSelectionListener;

    /**
     * DOC amaumont Comment method "initListeners".
     */
    private void initFieldsListeners() {

        JSONExtractorFieldModel schemaModel = this.fieldsTableEditorView.getModel();

        final Table fieldsTable = this.fieldsTableEditorView.getTable();

        schemaModelModifiedBeanListener = new IModifiedBeanListener<SchemaTarget>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<SchemaTarget> event) {
                handleModifiedBeanEvent(event);
            }

            private void handleModifiedBeanEvent(ModifiedBeanEvent<SchemaTarget> event) {
                if (event.column == fieldsTableEditorView.getXPathColumn()) {
                    onXPathValueChanged(fieldsTable, (String) event.newValue, event.index);
                }

            }

        };
        schemaModel.addModifiedBeanListener(schemaModelModifiedBeanListener);

        schemaModelBeforeListenableListListener = new IListenableListListener<SchemaTarget>() {

            @Override
            public void handleEvent(ListenableListEvent<SchemaTarget> event) {
                handleListenableListBeforeTableViewerRefreshedEvent(event);
            }

        };
        schemaModel.addBeforeOperationListListener(-50, schemaModelBeforeListenableListListener);

        schemaModelAfterListenableListListener = new IListenableListListener<SchemaTarget>() {

            @Override
            public void handleEvent(ListenableListEvent<SchemaTarget> event) {
                handleListenableListAfterTableViewerRefreshedEvent(event);
            }

        };
        schemaModel.addAfterOperationListListener(schemaModelAfterListenableListListener);

        SelectionHelper selectionHelper = this.fieldsTableEditorView.getTableViewerCreator().getSelectionHelper();
        fieldsTableLineSelectionListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                updateLinksStyleAndControlsSelection(e.source.getTable(), true);
            }
        };
        selectionHelper.addAfterSelectionListener(fieldsTableLineSelectionListener);

    }

    private void removeFieldsListeners() {
        JSONExtractorFieldModel schemaModel = this.fieldsTableEditorView.getModel();
        schemaModel.removeModifiedBeanListener(schemaModelModifiedBeanListener);
        schemaModel.removeModifiedListListener(schemaModelBeforeListenableListListener);
        schemaModel.removeModifiedListListener(schemaModelAfterListenableListListener);
        SelectionHelper selectionHelper = this.fieldsTableEditorView.getTableViewerCreator().getSelectionHelper();
        selectionHelper.removeAfterSelectionListener(fieldsTableLineSelectionListener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.linking.TreeToTableLinker#drawBackground(org.eclipse.swt.graphics.GC)
     */
    @Override
    public void drawBackground(GC gc) {

        Rectangle clipBounds = getTree().getBounds();

        List<Table> tables = getTables();

        if (tables == null || tables.size() == 0) {
            throw new IllegalStateException();
        }
        Table table = tables.get(0);

        Rectangle tableBounds = table.getDisplay().map(table, getBgDrawableComposite(), table.getBounds());
        // System.out.println(tableBounds);

        // System.out.println(getBgDrawableComposite());

        int offset = WindowSystem.isGTK() ? 0 : 20;

        clipBounds.width = tableBounds.x;
        clipBounds.height += offset - 4;
        clipBounds.x = 0;
        clipBounds.y = offset;

        gc.setClipping(clipBounds);

        super.drawBackground(gc);

        gc.setClipping((Rectangle) null);
    }

    @Override
    protected TreeItem getTreeItem(Tree tree, Object dataOfTreeItem, Object dataOfTableItem) {
        String path = null;
        if (dataOfTableItem instanceof SchemaTarget) {
            SchemaTarget target = (SchemaTarget) dataOfTableItem;
            path = target.getRelativeXPathQuery();
        } else if (dataOfTableItem instanceof JSONXPathLoopDescriptor) {
            JSONXPathLoopDescriptor target = (JSONXPathLoopDescriptor) dataOfTableItem;
            path = target.getAbsoluteXPathQuery();
        }
        if (path == null) {
            return super.getTreeItem(tree, dataOfTreeItem, dataOfTableItem);
        }

        boolean expressionIsAbsolute = false;
        if (path.trim().startsWith("/")) { //$NON-NLS-1$
            expressionIsAbsolute = true;
        }

        String fullPath = ""; //$NON-NLS-1$
        if (!expressionIsAbsolute) {
            if (loopXpathNodes.size() > 0) {
                fullPath = loopXpathNodes.get(0) + "/"; //$NON-NLS-1$
            }
            // adapt relative path
            String[] relatedSplitedPaths = path.split("\\.\\./"); //$NON-NLS-1$
            if (relatedSplitedPaths.length > 1) {
                int pathsToRemove = relatedSplitedPaths.length - 1;
                String[] fullPathSplited = fullPath.split("/"); //$NON-NLS-1$
                fullPath = ""; //$NON-NLS-1$
                for (int j = 1; j < (fullPathSplited.length - pathsToRemove); j++) {
                    fullPath += "/" + fullPathSplited[j]; //$NON-NLS-1$
                }
                fullPath += "/" + relatedSplitedPaths[pathsToRemove]; //$NON-NLS-1$
            } else {
                fullPath += path;
            }
        } else {
            fullPath = path;
        }
        TreeItem treeItem = treePopulator.getTreeItem(fullPath);
        if (treeItem != null) {
            return treeItem;
        } else {
            return super.getTreeItem(tree, dataOfTreeItem, dataOfTableItem);
        }
    }

    private void handleListenableListBeforeTableViewerRefreshedEvent(ListenableListEvent<SchemaTarget> event) {
        if (event.type == TYPE.REMOVED) {
            Collection<SchemaTarget> removedObjects = event.removedObjects;
            for (SchemaTarget target : removedObjects) {
                linksManager.removeLinksFromDataItem2(target);
            }
        }

    }

    public void handleListenableListAfterTableViewerRefreshedEvent(final ListenableListEvent<SchemaTarget> event) {
        if (event.type == ListenableListEvent.TYPE.ADDED) {

            ProgressDialog progressDialog = new ProgressDialog(getTree().getShell(), 1000) {

                private IProgressMonitor monitorWrap;

                @Override
                public void run(IProgressMonitor monitor) {

                    monitorWrap = new EventLoopProgressMonitor(monitor);

                    List<SchemaTarget> addedObjects = new ArrayList<SchemaTarget>(event.addedObjects);

                    JSONToXPathLinker.this.createFieldsLinkWithProgressMonitor(monitorWrap, addedObjects.size(), addedObjects,
                            event.index, addedObjects.size());

                    monitorWrap.done();

                }

            };

            try {
                progressDialog.executeProcess();
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                // Nothing to do
            }

            getBackgroundRefresher().refreshBackground();
        } else if (event.type == ListenableListEvent.TYPE.SWAPED) {
            getBackgroundRefresher().refreshBackground();
        } else if (event.type == TYPE.REMOVED) {
            getBackgroundRefresher().refreshBackground();
        }

    }

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param treeItem
     * @param dataItem1
     * @param table
     * @param dataItem2
     */
    public void addLoopLink(TreeItem treeItem, Object dataItem1, Table table, JSONXPathLoopDescriptor dataItem2) {
        LinkDescriptor<TreeItem, Object, Table, Object> link = addLink(treeItem, dataItem1, table, dataItem2, true);
        link.setStyleLink(getSelectedLoopStyleLink());
    }

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param treeItem
     * @param dataItem1
     * @param table
     * @param dataItem2
     */
    private LinkDescriptor<TreeItem, Object, Table, Object> addLink(TreeItem treeItem, Object dataItem1, Table table,
            Object dataItem2, Boolean flag) {
        LinkDescriptor<TreeItem, Object, Table, Object> link = new LinkDescriptor<TreeItem, Object, Table, Object>(
                new TreeExtremityDescriptor(treeItem, dataItem1), new ExtremityLink<Table, Object>(table, dataItem2));

        link.setStyleLink(getUnselectedStyleLink());
        getLinksManager().addLink(link);
        updateLinksStyleAndControlsSelection(table, flag);
        return link;
    }

    /**
     * DOC amaumont Comment method "addLink".
     *
     * @param treeItem
     * @param dataItem1
     * @param table
     * @param dataItem2
     */
    public void addFieldLink(TreeItem treeItem, Object dataItem1, Table table, SchemaTarget dataItem2) {
        LinkDescriptor<TreeItem, Object, Table, Object> link = addLink(treeItem, dataItem1, table, dataItem2, false);
        // link.setStyleLink(getSelectedStyleLink());
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

    /**
     * DOC amaumont Comment method "onXPathValueChanged".
     *
     * @param table
     * @param newValue
     * @param itemIndex
     */
    public void onXPathValueChanged(Table table, String newValue, int itemIndex) {
        if (isLoopTable(table)) {
            nodeRetriever.setCurrentLoopXPath(newValue);
        }
        TableItem tableItem = table.getItem(itemIndex);
        linksManager.removeLinksFromDataItem2(tableItem.getData());
        if (isLoopTable(table)) {
            createLinks();
        } else {
            createFieldLinks(newValue, tableItem, null, null);
        }
        getBackgroundRefresher().refreshBackground();

    }

    /**
     * DOC amaumont Comment method "isLoopTable".
     *
     * @param table
     * @return
     */
    public boolean isLoopTable(Table table) {
        return loopTableEditorView.getTableViewerCreator().getTable() == table;
    }

    /**
     * DOC amaumont Comment method "createLoopLinks".
     *
     * @param monitorWrap
     *
     * @param pathQuery
     * @param tableItem
     */
    private void createLoopLinks(String xPathExpression, TableItem tableItemTarget, IProgressMonitor monitorWrap) {

        if (xPathExpression == null || xPathExpression.trim().length() == 0) {
            loopXpathNodes.clear();
            uniqueLoopNodes.clear();
            allLoopNodes.clear();
            return;
        }

        Set<String> alreadyProcessedXPath = new HashSet<String>();
        List<Node> nodeList = null;

        loopXpathNodes = new ArrayList<String>();
        uniqueLoopNodes = new ArrayList<Node>();
        allLoopNodes = new ArrayList<Node>();
        if (!alreadyProcessedXPath.contains(xPathExpression)) {
            loopXpathNodes.add(xPathExpression);
            TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(xPathExpression);
            if (treeItemFromAbsoluteXPath != null) {
                addLoopLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(), tableItemTarget.getParent(),
                        (JSONXPathLoopDescriptor) tableItemTarget.getData());
                alreadyProcessedXPath.add(xPathExpression);
            }
        }
    }

    /**
     * DOC amaumont Comment method "addLinks".
     *
     * @param relativeXpath
     * @param tableItemTarget
     * @param progressMonitor
     * @throws XPathExpressionException
     */
    protected void createFieldLinks(final String relativeXpathPrm, final TableItem tableItemTarget,
            IProgressMonitor progressMonitor, SchemaTarget schemaTarget) {

        if (relativeXpathPrm == null || relativeXpathPrm.trim().length() == 0) {
            return;
        }

        boolean expressionIsAbsolute = false;
        if (relativeXpathPrm.trim().startsWith("/")) { //$NON-NLS-1$
            expressionIsAbsolute = true;
        }

        String relativeXpath = relativeXpathPrm;
        // TDI-19173
        if (relativeXpath != null && relativeXpath.endsWith("]")) {
            relativeXpath = relativeXpath.substring(0, relativeXpath.lastIndexOf("["));
        }
        Set<String> alreadyProcessedXPath = new HashSet<String>();

        String fullPath = ""; //$NON-NLS-1$
        if (!expressionIsAbsolute) {
            if (loopXpathNodes.size() > 0) {
                fullPath = loopXpathNodes.get(0) + "/"; //$NON-NLS-1$
            }
            // adapt relative path
            String[] relatedSplitedPaths = relativeXpath.split("\\.\\./"); //$NON-NLS-1$
            if (relatedSplitedPaths.length > 1) {
                int pathsToRemove = relatedSplitedPaths.length - 1;
                String[] fullPathSplited = fullPath.split("/"); //$NON-NLS-1$
                fullPath = ""; //$NON-NLS-1$
                for (int i = 1; i < (fullPathSplited.length - pathsToRemove); i++) {
                    fullPath += "/" + fullPathSplited[i]; //$NON-NLS-1$
                }
                fullPath += "/" + relatedSplitedPaths[pathsToRemove]; //$NON-NLS-1$
            } else {
                fullPath += relativeXpath;
            }
        } else {
            fullPath = relativeXpath;
        }
        TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(fullPath);
        if (treeItemFromAbsoluteXPath != null && !alreadyProcessedXPath.contains(fullPath)) {
            addFieldLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(), tableItemTarget.getParent(),
                    (SchemaTarget) tableItemTarget.getData());
            alreadyProcessedXPath.add(fullPath);
        }

    }

    public SchemaTarget getNewSchemaTargetEntry(String relativeXPathValue) {

        JSONExtractorFieldModel xpathNodeSchemaModel = fieldsTableEditorView.getModel();
        SchemaTarget schemaTarget = xpathNodeSchemaModel.createNewSchemaTarget();
        schemaTarget.setRelativeXPathQuery(relativeXPathValue);

        return schemaTarget;
    }

    /**
     * Getter for tableEditorView.
     *
     * @return the tableEditorView
     */
    public ExtractionFieldsWithJSONXPathEditorView getFieldsTableEditorView() {
        return this.fieldsTableEditorView;
    }

    /**
     * Getter for loopTableEditorView.
     *
     * @return the loopTableEditorView
     */
    public ExtractionLoopWithJSONXPathEditorView getLoopTableEditorView() {
        return this.loopTableEditorView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateLinksStyleAndControlsSelection(Control currentControl, Boolean flag) {

        boolean selectedControlIsTable = false;
        if (currentControl instanceof Table) {
            selectedControlIsTable = true;
        } else if (currentControl instanceof Tree) {
            selectedControlIsTable = false;
        } else {
            throw new IllegalArgumentException("This type of currentControl is unsupported");
        }

        HashSet selectedItems = new HashSet();
        Map itemsToSelect = new HashMap();

        if (selectedControlIsTable) {
            for (Table table : getTables()) {
                if (table != currentControl) {
                    table.deselectAll();
                    if (isFieldsTable(table)) {
                        fieldsTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
                    }
                }
            }

            TableItem[] selection = ((Table) currentControl).getSelection();
            for (TableItem tableItem : selection) {
                selectedItems.add(tableItem.getData());
            }

        } else {

            TreeItem[] selection = getTree().getSelection();
            for (TreeItem treeItem : selection) {
                selectedItems.add(treeItem.getData());
            }

        }

        List<LinkDescriptor<TreeItem, Object, Table, Object>> links = linksManager.getLinks();
        for (LinkDescriptor<TreeItem, Object, Table, Object> link : links) {
            IStyleLink styleLink = null;
            IExtremityLink extremity = null;
            IExtremityLink otherExtremity = null;
            if (selectedControlIsTable) {
                extremity = link.getExtremity2();
                otherExtremity = link.getExtremity1();
            } else {
                extremity = link.getExtremity1();
                otherExtremity = link.getExtremity2();
            }
            boolean currentItemIsSelected = selectedItems.contains(extremity.getDataItem());

            if (extremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()
                    || otherExtremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()) {
                styleLink = getSelectedLoopStyleLink();
            } else {

                if (currentItemIsSelected) {
                    styleLink = getSelectedStyleLink();
                    if (selectedControlIsTable) {

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

        if (selectedControlIsTable) {
            TreeItem[] treeItems = (TreeItem[]) itemsToSelect.keySet().toArray(new TreeItem[0]);
            if (treeItems.length > 0) {
                if (!treeItems[0].isDisposed()) {
                    (getTree()).setSelection(treeItems);
                }
            }
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
                loopTableEditorView.getTable().deselectAll();
                fieldsTableEditorView.getTable().deselectAll();
            }
            fieldsTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
        }
        if (flag) {
            getLinksManager().sortLinks(getDrawingLinksComparator());
            getBackgroundRefresher().refreshBackground();
        }
    }

    /**
     * DOC amaumont Comment method "isFieldsTable".
     *
     * @param table
     * @return
     */
    public boolean isFieldsTable(Table table) {
        return fieldsTableEditorView.getTableViewerCreator().getTable() == table;
    }

    public StyleLink getSelectedLoopStyleLink() {
        if (this.selectedLoopStyleLink == null) {
            StyleLink styleLink = new StyleLink();
            styleLink.setDrawableLink(new BezierHorizontalLink(styleLink));
            styleLink.setForegroundColor(selectedLoopLinkColor);
            styleLink.setLineWidth(2);
            int xOffset = WindowSystem.isGTK() ? 2 : -2;
            int yOffset = WindowSystem.isGTK() ? -1 : 0;
            styleLink.setExtremity2(new ExtremityEastArrow(styleLink, -ExtremityEastArrow.WIDTH_ARROW + xOffset, yOffset));
            this.selectedLoopStyleLink = styleLink;
        }
        return this.selectedLoopStyleLink;
    }

    /**
     * DOC amaumont Comment method "getSelectedRelativeStyleLink".
     *
     * @param selectedLoopLinkColor
     */
    private void getSelectedRelativeStyleLink() {

        StyleLink selectedStyleLink = new StyleLink();
        selectedStyleLink.setDrawableLink(new BezierHorizontalLink(selectedStyleLink));
        selectedStyleLink.setForegroundColor(selectedRelativeLinkColor);
        selectedStyleLink.setLineWidth(2);
        int xOffset = WindowSystem.isGTK() ? 2 : -2;
        int yOffset = WindowSystem.isGTK() ? -1 : 0;
        selectedStyleLink.setExtremity2(new ExtremityEastArrow(selectedStyleLink, -ExtremityEastArrow.WIDTH_ARROW + xOffset,
                yOffset));
        setSelectedStyleLink(selectedStyleLink);
    }

    public String getCurrentLoopXPath() {
        return loopTableEditorView.getExtendedTableModel().getBeansList().get(0).getAbsoluteXPathQuery();
    }

    public void parseAllFieldsXPathExpressions() {

        List<SchemaTarget> beansList = fieldsTableEditorView.getExtendedTableModel().getBeansList();

        int lstSize = beansList.size();
        for (int i = 0; i < lstSize; i++) {
            SchemaTarget schemaTarget = beansList.get(i);
            onXPathValueChanged(fieldsTableEditorView.getTable(), schemaTarget.getRelativeXPathQuery(), i);
        }

    }

    /**
     * Getter for loopXpathNodes.
     *
     * @return the loopXpathNodes
     */
    public ArrayList<String> getLoopXpathNodes() {
        return this.loopXpathNodes;
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
            return "The current XPath expression is invalid";
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.linking.TreeToTablesLinker#getDrawingLinksComparator()
     */
    @Override
    protected Comparator<LinkDescriptor<TreeItem, Object, Table, Object>> getDrawingLinksComparator() {
        if (this.drawingLinksComparator == null) {
            this.drawingLinksComparator = new Comparator<LinkDescriptor<TreeItem, Object, Table, Object>>() {

                @Override
                public int compare(LinkDescriptor<TreeItem, Object, Table, Object> link1,
                        LinkDescriptor<TreeItem, Object, Table, Object> link2) {
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

    /**
     * Getter for allLoopNodes.
     *
     * @return the allLoopNodes
     */
    public ArrayList<Node> getAllLoopNodes() {
        return this.allLoopNodes;
    }

    /**
     * Getter for uniqueLoopNodes.
     *
     * @return the uniqueLoopNodes
     */
    public ArrayList<Node> getUniqueLoopNodes() {
        return this.uniqueLoopNodes;
    }

    /**
     * Getter for nodeRetriever.
     *
     * @return the nodeRetriever
     */
    public XmlNodeRetriever getNodeRetriever() {
        return this.nodeRetriever;
    }

    public String getFieldSeperator() {
        return "/"; //$NON-NLS-1$
    }

    public String getRootSeperator() {
        return "/"; //$NON-NLS-1$
    }

    public AbstractTreePopulator getTreePopulator() {
        return this.treePopulator;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.linking.TreeToTablesLinker#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        removeListeners();
        xmlExtractorBgRefresher.dispose();
    }

}
