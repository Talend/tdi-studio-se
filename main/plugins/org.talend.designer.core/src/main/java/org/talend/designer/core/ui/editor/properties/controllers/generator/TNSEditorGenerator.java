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
package org.talend.designer.core.ui.editor.properties.controllers.generator;

import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.TNSEditorController;

/**
 *
 * rshi class global comment. Detailled comment
 */
public class TNSEditorGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    public AbstractElementPropertySectionController generate() {
        // TODO Auto-generated method stub
        return new TNSEditorController(dp);
    }

    public void setDynamicProperty(IDynamicProperty dp) {
        // TODO Auto-generated method stub
        this.dp = dp;
    }

}
