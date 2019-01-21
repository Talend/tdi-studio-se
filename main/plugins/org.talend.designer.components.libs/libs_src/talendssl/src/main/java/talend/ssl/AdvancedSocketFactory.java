// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi
// Source File Name: AdvancedSocketFactory.java

package talend.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Security;
import java.util.Hashtable;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.internal.ssl.Provider;

public class AdvancedSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory factory;

    private static TrustManager trustManagers[] = null;

    private static AdvancedSocketFactory default_factory = null;

    private static Hashtable factories = null;

    private static String certStorePath = null;

    private static String certStorePwd = null;

    private static final TrustManager[] ALWAYS_TRUST_MANAGER = new TrustManager[] { new AlwaysTruster() };
    
    protected AdvancedSocketFactory() {
        factory = null;
        init(null, null);
    }

    protected AdvancedSocketFactory(InputStream in, String keyStore, String password) throws Exception {
        factory = null;
        KeyStore ks = null;
        if (keyStore.endsWith(".p12"))
            ks = KeyStore.getInstance("PKCS12");
        else
            ks = KeyStore.getInstance("JKS");
        char pwd[] = password.toCharArray();
        ks.load(in, pwd);
        init(ks, pwd);
    }

    protected AdvancedSocketFactory(String keyStore, String passphrase) {
        factory = null;
        init(null, null);
    }

    private static void closeStream(InputStream in) {
        if (in == null)
            return;
        try {
            in.close();
        } catch (Exception _ex) {
        }
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress client_host, int client_port) throws IOException,
            UnknownHostException {
        return factory.createSocket(host, port, client_host, client_port);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException, UnknownHostException {
        return factory.createSocket(host, port);
    }

    public Socket createSocket(InetAddress host, int port, InetAddress client_host, int client_port) throws IOException,
            UnknownHostException {
        return factory.createSocket(host, port, client_host, client_port);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoclose) throws IOException, UnknownHostException {
        return factory.createSocket(socket, host, port, autoclose);
    }

    public static synchronized SocketFactory getDefault() {
        return getDefaultFactory();
    }

    public static void setCertStorePath(String path) {
        AdvancedSocketFactory.certStorePath = path;
    }

    public static void setCertStorePassword(String password) {
        AdvancedSocketFactory.certStorePwd = password;
    }

    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    private static SocketFactory getDefaultFactory() {
        if (default_factory == null)
            default_factory = new AdvancedSocketFactory();
        return default_factory;
    }

    private TrustManager[] getDefaultTrustManager() {
        if (trustManagers == null)
            if (AdvancedSocketFactory.certStorePwd != null) {
                trustManagers = (new Truster[] { new Truster(AdvancedSocketFactory.certStorePath,
                        AdvancedSocketFactory.certStorePwd) });
            } else {
                trustManagers = (new Truster[] { new Truster(AdvancedSocketFactory.certStorePath) });
            }
        return trustManagers;
    }

    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    private void init(KeyStore ks, char password[]) {
        SSLContext ctx = null;
        com.sun.net.ssl.KeyManager keyManagers[] = null;
        TrustManager trustManagers[] = null;
        try {
            if (ks != null) {
                KeyManagerFactory kmf = null;
                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password);
                keyManagers = kmf.getKeyManagers();
            }
            ctx = SSLContext.getInstance("TLS");
            trustManagers = getDefaultTrustManager();
            ctx.init(keyManagers, trustManagers, null);
            factory = ctx.getSocketFactory();
        } catch (Exception e) {
            System.err.println("ASF Error: failed to initialize : " + e.getMessage());
            e.printStackTrace();
        }
    }

    static {
        Security.addProvider(new Provider());
    }
    
    public static void alwaysTrust() {
        trustManagers = ALWAYS_TRUST_MANAGER;
    }
}
