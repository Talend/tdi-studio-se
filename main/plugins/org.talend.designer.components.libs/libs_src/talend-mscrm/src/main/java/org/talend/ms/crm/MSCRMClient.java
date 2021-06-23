package org.talend.ms.crm;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.UUID;

import javax.naming.AuthenticationException;
import javax.naming.ServiceUnavailableException;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.http.HttpTransportProperties.ProxyProperties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.client.api.http.HttpClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.ms.crm.odata.ClientConfiguration;
import org.talend.ms.crm.odata.authentication.AuthStrategyFactory;
import org.talend.ms.crm.odata.authentication.IAuthStrategy;
import org.talend.ms.crm.sdk.Instance;
import org.talend.ms.crm.sdk.OnlineAuthenticationPolicy;
import org.talend.ms.crm.sdk.OrganizationServiceStubWrapper;
import org.talend.ms.crm.sdk.RequestDateTimeData;
import org.talend.ms.crm.sdk.SecurityData;
import org.talend.ms.crm.sdk.WsdlTokenManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.schemas.xrm._2011.contracts.OrganizationServiceStub;

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

/**
 * created by bchen on Jun 26, 2013 Detailled comment
 *
 */
public class MSCRMClient {

    static Logger logger = LoggerFactory.getLogger(MSCRMClient.class.getName());

    /**
     * Unique Name of the organization
     */
    private String orgName;
    
    /**
     * Unique Name of the organization
     */
    private ClientConfiguration clientConfiguration;

    /**
     * Suffix for the Flat WSDL
     */
    private static final String FlatWSDLSuffix = "?wsdl";

    private OrganizationServiceStub serviceStub;

	private String discoveryServiceURL = "https://globaldisco.crm.dynamics.com/api/discovery/v2.0/Instances";
	
	/**
     * This sample application registration values are available for all online instances
     * This is suggested to use for development and prototyping purposes.For production use, you should create an AppId or ClientId that is specific 
     * to your tenant in the Azure Management portal.
     * 
     */
    private static final String DEFAULT_CLIENT_ID = "51f81489-12ee-4a9e-aaae-a2591f45987d";
	
	private IAuthStrategy authStrategy;

    public MSCRMClient(ClientConfiguration clientConfiguration, String orgName, String discoveryServiceURL) throws AuthenticationException {
    	this.clientConfiguration = clientConfiguration;
    	this.orgName = orgName;
        this.discoveryServiceURL  = discoveryServiceURL;
        init();

    }
    
    private void init() throws AuthenticationException {
        if (clientConfiguration != null && discoveryServiceURL != null && discoveryServiceURL.indexOf("/api/discovery/") > 0) {
            clientConfiguration.setResource(discoveryServiceURL.substring(0, discoveryServiceURL.indexOf("/api/discovery/")));
        }
        
        if(clientConfiguration != null) {
        	if (discoveryServiceURL != null && discoveryServiceURL.indexOf("/api/discovery/") > 0) {
                clientConfiguration.setResource(discoveryServiceURL.substring(0, discoveryServiceURL.indexOf("/api/discovery/")));
            }
        	if (clientConfiguration.getClientId()==null || clientConfiguration.getClientId().isEmpty()) {
                clientConfiguration.setClientId(DEFAULT_CLIENT_ID);
            }
        }

        authStrategy = AuthStrategyFactory.createAuthStrategy(this.clientConfiguration);
        authStrategy.init();

    }


    public OrganizationServiceStub getOnlineConnection() throws Exception {
        return new OrganizationServiceStubWrapper(doGetOnlineConnection(), this, discoveryServiceURL,
        		clientConfiguration.getMaxRetryTimes(), clientConfiguration.getIntervalTime());
    }

    /**
     * Organization information is stored in the Instance table of the Discovery Service. To see the kind of information contained in that table, 
     * send an HTTP GET request to the service for one of your instances.
     * environments refer http://technet.microsoft.com/en-us/library/gg309401.aspx
     *
     * @throws Exception
     */
    public OrganizationServiceStub doGetOnlineConnection() throws Exception {

    	String serviceURL = discoveryServiceURL;
    	if(!discoveryServiceURL.contains("(")) {
    		serviceURL =  discoveryServiceURL+"(UniqueName='"+orgName+"')";
    	}
		
		String orgUrl = getOrgURL( serviceURL);
		// The discovery service stub cannot be reused against the organization service
		// as the Issuer and AppliesTo may differ between the discovery and organization services.
		// Retrieve the authentication policy for the organization service.
		OnlineAuthenticationPolicy organizationPolicy = new OnlineAuthenticationPolicy(orgUrl + FlatWSDLSuffix);
		WsdlTokenManager orgTokenManager = new WsdlTokenManager();
		// Authenticate the user using the organization authentication policy.
		SecurityData securityData = orgTokenManager.authenticate(orgUrl, clientConfiguration.getUserName(), clientConfiguration.getPassword(),
		        organizationPolicy.getAppliesTo(), organizationPolicy.getPolicy(), organizationPolicy.getIssuerUri());

		// Retrieve organization stub using organization URL with the security data.
		serviceStub = createOrganizationServiceStub(orgUrl, securityData);

		Options options = serviceStub._getServiceClient().getOptions();
		
	    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.REUSE_HTTP_CLIENT, clientConfiguration.isReuseHttpClient());
	    options.setTimeOutInMilliSeconds(Long.valueOf(clientConfiguration.getTimeout()));
	    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, clientConfiguration.getTimeout());
	    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, clientConfiguration.getTimeout());
        return serviceStub;

    }

    private static OrganizationServiceStub createOrganizationServiceStub(String organizationServiceURL, SecurityData securityData)
            throws RemoteException, XMLStreamException {
        try {
            OrganizationServiceStub stub = new OrganizationServiceStub(getConfigurationContext(), organizationServiceURL);
            setServiceClientOptions(stub._getServiceClient(), securityData);
            return stub;
        } catch (RemoteException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private static void setServiceClientOptions(ServiceClient sc, SecurityData securityData) throws AxisFault, XMLStreamException {
        Options options = sc.getOptions();

        options.setMessageId("urn:uuid:" + UUID.randomUUID().toString());

        EndpointReference endPoint = new EndpointReference("http://www.w3.org/2005/08/addressing/anonymous");
        options.setReplyTo(endPoint);

        sc.setOptions(options);
        sc.addHeader(createCRMSecurityHeaderBlock(securityData));
        HttpTransportProperties.ProxyProperties proxyProps = getProxyProperties();
        if (proxyProps != null) {
            sc.getOptions().setProperty(HTTPConstants.PROXY, proxyProps);
        }
        try {
            sc.engageModule("addressing");
        } catch (AxisFault e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private static SOAPHeaderBlock createCRMSecurityHeaderBlock(SecurityData securityData) throws XMLStreamException {
        RequestDateTimeData dateTimeData = WsdlTokenManager.getRequestDateTime();

        String currentDateTime = dateTimeData.getCreatedDateTime();
        String expireDateTime = dateTimeData.getExpiresDateTime();

        String securityHeaderTemplate = "<EncryptedData "
                + "    xmlns=\"http://www.w3.org/2001/04/xmlenc#\""
                + "     Id=\"Assertion0\" "
                + "    Type=\"http://www.w3.org/2001/04/xmlenc#Element\">"
                + "    <EncryptionMethod "
                + "        Algorithm=\"http://www.w3.org/2001/04/xmlenc#tripledes-cbc\"/>"
                + "    <ds:KeyInfo "
                + "        xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">"
                + "        <EncryptedKey>"
                + "            <EncryptionMethod "
                + "                Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\"/>"
                + "            <ds:KeyInfo Id=\"keyinfo\">"
                + "                <wsse:SecurityTokenReference "
                + "                    xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
                + "                    <wsse:KeyIdentifier "
                + "                        EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" "
                + "                        ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier\">%s</wsse:KeyIdentifier>"
                + "                </wsse:SecurityTokenReference>" + "            </ds:KeyInfo>" + "            <CipherData>"
                + "                <CipherValue>%s</CipherValue>" + "            </CipherData>" + "        </EncryptedKey>"
                + "    </ds:KeyInfo>" + "    <CipherData>" + "        <CipherValue>%s</CipherValue>" + "    </CipherData>"
                + "</EncryptedData>";

        String securityHeader = String.format(securityHeaderTemplate, securityData.getKeyIdentifier(),
                securityData.getSecurityToken0(), securityData.getSecurityToken1());

        try {

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMNamespace securityNS = factory.createOMNamespace(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "o");
            OMNamespace utitlityNS = factory.createOMNamespace(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "u");

            OMElement timeStamp = factory.createOMElement("Timestamp", utitlityNS);
            timeStamp.addAttribute("Id", "_0", utitlityNS);

            OMElement created = factory.createOMElement("Created", utitlityNS);
            OMText createdTime = factory.createOMText(currentDateTime + "Z");
            created.addChild(createdTime);

            OMElement expires = factory.createOMElement("Expires", utitlityNS);
            OMText expiresTime = factory.createOMText(expireDateTime + "Z");
            expires.addChild(expiresTime);

            timeStamp.addChild(created);
            timeStamp.addChild(expires);

            SOAPHeaderBlock wsseHeader = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock("Security", securityNS);
            wsseHeader.setMustUnderstand(true);

            wsseHeader.addChild(timeStamp);
            wsseHeader.addChild(AXIOMUtil.stringToOM(factory, securityHeader));

            return wsseHeader;
        } catch (XMLStreamException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private static ConfigurationContext getConfigurationContext() throws AxisFault {
        String workingDirectory = System.getProperty("user.dir");
        String fileSeperator = System.getProperty("file.separator");
        String pathToAxis2File = workingDirectory + fileSeperator + "src" + fileSeperator + "axis2.xml";

        logger.debug("Working directory: " + workingDirectory);
        logger.debug("Path to Axis2.xml file: " + pathToAxis2File);

        ConfigurationContext ctx;
        try {
            // ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(workingDirectory,
            // pathToAxis2File);
            ctx = ConfigurationContextFactory.createConfigurationContextFromURIs(
                    MSCRMClient.class.getClassLoader().getResource("org/talend/ms/crm/sdk/axis2_mscrm.xml"), null);
        } catch (AxisFault e) {
            logger.error(e.getMessage());
            throw e;
        }
        return ctx;
    }

    private static HttpTransportProperties.ProxyProperties getProxyProperties() {
        String proxyHost = null;
        String proxyPort = null;
        String proxyUser = null;
        String proxyPwd = null;
        HttpTransportProperties.ProxyProperties proxyProps = null;
        // set by other components like tSetProxy
        if (System.getProperty("https.proxyHost") != null) {
            proxyHost = System.getProperty("https.proxyHost");
            proxyPort = System.getProperty("https.proxyPort");
            proxyUser = System.getProperty("https.proxyUser");
            proxyPwd = System.getProperty("https.proxyPassword");
        }

        if (proxyHost != null) {
            proxyProps = new ProxyProperties();
            proxyProps.setUserName((proxyUser == null ? "" : proxyUser));
            proxyProps.setPassWord(proxyPwd);
            proxyProps.setProxyName(proxyHost);
            if (proxyPort != null && proxyPort.length() > 0) {
                proxyProps.setProxyPort(Integer.parseInt(proxyPort));
            }
        }
        return proxyProps;
    }
    
    private String getOrgURL(String discoveryServiceURL) throws ServiceUnavailableException {
        try {
            HttpGet request = new HttpGet(discoveryServiceURL);
    		
            // https://docs.microsoft.com/en-us/powerapps/developer/data-platform/webapi/discover-url-organization-web-api#authentication
            authStrategy.configureRequest(request);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(request)) {
            	
            	int statusCode = response.getStatusLine().getStatusCode();
            	if(statusCode != HttpStatus.SC_NO_CONTENT && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_OK) {
                    String message = null;
                    if (statusCode == HttpStatus.SC_NOT_FOUND ) {
                        message = "The organization '"+orgName+"' does not exist.";
                    } else {
                        message = response.getStatusLine().getReasonPhrase();
                    }
                    throw new HttpClientException(message);
            	}

                HttpEntity entity = response.getEntity();
                String orgUrl = null;
                if (entity != null) {
                	String result = EntityUtils.toString(entity);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    JsonNode tree = mapper.readTree(result);
                    Iterator<JsonNode> iter = tree.path("value").elements();
                    while (iter.hasNext()){
                        JsonNode node = iter.next();
                        Instance instance = mapper.readValue(node.toString(), Instance.class);
                        // Should only return one instance.
                        if(orgName.equals(instance.getUniqueName())) {
                        	orgUrl = instance.getApiUrl() + "/XRMServices/2011/Organization.svc";
                        	break;
                        }
                    }
                }
                if(orgUrl == null) {
                	throw new HttpClientException("No organization available.");
                }
                return orgUrl;
            }
        } catch (Exception e) {
            throw new ServiceUnavailableException(e.getMessage());
        } 
	}
    
}
