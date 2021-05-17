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
package org.talend.sqlbuilder.sqleditor;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Single Token SQL Scanner. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public final class SingleTokenSQLScanner extends AbstractSQLScanner {

    private String[] fProperty;

    public SingleTokenSQLScanner(IColorManager manager, IPreferenceStore store, String property) {
        super(manager, store);
        fProperty = new String[] { property };
        initialize();
    }

    /*
     * @see AbstractJavaScanner#getTokenProperties()
     */
    protected String[] getTokenProperties() {
        return fProperty;
    }

    /*
     * @see AbstractJavaScanner#createRules()
     */
    protected List createRules() {
        setDefaultReturnToken(getToken(fProperty[0]));
        return null;
    }
}
