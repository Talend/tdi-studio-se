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
package org.talend.ms.crm.odata;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.client.api.serialization.ODataSerializer;
import org.apache.olingo.client.api.uri.QueryOption;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.DefaultHttpClientFactory;
import org.apache.olingo.client.core.http.HttpPatch;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.talend.ms.crm.odata.authentication.AuthStrategyFactory;
import org.talend.ms.crm.odata.authentication.IAuthStrategy;
import org.talend.ms.crm.odata.httpclientfactory.DefaultHttpClientState;
import org.talend.ms.crm.odata.httpclientfactory.IHttpClientFactoryObserver;
import org.talend.ms.crm.odata.httpclientfactory.IHttpclientFactoryObservable;

import javax.naming.AuthenticationException;
import javax.naming.ServiceUnavailableException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Client for accessing Dynamics CRM Online using the Web API
 *
 */
public class DynamicsCRMClient implements IHttpClientFactoryObserver {

    private static final String NAMESPAVE = "Microsoft.Dynamics.CRM";
    private  static final String LOOKUP_NAME_PREFIX = "_";
    private  static final String LOOKUP_NAME_SUFFIX = "_value";
    private static final int LOOKUP_NAME_MINSIZE = LOOKUP_NAME_PREFIX.length()+LOOKUP_NAME_SUFFIX.length();

    private ClientConfiguration clientConfiguration;

    /**
     * The service endpoint to which this client will send requests.
     */
    private String serviceRootURL;

    private ODataClient odataClient;

    private DefaultHttpClientState httpClientState;

    private IHttpclientFactoryObservable httpClientFactory;

    private IAuthStrategy authStrategy;

    private String entitySet;

    private String entityType;

    /**
     * A list which contains all navigation links to set to null.
     *
     * A distinct delete command must be sent to set a navigation property to null. It can't be done in the same
     * time that the entity update.
     */
    private List<String> navigationLinksToNull = new ArrayList<String>();

    public DynamicsCRMClient(ClientConfiguration clientConfiguration, String serviceRootURL, String entitySet)
            throws AuthenticationException {
        this.clientConfiguration = clientConfiguration;

        this.serviceRootURL = serviceRootURL;

        this.entitySet = entitySet;

        init();
    }

    private void init() throws AuthenticationException {
        odataClient = ODataClientFactory.getClient();
        if (clientConfiguration != null && serviceRootURL != null && serviceRootURL.indexOf("/api/data") > 0) {
            clientConfiguration.setResource(serviceRootURL.substring(0, serviceRootURL.indexOf("/api/data")));
        }

        authStrategy = AuthStrategyFactory.createAuthStrategy(this.clientConfiguration);
        authStrategy.init();

        httpClientFactory = authStrategy.getHttpClientFactory();
        httpClientFactory.addListener(this);

        odataClient.getConfiguration().setHttpClientFactory((DefaultHttpClientFactory) httpClientFactory);

        ((DefaultHttpClientFactory) httpClientFactory).create(null, null);

    }

    @Override
    public void httpClientCreated(DefaultHttpClientState httpClientState) {
        this.httpClientState = httpClientState;

        HttpConnectionParams.setConnectionTimeout(httpClientState.getHttpClient().getParams(),
                clientConfiguration.getTimeout() * 1000);
        HttpConnectionParams.setSoTimeout(httpClientState.getHttpClient().getParams(), clientConfiguration.getTimeout() * 1000);

        // setup proxy
        setHttpclientProxy(httpClientState.getHttpClient());
    }

    public ODataClient getClient() {
        return odataClient;
    }

    /**
     * Create EntitySet Iterator request
     *
     * @param queryOption
     * @return EntitySet iterator request
     */
    public ODataEntitySetRequest<ClientEntitySet> createEntityRetrieveRequest(QueryOptionConfig queryOption) {
        URIBuilder uriBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet);
        if (queryOption.getReturnEntityProperties() != null) {
            uriBuilder.select(queryOption.getReturnEntityProperties());
        }
        if (queryOption.getTop() > 0) {
            uriBuilder.top(queryOption.getTop());
        }
        if (!StringUtils.isEmpty(queryOption.getOrderBy())) {
            uriBuilder.orderBy(queryOption.getOrderBy());
        }
        if (!StringUtils.isEmpty(queryOption.getFilter())) {
            uriBuilder.filter(queryOption.getFilter());
        }
        ODataEntitySetRequest<ClientEntitySet> request = odataClient.getRetrieveRequestFactory()
                                                                    .getEntitySetRequest(uriBuilder.build());

        this.authStrategy.configureRequest(request);

        return request;
    }

    public EdmMetadataRequest createMetadataRetrieveRequest() {
        EdmMetadataRequest request = odataClient.getRetrieveRequestFactory()
                                                .getMetadataRequest(serviceRootURL);
        this.authStrategy.configureRequest(request);
        return request;
    }

    public ODataEntitySetRequest<ClientEntitySet> createEntitySetRetrieveRequest() {
        URIBuilder uriBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment("EntityDefinitions").select("EntitySetName,LogicalName");
        ODataEntitySetRequest<ClientEntitySet> entitySetRequest = odataClient.getRetrieveRequestFactory().getEntitySetRequest(uriBuilder.build());
        this.authStrategy.configureRequest(entitySetRequest);
        return entitySetRequest;
    }

    public ODataEntitySetRequest<ClientEntitySet> createEndpointsNamesRequest(){
        URIBuilder uriBuilder = odataClient.newURIBuilder(serviceRootURL);
        ODataEntitySetRequest<ClientEntitySet> entitySetRequest = odataClient.getRetrieveRequestFactory().getEntitySetRequest(uriBuilder.build());
        this.authStrategy.configureRequest(entitySetRequest);
        return entitySetRequest;
    }

    public ODataEntitySetRequest<ClientEntitySet> createRequest(URIBuilder uriBuilder) {
        ODataEntitySetRequest<ClientEntitySet> entitySetRequest = odataClient.getRetrieveRequestFactory().getEntitySetRequest(uriBuilder.build());
        this.authStrategy.configureRequest(entitySetRequest);
        return entitySetRequest;
    }

    /**
     * Retrieve records from EntitySet
     *
     * @param queryOption
     * @return the entity set iterator object
     *
     * @throws ServiceUnavailableException
     */
    public ClientEntitySet retrieveEntities(QueryOptionConfig queryOption) throws AuthenticationException {
        boolean hasRetried = false;
        while (true) {
            ODataRetrieveResponse<ClientEntitySet> response = null;
            try {
                ODataEntitySetRequest<ClientEntitySet> request = createEntityRetrieveRequest(queryOption);
                response = request.execute();
                if (isResponseSuccess(response.getStatusCode())) {
                    return response.getBody();
                }
            } catch (ODataClientErrorException clientException) {
                // If get 401 Unauthorized, it would refresh the token
                if (clientException.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED && !hasRetried) {
                    this.authStrategy.refreshAuth();
                    hasRetried = true;
                    continue;
                }
                // If have been refresh the token or still got other kind of exception
                throw clientException;
            }
            throw new HttpClientException(response.getStatusMessage());
        }

    }

    /**
     * Create entity
     *
     * @param entity provided content for create
     *
     * @throws ServiceUnavailableException
     */
    public HttpResponse insertEntity(ClientEntity entity) throws ServiceUnavailableException {
        URIBuilder insertURIBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet);
        HttpEntity httpEntity = convertToHttpEntity(entity);
        return createAndExecuteRequest(insertURIBuilder.build(), httpEntity, HttpMethod.POST);
    }

    /**
     * Update entity with provided content.
     * The PATCH method is used, so only given properties are updated.
     * Navigation link properties that must be set to null are updated by another DELETE calls.
     * Navigation link properties are saved during {@link #addEntityNavigationLink(ClientEntity, String, String, String, boolean, boolean)}
     * method call
     *
     * @param entity The payload containing properties to update
     * @param keySegment The id of the entity to update
     *
     * @throws ServiceUnavailableException
     *
     * @deprecated use {@link #updateEntity(ClientEntity, String, List)} instead with a list of navigation links list instead;
     */
    @Deprecated
    public HttpResponse updateEntity(ClientEntity entity, String keySegment) throws ServiceUnavailableException {
        HttpResponse response = updateEntity(entity, keySegment, this.navigationLinksToNull);
        this.navigationLinksToNull.clear();
        return response;
    }

    /**
     * Update entity with provided content.
     * The PATCH method is used, so only given properties are updated.
     * Navigation link properties that must be set to null are updated by another DELETE calls.
     *
     * @param entity The payload containing properties to update
     * @param keySegment The id of the entity to update
     * @param navigationLinksToDelete list of navigation link names to delete
     *
     * @throws ServiceUnavailableException
     */
    public HttpResponse updateEntity(ClientEntity entity, String keySegment, List<String> navigationLinksToDelete) throws ServiceUnavailableException {
        URIBuilder updateURIBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet)
                                                 .appendKeySegment(UUID.fromString(keySegment));
        HttpEntity httpEntity = convertToHttpEntity(entity);
        HttpResponse updateHttpResponse =  createAndExecuteRequest(updateURIBuilder.build(), httpEntity, HttpMethod.PATCH);

        // No need to test the updateHttpResponse code since it is returned only if it's a success.
        // The deletion of navigation links will be done only if the previous update doesn't throw an exception.
        this.deleteNavigationLinksToNull(keySegment, navigationLinksToDelete);

        return updateHttpResponse;
    }

    protected void deleteNavigationLinksToNull(String keySegment, List<String> navigationLinksToNull) throws ServiceUnavailableException {
        if(navigationLinksToNull == null || navigationLinksToNull.isEmpty()) {
            return;
        }
        for(String navigationLinkName : navigationLinksToNull){
            this.deleteNavigationLink(navigationLinkName, keySegment);
        }
    }

    /**
     * Deleted entity by key
     *
     * @param keySegment Entity key segment
     *
     * @throws ServiceUnavailableException
     */
    public HttpResponse deleteEntity(String keySegment) throws ServiceUnavailableException {
        URIBuilder deleteURIBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet)
                                                 .appendKeySegment(UUID.fromString(keySegment));

        return createAndExecuteRequest(deleteURIBuilder.build(), null, HttpMethod.DELETE);
    }


    /**
     * Delete a navigation link from an entity (set the property to null).
     * Jira : TDI-39571
     *
     * @param navigationLinkName The navigation link name (not the _name_value generated lookup property)
     * @param keySegment The keysegment(id) of the main property
     * @return The Http response.
     * @throws ServiceUnavailableException
     */
    public HttpResponse deleteNavigationLink(String navigationLinkName, String keySegment) throws ServiceUnavailableException {
        URIBuilder deleteNavLinkURIBuilder = odataClient.newURIBuilder(serviceRootURL)
                                                        .appendEntitySetSegment(entitySet)
                                                        .appendKeySegment(UUID.fromString(keySegment))
                                                        .appendNavigationSegment(navigationLinkName).appendRefSegment();

        return createAndExecuteRequest(deleteNavLinkURIBuilder.build(), null, HttpMethod.DELETE);
    }

    /**
     * Get entity type by EntitySet name
     *
     * @param entitySetName
     * @return entity type value
     */
    public String getEntityType(String entitySetName) {
        // TODO need to check whether have another better way
        URIBuilder uriBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment("EntityDefinitions");
        uriBuilder.addQueryOption(QueryOption.SELECT, "EntitySetName,LogicalName");
        uriBuilder.filter("EntitySetName eq '" + entitySetName + "'");
        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = odataClient.getRetrieveRequestFactory()
                                                                                          .getEntitySetIteratorRequest(uriBuilder.build());
        this.authStrategy.configureRequest(request);

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();
        try {
            ClientEntitySetIterator<ClientEntitySet, ClientEntity> entitySetIterator = response.getBody();
            if (entitySetIterator.hasNext()) {
                ClientProperty localName = entitySetIterator.next().getProperty("LogicalName");
                if (localName != null && localName.getValue() != null) {
                    return localName.getValue().toString();
                }
            }
        } finally {
            response.close();
            // Close reponse would also close connection. So here need recreate httpclient
            this.httpClientState.setNeedNewHttpClient(true);
        }
        return null;
    }

    /**
     * Create a OData entity with entity type
     *
     * @return OData entity
     */
    public ClientEntity newEntity() {
        if (entityType == null) {
            entityType = getEntityType(entitySet);
        }
        return odataClient.getObjectFactory().newEntity(new FullQualifiedName(NAMESPAVE, entityType));
    }

    public void addEntityProperty(ClientEntity entity, String propertyName, EdmPrimitiveTypeKind type, Object value) {
        if (type != null) {
            entity.getProperties().add(odataClient.getObjectFactory().newPrimitiveProperty(propertyName,
                    odataClient.getObjectFactory().newPrimitiveValueBuilder().setType(type).setValue(value).build()));
        }

    }

    /**
     * This setter has been added for unit tests to avoid some http requests.
     *
     * @param entityType
     */
    public void setEntityType(String entityType){
        this.entityType = entityType;
    }

    public void addEntityNavigationLink(ClientEntity entity, String lookupEntitySet, String lookupName,
            String linkedEntityId, boolean emptyLookupIntoNull, boolean ignoreNull) {

        // To help the final user since lookup names '_name_value' are available in the schema
        // But it's the navigation link that can be updated.
        String navigationLinkName = this.extractNavigationLinkName(lookupName);

        // If value is empty and emptyLookupIntoNull, then set the value to null to unlink the navigation (set to null)
        if(emptyLookupIntoNull && linkedEntityId != null && linkedEntityId.isEmpty()){
            linkedEntityId = null;
        }

        // If ignore null is set and the value is null, then don't update/delete this navigation link
        if(ignoreNull && linkedEntityId == null){
            return;
        }

        if (linkedEntityId != null) {
            try {
                entity.getNavigationLinks().add(odataClient.getObjectFactory().newEntityNavigationLink(navigationLinkName,
                        new URI(lookupEntitySet + "(" + linkedEntityId + ")")));
            } catch (URISyntaxException e) {
                throw new HttpClientException(e);
            }
        }
        else{
            // Retains all navigation links to delete (set to null)
            navigationLinksToNull.add(navigationLinkName);
        }
    }

    /**
     * Try to add navigation link to entity.
     * @param navigationLinkName extracted navigation link(see {@link #extractNavigationLinkName(String)})
     * @param entity to be updated
     * @param lookupEntitySet entity set referred by navigation link
     * @param linkedEntityId id of the referred entity
     * @param emptyLookupIntoNull if empty lookup values should be converted to null values
     * @param ignoreNull if null values should be skipped
     * @return true if the link was added or skipped, false if it should be deleted with a separate call.
     */
    public boolean addOrSkipEntityNavigationLink(ClientEntity entity, String lookupEntitySet, String navigationLinkName,
            String linkedEntityId, boolean emptyLookupIntoNull, boolean ignoreNull){
        // If value is empty and emptyLookupIntoNull, then set the value to null to unlink the navigation (set to null)
        if(emptyLookupIntoNull && linkedEntityId != null && linkedEntityId.isEmpty()){
            linkedEntityId = null;
        }

        // If ignore null is set and the value is null, then don't update/delete this navigation link
        if(ignoreNull && linkedEntityId == null){
            return true;
        }

        if (linkedEntityId != null) {
            try {
                entity.getNavigationLinks().add(odataClient.getObjectFactory().newEntityNavigationLink(navigationLinkName,
                        new URI(lookupEntitySet + "(" + linkedEntityId + ")")));
            } catch (URISyntaxException e) {
                throw new HttpClientException(e);
            }
        }
        else{
            // Retains all navigation links to delete (set to null)
            return false;
        }
        return true;
    }

    public int getNbNavigationLinkToRemove(){
        return navigationLinksToNull.size();
    }

    /**
     * Get the navigation link name from a lookup one.
     * MSCRM auto-generates lookup properties from navigation links.
     * The auto-generated lookup property name is _navigationLinkName_value.
     * This method extract 'navigationLinkName' only if it begins by '_' and ends by '_value'.
     *
     * @param lookupName The auto-generated lookup name
     * @return The extracted navigation link name or the lookup name if it can't be extracted.
     */
    public String extractNavigationLinkName(String lookupName){
        final int nameSize = lookupName.length();
        if(nameSize <= LOOKUP_NAME_MINSIZE){
            return lookupName;
        }

        String pref = lookupName.substring(0, LOOKUP_NAME_PREFIX.length());
        if(!pref.equals(LOOKUP_NAME_PREFIX)){
            return lookupName;
        }

        final int endName = nameSize - LOOKUP_NAME_SUFFIX.length();
        String suff = lookupName.substring(endName, nameSize);
        if(!suff.equals(LOOKUP_NAME_SUFFIX)){
            return lookupName;
        }

        return lookupName.substring(LOOKUP_NAME_PREFIX.length(), endName);
    }

    /**
     * Convert OData entity to HttpEntity type
     *
     * @param entity OData entity.
     *
     * @return An entity that can be sent or received with an HTTP message.
     * @throws
     */

    protected HttpEntity convertToHttpEntity(ClientEntity entity) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(output, Constants.UTF8);
            final ODataSerializer serializer = odataClient.getSerializer(org.apache.olingo.commons.api.format.ContentType.JSON);
            serializer.write(writer, odataClient.getBinder().getEntity(entity));
            HttpEntity httpEntity = new ByteArrayEntity(output.toByteArray(),
                    org.apache.http.entity.ContentType.APPLICATION_JSON);
            return httpEntity;
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * Created and executes a request
     *
     * @param uri the request URI
     * @param httpEntity the entity to send.
     * @param method HTTP method
     *
     * @return the response to the request.
     * @throws ServiceUnavailableException
     */

    protected HttpResponse createAndExecuteRequest(URI uri, HttpEntity httpEntity, HttpMethod method)
            throws ServiceUnavailableException {

        boolean hasRetried = false;
        while (true) {
            try {
                ((DefaultHttpClientFactory) httpClientFactory).create(null, null);
                HttpRequestBase request = null;
                if (method == HttpMethod.POST) {
                    request = new HttpPost(uri);
                } else if (method == HttpMethod.PATCH) {
                    request = new HttpPatch(uri);
                } else if (method == HttpMethod.DELETE) {
                    request = new HttpDelete(uri);
                } else {
                    throw new HttpClientException("Unsupported operation:" + method);
                }
                this.authStrategy.configureRequest(request);

                if (request instanceof HttpEntityEnclosingRequestBase) {
                    ((HttpEntityEnclosingRequestBase) request).setEntity(httpEntity);
                }
                HttpResponse response = httpClientState.getHttpClient().execute(request);
                if (isResponseSuccess(response.getStatusLine().getStatusCode())) {
                    request.releaseConnection();
                    EntityUtils.consume(response.getEntity());
                    return response;
                } else {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED && !hasRetried) {
                        this.authStrategy.refreshAuth();
                        hasRetried = true;
                        continue;
                    }
                    HttpEntity entity = response.getEntity();
                    String message = null;
                    if (entity != null) {
                        message = odataClient.getDeserializer(ContentType.JSON).toError(entity.getContent()).getMessage();
                    } else {
                        message = response.getStatusLine().getReasonPhrase();
                    }
                    throw new HttpClientException(message);
                }
            } catch (Exception e) {
                throw new HttpClientException(e);
            }
        }
    }

    /**
     * Judge whether specify code is success code
     * 200 retrieve entity success
     * 204 create/update/delete entity success without return
     * 201 create/update/delete entity success with return (not used in our component at present)
     *
     * @param statusCode HTTP status code
     *
     * @return success or not(true or false)
     */
    public boolean isResponseSuccess(int statusCode) {
        return statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK;
    }

    /**
     * Setup proxy for httpClient
     */
    private void setHttpclientProxy(DefaultHttpClient httpClient) {
        Proxy proxy = ProxyProvider.getProxy();
        String proxyUser = ProxyProvider.getProxyUserName();
        String proxyPwd = ProxyProvider.getProxyUserPassword();
        // set by other components like tSetProxy
        if (proxy != null) {

            final HttpHost httpHost = new HttpHost(((InetSocketAddress) proxy.address()).getHostName(),
                    ((InetSocketAddress) proxy.address()).getPort());
            // Sets usage of HTTP proxy
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);

            // Sets proxy authentication, if credentials were provided
            // TODO Because the proxy of get accesToken can't support authentication. remove this ?
            if (proxyUser != null && proxyPwd != null) {
                httpClient.getCredentialsProvider().setCredentials(new AuthScope(httpHost),
                        new UsernamePasswordCredentials(proxyUser, proxyPwd));
            }

        }

    }

}
