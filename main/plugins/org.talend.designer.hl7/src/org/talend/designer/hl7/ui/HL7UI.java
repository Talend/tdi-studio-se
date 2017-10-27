// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.impl.MetadataColumnImpl;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.hl7.HL7InputComponent;
import org.talend.designer.hl7.edit.HL7Tree2SchemaLinker;
import org.talend.designer.hl7.managers.HL7Manager;
import org.talend.designer.hl7.model.IModel;
import org.talend.designer.hl7.model.PrimitiveModel;
import org.talend.designer.hl7.ui.footer.FooterComposite;
import org.talend.designer.hl7.ui.header.HL7Parse;
import org.talend.designer.hl7.ui.header.HeaderComposite;
import org.talend.designer.hl7.ui.provider.HL7MessageTreeContentProvider;
import org.talend.designer.hl7.ui.provider.HL7MessageTreeLabelProvider;
import org.talend.designer.hl7.ui.view.HL7MetadataEmfTableEditorView;

import ca.uhn.hl7v2.model.Message;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 * 
 * $Id: FOXUI.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 * 
 */
public class HL7UI {

    protected HL7Manager hl7Manager;

    private Composite hl7UIParent;

    protected HL7InputComponent externalNode;

    private SashForm MsgToSchemaSash;

    protected TreeViewer messageViewer;

    protected HL7Tree2SchemaLinker linker;

    private List<String> initMsgContentList;

    private Group schemaTargetGroup;

    // private HL7SchemaEditorView hl7SchemaEditorView;
    protected HL7MetadataEmfTableEditorView hl7SchemaEditorView;

    protected MetadataEmfTableEditor metadataEditor;

    protected HL7MessageTreeContentProvider contentProvider;

    protected HL7MessageTreeLabelProvider labelProvider;

    protected String filePath;

    protected HeaderComposite header;

    private String startChar;

    private String endChar;

    protected boolean isRepository;

    public HL7UI(Composite parent, HL7Manager hl7Manager) {
        this.hl7Manager = hl7Manager;
        this.hl7Manager.getUiManager().setHl7UI(this);
        this.initMsgContentList = hl7Manager.getInitMsgContentList();
        this.filePath = hl7Manager.getFilePath();
        this.startChar = hl7Manager.getStartChar();
        this.endChar = hl7Manager.getEndChar();
        externalNode = hl7Manager.getHl7Component();
        this.hl7UIParent = parent;
        hl7UIParent.setLayout(new GridLayout());
    }

    public void init() {
        createContent(hl7UIParent);
    }

    /**
     * Comment method "createContent".
     * 
     * @param child
     */
    private void createContent(Composite mainComposite) {
        judgeRepository();
        header = new HeaderComposite(mainComposite, SWT.NONE, this.filePath, startChar, endChar, hl7Manager, this.isRepository);

        MsgToSchemaSash = new SashForm(mainComposite, SWT.HORIZONTAL | SWT.SMOOTH);
        MsgToSchemaSash.setLayoutData(new GridData(GridData.FILL_BOTH));
        MsgToSchemaSash.setBackgroundMode(SWT.INHERIT_FORCE);

        addMessageViewer(MsgToSchemaSash, 300, 110);
        addSchemaViewer(MsgToSchemaSash, 400, 110);

        MsgToSchemaSash.setWeights(new int[] { 40, 60 });

        linker = new HL7Tree2SchemaLinker(this.MsgToSchemaSash, this.isRepository);
        linker.setMainui(this);
        hl7SchemaEditorView.setLinker(linker);
        GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
        data2.heightHint = 300;
        hl7SchemaEditorView.initGraphicComponents();

        final Composite tableEditorComposite = hl7SchemaEditorView.getMainComposite();
        tableEditorComposite.setLayoutData(data2);
        tableEditorComposite.setBackground(null);
        metadataEditor = new MetadataEmfTableEditor(""); //$NON-NLS-1$
        metadataEditor.setDefaultLabel("newColumn"); //$NON-NLS-1$
        metadataEditor.registerDataList(new ArrayList());
        hl7SchemaEditorView.setMetadataEditor(metadataEditor);

        linker.init(messageViewer, hl7SchemaEditorView);
        linker.setManager(hl7Manager);
        initMessageTree();
        new FooterComposite(mainComposite, SWT.NONE, hl7Manager);
        initSchemaCombo(externalNode.getMetadataList());
        initTableViewer();
        initlinkers();

        boolean jobReadOnly = externalNode.getProcess().isReadOnly();
        if (externalNode.getOriginalNode().getJobletNode() != null) {
            jobReadOnly = externalNode.getOriginalNode().isReadOnly();
        }
        if (jobReadOnly || isRepository) {
            hl7SchemaEditorView.setReadOnly(true);
        }
        addModifylistener();
    }

    private void judgeRepository() {
        IElementParameter elem = externalNode.getElementParameter("PROPERTY_TYPE"); //$NON-NLS-1$
        if (elem != null) {
            String value = (String) elem.getValue();
            if (value != null && value.equals("REPOSITORY")) { //$NON-NLS-1$
                isRepository = true;
            }
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

    @SuppressWarnings("unchecked")
    protected void initlinkers() {
        // 1.set tableviewer by component
        redrawLinkers();
        linker.getBackgroundRefresher().refreshBackground();
        // 2.initlinks initLinkersByPrimitiveModels(this.labelProvider.getAllPrimitives());
    }

    private void initTableViewer() {
        List<IMetadataTable> tables = externalNode.getMetadataList();
        for (IMetadataTable table : tables) {
            MetadataTable loaded = ConvertionHelper.convert(table);
            List<MetadataColumn> columns = loaded.getColumns();
            String trueTableName = table.getLabel();
            if (trueTableName.split("_").length > 2) { // get true table name from node draged from repository
                trueTableName = trueTableName.split("_")[1]; //$NON-NLS-N$  
            }
            if (hl7Manager.getSchemaRelationMap().get(trueTableName) != null) {
                hl7Manager.getSchemaRelationMap().remove(trueTableName);
                hl7Manager.getSchemaRelationMap().put(trueTableName, columns);
            } else {
                hl7Manager.getSchemaRelationMap().put(trueTableName, columns);
            }
            MetadataColumn[] array = new MetadataColumn[columns.size()];
            int i = 0;
            for (MetadataColumn column : columns) {
                MetadataColumn newColumn = copyColumn(column);
                array[i] = newColumn;
                i++;
            }
            if (this.getMetaTableViewer().getSelection() instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) this.getMetaTableViewer().getSelection();
                if (selection.getFirstElement() != null) {
                    String name = ((IModel) selection.getFirstElement()).getDisplayName();
                    if (name.equals(trueTableName)) {
                        for (int j = 0; j < array.length; j++) {
                            hl7SchemaEditorView.getMetadataEditor().add(array[j]);
                        }
                        // hl7SchemaEditorView.getMetadataEditor().addAll(columns);
                    }
                }
            }
        }
    }

    protected MetadataColumn copyColumn(MetadataColumn column) {
        MetadataColumn newColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
        newColumn.setComment(column.getComment());
        newColumn.setDefaultValue(column.getDefaultValue());
        newColumn.setKey(column.isKey());
        newColumn.setLabel(column.getLabel());
        newColumn.setPattern(column.getPattern());
        if (column.getLength() < 0) {
            newColumn.setLength(0);
        } else {
            newColumn.setLength(column.getLength());
        }
        newColumn.setNullable(column.isNullable());
        if (column.getPrecision() < 0) {
            newColumn.setPrecision(0);
        } else {
            newColumn.setPrecision(column.getPrecision());
        }
        newColumn.setTalendType(column.getTalendType());
        newColumn.setSourceType(column.getSourceType());
        if (column.getOriginalField() == null || column.getOriginalField().equals("")) { //$NON-NLS-1$
            newColumn.setLabel(column.getLabel());
        } else {
            newColumn.setOriginalField(column.getOriginalField());
        }
        return newColumn;
    }

    public void initMessageTree() {
        HL7Parse hl7Parse = new HL7Parse();
        List<Message> messageList = new ArrayList<Message>();
        if (initMsgContentList != null && initMsgContentList.size() > 0) {
            messageList = hl7Parse.doParse(initMsgContentList);
            if (messageList != null && messageList.size() > 0) {
                messageViewer.setInput(messageList.toArray());
            } else {
                messageViewer.setInput(null);
            }
        } else {
            filePath = externalNode.getElementParameter(EParameterName.FILENAME.getName()).getValue().toString();
            String filePathNoQuotes = TalendTextUtils.removeQuotes(filePath);
            File file = Path.fromOSString(filePathNoQuotes).toFile();
            if (file.exists()) {
                messageList = hl7Parse.doParse(filePath, startChar, endChar);
                if (messageList != null && messageList.size() > 0) {
                    messageViewer.setInput(messageList.toArray());
                } else {
                    messageViewer.setInput(null);
                }
            }
        }
    }

    public void initSchemaCombo(List<IMetadataTable> tables) {

    }

    private void addSchemaViewer(final Composite mainComposite, final int width, final int height) {

        // Group Schema Viewer
        schemaTargetGroup = Form.createGroup(mainComposite, 1, "Schema View", height); //$NON-NLS-1$
        schemaTargetGroup.setBackgroundMode(SWT.INHERIT_FORCE);
        createCombo(schemaTargetGroup);
        hl7SchemaEditorView = new HL7MetadataEmfTableEditorView(schemaTargetGroup, SWT.BORDER, false);
        hl7SchemaEditorView.setRepository(isRepository);
        // boolean isRepository =
        // hl7SchemaEditorView.setRepository(isRepository);
        hl7SchemaEditorView.setShowDbTypeColumn(true, true, false);
        hl7SchemaEditorView.setShowDbColumnName(true, false);

    }

    private void addModifylistener() {
        hl7SchemaEditorView.getMetadataEditor().addModifiedBeanListener(new IModifiedBeanListener<MetadataColumn>() {

            public void handleEvent(ModifiedBeanEvent<MetadataColumn> event) {
                if (AbstractMetadataTableEditorView.ID_COLUMN_NAME.equals(event.column.getId())) {
                    MetadataColumnImpl bean = (MetadataColumnImpl) event.bean;
                    if (bean != null) {
                        final Map<String, List<MetadataColumn>> schemaRelationMap = hl7Manager.getSchemaRelationMap();
                        if (schemaRelationMap != null) {
                            final String key = bean.getTable().getLabel();
                            final List<MetadataColumn> schema = schemaRelationMap.get(key);
                            if (schema != null) {
                                for (MetadataColumn column : schema) {
                                    if (column.getLabel().equals(event.previousValue)) {
                                        schema.add(copyColumn(bean));
                                        schema.remove(column);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    // just judge if select the advance model

    private void addMessageViewer(final Composite mainComposite, final int width, final int height) {
        // Group Schema Viewer
        final Group group = Form.createGroup(mainComposite, 1, "Message View", height); //$NON-NLS-1$
        messageViewer = new TreeViewer(group);
        GridData data2 = new GridData(GridData.FILL_BOTH);
        Tree tree = messageViewer.getTree();
        // TreeItem ti = new TreeItem(tree, SWT.NONE);
        // ti.setText("Segment");
        tree.setLayoutData(data2);
        messageViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        contentProvider = new HL7MessageTreeContentProvider();
        labelProvider = new HL7MessageTreeLabelProvider();
        messageViewer.setContentProvider(contentProvider);
        messageViewer.setLabelProvider(labelProvider);
    }

    public void redrawLinkers() {
        linker.removeAllLinks();
        linker.getBackgroundRefresher().refreshBackground();
        List<PrimitiveModel> pms = this.labelProvider.getAllPrimitives();
        initLinkersByPrimitiveModels(pms);
    }

    protected void initLinkersByPrimitiveModels(List<PrimitiveModel> pms) {
        TableItem[] tableItems = this.linker.getTarget().getItems();
        TreeItem[] treeItems = this.linker.getSource().getItems();
        TreeItem sourceItem = null;
        String sourceItemName = "";
        Object targetItemData = null;
        List<linkMapPropertis> allLinkProp = new ArrayList<linkMapPropertis>();
        for (PrimitiveModel pm : pms) {
            for (TableItem ti : tableItems) {
                Object data = ti.getData();
                MetadataColumn column = (MetadataColumn) data;
                if (column.getOriginalField().equals(pm.getDisplayName())) {
                    sourceItemName = pm.getDisplayName();
                    sourceItem = findSourceItem(sourceItemName, treeItems);
                    targetItemData = ti.getData();
                    linkMapPropertis linkProp = new linkMapPropertis(sourceItem, targetItemData);
                    allLinkProp.add(linkProp);
                }
            }
        }
        for (linkMapPropertis linkProp : allLinkProp) {
            linker.addLinks(linkProp.getSource(), linkProp.getSource().getData(), this.linker.getTarget(),
                    linkProp.getTargetData());
        }
    }

    private TreeItem findSourceItem(String sourceItemName, TreeItem[] items) {
        TreeItem item2return = null;
        for (TreeItem item : items) {
            if (item.getData() != null && (item.getData() instanceof PrimitiveModel)
                    && ((PrimitiveModel) item.getData()).getDisplayName().equals(sourceItemName)) {
                item2return = item;
            } else {
                item2return = findSourceItem(sourceItemName, item.getItems());
            }
            if (item2return == null) {
                continue;
            } else {
                break;
            }
        }
        return item2return;
    }

    class linkMapPropertis {

        private TreeItem source;

        private Object targetData;

        linkMapPropertis(TreeItem source, Object targetData) {
            this.source = source;
            this.targetData = targetData;
        }

        public TreeItem getSource() {
            return this.source;
        }

        public void setSource(TreeItem source) {
            this.source = source;
        }

        public Object getTargetData() {
            return this.targetData;
        }

        public void setTargetData(Object targetData) {
            this.targetData = targetData;
        }

    }

    public Composite getHl7UIParent() {
        return this.hl7UIParent;
    }

    public HL7Manager gethl7Manager() {
        return hl7Manager;
    }

    public HL7MessageTreeContentProvider getContentProvider() {
        return this.contentProvider;
    }

    public ComboViewer getMetaTableViewer() {
        return null;
    }

    public MetadataEmfTableEditor getMetadataEditor() {
        return this.metadataEditor;
    }

    public List<String> getInitMsgContentList() {
        return this.initMsgContentList;
    }

    public TreeViewer getMessageViewer() {
        return this.messageViewer;
    }

    public HeaderComposite getHeader() {
        return this.header;
    }

    public IConnection getConnection() {
        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(hl7Manager.getHl7Component(),
                IConnectionCategory.FLOW);
        if (incomingConnections.size() > 0) {
            return incomingConnections.get(0);
        }
        return null;
    }

    public void autoMap() {
        linker.updateLinksStyleAndControlsSelection(hl7SchemaEditorView.getTable(), true);
    }

}
