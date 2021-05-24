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
package org.talend.mq;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

/**
 * A buffer to keep all the MQ connections, make it reusable between the
 * different jobs.
 */
public class SharedWebSphereMQConnection {

    private static boolean DEBUG = false;

    private static SharedWebSphereMQConnection instance = null;

    private Map<String, com.ibm.mq.MQQueueManager> sharedConnections = new HashMap<String, com.ibm.mq.MQQueueManager>();

    private SharedWebSphereMQConnection() {

    }

    private static synchronized SharedWebSphereMQConnection getInstance() {
        if (instance == null) {
            instance = new SharedWebSphereMQConnection();
        }
        return instance;
    }

    private synchronized com.ibm.mq.MQQueueManager getConnection(String queueManager,
            java.util.Hashtable<String, Object> properties, String mqConnectionName) throws MQException {
        if (DEBUG) {
            Set<String> keySet = sharedConnections.keySet();
            System.out.print("SharedMQConnection, current shared connections list is:"); //$NON-NLS-1$
            for (String key : keySet) {
                System.out.print(" " + key); //$NON-NLS-1$
            }
            System.out.println();
        }
        com.ibm.mq.MQQueueManager connection = sharedConnections.get(mqConnectionName);
        if (connection == null) {
            if (DEBUG) {
                System.out
                        .println("SharedMQConnection, can't find the key:" + mqConnectionName + " " //$NON-NLS-1$ //$NON-NLS-2$
                                + "so create a new one and share it."); //$NON-NLS-1$
            }
            connection = new MQQueueManager(queueManager, properties);
            sharedConnections.put(mqConnectionName, connection);
        } else if (!connection.isConnected()) {
            if (DEBUG) {
                System.out
                        .println("SharedMQConnection, find the key: " + mqConnectionName + " " //$NON-NLS-1$ //$NON-NLS-2$
                                + "But it is closed. So create a new one and share it."); //$NON-NLS-1$
            }
            connection = new MQQueueManager(queueManager, properties);
            sharedConnections.put(mqConnectionName, connection);
        } else {
            if (DEBUG) {
                System.out.println("SharedMQConnection, find the key: " + mqConnectionName + " " + "it is OK."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        return connection;
    }

    /**
     * If there don't exist the connection or it is closed, create and store it.
     *
     * @param queueManager
     * @param properties
     * @param mqConnectionName
     * @return
     * @throws MQException
     */
    public static com.ibm.mq.MQQueueManager getMQConnection(String queueManager,
            java.util.Hashtable<String, Object> properties, String mqConnectionName) throws MQException {
        SharedWebSphereMQConnection instanceLocal = getInstance();
        com.ibm.mq.MQQueueManager connection = instanceLocal.getConnection(queueManager, properties, mqConnectionName);
        return connection;
    }

    /**
     * Set the buffer as null, make it recyclable.
     */
    public static void clear() {
        instance = null;
    }

    public static void setDebugMode(boolean debug) {
        DEBUG = debug;
    }
}
