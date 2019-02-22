package org.talend.mq;

import java.util.Hashtable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.mq.MQQueueManager;

import junit.framework.TestCase;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(SharedWebSphereMQConnection.class)
public class TestSharedWebShpereMQConn extends TestCase {

    Hashtable<String, Object> properties = new java.util.Hashtable<String, Object>();

    @After
    public void clear() {
        SharedWebSphereMQConnection.clear();
    }

    @Test
    public void testSameConnName() throws Exception {
        setUpPowerMock();
        MQQueueManager mqConnection1 = SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn");
        setUpPowerMock();
        MQQueueManager mqConnection2 = SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn");

        assertSame(mqConnection1, mqConnection2);
    }

    @Test
    public void testDiffConnName() throws Exception {
        setUpPowerMock();
        MQQueueManager mqConnection1 = SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn1");
        setUpPowerMock();
        MQQueueManager mqConnection2 = SharedWebSphereMQConnection.getMQConnection("TALEND", properties, "conn2");

        assertNotSame(mqConnection1, mqConnection2);
    }

    @Before
    public void prepare() throws Exception {
        properties.put("hostname", "localhost");
        properties.put("port", Integer.valueOf("1414"));
        properties.put("channel", "TALEND.CH");
        properties.put("CCSID", new Integer(1208));
        properties.put("transport", "MQSeries");
    }

    private void setUpPowerMock() throws Exception {
        // we need to return new queue manager on every call of createQueueManager
        MQQueueManager queueManagerMock = Mockito.mock(MQQueueManager.class);
        // we need the queue manager to be "connected" every time. If it isn't, it won't be used, and a new one will
        // be created for the same connection name
        Mockito.when(queueManagerMock.isConnected()).thenReturn(true);
        PowerMockito.whenNew(MQQueueManager.class).withAnyArguments().thenReturn(queueManagerMock);
    }

}
