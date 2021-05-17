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
import org.talend.designer.core.generic.controller.GenericHiddenTextController;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator;

/**
 * created by hcyi on Feb 19, 2016 Detailled comment
 *
 */
public class GenericHiddenTextGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    @Override
    public AbstractElementPropertySectionController generate() {
        return new GenericHiddenTextController(dp);
    }

    @Override
    public void setDynamicProperty(IDynamicProperty dp) {
        this.dp = dp;
    }
}
