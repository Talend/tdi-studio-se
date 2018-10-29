package org.talend.sdk.component.studio.model.parameter;

import org.talend.core.model.process.IElement;
import org.talend.sdk.component.studio.model.action.IActionParameter;

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
}
