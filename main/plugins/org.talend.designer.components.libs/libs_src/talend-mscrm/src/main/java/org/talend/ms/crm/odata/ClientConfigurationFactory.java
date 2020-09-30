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
package org.talend.ms.crm.odata;

import org.talend.ms.crm.odata.ClientConfiguration.AuthStrategyEnum;

/**
 * Generate the ClientConfiguration according to wanted authentication.
 * <p>
 * Different authentications need different information.
 */
public class ClientConfigurationFactory {

    public final static ClientConfiguration buildOAuthNativeClientConfiguration(String clientId, String userName, String password,
                                                                                String authoryEndpoint) {
        ClientConfiguration clientConfiguration = new ClientConfiguration(AuthStrategyEnum.OAUTH);
        clientConfiguration.setAppRegisteredType(ClientConfiguration.AppRegisteredType.NATIVE_APP);
        clientConfiguration.setClientId(clientId);
        clientConfiguration.setUserName(userName);
        clientConfiguration.setPassword(password);
        clientConfiguration.setAuthoryEndpoint(authoryEndpoint);

        return clientConfiguration;
    }

    public final static ClientConfiguration buildOAuthWebClientConfiguration(String clientId, String clientSecret, String userName, String password,
                                                                             String authoryEndpoint, ClientConfiguration.WebAppPermission permission) {
        ClientConfiguration clientConfiguration = new ClientConfiguration(AuthStrategyEnum.OAUTH);
        clientConfiguration.setAppRegisteredType(ClientConfiguration.AppRegisteredType.WEB_APP);
        clientConfiguration.setWebAppPermission(permission);

        clientConfiguration.setClientId(clientId);
        clientConfiguration.setClientSecret(clientSecret);
        clientConfiguration.setUserName(userName);
        clientConfiguration.setPassword(password);
        clientConfiguration.setAuthoryEndpoint(authoryEndpoint);

        return clientConfiguration;
    }

    public final static ClientConfiguration buildNtlmClientConfiguration(String userName, String password, String workstation,
                                                                         String domain) {
        ClientConfiguration clientConfiguration = new ClientConfiguration(AuthStrategyEnum.NTLM);
        clientConfiguration.setUserName(userName);
        clientConfiguration.setPassword(password);
        clientConfiguration.setWorkstation(workstation);
        clientConfiguration.setDomain(domain);

        return clientConfiguration;
    }

    public final static ClientConfiguration buildOAuthPremiseClientConfiguration(String userName, String password, String authoryEndpoint,
                                                                                 String serviceAPI, String clientId, String redirectUrl) {
        ClientConfiguration clientConfiguration = new ClientConfiguration(AuthStrategyEnum.OAUTH_PREMISE);
        clientConfiguration.setUserName(userName);
        clientConfiguration.setPassword(password);
        clientConfiguration.setAuthoryEndpoint(authoryEndpoint);
        clientConfiguration.setClientId(clientId);
        clientConfiguration.setRedirectURL(redirectUrl);
        clientConfiguration.setServiceAPI(serviceAPI);

        return clientConfiguration;
    }

}
