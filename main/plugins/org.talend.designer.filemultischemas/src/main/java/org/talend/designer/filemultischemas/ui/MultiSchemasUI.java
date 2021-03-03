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
package org.talend.designer.filemultischemas.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.command.CommandStackForComposite;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.EMetadataEncoding;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.Escape;
import org.talend.core.model.metadata.builder.connection.FieldSeparator;
import org.talend.core.model.metadata.builder.connection.FileFormat;
import org.talend.core.model.metadata.builder.connection.RowSeparator;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.filemultischemas.MultiSchemasComponent;
import org.talend.designer.filemultischemas.data.CSVArrayAndSeparator;
import org.talend.designer.filemultischemas.data.ExternalMultiSchemasUIProperties;
import org.talend.designer.filemultischemas.data.MultiMetadataColumn;
import org.talend.designer.filemultischemas.data.SchemasKeyData;
import org.talend.designer.filemultischemas.managers.MultiSchemasManager;
import org.talend.designer.filemultischemas.managers.UIManager;
import org.talend.designer.filemultischemas.ui.dialog.AddRowDialog;
import org.talend.designer.filemultischemas.ui.dialog.MultiSchemaEventListener;
import org.talend.designer.filemultischemas.ui.preview.MultiSchemasShadowProcessPreview;
import org.talend.designer.filemultischemas.ui.preview.MultiSchemasUIThreadProcessor;
import org.talend.designer.filemultischemas.ui.provider.SchemasTreeContentProvider;
import org.talend.designer.filemultischemas.ui.provider.SchemasTreeLabelProvider;
import org.talend.metadata.managment.ui.preview.ProcessDescription;
import org.talend.metadata.managment.ui.preview.ShadowProcessPreview;
import org.talend.metadata.managment.ui.utils.ShadowProcessHelper;
import org.talend.repository.i18n.Messages;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemasUI {

    protected int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore()
            .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT);

    private static final String EMPTY_VALUE = Messages.getString("FileStep2.empty"); //$NON-NLS-1$

    private static final String[] TEXT_ENCLOSURE_DATA = { EMPTY_VALUE,
            TalendTextUtils.addQuotes("\""), TalendTextUtils.addQuotes("\'"), TalendTextUtils.addQuotes("\\\\") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final String[] ESCAPE_CHAR_DATA = { EMPTY_VALUE,
            TalendTextUtils.addQuotes("\""), TalendTextUtils.addQuotes("\'"), TalendTextUtils.addQuotes("\\\\") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private MultiSchemasManager multiSchemaManager;

    private static final String SCHEMA_NAME_PATTERN = "^[a-zA-Z0-9\\_]+[a-zA-Z0-9\\_]*$"; //$NON-NLS-1$

    /**
     * for schema details viewer.
     */

    private SchameDetailsPropertyAction propertyAction;

    private SchameDetailsColumnAction columnAction;

    /**
     *
     */
    private Composite uiParent;

    private LabelledFileField fileField;

    private LabelledCombo encodingCombo;

    private LabelledCombo fieldSeparatorCombo;

    private LabelledCombo rowSeparatorCombo;

    private LabelledText rowSeparatorText;

    private LabelledText fieldSeparatorText;

    private LabelledText cardText;

    private Button csvRadio, delimitedRadio;

    private LabelledCombo escapeCharCombo, textEnclosureCombo;

    private Label escapeCharFlag, textEnclosureFlag;

    private Button previewBtn;

    private Button fetchBtn, leftBtn, rightBtn;

    private Button addRow, removeRow, editSchema;

    private TreeViewer schemaTreeViewer;

    private TreeViewer schemaDetailsViewer;

    private Label previewInformationLabel;

    private CTabFolder tabFolder;

    private CTabItem previewTabItem;

    private CTabItem outputTabItem;

    private Composite outputComposite;

    private SashForm allContentForm;

    private SashForm headerSashForm;

    private Button useMultiSaparators;

    private LabelledText multiSeparatorsText;

    private LabelledText keyValuesText;

    private LabelledText keyIndexText;

    /**
     *
     */
    private MultiSchemasShadowProcessPreview multiSchemasFilePreview;

    private MultiSchemasUIThreadProcessor processor;

    private MultiSchemaEventListener listener;

    public int getSelectedColumnIndex() {
        return multiSchemaManager.getSelectedColumnIndex();
    }

    public void setSelectedColumnIndex(int selectedColumn) {
        multiSchemaManager.setSelectedColumnIndex(selectedColumn);
    }

    public MultiSchemasUI(Composite uiParent, MultiSchemasManager multiSchemaManager) {
        super();
        this.uiParent = uiParent;
        this.multiSchemaManager = multiSchemaManager;
        this.processor = new MultiSchemasUIThreadProcessor(this);
    }

    private DelimitedFileConnection getConnection() {
        return multiSchemaManager.getRecordConnection();
    }

    /**
     * Sets the listener.
     *
     * @param listener the listener to set
     */
    public void addListener(MultiSchemaEventListener listener) {
        this.listener = listener;
    }

    private Shell getShell() {
        return this.uiParent.getShell();
    }

    public void init() {
        uiParent.setLayout(new GridLayout());

        Composite composite = new Composite(uiParent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new FillLayout());

        ExpandBar bar = new ExpandBar(composite, SWT.V_SCROLL);

        final Composite fileGroup = new Composite(bar, SWT.NONE);
        createFileGroup(fileGroup);
        final ExpandItem settingItem = new ExpandItem(bar, SWT.NONE, 0);
        settingItem.setText(ExternalMultiSchemasUIProperties.SETTINGS_LABEL);
        settingItem.setHeight(fileGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        settingItem.setControl(fileGroup);
        settingItem.setExpanded(true);

        allContentForm = new SashForm(bar, SWT.NONE);
        createViewers(allContentForm);
        final ExpandItem previewItem = new ExpandItem(bar, SWT.NONE, 1);
        previewItem.setText(ExternalMultiSchemasUIProperties.PREVIEW_LABEL);
        previewItem.setHeight(allContentForm.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + 100);
        previewItem.setControl(allContentForm);
        previewItem.setExpanded(true);

        bar.addExpandListener(new ExpandListener() {

            public void itemExpanded(ExpandEvent e) {
                if (e.item != null && e.item instanceof ExpandItem) {
                    if (((ExpandItem) e.item).getText().equals(ExternalMultiSchemasUIProperties.PREVIEW_LABEL)) {
                        if (settingItem.getExpanded()) {
                            previewItem.setHeight(allContentForm.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                        } else {
                            previewItem.setHeight(allContentForm.computeSize(SWT.DEFAULT, SWT.DEFAULT).y
                                    + fileGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                        }
                    } else if (((ExpandItem) e.item).getText().equals(ExternalMultiSchemasUIProperties.SETTINGS_LABEL)) {
                        settingItem.setHeight(fileGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                    }
                }
            }

            public void itemCollapsed(ExpandEvent e) {
                if (e.item != null && e.item instanceof ExpandItem) {
                    if (((ExpandItem) e.item).getText().equals(ExternalMultiSchemasUIProperties.PREVIEW_LABEL)) {
                        // settingItem.setHeight(allContentForm.computeSize(SWT.DEFAULT, SWT.DEFAULT).y
                        // + fileGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                    } else if (((ExpandItem) e.item).getText().equals(ExternalMultiSchemasUIProperties.SETTINGS_LABEL)) {
                        previewItem.setHeight(allContentForm.computeSize(SWT.DEFAULT, SWT.DEFAULT).y
                                + fileGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                    }
                }
            }
        });

        initFieldValues();
        // listener
        addFieldListeners();
        addButtonListeners();
        addFieldsListenersGroupFileSettings();
        addFieldsListenersGroupsEscapeChar();
        // preview
        refreshPreview();
        checkDialog();
        // reload
        reloadSchemaDataFromNode();
    }

    private void reloadSchemaDataFromNode() {
        SchemasKeyData rootData = this.getMultiSchemaManager().retrievePropertiesFromNode();
        schemaTreeViewer.setInput(rootData);
        schemaTreeViewer.refresh();
        getUIManager().packSchemaTreeFirstColumn(schemaTreeViewer);
    }

    protected MultiSchemasComponent getMultiSchemasComponent() {
        return getMultiSchemaManager().getMultiSchemasComponent();
    }

    protected MultiSchemasManager getMultiSchemaManager() {
        return this.multiSchemaManager;
    }

    private void initFieldValues() {
        // hywang add for feature 7373
        String selectedColumn = getMultiSchemaManager().getParameterValue(EParameterName.COLUMNINDEX);
        selectedColumn = TalendTextUtils.removeQuotes(selectedColumn);
        if (selectedColumn != null && !selectedColumn.equals("")) {
            multiSchemaManager.setSelectedColumnIndex(Integer.parseInt(selectedColumn));
        }

        String filePath = getMultiSchemaManager().getParameterValue(EParameterName.FILENAME);
        getConnection().setFilePath(filePath);
        fileField.setText(getConnection().getFilePath());
        getConnection().setFieldSeparatorValue(getMultiSchemaManager().getParameterValue(EParameterName.FIELDSEPARATOR));
        getConnection().setFieldSeparatorType(getFieldSeparatorFromString(getConnection().getFieldSeparatorValue()));

        boolean isUseMuliSaparator = (Boolean) getMultiSchemaManager()
                .getParameterObjectValue(EParameterName.USE_MULTISEPARATORS);
        useMultiSaparators.setSelection(isUseMuliSaparator);

        String encoding = getMultiSchemaManager().getParameterValue(EParameterName.ENCODING);
        getConnection().setEncoding(TalendTextUtils.removeQuotes(encoding));
        encodingCombo.setText(getConnection().getEncoding());

        getConnection().setCsvOption((Boolean) getMultiSchemaManager().getParameterObjectValue(EParameterName.CSV_OPTION));
        if (isUseMuliSaparator || getConnection().isCsvOption()) {
            getConnection().setEscapeType(Escape.CSV);
        } else {
            getConnection().setEscapeType(Escape.DELIMITED);
        }
        getConnection().setTextEnclosure(getMultiSchemaManager().getParameterValue(EParameterName.TEXT_ENCLOSURE));
        getConnection().setEscapeChar(getMultiSchemaManager().getParameterValue(EParameterName.ESCAPE_CHAR));

        getConnection().setRowSeparatorValue(getMultiSchemaManager().getParameterValue(EParameterName.ROWSEPARATOR));
        getConnection().setRowSeparatorType(getRowSeparatorFromString(getConnection().getRowSeparatorValue()));
        rowSeparatorCombo.setText(getConnection().getRowSeparatorType().getLiteral());
        rowSeparatorText.setText(getConnection().getRowSeparatorValue());

        rowSeparatorManager();
        fieldSeparatorCombo.setText(getConnection().getFieldSeparatorType().getName());
        fieldSeparatorText.setText(getConnection().getFieldSeparatorValue());
        multiSeparatorsText.setText(getMultiSchemaManager().getParameterValue(EParameterName.MULTI_SEPARATORS));
        keyIndexText.setText(TalendTextUtils.removeQuotes(getMultiSchemaManager().getParameterValue(EParameterName.COLUMNINDEX)));
        keyValuesText.setText(getMultiSchemaManager().getParameterValue(EParameterName.MULTI_KEYVALUES));
        fieldSeparatorCombo.setEnabled(!isUseMuliSaparator);
        fieldSeparatorText.setEditable(!isUseMuliSaparator);
        multiSeparatorsText.setEditable(isUseMuliSaparator);
        keyIndexText.setEditable(isUseMuliSaparator);
        keyValuesText.setEditable(isUseMuliSaparator);
        csvRadio.setSelection(isUseMuliSaparator || getConnection().isCsvOption());
        delimitedRadio.setSelection(!isUseMuliSaparator && !getConnection().isCsvOption());
        delimitedRadio.setEnabled(!(isUseMuliSaparator && getConnection().isCsvOption()));
        escapeCharCombo.setEnabled(isUseMuliSaparator || getConnection().isCsvOption());
        textEnclosureCombo.setEnabled(isUseMuliSaparator || getConnection().isCsvOption());
        fieldSeparatorManager();
        if (!isUseMuliSaparator) {
            getConnection().setFieldSeparatorValue(getMultiSchemaManager().getParameterValue(EParameterName.FIELDSEPARATOR));
        } else {
            getConnection().setFieldSeparatorValue(getMultiSchemaManager().getParameterValue(EParameterName.MULTI_SEPARATORS));

        }

        // Fields to the Group Escape Char Settings
        textEnclosureCombo.select(0);
        escapeCharCombo.select(0);

        if (Escape.DELIMITED.equals(getConnection().getEscapeType())) {
            csvRadio.setSelection(false);
            delimitedRadio.setSelection(true);
            textEnclosureCombo.setEnabled(false);
            escapeCharCombo.setEnabled(false);
        }
        if (Escape.CSV.equals(getConnection().getEscapeType())) {
            csvRadio.setSelection(true);
            delimitedRadio.setSelection(false);
        }

        String s = getConnection().getEscapeChar();
        if (!(s == null) && !s.equals("") && !s.equals(EMPTY_VALUE)) { //$NON-NLS-1$
            escapeCharCombo.setText(s);
        }
        s = getConnection().getTextEnclosure();
        if (!(s == null) && !s.equals("") && !s.equals(EMPTY_VALUE)) { //$NON-NLS-1$
            textEnclosureCombo.setText(s);
        }

        // clearSelection of the selected combo
        encodingCombo.clearSelection();
        fieldSeparatorCombo.clearSelection();
        rowSeparatorCombo.clearSelection();
        escapeCharCombo.clearSelection();
        textEnclosureCombo.clearSelection();

    }

    private void adapteReadOnly() {
        fileField.setReadOnly(isReadOnly());
        fieldSeparatorCombo.setReadOnly(isReadOnly());
        fieldSeparatorText.setEditable(!isReadOnly());
        rowSeparatorCombo.setReadOnly(isReadOnly());
        rowSeparatorText.setEditable(!isReadOnly());
        encodingCombo.setReadOnly(isReadOnly());
        csvRadio.setEnabled(!isReadOnly());
        delimitedRadio.setEnabled(!isReadOnly());
        leftBtn.setEnabled(!isReadOnly());
        rightBtn.setEnabled(!isReadOnly());
        cardText.setEditable(!isReadOnly());
    }

    private void createFileGroup(Composite fileGroup) {
        fileGroup.setLayout(new GridLayout(3, false));

        fileField = new LabelledFileField(fileGroup, ExternalMultiSchemasUIProperties.FILE_LABEL,
                ExternalMultiSchemasUIProperties.FILE_EXTENSIONS, 1, SWT.BORDER) {

            @Override
            protected void setFileFieldValue(String result) {
                if (result != null) {
                    getTextControl().setText(TalendTextUtils.addQuotes(PathUtils.getPortablePath(result)));
                }
            }

        };
        Composite settings = new Composite(fileGroup, SWT.NONE);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        settings.setLayoutData(layoutData);
        settings.setLayout(new GridLayout(2, false));

        addGroupMultiSchemaSettings(settings);
        addGroupEscapeChar(settings);

    }

    private void addGroupMultiSchemaSettings(final Composite mainComposite) {
        Group group = Form.createGroup(mainComposite, 1, Messages.getString("FileStep2.groupDelimitedFileSettings"), 280); //$NON-NLS-1$

        Composite composite = new Composite(group, SWT.NONE);
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.marginHeight = 1;
        composite.setLayout(gridLayout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        EMetadataEncoding[] values = EMetadataEncoding.values();
        String[] encodingData = new String[values.length];
        for (int j = 0; j < values.length; j++) {
            encodingData[j] = values[j].getName();
        }

        encodingCombo = new LabelledCombo(composite, Messages.getString("FileStep2.encoding"), Messages //$NON-NLS-1$
                .getString("FileStep2.encodingTip"), encodingData, 3, true, SWT.NONE); //$NON-NLS-1$

        // Field Separator Combo & Text
        String[] fieldSeparatorData = getFieldSeparatorStyleSupportByLanguage();

        fieldSeparatorCombo = new LabelledCombo(composite, Messages.getString("FileStep2.fieldSeparator"), Messages //$NON-NLS-1$
                .getString("FileStep2.fieldSeparatorDelimitedTip"), fieldSeparatorData, 1, true, SWT.READ_ONLY); //$NON-NLS-1$

        fieldSeparatorText = new LabelledText(composite, "", 1, true, SWT.RIGHT); //$NON-NLS-1$

        // Dimension of columb of Separator Text
        GridData gridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        gridData.minimumWidth = 80;
        fieldSeparatorText.setLayoutData(gridData);

        // Row Separator Combo & Text
        String[] rowSeparatorData = { RowSeparator.STANDART_EOL_LITERAL.getLiteral(),
                RowSeparator.CUSTOM_STRING_LITERAL.getLiteral() };
        rowSeparatorCombo = new LabelledCombo(composite, Messages.getString("FileStep2.rowSeparator"), Messages //$NON-NLS-1$
                .getString("FileStep2.rowSeparatorTip"), rowSeparatorData, 1, true, SWT.READ_ONLY); //$NON-NLS-1$
        rowSeparatorText = new LabelledText(composite, "", 1, true, SWT.RIGHT); //$NON-NLS-1$

        useMultiSaparators = new Button(group, SWT.CHECK);
        Composite multiComp = new Composite(group, SWT.NONE);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 1;
        multiComp.setLayout(gridLayout);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        multiComp.setLayoutData(gd);

        useMultiSaparators.setText(Messages.getString("MultiSchemasUI.useMultiSchemabtn"));
        multiSeparatorsText = new LabelledText(multiComp, "Multiple Separators", 1, true); //$NON-NLS-1$
        multiSeparatorsText.setEditable(false);
        keyValuesText = new LabelledText(multiComp, Messages.getString("MultiSchemasUI.keyValues"), 1, true); //$NON-NLS-1$
        keyValuesText.setEditable(false);
        keyIndexText = new LabelledText(multiComp, Messages.getString("MultiSchemasUI.keyIndex"), 1, true); //$NON-NLS-1$
        keyIndexText.setEditable(false);

    }

    private String[] getFieldSeparatorStyleSupportByLanguage() {
        ECodeLanguage language = LanguageManager.getCurrentLanguage();
        String[] styles = { FieldSeparator.SEMICOLON_LITERAL.getName(), FieldSeparator.COMMA_LITERAL.getName(),
                FieldSeparator.TABULATION_LITERAL.getName(), FieldSeparator.SPACE_LITERAL.getName(),
                FieldSeparator.ALT_65_LITERAL.getName(), FieldSeparator.CUSTOM_ANSI_LITERAL.getName(),
                FieldSeparator.CUSTOM_UTF8_LITERAL.getName(), FieldSeparator.CUSTOM_REG_EXP_LITERAL.getName() };
        switch (language) {
        case JAVA:
            String[] javaStyles = new String[styles.length - 1];
            System.arraycopy(styles, 0, javaStyles, 0, javaStyles.length);
            return javaStyles;
        default: // PERL
            return styles;
        }
    }

    /**
     * rowSeparator : Adapt Custom Label and set the field Text.
     */
    protected void rowSeparatorManager() {
        RowSeparator separator = RowSeparator.getByName(rowSeparatorCombo.getText());
        getConnection().setRowSeparatorType(separator);

        if (rowSeparatorCombo.getSelectionIndex() == 1) {
            // Adapt Custom Label
            rowSeparatorText.setLabelText(rowSeparatorCombo.getText());
            // rowSeparatorText.forceFocus();
        } else {
            // set the Flag width the char value of the Combo
            // { "Standard EOL", "Custom String" };
            if (rowSeparatorCombo.getSelectionIndex() == 0) {
                if (getConnection().getFormat().toString().equals(FileFormat.MAC_LITERAL.getName())) {
                    rowSeparatorText.setText(TalendTextUtils.QUOTATION_MARK + "\\r" + TalendTextUtils.QUOTATION_MARK); //$NON-NLS-1$
                } else {
                    rowSeparatorText.setText(TalendTextUtils.QUOTATION_MARK + "\\n" + TalendTextUtils.QUOTATION_MARK); //$NON-NLS-1$
                }
            }
            // Init Custom Label
            rowSeparatorText.setLabelText(Messages.getString("FileStep2.correspondingCharacter")); //$NON-NLS-1$
            getConnection().setRowSeparatorValue(rowSeparatorText.getText());
        }
    }

    private RowSeparator getRowSeparatorFromString(String separator) {
        if (separator == null || !separator.equals(TalendTextUtils.addQuotes("\\n"))) {
            return RowSeparator.CUSTOM_STRING_LITERAL;
        }
        return RowSeparator.STANDART_EOL_LITERAL;
    }

    /**
     * fieldSeparator : Adapt Custom Label and set the field Text.
     */
    protected void fieldSeparatorManager() {
        FieldSeparator seperator = FieldSeparator.getByName(fieldSeparatorCombo.getText());
        getConnection().setFieldSeparatorType(seperator);

        if (fieldSeparatorCombo.getSelectionIndex() >= 5) {
            // Adapt Custom Label
            fieldSeparatorText.setLabelText(fieldSeparatorCombo.getText());
            fieldSeparatorText.setText(getConnection().getFieldSeparatorValue());
            fieldSeparatorText.forceFocus();

        } else {
            // set the Flag width the char value of the Combo
            // { "Tabulation", "Semicolon", "Comma", "Space", "''(Alt 65, #A4)", "Custom ANSI", "Custom UTF8",
            switch (fieldSeparatorCombo.getSelectionIndex()) {
            case 0:
                fieldSeparatorText.setText(TalendTextUtils.addQuotes(";")); //$NON-NLS-1$
                break;
            case 1:
                fieldSeparatorText.setText(TalendTextUtils.addQuotes(",")); //$NON-NLS-1$
                break;
            case 2:
                fieldSeparatorText.setText(TalendTextUtils.addQuotes("\\t")); //$NON-NLS-1$
                break;
            case 3:
                fieldSeparatorText.setText(TalendTextUtils.addQuotes(" ")); //$NON-NLS-1$
                break;
            case 4:
                fieldSeparatorText.setText(TalendTextUtils.addQuotes("''")); //$NON-NLS-1$
                break;
            default:
                break;
            }

            // Init Custom Label
            getConnection().setFieldSeparatorValue(fieldSeparatorText.getText());
            fieldSeparatorText.setLabelText(Messages.getString("FileStep2.correspondingCharacter")); //$NON-NLS-1$

        }
    }

    private FieldSeparator getFieldSeparatorFromString(String separator) {
        if (separator == null) {
            return FieldSeparator.SEMICOLON_LITERAL;
        }
        if (separator.equals(TalendTextUtils.addQuotes(";"))) {
            return FieldSeparator.SEMICOLON_LITERAL;
        } else if (separator.equals(TalendTextUtils.addQuotes(","))) {
            return FieldSeparator.COMMA_LITERAL;
        } else if (separator.equals(TalendTextUtils.addQuotes("\\t"))) {
            return FieldSeparator.TABULATION_LITERAL;
        } else if (separator.equals(TalendTextUtils.addQuotes(" "))) {
            return FieldSeparator.SPACE_LITERAL;
        } else if (separator.equals(TalendTextUtils.addQuotes("''"))) {
            return FieldSeparator.ALT_65_LITERAL;
        }
        return FieldSeparator.CUSTOM_UTF8_LITERAL;
    }

    private void addFieldsListenersGroupFileSettings() {
        // Event encodingCombo
        encodingCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setEncoding(encodingCombo.getText());
                checkFieldsValue();
            }
        });

        // Separator Combo (field and row)
        fieldSeparatorCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                // Label Custom of fieldSeparatorText
                fieldSeparatorManager();
            }
        });
        rowSeparatorCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                // Label Custom of rowSeparatorText
                rowSeparatorManager();
            }
        });

        // Separator Text (field and row)
        fieldSeparatorText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setFieldSeparatorValue(fieldSeparatorText.getText());
                checkFieldsValue();
            }
        });

        multiSeparatorsText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setFieldSeparatorValue(multiSeparatorsText.getText());
                previewBtn.setEnabled(checkFieldsValue());
            }
        });

        keyIndexText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character > '9' || e.character < '0') {
                    if (e.character != '\b' || e.keyCode == '\u0016') {
                        e.doit = false;
                    }
                }
            }

        });
        keyIndexText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                boolean modify = false;
                if (keyIndexText.getText() == null || "".equals(keyIndexText.getText())) {
                    multiSchemaManager.setSelectedColumnIndex(0);
                    modify = true;
                } else {
                    int index = 0;
                    try {
                        index = Integer.parseInt(keyIndexText.getText());
                    } catch (Exception ex) {
                        index = 0;
                        keyIndexText.setText(String.valueOf(index));
                    }
                    multiSchemaManager.setSelectedColumnIndex(index);
                    modify = true;
                }
                if (modify) {
                    // refreshPreview();

                }
                previewBtn.setEnabled(checkFieldsValue());
                if (previewBtn.isEnabled()) {
                    refreshPreview();
                }

            }
        });

        keyValuesText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                previewBtn.setEnabled(checkFieldsValue());
            }

        });
        rowSeparatorText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setRowSeparatorValue(rowSeparatorText.getText());
                checkFieldsValue();
            }
        });
        cardText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                IStructuredSelection selection = (IStructuredSelection) schemaTreeViewer.getSelection();
                Object element = selection.getFirstElement();
                if (element != null && (element instanceof SchemasKeyData)) {
                    ((SchemasKeyData) element).setCard(cardText.getText());
                }

            }

        });
    }

    private void addFieldsListenersGroupsEscapeChar() {
        // Radio csvRadio/delimitedRadio : Event modify
        ArrayList<Button> radio2Control = new ArrayList<Button>();
        radio2Control.add(csvRadio);
        radio2Control.add(delimitedRadio);

        Iterator<Button> iButton;
        Button button;

        for (iButton = radio2Control.iterator(); iButton.hasNext();) {
            button = iButton.next();
            // RADIO ONLY
            button.addSelectionListener(new SelectionListener() {

                String escapeCharComboOldValue = ""; //$NON-NLS-1$

                String textEnclosureComboOldValue = ""; //$NON-NLS-1$

                public void widgetDefaultSelected(SelectionEvent e) {
                }

                public void widgetSelected(SelectionEvent e) {
                    Boolean b = !(csvRadio.getSelection());
                    getConnection().setEscapeType(b ? Escape.DELIMITED : Escape.CSV);
                    textEnclosureCombo.setEnabled(!b);
                    escapeCharCombo.setEnabled(!b);
                    if (b) {
                        escapeCharComboOldValue = escapeCharCombo.getText();
                        textEnclosureComboOldValue = textEnclosureCombo.getText();
                        textEnclosureCombo.select(0);
                        escapeCharCombo.select(0);
                        textEnclosureFlag.setText(" "); //$NON-NLS-1$
                        escapeCharFlag.setText(" "); //$NON-NLS-1$
                        checkFieldsValue();
                    } else {
                        // select the old value to the two fields
                        if ((!"".equals(escapeCharComboOldValue)) && (!escapeCharComboOldValue.equals(EMPTY_VALUE))) { //$NON-NLS-1$
                            escapeCharCombo.setText(escapeCharComboOldValue);
                            escapeCharFlag.setText(escapeCharCombo.getText());
                        }
                        if ((!"".equals(textEnclosureComboOldValue)) && (!textEnclosureComboOldValue.equals(EMPTY_VALUE))) { //$NON-NLS-1$
                            textEnclosureCombo.setText(textEnclosureComboOldValue);
                            textEnclosureFlag.setText(textEnclosureCombo.getText());
                        }
                    }
                }
            });
        }

        // Escape Char Combo
        escapeCharCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (escapeCharCombo.getText() != null && !("").equals(escapeCharCombo.getText()) //$NON-NLS-1$
                        && !(EMPTY_VALUE).equals(escapeCharCombo.getText())) {
                    getConnection().setEscapeChar(escapeCharCombo.getText());
                } else {
                    getConnection().setEscapeChar(null);
                }
                checkFieldsValue();
            }
        });
        textEnclosureCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (textEnclosureCombo.getText() != null && !("").equals(textEnclosureCombo.getText()) //$NON-NLS-1$
                        && !(EMPTY_VALUE).equals(textEnclosureCombo.getText())) {
                    getConnection().setTextEnclosure(textEnclosureCombo.getText());
                } else {
                    getConnection().setTextEnclosure(null);
                }
                checkFieldsValue();
            }
        });
    }

    private void addGroupEscapeChar(final Composite mainComposite) {

        // Composite Escape Char
        Group group = Form.createGroup(mainComposite, 3, Messages.getString("FileStep2.groupEscapeCharSettings"), 100); //$NON-NLS-1$

        // CSV or Positionel Radio
        csvRadio = new Button(group, SWT.RADIO);
        csvRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setCsvOption(csvRadio.getSelection());
                if (csvRadio.getSelection()) {
                    getConnection().setSplitRecord(false);
                }
            }
        });

        csvRadio.setText(Messages.getString("FileStep2.csv")); //$NON-NLS-1$
        delimitedRadio = new Button(group, SWT.RADIO);
        delimitedRadio.setText(Messages.getString("FileStep2.delimited")); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        gridData.horizontalSpan = 2;
        delimitedRadio.setLayoutData(gridData);
        getConnection().setCsvOption(false);
        delimitedRadio.setSelection(true);
        // escape Char Combo
        escapeCharCombo = new LabelledCombo(group, Messages.getString("FileStep2.escapeChar"), Messages //$NON-NLS-1$
                .getString("FileStep2.escapeCharTip"), ESCAPE_CHAR_DATA, 1, false, SWT.READ_ONLY); //$NON-NLS-1$
        escapeCharFlag = new Label(group, SWT.NONE);
        escapeCharFlag.setText("    "); //$NON-NLS-1$
        // Text Enclosure Combo
        textEnclosureCombo = new LabelledCombo(group, Messages.getString("FileStep2.textEnclosure"), Messages //$NON-NLS-1$
                .getString("FileStep2.textEnclosureTip"), TEXT_ENCLOSURE_DATA, 1, false, SWT.READ_ONLY); //$NON-NLS-1$
        textEnclosureFlag = new Label(group, SWT.NONE);
        textEnclosureFlag.setText("    "); //$NON-NLS-1$

    }

    private void createViewers(SashForm allContentForm) {
        allContentForm.setLayout(new FillLayout());
        allContentForm.setOrientation(SWT.VERTICAL);
        allContentForm.setSashWidth(ExternalMultiSchemasUIProperties.SASHFORM_WIDTH);

        createHeader(allContentForm);
        creatButtom(allContentForm);

        allContentForm.setWeights(ExternalMultiSchemasUIProperties.getAllSashformWeights());

    }

    private void creatButtom(SashForm allContentForm) {
        //
        Composite composite = new Composite(allContentForm, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 2;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        schemaDetailsViewer = new TreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        final Tree tree = schemaDetailsViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        createCardComposite(composite);

        getUIManager().changeSchemasDetailView(schemaDetailsViewer, getSchemaDetailModel());
    }

    private void createCardComposite(Composite parent) {

        Composite cardComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        cardComposite.setLayout(layout);
        GridData layoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        cardComposite.setLayoutData(layoutData);

        final ToolBar menuBar = new ToolBar(cardComposite, SWT.FLAT | SWT.NO_BACKGROUND);
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).span(2, 1).applyTo(menuBar);
        createMenuBar(menuBar);

        cardText = new LabelledText(cardComposite, "Cardinality");
        layoutData = new GridData();
        layoutData.minimumWidth = 90;
        layoutData.widthHint = 90;
        cardText.getTextControl().setLayoutData(layoutData);
        cardText.setEnabled(false);
    }

    private void createMenuBar(final ToolBar menuBar) {
        ToolItem pullDownButton = new ToolItem(menuBar, SWT.PUSH);
        // Image hoverImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_RENDERED_VIEW_MENU);
        Image hoverImage = PlatformUI.getWorkbench().getSharedImages().getImage("IMG_LCL_RENDERED_VIEW_MENU"); //$NON-NLS-1$
        pullDownButton.setDisabledImage(hoverImage);
        pullDownButton.setImage(hoverImage);
        pullDownButton.setToolTipText("Menu");
        pullDownButton.setWidth(5);

        MenuManager menuManager = new MenuManager("MultiSchema");//$NON-NLS-1$

        columnAction = new SchameDetailsColumnAction(schemaDetailsViewer);
        columnAction.setChecked(!ExternalMultiSchemasUIProperties.isSchemaDetailsModel());
        menuManager.add(columnAction);

        propertyAction = new SchameDetailsPropertyAction(schemaDetailsViewer);
        propertyAction.setChecked(ExternalMultiSchemasUIProperties.isSchemaDetailsModel());
        menuManager.add(propertyAction);

        final Menu aMenu = menuManager.createContextMenu(menuBar.getParent());

        pullDownButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Point toolbarSize = menuBar.getSize();
                toolbarSize = menuBar.toDisplay(0, toolbarSize.y);
                aMenu.setLocation(toolbarSize);
                aMenu.setVisible(true);
            }
        });
    }

    private void createHeader(SashForm allContentForm) {
        //
        tabFolder = new CTabFolder(allContentForm, SWT.NULL);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        previewTabItem = new CTabItem(tabFolder, SWT.NONE);
        previewTabItem.setText(ExternalMultiSchemasUIProperties.PREVIEW_LABEL);
        tabFolder.setSelection(previewTabItem);

        outputTabItem = new CTabItem(tabFolder, SWT.BORDER);
        outputTabItem.setText(ExternalMultiSchemasUIProperties.OUTPUT_LABEL);
        outputComposite = Form.startNewGridLayout(tabFolder, 1);
        outputTabItem.setControl(outputComposite);

        Composite headerComposite = new Composite(tabFolder, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        headerComposite.setLayout(layout);
        previewTabItem.setControl(headerComposite);

        createHeaderStatus(headerComposite);

        createHeaderSashForm(headerComposite);
    }

    private void createHeaderStatus(Composite headerComposite) {
        Composite previewComposite = new Composite(headerComposite, SWT.NONE);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.verticalIndent = 3;
        previewComposite.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 2;
        previewComposite.setLayout(layout);

        previewBtn = new Button(previewComposite, SWT.PUSH);
        previewBtn.setText(ExternalMultiSchemasUIProperties.PREVIEW_STRING);
        layoutData = new GridData();
        layoutData.horizontalIndent = 50;
        previewBtn.setLayoutData(layoutData);

        previewInformationLabel = new Label(previewComposite, SWT.NONE);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalIndent = 20;
        previewInformationLabel.setLayoutData(layoutData);
        previewInformationLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
    }

    private void createHeaderSashForm(Composite headerComposite) {
        GridLayout layout;
        headerSashForm = new SashForm(headerComposite, SWT.NONE);
        headerSashForm.setLayout(new FillLayout());
        headerSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        headerSashForm.setSashWidth(ExternalMultiSchemasUIProperties.SASHFORM_WIDTH);
        //
        multiSchemasFilePreview = new MultiSchemasShadowProcessPreview(this, headerSashForm);
        multiSchemasFilePreview.newTablePreview();

        Composite struComp = new Composite(headerSashForm, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        struComp.setLayout(layout);
        headerSashForm.setWeights(ExternalMultiSchemasUIProperties.getHeaderSashformWeights());
        //
        fetchBtn = new Button(struComp, SWT.PUSH);
        fetchBtn.setText(ExternalMultiSchemasUIProperties.FETCH_LABEL);
        fetchBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        //
        schemaTreeViewer = new TreeViewer(struComp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        Tree tree = schemaTreeViewer.getTree();
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        schemaTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        // SchemasTreeDnD dnd = new SchemasTreeDnD(schemaTreeViewer);
        // dnd.addDragAndDrop();

        schemaTreeViewer.setContentProvider(new SchemasTreeContentProvider());
        schemaTreeViewer.setLabelProvider(new SchemasTreeLabelProvider());

        // schemaTreeViewer.setSorter(new ViewerSorter() {
        //
        // @SuppressWarnings("unchecked")
        // @Override
        // public int compare(Viewer viewer, Object e1, Object e2) {
        // if (e1 instanceof SchemasKeyData && e2 instanceof SchemasKeyData) {
        // return getComparator().compare(((SchemasKeyData) e1).getKeyName(), ((SchemasKeyData) e2).getKeyName());
        // }
        // return super.compare(viewer, e1, e2);
        // }
        //
        // });
        TreeColumn column = new TreeColumn(tree, SWT.LEFT);
        column.setWidth(55);
        column.setText("Schema");//$NON-NLS-1$
        column.setResizable(true);

        column = new TreeColumn(tree, SWT.LEFT);
        column.setWidth(100);
        column.setText("Record");//$NON-NLS-1$
        column.setResizable(true);

        column = new TreeColumn(tree, SWT.LEFT);
        column.setWidth(20);
        column.setText("Separator");//$NON-NLS-1$
        column.setResizable(true);
        schemaTreeViewer.setColumnProperties(new String[] { ExternalMultiSchemasUIProperties.COLUMN_KEY,
                ExternalMultiSchemasUIProperties.COLUMN_RECORD, ExternalMultiSchemasUIProperties.COLUMN_SEPARATOR });

        // hywang for 10263
        int columnCount = schemaTreeViewer.getTree().getColumnCount();
        CellEditor[] editors = new CellEditor[columnCount];
        for (int i = 0; i < columnCount; i++) {
            editors[i] = new TextCellEditor(schemaTreeViewer.getTree());
        }
        schemaTreeViewer.setCellEditors(editors);
        schemaTreeViewer.setCellModifier(new ICellModifier() {

            public void modify(Object element, String property, Object value) {
                if (element != null) {
                    if (element instanceof TreeItem) {
                        TreeItem item = (TreeItem) element;
                        if (item.getData() != null) {
                            if (item.getData() instanceof SchemasKeyData) {
                                SchemasKeyData key = (SchemasKeyData) item.getData();
                                if (property.equals(ExternalMultiSchemasUIProperties.COLUMN_KEY)) {
                                    final boolean flag = checkKeyValue(key.getUniqueRecord(), value.toString());
                                    if (flag) {
                                        key.setUniqueRecord(value.toString());
                                    }
                                }
                                if (property.equals(ExternalMultiSchemasUIProperties.COLUMN_RECORD)) {
                                    key.setRecordType(value.toString());
                                }
                                if (property.equals(ExternalMultiSchemasUIProperties.COLUMN_SEPARATOR)) {
                                    key.setSeparator(value.toString());
                                }
                                // Object input = schemaTreeViewer.getInput();
                                // SchemasKeyData in = (SchemasKeyData) input;
                                // if (in.getChildren().contains(key)) {
                                // in.getChildren().remove(in.getChildren().indexOf(key));
                                // List<MultiMetadataColumn> colums = multiSchemaManager.createPropertiesColumns(key);
                                // key.setMetadataColumns(colums);
                                // in.getChildren().add(key);
                                // }
                            }
                            schemaTreeViewer.refresh();
                        }
                    }
                }
            }

            public Object getValue(Object element, String property) {
                String record = "";
                if (element != null) {
                    if (element instanceof SchemasKeyData) {
                        SchemasKeyData key = (SchemasKeyData) element;
                        if (property.equals(ExternalMultiSchemasUIProperties.COLUMN_KEY)) {
                            record = key.getUniqueRecord();
                        }
                        if (property.equals(ExternalMultiSchemasUIProperties.COLUMN_RECORD)) {
                            record = key.getRecordType();
                        }
                        if (property.equals(ExternalMultiSchemasUIProperties.COLUMN_SEPARATOR)) {
                            record = key.getSeparator();
                        }
                    }
                }
                return record;
            }

            public boolean canModify(Object element, String property) {
                return true;
            }
        });

        Composite operateTreeComposite = new Composite(struComp, SWT.NONE);
        operateTreeComposite.setLayout(new GridLayout(3, false));
        operateTreeComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        addRow = new Button(operateTreeComposite, SWT.NONE);
        addRow.setText("Add");
        removeRow = new Button(operateTreeComposite, SWT.NONE);
        removeRow.setText("Remove");
        removeRow.setEnabled(false);
        editSchema = new Button(operateTreeComposite, SWT.NONE);
        editSchema.setText("Edit Columns");
        editSchema.setEnabled(false);

        Composite operation = new Composite(struComp, SWT.NONE);
        operation.setLayout(new GridLayout(2, false));
        operation.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        leftBtn = new Button(operation, SWT.PUSH);
        leftBtn.setToolTipText("Left");
        leftBtn.setImage(ImageProvider.getImage(EImage.LEFT_ICON));
        leftBtn.setEnabled(false);

        rightBtn = new Button(operation, SWT.PUSH);
        rightBtn.setToolTipText("Right");
        rightBtn.setImage(ImageProvider.getImage(EImage.RIGHT_ICON));
        rightBtn.setEnabled(false);
    }

    private void addFieldListeners() {
        fileField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                getConnection().setFilePath(fileField.getText());
                previewBtn.setEnabled(checkFieldsValue());
                clearPreview();
                refreshPreview();
            }
        });
        rowSeparatorText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                getConnection().setRowSeparatorValue(rowSeparatorText.getText());
                previewBtn.setEnabled(checkFieldsValue());
                clearPreview();

            }
        });
        fieldSeparatorText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                getConnection().setFieldSeparatorValue(fieldSeparatorText.getText());
                previewBtn.setEnabled(checkFieldsValue());
                clearPreview();

            }
        });

        useMultiSaparators.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selected = useMultiSaparators.getSelection();
                fieldSeparatorCombo.setEnabled(!selected);
                fieldSeparatorText.setEditable(!selected);
                multiSeparatorsText.setEditable(selected);
                keyValuesText.setEditable(selected);
                keyIndexText.setEditable(selected);
                escapeCharCombo.setEnabled(selected);
                textEnclosureCombo.setEnabled(selected);
                csvRadio.setSelection(selected);
                delimitedRadio.setSelection(!selected);
                delimitedRadio.setEnabled(!selected);
                if (selected) {
                    getConnection().setFieldSeparatorValue(multiSeparatorsText.getText());
                    multiSchemaManager.setSelectedColumnIndex(Integer.parseInt(keyIndexText.getText()));
                } else {
                    getConnection().setFieldSeparatorValue(fieldSeparatorText.getText());
                }
                clearPreview();
                previewBtn.setEnabled(checkFieldsValue());
                if (previewBtn.isEnabled()) {
                    refreshPreview();
                }
            }

        });

        schemaTreeViewer.getTree().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getUIManager().refreshSchemasDetailView(schemaTreeViewer, schemaDetailsViewer, getSchemaDetailModel());
            }
        });
        schemaTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                removeRow.setEnabled(true);
                editSchema.setEnabled(true);
                if (isReadOnly()) {
                    leftBtn.setEnabled(false);
                    rightBtn.setEnabled(false);
                } else {
                    leftBtn.setEnabled(getUIManager().enableMovedRecord(schemaTreeViewer, true));
                    rightBtn.setEnabled(getUIManager().enableMovedRecord(schemaTreeViewer, false));
                }
                IStructuredSelection selection = (IStructuredSelection) schemaTreeViewer.getSelection();
                Object element = selection.getFirstElement();
                if (element != null && (element instanceof SchemasKeyData) && ((SchemasKeyData) element).getTagLevel() > 0) {
                    cardText.setText(((SchemasKeyData) element).getCard());
                    cardText.setEnabled(true);
                } else {
                    cardText.setText(""); //$NON-NLS-1$
                    cardText.setEnabled(false);
                }
            }
        });
        schemaTreeViewer.getTree().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                //
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //
            }
        });
        schemaTreeViewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {
                // getUIManager().packSchemaTreeFirstColumn(schemaTreeViewer);
            }

            public void treeExpanded(TreeExpansionEvent event) {
                // getUIManager().packSchemaTreeFirstColumn(schemaTreeViewer);
            }
        });
    }

    protected void addButtonListeners() {

        // Event PreviewButton
        previewBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                refreshPreview();
            }
        });
        fetchBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                fetchCodes();
            }
        });
        leftBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                getUIManager().moveRecord(schemaTreeViewer, true);
                getUIManager().packSchemaTreeFirstColumn(schemaTreeViewer);
            }
        });
        rightBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                getUIManager().moveRecord(schemaTreeViewer, false);
                getUIManager().packSchemaTreeFirstColumn(schemaTreeViewer);
            }
        });
        addRow.addSelectionListener(new SelectionAdapter() { // hywang for 10263

            @Override
            public void widgetSelected(SelectionEvent e) {
                SchemasKeyData root = (SchemasKeyData) schemaTreeViewer.getInput();
                AddRowDialog dialog = new AddRowDialog(MultiSchemasUI.this.getShell(), root);
                if (Window.OK == dialog.open()) {
                    Object input = schemaTreeViewer.getInput();
                    if (input instanceof SchemasKeyData) {

                        SchemasKeyData data = (SchemasKeyData) input;
                        List<SchemasKeyData> all = data.getChildren();
                        SchemasKeyData newData = new SchemasKeyData(dialog.getRecordValue());
                        newData.setSeparator(dialog.getSepValue());
                        newData.setUniqueRecord(dialog.getKeyValue());

                        final IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNode(
                                getMultiSchemasComponent(), dialog.getKeyValue());
                        if (metadataTable != null) {
                            multiSchemaManager.createMultiSchemasColumns(newData, metadataTable.clone(true));
                        } else {
                            List<MultiMetadataColumn> colums = multiSchemaManager.createPropertiesColumns(newData);
                            newData.setMetadataColumns(colums);
                        }
                        all.add(newData);
                        data.addChild(newData);
                        newData.setParent(data);
                        schemaTreeViewer.refresh();
                        int len = schemaTreeViewer.getTree().getItems().length;
                        schemaTreeViewer.getTree().select(schemaTreeViewer.getTree().getItems()[len - 1]);
                    }
                    getUIManager().refreshSchemasDetailView(schemaTreeViewer, schemaDetailsViewer, getSchemaDetailModel());
                }
            }
        });
        removeRow.addSelectionListener(new SelectionAdapter() { // hywang for 10263

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        boolean needRefresh = false;
                        if (schemaTreeViewer == null) {
                            return;
                        }
                        SchemasKeyData removeData = null;
                        ISelection selection = schemaTreeViewer.getSelection();
                        Object input = schemaTreeViewer.getInput();
                        if (selection instanceof IStructuredSelection) {
                            Object element = ((IStructuredSelection) selection).getFirstElement();
                            if (element instanceof SchemasKeyData) {
                                removeData = (SchemasKeyData) element;
                            }
                        }
                        if (input instanceof SchemasKeyData) {
                            SchemasKeyData data = (SchemasKeyData) input;
                            List<SchemasKeyData> all = data.getChildren();
                            if (all.contains(removeData)) {
                                all.remove(removeData);
                                schemaTreeViewer.setInput(data);
                                needRefresh = true;
                            }
                            if (needRefresh) {
                                schemaTreeViewer.refresh();
                                int len = schemaTreeViewer.getTree().getItems().length;
                                if (len > 0) {
                                    schemaTreeViewer.getTree().select(schemaTreeViewer.getTree().getItems()[len - 1]);
                                }
                                removeRow.setEnabled(!all.isEmpty());
                                editSchema.setEnabled(!all.isEmpty());
                                getUIManager().refreshSchemasDetailView(schemaTreeViewer, schemaDetailsViewer,
                                        getSchemaDetailModel());
                            }
                        }
                    }
                });

        editSchema.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = schemaTreeViewer.getSelection();
                SchemasKeyData selectedData = null;
                if (selection instanceof IStructuredSelection) {
                    Object element = ((IStructuredSelection) selection).getFirstElement();
                    if (element instanceof SchemasKeyData) {
                        selectedData = (SchemasKeyData) element;
                        IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNode(getMultiSchemasComponent(),
                                selectedData.getUniqueRecord());
                        if (metadataTable == null) {
                            metadataTable = new org.talend.core.model.metadata.MetadataTable();
                        }
                        metadataTable.getListColumns().clear();
                        SchemasKeyData keydata = (SchemasKeyData) schemaTreeViewer.getInput();
                        for (int i = 0; i < keydata.getChildren().size(); i++) {
                            if (selectedData.getUniqueRecord().equals(keydata.getChildren().get(i).getUniqueRecord())) {
                                metadataTable.getListColumns().addAll(keydata.getChildren().get(i).getMetadataColumns());
                                break;
                            }
                        }

                        MetadataDialog dialog = new MetadataDialog(MultiSchemasUI.this.getShell(), metadataTable,
                                getMultiSchemasComponent(), new CommandStackForComposite(MultiSchemasUI.this.getShell()));
                        dialog.setText("Schema of " + selectedData.getUniqueRecord());
                        if (Window.OK == dialog.open()) {
                            metadataTable = dialog.getOutputMetaData();
                            if (!selectedData.getMetadataColumns().isEmpty()) {
                                selectedData.getMetadataColumns().clear();
                            }
                            multiSchemaManager.createMultiSchemasColumns(selectedData, metadataTable);
                            getUIManager()
                                    .refreshSchemasDetailView(schemaTreeViewer, schemaDetailsViewer, getSchemaDetailModel());
                        }
                    }
                }
            }

        });

    }

    @SuppressWarnings("restriction")
    private void fetchCodes() {
        try {
            final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
            dialog.run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Fetch...", IProgressMonitor.UNKNOWN);
                    monitor.setCanceled(false);

                    final CsvArray csvArray = processor.getCsvArray();

                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            SchemasKeyData schemasModel = null;
                            boolean checked = (csvArray != null && csvArray.getRows().size() > 0);
                            CSVArrayAndSeparator uniqueCsvArray = null;

                            if (useMultiSaparators.getSelection()) {
                                getMultiSchemaManager().setKeyValues(keyValuesText.getText());
                            }

                            if (multiSchemasFilePreview.getSelectColumnIndex() < 0
                                    && multiSchemaManager.getSelectedColumnIndex() != 0) {
                                uniqueCsvArray = getMultiSchemaManager().retrieveCsvArrayInUniqueModel(getProcessDescription(),
                                        checked, multiSchemaManager.getSelectedColumnIndex(), useMultiSaparators.getSelection());

                                schemasModel = getMultiSchemaManager().createSchemasTree(uniqueCsvArray,
                                        multiSchemaManager.getSelectedColumnIndex());

                            } else {

                                uniqueCsvArray = getMultiSchemaManager().retrieveCsvArrayInUniqueModel(getProcessDescription(),
                                        checked, multiSchemasFilePreview.getSelectColumnIndex(),
                                        useMultiSaparators.getSelection());

                                schemasModel = getMultiSchemaManager().createSchemasTree(uniqueCsvArray,
                                        multiSchemasFilePreview.getSelectColumnIndex());
                                getMultiSchemaManager().setSelectedColumnIndex(multiSchemasFilePreview.getSelectColumnIndex());
                                schemaTreeViewer.setInput(schemasModel);
                                getUIManager().packSchemaTreeFirstColumn(schemaTreeViewer);
                                clearSchemaDetail();
                                checkDialog();
                            }
                        }
                    });
                    monitor.done();
                }
            });

        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }

    }

    /**
     * clear the table preview.
     */
    void clearPreview() {
        multiSchemasFilePreview.removePreviewContent();
        this.processor.clearCsvArray();
    }

    void clearSchemaTree() {
        schemaTreeViewer.setInput(null);
    }

    void clearSchemaDetail() {
        schemaDetailsViewer.setInput(null);
        // dipose other columns, except first.
        // getUIManager().updateColumns(schemaDetailsViewer.getTree(), 0);
    }

    private UIManager getUIManager() {
        return getMultiSchemaManager().getUIManager();
    }

    /**
     * refreshPreview use ShadowProcess to refresh the preview.
     */
    void refreshPreview() {

        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                if (checkFieldsValue()) {
                    if (useMultiSaparators.getSelection()) {
                        launch();
                    } else {
                        processor.execute();
                    }
                    setFetchButtonStatus(true);
                }
            }
        });
    }

    private void launch() {
        try {
            String filePath = this.multiSchemaManager.getOriginalValue(this.getConnection().getFilePath());
            CsvArray csvArray = getMultiSchemaManager().getCsvArrayForMultiSchemaDelimited(filePath,
                    multiSeparatorsText.getText(), encodingCombo.getText(), keyValuesText.getText(),
                    multiSchemaManager.getSelectedColumnIndex());
            processor.setCsvArray(csvArray);

            Display.getDefault().asyncExec(new Runnable() {

                public void run() {
                    processor.execute2();

                }
            });

        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

    }

    private String getFile() {
        return this.multiSchemaManager.getOriginalValue(this.getConnection().getFilePath());
    }

    private String getRowSeperator() {
        return this.multiSchemaManager.getOriginalValue(this.getConnection().getRowSeparatorValue());
    }

    private String getFieldSeperator(boolean useMultiSeparator) {
        if (useMultiSeparator) {
            return multiSeparatorsText.getText();
        } else {
            return this.multiSchemaManager.getOriginalValue(this.getConnection().getFieldSeparatorValue());
        }

    }

    private boolean checkString(String value) {
        if (value != null && !"".equals(value)) {
            return true;
        }
        return false;
    }

    protected boolean checkFieldsValue() {
        if (!checkString(getFile())) {
            previewInformationLabel.setText(Messages.getString("FileStep2.filePathIncomplete")); //$NON-NLS-1$
            return false;
        } else {
            String file = TalendTextUtils.removeQuotes(getFile());
            if (!new File(file).exists()) {
                previewInformationLabel.setText("File not found");
                return false;
            }
        }
        boolean checkFieldSeparator = false;

        if (!checkString(getRowSeperator()) || !checkString(getFieldSeperator(useMultiSaparators.getSelection()))) {
            previewInformationLabel.setText(Messages.getString("FileStep2.settingsIncomplete")); //$NON-NLS-1$
            return false;
        }
        // escape Char Combo
        if (escapeCharCombo.getText() == "") { //$NON-NLS-1$
            previewInformationLabel.setText(Messages.getString("FileStep2.escapeCharAlert")); //$NON-NLS-1$
            return false;
        }
        if (textEnclosureCombo.getText() == "") { //$NON-NLS-1$
            previewInformationLabel.setText(Messages.getString("FileStep2.textEnclosureAlert")); //$NON-NLS-1$
            return false;
        }
        previewInformationLabel.setText(""); //$NON-NLS-1$
        return true;
    }

    public ProcessDescription getProcessDescription() {

        ProcessDescription processDescription = ShadowProcessHelper.getProcessDescription(getConnection());

        processDescription.setHeaderRow(-1);
        processDescription.setFooterRow(0);
        processDescription.setLimitRows(maximumRowsToPreview);
        processDescription.setSplitRecord(getConnection().isSplitRecord());
        processDescription.setCSVOption(getConnection().isCsvOption());
        // add by wzhang to fix 8039.
        processDescription.setFilepath(PathUtils.getPortablePath(getConnection().getFilePath()));
        String curFilePath = processDescription.getFilepath();
        String orignalPath;
        if (curFilePath != null) {
            orignalPath = multiSchemaManager.getOriginalValue(curFilePath);
            processDescription.setFilepath(orignalPath);
        }
        processDescription.setFieldSeparator(multiSchemaManager.getOriginalValue(processDescription.getFieldSeparator()));
        processDescription.setRowSeparator(multiSchemaManager.getOriginalValue(processDescription.getRowSeparator()));

        return processDescription;
    }

    public Label getPreviewInformationLabel() {
        return this.previewInformationLabel;
    }

    public ShadowProcessPreview getMultiSchemasFilePreview() {
        return this.multiSchemasFilePreview;
    }

    public CTabFolder getTabFolder() {
        return this.tabFolder;
    }

    public CTabItem getOutputTabItem() {
        return this.outputTabItem;
    }

    public Composite getOutputComposite() {
        return this.outputComposite;
    }

    /**
     *
     * cLi Comment method "preCheckProcessStart".
     *
     * for processor
     */
    public boolean preCheckProcessStart() {
        previewBtn.setText(Messages.getString("FileStep2.stop"));

        clearPreview();
        if (!checkFieldsValue()) {
            return false;
        }

        previewInformationLabel.setText(Messages.getString("FileStep2.previewProgress")); //$NON-NLS-1$
        return true;
    }

    /**
     *
     * cLi Comment method "updateUIInThreadIfThreadFinally".
     *
     * for processor
     */
    public void updateUIInThreadIfThreadFinally() {
        if (!previewBtn.isDisposed()) {
            previewBtn.setText(ExternalMultiSchemasUIProperties.PREVIEW_STRING);
            previewBtn.setEnabled(true);

        }
    }

    /**
     *
     * cLi Comment method "postProcessCancle".
     *
     * for processor
     */
    public void postProcessCancle() {
        previewBtn.setEnabled(false);
    }

    public void setFetchButtonStatus(final boolean status) {
        fetchBtn.setEnabled(status);
    }

    public void checkDialog() {
        if (this.listener != null) {
            this.listener.checkPerformed(canFinished());
        }
    }

    boolean isReadOnly() {
        return getMultiSchemaManager().isReadOnly();
    }

    public boolean canFinished() {
        final Object input = schemaTreeViewer.getInput();
        if (input != null && input instanceof SchemasKeyData) {
            return true;
        }
        return false;
    }

    /**
     *
     * cLi Comment method "saveProperties".
     *
     *
     */
    public void saveProperties() {
        TableColumn[] tcs = this.multiSchemasFilePreview.getTable().getColumns();
        int currentIndexIntable = 0;
        for (int i = 0; i < this.multiSchemasFilePreview.getTable().getColumnCount(); i++) {
            if (tcs[i].getImage().equals(ImageProvider.getImage(EImage.CHECKED_ICON))) {
                currentIndexIntable = i;
            }
        }

        final Object input = schemaTreeViewer.getInput();
        if (input != null) {
            getMultiSchemaManager().savePropertiesToComponent((SchemasKeyData) input, fetchNewParamters(currentIndexIntable),
                    currentIndexIntable);
        }
    }

    private Map<EParameterName, String> fetchNewParamters(int index) {
        String empty = "\"\"\"";//$NON-NLS-1$
        Map<EParameterName, String> paramsMap = new HashMap<EParameterName, String>();
        paramsMap.put(EParameterName.FILENAME, fileField.getText());
        paramsMap.put(EParameterName.ENCODING_TYPE, encodingCombo.getText());
        paramsMap.put(EParameterName.ENCODING, TalendTextUtils.addQuotes(encodingCombo.getText()));
        paramsMap.put(EParameterName.FIELDSEPARATOR, fieldSeparatorText.getText());
        paramsMap.put(EParameterName.ROWSEPARATOR, rowSeparatorText.getText());
        String textEnclosure = textEnclosureCombo.getText();
        if (EMPTY_VALUE.equals(textEnclosure)) {
            textEnclosure = empty;
        }
        paramsMap.put(EParameterName.TEXT_ENCLOSURE, textEnclosure);
        String escapeChar = escapeCharCombo.getText();
        if (EMPTY_VALUE.equals(escapeChar)) {
            escapeChar = empty;
        }
        paramsMap.put(EParameterName.ESCAPE_CHAR, escapeChar);
        paramsMap.put(EParameterName.CSV_OPTION, String.valueOf(csvRadio.getSelection()));
        paramsMap.put(EParameterName.COLUMNINDEX, TalendTextUtils.addQuotes(String.valueOf(index)));
        paramsMap.put(EParameterName.USE_MULTISEPARATORS, String.valueOf(useMultiSaparators.getSelection()));
        paramsMap.put(EParameterName.MULTI_SEPARATORS, multiSeparatorsText.getText());
        paramsMap.put(EParameterName.MULTI_KEYVALUES, keyValuesText.getText());
        return paramsMap;
    }

    public void prepareClosing(int dialogResponse) {
        multiSchemaManager.getUIManager().setDialogResponse(dialogResponse);
        if (dialogResponse == SWT.OK) {
            ExternalMultiSchemasUIProperties.setShellMaximized(getShell().getMaximized());
            ExternalMultiSchemasUIProperties.setBoundsMapper(getShell().getBounds());
            ExternalMultiSchemasUIProperties.setAllSashformWeights(this.allContentForm.getWeights());
            ExternalMultiSchemasUIProperties.setHeaderSashformWeights(this.headerSashForm.getWeights());
            ExternalMultiSchemasUIProperties.setSchemaDetailsModel(getSchemaDetailModel());
        }

    }

    private boolean getSchemaDetailModel() {
        if (propertyAction != null) {
            return propertyAction.isChecked();
        }
        return ExternalMultiSchemasUIProperties.isSchemaDetailsModel(); // default
    }

    class SchameDetailsPropertyAction extends Action {

        private final TreeViewer schemaDetailsViewer;

        public SchameDetailsPropertyAction(final TreeViewer schemaDetailsViewer) {
            super("Property model", IAction.AS_RADIO_BUTTON);
            this.schemaDetailsViewer = schemaDetailsViewer;
        }

        @Override
        public void run() {
            if (isChecked()) {
                changeSchemaDetailsView();
            }
        }

    }

    class SchameDetailsColumnAction extends Action {

        private final TreeViewer schemaDetailsViewer;

        public SchameDetailsColumnAction(final TreeViewer schemaDetailsViewer) {
            super("Column model", IAction.AS_RADIO_BUTTON);
            this.schemaDetailsViewer = schemaDetailsViewer;
        }

        @Override
        public void run() {
            if (isChecked()) {
                changeSchemaDetailsView();
            }
        }
    }

    private boolean checkKeyValue(String oldValue, String newValue) {
        String errorMsg = null;
        boolean canModify = true;
        if (!Pattern.matches(SCHEMA_NAME_PATTERN, newValue)) {
            errorMsg = "'" + newValue + "' is invalid value.";
        } else {
            final Object input = schemaTreeViewer.getInput();
            if (input instanceof SchemasKeyData) {
                List<String> list = new ArrayList<String>();
                final List<SchemasKeyData> children = ((SchemasKeyData) input).getChildren();
                for (SchemasKeyData schema : children) {
                    final String name = schema.getUniqueRecord();
                    if (!name.equals(oldValue)) {
                        list.add(name.toUpperCase());
                    }
                }
                if (newValue != null && list.contains(newValue.toUpperCase())) {
                    errorMsg = "'" + newValue + "' already exist.";
                }
            }
        }
        if (errorMsg != null) {
            MessageDialog.openError(schemaTreeViewer.getTree().getShell(), "Invalid value", errorMsg);
            canModify = false;
        }
        return canModify;
    }

    private void changeSchemaDetailsView() {
        final Tree tree = schemaDetailsViewer.getTree();
        for (TreeItem item : tree.getItems()) {
            item.dispose();
        }
        schemaDetailsViewer.setInput(null);

        getUIManager().changeSchemasDetailView(schemaDetailsViewer, getSchemaDetailModel());
        getUIManager().refreshSchemasDetailView(schemaTreeViewer, schemaDetailsViewer, getSchemaDetailModel());
    }

    public LabelledText getKeyIndexText() {
        return this.keyIndexText;
    }

    public void setKeyIndexText(LabelledText keyIndexText) {
        this.keyIndexText = keyIndexText;
    }

    public Button getUseMultiSaparators() {
        return this.useMultiSaparators;
    }

    public void setUseMultiSaparators(Button useMultiSaparators) {
        this.useMultiSaparators = useMultiSaparators;
    }

}
