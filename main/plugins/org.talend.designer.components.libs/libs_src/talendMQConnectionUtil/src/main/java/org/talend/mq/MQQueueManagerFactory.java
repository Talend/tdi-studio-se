package org.talend.mq;

import java.util.Hashtable;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

public interface MQQueueManagerFactory {

    public MQQueueManager createQueueManager(String queueManagerName, Hashtable properties) throws MQException;

}
