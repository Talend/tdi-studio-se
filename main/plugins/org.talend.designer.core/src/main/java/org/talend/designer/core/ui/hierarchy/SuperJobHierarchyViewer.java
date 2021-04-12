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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.process.IProcess2;

/**
 * A viewer including the content provider for the superjob hierarchy. Used by the JobHierarchyViewPart which has to
 * provide a JobHierarchyLifeCycle on construction
 */
public class SuperJobHierarchyViewer extends JobHierarchyViewer {

    public SuperJobHierarchyViewer(Composite parent, JobHierarchyLifeCycle lifeCycle, IWorkbenchPart part) {
        super(parent, new SuperJobHierarchyContentProvider(lifeCycle), lifeCycle, part);
    }

    /*
     * @see TypeHierarchyViewer#updateContent
     */
    public void updateContent(boolean expand) {
        getTree().setRedraw(false);
        refresh();
        if (expand) {
            expandAll();
        }
        getTree().setRedraw(true);
    }

    /**
     * Content provider for the 'traditional' job hierarchy.
     */
    public static class SuperJobHierarchyContentProvider extends JobHierarchyContentProvider {

        public SuperJobHierarchyContentProvider(JobHierarchyLifeCycle provider) {
            super(provider);
        }

        protected void getJobsInHierarchy(IProcess2 process, List res) {

        }
    }

}
