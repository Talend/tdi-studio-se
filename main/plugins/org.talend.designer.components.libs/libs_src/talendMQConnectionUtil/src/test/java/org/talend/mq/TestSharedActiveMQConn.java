package org.talend.mq;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.TestCase;

public class TestSharedActiveMQConn extends TestCase {

    @Test
    public void testSameConnNAme() throws JMSException {
        ActiveMQConnectionFactoryProvider provider = createConnectionFactoryProvider();
        assertTrue(SharedActiveMQConnection
                .getMQConnection("tcp://localhost:61616", "", "", "conn", provider) == SharedActiveMQConnection
                        .getMQConnection("tcp://localhost:61616", "", "", "conn", provider));
    }

    @Test
    public void testDiffConnNAme() throws JMSException {
        ActiveMQConnectionFactoryProvider provider = createConnectionFactoryProvider();
        assertFalse(SharedActiveMQConnection
                .getMQConnection("tcp://localhost:61616", "", "", "conn1", provider) == SharedActiveMQConnection
                        .getMQConnection("tcp://localhost:61616", "", "", "conn2", provider));
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
