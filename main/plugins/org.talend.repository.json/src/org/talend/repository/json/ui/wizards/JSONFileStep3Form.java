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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.types.JavaDataTypeHelper;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.PerlDataTypeHelper;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditorView;
import org.talend.core.ui.preference.metadata.MetadataTypeLengthConstants;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.core.utils.CsvArray;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.preview.ProcessDescription;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.json.ui.shadow.JSONShadowProcessHelper;
import org.talend.repository.json.util.JSONConnectionContextUtils;
import org.talend.repository.json.util.JSONUtil;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.SchemaTarget;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;

/**
 * @author ocarbone
 *
 */
public class JSONFileStep3Form extends AbstractJSONFileStepForm {

    private static Logger log = Logger.getLogger(JSONFileStep3Form.class);

    private static final int WIDTH_GRIDDATA_PIXEL = 750;

    private UtilsButton cancelButton;

    private UtilsButton guessButton;

    private MetadataEmfTableEditor metadataEditor;

    private MetadataEmfTableEditorView tableEditorView;

    private Label informationLabel;

    private final MetadataTable metadataTable;

    private LabelledText metadataNameText;

    private LabelledText metadataCommentText;

    private boolean readOnly;

    private JSONWizard wizard;

    /**
     * Constructor to use by RCP Wizard.
     *
     * @param Composite
     */
    public JSONFileStep3Form(Composite parent, ConnectionItem connectionItem, MetadataTable metadataTable, JSONWizard wizard,
            String[] existingNames) {
        super(parent, connectionItem, metadataTable, existingNames);
        this.connectionItem = connectionItem;
        this.metadataTable = metadataTable;
        this.wizard = wizard;
        setupForm();
    }

    /**
     *
     * Initialize value, forceFocus first field.
     */
    @Override
    protected void initialize() {
        // init the metadata Table
        String label = MetadataToolHelper.validateValue(metadataTable.getLabel());
        metadataNameText.setText(label);
        metadataCommentText.setText(metadataTable.getComment());
        metadataEditor.setMetadataTable(metadataTable);
        tableEditorView.setMetadataEditor(metadataEditor);
        tableEditorView.getTableViewerCreator().layout();

        // if (getConnection().isReadOnly()) {
        // adaptFormToReadOnly();
        // } else {
        // updateStatus(IStatus.OK, null);
        // }
    }

    /**
     * DOC ocarbone Comment method "adaptFormToReadOnly".
     *
     */
    @Override
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();
        guessButton.setEnabled(!isReadOnly());
        metadataNameText.setReadOnly(isReadOnly());
        metadataCommentText.setReadOnly(isReadOnly());
        tableEditorView.setReadOnly(isReadOnly());
    }

    @Override
    protected void addFields() {

        // Header Fields
        Composite mainComposite = Form.startNewDimensionnedGridLayout(this, 2, WIDTH_GRIDDATA_PIXEL, 60);
        metadataNameText = new LabelledText(mainComposite, "Name");
        metadataCommentText = new LabelledText(mainComposite, "Comment");

        // Group MetaData
        Group groupMetaData = Form.createGroup(this, 1, "Schema", 280);
        Composite compositeMetaData = Form.startNewGridLayout(groupMetaData, 1);

        // Composite Guess
        Composite compositeGuessButton = Form.startNewDimensionnedGridLayout(compositeMetaData, 2, WIDTH_GRIDDATA_PIXEL, 40);
        informationLabel = new Label(compositeGuessButton, SWT.NONE);
        informationLabel.setText("Click Guess button to update the schema below according to your settings"
                + "                                                  ");
        informationLabel.setSize(500, HEIGHT_BUTTON_PIXEL);

        guessButton = new UtilsButton(compositeGuessButton, "Guess", WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);
        guessButton.setToolTipText("Click to update schema preview");

        // Composite MetadataTableEditorView
        Composite compositeTable = Form.startNewDimensionnedGridLayout(compositeMetaData, 1, WIDTH_GRIDDATA_PIXEL, 200);
        compositeTable.setLayout(new FillLayout());
        metadataEditor = new MetadataEmfTableEditor("Description of the Schema");
        tableEditorView = new MetadataEmfTableEditorView(compositeTable, SWT.NONE);

        if (!isInWizard()) {
            // Bottom Button
            Composite compositeBottomButton = Form.startNewGridLayout(this, 2, false, SWT.CENTER, SWT.CENTER);
            // Button Cancel
            cancelButton = new UtilsButton(compositeBottomButton, "Cancel", WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);
        }
        // addUtilsButtonListeners(); changed by hqzhang, need not call here, has been called in setupForm()
    }

    /**
     * Main Fields addControls.
     */
    @Override
    protected void addFieldsListeners() {
        // metadataNameText : Event modifyText
        metadataNameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                MetadataToolHelper.validateSchema(metadataNameText.getText());
                metadataTable.setLabel(metadataNameText.getText());
                checkFieldsValue();
            }
        });
        // metadataNameText : Event KeyListener
        metadataNameText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                MetadataToolHelper.checkSchema(getShell(), e);
            }
        });

        // metadataCommentText : Event modifyText
        metadataCommentText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                metadataTable.setComment(metadataCommentText.getText());
            }
        });

        // add listener to tableMetadata (listen the event of the toolbars)
        tableEditorView.getMetadataEditor().addAfterOperationListListener(new IListenableListListener() {

            @Override
            public void handleEvent(ListenableListEvent event) {
                checkFieldsValue();
            }
        });
    }

    /**
     * getContextJSONPath.
     *
     * @return String
     */
    private String getContextJSONPath(JSONFileConnection connection) {
        String contextJSONPath = ""; //$NON-NLS-1$
        if (ConnectionContextHelper.getContextTypeForContextMode(connection, connection.getContextName()) == null) {
            return null;
        }
        EList eList = ConnectionContextHelper.getContextTypeForContextMode(connection, connection.getContextName())
                .getContextParameter();
        for (int i = 0; i < eList.size(); i++) {
            ContextParameterType parameterType = (ContextParameterType) eList.get(i);
            if (parameterType.getPrompt().contains("JSONFilePath")) { //$NON-NLS-1$
                contextJSONPath = parameterType.getValue();
            }
        }
        return contextJSONPath;
    }

    /**
     * addButtonControls.
     *
     * @param cancelButton
     */
    @Override
    protected void addUtilsButtonListeners() {

        // Event guessButton
        guessButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                // changed by hqzhang for TDI-13613, old code is strange, maybe caused by duplicated
                // addUtilsButtonListeners() in addFields() method

                JSONFileConnection connection2 = getConnection();
                if (connection2.isContextMode()) {
                    connection2.setContextName(null);
                }
                String tempJSONFilePath = getContextJSONPath(connection2);

                if (connection2.getJSONFilePath() == null || connection2.getJSONFilePath().equals("")) {
                    informationLabel.setText("   " + "The file path must be specified"
                            + "                                                                              ");
                    return;
                }
                if (tempJSONFilePath == null ? (!new File(connection2.getJSONFilePath()).exists()) : (!new File(connection2
                        .getJSONFilePath()).exists() && !new File(tempJSONFilePath).exists())) {
                    String msg = "File {0} does not exist.";
                    informationLabel.setText(MessageFormat.format(msg, connection2.getJSONFilePath()));
                    return;
                }

                if (tableEditorView.getMetadataEditor().getBeanCount() > 0) {
                    MessageBox box = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO | SWT.CANCEL);
                    box.setMessage("Schema settings changed and are different from current schema preview. Updating schema will overwrite all current data. if choose NO,don't overwrite current data.Continue anyway ?");
                    int open7 = box.open();
                    if (open7 == SWT.YES) {
                        runShadowProcess(true);
                    } else if (open7 == SWT.NO) {
                        runShadowProcess(false);
                    }
                    return;
                }
                runShadowProcess(true);
            }

        });
        if (cancelButton != null) {
            // Event CancelButton
            cancelButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    getShell().close();
                }
            });
        }

    }

    /**
     * create ProcessDescription and set it.
     *
     * WARNING ::field FieldSeparator, RowSeparator, EscapeChar and TextEnclosure are surround by double quote.
     *
     *
     * @return processDescription
     */
    private ProcessDescription getProcessDescription(boolean defaultContext) {
        JSONFileConnection connection2 = JSONConnectionContextUtils.getJSONOriginalValueConnection(getConnection(),
                this.connectionItem, isContextMode(), defaultContext);
        ProcessDescription processDescription = null;
        if (wizard != null) {
            processDescription = JSONShadowProcessHelper.getProcessDescription(connection2, wizard.getTempJsonPath());
        } else {
            if (EJsonReadbyMode.JSONPATH.getValue().equals(connection2.getReadbyMode())) {
                processDescription = JSONShadowProcessHelper.getProcessDescription(connection2, connection2.getJSONFilePath());
            } else {
                processDescription = JSONShadowProcessHelper.getProcessDescription(connection2,
                        JSONUtil.changeJsonToXml(connection2.getJSONFilePath(), getConnectionEncoding()));
            }
        }
        return processDescription;
    }

    /**
     * run a ShadowProcess to determined the Metadata.
     */
    protected void runShadowProcess(Boolean flag) {

        // getConnection().getXsdFilePath() != null && !getConnection().getXsdFilePath().equals("") &&
        JSONFileConnection connection2 = getConnection();
        String tempJSONFilePath = getContextJSONPath(connection2);
        // if (tempJSONFilePath == null ? JSONUtil.isXSDFile(connection2.getJSONFilePath()) :
        // JSONUtil.isXSDFile(connection2
        // .getJSONFilePath()) || JSONUtil.isXSDFile(tempJSONFilePath)) {
        // // no preview for XSD file
        //
        // refreshMetaDataTable(null, ((JSONXPathLoopDescriptor) connection2.getSchema().get(0)).getSchemaTargets(),
        // flag);
        // checkFieldsValue();
        // return;
        // }

        try {
            informationLabel.setText("   " + "Guess in progress...");

            CsvArray csvArray = null;
            ProcessDescription processDescription = getProcessDescription(false);
            if (EJsonReadbyMode.JSONPATH.getValue().equals(connection2.getReadbyMode())) {
                csvArray = JSONShadowProcessHelper.getCsvArray(processDescription, "FILE_JSON"); //$NON-NLS-1$
            } else {
                /**
                 * JSON XPATH mode uses the temp generated xml file to execute the preview, the generated xml file is
                 * encoded using UTF-8. <br/>
                 * (The generated xml file can't be encoded using other charset, otherwise the converted xml file will
                 * be empty)
                 */
                processDescription.setEncoding(TalendQuoteUtils.addQuotes("UTF-8"));
                csvArray = JSONShadowProcessHelper.getCsvArray(processDescription, "FILE_XML"); //$NON-NLS-1$
            }
            if (csvArray == null) {
                informationLabel.setText("   " + "Guess failure");

            } else {
                refreshMetaDataTable(csvArray, connection2.getSchema().get(0).getSchemaTargets(), flag);
            }

        } catch (CoreException e) {
            if (getParent().getChildren().length == 1) {
                new ErrorDialogWidthDetailArea(getShell(), PID, "Guess failure" + "\n" + "Guess based on preview to step 2",
                        e.getMessage());
            } else {
                new ErrorDialogWidthDetailArea(getShell(), PID, "Guess failure", e.getMessage());
            }
            log.error("Guess failure" + " " + e.getMessage());
        }
        checkFieldsValue();
    }

    /**
     * DOC ocarbone Comment method "refreshMetaData".
     *
     * @param csvArray
     */
    public void refreshMetaDataTable(final CsvArray csvArray, List<SchemaTarget> schemaTarget, Boolean flag) {
        informationLabel.setText("   " + "Guess successful");
        List mcolumns = new ArrayList();
        mcolumns.addAll(tableEditorView.getMetadataEditor().getMetadataColumnList());
        // clear all items
        tableEditorView.getMetadataEditor().removeAll();

        List<MetadataColumn> columns = new ArrayList<MetadataColumn>();

        String file = ((JSONFileConnection) this.connectionItem.getConnection()).getJSONFilePath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(),
                    connectionItem.getConnection().getContextName());
            file = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, file));
        }

        if (csvArray == null || csvArray.getRows().isEmpty()) {
            return;
        } else {

            List<String[]> csvRows = csvArray.getRows();
            String[] fields = csvRows.get(0);
            int numberOfCol = fields.length;

            // define the label to the metadata width the content of the first row
            int firstRowToExtractMetadata = 0;

            // the first rows is used to define the label of any metadata
            String[] label = new String[numberOfCol];
            for (int i = 0; i < numberOfCol; i++) {
                label[i] = DEFAULT_LABEL + i;

                if (firstRowToExtractMetadata == 0) {
                    if (schemaTarget.get(i).getTagName() != null && !schemaTarget.get(i).getTagName().equals("")) { //$NON-NLS-1$
                        label[i] = "" + schemaTarget.get(i).getTagName().trim().replaceAll(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        label[i] = MetadataToolHelper.validateColumnName(label[i], i);
                    }
                }

                // if (firstRowToExtractMetadata == 1) {
                // String value = fields.get(i).getValue();
                // if (!value.equals("")) {
                // label[i] = value;
                // }
                // }
            }

            for (int i = 0; i < numberOfCol; i++) {
                // define the first currentType and assimile it to globalType
                String globalType = null;
                int lengthValue = 0;
                int precisionValue = 0;

                int current = firstRowToExtractMetadata;
                while (globalType == null) {
                    if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                        if (i >= csvRows.get(current).length) {
                            globalType = "id_String"; //$NON-NLS-1$
                        } else {
                            globalType = JavaDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                            // if (current == csvRows.size()) {
                            // globalType = "id_String"; //$NON-NLS-1$
                            // }
                        }
                    } else {
                        if (i >= csvRows.get(current).length) {
                            globalType = "String"; //$NON-NLS-1$
                        } else {
                            globalType = PerlDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                            // if (current == csvRows.size()) {
                            // globalType = "String"; //$NON-NLS-1$
                            // }
                        }
                    }
                }
                // for another lines
                for (int f = firstRowToExtractMetadata; f < csvRows.size(); f++) {
                    fields = csvRows.get(f);
                    if (fields.length > i) {
                        String value = fields[i];
                        if (!value.equals("")) { //$NON-NLS-1$

                            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                                if (!JavaDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                    globalType = JavaDataTypeHelper.getCommonType(globalType,
                                            JavaDataTypeHelper.getTalendTypeOfValue(value));
                                }
                            } else {
                                if (!PerlDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                    globalType = PerlDataTypeHelper.getCommonType(globalType,
                                            PerlDataTypeHelper.getTalendTypeOfValue(value));
                                }
                            }
                            if (lengthValue < value.length()) {
                                lengthValue = value.length();
                            }
                            int positionDecimal = 0;
                            if (value.indexOf(',') > -1) {
                                positionDecimal = value.lastIndexOf(',');
                                precisionValue = lengthValue - positionDecimal;
                            } else if (value.indexOf('.') > -1) {
                                positionDecimal = value.lastIndexOf('.');
                                precisionValue = lengthValue - positionDecimal;
                            }
                        } else {
                            IPreferenceStore preferenceStore = null;
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreUIService.class)) {
                                IDesignerCoreUIService designerCoreUiService = (IDesignerCoreUIService) GlobalServiceRegister
                                        .getDefault().getService(IDesignerCoreUIService.class);
                                preferenceStore = designerCoreUiService.getPreferenceStore();
                            }
                            if (preferenceStore != null
                                    && preferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE) != null
                                    && !preferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE).equals("")) { //$NON-NLS-1$
                                globalType = preferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE);
                                if (preferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH) != null
                                        && !preferenceStore.getString(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH)
                                                .equals("")) { //$NON-NLS-1$
                                    lengthValue = Integer.parseInt(preferenceStore
                                            .getString(MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH));
                                }
                            }

                        }
                    }
                }

                // define the metadataColumn to field i
                MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                // hshen bug7249
                metadataColumn.setPattern("\"dd-MM-yyyy\""); //$NON-NLS-1$
                // Convert javaType to TalendType
                String talendType = null;
                talendType = globalType;
                if (globalType.equals(JavaTypesManager.FLOAT.getId()) || globalType.equals(JavaTypesManager.DOUBLE.getId())) {
                    metadataColumn.setPrecision(precisionValue);
                } else {
                    metadataColumn.setPrecision(0);
                }
                metadataColumn.setTalendType(talendType);
                metadataColumn.setLength(lengthValue);

                // Check the label and add it to the table
                metadataColumn.setLabel(tableEditorView.getMetadataEditor().getNextGeneratedColumnName(label[i]));
                columns.add(i, metadataColumn);
            }
        }

        if (!flag) {
            for (int i = 0; i < columns.size(); i++) {
                for (int j = 0; j < mcolumns.size(); j++) {
                    if (columns.get(i).getLabel().equals(((MetadataColumn) mcolumns.get(j)).getLabel())) {
                        columns.remove(i);
                        columns.add(i, (MetadataColumn) mcolumns.get(j));
                    }
                }
            }
        }
        tableEditorView.getMetadataEditor().addAll(columns);
        checkFieldsValue();
        tableEditorView.getTableViewerCreator().layout();
        tableEditorView.getTableViewerCreator().getTable().deselectAll();
        informationLabel.setText("Click to update schema preview");
    }

    /**
     * Ensures that fields are set. Update checkEnable / use to checkConnection().
     *
     * @return
     */
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

    public void saveMetaData() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (super.isVisible()) {
            // getConnection().getXsdFilePath() != null && !getConnection().getXsdFilePath().equals("") &&
            if ((getConnection().getJSONFilePath() != null && new File(getConnection().getJSONFilePath()).exists())
                    || (wizard != null && wizard.getTempJsonPath() != null) || JSONUtil.tempJSONXsdPath != null) {
                runShadowProcess(true);
            }

            if (isReadOnly() != readOnly) {
                adaptFormToReadOnly();
            }
        }
    }
}
