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
package org.talend.designer.rowgenerator.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.designer.rowgenerator.external.data.ExternalRowGeneratorUiProperties;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.ui.RowGeneratorUI;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;
import org.talend.designer.rowgenerator.ui.editor.MetadataToolbarEditorViewExt;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: UIManager.java,v 1.8 2007/01/31 10:31:05 pub Exp $
 *
 */
public class UIManager {

    private RowGeneratorUI generatorUI;

    private int rowGenResponse = SWT.NONE;

    private final RowGeneratorManager rgManager;

    private boolean shiftPressed;

    private boolean ctrlPressed;

    private ExternalRowGeneratorUiProperties uiProperties;

    int currentDragDetail;

    /**
     * amaumont UIManager constructor comment.
     *
     * @param tableManager
     * @param manager
     */
    public UIManager(RowGeneratorManager rgManager) {
        this.rgManager = rgManager;
    }

    public ExternalRowGeneratorUiProperties getUiProperties() {
        if (this.uiProperties == null) {
            this.uiProperties = new ExternalRowGeneratorUiProperties();
        }
        return this.uiProperties;
    }

    public void setUiProperties(ExternalRowGeneratorUiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    public RowGeneratorUI getGeneratorUI() {
        return this.generatorUI;
    }

    public void setGeneratorUI(RowGeneratorUI generatorUI) {
        this.generatorUI = generatorUI;
    }

    public int getRowGenResponse() {
        return this.rowGenResponse;
    }

    public void setRowGenResponse(int rowGenResponse) {
        this.rowGenResponse = rowGenResponse;
    }

    public boolean isCtrlPressed() {
        return this.ctrlPressed;
    }

    public void setCtrlPressed(boolean ctrlPressed) {
        this.ctrlPressed = ctrlPressed;
    }

    public boolean isShiftPressed() {
        return this.shiftPressed;
    }

    public void setShiftPressed(boolean shiftPressed) {
        this.shiftPressed = shiftPressed;
    }

    /**
     * yzhang Comment method "closeRowGenerator".
     *
     * @param response
     */
    public void closeRowGenerator(int response, boolean fromDialog) {
        setRowGenResponse(response);
        Composite parent = generatorUI.getRowGenUIParent();
        saveCurrentUIProperties();
        MetadataTable table = (MetadataTable) rgManager.getRowGeneratorComponent().getMetadataList().get(0);
        boolean hasColumns = (table != null) && (table.getListColumns() != null && (table.getListColumns().size() != 0));
        List<Map<String, Object>> originalColumnDataList = this.getRowGenManager().getRowGeneratorComponent().getMapList();
        List<Map<String, Object>> currentColumnDataList = getCurrentColumnData();
        boolean containsAll1 = originalColumnDataList.containsAll(currentColumnDataList);
        boolean containsAll2 = currentColumnDataList.containsAll(originalColumnDataList);
        boolean containsAll = containsAll1 && containsAll2;
        if (hasColumns && !containsAll && response == SWT.CANCEL) {
            boolean isNotSaveSetting = MessageDialog.openQuestion(parent.getShell(),
                    Messages.getString("UIManager.MessageBox.title"), Messages.getString("UIManager.MessageBox.Content")); //$NON-NLS-1$ //$NON-NLS-2$
            if (!isNotSaveSetting) {
                response = SWT.NONE;
                setRowGenResponse(response);
            }
            return;
        }
        if (response == SWT.OK) {
            saveAllData(currentColumnDataList);
        }
        if (parent instanceof Shell && !fromDialog) {
            ((Shell) parent).close();
        }
    }

    /**
     * ftang Comment method "saveAllData".
     */
    private void saveAllData(List<Map<String, Object>> map) {
        rgManager.getRowGeneratorComponent().setTableElementParameter(map);
        rgManager.getRowGeneratorComponent().setNumber(generatorUI.getDataTableView().getExtendedToolbar().getNumRows());
    }

    /**
     * ftang Comment method "saveAllData".
     */
    private List<Map<String, Object>> getCurrentColumnData() {
        List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
        MetadataTable table = (MetadataTable) rgManager.getRowGeneratorComponent().getMetadataList().get(0);
        convert(rgManager.getRowGeneratorComponent(), table, generatorUI.getFunctionManager());
        for (IMetadataColumn col : table.getListColumns()) {
            MetadataColumnExt ext = (MetadataColumnExt) col;
            Map<String, Object> value = new HashMap<String, Object>();
            value.put(RowGeneratorComponent.COLUMN_NAME, ext.getLabel());
            value.put(RowGeneratorComponent.ARRAY, FunctionManagerExt.getOneColData(ext));
            map.add(value);
        }
        return map;
    }

    /**
     * qzhang Comment method "saveAllData".
     */
    private void reductAllData() {
        List<Map<String, Object>> eps = this.getRowGenManager().getOrginEP();
        this.getRowGenManager().getRowGeneratorComponent().setTableElementParameter(eps);
        this.getRowGenManager().getRowGeneratorComponent().setNumber(this.getRowGenManager().getOrginNumber());
    }

    /**
     * qzhang Comment method "saveCurrentUIProperties".
     */
    private void saveCurrentUIProperties() {
        ExternalRowGeneratorUiProperties.setWeightsMainSashForm(generatorUI.getMainSashForm().getWeights());
        ExternalRowGeneratorUiProperties.setWeightsDatasFlowViewSashForm(generatorUI.getDatasFlowViewSashForm().getWeights());
        ExternalRowGeneratorUiProperties.setShellMaximized(generatorUI.getRowGenUIParent().getShell().getMaximized());
        if (!ExternalRowGeneratorUiProperties.isShellMaximized()) {
            ExternalRowGeneratorUiProperties.setBoundsRowGen(generatorUI.getRowGenUIParent().getShell().getBounds());
        }
        ExternalRowGeneratorUiProperties.setShowColumnsList(getShowColumnsList());
    }

    /**
     * qzhang Comment method "getShowColumnsList".
     *
     * @return
     */
    private String[] getShowColumnsList() {
        List<String> cols = new ArrayList<String>();
        MetadataToolbarEditorViewExt editorViewExt = generatorUI.getDataTableView().getExtendedToolbar();
        MenuItem[] items = editorViewExt.getColumnsListmenu().getItems();
        for (MenuItem item : items) {
            if (!item.getSelection()) {
                cols.add(item.getData().toString());
            }
        }
        return cols.toArray(new String[cols.size()]);
    }

    public RowGeneratorManager getRowGenManager() {
        return this.rgManager;
    }

    @SuppressWarnings("unchecked")
    protected void saveOneColData(MetadataColumnExt bean) {
        String newValue2 = FunctionManagerExt.getOneColData(bean);
        if (rgManager.getRowGeneratorComponent() != null && newValue2 != null) {
            rgManager.getRowGeneratorComponent().setColumnValue(bean.getLabel(), newValue2);
        }
    }

    public static boolean isJavaProject() {
        return FunctionManagerExt.isJavaProject();
    }

    /**
     * qzhang Comment method "convert".
     *
     * @param outputMetaTable2
     * @return TODO
     */
    public void convert(RowGeneratorComponent externalNode, IMetadataTable outputMetaTable2, FunctionManagerExt functionManager) {
        List<IMetadataColumn> exts = new ArrayList<IMetadataColumn>();
        for (int j = 0; j < outputMetaTable2.getListColumns().size(); j++) {
            IMetadataColumn column = outputMetaTable2.getListColumns().get(j);
            MetadataColumnExt ext = null;
            if (column instanceof MetadataColumnExt) {
                ext = (MetadataColumnExt) column.clone();
            } else if (column instanceof MetadataColumn) {
                ext = new MetadataColumnExt((MetadataColumn) column);
            }
            if (ext != null) {
                List<Function> funs = functionManager.getFunctionsByType(ext.getTalendType());
                ext.setArrayFunctions(functionManager.getFunctionArrays(funs));
                if (!funs.isEmpty()) {
                    Function funtion = functionManager.getFunctionFromColumn(ext);
                    if (funtion == null) {
                        funtion = functionManager.getFuntionFromArray(ext, externalNode, j);
                    }
                    ext.setFunction(funtion);
                }
                exts.add(ext);
            }
        }
        outputMetaTable2.setListColumns(exts);
    }
}
