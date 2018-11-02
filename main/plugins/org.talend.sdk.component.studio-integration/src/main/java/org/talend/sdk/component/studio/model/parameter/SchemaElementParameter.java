package org.talend.sdk.component.studio.model.parameter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.studio.model.action.IActionParameter;

/**
 * TacokitElementParameter, which provides a view for Component metadata (schema) if the form of {@code List<String>}
 */
public class SchemaElementParameter extends TaCoKitElementParameter {

    public static final String CONNECTION_TYPE = "org.talend.sdk.connection.type";

    public SchemaElementParameter(final IElement element) {
        super(element);
    }

    @Override
    public IActionParameter createActionParameter(final String actionParameter) {
        return new SchemaActionParameter(this, actionParameter);
    }

    /**
     * Gets metadata (schema) value and converts it to {@code List<String>} of column names
     *
     * @return {@code List<String>} of schema column names
     */
    @Override
    public List<String> getValue() {
        final Optional<IMetadataTable> metadata = getMetadata();
        if (metadata.isPresent()) {
            return toSchema(metadata.get());
        } else {
            return Collections.emptyList();
        }
    }

    private Optional<IMetadataTable> getMetadata() {
        IElement elem = getElement();
        if (elem == null || !(elem instanceof Node)) {
            return Optional.empty();
        }
        // TODO connection type should be set in constructor and should be always present
        final String connectionType = (String) getTaggedValue(CONNECTION_TYPE);
        if (connectionType == null) {
            return Optional.empty();
        }
        final Optional<IConnection> connection = findConnection(connectionType, (Node) elem);
        return connection.map(IConnection::getMetadataTable);

    }

    // TODO try to get Metadata directly form Node, not from Connection
    private Optional<IConnection> findConnection(final String connectionType, final Node node) {
        if ("in".equalsIgnoreCase(connectionType)) {
            if (node.getComponent().useLookup()) {
                for (final IConnection conn : node.getIncomingConnections()) {
                    if (!(conn instanceof IAdditionalInfo)) {
                        continue;
                    }
                    String inputName = (String) IAdditionalInfo.class.cast(conn).getInfo("INPUT_NAME");
                    if (inputName != null && inputName.equals(getContext())) {
                        return Optional.of(conn);
                    }
                }
            } else {
                final List<? extends IConnection> connections = NodeUtil.getIncomingConnections(node, getContext());
                if (connections != null && !connections.isEmpty()) {
                    return Optional.of(connections.get(0));
                }
            }
        } else {
            final List<? extends IConnection> connections = NodeUtil.getOutgoingConnections(node, getContext());
            if (connections != null && !connections.isEmpty()) {
                return Optional.of(connections.get(0));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves schema column names {code List<String>} from IMetadataTable
     *
     * @param metadata IMetadataTable which represents schema
     * @return {code List<String>} of schema column names
     */
    private List<String> toSchema(final IMetadataTable metadata) {
        final List<IMetadataColumn> columns = metadata.getListColumns();
        if (columns == null) {
            return Collections.emptyList();
        }
        return columns.stream()
                .map(IMetadataColumn::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves schema value and converts it to String using List.toString() method
     *
     * @return string representation schema value
     */
    @Override
    public String getStringValue() {
        final List<String> tableValue = getValue();
        return tableValue.toString();
    }

    /**
     * Sets new value
     *
     * @param newValue value to be set
     */
    @Override
    public void setValue(final Object newValue) {
        super.setValue(newValue); // TODO
    }

    /**
     * Denotes whether parameter should be persisted.
     * SchemaElementParameter should not be persisted.
     *
     * @return false
     */
    @Override
    public boolean isPersisted() {
        return false;
    }
}
