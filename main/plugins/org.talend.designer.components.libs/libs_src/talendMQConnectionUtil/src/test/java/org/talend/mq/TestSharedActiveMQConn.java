package org.talend.mq;

import javax.jms.Connection;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SharedActiveMQConnection.class)
public class TestSharedActiveMQConn extends TestCase {

    @After
    public void clear() {
        SharedActiveMQConnection.clear();
    }

    @Test
    public void testSameConnName() throws Exception {
        Connection connection1 = SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn");
        Connection connection2 = SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn");

        assertSame(connection1, connection2);
    }

    @Test
    public void testDiffConnName() throws Exception {
        Connection connection1 = SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn1");
        Connection connection2 = SharedActiveMQConnection.getMQConnection("tcp://localhost:61616", "", "", "conn2");

        assertNotSame(connection1, connection2);
    }

    @Before
    public void prepare() throws Exception {
        ActiveMQConnectionFactory connectionFactory = Mockito.mock(ActiveMQConnectionFactory.class);
        Mockito.when(connectionFactory.createConnection()).thenAnswer((o) -> {
            return Mockito.mock(Connection.class);
        });

        PowerMockito.whenNew(ActiveMQConnectionFactory.class).withAnyArguments().thenReturn(connectionFactory);
        PowerMockito.whenNew(ActiveMQConnectionFactory.class).withNoArguments().thenReturn(connectionFactory);
    }

}
