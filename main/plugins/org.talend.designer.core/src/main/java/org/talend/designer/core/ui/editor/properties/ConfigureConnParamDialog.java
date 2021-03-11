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
package org.talend.designer.core.ui.editor.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.PathExtractor;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 */
public class ConfigureConnParamDialog extends Dialog {

    private static final String TEXT_CONTROL = "TEXT"; //$NON-NLS-1$

    private static final String CONTEXT_WITH = Messages.getString("ConfigureConnParamDialog.ContextText"); //$NON-NLS-1$

    private static final int LABEL_DEFAULT_X = 103;

    private static final int DEFAULT_HEIGHT = 13;

    private final ConnectionParameters parameters;

    private static IContextManager contextManager;

    private final Map<EConnectionParameterName, String> pvValues = new HashMap<EConnectionParameterName, String>();

    private final IElement selctedElement;

    /**
     * qzhang ConfigureConnParamDialog constructor comment.
     *
     * @param parentShell
     * @param parameters
     * @param contextManager
     */
    public ConfigureConnParamDialog(Shell parentShell, ConnectionParameters parameters, IContextManager contextManager,
            IElement elem) {
        super(parentShell);
        this.parameters = parameters;
        pvValues.put(EConnectionParameterName.PASSWORD, parameters.getPassword());
        pvValues.put(EConnectionParameterName.PORT, parameters.getPort());
        pvValues.put(EConnectionParameterName.SCHEMA, parameters.getSchema());
        pvValues.put(EConnectionParameterName.SERVER_NAME, parameters.getHost());
        pvValues.put(EConnectionParameterName.USERNAME, parameters.getUserName());
        pvValues.put(EConnectionParameterName.SID, parameters.getDbName());
        pvValues.put(EConnectionParameterName.FILE, parameters.getFilename());
        pvValues.put(EConnectionParameterName.DIRECTORY, parameters.getDirectory());
        pvValues.put(EConnectionParameterName.DATASOURCE, parameters.getDatasource());
        pvValues.put(EConnectionParameterName.PROPERTIES_STRING, parameters.getJdbcProperties());
        pvValues.put(EConnectionParameterName.URL, parameters.getUrl());
        pvValues.put(EConnectionParameterName.DRIVER_CLASS, parameters.getDriverClass());
        pvValues.put(EConnectionParameterName.DRIVER_JAR, parameters.getDriverJar());
        ConfigureConnParamDialog.contextManager = contextManager;
        setShellStyle(SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.DIALOG_TRIM);
        this.selctedElement = elem;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("ConfigureConnParamDialog.DialogLabel")); //$NON-NLS-1$
        newShell.setSize(600, 600);
        Rectangle screen = Display.getDefault().getBounds();
        newShell.setLocation(screen.width / 3, screen.height / 3);
    }

    private Combo contextCombo;

    private Composite mainComposite;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 0;

        parentComposite.setLayout(gridLayout);

        IContext defaultContext = createContextComposite(parentComposite);

        // Label hLabel = new Label(parentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        //
        // hLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.V_SCROLL | SWT.BORDER);

        mainComposite = new Composite(scrolledComposite, SWT.NONE);
        gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 0;
        mainComposite.setLayout(gridLayout);

        addComponents(defaultContext);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        scrolledComposite.setLayoutData(gridData);
        scrolledComposite.setContent(mainComposite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        contextCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Text text : allParamText) {
                    if (text != null && !text.isDisposed()) {
                        Object data = text.getData(TEXT_CONTROL);
                        if (data != null && data instanceof Text) {
                            resetValues(text, (Text) data);
                        }
                    }
                }
            }
        });
        return parentComposite;
    }

    private final List<Text> allParamText = new ArrayList<Text>();

    /**
     * qzhang Comment method "addComponents".
     *
     * @param defaultContext
     */
    private void addComponents(IContext defaultContext) {
        for (EConnectionParameterName key : EConnectionParameterName.values()) {

            if (parameters.getRepositoryNameParaName().get(key.getName()) != null) {
                if (key.equals(EConnectionParameterName.FILE)) {
                    createFileComponent(key, defaultContext);
                } else if (key.equals(EConnectionParameterName.DIRECTORY)) {
                    createDirectoryComponent(key, defaultContext);
                } else {
                    createTextComponent(key, defaultContext);
                }
            }
        }
    }

    /**
     * qiang.zhang Comment method "createDirectoryComponent".
     *
     * @param key
     * @param defaultContext
     */
    private void createDirectoryComponent(final EConnectionParameterName key, IContext defaultContext) {
        GridLayout gridLayout;
        GridData gridData;
        Composite hostComposite = new Composite(mainComposite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 2;
        gridLayout.marginTop = 5;
        gridLayout.marginBottom = 2;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        hostComposite.setLayout(gridLayout);
        hostComposite.setLayoutData(gridData);
        Label hostLabel = new Label(hostComposite, SWT.NONE);
        hostLabel.setText(key.getDisplayName() + ":"); //$NON-NLS-1$
        // GridDataFactory.swtDefaults().hint(LABEL_DEFAULT_X, DEFAULT_HEIGHT).applyTo(hostLabel);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        hostLabel.setLayoutData(data);

        Composite fileComposite = new Composite(hostComposite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        fileComposite.setLayout(gridLayout);
        fileComposite.setLayoutData(gridData);

        final Text host = new Text(fileComposite, SWT.BORDER);
        host.setText(pvValues.get(key));
        if (host.getText().trim().length() == 0) {
            host.setBackground(ColorConstants.red);
            host.redraw();
        }
        // GridDataFactory.swtDefaults().hint(TEXT_DEFAULT_X, DEFAULT_HEIGHT).applyTo(host);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        host.setLayoutData(gridData);
        Button button = new Button(fileComposite, SWT.PUSH);
        button.setText("..."); //$NON-NLS-1$
        button.addSelectionListener(new SelectionListener() {

            /*
             * (non-Javadoc)
             *
             * @see
             * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(mainComposite.getShell(), SWT.NONE);
                String path = PathExtractor.extractPath(host.getText());
                dialog.setFilterPath(PathUtils.getOSPath(path));
                String file = dialog.open();
                if (file != null) {
                    if (!file.equals("")) { //$NON-NLS-1$
                        host.setText(PathUtils.getPortablePath(file));
                    }
                }
            }

        });

        final Text hostText = new Text(hostComposite, SWT.NONE);
        hostText.setEditable(false);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        hostText.setLayoutData(gridData);
        hostText.setText(CONTEXT_WITH + ContextParameterUtils.parseScriptContextCode(host.getText(), defaultContext));
        host.setData(TEXT_CONTROL, hostText);
        host.setData(key);
        allParamText.add(host);
        host.addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            @Override
            public void modifyText(ModifyEvent e) {
                if (isRequriedValue(key.getName())) {
                    if (host.getText().trim().length() == 0) {
                        host.setBackground(ColorConstants.red);
                        host.redraw();
                    } else {
                        host.setBackground(ColorConstants.white);
                        host.redraw();
                    }
                    resetValues(host, hostText);

                }
            }

        });
    }

    /**
     * qiang.zhang Comment method "createFileComponent".
     *
     * @param key
     * @param defaultContext
     */
    private void createFileComponent(final EConnectionParameterName key, IContext defaultContext) {
        GridLayout gridLayout;
        GridData gridData;
        Composite hostComposite = new Composite(mainComposite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 2;
        gridLayout.marginTop = 5;
        gridLayout.marginBottom = 2;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        hostComposite.setLayout(gridLayout);
        hostComposite.setLayoutData(gridData);
        Label hostLabel = new Label(hostComposite, SWT.NONE);
        hostLabel.setText(key.getDisplayName() + ":"); //$NON-NLS-1$
        // GridDataFactory.swtDefaults().hint(LABEL_DEFAULT_X, DEFAULT_HEIGHT).applyTo(hostLabel);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        hostLabel.setLayoutData(data);

        Composite fileComposite = new Composite(hostComposite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        fileComposite.setLayout(gridLayout);
        fileComposite.setLayoutData(gridData);

        final Text host = new Text(fileComposite, SWT.BORDER);
        host.setText(pvValues.get(key));
        if (host.getText().trim().length() == 0) {
            host.setBackground(ColorConstants.red);
            host.redraw();
        }
        // GridDataFactory.swtDefaults().hint(TEXT_DEFAULT_X, DEFAULT_HEIGHT).applyTo(host);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        host.setLayoutData(gridData);
        Button button = new Button(fileComposite, SWT.PUSH);
        button.setText("..."); //$NON-NLS-1$
        button.addSelectionListener(new SelectionListener() {

            /*
             * (non-Javadoc)
             *
             * @see
             * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(mainComposite.getShell(), SWT.NONE);
                String path = PathExtractor.extractPath(host.getText());
                dialog.setFileName(PathUtils.getOSPath(path));
                String file = dialog.open();
                if (file != null) {
                    if (!file.equals("")) { //$NON-NLS-1$
                        host.setText(PathUtils.getPortablePath(file));
                    }
                }
            }

        });

        final Text hostText = new Text(hostComposite, SWT.NONE);
        hostText.setEditable(false);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        hostText.setLayoutData(gridData);
        hostText.setText(CONTEXT_WITH + ContextParameterUtils.parseScriptContextCode(host.getText(), defaultContext));
        host.setData(TEXT_CONTROL, hostText);
        host.setData(key);
        allParamText.add(host);
        host.addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            @Override
            public void modifyText(ModifyEvent e) {
                if (isRequriedValue(key.getName())) {
                    if (host.getText().trim().length() == 0) {
                        host.setBackground(ColorConstants.red);
                        host.redraw();
                    } else {
                        host.setBackground(ColorConstants.white);
                        host.redraw();
                    }
                    resetValues(host, hostText);

                }

            }
        });
    }

    private boolean isRequriedValue(String key) {

        if (key == null || selctedElement == null) {
            return false;
        }

        String actualKey = AbstractElementPropertySectionController.connKeyMap.get(key);
        IElementParameter param = selctedElement.getElementParameter(actualKey == null ? key : actualKey);
        return param != null && param.isRequired();
    }

    private void createTextComponent(final EConnectionParameterName key, IContext defaultContext) {
        GridLayout gridLayout;
        GridData gridData;
        Composite hostComposite = new Composite(mainComposite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 2;
        gridLayout.marginTop = 5;
        gridLayout.marginBottom = 2;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;

        gridData = new GridData(GridData.FILL_HORIZONTAL);

        hostComposite.setLayout(gridLayout);
        hostComposite.setLayoutData(gridData);

        Label hostLabel = new Label(hostComposite, SWT.NONE);
        hostLabel.setText(key.getDisplayName() + ":"); //$NON-NLS-1$
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        // GridDataFactory.swtDefaults().hint(LABEL_DEFAULT_X, DEFAULT_HEIGHT).applyTo(hostLabel);
        hostLabel.setLayoutData(data);
        boolean isContext = ContextParameterUtils.isContainContextParam(pvValues.get(key));
        int passStyle = SWT.BORDER;
        if ((key == EConnectionParameterName.PASSWORD) && !isContext) {
            passStyle = SWT.PASSWORD;
        }
        final Text host = new Text(hostComposite, passStyle);
        host.setText(pvValues.get(key));
        if (host.getText().trim().length() == 0) {
            host.setBackground(ColorConstants.red);
            host.redraw();
        }
        // GridDataFactory.swtDefaults().hint(TEXT_DEFAULT_X, DEFAULT_HEIGHT).applyTo(host);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        host.setLayoutData(gridData);

        Composite contextComposite = new Composite(hostComposite, SWT.NULL);
        GridLayout naviCommonCompLayout = new GridLayout(2, false);
        naviCommonCompLayout.marginWidth = 0;
        naviCommonCompLayout.marginHeight = 0;
        contextComposite.setLayout(naviCommonCompLayout);
        contextComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contextComposite.setVisible(isContext);

        final Text hostText = new Text(contextComposite, SWT.NONE);
        hostText.setEditable(false);
        gridData = new GridData();
        hostText.setLayoutData(gridData);
        hostText.setText(CONTEXT_WITH);

        passStyle = SWT.NONE;
        if (key == EConnectionParameterName.PASSWORD) {
            passStyle = SWT.PASSWORD;
        }
        final Text hostValue = new Text(contextComposite, passStyle);
        hostValue.setEditable(false);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        hostValue.setLayoutData(gridData);
        hostValue.setText(ContextParameterUtils.parseScriptContextCode(host.getText(), defaultContext));
        host.setData(TEXT_CONTROL, hostValue);
        host.setData(key);
        allParamText.add(host);
        host.addKeyListener(new KeyAdapter() {

            /*
             * c (non-Javadoc)
             *
             * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            @Override
            public void keyReleased(KeyEvent e) {
                if (host.getText().trim().length() == 0) {
                    host.setBackground(ColorConstants.red);
                    host.redraw();
                } else {
                    host.setBackground(ColorConstants.white);
                    host.redraw();
                }
                resetValues(host, hostValue);
            }

        });
    }

    /**
     * qzhang Comment method "createContextComposite".
     *
     * @return
     */
    private IContext createContextComposite(Composite parent) {
        Composite contextComposite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        contextComposite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalAlignment = GridData.END;
        contextComposite.setLayoutData(gridData);
        Label contextLabel = new Label(contextComposite, SWT.NONE);
        contextLabel.setText(Messages.getString("ConfigureConnParamDialog.ContextLabel")); //$NON-NLS-1$
        contextCombo = new Combo(contextComposite, SWT.PUSH);
        GridDataFactory.swtDefaults().hint(LABEL_DEFAULT_X, DEFAULT_HEIGHT).applyTo(contextCombo);
        contextCombo.setItems(getContextNames());
        contextCombo.select(0);
        IContext defaultContext = contextManager.getDefaultContext();
        return defaultContext;
    }

    /**
     * qzhang Comment method "getContextNames".
     *
     * @return
     */
    private String[] getContextNames() {
        List<String> items = new ArrayList<String>();
        if (contextManager != null) {
            final List<IContext> listContext = contextManager.getListContext();
            if (listContext.size() > 0) {
                items.add(contextManager.getDefaultContext().getName());
                for (int i = 0; i < listContext.size(); i++) {
                    IContext context = listContext.get(i);
                    if (!context.getName().equals(items.get(0))) {
                        items.add(context.getName());
                    }
                }
            }
        }
        return items.toArray(new String[] {});
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        for (Text text : allParamText) {
            EConnectionParameterName name = (EConnectionParameterName) text.getData();
            String value = ((Text) text.getData(TEXT_CONTROL)).getText();
            switch (name) {
            case SID:
                parameters.setDbName(value);
                break;
            case DATASOURCE:
                parameters.setDatasource(value);
                break;
            case PASSWORD:
                parameters.setPassword(value);
                break;
            case PORT:
                parameters.setPort(value);
                break;
            case SCHEMA:
                parameters.setSchema(value);
                break;
            case SERVER_NAME:
                parameters.setHost(value);
                break;
            case USERNAME:
                parameters.setUserName(value);
                break;
            case DIRECTORY:
                parameters.setDirectory(value);
                break;
            case FILE:
                parameters.setFilename(value);
                break;
            case PROPERTIES_STRING:
                parameters.setJdbcProperties(value);
                break;
            default:

            }
        }
        super.okPressed();
    }

    /**
     * qzhang Comment method "resetValues".
     *
     * @param host
     */
    private void resetValues(final Text key, final Text value) {
        final IContext context = contextManager.getContext(contextCombo.getItem(contextCombo.getSelectionIndex()));
        value.setText(ContextParameterUtils.parseScriptContextCode(key.getText(), context));
    }

}
