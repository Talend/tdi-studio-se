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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWithDetailAreaAndContinueButton;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.DatabaseConnStrUtil;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataFromDataBase;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.connection.ConnectionStatus;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.metadata.dialog.DbTableSelectorDialog;
import org.talend.core.ui.metadata.dialog.DbTableSelectorObject;
import org.talend.core.ui.metadata.dialog.DbTableSelectorObject.ObjectType;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.services.ISQLBuilderService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.TracesConnectionUtils;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.ConfigureConnParamDialog;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;
import org.talend.metadata.managment.connection.manager.HiveConnectionManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: TextController.java 1 2006-12-12 下午01:53:53 +0000 (下午01:53:53) yzhang $
 *
 */
public class DbTableController extends AbstractElementPropertySectionController {

    private static Logger log = Logger.getLogger(DbTableController.class);

    private List<String> returnTablesFormConnection = null;

    /**
     * DOC yzhang TextController constructor comment.
     *
     * @param dtp
     */
    public DbTableController(IDynamicProperty dp) {
        super(dp);
    }

    SelectionListener openTablesListener = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (part == null) {
                createListTablesCommand((Button) e.getSource(), new EmptyContextManager());
            } else {
                createListTablesCommand((Button) e.getSource(), part.getProcess().getContextManager());
            }
        }
    };

    SelectionListener openSQLListener = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (part == null) {
                createOpenSQLCommand((Button) e.getSource(), new EmptyContextManager());
            } else {
                createOpenSQLCommand((Button) e.getSource(), part.getProcess().getContextManager());
            }

        }
    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        FormData data;

        this.paramFieldType = param.getFieldType();
        this.curParameter = param;
        Control lastDbControl = null;
        Button openListTable = null;
        if (!"DATABASE:CDC".equals(param.getFilter())) { //$NON-NLS-1$
            openListTable = addListTablesButton(subComposite, param, top, numInRow, nbInRow);
            lastDbControl = openListTable;
        }
        boolean isHive = false;
        IElementParameter typePara = elem.getElementParameter("TYPE"); //$NON-NLS-1$
        if (typePara != null && "Hive".equalsIgnoreCase((String) typePara.getValue())) { //$NON-NLS-1$
            isHive = true;
        }

        Control openSqlBuilder = null;
        if (!isContainSqlMemo() && !"DATABASE:CDC".equals(param.getFilter()) && !isHive) { //$NON-NLS-1$
            openSqlBuilder = addOpenSqlBulderButton(subComposite, param, top, numInRow, nbInRow);
            FormData data1 = new FormData();
            data1.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            data1.left = new FormAttachment(100, -(ITabbedPropertyConstants.HSPACE + STANDARD_BUTTON_WIDTH));
            data1.top = new FormAttachment(0, top);
            data1.height = STANDARD_HEIGHT - 2;
            openSqlBuilder.setLayoutData(data1);
            openSqlBuilder.setToolTipText(Messages.getString("DbTableController.openSQLBuilder")); //$NON-NLS-1$
            lastDbControl = openSqlBuilder;
        }
        data = new FormData();
        if (openSqlBuilder != null) {
            data.right = new FormAttachment(openSqlBuilder, -5, SWT.LEFT);
            data.left = new FormAttachment(openSqlBuilder, -(15 + STANDARD_BUTTON_WIDTH), SWT.LEFT);
        } else {
            data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
            data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        }
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        if (openListTable != null) {
            openListTable.setLayoutData(data);
            openListTable.setData(PARAMETER_NAME, param.getName());
            openListTable.setEnabled(!param.isReadOnly());
            openListTable.addSelectionListener(openTablesListener);
            openListTable.setToolTipText(Messages.getString("DbTableController.showTableList")); //$NON-NLS-1$
        }

        // TDI-25576 : just hide this button in Component View for Hive components , maybe later will remove this .
        if (isHive) {
            openListTable.setVisible(false);
        }

        Text labelText;

        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("TextController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }
        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();

        labelText.setData(PARAMETER_NAME, param.getName());

        editionControlHelper.register(param.getName(), labelText);

        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(!param.isReadOnly());
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
        addDragAndDropTarget(labelText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();
        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        if (openListTable != null) {
            data.right = new FormAttachment(openListTable, -5, SWT.LEFT);
        } else {
            data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
            lastDbControl = labelText;
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************

        hashCurControls.put(param.getName(), labelText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        // curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return lastDbControl;
    }

    /**
     * qzhang Comment method "createOpenSQLCommand".
     *
     * @param button
     */
    protected void createOpenSQLCommand(Button button, IContextManager contextManager) {
        final Button btn = button;

        initConnectionParameters();
        if (this.connParameters != null) {
            if (isUseExistingConnection()) {
                initConnectionParametersWithContext(connectionNode, contextManager.getDefaultContext());
            } else {
                initConnectionParametersWithContext(elem, contextManager.getDefaultContext());
            }

            DatabaseConnection connection = getExistConnection();
            if (connection == null) {
                ISQLBuilderService service = (ISQLBuilderService) GlobalServiceRegister.getDefault().getService(
                        ISQLBuilderService.class);
                connection = service.createConnection(connParameters);
            }
            boolean isStatus = false;

            if (connection != null) {
                String contextId = connection.getContextId();
                if (contextId == null || "".equals(contextId)) {
                    IMetadataConnection metadataConnection = null;
                    metadataConnection = ConvertionHelper.convert(connection);
                    metadataConnection.setAdditionalParams(ConvertionHelper.convertAdditionalParameters(connection));
                    isStatus = checkConnection(metadataConnection);
                } else {
                    isStatus = true;
                }
            }

            if (isStatus) {
                openSQLBuilderWithParamer(button);
            } else {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        String pid = "org.talend.sqlbuilder"; //$NON-NLS-1$
                        String mainMsg = "Database connection is failed. "; //$NON-NLS-1$
                        ErrorDialogWithDetailAreaAndContinueButton dialog = new ErrorDialogWithDetailAreaAndContinueButton(
                                composite.getShell(), pid, mainMsg, connParameters.getConnectionComment());
                        if (dialog.getCodeOfButton() == Window.OK) {
                            openParamemerDialog(btn, part.getProcess().getContextManager());
                        }
                    }
                });
            }
        } else {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    String pid = "org.talend.sqlbuilder"; //$NON-NLS-1$
                    String mainMsg = "Database connection is failed. "; //$NON-NLS-1$
                    ErrorDialogWithDetailAreaAndContinueButton dialog = new ErrorDialogWithDetailAreaAndContinueButton(composite
                            .getShell(), pid, mainMsg, connParameters.getConnectionComment());
                    if (dialog.getCodeOfButton() == Window.OK) {
                        openParamemerDialog(btn, part.getProcess().getContextManager());
                    }
                }
            });

        }
    }

    /**
     * DOC zli Comment method "getExistConnection".
     *
     * @return
     */
    private DatabaseConnection getExistConnection() {
        String implicitRepositoryId = getImplicitRepositoryId();
        String statsLogPrositoryId = getStatsLogRepositoryId();
        if (implicitRepositoryId != null || statsLogPrositoryId != null) {
            // jobsetting view load the exist db info from current selected category
            String repId = null;
            if (EComponentCategory.EXTRA.equals(section)) {
                repId = implicitRepositoryId;

            } else if (EComponentCategory.STATSANDLOGS.equals(section)) {
                repId = statsLogPrositoryId;
            }
            if (repId != null) {
                IProxyRepositoryFactory proxyRepositoryFactory = DesignerPlugin.getDefault().getRepositoryService()
                        .getProxyRepositoryFactory();
                try {
                    IRepositoryViewObject lastVersion = proxyRepositoryFactory.getLastVersion(repId);
                    if (lastVersion != null) {
                        Item item = lastVersion.getProperty().getItem();
                        if (item instanceof DatabaseConnectionItem) {
                            DatabaseConnection connection = (DatabaseConnection) ((DatabaseConnectionItem) item).getConnection();
                            return connection;
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return null;
    }

    /**
     * DOC yexiaowei Comment method "openSQLBuilderWithParamer".
     *
     * @param button
     */
    private void openSQLBuilderWithParamer(Button button) {
        String repositoryType = null;
        if (this.curParameter != null) {
            IElementParameter propertyTypeParam = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE,
                    this.curParameter.getCategory());
            if (propertyTypeParam != null) {
                propertyTypeParam = propertyTypeParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName());
            }
            repositoryType = (String) propertyTypeParam.getValue();
        }
        String propertyName = (String) button.getData(PARAMETER_NAME);
        if (repositoryType != null) {
            openSQLBuilder(repositoryType, propertyName, ""); //$NON-NLS-1$
        }
    }

    /**
     * qzhang Comment method "isContainSqlMemo".
     *
     * @return
     */
    private boolean isContainSqlMemo() {
        IElementParameter elementParameterFromField = elem.getElementParameterFromField(EParameterFieldType.MEMO_SQL);
        return elementParameterFromField != null;
    }

    /**
     * qzhang Comment method "addOpenSqlBulderButton".
     *
     * @param subComposite
     * @param param
     * @param top
     * @param numInRow
     * @param nbInRow
     * @return
     */
    private Control addOpenSqlBulderButton(Composite subComposite, IElementParameter param, int top, int numInRow, int nbInRow) {
        final DecoratedField dField1 = new DecoratedField(subComposite, SWT.PUSH, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return new Button(parent, style);
            }
        });

        Control buttonControl = dField1.getLayoutControl();

        Button openSQLEditorButton = (Button) dField1.getControl();
        openSQLEditorButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        openSQLEditorButton.setImage(ImageProvider.getImage(ImageProvider.getImageDesc(EImage.READ_ICON)));
        buttonControl.setBackground(subComposite.getBackground());
        openSQLEditorButton.setEnabled(!param.isReadOnly());
        openSQLEditorButton.setData(NAME, SQLEDITOR);
        openSQLEditorButton.setData(PARAMETER_NAME, param.getName());
        if (param.getFieldType() == EParameterFieldType.DBTABLE) {
            openSQLEditorButton.setEnabled(ExtractMetaDataUtils.getInstance().haveLoadMetadataNode());
        }
        openSQLEditorButton.addSelectionListener(openSQLListener);

        return buttonControl;
    }

    /**
     * qzhang Comment method "addListTablesButton".
     *
     * @param subComposite
     * @param param
     * @param top
     * @return
     */
    private Button addListTablesButton(final Composite subComposite, final IElementParameter param, final int top, int numInRow,
            final int nbInRow) {

        Button openListTable = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        openListTable.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        openListTable.setData(PARAMETER_NAME, param.getName());
        return openListTable;
    }

    /**
     * qzhang Comment method "createCommand".
     *
     * @param button
     *
     * @return
     */
    protected void createListTablesCommand(Button button, IContextManager manager) {
        button.setEnabled(false);
        contextManager = manager;
        initConnectionParameters();
        if (this.connParameters != null) {
            if (isUseExistingConnection()) {
                initConnectionParametersWithContext(connectionNode, manager.getDefaultContext());
                if (isUseAlternateSchema()) {
                    initAlternateSchema(elem, manager.getDefaultContext());
                }
            } else {
                initConnectionParametersWithContext(elem, manager.getDefaultContext());
            }
            openDbTableSelectorJob(button);
        } else {
            MessageDialog
                    .openWarning(
                            button.getShell(),
                            Messages.getString("DbTableController.connectionError"), Messages.getString("DbTableController.setParameter")); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    /**
     * qzhang Comment method "openDbTableSelectorJob".
     *
     * @param openListTable
     */
    private void openDbTableSelectorJob(final Button openListTable) {
        Job job = new Job(Messages.getString("DbTableController.openSelectionDialog")) { //$NON-NLS-1$

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                monitor.beginTask(Messages.getString("DbTableController.waitingForOpen"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                DatabaseConnection existConnection = getExistConnection();
                if (existConnection == null) {
                    if (connParameters == null) {
                        initConnectionParameters();
                    }
                    existConnection = TracesConnectionUtils.createConnection(connParameters);
                }
                final DatabaseConnection con = existConnection;
                IMetadataConnection iMetadataConnection = null;
                final IMetadataConnection[] iMetadata = new IMetadataConnection[1];
                boolean isStatus = false;
                if (existConnection != null) {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            IMetadataConnection convert = ConvertionHelper.convert(con);
                            iMetadata[0] = convert;
                        }
                    });
                    iMetadataConnection = iMetadata[0];
                    // Added by Marvin Wang for bug TDI-24288.
                    if (EDatabaseTypeName.HIVE.getProduct().equalsIgnoreCase(con.getDatabaseType())) {
                        if (EDatabaseVersion4Drivers.HIVE_EMBEDDED.getVersionValue().equalsIgnoreCase(con.getDbVersionString())) {
                            try {
                                HiveConnectionManager.getInstance().checkConnection(iMetadataConnection);
                                isStatus = true;
                            } catch (ClassNotFoundException e) {
                                isStatus = false;
                                ExceptionHandler.process(e);
                            } catch (InstantiationException e) {
                                isStatus = false;
                                ExceptionHandler.process(e);
                            } catch (IllegalAccessException e) {
                                isStatus = false;
                                ExceptionHandler.process(e);
                            } catch (SQLException e) {
                                isStatus = false;
                                ExceptionHandler.process(e);
                            }
                        }
                    } else {
                        iMetadataConnection.setAdditionalParams(ConvertionHelper.convertAdditionalParameters(existConnection));
                        isStatus = checkConnection(iMetadataConnection);
                    }
                }
                String type = null;
                if (iMetadataConnection != null) {
                    type = iMetadataConnection.getDbType();
                }
                final String dbType = type;
                if (!monitor.isCanceled()) {

                    try {
                        if (isStatus) {
                            // Added by Marvin Wang for bug TDI-24288.
                            if (EDatabaseTypeName.HIVE.getProduct().equalsIgnoreCase(con.getDatabaseType())) {
                                if (EDatabaseVersion4Drivers.HIVE_EMBEDDED.getVersionValue().equalsIgnoreCase(
                                        con.getDbVersionString())) {
                                    JavaSqlFactory
                                            .doHivePreSetup((DatabaseConnection) iMetadataConnection.getCurrentConnection());
                                    returnTablesFormConnection = ExtractMetaDataFromDataBase
                                            .fetchAllTablesForHiveEmbeddedModel(iMetadataConnection);
                                    JavaSqlFactory.doHiveConfigurationClear();
                                }
                            } else {
                                returnTablesFormConnection = ExtractMetaDataFromDataBase
                                        .returnTablesFormConnection(iMetadataConnection);

                            }
                            Display.getDefault().syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    final DbTableSelectorObject object = new DbTableSelectorObject();
                                    DbTableSelectorObject connO = new DbTableSelectorObject();
                                    if (dbType != null && dbType.equals(EDatabaseTypeName.ORACLE_OCI.getDisplayName())) {
                                        connO.setLabel(connParameters.getLocalServiceName());
                                    } else if ("".equals(connParameters.getDbName())) { //$NON-NLS-1$
                                        connO.setLabel(connParameters.getDatasource());
                                    } else {
                                        connO.setLabel(connParameters.getDbName());
                                    }

                                    // for general jdbc, there will always no db name and data source as the label, So
                                    // ...
                                    if (connO.getLabel() == null || connO.getLabel().equals("")) { //$NON-NLS-1$
                                        if (elem instanceof INode) {
                                            connO.setLabel(elem.getElementName());
                                        } else {
                                            connO.setLabel("tJDBCConnection");
                                        }
                                    }

                                    connO.setType(ObjectType.DB);

                                    if (monitor.isCanceled()) {
                                        monitor.done();
                                        return;
                                    }
                                    for (String string : returnTablesFormConnection) {

                                        DbTableSelectorObject tableO = new DbTableSelectorObject();
                                        tableO.setLabel(string);
                                        tableO.setType(ObjectType.TABLE);
                                        connO.addChildren(tableO);
                                    }
                                    object.addChildren(connO);
                                    String propertyName = (String) openListTable.getData(PARAMETER_NAME);
                                    DbTableSelectorDialog selectorDialog = new DbTableSelectorDialog(composite.getShell(), object);
                                    if (selectorDialog.open() == DbTableSelectorDialog.OK) {
                                        String name = selectorDialog.getSelectName();
                                        if (name != null) {
                                            Command dbSelectorCommand = new PropertyChangeCommand(elem, propertyName,
                                                    TalendTextUtils.addQuotes(name));
                                            executeCommand(dbSelectorCommand);
                                            Text labelText = (Text) hashCurControls.get(propertyName);
                                            labelText.setText(TalendTextUtils.addQuotes(name));
                                        }
                                    }

                                }
                            });
                        } else {
                            Display.getDefault().syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    String pid = "org.talend.sqlbuilder"; //$NON-NLS-1$
                                    String mainMsg = "Database connection is failed. "; //$NON-NLS-1$
                                    ErrorDialogWithDetailAreaAndContinueButton dialog = new ErrorDialogWithDetailAreaAndContinueButton(
                                            composite.getShell(), pid, mainMsg, connParameters.getConnectionComment());
                                    if (dialog.getCodeOfButton() == Window.OK) {
                                        openParamemerDialog(openListTable, part.getProcess().getContextManager());
                                    }

                                }
                            });
                        }
                    } catch (SQLException e) {
                        // Added by Marvin Wang for bug TDI-24288.
                        JavaSqlFactory.doHiveConfigurationClear();
                        ExceptionHandler.process(e);

                        Display.getDefault().syncExec(new Runnable() {

                            @Override
                            public void run() {
                                MessageDialog.openError(openListTable.getShell(),
                                        Messages.getString("DbTableController.dialog.title"), //$NON-NLS-1$
                                        Messages.getString("DbTableController.dialog.contents.fetchTableFailed")); //$NON-NLS-1$
                            }
                        });

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                monitor.done();
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        openListTable.setEnabled(true);
                    }
                });
                return Status.OK_STATUS;
            }

        };

        if (part != null) {
            IWorkbenchSiteProgressService siteps = (IWorkbenchSiteProgressService) part.getSite().getAdapter(
                    IWorkbenchSiteProgressService.class);
            siteps.showInDialog(composite.getShell(), job);
        } else {
            PlatformUI.getWorkbench().getProgressService().showInDialog(composite.getShell(), job);
        }

        job.setUser(true);
        job.schedule();
    }

    private void openParamemerDialog(Button button, IContextManager manager) {
        initConnectionParameters();
        if (connParameters != null) {
            ConfigureConnParamDialog paramDialog = new ConfigureConnParamDialog(button.getShell(), connParameters, manager, elem);
            if (paramDialog.open() == Window.OK) {
                openDbTableSelectorJob(button);
            }
        } else {
            MessageDialog
                    .openWarning(
                            button.getShell(),
                            Messages.getString("DbTableController.connectionError"), Messages.getString("DbTableController.setParameter")); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    /**
     * nma Comment method "checkConnection".
     *
     * @param metadataConnection, IContextManager
     */
    protected boolean checkConnection(IMetadataConnection metadataConnection, IContextManager contextManager) {
        try {
            String DBType = ContextParameterUtils.parseScriptContextCode(metadataConnection.getDbType(), contextManager);
            String userName = ContextParameterUtils.parseScriptContextCode(metadataConnection.getUsername(), contextManager);
            String password = ContextParameterUtils.parseScriptContextCode(metadataConnection.getPassword(), contextManager);
            String schema = ContextParameterUtils.parseScriptContextCode(metadataConnection.getSchema(), contextManager);
            String driveClass = ContextParameterUtils.parseScriptContextCode(metadataConnection.getDriverClass(), contextManager);
            String driverJarPath = ContextParameterUtils.parseScriptContextCode(metadataConnection.getDriverJarPath(),
                    contextManager);
            String dbVersion = ContextParameterUtils.parseScriptContextCode(metadataConnection.getDbVersionString(),
                    contextManager);
            String additionalParams = ContextParameterUtils.parseScriptContextCode(metadataConnection.getAdditionalParams(),
                    contextManager);
            // specially used for URL
            String server = ContextParameterUtils.parseScriptContextCode(metadataConnection.getServerName(), contextManager);
            String port = ContextParameterUtils.parseScriptContextCode(metadataConnection.getPort(), contextManager);
            String sidOrDatabase = ContextParameterUtils.parseScriptContextCode(metadataConnection.getDatabase(), contextManager);
            String filePath = ContextParameterUtils.parseScriptContextCode(metadataConnection.getFileFieldName(), contextManager);
            String datasource = ContextParameterUtils.parseScriptContextCode(metadataConnection.getDataSourceName(),
                    contextManager);
            String dbRootPath = ContextParameterUtils.parseScriptContextCode(metadataConnection.getDbRootPath(), contextManager);
            String additionParam = ContextParameterUtils.parseScriptContextCode(metadataConnection.getAdditionalParams(),
                    contextManager);

            String newURL = DatabaseConnStrUtil.getURLString(DBType, dbVersion, server, userName, password, port, sidOrDatabase,
                    filePath.toLowerCase(), datasource, dbRootPath, additionParam);

            ConnectionStatus testConnection = ExtractMetaDataFromDataBase.testConnection(DBType, newURL, userName, password,
                    schema, driveClass, driverJarPath, dbVersion, additionalParams);
            ConnectionParameters connParameters2 = new ConnectionParameters();
            if (connParameters == null) {
                connParameters = connParameters2;
            }
            connParameters.setConnectionComment(testConnection.getMessageException());

            if (EDatabaseTypeName.ACCESS.getDisplayName().equals(connParameters.getDbType())) {
                return true;
            }

            return testConnection.getResult();
        } catch (Exception e) {
            log.error(Messages.getString("CommonWizard.exception") + "\n" + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return false;
    }

    /**
     * qzhang Comment method "checkConnection".
     *
     * @param metadataConnection
     */
    protected boolean checkConnection(IMetadataConnection metadataConnection) {
        try {
            ConnectionStatus testConnection = ExtractMetaDataFromDataBase.testConnection(metadataConnection.getDbType(),
                    metadataConnection.getUrl(), metadataConnection.getUsername(), metadataConnection.getPassword(),
                    metadataConnection.getSchema(), metadataConnection.getDriverClass(), metadataConnection.getDriverJarPath(),
                    metadataConnection.getDbVersionString(), metadataConnection.getAdditionalParams());
            connParameters.setConnectionComment(testConnection.getMessageException());

            if (EDatabaseTypeName.ACCESS.getDisplayName().equals(connParameters.getDbType())) {
                return true;
            }

            return testConnection.getResult();
        } catch (Exception e) {
            log.error(Messages.getString("CommonWizard.exception") + "\n" + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
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

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        Text labelText = (Text) hashCurControls.get(param.getName());
        if (labelText == null || labelText.isDisposed()) {
            return;
        }
        Object value = param.getValue();
        boolean valueChanged = false;
        if (value == null) {
            labelText.setText(""); //$NON-NLS-1$
        } else {
            if (!value.equals(labelText.getText())) {
                labelText.setText((String) value);
                valueChanged = true;
            }
        }
        if (checkErrorsWhenViewRefreshed || valueChanged) {
            checkErrorsForPropertiesOnly(labelText);
        }
        fixedCursorPosition(param, labelText, value, valueChanged);
    }

}
