package org.talend.sdk.component.studio.model.parameter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IElement;
import org.talend.sdk.component.studio.model.action.IActionParameter;

/**
 * TacokitElementParameter, which provides a view for Component metadata (schema) if the form of {@code List<String>}
 */
public class SchemaElementParameter extends TaCoKitElementParameter {

    public SchemaElementParameter() {
        super();
    }

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
        return Collections.emptyList(); //TODO
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
