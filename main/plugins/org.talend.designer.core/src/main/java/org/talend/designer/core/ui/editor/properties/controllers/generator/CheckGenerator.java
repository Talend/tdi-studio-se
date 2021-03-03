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
import org.talend.designer.core.ui.editor.properties.controllers.CheckController;
import org.talend.designer.core.ui.editor.properties.controllers.tdq.ControllerUtils;
import org.talend.designer.core.ui.editor.properties.controllers.tdq.TGenKeyCheckController;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: CheckGenerator.java 1 2006-12-22 下午03:29:50 +0000 (下午03:29:50) yzhang $
 *
 */
public class CheckGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#generate()
     */
    @Override
    public AbstractElementPropertySectionController generate() {
        if (ControllerUtils.isFromTGenKey(dp.getElement())) {
            return new TGenKeyCheckController(dp);
        } else {
            return new CheckController(dp);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#setDynamicProperty(org
     * .talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty)
     */
    @Override
    public void setDynamicProperty(IDynamicProperty dp) {
        this.dp = dp;
    }
}
