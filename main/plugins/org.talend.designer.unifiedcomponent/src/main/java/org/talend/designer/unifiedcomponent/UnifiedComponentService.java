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
package org.talend.designer.unifiedcomponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.IUnifiedComponentService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.unifiedcomponent.component.DelegateComponent;
import org.talend.designer.unifiedcomponent.component.UnifiedObject;
import org.talend.designer.unifiedcomponent.manager.UnifiedComponentsManager;

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

/**
 * created by wchen on Dec 6, 2017 Detailled comment
 *
 */
public class UnifiedComponentService implements IUnifiedComponentService {

    private static final String FAMILY_HIER_SEPARATOR = "/"; //$NON-NLS-1$

    private static final String FAMILY_DATABASES = "Databases"; //$NON-NLS-1$

    private static final String FAMILY_SPECIFIED = "DB Specifics";//$NON-NLS-1$

    private static final String FAMILY_DATABASES_SPECIFIED = FAMILY_DATABASES + FAMILY_HIER_SEPARATOR + FAMILY_SPECIFIED;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IUnifiedComponentService#isUnifiedComponent(org.talend.core.model.components.IComponent)
     */
    @Override
    public boolean isUnifiedComponent(IComponent component) {
        if (component == null) {
            return false;
        }
        IComponent dcomp = getDelegateComponent(component);
        if (dcomp != component) {
            return true;
        }
        return false;
    }

    @Override
    public String getUnifiedComponetName4DndFromRepository(IComponentName setting, IComponent selectedComponent) {
        if (selectedComponent instanceof DelegateComponent) {
            DelegateComponent selectedDcomp = (DelegateComponent) selectedComponent;
            DelegateComponent inOutDelegateComp = null;
            String inOutComponent = setting.getInputComponentName();
            String paletteType = selectedDcomp.getPaletteType();
            if (inOutComponent != null) {
                inOutDelegateComp = (DelegateComponent) getDelegateForEmfComponent(inOutComponent, paletteType);
                if (inOutDelegateComp == selectedComponent) {
                    return inOutComponent;
                }
            }
            if (inOutDelegateComp == null) {
                inOutComponent = setting.getOutPutComponentName();
                if (inOutComponent != null) {
                    inOutDelegateComp = (DelegateComponent) getDelegateForEmfComponent(inOutComponent, paletteType);
                    if (inOutDelegateComp == selectedComponent) {
                        return inOutComponent;
                    }
                }
            }
            if (inOutDelegateComp == null) {
                inOutComponent = setting.getDefaultComponentName();
                if (inOutComponent != null) {
                    inOutDelegateComp = (DelegateComponent) getDelegateForEmfComponent(inOutComponent, paletteType);
                    if (inOutDelegateComp == selectedComponent) {
                        return inOutComponent;
                    }
                }
            }
            if (inOutDelegateComp != null) {
                UnifiedObject object = inOutDelegateComp.getUnifiedObjectByName(inOutComponent);
                if (object != null) {
                    String database = object.getDatabase();
                    UnifiedObject unifiedObjectByDatabase = selectedDcomp.getUnifiedObjectByDatabase(database);
                    if (unifiedObjectByDatabase != null) {
                        return unifiedObjectByDatabase.getComponentName();
                    }
                }

            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IUnifiedComponentService#getDelegateComponents()
     */
    @Override
    public Set<IComponent> getDelegateComponents(String paletteType) {
        return UnifiedComponentsManager.getInstance().getDelegateComponents(paletteType);
    }

    @Override
    public IComponent getDelegateComponent(IComponent component) {
        if (component instanceof DelegateComponent) {
            return component;
        }
        String paletteType = component.getPaletteType();
        for (IComponent comp : getDelegateComponents(paletteType)) {
            DelegateComponent dComp = (DelegateComponent) comp;
            Set<UnifiedObject> unifiedObjects = dComp.getUnifiedObjectsByPalette(dComp.getPaletteType());
            for (UnifiedObject obj : unifiedObjects) {
                if (obj.getComponentName().equals(component.getName())) {
                    return dComp;
                }
            }
        }
        return component;
    }

    private IComponent getDelegateForEmfComponent(String emfCompName, String paletteType) {
        for (IComponent comp : getDelegateComponents(paletteType)) {
            DelegateComponent dComp = (DelegateComponent) comp;
            Set<UnifiedObject> unifiedObjects = dComp.getUnifiedObjectsByPalette(dComp.getPaletteType());
            for (UnifiedObject obj : unifiedObjects) {
                if (obj.getComponentName().equals(emfCompName)) {
                    return dComp;
                }
            }
        }
        return null;
    }

    @Override
    public IComponent getDelegateComponent(String delegateCompName, String paletteType) {
        for (IComponent comp : getDelegateComponents(paletteType)) {
            if (comp.getName().equals(delegateCompName)) {
                return comp;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IUnifiedComponentService#isDelegateComponent(org.talend.core.model.components.IComponent
     * )
     */
    @Override
    public boolean isDelegateComponent(IComponent component) {
        if (component instanceof DelegateComponent) {
            return true;
        }
        return false;
    }

    @Override
    public void switchComponent(INode node, IComponent delegateComponent, String oldEmfComponent,
            List<? extends IElementParameter> oldParams, List<IMetadataTable> oldMetadataTables,
            List<INodeConnector> oldConnectors) {
        if (!(delegateComponent instanceof DelegateComponent)) {
            return;
        }
        DelegateComponent dComp = (DelegateComponent) delegateComponent;

        Map<String, String> oldParamMapping = new HashMap<String, String>();
        Map<String, String> oldConnectorMapping = new HashMap<String, String>();
        if (oldEmfComponent != null) {
            UnifiedObject unifiedObject = dComp.getUnifiedObjectByName(oldEmfComponent);
            if (unifiedObject != null) {
                oldParamMapping.putAll(unifiedObject.getParameterMapping());
                oldConnectorMapping.putAll(unifiedObject.getConnectorMapping());
            }
        }

        IElementParameter newUnifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        String unifiedComp = String.valueOf(newUnifiedParam.getValue());
        UnifiedObject unifiedObject = dComp.getUnifiedObjectByName(unifiedComp);
        Map<String, String> newParamMapping = new HashMap<String, String>();
        Map<String, String> newConnectorMapping = new HashMap<String, String>();
        Set<String> mappingExelude = new HashSet<String>();
        if (unifiedObject != null) {
            newParamMapping.putAll(unifiedObject.getParameterMapping());
            mappingExelude.addAll(unifiedObject.getParamMappingExclude());
            newConnectorMapping.putAll(unifiedObject.getConnectorMapping());
        }
        List<? extends IElementParameter> newParams = node.getElementParameters();
        Map<String, Object> storeValueMap = storeValue(oldParams);
        for (IElementParameter newParam : newParams) {
            if (newParam.getFieldType() == EParameterFieldType.UNIFIED_COMPONENTS
                    || newParam.getFieldType() == EParameterFieldType.PROPERTY_TYPE) {
                continue;
            }
            if (mappingExelude.contains(newParam.getName())) {
                continue;
            }

            String newParamMappedValue = newParamMapping.get(newParam.getName());
            String newParamRepositoryValue = newParam.getRepositoryValue();
            IElementParameter param2Find = null;
            for (IElementParameter oldParam : oldParams) {
                String oldParamMappedValue = oldParamMapping.get(oldParam.getName());
                String oldParamRepositoryValue = oldParam.getRepositoryValue();
                if (newParamMappedValue != null) {
                    if (newParamMappedValue.equals(oldParamMappedValue)
                            || newParamMappedValue.equalsIgnoreCase(oldParamRepositoryValue)) {
                        param2Find = oldParam;
                        break;
                    }
                } else if (newParamRepositoryValue != null) {
                    if (newParamRepositoryValue.equals(oldParamMappedValue)
                            || newParamRepositoryValue.equalsIgnoreCase(oldParamRepositoryValue)) {
                        param2Find = oldParam;
                        break;
                    }
                }
            }
            if (param2Find != null) {
                node.setPropertyValue(newParam.getName(), param2Find.getValue());
                if (newParam.getFieldType() == EParameterFieldType.TABLE) {
                    newParam.setListItemsValue(param2Find.getListItemsValue());
                }
                for (String name : newParam.getChildParameters().keySet()) {
                    IElementParameter targetChildParam = newParam.getChildParameters().get(name);
                    IElementParameter sourceChildParam = param2Find.getChildParameters().get(name);
                    if (sourceChildParam == null) {
                        continue;
                    }
                    String pname = newParam.getName() + ":" + sourceChildParam.getName();//$NON-NLS-1$
                    if (storeValueMap.get(pname) != null) {
                        node.setPropertyValue(pname, storeValueMap.get(pname));
                    } else {
                        node.setPropertyValue(pname, sourceChildParam.getValue());
                    }

                    if (targetChildParam.getFieldType() == EParameterFieldType.TABLE) {
                        targetChildParam.setListItemsValue(sourceChildParam.getListItemsValue());
                    }
                }

            }
        }
        updateComponentSchema(node, oldMetadataTables, oldConnectors, oldConnectorMapping, newConnectorMapping);

        node.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), true);

    }

    private Map<String, Object> storeValue(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (obj == null) {
            return map;
        }
        List<? extends IElementParameter> oldElementParameters = (List<? extends IElementParameter>) obj;
        for (IElementParameter sourceParam : oldElementParameters) {
            map.put(sourceParam.getName(), sourceParam.getValue());

            for (String name : sourceParam.getChildParameters().keySet()) {
                IElementParameter sourceChildParam = sourceParam.getChildParameters().get(name);
                if (sourceChildParam == null) {
                    continue;
                }
                map.put(sourceParam.getName() + ":" + sourceChildParam.getName(), sourceChildParam.getValue()); //$NON-NLS-1$
            }
        }
        return map;
    }

    private void updateComponentSchema(INode node, List<IMetadataTable> oldMetadataTables, List<INodeConnector> oldConnectors,
            Map<String, String> oldConnectorMapping, Map<String, String> newConnectorMapping) {
        List<IMetadataTable> reloadMetadataList = node.getMetadataList();
        if (oldMetadataTables != null) {
            for (IMetadataTable newTable : reloadMetadataList) {
                String connNewFromMapping = newConnectorMapping.get(newTable.getAttachedConnector()) == null
                        ? newTable.getAttachedConnector()
                        : newConnectorMapping.get(newTable.getAttachedConnector());
                for (IMetadataTable oldTable : oldMetadataTables) {
                    String connOldFromMapping = oldConnectorMapping.get(oldTable.getAttachedConnector()) == null
                            ? oldTable.getAttachedConnector()
                            : oldConnectorMapping.get(oldTable.getAttachedConnector());
                    if (connNewFromMapping.equals(connOldFromMapping)) {
                        newTable.getListColumns().clear();
                        for (IMetadataColumn column : oldTable.getListColumns()) {
                            newTable.getListColumns().add(column);
                        }
                        IGenericWizardService wizardService = null;
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
                            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault()
                                    .getService(IGenericWizardService.class);
                        }
                        if (wizardService != null) {
                            wizardService.updateComponentSchema(node, newTable);
                        }
                        break;
                    }
                }

            }
        }

        List<? extends INodeConnector> listConnector = node.getListConnector();
        for (INodeConnector newConnector : listConnector) {
            if (newConnector.getDefaultConnectionType().hasConnectionCategory(EConnectionType.FLOW)) {
                String newMappedName = newConnectorMapping.get(newConnector.getName()) == null ? newConnector.getName()
                        : newConnectorMapping.get(newConnector.getName());
                INodeConnector oldConnectorOut = findConnectors(newMappedName, oldConnectors, oldConnectorMapping, false);
                if (oldConnectorOut != null) {
                    if (oldConnectorOut.getCurLinkNbOutput() > 0
                            && oldConnectorOut.getDefaultConnectionType().hasConnectionCategory(EConnectionType.FLOW)) {
                        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections(oldConnectorOut.getName());
                        IMetadataTable findConnectedTable = findConnectedTable(newConnector, reloadMetadataList);
                        if (findConnectedTable != null) {
                            for (IConnection connection : outgoingConnections) {
                                connection.setConnectorName(newConnector.getName());
                                connection.setMetaName(findConnectedTable.getTableName());
                            }
                        }
                    }
                } else {
                    List<? extends IConnection> outgoingConnections = node.getOutgoingConnections(newConnector.getName());
                    IMetadataTable findConnectedTable = findConnectedTable(newConnector, reloadMetadataList);
                    if (findConnectedTable != null) {
                        for (IConnection connection : outgoingConnections) {
                            connection.setConnectorName(newConnector.getName());
                            connection.setMetaName(findConnectedTable.getTableName());
                        }
                    }
                }
            }
        }
        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        for (IConnection connection : incomingConnections) {
            INodeConnector targetNodeConnector = connection.getTargetNodeConnector();
            targetNodeConnector.setCurLinkNbInput(targetNodeConnector.getCurLinkNbInput() + 1);
        }

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        for (IConnection connection : outgoingConnections) {
            INodeConnector sourceNodeConnector = connection.getSourceNodeConnector();
            sourceNodeConnector.setCurLinkNbOutput(sourceNodeConnector.getCurLinkNbOutput() + 1);
        }

    }

    private INodeConnector findConnectors(String newMappedName, List<INodeConnector> listConnector,
            Map<String, String> oldConnectorMapping, boolean input) {
        for (INodeConnector connector : listConnector) {
            String oldMappedName = oldConnectorMapping.get(connector.getName()) == null ? connector.getName()
                    : oldConnectorMapping.get(connector.getName());
            if (newMappedName.equals(oldMappedName)) {
                if (input) {
                    if (connector.getMaxLinkInput() == -1 || connector.getMaxLinkInput() > 0) {
                        return connector;
                    }
                } else {
                    if (connector.getMaxLinkOutput() == -1 || connector.getMaxLinkOutput() > 0) {
                        return connector;
                    }
                }
            }
        }
        return null;
    }

    private IMetadataTable findConnectedTable(INodeConnector connector, List<IMetadataTable> metadataList) {
        for (IMetadataTable table : metadataList) {
            if (table.getAttachedConnector() != null && table.getAttachedConnector().equals(connector.getName())) {
                return table;
            }
        }
        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.IUnifiedComponentService#createParameters(java.util.List,
     * org.talend.core.model.components.IComponent, org.talend.core.model.components.IComponent)
     */
    @Override
    public void createParameters(INode node, List<IElementParameter> listParams, IComponent delegateComp, IComponent emfComp) {
        if (delegateComp instanceof DelegateComponent) {
            DelegateComponent dComp = (DelegateComponent) delegateComp;
            if (emfComp != dComp) {
                listParams.addAll(dComp.createElementParameters(node, false));
                IElementParameter unifiedParam = getUnifiedParameter(listParams);
                String unifiedComponents = emfComp.getName();
                if (node instanceof Node) {
                    Node editorNode = (Node) node;
                    if (StringUtils.isNoneBlank(editorNode.getUnifiedComponentDisplayName())) {
                        unifiedComponents = editorNode.getUnifiedComponentDisplayName();
                    }
                }
                unifiedParam.setValue(unifiedComponents);
            } else {
                listParams.addAll(dComp.createElementParameters(node));
            }
        }
    }

    private IElementParameter getUnifiedParameter(List<IElementParameter> listParams) {
        for (IElementParameter param : listParams) {
            if (param.getFieldType() == EParameterFieldType.UNIFIED_COMPONENTS) {
                return param;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.IUnifiedComponentService#getUnifiedCompDisplayName(org.talend.core.model.components.
     * IComponent, java.lang.String)
     */
    @Override
    public String getUnifiedCompDisplayName(IComponent delegateComponent, String emfComponent) {
        if (delegateComponent instanceof DelegateComponent) {
            DelegateComponent dcomp = (DelegateComponent) delegateComponent;
            UnifiedObject unifiedObjectByName = dcomp.getUnifiedObjectByName(emfComponent);
            if (unifiedObjectByName != null) {
                return unifiedObjectByName.getDatabase();
            }
        }
        return null;
    }

    public String getUnifiedCompRealComponentName(IComponent delegateComponent, String emfComponent) {
        if (delegateComponent instanceof DelegateComponent) {
            DelegateComponent dcomp = (DelegateComponent) delegateComponent;
            UnifiedObject unifiedObjectByName = dcomp.getUnifiedObjectByName(emfComponent);
            if (unifiedObjectByName != null) {
                return unifiedObjectByName.getComponentName();
            }
        }
        return null;
    }

    @Override
    public void filterUnifiedComponentForPalette(IComponentsFactory compFac, Collection<IComponent> componentSet,
            String lowerCasedKeyword) {
        IUnifiedComponentService service = (IUnifiedComponentService) GlobalServiceRegister.getDefault()
                .getService(IUnifiedComponentService.class);
        // filter unified components
        Iterator<IComponent> iterator = componentSet.iterator();
        while (iterator.hasNext()) {
            boolean familyChanged = false;
            boolean hideAll = false;
            IComponent next = iterator.next();
            String[] originFamilies = next.getOriginalFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
            String[] translatedFamilies = next.getTranslatedFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
            IComponent delegateComp = service.getDelegateComponent(next);
            if (delegateComp instanceof DelegateComponent) {
                UnifiedObject unifiedObject = ((DelegateComponent) delegateComp).getUnifiedObjectByName(next.getName());
                if (unifiedObject != null) {
                    hideAll = true;
                    for (int i = 0; i < originFamilies.length; i++) {
                        String oFamily = originFamilies[i];
                        // if no hide family is not set in json , will hide all
                        if (!unifiedObject.getHideFamilies().isEmpty() && !unifiedObject.getHideFamilies().contains(oFamily)) {
                            hideAll = false;
                        } else {
                            familyChanged = true;
                            originFamilies[i] = null;
                            translatedFamilies[i] = null;
                        }
                    }
                    if (hideAll) {
                        familyChanged = false;
                        iterator.remove();
                    }
                }
            }
            if (familyChanged) {
                String newOriginal = "";
                String newTranslated = "";
                for (int i = 0; i < originFamilies.length; i++) {
                    if (originFamilies[i] != null) {
                        newOriginal = newOriginal + originFamilies[i];
                        newTranslated = newTranslated + translatedFamilies[i];
                        if (i < originFamilies.length - 1) {
                            newOriginal = newOriginal + "|";
                            newTranslated = newTranslated + "|";
                        }
                    }
                }
                next.setOriginalFamilyName(newOriginal);
                next.setTranslatedFamilyName(newTranslated);
            }
        }

    }

    @Override
    public String getComponentDisplayNameForPalette(IComponent delegateComponent, String filter) {
        if (delegateComponent instanceof DelegateComponent) {
            DelegateComponent dcomp = (DelegateComponent) delegateComponent;
            if (filter != null) {
                for (UnifiedObject obj : dcomp.getUnifiedObjects()) {
                    if (matchFilter(obj, filter)) {
                        return dcomp.getName() + "(" + obj.getDatabase() + ")";
                    }
                }

            }
        }
        return delegateComponent.getName();
    }

    @Override
    public IComponent getUnifiedComponentByFilter(IComponent delegateComponent, String filter) {
        if (delegateComponent instanceof DelegateComponent) {
            DelegateComponent dcomp = (DelegateComponent) delegateComponent;
            if (filter != null) {
                for (UnifiedObject obj : dcomp.getUnifiedObjects()) {
                    if (matchFilter(obj, filter)) {
                        IComponentsService compService = (IComponentsService) GlobalServiceRegister.getDefault()
                                .getService(IComponentsService.class);
                        IComponent emfComponent = compService.getComponentsFactory().get(obj.getComponentName(),
                                dcomp.getPaletteType());
                        return emfComponent;
                    }
                }

            }
        }
        return null;
    }

    private boolean matchFilter(UnifiedObject obj, String filter) {
        boolean match = obj.getDatabase().equalsIgnoreCase(filter) || obj.getComponentName().equalsIgnoreCase(filter)
                || obj.getComponentName().substring(1).equalsIgnoreCase(filter);
        return match;
    }

    public void initComponentIfJDBC(INode node, IComponent delegateComponent) {
        if (!(delegateComponent instanceof DelegateComponent)) {
            return;
        }

        DelegateComponent dComp = (DelegateComponent) delegateComponent;
        IElementParameter newUnifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        String unifiedComp = String.valueOf(newUnifiedParam.getValue());
        UnifiedObject unifiedObject = dComp.getUnifiedObjectByName(unifiedComp);
        if (!unifiedObject.getComponentName().equals(unifiedObject.getDisplayComponent())) {
            // database name is like delta lake
            String database = unifiedObject.getDatabase();
            // TODO load from json
            if ("Delta Lake".equals(database)) {
                // init the required param
                node.getElementParameter("connection.jdbcUrl").setValue("jdbc:spark://");
//                node.getElementParameter("connection.driverTable")
//                        .setValue(new ArrayList<String>().add("mvn:Spark/SparkJDBC42/2.6.14.1018/jar"));
                node.getElementParameter("connection.driverClass").setValue("com.simba.spark.jdbc.Driver");
            }

        }
    }

}
