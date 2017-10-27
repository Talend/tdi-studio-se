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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.model.libentry.JarLibEntry;
import org.talend.componentdesigner.ui.composite.xmltree.ATreeNodeUtil;
import org.talend.componentdesigner.ui.composite.xmltree.TreeNodeData;
import org.talend.componentdesigner.ui.composite.xmltree.TreePopulator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DOC slanglois class global comment. Detailled comment
 */
public class PerlXMLConfigWizardPage extends AbstractXMLConfigPage {

    /**
     * DOC slanglois PerlXMLConfigWizardPage constructor comment.
     * 
     * @param pageName
     * @param componentPref
     */
    public PerlXMLConfigWizardPage(String pageName, ComponentPref componentPref) {
        super(pageName, componentPref);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.ui.wizard.creatcomponent.AbstractComponentPage#initialize()
     */
    protected void initialize() {
        super.initialize();

        String perlXMLFileName = componentPref.getPerlXMLFileName();
        if (perlXMLFileName == null) {

            URL url = null;
            try {
                url = FileLocator.toFileURL(ComponentDesigenerPlugin.getDefault().getBundle().getResource(
                        "/data/sampleperlcomponent.xml")); //$NON-NLS-1$
            } catch (IOException e) {
                // e.printStackTrace();
                org.talend.componentdesigner.exception.ExceptionHandler.process(e);
            }
            perlXMLFileName = url.getFile();
        }

        TreePopulator treePopulator = new TreePopulator(availableXmlTree, perlXMLFileName);
        treePopulator.populateTree();
        componentPref.setPerlXMLDocument(treePopulator.getReadDocument());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.ui.wizard.creatcomponent.AbstractXMLConfigPage#fillImportItems()
     */
    protected void fillImportItems() {
        for (int i = 0; i < availableXmlTree.getItems().length; i++) {
            TreeItem item = availableXmlTree.getItem(i);
            if (item.getText().equals(CODEGENERATION)) {
                Node codeGenNode = ((TreeNodeData) item.getData()).getXMLNode();

                TreeItem importsItem = new TreeItem(item, SWT.CASCADE);
                importsItem.setText(IMPORTS);
                Element importsEle = codeGenNode.getOwnerDocument().createElement(IMPORTS);
                codeGenNode.appendChild(importsEle);

                TreeNodeData importsData = new TreeNodeData();
                importsData.setXMLNode(importsEle);
                importsData.setTreeNode(ATreeNodeUtil.getTreeNodeByPath(IMPORTSITEMPATH));
                importsItem.setData(importsData);

                List<Node> jarImportNodes = new ArrayList<Node>();
                List<Node> pmImportNodes = new ArrayList<Node>();

                for (ILibEntry libEntry : componentPref.getLibEntries()) {
                    TreeItem importItem = new TreeItem(importsItem, SWT.CASCADE);
                    importItem.setText(IMPORT);
                    Element importEle = importsEle.getOwnerDocument().createElement(IMPORT);
                    importsEle.appendChild(importEle);
                    importEle.setAttribute(NAME, libEntry.getNamePrefix());
                    importEle.setAttribute(MODULE, libEntry.getName());
                    importEle.setAttribute(REQUIRED, "true"); //$NON-NLS-1$

                    TreeNodeData nodeData = new TreeNodeData();
                    nodeData.putAttrValue(NAME, libEntry.getNamePrefix());
                    nodeData.putAttrValue(MODULE, libEntry.getName());
                    nodeData.putAttrValue(REQUIRED, "true"); //$NON-NLS-1$
                    nodeData.setXMLNode(importEle);
                    nodeData.setTreeNode(ATreeNodeUtil.getTreeNodeByPath(IMPORTITEMPATH));
                    importItem.setData(nodeData);

                    if (libEntry instanceof JarLibEntry) {
                        jarImportNodes.add(importEle);
                    } else {
                        pmImportNodes.add(importEle);
                    }
                }

                this.componentPref.setPerlImportNodes(pmImportNodes.toArray(new Node[pmImportNodes.size()]));

            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.ui.wizard.creatcomponent.AbstractXMLConfigPage#loadAttrsInfo()
     */
    @Override
    protected void loadAttrsInfo() {
        super.loadAttrsInfo();

        // available TYPE values are different between LANGUAGES
        attrsAvailableValuesMap.put("TYPE", new String[] { "boolean", "date", "datetime", "int", "decimal", "string" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    }
}
