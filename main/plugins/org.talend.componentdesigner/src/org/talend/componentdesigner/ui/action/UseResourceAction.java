// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.action;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.ui.composite.ILibListViewer;

/**
 * Action used with a runtime classpath viewer.
 */
public abstract class UseResourceAction extends SelectionListenerAction {

    public static final int DEFAULT = 0;

    public static final int ADD = 1;

    public static final int REMOVE = 2;

    private ILibListViewer fViewer;

    private Button fButton;

    private Shell fShell;

    public UseResourceAction(String label, ILibListViewer viewer) {
        super(label);
        setViewer(viewer);
    }

    /**
     * Sets the viewer on which this action operates.
     * 
     * @param viewer the viewer on which this action operates
     */
    public void setViewer(ILibListViewer viewer) {
        if (fViewer != null) {
            fViewer.removeSelectionChangedListener(this);
        }
        fViewer = viewer;
        if (fViewer != null) {
            fViewer.addSelectionChangedListener(this);
            update();
        }
    }

    /**
     * Returns the viewer on which this action operates.
     * 
     * @return the viewer on which this action operates
     */
    protected ILibListViewer getViewer() {
        return fViewer;
    }

    /**
     * Updates the entries to the entries in the given list.
     */
    protected void setEntries(List<ILibEntry> list) {
        getViewer().setEntries((ILibEntry[]) list.toArray(new ILibEntry[list.size()]));
        // update all selection listeners
        getViewer().setSelection(getViewer().getSelection());
    }

    /**
     * Sets the button that invokes this action.
     */
    public void setButton(Button button) {
        fButton = button;
        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent evt) {
                run();
            }
        });
        // fButton.setEnabled(false);
    }

    /**
     * @see IAction#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (fButton != null) {
            fButton.setEnabled(enabled);
        }
    }

    /**
     * Updates the enabled state.
     */
    protected void update() {
        selectionChanged((IStructuredSelection) getViewer().getSelection());
    }

    /**
     * Returns the shell used to realize this action's dialog (if any).
     */
    protected Shell getShell() {
        if (fShell == null) {
            fShell = getViewer().getShell();
        }
        return fShell;
    }

    /**
     * Sets the shell used to realize this action's dialog (if any).
     */
    public void setShell(Shell shell) {
        fShell = shell;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public boolean updateSelection(IStructuredSelection selection) {
        return getViewer().updateSelection(getActionType(), selection);
    }

    protected int getActionType() {
        return DEFAULT;
    }
}
