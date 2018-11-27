package org.talend.sdk.component.studio.model.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.model.action.AbstractActionParameter;

public class SchemaActionParameter extends AbstractActionParameter {

    private final SchemaElementParameter schemaParameter;

    public SchemaActionParameter(final SchemaElementParameter schemaParameter, final String actionParameter) {
        super(schemaParameter.getName(), actionParameter);
        this.schemaParameter = schemaParameter;
    }

    @Override
    public Collection<Pair<String, String>> parameters() {
        final List<Pair<String, String>> parameters = new ArrayList<>();
        final List<String> columns = schemaParameter.getValue();
        for (int i = 0; i < columns.size(); i++) {
            final String column = columns.get(i);
            final Pair<String, String> pair = new Pair<>(getParameter() + "[" + i + "]", column);
            parameters.add(pair);
        }
        return parameters;
    }

}
