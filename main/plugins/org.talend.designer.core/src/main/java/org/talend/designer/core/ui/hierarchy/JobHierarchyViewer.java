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
package org.talend.designer.core.ui.hierarchy;

import org.eclipse.jdt.internal.ui.viewsupport.ProblemTreeViewer;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC bqian class global comment. Detailled comment
 */
public abstract class JobHierarchyViewer extends ProblemTreeViewer {

    private JobHierarchyLabelProvider fLabelProvider;

    public JobHierarchyViewer(Composite parent, IContentProvider contentProvider, JobHierarchyLifeCycle lifeCycle,
            IWorkbenchPart part) {
        super(new Tree(parent, SWT.SINGLE));

        fLabelProvider = new JobHierarchyLabelProvider(lifeCycle);

        setLabelProvider(fLabelProvider);
        setUseHashlookup(true);

        setContentProvider(contentProvider);
        // setComparator(new HierarchyViewerSorter(lifeCycle));

        // fOpen = new OpenAction(part.getSite());
        // addOpenListener(new IOpenListener() {
        //
        // public void open(OpenEvent event) {
        // fOpen.run();
        // }
        // });
    }

    /**
     * Returns true if the hierarchy contains elements. Returns one of them With member filtering it is possible that no
     * elements are visible
     *
     * @return one of the elements contained
     */
    public Object containsElements() {
        JobHierarchyContentProvider contentProvider = getHierarchyContentProvider();
        if (contentProvider != null) {
            Object[] elements = contentProvider.getElements(null);
            if (elements.length > 0) {
                return elements[0];
            }
        }
        return null;
    }

    /**
     * Attaches a contextmenu listener to the tree
     *
     * @param menuListener the menu listener
     * @param popupId the popup id
     * @param viewSite the view site
     */
    public void initContextMenu(IMenuListener menuListener, IWorkbenchPartSite viewSite) {
        String popupId = "JobHierarchyViewer_ContextMenu"; //$NON-NLS-1$
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(menuListener);
        Menu menu = menuMgr.createContextMenu(getTree());
        getTree().setMenu(menu);
        viewSite.registerContextMenu(popupId, menuMgr, this);
    }

    /**
     * DOC bqian Comment method "getHierarchyContentProvider".
     *
     * @return
     */
    private JobHierarchyContentProvider getHierarchyContentProvider() {
        return (JobHierarchyContentProvider) getContentProvider();
    }

    /**
     * Returns the title for the current view
     *
     * @return the title
     */
    public String getTitle() {
        return Messages.getString("JobHierarchyViewer.hirarchy"); //$NON-NLS-1$
    }

    /*
     * @see TypeHierarchyViewer#updateContent
     */
    public abstract void updateContent(boolean expand);

}
