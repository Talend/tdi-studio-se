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
package org.talend.designer.unifiedcomponent.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.IUnifiedComponentService;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.designer.unifiedcomponent.component.DelegateComponent;
import org.talend.designer.unifiedcomponent.component.UnifiedObject;

/**
 * created by wchen on Dec 9, 2017 Detailled comment
 *
 */
public class ChangeComponentCommand extends Command {

    private Node node;

    private DelegateComponent delegateComponent;

    private IElementParameter unifiedParam;

    private String newComponent;

    private Object oldComponent;

    private Object repositoryValue;

    public ChangeComponentCommand(Node node, IElementParameter param, String database) {
        this.node = node;
        this.unifiedParam = param;
        if (node.getDelegateComponent() instanceof DelegateComponent) {
            delegateComponent = (DelegateComponent) node.getDelegateComponent();
            UnifiedObject object = delegateComponent.getUnifiedObjectByDatabase(database);
            if (object != null) {
                this.newComponent = object.getDisplayComponentName();
            }
            this.oldComponent = param.getValue();
            IElementParameter propertyType = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (propertyType != null) {
                if (propertyType.getChildParameters().get("PROPERTY_TYPE") != null
                        && !EmfComponent.BUILTIN.equals(propertyType.getChildParameters().get("PROPERTY_TYPE").getValue())) {
                    repositoryValue = propertyType.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").getValue();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        IElementParameter param = node.getElementParameterFromField(unifiedParam.getFieldType());
        param.setValue(newComponent);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IUnifiedComponentService.class)) {
            IUnifiedComponentService service = (IUnifiedComponentService) GlobalServiceRegister.getDefault()
                    .getService(IUnifiedComponentService.class);
            if (service.isDelegateComponent(delegateComponent)) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                List<IMetadataTable> metadataList = new ArrayList<>(node.getMetadataList());
                List<INodeConnector> listConnector = new ArrayList<>(node.getListConnector());
                parameters.put(INode.RELOAD_PARAMETER_ELEMENT_PARAMETERS,
                        new ArrayList<IElementParameter>(node.getElementParameters()));
                parameters.put(INode.OLD_UNIFIED_COMPONENT, oldComponent);
                parameters.put(INode.SWITCH_NODE_METADATA_LIST, metadataList);
                parameters.put(INode.SWITCH_NODE_CONNECTORS, listConnector);
                node.reloadComponent(delegateComponent, parameters, false);

                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
                    IDesignerCoreService designerService = (IDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(IDesignerCoreService.class);
                    if (designerService != null) {
                        designerService.refreshComponentView();
                    }
                }
                node.fireImageChage();
                node.getProcess().checkStartNodes();
                UnifiedComponentUtil.refreshComponentViewTitle();
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        IElementParameter param = node.getElementParameterFromField(unifiedParam.getFieldType());
        param.setValue(oldComponent);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IUnifiedComponentService.class)) {
            IUnifiedComponentService service = (IUnifiedComponentService) GlobalServiceRegister.getDefault()
                    .getService(IUnifiedComponentService.class);
            if (service.isDelegateComponent(delegateComponent)) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                List<IMetadataTable> metadataList = new ArrayList<>(node.getMetadataList());
                List<INodeConnector> listConnector = new ArrayList<>(node.getListConnector());
                parameters.put(INode.RELOAD_PARAMETER_ELEMENT_PARAMETERS,
                        new ArrayList<IElementParameter>(node.getElementParameters()));
                parameters.put(INode.OLD_UNIFIED_COMPONENT, newComponent);
                parameters.put(INode.SWITCH_NODE_METADATA_LIST, metadataList);
                parameters.put(INode.SWITCH_NODE_CONNECTORS, listConnector);

                node.reloadComponent(delegateComponent, parameters, false);
                IElementParameter propertyType = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
                if (propertyType != null) {
                    if (repositoryValue != null) {
                        propertyType.getChildParameters().get("PROPERTY_TYPE").setValue(EmfComponent.REPOSITORY);
                        propertyType.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").setValue(repositoryValue);
                    }
                }

                // updateComponentSchema(metadataMap);

                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
                    IDesignerCoreService designerService = (IDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(IDesignerCoreService.class);
                    if (designerService != null) {
                        designerService.refreshComponentView();
                    }
                }
                node.fireImageChage();
                node.getProcess().checkStartNodes();
                UnifiedComponentUtil.refreshComponentViewTitle();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        return delegateComponent != null;
    }

}
