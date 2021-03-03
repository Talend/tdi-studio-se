package org.talend.ms.crm;

import java.rmi.RemoteException;
import java.util.UUID;

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
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.http.HttpTransportProperties.ProxyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.datacontract.schemas._2004._07.system_collections_generic.KeyValuePairOfEndpointTypestringztYlk6OT;
import org.talend.ms.crm.sdk.OnlineAuthenticationPolicy;
import org.talend.ms.crm.sdk.OrganizationServiceStubWrapper;
import org.talend.ms.crm.sdk.RequestDateTimeData;
import org.talend.ms.crm.sdk.SecurityData;
import org.talend.ms.crm.sdk.WsdlTokenManager;

import com.microsoft.schemas.xrm._2011.contracts.DiscoveryServiceStub;
import com.microsoft.schemas.xrm._2011.contracts.IDiscoveryService_Execute_DiscoveryServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.OrganizationServiceStub;
import com.microsoft.schemas.xrm._2011.contracts.discovery.EndpointType;
import com.microsoft.schemas.xrm._2011.contracts.discovery.ExecuteDocument;
import com.microsoft.schemas.xrm._2011.contracts.discovery.ExecuteDocument.Execute;
import com.microsoft.schemas.xrm._2011.contracts.discovery.ExecuteResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.discovery.ExecuteResponseDocument.ExecuteResponse;
import com.microsoft.schemas.xrm._2011.contracts.discovery.OrganizationDetail;
import com.microsoft.schemas.xrm._2011.contracts.discovery.RetrieveOrganizationRequest;
import com.microsoft.schemas.xrm._2011.contracts.discovery.RetrieveOrganizationResponse;

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
     * Microsoft account (e.g. youremail@live.com) or Microsoft Office 365 (Org ID e.g.
     * youremail@yourorg.onmicrosoft.com) User Name.
     */
    private String username;

    /**
     * Microsoft account or Microsoft Office 365 (Org ID) Password.
     */
    private String password;

    /**
     * Unique Name of the organization
     */
    private String orgName;

    /**
     * Suffix for the Flat WSDL
     */
    private static final String FlatWSDLSuffix = "?wsdl";

    private OrganizationServiceStub serviceStub;

    private Integer timeout;

    private Boolean reuseHttpClient;

    private int maxConnectionRetries = 5;

    private int attemptsInterval = 1000;

    public MSCRMClient(String username, String password, String orgName) {
        this.username = username;
        this.password = password;
        this.orgName = orgName;

    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public void setReuseHttpClient(Boolean reuseHttpClient) {
        this.reuseHttpClient = reuseHttpClient;
    }

    public void setMaxConnectionRetries(int maxConnectionRetries) {
        this.maxConnectionRetries = maxConnectionRetries;
    }

    public void setAttemptsInterval(int attemptsInterval) {
        this.attemptsInterval = attemptsInterval;
    }

    public OrganizationServiceStub getOnlineConnection(String discoveryServiceURL) throws Exception {
        return new OrganizationServiceStubWrapper(doGetOnlineConnection(discoveryServiceURL), this, discoveryServiceURL,
                maxConnectionRetries, attemptsInterval);
    }

    /**
     * URL for the Discovery Service For North America Microsoft account, discovery service url is
     * https://dev.crm.dynamics.com/XRMServices/2011/Discovery.svc Microsoft office 365, discovery service url is
     * https://disco.crm.dynamics.com/XRMServices/2011/Discovery.svc To use appropriate discovery service url for other
     * environments refer http://technet.microsoft.com/en-us/library/gg309401.aspx
     *
     * @throws Exception
     */
    public OrganizationServiceStub doGetOnlineConnection(String discoveryServiceURL) throws Exception {

        try {
            // Retrieve the authentication policy for the discovery service.
            OnlineAuthenticationPolicy discoveryPolicy = new OnlineAuthenticationPolicy(discoveryServiceURL + FlatWSDLSuffix);
            WsdlTokenManager discoeryTokenManager = new WsdlTokenManager();
            // Authenticate the user using the discovery authentication policy.
            SecurityData discoverySecurityData = discoeryTokenManager.authenticate(discoveryServiceURL, username, password,
                    discoveryPolicy.getAppliesTo(), discoveryPolicy.getPolicy(), discoveryPolicy.getIssuerUri());
            // Retrieve discovery stub using organization URL with the security data.
            DiscoveryServiceStub discoveryServiceStub = createDiscoveryServiceStub(discoveryServiceURL, discoverySecurityData);
            // Retrieve organization service url using discovery stub.
            String orgUrl = discoverOrganizationUrl(discoveryServiceStub, orgName);
            // The discovery service stub cannot be reused against the organization service
            // as the Issuer and AppliesTo may differ between the discovery and organization services.
            // Retrieve the authentication policy for the organization service.
            OnlineAuthenticationPolicy organizationPolicy = new OnlineAuthenticationPolicy(orgUrl + FlatWSDLSuffix);
            WsdlTokenManager orgTokenManager = new WsdlTokenManager();
            // Authenticate the user using the organization authentication policy.
            SecurityData securityData = orgTokenManager.authenticate(orgUrl, username, password,
                    organizationPolicy.getAppliesTo(), organizationPolicy.getPolicy(), organizationPolicy.getIssuerUri());

            // Retrieve organization stub using organization URL with the security data.
            serviceStub = createOrganizationServiceStub(orgUrl, securityData);

            Options options = serviceStub._getServiceClient().getOptions();
            if (reuseHttpClient != null) {
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.REUSE_HTTP_CLIENT, reuseHttpClient);
            }
            if (timeout != null) {
                options.setTimeOutInMilliSeconds(Long.valueOf(timeout));
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT, timeout);
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT, timeout);
            }

        } catch (IDiscoveryService_Execute_DiscoveryServiceFaultFault_FaultMessage e) {
            throw new Exception(e.getFaultMessage().getDiscoveryServiceFault().getMessage());
        }
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

    private static DiscoveryServiceStub createDiscoveryServiceStub(String discoveryServiceURL, SecurityData securityData)
            throws RemoteException, XMLStreamException {
        try {
            DiscoveryServiceStub stub = new DiscoveryServiceStub(getConfigurationContext(), discoveryServiceURL);
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

    private static String discoverOrganizationUrl(DiscoveryServiceStub serviceStub, String organizationUniqueName)
            throws RemoteException, IDiscoveryService_Execute_DiscoveryServiceFaultFault_FaultMessage {
        try {
            RetrieveOrganizationRequest request = RetrieveOrganizationRequest.Factory.newInstance();

            request.setUniqueName(organizationUniqueName);

            Execute exe = Execute.Factory.newInstance();
            exe.setRequest(request);

            ExecuteDocument exeDoc = ExecuteDocument.Factory.newInstance();
            exeDoc.setExecute(exe);

            ExecuteResponseDocument executeRespDoc = serviceStub.execute(exeDoc);
            ExecuteResponse executeResp = executeRespDoc.getExecuteResponse();

            RetrieveOrganizationResponse result = (RetrieveOrganizationResponse) executeResp.getExecuteResult();

            OrganizationDetail orgDetail = result.getDetail();

            KeyValuePairOfEndpointTypestringztYlk6OT[] keyValuePairs = orgDetail.getEndpoints()
                    .getKeyValuePairOfEndpointTypestringztYlk6OTArray();

            for (KeyValuePairOfEndpointTypestringztYlk6OT keyValuePair : keyValuePairs) {

                if (keyValuePair.getKey() == EndpointType.ORGANIZATION_SERVICE) {
                    return keyValuePair.getValue();
                }
            }
        } catch (RemoteException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (IDiscoveryService_Execute_DiscoveryServiceFaultFault_FaultMessage e) {
            logger.error(e.getMessage());
            throw e;
        }
        return null;
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

}
