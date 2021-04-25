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
package org.talend.repository.json.ui.wizards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.datatools.xml.utils.ATreeNode;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.OtherConnectionContextUtils.EParamName;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.JSONFileNode;
import org.talend.repository.model.json.JsonFactory;

/**
 * DOC cantoine class global comment. Detailled comment <br/>
 *
 * $Id: AbstractJSONFileStepForm.java 48226 2010-09-14 10:04:12Z hywang $
 *
 */
public abstract class AbstractJSONFileStepForm extends AbstractJSONStepForm {

    protected JSONFileConnection connection;

    protected int order = 1;

    protected Map<String, Integer> orderMap = new HashMap<String, Integer>();

    protected boolean xsdPathChanged = false;

    /**
     * DOC cantoine AbstractJSONFileStepForm constructor comment. Use to step1
     */
    public AbstractJSONFileStepForm(Composite parent, ConnectionItem connectionItem, String[] existingNames) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
    }

    /**
     * DOC cantoine AbstractJSONFileStepForm constructor comment. Use to step2
     *
     * @param parent
     * @param connection2
     */
    public AbstractJSONFileStepForm(Composite parent, ConnectionItem connectionItem) {
        this(parent, connectionItem, null);
    }

    /**
     * DOC cantoine AbstractDelimitedFileStepForm constructor comment. Use to step1
     */
    public AbstractJSONFileStepForm(Composite parent, ConnectionItem connectionItem, MetadataTable metadataTable,
            String[] existingNames) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
    }

    protected JSONFileConnection getConnection() {
        return (JSONFileConnection) connectionItem.getConnection();
    }

    @Override
    protected void exportAsContext() {
        collectConnParams();
        super.exportAsContext();
    }

    protected void collectConnParams() {
        if (getConnection().isInputModel()) {
            addContextParams(EParamName.FilePath, true);
            addContextParams(EParamName.Encoding, true);
            addContextParams(EParamName.XPathQuery, true);
        } else {
            addContextParams(EParamName.OutputFilePath, true);
        }

    }

    protected void initMetadataTable(List<FOXTreeNode> list, EList columnList) {
        int maxColumnsNumber = CoreUIPlugin.getDefault().getPreferenceStore()
                .getInt(ITalendCorePrefConstants.MAXIMUM_AMOUNT_OF_COLUMNS_FOR_XML);
        for (FOXTreeNode node : list) {
            if (columnList.size() > maxColumnsNumber) {
                return;
            }
            MetadataEmfTableEditor editor = new MetadataEmfTableEditor();
            if (node instanceof Element) {
                String label = node.getLabel();
                if (!node.hasChildren() && label != null && !label.equals("")) {
                    String columnName = label;
                    if (columnName.contains(":")) { //$NON-NLS-1$
                        columnName = columnName.split(":")[1]; //$NON-NLS-1$
                    }
                    columnName = columnName.replaceAll("[^a-zA-Z0-9]", "_");
                    String dataType = node.getDataType();
                    MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                    metadataColumn.setLabel(editor.getNextGeneratedColumnName(columnName, columnList));
                    metadataColumn.setOriginalField(label);
                    metadataColumn.setTalendType(dataType);
                    columnList.add(metadataColumn);
                    node.setColumn(ConvertionHelper.convertToIMetaDataColumn(metadataColumn));
                }
            }
            if (node instanceof Attribute) {
                String label = node.getLabel();
                if (label != null && !label.equals("")) {
                    String columnName = label;
                    if (columnName.contains(":")) { //$NON-NLS-1$
                        columnName = columnName.split(":")[1]; //$NON-NLS-1$
                    }
                    columnName = columnName.replaceAll("[^a-zA-Z0-9]", "_");
                    String dataType = node.getDataType();
                    MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                    metadataColumn.setLabel(editor.getNextGeneratedColumnName(columnName, columnList));
                    metadataColumn.setOriginalField(label);
                    metadataColumn.setTalendType(dataType);
                    columnList.add(metadataColumn);
                    node.setColumn(ConvertionHelper.convertToIMetaDataColumn(metadataColumn));
                }
            }
            if (node.hasChildren()) {
                List<FOXTreeNode> children = node.getChildren();
                initMetadataTable(children, columnList);
            }
        }
    }

    protected void initNodeOrder(FOXTreeNode node) {
        if (node == null) {
            return;
        }
        FOXTreeNode parent = node.getParent();
        if (parent == null) {
            node.setOrder(1);
            String path = TreeUtil.getPath(node);
            orderMap.put(path, order);
            order++;
        }
        List<FOXTreeNode> childNode = node.getChildren();
        for (FOXTreeNode child : childNode) {
            child.setOrder(order);
            String path = TreeUtil.getPath(child);
            orderMap.put(path, order);
            order++;
            if (child.getChildren().size() > 0) {
                initNodeOrder(child);
            }
        }
    }

    protected int getNodeOrder(FOXTreeNode node) {
        if (node != null) {
            String path = TreeUtil.getPath(node);
            return orderMap.get(path);
        }
        return 0;
    }

    protected void tableLoader(Element element, String parentPath, List<JSONFileNode> table, String defaultValue) {
        JSONFileNode JSONFileNode = JsonFactory.eINSTANCE.createJSONFileNode();
        String currentPath = parentPath + "/" + element.getLabel();
        JSONFileNode.setJSONPath(currentPath);
        JSONFileNode.setRelatedColumn(element.getColumnLabel());
        JSONFileNode.setAttribute(element.isMain() ? "main" : "branch");
        JSONFileNode.setDefaultValue(defaultValue);
        JSONFileNode.setType(element.getDataType());
        JSONFileNode.setOrder(getNodeOrder(element));
        table.add(JSONFileNode);
        for (FOXTreeNode att : element.getAttributeChildren()) {
            JSONFileNode = JsonFactory.eINSTANCE.createJSONFileNode();
            JSONFileNode.setJSONPath(att.getLabel());
            JSONFileNode.setRelatedColumn(att.getColumnLabel());
            JSONFileNode.setAttribute("attri");
            JSONFileNode.setDefaultValue(att.getDefaultValue());
            JSONFileNode.setType(att.getDataType());
            JSONFileNode.setOrder(getNodeOrder(att));
            table.add(JSONFileNode);
        }
        for (FOXTreeNode att : element.getNameSpaceChildren()) {
            JSONFileNode = JsonFactory.eINSTANCE.createJSONFileNode();
            JSONFileNode.setJSONPath(att.getLabel());
            JSONFileNode.setRelatedColumn(att.getColumnLabel());
            JSONFileNode.setAttribute("ns");
            JSONFileNode.setDefaultValue(att.getDefaultValue());
            JSONFileNode.setType(att.getDataType());
            JSONFileNode.setOrder(getNodeOrder(att));
            table.add(JSONFileNode);
        }
        List<FOXTreeNode> children = element.getElementChildren();
        for (FOXTreeNode child : children) {
            if (!child.isGroup() && !child.isLoop()) {
                tableLoader((Element) child, currentPath, table, child.getDefaultValue());
            }
        }

    }

    protected void updateConnectionProperties(FOXTreeNode foxTreeNode) {
        EList root = getConnection().getRoot();
        EList loop = getConnection().getLoop();
        EList group = getConnection().getGroup();
        root.clear();
        loop.clear();
        group.clear();
        if (foxTreeNode != null) {
            initNodeOrder(foxTreeNode);
            tableLoader((Element) foxTreeNode, "", root, foxTreeNode.getDefaultValue());
            Element loopNode = (Element) TreeUtil.getLoopNode(foxTreeNode);
            if (loopNode != null) {
                String path = TreeUtil.getPath(loopNode);
                tableLoader(loopNode, path.substring(0, path.lastIndexOf("/")), loop, loopNode.getDefaultValue());
            }
            Element groupNode = (Element) TreeUtil.getGroupNode(foxTreeNode);
            if (groupNode != null) {
                String path = TreeUtil.getPath(groupNode);
                tableLoader(groupNode, path.substring(0, path.lastIndexOf("/")), group, groupNode.getDefaultValue());
            }
        }
    }

    protected List<FOXTreeNode> getCorrespondingFoxTreeNodes(ATreeNode selectedRootNode, boolean resolved) {
        JSONWizard wizard = ((JSONWizard) getPage().getWizard());
        Map<String, List<FOXTreeNode>> foxNodesMap = wizard.getFoxNodesMap();
        String key = String.valueOf(selectedRootNode.getValue());
        List<FOXTreeNode> foxTreeNodes = foxNodesMap.get(key);
        // if (foxTreeNodes == null) {
        // XSDSchema xsdSchema = wizard.getXSDSchema();
        // if (xsdSchema == null) {
        // return new ArrayList<FOXTreeNode>();
        // }
        // foxTreeNodes = TreeUtil.getFoxTreeNodesByRootNode(xsdSchema, selectedRootNode, resolved);
        // foxNodesMap.put(key, foxTreeNodes);
        // }

        return foxTreeNodes;
    }

    protected void updateTreeNodes(String JSONFilePath) {
        if (JSONFilePath == null || "".equals(JSONFilePath)) {
            return;
        }
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(), true);
            JSONFilePath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, JSONFilePath));
        }
        // updateRootNodes(updateXSDSchema(JSONFilePath), false);
    }

    protected ATreeNode getAdaptRootNode(List<ATreeNode> rootNodes) {
        ATreeNode rootNode = null;
        if (rootNodes == null || rootNodes.size() == 0) {
            return rootNode;
        }
        if (rootNodes.size() > 0) {
            JSONFileNode selectedNode = getConnection().getRoot().get(0);
            if (selectedNode != null) {
                String JSONPath = selectedNode.getJSONPath();
                if (JSONPath != null && JSONPath.length() > 0) {
                    JSONPath = JSONPath.substring(JSONPath.lastIndexOf("/") + 1); //$NON-NLS-1$
                    for (int i = 0; i < rootNodes.size(); i++) {
                        ATreeNode node = rootNodes.get(i);
                        if (JSONPath.equals(node.getValue())) {
                            rootNode = node;
                            break;
                        }
                    }
                }
            }
        }
        return rootNode;
    }

    protected String getXSDJSONFilePath() {
        if (getConnection().getFileContent() == null || getConnection().getFileContent().length == 0) {
            return null;
        }
        byte[] bytes = getConnection().getFileContent();
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject fsProject = null;
        try {
            fsProject = ResourceUtils.getProject(project);
        } catch (PersistenceException e2) {
            ExceptionHandler.process(e2);
        }
        if (fsProject == null) {
            return null;
        }
        String temPath = fsProject.getLocationURI().getPath() + File.separator + "temp"; //$NON-NLS-1$
        String fileName = ""; //$NON-NLS-1$
        if (getConnection().getJSONFilePath() != null) {
            fileName = "tempJSONFile" + '.' + "json";
        }
        File temfile = new File(temPath + File.separator + fileName);

        if (!temfile.exists()) {
            try {
                temfile.createNewFile();
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(temfile);
            outStream.write(bytes);
            outStream.close();
        } catch (FileNotFoundException e1) {
            ExceptionHandler.process(e1);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        String tempJSONXsdPath = temfile.getPath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
            tempJSONXsdPath = TalendQuoteUtils.removeQuotes(ConnectionContextHelper
                    .getOriginalValue(contextType, tempJSONXsdPath));
        }

        return tempJSONXsdPath;
    }

    protected ATreeNode getDefaultRootNode(List<ATreeNode> rootNodes) {
        if (rootNodes != null && rootNodes.size() > 0) {
            EList<JSONFileNode> root = getConnection().getRoot();
            JSONFileNode selectedNode = null;
            if (root != null && root.size() > 0) {
                selectedNode = root.get(0);
            } else {
                EList<JSONFileNode> loop = getConnection().getLoop();
                if (loop != null && loop.size() > 0) {
                    selectedNode = loop.get(0);
                }
            }
            if (selectedNode != null) {
                String JSONPath = selectedNode.getJSONPath();
                if (JSONPath != null && JSONPath.length() > 0) {
                    JSONPath = JSONPath.substring(JSONPath.lastIndexOf("/") + 1);
                    for (int i = 0; i < rootNodes.size(); i++) {
                        ATreeNode node = rootNodes.get(i);
                        if (JSONPath.equals(node.getValue())) {
                            return node;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected String getConnectionEncoding() {
        JSONFileConnection conn = getConnection();
        String encoding = conn.getEncoding();
        try {
            if (isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(conn, conn.getContextName());
                encoding = TalendQuoteUtils.removeQuotes(ContextParameterUtils.getOriginalValue(contextType, encoding));
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return encoding;
    }

    @Override
    public void redrawLinkers() {
    }

    @Override
    public void updateConnection() {
    }

    @Override
    public void updateStatus() {
    }

    @Override
    public List<FOXTreeNode> getTreeData() {
        return null;
    }

    @Override
    public void setSelectedText(String label) {
    }

    @Override
    public MetadataTable getMetadataTable() {
        return null;
    }

    @Override
    public TableViewer getSchemaViewer() {
        return null;
    }

    @Override
    public MetadataTable getMetadataOutputTable() {
        return null;
    }

}
