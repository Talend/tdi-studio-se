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

package org.talend.componentdesigner.ui.composite;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.talend.componentdesigner.model.ILibEntry;

/**
 * Defines a classpath viewer to abstract between classpath viewers that are tree or table viewers.
 */
public interface ILibListViewer extends ISelectionProvider {

    /**
     * Returns the entries in this viewer that are the children of the parent element. associated with the selected
     * item(s)
     * 
     * @return the entries in this viewer
     */
    public ILibEntry[] getEntries();

    /**
     * Sets the entries in this viewer to the given runtime classpath. entries
     * 
     * @param entries runtime classpath entries
     */
    public void setEntries(ILibEntry[] entries);

    /**
     * Returns the shell associated with this viewer.
     * 
     * @return a shell
     */
    public Shell getShell();

    /**
     * Returns whether this viewer is enabled.
     * 
     * @return whether this viewer is enabled
     */
    public boolean isEnabled();

    /**
     * Adds the given entries to the list. If there is no selection in the list, the entries are added at the end of the
     * list, otherwise the new entries are added before the (first) selected entry. The new entries are selected.
     * 
     * @param entries additions
     */
    public void addEntries(ILibEntry[] entries);

    public void removeEntries(ILibEntry[] entries);

    /**
     * Refreshes this entry in the viewer.
     * 
     * @param entry the entry to be refreshed
     */
    public void refresh(Object entry);

    /**
     * The entries in a runtime classpath entry viewer have changed in some way. Calling this method allows the viewer
     * to adapt to those changes if necessary.
     */
    public void notifyChanged();

    /**
     * Returns the index of an equivalent entry, or -1 if none.
     * 
     * @return the index of an equivalent entry, or -1 if none
     */
    public int indexOf(ILibEntry entry);

    /**
     * Returns whether an action of the supplied action type should be enabled based on the supplied selection.
     * 
     * @param actionType One of RuntimeClasspathAction constants defining the action type
     * @param selection The selection to use for the update
     * @return Whether the action of this type should be enabled based on the selection
     */
    public boolean updateSelection(int actionType, IStructuredSelection selection);

    ILibEntry[] getSelectedEntries();
}
