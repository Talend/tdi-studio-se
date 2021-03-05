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

import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.designer.mapper.MapperMain;
import org.talend.designer.mapper.language.java.JavaLanguage;
import org.talend.designer.mapper.language.perl.PerlLanguage;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class LanguageProvider {

    private static ILanguage language;

    public static ILanguage getCurrentLanguage() {
        ECodeLanguage codeLanguage = null;
        if (!MapperMain.isStandAloneMode()) {
            RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                    Context.REPOSITORY_CONTEXT_KEY);
            codeLanguage = repositoryContext.getProject().getLanguage();
        } else {
            codeLanguage = ECodeLanguage.PERL;
        }
        switch (codeLanguage) {
        case JAVA:
            if (!(language instanceof JavaLanguage) || LanguageProvider.language == null) {
                LanguageProvider.language = getJavaLanguage();
            }
            break;
        default:
            if (!(language instanceof PerlLanguage) || LanguageProvider.language == null) {
                LanguageProvider.language = getPerlLanguage();
            }
        }
        return LanguageProvider.language;
    }

    public static ILanguage getPerlLanguage() {
        return new PerlLanguage();
    }

    public static ILanguage getJavaLanguage() {
        return new JavaLanguage();
    }

}
