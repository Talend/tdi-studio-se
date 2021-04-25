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

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.EEditSelection;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.ESelectionType;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.services.ISQLBuilderService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeQueryCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: SQLEditorController.java 1 2006-12-12 上午11:24:40 +0000 (上午11:24:40) yzhang $
 *
 */
public class SqlMemoController extends AbstractElementPropertySectionController {

    private IElementParameter switchParam;

    /**
     * DOC yzhang SqlMemoController constructor comment.
     *
     * @param dtp
     */
    public SqlMemoController(IDynamicProperty dp) {
        super(dp);
    }

    private Button openSQLEditorButton;

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand();
            executeCommand(cmd);
        }
    };

    private ColorStyledText queryText;

    // built-in open query
    private Command createCommand() {
        initConnectionParameters();
        IElementParameter basePropertyParameter = null;
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.PROPERTY_TYPE) {
                if (param.getRepositoryValue().startsWith("DATABASE")) {
                    basePropertyParameter = param;
                    break;
                }
            }
        }
        String repositoryType = null;
        if (basePropertyParameter != null) {
            repositoryType = (String) basePropertyParameter.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName())
                    .getValue();
        } else {
            repositoryType = (String) elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
        }
        String propertyName = (String) openSQLEditorButton.getData(PARAMETER_NAME);
        String query = (String) elem.getPropertyValue(propertyName);
        ECodeLanguage lang = LanguageManager.getCurrentLanguage();
        if ((!TalendQuoteUtils.isCommonString(query) || QueryUtil.checkIfHasSpecialEscapeValue(query)
                || QueryUtil
                .checkIfIsNoQuotesAtAll(query)) && (lang == ECodeLanguage.JAVA)) {// if
            // the input query is in context mode in java
            // String pid = SqlBuilderPlugin.PLUGIN_ID;
            // String mainMsg = Messages.getString("SqlMemoController.QueryError.mainMsg");
            // String infoMsg = Messages.getString("SqlMemoController.QueryError.infoMsg",
            // TalendTextUtils.getQuoteChar());
            // new ErrorDialogWidthDetailArea(composite.getShell(), pid, mainMsg, infoMsg);
            // return null;

            // pass the value to Initializing the contextmode button
            connParameters.setIfContextButtonCheckedFromBuiltIn(true);
            // String contextSql = openSQLBuilder(repositoryType, propertyName, query);
            // if (contextSql != null) {
            // queryText.setText(contextSql);
            // return new PropertyChangeCommand(elem, propertyName, contextSql);
            // }
            // return null;
        } else {
            query = TalendTextUtils.removeStrInQuery(query);
        }

        initConnectionParametersWithContext(elem, part == null ? new EmptyContextManager().getDefaultContext() : part
                .getProcess().getContextManager().getDefaultContext());
        String sql = openSQLBuilder(repositoryType, propertyName, query);
        if (sql != null) {
            queryText.setText(sql);
            return new PropertyChangeCommand(elem, propertyName, sql);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());

        final DecoratedField dField1 = new DecoratedField(subComposite, SWT.PUSH, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return new Button(parent, style);
            }
        });

        Control buttonControl = dField1.getLayoutControl();
        openSQLEditorButton = (Button) dField1.getControl();

        openSQLEditorButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        openSQLEditorButton.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        buttonControl.setBackground(subComposite.getBackground());
        openSQLEditorButton.setEnabled(true);
        openSQLEditorButton.setData(NAME, SQLEDITOR);
        openSQLEditorButton.setData(PARAMETER_NAME, param.getName());
        openSQLEditorButton.setEnabled(!param.isReadOnly());
        openSQLEditorButton.addSelectionListener(listenerSelection);
        if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
            openSQLEditorButton.setEnabled(ExtractMetaDataUtils.getInstance().haveLoadMetadataNode());
        }

        openSQLEditorButton.setVisible(visibleOpenSQLEditorButton());// visible

        FormData data1 = new FormData();
        data1.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
        data1.left = new FormAttachment(100, -(ITabbedPropertyConstants.HSPACE + STANDARD_BUTTON_WIDTH));
        data1.top = new FormAttachment(0, top);

        buttonControl.setLayoutData(data1);

        int nbLines = param.getNbLines();

        IControlCreator txtCtrl = new IControlCreator() {

            @Override
            public Control createControl(final Composite parent, final int style) {
                return createColorStyledText(parent, style);
            }
        };
        DecoratedField dField = null;
        if (param.getNbLines() != 1) {
            dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, txtCtrl);
        } else {
            dField = new DecoratedField(subComposite, SWT.BORDER, txtCtrl);
        }
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();
        queryText = (ColorStyledText) dField.getControl();
        queryText.setData(PARAMETER_NAME, param.getName());
        editionControlHelper.register(param.getName(), queryText);

        FormData d = (FormData) queryText.getLayoutData();
        if (getAdditionalHeightSize() != 0) {
            nbLines += this.getAdditionalHeightSize() / queryText.getLineHeight();
        }
        d.height = queryText.getLineHeight() * nbLines;
        FormData data;
        queryText.getParent().setSize(subComposite.getSize().x, queryText.getLineHeight() * nbLines);
        cLayout.setBackground(subComposite.getBackground());
        // for bug 7580
        if (!(queryText instanceof ColorStyledText)) {
            queryText.setEnabled(!param.isReadOnly());
        } else {
            queryText.setEditable(!param.isReadOnly());
        }
        if (elem instanceof Node) {
            queryText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
        queryText.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (switchParam != null) {
                    switchParam.setValue(Boolean.FALSE);
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });

        if (param.isReadOnly() && param.isRepositoryValueUsed()) {
            cLayout.addMouseListener(listenerClick);
        }
        addDragAndDropTarget(queryText);

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
        data.right = new FormAttachment(buttonControl, -5, SWT.LEFT);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), queryText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        // curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return null;
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
        IControlCreator txtCtrl = new IControlCreator() {

            @Override
            public Control createControl(final Composite parent, final int style) {
                return createColorStyledText(parent, style);
            }
        };

        DecoratedField dField = null;
        if (param.getNbLines() != 1) {
            dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, txtCtrl);
        } else {
            dField = new DecoratedField(subComposite, SWT.BORDER, txtCtrl);
        }
        ColorStyledText text = (ColorStyledText) dField.getControl();
        FormData d = (FormData) text.getLayoutData();
        d.height = text.getLineHeight() * param.getNbLines();
        text.getParent().setSize(subComposite.getSize().x, text.getLineHeight() * param.getNbLines());

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#hasDynamicRowSize
     * ()
     */
    @Override
    public boolean hasDynamicRowSize() {
        return true;
    }

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        ColorStyledText labelText = (ColorStyledText) hashCurControls.get(param.getName());
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

    MouseListener listenerClick = new MouseAdapter() {

        @Override
        public void mouseDown(MouseEvent e) {
            promptForChangingMode(((Control) e.getSource()).getShell());
        }
    };

    private Command changeToBuildInCommand() {

        return new RepositoryChangeQueryCommand(this.elem, null, "QUERYSTORE_TYPE", "BUILT_IN"); //$NON-NLS-1$ //$NON-NLS-2$

    }

    private Command refreshConnectionCommand() {
        // open sql builder in repository mode, just use query object, no need for connection information
        ConnectionParameters connParameters = new ConnectionParameters();
        String queryId = (String) elem.getPropertyValue(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName());
        Query query = MetadataToolHelper.getQueryFromRepository(queryId);
        DatabaseConnectionItem item = findRepositoryItem(queryId);
        if (item != null) {
            connParameters.setRepositoryName(item.getProperty().getLabel());
            connParameters.setRepositoryId(item.getProperty().getId());
        }
        connParameters.setQueryObject(query);
        connParameters.setQuery(query.getValue(), true);

        TextUtil.setDialogTitle(TextUtil.SQL_BUILDER_TITLE_REP);

        String processName = null;
        if (elem instanceof IProcess) {
            processName = ((IProcess) elem).getName();
        } else if (elem instanceof INode) {
            processName = ((INode) elem).getProcess().getName();
        } else if (elem instanceof IConnection) {
            processName = ((IConnection) elem).getSource().getProcess().getName();
        }

        connParameters.setNodeReadOnly(false);
        connParameters.setFromRepository(true);
        ISQLBuilderService sqlBuilderService = GlobalServiceRegister.getDefault().getService(
                ISQLBuilderService.class);
        Dialog sqlBuilder = sqlBuilderService.openSQLBuilderDialog(composite.getShell(), processName, connParameters);

        String sql = null;

        if (Window.OK == sqlBuilder.open()) {
            sql = connParameters.getQuery();
            sql = TalendTextUtils.addQuotes(sql);
        }
        if (sql != null && !queryText.isDisposed()) {
            queryText.setText(sql);
            String propertyName = (String) openSQLEditorButton.getData(PARAMETER_NAME);
            return new PropertyChangeCommand(elem, propertyName, sql);
        }
        return null;
    }

    /**
     * Find the label of DatabaseConnectionItem that contains current query.
     *
     * @param queryId
     * @return
     */
    private DatabaseConnectionItem findRepositoryItem(String queryId) {
        try {
            String[] names = queryId.split(" - "); //$NON-NLS-1$
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            IRepositoryViewObject node = factory.getLastVersion(names[0]);
            DatabaseConnectionItem item = (DatabaseConnectionItem) node.getProperty().getItem();
            return item;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    /**
     * Display a dialog to ask the user to update the query in the repository directly or change the query to built-in
     * mode.
     *
     * @param shell
     */
    private void promptForChangingMode(Shell shell) {

        ModelSelectionDialog modelSelect = new ModelSelectionDialog(shell, ESelectionType.QUERY);

        if (modelSelect.open() == ModelSelectionDialog.OK) {
            if (modelSelect.getOptionValue() == EEditSelection.BUILDIN) {
                executeCommand(changeToBuildInCommand());
            }
            if (modelSelect.getOptionValue() == EEditSelection.REPOSITORY) {
                executeCommand(refreshConnectionCommand());
            }
            if (modelSelect.getOptionValue() == EEditSelection.SHOW_QUERY) {
                if (queryText != null) {
                    ShowQueryDialog showQueryDialog = new ShowQueryDialog(shell, queryText.getText());
                    showQueryDialog.open();
                }
            }
        }
    }

    private ColorStyledText createColorStyledText(final Composite parent, final int style) {
        ColorStyledText colorText = new ColorStyledText(parent, style, CorePlugin.getDefault().getPreferenceStore(), "tsql"); //$NON-NLS-1$
        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
        String fontType = preferenceStore.getString(TalendDesignerPrefConstants.MEMO_TEXT_FONT);
        FontData fontData = new FontData(fontType);
        Font font = new Font(null, fontData);
        addResourceDisposeListener(colorText, font);
        colorText.setFont(font);
        return colorText;
    }

    /**
     *
     * ggu ShowQueryDialog class global comment. Detailled comment
     */
    class ShowQueryDialog extends Dialog {

        private String query;

        public ShowQueryDialog(Shell parentShell, String query) {
            super(parentShell);
            setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
            this.query = query;
        }

        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.getString("SqlMemoController.query")); //$NON-NLS-1$
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite2 = (Composite) super.createDialogArea(parent);

            ColorStyledText colorStyledText = createColorStyledText(composite2, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL
                    | SWT.READ_ONLY | SWT.WRAP);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.minimumHeight = 130;
            gridData.heightHint = 150;
            gridData.minimumWidth = 350;
            gridData.widthHint = 400;
            colorStyledText.setLayoutData(gridData);
            if (query != null) {
                colorStyledText.setText(query);
            }
            return colorStyledText;
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        }
    }

    public boolean visibleOpenSQLEditorButton() {
        IElementParameter typeParam = elem.getElementParameter("TYPE"); //$NON-NLS-1$
        if (typeParam != null) {
            String typeParamValue = String.valueOf(typeParam.getValue());
            if ("Hive".equalsIgnoreCase(typeParamValue) || "Impala".equalsIgnoreCase(typeParamValue)) { //$NON-NLS-1$//$NON-NLS-2$
                return false;
            }
        }
        if (curParameter != null) {
            if (curParameter instanceof ElementParameter) {
                Object sourceName = ((ElementParameter) curParameter).getTaggedValue("org.talend.sdk.component.source");//$NON-NLS-1$
                if ("tacokit".equalsIgnoreCase(String.valueOf(sourceName))) {//$NON-NLS-1$
                    Object familyValue = elem.getPropertyValue(EParameterName.FAMILY.getName());
                    if (isInWizard() || "jdbc".equalsIgnoreCase(String.valueOf(familyValue))) { //$NON-NLS-1$
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
