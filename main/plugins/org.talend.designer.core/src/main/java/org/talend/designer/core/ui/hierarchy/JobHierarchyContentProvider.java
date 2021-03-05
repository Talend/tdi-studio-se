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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.process.IProcess2;

/**
 * Base class for content providers for job hierarchy viewers.
 */
public abstract class JobHierarchyContentProvider implements ITreeContentProvider {

    protected static final Object[] NO_ELEMENTS = new Object[0];

    private JobHierarchyLifeCycle fJobHierarchy;

    public JobHierarchyContentProvider(JobHierarchyLifeCycle lifecycle) {
        fJobHierarchy = lifecycle;
    }

    public Object[] getChildren(Object element) {
        if (element instanceof IProcess2) {
            IProcess2 process = (IProcess2) element;

            List children = new ArrayList();
            addJobChildren(process, children);

            return children.toArray();
        }
        return NO_ELEMENTS;
    }

    public Object getParent(Object element) {
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element instanceof IProcess2) {
            IProcess2 type = (IProcess2) element;
            return hasJobChildren(type);
        }
        return false;
    }

    public Object[] getElements(Object inputElement) {
        ArrayList types = new ArrayList();
        getRootJobs(types);
        // for (int i = types.size() - 1; i >= 0; i--) {
        // IProcess2 curr = (IProcess2) types.get(i);
        // try {
        // if (!isInTree(curr)) {
        // types.remove(i);
        // }
        // } catch (JavaModelException e) {
        // // ignore
        // }
        // }
        return types.toArray();
    }

    protected void getRootJobs(List res) {
        JobHierarchy hierarchy = getHierarchy();
        if (hierarchy != null) {
            IProcess2 process = hierarchy.getProcess();
            if (process != null) {
                res.add(process);
            }
        }
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }

    protected final JobHierarchy getHierarchy() {
        return fJobHierarchy.getHierarchy();
    }

    private void addJobChildren(IProcess2 process, List children) {
        ArrayList types = new ArrayList();
        getJobsInHierarchy(process, types);
        // int len = types.size();
        // for (int i = 0; i < len; i++) {
        // IJob curr = (IJob) types.get(i);
        // if (isInTree(curr)) {
        // children.add(curr);
        // }
        // }
        children.addAll(types);

    }

    /**
     * DOC bqian Comment method "hasJobChildren".
     *
     * @param type
     * @return
     */
    private boolean hasJobChildren(IProcess2 type) {
        ArrayList types = new ArrayList();
        getJobsInHierarchy(type, types);
        return types.size() > 0;
    }

    /**
     * Hook to overwrite. Filter will be applied on the returned jobs
     *
     * @param type the type
     * @param res all types in the hierarchy of the given type
     */
    protected abstract void getJobsInHierarchy(IProcess2 process, List res);
}
