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

import java.util.List;

import org.talend.core.language.ICodeProblemsChecker;
import org.talend.designer.dbmap.language.operator.IDbOperatorManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ILanguage.java 1877 2007-02-06 17:16:43Z amaumont $
 *
 */
public interface IDbLanguage {

    public String getLocationPattern();

    public String getPrefixField();

    public String getPrefixFieldRegexp();

    public String getPrefixTable();

    public String getPrefixTableRegexp();

    public String getSuffixField();

    public String getSuffixFieldRegexp();

    public String getSuffixTable();

    public String getSuffixTableRegexp();

    public String getSubstPatternForPrefixColumnName();

    public String getSubstPatternForReplaceLocation();

    public String getTemplateTableColumnVariable();

    public String getTemplateTableVariable();

    public String getTemplateVarsColumnVariable();

    public String getTemplateGeneratedCodeTableColumnVariable();

    /**
     * DOC amaumont Comment method "getAndCondition".
     *
     * @return
     */
    public String getAndCondition();

    public String getLocation(String tableName, String columnName);

    public String getLocation(String tableName);

    public ICodeProblemsChecker getCodeChecker();

    public abstract IJoinType[] getAvailableJoins();

    public abstract IJoinType getJoin(String joinType);

    public abstract IDbOperatorManager getOperatorsManager();

    public List<IJoinType> unuseWithExplicitJoin();
}
