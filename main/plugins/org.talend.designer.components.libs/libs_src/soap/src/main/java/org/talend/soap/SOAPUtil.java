package org.talend.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.dom4j.io.DOMReader;
import org.talend.soap.sun.SunNtlmAuthenticationUpdater;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.xml.messaging.saaj.soap.SOAPPartImpl;

public class SOAPUtil {

    private static final String vmVendor = System.getProperty("java.vendor.url");

    private static final String sunVmVendor = "http://java.sun.com/";

    private static final String oracleVmVendor = "http://java.oracle.com/";

    private SOAPConnection connection;

    public static final String SOAP11 = "soap11";

    public static final String SOAP12 = "soap12";

    private String reBodyMessage;

    private String reHeaderMessage;

    private String reFaultMessage;

    private String username;

    private String password;

    private boolean basicAuth;

    private boolean hasFault = false;

    public SOAPUtil() throws SOAPException {
        init();
    }

    private void init() throws SOAPException {
        SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
        connection = soapConnFactory.createConnection();
    }

    public void setBasicAuth(final String username, final String password) {
        this.username = username;
        this.password = password;
        this.basicAuth = true;
        setAuthenticator(username, password);
    }

    public void setAuthenticator(final String username, final String password) {
        Authenticator.setDefault(new Authenticator() {

            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });
    }

    public void setNTLMAuth(String domain, String username, String password) {
        setAuthenticator(domain + "\\" + username, password);
    }

    public void enforceNtlmCredentials() {
        if (vmVendor.equals(sunVmVendor) || vmVendor.equals(oracleVmVendor)) {
            SunNtlmAuthenticationUpdater.getInstance().resetNtlmAuthenticationCallback();
        }
    }

    public void setProxy(String host, int port, String username, String password) {
        System.setProperty("proxySet", "true");
        System.setProperty("proxyHost", host);
        System.setProperty("proxyPort", String.valueOf(port));
        System.setProperty("http.proxyUser", username);
        System.setProperty("http.proxyPassword", password);
    }

    public void invokeSOAP(String version, String destination, String soapAction, String soapMessage) throws SOAPException,
            TransformerException, ParserConfigurationException, FileNotFoundException, UnsupportedEncodingException {
        MessageFactory messageFactory = null;
        if (version.equals(SOAP12)) {
            messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        } else {
            messageFactory = MessageFactory.newInstance();
        }
        SOAPMessage message = messageFactory.createMessage();
        MimeHeaders mimeHeaders = message.getMimeHeaders();

        setSoapAction(version, soapAction, mimeHeaders);

        if (basicAuth) {
            addBasicAuthHeader(mimeHeaders, username, password);
        }

        // Create objects for the message parts
        SOAPPart soapPart = message.getSOAPPart();

        String encoding = getEncoding(soapMessage);
        
        ByteArrayInputStream stream = new ByteArrayInputStream(soapMessage.getBytes(encoding));
        StreamSource preppedMsgSrc = new StreamSource(stream);
        soapPart.setContent(preppedMsgSrc);

        message.saveChanges();

        SOAPMessage reply = connection.call(message, destination);

        SOAPPart reSoapPart = reply.getSOAPPart();
        
        if (reSoapPart != null && reSoapPart instanceof SOAPPartImpl) {
			((SOAPPartImpl) reSoapPart).setSourceCharsetEncoding(encoding);
		}
        
        SOAPEnvelope reEnvelope = reSoapPart.getEnvelope();

        SOAPHeader reHeader = reEnvelope.getHeader();
        if (reHeader != null) {
            setReHeaderMessage(Doc2StringWithoutDeclare(extractContentAsDocument(reHeader)));
        }
        SOAPBody reBody = reEnvelope.getBody();
        if (reBody.getFault() != null) {
            hasFault = true;
            setReFaultMessage(Doc2StringWithoutDeclare(extractContentAsDocument(reBody)));
            setReBodyMessage(null);
        } else {
            hasFault = false;
            if (reBody.getChildNodes().getLength() < 1) {
                setReBodyMessage(null);
            } else if (reBody.getChildNodes().getLength() == 1 && reBody.getChildNodes().item(0) instanceof javax.xml.soap.Text) {
                setReBodyMessage(null);
            } else {
                setReBodyMessage(Doc2StringWithoutDeclare(extractContentAsDocument(reBody)));
            }
            setReFaultMessage(null);
        }
    }

    public void close() throws SOAPException {
        connection.close();
    }

    public boolean hasFault() {
        return hasFault;
    }

    private void setReFaultMessage(String faultMessageStr) {
        reFaultMessage = faultMessageStr;
    }

    public String getReFaultMessage() {
        return reFaultMessage;
    }

    private Document extractContentAsDocument(SOAPBody body) throws SOAPException, TransformerException,
            ParserConfigurationException {
        return body.extractContentAsDocument();
    }

    private void setReBodyMessage(String bodyMessageStr) {
        reBodyMessage = bodyMessageStr;
    }

    public String getReBodyMessage() {
        return reBodyMessage;
    }

    private Document extractContentAsDocument(SOAPHeader header) throws ParserConfigurationException, TransformerException {
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        try {
        	factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        	factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception e) {
        	//do nothing
        }
        
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();

        Node content;
        Element headerRootElem = document.createElement("Header");

        Iterator<javax.xml.soap.Node> childElements = header.getChildElements();
        org.w3c.dom.Node domNode = null;
        while (childElements.hasNext()) {
            domNode = (org.w3c.dom.Node) childElements.next();
            content = (Node) document.importNode(domNode, true);
            headerRootElem.appendChild(content);
        }
        document.appendChild(headerRootElem);
        return document;
    }

    private void setReHeaderMessage(String headerMessageStr) {
        reHeaderMessage = headerMessageStr;
    }

    public String getReHeaderMessage() {
        return reHeaderMessage;
    }

    private String Doc2StringWithoutDeclare(Document doc) {
        DOMReader reader = new DOMReader();
        org.dom4j.Document document = reader.read(doc);
        return document.getRootElement().asXML();
    }

	/**
	 * invoke soap and return the response document
	 */
	public String extractContentAsDocument(String version, String destination, String soapAction, String soapMessage) throws SOAPException,
            TransformerException, ParserConfigurationException, FileNotFoundException, UnsupportedEncodingException {
    	MessageFactory messageFactory = null;
    	if (version.equals(SOAP12)) {
    		messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    	} else {
    		messageFactory = MessageFactory.newInstance();
    	}
    	SOAPMessage message = messageFactory.createMessage();
    	MimeHeaders mimeHeaders = message.getMimeHeaders();
        setSoapAction(version, soapAction, mimeHeaders);
        SOAPPart soapPart = message.getSOAPPart();
    	
    	String encoding = getEncoding(soapMessage);
    	
    	ByteArrayInputStream stream = new ByteArrayInputStream(soapMessage.getBytes(encoding));
    	StreamSource preppedMsgSrc = new StreamSource(stream);
    	soapPart.setContent(preppedMsgSrc);
    	message.saveChanges();
    	SOAPMessage reply = connection.call(message, destination);
    	SOAPPart reSoapPart = reply.getSOAPPart();
    	
    	if (reSoapPart != null && reSoapPart instanceof SOAPPartImpl) {
			((SOAPPartImpl) reSoapPart).setSourceCharsetEncoding(encoding);
		}
    	
    	try {
			TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
			Transformer t = tf.newTransformer();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			t.transform(reSoapPart.getContent(), new StreamResult(bos));
			return bos.toString(encoding);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

    /* https://jira.talendforge.org/browse/TDI-42581 skip add SOAPAction directly to header v1.2 */
    private void setSoapAction(String version, String soapAction, MimeHeaders mimeHeaders) {
        if (SOAP12.equals(version)) {
            // in soap version 1.2 param 'action' optional and should not be empty
            if( soapAction != null && !soapAction.trim().isEmpty()) {
                mimeHeaders.setHeader("Content-Type", "application/soap+xml; charset=utf-8; action=\"" + soapAction + "\"");
            }
        } else {
            mimeHeaders.setHeader("SOAPAction", soapAction);
        }
    }

    private String getEncoding(String text) {
        String result = Charset.defaultCharset().name();

        if(text == null) {
			return result;
		}
        
        char[] match = {'<','?','x','m','l'};
        boolean found = false;
        int i = 0;
        int j = 0;
        for(;i<text.length();i++) {
        	if(j==0 && text.charAt(i) <= ' ') {
        		continue;
        	}
        	
        	if(j<match.length && text.charAt(i) == match[j++]) {
        		if(j==match.length) {
        			found = true;
        			break;
        		}
        	} else {
        		break;
        	}
        }
        
        if (found) {
            int end = text.indexOf("?>");
            String sub = text.substring(i+1, end);
            StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();

                if ("encoding".equals(token)) {
                    if (tokens.hasMoreTokens()) {
                        String encoding = tokens.nextToken();
                        if(Charset.isSupported(encoding)) {
                        	result = encoding;
                        	return result;
                        }
                    }

                    break;
                }
            }
        }

        return result;
    }
	
	private void addBasicAuthHeader(MimeHeaders headers, String username, String password) {
        String encodeUserInfo = username + ":" + password;
        encodeUserInfo = Base64.encodeBase64String(encodeUserInfo.getBytes());
        headers.setHeader("Authorization", "Basic " + encodeUserInfo);
    }
	
}
