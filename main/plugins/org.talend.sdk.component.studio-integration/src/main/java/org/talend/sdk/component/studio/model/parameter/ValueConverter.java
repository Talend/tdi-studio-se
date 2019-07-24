/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for ElementParameter value conversion. It is used to convert string values from repository to
 * appropriate types
 * used in ElementParameter
 */
public final class ValueConverter {

    private ValueConverter() {
        new AssertionError();
    }

    /**
     * Pattern used to find first and last square bracket in the string
     */
    private static final Pattern BRACKETS_PATTERN = Pattern.compile("^\\[|\\]$");

    /**
     * Pattern used to find first and last curly bracket in the string
     */
    private static final Pattern CURLY_BRACKETS_PATTERN = Pattern.compile("^\\{|\\}$");

    /**
     * Converts String to List of Maps (table element parameter representation)
     * It assumes {@code str} has correct format (it doesn't check it)
     * TODO: quick implementation. May have bugs
     *
     * @param str String value to be converted to list
     * @return list value
     */
    public static List<Map<String, Object>> toTable(final String str) {
        if (isListEmpty(str)) {
            return new ArrayList<>();
        }
        ArrayList<Map<String, Object>> table = new ArrayList<>();
        String trimmed = trimBrackets(str);
        String[] records = trimmed.split("\\}, \\{");
        for (String record : records) {
            record = trimCurlyBrackets(record);
            String[] entries = record.split(", ");
            Map<String, Object> element = new HashMap<String, Object>();
            for (String entry : entries) {
                String[] keyValue = entry.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                element.put(key, value);
            }
            table.add(element);
        }
        return table;
    }

    /**
     * Checks whether String representation of the list is empty or not
     *
     * @param list String representation of the list
     * @return true, if it is empty
     */
    private static boolean isListEmpty(final String list) {
        return list == null || list.isEmpty() || "[]".equals(list) || "[{}]".equals(list);
    }

    /**
     * Trims first symbol if it is '[' and last one if it is ']'
     *
     * @param str String to trim
     * @return trimmed string
     */
    private static String trimBrackets(final String str) {
        return BRACKETS_PATTERN.matcher(str).replaceAll("");
    }

    /**
     * Trims first symbol if it is '{' and last one if it is '}'
     *
     * @param str String to trim
     * @return trimmed string
     */
    private static String trimCurlyBrackets(final String str) {
        return CURLY_BRACKETS_PATTERN.matcher(str).replaceAll("");
    }

}
