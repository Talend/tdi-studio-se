package org.talend.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Functional interface to use in
 * {@link SharedActiveMQConnection#getMQConnection(String, String, String, String, ActiveMQConnectionFactoryProvider)}
 * Added mostly for testing purposes, to be able to test the shared connections mechanism
 */
public interface ActiveMQConnectionFactoryProvider {

    public ActiveMQConnectionFactory createConnectionFactory(String url);

}
