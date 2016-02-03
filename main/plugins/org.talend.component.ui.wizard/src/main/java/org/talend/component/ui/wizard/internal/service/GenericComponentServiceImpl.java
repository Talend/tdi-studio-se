// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.component.ui.wizard.internal.service;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.talend.component.ui.wizard.util.GenericContextUtil;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentProperties.Deserialized;
import org.talend.components.api.properties.Repository;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;

/**
 * created by ycbai on 2015年11月27日 Detailled comment
 *
 */
public class GenericComponentServiceImpl implements ComponentService {

    private ComponentService componentService;

    private ConnectionItem connectionItem;

    public GenericComponentServiceImpl(ComponentService componentService, ConnectionItem connectionItem) {
        this.componentService = componentService;
        this.connectionItem = connectionItem;
    }

    @Override
    public ComponentProperties validateProperty(String propName, ComponentProperties properties) throws Throwable {
        componentService.validateProperty(propName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    @Override
    public ComponentProperties beforePropertyActivate(String propName, ComponentProperties properties) throws Throwable {
        componentService.beforePropertyActivate(propName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    @Override
    public ComponentProperties beforePropertyPresent(String propName, ComponentProperties properties) throws Throwable {
        componentService.beforePropertyPresent(propName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    @Override
    public ComponentProperties afterProperty(String propName, ComponentProperties properties) throws Throwable {
        componentService.afterProperty(propName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    @Override
    public ComponentProperties beforeFormPresent(String formName, ComponentProperties properties) throws Throwable {
        componentService.beforeFormPresent(formName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    @Override
    public ComponentProperties afterFormNext(String formName, ComponentProperties properties) throws Throwable {
        componentService.afterFormNext(formName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    @Override
    public ComponentProperties afterFormBack(String formName, ComponentProperties properties) throws Throwable {
        componentService.afterFormBack(formName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    @Override
    public ComponentProperties afterFormFinish(String formName, ComponentProperties properties) throws Throwable {
        componentService.afterFormFinish(formName, getComponentPropertiesWithoutContext(properties));
        return properties;
    }

    private ComponentProperties getClonedComponentProperties(ComponentProperties properties) {
        if (properties == null) {
            return null;
        }
        String compPropertiesStr = properties.toSerialized();
        if (compPropertiesStr != null) {
            Deserialized fromSerialized = ComponentProperties.fromSerialized(compPropertiesStr);
            if (fromSerialized != null) {
                return fromSerialized.properties;
            }
        }
        return null;
    }

    private ComponentProperties getComponentPropertiesWithoutContext(ComponentProperties properties) {
        ComponentProperties componentProperties = properties;
        if (isContextMode()) {
            componentProperties = getClonedComponentProperties(properties);
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(null, connectionItem.getConnection(),
                    true);
            GenericContextUtil.revertPropertiesValues(componentProperties, contextType);
        }
        return componentProperties;
    }

    @Override
    public ComponentProperties getPropertiesForComponent(String componentId) {
        return componentService.getPropertiesForComponent(componentId);
    }

    @Override
    public String storeComponentProperties(ComponentProperties properties, String name, String repositoryLocation, Schema schema) {
        return componentService.storeComponentProperties(properties, name, repositoryLocation, schema);
    }

    @Override
    public ComponentProperties commitFormValues(ComponentProperties properties, String formName) {
        componentService.commitFormValues(getComponentPropertiesWithoutContext(properties), formName);
        return properties;
    }

    @Override
    public Set<String> getAllComponentNames() {
        return componentService.getAllComponentNames();
    }

    @Override
    public Set<ComponentDefinition> getAllComponents() {
        return componentService.getAllComponents();
    }

    @Override
    public ComponentDefinition getComponentDefinition(String name) {
        return componentService.getComponentDefinition(name);
    }

    @Override
    public InputStream getComponentPngImage(String componentName, ComponentImageType imageType) {
        return componentService.getComponentPngImage(componentName, imageType);
    }

    @Override
    public ComponentProperties getComponentProperties(String name) {
        return componentService.getComponentProperties(name);
    }

    @Override
    public ComponentWizard getComponentWizard(String name, String location) {
        return componentService.getComponentWizard(name, location);
    }

    @Override
    public List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String location) {
        return componentService.getComponentWizardsForProperties(properties, location);
    }

    @Override
    public Set<String> getMavenUriDependencies(String componentName) {
        return componentService.getMavenUriDependencies(componentName);
    }

    @Override
    public List<ComponentDefinition> getPossibleComponents(ComponentProperties properties) throws Throwable {
        return componentService.getPossibleComponents(properties);
    }

    @Override
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return componentService.getTopLevelComponentWizards();
    }

    @Override
    public InputStream getWizardPngImage(String wizardName, WizardImageType imageType) {
        return componentService.getWizardPngImage(wizardName, imageType);
    }

    @Override
    public ComponentProperties makeFormCancelable(ComponentProperties properties, String formName) {
        componentService.makeFormCancelable(getComponentPropertiesWithoutContext(properties), formName);
        return properties;
    }

    @Override
    public void setRepository(Repository repository) {
        componentService.setRepository(repository);
    }

    private final boolean isContextMode() {
        if (connectionItem != null) {
            return connectionItem.getConnection().isContextMode();
        }
        return false;
    }

}
