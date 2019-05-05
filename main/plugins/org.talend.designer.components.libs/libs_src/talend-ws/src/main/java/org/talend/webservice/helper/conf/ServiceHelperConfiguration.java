/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.helper.conf;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.wsdl.xml.WSDLLocator;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;

/**
 * 
 * @author rlamarche
 */
public class ServiceHelperConfiguration {

    private String proxyServer;

    private int proxyPort;

    private Long connectionTimeout;

    private Long receiveTimeout;

    private String cookie;

    private String username;

    private String password;

    private String proxyUsername;

    private String proxyPassword;

    private String trustStoreFile;

    private String trustStorePwd;

    private String trustStoreType;

    private String keyStoreFile;

    private String keyStorePwd;

    private String keyStoreType;

    private boolean allowChunking = true;

    public boolean isAllowChunking() {
        return allowChunking;
    }

    public void setAllowChunking(boolean allowChunking) {
        this.allowChunking = allowChunking;
    }

    public WSDLLocator createWSDLLocator(String wsdlUri) {
        return new WSDLLocatorImpl(this, wsdlUri);
    }

    private void updateClientPolicy(HTTPClientPolicy hTTPClientPolicy) {
        if (proxyServer != null) {
            hTTPClientPolicy.setProxyServer(proxyServer);
            hTTPClientPolicy.setProxyServerPort(proxyPort);
            hTTPClientPolicy.setProxyServerType(ProxyServerType.HTTP);
        } else {
            hTTPClientPolicy.setProxyServer(null);
        }
        if (connectionTimeout != null) {
            hTTPClientPolicy.setConnectionTimeout(connectionTimeout);
        }
        if (receiveTimeout != null) {
            hTTPClientPolicy.setReceiveTimeout(receiveTimeout);
        }
        if (cookie != null) {
            hTTPClientPolicy.setCookie(cookie);
        } else {
            hTTPClientPolicy.setCookie(null);
        }
        hTTPClientPolicy.setAllowChunking(allowChunking);
    }

    public void configureHttpConduit(HTTPConduit httpConduit) {
        createAuthorizationPolicy(httpConduit);
        createProxyAuthorizationPolicy(httpConduit);
        httpConduit.setTlsClientParameters(createTLSClientParameters());
        updateClientPolicy(httpConduit.getClient());
    }

    private void createAuthorizationPolicy(HTTPConduit httpConduit) {
        if (username != null) {
            AuthorizationPolicy authorizationPolicy = httpConduit.getAuthorization();
            // authorizationPolicy.setAuthorizationType("Basic");
            authorizationPolicy.setUserName(username);
            authorizationPolicy.setPassword(password);
        }
    }

    private void createProxyAuthorizationPolicy(HTTPConduit httpConduit) {
        if (proxyUsername != null) {
            ProxyAuthorizationPolicy authorizationPolicy = httpConduit.getProxyAuthorization();
            // authorizationPolicy.setAuthorizationType("Basic");
            authorizationPolicy.setUserName(proxyUsername);
            authorizationPolicy.setPassword(proxyPassword);

        }
    }

    private TLSClientParameters createTLSClientParameters() {
        if (trustStoreFile != null || keyStoreFile != null) {
            TLSClientParameters tlsCP = new TLSClientParameters();
            if (trustStoreFile != null) {
                try {
                    KeyStore trustStore = KeyStore.getInstance(trustStoreType);
                    trustStore.load(new FileInputStream(trustStoreFile), trustStorePwd.toCharArray());
                    String alg = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
                    fac.init(trustStore);
                    TrustManager[] myTrustStoreKeyManagers = fac.getTrustManagers();
                    tlsCP.setTrustManagers(myTrustStoreKeyManagers);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (keyStoreFile != null) {
                try {
                    KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                    keyStore.load(new FileInputStream(keyStoreFile), keyStorePwd.toCharArray());
                    String alg = KeyManagerFactory.getDefaultAlgorithm();
                    char[] keyPass = keyStorePwd != null ? keyStorePwd.toCharArray() : null;
                    KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
                    fac.init(keyStore, keyPass);
                    KeyManager[] myKeyManagers = fac.getKeyManagers();
                    tlsCP.setKeyManagers(myKeyManagers);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            tlsCP.setDisableCNCheck(true);
            return tlsCP;
        } else {
            return null;
        }
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public Long getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(Long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getTrustStorePwd() {
        return trustStorePwd;
    }

    public void setTrustStorePwd(String trustStorePwd) {
        this.trustStorePwd = trustStorePwd;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePwd() {
        return keyStorePwd;
    }

    public void setKeyStorePwd(String keyStorePwd) {
        this.keyStorePwd = keyStorePwd;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

}
