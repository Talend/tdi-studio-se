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

import org.talend.commons.utils.data.text.StringHelper;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class AbstractLanguage implements ILanguage {

    public static final String CARRIAGE_RETURN = "\n"; //$NON-NLS-1$

    public static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private ECodeLanguage codeLanguage;

    protected ICodeProblemsChecker codeChecker;

    /**
     * DOC amaumont AbstractLanguage constructor comment.
     *
     * @param perl
     */
    public AbstractLanguage(ECodeLanguage codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getLocation(org.talend.designer.mapper.model.tableentry.TableEntryLocation)
     */
    public String getLocation(String tableName, String columnName) {
        return StringHelper.replacePrms(getTemplateTableColumnVariable(), new Object[] { EMPTY_STRING, tableName,
                columnName });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getLocation(java.lang.String)
     */
    public String getLocation(String tableName) {
        return StringHelper.replacePrms(getTemplateTableVariable(), new Object[] { tableName });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getCodeLanguage()
     */
    public ECodeLanguage getCodeLanguage() {
        return codeLanguage;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.language.ILanguage#getCodeChecker()
     */
    public ICodeProblemsChecker getCodeChecker() {
        return codeChecker;
    }

}
