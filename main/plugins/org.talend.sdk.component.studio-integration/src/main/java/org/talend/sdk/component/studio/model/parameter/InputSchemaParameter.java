// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * SchemaElementParameter for Component input schema.
 * It stores incoming metadata in previous linked Component Node
 */
public class InputSchemaParameter extends SchemaElementParameter {

    public InputSchemaParameter(final IElement element) {
        super(element);
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
        final List<? extends IConnection> connections = NodeUtil.getIncomingConnections(((Node) elem), getContext());
        if (connections == null || connections.isEmpty()) {
            return Optional.empty();
        }
        final IConnection connection = connections.get(0);
        return Optional.ofNullable(connection.getMetadataTable());
    }
}
