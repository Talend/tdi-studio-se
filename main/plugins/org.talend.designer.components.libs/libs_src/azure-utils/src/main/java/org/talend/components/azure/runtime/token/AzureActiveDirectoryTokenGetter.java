package org.talend.components.azure.runtime.token;

import java.util.Collections;
import java.util.Set;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;

public class AzureActiveDirectoryTokenGetter {

    private final String authority;
    private final String clientId;
    private final String clientSecret;
    private static final String RESOURCE = "https://storage.azure.com/";

    public AzureActiveDirectoryTokenGetter(String tenantId, String clientId, String clientSecret) {
        this.authority = "https://login.microsoftonline.com/" + tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Returns an active directory token will be used for connection
     */
    public String retrieveAccessToken() throws Exception {
        IClientCredential credential = ClientCredentialFactory.createFromSecret(clientSecret);
        ConfidentialClientApplication confidentialClientApplication = ConfidentialClientApplication.builder(clientId, credential)
                .authority(authority).build();

        Set<String> scopes = Collections.singleton(RESOURCE + "/.default"); //for azure storage scope should ends with ".default"
        ClientCredentialParameters parameters = ClientCredentialParameters.builder(scopes).build();

        IAuthenticationResult result = confidentialClientApplication.acquireToken(parameters).get();
        return result.accessToken();
    }
}
