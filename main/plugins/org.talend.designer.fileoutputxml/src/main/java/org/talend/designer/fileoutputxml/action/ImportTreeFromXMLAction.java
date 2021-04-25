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
package org.talend.designer.fileoutputxml.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.xsd.XSDSchema;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.datatools.xml.utils.SchemaPopulationUtil;
import org.talend.datatools.xml.utils.XSDPopulationUtil2;
import org.talend.designer.fileoutputxml.i18n.Messages;
import org.talend.designer.fileoutputxml.ui.FOXUI;
import org.talend.metadata.managment.ui.dialog.RootNodeSelectDialog;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: ImportTreeFromXMLAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class ImportTreeFromXMLAction extends SelectionProviderAction {

    private static final String LINEFEED = "\n";//$NON-NLS-1$

    // the xml viewer, see FOXUI.
    private TreeViewer xmlViewer;

    private FOXUI foxui;

    /**
     * CreateNode constructor comment.
     *
     * @param provider
     * @param text
     */
    public ImportTreeFromXMLAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public ImportTreeFromXMLAction(TreeViewer xmlViewer, FOXUI foxui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.foxui = foxui;
    }

    private List<FOXTreeNode> treeNodeAdapt(String file) {
        List<FOXTreeNode> list = new ArrayList<FOXTreeNode>();
        try {
            ATreeNode treeNode = SchemaPopulationUtil.getSchemaTree(file, true, 0);
            String schemaName = getSelectedSchema();
            String rootName = "";
            if (treeNode.getValue() instanceof String) {
                rootName += "/" + treeNode.getValue();
            }
            FOXTreeNode root = cloneATreeNode(treeNode, schemaName, rootName);
            Element rootElement = (Element) root;
            if (rootElement.getElementChildren() != null && rootElement.getElementChildren().size() > 0) {
                for (FOXTreeNode foxTreeNode : rootElement.getElementChildren()) {
                    foxTreeNode.setParent(null);
                    list.add(foxTreeNode);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return list;
    }

    private String getFilePath() {
        FileDialog f = new FileDialog(xmlViewer.getControl().getShell());
        String file = f.open();
        return file;
    }

    private FOXTreeNode cloneATreeNode(ATreeNode treeNode, String schemaName, String currentPath) throws Exception {
        FOXTreeNode node = null;
        if (treeNode.getType() == ATreeNode.ATTRIBUTE_TYPE) {
            node = new Attribute();
        } else {
            node = new Element();
        }
        // zli fixed for bug 7414
        if (treeNode.getType() == ATreeNode.NAMESPACE_TYPE) {
            node = new NameSpaceNode();
            node.setLabel("");//$NON-NLS-1$
            node.setDefaultValue((String) treeNode.getValue());
        } else {
            node.setLabel((String) treeNode.getValue());
        }

        Object[] children = treeNode.getChildren();
        if (children != null) {
            for (Object element : children) {
                if (element instanceof ATreeNode) {
                    ATreeNode child = (ATreeNode) element;
                    String newPath = currentPath + "/";
                    if (child.getValue() instanceof String) {
                        String elementName = (String) child.getValue();
                        if (currentPath.contains("/" + elementName + "/")) {
                            ExceptionHandler.process(new Exception("XSD ERROR: loop found. Item: " + elementName
                                    + " is already in the currentPath (" + currentPath + ")."));
                            continue;
                        }
                        newPath += elementName;
                    } else {
                        newPath += "unknownElement";
                    }

                    FOXTreeNode foxChild = cloneATreeNode(child, schemaName, newPath);
                    foxChild.setRow(schemaName);
                    node.addChild(foxChild);
                }
            }
        }
        return node;
    }

    /**
     *
     * wzhang Comment method "getSelectedSchema".
     *
     * @return
     */
    private String getSelectedSchema() {
        TreeItem[] selection = xmlViewer.getTree().getSelection();
        if (selection.length > 0) {
            Object data = selection[0].getData();
            if (data instanceof FOXTreeNode) {
                return ((FOXTreeNode) data).getRow();
            }
        }
        return foxui.getFoxManager().getCurrentSchema();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        List<FOXTreeNode> newInput = new ArrayList<FOXTreeNode>();

        String filePath = getFilePath();
        if (filePath == null) {
            return;
        }

        boolean changed = true;
        try {
            if (XmlUtil.isXSDFile(filePath)) {
                XSDSchema xsdSchema = TreeUtil.getXSDSchema(filePath);

                // check if there have some (<xs:import>) import reference schema xsd file don't exist
                Set<String> notExistImportSchema = TreeUtil.getNotExistImportSchema(filePath, xsdSchema);
                if (!notExistImportSchema.isEmpty()) {
                    StringBuffer detail = new StringBuffer();
                    detail.append(Messages.getString("ImportTreeFromXMLAction.schemaFileNotExistDetailTitle")).append(LINEFEED);//$NON-NLS-1$
                    for (String xsdfilePath : notExistImportSchema) {
                        detail.append(xsdfilePath).append(LINEFEED);
                    }
                    new ErrorDialogWidthDetailArea(xmlViewer.getControl().getShell(), Messages.PLUGIN_ID,
                            Messages.getString("ImportTreeFromXMLAction.ImportSchemaNotExistError"), detail.toString());//$NON-NLS-1$
                }else {
                    List<ATreeNode> list = new XSDPopulationUtil2().getAllRootNodes(xsdSchema);
                    if (list.size() > 1) {
                        RootNodeSelectDialog dialog = new RootNodeSelectDialog(xmlViewer.getControl().getShell(), list);
                        if (dialog.open() == IDialogConstants.OK_ID) {
                            ATreeNode selectedNode = dialog.getSelectedNode();
                            newInput = TreeUtil.getFoxTreeNodesByRootNode(xsdSchema, selectedNode);
                            changed = true;
                        } else {
                            changed = false;
                        }
                    } else {
                        newInput = TreeUtil.getFoxTreeNodesByRootNode(xsdSchema, list.get(0));
                        changed = true;
                    }
                }
            } else {
                newInput = treeNodeAdapt(filePath);
                changed = true;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        if (newInput.size() == 0) {
            return;
        }

        if (changed) {
            // updateNode
            for (FOXTreeNode node : newInput) {
                foxui.updateNode(node, getSelectedSchema());
            }
            List<FOXTreeNode> treeData = foxui.getFoxManager().getTreeData(getSelectedSchema());
            treeData.clear();
            treeData.addAll(newInput);
            xmlViewer.setInput(foxui.getFoxManager().getTreeData());
            // TreeUtil.guessAndSetLoopNode((FOXTreeNode) xmlViewer.getTree().getItem(0).getData());
            xmlViewer.refresh();
            xmlViewer.expandToLevel(3);
            foxui.updateStatus();
            foxui.redrawLinkers();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.setEnabled(true);
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node != null) {
            foxui.setSelectedText(node.getLabel());
        }
    }
}
