// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.utils.TalendTextUtils;

/**
 * DOC ggu class global comment. Detailled comment
 */
public final class MultiDefaultValuesUtils {

    public static final String SEMICOLON = ";"; //$NON-NLS-1$

    public static final String EMPTY = ""; //$NON-NLS-1$

    private static final String REG_VALUE = TalendTextUtils.addQuotes("(.*?[^\\\\])"); //$NON-NLS-1$

    public static List<String> parserDefaultValues(final String valuesStr) {
        if (valuesStr == null) {
            return Collections.emptyList();
        }
        List<String> defaultValues = new ArrayList<String>();

        Pattern regex = Pattern.compile(REG_VALUE);
        Matcher regexMatcher = regex.matcher(valuesStr);
        while (regexMatcher.find()) {
            defaultValues.add(removeStringQuotationMarks(regexMatcher.group(1)));
        }
        return defaultValues;
    }

    public static String removeStringQuotationMarks(String str) {
        if (str == null) {
            return EMPTY;
        }
        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            return str.replace("\\\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
        case PERL:
        default: // PERL
            return str.replace("\\'", "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    /**
     *
     * DOC ggu Comment method "convertDefaultValues".
     *
     * Convert the default values to string sequence.
     *
     * example: "abc";"def"
     */
    public static String convertDefaultValues(final IElementParameter param) {
        if (param == null) {
            return TalendTextUtils.addQuotes(EMPTY);
        }
        StringBuffer valuesStr = new StringBuffer();
        List<String> defaultValuesList = new ArrayList<String>();

        switch (param.getFieldType()) {
        case COMMAND:
            defaultValuesList = getCurSystemCommandsList(param);
            break;
        default:
            defaultValuesList = getDefaultValues(param);
            break;
        }

        for (String value : defaultValuesList) {
            valuesStr.append(value);
            valuesStr.append(SEMICOLON);
        }
        String values = valuesStr.toString();
        if (values.length() > 0) {
            if (values.endsWith(SEMICOLON)) {
                // remove the ";"
                values = values.substring(0, values.length() - 1);
            }
        } else {
            values = TalendTextUtils.addQuotes(EMPTY);
        }
        return values;
    }

    private static List<String> getDefaultValues(final IElementParameter param) {
        if (param == null) {
            return Collections.emptyList();
        }
        List<IElementParameterDefaultValue> defaultValues = param.getDefaultValues();
        if (defaultValues == null || defaultValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> defaultValuesList = new ArrayList<String>();

        for (IElementParameterDefaultValue eleParam : defaultValues) {
            Object obj = eleParam.getDefaultValue();
            if (obj != null && obj instanceof String) {
                defaultValuesList.add((String) obj);
            }
        }
        return defaultValuesList;
    }

    /**
     *
     * DOC ggu Comment method "getCurSystemCommandsList".
     *
     * this is only for COMMAND
     */
    private static List<String> getCurSystemCommandsList(final IElementParameter param) {
        if (param == null) {
            return Collections.emptyList();
        }
        List<IElementParameterDefaultValue> defaultValues = param.getDefaultValues();
        if (defaultValues == null || defaultValues.isEmpty()) {
            return Collections.emptyList();
        }
        // current os commands
        List<String> curOSCommandsList = new ArrayList<String>();
        // default commands
        List<String> defaultCommandList = new ArrayList<String>();

        for (IElementParameterDefaultValue eleParamValue : defaultValues) {
            Object obj = eleParamValue.getDefaultValue();
            if (obj != null) {
                String condition = eleParamValue.getIfCondition();
                String value = ((String) obj).trim();
                if (condition == null) {
                    // default for all OS
                    defaultCommandList.add(value);
                } else {
                    if (Expression.evaluate(condition, null)) {
                        curOSCommandsList.add(value);
                    }
                }
            }
        }

        if (curOSCommandsList.isEmpty()) {
            return defaultCommandList;
        }
        return curOSCommandsList;
    }
}
