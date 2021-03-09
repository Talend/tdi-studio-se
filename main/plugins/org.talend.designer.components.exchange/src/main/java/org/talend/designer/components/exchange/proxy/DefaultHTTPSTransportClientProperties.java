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

import org.apache.commons.lang.StringUtils;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class DefaultHTTPSTransportClientProperties extends DefaultHTTPTransportClientProperties {

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getProxyHost()
     */
    @Override
    public String getProxyHost() {
        return StringUtils.trimToEmpty(System.getProperty("https.proxyHost")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getNonProxyHosts()
     */
    @Override
    public String getNonProxyHosts() {

        return StringUtils.trimToEmpty(System.getProperty("https.nonProxyHosts")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getPort()
     */
    @Override
    public String getProxyPort() {

        return StringUtils.trimToEmpty(System.getProperty("https.proxyPort")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getUser()
     */
    @Override
    public String getProxyUser() {

        return StringUtils.trimToEmpty(System.getProperty("https.proxyUser")); //$NON-NLS-1$
    }

    /**
     * @see org.apache.axis.components.net.TransportClientProperties#getPassword()
     */
    @Override
    public String getProxyPassword() {

        return StringUtils.trimToEmpty(System.getProperty("https.proxyPassword")); //$NON-NLS-1$
    }
}
