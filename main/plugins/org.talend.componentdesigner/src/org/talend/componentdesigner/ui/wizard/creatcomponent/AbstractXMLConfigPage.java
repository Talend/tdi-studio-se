// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.wizard.creatcomponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.ui.composite.TreeNodeAttrCompsite;
import org.talend.componentdesigner.ui.composite.xmltree.ATreeNodeUtil;
import org.talend.componentdesigner.ui.composite.xmltree.TreeNodeData;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.w3c.dom.Node;

/**
 * DOC rli class global comment. Detailled comment
 */
public abstract class AbstractXMLConfigPage extends AbstractComponentPage {

    protected static final String IMPORTS = "IMPORTS"; //$NON-NLS-1$

    protected static final String IMPORT = "IMPORT"; //$NON-NLS-1$

    protected static final String REQUIRED = "REQUIRED"; //$NON-NLS-1$

    protected static final String MODULE = "MODULE"; //$NON-NLS-1$

    protected static final String NAME = "NAME"; //$NON-NLS-1$

    protected static final String CODEGENERATION = "CODEGENERATION"; //$NON-NLS-1$

    protected static final String IMPORTITEMPATH = "COMPONENT/CODEGENERATION/IMPORTS/IMPORT"; //$NON-NLS-1$

    protected static final String IMPORTSITEMPATH = "COMPONENT/CODEGENERATION/IMPORTS"; //$NON-NLS-1$

    protected transient Tree availableXmlTree;

    // protected TreePopulator treePopulator;

    protected Composite rightComposite;

    protected TreeNodeAttrCompsite nodeAttrCompsite;

    protected Map<String, String[]> attrsAvailableValuesMap = null;

    protected Map<String, String> attrsToolTipsMap = null;

    private ScrolledComposite scrolledComposite;

    /**
     * DOC rli WizardXMLConfigPage constructor comment.
     * 
     * @param pageName
     * @param componentPref
     */
    public AbstractXMLConfigPage(String pageName, ComponentPref componentPref) {
        super(pageName, componentPref);
        loadAttrsInfo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.componentdesigner.ui.wizard.creatcomponent.AbstractComponentPage#createPageContent(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected void createPageContent(Composite parent) {
        Composite topComposite = new Composite(parent, SWT.NONE);
        topComposite.setLayout(new GridLayout(2, false));
        GridData data = new GridData(GridData.FILL_BOTH);
        topComposite.setLayoutData(data);
        // topComposite.setLayout(new FillLayout());
        availableXmlTree = new Tree(topComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData treeGD = new GridData(GridData.FILL_BOTH);
        treeGD.widthHint = 196;
        treeGD.grabExcessHorizontalSpace = true;
        treeGD.grabExcessVerticalSpace = true;
        availableXmlTree.setLayoutData(treeGD);

        scrolledComposite = new ScrolledComposite(topComposite, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new FillLayout());
        // rightComposite = new Composite(topComposite, SWT.NONE);
        rightComposite = new Composite(scrolledComposite, SWT.NONE);
        GridData rightPanelGd = new GridData(GridData.FILL_BOTH);
        rightPanelGd.widthHint = 180;
        rightComposite.setLayoutData(rightPanelGd);
        rightComposite.setLayout(new GridLayout());

        Label label = new Label(rightComposite, SWT.NONE);
        label.setText(Messages.getString("AbstractXMLConfigPage.SetProperties")); //$NON-NLS-1$
        GridData labelGd = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(labelGd);

        scrolledComposite.setContent(rightComposite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        this.setControl(topComposite);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.ui.wizard.creatcomponent.AbstractComponentPage#initialize()
     */
    protected void initialize() {
        availableXmlTree.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (availableXmlTree.getMenu() != null) {
                    availableXmlTree.getMenu().dispose();
                }

                final TreeItem[] treeItem = ((Tree) event.getSource()).getSelection();

                Menu popMenu = new Menu(availableXmlTree);
                MenuItem itemNew = new MenuItem(popMenu, SWT.CASCADE);
                itemNew.setText(Messages.getString("AbstractXMLConfigPage.New")); //$NON-NLS-1$
                MenuItem itemDel = new MenuItem(popMenu, SWT.CASCADE);
                itemDel.setText(Messages.getString("AbstractXMLConfigPage.Delete")); //$NON-NLS-1$
                itemDel.addSelectionListener(new SelectionAdapter() {

                    public void widgetSelected(SelectionEvent e) {
                        TreeNodeData currentNodeData = (TreeNodeData) treeItem[0].getParentItem().getData();
                        currentNodeData.getXMLNode().removeChild(((TreeNodeData) treeItem[0].getData()).getXMLNode());
                        treeItem[0].dispose();
                    }
                });

                final TreeNodeData treeNodeData = ((TreeNodeData) treeItem[0].getData());
                if (treeNodeData.isHasChildTreeNode()) {
                    Menu subNewMenu = new Menu(itemNew);
                    itemNew.setMenu(subNewMenu);

                    Object[] subNodeNames = treeNodeData.getChildNodeNames();
                    for (Object nodeName : subNodeNames) {
                        final MenuItem nodeMenuItem = new MenuItem(subNewMenu, SWT.CASCADE);
                        final String itemName = (String) nodeName;
                        nodeMenuItem.setText(itemName);
                        nodeMenuItem.addSelectionListener(new SelectionAdapter() {

                            public void widgetSelected(SelectionEvent e) {
                                TreeItem newItem = new TreeItem(treeItem[0], 0);
                                newItem.setText(itemName);
                                TreeNodeData newNodeData = null;
                                newNodeData = new TreeNodeData();
                                Node parentNode = treeNodeData.getXMLNode();
                                newNodeData.setXMLNode(parentNode.appendChild(parentNode.getOwnerDocument().createElement(
                                        itemName)));
                                newNodeData.setTreePath(treeNodeData.getTreePath() + "/" + itemName); //$NON-NLS-1$
                                newNodeData.setTreeNode(ATreeNodeUtil.getTreeNodeByPath(newNodeData.getTreePath()));
                                newItem.setData(newNodeData);
                            }
                        });
                    }
                } else {
                    itemNew.setEnabled(false);
                }

                availableXmlTree.setMenu(popMenu);

                if (nodeAttrCompsite != null) {
                    nodeAttrCompsite.dispose();
                }
                rebuildAttrComposite(treeNodeData);
                setComponentMinSize(treeNodeData.getTreeNode().getLabel());
                rightComposite.layout();
            }

        });

    }

    private void setComponentMinSize(String label) {
        if (("HEADER").equals(label)) {
            scrolledComposite.setMinSize(0, 0);
            scrolledComposite.setMinSize(0, 750);
        } else if (("CONNECTOR").equals(label)) {
            scrolledComposite.setMinSize(0, 0);
            scrolledComposite.setMinSize(0, 500);
        } else if (("PARAMETER").equals(label)) {
            scrolledComposite.setMinSize(0, 0);
            scrolledComposite.setMinSize(0, 630);
        } else if (("RETURN").equals(label)) {
            scrolledComposite.setMinSize(0, 0);
            scrolledComposite.setMinSize(0, 150);
        } else {
            scrolledComposite.setMinSize(0, 0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.ui.wizard.creatcomponent.AbstractComponentPage#validatePage()
     */
    protected boolean validatePage() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (componentPref.getLibEntries() != null && componentPref.getLibEntries().length != 0) {
            fillImportItems();
        }
        super.setVisible(visible);
    }

    protected abstract void fillImportItems();

    private void rebuildAttrComposite(TreeNodeData nodeData) {
        nodeAttrCompsite = new TreeNodeAttrCompsite(rightComposite, SWT.NONE, nodeData);
        nodeAttrCompsite.setLayoutData(new GridData(GridData.FILL_BOTH));
        nodeAttrCompsite.creatPart(attrsAvailableValuesMap, attrsToolTipsMap);
    }

    protected void loadAttrsInfo() {

        attrsAvailableValuesMap = new HashMap<String, String[]>();

        String[] valuesArray = null;

        // CTYPE
        EConnectionType[] eConnectionTypes = EConnectionType.values();
        valuesArray = new String[eConnectionTypes.length];
        for (int i = 0; i < eConnectionTypes.length; i++) {
            valuesArray[i] = eConnectionTypes[i].getName();
        }
        attrsAvailableValuesMap.put("CTYPE", valuesArray); //$NON-NLS-1$

        // FIELD
        EParameterFieldType[] eParameterFieldType = EParameterFieldType.values();
        valuesArray = new String[eParameterFieldType.length];
        for (int i = 0; i < eParameterFieldType.length; i++) {
            valuesArray[i] = eParameterFieldType[i].getName();
        }
        attrsAvailableValuesMap.put("FIELD", valuesArray); //$NON-NLS-1$

        // PLATFORM
        attrsAvailableValuesMap.put("PLATEFORM", new String[] { "ALL", "WINDOWS", "LINUX" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        // COMPATIBILITY
        attrsAvailableValuesMap.put("COMPATIBILITY", new String[] { "ALL", "WINDOWS", "LINUX" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        // STATUS
        attrsAvailableValuesMap.put("STATUS", new String[] { "ALPHA", "BETA", "RELEASE" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        // LINE_STYLE
        attrsAvailableValuesMap.put("LINE_STYLE", new String[] { "1", "2", "3", "4" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        // NB_LINES
        attrsAvailableValuesMap.put("NB_LINES", new String[] { "1", "2", "3", "4", "5" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

        // MAX_INPUT MIN_INPUT MAX_OUTPUT MIN_OUTPUT
        valuesArray = new String[] { "0", "1", "2", "3", "4", "5" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        attrsAvailableValuesMap.put("MAX_INPUT", valuesArray); //$NON-NLS-1$
        attrsAvailableValuesMap.put("MIN_INPUT", valuesArray); //$NON-NLS-1$
        attrsAvailableValuesMap.put("MAX_OUTPUT", valuesArray); //$NON-NLS-1$
        attrsAvailableValuesMap.put("MIN_OUTPUT", valuesArray); //$NON-NLS-1$

        // PROPERTY_TYPE
        List<String> valuesList = new ArrayList<String>();
        valuesList.add("DELIMITED"); //$NON-NLS-1$
        valuesList.add("POSITIONAL"); //$NON-NLS-1$
        valuesList.add("REGEX"); //$NON-NLS-1$
        valuesList.add("XML"); //$NON-NLS-1$
        valuesList.add("LDAP"); //$NON-NLS-1$
        valuesList.add("WSDL"); //$NON-NLS-1$
        valuesList.add("GENERIC"); //$NON-NLS-1$
        valuesList.add("DATABASE"); //$NON-NLS-1$
        for (EDatabaseTypeName eDatabaseTypeName : EDatabaseTypeName.values()) {
            valuesList.add("DATABASE:" + eDatabaseTypeName.getProduct()); //$NON-NLS-1$
        }
        attrsAvailableValuesMap.put("REPOSITORY_VALUE", valuesList.toArray(new String[0])); //$NON-NLS-1$

        // attributes with boolean value
        String[] booleanAttrNameArray = { "SHOW", "REQUIRED", "READONLY", "BASED_ON_SCHEMA", "KEY", "NULLABLE", "CUSTOM", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                "BUILTIN", "STARTABLE", "SCHEMA_AUTO_PROPAGATE", "DATA_AUTO_PROPAGATE", "HAS_CONDITIONAL_OUTPUTS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "IS_MULTIPLYING_OUTPUTS", "VISIBLE", "TSTATCATCHER_STATS", "USE_MERGE" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String[] booleanArray = { "true", "false" }; //$NON-NLS-1$ //$NON-NLS-2$
        for (String attrName : booleanAttrNameArray) {
            attrsAvailableValuesMap.put(attrName, booleanArray);
        }

        // init maps for TooTipsText
        attrsToolTipsMap = new HashMap<String, String>();
        attrsToolTipsMap.put("MAX_INPUT", Messages.getString("AbstractXMLConfigPage.MAXINPUTTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("MIN_INPUT", Messages.getString("AbstractXMLConfigPage.MININPUTTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("MAX_OUTPUT", Messages.getString("AbstractXMLConfigPage.MAXOUTPUTTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("MIN_OUTPUT", Messages.getString("AbstractXMLConfigPage.MINOUTPUTTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("LINE_STYLE", //$NON-NLS-1$
                Messages.getString("AbstractXMLConfigPage.LINESTYLETip")); //$NON-NLS-1$
        attrsToolTipsMap.put("LENGTH", Messages.getString("AbstractXMLConfigPage.IntValue")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("PRECISION", Messages.getString("AbstractXMLConfigPage.IntValue")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("NB_LINES", Messages.getString("AbstractXMLConfigPage.NBLINESTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("NUM_ROW", Messages.getString("AbstractXMLConfigPage.NUMROWTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("VERSION", Messages.getString("AbstractXMLConfigPage.VERSIONTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("REPOSITORY_VALUE", Messages.getString("AbstractXMLConfigPage.REPOSITORYVALUETip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("EXTENSION", Messages.getString("AbstractXMLConfigPage.EXTENSIONTip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("CTYPE", Messages.getString("AbstractXMLConfigPage.CTYPETip")); //$NON-NLS-1$ //$NON-NLS-2$
        attrsToolTipsMap.put("TYPE", Messages.getString("AbstractXMLConfigPage.TYPETip")); //$NON-NLS-1$ //$NON-NLS-2$

        attrsToolTipsMap.put("COLOR", Messages.getString("AbstractXMLConfigPage.COLORTip")); //$NON-NLS-1$ //$NON-NLS-2$

        for (String attrName : booleanAttrNameArray) {
            attrsToolTipsMap.put(attrName, Messages.getString("AbstractXMLConfigPage.BooleanTip")); //$NON-NLS-1$
        }

    }

}
