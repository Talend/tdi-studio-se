package org.talend.designer.webservice.ui.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.webservice.WebServiceComponent;
import org.talend.designer.webservice.WebServiceComponentMain;
import org.talend.designer.webservice.data.ExternalWebServiceUIProperties;
import org.talend.designer.webservice.data.InputMappingData;
import org.talend.designer.webservice.data.OutPutMappingData;
import org.talend.designer.webservice.managers.UIManager;
import org.talend.designer.webservice.ui.ParameterInfoUtil;
import org.talend.designer.webservice.ui.WebServiceUI;
import org.talend.designer.webservice.ws.wsdlinfo.Function;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;
import org.talend.designer.webservice.ws.wsdlinfo.PortNames;

public class WebServiceDialog extends Dialog implements WebServiceEventListener {

    private String title;

    private Rectangle size;

    private WebServiceUI webServiceUI;

    private WebServiceComponentMain webServiceComponentMain;

    private boolean maximized;

    private CTabFolder tabFolder;

    private Button backButton;

    private Button nextButton;

    private boolean isRepositoryMode = false;

    public WebServiceDialog(Shell parentShell, WebServiceComponentMain webServiceComponentMain) {
        super(parentShell);
        this.webServiceComponentMain = webServiceComponentMain;
        setShellStyle(ExternalWebServiceUIProperties.DIALOG_STYLE);
    }

    public WebServiceUI getWebServiceUI() {
        return this.webServiceUI;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (title != null) {
            newShell.setText(title);
        }
        if (maximized) {
            newShell.setMaximized(true);
        } else {
            newShell.setBounds(size);
        }
    }

    public void setTitle(String title) {
        this.title = title;

    }

    public boolean isRepositoryMode() {
        return this.isRepositoryMode;
    }

    public void setRepositoryMode(boolean isRepositoryMode) {
        this.isRepositoryMode = isRepositoryMode;
    }

    /**
     * Sets the size.
     * 
     * @param size the size to set
     */
    public void setSize(Rectangle size) {
        this.size = size;
    }

    /**
     * Sets the maximizedSize.
     * 
     * @param maximizedSize the maxmimizedSize to set
     */
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    private UIManager getUIManager() {
        return webServiceComponentMain.getWebServiceManager().getUIManager();
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.OK_ID == buttonId) {
            okPressed();
        } else if (IDialogConstants.CANCEL_ID == buttonId) {
            cancelPressed();
        } else if (IDialogConstants.NEXT_ID == buttonId) {
            nextPressed();
        } else if (IDialogConstants.BACK_ID == buttonId) {
            backPressed();
        }
    }

    @Override
    protected void cancelPressed() {
        super.cancelPressed();
        getUIManager().setDialogResponse(SWT.CANCEL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, false);
        createButton(parent, IDialogConstants.NEXT_ID, IDialogConstants.NEXT_LABEL, false);
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        backButton = getButton(IDialogConstants.BACK_ID);
        backButton.setEnabled(false);
        nextButton = getButton(IDialogConstants.NEXT_ID);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(panel);

        webServiceUI = new WebServiceUI(panel, this.webServiceComponentMain);
        webServiceUI.addListener(this);
        webServiceUI.init();
        if (isRepositoryMode) {
            webServiceUI.setRepositoryMode();
        }

        return panel;
    }

    protected void backPressed() {
        tabFolder = webServiceUI.getTabFolder();
        int curreSelect = tabFolder.getSelectionIndex();
        if (curreSelect > 0) {
            tabFolder.setSelection(curreSelect - 1);
            if (!nextButton.getEnabled()) {
                nextButton.setEnabled(true);
            }
            if ((curreSelect - 1) == 0) {
                backButton.setEnabled(false);
            }
        }
    }

    protected void nextPressed() {
        tabFolder = webServiceUI.getTabFolder();
        Function function = webServiceUI.getCurrentFunction();
        int curreSelect = tabFolder.getSelectionIndex();
        if (function == null) {
            warningDialog("Please Select a Operation!");
        } else if (curreSelect < 2 && function != null) {
            if (webServiceUI.getIsFirst()) {
                webServiceUI.initWebserviceData();
            }
            tabFolder.setSelection(curreSelect + 1);
            if (!backButton.getEnabled()) {
                backButton.setEnabled(true);
            }
            if ((curreSelect + 1) == 2) {
                nextButton.setEnabled(false);
            }
            webServiceUI.refreshTableView();
            // for lazy load, the virtual table will load data after nextPress refresh tableViewe
            // should re-initial those links
            if (TableViewerCreator.getRecommandLazyLoad()) {
                webServiceUI.reInitLinks();
            }
        }
    }

    @Override
    protected void okPressed() {
        // if (getWebServiceUI().getIsFirst()) {
        // super.cancelPressed();
        // } else {
        getWebServiceUI().saveProperties();
        // getWebServiceUI().prepareClosing(SWT.OK);
        saveValue();
        getUIManager().setDialogResponse(SWT.OK);
        // }
    }

    private void saveValue() {
        List<InputMappingData> inputList = webServiceUI.getInputParams();
        List<OutPutMappingData> outputList = webServiceUI.getOutputParams();
        String currentURL = webServiceUI.getURL();
        List<PortNames> allPortNames = webServiceUI.getAllPortNames();
        Function function = webServiceUI.getCurrentFunction();
        PortNames currePortName = webServiceUI.getCurrentPortName();
        List<OutPutMappingData> outEleList = webServiceUI.getOutputElement();
        Set<String> insourceList = webServiceUI.getInSourceList();
        Set<String> outsourceList = webServiceUI.getOutSourceList();

        IMetadataTable inputMetaCopy = webServiceUI.getInputMetaCopy();
        IMetadataTable outputMetaCopy = webServiceUI.getOutputMetaCopy();
        IMetadataTable inputMetadata = webServiceUI.getInputMetadata();
        IMetadataTable outputMetadata = webServiceUI.getOutputMetadata();
        List<IMetadataColumn> inputmetadata = webServiceUI.getInputValue();
        WebServiceComponent wenCom = webServiceComponentMain.getWebServiceComponent();

        IMetadataTable wenMetadata = wenCom.getMetadataFromConnector("OUTPUT");
        if (wenMetadata != null && outputMetadata != wenMetadata) {
            outputMetadata = wenMetadata;
        }

        // save schema.
        if (outputMetaCopy != null) {
            if (!outputMetaCopy.sameMetadataAs(outputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                outputMetadata.getListColumns().clear();
                outputMetadata.getListColumns().addAll(outputMetaCopy.getListColumns());
            }
        }
        // if (inputMetadata != null) {
        // wenCom.setInputMetadata(inputMetaCopy);
        // if (!inputMetaCopy.sameMetadataAs(inputMetadata, IMetadataColumn.OPTIONS_NONE)) {
        // inputMetadata.getListColumns().clear();
        // inputMetadata.getListColumns().addAll(inputMetaCopy.getListColumns());
        // }
        // }

        if (!"".equals(currentURL) && currentURL != null) {
            IElementParameter ENDPOINTPara = wenCom.getElementParameter("ENDPOINT");
            ENDPOINTPara.setValue(currentURL);
        }

        if (currePortName != null) {
            IElementParameter Port_Name = wenCom.getElementParameter("PORT_NAME");
            Port_Name.setValue(currePortName.getPortName());
        } else if (currePortName == null && !allPortNames.isEmpty()) {
            currePortName = allPortNames.get(0);
            IElementParameter Port_Name = wenCom.getElementParameter("PORT_NAME");
            Port_Name.setValue(currePortName.getPortName());
        }

        if (function != null) {
            if (function.getName() != null) {
                IElementParameter METHODPara = wenCom.getElementParameter("METHOD");
                METHODPara.setValue(function.getName());
            }
            if (function.getServerNameSpace() != null) {
                IElementParameter Service_NS = wenCom.getElementParameter("SERVICE_NS");
                Service_NS.setValue(function.getServerNameSpace());
            }
            if (function.getServerName() != null) {
                IElementParameter Service_Name = wenCom.getElementParameter("SERVICE_NAME");
                Service_Name.setValue(function.getServerName());
            }
            if (function.getServerNameSpace() != null) {
                IElementParameter Port_NS = wenCom.getElementParameter("PORT_NS");
                Port_NS.setValue(function.getServerNameSpace());
            }
            // IElementParameter ADDRESSLocation = wenCom.getElementParameter("ADDRESS_LOCATION");
            // ADDRESSLocation.setValue(function.getAddressLocation());

        }

        // save input
        IElementParameter INPUT_PARAMSPara = wenCom.getElementParameter("INPUT_PARAMS");
        List<Map<String, String>> inputparaValue = (List<Map<String, String>>) INPUT_PARAMSPara.getValue();
        if (inputparaValue == null) {
            inputparaValue = new ArrayList<Map<String, String>>();
        } else {
            inputparaValue.clear();
        }

        for (InputMappingData inputData : inputList) {

            Map<String, String> inputMap = new HashMap<String, String>(2);
            if (inputData.getInputColumnValue() != null) {
                inputMap.put("EXPRESSION", inputData.getInputColumnValue());
            }

            if (inputData.getParameterName() != null) {
                String name = inputData.getParameterName();
                if (!name.equals("")) {
                    inputMap.put("ELEMENT", name);
                }
            } else if (inputData.getParameterName() == null && inputData.getParameter() != null) {
                if (inputData.getParameter().getParent() != null) {
                    String name = new ParameterInfoUtil().getParentName(inputData.getParameter());
                    inputMap.put("ELEMENT", name);
                } else {
                    inputMap.put("ELEMENT", inputData.getParameter().getName());
                }
                // inputMap.put("ELEMENT", inputData.getInputElementValue());
                // inputMap.put("NAMESPACE", inputData.getParameter().getNameSpace());
                // inputMap.put("TYPE", inputData.getParameter().getKind());
            }

            inputparaValue.add(inputMap);
        }
        for (IMetadataColumn column : inputmetadata) {
            Map<String, String> columnMap = new HashMap<String, String>(1);
            if (column.getLabel() != null) {
                columnMap.put("COLUMN", column.getLabel());
                inputparaValue.add(columnMap);
            }
        }
        for (String insource : insourceList) {
            if (insource == null || "".equals(insource)) {
                continue;
            }
            Map<String, String> sourceMap = new HashMap<String, String>(1);
            sourceMap.put("SOURCE", insource);
            inputparaValue.add(sourceMap);
        }
        List<ParameterInfo> listParamter = new ArrayList();
        if (function != null) {
            List inputParameter = function.getInputParameters();
            if (inputParameter != null) {
                // for (int i = 0; i < inputParameter.size(); i++) {
                boolean mark = true;
                goin: for (Iterator iterator2 = inputParameter.iterator(); iterator2.hasNext();) {
                    ParameterInfo element = (ParameterInfo) iterator2.next();
                    Map<String, String> sourceMap = new HashMap<String, String>(2);
                    sourceMap.put("PARAMETERINFO", element.getName());
                    if (element.getParent() == null) {
                        sourceMap.put("PARAPARENT", "");
                    } else {
                        sourceMap.put("PARAPARENT", element.getParent().getName());
                    }
                    inputparaValue.add(sourceMap);
                    mark = false;
                    if (!element.getParameterInfos().isEmpty()) {
                        listParamter.addAll(new ParameterInfoUtil().getAllChildren(element));
                    }
                    break goin;
                }
                if (!mark) {
                    for (ParameterInfo para : listParamter) {
                        Map<String, String> sourceMap = new HashMap<String, String>(2);
                        sourceMap.put("PARAMETERINFO", para.getName());
                        sourceMap.put("PARAPARENT", para.getParent().getName());
                        sourceMap.put("PARAMETERARRAYSIZE", String.valueOf(para.getArraySize()));
                        sourceMap.put("PARAMETERINDEX", para.getIndex());
                        inputparaValue.add(sourceMap);
                    }
                }
                // }
            }
        }
        // save output
        IElementParameter OUTPUT_PARAMSPara = wenCom.getElementParameter("OUTPUT_PARAMS");
        List<Map<String, String>> outputMap = (List<Map<String, String>>) OUTPUT_PARAMSPara.getValue();
        if (outputMap == null) {
            outputMap = new ArrayList<Map<String, String>>();
        } else {
            outputMap.clear();
        }
        for (OutPutMappingData outData : outEleList) {
            ParameterInfo para = outData.getParameter();
            if (para.getName() == null || "".equals(para.getName())) {
                continue;
            }

            Map<String, String> eleMap = new HashMap<String, String>(3);
            if (outData.getParameterName() != null) {
                eleMap.put("ELEMENT", outData.getParameterName());
            } else {
                eleMap.put("ELEMENT", para.getName());
            }
            // eleMap.put("NAMESPACE", para.getNameSpace());
            // eleMap.put("TYPE", para.getKind());
            outputMap.add(eleMap);
        }

        for (OutPutMappingData data : outputList) {

            Map<String, String> dataMap = new HashMap<String, String>(2);
            if (data.getParameterName() != null) {
                dataMap.put("EXPRESSION", data.getParameterName());
            } else if (data.getParameterName() == null) {
                // warningDialog("Please Select a Output Item.");
                // return;
            }

            if (data.getMetadataColumn() != null) {
                dataMap.put("COLUMN", data.getMetadataColumn().getLabel());
            }
            // else if (data.getMetadataColumn() == null) {
            // warningDialog("Please Select a Output Item.");
            // return;
            // }

            // Map<String, String> dataMap2 = new HashMap<String, String>(2);
            // if (data.getParameterList().size() > 0) {
            // for (ParameterInfo para : data.getParameterList()) {
            // dataMap2.put("", para.getNameSpace());
            // dataMap2.put("", para.getKind());
            // }
            // }
            //
            // if (data.getOutputColumnValue() != null) {
            // dataMap.put(data.getParameterName(), data.getOutputColumnValue());
            // }

            outputMap.add(dataMap);
            // outputMap.add(dataMap2);
        }

        for (String outsource : outsourceList) {
            if (outsource == null || "".equals(outsource)) {
                continue;
            }
            Map<String, String> sourceMap = new HashMap<String, String>(1);
            sourceMap.put("SOURCE", outsource);
            outputMap.add(sourceMap);
        }
        List<ParameterInfo> ls = new ArrayList();
        if (function != null) {
            List inputParameter = function.getOutputParameters();
            if (inputParameter != null) {
                // for (int i = 0; i < inputParameter.size(); i++) {
                boolean mark = true;
                goin: for (Iterator iterator2 = inputParameter.iterator(); iterator2.hasNext();) {
                    ParameterInfo element = (ParameterInfo) iterator2.next();
                    Map<String, String> sourceMap = new HashMap<String, String>(1);
                    sourceMap.put("PARAMETERINFO", element.getName());
                    if (element.getParent() == null) {
                        sourceMap.put("PARAPARENT", "");
                    } else {
                        sourceMap.put("PARAPARENT", element.getParent().getName());
                    }
                    outputMap.add(sourceMap);
                    mark = false;
                    if (!element.getParameterInfos().isEmpty()) {
                        ls.addAll(new ParameterInfoUtil().getAllChildren(element));
                    }
                    break goin;
                }
                if (!mark) {
                    for (ParameterInfo para : ls) {
                        Map<String, String> sourceMap = new HashMap<String, String>(2);
                        sourceMap.put("PARAMETERINFO", para.getName());
                        sourceMap.put("PARAPARENT", para.getParent().getName());
                        sourceMap.put("PARAMETERARRAYSIZE", String.valueOf(para.getArraySize()));
                        sourceMap.put("PARAMETERINDEX", para.getIndex());
                        outputMap.add(sourceMap);
                    }
                }
                // }
            }
        }
        super.okPressed();

    }

    private void warningDialog(String title) {
        MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
        box.setText("WARNING"); //$NON-NLS-1$
        box.setMessage(title); 
        box.open();
    }

    @Override
    public void checkPerformed(boolean enable) {
        final Button okBtn = getButton(IDialogConstants.OK_ID);
        if (okBtn != null) {
            okBtn.setEnabled(enable);
        }

    }

    public Button getBackButton() {
        return this.backButton;
    }

    public Button getNextButton() {
        return this.nextButton;
    }

    public void setBackButtonUnuse() {
        backButton.setEnabled(false);
    }

    public void setNextButtonUnuse() {
        nextButton.setEnabled(false);
    }

    public void setBackButtonCanuse() {
        backButton.setEnabled(true);
    }

    public void setNextButtonCanuse() {
        nextButton.setEnabled(true);
    }

}
