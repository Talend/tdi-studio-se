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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.talend.core.model.process.EParameterFieldType;

/**
 * Property node, which contains nested properties. This node is used for Tables and Schema
 */
public class ListPropertyNode extends PropertyNode {

    private final List<PropertyNode> nestedProperties = new ArrayList<>();

    /**
     * @param property {@link PropertyDefinitionDecorator}
     * @param fieldType widget type, defines UI representation
     * @param root specifies whether this node is root node
     */
    public ListPropertyNode(final PropertyDefinitionDecorator property, final EParameterFieldType fieldType,
            final boolean root) {
        super(property, fieldType, root);
    }

    /**
     * Adds child as nested property
     * {@link ListPropertyNode} can't have children nodes. It is leaf node.
     * But it may have nested properties, which represent table columns
     *
     * @param column {@link PropertyNode} to be added as table column
     */
    @Override
    public void addChild(final PropertyNode column) {
        nestedProperties.add(column);
        column.setParent(this);
    }

    public List<PropertyNode> getColumns(String form) {
        final List<String> childrenNames = getChildrenNames(form);
        return Collections.unmodifiableList(
                sortChildren(nestedProperties
                        .stream()
                        .filter(node -> childrenNames.contains(node.getProperty().getName()))
                        .collect(Collectors.toList()), form));
    }

    /**
     * Returns all nested properties names
     *
     * @return nested properties names
     */
    @Override
    protected List<String> getChildrenNames() {
        return nestedProperties.stream().map(node -> node.getProperty().getName()).collect(toList());
    }
}
