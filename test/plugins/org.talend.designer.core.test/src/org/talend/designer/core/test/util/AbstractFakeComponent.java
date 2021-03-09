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
package org.talend.designer.core.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.core.model.components.AbstractComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.NodeConnector;

/**
 * created by nrousseau on Jun 27, 2012 Detailled comment
 *
 */
public abstract class AbstractFakeComponent extends AbstractComponent {

    /**
     * DOC nrousseau AbstractFakeComponent constructor comment.
     */
    public AbstractFakeComponent() {
        super();
    }

    @Override
    public String getLongName() {
        return "Default Name"; //$NON-NLS-1$
    }

    @Override
    public String getOriginalFamilyName() {
        return "family"; //$NON-NLS-1$
    }

    @Override
    public String getTranslatedFamilyName() {
        return "translated family"; //$NON-NLS-1$
    }

    @Override
    public void setImageRegistry(Map<String, ImageDescriptor> imageRegistry) {
        // do nothing
    }

    @Override
    public ImageDescriptor getIcon32() {
        return null;
    }

    @Override
    public ImageDescriptor getIcon24() {
        return null;
    }

    @Override
    public ImageDescriptor getIcon16() {
        return null;
    }

    @Override
    public List<? extends IElementParameter> createElementParameters(INode node) {
        List<ElementParameter> listParam = new ArrayList<ElementParameter>();

        ElementParameter param;
        param = new ElementParameter(node);
        param.setName(EParameterName.UNIQUE_NAME.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.UNIQUE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.STARTABLE.getName());
        param.setValue(Boolean.TRUE);
        param.setDisplayName(EParameterName.STARTABLE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.ACTIVATE.getName());
        param.setValue(new Boolean(true));
        param.setDisplayName(EParameterName.ACTIVATE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(5);
        param.setReadOnly(false);
        param.setRequired(false);
        param.setShow(true);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName("PROPERTY");//$NON-NLS-1$
        param.setCategory(EComponentCategory.BASIC);
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        param.setValue("");//$NON-NLS-1$
        param.setNumRow(1);
        param.setShow(true);

        ElementParameter propertyChildParam = new ElementParameter(node);
        propertyChildParam.setCategory(EComponentCategory.BASIC);
        propertyChildParam.setName(EParameterName.PROPERTY_TYPE.getName());
        propertyChildParam.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        propertyChildParam.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        propertyChildParam.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        propertyChildParam.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        propertyChildParam.setValue(EmfComponent.BUILTIN);
        propertyChildParam.setNumRow(param.getNumRow());
        propertyChildParam.setFieldType(EParameterFieldType.TECHNICAL);
        propertyChildParam.setShow(false);
        propertyChildParam.setShowIf(param.getName() + " =='" + EmfComponent.REPOSITORY + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        propertyChildParam.setReadOnly(param.isReadOnly());
        propertyChildParam.setNotShowIf(param.getNotShowIf());
        propertyChildParam.setSerialized(true);
        propertyChildParam.setParentParameter(param);

        propertyChildParam = new ElementParameter(node);
        propertyChildParam.setCategory(EComponentCategory.BASIC);
        propertyChildParam.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        propertyChildParam.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        propertyChildParam.setListItemsDisplayName(new String[] {});
        propertyChildParam.setListItemsValue(new String[] {});
        propertyChildParam.setNumRow(param.getNumRow());
        propertyChildParam.setFieldType(EParameterFieldType.TECHNICAL);
        propertyChildParam.setValue(""); //$NON-NLS-1$
        propertyChildParam.setShow(false);
        propertyChildParam.setRequired(true);
        propertyChildParam.setReadOnly(param.isReadOnly());
        propertyChildParam.setShowIf(param.getName() + " =='" + EmfComponent.REPOSITORY + "'"); //$NON-NLS-1$//$NON-NLS-2$
        propertyChildParam.setNotShowIf(param.getNotShowIf());
        propertyChildParam.setSerialized(true);
        propertyChildParam.setParentParameter(param);
        listParam.add(param);

        for (INodeConnector connector : createConnectors(node)) {
            if (!connector.getDefaultConnectionType().hasConnectionCategory(IConnectionCategory.DATA)) {
                continue;
            }
            String context = connector.getName();

            ElementParameter parentParam = new ElementParameter(node);
            parentParam.setName(EParameterName.NOT_SYNCHRONIZED_SCHEMA.getName());
            parentParam.setDisplayName(EParameterName.SCHEMA_TYPE.getDisplayName());
            parentParam.setFieldType(EParameterFieldType.SCHEMA_TYPE);
            parentParam.setCategory(EComponentCategory.BASIC);
            parentParam.setNumRow(1);
            parentParam.setReadOnly(false);
            parentParam.setShow(false);
            parentParam.setContext(context);
            listParam.add(parentParam);

            ElementParameter newParam = new ElementParameter(node);
            newParam.setCategory(EComponentCategory.BASIC);
            newParam.setName(EParameterName.SCHEMA_TYPE.getName());
            newParam.setDisplayName(EParameterName.SCHEMA_TYPE.getDisplayName());
            newParam.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
            newParam.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
            newParam.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
            newParam.setValue(EmfComponent.BUILTIN);
            newParam.setNumRow(1);
            newParam.setFieldType(EParameterFieldType.TECHNICAL);
            newParam.setShow(true);
            newParam.setReadOnly(true);

            newParam.setContext(context);
            newParam.setParentParameter(parentParam);

            newParam = new ElementParameter(node);
            newParam.setCategory(EComponentCategory.BASIC);
            newParam.setName(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
            newParam.setDisplayName(EParameterName.REPOSITORY_SCHEMA_TYPE.getDisplayName());
            newParam.setListItemsDisplayName(new String[] {});
            newParam.setListItemsValue(new String[] {});
            newParam.setNumRow(1);
            newParam.setFieldType(EParameterFieldType.TECHNICAL);
            newParam.setValue(""); //$NON-NLS-1$
            newParam.setShow(false);
            newParam.setRequired(true);
            newParam.setContext(context);
            newParam.setParentParameter(parentParam);

        }

        return listParam;
    }

    @Override
    public List<? extends INodeReturn> createReturns(INode node) {
        return Collections.emptyList();
    }

    @Override
    public List<? extends INodeConnector> createConnectors(INode node) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasConditionalOutputs() {
        return false;
    }

    @Override
    public boolean isMultiplyingOutputs() {
        return false;
    }

    @Override
    public String getPluginExtension() {
        return null;
    }

    @Override
    public boolean isSchemaAutoPropagated() {
        return true;
    }

    @Override
    public boolean isDataAutoPropagated() {
        return false;
    }

    @Override
    public boolean isHashComponent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean useMerge() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean useLookup() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return null;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isVisible(String family) {
        return true;
    }

    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        return Collections.emptyList();
    }

    @Override
    public String getPathSource() {
        return null;
    }

    @Override
    public List<ECodePart> getAvailableCodeParts() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getPluginDependencies() {
        return null;
    }

    @Override
    public boolean isMultipleOutput() {
        return false;
    }

    @Override
    public boolean useImport() {
        return false;
    }

    @Override
    public EComponentType getComponentType() {
        return EComponentType.EMF;
    }

    @Override
    public boolean isTechnical() {
        return false;
    }

    @Override
    public boolean isVisibleInComponentDefinition() {
        return true;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public boolean isMainCodeCalled() {
        return false;
    }

    @Override
    public boolean canParallelize() {
        return false;
    }

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public String getCombine() {
        return null;
    }

    @Override
    public IProcess getProcess() {
        return null;
    }

    protected NodeConnector createBaseConnector(INode node, EConnectionType currentType) {
        NodeConnector nodeConnector = new NodeConnector(node);

        nodeConnector.setDefaultConnectionType(currentType);
        // set the default values
        nodeConnector.setLinkName(currentType.getDefaultLinkName());
        nodeConnector.setMenuName(currentType.getDefaultMenuName());
        RGB rgb = currentType.getRGB();
        Integer lineStyle = currentType.getDefaultLineStyle();

        String connectorName = currentType.getName();

        nodeConnector.setName(connectorName);
        nodeConnector.setMenuName(connectorName + ".MENU"); //$NON-NLS-1$
        nodeConnector.setLinkName(connectorName + ".LINK"); //$NON-NLS-1$

        nodeConnector.addConnectionProperty(currentType, rgb, lineStyle);
        nodeConnector.setBaseSchema(nodeConnector.getName());
        return nodeConnector;
    }

    /* (non-Javadoc)
     * @see org.talend.core.model.components.IComponent#getModulesNeeded(org.talend.core.model.process.INode)
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded(INode node) {
        return getModulesNeeded();
    }
}
