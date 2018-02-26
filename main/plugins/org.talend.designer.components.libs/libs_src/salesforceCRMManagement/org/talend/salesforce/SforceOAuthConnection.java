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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.talend.salesforce.oauth.OAuthClient;
import org.talend.salesforce.oauth.Token;

import com.salesforce.soap.partner.CallOptions;
import com.salesforce.soap.partner.SessionHeader;
import com.salesforce.soap.partner.SforceServiceStub;

/**
 * created by bchen on Jul 10, 2014 Detailled comment
 * 
 */
public class SforceOAuthConnection extends SforceConnection {

    private static final String REFRESHTOKEN_KEY = "refreshtoken";

    private final String login_endpoint;

    private final String oauth_clientID;

    private final String oauth_clientSecret;

    private String tokenFilePath;

    private final String apiVersion;

    private final String callbackHost;

    private final int callbackPort;

    private boolean needCompression;
    
    private boolean useHttpChunked;

    private int timeout;

    private String clientID;
    
    private String serviceEndPoint;
    
    public String getServiceEndPoint(){
    	return this.serviceEndPoint;
    }

    private SforceOAuthConnection() throws Exception {
        throw new Exception("should use builder to init"); //$NON-NLS-1$
    }

    private SforceOAuthConnection(Builder builder) throws Exception {
        this.login_endpoint = builder.login_endpoint;
        this.oauth_clientID = builder.oauth_clientID;
        this.oauth_clientSecret = builder.oauth_clientSecret;
        this.apiVersion = builder.apiVersion;
        this.tokenFilePath = builder.tokenFilePath;
        this.callbackHost = builder.callbackHost;
        this.callbackPort = builder.callbackPort;
        this.needCompression = builder.needCompression;
        this.useHttpChunked = builder.useHttpChunked;
        this.timeout = builder.timeout;
        this.clientID = builder.clientID;
        init();
    }

    private Token loginWithOAuth() throws Exception {
        String session_id = null;

        OAuthClient oauthClient = new OAuthClient();
        oauthClient.setBaseOAuthURL(login_endpoint);
        oauthClient.setClientID(oauth_clientID);
        oauthClient.setClientSecret(oauth_clientSecret);
        Token token = null;
        String refreshToken = null;
        // 1. if tokenFile exist, try refresh token
        if (tokenFilePath != null) {
            Properties prop = new Properties();
            FileInputStream inputStream = new FileInputStream(tokenFilePath);
            prop.load(inputStream);
            inputStream.close();
            String storedRefreshToken = (String) prop.get(REFRESHTOKEN_KEY);
            if (storedRefreshToken != null) {
                token = oauthClient.refreshToken(storedRefreshToken);
                session_id = token.getAccess_token();
                refreshToken = token.getRefresh_token();
            }
        }
        // 2. try to auth if session_id can't be retrieved
        if (session_id == null) {
            oauthClient.setCallbackHost(callbackHost);
            oauthClient.setCallbackPort(callbackPort);
            token = oauthClient.getToken();
            refreshToken = token.getRefresh_token();
        }
        // 3.if refresh token & tokenFile exist, store
        if (refreshToken != null && tokenFilePath != null) {
            Properties prop = new Properties();
            prop.setProperty(REFRESHTOKEN_KEY, refreshToken);
            FileOutputStream outputStream = new FileOutputStream(tokenFilePath);
            prop.store(outputStream, null);
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return token;
    }

    private void init() throws Exception {
        if (clientID != null) {
            co = new CallOptions();
            co.setClient(clientID);
        }
        stub = new SforceServiceStub();
        SforceManagementUtil.needCompression(stub, needCompression);
        SforceManagementUtil.setTimeout(stub, timeout);
        SforceManagementUtil.useHttpChunked(stub, useHttpChunked);
        // SforceManagementUtil.setHttpProxy(stub);//don't support proxy for OAuth
        sh = new SessionHeader();
        renewSession();
    }

    @Override
    protected void renewSession() throws Exception {
        Token token = loginWithOAuth();
        String session_id = token.getAccess_token();
        String endpoint = OAuthClient.getSOAPEndpoint(token, apiVersion);
        this.serviceEndPoint = endpoint;
        SforceManagementUtil.setEndpoint(stub, endpoint);
        sh.setSessionId(session_id);
    }

    public static class Builder {

        private final String login_endpoint;

        private final String oauth_clientID;

        private final String oauth_clientSecret;

        private final String apiVersion;

        private String tokenFilePath = null;

        private final String callbackHost;

        private final int callbackPort;

        private boolean needCompression = false;
        
        private boolean useHttpChunked;

        private int timeout = 60000;

        private String clientID = null;

        public Builder(String login_endpoint, String oauth_clientID, String oauth_clientSecret, String apiVersion,
                String callbackHost, int callbackPort) {
            this.login_endpoint = login_endpoint;
            this.oauth_clientID = oauth_clientID;
            this.oauth_clientSecret = oauth_clientSecret;
            this.apiVersion = apiVersion;
            this.callbackHost = callbackHost;
            this.callbackPort = callbackPort;
        }

        public Builder setTokenFilePath(String tokenFilePath) {
            this.tokenFilePath = tokenFilePath;
            return this;
        }

        public Builder needCompression(boolean needCompression) {
            this.needCompression = needCompression;
            return this;
        }
        
        public Builder useHttpChunked(boolean useHttpChunked) {
            this.useHttpChunked = useHttpChunked;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setTimeout(String timeout) {
            this.timeout = Integer.valueOf(timeout);
            return this;
        }

        public Builder setClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public SforceOAuthConnection build() throws Exception {
            return new SforceOAuthConnection(this);
        }
    }

}
