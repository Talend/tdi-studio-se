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
package org.talend.designer.webservice.ui.dialog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroupBase;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.talend.core.model.general.Project;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.webservice.i18n.Messages;
import org.talend.designer.webservice.ui.tree.WebServiceTreeContentProvider;
import org.talend.designer.webservice.ui.tree.WebServiceTreeLabelProvider;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;
import org.talend.designer.webservice.ws.wsdlinfo.ServiceInfo;
import org.talend.designer.webservice.ws.wsdlutil.ComponentBuilder;
import org.talend.designer.webservice.ws.wsdlutil.ServiceHelperConfiguration;
import org.talend.designer.webservice.ws.wsdlutil.WsdlTypeUtil;
import org.talend.repository.ProjectManager;

public class AllTypeDialog extends Dialog {

    private Shell parentShell;

    private List list;

    private ParameterInfo info;

    private String URLValue;

    private ServiceHelperConfiguration serverConfig;

    private java.util.List<XmlSchemaType> allXmlSchemaType = new ArrayList<XmlSchemaType>();

    private java.util.List<XmlSchemaElement> allXmlSchemaElement = new ArrayList<XmlSchemaElement>();

    private String[] allXMLSchemaTypeName;

    private String[] allXMLSimpleTypeName;

    // = new String[] { "simpletype:integer", "simpletype:int", "simpletype:long",
    // "simpletype:short", "simpletype:decimal", "simpletype:float", "simpletype:double", "simpletype:boolean",
    // "simpletype:byte", "simpletype:QName", "simpletype:dateTime", "simpletype:base64Binary", "simpletype:hexBinary",
    // "simpletype:unsignedInt", "simpletype:unsignedShort", "simpletype:unsignedByte", "simpletype:time",
    // "simpletype:date", "simpletype:gYear", "simpletype:gYearMonth", "simpletype:gMonth", "simpletype:gMonthDay",
    // "simpletype:gDay", "simpletype:gDay", "simpletype:duration", "simpletype:NOTATION", "simpletype:anyURI",
    // "simpletype:token", "simpletype:IDREF", "simpletype:NCName", "simpletype:ENTITY", "simpletype:normalizedString",
    // "simpletype:language", "simpletype:Name", "simpletype:NMTOKEN", "simpletype:NMTOKENS", "simpletype:ID",
    // "simpletype:IDREFS", "simpletype:ENTITIES", "simpletype:nonPositiveInteger", "simpletype:nonNegativeInteger",
    // "simpletype:negativeInteger", "simpletype:positiveInteger", "simpletype:unsignedLong" };

    private TreeViewer treeViewer = null;

    private Tree tree = null;

    private static final String COMPLEXTYPE = "complextype:";

    private static final String NAME = "NAME"; //$NON-NLS-1$

    private static final String INDEX = "INDEX"; //$NON-NLS-1$

    private ParameterInfo selectedParaInfo;

    private String selectedName;

    private Map<String, String> labelAndNameSpaceMap = new HashMap<String, String>();

    /**
     * Getter for labelAndNameSpaceMap.
     *
     * @return the labelAndNameSpaceMap
     */
    public Map<String, String> getLabelAndNameSpaceMap() {
        return this.labelAndNameSpaceMap;
    }

    protected AllTypeDialog(Shell parentShell, ParameterInfo info, String URLValue, ServiceHelperConfiguration serverConfig)
            throws Exception {
        super(parentShell);
        this.parentShell = parentShell;
        this.URLValue = URLValue;
        this.serverConfig = serverConfig;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        init();
    }

    private void init() throws Exception {
        labelAndNameSpaceMap.clear();
        URLValue = TalendTextUtils.removeQuotes(URLValue);
        ComponentBuilder builder = new ComponentBuilder();

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setWsdlUri(URLValue);
        serviceInfo.setAuthConfig(serverConfig);
        serviceInfo = builder.buildserviceinformation(serviceInfo);
        allXmlSchemaType = builder.getAllXmlSchemaType();
        allXmlSchemaElement = builder.getAllXmlSchemaElement();
        initSimpleType();
        initComplexType();
        allXMLSchemaTypeName = new String[allXmlSchemaType.size() + allXMLSimpleTypeName.length];
        for (int i = 0; i < allXMLSimpleTypeName.length; i++) {
            allXMLSchemaTypeName[i] = allXMLSimpleTypeName[i];
        }
        for (int i = allXMLSimpleTypeName.length; i < allXmlSchemaType.size() + allXMLSimpleTypeName.length; i++) {
            allXMLSchemaTypeName[i] = COMPLEXTYPE + allXmlSchemaType.get(i - allXMLSimpleTypeName.length).getName();
        }
    }

    private void initComplexType() {
        for (XmlSchemaType xmlSchemaType : allXmlSchemaType) {
            if (xmlSchemaType instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType complexType = (XmlSchemaComplexType) xmlSchemaType;
                String label = COMPLEXTYPE + complexType.getName();
                labelAndNameSpaceMap.put(label, complexType.getQName().toString());
            }
        }
    }

    private void initSimpleType() throws WSDLException, URISyntaxException {
        String url = URLValue;
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader newWSDLReader = wsdlFactory.newWSDLReader();
        newWSDLReader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        URI wsdlURI = new URI(url);

        Definition definition = newWSDLReader.readWSDL(url);

        java.util.List<ExtensibilityElement> extensibilityElements = definition.getTypes().getExtensibilityElements();
        String tmpTNName = "";
        int tmpCount = 0;
        for (ExtensibilityElement el : extensibilityElements) {
            if (el instanceof Schema) {
                Schema schema = (Schema) el;
                // for bug 8674
                // set base uri for relative path in schemaLocation.
                schemaCollection.setBaseUri(schema.getDocumentBaseURI());

                // synthetic URI for the schemas without targetNamespace,avoid conflict error.

                if (schema.getElement().getAttributeNode("targetNamespace") == null) {
                    tmpTNName = schema.getDocumentBaseURI() + "#type" + tmpCount;
                    schemaCollection.read(schema.getElement(), tmpTNName);
                    tmpCount++;
                } else {
                    schemaCollection.read(schema.getElement());
                }
            }
        }

        Map namespaces = definition.getNamespaces();
        // System.out.println(namespaces);

        XmlSchema[] schemas = schemaCollection.getXmlSchemas();
        java.util.List<String> labelList = new ArrayList<String>();
        for (int i = 0; i < schemas.length; i++) {
            XmlSchema schema = schemas[i];
            XmlSchemaObjectTable types = schema.getSchemaTypes();
            Iterator it = types.getValues();
            while (it.hasNext()) {
                XmlSchemaType type = (XmlSchemaType) it.next();
                if (type instanceof XmlSchemaSimpleType) {
                    XmlSchemaSimpleType t = (XmlSchemaSimpleType) type;
                    String label = "simpletype:" + t.getName();
                    if (!labelList.contains(label)) {
                        labelList.add(label);
                        labelAndNameSpaceMap.put(label, t.getQName().toString());
                    }
                }
            }
        }
        allXMLSimpleTypeName = new String[labelList.size()];
        for (int i = 0; i < labelList.size(); i++) {
            allXMLSimpleTypeName[i] = labelList.get(i);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite createDialogArea = (Composite) super.createDialogArea(parent);
        createDialogArea.setLayout(new FillLayout());
        SashForm baseCom = new SashForm(createDialogArea, SWT.VERTICAL | SWT.BORDER);
        baseCom.setLayout(new GridLayout(1, true));
        Group group1 = new Group(baseCom, SWT.None);
        group1.setText(Messages.getString("AllTypeDialog.Group1Text"));
        group1.setLayoutData(new GridData(GridData.FILL_BOTH));
        group1.setLayout(new FillLayout());
        Font font = new Font(Display.getCurrent(), "Arial", 43, 3);
        group1.setFont(font);
        font.dispose();
        Group group2 = new Group(baseCom, SWT.None);
        group2.setText(Messages.getString("AllTypeDialog.Group2Text"));
        group2.setLayoutData(new GridData(GridData.FILL_BOTH));
        font = new Font(Display.getCurrent(), "Arial", 43, 3);
        group2.setFont(font);
        group2.setLayout(new FillLayout());
        font.dispose();

        list = new List(group1, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        list.setItems(allXMLSchemaTypeName);

        treeViewer = new TreeViewer(group2, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        tree = treeViewer.getTree();
        tree.setHeaderVisible(false);
        tree.setLinesVisible(true);
        treeViewer.setContentProvider(new WebServiceTreeContentProvider());
        treeViewer.setLabelProvider(new AllTypeLabelProvider());
        treeViewer.setInput(null);
        treeViewer.refresh();

        list.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String selection = list.getSelection()[0];
                if (selection.contains(COMPLEXTYPE)) {
                    String[] split = selection.split(":");
                    String complexTypeName = split[1];
                    for (XmlSchemaType xmlSchemaType : allXmlSchemaType) {
                        if (xmlSchemaType.getName().equals(complexTypeName)) {
                            ParameterInfo parameterRoot = new ParameterInfo();
                            parameterRoot.setName("parameters");
                            ParameterInfo parameterSon = new ParameterInfo();
                            parameterSon.setName(complexTypeName);
                            parameterSon.setParent(parameterRoot);
                            parameterRoot.getParameterInfos().add(parameterSon);
                            XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaType;
                            XmlSchemaParticle xmlSchemaParticle = xmlElementComplexType.getParticle();
                            if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                                XmlSchemaObjectCollection childCollection = xmlSchemaGroupBase.getItems();
                                if (childCollection != null) {
                                    buildParameterFromCollection(childCollection, parameterSon);
                                }
                            }
                            treeViewer.setInput(parameterRoot);
                            treeViewer.refresh();
                            selectedParaInfo = parameterSon;
                        }
                    }
                } else {
                    treeViewer.setInput(null);
                    treeViewer.refresh();
                    selectedParaInfo = null;
                }
            }

        });

        return baseCom;
    }

    private class AllTypeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            ParameterInfo info = (ParameterInfo) element;
            return info.getName();
        }

    }

    private void buildParameterFromCollection(XmlSchemaObjectCollection xmlSchemaObjectCollection, ParameterInfo parameter) {

        // XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) xmlSchemaParticle;
        // XmlSchemaObjectCollection xmlSchemaObjectCollection = xmlSchemaSequence.getItems();
        int count = xmlSchemaObjectCollection.getCount();
        for (int j = 0; j < count; j++) {
            XmlSchemaObject xmlSchemaObject = xmlSchemaObjectCollection.getItem(j);
            if (xmlSchemaObject instanceof XmlSchemaGroupBase) {
                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaObject;
                XmlSchemaObjectCollection items = xmlSchemaGroupBase.getItems();
                if (items != null) {
                    buildParameterFromCollection(items, parameter);
                }
            } else if (xmlSchemaObject instanceof XmlSchemaAny) {
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName("_content_");
                parameterSon.setParent(parameter);
                parameter.getParameterInfos().add(parameterSon);

            } else if (xmlSchemaObject instanceof XmlSchemaElement) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) xmlSchemaObject;
                String elementName = xmlSchemaElement.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName(elementName);
                parameterSon.setParent(parameter);
                Long min = xmlSchemaElement.getMinOccurs();
                Long max = xmlSchemaElement.getMaxOccurs();
                if (max - min > 1) {
                    parameterSon.setArraySize(-1);
                    parameterSon.setIndex("*");
                }
                parameter.getParameterInfos().add(parameterSon);
                if (xmlSchemaElement.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                    if (elementTypeName != null && elementTypeName.equals("anyType")) {
                        parameterSon.setName(xmlSchemaElement.getName() + ":anyType");
                    }
                    parameterSon.setType(elementTypeName);
                    if (!WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes(elementTypeName, parameterSon);
                    }

                } else if (xmlSchemaElement.getSchemaType() != null) {
                    if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                        XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                        XmlSchemaParticle xmlSchemaParticle = xmlElementComplexType.getParticle();
                        if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                            XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                            XmlSchemaObjectCollection childCollection = xmlSchemaGroupBase.getItems();
                            if (childCollection != null) {
                                buildParameterFromCollection(childCollection, parameterSon);
                            }
                        } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                            String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                            if (paraTypeName != null) {
                                parameter.setType(paraTypeName);
                                buileParameterFromTypes(paraTypeName, parameterSon);
                            }
                        }
                    } else if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaSimpleType) {
                        XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaElement.getSchemaType();
                        String typeName = xmlSchemaSimpleType.getName();
                        if (typeName != null && typeName.equals("anyType")) {
                            ParameterInfo pSon = new ParameterInfo();
                            pSon.setName(xmlSchemaElement.getName() + "(anyType)");
                            pSon.setParent(parameter);
                            parameter.getParameterInfos().add(pSon);
                        }
                        parameter.setType(typeName);

                    }

                } else if (xmlSchemaElement.getRefName() != null) {
                    String elementTypeName = xmlSchemaElement.getRefName().getLocalPart();
                    if (!WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buildParameterFromElements(elementTypeName, parameterSon);
                    }
                }

            } else if (xmlSchemaObject instanceof XmlSchemaAttribute) {
                XmlSchemaAttribute xmlSchemaAttribute = (XmlSchemaAttribute) xmlSchemaObject;
                String elementName = xmlSchemaAttribute.getName();
                ParameterInfo parameterSon = new ParameterInfo();
                parameterSon.setName(elementName);
                parameterSon.setParent(parameter);

                parameter.getParameterInfos().add(parameterSon);
                if (xmlSchemaAttribute.getSchemaTypeName() != null) {
                    String elementTypeName = xmlSchemaAttribute.getSchemaTypeName().getLocalPart();
                    parameterSon.setType(elementTypeName);
                    if (!WsdlTypeUtil.isJavaBasicType(elementTypeName)) {
                        buileParameterFromTypes(elementTypeName, parameterSon);
                    }
                } else if (xmlSchemaAttribute.getRefName() != null) {
                    String refName = xmlSchemaAttribute.getRefName().getLocalPart();
                    parameterSon.setType(refName);
                }
            }
        }
    }

    private void buildParameterFromElements(String partElement, ParameterInfo parameterRoot) {
        // XmlSchemaObjectTable elements = xmlSchema.getElements();
        Iterator elementsItr = allXmlSchemaElement.iterator();
        if (partElement != null) {
            while (elementsItr.hasNext()) {
                XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) elementsItr.next();
                if (partElement.equals(xmlSchemaElement.getName())) {
                    // ParameterInfo parameter = new ParameterInfo();
                    // parameter.setName(partName);
                    if (xmlSchemaElement.getSchemaType() != null) {
                        if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaComplexType) {
                            XmlSchemaComplexType xmlElementComplexType = (XmlSchemaComplexType) xmlSchemaElement.getSchemaType();
                            XmlSchemaParticle xmlSchemaParticle = xmlElementComplexType.getParticle();
                            if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                                XmlSchemaObjectCollection xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                                if (xmlSchemaObjectCollection != null) {
                                    buildParameterFromCollection(xmlSchemaObjectCollection, parameterRoot);
                                }
                            } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                                String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                                if (paraTypeName != null) {
                                    parameterRoot.setType(paraTypeName);
                                    buileParameterFromTypes(paraTypeName, parameterRoot);
                                }
                            }
                        } else if (xmlSchemaElement.getSchemaType() instanceof XmlSchemaSimpleType) {
                            XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaElement.getSchemaType();
                            String typeName = xmlSchemaSimpleType.getName();
                            if (typeName != null && typeName.equals("anyType")) {
                                ParameterInfo parameterSon = new ParameterInfo();
                                parameterSon.setName(xmlSchemaElement.getName() + "(anyType)");
                                parameterSon.setParent(parameterRoot);
                                parameterRoot.getParameterInfos().add(parameterSon);
                            }
                            parameterRoot.setType(typeName);
                        }

                    } else if (xmlSchemaElement.getSchemaTypeName() != null) {
                        String paraTypeName = xmlSchemaElement.getSchemaTypeName().getLocalPart();
                        if (paraTypeName != null) {
                            parameterRoot.setType(paraTypeName);
                            buileParameterFromTypes(paraTypeName, parameterRoot);
                        }
                    }
                }
            }
        }

    }

    private void buileParameterFromTypes(String paraType, ParameterInfo parameter) {
        for (int i = 0; i < allXmlSchemaType.size(); i++) {
            XmlSchemaType type = allXmlSchemaType.get(i);
            String typeName = type.getName();
            if (paraType.equals(typeName)) {
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType xmlSchemaComplexType = (XmlSchemaComplexType) type;
                    XmlSchemaParticle xmlSchemaParticle = xmlSchemaComplexType.getParticle();
                    XmlSchemaObjectCollection xmlSchemaObjectCollection = null;
                    if (xmlSchemaParticle == null && xmlSchemaComplexType.getContentModel() != null) {
                        Object obj = xmlSchemaComplexType.getContentModel().getContent();
                        if (obj instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension xscce = (XmlSchemaComplexContentExtension) obj;
                            if (xscce.getBaseTypeName() != null) {
                                buileParameterFromTypes(xscce.getBaseTypeName().getLocalPart(), parameter);
                            }
                            if (xscce != null) {
                                xmlSchemaParticle = xscce.getParticle();
                            }
                            if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                                XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                                xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                            }

                        } else if (obj instanceof XmlSchemaComplexContentRestriction) {
                            XmlSchemaComplexContentRestriction xsccr = (XmlSchemaComplexContentRestriction) obj;
                            xmlSchemaObjectCollection = xsccr.getAttributes();
                        }

                    } else if (xmlSchemaParticle instanceof XmlSchemaGroupBase) {
                        XmlSchemaGroupBase xmlSchemaGroupBase = (XmlSchemaGroupBase) xmlSchemaParticle;
                        xmlSchemaObjectCollection = xmlSchemaGroupBase.getItems();
                    }
                    if (xmlSchemaObjectCollection != null && xmlSchemaObjectCollection.getCount() > 0) {
                        buildParameterFromCollection(xmlSchemaObjectCollection, parameter);
                    }
                } else if (type instanceof XmlSchemaSimpleType) {

                }
                break;
            }

        }
    }

    @Override
    protected void okPressed() {
        if (list.getSelection()[0] == null) {
            return;
        }
        selectedName = list.getSelection()[0];
        super.okPressed();
    }

    public ParameterInfo getSelectedParaInfo() {
        return selectedParaInfo;
    }

    public String getSelectedName() {
        return selectedName;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("AllTypeDialog.title"));
        newShell.setBounds(500, 300, 400, 450);
    }
}
