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

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.CellEditorValueAdapterFactory;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CheckColumnSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ColumnCellModifier;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnColorProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnLabelProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.CELL_EDITOR_STATE;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.celleditor.DialogErrorForCellEditorListener;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.i18n.Messages;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.ui.metadata.celleditor.JavaTypeComboValueAdapter;
import org.talend.core.ui.proposal.JavaSimpleDateFormatProposalProvider;
import org.talend.designer.hl7.edit.HL7Tree2SchemaLinker;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 * @param <B> Type of beans in table
 */
public abstract class AbstractHL7MetadataTableEditorView<B> extends AbstractDataTableEditorView<B> {

    private static Logger log = Logger.getLogger(AbstractHL7MetadataTableEditorView.class);

    public static final Color READONLY_CELL_BG_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    public static final String ID_COLUMN_NAME = "ID_COLUMN_NAME"; //$NON-NLS-1$

    public static final String ID_COLUMN_KEY = "ID_COLUMN_KEY"; //$NON-NLS-1$

    public static final String ID_COLUMN_TYPE = "ID_COLUMN_TYPE"; //$NON-NLS-1$

    public static final String ID_COLUMN_DBTYPE = "ID_COLUMN_DBTYPE"; //$NON-NLS-1$

    public static final String ID_COLUMN_NULLABLE = "ID_COLUMN_NULLABLE"; //$NON-NLS-1$

    public static final String ID_COLUMN_PATTERN = "ID_COLUMN_PATTERN"; //$NON-NLS-1$

    public static final String ID_COLUMN_DEFAULT = "ID_COLUMN_DEFAULT"; //$NON-NLS-1$

    public static final String ID_COLUMN_COMMENT = "ID_COLUMN_COMMENT"; //$NON-NLS-1$

    public static final String ID_COLUMN_LENGHT = "ID_COLUMN_LENGHT"; //$NON-NLS-1$

    public static final String ID_COLUMN_PRECISION = "ID_COLUMN_PRECISION"; //$NON-NLS-1$

    public static final String ID_COLUMN_DBCOLUMNNAME = "ID_COLUMN_DBCOLUMNNAME"; //$NON-NLS-1$

    protected boolean showDbColumnName;

    protected boolean showDbTypeColumn;

    protected boolean showTalendTypeColumn = true;

    protected boolean showDbTypeColumnAtLeftPosition;

    protected boolean dbTypeColumnWritable;

    protected boolean dbColumnNameWritable;

    private boolean showPatternColumn = true;

    protected String dbmsId;

    protected TableViewerCreatorColumn nameColumn;

    protected HL7Tree2SchemaLinker linker;

    /**
     * DOC amaumont AbstractMetadataTableEditorView constructor comment.
     */
    public AbstractHL7MetadataTableEditorView() {
        super();
    }

    /**
     * DOC amaumont AbstractMetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param initGraphicsComponents
     */
    public AbstractHL7MetadataTableEditorView(Composite parentComposite, int mainCompositeStyle, boolean initGraphicsComponents) {
        super(parentComposite, mainCompositeStyle, initGraphicsComponents);
    }

    /**
     * DOC amaumont AbstractMetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param extendedTableModel
     * @param readOnly
     * @param toolbarVisible
     * @param labelVisible
     * @param initGraphicsComponents
     */
    public AbstractHL7MetadataTableEditorView(Composite parentComposite, int mainCompositeStyle,
            ExtendedTableModel<B> extendedTableModel, boolean readOnly, boolean toolbarVisible, boolean labelVisible,
            boolean initGraphicsComponents) {
        super(parentComposite, mainCompositeStyle, extendedTableModel, readOnly, toolbarVisible, labelVisible,
                initGraphicsComponents);
    }

    /**
     * DOC amaumont AbstractMetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param extendedTableModel
     * @param readOnly
     * @param toolbarVisible
     * @param labelVisible
     */
    public AbstractHL7MetadataTableEditorView(Composite parentComposite, int mainCompositeStyle,
            ExtendedTableModel<B> extendedTableModel, boolean readOnly, boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, extendedTableModel, readOnly, toolbarVisible, labelVisible);
    }

    /**
     * DOC amaumont AbstractMetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param extendedTableModel
     */
    public AbstractHL7MetadataTableEditorView(Composite parentComposite, int mainCompositeStyle,
            ExtendedTableModel<B> extendedTableModel) {
        super(parentComposite, mainCompositeStyle, extendedTableModel);
    }

    /**
     * DOC amaumont AbstractMetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     */
    public AbstractHL7MetadataTableEditorView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#createColumns(org.talend.commons.ui
     * .swt.tableviewer.TableViewerCreator, org.eclipse.swt.widgets.Table)
     */
    @Override
    protected void createColumns(TableViewerCreator<B> tableViewerCreator, Table table) {
        // //////////////////////////////////////////////////////////////////////////////////////
        tableViewerCreator.setReadOnly(this.readOnly);
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(""); //$NON-NLS-1$
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        column.setWidth(15);

        // //////////////////////////////////////////////////////////////////////////////////////
        configureNameColumn(tableViewerCreator);
        if (showDbColumnName) {
            configureDbColumnName(tableViewerCreator);
        }

        // //////////////////////////////////////////////////////////////////////////////////////

        // configureKeyColumn(tableViewerCreator);

        // //////////////////////////////////////////////////////////////////////////////////////

        // configureTypeColumns(tableViewerCreator);

        // final CellEditorValueAdapter finalComboValueAdapter =
        // comboValueAdapter;
        // final String[] finalArrayTalendTypes = arrayTalendTypes;
        //
        // column.setTableEditorContent(new TableEditorContent() {
        //
        // /* (non-Javadoc)
        // * @see
        // org.talend.commons.ui.swt.tableviewer.tableeditor.TableEditorContent#initialize(org.eclipse.swt.widgets.Table,
        // org.eclipse.swt.custom.TableEditor,
        // org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn,
        // java.lang.Object, java.lang.Object)
        // */
        // @Override
        // public Control initialize(Table table, TableEditor tableEditor,
        // TableViewerCreatorColumn currentColumn,
        // Object currentRowObject, Object currentCellValue) {
        // CCombo combo = new CCombo(table, SWT.FLAT | SWT.READ_ONLY);
        // combo.setItems(finalArrayTalendTypes);
        // // combo.setText(String.valueOf(currentCellValue));
        // combo.computeSize(SWT.DEFAULT, table.getItemHeight());
        // // Set attributes of the editor
        // tableEditor.grabHorizontal = true;
        // tableEditor.minimumHeight = combo.getSize().y - 4;
        // tableEditor.minimumWidth = combo.getSize().x;
        // return combo;
        // }
        //
        // });

        // //////////////////////////////////////////////////////////////////////////////////////

        // configureNullableColumn(tableViewerCreator);

        // //////////////////////////////////////////////////////////////////////////////////////

        // configurePatternColumn(tableViewerCreator);

        // //////////////////////////////////////////////////////////////////////////////////////

        // configureLengthColumn(tableViewerCreator);

        // //////////////////////////////////////////////////////////////////////////////////////

        // configurePrecisionColumn(tableViewerCreator);

        // //////////////////////////////////////////////////////////////////////////////////////

        // configureDefaultColumn(tableViewerCreator);

        // //////////////////////////////////////////////////////////////////////////////////////

        // configureCommentColumn(tableViewerCreator);

    }

    /**
     * DOC amaumont Comment method "configureCommentColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configureCommentColumn(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("MetadataTableEditorView.CommentTitle")); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.CommentTitle")); //$NON-NLS-1$
        column.setId(ID_COLUMN_COMMENT);
        column.setBeanPropertyAccessors(getCommentAccessor());
        column.setWeight(10);
        column.setModifiable(!isReadOnly());
        column.setMinimumWidth(20);
        column.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));
    }

    /**
     * DOC amaumont Comment method "getCommentAccessor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, String> getCommentAccessor();

    /**
     * DOC amaumont Comment method "configureDefaultColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configureDefaultColumn(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("MetadataTableEditorView.DefaultTitle")); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.DefaultTitle")); //$NON-NLS-1$
        column.setId(ID_COLUMN_DEFAULT);
        column.setBeanPropertyAccessors(getDefaultValueAccessor());
        column.setWeight(8);
        column.setModifiable(!isReadOnly());
        column.setMinimumWidth(30);
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        column.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));
    }

    /**
     * DOC amaumont Comment method "getDefaultValueAccessor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, String> getDefaultValueAccessor();

    /**
     * DOC amaumont Comment method "configurePrecisionColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configurePrecisionColumn(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("MetadataTableEditorView.PrecisionTitle")); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.PrecisionTitle")); //$NON-NLS-1$
        column.setId(ID_COLUMN_PRECISION);
        column.setBeanPropertyAccessors(getPrecisionAccessor());
        column.setModifiable(!isReadOnly());
        column.setWeight(10);
        column.setMinimumWidth(10);
        column.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()),
                CellEditorValueAdapterFactory.getPositiveIntAdapter());
    }

    /**
     * DOC amaumont Comment method "getPrecisionAccessor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, Long> getPrecisionAccessor();

    /**
     * DOC amaumont Comment method "configureLengthColumn".
     * 
     * @param tableViewerCreator
     * @param positiveIntValueAdapter
     */
    protected void configureLengthColumn(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("MetadataTableEditorView.LengthTitle")); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.LengthTitle")); //$NON-NLS-1$
        column.setId(ID_COLUMN_LENGHT);
        column.setBeanPropertyAccessors(getLengthAccessor());
        column.setModifiable(!isReadOnly());
        column.setWeight(10);
        column.setMinimumWidth(10);
        column.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()),
                CellEditorValueAdapterFactory.getPositiveIntAdapter(true));
    }

    /**
     * DOC amaumont Comment method "getLengthAccessor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, Long> getLengthAccessor();

    /**
     * DOC amaumont Comment method "configurePatternColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configurePatternColumn(TableViewerCreator<B> tableViewerCreator) {
        if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA && showPatternColumn) {
            final TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
            String patternTitle = Messages.getString("MetadataTableEditorView.PatternTitle"); //$NON-NLS-1$
            column.setTitle(patternTitle);
            column.setToolTipHeader(patternTitle);
            column.setId(ID_COLUMN_PATTERN);
            column.setBeanPropertyAccessors(getPatternAccessor());
            column.setModifiable(!isReadOnly());
            column.setWeight(16);
            final ColumnCellModifier columnCellModifier = new ColumnCellModifier(column) {

                public boolean canModify(Object bean) {
                    boolean typeIsDate = currentBeanHasJavaDateType(bean) && !isReadOnly();
                    return typeIsDate;
                }

            };
            column.setColorProvider(new IColumnColorProvider() {

                public Color getBackgroundColor(Object bean) {
                    if (!columnCellModifier.canModify(bean)) {
                        return READONLY_CELL_BG_COLOR;
                    }
                    return null;
                }

                public Color getForegroundColor(Object bean) {
                    return null;
                }

            });
            column.setLabelProvider(new IColumnLabelProvider() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.talend.commons.ui.swt.tableviewer.behavior.IColumnLabelProvider#getLabel(java.lang.Object)
                 */
                public String getLabel(Object bean) {
                    if (!currentBeanHasJavaDateType(bean)) {
                        return ""; //$NON-NLS-1$
                    }
                    return null;
                }

            });
            column.setColumnCellModifier(columnCellModifier);

            JavaSimpleDateFormatProposalProvider proposalProvider = new JavaSimpleDateFormatProposalProvider();
            TextCellEditorWithProposal patternCellEditor = new TextCellEditorWithProposal(tableViewerCreator.getTable(), column);
            ContentProposalAdapterExtended contentProposalAdapter = patternCellEditor.getContentProposalAdapter();
            contentProposalAdapter.setFilterStyle(ContentProposalAdapterExtended.FILTER_NONE);
            contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapterExtended.PROPOSAL_INSERT);
            patternCellEditor.setContentProposalProvider(proposalProvider);
            column.setCellEditor(patternCellEditor, CellEditorValueAdapterFactory.getNullToEmptyStringTextAdapater());

        }
    }

    /**
     * DOC amaumont Comment method "getPatternAccessor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, String> getPatternAccessor();

    /**
     * DOC amaumont Comment method "configureNullableColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configureNullableColumn(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        String nullableTitle = Messages.getString("MetadataTableEditorView.NullableTitle"); //$NON-NLS-1$
        column.setTitle(nullableTitle);
        column.setToolTipHeader(nullableTitle);
        column.setId(ID_COLUMN_NULLABLE);
        column.setBeanPropertyAccessors(getNullableAccessor());
        column.setModifiable(!isReadOnly());
        column.setWidth(56);
        column.setDisplayedValue(""); //$NON-NLS-1$
        column.setSortable(true);
        column.setTableColumnSelectionListener(new CheckColumnSelectionListener(column, tableViewerCreator));
        column.setImageHeader(ImageProvider.getImage(EImage.CHECKED_ICON));
        CheckboxTableEditorContent nullableCheckbox = new CheckboxTableEditorContent();
        nullableCheckbox.setToolTipText(nullableTitle);
        column.setTableEditorContent(nullableCheckbox);
    }

    /**
     * DOC amaumont Comment method "getNullableAccessors".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, Boolean> getNullableAccessor();

    /**
     * DOC amaumont Comment method "configureTypeColumns".
     * 
     * @param tableViewerCreator
     * @param comboValueAdapter
     * @param arrayTalendTypes
     */
    protected void configureTypeColumns(TableViewerCreator<B> tableViewerCreator) {
        if (showDbTypeColumn) {
            if (showDbTypeColumnAtLeftPosition) {
                configureDbTypeColumn(tableViewerCreator);
                if (showTalendTypeColumn) {
                    configureTalendTypeColumn(tableViewerCreator);
                }
            } else {
                if (showTalendTypeColumn) {
                    configureTalendTypeColumn(tableViewerCreator);
                }
                configureDbTypeColumn(tableViewerCreator);
            }
        } else {
            configureTalendTypeColumn(tableViewerCreator);
        }
    }

    /**
     * DOC amaumont Comment method "configureKeyColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configureKeyColumn(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("MetadataTableEditorView.KeyTitle")); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.KeyTitle")); //$NON-NLS-1$
        column.setId(ID_COLUMN_KEY);
        column.setModifiable(!isReadOnly());
        column.setBeanPropertyAccessors(getKeyAccesor());
        column.setWidth(35);
        column.setDisplayedValue(""); //$NON-NLS-1$
        CheckboxTableEditorContent keyCheckbox = new CheckboxTableEditorContent();
        column.setTableEditorContent(keyCheckbox);
        keyCheckbox.setToolTipText(Messages.getString("MetadataTableEditorView.KeyTitle")); //$NON-NLS-1$
    }

    /**
     * DOC amaumont Comment method "getKeyAccesor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, Boolean> getKeyAccesor();

    /**
     * DOC amaumont Comment method "configureNameColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configureNameColumn(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setId(ID_COLUMN_NAME);
        column.setTitle("User Column(Click to edit)"); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.ColumnTitle")); //$NON-NLS-1$

        column.setBeanPropertyAccessors(getLabelAccessor());
        final Image imageKey = ImageProvider.getImage(EImage.KEY_ICON);
        final Image imageEmpty = org.talend.commons.ui.runtime.image.ImageProvider.getImage(EImage.EMPTY);
        final TextCellEditor cellEditor = new TextCellEditor(tableViewerCreator.getTable());
        column.setCellEditor(cellEditor);
        cellEditor.addListener(new DialogErrorForCellEditorListener(cellEditor, column) {

            @Override
            public void newValidValueTyped(int itemIndex, Object previousValue, Object newValue, CELL_EDITOR_STATE state) {
            }

            @Override
            public String validateValue(String newValue, int beanPosition) {
                return validateColumnName(newValue, beanPosition);
            }

        });

        column.setImageProvider(new IColumnImageProvider() {

            public Image getImage(Object element) {
                if (getKeyAccesor().get((B) element)) {
                    return imageKey;
                } else {
                    return imageEmpty;
                }
            }

        });
        column.setWeight(25);
        column.setModifiable(!isReadOnly());
        column.setMinimumWidth(45);
        // final TextCellEditor cellEditor = new TextCellEditor(tableViewerCreator.getTable());
        // cellEditor.addListener(new DialogErrorForCellEditorListener(cellEditor, column) {
        //
        // @Override
        // public void newValidValueTyped(int itemIndex, Object previousValue, Object newValue, CELL_EDITOR_STATE state)
        // {
        // }
        //
        // @Override
        // public String validateValue(String newValue, int beanPosition) {
        // return validateColumnName(newValue, beanPosition);
        // }
        //
        // });
        // column.setCellEditor(cellEditor);
        nameColumn = column;
    }

    /**
     * DOC amaumont Comment method "configureNameColumn".
     * 
     * @param tableViewerCreator
     */
    protected void configureDbColumnName(TableViewerCreator<B> tableViewerCreator) {
        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setId(ID_COLUMN_DBCOLUMNNAME);
        column.setTitle("Mapping Column"); //$NON-NLS-1$ 
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.ColumnTitle")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(getDbColumnNameAccessor());
        column.setWeight(25);
        column.setModifiable(!isReadOnly());
        column.setMinimumWidth(45);
        final TextCellEditor cellEditor = new TextCellEditor(tableViewerCreator.getTable());
        column.setCellEditor(cellEditor);

        column.setColumnCellModifier(new ColumnCellModifier(column) {

            @Override
            public boolean canModify(Object bean) {
                return super.canModify(bean) && canModifyDBColumn(bean);
            }
        });
        column.setCellEditorAppliedListener(new ITableCellValueModifiedListener() {

            public void cellValueModified(TableCellValueModifiedEvent e) {
                linker.getMainui().redrawLinkers();
                linker.getBackgroundRefresher().refreshBackground();
            }
        });
        if (!dbColumnNameWritable) {
            column.setColorProvider(new IColumnColorProvider() {

                public Color getBackgroundColor(Object bean) {
                    return READONLY_CELL_BG_COLOR;
                }

                public Color getForegroundColor(Object bean) {
                    return null;
                }

            });
        }
    }

    protected boolean canModifyDBColumn(Object bean) {
        return dbColumnNameWritable;
    }

    /**
     * DOC amaumont Comment method "validateColumnName".
     * 
     * @param newValue
     * @param beanPosition
     * @return
     */
    protected abstract String validateColumnName(String newValue, int beanPosition);

    /**
     * DOC amaumont Comment method "getLabelAccessor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, String> getLabelAccessor();

    protected abstract IBeanPropertyAccessors<B, String> getDbColumnNameAccessor();

    /**
     * DOC amaumont Comment method "initTalendTypeColumn".
     * 
     * @param tableViewerCreator
     * @param comboValueAdapter
     * @param arrayTalendTypes
     */
    private TableViewerCreatorColumn configureTalendTypeColumn(final TableViewerCreator<B> tableViewerCreator) {

        CellEditorValueAdapter comboValueAdapter = null;
        ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();

        if (codeLanguage == ECodeLanguage.JAVA) {
            comboValueAdapter = new JavaTypeComboValueAdapter<B>(JavaTypesManager.getDefaultJavaType(), getNullableAccessor());
        } else if (codeLanguage == ECodeLanguage.PERL) {
            comboValueAdapter = CellEditorValueAdapterFactory.getComboAdapterForComboCellEditor();
        }

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

        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("MetadataTableEditorView.TypleTitle")); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.TypleTitle")); //$NON-NLS-1$
        column.setId(ID_COLUMN_TYPE);
        column.setBeanPropertyAccessors(getTalendTypeAccessor());
        column.setModifiable(!isReadOnly());
        column.setWeight(10);
        column.setMinimumWidth(30);
        ComboBoxCellEditor typeComboEditor = new ComboBoxCellEditor(tableViewerCreator.getTable(), arrayTalendTypes,
                SWT.READ_ONLY);
        CCombo typeCombo = (CCombo) typeComboEditor.getControl();
        typeCombo.setEditable(false);
        column.setCellEditor(typeComboEditor, comboValueAdapter);
        return column;
    }

    /**
     * DOC amaumont Comment method "getTalendType".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors<B, String> getTalendTypeAccessor();

    /**
     * DOC amaumont Comment method "initDbTypeColumn".
     * 
     * @param tableViewerCreator
     */
    private TableViewerCreatorColumn configureDbTypeColumn(final TableViewerCreator<B> tableViewerCreator) {

        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("MetadataTableEditorView.DBTypeTitle")); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("MetadataTableEditorView.DBTypeTitle")); //$NON-NLS-1$
        column.setId(ID_COLUMN_DBTYPE);
        column.setBeanPropertyAccessors(getDbTypeAccessor());
        column.setModifiable(dbTypeColumnWritable && !isReadOnly());
        column.setWeight(10);
        column.setMinimumWidth(60);

        if (dbTypeColumnWritable) {

            CellEditorValueAdapter comboValueAdapter = CellEditorValueAdapterFactory.getComboAdapterForComboCellEditor();
            String[] arrayDbTypes = new String[0];
            arrayDbTypes = MetadataTalendType.getDbTypes(getCurrentDbms()); //$NON-NLS-1$
            // System.out.println("currentDbms:" + getCurrentDbms() + "
            // dbTypes:" + arrayDbTypes);
            ComboBoxCellEditor typeComboEditor = new ComboBoxCellEditor(tableViewerCreator.getTable(), arrayDbTypes,
                    SWT.READ_ONLY);
            CCombo typeCombo = (CCombo) typeComboEditor.getControl();
            typeCombo.setEditable(false);
            column.setCellEditor(typeComboEditor, comboValueAdapter);
        } else {
            column.setColorProvider(new IColumnColorProvider() {

                public Color getBackgroundColor(Object bean) {
                    return READONLY_CELL_BG_COLOR;
                }

                public Color getForegroundColor(Object bean) {
                    return null;
                }

            });
        }

        return column;

    }

    /**
     * DOC amaumont Comment method "getDbTypeAccessor".
     * 
     * @return
     */
    protected abstract IBeanPropertyAccessors getDbTypeAccessor();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#setTableViewerCreatorOptions(org.talend
     * .commons.ui.swt.tableviewer.TableViewerCreator)
     */
    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<B> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
        newTableViewerCreator.setFirstVisibleColumnIsSelection(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#initToolBar()
     */
    @Override
    protected abstract ExtendedToolbarView initToolBar();

    public void setShowDbTypeColumn(boolean showDbTypeColumn, boolean showDbTypeColumnAtLeftPosition, boolean writable) {
        this.showDbTypeColumn = showDbTypeColumn;
        this.showDbTypeColumnAtLeftPosition = showDbTypeColumnAtLeftPosition;
        this.dbTypeColumnWritable = writable;
    }

    public void setShowDbColumnName(boolean showDbColumnName, boolean writable) {
        this.showDbColumnName = showDbColumnName;
        this.dbColumnNameWritable = writable;
    }

    /**
     * DOC ocarbone Comment method "setGridDataSize".
     * 
     * @param minimumWidth
     * @param minimumHeight
     */
    public void setGridDataSize(final int minimumWidth, final int minimumHeight) {
        getMainComposite().setSize(minimumWidth, minimumHeight);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = minimumWidth;
        gridData.minimumHeight = minimumHeight;
        getMainComposite().setLayoutData(gridData);

    }

    /**
     * Getter for showPatternColumn.
     * 
     * @return the showPatternColumn
     */
    public boolean isShowPatternColumn() {
        return this.showPatternColumn;
    }

    /**
     * Sets the showPatternColumn.
     * 
     * @param showPatternColumn the showPatternColumn to set
     */
    public void setShowPatternColumn(boolean showPatternColumn) {
        this.showPatternColumn = showPatternColumn;
    }

    /**
     * DOC amaumont Comment method "currentBeanHasJavaDateType".
     * 
     * @param element
     * @return
     */
    public boolean currentBeanHasJavaDateType(Object element) {
        String talendType = getTalendTypeAccessor().get((B) element);
        boolean typeIsDate = JavaTypesManager.DATE.getId().equals(talendType);
        return typeIsDate;
    }

    public boolean isShowTalendTypeColumn() {
        return this.showTalendTypeColumn;
    }

    public void setShowTalendTypeColumn(boolean showTalenTypeColumn) {
        this.showTalendTypeColumn = showTalenTypeColumn;
    }

    public String getCurrentDbms() {
        if (!this.showDbTypeColumn) {
            return null;
        }
        if (dbmsId == null) {
            return "mysql_id"; //$NON-NLS-1$
        }
        return dbmsId;
    }

    public void setCurrentDbms(String dbmsId) {
        this.dbmsId = dbmsId;
    }

    /*
     * When the type is Date in java, set the default pattern.
     */
    public String getJavaDateTypeForDefaultPattern(Object bean) {
        if (currentBeanHasJavaDateType(bean)) {
            return new JavaSimpleDateFormatProposalProvider().getProposals(null, 0)[0].getContent();
        }
        return ""; //$NON-NLS-1$
    }

    public HL7Tree2SchemaLinker getLinker() {
        return this.linker;
    }

    public void setLinker(HL7Tree2SchemaLinker linker) {
        this.linker = linker;
    }
}
