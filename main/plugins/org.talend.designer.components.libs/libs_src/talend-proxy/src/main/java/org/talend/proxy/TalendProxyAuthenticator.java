package org.talend.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;

public class TalendProxyAuthenticator extends Authenticator {
    private static TalendProxyAuthenticator instance;

    public static synchronized TalendProxyAuthenticator getInstance() {
        if (instance == null) {
            instance = new TalendProxyAuthenticator();
        }

        return instance;
    }

    private TalendProxyAuthenticator() {

    }

    private Map<String, ProxyCreds> proxyCredsSet = new HashMap<>();

    public void addAuthForProxy(String host, String port, String userName, String pass) {
        proxyCredsSet.put(host + ":" + port, new ProxyCreds(userName, pass));
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String requestURI = super.getRequestingHost() + ":" + super.getRequestingPort();
        if (proxyCredsSet.containsKey(requestURI)) {
            return new PasswordAuthentication(proxyCredsSet.get(requestURI).getUser(), proxyCredsSet.get(requestURI).getPass().toCharArray());
        } else {
            return super.getPasswordAuthentication(); //don't use authentication
        }
    }
}
