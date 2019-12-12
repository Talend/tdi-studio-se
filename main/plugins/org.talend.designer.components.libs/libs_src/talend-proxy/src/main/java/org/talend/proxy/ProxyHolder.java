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

    /**
     *
     * @param proxy HTTP or SOCKS proxy instance to use
     * @param host without protocol
     * @param port -1 to apply proxy for every port
     */
    public void putNewHost(Proxy proxy, String host, int port) {
        if (port != -1) {
            proxyMap.put(host + ":" + port, proxy);
        } else {
            proxyMap.put(host, proxy);
        }
    }

    public Map<String, Proxy> getProxyMap() {
        return proxyMap;
    }
}

