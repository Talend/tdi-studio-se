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
package org.talend.sqlbuilder.ui.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;

/**
 * DOC dev  class global comment. Detailled comment
 * <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 * @author qiang.zhang
 */
public class OpenSQLBuilderDialogProgress implements IRunnableWithProgress {

	private SQLBuilderRepositoryNodeManager manager;

	private ConnectionParameters connectionParameters;

	private RepositoryNode repositoryNode;

	public OpenSQLBuilderDialogProgress(ConnectionParameters connectionParameters,
			SQLBuilderRepositoryNodeManager manager, Composite composite) {
    	this.connectionParameters = connectionParameters;
    	this.manager = manager;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		monitor.setTaskName(Messages.getString("OpenDialogJob.Waitdatabase")); //$NON-NLS-1$
        monitor.beginTask(Messages.getString("OpenDialogJob.Waitdatabase"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        try {
        	repositoryNode = manager.getRepositoryNodeByBuildIn(null, connectionParameters);
            connectionParameters.setRepositoryNodeBuiltIn(repositoryNode);
            while (true) {

                if (monitor.isCanceled()) {

                    if (repositoryNode != null) {
                    	repositoryNode = null;
                    }
                    connectionParameters.setConnectionComment(null);
                    break;
                }
                if (connectionParameters.getConnectionComment() != null
                        && connectionParameters.getRepositoryNodeBuiltIn() == null) {
                    break;
                }
                if (connectionParameters.getRepositoryNodeBuiltIn() != null) {
                	break;
                }

                Thread.sleep(100);
            }

            // check for cancellation by user
            if (monitor.isCanceled()) {
                monitor.done();
                throw new InterruptedException(Messages.getString("OpenSQLBuilderDialogProgress.exceptionMessage")); //$NON-NLS-1$
            }
            monitor.done();


        } catch (Throwable e) {
        	SqlBuilderPlugin.log(Messages.getString("OpenSQLBuilderDialogProgress.logMessage"), e); //$NON-NLS-1$
        } finally {
            monitor.done();
        }
    }

	/**
     * Close any RepositoryNode.
     */
    public void cleanUp() {
            if (connectionParameters.getRepositoryNodeBuiltIn() != null) {
            	connectionParameters.setRepositoryNodeBuiltIn(null);
            }

    }
}
