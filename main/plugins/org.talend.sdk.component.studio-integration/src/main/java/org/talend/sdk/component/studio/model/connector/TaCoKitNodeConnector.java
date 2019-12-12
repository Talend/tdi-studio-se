/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.model.connector;

import static org.talend.core.model.process.EConnectionType.FLOW_MAIN;
import static org.talend.core.model.process.EConnectionType.REJECT;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.talend.core.CorePlugin;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.sdk.component.studio.lang.Strings;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitNodeConnector extends NodeConnector implements IAdditionalInfo {

    static final String DEFAULT = "__default__";

    private static final String TYPE = "CONNECTOR_TYPE";

    private static final String TACOKIT_TYPE = "tacokit";

    private final Map<String, Object> info = new HashMap<>();

    private boolean hasInput = false;

    private boolean hasOutput = false;

    private Boolean isMultiSchema = null;

    /**
     * This constructor should be used by default. It is suitable to create connectors of all types except FLOW and REJECT.
     * FLOW connector may have different name, linkName and menuName from default values for FLOW connection type.
     * REJECT connector is different from other, because its default connection type should be FLOW.
     *
     * @param node node to which this connector belongs to
     * @param type connection type
     */
    TaCoKitNodeConnector(final INode node, final EConnectionType type) {
        this(node, type.getName(), type.getDefaultLinkName(), type.getDefaultMenuName(), type, type);
    }

    /**
     * Creates connector according specified arguments and sets min/max links to 0, which means connector may no have any connections.
     * Correct min/max links values should be set after constructor
     *
     * @param node node to which this connector belongs to
     * @param name connection type name
     * @param linkName display name, which is shown as part of connection name
     * @param menuName display name, which is shown in Component context menu
     * @param type connection type
     * @param defaultConnectionType default connection type
     */
    private TaCoKitNodeConnector(final INode node, final String name, final String linkName, final String menuName, final EConnectionType type, final EConnectionType defaultConnectionType) {
        super(node);
        Objects.requireNonNull(name);
        Objects.requireNonNull(linkName);
        Objects.requireNonNull(menuName);
        Objects.requireNonNull(type);
        Objects.requireNonNull(defaultConnectionType);
        setBaseSchema(type.getName());
        setName(name);
        setLinkName(linkName);
        setMenuName(menuName);
        setDefaultConnectionType(defaultConnectionType);
        addConnectionProperty(CorePlugin.getDefault() == null ? null : type, type.getRGB(),
                type.getDefaultLineStyle());
        putInfo(TYPE, TACOKIT_TYPE);
    }

    /**
     * Creates new connector of FLOW_MAIN type.
     * This connector may have name(type), linkName and menuName defined in component.
     * If type defined in component is "_default_" than connector has the same values as FLOW_MAIN connection type
     *
     * @param node node to which this connector belongs to
     * @param name connector name(type)
     * @return TaCoKitNodeConnector of FLOW_MAIN type
     */
    static TaCoKitNodeConnector newFlow(final INode node, final String name) {
        Objects.requireNonNull(name);
        Strings.requireNonEmpty(name);
        TaCoKitNodeConnector tacokitConnector;
        if (DEFAULT.equals(name)) {
            tacokitConnector = new TaCoKitNodeConnector(node, FLOW_MAIN.getName(), FLOW_MAIN.getDefaultLinkName(),
                    FLOW_MAIN.getDefaultMenuName(), FLOW_MAIN, FLOW_MAIN);
        } else {
            tacokitConnector = new TaCoKitNodeConnector(node, name, name, name, FLOW_MAIN, FLOW_MAIN);
        }
        return tacokitConnector;
    }

    /**
     * Creates new connector of REJECT type.
     * A specialty of REJECT connector is it should have FLOW_MAIN type as default connector type
     *
     * @param node node to which this connector belongs to
     * @param originalName original connector name set in component
     * @return TaCoKitNodeConnector of REJECT type
     */
    static TaCoKitNodeConnector newReject(final INode node, final String originalName) {
        return new TaCoKitNodeConnector(node, originalName, REJECT.getDefaultLinkName(), REJECT.getDefaultMenuName(), FLOW_MAIN,
                FLOW_MAIN);
    }

    @Override
    public void setMultiSchema(boolean multiSchema) {
        super.setMultiSchema(multiSchema);
        this.isMultiSchema = multiSchema;
    }

    @Override
    public boolean isMultiSchema() {
        if (this.isMultiSchema == null) {
            return super.isMultiSchema();
        } else {
            return this.isMultiSchema;
        }
    }

    public boolean hasInput() {
        return hasInput;
    }

    void setHasInput(final boolean input) {
        hasInput = input;
    }

    public boolean hasOutput() {
        return hasOutput;
    }

    void setHasOutput(final boolean output) {
        hasOutput = output;
    }

    @Override
    public Object getInfo(String key) {
        Objects.requireNonNull(key);
        return info.get(key);
    }

    @Override
    public void putInfo(String key, Object value) {
        Objects.requireNonNull(key);
        info.put(key, value);
    }

    @Override
    public void onEvent(String event, Object... parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cloneAddionalInfoTo(IAdditionalInfo target) {
        Objects.requireNonNull(target);
        info.entrySet().stream().forEach(e -> target.putInfo(e.getKey(), e.getValue()));
    }

}
