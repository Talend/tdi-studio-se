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
package org.talend.designer.rowgenerator.ui.editor;

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.CellEditorValueAdapterFactory;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.tableeditor.TableEditorContentNotModifiable;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.designer.rowgenerator.data.StringParameter;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.ui.RowGeneratorUI;
import org.talend.designer.rowgenerator.ui.tabs.TabFolderEditors;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-3-8 下午05:52:30 (星期五, 29 九月 2006) qzhang $
 *
 */
public class MetadataTableEditorViewExt extends MetadataTableEditorView {

    public static final String ID_COLUMN_PARAMETER = "ID_COLUMN_PARAMETER"; //$NON-NLS-1$

    public static final String ID_COLUMN_FUNCTION = "ID_COLUMN_FUNCTION"; //$NON-NLS-1$

    public static final String ID_COLUMN_PREVIEW = "ID_COLUMN_PREVIEW"; //$NON-NLS-1$

    private static final int TITLE_DEFAULT_HEIGHT = 25;

    private RowGeneratorComponent rGcomponent;

    private Composite schCom;

    private Composite funCom;

    private Composite preCom;

    private Composite headerTable;

    private Label empty;

    private FunctionManagerExt functionManager;

    private MetadataToolbarEditorViewExt extendedToolbar;

    private RowGeneratorUI generatorUI;

    private int next;

    public MetadataTableEditorViewExt(Composite parentComposite, int mainCompositeStyle,
            ExtendedTableModel<IMetadataColumn> extendedTableModel, boolean readOnly, boolean toolbarVisible,
            RowGeneratorComponent rGcomponent, FunctionManagerExt functionManager) {
        super(parentComposite, mainCompositeStyle, extendedTableModel, readOnly, toolbarVisible, true, false);
        this.rGcomponent = rGcomponent;
        this.functionManager = functionManager;
        setMetadataTalendTypeFilter(NodeUtil.createMetadataTalendTypeFilter(rGcomponent));
        initGraphicComponents();
    }

    @Override
    public void initGraphicComponents() {
        mainComposite = new Composite(parentComposite, SWT.NONE);
        if (parentComposite.getBackground() != null && !parentComposite.getBackground().equals(mainComposite.getBackground())) {
            mainComposite.setBackground(parentComposite.getBackground());
        }
        GridLayout layout = new GridLayout();
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.horizontalSpacing = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        mainComposite.setLayout(layout);

        GridData gridData = new GridData(GridData.FILL_BOTH);

        mainComposite.setLayoutData(gridData);

        createHeaderTable();

        initTable();

        getExtendedTableViewer().getTableViewerCreator().getTableViewer().setComparer(createElementComparer());

        getExtendedTableViewer().getTableViewerCreator().getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

        initToolBar();

        addListeners();

        addTableColumnsListener();

        addHorizontalBarListener();

    }

    @Override
    protected void createColumns(TableViewerCreator<IMetadataColumn> tableViewerCreator, Table table) {
        setShowKeyColumn(false);
        setShowNullableColumn(false);
        setShowPatternColumn(false);
        setShowLengthColumn(false);
        setShowPrecisionColumn(false);
        setShowDefaultColumn(false);
        setShowCommentColumn(false);

        // System.out.println(editorView.isShowDefaultColumn());
        super.createColumns(tableViewerCreator, table);
        // ////////////////////////////////////////////
        configureFunctionColumns(tableViewerCreator);
        // ///////////////////////////////////////////
        configureParameterColumns(tableViewerCreator);
        // ////////////////////////////////////////////
        configurePreviewColumns(tableViewerCreator);
    }

    /**
     * qzhang Comment method "configurePreviewColumns".
     *
     * @param tableViewerCreator
     */
    private void configurePreviewColumns(TableViewerCreator<IMetadataColumn> tableViewerCreator) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setId(ID_COLUMN_PREVIEW);
        column.setTitle(Messages.getString("RowGenTableEditor2.Preview.TitleText")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(getPreviewAccessor());
        column.setModifiable(false);
        column.setWeight(10);
        column.setMinimumWidth(110);
        column.setWidth(120);
    }

    /**
     * qzhang Comment method "getPreviewAccessor".
     *
     * @return
     */
    private IBeanPropertyAccessors<MetadataColumnExt, String> getPreviewAccessor() {
        return new IBeanPropertyAccessors<MetadataColumnExt, String>() {

            @Override
            public String get(MetadataColumnExt bean) {
                if (bean.getFunction() == null) {
                    return ""; //$NON-NLS-1$
                }
                return bean.getFunction().getPreview();
            }

            @Override
            public void set(MetadataColumnExt bean, String value) {
                bean.getFunction().setPreview(value);
                bean.setPreview(value);
            }

        };
    }

    /**
     * qzhang Comment method "configureParameterColumns".
     *
     * @param tableViewerCreator
     */
    private void configureParameterColumns(TableViewerCreator<IMetadataColumn> tableViewerCreator) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setId(ID_COLUMN_PARAMETER);
        column.setTitle(Messages.getString("RowGenTableEditor2.Parameters.TitleText")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(getParameterAccessor());
        column.setModifiable(true);
        column.setWeight(10);
        column.setWidth(45);
    }

    /**
     * qzhang Comment method "getParameterAccessor".
     *
     * @return
     */
    private IBeanPropertyAccessors<MetadataColumnExt, String> getParameterAccessor() {
        return new IBeanPropertyAccessors<MetadataColumnExt, String>() {

            @Override
            public String get(MetadataColumnExt bean) {
                if (bean.getFunction() != null && bean.getFunction().getName().equals(FunctionManagerExt.PURE_PERL_NAME)) {
                    return ((StringParameter) bean.getFunction().getParameters().get(0)).getValue();
                }
                return bean.getParameter();
            }

            @Override
            public void set(MetadataColumnExt bean, String value) {
                if (bean.getFunction() != null && bean.getFunction().getName().equals(FunctionManagerExt.PURE_PERL_NAME)) {
                    ((StringParameter) bean.getFunction().getParameters().get(0)).setValue(value);
                }
                bean.setChanged(true);
            }

        };
    }

    /**
     * qzhang Comment method "configureFunctionColumns".
     *
     * @param tableViewerCreator
     */
    private void configureFunctionColumns(TableViewerCreator<IMetadataColumn> tableViewerCreator) {
        CellEditorValueAdapter comboValueAdapter = CellEditorValueAdapterFactory.getComboAdapterForComboCellEditor("String"); //$NON-NLS-1$
        final TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        final ComboBoxCellEditor functComboBox = new ComboBoxCellEditor();
        functComboBox.create(tableViewerCreator.getTable());
        CCombo functCombo = (CCombo) functComboBox.getControl();
        functCombo.setEditable(false);
        column.setCellEditor(functComboBox, comboValueAdapter);
        // if (functComboBox.getControl() instanceof CCombo) {
        // final CCombo combo = (CCombo) functComboBox.getControl();
        // combo.addSelectionListener(new SelectionAdapter() {
        //
        // /*
        // * (non-Javadoc)
        // *
        // * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
        // */
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // if (getTable() != null) {
        // generatorUI.updateFunParameter(getTable());
        // }
        // }
        // });
        // }
        column.setId(ID_COLUMN_FUNCTION);
        column.setTitle(Messages.getString("RowGenTableEditor2.Fuctions.TitleText")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(getFunctionAccessor(functComboBox));
        column.setModifiable(!readOnly);
        column.setWeight(10);
        column.setWidth(45);
    }

    protected IBeanPropertyAccessors<MetadataColumnExt, String> getFunctionAccessor(final ComboBoxCellEditor functComboBox) {
        return new IBeanPropertyAccessors<MetadataColumnExt, String>() {

            @Override
            public String get(MetadataColumnExt bean) {
                if (bean.getFunction() != null) {
                    if (getTable() != null) {
                        generatorUI.updateFunParameter(getTable());
                    }
                    String[] arrayFunctions = bean.getArrayFunctions();
                    functComboBox.setItems(arrayFunctions);
                    String selectedFunction = FunctionManagerExt.getFunctionLable(bean.getFunction());

                    // need set back the selection of combo after use LazyContentProvider in tableviewer because refresh
                    // will execute after mouseDown event
                    int index = Arrays.asList(arrayFunctions).indexOf(selectedFunction);
                    functComboBox.setValue(index);
                    return selectedFunction;

                }
                return ""; //$NON-NLS-1$
            }

            @Override
            public void set(MetadataColumnExt bean, String value) {

                bean.setFunction(functionManager.getCurrentFunction(value, bean));
                bean.setChanged(true);
            }

        };
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.ui.metadata.editor.MetadataTableEditorView#getTalendTypeAccessor()
     */
    @Override
    protected IBeanPropertyAccessors<IMetadataColumn, String> getTalendTypeAccessor() {
        return new IBeanPropertyAccessors<IMetadataColumn, String>() {

            @Override
            public String get(IMetadataColumn bean) {
                return bean.getTalendType();
            }

            @Override
            public void set(IMetadataColumn bean, String value) {
                if (bean instanceof MetadataColumnExt) {
                    MetadataColumnExt ext = (MetadataColumnExt) bean;
                    ext.setTalendType(value);
                    ext.setFunction(functionManager.getDefaultFunction(ext, value));
                    ext.setChanged(true);
                }
                bean.setPattern(MetadataTableEditorViewExt.this.getJavaDateOrDynTypeForDefaultPattern(bean));

            }

        };
    }

    @Override
    public MetadataTableEditorExt getMetadataTableEditor() {
        return (MetadataTableEditorExt) getExtendedTableModel();
    }

    @Override
    protected ExtendedToolbarView initToolBar() {
        extendedToolbar = new MetadataToolbarEditorViewExt(getMainComposite(), SWT.NONE, getExtendedTableViewer(), this);
        return extendedToolbar;
    }

    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<IMetadataColumn> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
        newTableViewerCreator.setLayoutMode(LAYOUT_MODE.DEFAULT);
    }

    @Override
    public MetadataToolbarEditorViewExt getExtendedToolbar() {
        return this.extendedToolbar;
    }

    protected void preview(TabFolderEditors tabFolderEditors, boolean isGenerate) {
        // tabFolderEditors.refreshPreview(tabFolderEditors.getRowText());
        Table table = tabFolderEditors.getProcessPreview().getTable();
        if (table == null || table.getItemCount() < 1 || next >= table.getItemCount() || isGenerate) {
            tabFolderEditors.refreshPreview(tabFolderEditors.getRowText());
            next = 0;
        }
        if (table.getItems().length > 0) {
            TableItem item = table.getItems()[next];
            next++;
            final int itemCount = getTable().getItemCount();
            for (int i = 1; i < table.getColumnCount(); i++) {
                if (itemCount > i - 1) {
                    MetadataColumnExt ext = (MetadataColumnExt) getTable().getItem(i - 1).getData();
                    ext.setPreview(item.getText(i));
                    ext.setChanged(false);
                }
            }
            getTableViewerCreator().getTableViewer().refresh();
        }

    }

    protected void preview2(TabFolderEditors tabFolderEditors) {
        boolean isGenerate = false;
        for (int i = 0; i < getTable().getItems().length && !isGenerate; i++) {
            MetadataColumnExt ext = (MetadataColumnExt) getTable().getItems()[i].getData();
            if (ext.isChanged()) {
                isGenerate = true;
            }
        }

        preview(tabFolderEditors, isGenerate);
    }

    public RowGeneratorUI getGeneratorUI() {
        return this.generatorUI;
    }

    public void setGeneratorUI(RowGeneratorUI generatorUI) {
        this.generatorUI = generatorUI;
    }

    public void updateHeader(String[] hideColumnsList) {
        if (hideColumnsList == null || hideColumnsList.length == 0) {
            attachLabelPosition();
            return;
        }
        for (String string : hideColumnsList) {
            updateHeader(string, null, true);
        }
        extendedToolbar.updateColumnsList(hideColumnsList);
    }

    public void setRGcomponent(RowGeneratorComponent gcomponent) {
        this.rGcomponent = gcomponent;
    }

    public RowGeneratorComponent getRGcomponent() {
        return this.rGcomponent;
    }

    public void setMetadataTableEditor(MetadataTableEditorExt metadataTableEditor) {
        setExtendedTableModel(metadataTableEditor);
    }

    private void addHorizontalBarListener() {
        final ScrollBar horizontalBar = getTable().getHorizontalBar();
        horizontalBar.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Java)
             *
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                // if (e.detail == SWT.DRAG) {
                int shift = horizontalBar.getSelection();
                attachLabelPosition(shift);
                // }
            }
        });
    }

    /**
     * qzhang Comment method "attachLabelPosition".
     *
     * @param shift
     */
    protected void attachLabelPosition(int shift) {
        attachLabelPosition();
        schCom.setLocation(-shift, schCom.getBounds().y);
        funCom.setLocation(funCom.getBounds().x - shift, funCom.getBounds().y);
        preCom.setLocation(preCom.getBounds().x - shift, preCom.getBounds().y);
    }

    private void addTableColumnsListener() {
        ControlListener controlListener = new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
                MetadataTableEditorViewExt.this.attachLabelPosition(getTable().getHorizontalBar().getSelection());
            }

            @Override
            public void controlResized(ControlEvent e) {
                MetadataTableEditorViewExt.this.attachLabelPosition(getTable().getHorizontalBar().getSelection());
            }

        };
        for (Object element2 : getTableViewerCreator().getColumns()) {
            TableViewerCreatorColumnNotModifiable element = (TableViewerCreatorColumnNotModifiable) element2;
            element.getTableColumn().addControlListener(controlListener);
        }
    }

    private void createHeaderTable() {
        headerTable = new Composite(mainComposite, SWT.NONE);

        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.makeColumnsEqualWidth = false;

        headerTable.setLayout(gridLayout);
        final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = TITLE_DEFAULT_HEIGHT;
        gridData.verticalIndent = 0;
        gridData.horizontalIndent = 0;

        headerTable.setLayoutData(gridData);

        Label titleLabel = createSchemaComposite();

        Label funLabel = createFunctionComposite();

        Label previewLabel = createPreviewComposite();

        setHeaderText(titleLabel, funLabel, previewLabel);
        empty = new Label(headerTable, SWT.NONE);
        empty.setText(""); //$NON-NLS-1$
        empty.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void setHeaderText(Label titleLabel, Label funLabel, Label previewLabel) {
        FontData[] fds = titleLabel.getFont().getFontData();
        for (FontData fd : fds) {
            fd.setHeight(fd.getHeight() + 2);
            fd.setStyle(fd.getStyle() | SWT.BOLD);
        }
        Font titleFont = new Font(titleLabel.getDisplay(), fds);
        titleLabel.setFont(titleFont);
        funLabel.setFont(titleFont);
        previewLabel.setFont(titleFont);

        titleLabel.setText(Messages.getString("RowGeneratorUI.SchemaTitle.Text")); //$NON-NLS-1$
        funLabel.setText(Messages.getString("RowGenTableEditor2.Fuctions.TitleText")); //$NON-NLS-1$
        previewLabel.setText(Messages.getString("RowGenTableEditor2.Preview.TitleText")); //$NON-NLS-1$
    }

    private Label createPreviewComposite() {
        preCom = new Composite(headerTable, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;

        final GridData gridData = new GridData();
        gridData.heightHint = TITLE_DEFAULT_HEIGHT;
        // gridData.horizontalAlignment = SWT.LEFT;
        // gridData.verticalAlignment = SWT.CENTER;

        preCom.setLayout(gridLayout);
        preCom.setLayoutData(gridData);

        Label separator = new Label(preCom, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        Label previewLabel = new Label(preCom, SWT.NONE);
        previewLabel.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        if (parentComposite.getBackground() != null && !parentComposite.getBackground().equals(previewLabel.getBackground())) {
            previewLabel.setBackground(parentComposite.getBackground());
        }
        Button refresh = new Button(preCom, SWT.NONE);
        refresh.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        GridData gridData2 = new GridData(GridData.FILL_VERTICAL);
        gridData2.horizontalAlignment = GridData.BEGINNING;
        refresh.setLayoutData(gridData2);
        refresh.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Java)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshPreviewColumn();
            }
        });
        return previewLabel;
    }

    private Label createFunctionComposite() {
        funCom = new Composite(headerTable, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();

        gridLayout.numColumns = 2;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;

        funCom.setLayout(gridLayout);

        final GridData gridData = new GridData();
        gridData.heightHint = TITLE_DEFAULT_HEIGHT;
        gridData.verticalIndent = 0;
        gridData.horizontalIndent = 0;
        funCom.setLayoutData(gridData);

        Label separator = new Label(funCom, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        Label funLabel = new Label(funCom, SWT.NONE);
        GridData funLabelGd = new GridData(GridData.FILL_BOTH);
        funLabel.setLayoutData(funLabelGd);

        if (parentComposite.getBackground() != null && !parentComposite.getBackground().equals(funLabel.getBackground())) {
            funLabel.setBackground(parentComposite.getBackground());
        }
        return funLabel;
    }

    private Label createSchemaComposite() {

        schCom = new Composite(headerTable, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.horizontalSpacing = 10;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        schCom.setLayout(gridLayout);
        final GridData gridData = new GridData();
        gridData.heightHint = TITLE_DEFAULT_HEIGHT;
        gridData.verticalIndent = 0;
        gridData.horizontalIndent = 0;
        schCom.setLayoutData(gridData);
        new Label(schCom, SWT.NONE);

        Label titleLabel = new Label(schCom, SWT.NONE);
        titleLabel.setLayoutData(gridData);
        if (parentComposite.getBackground() != null && !parentComposite.getBackground().equals(titleLabel.getBackground())) {
            titleLabel.setBackground(parentComposite.getBackground());
        }
        return titleLabel;
    }

    public void attachLabelPosition() {
        int funwidth = 0;
        int prewidth = 0;
        int w = 0;
        for (Object element2 : getTableViewerCreator().getColumns()) {
            TableViewerCreatorColumnNotModifiable element = (TableViewerCreatorColumnNotModifiable) element2;
            w += element.getTableColumn().getWidth();
        }
        prewidth = w - getTableViewerCreator().getColumn(ID_COLUMN_PREVIEW).getTableColumn().getWidth();

        funwidth = prewidth - getTableViewerCreator().getColumn(ID_COLUMN_FUNCTION).getTableColumn().getWidth()
                - getTableViewerCreator().getColumn(ID_COLUMN_PARAMETER).getTableColumn().getWidth();

        // GridDataFactory.swtDefaults().hint(funwidth, TITLE_DEFAULT_HEIGHT).applyTo(schCom);
        schCom.setSize(funwidth, TITLE_DEFAULT_HEIGHT);
        funCom.setLocation(funwidth, schCom.getBounds().y);
        // GridDataFactory.swtDefaults().hint(prewidth - funwidth, TITLE_DEFAULT_HEIGHT).applyTo(funCom);
        funCom.setSize(prewidth - funwidth, TITLE_DEFAULT_HEIGHT);
        preCom.setLocation(prewidth, schCom.getBounds().y);
        // GridDataFactory.swtDefaults().hint(w - prewidth, TITLE_DEFAULT_HEIGHT).applyTo(preCom);
        // preCom.setSize(w - prewidth, TITLE_DEFAULT_HEIGHT);
        // getTableViewerCreator().getColumn(PREVIEW_ID_COLUMN).getTableColumn().setWidth(preCom.getSize().x);
        empty.setLocation(w, schCom.getBounds().y);
    }

    public void fixedLabelSize() {
        int funwidth = 0;
        int prewidth = 0;
        int w = 0;
        for (Object element2 : getTableViewerCreator().getColumns()) {
            TableViewerCreatorColumnNotModifiable element = (TableViewerCreatorColumnNotModifiable) element2;
            w += element.getTableColumn().getWidth();
        }
        prewidth = w - getTableViewerCreator().getColumn(ID_COLUMN_PREVIEW).getTableColumn().getWidth();

        funwidth = prewidth - getTableViewerCreator().getColumn(ID_COLUMN_FUNCTION).getTableColumn().getWidth()
                - getTableViewerCreator().getColumn(ID_COLUMN_PARAMETER).getTableColumn().getWidth();
        GridDataFactory.swtDefaults().hint(funwidth, TITLE_DEFAULT_HEIGHT).applyTo(schCom);
        funCom.setLocation(schCom.getBounds().x + funwidth, schCom.getBounds().y);
        GridDataFactory.swtDefaults().hint(prewidth - funwidth, TITLE_DEFAULT_HEIGHT).applyTo(funCom);
        preCom.setLocation(schCom.getBounds().x + prewidth, schCom.getBounds().y);
        GridDataFactory.swtDefaults().hint(w - prewidth, TITLE_DEFAULT_HEIGHT).applyTo(preCom);
        empty.setLocation(schCom.getBounds().x + w, schCom.getBounds().y);
    }

    public void updateHeader(String id, String currTitle, boolean isHide) {
        final TableViewerCreatorColumnNotModifiable funColumn = getTableViewerCreator().getColumn(id);
        if (isHide) {
            final TableEditorContentNotModifiable tableEditorContent = funColumn.getTableEditorContent();
            if (tableEditorContent != null && tableEditorContent instanceof CheckboxTableEditorContent) {
                funColumn.setTableEditorContent(null);
            }
            funColumn.getTableColumn().setText(""); //$NON-NLS-1$
            funColumn.getTableColumn().setWidth(0);
            funColumn.setWidth(0);
            funColumn.getTableColumn().setResizable(false);
            funColumn.setMoveable(false);
            if ((id.equals(ID_COLUMN_FUNCTION) && getTableViewerCreator().getColumn(ID_COLUMN_PARAMETER).getTableColumn()
                    .getWidth() == 0)
                    || ((id.equals(ID_COLUMN_PARAMETER)) && getTableViewerCreator().getColumn(ID_COLUMN_FUNCTION)
                            .getTableColumn().getWidth() == 0)) {
                funCom.setVisible(false);
            }
            if (id.equals(ID_COLUMN_PREVIEW)) {
                preCom.setVisible(false);
            }
            getTableViewerCreator().refreshTableEditorControls();
        } else {

            final TableEditorContentNotModifiable tableEditorContent = funColumn.getTableEditorContent();
            if (tableEditorContent == null
                    && (funColumn.getId().equals(ID_COLUMN_KEY) || funColumn.getId().equals(ID_COLUMN_NULLABLE))) {
                final CheckboxTableEditorContent checkboxTableEditorContent = new CheckboxTableEditorContent();
                checkboxTableEditorContent.setToolTipText(currTitle);
                checkboxTableEditorContent.createTableEditor(getTable());
                funColumn.setTableEditorContent(checkboxTableEditorContent);
            }
            funColumn.setModifiable(!isReadOnly());
            funColumn.setMinimumWidth(35);
            funColumn.getTableColumn().setWidth(65);
            funColumn.getTableColumn().setText(currTitle);
            funColumn.getTableColumn().setResizable(true);
            funColumn.setTitle(currTitle);
            funColumn.setMoveable(true);
            if (id.equals(ID_COLUMN_FUNCTION) || id.equals(ID_COLUMN_PARAMETER)) {
                funCom.setVisible(true);
            }
            if (id.equals(ID_COLUMN_PREVIEW)) {
                preCom.setVisible(true);
            }
            getTableViewerCreator().refreshTableEditorControls();
        }
        attachLabelPosition();
    }

    // private Function getFunnctionByName(String talendType, String value) {
    // Function func = null;
    // List<Function> functionByType = functionManager.getFunctionsByType(talendType);
    // for (Function fun : functionByType) {
    // // see bug 8055,remove the getLastName() method in TDQ,it has the same name as in TIS.
    // if (value.equals(FunctionManagerExt.getFunctionLable(fun))) {
    // return (Function) fun.clone();
    // }
    // }
    // return func;
    // }

    /**
     * qzhang Comment method "refreshPreviewColumn".
     */
    public void refreshPreviewColumn() {
        TabFolderEditors tabFolderEditors = generatorUI.getTabFolderEditors();
        preview2(tabFolderEditors);
    }


    /**
     * Getter for functionManager.
     * @return the functionManager
     */
    protected FunctionManagerExt getFunctionManager() {
        return this.functionManager;
    }

}
