/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.helper.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.xml.WSDLLocator;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.xml.sax.InputSource;

/**
 * 
 * @author rlamarche
 */
public class WSDLLocatorImpl implements WSDLLocator {

    private static final String HTTP_HEADER_COOKIE = "Cookie";

    private String wsdlUri;

    private String latestImportUri;

    private ServiceHelperConfiguration configuration;

    private HttpClient httpClient;

    private Set<InputStream> inputStreams;

    public WSDLLocatorImpl(ServiceHelperConfiguration configuration, String wsdlUri) {
        this.configuration = configuration;
        this.httpClient = createHttpClient();
        this.wsdlUri = wsdlUri;
        inputStreams = new HashSet<InputStream>();
    }

    public InputSource getBaseInputSource() {
        HttpRequestBase get = createGetMethod(wsdlUri);
        try {
            HttpResponse response = httpClient.execute(get);
            InputStream is = response.getEntity().getContent();
            inputStreams.add(is);
            return new InputSource(is);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        try {
            URL url = getURL(parentLocation, importLocation);
            latestImportUri = url.toExternalForm();
            HttpRequestBase get = createGetMethod(latestImportUri);
            HttpResponse response = httpClient.execute(get);
            InputStream is = response.getEntity().getContent();
            inputStreams.add(is);
            return new InputSource(is);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static URL getURL(String parentLocation, String wsdlLocation) throws MalformedURLException {
        URL contextURL = (parentLocation != null) ? getURL(null, parentLocation) : null;
        try {
            return new URL(contextURL, wsdlLocation);
        } catch (MalformedURLException e) {
            File tempFile = new File(wsdlLocation);
            if (contextURL == null || (contextURL != null && tempFile.isAbsolute())) {
                return tempFile.toURI().toURL();
            }
            // this line is reached if contextURL != null, wsdlLocation is a relative path,
            // and a MalformedURLException has been thrown - so re-throw the Exception.
            throw e;
        }
    }

    public String getBaseURI() {
        return wsdlUri;
    }

    public String getLatestImportURI() {
        return latestImportUri;
    }

    public void close() {
        for (InputStream is : inputStreams) {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(WSDLLocatorImpl.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        inputStreams.clear();
    }

    private HttpRequestBase createGetMethod(String uri) {
        HttpGet get = new HttpGet(uri);
        if (configuration.getCookie() != null) {
            get.setHeader(HTTP_HEADER_COOKIE, configuration.getCookie());
        }

        return get;
    }

    private HttpClient createHttpClient() {
        HttpClientBuilder builder = HttpClients.custom();
        CredentialsProvider credentialsProvider = null;
        if (configuration.getProxyServer() != null) {
            builder.setProxy(new HttpHost(configuration.getProxyServer(), configuration.getProxyPort()));
        }

        if (configuration.getUsername() != null) {
            if (credentialsProvider == null) {
                credentialsProvider = new BasicCredentialsProvider();
            }
            credentialsProvider
                    .setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword()));
        }

        if (configuration.getProxyUsername() != null) {
            if (credentialsProvider == null) {
                credentialsProvider = new BasicCredentialsProvider();
            }
            credentialsProvider
                    .setCredentials(new AuthScope(configuration.getProxyServer(), configuration.getProxyPort()),
                            new UsernamePasswordCredentials(configuration.getProxyUsername(),
                                    configuration.getProxyPassword()));
            builder.setProxy(new HttpHost(configuration.getProxyServer(), configuration.getProxyPort()));
        }
        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        return builder.build();
    }
}
