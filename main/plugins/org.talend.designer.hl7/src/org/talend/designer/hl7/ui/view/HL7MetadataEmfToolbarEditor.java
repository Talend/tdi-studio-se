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
package org.talend.designer.hl7.ui.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ExportPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ExportPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ImportPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ImportPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveDownPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveDownPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveUpPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveUpPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ResetDBTypesPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ResetDBTypesPushButtonForExtendedTable;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.ui.metadata.extended.command.ExtendedTableResetDBTypesCommand;
import org.talend.core.ui.metadata.extended.command.MetadataEmfExportXmlCommand;
import org.talend.core.ui.metadata.extended.command.MetadataEmfImportXmlCommand;
import org.talend.core.ui.metadata.extended.command.MetadataEmfPasteCommand;
import org.talend.designer.hl7.edit.HL7Tree2SchemaLinker;
import org.talend.designer.hl7.model.IModel;
import org.talend.designer.hl7.ui.HL7MultiSchemaUI;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * TGU same purpose as MetadataToolbarEditorView but uses EMF model directly $Id: MetadataToolbarEditorView2.java,v 1.1
 * 2006/08/02 19:43:45 tguiu Exp $
 * 
 */
public class HL7MetadataEmfToolbarEditor extends ExtendedToolbarView {

    private String dbmsId;

    private HL7Tree2SchemaLinker linker;

    private List<MetadataColumn> needUpdateInRelationMap = new ArrayList<MetadataColumn>();

    private boolean isRepository;

    /**
     * DOC amaumont MatadataToolbarEditor constructor comment.
     * 
     * @param parent
     * @param style
     * @param metadataEditorView
     */
    public HL7MetadataEmfToolbarEditor(Composite parent, int style,
            AbstractExtendedTableViewer<MetadataColumn> extendedTableViewer) {
        super(parent, style, extendedTableViewer);
    }

    // hywang add
    public HL7MetadataEmfToolbarEditor(Composite parent, int style,
            AbstractExtendedTableViewer<MetadataColumn> extendedTableViewer, HL7Tree2SchemaLinker linker, boolean isRepository) {
        super(parent, style, extendedTableViewer);
        this.linker = linker;
        this.isRepository = isRepository;
    }

    public HL7MetadataEmfToolbarEditor(Composite parent, int style,
            AbstractExtendedTableViewer<MetadataColumn> extendedTableViewer, String dbmsId) {
        this(parent, style, extendedTableViewer);
        this.dbmsId = dbmsId;
        if (dbmsId != null) {
            resetDBTypesButton = createResetDBTypesPushButton(dbmsId);
            updateEnabledStateOfButtons();
        }
    }

    protected ResetDBTypesPushButton createResetDBTypesPushButton(final String dbmsId) {
        return new ResetDBTypesPushButtonForExtendedTable(toolbar, extendedTableViewer, dbmsId) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel) {
                return new ExtendedTableResetDBTypesCommand(extendedTableModel, dbmsId, extendedTableViewer);
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createAddPushButton()
     */
    @Override
    protected AddPushButton createAddPushButton() {
        return new AddPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

            boolean hasSchema = false;

            @Override
            protected Object getObjectToAdd() {
                if (hasSchema) {
                    MetadataEmfTableEditor tableEditorModel = (MetadataEmfTableEditor) getExtendedTableViewer()
                            .getExtendedControlModel();
                    if (tableEditorModel.getMetadataTable() == null) {
                        tableEditorModel.setMetadataTable(ConnectionFactory.eINSTANCE.createMetadataTable());
                    }
                    MetadataColumn metadatacolumn = tableEditorModel.createNewMetadataColumn(dbmsId);
                    metadatacolumn.setLength(226);
                    metadatacolumn.setPrecision(0);
                    updateCurrentTableModelAndMap(tableEditorModel);
                    return metadatacolumn;
                }
                return null;
            }

            @Override
            protected void beforeCommandExecution() {
                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    hasSchema = true;
                    super.beforeCommandExecution();
                } else {
                    hasSchema = false;
                    MessageDialog.openError(HL7MetadataEmfToolbarEditor.this.getParentComposite().getShell(),
                            "Can't add new column!", "Need to init a schema by choosing a file at first");
                }

            }

            private void updateCurrentTableModelAndMap(MetadataEmfTableEditor tableEditorModel) {
                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    String key = ((IModel) selectedObj).getDisplayName();
                    linker.getManager().updateRelationMapping(key, tableEditorModel.createNewMetadataColumn(dbmsId), true);
                }
            }

            @Override
            public boolean getEnabledState() {
                return super.getEnabledState() && !isRepository; // 13749
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createPastButton()
     */
    @Override
    public PastePushButton createPastePushButton() {
        return new PastePushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, Integer indexWhereInsert) {
                return new MetadataEmfPasteCommand(extendedTableModel, indexWhereInsert);
            }

            @Override
            public boolean getEnabledState() {
                return super.getEnabledState() && !isRepository; // 13749
            }

            @Override
            protected void afterCommandExecution(Command executedCommand) {
                MetadataEmfTableEditor tableEditorModel = (MetadataEmfTableEditor) getExtendedTableViewer()
                        .getExtendedControlModel();
                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    String key = ((IModel) selectedObj).getDisplayName();
                    List<MetadataColumn> metadataColumnList = tableEditorModel.getMetadataColumnList();
                    Map<String, List<MetadataColumn>> schemaRelationMap = linker.getManager().getSchemaRelationMap();
                    if (schemaRelationMap != null) {
                        List<MetadataColumn> schemas = new ArrayList<MetadataColumn>();
                        schemas.addAll(metadataColumnList);
                        schemaRelationMap.put(key, schemas);
                    }

                }
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createExportPushButton()
     */
    @Override
    protected ExportPushButton createExportPushButton() {
        return new ExportPushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, File file) {
                return new MetadataEmfExportXmlCommand((MetadataEmfTableEditor) extendedTableModel, file, dbmsId);
            }

            @Override
            public boolean getEnabledState() {
                return true;
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createPastButton()
     */
    @Override
    public ImportPushButton createImportPushButton() {
        return new ImportPushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, File file) {
                return new MetadataEmfImportXmlCommand(extendedTableModel, file);
            }

            @Override
            public boolean getEnabledState() {
                return super.getEnabledState() && !isRepository; // 13749
            }

            @Override
            protected void beforeCommandExecution() {

                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    super.beforeCommandExecution();
                } else {
                    MessageDialog.openError(HL7MetadataEmfToolbarEditor.this.getParentComposite().getShell(),
                            "Can't import xml file", "Need to init a schema by choosing a file at first");
                }

            }

            protected void afterCommandExecution(Command executedCommand) {
                MetadataEmfTableEditor tableEditorModel = (MetadataEmfTableEditor) getExtendedTableViewer()
                        .getExtendedControlModel();
                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    String key = ((IModel) selectedObj).getDisplayName();
                    for (MetadataColumn col : tableEditorModel.getMetadataColumnList()) {
                        linker.getManager().updateRelationMapping(key, col, true);
                    }
                }
                linker.getMainui().redrawLinkers();
                linker.getBackgroundRefresher().refreshBackground();
            }

        };
    }

    @Override
    protected RemovePushButton createRemovePushButton() {
        return new RemovePushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected void beforeCommandExecution() {
                List beanList = this.getExtendedTableViewer().getExtendedTableModel().getBeansList();
                for (int index : this.getExtendedTableViewer().getTable().getSelectionIndices()) {
                    MetadataColumn column = (MetadataColumn) beanList.get(index);
                    needUpdateInRelationMap.add(column);
                }
            }

            @Override
            protected void afterCommandExecution(Command executedCommand) {
                String key = ""; //$NON-NLS-N$
                IStructuredSelection selection = (IStructuredSelection) linker.getMainui().getMetaTableViewer().getSelection();
                if (selection.getFirstElement() != null && selection.getFirstElement() instanceof IModel) {
                    key = ((IModel) selection.getFirstElement()).getDisplayName();
                }
                if (!needUpdateInRelationMap.isEmpty()) {
                    for (MetadataColumn col : needUpdateInRelationMap) {
                        linker.getManager().updateRelationMapping(key, col, false);
                    }
                }
                linker.getMainui().redrawLinkers();
                linker.getBackgroundRefresher().refreshBackground();
            }

            @Override
            public boolean getEnabledState() {
                return super.getEnabledState() && !isRepository; // 13749
            }

        };
    }

    @Override
    protected MoveDownPushButton createMoveDownPushButton() {
        return new MoveDownPushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected void afterCommandExecution(Command executedCommand) {
                MetadataEmfTableEditor tableEditorModel = (MetadataEmfTableEditor) getExtendedTableViewer()
                        .getExtendedControlModel();
                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    String key = ((IModel) selectedObj).getDisplayName();
                    List<MetadataColumn> metadataColumnList = tableEditorModel.getMetadataColumnList();
                    Map<String, List<MetadataColumn>> schemaRelationMap = linker.getManager().getSchemaRelationMap();
                    if (schemaRelationMap != null) {
                        List<MetadataColumn> schemas = new ArrayList<MetadataColumn>();
                        schemas.addAll(metadataColumnList);
                        schemaRelationMap.put(key, schemas);
                    }
                }
                linker.getMainui().redrawLinkers();
                linker.getBackgroundRefresher().refreshBackground();
            }

            @Override
            public boolean getEnabledState() {
                return super.getEnabledState() && !isRepository; // 13749
            }
        };

    }

    @Override
    protected MoveUpPushButton createMoveUpPushButton() {
        return new MoveUpPushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected void afterCommandExecution(Command executedCommand) {
                MetadataEmfTableEditor tableEditorModel = (MetadataEmfTableEditor) getExtendedTableViewer()
                        .getExtendedControlModel();
                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    String key = ((IModel) selectedObj).getDisplayName();
                    List<MetadataColumn> metadataColumnList = tableEditorModel.getMetadataColumnList();
                    Map<String, List<MetadataColumn>> schemaRelationMap = linker.getManager().getSchemaRelationMap();
                    if (schemaRelationMap != null) {
                        List<MetadataColumn> schemas = new ArrayList<MetadataColumn>();
                        schemas.addAll(metadataColumnList);
                        schemaRelationMap.put(key, schemas);
                    }

                }
                linker.getMainui().redrawLinkers();
                linker.getBackgroundRefresher().refreshBackground();
            }

            @Override
            public boolean getEnabledState() {
                return super.getEnabledState() && !isRepository; // 13749
            }
        };
    }

}
