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
package org.talend.designer.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.UpdateRepositoryUtils;

/**
 * ggu class global comment. Detailled comment
 */
public final class DetectContextVarsUtils {

    private DetectContextVarsUtils() {
    }

    /**
     *
     * ggu Comment method "detectByPropertyType".
     *
     * type is the EParameterFieldType.PROPERTY_TYPE
     */
    public static Map<String, Set<String>> detectByPropertyType(Element element, boolean show) {
        Map<String, Set<String>> varsMap = new HashMap<String, Set<String>>();
        // PTODO
        List<IElementParameter> elementParameters = element.getElementParametersFromField(EParameterFieldType.PROPERTY_TYPE);
        if (elementParameters != null) {
            for (IElementParameter ptParam : elementParameters) {
                IElementParameter rParam = ptParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName());
                IElementParameter idParam = ptParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                if (rParam != null && idParam != null && EmfComponent.REPOSITORY.equals(rParam.getValue())) {
                    String id = (String) idParam.getValue();
                    ConnectionItem connItem = UpdateRepositoryUtils.getConnectionItemByItemId(id);
                    if (connItem != null) {
                        ConnectionContextHelper.checkContextMode(connItem);
                        Connection connection = connItem.getConnection();
                        if (connection.isContextMode() && (!show || (show && ptParam.isShow(element.getElementParameters())))) {
                            Set<String> neededVars = ConnectionContextHelper.retrieveContextVar(element.getElementParameters(),
                                    connection, null);
                            if (neededVars != null && !neededVars.isEmpty()) {
                                Set<String> varsSet = varsMap.get(id);
                                if (varsSet == null) {
                                    varsMap.put(id, neededVars);
                                } else {
                                    varsSet.addAll(neededVars);
                                }
                            }
                        }
                    }
                }

            }
        }
        return varsMap;
    }

    public static Map<String, Set<String>> detectProcess(IProcess2 process2, boolean withNodes) {
        Map<String, Set<String>> varsMap = new HashMap<String, Set<String>>();
        if (process2 instanceof Element) {
            varsMap.putAll(detectByPropertyType((Element) process2, true));
        }
        if (withNodes) {
            for (INode node : process2.getGraphicalNodes()) {
                varsMap.putAll(detectNode(node));
            }
        }
        return varsMap;
    }

    public static Map<String, Set<String>> detectNode(INode node) {
        if (node instanceof Element) {
            return detectByPropertyType((Element) node, true);
        }
        return Collections.emptyMap();
    }
}
