// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.talend.salesforce.oauth.OAuthClient;
import org.talend.salesforce.oauth.Token;

import com.sforce.async.BulkConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * created by bchen on Jul 11, 2014 Detailled comment
 * 
 */
public class SforceOAuthBulkConnection extends SforceBulkConnection {

    private static final String REFRESHTOKEN_KEY = "refreshtoken";

    private final String login_endpoint;

    private final String oauth_clientID;

    private final String oauth_clientSecret;

    private String tokenFilePath;

    private final String apiVersion;

    private final String callbackHost;

    private final int callbackPort;

    private boolean needCompression;

    private boolean needTraceMessage;

    private SforceOAuthBulkConnection() throws Exception {
        throw new Exception("should use builder to init"); //$NON-NLS-1$
    }

    private SforceOAuthBulkConnection(Builder builder) throws Exception {
        this.login_endpoint = builder.login_endpoint;
        this.oauth_clientID = builder.oauth_clientID;
        this.oauth_clientSecret = builder.oauth_clientSecret;
        this.apiVersion = builder.apiVersion;
        this.tokenFilePath = builder.tokenFilePath;
        this.callbackHost = builder.callbackHost;
        this.callbackPort = builder.callbackPort;
        this.needCompression = builder.needCompression;
        this.needTraceMessage = builder.needTraceMessage;
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
        config = new ConnectorConfig();
        // This should only be false when doing debugging.
        config.setCompression(needCompression);
        // Set this to true to see HTTP requests and responses on stdout
        config.setTraceMessage(needTraceMessage);
        renewSession();
        connection = new BulkConnection(config);
    }

    @Override
    protected void renewSession() throws ConnectionException {
        Token token;
        try {
            token = loginWithOAuth();
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
        String session_id = token.getAccess_token();
        String endpoint = OAuthClient.getBulkEndpoint(token, apiVersion);
        config.setSessionId(session_id);
        config.setRestEndpoint(endpoint);
    }

    public static class Builder {

        private final String login_endpoint;

        private final String oauth_clientID;

        private final String oauth_clientSecret;

        private final String apiVersion;

        private String tokenFilePath = null;

        private final String callbackHost;

        private final int callbackPort;

        private boolean needCompression = true;

        private boolean needTraceMessage = false;

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

        public Builder needTraceMessage(boolean needTraceMessage) {
            this.needTraceMessage = needTraceMessage;
            return this;
        }

        public SforceOAuthBulkConnection build() throws Exception {
            return new SforceOAuthBulkConnection(this);
        }
    }
}
