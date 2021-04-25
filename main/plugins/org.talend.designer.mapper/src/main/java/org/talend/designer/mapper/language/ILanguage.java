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
package org.talend.designer.mapper.language;

import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public interface ILanguage {

    public ECodeLanguage getCodeLanguage();

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

    public String getTemplateGeneratedCodeTableColumnVariableWithComponentNamePrefix();

    /**
     * DOC amaumont Comment method "getAndCondition".
     *
     * @return
     */
    public String getAndCondition();

    public String getLocation(String tableName, String columnName);

    public String getLocation(String tableName);

    public ICodeProblemsChecker getCodeChecker();

}
