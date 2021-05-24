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

import static org.talend.sdk.component.studio.model.parameter.Metadatas.MAIN_FORM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import org.talend.core.model.process.EParameterFieldType;

public class PropertyNode {

    public static final String UPDATE_BUTTON = ".update";

    static final String CONNECTION_BUTTON = ".testConnection";

    static final String VALIDATION = "Validation";

    private PropertyNode parent;

    private final List<PropertyNode> children;

    private final Map<String, Layout> layouts = new HashMap<>();

    private final PropertyDefinitionDecorator property;

    private final EParameterFieldType fieldType;

    /**
     * Denotes whether this node is root in current tree
     */
    private final boolean root;

    public PropertyNode(final PropertyDefinitionDecorator property, final EParameterFieldType fieldType, final boolean root) {
        this.property = property;
        this.fieldType = fieldType;
        this.root = root;
        this.children = new ArrayList<>();
    }

    /*
        Constructor for tests
     */
    @JsonbCreator
    public PropertyNode(@JsonbProperty("property") final PropertyDefinitionDecorator property,
                        @JsonbProperty("fieldType") final EParameterFieldType fieldType,
                        @JsonbProperty("root") final boolean root,
                        @JsonbProperty("children") List<PropertyNode> children) {
        this.property = property;
        this.fieldType = fieldType;
        this.root = root;
        this.children = new ArrayList<>(children);
    }

    public void addChild(final PropertyNode child) {
        children.add(child);
        child.setParent(this);
    }

    public String getId() {
        return property.getPath();
    }

    public String getParentId() {
        return property.getParentPath();
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Checks whether it is column according ui::gridlayout for specified <code>form</code>
     *
     * @param form Name of form
     * @return true, if it column; false - otherwise
     */
    public boolean isColumn(final String form) {
        if (isRoot()) {
            return false;
        }
        final PropertyDefinitionDecorator parentProperty = getParent().getProperty();
        if (!parentProperty.hasGridLayout(form)) {
            return getParent().isColumn(form);
        }
        return parentProperty.isColumn(property.getName(), form);
    }

    /**
     * Traverses all nodes
     *
     * @param visitor the property visitor to use to traverse the nodes.
     */
    public <T extends PropertyVisitor> T accept(final T visitor) {
        children.forEach(child -> child.accept(visitor));
        visitor.visit(this);
        return visitor;
    }

    /**
     * Traverses nodes of specified <code>form</code> in sorted according metadata order
     *
     * @param visitor the property visitor to use to traverse the nodes.
     * @param form Name of form
     */
    public void accept(final PropertyVisitor visitor, final String form) {
        sortChildren(getChildren(form), form).forEach(child -> child.accept(visitor, form));
        visitor.visit(this);
    }

    private void acceptParentFirst(final PropertyVisitor visitor, final String form) {
        visitor.visit(this);
        final List<PropertyNode> children = sortChildren(getChildren(form), form);
        children.forEach(child -> child.acceptParentFirst(visitor, form));
    }

    /**
     * Returns children, which belongs to specified {@code form}
     *
     * @param form Name of form
     * @return children of specified form
     */
    public List<PropertyNode> getChildren(final String form) {
        final Set<String> childrenNames = new HashSet<>(getChildrenNames(form));
        return children.stream().filter(node -> childrenNames.contains(node.property.getName())).collect(Collectors.toList());
    }

    /**
     * Checks whether subtree rooted by this node has leaves, which belongs to specified {@code form}
     *
     * @param form Name of form
     * @return true, if it has leaves
     */
    public boolean hasLeaves(final String form) {
        final ArrayList<PropertyNode> subNodes = new ArrayList<>(getChildren(form));
        for (int i = 0; i < subNodes.size(); i++) {
            final PropertyNode current = subNodes.get(i);
            if (current.isLeaf()) {
                return true;
            } else {
                subNodes.addAll(current.getChildren(form));
            }
        }
        return false;
    }

    private PropertyNode getChild(final String name, final String form) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(form);
        return getChildren(form).stream().filter(p -> name.equals(p.getProperty().getName())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no child with name " + name));
    }

    /**
     * Sorts children according order specified in metadata or do nothing if order is not specified
     *
     * @param children children node, which belongs specified form
     * @param form Name or form
     * @return sorted list
     */
    protected List<PropertyNode> sortChildren(final List<PropertyNode> children, final String form) {
        final Map<String, Integer> order = property.getChildrenOrder(form);
        if (order != null) {
            children.sort((node1, node2) -> {
                Integer i1 = order.get(node1.getProperty().getName());
                Integer i2 = order.get(node2.getProperty().getName());
                return i1.compareTo(i2);
            });
        }
        // else do not sort
        return children;
    }

    /**
     * Returns all children names
     *
     * Subclasses may override this method
     *
     * @return children names
     */
    protected List<String> getChildrenNames() {
        return children.stream().map(n -> n.getProperty().getName()).collect(Collectors.toList());
    }

    /**
     * Returns children names for specified <code>form</code>.
     * If <code>form</code> is Main form its children may be specified by ui::gridlayout or ui:optionsorder.
     * If it has no both metadata, then all children are considered as Main children.
     * For other <code>form</code> children may be specified only by ui::gridlayout.
     *
     * @param form Name of form
     * @return children names of specified <code>form</code>
     */
    protected List<String> getChildrenNames(final String form) {
        if (MAIN_FORM.equals(form)) {
            return getMainChildrenNames();
        }
        return property.getChildrenNames(form);
    }

    /**
     * Returns children names for Main form
     * If it has ui:gridlayout metadata value for Main form, then names are retrieved from there
     * If it has ui:gridlayout for other forms, then it is considered that Main form is empty
     * If it has ui:optionsorder (and has no any ui:gridlayout), then names are retrieved from there
     * If it has no both metadatas, then all children belong to Main form
     *
     * This implementation calls overridable {@link #getChildrenNames()} to get all children names
     *
     * @return children names for Main form
     */
    private List<String> getMainChildrenNames() {
        if (property.hasGridLayout(MAIN_FORM)) {
            return property.getChildrenNames(MAIN_FORM);
        }
        if (property.hasGridLayouts()) {
            return Collections.emptyList();
        }
        if (property.hasOptionsOrder()) {
            return property.getOptionsOrderNames();
        }
        return getChildrenNames();
    }

    public Layout getLayout(final String name) {
        if (!layouts.containsKey(name)) {
            throw new IllegalArgumentException("No layout " + name);
        }
        return layouts.get(name);
    }

    /**
     * Creates layout for specified {@code form} and computes position for all children nodes.
     * It traverse a tree in-depth. Children nodes are visited before parent
     *
     * @param form Layout form for which node position is computed
     */
    void computePosition(final String form) {
        accept(new LayoutHeightVisitor(form), form);
        acceptParentFirst(new LayoutPositionVisitor(form), form);
    }

    void addLayout(final String name, final Layout layout) {
        layouts.putIfAbsent(name, layout);
    }

    private static class LayoutHeightVisitor implements PropertyVisitor {

        private final String form;

        private PropertyNode current;

        @Override
        public void visit(final PropertyNode node) {
            this.current = node;
            createLayout();
            computeHeight();
        }

        private void createLayout() {
            final Layout layout = new Layout(current.getId());
            if (!current.isLeaf()) {
                if (current.getProperty().hasGridLayout(form)) {
                    fillGridLayout(layout, current.getProperty().getUpdatable());
                } else {
                    fillSimpleLayout(layout, current.getProperty().getUpdatable());
                }
                if (current.getProperty().isCheckable()) {
                    addButton(layout, CONNECTION_BUTTON);
                }
                if (current.getProperty().getUpdatable().map(v -> v.getPreviousProperty().isEmpty()).orElse(false)) {
                    addButton(layout, UPDATE_BUTTON);
                }
            }
            current.addLayout(form, layout);
        }

        private void fillGridLayout(final Layout layout, final Optional<PropertyDefinitionDecorator.Updatable> updatable) {
            final String gridLayout = current.getProperty().getGridLayout(form);
            final String[] rows = gridLayout.split("\\|");
            // create Level for each row
            for (final String row : rows) {
                final Level level = new Level();
                layout.getLevels().add(level);
                for (final String column : row.split(",")) {
                    final PropertyNode child = current.getChild(column, form);
                    if (child.getProperty().hasConstraint() || child.getProperty().hasValidation()) {
                        addValidationLevel(child, layout);
                    }
                    if (matches(updatable, column)) {
                        addButton(layout, UPDATE_BUTTON);
                    }
                    level.getColumns().add(child.getLayout(form));
                }
            }
        }

        private void fillSimpleLayout(final Layout layout, final Optional<PropertyDefinitionDecorator.Updatable> updatable) {
            final List<PropertyNode> children = current.sortChildren(current.getChildren(form), form);
            children.forEach(child -> {
                final Level level = new Level();
                layout.getLevels().add(level);
                // each level contains only one column, when there is no GridLayout
                level.getColumns().add(child.getLayout(form));
                if (child.getProperty().hasConstraint() || child.getProperty().hasValidation()) {
                    addValidationLevel(child, layout);
                }
                if (matches(updatable, child.getProperty().getName())) {
                    addButton(layout, UPDATE_BUTTON);
                }
            });
        }

        private boolean matches(final Optional<PropertyDefinitionDecorator.Updatable> updatable, final String name) {
            return updatable.map(v -> name.equals(v.getPreviousProperty())).orElse(false);
        }

        private void addValidationLevel(final PropertyNode node, final Layout layout) {
            final Level level = new Level();
            Layout validationLayout = new Layout(node.getProperty().getPath() + VALIDATION);
            level.getColumns().add(validationLayout);
            layout.getLevels().add(level);
        }

        /**
         * Adds "Test Connection" button
         *
         * @param layout parent node layout
         */
        private void addButton(final Layout layout, final String buttonName) {
            final Layout buttonLayout = new Layout(layout.getPath() + buttonName);
            buttonLayout.setHeight(1);
            final Level level = new Level();
            level.getColumns().add(buttonLayout);
            layout.getLevels().add(level);
        }

        private void computeHeight() {
            final Layout layout = current.getLayout(form);
            int height = 0;
            if (current.isLeaf()) {
                height = 1;
                if (current.getProperty().hasConstraint() || current.getProperty().hasValidation()) {
                    height++;
                }
            } else {
                layout.getLevels().forEach(level -> {
                    final int levelHeight = level.getColumns().stream().mapToInt(Layout::getHeight).max().getAsInt();
                    level.setHeight(levelHeight);
                });
                height = layout.getLevels().stream().mapToInt(Level::getHeight).sum();
            }
            layout.setHeight(height);
        }

        public LayoutHeightVisitor(final String form) {
            this.form = form;
        }
    }

    private static class LayoutPositionVisitor implements PropertyVisitor {

        /**
         * First 2 position are occupied by schema and property type
         */
        private static final int INITIAL_POSITION = 3;

        private final String form;

        @Override
        public void visit(final PropertyNode node) {
            if (!node.isLeaf()) {
                final Layout layout = node.getLayout(form);
                if (node.isRoot()) {
                    layout.setPosition(INITIAL_POSITION);
                }
                int position = layout.getPosition();
                for (final Level level : layout.getLevels()) {
                    level.setPosition(position);
                    for (final Layout column : level.getColumns()) {
                        column.setPosition(position);
                    }
                    position = position + level.getHeight();
                }
            } // else no-op as position is set during visiting only parent node
        }

        public LayoutPositionVisitor(final String form) {
            this.form = form;
        }
    }

    public PropertyNode getParent() {
        return this.parent;
    }

    public List<PropertyNode> getChildren() {
        return this.children;
    }

    public Map<String, Layout> getLayouts() {
        return this.layouts;
    }

    public PropertyDefinitionDecorator getProperty() {
        return this.property;
    }

    public EParameterFieldType getFieldType() {
        return this.fieldType;
    }

    /**
     * Denotes whether this node is root in current tree
     */
    public boolean isRoot() {
        return this.root;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PropertyNode)) {
            return false;
        }
        final PropertyNode other = (PropertyNode) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$parent = this.getParent();
        final Object other$parent = other.getParent();
        if (this$parent == null ? other$parent != null : !this$parent.equals(other$parent)) {
            return false;
        }
        final Object this$children = this.getChildren();
        final Object other$children = other.getChildren();
        if (this$children == null ? other$children != null : !this$children.equals(other$children)) {
            return false;
        }
        final Object this$layouts = this.getLayouts();
        final Object other$layouts = other.getLayouts();
        if (this$layouts == null ? other$layouts != null : !this$layouts.equals(other$layouts)) {
            return false;
        }
        final Object this$property = this.getProperty();
        final Object other$property = other.getProperty();
        if (this$property == null ? other$property != null : !this$property.equals(other$property)) {
            return false;
        }
        final Object this$fieldType = this.getFieldType();
        final Object other$fieldType = other.getFieldType();
        if (this$fieldType == null ? other$fieldType != null : !this$fieldType.equals(other$fieldType)) {
            return false;
        }
        if (this.isRoot() != other.isRoot()) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PropertyNode;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;

        // don't calc parent/children, because it has dead loop/stackoverflow issue
        // final Object $parent = this.getParent();
        // result = result * PRIME + ($parent == null ? 43 : $parent.hashCode());
        // final Object $children = this.getChildren();
        // result = result * PRIME + ($children == null ? 43 : $children.hashCode());

        final Object $layouts = this.getLayouts();
        result = result * PRIME + ($layouts == null ? 43 : $layouts.hashCode());
        final Object $property = this.getProperty();
        result = result * PRIME + ($property == null ? 43 : $property.hashCode());
        final Object $fieldType = this.getFieldType();
        result = result * PRIME + ($fieldType == null ? 43 : $fieldType.hashCode());
        result = result * PRIME + (this.isRoot() ? 79 : 97);
        return result;
    }

    @Override
    public String toString() {
        return "PropertyNode(children=" + this.getChildren() + ", layouts=" + this.getLayouts() + ", property="
                + this.getProperty() + ", fieldType=" + this.getFieldType() + ", root=" + this.isRoot() + ")";
    }

    protected void setParent(final PropertyNode parent) {
        this.parent = parent;
    }
}
