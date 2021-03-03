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
package org.talend.designer.xmlmap.parts.directedit;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlComboCellEditor extends ComboBoxCellEditor {

    @Override
    protected Control createControl(Composite parent) {
        Control control = super.createControl(parent);
        CCombo combo = (CCombo) control;
        combo.addSelectionListener(new SelectionAdapter() {

            public void widgetDefaultSelected(SelectionEvent event) {
                // applyEditorValueAndDeactivate();
            }

            public void widgetSelected(SelectionEvent event) {
                valueChanged(true, true);
            }
        });
        return control;
    }
}
