// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.ms.crm.odata.authentication;

import org.talend.ms.crm.odata.ClientConfiguration;

/**
 * Factory for OData authentication strategies.
 * <p>
 * No need to use singleton pattern since no implementation/extends, static is enough.
 */
public final class AuthStrategyFactory {

    private AuthStrategyFactory() {
    }

    public static IAuthStrategy createAuthStrategy(ClientConfiguration conf) {
        IAuthStrategy authStrategy = null;
        switch (conf.getAuthStrategy()) {
            case OAUTH:
                authStrategy = new OAuthStrategyImpl(conf);
                break;
            case NTLM:
                authStrategy = new NTLMStrategyImpl(conf);
                break;
            case OAUTH_PREMISE:
                authStrategy = new OAuthPremiseStrategyImpl(conf);
                break;
        }

        return authStrategy;
    }

}
