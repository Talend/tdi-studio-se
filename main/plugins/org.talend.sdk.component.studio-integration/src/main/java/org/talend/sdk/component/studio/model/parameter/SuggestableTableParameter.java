// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * Table Element Parameter, which rows can be chosen from multiple provided suggestions.
 * Suggested values are schema column names.
 */
public class SuggestableTableParameter extends TableElementParameter {

    /**
     * Constructor setups Table columns and sets empty list as initial value
     *
     * @param element represents persisted element, to which this parameter belongs (it can be component Node
     *                or Connection instance)
     * @param columns a list of parameters, which represents Table columns
     */
    public SuggestableTableParameter(final IElement element, final List<IElementParameter> columns) {
        super(element, columns);
    }

    /**
     * Provides suggestions for Table parameter value.
     * Suggestions are column names which are retrieved from all incoming schemas
     *
     * @return suggestions for parameter value
     */
    public Map<String, String> getSuggestionValues() {
        return getMetadatas().stream()
                .flatMap(m -> m.getListColumns().stream())
                .map(IMetadataColumn::getLabel)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new
                ));
    }

    /**
     * Retrieves metadatas (schemas) of all incoming connections
     * Gets metadata (schema) from incoming connection.
     *
     * @return List of metadatas
     */
    private List<IMetadataTable> getMetadatas() {
        final IElement elem = getElement();
        if (elem == null || !(elem instanceof Node)) {
            return Collections.emptyList();
        }
        final List<? extends IConnection> connections = ((Node) elem).getIncomingConnections();
        if (connections == null || connections.isEmpty()) {
            return Collections.emptyList();
        }
        return connections.stream()
                .filter(c -> c.getLineStyle() == EConnectionType.FLOW_MAIN)
                .map(IConnection::getMetadataTable)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
