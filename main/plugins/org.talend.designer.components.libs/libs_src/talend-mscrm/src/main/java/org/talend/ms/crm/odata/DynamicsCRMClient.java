package org.talend.ms.crm.odata;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.ServiceUnavailableException;

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
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.client.api.http.HttpClientFactory;
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
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;

/**
 * Client for accessing Dynamics CRM Online using the Web API
 * 
 */
public class DynamicsCRMClient {

    private static final String NAMESPAVE = "Microsoft.Dynamics.CRM";

    private ClientConfiguration clientConfiguration;

    /**
     * The service endpoint to which this client will send requests.
     */
    private String serviceRootURL;

    private ODataClient odataClient;

    private DefaultHttpClient httpClient;

    private HttpClientFactory httpClientFactory;

    private AuthenticationResult authResult;

    private String entitySet;

    private String entityType;

    public DynamicsCRMClient(ClientConfiguration clientConfiguration, String serviceRootURL, String entitySet)
            throws ServiceUnavailableException {
        this.clientConfiguration = clientConfiguration;

        this.serviceRootURL = serviceRootURL;

        this.entitySet = entitySet;

        init();
    }

    private void init() throws ServiceUnavailableException {
        odataClient = ODataClientFactory.getClient();
        if (clientConfiguration != null && serviceRootURL != null && serviceRootURL.indexOf("/api/data") > 0) {
            clientConfiguration.setResource(serviceRootURL.substring(0, serviceRootURL.indexOf("/api/data")));
        }
        authResult = getAccessToken();

        httpClientFactory = new DefaultHttpClientFactory() {

            @Override
            public DefaultHttpClient create(final HttpMethod method, final URI uri) {
                if (!clientConfiguration.isReuseHttpClient() || httpClient == null) {
                    httpClient = super.create(method, uri);

                    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), clientConfiguration.getTimeout() * 1000);
                    HttpConnectionParams.setSoTimeout(httpClient.getParams(), clientConfiguration.getTimeout() * 1000);

                    // setup proxy
                    setHttpclientProxy(httpClient);
                }
                return httpClient;
            }

        };
        odataClient.getConfiguration().setHttpClientFactory(httpClientFactory);

        httpClient = (DefaultHttpClient) httpClientFactory.create(null, null);

    }

    public ODataClient getClient() {
        return odataClient;
    }

    protected AuthenticationResult getAccessToken() throws ServiceUnavailableException {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(clientConfiguration.getAuthoryEndpoint(), false, service);
            Proxy proxy = getProxy();
            if (proxy != null) {
                context.setProxy(proxy);
            }
            Future<AuthenticationResult> future = context.acquireToken(clientConfiguration.getResource(),
                    clientConfiguration.getClientId(), clientConfiguration.getUserName(), clientConfiguration.getPassword(),
                    null);
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

    /**
     * Create EntitySet Iterator request
     * 
     * @param entirySet entirySet the EntitySet name which you want to retrieve records
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
        request.addCustomHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
        return request;
    }

    /**
     * Retrieve records from EntitySet
     * 
     * @param entitySet the EntitySet name which you want to retrieve records
     * @param queryOption
     * @return the entity set iterator object
     * 
     * @throws ServiceUnavailableException
     */
    public ClientEntitySet retrieveEntities(QueryOptionConfig queryOption) throws ServiceUnavailableException {
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
                    refreshToken();
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
     * @param entitySet entitySet the EntitySet name which you want to create record
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
     * Update entity with provided content
     * 
     * @param entitySet entitySet the EntitySet name which you want to update records
     * @param entity provide to update
     * @param updateType UpdateType.REPLACE(Full updates) or UpdateType.PATCH(Partial updates )
     * 
     * @throws ServiceUnavailableException
     */
    public HttpResponse updateEntity(ClientEntity entity, String keySegment) throws ServiceUnavailableException {
        URIBuilder updateURIBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet)
                .appendKeySegment(UUID.fromString(keySegment));
        HttpEntity httpEntity = convertToHttpEntity(entity);
        return createAndExecuteRequest(updateURIBuilder.build(), httpEntity, HttpMethod.PATCH);
    }

    /**
     * Deleted entity by key
     * 
     * @param entitySet entitySet the EntitySet name which you want to delete records
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
        request.addCustomHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
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
            httpClient = null;
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
        if (type != null && value != null) {
            entity.getProperties().add(odataClient.getObjectFactory().newPrimitiveProperty(propertyName,
                    odataClient.getObjectFactory().newPrimitiveValueBuilder().setType(type).setValue(value).build()));
        }

    }

    public void addEntityNavigationLink(ClientEntity entity, String lookupEntitySet, String navigationName,
            String linkedEntityId) {
        if (linkedEntityId != null) {
            try {
                entity.getNavigationLinks().add(odataClient.getObjectFactory().newEntityNavigationLink(navigationName,
                        new URI(lookupEntitySet + "(" + linkedEntityId + ")")));
            } catch (URISyntaxException e) {
                throw new HttpClientException(e);
            }
        }

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
                httpClient = (DefaultHttpClient) httpClientFactory.create(null, null);
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
                request.addHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
                if (request instanceof HttpEntityEnclosingRequestBase) {
                    ((HttpEntityEnclosingRequestBase) request).setEntity(httpEntity);
                }
                HttpResponse response = httpClient.execute(request);
                request.releaseConnection();
                if (isResponseSuccess(response.getStatusLine().getStatusCode())) {
                    EntityUtils.consume(response.getEntity());
                    return response;
                } else {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED && !hasRetried) {
                        refreshToken();
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
                if (retryTime < clientConfiguration.getMaxRetryTimes()) {
                    retryTime++;
                    try {
                        Thread.sleep(clientConfiguration.getIntervalTime());
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                    continue;// retry to refresh token
                }
                throw e;// failed to refresh token after retry
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

        Proxy proxy = getProxy();
        String proxyUser = System.getProperty("https.proxyUser");
        String proxyPwd = System.getProperty("https.proxyPassword");
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

    /**
     * Get the proxy setting if there is proxy for system
     */
    private Proxy getProxy() {
        String proxyHost = System.getProperty("https.proxyHost");
        String proxyPort = System.getProperty("https.proxyPort");
        if (proxyHost != null) {
            int port = -1;
            if (proxyPort != null && proxyPort.length() > 0) {
                port = Integer.parseInt(proxyPort);
            }
            SocketAddress addr = new InetSocketAddress(proxyHost, port);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
            return proxy;
        }
        return null;
    }

}
