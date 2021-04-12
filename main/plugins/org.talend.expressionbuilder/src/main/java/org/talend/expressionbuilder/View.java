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
package org.talend.expressionbuilder;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: View.java 上午10:05:45 2007-7-24 +0000 (2007-7-24) yzhang $
 *
 */
public class View extends ViewPart {

    public static final String ID = "org.talend.expressionbuilder.view"; //$NON-NLS-1$

    private TableViewer viewer;

    /**
     * The content provider class is responsible for providing objects to the view. It can wrap existing objects in
     * adapters or simply return objects as-is. These objects may be sensitive to the current input of the view, or
     * ignore it and always show the same content (like Task List, for example).
     */
    class ViewContentProvider implements IStructuredContentProvider {

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            return new String[] { "One", "Two", "Three" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * yzhang View class global comment. Detailled comment <br/>
     *
     * $Id: View.java 上午10:05:55 2007-7-24 +0000 (2007-7-24) yzhang $
     *
     */
    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }

        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }

        public Image getImage(Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setInput(getViewSite());

        Action action = new Action() {

            public void run() {
                // ExpressionBuilderDialog dialog = new ExpressionBuilderDialog(View.this.getViewSite().getShell());
                // dialog.open();
                // dialog.setBlockOnOpen(true);
            }
        };
        action.setText("expression"); //$NON-NLS-1$
        this.getViewSite().getActionBars().getToolBarManager().add(action);

    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

}
