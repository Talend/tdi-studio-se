// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import org.talend.sdk.component.studio.ui.composite.controller.SuggestableTableController;

/**
 * Purpose of this class is to map EParameterFieldType.TACOKIT_SUGGESTABLE_TABLE widget type with
 * SuggestableTableController class, which is responsible for UI elements creation.
 * <p>
 * Mapping is done in plugin.xml file in "org.talend.designer.core.generators" extension point
 */
public class SuggestableTableGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#generate()
     */
    @Override
    public AbstractElementPropertySectionController generate() {
        return new SuggestableTableController(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#setDynamicProperty(org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty)
     */
    @Override
    public void setDynamicProperty(final IDynamicProperty dp) {
        this.dp = dp;
    }
}
