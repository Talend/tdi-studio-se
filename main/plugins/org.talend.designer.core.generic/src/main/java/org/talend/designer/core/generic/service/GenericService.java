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
package org.talend.designer.core.generic.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.runtime.services.IGenericService;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.daikon.properties.Properties;
import org.talend.designer.core.generic.model.Component;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;

import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by ycbai on 2016年3月24日 Detailled comment
 *
 */
public class GenericService implements IGenericService {

    @Override
    public boolean callBeforeActivate(IElementParameter parameter) {
        if (parameter instanceof GenericElementParameter) {
            return ((GenericElementParameter) parameter).callBeforeActivate();
        }
        return false;
    }

    @Override
    public List<Map<String, String>> getAllGenericComponentsInfo() {
        List<Map<String, String>> genericComponents = new ArrayList<>();
        Set<ComponentDefinition> definitions = ComponentsUtils.getComponentService().getAllComponents();
        if (definitions != null) {
            for (ComponentDefinition definition : definitions) {
                Map<String, String> componentInfo = new LinkedHashMap<>();
                componentInfo.put("Name", definition.getName()); //$NON-NLS-1$
                componentInfo.put("Title", definition.getTitle()); //$NON-NLS-1$
                genericComponents.add(componentInfo);
            }
        }
        return genericComponents;
    }

    @Override
    public void resetReferenceValue(INode curNode, String oldConnectionName, String newConnectionName) {
        ComponentReferenceProperties comPro = null;
        IElementParameter refPara = curNode.getElementParameterFromField(EParameterFieldType.COMPONENT_REFERENCE);
        if(refPara != null){
            comPro = ComponentsUtils.getReferencedComponent(refPara);
        }
        if(comPro != null){
            Object sValue = comPro.componentInstanceId.getStoredValue();
            if (oldConnectionName.equals(sValue)) {
                comPro.componentInstanceId.setValue(newConnectionName);
                return;
            }
            if (sValue != null && ((String)sValue).startsWith(oldConnectionName + "_")) { //$NON-NLS-1$
                comPro.componentInstanceId.setValue(((String)sValue).replaceFirst(oldConnectionName + "_", newConnectionName + "_")); //$NON-NLS-1$
                return;
            }
        }
        comPro = getComponentReferenceProperties(curNode);
        if(comPro == null){
            return;
        }
        Object sValue = comPro.componentInstanceId.getStoredValue();
        if (oldConnectionName.equals(sValue)) {
            comPro.componentInstanceId.setValue(newConnectionName);
        } else if (sValue != null && ((String)sValue).startsWith(oldConnectionName + "_")) { //$NON-NLS-1$
            comPro.componentInstanceId.setValue(((String)sValue).replaceFirst(oldConnectionName + "_", newConnectionName + "_")); //$NON-NLS-1$
        }
    }

    private ComponentReferenceProperties getComponentReferenceProperties(INode curNode){
        ComponentProperties pros = curNode.getComponentProperties();
        if(pros == null){
            return null;
        }
        Properties ps = pros.getProperties("referencedComponent"); //$NON-NLS-1$
        if(ps == null){
            Properties conn = pros.getProperties("connection"); //$NON-NLS-1$
            if(conn != null){
                ps = conn.getProperties("referencedComponent"); //$NON-NLS-1$
            }
        }
        if(ps == null){
            return null;
        }
        if(ps instanceof ComponentReferenceProperties){
            return (ComponentReferenceProperties)ps;
        }
        return null;
    }

    @Override
    public boolean isTcompv0(IComponent component) {
        return Component.class.isInstance(component);
    }

    @Override
    public void validateGenericConnection(Connection conn) throws Exception {
        ComponentsUtils.getComponentPropertiesFromSerialized(conn.getCompProperties(), conn);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            IGenericWizardService gs = GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
            List<MetadataTable> metadataTables = gs.getMetadataTables(conn);
            if (metadataTables != null && !metadataTables.isEmpty()) {
                for (MetadataTable metadataTable : metadataTables) {
                    if (metadataTable == null) {
                        continue;
                    }
                    for (TaggedValue taggedValue : metadataTable.getTaggedValue()) {
                        if (IComponentConstants.COMPONENT_PROPERTIES_TAG.equals(taggedValue.getTag())) {
                            ComponentsUtils.getComponentPropertiesFromSerialized(taggedValue.getValue(), conn);
                        }
                    }
                }
            }
        }
    }

}
