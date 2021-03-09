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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.commons.utils.encoding.CharsetToolkit;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.EMetadataEncoding;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.json.i18n.Messages;
import org.talend.repository.json.util.JSONUtil;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.TreePopulator;

import orgomg.cwm.resource.record.RecordFactory;
import orgomg.cwm.resource.record.RecordFile;

/**
 * wzhang class global comment. Detailled comment
 */
public class JSONFileOutputStep1Form extends AbstractJSONFileStepForm {

    private static Logger log = Logger.getLogger(JSONFileOutputStep1Form.class);

    private Button noFileButton;

    private Button useFileButton;

    private LabelledFileField jsonFilePath;

    private LabelledCombo encodingCombo;

    private Label labelLimitation;

    private String encoding;

    private Text commonNodesLimitation;

    private LabelledFileField outputFilePath;

    private transient Tree availableJSONTree;

    private Text fileContentText;

    private TreePopulator treePopulator;

    private ATreeNode treeNode;

    private final boolean creation;

    private boolean valid = true;

    private static final int WIDTH_GRIDDATA_PIXEL = 300;

    private String tempJSONPath;

    private boolean isModifing = true;

    private String tempPath;

    public JSONFileOutputStep1Form(boolean creation, Composite parent, ConnectionItem connectionItem, String[] existingNames) {
        super(parent, connectionItem, existingNames);
        this.creation = creation;
        setupForm(true);
    }

    @Override
    protected void initialize() {
        getConnection().setInputModel(false);
        this.treePopulator = new TreePopulator(availableJSONTree);
        if (getConnection().getJSONFilePath() != null) {
            jsonFilePath.setText(getConnection().getJSONFilePath().replace("\\\\", "\\"));
            checkFieldsValue();
            String jsonXmlPath = jsonFilePath.getText();
            if (isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
                jsonXmlPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                        jsonFilePath.getText()));
            }
            if (!new File(jsonXmlPath).exists() && getConnection().getFileContent() != null
                    && getConnection().getFileContent().length > 0) {
                initFileContent();
                jsonXmlPath = tempJSONPath;
            }
            if (JSONFileOutputStep1Form.this.tempPath == null) {
                if (jsonXmlPath != null && !jsonXmlPath.equals("")) {
                    JSONFileOutputStep1Form.this.tempPath = JSONUtil.changeJsonToXml(jsonXmlPath, getConnectionEncoding());
                } else {
                    JSONFileOutputStep1Form.this.tempPath = "";
                }
            }
            File file = new File(JSONFileOutputStep1Form.this.tempPath);
            if (file.exists()) {
                valid = this.treePopulator.populateTree(JSONFileOutputStep1Form.this.tempPath, treeNode);
            }
        }

        if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) {
            encodingCombo.setText(getConnection().getEncoding());
        } else {
            encodingCombo.select(0);
        }
        encodingCombo.clearSelection();
        if (getConnection().getOutputFilePath() != null) {
            outputFilePath.setText(getConnection().getOutputFilePath());
        }
        adaptFormToEditable();
    }

    @Override
    protected void adaptFormToEditable() {
        String JSONFilePath = getConnection().getJSONFilePath();
        boolean isFromFile = (JSONFilePath != null && !JSONFilePath.equals("")); //$NON-NLS-1$
        if(isContextMode()) {
            if (isFromFile) {
                noFileButton.setEnabled(false);
                useFileButton.setEnabled(false);

                useFileButton.setSelection(true);
                noFileButton.setSelection(false);

                jsonFilePath.setEditable(true);
                encodingCombo.setReadOnly(false);
            } else {
                noFileButton.setEnabled(false);
                useFileButton.setEnabled(false);

                noFileButton.setSelection(true);
                useFileButton.setSelection(false);

                jsonFilePath.setEditable(false);
                encodingCombo.setReadOnly(true);
            }
            outputFilePath.setEditable(false);
        } else {
            if (isFromFile) {
                noFileButton.setEnabled(true);
                useFileButton.setEnabled(true);

                noFileButton.setSelection(false);
                useFileButton.setSelection(true);

                jsonFilePath.setEditable(true);
                encodingCombo.setReadOnly(false);
            } else {
                noFileButton.setEnabled(true);
                useFileButton.setEnabled(true);

                useFileButton.setSelection(false);
                noFileButton.setSelection(true);

                jsonFilePath.setEditable(false);
                encodingCombo.setReadOnly(true);
            }
            outputFilePath.setEditable(true);
        }
        super.adaptFormToEditable();
    }

    @Override
    protected void addFields() {
        createOutputSettingArea();
        createOutputFile(this, WIDTH_GRIDDATA_PIXEL, 50);

        SashForm sash = new SashForm(this, SWT.HORIZONTAL | SWT.SMOOTH);
        GridData sashData = new GridData(GridData.FILL_BOTH);
        sash.setLayoutData(sashData);
        createFileContentViewer(sash, 400, 100);
        createFileContentText(sash, 400, 100);
    }

    private void createOutputSettingArea() {
        Group group = Form.createGroup(this, 1, "Output Setting", 80);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 150;
        group.setLayoutData(data);

        Composite compositeButton = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, 20);
        noFileButton = new Button(compositeButton, SWT.RADIO);
        noFileButton.setText("Create manually");
        useFileButton = new Button(compositeButton, SWT.RADIO);
        useFileButton.setText("Create from a file");

        Composite compositeOutput = Form.startNewDimensionnedGridLayout(group, 3, WIDTH_GRIDDATA_PIXEL, 20);
        String[] extensions = new String[] { "*.JSON", "*.*", "*" };
        jsonFilePath = new LabelledFileField(compositeOutput, "JSON File", extensions);
        jsonFilePath.setText("");

        EMetadataEncoding[] values = EMetadataEncoding.values();
        String[] encodingData = new String[values.length];
        for (int j = 0; j < values.length; j++) {
            encodingData[j] = values[j].getName();
        }
        encodingCombo = new LabelledCombo(compositeOutput, "Encording", "Encording", encodingData, 1, true, SWT.NONE);
        encodingCombo.setText("");
        Composite limitation = new Composite(compositeOutput, SWT.NONE);
        limitation.setLayout(new GridLayout(2, false));
        labelLimitation = new Label(limitation, SWT.LEFT);
        labelLimitation.setText("Limit");
        commonNodesLimitation = new Text(limitation, SWT.BORDER);
        GridData gd = new GridData(18, 12);
        commonNodesLimitation.setLayoutData(gd);
        commonNodesLimitation.setText(String.valueOf(TreePopulator.getLimit()));
        labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, commonNodesLimitation.getText()));

        if (!noFileButton.getSelection() && !useFileButton.getSelection()) {
            noFileButton.setSelection(true);
        }
    }

    private void createOutputFile(final Composite mainComposite, final int width, final int height) {
        Group group = Form.createGroup(mainComposite, 1, "Output File Path", height);
        GridData fileData = new GridData(GridData.FILL_HORIZONTAL);
        fileData.heightHint = 60;
        group.setLayoutData(fileData);
        Composite compositeFile = Form.startNewDimensionnedGridLayout(group, 3, WIDTH_GRIDDATA_PIXEL, height);
        String[] outputExtensions = new String[] { "*.JSON" };
        outputFilePath = new LabelledFileField(compositeFile, "Output file", outputExtensions);
        outputFilePath.setText("");
    }

    private void createFileContentViewer(final Composite mainComposite, final int width, final int height) {
        Group group = Form.createGroup(mainComposite, 1, "File Viewer", height);
        Composite compositeFileViewer = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, height);

        GridData gridData = new GridData(GridData.FILL_BOTH);

        availableJSONTree = new Tree(compositeFileViewer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        availableJSONTree.setLayoutData(gridData);
    }

    private void createFileContentText(final Composite mainComposite, final int width, final int height) {
        Group group = Form.createGroup(mainComposite, 1, "File Content", height);
        Composite compositeFileContext = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, height);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = WIDTH_GRIDDATA_PIXEL;
        fileContentText = new Text(compositeFileContext, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        fileContentText.setLayoutData(gridData);
        fileContentText.setToolTipText("When Filepath is specified, you can read here the "
                + TreePopulator.getMaximumRowsToPreview() + " first rows of the file.");
        fileContentText.setText("Filepath must be specified to show the Data file");
    }

    private void updateConnection(String text) {
        if (text == null || "".equals(text)) {
            return;
        }

        List<FOXTreeNode> rootFoxTreeNodes = null;
        if (JSONFileOutputStep1Form.this.tempPath == null) {
            JSONFileOutputStep1Form.this.tempPath = JSONUtil.changeJsonToXml(text, getConnectionEncoding());
        }
        if (treeNode == null) {
            rootFoxTreeNodes = TreeUtil.getFoxTreeNodes(JSONFileOutputStep1Form.this.tempPath);
        } else {
            rootFoxTreeNodes = getCorrespondingFoxTreeNodes(treeNode, true);
        }

        if (rootFoxTreeNodes.size() == 0) {
            return;
        }
        if (ConnectionHelper.getTables(getConnection()).isEmpty()) {
            MetadataTable table = ConnectionFactory.eINSTANCE.createMetadataTable();
            RecordFile record = (RecordFile) ConnectionHelper.getPackage(getConnection().getName(), getConnection(),
                    RecordFile.class);
            if (record != null) { // hywang
                PackageHelper.addMetadataTable(table, record);
            } else {
                RecordFile newrecord = RecordFactory.eINSTANCE.createRecordFile();
                newrecord.setName(connection.getName());
                ConnectionHelper.addPackage(newrecord, connection);
                PackageHelper.addMetadataTable(table, newrecord);
            }
        }
        EList schemaMetadataColumn = ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0].getColumns();
        schemaMetadataColumn.clear();
        initMetadataTable(rootFoxTreeNodes, schemaMetadataColumn);
        updateConnectionProperties(rootFoxTreeNodes.get(0));
    }

    @Override
    protected void addFieldsListeners() {
        jsonFilePath.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (jsonFilePath.getResult() == null) {
                    return;
                }
                String text = jsonFilePath.getText();
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                            connectionItem.getConnection(), true);
                    text = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
                }
                // getConnection().setJSONFilePath(PathUtils.getPortablePath(JSONXsdFilePath.getText()));
                if (JSONFileOutputStep1Form.this.tempPath == null) {
                    JSONFileOutputStep1Form.this.tempPath = JSONUtil.changeJsonToXml(text, getConnectionEncoding());
                }
                File file = new File(text);
                if (file.exists()) {
                    List<ATreeNode> treeNodes = new ArrayList<ATreeNode>();
                    valid = treePopulator.populateTree(JSONUtil.changeJsonToXml(text, getConnectionEncoding()), treeNode);
                    checkFieldsValue();
                    if (!valid) {
                        return;
                    }
                    if (treeNodes.size() > 0) {
                        treeNode = treeNodes.get(0);
                    }

                    updateConnection(text);
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        jsonFilePath.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                validateJsonFile();
            }
        });

        encodingCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setEncoding(encodingCombo.getText());
                try {
                    Charset charSet = Charset.forName(getConnectionEncoding());
                    if (charSet != null) {
                        validateJsonFile();
                        return;
                    }
                } catch (Exception ex) {
                    // ignore
                }
                checkFieldsValue();
            }
        });

        commonNodesLimitation.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String str = commonNodesLimitation.getText();
                if ((!str.matches("\\d+")) || (Integer.valueOf(str) < 0)) {
                    commonNodesLimitation.setText(String.valueOf(treePopulator.getLimit()));
                    labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, commonNodesLimitation.getText()));
                } else {
                    treePopulator.setLimit(Integer.valueOf(str));
                    labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, str));
                }
                if (JSONFileOutputStep1Form.this.tempPath == null) {
                    JSONFileOutputStep1Form.this.tempPath = JSONUtil.changeJsonToXml(jsonFilePath.getText(),
                            getConnectionEncoding());
                }

                File file = new File(JSONFileOutputStep1Form.this.tempPath);
                if (file.exists()) {
                    valid = treePopulator.populateTree(JSONFileOutputStep1Form.this.tempPath, treeNode);
                }
                checkFieldsValue();
            }
        });
        commonNodesLimitation.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                commonNodesLimitation.setText(String.valueOf(TreePopulator.getLimit()));
                labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, commonNodesLimitation.getText()));
            }

        });

        outputFilePath.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setOutputFilePath(PathUtils.getPortablePath(outputFilePath.getText()));
                checkFieldsValue();
            }
        });
    }

    @Override
    protected void addUtilsButtonListeners() {
        noFileButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                jsonFilePath.setEditable(false);
                jsonFilePath.setText("");
                encodingCombo.setEnabled(false);
                commonNodesLimitation.setEditable(false);
                availableJSONTree.setEnabled(false);
                fileContentText.setEnabled(false);
                getConnection().setJSONFilePath("");
                ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0].getColumns().clear();
                // ((MetadataTable) getConnection().getTables().get(0)).getColumns().clear();
                getConnection().getRoot().clear();
                getConnection().getLoop().clear();
                getConnection().getGroup().clear();
                checkFieldsValue();
            }
        });
        useFileButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                jsonFilePath.setEditable(true);
                String text = jsonFilePath.getText();
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                            connectionItem.getConnection(), true);
                    text = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
                }
                getConnection().setJSONFilePath(text);
                updateConnection(text);
                encodingCombo.setEnabled(true);
                commonNodesLimitation.setEditable(true);
                availableJSONTree.setEnabled(true);
                fileContentText.setEnabled(true);
                fileContentText.setEditable(false);
                checkFieldsValue();
            }
        });
    }

    @Override
    protected boolean checkFieldsValue() {
        String jsonFilePathText = jsonFilePath.getText();
        String outputFilePathText = outputFilePath.getText();
        boolean editable = jsonFilePath.getEditable();
        StringBuffer msgError = new StringBuffer();
        if (creation && !noFileButton.getSelection() && !useFileButton.getSelection()) {
            msgError.append("Should select one model\n");
        }
        if (creation && editable && jsonFilePathText == "") {
            msgError.append("JSON filepath must be specified\n");
        }
        if (!valid && creation) {
            if (jsonFilePathText != null && !"".equals(jsonFilePathText)) {
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper
                            .getContextTypeForContextMode(connectionItem.getConnection());
                    jsonFilePathText = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                            jsonFilePathText));
                }
                msgError.append(jsonFilePathText + " is not found or the JSON format is incorrect.\n");
            }
        }
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(), true);
            outputFilePathText = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                    outputFilePathText));
        }
        if (outputFilePathText != null && !outputFilePathText.equals("") && !isJSONFile(outputFilePathText)) {
            msgError.append("Output file is not a JSON file\n");
        }
        if ("".equals(msgError.toString())) {
            updateStatus(IStatus.OK, null);
            return true;
        }
        updateStatus(IStatus.ERROR, msgError.toString());
        return false;
    }

    private boolean isJSONFile(String value) {
        if (value != null) {
            return value.toLowerCase().endsWith(".json");
        }
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) {
                encodingCombo.setText(getConnection().getEncoding());
                isModifing = false;
                jsonFilePath.setText(getConnection().getJSONFilePath());
                outputFilePath.setText(getConnection().getOutputFilePath());
            }
            String JSONFilePath = getConnection().getJSONFilePath();
            boolean enable = (JSONFilePath != null && !JSONFilePath.equals(""));
            if (!creation) {
                noFileButton.setSelection(!enable);
                useFileButton.setSelection(enable);
            }
            if (!isContextMode()) {
                jsonFilePath.setEditable(enable);
                encodingCombo.setEnabled(enable);
                commonNodesLimitation.setEditable(enable);
                availableJSONTree.setEnabled(enable);
                fileContentText.setEnabled(enable);
                outputFilePath.setEditable(true);
                exportContextBtn.setEnabled(true);
                revertContextBtn.setEnabled(false);
            } else {
                // noFileButton.setEnabled(!isContextMode());
                // useFileButton.setEnabled(!isContextMode());
                outputFilePath.setEditable(!isContextMode());
                jsonFilePath.setEditable(!isContextMode() || enable);
                encodingCombo.setEnabled(!isContextMode() || enable);
                commonNodesLimitation.setEditable(!isContextMode());
                availableJSONTree.setEnabled(!isContextMode() || enable);
                fileContentText.setEnabled(!isContextMode());
                exportContextBtn.setEnabled(false);
                revertContextBtn.setEnabled(true);
            }
        }
    }

    @Override
    protected void adaptFormToReadOnly() {

    }

    private void setFileContent(File initFile) {
        InputStream stream = null;
        try {
            stream = initFile.toURL().openStream();
            byte[] innerContent = new byte[stream.available()];
            stream.read(innerContent);
            stream.close();
            getConnection().setFileContent(innerContent);
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    private void initFileContent() {
        if (getConnection().getJSONFilePath() == null || "".equals(getConnection().getJSONFilePath())) {
            return;
        }
        byte[] bytes = getConnection().getFileContent();
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject fsProject = null;
        try {
            fsProject = ResourceUtils.getProject(project);
        } catch (PersistenceException e2) {
            ExceptionHandler.process(e2);
        }
        if (fsProject == null) {
            return;
        }
        String tem = fsProject.getLocationURI().getPath() + File.separator + "temp";
        String fileName = "";

        String jsonPath = getConnection().getJSONFilePath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
            jsonPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, jsonPath));
        }
        if (jsonPath != null && jsonPath.contains(".zip")) {
            fileName = new Path(jsonPath).lastSegment();
        } else if (jsonPath != null) {
            fileName = JSONUtil.TMP_JSON_FILE;
        }
        File temfile = new File(tem + File.separator + fileName);
        if (!temfile.exists()) {
            try {
                temfile.createNewFile();
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(temfile);
            outStream.write(bytes);
            outStream.close();
        } catch (FileNotFoundException e1) {
            ExceptionHandler.process(e1);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        tempJSONPath = temfile.getPath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
            tempJSONPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, tempJSONPath));
        }
        // valid = this.treePopulator.populateTree(tempJSONXsdPath, treeNode);

    }

    private void validateJsonFile() {
        String text = jsonFilePath.getText();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                    connectionItem.getConnection(), true);
            text = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
        }

        if (getConnection().getJSONFilePath() != null && !getConnection().getJSONFilePath().equals(text)) {
            getConnection().getLoop().clear();
            getConnection().getRoot().clear();
            getConnection().getGroup().clear();
            xsdPathChanged = true;
        } else {
            xsdPathChanged = false;
        }
        if (Path.fromOSString(jsonFilePath.getText()).toFile().isFile()) {
            getConnection().setJSONFilePath(PathUtils.getPortablePath(jsonFilePath.getText()));
        } else {
            getConnection().setJSONFilePath(jsonFilePath.getText());
        }

        // updateConnection(text);

        StringBuilder fileContent = new StringBuilder();
        BufferedReader in = null;
        File file = null;
        if (tempJSONPath != null && getConnection().getFileContent() != null
                && getConnection().getFileContent().length > 0 && !isModifing) {
            file = new File(tempJSONPath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e2) {
                    ExceptionHandler.process(e2);
                }
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

        } else {
            file = new File(text);
        }
        String str;
        try {
            Charset guessCharset = null;
            try {
                guessCharset = Charset.forName(getConnectionEncoding());
            } catch (Exception e) {
                if (CommonsPlugin.isDebugMode()) {
                    ExceptionHandler.process(e, Priority.INFO);
                }
            }
            if (guessCharset == null) {
                guessCharset = CharsetToolkit.guessEncoding(file, 4096);
            }
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), guessCharset.displayName()));

            while ((str = in.readLine()) != null) {
                fileContent.append(str + "\n");
                // for encoding
                if (str.contains("encoding")) {
                    String regex = "^<\\?JSON\\s*version=\\\"[^\\\"]*\\\"\\s*encoding=\\\"([^\\\"]*)\\\"\\?>$";
                    Perl5Compiler compiler = new Perl5Compiler();
                    Perl5Matcher matcher = new Perl5Matcher();
                    Pattern pattern = null;
                    try {
                        pattern = compiler.compile(regex);
                        if (matcher.contains(str, pattern)) {
                            MatchResult matchResult = matcher.getMatch();
                            if (matchResult != null) {
                                encoding = matchResult.group(1);
                            }
                        }
                    } catch (MalformedPatternException malE) {
                        ExceptionHandler.process(malE);
                    }
                }
            }

            fileContentText.setText(new String(fileContent));

        } catch (Exception e) {
            String msgError = "File" + " \"" + jsonFilePath.getText().replace("\\\\", "\\") + "\"\n";
            if (e instanceof FileNotFoundException) {
                msgError = msgError + "is not found";
            } else if (e instanceof EOFException) {
                msgError = msgError + "have an incorrect character EOF";
            } else if (e instanceof IOException) {
                msgError = msgError + "is locked by another soft";
            } else {
                msgError = msgError + "doesn't exist";
            }
            fileContentText.setText(msgError);
            if (!isReadOnly()) {
                updateStatus(IStatus.ERROR, msgError);
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception exception) {
                ExceptionHandler.process(exception);
            }
        }
        if (getConnection().getEncoding() == null || "".equals(getConnection().getEncoding())) {
            getConnection().setEncoding(encoding);
            if (encoding != null && !"".equals(encoding)) {
                encodingCombo.setText(encoding);
            } else {
                encodingCombo.setText("UTF-8");
            }
        }

        // if (tempJSONXsdPath != null && getConnection().getFileContent() != null
        // && getConnection().getFileContent().length > 0 && !isModifing) {
        // valid = treePopulator.populateTree(tempJSONXsdPath, treeNode);
        // } else {
        // valid = treePopulator.populateTree(text, treeNode);
        // }
        if (file.exists()) {
            valid = treePopulator.populateTree(JSONUtil.changeJsonToXml(text, getConnectionEncoding()), treeNode);
            updateConnection(text);
        }
        checkFieldsValue();
        isModifing = true;
    }
}
