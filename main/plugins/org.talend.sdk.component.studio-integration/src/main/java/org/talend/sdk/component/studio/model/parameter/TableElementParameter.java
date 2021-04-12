/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.sdk.component.studio.model.action.IActionParameter;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * Represents Table parameter. Table parameter is ElementParameter, which EParameterFieldType is TABLE
 * Value of Table parameter should have {@code List<Map<String, Object>>} type. Its value is a list of table records.
 * Each table record is represented by Map.
 * This class ensures Table parameter integrity. When user sets string value to this parameter, it is converted to List
 * Also it implements conversion of stored value from String representation in getStringValue() method. It is used to
 * serialize
 * parameter value in repository.
 */
public class TableElementParameter extends TaCoKitElementParameter {

    /**
     * Constructor setups Table columns and sets empty list as initial value
     *
     * @param element represents persisted element, to which this parameter belongs (it can be component Node
     *                or Connection instance)
     * @param columns a list of parameters, which represents Table columns
     */
    public TableElementParameter(final IElement element, final List<IElementParameter> columns) {
        super(element);
        final List<String> columnNames = new ArrayList<>(columns.size());
        final List<String> displayNames = new ArrayList<>(columns.size());
        for (final IElementParameter param : columns) {
            columnNames.add(param.getName());
            displayNames.add(param.getDisplayName());
            TaCoKitUtil.fillDefaultItemsList(param, null);
        }
        setListItemsDisplayName(displayNames.toArray(new String[0]));
        setListItemsDisplayCodeName(columnNames.toArray(new String[0]));
        setListItemsValue(columns.toArray(new ElementParameter[0]));
        updateValueOnly(new ArrayList<Map<String, Object>>());
        setBasedOnSchema(false);
    }

    /**
     * Retrieves stored value and converts it to String using List.toString() method
     *
     * @return string representation of stored value
     */
    @Override
    public String getStringValue() {
        @SuppressWarnings("unchecked") final List<Map<String, Object>> tableValue =
                (List<Map<String, Object>>) super.getValue();
        return tableValue.toString();
    }

    /**
     * Sets new parameter value. If new value is of type String, converts it to List of Maps (Table) and delegates
     * parent to set the value.
     * Expected string input should be as follows: "[{key1=value11, key2=value12}, {key1=value21, key2=value22}]"
     * A Map instance in the List represents Table row. Rows are separated by ", " (comma with a whitespace).
     * Entries in the Map are also should be separated by ", ".
     * Generally, string argument should be equal to the result of a call to List.toString().
     * <p>
     * If incoming argument is of type List, then sets it without conversion.
     * Else it throws exception.
     *
     * @param newValue value to be set
     */
    @Override
    public void setValue(final Object newValue) {
        if (newValue == null || newValue instanceof String) {
            final List<Map<String, Object>> tableValue = ValueConverter.toTable((String) newValue);
            super.setValue(fromRepository(tableValue));
        } else if (newValue instanceof List) {
            super.setValue(fixClosedListColumn((List<Map<String, Object>>) newValue));
        } else {
            throw new IllegalArgumentException("wrong type on new value: " + newValue.getClass().getName());
        }
    }

    @Override
    public void updateValueOnly(final Object newValue) {
        if (newValue == null || newValue instanceof String) {
            final List<Map<String, Object>> tableValue = ValueConverter.toTable((String) newValue);
            super.updateValueOnly(fromRepository(tableValue));
        } else if (newValue instanceof List) {
            super.updateValueOnly(fixClosedListColumn((List<Map<String, Object>>) newValue));
        } else {
            throw new IllegalArgumentException("wrong type on new value: " + newValue.getClass().getName());
        }
    }

    /*
        Provides quickfix for Closed List column. For some reason, when new row is added Studio sets index of possible
        value instead of String value. This fix converts index back to String value
     */
    private List<Map<String, Object>> fixClosedListColumn(final List<Map<String, Object>> value) {
        if (value == null || value.size() == 0 || getListItemsValue() == null) {
            return value;
        }
        final Map<String, Object> lastRow = value.get(value.size() - 1);
        Arrays.stream(getListItemsValue())
                .map(o -> IElementParameter.class.cast(o))
                .filter(p -> EParameterFieldType.CLOSED_LIST.equals(p.getFieldType()))
                .forEach(p -> {
                    if (lastRow.get(p.getName()) instanceof Integer) {
                        final Object newValue = p.getListItemsValue()[(Integer) lastRow.get(p.getName())];
                        lastRow.put(p.getName(), newValue);
                    }
                });
        return value;
    }

    /**
     * Converts incoming {@code table} value retrieved from Action response to correct
     * parameter value. Following corrections are done:
     * 1. key is fixed - full path of Table child ElementParameter is used instead of Configuration class field name
     * 2. Integer are replaced with Strings (as Integer in TableElementParameter means index of possible value
     *
     * @param table table value retrieved from action response
     * @return correct value, which can be used in TableElementParameter
     */
    public void setValueFromAction(final List<Object> table) {
        final List<Map<String, Object>> converted = new ArrayList<>();
        for (final Object row : table) {
            final Map<String, Object> convertedRow = new LinkedHashMap<>();
            final List<TaCoKitElementParameter> columnParams = getColumnParameters();
            for (final TaCoKitElementParameter columnParam : columnParams) {
                Object columnValue;
                if (row instanceof Map) {
                    final String key = columnParam.getName().replaceFirst(Pattern.quote(getName() + "[]."), "");
                    columnValue = ((Map<String, Object>) row).get(key);
                } else {  // boxed primitive value or String
                    columnValue = row;
                }
                if (columnValue instanceof Integer) {
                    columnValue = String.valueOf(columnValue);
                }
                convertedRow.put(columnParam.getName(), columnValue);
            }
            converted.add(convertedRow);
        }
        super.setValue(converted);
    }

    private List<TaCoKitElementParameter> getColumnParameters() {
        return Arrays.stream(getListItemsValue())
                .map(TaCoKitElementParameter.class::cast)
                .collect(Collectors.toList());
    }

    /**
     * Checks whether incoming {@code table} retrieved from repository and converts
     * it to correct parameter value by replacing repository key with this
     * parameter's name. Note this method doesn't validate incoming {@code value}
     * correctness
     *
     * @param table table value from repository
     * @return converted table value, if incoming value retrieved from repository;
     * If it is not from repository, then returns incoming value unchanged
     */
    private List<Map<String, Object>> fromRepository(final List<Map<String, Object>> table) {
        final Optional<String> repositoryKey = getRepositoryKey(table);
        if (!repositoryKey.isPresent()) {
            return table;
        }
        final List<Map<String, Object>> converted = new ArrayList<>(table.size());
        for (final Map<String, Object> row : table) {
            final Map<String, Object> convertedRow = new HashMap<>();
            for (final Map.Entry<String, Object> cell : row.entrySet()) {
                final String newKey = cell.getKey().replace(repositoryKey.get(), getName());
                convertedRow.put(newKey, cell.getValue());
            }
            converted.add(convertedRow);
        }
        return converted;
    }

    /**
     * Returns repository key, if it was used in table value, or Optional.empty() if it was not user
     *
     * @param tableValue table value
     * @return repository key
     */
    private Optional<String> getRepositoryKey(final List<Map<String, Object>> tableValue) {
        if (tableValue.isEmpty()) {
            return Optional.empty();
        }
        final Map<String, Object> row = tableValue.get(0);
        for (final String repositoryKey : getRepositoryValue().split("\\|")) {
            for (final String cellKey : row.keySet()) {
                if (cellKey.startsWith(repositoryKey)) {
                    return Optional.of(repositoryKey);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public IActionParameter createActionParameter(final String actionParameter) {
        final TableActionParameter parameter = new TableActionParameter(this, actionParameter);
        return parameter;
    }

}
