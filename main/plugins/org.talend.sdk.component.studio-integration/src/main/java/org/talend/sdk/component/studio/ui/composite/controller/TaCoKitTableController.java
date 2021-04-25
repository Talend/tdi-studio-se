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
package org.talend.sdk.component.studio.ui.composite.controller;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.TableController;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.AbstractPropertiesTableEditorView;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;
import org.talend.sdk.component.studio.metadata.tableeditor.TaCoKitPropertiesTableEditorView;

/**
 * created by hcyi on Mar 16, 2021
 * Detailled comment
 *
 */
public class TaCoKitTableController extends TableController {

    public TaCoKitTableController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Control createControl(final Composite parentComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, int top, final Control lastControlPrm) {
        return super.createControl(parentComposite, param, numInRow, nbInRow, top, lastControlPrm);
    }

    @Override
    protected AbstractPropertiesTableEditorView getPropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle,
            PropertiesTableEditorModel tableEditorModel, IElementParameter param, boolean toolbarVisible, boolean labelVisible) {
        return new TaCoKitPropertiesTableEditorView<Map<String, Object>>(parentComposite, SWT.NONE, tableEditorModel,
                !param.isBasedOnSchema(), false);
    }
}
