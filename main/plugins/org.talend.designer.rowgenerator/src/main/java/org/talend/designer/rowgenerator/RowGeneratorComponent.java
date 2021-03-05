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
package org.talend.designer.rowgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.genhtml.HTMLDocUtils;
import org.talend.core.model.metadata.ColumnNameChanged;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IComponentDocumentation;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.Problem;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.designer.rowgenerator.external.data.ExternalRowGenTable;
import org.talend.designer.rowgenerator.external.data.ExternalRowGeneratorData;
import org.talend.designer.rowgenerator.shadow.RowGenProcessMain;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;
import org.talend.designer.rowgenerator.utils.problems.ProblemsAnalyser;

/**
 * This is the external component of rowGenerator component. <br/>
 *
 * $Id: RowGeneratorComponent.java,v 1.12 2007/02/02 03:34:13 pub Exp $
 *
 */
public class RowGeneratorComponent extends AbstractExternalNode {

    public static final String COLUMN_NAME = "SCHEMA_COLUMN"; //$NON-NLS-1$

    public static final String ARRAY = "ARRAY"; //$NON-NLS-1$

    private RowGenMain rowGeneratorMain;

    private List<IMetadataTable> metadataListOut;

    private ExternalRowGeneratorData externalData;

    private RowGenProcessMain codeGenMain;

    private String number;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#initialize()
     */
    @Override
    public void initialize() {
        initRowGeneratorMain();
        rowGeneratorMain.loadInitialParamters();
        codeGenMain = new RowGenProcessMain(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#getPersistentData()
     */
    @Override
    public IExternalData getExternalData() {
        return null;
    }

    private void initRowGeneratorMain() {
        rowGeneratorMain = new RowGenMain(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#open(org.eclipse.swt.widgets.Display)
     */
    @Override
    public int open(final Display display) {
        initRowGeneratorMain();
        rowGeneratorMain.createModelFromExternalData(getIODataComponents(), getMetadataList(), externalData, true);
        Shell shell = rowGeneratorMain.createUI(display);
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Throwable e) {
                if (RowGenMain.isStandAloneMode()) {
                    e.printStackTrace();
                } else {
                    ExceptionHandler.process(e);
                }
            }
        }
        if (RowGenMain.isStandAloneMode()) {
            display.dispose();
        }
        rowGeneratorMain.buildExternalData();
        return rowGeneratorMain.getMapperDialogResponse();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#open()
     */
    @Override
    public int open(final Composite parent) {
        initRowGeneratorMain();
        rowGeneratorMain.createModelFromExternalData(getIODataComponents(), getMetadataList(), externalData, true);
        Dialog dialog = rowGeneratorMain.createRowGeneratorDialog(parent.getShell());
        dialog.open();
        return rowGeneratorMain.getMapperDialogResponse();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IExternalComponent#setPersistentData(java.lang.Object)
     */
    @Override
    public void setExternalData(IExternalData externalData) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getGeneratedCode()
     */
    public String getGeneratedCode() {
        try {
            ICodeGeneratorService service = PluginUtils.getCodeGeneratorService();
            return service.createCodeGenerator().generateComponentCode(this, ECodePart.MAIN);
        } catch (SystemException e) {
            ExceptionHandler.process(e);
        }
        return ""; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getMetadataList()
     */
    @Override
    public List<IMetadataTable> getMetadataList() {
        return this.metadataListOut;
    }

    /*
     * add for bug TDI-26458 by fwang at 24 June,2013, need check update for functions.
     */
    private boolean sameMetadataFunctionsAs(IMetadataTable metaTable) {
        List<IMetadataColumn> metaTables = metaTable.getListColumns();
        for (IMetadataColumn metaColum : metaTables) {
            if (!(metaColum instanceof MetadataColumnExt)) {
                break;
            }
            MetadataColumnExt metaColumExt = (MetadataColumnExt) metaColum;
            String functionValue = FunctionManagerExt.getOneColData(metaColumExt);
            for (Map<String, Object> map : getMapList()) {
                if (metaColumExt.getLabel().equals(map.get(COLUMN_NAME)) && !functionValue.equals(map.get(ARRAY))) {
                    return false;
                }
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#setMetadataList(java.util.List)
     */
    @Override
    public void setMetadataList(List<IMetadataTable> metadataTablesOut) {
        boolean needUpdateMetadata = false;
        if (this.metadataListOut == null) {
            needUpdateMetadata = true;
        } else if (this.metadataListOut.size() != metadataTablesOut.size()) {
            needUpdateMetadata = true;
        } else {
            for (IMetadataTable metaTable : this.metadataListOut) {
                for (IMetadataTable externalMetaTable : metadataTablesOut) {
                    if (!metaTable.sameMetadataAs(externalMetaTable, IMetadataColumn.OPTIONS_NONE)
                            || !this.sameMetadataFunctionsAs(metaTable)) {
                        needUpdateMetadata = true;
                        break;
                    }
                }
                if (needUpdateMetadata) {
                    break;
                }
            }
        }
        if (needUpdateMetadata) {
            this.metadataListOut = metadataTablesOut;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#getProblems()
     */
    @Override
    public List<Problem> getProblems() {
        if (rowGeneratorMain == null) {
            initRowGeneratorMain();
        }
        ProblemsAnalyser problemsAnalyser = new ProblemsAnalyser(rowGeneratorMain.getRowGenManager());
        return problemsAnalyser.checkProblems(externalData);
    }

    public RowGenMain getRowGeneratorMain() {
        return this.rowGeneratorMain;
    }

    public RowGenProcessMain getCodeGenMain() {
        if (codeGenMain == null) {
            this.codeGenMain = new RowGenProcessMain(this);
        }
        return this.codeGenMain;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractExternalNode#renameMetadataColumnName(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    protected void renameMetadataColumnName(String conectionName, String oldColumnName, String newColumnName) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#renameInputConnection(java.lang.String, java.lang.String)
     */
    @Override
    public void renameInputConnection(String oldName, String newName) {
        // do nothing, because tRowGenerator has not Input Connections.
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#renameOutputConnection(java.lang.String, java.lang.String)
     */
    @Override
    public void renameOutputConnection(String oldName, String newName) {
        if (oldName == null || newName == null) {
            throw new NullPointerException();
        }
        if (externalData != null) {
            List<ExternalRowGenTable> outputTables = externalData.getOutputTables();
            for (ExternalRowGenTable table : outputTables) {
                if (table.getName().equals(oldName)) {
                    table.setName(newName);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public String getNumber() {
        boolean end = false;
        List<IElementParameter> eps = (List<IElementParameter>) this.getElementParameters();
        if (eps == null) {
            return "10"; //$NON-NLS-1$
        }
        for (int i = 0; i < eps.size() && !end; i++) {
            IElementParameter parameter = eps.get(i);
            if ("__NB_ROWS__".indexOf(parameter.getVariableName()) != -1) { //$NON-NLS-1$
                this.number = (String) parameter.getValue();
                end = true;
            }
        }
        return this.number;
    }

    @SuppressWarnings("unchecked")
    public void setNumber(String number) {
        this.number = number;
        List<IElementParameter> eps = (List<IElementParameter>) this.getElementParameters();
        boolean end = false;
        for (int i = 0; i < eps.size() && !end; i++) {
            IElementParameter parameter = eps.get(i);
            if ("__NB_ROWS__".indexOf(parameter.getVariableName()) != -1) { //$NON-NLS-1$
                parameter.setValue(this.number);
                end = true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMapList() {
        List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
        List<IElementParameter> eps = (List<IElementParameter>) this.getElementParameters();
        if (eps == null) {
            return map;
        }
        boolean end = false;
        for (int i = 0; i < eps.size() && !end; i++) {
            IElementParameter parameter = eps.get(i);
            if (parameter.getFieldType() == EParameterFieldType.TABLE) {
                map = (List<Map<String, Object>>) parameter.getValue();
                end = true;
            }
        }
        return map;
    }

    public void setColumnValue(String columnName, String value) {
        List<Map<String, Object>> map = getMapList();
        boolean isAdd = true;
        for (int i = 0; i < map.size(); i++) {
            Map<String, Object> line = map.get(i);
            if (line.get(COLUMN_NAME).equals(columnName)) {
                line.put(ARRAY, value);
                isAdd = false;
            }
        }
        if (isAdd) {
            Map<String, Object> l = new HashMap<String, Object>();
            l.put(COLUMN_NAME, columnName);
            l.put(ARRAY, value);
            map.add(l);
        }
        reinitColumnValues(map);
    }

    public void reinitColumnValues(List<Map<String, Object>> map) {
        MetadataTable tableExt = (MetadataTable) this.getMetadataList().get(0);
        List<String> columnNames = new ArrayList<String>();
        for (IMetadataColumn column : tableExt.getListColumns()) {
            columnNames.add(column.getLabel());
        }
        if (map.size() > tableExt.getListColumns().size()) {
            for (int i = 0; i < map.size(); i++) {
                Map<String, Object> line = map.get(i);
                if (!columnNames.contains(line.get(COLUMN_NAME))) {
                    map.remove(line);
                }
            }
        }
    }

    public String getColumnValue(IMetadataColumn ext, int index) {
        List<Map<String, Object>> map = getMapList();
        String arrayValue = ""; //$NON-NLS-1$
        if (map.size() > index) {
            arrayValue = (String) map.get(index).get(ARRAY);
            for (int i = 0; i < map.size(); i++) {
                Map<String, Object> line = map.get(i);
                if (ext.getLabel().equals(line.get(COLUMN_NAME))) {
                    arrayValue = (String) line.get(ARRAY);
                    break;
                }
            }
        }
        return arrayValue;
    }

    @SuppressWarnings("unchecked")
    public void setTableElementParameter(List<Map<String, Object>> epsl) {
        List<IElementParameter> eps = (List<IElementParameter>) this.getElementParameters();
        for (int i = 0; i < eps.size(); i++) {
            IElementParameter parameter = eps.get(i);
            if (parameter.getFieldType() == EParameterFieldType.TABLE) {
                List<Map<String, Object>> tableValues = epsl;
                ArrayList newValues = new ArrayList();
                for (Map<String, Object> map : tableValues) {
                    Map<String, Object> newMap = new HashMap<String, Object>();
                    newMap.putAll(map);
                    newValues.add(newMap);
                }
                parameter.setValue(newValues);
                break;
            }
        }
    }

    @Override
    public IODataComponentContainer getIODataComponents() {
        IODataComponentContainer inAndOut = new IODataComponentContainer();

        List<IODataComponent> outputs = inAndOut.getOuputs();
        for (IConnection currentConnection : getOutgoingConnections()) {
            if (currentConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IODataComponent component = new IODataComponent(currentConnection, metadataListOut.get(0));
                outputs.add(component);
            }
        }
        return inAndOut;

    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.model.process.AbstractExternalNode#metadataOutputChanged(org.talend.core.model.components.
     * IODataComponent, java.lang.String)
     */
    @Override
    public void metadataOutputChanged(IODataComponent dataComponent, String connectionToApply) {
        List<Map<String, Object>> oldMap = getMapList();
        List<Map<String, Object>> newMap = new ArrayList<Map<String, Object>>();

        List<Map<String, Object>> notuseMap = new ArrayList<Map<String, Object>>();
        notuseMap.addAll(oldMap);

        List<ColumnNameChanged> newColumns = dataComponent.getNewMetadataColumns();
        for (ColumnNameChanged changed : newColumns) {
            if ("".equals(changed.getOldName())) { //$NON-NLS-1$
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put(COLUMN_NAME, changed.getNewName());
                newMap.add(map2);
            }
        }

        List<ColumnNameChanged> removeColumns = dataComponent.getRemoveMetadataColumns();
        for (ColumnNameChanged changed : removeColumns) {
            if ("".equals(changed.getNewName())) { //$NON-NLS-1$
                for (Map<String, Object> map : oldMap) {
                    if (changed.getOldName().equals(map.get(COLUMN_NAME))) {
                        notuseMap.remove(map);
                        break;
                    }
                }
            }
        }

        List<ColumnNameChanged> columnNameChanged = dataComponent.getColumnNameChanged();
        for (ColumnNameChanged changed : columnNameChanged) {
            for (Map<String, Object> map : oldMap) {
                if (changed.getOldName().equals(map.get(COLUMN_NAME))) {
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put(COLUMN_NAME, changed.getNewName());
                    map2.put(ARRAY, map.get(ARRAY));
                    newMap.add(map2);
                    notuseMap.remove(map);
                    break;
                }
            }
        }
        newMap.addAll(notuseMap);
        // for (IMetadataColumn column : metadataTable.getListColumns()) {
        // for (Map<String, Object> map : oldMap) {
        // if (column.getLabel().equals(map.get(COLUMN_NAME))) {
        // Map<String, Object> map2 = new HashMap<String, Object>();
        // map2.put(COLUMN_NAME, column.getLabel());
        // map2.put(ARRAY, map.get(ARRAY));
        // newMap.add(map2);
        // }
        // }
        // }
        setTableElementParameter(newMap);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#getComponentDocumentation(java.lang.String, java.lang.String)
     */
    @Override
    public IComponentDocumentation getComponentDocumentation(String componentName, String tempFolderPath) {
        RowGeneratorComponentDocumentation componentDocumentation = new RowGeneratorComponentDocumentation();
        componentDocumentation.setComponentName(componentName);
        componentDocumentation.setTempFolderPath(tempFolderPath);
        componentDocumentation.setPreviewPicPath(HTMLDocUtils.getPreviewPicPath(this));
        componentDocumentation.setExternalNode(getExternalNode());

        // Added parameters, preview and functions value.
        for (IMetadataTable table : this.metadataListOut) {
            try {
                this.convert(table);
            } catch (Exception e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }
        }

        componentDocumentation.setMetadataListOut(this.metadataListOut);

        return componentDocumentation;
    }

    /**
     * Coverts <code>MetadataColumn</code> to <code>MetadataColumnExt</code>
     *
     * @param metadataTable
     * @return
     */
    private void convert(IMetadataTable metadataTable) {
        List<IMetadataColumn> exts = new ArrayList<IMetadataColumn>();
        for (int j = 0; j < metadataTable.getListColumns().size(); j++) {
            IMetadataColumn column = metadataTable.getListColumns().get(j);
            if (column instanceof MetadataColumn) {
                MetadataColumnExt ext = new MetadataColumnExt((MetadataColumn) column);

                String columnValue = this.getColumnValue(ext, j);

                if (columnValue != null && columnValue.length() > 0)

                {
                    columnValue = columnValue.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    columnValue = columnValue.substring(columnValue.lastIndexOf(".") + 1); //$NON-NLS-1$
                    Function function = new Function();
                    function.setName(columnValue);
                    ext.setFunction(function);
                }
                exts.add(ext);
            }
        }
        metadataTable.setListColumns(exts);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#getTMapExternalData()
     */
    @Override
    public IExternalData getTMapExternalData() {
        // TODO Auto-generated method stub
        return null;
    }

}
