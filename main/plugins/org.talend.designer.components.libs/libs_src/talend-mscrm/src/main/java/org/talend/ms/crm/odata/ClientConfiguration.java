package org.talend.ms.crm.odata;

public class ClientConfiguration {

    /*
     * This would be obtained after you register the Dynamic CRM in Active Directory on the Microsoft Azure portal
     */
    private String clientId;

    /*
     * Identifier of the target resource that is the recipient of the requested token.
     */
    private String resource;

    /*
     * Username of the managed or federated user.
     */
    private String userName;

    /*
     * Password of the managed or federated user.
     */
    private String password;

    /*
     * The URL of the authenticating authority
     */
    private String authoryEndpoint;

    /**
     * @param clientId
     * @param resource
     * @param userName
     * @param password
     * @param authoryEndpoint
     */
    public ClientConfiguration(String clientId, String userName, String password, String authoryEndpoint) {
        this.clientId = clientId;
        this.userName = userName;
        this.password = password;
        this.authoryEndpoint = authoryEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthoryEndpoint() {
        return authoryEndpoint;
    }

    public void setAuthoryEndpoint(String authoryEndpoint) {
        this.authoryEndpoint = authoryEndpoint;
    }

}
