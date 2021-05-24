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
package org.talend.designer.mapper.language.java;

import org.talend.core.language.ECodeLanguage;
import org.talend.designer.mapper.PluginUtils;
import org.talend.designer.mapper.language.AbstractLanguage;
import org.talend.designer.runprocess.IRunProcessService;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class JavaLanguage extends AbstractLanguage {

    private static final String DOUBLE_ESCAPE = "\\"; //$NON-NLS-1$

    private static final String PREFIX_VARIABLE_NAME = ""; //$NON-NLS-1$

    private static final String AND_CONDITION = "&&"; //$NON-NLS-1$

    private static final String PREFIX_FIELD_NAME = "."; //$NON-NLS-1$

    private static final String SUFFIX_FIELD_NAME = ""; //$NON-NLS-1$

    private static final String PREFIX_TABLE_NAME = PREFIX_VARIABLE_NAME;

    private static final String PREFIX_TABLE_NAME_REGEXP = ""; //$NON-NLS-1$

    private static final String SUFFIX_TABLE_NAME = ""; //$NON-NLS-1$

    private static final String SUFFIX_TABLE_NAME_REGEXP = SUFFIX_TABLE_NAME;

    private static final String PREFIX_FIELD_NAME_REGEXP = DOUBLE_ESCAPE + PREFIX_FIELD_NAME;

    private static final String SUFFIX_FIELD_NAME_REGEXP = SUFFIX_FIELD_NAME;

    // add double byte mark and full-size character match in the original local pattern
    // TDI-8499,TDI-17332:remove the extra matching character(such as ?,+,-,*,>,<,==,etc) in the local pattern regexp
    private static final String DOUBLE_BYTE_MARK_REGEXP = "[^\\S^\\W\\u30a1-\\u30f6\\u3041-\\u3093\\uFF00-\\uFFFF\\u4e00-\\u9fa5]*[^\\x00-\\xff]*";

    private static final String LOCATION_PATTERN = PREFIX_TABLE_NAME_REGEXP + "\\s*(\\w+)\\s*" + PREFIX_FIELD_NAME_REGEXP //$NON-NLS-1$
            + "\\s*(\\w*" + DOUBLE_BYTE_MARK_REGEXP + "\\w*" + DOUBLE_BYTE_MARK_REGEXP + "\\w*)\\s*" + SUFFIX_FIELD_NAME_REGEXP; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    private static final String SUBST_PATTERN_FOR_PREFIX_COLUMN_NAME = PREFIX_TABLE_NAME_REGEXP + "\\s*({0})\\s*" //$NON-NLS-1$
            + PREFIX_FIELD_NAME_REGEXP + "\\s*({1})\\s*" + SUFFIX_FIELD_NAME_REGEXP; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    private static final String SUBST_PATTERN_FOR_REPLACE_LOCATION = PREFIX_TABLE_NAME_REGEXP + "(\\s*)\\b{0}\\b(\\s*)" //$NON-NLS-1$
            + SUFFIX_TABLE_NAME_REGEXP + "(\\s*)" + PREFIX_FIELD_NAME_REGEXP + "(\\s*)\\b{1}\\b(\\s*)" + SUFFIX_FIELD_NAME_REGEXP; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    private static final String TEMPLATE_TABLE_VARIABLE = PREFIX_TABLE_NAME + "{1}"; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    private static final String TEMPLATE_TABLE_COLUMN_VARIABLE = TEMPLATE_TABLE_VARIABLE + PREFIX_FIELD_NAME
            + "{2}" + SUFFIX_FIELD_NAME; //$NON-NLS-1$

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    private static final String TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE = PREFIX_TABLE_NAME
            + "{1}" + PREFIX_FIELD_NAME + "{2}" //$NON-NLS-1$ //$NON-NLS-2$
            + SUFFIX_FIELD_NAME;

    /**
     * {0} and {1} must be replaced respectively by the table name and the column name.
     */
    private static final String TEMPLATE_VARS_COLUMN_VARIABLE = PREFIX_VARIABLE_NAME + "{0}"; //$NON-NLS-1$

    /**
     * DOC amaumont PerlLanguage constructor comment.
     */
    public JavaLanguage() {
        super(ECodeLanguage.JAVA);

        IRunProcessService service = PluginUtils.getRunProcessService();
        this.codeChecker = service.getSyntaxChecker(ECodeLanguage.JAVA);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getCouplePattern()
     */
    public String getLocationPattern() {
        return LOCATION_PATTERN;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getPREFIX_FIELD_NAME_REGEXP()
     */
    public String getPrefixFieldRegexp() {
        return PREFIX_FIELD_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getPREFIX_TABLE_NAME_REGEXP()
     */
    public String getPrefixTableRegexp() {
        return PREFIX_TABLE_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getSUFFIX_FIELD_NAME_REGEXP()
     */
    public String getSuffixFieldRegexp() {
        return SUFFIX_FIELD_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getSUFFIX_TABLE_NAME_REGEXP()
     */
    public String getSuffixTableRegexp() {
        return SUFFIX_TABLE_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getSubstPatternForPrefixColumnName()
     */
    public String getSubstPatternForPrefixColumnName() {
        return SUBST_PATTERN_FOR_PREFIX_COLUMN_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getSubstPatternForReplaceLocation()
     */
    public String getSubstPatternForReplaceLocation() {
        return SUBST_PATTERN_FOR_REPLACE_LOCATION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.language.ILanguage#getTEMPLATE_TABLE_COLUMN_VARIABLE()
     */
    public String getTemplateTableColumnVariable() {
        return TEMPLATE_TABLE_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getTEMPLATE_PROCESS_COLUMN_VARIABLE()
     */
    public String getTemplateVarsColumnVariable() {
        return TEMPLATE_VARS_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getTemplateGeneratedCodeTableColumnVariable()
     */
    public String getTemplateGeneratedCodeTableColumnVariable() {
        return TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getTemplateGeneratedCodeTableColumnVariableWithComponentName()
     */
    public String getTemplateGeneratedCodeTableColumnVariableWithComponentNamePrefix() {
        return TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getAndCondition()
     */
    public String getAndCondition() {
        return AND_CONDITION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getPrefixField()
     */
    public String getPrefixField() {
        return PREFIX_FIELD_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getPrefixTable()
     */
    public String getPrefixTable() {
        return PREFIX_TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getSuffixField()
     */
    public String getSuffixField() {
        // TODO Auto-generated method stub
        return SUFFIX_FIELD_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getSuffixTable()
     */
    public String getSuffixTable() {
        return SUFFIX_TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getTemplateTableVariable()
     */
    public String getTemplateTableVariable() {
        return TEMPLATE_TABLE_VARIABLE;
    }

}
