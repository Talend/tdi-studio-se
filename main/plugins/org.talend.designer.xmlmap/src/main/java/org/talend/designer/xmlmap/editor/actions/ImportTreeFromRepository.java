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
package org.talend.designer.xmlmap.editor.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.ConceptTarget;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MdmConceptType;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.builder.connection.impl.XmlXPathLoopDescriptorImpl;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.core.ui.IMDMProviderService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.xmlmap.XmlMapPlugin;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.dialog.RepositoryXmlSelectionDialog;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapConnectionBuilder;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ImportTreeFromRepository extends SelectionAction {

    private MapperManager mapperManager;

    private TreeNode schemaNode;

    private final Shell shell;

    private final Map<String, Integer> xpathAndOrder = new HashMap<String, Integer>();

    private boolean input;

    private MetadataTable metadataTable;

    private List schemaTargets;

    private String absoluteXPathQuery = "";

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.ImportTreeFromRepository";

    private static final String RELATIVE_PATH = "../";

    private static final String RELATIVE_PATH_PATTERN = "\\.\\./";

    List<XMLFileNode> root;

    List<XMLFileNode> group;

    List<XMLFileNode> loop;

    List<TreeNode> groupElements;

    TreeNode loopNode = null;

    private Map<String, String> targetAbsolutePath;

    private Map<String, String> targetDatePattern;

    /**
     * DOC talend ImportTreeFromRepository constructor comment.
     *
     * @param part
     */
    public ImportTreeFromRepository(IWorkbenchPart part, Shell shell) {
        super(part);
        this.shell = shell;
        setId(ID);
        setText("Import From Repository");
    }

    @Override
    public void run() {
        targetAbsolutePath = null;
        loopNode = null;
        xpathAndOrder.clear();
        RepositoryXmlSelectionDialog reviewDialog = new RepositoryXmlSelectionDialog(shell, new String[] { "XML", "MDM" });
        if (reviewDialog.open() == Window.OK) {
            TreeNode treeNodeRoot = XmlMapUtil.getTreeNodeRoot(schemaNode);

            XmlMapUtil.detachNodeConnections(treeNodeRoot, mapperManager.getExternalData(), true);
            RepositoryNode repositoryNode = reviewDialog.getResult();

            Item item = repositoryNode.getObject().getProperty().getItem();
            String detailedMessage = "";
            try {
                if (item instanceof XmlFileConnectionItem) {
                    XmlFileConnectionItem xmlitem = (XmlFileConnectionItem) item;
                    XmlFileConnection connection = (XmlFileConnection) xmlitem.getConnection();
                    prepareEmfTreeFromXml(connection);
                } else if (item instanceof MDMConnectionItem) {
                    String selectedSchema = (String) repositoryNode.getProperties(EProperties.LABEL);
                    MDMConnection connection = (MDMConnection) ((MDMConnectionItem) item).getConnection();
                    prepareEmfTreeFromMdm(connection, selectedSchema);
                }
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
            if (schemaNode.getChildren().isEmpty()) {
                childrenEmpty = true;
                TreeNode rootNode = createModel();
                rootNode.setName("root");
                rootNode.setNodeType(NodeType.ELEMENT);
                rootNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                rootNode.setXpath(XmlMapUtil.getXPath(schemaNode.getXpath(), "root", NodeType.ELEMENT));
                schemaNode.getChildren().add(rootNode);
                if (loopNode == null) {
                    schemaNode.getChildren().get(0).setLoop(true);
                    schemaNode.getChildren().get(0).setMain(true);
                }
            }
            if (childrenEmpty || (detailedMessage != null && !"".equals(detailedMessage))) {
                showError(detailedMessage);
            }

            AbstractInOutTree tree = null;
            if (schemaNode.eContainer() instanceof InputXmlTree) {
                XmlMapConnectionBuilder connectionBuilder = new XmlMapConnectionBuilder();
                connectionBuilder.setCheckRootNodePrefix(true);
                connectionBuilder.rebuildLinks(schemaNode, mapperManager.getExternalData());
                mapperManager.refreshTreeSchemaEditor((InputXmlTree) schemaNode.eContainer());
                // mapperManager.inputTreeSchemaBeanListModified();
                tree = (InputXmlTree) schemaNode.eContainer();
            } else if (schemaNode.eContainer() instanceof OutputXmlTree) {
                // mapperManager.outputTreeSchemaBeanListModified();
                mapperManager.refreshTreeSchemaEditor((OutputXmlTree) schemaNode.eContainer());
                tree = (OutputXmlTree) schemaNode.eContainer();
            }
            if (tree != null) {
                mapperManager.getProblemsAnalyser().checkProblems(tree);
                mapperManager.getMapperUI().updateStatusBar();
            }
        }
    }

    private void prepareEmfTreeFromXml(XmlFileConnection connection) throws Exception {
        if (!connection.isInputModel()) {
            prepareEmfTreeFromConnection(connection);
            // String file = connection.getXmlFilePath();
            // List<FOXTreeNode> list = new ArrayList<FOXTreeNode>();
            // // fix for TDI-20671 , root element is loop in output
            // String rootXpath = null;
            // if (!connection.getRoot().isEmpty()) {
            // rootXpath = connection.getRoot().get(0).getXMLPath();
            // } else if (!connection.getLoop().isEmpty()) {
            // rootXpath = connection.getLoop().get(0).getXMLPath();
            // }
            // File xmlFile = new File(file);
            // if (xmlFile.exists() && !file.endsWith(".zip")) {
            // list = TreeUtil.getFoxTreeNodesForXmlMap(xmlFile.getAbsolutePath(), rootXpath, true);
            // } else if (connection.getFileContent() != null && connection.getFileContent().length > 0) {
            // String xsdFile = initFileContent(connection);
            // if (xsdFile != null && new File(xsdFile).exists()) {
            // String targetNameSpace = connection.getTargetNameSpace();
            // list = TreeUtil.getFoxTreeNodesForXmlMap(xsdFile, rootXpath, targetNameSpace, true);
            // } else {
            // // for manually created output
            // prepareEmfTreeFromConnection(connection);
            // return;
            //
            // }
            // } else {
            // // for manually created output
            // prepareEmfTreeFromConnection(connection);
            // return;
            // }
            // schemaNode.getChildren().clear();
            // root = connection.getRoot();
            // loop = connection.getLoop();
            // group = connection.getGroup();
            // groupElements = new ArrayList<TreeNode>();
            // prepareModelFromOutput(list, schemaNode);
            // if (loopNode != null) {
            // fillGroup(loopNode, groupElements);
            // } else {
            // if (!schemaNode.getChildren().isEmpty()) {
            // schemaNode.getChildren().get(0).setLoop(true);
            // }
            // }
        } else {
            List<SchemaTarget> schemaTargets = null;
            if (!connection.getSchema().isEmpty() && connection.getSchema().get(0) instanceof XmlXPathLoopDescriptorImpl) {
                absoluteXPathQuery = ((XmlXPathLoopDescriptorImpl) connection.getSchema().get(0)).getAbsoluteXPathQuery();
                schemaTargets = ((XmlXPathLoopDescriptorImpl) connection.getSchema().get(0)).getSchemaTargets();
                metadataTable = ConnectionHelper.getTables(connection).toArray(new MetadataTable[0])[0];

            }

            // fix for TDI-8707 : only import mapped elements from connection to xml map
            if (schemaTargets == null || schemaTargets.isEmpty()) {
                return;
            }

            this.schemaTargets = schemaTargets;
            String file = connection.getXmlFilePath();
            List<FOXTreeNode> list = new ArrayList<FOXTreeNode>();
            File xmlFile = new File(file);
            if (xmlFile.exists()) {
                list = TreeUtil.getFoxTreeNodesForXmlMap(xmlFile.getAbsolutePath(), absoluteXPathQuery, true);
            } else if (connection.getFileContent() != null && connection.getFileContent().length > 0) {
                String xsdFile = initFileContent(connection);
                if (xsdFile != null && new File(xsdFile).exists()) {
                    list = TreeUtil.getFoxTreeNodesForXmlMap(xsdFile, absoluteXPathQuery, true);
                }
            } else {
                throw new FileNotFoundException();
            }
            schemaNode.getChildren().clear();
            prepareEmfTree(list, schemaNode);
        }

    }

    private void prepareEmfTreeFromConnection(XmlFileConnection connection) {
        schemaNode.getChildren().clear();
        EList root = connection.getRoot();
        EList loop = connection.getLoop();
        EList group = connection.getGroup();

        TreeNode rootNode = null;
        TreeNode current = null;
        TreeNode temp = null;
        TreeNode mainNode = null;
        // xpath in item file
        String mainPath = null;
        // xpath in item file
        String currentPath = "";
        String defaultValue = null;

        MetadataTable metaTable = ConnectionHelper.getTables(connection).toArray(new MetadataTable[0])[0];

        // build root tree
        for (int i = 0; i < root.size(); i++) {
            XMLFileNode node = (XMLFileNode) root.get(i);
            String newPath = node.getXMLPath();
            defaultValue = node.getDefaultValue();
            String type = node.getType();

            if (node.getAttribute().equals("attri")) {
                this.addAttributeNamespace(current, currentPath, newPath, type, NodeType.ATTRIBUT, defaultValue);
            } else if (node.getAttribute().equals("ns")) {
                this.addAttributeNamespace(current, currentPath, newPath, type, NodeType.NAME_SPACE, defaultValue);
            } else {
                temp = this.addElement(current, currentPath, newPath, type,
                        XmlMapUtil.getColumnPatternFromMetadataTable(node, metaTable), NodeType.ELEMENT, node.getOrder());
                if (rootNode == null) {
                    rootNode = temp;
                }
                if (node.getAttribute().equals("main")) {
                    temp.setMain(true);
                    mainNode = temp;
                    mainPath = newPath;
                }
                current = temp;
                currentPath = newPath;
            }

        }

        // build group tree
        current = mainNode;
        currentPath = mainPath;
        boolean isFirst = true;
        groupElements = new ArrayList<TreeNode>();
        for (int i = 0; i < group.size(); i++) {
            XMLFileNode node = (XMLFileNode) group.get(i);
            String newPath = node.getXMLPath();
            defaultValue = node.getDefaultValue();
            String type = node.getType();
            if (node.getAttribute().equals("attri")) {
                this.addAttributeNamespace(current, currentPath, newPath, type, NodeType.ATTRIBUT, defaultValue);
            } else if (node.getAttribute().equals("ns")) {
                this.addAttributeNamespace(current, currentPath, newPath, type, NodeType.NAME_SPACE, defaultValue);
            } else {
                temp = this.addElement(current, currentPath, newPath, type,
                        XmlMapUtil.getColumnPatternFromMetadataTable(node, metaTable), NodeType.ELEMENT, node.getOrder());
                groupElements.add(temp);
                if (node.getAttribute().equals("main")) {
                    temp.setMain(true);
                    mainNode = temp;
                    mainPath = newPath;
                }
                current = temp;
                currentPath = newPath;
            }

        }

        // build loop tree
        current = mainNode;
        currentPath = mainPath;
        isFirst = true;
        for (int i = 0; i < loop.size(); i++) {
            XMLFileNode node = (XMLFileNode) loop.get(i);
            String newPath = node.getXMLPath();
            defaultValue = node.getDefaultValue();
            String type = node.getType();
            if (node.getAttribute().equals("attri")) {
                this.addAttributeNamespace(current, currentPath, newPath, type, NodeType.ATTRIBUT, defaultValue);
            } else if (node.getAttribute().equals("ns")) {
                this.addAttributeNamespace(current, currentPath, newPath, type, NodeType.NAME_SPACE, defaultValue);
            } else {
                temp = this.addElement(current, currentPath, newPath, type,
                        XmlMapUtil.getColumnPatternFromMetadataTable(node, metaTable), NodeType.ELEMENT, node.getOrder());
                // if root node is loop
                if (rootNode == null) {
                    rootNode = temp;
                }
                if (node.getAttribute().equals("main")) {
                    temp.setMain(true);
                    mainNode = temp;
                    mainPath = newPath;
                }
                if (isFirst) {
                    temp.setLoop(true);
                    loopNode = temp;
                    isFirst = false;
                }
                current = temp;
                currentPath = newPath;
            }
        }

        if (rootNode != null) {
            schemaNode.getChildren().add(rootNode);
        }
        if (loopNode != null) {
            fillGroup(loopNode, groupElements);
        }

    }

    private void addAttributeNamespace(TreeNode current, String currentPath, String newPath, String type, NodeType nodeType,
            String defaultValue) {
        TreeNode temp = createModel();
        String name = null;
        name = newPath;
        if (NodeType.NAME_SPACE.equals(nodeType)) {
            if ("".endsWith(name)) {
                name = XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX;
            }
        }
        temp.setName(name);

        temp.setDefaultValue(defaultValue);
        if (type == null) {
            type = XmlMapUtil.DEFAULT_DATA_TYPE;
        }
        temp.setType(type);
        temp.setNodeType(nodeType);
        if (type.equals("id_Date")) {
            temp.setPattern("\"dd-MM-yyyy\"");//$NON-NLS-1$
        }
        temp.setXpath(XmlMapUtil.getXPath(current.getXpath(), name, nodeType));
        current.getChildren().add(temp);
    }

    private TreeNode addElement(TreeNode current, String currentPath, String newPath, String type, String pattern,
            NodeType nodeType, int order) {
        TreeNode temp = createModel();
        String name = newPath.substring(newPath.lastIndexOf("/") + 1); //$NON-NLS-1$
        String parentPath = newPath.substring(0, newPath.lastIndexOf("/"));
        temp.setName(name);
        if (type == null) {
            type = XmlMapUtil.DEFAULT_DATA_TYPE;
        }
        temp.setType(type);
        temp.setNodeType(nodeType);
        if (pattern != null) {
            temp.setPattern(pattern);
        } else if (type.equals("id_Date")) {
            temp.setPattern("\"dd-MM-yyyy\"");//$NON-NLS-1$
        }

        if (current == null) {// root node
            temp.setXpath(XmlMapUtil.getXPath(schemaNode.getXpath(), name, nodeType));
            return temp;
        }

        if (currentPath.equals(parentPath)) {
            temp.setXpath(XmlMapUtil.getXPath(current.getXpath(), name, nodeType));
            addChildInOrder(current, temp, order);
        } else {
            String[] nods = currentPath.split("/"); //$NON-NLS-1$
            String[] newNods = parentPath.split("/"); //$NON-NLS-1$
            int parentLevel = 0;
            int checkLength = nods.length < newNods.length ? nods.length : newNods.length;
            for (int i = 1; i < checkLength; i++) {
                if (nods[i].equals(newNods[i])) {
                    parentLevel = i;
                }
            }
            TreeNode parent = current;
            for (int i = 0; i < nods.length - (parentLevel + 1); i++) {
                TreeNode tmpParent = (TreeNode) parent.eContainer();
                if (tmpParent == null) {
                    break;
                }
                parent = tmpParent;
            }

            if (parent != null) {
                temp.setXpath(XmlMapUtil.getXPath(parent.getXpath(), name, nodeType));
                addChildInOrder(parent, temp, order);
            }
        }

        return temp;
    }

    private void addChildInOrder(TreeNode parent, TreeNode child, int childOrder) {
        xpathAndOrder.put(child.getXpath(), childOrder);
        Integer index = null;
        List<TreeNode> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Integer order = xpathAndOrder.get(children.get(i).getXpath());
            if (order == null) {
                order = 0;
            }
            if (order < childOrder) {
                index = i + 1;
            }
        }
        if (index == null) {
            parent.getChildren().add(0, child);
        } else {
            parent.getChildren().add(index, child);
        }

    }

    private void prepareEmfTree(List<FOXTreeNode> list, TreeNode parent) {
        prepareEmfTree(list, parent, false);
    }

    private void prepareEmfTree(List<FOXTreeNode> list, TreeNode parent, boolean ignoreMapped) {
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
            TreeNode createTreeNode = createModel();
            createTreeNode.setOptional(foxNode.isOptional());
            String label = foxNode.getLabel();
            createTreeNode.setName(label);
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
            // tempXpath is current xpath remove schema node xpath like: row1:newColumn1
            String tempXpath = createTreeNode.getXpath().substring(schemaNode.getXpath().length() + 1);

            if (createTreeNode.isChoice() || createTreeNode.isSubstitution()) {
                if (!isMappedChoiceSubs(foxNode, xPath)) {
                    continue;
                }

            } else if (!ignoreMapped && !isMappedChild(tempXpath)) {
                continue;
            }

            // get talend type from metadata table
            String dataType = null;
            if (targetAbsolutePath != null) {
                dataType = targetAbsolutePath.get(tempXpath);
            }
            if (dataType == null) {
                dataType = foxNode.getDataType();
            }
            if (dataType != null && !"".equals(dataType)) {
                if (dataType.equals("id_Date")) {
                    String datePattern = targetDatePattern.get(tempXpath);
                    if (datePattern == null || "".equals(datePattern)) { //$NON-NLS-1$
                        datePattern = "\"dd-MM-yyyy\""; //$NON-NLS-1$
                    }
                    createTreeNode.setPattern(datePattern);
                }
                createTreeNode.setType(dataType);
            } else {
                createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
            }

            if (tempXpath.equals(absoluteXPathQuery)) {
                createTreeNode.setLoop(true);
                createTreeNode.setOptional(foxNode.isOptional());
                loopNode = createTreeNode;
            }

            parent.getChildren().add(createTreeNode);
            if (createTreeNode.isLoop()) {
                XmlMapUtil.upsetMainNode(createTreeNode);
            }
            if (foxNode.getChildren() != null && !foxNode.getChildren().isEmpty()) {
                prepareEmfTree(foxNode.getChildren(), createTreeNode, ignoreMapped);
            }
        }

    }

    private boolean isMappedChoiceSubs(FOXTreeNode choiceSubs, String parentXpath) {
        // if any child of the choice or subs is mapped to target schema , it should be created in xmlmap
        for (FOXTreeNode child : choiceSubs.getChildren()) {
            NodeType nodeType = null;
            if (child instanceof Element) {
                nodeType = NodeType.ELEMENT;
            } else if (child instanceof Attribute) {
                nodeType = NodeType.ATTRIBUT;
            } else if (child instanceof NameSpaceNode) {
                nodeType = NodeType.NAME_SPACE;
            }
            String tempPath = XmlMapUtil.getXPath(parentXpath, child.getLabel(), nodeType);
            tempPath = tempPath.substring(schemaNode.getXpath().length() + 1);
            if (isMappedChild(tempPath)) {
                return true;
            }
        }
        return false;
    }

    private void prepareEmfTreeFromMdm(MDMConnection connection, String selectedConcept) throws Exception {
        if (connection == null || selectedConcept == null) {
            return;
        }
        EList<Concept> schemas = connection.getSchemas();
        Concept selected = null;
        for (int i = 0; i < schemas.size(); i++) {
            Concept concept = schemas.get(i);
            if (selectedConcept.equals(concept.getLabel())) {
                selected = concept;
                break;
            }
        }

        if (selected != null) {
            IMDMProviderService service = (IMDMProviderService) GlobalServiceRegister.getDefault().getService(
                    IMDMProviderService.class);
            if (service != null) {
                boolean initConcepts = service.initConcepts(connection);
                if (initConcepts) {
                    if (!MdmConceptType.OUTPUT.equals(selected.getConceptType())) {
                        String prefix = service.getXPathPrefixValue(selected);
                        prefix = TalendTextUtils.removeQuotes(prefix);
                        absoluteXPathQuery = selected.getLoopExpression();
                        EList<ConceptTarget> conceptTargets = selected.getConceptTargets();
                        if (conceptTargets == null || absoluteXPathQuery == null) {
                            return;
                        }
                        this.schemaTargets = conceptTargets;
                        // find corresponding metadata table
                        Set<MetadataTable> metadataTables = ConnectionHelper.getTables(connection);
                        for (MetadataTable table : metadataTables) {
                            if (selected.getLabel() != null && selected.getLabel().equals(table.getLabel())) {
                                metadataTable = table;
                                break;
                            }
                        }

                        List<FOXTreeNode> list = TreeUtil.getFoxTreeNodesForXmlMap(getTempTemplateXSDFile().getAbsolutePath(),
                                absoluteXPathQuery, true);

                        TreeNode pNode = schemaNode;
                        if (MdmConceptType.RECEIVE.equals(selected.getConceptType())) {
                            List<FOXTreeNode> updateNodesList = TreeUtil.parseMDMUpdateReport(shell, true);
                            schemaNode.getChildren().clear();
                            if (updateNodesList == null) {
                                if (prefix != null && !"".equals(prefix)) {
                                    String[] preValues = prefix.split(XmlMapUtil.XPATH_SEPARATOR);
                                    for (String value : preValues) {
                                        if (!"".equals(value)) {
                                            TreeNode createTreeNode = createModel();
                                            createTreeNode.setName(value);
                                            createTreeNode.setNodeType(NodeType.ELEMENT);
                                            createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                                            createTreeNode.setXpath(XmlMapUtil.getXPath(pNode.getXpath(),
                                                    createTreeNode.getName(), createTreeNode.getNodeType()));
                                            pNode.getChildren().add(createTreeNode);
                                            pNode = createTreeNode;
                                        }
                                    }
                                    absoluteXPathQuery = prefix + absoluteXPathQuery;
                                }
                            } else {
                                TreeNode rootNode = createDefaultTreeNode(pNode, "exchange"); //$NON-NLS-1$
                                TreeNode reportNode = createDefaultTreeNode(rootNode, "report"); //$NON-NLS-1$
                                TreeNode itemNode = createDefaultTreeNode(rootNode, "item"); //$NON-NLS-1$
                                // parse the update content..
                                prepareEmfTree(updateNodesList, reportNode, true);
                                pNode = itemNode;
                                absoluteXPathQuery = "/exchange/item" + absoluteXPathQuery; //$NON-NLS-1$
                            }
                        } else {
                            schemaNode.getChildren().clear();
                        }
                        prepareEmfTree(list, pNode);
                    } else {
                        List<FOXTreeNode> list = TreeUtil.getFoxTreeNodesForXmlMap(getTempTemplateXSDFile().getAbsolutePath(),
                                selected.getRoot().get(0).getXMLPath(), true);
                        schemaNode.getChildren().clear();
                        root = selected.getRoot();
                        loop = selected.getLoop();
                        group = selected.getGroup();
                        groupElements = new ArrayList<TreeNode>();

                        prepareModelFromOutput(list, schemaNode);
                        if (loopNode != null) {
                            fillGroup(loopNode, groupElements);
                        } else {
                            if (!schemaNode.getChildren().isEmpty()) {
                                schemaNode.getChildren().get(0).setLoop(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private TreeNode createDefaultTreeNode(TreeNode parentNode, String name) {
        TreeNode createTreeNode = createModel();
        createTreeNode.setName(name);
        createTreeNode.setNodeType(NodeType.ELEMENT);
        createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
        createTreeNode
                .setXpath(XmlMapUtil.getXPath(parentNode.getXpath(), createTreeNode.getName(), createTreeNode.getNodeType()));
        parentNode.getChildren().add(createTreeNode);

        return createTreeNode;
    }

    private boolean isMappedChild(String tempXpath) {

        if (targetAbsolutePath == null) {
            targetAbsolutePath = new HashMap<String, String>();
            targetDatePattern = new HashMap<String, String>();
            targetAbsolutePath.put(absoluteXPathQuery, null);
            Pattern regex = Pattern.compile(RELATIVE_PATH_PATTERN, Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE
                    | Pattern.MULTILINE);
            for (int n = 0; n < schemaTargets.size(); n++) {
                Object obj = schemaTargets.get(n);
                String relativeXPathQuery = "";
                if (obj instanceof SchemaTarget) {
                    relativeXPathQuery = ((SchemaTarget) obj).getRelativeXPathQuery();
                } else if (obj instanceof ConceptTarget) {
                    relativeXPathQuery = ((ConceptTarget) obj).getRelativeLoopExpression();
                }

                StringBuffer tempAbsolute = new StringBuffer();
                tempAbsolute.append(absoluteXPathQuery);
                tempAbsolute.append(XmlMapUtil.XPATH_SEPARATOR);
                tempAbsolute.append(relativeXPathQuery);

                if (relativeXPathQuery.startsWith(RELATIVE_PATH)) {
                    Matcher regexMatcher = regex.matcher(relativeXPathQuery);
                    int relativeLength = 0;
                    while (regexMatcher.find()) {
                        relativeLength++;
                    }
                    if (relativeLength > 0) {
                        String subRelativeQuery = relativeXPathQuery.substring(relativeLength * RELATIVE_PATH.length(),
                                relativeXPathQuery.length());
                        String[] absoluteSplit = absoluteXPathQuery.split(XmlMapUtil.XPATH_SEPARATOR);
                        if (absoluteSplit.length > relativeLength) {
                            tempAbsolute = new StringBuffer();
                            for (int i = 0; i < absoluteSplit.length - relativeLength; i++) {
                                tempAbsolute.append(absoluteSplit[i]);
                                tempAbsolute.append(XmlMapUtil.XPATH_SEPARATOR);
                            }
                            tempAbsolute.append(subRelativeQuery);
                        }

                    }
                }
                if (metadataTable != null && n < metadataTable.getColumns().size()) {
                    MetadataColumn column = metadataTable.getColumns().get(n);
                    targetAbsolutePath.put(tempAbsolute.toString(), column.getTalendType());
                    targetDatePattern.put(tempAbsolute.toString(), column.getPattern());
                } else {
                    targetAbsolutePath.put(tempAbsolute.toString(), null);
                    targetDatePattern.put(tempAbsolute.toString(), null);
                }
            }
        }

        for (String absTarget : targetAbsolutePath.keySet()) {
            if (absTarget.startsWith(tempXpath)) {
                return true;
            }
        }

        return false;
    }

    /*
     * same as XmlFileOutputStep2Form.initXmlTreeData()
     */
    private String initFileContent(XmlFileConnection connection) throws IOException {
        byte[] bytes = connection.getFileContent();
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject fsProject = null;
        try {
            fsProject = ResourceUtils.getProject(project.getTechnicalLabel());
        } catch (PersistenceException e2) {
            ExceptionHandler.process(e2);
        }
        if (fsProject == null) {
            return null;
        }
        String temPath = fsProject.getLocationURI().getPath() + File.separator + "temp";
        String fileName = "";

        String pathStr = connection.getXmlFilePath();
        if (connection.isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connection, true);
            pathStr = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, pathStr));
        }
        if (pathStr != null && XmlUtil.isXMLFile(pathStr)) {
            fileName = StringUtil.TMP_XML_FILE;
        } else if (pathStr != null && XmlUtil.isXSDFile(pathStr)) {
            fileName = StringUtil.TMP_XSD_FILE;
        } else if (pathStr != null && XmlUtil.isWSDLFile(pathStr)) {
            fileName = StringUtil.TMP_WSDL_FILE;
        } else if (pathStr.contains(".zip")) {
            fileName = new Path(pathStr).lastSegment();
        } else {
            return null;
        }
        File temfile = new File(temPath + File.separator + fileName);
        if (!temfile.exists()) {
            temfile.createNewFile();
        }

        FileOutputStream outStream;
        outStream = new FileOutputStream(temfile);
        outStream.write(bytes);
        outStream.close();

        return temfile.getPath();

    }

    private File getTempTemplateXSDFile() {
        IPath tempPath = SharedStudioUtils.getTempFolderPath();
        File tempFile = tempPath.toFile();
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        File file = new File(tempFile, "template.xsd"); //$NON-NLS-1$
        return file;
    }

    private void prepareModelFromOutput(List<FOXTreeNode> foxTreeNodes, TreeNode parent) {
        TreeNode realParent = XmlMapUtil.getRealParentNode(parent);
        for (FOXTreeNode foxNode : foxTreeNodes) {
            TreeNode createTreeNode = createModel();
            createTreeNode.setOptional(foxNode.isOptional());
            String label = foxNode.getLabel();
            createTreeNode.setName(label);
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

            String xPath = XmlMapUtil.getXPath(realParent.getXpath(), label, createTreeNode.getNodeType());
            createTreeNode.setXpath(xPath);
            if (createTreeNode.isSubstitution() || createTreeNode.isChoice()) {
                if (isMappedChoiceSubsOutput(foxNode, realParent.getXpath())) {
                    parent.getChildren().add(createTreeNode);
                }
            } else {
                String subXpath = xPath.substring(schemaNode.getXpath().length() + 1, xPath.length());
                XMLFileNode found = findXmlFileNode(subXpath, root);
                if (found == null) {
                    found = findXmlFileNode(subXpath, group);
                    if (found != null) {
                        groupElements.add(createTreeNode);
                    }
                }
                if (found == null) {
                    found = findXmlFileNode(subXpath, loop);
                    if (found != null) {
                        if (loopNode == null) {
                            loopNode = createTreeNode;
                            loopNode.setLoop(true);
                            loopNode.setOptional(foxNode.isOptional());
                        }
                    }
                }
                if (found == null) {
                    continue;
                } else {
                    String dataType = found.getType();
                    if (dataType == null || dataType.equals("")) {
                        dataType = foxNode.getDataType();
                    }
                    if (dataType != null && !"".equals(dataType)) {
                        if (dataType.equals("id_Date")) {
                            createTreeNode.setPattern("\"dd-MM-yyyy\"");//$NON-NLS-1$
                        }
                        createTreeNode.setType(dataType);
                    } else {
                        createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                    }

                    addChildInOrder(parent, createTreeNode, found.getOrder());
                }

            }
            prepareModelFromOutput(foxNode.getChildren(), createTreeNode);
        }
    }

    private boolean isMappedChoiceSubsOutput(FOXTreeNode node, String pXpath) {
        List<FOXTreeNode> children = node.getChildren();
        List<FOXTreeNode> subsChoice = new ArrayList<FOXTreeNode>();
        XMLFileNode found = null;
        for (FOXTreeNode child : children) {
            if (child.isSubstitution() || child.isChoice()) {
                subsChoice.add(child);
            } else {
                String childXpath = "";
                if (child instanceof Element) {
                    childXpath = XmlMapUtil.getXPath(pXpath, child.getLabel(), NodeType.ELEMENT);
                } else if (child instanceof Attribute) {
                    childXpath = XmlMapUtil.getXPath(pXpath, child.getLabel(), NodeType.ATTRIBUT);
                } else if (child instanceof NameSpaceNode) {
                    childXpath = XmlMapUtil.getXPath(pXpath, child.getLabel(), NodeType.NAME_SPACE);
                }

                found = findXmlFileNode(childXpath, root);
                if (found == null) {
                    found = findXmlFileNode(childXpath, group);
                }
                if (found == null) {
                    found = findXmlFileNode(childXpath, loop);
                }
                if (found != null) {
                    break;
                }
            }
        }

        if (found != null) {
            return true;
        }

        if (!subsChoice.isEmpty()) {
            for (FOXTreeNode foxNode : subsChoice) {
                boolean isChildMapped = isMappedChoiceSubsOutput(foxNode, pXpath);
                if (isChildMapped) {
                    return true;
                }
            }
        }

        return found != null;
    }

    private XMLFileNode findXmlFileNode(String subXpath, List<XMLFileNode> list) {
        String pXpath = "";
        for (XMLFileNode fileNode : list) {
            if (fileNode.getAttribute().equals("attri")) {
                String tempXpath = pXpath + XmlMapUtil.XPATH_SEPARATOR + XmlMapUtil.XPATH_ATTRIBUTE + fileNode.getXMLPath();
                if (subXpath != null && subXpath.endsWith(tempXpath)) {
                    return fileNode;
                }
            } else if (fileNode.getAttribute().equals("ns")) {
                String tempXpath = pXpath + XmlMapUtil.XPATH_SEPARATOR + XmlMapUtil.XPATH_NAMESPACE + fileNode.getXMLPath();
                if (subXpath != null && subXpath.endsWith(tempXpath)) {
                    return fileNode;
                }
            } else {
                pXpath = fileNode.getXMLPath();
                if (subXpath != null && subXpath.endsWith(pXpath)) {
                    return fileNode;
                }
            }
            if (fileNode.getXMLPath() != null && fileNode.getXMLPath().equals(subXpath)) {
                return fileNode;
            }
        }
        return null;
    }

    private void fillGroup(TreeNode loopElement, List<TreeNode> groupElements) {
        final EObject parent = loopElement.eContainer();
        if (parent instanceof TreeNode) {
            if (groupElements.contains(parent)) {
                ((TreeNode) parent).setGroup(true);
            }
            if (parent.eContainer() != null && !(parent.eContainer().eContainer() instanceof AbstractInOutTree)) {
                fillGroup((TreeNode) parent, groupElements);
            }
        }

    }

    @Override
    protected boolean calculateEnabled() {
        RepositoryNode rootNode = ProjectRepositoryNode.getInstance().getRootRepositoryNode(ERepositoryObjectType.METADATA);
        if (getSelectedObjects().isEmpty() || rootNode == null) {
            return false;
        } else {
            // get the last selection to run the action
            Object s = getSelectedObjects().get(0);
            if (s instanceof List && !((List) s).isEmpty()) {
                List selectedarts = (List) s;
                Object object = selectedarts.get(selectedarts.size() - 1);
                if (object instanceof TreeNodeEditPart) {
                    TreeNodeEditPart parentPart = (TreeNodeEditPart) object;
                    schemaNode = (TreeNode) parentPart.getModel();
                    if (schemaNode.eContainer() instanceof AbstractInOutTree && XmlMapUtil.DOCUMENT.equals(schemaNode.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    private void showError(String detailedMessage) {
        ErrorDialogWidthDetailArea dialog = new ErrorDialogWidthDetailArea(null, XmlMapPlugin.PLUGIN_ID,
                "Import from repository fail, please check the repository connection!", detailedMessage, IStatus.ERROR);
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    private TreeNode createModel() {
        if (input) {
            return XmlmapFactory.eINSTANCE.createTreeNode();
        } else {
            return XmlmapFactory.eINSTANCE.createOutputTreeNode();
        }
    }

}
