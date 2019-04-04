package talend.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class Truster implements X509TrustManager {

    private String certStore;

    private char certStorePwd[];

    private X509TrustManager trustManager;

    private boolean isSaveCA = true;

    private KeyStore ks;

    public Truster(String certStorePath) {
        certStore = null;
        certStorePwd = null;
        trustManager = null;
        ks = null;
        if (certStorePath == null) {
            isSaveCA = false;
            certStore = "talendcecerts";
        } else {
            certStore = certStorePath;
        }
        certStorePwd = "changeit".toCharArray();
        init();
    }

    public Truster(String certStorePath, String password) {
        certStore = null;
        certStorePwd = null;
        trustManager = null;
        ks = null;
        if (certStorePath == null) {
            isSaveCA = false;
            certStore = "talendcecerts";
        } else {
            certStore = certStorePath;
        }
        if (password != null && password.length() > 0) {
            password.toCharArray();
        }
        init();
    }

    public X509Certificate[] getAcceptedIssuers() {
        if (trustManager == null)
            return null;
        else
            return trustManager.getAcceptedIssuers();
    }

    private X509Certificate getCACert(X509Certificate chain[]) {
        X509Certificate ca = chain[chain.length - 1];
        if (ca.getSubjectDN().equals(ca.getIssuerDN()))
            return ca;
        else
            return null;
    }

    private void init() {
        try {
            if (certStore.endsWith(".p12"))
                ks = KeyStore.getInstance("PKCS12");
            else
                ks = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            System.err.println("ASF Truster: Failed to create cert store : " + e.getMessage());
            return;
        }
        InputStream in = null;
        if (certStore.indexOf("://") == -1)
            try {
                in = new FileInputStream(certStore);
            } catch (FileNotFoundException _ex) {
            }
        else
            try {
                URL url = new URL(certStore);
                URLConnection con = url.openConnection();
                in = con.getInputStream();
            } catch (MalformedURLException e) {
                System.err.println("ASF Truster: The location of the cert store file is invalid: " + e.getMessage());
            } catch (IOException _ex) {
            }
        try {
            ks.load(in, certStorePwd);
        } catch (Exception e) {
            System.err.println("ASF Truster: Failed to load the cert store : " + e.getMessage());
            return;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception _ex) {
                }
        }
        try {
            trustManager = initTrustManager(ks);
        } catch (Exception e) {
            System.err.println("ASF Truster: Failed to create initial trust manager : " + e.getMessage());
            return;
        }
    }

    private X509TrustManager initTrustManager(KeyStore ks) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = null;
        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        javax.net.ssl.TrustManager trusts[] = trustManagerFactory.getTrustManagers();
        return (X509TrustManager) trusts[0];
    }

    private boolean isAccepted(X509Certificate caCert) {
        X509Certificate certs[] = getAcceptedIssuers();
        if (certs == null)
            return false;
        for (int i = 0; i < certs.length; i++)
            if (caCert.equals(certs[i]))
                return true;

        return false;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (trustManager == null)
            throw new CertificateException("Trust manager is not initialized");
        else
            trustManager.checkClientTrusted(chain, authType);
    }

    private boolean saveStore() {
        OutputStream out = null;
        try {
            try {
                if (certStore.indexOf("://") == -1) {
                    out = new FileOutputStream(certStore);
                } else {
                    URL url = new URL(certStore);
                    URLConnection con = url.openConnection();
                    con.setDoOutput(true);
                    out = con.getOutputStream();
                }
                ks.store(out, certStorePwd);
                return true;
            } catch (Exception e) {
                System.err.println("ASF Truster: Failed to save trust store : " + e.getMessage());
            }
            return false;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (Exception _ex) {
                }
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (trustManager != null) {
            try {
                trustManager.checkServerTrusted(chain, authType);
                return;
            } catch (Exception e) {
            }
        }
        X509Certificate ca = getCACert(chain);
        if (ca != null) {
            if (!isAccepted(ca)) {
                throw new CertificateException("Server certificate chain verification failed.");
            }
            String id = String.valueOf(System.currentTimeMillis());
            X509TrustManager tmpTrustManager = null;
            try {
                ks.setCertificateEntry(id, ca);
                tmpTrustManager = initTrustManager(ks);
            } catch (Exception e) {
                throw new CertificateException("ASF Truster: Failed to create tmp trust store", e);
            }
            try {
                tmpTrustManager.checkServerTrusted(chain, authType);
                if (this.isSaveCA) {
                    saveStore();
                    trustManager = tmpTrustManager;
                }
                return;
            } catch (CertificateException e) {
                throw new CertificateException(
                        "SSL Error:Server certificate chain verification failed and \nthe CA is missing.", e);
            }
        } else {
            throw new CertificateException(
                    "CA certificate is not in the server certificate chain.\nPlease use the keytool command to import the server certificate.");
        }
    }
}
