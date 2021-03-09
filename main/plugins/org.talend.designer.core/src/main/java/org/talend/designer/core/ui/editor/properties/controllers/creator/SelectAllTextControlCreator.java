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
package org.talend.designer.core.ui.editor.properties.controllers.creator;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * An {@link IControlCreator} for SWT Text controls. <br>
 * This is a convenience class for creating text controls to be supplied to a decorated field.<br>
 * And this class implements to select all(hot key: Ctrl+A) contents of the Text control.
 *
 * @since 3.2
 * @deprecated As of 3.3, clients should use {@link ControlDecoration} instead of {@link DecoratedField}.
 */
public class SelectAllTextControlCreator extends TextControlCreator {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.TextControlCreator#createControl(org.eclipse.swt.widgets.Composite, int)
     */
    @Override
    public Control createControl(Composite parent, int style) {
        Text text = new Text(parent, style);
        addSelectAllListener(text);
        return text;
    }

    /**
     *
     * DOC ggu Comment method "addSelectAllListener".
     *
     * @param text
     */
    private void addSelectAllListener(final Text text) {
        text.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                if (event.character == '\u0001') { // CTRL + A
                    text.selectAll();
                }
            }
        });
    }
}
