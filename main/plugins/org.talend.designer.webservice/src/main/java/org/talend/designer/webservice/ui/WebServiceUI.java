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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.drawing.background.BackgroundRefresher;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.linking.TableToTablesLinker;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.CELL_EDITOR_STATE;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.celleditor.DialogErrorForCellEditorListener;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.WSDLParameter;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.AbstractWebService;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.webservice.WebServiceComponent;
import org.talend.designer.webservice.WebServiceComponentMain;
import org.talend.designer.webservice.data.ExternalWebServiceUIProperties;
import org.talend.designer.webservice.data.InputMappingData;
import org.talend.designer.webservice.data.OutPutMappingData;
import org.talend.designer.webservice.i18n.Messages;
import org.talend.designer.webservice.managers.UIManager;
import org.talend.designer.webservice.managers.WebServiceManager;
import org.talend.designer.webservice.ui.dialog.AddListDialog;
import org.talend.designer.webservice.ui.dialog.InputIndexValidator;
import org.talend.designer.webservice.ui.dialog.WSDLSchemaDialog;
import org.talend.designer.webservice.ui.dialog.WebServiceDialog;
import org.talend.designer.webservice.ui.dialog.WebServiceEventListener;
import org.talend.designer.webservice.ui.dnd.DragAndDropForWebService;
import org.talend.designer.webservice.ui.link.WebServiceTableLiner;
import org.talend.designer.webservice.ws.WSDLDiscoveryHelper;
import org.talend.designer.webservice.ws.wsdlinfo.Function;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;
import org.talend.designer.webservice.ws.wsdlinfo.PortNames;
import org.talend.designer.webservice.ws.wsdlutil.ServiceHelperConfiguration;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.ProjectManager;

/**
 * gcui class global comment. Detailled comment
 */
public class WebServiceUI implements AbstractWebService {

    protected int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore()
            .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT);

    private WebServiceManager webServiceManager;

    private WebServiceDialog webServiceDialog;

    private Composite uiParent;

    private LabelledFileField wsdlField;

    private Label operationLabel;

    private Label portNameLabel;

    private CTabFolder tabFolder;

    private CTabItem wsdlTabItem;

    private CTabItem inputMappingTabItem;

    private CTabItem outputMappingTabItem;

    private Composite outputComposite;

    private String inComeName;

    private Composite inputComposite;

    private Composite wsdlComposite;

    private SashForm allContentForm;

    private TableToTablesLinker<Object, Object> tabTotabLinkForin;

    private TableToTablesLinker<Object, Object> tabTotabLinkForout;

    private BackgroundRefresher backgroundRefresher;

    private AbstractDataTableEditorView<Function> listTableView;

    private AbstractDataTableEditorView<PortNames> portListTableView;

    private AbstractDataTableEditorView<IMetadataColumn> columnInPutTableView;

    private AbstractDataTableEditorView<OutPutMappingData> rowoutPutTableView;

    private AbstractDataTableEditorView<InputMappingData> expressinPutTableView;

    private AbstractDataTableEditorView<OutPutMappingData> expressoutPutTableView;

    private WebServiceEventListener listener;

    private Button refreshbut;

    private Table listTable;

    private Table portListTable;

    private Button addListButForIn;

    private Button removeButForIn;

    private Button removeButForout;

    private Button normalizeButForIn;

    private Button denorButForIn;

    private Button addListButForOut;

    private Button normalizeButForOut;

    private Button denorButForOut;

    private Button schemaButton;

    private Button inputschemaButton;

    private Button autoMapButtonForIn;

    private Button autoMapButtonForOut;

    private Table expressTableForIn;

    private Table expressTableForout;

    private Table rowTableForout;

    private Table rowTableForin;

    private AddListDialog dialogInputList;

    private WebServiceComponent connector;

    private static int DEFAULT_INDEX = 0;

    private int selectedColumnIndex = DEFAULT_INDEX;

    private List<Function> allfunList = new ArrayList<Function>();

    private List<PortNames> allPortNames = new ArrayList<PortNames>();

    // private List<ParameterInfo> inParaList = new ArrayList<ParameterInfo>();

    private List<OutPutMappingData> outParaList = new ArrayList<OutPutMappingData>();

    private List<InputMappingData> inputMappingList = new ArrayList<InputMappingData>();

    private List<IMetadataColumn> inPutcolumnList = new ArrayList<IMetadataColumn>();

    private List<OutPutMappingData> outPutcolumnList = new ArrayList<OutPutMappingData>();

    private List<OutPutMappingData> outputColumnCompareList = new ArrayList<OutPutMappingData>();

    private String URLValue;

    private InputMappingData currentInputMappingData;

    private OutPutMappingData currentOutputMappingData;

    private ParameterInfo currentSelectedInChildren;

    private List<ParameterInfo> multiSelectedInChildren;

    private ParameterInfo currentOutputParameter;

    private ParameterInfo currentSelectedOutChildren;

    private OutPutMappingData currentSelectedOutExpress;

    private Function currentFunction;

    private PortNames currentPortName;

    private List<IMetadataColumn> forOutColumnList = new ArrayList<IMetadataColumn>();

    private int currentElementIndexForIn = -1;

    private int currentElementIndexForOut = -1;

    private int currentIndexForOutExpress = -1;

    private ServiceHelperConfiguration serverConfig = null;

    private Boolean isOutPutArray = false;

    private IMetadataTable inputMetaCopy = null;

    private IMetadataTable outputMetaCopy = null;

    private IMetadataTable inputMetadata = null;

    private IMetadataTable outputMetadata = null;

    private IMetadataTable temInputMetadata = null;

    private IMetadataTable temOutputMetadata = null;

    private INode outputNode = null;

    private INode inputNode = null;

    private Boolean isFirst = true;

    private WSDLSchemaConnection connection = null;

    private ConnectionItem connectionItem;

    private List<Function> funList = new ArrayList<Function>();

    private List<PortNames> portNameList = new ArrayList<PortNames>();

    private String url = "";

    public WebServiceUI(Composite uiParent, WebServiceComponentMain webServiceMain) {
        super();
        this.uiParent = uiParent;
        this.webServiceDialog = webServiceMain.getDialog();
        this.webServiceManager = webServiceMain.getWebServiceManager();
        this.connector = webServiceMain.getWebServiceComponent();
        URLValue = new String();
        // getInConnList();
        // getOutConnList();
        // getLastFunction();
        initWebserviceUI();
        // getInputElementList();
        // getOutputElementList();
        initInputMetaCopy();
        initOutputMetaCopy();
    }

    public WebServiceUI(Composite uiParent, WebServiceComponentMain webServiceMain, ConnectionItem connectionItem) {
        super();
        this.uiParent = uiParent;
        this.webServiceDialog = webServiceMain.getDialog();
        this.webServiceManager = webServiceMain.getWebServiceManager();
        this.connector = webServiceMain.getWebServiceComponent();
        this.connection = (WSDLSchemaConnection) connectionItem.getConnection();
        this.connectionItem = connectionItem;
        URLValue = new String();
        // getInConnList();
        // getOutConnList();
        // getLastFunction();
        initWebserviceUI();
        // getInputElementList();
        // getOutputElementList();
        initInputMetaCopy();
        initOutputMetaCopy();
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
            initwebServiceMappingData(currentURL);
        }
    }

    private void initwebServiceMappingData(String currentURL) {
        if (currentURL != null && !currentURL.equals("")) {
            isFirst = false;
            // getLastFunction();
            Function fun = new Function(currentURL);
            IElementParameter METHODPara = this.connector.getElementParameter("METHOD");
            Object obj = METHODPara.getValue();
            if (obj == null) {
                return;
            }
            if (obj instanceof String) {
                String str = (String) obj;
                fun.setName(str);
            }
            // fun.se
            ParameterInfo para = new ParameterInfo();
            IElementParameter OUTPUT_PARAMSPara = connector.getElementParameter("OUTPUT_PARAMS"); //$NON-NLS-1$
            List<Map<String, String>> outputMap = (List<Map<String, String>>) OUTPUT_PARAMSPara.getValue();
            List<ParameterInfo> paraoutList = new ArrayList<ParameterInfo>();
            ParameterInfo paraout = new ParameterInfo();
            for (Map<String, String> map : outputMap) {
                if (map.get("PARAMETERINFO") != null && map.get("PARAMETERINFO") instanceof String) {
                    String ele = map.get("PARAMETERINFO"); //$NON-NLS-1$
                    int arraySize = 0;
                    String paraIndex = null;
                    if (map.get("PARAMETERARRAYSIZE") != null) {
                        if (!map.get("PARAMETERARRAYSIZE").equals("")) {
                            arraySize = Integer.valueOf(map.get("PARAMETERARRAYSIZE"));
                        }
                    }
                    if (map.get("PARAMETERINDEX") != null && !map.get("PARAMETERINDEX").equals("")) {
                        paraIndex = map.get("PARAMETERINDEX");
                    }
                    if (!ele.equals("")) {
                        if (!map.get("PARAPARENT").equals("")) {
                            String paraParent = map.get("PARAPARENT");
                            // if (paraParent.equals(ele)) {
                            // ParameterInfo parain = new ParameterInfo();
                            // parain.setName(ele);
                            // } else {
                            for (int i = paraoutList.size() - 1; i >= 0; i--) {
                                ParameterInfo para2 = paraoutList.get(i);
                                if (paraParent.equals(para2.getName())) {
                                    // para.setParent(para2);
                                    ParameterInfo parain = new ParameterInfo();
                                    parain.setName(ele);
                                    // bug 22801:After import tWebService job,missing [*] when added new Element
                                    parain.setArraySize(arraySize);
                                    parain.setIndex(paraIndex);
                                    para2.getParameterInfos().add(parain);
                                    parain.setParent(para2);
                                    paraoutList.add(parain);
                                    break;
                                }
                            }
                            // }
                        } else {
                            paraout = new ParameterInfo();
                            paraout.setName(ele);
                            paraoutList.add(paraout);
                        }

                    }
                }
            }
            fun.setOutputParameters(paraoutList);
            for (Map<String, String> map : outputMap) {
                if (map.get("ELEMENT") != null && map.get("ELEMENT") instanceof String) {
                    String ele = map.get("ELEMENT"); //$NON-NLS-1$
                    if (!ele.equals("")) {
                        OutPutMappingData data = new OutPutMappingData();
                        if (para.getName() == null) {
                            para.setName(ele);
                            para.getParameterInfos().add(new ParameterInfo());
                            if (!paraoutList.isEmpty()) {
                                data.setParameter(paraoutList.get(0));
                            } else {
                                data.setParameter(new ParameterInfo());
                            }
                            data.setParameterName(ele);
                        } else {
                            ParameterInfo para2 = new ParameterInfo();
                            para2.setParent(para);
                            para2.setName(ele);
                            if (para.getParameterInfos().get(0).getName() == null) {
                                para.getParameterInfos().clear();
                            }
                            para.getParameterInfos().add(para2);
                            data.setParameter(para2);
                            data.setParameterName(ele);
                        }
                        outParaList.add(data);
                    }
                }
            }

            ParameterInfo inpara = new ParameterInfo();
            List<ParameterInfo> paraList = new ArrayList<ParameterInfo>();
            ParameterInfo paraiun = new ParameterInfo();
            IElementParameter INPUT_PARAMSPara = connector.getElementParameter("INPUT_PARAMS"); //$NON-NLS-1$
            List<Map<String, String>> inputparaValue = (List<Map<String, String>>) INPUT_PARAMSPara.getValue();
            for (Map<String, String> map : inputparaValue) {
                if (map.get("PARAMETERINFO") != null && map.get("PARAMETERINFO") instanceof String) {
                    String ele = map.get("PARAMETERINFO"); //$NON-NLS-1$
                    int arraySize = 0;
                    String paraIndex = null;
                    if (map.get("PARAMETERARRAYSIZE") != null) {
                        if (!map.get("PARAMETERARRAYSIZE").equals("")) {
                            arraySize = Integer.valueOf(map.get("PARAMETERARRAYSIZE"));
                        }
                    }
                    if (map.get("PARAMETERINDEX") != null && !map.get("PARAMETERINDEX").equals("")) {
                        paraIndex = map.get("PARAMETERINDEX");
                    }
                    if (!ele.equals("")) {
                        if (!map.get("PARAPARENT").equals("")) {
                            String paraParent = map.get("PARAPARENT");
                            if (paraParent.equals(ele)) {
                                ParameterInfo parain = new ParameterInfo();
                                parain.setName(ele);
                            } else {
                                for (int i = 0; i < paraList.size(); i++) {
                                    ParameterInfo para2 = paraList.get(i);
                                    if (paraParent.equals(para2.getName())) {
                                        // para.setParent(para2);
                                        ParameterInfo parain = new ParameterInfo();
                                        parain.setName(ele);
                                        // bug 22801:After import tWebService job,missing [*] when added new Element
                                        parain.setArraySize(arraySize);
                                        parain.setIndex(paraIndex);
                                        para2.getParameterInfos().add(parain);
                                        parain.setParent(para2);
                                        paraList.add(parain);
                                    }
                                }
                            }
                        } else {
                            paraiun = new ParameterInfo();
                            paraiun.setName(ele);
                            paraList.add(paraiun);
                        }

                    }
                }
            }
            fun.setInputParameters(paraList);
            for (Map<String, String> map : inputparaValue) {
                InputMappingData data = new InputMappingData();
                if (map.get("EXPRESSION") != null && map.get("EXPRESSION") instanceof String) {
                    String expr = map.get("EXPRESSION");
                    data.setInputColumnValue(expr);
                }

                if (map.get("ELEMENT") != null && map.get("ELEMENT") instanceof String && !map.get("ELEMENT").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                    String paraName = map.get("ELEMENT");
                    if (inpara.getName() == null) {
                        inpara.setName(paraName);
                        inpara.getParameterInfos().add(new ParameterInfo());
                        if (!paraList.isEmpty()) {
                            data.setParameter(paraList.get(0));
                        } else {
                            data.setParameter(new ParameterInfo());
                        }
                        data.setParameterName(paraName);
                    } else {
                        ParameterInfo para2 = new ParameterInfo();
                        para2.setParent(inpara);
                        para2.setName(paraName);
                        if (inpara.getParameterInfos().get(0).getName() == null) {
                            inpara.getParameterInfos().clear();
                        }
                        inpara.getParameterInfos().add(para2);
                        data.setParameter(para2);
                        data.setParameterName(paraName);
                    }
                    inputMappingList.add(data);
                }
            }
            getInputElementList();
            getOutputElementList();
            currentFunction = fun;
        }
    }

    public void initWebserviceData() {
        ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell()
                .getShell());
        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) {
                monitor.beginTask("Retrieve WSDL parameter from net,please wait....", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (inPutcolumnList.isEmpty()) {
                            getInConnList();
                        }
                        if (outPutcolumnList.isEmpty()) {
                            getOutConnList();
                        }
                        getLastFunction();
                        getInputElementList();
                        getOutputElementList();
                        expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();
                        expressoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
                        rowoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
                        initLinksForIn();
                        initLinksForOut();
                        expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();
                        isFirst = false;
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

    }

    private void getInConnList() {
        List<? extends IConnection> inConnList = connector.getIncomingConnections();
        // List<IMetadataTable> metaList = connector.getMetadataList();
        for (int i = 0; i < inConnList.size(); i++) {
            IConnection conn = inConnList.get(i);
            if (conn == null) {
                continue;
            }
            inComeName = conn.getUniqueName();
            IMetadataTable metaTable = conn.getMetadataTable();
            if (metaTable == null) {
                continue;
            }
            inPutcolumnList.addAll(metaTable.getListColumns());
        }
        if (inComeName == null) {
            inComeName = ""; //$NON-NLS-1$
        }
        columnInPutTableView.getTableViewerCreator().getTableViewer().refresh();
    }

    private void getOutConnList() {
        List<? extends IConnection> outConnList = connector.getOutgoingSortedConnections();
        List<IMetadataColumn> list = new ArrayList<IMetadataColumn>();
        for (int i = 0; i < outConnList.size(); i++) {
            IConnection conn = outConnList.get(i);
            if (conn == null) {
                continue;
            }
            if (this.getWebServiceManager().getWebServiceComponent().getMetadataList().size() > 1) {
                IMetadataTable metaTable = this.connector.getMetadataFromConnector("OUTPUT");// this.getWebServiceManager().getWebServiceComponent().getMetadataList().get(0);
                if (metaTable == null) {
                    continue;
                }
                list.addAll(metaTable.getListColumns());
            }
        }

        for (int i = 0; i < list.size(); i++) {
            IMetadataColumn col = list.get(i);
            forOutColumnList.add(col);
            OutPutMappingData outData = new OutPutMappingData();
            outData.setOutputColumnValue(col.getLabel());
            outData.setMetadataColumn(col);
            // outPutcolumnList.add(outData);
        }
        expressoutPutTableView.getTableViewerCreator().getTableViewer().refresh();

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

            WebServiceComponent webServiceComponent = webServiceManager.getWebServiceComponent();
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

    private void getInputElementList() {
        IElementParameter INPUT_PARAMSPara = connector.getElementParameter("INPUT_PARAMS"); //$NON-NLS-1$
        List<Map<String, String>> inputparaValue = (List<Map<String, String>>) INPUT_PARAMSPara.getValue();
        WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
        List<ParameterInfo> childList = new ArrayList<ParameterInfo>();
        if (connector.getIncomingConnections().isEmpty()) {
            if (inPutcolumnList.isEmpty()) {
                IMetadataTable inputMetadataTable = this.connector.getMetadataFromConnector("FLOW");
                inPutcolumnList.addAll(inputMetadataTable.getListColumns());
            }
        }
        for (Map<String, String> map : inputparaValue) {
            boolean mark = true;
            InputMappingData data = new InputMappingData();
            Function fun = allfunList.get(0);
            List<ParameterInfo> list = fun.getInputParameters();
            if (list != null) {
                if (map.get("EXPRESSION") != null && map.get("EXPRESSION") instanceof String) { //$NON-NLS-1$ //$NON-NLS-2$
                    String expr = map.get("EXPRESSION"); //$NON-NLS-1$

                    if (inPutcolumnList == null || inPutcolumnList.size() <= 0) {
                        Map<String, String> exPmap = webParser.parseInTableEntryLocations(expr);
                        data.setInputColumnValue(expr);

                    } else {

                        Map<String, String> exPmap = webParser.parseInTableEntryLocations(expr);
                        data.setInputColumnValue(expr);
                        for (IMetadataColumn column : inPutcolumnList) {
                            Set<Entry<String, String>> set = exPmap.entrySet();
                            Iterator<Entry<String, String>> ite = set.iterator();
                            while (ite.hasNext()) {
                                if (ite.next().getKey().equals(column.getLabel())) {// (expr.contains(column.getLabel()))
                                    // {
                                    List<IMetadataColumn> columnList = data.getMetadataColumnList();
                                    columnList.add(column);
                                }
                            }
                        }
                    }
                }
                if (map.get("ELEMENT") != null && map.get("ELEMENT") instanceof String) { //$NON-NLS-1$ //$NON-NLS-2$
                    String paraName = map.get("ELEMENT"); //$NON-NLS-1$
                    String reArrayParaName = paraName;
                    // if (paraName.endsWith("]")) {
                    // int lastArray = paraName.lastIndexOf("[");
                    // reArrayParaName = paraName.substring(0, lastArray);
                    // }
                    if (reArrayParaName.contains("[")) {
                        reArrayParaName = paraName.replaceAll("\\[\\S+\\]", "");
                    }
                    if (allfunList == null || allfunList.size() <= 0) {
                        return;
                    }

                    goin: for (ParameterInfo para : list) {
                        String firstParaName = para.getName();
                        if (para.getName().equals(reArrayParaName)) {
                            // paraName = para.getName();
                            mark = false;
                            childList.clear();
                            data.setParameter(para);
                            data.setParameterName(paraName);
                            if (para.getParameterInfos().size() > 0) {
                                childList.addAll(new ParameterInfoUtil().getAllChildren(para));
                            }
                            break goin;
                        }
                        // else if (!para.getParameterInfos().isEmpty()) {
                        // List<ParameterInfo> nexChildlist = para.getParameterInfos();
                        // for (ParameterInfo nexPara : nexChildlist) {
                        // String nextParaName = firstParaName + "." + nexPara.getName();
                        // if (nextParaName.equals(reArrayParaName)) {
                        // mark = false;
                        // childList.clear();
                        // data.setParameter(para);
                        // data.setParameterName(paraName);
                        // if (para.getParameterInfos().size() > 0) {
                        // childList.addAll(new ParameterInfoUtil().getAllChildren(para));
                        // }
                        // break goin;
                        //
                        // }
                        // }
                        // }
                    }
                    if (mark) {
                        goout: for (ParameterInfo para : childList) {
                            if (para.getName().equals(paraName) || paraName.endsWith(para.getName())
                                    || reArrayParaName.endsWith(para.getName())) {
                                // paraName = para.getName();
                                data.setParameter(para);
                                data.setParameterName(paraName);
                                // if (para.getParameterInfos().size() > 0) {
                                // childList.addAll(para.getParameterInfos());
                                // }
                                break goout;
                            }
                        }
                    }

                }
                if (("".equals(data.getInputColumnValue()) || data.getInputColumnValue() == null) //$NON-NLS-1$
                        && ("".equals(data.getParameterName()) || data.getParameterName() == null)) { //$NON-NLS-1$
                    continue;
                } else {
                    inputMappingList.add(data);
                }
            }
        }
    }

    private void getOutputElementList() {
        IElementParameter OUTPUT_PARAMSPara = connector.getElementParameter("OUTPUT_PARAMS"); //$NON-NLS-1$
        List<Map<String, String>> outputMap = (List<Map<String, String>>) OUTPUT_PARAMSPara.getValue();
        List<OutPutMappingData> list = new ArrayList<OutPutMappingData>();
        WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*\\w+(\\[\\d+\\])?\\s*"); //$NON-NLS-1$
        List<ParameterInfo> childList = new ArrayList<ParameterInfo>();
        for (Map<String, String> map : outputMap) {
            boolean mark = true;
            OutPutMappingData data = new OutPutMappingData();

            if (map.get("ELEMENT") != null && map.get("ELEMENT") instanceof String) { //$NON-NLS-1$ //$NON-NLS-2$
                String ele = map.get("ELEMENT"); //$NON-NLS-1$
                String reArrayOutEle = "";
                if (ele.endsWith("]")) {
                    int lastArray = ele.lastIndexOf("[");
                    reArrayOutEle = ele.substring(0, lastArray);
                }
                if (allfunList == null || allfunList.size() <= 0) {
                    return;
                }
                Function fun = allfunList.get(0);
                List<ParameterInfo> outPaList = fun.getOutputParameters();
                if (outPaList != null) {
                    goin: for (ParameterInfo para : outPaList) {
                        if (para.getName().equals(ele)) {
                            childList.clear();
                            mark = false;
                            data.setParameter(para);
                            data.setParameterName(ele);
                            outParaList.add(data);
                            if (para.getParameterInfos().size() > 0) {
                                childList.addAll(new ParameterInfoUtil().getAllChildren(para));
                            }
                            // if (para != null) {
                            // OutPutMappingData outData = new OutPutMappingData();
                            // outData.setParameter(para);
                            // outData.setParameterName(ele);
                            // outParaList.add(outData);
                            // }
                            break goin;
                        }
                    }

                    if (mark) {
                        goout: for (ParameterInfo para : childList) {
                            if (para.getName().equals(ele) || ele.endsWith(para.getName())
                                    || reArrayOutEle.endsWith(para.getName())) {
                                data.setParameter(para);
                                data.setParameterName(ele);
                                outParaList.add(data);
                                // if (para.getParameterInfos().size() > 0) {
                                // childList.addAll(para.getParameterInfos());
                                // }
                                // if (para != null) {
                                // OutPutMappingData outData = new OutPutMappingData();
                                // outData.setParameter(para);
                                // outParaList.add(outData);
                                // }
                                break goout;
                            }
                        }
                    }

                }
            }
            if (map.get("EXPRESSION") != null && map.get("EXPRESSION") instanceof String) { //$NON-NLS-1$ //$NON-NLS-2$
                String exp = map.get("EXPRESSION"); //$NON-NLS-1$
                String reArrayOutExp = "";
                if (exp.endsWith("]")) {
                    int lastArray = exp.lastIndexOf("[");
                    reArrayOutExp = exp.substring(0, lastArray);
                }
                if (!"".equals(exp) && exp != null) { //$NON-NLS-1$
                    data.setParameterName(exp);
                    // org.talend.core.model.metadata.MetadataColumn
                    // Set<String> expList = webParser.parseOutTableEntryLocations(exp);
                    for (OutPutMappingData outMappingData : outParaList) {
                        ParameterInfo outInfo = outMappingData.getParameter();
                        String outInfoName = outMappingData.getParameterName();
                        // if (outInfoName != null) {
                        // int m = outInfoName.lastIndexOf(".");
                        // outInfoName = outInfoName.substring(m + 1);
                        // } else {
                        // outInfoName = outInfo.getName();
                        // }
                        // Iterator<String> ite = expList.iterator();
                        // while (ite.hasNext()) {
                        if (exp.equals(outInfoName)) {
                            data.getParameterList().add(outInfo);
                        }
                        // }
                    }
                }

            }

            if (map.get("COLUMN") != null && map.get("COLUMN") instanceof String) {
                String columnName = map.get("COLUMN");
                org.talend.core.model.metadata.MetadataColumn column = new org.talend.core.model.metadata.MetadataColumn();
                column.setLabel(columnName);
                data.setMetadataColumn(column);
                data.setOutputColumnValue(columnName);
                outputColumnCompareList.add(data);
                if (!"".equals(columnName) && data.getParameterName() == null) {
                    data.setParameterName("");
                }
            }
            if (data != null && data.getParameterName() != null) {
                list.add(data);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            OutPutMappingData outData = list.get(i);
            if (outPutcolumnList.size() > i) {
                OutPutMappingData outData2 = outPutcolumnList.get(i);
                outData.setOutputColumnValue(outData2.getOutputColumnValue());
                outData.setMetadataColumn(outData2.getMetadataColumn());
            }
        }
        if (outPutcolumnList.size() > list.size()) {
            for (int i = list.size(); i < outPutcolumnList.size(); i++) {
                OutPutMappingData outData = outPutcolumnList.get(i);
                if (outData == null) {
                    continue;
                }
                list.add(outData);

            }
        }
        if (list.size() > 0) {
            outPutcolumnList.clear();
            outPutcolumnList.addAll(list);
        }
    }

    /**
     * DOC Comment method "useProxy".
     */
    private ServiceHelperConfiguration useProxy() {
        String proxyHost = "";
        String proxyPort = "";
        String proxyUser = "";
        String proxyPassword = "";
        IElementParameter proxyHostParameter = webServiceManager.getWebServiceComponent().getElementParameter("PROXY_HOST");
        IElementParameter proxyPortParameter = webServiceManager.getWebServiceComponent().getElementParameter("PROXY_PORT");
        IElementParameter proxyUserParameter = webServiceManager.getWebServiceComponent().getElementParameter("PROXY_USERNAME");
        IElementParameter proxyPasswordParameter = webServiceManager.getWebServiceComponent().getElementParameter(
                "PROXY_PASSWORD");

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
        serverConfig.setProxyServer(proxyHost);
        serverConfig.setProxyPort(Integer.parseInt(proxyPort));
        serverConfig.setUsername(proxyUser);
        serverConfig.setPassword(proxyPassword);

        return serverConfig;

    }

    /**
     * DOC Comment method "useAuth".
     */
    private ServiceHelperConfiguration useAuth() {

        String authUsername = "";
        String authPassword = "";

        IElementParameter authUsernameParameter = webServiceManager.getWebServiceComponent().getElementParameter("AUTH_USERNAME");
        IElementParameter authPasswordParameter = webServiceManager.getWebServiceComponent().getElementParameter("AUTH_PASSWORD");
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

    /**
     * DOC gcui Comment method "useSSL".
     *
     * @return
     */
    private void useSSL() {
        String trustStoreFile = "";
        String trustStorePassword = "";
        IElementParameter trustserverFileParameter = webServiceManager.getWebServiceComponent().getElementParameter(
                "SSL_TRUSTSERVER_TRUSTSTORE");
        IElementParameter trustserverPasswordParameter = webServiceManager.getWebServiceComponent().getElementParameter(
                "SSL_TRUSTSERVER_PASSWORD");
        if (trustserverFileParameter.getValue() != null) {
            trustStoreFile = trustserverFileParameter.getValue().toString();
            trustStoreFile = TalendTextUtils.removeQuotes(trustStoreFile);
        }
        if (trustserverPasswordParameter.getValue() != null) {
            trustStorePassword = trustserverPasswordParameter.getValue().toString();
            trustStorePassword = TalendTextUtils.removeQuotes(trustStorePassword);
        }

        // System.clearProperty("javax.net.ssl.trustStore");
        System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    /**
     * Sets the listener.
     *
     * @param listener the listener to set
     */
    public void addListener(WebServiceEventListener listener) {
        this.listener = listener;
    }

    private Shell getShell() {
        return this.uiParent.getShell();
    }

    public void init() {
        uiParent.setLayout(new GridLayout());

        Composite composite = new Composite(uiParent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new FormLayout());

        allContentForm = new SashForm(composite, SWT.NONE);
        FormData formData = new FormData();
        formData.top = new FormAttachment(5, 5);
        formData.left = new FormAttachment(0, 5);
        formData.right = new FormAttachment(100, -5);
        formData.bottom = new FormAttachment(100, -5);
        allContentForm.setLayoutData(formData);
        createViewers(allContentForm);

    }

    protected WebServiceComponent getWebServiceComponent() {
        return getWebServiceManager().getWebServiceComponent();
    }

    protected WebServiceManager getWebServiceManager() {
        return this.webServiceManager;
    }

    private void createViewers(SashForm allContentForm) {
        // allContentForm.setLayout(new FillLayout());
        // allContentForm.setOrientation(SWT.VERTICAL);
        // allContentForm.setSashWidth(ExternalWebServiceUIProperties.SASHFORM_WIDTH);

        createHeader(allContentForm);

    }

    private void createHeader(SashForm allContentForm) {
        //
        tabFolder = new CTabFolder(allContentForm, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        wsdlTabItem = new CTabItem(tabFolder, SWT.NONE);
        wsdlTabItem.setText(ExternalWebServiceUIProperties.WSDL_LABEL);
        tabFolder.setSelection(wsdlTabItem);
        tabFolder.setSimple(false);
        // tabFolder.setSelectionForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        // tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));

        inputMappingTabItem = new CTabItem(tabFolder, SWT.BORDER);
        inputMappingTabItem.setText(ExternalWebServiceUIProperties.INPUT_LABEL);

        outputMappingTabItem = new CTabItem(tabFolder, SWT.BORDER);
        outputMappingTabItem.setText(ExternalWebServiceUIProperties.OUTPUT_LABEL);

        wsdlTabItem.setControl(createWSDLStatus());
        inputMappingTabItem.setControl(createInputMappingStatus());
        outputMappingTabItem.setControl(createOutputMappingStatus());

        boolean flag = ProjectManager.getInstance().isInCurrentMainProject(this.connectionItem);
        if (!flag && this.connectionItem != null) {
            wsdlTabItem.getControl().setEnabled(false);
            inputMappingTabItem.getControl().setEnabled(false);
            outputMappingTabItem.getControl().setEnabled(false);
        }

        tabFolder.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (expressinPutTableView == null) {
                    return;
                }

                if (currentFunction == null) {
                    tabFolder.setSelection(wsdlTabItem);
                    MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
                    box.setText("WARNING"); //$NON-NLS-1$
                    box.setMessage("Please Select a Operation."); //$NON-NLS-1$
                    box.open();
                    return;
                }
                if (isFirst) {
                    initWebserviceData();
                } else {
                    if (inPutcolumnList.isEmpty()) {
                        getInConnList();
                    }
                    if (outPutcolumnList.isEmpty()) {
                        getOutConnList();
                    }
                }

                // set dialog back button and next button can use or not.
                int crruenSelect = tabFolder.getSelectionIndex();
                if (webServiceDialog != null) {
                    if (crruenSelect == 0) {
                        webServiceDialog.setBackButtonUnuse();
                        webServiceDialog.setNextButtonCanuse();
                    }
                    if (crruenSelect == 2) {
                        webServiceDialog.setBackButtonCanuse();
                        webServiceDialog.setNextButtonUnuse();
                    }
                    if (crruenSelect == 1) {
                        webServiceDialog.setBackButtonCanuse();
                        webServiceDialog.setNextButtonCanuse();
                    }
                }
                refreshTableView();
            }
        });
    }

    private Composite createWSDLStatus() {
        wsdlComposite = new Composite(tabFolder, SWT.NONE);
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
                    URLValue = wsdlField.getText();
                    getDataFromNet();
                    isFirst = false;
                }
            }

        };
        wsdlField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                // TODO Auto-generated method stub
                URLValue = wsdlField.getText();
                if (connection != null) {
                    connection.setWSDL(URLValue);
                }
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

        // if (wsdlUrl != null && !wsdlUrl.contains("\"")) {
        // wsdlField.setReadOnly(true);
        // refreshbut.setEnabled(false);
        // }

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

                    @Override
                    public String get(PortNames bean) {
                        return bean.getPortName();
                    }

                    @Override
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

                    @Override
                    public String get(Function bean) {
                        return bean.getName();
                    }

                    @Override
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
        return wsdlComposite;
    }

    private void addListenerForWSDLCom() {
        refreshbut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refresh(wsdlField.getText(), false);
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
                currentFunction = item.length > 0 ? (Function) item[0].getData() : null;
                if (currentFunction == null) {
                    return;
                }

                // if select the same as before ,don't change it
                // IElementParameter METHODPara = connector.getElementParameter("METHOD"); //$NON-NLS-1$
                // Object obj = METHODPara.getValue();
                // if (currentFunction.getName().equals(obj.toString())) {
                // return;
                // }
                List<ParameterInfo> listIn = currentFunction.getInputParameters();
                List<ParameterInfo> listOut = currentFunction.getOutputParameters();

                // For fix TDI-34970 to show the input only parameters
                if (listIn == null /* || listOut == null */) {
                    return;
                }
                ExtendedTableModel<InputMappingData> columnModel = expressinPutTableView.getExtendedTableModel();
                columnModel.removeAll();
                if (listIn != null) {
                    for (int i = 0; i < listIn.size(); i++) {
                        ParameterInfo pa = listIn.get(i);
                        InputMappingData inData = new InputMappingData();
                        inData.setParameterName(pa.getName());
                        inData.setParameter(pa);

                        columnModel.add(inData);
                    }
                    // if input parameter only one add it auto
                    // ParameterInfo onlyOneInPara = isOnlyOnePara(listIn);
                    // if (onlyOneInPara != null) {
                    // if (onlyOneInPara.getParent() != null) {
                    // InputMappingData inData = new InputMappingData();
                    // inData.setParameterName(onlyOneInPara.getParaFullName());
                    // inData.setParameter(onlyOneInPara);
                    // columnModel.add(inData);
                    // }
                    // }
                }
                expressTableForIn.setSelection(0);

                ExtendedTableModel<OutPutMappingData> rowForOutput = rowoutPutTableView.getExtendedTableModel();
                rowForOutput.removeAll();
                if (listOut != null) {
                    for (int i = 0; i < listOut.size(); i++) {
                        OutPutMappingData outData = new OutPutMappingData();
                        ParameterInfo pa = listOut.get(i);
                        outData.setParameter(pa);
                        rowForOutput.add(outData);
                    }
                    // if output parameter only one add it auto
                    // ParameterInfo onlyOneOutPara = isOnlyOnePara(listOut);
                    // if (onlyOneOutPara != null) {
                    // if (onlyOneOutPara.getParent() != null) {
                    // OutPutMappingData outData = new OutPutMappingData();
                    // outData.setParameterName(onlyOneOutPara.getParaFullName());
                    // outData.setParameter(onlyOneOutPara);
                    // rowForOutput.add(outData);
                    // }
                    // }
                }
                ExtendedTableModel<OutPutMappingData> exforoutput = expressoutPutTableView.getExtendedTableModel();
                exforoutput.removeAll();
                for (IMetadataColumn column : forOutColumnList) {
                    OutPutMappingData outData = new OutPutMappingData();
                    outData.setOutputColumnValue(column.getLabel());
                    outData.setMetadataColumn(column);
                    exforoutput.add(outData);
                }
                rowTableForout.setSelection(0);
                if (connection != null) {
                    if (currentPortName != null) {
                        connection.setPortName(currentPortName.getPortName());

                    } else if (currentPortName == null && allPortNames != null) {
                        currentPortName = allPortNames.get(0);
                        connection.setPortName(currentPortName.getPortName());
                    }
                    if (currentFunction != null) {
                        if (currentFunction.getName() != null) {
                            connection.setMethodName(currentFunction.getName());
                        }
                        if (currentFunction.getServerNameSpace() != null) {
                            connection.setServerNameSpace(currentFunction.getServerNameSpace());
                        }
                        if (currentFunction.getServerName() != null) {
                            connection.setServerName(currentFunction.getServerName());
                        }
                        if (currentFunction.getServerNameSpace() != null) {
                            connection.setPortNameSpace(currentFunction.getServerNameSpace());
                        }
                    }
                }
            }
        });

        portListTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] item = portListTable.getSelection();
                currentPortName = item.length > 0 ? (PortNames) item[0].getData() : null;
                if (connection != null) {
                    if (currentPortName != null) {
                        connection.setPortName(currentPortName.getPortName());

                    } else if (currentPortName == null && allPortNames != null) {
                        currentPortName = allPortNames.get(0);
                        connection.setPortName(currentPortName.getPortName());
                    } else {
                        connection.setPortName("");
                    }

                }
            }
        });
    }

    public void refresh(String url, boolean resetFunction) {
        URLValue = url;
        final Job job = new Job("t") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // TODO Auto-generated method stub
                // monitor.setCanceled(true);
                monitor.beginTask("Retrieve WSDL parameter from net.", IProgressMonitor.UNKNOWN);
                getDataFromNet();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
        ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell()
                .getShell());
        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) {
                monitor.beginTask("Retrieve WSDL parameter from net.", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                boolean f = true;
                // TODO Auto-generated method stub
                while (f) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (monitor.isCanceled()) {
                        job.done(Status.OK_STATUS);
                        job.cancel();
                    }
                    if (monitor.isCanceled() || (job.getResult() != null && job.getResult().isOK())) {
                        monitor.done();
                        f = false;
                    }
                }

                // monitor.done();
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
        if (resetFunction) {
            currentFunction = null;
        }

        if (connection != null) {
            if (listTable.getItemCount() > 0) {
                listTable.setSelection(listTable.getItem(0));
            }
            if (currentFunction != null) {
                if (currentFunction.getName() != null) {
                    connection.setMethodName(currentFunction.getName());
                }
                if (currentFunction.getServerNameSpace() != null) {
                    connection.setServerNameSpace(currentFunction.getServerNameSpace());
                }
                if (currentFunction.getServerName() != null) {
                    connection.setServerName(currentFunction.getServerName());
                }
                if (currentFunction.getServerNameSpace() != null) {
                    connection.setPortNameSpace(currentFunction.getServerNameSpace());
                }
            }
            if (currentPortName != null) {
                connection.setPortName(currentPortName.getPortName());

            } else if (currentPortName == null && !allPortNames.isEmpty()) {
                currentPortName = allPortNames.get(0);
                connection.setPortName(currentPortName.getPortName());
            }
        }
        // listTable.setSelection(listTable.getItem(0));
        // listTable.select(0);
        isFirst = false;

    }

    private ParameterInfo isOnlyOnePara(List<ParameterInfo> list) {
        if (list.size() == 1) {
            ParameterInfo first = list.get(0);
            if (first.getParameterInfos() != null && !first.getParameterInfos().isEmpty()) {
                return isOnlyOnePara(list.get(0).getParameterInfos());
            } else {
                return first;
            }

        }

        return null;
    }

    private void getDataFromNet() {
        funList.clear();
        portNameList.clear();

        if (URLValue == null) {
            URLValue = ""; //$NON-NLS-1$
        }
        final WSDLDiscoveryHelper ws = new WSDLDiscoveryHelper();

        WebServiceComponent webServiceComponent = webServiceManager.getWebServiceComponent();
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
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                ExtendedTableModel<Function> listModel = listTableView.getExtendedTableModel();
                ExtendedTableModel<PortNames> portListModel = portListTableView.getExtendedTableModel();
                listModel.removeAll();
                listModel.addAll(funList);
                // allfunList.clear();
                // allfunList.addAll(funList);
                // getInputElementList();
                // getOutputElementList();
                portListModel.removeAll();
                portListModel.addAll(portNameList);
            }

        });

    }

    private String parseContextParameter(final String contextValue) {

        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                String url = "";
                // TODO Auto-generated method stub
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                IContextManager contextManager = null;
                if (connector.getProcess() == null) {
                    // contextManager = contextModeManager.getSelectedContextType().getContextParameter(); //
                    // connection.get
                    // IContextManager contextManager
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connection);
                    url = ConnectionContextHelper.getOriginalValue(contextType, contextValue);
                } else {
                    contextManager = connector.getProcess().getContextManager();
                    String currentDefaultName = contextManager.getDefaultContext().getName();
                    List contextList = contextManager.getListContext();
                    if (!contextList.isEmpty() && contextList.size() > 1) {
                        currentDefaultName = ConnectionContextHelper.getContextTypeForJob(shell, contextManager, false);
                    }
                    // ContextSetsSelectionDialog cssd=new ContextSetsSelectionDialog(shell,,false);
                    // ContextType contextType=ConnectionContextHelper.getContextTypeForContextMode(connector);
                    IContext context = contextManager.getContext(currentDefaultName);
                    url = ContextParameterUtils.parseScriptContextCode(contextValue, context);

                }
                setParseURL(url);
            }

        });

        return getparseURL();
    }

    private String getparseURL() {
        return url;
    }

    private void setParseURL(String url) {
        this.url = url;
    }

    private Composite createInputMappingStatus() {

        SashForm inputComposite = new SashForm(tabFolder, SWT.NONE);
        GridLayout layout = new GridLayout(10, false);
        layout.marginWidth = 20;
        layout.marginHeight = 20;
        layout.horizontalSpacing = 150;

        inputComposite.setLayout(layout);
        inputComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        inputComposite.setBackgroundMode(SWT.INHERIT_FORCE);

        // input mapping cloumn.
        SashForm comforRow = new SashForm(inputComposite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 220;
        data.horizontalSpan = 3;
        comforRow.setLayoutData(data);
        comforRow.setLayout(new FillLayout());
        tabTotabLinkForin = new WebServiceTableLiner(inputComposite);
        ExtendedTableModel<IMetadataColumn> model = new ExtendedTableModel<IMetadataColumn>("INPUTCOLUMN", inPutcolumnList); //$NON-NLS-1$

        Composite columnComposite = new Composite(comforRow, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 5;
        // data.widthHint = 420;
        // data.heightHint = 350;
        columnComposite.setLayoutData(data);
        columnComposite.setLayout(new GridLayout());
        Composite buttonCompsoite = new Composite(columnComposite, SWT.NONE);
        buttonCompsoite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttonCompsoite.pack();
        GridLayout buttonCompsoiteLayout = new GridLayout();
        buttonCompsoiteLayout.numColumns = 6;
        buttonCompsoiteLayout.verticalSpacing = 6;
        buttonCompsoite.setLayout(buttonCompsoiteLayout);
        GridData butComData3 = new GridData(GridData.FILL_BOTH);
        butComData3.horizontalSpan = 1;
        GridData butComData4 = new GridData(GridData.FILL_BOTH);
        butComData4.horizontalSpan = 2;
        // Button addButton = new Button(buttonCompsoite, SWT.PUSH);
        // addButton.setEnabled(true);
        // addButton.setImage(ImageProvider.getImage(EImage.ADD_ICON));
        //        addButton.setToolTipText(Messages.getString("WebServiceUI.Add_list_element")); //$NON-NLS-1$
        // addButton.setLayoutData(butComData3);
        // addButton.pack();
        // Button removeBut = new Button(buttonCompsoite, SWT.PUSH);
        // removeBut.setEnabled(false);
        // removeBut.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
        //        removeBut.setToolTipText(Messages.getString("WebServiceUI.Remove_element")); //$NON-NLS-1$
        // removeBut.setLayoutData(butComData3);
        // removeBut.pack();
        //
        // Button upButton = new Button(buttonCompsoite, SWT.PUSH);
        // upButton.setEnabled(false);
        // upButton.setImage(ImageProvider.getImage(EImage.UP_ICON));
        // upButton.setLayoutData(butComData4);
        // upButton.pack();
        //
        // // input mapping Denormalize button.
        // Button downButton = new Button(buttonCompsoite, SWT.PUSH);
        // downButton.setEnabled(false);
        // downButton.setImage(ImageProvider.getImage(EImage.DOWN_ICON));
        // downButton.setLayoutData(butComData4);
        // downButton.pack();
        inputschemaButton = new Button(buttonCompsoite, SWT.PUSH);
        inputschemaButton.setText("Schema Management");
        inputschemaButton.setToolTipText("You can add or edit schema and save in 'Schema List' viewer");
        if (connection == null) {
            inputschemaButton.setEnabled(true);
        }

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = SWT.END;
        autoMapButtonForIn = new Button(buttonCompsoite, SWT.PUSH);
        autoMapButtonForIn.setLayoutData(gd);
        autoMapButtonForIn.setText("Auto Map");
        autoMapButtonForIn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                autoMapForInput();
            }
        });

        Composite viewComposite = new Composite(columnComposite, SWT.NONE);
        viewComposite.setLayout(new FillLayout());
        viewComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        columnInPutTableView = new AbstractDataTableEditorView<IMetadataColumn>(viewComposite, SWT.BORDER, model, false, true,
                false) {

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<IMetadataColumn> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(true);
                newTableViewerCreator.setVerticalScroll(true);
                newTableViewerCreator.setReadOnly(true);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void createColumns(TableViewerCreator<IMetadataColumn> tableViewerCreator, Table table) {
                TableViewerCreatorColumn rowColumn = new TableViewerCreatorColumn(tableViewerCreator);
                rowColumn.setTitle(Messages.getString("WebServiceUI.COLUMN")); //$NON-NLS-1$
                rowColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<IMetadataColumn, String>() {

                    @Override
                    public String get(IMetadataColumn bean) {
                        return bean.getLabel();
                    }

                    @Override
                    public void set(IMetadataColumn bean, String value) {
                        bean.setLabel(value);

                    }

                });
                rowColumn.setWeight(60);
                rowColumn.setModifiable(true);
                rowColumn.setMinimumWidth(60);
                rowColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));

            }

        };

        // input mapping right SashForm.
        rowTableForin = columnInPutTableView.getTable();
        new DragAndDropForWebService(webServiceManager, columnInPutTableView, null, connector, true, false);

        SashForm comforRow1 = new SashForm(inputComposite, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        comforRow1.setLayoutData(data);

        Composite comforExp = new Composite(inputComposite, SWT.BORDER);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 5;
        // data.widthHint = 420;
        // data.heightHint = 350;
        comforExp.setLayoutData(data);
        comforExp.setLayout(new GridLayout());

        Composite butCom = new Composite(comforExp, SWT.NONE);
        butCom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        butCom.pack();
        GridLayout butComLayout = new GridLayout();
        butComLayout.numColumns = 6;
        butComLayout.verticalSpacing = 6;
        butCom.setLayout(butComLayout);
        GridData butComData1 = new GridData(GridData.FILL_BOTH);
        butComData1.horizontalSpan = 1;
        GridData butComData2 = new GridData(GridData.FILL_BOTH);
        butComData2.horizontalSpan = 2;

        // input mapping add button.
        addListButForIn = new Button(butCom, SWT.PUSH);
        addListButForIn.setEnabled(false);
        addListButForIn.setImage(ImageProvider.getImage(EImage.ADD_ICON));
        addListButForIn.setToolTipText(Messages.getString("WebServiceUI.Add_list_element")); //$NON-NLS-1$
        addListButForIn.setLayoutData(butComData1);
        addListButForIn.pack();

        // input mapping remove button.
        removeButForIn = new Button(butCom, SWT.PUSH);
        removeButForIn.setEnabled(false);
        removeButForIn.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
        removeButForIn.setToolTipText(Messages.getString("WebServiceUI.Remove_element")); //$NON-NLS-1$
        removeButForIn.setLayoutData(butComData1);
        removeButForIn.pack();

        // input mapping Normalize button.
        normalizeButForIn = new Button(butCom, SWT.PUSH);
        normalizeButForIn.setEnabled(false);
        normalizeButForIn.setText(Messages.getString("WebServiceUI.Normalize")); //$NON-NLS-1$
        normalizeButForIn.setLayoutData(butComData2);
        normalizeButForIn.pack();

        // input mapping Denormalize button.
        denorButForIn = new Button(butCom, SWT.PUSH);
        denorButForIn.setEnabled(false);
        denorButForIn.setText(Messages.getString("WebServiceUI.Denormalize")); //$NON-NLS-1$
        denorButForIn.setLayoutData(butComData2);
        denorButForIn.pack();

        SashForm safhEx = new SashForm(comforExp, SWT.NONE);
        safhEx.pack();
        safhEx.setLayoutData(new GridData(GridData.FILL_BOTH));
        safhEx.setLayout(new FillLayout());

        ExtendedTableModel<InputMappingData> targetModel = new ExtendedTableModel<InputMappingData>("INPUTMAPPING", //$NON-NLS-1$
                inputMappingList);
        expressinPutTableView = new AbstractDataTableEditorView<InputMappingData>(safhEx, SWT.NONE | SWT.MULTI
                | SWT.FULL_SELECTION | SWT.SINGLE, targetModel, false, true, false) {

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<InputMappingData> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(true);
                newTableViewerCreator.setVerticalScroll(true);
                newTableViewerCreator.setReadOnly(false);
            }

            @Override
            protected void createColumns(TableViewerCreator<InputMappingData> tableViewerCreator, final Table table) {
                ExtendedTableModel<InputMappingData> tableModel = this.getExtendedTableModel();

                // input mapping express cloumn.
                TableViewerCreatorColumn<InputMappingData, String> expressionColumn = new TableViewerCreatorColumn<InputMappingData, String>(
                        tableViewerCreator);
                expressionColumn.setTitle(Messages.getString("WebServiceUI.EXPRESSION")); //$NON-NLS-1$
                expressionColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputMappingData, String>() {

                    @Override
                    public String get(InputMappingData bean) {
                        if ("".equals(bean.getInputColumnValue()) || bean.getInputColumnValue() == null) {
                            return "";
                        }
                        // else {
                        // StringBuffer expression = new StringBuffer();
                        // for (IMetadataColumn column : inPutcolumnList) {
                        // if (bean.getInputColumnValue().contains(column.getLabel())) {
                        // expression.append(inComeName + ".");
                        // expression.append(column.getLabel());
                        // expression.append(" ");
                        // }
                        // }
                        // return expression.toString();// inComeName + "." + bean.getInputColumnValue();
                        // }

                        return bean.getInputColumnValue();
                    }

                    @Override
                    public void set(InputMappingData bean, String value) {
                        bean.setInputColumnValue(value);

                    }

                });
                expressionColumn.setWeight(40);
                expressionColumn.setModifiable(true);
                expressionColumn.setMinimumWidth(40);
                TextCellEditor cellEditorForIn = new TextCellEditor(tableViewerCreator.getTable());
                expressionColumn.setCellEditor(cellEditorForIn);
                cellEditorForIn.addListener(new DialogErrorForCellEditorListener(cellEditorForIn, expressionColumn) {

                    @Override
                    public void newValidValueTyped(int itemIndex, Object previousValue, Object newValue, CELL_EDITOR_STATE state) {
                        if (state == CELL_EDITOR_STATE.EDITING) {
                            tabTotabLinkForin.onXPathValueChanged(rowTableForin, table, previousValue.toString(),
                                    newValue.toString(), inComeName, itemIndex);
                        }

                    }

                    @Override
                    public String validateValue(String value, int beanPosition) {
                        return null;
                    }

                });

                // input mapping element.
                TableViewerCreatorColumn<InputMappingData, String> elementColumn = new TableViewerCreatorColumn<InputMappingData, String>(
                        tableViewerCreator);
                elementColumn.setTitle(Messages.getString("WebServiceUI.ELEMENT")); //$NON-NLS-1$
                elementColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputMappingData, String>() {

                    @Override
                    public String get(InputMappingData bean) {
                        ParameterInfo para = bean.getParameter();
                        if (para != null) {
                            List<ParameterInfo> paraList = new ParameterInfoUtil().getAllChildren(para);// para.
                            if (paraList != null && paraList.size() > 0 && para.getParent() == null) {
                                boolean flag = false;
                                if (TableViewerCreator.getRecommandLazyLoad()) {
                                    flag = isExpanded4Input(tableModel.getBeansList(), para, paraList);
                                } else {
                                    flag = isExpanded(table, para, paraList, "IN"); //$NON-NLS-1$
                                }
                                if (flag) {
                                    return "[-] " + para.getName(); //$NON-NLS-1$
                                }
                                return "[+] " + para.getName();// bean.getParameterName(); //$NON-NLS-1$
                            } else if (para.getParent() != null) {
                                String name;
                                if (bean.getParameterName() != null) {
                                    name = bean.getParameterName();
                                } else {
                                    name = new ParameterInfoUtil().getParentName(para);
                                }
                                return " |-- " + name; //$NON-NLS-1$
                            }
                        }
                        return bean.getParameterName();
                    }

                    @Override
                    public void set(InputMappingData bean, String value) {
                        if (value.contains("[+] ")) { //$NON-NLS-1$
                            value.replace("[+] ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        } else if (value.contains("[-] ")) { //$NON-NLS-1$
                            value.replace("[-] ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        // else if (value.contains(" [-] ")) {
                        // value.replace(" [-] ", "");
                        // }
                        else if (value.contains(" |-- ")) { //$NON-NLS-1$
                            value.replace(" |-- ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        bean.setParameterName(value);

                    }

                });
                elementColumn.setWeight(40);
                elementColumn.setModifiable(false);
                elementColumn.setMinimumWidth(40);
                TextCellEditor cellElementEditorForin = new TextCellEditor(tableViewerCreator.getTable());
                elementColumn.setCellEditor(cellElementEditorForin);

            }

        };
        expressTableForIn = expressinPutTableView.getTable();
        new DragAndDropForWebService(webServiceManager, expressinPutTableView, tabTotabLinkForin, connector, false, true);

        backgroundRefresher = new BackgroundRefresher(tabTotabLinkForin);
        Table[] tabs = new Table[1];
        tabs[0] = expressTableForIn;
        tabTotabLinkForin.init(rowTableForin, tabs, backgroundRefresher);
        addListenerForInputCom();
        getInConnList();
        initLinksForIn();
        inputComposite.setWeights(new int[] { 5, 2, 5 });

        if (WindowSystem.isBigSurOrLater()) {
            backgroundRefresher.setBackgroundColor(tabTotabLinkForin.getBgDrawableComposite().getBackground());
            Composite comp = expressinPutTableView.getTable().getParent();
            while (comp != null && comp != inputComposite) {
                comp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
                comp = comp.getParent();
            }
            comp = columnInPutTableView.getTable().getParent();
            while (comp != null && comp != inputComposite) {
                comp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
                comp = comp.getParent();
            }
            comforRow1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
            inputComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
        }

        return inputComposite;
    }

    private void autoMapForInput() {
        TableItem[] inputItems = columnInPutTableView.getTable().getItems();
        TableItem[] expressItems = expressinPutTableView.getTable().getItems();
        tabTotabLinkForin.clearLinks();
        for (TableItem inputItem : inputItems) {
            Object inputData = inputItem.getData();
            if (inputData instanceof IMetadataColumn) {
                IMetadataColumn inputColumn = (IMetadataColumn) inputData;
                String inputlabel = inputColumn.getLabel();
                for (TableItem expressItem : expressItems) {
                    Object expressData = expressItem.getData();
                    if (expressData instanceof InputMappingData) {
                        InputMappingData imData = (InputMappingData) expressData;
                        ParameterInfo parameter = imData.getParameter();
                        if (parameter.getParent() != null) {
                            String columnValue = imData.getInputColumnValue();
                            if (columnValue == null || "".equals(columnValue)) {
                                String paraName = parameter.getName();
                                // TDI-28561 fixed:code here should be same as the autoMapForOutput() for the case
                                // parentName.parameterName
                                if (paraName.startsWith(parameter.getParent().getName()) && parameter.getName().contains(".")) {
                                    paraName = paraName.substring(paraName.lastIndexOf(".") + 1);
                                }
                                if (paraName.equals(inputlabel)) {
                                    imData.setInputColumnValue(connector.initInRoWName() + "." + paraName);
                                    if (!imData.getMetadataColumnList().contains(inputColumn)) {
                                        imData.getMetadataColumnList().add(inputColumn);
                                    }
                                    tabTotabLinkForin.addLinks(inputItem, inputData, expressItem.getParent(), expressData,
                                            "INPUTMAPPING");
                                }
                            } else {
                                if (columnValue.contains(".")) {
                                    String originalValue = columnValue.substring(columnValue.lastIndexOf(".") + 1);
                                    if (originalValue.equals(inputlabel)) {
                                        tabTotabLinkForin.addLinks(inputItem, inputData, expressItem.getParent(), expressData,
                                                "INPUTMAPPING");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        tabTotabLinkForin.updateLinksStyleAndControlsSelection(expressinPutTableView.getTable(), true);
        columnInPutTableView.getTableViewerCreator().getTableViewer().refresh();
        expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();
    }

    private void autoMapForOutput() {
        TableItem[] outputItems = rowoutPutTableView.getTable().getItems();
        TableItem[] expressItems = expressoutPutTableView.getTable().getItems();
        tabTotabLinkForout.clearLinks();
        for (TableItem expressItem : expressItems) {
            Object expressData = expressItem.getData();
            if (expressData instanceof OutPutMappingData) {
                OutPutMappingData omData = (OutPutMappingData) expressData;
                String outputColumnValue = omData.getOutputColumnValue();
                String parameterName = omData.getParameterName();
                for (TableItem outputItem : outputItems) {
                    Object outputData = outputItem.getData();
                    if (outputData instanceof OutPutMappingData) {
                        OutPutMappingData opData = (OutPutMappingData) outputData;
                        ParameterInfo parameter = opData.getParameter();
                        String sourceParameterName = opData.getParameterName();
                        if (parameter.getParent() != null) {
                            if (parameterName == null || "".equals(parameterName)) {
                                String value = "";
                                if (parameter.getName().contains(".")) {
                                    value = parameter.getName().substring(parameter.getName().lastIndexOf(".") + 1);
                                } else {
                                    value = parameter.getName();
                                }
                                if (value.equals(outputColumnValue)) {
                                    String str = "";
                                    if (opData.getParameterName() != null) {
                                        str = opData.getParameterName();
                                    } else {
                                        str = new ParameterInfoUtil().getParentName(parameter);
                                    }
                                    omData.setParameterName(str);
                                    if (!omData.getParameterList().contains(parameter)) {
                                        omData.getParameterList().add(parameter);
                                    }
                                    tabTotabLinkForout.addLinks(outputItem, outputData, expressItem.getParent(), expressData,
                                            "OUTPUTMAPPING");
                                }
                            } else {
                                if (parameterName.equals(sourceParameterName)) {
                                    tabTotabLinkForout.addLinks(outputItem, outputData, expressItem.getParent(), expressData,
                                            "OUTPUTMAPPING");
                                }
                            }
                        }
                    } else if (outputData instanceof ParameterInfo) {
                        ParameterInfo paraData = (ParameterInfo) outputData;
                        if (paraData.getParent() != null && paraData.getName().equals(outputColumnValue)) {
                            if (parameterName == null || "".equals(parameterName)) {
                                String value = "";
                                if (paraData.getName().contains(".")) {
                                    value = paraData.getName().substring(paraData.getName().lastIndexOf(".") + 1);
                                } else {
                                    value = paraData.getName();
                                }
                                if (value.equals(outputColumnValue)) {
                                    String name = new ParameterInfoUtil().getParentName(paraData);
                                    omData.setParameterName(name);
                                    if (!omData.getParameterList().contains(paraData)) {
                                        omData.getParameterList().add(paraData);
                                    }
                                    tabTotabLinkForout.addLinks(outputItem, outputData, expressItem.getParent(), expressData,
                                            "OUTPUTMAPPING");
                                }
                            } else {
                                String value = "";
                                if (parameterName.contains(".")) {
                                    value = parameterName.substring(parameterName.lastIndexOf(".") + 1);
                                } else {
                                    value = parameterName;
                                }
                                if (value.equals(paraData.getName())) {
                                    tabTotabLinkForout.addLinks(outputItem, outputData, expressItem.getParent(), expressData,
                                            "OUTPUTMAPPING");
                                }
                            }
                        }
                    }
                }

            }
        }
        tabTotabLinkForout.updateLinksStyleAndControlsSelection(expressoutPutTableView.getTable(), true);
        rowoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
        expressoutPutTableView.getTableViewerCreator().getTableViewer().refresh();

    }

    private void addListenerForInputCom() {
        rowTableForin.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });

        expressTableForIn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] selectedItem = expressTableForIn.getSelection();
                if (selectedItem == null || selectedItem.length == 0)
                    return;
                currentElementIndexForIn = expressTableForIn.getSelectionIndex();

                currentInputMappingData = (InputMappingData) selectedItem[0].getData();
                // currentSelectedInParameter = inputData.getParameter();
                if (currentInputMappingData.getParameter() == null) {
                    return;
                }
                List<ParameterInfo> infoList = currentInputMappingData.getParameter().getParameterInfos();
                Boolean isArray = false;
                String isArrayName = currentInputMappingData.getParameterName();
                if ((currentInputMappingData.getParameter().getArraySize() == -1 || currentInputMappingData.getParameter()
                        .getArraySize() > 1) && !isArrayName.endsWith("]")) {
                    isArray = true;
                }
                if (isArrayName.contains("*")) {
                    isArray = true;
                }
                ParameterInfo infoPa = currentInputMappingData.getParameter().getParent();

                if (infoList != null && infoList.size() > 0 || isArray) {
                    addListButForIn.setEnabled(true);
                } else {
                    addListButForIn.setEnabled(false);
                }

                if (infoPa != null) {
                    removeButForIn.setEnabled(true);
                } else {
                    removeButForIn.setEnabled(false);
                }

                List<IMetadataColumn> columnList = currentInputMappingData.getMetadataColumnList();
                if (columnList != null && columnList.size() != 1) {
                    normalizeButForIn.setEnabled(false);
                    denorButForIn.setEnabled(false);
                } else if (columnList != null && columnList.size() == 1) {
                    IMetadataColumn column = columnList.get(0);
                    if (column == null) {
                        return;
                    }
                    if (column.getTalendType().equals("id_List") && !isArray && !currentInputMappingData.getInputColumnValue().contains("denormalize")) { //$NON-NLS-1$
                        denorButForIn.setEnabled(true);
                    } else {
                        denorButForIn.setEnabled(false);
                    }

                    if (!column.getTalendType().equals("id_List") && isArray && !currentInputMappingData.getInputColumnValue().contains("normalize")) { //$NON-NLS-1$
                        normalizeButForIn.setEnabled(true);
                    } else {
                        normalizeButForIn.setEnabled(false);
                    }
                }

            }

        });
        inputschemaButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                Shell shell = uiParent.getShell();
                MetadataTable inmetadataTable = null;
                if (connection != null) {
                    Set<org.talend.core.model.metadata.builder.connection.MetadataTable> tables = ConnectionHelper
                            .getTables(connection);
                    Iterator it = tables.iterator();
                    while (it.hasNext()) {
                        org.talend.core.model.metadata.builder.connection.MetadataTable metadatatable = (org.talend.core.model.metadata.builder.connection.MetadataTable) it
                                .next();
                        if (metadatatable.getLabel().equals("Input")) {
                            inmetadataTable = metadatatable;
                        }
                    }
                } else {
                    inmetadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
                    inmetadataTable.setLabel("Input");
                    EList schemaMetadataColumn = inmetadataTable.getColumns();
                    for (int i = 0; i < inPutcolumnList.size(); i++) {
                        org.talend.core.model.metadata.MetadataColumn metadatacolumn = (org.talend.core.model.metadata.MetadataColumn) inPutcolumnList
                                .get(i);
                        MetadataColumn column = ConnectionFactory.eINSTANCE.createMetadataColumn();
                        column.setLabel(metadatacolumn.getLabel());
                        column.setTalendType(metadatacolumn.getTalendType());
                        column.setOriginalField(metadatacolumn.getLabel());
                        schemaMetadataColumn.add(column);
                    }
                }
                WSDLSchemaDialog dialog = new WSDLSchemaDialog(shell, inmetadataTable);
                EList columnList = inmetadataTable.getColumns();
                if (dialog != null && dialog.open() == WSDLSchemaDialog.OK) {
                    IMetadataTable inputMetadataTable = connector.getMetadataFromConnector("FLOW");
                    inputMetadataTable.getListColumns().clear();
                    MetadataTable metadataTable = dialog.getMetadataTable();
                    EList columns = metadataTable.getColumns();
                    List<MetadataColumn> inputList = new ArrayList<MetadataColumn>();
                    List<org.talend.core.model.metadata.IMetadataColumn> llist = new ArrayList<org.talend.core.model.metadata.IMetadataColumn>();
                    for (int i = 0; i < columns.size(); i++) {
                        MetadataColumn column = (MetadataColumn) columns.get(i);
                        org.talend.core.model.metadata.MetadataColumn metadatacolumn = new org.talend.core.model.metadata.MetadataColumn();
                        metadatacolumn.setLabel(column.getLabel());
                        metadatacolumn.setTalendType(column.getTalendType());
                        metadatacolumn.setOriginalDbColumnName(column.getLabel());
                        llist.add(metadatacolumn);
                    }
                    inPutcolumnList.clear();
                    inPutcolumnList.addAll(llist);
                    if (inputMetaCopy != null) {
                        inputMetaCopy.getListColumns().clear();
                        inputMetaCopy.setListColumns(llist);
                    }
                    inputMetadataTable.setListColumns(llist);
                    columnInPutTableView.getTableViewerCreator().getTableViewer().refresh();
                    synchronizeInputLinks(tabTotabLinkForin, llist);
                }
            }
        });
        addListButForIn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = uiParent.getShell();
                // if (currentInputMappingData.getParameter().getArraySize() != 0) {
                // AddArrayIndexDialog arrayDialog = new AddArrayIndexDialog(shell,
                // currentInputMappingData.getParameter());
                // if (arrayDialog.open() == AddArrayIndexDialog.OK) {
                // int index = arrayDialog.returnIndex();
                // if (index != -1) {
                // ParameterInfo paraInfo = currentInputMappingData.getParameter();
                // ParameterInfo childPara = paraInfo.getParameterInfos().get(index);
                // InputMappingData data = new InputMappingData();
                // data.setParameter(childPara);
                // data.setParameterName(childPara.getName());
                // expressinPutTableView.getExtendedTableModel().add(data, currentElementIndexForIn + 1);
                // }
                // }
                // return;
                // }
                TableItem[] items = expressTableForIn.getSelection();
                if (items.length <= 0) {
                    return;
                }
                // if select is array.
                ParameterInfo para = ((InputMappingData) items[0].getData()).getParameter();

                if (para.getArraySize() == -1 && para.getParameterInfos().isEmpty()) {
                    InputDialog dlg = new InputDialog(shell,
                            "", Messages.getString("AddListDialog.INPUTINDEX"), "", new InputIndexValidator()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (dlg.open() == dlg.OK) {
                        String indexValue = dlg.getValue();
                        InputMappingData data = new InputMappingData();
                        data.setParameter(para);
                        if (para.getParent() != null) {
                            String name = ((InputMappingData) items[0].getData()).getParameterName() + "[" + indexValue + "]";
                            data.setParameterName(name);
                        } else {
                            data.setParameterName(para.getName());
                        }
                        inputMappingList.size();
                        // expressinPutTableView.getExtendedTableModel().add(data, currentElementIndexForIn + 1);
                        expressinPutTableView.getExtendedTableModel().add(data, inputMappingList.size() + 1);
                        expressTableForIn.select(currentElementIndexForIn);
                        expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();
                    }
                    return;
                }

                // if select is list.
                dialogInputList = new AddListDialog(shell, ((InputMappingData) items[0].getData()).getParameter(), "input",
                        URLValue, serverConfig);
                dialogInputList.setTitle(Messages.getString("WebServiceUI.ParameterTree")); //$NON-NLS-1$
                Rectangle boundsMapper = new Rectangle(50, 50, 100, 50);// ExternalWebServiceUIProperties.getBoundsMapper
                // ();
                if (ExternalWebServiceUIProperties.isShellMaximized()) {
                    dialogInputList.setMaximized(ExternalWebServiceUIProperties.isShellMaximized());
                } else {
                    boundsMapper = ExternalWebServiceUIProperties.getBoundsMapper();
                    if (boundsMapper.x < 0) {
                        boundsMapper.x = 0;
                    }
                    if (boundsMapper.y < 0) {
                        boundsMapper.y = 0;
                    }
                    dialogInputList.setSize(boundsMapper);
                    if (dialogInputList.open() == AddListDialog.OK) {

                        multiSelectedInChildren = dialogInputList.getParamList();
                        if (multiSelectedInChildren != null && !multiSelectedInChildren.isEmpty()) {
                            int selectAmount = multiSelectedInChildren.size();
                            for (int i = 0; i < selectAmount; i++) {
                                ParameterInfo paramet = multiSelectedInChildren.get(i);
                                InputMappingData data = new InputMappingData();
                                data.setParameter(paramet);
                                if (paramet.getParent() != null) {
                                    String name = dialogInputList.getParaUtil().getArrayFullName(paramet);
                                    if (name.contains("anyType{") && name.contains(".simpletype:")) {
                                        int index = name.indexOf(".simpletype:");
                                        name = name.substring(0, index);
                                    }
                                    if (name.contains("parameters.anyType{")) {
                                        name = name.substring(0, 10) + ":" + name.substring(11, name.length());
                                    }
                                    data.setParameterName(name);
                                } else {
                                    data.setParameterName(paramet.getName());

                                }
                                if (currentElementIndexForIn == 0) {
                                    expressinPutTableView.getExtendedTableModel().add(data, -1);
                                } else {
                                    expressinPutTableView.getExtendedTableModel().add(data, currentElementIndexForIn + i + 1);
                                }
                            }

                            expressTableForIn.select(currentElementIndexForIn);
                            expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();
                        } else {
                            currentSelectedInChildren = dialogInputList.getSelectedParaInfo();
                            if (currentSelectedInChildren == null || currentSelectedInChildren.getName() == null) {
                                return;
                            }
                            if (currentElementIndexForIn >= 0) {
                                // if (currentSelectedInChildren.getParent() == currentInputMappingData.getParameter())
                                // {
                                InputMappingData data = new InputMappingData();
                                data.setParameter(currentSelectedInChildren);
                                // else if (para.getParent() != null) {
                                // String name = new ParameterInfoUtil().getParentName(para);
                                //                                return " |-- " + name; //$NON-NLS-1$
                                // }
                                if (currentSelectedInChildren.getParent() != null) {
                                    String name = dialogInputList.getParaUtil().getArrayFullName(currentSelectedInChildren);
                                    String element = name.substring(name.lastIndexOf("."), name.length());
                                    data.setParameterName(name);
                                    // if (connection != null) {
                                    // data.setInputColumnValue("input" + element.toLowerCase());
                                    // } else {
                                    // data.setInputColumnValue("row1" + element.toLowerCase());
                                    // }
                                } else {
                                    data.setParameterName(currentSelectedInChildren.getName());

                                }
                                if (currentElementIndexForIn == 0) {
                                    expressinPutTableView.getExtendedTableModel().add(data, -1);
                                } else {
                                    expressinPutTableView.getExtendedTableModel().add(data, currentElementIndexForIn + 1);
                                }
                                expressTableForIn.select(currentElementIndexForIn);
                                expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();

                            }
                        }
                    }

                }

            }

        });

        removeButForIn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = expressTableForIn.getSelection();
                for (TableItem item : items) {
                    InputMappingData info = (InputMappingData) item.getData();
                    List<LinkDescriptor<TableItem, Object, Table, Object>> links = tabTotabLinkForin.getLinksManager().getLinks();
                    List<LinkDescriptor<TableItem, Object, Table, Object>> removeLinks = new ArrayList<LinkDescriptor<TableItem, Object, Table, Object>>();
                    for (LinkDescriptor<TableItem, Object, Table, Object> link : links) {
                        InputMappingData dataItem = (InputMappingData) link.getExtremity2().getDataItem();
                        if (info == dataItem) {
                            removeLinks.add(link);
                        }
                    }
                    for (LinkDescriptor<TableItem, Object, Table, Object> link : removeLinks) {
                        tabTotabLinkForin.getLinksManager().removeLink(link);
                    }
                    tabTotabLinkForin.getBackgroundRefresher().refreshBackground();
                    // if (info.getInputColumnValue() != null && !"".equals(info.getInputColumnValue())) {
                    // // info.getInputColumnValue().
                    // info.setInputColumnValue(null);
                    // info.getMetadataColumnList().clear();
                    // }
                    expressinPutTableView.getExtendedTableModel().remove(info);
                }

                items = expressTableForIn.getSelection();
                if ((((InputMappingData) items[0].getData()).getParameter()).getParameterInfos().size() == 0) {
                    addListButForIn.setEnabled(false);
                }
                if ((((InputMappingData) items[0].getData()).getParameter()).getParent() == null) {
                    removeButForIn.setEnabled(false);
                }
                expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();
                // columnInPutTableView.getTableViewerCreator().getTableViewer().refresh();
            }

        });

        normalizeButForIn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String columnName = currentInputMappingData.getInputColumnValue();
                if (!(columnName.contains("normalize(") && columnName.contains(",\":\")"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    ExtendedTableModel model = expressinPutTableView.getExtendedTableModel();
                    currentInputMappingData.setInputColumnValue("normalize(" + columnName + ",\":\")"); //$NON-NLS-1$ //$NON-NLS-2$
                    model.remove(currentElementIndexForIn);
                    model.add(currentInputMappingData, currentElementIndexForIn);
                }

            }

        });

        denorButForIn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String columnName = currentInputMappingData.getInputColumnValue();
                if (!(columnName.contains("denormalize(") && columnName.contains(",\":\")"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    ExtendedTableModel model = expressinPutTableView.getExtendedTableModel();
                    currentInputMappingData.setInputColumnValue("denormalize(" + columnName + ",\":\")"); //$NON-NLS-1$ //$NON-NLS-2$
                    model.remove(currentElementIndexForIn);
                    model.add(currentInputMappingData, currentElementIndexForIn);
                }
            }

        });
    }

    private Composite createOutputMappingStatus() {
        SashForm outputComposite = new SashForm(tabFolder, SWT.NONE);
        GridLayout layout = new GridLayout(10, false);
        layout.marginWidth = 20;
        layout.marginHeight = 20;
        layout.horizontalSpacing = 150;

        outputComposite.setLayout(layout);
        outputComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        outputComposite.setBackgroundMode(SWT.INHERIT_FORCE);

        Composite comforRow = new Composite(outputComposite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        comforRow.setLayoutData(data);
        comforRow.setLayout(new GridLayout());

        Composite butComRow = new Composite(comforRow, SWT.NONE);
        butComRow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        butComRow.pack();
        GridLayout butComLayoutRow = new GridLayout();
        butComLayoutRow.numColumns = 4;
        butComLayoutRow.verticalSpacing = 6;
        butComRow.setLayout(butComLayoutRow);
        GridData butComData = new GridData();
        butComData.horizontalSpan = 1;

        addListButForOut = new Button(butComRow, SWT.PUSH);
        addListButForOut.setEnabled(false);
        addListButForOut.setImage(ImageProvider.getImage(EImage.ADD_ICON));
        addListButForOut.setToolTipText(Messages.getString("WebServiceUI.Add_List_element")); //$NON-NLS-1$
        addListButForOut.setLayoutData(butComData);
        addListButForOut.pack();

        removeButForout = new Button(butComRow, SWT.PUSH);
        removeButForout.setEnabled(false);
        removeButForout.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
        removeButForout.setToolTipText(Messages.getString("WebServiceUI.Remove_element")); //$NON-NLS-1$
        removeButForout.setLayoutData(butComData);
        removeButForout.pack();

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = SWT.END;
        autoMapButtonForOut = new Button(butComRow, SWT.PUSH);
        autoMapButtonForOut.setLayoutData(gd);
        autoMapButtonForOut.setText("Auto Map");
        autoMapButtonForOut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                autoMapForOutput();
            }
        });

        SashForm safhExRow = new SashForm(comforRow, SWT.NONE);
        safhExRow.pack();
        safhExRow.setLayoutData(new GridData(GridData.FILL_BOTH));
        safhExRow.setLayout(new FillLayout());

        ExtendedTableModel<OutPutMappingData> model = new ExtendedTableModel<OutPutMappingData>("OUTPUTELEMENT", outParaList); //$NON-NLS-1$
        rowoutPutTableView = new AbstractDataTableEditorView<OutPutMappingData>(safhExRow, SWT.NONE, model, false, true, false) {

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<OutPutMappingData> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(true);
                newTableViewerCreator.setVerticalScroll(true);
                newTableViewerCreator.setReadOnly(true);
            }

            @Override
            protected void createColumns(TableViewerCreator<OutPutMappingData> tableViewerCreator, final Table table) {
                ExtendedTableModel<OutPutMappingData> tableModel = this.getExtendedTableModel();

                TableViewerCreatorColumn rowColumn = new TableViewerCreatorColumn(tableViewerCreator);
                rowColumn.setTitle(Messages.getString("WebServiceUI.ELEMENT")); //$NON-NLS-1$
                rowColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutPutMappingData, String>() {

                    @Override
                    public String get(OutPutMappingData bean) {

                        ParameterInfo para = bean.getParameter();

                        if (para != null) {
                            List<ParameterInfo> paraList = new ParameterInfoUtil().getAllChildren(para);// para.
                            if (paraList != null && paraList.size() > 0 && para.getParent() == null) {
                                boolean flag = false;
                                if (TableViewerCreator.getRecommandLazyLoad()) {
                                    flag = isExpanded4Output(tableModel.getBeansList(), para, paraList);
                                } else {
                                    flag = isExpanded(table, para, paraList, "OUT"); //$NON-NLS-1$
                                }
                                if (flag) {
                                    return "[-] " + para.getName(); //$NON-NLS-1$
                                }
                                return "[+] " + para.getName();// bean.getParameterName(); //$NON-NLS-1$
                            } else if (para.getParent() != null) {
                                String name = "";
                                if (bean.getParameterName() != null) {
                                    name = bean.getParameterName();
                                } else {
                                    name = new ParameterInfoUtil().getParentName(para);
                                }
                                return " |-- " + name; //$NON-NLS-1$
                            }
                            // return para.getName();
                        }
                        if (bean.getParameterName() == null) {
                            return para.getName();
                        }
                        return bean.getParameterName();
                    }

                    @Override
                    public void set(OutPutMappingData bean, String value) {
                        if (value.contains("[+] ")) { //$NON-NLS-1$
                            value.replace("[+] ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        } else if (value.contains("[-] ")) { //$NON-NLS-1$
                            value.replace("[-] ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        // else if (value.contains(" [-] ")) {
                        // value.replace(" [-] ", "");
                        // }
                        else if (value.contains(" |-- ")) { //$NON-NLS-1$
                            value.replace(" |-- ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        bean.setParameterName(value);
                    }

                });
                rowColumn.setWeight(60);
                rowColumn.setModifiable(true);
                rowColumn.setMinimumWidth(60);
                rowColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));

            }

        };
        rowTableForout = rowoutPutTableView.getTable();
        new DragAndDropForWebService(webServiceManager, rowoutPutTableView, null, connector, true, false);

        SashForm comforRow1 = new SashForm(outputComposite, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        comforRow1.setLayoutData(data);

        Composite comforExp = new Composite(outputComposite, SWT.BORDER);
        data = new GridData(GridData.FILL_BOTH);
        // data.horizontalSpan = 5;
        // data.widthHint = 420;
        // data.heightHint = 350;
        comforExp.setLayoutData(data);
        comforExp.setLayout(new GridLayout());

        Composite butCom = new Composite(comforExp, SWT.NONE);
        butCom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        butCom.pack();
        GridLayout butComLayout = new GridLayout();
        butComLayout.numColumns = 2;
        butComLayout.verticalSpacing = 6;
        butCom.setLayout(butComLayout);
        butComData = new GridData(GridData.FILL_BOTH);
        butComData.horizontalSpan = 1;

        Label schemaLabel = new Label(butCom, SWT.RIGHT);
        schemaLabel.setText("Edit Schema:");
        schemaLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        schemaButton = new Button(butCom, SWT.PUSH);
        schemaButton.setText("...");

        normalizeButForOut = new Button(butCom, SWT.PUSH);
        normalizeButForOut.setEnabled(false);
        normalizeButForOut.setText(Messages.getString("WebServiceUI.Normalize")); //$NON-NLS-1$
        normalizeButForOut.setLayoutData(butComData);
        normalizeButForOut.pack();
        denorButForOut = new Button(butCom, SWT.PUSH);
        denorButForOut.setEnabled(false);
        denorButForOut.setText(Messages.getString("WebServiceUI.Denormalize")); //$NON-NLS-1$
        denorButForOut.setLayoutData(butComData);
        denorButForOut.pack();

        SashForm safhEx = new SashForm(comforExp, SWT.NONE);
        safhEx.pack();
        safhEx.setLayoutData(new GridData(GridData.FILL_BOTH));
        safhEx.setLayout(new FillLayout());

        ExtendedTableModel<OutPutMappingData> targetModel = new ExtendedTableModel<OutPutMappingData>("OUTPUTMAPPING", //$NON-NLS-1$
                outPutcolumnList);
        expressoutPutTableView = new AbstractDataTableEditorView<OutPutMappingData>(safhEx, SWT.NONE | SWT.MULTI
                | SWT.FULL_SELECTION | SWT.SINGLE, targetModel, false, true, false) {

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<OutPutMappingData> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(true);
                newTableViewerCreator.setVerticalScroll(true);
                newTableViewerCreator.setReadOnly(false);
            }

            @Override
            protected void createColumns(TableViewerCreator<OutPutMappingData> tableViewerCreator, final Table table) {
                TableViewerCreatorColumn<OutPutMappingData, String> expressionColumn = new TableViewerCreatorColumn<OutPutMappingData, String>(
                        tableViewerCreator);
                expressionColumn.setTitle(Messages.getString("WebServiceUI.EXPRESSION")); //$NON-NLS-1$
                expressionColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutPutMappingData, String>() {

                    @Override
                    public String get(OutPutMappingData bean) {
                        StringBuffer paraName = new StringBuffer();
                        String paraNameof = bean.getParameterName();
                        List<ParameterInfo> paraList = bean.getParameterList();
                        if ("".equals(paraNameof) || paraNameof == null) {
                            return "";
                        }
                        for (ParameterInfo para : paraList) {
                            if (para.getParent() != null) {
                                String name;
                                if (bean.getParameterName() != null) {
                                    name = bean.getParameterName();
                                } else {
                                    name = new ParameterInfoUtil().getParentName(para);
                                }
                                paraName.append(name);
                                paraName.append(" "); //$NON-NLS-1$
                            }
                        }
                        if (!"".equals(paraName.toString())) { //$NON-NLS-1$
                            //                            if (paraNameof.contains("denormalize(") && paraNameof.contains(",':')")) { //$NON-NLS-1$ //$NON-NLS-2$
                            //                                return "denormalize(" + paraName.toString() + ",':')"; //$NON-NLS-1$ //$NON-NLS-2$
                            //                            } else if (paraNameof.contains("normalize(") && paraNameof.contains(",':')")) { //$NON-NLS-1$ //$NON-NLS-2$
                            //                                return "normalize(" + paraName.toString() + ",':')"; //$NON-NLS-1$ //$NON-NLS-2$
                            // }
                            return paraName.toString();
                        } else {
                            return bean.getParameterName();
                        }
                    }

                    @Override
                    public void set(OutPutMappingData bean, String value) {
                        bean.setParameterName(value);

                    }

                });
                expressionColumn.setWeight(40);
                expressionColumn.setModifiable(true);
                expressionColumn.setMinimumWidth(40);
                TextCellEditor cellEditorForOut = new TextCellEditor(tableViewerCreator.getTable());
                expressionColumn.setCellEditor(cellEditorForOut);
                cellEditorForOut.addListener(new DialogErrorForCellEditorListener(cellEditorForOut, expressionColumn) {

                    @Override
                    public void newValidValueTyped(int itemIndex, Object previousValue, Object newValue, CELL_EDITOR_STATE state) {
                        if (state == CELL_EDITOR_STATE.EDITING) {
                            tabTotabLinkForout.onXPathValueChanged(rowTableForout, table, previousValue.toString(),
                                    newValue.toString(), null, itemIndex);
                        }

                    }

                    @Override
                    public String validateValue(String value, int beanPosition) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                });

                TableViewerCreatorColumn<OutPutMappingData, String> elementColumn = new TableViewerCreatorColumn<OutPutMappingData, String>(
                        tableViewerCreator);
                elementColumn.setTitle(Messages.getString("WebServiceUI.COLUMN")); //$NON-NLS-1$
                elementColumn.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutPutMappingData, String>() {

                    @Override
                    public String get(OutPutMappingData bean) {
                        return bean.getOutputColumnValue();
                    }

                    @Override
                    public void set(OutPutMappingData bean, String value) {
                        bean.setOutputColumnValue(value);

                    }

                });
                elementColumn.setWeight(40);
                elementColumn.setModifiable(false);
                elementColumn.setMinimumWidth(40);
                elementColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));
            }

        };

        expressTableForout = expressoutPutTableView.getTable();
        tabTotabLinkForout = new WebServiceTableLiner(outputComposite);
        new DragAndDropForWebService(webServiceManager, expressoutPutTableView, tabTotabLinkForout, connector, false, true);

        backgroundRefresher = new BackgroundRefresher(tabTotabLinkForout);
        Table[] tabs = new Table[1];
        tabs[0] = expressTableForout;
        tabTotabLinkForout.init(rowTableForout, tabs, backgroundRefresher);
        getOutConnList();
        initLinksForOut();
        addListenerForOutputCom();
        outputComposite.setWeights(new int[] { 5, 2, 5 });

        if (WindowSystem.isBigSurOrLater()) {
            backgroundRefresher.setBackgroundColor(tabTotabLinkForout.getBgDrawableComposite().getBackground());
            tabTotabLinkForout.getBgDrawableComposite().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
            Composite comp = expressoutPutTableView.getTable().getParent();
            while (comp != null && comp != outputComposite) {
                comp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
                comp = comp.getParent();
            }
            comp = rowoutPutTableView.getTable().getParent();
            while (comp != null && comp != outputComposite) {
                comp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
                comp = comp.getParent();
            }
            comforRow1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TRANSPARENT));
        }

        return outputComposite;
    }

    private void addListenerForOutputCom() {
        rowTableForout.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = rowTableForout.getSelection();
                currentElementIndexForOut = rowTableForout.getSelectionIndex();
                currentOutputParameter = ((OutPutMappingData) items[0].getData()).getParameter();
                currentOutputMappingData = (OutPutMappingData) items[0].getData();
                // int arraySize = currentOutputParameter.getArraySize();
                List<ParameterInfo> infoList = currentOutputParameter.getParameterInfos();
                ParameterInfo infoPa = currentOutputParameter.getParent();

                Boolean isArray = false;
                String isArrayName = "";
                if (currentOutputMappingData.getParameterName() != null) {
                    isArrayName = currentOutputMappingData.getParameterName();
                }
                if ((currentOutputMappingData.getParameter().getArraySize() == -1 || currentOutputMappingData.getParameter()
                        .getArraySize() > 1) && !isArrayName.endsWith("]")) {
                    isArray = true;
                    isOutPutArray = true;
                }

                if (infoList != null && infoList.size() > 0 || isArray) {
                    addListButForOut.setEnabled(true);
                } else {
                    addListButForOut.setEnabled(false);
                }

                if (infoPa != null) {
                    removeButForout.setEnabled(true);
                } else {
                    removeButForout.setEnabled(false);
                }

            }

        });

        expressTableForout.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = expressTableForout.getSelection();
                if (items == null || items.length == 0)
                    return;
                currentIndexForOutExpress = expressTableForout.getSelectionIndex();
                currentSelectedOutExpress = (OutPutMappingData) items[0].getData();

                if (currentSelectedOutExpress.getParameterList().size() > 1) {
                    denorButForOut.setEnabled(false);
                    normalizeButForOut.setEnabled(false);
                    return;
                }
                List<ParameterInfo> list = currentSelectedOutExpress.getParameterList();
                if (list.size() <= 0) {
                    return;
                }
                List<ParameterInfo> infoList = currentSelectedOutExpress.getParameterList().get(0).getParameterInfos();
                IMetadataColumn column = currentSelectedOutExpress.getMetadataColumn();
                if (column == null) {
                    return;
                }
                Boolean isArray = false;
                String isArrayName = "";
                if (currentSelectedOutExpress != null) {
                    if (currentSelectedOutExpress.getParameterName() != null) {
                        isArrayName = currentSelectedOutExpress.getParameterName();
                    }
                    if ((currentSelectedOutExpress.getParameterList().get(0).getArraySize() != 0) && !isArrayName.endsWith("]")) {
                        isArray = true;
                    }
                    if (currentSelectedOutExpress.getParameterName().contains("*")
                            && !currentSelectedOutExpress.getParameterName().contains("denormalize")) {
                        isArray = true;

                    }
                }
                if (isArray && !(column.getTalendType().equals("id_List"))) { //$NON-NLS-1$
                    denorButForOut.setEnabled(true);
                } else {
                    denorButForOut.setEnabled(false);
                }

                if (column.getTalendType().equals("id_List") && !isArray) { //$NON-NLS-1$
                    normalizeButForOut.setEnabled(true);
                } else {
                    normalizeButForOut.setEnabled(false);
                }

            }

        });

        addListButForOut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = uiParent.getShell();

                TableItem[] items = rowTableForout.getSelection();
                if (items.length <= 0) {
                    return;
                }

                // if select is array.
                ParameterInfo para = ((OutPutMappingData) items[0].getData()).getParameter();
                if (para.getArraySize() == -1 && para.getParameterInfos().isEmpty()) {
                    InputDialog dlg = new InputDialog(shell,
                            "", Messages.getString("AddListDialog.INPUTINDEX"), "", new InputIndexValidator()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    int openCode = dlg.open();
                    if (openCode == InputDialog.OK) {
                        String indexValue = dlg.getValue();
                        OutPutMappingData data = new OutPutMappingData();
                        data.setParameter(para);
                        if (para.getParent() != null) {
                            String name = ((OutPutMappingData) items[0].getData()).getParameterName() + "[" + indexValue + "]";
                            data.setParameterName(name);
                        } else {
                            data.setParameterName(para.getName());
                        }
                        if (currentElementIndexForOut == 0) {
                            rowoutPutTableView.getExtendedTableModel().add(data, -1);
                        } else {
                            rowoutPutTableView.getExtendedTableModel().add(data, currentElementIndexForOut + 1);
                        }
                        rowTableForout.setSelection(currentElementIndexForOut);
                        rowoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
                    }
                    return;
                }

                // if select is a list.
                AddListDialog dialog = new AddListDialog(shell, ((OutPutMappingData) items[0].getData()).getParameter(),
                        "output", URLValue, serverConfig);
                dialog.setTitle(Messages.getString("WebServiceUI.ParameterTree")); //$NON-NLS-1$
                Rectangle boundsMapper = new Rectangle(50, 50, 100, 50);// ExternalWebServiceUIProperties.getBoundsMapper
                if (ExternalWebServiceUIProperties.isShellMaximized()) {
                    dialog.setMaximized(ExternalWebServiceUIProperties.isShellMaximized());
                } else {
                    boundsMapper = ExternalWebServiceUIProperties.getBoundsMapper();
                    if (boundsMapper.x < 0) {
                        boundsMapper.x = 0;
                    }
                    if (boundsMapper.y < 0) {
                        boundsMapper.y = 0;
                    }
                    dialog.setSize(boundsMapper);
                    if (dialog.open() == AddListDialog.OK) {

                        multiSelectedInChildren = dialog.getParamList();
                        if (multiSelectedInChildren != null && !multiSelectedInChildren.isEmpty()) {
                            int selectAmount = multiSelectedInChildren.size();
                            for (int i = 0; i < selectAmount; i++) {
                                ParameterInfo paramet = multiSelectedInChildren.get(i);
                                OutPutMappingData data = new OutPutMappingData();
                                data.setParameter(paramet);
                                if (paramet.getParent() != null) {
                                    String name = dialog.getParaUtil().getArrayFullName(paramet);
                                    if (name.contains("anyType{") && name.contains(".simpletype:")) {
                                        int index = name.indexOf(".simpletype:");
                                        name = name.substring(0, index);
                                    }
                                    if (name.contains("parameters.anyType{")) {
                                        name = name.substring(0, 10) + ":" + name.substring(11, name.length());
                                    }
                                    data.setParameterName(name);
                                } else {
                                    data.setParameterName(paramet.getName());

                                }
                                rowoutPutTableView.getExtendedTableModel().add(data, currentElementIndexForOut + i + 1);
                            }

                            rowoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
                            rowTableForout.setSelection(currentElementIndexForOut);

                        } else {
                            currentSelectedOutChildren = dialog.getSelectedParaInfo();
                            if (currentSelectedOutChildren == null || currentSelectedOutChildren.getName() == null) {
                                return;
                            }
                            if (currentElementIndexForOut >= 0) {
                                OutPutMappingData data = new OutPutMappingData();
                                data.setParameter(currentSelectedOutChildren);
                                if (currentSelectedOutChildren.getParent() != null) {
                                    String name = dialog.getParaUtil().getArrayFullName(currentSelectedOutChildren);
                                    data.setParameterName(name);
                                } else {
                                    data.setParameterName(currentSelectedOutChildren.getName());

                                }

                                rowoutPutTableView.getExtendedTableModel().add(data, currentElementIndexForOut + 1);

                                rowoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
                                rowTableForout.setSelection(currentElementIndexForOut);

                            }
                        }

                    }

                }
            }

        });

        removeButForout.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = rowTableForout.getSelection();
                for (TableItem item : items) {
                    OutPutMappingData info = (OutPutMappingData) item.getData();
                    // ParameterInfo info = (ParameterInfo) items[i].getData();
                    rowoutPutTableView.getExtendedTableModel().remove(info);
                    tabTotabLinkForout.updateLinksStyleAndControlsSelection(rowTableForout, true);
                }
                items = rowTableForout.getSelection();
                if (((OutPutMappingData) items[0].getData()).getParameter().getParameterInfos().size() == 0) {
                    addListButForOut.setEnabled(false);
                }
                if ((((OutPutMappingData) items[0].getData()).getParameter()).getParent() == null) {
                    removeButForout.setEnabled(false);
                }
                rowoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
            }
        });

        normalizeButForOut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = expressTableForout.getSelection();
                OutPutMappingData currentData = (OutPutMappingData) items[0].getData();
                int currentIndex = expressTableForout.getSelectionIndex();
                String parameterName = currentData.getParameterName();
                if (!(parameterName.contains("normalize(") && parameterName.contains(",\":\")"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    ExtendedTableModel model = expressoutPutTableView.getExtendedTableModel();
                    currentData.setParameterName("normalize(" + parameterName + ",\":\")"); //$NON-NLS-1$ //$NON-NLS-2$
                    model.remove(currentIndex);
                    model.add(currentData, currentIndex);
                }
            }
        });

        denorButForOut.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = expressTableForout.getSelection();
                OutPutMappingData currentData = (OutPutMappingData) items[0].getData();
                int currentIndex = expressTableForout.getSelectionIndex();
                String parameterName = ((OutPutMappingData) items[0].getData()).getParameterName();
                if (!(parameterName.contains("denormalize(") && parameterName.contains(",\":\")"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    ExtendedTableModel model = expressoutPutTableView.getExtendedTableModel();
                    currentData.setParameterName("denormalize(" + parameterName + ",\":\")"); //$NON-NLS-1$ //$NON-NLS-2$
                    model.remove(currentIndex);
                    model.add(currentData, currentIndex);
                }
            }
        });

        schemaButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // create the Schema MetadataDialog
                MetadataDialog metaDialog = null;
                List outPutConnections = outputNode.getOutgoingConnections();
                if (inputMetaCopy != null && inputNode != null && outPutConnections != null && !outPutConnections.isEmpty()) {
                    metaDialog = new MetadataDialog(uiParent.getShell(), inputMetaCopy, inputNode, outputMetaCopy, outputNode,
                            null);
                } else {
                    metaDialog = new MetadataDialog(uiParent.getShell(), outputMetaCopy, outputNode, null);
                }

                // IElementParameter param = outputNode.getElementParameter("SCHEMA");
                int openCode = metaDialog.open();
                if (openCode == MetadataDialog.OK) {
                    inputMetaCopy = metaDialog.getInputMetaData();
                    outputMetaCopy = metaDialog.getOutputMetaData();
                    // save modify input schema to inout column.
                    if (inputMetadata != null) {
                        if (inputMetaCopy != null
                                && !inputMetaCopy.sameMetadataAs(temInputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                            List<IMetadataColumn> inNewColumn = inputMetaCopy.getListColumns();
                            inPutcolumnList.clear();
                            inPutcolumnList.addAll(inNewColumn);
                            temInputMetadata = inputMetaCopy.clone();
                            columnInPutTableView.getTableViewerCreator().getTableViewer().refresh();
                        }
                    }

                    if (!outputMetaCopy.sameMetadataAs(temOutputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                        List<IMetadataColumn> outNewColumn = outputMetaCopy.getListColumns();
                        // List<IMetadataColumn> outOldColumnList = temOutputMetadata.getListColumns();

                        if (outNewColumn.isEmpty()) {
                            outPutcolumnList.clear();
                        } else {
                            EList schemaMetadataColumn = null;
                            if (connection != null) {
                                Set<org.talend.core.model.metadata.builder.connection.MetadataTable> tables = ConnectionHelper
                                        .getTables(connection);
                                Iterator it = tables.iterator();
                                while (it.hasNext()) {
                                    org.talend.core.model.metadata.builder.connection.MetadataTable metadatatable = (org.talend.core.model.metadata.builder.connection.MetadataTable) it
                                            .next();
                                    if (metadatatable.getLabel().equals("Output")) {
                                        schemaMetadataColumn = metadatatable.getColumns();
                                    }
                                }
                            }
                            // modify schema name.
                            for (int i = 0; i < outPutcolumnList.size(); i++) {
                                OutPutMappingData outcol = outPutcolumnList.get(i);
                                IMetadataColumn oldCol = outcol.getMetadataColumn();
                                if (!outcol.getOutputColumnValue().equals(oldCol.getLabel())) {
                                    if (connection != null) {
                                        for (int j = 0; j < schemaMetadataColumn.size(); j++) {
                                            MetadataColumn mcolumn = (MetadataColumn) schemaMetadataColumn.get(j);
                                            if (mcolumn.getLabel().equals(outcol.getOutputColumnValue())) {
                                                mcolumn.setLabel(oldCol.getLabel());
                                                mcolumn.setOriginalField(oldCol.getLabel());
                                                mcolumn.setTalendType(oldCol.getTalendType());
                                            }
                                        }
                                    }
                                    outcol.setOutputColumnValue(oldCol.getLabel());
                                }
                            }
                            // add schema.
                            for (int i = 0; i < outNewColumn.size(); i++) {
                                IMetadataColumn newCol = outNewColumn.get(i);
                                if (isAdd(newCol, outPutcolumnList)) {
                                    forOutColumnList.add(newCol);
                                    OutPutMappingData outData = new OutPutMappingData();
                                    outData.setOutputColumnValue(newCol.getLabel());
                                    outData.setMetadataColumn(newCol);
                                    outPutcolumnList.add(outData);
                                    if (connection != null) {
                                        MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                                        metadataColumn.setLabel(newCol.getLabel());
                                        metadataColumn.setOriginalField(newCol.getLabel());
                                        metadataColumn.setTalendType(newCol.getTalendType());
                                        schemaMetadataColumn.add(metadataColumn);
                                    }
                                }

                            }
                            // delete schema.
                            for (int i = 0; i < outPutcolumnList.size(); i++) {
                                OutPutMappingData outcol = outPutcolumnList.get(i);
                                IMetadataColumn oldCol = outcol.getMetadataColumn();
                                // if (!outcol.getOutputColumnValue().equals(oldCol.getLabel())) {
                                // outcol.setOutputColumnValue(oldCol.getLabel());
                                // }
                                if (isDelete(oldCol, outNewColumn)) {
                                    // forOutColumnList.remove(oldCol);
                                    if (schemaMetadataColumn != null) {
                                        for (int j = 0; j < schemaMetadataColumn.size(); j++) {
                                            MetadataColumn mcolumn = (MetadataColumn) schemaMetadataColumn.get(j);
                                            if (mcolumn.getLabel().equals(oldCol.getLabel())) {
                                                schemaMetadataColumn.remove(mcolumn);
                                                j--;
                                            }
                                        }
                                    }
                                    outPutcolumnList.remove(outcol);
                                    i--;
                                }

                            }
                            for (int i = 0; i < outputColumnCompareList.size(); i++) {
                                OutPutMappingData outcol = outputColumnCompareList.get(i);
                                IMetadataColumn oldCol = outcol.getMetadataColumn();
                                if (isDelete(oldCol, outNewColumn)) {
                                    if (schemaMetadataColumn != null) {
                                        for (int j = 0; j < schemaMetadataColumn.size(); j++) {
                                            MetadataColumn mcolumn = (MetadataColumn) schemaMetadataColumn.get(j);
                                            if (mcolumn.getLabel().equals(oldCol.getLabel())) {
                                                schemaMetadataColumn.remove(mcolumn);
                                                j--;
                                            }
                                        }
                                    }

                                }
                                // i--;
                            }
                        }
                        temOutputMetadata = outputMetaCopy.clone();
                        expressoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
                    }
                    synchronizeOutputLinks(tabTotabLinkForout, outputMetaCopy.getListColumns());
                } else if (openCode == MetadataDialog.CANCEL) {
                    outputMetaCopy = temOutputMetadata.clone();
                    return;
                }
            }

            private boolean isDeleteInputColumn(IMetadataColumn oldCol, List<IMetadataColumn> inNewColumn) {
                for (int i = 0; i < inNewColumn.size(); i++) {
                    IMetadataColumn newCol = inNewColumn.get(i);
                    if ((oldCol.getLabel()).equals(newCol.getLabel())) {
                        return false;
                    }
                }
                return true;
            }

            private boolean isAddInputColumn(IMetadataColumn newCol, List<IMetadataColumn> inPutcolumnList) {
                for (int i = 0; i < inPutcolumnList.size(); i++) {

                    if ((newCol.getLabel()).equals(inPutcolumnList.get(i).getLabel())) {
                        return false;
                    }
                }
                return true;
            }

            private boolean isDelete(IMetadataColumn oldCol, List<IMetadataColumn> list) {

                for (int i = 0; i < list.size(); i++) {
                    IMetadataColumn newCol = list.get(i);
                    if ((oldCol.getLabel()).equals(newCol.getLabel())) {
                        return false;
                    }
                }
                return true;
            }

            private boolean isAdd(IMetadataColumn newCol, List<OutPutMappingData> outOldcolumnList) {
                // Iterator<OutPutMappingData> outIter=outPutcolumnList.iterator();
                for (int i = 0; i < outOldcolumnList.size(); i++) {
                    IMetadataColumn outIter = outOldcolumnList.get(i).getMetadataColumn();
                    if ((newCol.getLabel()).equals(outIter.getLabel())) {
                        return false;
                    }
                }
                return true;
            }

        });

    }

    private void initInputMetaCopy() {
        List<? extends IConnection> inConnList = connector.getIncomingConnections();
        if (!inConnList.isEmpty()) {
            for (int i = 0; i < inConnList.size(); i++) {
                IConnection inConn = inConnList.get(i);
                if (inConn == null) {
                    continue;
                }
                inputNode = (inConn.getSource());
                inputMetadata = inConn.getMetadataTable();
            }
            if (inputMetadata != null) {
                inputMetaCopy = inputMetadata.clone();
                temInputMetadata = inputMetadata.clone();
            }
        }
    }

    private void initOutputMetaCopy() {
        outputNode = this.getWebServiceManager().getWebServiceComponent();
        if (outputNode.getComponent() == null) {
            outputNode.setComponent(this.getWebServiceManager().getWebServiceComponent().getComponent());
        }
        if (this.getWebServiceManager().getWebServiceComponent().getMetadataList().size() > 1) {
            outputMetadata = this.connector.getMetadataFromConnector("OUTPUT");
            outputMetaCopy = outputMetadata.clone();
            temOutputMetadata = outputMetadata.clone();
        }
    }

    private UIManager getUIManager() {
        return getWebServiceManager().getUIManager();
    }

    public CTabFolder getTabFolder() {
        return this.tabFolder;
    }

    public CTabItem getOutputTabItem() {
        return this.outputMappingTabItem;
    }

    public Composite getOutputComposite() {
        return this.outputComposite;
    }

    public CTabItem getInputTabItem() {
        return this.inputMappingTabItem;
    }

    public Composite getInputComposite() {
        return this.inputComposite;
    }

    public Composite getWsdlComposite() {
        return this.wsdlComposite;
    }

    // bug 14067
    public String getURL() {
        return URLValue;
    }

    public Function getCurrentFunction() {
        return currentFunction;
    }

    public List<InputMappingData> getInputParams() {
        return expressinPutTableView.getExtendedTableModel().getBeansList();
    }

    public List<IMetadataColumn> getInputValue() {
        return columnInPutTableView.getExtendedTableModel().getBeansList();
    }

    public List<OutPutMappingData> getOutputParams() {
        return expressoutPutTableView.getExtendedTableModel().getBeansList();
    }

    public List<OutPutMappingData> getOutputElement() {
        return rowoutPutTableView.getExtendedTableModel().getBeansList();
    }

    public PortNames getCurrentPortName() {
        return currentPortName;
    }

    public List<PortNames> getAllPortNames() {
        return this.allPortNames;
    }

    public Set<String> getInSourceList() {
        return tabTotabLinkForin.getInputSource();
    }

    public Set<String> getOutSourceList() {
        return tabTotabLinkForout.getOutputSource();
    }

    public int getSelectedColumnIndex() {
        return this.selectedColumnIndex;
    }

    public IMetadataTable getInputMetaCopy() {
        return this.inputMetaCopy;
    }

    public void setInputMetaCopy(IMetadataTable inputMetaCopy) {
        this.inputMetaCopy = inputMetaCopy;
    }

    public IMetadataTable getOutputMetaCopy() {
        return this.outputMetaCopy;
    }

    public void setOutputMetaCopy(IMetadataTable outputMetaCopy) {
        this.outputMetaCopy = outputMetaCopy;
    }

    public IMetadataTable getInputMetadata() {
        return this.inputMetadata;
    }

    public void setInputMetadata(IMetadataTable inputMetadata) {
        this.inputMetadata = inputMetadata;
    }

    public IMetadataTable getOutputMetadata() {
        return this.outputMetadata;
    }

    public void setOutputMetadata(IMetadataTable outputMetadata) {
        this.outputMetadata = outputMetadata;
    }

    public void reInitLinks() {
        initLinksForIn();
        initLinksForOut();
    }

    private void initLinksForIn() {
        // refreshTableView();
        IElementParameter INPUT_PARAMSPara = connector.getElementParameter("INPUT_PARAMS"); //$NON-NLS-1$
        List<Map<String, String>> inputparaValue = (List<Map<String, String>>) INPUT_PARAMSPara.getValue();
        TableItem[] items = rowTableForin.getItems();
        TableItem[] tarItems = expressinPutTableView.getTable().getItems();

        // tableItem.getdata() returned null without this.
        for (TableItem tableItem : tarItems) {
            tableItem.getText();
        }
        for (TableItem item : items) {
            item.getText();
        }
        WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*"); //$NON-NLS-1$
        for (Map<String, String> map : inputparaValue) {
            if (map.get("SOURCE") != null && map.get("SOURCE") instanceof String) { //$NON-NLS-1$ //$NON-NLS-2$
                String source = map.get("SOURCE"); //$NON-NLS-1$
                if ("".equals(source)) { //$NON-NLS-1$
                    continue;
                }

                TableItem sourceItem = null;
                Object sourceData = null;
                goin: for (int i = 0; i < items.length; i++) {
                    if (items[i].getData() != null && ((IMetadataColumn) items[i].getData()).getLabel() != null
                            && ((IMetadataColumn) items[i].getData()).getLabel().equals(source)) {
                        sourceItem = items[i];
                        sourceData = items[i].getData();
                        if (sourceItem == null) {
                            continue;
                        } else {
                            break goin;
                        }
                    }
                }

                if (sourceItem != null) {
                    TableItem targetItem = null;
                    Object targetData = null;
                    for (TableItem tarItem : tarItems) {
                        if (tarItem.getData() == null) {
                            continue;
                        }
                        String columnName = ((InputMappingData) tarItem.getData()).getInputColumnValue();
                        if (columnName == null || "".equals(columnName)) { //$NON-NLS-1$
                            continue;
                        }

                        Map<String, String> itemNamemap = webParser.parseInTableEntryLocations(columnName);
                        Set<Entry<String, String>> set = itemNamemap.entrySet();
                        Iterator<Entry<String, String>> ite = set.iterator();
                        while (ite.hasNext()) {
                            Entry<String, String> entry = ite.next();
                            String columnValue = entry.getKey();
                            String rowValue = entry.getValue();

                            if (columnValue.equals(source)) {
                                targetItem = tarItem;
                                targetData = tarItem.getData();
                                if (targetItem == null) {
                                    continue;
                                }
                                tabTotabLinkForin.addLinks(sourceItem, sourceData, targetItem.getParent(), targetData,
                                        "INPUTMAPPING"); //$NON-NLS-1$
                            }

                            if (!rowValue.equals(inComeName)) {

                            }
                        }

                    }
                }
            }

        }
    }

    private void initLinksForOut() {
        IElementParameter OUTPUT_PARAMSPara = connector.getElementParameter("OUTPUT_PARAMS"); //$NON-NLS-1$
        List<Map<String, String>> outputMap = (List<Map<String, String>>) OUTPUT_PARAMSPara.getValue();
        TableItem[] items = rowoutPutTableView.getTable().getItems();
        TableItem[] tarItems = expressoutPutTableView.getTable().getItems();
        // tabeItem.getData() returned null without this.
        for (TableItem tableItem : tarItems) {
            tableItem.getText();
        }
        for (TableItem item : items) {
            item.getText();
        }
        WebServiceExpressionParser webParser = new WebServiceExpressionParser("\\s*\\w+(\\[\\d+?\\])?\\s*"); //$NON-NLS-1$
        for (Map<String, String> map : outputMap) {
            if (map.get("SOURCE") != null && map.get("SOURCE") instanceof String) { //$NON-NLS-1$ //$NON-NLS-2$
                String source = map.get("SOURCE"); //$NON-NLS-1$

                TableItem sourceItem = null;
                Object sourceData = null;
                goin: for (int i = 0; i < items.length; i++) {
                    if (items[i].getData() != null && ((OutPutMappingData) items[i].getData()).getParameter().getName() != null) {
                        String sourseName = ((OutPutMappingData) items[i].getData()).getParameterName();
                        // int m = sourseName.lastIndexOf(".");
                        // sourseName = sourseName.substring(m + 1);
                        if (sourseName != null && sourseName.equals(source)) {
                            sourceItem = items[i];
                            sourceData = items[i].getData();
                            if (sourceItem == null) {
                                continue;
                            } else {
                                break goin;
                            }
                        }
                    }
                }
                if (sourceItem != null) {
                    TableItem targetItem = null;
                    Object targetData = null;
                    for (TableItem tarItem : tarItems) {
                        if (tarItem.getData() == null) {
                            continue;
                        }
                        String columnName = ((OutPutMappingData) tarItem.getData()).getParameterName();
                        if (columnName == null || "".equals(columnName)) { //$NON-NLS-1$
                            continue;
                        }
                        Set<String> set = webParser.parseOutTableEntryLocations(columnName);
                        if (columnName.indexOf("normalize") == 0) {
                            columnName = columnName.trim();
                            columnName = columnName.substring(10);
                            int index = columnName.indexOf(",");
                            columnName = columnName.substring(0, index);
                        }
                        if (columnName.indexOf("denormalize") == 0) {
                            columnName = columnName.trim();
                            columnName = columnName.substring(12);
                            int index = columnName.indexOf(",");
                            columnName = columnName.substring(0, index);
                        }
                        Set<String> set1 = webParser.parseOutTableEntryLocations(columnName);
                        // Set<String> set = webParser.parseOutTableEntryLocations(columnName);
                        // Iterator<String> ite = set.iterator();
                        // while (ite.hasNext()) {
                        // String columnValue = ite.next();
                        if (columnName.equals(source)) {
                            targetItem = tarItem;
                            targetData = tarItem.getData();

                            if (targetItem == null) {
                                continue;
                            }
                            tabTotabLinkForout.addLinks(sourceItem, sourceData, targetItem.getParent(), targetData,
                                    "OUTPUTMAPPING"); //$NON-NLS-1$
                        }
                        // }

                    }
                }
            }
        }
    }

    public void saveProperties() {
        getWebServiceManager().savePropertiesToComponent();
    }

    public Boolean getIsFirst() {
        return this.isFirst;
    }

    public void prepareClosing(int dialogResponse) {

    }

    // private List<ParameterInfo> getAllMostParameterInfo(ParameterInfo para, String mark) {
    // ParameterInfo parentPara = para.getParent();
    // ParameterInfo paraToCom = null;
    // if (mark.equals("IN")) {
    // paraToCom = currentInputMappingData.getParameter();
    // } else if (mark.equals("OUT")) {
    // paraToCom = currentOutputParameter;
    // }
    // List<ParameterInfo> list = new ArrayList<ParameterInfo>();
    // if (parentPara != paraToCom) {
    // list.add(para);
    // List<ParameterInfo> pali = getAllMostParameterInfo(parentPara, mark);
    // list.addAll(pali);
    // } else {
    // list.add(para);
    // }
    // return list;
    // }
    //
    // private List<ParameterInfo> getAllParameterInfo(ParameterInfo para) {
    // ParameterInfo parentPara = para.getParent();
    // List<ParameterInfo> list = new ArrayList<ParameterInfo>();
    // if (parentPara != null) {
    // list.add(para);
    // List<ParameterInfo> pali = getAllParameterInfo(parentPara);
    // list.addAll(pali);
    // } else {
    // list.add(para);
    // }
    // return list;
    // }
    //
    // private String getParentName(ParameterInfo para) {
    // List<ParameterInfo> paraList = getAllParameterInfo(para);
    // StringBuffer buffer = new StringBuffer();
    // for (int i = paraList.size() - 1; i >= 0; i--) {
    // ParameterInfo parentPara = paraList.get(i);
    // if (parentPara == null) {
    // continue;
    // }
    // buffer.append(parentPara.getName());
    // if (i != 0) {
    // buffer.append(".");
    // }
    //
    // }
    // return buffer.toString();
    // }

    private boolean isExpanded(Table table, ParameterInfo para, List<ParameterInfo> paraList, String mark) {
        boolean flag = false;
        if (mark.equals("IN")) { //$NON-NLS-1$
            // if (currentInputMappingData == null) {
            // return false;
            // }

            TableItem[] items = table.getItems();
            int index = -1;
            for (int i = 0; i < items.length; i++) {
                if (((InputMappingData) items[i].getData()).getParameter() == para) {
                    index = i;
                }
            }
            if (index + 1 < items.length) {
                ParameterInfo paraToComp = ((InputMappingData) items[index + 1].getData()).getParameter();
                for (ParameterInfo info : paraList) {
                    if (paraToComp == info) {
                        flag = true;
                        return flag;
                    }
                }
            }

        } else if (mark.equals("OUT")) { //$NON-NLS-1$
            // if (currentOutputParameter == null) {
            // return false;
            // }

            TableItem[] items = table.getItems();
            int index = -1;
            for (int i = 0; i < items.length; i++) {
                if (items[i].getData() == para) {
                    index = i;
                }
            }
            if (index + 1 < items.length) {
                ParameterInfo paraToComp = ((OutPutMappingData) items[index + 1].getData()).getParameter();
                for (ParameterInfo info : paraList) {
                    if (paraToComp == info) {
                        flag = true;
                        return flag;
                    }
                }
            }
        }

        // List<ParameterInfo> paraList = currentInputMappingData.getParameter().getParameterInfos();

        return flag;
    }

    /**
     * For Lazyload, table maybe didn't load data completely, use TableModel to judge if is expanded DOC jding Comment
     * method "isExpanded".
     *
     * @param tableBeansList
     * @param para
     * @param childparaList
     * @return
     */
    private boolean isExpanded4Input(List<InputMappingData> tableBeansList, ParameterInfo para,
            List<ParameterInfo> childparaList) {
        boolean flag = false;
        int index = -1;
        for (int i = 0; i < tableBeansList.size(); i++) {
            if (tableBeansList.get(i).getParameter() == para) {
                index = i;
            }
        }

        if (index + 1 < tableBeansList.size()) {
            ParameterInfo paraToComp = tableBeansList.get(index + 1).getParameter();
            for (ParameterInfo info : childparaList) {
                if (paraToComp == info) {
                    flag = true;
                    return flag;
                }
            }
        }
        return flag;
    }

    /**
     * For Lazyload, table maybe didn't load data completely, use TableModel to judge if is expanded DOC jding Comment
     * method "isExpanded4Output".
     *
     * @param tableBeansList
     * @param para
     * @param childparaList
     * @return
     */
    private boolean isExpanded4Output(List<OutPutMappingData> tableBeansList, ParameterInfo para,
            List<ParameterInfo> childparaList) {
        boolean flag = false;
        int index = -1;
        for (int i = 0; i < tableBeansList.size(); i++) {
            if (tableBeansList.get(i).getParameter() == para) {
                index = i;
            }
        }
        if (index + 1 < tableBeansList.size()) {
            ParameterInfo paraToComp = tableBeansList.get(index + 1).getParameter();
            for (ParameterInfo info : childparaList) {
                if (paraToComp == info) {
                    flag = true;
                    return flag;
                }
            }
        }

        return flag;
    }

    public Table getTable() {
        // TODO Auto-generated method stub
        return listTable;// webServiceUI.getTable();
    }

    public LabelledFileField getWSDLLabel(Boolean b) {
        refreshbut.setEnabled(!b);
        return wsdlField;
    }

    public void saveInputValue() {
        if (connection != null) {
            if (currentFunction != null) {
                if (currentFunction.getName() != null) {
                    connection.setMethodName(currentFunction.getName());
                }
                if (currentFunction.getServerNameSpace() != null) {
                    connection.setServerNameSpace(currentFunction.getServerNameSpace());
                }
                if (currentFunction.getServerName() != null) {
                    connection.setServerName(currentFunction.getServerName());
                }
                if (currentFunction.getServerNameSpace() != null) {
                    connection.setPortNameSpace(currentFunction.getServerNameSpace());
                }
            }
            if (currentPortName != null) {
                connection.setPortName(currentPortName.getPortName());
            } else if (currentPortName == null && !allPortNames.isEmpty()) {
                currentPortName = allPortNames.get(0);
                connection.setPortName(currentPortName.getPortName());
            }

        }
        EList inputValue = connection.getParameterValue();

        IElementParameter INPUT_PARAMSPara = connector.getElementParameter("INPUT_PARAMS");
        List<Map<String, String>> inputparaValue = (List<Map<String, String>>) INPUT_PARAMSPara.getValue();
        if (inputparaValue != null) {
            inputValue.clear();

            for (InputMappingData inputData : getInputParams()) {
                WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                Map<String, String> inputMap = new HashMap<String, String>(2);
                if (inputData.getInputColumnValue() != null) {
                    parameter.setExpression(inputData.getInputColumnValue());
                }

                if (inputData.getParameterName() != null) {
                    String name = inputData.getParameterName();
                    if (!name.equals("")) {
                        parameter.setElement(name);
                    }
                } else if (inputData.getParameterName() == null && inputData.getParameter() != null) {
                    if (inputData.getParameter().getParent() != null) {
                        String name = new ParameterInfoUtil().getParentName(inputData.getParameter());
                        parameter.setElement(name);
                    } else {
                        parameter.setElement(inputData.getParameter().getName());
                    }
                }

                inputValue.add(parameter);
            }
            if (currentFunction != null) {
                List inputParameter = currentFunction.getInputParameters();
                if (inputParameter != null) {

                    boolean mark = true;
                    List<ParameterInfo> ls = new ArrayList();
                    goin: for (Iterator iterator2 = inputParameter.iterator(); iterator2.hasNext();) {
                        ParameterInfo element = (ParameterInfo) iterator2.next();
                        WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                        parameter.setParameterInfo(element.getName());
                        if (element.getParent() == null) {
                            parameter.setParameterInfoParent("");
                        } else {
                            parameter.setParameterInfoParent(element.getParent().getName());
                        }
                        inputValue.add(parameter);
                        mark = false;
                        if (!element.getParameterInfos().isEmpty()) {
                            ls.addAll(new ParameterInfoUtil().getAllChildren(element));
                        }
                        break goin;
                    }
                    if (!mark) {
                        for (ParameterInfo para : ls) {
                            WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                            parameter.setParameterInfo(para.getName());
                            parameter.setParameterInfoParent(para.getParent().getName());
                            inputValue.add(parameter);
                        }
                    }

                }
            }
            for (IMetadataColumn column : getInputValue()) {
                WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                if (column.getLabel() != null) {
                    parameter.setColumn(column.getLabel());
                    inputValue.add(parameter);
                }
            }
            for (String insource : getInSourceList()) {
                WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                if (insource == null || "".equals(insource)) {
                    continue;
                }
                // Map<String, String> sourceMap = new HashMap<String, String>(1);
                parameter.setSource(insource);
                inputValue.add(parameter);
            }
        }
    }

    public void saveOutPutValue() {
        // save output
        EList outPutValue = connection.getOutputParameter();

        List<ParameterInfo> ls = new ArrayList();
        IElementParameter OUTPUT_PARAMSPara = connector.getElementParameter("OUTPUT_PARAMS");
        List<Map<String, String>> outputMap = (List<Map<String, String>>) OUTPUT_PARAMSPara.getValue();
        if (outputMap != null) {
            outPutValue.clear();
            for (OutPutMappingData outData : getOutputElement()) {
                WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                ParameterInfo para = outData.getParameter();
                if (para.getName() == null || "".equals(para.getName())) {
                    continue;
                }

                Map<String, String> eleMap = new HashMap<String, String>(3);
                if (outData.getParameterName() != null) {
                    String name = outData.getParameterName();
                    if (!name.equals("")) {
                        parameter.setElement(name);
                    }
                } else {
                    parameter.setElement(para.getName());
                }
                outPutValue.add(parameter);
            }
            if (currentFunction != null) {
                List inputParameter = currentFunction.getOutputParameters();
                if (inputParameter != null) {
                    // for (int i = 0; i < inputParameter.size(); i++) {
                    boolean mark = true;
                    goin: for (Iterator iterator2 = inputParameter.iterator(); iterator2.hasNext();) {
                        ParameterInfo element = (ParameterInfo) iterator2.next();
                        WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                        parameter.setParameterInfo(element.getName());
                        if (element.getParent() == null) {
                            parameter.setParameterInfoParent("");
                        } else {
                            parameter.setParameterInfoParent(element.getParent().getName());
                        }
                        outPutValue.add(parameter);
                        // System.out.println(element.getParent() + " ppp");
                        mark = false;
                        if (!element.getParameterInfos().isEmpty()) {
                            ls.addAll(new ParameterInfoUtil().getAllChildren(element));
                        }
                        break goin;
                    }
                    if (!mark) {
                        for (ParameterInfo para : ls) {
                            WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                            parameter.setParameterInfo(para.getName());
                            parameter.setParameterInfoParent(para.getParent().getName());
                            outPutValue.add(parameter);
                        }
                    }
                    // }
                }
            }
            for (OutPutMappingData data : getOutputParams()) {
                WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                Map<String, String> dataMap = new HashMap<String, String>(2);
                if (data.getParameterName() != null) {
                    parameter.setExpression(data.getParameterName());

                } else if (data.getParameterName() == null) {
                    // warningDialog("Please Select a Output Item.");
                    // return;
                }

                if (data.getMetadataColumn() != null) {
                    parameter.setColumn(data.getMetadataColumn().getLabel());
                }
                outPutValue.add(parameter);
            }

            for (String outsource : getOutSourceList()) {
                WSDLParameter parameter = ConnectionFactory.eINSTANCE.createWSDLParameter();
                if (outsource == null || "".equals(outsource)) {
                    continue;
                }
                Map<String, String> sourceMap = new HashMap<String, String>(1);
                parameter.setSource(outsource);
                outPutValue.add(parameter);
            }
        }
    }

    /**
     * DOC PLV Comment method "refreshTableView".
     */
    public void refreshTableView() {
        ExtendedTableModel<InputMappingData> inputModel = expressinPutTableView.getExtendedTableModel();
        boolean removeLinksIn = true;
        goin: for (InputMappingData indata : inputModel.getBeansList()) {
            if (indata.getInputColumnValue() != null && !"".equals(indata.getInputColumnValue())) { //$NON-NLS-1$
                removeLinksIn = false;
                break goin;
            }
        }

        ExtendedTableModel<OutPutMappingData> outputModel = expressoutPutTableView.getExtendedTableModel();
        boolean removeLinksOut = true;
        goout: for (OutPutMappingData outdata : outputModel.getBeansList()) {
            if (outdata.getParameterName() != null && !"".equals(outdata.getParameterName())) { //$NON-NLS-1$
                removeLinksOut = false;
                break goout;
            }
        }

        if (removeLinksIn && removeLinksOut && tabTotabLinkForin != null && tabTotabLinkForout != null) {
            tabTotabLinkForin.clearLinks();
            tabTotabLinkForout.clearLinks();
        }
        expressinPutTableView.getTableViewerCreator().getTableViewer().refresh();
        rowoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
        expressoutPutTableView.getTableViewerCreator().getTableViewer().refresh();
    }

    private void synchronizeInputLinks(TableToTablesLinker<Object, Object> tabTotabLink,
            List<org.talend.core.model.metadata.IMetadataColumn> llist) {
        try {
            List<LinkDescriptor<TableItem, Object, Table, Object>> links = tabTotabLink.getLinksManager().getLinks();
            List<LinkDescriptor<TableItem, Object, Table, Object>> removeLinks = new ArrayList<LinkDescriptor<TableItem, Object, Table, Object>>();
            for (LinkDescriptor<TableItem, Object, Table, Object> link : links) {
                boolean isExistColumn = false;
                IExtremityLink extremity = link.getExtremity1();
                IMetadataColumn c = null;
                if (extremity.getDataItem() instanceof IMetadataColumn) {
                    c = (IMetadataColumn) extremity.getDataItem();
                }
                if (c != null) {
                    for (IMetadataColumn column : llist) {
                        if (c.getLabel().equals(column.getLabel())) {
                            extremity.setDataItem(column);
                            isExistColumn = true;
                            break;
                        }
                    }
                    if (!isExistColumn) {
                        removeLinks.add(link);
                        for (InputMappingData inputData : inputMappingList) {
                            InputMappingData dataItem = (InputMappingData) link.getExtremity2().getDataItem();
                            if (inputData == dataItem) {
                                inputData.setInputColumnValue(StringUtils.remove(inputData.getInputColumnValue(),
                                        "input." + c.getLabel()).trim());
                            }
                        }
                    }
                }
            }
            for (LinkDescriptor<TableItem, Object, Table, Object> link : removeLinks) {
                tabTotabLink.getLinksManager().removeLink(link);
            }
            expressinPutTableView.getExtendedTableViewer().getTableViewerCreator().refresh();
            tabTotabLink.getBackgroundRefresher().refreshBackground();
        } catch (Exception e) {
            tabTotabLink.clearLinks();
        }
    }

    private void synchronizeOutputLinks(TableToTablesLinker<Object, Object> tabTotabLink,
            List<org.talend.core.model.metadata.IMetadataColumn> llist) {
        try {
            List<LinkDescriptor<TableItem, Object, Table, Object>> links = tabTotabLink.getLinksManager().getLinks();
            List<LinkDescriptor<TableItem, Object, Table, Object>> removeLinks = new ArrayList<LinkDescriptor<TableItem, Object, Table, Object>>();
            for (LinkDescriptor<TableItem, Object, Table, Object> link : links) {
                boolean isExistColumn = false;
                IExtremityLink extremity = link.getExtremity2();
                IMetadataColumn c = null;
                if (extremity.getDataItem() instanceof OutPutMappingData) {
                    c = ((OutPutMappingData) extremity.getDataItem()).getMetadataColumn();
                }
                if (c != null) {
                    for (IMetadataColumn column : llist) {
                        if (c.getLabel().equals(column.getLabel())) {
                            ((OutPutMappingData) extremity.getDataItem()).setMetadataColumn(column);
                            isExistColumn = true;
                            break;
                        }
                    }
                }
            }
            for (LinkDescriptor<TableItem, Object, Table, Object> link : removeLinks) {
                tabTotabLink.getLinksManager().removeLink(link);
            }
            tabTotabLink.getBackgroundRefresher().refreshBackground();
        } catch (Exception e) {
            tabTotabLink.clearLinks();
        }
    }

    public void setRepositoryMode() {
        addListButForIn.setEnabled(false);
        addListButForOut.setEnabled(false);
        autoMapButtonForIn.setEnabled(false);
        autoMapButtonForOut.setEnabled(false);
        denorButForIn.setEnabled(false);
        denorButForOut.setEnabled(false);
        expressTableForIn.setEnabled(false);
        expressTableForout.setEnabled(false);
        // inputComposite.setEnabled(false);
        inputschemaButton.setEnabled(false);
        listTable.setEnabled(false);
        listTableView.getMainComposite().setEnabled(false);
        normalizeButForIn.setEnabled(false);
        normalizeButForOut.setEnabled(false);
        // outputComposite.setEnabled(false);
        portListTable.setEnabled(false);
        portListTableView.getMainComposite().setEnabled(false);
        refreshbut.setEnabled(false);
        removeButForIn.setEnabled(false);
        removeButForout.setEnabled(false);
        rowoutPutTableView.getMainComposite().setEnabled(false);
        rowTableForin.setEnabled(false);
        rowTableForout.setEnabled(false);
        schemaButton.setEnabled(false);
        wsdlComposite.setEnabled(false);
        wsdlField.getTextControl().setEditable(false);
        wsdlField.getButtonControl().setEnabled(false);
    }
}
