// =====================================================================
// This file is part of the Microsoft Dynamics CRM SDK code samples.
//
// Copyright (C) Microsoft Corporation. All rights reserved.
//
// This source code is intended only as a supplement to Microsoft
// Development Tools and/or on-line documentation. See these other
// materials for detailed information regarding Microsoft code samples.
//
// THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
// KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
// PARTICULAR PURPOSE.
// =====================================================================

package org.talend.ms.crm.sdk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Utility to authenticate Microsoft account and Microsoft Office 365 (i.e. OSDP / OrgId) users without using the
 * classes exposed in Microsoft.Xrm.Sdk.dll
 */
public final class WsdlTokenManager {

    static Logger logger = Logger.getLogger(WsdlTokenManager.class.getName());

    private final String DeviceTokenTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + " <s:Envelope "
            + " xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\""
            + " xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\""
            + " xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\""
            + " xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\""
            + " xmlns:wsa=\"http://www.w3.org/2005/08/addressing\""
            + " xmlns:wst=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">" + "  <s:Header>"
            + "   <wsa:Action s:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue</wsa:Action>"
            + "   <wsa:To s:mustUnderstand=\"1\">http://Passport.NET/tb</wsa:To>    " + "   <wsse:Security>"
            + "     <wsse:UsernameToken wsu:Id=\"devicesoftware\">" + "       <wsse:Username>%s</wsse:Username>"
            + "       <wsse:Password>%s</wsse:Password>" + "     </wsse:UsernameToken>" + "   </wsse:Security>" + " </s:Header>"
            + " <s:Body>" + "   <wst:RequestSecurityToken Id=\"RST0\">"
            + "        <wst:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</wst:RequestType>"
            + "        <wsp:AppliesTo>" + "           <wsa:EndpointReference>"
            + "              <wsa:Address>http://Passport.NET/tb</wsa:Address>" + "           </wsa:EndpointReference>"
            + "        </wsp:AppliesTo>" + "     </wst:RequestSecurityToken>" + " </s:Body>" + " </s:Envelope>";

    private final String SecurityTokenSoapTemplate = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\""
            + " xmlns:a=\"http://www.w3.org/2005/08/addressing\""
            + " xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\""
            + " xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\""
            + " xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\""
            + " xmlns:t=\"http://schemas.xmlsoap.org/ws/2005/02/trust\"" + " xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\""
            + " xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" + "<s:Header>"
            + "    <a:Action s:mustUnderstand=\"1\">" + "    http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue</a:Action>"
            + "    <a:MessageID>urn:uuid:%s</a:MessageID>" + "    <a:ReplyTo>"
            + "      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>" + "    </a:ReplyTo>"
            + "    <a:To s:mustUnderstand=\"1\">http://Passport.NET/tb</a:To>" + "    <o:Security s:mustUnderstand=\"1\">"
            + "      <o:UsernameToken u:Id=\"user\">" + "        <o:Username>%s</o:Username>"
            + "        <o:Password>%s</o:Password>" + "      </o:UsernameToken>" + "        %s" + "    </o:Security>"
            + " </s:Header>" + "  <s:Body>" + "    <t:RequestSecurityToken>" + "      <wsp:AppliesTo>"
            + "        <a:EndpointReference>" + "          <a:Address>%s</a:Address>" + "        </a:EndpointReference>"
            + "      </wsp:AppliesTo>" + "     <wsp:PolicyReference URI=\"%s\"/>"
            + "      <t:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType>"
            + "    </t:RequestSecurityToken>" + "  </s:Body>" + " </s:Envelope>";

    private final String BinarySecurityToken = "      <wsse:BinarySecurityToken ValueType=\"urn:liveid:device\">"
            + "        <EncryptedData Id=\"BinaryDAToken0\"" + "        Type=\"http://www.w3.org/2001/04/xmlenc#Element\""
            + "        xmlns=\"http://www.w3.org/2001/04/xmlenc#\">"
            + "          <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#tripledes-cbc\">"
            + "          </EncryptionMethod>" + "          <ds:KeyInfo>"
            + "            <ds:KeyName>http://Passport.NET/STS</ds:KeyName>" + "          </ds:KeyInfo>"
            + "          <CipherData>" + "            <CipherValue>" + "              %s" + "            </CipherValue>"
            + "          </CipherData>" + "        </EncryptedData>" + "      </wsse:BinarySecurityToken>";

    /**
     * This method demonstrate authentication with CRM Online using soap and retrieve security data containing key
     * identifier and security tokens.
     * 
     * @param crmUrl CRM organization service url.
     * @param username User Name that should be used to connect to the server.
     * @param password Password that should be used to connect to the server.
     * @param appliesTo Indicates the AppliesTo that is required for the token
     * @param policy Policy that should be used when communicating with the server.
     * @param issuerUri URL for the current token issuer
     * @return security data
     */
    public SecurityData authenticate(String crmUrl, String username, String password, String appliesTo, String policy,
            URI issuerUri) throws IllegalStateException, SAXException, ParserConfigurationException,
            DeviceRegistrationFailedException, IOException, XPathExpressionException {
        // Check for null parameters
        if (crmUrl == null) {
            throw new NullPointerException("crmUrl");
        }
        if (username == null) {
            throw new NullPointerException("username");
        }
        if (password == null) {
            throw new NullPointerException("password");
        }
        if (appliesTo == null) {
            throw new NullPointerException("partner");
        }
        if (policy == null) {
            throw new NullPointerException("policy");
        }
        if (issuerUri == null) {
            throw new NullPointerException("issuerUri");
        }

        String binarySecurityTokenTemplate = "";

        if (issuerUri.toString().indexOf("login.live.com") != -1) {
            // Generates random credentials (Username, Password & Application ID) for the device
            // Sends the credentials to WLID and gets a PUID back
            DeviceCredentials deviceCredentials = DeviceIdManager.registerDevice();

            // Register Device Credentials and get binaryDAToken
            String soapTemplate = String.format(DeviceTokenTemplate, "11" + deviceCredentials.getDeviceName(),
                    deviceCredentials.getPassword());

            logger.debug("Device Credential Request: " + soapTemplate);

            String binaryDATokenXML = getSOAPResponse(issuerUri, soapTemplate);

            logger.debug("Response: " + binaryDATokenXML);

            String cipherValue = getValueFromXML(binaryDATokenXML, "//*[local-name()='CipherValue']");

            logger.debug("CipherValue: " + cipherValue);

            binarySecurityTokenTemplate = String.format(BinarySecurityToken, cipherValue);
        }

        // Get Security Token by sending WLID username, password and device binaryDAToken
        String securityTemplate = String.format(SecurityTokenSoapTemplate, UUID.randomUUID().toString(), username, password,
                binarySecurityTokenTemplate, appliesTo, policy);

        logger.debug("Security Token Request: " + securityTemplate);

        String securityTokenXML = getSOAPResponse(issuerUri, securityTemplate);

        logger.debug("Security Token Response: " + securityTokenXML);

        String faultString = getValueFromXML(securityTokenXML, "/Envelope/Body/Fault");
        if (faultString != null) {
            throw new RuntimeException(getValueFromXML(securityTokenXML, "/Envelope/Body/Fault/Reason") + ":"
                    + String.valueOf(getValueFromXML(securityTokenXML, "/Envelope/Body/Fault/Detail")).trim());
        }
        // Retrieve security tokens and key identifier from security token response.
        String securityToken0 = getValueFromXML(securityTokenXML, "//*[local-name()='CipherValue']");
        String securityToken1 = getValueFromXML(securityTokenXML, "(//*[local-name()='CipherValue'])[2]");
        String keyIdentifier = getValueFromXML(securityTokenXML, "//*[local-name()='KeyIdentifier']");

        logger.debug("Security Token 0: " + securityToken0);
        logger.debug("Security Token 1: " + securityToken1);
        logger.debug("Key Identifier: " + keyIdentifier);

        return new SecurityData(keyIdentifier, securityToken0, securityToken1);
    }

    /***
     * Get current and 5 minutes later GMT time in simple date format. e.g. "yyyy-MM-dd'T'HH:mm:ss"
     * 
     * @return RequestDateTimeData
     */
    public static RequestDateTimeData getRequestDateTime() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Calendar calendar = Calendar.getInstance(gmt);
        Calendar calendarFiveMinute = (Calendar) calendar.clone();

        calendarFiveMinute.add(Calendar.MINUTE, 5);

        Date binaryDARequestCreatedTime = calendar.getTime();
        Date fiveMinuteFromNow = calendarFiveMinute.getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        formatter.setTimeZone(gmt);

        return new RequestDateTimeData(formatter.format(binaryDARequestCreatedTime), formatter.format(fiveMinuteFromNow));
    }

    private String getValueFromXML(String inputXML, String xPathQuery)
            throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = builder.parse(new ByteArrayInputStream(inputXML.getBytes()));

            XPathFactory xpf = XPathFactory.newInstance();
            try {
                xpf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (XPathFactoryConfigurationException ex) {
                ExceptionHandler.process(ex);
            }
            XPath xpath = xpf.newXPath();
            String expression = xPathQuery;
            Node cipherValue = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
            return cipherValue == null ? null : cipherValue.getTextContent();
        } catch (XPathExpressionException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (SAXException e) {
            logger.error(e.getMessage());
            throw e;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private static String getSOAPResponse(URI issuerUri, String soapEnvelope) {
        HttpResponse response = null;

        // Create the request that will submit the request to the server
        try {
            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 180000);

            HttpClient client = new SystemDefaultHttpClient(params);
            HttpPost post = new HttpPost(issuerUri);
            StringEntity entity = new StringEntity(soapEnvelope);

            post.setHeader("Content-Type", "application/soap+xml; charset=UTF-8");
            post.setEntity(entity);

            response = client.execute(post);

            return EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return null;
    }
}
