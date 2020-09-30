package org.talend.ms.crm.odata.authentication.httpclienthelper;

public final class Token {
    // Time to be sure to refresh the token at time
    private final static int EXPIRATION_SECURITY_GAP_SECOND = 60;

    private String access_token;
    private String token_type;
    private Long expires_in;
    private Long created_at;

    public Token(){
        created_at = System.currentTimeMillis();
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
    }

}
