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
package org.talend.repository.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTablePasteCommand;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.types.AutoConversionType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.ui.metadata.celleditor.JavaTypeComboValueAdapter;
import org.talend.core.ui.proposal.AutoConversionProposalProvider;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.AutoConversionTypeModel;

/**
 *
 * created by hcyi on Aug 23, 2016 Detailled comment
 *
 */
public class AutoConversionTypesEditorView extends AbstractDataTableEditorView<AutoConversionType> {

    public AutoConversionTypesEditorView(Composite parent, AutoConversionTypeModel model) {
        super(parent, SWT.NONE, model, false, true, false);
    }

    public AutoConversionTypesEditorView(Composite parent, AutoConversionTypeModel model, boolean labelVisible) {
        super(parent, SWT.NONE, model, false, true, labelVisible);
    }

    @Override
    protected void handleBeforeListenableListOperationEvent(ListenableListEvent<AutoConversionType> event) {
        super.handleBeforeListenableListOperationEvent(event);
    }

    @Override
    protected void handleAfterListenableListOperationEvent(ListenableListEvent<AutoConversionType> event) {
        super.handleAfterListenableListOperationEvent(event);
    }

    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<AutoConversionType> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
    }

    @Override
    protected void createColumns(TableViewerCreator<AutoConversionType> tableViewerCreator, Table table) {
        createSourceDataTypeColumn(tableViewerCreator);
        createTargetDataTypeColumn(tableViewerCreator);
        createConversionFunctionColumn(tableViewerCreator);
    }

    private TableViewerCreatorColumn createSourceDataTypeColumn(TableViewerCreator<AutoConversionType> tableViewerCreator) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("AutoConversionTypesEditor.table.column.sourceDataType")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<AutoConversionType, String>() {

            @Override
            public String get(AutoConversionType bean) {
                return bean.getSourceDataType();
            }

            @Override
            public void set(AutoConversionType bean, String value) {
                bean.setSourceDataType(value);
            }

        });
        column.setModifiable(true);
        column.setWeight(30);
        column.setMinimumWidth(50);
        configureTypeColumn(tableViewerCreator, column);
        return column;
    }

    private TableViewerCreatorColumn createTargetDataTypeColumn(TableViewerCreator<AutoConversionType> tableViewerCreator) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("AutoConversionTypesEditor.table.column.targetDataType")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<AutoConversionType, String>() {

            @Override
            public String get(AutoConversionType bean) {
                return bean.getTargetDataType();
            }

            @Override
            public void set(AutoConversionType bean, String value) {
                bean.setTargetDataType(value);
            }

        });
        column.setModifiable(true);
        column.setWeight(30);
        column.setMinimumWidth(50);
        configureTypeColumn(tableViewerCreator, column);
        return column;
    }

    private void configureTypeColumn(TableViewerCreator<AutoConversionType> tableViewerCreator, TableViewerCreatorColumn column) {
        IBeanPropertyAccessors<AutoConversionType, Boolean> nullableAccessors = new IBeanPropertyAccessors<AutoConversionType, Boolean>() {

            @Override
            public Boolean get(AutoConversionType bean) {
                return Boolean.TRUE;
            }

            @Override
            public void set(AutoConversionType bean, Boolean value) {
                return;
            }
        };
        CellEditorValueAdapter comboValueAdapter = new JavaTypeComboValueAdapter(JavaTypesManager.getDefaultJavaType(),
                nullableAccessors);
        String[] arrayTalendTypes = new String[0];
        try {
            arrayTalendTypes = MetadataTalendType.getTalendTypesLabels();
        } catch (NoClassDefFoundError e) {
            // shouln't be happend
            // e.printStackTrace();
            ExceptionHandler.process(e);
        } catch (ExceptionInInitializerError e) {
            // shouln't be happend
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        ComboBoxCellEditor typeComboEditor = new ComboBoxCellEditor(tableViewerCreator.getTable(), arrayTalendTypes,
                SWT.READ_ONLY);
        CCombo typeCombo = (CCombo) typeComboEditor.getControl();
        typeCombo.setEditable(false);
        column.setCellEditor(typeComboEditor, comboValueAdapter);
    }

    private TableViewerCreatorColumn createConversionFunctionColumn(TableViewerCreator<AutoConversionType> tableViewerCreator) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("AutoConversionTypesEditor.table.column.conversionFunction")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<AutoConversionType, String>() {

            @Override
            public String get(AutoConversionType bean) {
                return bean.getConversionFunction();
            }

            @Override
            public void set(AutoConversionType bean, String value) {
                bean.setConversionFunction(value);
            }

        });

        column.setModifiable(true);
        column.setWeight(60);
        column.setMinimumWidth(50);
        column.setDefaultInternalValue(""); //$NON-NLS-1$

        AutoConversionProposalProvider functionProposalProvider = new AutoConversionProposalProvider();
        TextCellEditorWithProposal textCellEditor = new TextCellEditorWithProposal(tableViewerCreator.getTable(), column);
        textCellEditor.setContentProposalProvider(functionProposalProvider);
        column.setCellEditor(textCellEditor);
        return column;
    }

    public AutoConversionTypeModel getModel() {
        return (AutoConversionTypeModel) getExtendedTableModel();
    }

    @Override
    protected ExtendedToolbarView initToolBar() {
        return new ExtendedToolbarView(getMainComposite(), SWT.NONE, getExtendedTableViewer()) {

            @Override
            protected AddPushButtonForExtendedTable createAddPushButton() {
                return new AddPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

                    @Override
                    protected Object getObjectToAdd() {
                        AutoConversionType bean = new AutoConversionType();
                        bean.setSourceDataType(JavaTypesManager.getDefaultJavaType().getId());
                        bean.setTargetDataType(JavaTypesManager.getDefaultJavaType().getId());
                        bean.setConversionFunction("String.valueOf(${0})"); //$NON-NLS-1$
                        return bean;
                    }

                };
            }

            @Override
            protected PastePushButton createPastePushButton() {
                return new PastePushButtonForExtendedTable(toolbar, extendedTableViewer) {

                    @Override
                    protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, Integer indexWhereInsert) {
                        return new ExtendedTablePasteCommand(extendedTableModel, indexWhereInsert) {

                            @Override
                            public List<AutoConversionType> createPastableBeansList(ExtendedTableModel extendedTableModel,
                                    List copiedObjectsList) {
                                List<AutoConversionType> beans = new ArrayList<>();
                                beans.addAll(copiedObjectsList);
                                return beans;
                            }
                        };
                    }

                };
            }
        };
    }
}
