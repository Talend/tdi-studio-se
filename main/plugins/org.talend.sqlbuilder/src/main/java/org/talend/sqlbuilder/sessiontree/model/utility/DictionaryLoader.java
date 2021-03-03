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
package org.talend.sqlbuilder.sessiontree.model.utility;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * DOC qianbing class global comment. Detailled comment <br/>
 *
 * $Id: DictionaryLoader.java,v 1.3 2006/11/01 07:49:10 peiqin.hou Exp $
 *
 */
public class DictionaryLoader extends Job {

    private static final String ID = "net.sourceforge.sqlexplorer"; //$NON-NLS-1$

    /**
     * Hidden constructor.
     */
    private DictionaryLoader() {
        super(null);
    }

    /**
     * Default constructor.
     *
     * @param sessionNode SessionTreeNode
     */
    public DictionaryLoader(SessionTreeNode sessionNode) {
        super(Messages.getString("Progress.Dictionary.Title")); //$NON-NLS-1$
    }

    /**
     * Load dictionary in background process.
     *
     * @param monitor IProgressMonitor
     * @return IStatus
     */
    protected IStatus run(IProgressMonitor monitor) {

        // check if we can persisted dictionary
        monitor.setTaskName(Messages.getString("Progress.Dictionary.Scanning")); //$NON-NLS-1$

        return new Status(IStatus.OK, ID, IStatus.OK, Messages.getString("DictionaryLoader.teseedOk"), null); //$NON-NLS-1$
    }

}
