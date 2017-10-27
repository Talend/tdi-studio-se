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
package org.talend.salesforce.oauth;

/**
 * created by bchen on Aug 28, 2013 Detailled comment
 * 
 */
public class Token {

    private String access_token;

    private String refresh_token;

    private String instance_url;

    private String id;

    private long issued_at;

    private String signature;

    /**
     * Getter for access_token.
     * 
     * @return the access_token
     */
    public String getAccess_token() {
        return this.access_token;
    }

    /**
     * Sets the access_token.
     * 
     * @param access_token the access_token to set
     */
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    /**
     * Getter for refresh_token.
     * 
     * @return the refresh_token
     */
    public String getRefresh_token() {
        return this.refresh_token;
    }

    /**
     * Sets the refresh_token.
     * 
     * @param refresh_token the refresh_token to set
     */
    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    /**
     * Getter for instance_url.
     * 
     * @return the instance_url
     */
    public String getInstance_url() {
        return this.instance_url;
    }

    /**
     * Sets the instance_url.
     * 
     * @param instance_url the instance_url to set
     */
    public void setInstance_url(String instance_url) {
        this.instance_url = instance_url;
    }

    /**
     * Getter for id.
     * 
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for issued_at.
     * 
     * @return the issued_at
     */
    public long getIssued_at() {
        return this.issued_at;
    }

    /**
     * Sets the issued_at.
     * 
     * @param issued_at the issued_at to set
     */
    public void setIssued_at(long issued_at) {
        this.issued_at = issued_at;
    }

    /**
     * Getter for signature.
     * 
     * @return the signature
     */
    public String getSignature() {
        return this.signature;
    }

    /**
     * Sets the signature.
     * 
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

}
