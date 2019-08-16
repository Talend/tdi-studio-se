package org.talend.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TalendProxySelector extends ProxySelector {
    private static TalendProxySelector instance;

    private static final Logger log = LoggerFactory.getLogger(TalendProxySelector.class);

    private ThreadLocal<ProxyHolder> myHolder;

    public static synchronized TalendProxySelector getInstance() {
        if (instance == null) {
            instance = new TalendProxySelector();
        }

        return instance;
    }

    private TalendProxySelector() {
    }

    public void addProxySettingsForThread(Proxy proxy, String... hosts) {
        if (myHolder == null) {
            myHolder = new ThreadLocal<>();
        }
        if (myHolder.get() == null) {
            ProxyHolder newProxyHolder = new ProxyHolder();
            myHolder.set(newProxyHolder);
        }

        myHolder.get().putNewHosts(proxy, hosts);
    }

    @Override
    public List<Proxy> select(URI uri) {
        String uriString = uri.getHost() + ":" + uri.getPort();
        log.debug("Network request hadling from Talend proxy selector. Thread {}. URI to connect: {}", Thread.currentThread().getName(), uriString);
        if (myHolder != null && myHolder.get() != null && myHolder.get().getProxyMap().containsKey(uri.toString())) {
            log.debug("Proxy {} is using to connect to URI {}", myHolder.get().getProxyMap().get(uriString), uriString);
            return Collections.singletonList(myHolder.get().getProxyMap().get(uriString));
        } else {
            log.debug("No proxy is using to connect to URI {}", uriString);
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (ioe != null) {
            log.warn("Connect failed when use Talend ProxySelector to the URI:" + uri.toString(), ioe);
        } else {
            log.warn("Connect failed when use Talend ProxySelector to the {}", uri);
        }
    }
}

