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

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ConnectionClosedException;

/**
 * A buffer to keep all the MQ connections, make it reusable between the
 * different jobs.
 */
public class SharedActiveMQConnection {

    private static boolean DEBUG = false;

    private static SharedActiveMQConnection instance = null;

    private Map<String, javax.jms.Connection> sharedConnections = new HashMap<String, javax.jms.Connection>();

    private SharedActiveMQConnection() {

    }

    private static synchronized SharedActiveMQConnection getInstance() {
        if (instance == null) {
            instance = new SharedActiveMQConnection();
        }
        return instance;
    }

    private synchronized javax.jms.Connection getConnection(String url, String userName, String password,
            String mqConnectionName) throws JMSException {
        if (DEBUG) {
            Set<String> keySet = sharedConnections.keySet();
            System.out.print("SharedMQConnection, current shared connections list is:"); //$NON-NLS-1$
            for (String key : keySet) {
                System.out.print(" " + key); //$NON-NLS-1$
            }
            System.out.println();
        }
        javax.jms.Connection connection = sharedConnections.get(mqConnectionName);
        if (connection == null) {
            if (DEBUG) {
                System.out
                        .println("SharedMQConnection, can't find the key:" + mqConnectionName + " " //$NON-NLS-1$ //$NON-NLS-2$
                                + "so create a new one and share it."); //$NON-NLS-1$
            }
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
            if (userName == null || ("").equals(userName)) {
                connection = factory.createConnection();
            } else {
                connection = factory.createConnection(userName, password);
            }
            connection.start();
            sharedConnections.put(mqConnectionName, connection);
        } else {
            try {
                connection.start();
                if (DEBUG) {
                    System.out.println("SharedMQConnection, find the key: " + mqConnectionName + " " + "it is OK."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            } catch (ConnectionClosedException e) {
                if (DEBUG) {
                    System.out
                            .println("SharedMQConnection, find the key: " + mqConnectionName + " " //$NON-NLS-1$ //$NON-NLS-2$
                                    + "But it is closed. So create a new one and share it."); //$NON-NLS-1$
                }
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
                if (userName == null || ("").equals(userName)) {
                    connection = factory.createConnection();
                } else {
                    connection = factory.createConnection(userName, password);
                }
                connection.start();
                sharedConnections.put(mqConnectionName, connection);
            }
        }
        return connection;
    }

    /**
     * If there don't exist the connection or it is closed, create and store it.
     *
     * @param url
     * @param mqConnectionName
     * @return
     * @throws JMSException
     */
    public static javax.jms.Connection getMQConnection(String url, String userName, String password,
            String mqConnectionName) throws JMSException {
        SharedActiveMQConnection instanceLocal = getInstance();
        javax.jms.Connection connection = instanceLocal.getConnection(url, userName, password, mqConnectionName);
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
