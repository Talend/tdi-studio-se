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
package org.talend.designer.abstractmap.ui.listener;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.talend.designer.abstractmap.managers.AbstractUIManager;

/**
 * cli class global comment. Detailled comment
 */
public class MouseScrolledListener implements MouseWheelListener {

    private static final int INCREMENT = 20;

    private ScrolledComposite scrolledComposite;

    private AbstractUIManager uiManager;

    public MouseScrolledListener(AbstractUIManager uiManager, ScrolledComposite scrolledComposite) {
        super();
        this.uiManager = uiManager;
        this.scrolledComposite = scrolledComposite;
    }

    public void addMouseWheelListener(Control comp) {
        if (comp != null && comp instanceof Composite) {
            comp.addMouseWheelListener(this);
            for (Control c : ((Composite) comp).getChildren()) {
                addMouseWheelListener(c);
            }
        }
    }

    public void removeMouseWheelListener(Control comp) {
        if (comp != null && comp instanceof Table) {
            comp.removeMouseWheelListener(this);
            for (Control c : ((Composite) comp).getChildren()) {
                removeMouseWheelListener(c);
            }
        }
    }

    public void mouseScrolled(MouseEvent e) {
        if (scrolledComposite != null && !scrolledComposite.isDisposed()) {
            Control content = scrolledComposite.getContent();
            if (content != null) {
                Point location = content.getLocation();
                ScrollBar vBar = scrolledComposite.getVerticalBar();
                if (e.count > 0) {
                    vBar.setSelection(vBar.getSelection() - INCREMENT);
                } else {
                    vBar.setSelection(vBar.getSelection() + INCREMENT);
                }
                int vSelection = vBar.getSelection();
                content.setLocation(location.x, -vSelection);
                uiManager.refreshBackground(true, false);
            }
        }
    }
}
