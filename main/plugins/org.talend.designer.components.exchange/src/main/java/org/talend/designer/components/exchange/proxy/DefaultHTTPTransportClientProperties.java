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
package org.talend.designer.components.exchange.proxy;

import org.apache.axis.components.net.TransportClientProperties;
import org.apache.commons.lang.StringUtils;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class DefaultHTTPTransportClientProperties implements TransportClientProperties {

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getProxyHost()
     */
    public String getProxyHost() {
        return StringUtils.trimToEmpty(System.getProperty("http.proxyHost")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getNonProxyHosts()
     */
    public String getNonProxyHosts() {
        return StringUtils.trimToEmpty(System.getProperty("http.nonProxyHosts")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getPort()
     */
    public String getProxyPort() {
        return StringUtils.trimToEmpty(System.getProperty("http.proxyPort")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getProxyUser()
     */
    public String getProxyUser() {
        return StringUtils.trimToEmpty(System.getProperty("http.proxyUser")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getProxyPassword()
     */
    public String getProxyPassword() {
        return StringUtils.trimToEmpty(System.getProperty("http.proxyPassword")); //$NON-NLS-1$
    }
}
