package org.talend.mq;

import java.util.Hashtable;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

/**
 * Functional interface to use in
 * {@link SharedWebSphereMQConnection#getMQConnection(String, Hashtable, String, MQQueueManagerFactory)}
 * Added mostly for testing purposes, to be able to test the shared connections mechanism
 */
public interface MQQueueManagerFactory {

    public MQQueueManager createQueueManager(String queueManagerName, Hashtable properties) throws MQException;

}
