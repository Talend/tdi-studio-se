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
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: LanguageMemoController.java 1 2006-12-12 下午02:48:54 +0000 (下午02:48:54) yzhang $
 *
 */
public class PerlMemoController extends AbstractLanguageMemoController {

    public PerlMemoController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    protected void setLanguage() {
        this.setCurrentLanguage("perl"); //$NON-NLS-1$
    }

}
