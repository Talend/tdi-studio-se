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
import org.talend.designer.core.ui.editor.properties.controllers.HadoopJarSetupController;

/**
 * DOC zwzhao class global comment. Detailled comment
 */
public class HadoopJarSetupGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#generate()
     */
    @Override
    public AbstractElementPropertySectionController generate() {
        return new HadoopJarSetupController(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#setDynamicProperty(org
     * .talend.core.properties.tab.IDynamicProperty)
     */
    @Override
    public void setDynamicProperty(IDynamicProperty dp) {
        this.dp = dp;
    }

}
