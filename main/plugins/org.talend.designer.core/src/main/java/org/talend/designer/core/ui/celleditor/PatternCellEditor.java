package org.talend.designer.core.ui.celleditor;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQPatternService;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;


public class PatternCellEditor extends DialogCellEditor {
    private INode node;

    private AbstractDataTableEditorView tableEditorView;
    
    private Button button;
    
    public PatternCellEditor(Composite parent, int style) {
        super(parent, style);
    }

    public PatternCellEditor(Composite parent, IElement element) {
        super(parent, SWT.NONE);
        this.node = (INode)element;
        
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

    //TODO: when no one use repository, need to set back to "Built-in"
    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        ITDQPatternService service = null;
        if(GlobalServiceRegister.getDefault().isServiceRegistered(ITDQPatternService.class)){
            service = (ITDQPatternService) GlobalServiceRegister.getDefault().getService(ITDQPatternService.class);
        }
        if (service != null) {
            
            IElementParameter typeParam = node.getElementParameter("TYPE");
            if (typeParam ==null && node.getIncomingConnections().size() >0) {
                 IConnection iConnection = node.getIncomingConnections().get(0);
                 typeParam = iConnection.getElementParameter("TYPE");
              }
            
            String[] patternInfo = service.selectPattern(cellEditorWindow.getShell(),typeParam);
            
            //modify the same row's pattern info: id, name, regex
            if(patternInfo!=null){
                int index = 0;
                TableViewer tableViewer = getTableViewer();
                if(tableViewer!=null){
                    index = tableViewer.getTable().getSelectionIndex();
                }    
                updatePatterInfoOnRow(index,patternInfo);
                
                // if any row select a pattern, set the node's PROPERTY = REPOSITORY
                IElementParameter property = node.getElementParameter(EParameterName.PROPERTY_TYPE.getName());

                if (property != null) {
                    IElementParameter repositoryParam = node.getElementParameter(EParameterName.REPOSITORY_PROPERTY_TYPE
                            .getName());
                    if (StringUtils.isNotBlank(patternInfo[0])) {
                        property.setValue(EmfComponent.REPOSITORY);
                        if (repositoryParam != null) {
                            repositoryParam.setValue(patternInfo[0]);
                       }
                    } 
                }
           }
            return patternInfo[1];
        }
        return null;
    }

    /**
     *  <ITEM NAME="PATTERN_NAME" FIELD="TEXT"  VALUE='""' NO_CONTEXT_ASSIST="true"/>
        <ITEM NAME="PATTERN_SELECT"  FIELD="MULTI_PATTERN"  />
        <ITEM NAME="PATTERN_ID" FIELD="TEXT" SHOW_IF="false" />
        <ITEM NAME="PATTERN_REGEX" FIELD="TEXT" SHOW_IF="false" />
        <ITEM NAME="PATTERN_DB_TYPE" FIELD="TEXT" SHOW_IF="false" />

     */
    private void updatePatterInfoOnRow(int rowIndex, String[] patternInfo) {
        IElementParameter schemasTableParam = node.getElementParameter("SCHEMA_PATTERN_CHECK");
        
        if (schemasTableParam != null) {
            List<Map<String, Object>> paramValues = (List<Map<String, Object>>) schemasTableParam.getValue();
            Map<String, Object> valueMap = null;
            if (paramValues.size() > 0) {
                valueMap = paramValues.get(rowIndex);
                valueMap.put("PATTERN_ID", patternInfo[0]);
                valueMap.put("PATTERN_NAME", patternInfo[1]);
                valueMap.put("PATTERN_REGEX", patternInfo[2]);
                valueMap.put("PATTERN_DB_TYPE", patternInfo[3]);
            }
        }
    }

}
