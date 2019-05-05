package org.talend.sdk.component.studio.model.parameter;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.process.IElement;
import org.talend.sdk.component.studio.model.action.IActionParameter;

/**
 * TacokitElementParameter, which provides a view for Component metadata (schema) if the form of {@code List<String>}
 */
public abstract class SchemaElementParameter extends TaCoKitElementParameter {

    /**
     * Schema display name default value
     */
    static final String DISPLAY_NAME = "!!!SCHEMA.NAME!!!";

    /**
     * A key for additional information. It denotes type of the schema and type of associated connection
     */
    static final String CONNECTION_TYPE = "org.talend.sdk.connection.type";

    private static final String GUESS_BUTTON_PREFIX = "Guess Schema_";

    /**
     * Talend type which denotes String. It is used as default type for schema column
     */
    private static final String STRING = "id_String";

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

    abstract Optional<IMetadataTable> getMetadata();

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
     * Sets schema value.
     * It creates MetadataColumns (schema columns) and sets column names.
     * Then sets columns to Node MetadataTable
     *
     * @param newValue {@code List<String>} of column names to be set
     */
    @Override
    public void setValue(final Object newValue) {
        if (newValue != null && newValue instanceof List) {
            final Optional<IMetadataTable> metadata = getMetadata();
            if (!metadata.isPresent()) {
                return;
            }

            final IMetadataTable table = metadata.orElseThrow(IllegalStateException::new);
            final List<String> schema = (List<String>) newValue;

            final List<IMetadataColumn> columns = new ArrayList<>();
            for (final String columnName : schema) {
                final IMetadataColumn column = ofNullable(table.getColumn(columnName))
                        .orElseGet(() -> {
                            final IMetadataColumn tmp = new MetadataColumn();
                            tmp.setLabel(columnName);
                            tmp.setOriginalDbColumnName(columnName);
                            tmp.setTalendType(STRING);
                            return tmp;
                        });
                columns.add(column);
            }
            table.setListColumns(columns);
        }
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

    /**
     * Creates name for Guess Schema button ElementParameter
     * TODO move to OutputSchemaParameter class
     *
     * @param schemaName a name of schema, which is guessed by the button
     * @return name for Guess Schema button ElementParameter
     */
    public static String guessButtonName(final String schemaName) {
        return GUESS_BUTTON_PREFIX + schemaName;
    }
}
