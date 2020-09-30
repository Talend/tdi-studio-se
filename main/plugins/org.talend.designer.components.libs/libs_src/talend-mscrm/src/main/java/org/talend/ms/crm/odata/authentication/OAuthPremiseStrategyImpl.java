package org.talend.ms.crm.odata.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.olingo.client.api.communication.request.ODataRequest;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.talend.ms.crm.odata.ClientConfiguration;
import org.talend.ms.crm.odata.authentication.httpclienthelper.HttpClient;
import org.talend.ms.crm.odata.authentication.httpclienthelper.HttpResponse;
import org.talend.ms.crm.odata.authentication.httpclienthelper.RequestHttpContext;
import org.talend.ms.crm.odata.authentication.httpclienthelper.Token;
import org.talend.ms.crm.odata.httpclientfactory.IHttpclientFactoryObservable;
import org.talend.ms.crm.odata.httpclientfactory.OAuthHttpClientFactory;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class OAuthPremiseStrategyImpl implements IAuthStrategy {

    private final static int MAX_REDIRECT_AUTH_CODE = 3;

    private final ClientConfiguration conf;

    private Token token;
    private OAuthHttpClientFactory httpClientFactory;


    OAuthPremiseStrategyImpl(ClientConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public void init() throws AuthenticationException {
        oauthFlow();
    }

    @Override
    public IHttpclientFactoryObservable getHttpClientFactory() throws AuthenticationException {
        oauthFlow();

        if (httpClientFactory == null) {
            httpClientFactory = new OAuthHttpClientFactory(this.conf);
        }

        return httpClientFactory;
    }

    @Override
    public void refreshAuth() throws AuthenticationException {
        oauthFlow();
    }

    @Override
    public void configureRequest(ODataRequest request) {
        request.addCustomHeader(HttpHeader.AUTHORIZATION, "Bearer " + token.getAccess_token());
    }

    @Override
    public void configureRequest(HttpRequestBase request) {
        request.addHeader(HttpHeader.AUTHORIZATION, "Bearer " + token.getAccess_token());
    }

    private void oauthFlow() throws AuthenticationException {
        int retry = 0;

        while (true) {
            retry++;
            try {

                final Optional<String> code = retrieveAuthorizationCode();
                if (code.isPresent()) {
                    final String sCode = code.get();
                    final Optional<Token> token = retrieveToken(sCode);
                    if (token.isPresent()) {
                        this.token = token.get();
                        break;
                    } else {
                        throw new IllegalArgumentException("Can't retrieve oauth token, but no exception has been raised.");
                    }
                } else {
                    throw new IllegalArgumentException("Can't retrieve authorization code, but no exception has been raised.");
                }

            } catch (IOException | InterruptedException | IllegalArgumentException e) {
                if (retry < conf.getMaxRetryTimes()) {
                    try {
                        Thread.sleep(conf.getIntervalTime());
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                    continue;
                }

                throw new AuthenticationException("Can't retrieve ms crm oauth token after '" + retry + "' retries : " + e.getMessage());
            }
        }
    }

    private Token json2Token(String json) throws JsonIOException, JsonSyntaxException {
        Gson gson = new Gson();
        JsonReader jsr = new JsonReader(new StringReader(json));
        return gson.fromJson(jsr, Token.class);
    }

    private String getResource() throws MalformedURLException {
        URL url = new URL(conf.getServiceAPI());
        final int port = url.getPort();
        StringBuilder sp = new StringBuilder();
        if (port > -1) {
            sp = sp.append(":").append(port);
        }
        return url.getProtocol() + "://" + url.getHost() + sp.toString();
    }

    /**
     * To compute toekn url, change last segment by token.
     * @return
     */
    private String computeTokenUrl(String url){
        while(url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String token = url.substring(0, url.lastIndexOf('/')) + "/token";
        return token;
    }

    private Optional<Token> retrieveToken(String code) throws IOException, InterruptedException, IllegalArgumentException {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("code", code);
        body.put("redirect_uri", conf.getRedirectURL());
        body.put("client_id", conf.getClientId());

        RequestHttpContext queryContext = new RequestHttpContext("POST",
                computeTokenUrl(conf.getAuthoryEndpoint()),
                Collections.emptyMap(),
                new HashMap<>(),
                true,
                conf.getTimeout() * 1000,
                conf.getTimeout() * 1000);

        queryContext.setBodyContent(body);

        HttpClient client = new HttpClient(queryContext);

        // Redirect are followed by the java http client
        final HttpResponse call = client.call(new AtomicInteger(-1), e -> {
            return true;
        });

        Token token = null;

        try {
            token = json2Token(call.getBody());
        }
        catch (JsonIOException | JsonSyntaxException e){
            throw new IllegalArgumentException("Can't parse retrieve ms crm oauth token : " + e.getMessage(), e);
        }
        return Optional.ofNullable(token);
    }

    private Optional<String> retrieveAuthorizationCode() throws IOException, InterruptedException {
        Map<String, String> params = new HashMap();
        params.put("resource", getResource());
        params.put("response_type", "code");
        params.put("state", "");
        params.put("client_id", conf.getClientId());
        params.put("scope", "");
        params.put("redirect_uri", conf.getRedirectURL());

        Map<String, String> form = new HashMap<>();
        form.put("UserName", conf.getUserName());
        form.put("Password", conf.getPassword());
        form.put("AuthMethod", "FormsAuthentication");

        RequestHttpContext queryContext = new RequestHttpContext("POST",
                conf.getAuthoryEndpoint(),
                params,
                new HashMap<>(),
                false,
                conf.getTimeout() * 1000,
                conf.getTimeout() * 1000);

        queryContext.setBodyContent(form);
        HttpClient client = new HttpClient(queryContext);

        // We stop to follow redirect url once we have the code
        final HttpResponse call = client.call(new AtomicInteger(MAX_REDIRECT_AUTH_CODE), e -> {
            return !e.extractCode().isPresent();
        });

        return call.extractCode();
    }
}
