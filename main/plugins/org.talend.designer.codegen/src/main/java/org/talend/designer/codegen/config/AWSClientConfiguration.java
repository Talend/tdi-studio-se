/*
 * Copyright 2010-2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.talend.designer.codegen.config;


import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;

/**
 * Client configuration options such as proxy settings, user agent string, max retry attempts, etc.
 *
 *  */
public class AWSClientConfiguration {

    /** The default timeout for creating new connections. */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;

    /** The default timeout for reading from a connected socket. */
    public static final int DEFAULT_SOCKET_TIMEOUT = 50 * 1000;

    /**
     * The default timeout for a request. This is disabled by default.
     */
    public static final int DEFAULT_REQUEST_TIMEOUT = 0;

    /**
     * The default timeout for a request. This is disabled by default.
     */
    public static final int DEFAULT_CLIENT_EXECUTION_TIMEOUT = 0;

    /**
     * The default on whether to disable {@code Socket} proxies.
     */
    public static final boolean DEFAULT_DISABLE_SOCKET_PROXY = false;

    /** The default max connection pool size. */
    public static final int DEFAULT_MAX_CONNECTIONS = 50;

    /**
     * The default on whether to utilize the USE_EXPECT_CONTINUE handshake for operations. Currently
     * only honored for PUT operations.
     */
    public static final boolean DEFAULT_USE_EXPECT_CONTINUE = true;

    /**
     * The default on whether to throttle retries.
     */
    public static final boolean DEFAULT_THROTTLE_RETRIES = true;

    /**
     * The maximum number of times that a retryable failed request (ex: a 5xx response from a
     * service) will be retried. Or -1 if the user has not explicitly set this value, in which case
     * the configured RetryPolicy will be used to control the retry count.
     */
    private int maxErrorRetry = -1;


    /** Optionally specifies the local address to bind to */
    private InetAddress localAddress;


    /**
     * The protocol to use when connecting to an HTTP proxy.
     * <p>
     * The default configuration is to use HTTP.
     */
    private String proxyProtocol = "https";
    
    private String protocol = "https";

    /** Optionally specifies the proxy host to connect through. */
    private String proxyHost = null;

    /** Optionally specifies the port on the proxy host to connect through. */
    private int proxyPort = -1;

    /** Optionally specifies the user name to use when connecting through a proxy. */
    private String proxyUsername = null;

    /** Optionally specifies the password to use when connecting through a proxy. */
    private String proxyPassword = null;

    /** Optional Windows domain name for configuring NTLM proxy support. */
    private String proxyDomain = null;

    /** Optional Windows workstation name for configuring NTLM proxy support. */
    private String proxyWorkstation = null;

    /** Optional specifies the hosts that should be accessed without going through the proxy. */
    private String nonProxyHosts = null;
    
    /** Optional specifies the user agent */
    private String userAgent = null;

    /** The maximum number of open HTTP connections. */
    private int maxConnections = DEFAULT_MAX_CONNECTIONS;

    /**
     * The amount of time to wait (in milliseconds) for data to be transferred over an established,
     * open connection before the connection is timed out. A value of 0 means infinity, and is not
     * recommended.
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    /**
     * The amount of time to wait (in milliseconds) when initially establishing a connection before
     * giving up and timing out. A value of 0 means infinity, and is not recommended.
     */
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    /**
     * The amount of time to wait (in milliseconds) for a request to complete before giving up and
     * timing out. A value of 0 means infinity. Consider setting this if a harder guarantee is
     * required on the maximum amount of time a request will take for non-streaming operations, and
     * are willing to spin up a background thread to enforce it.
     */
    private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;

    private int clientExecutionTimeout = DEFAULT_CLIENT_EXECUTION_TIMEOUT;

    private boolean throttleRetries = DEFAULT_THROTTLE_RETRIES;

    /**
     * Optional size hint (in bytes) for the low level TCP send buffer. This is an advanced option
     * for advanced users who want to tune low level TCP parameters to try and squeeze out more
     * performance.
     */
    private int socketSendBufferSizeHint = 0;

    /**
     * Optional size hint (in bytes) for the low level TCP receive buffer. This is an advanced
     * option for advanced users who want to tune low level TCP parameters to try and squeeze out
     * more performance.
     */
    private int socketReceiveBufferSizeHint = 0;


    /**
     * Configuration option to disable the host prefix injection.
     *
     * The hostPrefix template is specified in the service model and is used by the SDK to modify the endpoint
     * the request is sent to. Host prefix injection is enabled by default. This option can be set to disable the behavior.
     */
    private boolean disableHostPrefixInjection;

    private final AtomicReference<URLHolder> httpProxyHolder = new AtomicReference<URLHolder>();

    private final AtomicReference<URLHolder> httpsProxyHolder = new AtomicReference<URLHolder>();


     /**
     * Returns the protocol (HTTP or HTTPS) to use when connecting to Amazon Web Services.
     * <p>
     * The default configuration is to use HTTPS for all requests for increased security.
     *
     * @return The protocol to use when connecting to Amazon Web Services.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the maximum number of allowed open HTTP connections.
     *
     * @return The maximum number of allowed open HTTP connections.
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Sets the maximum number of allowed open HTTP connections.
     *
     * @param maxConnections
     *            The maximum number of allowed open HTTP connections.
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public void setMaxConnections(String maxConnections) {
    	if(maxConnections != null && maxConnections != "") {
    		this.maxConnections = normalizeNumber(maxConnections);	
    	}
    }

     /**
     * Returns the optional local address the client will bind to.
     *
     * @return The local address the client will bind to.
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Sets the optional local address the client will bind to.
     *
     * @param localAddress
     *            The local address the client will bind to.
     */
    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Returns the value for the given system property.
     */
    private String getSystemProperty(String property) {
        return System.getProperty(property);
    }

    /**
     * Returns the value for the given environment variable.
     */
    private String getEnvironmentVariable(String environmentVariable) {
        String value = StringUtils.trim(System.getenv(environmentVariable));
        return (value != null && value != "") ? value : null;
    }

    /**
     * Returns the value for the given environment variable if its set, otherwise returns
     * the lowercase version of variable.
     */
    private String getEnvironmentVariableCaseInsensitive(String environmentVariable) {
        String result = getEnvironmentVariable(environmentVariable);
        return result != null ? result : getEnvironmentVariable(environmentVariable.toLowerCase());
    }

    /**
     * @return The protocol to use for connecting to the proxy.
     */
    public String getProxyProtocol() {
        return proxyProtocol;
    }

    /**
     * Set the protocol to use for connecting to the proxy.
     *
     * @param proxyProtocol The protocol.
     */
    public void setProxyProtocol(String proxyProtocol) {
    	this.proxyProtocol = proxyProtocol == null ? "htto" : proxyProtocol;
    }
    
    
    public void setProtocol(String protocol) {
    	this.protocol = protocol == null ? "htto" : protocol;
    }

    /**
     * Returns the Java system property for proxy host depending on
     * protocol i.e. if protocol is https, returns
     * the value of the system property https.proxyHost, otherwise
     * returns value of http.proxyHost.
     */
    private String getProxyHostProperty() {
        return getProtocol() == "https"
                ? getSystemProperty("https.proxyHost")
                : getSystemProperty("http.proxyHost");
    }

    /**
     * Returns the environment variable for proxy host depending on
     * {@link #getProtocol()}: i.e. if protocol is https, returns
     * the host in the value of the environment variable HTTPS_PROXY/https_proxy,
     * otherwise, returns the host in the value of the environment
     * variable HTTP_PROXY/http_proxy.
     */
    private String getProxyHostEnvironment() {
        URL httpProxy = getHttpProxyEnvironmentVariable();
        if (httpProxy != null) {
            return httpProxy.getHost();
        }
        return null;
    }

    /**
     * Returns the optional proxy host the client will connect
     * through.  Returns either the proxyHost set on this object, or
     * if not provided, checks the value of the Java system property
     * for proxy host according to {@link #getProtocol()}: i.e. if
     * protocol is https, returns the value of the system property
     * https.proxyHost, otherwise returns value of http.proxyHost.
     * If neither are set, checks the value of the environment variable
     * according to {@link #getProtocol()}: i.e. if protocol is https,
     * returns the host in the value of the HTTPS_PROXY/https_proxy
     * environment variable, otherwise returns the host in the value
     * of the HTTP_PROXY/http_proxy environment variable.
     *
     * @return The proxy host the client will connect through.
     */
    public String getProxyHost() {
        if (proxyHost != null) {
            return proxyHost;
        } else if (getProxyHostProperty() != null) {
            return getProxyHostProperty();
        } else {
            return getProxyHostEnvironment();
        }
    }

    /**
     * Sets the optional proxy host the client will connect through.
     *
     * @param proxyHost
     *            The proxy host the client will connect through.
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }


    /**
     * Returns the Java system property for proxy port depending on
     * {@link #getProtocol()}: i.e. if protocol is https, returns
     * the value of the system property https.proxyPort, otherwise
     * returns value of http.proxyPort.  Defaults to {@link this.proxyPort}
     * if the system property is not set with a valid port number.
     */
    private int getProxyPortProperty() {
        try {
            return getProtocol() == "https"
                    ? Integer.parseInt(getSystemProperty("https.proxyPort"))
                    : Integer.parseInt(getSystemProperty("http.proxyPort"));
        } catch (NumberFormatException e) {
            return proxyPort;
        }
    }

    /**
     * Returns the environment variable for proxy port depending on
     * {@link #getProtocol()}: i.e. if protocol is https, returns
     * the port in the value of the environment variable HTTPS_PROXY/https_proxy,
     * otherwise, returns the port in the value of the environment
     * variable HTTP_PROXY/http_proxy.
     */
    private int getProxyPortEnvironment() {
        URL httpProxy = getHttpProxyEnvironmentVariable();
        if (httpProxy != null) {
            return httpProxy.getPort();
        }
        return proxyPort;
    }

    /**
     * Returns the optional proxy port the client will connect
     * through.  Returns either the proxyPort set on this object, or
     * if not provided, checks the value of the Java system property
     * for proxy port according to {@link #getProtocol()}: i.e. if
     * protocol is https, returns the value of the system property
     * https.proxyPort, otherwise returns value of http.proxyPort.
     * If neither are set, checks the value of the environment variable
     * according to {@link #getProtocol()}: i.e. if protocol is https,
     * returns the port in the value of the HTTPS_PROXY/https_proxy
     * environment variable, otherwise returns the port in the value
     * of the HTTP_PROXY/http_proxy environment variable.
     *
     * @return The proxy port the client will connect through.
     */
    public int getProxyPort() {
        if (proxyPort >= 0) {
            return proxyPort;
        } else if (getProxyPortProperty() >= 0) {
            return getProxyPortProperty();
        } else {
            return getProxyPortEnvironment();
        }
    }

    /**
     * Sets the optional proxy port the client will connect through.
     *
     * @param proxyPort
     *            The proxy port the client will connect through.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyPort(String proxyPort) {
    	if(proxyPort != null && proxyPort != "") {
    		this.proxyPort = normalizeNumber(proxyPort);	
    	}
    }
    
    /**
     * Returns the Java system property for proxy user name depending on
     * {@link #getProtocol()}: i.e. if protocol is https, returns
     * the value of the system property https.proxyUser, otherwise
     * returns value of http.proxyUser.
     */
    private String getProxyUsernameProperty() {
        return (getProtocol() == "https")
                ? getSystemProperty("https.proxyUser")
                : getSystemProperty("http.proxyUser");
    }

    /**
     * Returns the environment variable for proxy host depending on
     * {@link #getProtocol()}: i.e. if protocol is https, returns
     * the user name in the value of the environment variable
     * HTTPS_PROXY/https_proxy, otherwise, returns the user name in
     * the value of the environment variable HTTP_PROXY/http_proxy.
     */
    private String getProxyUsernameEnvironment() {
        URL httpProxy = getHttpProxyEnvironmentVariable();
        if (httpProxy != null) {
            try {
                return httpProxy.getUserInfo().split(":", 2)[0];
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Returns the optional proxy user name to use if connecting
     * through a proxy.  Returns either the proxyUsername set on this
     * object, or if not provided, checks the value of the Java system
     * property for proxy user name according to {@link #getProtocol()}:
     * i.e. if protocol is https, returns the value of the system
     * property https.proxyUser, otherwise returns value of
     * http.proxyUser. If neither are set, checks the value of the
     * environment variable according to {@link #getProtocol()}: i.e.
     * if protocol is https, returns the user name in the value of the
     * HTTPS_PROXY/https_proxy environment variable, otherwise returns
     * the user name in the value of the HTTP_PROXY/http_proxy environment
     * variable.
     *
     * @return The optional proxy user name the configured client will use if connecting through a
     *         proxy.
     */
    public String getProxyUsername() {
        if (proxyUsername != null) {
            return proxyUsername;
        } else if (getProxyUsernameProperty() != null) {
            return getProxyUsernameProperty();
        } else {
            return getProxyUsernameEnvironment();
        }
    }

    /**
     * Sets the optional proxy user name to use if connecting through a proxy.
     *
     * @param proxyUsername
     *            The proxy user name to use if connecting through a proxy.
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }


    /**
     * Returns the Java system property for proxy password depending on
     * {@link #getProtocol()}: i.e. if protocol is https, returns
     * the value of the system property https.proxyPassword, otherwise
     * returns value of http.proxyPassword.
     */
    private String getProxyPasswordProperty() {
        return (getProtocol() == "https")
                ? getSystemProperty("https.proxyPassword")
                : getSystemProperty("http.proxyPassword");
    }

    /**
     * Returns the environment variable for proxy host depending on
     * {@link #getProtocol()}: i.e. if protocol is https, returns
     * the password in the value of the environment variable HTTPS_PROXY/https_proxy,
     * otherwise, returns the password in the value of the environment
     * variable HTTP_PROXY/http_proxy.
     */
    private String getProxyPasswordEnvironment() {
        URL httpProxy = getHttpProxyEnvironmentVariable();
        if (httpProxy != null) {
            try {
                return httpProxy.getUserInfo().split(":", 2)[1];
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Returns the optional proxy password to use if connecting
     * through a proxy.  Returns either the proxyPassword set on this
     * object, or if not provided, checks the value of the Java system
     * property for proxy password according to {@link #getProtocol()}:
     * i.e. if protocol is https, returns the value of the system
     * property https.proxyPassword, otherwise returns value of
     * http.proxyPassword. If neither are set, checks the value of the
     * environment variable according to {@link #getProtocol()}: i.e. if
     * protocol is https, returns the password in the value of the
     * HTTPS_PROXY/https_proxy environment variable, otherwise returns
     * the password in the value of the HTTP_PROXY/http_proxy environment
     * variable.
     *
     * @return The password to use when connecting through a proxy.
     */
    public String getProxyPassword() {
        if (proxyPassword != null) {
            return proxyPassword;
        } else if (getProxyPasswordProperty() != null) {
            return getProxyPasswordProperty();
        } else {
            return getProxyPasswordEnvironment();
        }
    }

    /**
     * Sets the optional proxy password to use when connecting through a proxy.
     *
     * @param proxyPassword
     *            The password to use when connecting through a proxy.
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
    
    public void setProxyUserName(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * Returns the optional Windows domain name for configuring an NTLM proxy. If you aren't using a
     * Windows NTLM proxy, you do not need to set this field.
     *
     * @return The optional Windows domain name for configuring an NTLM proxy.
     */
    public String getProxyDomain() {
        return proxyDomain;
    }

    /**
     * Sets the optional Windows domain name for configuration an NTLM proxy. If you aren't using a
     * Windows NTLM proxy, you do not need to set this field.
     *
     * @param proxyDomain
     *            The optional Windows domain name for configuring an NTLM proxy.
     */
    public void setProxyDomain(String proxyDomain) {
        this.proxyDomain = proxyDomain;
    }

    /**
     * Returns the optional Windows workstation name for configuring NTLM proxy support. If you
     * aren't using a Windows NTLM proxy, you do not need to set this field.
     *
     * @return The optional Windows workstation name for configuring NTLM proxy support.
     */
    public String getProxyWorkstation() {
        return proxyWorkstation;
    }

    /**
     * Sets the optional Windows workstation name for configuring NTLM proxy support. If you aren't
     * using a Windows NTLM proxy, you do not need to set this field.
     *
     * @param proxyWorkstation
     *            The optional Windows workstation name for configuring NTLM proxy support.
     */
    public void setProxyWorkstation(String proxyWorkstation) {
        this.proxyWorkstation = proxyWorkstation;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    /**
     * Returns the Java system property for nonProxyHosts. We still honor this property even
     * {@link #getProtocol()} is https, see http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html.
     *
     * This method expects the property to be set as pipe separated list.
     */
    private String getNonProxyHostsProperty() {
        return getSystemProperty("http.nonProxyHosts");
    }
    
    
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Returns the value of the environment variable NO_PROXY/no_proxy. This method expects
     * the environment variable to be set as a comma separated list, so this method
     * converts the comma separated list to pipe separated list to be used internally.
     */
    private String getNonProxyHostsEnvironment() {
        String nonProxyHosts = getEnvironmentVariableCaseInsensitive("NO_PROXY");
        if (nonProxyHosts != null) {
            nonProxyHosts = nonProxyHosts.replace(",", "|");
        }

        return nonProxyHosts;
    }

    /**
     * Returns the optional hosts the client will access without going
     * through the proxy. Returns either the nonProxyHosts set on this
     * object, or if not provided, returns the value of the Java system property
     * http.nonProxyHosts. We still honor this property even when
     * {@link #getProtocol()} is https, see http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html.
     * This property is expected to be set as a pipe separated list. If neither are set,
     * returns the value of the environment variable NO_PROXY/no_proxy. This environment
     * variable is expected to be set as a comma separated list.
     *
     * @return The hosts the client will connect through bypassing the proxy.
     */
    public String getNonProxyHosts() {
        if (nonProxyHosts != null) {
            return nonProxyHosts;
        } else if (getNonProxyHostsProperty() != null) {
            return getNonProxyHostsProperty();
        } else {
            return getNonProxyHostsEnvironment();
        }
    }

    /**
     * Set the optional hosts the client will access without going
     * through the proxy.
     *
     * @param nonProxyHosts
     *            The hosts the client will access without going through the proxy.
     */
    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    /**
     * Returns the maximum number of retry attempts for failed retryable requests (ex: 5xx error
     * responses from a service). This method returns -1 before a maxErrorRetry value is explicitly
     * set by {@link #setMaxErrorRetry(int)}, in which case the configured RetryPolicy will be used
     * to control the retry count.
     *
     * @return The maximum number of retry attempts for failed retryable requests, or -1 if
     *         maxErrorRetry has not been set by {@link #setMaxErrorRetry(int)}.
     */
    public int getMaxErrorRetry() {
        return maxErrorRetry;
    }

    /**
     * Sets the maximum number of retry attempts for failed retryable requests (ex: 5xx error
     * responses from services).
     *
     * @param maxErrorRetry
     *            The maximum number of retry attempts for failed retryable requests. This value
     *            should not be negative.
     */
    public void setMaxErrorRetry(int maxErrorRetry) {
        if (maxErrorRetry < 0) {
            throw new IllegalArgumentException("maxErrorRetry should be non-negative");
        }
        this.maxErrorRetry = maxErrorRetry;
    }
    
    
    public void setMaxErrorRetry(String maxErrorRetry) {
    	int value = 0;
    	if(maxErrorRetry != null && maxErrorRetry != "") {
    		value = normalizeNumber(maxErrorRetry);	
    	}
        if (value < 0) {
            throw new IllegalArgumentException("maxErrorRetry should be non-negative");
        }
        this.maxErrorRetry = value;
    }


     /**
     * Returns the amount of time to wait (in milliseconds) for data to be transferred over an
     * established, open connection before the connection times out and is closed. A value of 0
     * means infinity, and isn't recommended.
     *
     * @return The amount of time to wait (in milliseconds) for data to be transferred over an
     *         established, open connection before the connection times out and is closed.
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Sets the amount of time to wait (in milliseconds) for data to be transferred over an
     * established, open connection before the connection times out and is closed. A value of 0
     * means infinity, and isn't recommended.
     *
     * @param socketTimeout
     *            The amount of time to wait (in milliseconds) for data to be transferred over an
     *            established, open connection before the connection times out and is closed.
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    
    public void setSocketTimeout(String socketTimeout) {
    	if(socketTimeout != null && socketTimeout != "") {
    		this.socketTimeout = normalizeNumber(socketTimeout);	
    	}
    }
    /**
     * Returns the amount of time to wait (in milliseconds) when initially establishing a connection
     * before giving up and timing out. A value of 0 means infinity, and is not recommended.
     *
     * @return The amount of time to wait (in milliseconds) when initially establishing a connection
     *         before giving up and timing out.
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the amount of time to wait (in milliseconds) when initially establishing a connection
     * before giving up and timing out. A value of 0 means infinity, and is not recommended.
     *
     * @param connectionTimeout
     *            The amount of time to wait (in milliseconds) when initially establishing a
     *            connection before giving up and timing out.
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public void setConnectionTimeout(String connectionTimeout) {
    	if(connectionTimeout != null && connectionTimeout != "") {
    		this.connectionTimeout = normalizeNumber(connectionTimeout);	
    	}
    }

    /**
     * Returns the amount of time to wait (in milliseconds) for the request to complete before
     * giving up and timing out. A non-positive value disables this feature.
     * <p>
     * This feature requires buffering the entire response (for non-streaming APIs) into memory to
     * enforce a hard timeout when reading the response. For APIs that return large responses this
     * could be expensive.
     * <p>
     * <p>
     * The request timeout feature doesn't have strict guarantees on how quickly a request is
     * aborted when the timeout is breached. The typical case aborts the request within a few
     * milliseconds but there may occasionally be requests that don't get aborted until several
     * seconds after the timer has been breached. Because of this, the request timeout feature
     * should not be used when absolute precision is needed.
     * </p>
     * <b>Note:</b> This feature is not compatible with Java 1.6.
     * </p>
     *
     * @return The amount of time to wait (in milliseconds) for the request to complete before
     *         giving up and timing out.
     * @see {@link AWSClientConfiguration#setClientExecutionTimeout(int)} to enforce a timeout across
     *      all retries
     */
    public int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Sets the amount of time to wait (in milliseconds) for the request to complete before giving
     * up and timing out. A non-positive value disables this feature.
     * <p>
     * This feature requires buffering the entire response (for non-streaming APIs) into memory to
     * enforce a hard timeout when reading the response. For APIs that return large responses this
     * could be expensive.
     * <p>
     * <p>
     * The request timeout feature doesn't have strict guarantees on how quickly a request is
     * aborted when the timeout is breached. The typical case aborts the request within a few
     * milliseconds but there may occasionally be requests that don't get aborted until several
     * seconds after the timer has been breached. Because of this, the request timeout feature
     * should not be used when absolute precision is needed.
     * </p>
     * <p>
     * <b>Note:</b> This feature is not compatible with Java 1.6.
     * </p>
     *
     * @param requestTimeout
     *            The amount of time to wait (in milliseconds) for the request to complete before
     *            giving up and timing out.
     * @see {@link AWSClientConfiguration#setClientExecutionTimeout(int)} to enforce a timeout across
     *      all retries
     */
    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }


    /**
     * Returns the amount of time (in milliseconds) to allow the client to complete the execution of
     * an API call. This timeout covers the entire client execution except for marshalling. This
     * includes request handler execution, all HTTP request including retries, unmarshalling, etc.
     * <p>
     * This feature requires buffering the entire response (for non-streaming APIs) into memory to
     * enforce a hard timeout when reading the response. For APIs that return large responses this
     * could be expensive.
     * <p>
     * <p>
     * The client execution timeout feature doesn't have strict guarantees on how quickly a request
     * is aborted when the timeout is breached. The typical case aborts the request within a few
     * milliseconds but there may occasionally be requests that don't get aborted until several
     * seconds after the timer has been breached. Because of this, the client execution timeout
     * feature should not be used when absolute precision is needed.
     * </p>
     * <p>
     * This may be used together with {@link AWSClientConfiguration#setRequestTimeout(int)} to enforce
     * both a timeout on each individual HTTP request (i.e. each retry) and the total time spent on
     * all requests across retries (i.e. the 'client execution' time). A non-positive value disables
     * this feature.
     * </p>
     * <p>
     * <b>Note:</b> This feature is not compatible with Java 1.6.
     * </p>
     *
     * @return The amount of time (in milliseconds) to allow the client to complete the execution of
     *         an API call.
     * @see {@link AWSClientConfiguration#setRequestTimeout(int)} to enforce a timeout per HTTP request
     */
    public int getClientExecutionTimeout() {
        return this.clientExecutionTimeout;
    }

    /**
     * Sets the amount of time (in milliseconds) to allow the client to complete the execution of
     * an API call. This timeout covers the entire client execution except for marshalling. This
     * includes request handler execution, all HTTP request including retries, unmarshalling, etc.
     * <p>
     * This feature requires buffering the entire response (for non-streaming APIs) into memory to
     * enforce a hard timeout when reading the response. For APIs that return large responses this
     * could be expensive.
     * <p>
     * <p>
     * The client execution timeout feature doesn't have strict guarantees on how quickly a request
     * is aborted when the timeout is breached. The typical case aborts the request within a few
     * milliseconds but there may occasionally be requests that don't get aborted until several
     * seconds after the timer has been breached. Because of this, the client execution timeout
     * feature should not be used when absolute precision is needed.
     * </p>
     * <p>
     * This may be used together with {@link AWSClientConfiguration#setRequestTimeout(int)} to enforce
     * both a timeout on each individual HTTP request (i.e. each retry) and the total time spent on
     * all requests across retries (i.e. the 'client execution' time). A non-positive value disables
     * this feature.
     * </p>
     * <p>
     * <b>Note:</b> This feature is not compatible with Java 1.6.
     * </p>
     *
     * @param clientExecutionTimeout
     *            The amount of time (in milliseconds) to allow the client to complete the execution
     *            of an API call. A value of '0' disables this feature.
     * @see {@link AWSClientConfiguration#setRequestTimeout(int)} to enforce a timeout per HTTP request
     */
    public void setClientExecutionTimeout(int clientExecutionTimeout) {
        this.clientExecutionTimeout = clientExecutionTimeout;
    }


    /**
     * Returns the optional size hints (in bytes) for the low level TCP send and receive buffers.
     * This is an advanced option for advanced users who want to tune low level TCP parameters to
     * try and squeeze out more performance.
     * <p>
     * The optimal TCP buffer sizes for a particular application are highly dependent on network
     * configuration and operating system configuration and capabilities. For example, most modern
     * operating systems provide auto-tuning functionality for TCP buffer sizes, which can have a
     * big impact on performance for TCP connections that are held open long enough for the
     * auto-tuning to optimize buffer sizes.
     * <p>
     * Large buffer sizes (ex: 2MB) will allow the operating system to buffer more data in memory
     * without requiring the remote server to acknowledge receipt of that information, so can be
     * particularly useful when the network has high latency.
     * <p>
     * This is only a <b>hint</b>, and the operating system may choose not to honor it. When using
     * this option, users should <b>always</b> check the operating system's configured limits and
     * defaults. Most OS's have a maximum TCP buffer size limit configured, and won't let you go
     * beyond that limit unless you explicitly raise the max TCP buffer size limit.
     * <p>
     * There are many resources available online to help with configuring TCP buffer sizes and
     * operating system specific TCP settings, including:
     * <ul>
     * <li>http://onlamp.com/pub/a/onlamp/2005/11/17/tcp_tuning.html</li>
     * <li>http://fasterdata.es.net/TCP-tuning/</li>
     * </ul>
     *
     * @return A two element array containing first the TCP send buffer size hint and then the TCP
     *         receive buffer size hint.
     */
    public int[] getSocketBufferSizeHints() {
        return new int[] { socketSendBufferSizeHint, socketReceiveBufferSizeHint };
    }

    /**
     * Sets the optional size hints (in bytes) for the low level TCP send and receive buffers. This
     * is an advanced option for advanced users who want to tune low level TCP parameters to try and
     * squeeze out more performance.
     * <p>
     * The optimal TCP buffer sizes for a particular application are highly dependent on network
     * configuration and operating system configuration and capabilities. For example, most modern
     * operating systems provide auto-tuning functionality for TCP buffer sizes, which can have a
     * big impact on performance for TCP connections that are held open long enough for the
     * auto-tuning to optimize buffer sizes.
     * <p>
     * Large buffer sizes (ex: 2MB) will allow the operating system to buffer more data in memory
     * without requiring the remote server to acknowledge receipt of that information, so can be
     * particularly useful when the network has high latency.
     * <p>
     * This is only a <b>hint</b>, and the operating system may choose not to honor it. When using
     * this option, users should <b>always</b> check the operating system's configured limits and
     * defaults. Most OS's have a maximum TCP buffer size limit configured, and won't let you go
     * beyond that limit unless you explicitly raise the max TCP buffer size limit.
     * <p>
     * There are many resources available online to help with configuring TCP buffer sizes and
     * operating system specific TCP settings, including:
     * <ul>
     * <li>http://onlamp.com/pub/a/onlamp/2005/11/17/tcp_tuning.html</li>
     * <li>http://fasterdata.es.net/TCP-tuning/</li>
     * </ul>
     *
     * @param socketSendBufferSizeHint
     *            The size hint (in bytes) for the low level TCP send buffer.
     * @param socketReceiveBufferSizeHint
     *            The size hint (in bytes) for the low level TCP receive buffer.
     */
    public void setSocketBufferSizeHints(int socketSendBufferSizeHint, int socketReceiveBufferSizeHint) {
        this.socketSendBufferSizeHint = socketSendBufferSizeHint;
        this.socketReceiveBufferSizeHint = socketReceiveBufferSizeHint;
    }

    public void setSocketSendBufferSizeHints(int socketSendBufferSizeHint) {
        this.socketSendBufferSizeHint = socketSendBufferSizeHint;
    }
    
    public void setSocketSendBufferSizeHints(String socketSendBufferSizeHint) {
    	if(socketSendBufferSizeHint != null && socketSendBufferSizeHint != "") {
    		this.socketSendBufferSizeHint = normalizeNumber(socketSendBufferSizeHint);	
    	}
    }
    
    public void setSocketReceiveBufferSizeHints(int socketReceiveBufferSizeHint) {
        this.socketReceiveBufferSizeHint = socketReceiveBufferSizeHint;
    }
    
    public void setSocketReceiveBufferSizeHints(String socketReceiveBufferSizeHint) {
    	if(socketReceiveBufferSizeHint != null && socketReceiveBufferSizeHint != "") {
    		this.socketReceiveBufferSizeHint = normalizeNumber(socketReceiveBufferSizeHint);
    	}
    }
    
    private URL getHttpProxyEnvironmentVariable() {
        if (getProtocol() == "http") {
            return getUrlEnvVar(httpProxyHolder, "HTTP_PROXY");
        }
        return getUrlEnvVar(httpsProxyHolder, "HTTPS_PROXY");
    }

    private URL getUrlEnvVar(AtomicReference<URLHolder> cache, String name) {
        if (cache.get() == null) {
            URLHolder holder = new URLHolder();
            String value = getEnvironmentVariableCaseInsensitive(name);
            if (value != null) {
                try {
                    holder.url = new URL(value);
                } catch (MalformedURLException e) {
                }
            }
            cache.compareAndSet(null, holder);
        }
        return cache.get().url;
    }

    static class URLHolder {
        private URL url;
    }
    
    private static int normalizeNumber(String number) {
    	if (number != null) {
    		number = number.replaceAll("\"", "");
    	} 
    	
    	if (number != null || number == "") {
    		return 0;
    	} 
   		
    	return Integer.parseInt(number);
    }
}