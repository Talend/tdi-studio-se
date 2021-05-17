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
 * A viewer including the content provider for the subjob hierarchy. Used by the JobHierarchyViewPart which has to
 * provide a JobHierarchyLifeCycle on construction
 */
public class SubJobHierarchyViewer extends JobHierarchyViewer {

    public SubJobHierarchyViewer(Composite parent, JobHierarchyLifeCycle lifeCycle, IWorkbenchPart part) {
        super(parent, new SubJobHierarchyContentProvider(lifeCycle), lifeCycle, part);
    }

    /*
     * @see TypeHierarchyViewer#updateContent
     */
    public void updateContent(boolean expand) {
        getTree().setRedraw(false);
        refresh();

        if (expand) {
             int expandLevel = 3;
             expandToLevel(expandLevel);
        }
        getTree().setRedraw(true);
    }

    /**
     * Content provider for the 'traditional' job hierarchy.
     */
    public static class SubJobHierarchyContentProvider extends JobHierarchyContentProvider {

        public SubJobHierarchyContentProvider(JobHierarchyLifeCycle provider) {
            super(provider);
        }

        protected final void getJobsInHierarchy(IProcess2 type, List res) {
            JobHierarchy hierarchy = getHierarchy();
            if (hierarchy != null) {
                IProcess2[] types = hierarchy.getSubtypes(type);
                for (int i = 0; i < types.length; i++) {
                    res.add(types[i]);
                }
            }
        }
    }

}
