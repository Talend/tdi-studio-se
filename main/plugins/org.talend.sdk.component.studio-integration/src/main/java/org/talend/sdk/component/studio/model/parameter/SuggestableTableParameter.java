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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * Table Element Parameter with single column, which rows can be chosen from multiple provided suggestions.
 * Suggested values are schema column names.
 * It represents parameter for multi choice from defined list of suggested values
 */
public class SuggestableTableParameter extends TableElementParameter {

    /**
     * Name (path) of column ElementParameter.
     * It is used to get row values in {@link org.talend.sdk.component.studio.ui.composite.controller.TableValueSelectionDialog}
     */
    private final String columnKey;

    /**
     * Constructor setups Table columns and sets empty list as initial value
     *
     * @param element represents persisted element, to which this parameter belongs (it can be component Node
     *                or Connection instance)
     * @param columns a list of parameters, which represents Table columns
     */
    public SuggestableTableParameter(final IElement element, final List<IElementParameter> columns) {
        super(element, columns);
        final String[] columnNames = getListItemsDisplayCodeName();
        if (columnNames.length != 1) {
            throw new IllegalArgumentException("SuggestableTableParameter can have only 1 column");
        }
        this.columnKey = columnNames[0];
    }

    public String getColumnKey() {
        return this.columnKey;
    }

    /**
     * Provides suggestions for Table parameter value.
     * Suggestions are column names which are retrieved from all incoming schemas
     *
     * @return suggestions for parameter value
     */
    public List<Map<String, Object>> getSuggestionValues() {
        return getMetadatas().stream()
                .flatMap(m -> m.getListColumns().stream())
                .map(IMetadataColumn::getLabel)
                .distinct()
                .map(columnLabel -> {
                    final Map<String, Object> row = new LinkedHashMap<>();
                    row.put(columnKey, columnLabel);
                    return row;
                })
                .collect(Collectors.toList());
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
