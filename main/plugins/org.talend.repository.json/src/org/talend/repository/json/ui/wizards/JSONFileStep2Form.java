// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.ui.command.CommandStackForComposite;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCheckboxCombo;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.encoding.CharsetToolkit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.xml.XmlArray;
import org.talend.core.utils.CsvArray;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.datatools.xml.utils.XPathPopulationUtil;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.preview.AsynchronousPreviewHandler;
import org.talend.metadata.managment.ui.preview.IPreviewHandlerListener;
import org.talend.metadata.managment.ui.preview.ProcessDescription;
import org.talend.metadata.managment.ui.preview.StoppablePreviewLoader;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.ShadowProcessHelper;
import org.talend.metadata.managment.ui.wizard.IRefreshable;
import org.talend.repository.json.ui.preview.JSONShadowProcessPreview;
import org.talend.repository.json.ui.shadow.JSONShadowProcessHelper;
import org.talend.repository.json.ui.wizards.extraction.ExtractionFieldsWithJSONXPathEditorView;
import org.talend.repository.json.ui.wizards.extraction.ExtractionLoopWithJSONXPathEditorView;
import org.talend.repository.json.ui.wizards.extraction.JSONExtractorFieldModel;
import org.talend.repository.json.ui.wizards.extraction.JSONExtractorLoopModel;
import org.talend.repository.json.ui.wizards.extraction.JSONToJsonPathLinker;
import org.talend.repository.json.ui.wizards.extraction.JSONToXPathLinker;
import org.talend.repository.json.util.JSONConnectionContextUtils;
import org.talend.repository.json.util.JSONUtil;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.JSONXPathLoopDescriptor;
import org.talend.repository.model.json.JsonFactory;
import org.talend.repository.model.json.SchemaTarget;
import org.talend.repository.ui.wizards.metadata.connection.files.json.AbstractTreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;
import org.talend.repository.ui.wizards.metadata.connection.files.json.JsonTreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.TreePopulator;

/**
 * @author ocarbone
 *
 */
public class JSONFileStep2Form extends AbstractJSONFileStepForm implements IRefreshable {

    private static Logger log = Logger.getLogger(JSONFileStep2Form.class);

    /**
     * Main Fields.
     */

    private transient Tree availableJSONTree;

    private transient TreeViewer availableJSONTreeViewer;

    private ATreeNode treeNode;

    private JSONExtractorFieldModel fieldsModel;

    private ExtractionLoopWithJSONXPathEditorView loopTableEditorView;

    private ExtractionFieldsWithJSONXPathEditorView fieldsTableEditorView;

    private Button previewButton;

    private Label previewInformationLabel;

    private JSONShadowProcessPreview jsonFilePreview;

    private Text fileJSONText;

    protected boolean filePathIsDone;

    private UtilsButton cancelButton;

    private boolean readOnly;

    private SashForm jsonToSchemaSash;

    private JSONToXPathLinker linker;

    private AbstractTreePopulator treePopulator;

    private JSONExtractorLoopModel loopModel;

    private JSONXPathLoopDescriptor jsonXPathLoopDescriptor;

    private IPreviewHandlerListener previewHandlerListener;

    private static Boolean firstTimeWizardOpened = null;

    private String jsonFilePath;

    private String tempJSONXsdPath;

    private Group schemaTargetGroup;

    /**
     * Output tab.
     */
    private CTabFolder tabFolder;

    private CTabItem previewTabItem;

    private CTabItem outputTabItem;

    private CTabItem fileTabItem;

    private Composite outputComposite;

    private JSONWizard wizard;

    private String currentReadbyMode;

    /**
     * Constructor to use by RCP Wizard.
     *
     * @param Composite
     * @param Wizard
     * @param Style
     */
    public JSONFileStep2Form(Composite parent, ConnectionItem connectionItem, JSONWizard wizard) {
        super(parent, connectionItem);
        this.wizard = wizard;
        setupForm(true);
    }

    /**
     *
     * Initialize value, forceFocus first field.
     */
    @Override
    protected void initialize() {

        availableJSONTreeViewer = new TreeViewer(availableJSONTree);
        currentReadbyMode = this.wizard.getReadbyMode();
        initTreePopulator();

        checkFieldsValue();

        if (jsonXPathLoopDescriptor == null) {
            if (getConnection().getSchema() != null && !getConnection().getSchema().isEmpty()) {
                jsonXPathLoopDescriptor = getConnection().getSchema().get(0);
                jsonFilePath = getConnection().getJSONFilePath();
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                            connectionItem.getConnection(), connectionItem.getConnection().getContextName());
                    jsonFilePath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                            jsonFilePath));
                }
            } else {
                jsonXPathLoopDescriptor = JsonFactory.eINSTANCE.createJSONXPathLoopDescriptor();
                getConnection().getSchema().add(jsonXPathLoopDescriptor);
                // jsonXPathLoopDescriptor.setConnection(getConnection());
            }
        }

        loopModel.setJSONXPathLoopDescriptor(jsonXPathLoopDescriptor);
        if (jsonXPathLoopDescriptor.getLimitBoucle() == null) {
            jsonXPathLoopDescriptor.setLimitBoucle(-1);
            XmlArray.setLimitToDefault();
            jsonXPathLoopDescriptor.setLimitBoucle(XmlArray.getRowLimit());

        }
        treePopulator.setEncoding(getConnectionEncoding());
        treePopulator.populateTree(wizard.getTempJsonPath(), treeNode);
        fieldsModel.setJSONXPathLoopDescriptor(jsonXPathLoopDescriptor.getSchemaTargets());
        fieldsTableEditorView.getTableViewerCreator().layout();

        if (isContextMode()) {
            adaptFormToEditable();
        }

    }

    private void initTreePopulator() {
        if (EJsonReadbyMode.JSONPATH.getValue().equals(this.wizard.getReadbyMode())) {
            this.treePopulator = new JsonTreePopulator(availableJSONTreeViewer);
        } else {
            this.treePopulator = new TreePopulator(availableJSONTreeViewer);
        }
        this.treePopulator.setFilePath(this.wizard.getTempJsonPath());
        this.treePopulator.configureDefaultTreeViewer();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        // adapt all the fields to enabled
        adaptFormToReadOnly();

        if (exportContextBtn != null) {
            exportContextBtn.setEnabled(false);
        }
        if (!readOnly) {
            // adapt the field to the context
            checkFieldsValue();
        }
    }

    /**
     * DOC ocarbone Comment method "adaptFormToReadOnly".
     */
    @Override
    protected void adaptFormToReadOnly() {
        // readOnly = isReadOnly();
    }

    @Override
    protected void adaptFormToEditable() {
        super.adaptFormToEditable();
        loopTableEditorView.setReadOnly(isContextMode());
        this.fieldsTableEditorView.setReadOnly(isContextMode());
    }

    @Override
    protected void addFields() {

        // compositeFile Main Fields
        // Composite mainComposite = Form.startNewGridLayout(this, 1);
        SashForm mainComposite = new SashForm(this, SWT.VERTICAL | SWT.SMOOTH);
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (firstTimeWizardOpened == null) {
            firstTimeWizardOpened = Boolean.TRUE;
        } else if (firstTimeWizardOpened.equals(Boolean.TRUE)) {
            firstTimeWizardOpened = Boolean.FALSE;
        }

        // Splitter
        this.jsonToSchemaSash = new SashForm(mainComposite, SWT.HORIZONTAL | SWT.SMOOTH);
        jsonToSchemaSash.setLayoutData(new GridData(GridData.FILL_BOTH));
        jsonToSchemaSash.setBackgroundMode(SWT.INHERIT_FORCE);
        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            jsonToSchemaSash.setSashWidth((mainComposite.getShell().getBounds().width) / 6);
        }

        addGroupJSONFileSettings(jsonToSchemaSash, 400, 110);
        addGroupSchemaTarget(jsonToSchemaSash, 300, 110);
        jsonToSchemaSash.setWeights(new int[] { 40, 60 });

        SashForm sash2 = new SashForm(mainComposite, SWT.HORIZONTAL | SWT.SMOOTH);
        sash2.setLayoutData(new GridData(GridData.FILL_BOTH));

        addGroupFileViewer(sash2, 400, 210);
        // addGroupJSONViewer(sash2, 300, 110);

        if (!isInWizard()) {
            // Bottom Button
            Composite compositeBottomButton = Form.startNewGridLayout(this, 2, false, SWT.CENTER, SWT.CENTER);
            // Button Cancel
            cancelButton = new UtilsButton(compositeBottomButton, "Cancel", WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);
        }
        addUtilsButtonListeners();
        mainComposite.setWeights(new int[] { 60, 40 });
    }

    /**
     * add Field to Group JSON File Settings.
     *
     * @param mainComposite
     * @param form
     * @param width
     * @param height
     */
    private void addGroupJSONFileSettings(final Composite mainComposite, final int width, final int height) {

        // Group Schema Viewer
        Group group = Form.createGroup(mainComposite, 1, "Source Schema", height);
        group.setBackground(null);

        availableJSONTree = new Tree(group, SWT.MULTI | SWT.BORDER);

        // availableJSONTree.setVisible(false);
        GridData gridData2 = new GridData(GridData.FILL_BOTH);
        availableJSONTree.setLayoutData(gridData2);
    }

    private void addGroupSchemaTarget(final Composite mainComposite, final int width, final int height) {
        // Group Schema Viewer
        schemaTargetGroup = Form.createGroup(mainComposite, 1, "Target Schema", height);

        // ///////////////////////////////////////////
        // to correct graphic bug under Linux-GTK when the wizard is opened the first time
        if (WindowSystem.isGTK() && firstTimeWizardOpened.equals(Boolean.TRUE)) {
            schemaTargetGroup.addListener(SWT.Paint, new Listener() {

                @Override
                public void handleEvent(Event event) {
                    Point offsetPoint = event.display.map(linker.getBgDrawableComposite(), schemaTargetGroup, new Point(0, 0));
                    linker.setOffset(offsetPoint);
                    linker.drawBackground(event.gc);
                }

            });
        }
        // ///////////////////////////////////////////

        schemaTargetGroup.setBackgroundMode(SWT.INHERIT_FORCE);

        CommandStackForComposite commandStack = new CommandStackForComposite(schemaTargetGroup);

        loopModel = new JSONExtractorLoopModel("Path loop expression");

        loopTableEditorView = new ExtractionLoopWithJSONXPathEditorView(loopModel, schemaTargetGroup);
        loopTableEditorView.getExtendedTableViewer().setCommandStack(commandStack);
        GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
        data2.heightHint = 90;

        final Composite loopTableEditorComposite = loopTableEditorView.getMainComposite();
        loopTableEditorComposite.setLayoutData(data2);
        loopTableEditorComposite.setBackground(null);
        // ///////////////////////////////////////////
        // to correct graphic bug under Linux-GTK when the wizard is opened the first time
        if (WindowSystem.isGTK() && firstTimeWizardOpened.equals(Boolean.TRUE)) {
            loopTableEditorComposite.addListener(SWT.Paint, new Listener() {

                @Override
                public void handleEvent(Event event) {
                    Point offsetPoint = event.display.map(linker.getBgDrawableComposite(), loopTableEditorComposite, new Point(0,
                            0));
                    linker.setOffset(offsetPoint);
                    linker.drawBackground(event.gc);
                }

            });
        }
        // ///////////////////////////////////////////

        // Messages.getString("FileStep3.metadataDescription")
        fieldsModel = new JSONExtractorFieldModel("Fields to extract");
        fieldsTableEditorView = new ExtractionFieldsWithJSONXPathEditorView(fieldsModel, schemaTargetGroup);
        fieldsTableEditorView.getExtendedTableViewer().setCommandStack(commandStack);
        final Composite fieldTableEditorComposite = fieldsTableEditorView.getMainComposite();
        data2 = new GridData(GridData.FILL_BOTH);
        data2.heightHint = 180;
        fieldTableEditorComposite.setLayoutData(data2);
        fieldTableEditorComposite.setBackground(null);
        // ///////////////////////////////////////////
        // to correct graphic bug under Linux-GTK when the wizard is opened the first time
        if (WindowSystem.isGTK() && firstTimeWizardOpened.equals(Boolean.TRUE)) {
            fieldTableEditorComposite.addListener(SWT.Paint, new Listener() {

                @Override
                public void handleEvent(Event event) {
                    Point offsetPoint = event.display.map(linker.getBgDrawableComposite(), fieldTableEditorComposite, new Point(
                            0, 0));
                    linker.setOffset(offsetPoint);
                    linker.drawBackground(event.gc);
                }

            });
        }
        // ///////////////////////////////////////////

    }

    /**
     * add Field to Group File Viewer.
     *
     * @param parent
     * @param form
     * @param width
     * @param height
     */
    private void addGroupFileViewer(final Composite parent, final int width, int height) {
        // composite JSON File Preview
        // Group previewGroup = Form.createGroup(parent, 1, Messages.getString("FileStep2.groupPreview"), height);
        // //$NON-NLS-1$
        // Composite compositeJSONFilePreviewButton = Form.startNewDimensionnedGridLayout(previewGroup, 4, width,
        // HEIGHT_BUTTON_PIXEL);
        // height = height - HEIGHT_BUTTON_PIXEL - 15;

        tabFolder = new CTabFolder(parent, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        previewTabItem = new CTabItem(tabFolder, SWT.BORDER);
        previewTabItem.setText("Preview"); //$NON-NLS-1$
        outputTabItem = new CTabItem(tabFolder, SWT.BORDER);
        outputTabItem.setText("Output");
        fileTabItem = new CTabItem(tabFolder, SWT.BORDER);
        fileTabItem.setText("File Viewer");

        Composite previewComposite = Form.startNewGridLayout(tabFolder, 1);
        outputComposite = Form.startNewGridLayout(tabFolder, 1);
        Composite compositeFileViewer = Form.startNewGridLayout(tabFolder, 1);

        // previewGroup.setLayout(new GridLayout());

        Composite preivewButtonPart = new Composite(previewComposite, SWT.NONE);
        preivewButtonPart.setLayout(new GridLayout(3, false));
        preivewButtonPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Preview Button
        previewButton = new Button(preivewButtonPart, SWT.NONE);
        previewButton.setText("Refresh Preview"); //$NON-NLS-1$
        previewButton.setSize(WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);

        XmlArray.setLimitToDefault();
        previewInformationLabel = new Label(previewComposite, SWT.NONE);
        previewInformationLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

        // JSON File Preview
        jsonFilePreview = new JSONShadowProcessPreview(previewComposite, null, width, height - 10);
        jsonFilePreview.newTablePreview();

        // File View
        fileJSONText = new Text(compositeFileViewer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = width;
        gridData.minimumHeight = HEIGHT_BUTTON_PIXEL;
        fileJSONText.setLayoutData(gridData);
        fileJSONText.setToolTipText("When Filepath is specified, you can read here the" + " "
                + TreePopulator.getMaximumRowsToPreview() + " " + "first rows of the file.");
        fileJSONText.setEditable(false);
        fileJSONText.setText("Filepath must be specified to show the Data file");

        previewTabItem.setControl(previewComposite);
        outputTabItem.setControl(outputComposite);
        fileTabItem.setControl(compositeFileViewer);
        tabFolder.setSelection(previewTabItem);
        tabFolder.pack();
    }

    /**
     * add Field to Group File Viewer.
     *
     * @param parent
     * @param form
     * @param width
     * @param height
     */
    private void addGroupJSONViewer(final Composite parent, final int width, int height) {
        // Group File Viewer
        Group group = Form.createGroup(parent, 1, "File Viewer", height); //$NON-NLS-1$
        Composite compositeFileViewer = Form.startNewDimensionnedGridLayout(group, 1, width, HEIGHT_BUTTON_PIXEL);

        fileJSONText = new Text(compositeFileViewer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = width;
        gridData.minimumHeight = HEIGHT_BUTTON_PIXEL;
        fileJSONText.setLayoutData(gridData);
        fileJSONText.setToolTipText("When Filepath is specified, you can read here the" + " "
                + TreePopulator.getMaximumRowsToPreview() + " " + "first rows of the file.");
        fileJSONText.setEditable(false);
        fileJSONText.setText("Filepath must be specified to show the Data file");
    }

    /**
     * create ProcessDescription and set it.
     *
     * WARNING ::field FieldSeparator, RowSeparator, EscapeChar and TextEnclosure are surround by double quote.
     *
     * @param getConnection()
     *
     * @return processDescription
     */
    private ProcessDescription getProcessDescription(boolean defaultContext) {
        JSONFileConnection connection2 = JSONConnectionContextUtils.getJSONOriginalValueConnection(getConnection(),
                this.connectionItem, isContextMode(), defaultContext);
        ProcessDescription processDescription = JSONShadowProcessHelper.getProcessDescription(connection2,
                wizard.getTempJsonPath());
        return processDescription;
    }

    /**
     * clear the table preview.
     */
    void clearPreview() {
        jsonFilePreview.clearTablePreview();
    }

    /**
     * refreshPreview use ShadowProcess to refresh the preview.
     */
    void refreshPreview() {
        clearPreview();
        ContextType contextType = null;
        if (getConnection().isContextMode()) {
            contextType = ConnectionContextHelper.getContextTypeForContextMode(getConnection());
        }
        String filePath = getConnection().getJSONFilePath();
        if (contextType != null) {
            filePath = ContextParameterUtils.getOriginalValue(contextType, filePath);
        }
        // if no file, the process don't be executed
        if (filePath == null || !(new File(filePath).exists())
                && JSONUtil.tempJSONXsdPath == null) {
            previewInformationLabel.setText("   " + "The file path must be specified");
            previewInformationLabel.getParent().layout();
            return;
        }
        // if incomplete settings, , the process don't be executed
        if (!checkFieldsValue()) {
            previewInformationLabel.setText("   " + "The settings must be completed to show the preview");
            previewInformationLabel.getParent().layout();
            return;
        }

        // set row limit
        if (jsonXPathLoopDescriptor.getLimitBoucle() > 0 && jsonXPathLoopDescriptor.getLimitBoucle() < XmlArray.getRowLimit()) {
            XmlArray.setRowLimit(jsonXPathLoopDescriptor.getLimitBoucle());
        }

        previewInformationLabel.setText("   " + "Preview in progress...");
        previewInformationLabel.getParent().layout();

        String shadowProcessType = null;
        if (EJsonReadbyMode.JSONPATH.getValue().equals(getConnection().getReadbyMode())) {
            shadowProcessType = "FILE_JSON"; //$NON-NLS-1$
        }
        final String finalShadowProcessType = shadowProcessType;

        AsynchronousPreviewHandler<CsvArray> previewHandler = null;
        try {
            previewHandler = ShadowProcessHelper.createPreviewHandler(finalShadowProcessType);
        } catch (CoreException e) {
            previewInError(e);
            return;
        }

        StoppablePreviewLoader previewLoader = new StoppablePreviewLoader<CsvArray>(previewHandler, previewInformationLabel) {

            /*
             * (non-Javadoc)
             *
             * @see
             * org.talend.repository.ui.wizards.metadata.connection.files.JSON.StoppablePreviewLoader#previewEnded(java
             * .lang.Object)
             */
            @Override
            protected void previewEnded(CsvArray result) {
                jsonFilePreview.refreshTablePreview(result, false, (getConnection().getSchema().get(0)).getSchemaTargets());
                if (jsonFilePreview.isCsvRowsEmpty()) {
                    clearPreview();
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            handleErrorOutput(outputComposite, tabFolder, outputTabItem);

                        }
                    });
                    log.error("No result for this settings");
                    previewButton.setEnabled(true);
                }
            }

            @Override
            public void previewInError(CoreException e) {
                JSONFileStep2Form.this.previewInError(e);
            }

            @Override
            protected String getShadowProcessType() {
                if (finalShadowProcessType != null && !finalShadowProcessType.isEmpty()) {
                    return finalShadowProcessType;
                } else {
                    return super.getShadowProcessType();
                }
            }
        };

        ProcessDescription processDescription = getProcessDescription(false);
        EJsonReadbyMode mode = null;
        String readbyMode = getConnection().getReadbyMode();
        if (StringUtils.isNotBlank(readbyMode)) {
            mode = EJsonReadbyMode.getEJsonReadbyModeByValue(readbyMode);
        }
        if (EJsonReadbyMode.XPATH.equals(mode)) {
            /**
             * JSON XPATH mode uses the temp generated xml file to execute the preview, the generated xml file is
             * encoded using UTF-8. <br/>
             * (The generated xml file can't be encoded using other charset, otherwise the converted xml file will be
             * empty)
             */
            processDescription.setEncoding(TalendQuoteUtils.addQuotes("UTF-8"));
        }
        previewLoader.load(processDescription);

    }

    /**
     * DOC amaumont Comment method "previewInFileError".
     *
     * @param e
     */
    protected void previewInError(CoreException e) {

        String errorMessage = null;
        if (e != null) {
            errorMessage = e.getMessage();
        }

        previewInformationLabel
                .setText("   "
                        + "Preview error. Some settings must be changed.\\nNote\\: Preview errors are generally due to a wrong encoding setting. ");
        new ErrorDialogWidthDetailArea(
                previewInformationLabel.getShell(),
                PID,
                "Preview error. Some settings must be changed.\\nNote\\: Preview errors are generally due to a wrong encoding setting. ", //$NON-NLS-1$
                errorMessage);
        log.error("Preview error. Some settings must be changed.\\nNote\\: Preview errors are generally due to a wrong encoding setting. "
                + " " + errorMessage);

    }

    /**
     * Main Fields addControls.
     */
    @Override
    protected void addFieldsListeners() {
        // add listener to tableMetadata (listen the event of the toolbars)
        fieldsTableEditorView.getExtendedTableModel().addAfterOperationListListener(new IListenableListListener() {

            @Override
            public void handleEvent(ListenableListEvent event) {
                checkFieldsValue();
            }
        });

        fieldsTableEditorView.getExtendedTableModel().addModifiedBeanListener(new IModifiedBeanListener<SchemaTarget>() {

            @Override
            public void handleEvent(ModifiedBeanEvent<SchemaTarget> event) {
                checkFieldsValue();
                // updateStatus(IStatus.OK, null);
                // String msg = fieldsTableEditorView.checkColumnNames();
                // if (!StringUtils.isEmpty(msg)) {
                // updateStatus(IStatus.ERROR, msg);
                // }
            }
        });
    }

    /**
     * get the standby XPath expression.
     *
     * @return
     */
    protected List getSelectedXPath(TreeItem selected) {
        // TreeItem selected = this.selectedItem;
        String rootPath = ""; //$NON-NLS-1$
        if (selected.getData() instanceof ATreeNode) {
            ATreeNode node = (ATreeNode) selected.getData();
            rootPath = "/" + selected.getText(); //$NON-NLS-1$
        }

        while (selected.getParentItem() != null) {
            selected = selected.getParentItem();
            if (selected.getData() instanceof ATreeNode) {
                ATreeNode node = (ATreeNode) selected.getData();
                if (node.getType() == ATreeNode.ELEMENT_TYPE) {
                    rootPath = "/" + selected.getText() + rootPath; //$NON-NLS-1$
                }
            }
        }
        return XPathPopulationUtil.populateRootPath(rootPath);

    }

    /**
     * Ensures that fields are set. Update checkEnable / use to checkConnection().
     *
     * @return
     */
    @Override
    protected boolean checkFieldsValue() {
        previewInformationLabel.setText("   " + "The settings must be completed to show the preview");
        previewInformationLabel.getParent().layout();
        updateStatus(IStatus.OK, null);
        previewButton.setEnabled(false);

        String msg = fieldsTableEditorView.checkColumnNames();
        if (!StringUtils.isEmpty(msg)) {
            updateStatus(IStatus.ERROR, msg);
            return false;
        } else {
            previewButton.setEnabled(true);
        }

        // Labelled Checkbox Combo (Row to Skip and Limit)
        ArrayList<LabelledCheckboxCombo> labelledCheckboxCombo2Control = new ArrayList<LabelledCheckboxCombo>();

        Iterator<LabelledCheckboxCombo> iCheckboxCombo;
        LabelledCheckboxCombo labelledCheckboxCombo;

        for (iCheckboxCombo = labelledCheckboxCombo2Control.iterator(); iCheckboxCombo.hasNext();) {
            labelledCheckboxCombo = iCheckboxCombo.next();
            // if the checkbox is checked, check Numeric value
            if (labelledCheckboxCombo.getCheckbox().getSelection()) {
                if ("".equals(labelledCheckboxCombo.getText())) {
                    updateStatus(IStatus.ERROR, labelledCheckboxCombo.getLabelText() + "must be specified");
                    return false;
                }
            }
        }

        previewInformationLabel.setText(""); //$NON-NLS-1$

        String pathStr = getConnection().getJSONFilePath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(),
                    connectionItem.getConnection().getContextName());
            pathStr = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, pathStr));
        }
        previewButton.setEnabled(true);
        updateStatus(IStatus.OK, null);
        return true;
    }

    /**
     * addButtonControls.
     *
     * @param cancelButton
     */
    @Override
    protected void addUtilsButtonListeners() {

        // Event PreviewButton
        previewButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (!previewButton.getText().equals("Wait")) { //$NON-NLS-1$
                    previewButton.setText("Wait");
                    if (getConnection().getJSONFilePath() != null && getConnection().getJSONFilePath().length() != 0
                            && getConnection().getSchema() != null && !getConnection().getSchema().isEmpty()
                            && (getConnection().getSchema().get(0)).getAbsoluteXPathQuery() != null
                            && (getConnection().getSchema().get(0)).getAbsoluteXPathQuery().length() != 0
                            && (getConnection().getSchema().get(0)).getSchemaTargets() != null
                            && (getConnection().getSchema().get(0)).getSchemaTargets().size() != 0) {
                        refreshPreview();
                        outputTabItem.setControl(null);
                    } else {
                        previewButton.setText("Refresh Preview"); //$NON-NLS-1$
                        if (!previewButton.getEnabled()) {
                            // new ErrorDialogWidthDetailArea(getShell(), PID, Messages.getString("FileStep2.noresult"),
                            // Messages //$NON-NLS-1$
                            // .getString("FileStep2.noresultDetailMessage")); //$NON-NLS-1$
                            clearPreview();
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    handleErrorOutput(outputComposite, tabFolder, outputTabItem);

                                }
                            });
                            log.error("No result for this settings");
                            previewButton.setEnabled(true);
                        } else {
                            previewButton.setEnabled(false);
                        }
                    }
                } else {
                    previewButton.setText("Refresh Preview");
                }
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
     * ftang Comment method "handleErrorOutput".
     */
    private void handleErrorOutput(Composite outputComposite, CTabFolder tabFolder, CTabItem outputTabItem) {
        outputComposite = Form.startNewGridLayout(tabFolder, 1);
        outputTabItem.setControl(outputComposite);
        Font font = new Font(Display.getDefault(), "courier", 8, SWT.NONE); //$NON-NLS-1$

        StyledText text = new StyledText(outputComposite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        text.setLayoutData(gridData);
        outputComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

        String errorInfo = "No result for this settings" + "\n";
        errorInfo = errorInfo + "Please check right XPathExpression or XML source document." + "\n";

        text.setText(errorInfo);
        text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        text.setFont(font);

        tabFolder.setSelection(outputTabItem);
    }

    /**
     * checkFileFieldsValue active fileViewer if file exist.
     *
     * @throws IOException
     */
    private void checkFilePathAndManageIt() {
        updateStatus(IStatus.OK, null);
        filePathIsDone = false;
        if (getConnection().getJSONFilePath() == "") { //$NON-NLS-1$
            fileJSONText.setText("When Filepath is specified, you can read here the" + " "
                    + TreePopulator.getMaximumRowsToPreview() + " " + "first rows of the file.");
        } else {
            fileJSONText.setText("Check if the file exist ...");

            StringBuilder previewRows = new StringBuilder();
            BufferedReader in = null;

            String pathStr = ""; //$NON-NLS-1$

            try {
                if (tempJSONXsdPath != null) {
                    pathStr = tempJSONXsdPath;
                } else {
                    pathStr = getConnection().getJSONFilePath();
                }
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                            connectionItem.getConnection(), connectionItem.getConnection().getContextName());
                    pathStr = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, pathStr));
                }

                File file = new File(pathStr);
                if (!file.exists()) {
                    file.createNewFile();
                    FileOutputStream outStream;
                    try {
                        outStream = new FileOutputStream(file);
                        outStream.write(getConnection().getFileContent());
                        outStream.close();
                    } catch (FileNotFoundException e1) {
                        ExceptionHandler.process(e1);
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }

                Charset guessedCharset = null;
                try {
                    guessedCharset = Charset.forName(getConnectionEncoding());
                } catch (Exception e) {
                    if (CommonsPlugin.isDebugMode()) {
                        ExceptionHandler.process(e, Priority.WARN);
                    }
                }
                if (guessedCharset == null) {
                    guessedCharset = CharsetToolkit.guessEncoding(file, 4096);
                }

                String str;
                in = new BufferedReader(new InputStreamReader(new FileInputStream(pathStr), guessedCharset));

                while ((str = in.readLine()) != null) {
                    previewRows.append(str + "\n"); //$NON-NLS-1$
                }

                // show lines
                fileJSONText.setText(new String(previewRows));
                filePathIsDone = true;

            } catch (Exception e) {
                String msgError = "File" + " \"" + fileJSONText.getText().replace("\\\\", "\\") + "\"\n";
                if (e instanceof FileNotFoundException) {
                    msgError = msgError + "is not found";
                } else if (e instanceof EOFException) {
                    msgError = msgError + "have an incorrect character EOF";
                } else if (e instanceof IOException) {
                    msgError = msgError + "is locked by another soft";
                } else {
                    msgError = msgError + "doesn't exist";
                }
                fileJSONText.setText(msgError);
                if (!isReadOnly()) {
                    updateStatus(IStatus.ERROR, msgError);
                }
                log.error(msgError + " " + e.getMessage()); //$NON-NLS-1$
            } finally {
                String msgError = "File" + " \"" + fileJSONText.getText().replace("\\\\", "\\") + "\"\n";
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    msgError = msgError + "is locked by another soft";
                }
            }
            checkFieldsValue();
        }
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
            if (this.linker != null) {
                this.linker.removeAllLinks();
            }

            String pathStr = getConnection().getJSONFilePath();
            boolean isContextMode = isContextMode();
            if (isContextMode) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(),
                        connectionItem.getConnection().getContextName());
                pathStr = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, pathStr));
            }
            exportContextBtn.setEnabled(!isContextMode);
            revertContextBtn.setEnabled(isContextMode);

            // diffrent from xml , we do not save the content in item , find it in wizard temp
            if (EJsonReadbyMode.XPATH.getValue().equals(this.wizard.getReadbyMode())
                    && (pathStr == null || !new File(pathStr).exists())) {
                // initFileContent();
                tempJSONXsdPath = JSONUtil.tempJSONXsdPath;
                pathStr = tempJSONXsdPath;
            }
            // fix bug: when the JSON file is changed, the linker doesn't work.
            resetStatusIfNecessary();
            String tempJson = this.wizard.getTempJsonPath();
            this.treePopulator.setEncoding(getConnectionEncoding());
            this.treePopulator.populateTree(tempJson, treeNode);

            ScrollBar verticalBar = availableJSONTree.getVerticalBar();
            if (verticalBar != null) {
                verticalBar.setSelection(0);
            }

            if (this.linker == null) {
                this.linker = prepareJsonLinker();

            } else {
                this.linker.init(treePopulator);
                this.linker.createLinks();
            }
            checkFilePathAndManageIt();

            if (isContextMode()) {
                adaptFormToEditable();
            }

        }
    }

    private JSONToXPathLinker prepareJsonLinker() {
        if (this.linker != null) {
            this.linker.dispose();
        }
        if (EJsonReadbyMode.JSONPATH.getValue().equals(this.wizard.getReadbyMode())) {
            this.linker = new JSONToJsonPathLinker(this.jsonToSchemaSash);
        } else {
            this.linker = new JSONToXPathLinker(this.jsonToSchemaSash);
        }
        this.linker.init(availableJSONTree, loopTableEditorView, fieldsTableEditorView, treePopulator);
        loopTableEditorView.setLinker(this.linker);
        fieldsTableEditorView.setLinker(this.linker);
        return this.linker;
    }

    /**
     * see bug 0004206: bug on the loop limit case in JSONfile source metadata defining
     */
    private void resetStatusIfNecessary() {
        String curJSONPath = getConnection().getJSONFilePath();
        String oraginalPath = ""; //$NON-NLS-1$
        if (jsonFilePath != null && curJSONPath != null) {
            // change JSON file
            if (isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(),
                        connectionItem.getConnection().getContextName());
                oraginalPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, curJSONPath));
            }
            if ((!jsonFilePath.equals(curJSONPath) && !jsonFilePath.equals(oraginalPath))
                    || !this.wizard.getReadbyMode().equals(currentReadbyMode)) {
                currentReadbyMode = this.wizard.getReadbyMode();
                // clear command stack
                CommandStackForComposite commandStack = new CommandStackForComposite(schemaTargetGroup);
                loopTableEditorView.getExtendedTableViewer().setCommandStack(commandStack);
                fieldsTableEditorView.getExtendedTableViewer().setCommandStack(commandStack);

                getConnection().getSchema().remove(jsonXPathLoopDescriptor);
                jsonXPathLoopDescriptor = JsonFactory.eINSTANCE.createJSONXPathLoopDescriptor();
                getConnection().getSchema().add(jsonXPathLoopDescriptor);

                loopModel.setJSONXPathLoopDescriptor(jsonXPathLoopDescriptor);
                XmlArray.setLimitToDefault();
                jsonXPathLoopDescriptor.setLimitBoucle(XmlArray.getRowLimit());

                fieldsModel.setJSONXPathLoopDescriptor(jsonXPathLoopDescriptor.getSchemaTargets());
                fieldsTableEditorView.getTableViewerCreator().layout();
                initTreePopulator();
                prepareJsonLinker();
                // reset linker
                linker.init(treePopulator);
                loopTableEditorView.setLinker(linker);
                fieldsTableEditorView.setLinker(linker);
                jsonFilePreview.removePreviewContent();
            }
        }
        if (isContextMode()) {
            jsonFilePath = oraginalPath;
        } else {
            jsonFilePath = curJSONPath;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.IRefreshable#refresh()
     */
    @Override
    public void refresh() {
        refreshPreview();
    }

    private void initFileContent() {
        // byte[] bytes = getConnection().getFileContent();
        // Project project = ProjectManager.getInstance().getCurrentProject();
        // IProject fsProject = null;
        // try {
        // fsProject = ResourceModelUtils.getProject(project);
        // } catch (PersistenceException e2) {
        // ExceptionHandler.process(e2);
        // }
        // if (fsProject == null) {
        // return;
        // }
        // String temPath = fsProject.getLocationURI().getPath() + File.separator + "temp" + File.separator +
        // "jsonwizard"
        // + File.separator;
        // String fileName = "";

        // String pathStr = getConnection().getJSONFilePath();
        // if (isContextMode()) {
        // ContextType contextType =
        // ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(), true);
        // pathStr = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, pathStr));
        // }
        // if (pathStr != null) {
        // fileName = JSONUtil.TMP_JSON_FILE;
        // }
        // tempJSONXsdPath = temPath + JSONUtil.TMP_JSON_FILE;
        // if (!temfile.exists()) {
        // try {
        // temfile.createNewFile();
        // } catch (IOException e) {
        // ExceptionHandler.process(e);
        // }
        // }

        // FileOutputStream outStream;
        // try {
        // outStream = new FileOutputStream(temfile);
        // outStream.write(bytes);
        // outStream.close();
        // } catch (FileNotFoundException e1) {
        // ExceptionHandler.process(e1);
        // } catch (IOException e) {
        // ExceptionHandler.process(e);
        // }
        // tempJSONXsdPath = temfile.getPath();
        // if (isContextMode()) {
        // ContextType contextType =
        // ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
        // tempJSONXsdPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper
        // .getOriginalValue(contextType, tempJSONXsdPath));
        // }
        // this.treePopulator.populateTree(tempJSONXsdPath, treeNode);
    }

}
