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
package org.talend.sdk.component.studio.model.parameter;

import java.util.List;
import java.util.Optional;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * SchemaElementParameter for Component input schema.
 * It stores incoming metadata in previous linked Component Node
 * InputSchemaParameter is not shown on UI
 */
public class InputSchemaParameter extends SchemaElementParameter {

    /**
     * Constructor
     *
     * @param element        IElement to which this parameter belongs
     * @param name           parameter name - unique identifier
     * @param connectionName a name of connections with which this schema associated
     */
    public InputSchemaParameter(final IElement element, final String name, final String connectionName, final List<String> childrenNames) {
        super(element);
        setName(name);
        setDisplayName(DISPLAY_NAME);
        setFieldType(EParameterFieldType.TACOKIT_INPUT_SCHEMA);
        setShow(false);
        setReadOnly(false);
        setRequired(false);
        setContext(connectionName);
        setTaggedValue(CONNECTION_TYPE, PropertyDefinitionDecorator.Connection.Type.IN.toString());
        setListItemsDisplayCodeName(childrenNames.stream().filter(p-> !p.endsWith("[]")).toArray(String[]::new));
    }

    /**
     * Retrieves incoming connection ( connections which links this Component with previous ) by connector name
     * (aka context).
     * Gets metadata (schema) from incoming connection.
     * <p>
     * This implementation assumes there are no incoming connections with same name.
     * However, there may be incoming and outgoing connections with same name.
     *
     * @return metadata
     */
    @Override
    protected Optional<IMetadataTable> getMetadata() {
        final IElement elem = getElement();
        if (elem == null || !(elem instanceof Node)) {
            return Optional.empty();
        }
        final List<? extends IConnection> connections = NodeUtil.getIncomingConnections((Node) elem, getContext());
        if (connections == null || connections.isEmpty()) {
            return Optional.empty();
        }
        final IConnection connection = connections.get(0);
        return Optional.ofNullable(connection.getMetadataTable());
    }
}
