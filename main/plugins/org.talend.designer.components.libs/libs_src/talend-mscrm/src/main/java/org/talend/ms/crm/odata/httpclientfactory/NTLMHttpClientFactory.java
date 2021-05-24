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
package org.talend.ms.crm.odata.httpclientfactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.olingo.client.core.http.NTLMAuthHttpClientFactory;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.talend.ms.crm.odata.ClientConfiguration;

public class NTLMHttpClientFactory extends NTLMAuthHttpClientFactory implements IHttpclientFactoryObservable {

    private ClientConfiguration clientConfiguration;

    private DefaultHttpClientState defaultHttpClientState;

    private List<IHttpClientFactoryObserver> listeners = new ArrayList<IHttpClientFactoryObserver>();

    public NTLMHttpClientFactory(ClientConfiguration conf) {
        super(conf.getUserName(), conf.getPassword(), conf.getWorkstation(), conf.getDomain());

        this.clientConfiguration = conf;
    }

    @Override
    public final void addListener(IHttpClientFactoryObserver l) {
        this.listeners.add(l);
    }

    /**
     * Must be called each time a new HttpClient is created by the returned HttpClientFactory.
     */
    protected final void fireHttpClientCreated(DefaultHttpClientState defaultHttpClientState) {
        for (IHttpClientFactoryObserver l : listeners) {
            l.httpClientCreated(defaultHttpClientState);
        }
    }

    @Override
    public DefaultHttpClient create(final HttpMethod method, final URI uri) {
        if (!clientConfiguration.isReuseHttpClient() || defaultHttpClientState == null
                || defaultHttpClientState.needNewHttpClient()) {
            DefaultHttpClient httpClient = super.create(method, uri);

            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), clientConfiguration.getTimeout() * 1000);
            HttpConnectionParams.setSoTimeout(httpClient.getParams(), clientConfiguration.getTimeout() * 1000);

            defaultHttpClientState = new DefaultHttpClientState(httpClient);
            fireHttpClientCreated(defaultHttpClientState);
        }
        return defaultHttpClientState.getHttpClient();
    }

}
