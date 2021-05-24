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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.runtime.services.IGenericService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.process.statsandlogs.StatsAndLogsManager;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class ComponentListController extends AbstractElementPropertySectionController {

    public ComponentListController(IDynamicProperty dp) {
        super(dp);
    }

    private Command createCommand(SelectionEvent selectionEvent) {
        Collection<String> elementsName = hashCurControls.keySet();
        for (String name : elementsName) {
            Object o = hashCurControls.get(name);
            if (o instanceof Control) {
                Control ctrl = (Control) o;
                if (ctrl == null) {
                    hashCurControls.remove(name);
                    return null;
                }

                if (ctrl.equals(selectionEvent.getSource()) && ctrl instanceof CCombo) {
                    boolean isDisposed = ((CCombo) ctrl).isDisposed();
                    if (!isDisposed && (!elem.getPropertyValue(name).equals(((CCombo) ctrl).getText()))) {

                        String value = new String(""); //$NON-NLS-1$
                        List<? extends IElementParameter> elementParametersWithChildrens = elem
                                .getElementParametersWithChildrens();
                        for (int i = 0; i < elementParametersWithChildrens.size(); i++) {
                            IElementParameter param = elementParametersWithChildrens.get(i);
                            if (getParameterName(param).equals(name)) {
                                for (int j = 0; j < param.getListItemsValue().length; j++) {
                                    if (((CCombo) ctrl).getText().equals(param.getListItemsDisplayName()[j])) {
                                        value = (String) param.getListItemsValue()[j];
                                    }
                                }
                            }
                        }
                        return new PropertyChangeCommand(elem, name, value);
                    }
                }
            }
        }
        return null;
    }

    IControlCreator cbCtrl = new IControlCreator() {

        @Override
        public Control createControl(final Composite parent, final int style) {
            CCombo cb = new CCombo(parent, style);
            return cb;
        }
    };

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        this.curParameter = param;
        boolean isJobletOk = false;
        // IJobletProviderService service = getJobletProviderService(param);
        // if (service == null) { // not joblet
        // param.setDisplayName(EParameterName.COMPONENT_LIST.getDisplayName());
        // }
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        // **********************
        hashCurControls.put(getParameterName(param), combo);

        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return cLayout;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    public static void renameComponentUniqueName(String oldConnectionName, String newConnectionName, List<Node> nodesToUpdate) {
        for (Node curNode : nodesToUpdate) {
            for (IElementParameter curParam : curNode.getElementParameters()) {
                if (curParam.getFieldType().equals(EParameterFieldType.COMPONENT_LIST)) {
                    String value = (String)curParam.getValue();
                    if (oldConnectionName.equals(curParam.getValue())) {
                        curParam.setValue(newConnectionName);
                    } else if (value != null && value.startsWith(oldConnectionName + "_")) {
                        curParam.setValue(value.replaceFirst(oldConnectionName + "_", newConnectionName + "_"));
                    }
                }else if(curParam.getFieldType().equals(EParameterFieldType.COMPONENT_REFERENCE)){
                    String value = (String)curParam.getValue();
                    if (oldConnectionName.equals(value)) {
                        curParam.setValue(newConnectionName);
                    } else if (value != null && value.startsWith(oldConnectionName + "_")) {
                        curParam.setValue(value.replaceFirst(oldConnectionName + "_", newConnectionName + "_"));
                    }
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericService.class)) {
                        IGenericService genericService = (IGenericService) GlobalServiceRegister.getDefault().getService(
                                IGenericService.class);
                        if(genericService != null){
                            genericService.resetReferenceValue(curNode, oldConnectionName, newConnectionName);
                        }
                    }
                } else if (curParam.getFieldType().equals(EParameterFieldType.TABLE)) {
                    final Object[] itemsValue = curParam.getListItemsValue();
                    for (Object element : itemsValue) {
                        if (element instanceof IElementParameter) {
                            IElementParameter param = (IElementParameter) element;
                            if (param.getFieldType().equals(EParameterFieldType.COMPONENT_LIST)) {
                                List<Map<String, Object>> tableValues = (List<Map<String, Object>>) curParam.getValue();
                                for (Map<String, Object> curLine : tableValues) {
                                    Object value = curLine.get(param.getName());
                                    if (value instanceof Integer) {
                                        String connectionName = (String) param.getListItemsValue()[(Integer) value];
                                        if (connectionName.equals(oldConnectionName)) {
                                            // note: change from "Integer" value stored to "String" value
                                            curLine.put(param.getName(), newConnectionName);
                                        }else if (connectionName != null && connectionName.startsWith(oldConnectionName + "_")) {
                                            curParam.setValue(connectionName.replaceFirst(oldConnectionName + "_", newConnectionName + "_"));
                                        }
                                    } else if (value instanceof String) {
                                        curLine.put(param.getName(), newConnectionName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public static void updateComponentList(IElement elem, IElementParameter param) {
        if (elem instanceof INode) {
            final INode currentNode = (INode) elem;

            final List<INode> nodeList;
            IJobletProviderService jobletService = getJobletProviderService(param);
            if (jobletService != null) {
                nodeList = jobletService.getConnNodesForInputTrigger(currentNode, param);
            } else {
                List<? extends INode> list = null;
                if (PluginChecker.isJobLetPluginLoaded()) {
                    IJobletProviderService jobletProviderService = (IJobletProviderService) GlobalServiceRegister.getDefault()
                            .getService(IJobletProviderService.class);
                    INode jobletNode = currentNode.getJobletNode();
                    if (jobletNode != null && jobletProviderService.isJobletComponent(jobletNode)) {
                        List<? extends INode> jobletNodes = jobletProviderService.getGraphNodesForJoblet(jobletNode);
                        for (INode node : jobletNodes) {
                            if (node != null && node.getUniqueName() != null
                                    && node.getUniqueName().equals(currentNode.getUniqueName())) {
                                list = node.getProcess().getNodesOfType(param.getFilter());
                                break;
                            }
                        }
                    }
                }
                if (list == null) {
                    list = currentNode.getProcess().getNodesOfType(param.getFilter());
                }
                nodeList = new ArrayList<INode>();
                if (list != null) {
                    for (INode datanode : list) {
                        // it's possible to filter the virtual components from this list
                        if (!datanode.isVirtualGenerateNode() && datanode.getUniqueName() != null
                                && !datanode.getUniqueName().equals(StatsAndLogsManager.CONNECTION_UID)) {
                            nodeList.add(datanode);
                        }
                    }
                }
            }
            updateComponentList(nodeList, currentNode, param, true);
        }
    }

    protected static void updateComponentList(Collection<INode> nodeList, INode currentNode, IElementParameter param,
            boolean isSelectDefaultItem) {
        final Collection<String> componentDisplayNames = new ArrayList<String>();
        final Collection<String> componentUniqueNames = new ArrayList<String>();
        for (INode node : nodeList) {
            if (node.getJobletNode() != null) {
                node = node.getJobletNode();
            }
            final String uniqueName = node.getUniqueName();
            if (uniqueName.equals(currentNode.getUniqueName())) {
                continue;
            }
            String displayName = (String) node.getElementParameter("LABEL").getValue(); //$NON-NLS-1$
            if (displayName == null) {
                displayName = uniqueName;
            }
            if (displayName.indexOf("__UNIQUE_NAME__") != -1) { //$NON-NLS-1$
                displayName = displayName.replaceAll("__UNIQUE_NAME__", uniqueName); //$NON-NLS-1$
            }
            if (!displayName.equals(uniqueName)) {
                displayName = uniqueName + " - " + displayName; //$NON-NLS-1$
            }
            componentUniqueNames.add(uniqueName);
            componentDisplayNames.add(displayName);
        }

        param.setListItemsDisplayName(componentDisplayNames.toArray(new String[0]));
        final String[] componentValueList = componentUniqueNames.toArray(new String[0]);
        param.setListItemsValue(componentValueList);

        Object value = param.getValue();
        if (!componentUniqueNames.contains(value) && isSelectDefaultItem) {
            String newValue = null;
            if (!param.isDynamicSettings()) {
                if (!componentUniqueNames.isEmpty()) {
                    if (value == null || value.equals("")) { //$NON-NLS-1$
                        currentNode.setPropertyValue(getParameterName(param), componentValueList[0]);
                        if (currentNode instanceof IGraphicalNode) {
                            IGraphicalNode node = (IGraphicalNode) currentNode;
                            node.checkAndRefreshNode();
                            ((IProcess2) node.getProcess()).setProcessModified(true);
                        } else if (currentNode instanceof IConnection) {
                            ((IProcess2) ((IConnection) currentNode).getSource().getProcess()).setProcessModified(true);
                        }
                    } else {
                        newValue = componentValueList[0];

                    }
                } else { // removed the old value.
                    newValue = "";//$NON-NLS-1$
                }
            }

            if (!("".equals(newValue)) && newValue != null) { //$NON-NLS-1$
                IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                if (part instanceof AbstractMultiPageTalendEditor) {
                    AbstractTalendEditor te = ((AbstractMultiPageTalendEditor) part).getTalendEditor();
                    CommandStack cmdStack = (CommandStack) te.getAdapter(CommandStack.class);
                    cmdStack.execute(new PropertyChangeCommand(currentNode, getParameterName(param), ""));
                }
            }
        }
    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent event) {
            Command cmd = createCommand(event);
            executeCommand(cmd);
        }
    };

    @Override
    public void refresh(IElementParameter param, boolean check) {
        CCombo combo = (CCombo) hashCurControls.get(getParameterName(param));
        if (combo == null || combo.isDisposed()) {
            return;
        }
        doUpdateComponentList(elem, param);

        String[] curComponentNameList = param.getListItemsDisplayName();
        String[] curComponentValueList = (String[]) param.getListItemsValue();

        Object value = param.getValue();
        int numValue = -1;
        for (int i = 0; i < curComponentValueList.length; i++) {
            if (curComponentValueList[i].equals(value)) {
                numValue = i;
                break;
            }
        }

        if (param.isContextMode()) {
            combo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
            combo.setEnabled(false);
            combo.setText((String) value);
        } else {
            combo.setItems(curComponentNameList);
            if (numValue == -1) {
                if (isSelectDefaultItem() && curComponentNameList.length > 0) {
                    elem.setPropertyValue(getParameterName(param), curComponentValueList[0]);
                    combo.setText(curComponentNameList[0]);
                }
            } else {
                combo.setText(curComponentNameList[numValue]);
            }
        }
    }

    protected void doUpdateComponentList(IElement elem, IElementParameter param) {
        updateComponentList(elem, param);
    }

    protected boolean isSelectDefaultItem() {
        return false;
    }

    private static IJobletProviderService getJobletProviderService(IElementParameter param) {
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService jobletService = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (param != null && param.getElement() instanceof INode
                    && jobletService.isJobletComponent((INode) param.getElement()) && param.getParentParameter() != null) {
                return jobletService;
            }
        }
        return null;
    }

    private static String getParameterName(IElementParameter param) {
        IJobletProviderService service = getJobletProviderService(param);
        if (service != null) { // is joblet node
            return param.getParentParameter().getName() + ":" + param.getName(); //$NON-NLS-1$
        } else {
            return param.getName();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

}
