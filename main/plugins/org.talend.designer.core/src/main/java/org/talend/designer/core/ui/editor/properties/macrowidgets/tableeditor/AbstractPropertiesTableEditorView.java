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
package org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;

/**
 * created by hcyi on Mar 31, 2021
 * Detailled comment
 *
 */
public abstract class AbstractPropertiesTableEditorView<B> extends AbstractDataTableEditorView<B> {

    public AbstractPropertiesTableEditorView(Composite parent, int style, PropertiesTableEditorModel model) {
        super(parent, style, model);
    }

    public AbstractPropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle, PropertiesTableEditorModel model,
            boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, model, model.getElemParameter().isReadOnly(), toolbarVisible, labelVisible);
    }

    public AbstractPropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle,
            ExtendedTableModel<B> extendedTableModel, boolean readOnly, boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, extendedTableModel, readOnly, toolbarVisible, labelVisible, true);
    }

    public AbstractPropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    protected abstract Object getComboBoxCellEditorTypedValue(final TableViewerCreator<B> tableViewerCreator, IElement element,
            IElementParameter currentParam, CellEditor cellEditor, String currentKey, Object originalTypedValue);

    protected abstract void fillDefaultItemsList(IElementParameter currentParam, Object originalValue);

    public PropertiesTableToolbarEditorView getToolBar() {
        return (PropertiesTableToolbarEditorView) getExtendedToolbar();
    }
}
