package org.talend.mscrm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.cud.ODataDeleteRequest;
import org.apache.olingo.client.api.communication.request.cud.UpdateType;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.response.ODataDeleteResponse;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.http.HttpClientException;
import org.apache.olingo.client.api.serialization.ODataSerializer;
import org.apache.olingo.client.api.serialization.ODataSerializerException;
import org.apache.olingo.client.api.uri.QueryOption;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.HttpPatch;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.http.HttpHeader;

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

    private HttpClient httpClient;

    private boolean reuseHttpClient;

    private AuthenticationResult authResult;

    private String entitySet;

    private String entityType;

    private int maxRetryTimes = 5;

    // Retry intervalTime 1000(ms)
    private int intervalTime = 1000;

    // Default timeout 60(s)
    private int timeout = 60;

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
        newHttpClient();
    }

    private void newHttpClient() {
        httpClient = odataClient.getConfiguration().getHttpClientFactory().create(null, null);

        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), timeout * 1000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), timeout * 1000);

        // TODO proxy not implement
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
    public ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> createEntitySetIteratorRequest(
            QueryOptionConfig queryOption) {
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
        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = odataClient.getRetrieveRequestFactory()
                .getEntitySetIteratorRequest(uriBuilder.build());
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
    public ClientEntitySetIterator<ClientEntitySet, ClientEntity> retrieveEntities(QueryOptionConfig queryOption)
            throws ServiceUnavailableException {
        ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = createEntitySetIteratorRequest(queryOption);
        int retryTime = 0;
        while (true) {
            ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request.execute();
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                ClientEntitySetIterator<ClientEntitySet, ClientEntity> entitySetIterator = response.getBody();
                return entitySetIterator;
            } else {
                if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED && retryTime < maxRetryTimes) {
                    refreshToken();
                    request.addCustomHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
                    continue;
                }
                throw new HttpClientException(response.getStatusMessage());
            }
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
    public void insertEntity(ClientEntity entity) throws ServiceUnavailableException {
        URIBuilder uriBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet);
        HttpPost post = new HttpPost(uriBuilder.build());
        post.addHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
        HttpEntity httpEntity = convertToHttpEntity(entity);
        post.setEntity(httpEntity);
        executeRequest(post);
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
    public void updateEntity(ClientEntity entity, String keySegment) throws ServiceUnavailableException {
        URIBuilder uriBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet)
                .appendKeySegment(UUID.fromString(keySegment));
        HttpPatch patch = new HttpPatch(uriBuilder.build());
        patch.addHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
        HttpEntity httpEntity = convertToHttpEntity(entity);
        patch.setEntity(httpEntity);
        executeRequest(patch);
    }

    /**
     * Deleted entity by key
     * 
     * @param entitySet entitySet the EntitySet name which you want to delete records
     * @param keySegment Entity key segment
     * 
     * @throws ServiceUnavailableException
     */
    public void deleteEntity(String keySegment) throws ServiceUnavailableException {
        URIBuilder uriBuilder = odataClient.newURIBuilder(serviceRootURL).appendEntitySetSegment(entitySet)
                .appendKeySegment(UUID.fromString(keySegment));
        ODataDeleteRequest entityDeleteRequest = odataClient.getCUDRequestFactory().getDeleteRequest(uriBuilder.build());
        entityDeleteRequest.addCustomHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
        ODataDeleteResponse response = entityDeleteRequest.execute();
        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            refreshToken();
            entityDeleteRequest.addCustomHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
            entityDeleteRequest.execute();
        }
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

        ClientEntitySetIterator<ClientEntitySet, ClientEntity> entitySetIterator = response.getBody();
        if (entitySetIterator.hasNext()) {
            ClientProperty localName = entitySetIterator.next().getProperty("LogicalName");
            if (localName != null && localName.getValue() != null) {
                return localName.getValue().toString();
            }
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
     * Executes a request using the httpClient
     * 
     * @param httpClient the HTTP client which used to execute request
     * @param request the request to execute
     * 
     * @return the response to the request.
     * @throws ServiceUnavailableException
     */

    protected HttpResponse executeRequest(HttpUriRequest request) throws ServiceUnavailableException {
        if (!reuseHttpClient) {
            newHttpClient();
        }
        while (true) {
            try {
                HttpResponse response = httpClient.execute(request);
                if (isResponseSuccess(response.getStatusLine().getStatusCode())) {
                    return response;
                } else {
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        refreshToken();
                        request.addHeader(HttpHeader.AUTHORIZATION, "Bearer " + authResult.getAccessToken());
                        continue;
                    }
                    throw new HttpClientException(response.getStatusLine().getReasonPhrase());
                }
            } catch (Exception e) {
                // TODO retry is needed here?
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
                if (retryTime < maxRetryTimes) {
                    retryTime++;
                    try {
                        Thread.sleep(intervalTime);
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

    public void setMaxRetry(int maxRetry, int intervalTime) {
        this.maxRetryTimes = maxRetry;
        if (intervalTime > 0) {
            this.intervalTime = intervalTime;
        }

    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setReuseHttpClient(boolean reuseHttpClient) {
        this.reuseHttpClient = reuseHttpClient;
    }
}
