/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

import static java.util.Locale.*;
import static org.talend.core.model.process.EParameterFieldType.*;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.*;
import static org.talend.sdk.component.studio.model.parameter.PropertyTypes.*;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;

/**
 * Maps metadata retrieved from {@link PropertyDefinitionDecorator} to {@link EParameterFieldType}
 */
public class WidgetTypeMapper {

    private static final String TYPE_INPUT = "in";

    private static final String TYPE_OUTPUT = "out";

    /**
     * Recognizes {@link EParameterFieldType} for given {@link PropertyDefinitionDecorator}
     * Implementation note: Most possible types are located first.
     * All checks are implemented in separate methods
     * Only one checker method returns {@code true} for particular Property Definition
     *
     * @param property Property, which field type should be defined
     * @return widget type
     */
    public EParameterFieldType getFieldType(final PropertyDefinitionDecorator property) {
        if (property == null) {
            throw new IllegalArgumentException("property should not be null");
        }
        if (isOutputSchema(property)) {
            return getOutputSchemaType();
        } else if (isInputSchema(property)) {
            return getInputSchemaType();
        } else if (isText(property)) {
            return getTextType();
        } else if (property.isCredential()) {
            return getCredentialType();
        } else if (isTextAreaSelection(property)) {
            return getTextAreaSelectionType();
        } else if (isTextArea(property)) {
            return getTextAreaType();
        } else if (isCheck(property)) {
            return getCheckType();
        } else if (isClosedList(property)) {
            return getClosedListType();
        } else if (isPrevColumnList(property)) {
            return getPrevColumnListType();
        } else if (isSuggestableTable(property)) {
            return getSuggestableTableType();
        } else if (isTable(property)) {
            return getTableType();
        } else if (isValueSelection(property)) {
            return getValueSelectionType();
        } else if (isDate(property)) {
            final String type = property.getMetadata().getOrDefault("ui::datetime", "datetime");
            switch (type) {
                case "time": // HH:MM:ss
                case "date": // YYYY-MM-dd
                case "datetime": // YYYY-MM-dd HH:MM:ss
                case "zoneddatetime": // YYYY-MM-dd HH:MM:ss+offset[zone]
                default: // FIXME: today we don't completely map all the widgets
                    return getDateType();
            }
        }
        final String codeStyle = property.getMetadata().get(UI_CODE);
        if (codeStyle != null) {
            return getCodeType(codeStyle);
        }
        return getTextType();
    }

    /**
     * Checks whether {@code property} represents output schema.
     * Output schema is a Structure with type OUT
     * For output schema widget type should be {@link EParameterFieldType#SCHEMA_TYPE}
     *
     * @param property SimplePropertyDefinition to test
     * @return check result
     */
    private boolean isOutputSchema(final SimplePropertyDefinition property) {
        return property.getMetadata().containsKey(UI_STRUCTURE_VALUE)
                && TYPE_OUTPUT.equalsIgnoreCase(property.getMetadata().get(UI_STRUCTURE_TYPE));
    }

    /**
     * Checks whether {@code property} represents input schema.
     * Input schema is a Structure with type IN
     * For output schema widget type should be {@link EParameterFieldType#TACOKIT_INPUT_SCHEMA}
     *
     * @param property SimplePropertyDefinition to test
     * @return check result
     */
    private boolean isInputSchema(final SimplePropertyDefinition property) {
        return property.getMetadata().containsKey(UI_STRUCTURE_VALUE)
                && TYPE_INPUT.equalsIgnoreCase(property.getMetadata().get(UI_STRUCTURE_TYPE));
    }

    private boolean isPrevColumnList(final SimplePropertyDefinition property) {
        final String builtInSuggestable = property.getMetadata().get("action::built_in_suggestable");
        return "INCOMING_SCHEMA_ENTRY_NAMES".equals(builtInSuggestable) && STRING.equals(property.getType());
    }

    protected EParameterFieldType getPrevColumnListType() {
        return PREV_COLUMN_LIST;
    }

    private boolean isSuggestableTable(final SimplePropertyDefinition property) {
        final String builtInSuggestable = property.getMetadata().get("action::built_in_suggestable");
        return "INCOMING_SCHEMA_ENTRY_NAMES".equals(builtInSuggestable) && ARRAY.equals(property.getType());
    }

    protected EParameterFieldType getSuggestableTableType() {
        return TACOKIT_SUGGESTABLE_TABLE;
    }

    protected EParameterFieldType getCodeType(final String codeStyle) {
        switch (codeStyle.toLowerCase(ROOT)) {
        case "java":
            return MEMO_JAVA;
        case "perl":
            return MEMO_PERL;
        case "sql":
            return MEMO_SQL;
        default:
            return MEMO;
        }
    }

    protected EParameterFieldType getInputSchemaType() {
        return TACOKIT_INPUT_SCHEMA;
    }

    protected EParameterFieldType getOutputSchemaType() {
        return SCHEMA_TYPE;
    }

    /**
     * Checks whether widget type is {@link EParameterFieldType#TEXT}
     *
     * @param property SimplePropertyDefinition to test
     * @return check result
     */
    private boolean isText(final SimplePropertyDefinition property) {
        return STRING.equals(property.getType()) && property.getMetadata().isEmpty();
    }

    protected EParameterFieldType getTextType() {
        return TEXT;
    }

    /**
     * Checks whether widget type is {@link EParameterFieldType#TEXT_AREA}
     *
     * @param property SimplePropertyDefinition to test
     * @return check result
     */
    private boolean isTextArea(final SimplePropertyDefinition property) {
        return property.getMetadata().containsKey(UI_TEXTAREA);
    }

    protected EParameterFieldType getTextAreaType() {
        return TEXT_AREA;
    }

    private boolean isTextAreaSelection(final PropertyDefinitionDecorator property) {
        return isTextArea(property) && property.hasSuggestions();
    }

    protected EParameterFieldType getTextAreaSelectionType() {
        return TACOKIT_TEXT_AREA_SELECTION;
    }

    protected EParameterFieldType getCredentialType() {
        return PASSWORD;
    }

    /**
     * Checks whether widget type is {@link EParameterFieldType#CHECK}
     *
     * @param property SimplePropertyDefinition to test
     * @return check result
     */
    private boolean isCheck(final SimplePropertyDefinition property) {
        return BOOLEAN.equals(property.getType());
    }

    protected EParameterFieldType getCheckType() {
        return CHECK;
    }

    /**
     * Checks whether widget type is {@link EParameterFieldType#CLOSED_LIST}
     *
     * @param property SimplePropertyDefinition to test
     * @return check result
     */
    private boolean isClosedList(final SimplePropertyDefinition property) {
        return ENUM.equals(property.getType()) || property.getMetadata().containsKey(Metadatas.ACTION_DYNAMIC_VALUES);
    }

    protected EParameterFieldType getClosedListType() {
        return CLOSED_LIST;
    }

    /**
     * Checks whether widget type is {@link EParameterFieldType#TABLE}
     *
     * @param property SimplePropertyDefinition to test
     * @return check result
     */
    private boolean isTable(final SimplePropertyDefinition property) {
        return ARRAY.equals(property.getType());
    }

    private boolean isDate(final SimplePropertyDefinition property) {
        return property.getMetadata().containsKey("ui::datetime");
    }

    protected EParameterFieldType getTableType() {
        return TABLE;
    }

    protected EParameterFieldType getDateType() {
        return DATE;
    }

    private boolean isValueSelection(final PropertyDefinitionDecorator property) {
        return property.hasSuggestions();
    }

    private EParameterFieldType getValueSelectionType() {
        return TACOKIT_VALUE_SELECTION;
    }
}
