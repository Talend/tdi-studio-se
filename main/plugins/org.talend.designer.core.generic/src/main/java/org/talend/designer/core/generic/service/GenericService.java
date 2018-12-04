// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.runtime.services.IGenericService;
import org.talend.daikon.properties.Properties;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;

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
        ComponentProperties pros = curNode.getComponentProperties();
        if(pros == null){
            return;
        }
        Properties ps = pros.getProperties("referencedComponent"); //$NON-NLS-1$
        if(ps == null){
            return;
        }
        if(!(ps instanceof ComponentReferenceProperties)){
            return;
        }
        ComponentReferenceProperties comPro = (ComponentReferenceProperties) ps;
        Object sValue = comPro.componentInstanceId.getStoredValue();
        if (oldConnectionName.equals(sValue)) {
            comPro.componentInstanceId.setValue(newConnectionName);
        } else if (sValue != null && ((String)sValue).startsWith(oldConnectionName + "_")) { //$NON-NLS-1$
            comPro.componentInstanceId.setValue(((String)sValue).replaceFirst(oldConnectionName + "_", newConnectionName + "_")); //$NON-NLS-1$
        }
    }

}
