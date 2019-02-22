package org.talend.mq;

import java.util.Hashtable;

import javax.jms.JMSException;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

import junit.framework.TestCase;

public class TestSharedWebShpereMQConn extends TestCase {

    private static Hashtable<String, Object> properties = new java.util.Hashtable<String, Object>();
    static {
        properties.put("hostname", "localhost");
        properties.put("port", Integer.valueOf("1414"));
        properties.put("channel", "TALEND.CH");
        properties.put("CCSID", new Integer(1208));
        properties.put("transport", "MQSeries");
    }

    @After
    public void clear() {
        SharedWebSphereMQConnection.clear();
    }

    @Test
    public void testSameConnName() throws JMSException, MQException {
        MQQueueManagerFactory factoryMock = createQueueFactory();

        MQQueueManager mqConnection1 =
                SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn", factoryMock);
        MQQueueManager mqConnection2 =
                SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn", factoryMock);

        assertTrue(mqConnection1 == mqConnection2);
    }

    @Test
    public void testDiffConnName() throws JMSException, MQException {
        MQQueueManagerFactory factoryMock = createQueueFactory();

        MQQueueManager mqConnection1 =
                SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn1", factoryMock);
        MQQueueManager mqConnection2 =
                SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn2", factoryMock);

        assertTrue(mqConnection1 != mqConnection2);
    }

    private MQQueueManagerFactory createQueueFactory() throws MQException {
        MQQueueManagerFactory factoryMock = Mockito.mock(MQQueueManagerFactory.class);
        Mockito.when(factoryMock.createQueueManager(Mockito.anyString(), Mockito.any())).thenAnswer((o) -> {
            // we need to return new queue manager on every call of createQueueManager
            MQQueueManager queueManagerMock = Mockito.mock(MQQueueManager.class);
            // we need the queue manager to be "connected" every time. If it isn't, it won't be used, and a new one will
            // be created for the same connection name
            Mockito.when(queueManagerMock.isConnected()).thenReturn(true);
            return queueManagerMock;
        });
        return factoryMock;
    }

}
