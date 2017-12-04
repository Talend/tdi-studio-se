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
package org.talend.componentdesigner.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is a
 * guarantee neither that this API will work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class ResourceExtensionContentProvider extends WorkbenchContentProvider {

    private static final Object[] NO_CHILDREN = new Object[0];

    private Viewer viewer;

    /**
     * 
     */
    public ResourceExtensionContentProvider() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object element) {
        return super.getChildren(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object element) {
        if (element instanceof IResource) {
            return super.getChildren(element);
        }
        return NO_CHILDREN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        return element instanceof IContainer && super.hasChildren(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.WorkbenchContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     * java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        super.inputChanged(viewer, oldInput, newInput);
        this.viewer = viewer;
    }

    /**
     * Process the resource delta.
     * 
     * @param delta
     */
    protected void processDelta(IResourceDelta delta) {

        Control ctrl = viewer.getControl();
        if (ctrl == null || ctrl.isDisposed()) {
            return;
        }

        final Collection runnables = new ArrayList();
        processDelta(delta, runnables);

        if (runnables.isEmpty()) {
            return;
        }

        // Are we in the UIThread? If so spin it until we are done
        if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
            runUpdates(runnables);
        } else {
            ctrl.getDisplay().asyncExec(new Runnable() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    // Abort if this happens after disposes
                    Control ctrl = viewer.getControl();
                    if (ctrl == null || ctrl.isDisposed()) {
                        return;
                    }

                    runUpdates(runnables);
                }
            });
        }

    }

    /**
     * Process a resource delta. Add any runnables
     */
    private void processDelta(IResourceDelta delta, Collection runnables) {
        // he widget may have been destroyed
        // by the time this is run. Check for this and do nothing if so.
        Control ctrl = viewer.getControl();
        if (ctrl == null || ctrl.isDisposed()) {
            return;
        }

        // Get the affected resource
        final IResource resource = delta.getResource();

        // If any children have changed type, just do a full refresh of this
        // parent,
        // since a simple update on such children won't work,
        // and trying to map the change to a remove and add is too dicey.
        // The case is: folder A renamed to existing file B, answering yes to
        // overwrite B.
        IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED);
        for (int i = 0; i < affectedChildren.length; i++) {
            if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
                runnables.add(getRefreshRunnable(resource));
                return;
            }
        }

        // Check the flags for changes the Navigator cares about.
        // See ResourceLabelProvider for the aspects it cares about.
        // Notice we don't care about F_CONTENT or F_MARKERS currently.
        int changeFlags = delta.getFlags();
        if ((changeFlags & (IResourceDelta.OPEN | IResourceDelta.SYNC | IResourceDelta.TYPE | IResourceDelta.DESCRIPTION)) != 0) {
            // Runnable updateRunnable = new Runnable(){
            // public void run() {
            // ((StructuredViewer) viewer).update(resource, null);
            // }
            // };
            // runnables.add(updateRunnable);

            /*
             * support the Closed Projects filter; when a project is closed, it may need to be removed from the view.
             */
            runnables.add(getRefreshRunnable(resource.getParent()));
        }
        // Replacing a resource may affect its label and its children
        if ((changeFlags & IResourceDelta.REPLACED) != 0) {
            runnables.add(getRefreshRunnable(resource));
            return;
        }

        // Handle changed children .
        for (int i = 0; i < affectedChildren.length; i++) {
            processDelta(affectedChildren[i], runnables);
        }

        // @issue several problems here:
        // - should process removals before additions, to avoid multiple equal
        // elements in viewer
        // - Kim: processing removals before additions was the indirect cause of
        // 44081 and its varients
        // - Nick: no delta should have an add and a remove on the same element,
        // so processing adds first is probably OK
        // - using setRedraw will cause extra flashiness
        // - setRedraw is used even for simple changes
        // - to avoid seeing a rename in two stages, should turn redraw on/off
        // around combined removal and addition
        // - Kim: done, and only in the case of a rename (both remove and add
        // changes in one delta).

        IResourceDelta[] addedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
        IResourceDelta[] removedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);

        if (addedChildren.length == 0 && removedChildren.length == 0) {
            return;
        }

        final Object[] addedObjects;
        final Object[] removedObjects;

        // Process additions before removals as to not cause selection
        // preservation prior to new objects being added
        // Handle added children. Issue one update for all insertions.
        int numMovedFrom = 0;
        int numMovedTo = 0;
        if (addedChildren.length > 0) {
            addedObjects = new Object[addedChildren.length];
            for (int i = 0; i < addedChildren.length; i++) {
                addedObjects[i] = addedChildren[i].getResource();
                if ((addedChildren[i].getFlags() & IResourceDelta.MOVED_FROM) != 0) {
                    ++numMovedFrom;
                }
            }
        } else {
            addedObjects = new Object[0];
        }

        // Handle removed children. Issue one update for all removals.
        if (removedChildren.length > 0) {
            removedObjects = new Object[removedChildren.length];
            for (int i = 0; i < removedChildren.length; i++) {
                removedObjects[i] = removedChildren[i].getResource();
                if ((removedChildren[i].getFlags() & IResourceDelta.MOVED_TO) != 0) {
                    ++numMovedTo;
                }
            }
        } else {
            removedObjects = new Object[0];
        }
        // heuristic test for items moving within same folder (i.e. renames)
        final boolean hasRename = numMovedFrom > 0 && numMovedTo > 0;

        Runnable addAndRemove = new Runnable() {

            public void run() {
                if (viewer instanceof AbstractTreeViewer) {
                    AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
                    // Disable redraw until the operation is finished so we don't
                    // get a flash of both the new and old item (in the case of
                    // rename)
                    // Only do this if we're both adding and removing files (the
                    // rename case)
                    if (hasRename) {
                        treeViewer.getControl().setRedraw(false);
                    }
                    try {
                        if (addedObjects.length > 0) {
                            treeViewer.add(resource, addedObjects);
                        }
                        if (removedObjects.length > 0) {
                            treeViewer.remove(removedObjects);
                        }
                    } finally {
                        if (hasRename) {
                            treeViewer.getControl().setRedraw(true);
                        }
                    }
                } else {
                    ((StructuredViewer) viewer).refresh(resource);
                }
            }
        };
        runnables.add(addAndRemove);
    }

    /**
     * Return a runnable for refreshing a resource.
     * 
     * @param resource
     * @return Runnable
     */
    private Runnable getRefreshRunnable(final IResource resource) {
        return new Runnable() {

            public void run() {
                ((StructuredViewer) viewer).refresh(resource);
            }
        };
    }

    /**
     * Run all of the runnables that are the widget updates.
     * 
     * @param runnables
     */
    private void runUpdates(Collection runnables) {
        Iterator runnableIterator = runnables.iterator();
        while (runnableIterator.hasNext()) {
            ((Runnable) runnableIterator.next()).run();
        }

    }

}
