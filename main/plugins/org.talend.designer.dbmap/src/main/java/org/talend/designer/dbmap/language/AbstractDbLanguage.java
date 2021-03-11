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

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.utils.data.text.StringHelper;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.designer.dbmap.language.operator.IDbOperatorManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: AbstractLanguage.java 1877 2007-02-06 17:16:43Z amaumont $
 *
 */
public abstract class AbstractDbLanguage implements IDbLanguage {

    private IDbOperatorManager operatorsManager;

    protected ICodeProblemsChecker codeChecker;

    /**
     *
     * Database joints.
     *
     * $Id$
     *
     */
    public static enum JOIN implements IJoinType {
        NO_JOIN("(IMPLICIT JOIN)"), //$NON-NLS-1$
        INNER_JOIN("INNER JOIN"), //$NON-NLS-1$
        LEFT_OUTER_JOIN("LEFT OUTER JOIN"), //$NON-NLS-1$
        RIGHT_OUTER_JOIN("RIGHT OUTER JOIN"), //$NON-NLS-1$
        FULL_OUTER_JOIN("FULL OUTER JOIN"), //$NON-NLS-1$
        CROSS_JOIN("CROSS JOIN"); //$NON-NLS-1$

        String label;

        JOIN(String label) {
            this.label = label;
        }

        /**
         * Getter for label.
         *
         * @return the label
         */
        public String getLabel() {
            return this.label;
        }

        public static IJoinType getJoin(String joinType) {
            if (joinType == null) {
                return NO_JOIN;
            }
            return valueOf(joinType);
        }

    };

    /**
     *
     */
    public AbstractDbLanguage(IDbOperatorManager operatorsManager) {
        this.operatorsManager = operatorsManager;

    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.designer.dbmap.language.IDbLanguage#getLocation(org.talend.designer.dbmap.model.tableentry.
     * TableEntryLocation)
     */
    public String getLocation(String tableName, String columnName) {
        return StringHelper.replacePrms(getTemplateTableColumnVariable(), new Object[] { tableName, columnName });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getLocation(java.lang.String)
     */
    public String getLocation(String tableName) {
        return StringHelper.replacePrms(getTemplateTableVariable(), new Object[] { tableName });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getCodeChecker()
     */
    public ICodeProblemsChecker getCodeChecker() {
        return codeChecker;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getAvailableJoins()
     */
    public IJoinType[] getAvailableJoins() {
        return JOIN.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getJoin(java.lang.String)
     */
    public IJoinType getJoin(String joinType) {
        return JOIN.getJoin(joinType);
    }

    public List<IJoinType> unuseWithExplicitJoin() {
        List<IJoinType> joins = new ArrayList<IJoinType>();
        joins.add(JOIN.NO_JOIN);
        return joins;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getCouplePattern()
     */
    public String getLocationPattern() {
        return DbLanguageConstants.LOCATION_PATTERN;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getPREFIX_FIELD_NAME_REGEXP()
     */
    public String getPrefixFieldRegexp() {
        return DbLanguageConstants.PREFIX_FIELD_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getPREFIX_TABLE_NAME_REGEXP()
     */
    public String getPrefixTableRegexp() {
        return DbLanguageConstants.PREFIX_TABLE_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSUFFIX_FIELD_NAME_REGEXP()
     */
    public String getSuffixFieldRegexp() {
        return DbLanguageConstants.SUFFIX_FIELD_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSUFFIX_TABLE_NAME_REGEXP()
     */
    public String getSuffixTableRegexp() {
        return DbLanguageConstants.SUFFIX_TABLE_NAME_REGEXP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSubstPatternForPrefixColumnName()
     */
    public String getSubstPatternForPrefixColumnName() {
        return DbLanguageConstants.SUBST_PATTERN_FOR_PREFIX_COLUMN_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getSubstPatternForReplaceLocation()
     */
    public String getSubstPatternForReplaceLocation() {
        return DbLanguageConstants.SUBST_PATTERN_FOR_REPLACE_LOCATION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.language.ILanguage#getTEMPLATE_TABLE_COLUMN_VARIABLE()
     */
    public String getTemplateTableColumnVariable() {
        return DbLanguageConstants.TEMPLATE_TABLE_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getTEMPLATE_PROCESS_COLUMN_VARIABLE()
     */
    public String getTemplateVarsColumnVariable() {
        return DbLanguageConstants.TEMPLATE_VARS_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getTEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE()
     */
    public String getTemplateGeneratedCodeTableColumnVariable() {
        return DbLanguageConstants.TEMPLATE_GENERATED_CODE_TABLE_COLUMN_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getAndCondition()
     */
    public String getAndCondition() {
        return DbLanguageConstants.AND_CONDITION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getPrefixField()
     */
    public String getPrefixField() {
        return DbLanguageConstants.PREFIX_FIELD_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getPrefixTable()
     */
    public String getPrefixTable() {
        return DbLanguageConstants.PREFIX_TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getSuffixField()
     */
    public String getSuffixField() {
        return DbLanguageConstants.SUFFIX_FIELD_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getSuffixTable()
     */
    public String getSuffixTable() {
        return DbLanguageConstants.SUFFIX_TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.ILanguage#getTemplateTableVariable()
     */
    public String getTemplateTableVariable() {
        return DbLanguageConstants.TEMPLATE_TABLE_VARIABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbLanguage#getOperatorsManager()
     */
    public IDbOperatorManager getOperatorsManager() {
        return operatorsManager;
    }

}
