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
package org.talend.designer.core.generic.controller.generator;

import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.generic.controller.ComponentRefController;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator;

/**
 *
 * created by ycbai on 2015年9月28日 Detailled comment
 *
 */
public class ComponentRefGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    @Override
    public AbstractElementPropertySectionController generate() {
        return new ComponentRefController(dp);
    }

    @Override
    public void setDynamicProperty(IDynamicProperty dp) {
        this.dp = dp;
    }
}
