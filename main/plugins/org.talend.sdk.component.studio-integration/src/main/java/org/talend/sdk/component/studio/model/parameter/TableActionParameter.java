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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.lang.Strings;
import org.talend.sdk.component.studio.model.action.AbstractActionParameter;

public class TableActionParameter extends AbstractActionParameter {

    private final TableElementParameter elementParameter;

    TableActionParameter(final TableElementParameter elementParameter, final String actionParameter) {
        super(elementParameter.getName(), actionParameter);
        this.elementParameter = elementParameter;
    }

    @Override
    public Collection<Pair<String, String>> parameters() {
        final List<Map<String, Object>> value = (List<Map<String, Object>>) elementParameter.getValue();
        final List<Pair<String, String>> parameters = new ArrayList<>();
        if (value != null) {
            int rowNumber = getRowNumber();
            if (rowNumber >= 0 && rowNumber <= value.size()) {
                final Map<String, Object> row = value.get(rowNumber);
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    if (entry.getKey().endsWith(getParameter())) {
                        final String paramValue = Strings.removeQuotes(String.valueOf(entry.getValue()));
                        final Pair parameter = new Pair(getParameter(), paramValue);
                        parameters.add(parameter);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < value.size(); i++) {
                    final Map<String, Object> row = value.get(i);
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        final String key = entry.getKey().replace("[]", "[" + i + "]").replace(elementParameter.getName(),
                                getParameter());
                        final String paramValue = Strings.removeQuotes(String.valueOf(entry.getValue()));
                        final Pair parameter = new Pair(key, paramValue);
                        parameters.add(parameter);
                    }
                }
            }
        }
        return parameters;
    }

    @Override
    public boolean isMissingRequired() {
        final List<Map<String, Object>> value = (List<Map<String, Object>>) elementParameter.getValue();
        if (value != null) {
            int rowNumber = getRowNumber();
            if (rowNumber >= 0 && rowNumber <= value.size()) {
                final Map<String, Object> row = value.get(rowNumber);
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    if (entry.getKey().endsWith(getParameter())) {
                        Object obj = entry.getValue();
                        if (obj == null) {
                            return true;
                        } else if (obj instanceof Integer) {
                            return Integer.parseInt(obj.toString()) == 0;
                        }
                        return StringUtils.isBlank(obj.toString());
                    }
                }
            }
        }
        return this.missingRequired;
    }
}
