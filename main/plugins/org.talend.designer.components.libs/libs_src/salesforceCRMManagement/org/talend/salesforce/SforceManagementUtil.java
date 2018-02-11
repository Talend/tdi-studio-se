// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.salesforce;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties.ProxyProperties;

import com.salesforce.soap.partner.SforceServiceStub;

/**
 * created by bchen on Jul 8, 2014 Detailled comment
 *
 */
public class SforceManagementUtil {

    public static OMElement newOMElement(String name, String value) throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement ome = fac.createOMElement(name, null);
        ome.addChild(fac.createOMText(ome, value));
        return ome;
    }

    public static void setEndpoint(SforceServiceStub stub, String endpoint) {
        stub._getServiceClient().getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(endpoint));
    }

    public static void needCompression(SforceServiceStub stub, boolean needCompression) {
        if (!needCompression) {
            return;
        }
        Options options = stub._getServiceClient().getOptions();
        options.setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);
        options.setProperty(HTTPConstants.MC_GZIP_REQUEST, Boolean.TRUE);
    }

    public static void useHttpChunked(SforceServiceStub stub, boolean chunked){
        Options options = stub._getServiceClient().getOptions();
        if(chunked){
            options.setProperty(HTTPConstants.CHUNKED, Boolean.TRUE);
        }else{
            options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
        }
    }

    public static void setTimeout(SforceServiceStub stub, int timeout) {
        Options options = stub._getServiceClient().getOptions();
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeout);
        options.setProperty(HTTPConstants.SO_TIMEOUT, timeout);
    }

    public static void setHttpProxy(SforceServiceStub stub) {
        Options options = stub._getServiceClient().getOptions();
        String httpsHost = System.getProperty("https.proxyHost");
        String httpsPort = System.getProperty("https.proxyPort");
        String httpsUser = System.getProperty("https.proxyUser");
        String httpsPwd = System.getProperty("https.proxyPassword");
        if (httpsHost != null) {
            ProxyProperties proxyProperties = new ProxyProperties();
            proxyProperties.setProxyName(httpsHost);
            if (httpsPort != null) {
                proxyProperties.setProxyPort(Integer.parseInt(httpsPort));
            }
            if (httpsUser != null && !"".equals(httpsUser)) {
                proxyProperties.setUserName(httpsUser);
            }
            if (httpsPwd != null && !"".equals(httpsPwd)) {
                proxyProperties.setPassWord(httpsPwd);
            }
            options.setProperty(HTTPConstants.PROXY, proxyProperties);
        } else {
            String host = System.getProperty("http.proxyHost");
            String port = System.getProperty("http.proxyPort");
            String user = System.getProperty("http.proxyUser");
            String pwd = System.getProperty("http.proxyPassword");
            if (host != null) {
                ProxyProperties proxyProperties = new ProxyProperties();
                proxyProperties.setProxyName(host);
                if (port != null) {
                    proxyProperties.setProxyPort(Integer.parseInt(port));
                }
                if (user != null && !"".equals(user)) {
                    proxyProperties.setUserName(user);
                }
                if (pwd != null && !"".equals(pwd)) {
                    proxyProperties.setPassWord(pwd);
                }
                options.setProperty(HTTPConstants.PROXY, proxyProperties);
            } else {
                String socksHost = System.getProperty("socksProxyHost");
                String socksPort = System.getProperty("socksProxyPort");
                String socksUser = System.getProperty("java.net.socks.username");
                String socksPwd = System.getProperty("java.net.socks.password");
                if (socksHost != null) {
                    ProxyProperties proxyProperties = new ProxyProperties();
                    proxyProperties.setProxyName(socksHost);
                    if (socksPort != null) {
                        proxyProperties.setProxyPort(Integer.parseInt(socksPort));
                    }
                    if (socksUser != null && !"".equals(socksUser)) {
                        proxyProperties.setUserName(socksUser);
                    }
                    if (socksPwd != null && !"".equals(socksPwd)) {
                        proxyProperties.setPassWord(socksPwd);
                    }
                    options.setProperty(HTTPConstants.PROXY, proxyProperties);
                }
            }
        }
        // options.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_PROTOCOL_VERSION,
        // HTTPConstants.HEADER_PROTOCOL_10);
    }
}
