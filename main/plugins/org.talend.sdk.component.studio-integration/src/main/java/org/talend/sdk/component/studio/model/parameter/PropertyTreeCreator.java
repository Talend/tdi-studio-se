/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

import static java.util.Comparator.comparing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;

/**
 * Provides methods for handling {@link PropertyNode} tree
 */
public class PropertyTreeCreator {

    /**
     * Maps widget types to Parameter field type
     */
    private final WidgetTypeMapper typeMapper;

    public PropertyTreeCreator(final WidgetTypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    /**
     * Creates tree representation of {@link PropertyDefinitionDecorator}.
     * Not all definitions represent Component property, which may store User setting.
     * Some of them are holders for other definitions (like Forms or Properties in v0 integration)
     * ElementParameters should be created only from leaf nodes in this tree
     * Internal nodes store useful metadata information like ordering <br>
     * There may be different types of node (different {@link PropertyNode} implementations)
     * Node type is defined by {@link PropertyDefinitionDecorator}, so it should be known during
     * node creation <br>
     *
     * Tree is created according following algorithm:
     * <ol>
     * <li>Find root {@link PropertyDefinitionDecorator}</li>
     * <li>Create root node</li>
     * <li>Create other nodes, but not root</li>
     * <li>Create links between nodes</li>
     * </ol>
     *
     * Note, there are 3 traversals through Collection
     *
     * @param properties a collections of {@link PropertyDefinitionDecorator} retrieved from ComponentModel
     * @return root node of created tree
     */
    public PropertyNode createPropertyTree(final Collection<? extends PropertyDefinitionDecorator> properties) {
        if (properties == null) {
            throw new NullPointerException("properties should not be null");
        }
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("properties should not be empty");
        }
        final PropertyNode root = createRootNode(properties);
        final Map<String, PropertyNode> nodes = new HashMap<>();
        nodes.put(root.getId(), root);
        createRemainingNodes(properties, nodes);
        linkNodes(properties, nodes);
        root.computePosition(Metadatas.MAIN_FORM);
        root.computePosition(Metadatas.ADVANCED_FORM);
        return root;
    }

    /**
     * Creates tree representation of {@link ConfigTypeNode}.<br>
     * Also see {@link #createPropertyTree(Collection)}
     *
     * @param configTypeNode configuration type node: {@link ConfigTypeNode}
     * @return root node of created tree: {@link PropertyNode}
     */
    public PropertyNode createPropertyTree(final ConfigTypeNode configTypeNode) {
        final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator
                .wrap(configTypeNode.getProperties());
        return createPropertyTree(properties);
    }

    /**
     * Creates all nodes and put them into <code>nodes</code> except root node, as it is already there
     *
     * @param properties all {@link PropertyDefinitionDecorator}
     * @param nodes stores all created {@link PropertyNode}
     */
    void createRemainingNodes(final Collection<? extends PropertyDefinitionDecorator> properties,
            final Map<String, PropertyNode> nodes) {
        properties.forEach(property -> nodes.putIfAbsent(property.getPath(), createNode(property, false)));
    }

    /**
     * Links child nodes with their parent nodes. Only root node has no parent node, so it is skipped
     *
     * @param properties all {@link PropertyDefinitionDecorator}
     * @param nodes all {@link PropertyNode}
     */
    private void linkNodes(final Collection<? extends PropertyDefinitionDecorator> properties,
            final Map<String, PropertyNode> nodes) {
        // sort to ensure to builder parents before children
        properties.stream().sorted(comparing(PropertyDefinitionDecorator::getPath)).map(PropertyDefinitionDecorator::getPath)
                .map(nodes::get).filter(c -> !c.isRoot()).forEach(current -> {
                    final PropertyNode propertyNode = nodes.get(current.getParentId());
                    propertyNode.addChild(current);
                });
    }

    /**
     * Factory method, which creates specific {@link PropertyNode} implementation according Property type
     *
     * @param property Property Definition
     * @param isRoot specifies whether this Node is root Node
     * @return {@link PropertyNode} implementation
     */
    PropertyNode createNode(final PropertyDefinitionDecorator property, final boolean isRoot) {
        EParameterFieldType fieldType = typeMapper.getFieldType(property);
        PropertyNode node;
        switch (fieldType) {
        case TABLE:
        case TACOKIT_SUGGESTABLE_TABLE:
        case TACOKIT_INPUT_SCHEMA:
        case SCHEMA_TYPE:
            node = new ListPropertyNode(property, fieldType, isRoot);
            break;

        default:
            node = new PropertyNode(property, fieldType, isRoot);
        }
        return node;
    }

    /**
     * Creates and returns root PropertyNode
     *
     * @param properties all {@link PropertyDefinitionDecorator}
     * @return root PropertyNode
     */
    PropertyNode createRootNode(final Collection<? extends PropertyDefinitionDecorator> properties) {
        return createNode(findRootProperty(properties), true);
    }

    /**
     * Finds a root of {@link PropertyDefinitionDecorator} subtree represented by <code>properties</code> Collection.
     * Root is such {@link PropertyDefinitionDecorator}, which <code>path</code> is "shortest" (minimal) in following
     * meaning:
     * path1 is less than path2, when path2 contains path1. </br>
     * E.g. path1 = "p0.p1"; path2 = "p0.p1.p2"; // path1 is less than path2</br>
     * It is assumed input <code>properties</code> is not null and not empty.
     * Also it is assumed Collection contains only single root element.
     * Note, not any 2 arbitrary PropertyDefinitionDecorator may be compared. They may belong to different branches in a
     * tree.
     * Such PropertyDefinitionDecorator assumed to be equal
     *
     * @param properties Collection of {@link PropertyDefinitionDecorator}
     * @return root {@link PropertyDefinitionDecorator}
     */
    PropertyDefinitionDecorator findRootProperty(final Collection<? extends PropertyDefinitionDecorator> properties) {
        return Collections.min(properties, (p1, p2) -> {
            final String path1 = p1.getPath();
            final String path2 = p2.getPath();
            if (path2.startsWith(path1)) {
                return -1;
            }
            if (path1.startsWith(path2)) {
                return 1;
            }
            return 0;
        });
    }

    /**
     * Finds all roots of specified {@code properties}
     *
     * @param properties all properties
     * @return root properties
     */
    public List<PropertyDefinitionDecorator> findRoots(final List<PropertyDefinitionDecorator> properties) {
        final Map<String, PropertyDefinitionDecorator> nodes = properties.stream()
                .collect(Collectors.toMap(SimplePropertyDefinition::getPath, Function.identity()));
        return properties.stream().map(p -> getRoot(p, nodes)).distinct().collect(Collectors.toList());
    }

    /**
     * Returns root of {@code property} or {@code property} itself, if it is root
     *
     * @param property property whose root will be return
     * @param properties all properties to search for a root in
     * @return root PropertyDefinitionDecorator
     */
    private PropertyDefinitionDecorator getRoot(final PropertyDefinitionDecorator property, final Map<String, PropertyDefinitionDecorator> properties) {
        PropertyDefinitionDecorator current = property;
        while (true) {
            final String parentPath = current.getParentPath();
            if (PropertyDefinitionDecorator.NO_PARENT_ID.equals(parentPath)) {
                return current;
            }
            final PropertyDefinitionDecorator parent = properties.get(parentPath);
            if (parent == null) {
                return current;
            }
            current = parent;
        }
    }

}
