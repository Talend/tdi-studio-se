package org.talend.sdk.studio.process;

import static org.talend.core.model.process.EParameterFieldType.TEXT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.NodeTypeImpl;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.parameter.WidgetTypeMapper;

/**
 * Wrapper for NodeTypeImpl - class which represents persisted node (component in the process).
 * Provides additional functionality
 */
public final class TaCoKitNode {
    
    public static final String TACOKIT_COMPONENT_ID = "TACOKIT_COMPONENT_ID";
    
    private final NodeTypeImpl node;
    
    private final ComponentDetail detail;
    
    private static final Set<String> TECHNICAL_PARAMETERS = new HashSet<>(
            Arrays.asList(EParameterName.UNIQUE_NAME.getName())); 
    
    public TaCoKitNode(final NodeTypeImpl node) {
        Objects.requireNonNull(node, "Node should not be null");
        if(!isTacokit(node)) {
            throw new IllegalArgumentException("It is not Tacokit node " + node.getComponentName());
        }
        this.node = node;
        final String componentId = getComponentId(node).orElseThrow(() -> 
            new IllegalStateException("No component detail for " + node.getComponentName()));
        this.detail = Lookups.service().getDetailById(componentId);
    }
    
    public String getId() {
        return detail.getId().getId();
    }
    
    public boolean needsMigration() {
        final int currentVersion = getCurrentVersion();
        final int persistedVersion = getPersistedVersion();
        if (currentVersion < persistedVersion) {
            throw new IllegalStateException(
                    "current version: " + currentVersion + " persisted version: " + persistedVersion);
        }
        return currentVersion != persistedVersion;
    }
    
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        @SuppressWarnings("rawtypes")
        EList parameters = node.getElementParameter();
        for (final Object elem : parameters) {
            ElementParameterTypeImpl parameter = (ElementParameterTypeImpl) elem;
            if (!TECHNICAL_PARAMETERS.contains(parameter.getName())) {
                properties.put(parameter.getName(), parameter.getValue());
            }
        }
        return properties;
    }
    
    @SuppressWarnings("unchecked")
    public void migrate(final Map<String, String> properties) {
        final List<ElementParameterTypeImpl> technicalParameters = getTechnicalParameters();
        node.getElementParameter().clear();
        node.getElementParameter().addAll(technicalParameters);
        properties.forEach((name, value) -> {
            if (isComponentProperty(name)) {
                final ElementParameterTypeImpl parameter = createParameter(name, value);
                node.getElementParameter().add(parameter);
            }
        });
        setPersistedVersion(detail.getVersion());
    }
    
    /**
     * Workaround method to filter *validation properties, which should not be serialized normally.
     * This method should be removed after skipping *validation properties serialization.
     * 
     * @return true, if property is stored in SimplePropertyDefinition
     */
    private boolean isComponentProperty(final String name) {
        return detail.getProperties().stream().filter(property -> property.getPath().equals(name)).count() != 0;
    }
    
    private ElementParameterTypeImpl createParameter(final String name, final String value) {
        final ElementParameterTypeImpl parameter = new ElementParameter();
        parameter.setName(name);
        parameter.setValue(value);
        parameter.setField(getPropertyType(name));
        return parameter;
    }
    
    private SimplePropertyDefinition getProperty(final String name) {
        return detail.getProperties().stream().filter(property -> property.getPath().equals(name)).findFirst().orElseThrow(
                () -> new IllegalArgumentException("Can't find property for name: " + name));
    }
    
    private String getPropertyType(final String name) {
        final SimplePropertyDefinition property = getProperty(name);
        final String type = new WidgetTypeMapper().getFieldType(property).getName();
        return type;
    }
    
    /**
     * Returns a list of technical parameters. These parameters should not be lost during migration
     *
     * @return a list with technical parameters 
     */
    private List<ElementParameterTypeImpl> getTechnicalParameters() {
        List<ElementParameterTypeImpl> technical = new ArrayList<>();
        @SuppressWarnings("rawtypes")
        EList parameters = node.getElementParameter();
        for (final Object elem : parameters) {
            ElementParameterTypeImpl parameter = (ElementParameterTypeImpl) elem;
            if (isTechnical(parameter)) {
                technical.add(parameter);
            }
        }
        return technical;
    }
    
    private boolean isTechnical(final ElementParameterTypeImpl parameter) {
        return TECHNICAL_PARAMETERS.contains(parameter.getName()) || EParameterFieldType.TECHNICAL.toString().equals(parameter.getField());
    }
    
    public int getCurrentVersion() {
        return detail.getVersion();
    }
    
    public int getPersistedVersion() {
        return Integer.parseInt(node.getComponentVersion());
    }
    
    private void setPersistedVersion(final int version) {
        node.setComponentVersion(Integer.toString(version));
    }
    
    public static boolean isTacokit(final NodeTypeImpl node) {
        return getComponentId(node).isPresent();
    }
    
    private static Optional<String> getComponentId(final NodeTypeImpl node) {
        for (final Object elem : node.getElementParameter()) {
            ElementParameterTypeImpl parameter = (ElementParameterTypeImpl) elem;
            if (TACOKIT_COMPONENT_ID.equals(parameter.getName())) {
                return Optional.ofNullable(parameter.getValue());
            }
        }
        return Optional.empty();
    }
    
    /**
     * This class extends ElementParameterTypeImpl to allow instance creation
     */
    private static class ElementParameter extends ElementParameterTypeImpl {
        
        /**
         * Extends super class constructor visibility
         */
        public ElementParameter() {
            super();
        }
    }

}
