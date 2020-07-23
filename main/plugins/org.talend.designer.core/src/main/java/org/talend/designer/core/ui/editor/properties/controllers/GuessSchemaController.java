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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWithDetailAreaAndContinueButton;
import org.talend.commons.utils.data.list.UniqueStringGenerator;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRuleService;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.ConnParameterKeys;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataFromDataBase;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.core.model.metadata.connection.hive.HiveModeInfo;
import org.talend.core.model.metadata.connection.hive.HiveServerVersionInfo;
import org.talend.core.model.metadata.types.JavaDataTypeHelper;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.PerlDataTypeHelper;
import org.talend.core.model.metadata.types.PerlTypesManager;
import org.talend.core.model.metadata.types.TypesManager;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.connection.ConnectionStatus;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.services.ISQLBuilderService;
import org.talend.core.utils.CsvArray;
import org.talend.core.utils.KeywordsValidator;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.connections.TracesConnectionUtils;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.ConfigureConnParamDialog;
import org.talend.designer.core.ui.editor.properties.controllers.uidialog.OpenContextChooseComboDialog;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.metadata.managment.connection.manager.HiveConnectionManager;
import org.talend.metadata.managment.connection.manager.ImpalaConnectionManager;
import org.talend.metadata.managment.ui.dialog.MappingFileSelectDialog;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.ui.utils.ColumnNameValidator;

/**
 * DOC zqin class global comment. Detailled comment
 */
public class GuessSchemaController extends AbstractElementPropertySectionController {

    private static final String CONTEXT_CHOOSE_DIALOG_TITLE = "Choose a context for query :";//$NON-NLS-1$

    private static final String GUESS_SCHEMA_NAME = "Guess schema"; //$NON-NLS-1$

    private static final String SCHEMA = "SCHEMA"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(GuessSchemaController.class);

    private Map<IElementParameter, Button> queryButton = new HashMap<IElementParameter, Button>();

    // added by hyWang
    private DbInfo info;

    private Button btn;

    private ChangeMetadataCommand changeMetadataCommand;

    /*
     * DOC zqin GuessSchemaController constructor comment.
     *
     * @param dp
     */
    public GuessSchemaController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        FormData data;

        final Button btnCmd = new Button(subComposite, SWT.NONE);
        btnCmd.setText(GUESS_SCHEMA_NAME);

        data = new FormData();

        GC gc = new GC(btnCmd);
        Point labelSize = gc.stringExtent(GUESS_SCHEMA_NAME);
        gc.dispose();
        int currentLabelWidth = STANDARD_BUTTON_WIDTH;
        if ((labelSize.x + ITabbedPropertyConstants.HSPACE * 2) > STANDARD_BUTTON_WIDTH) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE * 2;
        }

        data.left = new FormAttachment(lastControl, 0);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT + 2;
        btnCmd.setLayoutData(data);
        btnCmd.setData(PARAMETER_NAME, param.getName());
        btnCmd.setData(NAME, SCHEMA);
        btnCmd.setData(SCHEMA, checkQuotes((String) param.getValue()));
        btnCmd.setEnabled(!param.isReadOnly());
        btnCmd.addSelectionListener(listenerSelection);

        return btnCmd;
    }

    private String checkQuotes(final String str) {
        if (str == null || "".equals(str)) { //$NON-NLS-1$
            return TalendTextUtils.addQuotes(str);
        }

        return str;
    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
        public void widgetSelected(SelectionEvent e) {

            Command cmd = null;

            if (part == null) {
                cmd = createButtonCommand((Button) e.getSource(), new EmptyContextManager());
            } else {
                cmd = createButtonCommand((Button) e.getSource(), part.getProcess().getContextManager());
            }

            executeCommand(cmd);
        }

    };

    /**
     * This method is used for "Guess Query" button.
     *
     * @return
     */

    // modified by hyWang
    public Command createButtonCommand(Button btn, IContextManager manager) {
        this.btn = btn;
        IElementParameter elementParameterFromField = elem.getElementParameterFromField(EParameterFieldType.MEMO_SQL);

        memoSQL = (String) elementParameterFromField.getValue();
        initConnectionParameters();
        if (this.connParameters != null && memoSQL != null) {
            initConnectionParametersWithContext(elem, manager.getDefaultContext());
            // runShadowProcess();
            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                useMockJob();

            } else if (LanguageManager.getCurrentLanguage() == ECodeLanguage.PERL) {
                IElementParameter switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
                memoSQL = memoSQL.substring(1, memoSQL.length() - 1).trim().replace("\\'", "\'"); //$NON-NLS-1$ //$NON-NLS-2$
                runShadowProcessForPerl();

                if (columns != null && columns.size() > 0) {
                    Node node = (Node) elem;
                    IMetadataTable tempMetatable = new MetadataTable();
                    IMetadataTable inputMetaCopy, inputMetadata, outputMetaCopy, originaleOutputTable;

                    String propertyName = (String) btn.getData(PARAMETER_NAME);
                    IElementParameter param = node.getElementParameter(propertyName);
                    for (IElementParameter eParam : elem.getElementParameters()) {
                        if (eParam.getContext() != null) {
                            param = eParam;
                        }
                    }
                    originaleOutputTable = node.getMetadataFromConnector(param.getContext());
                    if (originaleOutputTable != null) {
                        outputMetaCopy = originaleOutputTable.clone();
                    }

                    tempMetatable.setListColumns(columns);

                    MetadataDialog metaDialog = new MetadataDialog(composite.getShell(), tempMetatable, node, getCommandStack());
                    if (metaDialog != null) {
                        metaDialog.setText(Messages.getString("AbstractSchemaController.schema.title", node.getLabel())); //$NON-NLS-1$

                        if (metaDialog.open() == MetadataDialog.OK) {
                            outputMetaCopy = metaDialog.getOutputMetaData();
                            boolean modified = false;
                            if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
                                modified = true;
                            }

                            if (modified) {
                                if (switchParam != null) {
                                    switchParam.setValue(Boolean.FALSE);
                                }

                                ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, param,
                                        originaleOutputTable, outputMetaCopy);

                                return changeMetadataCommand;

                            }
                        }
                    }
                }
            }

            if (changeMetadataCommand != null) {
                return changeMetadataCommand;
            }

        }

        // if (columns != null && columns.size() > 0) {
        // Node node = (Node) elem;
        // IMetadataTable tempMetatable = new MetadataTable();
        // IMetadataTable inputMetaCopy, inputMetadata, outputMetaCopy, originaleOutputTable;
        //
        // String propertyName = (String) btn.getData(PARAMETER_NAME);
        // IElementParameter param = node.getElementParameter(propertyName);
        // for (IElementParameter eParam : elem.getElementParameters()) {
        // if (eParam.getContext() != null) {
        // param = eParam;
        // }
        // }
        // originaleOutputTable = node.getMetadataFromConnector(param.getContext());
        // if (originaleOutputTable != null) {
        // outputMetaCopy = originaleOutputTable.clone();
        // }
        //
        // tempMetatable.setListColumns(columns);
        //
        // MetadataDialog metaDialog = new MetadataDialog(composite.getShell(), tempMetatable, node, getCommandStack());
        // if (metaDialog != null) {
        //                metaDialog.setText(Messages.getString("SchemaController.schemaOf") + node.getLabel()); //$NON-NLS-1$
        //
        // if (metaDialog.open() == MetadataDialog.OK) {
        // outputMetaCopy = metaDialog.getOutputMetaData();
        // boolean modified = false;
        // if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
        // modified = true;
        // }
        //
        // if (modified) {
        // if (switchParam != null) {
        // switchParam.setValue(Boolean.FALSE);
        // }
        //
        // ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, param,
        // originaleOutputTable, outputMetaCopy);
        //
        // return changeMetadataCommand;
        //
        // }
        // }
        // }
        // }

        return null;
    }

    private String memoSQL = null;

    private List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();

    private void runShadowProcess(final Property property, final Node inputNode, final IContext selectContext,
            final IElementParameter switchParam) {
        DatabaseConnection connt = TracesConnectionUtils.createConnection(connParameters);
        String dbmsId = connt.getDbmsId();
        if (dbmsId == null) {
            Shell shell = Display.getCurrent().getActiveShell();
            MessageDialog.openError(shell, "No info about DB found !",
                    "Please choose the correct mapping file.\n Note: This is normal when using JDBC component");
            MappingFileSelectDialog dialog = new MappingFileSelectDialog(shell);
            dialog.open();
            dbmsId = dialog.getSelectId();
        }

        GuessSchemaProcess gsp = new GuessSchemaProcess(property, inputNode, selectContext, memoSQL, info, part.getProcess());
        try {
            List<Integer> indexsForSameNamedColumn = new ArrayList<Integer>();
            CsvArray array = gsp.run();
            List<String[]> schemaContent = array.getRows();
            List<String> columnLabels = new ArrayList<String>();
            if (columns != null) {
                columns.clear();
            }
            if (!schemaContent.isEmpty()) {
                int numbOfColumn = schemaContent.get(0).length;

                for (int i = 1; i <= numbOfColumn; i++) {
                    indexsForSameNamedColumn.clear();
                    Boolean b = false;
                    IMetadataColumn oneColum = new MetadataColumn();
                    // get the column name from the temp file genenrated by GuessSchemaProcess.java
                    String labelName = (schemaContent.get(0))[i - 1];
                    if (info != null && info.isHive()) {
                        if (labelName != null && labelName.indexOf(TalendTextUtils.JAVA_END_STRING) != -1) {
                            labelName = labelName.substring(labelName.lastIndexOf(TalendTextUtils.JAVA_END_STRING) + 1,
                                    labelName.length());
                        }
                    }
                    String name = labelName;
                    String sub = "";
                    String sub2 = "";
                    if (labelName != null && labelName.length() > 0 && labelName.startsWith("_")) { //$NON-NLS-1$
                        sub = labelName.substring(1);
                        if (sub != null && sub.length() > 0) {
                            sub2 = sub.substring(1);
                        }
                    }
                    if (KeywordsValidator.isKeyword(labelName) || KeywordsValidator.isKeyword(sub)
                            || KeywordsValidator.isKeyword(sub2)) {
                        labelName = "_" + labelName;
                        b = true;
                    }
                    // String label = labelName;
                    // findSameNamedColumnAndReplaceTheIndex(indexsForSameNamedColumn, i, oneColum, labelName);
                    oneColum.setLabel(MetadataToolHelper.validateColumnName(labelName, i, columnLabels));
                    //                    if (b && label != null && label.length() > 0 && label.startsWith("_")) { //$NON-NLS-1$
                    // String substring = label.substring(1);
                    // if (label.startsWith("_")
                    // && (KeywordsValidator.isKeyword(substring) || KeywordsValidator.isKeyword(sub) ||
                    // KeywordsValidator
                    // .isKeyword(sub2))) {
                    // label = substring;
                    // }
                    // }
                    oneColum.setOriginalDbColumnName(name);
                    if (schemaContent.size() > 5) {
                        oneColum.setPrecision(Integer.parseInt(schemaContent.get(2)[i - 1]));
                        oneColum.setLength(Integer.parseInt(schemaContent.get(3)[i - 1]));
                    }
                    try {
                        String talendType = null;
                        // to see if the language is java or perl
                        if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                            if (schemaContent.size() >= 5) {
                                talendType = MetadataTalendType.getMappingTypeRetriever(dbmsId).getDefaultSelectedTalendType(
                                        schemaContent.get(4)[i - 1]);
                            } else {
                                talendType = JavaTypesManager.STRING.getId();
                            }
                        } else {
                            if (schemaContent.size() >= 5) {
                                talendType = PerlDataTypeHelper.getNewTalendTypeOfValue(schemaContent.get(4)[i - 1]);
                            } else {
                                talendType = PerlTypesManager.STRING;
                            }
                        }
                        oneColum.setTalendType(talendType);
                        if (dbmsId != null) {
                            if (!TypesManager.checkDBType(dbmsId, oneColum.getTalendType(), oneColum.getType())) {
                                String dbType = MetadataConnectionUtils.getDBType(schemaContent.get(4)[i - 1], dbmsId);
                                if(dbType == null || dbType.length() <= 0){
                                    dbType = TypesManager.getDBTypeFromTalendType(dbmsId, oneColum.getTalendType());
                                }
                                oneColum.setType(dbType);
                            }
                        }
                        // oneColum.setTalendType(JavaTypesManager.STRING.getId());

                    } catch (Exception e) {
                        /*
                         * the table have no data at all ,to do nothing
                         */
                        ExceptionHandler.process(e);
                    }
                    // get if a column is nullable from the temp file genenrated by GuessSchemaProcess.java
                    oneColum.setNullable((schemaContent.get(1))[i - 1].equals(Boolean.TRUE.toString()) ? true : false);
                    columns.add(oneColum);
                    columnLabels.add(oneColum.getLabel());
                }

                if (columns.size() > 0) {
                    IElementParameter dqRule = elem.getElementParameter("DQRULES_LIST");
                    if (dqRule != null) {
                        ITDQRuleService ruleService = null;
                        try {
                            ruleService = (ITDQRuleService) GlobalServiceRegister.getDefault().getService(ITDQRuleService.class);
                        } catch (RuntimeException e) {
                            // nothing to do
                        }
                        IElementParameter queryParam = elem.getElementParameter("QUERY");
                        if (ruleService != null && queryParam != null) {
                            ruleService.updateOriginalColumnNames(columns, queryParam);
                        }
                    }
                }

                IMetadataTable tempMetatable = new MetadataTable();
                /* for bug 20973 */
                if (tempMetatable.getTableName() == null) {
                    tempMetatable.setTableName(inputNode.getUniqueName());
                }
                IMetadataTable outputMetaCopy, originaleOutputTable;
                String propertyName = "";
                if (!btn.isDisposed()) {
                    propertyName = (String) btn.getData(PARAMETER_NAME);
                }
                IElementParameter param = inputNode.getElementParameter(propertyName);
                for (IElementParameter eParam : elem.getElementParameters()) {
                    if (eParam.getContext() != null) {
                        param = eParam;
                    }
                }
                originaleOutputTable = inputNode.getMetadataFromConnector(param.getContext());
                if (originaleOutputTable != null) {
                    outputMetaCopy = originaleOutputTable.clone();
                }

                tempMetatable.setListColumns(columns);
                tempMetatable.setDbms(dbmsId);
                MetadataDialog metaDialog = new MetadataDialog(composite.getShell(), tempMetatable, inputNode, getCommandStack());
                if (metaDialog != null) {
                    metaDialog.setText(Messages.getString("AbstractSchemaController.schema.title", inputNode.getLabel())); //$NON-NLS-1$
                }

                // ok pressed
                if (metaDialog.open() == MetadataDialog.OK) {
                    outputMetaCopy = metaDialog.getOutputMetaData();
                    boolean modified = false;
                    if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
                        modified = true;
                    }

                    if (modified) {
                        if (switchParam != null) {
                            switchParam.setValue(Boolean.FALSE);
                        }

                        changeMetadataCommand = new ChangeMetadataCommand(inputNode, param, originaleOutputTable, outputMetaCopy);
                        changeMetadataCommand.execute();
                    }
                }
            }
        } catch (ProcessorException e) {
            ExtractMetaDataUtils.getInstance().closeConnection();
            final String strExcepton = e.getMessage();
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    MessageDialog.openWarning(composite.getShell(),
                            Messages.getString("GuessSchemaController.connectionWarning"), strExcepton); //$NON-NLS-1$
                }
            });
            ExceptionHandler.process(e);
        }

    }

    private void findSameNamedColumnAndReplaceTheIndex(List<Integer> indexsForSameNamedColumn, int i, IMetadataColumn oneColum,
            String labelName) {
        boolean findSameNameColumn = false;
        boolean hasMax = false;
        for (IMetadataColumn exsitingOneColumn : columns) {
            boolean hasIndex = false;
            String[] allseg = exsitingOneColumn.getLabel().split("_");
            String name = "";
            String priorIndex = allseg[allseg.length - 1];
            if (isNumeric(priorIndex)) {
                hasIndex = true;
                for (int j = 0; j < allseg.length - 1; j++) {
                    if (j != allseg.length - 2) {
                        name = name + allseg[j] + "_";
                    } else {
                        name = name + allseg[j];
                    }
                }
            }
            if (exsitingOneColumn.getLabel() != null && exsitingOneColumn.getLabel().split("_").length > 1 && hasIndex) {
                if (name.equals(MetadataToolHelper.validateColumnName(labelName, i))) {
                    findSameNameColumn = true;
                    indexsForSameNamedColumn.add(Integer.parseInt(priorIndex));
                }
            } else if (exsitingOneColumn.getLabel().equals(MetadataToolHelper.validateColumnName(labelName, i))) {
                findSameNameColumn = true;
            }
        }
        Integer[] indexsarray = indexsForSameNamedColumn.toArray(new Integer[0]);
        if (indexsarray.length > 0) {
            Arrays.sort(indexsarray);
            hasMax = true;
        }
        if (findSameNameColumn && hasMax) {
            int nextIndex = ++indexsarray[indexsarray.length - 1];
            oneColum.setLabel(MetadataToolHelper.validateColumnName(labelName + "_" + Integer.toString(nextIndex), i));
        } else if (findSameNameColumn) {
            oneColum.setLabel(MetadataToolHelper.validateColumnName(labelName + "_" + Integer.toString(1), i));
        } else {
            oneColum.setLabel(MetadataToolHelper.validateColumnName(labelName, i));
        }
    }

    /**
     * DOC ocarbone Comment method "refreshMetaData".
     *
     * @param csvArray
     */
    public void refreshMetaDataTable(ResultSetMetaData rsmd, final List<String[]> csvRows) throws SQLException {

        if (csvRows == null) {
            return;
        } else {

            List<String> allNames = new ArrayList<String>();

            if (csvRows.isEmpty()) {
                int numbOfColumn = rsmd.getColumnCount();
                for (int i = 1; i <= numbOfColumn; i++) {
                    IMetadataColumn oneColum = new MetadataColumn();
                    String labelName = rsmd.getColumnLabel(i);
                    labelName = ColumnNameValidator.validateColumnNameFormat(labelName, i);
                    oneColum.setLabel(getNextGeneratedColumnName(labelName, allNames));
                    oneColum.setOriginalDbColumnName(rsmd.getColumnName(i));
                    oneColum.setNullable(rsmd.isNullable(i) == 0 ? false : true);
                    oneColum.setTalendType(JavaTypesManager.STRING.getId());
                    columns.add(oneColum);
                }
                return;
            }
            String[] fields = csvRows.get(0);
            Integer numberOfCol = getRightFirstRow(csvRows);

            // define the label to the metadata width the content of the first row
            int firstRowToExtractMetadata = 0;

            for (int i = 0; i < numberOfCol.intValue(); i++) {
                // define the first currentType and assimile it to globalType
                String globalType = null;
                int lengthValue = 0;
                int precisionValue = 0;

                int current = firstRowToExtractMetadata;

                while (globalType == null && current < csvRows.size()) {

                    if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                        if (i >= csvRows.get(current).length) {
                            globalType = "id_String"; //$NON-NLS-1$
                        } else {
                            globalType = JavaDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                        }
                    } else {
                        if (i >= csvRows.get(current).length) {
                            globalType = "String"; //$NON-NLS-1$
                        } else {
                            // globalType = PerlDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            globalType = PerlDataTypeHelper.getNewTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                        }
                    }
                }

                // for another lines
                for (int f = firstRowToExtractMetadata; f < csvRows.size(); f++) {
                    fields = csvRows.get(f);
                    if (fields.length > i) {
                        String value = fields[i];
                        if (value != null && !value.equals("")) { //$NON-NLS-1$
                            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                                if (!JavaDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                    globalType = JavaDataTypeHelper.getCommonType(globalType,
                                            JavaDataTypeHelper.getTalendTypeOfValue(value));
                                }
                            } else {
                                // if (!PerlDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                if (!PerlDataTypeHelper.getNewTalendTypeOfValue(value).equals(globalType)) {
                                    // globalType = PerlDataTypeHelper.getCommonType(globalType, PerlDataTypeHelper
                                    // .getTalendTypeOfValue(value));
                                    globalType = PerlDataTypeHelper.getNewCommonType(globalType,
                                            PerlDataTypeHelper.getNewTalendTypeOfValue(value));
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
                            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                                globalType = JavaTypesManager.STRING.getId();
                            } else {
                                globalType = PerlTypesManager.STRING;
                            }
                        }
                    }
                }
                IMetadataColumn oneColum = new MetadataColumn();
                // Convert javaType to TalendType
                String talendType = null;
                if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                    talendType = globalType;
                    if (globalType.equals(JavaTypesManager.FLOAT.getId()) || globalType.equals(JavaTypesManager.DOUBLE.getId())) {
                        oneColum.setPrecision(precisionValue);
                    } else {
                        oneColum.setPrecision(0);
                    }
                } else {
                    talendType = PerlTypesManager.getNewTypeName(MetadataTalendType.loadTalendType(globalType,
                            "TALENDDEFAULT", false)); //$NON-NLS-1$
                    if (globalType.equals("FLOAT") || globalType.equals("DOUBLE")) { //$NON-NLS-1$ //$NON-NLS-2$
                        oneColum.setPrecision(precisionValue);
                    } else {
                        oneColum.setPrecision(0);
                    }
                }

                String labelName = rsmd.getColumnLabel(i + 1);
                labelName = ColumnNameValidator.validateColumnNameFormat(labelName, i);

                oneColum.setTalendType(talendType);
                oneColum.setLength(lengthValue);

                oneColum.setLabel(getNextGeneratedColumnName(labelName, allNames));
                oneColum.setOriginalDbColumnName(rsmd.getColumnName(i + 1));
                oneColum.setNullable(rsmd.isNullable(i + 1) == 0 ? false : true);

                columns.add(oneColum);

            }
        }
    }

    private String getNextGeneratedColumnName(String oldName, List<String> allNames) {

        String uniqueString;

        UniqueStringGenerator<String> uniqueStringGenerator = new UniqueStringGenerator<String>(oldName, allNames) {

            /*
             * (non-Javadoc)
             *
             * @see org.talend.commons.utils.data.list.UniqueStringGenerator#getBeanString(java.lang.Object)
             */
            @Override
            protected String getBeanString(String bean) {
                return bean;
            }

        };

        uniqueString = uniqueStringGenerator.getUniqueString();

        allNames.add(oldName);

        return uniqueString;
    }

    // CALCULATE THE NULBER OF COLUMNS IN THE PREVIEW
    private Integer getRightFirstRow(List<String[]> csvRows) {

        Integer numbersOfColumns = null;
        int parserLine = csvRows.size();
        if (parserLine > 50) {
            parserLine = 50;
        }
        for (int i = 0; i < parserLine; i++) {
            if (csvRows.get(i) != null) {
                String[] nbRow = csvRows.get(i);
                // List<XmlField> nbRowFields = nbRow.getFields();
                if (numbersOfColumns == null || nbRow.length >= numbersOfColumns) {
                    numbersOfColumns = nbRow.length;
                }
            }
        }
        return numbersOfColumns;
    }

    private void useMockJob() {
        /*
         * get the select node,it's the input node of the process. then transfer selected context varriable to
         * openContextChooseDialog, added by hyWang
         */
        final IElementParameter switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());

        final Shell parentShell = this.composite.getShell();
        final Node inputNode = (Node) this.curParameter.getElement();
        if (connParameters == null) {
            initConnectionParameters();
        }
        final String tmpMemoSql = this.memoSQL;
        final DatabaseConnection connt = TracesConnectionUtils.createConnection(connParameters);
        IMetadataConnection iMetadataConnection = null;
        boolean isStatus = false;
        try {
            if (connt != null) {
                iMetadataConnection = ConvertionHelper.convert(connt);
                iMetadataConnection.setAdditionalParams(ConvertionHelper.convertAdditionalParameters(connt));
                isStatus = checkConnection(iMetadataConnection);
            }

            if (isStatus) {
                if (EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(iMetadataConnection.getDbType())) {
                    info = new DbInfo(iMetadataConnection.getDbType(), iMetadataConnection.getUsername(),
                            iMetadataConnection.getPassword(), iMetadataConnection.getDbVersionString(),
                            iMetadataConnection.getUrl(), iMetadataConnection.getDriverClass(),
                            iMetadataConnection.getDriverJarPath(), iMetadataConnection.getAdditionalParams());
                } else if (EDatabaseTypeName.HIVE.getDisplayName().equals(iMetadataConnection.getDbType())) {
                    String jobTracker = TalendTextUtils.removeQuotes(String.valueOf(iMetadataConnection
                            .getParameter(ConnParameterKeys.CONN_PARA_KEY_JOB_TRACKER_URL)));
                    String nameNode = TalendTextUtils.removeQuotes(String.valueOf(iMetadataConnection
                            .getParameter(ConnParameterKeys.CONN_PARA_KEY_NAME_NODE_URL)));
                    String thrifturi = null;
                    if (HiveModeInfo.get(iMetadataConnection.getDbVersionString()) == HiveModeInfo.EMBEDDED) {
                        thrifturi = "thrift://" + iMetadataConnection.getServerName() + ":" + iMetadataConnection.getPort();
                    }
                    info = new DbInfo(iMetadataConnection.getDbType(), iMetadataConnection.getUsername(),
                            iMetadataConnection.getPassword(), iMetadataConnection.getDbVersionString(),
                            iMetadataConnection.getUrl(), jobTracker, nameNode, thrifturi, iMetadataConnection.getDriverJarPath());
                    String hiveServerVersion = String.valueOf(iMetadataConnection
                            .getParameter(ConnParameterKeys.HIVE_SERVER_VERSION));
                    String driverClass = ""; //$NON-NLS-1$
                    if (HiveServerVersionInfo.HIVE_SERVER_2.getKey().equals(hiveServerVersion)) {
                        driverClass = EDatabase4DriverClassName.HIVE2.getDriverClass();
                    } else {
                        driverClass = EDatabase4DriverClassName.HIVE.getDriverClass();
                    }
                    info.setDriverClassName(driverClass);
                } else if (EDatabaseTypeName.REDSHIFT_SSO.getDisplayName().equals(iMetadataConnection.getDbType())) {
                	info = new DbInfo(iMetadataConnection.getDbType(), iMetadataConnection.getUsername(),
                            iMetadataConnection.getPassword(), iMetadataConnection.getDbVersionString(),
                            iMetadataConnection.getUrl(), iMetadataConnection.getDriverJarPath(),iMetadataConnection.getAdditionalParams());
                } else if (EDatabaseTypeName.ORACLE_CUSTOM.getDisplayName().equals(iMetadataConnection.getDbType())) {
                    info = new DbInfo(iMetadataConnection.getDbType(), iMetadataConnection.getUsername(),
                            iMetadataConnection.getPassword(), iMetadataConnection.getDbVersionString(),
                            iMetadataConnection.getUrl(), iMetadataConnection.getDriverJarPath(),
                            iMetadataConnection.getAdditionalParams());
                }else {
                    info = new DbInfo(iMetadataConnection.getDbType(), iMetadataConnection.getUsername(),
                            iMetadataConnection.getPassword(), iMetadataConnection.getDbVersionString(),
                            iMetadataConnection.getUrl(), iMetadataConnection.getDriverJarPath());
                }

                final Property property = GuessSchemaProcess.getNewmockProperty();
                List<IContext> allcontexts = inputNode.getProcess().getContextManager().getListContext();

                OpenContextChooseComboDialog dialog = new OpenContextChooseComboDialog(parentShell, allcontexts);
                dialog.create();
                dialog.getShell().setText(CONTEXT_CHOOSE_DIALOG_TITLE);
                IContext selectContext = null;
                // job only have defoult context,or the query isn't context mode
                if (allcontexts.size() == 1 || TalendQuoteUtils.isCommonString(tmpMemoSql)) {
                    selectContext = inputNode.getProcess().getContextManager().getDefaultContext();
                } else if (Window.OK == dialog.open()) {
                    selectContext = dialog.getSelectedContext();
                }
                final IContext context = selectContext;
                if (context != null) {
                    //
                    final ProgressMonitorDialog pmd = new ProgressMonitorDialog(this.composite.getShell());

                    pmd.run(true, true, new IRunnableWithProgress() {

                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    runShadowProcess(property, inputNode, context, switchParam);
                                }
                            });
                        }

                    });
                }
            } else {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        String pid = "org.talend.sqlbuilder"; //$NON-NLS-1$
                        String mainMsg = Messages.getString("GuessSchemaController.connectionFailed"); //$NON-NLS-1$
                        ErrorDialogWithDetailAreaAndContinueButton dialog = new ErrorDialogWithDetailAreaAndContinueButton(
                                composite.getShell(), pid, mainMsg, connParameters.getConnectionComment());
                        if (dialog.getCodeOfButton() == Window.OK) {
                            openParamemerDialog(composite.getShell(), part.getProcess().getContextManager());
                        }
                    }
                });
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        // else {
        // try {
        // pmd2.run(true, true, new IRunnableWithProgress() {

        // public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        // Display.getDefault().asyncExec(new Runnable() {
        //
        // public void run() {
        // String pid = SqlBuilderPlugin.PLUGIN_ID;
        //                    String mainMsg = Messages.getString("GuessSchemaController.connectionFailed"); //$NON-NLS-1$
        // ErrorDialogWithDetailAreaAndContinueButton dialog = new ErrorDialogWithDetailAreaAndContinueButton(composite
        // .getShell(), pid, mainMsg, connParameters.getConnectionComment());
        // if (dialog.getCodeOfButton() == Window.OK) {
        // openParamemerDialog(composite.getShell(), part.getTalendEditor().getProcess().getContextManager());
        // }
        // }
        // });
        // }
        // });
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
    }

    private void openParamemerDialog(Shell shell, IContextManager manager) {
        initConnectionParameters();
        if (connParameters != null) {
            ConfigureConnParamDialog paramDialog = new ConfigureConnParamDialog(shell, connParameters, manager, elem);
            if (paramDialog.open() == Window.OK) {
                // runShadowProcess();
                useMockJob();
            }
        } else {
            MessageDialog
                    .openWarning(
                            shell,
                            Messages.getString("GuessSchemaController.connectionError"), Messages.getString("GuessSchemaController.setParameter")); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    /**
     * qzhang Comment method "checkConnection".
     *
     * @param metadataConnection
     */
    protected boolean checkConnection(IMetadataConnection metadataConnection) {
        try {
            if (EDatabaseTypeName.HIVE.getDisplayName().equals(metadataConnection.getDbType())) {
                ConnectionStatus connectionStatus = new ConnectionStatus();
                connectionStatus.setResult(false);
                try {
                    if (HiveModeInfo.get(metadataConnection.getDbVersionString()) == HiveModeInfo.EMBEDDED) {
                        JavaSqlFactory.doHivePreSetup((DatabaseConnection) metadataConnection.getCurrentConnection());
                    }
                    HiveConnectionManager.getInstance().checkConnection(metadataConnection);
                    connectionStatus.setResult(true);
                } catch (Exception e) {
                    log.error("" + "\n" + e.toString()); //$NON-NLS-1$//$NON-NLS-2$
                }
                return connectionStatus.getResult();
            }
            if (EDatabaseTypeName.IMPALA.getDisplayName().equals(metadataConnection.getDbType())) {
                ConnectionStatus connectionStatus = new ConnectionStatus();
                connectionStatus.setResult(false);
                try {
                    ImpalaConnectionManager.getInstance().checkConnection(metadataConnection);
                    connectionStatus.setResult(true);
                } catch (Exception e) {
                    log.error("" + "\n" + e.toString()); //$NON-NLS-1$//$NON-NLS-2$
                }
                return connectionStatus.getResult();
            }

            ConnectionStatus testConnection = ExtractMetaDataFromDataBase.testConnection(metadataConnection.getDbType(),
                    metadataConnection.getUrl(), metadataConnection.getUsername(), metadataConnection.getPassword(),
                    metadataConnection.getSchema(), metadataConnection.getDriverClass(), metadataConnection.getDriverJarPath(),metadataConnection.getDriverJarUri(),
                    metadataConnection.getDbVersionString(), metadataConnection.getAdditionalParams());
            connParameters.setConnectionComment(testConnection.getMessageException());
            return testConnection.getResult();
        } catch (Exception e) {
            log.error("" + "\n" + e.toString()); //$NON-NLS-1$//$NON-NLS-2$
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#
     * estimateRowSize (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org
     * .talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    private void runShadowProcessForPerl() {
        final ProgressMonitorDialog pmd = new ProgressMonitorDialog(this.composite.getShell());

        try {
            pmd.run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    if (columns != null) {
                        columns.clear();
                    }
                    monitor.beginTask(Messages.getString("GuessSchemaController.waitOpenDatabase"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    if (connParameters == null) {
                        initConnectionParameters();
                    }

                    ISQLBuilderService service = (ISQLBuilderService) GlobalServiceRegister.getDefault().getService(
                            ISQLBuilderService.class);
                    DatabaseConnection connt = service.createConnection(connParameters);
                    IMetadataConnection iMetadataConnection = null;
                    boolean isStatus = false;
                    ExtractMetaDataUtils extractMeta = ExtractMetaDataUtils.getInstance();
                    if (connt != null) {
                        iMetadataConnection = ConvertionHelper.convert(connt);
                        isStatus = checkConnection(iMetadataConnection);
                    }
                    if (!monitor.isCanceled()) {
                        try {
                            if (isStatus) {
                                extractMeta.getConnection(iMetadataConnection.getDbType(), iMetadataConnection.getUrl(),
                                        iMetadataConnection.getUsername(), iMetadataConnection.getPassword(),
                                        iMetadataConnection.getDatabase(), iMetadataConnection.getSchema(),
                                        iMetadataConnection.getDriverClass(), iMetadataConnection.getDriverJarPath(),iMetadataConnection.getDriverJarUri(),
                                        iMetadataConnection.getDbVersionString(), iMetadataConnection.getAdditionalParams());
                                if (extractMeta.getConn() != null) {
                                    Statement smst = extractMeta.getConn().createStatement();
                                    extractMeta.setQueryStatementTimeout(smst);
                                    ResultSet rs = smst.executeQuery(memoSQL);
                                    ResultSetMetaData rsmd = rs.getMetaData();
                                    int numbOfColumn = rsmd.getColumnCount();

                                    int count = 0;
                                    List<String[]> cvsArrays = new ArrayList<String[]>();
                                    while (rs.next() && count < 50) {

                                        String[] dataOneRow = new String[numbOfColumn];
                                        for (int i = 1; i <= numbOfColumn; i++) {
                                            String tempStr = rs.getString(i);
                                            dataOneRow[i - 1] = tempStr;
                                        }

                                        cvsArrays.add(dataOneRow);
                                        count++;
                                    }

                                    refreshMetaDataTable(rsmd, cvsArrays);

                                    extractMeta.closeConnection();
                                }
                            } else {
                                Display.getDefault().asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        String pid = "org.talend.sqlbuilder"; //$NON-NLS-1$
                                        String mainMsg = "Database connection is failed. "; //$NON-NLS-1$
                                        ErrorDialogWithDetailAreaAndContinueButton dialog = new ErrorDialogWithDetailAreaAndContinueButton(
                                                composite.getShell(), pid, mainMsg, connParameters.getConnectionComment());
                                        if (dialog.getCodeOfButton() == Window.OK) {
                                            openParamemerDialog(composite.getShell(), part.getProcess().getContextManager());
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            extractMeta.closeConnection();
                            ExceptionHandler.process(e);
                            final String strExcepton = "Connect to DB error ,or some errors in SQL query string, or 'Guess Schema' not compatible with current SQL query string."
                                    + System.getProperty("line.separator");
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    MessageDialog.openWarning(composite.getShell(),
                                            Messages.getString("GuessSchemaController.connError"), strExcepton); //$NON-NLS-1$
                                }
                            });
                        }
                    }

                }

            });
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    public static boolean isNumeric(String s) {
        if ((s != null) && (s != "")) {
            return s.matches("^[0-9]*$");
        } else {
            return false;
        }
    }

    public void setCurParameter(IElementParameter parameter){
        this.curParameter = parameter;
    }

}
