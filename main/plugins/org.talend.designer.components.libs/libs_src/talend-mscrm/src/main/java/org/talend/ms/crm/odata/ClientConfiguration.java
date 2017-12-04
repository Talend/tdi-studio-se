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
package org.talend.ms.crm.odata;

public final class ClientConfiguration {
    
    /*
     * Implemented authentication strategies for OData/MS CRM. 
     */
    public static enum AuthStrategyEnum {NTLM, OAUTH};

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
     * Workstation for NTLM authentication.
     */
    private String workstation;
    
    /*
     * Domain for NTLM authentication.
     */
    private String domain;
    
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
    
    private AuthStrategyEnum authStrategy = AuthStrategyEnum.OAUTH;

    
    ClientConfiguration(AuthStrategyEnum authStrategy) {
        this.authStrategy = authStrategy;
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
    
    
    public AuthStrategyEnum getAuthStrategy() {
        return authStrategy;
    }
    
    public void setAuthStrategy(AuthStrategyEnum authStrategy) {
        this.authStrategy = authStrategy;
    }
    
    public String getWorkstation() {
        return workstation;
    }

    
    public void setWorkstation(String workstation) {
        this.workstation = workstation;
    }

    
    public String getDomain() {
        return domain;
    }

    
    public void setDomain(String domain) {
        this.domain = domain;
    }

}
