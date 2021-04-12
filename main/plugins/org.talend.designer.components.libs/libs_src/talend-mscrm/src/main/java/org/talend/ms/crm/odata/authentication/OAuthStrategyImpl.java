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

import java.net.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.AuthenticationException;
import javax.naming.ServiceUnavailableException;

import com.microsoft.aad.adal4j.ClientCredential;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.olingo.client.api.communication.request.ODataRequest;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.talend.ms.crm.odata.ClientConfiguration;
import org.talend.ms.crm.odata.ProxyProvider;
import org.talend.ms.crm.odata.httpclientfactory.IHttpclientFactoryObservable;
import org.talend.ms.crm.odata.httpclientfactory.OAuthHttpClientFactory;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;

public class OAuthStrategyImpl implements IAuthStrategy {

    private ClientConfiguration conf;

    private AuthenticationResult authResult;

    private IHttpclientFactoryObservable httpClientFactory;

    OAuthStrategyImpl(ClientConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void init() throws AuthenticationException {
        try {
            authResult = getAccessToken();
        } catch (ServiceUnavailableException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public IHttpclientFactoryObservable getHttpClientFactory() throws AuthenticationException {
        try {
            authResult = getAccessToken();
        } catch (ServiceUnavailableException e) {
            throw new AuthenticationException(e.getMessage());
        }

        if (httpClientFactory == null) {
            httpClientFactory = new OAuthHttpClientFactory(this.conf);
        }

        return httpClientFactory;

    }

    @Override
    public void configureRequest(ODataRequest request) {
        request.addCustomHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
    }

    @Override
    public void configureRequest(HttpRequestBase request) {
        request.addHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
    }

    @Override
    public void refreshAuth() throws AuthenticationException {

        try {
            this.refreshToken();
        } catch (ServiceUnavailableException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    /**
     * Refresh token when expired
     */
    public void refreshToken() throws ServiceUnavailableException {
        int retryTime = 0;
        // refresh the access token
        while (true) {
            try {
                this.authResult = getAccessToken();
                break;// refresh token successfully
            } catch (ServiceUnavailableException e) {
                if (retryTime < conf.getMaxRetryTimes()) {
                    retryTime++;
                    try {
                        Thread.sleep(conf.getIntervalTime());
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                    continue;// retry to refresh token
                }
                throw e;// failed to refresh token after retry
            }

        }
    }

    private Future<AuthenticationResult> acquireToken(AuthenticationContext context) throws Exception {
        Future<AuthenticationResult> future;

        if(conf.getAppRegisteredType() == ClientConfiguration.AppRegisteredType.NATIVE_APP){
            future = context.acquireToken(conf.getResource(), conf.getClientId(), conf.getUserName(), conf.getPassword(), null);
        }
        else if(conf.getAppRegisteredType() == ClientConfiguration.AppRegisteredType.WEB_APP && conf.getWebAppPermission() == ClientConfiguration.WebAppPermission.DELEGATED){
            future = context.acquireToken(conf.getResource(), new ClientCredential(conf.getClientId(), conf.getClientSecret()), conf.getUserName(), conf.getPassword(), null);
        }
        else{
            throw new Exception("Can't retrieve token with this configuration : registered application type: "+conf.getAppRegisteredType()+", Web application permission: "+conf.getWebAppPermission());
        }

        return future;
    }

    private AuthenticationResult getAccessToken() throws ServiceUnavailableException {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(conf.getAuthoryEndpoint(), false, service);
            Proxy proxy = ProxyProvider.getProxy();
            if (proxy != null) {
                context.setProxy(proxy);
            }
            Future<AuthenticationResult> future = this.acquireToken(context);
            result = future.get();
        } catch (Exception e) {
            throw new ServiceUnavailableException(e.getMessage());
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException("Authenticated failed! Please check your configuration!");
        }
        return result;
    }

}
