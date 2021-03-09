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
package org.talend.designer.xmlmap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.cursor.CursorHelper;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.BlockCode;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.HashConfiguration;
import org.talend.core.model.process.HashableColumn;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IHashConfiguration;
import org.talend.core.model.process.IHashableColumn;
import org.talend.core.model.process.IHashableInputConnections;
import org.talend.core.model.process.ILookupMode;
import org.talend.core.model.process.IMatchingMode;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.node.MapperExternalNode;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.xmlmap.generation.GenerationManager;
import org.talend.designer.xmlmap.generation.GenerationManagerFactory;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.model.tree.XML_MAP_LOOKUP_MODE;
import org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager;
import org.talend.designer.xmlmap.util.MapDataHelper;
import org.talend.designer.xmlmap.util.XMLMapperHelper;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapComponent extends MapperExternalNode implements IHashableInputConnections {

    private AbstractExternalData emfMapData;

    private MapperMain mapperMain;

    private GenerationManager generationManager;

    private MapDataHelper mapperHelper;

    private XmlMapExpressionManager expressionManager;

    public XmlMapComponent() {
        mapperHelper = new MapDataHelper();
        expressionManager = new XmlMapExpressionManager();
    }

    @Override
    public boolean isGeneratedAsVirtualComponent() {
        return XMLMapperHelper.isGeneratedAsVirtualComponent(this);
    }

    @Override
    public int open(Display display) {

        // TimeMeasure.start("Total open");
        // TimeMeasure.display = false;

        Shell parentShell = display.getActiveShell();
        mapperMain = new MapperMain(this);

        if (parentShell != null) {
            CursorHelper.changeCursor(parentShell, SWT.CURSOR_WAIT);
        }

        Shell shell = null;
        try {
            shell = mapperMain.createUI(display);

        } finally {
            parentShell.setCursor(null);
        }
        while (shell != null && !shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }

        return mapperMain.getMapperDialogResponse();
    }

    @Override
    public void initialize() {
    }

    @Override
    public int open(Composite parent) {
        return open(parent.getDisplay());
    }

    @Override
    public void setExternalData(IExternalData persistentData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void renameInputConnection(String oldName, String newName) {
        XmlMapData externalEmfData = (XmlMapData) getExternalEmfData();
        for (InputXmlTree inputTree : externalEmfData.getInputTrees()) {
            if (inputTree.getName() != null && inputTree.getName().equals(oldName) && !oldName.equals(newName)) {
                inputTree.setName(newName);
                XmlMapUtil.updateXPathAndExpression(externalEmfData, expressionManager, inputTree.getNodes(),
                        inputTree.getName(), 1);
            }
        }
    }

    @Override
    public void renameOutputConnection(String oldName, String newName) {
        XmlMapData externalEmfData = (XmlMapData) getExternalEmfData();
        for (OutputXmlTree outputTree : externalEmfData.getOutputTrees()) {
            if (outputTree.getName() != null && outputTree.getName().equals(oldName) && !oldName.equals(newName)) {
                outputTree.setName(newName);
                XmlMapUtil.updateXPathAndExpression(externalEmfData, expressionManager, outputTree.getNodes(),
                        outputTree.getName(), 1);
            }
        }
        // Fix for TDI-31214,metadata Label maybe not useful except some UI, but still change to be the same as table
        // name here
        if (getOriginalNode() == null) {
            return;
        }
        for (IMetadataTable table : getOriginalNode().getMetadataList()) {
            boolean changeLabel = oldName.equals(table.getLabel())
                    || (table.getLabel() != null && !table.getLabel().equals(table.getTableName()) && oldName.equals(table
                            .getTableName()));
            if (changeLabel) {
                table.setLabel(newName);
            }
        }
    }

    @Override
    public IComponentDocumentation getComponentDocumentation(String componentName, String tempFolderPath) {
        MapperComponentDocumentation componentDocumentation = new MapperComponentDocumentation();
        componentDocumentation.setComponentName(componentName);
        componentDocumentation.setTempFolderPath(tempFolderPath);
        componentDocumentation.setExternalData((XmlMapData) this.emfMapData);
        componentDocumentation.setExternalNode(getExternalNode());
        return componentDocumentation;
    }

    @Override
    public IExternalData getTMapExternalData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void renameMetadataColumnName(String conectionName, String oldColumnName, String newColumnName) {
        List<AbstractInOutTree> abstractTrees = new ArrayList<AbstractInOutTree>();
        XmlMapData externalEmfData = (XmlMapData) getExternalEmfData();
        abstractTrees.addAll(externalEmfData.getInputTrees());
        abstractTrees.addAll(externalEmfData.getOutputTrees());
        for (AbstractInOutTree abstractTree : abstractTrees) {
            if (abstractTree.getName() != null && abstractTree.getName().equals(conectionName)) {
                List<TreeNode> children = new ArrayList<TreeNode>();
                if (abstractTree instanceof InputXmlTree) {
                    children.addAll(((InputXmlTree) abstractTree).getNodes());
                } else {
                    children.addAll(((OutputXmlTree) abstractTree).getNodes());
                }
                for (TreeNode treeNode : children) {
                    if (treeNode.getName() != null && treeNode.getName().equals(oldColumnName)
                            && !oldColumnName.equals(newColumnName)) {
                        treeNode.setName(newColumnName);
                        XmlMapUtil.updateXPathAndExpression(externalEmfData, expressionManager, treeNode, newColumnName, 2, true);
                    }
                }
            }
        }

    }

    @Override
    public void buildExternalData(AbstractExternalData abstractData) {
        if (abstractData instanceof XmlMapData) {
            this.emfMapData = abstractData;
        }
    }

    @Override
    public AbstractExternalData getExternalEmfData() {
        if (this.emfMapData == null) {
            this.emfMapData = XmlmapFactory.eINSTANCE.createXmlMapData();
        }

        return this.emfMapData;
    }

    @Override
    public void setExternalEmfData(AbstractExternalData emfMapData) {
        this.emfMapData = emfMapData;
    }

    @Override
    public IHashConfiguration getHashConfiguration(String connectionName) {

        IHashConfiguration hashConfigurationForMapper = null;
        XmlMapData externalData = (XmlMapData) getExternalEmfData();
        List<InputXmlTree> inputTables = externalData.getInputTrees();
        List<IHashableColumn> hashableColumns = new ArrayList<IHashableColumn>();
        for (InputXmlTree inputTable : inputTables) {
            if (inputTable.getName().equals(connectionName)) {

                List<TreeNode> metadataTableEntries = inputTable.getNodes();
                if (metadataTableEntries != null) {
                    int metadataTableEntriesListSize = metadataTableEntries.size();
                    for (int i = 0; i < metadataTableEntriesListSize; i++) {
                        TreeNode entry = metadataTableEntries.get(i);
                        if (entry.getExpression() != null && !entry.getExpression().trim().equals("")) { //$NON-NLS-1$
                            hashableColumns.add(new HashableColumn(entry.getName(), i));
                        }
                    }
                }

                IMatchingMode matchingMode = MATCHING_MODE.parse(inputTable.getMatchingMode());
                if (matchingMode == null) {
                    matchingMode = MATCHING_MODE.UNIQUE_MATCH;
                }

                ILookupMode lookupMode = org.talend.designer.xmlmap.model.tree.LOOKUP_MODE.parse(inputTable.getLookupMode());
                if (lookupMode == null) {
                    lookupMode = org.talend.designer.xmlmap.model.tree.LOOKUP_MODE.LOAD_ONCE;
                }

                IElementParameter tempFolderElem = getElementParameter("TEMPORARY_DATA_DIRECTORY"); //$NON-NLS-1$
                String tempFolder = null;
                if (tempFolderElem != null) {
                    tempFolder = (String) tempFolderElem.getValue();
                }
                if (("").equals(tempFolder)) {
                    tempFolder = (String) this.getProcess().getElementParameter("COMP_DEFAULT_FILE_DIR").getValue() + "/temp"; //$NON-NLS-1$ //$NON-NLS-2$
                    tempFolder = TalendTextUtils.addQuotes(tempFolder);
                }

                IElementParameter rowsBufferSizeElem = getElementParameter("ROWS_BUFFER_SIZE"); //$NON-NLS-1$
                String rowsBufferSize = null;
                if (rowsBufferSizeElem != null) {
                    rowsBufferSize = (String) rowsBufferSizeElem.getValue();
                }
                hashConfigurationForMapper = new HashConfiguration(hashableColumns, matchingMode, inputTable.isPersistent(),
                        tempFolder, rowsBufferSize);
                break;
            }
        }

        return hashConfigurationForMapper;
    }

    public GenerationManager initGenerationManager() {
        this.generationManager = GenerationManagerFactory.getInstance().getGenerationManager();
        return this.generationManager;
    }

    public GenerationManager getGenerationManager() {
        return this.generationManager;
    }

    @Override
    public List<BlockCode> getBlocksCodeToClose() {
        if (generationManager == null) {
            throw new IllegalStateException();
        }
        return this.generationManager.getBlocksCodeToClose();
    }

    @Override
    public List<String> checkNeededRoutines(List<String> possibleRoutines, String additionalString) {
        List<String> routinesToAdd = new ArrayList<String>();
        XmlMapData xmlMapData = (XmlMapData) getExternalEmfData();
        for (String routine : possibleRoutines) {
            List<OutputXmlTree> listOutput = xmlMapData.getOutputTrees();
            for (OutputXmlTree outTable : listOutput) {
                List<OutputTreeNode> listOutEntry = outTable.getNodes();
                if (listOutEntry != null && !listOutEntry.isEmpty()) {
                    for (OutputTreeNode outEntry : listOutEntry) {
                        String expression = outEntry.getExpression();
                        if (expression != null && !routinesToAdd.contains(routine)
                                && expression.contains(routine + additionalString)) {
                            routinesToAdd.add(routine);
                        }
                    }
                }
                String filter = outTable.getExpressionFilter();
                if (filter != null && !routinesToAdd.contains(routine) && filter.contains(routine + additionalString)) {
                    routinesToAdd.add(routine);
                }
            }
            List<InputXmlTree> listInput = xmlMapData.getInputTrees();
            for (InputXmlTree inputTable : listInput) {
                List<TreeNode> listInEntry = inputTable.getNodes();
                if (listInEntry != null && !listInEntry.isEmpty()) {
                    for (TreeNode inEntry : listInEntry) {
                        String expression = inEntry.getExpression();
                        if (expression != null && !routinesToAdd.contains(routine)
                                && expression.contains(routine + additionalString)) {
                            routinesToAdd.add(routine);
                        }
                    }
                }
                String filter = inputTable.getExpressionFilter();
                if (filter != null && !routinesToAdd.contains(routine) && filter.contains(routine + additionalString)) {
                    routinesToAdd.add(routine);
                }
            }
            List<VarTable> listVar = xmlMapData.getVarTables();
            for (VarTable varTable : listVar) {
                List<VarNode> listVarEntry = varTable.getNodes();
                if (listVarEntry != null && !listVarEntry.isEmpty()) {
                    for (VarNode varEntry : listVarEntry) {
                        String expression = varEntry.getExpression();
                        if (expression != null && !routinesToAdd.contains(routine)
                                && expression.contains(routine + additionalString)) {
                            routinesToAdd.add(routine);
                        }
                    }
                }
            }
        }
        return routinesToAdd;

    }

    @Override
    public List<Problem> getProblems() {
        initMapperMain();
        return mapperMain.getMapperManager().getProblemsAnalyser().getProblems();
    }

    private void initMapperMain() {
        if (mapperMain == null) {
            mapperMain = new MapperMain(this);
        }
    }

    @Override
    public void connectionStatusChanged(EConnectionType newValue, String connectionToApply) {
        XmlMapData externalEmfData = (XmlMapData) getExternalEmfData();
        InputXmlTree mainTable = null;
        InputXmlTree oldmainTable = null;
        for (InputXmlTree inputTree : externalEmfData.getInputTrees()) {
            if (!inputTree.isLookup()) {
                oldmainTable = inputTree;
                break;
            }
        }
        for (InputXmlTree inputTree : externalEmfData.getInputTrees()) {
            if (inputTree.getName() != null && inputTree.getName().equals(connectionToApply)) {
                boolean value = EConnectionType.FLOW_MAIN != newValue;
                inputTree.setLookup(value);
                if (!value) {
                    mainTable = inputTree;
                }
            } else if (EConnectionType.FLOW_MAIN == newValue) {
                inputTree.setLookup(true);
            }
        }
        // put the main table to the first place
        if (mainTable != null) {
            externalEmfData.getInputTrees().remove(mainTable);
            externalEmfData.getInputTrees().add(0, mainTable);
        }

        if (oldmainTable != null && oldmainTable != mainTable) {
            boolean checkMultiLoopsStatus = XmlMapUtil.checkMultiLoopsStatus(oldmainTable);
            if (checkMultiLoopsStatus) {
                MessageDialog.openInformation(null, "Information",
                        "Multiple loops in previous main table will be lost in tXmlMap");
                // multipleloops status of the new main table will be false ,clean the InputLoopNodesTables for
                // output will be ok
                XmlMapUtil.removeloopInOutputTree(externalEmfData, mainTable, new ArrayList<TreeNode>());
                // clean multipleloops and only keep the first loop of doc
                for (TreeNode treeNode : oldmainTable.getNodes()) {
                    if (XmlMapUtil.DOCUMENT.equals(treeNode.getType())) {
                        List<TreeNode> oldLoops = new ArrayList<TreeNode>();
                        XmlMapUtil.getChildLoops(oldLoops, treeNode.getChildren());
                        if (oldLoops.size() > 1) {
                            for (int i = 1; i < oldLoops.size(); i++) {
                                oldLoops.get(i).setLoop(false);
                            }
                        }
                    }
                }
                oldmainTable.setMultiLoops(false);
            }
        }

        initMapperMain();
        mapperMain.getMapperManager().getProblemsAnalyser().checkProblems();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractNode#removeInput(org.talend.core.model.process.IConnection)
     */
    @Override
    public void removeInput(IConnection connection) {
        XmlMapData externalEmfData = (XmlMapData) getExternalEmfData();
        InputXmlTree toRemove = null;
        for (InputXmlTree inputTree : externalEmfData.getInputTrees()) {
            if (inputTree.getName() != null && inputTree.getName().equals(connection.getUniqueName())) {
                toRemove = inputTree;
                break;
            }
        }
        if (toRemove != null) {
            if (!toRemove.isLookup() && toRemove.isMultiLoops()) {
                // clean InputLoopTable in putput tree
                XmlMapUtil.removeloopInOutputTree(externalEmfData, null, null);
            }
            for (TreeNode treeNode : toRemove.getNodes()) {
                XmlMapUtil.detachNodeConnections(treeNode, externalEmfData, true);
            }
            XmlMapUtil.detachFilterSource(toRemove, externalEmfData);
            externalEmfData.getInputTrees().remove(toRemove);
        }
    }

    @Override
    public boolean isRunRefSubProcessAtStart(String connectionName) {
        XmlMapData externalEmfData = (XmlMapData) getExternalEmfData();
        List<InputXmlTree> inputTrees = new ArrayList<InputXmlTree>(externalEmfData.getInputTrees());
        for (InputXmlTree table : inputTrees) {
            if (table.getName().equals(connectionName)) {
                String lookupMode = table.getLookupMode();
                if (XML_MAP_LOOKUP_MODE.RELOAD.name().equals(lookupMode)
                        || XML_MAP_LOOKUP_MODE.CACHE_OR_RELOAD.name().equals(lookupMode)) {
                    return false;
                }
            }
        }
        return true;
    }

    public XmlMapExpressionManager getExpressionManager() {
        return expressionManager;
    }

    @Override
    public boolean isReadOnly() {
        if (this.getOriginalNode().getJobletNode() != null) {
            return this.getOriginalNode().getJobletNode().isReadOnly() || this.getOriginalNode().isReadOnly();
        }

        return super.isReadOnly() || this.getProcess().isReadOnly() || this.getOriginalNode().isReadOnly();
    }
}
