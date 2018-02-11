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
package org.talend.salesforceBulk;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;

import com.sforce.async.BulkConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * created by bchen on Jul 11, 2014 Detailled comment
 * 
 */
public class SforceBasicBulkConnection extends SforceBulkConnection {

    private final String login_endpoint;

    private final String username;

    private final String password;

    private final String apiVersion;

    private boolean needCompression;

    private boolean needTraceMessage;

    private boolean useProxy;

    private String proxyHost;

    private int proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    private SforceBasicBulkConnection() throws Exception {
        throw new Exception("should use builder to init"); //$NON-NLS-1$
    }

    private SforceBasicBulkConnection(Builder builder) throws Exception {
        this.login_endpoint = builder.login_endpoint;
        this.username = builder.username;
        this.password = builder.password;
        this.apiVersion = builder.apiVersion;
        this.needCompression = builder.needCompression;
        this.needTraceMessage = builder.needTraceMessage;
        init();
    }

    @Override
    protected void renewSession() throws ConnectionException {
        ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setAuthEndpoint(login_endpoint);
        partnerConfig.setUsername(username);
        partnerConfig.setPassword(password);
        setProxyToConnection(partnerConfig);
        // Creating the connection automatically handles login and stores
        // the session in partnerConfig
        new PartnerConnection(partnerConfig);
        // The endpoint for the Bulk API service is the same as for the normal
        // SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
        String soapEndpoint = partnerConfig.getServiceEndpoint();
        String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
        // When PartnerConnection is instantiated, a login is implicitly
        // executed and, if successful,
        // a valid session is stored in the ConnectorConfig instance.
        // Use this key to initialize a BulkConnection:
        config.setSessionId(partnerConfig.getSessionId());
        config.setRestEndpoint(restEndpoint);
    }

    private void init() throws Exception {
        config = new ConnectorConfig();
        setProxyToConnection(config);
        // This should only be false when doing debugging.
        config.setCompression(needCompression);
        // Set this to true to see HTTP requests and responses on stdout
        config.setTraceMessage(needTraceMessage);
        renewSession();
        connection = new BulkConnection(config);
    }

    private void setProxyToConnection(ConnectorConfig conn) {
        Proxy socketProxy = null;
        if (!useProxy) {
            proxyHost = System.getProperty("https.proxyHost");
            if (proxyHost != null && System.getProperty("https.proxyPort") != null) {
                proxyPort = Integer.parseInt(System.getProperty("https.proxyPort"));
                proxyUsername = System.getProperty("https.proxyUser");
                proxyPassword = System.getProperty("https.proxyPassword");
                useProxy = true;
            } else {
                proxyHost = System.getProperty("http.proxyHost");
                if (proxyHost != null && System.getProperty("http.proxyPort") != null) {
                    proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
                    proxyUsername = System.getProperty("http.proxyUser");
                    proxyPassword = System.getProperty("http.proxyPassword");
                    useProxy = true;
                } else {
                    proxyHost = System.getProperty("socksProxyHost");
                    if (proxyHost != null && System.getProperty("socksProxyPort") != null) {
                        proxyPort = Integer.parseInt(System.getProperty("socksProxyPort"));
                        proxyUsername = System.getProperty("java.net.socks.username");
                        proxyPassword = System.getProperty("java.net.socks.password");
                        useProxy = true;

                        SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
                        socketProxy = new Proxy(Proxy.Type.SOCKS, addr);
                    }
                }
            }
        }
        if (useProxy) {
            if (socketProxy != null) {
                conn.setProxy(socketProxy);
            } else {
                conn.setProxy(proxyHost, proxyPort);
            }
            if (proxyUsername != null && !"".equals(proxyUsername)) {
                conn.setProxyUsername(proxyUsername);
                if (proxyPassword != null && !"".equals(proxyPassword)) {
                    conn.setProxyPassword(proxyPassword);

                    Authenticator.setDefault(new Authenticator() {

                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            if (getRequestorType() == Authenticator.RequestorType.PROXY) {
                                return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                            } else {
                                return super.getPasswordAuthentication();
                            }
                        }
                    });

                }
            }
        }
    }

    public static class Builder {

        private final String login_endpoint;

        private final String username;

        private final String password;

        private final String apiVersion;

        private boolean needCompression = true;

        private boolean needTraceMessage = false;

        private boolean useProxy = false;

        private String proxyHost;

        private int proxyPort;

        private String proxyUsername;

        private String proxyPassword;

        public Builder(String login_endpoint, String username, String password, String apiVersion) {
            this.login_endpoint = login_endpoint;
            this.username = username;
            this.password = password;
            this.apiVersion = apiVersion;
        }

        public Builder needCompression(boolean needCompression) {
            this.needCompression = needCompression;
            return this;
        }

        public Builder needTraceMessage(boolean needTraceMessage) {
            this.needTraceMessage = needTraceMessage;
            return this;
        }

        public Builder setProxy(boolean useProxy, String host, int port, String username, String password) {
            this.proxyHost = host;
            this.proxyPort = port;
            this.proxyUsername = username;
            this.proxyPassword = password;
            this.useProxy = useProxy;
            return this;
        }

        public SforceBasicBulkConnection build() throws Exception {
            return new SforceBasicBulkConnection(this);
        }
    }

}
