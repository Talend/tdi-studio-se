package org.talend.mq;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.TestCase;

public class TestSharedActiveMQConn extends TestCase {

    @After
    public void clear() {
        SharedActiveMQConnection.clear();
    }

    @Test
    public void testSameConnName() throws JMSException {
        ActiveMQConnectionFactoryProvider provider = createConnectionFactoryProvider();

        Connection connection1 =
                SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn", provider);
        Connection connection2 =
                SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn", provider);

        assertSame(connection1, connection2);
    }

    @Test
    public void testDiffConnName() throws JMSException {
        ActiveMQConnectionFactoryProvider provider = createConnectionFactoryProvider();

        Connection connection1 =
                SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn1", provider);
        Connection connection2 =
                SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn2", provider);

        assertNotSame(connection1, connection2);
    }

    private ActiveMQConnectionFactoryProvider createConnectionFactoryProvider() {
        ActiveMQConnectionFactoryProvider factoryMock = Mockito.mock(ActiveMQConnectionFactoryProvider.class);
        Mockito.when(factoryMock.createConnectionFactory(Mockito.anyString())).thenAnswer((o) -> {
            // we need to return new queue manager on every call of createQueueManager
            ActiveMQConnectionFactory queueManagerMock = Mockito.mock(ActiveMQConnectionFactory.class);
            Mockito.when(queueManagerMock.createConnection()).thenReturn(Mockito.mock(Connection.class));
            return queueManagerMock;
        });
        return factoryMock;
    }

}
