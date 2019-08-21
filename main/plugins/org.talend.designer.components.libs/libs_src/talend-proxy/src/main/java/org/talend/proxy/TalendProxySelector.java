package org.talend.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

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

    public void addProxySettingsForThread(Proxy proxy, boolean forAllThreads, String... hosts) {
        if (forAllThreads) {
            globalProxyHolder.putNewHosts(proxy, hosts);
        } else {
            if (threadLocalProxyHolder == null) {
                threadLocalProxyHolder = new ThreadLocal<>();
            }
            if (threadLocalProxyHolder.get() == null) {
                ProxyHolder newProxyHolder = new ProxyHolder();
                threadLocalProxyHolder.set(newProxyHolder);
            }

            threadLocalProxyHolder.get().putNewHosts(proxy, hosts);
        }
    }

    @Override
    public List<Proxy> select(URI uri) {
        String uriString = uri.getHost() + ":" + uri.getPort();
        log.debug("Network request hadling from Talend proxy selector. Thread " + Thread.currentThread().getName() + ". URI to connect: " + uriString);
        if (globalProxyHolder.getProxyMap().containsKey(uriString)) {
            log.debug("All threads proxy " + globalProxyHolder.getProxyMap().get(uriString) + " is using to connect to URI " + uriString);
            return Collections.singletonList(globalProxyHolder.getProxyMap().get(uriString));
        } else if (threadLocalProxyHolder != null && threadLocalProxyHolder.get() != null && threadLocalProxyHolder.get().getProxyMap().containsKey(uriString))
        {
            log.debug("Proxy " + threadLocalProxyHolder.get().getProxyMap().get(uriString) + " is using to connect to URI " + uriString);
            return Collections.singletonList(threadLocalProxyHolder.get().getProxyMap().get(uriString));
        } else {
            log.debug("No proxy is using to connect to URI " + uriString);
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (ioe != null) {
            log.warn("Connect failed when use Talend ProxySelector to the URI:" + uri.toString(), ioe);
        } else {
            log.warn("Connect failed when use Talend ProxySelector to the " + uri);
        }
    }
}

