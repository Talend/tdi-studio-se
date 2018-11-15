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

import java.util.Optional;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IElement;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * SchemaElementParameter for Component output schema.
 * It stores outgoing metadata in this Component Node
 */
public class OutputSchemaParameter extends SchemaElementParameter {

    public OutputSchemaParameter(final IElement element) {
        super(element);
    }

    /**
     * Gets metadata from this Component node
     *
     * @return metedata
     */
    protected Optional<IMetadataTable> getMetadata() {
        IElement elem = getElement();
        if (elem == null || !(elem instanceof Node)) {
            return Optional.empty();
        }
        final IMetadataTable metadata = ((Node) elem).getMetadataFromConnector(getContext());
        return Optional.ofNullable(metadata);
    }
}
