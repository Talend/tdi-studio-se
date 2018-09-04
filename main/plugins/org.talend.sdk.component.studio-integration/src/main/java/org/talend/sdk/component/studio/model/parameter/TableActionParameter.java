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

import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.lang.Strings;
import org.talend.sdk.component.studio.model.action.AbstractActionParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class TableActionParameter extends AbstractActionParameter {

    private final TableElementParameter elementParameter;

    TableActionParameter(final TableElementParameter elementParameter, final String actionParameter) {
        super(elementParameter.getName(), actionParameter);
        this.elementParameter = elementParameter;
    }

    @Override
    public Collection<Pair<String, String>> parameters() {
        final List<Map<String, String>> value = (List<Map<String, String>>) elementParameter.getValue();
        final List<Pair<String, String>> parameters = new ArrayList<>();
        if (value != null) {
            for (int i=0; i<value.size(); i++) {
                final Map<String, String> row = value.get(i);
                for(Map.Entry<String, String> entry : row.entrySet()) {
                    final String key = entry.getKey().replace("[]", "[" + i + "]")
                            .replace(elementParameter.getName(), getParameter());
                    final String paramValue = Strings.removeQuotes(entry.getValue());
                    final Pair parameter = new Pair(key, paramValue);
                    parameters.add(parameter);
                }
            }
        }
        return parameters;
    }
}
