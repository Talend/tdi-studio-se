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
package org.talend.sdk.component.studio.model.action.update;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.studio.model.parameter.ButtonParameter;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.TableElementParameter;
import org.talend.sdk.component.studio.model.parameter.command.BaseAsyncAction;

/**
 * This TacokitCommand is executed when update button is clicked.
 * This command executes UpdateAction, gets the result and sets result to corresponding ElemenParameters
 */
class UpdateCommand extends BaseAsyncAction<Object> {

    /**
     * Base path prefix which is used to map {@link TaCoKitElementParameter} with keys in UpdateAction result.
     * Prefix is constructed as base path + "."
     * Base path is absolute path of configuration option annotated with Updatable annotation.
     * This configuration option represents a type of result returned by the action.
     */
    private final String basePrefix;

    /**
     * Child parameters to update. This parameters are updated with values returned by UpdateAction
     */
    private final Map<String, TaCoKitElementParameter> parameters;

    /**
     * ButtonParameter is used here to trigger layout refresh
     */
    private final ButtonParameter button;

    /**
     * Constructor
     *
     * @param action     UpdateAction which will be called during running this Command
     * @param basePath   Absolute path of property annotated with Updatable annotation
     * @param parameters Child properties of property annotated with Updatable annotation
     * @param button     Button Parameter which is used to trigger layout refresh
     */
    UpdateCommand(final UpdateAction action, final String basePath,
                  final Map<String, TaCoKitElementParameter> parameters, final ButtonParameter button) {
        super(action);
        this.basePrefix = basePath + ".";
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
        this.button = button;
    }

    /**
     * Updates child parameters value based on action result
     *
     * @param result UpdateAction call result
     */
    @Override
    protected void onResult(final Map<String, Object> result) {
        onResult(basePrefix, result);
        button.firePropertyChange("show", null, true);
    }

    /**
     * Recursively traverse result tree and updates Component parameters according value from Update action result
     *
     * @param basePath  base path prefix
     * @param subResult sub tree of result returned by Update action
     */
    private void onResult(final String basePath, final Map<String, Object> subResult) {
        subResult.entrySet().forEach(entry -> {
            final Object value = entry.getValue();
            final String key = basePath + entry.getKey();
            if (value != null) {
                if (value instanceof Map) {
                    onResult(key + ".", (Map<String, Object>) value);
                } else {
                    final TaCoKitElementParameter param = parameters.get(key);
                    // TODO: throw exception here after fixing schema case
                    if (param != null) {
                        if (EParameterFieldType.TABLE.equals(param.getFieldType())) {
                            TableElementParameter table = (TableElementParameter) param;
                            table.setValueFromAction((List<Object>) value);
                        } else {
                            param.setValue(value);
                        }
                    }
                }
            }
        });
    }
}
