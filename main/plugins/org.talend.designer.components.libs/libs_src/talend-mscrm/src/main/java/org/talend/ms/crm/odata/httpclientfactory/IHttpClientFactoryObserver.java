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

import org.apache.http.impl.client.DefaultHttpClient;

/**
 * This interface allows DynamicsCRMClient to retrieve the last generate DefaultHttpClient.
 */
public interface IHttpClientFactoryObserver {

    public void httpClientCreated(DefaultHttpClientState httpclient);

}
