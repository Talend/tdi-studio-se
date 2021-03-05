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
package org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.TableController;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: MetadataTableEditor.java 801 2006-11-30 16:28:36Z amaumont $
 *
 * @param <B>
 */
public class PropertiesTableEditorModel<B> extends ExtendedTableModel<B> {

    private IElement element;

    private IElementParameter elemParameter;

    private List<B> beforeChangeBeansList = new ArrayList<B>();

    private IProcess process;

    private boolean dynamicData;

    // added for tAggregate
    private IElementParameter relatedParameter;

    /**
     * DOC amaumont PropertiesTableEditorModel constructor comment.
     */
    public PropertiesTableEditorModel() {
        super();
    }

    public PropertiesTableEditorModel(String titleName) {
        super(titleName);
    }

    public void setData(IElement element, IElementParameter elemParameter, IProcess process) {
        this.element = element;
        this.process = process;
        this.elemParameter = elemParameter;
        registerDataList((List<B>) elemParameter.getValue());

        // for tAggregateRow
        if (element instanceof Node) {
            IComponent component = ((Node) getElement()).getComponent();
            if ("tAggregateRow".equals(component.getName())) {
                String toFind = EParameterName.GROUPBYS.name();
                Node node = (Node) getElement();
                if (EParameterName.GROUPBYS.name().equals(getElemParameter().getName())) {
                    toFind = EParameterName.OPERATIONS.name();
                }
                relatedParameter = node.getElementParameter(toFind);
            }
        }

    }

    public String getTitleName() {
        return super.getName();
    }

    public B createNewEntry() {
        return (B) TableController.createNewLine(elemParameter);
    }

    /**
     * Getter for dynamicData.
     *
     * @return the dynamicData
     */
    public boolean isDynamicData() {
        return dynamicData;
    }

    /**
     * Getter for element.
     *
     * @return the element
     */
    public IElement getElement() {
        return this.element;
    }

    /**
     * Getter for elemParameter.
     *
     * @return the elemParameter
     */
    public IElementParameter getElemParameter() {
        return this.elemParameter;
    }

    /**
     * Getter for items.
     *
     * @return the items
     */
    public String[] getItems() {
        return elemParameter.getListItemsDisplayCodeName();
    }

    /**
     * Getter for itemsNotShowIf.
     *
     * @return the itemsNotShowIf
     */
    public String[] getItemsNotShowIf() {
        return elemParameter.getListItemsNotShowIf();
    }

    /**
     * Getter for itemsShowIf.
     *
     * @return the itemsShowIf
     */
    public String[] getItemsShowIf() {
        return elemParameter.getListItemsShowIf();
    }

    /**
     * Getter for itemsValue.
     *
     * @return the itemsValue
     */
    public Object[] getItemsValue() {
        return elemParameter.getListItemsValue();
    }

    /**
     * Getter for process.
     *
     * @return the process
     */
    public IProcess getProcess() {
        return this.process;
    }

    /**
     * Getter for titles.
     *
     * @return the titles
     */
    public String[] getTitles() {
        return elemParameter.getListItemsDisplayName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.extended.table.ExtendedTableModel#remove(java.lang.Object)
     */
    @Override
    public boolean remove(B bean) {
        initBeforeChangeBeansList();
        boolean result = super.remove(bean);
        refreshMR();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.extended.table.ExtendedTableModel#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<B> c) {
        boolean cancel = false;
        String schemaType = null;
        for (Object object : elemParameter.getListItemsValue()) {
            if (object instanceof ElementParameter) {
                ElementParameter param = (ElementParameter) object;
                if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                        || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                    schemaType = param.getName();
                    cancel = !MessageDialog.openQuestion(this.getTableViewer().getTable().getShell(),
                            Messages.getString("PropertiesTableEditorModel.removeSchema"), //$NON-NLS-1$
                            Messages.getString("PropertiesTableEditorModel.popWindow")); //$NON-NLS-1$
                }
            }
        }

        if (cancel) {
            return false;
        }
        if (schemaType != null) {
            INode node = ((INode) elemParameter.getElement());
            List<IMetadataTable> metadatasToRemove = new ArrayList<IMetadataTable>();
            for (Map<String, Object> line : (List<Map<String, Object>>) c) {
                String schemaName = (String) line.get(schemaType);
                IMetadataTable metadata = MetadataToolHelper.getMetadataTableFromNodeTableName(node, schemaName);
                if (metadata != null) {
                    metadatasToRemove.add(metadata);
                    IDesignerCoreService service = GlobalServiceRegister.getDefault().getService(
                            IDesignerCoreService.class);
                    if (service != null) {
                        service.removeConnection(node, metadata.getTableName());
                    }
                }
            }
            node.getMetadataList().removeAll(metadatasToRemove);
        }
        initBeforeChangeBeansList();
        boolean result = super.removeAll(c);
        refreshMR();
        return result;
    }

    public boolean isAggregateRow() {
        if (relatedParameter != null) {
            return true;
        }
        return false;
    }

    /**
     *
     * DOC Added for featerue TDI-7284
     *
     * @return
     */
    public boolean isButtonEnabled() {
        if (element instanceof Node && relatedParameter != null) {
            Node node = (Node) element;
            if (node.getMetadataList() != null && node.getMetadataList().get(0).getListColumns() != null) {
                List<IMetadataColumn> columns = node.getMetadataList().get(0).getListColumns();
                boolean foundNotExistColumn = false;
                for (IMetadataColumn column : columns) {
                    if (!exist(column.getLabel())) {
                        foundNotExistColumn = true;
                        break;
                    }
                }
                if (foundNotExistColumn) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean exist(String columnName) {
        if (element instanceof Node && relatedParameter != null && columnName != null) {
            if (relatedParameter.getValue() instanceof List) {
                for (Object obj : (List) relatedParameter.getValue()) {
                    if (obj instanceof Map) {
                        Map childElement = (Map) obj;
                        if (columnName.equals(childElement.get(relatedParameter.getListItemsDisplayCodeName()[0]))) {
                            return true;
                        }
                    }
                }
            }
            if (getElemParameter().getValue() instanceof List) {
                for (Object obj : (List) getElemParameter().getValue()) {
                    if (obj instanceof Map) {
                        Map childElement = (Map) obj;
                        if (columnName.equals(childElement.get(getElemParameter().getListItemsDisplayCodeName()[0]))) {
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }

    public void addModifiedBeanListenerForAggregateComponent() {
        if (isAggregateRow()) {
            this.addModifiedBeanListener(modifyListener);
        }
    }

    IModifiedBeanListener modifyListener = new IModifiedBeanListener() {

        @Override
        public void handleEvent(ModifiedBeanEvent event) {
            Node node = (Node) getElement();
            node.checkAndRefreshNode();
        }

    };

    @Override
    public void addAll(final Integer index, List<B> beans, boolean fireBefore, boolean fireAfter) {
        initBeforeChangeBeansList();
        super.addAll(index, beans, fireBefore, fireAfter);
        refreshMR();
    }

    @Override
    public void addAll(List<Integer> indicesWhereAdd, List<B> beans) {
        initBeforeChangeBeansList();
        super.addAll(indicesWhereAdd, beans);
        refreshMR();
    }

    @Override
    public B remove(int index) {
        initBeforeChangeBeansList();
        B b = super.remove(index);
        refreshMR();
        return b;
    }

    @Override
    public List<B> remove(int[] indexArray) {
        initBeforeChangeBeansList();
        List<B> list = super.remove(indexArray);
        return list;
    }

    private void refreshMR() {
        if (element instanceof Node) {
            Node node = (Node) element;
            if (!node.isMapReduce()) {
                return;
            }
            if (this.elemParameter == null) {
                return;
            }
            if (!this.elemParameter.getName().equals("MAP_ONLY")
                    && !this.elemParameter.getName().equals(EParameterName.GROUPBYS.getName())) {
                return;
            }
            ((IProcess2) node.getProcess()).getGeneratingNodes();
            ((IProcess2) node.getProcess()).setMRData();
            node.refreshNodeContainer();
        }
    }

    private void initBeforeChangeBeansList() {
        beforeChangeBeansList.clear();
        beforeChangeBeansList.addAll(getBeansList());
    }

    public List<B> getBeforeChangeBeansList() {
        return this.beforeChangeBeansList;
    }
}
