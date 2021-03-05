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
package org.talend.ms.crm.odata.httpclientfactory;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.impl.client.DefaultHttpClient;

/**
 * This class is a container to generated DefaultHttpClient.
 * It allows DynamicsCRMClient to retrieve DefaultHttpClient and expire them setting needNewHttpClient to true.
 */
public class DefaultHttpClientState {

    private AtomicBoolean needNewHttpClient = new AtomicBoolean(false);

    private final DefaultHttpClient httpClient;

    public DefaultHttpClientState(DefaultHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setNeedNewHttpClient(boolean b) {
        needNewHttpClient.set(b);
    }

    public boolean needNewHttpClient() {
        return needNewHttpClient.get();
    }

    public DefaultHttpClient getHttpClient() {
        return this.httpClient;
    }

}
