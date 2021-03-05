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

import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.sdk.component.studio.ui.composite.controller.TaCoKitTextAreaSelectionController;

/**
 * created by hcyi on Jul 26, 2019
 * Detailled comment
 *
 */
public class TaCoKitTextAreaSelectionGenerator extends AbstractTaCoKitGenerator {

    @Override
    public AbstractElementPropertySectionController generate() {
        TaCoKitTextAreaSelectionController controller = new TaCoKitTextAreaSelectionController(getDynamicProperty());
        controller.setEditableTextArea(true);
        return controller;
    }
}