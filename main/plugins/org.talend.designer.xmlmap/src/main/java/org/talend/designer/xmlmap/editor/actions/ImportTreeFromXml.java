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
package org.talend.designer.xmlmap.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.designer.xmlmap.XmlMapPlugin;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapConnectionBuilder;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class ImportTreeFromXml extends SelectionAction {

    private TreeNode parentNode;

    private MapperManager mapperManager;

    private Shell shell;

    private boolean input;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.ImportTreeFromXml";

    public ImportTreeFromXml(IWorkbenchPart part, Shell shell) {
        super(part);
        this.shell = shell;
        setId(ID);
        setText("Import From File");
    }

    @Override
    public void run() {
        List<FOXTreeNode> list = new ArrayList<FOXTreeNode>();
        FileDialog f = new FileDialog(shell);
        String file = f.open();
        if (file != null) {
            boolean clickOk = false;
            TreeNode treeNodeRoot = null;
            String detailedMessage = "";
            try {
                clickOk = TreeUtil.getFoxTreeNodesForXmlMap(file, shell, list);
                if (!clickOk) {
                    return;
                }
                treeNodeRoot = XmlMapUtil.getTreeNodeRoot(parentNode);
                XmlMapUtil.detachNodeConnections(treeNodeRoot, mapperManager.getExternalData(), true);
                parentNode.getChildren().clear();
                prepareEmfTreeNode(list, parentNode);
            } catch (Exception e) {
                ExceptionHandler.process(e);
                StringBuffer sb = new StringBuffer();
                sb.append(e.toString());
                sb.append("\n");
                if (e.getStackTrace() != null) {
                    for (StackTraceElement trace : e.getStackTrace()) {
                        sb.append(trace.toString());
                        sb.append("\n");
                    }
                }
                detailedMessage = sb.toString();

            }

            boolean childrenEmpty = false;
            if (parentNode.getChildren().isEmpty()) {
                childrenEmpty = true;
                TreeNode rootNode = null;
                if (input) {
                    rootNode = XmlmapFactory.eINSTANCE.createTreeNode();
                } else {
                    rootNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                }
                rootNode.setName("root");
                rootNode.setNodeType(NodeType.ELEMENT);
                rootNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                rootNode.setXpath(XmlMapUtil.getXPath(parentNode.getXpath(), "root", NodeType.ELEMENT));
                parentNode.getChildren().add(rootNode);
            }
            // set a default loop
            parentNode.getChildren().get(0).setLoop(true);
            parentNode.getChildren().get(0).setMain(true);
            if (childrenEmpty || (detailedMessage != null && !"".equals(detailedMessage))) {
                showError(detailedMessage);
            }

            if (parentNode.eContainer() instanceof InputXmlTree) {
                XmlMapConnectionBuilder connectionBuilder = new XmlMapConnectionBuilder();
                connectionBuilder.setCheckRootNodePrefix(true);
                connectionBuilder.rebuildLinks(parentNode, mapperManager.getExternalData());
                mapperManager.refreshTreeSchemaEditor((InputXmlTree) parentNode.eContainer());
                // mapperManager.inputTreeSchemaBeanListModified();
            } else if (parentNode.eContainer() instanceof OutputXmlTree) {
                mapperManager.refreshTreeSchemaEditor((OutputXmlTree) parentNode.eContainer());
                // mapperManager.outputTreeSchemaBeanListModified();
            }
            if (treeNodeRoot != null && treeNodeRoot.eContainer() instanceof AbstractInOutTree) {
                mapperManager.getProblemsAnalyser().checkProblems((AbstractInOutTree) treeNodeRoot.eContainer());
                mapperManager.getMapperUI().updateStatusBar();
            }

        }

    }

    private void prepareEmfTreeNode(List<FOXTreeNode> list, TreeNode parent) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String xPath = parent.getXpath();
        TreeNode realParentNode = parent;
        if (parent.isSubstitution() || parent.isChoice()) {
            realParentNode = XmlMapUtil.getRealParentNode(parent);
            if (realParentNode != null) {
                xPath = realParentNode.getXpath();
            }
        }
        for (FOXTreeNode foxNode : list) {
            TreeNode createTreeNode = null;
            if (input) {
                createTreeNode = XmlmapFactory.eINSTANCE.createTreeNode();
            } else {
                createTreeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
            }
            String label = foxNode.getLabel();
            createTreeNode.setName(label);
            createTreeNode.setOptional(foxNode.isOptional());
            if (foxNode instanceof Element) {
                createTreeNode.setNodeType(NodeType.ELEMENT);
                if (foxNode.isChoice()) {
                    createTreeNode.setChoice(foxNode.isChoice());
                } else if (foxNode.isSubstitution()) {
                    createTreeNode.setSubstitution(foxNode.isSubstitution());
                }
            } else if (foxNode instanceof Attribute) {
                createTreeNode.setNodeType(NodeType.ATTRIBUT);
            } else if (foxNode instanceof NameSpaceNode) {
                createTreeNode.setNodeType(NodeType.NAME_SPACE);
                createTreeNode.setDefaultValue(foxNode.getDefaultValue());
                if (createTreeNode.getName() == null || createTreeNode.getName().equals("")) {
                    createTreeNode.setName(XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX);
                }
            }
            createTreeNode.setXpath(XmlMapUtil.getXPath(xPath, label, createTreeNode.getNodeType()));
            if (foxNode.getDataType() != null && !"".equals(foxNode.getDataType())) {
                if (foxNode.getDataType().equals("id_Date")) {
                    createTreeNode.setPattern("\"dd-MM-yyyy\"");//$NON-NLS-1$
                }
                createTreeNode.setType(foxNode.getDataType());
            } else {
                createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
            }
            parent.getChildren().add(createTreeNode);
            if (foxNode.getChildren() != null && !foxNode.getChildren().isEmpty()) {
                prepareEmfTreeNode(foxNode.getChildren(), createTreeNode);
            }
        }

    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            // get the last selection to run the action
            Object s = getSelectedObjects().get(0);
            if (s instanceof List && !((List) s).isEmpty()) {
                List selectedarts = (List) s;
                Object object = selectedarts.get(selectedarts.size() - 1);
                if (object instanceof TreeNodeEditPart) {
                    TreeNodeEditPart parentPart = (TreeNodeEditPart) object;
                    parentNode = (TreeNode) parentPart.getModel();
                    if (parentNode.eContainer() instanceof AbstractInOutTree && XmlMapUtil.DOCUMENT.equals(parentNode.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    private void showError(String detailedMessage) {
        ErrorDialogWidthDetailArea dialog = new ErrorDialogWidthDetailArea(null, XmlMapPlugin.PLUGIN_ID,
                "Import fail, please check the xml file!", detailedMessage, IStatus.ERROR);
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

}
