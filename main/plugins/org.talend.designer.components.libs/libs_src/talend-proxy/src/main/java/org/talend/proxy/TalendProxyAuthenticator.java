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

    private Map<String, ProxyCreds> proxyCredsMap = new HashMap<>();

    public synchronized void addAuthForProxy(String host, String port, String userName, String pass) {
        proxyCredsMap.put(host + ":" + port, new ProxyCreds(userName, pass));
    }

    public synchronized ProxyCreds getCredsForProxyURI(String proxyURI) {
        return proxyCredsMap.get(proxyURI);
    }

    @Override
    protected synchronized PasswordAuthentication getPasswordAuthentication() {
        String requestURI = super.getRequestingHost() + ":" + super.getRequestingPort();
        if (proxyCredsMap.containsKey(requestURI)) {
            return new PasswordAuthentication(proxyCredsMap.get(requestURI).getUser(), proxyCredsMap.get(requestURI).getPass().toCharArray());
        } else {
            return super.getPasswordAuthentication(); //don't use authentication
        }
    }
}
