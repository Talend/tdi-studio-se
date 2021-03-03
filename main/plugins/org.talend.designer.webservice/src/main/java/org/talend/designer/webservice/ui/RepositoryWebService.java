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
package org.talend.designer.webservice.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.AbstractWebService;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.designer.webservice.WebServiceComponent;
import org.talend.designer.webservice.WebServiceComponentMain;
import org.talend.designer.webservice.data.ExternalWebServiceUIProperties;
import org.talend.designer.webservice.i18n.Messages;
import org.talend.designer.webservice.ws.WSDLDiscoveryHelper;
import org.talend.designer.webservice.ws.wsdlinfo.Function;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;
import org.talend.designer.webservice.ws.wsdlinfo.PortNames;
import org.talend.designer.webservice.ws.wsdlutil.ServiceHelperConfiguration;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class RepositoryWebService implements AbstractWebService {

    private Composite uiParent;

    private WebServiceComponent connector;

    private String URLValue;

    private List<Function> allfunList = new ArrayList<Function>();

    private List<PortNames> allPortNames = new ArrayList<PortNames>();

    private LabelledFileField wsdlField;

    private Button refreshbut;

    private Composite wsdlComposite;

    private Boolean isFirst = true;

    private AbstractDataTableEditorView<Function> listTableView;

    private AbstractDataTableEditorView<PortNames> portListTableView;

    private Table listTable;

    private Table portListTable;

    private Label operationLabel;

    private ServiceHelperConfiguration serverConfig = null;

    private Label portNameLabel;

    private Function currentFunction;

    private PortNames currentPortName;

    private WSDLSchemaConnection connection;

    public RepositoryWebService(Composite uiParent, WebServiceComponentMain webServiceMain, ConnectionItem connectionItem) {
        this.uiParent = uiParent;
        this.connector = webServiceMain.getWebServiceComponent();
        this.connection = (WSDLSchemaConnection) connectionItem.getConnection();
        URLValue = new String();
        initWebserviceUI();
        ctrate();
        getLastFunction();
    }

    private void initWebserviceUI() {
        IElementParameter METHODPara = connector.getElementParameter("METHOD"); //$NON-NLS-1$
        Object obj = METHODPara.getValue();
        if (obj == null) {
            return;
        }
        if (obj != null && obj instanceof String && !"".equals(obj)) {
            String currentURL = (String) connector.getElementParameter("PORT_NAME").getValue(); //$NON-NLS-1$

            PortNames retrivePortName = new PortNames();
            retrivePortName.setPortName(currentURL);
            allPortNames.clear();
            allPortNames.add(retrivePortName);
            retrivePortName.setPortName(currentURL);

            Function fun = new Function(obj.toString());
            allfunList.clear();
            allfunList.add(fun);
            if (fun != null) {
                currentFunction = fun;
            }
        }
    }

    private void getLastFunction() {
        IElementParameter METHODPara = connector.getElementParameter("METHOD"); //$NON-NLS-1$
        Object obj = METHODPara.getValue();
        if (obj == null) {
            return;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            String wsdlUrl = (String) connector.getElementParameter("ENDPOINT").getValue(); //$NON-NLS-1$
            String currentURL = (String) connector.getElementParameter("PORT_NAME").getValue(); //$NON-NLS-1$
            WSDLDiscoveryHelper ws = new WSDLDiscoveryHelper();
            List<Function> funList = new ArrayList<Function>();

            WebServiceComponent webServiceComponent = connector;
            boolean isUseProxy = webServiceComponent.getElementParameter("USE_PROXY").getValue().toString().equals("true");
            boolean isUseAuth = webServiceComponent.getElementParameter("NEED_AUTH").getValue().toString().equals("true");
            boolean isUseNTLM = webServiceComponent.getElementParameter("USE_NTLM").getValue().toString().equals("true");
            boolean isUseSSL = webServiceComponent.getElementParameter("NEED_SSL_TO_TRUSTSERVER").getValue().toString()
                    .equals("true");

            if (isUseProxy) {
                useProxy();
            }

            if (isUseAuth && !isUseNTLM) {
                useAuth();
            }

            if (isUseSSL) {
                useSSL();
            }

            if (serverConfig != null) {
                if (wsdlUrl != null && !wsdlUrl.contains("\"")) {
                    funList = ws.getFunctionsAvailable(parseContextParameter(wsdlUrl), serverConfig);
                } else {
                    funList = ws.getFunctionsAvailable(wsdlUrl, serverConfig);
                }
            } else {
                if (wsdlUrl != null && !wsdlUrl.contains("\"")) {
                    funList = ws.getFunctionsAvailable(parseContextParameter(wsdlUrl));
                } else {
                    funList = ws.getFunctionsAvailable(wsdlUrl);
                }
            }

            PortNames retrivePortName = new PortNames();
            retrivePortName.setPortName(currentURL);
            allPortNames.clear();
            allPortNames.add(retrivePortName);

            for (Function fun : funList) {
                if (fun.getName().equals(str)) {
                    allfunList.clear();
                    allfunList.add(fun);
                    if (fun != null) {
                        currentFunction = fun;
                    }
                    return;
                }
            }

        }

    }

    public void ctrate() {
        wsdlComposite = new Composite(uiParent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 20;
        layout.marginHeight = 20;
        wsdlComposite.setLayout(layout);
        wsdlComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // WSDL URL
        Composite wsdlUrlcomposite = new Composite(wsdlComposite, SWT.NONE);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.verticalIndent = 2;
        layoutData.verticalSpan = 1;
        wsdlUrlcomposite.setLayoutData(layoutData);
        layout = new GridLayout(4, false);
        wsdlUrlcomposite.setLayout(layout);

        wsdlField = new LabelledFileField(wsdlUrlcomposite, ExternalWebServiceUIProperties.FILE_LABEL,
                ExternalWebServiceUIProperties.FILE_EXTENSIONS, 1, SWT.BORDER) {

            @Override
            protected void setFileFieldValue(String result) {
                if (result != null) {
                    getTextControl().setText(TalendTextUtils.addQuotes(PathUtils.getPortablePath(result)));
                    getDataFromNet();

                    isFirst = false;
                }
            }

        };
        wsdlField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                // TODO Auto-generated method stub
                URLValue = wsdlField.getText();
                connection.setWSDL(URLValue);
            }
        });
        // add a listener for ctrl+space.
        TalendProposalUtils.installOn(wsdlField.getTextControl(), connector.getProcess(), connector);
        String wsdlUrl = (String) connector.getElementParameter("ENDPOINT").getValue(); //$NON-NLS-1$
        if (wsdlUrl != null) {
            wsdlField.setText(wsdlUrl);
        }

        refreshbut = new Button(wsdlUrlcomposite, SWT.PUSH | SWT.CENTER);
        refreshbut.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        GridData butData = new GridData();
        butData.verticalSpan = 1;
        refreshbut.setLayoutData(butData);

        // add port name UI
        Composite wsdlPortcomposite = new Composite(wsdlComposite, SWT.NONE);
        GridData portlayoutData = new GridData(GridData.FILL_HORIZONTAL);
        portlayoutData.verticalIndent = 2;
        portlayoutData.verticalSpan = 3;
        wsdlPortcomposite.setLayoutData(portlayoutData);
        layout = new GridLayout(2, false);
        layout.verticalSpacing = 1;
        wsdlPortcomposite.setLayout(layout);

        portNameLabel = new Label(wsdlPortcomposite, SWT.NONE);
        portNameLabel.setText(Messages.getString("WebServiceUI.Port")); //$NON-NLS-1$
        GridData portLabelGridData = new GridData();
        portLabelGridData.verticalAlignment = SWT.TOP;
        portNameLabel.setLayoutData(portLabelGridData);

        Composite portTabComposite = new Composite(wsdlPortcomposite, SWT.BORDER);
        portTabComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        portTabComposite.setLayout(new FillLayout());

        ExtendedTableModel<PortNames> portModel = new ExtendedTableModel<PortNames>("PORTNAMELIST", allPortNames); //$NON-NLS-1$
        portListTableView = new AbstractDataTableEditorView<PortNames>(portTabComposite, SWT.NONE, portModel, false, true, false) {

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<PortNames> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(false);
                newTableViewerCreator.setVerticalScroll(true);
                newTableViewerCreator.setReadOnly(true);
            }

            @Override
            protected void createColumns(TableViewerCreator<PortNames> tableViewerCreator, Table table) {
                TableViewerCreatorColumn rowColumn = new TableViewerCreatorColumn(tableViewerCreator);
                rowColumn.setTitle(Messages.getString("WebServiceUI.COLUMN")); //$NON-NLS-1$
                rowColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<PortNames, String>() {

                    public String get(PortNames bean) {
                        return bean.getPortName();
                    }

                    public void set(PortNames bean, String value) {
                        bean.setPortName(value);
                    }

                });
                rowColumn.setWeight(60);
                rowColumn.setModifiable(true);
                rowColumn.setMinimumWidth(60);
                rowColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));

            }
        };

        // WSDL Operation
        Composite wsdlOperationcomposite = new Composite(wsdlComposite, SWT.NONE);
        GridData operationlayoutData = new GridData(GridData.FILL_BOTH);
        operationlayoutData.verticalIndent = 2;
        operationlayoutData.verticalSpan = 5;
        wsdlOperationcomposite.setLayoutData(operationlayoutData);
        layout = new GridLayout(2, false);
        layout.verticalSpacing = 3;
        wsdlOperationcomposite.setLayout(layout);
        // wsdlOperationcomposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        operationLabel = new Label(wsdlOperationcomposite, SWT.NONE);
        operationLabel.setText(Messages.getString("WebServiceUI.Operation")); //$NON-NLS-1$
        GridData opertionLabelGridData = new GridData();
        opertionLabelGridData.verticalAlignment = SWT.TOP;
        operationLabel.setLayoutData(opertionLabelGridData);

        Composite tabComposite = new Composite(wsdlOperationcomposite, SWT.BORDER);
        GridData tabGridData = new GridData(GridData.FILL_BOTH);
        // tabGridData.verticalSpan = 3;
        tabComposite.setLayoutData(tabGridData);
        tabComposite.setLayout(new FillLayout());

        ExtendedTableModel<Function> funModel = new ExtendedTableModel<Function>("FUNCTIONLIST", allfunList); //$NON-NLS-1$
        listTableView = new AbstractDataTableEditorView<Function>(tabComposite, SWT.NONE, funModel, false, true, false) {

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<Function> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(false);
                newTableViewerCreator.setVerticalScroll(true);
                newTableViewerCreator.setReadOnly(true);
            }

            @Override
            protected void createColumns(TableViewerCreator<Function> tableViewerCreator, Table table) {
                TableViewerCreatorColumn rowColumn = new TableViewerCreatorColumn(tableViewerCreator);
                rowColumn.setTitle(Messages.getString("WebServiceUI.COLUMN")); //$NON-NLS-1$
                rowColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<Function, String>() {

                    public String get(Function bean) {
                        return bean.getName();
                    }

                    public void set(Function bean, String value) {
                        bean.setName(value);

                    }

                });
                rowColumn.setWeight(60);
                rowColumn.setModifiable(true);
                rowColumn.setMinimumWidth(60);
                rowColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));

            }
        };

        addListenerForWSDLCom();

    }

    private void addListenerForWSDLCom() {
        refreshbut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getDisplay()
                        .getActiveShell().getShell());
                IRunnableWithProgress runnable = new IRunnableWithProgress() {

                    public void run(final IProgressMonitor monitor) {
                        monitor.beginTask("Retrieve WSDL parameter from net.", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

                        Display.getDefault().syncExec(new Runnable() {

                            public void run() {
                                getDataFromNet();
                            }
                        });
                        monitor.done();
                    }
                };
                try {
                    progressDialog.run(true, true, runnable);
                } catch (InvocationTargetException e1) {
                    ExceptionHandler.process(e1);
                } catch (InterruptedException e1) {
                    ExceptionHandler.process(e1);
                } catch (WebServiceCancelException e1) {
                    return;
                }
                if (currentPortName != null) {
                    connection.setPortName(currentPortName.getPortName());

                } else if (currentPortName == null && allPortNames != null) {
                    currentPortName = allPortNames.get(0);
                    connection.setPortName(currentPortName.getPortName());
                }

                listTable.setSelection(listTable.getItem(0));
                if (currentFunction != null) {
                    connection.setMethodName(currentFunction.getName());
                    connection.setServerNameSpace(currentFunction.getServerNameSpace());
                    connection.setServerName(currentFunction.getServerName());
                    connection.setPortNameSpace(currentFunction.getServerNameSpace());
                }
                // listTable.select(0);
                isFirst = false;
            }
        });
        // TableItem firstItem = listTable.getItem(0);
        // currentFunction = firstItem.getData();
        listTable = listTableView.getTable();
        portListTable = portListTableView.getTable();

        listTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] item = listTable.getSelection();
                currentFunction = (Function) item[0].getData();
                if (currentFunction != null) {
                    connection.setServerName(currentFunction.getServerName());
                    connection.setServerNameSpace(currentFunction.getServerNameSpace());
                    connection.setMethodName(currentFunction.getName());
                }
                // if select the same as before ,don't change it
                // IElementParameter METHODPara = connector.getElementParameter("METHOD"); //$NON-NLS-1$
                // Object obj = METHODPara.getValue();
                // if (currentFunction.getName().equals(obj.toString())) {
                // return;
                // }
                List<ParameterInfo> listIn = currentFunction.getInputParameters();
                List<ParameterInfo> listOut = currentFunction.getOutputParameters();

            }
        });

        portListTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] item = portListTable.getSelection();
                currentPortName = (PortNames) item[0].getData();
                connection.setPortName(currentPortName.getPortName());
            }
        });
    }

    private void getDataFromNet() {
        List<Function> funList = new ArrayList<Function>();
        List<PortNames> portNameList = new ArrayList<PortNames>();
        URLValue = wsdlField.getText();
        if (URLValue == null) {
            URLValue = ""; //$NON-NLS-1$
        }
        final WSDLDiscoveryHelper ws = new WSDLDiscoveryHelper();

        WebServiceComponent webServiceComponent = connector;
        boolean isUseProxy = webServiceComponent.getElementParameter("USE_PROXY").getValue().toString().equals("true");
        boolean isUseNTLM = webServiceComponent.getElementParameter("USE_NTLM").getValue().toString().equals("true");
        boolean isUseAuth = webServiceComponent.getElementParameter("NEED_AUTH").getValue().toString().equals("true");

        if (isUseProxy) {
            useProxy();
        }

        if (isUseAuth && !isUseNTLM) {
            useAuth();
        }

        boolean isUseSSL = webServiceComponent.getElementParameter("NEED_SSL_TO_TRUSTSERVER").getValue().toString()
                .equals("true");
        if (isUseSSL) {
            useSSL();
        }

        if (serverConfig != null) {
            if (URLValue != null && !URLValue.contains("\"")) {
                funList = ws.getFunctionsAvailable(parseContextParameter(URLValue), serverConfig);
            } else {
                funList = ws.getFunctionsAvailable(URLValue, serverConfig);
            }

        } else {
            if (URLValue != null && !URLValue.contains("\"")) {
                funList = ws.getFunctionsAvailable(parseContextParameter(URLValue));
            } else {
                funList = ws.getFunctionsAvailable(URLValue);
            }
        }

        if (!funList.isEmpty()) {
            if (funList.get(0) != null) {
                if (funList.get(0).getPortNames() != null) {
                    portNameList = funList.get(0).getPortNames();
                }
            }
        }
        ExtendedTableModel<Function> listModel = listTableView.getExtendedTableModel();
        ExtendedTableModel<PortNames> portListModel = portListTableView.getExtendedTableModel();
        listModel.removeAll();
        listModel.addAll(funList);
        portListModel.removeAll();
        portListModel.addAll(portNameList);

        connection.setPortName(portNameList.get(0).getPortName());
    }

    private ServiceHelperConfiguration useProxy() {
        String proxyHost = "";
        String proxyPort = "";
        String proxyUser = "";
        String proxyPassword = "";
        IElementParameter proxyHostParameter = connector.getElementParameter("PROXY_HOST");
        IElementParameter proxyPortParameter = connector.getElementParameter("PROXY_PORT");
        IElementParameter proxyUserParameter = connector.getElementParameter("PROXY_USERNAME");
        IElementParameter proxyPasswordParameter = connector.getElementParameter("PROXY_PASSWORD");

        if (proxyHostParameter.getValue() != null) {
            proxyHost = TalendTextUtils.removeQuotes(proxyHostParameter.getValue().toString());
        }
        if (proxyPortParameter.getValue() != null) {
            proxyPort = TalendTextUtils.removeQuotes(proxyPortParameter.getValue().toString());
        }
        if (proxyUserParameter.getValue() != null) {
            proxyUser = TalendTextUtils.removeQuotes(proxyUserParameter.getValue().toString());
        }
        if (proxyPasswordParameter.getValue() != null) {
            proxyPassword = TalendTextUtils.removeQuotes(proxyPasswordParameter.getValue().toString());
        }
        if (serverConfig == null) {
            serverConfig = new ServiceHelperConfiguration();
        }
        serverConfig.setUsername(proxyHost);
        serverConfig.setPassword(proxyPort);
        serverConfig.setUsername(proxyUser);
        serverConfig.setPassword(proxyPassword);

        return serverConfig;

    }

    private ServiceHelperConfiguration useAuth() {

        String authUsername = "";
        String authPassword = "";

        IElementParameter authUsernameParameter = connector.getElementParameter("AUTH_USERNAME");
        IElementParameter authPasswordParameter = connector.getElementParameter("AUTH_PASSWORD");
        if (authUsernameParameter.getValue() != null) {
            authUsername = authUsernameParameter.getValue().toString();
            authUsername = TalendTextUtils.removeQuotes(authUsername);
        }
        if (authPasswordParameter.getValue() != null) {
            authPassword = authPasswordParameter.getValue().toString();
            authPassword = TalendTextUtils.removeQuotes(authPassword);
        }
        if (serverConfig == null) {
            serverConfig = new ServiceHelperConfiguration();
        }
        serverConfig.setUsername(authUsername);
        serverConfig.setPassword(authPassword);

        return serverConfig;

    }

    private void useSSL() {

        String trustStoreFile = "";
        String trustStorePassword = "";

        IElementParameter trustserverFileParameter = connector.getElementParameter("SSL_TRUSTSERVER_TRUSTSTORE");
        IElementParameter trustserverPasswordParameter = connector.getElementParameter("SSL_TRUSTSERVER_PASSWORD");
        if (trustserverFileParameter.getValue() != null) {
            trustStoreFile = trustserverFileParameter.getValue().toString();
            trustStoreFile = TalendTextUtils.removeQuotes(trustStoreFile);
        }
        if (trustserverPasswordParameter.getValue() != null) {
            trustStorePassword = trustserverPasswordParameter.getValue().toString();
            trustStorePassword = TalendTextUtils.removeQuotes(trustStorePassword);
        }

        System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

    }

    private String parseContextParameter(String contextValue) {
        String url = "";
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IContextManager contextManager = connector.getProcess().getContextManager();
        String currentDefaultName = contextManager.getDefaultContext().getName();
        List contextList = contextManager.getListContext();
        if (!contextList.isEmpty() && contextList.size() > 1) {
            currentDefaultName = ConnectionContextHelper.getContextTypeForJob(shell, contextManager, false);
        }
        // ContextSetsSelectionDialog cssd=new ContextSetsSelectionDialog(shell,,false);
        // ContextType contextType=ConnectionContextHelper.getContextTypeForContextMode(connector);
        IContext context = contextManager.getContext(currentDefaultName);
        url = ContextParameterUtils.parseScriptContextCode(contextValue, context);

        return url;
    }

    public String getURL() {
        return URLValue;
    }

    public Function getCurrentFunction() {
        return currentFunction;
    }

    public PortNames getCurrentPortName() {
        return currentPortName;
    }

    public List<PortNames> getAllPortNames() {
        return this.allPortNames;
    }

    public Table getTable() {
        return listTableView.getTable();
    }
}
