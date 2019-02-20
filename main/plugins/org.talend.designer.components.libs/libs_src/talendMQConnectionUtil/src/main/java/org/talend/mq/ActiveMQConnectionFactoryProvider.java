package org.talend.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

public interface ActiveMQConnectionFactoryProvider {

    public ActiveMQConnectionFactory createConnectionFactory(String url);

}
