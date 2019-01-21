package talend.ssl;

import java.security.cert.X509Certificate;
import com.sun.net.ssl.X509TrustManager;

public class AlwaysTruster implements X509TrustManager {

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public boolean isClientTrusted(X509Certificate[] arg0) {
        return true;
    }

    public boolean isServerTrusted(X509Certificate[] arg0) {
        return true;
    }
}
