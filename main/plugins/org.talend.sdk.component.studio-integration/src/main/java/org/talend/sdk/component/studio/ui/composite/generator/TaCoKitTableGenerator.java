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
package org.talend.sdk.component.studio.ui.composite.generator;

import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator;
import org.talend.sdk.component.studio.ui.composite.controller.TaCoKitTableController;

/**
 * created by hcyi on Mar 16, 2021
 * Detailled comment
 *
 */
public class TaCoKitTableGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#generate()
     */
    @Override
    public AbstractElementPropertySectionController generate() {
        return new TaCoKitTableController(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#setDynamicProperty(org.
     * talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty)
     */
    @Override
    public void setDynamicProperty(final IDynamicProperty dp) {
        this.dp = dp;
    }
}
