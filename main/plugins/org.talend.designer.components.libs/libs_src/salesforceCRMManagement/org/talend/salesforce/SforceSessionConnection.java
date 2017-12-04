// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.salesforce;

import com.salesforce.soap.partner.CallOptions;
import com.salesforce.soap.partner.SessionHeader;
import com.salesforce.soap.partner.SforceServiceStub;

/**
 * created by bchen on Jul 9, 2014 Detailled comment
 *
 */
public class SforceSessionConnection extends SforceConnection {
    private final String endpoint;
    private final String session_id;
    private boolean needCompression;
    private boolean useHttpChunked;
    private int timeout;
    private String clientID;
    private SforceSessionConnection() throws Exception {
        throw new Exception("should use builder to init"); //$NON-NLS-1$
    }

    private SforceSessionConnection(Builder builder) throws Exception {
        this.endpoint = builder.endpoint;
        this.session_id = builder.session_id;
        check();
        init();
    }

    private void check() throws Exception {
        if (endpoint == null || endpoint.trim().length() == 0 || session_id == null || session_id.trim().length() == 0) {
            throw new RuntimeException("Login failed!");
        }
    }

    private void init() throws Exception {
        if (clientID != null) {
            co = new CallOptions();
            co.setClient(clientID);
        }
        stub = new SforceServiceStub();
        SforceManagementUtil.needCompression(stub, needCompression);
        SforceManagementUtil.setTimeout(stub, timeout);
        SforceManagementUtil.useHttpChunked(stub, useHttpChunked);
        // SforceManagementUtil.setHttpProxy(stub);//don't support proxy for OAuth
        sh = new SessionHeader();
        // renewSession();
        SforceManagementUtil.setEndpoint(stub, endpoint);
        sh.setSessionId(session_id);
    }

    @Override
    protected void renewSession() throws Exception {
        throw new Exception("Session expire, need to renew session");
    }

    public static class Builder {
        private final String endpoint;
        private final String session_id;
        private boolean needCompression = false;
        private boolean useHttpChunked;
        private int timeout = 60000;
        private String clientID = null;
        public Builder(String endpoint, String session_id) {
            this.endpoint = endpoint;
            this.session_id = session_id;
        }

        public Builder needCompression(boolean needCompression) {
            this.needCompression = needCompression;
            return this;
        }

        public Builder useHttpChunked(boolean useHttpChunked) {
            this.useHttpChunked = useHttpChunked;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setTimeout(String timeout) {
            this.timeout = Integer.valueOf(timeout);
            return this;
        }

        public Builder setClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public SforceSessionConnection build() throws Exception {
            return new SforceSessionConnection(this);
        }
    }

}
