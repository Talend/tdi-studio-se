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
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.utils.DesignerUtilities;

/**
 * A job hierarchy provides navigations between a job and its resolved superjobs and subjobs for a specific job.
 */
public class JobHierarchy {

    /**
     * The job the hierarchy was specifically computed for, possibly null.
     */
    protected IProcess2 focusProcess;

    /**
     * DOC bqian JobHierarchy constructor comment.
     */
    public JobHierarchy(IProcess2 process) {
        this.focusProcess = process;
    }

    public IProcess2 getProcess() {
        return focusProcess;
    }

    /**
     * DOC bqian Comment method "getSubtypes".
     *
     * @param type
     * @return
     */
    public IProcess2[] getSubtypes(IProcess process) {
        List<INode> tRunjobs = DesignerUtilities.getTRunjobs(process);

        List<IProcess2> jobs = new ArrayList<IProcess2>(tRunjobs.size());
        for (INode node : tRunjobs) {
            IProcess2 job = DesignerUtilities.getCorrespondingProcessFromTRunjob(node);
            if (job != null) {
                jobs.add(job);
            }
        }

        return jobs.toArray(new IProcess2[0]);
    }

    public Collection<IRepositoryViewObject> getContextDependencies(IProcess2 process) {
        List<Item> items = new ArrayList<Item>(1);
        try {
            process.setProperty(ProxyRepositoryFactory.getInstance().getUptodateProperty(process.getProperty()));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        items.add(process.getProperty().getItem());
        return ProcessUtils.getProcessDependencies(ERepositoryObjectType.CONTEXT, items);
    }

    public Collection<IRepositoryViewObject> getMetadataDependencies(IProcess2 process) {
        List<Item> items = new ArrayList<Item>(1);
        items.add(process.getProperty().getItem());
        return ProcessUtils.getProcessDependencies(ERepositoryObjectType.METADATA, items);
    }

    /**
     * DOC bqian Comment method "refresh".
     *
     * @param pm
     */
    public synchronized void refresh(IProgressMonitor monitor) {
        // TODO Auto-generated method stub
    }
}
