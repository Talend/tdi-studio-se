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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.core.model.process.IProcess2;

/**
 * Manages a job hierarchy, to keep it refreshed, and to allow it to be shared.
 */
public class JobHierarchyLifeCycle {

    private boolean fHierarchyRefreshNeeded;

    private JobHierarchy fHierarchy;

    private IProcess2 inputProcess;

    public JobHierarchyLifeCycle() {
    }

    public IProcess2 getInputProcess() {
        return this.inputProcess;
    }

    public void setInputProcess(IProcess2 inputProcess) {
        this.inputProcess = inputProcess;
    }

    public JobHierarchy getHierarchy() {
        return fHierarchy;
    }

    /**
     * DOC bqian Comment method "freeHierarchy".
     */
    public void freeHierarchy() {
        if (fHierarchy != null) {
            fHierarchy = null;
            inputProcess = null;
        }

    }

    public void ensureRefreshedTypeHierarchy(final IProcess2 element, IRunnableContext context) throws InvocationTargetException,
            InterruptedException {
        if (element == null) {
            freeHierarchy();
            return;
        }
        // boolean hierachyCreationNeeded = (fHierarchy == null || !element.equals(inputProcess));
        boolean hierachyCreationNeeded = true;

        if (hierachyCreationNeeded || fHierarchyRefreshNeeded) {

            IRunnableWithProgress op = new IRunnableWithProgress() {

                public void run(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
                    try {
                        doHierarchyRefresh(element, pm);
                    } catch (JavaModelException e) {
                        throw new InvocationTargetException(e);
                    } catch (OperationCanceledException e) {
                        throw new InterruptedException();
                    }
                }
            };
            fHierarchyRefreshNeeded = true;
            context.run(true, true, op);
            fHierarchyRefreshNeeded = false;
        }
    }

    public synchronized void doHierarchyRefresh(IProcess2 element, IProgressMonitor pm) throws JavaModelException {
        // to ensure the order of the two listeners always remove / add listeners on operations
        // on job hierarchies
        // if (fHierarchy != null) {
        // fHierarchy.removeTypeHierarchyChangedListener(this);
        // JavaCore.removeElementChangedListener(this);
        // }
        fHierarchy = createTypeHierarchy(element, pm);
        if (pm != null && pm.isCanceled()) {
            throw new OperationCanceledException();
        }
        inputProcess = element;

        // fHierarchy.addTypeHierarchyChangedListener(this);
        // JavaCore.addElementChangedListener(this);
        fHierarchyRefreshNeeded = false;
    }

    private JobHierarchy createTypeHierarchy(IProcess2 process, IProgressMonitor pm) throws JavaModelException {
        return new JobHierarchy(process);
    }
}
