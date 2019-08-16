package org.talend.proxy;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;


/**
 * Use only inside of ThreadLocal
 */
public class ProxyHolder {
    private Map<String, Proxy> proxyMap;

    public ProxyHolder() {
        proxyMap = new HashMap<>();
    }

    public void putNewHosts(Proxy proxy, String... hosts) {
        for (String host: hosts) {
            proxyMap.put(host, proxy);
        }
    }

    public Map<String, Proxy> getProxyMap() {
        return proxyMap;
    }
}

