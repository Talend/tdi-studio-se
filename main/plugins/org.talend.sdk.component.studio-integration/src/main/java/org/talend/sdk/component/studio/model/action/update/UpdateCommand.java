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
package org.talend.sdk.component.studio.model.action.update;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.parameter.ButtonParameter;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.TableElementParameter;
import org.talend.sdk.component.studio.model.parameter.command.BaseAsyncAction;

/**
 * This TacokitCommand is executed when update button is clicked.
 * This command executes UpdateAction, gets the result and sets result to corresponding ElemenParameters
 */
public class UpdateCommand extends BaseAsyncAction<Object> {

    /**
     * Base path of property annotated with Updatable annotation. This property represents result returned by the action
     */
    private final String basePath;

    /**
     * Children parameters to update
     */
    private final List<TaCoKitElementParameter> parameters;

    /**
     * ButtonParameter is used here to trigger layout refresh
     */
    private final ButtonParameter button;

    public UpdateCommand(final Action<Object> action, final String basePath,
                         final List<TaCoKitElementParameter> parameters, final ButtonParameter button) {
        super(action);
        this.basePath = basePath + ".";
        this.parameters = Collections.unmodifiableList(parameters);
        this.button = button;
    }

    /**
     * Updates children parameters value based on action result
     *
     * @param result
     */
    @Override
    protected void onResult(Map<String, Object> result) {
        parameters.forEach(p -> {
            final String key = p.getName().replaceFirst(basePath, "");
            final Object value = result.get(key);
            if (value != null) {
                if (EParameterFieldType.TABLE.equals(p.getFieldType())) {
                    TableElementParameter t = (TableElementParameter) p;
                    t.setValueFromAction((List<Object>) value);
                } else {
                    p.setValue(value);
                }
            }
        });
        button.firePropertyChange("show", null, true);
    }
}
