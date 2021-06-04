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

import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.AbstractPropertiesTableEditorView;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;

/**
 * created by hcyi on Mar 31, 2021
 * Detailled comment
 *
 */
public abstract class AbstractTableController extends AbstractElementPropertySectionController {

    public AbstractTableController(IDynamicProperty dp) {
        super(dp);
    }

    protected abstract AbstractPropertiesTableEditorView getPropertiesTableEditorView(Composite parentComposite,
            int mainCompositeStyle, PropertiesTableEditorModel model, IElementParameter param, boolean toolbarVisible,
            boolean labelVisible);
}
