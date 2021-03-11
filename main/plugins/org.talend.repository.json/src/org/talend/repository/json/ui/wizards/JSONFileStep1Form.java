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
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.commons.utils.encoding.CharsetToolkit;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.ModuleNeeded.ELibraryInstallStatus;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.EMetadataEncoding;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.json.i18n.Messages;
import org.talend.repository.json.util.JSONUtil;
import org.talend.repository.ui.wizards.metadata.connection.files.json.AbstractTreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;
import org.talend.repository.ui.wizards.metadata.connection.files.json.JsonTreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.TreePopulator;

/**
 * @author ocarbone
 *
 */
public class JSONFileStep1Form extends AbstractJSONFileStepForm {

    private static Logger log = Logger.getLogger(JSONFileStep1Form.class);

    /**
     * Settings.
     */
    private static final int WIDTH_GRIDDATA_PIXEL = 300;

    /**
     * Main Fields.
     */

    private LabelledCombo readbyCombo;

    private LabelledFileField fileFieldJSON;

    private LabelledText fieldMaskXPattern;

    private Label labelIsGuess;

    private Button checkBoxIsGuess;

    /**
     * Another.
     */
    private boolean filePathIsDone;

    private transient Tree availableJSONTree;

    private transient TreeViewer availableJSONTreeViewer;

    private ATreeNode treeNode;

    private UtilsButton cancelButton;

    private boolean readOnly;

    private boolean creation;

    private AbstractTreePopulator treePopulator;

    private TreePopulator xmlTreePopulator;

    private JsonTreePopulator jsonTreePopulator;

    private LabelledCombo encodingCombo;

    private String encoding;

    private String tempJSONXsdPath;

    private boolean isModifing = true;

    private JSONWizard wizard;

    private int limit;

    private Text commonNodesLimitation;

    private Label labelLimitation;

    /**
     * Constructor to use by RCP Wizard.
     *
     * @param existingNames
     *
     * @param Composite
     * @param Wizard
     * @param Style
     */
    public JSONFileStep1Form(boolean creation, Composite parent, ConnectionItem connectionItem, String[] existingNames,
            JSONWizard wizard) {
        super(parent, connectionItem, existingNames);
        this.creation = creation;
        this.wizard = wizard;
        setupForm();
    }

    /**
     *
     * Initialize value, forceFocus first field.
     */
    @Override
    protected void initialize() {
        getConnection().setInputModel(true);
        availableJSONTreeViewer = new TreeViewer(availableJSONTree);
        this.xmlTreePopulator = new TreePopulator(availableJSONTreeViewer);
        // this.xmlTreePopulator.configureDefaultTreeViewer();
        this.jsonTreePopulator = new JsonTreePopulator(availableJSONTreeViewer);
        // this.jsonTreePopulator.configureDefaultTreeViewer();
        this.availableJSONTreeViewer.setUseHashlookup(true);

        // add init of CheckBoxIsGuess and Determine the Initialize checkFileXsdorJSON
        // if (getConnection().getXsdFilePath() != null) {
        // fileFieldXsd.setText(getConnection().getXsdFilePath().replace("\\\\", "\\"));
        // // init the fileViewer
        // this.treePopulator.populateTree(fileFieldXsd.getText(), treeNode);
        // checkFieldsValue();
        // }

        EJsonReadbyMode eJsonReadbyMode = null;

        if (getConnection().getReadbyMode() != null) {
            eJsonReadbyMode = EJsonReadbyMode.getEJsonReadbyModeByValue(getConnection().getReadbyMode());
        }
        if (eJsonReadbyMode == null) {
            eJsonReadbyMode = getDefaultJsonReadbyMode();
        }
        // JSONFileStep1Form.this.wizard.setReadbyMode(readbyCombo.getText());
        readbyCombo.setText(eJsonReadbyMode.getDisplayName());
        getConnection().setReadbyMode(eJsonReadbyMode.getValue());
        JSONFileStep1Form.this.wizard.setReadbyMode(eJsonReadbyMode.getValue());

        if (getConnection().getJSONFilePath() != null) {
            fileFieldJSON.setText(getConnection().getJSONFilePath().replace("\\\\", "\\")); //$NON-NLS-1$ //$NON-NLS-2$
            // init the fileViewer
            checkFieldsValue();
            String jsonFilePath = fileFieldJSON.getText();
            if (isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
                jsonFilePath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                        fileFieldJSON.getText()));
            }
            File file = new File(jsonFilePath);
            if (!file.exists() && getConnection().getFileContent() != null && getConnection().getFileContent().length > 0) {
                initFileContent();
                jsonFilePath = tempJSONXsdPath;
            }
            String tempxml = null;
            tempxml = getFilePath4Populate(jsonFilePath);
            switchPopulator(JSONFileStep1Form.this.wizard.getReadbyMode(), tempxml);

        }

        // Fields to the Group Delimited File Settings
        if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) { //$NON-NLS-1$
            encodingCombo.setText(getConnection().getEncoding());
        } else {
            encodingCombo.select(0);
        }
        encodingCombo.clearSelection();

        // if (getConnection().getMaskXPattern() != null) {
        // fieldMaskXPattern.setText(getConnection().getMaskXPattern().replace("\\\\", "\\"));
        // }
        adaptFormToEditable();

    }

    private String getFilePath4Populate(String jsonFilePath) {
        String tempxml = null;
        if (JSONFileStep1Form.this.wizard.getTempJsonPath() == null
                || JSONFileStep1Form.this.wizard.getTempJsonPath().length() == 0) {
            if (EJsonReadbyMode.XPATH.getValue().equals(JSONFileStep1Form.this.wizard.getReadbyMode())) {
                tempxml = JSONUtil.changeJsonToXml(jsonFilePath, getConnectionEncoding());
            } else {
                tempxml = jsonFilePath;
            }
            JSONFileStep1Form.this.wizard.setTempJsonPath(tempxml);
        } else {
            tempxml = JSONFileStep1Form.this.wizard.getTempJsonPath();
        }
        return tempxml;
    }

    private void switchPopulator(String readbyMode, String filePath) {
        if (EJsonReadbyMode.XPATH.getValue().equals(readbyMode)) {
            JSONFileStep1Form.this.wizard.setReadbyMode(readbyMode);
            this.treePopulator = xmlTreePopulator;
        } else if (EJsonReadbyMode.JSONPATH.getValue().equals(readbyMode)) {
            JSONFileStep1Form.this.wizard.setReadbyMode(readbyMode);
            this.treePopulator = jsonTreePopulator;
        } else {
            CommonExceptionHandler.process(new Exception("Unknown ReadBy mode"));
            JSONFileStep1Form.this.wizard.setReadbyMode(EJsonReadbyMode.JSONPATH.getValue());
            this.treePopulator = jsonTreePopulator;
        }
        getConnection().setReadbyMode(readbyMode);
        treePopulator.setLimit(limit);
        this.treePopulator.configureDefaultTreeViewer();
        if (filePath != null && !filePath.isEmpty()) {
            this.treePopulator.setEncoding(getConnectionEncoding());
            valid = this.treePopulator.populateTree(filePath, treeNode);
        }
    }

    /**
     * DOC ocarbone Comment method "adaptFormToReadOnly".
     *
     */
    @Override
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();
        // fileFieldXsd.setReadOnly(isReadOnly());
        fileFieldJSON.setReadOnly(isReadOnly());
        // fieldMaskXPattern.setReadOnly(isReadOnly());
        // checkBoxIsGuess.setReadOnly(isReadOnly());
        updateStatus(IStatus.OK, ""); //$NON-NLS-1$
    }

    @Override
    protected void adaptFormToEditable() {
        fileFieldJSON.setEditable(!isContextMode());
        encodingCombo.setReadOnly(isContextMode());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFields()
     */
    @Override
    protected void addFields() {

        // Group File Location
        Group group = Form.createGroup(this, 1, "File Settings", 100);
        Composite compositeFileLocation = Form.startNewDimensionnedGridLayout(group, 3, WIDTH_GRIDDATA_PIXEL, 100);

        GridData gridDataFileLocation = new GridData(GridData.FILL_HORIZONTAL);
        gridDataFileLocation.minimumWidth = WIDTH_GRIDDATA_PIXEL;
        group.setLayoutData(gridDataFileLocation);

        // file Field XSD
        //        String[] xsdExtensions = { "*.xsd", "*.*", "*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // fileFieldXsd = new LabelledFileField(compositeFileLocation, Messages.getString("JSONFileStep1.filepathXsd"),
        // xsdExtensions);

        // checkBox IsGuess
        // checkBoxIsGuess = new Button(compositeFileLocation, SWT.CHECK);
        // labelIsGuess = new Label(compositeFileLocation, SWT.LEFT);
        // GridData gridDataLabel = new GridData();
        // gridDataLabel.horizontalSpan = 2;
        // labelIsGuess.setLayoutData(gridDataLabel);
        // labelIsGuess.setText(Messages.getString("JSONFileStep1.checkBoxIsGuess"));

        // file Field JSON
        List<String> readbyModeValues = EJsonReadbyMode.getUsableReadbyModeValues();
        readbyCombo = new LabelledCombo(compositeFileLocation, "Read By", "Read By",
                readbyModeValues.toArray(new String[readbyModeValues.size()]), 2, true, SWT.READ_ONLY);
        String[] JSONExtensions = { "*.JSON", "*.*", "*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        fileFieldJSON = new LabelledFileField(compositeFileLocation, "Json", //$NON-NLS-1$
                JSONExtensions);

        EMetadataEncoding[] values = EMetadataEncoding.values();
        String[] encodingData = new String[values.length];
        for (int j = 0; j < values.length; j++) {
            encodingData[j] = values[j].getName();
        }

        encodingCombo = new LabelledCombo(compositeFileLocation, "Encoding", "Encoding", encodingData, 1, true, SWT.NONE); //$NON-NLS-1$

        Composite limitation = new Composite(compositeFileLocation, SWT.NONE);
        limitation.setLayout(new GridLayout(2, false));

        labelLimitation = new Label(limitation, SWT.LEFT);
        labelLimitation.setText("Limit"); //$NON-NLS-1$
        commonNodesLimitation = new Text(limitation, SWT.BORDER);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_FILL);
        gd.widthHint = 18;
        commonNodesLimitation.setLayoutData(gd);
        commonNodesLimitation.setText(String.valueOf(TreePopulator.getLimit()));
        labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, commonNodesLimitation.getText()));

        commonNodesLimitation.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {

                String str = commonNodesLimitation.getText();

                if ((!str.matches("\\d+")) || (Integer.valueOf(str) < 0)) { //$NON-NLS-1$
                    commonNodesLimitation.setText(String.valueOf(treePopulator.getLimit()));
                    labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, commonNodesLimitation.getText()));;
                } else {
                    limit = Integer.valueOf(str);
                    labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, limit));
                }

                String tempxml = null;
                if (tempJSONXsdPath != null && getConnection().getFileContent() != null
                        && getConnection().getFileContent().length > 0) {
                    tempxml = getFilePath4Populate(tempJSONXsdPath);
                } else {
                    tempxml = getFilePath4Populate(fileFieldJSON.getText());
                }
                switchPopulator(JSONFileStep1Form.this.wizard.getReadbyMode(), tempxml);
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

        // field XmaskPattern
        // fieldMaskXPattern = new LabelledText(compositeFileLocation,
        // Messages.getString("JSONFileStep1.maskXPattern"));

        // Group Schema Viewer
        group = Form.createGroup(this, 1, "Schema Viewer", 220);
        Composite compositeFileViewer = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, 220);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = WIDTH_GRIDDATA_PIXEL;
        // gridData.minimumHeight = 150;

        availableJSONTree = new Tree(compositeFileViewer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        availableJSONTree.setLayoutData(gridData);

        if (!isInWizard()) {
            // Composite BottomButton
            Composite compositeBottomButton = Form.startNewGridLayout(this, 2, false, SWT.CENTER, SWT.CENTER);

            // Button Cancel
            cancelButton = new UtilsButton(compositeBottomButton, "Cancel", WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);
        }
        addUtilsButtonListeners();
    }

    @Override
    protected void addUtilsButtonListeners() {

        if (!isInWizard()) {
            // Event cancelButton
            cancelButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    getShell().close();
                }
            });
        }
    }

    boolean valid = true;

    /**
     * Main Fields addControls.
     */
    @Override
    protected void addFieldsListeners() {

        // fileFieldXsd : Event modifyText
        // fileFieldXsd.addModifyListener(new ModifyListener() {
        //
        // public void modifyText(final ModifyEvent e) {
        // getConnection().setXsdFilePath(fileFieldXsd.getText());
        // treePopulator.populateTree(fileFieldXsd.getText(), treeNode);
        // checkFieldsValue();
        // }
        // });

        // fileFieldJSON.addSelectionListener(new SelectionListener() {
        //
        // public void widgetSelected(SelectionEvent event) {
        // if (fileFieldJSON.getResult() == null) {
        // return;
        // }
        // String text = fileFieldJSON.getText();
        // if (isContextMode()) {
        // ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
        // connectionItem.getConnection(), true);
        // text = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
        // }
        // // getConnection().setJSONFilePath(PathUtils.getPortablePath(JSONXsdFilePath.getText()));
        // File file = new File(text);
        // if (file.exists()) {
        // if (file.exists()) {
        // String tempxml = JSONUtil.changeJsonToXml(text);
        // JSONFileStep1Form.this.wizard.setTempJsonPath(tempxml);
        // valid = treePopulator.populateTree(tempxml, treeNode);
        // }
        // // add for bug TDI-20432
        // checkFieldsValue();
        // }
        //
        // }
        //
        // public void widgetDefaultSelected(SelectionEvent e) {
        //
        // }
        // });

        readbyCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                EJsonReadbyMode eJsonReadbyMode = EJsonReadbyMode.getEJsonReadbyModeByDisplayName(readbyCombo.getText());
                if (eJsonReadbyMode == null) {
                    eJsonReadbyMode = getDefaultJsonReadbyMode();
                }
                String readbyMode = eJsonReadbyMode.getValue();
                JSONFileStep1Form.this.wizard.setReadbyMode(readbyMode);
                String jsonPath = fileFieldJSON.getText();
                String text = validateJsonFilePath(jsonPath);
                if (text == null || text.isEmpty()) {
                    return;
                }

                String tempxml = null;

                if (EJsonReadbyMode.JSONPATH.getValue().equals(readbyMode)) {
                    tempxml = text;
                } else {
                    tempxml = JSONUtil.changeJsonToXml(text, getConnectionEncoding());
                }
                JSONFileStep1Form.this.wizard.setTempJsonPath(tempxml);
                switchPopulator(readbyMode, tempxml);
            }
        });

        // fileFieldJSON : Event modifyText
        fileFieldJSON.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                validateJsonFile();
            }
        });

        // Event encodingCombo
        encodingCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setEncoding(encodingCombo.getText());
                String encoding = getConnectionEncoding();
                if (treePopulator != null) {
                    treePopulator.setEncoding(encoding);
                }
                try {
                    Charset charSet = Charset.forName(encoding);
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

    }

    private String validateJsonFilePath(String jsonPath) {
        try {
            valid = jsonPath != null && !jsonPath.isEmpty()
                    && (new File(jsonPath).exists() || new URL(jsonPath).openStream() != null);

        } catch (MalformedURLException e1) {
            valid = false;
        } catch (IOException e1) {
            valid = false;
        }
        // add for bug TDI-20432
        checkFieldsValue();
        if (!valid) {
            return null;
        }
        String text = jsonPath;
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(), true);
            text = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
        }
        // getConnection().setJSONFilePath(PathUtils.getPortablePath(JSONXsdFilePath.getText()));
        File file = new File(text);
        if (file.isDirectory()) {
            valid = false;
            checkFieldsValue();
            return null;
        }
        return text;
    }

    private EJsonReadbyMode getDefaultJsonReadbyMode() {
        return EJsonReadbyMode.JSONPATH;
    }

    /**
     * Ensures that fields are set.
     *
     * @return
     */
    @Override
    protected boolean checkFieldsValue() {
        String jsonFilePathText = fileFieldJSON.getText();
        // The fields
        if ("Filepath must be specified".equals(jsonFilePathText)) {
            updateStatus(IStatus.ERROR, ""); //$NON-NLS-1$
            return false;
        }
        File file = new File(jsonFilePathText);
        if (file.isFile() && !file.exists()) {
            valid = false;
        } else if (file.isDirectory()) {
            valid = false;
        }
        if (!valid) {
            if (isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
                jsonFilePathText = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                        jsonFilePathText));
            }
            updateStatus(IStatus.ERROR, "File is not found or the json format is incorrect.");

            return false;
        }
        updateStatus(IStatus.OK, null);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (isReadOnly() != readOnly) {
            adaptFormToReadOnly();
        }
        if (super.isVisible()) {
            String JSONFilePath = getConnection().getJSONFilePath();
            // Fields to the Group Delimited File Settings
            if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) { //$NON-NLS-1$
                encodingCombo.setText(getConnection().getEncoding());
                isModifing = false;
                fileFieldJSON.setText(JSONFilePath);
            } else {
                encodingCombo.select(0);
            }

            if (isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(),
                        connectionItem.getConnection().getContextName());
                JSONFilePath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, JSONFilePath));
            }
            if (!creation) {
                updateTreeNodes(JSONFilePath);
                JSONWizard wizard = ((JSONWizard) getPage().getWizard());
                if (wizard.getTreeRootNode() == null) {
                    wizard.setTreeRootNode(treeNode);
                }
            }

            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.PERL
                    && GlobalServiceRegister.getDefault().isServiceRegistered(ILibrariesService.class)) {
                ILibrariesService moduleService = (ILibrariesService) GlobalServiceRegister.getDefault().getService(
                        ILibrariesService.class);
                try {
                    ELibraryInstallStatus status = moduleService.getLibraryStatus("JSON::LibJSON");
                    if (status != ELibraryInstallStatus.INSTALLED) {
                        new ErrorDialogWidthDetailArea(getShell(), PID, "The Perl Module" + " JSON::LibJSON "
                                + "is not installed",
                                "For more information, check out Talend's Wiki website at http\\://talendforge.org/wiki/doku.php.");
                        log.error("The Perl Module" + " JSON::LibJSONL " + "is not installed");
                    }
                } catch (BusinessException e) {
                    new ErrorDialogWidthDetailArea(getShell(), PID, "The Perl Module" + " JSON::LibJSON " + "is not installed",
                            "For more information, check out Talend's Wiki website at http\\://talendforge.org/wiki/doku.php.");
                    log.error("The Perl Module" + " JSON::LibJSONL " + "is not installed");
                }
            }

            adaptFormToEditable();

        }
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
        String temPath = fsProject.getLocationURI().getPath() + File.separator + "temp"; //$NON-NLS-1$
        String fileName = ""; //$NON-NLS-1$
        if (getConnection().getJSONFilePath() != null) {
            fileName = "tempJSONFile" + '.' + "json";
        }
        File temfile = new File(temPath + File.separator + fileName);

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
        tempJSONXsdPath = temfile.getPath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
            tempJSONXsdPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper
                    .getOriginalValue(contextType, tempJSONXsdPath));
        }
        // valid = this.treePopulator.populateTree(tempJSONXsdPath, treeNode);
        // temfile.delete();
    }

    private void validateJsonFile() {
        String jsonPath = fileFieldJSON.getText();
        String _jsonPath = jsonPath;
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                    connectionItem.getConnection(), connectionItem.getConnection().getContextName());
            jsonPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                    jsonPath));

        }
        String text = validateJsonFilePath(jsonPath);
        if (text == null || text.isEmpty()) {
            return;
        }

        String tempxml = null;
        String readbyMode = JSONFileStep1Form.this.wizard.getReadbyMode();

        if (EJsonReadbyMode.JSONPATH.getValue().equals(readbyMode)) {
            tempxml = text;
        } else {
            tempxml = JSONUtil.changeJsonToXml(text, getConnectionEncoding());
        }
        File file = new File(text);
        if (!file.exists()) {
            file = new File(JSONUtil.tempJSONXsdPath);
        }
        JSONFileStep1Form.this.wizard.setTempJsonPath(tempxml);
        String limitString = commonNodesLimitation.getText();
        try {
            limit = Integer.valueOf(limitString);
            labelLimitation.setToolTipText(MessageFormat.format(Messages.JSONLimitToolTip, limit));
        } catch (Exception excpt) {
            // nothing need to do
        }
        switchPopulator(readbyMode, tempxml);
        // }
        // add for bug TDI-20432
        checkFieldsValue();
        // }

        // String text = fileFieldJSON.getText();
        // File temp = new File(text);
        // if (!temp.exists()) {
        // return;
        // }

        if (getConnection().getJSONFilePath() != null && !getConnection().getJSONFilePath().equals(text)) {
            getConnection().getLoop().clear();
            xsdPathChanged = true;
        } else {
            xsdPathChanged = false;
        }

        if (isContextMode()) {
            jsonPath = _jsonPath;
        }
        if (Path.fromOSString(jsonPath).toFile().isFile()) {
            getConnection().setJSONFilePath(PathUtils.getPortablePath(jsonPath));
        } else {
            getConnection().setJSONFilePath(jsonPath);
        }

        JSONWizard wizard = ((JSONWizard) getPage().getWizard());
        wizard.setTreeRootNode(treeNode);

        BufferedReader in = null;

        // File file = null;
        //
        // if (tempJSONXsdPath != null && getConnection().getFileContent() != null
        // && getConnection().getFileContent().length > 0 && !isModifing) {
        // file = new File(tempJSONXsdPath);
        // if (!file.exists()) {
        // try {
        // file.createNewFile();
        // } catch (IOException e2) {
        // ExceptionHandler.process(e2);
        // }
        // FileOutputStream outStream;
        // try {
        // outStream = new FileOutputStream(file);
        // outStream.write(getConnection().getFileContent());
        // outStream.close();
        // } catch (FileNotFoundException e1) {
        // ExceptionHandler.process(e1);
        // } catch (IOException e3) {
        // ExceptionHandler.process(e3);
        // }
        // }
        //
        // } else {
        // file = new File(text);
        // }

        // setFileContent(file);
        // }

        try {

            Charset guessedCharset = CharsetToolkit.guessEncoding(file, 4096);
            String str;
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), guessedCharset.displayName()));
            while ((str = in.readLine()) != null) {
                if (str.contains("encoding")) { //$NON-NLS-1$
                    String regex = "^<\\?JSON\\s*version=\\\"[^\\\"]*\\\"\\s*encoding=\\\"([^\\\"]*)\\\"\\?>$"; //$NON-NLS-1$

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
        } catch (Exception ex) {
            String fileStr = text;
            String msgError = "JSON" + " \"" + fileStr.replace("\\\\", "\\") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + "\"\n"; //$NON-NLS-1$
            if (ex instanceof FileNotFoundException) {
                msgError = msgError + "is not found";
            } else if (ex instanceof EOFException) {
                msgError = msgError + "have an incorrect character EOF";
            } else if (ex instanceof IOException) {
                msgError = msgError + "is locked by another soft";
            } else {
                msgError = msgError + "doesn't exist";
            }
            if (!isReadOnly()) {
                updateStatus(IStatus.ERROR, msgError);
            }
            // ExceptionHandler.process(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex2) {
                ExceptionHandler.process(ex2);
            }
        }
        if (getConnection().getEncoding() == null || "".equals(getConnection().getEncoding())) { //$NON-NLS-1$
            getConnection().setEncoding(encoding);
            if (encoding != null && !("").equals(encoding)) { //$NON-NLS-1$
                encodingCombo.setText(encoding);
            } else {
                encodingCombo.setText("UTF-8"); //$NON-NLS-1$
            }
        }
        // if (tempJSONXsdPath != null && getConnection().getFileContent() != null
        // && getConnection().getFileContent().length > 0 && !isModifing) {
        // valid = treePopulator.populateTree(tempJSONXsdPath, treeNode);
        // } else {
        // valid = treePopulator.populateTree(text, treeNode);
        // }
        checkFieldsValue();
        isModifing = true;
    }
}
