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
package org.talend.ms.crm.odata.authentication;

import javax.naming.AuthenticationException;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.olingo.client.api.communication.request.ODataRequest;
import org.talend.ms.crm.odata.ClientConfiguration;
import org.talend.ms.crm.odata.httpclientfactory.IHttpclientFactoryObservable;
import org.talend.ms.crm.odata.httpclientfactory.NTLMHttpClientFactory;

public class NTLMStrategyImpl implements IAuthStrategy {

    private ClientConfiguration conf;

    private IHttpclientFactoryObservable httpClientFactory;

    NTLMStrategyImpl(ClientConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public IHttpclientFactoryObservable getHttpClientFactory() throws AuthenticationException {
        if (httpClientFactory == null) {
            httpClientFactory = new NTLMHttpClientFactory(this.conf);
        }

        return httpClientFactory;
    }

    @Override
    public void refreshAuth() throws AuthenticationException {
        // Nothing to do
    }

    @Override
    public void init() throws AuthenticationException {
        // Nothing to do
    }

    @Override
    public void configureRequest(ODataRequest request) {
        // Nothing to do
    }

    @Override
    public void configureRequest(HttpRequestBase request) {
        // Nothing to do
    }

}
