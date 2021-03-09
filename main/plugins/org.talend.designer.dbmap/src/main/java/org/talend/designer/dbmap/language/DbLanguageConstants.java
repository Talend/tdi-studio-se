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
package org.talend.designer.dbmap.language;

/**
 * ggu class global comment. Detailled comment
 */
public interface DbLanguageConstants {

    static final String CARRIAGE_RETURN = "\n"; //$NON-NLS-1$

    static final String DOUBLE_ESCAPE = "\\"; //$NON-NLS-1$

    static final String PREFIX_VARIABLE_NAME = ""; //$NON-NLS-1$

    static final String AND_CONDITION = "AND"; //$NON-NLS-1$

    static final String PREFIX_FIELD_NAME = "."; //$NON-NLS-1$

    static final String SUFFIX_FIELD_NAME = ""; //$NON-NLS-1$

    static final String PREFIX_TABLE_NAME = PREFIX_VARIABLE_NAME;

    static final String PREFIX_TABLE_NAME_REGEXP = ""; //$NON-NLS-1$

    static final String SUFFIX_TABLE_NAME = ""; //$NON-NLS-1$

    static final String SUFFIX_TABLE_NAME_REGEXP = SUFFIX_TABLE_NAME;

    static final String PREFIX_FIELD_NAME_REGEXP = DOUBLE_ESCAPE + PREFIX_FIELD_NAME;

    static final String SUFFIX_FIELD_NAME_REGEXP = SUFFIX_FIELD_NAME;

    static final String LOCATION_PATTERN = PREFIX_TABLE_NAME_REGEXP + "\\s*(\\w+)\\s*" + PREFIX_FIELD_NAME_REGEXP //$NON-NLS-1$
            + "\\s*(\\w+)\\s*" + SUFFIX_FIELD_NAME_REGEXP; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    static final String SUBST_PATTERN_FOR_PREFIX_COLUMN_NAME = PREFIX_TABLE_NAME_REGEXP + "\\s*({0})\\s*" //$NON-NLS-1$
            + PREFIX_FIELD_NAME_REGEXP + "\\s*({1})\\s*" + SUFFIX_FIELD_NAME_REGEXP; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    static final String SUBST_PATTERN_FOR_REPLACE_LOCATION = PREFIX_TABLE_NAME_REGEXP + "(\\s*){0}(\\s*)" //$NON-NLS-1$
            + SUFFIX_TABLE_NAME_REGEXP + "(\\s*)" + PREFIX_FIELD_NAME_REGEXP + "(\\s*){1}(\\s*)" + SUFFIX_FIELD_NAME_REGEXP; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    static final String TEMPLATE_TABLE_VARIABLE = PREFIX_TABLE_NAME + "{0}"; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    static final String TEMPLATE_TABLE_COLUMN_VARIABLE = TEMPLATE_TABLE_VARIABLE + PREFIX_FIELD_NAME + "{1}" + SUFFIX_FIELD_NAME; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    static final String TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE = PREFIX_TABLE_NAME + "{0}" + PREFIX_FIELD_NAME + "{1}" //$NON-NLS-1$ //$NON-NLS-2$
            + SUFFIX_FIELD_NAME;

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    static final String TEMPLATE_VARS_COLUMN_VARIABLE = PREFIX_VARIABLE_NAME + "{0}"; //$NON-NLS-1$

}
