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
package org.talend.designer.mapper.language.generation;

import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.java.JavaLanguage;
import org.talend.designer.mapper.language.perl.PerlLanguage;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class GenerationManagerFactory {

    private static GenerationManagerFactory instance;

    public static GenerationManagerFactory getInstance() {
        if (instance == null) {
            instance = new GenerationManagerFactory();
        }
        return instance;
    }

    public GenerationManager getGenerationManager(ILanguage language) {
        GenerationManager gen = null;
        if (language instanceof PerlLanguage) {
            gen = new PerlGenerationManager(language);
        } else if (language instanceof JavaLanguage) {
            gen = new JavaGenerationManager(language);
        } else {
            throw new IllegalArgumentException("Case not found !"); //$NON-NLS-1$
        }
        return gen;
    }

}
