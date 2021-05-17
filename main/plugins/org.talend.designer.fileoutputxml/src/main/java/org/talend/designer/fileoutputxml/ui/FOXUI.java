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
package org.talend.designer.fileoutputxml.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.fileoutputxml.FileOutputXMLComponent;
import org.talend.designer.fileoutputxml.action.CreateAttributeAction;
import org.talend.designer.fileoutputxml.action.CreateElementAction;
import org.talend.designer.fileoutputxml.action.CreateNameSpaceAction;
import org.talend.designer.fileoutputxml.action.DeleteNodeAction;
import org.talend.designer.fileoutputxml.action.DisconnectAction;
import org.talend.designer.fileoutputxml.action.FixValueAction;
import org.talend.designer.fileoutputxml.action.ImportTreeFromXMLAction;
import org.talend.designer.fileoutputxml.action.RemoveGroupAction;
import org.talend.designer.fileoutputxml.action.SetForLoopAction;
import org.talend.designer.fileoutputxml.action.SetGroupAction;
import org.talend.designer.fileoutputxml.i18n.Messages;
import org.talend.designer.fileoutputxml.managers.FOXManager;
import org.talend.designer.fileoutputxml.ui.edit.FOXTargetTreeViewerProvider;
import org.talend.designer.fileoutputxml.ui.edit.Schema2XMLLinker;
import org.talend.designer.fileoutputxml.ui.edit.SchemaTableViewerProvider;
import org.talend.designer.fileoutputxml.ui.footer.FooterComposite;
import org.talend.designer.fileoutputxml.ui.header.HeaderComposite;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 *
 * $Id: FOXUI.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class FOXUI {

    protected FOXManager foxManager;

    private Composite foxUIParent;

    protected FileOutputXMLComponent externalNode;

    private SashForm xmlToSchemaSash;

    protected TableViewer schemaViewer;

    protected TreeViewer xmlViewer;

    private HeaderComposite header;

    private Schema2XMLLinker linker;

    private IAction createAction;

    private IAction deleteAction;

    private IAction disconnectAction;

    private IAction fixValueAction;

    private IAction createAttributeAction;

    private IAction createNamespaceAction;

    private IAction importFromXMLAction;

    // private IAction guessLoopAction;

    private IAction setLoopAction;

    private IAction setGroupAction;

    private IAction removeGroupAction;

    private String selectedText;

    private boolean canModify;

    private boolean isRepository;

    private FooterComposite footerComp;

    public FOXUI(Composite parent, FOXManager foxManager) {
        this.foxManager = foxManager;
        this.foxManager.getUiManager().setFoxUI(this);
        externalNode = foxManager.getFoxComponent();

        // add listeners.
        this.foxUIParent = parent;
        foxUIParent.setLayout(new GridLayout());
    }

    /**
     * bqian Comment method "init".
     */
    public void init() {
        createContent(foxUIParent);
    }

    /**
     * Comment method "createContent".
     *
     * @param child
     */
    private void createContent(Composite mainComposite) {
        header = new HeaderComposite(mainComposite, SWT.NONE);
        if (this.foxManager.isNoLoopInComponent()) {
            header.updateStatus(Messages.getString("FOXUI.NoLoop")); //$NON-NLS-1$
        }
        // Splitter
        xmlToSchemaSash = new SashForm(mainComposite, SWT.HORIZONTAL | SWT.SMOOTH);
        xmlToSchemaSash.setLayoutData(new GridData(GridData.FILL_BOTH));
        xmlToSchemaSash.setBackgroundMode(SWT.INHERIT_FORCE);
        if (Platform.OS_MACOSX.equals(Platform.getOS()) || Platform.OS_LINUX.equals(Platform.getOS())) {
            xmlToSchemaSash.setSashWidth((mainComposite.getShell().getBounds().width) / 5);
        }
        canModify = externalNode.getProcess().isReadOnly();
        if (externalNode.getOriginalNode().getJobletNode() != null) {
            canModify = externalNode.getOriginalNode().isReadOnly();
        }
        IElementParameter elem = externalNode.getElementParameter("PROPERTY_TYPE");
        if (elem != null) {
            String value = (String) elem.getValue();
            if (value != null && value.equals("REPOSITORY")) {
                isRepository = true;
            }
        }
        addSchemaViewer(xmlToSchemaSash, 300, 110);
        addXMLViewer(xmlToSchemaSash, 400, 110);

        xmlToSchemaSash.setWeights(new int[] { 40, 60 });
        linker = new Schema2XMLLinker(this.xmlToSchemaSash);
        linker.init(schemaViewer.getTable(), xmlViewer);
        linker.setManager(foxManager);
        initSchemaTable();
        footerComp = new FooterComposite(mainComposite, SWT.NONE, foxManager);
        xmlViewer.expandToLevel(3);
        linker.createLinks();
        if (Platform.OS_MACOSX.equals(Platform.getOS()) || Platform.OS_LINUX.equals(Platform.getOS())) {
            mainComposite.getShell().addControlListener(new ControlListener() {

                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    xmlToSchemaSash.setSashWidth((mainComposite.getShell().getBounds().width) / 5);
                }

            });
        }
    }


    /**
     *
     * wzhang Comment method "createCombo".
     *
     * @param mainComposite
     */
    protected void createCombo(Composite mainComposite) {

    }

    /**
     *
     * wzhang Comment method "getConnection".
     *
     * @return
     */
    public IConnection getConnection() {
        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(foxManager.getFoxComponent(),
                IConnectionCategory.FLOW);
        if (incomingConnections.size() > 0) {
            return incomingConnections.get(0);
        }
        return null;
    }

    /**
     * DOC ke Comment method "initLinker".
     *
     * @param node
     * @param tableItems
     */
    private void initLinker(TreeItem node, TableItem[] tableItems) {
        FOXTreeNode treeNode = (FOXTreeNode) node.getData();
        IMetadataColumn column = treeNode.getColumn();
        if (column != null) {
            if (this.getFoxManager().getFoxComponent().istFileOutputMSXML() && treeNode.getChildren().size() <= 0) {
                for (TableItem tableItem : tableItems) {
                    IMetadataColumn mColumn = (IMetadataColumn) tableItem.getData();
                    if (mColumn.getLabel().equals(column.getLabel())) {
                        linker.addLoopLink(tableItem, tableItem.getData(), xmlViewer.getTree(), treeNode);
                        linker.getBackgroundRefresher().refreshBackground();
                        break;
                    }
                }
            }
            for (TableItem tableItem : tableItems) {
                IMetadataColumn mColumn = (IMetadataColumn) tableItem.getData();
                if (mColumn.getLabel().equals(column.getLabel())) {
                    linker.addLoopLink(tableItem, tableItem.getData(), xmlViewer.getTree(), treeNode);
                    linker.getBackgroundRefresher().refreshBackground();
                    break;
                }
            }
        }
        TreeItem[] children = node.getItems();
        for (TreeItem element : children) {
            initLinker(element, tableItems);
        }
    }

    /**
     * DOC ke Comment method "redrawLinkers".
     */
    public void redrawLinkers() {
        linker.createLinks();
        // linker.removeAllLinks();
        // TreeItem root = xmlViewer.getTree().getItem(0);
        // if (this.getFoxManager().getFoxComponent().istFileOutputMSXML()) {
        // List<FOXTreeNode> treeData = this.getFoxManager().getTreeData(this.getFoxManager().getCurrentSchema());
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
        //
        // TableItem[] tableItems = schemaViewer.getTable().getItems();
        // initLinker(root, tableItems);
        // if (linker.linkSize() == 0) {
        // linker.updateLinksStyleAndControlsSelection(xmlViewer.getTree(), true);
        // }
    }

    public void refreshXMLViewer(FOXTreeNode targetNode) {

        updateStatus();

        xmlViewer.getTree().setData("row", foxManager.getCurrentSchema()); //$NON-NLS-1$
        this.xmlViewer.refresh();
    }

    protected void initSchemaTable() {
        // if (!externalNode.istWriteXMLField()) {
        // INode originalNode = this.externalNode.getOriginalNode();
        // IMetadataTable metadataTable = null;
        // if (originalNode != null && originalNode.getIncomingConnections().size() > 0) {
        // metadataTable = originalNode.getIncomingConnections().get(0).getMetadataTable();
        // } else {
        // metadataTable = this.externalNode.getMetadataList().get(0);
        // }
        //
        // if (metadataTable != null) {
        // List<IMetadataColumn> columnList = metadataTable.getListColumns();
        // schemaViewer.setInput(columnList);
        // } else {
        // schemaViewer.setInput(new ArrayList<IMetadataColumn>());
        // }
        //
        // } else {
        IConnection inConn = null;
        for (IConnection conn : externalNode.getIncomingConnections()) {
            if ((conn.getLineStyle().equals(EConnectionType.FLOW_MAIN)) || (conn.getLineStyle().equals(EConnectionType.FLOW_REF))
                    || (conn.getLineStyle().equals(EConnectionType.FLOW_MERGE))) {
                inConn = conn;
                break;
            }
        }
        if (inConn != null) {
            List<IMetadataColumn> columnList = inConn.getMetadataTable().getListColumns();
            schemaViewer.setInput(columnList);

        } else {
            schemaViewer.setInput(new ArrayList<IMetadataColumn>());
        }
        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            TableItem[] items = schemaViewer.getTable().getItems();
            for (int rowid = 0; rowid < items.length; rowid++) {
                if (rowid % 2 == 0)
                    items[rowid].setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
                else
                    items[rowid].setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
            }
        }
    }

    /**
     * create xml viewer.
     *
     * @param mainComposite
     * @param form
     * @param width
     * @param height
     */
    private void addXMLViewer(final Composite mainComposite, final int width, final int height) {

        // Group Schema Viewer
        Group group = Form.createGroup(mainComposite, 1, Messages.getString("FOXUI.0"), height); //$NON-NLS-1$
        // group.setBackgroundMode(SWT.INHERIT_FORCE);

        xmlViewer = new TreeViewer(group, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        xmlViewer.getControl().setLayoutData(gridData);
        xmlViewer.setUseHashlookup(true);
        Tree tree = xmlViewer.getTree();
        // see bug 7087
        if (canModify || isRepository) {
            tree.setEnabled(false);
        }
        tree.setLinesVisible(true);
        tree.setBackground(tree.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        column1.setText(Messages.getString("FOXUI.1")); //$NON-NLS-1$
        column1.setWidth(170);

        // Related Column
        TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
        column2.setText(Messages.getString("FOXUI.2")); //$NON-NLS-1$
        column2.setWidth(100);

        TreeColumn column3 = new TreeColumn(tree, SWT.CENTER);
        column3.setText(Messages.getString("FOXUI.3")); //$NON-NLS-1$
        column3.setWidth(90);

        TreeColumn column4 = new TreeColumn(tree, SWT.CENTER);
        column4.setText(Messages.getString("FOXUI.4")); //$NON-NLS-1$
        column4.setWidth(90);

        tree.setHeaderVisible(true);
        // tree.setBackgroundMode(SWT.INHERIT_NONE);
        FOXTargetTreeViewerProvider provider = new FOXTargetTreeViewerProvider();
        xmlViewer.setLabelProvider(provider);

        xmlViewer.setCellModifier(new ICellModifier() {

            @Override
            public boolean canModify(Object element, String property) {
                FOXTreeNode node = (FOXTreeNode) element;
                if (property.equals("C1")) { //$NON-NLS-1$
                    if (node.getLabel() != null && node.getLabel().length() > 0) {
                        return true;
                    }
                }
                if (property.equals("C4")) { //$NON-NLS-1$
                    if (node.getDefaultValue() != null && node.getDefaultValue().length() > 0) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Object getValue(Object element, String property) {
                FOXTreeNode node = (FOXTreeNode) element;
                if (property.equals("C1")) { //$NON-NLS-1$
                    return node.getLabel();
                }
                if (property.equals("C4")) { //$NON-NLS-1$
                    return node.getDefaultValue();
                }

                return null;
            }

            @Override
            public void modify(Object element, String property, Object value) {
                TreeItem treeItem = (TreeItem) element;
                FOXTreeNode node = (FOXTreeNode) treeItem.getData();
                if (property.equals("C1")) { //$NON-NLS-1$
                    node.setLabel((String) value);
                }
                if (property.equals("C4")) { //$NON-NLS-1$
                    node.setDefaultValue((String) value);
                }
                xmlViewer.refresh(node);
            }
        });
        xmlViewer.setColumnProperties(new String[] { "C1", "C2", "C3", "C4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        CellEditor editor = new TextCellEditor(xmlViewer.getTree());

        editor.addListener(new DialogErrorXMLLabelCellEditor(editor, "C1")); //$NON-NLS-1$

        // add by wzhang for bug 8572. set Default value column to be edit.
        CellEditor editorDefault = new TextCellEditor(xmlViewer.getTree());
        editorDefault.addListener(new DialogErrorXMLLabelCellEditor(editorDefault, "C4")); //$NON-NLS-1$

        xmlViewer.setCellEditors(new CellEditor[] { editor, null, null, editorDefault });

        xmlViewer.setContentProvider(provider);
        xmlViewer.setInput(this.foxManager.getTreeData());
        createAction();
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                FOXUI.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(xmlViewer.getControl());
        xmlViewer.getControl().setMenu(menu);
        xmlViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                // TODO Auto-generated method stub

            }

        });
        refreshXMLViewer(null);
    }

    /**
     * Comment method "fillContextMenu".
     *
     * @param manager
     */
    protected void fillContextMenu(IMenuManager manager) {
        if (!xmlViewer.getSelection().isEmpty()) {
            manager.add(createAction);
            manager.add(createAttributeAction);
            if (!foxManager.getFoxComponent().istWriteJSONField()) {
                manager.add(createNamespaceAction);
            }
            // manager.add(editAction);
            manager.add(new Separator());
            manager.add(deleteAction);
            manager.add(disconnectAction);
            manager.add(fixValueAction);
            manager.add(new Separator());
            // manager.add(guessLoopAction);
            manager.add(setLoopAction);
            manager.add(new Separator());
            manager.add(setGroupAction);
            manager.add(removeGroupAction);
            manager.add(new Separator());
            manager.add(importFromXMLAction);
        }
    }

    // just judge if select the advance model
    private boolean getValue() {
        IElementParameter elementParameter = externalNode.getElementParameter("MERGE");//$NON-NLS-1$
        if (elementParameter != null) {
            return (Boolean) elementParameter.getValue();
        }
        return false;
    }

    private void createAction() {
        createAction = new CreateElementAction(xmlViewer, this, Messages.getString("FOXUI.10")); //$NON-NLS-1$
        createAttributeAction = new CreateAttributeAction(xmlViewer, this, Messages.getString("FOXUI.11")); //$NON-NLS-1$
        createNamespaceAction = new CreateNameSpaceAction(xmlViewer, this, Messages.getString("FOXUI.22")); //$NON-NLS-1$
        // editAction = new EditLabelAction(xmlViewer, "Edit Label");
        deleteAction = new DeleteNodeAction(xmlViewer, this, Messages.getString("FOXUI.12")); //$NON-NLS-1$
        disconnectAction = new DisconnectAction(xmlViewer, this, Messages.getString("FOXUI.13")); //$NON-NLS-1$
        fixValueAction = new FixValueAction(xmlViewer, this, Messages.getString("FOXUI.41")); //$NON-NLS-1$
        // disconnectAction.setToolTipText("Disconnect the linker of the current tree node.");
        importFromXMLAction = new ImportTreeFromXMLAction(xmlViewer, this, Messages.getString("FOXUI.14")); //$NON-NLS-1$
        // importFromXMLAction
        // .setToolTipText("Discard the current tree and then import a hierachy tree from an existing xml file.");
        // guessLoopAction = new GuessLoopAction(xmlViewer, Messages.getString("FOXUI.15")); //$NON-NLS-1$
        setLoopAction = new SetForLoopAction(xmlViewer, this, Messages.getString("FOXUI.16"), this.getValue()); //$NON-NLS-1$
        setGroupAction = new SetGroupAction(xmlViewer, this, Messages.getString("FOXUI.17"), this.getValue()); //$NON-NLS-1$
        //        removeGroupAction = new RemoveGroupAction(xmlViewer, Messages.getString("FOXUI.18")); //$NON-NLS-1$
        removeGroupAction = new RemoveGroupAction(xmlViewer, Messages.getString("FOXUI.18"), this); //$NON-NLS-1$

    }

    private void addSchemaViewer(final Composite mainComposite, final int width, final int height) {
        // Group Schema Viewer
        final Group group = Form.createGroup(mainComposite, 1, Messages.getString("FOXUI.19"), height); //$NON-NLS-1$
        // add by wzhang. add a combo for tFileOutputMSXML.
        createCombo(group);
        schemaViewer = new TableViewer(group);

        // schemaViewer.set
        // schemaViewer.getTable().setBackground(schemaViewer.getTable().getDisplay().getSystemColor(SWT.COLOR_WHITE));

        SchemaTableViewerProvider provider = new SchemaTableViewerProvider();
        schemaViewer.setContentProvider(provider);
        schemaViewer.setLabelProvider(provider);

        GridData data2 = new GridData(GridData.FILL_BOTH);
        Table table = schemaViewer.getTable();

        // see bug 7087
        if (canModify || isRepository) {
            table.setEnabled(false);
        }
        // table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setText(Messages.getString("FOXUI.20")); //$NON-NLS-1$

        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            column1.setWidth(mainComposite.getShell().getBounds().width);
            column1.addControlListener(new ControlListener() {

                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    column1.setWidth(mainComposite.getShell().getBounds().width);
                }

            });
        } else {
            column1.setWidth(100);
        }
        table.setLayoutData(data2);
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (footerComp != null) {
                    footerComp.isActivateBtn(false);
                }
            }
        });

    }

    public Composite getFoxUIParent() {
        return this.foxUIParent;
    }

    public FOXManager getFoxManager() {
        return foxManager;
    }

    /**
     * DOC gke FOXUI class global comment. Detailled comment <br/>
     *
     */
    class DialogErrorXMLLabelCellEditor implements ICellEditorListener {

        CellEditor editor;

        String property;

        Boolean validateLabel;

        @Override
        public void applyEditorValue() {
            String text = getControl().getText();
            onValueChanged(text, true, property);
        }

        @Override
        public void cancelEditor() {
        }

        @Override
        public void editorValueChanged(boolean oldValidState, boolean newValidState) {
            onValueChanged(getControl().getText(), false, property);
        }

        private void onValueChanged(final String newValue, boolean showAlertIfError, String property) {
            final Text text = getControl();
            FOXTreeNode selectNode = null;
            ISelection selection = xmlViewer.getSelection();
            if (selection instanceof TreeSelection) {
                Object obj = ((TreeSelection) selection).getFirstElement();
                if (obj instanceof FOXTreeNode) {
                    selectNode = (FOXTreeNode) obj;
                }
            }
            String errorMessage = null;

            // modified for wzhang. fix value can contains space.
            if ("C4".equals(property)) { //$NON-NLS-1$
                validateLabel = StringUtil.validateLabelForFixedValue(text.getText());
            } else {
                if ("C1".equals(property) && selectNode != null && selectNode instanceof NameSpaceNode) {
                    validateLabel = StringUtil.validateLabelForNameSpace(text.getText());
                } else {
                    validateLabel = StringUtil.validateLabelForXML(text.getText());
                }
            }
            if (!validateLabel) {
                errorMessage = Messages.getString("FOXUI.21"); //$NON-NLS-1$
            }

            if (errorMessage == null) {
                text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            } else {
                text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
                if (showAlertIfError) {
                    text.setText(selectedText);
                    MessageDialog.openError(text.getShell(), "Invalid XML label.", errorMessage); //$NON-NLS-1$
                }
            }
        }

        public DialogErrorXMLLabelCellEditor(CellEditor editor, String property) {
            super();
            this.property = property;
            this.editor = editor;
        }

        private Text getControl() {
            return (Text) editor.getControl();
        }

    }

    public void setSelectedText(String label) {
        selectedText = label;
    }

    public void updateStatus() {

        List<FOXTreeNode> allRootTreeData = foxManager.getTreeData();
        int num = 0, rootNum = 0;
        int groupNum = 0;
        List<FOXTreeNode> onLoopNodes = new ArrayList<FOXTreeNode>();
        for (FOXTreeNode node : allRootTreeData) {
            FOXTreeNode rootFOXTreeNode = foxManager.getRootFOXTreeNode(node);
            if (rootFOXTreeNode != null) {
                if (existedLoopNode(rootFOXTreeNode)) {
                    num++;
                } else {
                    onLoopNodes.add(rootFOXTreeNode);
                }
                rootNum++;
                if (existedGroupNode(rootFOXTreeNode)) {
                    groupNum++;
                } else {
                    // onLoopNodes.add(rootFOXTreeNode);
                }
            }
        }
        if (this.getValue()) {
            if (num != rootNum || groupNum != rootNum) {
                String message = Messages.getString("FOXUI.NoLoopOfAdvance"); //$NON-NLS-1$
                if (rootNum > 1) {
                    message = ""; //$NON-NLS-1$
                    for (FOXTreeNode node : onLoopNodes) {
                        message += node.getRow() + ","; //$NON-NLS-1$
                    }
                    message = message.substring(0, message.length() - 1);
                    message += Messages.getString("FOXUI.needLoop"); //$NON-NLS-1$
                }
                header.updateStatus(message);
            } else {
                header.clearStatus();

            }
        } else {
            if (num != rootNum) {
                String message = Messages.getString("FOXUI.NoLoop"); //$NON-NLS-1$

                if (rootNum > 1) {
                    message = ""; //$NON-NLS-1$
                    for (FOXTreeNode node : onLoopNodes) {
                        message += node.getRow() + ","; //$NON-NLS-1$
                    }
                    message = message.substring(0, message.length() - 1);
                    message += Messages.getString("FOXUI.needLoop"); //$NON-NLS-1$
                }
                header.updateStatus(message);
            } else {
                header.clearStatus();

            }
        }
    }

    public void updateNode(FOXTreeNode node, String schema) {
        if (node != null) {
            if (node.getRow() == null) {
                node.setRow(schema);
            }
            for (FOXTreeNode child : node.getChildren()) {
                updateNode(child, schema);
            }
        }
    }

    private boolean existedLoopNode(FOXTreeNode node) {
        if (node != null) {
            if (node.isLoop()) {
                return true;
            }
            for (FOXTreeNode child : node.getChildren()) {
                if (existedLoopNode(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existedGroupNode(FOXTreeNode node) {
        if (node != null) {
            if (node.isGroup()) {
                return true;
            }
            for (FOXTreeNode child : node.getChildren()) {
                if (existedGroupNode(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public TreeViewer getTreeViewer() {
        return this.xmlViewer;
    }
}
