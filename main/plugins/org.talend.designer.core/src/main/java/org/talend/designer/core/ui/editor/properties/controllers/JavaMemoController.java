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
package org.talend.designer.core.ui.editor.properties.controllers;

import org.talend.core.ui.properties.tab.IDynamicProperty;

/**
 * DOC ftang class global comment. Detailled comment <br/>
 *
 * $Id: JavaMemoController.java 2007-1-9,下午06:04:09 ftang $
 *
 */
public class JavaMemoController extends AbstractLanguageMemoController {

    public JavaMemoController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractLanguageMemoController#setLanguage()
     */
    protected void setLanguage() {
        this.setCurrentLanguage("java"); //$NON-NLS-1$
    }
}
