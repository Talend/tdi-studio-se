// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.ms.crm.odata;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

/**
 * Give proxy that can be used by DynamicsCRMClient.
 *
 * No need to use singleton pattern since no implementation/extends, static is enough.
 */
public final class ProxyProvider {

    private final static String proxyHost = System.getProperty("https.proxyHost");

    private final static String proxyPort = System.getProperty("https.proxyPort");

    private final static String proxyUserName = System.getProperty("https.proxyUser");

    private final static String proxyUserPassword = System.getProperty("https.proxyPassword");

    private ProxyProvider() {
    }

    public static String getProxyUserName() {
        return proxyUserName;
    }

    public static String getProxyUserPassword() {
        return proxyUserPassword;
    }

    /**
     * Get the proxy setting if there is proxy for system
     */
    public static Proxy getProxy() {
        if (proxyHost != null) {
            int port = -1;
            if (proxyPort != null && proxyPort.length() > 0) {
                port = Integer.parseInt(proxyPort);
            }
            SocketAddress addr = new InetSocketAddress(proxyHost, port);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
            return proxy;
        }
        return null;
    }
}
