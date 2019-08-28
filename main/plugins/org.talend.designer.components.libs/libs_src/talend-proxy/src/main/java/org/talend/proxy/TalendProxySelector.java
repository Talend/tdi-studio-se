package org.talend.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;


public class TalendProxySelector extends ProxySelector {
    private static TalendProxySelector instance;

    private static final Logger log = Logger.getLogger(TalendProxySelector.class);

    private ThreadLocal<ProxyHolder> threadLocalProxyHolder;
    private ProxyHolder globalProxyHolder;

    public static synchronized TalendProxySelector getInstance() {
        if (instance == null) {
            instance = new TalendProxySelector();
        }

        return instance;
    }

    private TalendProxySelector() {
        globalProxyHolder = new ProxyHolder();
    }

    public void addProxySettings(Proxy proxy, boolean forAllThreads, String host, int port) {
        if (forAllThreads) {
            globalProxyHolder.putNewHost(proxy, host, port);
        } else {
            if (threadLocalProxyHolder == null) {
                threadLocalProxyHolder = new ThreadLocal<>();
            }
            if (threadLocalProxyHolder.get() == null) {
                ProxyHolder newProxyHolder = new ProxyHolder();
                threadLocalProxyHolder.set(newProxyHolder);
            }

            threadLocalProxyHolder.get().putNewHost(proxy, host, port);
        }
    }

    /**
     * Finds and return proxy was set for specific host
     * @param uriString host:port
     * @return Optional of Proxy if such proxy setting was set
     */
    public Optional<Proxy> getProxyForUriString(String uriString) {
        if (proxyHolderContainsHost(globalProxyHolder, uriString)) {
            log.debug("All threads proxy " + globalProxyHolder.getProxyMap().get(uriString) + " is using to connect to URI " + uriString);
            return globalProxyHolder.getProxyMap().containsKey(uriString) ? Optional.of(globalProxyHolder.getProxyMap().get(uriString)) :
                    Optional.of(globalProxyHolder.getProxyMap().get(uriString.substring(0, uriString.lastIndexOf(":"))));
        } else if (threadLocalProxyHolder != null && proxyHolderContainsHost(threadLocalProxyHolder.get(), uriString)) {
            log.debug("Proxy " + threadLocalProxyHolder.get().getProxyMap().get(uriString) + " is using to connect to URI " + uriString);
            return threadLocalProxyHolder.get().getProxyMap().containsKey(uriString) ?
                    Optional.of(threadLocalProxyHolder.get().getProxyMap().get(uriString)) :
                    Optional.of(threadLocalProxyHolder.get().getProxyMap().get(uriString.substring(0, uriString.lastIndexOf(":"))));
        } else {
            log.debug("No proxy is using to connect to URI " + uriString);
            return Optional.of(Proxy.NO_PROXY);
        }
    }

    @Override
    public List<Proxy> select(URI uri) {
        String uriString = uri.getHost();
        if (uri.getPort() != -1) {
            uriString += ":" + uri.getPort() ;
        }
        log.debug("Network request hadling from Talend proxy selector. Thread " + Thread.currentThread().getName() + ". URI to connect: " + uriString);
        return Collections.singletonList(getProxyForUriString(uriString).orElse(Proxy.NO_PROXY));
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (ioe != null) {
            log.warn("Connect failed when use Talend ProxySelector to the URI:" + uri.toString(), ioe);
        } else {
            log.warn("Connect failed when use Talend ProxySelector to the " + uri);
        }
    }

    private static boolean proxyHolderContainsHost(ProxyHolder holder, String uriString) {
        return holder != null &&
                (holder.getProxyMap().containsKey(uriString)
                        || holder.getProxyMap().containsKey(uriString.substring(0, uriString.indexOf(":"))));
    }
}

