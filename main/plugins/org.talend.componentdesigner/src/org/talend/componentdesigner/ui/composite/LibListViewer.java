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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.ui.action.UseResourceAction;

/**
 * A viewer that displays and manipulates runtime classpath entries.
 */
public class LibListViewer extends TableViewer implements ILibListViewer {

    private List<ILibEntry> existingEntries = new ArrayList<ILibEntry>();

    private ComponentPref componentPrefBean;

    // private ILibEntry fCurrentParent= null;

    /**
     * Creates a runtime classpath viewer with the given parent.
     * 
     * @param parent the parent control
     */
    public LibListViewer(Composite parent) {
        super(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#setEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
     */
    public void setEntries(ILibEntry[] entries) {
        if (entries == null) {
            return;
        }
        // this.existingEntries = Arrays.asList(entries);
        this.existingEntries.clear();
        for (ILibEntry entry : entries) {
            if (entry != null) {
                existingEntries.add(entry);
            }
        }
        notifyChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getEntries()
     */
    public ILibEntry[] getEntries() {
        ILibEntry[] entrys = new ILibEntry[existingEntries.size()];
        return (ILibEntry[]) existingEntries.toArray(entrys);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#addEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
     */
    public void addEntries(ILibEntry[] entries) {
        for (int i = 0; i < entries.length; i++) {
            existingEntries.add(entries[i]);
        }
        notifyChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#isEnabled()
     */
    public boolean isEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#notifyChanged()
     */
    public void notifyChanged() {
        this.setInput(this.existingEntries);
        ILibEntry[] array = new ILibEntry[this.existingEntries.size()];
        componentPrefBean.setLibEntries(existingEntries.toArray(array));
        // this.propertyChangeBean.firePropertyChange(PluginConstant.LIBRARY_PROPERTY, null,
        // existingEntries.toArray(array));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getShell()
     */
    public Shell getShell() {
        return getControl().getShell();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#updateSelection(int,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public boolean updateSelection(int actionType, IStructuredSelection selection) {
        switch (actionType) {
        case UseResourceAction.ADD:
            break;
        case UseResourceAction.REMOVE:
            return selection != null && selection.size() > 0;
        default:
            break;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getSelectedEntries()
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public ILibEntry[] getSelectedEntries() {
        IStructuredSelection selection = (IStructuredSelection) getSelection();
        List<ILibEntry> entries = new ArrayList<ILibEntry>(selection.size() * 2);
        Iterator<ILibEntry> itr = selection.iterator();
        while (itr.hasNext()) {
            ILibEntry element = (ILibEntry) itr.next();
            entries.add(element);
        }
        ILibEntry[] array = new ILibEntry[entries.size()];
        return entries.toArray(array);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.ui.composite.IClasspathViewer#indexOf(org.talend.componentdesigner.model.ILibEntry)
     */
    public int indexOf(ILibEntry entry) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.componentdesigner.ui.composite.ILibListViewer#removeEntries(org.talend.componentdesigner.model.ILibEntry[])
     */
    public void removeEntries(ILibEntry[] entries) {
        for (ILibEntry libEntry : entries) {
            if (existingEntries.contains(libEntry)) {
                this.existingEntries.remove(libEntry);
            }
        }
        this.notifyChanged();
    }

    public void setConponentPrfBean(ComponentPref componentPrfBean) {
        this.componentPrefBean = componentPrfBean;
    }
}
