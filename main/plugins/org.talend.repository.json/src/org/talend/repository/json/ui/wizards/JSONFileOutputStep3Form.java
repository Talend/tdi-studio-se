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
package org.talend.repository.json.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditorView;
import org.talend.repository.model.json.JSONFileNode;

/**
 * wzhang class global comment. Detailled comment
 */
public class JSONFileOutputStep3Form extends AbstractJSONFileStepForm {

    private final MetadataTable metadataTable;

    private LabelledText metadataNameText;

    private LabelledText metadataCommentText;

    private MetadataEmfTableEditor metadataEditor;

    private MetadataEmfTableEditorView tableEditorView;

    private List<MetadataColumn> metadataColumnList = new ArrayList<MetadataColumn>();

    private static final int WIDTH_GRIDDATA_PIXEL = 750;

    public JSONFileOutputStep3Form(Composite parent, ConnectionItem connectionItem, MetadataTable metadataTable,
            String[] existingNames) {
        super(parent, connectionItem, metadataTable, existingNames);
        this.metadataTable = metadataTable;
        setupForm();
    }

    @Override
    protected void initialize() {
        String label = MetadataToolHelper.validateValue(metadataTable.getLabel());
        metadataNameText.setText(label);
        metadataCommentText.setText(metadataTable.getComment());
        metadataEditor.setMetadataTable(metadataTable);
        tableEditorView.setMetadataEditor(metadataEditor);
        tableEditorView.getTableViewerCreator().layout();

    }

    @Override
    protected void adaptFormToReadOnly() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addFields() {
        Composite mainComposite = Form.startNewDimensionnedGridLayout(this, 2, WIDTH_GRIDDATA_PIXEL, 60);
        metadataNameText = new LabelledText(mainComposite, "Name");
        metadataCommentText = new LabelledText(mainComposite, "Comment");

        Group groupMetaData = Form.createGroup(this, 1, "Schema", 280);
        Composite compositeMetaData = Form.startNewGridLayout(groupMetaData, 1);

        Composite compositeTable = Form.startNewDimensionnedGridLayout(compositeMetaData, 1, WIDTH_GRIDDATA_PIXEL, 200);
        compositeTable.setLayout(new FillLayout());
        metadataEditor = new MetadataEmfTableEditor("Description of the Schema");
        tableEditorView = new MetadataEmfTableEditorView(compositeTable, SWT.NONE);
    }

    @Override
    protected void addFieldsListeners() {
        metadataNameText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                MetadataToolHelper.validateSchema(metadataNameText.getText());
                metadataTable.setLabel(metadataNameText.getText());
                checkFieldsValue();
            }
        });
        metadataNameText.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                MetadataToolHelper.checkSchema(getShell(), e);
            }
        });

        metadataCommentText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                metadataTable.setComment(metadataCommentText.getText());
            }
        });

        tableEditorView.getMetadataEditor().addAfterOperationListListener(new IListenableListListener() {

            public void handleEvent(ListenableListEvent event) {
                checkFieldsValue();
            }
        });

    }

    @Override
    protected void addUtilsButtonListeners() {

    }

    @Override
    protected boolean checkFieldsValue() {
        if (metadataNameText.getCharCount() == 0) {
            metadataNameText.forceFocus();
            updateStatus(IStatus.ERROR, "Name must be specified");
            return false;
        } else if (!MetadataToolHelper.isValidSchemaName(metadataNameText.getText())) {
            metadataNameText.forceFocus();
            updateStatus(IStatus.ERROR, "Name content illegals characters");
            return false;
        } else if (isNameAllowed(metadataNameText.getText())) {
            updateStatus(IStatus.ERROR, "This name is already existing");
            return false;
        }
        if (tableEditorView.getMetadataEditor().getBeanCount() > 0) {
            updateStatus(IStatus.OK, null);
            return true;
        }
        updateStatus(IStatus.ERROR, "At least one item must exist on Schema");
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (super.isVisible()) {
            List<MetadataColumn> columns = new ArrayList<MetadataColumn>(metadataEditor.getMetadataColumnList());
            metadataEditor.removeAll();
            metadataEditor.addAll(columns);
        }
    }

    private List<MetadataColumn> createMetadataColumn(EList list) {
        List<MetadataColumn> columnList = new ArrayList<MetadataColumn>();
        if (list.size() == 0) {
            return columnList;
        }
        for (int i = 0; i < list.size(); i++) {
            JSONFileNode node = (JSONFileNode) list.get(i);
            String label = node.getRelatedColumn();
            if (label != null && !label.equals("")) {
                MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                metadataColumn.setLabel(label);
                metadataColumn.setOriginalField(label);
                String type = node.getType();
                if (type != null) {
                    metadataColumn.setTalendType(type);
                }
                columnList.add(metadataColumn);
            }
        }
        return columnList;
    }

}
