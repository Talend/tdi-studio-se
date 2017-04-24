package org.talend.designer.core.ui.celleditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;

public class PatternPropertyCellEditor extends CellEditor {

    private AbstractDataTableEditorView tableEditorView;

    private CCombo combo;

    private INode node;
    
    private List<String> stringToDisplay;

    public PatternPropertyCellEditor(Composite parent) {
        super(parent);
    }

    public PatternPropertyCellEditor(Composite parent, int style) {
        super(parent, style);
    }

    public PatternPropertyCellEditor(Composite parent, IElement element) {
        super(parent, SWT.BORDER);
        this.node = (INode) element;
    }

    public void setTableEditorView(AbstractDataTableEditorView tableEditorView) {
        this.tableEditorView = tableEditorView;
    }

    private TableViewer getTableViewer() {
        if (this.tableEditorView != null) {
            AbstractExtendedTableViewer extendedTableViewer = this.tableEditorView.getExtendedTableViewer();
            if (extendedTableViewer != null) {
                TableViewerCreator tableViewerCreator = extendedTableViewer.getTableViewerCreator();
                if (tableViewerCreator != null) {
                    return tableViewerCreator.getTableViewer();
                }
            }
        }
        return null;
    }
    
    @Override
    protected Control createControl(Composite parent) {
        combo = new CCombo(parent, SWT.BORDER);
        stringToDisplay = new ArrayList<String>();
        stringToDisplay.add(EmfComponent.BUILTIN);
        stringToDisplay.add(EmfComponent.REPOSITORY);
        combo.setItems(stringToDisplay.toArray(new String[0]));
        combo.setEditable(false);
        combo.setEnabled(true);
        combo.select(0);

        combo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing.
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectionIndex = combo.getSelectionIndex();
                if (selectionIndex == 0) {
                    //clear the pattern id/name of the same row
                    clearPatterInfoOnRow();
                    
                    if (allBuiltin()) {
                        // if all changed to BUILTIN, change the property type of the node to BUILTIN
                        IElementParameter property = node.getElementParameter(EParameterName.PROPERTY_TYPE.getName());

                        if (property != null) {
                            IElementParameter repositoryParam = node.getElementParameter(EParameterName.REPOSITORY_PROPERTY_TYPE
                                    .getName());
                            property.setValue(EmfComponent.BUILTIN);
                            if (repositoryParam != null) {
                                repositoryParam.setValue(StringUtils.EMPTY);
                            }
                        }
                    }
                }

            }

        });

        return combo;
    }

    // check each row, if all selected "BUILTIN",
    private boolean allBuiltin() {
        IElementParameter schemasTableParam = node.getElementParameter("SCHEMA_PATTERN_CHECK");
        TableViewer tableViewer = getTableViewer();
        boolean noPatternSelect = true;
        if (tableViewer != null) {
            int currentIndex = tableViewer.getTable().getSelectionIndex();
            if (schemasTableParam != null) {
                List<Map<String, Object>> paramValues = (List<Map<String, Object>>) schemasTableParam.getValue();
                for(int i=0;i<paramValues.size();i++){
                    if(i==currentIndex){
                        continue;//no need to check current row
                    }
                    Map<String, Object> valueMap = paramValues.get(i);
                    Object value = valueMap.get("PATTERN_PROPERTY");
                    if (StringUtils.equalsIgnoreCase(EmfComponent.REPOSITORY, (String)value)) {
                        noPatternSelect = false;
                    }
                }
            }
        }
        return noPatternSelect;
    }
    
    private void clearPatterInfoOnRow() {
        TableViewer tableViewer = getTableViewer();
        if (tableViewer != null) {
            int currentIndex = tableViewer.getTable().getSelectionIndex();
            IElementParameter schemasTableParam = node.getElementParameter("SCHEMA_PATTERN_CHECK");

            if (schemasTableParam != null) {
                List<Map<String, Object>> paramValues = (List<Map<String, Object>>) schemasTableParam.getValue();
                Map<String, Object> valueMap = null;
                if (paramValues.size() > 0) {
                    valueMap = paramValues.get(currentIndex);
                    valueMap.put("PATTERN_ID", StringUtils.EMPTY);
                    valueMap.put("PATTERN_NAME", StringUtils.EMPTY);
                }
            }
        }
    }

    @Override
    protected Object doGetValue() {
        return combo.getItem(combo.getSelectionIndex());
    }

    @Override
    protected void doSetFocus() {
        combo.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        combo.select(stringToDisplay.indexOf(value));
    }

}
