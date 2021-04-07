package org.talend.sdk.studio.process;

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
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.VersionParameter;
import org.talend.sdk.component.studio.model.parameter.WidgetTypeMapper;

/**
 * Wrapper for NodeTypeImpl - class which represents persisted node (component in the process).
 * Provides additional functionality
 */
public final class TaCoKitNode {

    public static final String TACOKIT_COMPONENT_ID = "TACOKIT_COMPONENT_ID";

    public static final String TACOKIT_METADATA_TYPE_ID = "TACOKIT_METADATA_TYPE_ID";

    private final NodeTypeImpl node;

    private final ComponentDetail detail;

    private static final Set<String> MIGRATION_EXCLUSIONS = new HashSet<>(Arrays.asList(EParameterName.UNIQUE_NAME.getName(), TACOKIT_COMPONENT_ID));

    public TaCoKitNode(final NodeTypeImpl node) {
        Objects.requireNonNull(node, "Node should not be null");
        if (!isTacokit(node)) {
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
        final int currentVersion = detail.getVersion();
        final int persistedVersion = getPersistedVersion();
        if (currentVersion < persistedVersion) {
            throw new IllegalStateException("current version: " + currentVersion + " persisted version: " + persistedVersion);
        }
        return currentVersion != persistedVersion;
    }

    public Map<String, String> getPropertiesToMigrate() {
        Map<String, String> properties = new HashMap<>();
        @SuppressWarnings("rawtypes")
        EList parameters = node.getElementParameter();
        for (final Object elem : parameters) {
            ElementParameterTypeImpl parameter = (ElementParameterTypeImpl) elem;
            if (!MIGRATION_EXCLUSIONS.contains(parameter.getName())
                    && (!EParameterFieldType.TECHNICAL.name().equals(parameter.getField())
                            || parameter.getName().endsWith(VersionParameter.VERSION_SUFFIX))) {
                properties.put(parameter.getName(), parameter.getValue());
            }
        }
        return properties;
    }

    private boolean isComponentProperty(final String name) {
        return detail.getProperties().stream().anyMatch(property -> property.getPath().equals(name));
    }

    @SuppressWarnings("unchecked")
    public void migrate(final Map<String, String> properties) {
        final List<ElementParameterTypeImpl> noMigration = getParametersExcludedFromMigration();
        node.getElementParameter().clear();
        node.getElementParameter().addAll(noMigration);
        properties.entrySet().stream().filter(e -> isComponentProperty(e.getKey())).forEach(e -> node.getElementParameter().add(createParameter(e.getKey(), e.getValue())));
        properties.entrySet().stream().filter(e -> e.getKey().endsWith(VersionParameter.VERSION_SUFFIX)).forEach(e -> {
            final ElementParameterTypeImpl parameter = new ElementParameter();
            parameter.setName(e.getKey());
            parameter.setValue(e.getValue());
            parameter.setField(EParameterFieldType.TECHNICAL.getName());
            parameter.setShow(false);
            node.getElementParameter().add(parameter);
        });
        node.setComponentVersion(Integer.toString(detail.getVersion()));
    }

    private ElementParameterTypeImpl createParameter(final String name, final String value) {
        final ElementParameterTypeImpl parameter = new ElementParameter();
        parameter.setName(name);
        parameter.setValue(value);
        parameter.setField(getPropertyType(name));
        return parameter;
    }

    private SimplePropertyDefinition getProperty(final String path) {
        return detail.getProperties().stream().filter(property -> property.getPath().equals(path)).findFirst().orElseThrow(
                () -> new IllegalArgumentException("Can't find property for name: " + path));
    }

    private String getPropertyType(final String path) {
        final PropertyDefinitionDecorator property = PropertyDefinitionDecorator.wrap(getProperty(path));
        return new WidgetTypeMapper().getFieldType(property).getName();
    }

    private List<ElementParameterTypeImpl> getParametersExcludedFromMigration() {
        List<ElementParameterTypeImpl> technical = new ArrayList<>();
        for (final Object elem : node.getElementParameter()) {
            if (MIGRATION_EXCLUSIONS.contains(((ElementParameterTypeImpl) elem).getName())) {
                technical.add((ElementParameterTypeImpl) elem);
            }
        }
        return technical;
    }

    public int getPersistedVersion() {
        return Integer.parseInt(node.getComponentVersion());
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
