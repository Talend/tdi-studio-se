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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Random;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Management utility for the Device Id.
 */
public final class DeviceIdManager {

    /**
     * Logger
     */
    static final Logger Log = LoggerFactory.getLogger(DeviceIdManager.class.getName());

    private static final Random RandomInstance = new Random();

    /**
     * Register device.
     * 
     * @return device credentials
     * @throws IOException
     * @throws DeviceRegistrationFailedException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IllegalStateException
     */
    public static DeviceCredentials registerDevice() throws IllegalStateException, SAXException, ParserConfigurationException,
            DeviceRegistrationFailedException, IOException {
        return registerDevice(UUID.randomUUID());
    }

    /**
     * Register device and generate device credentials.
     * 
     * @param applicationId Application Id
     * @return device credentials
     */
    public static DeviceCredentials registerDevice(UUID applicationId) throws IllegalStateException, SAXException,
            ParserConfigurationException, DeviceRegistrationFailedException, IOException {
        if (applicationId == null)
            throw new NullPointerException("applicationId");
        // Create the credentials
        DeviceCredentials credentials = generateCredentials();

        // Execute the registration request
        // PUID is returned so we can use it (if needed)

        String puid = executeRegistrationRequest(LiveIdConstants.RegistrationEndpointUri, createRegistrationEnvelope(
                LiveIdConstants.DevicePrefix, applicationId, credentials.getDeviceName(), credentials.getPassword()));

        // Read the result
        return credentials;
    }

    /**
     * This method helps to create a xml envelope needed for device registration request.
     * 
     * @param prefix Sets the required prefix for the device name.
     * @param applicationId app GUID
     * @param deviceName name of the device
     * @param password password for the device
     * @return xml envelope comprising device add request
     */
    private static String createRegistrationEnvelope(String prefix, UUID applicationId, String deviceName, String password) {
        // The format of the envelope is the following:
        // <DeviceAddRequest>
        // <ClientInfo name="[app GUID]" version="1.0"/>
        // <Authentication>
        // <Membername>[prefix][device name]</Membername>
        // <Password>[device password]</Password>
        // </Authentication>
        // </DeviceAddRequest>

        // Instantiate the writer and write the envelope
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter envelope = new StringWriter();
        XMLStreamWriter xml = null;
        try {
            xml = factory.createXMLStreamWriter(envelope);

            // Create the node
            xml.writeStartElement("DeviceAddRequest");

            // Write the ClientInfo node
            xml.writeStartElement("ClientInfo");
            xml.writeAttribute("name", applicationId.toString());
            xml.writeAttribute("version", "1.0");
            xml.writeEndElement();

            // Write the Authentication node
            xml.writeStartElement("Authentication");

            xml.writeStartElement("Membername");
            xml.writeCharacters(prefix + deviceName);
            xml.writeEndElement();

            xml.writeStartElement("Password");
            xml.writeCharacters(password);
            xml.writeEndElement();

            xml.writeEndElement(); // </Authentication>

            xml.writeEndElement(); // </DeviceAddRequest>

        } catch (XMLStreamException e) {
            Log.error(e.getMessage());
        } finally {
            if (xml != null) {
                try {
                    xml.flush();
                    xml.close();
                } catch (XMLStreamException e) {
                    Log.error(e.getMessage());
                    // Ignore if it is already closed
                }

            }

        }

        return envelope.toString();
    }

    /**
     * This method demonstrate execution of device registration request.
     * 
     * @param url Registration uri
     * @param soapEnvelope Registration xml envelope
     * @return device registration puid
     */
    private static String executeRegistrationRequest(String url, String soapEnvelope) throws IllegalStateException, SAXException,
            ParserConfigurationException, DeviceRegistrationFailedException, IOException {
        HttpResponse response = null;

        // Create the request that will submit the request to the server
        try {
            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 180000);

            HttpClient client = new SystemDefaultHttpClient(params);

            // Uncomment following lines to view the traffic in fiddler.
            // HttpHost proxy = new HttpHost("localhost", 8888);
            // client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

            HttpPost post = new HttpPost(url);
            StringEntity entity = new StringEntity(soapEnvelope);

            post.setHeader("Content-Type", "application/soap+xml; charset=UTF-8");
            post.setEntity(entity);

            response = client.execute(post);

            return parseRegistrationResponse(response, false);
        } catch (ClientProtocolException e) {
            Log.error(e.getMessage());

            parseRegistrationResponse(response, true);

            throw e;
        } catch (IOException e) {
            Log.error(e.getMessage());

            parseRegistrationResponse(response, true);

            throw e;
        }
    }

    /**
     * This method parses the device registration response.
     * 
     * @param response device registration response.
     * @param isFailure
     * @return device registration puid.
     */
    private static String parseRegistrationResponse(HttpResponse response, Boolean isFailure) throws IllegalStateException,
            SAXException, IOException, ParserConfigurationException, DeviceRegistrationFailedException {
        HttpEntity entity = response.getEntity();

        if (entity != null) {

            // Sample Reply
            //
            // <DeviceAddResponse Success="true">
            // <success>true</success>
            // <puid>00187FFE89937F26</puid>
            // <ServerInfo ServerTime="2011-02-23T06:11:54Z">BAYIDSLGN1E55 2011.01.07.23.08.26</ServerInfo>
            // </DeviceAddResponse>

            // Parse the response in a XML document.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            try {
	            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (Exception e) {
            	Log.warn("failed to enable xml safe feature");
            }
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(entity.getContent());
            document.getDocumentElement().normalize();
            // Get to the root element of response.
            Node root = document.getDocumentElement();
            // Get all child nodes of the root element.
            NodeList children = root.getChildNodes();

            String errorSubCode = null;
            DeviceRegistrationErrorCode errorCode = null;
            Node node = null;

            // Moving through each child element of the response root node.
            // Look for puid or error.
            for (int i = 0; i < children.getLength(); i++) {
                node = children.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                // Get the puid and return it.
                if (node.getNodeName().toUpperCase().equals("PUID")) {
                    return node.getTextContent();
                }
                // In case of device registration failure, parse error code and errorsubcode nodes.
                if (isFailure) {
                    if (node.getNodeName().toUpperCase().equals("ERROR")) {
                        errorCode = DeviceRegistrationErrorCode.Unknown;
                        // Parse attributes of the error node.
                        NamedNodeMap attributes = node.getAttributes();
                        String codeLabel = attributes.getNamedItem("Code").getTextContent();
                        // Retrieve the errorCode.
                        if (codeLabel != null && codeLabel.startsWith("dc")) {
                            int code = Integer.parseInt(codeLabel.substring(2));

                            errorCode = TranslateErrorCode(code);
                        }
                    }
                    // Retrieve the errorsubcode.
                    if (node.getNodeName().toUpperCase().equals("ERRORSUBCODE")) {
                        errorSubCode = node.getTextContent();
                    }
                }
            }

            // If this was a failure, throw an exception
            if (isFailure) {
                throw new DeviceRegistrationFailedException(errorCode, errorSubCode);
            }
        }
        return null;
    }

    /**
     * This method generates random device name and password.
     * 
     * @return device credentials
     */
    private static DeviceCredentials generateCredentials() {
        String deviceName = generateRandomString(LiveIdConstants.ValidDeviceNameCharacters, LiveIdConstants.DeviceNameLength);

        String devicePassword = generateRandomString(LiveIdConstants.ValidDevicePasswordCharacters,
                LiveIdConstants.DevicePasswordLength);

        return new DeviceCredentials(deviceName, devicePassword);
    }

    /**
     * This method generates random string based on specified character set and length.
     * 
     * @param characterSet character set constraints
     * @param count length of the random string
     * @return random string
     */
    private static String generateRandomString(String characterSet, int count) {
        // Create an array of the characters that will hold the final list of random characters
        char[] value = new char[count];

        // Convert the character set to an array that can be randomly accessed
        char[] set = characterSet.toCharArray();

        synchronized (RandomInstance) {
            // Populate the array with random characters from the character set
            for (int i = 0; i < count; i++) {
                value[i] = set[RandomInstance.nextInt(set.length)];
            }
        }

        return new String(value);
    }

    /**
     * This method translates int error code value to DeviceRegistrationErrorCode.
     * 
     * @param errorCode int value of error code.
     * @return device registration error code
     */
    private static DeviceRegistrationErrorCode TranslateErrorCode(int errorCode) {
        DeviceRegistrationErrorCode code;

        switch (errorCode) {
        case 0:
            code = DeviceRegistrationErrorCode.Unknown;
            break;
        case 1:
            code = DeviceRegistrationErrorCode.InterfaceDisabled;
            break;
        case 3:
            code = DeviceRegistrationErrorCode.InvalidRequestFormat;
            break;
        case 4:
            code = DeviceRegistrationErrorCode.UnknownClientVersion;
            break;
        case 6:
            code = DeviceRegistrationErrorCode.BlankPassword;
            break;
        case 7:
            code = DeviceRegistrationErrorCode.MissingDeviceUserNameOrPassword;
            break;
        case 8:
            code = DeviceRegistrationErrorCode.InvalidParameterSyntax;
            break;
        case 11:
            code = DeviceRegistrationErrorCode.InterfaceDisabled;
            break;
        case 13:
            code = DeviceRegistrationErrorCode.DeviceAlreadyExists;
            break;
        default:
            code = DeviceRegistrationErrorCode.Unknown;
        }

        return code;
    }

    /**
     * Stores all data needed to perform device registration.
     */
    private final class LiveIdConstants {

        public static final String RegistrationEndpointUri = "https://login.live.com/ppsecure/DeviceAddCredential.srf";

        public static final String DevicePrefix = "11";

        public static final String ValidDeviceNameCharacters = "0123456789abcdefghijklmnopqrstuvqxyz";

        public static final int DeviceNameLength = 24;

        public static final String ValidDevicePasswordCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^*()-_=+; ,./?`~";

        public static final int DevicePasswordLength = 24;
    }
}
