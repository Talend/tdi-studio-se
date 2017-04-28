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

    private int maxRetryTimes = 5;

    /*
     * Retry intervalTime 1000(ms)
     */
    private int intervalTime = 1000;

    /*
     * Default timeout 60(s)
     */
    private int timeout = 60;

    private boolean reuseHttpClient;

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

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setMaxRetry(int maxRetry, int intervalTime) {
        this.maxRetryTimes = maxRetry;
        if (intervalTime > 0) {
            this.intervalTime = intervalTime;
        }

    }

    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isReuseHttpClient() {
        return reuseHttpClient;
    }

    public void setReuseHttpClient(boolean reuseHttpClient) {
        this.reuseHttpClient = reuseHttpClient;
    }

}
