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
package org.talend.repository.ui.login.connections;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.LabelText;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.DynamicButtonBean;
import org.talend.core.repository.model.DynamicChoiceBean;
import org.talend.core.repository.model.DynamicFieldBean;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.model.provider.LoginConnectionManager;
import org.talend.core.repository.services.ILoginConnectionService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.actions.importproject.DeleteProjectsAsAction;
import org.talend.repository.ui.login.LoginDialogV2;
import org.talend.repository.ui.login.LoginHelper;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ConnectionFormComposite extends Composite {

    private ConnectionsDialog dialog;

    private FormToolkit toolkit;

    private ComboViewer repositoryCombo;

    private Text nameText;

    private Text descriptionText;

    private Label userLabel;

    private Text userText;

    private Label tpPlaceHolder;

    private Composite tpCompo;

    private Button tokenButton;

    private Composite tokenCompo;

    private Text tokenText;

    private Button tokenBrowseButton;

    private Button passwordButton;

    private Text passwordText;

    private String password;

    private Text workSpaceText;

    private Button workSpaceButton;

    protected Button deleteProjectsButton;

    private ConnectionBean connection;

    private ConnectionsListComposite connectionsListComposite;

    private Map<IRepositoryFactory, Map<String, LabelText>> dynamicControls = new HashMap<IRepositoryFactory, Map<String, LabelText>>();

    private Map<IRepositoryFactory, Map<String, LabelText>> dynamicRequiredControls = new HashMap<IRepositoryFactory, Map<String, LabelText>>();

    private Map<IRepositoryFactory, Map<String, Button>> dynamicButtons = new HashMap<IRepositoryFactory, Map<String, Button>>();

    private Map<IRepositoryFactory, Map<String, LabelledCombo>> dynamicChoices = new HashMap<IRepositoryFactory, Map<String, LabelledCombo>>();

    public static final String URL_FIELD_NAME = "url"; //$NON-NLS-1$

    private static final String TOKEN_URL_SUFFIX = "/user/access-tokens"; //$NON-NLS-1$

    Label passwordLabel = null;
    /**
     * DOC smallet ConnectionsComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public ConnectionFormComposite(Composite parent, int style, ConnectionsListComposite connectionsListComposite,
            ConnectionsDialog dialog) {
        super(parent, style);
        this.dialog = dialog;
        this.connectionsListComposite = connectionsListComposite;

        toolkit = new FormToolkit(this.getDisplay());
        toolkit.setBackground(ColorConstants.white);
        Composite formBody = toolkit.createComposite(this);
        formBody.setBackground(ColorConstants.white);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        formBody.setLayoutData(new GridData(GridData.FILL_BOTH));

        formBody.setLayout(new GridLayout(3, false));
        GridDataFactory formDefaultFactory = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER);
        // Repository
        Label repositoryLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.repository")); //$NON-NLS-1$
        formDefaultFactory.copy().applyTo(repositoryLabel);

        repositoryCombo = new ComboViewer(formBody, SWT.BORDER | SWT.READ_ONLY);
        repositoryCombo.setContentProvider(ArrayContentProvider.getInstance());
        repositoryCombo.setLabelProvider(new RepositoryFactoryLabelProvider());
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(repositoryCombo.getControl());

        // Name
        Label nameLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.name")); //$NON-NLS-1$
        formDefaultFactory.copy().applyTo(nameLabel);

        nameText = toolkit.createText(formBody, "", SWT.BORDER); //$NON-NLS-1$
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(nameText);

        // Comment
        Label descriptionLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.description")); //$NON-NLS-1$
        formDefaultFactory.copy().applyTo(descriptionLabel);

        descriptionText = toolkit.createText(formBody, "", SWT.BORDER); //$NON-NLS-1$
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(descriptionText);

        // User
        userLabel = toolkit.createLabel(formBody, ""); //$NON-NLS-1$
        changeUserLabel();

        formDefaultFactory.copy().applyTo(userLabel);

        userText = toolkit.createText(formBody, "", SWT.BORDER); //$NON-NLS-1$
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(userText);

        passwordLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.password")); //$NON-NLS-1$
        formDefaultFactory.copy().applyTo(passwordLabel);

        tpPlaceHolder = new Label(formBody, SWT.NONE);
        tpCompo = toolkit.createComposite(formBody);
        GridLayout tCompoLayout = new GridLayout(3, false);
        tCompoLayout.marginHeight = 0;
        tCompoLayout.marginWidth = 0;
        tpCompo.setLayout(tCompoLayout);
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(tpCompo);

        // Password
        passwordButton = toolkit.createButton(tpCompo, Messages.getString("connections.form.field.password"), SWT.RADIO); //$NON-NLS-1$
        formDefaultFactory.copy().applyTo(passwordButton);

        passwordText = toolkit.createText(tpCompo, "", SWT.PASSWORD | SWT.BORDER); //$NON-NLS-1$
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(passwordText);

        Label workSpaceLabel = toolkit.createLabel(formBody, Messages.getString("ConnectionFormComposite.WORKSPACE")); //$NON-NLS-1$
        formDefaultFactory.copy().applyTo(workSpaceLabel);
        // Token
        tokenButton = toolkit.createButton(tpCompo, Messages.getString("connections.form.field.token"), SWT.RADIO); //$NON-NLS-1$
        formDefaultFactory.copy().applyTo(tokenButton);

        tokenCompo = toolkit.createComposite(tpCompo);
        GridLayout tokenCompoLayout = new GridLayout(3, false);
        tokenCompoLayout.marginHeight = 0;
        tokenCompoLayout.marginWidth = 0;
        tokenCompo.setLayout(tokenCompoLayout);
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(tokenCompo);

        tokenText = toolkit.createText(tokenCompo, "", SWT.PASSWORD | SWT.BORDER); //$NON-NLS-1$
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(tokenText);

        tokenBrowseButton = toolkit.createButton(tokenCompo, null, SWT.PUSH);
        tokenBrowseButton.setToolTipText(Messages.getString("connections.form.field.token.browse")); //$NON-NLS-1$
        tokenBrowseButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        GridDataFactory.fillDefaults().applyTo(tokenBrowseButton);

        Composite wsCompo = toolkit.createComposite(formBody);
        GridLayout wsCompoLayout = new GridLayout(2, false);
        wsCompoLayout.marginHeight = 0;
        wsCompoLayout.marginWidth = 0;

        wsCompo.setLayout(wsCompoLayout);
        formDefaultFactory.copy().grab(true, false).span(2, 1).applyTo(wsCompo);

        workSpaceText = toolkit.createText(wsCompo, "", SWT.BORDER); //$NON-NLS-1$
        formDefaultFactory.copy().grab(true, false).applyTo(workSpaceText);

        workSpaceButton = toolkit.createButton(wsCompo, null, SWT.PUSH);
        workSpaceButton.setToolTipText(Messages.getString("ConnectionFormComposite.SELECT_WORKSPACE")); //$NON-NLS-1$
        workSpaceButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        GridDataFactory.fillDefaults().applyTo(workSpaceButton);

        List<IRepositoryFactory> availableRepositories = getUsableRepositoryProvider();
        for (IRepositoryFactory current : availableRepositories) {
            Map<String, LabelText> list = new HashMap<String, LabelText>();
            Map<String, LabelText> listRequired = new HashMap<String, LabelText>();
            Map<String, Button> listButtons = new HashMap<String, Button>();
            Map<String, LabelledCombo> listChoices = new HashMap<String, LabelledCombo>();
            dynamicControls.put(current, list);
            dynamicRequiredControls.put(current, listRequired);
            dynamicButtons.put(current, listButtons);
            dynamicChoices.put(current, listChoices);

            for (final DynamicChoiceBean currentChoiceBean : current.getChoices()) {
                Label label = toolkit.createLabel(formBody, currentChoiceBean.getName());
                formDefaultFactory.copy().applyTo(label);

                Combo combo = new Combo(formBody, SWT.BORDER | SWT.READ_ONLY);
                for (String label1 : currentChoiceBean.getChoices().values()) {
                    combo.add(label1);
                }

                formDefaultFactory.copy().grab(true, false).applyTo(combo);

                listChoices.put(currentChoiceBean.getId(), new LabelledCombo(label, combo));
            }

            for (DynamicFieldBean currentField : current.getFields()) {
                int textStyle = SWT.BORDER;
                if (currentField.isPassword()) {
                    textStyle = textStyle | SWT.PASSWORD;
                }
                Label label = toolkit.createLabel(formBody, currentField.getName());
                formDefaultFactory.copy().align(SWT.FILL, SWT.CENTER).applyTo(label);

                Text text = toolkit.createText(formBody, "", textStyle); //$NON-NLS-1$
                if (currentField.getDefaultValue() != null) {
                    text.setText(currentField.getDefaultValue());
                }
                boolean isReadonly = currentField.isReadonly();
                text.setEditable(!isReadonly);
                if (isReadonly) {
                    text.setForeground(null);
                    text.setBackground(null);
                }

                formDefaultFactory.copy().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(text);
                LabelText labelText = new LabelText(label, text);
                if (currentField.isRequired()) {
                    listRequired.put(currentField.getId(), labelText);
                }
                list.put(currentField.getId(), labelText);
            }

            for (final DynamicButtonBean currentButtonBean : current.getButtons()) {
                Button button = new Button(formBody, SWT.PUSH);
                button.setText(currentButtonBean.getName());
                button.addSelectionListener(new DelegateSelectionListener(currentButtonBean));
                formDefaultFactory.copy().align(SWT.RIGHT, SWT.CENTER).applyTo(button);

                listButtons.put(currentButtonBean.getId(), button);
            }
        }

        Label seperator = new Label(formBody, SWT.NONE);
        seperator.setVisible(false);
        GridData seperatorGridData = new GridData();
        seperatorGridData.horizontalSpan = 3;
        seperatorGridData.heightHint = 0;
        seperator.setLayoutData(seperatorGridData);
        Label placeHolder = new Label(formBody, SWT.NONE);
        // add delete buttons
        deleteProjectsButton = new Button(formBody, SWT.NONE);
        deleteProjectsButton.setText(Messages.getString("ConnectionFormComposite.deleteExistingProject")); //$NON-NLS-1$
        GridData deleteButtonGridData = new GridData();
        deleteButtonGridData.widthHint = LoginDialogV2.getNewButtonSize(deleteProjectsButton).x;
        deleteButtonGridData.horizontalSpan = 2;
        deleteProjectsButton.setLayoutData(deleteButtonGridData);

        addListeners();
        addTokenBrowseListener();
        addWorkSpaceListener();
        fillLists();
        showHideDynamicsControls();
        showHideTexts();
        // validateFields();
    }

    public boolean canFinish() {
        return validateFields();
    }

    private boolean isValidatedWorkspace(String workSpace) {
        File file = new File(workSpace);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        } else {
            return true;
        }

    }

    private boolean validateFields() {
        String errorMsg = null;
        boolean valid = true;
        if (dialog.getOKButton() != null) {
            dialog.getOKButton().setEnabled(true);
        }
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean isOnlyRemoteConnection = brandingService.getBrandingConfiguration().isOnlyRemoteConnection();
        boolean usesMailCheck = brandingService.getBrandingConfiguration().isUseMailLoginCheck();
        LabelText emptyUrl = null;
        if (getRepository() != null) {
            for (LabelText currentUrlLabel : dynamicRequiredControls.get(getRepository()).values()) {
                if (valid && currentUrlLabel.getText().length() == 0) {
                    emptyUrl = currentUrlLabel;
                }
            }
        }
        toActiveDynamicButtons(true);
        if (valid && getRepository() == null) {
            errorMsg = Messages.getString("connections.form.emptyField.repository"); //$NON-NLS-1$
        } else if (valid && getTextName().length() == 0) {
            errorMsg = Messages.getString("connections.form.emptyField.connname"); //$NON-NLS-1$
        } else if (valid && getUser().length() == 0) {
            errorMsg = Messages.getString("connections.form.emptyField.username"); //$NON-NLS-1$
            toActiveDynamicButtons(false);
        } else if (valid && isLocalConnection() && !Pattern.matches(RepositoryConstants.MAIL_PATTERN, getUser())) {
            errorMsg = Messages.getString("connections.form.malformedField.username"); //$NON-NLS-1$
        } else if (valid && emptyUrl != null) {
            errorMsg = Messages.getString("connections.form.dynamicFieldEmpty", emptyUrl.getLabel()); //$NON-NLS-1$
        } else if (valid && !this.isValidatedWorkspace(this.getWorkspace())) {
            errorMsg = Messages.getString("ConnectionFormComposite.workspaceInvalid"); //$NON-NLS-1$
        } else if (valid && isOnlyRemoteConnection) {
            // Uniserv feature 8,Add new Extension point to allow Uniserv to add some custom controls during TAC
            // connection check
            List<ILoginConnectionService> loginConnections = LoginConnectionManager.getRemoteConnectionService();
            for (ILoginConnectionService loginConncetion : loginConnections) {
                errorMsg = loginConncetion.checkConnectionValidation(getTextName(), getDesc(), getUser(), getPassword(),
                        getWorkspace(), connection.getDynamicFields().get(RepositoryConstants.REPOSITORY_URL));
            }
        } else if (valid && getTextName() != null) {
            List<ConnectionBean> connectionBeanList = dialog.getConnections();
            if (connectionBeanList != null && connectionBeanList.size() > 1) {
                for (ConnectionBean connectionBean : connectionBeanList) {
                    String connectionBeanName = connectionBean.getName();
                    if (connectionBeanName != null) {
                        if (this.connection != connectionBean) {
                            if (connectionBeanName.equals(getTextName())) {
                                errorMsg = Messages.getString("ConnectionFormComposite.connectionNameInvalid"); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }
        if (errorMsg != null && !errorMsg.equals("")) { //$NON-NLS-1$
            valid = false;
        }
        if (!valid) {
            dialog.setErrorMessage(errorMsg);
            if (dialog.getOKButton() != null) {
                dialog.getOKButton().setEnabled(false);
            }
        } else {
            dialog.setErrorMessage(null);
        }

        if (connection != null) {
            connection.setComplete(valid);
            connectionsListComposite.refresh(connection);
        }
        return valid;
    }

    private void showHideDynamicsControls() {
        // PTODO SML Optimize
        // 1. Hide all controls:
        for (IRepositoryFactory f : dynamicControls.keySet()) {
            for (LabelText control : dynamicControls.get(f).values()) {
                // control.setVisible(false);
                hideControl(control.getLabelControl(), true, false);
                hideControl(control.getTextControl(), true, false);
            }

            for (Button control : dynamicButtons.get(f).values()) {
                // control.setVisible(false);
                hideControl(control, true, false);
            }

            for (LabelledCombo control : dynamicChoices.get(f).values()) {
                // control.setVisible(false);
                hideControl(control.getCombo(), true, false);
            }
        }

        // 2. Show active repository controls:
        if (getRepository() != null) {
            for (LabelText control : dynamicControls.get(getRepository()).values()) {
                // control.setVisible(true);
                hideControl(control.getLabelControl(), false, false);
                hideControl(control.getTextControl(), false, false);
            }

            for (Button control : dynamicButtons.get(getRepository()).values()) {
                // control.setVisible(true);
                hideControl(control, false, false);
            }

            for (LabelledCombo control : dynamicChoices.get(getRepository()).values()) {
                // control.setVisible(true);
                hideControl(control.getCombo(), false, false);
            }

        }
        nameText.getParent().layout();
    }

    private void showHideTexts() {
        boolean token = false;
        if (connection != null && tokenText != null && !tokenText.isDisposed()) {
            token = connection.isToken();
        }
        // Check enable field or not
        boolean enablePasswordField = false;
        boolean enableTokenField = false;
        if (connection != null) {
            IRepositoryFactory factory = RepositoryFactoryProvider.getRepositoriyById(connection.getRepositoryId());
            if (factory != null && factory.isAuthenticationNeeded()) {
                enablePasswordField = true;
                if (LoginHelper.isCloudConnection(connection)) {
                    enableTokenField = true;
                }
            }
        } else if (getRepository() != null && LoginHelper.isRemotesRepository(getRepository().getId())) {
            enablePasswordField = true;
            if (LoginHelper.isCloudRepository(getRepository().getId())) {
                enableTokenField = true;
            }
        }

        // password
        boolean hidePasswordLabel = !enablePasswordField || enableTokenField;
        boolean enablePasswordText = passwordButton.getSelection() || !hidePasswordLabel;
        if (passwordText != null && !passwordText.isDisposed()) {
            if (enablePasswordField && enablePasswordText) {
                passwordText.setBackground(LoginDialogV2.WHITE_COLOR);
            } else {
                passwordText.setText(""); //$NON-NLS-1$
                password = ""; //$NON-NLS-1$
                passwordText.setBackground(LoginDialogV2.GREY_COLOR);
            }
            passwordText.setEnabled(enablePasswordField && enablePasswordText);
            passwordText.setEditable(enablePasswordField && enablePasswordText);
            hideControl(passwordText, !enablePasswordField, false);
            hideControl(passwordLabel, hidePasswordLabel, false);
            hideControl(passwordButton, !(enablePasswordField && enableTokenField), false);
            passwordText.getParent().layout();
            passwordText.getParent().getParent().layout();
        }
        // token
        if (tokenText != null && !tokenText.isDisposed()) {
            if (enableTokenField && tokenButton.getSelection()) {
                tokenText.setBackground(LoginDialogV2.WHITE_COLOR);
            } else {
                tokenText.setText(""); //$NON-NLS-1$
                password = ""; //$NON-NLS-1$
                tokenText.setBackground(LoginDialogV2.GREY_COLOR);
            }
            tokenText.setEnabled(enableTokenField && tokenButton.getSelection());
            tokenText.setEditable(enableTokenField && tokenButton.getSelection());
            tokenBrowseButton.setEnabled(enableTokenField && tokenButton.getSelection());
            hideControl(tokenButton, !enableTokenField, false);
            hideControl(tokenCompo, !enableTokenField, false);
            tokenText.getParent().getParent().layout();
            tokenText.getParent().getParent().getParent().layout();
        }

        hideControl(tpPlaceHolder, !enableTokenField, false);
        hideControl(tpCompo, !enablePasswordField, false);
        tpCompo.getParent().layout();
    }

    private void hideControl(Control control, boolean hide, boolean autoLayout) {
        control.setVisible(!hide);
        GridData layoutData = (GridData) control.getLayoutData();
        layoutData.exclude = hide;
        if (autoLayout) {
            control.getParent().layout();
        }
    }

    private void toActiveDynamicButtons(boolean enable) {
        // Button would only be active if user are filled
        if (getRepository() != null) {
            for (Button control : dynamicButtons.get(getRepository()).values()) {
                control.setEnabled(enable);
            }
        }
    }

    public IRepositoryFactory getRepository() {
        IRepositoryFactory repositoryFactory = null;
        IStructuredSelection sel = (IStructuredSelection) repositoryCombo.getSelection();
        repositoryFactory = (IRepositoryFactory) sel.getFirstElement();
        return repositoryFactory;
    }

    private String getTextName() {
        return nameText.getText();
    }

    private String getDesc() {
        return descriptionText.getText();
    }

    private String getUser() {
        return userText.getText();
    }

    private String getPassword() {
        return password;
    }

    private String getWorkspace() {
        return workSpaceText.getText();
    }

    ModifyListener standardTextListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            IRepositoryFactory repository = getRepository();
            if (repository != null && LoginHelper.isCloudRepository(repository.getId())) {
                String text = userText.getText();
                String lowerCaseText = text.toLowerCase();
                if (!text.equals(lowerCaseText)) {
                    Point selection = userText.getSelection();
                    userText.setText(lowerCaseText);
                    userText.setSelection(selection);
                }
            }
            fillBean(false, false);
            validateFields();
        }
    };

    ModifyListener standardTokenPasswordTextListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            fillBean(false, true);
            validateFields();
        }
    };

    ISelectionChangedListener repositoryListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(SelectionChangedEvent e) {
            showHideDynamicsControls();
            validateFields();
            fillBean(true, true);
            showHideTexts();
            changeUserLabel();
        }

    };

    SelectionListener deleteProjectClickListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                deleteProject();
            } catch (Throwable t) {
                CommonExceptionHandler.process(t);
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // nothing need to do
        }
    };

    SelectionListener passwordClickListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            tokenButton.setSelection(!passwordButton.getSelection());
            passwordButton.setSelection(passwordButton.getSelection());
            passwordText.setEnabled(passwordButton.getSelection());
            showHideTexts();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // nothing need to do
        }
    };

    SelectionListener tokenClickListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            passwordButton.setSelection(!tokenButton.getSelection());
            tokenButton.setSelection(tokenButton.getSelection());
            tokenText.setEnabled(tokenButton.getSelection());
            showHideTexts();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // nothing need to do
        }
    };

    private void addListeners() {
        repositoryCombo.addPostSelectionChangedListener(repositoryListener);
        nameText.addModifyListener(standardTextListener);
        descriptionText.addModifyListener(standardTextListener);
        userText.addModifyListener(standardTextListener);
        passwordText.addModifyListener(standardTokenPasswordTextListener);
        tokenText.addModifyListener(standardTokenPasswordTextListener);
        workSpaceText.addModifyListener(standardTextListener);

        for (IRepositoryFactory f : dynamicControls.keySet()) {
            for (LabelText control : dynamicControls.get(f).values()) {
                control.addModifyListener(standardTextListener);
            }
        }

        for (IRepositoryFactory f : dynamicChoices.keySet()) {
            for (LabelledCombo control : dynamicChoices.get(f).values()) {
                control.getCombo().addModifyListener(standardTextListener);
            }
        }
        passwordButton.addSelectionListener(passwordClickListener);
        tokenButton.addSelectionListener(tokenClickListener);
        deleteProjectsButton.addSelectionListener(deleteProjectClickListener);
    }

    public void changeUserLabel() {
        if (userLabel != null) {
            if (isLocalConnection()) {
                userLabel.setText(Messages.getString("connections.form.field.username")); //$NON-NLS-1$
            } else {
                userLabel.setText(Messages.getString("connections.form.field.usernameNoMail")); //$NON-NLS-1$
            }
        }
    }

    private void addWorkSpaceListener() {
        workSpaceButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dirDialog = new DirectoryDialog(dialog.getShell());
                String path = dirDialog.open();
                if (path == null || "".equals(path)) { //$NON-NLS-1$
                    workSpaceText.setText(getRecentWorkSpace());
                } else {
                    //                    String cPath = path.replaceAll("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    workSpaceText.setText(path);

                }
            }
        });
    }

    private void addTokenBrowseListener() {
        tokenBrowseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String currentUrl = "";
                if (getRepository() != null) {
                    for (LabelText currentUrlLabel : dynamicRequiredControls.get(getRepository()).values()) {
                        if (currentUrlLabel.getText().length() != 0) {
                            currentUrl = currentUrlLabel.getText();
                        }
                    }
                }
                try {
                    String tokenUrl = getTokenUrl(currentUrl);
                    openBrower(tokenUrl);
                } catch (Exception exception) {
                    MessageDialog.openError(getShell(), getShell().getText(),
                            Messages.getString("connections.form.getTokenUrl.failed")); //$NON-NLS-1$
                }
            }
        });
    }

    private String getTokenUrl(String serverUrlStr) throws Exception {
        URL serverUrl = new URL(serverUrlStr);
        String host = serverUrl.getHost().replace("tmc.", "");
        URL tokenUrl = new URL(serverUrl.getProtocol(), host, TOKEN_URL_SUFFIX);
        return tokenUrl.toString();
    }

    private void openBrower(String url) {
        Program.launch(url);
    }

    private void removeListeners() {
        repositoryCombo.removePostSelectionChangedListener(repositoryListener);
        nameText.removeModifyListener(standardTextListener);
        descriptionText.removeModifyListener(standardTextListener);
        userText.removeModifyListener(standardTextListener);
        passwordText.removeModifyListener(standardTokenPasswordTextListener);
        tokenText.removeModifyListener(standardTokenPasswordTextListener);
        workSpaceText.removeModifyListener(standardTextListener);

        for (IRepositoryFactory f : dynamicControls.keySet()) {
            for (LabelText control : dynamicControls.get(f).values()) {
                control.removeModifyListener(standardTextListener);
            }
        }

        for (IRepositoryFactory f : dynamicChoices.keySet()) {
            for (LabelledCombo control : dynamicChoices.get(f).values()) {
                control.getCombo().removeModifyListener(standardTextListener);
            }
        }
        passwordButton.removeSelectionListener(passwordClickListener);
        tokenButton.removeSelectionListener(tokenClickListener);
        deleteProjectsButton.removeSelectionListener(deleteProjectClickListener);
    }

    private void fillBean(boolean cleanDynamicValue, boolean enableHidePassword) {
        if (connection != null) {
            IRepositoryFactory repository = getRepository();
            if (repository != null) {
                connection.setRepositoryId(repository.getId());

                Map<String, String> connFields = new HashMap<String, String>();

                Map<String, LabelText> map = dynamicControls.get(repository);

                Map<String, DynamicFieldBean> keyBeanMap = new HashMap<>();
                List<DynamicFieldBean> fields = repository.getFields();
                if (fields != null) {
                    for (DynamicFieldBean field : fields) {
                        keyBeanMap.put(field.getId(), field);
                    }
                }

                for (String fieldKey : map.keySet()) {
                    if (cleanDynamicValue) {
                        String text = ""; //$NON-NLS-1$
                        DynamicFieldBean dynamicFieldBean = keyBeanMap.get(fieldKey);
                        if (dynamicFieldBean != null) {
                            text = dynamicFieldBean.getDefaultValue();
                        }
                        LabelText labelText = map.get(fieldKey);
                        labelText.setText(text);
                        boolean isReadonly = dynamicFieldBean.isReadonly();
                        Text textCtrl = labelText.getTextControl();
                        textCtrl.setEditable(!isReadonly);
                        if (isReadonly) {
                            textCtrl.setForeground(null);
                            textCtrl.setBackground(null);
                        }
                    }
                    connFields.put(fieldKey, map.get(fieldKey).getText());
                }

                Map<String, LabelledCombo> map2 = dynamicChoices.get(repository);
                for (String fieldKey : map2.keySet()) {
                    for (DynamicChoiceBean dynamicChoiceBean : repository.getChoices()) {
                        if (dynamicChoiceBean.getId().equals(fieldKey)) {
                            int selectionIndex = map2.get(fieldKey).getCombo().getSelectionIndex();
                            connFields.put(fieldKey, dynamicChoiceBean.getChoiceValue(selectionIndex));
                        }
                    }
                }

                connection.setDynamicFields(connFields);
            }
            connection.setName(nameText.getText());
            connection.setDescription(descriptionText.getText());
            connection.setUser(userText.getText());
            if (enableHidePassword) {
                String password = getPassword(); // $NON-NLS-1$
                if (StringUtils.isEmpty(password)) {
                    if (tokenButton.getSelection()) {
                        password = tokenText.getText();
                    } else {
                        password = passwordText.getText();
                    }
                }
                connection.setPassword(password);
                connection.setToken(tokenButton.getSelection());
            }
            connection.setWorkSpace(workSpaceText.getText());

            connectionsListComposite.refresh(connection);
        }
    }

    private void fillLists() {
        List<IRepositoryFactory> availableRepositories = getUsableRepositoryProvider();
        repositoryCombo.setInput(availableRepositories);
        fillFields();
    }

    private List<IRepositoryFactory> getUsableRepositoryProvider() {
        List<IRepositoryFactory> availableRepositories = RepositoryFactoryProvider.getAvailableRepositories();

        List<IRepositoryFactory> result = new ArrayList<IRepositoryFactory>();
        for (IRepositoryFactory repositoryFactory : availableRepositories) {
            if (repositoryFactory.isDisplayToUser()) {
                result.add(repositoryFactory);
            }
        }
        return result;
    }

    private boolean isLocalConnection() {
        IRepositoryFactory repository = getRepository();
        if (repository != null && RepositoryConstants.REPOSITORY_LOCAL_ID.equals(repository.getId())) {
            return true;
        }
        return false;
    }

    private void fillFields() {
        if (connection != null) {
            removeListeners();
            String repositoryId = connection.getRepositoryId();
            if (repositoryId == null || "".equals(repositoryId)) {
                if (getRepository() != null) {
                    connection.setRepositoryId(getRepository().getId());
                } else {
                    connection.setRepositoryId(RepositoryConstants.REPOSITORY_LOCAL_ID);
                }
            }
            IRepositoryFactory repositoriyById = RepositoryFactoryProvider.getRepositoriyById(repositoryId);
            repositoryCombo.setSelection(new StructuredSelection(new Object[] { repositoriyById }));

            if (getRepository() != null) {
                Map<String, String> dynamicFields = connection.getDynamicFields();
                String url = dynamicFields.get(RepositoryConstants.REPOSITORY_URL);
                if (null == url || "".equals(url)) {
                    List<DynamicFieldBean> fields = getRepository().getFields();
                    DynamicFieldBean dynamicUrlBean = null;
                    for (DynamicFieldBean field : fields) {
                        if (RepositoryConstants.REPOSITORY_URL.equals(field.getId())) {
                            dynamicUrlBean = field;
                            break;
                        }
                    }
                    if (dynamicUrlBean != null) {
                        dynamicFields.put(RepositoryConstants.REPOSITORY_URL, dynamicUrlBean.getDefaultValue());
                    }
                }
                Map<String, LabelText> map = dynamicControls.get(getRepository());

                for (String fieldKey : map.keySet()) {
                    LabelText current = map.get(fieldKey);
                    String string = connection.getDynamicFields().get(fieldKey);
                    current.setText(string == null ? "" : string); //$NON-NLS-1$
                }

                Map<String, LabelledCombo> map2 = dynamicChoices.get(getRepository());
                for (String fieldKey : map2.keySet()) {
                    Combo combo = map2.get(fieldKey).getCombo();
                    String value = connection.getDynamicFields().get(fieldKey);
                    combo.deselectAll();
                    for (DynamicChoiceBean dynamicChoiceBean : getRepository().getChoices()) {
                        if (dynamicChoiceBean.getId().equals(fieldKey)) {
                            combo.select(dynamicChoiceBean.getChoiceIndex(value));
                        }
                    }
                }

            }
            nameText.setText((connection.getName() == null ? "" : connection.getName())); //$NON-NLS-1$
            descriptionText.setText((connection.getDescription() == null ? "" : connection.getDescription())); //$NON-NLS-1$
            userText.setText((connection.getUser() == null ? "" : connection.getUser())); //$NON-NLS-1$
            String passwordStr = null;
            String originalPassword = connection.getPassword();
            if (StringUtils.isBlank(originalPassword) && StringUtils.isBlank(userText.getText())) {
                passwordStr = ""; //$NON-NLS-1$
            } else {
                passwordStr = TalendTextUtils.hidePassword(connection.getPassword());
            }
            tokenButton.setSelection(connection.isToken());
            tokenText.setEnabled(connection.isToken());
            passwordButton.setSelection(!connection.isToken());
            passwordText.setEnabled(!connection.isToken());
            if (connection.isToken()) {
                tokenText.setText(passwordStr);
            } else {
                passwordText.setText(passwordStr);
            }
            password = connection.getPassword() == null ? "" : connection.getPassword(); // $NON-NLS-1$
            workSpaceText
                    .setText(("".equals(connection.getWorkSpace()) || connection.getWorkSpace() == null) ? getRecentWorkSpace() : connection.getWorkSpace());//$NON-NLS-1$
            addListeners();
        }
    }

    public String getRecentWorkSpace() {
        String filePath = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
        // String defaultPath = Platform.getInstanceLocation().getURL().toString();
        //        String filePath = defaultPath.substring(defaultPath.indexOf("/"), defaultPath.length() - 1); //$NON-NLS-1$
        return filePath;
    }

    /**
     * DOC smallet Comment method "setConnection".
     *
     * @param selected
     */
    public void setConnection(ConnectionBean selected) {
        this.connection = selected;
        fillFields();
        showHideDynamicsControls();
        validateFields();
        showHideTexts();
        changeUserLabel();
    }

    public void deleteProject() {
        DeleteProjectsAsAction deleteProjectAction = new DeleteProjectsAsAction(true);
        deleteProjectAction.run();
    }

    /**
     */
    private final class DelegateSelectionListener implements SelectionListener {

        private DynamicButtonBean bean;

        private DelegateSelectionListener(DynamicButtonBean bean) {
            this.bean = bean;
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // not used.
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            e.data = connection;
            bean.getListener().widgetSelected(e);
        }
    }

    /**
     * DOC smallet LoginComposite class global comment. Detailled comment <br/>
     *
     * $Id: LoginComposite.java 1380 2007-01-10 11:18:55Z smallet $
     *
     */
    private class RepositoryFactoryLabelProvider extends LabelProvider {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            IRepositoryFactory prj = (IRepositoryFactory) element;
            return prj.getName();
        }
    }
}
