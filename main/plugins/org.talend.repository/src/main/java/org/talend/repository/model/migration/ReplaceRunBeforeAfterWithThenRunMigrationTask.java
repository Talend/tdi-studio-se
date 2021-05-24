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
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.i18n.Messages;

/**
 * Task replace run before and after with then run connection.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ChangeRunBeforeAfterToThenRunMigrationTask.java 下午04:41:56 2007-5-17 +0000 (2007-5-17) yzhang $
 *
 */
public class ReplaceRunBeforeAfterWithThenRunMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    @Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);
		if (processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}
        try {
            replaceConnections(item, processType);
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

	/**
	 * Replace run before and after connection with then run.
	 *
	 * yzhang Comment method "replaceConnections".
	 *
	 * @param processType
	 *
	 * @throws PersistenceException
	 */
    public void replaceConnections(Item item, ProcessType processType)
			throws PersistenceException {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        boolean modified = false;

        Map<String, List> runAfterMap = new TreeMap<String, List>();

        if (isMultiJob(processType.getConnection())) {
            // TODO: if it's a job with muliti sub jobs an error mark need to be added in repository.
        }

        for (Object o : processType.getConnection()) {

            ConnectionType currentConnection = (ConnectionType) o;

            if (currentConnection.getLabel().equals(Messages.getString("ReplaceRunBeforeAfterWithThenRunMigrationTask.RunAfter"))) { //$NON-NLS-1$

                currentConnection.setLabel(Messages.getString("ReplaceRunBeforeAfterWithThenRunMigrationTask.ThenRun")); //$NON-NLS-1$
                currentConnection.setLineStyle(EConnectionType.ON_SUBJOB_OK.getId());

                String sourceKey = currentConnection.getSource();
                if (!runAfterMap.containsKey(sourceKey)) {
                    List<ConnectionType> connectionList = new ArrayList<ConnectionType>();
                    connectionList.add(currentConnection);
                    runAfterMap.put(sourceKey, connectionList);
                } else {
                    runAfterMap.get(sourceKey).add(currentConnection);
                }

                modified = true;

            } else if (currentConnection.getLabel().equals(
                    Messages.getString("ReplaceRunBeforeAfterWithThenRunMigrationTask.RunBefore"))) { //$NON-NLS-1$

                currentConnection.setLabel(Messages.getString("ReplaceRunBeforeAfterWithThenRunMigrationTask.ThenRun")); //$NON-NLS-1$
                String target = currentConnection.getTarget();
                currentConnection.setTarget(currentConnection.getSource());
                currentConnection.setSource(target);

                String sourceKey = currentConnection.getSource();
                if (!runAfterMap.containsKey(sourceKey)) {
                    List<ConnectionType> connectionList = new ArrayList<ConnectionType>();
                    connectionList.add(currentConnection);
                    runAfterMap.put(sourceKey, connectionList);
                } else {
                    runAfterMap.get(sourceKey).add(currentConnection);
                }

                modified = true;
            }

        }
        if (modified) {
            resetDirectionOfConnections(runAfterMap);
            factory.save(item);
        }
    }

    /**
     * Reset driection of the connections.
     *
     * yzhang Comment method "resetRunAfterConnections".
     *
     * @param connectionMap
     */
    private void resetDirectionOfConnections(Map<String, List> connectionMap) {
        if (connectionMap.isEmpty()) {
            return;
        }
        for (Iterator iter = connectionMap.keySet().iterator(); iter.hasNext();) {
            String sourceName = (String) iter.next();
            List<ConnectionType> connectionList = connectionMap.get(sourceName);
            ConnectionType oldConnection = null;
            for (ConnectionType connection : connectionList) {
                if (oldConnection != null) {
                    oldConnection.setTarget(connection.getTarget());
                }
                connection.setSource(connection.getTarget());
                oldConnection = connection;
            }
            if (oldConnection != null) {
                oldConnection.setTarget(sourceName);
            }
        }
    }

    /**
     * To see whether the job with sub jobs.
     *
     * yzhang Comment method "isMulitiJob".
     *
     * @return
     */
    private boolean isMultiJob(List<ConnectionType> connections) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (ConnectionType conn : connections) {

            if (conn.getLabel().equals(Messages.getString("ReplaceRunBeforeAfterWithThenRunMigrationTask.RunAfter")) || conn.getLabel().equals(Messages.getString("ReplaceRunBeforeAfterWithThenRunMigrationTask.RunBefore"))) { //$NON-NLS-1$ //$NON-NLS-2$
                if (map.containsKey(conn.getTarget())) {
                    int i = map.get(conn.getTarget());
                    map.put(conn.getTarget(), ++i);
                } else {
                    map.put(conn.getTarget(), 1);
                }
            }
        }

        for (Integer flag : map.values()) {
            if (flag > 1) {

                return true;
            }
        }
        return false;

    }
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
