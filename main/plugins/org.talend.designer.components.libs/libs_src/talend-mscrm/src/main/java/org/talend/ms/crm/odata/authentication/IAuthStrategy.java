// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import org.talend.ms.crm.odata.httpclientfactory.IHttpclientFactoryObservable;

/**
 * This interface has been designed based on how OAuth were integrated into DynamicsCRMClient.
 * 
 * OAuth were the first (and the only) authentication way. To not have regression, I have designed this Interface to match
 * the way OAuth were implemented.
 */
public interface IAuthStrategy {

    /**
     * Init Authentication strategy.
     * Called when DynamicsCRMClient is instanciated.
     * 
     */
    public void init() throws AuthenticationException;

    /**
     * Retrieve the HttpClientFactory.
     * 
     * Should be AbstractHttpClientFactoryObservable since DynamicsCRMClient need
     * to know if a new client is necessary.
     * Called when DynamicsCRMClient is instanciated.
     * 
     */
    public IHttpclientFactoryObservable getHttpClientFactory() throws AuthenticationException;

    /**
     * Refresh Authentication if needed.
     */
    public void refreshAuth() throws AuthenticationException;

    /**
     * Configure request generated by odataClient.getRetrieveRequestFactory().
     */
    public void configureRequest(ODataRequest request);

    /**
     * Configure request HttpRequestBase with POST/PATCH/DELETE method.
     */
    public void configureRequest(HttpRequestBase request);

}
