package org.talend.salesforce.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

/**
 * created by bchen on Aug 26, 2013 Detailled comment
 * 
 */
public class OAuthClient {

    private static final String OAUTH2_TOKEN = "token";

    private static final String OAUTH2_AUTHORIZE = "authorize";

    private static final String UTF8 = "UTF-8";

    private String clientID;

    // don't need for user-agent flow
    private String clientSecret;

    private String callbackHost = "localhost";

    private int callbackPort;

    private String baseOAuthURL;

    private HttpsService service;

    public static String getBulkEndpoint(Token token, String version) {
        return token.getInstance_url() + "/services/async/" + version;
    }

    public static String getSOAPEndpoint(Token token, String version) throws MalformedURLException, IOException, ParseException {
        URLConnection idConn = new URL(token.getId()).openConnection();
        idConn.setRequestProperty("Authorization", "Bearer " + token.getAccess_token());
        String endpointURL = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(idConn.getInputStream()));
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(reader);
            JSONObject urls = (JSONObject) json.get("urls");
            endpointURL = urls.get("partner").toString().replace("{version}", version);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {

                }
            }
        }
        return endpointURL;
    }

    public Token refreshToken(String refreshToken) throws Exception {
        Token newToken = new Token();
        newToken.setRefresh_token(refreshToken);

        // get token using refresh token
        String token_url = baseOAuthURL.endsWith("/") ? baseOAuthURL + OAUTH2_TOKEN : baseOAuthURL + "/" + OAUTH2_TOKEN;
        URLConnection conn = new URL(token_url).openConnection();
        conn.setDoOutput(true);// post
        String query = String.format("grant_type=%s&client_id=%s&client_secret=%s&refresh_token=%s&format=%s", "refresh_token",
                clientID, clientSecret, refreshToken, "json");
        OutputStream output = null;
        try {
            output = conn.getOutputStream();
            output.write(query.getBytes(UTF8));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignore) {
                }
            }
        }
        InputStream input = conn.getInputStream();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(input));
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(reader);
            if (json.get("error") == null) {
                if (json.get("access_token") != null) {
                    newToken.setAccess_token(json.get("access_token").toString());
                }
                if (json.get("instance_url") != null) {
                    newToken.setInstance_url(json.get("instance_url").toString());
                }
                if (json.get("id") != null) {
                    newToken.setId(json.get("id").toString());
                }
                if (json.get("issued_at") != null) {
                    newToken.setIssued_at(Long.parseLong(json.get("issued_at").toString()));
                }
                if (json.get("signature") != null) {
                    newToken.setSignature(json.get("signature").toString());
                }
            } else {
                throw new Exception(json.toString());
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {

                }
            }
        }

        return newToken;
    }

    public String getUrl() throws Exception {
        URL callback_url = new URL("https", callbackHost, callbackPort, "");
        String oauth2_authorize_url = baseOAuthURL.endsWith("/") ? baseOAuthURL + OAUTH2_AUTHORIZE : baseOAuthURL + "/"
                + OAUTH2_AUTHORIZE;
        String authorize_url = String.format("%s?response_type=%s&client_id=%s&redirect_uri=%s", oauth2_authorize_url,// &scope=%s
                "code", clientID, URLEncoder.encode(callback_url.toString(), UTF8));// , "full%20refresh_token"
        return authorize_url;
    }

    public void startServer() throws Exception {
        service = new HttpsService(callbackPort);
    }

    public HttpsService getServer() {
        return service;
    }

    public void stopServer() throws Exception {
        if (service != null) {
            service.stop();
        }
    }

    public Token getTokenForWizard(String code) throws Exception {
        Token token = new Token();

        URL callback_url = new URL("https", callbackHost, callbackPort, "");

        String oauth2_authorize_url = baseOAuthURL.endsWith("/") ? baseOAuthURL + OAUTH2_AUTHORIZE : baseOAuthURL + "/"
                + OAUTH2_AUTHORIZE;
        String authorize_url = String.format("%s?response_type=%s&client_id=%s&redirect_uri=%s", oauth2_authorize_url,// &scope=%s
                "code", clientID, URLEncoder.encode(callback_url.toString(), UTF8));// , "full%20refresh_token"

        String token_url = baseOAuthURL.endsWith("/") ? baseOAuthURL + OAUTH2_TOKEN : baseOAuthURL + "/" + OAUTH2_TOKEN;
        URLConnection conn = new URL(token_url).openConnection();
        conn.setDoOutput(true);// post
        String query = String.format("grant_type=%s&client_id=%s&client_secret=%s&redirect_uri=%s&code=%s", "authorization_code",
                clientID, clientSecret, URLEncoder.encode(callback_url.toString(), UTF8), code);
        OutputStream output = null;
        try {
            output = conn.getOutputStream();
            output.write(query.getBytes(UTF8));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignore) {
                }
            }
        }
        InputStream input = conn.getInputStream();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(input));
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(reader);
            if (json.get("access_token") != null) {
                token.setAccess_token(json.get("access_token").toString());
            }
            if (json.get("refresh_token") != null) {
                token.setRefresh_token(json.get("refresh_token").toString());
            }
            if (json.get("instance_url") != null) {
                token.setInstance_url(json.get("instance_url").toString());
            }
            if (json.get("id") != null) {
                token.setId(json.get("id").toString());
            }
            if (json.get("issued_at") != null) {
                token.setIssued_at(Long.parseLong(json.get("issued_at").toString()));
            }
            if (json.get("signature") != null) {
                token.setSignature(json.get("signature").toString());
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {

                }
            }
        }
        return token;
    }

    public Token getToken() throws Exception {
        Token token = new Token();

        URL callback_url = new URL("https", callbackHost, callbackPort, "");

        String oauth2_authorize_url = baseOAuthURL.endsWith("/") ? baseOAuthURL + OAUTH2_AUTHORIZE : baseOAuthURL + "/"
                + OAUTH2_AUTHORIZE;
        String authorize_url = String.format("%s?response_type=%s&client_id=%s&redirect_uri=%s", oauth2_authorize_url,// &scope=%s
                "code", clientID, URLEncoder.encode(callback_url.toString(), UTF8));// , "full%20refresh_token"
        System.out.println("Paste this URL into a web browser to authorize Salesforce Access:");
        System.out.println(authorize_url);

        // start a service to get Authorization code
        HttpsService service = new HttpsService(callbackPort);
        String code = null;
        while (service.getCode() == null) {
            Thread.sleep(2 * 1000);
        }
        code = service.getCode();
        // stop service
        service.stop();

        // get token using Authorization code
        String token_url = baseOAuthURL.endsWith("/") ? baseOAuthURL + OAUTH2_TOKEN : baseOAuthURL + "/" + OAUTH2_TOKEN;
        URLConnection conn = new URL(token_url).openConnection();
        conn.setDoOutput(true);// post
        String query = String.format("grant_type=%s&client_id=%s&client_secret=%s&redirect_uri=%s&code=%s", "authorization_code",
                clientID, clientSecret, URLEncoder.encode(callback_url.toString(), UTF8), code);
        OutputStream output = null;
        try {
            output = conn.getOutputStream();
            output.write(query.getBytes(UTF8));
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignore) {
                }
            }
        }
        InputStream input = conn.getInputStream();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(input));
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(reader);
            if (json.get("access_token") != null) {
                token.setAccess_token(json.get("access_token").toString());
            }
            if (json.get("refresh_token") != null) {
                token.setRefresh_token(json.get("refresh_token").toString());
            }
            if (json.get("instance_url") != null) {
                token.setInstance_url(json.get("instance_url").toString());
            }
            if (json.get("id") != null) {
                token.setId(json.get("id").toString());
            }
            if (json.get("issued_at") != null) {
                token.setIssued_at(Long.parseLong(json.get("issued_at").toString()));
            }
            if (json.get("signature") != null) {
                token.setSignature(json.get("signature").toString());
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {

                }
            }
        }
        return token;

    }

    /**
     * Getter for clientID.
     * 
     * @return the clientID
     */
    public String getClientID() {
        return this.clientID;
    }

    /**
     * Sets the clientID.
     * 
     * @param clientID the clientID to set
     */
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /**
     * Getter for clientSecret.
     * 
     * @return the clientSecret
     */
    public String getClientSecret() {
        return this.clientSecret;
    }

    /**
     * Sets the clientSecret.
     * 
     * @param clientSecret the clientSecret to set
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Getter for callbackHost.
     * 
     * @return the callbackHost
     */
    public String getCallbackHost() {
        return this.callbackHost;
    }

    /**
     * Sets the callbackHost.
     * 
     * @param callbackHost the callbackHost to set
     */
    public void setCallbackHost(String callbackHost) {
        this.callbackHost = callbackHost;
    }

    /**
     * Getter for callbackPort.
     * 
     * @return the callbackPort
     */
    public int getCallbackPort() {
        return this.callbackPort;
    }

    /**
     * Sets the callbackPort.
     * 
     * @param callbackPort the callbackPort to set
     */
    public void setCallbackPort(int callbackPort) {
        this.callbackPort = callbackPort;
    }

    /**
     * Getter for baseOAuthURL.
     * 
     * @return the baseOAuthURL
     */
    public String getBaseOAuthURL() {
        return baseOAuthURL;
    }

    /**
     * Sets the baseOAuthURL.
     * 
     * @param baseOAuthURL the baseOAuthURL to set
     */
    public void setBaseOAuthURL(String baseOAuthURL) {
        this.baseOAuthURL = baseOAuthURL;
    }
}
