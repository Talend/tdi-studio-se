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
import org.talend.designer.core.ui.editor.properties.controllers.EncodingTypeController;

/**
 * This class is used for encoding type.
 *
 * $Id: EncodingTypeControllerGenerator.java 2007-2-11,10:20:13 ftang $
 *
 */
public class EncodingTypeControllerGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    public AbstractElementPropertySectionController generate() {
        return new EncodingTypeController(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#setDynamicProperty(org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty)
     */
    public void setDynamicProperty(IDynamicProperty dp) {
        this.dp = dp;
    }
}
