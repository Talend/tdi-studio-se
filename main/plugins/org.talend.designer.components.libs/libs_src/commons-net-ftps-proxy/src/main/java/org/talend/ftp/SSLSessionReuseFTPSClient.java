package org.talend.ftp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;

import org.apache.commons.net.ftp.FTPSClient;

public class SSLSessionReuseFTPSClient extends FTPSClient {

    public SSLSessionReuseFTPSClient(boolean isImplicit, SSLContext context) {
        super(isImplicit, context);
    }

    @Override
    protected void _prepareDataSocket_(final Socket socket) {
        if (socket instanceof SSLSocket) {
            final SSLSession session = ((SSLSocket) _socket_).getSession();
            final SSLSessionContext context = session.getSessionContext();
            try {
                final Field sessionHostPortCache = context.getClass().getDeclaredField("sessionHostPortCache");
                sessionHostPortCache.setAccessible(true);
                final Object cache = sessionHostPortCache.get(context);
                final Method putMethod = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
                putMethod.setAccessible(true);
                InetAddress address = socket.getInetAddress();
                int port = socket.getPort();
                
                String key = String.format("%s:%s", address.getHostName(), String.valueOf(port)).toLowerCase(Locale.ROOT);
                putMethod.invoke(cache, key, session);
                
                key = String.format("%s:%s", address.getHostAddress(), String.valueOf(port)).toLowerCase(Locale.ROOT);
                putMethod.invoke(cache, key, session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
